package com.hm.digital.twin.listener;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hm.digital.twin.config.RabbitDirectConfig;
import com.hm.digital.twin.util.Callback;
import com.hm.digital.twin.util.MQTTConnect;

@Component
public class TopicConsumer {

  @Autowired
  private MQTTConnect mqttConnect;
  @RabbitListener(queues = RabbitDirectConfig.NORMAL_QUEUE)
  public void receive1(String message) {
    try {
      mqttConnect.setMqttClient("sensor", "admin", "root", new Callback());
      mqttConnect.pub("sensor", message);
    } catch (MqttException e) {
      e.printStackTrace();
    }

  }

  @RabbitListener(bindings = {
      @QueueBinding(
          value = @Queue,
          exchange = @Exchange(name = "topics",  type = "topic"),
          key = {"product.*", "order.#"}
      )
  })
  public void receive2(String message) {
    System.out.println("topic模式接受到的信息2" + message);
  }
}


