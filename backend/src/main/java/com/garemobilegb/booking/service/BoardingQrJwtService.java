package com.garemobilegb.booking.service;

import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.shared.config.BoardingJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * JWT compact RS256 pour QR billet : signature serveur (clé privée), vérification offline possible
 * avec la clé publique. Claims stables P2.1 : {@code bookingId}, {@code vehicleId}, {@code seat},
 * {@code exp} (~24h depuis émission), {@code jti}.
 */
@Service
public class BoardingQrJwtService {

  public static final String TYP_BOARDING_QR = "boarding_qr";
  public static final String CLAIM_TYP = "typ";
  /** Claim court (rétrocompat). */
  public static final String CLAIM_BOOKING_ID = "bid";
  /** Claim court (rétrocompat). */
  public static final String CLAIM_VEHICLE_ID = "vid";
  /** Claim court (rétrocompat). -1 = siège non assigné. */
  public static final String CLAIM_SEAT = "sn";
  public static final String CLAIM_JTI = "jti";

  public static final String CLAIM_BOOKING_ID_SPEC = "bookingId";
  public static final String CLAIM_VEHICLE_ID_SPEC = "vehicleId";
  /** -1 si absent (toujours présent pour format stable). */
  public static final String CLAIM_SEAT_SPEC = "seat";

  private static final int SEAT_UNASSIGNED = -1;

  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private final long expirationMs;

  public BoardingQrJwtService(BoardingJwtProperties props) {
    try {
      String pem = loadPrivateKeyPem(props);
      this.privateKey = parsePkcs8PrivateKey(pem);
      this.publicKey = derivePublicKey(this.privateKey);
      this.expirationMs = props.expirationMs() > 0 ? props.expirationMs() : 86_400_000L;
    } catch (Exception e) {
      throw new IllegalStateException(
          "app.boarding.jwt : impossible de charger la clé privée RS256 (PKCS#8 PEM).", e);
    }
  }

  private static String loadPrivateKeyPem(BoardingJwtProperties props) throws IOException {
    if (StringUtils.hasText(props.privateKeyPem())) {
      return props.privateKeyPem().trim();
    }
    if (props.privateKeyLocation() == null) {
      throw new IllegalStateException(
          "Définir app.boarding.jwt.private-key-pem ou private-key-location (PKCS#8 PEM).");
    }
    return props.privateKeyLocation().getContentAsString(StandardCharsets.UTF_8);
  }

  private static PrivateKey parsePkcs8PrivateKey(String pem) throws Exception {
    String stripped =
        pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    byte[] bytes = Base64.getDecoder().decode(stripped);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
    return KeyFactory.getInstance("RSA").generatePrivate(spec);
  }

  private static PublicKey derivePublicKey(PrivateKey privateKey) throws Exception {
    RSAPrivateCrtKey rsa = (RSAPrivateCrtKey) privateKey;
    RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(rsa.getModulus(), rsa.getPublicExponent());
    return KeyFactory.getInstance("RSA").generatePublic(pubSpec);
  }

  /** Génère le contenu du QR (JWT compact). Nécessite {@link Booking#getId()} non null. */
  public String createBoardingQrToken(Booking booking) {
    if (booking.getId() == null) {
      throw new IllegalStateException("Booking doit être persisté avant génération du QR JWT.");
    }
    Instant issued = Instant.now();
    Instant exp = issued.plusMillis(expirationMs);
    String jti = UUID.randomUUID().toString();
    long bookingId = booking.getId();
    long vehicleId = booking.getVehicle().getId();
    int seatClaim =
        booking.getSeatNumber() != null ? booking.getSeatNumber() : SEAT_UNASSIGNED;
    var builder =
        Jwts.builder()
            .claim(CLAIM_TYP, TYP_BOARDING_QR)
            .claim(CLAIM_BOOKING_ID_SPEC, bookingId)
            .claim(CLAIM_VEHICLE_ID_SPEC, vehicleId)
            .claim(CLAIM_SEAT_SPEC, seatClaim)
            .claim(CLAIM_BOOKING_ID, bookingId)
            .claim(CLAIM_VEHICLE_ID, vehicleId)
            .claim(CLAIM_SEAT, seatClaim)
            .claim(CLAIM_JTI, jti)
            .issuedAt(Date.from(issued))
            .expiration(Date.from(exp));
    return builder.signWith(privateKey, Jwts.SIG.RS256).compact();
  }

  public Claims parseAndValidate(String token) {
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(publicKey)
              .build()
              .parseSignedClaims(token.trim())
              .getPayload();
      String typ = claims.get(CLAIM_TYP, String.class);
      if (!TYP_BOARDING_QR.equals(typ)) {
        throw new JwtException("Type de jeton QR invalide");
      }
      return claims;
    } catch (ExpiredJwtException e) {
      throw e;
    } catch (JwtException e) {
      throw new JwtException("Jeton QR invalide", e);
    }
  }
}
