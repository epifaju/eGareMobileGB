package com.garemobilegb.booking.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentProvider;
import com.garemobilegb.booking.payment.config.MobileMoneyProviderProperties;
import com.garemobilegb.booking.payment.config.MobileMoneyProviderProperties.ProviderEndpoint;
import com.garemobilegb.booking.payment.config.OrangeMoneyEndpoint;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Appels HTTP vers les API opérateurs (prod). Orange Money : Web Payment OAuth prioritaire, sinon
 * POST générique si {@code base-url} seul est renseigné.
 */
@Component
public class MobileMoneyRemoteInitiator {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final MobileMoneyProviderProperties props;
  private final OrangeMoneyWebPaymentClient orangeMoneyWebPaymentClient;

  public MobileMoneyRemoteInitiator(
      RestClient restClient,
      ObjectMapper objectMapper,
      MobileMoneyProviderProperties props,
      OrangeMoneyWebPaymentClient orangeMoneyWebPaymentClient) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
    this.props = props;
    this.orangeMoneyWebPaymentClient = orangeMoneyWebPaymentClient;
  }

  public Optional<RemoteInitiateResult> initiate(
      PaymentProvider provider, Booking booking, Payment payment) {
    if (provider == PaymentProvider.ORANGE_MONEY) {
      Optional<RemoteInitiateResult> om = orangeMoneyWebPaymentClient.initiate(booking, payment);
      if (om.isPresent()) {
        return om;
      }
      OrangeMoneyEndpoint ep = props.orangeMoney();
      if (ep != null && ep.legacyGenericReady()) {
        return genericInitiate(
            ep.baseUrl(),
            ep.apiKey(),
            ep.initiatePath(),
            ep.responseRedirectField(),
            provider,
            booking,
            payment);
      }
      return Optional.empty();
    }
    ProviderEndpoint ep = endpointFor(provider);
    if (ep == null || ep.baseUrl() == null || ep.baseUrl().isBlank()) {
      return Optional.empty();
    }
    return genericInitiate(
        ep.baseUrl(),
        ep.apiKey(),
        ep.initiatePath(),
        ep.responseRedirectField(),
        provider,
        booking,
        payment);
  }

  private Optional<RemoteInitiateResult> genericInitiate(
      String baseUrl,
      String apiKey,
      String initiatePath,
      String responseRedirectField,
      PaymentProvider provider,
      Booking booking,
      Payment payment) {
    String path = initiatePath != null ? initiatePath : "";
    if (!path.isEmpty() && !path.startsWith("/")) {
      path = "/" + path;
    }
    String url = baseUrl.replaceAll("/$", "") + path;
    Map<String, Object> body = new HashMap<>();
    body.put("reference", "booking-" + booking.getId());
    body.put("amount", payment.getAmount());
    body.put("currency", payment.getCurrency());
    body.put("providerHint", provider.name());
    try {
      String raw =
          restClient
              .post()
              .uri(URI.create(url))
              .contentType(MediaType.APPLICATION_JSON)
              .headers(
                  h -> {
                    if (apiKey != null && !apiKey.isBlank()) {
                      h.setBearerAuth(apiKey);
                    }
                  })
              .body(body)
              .retrieve()
              .body(String.class);
      if (raw == null || raw.isBlank()) {
        return Optional.empty();
      }
      JsonNode root = objectMapper.readTree(raw);
      String field =
          responseRedirectField != null && !responseRedirectField.isBlank()
              ? responseRedirectField
              : "redirectUrl";
      if (!root.has(field) || root.get(field).isNull()) {
        return Optional.empty();
      }
      String redirect = root.get(field).asText();
      String ext = null;
      if (root.has("transactionId") && !root.get("transactionId").isNull()) {
        ext = root.get("transactionId").asText();
      } else if (root.has("externalId") && !root.get("externalId").isNull()) {
        ext = root.get("externalId").asText();
      }
      return Optional.of(new RemoteInitiateResult(redirect, ext));
    } catch (RestClientException | java.io.IOException e) {
      return Optional.empty();
    }
  }

  private ProviderEndpoint endpointFor(PaymentProvider provider) {
    return switch (provider) {
      case WAVE -> props.wave();
      case MTN -> props.mtn();
      default -> null;
    };
  }
}
