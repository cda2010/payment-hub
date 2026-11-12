import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import {
  IdentityService,
  KINDS_PARTY,
  Party,
  PartyKind,
  PartyRequest
} from '../../core/service/identity.service';

interface Saisie {
  code: string;
  kind: PartyKind;
  displayName: string;
  phone: string;
  email: string;
  externalRef: string;
  tenantId: string;
  active: boolean;
}

/** Création ou modification d'une party (personne / organisation). */
@Component({
  selector: 'app-party-form-dialog',
  standalone: true,
  imports: [FormsModule, ButtonModule, CheckboxModule, DialogModule, DropdownModule, InputTextModule],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="visibleChange.emit($event)"
      [header]="party ? 'Modifier ' + party.code : 'Nouvelle identité'"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '36rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="enregistrer()">
        <div class="gw-field">
          <label for="py-code">Code *</label>
          <input
            pInputText
            id="py-code"
            name="code"
            [(ngModel)]="s.code"
            maxlength="64"
            placeholder="ex. CLI-001"
            [readonly]="!!party"
          />
          <small class="muted">{{ party ? 'Le code ne change pas.' : 'Mis en majuscules à l’enregistrement.' }}</small>
        </div>
        <div class="gw-field">
          <label for="py-kind">Type *</label>
          <p-dropdown
            inputId="py-kind"
            name="kind"
            [options]="kinds"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="s.kind"
            styleClass="w-full"
            appendTo="body"
          />
        </div>
        <div class="gw-field full">
          <label for="py-name">Nom / raison sociale *</label>
          <input pInputText id="py-name" name="displayName" [(ngModel)]="s.displayName" maxlength="200" />
        </div>
        <div class="gw-field">
          <label for="py-phone">Téléphone</label>
          <input pInputText id="py-phone" name="phone" [(ngModel)]="s.phone" maxlength="30" />
        </div>
        <div class="gw-field">
          <label for="py-email">E-mail</label>
          <input pInputText id="py-email" name="email" [(ngModel)]="s.email" maxlength="180" />
        </div>
        <div class="gw-field">
          <label for="py-ext">Réf. externe</label>
          <input pInputText id="py-ext" name="externalRef" [(ngModel)]="s.externalRef" maxlength="40" />
        </div>
        <div class="gw-field">
          <label for="py-tenant">Tenant</label>
          <input
            pInputText
            id="py-tenant"
            name="tenantId"
            [(ngModel)]="s.tenantId"
            maxlength="64"
            placeholder="CORIS_DEMO"
            [readonly]="!!party"
          />
        </div>
        @if (party) {
          <div class="gw-field full check">
            <p-checkbox inputId="py-active" name="active" [(ngModel)]="s.active" [binary]="true" />
            <label for="py-active">Active</label>
          </div>
        }
        @if (erreur()) {
          <p class="gw-msg error full">{{ erreur() }}</p>
        }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="visibleChange.emit(false)"></button>
          <button pButton type="submit" [label]="party ? 'Enregistrer' : 'Créer'" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .muted { color: #64748b; font-size: 0.78rem; }
    .check { display: flex; align-items: center; gap: 0.5rem; flex-direction: row !important; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class PartyFormDialogComponent implements OnChanges {
  private readonly identity = inject(IdentityService);

  @Input() visible = false;
  @Input() party: Party | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() saved = new EventEmitter<Party>();

  readonly kinds = KINDS_PARTY;
  readonly saving = signal(false);
  readonly erreur = signal<string | null>(null);

  s: Saisie = this.vide();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] || changes['party']) {
      if (this.visible) {
        this.erreur.set(null);
        this.s = this.party
          ? {
              code: this.party.code,
              kind: this.party.kind,
              displayName: this.party.displayName,
              phone: this.party.phone ?? '',
              email: this.party.email ?? '',
              externalRef: this.party.externalRef ?? '',
              tenantId: this.party.tenantId,
              active: this.party.active
            }
          : this.vide();
      }
    }
  }

  enregistrer(): void {
    if (!this.s.displayName.trim() || (!this.party && !this.s.code.trim())) {
      this.erreur.set('Code et nom sont obligatoires.');
      return;
    }
    const body: PartyRequest = {
      tenantId: this.s.tenantId.trim() || null,
      code: this.s.code.trim().toUpperCase(),
      kind: this.s.kind,
      displayName: this.s.displayName.trim(),
      phone: this.s.phone.trim() || null,
      email: this.s.email.trim() || null,
      externalRef: this.s.externalRef.trim() || null,
      active: this.party ? this.s.active : true
    };
    this.saving.set(true);
    this.erreur.set(null);
    const req$ = this.party
      ? this.identity.modifierParty(this.party.id, body)
      : this.identity.creerParty(body);
    req$.subscribe({
      next: (p) => {
        this.saving.set(false);
        this.visibleChange.emit(false);
        this.saved.emit(p);
      },
      error: (e: { message?: string }) => {
        this.saving.set(false);
        this.erreur.set(e?.message ?? 'Enregistrement impossible.');
      }
    });
  }

  private vide(): Saisie {
    return {
      code: '',
      kind: 'PERSON',
      displayName: '',
      phone: '',
      email: '',
      externalRef: '',
      tenantId: 'CORIS_DEMO',
      active: true
    };
  }
}
