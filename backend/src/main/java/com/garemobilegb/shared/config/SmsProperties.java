package com.garemobilegb.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.sms")
public record SmsProperties(SmsProvider provider) {
  public SmsProvider provider() {
    return provider != null ? provider : SmsProvider.LOG;
  }

  public enum SmsProvider {
    /** Aucun envoi (OTP toujours stocké Redis pour login). */
    NONE,
    /** Journalisation uniquement (pas d’SMS réel). */
    LOG,
    /** Fournisseur non branché en Phase 0 — comportement documenté dans docs/SMS_OTP.md. */
    AFRICASTALKING
  }
}
