package com.garemobilegb;

import com.garemobilegb.shared.config.AuthDevProperties;
import com.garemobilegb.shared.config.BookingProperties;
import com.garemobilegb.shared.config.JwtProperties;
import com.garemobilegb.shared.config.RateLimitProperties;
import com.garemobilegb.shared.config.SmsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
  JwtProperties.class,
  RateLimitProperties.class,
  AuthDevProperties.class,
  BookingProperties.class,
  SmsProperties.class
})
public class GareMobileGbApplication {

  public static void main(String[] args) {
    SpringApplication.run(GareMobileGbApplication.class, args);
  }
}
