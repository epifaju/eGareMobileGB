package com.garemobilegb.shared.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.shared.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  @Test
  void createAccessToken_peut_etre_valide() {
    JwtProperties props =
        new JwtProperties("dev-secret-key-min-32-chars-long!!", 900_000L, 604_800_000L);
    JwtService jwt = new JwtService(props);
    String token = jwt.createAccessToken(42L, "+24570000000", Role.USER);
    Claims claims = jwt.parseAndValidate(token, JwtService.TYP_ACCESS);
    assertEquals("42", claims.getSubject());
    assertEquals(Role.USER.name(), claims.get(JwtService.CLAIM_ROLE));
  }
}
