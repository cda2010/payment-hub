import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type AmountPolicy = 'FULL_ONLY' | 'PARTIAL' | 'EDITABLE';
export type LookupMode = 'SUBSCRIBER' | 'BILL_REF' | 'BOTH';
export type JourneyCode = 'OPEN_BILLS' | 'GET_INFOS' | 'PREPAID';
export type ProductFieldRole = 'SUBSCRIBER' | 'BILL_REF' | 'AMOUNT' | 'EXTRA';

export interface ProductField {
  name: string;
  label: string;
  type: string;
  required: boolean;
  placeholder?: string | null;
  pattern?: string | null;
  role?: ProductFieldRole | null;
  options?: string[] | null;
  defaultValue?: string | null;
}

export interface Bill {
  id: string;
  billerCode: string;
  billerName: string;
  productCode: string;
  productName: string;
  billReference: string;
  subscriberReference: string;
  amountDue: number | null;
  currency: string;
  dueDate: string | null;
  status: string;
  label: string;
  customerName: string;
  amountPolicy: AmountPolicy;
  minAmount: number | null;
  maxAmount: number | null;
  allowOverpay: boolean;
  journeyCode: JourneyCode;
}

export interface BillSearchResponse {
  searchMode: string;
  query: string;
  bills: Bill[];
  count: number;
}

export interface Biller {
  id: string;
  code: string;
  name: string;
  description?: string;
  logoUrl?: string;
  actif: boolean;
  defaultCurrency: string;
  ordreAffichage?: number;
  categoryCode?: string | null;
  categoryLabel?: string | null;
  productCount: number;
  providerId?: string | null;
  providerCode?: string | null;
  providerLibelle?: string | null;
}

export interface BillerProduct {
  id: string;
  billerId: string;
  billerCode: string;
  code: string;
  name: string;
  description?: string;
  actif: boolean;
  lookupMode: LookupMode;
  amountPolicy: AmountPolicy;
  minAmount: number | null;
  maxAmount: number | null;
  allowOverpay: boolean;
  journeyCode: JourneyCode;
  paymentFlowId: string | null;
  ordreAffichage?: number;
  providerId?: string | null;
  providerCode?: string | null;
  providerLibelle?: string | null;
  resolvedProviderId?: string | null;
  resolvedProviderCode?: string | null;
  lookupFields: ProductField[];
  confirmFields: ProductField[];
  amountFields: ProductField[];
}

@Injectable({ providedIn: 'root' })
export class BillService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/bills`;

  listBillers(): Observable<Biller[]> {
    return this.http.get<Biller[]>(`${this.base}/billers`);
  }

  listProducts(billerId: string): Observable<BillerProduct[]> {
    return this.http.get<BillerProduct[]>(`${this.base}/billers/${billerId}/products`);
  }

  search(params: {
    subscriberReference?: string;
    billReference?: string;
    billerCode: string;
    productCode: string;
  }): Observable<BillSearchResponse> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<BillSearchResponse>(`${this.base}/search`, { params: httpParams });
  }

  get(id: string): Observable<Bill> {
    return this.http.get<Bill>(`${this.base}/${id}`);
  }
}

