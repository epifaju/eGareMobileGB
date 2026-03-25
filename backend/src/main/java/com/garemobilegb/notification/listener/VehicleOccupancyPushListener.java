package com.garemobilegb.notification.listener;

import com.garemobilegb.notification.event.VehicleOccupancyChangedEvent;
import com.garemobilegb.notification.service.VehicleCapacityAlertService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class VehicleOccupancyPushListener {

  private final VehicleCapacityAlertService vehicleCapacityAlertService;

  public VehicleOccupancyPushListener(VehicleCapacityAlertService vehicleCapacityAlertService) {
    this.vehicleCapacityAlertService = vehicleCapacityAlertService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOccupancyChanged(VehicleOccupancyChangedEvent event) {
    vehicleCapacityAlertService.onOccupancyChanged(event);
  }
}
