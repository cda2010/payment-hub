import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Moteur de tarification (payment-pricing) : grilles, tranches, formules, simulation et calcul.
 * Les montants et taux circulent en nombres JSON ; les taux sont en forme décimale (0.015 = 1,5 %).
 */

export type StatutGrille = 'BROUILLON' | 'A_VALIDER' | 'VALIDEE' | 'PUBLIEE' | 'DESACTIVEE';
export type TypeDonnee = 'DECIMAL' | 'INTEGER' | 'BOOLEAN' | 'STRING' | 'ENUM';
export type ModeArrondi = 'HALF_UP' | 'HALF_EVEN' | 'HALF_DOWN' | 'UP' | 'DOWN' | 'CEILING' | 'FLOOR';
export type ValeurParametre = number | string | boolean;

export interface FormuleResume {
  id: string;
  code: string;
  libelle: string;
  version: number;
  type: 'PREDEFINIE' | 'EXPRESSION';
  statut: 'ACTIVE' | 'INACTIVE';
  expression?: string;
  description?: string;
}

export interface ParametreFormule {
  code: string;
  libelle: string;
  typeDonnee: TypeDonnee;
  obligatoire: boolean;
  valeurDefaut?: string;
  valeurMin?: number;
  valeurMax?: number;
  nbDecimales?: number;
  /** TAUX (saisi en %), MONTANT (dans la devise de la grille) ou libre. */
  unite?: string;
  ordre: number;
  aide?: string;
  valeursAutorisees?: string[];
  longueurMax?: number;
  format?: string;
}

export interface FormuleDetail extends FormuleResume {
  parametres: ParametreFormule[];
}

export interface FormuleRef {
  code: string;
  version: number;
}

/** TOUTES : aucune restriction ; ZONE : toute la zone ; UNITE : l'unité et sa descendance (ou le périmètre d'un regroupement). */
export type NiveauCible = 'TOUTES' | 'ZONE' | 'UNITE';

export interface Cible {
  niveau: NiveauCible;
  zone?: string | null;
  uniteId?: string | null;
  uniteCode?: string | null;
  uniteLibelle?: string | null;
  uniteType?: string | null;
}

/** Absente ou zone vide : toutes. Une unité suffit, sa zone est déduite. */
export interface CibleRequest {
  zone?: string | null;
  uniteId?: string | null;
}

/** Position du client dans le réseau au moment de la transaction : le critère le plus précis renseigné l'emporte. */
export interface Localisation {
  uniteId?: string | null;
  pays?: string | null;
  zone?: string | null;
}

/** Comment la grille retenue couvre un côté de la transaction ; via : membre par lequel un regroupement couvre l'unité. */
export interface Correspondance {
  niveau: NiveauCible;
  cible?: string | null;
  via?: string | null;
}

export interface GrilleResume {
  id: string;
  code: string;
  libelle: string;
  produit: string;
  canal?: string;
  devise: string;
  source: Cible;
  destination: Cible;
  dateDebut: string;
  dateFin?: string;
  version: number;
  statut: StatutGrille;
  nbTranches: number;
  updatedAt?: string;
  updatedBy?: string;
}

export interface Tranche {
  id: string;
  ordre: number;
  montantMin: number;
  montantMax?: number;
  formule: FormuleRef;
  formuleLibelle?: string;
  parametres: Record<string, ValeurParametre>;
  fraisMin?: number;
  fraisMax?: number;
}

export type BaseCalculFrais =
  | 'TRANSACTION_AMOUNT'
  | 'CONVERTED_AMOUNT'
  | 'CUMUL_AMOUNT'
  | 'CUMUL_COUNT'
  | 'CUSTOM';

export type PeriodeCumul = 'DAY' | 'WEEK' | 'MONTH' | 'YEAR' | 'ROLLING_24H' | 'ROLLING_30D' | 'CUSTOM';

export type ScopeCumul = 'CUSTOMER' | 'WALLET' | 'ACCOUNT' | 'AGENT' | 'MERCHANT' | 'TILL' | 'UNITE';

export const BASES_CALCUL: { value: BaseCalculFrais; label: string }[] = [
  { value: 'TRANSACTION_AMOUNT', label: 'Montant de la transaction' },
  { value: 'CONVERTED_AMOUNT', label: 'Montant converti (FX)' },
  { value: 'CUMUL_AMOUNT', label: 'Cumul de montants' },
  { value: 'CUMUL_COUNT', label: 'Cumul du nombre d’opérations' },
  { value: 'CUSTOM', label: 'Personnalisée (feeBaseAmount)' }
];

export const PERIODES_CUMUL: { value: PeriodeCumul; label: string }[] = [
  { value: 'DAY', label: 'Journalier' },
  { value: 'WEEK', label: 'Hebdomadaire' },
  { value: 'MONTH', label: 'Mensuel' },
  { value: 'YEAR', label: 'Annuel' },
  { value: 'ROLLING_24H', label: '24 h glissantes' },
  { value: 'ROLLING_30D', label: '30 jours glissants' },
  { value: 'CUSTOM', label: 'Personnalisée' }
];

export const SCOPES_CUMUL: { value: ScopeCumul; label: string }[] = [
  { value: 'WALLET', label: 'Wallet' },
  { value: 'CUSTOMER', label: 'Client' },
  { value: 'ACCOUNT', label: 'Compte' },
  { value: 'AGENT', label: 'Agent' },
  { value: 'MERCHANT', label: 'Marchand' },
  { value: 'TILL', label: 'Till' },
  { value: 'UNITE', label: 'Unité réseau' }
];

export function estBaseCumul(base: BaseCalculFrais | null | undefined): boolean {
  return base === 'CUMUL_AMOUNT' || base === 'CUMUL_COUNT';
}

export interface GrilleDetail extends Omit<GrilleResume, 'nbTranches'> {
  modifiable: boolean;
  continuiteRequise: boolean;
  arrondiEchelle: number;
  arrondiMode: ModeArrondi;
  baseCalcul?: BaseCalculFrais;
  cumulPeriod?: PeriodeCumul | null;
  cumulScope?: ScopeCumul | null;
  tranches: Tranche[];
  createdAt?: string;
  createdBy?: string;
}

export interface GrilleRequest {
  code: string;
  libelle: string;
  produit: string;
  canal?: string | null;
  devise: string;
  dateDebut: string;
  dateFin?: string | null;
  continuiteRequise?: boolean;
  arrondiEchelle?: number;
  arrondiMode?: ModeArrondi;
  source?: CibleRequest | null;
  destination?: CibleRequest | null;
  baseCalcul?: BaseCalculFrais;
  cumulPeriod?: PeriodeCumul | null;
  cumulScope?: ScopeCumul | null;
}

export interface TrancheRequest {
  montantMin: number;
  montantMax?: number | null;
  formule: FormuleRef;
  parametres: Record<string, ValeurParametre>;
  fraisMin?: number | null;
  fraisMax?: number | null;
}

export interface Anomalie {
  code: string;
  message: string;
  tranche?: number | null;
  champ?: string | null;
}

export interface RapportValidation {
  valide: boolean;
  erreurs: Anomalie[];
}

export interface ResultatFrais {
  montant: number;
  devise: string;
  grilleId: string;
  grilleCode: string;
  versionGrille: number;
  tranche: { ordre: number; montantMin: number; montantMax?: number; fraisMin?: number; fraisMax?: number };
  formuleCode: string;
  versionFormule: number;
  parametres: Record<string, ValeurParametre>;
  baseTaxable: number;
  fraisFormule: number;
  ajustement?: 'PLANCHER' | 'PLAFOND';
  fraisAvantArrondi: number;
  arrondi: { echelle: number; mode: ModeArrondi };
  frais: number;
  details: string;
  dateCalcul: string;
  source?: Correspondance;
  destination?: Correspondance;
}

export interface EssaiFormule {
  valide: boolean;
  erreurs: Anomalie[];
  parametres?: Record<string, ValeurParametre>;
  baseTaxable?: number;
  fraisFormule?: number;
  ajustement?: 'PLANCHER' | 'PLAFOND';
  frais?: number;
  details?: string;
}

export interface FiltreGrilles {
  code?: string;
  libelle?: string;
  produit?: string;
  canal?: string;
  devise?: string;
  statut?: StatutGrille | null;
  date?: string;
  /** Code de zone ou code d'unité. */
  source?: string;
  destination?: string;
}

export interface CalculRequest {
  produitCode: string;
  canalCode?: string | null;
  devise: string;
  montant: number;
  dateCalcul?: string | null;
  source?: Localisation | null;
  destination?: Localisation | null;
}

export interface PageGrilles {
  contenu: GrilleResume[];
  total: number;
  page: number;
  taille: number;
}

/** Même contexte qu'un calcul, sans montant : quelle grille le moteur appliquerait-il aujourd'hui ? */
export interface ApplicableRequest {
  produitCode: string;
  canalCode?: string | null;
  devise: string;
  dateCalcul?: string | null;
  source?: Localisation | null;
  destination?: Localisation | null;
}

export interface GrilleRetenue {
  id: string;
  code: string;
  version: number;
  libelle: string;
  canal?: string | null;
  dateDebut: string;
  dateFin?: string | null;
  source: Correspondance;
  destination: Correspondance;
}

/**
 * TROUVEE : grille retenue et, dans autres, les grilles moins précises qu'elle masque ; AMBIGUE : grilles à égalité.
 * voisines* : grilles publiées qui visent une autre unité du même type sous le même parent (candidat au regroupement).
 */
export interface GrilleApplicable {
  statut: 'TROUVEE' | 'AUCUNE' | 'AMBIGUE';
  message?: string | null;
  grille?: GrilleRetenue | null;
  autres: GrilleRetenue[];
  voisinesSource: number;
  voisinesDestination: number;
}

export const STATUTS_GRILLE: { value: StatutGrille; label: string; severity: 'success' | 'info' | 'warning' | 'danger' | 'secondary' }[] = [
  { value: 'BROUILLON', label: 'Brouillon', severity: 'secondary' },
  { value: 'A_VALIDER', label: 'À valider', severity: 'warning' },
  { value: 'VALIDEE', label: 'Validée', severity: 'info' },
  { value: 'PUBLIEE', label: 'Publiée', severity: 'success' },
  { value: 'DESACTIVEE', label: 'Désactivée', severity: 'danger' }
];

export const MODES_ARRONDI: { value: ModeArrondi; label: string }[] = [
  { value: 'HALF_UP', label: 'Au plus proche (0,5 vers le haut)' },
  { value: 'HALF_EVEN', label: 'Au plus proche (bancaire)' },
  { value: 'HALF_DOWN', label: 'Au plus proche (0,5 vers le bas)' },
  { value: 'UP', label: 'Toujours vers le haut (en valeur absolue)' },
  { value: 'DOWN', label: 'Troncature' },
  { value: 'CEILING', label: 'Plafond (vers +∞)' },
  { value: 'FLOOR', label: 'Plancher (vers −∞)' }
];

@Injectable({ providedIn: 'root' })
export class TarifService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.pricingApiUrl;
  private readonly formulesCache = new Map<string, Observable<FormuleDetail>>();

  listerFormules(): Observable<FormuleResume[]> {
    return this.http.get<FormuleResume[]>(`${this.base}/formules`);
  }

  /** Le schéma d'une version de formule est immuable : on le garde en cache pour la session. */
  formule(code: string, version: number): Observable<FormuleDetail> {
    const cle = `${code}@${version}`;
    let obs = this.formulesCache.get(cle);
    if (!obs) {
      obs = this.http
        .get<FormuleDetail>(`${this.base}/formules/${encodeURIComponent(code)}/versions/${version}`)
        .pipe(shareReplay({ bufferSize: 1, refCount: false }));
      this.formulesCache.set(cle, obs);
      obs.subscribe({ error: () => this.formulesCache.delete(cle) });
    }
    return obs;
  }

  essayerFormule(
    ref: FormuleRef,
    body: { montant: number; parametres: Record<string, ValeurParametre>; fraisMin?: number | null; fraisMax?: number | null }
  ): Observable<EssaiFormule> {
    return this.http.post<EssaiFormule>(
      `${this.base}/formules/${encodeURIComponent(ref.code)}/versions/${ref.version}/essais`,
      body
    );
  }

  /** Triée par code puis version décroissante ; 50 par page par défaut, 200 au plus. */
  rechercherGrilles(filtre: FiltreGrilles, page = 0, taille = 50): Observable<PageGrilles> {
    let params = new HttpParams().set('page', page).set('taille', taille);
    for (const [k, v] of Object.entries(filtre)) {
      if (v !== null && v !== undefined && String(v).trim() !== '') {
        params = params.set(k, String(v).trim());
      }
    }
    return this.http.get<PageGrilles>(`${this.base}/grilles`, { params });
  }

  grilleApplicable(body: ApplicableRequest): Observable<GrilleApplicable> {
    return this.http.post<GrilleApplicable>(`${this.base}/frais/grille-applicable`, body);
  }

  grille(id: string): Observable<GrilleDetail> {
    return this.http.get<GrilleDetail>(`${this.base}/grilles/${id}`);
  }

  creerGrille(body: GrilleRequest): Observable<GrilleDetail> {
    return this.http.post<GrilleDetail>(`${this.base}/grilles`, body);
  }

  modifierGrille(id: string, body: GrilleRequest): Observable<GrilleDetail> {
    return this.http.put<GrilleDetail>(`${this.base}/grilles/${id}`, body);
  }

  ajouterTranche(grilleId: string, body: TrancheRequest): Observable<GrilleDetail> {
    return this.http.post<GrilleDetail>(`${this.base}/grilles/${grilleId}/tranches`, body);
  }

  modifierTranche(grilleId: string, trancheId: string, body: TrancheRequest): Observable<GrilleDetail> {
    return this.http.put<GrilleDetail>(`${this.base}/grilles/${grilleId}/tranches/${trancheId}`, body);
  }

  supprimerTranche(grilleId: string, trancheId: string): Observable<GrilleDetail> {
    return this.http.delete<GrilleDetail>(`${this.base}/grilles/${grilleId}/tranches/${trancheId}`);
  }

  dupliquerTranche(grilleId: string, trancheId: string): Observable<GrilleDetail> {
    return this.http.post<GrilleDetail>(`${this.base}/grilles/${grilleId}/tranches/${trancheId}/dupliquer`, {});
  }

  /** Copie une grille figée en brouillon v+1, ou renvoie le brouillon déjà ouvert pour ce code. */
  nouvelleVersion(grilleId: string): Observable<GrilleDetail> {
    return this.http.post<GrilleDetail>(`${this.base}/grilles/${grilleId}/versions`, {});
  }

  publier(grilleId: string): Observable<GrilleDetail> {
    return this.http.post<GrilleDetail>(`${this.base}/grilles/${grilleId}/publier`, {});
  }

  controler(grilleId: string): Observable<RapportValidation> {
    return this.http.post<RapportValidation>(`${this.base}/grilles/${grilleId}/valider`, {});
  }

  simuler(grilleId: string, montant: number, dateCalcul?: string | null): Observable<ResultatFrais> {
    return this.http.post<ResultatFrais>(`${this.base}/grilles/${grilleId}/simulations`, {
      montant,
      dateCalcul: dateCalcul || null
    });
  }

  calculer(body: CalculRequest): Observable<ResultatFrais> {
    return this.http.post<ResultatFrais>(`${this.base}/frais/calculer`, body);
  }
}
