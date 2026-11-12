# Payment Gateway (`payment-parent`)

Gateway de paiement + couche Omnicanal (schéma / canal / SpecDriven), selon :

`Configuration + Payment Flow + Connector (app séparée) = Payment Execution`

Les **connecteurs fournisseurs** (Orange Money, Coris, Wave, …) ne sont **pas** embarqués
dans ce reactor : ce sont des **applications Spring Boot séparées**.
`payment-application` ne conserve que le port d'invocation (journal + circuit breaker).

## Modules

| Module | Rôle |
|--------|------|
| `payment-domain` | Entités JPA, enums, repositories, contrats provider |
| `payment-orchestration` | Schéma, LegCompiler, SpecDriven, canal Omnicanal |
| `payment-application` | Payment / billing / transfer / security / BPM checkout |
| `payment-channel-api` | Boot entry, Flyway, yml, modules RapidFrame |
| `payment-front` | Angular (checkout + admin RapidFrame) |

Dépendances : `channel-api → application → orchestration → domain`

## Stack

| Couche | Techno |
|--------|--------|
| Backend | Java 17, Spring Boot 3.3, Maven reactor, JPA, Flyway, Security/JWT, OpenAPI |
| Frontend | Angular 19 + RapidFrame admin |
| Data | PostgreSQL 16, Redis, AES-GCM secrets |
| Ops | Docker Compose, Actuator/Prometheus |

## Démarrage

```bash
# 1. PostgreSQL
docker compose up -d postgres

# 2. Backend (profil local = PostgreSQL)
./mvnw -pl payment-channel-api -am spring-boot:run

# 3. Frontend
cd payment-front
npm install
npm run sync:libs   # monApp/rapidframe-frontend → node_modules/@rapidframe
npm start
```

Profil Spring par défaut : **`local`**. H2 : `./mvnw -pl payment-channel-api -am spring-boot:run -Pdev`

**NetBeans** : ouvrir le projet Maven `payment-channel-api`, Run/Debug (action `nbactions.xml` → profil `local`).
Si le démarrage échoue sur `JwtService` / `WeakKeyException` : dans Run → Environment, retirer
`JWT_SECRET` s’il est vide (une variable vide écrase le secret par défaut).

- API / Swagger : http://localhost:8080/swagger-ui.html  
- UI : http://localhost:4200  
- Compte : `admin` / `Admin@123`
- Connexion via Keycloak (`docker compose up -d keycloak`, http://localhost:8180) :
  voir [keycloak/README.md](keycloak/README.md). L'ancien `POST /api/v1/auth/login` reste actif
  pour paymentHub2.

## Canal Omnicanal (équivalent)

- `POST /api/v1/canal/boundaryByTypeMethode` — exécution par type méthode (ex. CONSULTATIONSOLDE)
- `GET/POST /api/v1/canal/omnicanalPresentation` — présentation / params interface
- Auth canal : header `X-Api-Key` (`gateway.canal.api-key`)

## Intégration plateformes externes

**Contrat produit** (recommandé) : `/api/v1/gateway/*`  
→ [docs/INTEGRATION.md](docs/INTEGRATION.md)

| Endpoint | Produit |
|----------|---------|
| `POST /api/v1/gateway/products/{code}` | **Tout produit catalogue** |
| `POST /api/v1/gateway/checkout` | Alias CHECKOUT |
| `POST /api/v1/gateway/bill-payment` | Alias BILL_PAYMENT |
| `POST /api/v1/gateway/account-transfer` | Alias ACCOUNT_TRANSFER |
| `POST /api/v1/gateway/rtgs` | Alias RTGS |
| `POST /api/v1/gateway/swift` | Alias SWIFT |
| `POST /api/v1/gateway/cash-out` | Alias CASH_OUT |
| `POST /api/v1/gateway/cash-in` | Alias CASH_IN |
| `POST /api/v1/gateway/link` | Alias LINK (liaison compte) |
| `POST /api/v1/gateway/register` | Alias REGISTER → LINK |
| `GET/POST /api/v1/gateway/payments/{id}` | Mid-flow (écran) |
| `/api/v1/gateway/approvals/*` | Validations transfert |

Réponse : `IntegrationScreenResponse` (`type`, `formKey`, `formSchema`, `meta.paymentId`, …).

Postman : `docs/postman/Payment-2-Product-Gateway.postman_collection.json`

Référence UI : `monApp/paymentHub2` (même style d’API).


## Tests

```bash
./mvnw -pl payment-channel-api -am test
```

## Docker

```bash
docker compose up -d --build
```

Build context = racine ; Dockerfile = `payment-channel-api/Dockerfile`.
