package com.garemobilegb.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "booking_id", nullable = false, unique = true)
  private Booking booking;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount = BigDecimal.ZERO;

  @Column(nullable = false, length = 3)
  private String currency = "XOF";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PaymentProvider provider = PaymentProvider.INTERNAL;

  @Column(length = 128)
  private String providerRef;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private PaymentStatus status = PaymentStatus.PENDING;

  /** Clé idempotence client (Phase 3) — unique si renseignée. */
  @Column(name = "idempotency_key", length = 128, unique = true)
  private String idempotencyKey;

  /** Réponse {@code initiate} mise en cache pour rejouer la même réponse (même clé idempotence). */
  @Column(name = "checkout_url_cache", columnDefinition = "TEXT")
  private String checkoutUrlCache;

  @Column(name = "payment_token_cache", columnDefinition = "TEXT")
  private String paymentTokenCache;

  /** Montant effectivement remboursé (souvent 80 % du payé — Phase 4). */
  @Column(name = "refund_amount", precision = 12, scale = 2)
  private BigDecimal refundAmount;

  @Column(name = "refunded_at")
  private Instant refundedAt;

  /** Référence côté opérateur après remboursement réel. */
  @Column(name = "refund_provider_ref", length = 128)
  private String refundProviderRef;

  protected Payment() {}

  public Payment(Booking booking, BigDecimal amount, String currency, PaymentProvider provider) {
    this.booking = booking;
    this.amount = amount != null ? amount : BigDecimal.ZERO;
    this.currency = currency != null ? currency : "XOF";
    this.provider = provider != null ? provider : PaymentProvider.INTERNAL;
  }

  public Long getId() {
    return id;
  }

  public Booking getBooking() {
    return booking;
  }

  public void setBooking(Booking booking) {
    this.booking = booking;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public PaymentProvider getProvider() {
    return provider;
  }

  public void setProvider(PaymentProvider provider) {
    this.provider = provider;
  }

  public String getProviderRef() {
    return providerRef;
  }

  public void setProviderRef(String providerRef) {
    this.providerRef = providerRef;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  public String getCheckoutUrlCache() {
    return checkoutUrlCache;
  }

  public void setCheckoutUrlCache(String checkoutUrlCache) {
    this.checkoutUrlCache = checkoutUrlCache;
  }

  public String getPaymentTokenCache() {
    return paymentTokenCache;
  }

  public void setPaymentTokenCache(String paymentTokenCache) {
    this.paymentTokenCache = paymentTokenCache;
  }

  public BigDecimal getRefundAmount() {
    return refundAmount;
  }

  public void setRefundAmount(BigDecimal refundAmount) {
    this.refundAmount = refundAmount;
  }

  public Instant getRefundedAt() {
    return refundedAt;
  }

  public void setRefundedAt(Instant refundedAt) {
    this.refundedAt = refundedAt;
  }

  public String getRefundProviderRef() {
    return refundProviderRef;
  }

  public void setRefundProviderRef(String refundProviderRef) {
    this.refundProviderRef = refundProviderRef;
  }
}
