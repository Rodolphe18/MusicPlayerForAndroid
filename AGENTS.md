# AGENTS.md

Lecteur de musique Android **hors ligne** : il lit les fichiers audio déjà présents
sur l'appareil (`MediaStore`), sans compte ni réseau. UI 100 % Jetpack Compose,
lecture via Media3/ExoPlayer dans un service de premier plan.

Publié sur le Play Store sous `com.francotte.musicplayer`.

## Chaîne d'outils

| | |
|---|---|
| AGP | 8.13.2 · Gradle 8.13 |
| Kotlin | 2.0.20 · KSP 2.0.20-1.0.25 |
| Compose BOM | 2026.03.01 · Material 3 |
| SDK | `compileSdk`/`targetSdk` 36 · `minSdk` 23 |
| JVM cible | 11 (`compileOptions` / `kotlinOptions`) |

**Un build en ligne de commande exige un JDK 17** (contrainte d'AGP), alors que le
code compile vers un bytecode 11 — les deux ne sont pas contradictoires. Le JDK 17
Adoptium est déjà exposé par la variable d'environnement `JAVA_HOME` au niveau
machine. En cas d'erreur « Android Gradle plugin requires Java 17 », vérifier qu'une
variable `JAVA_HOME` utilisateur ou un shell ouvert de longue date ne masque pas
cette valeur. `.gradle/config.properties` ne concerne qu'Android Studio, la CLI
l'ignore.

## Commandes

```bash
./gradlew assembleDebug        # build de développement
./gradlew test                 # tests unitaires JVM
./gradlew bundleRelease        # AAB signé -> app/build/outputs/bundle/release/
jarsigner -verify app/build/outputs/bundle/release/app-release.aab
```

## Architecture

MVVM à flux de données unidirectionnel, activité unique.

```
com.francotte.contentproviderformusic
├── ads/         # bannière AdMob
├── consent/     # consentement UMP (RGPD)
├── data/        # Proto DataStore, dépôt UserData, sérialiseur
├── di/          # modules Hilt
├── domain/      # cas d'usage
├── model/       # Song, Playlist
├── repository/  # SongsFetcherRepository (titres scannés, en mémoire)
├── service/     # MusicService (MediaSessionService)
├── ui/          # composable/ favorites/ library/ playlists/ settings/
│                # navigation/ state/ theme/
└── utils/       # MediaManager, PermissionManager
```

`MainViewModel` expose l'état en `StateFlow` (`songs`, `favoritesSongs`,
`currentPlayingSong`, indicateurs de lecture). La liste affichée est dérivée en
combinant les titres scannés et les favoris persistés, si bien qu'un basculement de
favori recompose l'UI tout seul. Les commandes de lecture passent par un
`MediaController` relié à `MusicService` — **ne jamais piloter ExoPlayer
directement depuis l'UI**, la session média perdrait la main.

La liste depuis laquelle un titre est lancé devient la file de lecture : « suivant »
et « précédent » restent dans ce contexte.

## Pièges à connaître avant de toucher au code

**`namespace` ≠ `applicationId`.** Le `namespace` est resté
`com.francotte.contentproviderformusic` : c'est la racine des packages Kotlin, la
renommer imposerait un refactor complet sans bénéfice. Seul l'`applicationId`
(`com.francotte.musicplayer`) est public — et il est **définitivement figé** depuis
la publication. Ne pas « harmoniser » les deux.

**Le bloc `androidComponents` d'`app/build.gradle.kts` n'est pas décoratif.** En
module unique, KSP ne voit pas les classes proto générées et Hilt échoue sur
`error.NonExistentClass` autour de `UserPreferences`. Le bloc ajoute explicitement
les dossiers proto générés aux sources de la tâche KSP. Le supprimer casse le build.

**`Aurora.Purple` n'est pas violette.** C'est le corail `#E85D54`, l'accent
principal (boutons, onglet actif, pastilles). Le nom est historique et référencé
partout ; le renommer est un refactor à part entière, pas un effet de bord. Palette
complète dans `ui/theme/Color.kt` (« Tropical Fresh » : base bleu nuit, turquoise,
corail).

**La signature release échoue en silence.** `keystore.properties` et
`upload-keystore.jks` sont volontairement hors dépôt (voir
`keystore.properties.template`). S'ils manquent, `bundleRelease` produit un bundle
**non signé** sans la moindre erreur, que la Play Console refusera. Toujours
contrôler avec `jarsigner -verify` : attendu `jar verified` — l'avertissement PKIX
qui suit est normal pour un certificat auto-signé.

**`versionCode` doit être incrémenté à chaque envoi** sur le Play Store, sinon
l'upload est rejeté.

## Internationalisation

14 langues déclarées dans `res/xml/locales_config.xml`. `values/strings.xml` est la
source (anglais), accompagnée de 13 dossiers `values-<locale>/`. **Toute nouvelle
chaîne doit être ajoutée dans les 14 fichiers** ; une clé absente d'une locale fait
retomber l'app sur l'anglais au milieu d'un écran traduit.

Le français **vouvoie** l'utilisateur (« Appuyez sur le cœur d'un titre… »). Les
textes du Play Store suivent la même règle.

## Conventions

- Commentaires en **français**, identifiants et noms de fichiers en **anglais**.
- Les commentaires expliquent le *pourquoi* — une contrainte, un piège, un choix
  non évident. Pas de paraphrase du code.
- Icônes : Material Symbols (Rounded) en vector drawables, `res/drawable/ic_*.xml`.
- Polices embarquées dans `res/font/` (Space Grotesk, Manrope, Poppins).
- Listes immuables via `kotlinx.collections.immutable` dans l'état exposé.

## Tests

`app/src/test/` — `PlaylistMappersTest`, `PlaylistResolveTest` couvrent le mapping
et la résolution des playlists. `ExampleUnitTest` et `ExampleInstrumentedTest` sont
les gabarits d'origine, sans valeur.

## Firebase Crashlytics

Branché via la BOM Firebase (`libs.firebase.bom`), sans Analytics — Crashlytics
fonctionne seul ; ajouter Analytics élargirait la collecte de données et les
obligations de déclaration.

**`app/google-services.json` n'est pas versionné** (comme `keystore.properties`).
Le plugin `google-services` fait normalement échouer le build quand ce fichier
manque, ce qui rendrait tout clone non buildable : les deux plugins Firebase sont
donc appliqués **conditionnellement** dans `app/build.gradle.kts`. Sans le fichier,
le build passe, le SDK est embarqué mais inerte, et Gradle affiche à la
configuration :

```
google-services.json absent de app/ : Crashlytics est DESACTIVE pour ce build.
```

**Toujours vérifier l'absence de cet avertissement avant un `bundleRelease`**,
sinon la release part sans remontée de plantages.

Crashlytics s'initialise tout seul via un `ContentProvider`, avant `onCreate()` —
contrairement au SDK Ads, volontairement retardé jusqu'au consentement. Pour
aligner Crashlytics sur ce comportement, il faudrait `firebase_crashlytics_collection_enabled`
à `false` dans le manifeste, puis `setCrashlyticsCollectionEnabled(true)` une fois
le consentement obtenu — au prix des plantages du tout premier démarrage, qui ne
remonteraient plus.

`isMinifyEnabled = false` : les traces sont déjà lisibles, aucun mapping à envoyer.
Si R8 est activé un jour, l'upload du mapping est automatique en release.

Conséquences hors code : la **Sécurité des données** de la Play Console doit
déclarer les rapports de plantage, et la politique de confidentialité mentionner
Firebase Crashlytics.

## Publication

`playstore/` regroupe les visuels du Store et leurs scripts de génération
(`generate_assets.py`, `generate_tablet_shots.py`, dépendance : Pillow).
Contraintes Google : icône 512×512, image mise en avant 1024×500, captures en 16:9
ou 9:16 strict. **Aucune capture ne doit montrer la bannière AdMob** — c'est un
motif de rejet ; les scripts rognent cette zone.

L'app diffuse des publicités : la case « Cette application contient des annonces »
doit rester cochée dans la Play Console, et le consentement UMP est obligatoire.

## Git

- **Ne jamais ajouter de trailer `Co-Authored-By`** aux messages de commit.
- **Ne pas committer ni pousser sans demande explicite.**
