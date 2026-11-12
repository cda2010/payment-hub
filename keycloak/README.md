# Keycloak — authentification de l'écosystème payment-2

Keycloak (realm `payment`) émet les tokens de tous les fronts autour de payment-2. payment-2,
payment-identity et payment-pricing acceptent **en plus** les tokens de l'ancien `POST /api/v1/auth/login`, toujours
utilisé par paymentHub2 (monApp), qui n'a pas été migré.

## Lancer

```bash
cd payment-2
docker compose up -d keycloak        # http://localhost:8180 — console : admin / admin
```

Le realm est importé depuis `realm-payment.json` au **premier** démarrage. `--import-realm` ignore
un realm déjà présent ; pour réimporter après modification du fichier :

```bash
docker compose rm -sf keycloak && docker volume rm payment-2_keycloak_data && docker compose up -d keycloak
```

Vérification de bout en bout (payment-2 sur 8080, payment-identity sur 8094, payment-pricing sur 8095 ;
les services non lancés sont ignorés) :

```bash
keycloak/smoke-test.sh
```

## Clients

Tous les clients interactifs sont **publics**, en **Authorization Code + PKCE (S256)**.

| Client | Application | Redirections |
|--------|-------------|--------------|
| `payment-front` | back-office payment-2 | `localhost:4200/*` |
| `web-app-corporate` | portail entreprise | `localhost` / `127.0.0.1:4210/*` |
| `web-app-backoffice` | console back-office | `localhost` / `127.0.0.1:4210/*` et `:4213/*` |
| `web-app-agent` | portail agent / marchand | `localhost` / `127.0.0.1:4211/*` |
| `payment2-mobile` | mobile-app (connexion directe depuis l'écran de l'app, voir plus bas) | `:4205`, `:8100`, `com.gateway.payment2.mobile://callback` |
| `mobile-agent` | mobile-app-agent | `:4206`, `com.gateway.payment2.agent://callback` |
| `mobile-marchand` | mobile-app-marchand | `:4207`, `com.gateway.payment2.marchand://callback` |
| `payment-api` | audience des API (aucun flux) | — |
| `payment-dev-tools` | curl / Postman / tests (password grant) | — **à supprimer en production** |

Chaque client ajoute au token les claims `roles` (rôles realm), `tenant_id`, `party_id`,
`audience` (BACKOFFICE, CORPORATE, AGENT, MERCHANT, RETAIL) et l'audience `payment-api`.

Token pour curl :

```bash
curl -s -d client_id=payment-dev-tools -d grant_type=password -d scope=openid \
  -d username=admin -d 'password=Admin@123' \
  http://localhost:8180/realms/payment/protocol/openid-connect/token
```

## Comptes de démo

Tenant `CORIS_DEMO`. Les `party_id` correspondent au seed de payment-identity.

| Utilisateur | Mot de passe | Rôle | Audience |
|-------------|--------------|------|----------|
| `admin` | `Admin@123` | ADMIN | BACKOFFICE |
| `chef`, `chef1` | `Chef@1234` | CHEF_AGENCE | BACKOFFICE |
| `ops1` | `Ops@1234` | RESPONSABLE_OPERATIONS | BACKOFFICE |
| `directeur` | `Dir@1234` | DIRECTEUR | BACKOFFICE |
| `auditeur` | `Audit@1234` | AUDITOR | BACKOFFICE |
| `jean` | `Jean@1234` | GROUP_ADMIN | CORPORATE |
| `paul` | `Paul@1234` | TREASURER | CORPORATE |
| `marie` | `Marie@1234` | ACCOUNTANT | CORPORATE |
| `agent1` | `Agent@1234` | MAKER | AGENT |
| `checker1` | `Check@1234` | CHECKER | AGENT |
| `marchand1` | `Marchand@1234` | OWNER | MERCHANT |
| `caissier` | `Caisse@1234` | CASHIER | MERCHANT |
| `awa` | `Awa@1234` | CUSTOMER | RETAIL |

Les comptes `gateway_user` de payment-2 (ancien login) sont indépendants : un changement de mot de
passe dans Keycloak ne les modifie pas.

## Côté API

Variables communes à payment-2, payment-identity et payment-pricing :

| Variable | Défaut | Rôle |
|----------|--------|------|
| `KEYCLOAK_ENABLED` | `true` | `false` : seul l'ancien token est accepté |
| `KEYCLOAK_ISSUER` | `http://localhost:8180/realms/payment` | Doit être identique au claim `iss` |
| `KEYCLOAK_JWK_SET_URI` | dérivé de l'issuer | URL interne des clés (Docker : `http://keycloak:8080/realms/payment/protocol/openid-connect/certs`) |
| `KEYCLOAK_AUDIENCE` | `payment-api` | Audience exigée |

Le filtre lit le claim `iss` sans le vérifier pour choisir le vérificateur : JWKS Keycloak
(RS256, issuer, audience, expiration) ou ancien HMAC. Un token invalide laisse la requête
anonyme, donc les routes protégées répondent 401.

`KC_HOSTNAME` fixe l'issuer à `http://localhost:8180` même quand le token est demandé depuis un
conteneur. En recette / production, renseigner la vraie URL publique à la fois dans
`KC_HOSTNAME`, `KEYCLOAK_ISSUER` et l'`issuer` / `url` des environnements des fronts.

## Côté fronts

- **Web (Angular)** : `keycloak-js`, initialisé dans `main.ts` avant le bootstrap
  (`check-sso` + `public/silent-check-sso.html`). L'intercepteur rafraîchit le token
  (`updateToken(30)`) et ne l'envoie qu'aux URL de même origine.
  payment-front recopie l'access token dans `localStorage['gw_access_token']` parce que les
  écrans RapidFrame le lisent à cet endroit.
- **mobile-app** : pas de page Keycloak. Au premier lancement, l'écran d'enrôlement (`/enroll`)
  envoie identifiant et mot de passe au endpoint token (`grant_type=password`, « Direct access
  grants » activé sur `payment2-mobile` uniquement), puis l'utilisateur choisit un code à
  4 chiffres. Le refresh token offline est chiffré en AES-GCM avec une clé dérivée du code
  (`core/auth/pin-vault.ts`). Ensuite, l'écran PIN (`/login`) déchiffre ce token et ouvre la
  session. 5 codes faux effacent l'enrôlement ; « Code oublié ? » révoque la session et
  redemande le mot de passe ; « Se déconnecter » verrouille seulement (retour à l'écran PIN).
- **mobile-app-agent, mobile-app-marchand** : `core/auth/oidc-client.ts` (code + PKCE). Dans le
  navigateur, redirection classique ; sur appareil, ouverture de `@capacitor/browser` et retour
  par le schéma `com.gateway.payment2.*://callback`. La session est stockée via Capacitor
  Preferences.

### Projets natifs

Les dossiers `android/` et `ios/` ne sont pas générés. Après `npx cap add android|ios`, déclarer
le schéma de retour :

```xml
<!-- android/app/src/main/AndroidManifest.xml, dans l'activité principale -->
<intent-filter>
  <action android:name="android.intent.action.VIEW" />
  <category android:name="android.intent.category.DEFAULT" />
  <category android:name="android.intent.category.BROWSABLE" />
  <data android:scheme="com.gateway.payment2.mobile" />
</intent-filter>
```

```xml
<!-- ios/App/App/Info.plist -->
<key>CFBundleURLTypes</key>
<array><dict>
  <key>CFBundleURLSchemes</key>
  <array><string>com.gateway.payment2.mobile</string></array>
</dict></array>
```

(remplacer `mobile` par `agent` ou `marchand` selon l'application). Sur émulateur Android,
`localhost` désigne l'émulateur : utiliser `10.0.2.2:8180` pour Keycloak, ce qui change l'issuer
(`KC_HOSTNAME`) ; le plus simple est d'utiliser l'IP du poste partout.

## Limites connues

- L'ancien login reste actif tant que paymentHub2 n'est pas migré.
- Le token de payment-front est aussi en `localStorage` (contrainte RapidFrame).
- Le code secret de mobile-app est vérifié sur l'appareil (déchiffrement), pas par le serveur :
  4 chiffres ne protègent pas contre quelqu'un qui copie le stockage de l'application. En
  production, protéger la clé par le Keystore / Keychain (biométrie) ou vérifier le code côté
  serveur. Le PIN de mobile-app-agent n'est qu'une étape d'interface.
- La connexion directe (`grant_type=password`) est déconseillée par OAuth 2.1 : elle exclut la
  double authentification gérée par Keycloak tant que l'enrôlement passe par l'écran de l'app.
- Capacitor Preferences n'est pas chiffré (agent, marchand) : prévoir un stockage Keychain / Keystore.
- `GROUP_ADMIN` peut écrire dans payment-identity sans isolation par tenant.
- bulk-payment, transaction-engine et banking-core ne sont pas reliés à Keycloak : ils doivent
  rester sur le réseau interne. Si l'un d'eux est exposé, lui ajouter la validation JWKS ou un
  client `client_credentials` pour les appels service à service.
