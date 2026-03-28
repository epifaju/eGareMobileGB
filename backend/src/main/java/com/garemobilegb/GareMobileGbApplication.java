package com.garemobilegb;

import com.garemobilegb.shared.config.AuthDevProperties;
import com.garemobilegb.shared.config.BoardingJwtProperties;
import com.garemobilegb.shared.config.BookingProperties;
import com.garemobilegb.shared.config.ReceiptProperties;
import com.garemobilegb.shared.config.RefundProperties;
import com.garemobilegb.booking.payment.config.MobileMoneyProviderProperties;
import com.garemobilegb.shared.config.PaymentGatewayProperties;
import com.garemobilegb.shared.config.JwtProperties;
import com.garemobilegb.shared.config.RateLimitProperties;
import com.garemobilegb.shared.config.SmsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
  JwtProperties.class,
  BoardingJwtProperties.class,
  RateLimitProperties.class,
  AuthDevProperties.class,
  BookingProperties.class,
  RefundProperties.class,
  ReceiptProperties.class,
  PaymentGatewayProperties.class,
  MobileMoneyProviderProperties.class,
  SmsProperties.class
})
public class GareMobileGbApplication {

  public static void main(String[] args) {
    SpringApplication.run(GareMobileGbApplication.class, args);
  }
}
