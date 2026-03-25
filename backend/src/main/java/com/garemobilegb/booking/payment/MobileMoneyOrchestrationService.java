package com.garemobilegb.booking.payment;

import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentProvider;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.dto.PaymentInitiateRequest;
import com.garemobilegb.booking.dto.PaymentInitiateResponse;
import com.garemobilegb.booking.payment.MobileMoneyRemoteInitiator.RemoteInitiateResult;
import com.garemobilegb.booking.payment.config.MobileMoneyProviderProperties;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.booking.repository.PaymentRepository;
import com.garemobilegb.shared.config.PaymentGatewayProperties;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.shared.security.JwtService;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Phase 3 — moteur d’initiation : idempotence, sandbox, appels HTTP prod, chaîne de secours
 * Orange → Wave → MTN, métriques.
 */
@Service
public class MobileMoneyOrchestrationService {

  private final BookingRepository bookingRepository;
  private final PaymentRepository paymentRepository;
  private final JwtService jwtService;
  private final PaymentGatewayProperties paymentGatewayProperties;
  private final MobileMoneyProviderProperties mobileMoneyProperties;
  private final MobileMoneyRemoteInitiator remoteInitiator;
  private final MeterRegistry meterRegistry;

  public MobileMoneyOrchestrationService(
      BookingRepository bookingRepository,
      PaymentRepository paymentRepository,
      JwtService jwtService,
      PaymentGatewayProperties paymentGatewayProperties,
      MobileMoneyProviderProperties mobileMoneyProperties,
      MobileMoneyRemoteInitiator remoteInitiator,
      MeterRegistry meterRegistry) {
    this.bookingRepository = bookingRepository;
    this.paymentRepository = paymentRepository;
    this.jwtService = jwtService;
    this.paymentGatewayProperties = paymentGatewayProperties;
    this.mobileMoneyProperties = mobileMoneyProperties;
    this.remoteInitiator = remoteInitiator;
    this.meterRegistry = meterRegistry;
  }

  @Transactional
  public PaymentInitiateResponse initiatePayment(
      long bookingId, long userId, PaymentInitiateRequest request, String idempotencyKeyHeader) {
    if (request == null || request.provider() == null) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST, "PAYMENT_PROVIDER_REQUIRED", "Fournisseur de paiement requis.");
    }
    if (request.provider() == PaymentProvider.INTERNAL) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "PAYMENT_PROVIDER_INVALID",
          "Choisissez Orange Money, Wave ou MTN pour ce flux.");
    }
    String effectiveKey = firstNonBlank(idempotencyKeyHeader, request.idempotencyKey());

    Booking booking =
        bookingRepository
            .findByIdAndUser_Id(bookingId, userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Réservation introuvable"));
    if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "BOOKING_NOT_PENDING_PAYMENT",
          "Cette réservation n’est pas en attente de paiement.");
    }
    Payment payment = booking.getPayment();
    if (payment == null || payment.getStatus() != PaymentStatus.PENDING) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "PAYMENT_NOT_PENDING", "Paiement déjà traité ou absent.");
    }

    if (StringUtils.hasText(effectiveKey)) {
      Optional<Payment> existing = paymentRepository.findByIdempotencyKey(effectiveKey.trim());
      if (existing.isPresent()) {
        Payment p = existing.get();
        if (!p.getBooking().getId().equals(bookingId)
            || !p.getBooking().getUser().getId().equals(userId)) {
          throw new BusinessException(
              HttpStatus.CONFLICT,
              "IDEMPOTENCY_KEY_CONFLICT",
              "Cette clé d’idempotence est déjà utilisée pour une autre réservation.");
        }
        if (p.getStatus() == PaymentStatus.PENDING
            && StringUtils.hasText(p.getCheckoutUrlCache())
            && StringUtils.hasText(p.getPaymentTokenCache())) {
          meterRegistry.counter("gare.payments.initiate.idempotent_replay").increment();
          return new PaymentInitiateResponse(
              p.getCheckoutUrlCache(),
              p.getPaymentTokenCache(),
              p.getAmount(),
              p.getCurrency(),
              p.getProvider());
        }
      }
    }

    boolean tryFallback = Boolean.TRUE.equals(request.tryFallback());
    List<PaymentProvider> toTry = providersToTry(request.provider(), tryFallback);

    if (paymentGatewayProperties.sandbox()) {
      PaymentProvider chosen = request.provider();
      payment.setProvider(chosen);
      bookingRepository.save(booking);
      PaymentInitiateResponse res = buildSandboxResponse(bookingId, userId, booking, payment, chosen);
      cacheIdempotent(payment, effectiveKey, res);
      meterRegistry
          .counter("gare.payments.initiated", "provider", chosen.name(), "mode", "sandbox")
          .increment();
      return res;
    }

    for (PaymentProvider provider : toTry) {
      payment.setProvider(provider);
      bookingRepository.save(booking);
      Optional<RemoteInitiateResult> remote = remoteInitiator.initiate(provider, booking, payment);
      if (remote.isEmpty()) {
        meterRegistry
            .counter("gare.payments.provider.remote_unconfigured", "provider", provider.name())
            .increment();
        continue;
      }
      RemoteInitiateResult r = remote.get();
      if (StringUtils.hasText(r.externalReference())) {
        payment.setProviderRef(r.externalReference());
      }
      String token = jwtService.createPaymentCheckoutToken(bookingId, userId, provider.name());
      PaymentInitiateResponse res =
          new PaymentInitiateResponse(
              r.redirectUrl(),
              token,
              payment.getAmount(),
              payment.getCurrency(),
              provider);
      cacheIdempotent(payment, effectiveKey, res);
      meterRegistry
          .counter("gare.payments.initiated", "provider", provider.name(), "mode", "production")
          .increment();
      return res;
    }
    throw new BusinessException(
        HttpStatus.SERVICE_UNAVAILABLE,
        "PAYMENT_GATEWAY_UNAVAILABLE",
        "Aucune passerelle Mobile Money n’a répondu. Vérifiez app.payment.mobile-money.* (URLs API) "
            + "ou désactivez le mode production pour le sandbox.");
  }

  private List<PaymentProvider> providersToTry(PaymentProvider primary, boolean tryFallback) {
    if (!tryFallback) {
      return List.of(primary);
    }
    LinkedHashSet<PaymentProvider> set = new LinkedHashSet<>();
    set.add(primary);
    set.addAll(mobileMoneyProperties.effectiveFallbackOrder());
    return new ArrayList<>(set);
  }

  private PaymentInitiateResponse buildSandboxResponse(
      long bookingId,
      long userId,
      Booking booking,
      Payment payment,
      PaymentProvider provider) {
    String token =
        jwtService.createPaymentCheckoutToken(bookingId, userId, provider.name());
    String base = paymentGatewayProperties.publicBaseUrl().replaceAll("/$", "");
    String checkoutUrl =
        base
            + "/api/payments/sandbox/checkout?token="
            + URLEncoder.encode(token, StandardCharsets.UTF_8);
    return new PaymentInitiateResponse(
        checkoutUrl, token, payment.getAmount(), payment.getCurrency(), provider);
  }

  private void cacheIdempotent(
      Payment payment, String effectiveKey, PaymentInitiateResponse res) {
    if (!StringUtils.hasText(effectiveKey)) {
      return;
    }
    payment.setIdempotencyKey(effectiveKey.trim());
    payment.setCheckoutUrlCache(res.checkoutUrl());
    payment.setPaymentTokenCache(res.paymentToken());
    paymentRepository.save(payment);
  }

  private static String firstNonBlank(String a, String b) {
    if (StringUtils.hasText(a)) {
      return a.trim();
    }
    if (StringUtils.hasText(b)) {
      return b.trim();
    }
    return null;
  }
}
