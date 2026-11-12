import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { TableModule } from 'primeng/table';
import { CheckboxModule } from 'primeng/checkbox';
import {
  ApprovalAdminService,
  ApprovalProfileDto,
  ApprovalSimulateResponse,
} from '../../core/service/approval-admin.service';

@Component({
  selector: 'app-approval-profiles-admin',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    InputTextareaModule,
    CheckboxModule,
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Profils d'approbation</h1>
          <p class="subtitle">Circuits DMN → niveaux / rôles (ACCOUNT_TRANSFER)</p>
        </div>
        <div class="actions">
          <button pButton type="button" label="Simuler" class="p-button-outlined" (click)="openSimulate()"></button>
          <button pButton type="button" label="Nouveau profil" icon="pi pi-plus" (click)="openCreate()"></button>
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
              <th>Code</th>
              <th>Nom</th>
              <th>Actif</th>
              <th>Mis à jour</th>
              <th></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-p>
            <tr>
              <td><code>{{ p.code }}</code></td>
              <td>{{ p.name }}</td>
              <td>{{ p.actif ? 'Oui' : 'Non' }}</td>
              <td>{{ p.updatedAt | date: 'short' }}</td>
              <td>
                <button pButton type="button" class="p-button-text p-button-sm" label="Éditer" (click)="openEdit(p)"></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td colspan="5">Aucun profil</td></tr>
          </ng-template>
        </p-table>
      </div>
    </div>

    <p-dialog
      [(visible)]="dialogVisible"
      [header]="editId ? 'Éditer le profil' : 'Nouveau profil'"
      [modal]="true"
      [style]="{ width: '42rem' }"
      [draggable]="false"
    >
      <div class="form-grid">
        <label>Code <input pInputText [(ngModel)]="form.code" [disabled]="!!editId" /></label>
        <label>Nom <input pInputText [(ngModel)]="form.name" /></label>
        <label class="full">Description <input pInputText [(ngModel)]="form.description" /></label>
        <label class="check"><p-checkbox [(ngModel)]="form.actif" [binary]="true" inputId="actif"></p-checkbox> Actif</label>
        <label class="full">
          Définition JSON
          <textarea pInputTextarea [(ngModel)]="form.definitionJson" rows="12"></textarea>
        </label>
      </div>
      <ng-template pTemplate="footer">
        <button pButton type="button" class="p-button-text" label="Annuler" (click)="dialogVisible = false"></button>
        <button pButton type="button" label="Enregistrer" (click)="save()" [disabled]="saving()"></button>
      </ng-template>
    </p-dialog>

    <p-dialog
      [(visible)]="simulateVisible"
      header="Simuler le circuit"
      [modal]="true"
      [style]="{ width: '32rem' }"
      [draggable]="false"
    >
      <div class="form-grid">
        <label>Montant <input pInputText type="number" [(ngModel)]="simAmount" /></label>
        <label>Devise <input pInputText [(ngModel)]="simCurrency" /></label>
      </div>
      @if (simError()) {
        <p class="gw-msg error">{{ simError() }}</p>
      }
      @if (simulateResult(); as r) {
        <div class="sim-out">
          <p><strong>Profil :</strong> <code>{{ r.profileCode }}</code> ({{ r.source }})</p>
          <p><strong>Stratégie :</strong> {{ r.strategy }} — {{ r.levelCount }} niveau(x)</p>
          <ul>
            @for (l of r.levels; track l.level) {
              <li>
                N{{ l.level }} — {{ l.requiredApprovals }} approbation(s) —
                {{ l.assignments.join(', ') || '—' }}
              </li>
            }
          </ul>
          @if (r.levelCount === 0) {
            <p class="hint">Aucune validation humaine pour ce montant.</p>
          }
        </div>
      }
      <ng-template pTemplate="footer">
        <button pButton type="button" class="p-button-text" label="Fermer" (click)="simulateVisible = false"></button>
        <button
          pButton
          type="button"
          label="Lancer"
          [loading]="simBusy()"
          [disabled]="simBusy()"
          (click)="runSimulate()"
        ></button>
      </ng-template>
    </p-dialog>
  `,
  styles: `
    .gw-page-header {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: flex-start;
    }
    .actions {
      display: flex;
      gap: 0.5rem;
    }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem 1rem;
    }
    .form-grid label {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      font-size: 0.9rem;
    }
    .form-grid .full {
      grid-column: 1 / -1;
    }
    .form-grid .check {
      flex-direction: row;
      align-items: center;
    }
    .form-grid textarea,
    .form-grid input {
      width: 100%;
      font-family: ui-monospace, monospace;
    }
    .sim-out {
      margin-top: 1rem;
      background: #f3f4f6;
      padding: 0.75rem 1rem;
      border-radius: 6px;
      font-size: 0.9rem;
      line-height: 1.45;
    }
    .sim-out ul {
      margin: 0.4rem 0 0;
      padding-left: 1.2rem;
    }
    .sim-out .hint {
      margin: 0.5rem 0 0;
      color: #6b7280;
      font-size: 0.85rem;
    }
    .gw-msg.error {
      color: #b91c1c;
      margin-top: 0.75rem;
    }
  `,
})
export class ApprovalProfilesAdminComponent implements OnInit {
  private readonly api = inject(ApprovalAdminService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);
  readonly rows = signal<ApprovalProfileDto[]>([]);
  readonly simulateResult = signal<ApprovalSimulateResponse | null>(null);
  readonly simBusy = signal(false);
  readonly simError = signal<string | null>(null);

  dialogVisible = false;
  simulateVisible = false;
  editId: string | null = null;
  form = this.emptyForm();
  simAmount = 15000000;
  simCurrency = 'XOF';

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.listProfiles().subscribe({
      next: (rows) => {
        this.rows.set(rows ?? []);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Impossible de charger les profils');
        this.loading.set(false);
      },
    });
  }

  openCreate(): void {
    this.editId = null;
    this.form = this.emptyForm();
    this.dialogVisible = true;
  }

  openEdit(p: ApprovalProfileDto): void {
    this.editId = p.id;
    this.form = {
      code: p.code,
      name: p.name,
      description: p.description ?? '',
      definitionJson: this.pretty(p.definitionJson),
      actif: p.actif,
    };
    this.dialogVisible = true;
  }

  openSimulate(): void {
    this.simulateResult.set(null);
    this.simError.set(null);
    this.simulateVisible = true;
  }

  runSimulate(): void {
    this.simBusy.set(true);
    this.simError.set(null);
    this.simulateResult.set(null);
    this.api
      .simulate({
        amount: Number(this.simAmount),
        currency: this.simCurrency?.trim() || 'XOF',
        catalogServiceCode: 'ACCOUNT_TRANSFER',
      })
      .subscribe({
        next: (r) => {
          this.simBusy.set(false);
          this.simulateResult.set(r);
        },
        error: (err: { message?: string; error?: { message?: string } }) => {
          this.simBusy.set(false);
          this.simError.set(err?.message ?? err?.error?.message ?? 'Simulation échouée');
        },
      });
  }

  save(): void {
    this.saving.set(true);
    this.error.set(null);
    const body = {
      code: this.form.code.trim(),
      name: this.form.name.trim(),
      description: this.form.description?.trim() || null,
      definitionJson: this.form.definitionJson.trim(),
      actif: this.form.actif,
    };
    const req = this.editId
      ? this.api.updateProfile(this.editId, body)
      : this.api.createProfile(body);
    req.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogVisible = false;
        this.msg.set('Profil enregistré');
        this.reload();
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.saving.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Enregistrement impossible');
      },
    });
  }

  private emptyForm() {
    return {
      code: '',
      name: '',
      description: '',
      definitionJson:
        '{\n  "strategy": "SEQUENTIAL",\n  "levels": [\n    {\n      "level": 1,\n      "requiredApprovals": 1,\n      "assignments": [{ "type": "ROLE", "value": "CHEF_AGENCE" }]\n    }\n  ]\n}',
      actif: true,
    };
  }

  private pretty(json: string): string {
    try {
      return JSON.stringify(JSON.parse(json), null, 2);
    } catch {
      return json;
    }
  }
}
