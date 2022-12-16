package com.hm.digital.massage.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class RollCallPerSonDto implements Serializable {

  /**
   * id 主键
   */
  private String id;

  /**
   * 点名id
   */
  private String rollcallId;

  /**
   * 人员编号
   */
  private String prisonerNumber;

  /**
   * 人员名称
   */
  private String prisonerName;

  /**
   * 监室
   */
  private String dormCode;

  /**
   * 是否刷卡
   */
  private Boolean called;

  /**
   * 备注
   */
  private String remark;

  /**
   * 外出监室
   */
  private String outRoom;

}
