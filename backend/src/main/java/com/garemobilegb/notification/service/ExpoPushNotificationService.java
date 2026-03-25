package com.garemobilegb.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.notification.event.VehicleOccupancyChangedEvent;
import com.garemobilegb.notification.repository.UserPushTokenRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Envoie des notifications via l’API Expo Push (jetons {@code ExponentPushToken[...]}).
 *
 * <p>Doc : https://docs.expo.dev/push-notifications/sending-notifications/
 */
@Service
public class ExpoPushNotificationService {

  private static final Logger log = LoggerFactory.getLogger(ExpoPushNotificationService.class);
  private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

  private final UserPushTokenRepository userPushTokenRepository;
  private final ObjectMapper objectMapper;

  public ExpoPushNotificationService(
      UserPushTokenRepository userPushTokenRepository, ObjectMapper objectMapper) {
    this.userPushTokenRepository = userPushTokenRepository;
    this.objectMapper = objectMapper;
  }

  public void sendDriverCapacityAlert(VehicleOccupancyChangedEvent event, int thresholdPercent) {
    List<String> tokens =
        userPushTokenRepository.findByUser_Role(Role.DRIVER).stream()
            .map(t -> t.getExpoPushToken())
            .distinct()
            .toList();
    if (tokens.isEmpty()) {
      return;
    }
    String title = "Remplissage " + thresholdPercent + " %";
    String body =
        String.format(
            "%s : %d/%d places occupées.",
            event.registrationCode(), event.currentOccupiedSeats(), event.capacity());
    RestClient client = RestClient.create();
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
          data.put("type", "CAPACITY_THRESHOLD");
          data.put("vehicleId", event.vehicleId());
          data.put("threshold", thresholdPercent);
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
      } catch (RestClientException ex) {
        log.warn("Expo push failed: {}", ex.getMessage());
      }
    }
  }

  private static List<List<String>> chunks(List<String> tokens, int size) {
    List<List<String>> out = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i += size) {
      out.add(tokens.subList(i, Math.min(tokens.size(), i + size)));
    }
    return out;
  }
}
