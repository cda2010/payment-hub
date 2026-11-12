import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { PaymentService } from '../../core/service/payment.service';
import { Payment, PaymentStatus } from '../../core/model/api.models';
import {
  dateInputToInstant,
  paymentStatusFilterOptions,
  paymentStatusLabel,
  paymentStatusSeverity,
} from '../../core/util/payment-status.util';

@Component({
  selector: 'app-transactions-list',
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
          <h1>Transactions</h1>
          <p class="subtitle">
            Supervision — colonne « Opération » = title / subtitle serveur (Accept-Language)
          </p>
        </div>
      </div>

      <div class="gw-panel">
        <form [formGroup]="filters" class="gw-form-grid filters" (ngSubmit)="search(0)">
          <div class="gw-field">
            <label for="paymentId">Payment ID</label>
            <input pInputText id="paymentId" formControlName="paymentId" class="w-full" />
          </div>
          <div class="gw-field">
            <label for="provider">Fournisseur</label>
            <input pInputText id="provider" formControlName="provider" class="w-full" />
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
            <label for="phoneNumber">Téléphone</label>
            <input pInputText id="phoneNumber" formControlName="phoneNumber" class="w-full" />
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
                  <a [routerLink]="['/admin/transactions', p.paymentId]">{{ p.paymentId }}</a>
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
                <p-tag [value]="labelOf(p)" [severity]="severityOf(p.status)" />
              </td>
              <td>{{ p.provider || '—' }}</td>
              <td>{{ p.customerReference || p.phoneNumber || '—' }}</td>
              <td>{{ p.createdAt | date: 'short' }}</td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr>
              <td colspan="7">Aucune transaction</td>
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
export class TransactionsListComponent {
  private readonly payments = inject(PaymentService);
  private readonly fb = inject(FormBuilder);

  readonly statuses: PaymentStatus[] = [
    'CREATED',
    'INITIATED',
    'PENDING',
    'PROCESSING',
    'OTP_REQUIRED',
    'WAITING_APPROVAL',
    'AWAITING_USER_INPUT',
    'SUCCESS',
    'FAILED',
    'CANCELLED',
    'EXPIRED',
  ];
  readonly statusOptions = paymentStatusFilterOptions(this.statuses);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly rows = signal<Payment[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(20);

  readonly filters = this.fb.nonNullable.group({
    paymentId: [''],
    provider: [''],
    status: [''],
    customerReference: [''],
    phoneNumber: [''],
    from: [''],
    to: [''],
  });

  search(pageIndex: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.page.set(pageIndex);
    const f = this.filters.getRawValue();
    this.payments
      .adminSearch({
        paymentId: f.paymentId || undefined,
        provider: f.provider || undefined,
        status: f.status || undefined,
        customerReference: f.customerReference || undefined,
        phoneNumber: f.phoneNumber || undefined,
        from: dateInputToInstant(f.from, false),
        to: dateInputToInstant(f.to, true),
        page: pageIndex,
        size: this.size(),
        sort: 'createdAt,desc',
      })
      .subscribe({
        next: (page) => {
          this.rows.set(page.content);
          this.total.set(page.totalElements);
          this.loading.set(false);
        },
        error: (err: { message?: string }) => {
          this.error.set(err?.message ?? 'Erreur de recherche');
          this.loading.set(false);
        },
      });
  }

  onLazyLoad(ev: TableLazyLoadEvent): void {
    const rows = ev.rows ?? this.size();
    const first = ev.first ?? 0;
    this.size.set(rows);
    this.search(Math.floor(first / rows));
  }

  displayTitle(p: Payment): string {
    return (
      (p.title || '').trim() ||
      (p.productLabel || '').trim() ||
      p.catalogServiceCode ||
      p.serviceType ||
      p.paymentId
    );
  }

  displaySubtitle(p: Payment): string | null {
    const sub = (p.subtitle || '').trim();
    return sub || null;
  }

  labelOf(p: Payment): string {
    return paymentStatusLabel(p.status, p.statusLabel);
  }

  severityOf(status: PaymentStatus) {
    return paymentStatusSeverity(status);
  }

  formatAmount(p: Payment): string {
    if (p.amount == null) {
      return '—';
    }
    const n = Number(p.amount);
    const formatted = Number.isFinite(n)
      ? n.toLocaleString('fr-FR', { maximumFractionDigits: 0 })
      : String(p.amount);
    return `${formatted} ${p.currency || ''}`.trim();
  }
}
