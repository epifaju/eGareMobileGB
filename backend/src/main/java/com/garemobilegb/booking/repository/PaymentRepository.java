package com.garemobilegb.booking.repository;

import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  @EntityGraph(attributePaths = {"booking", "booking.user"})
  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  @EntityGraph(attributePaths = "booking")
  Optional<Payment> findWithBookingById(Long id);

  long countByStatus(PaymentStatus status);
}
