import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, of, catchError } from 'rxjs';
import { environment } from '../../../environments/environment';

export type CatalogServiceStatus =
  | 'DRAFT'
  | 'VALIDATED'
  | 'PUBLISHED'
  | 'SUSPENDED'
  | 'ARCHIVED';

/** Libellés par code langue : `{ fr: 'Montant', en: 'Amount' }`. */
export type LocalizedText = Record<string, string>;

/** Traductions indexées par chemin de configuration. */
export type ConfigurationTranslations = Record<string, LocalizedText>;

export interface CategorieService {
  id: string;
  code: string;
  /** Résolu selon la langue du navigateur. */
  name: string;
  description?: string | null;
  nameI18n?: LocalizedText | null;
  descriptionI18n?: LocalizedText | null;
  ordreAffichage?: number | null;
  actif: boolean;
}

export interface CatalogDeService {
  id: string;
  code: string;
  /** Résolu selon la langue du navigateur. */
  name: string;
  description?: string | null;
  nameI18n?: LocalizedText | null;
  descriptionI18n?: LocalizedText | null;
  categoryId?: string | null;
  categoryCode?: string | null;
  categoryName?: string | null;
  status: CatalogServiceStatus;
  currentVersionId?: string | null;
  currentVersion?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export type CatalogPlatformRoleCode =
  | 'SOURCE'
  | 'DESTINATION'
  | 'PAYER'
  | 'BILLER'
  | 'BENEFICIARY'
  | 'CORRESPONDENT'
  | 'PROVIDER'
  | 'AGENT_NETWORK';

export interface CatalogProductRole {
  code: CatalogPlatformRoleCode | string;
  label?: string;
  cardinality?: 'ONE_REQUIRED' | 'ONE_OPTIONAL' | 'MANY';
  selection?: 'USER' | 'FIXED' | 'DERIVED';
  derivedFrom?: string;
}

export interface CatalogProductParticipant {
  role: string;
  providerCode: string;
  default?: boolean;
}

export interface CatalogProductBinding {
  step: string;
  kind: 'UI' | 'INVOKE' | 'SOFT' | 'WAIT' | 'CAPABILITY' | string;
  actorRole?: string;
  providerCode?: string;
  formKey?: string;
  formSource?: 'FORM_KEY' | 'BILLER_PRODUCT' | 'RECAP' | string;
  billerScreen?: string;
  operation?: string;
  /** Requis si kind = CAPABILITY. */
  capability?: string;
}

export interface CatalogProductCasting {
  productCode: string;
  versionId: string;
  version: string;
  workflowKey?: string | null;
  roles: CatalogProductRole[];
  participants: CatalogProductParticipant[];
  bindings: CatalogProductBinding[];
  /** Journey produit (ex. STANDARD_CASH_OUT). */
  journeyCode?: string | null;
  /** Canaux d'accès autorisés (MOBILE, GAB, USSD, BACKOFFICE). */
  supportedChannels?: string[] | null;
  defaultChannel?: string | null;
  /** Audiences cibles (RETAIL, CORPORATE, AGENT, MERCHANT, BACKOFFICE). */
  supportedAudiences?: string[] | null;
  defaultAudience?: string | null;
}

export interface CatalogDeServiceVersion {
  id: string;
  catalogDeServiceId: string;
  catalogDeServiceCode: string;
  version: string;
  status: CatalogServiceStatus;
  effectiveFrom?: string | null;
  effectiveTo?: string | null;
  workflowKey?: string | null;
  configuration?: Record<string, unknown> | null;
  /** Libellés traduits de `configuration`, par chemin (`configuration.roles[SOURCE].label`). */
  translations?: ConfigurationTranslations | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CatalogWorkflowBinding {
  workflowKey: string;
  processName?: string | null;
  processVersion?: number | null;
  deploymentId?: string | null;
  productCode: string;
  catalogVersion: string;
  deployed: boolean;
}

export interface BpmProcessOption {
  key: string;
  label: string;
  name?: string;
  resourceName?: string;
}

export interface CatalogDeServiceRequest {
  code: string;
  /** Libellé dans la langue par défaut du gateway. */
  name: string;
  description?: string;
  nameI18n?: LocalizedText;
  descriptionI18n?: LocalizedText;
  categoryCode?: string;
  categoryId?: string;
  status: CatalogServiceStatus;
  initialVersion?: string;
  workflowKey?: string;
  configuration?: Record<string, unknown>;
  translations?: ConfigurationTranslations;
}

export interface CatalogDeServiceVersionRequest {
  version: string;
  status: CatalogServiceStatus;
  effectiveFrom?: string;
  effectiveTo?: string;
  workflowKey?: string;
  configuration?: Record<string, unknown>;
  translations?: ConfigurationTranslations;
}

@Injectable({ providedIn: 'root' })
export class CatalogDeServiceAdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/catalog-services`;
  private readonly categoriesBase = `${environment.apiUrl}/admin/catalog-categories`;

  listServices(): Observable<CatalogDeService[]> {
    return this.http.get<CatalogDeService[]>(this.base);
  }

  listPublishedServices(audience?: string | null): Observable<CatalogDeService[]> {
    const params = audience?.trim()
      ? `?audience=${encodeURIComponent(audience.trim())}`
      : '';
    return this.http.get<CatalogDeService[]>(`${this.base}/published${params}`);
  }

  getPublishedCasting(productCode: string): Observable<CatalogProductCasting> {
    return this.http.get<CatalogProductCasting>(`${this.base}/published/${encodeURIComponent(productCode)}/casting`);
  }

  getService(id: string): Observable<CatalogDeService> {
    return this.http.get<CatalogDeService>(`${this.base}/${id}`);
  }

  createService(body: CatalogDeServiceRequest): Observable<CatalogDeService> {
    return this.http.post<CatalogDeService>(this.base, body);
  }

  updateService(id: string, body: CatalogDeServiceRequest): Observable<CatalogDeService> {
    return this.http.put<CatalogDeService>(`${this.base}/${id}`, body);
  }

  listVersions(serviceId: string): Observable<CatalogDeServiceVersion[]> {
    return this.http.get<CatalogDeServiceVersion[]>(`${this.base}/${serviceId}/versions`);
  }

  getVersion(versionId: string): Observable<CatalogDeServiceVersion> {
    return this.http.get<CatalogDeServiceVersion>(`${this.base}/versions/${versionId}`);
  }

  createVersion(serviceId: string, body: CatalogDeServiceVersionRequest): Observable<CatalogDeServiceVersion> {
    return this.http.post<CatalogDeServiceVersion>(`${this.base}/${serviceId}/versions`, body);
  }

  updateDraftConfiguration(
    versionId: string,
    configuration: Record<string, unknown>,
    translations?: ConfigurationTranslations
  ): Observable<CatalogDeServiceVersion> {
    return this.http.put<CatalogDeServiceVersion>(`${this.base}/versions/${versionId}/configuration`, {
      configuration,
      translations
    });
  }

  publishVersion(versionId: string): Observable<CatalogDeServiceVersion> {
    return this.http.post<CatalogDeServiceVersion>(`${this.base}/versions/${versionId}/publish`, {});
  }

  resolveWorkflow(versionId: string): Observable<CatalogWorkflowBinding> {
    return this.http.get<CatalogWorkflowBinding>(`${this.base}/versions/${versionId}/workflow`);
  }

  listActiveCategories(): Observable<CategorieService[]> {
    return this.http.get<CategorieService[]>(`${this.categoriesBase}/active`);
  }

  /**
   * Processes déployés via RapidBPMN (catalogue RF / Flowable).
   */
  listBpmProcesses(): Observable<BpmProcessOption[]> {
    return this.http.get<unknown>(`${environment.apiUrl}/bpm/catalog/processes`).pipe(
      map(raw => this.normalizeProcesses(raw)),
      catchError(() => of([]))
    );
  }

  private normalizeProcesses(raw: unknown): BpmProcessOption[] {
    const list = Array.isArray(raw)
      ? raw
      : raw && typeof raw === 'object' && Array.isArray((raw as { content?: unknown }).content)
        ? (raw as { content: unknown[] }).content
        : [];
    const out: BpmProcessOption[] = [];
    for (const item of list) {
      if (!item || typeof item !== 'object') {
        continue;
      }
      const o = item as Record<string, unknown>;
      const key = String(
        o['key'] ?? o['processKey'] ?? o['processDefinitionKey'] ?? o['id'] ?? o['name'] ?? ''
      ).trim();
      if (!key) {
        continue;
      }
      const name = o['title'] != null ? String(o['title'])
        : o['name'] != null ? String(o['name'])
          : key;
      const resourceName = o['resourceName'] != null ? String(o['resourceName'])
        : o['fileName'] != null ? String(o['fileName'])
          : undefined;
      out.push({
        key,
        name,
        resourceName,
        label: name !== key ? `${name} (${key})` : key
      });
    }
    return out;
  }
}
