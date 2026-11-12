import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface OpsMetricsSummary {
  channel: Array<{ tags: Record<string, string>; count: number }>;
  providerCalls: Array<{ tags: Record<string, string>; count: number }>;
  businessOperations: Array<{ tags: Record<string, string>; count: number }>;
  circuits: Array<{ tags: Record<string, string>; stateOrder: number }>;
  classifier: Record<string, string>;
  executionsToday: Array<{
    providerCode: string;
    operationCode: string;
    successCount: number;
    failureCount: number;
    avgDurationMs: number;
  }>;
}

export interface CatalogParticipant {
  participantCode: string;
  fseCode: string;
  providerCode: string;
  name: string;
  type: string;
  enabled: boolean;
  capabilities: Array<{
    capabilityCode: string;
    label: string;
    category: string;
    enabled: boolean;
    operations: Array<{ operationCode: string; nom: string }>;
  }>;
}

@Injectable({ providedIn: 'root' })
export class OpsService {
  private readonly http = inject(HttpClient);

  metricsSummary(): Observable<OpsMetricsSummary> {
    return this.http.get<OpsMetricsSummary>(`${environment.apiUrl}/admin/metrics/summary`);
  }

  listParticipants(): Observable<CatalogParticipant[]> {
    return this.http.get<CatalogParticipant[]>(`${environment.apiUrl}/catalog/participants`);
  }

  getParticipant(code: string): Observable<CatalogParticipant> {
    return this.http.get<CatalogParticipant>(`${environment.apiUrl}/catalog/participants/${code}`);
  }
}
