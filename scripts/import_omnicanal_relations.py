#!/usr/bin/env python3
"""
Importe les relations FSE et TOUS les comptes catalogue depuis Omnicanal Postgres
vers payment-2 (relation_fse / compte_fse / type_compte).

Les comptes sans relation Omnicanal sont importés en comptes autonomes
(relation_id null).

Usage:
  python3 scripts/import_omnicanal_relations.py
  OMNI_DB_URL=postgresql://omnicanal:omnicanal@localhost:5433/omnicanal \\
  GATEWAY_DB_URL=postgresql://gateway:gateway@localhost:5432/payment_gateway \\
    python3 scripts/import_omnicanal_relations.py --dry-run
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import uuid
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


# Omnicanal type_service → payment-2 ServiceTypeCode
SERVICE_MAP = {
    "TRANSFERT": "TRANSFERT",
    "16": "PAIEMENT_FACTURE",
    "10": "TRANSFERT",
    "11": "TRANSFERT",
    "12": "TRANSFERT",
    "13": "TRANSFERT",
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
}


def parse_db_url(url: str) -> dict:
    u = urlparse(url)
    return dict(
        host=u.hostname or "localhost",
        port=u.port or 5432,
        dbname=(u.path or "/").lstrip("/") or "postgres",
        user=u.username or "postgres",
        password=u.password or "",
    )


def connect(url: str):
    return psycopg2.connect(**parse_db_url(url))


def map_service(code_type_service: str | None, type_methode: str | None) -> str:
    if type_methode and type_methode.strip().upper() in SERVICE_MAP:
        return SERVICE_MAP[type_methode.strip().upper()]
    if code_type_service and code_type_service.strip() in SERVICE_MAP:
        return SERVICE_MAP[code_type_service.strip()]
    if code_type_service and code_type_service.strip().upper() in SERVICE_MAP:
        return SERVICE_MAP[code_type_service.strip().upper()]
    return "TRANSFERT"


def stable_uuid(namespace: str, *parts: object) -> str:
    key = "|".join(str(p) for p in parts)
    return str(uuid.uuid5(uuid.NAMESPACE_URL, f"payment-2:{namespace}:{key}"))


def fetch_omnicanal(omni_url: str):
    with connect(omni_url) as conn, conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
        cur.execute(
            """
            select r.id as omni_id,
                   fs.code_fse as fse_source,
                   fd.code_fse as fse_destination,
                   ts.code_type_service,
                   ts.type_methode,
                   ts.libelle_type_service
            from relation r
            join fournisseur_service fs on fs.id = r.fse_source_id
            join fournisseur_service fd on fd.id = r.fse_destination_id
            left join type_service ts on ts.id = r.service_id
            order by fs.code_fse, fd.code_fse, r.id
            """
        )
        relations = list(cur.fetchall())

        cur.execute(
            """
            select c.id as omni_id,
                   c.code_banque,
                   c.code_agence,
                   c.numero_compte,
                   c.cle,
                   c.relation_id as omni_relation_id,
                   c.extype,
                   tc.code_parametre as type_compte,
                   coalesce(tc.nom_type_compte, tc.code_parametre) as type_libelle,
                   c.devise,
                   ft.code_fse as fse_teneur,
                   fti.code_fse as fse_titulaire,
                   ts.code_type_service,
                   ts.type_methode,
                   c.libelle_compte,
                   c.code_uo_titulaire,
                   c.mirroir_id as omni_mirroir_id,
                   a.code_agence as agence_code
            from compte c
            join type_compte tc on tc.id = c.type_compte_id
            join fournisseur_service ft on ft.id = c.fse_teneur_id
            join fournisseur_service fti on fti.id = c.fse_titulaire_id
            left join type_service ts on ts.id = c.service_id
            join agence a on a.id = c.agence_id
            order by c.id
            """
        )
        comptes = list(cur.fetchall())
    return relations, comptes


def build_payload(relations, comptes):
    rel_rows = []
    omni_rel_to_uuid = {}
    seen_keys = set()

    for r in relations:
        src = (r["fse_source"] or "").strip()
        dest = (r["fse_destination"] or "").strip()
        if not src or not dest:
            continue
        service = map_service(r.get("code_type_service"), r.get("type_methode"))
        key = (src, dest, service)
        if key in seen_keys:
            for existing in rel_rows:
                if (
                    existing["fse_source"] == src
                    and existing["fse_destination"] == dest
                    and existing["service_type"] == service
                ):
                    omni_rel_to_uuid[r["omni_id"]] = existing["id"]
                    break
            continue
        seen_keys.add(key)
        rid = stable_uuid("relation_fse", src, dest, service)
        omni_rel_to_uuid[r["omni_id"]] = rid
        rel_rows.append(
            {
                "id": rid,
                "fse_source": src,
                "fse_destination": dest,
                "service_type": service,
                "omni_id": r["omni_id"],
            }
        )

    type_comptes = {}
    compte_rows = []
    omni_compte_to_uuid = {}
    for c in comptes:
        code = (c["type_compte"] or "").strip()
        if not code:
            continue
        type_comptes[code] = (c.get("type_libelle") or code).strip()
        teneur = (c["fse_teneur"] or "").strip()
        titulaire = (c["fse_titulaire"] or teneur).strip()
        numero = (c["numero_compte"] or "").strip()
        if not teneur or not numero:
            continue

        rel_uuid = None
        omni_rel = c.get("omni_relation_id")
        if omni_rel is not None:
            rel_uuid = omni_rel_to_uuid.get(omni_rel)

        service = map_service(c.get("code_type_service"), c.get("type_methode"))
        if rel_uuid:
            for rel in rel_rows:
                if rel["id"] == rel_uuid:
                    service = rel["service_type"]
                    break

        omni_id = c["omni_id"]
        cid = stable_uuid("compte_fse", "omni", omni_id)
        omni_compte_to_uuid[omni_id] = cid
        compte_rows.append(
            {
                "id": cid,
                "omni_id": omni_id,
                "code_banque": (c.get("code_banque") or "00000").strip() or "00000",
                "code_agence": (c.get("code_agence") or "").strip(),
                "numero_compte": numero,
                "cle": (c.get("cle") or "").strip() or None,
                "extype": (c.get("extype") or "CompteFse").strip() or "CompteFse",
                "type_compte_code": code,
                "devise": (c.get("devise") or "").strip() or None,
                "fse_teneur": teneur,
                "fse_titulaire": titulaire or teneur,
                "service_type": service,
                "libelle_compte": c.get("libelle_compte") if c.get("libelle_compte") is not None else "",
                "code_uo_titulaire": (c.get("code_uo_titulaire") or "").strip() or None,
                "omni_mirroir_id": c.get("omni_mirroir_id"),
                "agence_code": (c.get("agence_code") or c.get("code_agence") or "01001").strip() or "01001",
                "relation_id": rel_uuid,
            }
        )

    for row in compte_rows:
        mid = row.get("omni_mirroir_id")
        row["mirroir_id"] = omni_compte_to_uuid.get(mid) if mid else None

    return {
        "version": 3,
        "source": "omnicanal-db-export",
        "relations": rel_rows,
        "type_comptes": [{"code_parametre": k, "libelle": v} for k, v in sorted(type_comptes.items())],
        "comptes": compte_rows,
    }


def apply_to_gateway(gateway_url: str, payload: dict, dry_run: bool) -> dict:
    stats = {
        "type_comptes_upserted": 0,
        "relations_upserted": 0,
        "comptes_deleted": 0,
        "comptes_upserted": 0,
        "comptes_standalone": 0,
        "mirroirs_linked": 0,
        "dry_run": dry_run,
    }
    if dry_run:
        stats["type_comptes_upserted"] = len(payload["type_comptes"])
        stats["relations_upserted"] = len(payload["relations"])
        stats["comptes_upserted"] = len(payload["comptes"])
        stats["comptes_standalone"] = sum(1 for c in payload["comptes"] if not c.get("relation_id"))
        stats["mirroirs_linked"] = sum(1 for c in payload["comptes"] if c.get("mirroir_id"))
        return stats

    with connect(gateway_url) as conn:
        with conn.cursor() as cur:
            for tc in payload["type_comptes"]:
                cur.execute(
                    """
                    insert into type_compte (code_parametre, libelle)
                    values (%s, %s)
                    on conflict (code_parametre) do update
                      set libelle = excluded.libelle
                    """,
                    (tc["code_parametre"], tc["libelle"]),
                )
                stats["type_comptes_upserted"] += 1

            planned_to_real: dict[str, str] = {}
            for r in payload["relations"]:
                cur.execute(
                    """
                    insert into relation_fse (id, fse_source, fse_destination, service_type, created_at)
                    values (%s::uuid, %s, %s, %s, now())
                    on conflict (fse_source, fse_destination, service_type) do update
                      set fse_source = excluded.fse_source
                    returning id::text
                    """,
                    (r["id"], r["fse_source"], r["fse_destination"], r["service_type"]),
                )
                rid = cur.fetchone()[0]
                planned_to_real[r["id"]] = rid
                r["id"] = rid
                stats["relations_upserted"] += 1

            # Catalogue = source of truth Omnicanal
            cur.execute("update compte_fse set mirroir_id = null")
            cur.execute("delete from compte_fse")
            stats["comptes_deleted"] = cur.rowcount

            for c in payload["comptes"]:
                planned_rel = c.get("relation_id")
                rel_id = planned_to_real.get(planned_rel) if planned_rel else None
                service = c.get("service_type") or "TRANSFERT"
                if rel_id:
                    for r in payload["relations"]:
                        if r["id"] == rel_id:
                            service = r["service_type"]
                            break

                cur.execute(
                    """
                    insert into compte_fse (
                        id, omni_id, code_banque, code_agence, numero_compte, cle,
                        relation_id, extype, type_compte_code, devise,
                        fse_teneur, fse_titulaire, service_type, libelle_compte,
                        code_uo_titulaire, mirroir_id, agence_code, created_at
                    ) values (
                        %s::uuid, %s, %s, %s, %s, %s,
                        %s::uuid, %s, %s, %s,
                        %s, %s, %s, %s,
                        %s, null, %s, now()
                    )
                    """,
                    (
                        c["id"],
                        c["omni_id"],
                        c["code_banque"],
                        c["code_agence"],
                        c["numero_compte"],
                        c.get("cle"),
                        rel_id,
                        c["extype"],
                        c["type_compte_code"],
                        c.get("devise"),
                        c["fse_teneur"],
                        c["fse_titulaire"],
                        service,
                        c.get("libelle_compte") or "",
                        c.get("code_uo_titulaire"),
                        c["agence_code"],
                    ),
                )
                stats["comptes_upserted"] += 1
                if not rel_id:
                    stats["comptes_standalone"] += 1

            for c in payload["comptes"]:
                mid = c.get("mirroir_id")
                if not mid:
                    continue
                cur.execute(
                    "update compte_fse set mirroir_id = %s::uuid where id = %s::uuid",
                    (mid, c["id"]),
                )
                stats["mirroirs_linked"] += cur.rowcount
        conn.commit()
    return stats


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--omni-url",
        default=os.environ.get(
            "OMNI_DB_URL", "postgresql://omnicanal:omnicanal@localhost:5433/omnicanal"
        ),
    )
    parser.add_argument(
        "--gateway-url",
        default=os.environ.get(
            "GATEWAY_DB_URL", "postgresql://gateway:gateway@localhost:5432/payment_gateway"
        ),
    )
    parser.add_argument(
        "--out",
        default=str(
            Path(__file__).resolve().parents[1]
            / "payment-channel-api/src/main/resources/omnicanal-migration/relations.json"
        ),
    )
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--skip-write-db", action="store_true", help="JSON only")
    args = parser.parse_args()

    print(f"Omnicanal: {args.omni_url}")
    print(f"Gateway:   {args.gateway_url}")
    relations, comptes = fetch_omnicanal(args.omni_url)
    print(f"Fetched: {len(relations)} relations, {len(comptes)} comptes (tous)")
    payload = build_payload(relations, comptes)
    standalone = sum(1 for c in payload["comptes"] if not c.get("relation_id"))
    print(
        f"Payload: {len(payload['relations'])} relations, "
        f"{len(payload['type_comptes'])} types, {len(payload['comptes'])} comptes "
        f"({standalone} autonomes)"
    )

    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Wrote {out}")

    if args.skip_write_db:
        return 0

    stats = apply_to_gateway(args.gateway_url, payload, dry_run=args.dry_run)
    print(json.dumps(stats, indent=2))

    if not args.dry_run:
        with connect(args.gateway_url) as conn, conn.cursor() as cur:
            cur.execute("select count(*) from relation_fse")
            n_rel = cur.fetchone()[0]
            cur.execute("select count(*) from compte_fse")
            n_cpt = cur.fetchone()[0]
            print(f"DB now: relation_fse={n_rel}, compte_fse={n_cpt}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
