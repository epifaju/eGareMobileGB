# Carte Google Maps (Android)

## Erreur `API key not found`

Sur **Android**, `react-native-maps` utilise le **Maps SDK for Android**. Il faut une clé API Google avec ce SDK activé.

## Configuration locale

1. [Google Cloud Console](https://console.cloud.google.com/) → APIs & Services → **Maps SDK for Android** (activer).
2. Créer une **clé API** (restrictions : package `com.garemobilegb.app` + SHA-1 du debug/release si besoin).
3. Dans `mobile/.env` :

```env
GOOGLE_MAPS_ANDROID_API_KEY=votre_cle_ici
```

4. **Reconstruire** le natif (la clé est injectée au build) :

```bash
cd mobile
npx expo run:android
```

Sans clé, l’app affiche une **liste des gares** à la place de la carte (pas de crash).

## iOS

Optionnel : `GOOGLE_MAPS_IOS_API_KEY` ; sinon la carte Apple peut suffire selon la config `react-native-maps`.
