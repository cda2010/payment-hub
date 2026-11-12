import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthenticationConfig,
  AuthenticationRequest,
  ConnectivityTestResponse,
  Environment,
  EnvironmentRequest,
  Flow,
  FlowRequest,
  FlowStepRequest,
  FseApi,
  FseApiRequest,
  Operation,
  OperationRequest,
  OtpConfig,
  OtpConfigRequest,
  Page,
  Provider,
  ProviderApi,
  ProviderApiRequest,
  ProviderConfigExport,
  ProviderConfigImportRequest,
  ProviderRequest,
  ProviderTemplate,
  CreateProviderFromTemplateRequest,
  ServiceType,
  ServiceTypeCode,
  StatusMapping,
  StatusMappingRequest,
  WebhookConfig,
  WebhookConfigRequest
} from '../model/api.models';

export interface PaysRef {
  id: string;
  code: string;
  nom: string;
  indicatif: string;
  devise: string;
  actif: boolean;
  ordre: number;
}

export interface PaysRequest {
  code: string;
  nom: string;
  indicatif: string;
  devise: string;
  actif: boolean;
  ordre: number;
}

@Injectable({ providedIn: 'root' })
export class ProviderService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/providers`;

  /** Liste paginée (admin table). */
  listPage(page = 0, size = 20, q?: string | null): Observable<Page<Provider>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'ordreAffichage,asc');
    const query = q?.trim();
    if (query) {
      params = params.set('q', query);
    }
    return this.http.get<Page<Provider>>(this.base, { params });
  }

  /**
   * Liste complète pour dropdowns / écrans secondaires.
   * Agrège les pages côté client (cap 500).
   */
  list(): Observable<Provider[]> {
    return this.listPage(0, 500).pipe(map((page) => page.content ?? []));
  }

  listActive(serviceType?: ServiceTypeCode): Observable<Provider[]> {
    let params = new HttpParams();
    if (serviceType) {
      params = params.set('serviceType', serviceType);
    }
    return this.http.get<Provider[]>(`${this.base}/active`, { params });
  }

  listServiceTypes(): Observable<ServiceType[]> {
    return this.http.get<ServiceType[]>(`${environment.apiUrl}/service-types`);
  }

  /** Référentiel pays — le nom est celui de l'omnicanal. */
  listPays(): Observable<PaysRef[]> {
    return this.http.get<PaysRef[]>(`${environment.apiUrl}/pays`);
  }

  createPays(body: PaysRequest): Observable<PaysRef> {
    return this.http.post<PaysRef>(`${environment.apiUrl}/pays`, body);
  }

  updatePays(id: string, body: PaysRequest): Observable<PaysRef> {
    return this.http.put<PaysRef>(`${environment.apiUrl}/pays/${id}`, body);
  }

  get(id: string): Observable<Provider> {
    return this.http.get<Provider>(`${this.base}/${id}`);
  }

  create(body: ProviderRequest): Observable<Provider> {
    return this.http.post<Provider>(this.base, body);
  }

  update(id: string, body: ProviderRequest): Observable<Provider> {
    return this.http.put<Provider>(`${this.base}/${id}`, body);
  }

  setEnabled(id: string, enabled: boolean): Observable<Provider> {
    const params = new HttpParams().set('enabled', String(enabled));
    return this.http.patch<Provider>(`${this.base}/${id}/enabled`, null, { params });
  }

  test(id: string): Observable<ConnectivityTestResponse> {
    return this.http.post<ConnectivityTestResponse>(`${this.base}/${id}/test`, null);
  }

  listTemplates(): Observable<ProviderTemplate[]> {
    return this.http.get<ProviderTemplate[]>(`${this.base}/templates`);
  }

  createFromTemplate(body: CreateProviderFromTemplateRequest): Observable<Provider> {
    return this.http.post<Provider>(`${this.base}/from-template`, body);
  }

  exportConfig(id: string): Observable<ProviderConfigExport> {
    return this.http.get<ProviderConfigExport>(`${this.base}/${id}/export`);
  }

  importConfig(body: ProviderConfigImportRequest): Observable<Provider> {
    return this.http.post<Provider>(`${this.base}/import`, body);
  }

  seedOmnicanalConnectivity(id: string): Observable<{
    providerCode: string;
    environmentCode: string;
    operationsUpserted: number;
    apisUpserted: number;
    operationCodes: string[];
  }> {
    return this.http.post<{
      providerCode: string;
      environmentCode: string;
      operationsUpserted: number;
      apisUpserted: number;
      operationCodes: string[];
    }>(`${this.base}/${id}/omnicanal-connectivity/seed`, {});
  }

  listOmnicanalTypeMethodes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/omnicanal-type-methodes`);
  }

  createOmnicanalMethod(
    providerId: string,
    body: {
      fseApiId: string;
      typeMethode: string;
      codeMethode?: string | null;
      nomMethode: string;
      typeAppel: string;
      typeReponse?: string | null;
      actif: boolean;
      idempotent: boolean;
      parametres: Array<{
        codeParametre: string;
        nomExterne: string;
        typeParametre?: string | null;
        categorie?: string | null;
      }>;
    }
  ): Observable<{
    operationId: string;
    operationCode: string;
    endpointId: string;
    path: string;
    httpMethod: string;
    fseApiId: string;
    fseApiCode: string;
    environmentCode: string | null;
    requestMappingJson: string | null;
  }> {
    return this.http.post<{
      operationId: string;
      operationCode: string;
      endpointId: string;
      path: string;
      httpMethod: string;
      fseApiId: string;
      fseApiCode: string;
      environmentCode: string | null;
      requestMappingJson: string | null;
    }>(`${this.base}/${providerId}/methods`, body);
  }

  listFseApis(providerId: string): Observable<FseApi[]> {
    return this.http.get<FseApi[]>(`${this.base}/${providerId}/fse-apis`);
  }

  createFseApi(providerId: string, body: FseApiRequest): Observable<FseApi> {
    return this.http.post<FseApi>(`${this.base}/${providerId}/fse-apis`, body);
  }

  updateFseApi(providerId: string, fseApiId: string, body: FseApiRequest): Observable<FseApi> {
    return this.http.put<FseApi>(`${this.base}/${providerId}/fse-apis/${fseApiId}`, body);
  }

  // Environments
  listEnvironments(providerId: string): Observable<Environment[]> {
    return this.http.get<Environment[]>(`${this.base}/${providerId}/environments`);
  }

  createEnvironment(providerId: string, body: EnvironmentRequest): Observable<Environment> {
    return this.http.post<Environment>(`${this.base}/${providerId}/environments`, body);
  }

  updateEnvironment(providerId: string, envId: string, body: EnvironmentRequest): Observable<Environment> {
    return this.http.put<Environment>(`${this.base}/${providerId}/environments/${envId}`, body);
  }

  // Operations
  listOperations(providerId: string): Observable<Operation[]> {
    return this.http.get<Operation[]>(`${this.base}/${providerId}/operations`);
  }

  createOperation(providerId: string, body: OperationRequest): Observable<Operation> {
    return this.http.post<Operation>(`${this.base}/${providerId}/operations`, body);
  }

  updateOperation(providerId: string, opId: string, body: OperationRequest): Observable<Operation> {
    return this.http.put<Operation>(`${this.base}/${providerId}/operations/${opId}`, body);
  }

  // APIs
  listApis(providerId: string): Observable<ProviderApi[]> {
    return this.http.get<ProviderApi[]>(`${this.base}/${providerId}/apis`);
  }

  createApi(providerId: string, body: ProviderApiRequest): Observable<ProviderApi> {
    return this.http.post<ProviderApi>(`${this.base}/${providerId}/apis`, body);
  }

  updateApi(providerId: string, apiId: string, body: ProviderApiRequest): Observable<ProviderApi> {
    return this.http.put<ProviderApi>(`${this.base}/${providerId}/apis/${apiId}`, body);
  }

  // Authentication
  getAuthentication(providerId: string, environmentId: string): Observable<AuthenticationConfig> {
    const params = new HttpParams().set('environmentId', environmentId);
    return this.http.get<AuthenticationConfig>(`${this.base}/${providerId}/authentication`, { params });
  }

  upsertAuthentication(providerId: string, body: AuthenticationRequest): Observable<AuthenticationConfig> {
    return this.http.put<AuthenticationConfig>(`${this.base}/${providerId}/authentication`, body);
  }

  // Flows
  listFlows(providerId: string): Observable<Flow[]> {
    return this.http.get<Flow[]>(`${this.base}/${providerId}/flows`);
  }

  createFlow(providerId: string, body: FlowRequest): Observable<Flow> {
    return this.http.post<Flow>(`${this.base}/${providerId}/flows`, body);
  }

  getFlow(flowId: string): Observable<Flow> {
    return this.http.get<Flow>(`${environment.apiUrl}/flows/${flowId}`);
  }

  replaceSteps(flowId: string, steps: FlowStepRequest[]): Observable<Flow> {
    return this.http.put<Flow>(`${environment.apiUrl}/flows/${flowId}/steps`, steps);
  }

  // Status mappings
  listStatusMappings(providerId: string): Observable<StatusMapping[]> {
    return this.http.get<StatusMapping[]>(`${this.base}/${providerId}/status-mappings`);
  }

  createStatusMapping(providerId: string, body: StatusMappingRequest): Observable<StatusMapping> {
    return this.http.post<StatusMapping>(`${this.base}/${providerId}/status-mappings`, body);
  }

  updateStatusMapping(
    providerId: string,
    mappingId: string,
    body: StatusMappingRequest
  ): Observable<StatusMapping> {
    return this.http.put<StatusMapping>(`${this.base}/${providerId}/status-mappings/${mappingId}`, body);
  }

  // OTP config
  getOtpConfig(providerId: string): Observable<OtpConfig> {
    return this.http.get<OtpConfig>(`${this.base}/${providerId}/otp-config`);
  }

  upsertOtpConfig(providerId: string, body: OtpConfigRequest): Observable<OtpConfig> {
    return this.http.put<OtpConfig>(`${this.base}/${providerId}/otp-config`, body);
  }

  // Webhooks
  listWebhooks(providerId: string): Observable<WebhookConfig[]> {
    return this.http.get<WebhookConfig[]>(`${this.base}/${providerId}/webhooks`);
  }

  upsertWebhook(providerId: string, body: WebhookConfigRequest): Observable<WebhookConfig> {
    return this.http.put<WebhookConfig>(`${this.base}/${providerId}/webhooks`, body);
  }
}
