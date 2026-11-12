# Guide — validations multi-niveaux (maker / checker)

Ce guide explique comment fonctionnent les **circuits de validation multi-niveaux** de
payment-2, comment **les configurer** pour un produit, et comment **les intégrer** dans une
application (côté initiateur et côté validateur).

> Client générique et écrans : [`GUIDE-INTEGRATION-PRODUITS.md`](GUIDE-INTEGRATION-PRODUITS.md).  
> Vue d’architecture : [`ARCHITECTURE.md` — §6 Validation maker / checker](ARCHITECTURE.md).  
> Collection Postman : dossiers ACCOUNT_TRANSFER / RTGS / SWIFT (étapes *Poll*, *Inbox*, *Decide*)
> et **08 Simulate approval (admin)**.

---

## 1. Le principe

Un **initiateur** (maker) prépare une opération. Avant son exécution, **0 à 3 niveaux** de
**validateurs** (checkers) doivent l’approuver, dans l’ordre. Le nombre de niveaux, les rôles
attendus et le nombre de votes par niveau dépendent du contexte (montant, devise, produit…).

```
 Initiateur                       payment-2                                  Validateurs
    │ SUBMIT_FORM (récap) ─────►  EVALUATE_APPROVAL
    │                             DMN (montant, devise, produit…) → profil « LEVEL_1_2 »
    │                             profil → L1 : CHEF_AGENCE, L2 : RESPONSABLE_OPERATIONS
    │ ◄──── WAIT (WAITING_APPROVAL)
    │                             APPROVAL_L1  ◄──────── APPROVE   (rôle CHEF_AGENCE)
    │                             APPROVAL_L2  ◄──────── APPROVE   (rôle RESPONSABLE_OPERATIONS)
    │ ◄──── ENTER_OTP             suite du parcours : OTP, débit, exécution
```

Un **refus** à n’importe quel niveau renvoie l’opération à l’initiateur, qui la corrige ; la
nouvelle soumission relance une **évaluation complète** (nouveau circuit depuis L1).

### Les quatre briques

| Brique | Où elle vit | Ce qu’elle décide |
|--------|-------------|-------------------|
| **Politique** (`approvalPolicy`) | `configuration` de la version catalogue du produit | Validation activée ou non, DMN à utiliser, règles maker/checker, champs critiques |
| **DMN** | `payment-channel-api/.../modules/payment-admin-module/decisions/*.dmn` | Contexte de l’opération → **code de profil** |
| **Profil** | Table `approval_profile` (API admin) | Niveaux, rôles validateurs, nombre de votes requis par niveau |
| **BPMN** | Processus du produit | Où se place l’évaluation, tâches `APPROVAL_L1..L3`, boucle de correction |

Au runtime, `ApprovalEngine` crée une **instance** (`approval_instance`), une **étape par
niveau** (`approval_step_instance`) et trace chaque **décision** (`approval_decision`).

### Produits branchés

| Produit | Intégration BPMN | Validation active par défaut |
|---------|------------------|------------------------------|
| `ACCOUNT_TRANSFER` | Tâches en ligne dans `account-transfer.bpmn20.xml` | Oui |
| `RTGS` | Tâches en ligne dans `rtgs-transfer.bpmn20.xml` | Oui |
| `SWIFT` | Tâches en ligne dans `swift-transfer.bpmn20.xml` | Oui |
| `BILL_PAYMENT` | `callActivity` vers le sous-processus partagé `approvalCircuit` | Seulement si `approvalPolicy.enabled` est posé dans sa configuration |

---

## 2. Configurer un circuit (équipe payment-2)

L’ordre recommandé est : rôles → profils → DMN → simulation → politique produit → BPMN.

### 2.1 Rôles validateurs

Un validateur est identifié par ses **rôles payment-2** (table `gateway_role`). Le JWT porte
ces rôles et Spring Security les expose sous la forme `ROLE_<CODE>`.

Rôles validateurs livrés (migration `V57`) :

| Rôle | Usage dans les profils livrés |
|------|-------------------------------|
| `CHEF_AGENCE` | Niveau 1 |
| `RESPONSABLE_OPERATIONS` | Niveau 2 |
| `DIRECTEUR` | Niveau 3 |

- **Nouveau rôle** : l’ajouter par migration Flyway dans `gateway_role` (même modèle que `V57`).
- **Attribuer un rôle** : à la création via `POST /api/v1/admin/users` (`"roles": ["CHEF_AGENCE"]`).
  L’API ne permet pas de modifier les rôles d’un utilisateur existant : passer par une
  migration ou du SQL sur `gateway_user_role`.
- **Correspondance** : un rôle de profil `CHEF_AGENCE` correspond aux autorités `CHEF_AGENCE`
  ou `ROLE_CHEF_AGENCE` (insensible à la casse). Le préfixe se règle dans `application.yml` :

```yaml
gateway:
  approval:
    identity:
      role-prefix: ROLE_
      admin-sees-all: true   # un ADMIN voit toute la boîte de réception
```

### 2.2 Profils : niveaux, rôles, nombre de votes

Un profil est un JSON `definitionJson` stocké dans `approval_profile` :

```json
{
  "strategy": "SEQUENTIAL",
  "levels": [
    { "level": 1, "requiredApprovals": 1,
      "assignments": [ { "type": "ROLE", "value": "CHEF_AGENCE" } ] },
    { "level": 2, "requiredApprovals": 2,
      "assignments": [ { "type": "ROLE", "value": "RESPONSABLE_OPERATIONS" } ] }
  ]
}
```

| Champ | Rôle |
|-------|------|
| `level` | Numéro du niveau. Les niveaux sont triés par numéro et traités dans l’ordre. |
| `requiredApprovals` | Nombre d’approbations **distinctes** nécessaires pour clore le niveau (défaut `1`). Avec `2`, deux validateurs différents doivent approuver. |
| `assignments` | Qui peut valider : `{ "type": "ROLE" \| "GROUP", "value": "<code rôle>" }`. Plusieurs entrées = l’un **ou** l’autre rôle. |
| `strategy` | Seul `SEQUENTIAL` est appliqué. |

Profils livrés (migration `V56`) :

| Code | Niveaux |
|------|---------|
| `NO_APPROVAL` | aucun |
| `LEVEL_1` | L1 : `CHEF_AGENCE` |
| `LEVEL_1_2` | L1 : `CHEF_AGENCE` → L2 : `RESPONSABLE_OPERATIONS` |
| `HIGH_VALUE` | L1 : `CHEF_AGENCE` → L2 : `RESPONSABLE_OPERATIONS` ×2 votes → L3 : `DIRECTEUR` |

API d’administration (rôles `ADMIN` / `CONFIG_MANAGER` en écriture ; `SUPPORT` / `AUDITOR` en lecture) :

```http
GET  /api/v1/admin/approvals/profiles
GET  /api/v1/admin/approvals/profiles/{id}
POST /api/v1/admin/approvals/profiles
PUT  /api/v1/admin/approvals/profiles/{id}

{
  "code": "TRESORERIE_2N",
  "name": "Trésorerie — 2 niveaux",
  "description": "SWIFT hors zone",
  "definitionJson": "{\"strategy\":\"SEQUENTIAL\",\"levels\":[…]}",
  "actif": true
}
```

Écran équivalent dans le back-office : **`/approval-profiles`** (liste, édition, bouton *Simuler*).

> ⚠️ **3 niveaux maximum** : les processus BPMN ne déclarent que `APPROVAL_L1`, `APPROVAL_L2` et
> `APPROVAL_L3`. Un profil à 4 niveaux laisserait le parcours reprendre après L3 alors que le
> circuit n’est pas terminé.
>
> ⚠️ Les affectations `USER` sont acceptées dans le JSON mais **ne sont pas utilisées** : la
> boîte de réception ne filtre que sur `ROLE` / `GROUP`.

### 2.3 DMN : du contexte au profil

La DMN reçoit le contexte de l’opération et renvoie **un seul résultat** :
`approvalProfileCode`. Fichier de référence :
`modules/payment-admin-module/decisions/transfer-approval.dmn` (décision
`decision_TransferApproval`, hit policy `FIRST` : la première règle qui correspond l’emporte).

Variables d’entrée disponibles :

| Variable | Source |
|----------|--------|
| `amount` | Premier montant strictement positif parmi : montant de la transaction, `amount` du contexte, `amountDue` (factures) |
| `currency` | `currency` du contexte, sinon devise de la transaction, sinon `XOF` |
| `catalogServiceCode` | Code du produit (`ACCOUNT_TRANSFER`, `SWIFT`…) |
| `rail`, `customerType`, `agencyCode`, `riskScore` | Transmis **s’ils sont présents** dans le contexte. Pour les utiliser, déclarez-les comme `inputData` / `input` dans la DMN. |

Règles actuelles du fichier livré (XOF) :

| Condition | Profil |
|-----------|--------|
| montant ≤ 0 | `NO_APPROVAL` |
| montant ≤ 100 | `LEVEL_1` |
| montant ≤ 50 000 000 | `LEVEL_1_2` |
| au-delà | `HIGH_VALUE` |
| autre devise | `LEVEL_1` |

> Ces seuils sont vraisemblablement des valeurs de test (la description du profil `LEVEL_1`
> annonce 1 M – 10 M XOF) et ne distinguent pas les produits. Les tableaux de seuils de
> [`INTEGRATION.md`](INTEGRATION.md) ne reflètent pas ce fichier. **Ajustez la DMN avant toute
> mise en production.** L’ancien fichier `rules/transfer-approval.dmn` renvoie un nombre de
> niveaux et non un code de profil : il n’est pas compatible avec `ApprovalEngine`.

Exemple de règle propre à un produit, à placer **avant** les règles génériques :

```xml
<rule id="r_swift_high">
  <inputEntry><text>&gt;= 10000000</text></inputEntry>   <!-- amount -->
  <inputEntry><text>"XOF"</text></inputEntry>           <!-- currency -->
  <inputEntry><text>"SWIFT"</text></inputEntry>         <!-- catalogServiceCode -->
  <outputEntry><text>"HIGH_VALUE"</text></outputEntry>
</rule>
```

**DMN dédiée à un produit** : ajoutez un fichier `.dmn` dans `decisions/` avec sa propre
décision (ex. `decision_SwiftApproval`, sortie `approvalProfileCode`) et référencez-la dans la
politique (`dmnDecisionKey`, §2.5). Si cette décision échoue, le moteur réessaie la décision
nommée `TransferApproval`. Si aucune ne renvoie de profil, la soumission du récapitulatif
échoue avec `APPROVAL_PROFILE_UNRESOLVED` : il n’y a **pas de seuil codé en dur** en secours.

> ⚠️ Si la DMN renvoie un code de profil **inconnu ou inactif**, l’opération est traitée
> **sans validation**. Vérifiez chaque code renvoyé avec la simulation (§2.4).

### 2.4 Simuler avant de publier

```http
POST /api/v1/admin/approvals/simulate
Authorization: Bearer … (ADMIN | CONFIG_MANAGER | SUPPORT | AUDITOR)

{ "amount": 25000000, "currency": "XOF", "catalogServiceCode": "ACCOUNT_TRANSFER" }
```

```json
{
  "profileCode": "LEVEL_1_2",
  "strategy": "SEQUENTIAL",
  "levelCount": 2,
  "levels": [
    { "level": 1, "requiredApprovals": 1, "assignments": ["ROLE:CHEF_AGENCE"] },
    { "level": 2, "requiredApprovals": 1, "assignments": ["ROLE:RESPONSABLE_OPERATIONS"] }
  ],
  "source": "dmn"
}
```

Champs optionnels : `rail`, `customerType`, `agencyCode`. La simulation évalue toujours
`decision_TransferApproval` : elle ne teste pas une DMN dédiée déclarée par `dmnDecisionKey`.

### 2.5 Politique produit (`approvalPolicy`)

La politique se pose dans la `configuration` de la version catalogue du produit (payload de
`POST /api/v1/admin/catalog-services/products/publish`, voir
[`INTEGRATION.md` — Publier un produit](INTEGRATION.md#publier-un-produit-admin-sans-java)) :

```json
"approvalPolicy": {
  "code": "TRANSFER_APPROVAL",
  "enabled": true,
  "dmnDecisionKey": "decision_TransferApproval",
  "initiatorCanApprove": false,
  "sameUserAcrossLevelsAllowed": false,
  "criticalFields": ["amount", "currency", "sourceAccount", "destAccount", "beneficiaryAccount"]
}
```

| Clé | Défaut | Effet |
|-----|--------|-------|
| `enabled` | `true` si l’objet est présent | `false` (ou politique absente) : aucune validation, même si le BPMN contient les étapes. |
| `dmnDecisionKey` (alias `decisionKey`) | `decision_TransferApproval` | Décision DMN à évaluer. |
| `initiatorCanApprove` (alias `selfApprovalAllowed`) | `false` | Autorise l’initiateur à valider sa propre opération. |
| `sameUserAcrossLevelsAllowed` | `false` | Autorise un même validateur à approuver plusieurs niveaux. |
| `criticalFields` | `amount`, `currency`, `destAccount`, `sourceAccount` | Champs dont l’empreinte (SHA-256) est figée à l’évaluation. Si l’un change avant une décision, la décision est refusée (`APPROVAL_CONTEXT_CHANGED`). |
| `code` | — | Libellé de la politique, conservé dans l’instance pour l’audit. |

Choisissez des `criticalFields` qui existent dans le contexte du produit. Par exemple, pour
`BILL_PAYMENT` : `amount`, `currency`, `billerCode`, `billReference`, `productCode`,
`subscriberReference`.

> Les configurations RTGS et SWIFT contiennent encore une liste `levels` / `candidateGroup`
> héritée : elle est **ignorée**. Les niveaux viennent uniquement du profil.

### 2.6 BPMN : placer le circuit dans le parcours

Placez l’évaluation **après le récapitulatif et avant l’authentification (OTP) et le débit**,
pour que le montant et les champs critiques soient définitifs. Deux modèles existent.

#### Modèle A — tâches en ligne (ACCOUNT_TRANSFER, RTGS, SWIFT)

```xml
<userTask id="CONFIRM" flowable:formKey="account-transfer-confirm"/>
<sequenceFlow sourceRef="CONFIRM" targetRef="EVALUATE_APPROVAL"/>

<serviceTask id="EVALUATE_APPROVAL" flowable:delegateExpression="${gatewayTransferApprovalEvaluateDelegate}"/>
<!-- ou : flowable:delegateExpression="${catalogStepDelegate}" pay:capability="EVALUATE_APPROVAL" -->
<sequenceFlow sourceRef="EVALUATE_APPROVAL" targetRef="gw_approvalNeeded"/>

<exclusiveGateway id="gw_approvalNeeded"/>
<sequenceFlow sourceRef="gw_approvalNeeded" targetRef="generateOtp">
  <conditionExpression>${approvalLevelsRequired == null || approvalLevelsRequired &lt; 1}</conditionExpression>
</sequenceFlow>
<sequenceFlow sourceRef="gw_approvalNeeded" targetRef="APPROVAL_L1">
  <conditionExpression>${approvalLevelsRequired &gt;= 1}</conditionExpression>
</sequenceFlow>

<userTask id="APPROVAL_L1" flowable:formKey="account-transfer-approval"
          flowable:candidateGroups="${approvalCandidateGroupL1}"/>
<exclusiveGateway id="gw_l1_result"/>
<!-- approved == false            → APPROVAL_RETURNED
     approved && levels >= 2      → APPROVAL_L2
     approved && levels < 2       → generateOtp
     (idem pour L2 → L3, puis L3 → generateOtp) -->

<serviceTask id="APPROVAL_RETURNED" flowable:delegateExpression="${gatewayCatalogStepDelegate}"/>
<sequenceFlow sourceRef="APPROVAL_RETURNED" targetRef="CORRECT_TRANSFER"/>
<userTask id="CORRECT_TRANSFER" flowable:formKey="account-transfer-return"/>
<sequenceFlow sourceRef="CORRECT_TRANSFER" targetRef="CONFIRM"/>
```

Points à respecter :

- Les tâches de validation doivent être **reconnues comme telles** : `id` commençant par
  `APPROVAL`, ou `formKey` dont le formulaire déclare `"gateway": { "nextAction": "APPROVAL" }`
  (cas de `account-transfer-approval`). Sinon `APPROVE` / `REJECT` sont refusés
  (`INVALID_ACTION`).
- `APPROVAL_RETURNED` passe la transaction en `RETURNED_FOR_CORRECTION` avec le motif de refus.
  `APPROVAL_REJECTED` (même capability `APPROVAL_OUTCOME`) l’**annule** à la place, si vous
  préférez un refus définitif.
- La boucle de correction doit revenir **avant** `EVALUATE_APPROVAL` pour que le circuit soit
  réévalué sur les nouvelles valeurs.

#### Modèle B — sous-processus partagé `approvalCircuit` (BILL_PAYMENT)

```xml
<callActivity id="approvalCircuit" calledElement="approvalCircuit"
              flowable:businessKey="${paymentId}">
  <extensionElements>
    <flowable:in  source="paymentId"          target="paymentId"/>
    <flowable:in  source="amount"             target="amount"/>
    <flowable:in  source="currency"           target="currency"/>
    <flowable:in  source="catalogServiceCode" target="catalogServiceCode"/>
    <flowable:out source="approvalRejected"   target="approvalRejected"/>
    <flowable:out source="approvalLevelsRequired" target="approvalLevelsRequired"/>
    <flowable:out source="approvalProfileCode"    target="approvalProfileCode"/>
  </extensionElements>
</callActivity>
<exclusiveGateway id="gw_after_approval" default="flow_to_pay"/>
<!-- approvalRejected == true → tâche de correction → retour au récapitulatif -->
```

Le sous-processus contient l’évaluation et les trois niveaux. Le produit n’a qu’à traiter
`approvalRejected`. Ce modèle évite de dupliquer les passerelles ; il ne passe pas par
`APPROVAL_RETURNED` : la correction est une simple tâche de formulaire du produit.

#### Variables posées dans le processus

| Variable | Posée par | Contenu |
|----------|-----------|---------|
| `approvalLevelsRequired` | Évaluation | Nombre de niveaux (0 à 3) |
| `approvalProfileCode` | Évaluation | Code du profil retenu |
| `approvalCandidateGroupL1..L3` | Évaluation | Rôles du niveau, séparés par des virgules |
| `approvalInstanceId`, `approvalRequired` | Évaluation | Identifiant d’instance, booléen |
| `approved`, `approvalComment`, `approverId` | Chaque niveau clos | Résultat de la dernière décision |
| `approvalRejected` | `approvalCircuit` uniquement | `true` si un niveau a refusé |

Une fois le BPMN modifié, `GET /api/v1/admin/catalog-services/versions/{versionId}/workflow/steps`
doit montrer `EVALUATE_APPROVAL` résolu sans erreur avant publication.

---

## 3. Intégrer côté application

L’intégration distingue deux utilisateurs : l’**initiateur**, qui suit le parcours produit
normal, et le **validateur**, qui travaille depuis une boîte de réception.

### 3.1 Côté initiateur

Rien de spécifique à coder : le client générique gère déjà le cas.

1. Après le récapitulatif, l’écran renvoyé est `WAIT` avec `meta.status = "WAITING_APPROVAL"`.
2. Une validation peut prendre des heures. **Ne bouclez pas indéfiniment** : persistez
   `meta.paymentId`, affichez « en attente de validation » et reprenez plus tard avec
   `GET /api/v1/gateway/payments/{paymentId}`.
3. Quand tous les niveaux ont approuvé, l’écran suivant est en général `ENTER_OTP` : c’est
   l’**initiateur** qui authentifie l’opération.
4. En cas de refus, l’écran devient un `FORM` de correction. Le motif est disponible dans
   `rejectionComment` via `GET /api/v1/gateway/approvals/by-payment/{paymentId}`. Après
   `SUBMIT_FORM`, le parcours revient au récapitulatif puis à une nouvelle évaluation.

C’est le seul cas où lire `meta.status` est justifié. Il permet de sortir de la boucle de polling :

```typescript
case 'WAIT':
  if (screen.meta?.status === 'WAITING_APPROVAL') {
    return screen; // l'app reprendra plus tard avec client.get(paymentId)
  }
  // … polling habituel
```

### 3.2 Côté validateur

Toutes les routes ci-dessous exigent le **token du validateur**.

| Besoin | Appel |
|--------|-------|
| Opérations à valider | `GET /api/v1/gateway/approvals/inbox` |
| Détail du circuit (niveaux, votes, historique) | `GET /api/v1/gateway/approvals/by-payment/{paymentId}` |
| Décider | `POST /api/v1/gateway/approvals/{paymentId}/decide` |

Les mêmes opérations existent sous `/api/v1/approvals/*` (réponse au format `PaymentResponse`
au lieu d’un écran) ; préférez les routes `/gateway`.

**Boîte de réception** : elle ne contient que les niveaux **en cours** dont un rôle correspond
à ceux de l’utilisateur. Un `ADMIN` voit tout si `admin-sees-all` est actif.

```json
[
  {
    "stepInstanceId": "…",
    "approvalInstanceId": "…",
    "paymentId": "PAY-…",
    "profileCode": "HIGH_VALUE",
    "levelNumber": 2,
    "requiredApprovals": 2,
    "collectedApprovals": 1,
    "candidateGroups": "RESPONSABLE_OPERATIONS",
    "initiatedBy": "jdupont",
    "amount": 75000000,
    "currency": "XOF",
    "paymentStatus": "WAITING_APPROVAL",
    "createdAt": "2026-10-04T09:12:00Z"
  }
]
```

**Détail** : statut de l’instance (`IN_PROGRESS`, `APPROVED`, `REJECTED`, `CANCELLED`,
`INVALIDATED`), `currentLevel` / `totalLevels`, `rejectionComment`, et pour chaque niveau
(`steps[]`) son statut (`PENDING`, `WAITING`, `APPROVED`, `REJECTED`), ses votes et la liste
des décisions (validateur, rôle, résultat, commentaire, date).

**Décision** :

```http
POST /api/v1/gateway/approvals/{paymentId}/decide
{ "action": "APPROVE", "data": { "approvalComment": "Conforme" } }
```

```http
POST /api/v1/gateway/approvals/{paymentId}/decide
{ "action": "REJECT", "data": { "approvalComment": "IBAN bénéficiaire erroné" } }
```

`approvalComment` (alias `comment`) est facultatif, mais **renseignez-le pour un refus** : c’est
le motif présenté à l’initiateur.

| Cas | Effet | Statut de la transaction |
|-----|-------|--------------------------|
| `APPROVE`, votes encore insuffisants (`requiredApprovals` > 1) | Vote enregistré, le niveau reste ouvert pour un autre validateur | `WAITING_APPROVAL` |
| `APPROVE`, niveau complet, niveau suivant prévu | Le niveau suivant s’ouvre et apparaît dans la boîte du rôle suivant | `WAITING_APPROVAL` |
| `APPROVE`, dernier niveau | Circuit `APPROVED`, le parcours reprend (OTP de l’initiateur…) | Selon l’étape suivante |
| `REJECT` (à tout niveau) | Circuit `REJECTED`, renvoi à l’initiateur | `RETURNED_FOR_CORRECTION` |

> La réponse de `decide` est l’**écran courant de la transaction**, destiné à l’initiateur
> (par exemple `ENTER_OTP` après la dernière approbation). Une app validateur ne doit pas
> l’afficher comme une étape à suivre : rafraîchissez la boîte de réception ou le détail.

**Notifications** : payment-2 n’en envoie aucune (l’étape `NOTIFY` est vide). Rafraîchissez la
boîte de réception à l’ouverture de l’écran et périodiquement (30 à 60 s).

### 3.3 Client validateur (TypeScript)

```typescript
export interface InboxItem {
  paymentId: string;
  profileCode: string;
  levelNumber: number;
  requiredApprovals: number;
  collectedApprovals: number;
  candidateGroups: string;
  initiatedBy: string;
  amount: number;
  currency: string;
  createdAt: string;
}

export class Payment2ApprovalClient {
  constructor(private readonly baseUrl: string, private readonly getToken: () => Promise<string>) {}

  private async call<T>(path: string, init: RequestInit = {}): Promise<T> {
    const res = await fetch(`${this.baseUrl}/api/v1/gateway/approvals${path}`, {
      ...init,
      headers: {
        Authorization: `Bearer ${await this.getToken()}`,
        'Content-Type': 'application/json',
        'Accept-Language': 'fr',
      },
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw Object.assign(new Error(err.message ?? res.statusText), { code: err.code, status: res.status });
    }
    return res.json();
  }

  inbox(): Promise<InboxItem[]> {
    return this.call('/inbox');
  }

  detail(paymentId: string): Promise<Record<string, unknown>> {
    return this.call(`/by-payment/${encodeURIComponent(paymentId)}`);
  }

  async decide(paymentId: string, approve: boolean, comment?: string): Promise<void> {
    await this.call(`/${encodeURIComponent(paymentId)}/decide`, {
      method: 'POST',
      body: JSON.stringify({
        action: approve ? 'APPROVE' : 'REJECT',
        data: comment ? { approvalComment: comment } : {},
      }),
    });
  }
}
```

Utilisation :

```typescript
const approvals = new Payment2ApprovalClient('https://payment2.example.com', getValidatorToken);

const items = await approvals.inbox();
try {
  await approvals.decide(items[0].paymentId, true, 'Conforme');
} catch (e: any) {
  if (e.code === 'SELF_APPROVAL_FORBIDDEN' || e.code === 'MULTI_LEVEL_SAME_USER_FORBIDDEN') {
    // informer : un autre validateur doit traiter ce niveau
  }
}
const refreshed = await approvals.inbox();
```

---

## 4. Contrôles appliqués à chaque décision

Dans l’ordre :

1. Utilisateur authentifié (`APPROVER_REQUIRED` sinon).
2. Une tâche de validation est active sur la transaction (`INVALID_ACTION` sinon).
3. Le circuit est encore ouvert (`APPROVAL_ALREADY_CLOSED`).
4. **Maker / checker** : l’initiateur ne valide pas sa propre opération, sauf
   `initiatorCanApprove` (`SELF_APPROVAL_FORBIDDEN`).
5. **Séparation des niveaux** : un validateur qui a déjà approuvé un niveau clos ne peut pas
   approuver un autre niveau, sauf `sameUserAcrossLevelsAllowed`
   (`MULTI_LEVEL_SAME_USER_FORBIDDEN`).
6. **Intégrité** : l’empreinte des `criticalFields` n’a pas changé (`APPROVAL_CONTEXT_CHANGED`).
7. **Double vote** : un validateur ne vote qu’une fois par niveau (`ALREADY_DECIDED`).

> ⚠️ **Non contrôlé** : l’appartenance du validateur aux rôles du niveau. Seule la boîte de
> réception est filtrée ; tout utilisateur authentifié autre que l’initiateur qui connaît un
> `paymentId` peut décider. Tant que ce contrôle n’est pas ajouté côté payment-2, un BFF doit
> vérifier que le `paymentId` figure dans la boîte de réception de l’utilisateur avant
> d’appeler `decide`.

---

## 5. Erreurs

| Code | Quand | Réaction client |
|------|-------|-----------------|
| `INVALID_ACTION` | `action` différente de `APPROVE` / `REJECT`, ou aucune tâche de validation active (déjà traitée, ou parcours ailleurs) | Rafraîchir la boîte de réception |
| `SELF_APPROVAL_FORBIDDEN` (403) | L’initiateur tente de valider | Faire traiter par un autre validateur |
| `MULTI_LEVEL_SAME_USER_FORBIDDEN` (403) | Même validateur sur un second niveau | Faire traiter par un autre validateur |
| `ALREADY_DECIDED` | Second vote du même validateur sur un niveau à votes multiples | Attendre un autre validateur |
| `APPROVAL_CONTEXT_CHANGED` | Champ critique modifié depuis l’évaluation | Demander une nouvelle soumission à l’initiateur |
| `APPROVAL_ALREADY_CLOSED`, `NO_WAITING_STEP` | Circuit déjà terminé | Rafraîchir |
| `APPROVAL_NOT_FOUND` (404 sur le détail) | Pas de circuit pour ce paiement | Vérifier le `paymentId` |
| `APPROVER_REQUIRED` (403) | Appel sans utilisateur identifié | Vérifier le token |
| `APPROVAL_PROFILE_UNRESOLVED` | À la soumission du récapitulatif (côté initiateur) : la DMN ne renvoie aucun profil | Erreur de configuration, à remonter à l’équipe payment-2 |

---

## 6. Suivi et audit

- **Historique d’un circuit** : `GET /api/v1/gateway/approvals/by-payment/{paymentId}`.
- **Événements de la transaction** : `APPROVAL_EVALUATED` (profil et nombre de niveaux),
  `APPROVAL_PARTIAL`, `APPROVAL_GRANTED`, `APPROVAL_DENIED`, `APPROVAL_RETURNED`.
- **Tables** : `approval_instance` (avec un instantané de la politique et du profil au moment
  de l’évaluation), `approval_step_instance`, `approval_decision`.
- **Back-office** : `/approvals` (boîte de réception) et `/approval-profiles` (profils et
  simulation).

Modifier un profil ou la DMN n’affecte pas les circuits déjà ouverts : ils gardent l’instantané
pris à l’évaluation.

---

## 7. Tester

Comptes locaux (collection Postman) : `admin` / `Admin@123` (initiateur) et `chef` /
`Chef@1234` (validateur `CHEF_AGENCE`). Créez des comptes `RESPONSABLE_OPERATIONS` et
`DIRECTEUR` pour tester les niveaux 2 et 3, et **deux** comptes `RESPONSABLE_OPERATIONS` pour
le niveau à double vote de `HIGH_VALUE`.

Scénarios à couvrir :

1. Montant sans validation : `WAIT` n’apparaît pas, le parcours passe directement à l’OTP.
2. Un niveau : `WAIT`, puis `APPROVE` par `chef`, puis `ENTER_OTP` côté initiateur.
3. Deux ou trois niveaux : l’opération passe d’une boîte de réception à l’autre.
4. Double vote : après un premier `APPROVE`, le niveau reste ouvert (`collectedApprovals = 1`).
5. Auto-validation par l’initiateur : `SELF_APPROVAL_FORBIDDEN`.
6. Même validateur sur deux niveaux : `MULTI_LEVEL_SAME_USER_FORBIDDEN`.
7. Refus avec commentaire : `FORM` de correction, `rejectionComment` renseigné, puis nouvelle
   soumission et nouveau circuit.

---

## 8. Checklist : ajouter un circuit à un produit

1. Rôles validateurs présents dans `gateway_role` et attribués aux utilisateurs (§2.1).
2. Profils créés et actifs, 3 niveaux au plus (§2.2).
3. Règles DMN renvoyant ces profils, avec des règles par `catalogServiceCode` si besoin (§2.3).
4. Simulation vérifiée pour chaque tranche de montant et chaque devise (§2.4).
5. `approvalPolicy` posée dans la configuration du produit, avec des `criticalFields`
   adaptés (§2.5).
6. Étapes BPMN ajoutées entre le récapitulatif et l’OTP, avec la boucle de correction (§2.6).
7. Publication de la version, puis exécution des scénarios du §7.

---

## 9. Limites connues

| Limite | Conséquence |
|--------|-------------|
| 3 niveaux maximum (tâches BPMN `APPROVAL_L1..L3`) | Les profils à 4 niveaux ou plus ne sont pas supportés. |
| Stratégie `SEQUENTIAL` uniquement | Pas de validation parallèle entre niveaux. |
| Affectation `USER` non exploitée | Impossible de désigner nommément un validateur. |
| Profil inconnu ou inactif renvoyé par la DMN | Opération exécutée sans validation. |
| Rôle du validateur non vérifié lors de la décision | Voir §4. |
| Ni notification, ni expiration des validations en attente | Les opérations peuvent rester indéfiniment en `WAITING_APPROVAL`. |
| Seuils DMN livrés de test ; simulation limitée à `decision_TransferApproval` | À ajuster et vérifier avant la production. |
| `approval_instance.payment_id` est unique, alors qu’une nouvelle soumission après refus crée une seconde instance pour le même paiement *(déduit du code, non testé)* | La réévaluation après correction risque d’échouer sur une violation de contrainte. Tester le scénario 7 du §7 ; correctif probable : supprimer l’ancienne instance ou retirer la contrainte d’unicité. |
