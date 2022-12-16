package com.hm.digital.massage.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.hm.digital.common.enums.ConfigEnum;
import com.hm.digital.common.enums.InputParameterEnum;
import com.hm.digital.common.exception.BaseException;
import com.hm.digital.common.rest.BaseController;
import com.hm.digital.common.utils.HttpClientUtil;
import com.hm.digital.inface.biz.ConfigsService;
import com.hm.digital.inface.entity.Config;
import com.hm.digital.inface.entity.RecordRollCall;
import com.hm.digital.inface.entity.Statistical;
import com.hm.digital.inface.mapper.RecordRollCallMapper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("recordrollcall")
@Slf4j
public class RecordRollCallController extends BaseController<RecordRollCallMapper, RecordRollCall> {

  @Value("${zh.httpGetChart}")
  private String httpGetChart;

  @Value("${zh.electronicCallStart}")
  private String electronicCallStart;

  @Autowired
  private ConfigsService configsService;

  /**
   * 发起点名
   *
   * @return
   */
  @SneakyThrows
  @RequestMapping("/start")
  @ExceptionHandler(value = BaseException.class)
  public String start(String prisonId,String dormCodes) {
    //查询每个监室所有数据
    Map<String, String> parametersRed = new HashMap<>();
    parametersRed.put("item", InputParameterEnum.DORM_CODE_PRISON_CHART.getKey());
    parametersRed.put("prisonId", prisonId);
    String responseRed = HttpClientUtil.sendGet(httpGetChart, parametersRed);
    responseRed = responseRed.replaceAll("jsh", "NAME");
    List<Statistical> lists = JSONObject.parseArray(responseRed, Statistical.class);
    String dormCode [] = dormCodes.split(",");
    Config config = configsService.getValue(ConfigEnum.ROLL_CALL.getKey(), prisonId).get(0);
    config.setValue(String.valueOf(Integer.valueOf(config.getValue()) + 1));
    configsService.updete(config);
    for (Statistical list : lists) {
      for (String s : dormCode) {
        if (list.getNAME().equals(s)){
          RecordRollCall recordRol = new RecordRollCall();
          recordRol.setDormCode(s);
          recordRol.setPrisonId(prisonId);
          recordRol.setRollCallCount(config.getValue());
          recordRol.setActualNumberPeople("0");
          recordRol.setNumberArrivals(list.getTotal());
          recordRol.setProgress("0");
          baseBiz.save(recordRol);
          break;
        }
      }
    }
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("dormCodes", dormCodes);
    parameters.put("prisonId", prisonId);
    String response = HttpClientUtil.httpPostRequest(electronicCallStart, parameters);
    return response;
  }
}
