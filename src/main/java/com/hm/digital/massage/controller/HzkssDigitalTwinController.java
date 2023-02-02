package com.hm.digital.massage.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hm.digital.common.enums.ConfigEnum;
import com.hm.digital.common.rest.BaseController;
import com.hm.digital.common.utils.HttpClientUtil;
import com.hm.digital.common.utils.ResultData;
import com.hm.digital.inface.biz.ConfigsService;
import com.hm.digital.inface.entity.Config;
import com.hm.digital.inface.entity.ElectronicCall;
import com.hm.digital.inface.entity.MqRollCall;
import com.hm.digital.inface.mapper.MqRollCallMapper;
import com.hm.digital.massage.dto.RollCallDto;
import com.hm.digital.massage.dto.RollCallPerSonDto;
import com.hm.digital.massage.enums.RabbitMqEnum;
import com.hm.digital.massage.util.MQTTConnect;
import com.hm.digital.massage.vo.MqRollCallVO;

@RestController
public class HzkssDigitalTwinController extends BaseController<MqRollCallMapper, MqRollCall> {
  @Autowired
  private RabbitTemplate rabbitTemplate;

  //  @Value("${zh.electronicCall}")
  private static String electronicCall;

  @Autowired
  private MQTTConnect mqttConnect;

  @Autowired
  public ConfigsService configsServices;

  @PostConstruct
  public void init() {
    try {
      Config config = configsServices.getValue(getCofig(ConfigEnum.ZH_ELECTRONICCALL.getKey())).get(0);
      if (config.getStatus()<2){
        config.setStatus(2);
        configsServices.save(config);
      }
      electronicCall = config.getValue();
    } catch (Exception e) {
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

  @RequestMapping("/trigger")
  public void trigger(@RequestBody MqRollCall mqRol) {
    System.out.println("===" + mqRol);
    rabbitTemplate
        .convertAndSend(RabbitMqEnum.ROLL_CALL_BEGINS.getExchange(), RabbitMqEnum.ROLL_CALL_BEGINS.getRoutingKey(),
            JSONObject.toJSONString(mqRol));
  }

  @RequestMapping("/personPosition")
  public void personPosition(@RequestBody MqRollCall mqRol) {
    System.out.println("===" + mqRol);
    mqRol.getParam().setAlarmTime(System.currentTimeMillis());
    rabbitTemplate.convertAndSend(RabbitMqEnum.ROLL_CALL_CREDIT_CARD.getExchange(),
        RabbitMqEnum.ROLL_CALL_CREDIT_CARD.getRoutingKey(), JSONObject.toJSONString(mqRol));
  }


  /**
   * 查询点名列表
   *
   * @param prisonId
   * @return
   */
  @GetMapping("/findByPrisonIdAndType")
  public ResultData findByPrisonIdAndType(String prisonId) {
    MqRollCallVO getParam = new MqRollCallVO();
    getParam.setOrder("rollcall");
    getParam.setPrisonId(prisonId);
    Sort sort = new Sort(Direction.DESC, "count");
    MqRollCall mqRollCall = baseBiz.findAll(getParam.toSpec(), sort).get(0);
    if (mqRollCall.getEndTime().getTime() < System.currentTimeMillis()) {
      sendNotification(prisonId);
    }
    return ResultData.success();
  }

  /**
   * 查询出结果并发送到Mqtt
   *
   */
  private void sendNotification(String prisonId) {
    Map<String, Object> maps = getResult("即时", prisonId);
    try {
      mqttConnect.pub("personResult", JSON.toJSONString(maps));
    } catch (MqttException e) {
      e.printStackTrace();
    }
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
}
