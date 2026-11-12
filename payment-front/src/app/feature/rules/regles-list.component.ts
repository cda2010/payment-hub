import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MultiSelectModule } from 'primeng/multiselect';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TabViewModule } from 'primeng/tabview';
import { TagModule } from 'primeng/tag';
import { GroupWrite, RuleGroupSummary, RuleSummary, RuleWrite, RulesService } from '../../core/service/rules.service';

interface SaisieGroupe {
  code: string;
  libelle: string;
  description: string;
  transactionType: string;
  productCode: string;
  evaluationStrategy: string;
  status: string;
  ruleCodes: string[];
}

function videGroupe(): SaisieGroupe {
  return {
    code: '',
    libelle: '',
    description: '',
    transactionType: '',
    productCode: '',
    evaluationStrategy: 'STOP_ON_REJECT',
    status: 'ACTIVE',
    ruleCodes: []
  };
}

function depuisGroupe(groupe: RuleGroupSummary): SaisieGroupe {
  return {
    code: groupe.code,
    libelle: groupe.libelle,
    description: groupe.description ?? '',
    transactionType: groupe.transactionType ?? '',
    productCode: groupe.productCode ?? '',
    evaluationStrategy: groupe.evaluationStrategy,
    status: groupe.status,
    ruleCodes: [...groupe.items].sort((a, b) => a.sortOrder - b.sortOrder).map((item) => item.ruleCode)
  };
}

interface SaisieRegle {
  code: string;
  libelle: string;
  legalRef: string;
  message: string;
  sourceType: string;
  sourcePeriod: string;
  sourceScope: string;
  operator: string;
  includeCurrent: boolean;
  priority: number;
  transactionType: string;
  groupCode: string;
  limitValue: number | null;
  effectiveFrom: string;
  identified: boolean;
  excludePro: boolean;
  creditRecu: boolean;
}

const METRIQUES = [
  { label: 'Détention (solde + opération)', value: 'PROJECTED_HOLDING' },
  { label: 'Cumul des montants', value: 'CUMUL_AMOUNT' },
  { label: 'Cumul du nombre', value: 'CUMUL_COUNT' },
  { label: 'Montant de l\'opération', value: 'TRANSACTION_AMOUNT' },
  { label: 'Nombre d\'opérations', value: 'TRANSACTION_COUNT' },
  { label: 'Solde après débit', value: 'PROJECTED_BALANCE' },
  { label: 'Solde du compte', value: 'WALLET_BALANCE' },
  { label: 'Niveau KYC', value: 'KYC_LEVEL' },
  { label: 'Statut du client', value: 'CUSTOMER_STATUS' },
  { label: 'Type d\'opération', value: 'TRANSACTION_TYPE' },
  { label: 'Pays', value: 'COUNTRY' },
  { label: 'Canal', value: 'CHANNEL' },
  { label: 'Attribut libre', value: 'CUSTOM_ATTRIBUTE' }
];

const PERIODES = [
  { label: 'Jour', value: 'DAY' },
  { label: 'Semaine', value: 'WEEK' },
  { label: 'Mois', value: 'MONTH' },
  { label: 'Année', value: 'YEAR' },
  { label: '24 heures glissantes', value: 'ROLLING_24H' },
  { label: '30 jours glissants', value: 'ROLLING_30D' }
];

const PERIMETRES = [
  { label: 'Client', value: 'CUSTOMER' },
  { label: 'Compte', value: 'WALLET' },
  { label: 'Compte technique', value: 'ACCOUNT' },
  { label: 'Agent', value: 'AGENT' },
  { label: 'Marchand', value: 'MERCHANT' },
  { label: 'Caisse', value: 'TILL' },
  { label: 'Unité', value: 'UNITE' }
];

const OPERATEURS = [
  { label: 'inférieur ou égal', value: '<=' },
  { label: 'inférieur', value: '<' },
  { label: 'supérieur ou égal', value: '>=' },
  { label: 'supérieur', value: '>' },
  { label: 'égal', value: '=' },
  { label: 'différent', value: '!=' }
];

const OPERATIONS = ['CASH_IN', 'CASH_OUT', 'ACCOUNT_TRANSFER'].map((v) => ({ label: v, value: v }));
const STRATEGIES = [
  { label: 'Arrêt au premier refus', value: 'STOP_ON_REJECT' },
  { label: 'Évaluer toutes les règles', value: 'EVALUATE_ALL' }
];
const STATUTS_GROUPE = [
  { label: 'Actif', value: 'ACTIVE' },
  { label: 'Inactif', value: 'INACTIVE' }
];

@Component({
  selector: 'app-regles-list',
  standalone: true,
  imports: [
    FormsModule, RouterLink, ButtonModule, CheckboxModule, DialogModule, DropdownModule,
    InputNumberModule, InputTextModule, InputTextareaModule, MultiSelectModule, ProgressSpinnerModule, TableModule, TabViewModule, TagModule
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Moteur de règles</h1>
          <p class="subtitle">
            Instruction BCEAO n° 020, en vigueur le 2 novembre 2026 : détention 4 000 000 XOF
            et rechargement mensuel 15 000 000 XOF pour un client identifié.
          </p>
        </div>
        <div class="header-actions">
          <a pButton class="p-button-outlined" icon="pi pi-play" label="Simulateur" routerLink="/admin/rules/simulateur"></a>
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
        <p-tabView styleClass="rules-tabs">
          <p-tabPanel header="Limites">
            <div class="panel-head">
              <p>{{ regles().length }} limites</p>
              <button pButton type="button" icon="pi pi-plus" label="Nouvelle limite" (click)="ouvrir(null)"></button>
            </div>
            <p-table
              [value]="regles()"
              styleClass="p-datatable-sm"
              dataKey="id"
              [paginator]="true"
              [rows]="10"
              [rowsPerPageOptions]="[10, 25, 50]"
              [showCurrentPageReport]="true"
              currentPageReportTemplate="{first}–{last} sur {totalRecords}"
              paginatorDropdownAppendTo="body"
            >
            <ng-template pTemplate="header">
              <tr>
                <th>Code</th>
                <th>Libellé</th>
                <th class="num">Seuil (XOF)</th>
                <th>Métrique</th>
                <th>Effet</th>
                <th>Statut</th>
                <th></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-r>
              <tr>
                <td class="code">{{ r.code }}</td>
                <td>
                  {{ r.libelle }}
                  @if (r.legalRef) {
                    <div class="ref">{{ r.legalRef }}</div>
                  }
                </td>
                <td class="num">{{ formatSeuil(r.limitValue) }}</td>
                <td>
                  {{ libelleMetrique(r) }}
                  <div class="ref">{{ parametresMetrique(r) }}</div>
                </td>
                <td>{{ r.effectiveFrom || '—' }}</td>
                <td>
                  <p-tag [value]="r.status" [severity]="r.status === 'PUBLIEE' ? 'success' : 'secondary'" />
                </td>
                <td class="actions">
                  <button pButton type="button" class="p-button-text" icon="pi pi-pencil" label="Modifier" (click)="ouvrir(r)"></button>
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td colspan="7">Aucune limite. Démarrez payment-rules (:8099) et sa base (:5441).</td></tr>
            </ng-template>
          </p-table>
          </p-tabPanel>

          <p-tabPanel header="Groupes">
          <div class="panel-head">
            <p>{{ groupes().length }} groupes</p>
            <button pButton type="button" icon="pi pi-plus" label="Nouveau groupe" (click)="ouvrirGroupe(null)"></button>
          </div>
          <p-table
            [value]="groupes()"
            styleClass="p-datatable-sm"
            dataKey="id"
            [paginator]="true"
            [rows]="10"
            [rowsPerPageOptions]="[10, 25, 50]"
            [showCurrentPageReport]="true"
            currentPageReportTemplate="{first}–{last} sur {totalRecords}"
            paginatorDropdownAppendTo="body"
          >
            <ng-template pTemplate="header">
              <tr>
                <th>Code</th>
                <th>Libellé</th>
                <th>Opération</th>
                <th>Stratégie</th>
                <th>Statut</th>
                <th></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-g>
              <tr>
                <td class="code">{{ g.code }}</td>
                <td>{{ g.libelle }}</td>
                <td>{{ g.transactionType || '—' }}</td>
                <td>{{ g.evaluationStrategy }}</td>
                <td>
                  <p-tag [value]="g.status" [severity]="g.status === 'ACTIVE' ? 'success' : 'secondary'" />
                </td>
                <td class="actions">
                  <button pButton type="button" class="p-button-text" icon="pi pi-pencil" label="Modifier" (click)="ouvrirGroupe(g)"></button>
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td colspan="6">Aucun groupe.</td></tr>
            </ng-template>
          </p-table>
          </p-tabPanel>
        </p-tabView>
      }

      <p-dialog
        [visible]="dialog()"
        (visibleChange)="dialog.set($event)"
        [header]="edition() ? 'Modifier le plafond' : 'Nouveau plafond'"
        [modal]="true"
        [draggable]="false"
        [style]="{ width: '52rem', maxWidth: '94vw' }"
        [contentStyle]="{ maxHeight: '82vh' }"
      >
        <form class="gw-form-grid" (ngSubmit)="enregistrer()">
          <div class="gw-field">
            <label>Code *</label>
            <input pInputText [(ngModel)]="saisie.code" name="code" maxlength="64" [readonly]="edition()" />
          </div>
          <div class="gw-field">
            <label>Libellé *</label>
            <input pInputText [(ngModel)]="saisie.libelle" name="libelle" maxlength="200" />
          </div>
          <div class="gw-field">
            <label>{{ estNombre(saisie.sourceType) ? 'Nombre maximum *' : 'Seuil (XOF)' }}</label>
            <p-inputNumber [(ngModel)]="saisie.limitValue" name="seuil" [min]="0" [maxFractionDigits]="0" styleClass="w-full" />
          </div>
          <div class="gw-field">
            <label>Métrique *</label>
            <p-dropdown [options]="metriques" [(ngModel)]="saisie.sourceType" (onChange)="appliquerMetrique($event)" name="metrique" optionLabel="label" optionValue="value" styleClass="w-full" appendTo="body" [baseZIndex]="12000" />
          </div>
          @if (estCumul(saisie.sourceType)) {
            <div class="gw-field">
              <label>Période *</label>
              <p-dropdown [options]="periodes" [(ngModel)]="saisie.sourcePeriod" name="periode" optionLabel="label" optionValue="value" styleClass="w-full" appendTo="body" [baseZIndex]="12000" />
            </div>
          }
          @if (aPerimetre(saisie.sourceType)) {
            <div class="gw-field">
              <label>Périmètre *</label>
              <p-dropdown [options]="perimetres" [(ngModel)]="saisie.sourceScope" name="perimetre" optionLabel="label" optionValue="value" styleClass="w-full" appendTo="body" [baseZIndex]="12000" />
            </div>
          }
          <div class="gw-field">
            <label>Opérateur *</label>
            <p-dropdown [options]="operateurs" [(ngModel)]="saisie.operator" name="operateur" optionLabel="label" optionValue="value" styleClass="w-full" appendTo="body" [baseZIndex]="12000" />
          </div>
          @if (estCumul(saisie.sourceType)) {
            <div class="gw-field check">
              <p-checkbox [(ngModel)]="saisie.includeCurrent" name="includeCurrent" [binary]="true" inputId="includeCurrent" />
              <label for="includeCurrent">Inclure l'opération en cours</label>
            </div>
          }
          <div class="gw-field">
            <label>Opération *</label>
            <p-dropdown [options]="operations" [(ngModel)]="saisie.transactionType" name="operation" optionLabel="label" optionValue="value" styleClass="w-full" [editable]="true" appendTo="body" [baseZIndex]="12000" />
          </div>
          @if (!edition()) {
            <div class="gw-field">
              <label>Groupe *</label>
              <p-dropdown [options]="groupes()" [(ngModel)]="saisie.groupCode" name="groupe" optionLabel="code" optionValue="code" styleClass="w-full" placeholder="CASH_IN_STANDARD" appendTo="body" [baseZIndex]="12000" />
            </div>
          }
          <div class="gw-field">
            <label>Date d'effet *</label>
            <input pInputText type="date" [(ngModel)]="saisie.effectiveFrom" name="effet" />
          </div>
          <div class="gw-field">
            <label>Référence juridique</label>
            <input pInputText [(ngModel)]="saisie.legalRef" name="legal" maxlength="200" />
          </div>
          <div class="gw-field full">
            <label>Message de refus *</label>
            <input pInputText [(ngModel)]="saisie.message" name="message" maxlength="500" />
          </div>
          <div class="gw-field check full">
            <p-checkbox [(ngModel)]="saisie.identified" name="identified" [binary]="true" inputId="identified" />
            <label for="identified">Client identifié (STANDARD, ENHANCED, FULL)</label>
          </div>
          <div class="gw-field check full">
            <p-checkbox [(ngModel)]="saisie.excludePro" name="excludePro" [binary]="true" inputId="excludePro" />
            <label for="excludePro">Hors distributeur et accepteur professionnels</label>
          </div>
          @if (controleCredit(saisie.sourceType)) {
            <div class="gw-field check full">
              <p-checkbox [(ngModel)]="saisie.creditRecu" name="creditRecu" [binary]="true" inputId="creditRecu" />
              <label for="creditRecu">Contrôler aussi le transfert reçu (hors rechargement mensuel)</label>
            </div>
          }
          @if (formError()) {
            <p class="gw-msg error full">{{ formError() }}</p>
          }
          <div class="dialog-actions full">
            <button pButton type="button" class="p-button-text" label="Annuler" (click)="dialog.set(false)"></button>
            <button pButton type="submit" label="Enregistrer" [loading]="saving()"></button>
          </div>
        </form>
      </p-dialog>

      <p-dialog
        [visible]="dialogGroupe()"
        (visibleChange)="dialogGroupe.set($event)"
        [header]="groupeEdition() ? 'Modifier le groupe' : 'Nouveau groupe'"
        [modal]="true"
        [draggable]="false"
        [style]="{ width: '72rem', maxWidth: '94vw' }"
        [contentStyle]="{ maxHeight: '82vh' }"
      >
        <form class="gw-form-grid" (ngSubmit)="enregistrerGroupe()">
          <div class="gw-field">
            <label>Code *</label>
            <input pInputText [(ngModel)]="saisieGroupe.code" name="gcode" maxlength="64" [readonly]="!!groupeEdition()" />
          </div>
          <div class="gw-field">
            <label>Libellé *</label>
            <input pInputText [(ngModel)]="saisieGroupe.libelle" name="glibelle" maxlength="200" />
          </div>
          <div class="gw-field full">
            <label>Description</label>
            <textarea pInputTextarea [(ngModel)]="saisieGroupe.description" name="gdesc" maxlength="500" rows="3"></textarea>
          </div>
          <div class="gw-field">
            <label>Opération</label>
            <p-dropdown [options]="operations" [(ngModel)]="saisieGroupe.transactionType" name="gop" optionLabel="label" optionValue="value" styleClass="w-full" [editable]="true" [showClear]="true" appendTo="body" [baseZIndex]="12000" />
          </div>
          <div class="gw-field">
            <label>Code produit</label>
            <input pInputText [(ngModel)]="saisieGroupe.productCode" name="gprod" maxlength="80" />
          </div>
          <div class="gw-field">
            <label>Stratégie *</label>
            <p-dropdown [options]="strategies" [(ngModel)]="saisieGroupe.evaluationStrategy" name="gstrat" optionLabel="label" optionValue="value" styleClass="w-full" appendTo="body" [baseZIndex]="12000" />
          </div>
          <div class="gw-field">
            <label>Statut *</label>
            <p-dropdown [options]="statutsGroupe" [(ngModel)]="saisieGroupe.status" name="gstat" optionLabel="label" optionValue="value" styleClass="w-full" appendTo="body" [baseZIndex]="12000" />
          </div>
          <div class="gw-field full">
            <label>Règles évaluées</label>
            <p-multiSelect
              [options]="reglesPubliees()"
              [ngModel]="saisieGroupe.ruleCodes"
              (ngModelChange)="ordonnerRegles($event)"
              name="gregles"
              optionLabel="libelle"
              optionValue="code"
              dataKey="code"
              display="chip"
              styleClass="w-full"
              appendTo="body"
              [baseZIndex]="12000"
              [scrollHeight]="'320px'"
              placeholder="Choisir les règles publiées"
            />
          </div>
          @if (saisieGroupe.ruleCodes.length) {
            <ol class="ordre full">
              @for (code of saisieGroupe.ruleCodes; track code; let i = $index) {
                <li>
                  <span class="code">{{ code }}</span>
                  <button pButton type="button" class="p-button-text" icon="pi pi-arrow-up" [disabled]="i === 0" (click)="deplacerRegle(i, -1)"></button>
                  <button pButton type="button" class="p-button-text" icon="pi pi-arrow-down" [disabled]="i === saisieGroupe.ruleCodes.length - 1" (click)="deplacerRegle(i, 1)"></button>
                </li>
              }
            </ol>
          }
          @if (groupeError()) {
            <p class="gw-msg error full">{{ groupeError() }}</p>
          }
          <div class="dialog-actions full">
            <button pButton type="button" class="p-button-text" label="Annuler" (click)="dialogGroupe.set(false)"></button>
            <button pButton type="submit" label="Enregistrer" [loading]="savingGroupe()"></button>
          </div>
        </form>
      </p-dialog>
    </div>
  `,
  styles: `
    .code { font-family: ui-monospace, monospace; font-weight: 600; }
    .num { text-align: right; font-variant-numeric: tabular-nums; }
    .ref { color: var(--text-color-secondary, #64748b); font-size: 0.8rem; margin-top: 0.15rem; }
    .actions { width: 8rem; text-align: right; white-space: nowrap; }
    .panel-head { display: flex; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 0.75rem; }
    .panel-head p { margin: 0; color: var(--text-color-secondary, #64748b); }
    .ordre { list-style: none; margin: 0; padding: 0.25rem 0 0; display: flex; flex-direction: column; gap: 0.25rem; max-height: 16rem; overflow: auto; border: 1px solid var(--surface-border, #e2e8f0); border-radius: 8px; }
    .ordre li { display: flex; align-items: center; gap: 0.5rem; padding: 0.15rem 0.5rem; }
    .check { display: flex; align-items: center; gap: 0.5rem; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.25rem; }
    :host ::ng-deep .rules-tabs.p-tabview { display: block; }
    :host ::ng-deep .rules-tabs .p-tabview-nav-container { overflow: visible; }
    :host ::ng-deep .rules-tabs .p-tabview-nav {
      display: flex;
      background: transparent;
      border: none;
      border-bottom: 1px solid var(--surface-border, #e2e8f0);
    }
    :host ::ng-deep .rules-tabs .p-tabview-nav li .p-tabview-nav-link {
      background: transparent;
      border: none;
      border-bottom: 2px solid transparent;
      margin-bottom: -1px;
      padding: 0.75rem 1.1rem;
    }
    :host ::ng-deep .rules-tabs .p-tabview-nav li.p-highlight .p-tabview-nav-link {
      border-bottom-color: var(--primary-color, #2563eb);
      color: var(--primary-color, #2563eb);
    }
    :host ::ng-deep .rules-tabs .p-tabview-panels {
      background: transparent;
      padding: 1rem 0 0;
    }
  `
})
export class ReglesListComponent implements OnInit {
  private readonly api = inject(RulesService);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly dialog = signal(false);
  readonly dialogGroupe = signal(false);
  readonly edition = signal<RuleSummary | null>(null);
  readonly groupeEdition = signal<RuleGroupSummary | null>(null);
  readonly savingGroupe = signal(false);
  readonly groupeError = signal<string | null>(null);
  readonly regles = signal<RuleSummary[]>([]);
  readonly groupes = signal<RuleGroupSummary[]>([]);
  readonly metriques = METRIQUES;
  readonly periodes = PERIODES;
  readonly perimetres = PERIMETRES;
  readonly operateurs = OPERATEURS;
  readonly operations = OPERATIONS;
  readonly strategies = STRATEGIES;
  readonly statutsGroupe = STATUTS_GROUPE;
  saisie: SaisieRegle = vide();
  saisieGroupe: SaisieGroupe = videGroupe();
  private metriqueOuverte = '';

  ngOnInit(): void {
    this.charger();
  }

  ouvrir(regle: RuleSummary | null): void {
    this.formError.set(null);
    this.edition.set(regle);
    this.saisie = regle ? depuis(regle) : vide(this.groupes());
    this.metriqueOuverte = this.saisie.sourceType;
    this.dialog.set(true);
  }

  libelleMetrique(regle: RuleSummary): string {
    return METRIQUES.find((m) => m.value === regle.sourceType)?.label ?? (regle.sourceType || '—');
  }

  parametresMetrique(regle: RuleSummary): string {
    const parts = [
      PERIODES.find((p) => p.value === regle.sourcePeriod)?.label,
      PERIMETRES.find((p) => p.value === regle.sourceScope)?.label,
      regle.operator || undefined
    ].filter((part): part is string => !!part);
    return parts.join(' · ') || '—';
  }

  estCumul(sourceType: string): boolean {
    return sourceType === 'CUMUL_AMOUNT' || sourceType === 'CUMUL_COUNT';
  }

  estNombre(sourceType: string): boolean {
    return sourceType === 'CUMUL_COUNT' || sourceType === 'TRANSACTION_COUNT';
  }

  aPerimetre(sourceType: string): boolean {
    return this.estCumul(sourceType)
      || sourceType === 'PROJECTED_HOLDING'
      || sourceType === 'PROJECTED_BALANCE'
      || sourceType === 'WALLET_BALANCE';
  }

  controleCredit(sourceType: string): boolean {
    return sourceType === 'PROJECTED_HOLDING' || sourceType === 'CUMUL_COUNT' || sourceType === 'CUMUL_AMOUNT';
  }

  appliquerMetrique(event: string | { value?: string }): void {
    const sourceType = typeof event === 'string' ? event : event?.value ?? '';
    if (!sourceType || sourceType === this.metriqueOuverte) {
      return;
    }
    this.metriqueOuverte = sourceType;
    const params = parametresParDefaut(sourceType);
    this.saisie.sourcePeriod = params.sourcePeriod;
    this.saisie.sourceScope = params.sourceScope;
    this.saisie.operator = params.operator;
    this.saisie.includeCurrent = params.includeCurrent;
    this.saisie.priority = prioriteDe(sourceType, params.sourcePeriod);
  }

  reglesPubliees(): RuleSummary[] {
    return this.regles().filter((regle) => regle.status === 'PUBLIEE');
  }

  ouvrirGroupe(groupe: RuleGroupSummary | null): void {
    this.groupeError.set(null);
    if (!groupe) {
      this.groupeEdition.set(null);
      this.saisieGroupe = videGroupe();
      this.dialogGroupe.set(true);
      return;
    }
    this.api.detailGroupe(groupe.code).subscribe({
      next: (detail) => {
        this.groupeEdition.set(detail);
        this.saisieGroupe = depuisGroupe(detail);
        this.dialogGroupe.set(true);
      },
      error: (e: { message?: string }) => this.groupeError.set(e?.message ?? 'Chargement du groupe impossible.')
    });
  }

  ordonnerRegles(codes: string[]): void {
    const previous = this.saisieGroupe.ruleCodes;
    const kept = previous.filter((code) => codes.includes(code));
    const added = codes.filter((code) => !previous.includes(code));
    this.saisieGroupe.ruleCodes = [...kept, ...added];
  }

  deplacerRegle(index: number, delta: number): void {
    const next = this.saisieGroupe.ruleCodes.slice();
    const target = index + delta;
    if (target < 0 || target >= next.length) {
      return;
    }
    const [code] = next.splice(index, 1);
    next.splice(target, 0, code);
    this.saisieGroupe.ruleCodes = next;
  }

  enregistrerGroupe(): void {
    if (!this.saisieGroupe.code.trim() || !this.saisieGroupe.libelle.trim()) {
      this.groupeError.set('Code et libellé sont requis.');
      return;
    }
    const body: GroupWrite = {
      code: this.saisieGroupe.code.trim(),
      libelle: this.saisieGroupe.libelle.trim(),
      description: this.saisieGroupe.description.trim(),
      transactionType: this.saisieGroupe.transactionType,
      productCode: this.saisieGroupe.productCode.trim(),
      evaluationStrategy: this.saisieGroupe.evaluationStrategy,
      status: this.saisieGroupe.status,
      ruleCodes: this.saisieGroupe.ruleCodes
    };
    this.savingGroupe.set(true);
    this.groupeError.set(null);
    const courant = this.groupeEdition();
    const appel = courant ? this.api.modifierGroupe(courant.code, body) : this.api.creerGroupe(body);
    appel.subscribe({
      next: () => {
        this.savingGroupe.set(false);
        this.dialogGroupe.set(false);
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.savingGroupe.set(false);
        this.groupeError.set(e?.message ?? 'Enregistrement impossible.');
      }
    });
  }

  private seuilRequis(sourceType: string): boolean {
    return this.estCumul(sourceType)
      || sourceType === 'TRANSACTION_AMOUNT'
      || sourceType === 'TRANSACTION_COUNT'
      || sourceType === 'PROJECTED_HOLDING'
      || sourceType === 'PROJECTED_BALANCE'
      || sourceType === 'WALLET_BALANCE';
  }

  formatSeuil(value?: number | null): string {
    if (value == null) {
      return '—';
    }
    return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(value);
  }

  enregistrer(): void {
    if (!this.saisie.code.trim() || !this.saisie.libelle.trim() || !this.saisie.message.trim()) {
      this.formError.set('Code, libellé et message sont requis.');
      return;
    }
    if (this.estCumul(this.saisie.sourceType) && !this.saisie.sourcePeriod) {
      this.formError.set('La période de la métrique est requise.');
      return;
    }
    if (this.aPerimetre(this.saisie.sourceType) && !this.saisie.sourceScope) {
      this.formError.set('Le périmètre de la métrique est requis.');
      return;
    }
    if (this.seuilRequis(this.saisie.sourceType) && (this.saisie.limitValue == null || this.saisie.limitValue < 0)) {
      this.formError.set('Le seuil est requis.');
      return;
    }
    const body = versRequete(this.saisie, !this.edition());
    this.saving.set(true);
    this.formError.set(null);
    const courant = this.edition();
    const appel = courant ? this.api.modifier(courant.id, body) : this.api.creer(body);
    appel.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialog.set(false);
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.saving.set(false);
        this.formError.set(e?.message ?? 'Enregistrement impossible.');
      }
    });
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    let pending = 2;
    const done = () => {
      pending -= 1;
      if (pending === 0) {
        this.loading.set(false);
      }
    };
    this.api.listerRegles().subscribe({
      next: (list) => {
        this.regles.set(list);
        done();
      },
      error: (e: { message?: string }) => {
        this.error.set(e?.message ?? 'Chargement impossible (payment-rules :8099).');
        done();
      }
    });
    this.api.listerGroupes().subscribe({
      next: (list) => {
        this.groupes.set(list);
        done();
      },
      error: () => done()
    });
  }
}

function vide(groupes: RuleGroupSummary[] = []): SaisieRegle {
  const groupe = groupes.find((g) => g.code === 'CASH_IN_STANDARD')?.code ?? groupes[0]?.code ?? 'CASH_IN_STANDARD';
  return {
    code: '',
    libelle: '',
    legalRef: 'Instruction BCEAO n°020 du 08/10/2026, art. 31',
    message: '',
    sourceType: 'PROJECTED_HOLDING',
    sourcePeriod: '',
    sourceScope: 'CUSTOMER',
    operator: '<=',
    includeCurrent: true,
    priority: 45,
    transactionType: 'CASH_IN',
    groupCode: groupe,
    limitValue: 4000000,
    effectiveFrom: '2026-11-02',
    identified: true,
    excludePro: true,
    creditRecu: true
  };
}

function depuis(regle: RuleSummary): SaisieRegle {
  const cond = lireConditions(regle.conditionsJson);
  return {
    code: regle.code,
    libelle: regle.libelle,
    legalRef: regle.legalRef ?? '',
    message: regle.message ?? '',
    sourceType: regle.sourceType || 'PROJECTED_HOLDING',
    sourcePeriod: regle.sourcePeriod ?? '',
    sourceScope: regle.sourceScope ?? '',
    operator: regle.operator || '<=',
    includeCurrent: regle.includeCurrent !== false,
    priority: regle.priority ?? prioriteDe(regle.sourceType || '', regle.sourcePeriod || ''),
    transactionType: cond.transactionType,
    groupCode: '',
    limitValue: regle.limitValue ?? 0,
    effectiveFrom: (regle.effectiveFrom ?? '2026-11-02').slice(0, 10),
    identified: cond.identified,
    excludePro: cond.excludePro,
    creditRecu: cond.creditRecu
  };
}

function versRequete(saisie: SaisieRegle, creation: boolean): RuleWrite {
  const operation = saisie.transactionType.trim() || 'CASH_IN';
  const cumul = saisie.sourceType === 'CUMUL_AMOUNT' || saisie.sourceType === 'CUMUL_COUNT';
  const conditions: Record<string, unknown> = {};
  if (saisie.creditRecu && (saisie.sourceType === 'PROJECTED_HOLDING' || saisie.sourceType === 'CUMUL_COUNT' || saisie.sourceType === 'CUMUL_AMOUNT')) {
    const operations = operation === 'ACCOUNT_TRANSFER' ? ['CASH_IN', 'ACCOUNT_TRANSFER'] : [operation, 'ACCOUNT_TRANSFER'];
    conditions['transactionTypes'] = [...new Set(operations)];
    conditions['skipSameCustomer'] = true;
  } else {
    conditions['transactionType'] = operation;
  }
  if (saisie.identified) {
    conditions['kycLevels'] = ['STANDARD', 'ENHANCED', 'FULL'];
  }
  if (saisie.excludePro) {
    conditions['excludeSubjectRoles'] = ['DISTRIBUTEUR', 'ACCEPTEUR', 'DISTRIBUTOR', 'ACCEPTOR'];
  }
  return {
    code: saisie.code.trim(),
    libelle: saisie.libelle.trim(),
    description: saisie.libelle.trim(),
    category: 'LIMIT',
    priority: saisie.priority,
    conditionsJson: JSON.stringify(conditions),
    sourceType: saisie.sourceType,
    sourcePeriod: cumul ? saisie.sourcePeriod : null,
    sourceScope: saisie.sourceScope || null,
    includeCurrent: saisie.includeCurrent,
    operator: saisie.operator,
    limitValue: saisie.limitValue,
    message: saisie.message.trim(),
    mandatory: true,
    legalRef: saisie.legalRef.trim(),
    effectiveFrom: saisie.effectiveFrom,
    groupCode: creation ? saisie.groupCode : undefined,
    publish: creation ? true : undefined
  };
}

function parametresParDefaut(sourceType: string): { sourcePeriod: string; sourceScope: string; operator: string; includeCurrent: boolean } {
  switch (sourceType) {
    case 'CUMUL_AMOUNT':
    case 'CUMUL_COUNT':
      return { sourcePeriod: 'MONTH', sourceScope: 'CUSTOMER', operator: '<=', includeCurrent: true };
    case 'PROJECTED_HOLDING':
      return { sourcePeriod: '', sourceScope: 'CUSTOMER', operator: '<=', includeCurrent: true };
    case 'PROJECTED_BALANCE':
    case 'WALLET_BALANCE':
      return { sourcePeriod: '', sourceScope: 'WALLET', operator: '>=', includeCurrent: true };
    case 'TRANSACTION_AMOUNT':
    case 'TRANSACTION_COUNT':
      return { sourcePeriod: '', sourceScope: '', operator: '<=', includeCurrent: true };
    default:
      return { sourcePeriod: '', sourceScope: '', operator: '=', includeCurrent: false };
  }
}

function prioriteDe(sourceType: string, period: string): number {
  if (sourceType === 'CUMUL_COUNT') {
    return 55;
  }
  if (sourceType === 'CUMUL_AMOUNT' && period === 'MONTH') {
    return 50;
  }
  if (sourceType === 'CUMUL_AMOUNT') {
    return 40;
  }
  if (sourceType === 'TRANSACTION_AMOUNT' || sourceType === 'TRANSACTION_COUNT') {
    return 30;
  }
  if (sourceType === 'PROJECTED_HOLDING') {
    return 45;
  }
  if (sourceType === 'PROJECTED_BALANCE' || sourceType === 'WALLET_BALANCE') {
    return 60;
  }
  return 100;
}

function lireConditions(json?: string | null): { transactionType: string; identified: boolean; excludePro: boolean; creditRecu: boolean } {
  try {
    const cond = JSON.parse(json || '{}') as {
      transactionType?: string;
      transactionTypes?: string[];
      kycLevels?: string[];
      excludeSubjectRoles?: string[];
    };
    const types = Array.isArray(cond.transactionTypes) ? cond.transactionTypes : [];
    const creditRecu = types.some((type) => type === 'ACCOUNT_TRANSFER');
    const transactionType = cond.transactionType
      || types.find((type) => type !== 'ACCOUNT_TRANSFER')
      || 'CASH_IN';
    return {
      transactionType,
      identified: Array.isArray(cond.kycLevels) && cond.kycLevels.length > 0,
      excludePro: Array.isArray(cond.excludeSubjectRoles) && cond.excludeSubjectRoles.length > 0,
      creditRecu
    };
  } catch {
    return { transactionType: 'CASH_IN', identified: true, excludePro: true, creditRecu: false };
  }
}
