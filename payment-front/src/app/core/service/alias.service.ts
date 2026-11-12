import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Alias de comptes (payment-alias) — propriétaires identity (PARTY/ORG) ou network (UNITE). */

export type AliasOwnerKind = 'PARTY' | 'ORG' | 'UNITE';
export type AliasType = 'IBAN' | 'WALLET' | 'INTERNAL' | 'TILL' | 'PHONE';

export interface AccountAlias {
  id: string;
  tenantId: string;
  aliasCode: string;
  label: string;
  aliasType: AliasType;
  externalRef: string;
  currency: string;
  ownerKind: AliasOwnerKind;
  ownerId: string;
  providerCode?: string | null;
  active: boolean;
}

export interface AliasRequest {
  tenantId?: string | null;
  aliasCode: string;
  label?: string | null;
  aliasType?: AliasType | null;
  externalRef: string;
  currency?: string | null;
  ownerKind: AliasOwnerKind;
  ownerId: string;
  providerCode?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AliasService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.aliasApiUrl;

  lister(filtre: { tenantId?: string; ownerKind?: AliasOwnerKind; ownerId?: string } = {}): Observable<AccountAlias[]> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries(filtre)) {
      if (v) {
        params = params.set(k, v);
      }
    }
    return this.http.get<AccountAlias[]>(this.base, { params });
  }

  resolve(aliasCode: string, tenantId?: string): Observable<AccountAlias> {
    let params = new HttpParams();
    if (tenantId) {
      params = params.set('tenantId', tenantId);
    }
    return this.http.get<AccountAlias>(`${this.base}/resolve/${encodeURIComponent(aliasCode)}`, { params });
  }

  creer(body: AliasRequest): Observable<AccountAlias> {
    return this.http.post<AccountAlias>(this.base, body);
  }

  modifier(id: string, body: AliasRequest): Observable<AccountAlias> {
    return this.http.put<AccountAlias>(`${this.base}/${id}`, body);
  }

  desactiver(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
