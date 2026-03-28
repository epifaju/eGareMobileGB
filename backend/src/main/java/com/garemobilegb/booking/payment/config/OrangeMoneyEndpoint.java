package com.garemobilegb.booking.payment.config;

import org.springframework.util.StringUtils;

/**
 * Configuration Orange Money : flux Web Payment (OAuth + session) ou appel HTTP générique legacy
 * (base-url + POST JSON).
 */
public record OrangeMoneyEndpoint(
    String baseUrl,
    String apiKey,
    String initiatePath,
    String responseRedirectField,
    String oauthTokenUrl,
    String webPaymentUrl,
    String clientId,
    String clientSecret,
    String merchantKey,
    String lang,
    String currency,
    String notifUrl,
    String returnUrl,
    String cancelUrl,
    String responsePaymentUrlField,
    String responsePayTokenField) {

  public boolean webPaymentReady() {
    return StringUtils.hasText(webPaymentUrl)
        && StringUtils.hasText(clientId)
        && StringUtils.hasText(clientSecret)
        && StringUtils.hasText(merchantKey)
        && StringUtils.hasText(oauthTokenUrl);
  }

  public boolean legacyGenericReady() {
    return StringUtils.hasText(baseUrl);
  }

  public String langOrDefault() {
    return StringUtils.hasText(lang) ? lang : "fr";
  }

  public String currencyOrDefault() {
    return StringUtils.hasText(currency) ? currency : "XOF";
  }

  public String paymentUrlFieldOrDefault() {
    return StringUtils.hasText(responsePaymentUrlField) ? responsePaymentUrlField : "payment_url";
  }

  public String payTokenFieldOrDefault() {
    return StringUtils.hasText(responsePayTokenField) ? responsePayTokenField : "pay_token";
  }
}
