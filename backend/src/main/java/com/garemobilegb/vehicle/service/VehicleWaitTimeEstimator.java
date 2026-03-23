package com.garemobilegb.vehicle.service;

import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Heuristique v1 (R4) : estimation d'attente en minutes.
 *
 * <p>Règles simples :
 *
 * <ul>
 *   <li>PARTI => 0
 *   <li>COMPLET => 2 (embarquement imminent)
 *   <li>Sinon base sur taux de remplissage + proximité du départ
 * </ul>
 */
@Component
public class VehicleWaitTimeEstimator {

  private final VehicleWaitCalibrationService calibrationService;

  public VehicleWaitTimeEstimator(VehicleWaitCalibrationService calibrationService) {
    this.calibrationService = calibrationService;
  }

  public int estimateMinutes(Vehicle v, Instant now) {
    if (v.getStatus() == VehicleStatus.PARTI) {
      return 0;
    }
    if (v.getStatus() == VehicleStatus.COMPLET) {
      return 2;
    }

    int capacity = Math.max(1, v.getCapacity());
    int occupied = Math.max(0, Math.min(v.getOccupiedSeats(), capacity));
    double fillRatio = (double) occupied / (double) capacity;

    // Base : moins le véhicule est rempli, plus l'attente est élevée.
    int base = (int) Math.round((1.0 - fillRatio) * 24.0); // 0..24

    // Ajustement selon statut métier.
    if (v.getStatus() == VehicleStatus.EN_FILE) {
      base += 6;
    } else if (v.getStatus() == VehicleStatus.REMPLISSAGE) {
      base += 2;
    }

    // Ajustement heure de départ si connue.
    if (v.getDepartureScheduledAt() != null) {
      long minsToDeparture = Duration.between(now, v.getDepartureScheduledAt()).toMinutes();
      if (minsToDeparture <= 0) {
        return 1;
      }
      base = Math.min(base, (int) minsToDeparture);
    }

    int baseline = Math.max(1, Math.min(base, 45));
    var calibration = calibrationService.lookup(v, now);
    if (calibration.isEmpty()) {
      return baseline;
    }
    double avg = calibration.get().avgMinutes();
    long samples = calibration.get().samples();
    double confidence = Math.min(0.75, Math.max(0.25, samples / 20.0));
    int blended = (int) Math.round((baseline * (1.0 - confidence)) + (avg * confidence));
    return Math.max(1, Math.min(blended, 45));
  }
}
