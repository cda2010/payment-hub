import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ApprovalProfileDto {
  id: string;
  code: string;
  name: string;
  description?: string | null;
  definitionJson: string;
  actif: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ApprovalProfileRequest {
  code: string;
  name: string;
  description?: string | null;
  definitionJson: string;
  actif?: boolean;
}

export interface ApprovalSimulateRequest {
  amount?: number;
  currency?: string;
  catalogServiceCode?: string;
  rail?: string;
}

export interface ApprovalSimulateResponse {
  profileCode: string;
  strategy: string;
  levelCount: number;
  levels: Array<{ level: number; requiredApprovals: number; assignments: string[] }>;
  source: string;
}

@Injectable({ providedIn: 'root' })
export class ApprovalAdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/approvals`;

  listProfiles(): Observable<ApprovalProfileDto[]> {
    return this.http.get<ApprovalProfileDto[]>(`${this.base}/profiles`);
  }

  getProfile(id: string): Observable<ApprovalProfileDto> {
    return this.http.get<ApprovalProfileDto>(`${this.base}/profiles/${id}`);
  }

  createProfile(body: ApprovalProfileRequest): Observable<ApprovalProfileDto> {
    return this.http.post<ApprovalProfileDto>(`${this.base}/profiles`, body);
  }

  updateProfile(id: string, body: ApprovalProfileRequest): Observable<ApprovalProfileDto> {
    return this.http.put<ApprovalProfileDto>(`${this.base}/profiles/${id}`, body);
  }

  simulate(body: ApprovalSimulateRequest): Observable<ApprovalSimulateResponse> {
    return this.http.post<ApprovalSimulateResponse>(`${this.base}/simulate`, body);
  }
}
