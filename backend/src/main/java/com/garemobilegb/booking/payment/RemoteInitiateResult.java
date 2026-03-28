package com.garemobilegb.booking.payment;

/**
 * Résultat d’un appel opérateur pour ouvrir une session de paiement (URL + référence externe).
 */
public record RemoteInitiateResult(String redirectUrl, String externalReference) {}
