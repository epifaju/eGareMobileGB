# Intégration Orange Money (Web Payment)

## Principe

1. **OAuth 2** (client credentials) sur `oauth-token-url` → `access_token`.
2. **POST** `web-payment-url` avec `Authorization: Bearer` et un corps JSON (merchant, montant, URLs de retour / notification).
3. Réponse typique : `payment_url` (redirection utilisateur) et `pay_token` (référence).

Si `web-payment-url`, `client-id`, `client-secret`, `merchant-key` et `oauth-token-url` sont **tous** renseignés, ce flux est utilisé. Sinon, le backend retombe sur l’appel **générique** (`base-url` + POST JSON legacy), utile pour des mocks.

## Variables d’environnement (exemple)

| Variable | Rôle |
|----------|------|
| `PAYMENT_MODE` | `production` pour appeler Orange (sinon simulateur interne). |
| `PAYMENT_PUBLIC_BASE_URL` | Base utilisée pour `notif_url`, `return_url`, `cancel_url` par défaut. |
| `ORANGE_MONEY_OAUTH_TOKEN_URL` | Endpoint token (souvent fourni par Orange pour votre pays). |
| `ORANGE_MONEY_WEB_PAYMENT_URL` | Endpoint création de session Web Payment. |
| `ORANGE_MONEY_CLIENT_ID` / `ORANGE_MONEY_CLIENT_SECRET` | Identifiants application (portail Orange Partner). |
| `ORANGE_MONEY_MERCHANT_KEY` | Clé commerçant. |

Les URLs exactes **dépendent du pays** et du contrat — à copier depuis la documentation Orange livrée avec votre souscription.

## Webhook

Par défaut, `notif_url` = `{public-base-url}/api/webhooks/payments/orange-money`.

Le corps doit rester compatible avec `PaymentWebhookRequest` (bookingId, status SUCCESS/PAID, etc.). Si Orange envoie un autre format, ajoutez un adaptateur dédié (mapper vers `confirmPaymentFromWebhook`).

## Pages retour

Après paiement, Orange redirige vers :

- succès : `/api/payments/orange/return?bookingId=…`
- annulation : `/api/payments/orange/cancel?bookingId=…`

Vous pouvez surcharger avec `return-url` / `cancel-url` dans `application.yml` (placeholders `{bookingId}` supportés côté code).
