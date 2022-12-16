package com.hm.digital.massage.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.digital.inface.biz.MqRollCallService;
import com.hm.digital.inface.entity.MqRollCall;
import com.hm.digital.inface.mapper.MqRollCallMapper;


@Service
@Transactional
public class MqRollCallServiceimpl implements MqRollCallService{

  @Autowired
  private MqRollCallMapper mqRollCallMapper;

  @Override
  public MqRollCall findAll(Specification<MqRollCall> toSpec, Sort sort) {
    return mqRollCallMapper.findAll(toSpec,sort).get(0);
  }
}
