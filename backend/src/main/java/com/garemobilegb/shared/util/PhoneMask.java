package com.garemobilegb.shared.util;

/** Masque un numéro pour affichage conducteur / manifeste (RGPD minimal). */
public final class PhoneMask {

  private PhoneMask() {}

  public static String mask(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.isBlank()) {
      return "—";
    }
    String p = phoneNumber.trim();
    if (p.length() <= 6) {
      return "****";
    }
    return p.substring(0, 4) + "****" + p.substring(p.length() - 4);
  }
}
