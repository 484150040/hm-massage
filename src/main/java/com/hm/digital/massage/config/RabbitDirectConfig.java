package com.hm.digital.massage.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitDirectConfig {

  public static final String NORMAL_QUEUE = "normalQueue";
  public static final String DEAD_EXCHANGE = "deadExchange";
  public static final String NORMAL_EXCHANGE = "normalExchange";
  public static final String DEAD_QUEUE = "deadqueue";

  @Bean
  public Queue directQueue() {
    //参数介绍
    //1.队列名 2.是否持久化 3.是否独占 4.自动删除 5.其他参数
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("x-message-ttl", 10000); // 消息过期时间
    arguments.put("x-dead-letter-exchange", DEAD_EXCHANGE); // 死信的交换机
    arguments.put("x-dead-letter-routing-key", "dead"); // 死信的RoutingKey
    return new Queue(NORMAL_QUEUE, false, false, false, arguments);
  }

  @Bean
  public DirectExchange directExchange() {
    //参数介绍
    //1.交换器名 2.是否持久化 3.自动删除 4.其他参数
    return new DirectExchange(NORMAL_EXCHANGE, false, false, null);
  }

  @Bean
  public Binding bingExchange() {
    return BindingBuilder.bind(directQueue())   //绑定队列
        .to(directExchange())       //队列绑定到哪个交换器
        .with("normal");         //绑定路由key,必须指定
  }
  // 死信队列
  @Bean
  public Queue deadQueue() {
    //参数介绍
    //1.队列名 2.是否持久化 3.是否独占 4.自动删除 5.其他参数
    return new Queue(DEAD_QUEUE, false, false, false, null);
  }

  @Bean
  public DirectExchange deadExchange() {
    //参数介绍
    //1.交换器名 2.是否持久化 3.自动删除 4.其他参数
    return new DirectExchange(DEAD_EXCHANGE, false, false, null);
  }

  @Bean
  public Binding bingDeadExchange() {
    return BindingBuilder.bind(deadQueue())   //绑定队列
        .to(deadExchange())       //队列绑定到哪个交换器
        .with("dead");         //绑定路由key,必须指定
  }
}

