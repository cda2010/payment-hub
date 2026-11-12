import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Référentiel Identité (payment-identity) : parties et profils d'offre
 * (services disponibles + grilles tarifaires de référence).
 */

export type IdentityAudience = 'RETAIL' | 'CORPORATE' | 'AGENT' | 'MERCHANT' | 'BACKOFFICE';
export type ProfileKind = 'SERVICE' | 'TARIFF' | 'LIMIT';
export type SensLimite = 'EMETTEUR' | 'BENEFICIAIRE' | 'DETENTION';

export type PartyKind = 'PERSON' | 'ORGANIZATION';

export interface Party {
  id: string;
  tenantId: string;
  code: string;
  kind: PartyKind;
  displayName: string;
  phone?: string | null;
  email?: string | null;
  externalRef?: string | null;
  active: boolean;
}

export interface PartyRequest {
  tenantId?: string | null;
  code?: string | null;
  kind?: PartyKind | null;
  displayName: string;
  phone?: string | null;
  email?: string | null;
  externalRef?: string | null;
  active?: boolean | null;
}

export interface FiltreParties {
  tenantId?: string | null;
  kind?: PartyKind | null;
  active?: boolean | null;
  texte?: string | null;
}

export interface PageParties {
  contenu: Party[];
  total: number;
  page: number;
  taille: number;
}

export interface PageRequeteParties {
  page?: number;
  taille?: number;
  tri?: 'code' | 'displayName' | 'kind' | 'phone' | 'email' | 'tenantId' | 'active' | 'maj' | null;
  sens?: 'asc' | 'desc' | null;
}

export const KINDS_PARTY: { value: PartyKind; label: string }[] = [
  { value: 'PERSON', label: 'Personne' },
  { value: 'ORGANIZATION', label: 'Organisation' }
];

export interface ProfileServiceLine {
  serviceCode: string;
  libelle?: string | null;
  active?: boolean | null;
}

export interface ProfileGridLine {
  produitCode: string;
  grilleCode: string;
  canalCode?: string | null;
  devise?: string | null;
}

export interface ProfileLimitLine {
  produitCode: string;
  sens: SensLimite;
  groupeCode: string;
}

export interface Profile {
  id: string;
  tenantId: string;
  code: string;
  libelle: string;
  description?: string | null;
  kind: ProfileKind;
  audience?: IdentityAudience | null;
  active: boolean;
  services: ProfileServiceLine[];
  grilles: ProfileGridLine[];
  limites?: ProfileLimitLine[];
}

export interface ProfileRequest {
  tenantId?: string | null;
  code: string;
  libelle: string;
  description?: string | null;
  kind?: ProfileKind | null;
  audience?: IdentityAudience | null;
  services?: ProfileServiceLine[];
  grilles?: ProfileGridLine[];
  limites?: ProfileLimitLine[];
}

export interface PartyProfile {
  id: string;
  partyId: string;
  profileId: string;
  profileCode?: string | null;
  profileLibelle?: string | null;
  profileKind?: ProfileKind | null;
  dateDebut: string;
  dateFin?: string | null;
  active: boolean;
}

export interface PartyProfileRequest {
  profileId: string;
  dateDebut?: string | null;
  dateFin?: string | null;
}

/** Affectation d'un profil d'offre à une unité du réseau. */
export interface UniteProfile {
  id: string;
  uniteId: string;
  profileId: string;
  profileCode?: string | null;
  profileLibelle?: string | null;
  profileKind?: ProfileKind | null;
  dateDebut: string;
  dateFin?: string | null;
  active: boolean;
}

export interface UniteProfileRequest {
  profileId: string;
  tenantId?: string | null;
  dateDebut?: string | null;
  dateFin?: string | null;
}

export interface Offre {
  ownerKind: 'PARTY' | 'UNITE';
  ownerId: string;
  ownerCode?: string | null;
  date: string;
  profils: Profile[];
  services: ProfileServiceLine[];
  grilles: ProfileGridLine[];
  limites?: ProfileLimitLine[];
}

export const SENS_LIMITE: { value: SensLimite; label: string }[] = [
  { value: 'EMETTEUR', label: 'Émetteur' },
  { value: 'BENEFICIAIRE', label: 'Bénéficiaire' },
  { value: 'DETENTION', label: 'Détention' }
];

export const AUDIENCES: { value: IdentityAudience; label: string }[] = [
  { value: 'RETAIL', label: 'Retail' },
  { value: 'CORPORATE', label: 'Corporate' },
  { value: 'AGENT', label: 'Agent' },
  { value: 'MERCHANT', label: 'Marchand' },
  { value: 'BACKOFFICE', label: 'Back-office' }
];

@Injectable({ providedIn: 'root' })
export class IdentityService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.identityApiUrl;

  listerParties(tenantId?: string | null): Observable<Party[]> {
    let params = new HttpParams();
    if (tenantId) {
      params = params.set('tenantId', tenantId);
    }
    return this.http.get<Party[]>(`${this.base}/parties`, { params });
  }

  rechercherParties(filtre: FiltreParties = {}, page: PageRequeteParties = {}): Observable<PageParties> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries({ ...filtre, ...page })) {
      if (v !== null && v !== undefined && String(v).trim() !== '') {
        params = params.set(k, String(v).trim());
      }
    }
    return this.http.get<PageParties>(`${this.base}/parties/search`, { params });
  }

  party(id: string): Observable<Party> {
    return this.http.get<Party>(`${this.base}/parties/${id}`);
  }

  creerParty(body: PartyRequest): Observable<Party> {
    return this.http.post<Party>(`${this.base}/parties`, body);
  }

  modifierParty(id: string, body: PartyRequest): Observable<Party> {
    return this.http.put<Party>(`${this.base}/parties/${id}`, body);
  }

  listerProfils(
    tenantId?: string | null,
    audience?: IdentityAudience | null,
    kind?: ProfileKind | null
  ): Observable<Profile[]> {
    let params = new HttpParams();
    if (tenantId) {
      params = params.set('tenantId', tenantId);
    }
    if (audience) {
      params = params.set('audience', audience);
    }
    if (kind) {
      params = params.set('kind', kind);
    }
    return this.http.get<Profile[]>(`${this.base}/profiles`, { params });
  }

  profil(id: string): Observable<Profile> {
    return this.http.get<Profile>(`${this.base}/profiles/${id}`);
  }

  creerProfil(body: ProfileRequest): Observable<Profile> {
    return this.http.post<Profile>(`${this.base}/profiles`, body);
  }

  modifierProfil(id: string, body: ProfileRequest): Observable<Profile> {
    return this.http.put<Profile>(`${this.base}/profiles/${id}`, body);
  }

  profilsParty(partyId: string): Observable<PartyProfile[]> {
    return this.http.get<PartyProfile[]>(`${this.base}/parties/${partyId}/profiles`);
  }

  affecterProfil(partyId: string, body: PartyProfileRequest): Observable<PartyProfile> {
    return this.http.post<PartyProfile>(`${this.base}/parties/${partyId}/profiles`, body);
  }

  retirerProfil(partyId: string, profileId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/parties/${partyId}/profiles/${profileId}`);
  }

  offreParty(partyId: string, date?: string | null): Observable<Offre> {
    let params = new HttpParams();
    if (date) {
      params = params.set('date', date);
    }
    return this.http.get<Offre>(`${this.base}/parties/${partyId}/offer`, { params });
  }

  profilsUnite(uniteId: string): Observable<UniteProfile[]> {
    return this.http.get<UniteProfile[]>(`${this.base}/unites/${uniteId}/profiles`);
  }

  affecterProfilUnite(uniteId: string, body: UniteProfileRequest): Observable<UniteProfile> {
    return this.http.post<UniteProfile>(`${this.base}/unites/${uniteId}/profiles`, body);
  }

  retirerProfilUnite(uniteId: string, profileId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/unites/${uniteId}/profiles/${profileId}`);
  }

  offreUnite(uniteId: string, date?: string | null): Observable<Offre> {
    let params = new HttpParams();
    if (date) {
      params = params.set('date', date);
    }
    return this.http.get<Offre>(`${this.base}/unites/${uniteId}/offer`, { params });
  }
}
