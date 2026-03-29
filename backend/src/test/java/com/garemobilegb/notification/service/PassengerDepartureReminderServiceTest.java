package com.garemobilegb.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.garemobilegb.auth.domain.User;
import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.shared.config.NotificationProperties;
import com.garemobilegb.shared.sms.SmsSender;
import com.garemobilegb.vehicle.domain.Vehicle;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PassengerDepartureReminderServiceTest {

  @Mock BookingRepository bookingRepository;
  @Mock ExpoPushNotificationService expoPushNotificationService;
  @Mock SmsSender smsSender;
  @Mock NotificationProperties notificationProperties;

  @InjectMocks PassengerDepartureReminderService passengerDepartureReminderService;

  @Test
  void runScan_whenRemindersDisabled_doesNothing() {
    when(notificationProperties.departureRemindersEnabledOrDefault()).thenReturn(false);
    passengerDepartureReminderService.runScan(Instant.now());
    verifyNoInteractions(bookingRepository);
  }

  @Test
  void runScan_pct80Window_sendsSmsWhenPushFails() {
    when(notificationProperties.departureRemindersEnabledOrDefault()).thenReturn(true);
    when(notificationProperties.departurePushPct80TitleOrDefault()).thenReturn("Titre");
    when(notificationProperties.departurePushPct80BodyOrDefault()).thenReturn("Corps {route}");
    when(notificationProperties.departureSmsFallbackEnabledOrDefault()).thenReturn(true);
    when(notificationProperties.departureSmsPct80TemplateOrDefault())
        .thenReturn("SMS {bookingId} {route} {registrationCode} {departure}");

    Instant t0 = Instant.parse("2026-03-01T10:00:00Z");
    Instant dep = Instant.parse("2026-03-01T12:00:00Z");
    Instant now = Instant.parse("2026-03-01T11:37:00Z");

    Booking booking = org.mockito.Mockito.mock(Booking.class);
    Vehicle vehicle = org.mockito.Mockito.mock(Vehicle.class);
    User user = org.mockito.Mockito.mock(User.class);
    when(booking.getVehicle()).thenReturn(vehicle);
    when(booking.getUser()).thenReturn(user);
    when(booking.getId()).thenReturn(42L);
    when(booking.getConfirmedAt()).thenReturn(t0);
    when(booking.getDepartureReminderPct80SentAt()).thenReturn(null);
    when(booking.getDepartureReminderImminentSentAt()).thenReturn(null);
    when(vehicle.getDepartureScheduledAt()).thenReturn(dep);
    when(vehicle.getRouteLabel()).thenReturn("Bissau → Gabú");
    when(vehicle.getRegistrationCode()).thenReturn("GW-0100");
    when(user.getId()).thenReturn(9L);
    when(user.getPhoneNumber()).thenReturn("+24550000000");

    when(bookingRepository.findConfirmedWithUpcomingDepartureForReminders(
            eq(BookingStatus.CONFIRMED), any(Instant.class), any(Instant.class)))
        .thenReturn(List.of(booking));
    when(bookingRepository.claimDepartureReminderPct80(eq(42L), any(Instant.class))).thenReturn(1);
    when(expoPushNotificationService.sendDataPushToUser(
            anyLong(), anyString(), anyString(), any())).thenReturn(false);

    passengerDepartureReminderService.runScan(now);

    verify(smsSender).sendTransactionalSms(eq("+24550000000"), contains("42"));
    verify(smsSender).sendTransactionalSms(eq("+24550000000"), contains("Bissau"));
    verify(bookingRepository, never()).clearDepartureReminderPct80(42L);
  }

  @Test
  void runScan_pct80WhenPushSucceeds_noSms() {
    when(notificationProperties.departureRemindersEnabledOrDefault()).thenReturn(true);
    when(notificationProperties.departurePushPct80TitleOrDefault()).thenReturn("T");
    when(notificationProperties.departurePushPct80BodyOrDefault()).thenReturn("B");

    Instant t0 = Instant.parse("2026-03-01T10:00:00Z");
    Instant dep = Instant.parse("2026-03-01T12:00:00Z");
    Instant now = Instant.parse("2026-03-01T11:37:00Z");

    Booking booking = org.mockito.Mockito.mock(Booking.class);
    Vehicle vehicle = org.mockito.Mockito.mock(Vehicle.class);
    User user = org.mockito.Mockito.mock(User.class);
    when(booking.getVehicle()).thenReturn(vehicle);
    when(booking.getUser()).thenReturn(user);
    when(booking.getId()).thenReturn(7L);
    when(booking.getConfirmedAt()).thenReturn(t0);
    when(booking.getDepartureReminderPct80SentAt()).thenReturn(null);
    when(booking.getDepartureReminderImminentSentAt()).thenReturn(null);
    when(vehicle.getDepartureScheduledAt()).thenReturn(dep);
    when(vehicle.getRouteLabel()).thenReturn("R");
    when(vehicle.getRegistrationCode()).thenReturn("X");
    when(user.getId()).thenReturn(1L);

    when(bookingRepository.findConfirmedWithUpcomingDepartureForReminders(
            eq(BookingStatus.CONFIRMED), any(Instant.class), any(Instant.class)))
        .thenReturn(List.of(booking));
    when(bookingRepository.claimDepartureReminderPct80(eq(7L), any(Instant.class))).thenReturn(1);
    when(expoPushNotificationService.sendDataPushToUser(
            anyLong(), anyString(), anyString(), any())).thenReturn(true);

    passengerDepartureReminderService.runScan(now);

    verifyNoInteractions(smsSender);
  }
}
