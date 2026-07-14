# Feature Playlists — Design

Date : 2026-07-14
Statut : validé (en attente de relecture avant plan d'implémentation)

## Objectif

Ajouter un 3ᵉ onglet « Playlists » dans la bottom bar permettant à l'utilisateur
de créer des playlists, d'y ajouter des titres, de les retrouver, de les lire, et
de les supprimer. Les playlists sont persistées via Proto DataStore.

## Décisions clés (arbitrages validés)

- **Identité des titres dans une playlist** : `title: String`, exactement comme le
  système de favoris existant. On n'ajoute **pas** d'`id` au modèle `Song` ; on
  n'utilise **pas** le `MediaStore._ID`. Proto : `repeated string song_titles`.
- **Id de playlist** : `int64` = `System.currentTimeMillis()` à la création.
- **Actions v1 incluses** : lecture depuis la playlist, retirer un titre d'une
  playlist, supprimer une playlist.
- **Écran d'ajout de titres** : un titre déjà présent affiche une **coche
  désactivée** (au lieu du `+`). Le `Set` empêche les doublons.
- **Création** : bouton « créer » **désactivé tant que le nom est vide** ;
  description optionnelle.
- **ViewModel** : on **étend `MainViewModel`** (cohérent avec les favoris qui y
  vivent déjà ; le détail réutilise la lecture existante). On note que
  `MainViewModel` grossit et pourrait être scindé ultérieurement — hors périmètre.

## 1. Persistance (Proto DataStore)

`app/src/main/proto/user_preferences.proto` :

```proto
message UserPreferences {
  map<string, bool> favorite_titles = 1;
  repeated Playlist playlists = 2;
}

message Playlist {
  int64 id = 1;
  string title = 2;
  string description = 3;
  repeated string song_titles = 4;   // clé = Song.title, comme les favoris
}
```

⚠️ Le bloc `androidComponents { onVariants … }` de `app/build.gradle.kts`
(workaround KSP/proto — voir mémoire `ksp-protobuf-source-workaround`) doit rester
en place : il rend visibles les classes proto générées (`Playlist`, etc.) à KSP.
Ne pas le supprimer.

## 2. Modèle domaine

Distinct des classes proto générées.

```kotlin
data class UserData(
    val favoritesSongs: Set<String>,
    val playlists: List<Playlist>,
)

data class Playlist(
    val id: Long,
    val title: String,
    val description: String,
    val songTitles: Set<String>,
)
```

`Song` : **inchangé** (pas d'ajout d'`id`).

## 3. Repository

`UserDataRepository` (interface) + `UserPreferencesDataSource` (impl) :

```kotlin
// nouveau, en plus de setFavoritesSongs existant
suspend fun createPlaylist(title: String, description: String): Long
suspend fun deletePlaylist(id: Long)
suspend fun addSongToPlaylist(playlistId: Long, songTitle: String)
suspend fun removeSongFromPlaylist(playlistId: Long, songTitle: String)
```

- Toutes implémentées via `userPreferences.updateData { it.copy { … } }`, avec
  mapping proto ↔ domaine.
- `createPlaylist` : `id = System.currentTimeMillis()`, ajoute un `Playlist` proto
  à la liste et renvoie l'id (nécessaire pour naviguer vers le détail).
- `addSongToPlaylist` / `removeSongFromPlaylist` : retrouvent la playlist par id,
  modifient sa liste `song_titles`, réécrivent la liste des playlists.
- Le flow `userData` mappe `playlistsList` → `List<Playlist>` domaine, en plus du
  mapping favoris existant.

## 4. Domaine + ViewModel

- **`PlaylistsUseCase`** (calqué sur `FavoritesUseCase`) :
  - expose les playlists (`Flow<List<Playlist>>`) ;
  - résout les `songTitles` d'une playlist → `List<Song>` en matchant
    `Song.title` contre `SongsFetcherRepository.songs`.
- **`MainViewModel`** gagne :
  - `val playlists: StateFlow<List<Playlist>>` ;
  - `fun songsForPlaylist(id: Long): List<Song>` (ou un flow dérivé par id) pour
    l'écran détail ;
  - `fun createPlaylist(title, description): renvoie/expose l'id créé` ;
  - `fun deletePlaylist(id)` ;
  - `fun addSongToPlaylist(id, songTitle)` ;
  - `fun removeSongFromPlaylist(id, songTitle)`.
  - Chaque action délègue au repository dans `viewModelScope.launch`.

## 5. Navigation & écrans

On **active le scaffolding déjà présent mais commenté** :
`TopLevelDestination.PLAYLISTS`, la liste `topLevelDestinations`,
`currentTopLevelDestination`, `navigateToTopLevelDestination`, et
l'enregistrement dans `MusicNavHost`.

Routes :

| Route                       | Écran                                             |
| --------------------------- | ------------------------------------------------- |
| `playlists` (top-level)     | Liste des playlists **ou** état vide              |
| `playlist_create`           | Formulaire (nom, description, « créer »)          |
| `playlist_detail/{id}`      | Détail (card glass + titres ajoutés)              |
| `playlist_add_songs/{id}`   | Liste de tous les titres avec `+` / coche         |

Flux nominal :

1. Onglet Playlists vide → bouton « Créer une playlist » → `playlist_create`.
2. Formulaire → « créer » → `createPlaylist` renvoie l'id → navigation vers
   `playlist_detail/{id}` en **popant le formulaire** (retour = liste).
3. Détail → bouton `+ ajouter des titres` → `playlist_add_songs/{id}`.
4. Ajout → clic `+` = `addSongToPlaylist` → retour → les titres apparaissent sous
   la rangée « ajouter des titres ».

## 6. Écrans en détail

### État vide (onglet Playlists)
- Icône playlist **multicolore**, rendue avec un `Brush` dégradé
  (Purple → Teal → Cyan de `Aurora`), **pas** un simple tint monochrome.
- Texte d'invitation + bouton « Créer une playlist ».
- Composable dédié `PlaylistsEmptyState` (l'`EmptyState` générique ne fait qu'un
  tint monochrome, insuffisant ici).

### Liste peuplée
- Une card par playlist : nom, description, nombre de titres.
- Tap → `playlist_detail/{id}`.
- Suppression : icône poubelle ou appui long → `deletePlaylist(id)`.

### Formulaire de création (`playlist_create`)
- Champ « nom de la playlist » (obligatoire).
- Champ « description » (optionnel).
- Bouton « créer » **désactivé tant que le nom est vide**.

### Détail (`playlist_detail/{id}`)
- En haut : **card effet glass** (`Aurora.BarBrush` + bordure blanche alpha, même
  recette que la sheet du player) avec **nom + description centrés**.
- En dessous : **bouton circulaire glass** contenant une icône `+`, suivi du texte
  « ajouter des titres ».
- Puis la liste des titres ajoutés (réutilise `SongItem`) :
  - clic sur un titre = `playFromList(playlistSongs, index)` (la playlist devient
    la file de lecture, comme favoris/biblio) ;
  - action « retirer » (icône `-` ou swipe) = `removeSongFromPlaylist`.

### Ajout de titres (`playlist_add_songs/{id}`)
- Liste de tous les titres, présentation similaire à l'onglet principal.
- Chaque rangée a un `+` à droite :
  - clic = `addSongToPlaylist(id, song.title)` ;
  - si `song.title` est déjà dans la playlist → affiche une **coche désactivée**
    au lieu du `+`.

## Style « glass » (rappel)

Recette existante réutilisée (cf. `FloatingPlayerBar` / `Color.kt`) :
`background(Aurora.BarBrush)` + `border(1.dp, Color.White.copy(alpha = 0.12f), shape)`.
Extraire idéalement un petit modificateur/composant réutilisable
(`GlassCard` / `Modifier.glass()`) pour la card du détail et le bouton circulaire.

## Hors périmètre (v1)

- Renommer / éditer une playlist après création.
- Réordonner les titres dans une playlist.
- Scinder `MainViewModel` en plusieurs ViewModels.
