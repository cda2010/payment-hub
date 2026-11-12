import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import {
  ApprovalInboxItem,
  GatewayService,
} from '../../core/service/gateway.service';

@Component({
  selector: 'app-approval-inbox',
  standalone: true,
  imports: [
    DatePipe,
    DecimalPipe,
    FormsModule,
    RouterLink,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextareaModule,
    TagModule,
    TooltipModule,
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Validations en attente</h1>
          <p class="subtitle">Transactions à approuver ou refuser (rôles du validateur)</p>
        </div>
        <div class="actions">
          <button
            pButton
            type="button"
            label="Actualiser"
            icon="pi pi-refresh"
            class="p-button-outlined"
            [disabled]="loading()"
            (click)="load()"
          ></button>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }
      @if (msg()) {
        <p class="gw-msg">{{ msg() }}</p>
      }

      <div class="gw-panel">
        <p-table [value]="rows()" [loading]="loading()" styleClass="p-datatable-sm">
          <ng-template pTemplate="header">
            <tr>
              <th>Payment ID</th>
              <th>Montant</th>
              <th>Profil</th>
              <th>Niveau</th>
              <th>Groupes</th>
              <th>Initié par</th>
              <th>Créé</th>
              <th></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-r>
            <tr>
              <td>
                <a [routerLink]="['/admin/transactions', r.paymentId]" class="mono">{{ r.paymentId }}</a>
              </td>
              <td>
                {{ r.amount | number: '1.0-0' }}
                <span class="muted">{{ r.currency }}</span>
              </td>
              <td><code>{{ r.profileCode || '—' }}</code></td>
              <td>
                <p-tag
                  [value]="'L' + r.levelNumber + ' (' + r.collectedApprovals + '/' + r.requiredApprovals + ')'"
                  severity="warning"
                />
              </td>
              <td>{{ r.candidateGroups || '—' }}</td>
              <td>{{ r.initiatedBy || '—' }}</td>
              <td>{{ r.createdAt | date: 'short' }}</td>
              <td class="row-actions">
                <button
                  pButton
                  type="button"
                  class="p-button-sm"
                  label="Approuver"
                  icon="pi pi-check"
                  (click)="openDecide(r, 'APPROVE')"
                ></button>
                <button
                  pButton
                  type="button"
                  class="p-button-sm p-button-outlined p-button-danger"
                  label="Refuser"
                  icon="pi pi-times"
                  (click)="openDecide(r, 'REJECT')"
                ></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr>
              <td colspan="8">Aucune transaction en attente de validation</td>
            </tr>
          </ng-template>
        </p-table>
      </div>
    </div>

    <p-dialog
      [(visible)]="decideVisible"
      [header]="decideAction === 'APPROVE' ? 'Approuver' : 'Refuser'"
      [modal]="true"
      [style]="{ width: '28rem' }"
      [draggable]="false"
    >
      @if (selected(); as s) {
        <p class="decide-summary">
          <code>{{ s.paymentId }}</code>
          — {{ s.amount | number: '1.0-0' }} {{ s.currency }}
          — niveau {{ s.levelNumber }}
        </p>
      }
      <label class="full">
        Commentaire
        <textarea pInputTextarea [(ngModel)]="comment" rows="3" class="w-full"></textarea>
      </label>
      @if (decideError()) {
        <p class="gw-msg error">{{ decideError() }}</p>
      }
      <ng-template pTemplate="footer">
        <button pButton type="button" class="p-button-text" label="Annuler" (click)="decideVisible = false"></button>
        <button
          pButton
          type="button"
          [label]="decideAction === 'APPROVE' ? 'Approuver' : 'Refuser'"
          [class]="decideAction === 'APPROVE' ? '' : 'p-button-danger'"
          [disabled]="deciding()"
          (click)="confirmDecide()"
        ></button>
      </ng-template>
    </p-dialog>
  `,
  styles: `
    .row-actions {
      display: flex;
      gap: 0.35rem;
      flex-wrap: wrap;
      justify-content: flex-end;
    }
    .mono {
      font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
      font-size: 0.85rem;
    }
    .muted {
      opacity: 0.7;
      margin-left: 0.25rem;
    }
    .decide-summary {
      margin: 0 0 0.75rem;
    }
    label.full {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
    }
  `,
})
export class ApprovalInboxComponent implements OnInit {
  private readonly gateway = inject(GatewayService);

  readonly rows = signal<ApprovalInboxItem[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);

  readonly selected = signal<ApprovalInboxItem | null>(null);
  decideVisible = false;
  decideAction: 'APPROVE' | 'REJECT' = 'APPROVE';
  comment = '';
  readonly deciding = signal(false);
  readonly decideError = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.gateway.approvalInbox().subscribe({
      next: (items) => {
        this.rows.set(items ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || err?.message || 'Impossible de charger l’inbox');
        this.loading.set(false);
      },
    });
  }

  openDecide(row: ApprovalInboxItem, action: 'APPROVE' | 'REJECT'): void {
    this.selected.set(row);
    this.decideAction = action;
    this.comment = '';
    this.decideError.set(null);
    this.decideVisible = true;
  }

  confirmDecide(): void {
    const row = this.selected();
    if (!row?.paymentId) {
      return;
    }
    this.deciding.set(true);
    this.decideError.set(null);
    this.gateway
      .decideApproval(row.paymentId, this.decideAction, {
        comment: this.comment?.trim() || undefined,
      })
      .subscribe({
        next: () => {
          this.deciding.set(false);
          this.decideVisible = false;
          this.msg.set(
            this.decideAction === 'APPROVE'
              ? `Approuvé : ${row.paymentId}`
              : `Refusé : ${row.paymentId}`,
          );
          this.load();
        },
        error: (err) => {
          this.deciding.set(false);
          this.decideError.set(err?.error?.message || err?.message || 'Décision refusée');
        },
      });
  }
}
