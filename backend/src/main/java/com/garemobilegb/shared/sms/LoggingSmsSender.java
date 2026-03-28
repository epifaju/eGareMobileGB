package com.garemobilegb.shared.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSmsSender implements SmsSender {

  private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);
  private final boolean warnStub;

  public LoggingSmsSender(boolean warnStub) {
    this.warnStub = warnStub;
  }

  @Override
  public void sendOtpSms(String phoneNumber, String otpCode) {
    if (warnStub) {
      log.warn(
          "SMS OTP non envoyé via passerelle réelle (mode documenté) — voir docs/SMS_OTP.md");
    }
    log.info("OTP SMS simulé pour un numéro se terminant par {}", suffix(phoneNumber));
    log.debug("Longueur OTP (valeur non journalisée) : {}", otpCode != null ? otpCode.length() : 0);
  }

  @Override
  public void sendTransactionalSms(String phoneNumber, String message) {
    if (warnStub) {
      log.warn(
          "SMS transactionnel non envoyé via passerelle réelle — voir docs/SMS_OTP.md");
    }
    log.info(
        "SMS transactionnel simulé pour ***{} — longueur message {} caractères",
        suffix(phoneNumber),
        message != null ? message.length() : 0);
  }

  private static String suffix(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() < 4) {
      return "****";
    }
    return phoneNumber.substring(phoneNumber.length() - 4);
  }
}
