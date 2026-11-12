import { Component, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AccountAlias, AliasService, AliasType } from '../../core/service/alias.service';

const TYPES: { value: AliasType; label: string }[] = [
  { value: 'IBAN', label: 'IBAN' },
  { value: 'WALLET', label: 'Wallet' },
  { value: 'TILL', label: 'Till / caisse' },
  { value: 'INTERNAL', label: 'Interne' },
  { value: 'PHONE', label: 'Téléphone' }
];

/** Alias de comptes (payment-alias) rattachés à une unité d'organisation du réseau. */
@Component({
  selector: 'app-unite-aliases',
  standalone: true,
  imports: [
    FormsModule,
    ButtonModule,
    DialogModule,
    DropdownModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule
  ],
  template: `
    <div class="gw-panel">
      <div class="entete">
        <h3>Alias de comptes ({{ aliases().length }})</h3>
        @if (peutEcrire) {
          <button pButton type="button" class="p-button-sm" icon="pi pi-plus" label="Ajouter un alias" (click)="ouvrir()"></button>
        }
      </div>
      <p class="muted">Comptes CBS / Coris / till liés à cette unité (module payment-alias, propriétaire UNITE).</p>

      @if (loading()) {
        <div class="gw-loading compact">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '28px', height: '28px' }" />
        </div>
      } @else if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      } @else {
        <p-table [value]="aliases()" styleClass="p-datatable-sm" [rowHover]="true" dataKey="id">
          <ng-template pTemplate="header">
            <tr>
              <th>Code</th>
              <th>Libellé</th>
              <th>Type</th>
              <th>Référence</th>
              <th>Devise</th>
              <th>Fournisseur</th>
              @if (peutEcrire) { <th></th> }
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-a>
            <tr>
              <td class="code">{{ a.aliasCode }}</td>
              <td>{{ a.label }}</td>
              <td><p-tag [value]="a.aliasType" severity="secondary" /></td>
              <td><code>{{ a.externalRef }}</code></td>
              <td>{{ a.currency }}</td>
              <td>{{ a.providerCode || '—' }}</td>
              @if (peutEcrire) {
                <td class="actions">
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm p-button-danger"
                    icon="pi pi-ban"
                    title="Désactiver"
                    [loading]="enCours() === a.id"
                    (click)="desactiver(a)"
                  ></button>
                </td>
              }
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td [attr.colspan]="peutEcrire ? 7 : 6">Aucun alias pour cette unité.</td></tr>
          </ng-template>
        </p-table>
      }
    </div>

    <p-dialog
      [(visible)]="dialogue"
      header="Nouvel alias pour cette unité"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '36rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="creer()">
        <div class="gw-field">
          <label for="a-code">Code *</label>
          <input pInputText id="a-code" name="code" [(ngModel)]="s.code" maxlength="80" placeholder="ex. ALIAS-AG-OUAGA-TILL" />
        </div>
        <div class="gw-field">
          <label for="a-type">Type *</label>
          <p-dropdown
            inputId="a-type"
            name="type"
            [options]="types"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="s.type"
            styleClass="w-full"
            appendTo="body"
          />
        </div>
        <div class="gw-field full">
          <label for="a-label">Libellé</label>
          <input pInputText id="a-label" name="label" [(ngModel)]="s.label" maxlength="200" />
        </div>
        <div class="gw-field full">
          <label for="a-ref">Référence core *</label>
          <input pInputText id="a-ref" name="ref" [(ngModel)]="s.ref" maxlength="120" placeholder="IBAN, walletId, till…" />
        </div>
        <div class="gw-field">
          <label for="a-devise">Devise</label>
          <input pInputText id="a-devise" name="devise" [(ngModel)]="s.devise" maxlength="3" />
        </div>
        <div class="gw-field">
          <label for="a-provider">Fournisseur</label>
          <input pInputText id="a-provider" name="provider" [(ngModel)]="s.provider" maxlength="50" placeholder="CORIS_BANK…" />
        </div>
        @if (erreurDialogue()) {
          <p class="gw-msg error full">{{ erreurDialogue() }}</p>
        }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="dialogue = false"></button>
          <button pButton type="submit" label="Créer" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .entete { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; }
    .entete h3 { margin: 0; }
    .muted { color: #64748b; font-size: 0.8rem; margin: 0.35rem 0 0.75rem; }
    .code { font-weight: 600; font-family: ui-monospace, monospace; }
    .actions { text-align: right; white-space: nowrap; }
    .gw-loading.compact { padding: 0.75rem; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class UniteAliasesComponent implements OnChanges {
  private readonly aliasApi = inject(AliasService);

  @Input({ required: true }) uniteId!: string;
  @Input() peutEcrire = false;
  /** Devise de la zone, préremplie à la création. */
  @Input() devise = 'XOF';

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly aliases = signal<AccountAlias[]>([]);
  readonly enCours = signal<string | null>(null);
  readonly saving = signal(false);
  readonly erreurDialogue = signal<string | null>(null);

  readonly types = TYPES;
  dialogue = false;
  s = this.vide();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['uniteId'] && this.uniteId) {
      this.charger();
    }
  }

  ouvrir(): void {
    this.s = this.vide();
    this.erreurDialogue.set(null);
    this.dialogue = true;
  }

  creer(): void {
    if (!this.s.code.trim() || !this.s.ref.trim()) {
      this.erreurDialogue.set('Code et référence sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.erreurDialogue.set(null);
    this.aliasApi
      .creer({
        aliasCode: this.s.code.trim(),
        label: this.s.label.trim() || this.s.code.trim(),
        aliasType: this.s.type,
        externalRef: this.s.ref.trim(),
        currency: this.s.devise.trim() || 'XOF',
        ownerKind: 'UNITE',
        ownerId: this.uniteId,
        providerCode: this.s.provider.trim() || null
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.dialogue = false;
          this.charger();
        },
        error: (e: { message?: string }) => {
          this.saving.set(false);
          this.erreurDialogue.set(e?.message ?? 'Création impossible.');
        }
      });
  }

  desactiver(a: AccountAlias): void {
    this.enCours.set(a.id);
    this.aliasApi.desactiver(a.id).subscribe({
      next: () => {
        this.enCours.set(null);
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.enCours.set(null);
        this.error.set(e?.message ?? 'Désactivation impossible.');
      }
    });
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.aliasApi.lister({ ownerKind: 'UNITE', ownerId: this.uniteId }).subscribe({
      next: (list) => {
        this.aliases.set(list);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.aliases.set([]);
        this.error.set(
          e?.message ??
            'Alias indisponibles (payment-alias doit tourner sur le port 8097).'
        );
      }
    });
  }

  private vide() {
    return {
      code: '',
      label: '',
      type: 'TILL' as AliasType,
      ref: '',
      devise: this.devise || 'XOF',
      provider: ''
    };
  }
}
