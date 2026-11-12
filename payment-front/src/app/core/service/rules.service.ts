import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface RuleSummary {
  id: string;
  code: string;
  libelle: string;
  description?: string | null;
  category: string;
  status: string;
  legalRef?: string | null;
  limitValue?: number | null;
  sourceType?: string | null;
  sourcePeriod?: string | null;
  sourceScope?: string | null;
  operator?: string | null;
  priority?: number | null;
  includeCurrent?: boolean | null;
  effectiveFrom?: string | null;
  message?: string | null;
  conditionsJson?: string | null;
}

export interface RuleWrite {
  code?: string;
  libelle: string;
  description?: string;
  category: string;
  priority?: number;
  conditionsJson?: string;
  sourceType: string;
  sourcePeriod?: string | null;
  sourceScope?: string | null;
  includeCurrent?: boolean;
  operator: string;
  limitValue?: number | null;
  message: string;
  mandatory?: boolean;
  legalRef?: string;
  effectiveFrom?: string;
  groupCode?: string;
  publish?: boolean;
}

export interface RuleGroupSummary {
  id: string;
  code: string;
  libelle: string;
  description?: string | null;
  transactionType?: string | null;
  productCode?: string | null;
  evaluationStrategy: string;
  status: string;
  items: {
    ruleVersionId: string;
    ruleCode: string;
    ruleVersion: number;
    sortOrder: number;
    message: string;
  }[];
}

export interface GroupWrite {
  code?: string;
  libelle: string;
  description?: string;
  transactionType?: string;
  productCode?: string;
  evaluationStrategy: string;
  status: string;
  ruleCodes: string[];
}

export interface EvaluationRequest {
  transactionId: string;
  transactionType: string;
  groupCode?: string;
  amount: number;
  currency?: string;
  customerId?: string;
  walletId?: string;
  context?: Record<string, unknown>;
}

export interface EvaluationResponse {
  transactionId: string;
  groupCode: string;
  decision: string;
  authorized: boolean;
  simulate: boolean;
  reservationId?: string | null;
  violations: {
    ruleCode: string;
    ruleVersion: number;
    message: string;
    currentCumul?: number | null;
    requestedAmount?: number | null;
    projectedCumul?: number | null;
    limit?: number | null;
  }[];
  evaluatedRules: string[];
}

@Injectable({ providedIn: 'root' })
export class RulesService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.rulesApiUrl;

  listerRegles(): Observable<RuleSummary[]> {
    return this.http.get<RuleSummary[]>(`${this.base}`);
  }

  listerGroupes(): Observable<RuleGroupSummary[]> {
    return this.http.get<RuleGroupSummary[]>(`${environment.rulesGroupsApiUrl}`);
  }

  detailGroupe(code: string): Observable<RuleGroupSummary> {
    return this.http.get<RuleGroupSummary>(`${environment.rulesGroupsApiUrl}/${code}`);
  }

  simuler(body: EvaluationRequest): Observable<EvaluationResponse> {
    return this.http.post<EvaluationResponse>(`${this.base}/simulate`, body);
  }

  evaluer(body: EvaluationRequest): Observable<EvaluationResponse> {
    return this.http.post<EvaluationResponse>(`${this.base}/evaluate`, body);
  }

  creer(body: RuleWrite): Observable<RuleSummary> {
    return this.http.post<RuleSummary>(this.base, body);
  }

  modifier(id: string, body: RuleWrite): Observable<RuleSummary> {
    return this.http.put<RuleSummary>(`${this.base}/${id}`, body);
  }

  creerGroupe(body: GroupWrite): Observable<RuleGroupSummary> {
    return this.http.post<RuleGroupSummary>(environment.rulesGroupsApiUrl, body);
  }

  modifierGroupe(code: string, body: GroupWrite): Observable<RuleGroupSummary> {
    return this.http.put<RuleGroupSummary>(`${environment.rulesGroupsApiUrl}/${code}`, body);
  }
}
