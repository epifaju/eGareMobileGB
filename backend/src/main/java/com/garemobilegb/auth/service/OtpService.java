package com.garemobilegb.auth.service;

import com.garemobilegb.auth.dto.OtpResponse;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.shared.sms.SmsSender;
import java.security.SecureRandom;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

  private static final Logger log = LoggerFactory.getLogger(OtpService.class);
  private static final String PREFIX = "otp:";
  private static final Duration TTL = Duration.ofMinutes(5);
  private static final int EXPIRES_SECONDS = (int) TTL.toSeconds();

  private final StringRedisTemplate redis;
  private final SmsSender smsSender;
  private final SecureRandom random = new SecureRandom();

  public OtpService(StringRedisTemplate redis, SmsSender smsSender) {
    this.redis = redis;
    this.smsSender = smsSender;
  }

  /**
   * Génère un OTP, le stocke dans Redis (TTL 5 min). En prod, brancher un fournisseur SMS ;
   * ne jamais journaliser la valeur de l’OTP.
   */
  public OtpResponse requestOtp(String phoneNumber, boolean exposeOtpInResponse) {
    String code = String.format("%06d", random.nextInt(1_000_000));
    redis.opsForValue().set(PREFIX + phoneNumber, code, TTL);
    log.info("OTP généré pour un numéro se terminant par {}", suffix(phoneNumber));
    smsSender.sendOtpSms(phoneNumber, code);
    String debugOtp = exposeOtpInResponse ? code : null;
    return new OtpResponse(
        "Code envoyé par SMS (ou affiché en dev si activé).", EXPIRES_SECONDS, debugOtp);
  }

  /** Valide l’OTP et supprime la clé Redis (usage unique). */
  public void validateAndConsume(String phoneNumber, String otp) {
    String key = PREFIX + phoneNumber;
    String stored = redis.opsForValue().get(key);
    if (stored == null || !stored.equals(otp)) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST, "OTP_INVALID", "Code OTP invalide ou expiré");
    }
    redis.delete(key);
  }

  private static String suffix(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() < 4) {
      return "****";
    }
    return phoneNumber.substring(phoneNumber.length() - 4);
  }
}
