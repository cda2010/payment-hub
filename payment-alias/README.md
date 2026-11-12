# payment-alias

Référentiel des **alias de comptes** : pointeurs stables vers un compte CBS / Coris / till.
Application Spring Boot autonome, base dédiée.

## Responsabilité

Un alias rattache une **référence technique de compte** à un propriétaire, qui peut être :

| `ownerKind` | Module source | Exemple |
|-------------|---------------|---------|
| `PARTY` | payment-identity | Wallet d'un particulier |
| `ORG` | payment-identity | Compte d'une entité légale / POS org |
| `UNITE` | payment-network | Till / compte d'une agence, master, SD… |

Ce module **ne valide pas** l'existence du propriétaire (UUID + kind seulement) et **ne détient pas** le solde.

## API

Base : `/api/v1/aliases` — Swagger : `http://localhost:8097/swagger-ui.html`

- `GET /aliases` — liste (filtres `tenantId`, `ownerKind`, `ownerId`)
- `GET /aliases/{id}`
- `GET /aliases/resolve/{aliasCode}`
- `POST /aliases`, `PUT /aliases/{id}`, `DELETE /aliases/{id}` (désactivation)

Authentification : mêmes tokens Keycloak / ancien login que les autres apps (`ADMIN`, `CONFIG_MANAGER`, `OPERATOR`, `GROUP_ADMIN` en écriture).

## Lancer en local

```bash
docker compose up -d alias-postgres
./mvnw -pl payment-alias spring-boot:run
# ou
./mvnw -pl payment-alias -DskipTests package
java -jar payment-alias/target/payment-alias.jar --spring.profiles.active=local
```

Port **8097**, base PostgreSQL port **5439** (`payment_alias` / `alias` / `alias`).

## Seed démo

`V1` reprend les alias identity + un till réseau `ALIAS-MS-SAHEL-TILL` (UNITE = MS-SAHEL).

## Migration depuis identity

Les alias étaient dans `identity_account_alias`. Ils sont repris dans `V1__alias_schema.sql` ;
identity les retire via `V4__drop_account_alias.sql`.
