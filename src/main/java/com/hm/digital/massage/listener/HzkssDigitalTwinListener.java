package com.hm.digital.massage.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hm.digital.common.enums.ConfigEnum;
import com.hm.digital.common.rest.BaseController;
import com.hm.digital.common.utils.CronUtils;
import com.hm.digital.common.utils.DateUtils;
import com.hm.digital.common.utils.HttpClientUtil;
import com.hm.digital.inface.biz.ConfigsService;
import com.hm.digital.inface.biz.RecordRollCallService;
import com.hm.digital.inface.entity.Config;
import com.hm.digital.inface.entity.ElectronicCall;
import com.hm.digital.inface.entity.MqRollCall;
import com.hm.digital.inface.entity.RecordRollCall;
import com.hm.digital.inface.mapper.MqRollCallMapper;
import com.hm.digital.massage.dto.RollCallDto;
import com.hm.digital.massage.dto.RollCallPerSonDto;
import com.hm.digital.massage.feign.QuartzFeignBiz;
import com.hm.digital.massage.util.MQTTConnect;
import com.hm.digital.massage.vo.MqRollCallVO;
import com.hm.digital.massage.vo.RecordRollCallVO;

import lombok.SneakyThrows;

@Component
public class HzkssDigitalTwinListener extends BaseController<MqRollCallMapper, MqRollCall> {

//  @Value("${zh.httpGetChart}")
  private static String httpGetChart;


//  @Value("${zh.electronicCall}")
  private static String electronicCall;

  @Autowired
  private MQTTConnect mqttConnect;

  @Autowired
  private QuartzFeignBiz quartzFeignBiz;

  @Autowired
  private ConfigsService configService;

  @Autowired
  private RecordRollCallService recordRollCallService;


  @Autowired
  public ConfigsService configsServices;
  @PostConstruct
  public void init() {
    try {
      electronicCall =  configsServices.getValue(getCofig(ConfigEnum.ZH_ELECTRONICCALL.getKey())).get(0).getValue();
      httpGetChart =  configsServices.getValue(getCofig(ConfigEnum.ZH_HTTPGETCHART.getKey())).get(0).getValue();
    }catch (Exception e){
      e.printStackTrace();
      return;
    }

  }

  private Config getCofig(String config) {
    Config configVO = new Config();
    configVO.setType(config);
    configVO.setUniverse("1");
    return configVO;
  }
  /**
   * 点名刷卡
   *
   * @param messages
   */
  @SneakyThrows
  @RabbitListener(bindings = {
      @QueueBinding(
          value = @Queue,
          exchange = @Exchange(name = "330100111_personPosition", type = "topic"),
          key = {"330100111.person_position"}
      )
  })
  public void creditCard(String messages) {
    System.out.println("点名刷卡" + messages);
    MqRollCall message = JSONObject.parseObject(messages, MqRollCall.class);
    if (!message.getParam().getPersonType().equals("prisoner")) {
      return;
    }
    //新增最近的一条数据
    MqRollCallVO getParam = new MqRollCallVO();
    getParam.setOrder("rollcall");
    getParam.setPrisonId(message.getPrisonId());
    Sort sort = new Sort(Direction.DESC, "count");
    MqRollCall mqRollCall = baseBiz.findAll(getParam.toSpec(), sort).get(0);
    if (mqRollCall.getEndTime().getTime() < message.getParam().getAlarmTime()) {
      return;
    }
    message.setCreateTime(new Date());
    message.setCount(mqRollCall.getCount());
    baseBiz.save(message);
    RecordRollCall recordRollCall = findRecordRollCall(message, mqRollCall);
    recordRollCallService.update(recordRollCall);
    //查询出结果并发送到Mqtt
    sendNotification(message, mqRollCall.getEndTime().getTime());

  }

  /**
   * 查询出结果并发送到Mqtt
   *
   * @param message
   * @param time
   */
  private void sendNotification(MqRollCall message, long time) {
    RecordRollCallVO record = new RecordRollCallVO();
    record.setPrisonId(message.getPrisonId());
    record.setRollCallCount(message.getCount());
    List<RecordRollCall> list = recordRollCallService.findAll(record.toSpec());
    Map<String, Object> map = new HashMap<>();
    map.put("list", list);
    map.put("endTime", time);
    try {
      mqttConnect.pub("personPosition", JSON.toJSONString(map));
      Boolean result = true;
      for (RecordRollCall recordRollCall : list) {
        if (Double.valueOf(recordRollCall.getProgress()) < 1) {
          result = false;
          break;
        }
      }
      if (result) {
        Thread.sleep(1000);
        Map<String, Object> maps = getResult("即时", message.getPrisonId());
        mqttConnect.pub("personResult", JSON.toJSONString(maps));
        String jobClassName = "com.hm.digital.clocking.job.RollCallJob";
        quartzFeignBiz.deletejob(jobClassName, message.getPrisonId());
      }
    } catch (MqttException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println(JSON.toJSONString(map));
  }

  /**
   * 查询点名结果
   *
   * @param type
   * @param prisonId
   * @return
   */
  private Map<String, Object> getResult(String type, String prisonId) {
    Map<String, Object> params = new HashMap<>();
    Map<String, String> parameters = new HashMap<>();
    parameters.put("type", type);
    parameters.put("prisonId", prisonId);
    String response = HttpClientUtil.sendGet(electronicCall, parameters);
    RollCallDto rollCallDto = JSONObject.parseObject(response, RollCallDto.class);
    List<ElectronicCall> lists = rollCallDto.getRollcallDormCode();

    List<RollCallPerSonDto> rollPerson = rollCallDto.getRollcallPerson();
    List<RollCallPerSonDto> outPerson = rollCallDto.getRollcallOut();
    List<Map<String, Object>> mapList = new ArrayList<>();
    for (ElectronicCall list : lists) {
      List<RollCallPerSonDto> perSonDtoList = new ArrayList<>();
      List<RollCallPerSonDto> perSonDtoListNo = new ArrayList<>();
      for (RollCallPerSonDto person : rollPerson) {
        if (list.getDormCode().equals(person.getDormCode())) {
          if (person.getCalled()) {
            perSonDtoList.add(person);
          } else {
            person.setRemark("null");
            person.setOutRoom("null");
            perSonDtoListNo.add(person);
          }
        }
      }
      if (!CollectionUtils.isEmpty(outPerson)) {
        for (RollCallPerSonDto person : outPerson) {
          if (list.getDormCode().equals(person.getDormCode())) {
            if (StringUtils.isEmpty(person.getOutRoom())) {
              person.setOutRoom("null");
            }
            perSonDtoListNo.add(person);
          }
        }
      }
      Map<String, Object> map = new HashMap<>();
      map.put("dormCode", list.getDormCode());
      map.put("perSonTrue", perSonDtoList);
      map.put("perSonFalse", perSonDtoListNo);
      mapList.add(map);
    }
    params.put("mapList", mapList);
    params.put("code", 200);
    return params;
  }

  /**
   * 整合数据进行修改
   *
   * @param mqRollCall
   * @param message
   * @return
   */
  private RecordRollCall findRecordRollCall(MqRollCall message, MqRollCall mqRollCall) {
    //查询总的人数
    MqRollCallVO countVO = new MqRollCallVO();
    countVO.setOrder("person_position");
    countVO.setPrisonId(message.getPrisonId());
    countVO.setCount(mqRollCall.getCount());
    countVO.setParam("%\"dormCode\": \"" + message.getParam().getDormCode() + "\"%");
    Long mqRollCallCount = baseBiz.count(countVO.toSpec());
    //查询该监室数据并进行修改、接着查询该点名次数所有数据
    RecordRollCallVO recordRollCallVO = new RecordRollCallVO();
    recordRollCallVO.setPrisonId(message.getPrisonId());
    recordRollCallVO.setDormCode(message.getParam().getDormCode());
    recordRollCallVO.setRollCallCount(message.getCount());
    RecordRollCall recordRollCall = recordRollCallService.selectOne(recordRollCallVO.toSpec());
    recordRollCall.setActualNumberPeople(mqRollCallCount.toString());
    recordRollCall.setProgress(String.format("%.2f",
        Double.valueOf(recordRollCall.getActualNumberPeople()) / Double.valueOf(recordRollCall.getNumberArrivals())));
    return recordRollCall;
  }

  /**
   * 点名开始
   *
   * @param messages
   */
  @RabbitListener(bindings = {
      @QueueBinding(
          value = @Queue,
          exchange = @Exchange(name = "330100111_trigger", type = "topic"),
          key = {"jc.rollcall.notice"}
      )
  })
  public void begins(String messages) {
    System.out.println("点名开始" + messages);
    MqRollCall mqRol = JSONObject.parseObject(messages, MqRollCall.class);
    mqRol.setCreateTime(new Date());
    mqRol.setEndTime(DateUtils.addMinute(new Date(), mqRol.getParam().getLimitedTime()));
    Config config = configService.getValue(ConfigEnum.ROLL_CALL.getKey(), mqRol.getPrisonId()).get(0);
    mqRol.setCount(config.getValue());
    baseBiz.save(mqRol);
    String jobClassName = "com.hm.digital.clocking.job.RollCallJob";
    try {
      String cron = CronUtils.onlyOnce(DateUtils.format(mqRol.getEndTime(), DateUtils.DATE_TIME_FORMAT));
      quartzFeignBiz.addjob(jobClassName, mqRol.getPrisonId(), cron);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private static Class<? extends QuartzJobBean> getClass(String classname) throws Exception {
    Class<?> class1 = Class.forName(classname);
    return (Class<? extends QuartzJobBean>) class1;
  }
}
