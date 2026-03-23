package com.garemobilegb.shared.security;

import com.garemobilegb.shared.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Fenêtre fixe par minute : compteur Redis par IP et par tranche d’une minute.
 * Complexité : O(1) par requête (INCR + EXPIRE paresseux).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthRateLimitFilter extends OncePerRequestFilter {

  private static final String HEADER_XFF = "X-Forwarded-For";

  private final StringRedisTemplate redis;
  private final RateLimitProperties props;

  public AuthRateLimitFilter(StringRedisTemplate redis, RateLimitProperties props) {
    this.redis = redis;
    this.props = props;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/api/auth");
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String ip = resolveClientIp(request);
    long minuteBucket = System.currentTimeMillis() / 60_000L;
    String key = "rl:auth:" + ip + ":" + minuteBucket;

    Long count = redis.opsForValue().increment(key);
    if (count != null && count == 1L) {
      redis.expire(key, Duration.ofMinutes(2));
    }
    if (count != null && count > props.perMinute()) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType("application/json");
      response.getWriter().write("{\"code\":\"RATE_LIMIT\",\"message\":\"Trop de requêtes\"}");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private static String resolveClientIp(HttpServletRequest request) {
    String xff = request.getHeader(HEADER_XFF);
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
  }
}
