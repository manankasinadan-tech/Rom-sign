# Sélecteur Universel de Fichiers ROM & IMG avec Analyse Immédiate

Ajout d'une fonctionnalité complète de sélection de fichiers pour l'application ROM Resigner Studio, combinant l'explorateur système natif Android (Storage Access Framework) et un navigateur de fichiers sdcard visuel intégré, suivi d'une analyse immédiate et de la préparation automatique de la ROM dans l'espace de travail.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> **Choix confirmés avec l'utilisateur :**
> - **Mode de sélection :** Double modalité avec (1) Sélecteur système Android SAF (compatible toutes sources, clés USB OTG, Downloads, Drive) et (2) Navigateur de fichiers visuel intégré pour explorer directement `/sdcard` et ses sous-dossiers.
> - **Comportement post-sélection :** Analyse immédiate du fichier (type de conteneur ZIP/IMG/GSI, taille, partitions détectées) et préparation automatique dans l'espace de travail `/sdcard/ROM_Resigner/workspace`.

---

### 1. Overview & Core Concept

- **Expérience utilisateur :** Sur l'écran Studio, un bouton proéminent "Parcourir & Sélectionner une ROM / IMG" permet à l'utilisateur de choisir son fichier soit via l'application Fichiers standard d'Android, soit via l'explorateur interactif intégré dans `/sdcard`.
- **Formats ciblés :** Archives flashables (`.zip`), images de partitions (`system.img`, `vendor.img`, `product.img`), images GSI (`system-arm64-ab.img`, `gsi.img`), et payloads OTA (`payload.bin`).
- **Analyse temps réel :** Dès la sélection, l'application lit les métadonnées (taille, en-tête magic bytes pour distinguer les images sparse Android des images ext4 brutes et archives ZIP), met à jour la carte d'état et émet un journal d'analyse dans la console de télémétrie.

---

### 2. User Experience & Visual Design

#### Nouveaux composants d'interface
1. **Bouton d'Action & Sélecteur sur l'Écran Studio :**
   - Nouveau composant interactif "Sélectionner un fichier ROM / IMG" avec menu contextuel :
     - *Option A :* "Explorateur Système (SAF)" (ouvre l'application Documents/Fichiers Android avec filtre `*/*` ou `.zip, .img`).
     - *Option B :* "Naviguer dans /sdcard" (ouvre une boîte de dialogue d'exploration arborescente locale).
   - Carte de détail du fichier sélectionné : Nom du fichier, taille en Mo/Go, chemin source complet, badge de type (`ZIP FLASHABLE`, `IMAGE SYSTEM / GSI`, `DOSSIER EXTRAIT`).

2. **Navigateur de Fichiers Intégré (`StorageFileBrowserDialog`) :**
   - Navigation fluide dossier par dossier à partir de `/sdcard` ou `/storage/emulated/0`.
   - Bouton "Dossier Parent" et chemin d'accès interactif (breadcrumbs).
   - Filtrage intelligent : mise en avant visuelle des fichiers `.zip`, `.img`, `.bin`, `.tar`.
   - Affichage de la taille de chaque élément et date de modification.

3. **Retour Visuel et Télémétrie :**
   - Message immédiat dans la console de logs en direct :
     - `[INFO] [PICKER] Fichier sélectionné : lineage-21.0-2024.zip (1.84 Go)`
     - `[INFO] [ANALYSIS] Type détecté : Archive ZIP Flashable AOSP`
     - `[SUCCESS] [WORKSPACE] Préparation du workspace dans /sdcard/ROM_Resigner/workspace`

---

### 3. Key Product Decisions & Trade-Offs

- **Gestion des URIs SAF et chemins directs :**
  - *Approche :* Lorsqu'un fichier est sélectionné via SAF (`content://...`), l'application extrait le nom réel et la taille via `DocumentFile` ou `ContentResolver.query()`, et prépare un flux de copie/liaison vers le dossier de travail. Lorsqu'un fichier est sélectionné via le navigateur interne `/sdcard`, le chemin direct est conservé.
  - *Raison :* Compatibilité totale avec les restrictions de stockage scoped storage sous Android 11 à 15 tout en offrant une ergonomie maximale pour les utilisateurs avancés.

- **Détection automatique du format sans dépendre de l'extension seule :**
  - *Approche :* Analyse des premiers octets du fichier (`PK\x03\x04` pour ZIP, `0x3AFF26ED` pour Android Sparse Image, `0x53EF` à l'offset 1080 pour ext4).
  - *Raison :* Prévient les erreurs de manipulation si un fichier GSI a une extension tronquée ou renommée.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        STUDIO DASHBOARD SCREEN                         │
│  [ Bouton Parcourir ROM / IMG ] ─────────┐                             │
└──────────────┬───────────────────────────┼─────────────────────────────┘
               │                           │
               ▼                           ▼
┌───────────────────────────────┐ ┌──────────────────────────────────────┐
│  SAF System Document Launcher │ │  StorageFileBrowserDialog (/sdcard)  │
│  (ActivityResultContracts)    │ │  (Navigation arborescente interne)   │
└──────────────┬────────────────┘ └────────┬─────────────────────────────┘
               │                           │
               └──────────────┬────────────┘
                              ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     ROM RESIGNER VIEWMODEL                             │
│  • onRomFileSelected(uri, directPath, displayName, sizeBytes)          │
│  • Détection du type de package (ZIP, SYSTEM_IMG, GSI, PAYLOAD)        │
│  • Émission de log de préparation dans le flux de télémétrie           │
│  • Mise à jour de StateFlow<RomSummary>                                │
└────────────────────────────────────────────────────────────────────────┘
```

---

Le plan d'implémentation est prêt pour votre validation.
