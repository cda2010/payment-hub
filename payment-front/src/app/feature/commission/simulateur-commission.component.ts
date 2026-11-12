import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { CommissionService, SimulationResult } from '../../core/service/commission.service';

/** Simulateur Lot 1 — cas CDC R-01 : frais 1000 → parts 40/25/20/10/5. */
@Component({
  selector: 'app-simulateur-commission',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputNumberModule, InputTextModule, TableModule, TagModule],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Simulateur de commissionnement</h1>
          <p class="subtitle">Calcule les parts sans créer de droit (simulation seule)</p>
        </div>
      </div>

      <form class="gw-panel filtres" (ngSubmit)="lancer()">
        <div class="field">
          <label>Opération</label>
          <input pInputText name="operation" [(ngModel)]="operation" />
        </div>
        <div class="field">
          <label>Canal</label>
          <input pInputText name="channel" [(ngModel)]="channel" />
        </div>
        <div class="field">
          <label>Montant</label>
          <p-inputNumber [(ngModel)]="amount" name="amount" mode="decimal" [minFractionDigits]="0" />
        </div>
        <div class="field">
          <label>Frais</label>
          <p-inputNumber [(ngModel)]="fees" name="fees" mode="decimal" [minFractionDigits]="0" />
        </div>
        <div class="field">
          <label>Devise</label>
          <input pInputText name="currency" [(ngModel)]="currency" />
        </div>
        <button pButton type="submit" icon="pi pi-play" label="Simuler" [loading]="loading()"></button>
      </form>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      @if (result(); as r) {
        <div class="gw-panel">
          <h3>
            Grille {{ r.grilleCode }} v{{ r.grilleVersion }}
            <p-tag [value]="r.envelopeMode" severity="info" />
          </h3>
          <p>
            Frais <strong>{{ r.fees }}</strong> · Total commissions
            <strong>{{ r.totalCommissions }}</strong> · Reliquat <strong>{{ r.reliquat }}</strong>
          </p>
          @if (r.warnings.length) {
            <ul class="warn">
              @for (w of r.warnings; track w) {
                <li>{{ w }}</li>
              }
            </ul>
          }
          <p-table [value]="r.parts" styleClass="p-datatable-sm">
            <ng-template pTemplate="header">
              <tr>
                <th>Règle</th>
                <th>Bénéficiaire</th>
                <th>Formule</th>
                <th>Base</th>
                <th class="num">Commission</th>
                <th>Financement</th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-p>
              <tr>
                <td class="code">{{ p.ruleCode }}</td>
                <td>{{ p.beneficiaryRole }}</td>
                <td>{{ p.formuleCode }}</td>
                <td>{{ p.baseAmount }} ({{ p.commissionBase }})</td>
                <td class="num"><strong>{{ p.commissionAmount }}</strong></td>
                <td>{{ p.fundingType }}</td>
              </tr>
            </ng-template>
          </p-table>
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
    .warn { color: #b45309; }
    h3 { margin: 0 0 0.5rem; font-size: 1rem; display: flex; gap: 0.5rem; align-items: center; }
  `
})
export class SimulateurCommissionComponent {
  private readonly api = inject(CommissionService);

  operation = 'CASH_OUT';
  channel = 'AGENT';
  currency = 'XOF';
  amount = 20000;
  fees = 1000;

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<SimulationResult | null>(null);

  lancer(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .simuler({
        transactionId: 'SIM-' + Date.now(),
        operation: this.operation,
        channel: this.channel,
        currency: this.currency,
        amount: this.amount,
        fees: this.fees
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
