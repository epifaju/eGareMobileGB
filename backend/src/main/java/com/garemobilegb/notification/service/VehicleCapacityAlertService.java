package com.garemobilegb.notification.service;

import com.garemobilegb.notification.event.VehicleOccupancyChangedEvent;
import java.util.List;
import org.springframework.stereotype.Service;

/** Paliers 80 / 90 / 100 % de remplissage (PRD §4.3 D3). */
@Service
public class VehicleCapacityAlertService {

  private final ExpoPushNotificationService expoPushNotificationService;

  public VehicleCapacityAlertService(ExpoPushNotificationService expoPushNotificationService) {
    this.expoPushNotificationService = expoPushNotificationService;
  }

  public void onOccupancyChanged(VehicleOccupancyChangedEvent event) {
    if (event.capacity() <= 0) {
      return;
    }
    int oldPct = (event.previousOccupiedSeats() * 100) / event.capacity();
    int newPct = (event.currentOccupiedSeats() * 100) / event.capacity();
    for (int threshold : List.of(80, 90, 100)) {
      if (newPct >= threshold && oldPct < threshold) {
        expoPushNotificationService.sendDriverCapacityAlert(event, threshold);
      }
    }
  }
}
