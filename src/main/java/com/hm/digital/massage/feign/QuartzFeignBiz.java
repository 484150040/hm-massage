package com.hm.digital.massage.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "clocking-nacos", path = "/clocking")
public interface QuartzFeignBiz {

  @GetMapping(value = "/job/addjob")
  void addjob(@RequestParam(value = "jobClassName") String jobClassName,
      @RequestParam(value = "jobGroupName") String jobGroupName,
      @RequestParam(value = "cronExpression") String cronExpression);

  @GetMapping(value = "/job/deletejob")
  void deletejob(@RequestParam(value = "jobClassName") String jobClassName,
      @RequestParam(value = "jobGroupName") String jobGroupName);
}
