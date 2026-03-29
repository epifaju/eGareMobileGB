package com.garemobilegb.notification.service;

import com.garemobilegb.auth.domain.User;
import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.shared.config.NotificationProperties;
import com.garemobilegb.shared.sms.SmsSender;
import com.garemobilegb.vehicle.domain.Vehicle;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rappels passagers (PRD §6.3) : ~80 % du délai confirmation→départ, puis fenêtre « imminent » avant
 * {@code departureScheduledAt}. Push Expo + repli SMS configurable.
 */
@Service
public class PassengerDepartureReminderService {

  private static final Logger log = LoggerFactory.getLogger(PassengerDepartureReminderService.class);
  private static final DateTimeFormatter DEPARTURE_FMT =
      DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.FRENCH).withZone(ZoneOffset.UTC);

  private final BookingRepository bookingRepository;
  private final ExpoPushNotificationService expoPushNotificationService;
  private final SmsSender smsSender;
  private final NotificationProperties notificationProperties;

  public PassengerDepartureReminderService(
      BookingRepository bookingRepository,
      ExpoPushNotificationService expoPushNotificationService,
      SmsSender smsSender,
      NotificationProperties notificationProperties) {
    this.bookingRepository = bookingRepository;
    this.expoPushNotificationService = expoPushNotificationService;
    this.smsSender = smsSender;
    this.notificationProperties = notificationProperties;
  }

  @Transactional
  public void runScan(Instant now) {
    if (!notificationProperties.departureRemindersEnabledOrDefault()) {
      return;
    }
    Instant from = now.minus(48, java.time.temporal.ChronoUnit.HOURS);
    Instant to = now.plus(72, java.time.temporal.ChronoUnit.HOURS);
    List<Booking> bookings =
        bookingRepository.findConfirmedWithUpcomingDepartureForReminders(
            BookingStatus.CONFIRMED, from, to);
    for (Booking b : bookings) {
      try {
        processBooking(b, now);
      } catch (Exception e) {
        log.warn("Rappel départ ignoré pour réservation {} : {}", b.getId(), e.getMessage());
      }
    }
  }

  private void processBooking(Booking booking, Instant now) {
    Vehicle v = booking.getVehicle();
    Instant dep = v.getDepartureScheduledAt();
    if (dep == null || !now.isBefore(dep)) {
      return;
    }

    Instant t0 = booking.getConfirmedAt() != null ? booking.getConfirmedAt() : booking.getCreatedAt();
    int imminentMin = notificationProperties.departureImminentMinutesOrDefault();
    Instant imminentStart = dep.minus(Duration.ofMinutes(imminentMin));

    if (booking.getDepartureReminderPct80SentAt() == null) {
      Duration total = Duration.between(t0, dep);
      if (!total.isNegative() && !total.isZero()) {
        Instant triggerPct80 = t0.plusMillis((long) (total.toMillis() * 0.8));
        if (!now.isBefore(triggerPct80) && now.isBefore(dep)) {
          sendPct80(booking, v, now);
        }
      }
    }

    if (booking.getDepartureReminderImminentSentAt() == null) {
      if (!now.isBefore(imminentStart) && now.isBefore(dep)) {
        sendImminent(booking, v, now);
      }
    }
  }

  private void sendPct80(Booking booking, Vehicle vehicle, Instant now) {
    int claimed = bookingRepository.claimDepartureReminderPct80(booking.getId(), now);
    if (claimed != 1) {
      return;
    }
    User user = booking.getUser();
    String route = vehicle.getRouteLabel();
    String reg = vehicle.getRegistrationCode();
    String depStr = DEPARTURE_FMT.format(vehicle.getDepartureScheduledAt());

    String title =
        replaceBookingPlaceholders(
            notificationProperties.departurePushPct80TitleOrDefault(), booking, vehicle, depStr);
    String body =
        replaceBookingPlaceholders(
            notificationProperties.departurePushPct80BodyOrDefault(), booking, vehicle, depStr);

    boolean pushOk =
        expoPushNotificationService.sendDataPushToUser(
            user.getId(),
            title,
            body,
            data -> {
              data.put("type", "BOOKING_DEPARTURE_PCT80");
              data.put("bookingId", String.valueOf(booking.getId()));
              data.put("vehicleId", String.valueOf(vehicle.getId()));
            });

    boolean smsOk = false;
    if (!pushOk && notificationProperties.departureSmsFallbackEnabledOrDefault()) {
      String sms =
          replaceBookingPlaceholders(
              notificationProperties.departureSmsPct80TemplateOrDefault(),
              booking,
              vehicle,
              depStr);
      smsSender.sendTransactionalSms(user.getPhoneNumber(), sms);
      smsOk = true;
    }

    if (!pushOk && !smsOk) {
      bookingRepository.clearDepartureReminderPct80(booking.getId());
    }
  }

  private void sendImminent(Booking booking, Vehicle vehicle, Instant now) {
    int claimed = bookingRepository.claimDepartureReminderImminent(booking.getId(), now);
    if (claimed != 1) {
      return;
    }
    User user = booking.getUser();
    String depStr = DEPARTURE_FMT.format(vehicle.getDepartureScheduledAt());

    String title =
        replaceBookingPlaceholders(
            notificationProperties.departurePushImminentTitleOrDefault(), booking, vehicle, depStr);
    String body =
        replaceBookingPlaceholders(
            notificationProperties.departurePushImminentBodyOrDefault(), booking, vehicle, depStr);

    boolean pushOk =
        expoPushNotificationService.sendDataPushToUser(
            user.getId(),
            title,
            body,
            data -> {
              data.put("type", "BOOKING_DEPARTURE_IMMINENT");
              data.put("bookingId", String.valueOf(booking.getId()));
              data.put("vehicleId", String.valueOf(vehicle.getId()));
            });

    boolean smsOk = false;
    if (!pushOk && notificationProperties.departureSmsFallbackEnabledOrDefault()) {
      String sms =
          replaceBookingPlaceholders(
              notificationProperties.departureSmsImminentTemplateOrDefault(),
              booking,
              vehicle,
              depStr);
      smsSender.sendTransactionalSms(user.getPhoneNumber(), sms);
      smsOk = true;
    }

    if (!pushOk && !smsOk) {
      bookingRepository.clearDepartureReminderImminent(booking.getId());
    }
  }

  private static String replaceBookingPlaceholders(
      String template, Booking booking, Vehicle vehicle, String departureFormatted) {
    return template
        .replace("{bookingId}", String.valueOf(booking.getId()))
        .replace("{route}", vehicle.getRouteLabel())
        .replace("{registrationCode}", vehicle.getRegistrationCode())
        .replace("{departure}", departureFormatted);
  }
}
