import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { EvaluationResponse, RulesService } from '../../core/service/rules.service';

/** Simulateur CDC — cas plafond journalier 420k + 100k → REJECT. */
@Component({
  selector: 'app-simulateur-regles',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputNumberModule, InputTextModule, TableModule, TagModule],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Simulateur de règles</h1>
          <p class="subtitle">Teste sans réserver de capacité (POST /rules/simulate)</p>
        </div>
      </div>

      <form class="gw-panel filtres" (ngSubmit)="lancer()">
        <div class="field">
          <label>Type d'opération</label>
          <input pInputText name="txType" [(ngModel)]="transactionType" />
        </div>
        <div class="field">
          <label>Groupe</label>
          <input pInputText name="group" [(ngModel)]="groupCode" />
        </div>
        <div class="field">
          <label>Montant</label>
          <p-inputNumber [(ngModel)]="amount" name="amount" mode="decimal" [minFractionDigits]="0" />
        </div>
        <div class="field">
          <label>KYC</label>
          <input pInputText name="kyc" [(ngModel)]="kycLevel" />
        </div>
        <div class="field">
          <label>Statut client</label>
          <input pInputText name="status" [(ngModel)]="customerStatus" />
        </div>
        <div class="field">
          <label>Solde wallet</label>
          <p-inputNumber [(ngModel)]="walletBalance" name="balance" mode="decimal" [minFractionDigits]="0" />
        </div>
        <button pButton type="submit" icon="pi pi-play" label="Simuler" [loading]="loading()"></button>
      </form>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      @if (result(); as r) {
        <div class="gw-panel">
          <h3>
            Décision
            <p-tag
              [value]="r.decision"
              [severity]="r.decision === 'ALLOW' ? 'success' : r.decision === 'REVIEW' ? 'warning' : 'danger'"
            />
            · groupe {{ r.groupCode }}
          </h3>
          <p>Règles évaluées : {{ r.evaluatedRules.join(', ') || '—' }}</p>
          @if (r.violations.length) {
            <p-table [value]="r.violations" styleClass="p-datatable-sm">
              <ng-template pTemplate="header">
                <tr>
                  <th>Règle</th>
                  <th>Message</th>
                  <th class="num">Cumul</th>
                  <th class="num">Demandé</th>
                  <th class="num">Projeté</th>
                  <th class="num">Limite</th>
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-v>
                <tr>
                  <td class="code">{{ v.ruleCode }} v{{ v.ruleVersion }}</td>
                  <td>{{ v.message }}</td>
                  <td class="num">{{ v.currentCumul ?? '—' }}</td>
                  <td class="num">{{ v.requestedAmount ?? '—' }}</td>
                  <td class="num"><strong>{{ v.projectedCumul ?? '—' }}</strong></td>
                  <td class="num">{{ v.limit ?? '—' }}</td>
                </tr>
              </ng-template>
            </p-table>
          } @else {
            <p>Aucune violation.</p>
          }
        </div>
      }
    </div>
  `,
  styles: `
    .filtres { display: flex; flex-wrap: wrap; gap: 0.75rem; align-items: flex-end; }
    .field { display: flex; flex-direction: column; gap: 0.25rem; }
    .field label { font-size: 0.8rem; color: #64748b; }
    .num { text-align: right; }
    .code { font-family: ui-monospace, monospace; font-weight: 600; }
    h3 { margin: 0 0 0.5rem; font-size: 1rem; display: flex; gap: 0.5rem; align-items: center; }
  `
})
export class SimulateurReglesComponent {
  private readonly api = inject(RulesService);

  transactionType = 'CASH_OUT';
  groupCode = 'CASH_OUT_STANDARD';
  amount = 100000;
  kycLevel = 'STANDARD';
  customerStatus = 'ACTIVE';
  walletBalance = 1000000;

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<EvaluationResponse | null>(null);

  lancer(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .simuler({
        transactionId: 'SIM-' + Date.now(),
        transactionType: this.transactionType,
        groupCode: this.groupCode,
        amount: this.amount,
        currency: 'XOF',
        customerId: 'CLI001',
        walletId: 'WALLET001',
        context: {
          kycLevel: this.kycLevel,
          customerStatus: this.customerStatus,
          walletBalance: this.walletBalance,
          channel: 'MOBILE'
        }
      })
      .subscribe({
        next: (r) => {
          this.result.set(r);
          this.loading.set(false);
        },
        error: (e: { message?: string }) => {
          this.loading.set(false);
          this.error.set(e?.message ?? 'Simulation impossible.');
        }
      });
  }
}
