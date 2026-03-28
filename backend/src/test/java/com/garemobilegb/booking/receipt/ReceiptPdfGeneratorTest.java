package com.garemobilegb.booking.receipt;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReceiptPdfGeneratorTest {

  @Test
  void generate_producesPdfHeader() {
    ReceiptPdfGenerator gen = new ReceiptPdfGenerator();
    ReceiptPdfData data =
        new ReceiptPdfData(
            42L,
            "Gare de Bissau",
            "Bissau → Gabú",
            "GB-001",
            7,
            new BigDecimal("5000"),
            "XOF",
            "INTERNAL",
            "PAID",
            "CONFIRMED",
            "ref-xyz",
            "28 mars 2026 à 10:00",
            "27 mars 2026 à 18:00");
    byte[] pdf = gen.generate(data);
    assertThat(pdf).isNotEmpty();
    assertThat(new String(pdf, java.nio.charset.StandardCharsets.ISO_8859_1)).startsWith("%PDF");
  }
}
