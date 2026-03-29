package com.garemobilegb.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Phase 6 — push Expo + repli SMS (conducteurs : paliers remplissage ; passagers : rappels départ).
 */
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(
    Boolean capacitySmsFallbackEnabled,
    String capacitySmsTemplate,
    Boolean departureRemindersEnabled,
    Long departureReminderScanMs,
    Integer departureImminentMinutes,
    String departurePushPct80Title,
    String departurePushPct80Body,
    String departurePushImminentTitle,
    String departurePushImminentBody,
    String departureSmsPct80Template,
    String departureSmsImminentTemplate,
    Boolean departureSmsFallbackEnabled) {

  public boolean capacitySmsFallbackEnabledOrDefault() {
    return capacitySmsFallbackEnabled == null || capacitySmsFallbackEnabled;
  }

  public String capacitySmsTemplateOrDefault() {
    if (capacitySmsTemplate != null && !capacitySmsTemplate.isBlank()) {
      return capacitySmsTemplate;
    }
    return "eGare : {registrationCode} ({route}) — remplissage {threshold} %. Places {occupied}/{capacity}. "
        + "Ouvrez l’app conducteur.";
  }

  public boolean departureRemindersEnabledOrDefault() {
    return departureRemindersEnabled == null || departureRemindersEnabled;
  }

  public long departureReminderScanMsOrDefault() {
    return departureReminderScanMs != null && departureReminderScanMs >= 5_000L
        ? departureReminderScanMs
        : 60_000L;
  }

  public int departureImminentMinutesOrDefault() {
    return departureImminentMinutes != null && departureImminentMinutes > 0
        ? departureImminentMinutes
        : 15;
  }

  public boolean departureSmsFallbackEnabledOrDefault() {
    return departureSmsFallbackEnabled == null || departureSmsFallbackEnabled;
  }

  public String departurePushPct80TitleOrDefault() {
    if (departurePushPct80Title != null && !departurePushPct80Title.isBlank()) {
      return departurePushPct80Title;
    }
    return "Votre départ approche";
  }

  public String departurePushPct80BodyOrDefault() {
    if (departurePushPct80Body != null && !departurePushPct80Body.isBlank()) {
      return departurePushPct80Body;
    }
    return "{route} — véhicule {registrationCode}. Pensez à vous présenter à l’heure à la gare.";
  }

  public String departurePushImminentTitleOrDefault() {
    if (departurePushImminentTitle != null && !departurePushImminentTitle.isBlank()) {
      return departurePushImminentTitle;
    }
    return "Départ imminent";
  }

  public String departurePushImminentBodyOrDefault() {
    if (departurePushImminentBody != null && !departurePushImminentBody.isBlank()) {
      return departurePushImminentBody;
    }
    return "{route} — {registrationCode} : départ sous peu. Présentez votre billet (QR) à l’embarquement.";
  }

  public String departureSmsPct80TemplateOrDefault() {
    if (departureSmsPct80Template != null && !departureSmsPct80Template.isBlank()) {
      return departureSmsPct80Template;
    }
    return "eGare : rappel trajet {route} ({registrationCode}). Départ prévu {departure}. Réf. résa {bookingId}.";
  }

  public String departureSmsImminentTemplateOrDefault() {
    if (departureSmsImminentTemplate != null && !departureSmsImminentTemplate.isBlank()) {
      return departureSmsImminentTemplate;
    }
    return "eGare : départ imminent — {route} ({registrationCode}), {departure}. Résa {bookingId}. Présentez votre QR.";
  }
}
