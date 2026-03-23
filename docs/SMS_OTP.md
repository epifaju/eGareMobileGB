# SMS / OTP (Phase 0)

## Comportement actuel

- **OTP** : généré et stocké dans **Redis** (TTL 5 min), validation inchangée (`AuthController` / `OtpService`).
- **Envoi SMS** : **non** branché à un fournisseur réel (Orange, MTN, etc.). Le flux est extensible via `SmsSender` (`com.garemobilegb.shared.sms`).

## Configuration (`application.yml`)

| Clé | Valeurs | Effet |
|-----|---------|--------|
| `app.sms.provider` | `NONE` | Aucun appel sortant (OTP toujours en Redis). |
| | `LOG` | Journalisation « SMS simulé » (pas d’OTP en clair dans les logs). |
| | `AFRICASTALKING` | **Passerelle non intégrée** en Phase 0 ; un avertissement est émis au moment de l’envoi (OTP toujours stocké Redis). |

## Intégration réelle (prochaine étape)

1. Ajouter une implémentation `SmsSender` (HTTP REST ou SDK du fournisseur).
2. Secrets / clés API en variables d’environnement, **jamais** dans le dépôt.
3. Respecter la RGPD / consentement pour l’usage du numéro de téléphone.
4. Ne **jamais** journaliser le code OTP en production.

## Référence code

- `SmsSender`, `SmsSenderConfiguration`, `LoggingSmsSender`
- `OtpService` appelle `smsSender.sendOtpSms(phoneNumber, code)` après écriture Redis.
