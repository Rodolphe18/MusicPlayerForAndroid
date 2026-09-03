# Règles R8 / ProGuard du module app.
#
# Elles s'ajoutent à `proguard-android-optimize.txt` (règles par défaut d'AGP) et
# aux « consumer rules » que chaque dépendance embarque dans son AAR/JAR. La
# majorité de nos libs se protègent seules — Media3, OkHttp, Hilt/Dagger,
# coroutines, play-services-ads, AppCompat, Navigation, Room, WorkManager. Seuls
# les cas ci-dessous ne sont couverts par personne.
#
# Après chaque changement de dépendance, vérifier
# `app/build/outputs/mapping/release/missing_rules.txt` : R8 y liste lui-même les
# `-dontwarn` qu'il attend.


# ---------------------------------------------------------------------------
# Proto DataStore — INDISPENSABLE
# ---------------------------------------------------------------------------
# Ni protobuf-javalite, ni protobuf-kotlin-lite, ni androidx.datastore n'embarquent
# de règles. Or les classes générées (UserPreferences, Playlist) déclarent leur
# schéma par NOM DE CHAMP dans dynamicMethod() :
#
#   case BUILD_MESSAGE_INFO:
#     objects = { "favoriteTitles_", ..., "playlists_", ..., "autoplayDisabled_" };
#     return newMessageInfo(DEFAULT_INSTANCE, info, objects);
#
# Le runtime protobuf-lite résout ensuite ces champs par réflexion. Si R8 les
# renomme, UserPreferencesSerializer.readFrom() ne sait plus relire le fichier :
# favoris ET playlists de tous les utilisateurs sont perdus à la mise à jour.
#
# Google applique exactement la même règle à ses propres protos dans le
# proguard.txt de play-services-ads.
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}


# ---------------------------------------------------------------------------
# Firebase Crashlytics
# ---------------------------------------------------------------------------
# firebase-crashlytics n'embarque aucune règle. Rien ne casse sans, mais les
# stacktraces remontées deviennent illisibles. On conserve le nom de fichier et
# les numéros de ligne ; le plugin Gradle Crashlytics envoie le mapping.txt pour
# désobfusquer le reste côté console.
#
# -renamesourcefileattribute remplace le nom de fichier réel par « SourceFile » :
# les numéros de ligne restent exploitables via le mapping, sans divulguer
# l'arborescence des sources.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
