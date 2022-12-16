package com.hm.digital.massage.vo;

import javax.persistence.Column;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.Specification;

import com.hm.digital.common.config.QueryCondition;
import com.hm.digital.common.enums.MatchType;
import com.hm.digital.common.query.BaseQuery;
import com.hm.digital.inface.dto.MqRollCallDto;
import com.hm.digital.inface.entity.MqRollCall;

import lombok.Data;

@Data
public class MqRollCallVO extends BaseQuery<MqRollCall> {

  /**
   * 命令
   */
  @QueryCondition(func = MatchType.equal)
  private String order;

  /**
   * 监所编号
   */
  @QueryCondition(func = MatchType.equal)
  private String prisonId;

  /**
   * 点名次数
   */
  @QueryCondition(func = MatchType.equal)
  private String count;

  /**
   * 内容
   */
  @QueryCondition(func = MatchType.like)
  private String param;

  @Override
  public Specification<MqRollCall> toSpec() {
    return super.toSpecWithAnd();
  }

}
