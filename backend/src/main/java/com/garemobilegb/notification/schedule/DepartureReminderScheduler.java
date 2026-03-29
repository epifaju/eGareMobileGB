package com.garemobilegb.notification.schedule;

import com.garemobilegb.notification.service.PassengerDepartureReminderService;
import com.garemobilegb.shared.config.NotificationProperties;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DepartureReminderScheduler {

  private final PassengerDepartureReminderService passengerDepartureReminderService;
  private final NotificationProperties notificationProperties;

  public DepartureReminderScheduler(
      PassengerDepartureReminderService passengerDepartureReminderService,
      NotificationProperties notificationProperties) {
    this.passengerDepartureReminderService = passengerDepartureReminderService;
    this.notificationProperties = notificationProperties;
  }

  @Scheduled(fixedDelayString = "${app.notification.departure-reminder-scan-ms:60000}")
  public void scanDepartureReminders() {
    if (!notificationProperties.departureRemindersEnabledOrDefault()) {
      return;
    }
    passengerDepartureReminderService.runScan(Instant.now());
  }
}
