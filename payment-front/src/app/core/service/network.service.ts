import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Réseau de distribution (payment-network) : arbre Pays > FSE > Agence > Master ou Marchand > Sous-distributeur par
 * zone. Un regroupement se rattache à une unité de n'importe quel niveau et couvre ses membres, pris dans la branche
 * de cette unité, avec toute leur descendance. Les zones sont un
 * référentiel administrable ; un pays se crée à la racine d'une zone active.
 */

/** Code de zone (UEMOA, CEMAC… ou zone ajoutée par l'administration). */
export type Zone = string;
export type TypeUnite = 'PAYS' | 'FSE' | 'AGENCE' | 'MASTER' | 'MARCHAND' | 'SOUS_DISTRIBUTEUR' | 'REGROUPEMENT';
export type StatutUnite = 'ACTIF' | 'INACTIF';

export interface UniteResume {
  id: string;
  code: string;
  libelle: string;
  type: TypeUnite;
  zone: Zone;
  /** Code FSE Omnicanal si type = FSE. */
  codeFse?: string | null;
  statut: StatutUnite;
  niveau: number;
  parentId?: string | null;
}

export interface UniteDetail {
  id: string;
  code: string;
  libelle: string;
  type: TypeUnite;
  zone: Zone;
  /** Code FSE Omnicanal si type = FSE. */
  codeFse?: string | null;
  devise: string;
  statut: StatutUnite;
  niveau: number;
  parent?: UniteResume | null;
  /** Du pays au parent direct. */
  ascendants: UniteResume[];
  nbEnfants: number;
  /** Regroupement : membres directs (leur descendance est couverte aussi). */
  nbMembres: number;
  typesEnfantsAutorises: TypeUnite[];
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}

/** Nœud de l'arbre chargé à la demande : ses enfants s'obtiennent par {@link NetworkService.enfants}. */
export interface NoeudArbre {
  id: string;
  code: string;
  libelle: string;
  type: TypeUnite;
  zone: Zone;
  statut: StatutUnite;
  niveau: number;
  parentId?: string | null;
  nbEnfants: number;
  /** Regroupement : nombre de membres directs ; 0 pour les autres types. */
  nbMembres: number;
}

/** Page de résultats renvoyée par le serveur (page numérotée à partir de 0, 500 éléments au plus). */
export interface Page<T> {
  contenu: T[];
  total: number;
  page: number;
  taille: number;
}

export type ObjetImport = 'unites' | 'membres';
export type FormatModele = 'csv' | 'xlsx';

export interface ErreurImport {
  /** Numéro de ligne dans le fichier, en-tête compris ; 0 pour une erreur globale. */
  ligne: number;
  code?: string | null;
  champ?: string | null;
  codeErreur: string;
  message: string;
}

export interface RapportImport {
  objet: string;
  format: string;
  essai: boolean;
  applique: boolean;
  nbLignes: number;
  nbCreations: number;
  nbMisesAJour: number;
  nbDeplacements: number;
  nbInchangees: number;
  nbErreurs: number;
  /** Les 500 premières erreurs. */
  erreurs: ErreurImport[];
  dureeMs: number;
}

export interface CreationUniteRequest {
  code: string;
  libelle: string;
  type: TypeUnite;
  zone?: Zone | null;
  parentId?: string | null;
  /** Obligatoire si type = FSE (code Omnicanal, ex. 0011). */
  codeFse?: string | null;
}

export interface ModificationUniteRequest {
  libelle: string;
  type: TypeUnite;
}

export interface Membre {
  unite: UniteResume;
  ajouteLe?: string;
  ajoutePar?: string;
}

export interface Couverture {
  regroupement: UniteResume;
  /** Membre par lequel l'unité est couverte : elle-même ou un de ses ascendants. */
  via: UniteResume;
  direct: boolean;
}

export interface ZoneRef {
  code: Zone;
  libelle: string;
  /** ISO 4217 ; figée dès que la zone contient une unité. */
  devise: string;
  statut: StatutUnite;
  nbPays: number;
  nbUnites: number;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface CreationZoneRequest {
  code: string;
  libelle: string;
  devise: string;
}

export interface ModificationZoneRequest {
  libelle: string;
  devise: string;
}

export interface TypeRef {
  code: TypeUnite;
  libelle: string;
  racine: boolean;
  regroupement: boolean;
  parentsAutorises: TypeUnite[];
  enfantsAutorises: TypeUnite[];
  regle: string;
}

export interface Referentiel {
  zones: ZoneRef[];
  types: TypeRef[];
}

export interface FiltreUnites {
  zone?: Zone | null;
  /** Un ou plusieurs types (OU). */
  type?: TypeUnite | TypeUnite[] | null;
  statut?: StatutUnite | null;
  parentId?: string | null;
  /** Unité et toute sa descendance. */
  branche?: string | null;
  sansRegroupements?: boolean | null;
  /** Code ou libellé, sans casse. */
  texte?: string | null;
}

export interface PageRequete {
  page?: number;
  taille?: number;
  tri?: 'code' | 'libelle' | 'type' | 'zone' | 'statut' | 'niveau' | 'maj' | null;
  sens?: 'asc' | 'desc' | null;
}

@Injectable({ providedIn: 'root' })
export class NetworkService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.networkApiUrl;
  private referentiel$?: Observable<Referentiel>;

  /** Chargé une fois, puis rechargé après toute modification des zones. */
  referentiel(): Observable<Referentiel> {
    if (!this.referentiel$) {
      this.referentiel$ = this.http
        .get<Referentiel>(`${this.base}/referentiel`)
        .pipe(shareReplay({ bufferSize: 1, refCount: false }));
      this.referentiel$.subscribe({ error: () => (this.referentiel$ = undefined) });
    }
    return this.referentiel$;
  }

  zones(): Observable<ZoneRef[]> {
    return this.http.get<ZoneRef[]>(`${this.base}/zones`);
  }

  creerZone(body: CreationZoneRequest): Observable<ZoneRef> {
    return this.http.post<ZoneRef>(`${this.base}/zones`, body).pipe(tap(() => this.oublierReferentiel()));
  }

  modifierZone(code: string, body: ModificationZoneRequest): Observable<ZoneRef> {
    return this.http.put<ZoneRef>(`${this.base}/zones/${code}`, body).pipe(tap(() => this.oublierReferentiel()));
  }

  activerZone(code: string): Observable<ZoneRef> {
    return this.http.post<ZoneRef>(`${this.base}/zones/${code}/activer`, {}).pipe(tap(() => this.oublierReferentiel()));
  }

  desactiverZone(code: string): Observable<ZoneRef> {
    return this.http
      .post<ZoneRef>(`${this.base}/zones/${code}/desactiver`, {})
      .pipe(tap(() => this.oublierReferentiel()));
  }

  private oublierReferentiel(): void {
    this.referentiel$ = undefined;
  }

  /** Une page d'unités (50 par défaut, triées par niveau puis code). */
  rechercher(filtre: FiltreUnites, page: PageRequete = {}): Observable<Page<UniteResume>> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries({ ...filtre, ...page })) {
      for (const valeur of Array.isArray(v) ? v : [v]) {
        if (valeur !== null && valeur !== undefined && valeur !== false && String(valeur).trim() !== '') {
          params = params.append(k, String(valeur).trim());
        }
      }
    }
    return this.http.get<Page<UniteResume>>(`${this.base}/unites`, { params });
  }

  unite(id: string): Observable<UniteDetail> {
    return this.http.get<UniteDetail>(`${this.base}/unites/${id}`);
  }

  /** Enfants directs, triés par code. */
  enfants(id: string, page = 0, taille = 100): Observable<Page<NoeudArbre>> {
    return this.http.get<Page<NoeudArbre>>(`${this.base}/unites/${id}/enfants`, { params: { page, taille } });
  }

  descendants(id: string, page = 0, taille = 50): Observable<Page<UniteResume>> {
    return this.http.get<Page<UniteResume>>(`${this.base}/unites/${id}/descendants`, { params: { page, taille } });
  }

  /** Du pays au parent direct. */
  ascendants(id: string): Observable<UniteResume[]> {
    return this.http.get<UniteResume[]>(`${this.base}/unites/${id}/ascendants`);
  }

  couvertures(id: string): Observable<Couverture[]> {
    return this.http.get<Couverture[]>(`${this.base}/unites/${id}/regroupements`);
  }

  /** Pays de la zone (racines de l'arbre) ; les niveaux suivants se chargent par {@link enfants}. */
  arbre(zone: Zone): Observable<NoeudArbre[]> {
    return this.http.get<NoeudArbre[]>(`${this.base}/arbre`, { params: { zone } });
  }

  creer(body: CreationUniteRequest): Observable<UniteDetail> {
    return this.http.post<UniteDetail>(`${this.base}/unites`, body);
  }

  modifier(id: string, body: ModificationUniteRequest): Observable<UniteDetail> {
    return this.http.put<UniteDetail>(`${this.base}/unites/${id}`, body);
  }

  /** Rattache l'unité à un autre parent ; toute sa branche suit. */
  deplacer(id: string, parentId: string): Observable<UniteDetail> {
    return this.http.post<UniteDetail>(`${this.base}/unites/${id}/deplacer`, { parentId });
  }

  activer(id: string): Observable<UniteDetail> {
    return this.http.post<UniteDetail>(`${this.base}/unites/${id}/activer`, {});
  }

  desactiver(id: string): Observable<UniteDetail> {
    return this.http.post<UniteDetail>(`${this.base}/unites/${id}/desactiver`, {});
  }

  membres(regroupementId: string): Observable<Membre[]> {
    return this.http.get<Membre[]>(`${this.base}/regroupements/${regroupementId}/membres`);
  }

  /** Membres et toute leur descendance. */
  perimetre(regroupementId: string, page = 0, taille = 50): Observable<Page<UniteResume>> {
    return this.http.get<Page<UniteResume>>(`${this.base}/regroupements/${regroupementId}/perimetre`, {
      params: { page, taille }
    });
  }

  /** Renvoie la liste des membres à jour. */
  ajouterMembre(regroupementId: string, uniteId: string): Observable<Membre[]> {
    return this.http.post<Membre[]>(`${this.base}/regroupements/${regroupementId}/membres`, { uniteId });
  }

  /** Renvoie la liste des membres à jour. */
  retirerMembre(regroupementId: string, uniteId: string): Observable<Membre[]> {
    return this.http.delete<Membre[]>(`${this.base}/regroupements/${regroupementId}/membres/${uniteId}`);
  }

  /** essai = true : contrôle complet sans rien écrire. Sinon tout ou rien. */
  importer(objet: ObjetImport, fichier: File, essai: boolean): Observable<RapportImport> {
    const form = new FormData();
    form.append('fichier', fichier, fichier.name);
    return this.http.post<RapportImport>(`${this.base}/imports/${objet}`, form, { params: { essai } });
  }

  modele(objet: ObjetImport, format: FormatModele): Observable<Blob> {
    return this.http.get(`${this.base}/imports/modeles/${objet}`, { params: { format }, responseType: 'blob' });
  }
}
