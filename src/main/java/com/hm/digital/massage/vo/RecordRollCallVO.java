package com.hm.digital.massage.vo;

import org.springframework.data.jpa.domain.Specification;

import com.hm.digital.common.config.QueryCondition;
import com.hm.digital.common.enums.MatchType;
import com.hm.digital.common.query.BaseQuery;
import com.hm.digital.inface.entity.MqRollCall;
import com.hm.digital.inface.entity.RecordRollCall;

import lombok.Data;

@Data
public class RecordRollCallVO extends BaseQuery<RecordRollCall> {

  /**
   * 点名次数
   */
  @QueryCondition(func = MatchType.equal)
  private String rollCallCount;

  /**
   * 监所编号
   */
  @QueryCondition(func = MatchType.equal)
  private String prisonId;

  /**
   *监室
   */
  @QueryCondition(func = MatchType.equal)
  private String dormCode;

  @Override
  public Specification<RecordRollCall> toSpec() {
    return super.toSpecWithAnd();
  }

}
