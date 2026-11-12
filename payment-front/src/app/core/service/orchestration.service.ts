import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  InterfaceUtilisateur,
  InterfaceUtilisateurRequest,
  InterfaceTemplatePreview,
  LigneSchemaEcriture,
  LigneSchemaEcritureRequest,
  OrchestrationCatalog,
  Page,
  ParametreSchema,
  ParametreSchemaRequest,
  ParametreUtilisateur,
  ParametreUtilisateurRequest,
  CompteFse,
  CompteFseRequest,
  RelationFseCorridor,
  RelationFseCorridorRequest,
  SchemaEcriture,
  TypeCompte,
  SchemaEcritureRequest,
  SchemaExecuteRequest,
  SchemaExecuteResponse,
  SchemaMatchResult,
  ServiceTypeCode
} from '../model/api.models';

@Injectable({ providedIn: 'root' })
export class OrchestrationService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/orchestration`;

  listSchemas(serviceType?: ServiceTypeCode): Observable<SchemaEcriture[]> {
    let params = new HttpParams();
    if (serviceType) {
      params = params.set('serviceType', serviceType);
    }
    return this.http.get<SchemaEcriture[]>(`${this.base}/schema-ecritures`, { params });
  }

  getSchema(id: string): Observable<SchemaEcriture> {
    return this.http.get<SchemaEcriture>(`${this.base}/schema-ecritures/${id}`);
  }

  createSchema(body: SchemaEcritureRequest): Observable<SchemaEcriture> {
    return this.http.post<SchemaEcriture>(`${this.base}/schema-ecritures`, body);
  }

  updateSchema(id: string, body: SchemaEcritureRequest): Observable<SchemaEcriture> {
    return this.http.put<SchemaEcriture>(`${this.base}/schema-ecritures/${id}`, body);
  }

  deleteSchema(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/schema-ecritures/${id}`);
  }

  publishSchema(id: string): Observable<SchemaEcriture> {
    return this.http.post<SchemaEcriture>(`${this.base}/schema-ecritures/${id}/publish`, {});
  }

  unpublishSchema(id: string): Observable<SchemaEcriture> {
    return this.http.post<SchemaEcriture>(`${this.base}/schema-ecritures/${id}/unpublish`, {});
  }

  listLignes(schemaId: string): Observable<LigneSchemaEcriture[]> {
    return this.http.get<LigneSchemaEcriture[]>(`${this.base}/schema-ecritures/${schemaId}/lignes`);
  }

  getLigne(id: string): Observable<LigneSchemaEcriture> {
    return this.http.get<LigneSchemaEcriture>(`${this.base}/ligne-schema-ecritures/${id}`);
  }

  addLigne(body: LigneSchemaEcritureRequest): Observable<LigneSchemaEcriture> {
    return this.http.post<LigneSchemaEcriture>(`${this.base}/ligne-schema-ecritures`, body);
  }

  updateLigne(id: string, body: LigneSchemaEcritureRequest): Observable<LigneSchemaEcriture> {
    return this.http.put<LigneSchemaEcriture>(`${this.base}/ligne-schema-ecritures/${id}`, body);
  }

  deleteLigne(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/ligne-schema-ecritures/${id}`);
  }

  listParametres(ligneId: string): Observable<ParametreSchema[]> {
    return this.http.get<ParametreSchema[]>(`${this.base}/parametre-schemas/ligne/${ligneId}`);
  }

  addParametre(body: ParametreSchemaRequest): Observable<ParametreSchema> {
    return this.http.post<ParametreSchema>(`${this.base}/parametre-schemas`, body);
  }

  updateParametre(id: string, body: ParametreSchemaRequest): Observable<ParametreSchema> {
    return this.http.put<ParametreSchema>(`${this.base}/parametre-schemas/${id}`, body);
  }

  deleteParametre(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/parametre-schemas/${id}`);
  }

  listPatternCodes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/pattern-codes`);
  }

  catalog(): Observable<OrchestrationCatalog> {
    return this.http.get<OrchestrationCatalog>(`${this.base}/catalog`);
  }

  matchSchema(body: {
    serviceType: ServiceTypeCode;
    sourceCodeFse: string;
    destCodeFse: string;
    siege?: number;
  }): Observable<SchemaMatchResult> {
    return this.http.post<SchemaMatchResult>(`${this.base}/match-schema`, body);
  }

  executeSchema(body: SchemaExecuteRequest): Observable<SchemaExecuteResponse> {
    return this.http.post<SchemaExecuteResponse>(`${this.base}/execute-schema`, body);
  }

  listInterfaces(providerId: string): Observable<InterfaceUtilisateur[]> {
    return this.listInterfacesPage(providerId, 0, 1000).pipe(
      map((page) => page.content ?? [])
    );
  }

  listInterfacesPage(
    providerId: string,
    page = 0,
    size = 10
  ): Observable<Page<InterfaceUtilisateur>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'typeMethode,asc');
    return this.http
      .get<Page<InterfaceUtilisateur> | InterfaceUtilisateur[]>(
        `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs`,
        { params }
      )
      .pipe(map((res) => this.normalizeInterfacePage(res, page, size)));
  }

  getInterface(providerId: string, interfaceId: string): Observable<InterfaceUtilisateur> {
    return this.http.get<InterfaceUtilisateur>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}`
    );
  }

  /** Accepte une Page Spring ou un tableau legacy (backend non redémarré). */
  private normalizeInterfacePage(
    res: Page<InterfaceUtilisateur> | InterfaceUtilisateur[],
    page: number,
    size: number
  ): Page<InterfaceUtilisateur> {
    if (Array.isArray(res)) {
      const start = page * size;
      const slice = res.slice(start, start + size);
      return {
        content: slice,
        totalElements: res.length,
        totalPages: Math.max(1, Math.ceil(res.length / size)),
        size,
        number: page
      };
    }
    return {
      content: res.content ?? [],
      totalElements: res.totalElements ?? res.content?.length ?? 0,
      totalPages: res.totalPages ?? 1,
      size: res.size ?? size,
      number: res.number ?? page
    };
  }

  createInterface(providerId: string, body: InterfaceUtilisateurRequest): Observable<InterfaceUtilisateur> {
    return this.http.post<InterfaceUtilisateur>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs`,
      body
    );
  }

  updateInterface(
    providerId: string,
    interfaceId: string,
    body: InterfaceUtilisateurRequest
  ): Observable<InterfaceUtilisateur> {
    return this.http.put<InterfaceUtilisateur>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}`,
      body
    );
  }

  deleteInterface(providerId: string, interfaceId: string): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}`
    );
  }

  publishInterface(providerId: string, interfaceId: string): Observable<InterfaceUtilisateur> {
    return this.http.post<InterfaceUtilisateur>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}/publish`,
      {}
    );
  }

  unpublishInterface(providerId: string, interfaceId: string): Observable<InterfaceUtilisateur> {
    return this.http.post<InterfaceUtilisateur>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}/unpublish`,
      {}
    );
  }

  previewInterfaceTemplate(providerId: string, typeMethode: string): Observable<InterfaceTemplatePreview> {
    const params = new HttpParams().set('typeMethode', typeMethode);
    return this.http.get<InterfaceTemplatePreview>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/templates`,
      { params }
    );
  }

  seedInterfaceDefaultParams(providerId: string, interfaceId: string): Observable<InterfaceUtilisateur> {
    return this.http.post<InterfaceUtilisateur>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}/parametres/seed-defaults`,
      {}
    );
  }

  /** Génère une InterfaceUtilisateur REQUEST depuis une méthode (bloque si déjà existante). */
  generateInterfaceFromApi(providerId: string, providerApiId: string): Observable<InterfaceUtilisateur> {
    return this.http.post<InterfaceUtilisateur>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/from-api/${providerApiId}`,
      {}
    );
  }

  listInterfaceParametres(providerId: string, interfaceId: string): Observable<ParametreUtilisateur[]> {
    return this.http.get<ParametreUtilisateur[]>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}/parametres`
    );
  }

  addInterfaceParametre(
    providerId: string,
    interfaceId: string,
    body: ParametreUtilisateurRequest
  ): Observable<ParametreUtilisateur> {
    return this.http.post<ParametreUtilisateur>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}/parametres`,
      body
    );
  }

  updateInterfaceParametre(
    providerId: string,
    interfaceId: string,
    paramId: string,
    body: ParametreUtilisateurRequest
  ): Observable<ParametreUtilisateur> {
    return this.http.put<ParametreUtilisateur>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}/parametres/${paramId}`,
      body
    );
  }

  deleteInterfaceParametre(providerId: string, interfaceId: string, paramId: string): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/${interfaceId}/parametres/${paramId}`
    );
  }

  seedInterfaces(providerId: string): Observable<{
    providerId: string;
    interfacesBefore: number;
    typeMethodesUpserted: string[];
  }> {
    return this.http.post<{
      providerId: string;
      interfacesBefore: number;
      typeMethodesUpserted: string[];
    }>(`${environment.apiUrl}/providers/${providerId}/interface-utilisateurs/seed`, {});
  }

  listCorridorsFse(): Observable<RelationFseCorridor[]> {
    return this.http.get<RelationFseCorridor[]>(`${this.base}/corridors-fse`);
  }

  getCorridorFse(id: string): Observable<RelationFseCorridor> {
    return this.http.get<RelationFseCorridor>(`${this.base}/corridors-fse/${id}`);
  }

  createCorridorFse(body: RelationFseCorridorRequest): Observable<RelationFseCorridor[]> {
    return this.http.post<RelationFseCorridor[]>(`${this.base}/corridors-fse`, body);
  }

  updateCorridorFse(id: string, body: RelationFseCorridorRequest): Observable<RelationFseCorridor> {
    return this.http.put<RelationFseCorridor>(`${this.base}/corridors-fse/${id}`, body);
  }

  deleteCorridorFse(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/corridors-fse/${id}`);
  }

  listComptesFse(): Observable<CompteFse[]> {
    return this.http.get<CompteFse[]>(`${this.base}/comptes-fse`);
  }

  getCompteFse(id: string): Observable<CompteFse> {
    return this.http.get<CompteFse>(`${this.base}/comptes-fse/${id}`);
  }

  listTypesCompte(): Observable<TypeCompte[]> {
    return this.http.get<TypeCompte[]>(`${this.base}/comptes-fse/types`);
  }

  createCompteFse(body: CompteFseRequest): Observable<CompteFse> {
    return this.http.post<CompteFse>(`${this.base}/comptes-fse`, body);
  }

  updateCompteFse(id: string, body: CompteFseRequest): Observable<CompteFse> {
    return this.http.put<CompteFse>(`${this.base}/comptes-fse/${id}`, body);
  }

  deleteCompteFse(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/comptes-fse/${id}`);
  }
}
