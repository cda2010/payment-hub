import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { PaysRef, PaysRequest, ProviderService } from '../../core/service/provider.service';

interface SaisiePays {
  code: string;
  nom: string;
  indicatif: string;
  devise: string;
  actif: boolean;
  ordre: number;
}

/**
 * Référentiel des pays utilisé par le formulaire fournisseur (nom identique à l'omnicanal).
 */
@Component({
  selector: 'app-pays-admin',
  standalone: true,
  imports: [
    FormsModule,
    ButtonModule,
    CheckboxModule,
    DialogModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule,
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Pays</h1>
          <p class="subtitle">
            Référentiel des pays. Le nom est celui attendu par l'omnicanal
            @if (!loading()) {
              · <strong>{{ pays().length }}</strong> pays
            }
          </p>
        </div>
        <div class="header-actions">
          <button pButton type="button" icon="pi pi-plus" label="Nouveau pays" (click)="ouvrir(null)"></button>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else {
        <div class="gw-panel">
          <p-table [value]="pays()" [rowHover]="true" dataKey="id" styleClass="p-datatable-sm">
            <ng-template pTemplate="header">
              <tr>
                <th>Code</th>
                <th>Nom</th>
                <th>Indicatif</th>
                <th>Devise</th>
                <th class="num">Ordre</th>
                <th>Statut</th>
                <th></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-row>
              <tr [class.inactif]="!row.actif">
                <td class="code">{{ row.code }}</td>
                <td>{{ row.nom }}</td>
                <td class="code">{{ row.indicatif }}</td>
                <td>{{ row.devise }}</td>
                <td class="num">{{ row.ordre }}</td>
                <td>
                  <p-tag [value]="row.actif ? 'Actif' : 'Inactif'" [severity]="row.actif ? 'success' : 'secondary'" />
                </td>
                <td class="actions">
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm"
                    icon="pi pi-pencil"
                    (click)="ouvrir(row)"
                  ></button>
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr>
                <td colspan="7">Aucun pays.</td>
              </tr>
            </ng-template>
          </p-table>
        </div>
      }
    </div>

    <p-dialog
      [header]="editionId() ? 'Modifier le pays' : 'Nouveau pays'"
      [visible]="dialog()"
      (visibleChange)="dialog.set($event)"
      [modal]="true"
      [style]="{ width: '28rem' }"
    >
      <div class="gw-form-grid">
        <div class="gw-field">
          <label for="paysCode">Code</label>
          <input pInputText id="paysCode" [(ngModel)]="saisie.code" maxlength="8" class="w-full" />
        </div>
        <div class="gw-field">
          <label for="paysNom">Nom</label>
          <input pInputText id="paysNom" [(ngModel)]="saisie.nom" maxlength="80" class="w-full" />
        </div>
        <div class="gw-field">
          <label for="paysIndicatif">Indicatif</label>
          <input pInputText id="paysIndicatif" [(ngModel)]="saisie.indicatif" maxlength="8" placeholder="+226" class="w-full" />
        </div>
        <div class="gw-field">
          <label for="paysDevise">Devise</label>
          <input pInputText id="paysDevise" [(ngModel)]="saisie.devise" maxlength="10" class="w-full" />
        </div>
        <div class="gw-field">
          <label for="paysOrdre">Ordre</label>
          <input pInputText id="paysOrdre" type="number" [(ngModel)]="saisie.ordre" class="w-full" />
        </div>
        <div class="gw-field">
          <p-checkbox [(ngModel)]="saisie.actif" [binary]="true" inputId="paysActif" label="Actif"></p-checkbox>
        </div>
      </div>
      @if (saveError()) {
        <p class="gw-msg error">{{ saveError() }}</p>
      }
      <ng-template pTemplate="footer">
        <button pButton type="button" class="p-button-text" label="Annuler" (click)="dialog.set(false)"></button>
        <button
          pButton
          type="button"
          label="Enregistrer"
          [loading]="saving()"
          (click)="enregistrer()"
        ></button>
      </ng-template>
    </p-dialog>
  `,
})
export class PaysAdminComponent implements OnInit {
  private readonly api = inject(ProviderService);

  readonly pays = signal<PaysRef[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly dialog = signal(false);
  readonly saving = signal(false);
  readonly saveError = signal('');
  readonly editionId = signal<string | null>(null);

  saisie: SaisiePays = this.vide();

  ngOnInit(): void {
    this.charger();
  }

  ouvrir(row: PaysRef | null): void {
    this.saveError.set('');
    this.editionId.set(row?.id ?? null);
    this.saisie = row
      ? {
          code: row.code,
          nom: row.nom,
          indicatif: row.indicatif ?? '',
          devise: row.devise,
          actif: row.actif,
          ordre: row.ordre,
        }
      : this.vide();
    this.dialog.set(true);
  }

  enregistrer(): void {
    const body: PaysRequest = {
      code: this.saisie.code.trim(),
      nom: this.saisie.nom.trim(),
      indicatif: this.saisie.indicatif.trim(),
      devise: this.saisie.devise.trim() || 'XOF',
      actif: this.saisie.actif,
      ordre: Number(this.saisie.ordre) || 0,
    };
    if (!body.code || !body.nom || !body.indicatif) {
      this.saveError.set('Le code, le nom et l’indicatif sont obligatoires.');
      return;
    }
    this.saving.set(true);
    this.saveError.set('');
    const id = this.editionId();
    const request = id ? this.api.updatePays(id, body) : this.api.createPays(body);
    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialog.set(false);
        this.charger();
      },
      error: (err: { message?: string }) => {
        this.saving.set(false);
        this.saveError.set(err?.message || "Enregistrement impossible.");
      },
    });
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.listPays().subscribe({
      next: (rows) => {
        this.pays.set(rows ?? []);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.loading.set(false);
        this.error.set(err?.message || 'Impossible de charger les pays.');
      },
    });
  }

  private vide(): SaisiePays {
    return { code: '', nom: '', indicatif: '', devise: 'XOF', actif: true, ordre: 0 };
  }
}
