import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { PaymentService } from '../../core/service/payment.service';
import { OpsService, OpsMetricsSummary } from '../../core/service/ops.service';
import { DailyStats, Payment } from '../../core/model/api.models';
import {
  paymentStatusLabel,
  paymentStatusSeverity,
} from '../../core/util/payment-status.util';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [ProgressSpinnerModule, ButtonModule, TableModule, TagModule, RouterLink, DatePipe, DecimalPipe],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Tableau de bord</h1>
          <p class="subtitle">
            Statistiques du jour (UTC){{ stats()?.date ? ' — ' + stats()!.date : '' }}
          </p>
        </div>
        <div class="header-actions">
          <a pButton routerLink="/admin/approvals" label="Validations en attente" icon="pi pi-inbox" class="p-button-outlined p-button-sm"></a>
          <a pButton routerLink="/admin/transactions" label="Voir les transactions" icon="pi pi-list" class="p-button-outlined p-button-sm"></a>
        </div>
      </div>

      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      } @else {
        @if (stats(); as s) {
          <div class="gw-stats">
            <div class="gw-stat">
              <div class="value">{{ s.total }}</div>
              <div class="label">Paiements du jour</div>
            </div>
            <div class="gw-stat">
              <div class="value ok">{{ s.success }}</div>
              <div class="label">Succès</div>
            </div>
            <div class="gw-stat">
              <div class="value pending">{{ s.pending }}</div>
              <div class="label">En attente</div>
            </div>
            <div class="gw-stat">
              <div class="value fail">{{ s.failed }}</div>
              <div class="label">Échecs</div>
            </div>
            <div class="gw-stat">
              <div class="value">{{ s.successRate }}%</div>
              <div class="label">Taux de succès</div>
            </div>
          </div>

          @if (opsMetrics(); as ops) {
            <div class="gw-panel ops-metrics">
              <div class="recent-head">
                <h2>Metriques gateway (jour)</h2>
                <a pButton routerLink="/admin/catalog" label="Catalogue FSE" class="p-button-text p-button-sm"></a>
                <a pButton routerLink="/admin/catalog-services" label="Catalogue services" class="p-button-text p-button-sm"></a>
              </div>
              @if (ops.executionsToday.length) {
                <table class="gw-data-table">
                  <thead>
                    <tr>
                      <th>Fournisseur</th>
                      <th>Operation</th>
                      <th>Succes</th>
                      <th>Echecs</th>
                      <th>Latence moy.</th>
                    </tr>
                  </thead>
                  <tbody>
                    @for (row of ops.executionsToday; track row.providerCode + row.operationCode) {
                      <tr>
                        <td>{{ row.providerCode }}</td>
                        <td>{{ row.operationCode }}</td>
                        <td class="ok">{{ row.successCount }}</td>
                        <td class="fail">{{ row.failureCount }}</td>
                        <td>{{ row.avgDurationMs | number: '1.0-0' }} ms</td>
                      </tr>
                    }
                  </tbody>
                </table>
              } @else {
                <p class="gw-msg">Aucun appel fournisseur aujourd'hui.</p>
              }
              @if (ops.circuits.length) {
                <p class="circuit-hint">Circuits: {{ ops.circuits.length }} surveille(s) — voir /actuator/prometheus</p>
              }
            </div>
          }

          <div class="gw-panel recent">
            <div class="recent-head">
              <h2>Dernières transactions</h2>
            </div>
            @if (recentLoading()) {
              <div class="gw-loading">
                <p-progressSpinner strokeWidth="4" [style]="{ width: '32px', height: '32px' }" />
              </div>
            } @else if (recent().length === 0) {
              <p class="gw-msg">Aucune transaction récente.</p>
            } @else {
              <p-table [value]="recent()" styleClass="p-datatable-sm p-datatable-striped">
                <ng-template pTemplate="header">
                  <tr>
                    <th>Opération</th>
                    <th>Statut</th>
                    <th>Montant</th>
                    <th>Créé</th>
                  </tr>
                </ng-template>
                <ng-template pTemplate="body" let-row>
                  <tr>
                    <td>
                      <div class="op-title">{{ row.title || row.productLabel || row.catalogServiceCode || row.provider }}</div>
                      <a [routerLink]="['/admin/transactions', row.paymentId]" class="op-id">{{ row.paymentId }}</a>
                    </td>
                    <td>
                      <p-tag [value]="statusLabel(row.status, row.statusLabel)" [severity]="statusSeverity(row.status)" />
                    </td>
                    <td>{{ row.amount | number: '1.0-2' }} {{ row.currency }}</td>
                    <td>{{ row.createdAt | date: 'short' }}</td>
                  </tr>
                </ng-template>
              </p-table>
            }
          </div>
        }
      }
    </div>
  `,
  styles: `
    :host {
      display: block;
    }
    .gw-stat .value.ok {
      color: #15803d;
    }
    .gw-stat .value.pending {
      color: #b45309;
    }
    .gw-stat .value.fail {
      color: #b91c1c;
    }
    .recent {
      margin-top: 1.25rem;
    }
    .ops-metrics {
      margin-top: 1.25rem;
    }
    .ops-metrics .ok { color: #15803d; }
    .ops-metrics .fail { color: #b91c1c; }
    .circuit-hint { font-size: 0.85rem; color: #64748b; margin-top: 0.75rem; }
    .recent-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.75rem;
    }
    .recent-head h2 {
      margin: 0;
      font-size: 1.05rem;
    }
    a {
      color: #2563eb;
    }
    .op-title {
      font-weight: 600;
      line-height: 1.25;
    }
    .op-id {
      display: block;
      margin-top: 0.15rem;
      font-size: 0.75rem;
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
    }
  `,
})
export class AdminDashboardComponent implements OnInit {
  private readonly payments = inject(PaymentService);
  private readonly ops = inject(OpsService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly stats = signal<DailyStats | null>(null);
  readonly opsMetrics = signal<OpsMetricsSummary | null>(null);
  readonly recent = signal<Payment[]>([]);
  readonly recentLoading = signal(true);

  ngOnInit(): void {
    this.payments.dailyStats().subscribe({
      next: (s) => {
        this.stats.set(s);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Impossible de charger les statistiques');
        this.loading.set(false);
      },
    });

    this.payments.adminSearch({ page: 0, size: 8, sort: 'createdAt,desc' }).subscribe({
      next: (page) => {
        this.recent.set(page.content ?? []);
        this.recentLoading.set(false);
      },
      error: () => {
        this.recent.set([]);
        this.recentLoading.set(false);
      },
    });

    this.ops.metricsSummary().subscribe({
      next: (m) => this.opsMetrics.set(m),
      error: () => this.opsMetrics.set(null),
    });
  }

  statusLabel(status: string, fallback?: string | null): string {
    return paymentStatusLabel(status, fallback);
  }

  statusSeverity(status: string) {
    return paymentStatusSeverity(status);
  }
}
