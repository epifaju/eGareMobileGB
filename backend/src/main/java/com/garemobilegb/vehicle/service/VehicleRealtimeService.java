package com.garemobilegb.vehicle.service;

import com.garemobilegb.vehicle.dto.VehicleResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Diffusion temps réel des mises à jour véhicule (STOMP). En cluster, remplacer par Redis pub/sub.
 */
@Service
public class VehicleRealtimeService {

  private final SimpMessagingTemplate messagingTemplate;
  private final VehicleWaitCalibrationService calibrationService;

  public VehicleRealtimeService(
      SimpMessagingTemplate messagingTemplate, VehicleWaitCalibrationService calibrationService) {
    this.messagingTemplate = messagingTemplate;
    this.calibrationService = calibrationService;
  }

  /** Topic : /topic/stations/{stationId}/vehicles — payload JSON {@link VehicleResponse}. */
  public void broadcastUpdate(long stationId, VehicleResponse vehicle) {
    messagingTemplate.convertAndSend("/topic/stations/" + stationId + "/vehicles", vehicle);
    messagingTemplate.convertAndSend("/topic/map/vehicles", vehicle);
    calibrationService.recordObservation(vehicle, java.time.Instant.now());
  }
}
