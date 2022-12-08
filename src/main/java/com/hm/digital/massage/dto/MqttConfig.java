package com.hm.digital.massage.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "mqtt-configs")
public class MqttConfig {
  private List<MqttDo> clients;
}
