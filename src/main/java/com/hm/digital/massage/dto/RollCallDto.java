package com.hm.digital.massage.dto;

import java.io.Serializable;
import java.util.List;

import com.hm.digital.inface.entity.ElectronicCall;

import lombok.Data;

@Data
public class RollCallDto  implements Serializable {

  /**
   * 所有监室人员信息
   */
  private List<RollCallPerSonDto> rollcallPerson;


  /**
   * 外出监室人员信息
   */
  private List<RollCallPerSonDto> rollcallOut;

  /**
   * 所有监室总信息
   */
  private List<ElectronicCall> rollcallDormCode;
}
