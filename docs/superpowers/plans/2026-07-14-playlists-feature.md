# Feature Playlists — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ajouter un 3ᵉ onglet « Playlists » permettant de créer, remplir, retrouver, lire et supprimer des playlists, persistées via Proto DataStore.

**Architecture:** Bottom-up. On étend la chaîne existante Proto → `UserDataRepository` → `MainViewModel` (comme les favoris), puis on active le scaffolding de navigation déjà commenté et on ajoute 4 écrans Compose. La logique pure (mapping proto↔domaine, transformations de listes, résolution titre→Song) est isolée dans des fonctions testées en TDD (JUnit4). Le câblage DataStore/ViewModel/Compose est vérifié par build + exécution de l'app.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), Hilt, Proto DataStore (protobuf-lite), Navigation Compose, JUnit4.

**Référence spec :** `docs/superpowers/specs/2026-07-14-playlists-feature-design.md`

---

## Conventions de build & test

Le build CLI exige **JDK 17+** (AGP 8.7.2). Le JDK par défaut du terminal est 11 ; on force JDK 19 (cf. mémoire `ksp-protobuf-source-workaround`). Toutes les commandes ci-dessous s'exécutent depuis la racine du repo **via l'outil Bash (git bash)** :

- **Tests unitaires (une classe)** :
  ```bash
  JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:testDebugUnitTest --tests "com.francotte.contentproviderformusic.NomDuTest"
  ```
- **Build complet debug** :
  ```bash
  JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:assembleDebug
  ```

> ⚠️ `testDebugUnitTest` compile **tout** le source set `main` du module. Chaque tâche doit donc laisser `main` compilable. Les tâches sont ordonnées pour respecter cet invariant. Alternative : Android Studio (JBR 17 intégré) pour lancer tests/app directement.

## Carte des fichiers

**Créés :**
- `app/src/main/java/.../model/Playlist.kt` — data class domaine.
- `app/src/main/java/.../data/PlaylistMappers.kt` — mapping proto↔domaine + transformations pures de listes.
- `app/src/test/java/.../PlaylistMappersTest.kt` — tests (JUnit4).
- `app/src/main/java/.../domain/PlaylistResolve.kt` — helper pur `resolveByTitle`.
- `app/src/test/java/.../PlaylistResolveTest.kt` — tests (JUnit4).
- `app/src/main/java/.../domain/PlaylistsUseCase.kt` — expose le flow des playlists.
- `app/src/main/java/.../ui/composable/GlassCard.kt` — composant glass réutilisable.
- `app/src/main/java/.../ui/playlists/PlaylistSongRow.kt` — rangée titre + action (slot).
- `app/src/main/java/.../ui/playlists/PlaylistsScreen.kt` — onglet (vide/liste/FAB/mode suppression).
- `app/src/main/java/.../ui/playlists/PlaylistCreateScreen.kt` — formulaire.
- `app/src/main/java/.../ui/playlists/PlaylistDetailScreen.kt` — détail.
- `app/src/main/java/.../ui/playlists/PlaylistAddSongsScreen.kt` — ajout de titres.
- `app/src/main/res/drawable/ic_add.xml`, `ic_check.xml`, `ic_delete.xml`, `ic_arrow_back.xml`.

**Modifiés :**
- `app/src/main/proto/user_preferences.proto`
- `app/src/main/java/.../data/UserData.kt`
- `app/src/main/java/.../data/UserDataRepository.kt`
- `app/src/main/java/.../data/UserPreferencesDataSource.kt`
- `app/src/main/java/.../ui/MainViewModel.kt`
- `app/src/main/java/.../ui/composable/TopLevelDestination.kt`
- `app/src/main/java/.../ui/state/MusicApp.kt`
- `app/src/main/java/.../ui/playlists/PlayListsRoute.kt` (routes + graph de navigation)
- `app/src/main/java/.../ui/navigation/MusicNavHost.kt`

> Raccourci de chemin : `.../` = `app/src/main/java/com/francotte/contentproviderformusic/` (ou `app/src/test/java/com/francotte/contentproviderformusic/` pour les tests).

---

## Task 1: Schéma proto + modèle domaine

**Files:**
- Modify: `app/src/main/proto/user_preferences.proto`
- Create: `.../model/Playlist.kt`
- Modify: `.../data/UserData.kt`

- [ ] **Step 1: Étendre le proto**

Remplacer le contenu de `app/src/main/proto/user_preferences.proto` par :

```proto
syntax = "proto3";

option java_package = "com.francotte.contentproviderformusic";
option java_multiple_files = true;

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

- [ ] **Step 2: Créer la data class domaine**

Créer `.../model/Playlist.kt` :

```kotlin
package com.francotte.contentproviderformusic.model

data class Playlist(
    val id: Long,
    val title: String,
    val description: String,
    val songTitles: Set<String>,
)
```

- [ ] **Step 3: Étendre UserData**

Remplacer le contenu de `.../data/UserData.kt` par :

```kotlin
package com.francotte.contentproviderformusic.data

import com.francotte.contentproviderformusic.model.Playlist

data class UserData(
    val favoritesSongs: Set<String>,
    val playlists: List<Playlist>,
)
```

- [ ] **Step 4: Générer le proto et vérifier la compilation**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:generateDebugProto
```
Expected: `BUILD SUCCESSFUL`. Le fichier `app/build/generated/source/proto/debug/java/com/francotte/contentproviderformusic/Playlist.java` doit exister.

> Note : `UserData` ne compile pas encore côté app car `UserPreferencesDataSource` ne fournit plus le nouveau champ — c'est corrigé en Task 3. On ne build pas l'app ici.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/proto/user_preferences.proto app/src/main/java/com/francotte/contentproviderformusic/model/Playlist.kt app/src/main/java/com/francotte/contentproviderformusic/data/UserData.kt
git commit -m "feat(playlists): proto schema + domain model"
```

---

## Task 2: Mappers proto↔domaine + transformations pures (TDD)

**Files:**
- Create: `.../data/PlaylistMappers.kt`
- Test: `.../PlaylistMappersTest.kt` (dans `app/src/test/java/...`)

- [ ] **Step 1: Écrire le test qui échoue**

Créer `app/src/test/java/com/francotte/contentproviderformusic/PlaylistMappersTest.kt` :

```kotlin
package com.francotte.contentproviderformusic

import com.francotte.contentproviderformusic.data.toDomainPlaylists
import com.francotte.contentproviderformusic.data.toProto
import com.francotte.contentproviderformusic.data.withNewPlaylist
import com.francotte.contentproviderformusic.data.withSongAdded
import com.francotte.contentproviderformusic.data.withSongRemoved
import com.francotte.contentproviderformusic.data.withoutPlaylist
import com.francotte.contentproviderformusic.model.Playlist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistMappersTest {

    @Test
    fun withNewPlaylist_appends_empty_playlist() {
        val result = emptyList<Playlist>().withNewPlaylist(1L, "Road", "trip")
        assertEquals(1, result.size)
        assertEquals(Playlist(1L, "Road", "trip", emptySet()), result[0])
    }

    @Test
    fun withSongAdded_adds_title_to_matching_playlist_only() {
        val start = listOf(
            Playlist(1L, "A", "", emptySet()),
            Playlist(2L, "B", "", emptySet()),
        )
        val result = start.withSongAdded(1L, "Song1")
        assertEquals(setOf("Song1"), result.first { it.id == 1L }.songTitles)
        assertTrue(result.first { it.id == 2L }.songTitles.isEmpty())
    }

    @Test
    fun withSongAdded_is_idempotent() {
        val start = listOf(Playlist(1L, "A", "", setOf("Song1")))
        val result = start.withSongAdded(1L, "Song1")
        assertEquals(setOf("Song1"), result[0].songTitles)
    }

    @Test
    fun withSongRemoved_removes_title() {
        val start = listOf(Playlist(1L, "A", "", setOf("Song1", "Song2")))
        val result = start.withSongRemoved(1L, "Song1")
        assertEquals(setOf("Song2"), result[0].songTitles)
    }

    @Test
    fun withoutPlaylist_drops_by_id() {
        val start = listOf(
            Playlist(1L, "A", "", emptySet()),
            Playlist(2L, "B", "", emptySet()),
        )
        val result = start.withoutPlaylist(1L)
        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test
    fun proto_domain_roundtrip_preserves_data_and_order() {
        val domain = listOf(
            Playlist(10L, "A", "desc", linkedSetOf("s1", "s2")),
            Playlist(20L, "B", "", emptySet()),
        )
        val proto = UserPreferences.newBuilder()
            .addAllPlaylists(domain.map { it.toProto() })
            .build()
        assertEquals(domain, proto.toDomainPlaylists())
    }
}
```

- [ ] **Step 2: Lancer le test pour vérifier l'échec**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:testDebugUnitTest --tests "com.francotte.contentproviderformusic.PlaylistMappersTest"
```
Expected: FAIL — erreur de compilation « unresolved reference: toDomainPlaylists / toProto / withNewPlaylist … ».

- [ ] **Step 3: Écrire l'implémentation minimale**

Créer `.../data/PlaylistMappers.kt` :

```kotlin
package com.francotte.contentproviderformusic.data

import com.francotte.contentproviderformusic.UserPreferences
import com.francotte.contentproviderformusic.model.Playlist
import com.francotte.contentproviderformusic.Playlist as ProtoPlaylist

/** proto -> domaine */
fun UserPreferences.toDomainPlaylists(): List<Playlist> =
    playlistsList.map { proto ->
        Playlist(
            id = proto.id,
            title = proto.title,
            description = proto.description,
            songTitles = proto.songTitlesList.toSet(),
        )
    }

/** domaine -> proto */
fun Playlist.toProto(): ProtoPlaylist =
    ProtoPlaylist.newBuilder()
        .setId(id)
        .setTitle(title)
        .setDescription(description)
        .addAllSongTitles(songTitles)
        .build()

/** Transformations pures de la liste de playlists (domaine). */
fun List<Playlist>.withNewPlaylist(id: Long, title: String, description: String): List<Playlist> =
    this + Playlist(id = id, title = title, description = description, songTitles = emptySet())

fun List<Playlist>.withoutPlaylist(id: Long): List<Playlist> =
    filterNot { it.id == id }

fun List<Playlist>.withSongAdded(playlistId: Long, songTitle: String): List<Playlist> =
    map { if (it.id == playlistId) it.copy(songTitles = it.songTitles + songTitle) else it }

fun List<Playlist>.withSongRemoved(playlistId: Long, songTitle: String): List<Playlist> =
    map { if (it.id == playlistId) it.copy(songTitles = it.songTitles - songTitle) else it }
```

- [ ] **Step 4: Lancer le test pour vérifier le succès**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:testDebugUnitTest --tests "com.francotte.contentproviderformusic.PlaylistMappersTest"
```
Expected: `BUILD SUCCESSFUL`, 6 tests passés.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/data/PlaylistMappers.kt app/src/test/java/com/francotte/contentproviderformusic/PlaylistMappersTest.kt
git commit -m "feat(playlists): proto<->domain mappers + pure list transforms (TDD)"
```

---

## Task 3: Repository (interface + impl DataStore)

**Files:**
- Modify: `.../data/UserDataRepository.kt`
- Modify: `.../data/UserPreferencesDataSource.kt`

- [ ] **Step 1: Étendre l'interface**

Remplacer le contenu de `.../data/UserDataRepository.kt` par :

```kotlin
package com.francotte.contentproviderformusic.data

import kotlinx.coroutines.flow.Flow

interface UserDataRepository {

    val userData: Flow<UserData>

    suspend fun setFavoritesSongs(songTitle: String, isFavorite: Boolean)

    suspend fun createPlaylist(id: Long, title: String, description: String)

    suspend fun deletePlaylist(id: Long)

    suspend fun addSongToPlaylist(playlistId: Long, songTitle: String)

    suspend fun removeSongFromPlaylist(playlistId: Long, songTitle: String)
}
```

> Note de conception : `createPlaylist` reçoit l'`id` (généré au point d'appel via `System.currentTimeMillis()`) plutôt que de le renvoyer. Cela rend la navigation vers le détail déterministe sans devoir attendre une coroutine.

- [ ] **Step 2: Implémenter dans le DataSource**

Remplacer le contenu de `.../data/UserPreferencesDataSource.kt` par :

```kotlin
package com.francotte.contentproviderformusic.data

import android.util.Log
import androidx.datastore.core.DataStore
import com.francotte.contentproviderformusic.UserPreferences
import com.francotte.contentproviderformusic.copy
import com.francotte.contentproviderformusic.model.Playlist
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) : UserDataRepository {

    override val userData = userPreferences.data
        .map {
            UserData(
                favoritesSongs = it.favoriteTitlesMap.keys,
                playlists = it.toDomainPlaylists(),
            )
        }

    override suspend fun setFavoritesSongs(songTitle: String, isFavorite: Boolean) {
        try {
            userPreferences.updateData {
                it.copy {
                    if (isFavorite) {
                        favoriteTitles.put(songTitle, true)
                    } else {
                        favoriteTitles.remove(songTitle)
                    }
                }
            }
        } catch (ioException: IOException) {
            Log.e("NiaPreferences", "Failed to update user preferences", ioException)
        }
    }

    override suspend fun createPlaylist(id: Long, title: String, description: String) =
        updatePlaylists { it.withNewPlaylist(id, title, description) }

    override suspend fun deletePlaylist(id: Long) =
        updatePlaylists { it.withoutPlaylist(id) }

    override suspend fun addSongToPlaylist(playlistId: Long, songTitle: String) =
        updatePlaylists { it.withSongAdded(playlistId, songTitle) }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songTitle: String) =
        updatePlaylists { it.withSongRemoved(playlistId, songTitle) }

    /** Lit les playlists (domaine), applique [transform], réécrit toute la liste proto. */
    private suspend fun updatePlaylists(transform: (List<Playlist>) -> List<Playlist>) {
        try {
            userPreferences.updateData { prefs ->
                val newList = transform(prefs.toDomainPlaylists())
                prefs.toBuilder()
                    .clearPlaylists()
                    .addAllPlaylists(newList.map { it.toProto() })
                    .build()
            }
        } catch (ioException: IOException) {
            Log.e("NiaPreferences", "Failed to update playlists", ioException)
        }
    }
}
```

- [ ] **Step 3: Vérifier la compilation du module**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/data/UserDataRepository.kt app/src/main/java/com/francotte/contentproviderformusic/data/UserPreferencesDataSource.kt
git commit -m "feat(playlists): repository CRUD backed by proto datastore"
```

---

## Task 4: Résolution titre→Song (TDD) + PlaylistsUseCase

**Files:**
- Create: `.../domain/PlaylistResolve.kt`
- Test: `.../PlaylistResolveTest.kt`
- Create: `.../domain/PlaylistsUseCase.kt`

- [ ] **Step 1: Écrire le test qui échoue**

Créer `app/src/test/java/com/francotte/contentproviderformusic/PlaylistResolveTest.kt` :

```kotlin
package com.francotte.contentproviderformusic

import com.francotte.contentproviderformusic.domain.resolveByTitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistResolveTest {

    @Test
    fun resolveByTitle_returns_items_in_title_iteration_order() {
        val items = listOf("a", "b", "c")
        val result = resolveByTitle(linkedSetOf("c", "a"), items) { it }
        assertEquals(listOf("c", "a"), result)
    }

    @Test
    fun resolveByTitle_skips_unknown_titles() {
        val result = resolveByTitle(setOf("x"), listOf("a", "b")) { it }
        assertTrue(result.isEmpty())
    }

    @Test
    fun resolveByTitle_empty_titles_returns_empty() {
        val result = resolveByTitle(emptySet(), listOf("a", "b")) { it }
        assertTrue(result.isEmpty())
    }
}
```

- [ ] **Step 2: Lancer le test pour vérifier l'échec**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:testDebugUnitTest --tests "com.francotte.contentproviderformusic.PlaylistResolveTest"
```
Expected: FAIL — « unresolved reference: resolveByTitle ».

- [ ] **Step 3: Écrire l'implémentation**

Créer `.../domain/PlaylistResolve.kt` :

```kotlin
package com.francotte.contentproviderformusic.domain

/**
 * Résout un ensemble de clés (titres) vers les éléments correspondants, dans l'ordre
 * d'itération de [titles]. Générique pour rester testable sans dépendances Android.
 */
fun <T> resolveByTitle(titles: Set<String>, items: List<T>, titleOf: (T) -> String): List<T> {
    val byTitle = items.associateBy(titleOf)
    return titles.mapNotNull { byTitle[it] }
}
```

- [ ] **Step 4: Lancer le test pour vérifier le succès**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:testDebugUnitTest --tests "com.francotte.contentproviderformusic.PlaylistResolveTest"
```
Expected: `BUILD SUCCESSFUL`, 3 tests passés.

- [ ] **Step 5: Créer le PlaylistsUseCase**

Créer `.../domain/PlaylistsUseCase.kt` (calqué sur `FavoritesUseCase`) :

```kotlin
package com.francotte.contentproviderformusic.domain

import com.francotte.contentproviderformusic.data.UserDataRepository
import com.francotte.contentproviderformusic.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaylistsUseCase @Inject constructor(userDataRepository: UserDataRepository) {

    val playlists: Flow<List<Playlist>> = userDataRepository.userData.map { it.playlists }
}
```

- [ ] **Step 6: Vérifier la compilation**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/domain/PlaylistResolve.kt app/src/main/java/com/francotte/contentproviderformusic/domain/PlaylistsUseCase.kt app/src/test/java/com/francotte/contentproviderformusic/PlaylistResolveTest.kt
git commit -m "feat(playlists): title->song resolver (TDD) + PlaylistsUseCase"
```

---

## Task 5: Étendre MainViewModel

**Files:**
- Modify: `.../ui/MainViewModel.kt`

- [ ] **Step 1: Injecter le use case et exposer l'état + les actions**

Dans `.../ui/MainViewModel.kt` :

1. Ajouter les imports (avec les autres imports en haut) :
```kotlin
import com.francotte.contentproviderformusic.domain.PlaylistsUseCase
import com.francotte.contentproviderformusic.model.Playlist
```

2. Modifier le constructeur pour injecter `PlaylistsUseCase` :
```kotlin
class MainViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    favoritesUseCase: FavoritesUseCase,
    playlistsUseCase: PlaylistsUseCase,
) : ViewModel() {
```

3. Ajouter le flow des playlists juste après la déclaration de `favoritesSongs` (ligne ~63) :
```kotlin
    val playlists: StateFlow<List<Playlist>> = playlistsUseCase.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

4. Ajouter les actions juste après `updateFavoritesSongs(...)` (ligne ~108) :
```kotlin
    fun createPlaylist(id: Long, title: String, description: String) {
        viewModelScope.launch {
            userDataRepository.createPlaylist(id, title, description)
        }
    }

    fun deletePlaylists(ids: Set<Long>) {
        viewModelScope.launch {
            ids.forEach { userDataRepository.deletePlaylist(it) }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songTitle: String) {
        viewModelScope.launch {
            userDataRepository.addSongToPlaylist(playlistId, songTitle)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songTitle: String) {
        viewModelScope.launch {
            userDataRepository.removeSongFromPlaylist(playlistId, songTitle)
        }
    }
```

- [ ] **Step 2: Vérifier la compilation**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`. Hilt injecte `PlaylistsUseCase` automatiquement (constructeur `@Inject`).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/ui/MainViewModel.kt
git commit -m "feat(playlists): expose playlists state + CRUD actions in MainViewModel"
```

---

## Task 6: Ressources (drawables) + composant GlassCard

**Files:**
- Create: `app/src/main/res/drawable/ic_add.xml`, `ic_check.xml`, `ic_delete.xml`, `ic_arrow_back.xml`
- Create: `.../ui/composable/GlassCard.kt`

- [ ] **Step 1: Créer les 4 drawables**

`app/src/main/res/drawable/ic_add.xml` :
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF"
        android:pathData="M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
</vector>
```

`app/src/main/res/drawable/ic_check.xml` :
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF"
        android:pathData="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z"/>
</vector>
```

`app/src/main/res/drawable/ic_delete.xml` :
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF"
        android:pathData="M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z"/>
</vector>
```

`app/src/main/res/drawable/ic_arrow_back.xml` :
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF"
        android:pathData="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z"/>
</vector>
```

- [ ] **Step 2: Créer le composant GlassCard**

Créer `.../ui/composable/GlassCard.kt` :

```kotlin
package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.ui.theme.Aurora

/** Conteneur "glass" réutilisable : dégradé Aurora + fine bordure blanche. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Aurora.BarBrush)
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape),
        content = content,
    )
}
```

- [ ] **Step 3: Vérifier la compilation**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/drawable/ic_add.xml app/src/main/res/drawable/ic_check.xml app/src/main/res/drawable/ic_delete.xml app/src/main/res/drawable/ic_arrow_back.xml app/src/main/java/com/francotte/contentproviderformusic/ui/composable/GlassCard.kt
git commit -m "feat(playlists): add icons + reusable GlassCard"
```

---

## Task 7: Activer le 3ᵉ onglet (scaffolding navigation)

Objectif : l'onglet Playlists apparaît dans la bottom bar et affiche un écran placeholder. Les vrais écrans arrivent aux tâches suivantes.

**Files:**
- Modify: `.../ui/composable/TopLevelDestination.kt`
- Modify: `.../ui/state/MusicApp.kt`
- Modify: `.../ui/playlists/PlayListsRoute.kt`
- Modify: `.../ui/navigation/MusicNavHost.kt`

- [ ] **Step 1: Activer la destination top-level**

Dans `.../ui/composable/TopLevelDestination.kt`, remplacer le bloc commenté `PLAYLISTS` par (⚠️ le drawable est `playlist_icon`, pas `ic_playlist_play`) :

```kotlin
    data object PLAYLISTS : TopLevelDestination(
        R.drawable.playlist_icon,
        R.string.playlists,
        PLAYLISTS_ROUTE
    )
```

- [ ] **Step 2: Activer l'onglet dans MusicAppState**

Dans `.../ui/state/MusicApp.kt` :

1. `topLevelDestinations` → décommenter la ligne :
```kotlin
            return persistentListOf(
                TopLevelDestination.LIBRARY,
                TopLevelDestination.FAVORITES,
                TopLevelDestination.PLAYLISTS,
            )
```

2. `currentTopLevelDestination` → décommenter la branche :
```kotlin
                LIBRARY_ROUTE -> TopLevelDestination.LIBRARY
                FAVORITES_ROUTE -> TopLevelDestination.FAVORITES
                PLAYLISTS_ROUTE -> TopLevelDestination.PLAYLISTS
                else -> null
```

3. `navigateToTopLevelDestination` → décommenter la branche :
```kotlin
        when (topLevelDestination) {
            is TopLevelDestination.LIBRARY -> navController.navigateToLibraryScreen(topLevelNavOptions)
            is TopLevelDestination.FAVORITES -> navController.navigateToFavoritesScreen(topLevelNavOptions)
            is TopLevelDestination.PLAYLISTS -> navController.navigateToPlayListsScreen(topLevelNavOptions)
        }
```

- [ ] **Step 3: Ajouter routes, helpers de navigation et graph placeholder**

Remplacer le contenu de `.../ui/playlists/PlayListsRoute.kt` par :

```kotlin
package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.contentproviderformusic.ui.MainViewModel
import com.francotte.contentproviderformusic.ui.composable.PLAYLISTS_ROUTE
import com.francotte.contentproviderformusic.ui.state.MusicAppState

const val PLAYLIST_CREATE_ROUTE = "playlist_create"
const val PLAYLIST_DETAIL_ROUTE = "playlist_detail"
const val PLAYLIST_ADD_SONGS_ROUTE = "playlist_add_songs"
const val PLAYLIST_ID_ARG = "playlistId"

fun NavController.navigateToPlayListsScreen(navOptions: NavOptions? = null) {
    this.navigate(PLAYLISTS_ROUTE, navOptions)
}

fun NavController.navigateToPlaylistCreate() = navigate(PLAYLIST_CREATE_ROUTE)
fun NavController.navigateToPlaylistDetail(id: Long) = navigate("$PLAYLIST_DETAIL_ROUTE/$id")
fun NavController.navigateToPlaylistAddSongs(id: Long) = navigate("$PLAYLIST_ADD_SONGS_ROUTE/$id")

fun NavGraphBuilder.playlistsGraph(
    appState: MusicAppState,
    mainViewModel: MainViewModel,
) {
    composable(route = PLAYLISTS_ROUTE) {
        // Placeholder — remplacé en Task 8.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Playlists")
        }
    }
}
```

- [ ] **Step 4: Enregistrer le graph dans le NavHost**

Dans `.../ui/navigation/MusicNavHost.kt` :

1. Ajouter l'import :
```kotlin
import com.francotte.contentproviderformusic.ui.playlists.playlistsGraph
```

2. Ajouter l'appel dans le `NavHost { … }`, après `favoritesScreen(...)` :
```kotlin
        playlistsGraph(appState, mainViewModel)
```

- [ ] **Step 5: Build + vérification visuelle**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

Vérifier dans l'app (voir skill `run`) : la bottom bar affiche 3 onglets ; taper « Playlists » affiche l'écran placeholder « Playlists ».

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/ui/composable/TopLevelDestination.kt app/src/main/java/com/francotte/contentproviderformusic/ui/state/MusicApp.kt app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlayListsRoute.kt app/src/main/java/com/francotte/contentproviderformusic/ui/navigation/MusicNavHost.kt
git commit -m "feat(playlists): enable 3rd bottom-bar tab with placeholder screen"
```

---

## Task 8: Écran onglet — état vide + liste + FAB

**Files:**
- Create: `.../ui/playlists/PlaylistsScreen.kt`
- Modify: `.../ui/playlists/PlayListsRoute.kt` (brancher le vrai écran)

- [ ] **Step 1: Créer PlaylistsScreen (sans mode suppression)**

Créer `.../ui/playlists/PlaylistsScreen.kt` :

```kotlin
package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Playlist
import com.francotte.contentproviderformusic.ui.composable.BottomBar
import com.francotte.contentproviderformusic.ui.state.MusicAppState
import com.francotte.contentproviderformusic.ui.theme.Aurora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    appState: MusicAppState,
    playlists: List<Playlist>,
    onCreateClick: () -> Unit,
    onPlaylistClick: (Long) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(
                modifier = Modifier.fillMaxWidth(),
                destinations = appState.topLevelDestinations,
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                currentDestination = appState.currentDestination,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                shape = RoundedCornerShape(16.dp),
                containerColor = Aurora.Purple,
                contentColor = Color.White,
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = "Créer une playlist")
            }
        },
    ) { innerPadding ->
        if (playlists.isEmpty()) {
            PlaylistsEmptyState(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                onCreateClick = onCreateClick,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Aurora.Purple.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (playlist.description.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = playlist.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${playlist.songTitles.size} titre(s)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun PlaylistsEmptyState(modifier: Modifier = Modifier, onCreateClick: () -> Unit) {
    val gradient = Brush.linearGradient(listOf(Aurora.Purple, Aurora.Teal, Aurora.Cyan))
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icône teintée par un dégradé (SrcAtop sur les pixels opaques) → multicolore.
        Image(
            painter = painterResource(R.drawable.playlist_icon),
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .drawWithContent {
                    drawContent()
                    drawRect(brush = gradient, blendMode = BlendMode.SrcAtop)
                },
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Aucune playlist",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Créez votre première playlist pour rassembler vos titres préférés.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreateClick) {
            Text("Créer une playlist")
        }
    }
}
```

- [ ] **Step 2: Brancher l'écran dans le graph**

Dans `.../ui/playlists/PlayListsRoute.kt`, remplacer le corps `composable(route = PLAYLISTS_ROUTE) { … }` du placeholder par :

```kotlin
    composable(route = PLAYLISTS_ROUTE) {
        val playlists by mainViewModel.playlists.collectAsStateWithLifecycle()
        PlaylistsScreen(
            appState = appState,
            playlists = playlists,
            onCreateClick = { appState.navController.navigateToPlaylistCreate() },
            onPlaylistClick = { id -> appState.navController.navigateToPlaylistDetail(id) },
        )
    }
```

Ajouter les imports nécessaires en haut de `PlayListsRoute.kt` :
```kotlin
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
```
Et supprimer les imports devenus inutiles du placeholder (`Box`, `fillMaxSize`, `Text`, `Alignment`, `Modifier` s'ils ne servent plus).

- [ ] **Step 3: Build + vérification visuelle**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

Vérifier : onglet Playlists → état vide (icône multicolore + texte + bouton) ; le FAB `+` est en bas à droite.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlaylistsScreen.kt app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlayListsRoute.kt
git commit -m "feat(playlists): playlists tab with empty state, card list and FAB"
```

---

## Task 9: Mode suppression (multi-sélection + bottom bar poubelle)

Comportement : appui long sur une card → mode suppression ; radio à droite de chaque card ; la card longue-pressée est présélectionnée ; on peut en (dé)sélectionner d'autres ; désélectionner la dernière quitte le mode ; barre en bas avec icône poubelle → supprime les sélectionnées.

**Files:**
- Modify: `.../ui/playlists/PlaylistsScreen.kt`
- Modify: `.../ui/playlists/PlayListsRoute.kt`

- [ ] **Step 1: Ajouter l'état de sélection et la barre poubelle**

Remplacer la signature et le corps de `PlaylistsScreen` par la version ci-dessous (ajoute `onDeletePlaylists`, l'état de sélection local, la barre du bas, et passe la sélection aux cards) :

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    appState: MusicAppState,
    playlists: List<Playlist>,
    onCreateClick: () -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onDeletePlaylists: (Set<Long>) -> Unit,
) {
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }

    fun exitSelection() {
        selectionMode = false
        selectedIds = emptySet()
    }

    fun toggle(id: Long) {
        val next = if (id in selectedIds) selectedIds - id else selectedIds + id
        if (next.isEmpty()) exitSelection() else selectedIds = next
    }

    // Ferme le mode suppression au bouton retour système.
    BackHandler(enabled = selectionMode) { exitSelection() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(
                modifier = Modifier.fillMaxWidth(),
                destinations = appState.topLevelDestinations,
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                currentDestination = appState.currentDestination,
            )
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Aurora.Purple,
                    contentColor = Color.White,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = "Créer une playlist")
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (playlists.isEmpty()) {
                PlaylistsEmptyState(
                    modifier = Modifier.fillMaxSize(),
                    onCreateClick = onCreateClick,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            selectionMode = selectionMode,
                            selected = playlist.id in selectedIds,
                            onClick = {
                                if (selectionMode) toggle(playlist.id)
                                else onPlaylistClick(playlist.id)
                            },
                            onLongClick = {
                                if (!selectionMode) {
                                    selectionMode = true
                                    selectedIds = setOf(playlist.id)
                                }
                            },
                        )
                    }
                }
            }

            // Barre du bas persistante avec l'icône poubelle (mode suppression).
            if (selectionMode) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    color = Aurora.Purple,
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${selectedIds.size} sélectionnée(s)", color = Color.White)
                        IconButton(onClick = {
                            onDeletePlaylists(selectedIds)
                            exitSelection()
                        }) {
                            Icon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = "Supprimer",
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Mettre à jour PlaylistCard (clic long + radio)**

Remplacer `PlaylistCard` par :

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistCard(
    playlist: Playlist,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Aurora.Purple.copy(alpha = if (selected) 0.16f else 0.08f))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (playlist.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${playlist.songTitles.size} titre(s)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
        if (selectionMode) {
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}
```

- [ ] **Step 3: Ajouter les imports manquants**

Dans `PlaylistsScreen.kt`, ajouter :
```kotlin
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
```
Retirer l'ancien `clickable` s'il n'est plus utilisé.

- [ ] **Step 4: Passer l'action de suppression depuis le graph**

Dans `PlayListsRoute.kt`, ajouter le paramètre à l'appel `PlaylistsScreen(...)` :
```kotlin
            onDeletePlaylists = { ids -> mainViewModel.deletePlaylists(ids) },
```

- [ ] **Step 5: Build + vérification visuelle**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

Vérifier (après avoir créé 2 playlists via les tâches suivantes, ou en re-testant à la fin) : appui long → radios + barre poubelle ; sélection multiple ; désélection de la dernière quitte le mode ; poubelle supprime.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlaylistsScreen.kt app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlayListsRoute.kt
git commit -m "feat(playlists): multi-select delete mode with trash bottom bar"
```

---

## Task 10: Écran de création

**Files:**
- Create: `.../ui/playlists/PlaylistCreateScreen.kt`
- Modify: `.../ui/playlists/PlayListsRoute.kt`

- [ ] **Step 1: Créer le formulaire**

Créer `.../ui/playlists/PlaylistCreateScreen.kt` :

```kotlin
package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCreateScreen(
    onBack: () -> Unit,
    onCreate: (title: String, description: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle playlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Retour")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nom de la playlist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { onCreate(title.trim(), description.trim()) },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Créer")
            }
        }
    }
}
```

- [ ] **Step 2: Enregistrer la route + navigation**

Dans `.../ui/playlists/PlayListsRoute.kt`, à l'intérieur de `playlistsGraph`, après le `composable(PLAYLISTS_ROUTE)`, ajouter :

```kotlin
    composable(route = PLAYLIST_CREATE_ROUTE) {
        PlaylistCreateScreen(
            onBack = { appState.navController.popBackStack() },
            onCreate = { title, description ->
                val id = System.currentTimeMillis()
                mainViewModel.createPlaylist(id, title, description)
                appState.navController.navigate("$PLAYLIST_DETAIL_ROUTE/$id") {
                    popUpTo(PLAYLIST_CREATE_ROUTE) { inclusive = true }
                }
            },
        )
    }
```

- [ ] **Step 3: Build + vérification visuelle**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

Vérifier : FAB `+` (ou bouton de l'état vide) → formulaire ; « Créer » désactivé tant que le nom est vide.

> La navigation vers le détail affichera un écran vide tant que Task 11 n'est pas faite — normal.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlaylistCreateScreen.kt app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlayListsRoute.kt
git commit -m "feat(playlists): create-playlist form screen"
```

---

## Task 11: Écran détail (card glass + titres + lecture + retrait)

**Files:**
- Create: `.../ui/playlists/PlaylistSongRow.kt`
- Create: `.../ui/playlists/PlaylistDetailScreen.kt`
- Modify: `.../ui/playlists/PlayListsRoute.kt`

- [ ] **Step 1: Créer la rangée réutilisable (titre + slot d'action)**

Créer `.../ui/playlists/PlaylistSongRow.kt` :

```kotlin
package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.composable.ItemAlbumImage
import com.francotte.contentproviderformusic.ui.theme.Aurora

/**
 * Rangée de titre réutilisée par le détail (action retirer) et l'ajout (action +/coche).
 * [trailing] fournit l'action à droite.
 */
@Composable
fun PlaylistSongRow(
    song: Song,
    isCurrent: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrent) Aurora.Purple.copy(0.15f) else Color.White)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ItemAlbumImage(Modifier.size(50.dp), song.data, 16.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .height(70.dp)
                .padding(start = 14.dp, end = 8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing()
    }
}
```

- [ ] **Step 2: Créer l'écran détail**

Créer `.../ui/playlists/PlaylistDetailScreen.kt` :

```kotlin
package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Playlist
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.composable.GlassCard
import com.francotte.contentproviderformusic.ui.theme.Aurora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    songs: List<Song>,
    currentSong: Song?,
    onBack: () -> Unit,
    onAddSongsClick: () -> Unit,
    onPlay: (List<Song>, Int) -> Unit,
    onRemoveSong: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(playlist?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Retour")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // En-tête : card glass avec nom + description centrés.
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = playlist?.title.orEmpty(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                        if (!playlist?.description.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = playlist!!.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            // Rangée "ajouter des titres" : bouton circulaire glass + texte.
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddSongsClick() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassCard(shape = CircleShape, modifier = Modifier.size(48.dp)) {
                        Icon(
                            painterResource(R.drawable.ic_add),
                            contentDescription = "Ajouter des titres",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Text("Ajouter des titres", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Titres de la playlist.
            items(songs, key = { it.title }) { song ->
                val index = songs.indexOf(song)
                PlaylistSongRow(
                    song = song,
                    isCurrent = song.uri == currentSong?.uri,
                    onClick = { onPlay(songs, index) },
                    trailing = {
                        IconButton(onClick = { onRemoveSong(song.title) }) {
                            Icon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = "Retirer",
                                tint = Aurora.Purple,
                            )
                        }
                    },
                )
            }
        }
    }
}
```

- [ ] **Step 3: Enregistrer la route détail**

Dans `.../ui/playlists/PlayListsRoute.kt` :

1. Ajouter les imports :
```kotlin
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.francotte.contentproviderformusic.domain.resolveByTitle
import androidx.compose.runtime.remember
```

2. Dans `playlistsGraph`, ajouter le composable :
```kotlin
    composable(
        route = "$PLAYLIST_DETAIL_ROUTE/{$PLAYLIST_ID_ARG}",
        arguments = listOf(navArgument(PLAYLIST_ID_ARG) { type = NavType.LongType }),
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getLong(PLAYLIST_ID_ARG) ?: return@composable
        val playlists by mainViewModel.playlists.collectAsStateWithLifecycle()
        val allSongs by mainViewModel.songs.collectAsStateWithLifecycle()
        val currentSong by mainViewModel.currentPlayingSong.collectAsStateWithLifecycle()
        val playlist = playlists.find { it.id == playlistId }
        val playlistSongs = remember(playlist, allSongs) {
            playlist?.let { resolveByTitle(it.songTitles, allSongs) { s -> s.title } } ?: emptyList()
        }
        PlaylistDetailScreen(
            playlist = playlist,
            songs = playlistSongs,
            currentSong = currentSong,
            onBack = { appState.navController.popBackStack() },
            onAddSongsClick = { appState.navController.navigateToPlaylistAddSongs(playlistId) },
            onPlay = { list, index -> mainViewModel.playFromList(list, index) },
            onRemoveSong = { songTitle -> mainViewModel.removeSongFromPlaylist(playlistId, songTitle) },
        )
    }
```

- [ ] **Step 4: Build + vérification visuelle**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

Vérifier : après « Créer », l'écran détail montre la card glass (nom + description centrés) et la rangée `+ Ajouter des titres`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlaylistSongRow.kt app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlaylistDetailScreen.kt app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlayListsRoute.kt
git commit -m "feat(playlists): detail screen with glass card, playback and song removal"
```

---

## Task 12: Écran d'ajout de titres (+/coche)

**Files:**
- Create: `.../ui/playlists/PlaylistAddSongsScreen.kt`
- Modify: `.../ui/playlists/PlayListsRoute.kt`

- [ ] **Step 1: Créer l'écran d'ajout**

Créer `.../ui/playlists/PlaylistAddSongsScreen.kt` :

```kotlin
package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.model.Song
import com.francotte.contentproviderformusic.ui.theme.Aurora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistAddSongsScreen(
    songs: List<Song>,
    addedTitles: Set<String>,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Ajouter des titres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Retour")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(songs, key = { it.uri.toString() }) { song ->
                val added = song.title in addedTitles
                PlaylistSongRow(
                    song = song,
                    trailing = {
                        if (added) {
                            IconButton(onClick = {}, enabled = false) {
                                Icon(
                                    painterResource(R.drawable.ic_check),
                                    contentDescription = "Déjà ajouté",
                                    tint = Aurora.Teal,
                                )
                            }
                        } else {
                            IconButton(onClick = { onAdd(song.title) }) {
                                Icon(
                                    painterResource(R.drawable.ic_add),
                                    contentDescription = "Ajouter",
                                    tint = Aurora.Purple,
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}
```

- [ ] **Step 2: Enregistrer la route d'ajout**

Dans `.../ui/playlists/PlayListsRoute.kt`, dans `playlistsGraph`, ajouter :

```kotlin
    composable(
        route = "$PLAYLIST_ADD_SONGS_ROUTE/{$PLAYLIST_ID_ARG}",
        arguments = listOf(navArgument(PLAYLIST_ID_ARG) { type = NavType.LongType }),
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getLong(PLAYLIST_ID_ARG) ?: return@composable
        val playlists by mainViewModel.playlists.collectAsStateWithLifecycle()
        val allSongs by mainViewModel.songs.collectAsStateWithLifecycle()
        val addedTitles = playlists.find { it.id == playlistId }?.songTitles ?: emptySet()
        PlaylistAddSongsScreen(
            songs = allSongs,
            addedTitles = addedTitles,
            onBack = { appState.navController.popBackStack() },
            onAdd = { songTitle -> mainViewModel.addSongToPlaylist(playlistId, songTitle) },
        )
    }
```

- [ ] **Step 3: Build + vérification visuelle**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

Vérifier : détail → `+ Ajouter des titres` → liste complète ; `+` ajoute (devient une coche désactivée) ; retour → le titre apparaît sous la rangée.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlaylistAddSongsScreen.kt app/src/main/java/com/francotte/contentproviderformusic/ui/playlists/PlayListsRoute.kt
git commit -m "feat(playlists): add-songs screen with +/check state"
```

---

## Task 13: Vérification end-to-end

**Files:** aucun (vérification manuelle via l'app).

- [ ] **Step 1: Build complet + tests unitaires**

Run:
```bash
JAVA_HOME="C:/Program Files/Java/jdk-19" ./gradlew :app:testDebugUnitTest :app:assembleDebug
```
Expected: `BUILD SUCCESSFUL`, tous les tests passent.

- [ ] **Step 2: Parcours complet dans l'app** (voir skill `run`)

Scénario à valider :
1. Onglet Playlists vide → icône multicolore + texte + bouton.
2. FAB `+` → formulaire → « Créer » désactivé si nom vide → saisir « Road trip » + description → Créer.
3. Détail : card glass (nom + description centrés) + rangée `+ Ajouter des titres`.
4. `+ Ajouter des titres` → liste ; ajouter 2 titres (`+` → coche) → retour.
5. Détail : les 2 titres apparaissent sous la rangée ; taper un titre lance la lecture.
6. Retirer un titre (icône poubelle sur la rangée) → il disparaît.
7. Retour à l'onglet : la card de la playlist affiche le bon nombre de titres.
8. Créer une 2ᵉ playlist ; appui long sur une card → mode suppression (radios + barre poubelle) ; sélectionner les 2 → poubelle → supprimées.
9. **Persistance** : tuer et relancer l'app → les playlists non supprimées sont toujours là.

- [ ] **Step 3: Commit final éventuel** (si ajustements)

```bash
git add -A
git commit -m "test(playlists): end-to-end verification adjustments"
```

---

## Notes de conception (rappel)

- **Ordre des titres dans le détail** : suit l'ordre d'insertion (`repeated string` proto → `LinkedHashSet` → `resolveByTitle` respecte l'ordre d'itération). Les titres récemment ajoutés apparaissent en dernier.
- **Identité par titre** : cohérent avec les favoris. Deux fichiers au même titre exact seraient traités comme un seul dans une playlist — accepté (même limite que les favoris).
- **Hors périmètre v1** : renommer/éditer une playlist, réordonner les titres, mini-player sur les sous-écrans, scinder `MainViewModel`.
