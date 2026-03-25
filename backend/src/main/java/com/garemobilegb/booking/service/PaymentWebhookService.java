package com.garemobilegb.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garemobilegb.booking.domain.PaymentProvider;
import com.garemobilegb.booking.dto.PaymentWebhookRequest;
import com.garemobilegb.shared.config.PaymentGatewayProperties;
import com.garemobilegb.shared.exceptions.BusinessException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentWebhookService {

  private final BookingService bookingService;
  private final PaymentGatewayProperties paymentGatewayProperties;
  private final PaymentWebhookSignatureVerifier signatureVerifier;
  private final ObjectMapper objectMapper;
  private final MeterRegistry meterRegistry;

  public PaymentWebhookService(
      BookingService bookingService,
      PaymentGatewayProperties paymentGatewayProperties,
      PaymentWebhookSignatureVerifier signatureVerifier,
      ObjectMapper objectMapper,
      MeterRegistry meterRegistry) {
    this.bookingService = bookingService;
    this.paymentGatewayProperties = paymentGatewayProperties;
    this.signatureVerifier = signatureVerifier;
    this.objectMapper = objectMapper;
    this.meterRegistry = meterRegistry;
  }

  @Transactional
  public void handleWebhook(
      String providerPath,
      String rawBody,
      String sandboxSecretHeader,
      String signatureHeader) {
    if (paymentGatewayProperties.sandbox()) {
      if (!signatureVerifier.verifySandboxSecret(sandboxSecretHeader)) {
        throw new BusinessException(
            HttpStatus.UNAUTHORIZED,
            "WEBHOOK_UNAUTHORIZED",
            "Secret sandbox invalide (X-Sandbox-Secret).");
      }
    } else {
      if (!signatureVerifier.verifyProductionHmac(rawBody, signatureHeader)) {
        throw new BusinessException(
            HttpStatus.UNAUTHORIZED,
            "WEBHOOK_SIGNATURE_INVALID",
            "Signature HMAC invalide (X-Signature: sha256=...).");
      }
    }

    PaymentWebhookRequest req;
    try {
      req = objectMapper.readValue(rawBody, PaymentWebhookRequest.class);
    } catch (Exception e) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST, "WEBHOOK_BODY_INVALID", "Corps JSON invalide.");
    }
    if (!"SUCCESS".equalsIgnoreCase(req.status()) && !"PAID".equalsIgnoreCase(req.status())) {
      return;
    }
    PaymentProvider provider = resolveProvider(req.provider(), providerPath);
    bookingService.confirmPaymentFromWebhook(
        req.bookingId(), provider, req.externalTransactionId());
    meterRegistry
        .counter("gare.payments.webhook.success", "provider", provider.name())
        .increment();
  }

  private static PaymentProvider resolveProvider(String fromBody, String pathSegment) {
    if (fromBody != null && !fromBody.isBlank()) {
      try {
        return PaymentProvider.valueOf(fromBody.trim());
      } catch (IllegalArgumentException e) {
        throw new BusinessException(
            HttpStatus.BAD_REQUEST,
            "WEBHOOK_PROVIDER_INVALID",
            "Valeur provider invalide: " + fromBody);
      }
    }
    return mapProviderFromPath(pathSegment);
  }

  private static PaymentProvider mapProviderFromPath(String pathSegment) {
    if (pathSegment == null) {
      return PaymentProvider.INTERNAL;
    }
    return switch (pathSegment.toLowerCase()) {
      case "orange", "orange-money", "orangemoney" -> PaymentProvider.ORANGE_MONEY;
      case "wave" -> PaymentProvider.WAVE;
      case "mtn", "mtn-momo" -> PaymentProvider.MTN;
      case "sandbox" -> PaymentProvider.ORANGE_MONEY;
      default -> throw new BusinessException(
          HttpStatus.BAD_REQUEST, "WEBHOOK_PROVIDER_UNKNOWN", "Passerelle inconnue: " + pathSegment);
    };
  }
}
