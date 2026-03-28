package com.garemobilegb.booking.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pages minimales après paiement Orange Money Web Payment ({@code return_url} / {@code cancel_url}).
 */
@RestController
public class OrangeMoneyReturnController {

  @GetMapping(value = "/api/payments/orange/return", produces = MediaType.TEXT_HTML_VALUE)
  public String returnPage(@RequestParam(required = false) Long bookingId) {
    String id = bookingId != null ? "#" + bookingId : "";
    return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>"
        + "<title>Paiement</title></head><body style=\"font-family:system-ui;padding:2rem\">"
        + "<p>Paiement Orange Money terminé. Réservation "
        + id
        + " — vous pouvez fermer cette page et retourner à l’application.</p>"
        + "</body></html>";
  }

  @GetMapping(value = "/api/payments/orange/cancel", produces = MediaType.TEXT_HTML_VALUE)
  public String cancelPage(@RequestParam(required = false) Long bookingId) {
    String id = bookingId != null ? "#" + bookingId : "";
    return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>"
        + "<title>Paiement annulé</title></head><body style=\"font-family:system-ui;padding:2rem\">"
        + "<p>Paiement annulé. Réservation "
        + id
        + ".</p>"
        + "</body></html>";
  }
}
