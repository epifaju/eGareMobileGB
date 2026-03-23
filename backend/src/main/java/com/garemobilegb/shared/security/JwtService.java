package com.garemobilegb.shared.security;

import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.shared.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  public static final String CLAIM_TYP = "typ";
  public static final String CLAIM_ROLE = "role";
  public static final String TYP_ACCESS = "access";
  public static final String TYP_REFRESH = "refresh";

  private final JwtProperties props;
  private final SecretKey key;

  public JwtService(JwtProperties props) {
    this.props = props;
    byte[] bytes = props.secret().getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) {
      throw new IllegalStateException("JWT secret doit faire au moins 32 octets (HS256).");
    }
    this.key = Keys.hmacShaKeyFor(bytes);
  }

  public String createAccessToken(long userId, String phoneNumber, Role role) {
    return buildToken(userId, phoneNumber, role, TYP_ACCESS, props.accessExpirationMs());
  }

  public String createRefreshToken(long userId, String phoneNumber, Role role) {
    return buildToken(userId, phoneNumber, role, TYP_REFRESH, props.refreshExpirationMs());
  }

  private String buildToken(
      long userId, String phoneNumber, Role role, String typ, long ttlMs) {
    Instant issued = Instant.now();
    Instant exp = issued.plusMillis(ttlMs);
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim(CLAIM_TYP, typ)
        .claim(CLAIM_ROLE, role.name())
        .claim("phone", phoneNumber)
        .issuedAt(Date.from(issued))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public Claims parseAndValidate(String token, String expectedTyp) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      String typ = claims.get(CLAIM_TYP, String.class);
      if (!expectedTyp.equals(typ)) {
        throw new JwtException("Type de jeton invalide");
      }
      return claims;
    } catch (ExpiredJwtException e) {
      throw e;
    } catch (JwtException e) {
      throw new JwtException("Jeton invalide", e);
    }
  }

}
