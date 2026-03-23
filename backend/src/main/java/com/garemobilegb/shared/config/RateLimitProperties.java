package com.garemobilegb.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record RateLimitProperties(int rateLimitPerMinute) {

  public int perMinute() {
    return rateLimitPerMinute;
  }
}
