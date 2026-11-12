import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface FormuleCommission {
  id: string;
  code: string;
  version: number;
  libelle: string;
  description?: string | null;
  type: string;
  status: string;
  parametres: { code: string; libelle: string; dataType: string; required: boolean; defaultValue?: string | null }[];
}

export interface RegleCommission {
  id: string;
  code: string;
  libelle: string;
  beneficiaryRole: string;
  formuleCode: string;
  formuleVersion: number;
  commissionBase: string;
  tierSelectionSource?: string | null;
  fundingType: string;
  funderRole?: string | null;
  paramsJson: string;
  floorAmount?: number | null;
  capAmount?: number | null;
  priority: number;
  active: boolean;
}

export interface GrilleCommission {
  id: string;
  code: string;
  version: number;
  libelle: string;
  description?: string | null;
  operationType: string;
  productCode?: string | null;
  channelCode?: string | null;
  currency: string;
  status: string;
  validFrom: string;
  validTo?: string | null;
  envelopeMode: string;
  regles: RegleCommission[];
}

export interface PartCommission {
  ruleId: string;
  ruleCode: string;
  beneficiaryRole: string;
  formuleCode: string;
  commissionBase: string;
  baseAmount: number;
  commissionAmount: number;
  fundingType: string;
  detail?: string | null;
}

export interface SimulationResult {
  grilleId: string;
  grilleCode: string;
  grilleVersion: number;
  fees: number;
  totalCommissions: number;
  reliquat: number;
  envelopeMode: string;
  parts: PartCommission[];
  warnings: string[];
}

export interface SimulationRequest {
  transactionId?: string;
  operation: string;
  productCode?: string;
  channel?: string;
  currency?: string;
  amount: number;
  fees: number;
}

export interface CreateGrilleRequest {
  code: string;
  libelle: string;
  description?: string | null;
  operationType: string;
  productCode?: string | null;
  channelCode?: string | null;
  currency?: string;
  validFrom?: string | null;
  validTo?: string | null;
  envelopeMode?: string;
}

export interface UpdateGrilleRequest {
  libelle?: string;
  description?: string | null;
  operationType?: string;
  productCode?: string | null;
  channelCode?: string | null;
  currency?: string;
  validFrom?: string | null;
  validTo?: string | null;
  envelopeMode?: string;
}

export interface UpsertRegleRequest {
  code: string;
  libelle: string;
  beneficiaryRole: string;
  formuleCode: string;
  formuleVersion?: number;
  commissionBase?: string;
  fundingType?: string;
  funderRole?: string | null;
  paramsJson?: string;
  floorAmount?: number | null;
  capAmount?: number | null;
  priority?: number;
  active?: boolean;
}

@Injectable({ providedIn: 'root' })
export class CommissionService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.commissionApiUrl;

  listerFormules(): Observable<FormuleCommission[]> {
    return this.http.get<FormuleCommission[]>(`${this.base}/formulas`);
  }

  listerGrilles(status?: string | null): Observable<GrilleCommission[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<GrilleCommission[]>(`${this.base}/grids`, { params });
  }

  grille(id: string): Observable<GrilleCommission> {
    return this.http.get<GrilleCommission>(`${this.base}/grids/${id}`);
  }

  creerGrille(body: CreateGrilleRequest): Observable<GrilleCommission> {
    return this.http.post<GrilleCommission>(`${this.base}/grids`, body);
  }

  majGrille(id: string, body: UpdateGrilleRequest): Observable<GrilleCommission> {
    return this.http.put<GrilleCommission>(`${this.base}/grids/${id}`, body);
  }

  supprimerGrille(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/grids/${id}`);
  }

  publier(id: string): Observable<GrilleCommission> {
    return this.http.post<GrilleCommission>(`${this.base}/grids/${id}/publish`, {});
  }

  nouvelleVersion(id: string): Observable<GrilleCommission> {
    return this.http.post<GrilleCommission>(`${this.base}/grids/${id}/versions`, {});
  }

  ajouterRegle(grilleId: string, body: UpsertRegleRequest): Observable<RegleCommission> {
    return this.http.post<RegleCommission>(`${this.base}/grids/${grilleId}/rules`, body);
  }

  majRegle(grilleId: string, ruleId: string, body: UpsertRegleRequest): Observable<RegleCommission> {
    return this.http.put<RegleCommission>(`${this.base}/grids/${grilleId}/rules/${ruleId}`, body);
  }

  supprimerRegle(grilleId: string, ruleId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/grids/${grilleId}/rules/${ruleId}`);
  }

  simuler(body: SimulationRequest): Observable<SimulationResult> {
    return this.http.post<SimulationResult>(`${this.base}/simulations`, body);
  }
}
