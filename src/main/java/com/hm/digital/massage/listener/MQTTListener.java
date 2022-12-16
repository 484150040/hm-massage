package com.hm.digital.massage.listener;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.hm.digital.massage.dto.MqttConfig;
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
  @Autowired
  private MqttConfig mqttConfig;
  @Autowired
  public MQTTListener(MQTTConnect server)  {
    this.server = server;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    try {
      if (isture){
        return;
      }
      List<MqttDo> list = mqttConfig.getClients();
      for (MqttDo mqttDo : list) {
        server.setMqttClient(mqttDo.getClientId(),mqttDo.getUserName(), mqttDo.getPassword(), new Callback());
        isture=true;
      }
//      server.sub("com/iot/init");
    } catch (MqttException e) {
      log.error(e.getMessage(), e);
    }
  }
}

