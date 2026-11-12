# Couche Omnicanal dans payment-2

Organisation miroir de `omnicanal-application` (`com.m2i.omnicanal.presentation.service.*`) :

| Package | Rôle |
|---------|------|
| `policy` | Retry, timeout, compensation, ErrorPolicyResolver |
| `response` | CanalTransferResponse, SpecRunResult, OrchErrorCode |
| `validation` | ChannelPayloadValidator |
| `idempotency` | CanalIdempotencyService (Idempotency-Key) |
| `concurrency` | StepClaimService / StepStateMachine |
| `capability` | Catalogue capacités FSE |
| `schema` | WorkflowSelectService (DMN match) |
| `maestro` | TransferOrchestrationGateway |
| `spec` | SpecDrivenTransferExecutor (+ compensation) |
| `orchestration` | TransactionProcessor |
| `flowable` | CallFseCompensateDelegate |
| `canal` | API canal (`boundaryByTypeMethode`, `omnicanalPresentation`) |
| `util` | Parse otrParametre |

## Non-régression

Ne remplace **pas** :
- `payment` (checkout OTP)
- `billing` (factures)
- `transfer` (rails / frais)
- `configuration` (providers)
- Admin RapidFrame

## Canal

```bash
curl -X POST 'http://localhost:8080/api/v1/boundaryByTypeMethode' \
  -H 'X-Api-Key: change-me-mobile' \
  --data-urlencode 'codeFSE=0011' \
  --data-urlencode 'codeMethode=CONSULTATIONSOLDE' \
  --data-urlencode 'otrParametre=numeroCompte=0011000003090|codeFse=0011'
```

Config : `gateway.canal.api-key` / `gateway.canal.enabled`.
