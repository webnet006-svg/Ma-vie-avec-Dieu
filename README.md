# Ma Vie avec Dieu

Application Android chrétienne évangélique et protestante pour la méditation
biblique quotidienne, le journal de prière, les objectifs spirituels et
l'édification dans la foi.

## Stack

- Kotlin, Jetpack Compose, Material 3
- Room (données locales) + DataStore (préférences)
- Firebase Auth (Google) + Cloud Firestore
- Assistant « Eli » : Gemini API, avec moteur biblique hors-ligne de secours

## Compiler

Prérequis : JDK 21, Android SDK 36, Gradle 9.3.1.

```sh
cp .env.example .env       # renseigner GEMINI_API_KEY
gradle :app:assembleDebug
```

La CI GitHub Actions (`.github/workflows/android-build.yml`) compile l'APK de
debug et exécute les tests hôtes à chaque push.

Dans Android Studio, ouvrir le dossier du projet puis lancer la configuration
`app`.

## Bible

Le texte biblique est la **Louis Segond 1910**, dans le domaine public. Il est
embarqué dans `app/src/main/assets/bible/` (un fichier JSON par livre) et lu
hors-ligne par `BibleRepository`. Voir `THIRD_PARTY_NOTICES.md`.

## Sécurité

- `GEMINI_API_KEY` est compilée dans l'APK via `BuildConfig`. Une clé côté
  client est **extractible** : pour une publication en production, passer par
  un proxy serveur (Cloud Function) et activer Firebase App Check, puis retirer
  la clé du client.
- La sauvegarde Android est désactivée (`allowBackup=false`) car les données
  contiennent des prières et des témoignages personnels.
- La déconnexion efface les données locales de l'utilisateur ; les données
  cloud sont restaurées à la reconnexion.
