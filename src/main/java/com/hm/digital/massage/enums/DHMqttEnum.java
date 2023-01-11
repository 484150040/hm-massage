package com.hm.digital.massage.enums;

import lombok.Getter;

@Getter
public enum DHMqttEnum {

  /**
   * 获取机库 SN/上报拓扑信息(消息体 method 字段值：device_topo)
   */
  SYS_PRODUCT_STATUS("sys/product/#{sn}/status", "sys/product/#{sn}/status"),

  /**
   *上报机库状态信息(消息体 method 字段值：nest_status)/上报无人机状态信息(消息体 method 字段值：drone_status)
   */
  SYS_PRODUCT_OSD("thing/product/#{sn}/osd", "thing/product/#{sn}/osd"),

  /**
   *上报无人机执行航迹任务进度状态信息(消息体 method 字段值：task_status)
   */
  SYS_PRODUCT_STATE("thing/product/#{sn}/state", "thing/product/#{sn}/state"),

  /**
   *上报通知事件(消息体 method 字段值：nest_realtime_status)
   */
  SYS_PRODUCT_EVENT("thing/product/#{sn}/events", "thing/product/#{sn}/events"),

  /**
   *下发航迹任务(消息体 method 字段值：start_task)/功能操作(消息体 method 字段值：function_operation)
   */
  SYS_PRODUCT_SERVICES("thing/product/#{sn}/services", "thing/product/#{sn}/services");

  /**
   * 请求参数exchange
   */
  String value;

  /**
   * 请求参数routingKey
   */
  String routingKey;

  DHMqttEnum(String value, String routingKey) {
    this.value = value;
    this.routingKey = routingKey;
  }

}
