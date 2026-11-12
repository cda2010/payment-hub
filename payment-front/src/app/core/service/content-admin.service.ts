import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ContentLocaleDto {
  locale: string;
  eyebrow?: string | null;
  title: string;
  body?: string | null;
  cta?: string | null;
}

export interface ContentItemDto {
  id: string;
  code: string;
  slot: string;
  enabled: boolean;
  priority: number;
  pluginId?: string | null;
  route?: string | null;
  icon?: string | null;
  tone?: string | null;
  imageUrl?: string | null;
  imageAlt?: string | null;
  startsAt?: string | null;
  endsAt?: string | null;
  /** AccessChannel[] — vide = tous. */
  channels?: string[];
  /** ProductAudience[] — vide = toutes. */
  audiences?: string[];
  locales: ContentLocaleDto[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ContentUpsertRequest {
  code: string;
  slot: string;
  enabled: boolean;
  priority: number;
  pluginId?: string | null;
  route?: string | null;
  icon?: string | null;
  tone?: string | null;
  imageUrl?: string | null;
  imageAlt?: string | null;
  startsAt?: string | null;
  endsAt?: string | null;
  channels?: string[];
  audiences?: string[];
  locales: ContentLocaleDto[];
}

export interface UploadedMedia {
  fileName: string;
  url: string;
  contentType?: string;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class ContentAdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/content`;

  list(slot?: string): Observable<ContentItemDto[]> {
    const q = slot ? `?slot=${encodeURIComponent(slot)}` : '';
    return this.http.get<ContentItemDto[]>(`${this.base}${q}`);
  }

  get(id: string): Observable<ContentItemDto> {
    return this.http.get<ContentItemDto>(`${this.base}/${id}`);
  }

  create(body: ContentUpsertRequest): Observable<ContentItemDto> {
    return this.http.post<ContentItemDto>(this.base, body);
  }

  update(id: string, body: ContentUpsertRequest): Observable<ContentItemDto> {
    return this.http.put<ContentItemDto>(`${this.base}/${id}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  uploadMedia(file: File): Observable<UploadedMedia> {
    const form = new FormData();
    form.append('file', file, file.name);
    return this.http.post<UploadedMedia>(`${this.base}/media`, form);
  }
}
