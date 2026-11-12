# payment-pricing

Moteur unifié **Frais · Taux · Limites**, déployé comme **application Spring Boot autonome**
avec sa propre base de données. Les frais sont calculés par le **moteur de tarification
configurable** décrit dans `Cahier_des_charges_moteur_tarification.docx` (phase 1 livrée :
modèle, formules, validation, calcul, simulation).

## Concepts

| Table | Rôle |
|-------|------|
| `formule` + `formule_parametre` | Catalogue versionné des formules et schéma de leurs paramètres |
| `grille_tarifaire` | Périmètre (produit, canal, devise, source, destination, période), version, statut, règle d'arrondi |
| `tranche_tarifaire` | Intervalle de montants, formule référencée, paramètres (JSONB), frais min/max |
| `pricing_rate` | Taux FX / marge (paire de devises, validité) |
| ~~`pricing_limit`~~ | **Retiré (V5)** — plafonds / cumuls → `payment-rules` |
| `pricing_policy` | Binding produit + canal + audience + pays + rail → taux, limites, payeur des frais |

## Source et destination

Une grille peut viser, de chaque côté de la transaction (source et destination, indépendamment) :

| Niveau | Saisie | Couvre |
|--------|--------|--------|
| `TOUTES` | rien | toutes les transactions, y compris sans ce côté renseigné |
| `ZONE` | `{"zone":"UEMOA"}` | toute unité de la zone |
| `UNITE` | `{"uniteId":"…"}` | l'unité et toute sa descendance ; pour un regroupement, ses membres et leur descendance |

L'unité peut être de n'importe quel type du réseau (payment-network) : pays, FSE, agence, master, marchand,
sous-distributeur ou regroupement. Elle doit être active ; sa zone est déduite (une zone saisie différente est refusée)
et son code, son libellé et son type sont copiés sur la grille (rafraîchis à la publication). Quand la source est
renseignée, la devise de la grille doit être celle de la zone source ; la destination est libre (transfert entre
zones possible). Un ensemble de plusieurs pays se décrit par un regroupement, une zone, ou une grille par pays.

Côté transaction, la position s'exprime par `uniteId`, `pays` (code ISO 2) ou `zone` ; le critère le plus précis
l'emporte. Une demande sans destination ne retient que les grilles sans destination. Le réseau n'est interrogé
(`GET /api/v1/network/positions`, token relayé, cache court) que si une grille candidate vise ce côté.

**Volume.** Les grilles peuvent descendre jusqu'au sous-distributeur, donc se compter par milliers. La base ne lit
que les candidates : à partir de la position, la requête retient les grilles dont la cible est `TOUTES`, la zone de
la position, ou l'une des unités de sa lignée ou des regroupements qui la couvrent (index partiels sur
`(produit, devise, statut, source_unite_id)` et idem pour la destination, migration `V4__selection_grilles.sql`).
Un calcul parmi 2 000 grilles ciblées lit ainsi 2 ou 3 grilles et prend quelques millisecondes (test d'intégration
`selectionEnBaseParmiDeuxMilleGrillesCiblees`). La grille garde aussi le parent de son unité (instantané pris à la
publication) pour repérer les grilles sœurs.

**Tarif actuel.** `POST /api/v1/frais/grille-applicable` prend le contexte d'un calcul sans montant (`produitCode`,
`canalCode`, `devise`, `dateCalcul`, `source`, `destination`) et répond `statut` = `TROUVEE` (`grille` retenue, et
dans `autres` jusqu'à 5 grilles moins précises qu'elle masque), `AUCUNE` ou `AMBIGUE` (grilles à égalité dans
`autres`), avec `voisinesSource` / `voisinesDestination` : nombre de grilles publiées qui visent une autre unité du
même type sous le même parent. Contrairement à `/frais/calculer`, un côté peut être un **regroupement** (unité
d'organisation de type `REGROUPEMENT`) : on lit alors la cible telle qu'elle est stockée, pour détecter une grille
déjà publiée sur ce même regroupement. Le formulaire de grille de la console s'en sert pour afficher, pendant la
saisie, la grille qui s'applique aujourd'hui au périmètre choisi (unité opérationnelle ou regroupement), prévenir
d'un `GRILLE_PERIMETRE_OCCUPE` avant la publication, et suggérer un regroupement quand plusieurs unités sœurs ont
chacune leur grille.

## Formules prédéfinies

Les taux sont en **forme décimale** : `0.015` = 1,5 %. Une interface peut afficher 1,5 % mais
transmet `0.015`.

| Code | Calcul | Paramètres (obligatoires en gras) |
|------|--------|-----------------------------------|
| `FIXED` | F = montantFixe | **montantFixe** |
| `PERCENT_TOTAL` | F = montant × taux + fraisFixes | **taux**, fraisFixes (défaut 0) |
| `AX_PLUS_Y` | F = montant × taux + fraisFixes | **taux**, **fraisFixes** |
| `EXCESS_AX_PLUS_Y` | 0 si montant ≤ seuil, sinon (montant − seuil) × taux + fraisFixes | **seuil**, **taux**, fraisFixes (défaut 0) |
| `EXCESS_CAPPED` | idem, puis min(…, plafond) | **seuil**, **taux**, **fraisFixes**, **plafond** |

Chaque version de formule a une implémentation Java (`tarif/formule/predefinies`) enregistrée
dans le `FormulaRegistry`. Au démarrage, `FormuleCatalogueVerifier` refuse de lancer
l'application si une formule prédéfinie active en base n'a pas d'implémentation, ou si son schéma
de paramètres (codes, types, obligation) diffère de celui de l'implémentation. Les formules de
type `EXPRESSION` (formule libre) sont prévues en phase 2 et refusées pour l'instant.

## Calcul des frais

Ordre de traitement (`MoteurFrais`) :

1. **Grille** : publiée (`PUBLIEE`), valide à la date de calcul (`date_debut ≤ date ≤ date_fin`),
   même produit et même devise, dont la source et la destination couvrent la transaction. Entre
   candidates, la plus précise l'emporte : d'abord sur la source, puis sur la destination, puis sur le
   canal (canal demandé avant « tous canaux »). Précision d'un côté : l'unité de l'opération, puis ses
   ascendants du plus proche au pays, puis la zone, puis « toutes » ; un regroupement se classe juste
   après le membre par lequel il couvre l'unité et avant le parent de ce membre. Plusieurs candidates au
   même niveau → `GRILLE_AMBIGUE`.
2. **Tranche** : celle qui couvre le montant, bornes incluses. Aucune → `AUCUNE_TRANCHE` ;
   le moteur ne choisit jamais une tranche arbitrairement.
3. **Paramètres** : revalidés contre le schéma de la version de formule (défauts appliqués aux
   seuls paramètres facultatifs).
4. **Formule** : calcul en `BigDecimal`, sans arrondi intermédiaire.
5. **Plancher puis plafond** de la tranche (`frais_min`, `frais_max`), si renseignés.
6. **Arrondi** de la grille : par défaut à l'unité, `HALF_UP` (8 001 → 100,015 → 100 FCFA).

La réponse indique la grille et sa version, la tranche, la formule et sa version, les paramètres
effectifs, la base taxable, le calcul intermédiaire, l'ajustement plancher/plafond éventuel,
l'arrondi, les frais finaux et, pour la source et la destination, la correspondance retenue
(`niveau`, `cible`, `via` = membre par lequel un regroupement couvre l'unité). Aucun montant n'est écrit dans les logs ; la métrique
`pricing.frais.calcul` (tag `issue` = `OK` ou code d'erreur) mesure la latence.

## API

Swagger : `http://localhost:8095/swagger-ui.html`. Toutes les routes exigent un token.

| Méthode | Route | Rôle requis |
|---------|-------|-------------|
| POST | `/api/v1/frais/calculer`, `/api/v1/frais/grille-applicable` (tarif actuel, sans montant) | authentifié |
| GET | `/api/v1/formules`, `/api/v1/formules/{code}/versions/{version}` | authentifié |
| POST | `/api/v1/formules/{code}/versions/{version}/essais` (essai de paramètres, sans écriture ni arrondi de grille) | authentifié |
| GET | `/api/v1/grilles` (filtres `code`, `libelle`, `produit`, `canal`, `devise`, `source`, `destination` (code de zone ou d'unité), `statut`, `date` ; `page` (0 par défaut), `taille` (50 par défaut, 200 au plus) ; réponse `{contenu, total, page, taille}` triée par code puis version décroissante), `/api/v1/grilles/{id}` | authentifié |
| POST | `/api/v1/grilles/{id}/valider`, `/api/v1/grilles/{id}/simulations` | authentifié |
| POST / PUT | `/api/v1/grilles`, `/api/v1/grilles/{id}` | `ADMIN` ou `CONFIG_MANAGER` |
| POST / PUT / DELETE | `/api/v1/grilles/{id}/tranches[/{trancheId}]`, `…/{trancheId}/dupliquer` | `ADMIN` ou `CONFIG_MANAGER` |
| POST | `/api/v1/grilles/{id}/versions` (nouvelle version en brouillon), `/api/v1/grilles/{id}/publier` | `ADMIN` ou `CONFIG_MANAGER` |
| POST | `/api/v1/pricing/quote` | authentifié |
| GET | `/api/v1/pricing/rates`, `/policies` | authentifié |

```bash
curl -s -X POST http://localhost:8095/api/v1/frais/calculer \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"produitCode":"TRANSFERT","canalCode":"MOBILE_MONEY","devise":"XOF","montant":20000}'
# → grilleCode TRF_STD v1, tranche 8001–25000, EXCESS_AX_PLUS_Y, baseTaxable 12000,
#   details "(20000 - 8000) * 0.015 + 100", frais 280

# Avec la position de la transaction (unité source, pays de destination)
curl -s -X POST http://localhost:8095/api/v1/frais/calculer \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"produitCode":"TRANSFERT_INTL","devise":"XOF","montant":20000,
       "source":{"uniteId":"00000000-0000-0000-0000-000000000031"},"destination":{"pays":"CM"}}'
```

**Administration (phase 1).** Une grille est créée en `BROUILLON` (version 1) et seul un
brouillon est modifiable ; toute écriture sur une grille d'un autre statut renvoie 409
`GRILLE_NON_MODIFIABLE`. À l'enregistrement, chaque tranche est contrôlée seule (bornes, frais,
paramètres) ; les tranches sont renumérotées par montant minimum. Les contrôles entre tranches
(chevauchements, trous si `continuiteRequise`, tranche ouverte ailleurs qu'en dernier) sont
rapportés par `POST /valider`, ce qui permet une saisie progressive. La simulation applique une
grille quel que soit son statut et n'écrit rien.

**Modifier une grille publiée.** `POST /{id}/versions` copie la grille (en-tête et tranches) en
brouillon `v+1`, même code ; s'il existe déjà un brouillon pour ce code, il est renvoyé tel quel.
Le code ne change plus à partir de la v2 (409 `GRILLE_CODE_FIGE`). `POST /{id}/publier` passe le
brouillon en `PUBLIEE` s'il ne présente aucune anomalie (sinon 400 `CONFIGURATION_INVALIDE` avec
les anomalies) et si aucune grille publiée d'un autre code ne couvre le même produit, canal, devise, source et
destination sur une période qui recoupe la sienne (409 `GRILLE_PERIMETRE_OCCUPE`). Les versions
publiées du même code qui commencent avant lui s'arrêtent la veille de sa date de début ; les
autres passent en `DESACTIVEE`. Le circuit maker-checker (`A_VALIDER`, `VALIDEE`) reste prévu en
phase 2.

**Devis.** `POST /api/v1/pricing/quote` délègue les frais au moteur (produit = `productCode`,
canal = `channel`, devise = `currency`, date du jour), avec `source` et `destination` facultatifs (même
forme que pour le calcul). Sans grille applicable, les frais sont
nuls (« Sans frais ») ; toute autre erreur du moteur devient une violation et `allowed=false`.
La réponse porte `feeGridCode`, `feeGridVersion` et `formulaCode`. Le payeur des frais
(`SENDER` / `RECIPIENT`) est porté par la policy.

**Erreurs.** Format `ApiError` de payment-2 (`timestamp`, `status`, `code`, `message`, `path`),
avec `details` pour les erreurs de configuration : chaque détail donne `code`, `message`,
`tranche` (ordre) et `champ` (ex. `parametres.taux`).

| Statut | Codes |
|--------|-------|
| 400 | `REQUETE_INVALIDE`, `REQUETE_ILLISIBLE`, `CONFIGURATION_INVALIDE`, `MONTANT_INVALIDE`, `CONTEXTE_INVALIDE` (position inconnue ; sur `/frais/calculer` seulement : regroupement donné comme unité de l'opération) |
| 404 | `GRILLE_INEXISTANTE`, `TRANCHE_INEXISTANTE`, `FORMULE_INEXISTANTE` |
| 409 | `GRILLE_NON_MODIFIABLE`, `GRILLE_CODE_EXISTANT`, `GRILLE_CODE_FIGE`, `GRILLE_PERIMETRE_OCCUPE` |
| 422 | `AUCUNE_GRILLE_APPLICABLE`, `GRILLE_AMBIGUE`, `AUCUNE_TRANCHE`, `TRANCHES_CHEVAUCHEMENT`, `CONFIGURATION_INVALIDE` (configuration publiée invalide), `FORMULE_NON_IMPLEMENTEE` |
| 503 | `RESEAU_INDISPONIBLE` (payment-network injoignable) |

Codes de validation (`details[].code`) : `FORMULE_INCONNUE`, `FORMULE_INACTIVE`,
`FORMULE_NON_SUPPORTEE`, `PARAM_NON_DECLARE`, `PARAM_OBLIGATOIRE_MANQUANT`,
`PARAM_TYPE_INVALIDE`, `PARAM_HORS_BORNES`, `PARAM_PRECISION`, `PARAM_VALEUR_NON_AUTORISEE`,
`PARAM_FORMAT_INVALIDE`, `PARAM_DEFAUT_INVALIDE`, `GRILLE_SANS_TRANCHE`,
`GRILLE_DATES_INVALIDES`, `TRANCHE_BORNES_INVALIDES`, `TRANCHE_FRAIS_INVALIDES`,
`TRANCHES_CHEVAUCHEMENT`, `TRANCHE_OUVERTE_NON_DERNIERE`, `TRANCHES_DISCONTINUES`. Cibles
(`champ` = `source.uniteId`, `destination.zone`, `devise`…) : `CIBLE_INCONNUE`, `CIBLE_INACTIVE`,
`CIBLE_ZONE_INCOHERENTE`, `CIBLE_DEVISE_INCOHERENTE`.

## Décisions de conception

- **Paramètres en JSONB** sur la tranche, seule source de vérité (pas de table
  `configuration_parametre`). Les nombres sont relus en `BigDecimal`
  (`DecimalJsonFormatMapper`) ; un `double` est toujours refusé.
- **Bornes** en `NUMERIC(18,2)`, au plus 2 décimales. Deux tranches sont contiguës si la seconde
  commence une unité après la fin de la première ; l'unité est la plus petite décimale utilisée
  par les bornes de la grille (1 pour 8000 / 8001, 0,01 pour 50000 / 50000.01).
- **Taux** : 6 décimales au plus, entre 0 et 1. **Montants de paramètres** : 2 décimales, ≥ 0.
- **Rôles** : en phase 1, écriture `ADMIN` / `CONFIG_MANAGER`. Les rôles distincts
  consultation / modification / validation / publication (ENF-001) arrivent avec le workflow.

## Prochaines phases

- **Phase 2** : workflow `BROUILLON → A_VALIDER → VALIDEE → PUBLIEE → DESACTIVEE` avec
  maker-checker, nouvelle version d'une grille publiée, journal `audit_tarification`,
  historique, rôles `TARIF_*`, cache des grilles publiées ; formules `EXPRESSION` (désactivées
  par défaut, interpréteur à liste blanche).
- **Phase 3** : écrans A à F dans payment-front.
- **Phase 4** : branchement du gateway sur `/api/v1/frais/calculer`.

## Authentification

Pas de base utilisateurs : nom et rôles viennent du token, choisi d'après le claim `iss` :

- **Keycloak** (realm `payment`, voir `../keycloak/README.md`) : signature RS256 vérifiée via le
  JWKS, issuer et audience `payment-api` contrôlés ;
- **ancien login payment-2** (`POST /api/v1/auth/login`, HMAC), conservé pour paymentHub2.

| Variable | Doit valoir |
|----------|-------------|
| `KEYCLOAK_ENABLED` | `true` (défaut) |
| `KEYCLOAK_ISSUER` | `http://localhost:8180/realms/payment` (issuer public des tokens) |
| `KEYCLOAK_JWK_SET_URI` | Optionnel, pour joindre Keycloak par une autre URL (ex. `http://keycloak:8080/...` en Docker) |
| `KEYCLOAK_AUDIENCE` | `payment-api` |
| `JWT_SECRET` | Le même secret que `gateway.security.jwt.secret` du gateway |
| `JWT_ISSUER` | Le même issuer (`payment-gateway` par défaut) |

## Lancer en local

```bash
# Base dédiée (port 5437)
docker compose up -d pricing-postgres

# Depuis la racine payment-2
./mvnw -pl payment-pricing spring-boot:run          # profil local par défaut via Maven
# ou
./mvnw -pl payment-pricing -DskipTests package
java -jar payment-pricing/target/payment-pricing.jar --spring.profiles.active=local
```

Service sur `http://localhost:8095`. Flyway crée le schéma et le seed :
`V1__pricing_schema.sql` (taux, limites, policies, anciennes grilles) puis
`V2__moteur_tarification.sql` (catalogue de formules, grilles du moteur, suppression des
anciennes tables de frais), puis `V3__cibles_reseau.sql` (source et destination des grilles), puis
`V4__selection_grilles.sql` (parent de l'unité visée, index de sélection des grilles ciblées).

Docker : `docker compose up -d pricing` (image construite par `payment-pricing/Dockerfile`).

## Tests

`./mvnw -pl payment-pricing test` : formules (TA-001 à TA-005), validation des paramètres et des
grilles (TA-006 à TA-009, TA-013, TA-014), moteur (TA-002, TA-010, TA-015, priorité de canal,
ambiguïté, plancher/plafond), ciblage source/destination (précision, regroupements, réseau indisponible), service (TA-012), contrôleurs (droits, format des erreurs) et
intégration PostgreSQL 16 via Testcontainers (migrations, JSONB, TA-011, devis, cycle par l'API, grille ciblée,
sélection parmi 2 000 grilles ciblées) ; 116 tests.
Le test d'intégration est ignoré si Docker n'est pas disponible. Le pom fixe
`api.version=1.44` pour Testcontainers 1.19, dont l'API Docker par défaut est refusée par
Docker Engine 29+.

## Configuration

| Variable | Défaut |
|----------|--------|
| `PRICING_DB_URL` | `jdbc:postgresql://localhost:5437/payment_pricing` |
| `PRICING_DB_USER` / `PRICING_DB_PASSWORD` | `pricing` / `pricing` |
| `SERVER_PORT` | `8095` |
| `CORS_ORIGINS` | Origines locales des fronts (4200, 4210, 8100) |
| `NETWORK_URL` | `http://localhost:8096` (payment-network ; `http://network:8096` en Docker) |
| `NETWORK_CACHE_TTL` / `NETWORK_TIMEOUT` | `PT1M` / `PT3S` |
| `KEYCLOAK_*`, `JWT_*` | Voir « Authentification » |

Comme pour le gateway, le profil par défaut de `java -jar` est `prod` ; passez
`--spring.profiles.active=local` en local.

## Relation avec le gateway

Le gateway n'embarque plus ce module et ne sert plus `/api/v1/pricing/**`. La console
payment-front l'appelle directement (menu Tarification) ; en développement, `proxy.conf.json`
envoie ces routes sur le port 8095. Les frais des transferts restent calculés par le gateway (softRules
`DEFAULT_FEE`, tables `fee_rule` / `transfer_fee_policy`) ; le branchement sur
`/api/v1/frais/calculer` est prévu en phase 4. En déploiement, le reverse proxy route
`/api/v1/pricing/**`, `/api/v1/frais/**`, `/api/v1/grilles/**` et `/api/v1/formules/**` vers
ce service.

## Seed et reprise des données

Grilles publiées au départ (XOF, valides depuis le 1er janvier 2026) :

| Grille | Produit / canal | Origine |
|--------|-----------------|---------|
| `TRF_STD` | `TRANSFERT` / `MOBILE_MONEY` | Grille de démonstration du cahier (§3.2) |
| `FEE-DOMESTIC` | `ACCOUNT_TRANSFER` / `WEB` | Conversion de l'ancienne grille |
| `FEE-RTGS` | `RTGS` / `WEB` | idem |
| `FEE-SWIFT` | `SWIFT` / `WEB` | idem |
| `FEE-BILL` | `BILL_PAYMENT` / tous canaux | idem |
| `FEE-CASH-AGENT` | `CASH_IN` / tous canaux | idem |

La conversion reprend les tranches, montants, planchers et plafonds des anciennes grilles
(pourcentages passés en forme décimale, bornes `50000.01` ramenées à `50001` en XOF). Deux
différences : le périmètre d'une grille ne comporte plus l'audience, donc `FEE-CASH-AGENT`
s'applique à tout `CASH_IN` ; et les frais sont arrondis à l'unité au lieu de 2 décimales.

Les tables `pricing_*` de la base du gateway (créées par `V112__pricing_engine.sql`) ne sont
plus lues. Taux, limites et policies modifiés dans le gateway se reprennent par dump :

```bash
pg_dump -h <gateway-db> -U gateway -d payment_gateway --data-only \
  -t pricing_rate -t pricing_policy > pricing-data.sql
```

Le dump s'applique sur une base `payment_pricing` dont le seed a été vidé (mêmes UUID) ; la
colonne `fee_schedule_code` du dump est à retirer (remplacée par `fee_bearer`). Des grilles de
frais modifiées dans le gateway se recréent comme grilles du moteur via l'API.
