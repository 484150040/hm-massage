package com.hm.digital.massage.enums;

import lombok.Getter;

@Getter
public enum RabbitMqEnum {
  /**
   * 点名刷卡
   */
  ROLL_CALL_CREDIT_CARD("330100111_personPosition", "330100111.person_position"),
  /**
   * 点名开始
   */
  ROLL_CALL_BEGINS("330100111_trigger", "jc.rollcall.notice");
  /**
   * 请求参数exchange
   */
  String exchange;

  /**
   * 请求参数routingKey
   */
  String routingKey;

  RabbitMqEnum(String exchange, String routingKey) {
    this.exchange = exchange;
    this.routingKey = routingKey;
  }

}
