# Journal des modifications

Ce fichier sert aussi à préparer la **demande d'accès à la production** dans la Play
Console : Google y demande ce que les tests fermés ont révélé et ce qui a été corrigé
en conséquence. La section « Pour le formulaire d'accès à la production », en bas,
reprend l'essentiel sous une forme directement réutilisable.

---

## À publier — versionCode à incrémenter

> `versionCode = 2` a été consommé par la 1.1 du 3 septembre 2026. Passer à `3` dans
> `app/build.gradle.kts` avant tout nouvel envoi, sinon la Play Console rejette le
> bundle.

Rien pour l'instant.

---

## 1.1 (versionCode 2) — 3 septembre 2026

### Corrigé

**Écran blanc au démarrage après un refus de permission** — signalé par un testeur le
1er septembre 2026.

*Symptôme.* À l'ouverture de l'application, l'utilisateur arrivait sur un écran blanc,
sans message ni bouton. Aucun plantage : l'incident était donc totalement invisible
côté développeur, et seul le retour du testeur l'a fait remonter.

*Cause.* L'application demandait l'accès aux fichiers audio **et** l'autorisation de
notification en un seul bloc, puis exigeait que *toutes* soient accordées pour
afficher quoi que ce soit. Refuser les notifications — sans aucun rapport avec la
lecture de musique — suffisait donc à masquer l'intégralité de l'interface. Deux
facteurs rendaient la panne définitive : Android cesse d'afficher la demande après
deux refus, et l'application ne relisait jamais l'état réel des autorisations, se
fiant uniquement à la réponse de la boîte de dialogue. L'application restait donc
inutilisable à chaque lancement suivant.

*Corrections apportées.*

1. **Séparation des autorisations.** Seul l'accès aux fichiers audio conditionne
   désormais l'affichage. L'autorisation de notification est devenue facultative :
   la refuser prive de la notification de lecture, sans plus bloquer l'application.
2. **Lecture de l'état réel du système.** L'état des autorisations est vérifié
   auprès d'Android et réévalué à chaque retour au premier plan. Une autorisation
   accordée depuis les réglages du téléphone est prise en compte immédiatement, sans
   avoir à redémarrer l'application.
3. **Plus jamais d'écran vide.** Un écran d'explication remplace l'écran blanc : il
   indique pourquoi l'accès est nécessaire, précise qu'aucun fichier ne quitte
   l'appareil, et propose un bouton « Ouvrir les réglages ». Traduit dans les
   14 langues prises en charge.

**Application affichée en anglais sur les appareils configurés en indonésien** —
découvert le 3 septembre 2026 en préparant la fiche indonésienne du Play Store.

*Symptôme.* Un appareil réglé en indonésien ouvrait l'application entièrement en
anglais, alors que la traduction existait et était complète.

*Cause.* Android hérite d'un code de langue obsolète de Java : pour l'indonésien,
`Locale("id").getLanguage()` renvoie `in` et non `id`. Les traductions se trouvaient
dans `values-id`, le système cherchait `values-in`, ne trouvait rien et retombait sur
l'anglais. Le même piège concerne l'hébreu (`he` devient `iw`) et le yiddish (`yi`
devient `ji`) ; aucune des autres langues de l'application n'est touchée.

*Correction.* Le dossier `values-id` est renommé en `values-in`. Vérifié sur un
appareil réglé en indonésien : l'application affiche désormais « Perpustakaan Saya ».

Portée : tous les utilisateurs indonésiens depuis la première version, soit une
traduction complète restée invisible.

### Ajouté

**Firebase Crashlytics** — remontée automatique des plantages.

L'incident ci-dessus a mis en évidence l'absence totale de visibilité sur ce que
vivent les testeurs. Crashlytics comble ce manque pour les plantages. Intégré sans
Firebase Analytics, afin de ne pas élargir la collecte de données au-delà du
nécessaire.

Conséquences sur la fiche Store, à traiter avant publication :
- déclarer les *journaux de plantage* dans la section **Sécurité des données** ;
- mentionner Firebase Crashlytics dans la politique de confidentialité.

### Modifié

**Minification R8 activée sur la release.** Le code mort est supprimé et les
ressources non référencées élaguées, ce qui allège le téléchargement ; le code est
par ailleurs obfusqué. Les règles nécessaires (proto DataStore, Crashlytics) sont
consignées dans `proguard-rules.pro` ; les licences open source, chargées par
`getIdentifier()`, sont protégées par le fichier `keep` que génère le plugin
oss-licenses. Le `mapping.txt` est envoyé à Crashlytics à chaque `bundleRelease`,
de sorte que les traces restent lisibles en console malgré l'obfuscation.

**Dépendance kotlinx.serialization retirée.** Elle était déclarée sans être utilisée
nulle part dans le code.

---

## 1.0 (versionCode 1) — 1er septembre 2026

Première version, publiée en test fermé.

Lecture des fichiers audio locaux, lecteur plein écran, lecture en arrière-plan avec
notification média, favoris, playlists, recherche par titre ou artiste, lecture
aléatoire et répétition, partage d'un morceau. 14 langues. Bannière publicitaire avec
consentement UMP.

---

## Pour le formulaire d'accès à la production

**Ce que les tests fermés ont révélé.** Un testeur a signalé un écran blanc au
lancement, sur lequel l'application restait bloquée. Le problème ne provoquait aucun
plantage et n'apparaissait sur aucun de nos appareils : il n'a pu être découvert que
grâce au test fermé. L'analyse a montré qu'il touchait en réalité *tout* utilisateur
refusant l'autorisation de notification, quelle que soit sa version d'Android, et
qu'il rendait l'application définitivement inutilisable.

Un second défaut est apparu en préparant les fiches traduites du Store : sur tout
appareil réglé en indonésien, l'application s'affichait en anglais. Sa traduction
complète était restée invisible depuis la première version, à cause d'un code de
langue hérité qu'Android attend sous une autre forme.

**Ce que nous avons changé.** Les autorisations facultatives ne bloquent plus
l'application ; l'état des autorisations est relu auprès du système à chaque retour au
premier plan ; et un écran d'explication traduit en 14 langues, avec un accès direct
aux réglages, remplace l'ancien écran vide. La traduction indonésienne est désormais
réellement appliquée. Firebase Crashlytics a été intégré pour détecter à l'avenir ce
type d'incident sans dépendre d'un signalement manuel.

**Ce que nous en retenons.** Une autorisation refusée doit toujours mener à un écran
qui explique la situation et propose une issue, jamais à une interface vide. Et une
traduction livrée n'est pas une traduction vérifiée : chaque langue doit être ouverte
au moins une fois sur un appareil réellement configuré dans cette langue.
