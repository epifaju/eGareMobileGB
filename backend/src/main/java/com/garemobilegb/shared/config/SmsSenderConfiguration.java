package com.garemobilegb.shared.config;

import com.garemobilegb.shared.sms.LoggingSmsSender;
import com.garemobilegb.shared.sms.SmsSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsSenderConfiguration {

  @Bean
  public SmsSender smsSender(SmsProperties properties) {
    return switch (properties.provider()) {
      case NONE -> (phone, code) -> {};
      case LOG -> new LoggingSmsSender(false);
      case AFRICASTALKING -> new LoggingSmsSender(true);
    };
  }
}
