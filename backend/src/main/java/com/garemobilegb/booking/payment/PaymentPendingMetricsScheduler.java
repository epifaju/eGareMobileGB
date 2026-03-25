package com.garemobilegb.booking.payment;

import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.repository.PaymentRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Jauge Prometheus : paiements en attente (PENDING) — suivi du backlog et des taux de succès. */
@Component
public class PaymentPendingMetricsScheduler {

  private final PaymentRepository paymentRepository;
  private final MeterRegistry meterRegistry;
  private final AtomicLong pendingCount = new AtomicLong();

  public PaymentPendingMetricsScheduler(
      PaymentRepository paymentRepository, MeterRegistry meterRegistry) {
    this.paymentRepository = paymentRepository;
    this.meterRegistry = meterRegistry;
  }

  @PostConstruct
  void registerGauge() {
    Gauge.builder("gare.payments.pending.count", pendingCount, AtomicLong::get)
        .description("Nombre de paiements en statut PENDING (Mobile Money non finalisé)")
        .register(meterRegistry);
    refreshPendingGauge();
  }

  @Scheduled(fixedDelayString = "${app.payment.mobile-money.retry.poll-interval-ms:120000}")
  public void refreshPendingGauge() {
    pendingCount.set(paymentRepository.countByStatus(PaymentStatus.PENDING));
  }
}
