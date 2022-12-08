package com.hm.digital.twin.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class MqttDo implements Serializable {
  /**
   * clientId
   */
  private String clientId;
  /**
   * 用户名
   */
  private String userName;
  /**
   * 密码
   */
  private String password;
}
