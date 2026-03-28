package com.garemobilegb.booking.service;

import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.receipt.ReceiptPdfData;
import com.garemobilegb.booking.receipt.ReceiptPdfGenerator;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.shared.config.ReceiptProperties;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.shared.sms.SmsSender;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingReceiptService {

  private static final ZoneId DISPLAY_TZ = ZoneId.of("Africa/Bissau");
  private static final DateTimeFormatter DISPLAY_FMT =
      DateTimeFormatter.ofPattern("d MMM yyyy 'à' HH:mm", Locale.FRENCH).withZone(DISPLAY_TZ);

  private final BookingRepository bookingRepository;
  private final ReceiptPdfGenerator receiptPdfGenerator;
  private final ReceiptProperties receiptProperties;
  private final SmsSender smsSender;

  public BookingReceiptService(
      BookingRepository bookingRepository,
      ReceiptPdfGenerator receiptPdfGenerator,
      ReceiptProperties receiptProperties,
      SmsSender smsSender) {
    this.bookingRepository = bookingRepository;
    this.receiptPdfGenerator = receiptPdfGenerator;
    this.receiptProperties = receiptProperties;
    this.smsSender = smsSender;
  }

  @Transactional(readOnly = true)
  public byte[] buildReceiptPdf(long bookingId, long userId) {
    Booking booking = loadBooking(bookingId, userId);
    assertReceiptAllowed(booking);
    return receiptPdfGenerator.generate(toPdfData(booking, Instant.now()));
  }

  @Transactional(readOnly = true)
  public void sendReceiptSms(long bookingId, long userId) {
    Booking booking = loadBooking(bookingId, userId);
    assertReceiptAllowed(booking);
    String message = formatSms(booking);
    smsSender.sendTransactionalSms(booking.getUser().getPhoneNumber(), message);
  }

  private Booking loadBooking(long bookingId, long userId) {
    return bookingRepository
        .findByIdAndUser_Id(bookingId, userId)
        .orElseThrow(
            () ->
                new BusinessException(
                    HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Réservation introuvable"));
  }

  private static void assertReceiptAllowed(Booking booking) {
    Payment p = booking.getPayment();
    if (p == null) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "RECEIPT_NO_PAYMENT",
          "Aucun paiement associé à cette réservation.");
    }
    PaymentStatus s = p.getStatus();
    if (s != PaymentStatus.PAID
        && s != PaymentStatus.REFUNDED
        && s != PaymentStatus.REFUND_PENDING) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "RECEIPT_NOT_AVAILABLE",
          "Le reçu n’est disponible qu’après un paiement confirmé (ou en lien avec un remboursement).");
    }
  }

  private ReceiptPdfData toPdfData(Booking booking, Instant issuedAt) {
    var p = booking.getPayment();
    var v = booking.getVehicle();
    var station = v.getStation();
    return new ReceiptPdfData(
        booking.getId(),
        station.getName(),
        v.getRouteLabel(),
        v.getRegistrationCode(),
        booking.getSeatNumber(),
        p.getAmount(),
        p.getCurrency(),
        p.getProvider().name(),
        p.getStatus().name(),
        booking.getStatus().name(),
        p.getProviderRef(),
        DISPLAY_FMT.format(issuedAt),
        DISPLAY_FMT.format(booking.getCreatedAt()));
  }

  private String formatSms(Booking booking) {
    var p = booking.getPayment();
    var v = booking.getVehicle();
    var station = v.getStation();
    String template = receiptProperties.smsTemplateOrDefault();
    String amount =
        p.getAmount() != null ? p.getAmount().toPlainString() + " " + p.getCurrency() : "—";
    return template
        .replace("{bookingId}", String.valueOf(booking.getId()))
        .replace("{route}", v.getRouteLabel() != null ? v.getRouteLabel() : "—")
        .replace("{station}", station.getName() != null ? station.getName() : "—")
        .replace("{amount}", amount)
        .replace("{currency}", p.getCurrency() != null ? p.getCurrency() : "XOF");
  }
}
