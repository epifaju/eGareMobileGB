package com.garemobilegb.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * QR d'embarquement : signature RS256 côté serveur (clé privée). L'app conducteur n'embarque que
 * la clé publique (Phase 2).
 */
@ConfigurationProperties(prefix = "app.boarding.jwt")
public record BoardingJwtProperties(
    /**
     * PKCS#8 PEM RSA (optionnel). Si renseigné (ex. variable {@code BOARDING_JWT_PRIVATE_KEY}),
     * remplace le fichier {@link #privateKeyLocation}.
     */
    String privateKeyPem,
    /** Fichier PKCS#8 par défaut (développement). */
    Resource privateKeyLocation,
    /** Durée de vie du QR (défaut 24 h), en ms — utilisée pour {@code exp} JWT. */
    long expirationMs) {}
