import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BpmnDiagramState } from '@rapidframe/bpmn-designer';
import { environment } from '../../../environments/environment';
import {
  CreatePaymentRequest,
  DailyStats,
  Page,
  Payment,
  PaymentEvent,
  TransactionSearchParams
} from '../model/api.models';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/payments`;
  private readonly adminBase = `${environment.apiUrl}/admin/transactions`;

  createPayment(body: CreatePaymentRequest, idempotencyKey?: string): Observable<Payment> {
    let headers = new HttpHeaders();
    if (idempotencyKey) {
      headers = headers.set('Idempotency-Key', idempotencyKey);
    }
    return this.http.post<Payment>(this.base, body, { headers });
  }

  getPayment(id: string): Observable<Payment> {
    return this.http.get<Payment>(`${this.base}/${id}`);
  }

  getStatus(id: string): Observable<Payment> {
    return this.http.get<Payment>(`${this.base}/${id}/status`);
  }

  submitAction(id: string, action: string, data?: Record<string, unknown>): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/${id}/actions`, { action, data: data ?? {} });
  }

  cancel(id: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/${id}/cancel`, null);
  }

  retry(id: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/${id}/retry`, null);
  }

  events(id: string): Observable<PaymentEvent[]> {
    return this.http.get<PaymentEvent[]>(`${this.base}/${id}/events`);
  }

  /** État Flowable (XML + surbrillance) pour le viewer BPMN du détail transaction. */
  diagramState(id: string): Observable<BpmnDiagramState> {
    return this.http.get<BpmnDiagramState>(`${this.base}/${encodeURIComponent(id)}/bpm/diagram-state`);
  }

  adminSearch(params: TransactionSearchParams): Observable<Page<Payment>> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<Page<Payment>>(this.adminBase, { params: httpParams });
  }

  dailyStats(): Observable<DailyStats> {
    return this.http.get<DailyStats>(`${this.adminBase}/stats/daily`);
  }
}
