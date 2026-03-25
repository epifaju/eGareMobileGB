package com.garemobilegb.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.garemobilegb.notification.event.VehicleOccupancyChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleCapacityAlertServiceTest {

  @Mock ExpoPushNotificationService expoPushNotificationService;

  @InjectMocks VehicleCapacityAlertService vehicleCapacityAlertService;

  @Test
  void notifiesWhenCrossing80And90InOneStep() {
    var event = new VehicleOccupancyChangedEvent(1L, 10L, "AB-01", 5, 9, 10);
    vehicleCapacityAlertService.onOccupancyChanged(event);
    verify(expoPushNotificationService).sendDriverCapacityAlert(eq(event), eq(80));
    verify(expoPushNotificationService).sendDriverCapacityAlert(eq(event), eq(90));
    verify(expoPushNotificationService, never()).sendDriverCapacityAlert(eq(event), eq(100));
  }

  @Test
  void noNotifyWhenCapacityZero() {
    var event = new VehicleOccupancyChangedEvent(1L, 10L, "AB-01", 0, 0, 0);
    vehicleCapacityAlertService.onOccupancyChanged(event);
    verify(expoPushNotificationService, never()).sendDriverCapacityAlert(any(), anyInt());
  }
}
