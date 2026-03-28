package com.garemobilegb.booking.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.payment.config.OrangeMoneyEndpoint;
import com.garemobilegb.booking.payment.config.MobileMoneyProviderProperties;
import com.garemobilegb.shared.config.PaymentGatewayProperties;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Orange Money Web Payment / M Payment : OAuth 2 client credentials puis création de session.
 *
 * <p>Contrat aligné sur la documentation Orange (champs {@code merchant_key}, {@code order_id},
 * {@code payment_url}, {@code pay_token}). Les URLs exactes dépendent du pays — à renseigner depuis
 * le portail Orange Partner.
 */
@Component
public class OrangeMoneyWebPaymentClient {

  private static final Logger log = LoggerFactory.getLogger(OrangeMoneyWebPaymentClient.class);

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final MobileMoneyProviderProperties props;
  private final PaymentGatewayProperties gatewayProperties;

  public OrangeMoneyWebPaymentClient(
      RestClient restClient,
      ObjectMapper objectMapper,
      MobileMoneyProviderProperties props,
      PaymentGatewayProperties gatewayProperties) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
    this.props = props;
    this.gatewayProperties = gatewayProperties;
  }

  public Optional<RemoteInitiateResult> initiate(Booking booking, Payment payment) {
    OrangeMoneyEndpoint om = props.orangeMoney();
    if (om == null || !om.webPaymentReady()) {
      return Optional.empty();
    }
    String publicBase = gatewayProperties.publicBaseUrl().replaceAll("/$", "");
    String orderId = "GMB-" + booking.getId();
    String notifUrl =
        StringUtils.hasText(om.notifUrl())
            ? om.notifUrl()
            : publicBase + "/api/webhooks/payments/orange-money";
    String returnUrl =
        StringUtils.hasText(om.returnUrl())
            ? om.returnUrl().replace("{bookingId}", String.valueOf(booking.getId()))
            : publicBase + "/api/payments/orange/return?bookingId=" + booking.getId();
    String cancelUrl =
        StringUtils.hasText(om.cancelUrl())
            ? om.cancelUrl().replace("{bookingId}", String.valueOf(booking.getId()))
            : publicBase + "/api/payments/orange/cancel?bookingId=" + booking.getId();

    try {
      String accessToken = fetchAccessToken(om);
      ObjectNode body = objectMapper.createObjectNode();
      body.put("merchant_key", om.merchantKey());
      body.put("currency", om.currencyOrDefault());
      body.put("order_id", orderId);
      body.put(
          "amount",
          payment.getAmount().setScale(0, RoundingMode.HALF_UP).longValue());
      body.put("return_url", returnUrl);
      body.put("cancel_url", cancelUrl);
      body.put("notif_url", notifUrl);
      body.put("lang", om.langOrDefault());
      body.put("reference", orderId);

      String raw =
          restClient
              .post()
              .uri(URI.create(om.webPaymentUrl().trim()))
              .contentType(MediaType.APPLICATION_JSON)
              .headers(h -> h.setBearerAuth(accessToken))
              .body(body.toString())
              .retrieve()
              .body(String.class);

      if (raw == null || raw.isBlank()) {
        return Optional.empty();
      }
      JsonNode root = objectMapper.readTree(raw);
      String payUrlField = om.paymentUrlFieldOrDefault();
      String tokenField = om.payTokenFieldOrDefault();
      if (!root.has(payUrlField) || root.get(payUrlField).isNull()) {
        log.warn("Orange Money Web Payment : champ {} absent dans la réponse", payUrlField);
        return Optional.empty();
      }
      String paymentUrl = root.get(payUrlField).asText();
      String payToken = root.has(tokenField) && !root.get(tokenField).isNull()
          ? root.get(tokenField).asText()
          : null;
      return Optional.of(new RemoteInitiateResult(paymentUrl, payToken));
    } catch (RestClientException | IOException e) {
      log.error("Orange Money Web Payment échoué : {}", e.getMessage());
      return Optional.empty();
    }
  }

  private String fetchAccessToken(OrangeMoneyEndpoint om) throws IOException {
    String auth =
        Base64.getEncoder()
            .encodeToString(
                (om.clientId().trim() + ":" + om.clientSecret().trim())
                    .getBytes(StandardCharsets.UTF_8));
    String raw =
        restClient
            .post()
            .uri(URI.create(om.oauthTokenUrl().trim()))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("Authorization", "Basic " + auth)
            .body("grant_type=client_credentials")
            .retrieve()
            .body(String.class);
    if (raw == null || raw.isBlank()) {
      throw new IOException("Token Orange vide");
    }
    JsonNode root = objectMapper.readTree(raw);
    if (!root.has("access_token") || root.get("access_token").isNull()) {
      throw new IOException("Réponse token sans access_token");
    }
    return root.get("access_token").asText();
  }
}
