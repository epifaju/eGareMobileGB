package com.garemobilegb.booking.domain;

/** Passerelle ou mode de règlement (PRD : Orange Money, Wave, etc.). */
public enum PaymentProvider {
  /** Dev / Phase 0 : confirmation sans passerelle externe. */
  INTERNAL,
  ORANGE_MONEY,
  WAVE,
  MTN,
  CARD
}
