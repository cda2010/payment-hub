import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import {
  CommissionService,
  CreateGrilleRequest,
  GrilleCommission,
  UpdateGrilleRequest
} from '../../core/service/commission.service';

const ENVELOPPES = [
  { label: 'FULL_FEES — 100 % des frais', value: 'FULL_FEES' },
  { label: 'PARTIAL_FEES — répartition partielle', value: 'PARTIAL_FEES' },
  { label: 'INDEPENDENT — hors frais', value: 'INDEPENDENT' },
  { label: 'MIXED', value: 'MIXED' }
];

@Component({
  selector: 'app-grille-commission-form-dialog',
  standalone: true,
  imports: [FormsModule, ButtonModule, DialogModule, DropdownModule, InputTextModule],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="visibleChange.emit($event)"
      [header]="grille ? 'Modifier ' + grille.code : 'Nouvelle grille de commission'"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '36rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="enregistrer()">
        <div class="gw-field">
          <label>Code *</label>
          <input pInputText [(ngModel)]="s.code" name="code" maxlength="64" [readonly]="!!grille" />
        </div>
        <div class="gw-field">
          <label>Libellé *</label>
          <input pInputText [(ngModel)]="s.libelle" name="libelle" maxlength="200" />
        </div>
        <div class="gw-field">
          <label>Opération *</label>
          <input pInputText [(ngModel)]="s.operationType" name="op" placeholder="CASH_OUT" maxlength="64" />
        </div>
        <div class="gw-field">
          <label>Produit</label>
          <input pInputText [(ngModel)]="s.productCode" name="prod" maxlength="80" />
        </div>
        <div class="gw-field">
          <label>Canal</label>
          <input pInputText [(ngModel)]="s.channelCode" name="canal" placeholder="Tous si vide" maxlength="40" />
        </div>
        <div class="gw-field">
          <label>Devise</label>
          <input pInputText [(ngModel)]="s.currency" name="ccy" maxlength="3" />
        </div>
        <div class="gw-field">
          <label>Début validité</label>
          <input pInputText type="date" [(ngModel)]="s.validFrom" name="from" />
        </div>
        <div class="gw-field">
          <label>Fin validité</label>
          <input pInputText type="date" [(ngModel)]="s.validTo" name="to" />
        </div>
        <div class="gw-field full">
          <label>Enveloppe</label>
          <p-dropdown [options]="enveloppes" [(ngModel)]="s.envelopeMode" name="env" optionLabel="label" optionValue="value" styleClass="w-full" />
        </div>
        <div class="gw-field full">
          <label>Description</label>
          <input pInputText [(ngModel)]="s.description" name="desc" maxlength="500" />
        </div>
        @if (error()) {
          <p class="gw-msg error full">{{ error() }}</p>
        }
        <div class="actions full">
          <button pButton type="button" class="p-button-text" label="Annuler" (click)="visibleChange.emit(false)"></button>
          <button pButton type="submit" [label]="grille ? 'Enregistrer' : 'Créer'" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .gw-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem 1rem; }
    .gw-field { display: flex; flex-direction: column; gap: 0.25rem; }
    .gw-field.full, .full { grid-column: 1 / -1; }
    .actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem; }
    label { font-size: 0.8rem; color: #64748b; }
  `
})
export class GrilleCommissionFormDialogComponent implements OnChanges {
  private readonly api = inject(CommissionService);

  @Input() visible = false;
  @Input() grille: GrilleCommission | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() saved = new EventEmitter<GrilleCommission>();

  readonly enveloppes = ENVELOPPES;
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  s = this.vide();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] || changes['grille']) {
      this.error.set(null);
      this.s = this.grille
        ? {
            code: this.grille.code,
            libelle: this.grille.libelle,
            description: this.grille.description ?? '',
            operationType: this.grille.operationType,
            productCode: this.grille.productCode ?? '',
            channelCode: this.grille.channelCode ?? '',
            currency: this.grille.currency,
            validFrom: this.grille.validFrom,
            validTo: this.grille.validTo ?? '',
            envelopeMode: this.grille.envelopeMode
          }
        : this.vide();
    }
  }

  enregistrer(): void {
    this.error.set(null);
    if (!this.s.code.trim() || !this.s.libelle.trim() || !this.s.operationType.trim()) {
      this.error.set('Code, libellé et opération sont obligatoires.');
      return;
    }
    this.saving.set(true);
    if (this.grille) {
      const body: UpdateGrilleRequest = {
        libelle: this.s.libelle.trim(),
        description: this.s.description || null,
        operationType: this.s.operationType.trim(),
        productCode: this.s.productCode || null,
        channelCode: this.s.channelCode || null,
        currency: this.s.currency || 'XOF',
        validFrom: this.s.validFrom || null,
        validTo: this.s.validTo || null,
        envelopeMode: this.s.envelopeMode
      };
      this.api.majGrille(this.grille.id, body).subscribe({
        next: (g) => this.ok(g),
        error: (e: { message?: string }) => this.fail(e)
      });
    } else {
      const body: CreateGrilleRequest = {
        code: this.s.code.trim(),
        libelle: this.s.libelle.trim(),
        description: this.s.description || null,
        operationType: this.s.operationType.trim(),
        productCode: this.s.productCode || null,
        channelCode: this.s.channelCode || null,
        currency: this.s.currency || 'XOF',
        validFrom: this.s.validFrom || null,
        validTo: this.s.validTo || null,
        envelopeMode: this.s.envelopeMode
      };
      this.api.creerGrille(body).subscribe({
        next: (g) => this.ok(g),
        error: (e: { message?: string }) => this.fail(e)
      });
    }
  }

  private ok(g: GrilleCommission): void {
    this.saving.set(false);
    this.saved.emit(g);
    this.visibleChange.emit(false);
  }

  private fail(e: { message?: string }): void {
    this.saving.set(false);
    this.error.set(e?.message ?? 'Enregistrement impossible.');
  }

  private vide() {
    return {
      code: '',
      libelle: '',
      description: '',
      operationType: 'CASH_OUT',
      productCode: '',
      channelCode: '',
      currency: 'XOF',
      validFrom: new Date().toISOString().slice(0, 10),
      validTo: '',
      envelopeMode: 'FULL_FEES'
    };
  }
}
