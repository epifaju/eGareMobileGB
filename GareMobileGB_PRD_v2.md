+-----------------------------------------------------------------------+
| **🚌 GARE MOBILE GB**                                                 |
|                                                                       |
| Document de Spécifications Produit (PRD) --- Version 2.0              |
|                                                                       |
| *Application Transport Communautaire --- Guinée-Bissau*               |
+=======================================================================+
+-----------------------------------------------------------------------+

  -----------------------------------------------------------------------
  **Version**            2.0 MVP Amélioré
  ---------------------- ------------------------------------------------
  **Date**               Mars 2026

  **Statut**             ✅ Prêt pour développement Cursor AI

  **Équipe**             Product / Tech / Design

  **Stack**              React Native + Spring Boot + PostgreSQL

  **Délai MVP**          11 semaines (4 sprints)
  -----------------------------------------------------------------------

# 1. Résumé Exécutif

Gare Mobile GB est une application mobile qui digitalise le système de
transport routier en Guinée-Bissau. Elle s\'adapte au modèle local :
départ au remplissage complet, sans horaires fixes, avec gestion de file
d\'attente dynamique.

+-----------------------------------------------------------------------+
| **⚡ Problème résolu**                                                |
|                                                                       |
| • Les passagers attendent en moyenne 2-4h en gare sans visibilité sur |
| les départs                                                           |
|                                                                       |
| • Les conducteurs remplissent leurs véhicules inefficacement (perte   |
| de revenus \~30%)                                                     |
|                                                                       |
| • Les arnaques et conflits de places sont fréquents faute de          |
| traçabilité                                                           |
|                                                                       |
| • Aucun système de paiement sécurisé adapté au contexte local         |
| n\'existe                                                             |
+=======================================================================+
+-----------------------------------------------------------------------+

# 2. Objectifs & KPIs

## 2.1 Objectifs Business

  --------------------------------------------------------------------------
  **KPI**                **Cible**    **Délai**   **Méthode de mesure**
  ---------------------- ------------ ----------- --------------------------
  Réduction temps        −50%         M+3         Enquête terrain
  d\'attente                                      avant/après

  Utilisateurs actifs    10 000       M+6         DAU/MAU Firebase Analytics

  Conducteurs            500          M+6         Dashboard admin
  partenaires                                     

  Taux complétion        ≥ 80%        M+2         Funnel analytics
  réservation                                     

  Uptime API             ≥ 95%        Continu     Grafana / Sentry

  Note Play Store        ≥ 4.5 ★      M+4         Google Play Console
  --------------------------------------------------------------------------

## 2.2 Objectifs Utilisateurs

  -------------------------------------------------------------------------
  **Utilisateur**   **Objectif principal**      **Critère de succès**
  ----------------- --------------------------- ---------------------------
  Passager          Réserver sa place sans      Temps recherche→réservation
                    attendre physiquement       \< 5 min

  Conducteur        Optimiser remplissage et    Réduction temps attente
                    revenus                     départ \> 30%

  Agent de gare     Gérer les embarquements     Taux de litiges \< 2%
                    sans conflits               

  Administrateur    Superviser la plateforme en Tableau de bord \< 3s de
                    temps réel                  latence
  -------------------------------------------------------------------------

# 3. Utilisateurs Cibles & Personas

## 3.1 Personas Détaillés

  --------------------------------------------------------------------------
  **Persona**       **Profil**        **Besoins clés**    **Points de
                                                          friction actuels**
  ----------------- ----------------- ------------------- ------------------
  Aminata, 28 ans   Commerçante,      Savoir quand        Attente
  (Passagère        voyage 2x/sem     partir, payer sans  imprévisible,
  fréquente)        Bissau↔Bafatá,    cash                arnaques sur les
                    smartphone                            sièges
                    Android basique                       

  Carlos, 42 ans    Propriétaire de   Remplir vite,       Places non
  (Conducteur       taxi-brousse, 15  éviter les          honorées, bagarre
  partenaire)       ans               passagers fantômes  pour sièges
                    d\'expérience,                        
                    faible                                
                    alphabétisation                       
                    numérique                             

  Idrissa, 35 ans   Employé de gare   Valider les         Papier/stylo,
  (Agent de gare)   routière, gère    embarquements       conflits
                    20+               rapidement          quotidiens
                    véhicules/jour                        

  Admin Système     Équipe technique, Monitoring,         Aucun outil
                    supervise la      résolution          centralisé actuel
                    plateforme        incidents, rapports 
  --------------------------------------------------------------------------

# 4. Fonctionnalités MVP --- Spécifications Détaillées

## 4.1 Module Recherche & Visibilité (Passager)

  -------------------------------------------------------------------------------
  **Fonctionnalité**   **Description**        **Priorité**   **Critère
                                                             d\'acceptance**
  -------------------- ---------------------- -------------- --------------------
  Recherche par        Autocomplétion :       P0             Résultats \< 500ms
  destination          Bafatá, Gabu, Buba,                   
                       Cacheu, Mansoa\...                    

  Filtres avancés      Statut véhicule (En    P0             Filtres combinables
                       file / Remplissage /                  
                       Départ imminent), prix                
                       max, heure souhaitée                  

  Carte des gares      Géolocalisation GPS    P1             Offline avec cache
                       des gares routières                   24h
                       avec marqueurs statut                 
                       temps réel                            

  Estimation attente   Algorithme basé sur    P1             Précision ±20 min
                       historique des départs                
                       (ML simple en v1)                     

  Vue liste            Affichage liste pour   P2             \< 50KB de données
  alternative          zones réseau faible                   
  -------------------------------------------------------------------------------

## 4.2 Module Réservation & Paiement

  ----------------------------------------------------------------------------------
  **Fonctionnalité**   **Description**         **Priorité**   **Critère
                                                              d\'acceptance**
  -------------------- ----------------------- -------------- ----------------------
  Sélection de siège   Plan visuel du véhicule P0             Mise à jour temps réel
                       (8/15/20 places) avec                  
                       sièges                                 
                       disponibles/occupés                    

  Paiement Orange      Intégration API         P0             Taux succès \> 95%
  Money                officielle,                            
                       confirmation push                      

  Paiement Wave        Intégration API Wave    P0             Taux succès \> 95%
                       Sénégal/Guinée-Bissau                  

  Paiement MTN Mobile  Fallback si Orange      P1             Taux succès \> 90%
  Money                indisponible                           

  QR Code offline      Généré localement,      P0             Scan en \< 2 sec
                       valide 24h, fonctionne                 
                       sans internet                          

  Annulation &         Annulation jusqu\'à 30  P1             Process automatisé
  remboursement        min avant départ,                      
                       remboursement 80% sous                 
                       48h                                    

  Reçu numérique       PDF envoyé par          P2             Format lisible sans
                       SMS/WhatsApp après                     app
                       paiement                               
  ----------------------------------------------------------------------------------

## 4.3 Module Conducteur --- Gestion File

  -------------------------------------------------------------------------------
  **Fonctionnalité**   **Description**        **Priorité**   **Critère
                                                             d\'acceptance**
  -------------------- ---------------------- -------------- --------------------
  Mise à jour statut   Boutons simples : En   P0             Propagation \< 3 sec
                       file → Remplissage →                  
                       Complet → Parti                       

  Scan QR embarquement Caméra native pour     P0             Fonctionne hors
                       validation billets                    ligne
                       passagers                             

  Notifications        Alerte automatique à   P0             Push + SMS fallback
  paliers              80% / 90% / 100% de                   
                       remplissage                           

  Liste passagers      Manifeste embarquement P1             Export PDF possible
                       avec noms et sièges                   

  Historique revenus   Tableau récap          P2             Graphiques simples
                       journalier/hebdo des                  
                       courses                               
  -------------------------------------------------------------------------------

## 4.4 Module Hors Ligne --- Contrainte Critique

+-----------------------------------------------------------------------+
| **⚡ Exigence réseau Guinée-Bissau**                                  |
|                                                                       |
| La connectivité mobile est intermittente dans de nombreuses régions.  |
| Le mode offline est une fonctionnalité CORE, pas optionnelle.         |
|                                                                       |
| • Couverture 3G/4G : \~60% du territoire                              |
|                                                                       |
| • Zones rurales : EDGE ou aucun signal                                |
|                                                                       |
| • Exigence : l\'app doit fonctionner 100% offline pour les fonctions  |
| critiques                                                             |
+=======================================================================+
+-----------------------------------------------------------------------+

  -----------------------------------------------------------------------
  **Fonction**    **Mode Online** **Mode Offline**    **Sync**
  --------------- --------------- ------------------- -------------------
  Consultation    Temps réel      Cache local 24h     Auto à reconnexion
  gares                           (SQLite)            

  QR Code billet  Généré serveur  Généré localement   Validation différée
                                  (JWT signé)         

  Scan            Validation API  Validation locale   Log sync à
  embarquement                    (clé publique)      reconnexion

  Mise à jour     Push WebSocket  Queue locale        FIFO à reconnexion
  statut                                              
  -----------------------------------------------------------------------

# 5. Architecture Technique

## 5.1 Vue d\'Ensemble --- Décisions d\'Architecture

+-----------------------------------------------------------------------+
| **⚡ Choix architecturaux justifiés pour le contexte GB**             |
|                                                                       |
| • React Native 0.75+ : Une seule codebase iOS/Android, crucial pour   |
| réduire les coûts                                                     |
|                                                                       |
| • Spring Boot 3.3+ : Robustesse Java, écosystème mature, équipes      |
| backend disponibles                                                   |
|                                                                       |
| • PostgreSQL 16 : JSONB pour flexibilité schéma véhicules +           |
| performance requêtes géospatiales                                     |
|                                                                       |
| • Redis : Cache sessions + WebSocket pub/sub pour statuts temps réel  |
|                                                                       |
| • MinIO : Stockage objets auto-hébergeable (indépendance AWS si       |
| budget contraint)                                                     |
+=======================================================================+
+-----------------------------------------------------------------------+

## 5.2 Stack Frontend --- React Native

  ----------------------------------------------------------------------------------------------
  **Bibliothèque**                    **Version**   **Usage**       **Justification**
  ----------------------------------- ------------- --------------- ----------------------------
  React Native                        0.75+         Framework       Nouvelle architecture (JSI)
                                                    mobile          plus performante

  React Navigation                    6.x           Navigation      Standard, bien maintenu,
                                                                    deep linking

  Redux Toolkit + RTK Query           2.x           État + fetching RTK Query remplace Axios +
                                                                    cache automatique

  NativeWind                          4.x           Styling         Tailwind en mobile,
                                                                    cohérence avec design tokens

  React Native Paper                  5.x           Composants UI   Material Design 3,
                                                                    accessible

  react-native-maps                   1.x           Cartes offline  OpenStreetMap compatible +
                                                                    tuiles offline

  react-native-qrcode-svg             6.x           Génération QR   SVG = netteté sur tous les
                                                                    écrans

  react-native-vision-camera          4.x           Scanner QR      Remplace camera dépréciée,
                                                                    plus performant

  \@react-native-firebase/messaging   18.x          Notifications   FCM + APNs unifié
                                                    push            

  \@react-native-async-storage        1.x           Stockage local  Cache offline + préférences
                                                                    utilisateur

  react-native-mmkv                   2.x           Storage rapide  10x plus rapide
                                                                    qu\'AsyncStorage pour
                                                                    données fréquentes
  ----------------------------------------------------------------------------------------------

## 5.3 Stack Backend --- Spring Boot

  ------------------------------------------------------------------------
  **Composant**   **Technologie**        **Configuration recommandée**
  --------------- ---------------------- ---------------------------------
  Framework       Spring Boot 3.3+       Java 21 LTS (Virtual Threads
                                         activés)

  Base de données PostgreSQL 16+         PostGIS extension pour requêtes
                                         géospatiales

  ORM             Spring Data JPA +      Lazy loading, N+1 queries évités
                  Hibernate 6            

  Sécurité        Spring Security + JWT  Refresh tokens 7 jours, access 15
                  (jjwt 0.12)            min

  Cache           Spring Cache + Redis 7 Sessions, rate limiting, pub/sub
                                         WebSocket

  WebSocket       Spring WebSocket +     Mises à jour statut temps réel
                  STOMP                  

  API Docs        SpringDoc OpenAPI 3    Swagger UI auto-généré

  File Storage    MinIO (S3-compatible)  QR codes, reçus PDF, documents

  Monitoring      Micrometer +           Métriques custom exposées à
                  Prometheus             Grafana
  ------------------------------------------------------------------------

## 5.4 Schéma de Base de Données

  -----------------------------------------------------------------------------------
  **Table**         **Champs clés**       **Relations**   **Index**
  ----------------- --------------------- --------------- ---------------------------
  users             id, phone, role       1→N bookings,   idx_users_phone (UNIQUE)
                    (ENUM), name,         1→N vehicles    
                    created_at,                           
                    is_verified                           

  stations          id, name, city, lat,  1→N vehicles    idx_stations_geo (PostGIS)
                    lng, is_active                        

  vehicles          id, driver_id,        N→1 users, N→1  idx_vehicles_status,
                    station_id, capacity, stations, 1→N   idx_vehicles_station
                    current_load, status  bookings        
                    (ENUM), destination                   

  bookings          id, user_id,          N→1 users, N→1  idx_bookings_qr_token
                    vehicle_id,           vehicles, 1→1   (UNIQUE)
                    seat_number, status,  payments        
                    qr_token, expires_at                  

  payments          id, booking_id,       1→1 bookings    idx_payments_provider_ref
                    amount, currency                      
                    (XOF), provider,                      
                    provider_ref, status                  

  queue_positions   id, vehicle_id,       N→1 vehicles,   idx_queue_vehicle
                    user_id, position,    N→1 users       (composite)
                    joined_at                             
  -----------------------------------------------------------------------------------

## 5.5 API Endpoints --- Spécifications Complètes

  --------------------------------------------------------------------------------------
  **Méthode**   **Endpoint**                        **Auth**    **Description**
  ------------- ----------------------------------- ----------- ------------------------
  POST          /api/auth/register                  Public      Inscription par
                                                                téléphone + OTP SMS

  POST          /api/auth/login                     Public      Login → JWT + refresh
                                                                token

  POST          /api/auth/refresh                   Refresh     Renouvellement access
                                                    Token       token

  GET           /api/stations                       JWT         Liste gares avec statut
                                                                véhicules (paginé)

  GET           /api/stations/{id}/vehicles         JWT         Véhicules actifs d\'une
                                                                gare

  POST          /api/bookings                       JWT         Créer réservation +
                                                                initier paiement

  GET           /api/bookings/{id}                  JWT (owner) Détail réservation + QR
                                                                token

  DELETE        /api/bookings/{id}                  JWT (owner) Annulation avec calcul
                                                                remboursement

  PUT           /api/vehicles/{id}/status           JWT         Mise à jour statut
                                                    (driver)    véhicule

  POST          /api/vehicles/{id}/scan             JWT (agent) Valider QR code
                                                                embarquement

  POST          /api/payments/{bookingId}/confirm   JWT         Confirmation callback
                                                                paiement mobile

  GET           /api/admin/dashboard                JWT (admin) Métriques temps réel
                                                                agrégées

  WS            /ws/vehicles/{stationId}            JWT         WebSocket statuts temps
                                                                réel
  --------------------------------------------------------------------------------------

# 6. Design System & UX

## 6.1 Identité Visuelle

  -------------------------------------------------------------------------
  **Token**          **Valeur**        **Usage**
  ------------------ ----------------- ------------------------------------
  color-primary      #1E3A8A           Actions principales, navigation,
                                       titres

  color-success      #10B981           Confirmations, statut OK, validation

  color-warning      #F59E0B           Alertes, remplissage \> 80%,
                                       avertissements

  color-error        #EF4444           Erreurs, annulations, alertes
                                       critiques

  color-background   #F9FAFB           Fond principal de l\'app

  color-surface      #FFFFFF           Cards, modals, composants

  font-primary       Inter / Roboto    Tous les textes UI
                     (fallback)        

  radius-default     12px              Boutons, cards, inputs

  spacing-unit       8px               Base de la grille de spacing
  -------------------------------------------------------------------------

## 6.2 Composants Clés

  ----------------------------------------------------------------------------
  **Composant**        **Variants**            **Comportement Offline**
  -------------------- ----------------------- -------------------------------
  VehicleCard          En file / Remplissage / Badge \'Données locales\' si
                       Complet / Parti         cache

  SeatMap              8 / 15 / 20 places,     Grisé si non-disponible offline
                       horizontal/vertical     

  StatusBadge          4 couleurs selon statut Indicateur de fraîcheur des
                       ENUM                    données

  QRCodeView           Plein écran pour scan,  Généré localement si offline
                       mini pour liste         

  PaymentBottomSheet   Orange / Wave / MTN +   Message clair si échec réseau
                       état                    
                       loading/succès/erreur   

  OfflineBanner        Bandeau persistant en   Disparaît à la reconnexion auto
                       mode offline            
  ----------------------------------------------------------------------------

## 6.3 User Flows Critiques

### Flow 1 --- Recherche & Réservation (Passager)

1.  Accueil → Saisir destination (autocomplétion)

2.  Liste véhicules filtrés → Sélectionner un véhicule

3.  Voir plan des sièges → Choisir son siège

4.  Récapitulatif → Choisir moyen de paiement mobile

5.  Confirmation paiement → QR Code généré (stocké offline)

6.  Notification push à l\'approche du départ (80%, départ imminent)

### Flow 2 --- Conducteur (Gestion File)

7.  Login conducteur → Sélectionner sa gare + destination

8.  Voir liste passagers réservés + plan du véhicule

9.  Passer en mode \'Embarquement\' → Scanner QR codes

10. Notification auto à 80%/90% → Confirmer départ à 100%

11. Clôturer la course → Voir récapitulatif revenus

# 7. Sécurité & Conformité

  ------------------------------------------------------------------------
  **Exigence**       **Implémentation**            **Standard**
  ------------------ ----------------------------- -----------------------
  Authentification   JWT (15 min) + Refresh (7     OWASP Auth
                     jours) + OTP SMS pour         
                     inscription                   

  Chiffrement        AES-256 en transit (TLS 1.3)  ISO 27001
  données            et au repos (données PII)     

  Paiements          Tokenisation mobile money,    PCI-DSS lite
                     aucun numéro stocké en clair  

  QR Code            JWT signé avec expiration     Custom
  anti-fraude        24h + UUID unique par         
                     réservation                   

  Sessions           Auto-expiration 30 min        OWASP Session
                     d\'inactivité, révocation     
                     serveur                       

  Rate limiting      100 req/min par IP, 20        OWASP API
                     req/min par user (Redis)      

  Audit logs         Toutes transactions           RGPD / UEMOA
                     conservées 12 mois (RGPD      
                     adapté)                       

  Validation inputs  Hibernate Validator +         OWASP Input
                     sanitisation XSS côté serveur 
  ------------------------------------------------------------------------

# 8. Roadmap Développement --- Cursor AI Optimisé

## 8.1 Sprints de Développement

  -------------------------------------------------------------------------
  **Sprint**    **Durée**   **Livrables**                 **Critère de
                                                          sortie**
  ------------- ----------- ----------------------------- -----------------
  Sprint 1      3 semaines  • Projet React Native         Auth E2E
  Setup & Auth              initialisé (Expo Dev Client)  fonctionnel,
                            • Backend Spring Boot +       tests unitaires
                            Docker Compose local • Auth   \> 70%
                            JWT + OTP SMS (Africa\'s      
                            Talking API) • CRUD Users,    
                            Stations • Design system      
                            NativeWind configuré • CI/CD  
                            GitHub Actions basique        

  Sprint 2      3 semaines  • API Vehicles + Queue        Réservation E2E
  Recherche &               (WebSocket) • Écrans          sans paiement \<
  Réservation               recherche + filtres • SeatMap 3 min
                            component • Flow réservation  
                            complet • Génération QR Code  
                            (online + offline) • Mode     
                            offline basique               
                            (AsyncStorage + MMKV)         

  Sprint 3      2 semaines  • Intégration Orange Money    Paiement E2E en
  Paiement &                sandbox • Intégration Wave    sandbox, taux
  Conducteur                sandbox • Flow paiement       succès \> 95%
                            complet + gestion erreurs •   
                            Module conducteur (scan QR,   
                            mise à jour statut) •         
                            Notifications push (FCM)      

  Sprint 4      3 semaines  • Tests E2E Detox (parcours   Uptime 99%+ sur
  Tests &                   critiques) • Mode offline     staging, 0 bug P0
  Déploiement               complet + sync • Déploiement  
                            Docker + Kubernetes (staging) 
                            • Soumission Play Store (beta 
                            fermée) • Monitoring Sentry + 
                            Grafana • Tests charge        
                            (JMeter : 500 users           
                            concurrent)                   
  -------------------------------------------------------------------------

## 8.2 Configuration Cursor AI Recommandée

+-----------------------------------------------------------------------+
| **⚡ Optimisation Cursor AI pour ce projet**                          |
|                                                                       |
| • .cursorrules : Définir les conventions du projet (naming, patterns  |
| Redux, conventions API)                                               |
|                                                                       |
| • Cursor Composer : Générer les composants React Native à partir des  |
| specs de design system                                                |
|                                                                       |
| • Chat intégré : Debugging Spring Boot + résolution erreurs           |
| PostgreSQL/Hibernate                                                  |
|                                                                       |
| • \@Codebase : Permettre à Cursor de comprendre l\'architecture       |
| complète pour suggestions cohérentes                                  |
|                                                                       |
| • Terminal AI : Génération scripts DevOps (Docker, migrations DB,     |
| scripts CI/CD)                                                        |
|                                                                       |
| • Règle : Toujours fournir le contexte du design system Tailwind dans |
| les prompts de composants                                             |
+=======================================================================+
+-----------------------------------------------------------------------+

## 8.3 Fichier .cursorrules Recommandé

+-----------------------------------------------------------------------+
| **\# Gare Mobile GB --- Cursor Rules**                                |
|                                                                       |
| \## Stack: React Native 0.75 + Spring Boot 3.3 + PostgreSQL 16        |
|                                                                       |
| \## Style: NativeWind Tailwind + Redux Toolkit + RTK Query            |
|                                                                       |
| \## Toujours : TypeScript strict, tests Vitest, offline-first         |
|                                                                       |
| \## Jamais : useEffect pour fetching (utiliser RTK Query)             |
|                                                                       |
| \## Nommage : PascalCase composants, camelCase fonctions, UPPER_SNAKE |
| constantes                                                            |
+=======================================================================+
+-----------------------------------------------------------------------+

# 9. Intégrations Paiement --- Détail Technique

  -----------------------------------------------------------------------------
  **Provider**   **API**            **Couverture   **Fallback**   **Sandbox**
                                    GB**                          
  -------------- ------------------ -------------- -------------- -------------
  Orange Money   Orange Money API   Principale     Wave           ✅ Disponible
                 West Africa        (60%+ users)                  

  Wave           Wave API           Secondaire     MTN            ✅ Disponible
                 Sénégal/GB         (30% users)                   

  MTN Mobile     MTN MoMo API       Tertiaire      ---            ✅ Disponible
  Money                                                           

  Stripe         Stripe Payment     Diaspora /     ---            ✅ Disponible
                 Intents            cartes intl                   
  -----------------------------------------------------------------------------

+-----------------------------------------------------------------------+
| **⚡ Logique de fallback paiement**                                   |
|                                                                       |
| 1\. Tentative paiement Orange Money → timeout 30 sec                  |
|                                                                       |
| 2\. Si échec : proposer Wave → timeout 30 sec                         |
|                                                                       |
| 3\. Si échec : proposer MTN ou retenter plus tard                     |
|                                                                       |
| 4\. Réservation conservée 15 min en statut PENDING pendant les        |
| tentatives                                                            |
|                                                                       |
| 5\. Log tous les échecs pour analyse des taux de conversion par       |
| provider                                                              |
+=======================================================================+
+-----------------------------------------------------------------------+

# 10. Infrastructure & Déploiement

  -------------------------------------------------------------------------
  **Composant**      **Technologie**        **Configuration**
  ------------------ ---------------------- -------------------------------
  Containerisation   Docker + Docker        Image Alpine, multi-stage
                     Compose                builds

  Orchestration      Kubernetes (K8s)       2 replicas min, HPA sur CPU 70%

  Cloud primaire     AWS (eu-west-1) ou OVH OVH recommandé : latence
                     Gravelines             Afrique + coût −40%

  CDN                CloudFlare             Tuiles carte offline + assets
                                            statiques

  CI/CD              GitHub Actions         Build → Test → Staging → Prod
                                            (approval manuel)

  Monitoring         Sentry (errors) +      Alertes PagerDuty si uptime \<
                     Grafana (métriques)    99%

  Backup DB          pg_dump + S3           Rétention 30 jours, test
                     automatisé             restore mensuel
  -------------------------------------------------------------------------

# 11. Risques & Mitigations

  ------------------------------------------------------------------------------------
  **Risque**               **Probabilité**   **Impact**   **Mitigation**
  ------------------------ ----------------- ------------ ----------------------------
  Connectivité réseau      Haute             Critique     Mode offline-first complet,
  insuffisante                                            sync différée

  Adoption conducteurs     Haute             Critique     Formation terrain, support
  lente                                                   WhatsApp, incentives

  Échec intégration        Moyenne           Élevé        Sandbox dès S1, contrats API
  paiement mobile                                         signés avant code

  Fraude QR Code           Faible            Élevé        JWT signé + expiration 24h +
                                                          UUID unique

  Scalabilité à 10k users  Faible            Moyen        Tests charge JMeter dès S4,
                                                          HPA Kubernetes

  Barrière                 Haute             Moyen        UI iconographique, flux \< 4
  langue/alphabétisation                                  étapes, audio guide v2
  ------------------------------------------------------------------------------------

# 12. Annexes

## 12.1 Glossaire

  -----------------------------------------------------------------------
  **Terme**              **Définition**
  ---------------------- ------------------------------------------------
  Gare routière          Terminus de départ des taxis-brousse et minibus
                         interurbains

  Remplissage complet    Système où le véhicule ne part que quand toutes
                         les places sont occupées

  File d\'attente        Ordre d\'inscription des passagers pour un
                         véhicule donné

  XOF                    Franc CFA Ouest-Africain, monnaie de la
                         Guinée-Bissau

  QR Code offline        Code QR généré localement avec signature
                         cryptographique, valide sans internet

  RTK Query              Outil de fetching et cache intégré à Redux
                         Toolkit
  -----------------------------------------------------------------------

## 12.2 Contacts & Propriété

  -----------------------------------------------------------------------
  **Rôle**                **Responsabilité**
  ----------------------- -----------------------------------------------
  Product Owner           Validation des priorités, acceptance criteria

  Tech Lead Frontend      Architecture React Native, revues de code

  Tech Lead Backend       Architecture Spring Boot, sécurité API

  DevOps                  Infrastructure Docker/K8s, CI/CD, monitoring

  UX Designer             Design system, tests utilisateurs terrain
  -----------------------------------------------------------------------

*Gare Mobile GB PRD v2.0 --- Prêt pour développement Cursor AI --- Mars
2026*
