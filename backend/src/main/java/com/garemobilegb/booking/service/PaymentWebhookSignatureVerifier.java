package com.garemobilegb.booking.service;

import com.garemobilegb.shared.config.PaymentGatewayProperties;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class PaymentWebhookSignatureVerifier {

  private final PaymentGatewayProperties props;

  public PaymentWebhookSignatureVerifier(PaymentGatewayProperties props) {
    this.props = props;
  }

  public boolean verifySandboxSecret(String headerValue) {
    if (headerValue == null || props.webhookSecret() == null) {
      return false;
    }
    return props.webhookSecret().equals(headerValue);
  }

  /** Production : en-tête {@code X-Signature: sha256=<hex>}. */
  public boolean verifyProductionHmac(String rawBody, String signatureHeader) {
    if (signatureHeader == null
        || !signatureHeader.startsWith("sha256=")
        || props.webhookSecret() == null) {
      return false;
    }
    String expectedHex = signatureHeader.substring("sha256=".length());
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(props.webhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
      String hex = bytesToHex(hash);
      return constantTimeEquals(hex.toLowerCase(), expectedHex.toLowerCase());
    } catch (Exception e) {
      return false;
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private static boolean constantTimeEquals(String a, String b) {
    if (a.length() != b.length()) {
      return false;
    }
    int r = 0;
    for (int i = 0; i < a.length(); i++) {
      r |= a.charAt(i) ^ b.charAt(i);
    }
    return r == 0;
  }
}
