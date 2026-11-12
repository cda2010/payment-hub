# Capabilities — concept, implémentation et référentiel

Ce document explique ce qu'est une **capability** dans payment-2, comment le moteur l'exécute,
et liste les capabilities disponibles. Il complète la section
[« Configurer les étapes de service dans le BPMN »](GUIDE-INTEGRATION-PRODUITS.md#configurer-les-étapes-de-service-dans-le-bpmn)
du guide d'intégration. L'architecture générale est décrite dans [`ARCHITECTURE.md`](ARCHITECTURE.md).

---

## 1. Le concept

Une capability est une **brique métier réutilisable** : « calculer les frais », « générer l'OTP »,
« exécuter le transfert »… Un produit du catalogue est un enchaînement de capabilities, décrit
dans le BPMN et paramétré dans la configuration catalogue, **sans écrire de Java**.

Chaque étape (`serviceTask`) répond à trois questions distinctes :

| Question | Porté par | Exemple |
|---|---|---|
| **Quoi faire ?** | la capability (`pay:capability`) | `EXECUTE_TRANSFER` |
| **Chez qui ?** | le rôle acteur (`pay:actorRole`) | `SOURCE` → le fournisseur du compte émetteur |
| **Comment, côté fournisseur ?** | l'opération (`pay:operation`) | `TRANSFERT`, `EXECUTE_SWIFT`… |

Grâce à cette séparation, une même capability sert des produits très différents :
`EXECUTE_TRANSFER` couvre un virement interne, un RTGS ou un SWIFT, seule l'opération change.

Il existe trois catégories :

- **PROVIDER** : la capability appelle un fournisseur (banque, opérateur mobile, facturier…)
  via son adaptateur.
- **LOCAL** : la capability s'exécute dans le gateway, sans appel externe (contrôles, calculs,
  clôture…).
- **APPROVAL** : évaluation du circuit de validation (maker / checker).

---

## 2. Déclarer une capability sur une étape

Dans le concepteur RapidBPMN (section « Capability (étape) ») ou directement dans le XML :

```xml
<definitions … xmlns:pay="http://payment-gateway/schema/bpmn/pay">
  <serviceTask id="EXECUTE_LINK" name="Rattacher le compte"
               flowable:delegateExpression="${catalogStepDelegate}"
               pay:capability="EXECUTE_LINK"
               pay:actorRole="SOURCE"/>
```

- `pay:capability` indique la brique exécutée. Sans elle, le moteur tente de la déduire
  (voir 3.5) ; pour un nouveau produit, renseignez-la toujours.
- `pay:actorRole` n'a de sens que pour une capability PROVIDER.
- `pay:operation` ne sert qu'à **remplacer** l'opération par défaut de la capability
  (par exemple `EXECUTE_TRANSFER` avec `pay:operation="EXECUTE_SWIFT"`). Il est ignoré pour une
  capability locale.

---

## 3. L'implémentation

Le code se trouve dans `payment-application/src/main/java/com/gateway/payment/catalog/capability/`.

### 3.1 Le contrat : `CapabilityHandler`

Chaque capability est un bean Spring qui implémente :

```java
public interface CapabilityHandler {

    /** Code stable (ex. CALCULATE_FEES, NOTIFY). */
    String code();

    /** Opération fournisseur appelée par défaut ; null pour une capability locale. */
    default String defaultOperation() {
        return null;
    }

    CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration);
}
```

`defaultOperation()` détermine la catégorie : `null` signifie LOCAL, une valeur signifie PROVIDER.
`EVALUATE_APPROVAL` est la seule capability classée APPROVAL.

### 3.2 Le contexte : `CapabilityExecutionContext`

Le handler reçoit tout ce dont il a besoin, sans dépendre de Flowable :

| Champ | Contenu |
|---|---|
| `paymentId` | identifiant de la transaction |
| `step` | identifiant de l'étape BPMN |
| `transaction` | la `PaymentTransaction` |
| `context` | le contexte JSON mutable de la transaction (saisies, résultats précédents) |
| `bindingParams` | les `params` du binding catalogue de l'étape |
| `input` | les données soumises à cette étape |
| `binding` | le binding catalogue complet (capability, opération, rôle…) |
| `setVar(key, value)` | pose une variable dans le processus Flowable |

### 3.3 Le résultat : `CapabilityResult`

Un handler **ne modifie pas le statut de la transaction lui-même**. Il renvoie un résultat :

- `executionVars` : variables à poser dans le processus ;
- `events` : traces métier (type + message) ;
- `softRulesMatched` : indique si une règle produit s'est déclenchée ;
- `terminal` : effet sur la transaction ;
- `statusMessage` : message associé.

Effets terminaux possibles :

| `TerminalEffect` | Statut appliqué |
|---|---|
| `NONE` | aucun changement, le processus continue |
| `SUCCESS` | `SUCCESS`, étape courante `SUCCESS` |
| `OTP_REQUIRED` | `OTP_REQUIRED` (l'écran suivant demande l'OTP) |
| `REDIRECT_REQUIRED` | `REDIRECT_REQUIRED` (redirection vers le fournisseur) |
| `WAITING_CALLBACK` | `WAITING_CALLBACK` (attente d'une notification fournisseur) |
| `PROCESSING` | `PROCESSING` |
| `RETURNED_FOR_CORRECTION` | retour à l'initiateur (`APPROVAL_RETURNED`) |
| `CANCELLED` | annulation (`APPROVAL_REJECTED`) |

C'est le `CapabilityDispatcher` qui applique ce résultat (`applyVars`, puis `applyTerminal`).
La gestion des statuts reste ainsi centralisée.

### 3.4 Le registre : `CapabilityRegistry`

Il collecte tous les beans `CapabilityHandler` au démarrage et les indexe par `code()`.
Une capability déclarée dans le BPMN mais absente du registre fait échouer l'étape
(`CATALOG_CAPABILITY_UNKNOWN`) et bloque la publication du produit.

### 3.5 La résolution : quelle capability pour cette étape ?

À l'exécution, la capability d'une étape est déterminée dans cet ordre :

1. le champ `capability` du **binding catalogue** de l'étape, s'il est renseigné. Il permet à
   plusieurs produits de partager un même BPMN en surchargeant certaines étapes ;
2. l'attribut BPMN **`pay:capability`** ;
3. sinon, une **inférence** par `CapabilityResolver` :
   - à partir de l'opération (`CapabilityOperationMapping`, par exemple `EXECUTE_SWIFT` →
     `EXECUTE_TRANSFER`),
   - puis à partir de l'identifiant de l'étape (`inferFromStep`, par exemple `calculateFees` →
     `CALCULATE_FEES`, `WAIT_*` → `WAIT`).

L'inférence existe pour la compatibilité : les produits créés avant `pay:capability` continuent
de fonctionner. Pour un nouveau produit, déclarez toujours `pay:capability` explicitement.

Cas particulier : un binding de kind `SOFT` reste un **contrôle local**. Même si son opération
correspond à une capability PROVIDER, il n'est pas transformé en appel fournisseur, car certains
fournisseurs n'exposent pas l'opération (par exemple `VALIDATE_ACCOUNT` → `GETINFOS`).

### 3.6 L'aiguillage Flowable : `catalogStepDelegate`

Toutes les `serviceTask` utilisent le délégué générique `${catalogStepDelegate}`. Il :

1. lit les attributs `pay:*` de la tâche (`BpmnStepAttributes`) ;
2. les fusionne avec le binding catalogue (`CatalogStepBindings.merge`) ;
3. résout la capability ;
4. route vers l'un des trois chemins :

```
                     ┌─ pas de capability ─────────────► LOCAL (softRules seules)
catalogStepDelegate ─┼─ EVALUATE_APPROVAL ─────────────► APPROVAL  (ApprovalEngine + DMN)
                     ├─ defaultOperation() non nulle ──► PROVIDER  (appel adaptateur)
                     └─ sinon ─────────────────────────► LOCAL     (handler local)
```

### 3.7 Le chemin PROVIDER

`ProviderCapabilitySupport.invoke` appelle l'adaptateur du fournisseur. L'opération envoyée est,
par ordre de priorité :

1. `params.operation` (ou `params.operationCode`) du binding catalogue de l'étape ;
2. le champ `operation` du binding catalogue, sinon l'attribut `pay:operation` de la tâche ;
3. la `defaultOperation()` de la capability ;
4. à défaut, l'opération associée à l'identifiant de l'étape dans `ProviderCallOperationRegistry`.

La variable de processus `operationCode` ne choisit pas l'opération envoyée. Le chemin PROVIDER
passe par `GatewayProviderCallDelegate`, qui s'en sert seulement :

- pour **déduire la capability** quand ni le binding ni `pay:capability` n'en indiquent une
  (par exemple `operationCode=EXECUTE_SWIFT` → `EXECUTE_TRANSFER`, qui appellera alors `TRANSFERT`
  si aucune opération n'est déclarée) ;
- comme **opération brute** quand aucune capability ne correspond (compatibilité ; l'étape est
  tracée `RAW_OPERATION:<code>`).

Pour remplacer l'opération d'une capability, utilisez donc `pay:operation` ou le binding
catalogue, pas `operationCode`.

Le fournisseur appelé est celui du participant désigné par le rôle acteur, dans le casting du
produit. `ProviderCallOperationRegistry` décrit pour chaque opération :

- le **rail** : `PRIMARY` (fournisseur principal), `PAYER` (moyen de paiement) ou `BILLER`
  (facturier) ;
- le comportement **en cas de succès** (`onSuccess`) : continuer, demander l'OTP, rediriger,
  clore en succès…

### 3.8 Les règles produit (softRules)

Beaucoup de capabilities locales (`VALIDATE_AMOUNT`, `VALIDATE_ACCOUNT`, `CALCULATE_FEES`,
`SOFT_RULES`…) ne codent pas la règle en dur. Elles évaluent les `softRules` définies pour
l'étape dans la configuration catalogue, via `SoftRuleCapabilitySupport` (moteur
`CatalogSoftRuleEngine`).

La même capability peut donc imposer un plafond de 500 000 dans un produit et une liste de pays
autorisés dans un autre. Attention : sans règle, `VALIDATE_ACCOUNT` ne contrôle rien (un
avertissement est émis à la publication).

### 3.9 Le contrôle à la publication

`CatalogWorkflowStepValidator` vérifie chaque `serviceTask` du BPMN avant la publication d'une
version de produit :

| Code | Niveau | Signification |
|---|---|---|
| `CATALOG_CAPABILITY_UNKNOWN` | erreur | capability absente du registre |
| `CATALOG_ACTOR_ROLE_UNKNOWN` | erreur | rôle acteur inconnu |
| `CATALOG_OPERATION_UNKNOWN` | avertissement | opération absente de `ProviderCallOperationRegistry` |
| `CATALOG_STEP_NO_EFFECT` | avertissement | étape sans capability ni règle : elle ne fait rien |
| `CATALOG_CONTROL_WITHOUT_RULES` | avertissement | contrôle (ex. `VALIDATE_ACCOUNT`) sans softRule |
| `CATALOG_STEP_UNKNOWN` | avertissement | règle ou binding visant une étape absente du BPMN |

Une erreur bloque la publication (`CATALOG_WORKFLOW_INVALID`). Le rapport complet est disponible
sur `GET /api/v1/admin/catalog-services/versions/{versionId}/workflow/steps`.

### 3.10 L'exposition au concepteur

`GET /api/v1/catalog/capabilities` (`CatalogStepVocabularyController`) renvoie le vocabulaire
complet : capabilities (code, libellé, description, catégorie, opération par défaut), opérations
fournisseur (code, rail, onSuccess) et rôles acteur. Le panneau « Capability (étape) » de
`@rapidframe/bpmn-designer` alimente ses listes déroulantes avec cet endpoint.

---

## 4. Référentiel des capabilities

Il existe **28 capabilities** : 13 PROVIDER, 14 LOCAL et 1 APPROVAL.
La liste à jour est toujours celle renvoyée par `GET /api/v1/catalog/capabilities`.

### 4.1 PROVIDER (13)

| Capability | Opération par défaut | Libellé | Description |
|---|---|---|---|
| `GET_INFOS` | `GETINFOS` | Consulter le compte | Appel partenaire GETINFOS |
| `GET_BALANCE` | `GET_BALANCE` | Consulter le solde | Appel partenaire GET_BALANCE |
| `GET_BILLS` | `LOOKUP_BILL` | Consulter les factures | Appel facturier LOOKUP_BILL |
| `PAY_BILL` | `PAY_BILL` | Payer la facture | Appel facturier PAY_BILL ; clôture en SUCCESS |
| `CREATE_PAYMENT` | `CREATE_PAYMENT` | Créer le paiement | Session partenaire ; redirection ou attente |
| `GENERATE_OTP` | `GENERATE_OTP` | Générer l'OTP | Appel partenaire GENERATE_OTP ; écran suivant ENTER_OTP |
| `CONFIRM_PAYMENT` | `CONFIRM_PAYMENT` | Confirmer le paiement | Appel partenaire CONFIRM_PAYMENT (après OTP) |
| `EXECUTE_TOPUP` | `CONFIRM_PAYMENT` | Créditer (top-up) | Appel partenaire CONFIRM_PAYMENT |
| `EXECUTE_TRANSFER` | `TRANSFERT` | Exécuter le transfert | Appel partenaire TRANSFERT |
| `EXECUTE_REMITTANCE` | `REMITTANCE` | Envoyer la remittance | Appel partenaire REMITTANCE |
| `EXECUTE_PAYOUT` | `PAYOUT` | Exécuter la remise | Appel partenaire PAYOUT |
| `EXECUTE_LINK` | `LINK` | Rattacher le compte | Appel partenaire LINK |
| `EXECUTE_UNLINK` | `UNLINK` | Détacher le compte | Appel partenaire UNLINK |

### 4.2 LOCAL (15)

| Capability | Libellé | Description |
|---|---|---|
| `VALIDATE_AMOUNT` | Valider le montant | Exécute les softRules de l'étape (montant, limites) |
| `VALIDATE_ACCOUNT` | Valider le compte | Exécute les softRules de l'étape ; sans règle, l'étape ne contrôle rien |
| `SOFT_RULES` | Règles seules | Exécute uniquement les softRules de l'étape |
| `CALCULATE_FEES` | Calculer les frais | Quote live **payment-pricing** ; softRule `DEFAULT_FEE` seulement en secours si déjà posée |
| `EVALUATE_RULES` | Évaluer plafonds / cumuls | Appel **payment-rules** + réserve si ALLOW ; REJECT → 422 |
| `GET_QUOTE` | Coter | FX + frais + durée de validité |
| `RESOLVE_DOCUMENTS` | Déterminer les pièces | DMN montant / partenaire → profil de pièces |
| `VALIDATE_DOCUMENTS` | Vérifier les pièces | Exige les pièces requises dans le contexte |
| `LOOKUP_REMITTANCE` | Rechercher l'envoi | Charge l'envoi par référence |
| `VALIDATE_PAYOUT` | Contrôler la remise | Contrôles locaux avant remise |
| `COMPLETE_PAYOUT` | Clôturer la remise | Clôt la réception et l'envoi parent |
| `GENERATE_CODE` | Générer un code | Code de retrait / dépôt |
| `WAIT` | Attendre | Attente agent / règlement |
| `APPROVAL_OUTCOME` | Issue de validation | Retour à l'initiateur ou rejet |
| `NOTIFY` | Notifier et clôturer | Clôture la transaction en SUCCESS |

### 4.3 APPROVAL (1)

| Capability | Libellé | Description |
|---|---|---|
| `EVALUATE_APPROVAL` | Évaluer la validation | DMN → nombre de niveaux (`approvalLevelsRequired`) |

---

## 5. Les axes de variation d'une étape

Il y a 28 types de capabilities, mais beaucoup plus de comportements possibles. Chaque étape
varie aussi selon l'opération, le rôle acteur et le paramétrage du binding.

### 5.1 Opérations fournisseur (30)

Une capability PROVIDER peut appeler une autre opération que celle par défaut.
Par exemple, `EXECUTE_TRANSFER` peut appeler `TRANSFERT`, `TRANSFER_EXECUTE`, `EXECUTE_RTGS`,
`EXECUTE_SWIFT`, `EXECUTE_CASHIN` ou `EXECUTE_CASHOUT`.

| Opération | Rail | En cas de succès |
|---|---|---|
| `GETINFOS`, `GET_INFOS` | PRIMARY | `BILL_LOOKED_UP` |
| `GET_BALANCE`, `CONSULTATIONSOLDE` | PRIMARY | `NONE` |
| `GET_QUOTE`, `QUOTE` | PRIMARY | `NONE` |
| `LOOKUP_BILL` | BILLER | `BILL_LOOKED_UP` |
| `BILL_LIST` | BILLER | `BILL_LISTED` |
| `PAY_BILL` | BILLER | `SUCCESS_TERMINAL` |
| `CREATE_PAYMENT` | PAYER | `REDIRECT_OR_WAIT` |
| `GENERATE_OTP` | PAYER | `OTP_REQUIRED` |
| `CONFIRM_PAYMENT`, `TOPUP`, `EXECUTE_TOPUP` | PAYER | `CONFIRM_OR_CONTINUE` |
| `TRANSFERT`, `TRANSFER_EXECUTE`, `EXECUTE_TRANSFER` | PRIMARY | `CONFIRM_OR_CONTINUE` |
| `EXECUTE_RTGS`, `EXECUTE_SWIFT` | PRIMARY | `CONFIRM_OR_CONTINUE` |
| `EXECUTE_CASHIN`, `EXECUTE_CASHOUT` | PRIMARY | `CONFIRM_OR_CONTINUE` |
| `REMITTANCE`, `EXECUTE_REMITTANCE` | PRIMARY | `CONFIRM_OR_CONTINUE` |
| `PAYOUT`, `EXECUTE_PAYOUT` | PRIMARY | `CONFIRM_OR_CONTINUE` |
| `LINK`, `EXECUTE_LINK`, `REGISTER` | PRIMARY | `CONFIRM_OR_CONTINUE` |
| `UNLINK`, `EXECUTE_UNLINK` | PRIMARY | `CONFIRM_OR_CONTINUE` |

### 5.2 Rôles acteur (8)

Le rôle désigne quel participant du casting du produit exécute l'appel.

| Rôle | Libellé |
|---|---|
| `SOURCE` | Compte émetteur |
| `DESTINATION` | Compte destinataire |
| `PAYER` | Payeur |
| `BILLER` | Facturier |
| `BENEFICIARY` | Bénéficiaire |
| `CORRESPONDENT` | Correspondant |
| `PROVIDER` | Opérateur / fournisseur métier |
| `AGENT_NETWORK` | Réseau agent |

### 5.3 Paramétrage du binding catalogue

La configuration catalogue de chaque produit apporte, par étape :

- des `params` métier transmis au handler (`bindingParams`) ;
- des `softRules` (seuils, pays, frais, pièces exigées…) ;
- des variantes par canal (`channelDefaults`) ;
- éventuellement une `capability` ou une `operation` qui surcharge le BPMN.

Les bindings ont un *kind* : `UI`, `INVOKE`, `SOFT`, `WAIT`, `APPROVAL` ou `CAPABILITY`.
Les bindings `UI` (formulaires, écrans) restent dans le catalogue ; le comportement des
`serviceTask` se déclare de préférence dans le BPMN.

---

## 6. Ajouter une nouvelle capability

### 6.1 Capability locale

1. Ajouter la constante dans `CapabilityCodes`.
2. Créer le handler :

```java
@Component
public class CheckKycCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;

    public CheckKycCapabilityHandler(SoftRuleCapabilitySupport softRules) {
        this.softRules = softRules;
    }

    @Override
    public String code() {
        return CapabilityCodes.CHECK_KYC;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        return CapabilityResult.softOnly(soft.matched(), soft.events(), soft.vars());
    }
}
```

3. Ajouter le libellé et la description dans la map `LABELS` de `CapabilityCatalogService`.

### 6.2 Capability fournisseur

1. Ajouter la constante dans `CapabilityCodes`.
2. Déclarer le handler dans `ProviderCapabilityHandlers.java` en étendant
   `AbstractProviderCapabilityHandler` :

```java
@Component
class ExecuteRefundCapabilityHandler extends AbstractProviderCapabilityHandler {
    ExecuteRefundCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.EXECUTE_REFUND, "REFUND", support);
    }
}
```

3. Enregistrer l'opération (`REFUND`) dans `ProviderCallOperationRegistry`, avec son rail et son
   comportement en cas de succès.
4. Ajouter la correspondance capability ↔ opération dans `CapabilityOperationMapping`.
5. Ajouter le libellé dans `CapabilityCatalogService`.
6. Implémenter l'opération dans les adaptateurs des fournisseurs concernés.

### 6.3 Ce qui est automatique

Une fois le bean créé et le backend redémarré, la capability :

- apparaît dans `GET /api/v1/catalog/capabilities` ;
- est sélectionnable dans le concepteur RapidBPMN ;
- est reconnue par le validateur de publication ;
- est exécutée par `catalogStepDelegate` sans autre modification.

---

## 7. Exemple de référence

Le produit `LINK` (`payment-channel-api/src/main/resources/modules/payment-admin-module/processes/account-link.bpmn20.xml`)
enchaîne quatre capabilities :

| Étape | Capability | Catégorie | Rôle |
|---|---|---|---|
| `VALIDATE_ACCOUNT` | `VALIDATE_ACCOUNT` | LOCAL | — |
| `generateOtp` | `GENERATE_OTP` | PROVIDER | `SOURCE` |
| `EXECUTE_LINK` | `EXECUTE_LINK` | PROVIDER | `SOURCE` |
| `NOTIFY` | `NOTIFY` | LOCAL | — |
