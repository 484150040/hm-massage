package com.hm.digital.massage.listener;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hm.digital.common.enums.ConfigEnum;
import com.hm.digital.inface.biz.ConfigsService;
import com.hm.digital.inface.entity.Config;
import com.hm.digital.massage.dto.MqttDo;
import com.hm.digital.massage.util.Callback;
import com.hm.digital.massage.util.MQTTConnect;

import lombok.extern.slf4j.Slf4j;

/**
 * 项目启动 监听主题
 *
 * @author Mr.Qu
 * @since 2020/1/10
 */
@Slf4j
@Component
public class MQTTListener implements ApplicationListener<ContextRefreshedEvent> {

  private final MQTTConnect server;

  private static boolean isture;

  private static List<MqttDo> list;

  @Autowired
  public ConfigsService configsServices;
  @PostConstruct
  public void init() {
    try {
      Config config = configsServices.getValue(getCofig(ConfigEnum.MQTT_CONFIGS_CLIENTS.getKey())).get(0);
      if (config.getStatus()<2){
        config.setStatus(2);
        configsServices.save(config);
      }
      list = JSONObject
          .parseArray(config.getValue(),
              MqttDo.class);
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
  @Autowired
  public MQTTListener(MQTTConnect server) {
    this.server = server;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    try {
      if (isture) {
        return;
      }
      for (MqttDo mqttDo : list) {
        server.setMqttClient(mqttDo.getClientId(), mqttDo.getUserName(), mqttDo.getPassword(), new Callback());
        isture = true;
      }
//      server.sub("com/iot/init");
    } catch (MqttException e) {
      log.error(e.getMessage(), e);
    }
  }
}

