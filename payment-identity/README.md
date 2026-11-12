# payment-identity

Référentiel **Identité** de la plateforme (bounded context), déployé comme **application
Spring Boot autonome** avec sa propre base de données.

## Responsabilités

| Concept | Table | Rôle |
|---------|-------|------|
| Party | `identity_party` | Client / organisation |
| OrgNode | `identity_org_node` | Entité / sous-entité (arbre) |
| ChannelUser | `identity_channel_user` | Utilisateur canal (audience + rôle) |
| Membership | `identity_membership` | User ↔ org + scope |
| Profile | `identity_profile` (+ `_service`, `_grid`) | **Profil d'offre** : services disponibles et grilles tarifaires de référence |
| PartyProfile | `identity_party_profile` | Affectation d'un profil à une Party |
| UniteProfile | `identity_unite_profile` | Affectation d'un profil à une **unité du réseau** (réf. logique `unite_id`) |

**Profil d'offre.** Ce n'est pas un rôle d'administration. Un profil décrit *ce qu'un acteur peut
consommer* : la liste des **services / produits** du catalogue (codes gateway, ex. `CASH_IN`,
`BILL_PAYMENT`) et, pour chaque produit, la **grille tarifaire** de référence (code
payment-pricing, ex. `FEE-BILL`). Identity stocke ces codes sans appeler le gateway ni pricing.
Une Party **ou une unité du réseau** peut cumuler plusieurs profils ; l'offre résolue
(`GET …/offer`) est l'union des services, et en cas de conflit de grille (même produit + canal)
l'affectation la plus récente l'emporte. Seed démo parties : `RETAIL-STD`, `AGENT-STD`,
`CORPORATE-STD` (`V3__profils.sql`). Seed réseau : profils `FSE-STD`, `AGENCE-STD`,
`MASTER-STD`, `MARCHAND-STD`, `SD-STD` puis affectation aux unités UEMOA/CEMAC
(`V5` + `V6__seed_unite_profiles.sql`).

**Ne gère pas** : orchestration paiement, soldes, alias de comptes (voir **payment-alias** :
Party / Org / Unité network), authentification (les tokens sont émis par Keycloak,
ou par payment-2 pour l'ancien login).

## API

Base : `/api/v1/identity` — Swagger : `http://localhost:8094/swagger-ui.html`

- `GET/POST /parties`, `GET /parties/{id}`
- `GET /org-nodes`, `/org-nodes/tree`, `/org-nodes/legal-entities`, `/org-nodes/{id}/children`, `POST /org-nodes`
- `GET/POST /users`, `GET /users/by-username/{username}`
- `GET /users/{id}/memberships`, `POST /memberships`
- `GET/POST /profiles`, `GET/PUT /profiles/{id}` — catalogue des profils d'offre
- `GET/POST /parties/{id}/profiles`, `DELETE /parties/{partyId}/profiles/{profileId}` — affectation Party
- `GET /parties/{id}/offer`, `GET /users/{id}/offer` — offre résolue Party / utilisateur
- `GET/POST /unites/{id}/profiles`, `DELETE /unites/{uniteId}/profiles/{profileId}` — affectation unité réseau
- `GET /unites/{id}/offer` — offre résolue d'une unité (`ownerKind=UNITE`)

Lecture : tout utilisateur authentifié. Création / affectation : rôles `ADMIN`, `CONFIG_MANAGER`,
`OPERATOR` ou `GROUP_ADMIN` (création de profil réservée à `ADMIN` / `CONFIG_MANAGER` /
`OPERATOR`). Pas encore d'isolation par tenant pour `GROUP_ADMIN`.
Les erreurs suivent le format `ApiError` de payment-2 (`timestamp`, `status`, `code`, `message`, `path`).

Tenant par défaut seed : `CORIS_DEMO`.

## Authentification

Le service ne connaît pas les utilisateurs. Il accepte deux sortes d'access tokens, choisies
d'après le claim `iss` :

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

Les rôles viennent du claim `roles` du token. Un utilisateur désactivé reste donc accepté ici
jusqu'à l'expiration de son access token (5 min pour Keycloak, 15 min pour l'ancien login).
La colonne `identity_channel_user.keycloak_user_id` (migration V2) permet de rattacher un
utilisateur canal à son `sub` Keycloak.

## Lancer en local

```bash
# Base dédiée (port 5436)
docker compose up -d identity-postgres

# Depuis la racine payment-2
./mvnw -pl payment-identity spring-boot:run          # profil local par défaut via Maven
# ou
./mvnw -pl payment-identity -DskipTests package
java -jar payment-identity/target/payment-identity.jar --spring.profiles.active=local
```

Service sur `http://localhost:8094` (8090 est pris par bulk-payment). Flyway crée le schéma
et le jeu de démo (`V1__identity_schema.sql`, `V2__channel_user_keycloak_link.sql`,
`V3__profils.sql`, `V4__drop_account_alias.sql` — les alias sont dans payment-alias).

Docker : `docker compose up -d identity` (image construite par `payment-identity/Dockerfile`).

## Configuration

| Variable | Défaut |
|----------|--------|
| `IDENTITY_DB_URL` | `jdbc:postgresql://localhost:5436/payment_identity` |
| `IDENTITY_DB_USER` / `IDENTITY_DB_PASSWORD` | `identity` / `identity` |
| `SERVER_PORT` | `8094` |
| `CORS_ORIGINS` | Origines locales des fronts (4200, 4210, 8100) |
| `KEYCLOAK_*` | Voir « Authentification » |

Comme pour le gateway, le profil par défaut de `java -jar` est `prod` ; passez
`--spring.profiles.active=local` en local.

## Clients

`web-app-corporate` appelle `/api/v1/identity/**` : son proxy de développement
(`proxy.conf.js`) route ce préfixe vers le port 8094, le reste de `/api` vers le gateway (8080).

`payment-front` (console admin) expose **Identité › Profils d'offre** (`/admin/identite/profils`) :
création / modification des profils (services + grilles) et affectation à une party. Sur la
**fiche d'une unité** du réseau, un panneau permet d'affecter / retirer des profils et d'afficher
l'offre résolue. Son
`proxy.conf.json` route `/api/v1/identity` vers le port 8094.

En déploiement, le reverse proxy doit appliquer la même règle.

## Migration des données

Les tables `identity_*` de la base du gateway (créées par `V111__identity_module.sql`) ne sont
plus lues. Elles sont conservées pour permettre de reprendre les données :

```bash
pg_dump -h <gateway-db> -U gateway -d payment_gateway --data-only \
  -t identity_party -t identity_org_node -t identity_account_alias \
  -t identity_channel_user -t identity_membership > identity-data.sql
```

Le dump s'applique sur une base `payment_identity` dont le seed de démo a été vidé (mêmes UUID).
Une fois la reprise validée, les tables pourront être supprimées par une migration du gateway.
