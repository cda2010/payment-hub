import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { IntegrationScreen } from './gateway.models';

export interface GatewayBiller {
  id: string;
  code: string;
  name: string;
  description?: string | null;
  actif?: boolean;
  defaultCurrency?: string;
  productCount?: number;
}

export interface GatewayBillerProduct {
  id: string;
  billerId: string;
  billerCode: string;
  code: string;
  name: string;
  description?: string | null;
  actif: boolean;
  journeyCode?: string | null;
  amountPolicy?: string | null;
  lookupMode?: string | null;
}

export interface CatalogItem {
  id: string;
  label: string;
  sub?: string | null;
  icon?: string | null;
  value?: string | null;
  meta?: Record<string, unknown>;
}

export interface CatalogSection {
  id: string;
  displayKind: string;
  dataSource: string;
  origin?: string;
  title?: string | null;
  bindsTo?: string | null;
  searchable?: boolean;
  pageable?: boolean;
  hasMore?: boolean;
  loading?: boolean;
  selectedId?: string | null;
  items: CatalogItem[];
}

export interface CatalogLayoutResponse {
  version: number;
  productCode: string;
  journeyCode?: string | null;
  channel?: string | null;
  sections: CatalogSection[];
}

export interface DataSourceResolveResponse {
  code: string;
  origin: string;
  items: CatalogItem[];
  hasMore: boolean;
  params?: Record<string, unknown>;
}

interface BillerPageResponse {
  items: GatewayBiller[];
  page: number;
  size: number;
  total: number;
  hasMore: boolean;
}

/**
 * Client Product Gateway (`/api/v1/gateway`) — parcours catalogue paramétrés.
 * Préférer à {@link PaymentService} pour les parcours produit.
 */
@Injectable({ providedIn: 'root' })
export class GatewayService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/gateway`;

  /** Démarre un produit PUBLISHED (ou alias connu côté facade). */
  startProduct(
    productCode: string,
    body: Record<string, unknown> = {},
    idempotencyKey?: string,
  ): Observable<IntegrationScreen> {
    let headers = new HttpHeaders();
    if (idempotencyKey) {
      headers = headers.set('Idempotency-Key', idempotencyKey);
    }
    const code = encodeURIComponent(productCode.trim());
    return this.http.post<IntegrationScreen>(`${this.base}/products/${code}`, body, { headers });
  }

  /** Catalogue présentation matérialisé (sections + items). */
  productCatalog(
    productCode: string,
    opts?: {
      channel?: string;
      category?: string;
      q?: string;
      page?: number;
      size?: number;
      hydrate?: boolean;
    },
  ): Observable<CatalogLayoutResponse> {
    let params = new HttpParams()
      .set('channel', opts?.channel?.trim() || 'WEB')
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 30))
      .set('hydrate', String(opts?.hydrate !== false));
    if (opts?.category?.trim()) {
      params = params.set('category', opts.category.trim());
    }
    if (opts?.q?.trim()) {
      params = params.set('q', opts.q.trim());
    }
    const code = encodeURIComponent(productCode.trim());
    return this.http.get<CatalogLayoutResponse>(`${this.base}/products/${code}/catalog`, { params });
  }

  listBillers(opts?: {
    q?: string;
    page?: number;
    size?: number;
    category?: string;
  }): Observable<GatewayBiller[]> {
    let params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 50));
    if (opts?.q?.trim()) {
      params = params.set('q', opts.q.trim());
    }
    if (opts?.category?.trim()) {
      params = params.set('category', opts.category.trim());
    }
    return this.http
      .get<BillerPageResponse>(`${this.base}/billers`, { params })
      .pipe(map((page) => (page?.items ?? []).filter((b) => b.actif !== false)));
  }

  listBillerProducts(billerKey: string): Observable<GatewayBillerProduct[]> {
    const key = encodeURIComponent(billerKey.trim());
    return this.http
      .get<GatewayBillerProduct[]>(`${this.base}/billers/${key}/products`)
      .pipe(map((list) => (list ?? []).filter((p) => p.actif !== false)));
  }

  /** Résolution d'une source Formio / catalogue (LOCAL | OMNICANAL | GATEWAY). */
  resolveDataSource(
    code: string,
    query: Record<string, string> = {},
  ): Observable<DataSourceResolveResponse> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries(query)) {
      if (v != null && String(v).trim() !== '') {
        params = params.set(k, String(v).trim());
      }
    }
    return this.http.get<DataSourceResolveResponse>(
      `${this.base}/data-sources/${encodeURIComponent(code.trim())}`,
      { params },
    );
  }

  getPayment(paymentId: string): Observable<IntegrationScreen> {
    return this.http.get<IntegrationScreen>(
      `${this.base}/payments/${encodeURIComponent(paymentId)}`,
    );
  }

  submitAction(
    paymentId: string,
    action: string,
    data: Record<string, unknown> = {},
  ): Observable<IntegrationScreen> {
    return this.http.post<IntegrationScreen>(
      `${this.base}/payments/${encodeURIComponent(paymentId)}/actions`,
      { action, data },
    );
  }

  /** Inbox validations (rôles JWT / ADMIN voit tout). */
  approvalInbox(): Observable<ApprovalInboxItem[]> {
    return this.http.get<ApprovalInboxItem[]>(`${this.base}/approvals/inbox`);
  }

  decideApproval(
    paymentId: string,
    action: 'APPROVE' | 'REJECT',
    data: Record<string, unknown> = {},
  ): Observable<IntegrationScreen> {
    return this.http.post<IntegrationScreen>(
      `${this.base}/approvals/${encodeURIComponent(paymentId)}/decide`,
      { action, data },
    );
  }

  /** Historique transactions (payment_transaction). */
  listTransactions(opts?: {
    customerReference?: string;
    status?: string;
    serviceType?: string;
    product?: string;
    provider?: string;
    q?: string;
    from?: string;
    to?: string;
    page?: number;
    size?: number;
  }): Observable<GatewayTransactionPage> {
    let params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    const set = (key: string, value?: string) => {
      if (value?.trim()) {
        params = params.set(key, value.trim());
      }
    };
    set('customerReference', opts?.customerReference);
    set('status', opts?.status);
    set('serviceType', opts?.serviceType);
    set('product', opts?.product);
    set('provider', opts?.provider);
    set('q', opts?.q);
    set('from', opts?.from);
    set('to', opts?.to);
    return this.http.get<GatewayTransactionPage>(`${this.base}/transactions`, { params });
  }
}

export interface GatewayTransactionSummary {
  paymentId: string;
  provider?: string | null;
  serviceType?: string | null;
  catalogServiceCode?: string | null;
  amount?: number | null;
  currency?: string | null;
  status?: string | null;
  currentStep?: string | null;
  providerReference?: string | null;
  customerReference?: string | null;
  phoneNumber?: string | null;
  errorCode?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  completedAt?: string | null;
  /** Libellé relevé formaté serveur (Accept-Language). */
  title?: string | null;
  /** Sous-ligne formatée serveur. */
  subtitle?: string | null;
  /** Nom produit localisé. */
  productLabel?: string | null;
  /** Statut localisé. */
  statusLabel?: string | null;
  /** Point agent / agence (détail opération). */
  agentDisplay?: string | null;
  /** Contexte métier sanitizé (rejeu / détail). */
  context?: Record<string, unknown> | null;
}

export interface GatewayTransactionPage {
  items: GatewayTransactionSummary[];
  page: number;
  size: number;
  total: number;
  hasMore: boolean;
}

export interface ApprovalInboxItem {
  stepInstanceId?: string;
  approvalInstanceId?: string;
  paymentId: string;
  profileCode?: string | null;
  levelNumber: number;
  requiredApprovals: number;
  collectedApprovals: number;
  candidateGroups?: string | null;
  initiatedBy?: string | null;
  amount?: number | null;
  currency?: string | null;
  paymentStatus?: string | null;
  createdAt?: string | null;
}
