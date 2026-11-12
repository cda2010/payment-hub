# Guide d’intégration générique — utiliser les produits payment-2

Ce guide explique comment **n’importe quelle application** (web, mobile, BFF, GAB, USSD…)
se branche sur payment-2 et exécute **n’importe quel produit catalogue** (SWIFT, RTGS,
BILL_PAYMENT, CASH_OUT, …) avec **un seul client générique**.

> Référence détaillée des endpoints et de la configuration produit : [`INTEGRATION.md`](INTEGRATION.md).  
> Configuration admin d’un **nouveau produit** (dont soft rules) :
> [`GUIDE-CONFIGURATION-PRODUIT.md`](GUIDE-CONFIGURATION-PRODUIT.md)
> — § [Ajouter des soft rules](GUIDE-CONFIGURATION-PRODUIT.md#4-ajouter-des-soft-rules-à-un-produit).  
> Librairie client partagée : § [11. `@payment2/gateway-core`](#11-librairie-client-payment2gateway-core)
> (procédure d’intégration).  
> Collection Postman : [`postman/Payment-2-Product-Gateway.postman_collection.json`](postman/Payment-2-Product-Gateway.postman_collection.json)
> (+ environnement `Payment-2-Local.postman_environment.json`).

---

## 1. Le principe en une phrase

Votre application **démarre un produit**, puis **affiche l’écran renvoyé** par payment-2 et
**renvoie la saisie**, en boucle, jusqu’à un écran terminal (`SUCCESS` ou `FAILURE`).

```
 Votre app                               payment-2
    │  POST /gateway/products/{CODE}  ──────►  démarre le workflow (BPMN)
    │  ◄──────────  écran (FORM, ENTER_OTP, WAIT…)
    │  POST /gateway/payments/{id}/actions ─►  avance d’une étape
    │  ◄──────────  écran suivant
    │  …
    │  ◄──────────  SUCCESS | FAILURE
```

Les étapes, formulaires, contrôles, frais, validations et appels partenaires sont portés par
payment-2 (BPMN + Form.io + configuration catalogue). **Votre app ne code aucune étape métier** :
un nouveau produit publié côté payment-2 fonctionne sans modifier votre client.

---

## 2. Prérequis

| Élément | Détail |
|---------|--------|
| URL de base | Locale : `http://localhost:8080` — Swagger : `/swagger-ui.html` (tag **Product Gateway**) |
| Compte d’intégration | Utilisateur payment-2 qui initie les opérations |
| Comptes validateurs | Requis pour les produits soumis à approbation (ACCOUNT_TRANSFER, RTGS, SWIFT). L’initiateur ne peut **pas** valider sa propre opération. |
| Canal (`channel`) | `MOBILE`, `WEB`, `GAB`, `USSD`, `BACKOFFICE` |
| Audience (`audience`) | `RETAIL`, `CORPORATE`, `AGENT`, `MERCHANT`, `BACKOFFICE` |
| Langue | En-tête `Accept-Language: fr` ou `en` (libellés produits et formulaires) |

---

## 3. Authentification

```http
POST /api/v1/auth/login
Content-Type: application/json

{ "username": "…", "password": "…" }
```

Réponse : `{ "accessToken": "…", "refreshToken": "…", … }`.

Tous les appels suivants portent :

```http
Authorization: Bearer <accessToken>
```

Renouvellement quand l’API répond `401` :

```http
POST /api/v1/auth/refresh
{ "refreshToken": "…" }
```

> Si votre application a un backend (BFF), c’est lui qui détient le token et appelle
> payment-2 : n’exposez pas le JWT gateway dans le navigateur.

---

## 4. Découvrir les produits disponibles

| Besoin | Appel |
|--------|-------|
| Produits publiés pour votre audience | `GET /api/v1/admin/catalog-services/published?audience=CORPORATE` |
| Casting d’un produit (rôles, participants, audiences supportées) | `GET /api/v1/admin/catalog-services/published/{code}/casting` |
| Partenaires / banques / wallets d’un produit | `GET /api/v1/gateway/providers?product={code ou alias}` |
| Écran d’accueil catalogue (sections, listes) | `GET /api/v1/gateway/products/{code}/catalog?channel=WEB` |
| Listes dynamiques (selects) | `GET /api/v1/gateway/data-sources/{code}?origin=LOCAL&…` |

Ces endpoints sont accessibles à tout utilisateur authentifié.

Produits livrés par défaut :

| Code | Alias REST | Usage |
|------|-----------|-------|
| `CHECKOUT` | `/gateway/checkout` | Paiement marchand via wallet |
| `BILL_PAYMENT` | `/gateway/bill-payment` | Paiement de factures |
| `ACCOUNT_TRANSFER` | `/gateway/account-transfer` | Transfert de compte à compte / wallet |
| `RTGS` | `/gateway/rtgs` | Virement interbancaire gros montant |
| `SWIFT` | `/gateway/swift` | Virement international BIC/IBAN |
| `CASH_OUT` / `CASH_IN` | `/gateway/cash-out`, `/gateway/cash-in` | Retrait / dépôt via agent |
| `LINK` | `/gateway/link`, `/gateway/register` | Rattachement d’un compte wallet |
| `REMITTANCE` / `REMITTANCE_RECEIVE` | `/gateway/remittance`, `/gateway/remittance-receive` | Envoi / réception d’argent (RIA, Western Union) |
| `COFFRE` | `/gateway/coffre` | Coffre / épargne |

Tout autre produit publié est accessible via l’endpoint générique
`POST /api/v1/gateway/products/{CODE}`. **Privilégiez cet endpoint générique** : les alias
n’existent que par commodité.

---

## 5. Démarrer un produit

```http
POST /api/v1/gateway/products/{CODE}
Authorization: Bearer …
Content-Type: application/json

{
  "idempotencyKey": "5f0c7e0a-…",
  "channel": "WEB",
  "audience": "CORPORATE",
  "customerReference": "REF-INTERNE-001",
  "amount": 1500,
  "currency": "EUR"
}
```

| Champ | Obligatoire | Rôle |
|-------|-------------|------|
| `idempotencyKey` | Fortement recommandé | Un même `idempotencyKey` renvoie la transaction déjà créée au lieu d’en créer une seconde (protège contre les doubles clics et les rejeux réseau). Générez un UUID par intention utilisateur. |
| `channel` | Si le produit déclare des canaux supportés | Canal d’accès ; il sélectionne les surcharges propres au canal. |
| `audience` | Si le produit déclare des audiences supportées | Segment client. Une audience non supportée par le produit fait échouer le démarrage. |
| `customerReference` | Non | Votre référence interne, réutilisable pour filtrer l’historique. |
| `amount`, `currency` | Selon le produit | Souvent saisis plus tard dans le parcours ; la devise par défaut dépend du produit. |
| `provider` | Selon le produit | Partenaire source. Pour la plupart des produits il est choisi en cours de parcours ; `PROVIDER_REQUIRED` signale qu’il est attendu au démarrage. |
| Autres champs | Non | Tout champ supplémentaire (ex. `bic`, `iban`, `billerCode`, `phoneNumber`) est versé dans le contexte et pré-remplit les étapes correspondantes. |

La réponse (HTTP `201`) est le **premier écran** du parcours.

---

## 6. L’écran : `IntegrationScreenResponse`

Chaque réponse de `/api/v1/gateway` a la même forme :

```json
{
  "type": "FORM",
  "title": "SWIFT",
  "message": null,
  "formKey": "swift-destination",
  "formSchema": { "display": "form", "components": [ … ] },
  "fields": [],
  "data": { "amount": 1500, "currency": "EUR" },
  "actions": [ { "id": "SUBMIT_FORM", "label": "Continuer", "style": "primary" } ],
  "otpLength": null,
  "url": null,
  "pollAfterSeconds": null,
  "meta": {
    "paymentId": "PAY-…",
    "status": "AWAITING_USER_INPUT",
    "currentStep": "CAPTURE_DESTINATION",
    "catalogServiceCode": "SWIFT",
    "errorCode": null
  }
}
```

| Champ | Utilisation |
|-------|-------------|
| `type` | **Le seul champ qui pilote votre UI** (voir tableau ci-dessous). |
| `meta.paymentId` | Identifiant de la transaction. **À persister** pour reprendre un parcours interrompu. |
| `formSchema` | Formulaire Form.io à afficher. |
| `fields` | Description simplifiée des champs (nom, libellé, type, obligatoire, options) pour les clients sans moteur Form.io. |
| `data` | Valeurs connues, à utiliser pour pré-remplir le formulaire ou afficher un récapitulatif. |
| `message` | Texte à afficher (information, attente, erreur). |
| `meta.status`, `meta.currentStep` | Informatifs (logs, suivi). Ne construisez pas de logique d’écran dessus. |

### Comportement attendu par type

| `type` | Ce que fait votre app | Action suivante |
|--------|----------------------|-----------------|
| `FORM` | Afficher `formSchema` (ou `fields`), collecter la saisie | `SUBMIT_FORM` avec les valeurs saisies |
| `ENTER_OTP` | Demander un code OTP de `otpLength` chiffres | `SUBMIT_OTP` avec `{ "otp": "…" }` |
| `WAIT` | Afficher `message` / un indicateur d’attente (traitement partenaire, **attente de validation**) | `GET /payments/{id}` après `pollAfterSeconds` secondes (3 par défaut) |
| `REDIRECT` | Ouvrir `url` (page du partenaire) | `GET /payments/{id}` en boucle jusqu’au changement d’écran |
| `DISPLAY_MESSAGE` | Afficher `message` | `SUBMIT_FORM` (sans données) pour continuer |
| `SUCCESS` | Afficher la confirmation | Fin |
| `FAILURE` | Afficher `message` et `meta.errorCode` | Fin |

---

## 7. Faire avancer le parcours

```http
POST /api/v1/gateway/payments/{paymentId}/actions
{ "action": "SUBMIT_FORM", "data": { "bic": "BNPAFRPPXXX", "iban": "FR76…" } }
```

```http
POST /api/v1/gateway/payments/{paymentId}/actions
{ "action": "SUBMIT_OTP", "data": { "otp": "123456" } }
```

| Action | Usage |
|--------|-------|
| `SUBMIT_FORM` | Valider un écran `FORM` / `DISPLAY_MESSAGE`. `data` = valeurs du formulaire (clés = `key` des composants Form.io). |
| `SUBMIT_OTP` | Valider un écran `ENTER_OTP`. L’OTP n’est jamais stocké en clair. |
| `CHECK_STATUS` | Forcer une vérification de statut auprès du partenaire. |

Relire l’écran courant à tout moment (reprise, polling) :

```http
GET /api/v1/gateway/payments/{paymentId}
```

Annuler une transaction non terminée : `POST /api/v1/payments/{paymentId}/cancel`.

---

## 8. Afficher les formulaires

Deux options selon votre client :

1. **Moteur Form.io** (web) : rendre `formSchema` tel quel et envoyer `submission.data` dans
   `SUBMIT_FORM`.
2. **Rendu natif** (mobile, PrimeNG, Ionic…) : parcourir `formSchema.components` (ou `fields`)
   et générer vos propres composants par `type` (`textfield`, `number`, `select`, `checkbox`…).

Dans les deux cas, gérez les propriétés payment-2 portées par `properties` des composants :

| Propriété | Comportement client |
|-----------|---------------------|
| `rfDataSource` sans `dependsOn` | Options déjà hydratées par le serveur dans le schéma. Rien à faire. |
| `rfDataSource` avec `dependsOn` | Au changement d’un champ parent : appeler `GET /api/v1/gateway/data-sources/{code}?origin=…` avec `params` + `paramsFrom` (valeurs du formulaire), remplacer les options et vider la valeur du champ dépendant. |
| `rfShowWhen` | Afficher le champ seulement si la condition est vraie (ex. `{ "field": "sourceRail", "notEmpty": true }`). |

Ignorez les boutons `submit` du schéma : utilisez `actions` de l’écran pour vos propres boutons.

Implémentations de référence : `payment-front/.../rf-datasource.util.ts`,
`mobile-app/.../rf-datasource.util.ts`.

---

## 9. Produits avec validation (maker / checker)

Pour les produits soumis à approbation, après l’étape de confirmation l’initiateur reçoit un
écran `WAIT` (statut `WAITING_APPROVAL`). La validation se fait **avec le token d’un
validateur** :

```http
GET  /api/v1/gateway/approvals/inbox
GET  /api/v1/gateway/approvals/by-payment/{paymentId}
POST /api/v1/gateway/approvals/{paymentId}/decide
{ "action": "APPROVE", "data": { "comment": "OK" } }
```

- `APPROVE` : passe au niveau suivant ou reprend le parcours (souvent l’OTP de l’initiateur).
- `REJECT` : renvoie l’opération à l’initiateur, qui reçoit un écran `FORM` de correction.

Le nombre de niveaux (L1, L2, L3) est calculé par des règles DMN selon le produit, le montant
et la devise. Votre app n’a pas à le connaître : elle suit les écrans.

Configuration des circuits, boîte de réception validateur, contrôles et erreurs :
[`GUIDE-VALIDATIONS-MULTI-NIVEAUX.md`](GUIDE-VALIDATIONS-MULTI-NIVEAUX.md).

---

## 10. Historique

```http
GET /api/v1/gateway/transactions?product=SWIFT&status=&customerReference=&from=&to=&page=0&size=20
```

Liste légère triée par date décroissante (`items`, `page`, `size`, `total`, `hasMore`). Pour le
détail et l’écran courant d’une transaction : `GET /api/v1/gateway/payments/{paymentId}`.

---

## 11. Librairie client `@payment2/gateway-core`

Package **headless** (TypeScript pur) : types `IntegrationScreen`, mapping
`formSchema` → champs, helpers `rf-datasource`, `Payment2Client` / `run(ui)`.

Emplacement : `payment-2/packages/gateway-core`  
README package : [`packages/gateway-core/README.md`](../packages/gateway-core/README.md)

**Hors scope de la lib :** rendu Ionic / PrimeNG / Form.io, auth Keycloak
(injecter `getToken`), enrichissements produit spécifiques.

### 11.1 Clients déjà branchés

| App | Audience / canal | Rendu UI |
|-----|------------------|----------|
| `mobile-app` | RETAIL / MOBILE | Ionic (`p2-dynamic-form`) |
| `payment-front` | — (admin + parcours) | PrimeNG (`gateway-primeng-form`) |
| `web-app-corporate` | CORPORATE / WEB | Screen driver + journey |
| `web-app-agent` | AGENT / WEB | `startProduct` + types |
| `mobile-app-agent` | AGENT / MOBILE | Lib + re-exports (prêt journeys) |
| `mobile-app-marchand` | MERCHANT / MOBILE | Lib + re-exports (prêt journeys) |

### 11.2 Procédure d’intégration (nouvelle app)

À faire **une fois** par application cliente.

#### Étape 1 — Dépendance npm

Depuis la racine de l’app (sibling de `payment-2` sous `Documents/`) :

```json
{
  "dependencies": {
    "@payment2/gateway-core": "file:../payment-2/packages/gateway-core"
  }
}
```

Si l’app est **dans** le monorepo payment-2 (ex. `payment-front`) :

```json
"@payment2/gateway-core": "file:../packages/gateway-core"
```

Puis :

```bash
npm install
```

#### Étape 2 — Path TypeScript

Dans `tsconfig.json` (ou `tsconfig.app.json` si c’est là que vivent les `paths`) :

```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@payment2/gateway-core": ["../payment-2/packages/gateway-core/src/index.ts"]
    }
  }
}
```

Ajuster le chemin relatif selon l’emplacement de l’app
(`../packages/gateway-core/src/index.ts` pour `payment-front`).

#### Étape 3 — Re-exports locaux (recommandé)

Évite de disperser les imports `@payment2/gateway-core` partout ; les apps
existantes exposent des facades :

**`src/app/shared/formio/rf-datasource.util.ts`**

```typescript
export {
  buildDataSourceQuery,
  dependsOnList,
  extractAmountChips,
  extractRfDataSource,
  extractRfShowWhen,
  isShowWhenVisible,
  parentsReady,
  type RfDataSourceConfig,
  type RfShowWhen
} from '@payment2/gateway-core';
```

**`src/app/shared/models/gateway-form.mapper.ts`**

```typescript
export type { GatewayField as JourneyField } from '@payment2/gateway-core';

export {
  displayHtmlFromIntegrationScreen,
  fieldsFromFormioComponents,
  fieldsFromIntegrationScreen,
  hiddenDefaultsFromIntegrationScreen,
  isPassThroughForm,
  mapGatewayField,
  mapGatewayFields,
  withoutPendingAmount
} from '@payment2/gateway-core';
```

**`src/app/shared/models/gateway.models.ts`**

```typescript
export type {
  IntegrationScreen,
  IntegrationScreenAction,
  IntegrationScreenType,
  StartProductRequest
} from '@payment2/gateway-core';

export { screenPaymentId, screenStatus, isResumableStatus } from '@payment2/gateway-core';
// + DTOs propres à l’app (billers, catalogue, …) si besoin
```

Option mobile : `libs/payment2-sdk` + alias `@coris/payment2-sdk` qui
`export * from '@payment2/gateway-core'`.

#### Étape 4 — Environnement

```typescript
export const environment = {
  // …
  gatewayBaseUrl: '/api/v1/gateway', // ou URL absolue
  audience: 'CORPORATE' as const,    // RETAIL | CORPORATE | AGENT | MERCHANT | …
};
```

Canal typique : `MOBILE` (apps Ionic) ou `WEB` (portails).

#### Étape 5 — Client HTTP Angular (ou `Payment2Client`)

**Option A — HttpClient Angular** (pattern corporate / agent / mobile) :

```typescript
startProduct(code: string, body: StartProductRequest = {}): Observable<IntegrationScreen> {
  const payload = {
    ...body,
    channel: body.channel || 'WEB',
    audience: body.audience || 'CORPORATE',
    idempotencyKey: body.idempotencyKey || crypto.randomUUID(),
  };
  return this.http.post<IntegrationScreen>(
    `${environment.gatewayBaseUrl}/products/${encodeURIComponent(code)}`,
    payload,
  );
}

getGatewayPayment(id: string) { /* GET …/payments/{id} */ }
submitGatewayAction(id: string, action: string, data = {}) { /* POST …/actions */ }
```

**Option B — `Payment2Client` headless** (fetch, sans Angular) :

```typescript
import { Payment2Client, fieldsFromIntegrationScreen } from '@payment2/gateway-core';

const client = new Payment2Client(baseUrl, getToken, {
  channel: 'WEB',
  audience: 'CORPORATE',
});

const first = await client.start('SWIFT', { amount: 1500, currency: 'EUR' });
const fields = fieldsFromIntegrationScreen(first); // → votre UI
await client.run(first, myUi);
```

`GatewayUi` à implémenter dans l’app :

```typescript
interface GatewayUi {
  collectForm(screen: IntegrationScreen): Promise<Record<string, unknown>>;
  collectOtp(screen: IntegrationScreen): Promise<string>;
  openUrl(url: string): void;
  showWaiting(message?: string | null): void;
}
```

#### Étape 6 — Rendu UI (reste dans l’app)

1. Sur `type === 'FORM'` : `fieldsFromIntegrationScreen(screen)` (ou `formSchema` Form.io).
2. Mapper chaque `GatewayField.type` vers vos composants (Ionic / PrimeNG / …).
3. Au submit : `submitGatewayAction(paymentId, 'SUBMIT_FORM', data)` avec les
   **mêmes clés** que `field.code`.
4. Gérer `ENTER_OTP`, `WAIT` / `REDIRECT` (poll), `SUCCESS` / `FAILURE` — voir §6–§8.

Références de rendu :

| Stack | Fichier |
|-------|---------|
| Ionic | `mobile-app/.../p2-dynamic-form.component.ts` |
| PrimeNG | `payment-front/.../gateway-primeng-form.component.ts` |
| Screen driver | `mobile-app` / `web-app-corporate` `gateway-screen-driver.service.ts` |

#### Étape 7 — Vérification

```bash
npm install
npx ng build --configuration=development
```

Smoke : `POST /gateway/products/{CODE}` avec JWT + `channel` / `audience`, puis
enchaîner les actions jusqu’à `SUCCESS` ou `FAILURE`.

### 11.3 API exportée (rappel)

```typescript
import {
  Payment2Client,
  fieldsFromIntegrationScreen,
  screenPaymentId,
  isPassThroughForm,
  buildDataSourceQuery,
  type IntegrationScreen,
  type GatewayField,
  type StartProductRequest,
  type GatewayUi,
} from '@payment2/gateway-core';
```

| Export | Rôle |
|--------|------|
| `Payment2Client` | `start` / `get` / `act` / `run(ui)` / `resolveDataSource` |
| `fieldsFromIntegrationScreen` | `formSchema` / `fields` → champs plats |
| `hiddenDefaultsFromIntegrationScreen` | valeurs `hidden` à merger au submit |
| `isPassThroughForm` | FORM vide → auto `SUBMIT_FORM` |
| `buildDataSourceQuery` / `parentsReady` | cascades `rfDataSource` |
| `screenPaymentId` / `isResumableStatus` | meta parcours |

Reprise après fermeture de l’app : `client.run(await client.get(paymentId), myUi)`
ou `GET /gateway/payments/{id}` + screen-driver.

---

## 12. Erreurs

Les erreurs HTTP renvoient un corps `ApiError` :

```json
{
  "timestamp": "2026-09-27T12:00:00Z",
  "status": 400,
  "code": "PROVIDER_REQUIRED",
  "message": "provider is required",
  "path": "/api/v1/gateway/products/CHECKOUT",
  "traceId": "…",
  "fieldErrors": [ { "field": "amount", "message": "…" } ]
}
```

| Code / statut | Cause | Réaction client |
|---------------|-------|-----------------|
| `401` | Token absent ou expiré | Rafraîchir le token, rejouer |
| `403` | Rôle insuffisant (ex. validation) | Informer l’utilisateur |
| `PROVIDER_REQUIRED` | Le produit exige `provider` au démarrage | Ajouter `provider` au body |
| `SELF_APPROVAL_FORBIDDEN` | L’initiateur tente de valider | Utiliser un autre compte validateur |
| `UNKNOWN_ACTION` | Action non supportée | Vérifier `action` (`SUBMIT_FORM`, `SUBMIT_OTP`, `CHECK_STATUS`) |
| `PROVIDER_FAILED` | Échec côté partenaire | Afficher `message` ; l’écran passe généralement en `FAILURE` |

Une erreur **métier en cours de parcours** (contrôle refusé, fonds insuffisants…) n’est
généralement pas une erreur HTTP : elle arrive sous forme d’écran `FAILURE` avec `message` et
`meta.errorCode`. Communiquez le `traceId` au support en cas d’incident.

---

## 13. Checklist d’intégration

1. Obtenir un compte d’intégration (et des comptes validateurs si besoin).
2. Jouer le parcours du produit dans Postman pour voir chaque écran.
3. Intégrer **`@payment2/gateway-core`** (§11.2) et un rendu de formulaire (§8).
4. Envoyer `channel`, `audience` et un `idempotencyKey` à chaque démarrage.
5. Persister `meta.paymentId` et gérer la reprise.
6. Plafonner le polling `WAIT` / `REDIRECT`.
7. Piloter l’UI **uniquement** par `type` + `formSchema` / `fields` + `actions`, jamais par
   `formKey` ou `currentStep` codés en dur.
8. Garder le JWT côté serveur si vous avez un BFF.
9. Tester les cas `FAILURE`, `REJECT` en validation, et l’expiration du token.

---

## 14. Et pour un nouveau produit ?

Côté intégrateur : **rien à coder**. Une fois le produit publié par l’équipe payment-2
(BPMN + formulaires + `POST /api/v1/admin/catalog-services/products/publish`), appelez
`POST /api/v1/gateway/products/{NOUVEAU_CODE}` avec le même client.

Checklist config (catalogue, pricing, rules, commission, identity) :
[`GUIDE-CONFIGURATION-PRODUIT.md`](GUIDE-CONFIGURATION-PRODUIT.md).  
Détail API publish : [`INTEGRATION.md` — Publier un produit](INTEGRATION.md#publier-un-produit-admin-sans-java).

### Configurer les étapes de service dans le BPMN

Ce que fait une `serviceTask` se déclare **sur la tâche**, dans le concepteur RapidBPMN
(section « Capability (étape) ») ou directement dans le XML :

```xml
<definitions … xmlns:pay="http://payment-gateway/schema/bpmn/pay">
  <serviceTask id="EXECUTE_LINK" flowable:delegateExpression="${catalogStepDelegate}"
               pay:capability="EXECUTE_LINK" pay:actorRole="SOURCE" pay:operation="LINK"/>
```

| Attribut | Rôle |
|---|---|
| `pay:capability` | Brique exécutée (`VALIDATE_ACCOUNT`, `GENERATE_OTP`, `EXECUTE_TRANSFER`…). |
| `pay:actorRole` | Participant du casting qui exécute l’appel (`SOURCE`, `DESTINATION`, `BILLER`…). |
| `pay:operation` | Opération fournisseur, seulement si elle diffère de celle de la capability. |

`${catalogStepDelegate}` choisit le chemin d’exécution : appel fournisseur, évaluation
d’approbation ou étape locale (softRules).

La configuration catalogue garde les bindings **UI**, les `softRules`, les paramètres métier
et les variantes canal (`channelDefaults`). Un binding catalogue qui porte une `capability`
**surcharge** la tâche (workflow partagé entre plusieurs produits). Le BPMN ne fait alors que
compléter les champs vides.

Pour **ajouter / modifier** les soft rules d’un produit :
[`GUIDE-CONFIGURATION-PRODUIT.md` §4](GUIDE-CONFIGURATION-PRODUIT.md#4-ajouter-des-soft-rules-à-un-produit).

Référentiels et contrôles :

- `GET /api/v1/catalog/capabilities` renvoie les capabilities, les opérations fournisseur et
  les rôles. Les listes déroulantes du concepteur sont alimentées par cet endpoint.
- `GET /api/v1/admin/catalog-services/versions/{versionId}/workflow/steps` renvoie la capability
  résolue pour chaque `serviceTask`, avec les erreurs et les avertissements.
- La publication d’une version est **refusée** (`CATALOG_WORKFLOW_INVALID`) si une capability ou
  un rôle est inconnu. Les cas suivants sont seulement signalés : étape sans effet,
  `VALIDATE_ACCOUNT` sans softRule, ou règle visant une étape absente du process.

Le produit `LINK` (`account-link.bpmn20.xml`) sert d’exemple de référence.

Concept, fonctionnement interne et liste complète des capabilities :
[`CAPABILITIES.md`](CAPABILITIES.md).
