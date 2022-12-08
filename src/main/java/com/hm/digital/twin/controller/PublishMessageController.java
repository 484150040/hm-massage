package com.hm.digital.twin.controller;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hm.digital.twin.config.RabbitDirectConfig;

@RestController
public class PublishMessageController {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @RequestMapping("/receiveMessage")
  public void receiveMessage(String str) {
    System.out.println("===" + str);
    rabbitTemplate.convertAndSend(RabbitDirectConfig.NORMAL_QUEUE, "接收到的信息为" + str);
  }
}


