package com.garemobilegb.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.auth.domain.User;
import com.garemobilegb.auth.repository.UserRepository;
import com.garemobilegb.notification.domain.UserPushToken;
import com.garemobilegb.notification.event.VehicleOccupancyChangedEvent;
import com.garemobilegb.notification.repository.UserPushTokenRepository;
import com.garemobilegb.shared.config.NotificationProperties;
import com.garemobilegb.shared.sms.SmsSender;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Envoie des notifications via l’API Expo Push (jetons {@code ExponentPushToken[...]}). Phase 6 : analyse
 * des réponses Expo et repli SMS pour les conducteurs si aucune livraison push n’a réussi.
 *
 * <p>Doc : https://docs.expo.dev/push-notifications/sending-notifications/
 */
@Service
public class ExpoPushNotificationService {

  private static final Logger log = LoggerFactory.getLogger(ExpoPushNotificationService.class);
  private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

  private final UserPushTokenRepository userPushTokenRepository;
  private final UserRepository userRepository;
  private final SmsSender smsSender;
  private final NotificationProperties notificationProperties;
  private final ObjectMapper objectMapper;

  public ExpoPushNotificationService(
      UserPushTokenRepository userPushTokenRepository,
      UserRepository userRepository,
      SmsSender smsSender,
      NotificationProperties notificationProperties,
      ObjectMapper objectMapper) {
    this.userPushTokenRepository = userPushTokenRepository;
    this.userRepository = userRepository;
    this.smsSender = smsSender;
    this.notificationProperties = notificationProperties;
    this.objectMapper = objectMapper;
  }

  public void sendDriverCapacityAlert(VehicleOccupancyChangedEvent event, int thresholdPercent) {
    List<String> tokens =
        userPushTokenRepository.findByUser_Role(Role.DRIVER).stream()
            .map(UserPushToken::getExpoPushToken)
            .distinct()
            .toList();

    String title = "Remplissage " + thresholdPercent + " %";
    String body =
        String.format(
            "%s : %d/%d places occupées.",
            event.registrationCode(), event.currentOccupiedSeats(), event.capacity());

    boolean anyPushOk =
        sendDataPush(
            tokens,
            title,
            body,
            data -> {
              data.put("type", "CAPACITY_THRESHOLD");
              data.put("vehicleId", String.valueOf(event.vehicleId()));
              data.put("threshold", String.valueOf(thresholdPercent));
            });

    if (anyPushOk) {
      return;
    }
    if (!notificationProperties.capacitySmsFallbackEnabledOrDefault()) {
      return;
    }
    sendCapacitySmsFallbackToDrivers(event, thresholdPercent);
  }

  private void sendCapacitySmsFallbackToDrivers(
      VehicleOccupancyChangedEvent event, int thresholdPercent) {
    String template = notificationProperties.capacitySmsTemplateOrDefault();
    String msg =
        template
            .replace("{registrationCode}", event.registrationCode())
            .replace("{route}", event.routeLabel() != null ? event.routeLabel() : "")
            .replace("{threshold}", String.valueOf(thresholdPercent))
            .replace("{occupied}", String.valueOf(event.currentOccupiedSeats()))
            .replace("{capacity}", String.valueOf(event.capacity()))
            .replace("{vehicleId}", String.valueOf(event.vehicleId()));

    Set<String> phones = new LinkedHashSet<>();
    for (User u : userRepository.findByRole(Role.DRIVER)) {
      phones.add(u.getPhoneNumber());
    }
    for (String phone : phones) {
      smsSender.sendTransactionalSms(phone, msg);
    }
  }

  /**
   * @return {@code true} si au moins un message Expo a le statut {@code ok} sur l’ensemble des lots, ou
   *     s’il n’y avait aucun jeton (laisser l’appelant décider du SMS).
   */
  public boolean sendDataPushToUser(
      long userId, String title, String body, Consumer<ObjectNode> dataCustomizer) {
    List<String> tokens =
        userPushTokenRepository.findByUser_Id(userId).stream()
            .map(UserPushToken::getExpoPushToken)
            .distinct()
            .toList();
    return sendDataPush(tokens, title, body, dataCustomizer);
  }

  /**
   * @return {@code true} si au moins une livraison Expo {@code ok} ; {@code false} si aucun jeton ou
   *     toutes les tentatives ont échoué / réponse invalide.
   */
  public boolean sendDataPush(
      List<String> tokens,
      String title,
      String body,
      Consumer<ObjectNode> dataCustomizer) {
    if (tokens.isEmpty()) {
      return false;
    }
    RestClient client = RestClient.create();
    boolean anyOk = false;
    for (List<String> chunk : chunks(tokens, 90)) {
      try {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode messages = root.putArray("messages");
        for (String to : chunk) {
          ObjectNode m = messages.addObject();
          m.put("to", to);
          m.put("title", title);
          m.put("body", body);
          m.put("sound", "default");
          ObjectNode data = m.putObject("data");
          dataCustomizer.accept(data);
        }
        String response =
            client
                .post()
                .uri(EXPO_PUSH_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(root.toString())
                .retrieve()
                .body(String.class);
        log.debug("Expo push response: {}", response);
        if (responseIndicatesAnySuccess(response)) {
          anyOk = true;
        }
      } catch (RestClientException ex) {
        log.warn("Expo push failed: {}", ex.getMessage());
      }
    }
    return anyOk;
  }

  private boolean responseIndicatesAnySuccess(String responseBody) {
    if (responseBody == null || responseBody.isBlank()) {
      return false;
    }
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode data = root.path("data");
      if (!data.isArray()) {
        return false;
      }
      for (JsonNode item : data) {
        if ("ok".equalsIgnoreCase(item.path("status").asText())) {
          return true;
        }
      }
    } catch (Exception e) {
      log.debug("Expo push response parse error: {}", e.getMessage());
    }
    return false;
  }

  private static List<List<String>> chunks(List<String> tokens, int size) {
    List<List<String>> out = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i += size) {
      out.add(tokens.subList(i, Math.min(tokens.size(), i + size)));
    }
    return out;
  }
}
