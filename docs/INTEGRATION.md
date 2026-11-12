# Guide d’intégration — systèmes externes

Contrat **produit** pour brancher checkout, facture et account transfer dans une plateforme tierce — **sans recopier le BFF paymentHub2**.

Base URL locale : `http://localhost:8080`  
Swagger : `/swagger-ui.html` → tag **Product Gateway**

**Postman** : importer  
[`docs/postman/Payment-2-Product-Gateway.postman_collection.json`](postman/Payment-2-Product-Gateway.postman_collection.json)  
(+ env [`Payment-2-Local.postman_environment.json`](postman/Payment-2-Local.postman_environment.json)).  
Dossiers : Auth → Catalogue → Checkout → Bill payment → Account transfer → RTGS → SWIFT (+ approve).

> Référence d’UI : [paymentHub2](../../monApp/paymentHub2/docs/PAYMENT_GATEWAY.md) consomme le même style d’API (écran + actions).  
> Les endpoints bas niveau `/api/v1/payments` restent disponibles ; **préférez `/api/v1/gateway`**.

---

## 1. Surface recommandée : `/api/v1/gateway`

```
Votre UI / BFF
      │  JWT Bearer
      ▼
POST /api/v1/gateway/products/{CODE}   ← générique (nouveau produit = zéro Java)
  ou alias /checkout|/bill-payment|/account-transfer|/rtgs|/swift
      │
      ▼
IntegrationScreenResponse  (type, formKey, formSchema, actions, meta.paymentId, …)
```

**Nouveau produit sans code** :
1. BPMN + forms déployés
2. `POST /api/v1/admin/catalog-services/products/publish` (config + `softRules` + `gateway` + hôte)
3. `POST /api/v1/gateway/products/{CODE}`

---

## 2. Endpoints

### Catalogue

| Méthode | URL | Rôle |
|---------|-----|------|
| `GET` | `/api/v1/gateway/providers?product=checkout\|bill-payment\|account-transfer\|rtgs\|swift` | Providers |
| `GET` | `/api/v1/gateway/transfer-destinations` | Destinations transfert |
| `GET` | `/api/v1/gateway/biller-categories` | Catégories facturiers |
| `GET` | `/api/v1/gateway/billers?category=&q=&page=&size=` | Facturiers (page filtrable) |
| `GET` | `/api/v1/gateway/billers/{idOuCode}/products` | Produits du facturier (UUID **ou** code) |
| `GET` | `/api/v1/gateway/biller-products?billerCode=` | Idem, filtre explicite par code |
| `GET` | `/api/v1/gateway/bills/search?billerCode=&productCode=&subscriberReference=` | Recherche factures |
| `GET` | `/api/v1/gateway/products/{code}/catalog-layout?channel=MOBILE` | Composition UI (`displayKind` + `dataSource`) |
| `GET` | `/api/v1/gateway/products/{code}/catalog?channel=&category=&q=&page=&size=` | Catalogue matérialisé (sections + items) |

#### Presentation catalogue (`configuration.presentation`)

Le produit catalogue peut déclarer la **composition UI** (mobile) sans CSS :

```json
{
  "presentation": {
    "version": 1,
    "sections": [
      {
        "id": "favorites",
        "displayKind": "SHORTCUT_SLIDE",
        "dataSource": "BILL_FAVORITES",
        "origin": "LOCAL",
        "title": "Vos favoris",
        "items": []
      },
      {
        "id": "categories",
        "displayKind": "FILTER_CHIPS",
        "dataSource": "BILLER_CATEGORIES",
        "origin": "LOCAL",
        "bindsTo": "billers"
      },
      {
        "id": "billers",
        "displayKind": "LEAF_LIST",
        "dataSource": "BILLERS",
        "origin": "LOCAL",
        "searchable": true,
        "pageable": true
      }
    ]
  }
}
```

`displayKind` : `FILTER_CHIPS` | `SHORTCUT_SLIDE` | `LEAF_LIST` | `SECTIONED_LIST` | `ENTRY_GRID` | `DRILL_NAV`  
`dataSource` : clé de jeu (`BILLER_CATEGORIES` | `BILLERS` | `BILL_FAVORITES` | `STATIC_ITEMS` | `TRANSFER_PARTICIPANTS` | `CATALOG_PARTICIPANTS` | `TRANSFER_BENEFICIARIES` | `SWIFT_EMITTER_BANKS` | clé libre)  
`origin` : `LOCAL` (handlers Spring / services payment-2) | `OMNICANAL` (jambe FSE `typeMethode=DATASOURCE`) | `GATEWAY` (Product Gateway d'une app peer via module `payment-data-sources`)  

Pour `origin: OMNICANAL`, renseigner `params.codeFse` (ou `providerCode`) et optionnellement `params.dataKey` ; la réponse FSE (`items[]`) est mappée en `CatalogItemDto`.

Pour `origin: GATEWAY`, le code doit correspondre à une entrée `gateway.data-sources.apps.<CODE>` (base-url + list-path). Alias Formio acceptés : `REMOTE_APP`, `REMOTE`, `PEER`.

#### Module `payment-data-sources`

Appelle **directement** le gateway HTTP d'une autre app (établissements, …) — hors Omnicanal.

```yaml
gateway:
  data-sources:
    apps:
      ESTABLISHMENTS:
        enabled: true
        label: Établissements
        base-url: ${ESTABLISHMENTS_GATEWAY_URL:http://localhost:8101}
        list-path: /api/v1/gateway/establishments
        auth-type: none   # none | bearer | api-key
```

Formio :

```json
{
  "type": "select",
  "key": "etablissement",
  "properties": {
    "rfDataSource": {
      "code": "ESTABLISHMENTS",
      "origin": "GATEWAY"
    }
  }
}
```

Test API : `GET /api/v1/gateway/data-sources/ESTABLISHMENTS?origin=GATEWAY`

Si `presentation` est absent pour `BILL_PAYMENT`, le gateway applique un layout défaut. Overlay canal : `channelDefaults.MOBILE.presentation` / `channelDefaults.WEB.presentation` (canaux : MOBILE, WEB, GAB, USSD, BACKOFFICE).

Admin UI : **Catalogue de services** → carte **Présentation catalogue** (base + onglets par canal).

#### Sources de données (API directe / Formio)

Les mêmes sources alimentent le catalogue **et** les selects Formio. Aucun client n’appelle directement une app tierce : tout passe par payment-2, puis LOCAL, Omnicanal ou **GATEWAY** (peer).

| Méthode | URL | Rôle |
|---------|-----|------|
| `GET` | `/api/v1/gateway/data-sources` | Inventaire des codes (designer RapidFrame) |
| `GET` | `/api/v1/gateway/data-sources/{code}` | Résolution → `{ code, origin, items[], hasMore, params }` |

Query params utiles : `origin` (`LOCAL`\|`OMNICANAL`\|`GATEWAY`), `category`, `q`, `page`, `size`, `productCode` (requis pour `CATALOG_PARTICIPANTS`), `role`, `codeFse`, `providerCode`, `dataKey`, `limit`.

Exemple Formio (déclaratif — hydraté serveur via `properties.rfDataSource`) :

```json
{
  "type": "select",
  "key": "sourceProvider",
  "properties": {
    "rfDataSource": {
      "code": "TRANSFER_PARTICIPANTS",
      "origin": "LOCAL",
      "params": { "role": "SOURCE" }
    }
  }
}
```

#### OnChange Formio — deux mécanismes (contrat **multi-clients**)

Le schéma Formio + l’API gateway sont la **source de vérité**. Chaque client
(mobile, payment-front, hubs RapidFrame, GAB…) doit implémenter le **même**
contrat — pas de logique métier spécifique app.

| Client | A. Formio natif | B. Cascade `dependsOn` | C. `rfShowWhen` |
|--------|-----------------|------------------------|-----------------|
| payment-front (Formio) | oui | oui | — |
| payment-front (PrimeNG produit) | — | oui | oui |
| mobile-app (`p2-dynamic-form`) | — | oui | oui |
| RapidFrame `rf-formio-host` | oui | oui (`RF_DATA_SOURCE_RESOLVER` / `createHttpDataSourceResolver`) | — |

**Algo client B (portable)** — à recopier / partager :

1. Lire `properties.rfDataSource` (ou `rfDataSource`) sur chaque select
2. Si `dependsOn` non vide → **ne pas** attendre d’options serveur au load
3. Au changement d’un parent ∈ `dependsOn` :
   - construire query = `params` + `paramsFrom` (valeur form → query)
   - `GET /api/v1/gateway/data-sources/{code}?origin=…&…`
   - remplacer `options` / `data.values`
   - vider la valeur du champ dépendant
4. Util utilitaire de référence :
   - RapidFrame : `@rapidframe/formio-designer` (`rf-datasource.util`, token `RF_DATA_SOURCE_RESOLVER`)
   - `payment-front/.../rf-datasource.util.ts` et `mobile-app/.../rf-datasource.util.ts` (même API)

**A. Logique Formio native** (renderer Form.io / `rf-formio-host`) :

| Propriété | Rôle |
|-----------|------|
| `customConditional` | show/hide |
| `calculateValue` | calculer une valeur |
| `logic[]` | actions au trigger |
| `redrawOn` | redessiner un champ dépendant |

**B. Cascade data-source** (`dependsOn`) — re-fetch via `GET /data-sources/{code}` :

```json
{
  "type": "select",
  "key": "sourceAccount",
  "properties": {
    "rfDataSource": {
      "code": "SWIFT_SOURCE_ACCOUNTS",
      "origin": "LOCAL",
      "dependsOn": ["sourceRail"],
      "paramsFrom": { "providerCode": "sourceRail" }
    }
  }
}
```

**C. Show/hide allégé** (clients sans moteur Formio — PrimeNG / Ionic) :

```json
"properties": {
  "rfShowWhen": { "field": "sourceRail", "notEmpty": true }
}
```

Si `dependsOn` est présent, le serveur **n’hydrate pas** les options au chargement (le client les charge après sélection du parent).

Extension : un bean Spring `com.gateway.payment.datasources.spi.CatalogDataSourceHandler` par code LOCAL
(ex. package `catalog.presentation.datasource.local` dans payment-application, dont
`SWIFT_EMITTER_BANKS` pour les banques émettrices du casting SWIFT) ; pour une app peer,
`origin: GATEWAY` + YAML ; pour un FSE, `origin: OMNICANAL` + `params.codeFse`.
Le SPI / resolve / DTOs vivent dans le module `payment-data-sources` (`com.gateway.payment.datasources`).

Exemple SWIFT émetteur :

```json
{
  "type": "select",
  "key": "sourceBank",
  "properties": {
    "rfDataSource": {
      "code": "SWIFT_EMITTER_BANKS",
      "origin": "LOCAL"
    }
  }
}
```

`GET /api/v1/gateway/data-sources/SWIFT_EMITTER_BANKS?origin=LOCAL` — charge le casting
produit `SWIFT`, filtre `role=SOURCE`.

### Produits

| Méthode | URL | Produit |
|---------|-----|---------|
| `POST` | `/api/v1/gateway/products/{code}` | **Générique** — tout produit PUBLISHED |
| `POST` | `/api/v1/gateway/checkout` | Alias CHECKOUT |
| `POST` | `/api/v1/gateway/bill-payment` | Alias BILL_PAYMENT |
| `POST` | `/api/v1/gateway/account-transfer` | Alias ACCOUNT_TRANSFER |
| `POST` | `/api/v1/gateway/rtgs` | Alias RTGS |
| `POST` | `/api/v1/gateway/swift` | Alias SWIFT |
| `POST` | `/api/v1/gateway/cash-out` | Alias CASH_OUT |
| `POST` | `/api/v1/gateway/cash-in` | Alias CASH_IN |
| `POST` | `/api/v1/gateway/link` | Alias LINK (liaison compte) |
| `POST` | `/api/v1/gateway/register` | Alias REGISTER → LINK |
| `GET` | `/api/v1/gateway/transactions` | Historique (page filtrable, `payment_transaction`) |
| `GET` | `/api/v1/gateway/payments/{paymentId}` | Snapshot écran |
| `POST` | `/api/v1/gateway/payments/{paymentId}/actions` | `SUBMIT_FORM` / `SUBMIT_OTP` / … |

### Historique

| Méthode | URL | Notes |
|---------|-----|--------|
| `GET` | `/api/v1/gateway/transactions?customerReference=&status=&serviceType=&product=&provider=&q=&from=&to=&page=&size=` | Liste légère (sans `nextAction`). Tri `createdAt` desc. |

Réponse :

```json
{
  "items": [
    {
      "paymentId": "PAY-…",
      "provider": "BILL_PAYMENT",
      "serviceType": "PAIEMENT_FACTURE",
      "catalogServiceCode": "BILL_PAYMENT",
      "amount": 5000,
      "currency": "XOF",
      "status": "WAITING_APPROVAL",
      "currentStep": "APPROVAL_L1",
      "customerReference": "diag-ref",
      "createdAt": "2026-09-14T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 42,
  "hasMore": true
}
```

> Ce n’est **pas** un produit catalogue BPMN : lecture seule sur les transactions gateway.
> Supervision admin : `GET /api/v1/admin/transactions`.

### Validations (ACCOUNT_TRANSFER / RTGS / SWIFT)

| Méthode | URL |
|---------|-----|
| `GET` | `/api/v1/gateway/approvals/inbox` |
| `GET` | `/api/v1/gateway/approvals/by-payment/{paymentId}` |
| `POST` | `/api/v1/gateway/approvals/{paymentId}/decide` body `{ "action": "APPROVE\|REJECT", "data": {…} }` |

---

## 3. Réponse écran — `IntegrationScreenResponse`

```json
{
  "type": "FORM",
  "title": "ACCOUNT_TRANSFER",
  "message": null,
  "formKey": "account-transfer-source",
  "formSchema": { "display": "form", "components": [ … ] },
  "fields": [],
  "data": { "amount": 2000000, "currency": "XOF", "provider": "WAVE" },
  "actions": [ { "id": "SUBMIT_FORM", "label": "Continuer", "style": "primary" } ],
  "otpLength": null,
  "url": null,
  "pollAfterSeconds": null,
  "meta": {
    "paymentId": "PAY-…",
    "status": "AWAITING_USER_INPUT",
    "currentStep": "SELECT_SOURCE_ACCOUNT",
    "catalogServiceCode": "ACCOUNT_TRANSFER",
    "formKey": "account-transfer-source"
  }
}
```

| `type` | Comportement |
|--------|----------------|
| `FORM` | Afficher `formSchema` / `fields` ; action `SUBMIT_FORM` |
| `ENTER_OTP` | Saisie OTP (`otpLength`) ; `SUBMIT_OTP` + `{ "otp": "…" }` |
| `WAIT` | Poll `GET …/payments/{id}` (`pollAfterSeconds`, défaut 3). Inclut `WAITING_APPROVAL`. |
| `REDIRECT` | Ouvrir `url` + poll |
| `DISPLAY_MESSAGE` | Afficher `message` |
| `SUCCESS` / `FAILURE` | Terminal |

---

## 4. Démarrer un produit

### Checkout

```http
POST /api/v1/gateway/checkout
Authorization: Bearer …
Content-Type: application/json

{
  "provider": "ORANGE_MONEY",
  "amount": 2500,
  "currency": "XOF",
  "phoneNumber": "22670......",
  "customerReference": "ORD-123",
  "idempotencyKey": "checkout-ORD-123"
}
```

### Bill payment

Parcours : **collecte facture → confirm → choix wallet (Orange, Wave…) → parcours provider → acquittement**.

Au start, le moyen de paiement est **optionnel** (choisi en mid-flow). Sans `provider`, le rail facturier `BILL_PAYMENT` est utilisé.

```http
POST /api/v1/gateway/bill-payment

{
  "currency": "XOF",
  "customerReference": "BILL-123",
  "billerCode": "SONABEL",
  "productCode": "PREPAID",
  "subscriberReference": "AB-2002"
}
```

Champs optionnels au start : `provider` / `payProvider` (wallet anticipé), `amount`, `billIds`, `billReference`, `billLines`.

Après `confirmBill`, écran `provider-bill-select-pay` (`payProvider`) puis OTP/redirect selon le PaymentFlow du wallet.

### Account transfer

`provider` / `destinationProvider` sont **optionnels** à l’initiation (choisis en mid-flow via `sourceRail` / `destRail`).

```http
POST /api/v1/gateway/account-transfer

{
  "amount": 2000000,
  "currency": "XOF",
  "customerReference": "TRF-001"
}
```

Rétrocompat : vous pouvez encore passer `provider` + `destinationProvider` au start.

Montants XOF → circuit d’approbation DMN :

| Montant | Profil | Rôle(s) |
|---------|--------|--------|
| ≤ 1 M | `NO_APPROVAL` | — |
| ≤ 10 M | `LEVEL_1` | `CHEF_AGENCE` |
| ≤ 50 M | `LEVEL_1_2` | + `RESPONSABLE_OPERATIONS` |
| > 50 M | `HIGH_VALUE` | + `DIRECTEUR` |

L’initiateur **ne peut pas** s’auto-valider. Utiliser un JWT validateur puis `POST …/approvals/{id}/decide`.

### RTGS

Produit catalogue **séparé** (`RTGS`, workflow `rtgsTransfer`) — banques uniquement, min **1 M XOF**, fenêtre lun–ven 08h–16h (`Africa/Ouagadougou`). Contournement local : `"skipCutoffCheck": true` dans le body / data.

`provider` / `destinationProvider` optionnels au start (choix mid-flow via `sourceRail` / `destRail`).

```http
POST /api/v1/gateway/rtgs

{
  "amount": 2500000,
  "currency": "XOF",
  "customerReference": "RTGS-001",
  "skipCutoffCheck": true
}
```

Approbation DMN (XOF) :

| Montant | Niveaux |
|---------|---------|
| &lt; 5 M | L1 |
| ≥ 5 M | L1 + L2 |

Catalogue : `GET /api/v1/gateway/providers?product=rtgs` → banques `BANQUE` + `TRANSFERT`.

### SWIFT

Produit catalogue **séparé** (`SWIFT`, workflow `swiftTransfer`) — cross-border BIC/IBAN, multi-devises (défaut EUR), frais OUR/SHA/BEN. Soft steps : validation BIC, conformité pays (bloque KP/IR/SY), min amount catalogue (défaut 100). Contournement : `"skipComplianceCheck": true`.

`INTERNATIONAL_TRANSFER` (legacy) pointe aussi vers `swiftTransfer`.

```http
POST /api/v1/gateway/swift

{
  "amount": 1500,
  "currency": "EUR",
  "customerReference": "SWIFT-001",
  "skipComplianceCheck": true
}
```

Approbation DMN : toujours ≥ L1 ; XOF ≥ 10 M → L1+L2.

Catalogue : `GET /api/v1/gateway/providers?product=swift`.

### Publier un produit (admin, sans Java)

```http
POST /api/v1/admin/catalog-services/products/publish
Authorization: Bearer … (ADMIN | CONFIG_MANAGER)

{
  "code": "STAR",
  "name": "Transfert STAR",
  "categoryCode": "TRANSFER",
  "workflowKey": "starTransfer",
  "ensureHostFlow": true,
  "hostProviderCode": "STAR",
  "configuration": {
    "serviceType": "TRANSFERT",
    "terminalOnConfirm": false,
    "minAmount": 50000,
    "gateway": {
      "defaultCurrency": "XOF",
      "optionalProviderAtStart": true,
      "providersQuery": { "participantKind": "BANQUE", "serviceType": "TRANSFERT" },
      "hostFlow": { "autoCreate": true, "providerCode": "STAR", "participantKind": "RESEAU" }
    },
    "softRules": [
      { "step": "CHECK_LIMITS", "type": "MIN_AMOUNT", "params": { "minAmountKey": "minAmount" } }
    ],
    "roles": [ … ],
    "participants": [ … ],
    "bindings": [ … ]
  }
}
```

Puis : `POST /api/v1/gateway/products/STAR`.

Types `softRules` supportés (builtins module `payment-soft-rules`) : `MIN_AMOUNT`, `BUSINESS_HOURS`, `REGEX_FIELD`, `REQUIRED_FIELD`, `BLOCKED_VALUES`, `DEFAULT_FEE`, `GENERATE_CODE`, `PASS`.

Extension : implémenter `SoftRuleHandler` (`types()` + `apply`) comme bean Spring — pas de switch dans `CatalogSoftRuleEngine`. SPI : `com.gateway.payment.softrules.spi`.

### Internationalisation d’un produit

Langues disponibles : `GET /api/v1/i18n/languages` (configurées via `gateway.i18n.supported-languages`, `fr` et `en` par défaut).

Un produit se crée avec ses traductions. `name` / `description` portent la langue par défaut ;
`nameI18n` / `descriptionI18n` ajoutent les autres langues, et `translations` couvre les libellés
de `configuration`, indexés par chemin.

```http
POST /api/v1/admin/catalog-services/products/publish

{
  "code": "STAR",
  "name": "Transfert STAR",
  "nameI18n": { "en": "STAR transfer" },
  "description": "Transfert interbancaire STAR",
  "descriptionI18n": { "en": "STAR interbank transfer" },
  "categoryCode": "TRANSFER",
  "workflowKey": "starTransfer",
  "translations": {
    "configuration.roles[SOURCE].label": { "fr": "Banque émettrice", "en": "Sending bank" },
    "configuration.recap.title": { "fr": "Récapitulatif STAR", "en": "STAR summary" },
    "configuration.recap.fields[amount].label": { "fr": "Montant", "en": "Amount" },
    "configuration.presentation.sections[favorites].title": { "fr": "Vos favoris", "en": "Your favourites" }
  },
  "configuration": { … }
}
```

Chemins traduisibles de `configuration` :

| Chemin | Texte |
|--------|-------|
| `configuration.roles[CODE].label` | Libellé d’un rôle |
| `configuration.recap.title` / `.subtitle` / `.confirmLabel` | En-tête et case de confirmation du récap |
| `configuration.recap.fields[KEY].label` | Libellé d’une ligne de récap |
| `configuration.presentation.sections[ID].title` | Titre d’une section de présentation |

Côté lecture, la langue vient de l’en-tête `Accept-Language` de la requête :

- les endpoints catalogue renvoient `name` / `description` déjà résolus, et les maps
  `nameI18n` / `descriptionI18n` pour l’édition admin ;
- `GET /published/{code}/casting` et l’écran de récap renvoient les libellés de `configuration`
  résolus ;
- une traduction absente n’efface jamais le libellé : le repli est langue par défaut, puis la
  valeur d’origine stockée en base.

Les libellés des formulaires Form.io restent portés par le bloc `i18n` de chaque descripteur
sous `modules/payment-admin-module/forms/`.

### Capabilities (briques réutilisables)

Au lieu d’un service Java par produit, les étapes passent par des `CapabilityHandler`.

**Locales** (catalogue step delegate) :

| Code | Rôle |
|------|------|
| `VALIDATE_AMOUNT` / `VALIDATE_ACCOUNT` | Contrôles (via softRules) |
| `CALCULATE_FEES` | Frais + softRules |
| `GENERATE_CODE` | Code dépôt/retrait |
| `WAIT` | Attente agent / settlement |
| `NOTIFY` | Clôture SUCCESS |
| `APPROVAL_OUTCOME` | Retour / rejet validation |
| `SOFT_RULES` | Soft rules seules |

**Provider / INVOKE** (provider call delegate) :

| Capability | Opération provider | Produits typiques |
|------------|--------------------|-------------------|
| `GET_INFOS` | `GETINFOS` | consultation compte |
| `GET_BILLS` | `LOOKUP_BILL` | `BILL_PAYMENT` |
| `GET_BALANCE` | `GET_BALANCE` | solde |
| `GENERATE_OTP` | `GENERATE_OTP` | la plupart |
| `CONFIRM_PAYMENT` / `EXECUTE_TOPUP` | `CONFIRM_PAYMENT` | checkout / top-up |
| `PAY_BILL` | `PAY_BILL` | `BILL_PAYMENT` |
| `EXECUTE_TRANSFER` | `TRANSFERT` | `ACCOUNT_TRANSFER`, RTGS, SWIFT, cash |
| `EXECUTE_LINK` | `LINK` | `LINK` / REGISTER |
| `EXECUTE_UNLINK` | `UNLINK` | `UNLINK` |
| `CREATE_PAYMENT` | `CREATE_PAYMENT` | `CHECKOUT` |
| `EVALUATE_APPROVAL` | ApprovalEngine | transferts avec validation |

Résolution : `bindings[].capability` → sinon `bindings[].operation` → sinon **inférence** activityId. Les produits existants n’ont rien à changer.

```json
{
  "step": "EXECUTE_TRANSFER",
  "kind": "INVOKE",
  "capability": "EXECUTE_TRANSFER",
  "actorRole": "SOURCE",
  "operation": "TRANSFERT"
}
```

**Ne pas** réutiliser un code produit (`ACCOUNT_TRANSFER`, `CHECKOUT`, `BILL_PAYMENT`) comme capability.

### Étape récap (tous produits)

Dans la config catalogue :

```json
{
  "recap": {
    "enabled": true,
    "title": "Récapitulatif",
    "confirmField": "confirmRecap",
    "fields": [
      { "key": "amount", "label": "Montant" },
      { "key": "fees", "label": "Frais" }
    ]
  },
  "bindings": [
    {
      "step": "CONFIRM",
      "kind": "UI",
      "formSource": "RECAP",
      "formKey": "product-recap"
    }
  ]
}
```

- `formSource: RECAP` → écran généré (HTML + case à cocher) depuis le contexte
- Ou `recap.enabled: true` sur une userTask `CONFIRM` / `RECAP`
- Aucun formulaire produit dédié obligatoire

### DocumentPolicy (pièces via DMN)

Quand des documents sont requis (montant, provider…), le catalogue déclare une policy ; le DMN choisit un **profil** (`document_profile`), pas la liste brute des pièces.

```json
{
  "documentPolicy": {
    "enabled": true,
    "code": "REMITTANCE_DOCS",
    "dmnDecisionKey": "decision_DocumentRequirements",
    "amountField": "amount",
    "formKey": "product-document-upload"
  },
  "bindings": [
    {
      "step": "RESOLVE_DOCUMENTS",
      "kind": "CAPABILITY",
      "capability": "RESOLVE_DOCUMENTS"
    },
    {
      "step": "UPLOAD_DOCUMENTS",
      "kind": "UI",
      "formSource": "DOCUMENTS",
      "formKey": "product-document-upload"
    },
    {
      "step": "VALIDATE_DOCUMENTS",
      "kind": "CAPABILITY",
      "capability": "VALIDATE_DOCUMENTS"
    }
  ]
}
```

Flux typique (après quote / montant) :

1. `RESOLVE_DOCUMENTS` — DMN (`amount`, `currency`, `providerCode`, `catalogServiceCode`) → `documentProfileCode` → charge `document_profile` → vars `documentsRequired`, `requiredDocs`, `optionalDocs`
2. UserTask UI `formSource: DOCUMENTS` si `documentsRequired=true` (Form.io dynamique)
3. `VALIDATE_DOCUMENTS` — exige `context.documents.<code>` (ou clés plates `documents.ID_FRONT`)

Profils seed (Flyway V67) : `NONE`, `BASIC_ID`, `STANDARD`, `ENHANCED`, `PROVIDER_STRICT` (RIA / Western Union).

DMN : `modules/payment-admin-module/decisions/document-requirements.dmn` (aussi `rules/document-requirements.dmn`).

### Remittance (REMITTANCE)

Parcours **envoi d'argent** (**RIA**, **Western Union**) — produit catalogue, pas une capability :

1. Expéditeur → bénéficiaire → pays / devise réception  
2. Opérateur remittance → mode payout (cash / bank / wallet)  
3. Montant → `VALIDATE_AMOUNT` → **`GET_QUOTE`** (FX + frais + TTL)  
4. **`RESOLVE_DOCUMENTS`** (DMN) → upload si requis → `VALIDATE_DOCUMENTS`  
5. Récap → wallet de financement → OTP  
6. **`EXECUTE_REMITTANCE`** → notify (sans SUCCESS) → **`WAIT_PAYOUT`** (`AVAILABLE_FOR_PAYOUT` / `WAITING_CALLBACK`)

```http
POST /api/v1/gateway/products/REMITTANCE
# ou POST /api/v1/gateway/remittance

{ "amount": 500000, "currency": "XOF", "receiveCurrency": "EUR" }
```

Capabilities dédiées : `GET_QUOTE`, `EXECUTE_REMITTANCE` (≠ `EXECUTE_TRANSFER` / SWIFT).  
BPMN : `remittanceSend`. Config : Flyway V68 + `documentPolicy` + `remittanceQuote`.

Formulaires **provider-specific** (après choix RIA / WESTERN_UNION) :

| Étape | RIA | Western Union |
|-------|-----|---------------|
| Bénéficiaire | `remittance-beneficiary-ria` | `remittance-beneficiary-western-union` |
| Pays | `remittance-country-ria` | `remittance-country-western-union` |
| Payout | `remittance-payout-ria` | `remittance-payout-western-union` |
| Montant | `remittance-amount-ria` | `remittance-amount-western-union` |

Bindings catalogue avec `providerCode` + `actorRole: PROVIDER` (Flyway V71). Ordre BPMN : sender → **provider** → beneficiary → country → payout → amount.

### Remittance receive (REMITTANCE_RECEIVE)

Parcours **retrait / crédit** après un envoi en `AVAILABLE_FOR_PAYOUT` :

1. Saisie référence (`remittanceReference` / MTCN / paymentId)  
2. **`LOOKUP_REMITTANCE`** → charge l’envoi, pose `payoutMethod`  
3. Branche BPMN : **CASH_PICKUP** (ID bénéficiaire) ou **BANK_DEPOSIT / WALLET** (confirm compte)  
4. `VALIDATE_PAYOUT` → récap → **`EXECUTE_PAYOUT`** → **`COMPLETE_PAYOUT`** (clôture parent `PAID_OUT` / `CREDITED`) → NOTIFY  

```http
POST /api/v1/gateway/products/REMITTANCE_RECEIVE
# ou POST /api/v1/gateway/remittance-receive

{ "remittanceReference": "QTE-ABC123..." }
```

Capabilities : `LOOKUP_REMITTANCE`, `VALIDATE_PAYOUT`, `EXECUTE_PAYOUT`, `COMPLETE_PAYOUT`.  
BPMN : `remittanceReceive` (Flyway V69).  
ID pickup provider-specific : `remittance-receive-id-ria` / `remittance-receive-id-western-union` (V71).

### Cash-out (CASH_OUT)

Parcours **client-initié → code agent** :

```
PRODUCT CASH_OUT
  └── JOURNEY STANDARD_CASH_OUT
        └── CHANNEL MOBILE | GAB | USSD | BACKOFFICE
```

1. Montant cash  
2. Compte / wallet source  
3. Réseau agent (remise)  
4. Frais + récap  
5. OTP → débit → **code retrait** → attente agent  

```http
POST /api/v1/gateway/products/CASH_OUT
# ou POST /api/v1/gateway/cash-out

{
  "amount": 25000,
  "currency": "XOF",
  "journeyCode": "STANDARD_CASH_OUT",
  "channel": "MOBILE"
}
```

`channel` / `accessChannel` est **requis** si le produit déclare `supportedChannels` (Flyway V72).  
Overlays optionnels : `configuration.channelDefaults.<CHANNEL>` (bindings / softRules).

### Audience produit (RETAIL / CORPORATE / …)

Orthogonal au canal d'accès. Déclaré sur la config versionnée :

```json
{
  "supportedAudiences": ["RETAIL", "CORPORATE"],
  "defaultAudience": "RETAIL"
}
```

Valeurs : `RETAIL`, `CORPORATE`, `AGENT`, `MERCHANT` (alias `MARCHAND`), `BACKOFFICE`.  
Flyway **V108** renseigne les produits existants.

- Liste filtrée : `GET /api/v1/admin/catalog-services/published?audience=CORPORATE`
- Start produit : body `audience` / `productAudience` (validé si politique déclarée)
- Casting expose `supportedAudiences` / `defaultAudience`

Sans `supportedAudiences` → produit visible pour **toutes** les audiences (compat).

### Cash-in (CASH_IN)

Même modèle : journey `STANDARD_CASH_IN` + canal d'accès.
### Link / Register (LINK)

Parcours **rattachement compte wallet** (`ServiceType` = `LINK`) :

1. Choix du wallet (WAVE / OM / …)  
2. MSISDN (+ alias optionnel)  
3. Validation champs (`VALIDATE_ACCOUNT`)  
4. Récap générique  
5. OTP → **`EXECUTE_LINK`** (`operation: LINK`) → notify  

Alias : `REGISTER` → même produit `LINK`.

```http
POST /api/v1/gateway/products/LINK
# ou POST /api/v1/gateway/link
# ou POST /api/v1/gateway/register

{ "phoneNumber": "22670000001", "currency": "XOF" }
```

---

## 5. Soumettre une action

```http
POST /api/v1/gateway/payments/{paymentId}/actions

{ "action": "SUBMIT_FORM", "data": { "sourceRail": "WAVE", "sourceAccount": "77000001" } }
```

```json
{ "action": "SUBMIT_OTP", "data": { "otp": "123456" } }
```

---

## 6. Squelette client (TypeScript)

```typescript
type Screen = {
  type: string;
  formKey?: string;
  formSchema?: Record<string, unknown>;
  message?: string;
  otpLength?: number;
  url?: string;
  pollAfterSeconds?: number;
  meta?: { paymentId?: string; status?: string };
  actions?: Array<{ id: string; label: string }>;
};

class GatewayClient {
  constructor(
    private baseUrl: string,
    private token: () => Promise<string>,
    private ui: {
      collectForm: (s: Screen) => Promise<Record<string, unknown>>;
      collectOtp: (s: Screen) => Promise<string>;
      openRedirect: (url: string) => void;
      showMessage: (m: string) => void;
    }
  ) {}

  private async headers() {
    return {
      Authorization: `Bearer ${await this.token()}`,
      'Content-Type': 'application/json',
    };
  }

  async start(product: 'checkout' | 'bill-payment' | 'account-transfer', body: object): Promise<Screen> {
    const res = await fetch(`${this.baseUrl}/api/v1/gateway/${product}`, {
      method: 'POST',
      headers: await this.headers(),
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  }

  async get(paymentId: string): Promise<Screen> {
    const res = await fetch(`${this.baseUrl}/api/v1/gateway/payments/${paymentId}`, {
      headers: await this.headers(),
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  }

  async action(paymentId: string, action: string, data: Record<string, unknown> = {}): Promise<Screen> {
    const res = await fetch(`${this.baseUrl}/api/v1/gateway/payments/${paymentId}/actions`, {
      method: 'POST',
      headers: await this.headers(),
      body: JSON.stringify({ action, data }),
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  }

  async run(initial: Screen, maxPolls = 60): Promise<Screen> {
    let screen = initial;
    let polls = 0;
    for (;;) {
      const paymentId = screen.meta?.paymentId;
      if (!paymentId) throw new Error('paymentId manquant');
      switch (screen.type) {
        case 'SUCCESS':
        case 'FAILURE':
          return screen;
        case 'FORM':
        case 'DISPLAY_MESSAGE': {
          const data = await this.ui.collectForm(screen);
          screen = await this.action(paymentId, 'SUBMIT_FORM', data);
          break;
        }
        case 'ENTER_OTP': {
          const otp = await this.ui.collectOtp(screen);
          screen = await this.action(paymentId, 'SUBMIT_OTP', { otp });
          break;
        }
        case 'REDIRECT': {
          if (screen.url) this.ui.openRedirect(screen.url);
          await sleep((screen.pollAfterSeconds ?? 3) * 1000);
          screen = await this.get(paymentId);
          if (++polls > maxPolls) throw new Error('Timeout REDIRECT');
          break;
        }
        case 'WAIT': {
          if (screen.message) this.ui.showMessage(screen.message);
          await sleep((screen.pollAfterSeconds ?? 3) * 1000);
          screen = await this.get(paymentId);
          if (++polls > maxPolls) throw new Error('Timeout WAIT');
          break;
        }
        default:
          throw new Error(`type inconnu: ${screen.type}`);
      }
    }
  }
}

function sleep(ms: number) {
  return new Promise((r) => setTimeout(r, ms));
}
```

---

## 7. Checklist

1. Compte JWT d’intégration (+ comptes validateurs pour transferts).
2. Toujours consommer **`IntegrationScreenResponse.type`**, pas des étapes hardcodées.
3. Persister `meta.paymentId` pour reprise.
4. `WAIT` / `REDIRECT` : poller avec plafond.
5. Ne pas exposer le JWT gateway au navigateur si vous avez un BFF.
6. paymentHub2 peut rester un client ; un autre système appelle **directement** `/api/v1/gateway`.

---

## 8. Erreurs fréquentes

| Code / symptôme | Cause |
|-----------------|--------|
| `PROVIDER_REQUIRED` | `provider` manquant au start |
| `SELF_APPROVAL_FORBIDDEN` | même user initiateur + validateur |
| `401` | token expiré |
| `PROVIDER_FAILED` | connecteur / opération provider absente |

---

## 9. Code

| Sujet | Chemin |
|-------|--------|
| Controller | `…/integration/api/ProductGatewayController.java` |
| Facade | `…/integration/service/ProductGatewayFacade.java` |
| Mapper écran | `…/integration/service/IntegrationScreenMapper.java` |
| DTO | `…/integration/api/dto/IntegrationScreenResponse.java` |
| API bas niveau | `/api/v1/payments`, `/api/v1/bills`, `/api/v1/approvals` |
