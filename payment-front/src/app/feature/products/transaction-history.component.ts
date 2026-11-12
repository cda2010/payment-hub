import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import {
  GatewayService,
  GatewayTransactionSummary,
} from '../../core/service/gateway.service';
import {
  dateInputToInstant,
  paymentStatusFilterOptions,
  paymentStatusLabel,
  paymentStatusSeverity,
} from '../../core/util/payment-status.util';

/**
 * Historique canal — GET /gateway/transactions.
 * Affiche les libellés serveur (title / subtitle / productLabel / statusLabel).
 */
@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    TableModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    TagModule,
    TooltipModule,
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Historique des transactions</h1>
          <p class="subtitle">Gateway — libellés localisés (Accept-Language)</p>
        </div>
        <div class="header-actions">
          <a
            pButton
            routerLink="/products"
            label="Produits"
            icon="pi pi-th-large"
            class="p-button-outlined p-button-sm"
          ></a>
        </div>
      </div>

      <div class="gw-panel">
        <form [formGroup]="filters" class="gw-form-grid filters" (ngSubmit)="search(0)">
          <div class="gw-field">
            <label for="q">Recherche</label>
            <input
              pInputText
              id="q"
              formControlName="q"
              class="w-full"
              placeholder="paymentId, téléphone, réf…"
            />
          </div>
          <div class="gw-field">
            <label for="product">Produit</label>
            <input
              pInputText
              id="product"
              formControlName="product"
              class="w-full"
              placeholder="CASH_OUT, BILL_PAYMENT…"
            />
          </div>
          <div class="gw-field">
            <label for="status">Statut</label>
            <p-dropdown
              inputId="status"
              formControlName="status"
              [options]="statusOptions"
              optionLabel="label"
              optionValue="value"
              placeholder="Tous"
              styleClass="w-full"
            />
          </div>
          <div class="gw-field">
            <label for="customerReference">Réf. client</label>
            <input
              pInputText
              id="customerReference"
              formControlName="customerReference"
              class="w-full"
            />
          </div>
          <div class="gw-field">
            <label for="provider">Fournisseur</label>
            <input pInputText id="provider" formControlName="provider" class="w-full" />
          </div>
          <div class="gw-field">
            <label for="from">Du</label>
            <input pInputText type="date" id="from" formControlName="from" class="w-full" />
          </div>
          <div class="gw-field">
            <label for="to">Au</label>
            <input pInputText type="date" id="to" formControlName="to" class="w-full" />
          </div>
          <div class="gw-actions">
            <button pButton type="submit" label="Rechercher" icon="pi pi-search"></button>
          </div>
        </form>

        @if (error()) {
          <p class="gw-msg error">{{ error() }}</p>
        }

        <p-table
          [value]="rows()"
          [lazy]="true"
          [loading]="loading()"
          [paginator]="true"
          [rows]="size()"
          [totalRecords]="total()"
          [first]="page() * size()"
          [rowsPerPageOptions]="[10, 20, 50]"
          (onLazyLoad)="onLazyLoad($event)"
          styleClass="p-datatable-sm"
          [tableStyle]="{ 'min-width': '72rem' }"
        >
          <ng-template pTemplate="header">
            <tr>
              <th>Opération</th>
              <th>Produit</th>
              <th>Montant</th>
              <th>Statut</th>
              <th>Fournisseur</th>
              <th>Client</th>
              <th>Créé</th>
              <th></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-p>
            <tr>
              <td class="op-cell">
                <div class="op-title" [pTooltip]="p.title || ''" tooltipPosition="top">
                  {{ displayTitle(p) }}
                </div>
                @if (displaySubtitle(p); as sub) {
                  <div class="op-sub">{{ sub }}</div>
                }
                <div class="op-id">
                  @if (resumeLink(p); as link) {
                    <a [routerLink]="link">{{ p.paymentId }}</a>
                  } @else {
                    <a [routerLink]="['/admin/transactions', p.paymentId]">{{ p.paymentId }}</a>
                  }
                </div>
              </td>
              <td>
                <div>{{ p.productLabel || p.catalogServiceCode || p.serviceType || '—' }}</div>
                @if (p.productLabel && p.catalogServiceCode) {
                  <div class="op-code">{{ p.catalogServiceCode }}</div>
                }
              </td>
              <td class="amt">{{ formatAmount(p) }}</td>
              <td>
                <p-tag
                  [value]="statusLabel(p)"
                  [severity]="statusSeverity(p.status)"
                />
              </td>
              <td>{{ p.provider || '—' }}</td>
              <td>{{ p.customerReference || p.phoneNumber || '—' }}</td>
              <td>{{ p.createdAt | date: 'short' }}</td>
              <td>
                <a
                  pButton
                  class="p-button-text p-button-sm"
                  icon="pi pi-eye"
                  [routerLink]="['/admin/transactions', p.paymentId]"
                  pTooltip="Détail supervision"
                ></a>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr>
              <td colspan="8">Aucune transaction</td>
            </tr>
          </ng-template>
        </p-table>
      </div>
    </div>
  `,
  styles: `
    .filters {
      margin-bottom: 1rem;
    }
    .w-full {
      width: 100%;
    }
    a {
      color: var(--primary-color, #2563eb);
      text-decoration: none;
      font-weight: 500;
    }
    a:hover {
      text-decoration: underline;
    }
    .op-cell {
      max-width: 22rem;
    }
    .op-title {
      font-weight: 600;
      color: var(--text-color, #0f172a);
      line-height: 1.25;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .op-sub {
      margin-top: 0.15rem;
      font-size: 0.8rem;
      color: #64748b;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .op-id {
      margin-top: 0.25rem;
      font-size: 0.75rem;
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
    }
    .op-code {
      margin-top: 0.15rem;
      font-size: 0.72rem;
      color: #94a3b8;
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
    }
    .amt {
      white-space: nowrap;
      font-variant-numeric: tabular-nums;
      font-weight: 600;
    }
  `,
})
export class TransactionHistoryComponent implements OnInit {
  private readonly gateway = inject(GatewayService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly rows = signal<GatewayTransactionSummary[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(20);

  readonly statusOptions = paymentStatusFilterOptions([
    'SUCCESS',
    'FAILED',
    'AWAITING_USER_INPUT',
    'WAITING_APPROVAL',
    'PROCESSING',
    'OTP_REQUIRED',
    'CANCELLED',
    'EXPIRED',
  ]);

  readonly filters = this.fb.nonNullable.group({
    q: [''],
    product: [''],
    status: [''],
    customerReference: [''],
    provider: [''],
    from: [''],
    to: [''],
  });

  ngOnInit(): void {
    this.search(0);
  }

  onLazyLoad(event: TableLazyLoadEvent): void {
    const rows = event.rows ?? this.size();
    const first = event.first ?? 0;
    this.size.set(rows);
    this.search(Math.floor(first / rows));
  }

  search(page: number): void {
    this.page.set(page);
    this.loading.set(true);
    this.error.set(null);
    const v = this.filters.getRawValue();
    this.gateway
      .listTransactions({
        page,
        size: this.size(),
        q: v.q || undefined,
        product: v.product || undefined,
        status: v.status || undefined,
        customerReference: v.customerReference || undefined,
        provider: v.provider || undefined,
        from: dateInputToInstant(v.from, false),
        to: dateInputToInstant(v.to, true),
      })
      .subscribe({
        next: (res) => {
          this.loading.set(false);
          this.rows.set(res.items ?? []);
          this.total.set(res.total ?? 0);
        },
        error: (err: { error?: { message?: string }; message?: string }) => {
          this.loading.set(false);
          this.error.set(
            err?.error?.message || err?.message || 'Impossible de charger l’historique'
          );
        },
      });
  }

  displayTitle(p: GatewayTransactionSummary): string {
    return (
      (p.title || '').trim() ||
      (p.productLabel || '').trim() ||
      p.catalogServiceCode ||
      p.serviceType ||
      p.paymentId
    );
  }

  displaySubtitle(p: GatewayTransactionSummary): string | null {
    const sub = (p.subtitle || '').trim();
    return sub || null;
  }

  statusLabel(p: GatewayTransactionSummary): string {
    return paymentStatusLabel(p.status, p.statusLabel);
  }

  statusSeverity(status?: string | null) {
    return paymentStatusSeverity(status);
  }

  formatAmount(p: GatewayTransactionSummary): string {
    if (p.amount == null) {
      return '—';
    }
    const n = Number(p.amount);
    const formatted = Number.isFinite(n)
      ? n.toLocaleString('fr-FR', { maximumFractionDigits: 0 })
      : String(p.amount);
    return `${formatted} ${p.currency || ''}`.trim();
  }

  resumeLink(p: GatewayTransactionSummary): string[] | null {
    const code = (p.catalogServiceCode || '').trim();
    if (!code || !p.paymentId) {
      return null;
    }
    return ['/products', code, p.paymentId];
  }
}
