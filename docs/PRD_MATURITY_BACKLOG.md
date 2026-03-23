# Gare Mobile GB — Maturité PRD v2.0 vs code actuel

Document de référence pour prioriser sans mélanger les couches (auth, métier, offline, paiement).  
Légende des statuts : **Fait** | **Partiel** | **Manquant**

### Phase 0 — Fondations (livré)

- Schéma **bookings** + **payments** (statuts réservation : `PENDING_PAYMENT`, `CONFIRMED`, `CANCELLED`, `EXPIRED` ; paiement `INTERNAL` + confirmation stub). L’ancienne table `reservations` n’est plus utilisée par l’API (nettoyage / migration Flyway à prévoir en prod).
- API alignées PRD : `GET /api/me/bookings`, `GET /api/bookings/{id}`, `DELETE /api/bookings/{id}`, `POST /api/payments/{bookingId}/confirm` ; les anciens `GET /api/me/reservations` et `DELETE /api/reservations/{id}` restent disponibles en **déprécié**.
- OTP / SMS : interface `SmsSender`, config `app.sms.provider`, doc `docs/SMS_OTP.md` — **aucun** fournisseur SMS réel intégré en Phase 0.
- Qualité : tests `BookingServiceTest`, CI inchangée (`mvn verify`, mobile `typecheck` + Jest).

### Phase 1 — Recherche §4.1 (livré)

- **Destinations + autocomplétion** : `GET /api/destinations/suggest?q=` (libellés distincts `routeLabel`, min. 2 caractères).
- **Recherche filtrée** : `GET /api/search/vehicles` avec `stationId`, `q`, `status`, `minFareXof`, `maxFareXof`, `departureAfter` / `departureBefore` (ISO-8601), `activeOnly` (pagination Spring).
- **Tarif** : champ `fareAmountXof` sur les véhicules (seed démo + affichage mobile).
- **Mobile** : écran **Recherche** (autocomplétion, filtres gare / statut / prix / fenêtre départ, préréglage « 2 h »), entrée depuis l’accueil voyageur.

### Phase 1b — R3 carte + affinage recherche (livré)

- **R3** : écran **Carte des gares** (`react-native-maps`, `expo-location`), marqueurs sur coordonnées stations, position utilisateur, navigation vers véhicules ; **web** : liste de secours sans carte.
- **Recherche** : `SearchSynonyms` (accents, variantes typo type gabu→gabú), autocomplétion et `VehicleSpecifications` enrichis ; option PostgreSQL documentée dans `docs/SEARCH_PG.md`.

### Phase 2 — Sièges (B1 livré)

- **`seat_number` exploité** en réservation : `POST /api/vehicles/{id}/reserve-seat` accepte `{ seatNumber }` (optionnel).
- **SeatMap API structurée** : `GET /api/vehicles/{id}/seat-map` (layout `L8/L15/L20`, grille avec cellules siège/allée, indisponibles/disponibles).
- **Conflits** : validation siège invalide / déjà pris + verrou pessimiste véhicule pour limiter la concurrence.
- **Mobile** : écran **SeatMap** avec rendu différencié 8/15/20 + rafraîchissement fréquent pour convergence temps réel.

---

## Synthèse par thème

| Thème | Couverture | Commentaire |
|--------|------------|-------------|
| Auth & rôles | Partiel | JWT, refresh, OTP (dev), rôles USER/DRIVER/ADMIN — pas SMS prod type Africa’s Talking |
| Gares & véhicules | Partiel | Liste gares, véhicules par gare, **recherche + filtres** (§4.1) — pas carte GPS (R3) |
| Temps réel | Partiel | STOMP / topic gare — pas le chemin `WS /ws/vehicles/{id}` du PRD à la lettre |
| Réservation | Partiel | Modèle **booking** + **payment**, siège, `qr_token`, TTL ; liste / annulation — pas plan de sièges temps réel ni passerelles payantes |
| Conducteur | Partiel | Statuts + onglet dédié — pas scan QR, manifeste, revenus |
| Offline | Manquant | Bandeau NetInfo seulement — pas cache 24h, file sync, queue FIFO |
| Paiement | Partiel | Entité paiement + `POST .../confirm` (stub interne) ; pas Orange / Wave / MTN |
| Admin produit | Manquant | Pas dashboard métriques ; promotion rôle via API seulement |
| Qualité / Ops | Partiel | CI basique ; pas Detox, JMeter, K8s, Sentry/Grafana comme au PRD |

---

## Backlog détaillé (PRD §4 et §5)

### 4.1 Recherche & visibilité (passager)

| ID | Exigence PRD | P | Statut | Suite logique |
|----|----------------|----|--------|----------------|
| R1 | Recherche par destination + autocomplétion | P0 | Fait | Suggestions enrichies + normalisation accents/typos + UI mobile |
| R2 | Filtres (statut véhicule, prix, heure) | P0 | Fait | Filtres complets + tri + créneaux horaires + persistance locale |
| R3 | Carte gares + GPS + marqueurs temps réel | P1 | Fait | Carte gares + GPS + marqueurs véhicules live (endpoint + topic map) |
| R4 | Estimation attente (ML simple) | P1 | Fait | v2 data-driven : historisation + calibration station/tranche horaire + affichage mobile |
| R5 | Vue liste alternative (réseau faible) | P2 | Fait | Endpoint compact + écran mobile dédié + affichage payload approx |

### 4.2 Réservation & paiement

| ID | Exigence PRD | P | Statut | Suite logique |
|----|----------------|----|--------|----------------|
| B1 | Plan de sièges (8/15/20) temps réel | P0 | Fait | SeatMap API + UI mobile 8/15/20 + conflits sièges |
| B2 | Orange Money | P0 | Manquant | Sandbox + webhook confirmation |
| B3 | Wave | P0 | Manquant | Idem |
| B4 | MTN MoMo | P1 | Manquant | Après Orange/Wave |
| B5 | QR billet offline 24h | P0 | Manquant | JWT + QR côté booking |
| B6 | Annulation + remboursement 80 % / 48h | P1 | Partiel | Annulation place OK ; pas remboursement |
| B7 | Reçu PDF SMS/WhatsApp | P2 | Manquant | Génération PDF + file d’envoi |

### 4.3 Conducteur — file

| ID | Exigence PRD | P | Statut | Suite logique |
|----|----------------|----|--------|----------------|
| D1 | Boutons statut En file → Parti | P0 | Fait | — |
| D2 | Scan QR embarquement | P0 | Manquant | `vision-camera` + endpoint validation |
| D3 | Notifications paliers 80/90/100 % | P0 | Manquant | FCM + règles métier |
| D4 | Liste passagers / manifeste | P1 | Manquant | API liste réservations par véhicule (conducteur) |
| D5 | Historique revenus | P2 | Manquant | Agrégation paiements |

### 4.4 Hors ligne (CORE PRD)

| ID | Exigence PRD | Statut | Suite logique |
|----|--------------|--------|---------------|
| O1 | Cache gares 24h (SQLite) | Manquant | Persistance requêtes RTK + SQLite ou WatermelonDB |
| O2 | File d’attente actions (FIFO) | Manquant | Queue locale + replay au `NetInfo` |
| O3 | QR local + sync différée | Manquant | Dépend B5 + O2 |
| O4 | Statut conducteur : queue offline | Manquant | Même mécanisme O2 |

---

## Infrastructure & sécurité (PRD §5 / §7)

| ID | Sujet | Statut | Suite logique |
|----|--------|--------|---------------|
| I1 | PostGIS / index géo | Manquant | Si carte P1 |
| I2 | MinIO (reçus, QR assets) | Manquant | Quand PDF/QR |
| I3 | Rate limiting Redis (déjà partiel ?) | À vérifier | Aligner PRD 100/min IP |
| I4 | Audit logs 12 mois | Manquant | Table + aspect |

---

## Alignement sur les 4 sprints PRD (§8.1)

| Sprint PRD | Critère de sortie PRD | État actuel (honnête) |
|------------|-------------------------|------------------------|
| **S1** — Setup & Auth | Auth E2E, tests &gt; 70 % | Auth utilisable ; **tests 70 %** et **OTP SMS prod** non vérifiés |
| **S2** — Recherche & réservation | E2E résa sans paiement &lt; 3 min | **Pas** recherche/filtres/SeatMap/QR ; résa simplifiée **sans** parcours PRD complet |
| **S3** — Paiement & conducteur | Paiement sandbox &gt; 95 % | **Paiement absent** ; conducteur **sans** scan ni push |
| **S4** — Tests & déploiement | Staging 99 %, 0 bug P0 | **Non** atteint |

**Lecture** : le code actuel correspond à un **transversal** « socle + véhicules + résa légère + conducteur statut », pas à la clôture séquentielle des sprints PRD.

---

## Objectif stratégique validé : conformité PRD v2

**Décision produit** : viser l’alignement avec le PRD v2 sur **recherche**, **sièges**, **paiement**, **offline fort** (et le reste des §4–5 : QR, conducteur avancé, remboursements, etc.).

Cela implique : évolution du **modèle de données** (bookings / payments proches du schéma PRD), **nouvelles intégrations** (Mobile Money, FCM, cartes, caméra), et **architecture offline** (cache + file de sync), pas seulement des écrans isolés.

---

## Feuille de route par phases (ordre de dépendances)

Les blocs ci-dessous minimisent les impasses techniques : chaque phase prépare la suivante.

| Phase | Focus | Livrables typiques (PRD) |
|-------|--------|---------------------------|
| **0 — Fondations** | Modèle métier & API stables | Tables / entités `booking` (siège, statuts `PENDING` / `PAID` / `CANCELLED`), `payment`, webhooks ; alignement endpoints sur §5.5 ; OTP SMS prod ou fournisseur documenté ; tests ciblés + CI. |
| **1 — Recherche & visibilité** | §4.1 | Destinations indexées, autocomplétion, filtres P0, carte P1 si PostGIS / coords OK. |
| **2 — Sièges & réservation complète** | §4.2 (sans paiement d’abord si besoin) | SeatMap, attribution `seat_number`, règles de conflit, flux réservation E2E &lt; 3 min (critère Sprint 2 PRD). |
| **3 — Offline fort** | §4.4 | Cache local (SQLite / équivalent), invalidation 24h, **file FIFO** des mutations, reconnexion + sync ; WebSocket / conducteur en file d’attente si applicable. |
| **4 — Paiement** | §4.2 + §9 | Sandbox Orange Money + Wave (+ MTN P1), états `PENDING` → `PAID`, gestion erreurs, fallback PRD. |
| **5 — QR & embarquement** | §4.2–4.3 | Génération QR (JWT / token réservation), scan conducteur, validation API + mode offline différé. |
| **6 — Conducteur & ops** | §4.3 + §8 | Manifeste passagers, push paliers 80/90/100 %, revenus P2 ; Detox, charge, monitoring, déploiement (Sprint 4 PRD). |

**Remarque** : le PRD place l’offline en **CORE** — en pratique, les **spécifications** du cache et de la queue (Phase 3) doivent être **définies tôt** (Phase 0–1), même si l’implémentation complète suit le modèle booking (Phase 2–3).

---

## Risques à anticiper

- **Délai** : le périmètre PRD v2 dépasse largement un seul cycle court ; prévoir des **jalons** par phase avec critères d’acceptation mesurables.
- **Paiement** : dépendances externes (contrats, sandboxes, devises XOF).
- **Offline + paiement** : ordre des opérations idempotent côté serveur pour éviter double débit au replay.

---

*Objectif PRD v2 et phases : à valider avec le Product Owner ; le tableau de maturité ci-dessus sert de checklist de couverture.*
