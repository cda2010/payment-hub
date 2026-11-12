# `@payment2/gateway-core`

Client **headless** (TypeScript pur) pour le Product Gateway payment-2.

## Contenu

| Module | Rôle |
|--------|------|
| Types | `IntegrationScreen`, champs, start body |
| `rf-datasource` | `rfDataSource` / `rfShowWhen` / query cascade |
| Form mapper | `formSchema` → champs plats pour rendu natif |
| `Payment2Client` | `start` / `get` / `act` / `run(ui)` |
| Classify | pass-through FORM, helpers écran |

**Hors scope :** rendu Ionic / PrimeNG / Form.io, auth (injecter `getToken`).

## Intégration dans une app

Procédure complète (npm, tsconfig paths, re-exports, HttpClient / `Payment2Client`,
rendu UI) :

→ [`docs/GUIDE-INTEGRATION-PRODUITS.md` §11](../../docs/GUIDE-INTEGRATION-PRODUITS.md#11-librairie-client-payment2gateway-core)

## Consommation rapide

```ts
import { Payment2Client, fieldsFromIntegrationScreen } from '@payment2/gateway-core';

const client = new Payment2Client(baseUrl, getToken, { channel: 'MOBILE', audience: 'RETAIL' });
const first = await client.start('SWIFT', { amount: 1500, currency: 'EUR' });
const fields = fieldsFromIntegrationScreen(first); // → votre UI
await client.run(first, myUi);
```
