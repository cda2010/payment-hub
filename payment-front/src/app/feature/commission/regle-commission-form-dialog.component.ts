import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import {
  CommissionService,
  FormuleCommission,
  RegleCommission,
  UpsertRegleRequest
} from '../../core/service/commission.service';

const BASES = [
  'TRANSACTION_FEES',
  'TRANSACTION_AMOUNT',
  'CUMUL_FEES',
  'CUMUL_AMOUNT',
  'NET_REVENUE'
].map((v) => ({ label: v, value: v }));

const FUNDINGS = [
  'TRANSACTION_FEES',
  'COMMERCIAL_ENVELOPE',
  'SUBSIDY',
  'MIXED'
].map((v) => ({ label: v, value: v }));

@Component({
  selector: 'app-regle-commission-form-dialog',
  standalone: true,
  imports: [
    FormsModule, ButtonModule, CheckboxModule, DialogModule,
    DropdownModule, InputNumberModule, InputTextModule
  ],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="visibleChange.emit($event)"
      [header]="regle ? 'Modifier la règle ' + regle.code : 'Nouvelle règle'"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '40rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="enregistrer()">
        <div class="gw-field">
          <label>Code *</label>
          <input pInputText [(ngModel)]="s.code" name="code" maxlength="64" />
        </div>
        <div class="gw-field">
          <label>Libellé *</label>
          <input pInputText [(ngModel)]="s.libelle" name="libelle" maxlength="200" />
        </div>
        <div class="gw-field">
          <label>Bénéficiaire (rôle) *</label>
          <input pInputText [(ngModel)]="s.beneficiaryRole" name="role" placeholder="AGENT, DISTRIBUTOR…" maxlength="64" />
        </div>
        <div class="gw-field">
          <label>Priorité</label>
          <p-inputNumber [(ngModel)]="s.priority" name="prio" [min]="1" [max]="9999" styleClass="w-full" />
        </div>
        <div class="gw-field">
          <label>Formule *</label>
          <p-dropdown
            [options]="formuleOptions()"
            [(ngModel)]="s.formuleKey"
            name="formule"
            optionLabel="label"
            optionValue="value"
            styleClass="w-full"
            placeholder="Choisir…"
          />
        </div>
        <div class="gw-field">
          <label>Base</label>
          <p-dropdown [options]="bases" [(ngModel)]="s.commissionBase" name="base" optionLabel="label" optionValue="value" styleClass="w-full" />
        </div>
        <div class="gw-field">
          <label>Financement</label>
          <p-dropdown [options]="fundings" [(ngModel)]="s.fundingType" name="fund" optionLabel="label" optionValue="value" styleClass="w-full" />
        </div>
        <div class="gw-field">
          <label>Funder role (si SUBSIDY)</label>
          <input pInputText [(ngModel)]="s.funderRole" name="funder" maxlength="64" />
        </div>
        <div class="gw-field full">
          <label>Params JSON *</label>
          <input pInputText [(ngModel)]="s.paramsJson" name="params" placeholder='{"taux":0.20} ou {"montantFixe":50}' />
          <small class="muted">PERCENT → taux (0–1) · FIXED → montantFixe · HYBRID → les deux</small>
        </div>
        <div class="gw-field">
          <label>Plancher</label>
          <p-inputNumber [(ngModel)]="s.floorAmount" name="floor" mode="decimal" [minFractionDigits]="0" styleClass="w-full" />
        </div>
        <div class="gw-field">
          <label>Plafond</label>
          <p-inputNumber [(ngModel)]="s.capAmount" name="cap" mode="decimal" [minFractionDigits]="0" styleClass="w-full" />
        </div>
        <div class="gw-field check full">
          <p-checkbox [(ngModel)]="s.active" name="active" [binary]="true" inputId="r-active" />
          <label for="r-active">Règle active</label>
        </div>
        @if (error()) {
          <p class="gw-msg error full">{{ error() }}</p>
        }
        <div class="actions full">
          <button pButton type="button" class="p-button-text" label="Annuler" (click)="visibleChange.emit(false)"></button>
          <button pButton type="submit" [label]="regle ? 'Enregistrer' : 'Ajouter'" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .gw-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem 1rem; }
    .gw-field { display: flex; flex-direction: column; gap: 0.25rem; }
    .gw-field.full, .full { grid-column: 1 / -1; }
    .gw-field.check { flex-direction: row; align-items: center; gap: 0.5rem; }
    .actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem; }
    label { font-size: 0.8rem; color: #64748b; }
    .muted { color: #94a3b8; font-size: 0.75rem; }
  `
})
export class RegleCommissionFormDialogComponent implements OnChanges {
  private readonly api = inject(CommissionService);

  @Input() visible = false;
  @Input() grilleId = '';
  @Input() regle: RegleCommission | null = null;
  @Input() formules: FormuleCommission[] = [];
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() saved = new EventEmitter<void>();

  readonly bases = BASES;
  readonly fundings = FUNDINGS;
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  s = this.vide();

  formuleOptions(): { label: string; value: string }[] {
    return this.formules.map((f) => ({
      label: `${f.code} v${f.version} — ${f.libelle}`,
      value: `${f.code}|${f.version}`
    }));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] || changes['regle'] || changes['formules']) {
      this.error.set(null);
      if (this.regle) {
        this.s = {
          code: this.regle.code,
          libelle: this.regle.libelle,
          beneficiaryRole: this.regle.beneficiaryRole,
          formuleKey: `${this.regle.formuleCode}|${this.regle.formuleVersion}`,
          commissionBase: this.regle.commissionBase,
          fundingType: this.regle.fundingType,
          funderRole: this.regle.funderRole ?? '',
          paramsJson: this.regle.paramsJson || '{}',
          floorAmount: this.regle.floorAmount ?? null,
          capAmount: this.regle.capAmount ?? null,
          priority: this.regle.priority,
          active: this.regle.active
        };
      } else {
        this.s = this.vide();
        if (this.formules.length && !this.s.formuleKey) {
          const f = this.formules[0];
          this.s.formuleKey = `${f.code}|${f.version}`;
        }
      }
    }
  }

  enregistrer(): void {
    this.error.set(null);
    if (!this.s.code.trim() || !this.s.libelle.trim() || !this.s.beneficiaryRole.trim() || !this.s.formuleKey) {
      this.error.set('Code, libellé, bénéficiaire et formule sont obligatoires.');
      return;
    }
    try {
      JSON.parse(this.s.paramsJson || '{}');
    } catch {
      this.error.set('paramsJson doit être un JSON valide.');
      return;
    }
    const [formuleCode, ver] = this.s.formuleKey.split('|');
    const body: UpsertRegleRequest = {
      code: this.s.code.trim(),
      libelle: this.s.libelle.trim(),
      beneficiaryRole: this.s.beneficiaryRole.trim(),
      formuleCode,
      formuleVersion: Number(ver) || 1,
      commissionBase: this.s.commissionBase,
      fundingType: this.s.fundingType,
      funderRole: this.s.funderRole || null,
      paramsJson: this.s.paramsJson || '{}',
      floorAmount: this.s.floorAmount,
      capAmount: this.s.capAmount,
      priority: this.s.priority,
      active: this.s.active
    };
    this.saving.set(true);
    const req$ = this.regle
      ? this.api.majRegle(this.grilleId, this.regle.id, body)
      : this.api.ajouterRegle(this.grilleId, body);
    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.saved.emit();
        this.visibleChange.emit(false);
      },
      error: (e: { message?: string }) => {
        this.saving.set(false);
        this.error.set(e?.message ?? 'Enregistrement impossible.');
      }
    });
  }

  private vide() {
    return {
      code: '',
      libelle: '',
      beneficiaryRole: '',
      formuleKey: '',
      commissionBase: 'TRANSACTION_FEES',
      fundingType: 'TRANSACTION_FEES',
      funderRole: '',
      paramsJson: '{"taux":0.20}',
      floorAmount: null as number | null,
      capAmount: null as number | null,
      priority: 100,
      active: true
    };
  }
}
