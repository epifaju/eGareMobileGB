package com.garemobilegb.booking.payment.config;

import com.garemobilegb.booking.domain.PaymentProvider;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Intégration HTTP des passerelles (URLs et clés en env). En sandbox, ces champs sont ignorés
 * (simulateur local).
 */
@ConfigurationProperties(prefix = "app.payment.mobile-money")
public record MobileMoneyProviderProperties(
    /** Ordre de secours ; vide = Orange → Wave → MTN. */
    List<PaymentProvider> fallbackOrder,
    RetrySettings retry,
    OrangeMoneyEndpoint orangeMoney,
    ProviderEndpoint wave,
    ProviderEndpoint mtn) {

  public List<PaymentProvider> effectiveFallbackOrder() {
    if (fallbackOrder == null || fallbackOrder.isEmpty()) {
      return List.of(PaymentProvider.ORANGE_MONEY, PaymentProvider.WAVE, PaymentProvider.MTN);
    }
    return fallbackOrder;
  }

  public RetrySettings retryOrDefault() {
    return retry != null ? retry : new RetrySettings(false, 120_000L);
  }

  public record RetrySettings(boolean enabled, long pollIntervalMs) {}

  /**
   * @param baseUrl URL de base API
   * @param apiKey clé ou Bearer selon la doc opérateur
   * @param initiatePath chemin relatif
   * @param responseRedirectField champ JSON pour l’URL (défaut redirectUrl)
   */
  /**
   * @param responseRedirectField nom du champ JSON pour l’URL de redirection (défaut {@code redirectUrl})
   */
  public record ProviderEndpoint(
      String baseUrl, String apiKey, String initiatePath, String responseRedirectField) {}
}
