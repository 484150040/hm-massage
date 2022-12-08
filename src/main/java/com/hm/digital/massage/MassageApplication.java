package com.hm.digital.massage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class MassageApplication {

  public static void main(String[] args) {
    SpringApplication.run(MassageApplication.class, args);
  }
}

