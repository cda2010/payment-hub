# payment-network

Référentiel du **réseau de distribution** : unités d'organisation (UO) et regroupements.
C'est une **application Spring Boot autonome** avec sa propre base de données, comme payment-pricing.

## Modèle

Chaque unité appartient à une **zone** monétaire, qui fixe sa devise. `UEMOA` (XOF) et `CEMAC` (XAF)
sont fournies ; l'administration en ajoute d'autres (voir « Zones »).
Pays, FSE, agences, masters, marchands, sous-distributeurs et regroupements sont tous des **unités
d'organisation** (même table, même API), distinguées par leur type. Les unités forment une
hiérarchie stricte, où les regroupements se placent à n'importe quel niveau sous une unité :

```
ZONE (UEMOA, CEMAC…, hors table des unités)
PAYS (racine de l'arbre, dans une zone)
└── FSE (plusieurs par pays)
    └── AGENCE
        ├── MASTER
        │   └── SOUS_DISTRIBUTEUR
        └── MARCHAND
REGROUPEMENT : sous n'importe laquelle de ces unités, avec des membres pris dans sa branche
```

| Type | Parent autorisé |
|------|-----------------|
| `PAYS` | aucun (racine) |
| `FSE` | `PAYS` |
| `AGENCE` | `FSE` |
| `MASTER` | `AGENCE` |
| `MARCHAND` | `AGENCE` |
| `SOUS_DISTRIBUTEUR` | `MASTER` |
| `REGROUPEMENT` | `PAYS`, `FSE`, `AGENCE`, `MASTER`, `MARCHAND` ou `SOUS_DISTRIBUTEUR` (jamais à la racine) |

Une unité rattachée hérite de la zone de son parent : une branche ne change jamais de zone.

**Zones.** Table `zone` (migration `V3__zones.sql`) : code (2 à 10 lettres, chiffres ou `_`), libellé,
devise ISO 4217, statut. Une zone se crée et se renomme par l'API ; son **code est définitif** et sa
**devise se fige** dès qu'elle contient une unité (`DEVISE_FIGEE`). Elle ne se supprime pas : elle se
désactive une fois tous ses pays désactivés (`ZONE_AVEC_PAYS_ACTIFS`). Dans une zone inactive, on ne
crée plus de pays, et on n'y réactive pas de pays (`ZONE_INACTIVE`).

**Pays.** Un pays se crée à la racine d'une zone **active**, avec son code ISO 3166 alpha-2 comme code
d'unité (`CODE_PAYS_INVALIDE` sinon, `CODE_EXISTANT` s'il existe déjà). Les 14 pays UEMOA et CEMAC sont
fournis par la migration `V2__pays.sql`, avec l'identifiant `a0000000-0000-0000-0000-000000000<code ISO
numérique>` (ex. `…000854` pour le Burkina Faso).

| Zone | Pays |
|------|------|
| UEMOA | `BJ` Bénin, `BF` Burkina Faso, `CI` Côte d'Ivoire, `GW` Guinée-Bissau, `ML` Mali, `NE` Niger, `SN` Sénégal, `TG` Togo |
| CEMAC | `CM` Cameroun, `CF` Centrafrique, `CG` Congo, `GA` Gabon, `GQ` Guinée équatoriale, `TD` Tchad |

Un pays ne change ni de zone ni de code ; seul son libellé se modifie. Il se désactive quand il n'a plus de FSE actif. Un **FSE**
se crée sous un pays, dont il hérite la zone, et **n'en change plus** (`FSE_NON_DEPLACABLE`).

**Regroupement.** Un regroupement (ex. « Agences du Nord ») est une unité de l'arbre, rattachée à une
unité de **n'importe quel niveau** (pays, FSE, agence, master, marchand ou sous-distributeur) ; il n'y a pas
de regroupement à la racine. Il n'a pas d'enfants mais des **membres**, de niveaux éventuellement différents. Il couvre chaque membre **et toute sa descendance** :
regrouper l'Agence A et l'Agence B couvre aussi leurs masters, marchands et sous-distributeurs.
Les règles :

- une unité peut appartenir à plusieurs regroupements (relation N-N) ;
- un regroupement a toujours un parent (niveau = niveau du parent + 1) ; il ne peut être ni membre ni
  parent d'une autre unité (`MEMBRE_REGROUPEMENT_INTERDIT`, `RATTACHEMENT_INTERDIT`) ;
- tous les membres sont dans la zone du regroupement et doivent être actifs au moment de l'ajout ;
- **membres dans la branche du parent** : un membre est le parent lui-même ou une unité de sa branche
  (`MEMBRE_HORS_BRANCHE`) ; un regroupement placé sous Master Sahel ne regroupe que Master Sahel et ses
  sous-distributeurs ;
- **pas de double couverture** : dans un même regroupement, aucun membre ne descend d'un autre
  membre (ajouter Master Sahel à « Agences du Nord », qui contient déjà l'Agence de Ouahigouya, est
  refusé).

| Table | Rôle |
|-------|------|
| `zone` | Zones monétaires : code, libellé, devise, statut |
| `unite_organisation` | Toutes les unités, regroupements compris : type, zone (clé étrangère vers `zone`), parent (obligatoire sauf pour un pays), statut, niveau, chemin |
| `regroupement_membre` | Membres directs de chaque regroupement (unique par couple regroupement / unité) |

`chemin` est un **chemin matérialisé** (`/idPays/idFse/idAgence/.../idUnite/`). Il permet de lire une
branche entière par un simple `LIKE 'chemin%'` (index `varchar_pattern_ops`), de calculer les
ascendants sans requête récursive, et de vérifier la couverture d'un regroupement. `niveau` vaut 1
pour un pays, 2 pour un FSE, 3 pour une agence, etc. Un regroupement a le niveau de son parent + 1.

## Règles de gestion

- **Création** : le code est unique (mis en majuscules, lettres, chiffres, `_` ou `-`, 40 max).
  La zone est obligatoire, existante et active pour un pays ; toute autre unité, regroupement compris,
  exige un parent (`PARENT_OBLIGATOIRE`) dont elle hérite la zone (si elle est fournie, elle doit être identique).
- **Modification** : libellé et type. Le code, la zone et le parent ne
  changent pas ici. Le type change seulement entre types compatibles avec le parent et avec les
  unités rattachées (un master qui a des sous-distributeurs ne devient pas marchand). Un pays reste
  un pays et un regroupement reste un regroupement.
- **Déplacement** : `POST /unites/{id}/deplacer` rattache une agence, un master, un marchand, un
  sous-distributeur ou un regroupement à un autre parent ; toute sa branche suit (chemins et niveaux recalculés en une
  seule requête). Un regroupement se déplace seul, si tous ses membres sont dans la branche du nouveau
  parent (`MEMBRE_HORS_BRANCHE`). Ni un pays ni un FSE ne se déplacent. Refusé aussi si le nouveau parent est dans la
  branche (`BOUCLE`), d'une autre zone, ou si le déplacement ferait sortir un membre de la branche
  de son regroupement (`SORTIE_BRANCHE_REGROUPEMENT`) ou créerait une double couverture.
- **Désactivation** : refusée tant qu'il reste des unités actives rattachées (`ENFANTS_ACTIFS`).
  **Réactivation** : le parent doit être actif (pour un pays, sa zone). Une unité inactive ne peut recevoir ni enfant ni
  entrée dans un regroupement ; elle reste membre des regroupements dont elle faisait déjà partie.

## API

Swagger : `http://localhost:8096/swagger-ui.html`. Toutes les routes exigent un token.

| Méthode | Route | Rôle requis |
|---------|-------|-------------|
| GET | `/api/v1/network/referentiel` (zones, types, rattachements autorisés) | authentifié |
| GET | `/api/v1/network/zones`, `…/zones/{code}` (avec `nbPays` et `nbUnites`) | authentifié |
| POST / PUT | `/api/v1/network/zones`, `/api/v1/network/zones/{code}` | `ADMIN` ou `CONFIG_MANAGER` |
| POST | `/api/v1/network/zones/{code}/activer`, `…/desactiver` | `ADMIN` ou `CONFIG_MANAGER` |
| GET | `/api/v1/network/unites` (filtres `zone`, `type` répétable, `statut`, `parentId`, `branche` = unité et sa descendance, `sansRegroupements`, `texte` sur code ou libellé ; paginé) | authentifié |
| GET | `/api/v1/network/unites/{id}`, `…/{id}/ascendants` ; `…/{id}/enfants` (paginé, par code, avec `nbEnfants` et `nbMembres`), `…/{id}/descendants` (paginé) | authentifié |
| GET | `/api/v1/network/unites/{id}/regroupements` (regroupements qui couvrent l'unité, `direct` ou via un ascendant) | authentifié |
| GET | `/api/v1/network/arbre?zone=UEMOA` (pays de la zone, racines de l'arbre, avec `nbEnfants` ; les niveaux suivants par `…/unites/{id}/enfants`) | authentifié |
| GET | `/api/v1/network/positions?unite={id}` \| `?pays=BF` \| `?zone=UEMOA` (exactement un critère : zone, devise, unité, lignée de l'unité jusqu'au pays, regroupements qui la couvrent ; utilisé par payment-pricing pour choisir la grille) | authentifié |
| GET | `/api/v1/network/regroupements/{id}/membres`, `…/{id}/perimetre` (membres et leurs branches, paginé) | authentifié |
| POST / PUT | `/api/v1/network/unites`, `/api/v1/network/unites/{id}` | `ADMIN` ou `CONFIG_MANAGER` |
| POST | `/api/v1/network/unites/{id}/deplacer`, `…/activer`, `…/desactiver` | `ADMIN` ou `CONFIG_MANAGER` |
| POST / DELETE | `/api/v1/network/regroupements/{id}/membres[/{uniteId}]` | `ADMIN` ou `CONFIG_MANAGER` |
| POST | `/api/v1/network/imports/unites`, `…/imports/membres` (multipart `fichier`, `essai=true` par défaut) | `ADMIN` ou `CONFIG_MANAGER` |
| GET | `/api/v1/network/imports/modeles/{unites\|membres}?format=csv\|xlsx` (modèle à remplir) | `ADMIN` ou `CONFIG_MANAGER` |

**Pagination.** Les listes paginées acceptent `page` (à partir de 0), `taille` (50 par défaut, 500 au plus),
`tri` (`code`, `libelle`, `type`, `zone`, `statut`, `niveau`, `maj`) et `sens` (`asc`, `desc`) ; par défaut, niveau
puis code. Elles renvoient `{ contenu, total, page, taille }`. Aucune route ne renvoie le réseau entier : la console
charge l'arbre nœud par nœud et cherche les unités côté serveur.

**Import en masse.** Un fichier CSV (séparateur `;`, `,` ou tabulation, UTF-8 ou Windows-1252) ou Excel
(`.xlsx`/`.xls`, première feuille), 20 000 lignes et 10 Mo au plus ; en-têtes sans casse ni accents.

- *Unités* : `code ; libelle ; type ; code_parent ; zone`. Le parent est une unité existante ou une ligne du fichier,
  dans n'importe quel ordre ; la zone n'est obligatoire que pour un pays. Un code existant est mis à jour : libellé
  renommé, déplacement avec sa branche si `code_parent` change (mêmes règles que `…/deplacer`) ; type et zone ne
  changent pas. Une ligne identique compte comme inchangée : réimporter un fichier ne modifie rien.
- *Membres* : `code_regroupement ; code_membre`, avec les contrôles de l'ajout unitaire (branche, double couverture
  vis-à-vis des membres existants et des autres lignes).

Tout le fichier est contrôlé avant écriture et le rapport liste les erreurs par ligne (`ligne`, `code`, `champ`,
`codeErreur`, `message` ; 500 au plus). Avec `essai=true`, rien n'est écrit ; sinon l'import est appliqué en une
transaction, et rien n'est écrit s'il reste une erreur. Insertions et renommages par lots JDBC : 6 240 unités
s'importent en moins d'une seconde.

```bash
curl -s -H "Authorization: Bearer $TOKEN" -o modele.xlsx \
  'http://localhost:8096/api/v1/network/imports/modeles/unites?format=xlsx'
curl -s -H "Authorization: Bearer $TOKEN" -F fichier=@unites.csv \
  'http://localhost:8096/api/v1/network/imports/unites?essai=true'     # contrôle ; essai=false pour appliquer
```

```bash
# Créer une zone, puis un pays dedans
curl -s -X POST http://localhost:8096/api/v1/network/zones \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"code":"EAC","libelle":"Communauté d'\''Afrique de l'\''Est","devise":"KES"}'
curl -s -X POST http://localhost:8096/api/v1/network/unites \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"code":"KE","libelle":"Kenya","type":"PAYS","zone":"EAC"}'

# Créer un FSE au Mali (la zone UEMOA est héritée du pays)
curl -s -X POST http://localhost:8096/api/v1/network/unites \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"code":"FSE-MALI","libelle":"Coris Money Mali","type":"FSE",
       "parentId":"a0000000-0000-0000-0000-000000000466"}'

# Créer une agence sous le FSE UEMOA
curl -s -X POST http://localhost:8096/api/v1/network/unites \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"code":"AG-DORI","libelle":"Agence de Dori","type":"AGENCE",
       "parentId":"10000000-0000-0000-0000-000000000001"}'

# Ajouter l'agence au regroupement « Agences du Nord »
curl -s -X POST http://localhost:8096/api/v1/network/regroupements/10000000-0000-0000-0000-000000000041/membres \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"uniteId":"<id de AG-DORI>"}'
```

**Erreurs.** Format `ApiError` de payment-2 (`timestamp`, `status`, `code`, `message`, `path`), avec
`details` (`code`, `champ`, `message`) pour les erreurs de validation des requêtes.

| Statut | Codes |
|--------|-------|
| 400 | `REQUETE_INVALIDE` (détails `CHAMP_INVALIDE` ; fichier ou paramètre manquant), `REQUETE_ILLISIBLE` |
| 413 | `FICHIER_TROP_GROS` (plus de 10 Mo) |
| 422 (import) | `FICHIER_VIDE`, `FICHIER_ILLISIBLE`, `COLONNES_MANQUANTES`, `TROP_DE_LIGNES`, `FORMAT_INCONNU` ; les erreurs de ligne sont dans le rapport (`CODE_OBLIGATOIRE`, `CODE_INVALIDE`, `DOUBLON_FICHIER`, `LIBELLE_OBLIGATOIRE`, `LIBELLE_TROP_LONG`, `TYPE_INCONNU`, `TYPE_DIFFERENT`, `PARENT_EN_ERREUR`, `REGROUPEMENT_INEXISTANT`, `ERREUR_BASE` et les codes de hiérarchie et de regroupement ci-dessous) |
| 401 / 403 | `UNAUTHORIZED`, `ACCESS_DENIED` |
| 404 | `UNITE_INEXISTANTE`, `PARENT_INEXISTANT`, `MEMBRE_INEXISTANT`, `ZONE_INEXISTANTE`, `PAYS_INEXISTANT`, `MODELE_INEXISTANT` |
| 422 (positions) | `POSITION_INVALIDE` (aucun ou plusieurs critères) |
| 409 | `CODE_EXISTANT`, `DEJA_MEMBRE`, `ZONE_EXISTANTE` |
| 422 (zones) | `ZONE_INACTIVE`, `DEVISE_FIGEE`, `ZONE_AVEC_PAYS_ACTIFS` |
| 422 (hiérarchie) | `CODE_PAYS_INVALIDE`, `FSE_NON_DEPLACABLE`, `RATTACHEMENT_INTERDIT`, `PARENT_OBLIGATOIRE`, `PARENT_INACTIF`, `BOUCLE`, `ZONE_OBLIGATOIRE`, `ZONE_INCOHERENTE`, `TYPE_NON_MODIFIABLE`, `TYPE_INCOMPATIBLE_ENFANTS`, `ENFANTS_ACTIFS` |
| 422 (regroupements) | `PAS_UN_REGROUPEMENT`, `MEMBRE_REGROUPEMENT_INTERDIT`, `UNITE_INACTIVE`, `MEMBRE_HORS_BRANCHE`, `DOUBLON_COUVERTURE`, `SORTIE_BRANCHE_REGROUPEMENT` |

## Authentification

Identique à payment-pricing : pas de base utilisateurs, nom et rôles viennent du token, choisi
d'après le claim `iss` :

- **Keycloak** (realm `payment`, voir `../keycloak/README.md`) : signature RS256 vérifiée via le
  JWKS, issuer et audience `payment-api` contrôlés ;
- **ancien login payment-2** (`POST /api/v1/auth/login`, HMAC), conservé le temps de la bascule.

| Variable | Doit valoir |
|----------|-------------|
| `KEYCLOAK_ENABLED` | `true` (défaut) |
| `KEYCLOAK_ISSUER` | `http://localhost:8180/realms/payment` |
| `KEYCLOAK_JWK_SET_URI` | Optionnel, pour joindre Keycloak par une autre URL (ex. `http://keycloak:8080/...` en Docker) |
| `KEYCLOAK_AUDIENCE` | `payment-api` |
| `JWT_SECRET` | Le même secret que `gateway.security.jwt.secret` du gateway |
| `JWT_ISSUER` | Le même issuer (`payment-gateway` par défaut) |

## Lancer en local

```bash
# Base dédiée (port 5438)
docker compose up -d network-postgres

# Depuis la racine payment-2
./mvnw -pl payment-network -DskipTests package
java -jar payment-network/target/payment-network.jar --spring.profiles.active=local
```

Service sur `http://localhost:8096`. Flyway crée le schéma et le seed (`V1__network_schema.sql`), puis
ajoute les pays et place les FSE existants dessous (`V2__pays.sql` : FSE UEMOA au Burkina Faso, FSE CEMAC
au Cameroun, chemins et niveaux décalés d'un rang), enfin crée la table des zones et y relie les
unités (`V3__zones.sql`). `V4__regroupements_dans_arbre.sql` range les regroupements dans l'arbre :
l'ancien ancrage devient le parent ; un regroupement sans ancrage est placé sous l'unité commune la plus
basse de ses membres, et supprimé (avec un `NOTICE`) s'il n'en a pas, faute de regroupement à la racine.
C'est le cas de l'ancien « Pays du Sahel » (BF, ML, NE), remplacé dans la démo par « Équipe Sahel ».
Docker : `docker compose up -d network` (image construite par `payment-network/Dockerfile`).

La console payment-front l'appelle directement (menu Réseau) ; en développement, `proxy.conf.json`
envoie `/api/v1/network` sur le port 8096. En déploiement, le reverse proxy route
`/api/v1/network/**` vers ce service.

## Configuration

| Variable | Défaut |
|----------|--------|
| `NETWORK_DB_URL` | `jdbc:postgresql://localhost:5438/payment_network` |
| `NETWORK_DB_USER` / `NETWORK_DB_PASSWORD` | `network` / `network` |
| `SERVER_PORT` | `8096` |
| `spring.servlet.multipart.max-file-size` | `10MB` (fichier d'import) |
| `CORS_ORIGINS` | Origines locales des fronts (4200, 4210, 8100) |
| `KEYCLOAK_*`, `JWT_*` | Voir « Authentification » |

Le profil par défaut de `java -jar` est `prod` ; passez `--spring.profiles.active=local` en local.

## Tests

`./mvnw -pl payment-network test` : matrice de rattachement, détection des doubles couvertures,
zones (création, devise figée, désactivation), services (création de pays et d'unités, modification, déplacement, activation, membres pris dans la branche du parent, périmètre,
couvertures), contrôleurs (droits, format des erreurs) et intégration PostgreSQL 16 via
Testcontainers (migrations et seed, regroupements rangés dans l'arbre, nouvelle zone et ses pays, périmètre, déplacement de branche, position d'une unité, d'un pays et d'une zone, cycle par l'API, arbre chargé niveau par niveau, pagination). Lecture des fichiers d'import (CSV avec BOM et guillemets, Windows-1252, XLSX) et import en intégration : contrôle sans écriture puis application dans n'importe quel ordre, erreurs ligne par ligne sans écriture partielle, membres, modèles réimportables, et volume de 6 240 unités et 360 membres (durées affichées). Les tests
d'intégration sont ignorés si Docker n'est pas disponible ; le pom fixe `api.version=1.44` pour
Testcontainers 1.19 avec Docker Engine 29+.

## Données de démonstration

```
UEMOA
BF Burkina Faso
└── FSE-UEMOA
    ├── AG-OUAHIGOUYA
    │   ├── MS-SAHEL ── SD-SAHEL-01, SD-SAHEL-02
    │   │   └── RG-EQUIPE-SAHEL « Équipe Sahel » : SD-SAHEL-01, SD-SAHEL-02
    │   └── MC-MARCHE-OHG
    ├── AG-KAYA ── MS-KAYA
    ├── AG-BOBO ── MC-BOBO-STATION
    └── RG-AGENCES-NORD « Agences du Nord » : AG-OUAHIGOUYA, AG-KAYA
BJ, CI, GW, ML, NE, SN, TG (sans réseau)

CEMAC
CM Cameroun ── FSE-CEMAC ── AG-DOUALA ── MS-LITTORAL ── SD-LITTORAL-01
CF, CG, GA, GQ, TD (sans réseau)
```

« Agences du Nord » couvre ainsi les deux agences, MS-SAHEL et ses deux sous-distributeurs,
MC-MARCHE-OHG et MS-KAYA ; AG-BOBO et sa branche n'en font pas partie. « Équipe Sahel », rattaché à
MS-SAHEL, couvre ses deux sous-distributeurs : SD-SAHEL-01 est donc couvert par les deux regroupements.

**Jeu de volume.** `python3 scripts/reseau_volume.py --importer` génère et importe 6 240 unités sous FSE-UEMOA
(120 agences avec chacune un regroupement, 5 masters de 8 sous-distributeurs et 5 marchands) et 360 membres ; les
codes contiennent `-VOL` (`AG-VOL001`, `SD-VOL001-1-1`…). Sans `--importer`, il écrit seulement les deux CSV dans
`scripts/load/out`. Les requêtes de lecture (page de recherche, enfants, périmètre, position) restent sous 50 ms
avec ce jeu. Pour le retirer de la base locale, voir l'en-tête du script (deux `delete` sur les codes `%-VOL%`).
