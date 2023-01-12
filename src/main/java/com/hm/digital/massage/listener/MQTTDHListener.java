package com.hm.digital.massage.listener;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.hm.digital.common.enums.ConfigEnum;
import com.hm.digital.inface.biz.ConfigsService;
import com.hm.digital.inface.entity.Config;
import com.hm.digital.massage.dto.MqttDo;
import com.hm.digital.massage.util.Callback;
import com.hm.digital.massage.util.CallbackDH;
import com.hm.digital.massage.util.MQTTConnect;
import com.hm.digital.massage.util.MQTTConnectDH;

import lombok.extern.slf4j.Slf4j;

/**
 * 项目启动 监听主题
 *
 * @author Mr.Qu
 * @since 2020/1/10
 */
@Slf4j
@Component
public class MQTTDHListener implements ApplicationListener<ContextRefreshedEvent> {

  private final MQTTConnectDH server;

  @Autowired
  public MQTTDHListener(MQTTConnectDH server) {
    this.server = server;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    try {
      server.setMqttClient("62d50a5a4c7c4e3646bb4a9d2023012302",  new CallbackDH());
//      server.sub("com/iot/init");
    } catch (MqttException e) {
      log.error(e.getMessage(), e);
    }
  }
}

