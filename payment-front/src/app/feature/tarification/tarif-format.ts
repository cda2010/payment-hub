import {
  Anomalie,
  ParametreFormule,
  STATUTS_GRILLE,
  StatutGrille,
  ValeurParametre
} from '../../core/service/tarif.service';

const NOMBRE = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 6 });

export function montant(v: number | null | undefined, devise?: string | null): string {
  if (v === null || v === undefined) {
    return '—';
  }
  return devise ? `${NOMBRE.format(v)} ${devise}` : NOMBRE.format(v);
}

export function statutLabel(s: StatutGrille): string {
  return STATUTS_GRILLE.find((x) => x.value === s)?.label ?? s;
}

export function statutSeverity(s: StatutGrille): 'success' | 'info' | 'warning' | 'danger' | 'secondary' {
  return STATUTS_GRILLE.find((x) => x.value === s)?.severity ?? 'secondary';
}

export function aujourdhui(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

// ── Nombres décimaux exacts ───────────────────────────────────────────────
// Les conversions % ↔ forme décimale se font sur le texte : 0.35 / 100 en flottant donne 0.0034999…

/** Texte décimal exact d'un nombre JSON, sans notation exponentielle. */
export function enTexte(v: number): string {
  const s = String(v);
  return /e/i.test(s) ? v.toFixed(12).replace(/\.?0+$/, '') : s;
}

/** Décale la virgule de n rangs vers la droite (n < 0 : vers la gauche). */
export function decaler(s: string, n: number): string {
  const negatif = s.startsWith('-');
  let [ent, dec = ''] = (negatif ? s.slice(1) : s).split('.');
  if (n > 0) {
    dec = dec.padEnd(n, '0');
    ent += dec.slice(0, n);
    dec = dec.slice(n);
  } else if (n < 0) {
    const k = -n;
    ent = ent.padStart(k + 1, '0');
    dec = ent.slice(ent.length - k) + dec;
    ent = ent.slice(0, ent.length - k);
  }
  ent = ent.replace(/^0+(?=\d)/, '');
  dec = dec.replace(/0+$/, '');
  return (negatif ? '-' : '') + ent + (dec ? '.' + dec : '');
}

/** Saisie utilisateur (virgule, espaces acceptés) → texte décimal canonique, ou null si ce n'est pas un nombre. */
export function normaliser(saisie: string | null | undefined): string | null {
  const s = (saisie ?? '').replace(/[\s\u00a0\u202f]/g, '').replace(',', '.');
  if (!/^-?(\d+\.?\d*|\.\d+)$/.test(s)) {
    return null;
  }
  return decaler(s.startsWith('.') ? '0' + s : s.startsWith('-.') ? '-0' + s.slice(1) : s, 0);
}

export function nbDecimales(canonique: string): number {
  const i = canonique.indexOf('.');
  return i < 0 ? 0 : canonique.length - i - 1;
}

/** Affichage dans un champ de saisie : virgule décimale. */
export function pourSaisie(v: number | null | undefined): string {
  return v === null || v === undefined ? '' : enTexte(v).replace('.', ',');
}

/** Montant optionnel saisi → nombre pour l'API ; undefined si invalide. */
export function montantSaisi(saisie: string | null | undefined): number | null | undefined {
  if (!saisie || !saisie.trim()) {
    return null;
  }
  const c = normaliser(saisie);
  return c === null ? undefined : Number(c);
}

// ── Paramètres de formule ─────────────────────────────────────────────────

export type SaisieParametres = Record<string, string | boolean>;

export function estTaux(p: ParametreFormule): boolean {
  return p.typeDonnee === 'DECIMAL' && p.unite === 'TAUX';
}

export function estMontant(p: ParametreFormule): boolean {
  return p.typeDonnee === 'DECIMAL' && p.unite === 'MONTANT';
}

/** Valeurs stockées (taux en forme décimale) → saisie (taux en %). Les absents prennent la valeur par défaut. */
export function saisieDepuisValeurs(
  schema: ParametreFormule[],
  valeurs: Record<string, ValeurParametre> | null | undefined
): SaisieParametres {
  const saisie: SaisieParametres = {};
  for (const p of schema) {
    const v = valeurs?.[p.code] ?? p.valeurDefaut;
    if (p.typeDonnee === 'BOOLEAN') {
      saisie[p.code] = v === true || v === 'true';
    } else if (v === undefined || v === null) {
      saisie[p.code] = '';
    } else if (p.typeDonnee === 'DECIMAL' || p.typeDonnee === 'INTEGER') {
      const texte = typeof v === 'number' ? enTexte(v) : normaliser(String(v)) ?? String(v);
      saisie[p.code] = (estTaux(p) ? decaler(texte, 2) : texte).replace('.', ',');
    } else {
      saisie[p.code] = String(v);
    }
  }
  return saisie;
}

export interface ConversionParametres {
  valeurs: Record<string, ValeurParametre>;
  /** code du paramètre → message */
  erreurs: Record<string, string>;
}

/**
 * Saisie → valeurs pour l'API, avec les contrôles locaux (type, bornes, précision) exprimés comme l'utilisateur
 * les saisit (taux en %). Le serveur reste juge : ses erreurs sont affichées en plus.
 */
export function valeursDepuisSaisie(schema: ParametreFormule[], saisie: SaisieParametres): ConversionParametres {
  const valeurs: Record<string, ValeurParametre> = {};
  const erreurs: Record<string, string> = {};
  for (const p of schema) {
    const brut = saisie[p.code];
    if (p.typeDonnee === 'BOOLEAN') {
      valeurs[p.code] = brut === true;
      continue;
    }
    const texte = typeof brut === 'string' ? brut.trim() : '';
    if (!texte) {
      if (p.obligatoire) {
        erreurs[p.code] = 'Obligatoire.';
      }
      continue;
    }
    if (p.typeDonnee === 'STRING' || p.typeDonnee === 'ENUM') {
      valeurs[p.code] = texte;
      continue;
    }
    const c = normaliser(texte);
    if (c === null) {
      erreurs[p.code] = p.typeDonnee === 'INTEGER' ? 'Nombre entier attendu.' : 'Nombre attendu.';
      continue;
    }
    if (p.typeDonnee === 'INTEGER' && c.includes('.')) {
      erreurs[p.code] = 'Nombre entier attendu.';
      continue;
    }
    const taux = estTaux(p);
    const stocke = taux ? decaler(c, -2) : c;
    const message = horsBornes(p, stocke) ?? tropDeDecimales(p, stocke);
    if (message) {
      erreurs[p.code] = message;
      continue;
    }
    valeurs[p.code] = Number(stocke);
  }
  return { valeurs, erreurs };
}

function horsBornes(p: ParametreFormule, stocke: string): string | null {
  const v = Number(stocke);
  const min = p.valeurMin;
  const max = p.valeurMax;
  if ((min === undefined || v >= min) && (max === undefined || v <= max)) {
    return null;
  }
  const fmt = (b: number) => (estTaux(p) ? `${montant(Number(decaler(enTexte(b), 2)))} %` : montant(b));
  if (min !== undefined && max !== undefined) {
    return `Doit être compris entre ${fmt(min)} et ${fmt(max)}.`;
  }
  return min !== undefined ? `Doit être supérieur ou égal à ${fmt(min)}.` : `Doit être inférieur ou égal à ${fmt(max!)}.`;
}

function tropDeDecimales(p: ParametreFormule, stocke: string): string | null {
  if (p.nbDecimales === undefined || p.nbDecimales === null || nbDecimales(stocke) <= p.nbDecimales) {
    return null;
  }
  const permis = estTaux(p) ? Math.max(p.nbDecimales - 2, 0) : p.nbDecimales;
  return `Au plus ${permis} décimale(s)${estTaux(p) ? ' en %' : ''}.`;
}

/**
 * Messages du serveur rangés par champ du formulaire de tranche : « montantMin », « parametres.taux »…
 * Pour un taux, les messages de bornes et de précision du serveur parlent en forme décimale : on les reformule.
 */
export function erreursParChamp(anomalies: Anomalie[], schema: ParametreFormule[] = []): Record<string, string> {
  const parChamp: Record<string, string> = {};
  for (const a of anomalies) {
    const champ = a.champ || '_';
    let message = a.message;
    if (champ.startsWith('parametres.')) {
      const p = schema.find((x) => `parametres.${x.code}` === champ);
      if (p && estTaux(p) && (a.code === 'PARAM_HORS_BORNES' || a.code === 'PARAM_PRECISION')) {
        message = `${p.libelle} : ${a.code === 'PARAM_PRECISION' ? 'trop de décimales' : 'hors des bornes autorisées'}.`;
      }
    }
    parChamp[champ] = parChamp[champ] ? `${parChamp[champ]} ${message}` : message;
  }
  return parChamp;
}

/** Résumé lisible des paramètres d'une tranche : « Taux 1,5 % · Frais fixes 100 XOF ». */
export function resumeParametres(
  schema: ParametreFormule[] | undefined,
  valeurs: Record<string, ValeurParametre>,
  devise: string
): string {
  if (!schema) {
    return Object.entries(valeurs)
      .map(([k, v]) => `${k} ${v}`)
      .join(' · ');
  }
  return [...schema]
    .sort((a, b) => a.ordre - b.ordre)
    .filter((p) => valeurs[p.code] !== undefined)
    .map((p) => `${p.libelle} ${valeurAffichee(p, valeurs[p.code], devise)}`)
    .join(' · ');
}

export function valeurAffichee(p: ParametreFormule, v: ValeurParametre, devise: string): string {
  if (typeof v === 'boolean') {
    return v ? 'oui' : 'non';
  }
  if (typeof v === 'number') {
    if (estTaux(p)) {
      return `${montant(Number(decaler(enTexte(v), 2)))} %`;
    }
    return estMontant(p) ? montant(v, devise) : montant(v);
  }
  return v;
}
