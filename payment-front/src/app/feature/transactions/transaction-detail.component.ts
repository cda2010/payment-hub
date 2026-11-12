import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BpmnDiagramState, BpmnViewerHostComponent } from '@rapidframe/bpmn-designer';
import { PaymentService } from '../../core/service/payment.service';
import { Payment, PaymentEvent, PaymentStatus } from '../../core/model/api.models';
import {
  paymentStatusLabel,
  paymentStatusSeverity,
} from '../../core/util/payment-status.util';

@Component({
  selector: 'app-transaction-detail',
  standalone: true,
  imports: [DatePipe, RouterLink, ButtonModule, ProgressSpinnerModule, TagModule, BpmnViewerHostComponent],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <a routerLink="/admin/transactions" class="back-link">← Transactions</a>
          @if (payment(); as p) {
            <h1>Transaction {{ p.paymentId }}</h1>
          } @else {
            <h1>Transaction</h1>
          }
        </div>
      </div>

      @if (loading()) {
        <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" /></div>
      } @else if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      } @else {
        @if (payment(); as p) {
        <div class="grid">
          <div class="gw-panel">
            <h2>Résumé</h2>
            <dl>
              @if (p.title) {
                <dt>Opération</dt><dd class="op-title">{{ p.title }}</dd>
              }
              @if (p.subtitle) {
                <dt>Détail</dt><dd class="muted">{{ p.subtitle }}</dd>
              }
              <dt>Produit</dt><dd>{{ p.productLabel || p.catalogServiceCode || p.serviceType || '—' }}</dd>
              <dt>Fournisseur</dt><dd>{{ p.provider }}</dd>
              <dt>Montant</dt><dd>{{ p.amount }} {{ p.currency }}</dd>
              <dt>Statut</dt>
              <dd>
                <p-tag
                  [value]="statusLabel(p.status, p.statusLabel)"
                  [severity]="statusSeverity(p.status)"
                />
              </dd>
              @if (p.agentDisplay) {
                <dt>Agent / point de retrait</dt><dd class="op-title">{{ p.agentDisplay }}</dd>
              }
              @if (p.withdrawalChannel) {
                <dt>Canal de retrait</dt><dd>{{ p.withdrawalChannel }}</dd>
              }
              <dt>Étape</dt><dd>{{ p.currentStep || '—' }}</dd>
              <dt>Réf. client</dt><dd>{{ p.customerReference || '—' }}</dd>
              <dt>Réf. fournisseur</dt><dd>{{ p.providerReference || '—' }}</dd>
              <dt>Téléphone</dt><dd>{{ p.phoneNumber || '—' }}</dd>
              <dt>Créé</dt><dd>{{ p.createdAt | date:'medium' }}</dd>
              <dt>Mis à jour</dt><dd>{{ p.updatedAt | date:'medium' }}</dd>
              @if (p.errorMessage) {
                <dt>Erreur</dt><dd class="gw-msg error">{{ p.errorCode }} — {{ p.errorMessage }}</dd>
              }
            </dl>
            <div class="gw-actions">
              <button pButton type="button" label="Relancer" class="p-button-outlined" (click)="retry()"></button>
              <button pButton type="button" label="Annuler" class="p-button-danger p-button-text" (click)="cancel()"></button>
            </div>
          </div>

          <div class="gw-panel">
            <h2>Timeline</h2>
            @if (!events().length) {
              <p class="muted">Aucun événement</p>
            } @else {
              <ol class="timeline">
                @for (e of events(); track e.timestamp + e.eventType) {
                  <li>
                    <time>{{ e.timestamp | date:'medium' }}</time>
                    <strong>{{ e.eventType }}</strong>
                    <span>{{ e.message }}</span>
                  </li>
                }
              </ol>
            }
          </div>
        </div>

        <div class="gw-panel bpm-panel">
          <div class="bpm-header">
            <div>
              <h2>Parcours Flowable</h2>
              @if (diagramState(); as s) {
                <p class="bpm-sub">
                  {{ s.processDefinitionKey || 'process' }}
                  <span class="bpm-badge" [class.bpm-badge-ended]="s.ended">
                    {{ s.ended ? 'Terminé' : 'En cours' }}
                  </span>
                </p>
              } @else if (!diagramLoading() && !diagramError()) {
                <p class="bpm-sub">Aucun diagramme</p>
              }
            </div>
            <div class="bpm-actions">
              <button type="button" class="bpm-btn" (click)="zoomOut()" [disabled]="!viewerReady()" title="Zoom arrière">−</button>
              <button type="button" class="bpm-btn" (click)="zoomIn()" [disabled]="!viewerReady()" title="Zoom avant">+</button>
              <button type="button" class="bpm-btn" (click)="center()" [disabled]="!viewerReady()" title="Recentrer">Centrer</button>
              <button type="button" class="bpm-btn" (click)="fit()" [disabled]="!viewerReady()" title="Tout voir">Tout voir</button>
              <button type="button" class="bpm-btn bpm-btn-primary" (click)="reloadDiagram()">Actualiser</button>
            </div>
          </div>

          <div class="bpm-legend">
            <span class="bpm-dot bpm-dot-active"></span> Étape courante
            <span class="bpm-dot bpm-dot-completed"></span> Déjà exécutée
            <span class="bpm-hint">Molette = zoom · glisser = déplacer</span>
          </div>

          @if (diagramError()) {
            <p class="gw-msg error">{{ diagramError() }}</p>
          }
          @if (diagramLoading()) {
            <p class="muted">Chargement du parcours…</p>
          }

          @if (diagramState()?.xml; as xml) {
            <div class="bpm-canvas">
              <rf-bpmn-viewer-host
                #viewerHost
                [xml]="xml"
                [activeActivityIds]="diagramState()?.activeActivityIds || []"
                [completedActivityIds]="diagramState()?.completedActivityIds || []"
                [takenSequenceFlowIds]="diagramState()?.takenSequenceFlowIds || []"
                (importDone)="onViewerReady(viewerHost)"
                (importFailed)="onViewerFailed($event)"
              ></rf-bpmn-viewer-host>
            </div>
          }

          @if (diagramState(); as s) {
            <div class="bpm-meta">
              @if (s.currentUserTasks?.length) {
                <div>
                  <h3>Tâches actives</h3>
                  <ul>
                    @for (t of s.currentUserTasks; track t.taskId || t.activityId) {
                      <li>
                        {{ t.name || t.activityId }}
                        @if (t.formKey) {
                          <small> · {{ t.formKey }}</small>
                        }
                      </li>
                    }
                  </ul>
                </div>
              }
              @if (s.childInstances?.length) {
                <div>
                  <h3>CallActivities</h3>
                  <ul>
                    @for (c of s.childInstances; track c.processInstanceId) {
                      <li>{{ c.processDefinitionKey }} · {{ c.processInstanceId }}</li>
                    }
                  </ul>
                </div>
              }
            </div>
          }
        </div>

        @if (actionMsg()) {
          <p class="gw-msg">{{ actionMsg() }}</p>
        }
        }
      }
    </div>
  `,
  styles: `
    .back-link {
      display: inline-block;
      margin-bottom: 0.35rem;
      color: var(--primary-color, #2563eb);
      font-size: 0.9rem;
    }
    .grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }
    @media (max-width: 900px) {
      .grid { grid-template-columns: 1fr; }
    }
    dl {
      display: grid;
      grid-template-columns: 140px 1fr;
      gap: 0.35rem 0.75rem;
      margin: 0.75rem 0;
    }
    dt { color: #64748b; }
    dd { margin: 0; }
    .timeline {
      list-style: none;
      padding: 0;
      margin: 0.75rem 0 0;
    }
    .timeline li {
      border-left: 2px solid #cbd5e1;
      padding: 0 0 1rem 1rem;
      position: relative;
    }
    .timeline li::before {
      content: '';
      position: absolute;
      left: -5px;
      top: 4px;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #0f172a;
    }
    .timeline time {
      display: block;
      font-size: 0.8rem;
      color: #64748b;
    }
    .timeline strong { display: block; margin: 0.15rem 0; }
    .muted { color: #64748b; }
    .op-title { font-weight: 600; color: #0f172a; }

    .bpm-panel { margin-top: 1rem; }
    .bpm-header {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: flex-start;
      flex-wrap: wrap;
    }
    .bpm-header h2 { margin: 0; }
    .bpm-sub {
      margin: 0.35rem 0 0;
      color: #64748b;
      font-size: 0.9rem;
    }
    .bpm-badge {
      display: inline-block;
      margin-left: 0.45rem;
      padding: 0.1rem 0.45rem;
      border-radius: 999px;
      background: #ccfbf1;
      color: #0f766e;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .bpm-badge-ended {
      background: #e2e8f0;
      color: #475569;
    }
    .bpm-actions {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }
    .bpm-btn {
      border: 1px solid #cbd5e1;
      background: #fff;
      border-radius: 6px;
      padding: 0.4rem 0.75rem;
      cursor: pointer;
      font-size: 0.85rem;
      font-family: inherit;
    }
    .bpm-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .bpm-btn-primary {
      background: #0f766e;
      border-color: #0f766e;
      color: #fff;
    }
    .bpm-legend {
      display: flex;
      align-items: center;
      gap: 0.65rem;
      color: #64748b;
      font-size: 0.85rem;
      flex-wrap: wrap;
      margin: 0.75rem 0;
    }
    .bpm-hint {
      margin-left: auto;
      opacity: 0.85;
      font-size: 0.8rem;
    }
    .bpm-dot {
      width: 0.75rem;
      height: 0.75rem;
      border-radius: 2px;
      display: inline-block;
    }
    .bpm-dot-active {
      background: #ccfbf1;
      border: 2px solid #0f766e;
    }
    .bpm-dot-completed {
      background: #e2e8f0;
      border: 2px solid #64748b;
    }
    .bpm-canvas {
      height: min(55vh, 520px);
      min-height: 280px;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      overflow: hidden;
      background: #fafafa;
    }
    .bpm-canvas rf-bpmn-viewer-host {
      display: block;
      height: 100%;
    }
    .bpm-meta {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 1rem;
      margin-top: 0.85rem;
      font-size: 0.875rem;
    }
    .bpm-meta h3 {
      margin: 0 0 0.35rem;
      font-size: 0.95rem;
    }
    .bpm-meta ul {
      margin: 0;
      padding-left: 1.1rem;
    }
  `
})
export class TransactionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly payments = inject(PaymentService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly payment = signal<Payment | null>(null);
  readonly events = signal<PaymentEvent[]>([]);
  readonly actionMsg = signal<string | null>(null);

  readonly diagramState = signal<BpmnDiagramState | null>(null);
  readonly diagramLoading = signal(false);
  readonly diagramError = signal<string | null>(null);
  readonly viewerReady = signal(false);

  private paymentId = '';
  private viewer: BpmnViewerHostComponent | null = null;

  ngOnInit(): void {
    this.paymentId = this.route.snapshot.paramMap.get('paymentId')!;
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    forkJoin({
      payment: this.payments.getPayment(this.paymentId),
      events: this.payments.events(this.paymentId)
    }).subscribe({
      next: ({ payment, events }) => {
        this.payment.set(payment);
        this.events.set(events);
        this.loading.set(false);
        this.reloadDiagram();
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Transaction introuvable');
        this.loading.set(false);
      }
    });
  }

  reloadDiagram(): void {
    this.diagramError.set(null);
    this.diagramState.set(null);
    this.viewer = null;
    this.viewerReady.set(false);
    this.diagramLoading.set(true);
    this.payments.diagramState(this.paymentId).pipe(
      catchError((err: { message?: string; error?: { message?: string } }) => {
        this.diagramError.set(
          err?.error?.message || err?.message || 'Impossible de charger le parcours Flowable'
        );
        this.diagramLoading.set(false);
        return of(null);
      })
    ).subscribe((state) => {
      if (state) {
        this.diagramState.set(state);
      }
      this.diagramLoading.set(false);
    });
  }

  onViewerReady(host: BpmnViewerHostComponent): void {
    this.viewer = host;
    this.viewerReady.set(true);
  }

  onViewerFailed(message: string): void {
    this.viewer = null;
    this.viewerReady.set(false);
    this.diagramError.set(message || 'Échec du rendu BPMN');
  }

  fit(): void {
    this.viewer?.fitViewport();
  }

  center(): void {
    this.viewer?.centerViewport();
  }

  zoomIn(): void {
    this.viewer?.zoomIn();
  }

  zoomOut(): void {
    this.viewer?.zoomOut();
  }

  retry(): void {
    this.payments.retry(this.paymentId).subscribe({
      next: (p) => {
        this.payment.set(p);
        this.actionMsg.set('Relance effectuée');
        this.payments.events(this.paymentId).subscribe((e) => this.events.set(e));
        this.reloadDiagram();
      },
      error: (err: { message?: string }) => this.actionMsg.set(err?.message ?? 'Erreur')
    });
  }

  cancel(): void {
    this.payments.cancel(this.paymentId).subscribe({
      next: (p) => {
        this.payment.set(p);
        this.actionMsg.set('Paiement annulé');
        this.payments.events(this.paymentId).subscribe((e) => this.events.set(e));
        this.reloadDiagram();
      },
      error: (err: { message?: string }) => this.actionMsg.set(err?.message ?? 'Erreur')
    });
  }

  statusLabel(status: PaymentStatus, fallback?: string | null): string {
    return paymentStatusLabel(status, fallback);
  }

  statusSeverity(status: PaymentStatus) {
    return paymentStatusSeverity(status);
  }
}
