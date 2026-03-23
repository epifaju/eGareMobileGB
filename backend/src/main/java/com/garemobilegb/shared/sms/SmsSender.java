package com.garemobilegb.shared.sms;

@FunctionalInterface
public interface SmsSender {

  /** Envoie (ou simule) un SMS contenant l’OTP. Ne jamais journaliser la valeur complète en prod. */
  void sendOtpSms(String phoneNumber, String otpCode);
}
