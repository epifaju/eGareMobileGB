package com.garemobilegb.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthDevProperties(
    boolean exposeOtpInResponse,
    /**
     * Si true : l’inscription peut demander le rôle DRIVER via {@code registerAsDriver} dans le body.
     * Désactivé par défaut (prod) pour éviter que n’importe qui s’enregistre comme conducteur.
     */
    boolean allowDriverSelfRegistration) {}
