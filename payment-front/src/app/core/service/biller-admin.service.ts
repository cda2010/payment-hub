import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AmountPolicy, Biller, BillerProduct, JourneyCode, LookupMode, ProductField } from './bill.service';

export interface BillerRequest {
  code: string;
  name: string;
  description?: string;
  logoUrl?: string;
  actif: boolean;
  defaultCurrency: string;
  ordreAffichage?: number | null;
  categoryCode?: string | null;
  /** Fournisseur de type FACTURIER (obligatoire). */
  providerId: string;
}

export interface BillerProductRequest {
  code: string;
  name: string;
  description?: string;
  actif: boolean;
  lookupMode: LookupMode;
  amountPolicy: AmountPolicy;
  minAmount?: number | null;
  maxAmount?: number | null;
  allowOverpay: boolean;
  journeyCode: JourneyCode;
  paymentFlowId?: string | null;
  ordreAffichage?: number | null;
  /** Override optionnel — sinon hérite du facturier. */
  providerId?: string | null;
  lookupFields?: ProductField[];
  confirmFields?: ProductField[];
  amountFields?: ProductField[];
}

@Injectable({ providedIn: 'root' })
export class BillerAdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/billers`;

  list(): Observable<Biller[]> {
    return this.http.get<Biller[]>(this.base);
  }

  get(id: string): Observable<Biller> {
    return this.http.get<Biller>(`${this.base}/${id}`);
  }

  create(body: BillerRequest): Observable<Biller> {
    return this.http.post<Biller>(this.base, body);
  }

  update(id: string, body: BillerRequest): Observable<Biller> {
    return this.http.put<Biller>(`${this.base}/${id}`, body);
  }

  listProducts(billerId: string): Observable<BillerProduct[]> {
    return this.http.get<BillerProduct[]>(`${this.base}/${billerId}/products`);
  }

  createProduct(billerId: string, body: BillerProductRequest): Observable<BillerProduct> {
    return this.http.post<BillerProduct>(`${this.base}/${billerId}/products`, body);
  }

  updateProduct(productId: string, body: BillerProductRequest): Observable<BillerProduct> {
    return this.http.put<BillerProduct>(`${this.base}/products/${productId}`, body);
  }
}

