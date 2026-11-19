import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Document renvoyé par payment-documents. */
export interface DocumentEnregistre {
  id: string;
  code: string;
  ownerKind: string;
  ownerId: string;
  originalName: string;
  contentType: string;
  sizeBytes: number;
  status: string;
  createdAt: string;
}

/** Lecture du coffre documentaire. La fiche client n'a pas de copie locale. */
@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.documentsApiUrl;

  listerPersonne(partyId: string): Observable<DocumentEnregistre[]> {
    const params = new HttpParams().set('ownerKind', 'PARTY').set('ownerId', partyId);
    return this.http.get<DocumentEnregistre[]>(this.base, { params });
  }

  fichier(id: string): Observable<Blob> {
    return this.http.get(`${this.base}/${id}/file`, { responseType: 'blob' });
  }

  statut(id: string, status: 'VERIFIED' | 'REJECTED'): Observable<DocumentEnregistre> {
    return this.http.patch<DocumentEnregistre>(`${this.base}/${id}/status`, { status });
  }
}
