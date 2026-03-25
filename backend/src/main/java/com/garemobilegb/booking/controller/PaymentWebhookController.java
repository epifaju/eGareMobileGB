package com.garemobilegb.booking.controller;

import com.garemobilegb.booking.service.PaymentWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/payments")
public class PaymentWebhookController {

  private final PaymentWebhookService paymentWebhookService;

  public PaymentWebhookController(PaymentWebhookService paymentWebhookService) {
    this.paymentWebhookService = paymentWebhookService;
  }

  /**
   * Webhooks passerelles (sandbox : secret simple ; prod : HMAC du corps).
   *
   * @param provider segment : orange-money, wave, mtn, sandbox
   */
  @PostMapping("/{provider}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void receive(
      @PathVariable String provider,
      @RequestBody String rawBody,
      @RequestHeader(value = "X-Sandbox-Secret", required = false) String sandboxSecret,
      @RequestHeader(value = "X-Signature", required = false) String signature) {
    paymentWebhookService.handleWebhook(provider, rawBody, sandboxSecret, signature);
  }
}
