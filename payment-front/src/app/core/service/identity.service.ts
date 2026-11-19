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

export interface PieceDeclaree {
  typeDocument?: string | null;
  numeroPiece?: string | null;
  dateValidite?: string | null;
  nationalite?: string | null;
}

export interface Appareil {
  id: string;
  appareilId: string;
  nom: string;
  plateforme: string;
  statut: string;
  vuLe: string;
  courant: boolean;
}

export interface Party {
  id: string;
  tenantId: string;
  code: string;
  kind: PartyKind;
  displayName: string;
  phone?: string | null;
  email?: string | null;
  externalRef?: string | null;
  kycLevel?: string | null;
  customerStatus?: string | null;
  active: boolean;
}

export interface CompteOuvert {
  maison?: string | null;
  numero: string;
}

export interface DossierFiche {
  nom?: string | null;
  prenom?: string | null;
  dateNaissance?: string | null;
  sexe?: string | null;
  lieuNaissance?: string | null;
  nationalite?: string | null;
  adresse: Record<string, string>;
  comptes: CompteOuvert[];
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

export interface OrgNode {
  id: string;
  tenantId: string;
  code: string;
  name: string;
  type: string;
  parentId?: string | null;
  active: boolean;
  children?: OrgNode[];
}

export interface ApprovalRule {
  code: string;
  label: string;
  position: number;
  totalAmountLte?: number | null;
  lineCountLte?: number | null;
  maxLineAmountLte?: number | null;
  elseRule: boolean;
  profile: string;
}

export interface ApprovalAssignee {
  level: string;
  type: string;
  value: string;
  scope: string;
}

export interface ChannelUser {
  id: string;
  tenantId: string;
  username: string;
  partyId: string;
  audience: IdentityAudience;
  role: string;
  enabled: boolean;
  lastLoginAt?: string | null;
}

export interface Membership {
  id: string;
  userId: string;
  orgNodeId: string;
  scope: string;
  active: boolean;
}

export interface UtilisateurOrganisation {
  id: string;
  username: string;
  nom: string;
  role: string;
  enabled: boolean;
  rattachements: Membership[];
}

export interface ApprovalPolicy {
  id: string;
  orgNodeId: string;
  orgCode: string;
  productCode: string;
  enabled: boolean;
  override: boolean;
  rules: ApprovalRule[];
  assignees: ApprovalAssignee[];
  updatedAt?: string | null;
}

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

  pieceDeclaree(partyId: string): Observable<PieceDeclaree> {
    return this.http.get<PieceDeclaree>(`${this.base}/parties/${partyId}/piece`);
  }

  dossier(partyId: string): Observable<DossierFiche> {
    return this.http.get<DossierFiche>(`${this.base}/parties/${partyId}/dossier`);
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

  appareilsParty(partyId: string): Observable<Appareil[]> {
    return this.http.get<Appareil[]>(`${this.base}/parties/${partyId}/appareils`);
  }

  revoquerAppareil(partyId: string, id: string): Observable<string> {
    return this.http.post(`${this.base}/parties/${partyId}/appareils/${id}/revoquer`, {}, { responseType: 'text' });
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

  arbreOrg(tenantId: string): Observable<OrgNode[]> {
    const params = new HttpParams().set('tenantId', tenantId);
    return this.http.get<OrgNode[]>(`${this.base}/org-nodes/tree`, { params });
  }

  listerUtilisateurs(
    tenantId: string,
    audience: IdentityAudience = 'CORPORATE',
    includeDisabled = true
  ): Observable<ChannelUser[]> {
    const params = new HttpParams()
      .set('tenantId', tenantId)
      .set('audience', audience)
      .set('includeDisabled', String(includeDisabled));
    return this.http.get<ChannelUser[]>(`${this.base}/users`, { params });
  }

  membershipsUtilisateur(userId: string): Observable<Membership[]> {
    return this.http.get<Membership[]>(`${this.base}/users/${userId}/memberships`);
  }

  politiquesApprobation(tenantId: string): Observable<ApprovalPolicy[]> {
    const params = new HttpParams().set('tenantId', tenantId);
    return this.http.get<ApprovalPolicy[]>(`${this.base}/approval-policies`, { params });
  }

  offreUnite(uniteId: string, date?: string | null): Observable<Offre> {
    let params = new HttpParams();
    if (date) {
      params = params.set('date', date);
    }
    return this.http.get<Offre>(`${this.base}/unites/${uniteId}/offer`, { params });
  }
}
