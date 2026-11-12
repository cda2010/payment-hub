import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type CmsFieldType = 'string' | 'text' | 'number' | 'boolean' | 'enum' | 'string[]';

export interface CmsSchemaField {
  key: string;
  type: CmsFieldType;
  label: string;
  values?: string[];
}

export interface CmsCollectionLocale {
  locale: string;
  title: string;
  description?: string | null;
}

export interface CmsDocumentLocale {
  locale: string;
  title: string;
  summary?: string | null;
  body?: string | null;
}

export interface CmsCollectionDto {
  id: string;
  code: string;
  kind: string;
  enabled: boolean;
  priority: number;
  icon?: string | null;
  channels?: string[];
  audiences?: string[];
  schema?: { fields?: CmsSchemaField[] } | Record<string, unknown>;
  locales: CmsCollectionLocale[];
  updatedAt?: string;
}

export interface CmsDocumentDto {
  id: string;
  code: string;
  collectionCode: string;
  enabled: boolean;
  priority: number;
  route?: string | null;
  icon?: string | null;
  imageUrl?: string | null;
  imageAlt?: string | null;
  attrs?: Record<string, unknown>;
  channels?: string[];
  audiences?: string[];
  startsAt?: string | null;
  endsAt?: string | null;
  locales: CmsDocumentLocale[];
  updatedAt?: string;
}

export interface CmsCollectionUpsert {
  code: string;
  kind: string;
  enabled: boolean;
  priority: number;
  icon?: string | null;
  channels?: string[];
  audiences?: string[];
  schema?: { fields: CmsSchemaField[] };
  locales: CmsCollectionLocale[];
}

export interface CmsDocumentUpsert {
  code: string;
  collectionCode: string;
  enabled: boolean;
  priority: number;
  route?: string | null;
  icon?: string | null;
  imageUrl?: string | null;
  imageAlt?: string | null;
  attrs?: Record<string, unknown>;
  channels?: string[];
  audiences?: string[];
  startsAt?: string | null;
  endsAt?: string | null;
  locales: CmsDocumentLocale[];
}

export function schemaFieldsOf(col: CmsCollectionDto | null | undefined): CmsSchemaField[] {
  const raw = col?.schema as { fields?: CmsSchemaField[] } | undefined;
  return Array.isArray(raw?.fields) ? raw!.fields! : [];
}

@Injectable({ providedIn: 'root' })
export class CmsAdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/cms`;

  listCollections(): Observable<CmsCollectionDto[]> {
    return this.http.get<CmsCollectionDto[]>(`${this.base}/collections`);
  }

  createCollection(body: CmsCollectionUpsert): Observable<CmsCollectionDto> {
    return this.http.post<CmsCollectionDto>(`${this.base}/collections`, body);
  }

  updateCollection(id: string, body: CmsCollectionUpsert): Observable<CmsCollectionDto> {
    return this.http.put<CmsCollectionDto>(`${this.base}/collections/${id}`, body);
  }

  deleteCollection(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/collections/${id}`);
  }

  listDocuments(collectionCode: string): Observable<CmsDocumentDto[]> {
    return this.http.get<CmsDocumentDto[]>(
      `${this.base}/documents?collection=${encodeURIComponent(collectionCode)}`
    );
  }

  createDocument(body: CmsDocumentUpsert): Observable<CmsDocumentDto> {
    return this.http.post<CmsDocumentDto>(`${this.base}/documents`, body);
  }

  updateDocument(id: string, body: CmsDocumentUpsert): Observable<CmsDocumentDto> {
    return this.http.put<CmsDocumentDto>(`${this.base}/documents/${id}`, body);
  }

  deleteDocument(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/documents/${id}`);
  }
}
