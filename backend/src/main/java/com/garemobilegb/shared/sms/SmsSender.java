package com.garemobilegb.shared.sms;

public interface SmsSender {

  /** Envoie (ou simule) un SMS contenant l’OTP. Ne jamais journaliser la valeur complète en prod. */
  void sendOtpSms(String phoneNumber, String otpCode);

  /** SMS transactionnel (reçu, rappel, …). Ne pas journaliser le corps complet en prod. */
  void sendTransactionalSms(String phoneNumber, String message);
}
