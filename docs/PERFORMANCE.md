# Performance & scale — payment-2 (1000 trx/s, P99 &lt; 1s)

## Phases livrées

| Phase | Contenu |
|-------|---------|
| **1** | Pool HTTP partagé, caches Caffeine (interfaces + catalogue), TX courtes, audit async, Hikari |
| **2** | Redis (locks, claims, idempotence), jambes parallèles SpecDriven, cache catalogue, events async |
| **3** | PgBouncer, Redis LRU, scale `backend`, circuit breaker agressif, script k6 |
| **4** | Java 21 + virtual threads (Tomcat + HTTP), pool HTTP par host FSE |

## Démarrage stack

```bash
docker compose up -d --scale backend=3
```

- Postgres : `:5432` (max_connections=300)
- PgBouncer : `:6432` → backends passent par transaction pooling
- Redis : `:6379` (maxmemory 256mb, allkeys-lru)
- Backends : ports `8080-8082`

Prod active `gateway.performance.redis-enabled=true`.

## Load test

```bash
k6 run -e BASE_URL=http://localhost:8080 -e API_KEY=change-me-mobile \
  scripts/load/k6-omnicanal-canal.js
```

Seuils : P95 &lt; 800 ms, P99 &lt; 1 s, erreur &lt; 1 %.

## Config clé

```yaml
gateway.performance:
  redis-enabled: true
  parallel-legs: true
  async-audit: true
  virtual-threads: true
  http:
    connect-timeout-ms: 2000
    read-timeout-ms: 25000
    max-connections-per-route: 80
```

## Limites restantes

- Latence dominée par les connecteurs FSE (200–400 ms/jambe).
- Transfert 2 jambes dépendantes (DEBIT→CREDIT) reste séquentiel ; le parallélisme s’applique aux jambes sans `dependsOn`.
- Valider P99 sous charge réelle des FSE (mock vs prod).

## Mesure FSE (P2)

Objectif : séparer latence gateway vs connecteur.

1. Activer logs DEBUG `com.gateway.payment.payment.application.ConnectorHttpInvoker` (durée HTTP par jambe).
2. Baseline mock (sans FSE réel) :

```bash
k6 run -e BASE_URL=http://localhost:8080 -e API_KEY=change-me-mobile \
  scripts/load/k6-omnicanal-canal.js
```

3. Même scénario contre FSE de préprod ; comparer P95/P99 et `http_req_duration` vs temps connecteur dans les logs.
4. Flowable async (WAIT durable) : `FLOWABLE_ASYNC_EXECUTOR=true` en prod — ne pas activer en local sans jobs Flowable DB.
5. Outbox multi-consommateurs : `GATEWAY_OUTBOX_KAFKA=true` + `KAFKA_BOOTSTRAP_SERVERS=…` (topic `payment.domain-events`).

Seuil cible inchangé : P99 &lt; 1 s côté gateway hors latence FSE externe.
