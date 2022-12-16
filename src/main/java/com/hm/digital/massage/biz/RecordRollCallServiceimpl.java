package com.hm.digital.massage.biz;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.digital.inface.biz.RecordRollCallService;
import com.hm.digital.inface.entity.RecordRollCall;
import com.hm.digital.inface.mapper.RecordRollCallMapper;


@Service
@Transactional
public class RecordRollCallServiceimpl implements RecordRollCallService {

  @Autowired
  private RecordRollCallMapper recordRollCallMapper;

  @Override
  public RecordRollCall selectOne(Specification<RecordRollCall> toSpec) {
    return recordRollCallMapper.findOne(toSpec).get();
  }
  @Override
  public RecordRollCall update(RecordRollCall recordRollCall) {
    return recordRollCallMapper.save(recordRollCall);
  }
  @Override
  public List<RecordRollCall> findAll(Specification<RecordRollCall> toSpec) {
    return recordRollCallMapper.findAll(toSpec);
  }
}
