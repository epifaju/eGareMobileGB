# Sandbox paiement — parcours stable (Phase 3)

## Prérequis

| Configuration | Valeur attendue |
|---------------|-----------------|
| `app.booking.auto-confirm-without-payment-gateway` | `false` (défaut dans `application.yml`) |
| `app.payment.mode` | `sandbox` |
| `app.payment.webhook-secret` | Identique entre le backend et les appels webhook (ex. `dev-webhook-secret-change-me`) |
| `app.payment.public-base-url` | URL atteignable par le navigateur du téléphone (ex. `http://192.168.1.x:8080`). **Obligatoire** : le bouton « Confirmer » appelle `{public-base-url}/api/webhooks/payments/sandbox`. |

Pour retrouver l’ancien comportement « réservation confirmée sans paiement » :

```bash
set APP_BOOKING_AUTO_CONFIRM_WITHOUT_PAYMENT_GATEWAY=true
```

(Linux/mac : `export APP_BOOKING_AUTO_CONFIRM_WITHOUT_PAYMENT_GATEWAY=true`)

## Parcours E2E (manuel)

1. Démarrer PostgreSQL + Redis + backend (`SERVER_PORT=8080`).
2. Côté mobile ou API : **réserver un siège** → statut **PENDING_PAYMENT**.
3. **POST** `/api/bookings/{id}/payment/initiate` (JWT utilisateur) avec `provider` (ex. `ORANGE_MONEY`) → réponse `checkoutUrl` + `paymentToken`.
4. Ouvrir `checkoutUrl` dans un navigateur (souvent sur le même LAN que le téléphone).
5. Cliquer **Confirmer le paiement** → le backend reçoit le webhook et passe la réservation en **CONFIRMÉ** / paiement **PAID**.
6. Vérifier **GET** `/api/me/bookings` ou l’écran « Mes réservations ».

## Webhook sans page HTML (curl)

Remplacez `BASE`, `SECRET` et `BOOKING_ID` :

```bash
curl -sS -X POST "${BASE}/api/webhooks/payments/sandbox" \
  -H "Content-Type: application/json" \
  -H "X-Sandbox-Secret: ${SECRET}" \
  -d "{\"bookingId\":${BOOKING_ID},\"externalTransactionId\":\"sandbox-tx-manual\",\"status\":\"SUCCESS\",\"provider\":\"ORANGE_MONEY\"}"
```

Réponse attendue : **202 Accepted**.

## Dépannage

- **401 sur webhook** : `X-Sandbox-Secret` ≠ `app.payment.webhook-secret`.
- **Réseau / fetch échoue** depuis la page sandbox : `public-base-url` incorrect (mauvais IP ou port) ou CORS inutile ici (même origine). Utiliser l’URL exacte affichée dans `checkoutUrl`.
- **Mode production** (`app.payment.mode=production`) : la page `/api/payments/sandbox/checkout` affiche un message de désactivation ; les vrais paiements passent par les APIs opérateurs, pas cette page.
