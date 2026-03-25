package com.garemobilegb.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PhoneMaskTest {

  @Test
  void masksLongNumber() {
    assertThat(PhoneMask.mask("+24500112233")).contains("****").endsWith("2233");
  }

  @Test
  void shortReturnsStars() {
    assertThat(PhoneMask.mask("12345")).isEqualTo("****");
  }
}
