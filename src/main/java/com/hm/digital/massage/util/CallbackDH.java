package com.hm.digital.massage.util;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.hm.digital.common.utils.DateUtils;
import com.hm.digital.common.utils.ResultData;

import lombok.extern.slf4j.Slf4j;

/**
 * 常规MQTT回调函数
 *
 */
@Slf4j
public class CallbackDH implements MqttCallback {

  /**
   * MQTT 断开连接会执行此方法
   */
  @Override
  public void connectionLost(Throwable throwable) {
    log.info("断开了MQTT连接 ：{}", throwable.getMessage());
    log.error(throwable.getMessage(), throwable);
  }

  /**
   * publish发布成功后会执行到这里
   */
  @Override
  public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    log.info("发布消息成功");
  }

  /**
   * subscribe订阅后得到的消息会执行到这里
   */
  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    MQTTConnect mqttConnect = new MQTTConnect();
    mqttConnect.setMqttClient("62d50a5a4c7c4e3646bb4a9d" + DateUtils.format(new Date(),DateUtils.DATE_FORMAT2), "admin", "root", new Callback());
    mqttConnect.sub(topic);
    mqttConnect.pub(topic, new String(message.getPayload()));
    //  TODO    此处可以将订阅得到的消息进行业务处理、数据存储
    log.info("收到来自 " + topic + " 的消息：{}", new String(message.getPayload()));
  }
}

