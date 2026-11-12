#!/usr/bin/env python3
"""
Exporte la connectivité Omnicanal (FSE → Api → Méthodes → ParametreMethode)
depuis Postgres vers des fixtures JSON compatibles payment-2
({@code OmnicanalConnectivityImportRequest}).

Usage:
  python3 scripts/export_omnicanal_connectivity.py
  OMNI_DB_URL=postgresql://omnicanal:omnicanal@localhost:5433/omnicanal \\
    python3 scripts/export_omnicanal_connectivity.py --out payment-channel-api/src/main/resources/omnicanal-migration/exported
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
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

KNOWN_PROVIDER_HINTS = {
    "0004": "CORIS_BANK",
    "0011": "CORIS_MONEY",
}

# Heuristique statut Omnicanal → participantKind payment-2
KIND_BY_STATUT = {
    "BANQUE": "BANQUE",
    "NONBANQUE": "WALLET",
    "AUTRE": "FACTURIER",
}


def parse_db_url(url: str):
    u = urlparse(url)
    return dict(
        host=u.hostname or "localhost",
        port=u.port or 5432,
        dbname=(u.path or "/omnicanal").lstrip("/") or "omnicanal",
        user=u.username or "omnicanal",
        password=u.password or "omnicanal",
    )


def slugify(text: str, fallback: str) -> str:
    raw = (text or "").upper()
    raw = re.sub(r"[^A-Z0-9]+", "_", raw).strip("_")
    return (raw[:40] or fallback).rstrip("_")


def is_bcrypt(value: str | None) -> bool:
    return bool(value) and value.startswith("$2a$")


def map_auth(type_autorization: str | None) -> str:
    n = (type_autorization or "NONE").strip().upper()
    if n in ("", "NOAUTH", "NONE"):
        return "NONE"
    if n in ("OAUTH", "OAUTH2"):
        return "OAUTH2_CLIENT_CREDENTIALS"
    return n


def build_base_url(host: str | None, port: str | None, use_ssl: bool, endpoint: str | None) -> str:
    if endpoint and endpoint.startswith("http"):
        return endpoint.rstrip("/")
    h = (host or "localhost").strip()
    scheme = "https" if use_ssl else "http"
    if port and str(port).strip():
        return f"{scheme}://{h}:{str(port).strip()}"
    return f"{scheme}://{h}"


def secret_or_none(value: str | None) -> str | None:
    if not value or is_bcrypt(value):
        return None
    return value


def operation_code(type_methode: str, code_methode: str, seen: dict[str, int]) -> str:
    tm = (type_methode or "UNKNOWN").strip().upper() or "UNKNOWN"
    seen[tm] = seen.get(tm, 0) + 1
    if seen[tm] == 1:
        return tm
    cm = (code_methode or "").strip()
    return f"{tm}_{cm}" if cm else f"{tm}_{seen[tm]}"


def request_mapping(params: list[dict]) -> str:
    mapping = {}
    for p in params:
        code = p.get("code") or p.get("nomExterne")
        if not code:
            continue
        mapping[code] = f"${{{code}}}"
    return json.dumps(mapping, ensure_ascii=False)


def kind_hint(statut: str | None, nom: str | None) -> str:
    n = (nom or "").upper()
    if any(k in n for k in ("MONEY", "ORANGE", "MOOV", "MTN", "WAVE")):
        return "WALLET"
    if "BANK" in n or "BANQUE" in n:
        return "BANQUE"
    if any(k in n for k in ("MONEYGRAM", "RIA", "WESTERN")):
        return "REMITTANCE"
    return KIND_BY_STATUT.get((statut or "").upper(), "FACTURIER")


def export_fse(cur, code_fse: str | None = None) -> list[dict]:
    sql = """
        SELECT fs.id, fs.code_fse, fs.nom, fs.statut, fs.groupe, fs.code_externe, fs.typeetc
        FROM fournisseur_service fs
        WHERE EXISTS (
            SELECT 1 FROM api a
            JOIN methode m ON m.api_id = a.id
            WHERE a.fournisseur_service_id = fs.id
        )
    """
    params: list = []
    if code_fse:
        sql += " AND fs.code_fse = %s"
        params.append(code_fse)
    sql += " ORDER BY fs.code_fse"
    cur.execute(sql, params)
    fses = cur.fetchall()

    fixtures = []
    for fs in fses:
        fs_id = fs["id"]
        code_fse = fs["code_fse"]
        nom = fs["nom"]
        statut = fs["statut"]
        groupe = fs["groupe"]
        code_externe = fs["code_externe"]
        typeetc = fs["typeetc"]
        cur.execute(
            """
            SELECT * FROM api
            WHERE fournisseur_service_id = %s
            ORDER BY id
            """,
            (fs_id,),
        )
        apis = cur.fetchall()
        # Première Api qui a au moins une méthode
        api = None
        methods_rows = []
        for candidate in apis:
            cur.execute(
                """
                SELECT * FROM methode WHERE api_id = %s ORDER BY code_methode, id
                """,
                (candidate["id"],),
            )
            rows = cur.fetchall()
            if rows:
                api = candidate
                methods_rows = rows
                break
        if not api or not methods_rows:
            continue

        base_url = build_base_url(api["host"], api["port"], bool(api["use_ssl"]), api["endpoint"])
        token_url = api["authentification_url"] or (base_url + "/oauth/token")

        methods = []
        seen_types: dict[str, int] = {}
        for m in methods_rows:
            cur.execute(
                """
                SELECT code_parametre, nom_parametre, nom_externe, type_parametre, code_categorie
                FROM parametre_methode
                WHERE methode_id = %s
                ORDER BY id
                """,
                (m["id"],),
            )
            params_rows = cur.fetchall()
            parameters = []
            for p in params_rows:
                param_code = p["code_parametre"] or p["nom_externe"] or p["nom_parametre"]
                if not param_code:
                    continue
                parameters.append(
                    {
                        "code": param_code,
                        "nomExterne": p["nom_externe"] or param_code,
                        "type": (p["type_parametre"] or "STRING").upper(),
                        "categorie": (p["code_categorie"] or "USER").upper(),
                    }
                )

            http = (m["type_appel"] or "POST").upper()
            if http not in ("GET", "POST", "PUT", "PATCH", "DELETE"):
                http = "POST"
            path = (m["nom_methode"] or "/").lstrip("/")
            type_methode = (m["type_methode"] or "UNKNOWN").upper()
            methods.append(
                {
                    "typeMethode": type_methode,
                    "codeMethode": m["code_methode"],
                    "operationCode": operation_code(type_methode, m["code_methode"] or "", seen_types),
                    "nom": m["nom_methode"] or type_methode,
                    "httpMethod": http,
                    "path": path,
                    "typeReponse": m["type_reponse"] or "JSON",
                    "requestMappingJson": request_mapping(parameters),
                    "parameters": parameters,
                }
            )

        hint = KNOWN_PROVIDER_HINTS.get(code_fse) or f"FSE_{code_fse}"
        fixture = {
            "version": 1,
            "source": "omnicanal-db-export",
            "codeFse": code_fse,
            "providerCodeHint": hint,
            "providerLibelle": nom,
            "participantKindHint": kind_hint(statut, nom),
            "fseStatut": (statut or "AUTRE").upper(),
            "groupCode": groupe,
            "codeExterne": code_externe or code_fse,
            "typeEtc": typeetc,
            "api": {
                "nomApi": api["nom_api"] or f"API {code_fse}",
                "codeApi": api["code_api"] or f"API_{code_fse}",
                "type": (api["type"] or "REST").upper(),
                "host": api["host"],
                "port": str(api["port"]) if api["port"] is not None else None,
                "useSsl": bool(api["use_ssl"]),
                "useProxy": bool(api["use_proxy"]),
                "baseUrl": base_url,
                "authType": map_auth(api["type_autorization"]),
                "clientId": api["client_id"],
                "clientSecret": secret_or_none(api["client_secret"]),
                "tokenUrl": token_url if map_auth(api["type_autorization"]).startswith("OAUTH") else None,
                "grantType": api["grant_type"] or (
                    "client_credentials" if map_auth(api["type_autorization"]).startswith("OAUTH") else None
                ),
                "username": secret_or_none(api["login"]) if not is_bcrypt(api["login"]) else None,
                "password": secret_or_none(api["password"]),
            },
            "targetEnvironmentCode": "DEVELOPPEMENT",
            "methods": methods,
        }
        # Nettoyage nulls inutiles dans api
        fixture["api"] = {k: v for k, v in fixture["api"].items() if v is not None}
        fixtures.append(fixture)
    return fixtures


def main():
    parser = argparse.ArgumentParser(description="Export Omnicanal connectivity fixtures")
    parser.add_argument(
        "--db",
        default=os.environ.get(
            "OMNI_DB_URL", "postgresql://omnicanal:omnicanal@localhost:5433/omnicanal"
        ),
    )
    parser.add_argument(
        "--out",
        default=str(
            Path(__file__).resolve().parents[1]
            / "payment-channel-api/src/main/resources/omnicanal-migration/exported"
        ),
    )
    parser.add_argument("--code-fse", default=None, help="Exporter un seul code FSE")
    args = parser.parse_args()

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    conn = psycopg2.connect(**parse_db_url(args.db), cursor_factory=psycopg2.extras.RealDictCursor)
    try:
        with conn.cursor() as cur:
            fixtures = export_fse(cur, args.code_fse)
    finally:
        conn.close()

    manifest = []
    for fx in fixtures:
        code = fx["codeFse"]
        slug = slugify(fx.get("providerLibelle") or "", code)
        filename = f"fse-{code}-{slug}.json"
        path = out_dir / filename
        path.write_text(json.dumps(fx, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        manifest.append(
            {
                "file": filename,
                "codeFse": code,
                "providerCodeHint": fx["providerCodeHint"],
                "methods": len(fx["methods"]),
                "libelle": fx.get("providerLibelle"),
            }
        )
        print(f"wrote {path.name} ({len(fx['methods'])} methods)")

    (out_dir / "manifest.json").write_text(
        json.dumps(
            {
                "version": 1,
                "source": "omnicanal-db-export",
                "count": len(manifest),
                "fixtures": manifest,
            },
            ensure_ascii=False,
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )
    print(f"\n{len(manifest)} fixture(s) → {out_dir}")


if __name__ == "__main__":
    main()
