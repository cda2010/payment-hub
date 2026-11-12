#!/usr/bin/env python3
"""
Jeu de volume pour payment-network : génère un réseau d'environ 6 000 unités sous FSE-UEMOA (fichiers CSV au
format de l'import en masse) et, avec --importer, le charge par l'API d'import (contrôle puis application).

Par agence : 1 regroupement, 5 masters de 8 sous-distributeurs chacun et 5 marchands, soit 52 unités ; les 3
premiers masters de chaque agence sont membres de son regroupement. 120 agences (défaut) donnent 6 240 unités et
360 membres. Tous les codes contiennent le préfixe (défaut VOL) : AG-VOL001, MS-VOL001-1, SD-VOL001-1-1, MC-VOL001-1,
RG-VOL001.

Usage :
  python3 scripts/reseau_volume.py                       # écrit scripts/load/out/reseau-unites.csv et -membres.csv
  python3 scripts/reseau_volume.py --importer            # génère puis importe (Keycloak local, admin)
  python3 scripts/reseau_volume.py --agences 20 --prefixe TST --importer

Variables : NETWORK_URL (http://localhost:8096/api/v1/network), KC_TOKEN_URL, KC_CLIENT, KC_USER, KC_PASSWORD.

Les unités ne se suppriment pas par l'API (on les désactive) ; pour retirer le jeu de la base locale
(psql -h localhost -p 5438 payment_network) :
  delete from regroupement_membre where regroupement_id in (select id from unite_organisation where code like '%-VOL%')
                                     or unite_id in (select id from unite_organisation where code like '%-VOL%');
  delete from unite_organisation where code like '%-VOL%';
"""

from __future__ import annotations

import argparse
import json
import os
import random
import sys
import time
import urllib.parse
import urllib.request
import uuid
from pathlib import Path

NETWORK_URL = os.environ.get("NETWORK_URL", "http://localhost:8096/api/v1/network")
KC_TOKEN_URL = os.environ.get("KC_TOKEN_URL", "http://localhost:8180/realms/payment/protocol/openid-connect/token")
KC_CLIENT = os.environ.get("KC_CLIENT", "payment-dev-tools")
KC_USER = os.environ.get("KC_USER", "admin")
KC_PASSWORD = os.environ.get("KC_PASSWORD", "Admin@123")


def generer(agences: int, prefixe: str) -> tuple[str, str]:
    lignes: list[str] = []
    membres = ["code_regroupement;code_membre"]
    for a in range(1, agences + 1):
        ag = f"AG-{prefixe}{a:03d}"
        rg = f"RG-{prefixe}{a:03d}"
        lignes.append(f"{ag};Agence {prefixe} {a};AGENCE;FSE-UEMOA;")
        lignes.append(f"{rg};Réseau {prefixe} {a};REGROUPEMENT;{ag};")
        for m in range(1, 6):
            ms = f"MS-{prefixe}{a:03d}-{m}"
            lignes.append(f"{ms};Master {a}-{m};MASTER;{ag};")
            lignes.append(f"MC-{prefixe}{a:03d}-{m};Marchand {a}-{m};MARCHAND;{ag};")
            for s in range(1, 9):
                lignes.append(f"SD-{prefixe}{a:03d}-{m}-{s};Boutique {a}-{m}-{s};SOUS_DISTRIBUTEUR;{ms};")
            if m <= 3:
                membres.append(f"{rg};{ms}")
    # Ordre quelconque : l'import résout les parents présents dans le fichier.
    random.Random(42).shuffle(lignes)
    unites = "\r\n".join(["code;libelle;type;code_parent;zone", *lignes]) + "\r\n"
    return unites, "\r\n".join(membres) + "\r\n"


def jeton() -> str:
    corps = urllib.parse.urlencode({
        "client_id": KC_CLIENT, "grant_type": "password", "scope": "openid",
        "username": KC_USER, "password": KC_PASSWORD,
    }).encode()
    with urllib.request.urlopen(urllib.request.Request(KC_TOKEN_URL, data=corps)) as r:
        return json.load(r)["access_token"]


def importer(objet: str, nom: str, contenu: bytes, essai: bool, token: str) -> dict:
    limite = uuid.uuid4().hex
    corps = (
        f"--{limite}\r\nContent-Disposition: form-data; name=\"fichier\"; filename=\"{nom}\"\r\n"
        f"Content-Type: text/csv\r\n\r\n"
    ).encode() + contenu + f"\r\n--{limite}--\r\n".encode()
    req = urllib.request.Request(
        f"{NETWORK_URL}/imports/{objet}?essai={'true' if essai else 'false'}",
        data=corps,
        headers={"Authorization": f"Bearer {token}", "Content-Type": f"multipart/form-data; boundary={limite}"},
    )
    debut = time.monotonic()
    try:
        with urllib.request.urlopen(req) as r:
            rapport = json.load(r)
    except urllib.error.HTTPError as e:
        sys.exit(f"{objet} : HTTP {e.code} {e.read().decode(errors='replace')}")
    rapport["_http_ms"] = round((time.monotonic() - debut) * 1000)
    return rapport


def resume(r: dict) -> str:
    return (f"{r['nbLignes']} lignes, {r['nbCreations']} créations, {r['nbMisesAJour']} renommages, "
            f"{r['nbDeplacements']} déplacements, {r['nbInchangees']} inchangées, {r['nbErreurs']} erreurs "
            f"— serveur {r['dureeMs']} ms, aller-retour {r['_http_ms']} ms")


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--agences", type=int, default=120)
    p.add_argument("--prefixe", default="VOL")
    p.add_argument("--sortie", default=str(Path(__file__).parent / "load" / "out"))
    p.add_argument("--importer", action="store_true", help="contrôle puis applique l'import via l'API")
    a = p.parse_args()

    unites, membres = generer(a.agences, a.prefixe.upper())
    sortie = Path(a.sortie)
    sortie.mkdir(parents=True, exist_ok=True)
    (sortie / "reseau-unites.csv").write_text(unites, encoding="utf-8-sig")
    (sortie / "reseau-membres.csv").write_text(membres, encoding="utf-8-sig")
    print(f"{unites.count(chr(10)) - 1} unités et {membres.count(chr(10)) - 1} membres écrits dans {sortie}")
    if not a.importer:
        return

    token = jeton()
    for objet, nom, contenu in (("unites", "reseau-unites.csv", unites), ("membres", "reseau-membres.csv", membres)):
        donnees = contenu.encode("utf-8")
        essai = importer(objet, nom, donnees, True, token)
        print(f"{objet} — contrôle : {resume(essai)}")
        if essai["nbErreurs"]:
            for e in essai["erreurs"][:20]:
                print(f"  ligne {e['ligne']} {e.get('code') or ''} {e['codeErreur']} : {e['message']}")
            sys.exit(1)
        print(f"{objet} — import : {resume(importer(objet, nom, donnees, False, token))}")


if __name__ == "__main__":
    main()
