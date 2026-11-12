import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TransferField {
  name: string;
  label: string;
  type: string;
  required: boolean;
  placeholder?: string;
  pattern?: string | null;
  options?: string[] | null;
}

export type ParticipantKind = 'WALLET' | 'BANK';
export type RailScope = 'INTERNAL' | 'EXTERNAL';

export interface TransferProvider {
  id: string;
  code: string;
  name: string;
  countryCode: string;
  actif: boolean;
  canEmit: boolean;
  canReceive: boolean;
  participantKind?: ParticipantKind;
  groupCode?: string | null;
  providerId?: string | null;
  minAmount: number | null;
  maxAmount: number | null;
  emitterFields: TransferField[];
  beneficiaryFields: TransferField[];
  ordreAffichage?: number;
}

export interface TransferRail {
  id: string;
  code: string;
  name: string;
  description?: string | null;
  railScope: RailScope;
  currency: string;
  minAmount: number | null;
  maxAmount: number | null;
  requiresBank: boolean;
  sameCountryOnly: boolean;
  sameGroupOnly: boolean;
  crossBorderOnly: boolean;
  extraFields: TransferField[];
  actif: boolean;
  ordre?: number | null;
  displayLabel?: string | null;
  etaLabel?: string | null;
  warning?: string | null;
}

export interface TransferModeOption {
  railCode: string;
  railName: string;
  displayLabel: string;
  etaLabel: string;
  warning: string | null;
  description: string | null;
  railScope: RailScope;
  recommended: boolean;
  amount: number;
  feeAmount: number;
  totalDebit: number;
  amountReceived: number;
  feeBearer: 'SENDER' | 'RECIPIENT';
  feeBreakdown: string;
  currency: string;
}

export interface TransferQuote {
  transferOrderId: string;
  orderNumber: string;
  emitterProviderCode: string;
  emitterProviderName: string;
  beneficiaryProviderCode: string;
  beneficiaryProviderName: string;
  railCode: string;
  railName: string;
  countryCode: string;
  currency: string;
  sameProvider: boolean;
  amount: number;
  feeAmount: number;
  totalDebit: number;
  amountReceived: number;
  feeBearer: 'SENDER' | 'RECIPIENT';
  feeRuleCode: string | null;
  feeBreakdown: string;
  status: string;
  quotedAt: string;
  expiresAt: string;
}

export interface FeeRuleLine {
  id?: string;
  feeType: 'FIXED' | 'PERCENT';
  minAmount: number | null;
  maxAmount: number | null;
  value: number;
  fixedAmount?: number | null;
  percentValue?: number | null;
  minFee?: number | null;
  maxFee?: number | null;
  ordre: number;
}

export interface FeeRule {
  id: string;
  code: string;
  name: string;
  currency: string;
  description?: string;
  actif: boolean;
  feeBearer: 'SENDER' | 'RECIPIENT';
  lines: FeeRuleLine[];
}

export type FeePolicyType = 'SAME_PROVIDER' | 'CROSS_PROVIDER' | 'PAIR_OVERRIDE';

export interface TransferFeePolicy {
  id: string;
  code: string;
  name: string;
  policyType: FeePolicyType;
  countryCode: string;
  currency: string;
  emitterCode: string | null;
  beneficiaryCode: string | null;
  railCode: string | null;
  feeRuleId: string;
  feeRuleCode: string;
  actif: boolean;
  ordre: number;
}

@Injectable({ providedIn: 'root' })
export class TransferService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/transfers`;
  private readonly adminBase = `${environment.apiUrl}/admin/transfers`;

  listProviders(params?: { canEmit?: boolean; canReceive?: boolean }): Observable<TransferProvider[]> {
    let httpParams = new HttpParams();
    if (params?.canEmit !== undefined) {
      httpParams = httpParams.set('canEmit', String(params.canEmit));
    }
    if (params?.canReceive !== undefined) {
      httpParams = httpParams.set('canReceive', String(params.canReceive));
    }
    return this.http.get<TransferProvider[]>(`${this.base}/providers`, { params: httpParams });
  }

  listRails(): Observable<TransferRail[]> {
    return this.http.get<TransferRail[]>(`${this.base}/rails`);
  }

  listEligibleRails(
    emitterProviderCode: string,
    beneficiaryProviderCode: string,
    amount?: number | null
  ): Observable<TransferRail[]> {
    let httpParams = new HttpParams()
      .set('emitterProviderCode', emitterProviderCode)
      .set('beneficiaryProviderCode', beneficiaryProviderCode);
    if (amount != null && !Number.isNaN(amount)) {
      httpParams = httpParams.set('amount', String(amount));
    }
    return this.http.get<TransferRail[]>(`${this.base}/rails/eligible`, { params: httpParams });
  }

  estimate(body: {
    emitterProviderCode: string;
    beneficiaryProviderCode: string;
    amount: number;
    countryCode?: string;
  }): Observable<TransferModeOption[]> {
    return this.http.post<TransferModeOption[]>(`${this.base}/estimate`, body);
  }

  quote(body: {
    emitterProviderCode: string;
    beneficiaryProviderCode: string;
    railCode: string;
    amount: number;
    emitter: Record<string, unknown>;
    recipient: Record<string, unknown>;
    customerReference?: string;
    countryCode?: string;
  }): Observable<TransferQuote> {
    return this.http.post<TransferQuote>(`${this.base}/quote`, body);
  }

  getOrder(id: string): Observable<unknown> {
    return this.http.get(`${this.base}/orders/${id}`);
  }

  adminListProviders(): Observable<TransferProvider[]> {
    return this.http.get<TransferProvider[]>(`${this.adminBase}/providers`);
  }

  adminSaveProvider(body: Omit<TransferProvider, 'id'> & { id?: string }, id?: string): Observable<TransferProvider> {
    const payload = {
      code: body.code,
      name: body.name,
      countryCode: body.countryCode || 'BF',
      actif: body.actif ?? true,
      canEmit: body.canEmit ?? true,
      canReceive: body.canReceive ?? true,
      participantKind: body.participantKind ?? 'WALLET',
      groupCode: body.groupCode || null,
      minAmount: body.minAmount,
      maxAmount: body.maxAmount,
      emitterFields: body.emitterFields || [],
      beneficiaryFields: body.beneficiaryFields || [],
      ordreAffichage: body.ordreAffichage,
      providerId: body.providerId ?? null
    };
    return id
      ? this.http.put<TransferProvider>(`${this.adminBase}/providers/${id}`, payload)
      : this.http.post<TransferProvider>(`${this.adminBase}/providers`, payload);
  }

  adminListRails(): Observable<TransferRail[]> {
    return this.http.get<TransferRail[]>(`${this.adminBase}/rails`);
  }

  adminSaveRail(body: Omit<TransferRail, 'id'> & { id?: string }, id?: string): Observable<TransferRail> {
    const payload = {
      code: body.code,
      name: body.name,
      description: body.description ?? null,
      railScope: body.railScope,
      currency: body.currency || 'XOF',
      minAmount: body.minAmount,
      maxAmount: body.maxAmount,
      requiresBank: body.requiresBank ?? false,
      sameCountryOnly: body.sameCountryOnly ?? false,
      sameGroupOnly: body.sameGroupOnly ?? false,
      crossBorderOnly: body.crossBorderOnly ?? false,
      extraFields: body.extraFields || [],
      actif: body.actif ?? true,
      ordre: body.ordre ?? 0
    };
    return id
      ? this.http.put<TransferRail>(`${this.adminBase}/rails/${id}`, payload)
      : this.http.post<TransferRail>(`${this.adminBase}/rails`, payload);
  }

  adminListFeeRules(): Observable<FeeRule[]> {
    return this.http.get<FeeRule[]>(`${this.adminBase}/fee-rules`);
  }

  adminSaveFeeRule(body: Omit<FeeRule, 'id'> & { id?: string }, id?: string): Observable<FeeRule> {
    return id
      ? this.http.put<FeeRule>(`${this.adminBase}/fee-rules/${id}`, body)
      : this.http.post<FeeRule>(`${this.adminBase}/fee-rules`, body);
  }

  adminListFeePolicies(): Observable<TransferFeePolicy[]> {
    return this.http.get<TransferFeePolicy[]>(`${this.adminBase}/fee-policies`);
  }

  adminSaveFeePolicy(body: Omit<TransferFeePolicy, 'id' | 'feeRuleCode'> & { id?: string }, id?: string): Observable<TransferFeePolicy> {
    const payload = {
      code: body.code,
      name: body.name,
      policyType: body.policyType,
      countryCode: body.countryCode,
      currency: body.currency,
      emitterCode: body.emitterCode || null,
      beneficiaryCode: body.beneficiaryCode || null,
      railCode: body.railCode || null,
      feeRuleId: body.feeRuleId,
      actif: body.actif ?? true,
      ordre: body.ordre ?? 0
    };
    return id
      ? this.http.put<TransferFeePolicy>(`${this.adminBase}/fee-policies/${id}`, payload)
      : this.http.post<TransferFeePolicy>(`${this.adminBase}/fee-policies`, payload);
  }
}
