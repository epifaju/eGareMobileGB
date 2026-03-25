package com.garemobilegb.booking.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentProvider;
import com.garemobilegb.booking.payment.config.MobileMoneyProviderProperties;
import com.garemobilegb.booking.payment.config.MobileMoneyProviderProperties.ProviderEndpoint;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Appels HTTP vers les API opérateurs (prod). Les URLs et schémas JSON sont configurables ; brancher
 * les vrais endpoints selon le contrat Orange / Wave / MTN du pays cible.
 */
@Component
public class MobileMoneyRemoteInitiator {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final MobileMoneyProviderProperties props;

  public MobileMoneyRemoteInitiator(
      RestClient restClient, ObjectMapper objectMapper, MobileMoneyProviderProperties props) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
    this.props = props;
  }

  public record RemoteInitiateResult(String redirectUrl, String externalReference) {}

  public Optional<RemoteInitiateResult> initiate(
      PaymentProvider provider, Booking booking, Payment payment) {
    ProviderEndpoint ep = endpointFor(provider);
    if (ep == null || ep.baseUrl() == null || ep.baseUrl().isBlank()) {
      return Optional.empty();
    }
    String path = ep.initiatePath() != null ? ep.initiatePath() : "";
    if (!path.isEmpty() && !path.startsWith("/")) {
      path = "/" + path;
    }
    String url = ep.baseUrl().replaceAll("/$", "") + path;
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
                    if (ep.apiKey() != null && !ep.apiKey().isBlank()) {
                      h.setBearerAuth(ep.apiKey());
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
          ep.responseRedirectField() != null && !ep.responseRedirectField().isBlank()
              ? ep.responseRedirectField()
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
      case ORANGE_MONEY -> props.orangeMoney();
      case WAVE -> props.wave();
      case MTN -> props.mtn();
      default -> null;
    };
  }
}
