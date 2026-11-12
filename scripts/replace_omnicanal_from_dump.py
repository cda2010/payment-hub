#!/usr/bin/env python3
"""
Remplace le catalogue FSE de payment_gateway par sql-base-corisnafa.sql.

Périmètre :
  fournisseur_service → payment_provider (code stable conservé :
  0001 ORANGE_MONEY, 0004 CORIS_BANK, 0011 CORIS_MONEY, les autres FSE_<code>)
  api / methode / parametre_methode → provider_fse_api, provider_operation, provider_api
  interface_utilisateur REQUEST + parametre_utilisateur
  relation / compte / type_compte → relation_fse, compte_fse, type_compte

Conservé :
  hôtes d'orchestration sans code_fse (CASH_IN, WAVE, BILL_PAYMENT, …)
  payment_flow, catalogue, payment_transaction, schema_ecriture BPMN

Les secrets bcrypt du dump ne sont pas recopiés. Les secrets en clair sont
chiffrés en AES-GCM comme SecretEncryptionService (gateway.encryption.secret-key).

Usage :
  python3 scripts/replace_omnicanal_from_dump.py --dry-run
  python3 scripts/replace_omnicanal_from_dump.py
"""

from __future__ import annotations

import argparse
import base64
import json
import os
import sys
import uuid
from collections import defaultdict
from pathlib import Path
from urllib.parse import urlparse

try:
    import psycopg2
    import psycopg2.extras
except ImportError:
    import subprocess

    subprocess.check_call([sys.executable, "-m", "pip", "install", "psycopg2-binary", "-q"])
    import psycopg2
    import psycopg2.extras

try:
    from cryptography.hazmat.primitives.ciphers.aead import AESGCM
except ImportError:
    import subprocess

    subprocess.check_call([sys.executable, "-m", "pip", "install", "cryptography", "-q"])
    from cryptography.hazmat.primitives.ciphers.aead import AESGCM


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DUMP = ROOT / "sql-base-corisnafa.sql"
DEFAULT_GATEWAY = os.environ.get(
    "GATEWAY_DB_URL", "postgresql://gateway:gateway@localhost:5432/payment_gateway"
)
DEFAULT_KEY = os.environ.get("ENCRYPTION_SECRET", "0123456789abcdef0123456789abcdef")

# Codes produit déjà branchés sur le catalogue. On les met à jour, on ne les recrée pas.
STABLE_PROVIDER_CODE = {
    "0001": "ORANGE_MONEY",
    "0004": "CORIS_BANK",
    "0011": "CORIS_MONEY",
}

PAYS_ISO = {
    1: "BF",
    2: "CI",
    3: "ML",
    5: "TG",
    6: "SN",
    7: "BJ",
    8: "NE",
    9: "GN",
    11: "MA",
    12: "TD",
    13: "GW",
}

# code_type_service / type_methode Omnicanal → ServiceTypeCode payment-2
SERVICE_MAP = {
    "TRANSFERT": "TRANSFERT",
    "16": "PAIEMENT_FACTURE",
    "10": "TRANSFERT",
    "11": "TRANSFERT",
    "12": "TRANSFERT",
    "13": "PAYEMENT",
    "14": "CONSULTATIONHIST",
    "15": "CONSULTATIONHIST",
    "17": "CONSULTATION_SOLDE",
    "001": "TRANSFERT",
    "002": "TRANSFERT",
    "003": "TRANSFERT",
    "004": "TRANSFERT",
    "005": "TRANSFERT",
    "006": "TRANSFERT",
    "007": "TRANSFERT",
    "008": "TRANSFERT",
    "009": "TRANSFERT",
    "011": "TRANSFERT",
    "012": "TRANSFERT",
}

METHOD_SERVICE = {
    "TRANSFERT": "TRANSFERT",
    "CREDIT": "TRANSFERT",
    "DEBIT": "TRANSFERT",
    "CONSULTATION": "CONSULTATION_SOLDE",
    "CONSULTATIONSOLDE": "CONSULTATION_SOLDE",
    "CONSULTATIONHIST": "CONSULTATIONHIST",
    "CONSULTATIONFACTURE": "PAIEMENT_FACTURE",
    "GETAIRTIME": "PAYEMENT",
    "SETTLEMENT": "PAYEMENT",
    "INFOCLIENT": "INFOCLIENT",
    "GETMONTANT": "PAIEMENT_FACTURE",
    "VERIFREGLEFACTURIER": "PAIEMENT_FACTURE",
}

EXPECTED_COUNTS = {
    "fournisseur_service": 118,
    "api": 106,
    "methode": 456,
    "parametre_methode": 1769,
    "interface_utilisateur": 393,
    "parametre_utilisateur": 2399,
    "relation": 30,
    "compte": 140,
    "type_compte": 19,
    "type_service": 19,
    "agence": 27,
    "pays": 11,
}

TABLES = list(EXPECTED_COUNTS)


def parse_db_url(url: str) -> dict:
    parsed = urlparse(url)
    return dict(
        host=parsed.hostname or "localhost",
        port=parsed.port or 5432,
        dbname=(parsed.path or "/").lstrip("/") or "postgres",
        user=parsed.username or "gateway",
        password=parsed.password or "",
    )


def clip(value, size: int):
    if value is None:
        return None
    text = str(value)
    return text if len(text) <= size else text[:size]


def is_bcrypt(value) -> bool:
    return isinstance(value, str) and value.startswith(("$2a$", "$2b$", "$2y$"))


def encrypt_secret(value, key: bytes):
    if value is None:
        return None
    text = str(value).strip()
    if not text or is_bcrypt(text):
        return None
    iv = os.urandom(12)
    cipher = AESGCM(key).encrypt(iv, text.encode("utf-8"), None)
    return base64.b64encode(iv + cipher).decode("ascii")


def map_service(code_type_service, type_methode) -> str:
    for raw in (type_methode, code_type_service):
        if raw is None:
            continue
        token = str(raw).strip()
        if token in SERVICE_MAP:
            return SERVICE_MAP[token]
        upper = token.upper()
        if upper in SERVICE_MAP:
            return SERVICE_MAP[upper]
        if upper in METHOD_SERVICE:
            return METHOD_SERVICE[upper]
    return "TRANSFERT"


def map_auth(raw) -> str:
    name = (raw or "NONE").strip().upper()
    if name in ("", "NOAUTH", "NONE"):
        return "NONE"
    if name in ("OAUTH", "OAUTH2"):
        return "OAUTH2_CLIENT_CREDENTIALS"
    if name in ("BASIC", "API_KEY", "BEARER_TOKEN", "CUSTOM_HEADER", "MTLS"):
        return name
    if name == "BEARER":
        return "BEARER_TOKEN"
    return "NONE"


def kind_and_method(statut, nom, type_etc):
    label = (nom or "").upper()
    etc = (type_etc or "").upper()
    status = (statut or "").upper()
    if any(word in label for word in ("MONEY", "ORANGE", "MOOV", "MTN", "WAVE")) or etc == "TEL":
        kind = "WALLET"
        method = "MOBILE_MONEY"
        execution = "OTP"
    elif "BANK" in label or "BANQUE" in label or status == "BANQUE" or etc == "FIL":
        kind = "BANQUE"
        method = "BANK_TRANSFER"
        execution = "DIRECT"
    elif any(word in label for word in ("MONEYGRAM", "RIA", "WESTERN")):
        kind = "REMITTANCE"
        method = "OTHER"
        execution = "DIRECT"
    elif status == "NONBANQUE":
        kind = "FACTURIER"
        method = "OTHER"
        execution = "DIRECT"
    else:
        kind = "FACTURIER"
        method = "OTHER"
        execution = "DIRECT"
    fse_statut = status if status in ("BANQUE", "NONBANQUE", "AUTRE") else "AUTRE"
    return kind, method, execution, fse_statut


def build_base_url(host, port, use_ssl, endpoint) -> str:
    if isinstance(endpoint, str) and endpoint.startswith("http"):
        return endpoint.rstrip("/")[:500]
    name = (host or "localhost").strip() or "localhost"
    scheme = "https" if use_ssl else "http"
    if port and str(port).strip():
        return f"{scheme}://{name}:{str(port).strip()}"[:500]
    return f"{scheme}://{name}"[:500]


def normalize_http(raw) -> str:
    method = (raw or "POST").strip().upper()
    if method not in ("GET", "POST", "PUT", "PATCH", "DELETE"):
        return "POST"
    return method


def normalize_path(raw) -> str:
    path = (raw or "").strip()
    if path.startswith("http://") or path.startswith("https://"):
        parsed = urlparse(path)
        path = parsed.path or "/"
    path = path.lstrip("/")
    return (path or "invoke")[:500]


class MysqlDump:
    def __init__(self, text: str):
        self.text = text
        self.rows = {name: [] for name in TABLES}

    def parse(self):
        for name in TABLES:
            statement = self._extract_insert(name)
            columns, tuples = self._parse_insert(statement)
            self.rows[name] = [dict(zip(columns, row)) for row in tuples]
        return self.rows

    def _extract_insert(self, table: str) -> str:
        needle = f"INSERT INTO `{table}`"
        start = self.text.find(needle)
        if start < 0:
            raise SystemExit(f"INSERT introuvable pour {table}")
        return self._read_statement(start)

    def _read_statement(self, start: int) -> str:
        chars = []
        i = start
        n = len(self.text)
        in_string = False
        while i < n:
            ch = self.text[i]
            if in_string:
                chars.append(ch)
                if ch == "\\":
                    if i + 1 < n:
                        chars.append(self.text[i + 1])
                        i += 2
                        continue
                elif ch == "'":
                    if i + 1 < n and self.text[i + 1] == "'":
                        chars.append("'")
                        i += 2
                        continue
                    in_string = False
                i += 1
                continue
            if ch == "'":
                in_string = True
                chars.append(ch)
                i += 1
                continue
            if ch == ";":
                break
            chars.append(ch)
            i += 1
        return "".join(chars)

    def _parse_insert(self, statement: str):
        open_paren = statement.find("(")
        if open_paren < 0:
            raise SystemExit("liste de colonnes absente")
        depth = 0
        cols_end = None
        for i in range(open_paren, len(statement)):
            if statement[i] == "(":
                depth += 1
            elif statement[i] == ")":
                depth -= 1
                if depth == 0:
                    cols_end = i
                    break
        columns = [
            part.strip().strip("`")
            for part in statement[open_paren + 1 : cols_end].split(",")
        ]
        values_at = statement.upper().find("VALUES", cols_end)
        body = statement[values_at + len("VALUES") :]
        tuples = []
        i = 0
        n = len(body)
        while i < n:
            while i < n and body[i] in " \r\n\t,":
                i += 1
            if i >= n:
                break
            if body[i] != "(":
                break
            row, i = self._parse_tuple(body, i)
            tuples.append(row)
        return columns, tuples

    def _parse_tuple(self, body: str, i: int):
        assert body[i] == "("
        i += 1
        values = []
        n = len(body)
        while i < n:
            while i < n and body[i] in " \r\n\t":
                i += 1
            if i < n and body[i] == ")":
                return values, i + 1
            value, i = self._parse_value(body, i)
            values.append(value)
            while i < n and body[i] in " \r\n\t":
                i += 1
            if i < n and body[i] == ",":
                i += 1
                continue
            if i < n and body[i] == ")":
                return values, i + 1
            raise SystemExit(f"tuple MySQL illisible vers {body[i:i+40]!r}")
        raise SystemExit("tuple MySQL non terminé")

    def _parse_value(self, body: str, i: int):
        if body.startswith("NULL", i) and (i + 4 == len(body) or body[i + 4] in ",) \r\n\t"):
            return None, i + 4
        if body.startswith("b'", i) or body.startswith("B'", i):
            bit = body[i + 2 : body.find("'", i + 2)]
            return bit == "1", body.find("'", i + 2) + 1
        if body[i] == "'":
            return self._parse_string(body, i)
        j = i
        if body[j] in "+-":
            j += 1
        while j < len(body) and (body[j].isdigit() or body[j] == "."):
            j += 1
        token = body[i:j]
        if token in ("", "+", "-", "."):
            raise SystemExit(f"valeur MySQL inattendue {body[i:i+40]!r}")
        if "." in token:
            return float(token), j
        return int(token), j

    def _parse_string(self, body: str, i: int):
        i += 1
        out = []
        n = len(body)
        while i < n:
            ch = body[i]
            if ch == "\\":
                nxt = body[i + 1] if i + 1 < n else ""
                out.append(
                    {
                        "0": "\0",
                        "b": "\b",
                        "n": "\n",
                        "r": "\r",
                        "t": "\t",
                        "Z": "\x1a",
                        "'": "'",
                        '"': '"',
                        "\\": "\\",
                    }.get(nxt, nxt)
                )
                i += 2
                continue
            if ch == "'":
                if i + 1 < n and body[i + 1] == "'":
                    out.append("'")
                    i += 2
                    continue
                return "".join(out), i + 1
            out.append(ch)
            i += 1
        raise SystemExit("chaîne MySQL non terminée")


def as_int(value):
    if value is None or value == "":
        return None
    return int(value)


def load_dump(path: Path) -> dict:
    text = path.read_text(encoding="utf-8")
    rows = MysqlDump(text).parse()
    mismatches = {
        name: (len(rows[name]), EXPECTED_COUNTS[name])
        for name in TABLES
        if len(rows[name]) != EXPECTED_COUNTS[name]
    }
    if mismatches:
        raise SystemExit(f"comptages dump inattendus: {mismatches}")
    return rows


def index_by_id(rows):
    return {as_int(row["id"]): row for row in rows}


def provider_code_for(code_fse: str) -> str:
    if code_fse in STABLE_PROVIDER_CODE:
        return STABLE_PROVIDER_CODE[code_fse]
    slug = "".join(ch if ch.isalnum() or ch == "_" else "_" for ch in code_fse.strip())
    return f"FSE_{slug}"[:50]


def operation_code(type_methode, code_methode, used: set[str]) -> str:
    base = (type_methode or "UNKNOWN").strip().upper() or "UNKNOWN"
    if base not in used and len(base) <= 50:
        code = base
    else:
        suffix = (code_methode or "").strip() or str(len(used) + 1)
        code = f"{base}_{suffix}"[:50]
    n = 2
    candidate = code
    while candidate in used:
        tail = f"_{n}"
        candidate = f"{code[:50 - len(tail)]}{tail}"
        n += 1
    used.add(candidate)
    return candidate


def prepare(rows: dict) -> dict:
    pays = index_by_id(rows["pays"])
    fournisseurs = index_by_id(rows["fournisseur_service"])
    apis = index_by_id(rows["api"])
    methodes = index_by_id(rows["methode"])
    type_services = index_by_id(rows["type_service"])
    type_comptes = index_by_id(rows["type_compte"])
    agences = index_by_id(rows["agence"])

    params_by_methode = defaultdict(list)
    for param in rows["parametre_methode"]:
        params_by_methode[as_int(param["methode_id"])].append(param)
    params_by_interface = defaultdict(list)
    for param in rows["parametre_utilisateur"]:
        params_by_interface[as_int(param["interface_utilisateur_id"])].append(param)
    apis_by_fse = defaultdict(list)
    for api in rows["api"]:
        fse_id = as_int(api["fournisseur_service_id"])
        if fse_id in fournisseurs:
            apis_by_fse[fse_id].append(api)
    methodes_by_api = defaultdict(list)
    for methode in rows["methode"]:
        methodes_by_api[as_int(methode["api_id"])].append(methode)

    return {
        "pays": pays,
        "fournisseurs": fournisseurs,
        "apis": apis,
        "methodes": methodes,
        "type_services": type_services,
        "type_comptes": type_comptes,
        "agences": agences,
        "params_by_methode": params_by_methode,
        "params_by_interface": params_by_interface,
        "apis_by_fse": apis_by_fse,
        "methodes_by_api": methodes_by_api,
        "relations": rows["relation"],
        "comptes": rows["compte"],
    }


def apply(gateway_url: str, model: dict, key: bytes, dry_run: bool) -> dict:
    stats = defaultdict(int)
    conn = psycopg2.connect(**parse_db_url(gateway_url))
    conn.autocommit = False
    try:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute("SELECT count(*) AS n FROM payment_flow")
            flows_before = cur.fetchone()["n"]
            cur.execute("SELECT count(*) AS n FROM schema_ecriture")
            schemas_before = cur.fetchone()["n"]
            cur.execute("SELECT count(*) AS n FROM payment_transaction")
            tx_before = cur.fetchone()["n"]
            cur.execute("SELECT code FROM service_type")
            service_codes = {row["code"] for row in cur.fetchall()}
            cur.execute(
                """
                SELECT id::text, code, code_fse, libelle
                FROM payment_provider
                """
            )
            providers = list(cur.fetchall())
            by_code = {row["code"]: row for row in providers}
            by_fse = {row["code_fse"]: row for row in providers if row["code_fse"]}

            canonical = {}
            for fse_id, fse in sorted(model["fournisseurs"].items(), key=lambda item: str(item[1]["code_fse"])):
                code_fse = str(fse["code_fse"]).strip()
                preferred = provider_code_for(code_fse)
                row = by_code.get(preferred) or by_fse.get(code_fse)
                if row is None:
                    new_id = str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:provider:{code_fse}"))
                    row = {"id": new_id, "code": preferred, "code_fse": None, "libelle": None, "new": True}
                    by_code[preferred] = row
                    stats["providers_created"] += 1
                else:
                    row = dict(row)
                    row["new"] = False
                    stats["providers_updated"] += 1
                other = by_fse.get(code_fse)
                if other and other["id"] != row["id"]:
                    _retire_duplicate(cur, other, stats, dry_run)
                    by_fse.pop(code_fse, None)
                by_fse[code_fse] = row
                canonical[code_fse] = (row, fse)

            if dry_run:
                stats["flows_before"] = flows_before
                stats["fournisseurs"] = len(canonical)
                stats["apis"] = sum(len(v) for v in model["apis_by_fse"].values())
                stats["methodes"] = len(model["methodes"])
                request_ifaces = [row for row in _all_interfaces(model) if _is_request(row)]
                stats["interfaces_request"] = len(request_ifaces)
                stats["interfaces_skipped"] = len(model["interfaces"]) - len(request_ifaces)
                conn.rollback()
                return stats

            for code_fse, (row, fse) in canonical.items():
                kind, method, execution, fse_statut = kind_and_method(
                    fse.get("statut"), fse.get("nom"), fse.get("typeETC")
                )
                pays = model["pays"].get(as_int(fse.get("pays_id")))
                country = PAYS_ISO.get(as_int(fse.get("pays_id")))
                if pays and country is None:
                    stats["pays_sans_iso"] += 1
                libelle = clip(fse.get("nom") or code_fse, 150)
                code_externe = clip(fse.get("code_externe") or code_fse, 50)
                group_code = clip(fse.get("groupe"), 50)
                if row.get("new"):
                    cur.execute(
                        """
                        INSERT INTO payment_provider (
                            id, code, libelle, description, actif, ordre_affichage,
                            mode_execution, payment_method_type, code_fse, code_externe,
                            group_code, country_code, participant_kind, fse_statut,
                            created_at, updated_at
                        ) VALUES (
                            %s::uuid, %s, %s, %s, true, 0,
                            %s, %s, %s, %s,
                            %s, %s, %s, %s,
                            now(), now()
                        )
                        """,
                        (
                            row["id"],
                            row["code"],
                            libelle,
                            f"Fournisseur Omnicanal {code_fse}",
                            execution,
                            method,
                            code_fse,
                            code_externe,
                            group_code,
                            country,
                            kind,
                            fse_statut,
                        ),
                    )
                else:
                    cur.execute(
                        """
                        UPDATE payment_provider SET
                            libelle = %s,
                            code_fse = %s,
                            code_externe = %s,
                            group_code = %s,
                            country_code = %s,
                            participant_kind = %s,
                            fse_statut = %s,
                            updated_at = now()
                        WHERE id = %s::uuid
                        """,
                        (
                            libelle,
                            code_fse,
                            code_externe,
                            group_code,
                            country,
                            kind,
                            fse_statut,
                            row["id"],
                        ),
                    )
                row["code_fse"] = code_fse

            target_ids = [row["id"] for row, _fse in canonical.values()]
            code_fses = list(canonical)

            cur.execute(
                """
                UPDATE interface_utilisateur SET provider_operation_id = NULL
                WHERE provider_operation_id IN (
                    SELECT id FROM provider_operation WHERE provider_id = ANY(%s::uuid[])
                )
                """,
                (target_ids,),
            )
            cur.execute(
                """
                UPDATE interface_utilisateur SET provider_api_id = NULL
                WHERE provider_api_id IN (
                    SELECT id FROM provider_api WHERE provider_id = ANY(%s::uuid[])
                )
                """,
                (target_ids,),
            )
            cur.execute(
                """
                DELETE FROM interface_utilisateur
                WHERE provider_id = ANY(%s::uuid[]) OR code_fse = ANY(%s)
                """,
                (target_ids, code_fses),
            )
            stats["interfaces_deleted"] = cur.rowcount
            cur.execute("DELETE FROM provider_api WHERE provider_id = ANY(%s::uuid[])", (target_ids,))
            stats["provider_api_deleted"] = cur.rowcount
            cur.execute(
                "DELETE FROM provider_operation WHERE provider_id = ANY(%s::uuid[])",
                (target_ids,),
            )
            stats["operations_deleted"] = cur.rowcount
            cur.execute(
                "DELETE FROM provider_fse_api WHERE provider_id = ANY(%s::uuid[])",
                (target_ids,),
            )
            stats["fse_api_deleted"] = cur.rowcount

            methode_to_op = {}
            methode_to_api = {}
            services_by_provider = defaultdict(set)

            for code_fse, (row, fse) in canonical.items():
                fse_id = as_int(fse["id"])
                provider_id = row["id"]
                env_id = _ensure_environment(cur, provider_id, model["apis_by_fse"].get(fse_id, []))
                used_op_codes = set()
                used_api_codes = set()
                for api in model["apis_by_fse"].get(fse_id, []):
                    api_code = (str(api.get("code_api") or f"API_{api['id']}").strip().upper() or f"API_{api['id']}")[:50]
                    if api_code in used_api_codes:
                        api_code = f"{api_code[:40]}_{api['id']}"[:50]
                    used_api_codes.add(api_code)
                    base_url = build_base_url(api.get("host"), api.get("port"), bool(api.get("use_ssl")), api.get("endpoint"))
                    auth = map_auth(api.get("type_autorization"))
                    token_url = api.get("authentification_url") or None
                    if auth != "OAUTH2_CLIENT_CREDENTIALS":
                        token_url = token_url if token_url else None
                    extra = {
                        "source": "omnicanal",
                        "omniApiId": api["id"],
                    }
                    if api.get("grant_type"):
                        extra["grantType"] = str(api["grant_type"])[:40]
                    fse_api_id = str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:fse-api:{code_fse}:{api['id']}"))
                    cur.execute(
                        """
                        INSERT INTO provider_fse_api (
                            id, provider_id, environment_id, code, nom, type_api, base_url,
                            host, port, use_ssl, use_proxy, actif, auth_type, client_id,
                            client_secret_enc, token_url, username_enc, password_enc,
                            extra_config_json, created_at
                        ) VALUES (
                            %s::uuid, %s::uuid, %s::uuid, %s, %s, %s, %s,
                            %s, %s, %s, %s, %s, %s, %s,
                            %s, %s, %s, %s,
                            %s, now()
                        )
                        """,
                        (
                            fse_api_id,
                            provider_id,
                            env_id,
                            api_code,
                            clip(api.get("nom_api") or api_code, 150),
                            clip((api.get("type") or "REST"), 20).upper(),
                            base_url,
                            clip(api.get("host"), 255),
                            clip(api.get("port"), 20),
                            bool(api.get("use_ssl")),
                            bool(api.get("use_proxy")),
                            bool(api.get("actif")) if api.get("actif") is not None else True,
                            auth,
                            clip(api.get("client_id"), 255) or None,
                            encrypt_secret(api.get("client_secret"), key),
                            clip(token_url, 500),
                            encrypt_secret(api.get("login"), key),
                            encrypt_secret(api.get("password"), key),
                            json.dumps(extra),
                        ),
                    )
                    stats["fse_api_inserted"] += 1
                    if is_bcrypt(api.get("password")) or is_bcrypt(api.get("client_secret")) or is_bcrypt(api.get("login")):
                        stats["secrets_bcrypt_ignores"] += 1

                    for methode in model["methodes_by_api"].get(as_int(api["id"]), []):
                        type_methode = (methode.get("type_methode") or "UNKNOWN").strip().upper() or "UNKNOWN"
                        op_code = operation_code(type_methode, methode.get("code_methode"), used_op_codes)
                        op_id = str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:operation:{code_fse}:{methode['id']}"))
                        api_row_id = str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:provider-api:{code_fse}:{methode['id']}"))
                        params = model["params_by_methode"].get(as_int(methode["id"]), [])
                        mapping = {}
                        for param in params:
                            code = param.get("code_parametre") or param.get("nom_externe") or param.get("nom_parametre")
                            if code:
                                mapping[str(code)[:100]] = f"${{{str(code)[:100]}}}"
                        cur.execute(
                            """
                            INSERT INTO provider_operation (
                                id, provider_id, code, nom, description, idempotent, actif, created_at
                            ) VALUES (%s::uuid, %s::uuid, %s, %s, %s, false, true, now())
                            """,
                            (
                                op_id,
                                provider_id,
                                op_code,
                                clip(methode.get("nom_methode") or type_methode, 150),
                                f"Omnicanal {code_fse} · {type_methode} ({methode.get('code_methode') or ''})",
                            ),
                        )
                        content_type = "application/json"
                        response_kind = (methode.get("type_reponse") or "").upper()
                        if "XML" in response_kind:
                            content_type = "application/xml"
                        cur.execute(
                            """
                            INSERT INTO provider_api (
                                id, provider_id, environment_id, operation_id, fse_api_id,
                                code, nom, http_method, path, content_type,
                                request_mapping_json, actif, created_at
                            ) VALUES (
                                %s::uuid, %s::uuid, %s::uuid, %s::uuid, %s::uuid,
                                %s, %s, %s, %s, %s,
                                %s, true, now()
                            )
                            """,
                            (
                                api_row_id,
                                provider_id,
                                env_id,
                                op_id,
                                fse_api_id,
                                op_code,
                                clip(methode.get("nom_methode") or op_code, 150),
                                normalize_http(methode.get("type_appel")),
                                normalize_path(methode.get("nom_methode")),
                                content_type,
                                json.dumps(mapping, ensure_ascii=False) if mapping else None,
                            ),
                        )
                        methode_to_op[as_int(methode["id"])] = op_id
                        methode_to_api[as_int(methode["id"])] = api_row_id
                        stats["methodes_inserted"] += 1
                        mapped = METHOD_SERVICE.get(type_methode)
                        if mapped in service_codes:
                            services_by_provider[provider_id].add(mapped)

            seen_interfaces = set()
            for iface in _all_interfaces(model):
                if not _is_request(iface):
                    stats["interfaces_skipped"] += 1
                    continue
                fse = model["fournisseurs"].get(as_int(iface.get("fournisseur_service_id")))
                if fse is None and iface.get("methode_id") is not None:
                    methode = model["methodes"].get(as_int(iface.get("methode_id")))
                    if methode is not None:
                        api = model["apis"].get(as_int(methode.get("api_id")))
                        if api is not None:
                            fse = model["fournisseurs"].get(as_int(api.get("fournisseur_service_id")))
                if fse is None:
                    stats["interfaces_sans_fse"] += 1
                    continue
                code_fse = str(fse["code_fse"]).strip()
                provider = canonical[code_fse][0]
                service = _interface_service(iface, model)
                type_methode = (iface.get("type_methode") or "").strip().upper()
                if not type_methode and iface.get("methode_id") is not None:
                    methode = model["methodes"].get(as_int(iface.get("methode_id")))
                    if methode is not None:
                        type_methode = (methode.get("type_methode") or "").strip().upper()
                if not type_methode:
                    type_methode = clip(iface.get("code_interface") or "NOP", 64)
                key = (code_fse, service, type_methode)
                if key in seen_interfaces:
                    suffix = "_" + str(iface.get("code_interface") or iface["id"])
                    type_methode = f"{type_methode[: max(1, 64 - len(suffix))]}{suffix}"[:64]
                    n = 2
                    while (code_fse, service, type_methode) in seen_interfaces:
                        tail = f"_{n}"
                        type_methode = f"{type_methode[:64 - len(tail)]}{tail}"
                        n += 1
                    stats["interfaces_dedoublonnees"] += 1
                seen_interfaces.add((code_fse, service, type_methode))
                iface_id = str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:interface:{iface['id']}"))
                methode_id = as_int(iface.get("methode_id"))
                cur.execute(
                    """
                    INSERT INTO interface_utilisateur (
                        id, provider_id, code_fse, service_type, type_methode, type_envoi,
                        provider_operation_id, provider_api_id, code_interface, nom_interface,
                        actif, created_at
                    ) VALUES (
                        %s::uuid, %s::uuid, %s, %s, %s, 'REQUEST',
                        %s::uuid, %s::uuid, %s, %s,
                        true, now()
                    )
                    """,
                    (
                        iface_id,
                        provider["id"],
                        code_fse,
                        service,
                        type_methode,
                        methode_to_op.get(methode_id),
                        methode_to_api.get(methode_id),
                        clip(iface.get("code_interface"), 50),
                        clip(iface.get("nom_interface"), 150),
                    ),
                )
                stats["interfaces_inserted"] += 1
                if service in service_codes:
                    services_by_provider[provider["id"]].add(service)
                seen_param = set()
                ordre = 0
                for param in model["params_by_interface"].get(as_int(iface["id"]), []):
                    code = clip(param.get("code_parametre") or param.get("nom_parametre"), 100)
                    if not code or code in seen_param:
                        stats["parametres_ignores"] += 1
                        continue
                    seen_param.add(code)
                    propriete = param.get("parametre_api")
                    if propriete is not None:
                        propriete = str(propriete).strip().upper()
                        if propriete not in ("FSESOURCE", "FSEDESTINATAIRE", "FSEPARTENAIRE"):
                            propriete = None
                    attribut = param.get("attribut")
                    extract = None
                    if isinstance(attribut, str) and len(attribut) > 500:
                        extract = attribut
                        attribut = attribut[:500]
                    cur.execute(
                        """
                        INSERT INTO parametre_utilisateur (
                            id, interface_utilisateur_id, nom_parametre, code_parametre,
                            code_parametre_methode, type_parametre, valeur, parametre_api,
                            attribut, extract_script, ordre, created_at
                        ) VALUES (
                            %s::uuid, %s::uuid, %s, %s,
                            %s, %s, %s, %s,
                            %s, %s, %s, now()
                        )
                        """,
                        (
                            str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:param-user:{param['id']}")),
                            iface_id,
                            clip(param.get("nom_parametre"), 150),
                            code,
                            clip(param.get("code_parametre_methode"), 100),
                            clip(param.get("type_parametre"), 50),
                            clip(param.get("valeur"), 500),
                            propriete,
                            attribut,
                            extract,
                            ordre,
                        ),
                    )
                    ordre += 1
                    stats["parametres_inserted"] += 1

            for provider_id, codes in services_by_provider.items():
                for code in codes:
                    cur.execute(
                        """
                        INSERT INTO provider_service_type (provider_id, service_type_code)
                        VALUES (%s::uuid, %s)
                        ON CONFLICT DO NOTHING
                        """,
                        (provider_id, code),
                    )
                    stats["service_links"] += cur.rowcount

            _replace_accounts(cur, model, stats)

            cur.execute("SELECT count(*) AS n FROM payment_flow")
            flows_after = cur.fetchone()["n"]
            cur.execute("SELECT count(*) AS n FROM schema_ecriture")
            schemas_after = cur.fetchone()["n"]
            cur.execute("SELECT count(*) AS n FROM payment_transaction")
            tx_after = cur.fetchone()["n"]
            if (flows_after, schemas_after, tx_after) != (flows_before, schemas_before, tx_before):
                raise SystemExit(
                    f"volumes produit modifiés: flows {flows_before}->{flows_after}, "
                    f"schemas {schemas_before}->{schemas_after}, tx {tx_before}->{tx_after}"
                )
            cur.execute(
                """
                SELECT count(*) AS n FROM payment_provider p
                WHERE p.code_fse = ANY(%s)
                """,
                (code_fses,),
            )
            linked = cur.fetchone()["n"]
            if linked != len(code_fses):
                raise SystemExit(f"fournisseurs liés {linked} / {len(code_fses)}")
            stats["flows"] = flows_after
            stats["schemas"] = schemas_after
            stats["transactions"] = tx_after
            stats["fournisseurs"] = linked
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()
    return stats


def _all_interfaces(model):
    # params_by_interface is only a side index; interfaces live on the parsed rows
    # rehydrated through methode/fournisseur lookups. The caller passes model built
    # by prepare(), which does not keep the raw interface list. Stored below.
    return model["interfaces"]


def _is_request(iface) -> bool:
    kind = (iface.get("type_interface") or "REQUEST").strip().upper()
    return kind == "REQUEST"


def _interface_service(iface, model) -> str:
    service = model["type_services"].get(as_int(iface.get("service_id")))
    if service is not None:
        return map_service(service.get("code_type_service"), service.get("type_methode") or iface.get("type_methode"))
    return map_service(None, iface.get("type_methode"))


def _ensure_environment(cur, provider_id, apis) -> str:
    cur.execute(
        """
        SELECT id::text FROM provider_environment
        WHERE provider_id = %s::uuid AND code = 'DEVELOPPEMENT'
        """,
        (provider_id,),
    )
    found = cur.fetchone()
    if found:
        return found["id"]
    base = "http://localhost"
    if apis:
        first = apis[0]
        base = build_base_url(first.get("host"), first.get("port"), bool(first.get("use_ssl")), first.get("endpoint"))
    env_id = str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:env:{provider_id}:DEVELOPPEMENT"))
    cur.execute(
        """
        INSERT INTO provider_environment (
            id, provider_id, code, base_url, timeout_seconds, actif, created_at, updated_at
        ) VALUES (%s::uuid, %s::uuid, 'DEVELOPPEMENT', %s, 30, true, now(), now())
        """,
        (env_id, provider_id, base),
    )
    return env_id


def _retire_duplicate(cur, other, stats, dry_run: bool):
    cur.execute("SELECT count(*) AS n FROM payment_flow WHERE provider_id = %s::uuid", (other["id"],))
    flows = cur.fetchone()["n"]
    cur.execute("SELECT count(*) AS n FROM biller WHERE provider_id = %s::uuid", (other["id"],))
    billers = cur.fetchone()["n"]
    cur.execute("SELECT count(*) AS n FROM biller_product WHERE provider_id = %s::uuid", (other["id"],))
    products = cur.fetchone()["n"]
    if dry_run:
        stats["duplicates"] += 1
        return
    if flows or billers or products:
        cur.execute(
            "UPDATE payment_provider SET code_fse = NULL, updated_at = now() WHERE id = %s::uuid",
            (other["id"],),
        )
        stats["duplicates_detached"] += 1
        return
    cur.execute("DELETE FROM payment_provider WHERE id = %s::uuid", (other["id"],))
    stats["duplicates_deleted"] += 1


def _replace_accounts(cur, model, stats):
    cur.execute("UPDATE compte_fse SET mirroir_id = NULL")
    cur.execute("DELETE FROM compte_fse")
    stats["comptes_deleted"] = cur.rowcount
    cur.execute("DELETE FROM relation_fse")
    stats["relations_deleted"] = cur.rowcount

    for type_row in model["type_comptes"].values():
        code = clip(type_row.get("code_parametre"), 64)
        if not code:
            continue
        cur.execute(
            """
            INSERT INTO type_compte (code_parametre, libelle)
            VALUES (%s, %s)
            ON CONFLICT (code_parametre) DO UPDATE SET libelle = EXCLUDED.libelle
            """,
            (code, clip(type_row.get("nom_type_compte") or code, 150)),
        )
        stats["type_comptes"] += 1

    rel_ids = {}
    seen_rel = {}
    for rel in model["relations"]:
        source = model["fournisseurs"].get(as_int(rel.get("fse_source_id")))
        dest = model["fournisseurs"].get(as_int(rel.get("fse_destination_id")))
        if source is None or dest is None:
            stats["relations_ignorees"] += 1
            continue
        service_row = model["type_services"].get(as_int(rel.get("service_id")))
        service = map_service(
            None if service_row is None else service_row.get("code_type_service"),
            None if service_row is None else service_row.get("type_methode"),
        )
        src = str(source["code_fse"]).strip()
        dst = str(dest["code_fse"]).strip()
        key = (src, dst, service)
        if key in seen_rel:
            rel_ids[as_int(rel["id"])] = seen_rel[key]
            stats["relations_fusionnees"] += 1
            continue
        rel_uuid = str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:relation:{src}:{dst}:{service}"))
        cur.execute(
            """
            INSERT INTO relation_fse (id, fse_source, fse_destination, service_type, created_at)
            VALUES (%s::uuid, %s, %s, %s, now())
            ON CONFLICT (fse_source, fse_destination, service_type) DO UPDATE
              SET fse_source = EXCLUDED.fse_source
            RETURNING id::text
            """,
            (rel_uuid, src, dst, service),
        )
        stored = cur.fetchone()["id"]
        seen_rel[key] = stored
        rel_ids[as_int(rel["id"])] = stored
        stats["relations_inserted"] += 1

    compte_ids = {}
    pending_mirror = []
    for compte in model["comptes"]:
        type_row = model["type_comptes"].get(as_int(compte.get("type_compte_id")))
        teneur = model["fournisseurs"].get(as_int(compte.get("fse_teneur_id")))
        titulaire = model["fournisseurs"].get(as_int(compte.get("fse_titulaire_id")))
        if type_row is None or teneur is None:
            stats["comptes_ignores"] += 1
            continue
        numero = clip(compte.get("numero_compte"), 20)
        if not numero:
            stats["comptes_ignores"] += 1
            continue
        if compte.get("numero_compte") and len(str(compte["numero_compte"])) > 20:
            stats["numeros_tronques"] += 1
        service_row = model["type_services"].get(as_int(compte.get("service_id")))
        service = None
        if service_row is not None:
            service = map_service(service_row.get("code_type_service"), service_row.get("type_methode"))
        agence = model["agences"].get(as_int(compte.get("agence_id")))
        agence_code = clip(
            (agence or {}).get("code_agence") or compte.get("code_agence") or "01001",
            20,
        ) or "01001"
        compte_uuid = str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:compte:{compte['id']}"))
        compte_ids[as_int(compte["id"])] = compte_uuid
        if compte.get("mirroir_id"):
            pending_mirror.append((compte_uuid, as_int(compte["mirroir_id"])))
        cur.execute(
            """
            INSERT INTO compte_fse (
                id, omni_id, code_banque, code_agence, numero_compte, cle,
                relation_id, extype, type_compte_code, devise,
                fse_teneur, fse_titulaire, service_type, libelle_compte,
                code_uo_titulaire, mirroir_id, agence_code, created_at
            ) VALUES (
                %s::uuid, %s, %s, %s, %s, %s,
                %s::uuid, %s, %s, %s,
                %s, %s, %s, %s,
                %s, NULL, %s, now()
            )
            """,
            (
                compte_uuid,
                as_int(compte["id"]),
                clip(compte.get("code_banque") or "00000", 20),
                clip(compte.get("code_agence") or "", 20),
                numero,
                clip(compte.get("cle"), 5),
                rel_ids.get(as_int(compte.get("relation_id"))),
                clip(compte.get("extype") or "CompteFse", 20),
                clip(type_row.get("code_parametre"), 64),
                clip(compte.get("devise"), 10),
                str(teneur["code_fse"]).strip(),
                str((titulaire or teneur)["code_fse"]).strip(),
                service,
                clip(compte.get("libelle_compte") or "", 200),
                clip(compte.get("code_uo_titulaire"), 20),
                agence_code,
            ),
        )
        stats["comptes_inserted"] += 1

    for compte_uuid, mirror_omni in pending_mirror:
        mirror_uuid = compte_ids.get(mirror_omni)
        if not mirror_uuid:
            stats["miroirs_manquants"] += 1
            continue
        cur.execute(
            "UPDATE compte_fse SET mirroir_id = %s::uuid WHERE id = %s::uuid",
            (mirror_uuid, compte_uuid),
        )
        stats["miroirs_lies"] += 1


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dump", default=str(DEFAULT_DUMP))
    parser.add_argument("--gateway-url", default=DEFAULT_GATEWAY)
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument(
        "--encryption-key",
        default=DEFAULT_KEY,
        help="Clé AES gateway.encryption.secret-key (16, 24 ou 32 octets)",
    )
    args = parser.parse_args()
    key = args.encryption_key.encode("utf-8")
    if len(key) not in (16, 24, 32):
        raise SystemExit("la clé de chiffrement doit faire 16, 24 ou 32 octets")

    dump_path = Path(args.dump)
    rows = load_dump(dump_path)
    model = prepare(rows)
    model["interfaces"] = rows["interface_utilisateur"]
    print(f"Dump lu: {dump_path.name}")
    print(
        "Fournisseurs {f}, API {a}, méthodes {m}, interfaces {i}, comptes {c}".format(
            f=len(rows["fournisseur_service"]),
            a=len(rows["api"]),
            m=len(rows["methode"]),
            i=len(rows["interface_utilisateur"]),
            c=len(rows["compte"]),
        )
    )
    stats = apply(args.gateway_url, model, key, dry_run=args.dry_run)
    printable = {name: value for name, value in sorted(stats.items())}
    print(json.dumps(printable, indent=2, ensure_ascii=False))
    if args.dry_run:
        print("Dry-run: aucune écriture.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
