package com.hm.digital.massage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.hm.digital.inface.mapper")
@EntityScan(basePackages = "com.hm.digital.inface.entity")
@EnableFeignClients({"com.hm.digital.massage.feign"})
public class MassageApplication {

  public static void main(String[] args) {
    SpringApplication.run(MassageApplication.class, args);
  }
}

