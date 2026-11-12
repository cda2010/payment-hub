# Guide de configuration d’un nouveau produit

**Public :** équipe configuration payment-2 (`ADMIN`, `CONFIG_MANAGER`)  
**Objectif :** publier un produit catalogue exécutable de bout en bout (parcours, frais, règles, commission, offre identité), ou un produit hors paiement comme l’enrôlement ([§6.5](#65-enrôlement-enrolement)).

> Côté **client** d’un produit de paiement (web / mobile / BFF) : rien à coder — une fois le produit publié,  
> `POST /api/v1/gateway/products/{CODE}` suffit. Voir [`GUIDE-INTEGRATION-PRODUITS.md`](GUIDE-INTEGRATION-PRODUITS.md).  
> L’enrôlement se consomme par l’API identity (`/api/v1/identity/enrolement`), pas par ce POST.

Documents liés : [`CAPABILITIES.md`](CAPABILITIES.md), [`INTEGRATION.md`](INTEGRATION.md), [`GUIDE-VALIDATIONS-MULTI-NIVEAUX.md`](GUIDE-VALIDATIONS-MULTI-NIVEAUX.md), [`DOCUMENTATION-TECHNIQUE.md`](DOCUMENTATION-TECHNIQUE.md).

---

## 1. Principe

Un produit n’est **pas** une API Java dédiée. Il combine :

| Brique | Rôle |
|--------|------|
| **BPMN** (Flowable) | Enchaînement des étapes (`${catalogStepDelegate}`) |
| **Form.io** | Écrans de saisie |
| **Catalogue** (`CatalogDeService` + version) | Rôles, bindings UI, softRules, canaux, audiences |
| **Capabilities** | Verbes réutilisables (`CALCULATE_FEES`, `EVALUATE_RULES`, `EXECUTE_TRANSFER`…) |
| **Pricing** | Grille tarifaire (frais) |
| **Rules** | Plafonds / cumuls réglementaires (≠ softRules catalogue) |
| **Commission** | Répartition des droits entre acteurs |
| **Identity** | Profils d’offre SERVICE / TARIFF / LIMIT (qui peut consommer quoi, et quels plafonds) |
| **Alias / Network** | Comptes CBS/till/wallet, arbre agent, FSE (si besoin) |
| **DMN** | Décisions de configuration (pièces et profil de limite de l’enrôlement) |

Un produit de **paiement** suit le chemin live ci-dessous. Un produit **hors paiement** (`serviceType` `DEFAUT`, sans flow ni montant) s’arrête au catalogue, aux formulaires et à l’identité : l’enrôlement est décrit en [§6.5](#65-enrôlement-enrolement).

Chemin live typique au runtime :

```text
CALCULATE_FEES → payment-pricing
EVALUATE_RULES → payment-rules (réserve si ALLOW)
… exécution …
SUCCESS → confirm réservation + accrual commission
FAIL / CANCEL → release réservation
```

---

## 2. Prérequis

À préparer **avant** le produit (référentiels partagés) :

| Élément | Console | Notes |
|---------|---------|--------|
| Fournisseurs / APIs | `/admin/providers` | Participants des rôles `SOURCE`, `DESTINATION`, etc. |
| Formules de frais | `/admin/tarification/formules` | Avant toute grille |
| Réseau (si frais ciblés ou agents) | `/admin/reseau/arbre` | Unités, zones, regroupements |
| Profils d’approbation (si maker/checker) | `/admin/approval-profiles` | ACCOUNT_TRANSFER, RTGS, SWIFT |
| Facturiers (si facture) | `/admin/billers` | BILL_PAYMENT |

Services à démarrer : gateway `:8080`, pricing `:8095`, rules `:8099`, commission `:8098`, identity `:8094`, network `:8096`, alias `:8097`, documents `:8101` (copies d’identité), Keycloak, front `:4200`. Le mobile d’enrôlement tourne à part (`:4205`).

---

## 3. Checklist ordonnée

### Étape A — Processus BPMN + formulaires

1. Déployer le processus Flowable (clé = `workflowKey` du catalogue ; **jamais** auto-générée).
2. Sur chaque `serviceTask` :
   - `flowable:delegateExpression="${catalogStepDelegate}"`
   - `pay:capability` (obligatoire pour un produit neuf)
   - `pay:actorRole` si appel fournisseur
   - `pay:operation` seulement si différent du défaut de la capability
3. Créer / lier les formulaires Form.io (`formKey`).

Exemple :

```xml
<serviceTask id="CALCULATE_FEES"
             flowable:delegateExpression="${catalogStepDelegate}"
             pay:capability="CALCULATE_FEES"/>
```

Vocabulaire : `GET /api/v1/catalog/capabilities`  
Contrôle : `GET /api/v1/admin/catalog-services/versions/{versionId}/workflow/steps`

### Étape B — Catalogue produit

**UI :** `/admin/catalog-services`

1. Créer le produit (`code` stable, ex. `CASH_OUT`, `MY_PRODUCT`) + catégorie.
2. Ouvrir une **version DRAFT** et renseigner la configuration :

| Section | Contenu |
|---------|---------|
| `roles[]` | SOURCE, DESTINATION, PAYER, BILLER, BENEFICIARY, PROVIDER, AGENT_NETWORK… + cardinalité |
| `participants[]` | Fournisseurs par rôle |
| `bindings[]` | Par étape : UI / INVOKE / SOFT / WAIT / CAPABILITY + formKey, capability, params |
| `softRules[]` | REQUIRED_FIELD, MIN_AMOUNT, DEFAULT_FEE, GENERATE_CODE… — voir [§4](#4-ajouter-des-soft-rules-à-un-produit) |
| `supportedChannels` | WEB, MOBILE, AGENT, USSD… |
| `supportedAudiences` | RETAIL, CORPORATE, AGENT, MERCHANT… |
| `channelDefaults` | Surcharges par canal (peut surcharger `softRules`) |
| `approvalPolicy` | Si validation multi-niveaux |
| `history` + `notifications[]` | Libellés du relevé et modèles SMS / push — voir [Messages](#messages-historique-et-notifications) |

3. Valider le workflow (pas d’erreur `CATALOG_WORKFLOW_INVALID`).
4. **Publier** la version (`ADMIN` / `CONFIG_MANAGER`).

API alternative : `POST /api/v1/admin/catalog-services/versions/{id}/publish`  
(détail payload : [`INTEGRATION.md`](INTEGRATION.md#publier-un-produit-admin-sans-java)).

### Étape C — Capabilities métier clés

Dans le BPMN / bindings, placer au bon moment :

| Capability | Effet |
|------------|--------|
| `VALIDATE_AMOUNT` / `VALIDATE_ACCOUNT` | SoftRules locales |
| `CALCULATE_FEES` | Quote live **pricing** (softRule `DEFAULT_FEE` en secours) |
| `EVALUATE_RULES` | Évalue **rules** + réserve les cumuls si ALLOW |
| `EVALUATE_APPROVAL` | Circuit validation |
| `GENERATE_OTP` / `CONFIRM_PAYMENT` | OTP provider |
| `EXECUTE_TRANSFER` / `PAY_BILL` / … | Exécution provider |
| `GENERATE_CODE` + `WAIT` | Cash-out / cash-in agent |
| `NOTIFY` | Clôture + envoi du modèle dont le `code` correspond au statut (voir [Messages](#messages-historique-et-notifications)) |

Liste complète : [`CAPABILITIES.md`](CAPABILITIES.md).

### Étape D — Tarification (pricing)

**UI :** `/admin/tarification/grilles` → détail → `/admin/tarification/simulateur`

1. Créer une grille pour `produitCode` = code catalogue + canal + devise (+ cible réseau si besoin).
2. Choisir la **base de calcul** : `TRANSACTION_AMOUNT` (défaut), `CONVERTED_AMOUNT` (FX), `CUMUL_AMOUNT` / `CUMUL_COUNT` (période + scope obligatoires), ou `CUSTOM`.
3. Ajouter les **tranches** (min/max + formule + params — paliers dans l’unité de l’assiette).
4. Publier / valider la grille.
5. Simuler un montant représentatif.

Override parcours : `feeBaseAmount` ; cumuls : `aggregates` / `feeAggregates` (ex. `cumulAmount.MONTH.AGENT`).

### Étape E — Règles & limites (rules)

**UI :** `/admin/rules` → `/admin/rules/groupes/{code}` → `/admin/rules/simulateur`

1. Créer / compléter un **groupe** (ex. `CASH_OUT_STANDARD`).
2. Ajouter les règles (plafond journalier, cumul, solde…) et publier.
3. Dans le BPMN : étape `EVALUATE_RULES` avec `groupCode` (params binding ou contexte).
4. Simuler ALLOW / REJECT / REVIEW.

> SoftRules catalogue ≠ Rules BC. SoftRules = UX / contrôles locaux. Rules = réglementaire / cumuls.

### Étape F — Commissionnement

**UI :** `/admin/commission/grilles` → détail → `/admin/commission/simulateur`

1. Créer une grille **brouillon** (`operationType` aligné sur le produit, ex. `CASH_OUT`).
2. Ajouter les **règles** (bénéficiaire, formule FIXED/PERCENT/HYBRID, `paramsJson`, priorité).
3. Publier.
4. Simuler (montant + frais).

À la réussite réelle du paiement, le gateway **accrue** automatiquement les droits (si `gateway.bc.commission-enabled=true`).

### Étape G — Identité : profils d’offre

**UI :**

- Services : `/admin/identite/profils/services`
- Tarifs : `/admin/identite/profils/tarifs`
- Limites : `/admin/identite/profils/limites`
- Affectation : fiche party `/admin/identite/parties/{id}` ou UO `/admin/reseau/unites/{id}`

1. Profil **SERVICE** : autoriser le `serviceCode` (= code catalogue).
2. Profil **TARIFF** : lier `produitCode` → `grilleCode` pricing (+ canal).
3. Profil **LIMIT** : plafonds rattachés à une party (l’enrôlement en affecte un selon le choix, voir [§6.5](#65-enrôlement-enrolement)).
4. Affecter le(s) profil(s) aux parties / unités concernées (période de validité).

Sans profil SERVICE actif, l’acteur ne voit / ne démarre pas le produit via l’offre résolue.

### Étape H — Alias (si comptes / tills)

Pas de menu dédié : panneau **Alias** sur la fiche party ou unité.

Types usuels : `IBAN`, `WALLET`, `TILL`, `PHONE`, `INTERNAL`.

### Étape I — Recette

| Contrôle | Comment |
|----------|---------|
| Produit publié | `GET /api/v1/admin/catalog-services/published?audience=…` |
| Casting | `GET …/published/{code}/casting` |
| Parcours | `POST /api/v1/gateway/products/{CODE}` (JWT) |
| Frais | Étape CALCULATE_FEES → montant / feeGridCode dans le contexte |
| Rules | REJECT attendu si plafond dépassé ; ALLOW + réservation sinon |
| Commission | Après SUCCESS : `GET /api/v1/commission/droits?transactionId=…` |
| Supervision | `/admin/transactions` |

---

## 4. Ajouter des soft rules à un produit

Les **soft rules** sont des contrôles / effets **locaux** déclarés dans la configuration
catalogue (`configuration.softRules[]`). Elles ne remplacent pas les **Rules** BC
(plafonds / cumuls sur `payment-rules`).

Elles s’exécutent quand une capability locale de l’étape tourne
(`VALIDATE_AMOUNT`, `VALIDATE_ACCOUNT`, `SOFT_RULES`, `CALCULATE_FEES`, `GENERATE_CODE`…).

### 4.1 Prérequis BPMN

1. L’étape ciblée existe dans le BPMN (`serviceTask` avec un **id** stable, ex. `CHECK_COMPLIANCE`).
2. Cette étape porte une capability locale (`pay:capability="VALIDATE_ACCOUNT"` ou
   `SOFT_RULES`, `VALIDATE_AMOUNT`, etc.).
3. Les champs contrôlés (`field`, `skipFlag`…) existent dans le **contexte paiement**
   (saisis via Form.io ou passés au `POST /gateway/products/{CODE}`).

Sans soft rule, `VALIDATE_ACCOUNT` ne contrôle rien (avertissement à la publication :
`CATALOG_CONTROL_WITHOUT_RULES`).

### 4.2 Structure d’une entrée

```json
{
  "step": "CHECK_COMPLIANCE",
  "type": "BLOCKED_VALUES",
  "params": {
    "field": "countryCode",
    "blocked": ["KP", "IR", "SY"],
    "skipFlag": "skipComplianceCheck",
    "errorCode": "SWIFT_COMPLIANCE_BLOCKED"
  }
}
```

| Champ | Rôle |
|-------|------|
| `step` | Id exact de l’étape BPMN (doit matcher le `serviceTask`) |
| `type` | Type builtin (voir tableau ci-dessous) |
| `params` | Paramètres propres au type |

### 4.3 Types supportés et params

| Type | Params principaux |
|------|-------------------|
| `REQUIRED_FIELD` | `field` (requis), `errorCode`, `message` |
| `REGEX_FIELD` | `field`, `pattern` (requis), `uppercase`, `errorCode`, `message` |
| `BLOCKED_VALUES` | `field` (requis), `blocked` (liste), `skipFlag`, `errorCode` |
| `MIN_AMOUNT` | `minAmount` / `min`, ou `minAmountKey` (lit `configuration.minAmount`), `errorCode` |
| `BUSINESS_HOURS` | `zone`, `from`, `to`, `weekdaysOnly`, `skipFlag` (défaut `skipCutoffCheck`), `errorCode` |
| `DEFAULT_FEE` | `amount` / `fee` (sinon `configuration.defaultFee`) — secours si pricing live absent |
| `GENERATE_CODE` | `field` (défaut `cashOutCode`), `prefix` (défaut `CO`), `length` (4–12) |
| `PASS` | aucun |

Extension d’un **nouveau type** : implémenter `SoftRuleHandler` (`types()` + `apply`) comme
bean Spring dans `payment-soft-rules` — SPI `com.gateway.payment.softrules.spi`.

### 4.4 Comment les ajouter (API)

Sur une version **DRAFT** :

```http
PUT /api/v1/admin/catalog-services/versions/{versionId}/configuration
Authorization: Bearer … (ADMIN | CONFIG_MANAGER)
Content-Type: application/json
```

```json
{
  "configuration": {
    "minAmount": 100,
    "softRules": [
      {
        "step": "VALIDATE_BIC",
        "type": "REGEX_FIELD",
        "params": {
          "field": "bic",
          "pattern": "^[A-Z0-9]{8}([A-Z0-9]{3})?$",
          "uppercase": true,
          "errorCode": "SWIFT_BIC_INVALID",
          "message": "BIC/SWIFT invalide (8 ou 11 caractères)"
        }
      },
      {
        "step": "VALIDATE_BIC",
        "type": "REQUIRED_FIELD",
        "params": {
          "field": "iban",
          "errorCode": "SWIFT_IBAN_REQUIRED",
          "message": "IBAN bénéficiaire requis"
        }
      },
      {
        "step": "CHECK_COMPLIANCE",
        "type": "BLOCKED_VALUES",
        "params": {
          "field": "countryCode",
          "blocked": ["KP", "IR", "SY"],
          "skipFlag": "skipComplianceCheck",
          "errorCode": "SWIFT_COMPLIANCE_BLOCKED"
        }
      },
      {
        "step": "CHECK_LIMITS",
        "type": "MIN_AMOUNT",
        "params": {
          "minAmountKey": "minAmount",
          "errorCode": "SWIFT_MIN_AMOUNT"
        }
      }
    ]
  }
}
```

Puis publier :

```http
POST /api/v1/admin/catalog-services/versions/{versionId}/publish
```

> Une version déjà **PUBLISHED** est figée : créer une **nouvelle version DRAFT**,
> y reporter / enrichir `softRules`, puis republier.

### 4.5 Lien avec le démarrage produit

- `params.field` / `skipFlag` = **clés du contexte runtime**, pas des libellés Form.io.
- Ex. `"skipFlag": "skipComplianceCheck"` → si le body de start contient
  `"skipComplianceCheck": true`, la règle est court-circuitée (usage local / tests).
- Même idée pour RTGS : `"skipFlag": "skipCutoffCheck"` avec `BUSINESS_HOURS`.

### 4.6 Checklist

1. Id d’étape BPMN = `step` de la soft rule.
2. Capability locale sur cette étape.
3. Champ présent dans le formulaire / body (`field`).
4. `softRules` sur la version DRAFT → publish.
5. Smoke : `POST /api/v1/gateway/products/{CODE}` jusqu’à l’étape contrôlée.

Référence détaillée types / publish one-shot :
[`INTEGRATION.md`](INTEGRATION.md#publier-un-produit-admin-sans-java).

---

## 5. Ordre recommandé (schéma)

```text
Fournisseurs (+ réseau si besoin)
    → Formules pricing
    → BPMN + Form.io + CatalogDeService (DRAFT)
    → softRules + bindings + pay:capability
    → Messages (history + notifications) + traductions
    → Publier catalogue
    → Grille tarifaire (produitCode)
    → Groupe rules + EVALUATE_RULES dans le BPMN
    → Grille commission (operationType) + publier
    → Profils Identity SERVICE + TARIFF + LIMIT + affectations
    → Alias comptes / tills
    → [si besoin] Approbation multi-niveaux
    → Smoke gateway

Hors paiement (ENROLEMENT) :
    Catalogue publié (steps, bindings UI, pays)
        → Formulaires Form.io
        → DMN pièces + DMN profil de limite
        → Profils LIMIT existants
        → FSE Mobile Money / Banque
        → Smoke GET /api/v1/identity/enrolement/workflow
```

---

## 6. Variantes produit

### 6.1 Cash-out (`CASH_OUT`)

- Audience souvent `AGENT` / `RETAIL` ; journey `STANDARD_CASH_OUT`.
- Ordre typique (débit **avant** code) : montant → source → agent → **CALCULATE_FEES** → **EVALUATE_RULES** (`CASH_OUT_STANDARD`) → récap → OTP → débit → **GENERATE_CODE** (`cashOutCode`) → **WAIT** remise → **NOTIFY**.
- Pricing : grille `FEE-CASH-OUT` (`produitCode=CASH_OUT`) — **sans** softRule `DEFAULT_FEE`.
- Commission : grille `CASH_OUT-STD` (`operationType=CASH_OUT`).
- Alias **TILL** / wallet sur l’unité agent.
- Réseau agent (ex. `CORIS_MONEY`) : le code unité est vérifié dans payment-network
  (lignée FSE ↔ `codeFse` du provider, ex. `0011`) ; le récap affiche UO / FSE / zone.

### 6.2 Cash-in (`CASH_IN`)

- Audience `RETAIL` + `AGENT` ; journey `STANDARD_CASH_IN`.
- Ordre typique (code **avant** crédit) : montant → destination → agent → **CALCULATE_FEES** → **EVALUATE_RULES** (`CASH_IN_STANDARD`) → récap → OTP → **GENERATE_CODE** (`cashInCode`) → **WAIT** encaissement → crédit → **NOTIFY**.
- Pricing : grille `FEE-CASH-AGENT` (`produitCode=CASH_IN`) — **sans** softRule `DEFAULT_FEE` (sinon le live pricing est court-circuité).
- Commission : grille `CASH_IN-STD` (`operationType=CASH_IN`) ; accrual gateway envoie `operation=productCode`.
- Identity : profils SERVICE / TARIFF déjà seedés (AGENT, AGENCE, MASTER, MARCHAND…) ;
  retail (`RETAIL-STD` / `RETAIL-FEE`) inclut aussi `CASH_IN` / `CASH_OUT` pour le menu mobile client.

### 6.3 Virement compte (`ACCOUNT_TRANSFER`)

- Rôles SOURCE / DESTINATION.
- **EVALUATE_APPROVAL** + DMN + validateurs (≠ initiateur).
- Voir [`GUIDE-VALIDATIONS-MULTI-NIVEAUX.md`](GUIDE-VALIDATIONS-MULTI-NIVEAUX.md).
- OTP puis `EXECUTE_TRANSFER`.

### 6.4 Paiement facture (`BILL_PAYMENT`)

- Facturier + `GET_BILLS` / `PAY_BILL`.
- SoftRules sur références facture.

### 6.5 Enrôlement (`ENROLEMENT`)

Produit catalogue **hors paiement** : `serviceType` `DEFAUT`, aucun `payment_flow`, aucun montant. Les étapes C à F (capabilities de paiement, grille tarifaire, groupe rules, commission) ne le concernent pas.

**UI :** `/admin/catalog-services` — code `ENROLEMENT`, catégorie `ENROLEMENT`, version publiée `1.0`, `workflowKey` `enrolement`.

| Clé de configuration | Valeur |
|----------------------|--------|
| `serviceType` | `DEFAUT` |
| `journeyCode` / `workflow` | `ENROLEMENT` |
| `supportedChannels` / `defaultChannel` | `MOBILE` |
| `supportedAudiences` / `defaultAudience` | `RETAIL` |
| `defautPays` | `BF` |

Le mobile (page d’ouverture de compte, plugin auth) affiche les écrans renvoyés par :

```http
GET /api/v1/identity/enrolement/workflow
```

Cet appel est public. Identity lit la version **publiée** dans `payment_gateway`, puis le JSON Form.io de chaque binding `kind = UI`.

#### Écrans et formulaires

Les `steps` publiés : `PORTEFEUILLE`, `COMPTE`, `TELEPHONE`, `CODE`, `PROPOSER`, `PAYS`, `IDENTITE`, `PIECE`, `SUITE`, `ADRESSE`.

| Étape | formKey | Rôle |
|-------|---------|------|
| `PAYS` | `enrolement-pays` | Pays choisi en premier (grille de drapeaux) |
| `PORTEFEUILLE` | `enrolement-portefeuille` | « Avez-vous un compte Mobile Money ? » |
| `COMPTE` | `enrolement-compte` | « Avez-vous un compte bancaire ou Meso Finance ? » |
| `TELEPHONE` | `enrolement-telephone` | Indicatif (drapeau + code du pays) et numéro local |
| `CODE` | `enrolement-code` | OTP : il prouve la SIM |
| `IDENTITE` | `enrolement-identite` | « Identité personnelle » |
| `PROPOSER` | `enrolement-proposer` | « Créer un compte Coris Money ? » |
| `SUITE` | `enrolement-suite` | « Créer le compte maintenant » (`CREATION`) ou « Identifiez-vous » (`IDENTIFICATION`) |
| `ADRESSE` | `enrolement-adresse` | Adresse structurée, seulement après `IDENTIFICATION` |
| `PIECE` | `enrolement-piece` | Type de pièce, copies, selfie |

Variantes par pays (`providerCode` du binding) :

| Étape | Pays | formKey |
|-------|------|---------|
| `IDENTITE` | `CI` | `enrolement-identite-ci` |
| `IDENTITE` | `SN` | `enrolement-identite-sn` |
| `IDENTITE` | `GN` | `enrolement-identite-gn` |
| `IDENTITE` | `MA` | `enrolement-identite-ma` |
| `PIECE` | `GN` | `enrolement-piece-gn` |

Fichiers : `payment-channel-api/src/main/resources/modules/payment-admin-module/forms/enrolement-*.json`. Le titre Form.io est le titre d’écran (ex. « Identité personnelle »). Identity relit ces fichiers à chaque `GET /workflow`.

L’ordre des écrans est celui du workflow Java (`EnrolementWorkflow`), pas l’ordre du tableau `steps` :

```text
CODE → ENTRE            (origine Mobile Money déjà connu)
CODE → DOSSIER          (téléphone, banque ou meso : tout le monde passe par l'identité)
DOSSIER → PROPOSER
PROPOSER → SUITE | TERMINE
SUITE → PRET            (CREATION : ouvrir sans pièce)
SUITE → ADRESSE         (IDENTIFICATION)
ADRESSE → PIECE
PRET | PIECE → OUVERT
```

Ajouter un binding affiche un formulaire pour une étape **déjà** ouverte par ce workflow. Une nouvelle étape demande aussi une transition dans `EnrolementWorkflow`.

#### Pays (`configuration.pays`)

Chaque code ISO porte `nom`, `devise`, `maisons` (`MONEY`, `BANK`, `MESO` selon le pays) et `ouverture` :

- `champs` : identité exigée à l’enregistrement (`nom`, `prenom`, `dateNaissance`, `lieuNaissance`, `sexe`, `nationalite`, `numeroPiece`, `dateValidite`). `ville` et l’adresse libre n’y figurent plus : l’adresse est la page `ADRESSE`.
- `documents` : déclaration pays (faces historiques). Les faces réellement demandées à l’ouverture viennent du DMN pièces.

`CatalogueGateway` met `pays` en cache pour la vie du process identity : après un changement SQL du catalogue, **redémarrer identity**. Les formulaires, eux, sont relus à chaque appel.

Les champs d’identité du formulaire et ceux de `ouverture.champs` doivent porter les mêmes `key` / `code`. Le serveur refuse l’enregistrement si un champ `requis` du pays est absent, même si le formulaire ne l’affiche plus.

#### Adresse

Page `enrolement-adresse`, uniquement sur le chemin « Identifiez-vous », juste avant les copies et le selfie. Créer le compte maintenant, ou refuser le compte, saute cette page.

Requis côté serveur (clés du formulaire) : `countryCode`, `regionCode`, `provinceCode`, `communeCode`, `city`, `arrondissement`, `sector`, `district`, `street`. `countryCode` est un ISO à 2 lettres, prérempli avec le pays déjà choisi.

Facultatifs : `buildingNumber`, `building`, `floor`, `unit`, `postalCode`, `landmark`, `additionalInfo`, `latitude`, `longitude`. Latitude et longitude, si présentes, sont numériques. Il n’existe pas de référentiel géographique : région, province et commune sont des codes saisis.

#### Mobile Money, banque, indicatif

- FSE `famille = MOBILE_MONEY` : liste limitée à la branche du pays choisi (`code_fse` `0011` sous BF = Coris Money). Un pays sans FSE Mobile Money affiche « Aucun FSE n’est proposé pour ce choix. »
- FSE `famille = BANQUE` : banques et meso finance dans la même liste, sans filtre pays (`0101` Coris Bank, `0102` Coris Meso Finance).
- L’indicatif vient de `pays.indicatif`. L’écran montre le drapeau, un indicatif modifiable prérempli, puis le numéro **local**. `POST /enrolement/telephone` et `POST /enrolement/infos` envoient ce numéro local (ex. `70123456`) plus l’indicatif à part, pour qu’un numéro Money existant reste reconnu.
- Le code OTP prouve la SIM. En profil local, `enrollment.expose-code: true` le révèle ; le défaut production est `false`. Le PIN choisi en fin de parcours n’est pas persisté.

#### Pièces (DMN)

Fichier : `payment-channel-api/src/main/resources/modules/payment-admin-module/decisions/enrolement-pieces.dmn` (copie sous `rules/`).

1. `decision_EnrolementProfil` (FIRST) : pays + `typeDocument` → profil (`PASSEPORT`, `GUINEE` si pays `GN`, sinon `STANDARD`).
2. `decision_EnrolementPieces` (COLLECT) : profil → faces `RECTO` / `VERSO` / `SELFIE`, libellé, `requis`.

Le compte ne s’ouvre en identification que si chaque face requise est déposée dans payment-documents. Une table vide lève « Aucune pièce n'est paramétrée pour ce document ». Le DMN est chargé au démarrage d’identity : un changement de fichier demande un **redémarrage identity**.

#### Profil de limite (DMN + profils LIMIT)

Fichier : `…/decisions/enrolement-profil-limite.dmn`, décision `decision_EnrolementProfilLimite` (FIRST). Entrées `segment` + `choix` :

| Segment | Choix | Profil | KYC à l’ouverture |
|---------|-------|--------|-------------------|
| `RETAIL` | `REFUS` | `RETAIL-LIMITE` | `BASIC` (pas de wallet) |
| `RETAIL` | `CREATION` | `RETAIL-LIMITE-COMPTE` | `LIGHT` |
| `RETAIL` | `IDENTIFICATION` | `RETAIL-LIMITE-IDENTIFIE` | `FULL` |
| autre | autre | `RETAIL-LIMITE` | selon le chemin |

Ces codes doivent exister comme profils **LIMIT** (`/admin/identite/profils/limites`), tenant `CORIS_DEMO`. Le segment envoyé aujourd’hui est `RETAIL`. Changer une ligne du DMN change le profil affecté, sans modifier le mobile ; identity doit être redémarré pour recharger le fichier. Le groupe rules du paiement (`EVALUATE_RULES`) reste indépendant de ce profil.

#### Recette

| Contrôle | Comment |
|----------|---------|
| Produit publié | `GET /api/v1/admin/catalog-services/published` contient `ENROLEMENT` |
| Écrans | `GET /api/v1/identity/enrolement/workflow` : titres, champs, variante du pays |
| Pays | un code absent de `configuration.pays` est refusé (« Ce pays n'ouvre pas le produit d'enrôlement ») |
| Mobile Money | pays `BF` → FSE `0011` ; un autre pays sans branche → liste vide |
| Banque | `GET /api/v1/network/fse?famille=BANQUE` (public) |
| Refus | identité puis récap, consentement, PIN, fin sans wallet, KYC `BASIC` |
| Création immédiate | `CREATION` → pas d’adresse ni de pièce, KYC `LIGHT`, profil `RETAIL-LIMITE-COMPTE` |
| Identification | adresse puis pièces DMN, KYC `FULL`, profil `RETAIL-LIMITE-IDENTIFIE` |

`ENROLEMENT` n’a pas de `history` ni de `notifications` : l’étape `NOTIFY` ne tourne pas. Les textes d’écran sont les titres et libellés des formulaires Form.io. Les refus (numéro déjà Money, code incorrect, champ requis, pièce manquante) sont des messages du serveur. Le code OTP n’est pas un modèle catalogue.

---

## Messages (historique et notifications)

Carte **Historique & messages** sur la version, dans `/admin/catalog-services`. Une version publiée est figée : Modifier ouvre un brouillon.

Deux blocs, tous deux des templates `{{variable}}` (ou `${variable}`), résolus au moment de l’affichage ou de l’envoi. La langue par défaut est écrite dans `configuration` ; les autres langues partent dans `i18n_translation` (`entity_type` `CATALOG_DE_SERVICE_VERSION`). `Accept-Language` choisit le texte, avec repli sur la configuration.

### Historique (relevé)

| Champ | Rôle |
|-------|------|
| `kindLabel` | Type d’opération (« Retrait d'espèces », « Transfert »…) |
| `titleTemplate` | Titre de la ligne |
| `subtitleTemplate` | Sous-titre |

Variables utiles : `kind`, `party`, `channel`, `ref`, `agentDisplay`, `productLabel`, `statusLabel`, `when`, `amount`, `currency`, `paymentId`, `provider`. Un champ vide laisse l’heuristique du gateway.

Exemples déjà publiés :

| Produit | Titre | Notifications |
|---------|-------|----------------|
| `CASH_OUT` | `{{kind}} · via {{agentDisplay}}` | `SUCCESS`, `WAITING_CODE` (SMS) |
| `ACCOUNT_TRANSFER` | `{{kind}} · {{party}} · {{channel}}` | `SUCCESS`, `WAITING_APPROVAL` (SMS) |
| `BILL_PAYMENT` | `{{kind}} · {{party}} · {{ref}}` | `SUCCESS`, `WAITING_APPROVAL` (SMS) |

### Notifications

Chaque entrée :

| Champ | Rôle |
|-------|------|
| `code` | Événement, en majuscules : `SUCCESS`, `WAITING_CODE`, `WAITING_APPROVAL`… |
| `channelHint` | `SMS`, `PUSH` ou `ALL` (défaut `SMS`) |
| `title` | Optionnel |
| `body` | Corps du message |

L’étape BPMN `NOTIFY` publie `notification.requested`. Elle cherche le modèle dont le `code` vaut le statut du paiement (`notificationCode` ou `event` du binding, sinon le statut, sinon `SUCCESS`). À défaut de ce code, elle reprend le modèle `SUCCESS`. Sans aucun modèle, l’événement part sans corps.

Le corps est rendu avec le contexte du paiement : `amount`, `currency`, `paymentId`, `phoneNumber`, et les clés déjà posées par le parcours (`agentDisplay`, `beneficiaryName`, `billerName`, `billReference`, `withdrawalCode`…). Alias acceptés : `phone` / `msisdn` → `phoneNumber`, `transactionAmount` → `amount`, `currencyCode` → `currency`.

Canaux : `SMS` (et tout hint contenant `SMS` ou `PHONE`) journalise la demande ; `PUSH` de même. Le fournisseur SMS réel n’est pas branché : le message configuré est celui qui partira quand il le sera.

```json
{
  "history": {
    "kindLabel": "Retrait d'espèces",
    "titleTemplate": "{{kind}} · via {{agentDisplay}}",
    "subtitleTemplate": "{{statusLabel}} · {{when}}"
  },
  "notifications": [
    {
      "code": "SUCCESS",
      "channelHint": "SMS",
      "body": "Votre retrait de {{amount}} {{currency}} chez {{agentDisplay}} est confirmé. Réf. {{paymentId}}"
    },
    {
      "code": "WAITING_CODE",
      "channelHint": "SMS",
      "body": "Code de retrait : {{withdrawalCode}}. Présentez-le à {{agentDisplay}}."
    }
  ]
}
```

Traductions (langue autre que le défaut), mêmes chemins que l’admin :

- `configuration.history.kindLabel`
- `configuration.history.titleTemplate`
- `configuration.history.subtitleTemplate`
- `configuration.notifications[SUCCESS].body`
- `configuration.notifications[SUCCESS].title`

Même mécanisme pour d’autres libellés de la configuration : `configuration.roles[CODE].label`, `configuration.recap.title` / `subtitle` / `confirmLabel`, `configuration.recap.fields[clé].label`.

### Checklist

1. Étape `NOTIFY` présente dans le BPMN (capability `NOTIFY`).
2. Un modèle par statut que le client doit lire (`SUCCESS`, et `WAITING_CODE` ou `WAITING_APPROVAL` si le parcours s’y arrête).
3. Les `{{clés}}` existent dans le contexte à cette étape.
4. Enregistrer sur le brouillon, puis publier.
5. Smoke : après SUCCESS, l’événement porte le `body` rendu ; la ligne d’historique reprend `titleTemplate`.

---

## 7. Carte console admin

| Domaine | Chemins |
|---------|---------|
| Catalogue | `/admin/catalog-services` |
| Tarification | `/admin/tarification/grilles`, `…/formules`, `…/simulateur` |
| Commission | `/admin/commission/grilles`, `…/simulateur` |
| Rules | `/admin/rules`, `…/groupes/:code`, `…/simulateur` |
| Identité | `/admin/identite/parties`, `…/profils/services`, `…/profils/tarifs`, `…/profils/limites` |
| Décisions | `/dmn-designer` (pièces et profil de limite de l’enrôlement) |
| Réseau | `/admin/reseau/arbre`, `…/unites`, `…/zones` |
| Fournisseurs | `/admin/providers` |
| Approbations | `/admin/approval-profiles`, `/admin/approvals` |
| Transactions | `/admin/transactions` |

---

## 8. Pièges fréquents

| Symptôme | Cause probable |
|----------|----------------|
| `CATALOG_WORKFLOW_INVALID` à la publication | Capability / rôle inconnu, BPMN non déployé |
| Frais toujours à 0 | Pas de grille pricing, ou softRule `DEFAULT_FEE` absente, ou pricing down |
| Soft rule ne se déclenche pas | `step` ≠ id BPMN, ou capability locale absente sur l’étape |
| `SOFT_RULE_INVALID` | `field` / `pattern` manquant selon le type |
| `Ressource introuvable` sur commission/rules | Front démarré sans proxy à jour → redémarrer `ng serve` |
| Rules / commission non modifiables | Grille **PUBLIEE** → **Nouvelle version** (brouillon) |
| Produit invisible pour un agent | Profil SERVICE non affecté à la party / UO |
| `RULES_REJECTED` | Groupe / limites ; simuler dans `/admin/rules/simulateur` |
| Commission non accrues | `GATEWAY_COMMISSION_ENABLED=false` ou grille non PUBLIEE pour l’opération |
| Pays d’enrôlement ignoré après un SQL catalogue | Cache `CatalogueGateway` : redémarrer identity |
| Formulaire d’enrôlement introuvable | `formKey` sans fichier `enrolement-….json` dans le répertoire de formulaires |
| Pièce ou profil de limite inchangé | DMN relu seulement au démarrage d’identity |
| « Aucune pièce n'est paramétrée pour ce document » | DMN pièces : aucune ligne COLLECT pour le profil résolu |
| Identité refusée alors que l’écran est complet | `ouverture.champs` du pays exige une clé absente du formulaire |
| Adresse refusée | une des 9 clés requises manque, ou `countryCode` n’est pas un ISO à 2 lettres |
| SMS / push sans texte | Aucun modèle `notifications` pour le statut, et pas de repli `SUCCESS` |
| Relevé générique | `history` absent : l’heuristique gateway remplace les templates |
| Message dans la mauvaise langue | Traduction absente de `i18n_translation` pour ce chemin : repli sur le texte de la configuration |

---

## 9. Rôles et permissions

| Action | Rôles |
|--------|--------|
| Publier catalogue / grilles pricing / commission / rules | `ADMIN`, `CONFIG_MANAGER` |
| Compliance rules | souvent `COMPLIANCE` en plus |
| Exécuter un parcours | utilisateur canal authentifié (JWT Keycloak) |
| Offre identity | lecture authentifiée ; écriture profils = config |

---

## 10. Exemple minimal « MY_CASH_OUT »

1. Copier un BPMN cash-out existant → clé `myCashOut`.
2. Catalogue `MY_CASH_OUT` v1 DRAFT → binder `CALCULATE_FEES`, `EVALUATE_RULES`, `GENERATE_CODE`, `WAIT`.
3. Publier catalogue.
4. Grille pricing `produitCode=MY_CASH_OUT`, canal `AGENT`, devise `XOF` + 1 tranche.
5. Groupe rules `MY_CASH_OUT_LIMITS` + règle plafond journalier.
6. Grille commission `operationType=MY_CASH_OUT` (ou `CASH_OUT` si partagée) + règles de parts.
7. Profil SERVICE contenant `MY_CASH_OUT` → affecter à l’UO agent de test.
8. `POST /api/v1/gateway/products/MY_CASH_OUT` et parcourir jusqu’à SUCCESS.
