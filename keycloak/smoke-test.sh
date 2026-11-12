#!/usr/bin/env bash
# Vérifie que payment-2 (et payment-identity / payment-pricing si lancés) accepte les tokens Keycloak et les anciens tokens.
# Usage : keycloak/smoke-test.sh   (Keycloak sur :8180, payment-2 sur :8080, identity sur :8094, pricing sur :8095)
set -u
KC=${KC:-http://localhost:8180/realms/payment}
API=${API:-http://localhost:8080}
IDENTITY=${IDENTITY:-http://localhost:8094}
PRICING=${PRICING:-http://localhost:8095}

kc_token() {
  curl -s -d client_id=payment-dev-tools -d grant_type=password -d scope=openid \
    -d "username=$1" -d "password=$2" "$KC/protocol/openid-connect/token" |
    python3 -c "import sys,json;print(json.load(sys.stdin).get('access_token',''))"
}

# Altère le 1er caractère de la signature : le dernier ne porte parfois que des bits de bourrage base64url.
tamper() { local sig=${1##*.} c=A; [ "${sig:0:1}" = A ] && c=B; echo "${1%.*}.$c${sig:1}"; }

check() { # libellé, code attendu, curl args...
  local label=$1 expected=$2; shift 2
  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' "$@")
  if [ "$code" = "$expected" ]; then echo "OK   $label ($code)"; else echo "FAIL $label (attendu $expected, reçu $code)"; FAILED=1; fi
}

FAILED=0
legacy=$(curl -s -H 'Content-Type: application/json' -d '{"username":"admin","password":"Admin@123"}' \
  "$API/api/v1/auth/login" | python3 -c "import sys,json;print(json.load(sys.stdin).get('accessToken',''))")
admin=$(kc_token admin 'Admin@123')
jean=$(kc_token jean 'Jean@1234')
awa=$(kc_token awa 'Awa@1234')

check "payment-2 sans token"                        401 "$API/api/v1/admin/users"
check "payment-2 ancien token admin"                200 -H "Authorization: Bearer $legacy" "$API/api/v1/admin/users"
check "payment-2 Keycloak admin"                    200 -H "Authorization: Bearer $admin" "$API/api/v1/admin/users"
check "payment-2 Keycloak jean (GROUP_ADMIN) admin" 403 -H "Authorization: Bearer $jean" "$API/api/v1/admin/users"
check "payment-2 Keycloak awa catalogue publié"     200 -H "Authorization: Bearer $awa" "$API/api/v1/admin/catalog-services/published"
check "payment-2 token altéré"                      401 -H "Authorization: Bearer $(tamper "$admin")" "$API/api/v1/admin/users"
check "payment-2 route publique + token altéré"     200 -H "Authorization: Bearer $(tamper "$admin")" "$API/api/v1/ping"

if curl -s -o /dev/null "$IDENTITY/actuator/health"; then
  check "identity sans token"              401 "$IDENTITY/api/v1/identity/parties"
  check "identity Keycloak jean"           200 -H "Authorization: Bearer $jean" "$IDENTITY/api/v1/identity/parties"
  check "identity ancien token admin"      200 -H "Authorization: Bearer $legacy" "$IDENTITY/api/v1/identity/parties"
  check "identity Keycloak awa (écriture)" 403 -X POST -H 'Content-Type: application/json' -d '{}' \
    -H "Authorization: Bearer $awa" "$IDENTITY/api/v1/identity/parties"
else
  echo "SKIP payment-identity non joignable sur $IDENTITY"
fi

if curl -s -o /dev/null "$PRICING/actuator/health"; then
  quote='{"productCode":"ACCOUNT_TRANSFER","channel":"WEB","audience":"RETAIL","countryCode":"BF","amount":100000,"currency":"XOF"}'
  check "pricing sans token"               401 "$PRICING/api/v1/pricing/policies"
  check "pricing Keycloak awa"             200 -H "Authorization: Bearer $awa" "$PRICING/api/v1/pricing/policies"
  check "pricing ancien token admin"       200 -H "Authorization: Bearer $legacy" "$PRICING/api/v1/pricing/policies"
  check "pricing devis Keycloak awa"       200 -X POST -H 'Content-Type: application/json' -d "$quote" \
    -H "Authorization: Bearer $awa" "$PRICING/api/v1/pricing/quote"
  check "pricing calcul frais Keycloak awa" 200 -X POST -H 'Content-Type: application/json' \
    -d '{"produitCode":"TRANSFERT","canalCode":"MOBILE_MONEY","devise":"XOF","montant":20000}' \
    -H "Authorization: Bearer $awa" "$PRICING/api/v1/frais/calculer"
  check "pricing création grille awa"      403 -X POST -H 'Content-Type: application/json' -d '{}' \
    -H "Authorization: Bearer $awa" "$PRICING/api/v1/grilles"
  check "pricing token altéré"             401 -H "Authorization: Bearer $(tamper "$awa")" "$PRICING/api/v1/pricing/policies"
  check "gateway ne sert plus /pricing"    404 -H "Authorization: Bearer $admin" "$API/api/v1/pricing/policies"
else
  echo "SKIP payment-pricing non joignable sur $PRICING"
fi
exit $FAILED
