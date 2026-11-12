import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { concat, forkJoin, Observable, of } from 'rxjs';
import { catchError, concatMap, last, map } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { ProviderService } from '../../core/service/provider.service';
import { OrchestrationService } from '../../core/service/orchestration.service';
import {
  InterfaceUtilisateur,
  InterfaceUtilisateurRequest,
  InterfaceTemplatePreview,
  Operation,
  ParametreUtilisateur,
  Provider,
  ProviderApi,
  SchemaEcriture,
  SchemaMatchResult,
  SERVICE_TYPE_CODES,
  ServiceTypeCode,
  TypeEnvoi
} from '../../core/model/api.models';

const SERVICE_TYPES = SERVICE_TYPE_CODES;

const TYPE_ENVOI: TypeEnvoi[] = ['REQUEST'];

interface DraftParametre {
  localId: string;
  id: string | null;
  codeParametre: string;
  codeParametreMethode: string;
  attribut: string;
  sourceParametre: string;
  extractScript: string;
  valeur: string;
}

let draftParamSeq = 0;
function newDraftLocalId(): string {
  draftParamSeq += 1;
  return `draft-${draftParamSeq}`;
}

@Component({
  selector: 'app-provider-orchestration',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CheckboxModule,
    DialogModule,
    DropdownModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TooltipModule
  ],
  template: `
    <div class="gw-page-header">
      <div>
        <h2>Orchestration Omnicanal</h2>
        <p class="subtitle">
          Interfaces utilisateur (codeFse + typeMethode → operation/API) et ParametreUtilisateur (constitues)
        </p>
      </div>
      <div class="header-actions">
        <a
          pButton
          class="p-button-text"
          routerLink="/admin/interface-utilisateurs"
          label="Admin interfaces"
          icon="pi pi-external-link"
        ></a>
        @if (provider()?.codeFse) {
          <button
            pButton
            type="button"
            class="p-button-outlined"
            label="Générer depuis APIs"
            icon="pi pi-sync"
            [loading]="seeding()"
            (click)="seedInterfaces()"
          ></button>
          <button
            pButton
            type="button"
            label="Nouvelle interface"
            icon="pi pi-plus"
            (click)="openCreate()"
          ></button>
        }
      </div>
    </div>

    @if (loading()) {
      <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" /></div>
    } @else {
      @if (msg()) {
        <p class="gw-msg" [class.error]="msgError()">{{ msg() }}</p>
      }

      <section class="gw-panel">
        <h3>Interfaces utilisateur · {{ totalInterfaces() }}</h3>
        <p-table
          [value]="interfaces()"
          styleClass="p-datatable-sm p-datatable-striped"
          [rowHover]="true"
          dataKey="id"
          [lazy]="true"
          [loading]="loadingInterfaces()"
          [paginator]="true"
          [rows]="ifacePageSize()"
          [totalRecords]="totalInterfaces()"
          [first]="ifacePage() * ifacePageSize()"
          [rowsPerPageOptions]="[10, 20, 50]"
          [showCurrentPageReport]="true"
          currentPageReportTemplate="{first}-{last} sur {totalRecords}"
          (onLazyLoad)="onInterfacesLazyLoad($event)"
        >
          <ng-template pTemplate="header">
            <tr>
              <th>Code FSE</th>
              <th>Type méthode</th>
              <th>Service</th>
              <th>Opération</th>
              <th>Endpoint</th>
              <th>Params</th>
              <th>Actif</th>
              <th></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-i>
            <tr [class.selected]="selectedInterface()?.id === i.id">
              <td>{{ i.codeFse }}</td>
              <td><code>{{ i.typeMethode }}</code></td>
              <td>{{ i.serviceType }}</td>
              <td>{{ i.providerOperationCode ?? '—' }}</td>
              <td>{{ i.providerApiCode ?? '—' }}</td>
              <td>{{ i.parametres?.length ?? 0 }}</td>
              <td>{{ i.actif ? 'Oui' : 'Non' }}</td>
              <td class="row-actions">
                <button
                  pButton
                  type="button"
                  class="p-button-text p-button-sm"
                  icon="pi pi-pencil"
                  pTooltip="Modifier"
                  (click)="openEdit(i)"
                ></button>
                <button
                  pButton
                  type="button"
                  class="p-button-text p-button-sm"
                  label="Params"
                  (click)="selectInterface(i)"
                ></button>
                <button
                  pButton
                  type="button"
                  class="p-button-text p-button-danger p-button-sm"
                  icon="pi pi-trash"
                  (click)="deleteInterface(i)"
                ></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr>
              <td colspan="8">
                <p class="hint">
                  Aucune interface. Créez-en une, ou chargez le catalogue APIs puis « Générer depuis APIs ».
                </p>
              </td>
            </tr>
          </ng-template>
        </p-table>
      </section>

      @if (selectedInterface(); as iface) {
        <section class="gw-panel">
          <div class="panel-header">
            <h3>ParametreUtilisateur — {{ iface.typeMethode }}</h3>
            <div class="panel-actions">
              <button
                pButton
                type="button"
                class="p-button-sm"
                label="Ajouter"
                icon="pi pi-plus"
                (click)="openAddLiveParam()"
              ></button>
              <button
                pButton
                type="button"
                class="p-button-outlined p-button-sm"
                label="Compléter défauts"
                icon="pi pi-sync"
                [loading]="seedingParams()"
                (click)="seedDefaultParams(iface)"
              ></button>
              <button
                pButton
                type="button"
                class="p-button-text p-button-sm"
                icon="pi pi-times"
                label="Fermer"
                (click)="selectedInterface.set(null)"
              ></button>
            </div>
          </div>
          <p class="hint">Contrat canal → codes méthode (équivalent Omnicanal constitues).</p>

          @if (!(iface.parametres?.length)) {
            <p class="hint">Aucun paramètre. Cliquez sur « Ajouter » ou « Compléter défauts ».</p>
          } @else {
            <table class="gw-data-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>→ Méthode (source)</th>
                  <th>Attribut</th>
                  <th>Défaut</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                @for (p of iface.parametres!; track p.id) {
                  <tr>
                    <td><code>{{ p.codeParametre }}</code></td>
                    <td><code>{{ p.codeParametreMethode ?? '—' }}</code></td>
                    <td>{{ p.attribut ?? '—' }}</td>
                    <td>{{ p.valeur ?? '—' }}</td>
                    <td>
                      <button
                        pButton
                        type="button"
                        class="p-button-text p-button-sm"
                        icon="pi pi-pencil"
                        (click)="openEditParam(p)"
                      ></button>
                      <button
                        pButton
                        type="button"
                        class="p-button-text p-button-danger p-button-sm"
                        icon="pi pi-trash"
                        (click)="deleteParam(p)"
                      ></button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </section>
      }

      <section class="gw-panel">
        <h3>Schémas d'écriture (TRANSFERT)</h3>
        <table class="gw-data-table">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Pattern</th>
              <th>Workflow</th>
              <th>Lignes</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for (s of schemas(); track s.id) {
              <tr>
                <td>{{ s.nomSchema }}</td>
                <td><code>{{ s.patternCode }}</code></td>
                <td>{{ s.workflowName }} v{{ s.workflowVersion }}</td>
                <td>{{ s.lignes?.length ?? '—' }}</td>
                <td>
                  <a
                    pButton
                    class="p-button-text p-button-sm"
                    [routerLink]="['/admin/schema-ecritures']"
                    [queryParams]="{ id: s.id }"
                    label="Gérer"
                  ></a>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </section>

      @if (provider()?.codeFse) {
        <section class="gw-panel">
          <h3>Test matching schéma</h3>
          <form [formGroup]="matchForm" (ngSubmit)="runMatch()" class="gw-form-grid">
            <div class="gw-field">
              <label>FSE source</label>
              <input pInputText formControlName="sourceCodeFse" class="w-full" />
            </div>
            <div class="gw-field">
              <label>FSE dest</label>
              <input pInputText formControlName="destCodeFse" class="w-full" />
            </div>
            <div class="gw-field actions">
              <button pButton type="submit" label="Matcher" [loading]="matching()"></button>
            </div>
          </form>
          @if (matchResult(); as m) {
            <p class="match-result">
              Schéma retenu : <strong>{{ m.nomSchema }}</strong> ({{ m.patternCode }}) → workflow
              <code>{{ m.workflowName }}</code> · {{ m.lignes.length }} jambe(s)
            </p>
          }
        </section>
      }
    }

    <p-dialog
      [header]="dialogTitle()"
      [visible]="dialogVisible()"
      (visibleChange)="dialogVisible.set($event)"
      [modal]="true"
      [style]="{ width: 'min(72rem, 98vw)' }"
      [draggable]="false"
    >
      <form [formGroup]="ifaceForm" (ngSubmit)="saveInterface()" class="dialog-form">
        <div class="gw-field">
          <label>Code FSE *</label>
          <input pInputText formControlName="codeFse" class="w-full" />
        </div>
          <div class="gw-field">
            <label>Type méthode *</label>
            <p-dropdown
              formControlName="typeMethode"
              [options]="typeMethodeOptions()"
              optionLabel="label"
              optionValue="value"
              [filter]="true"
              [editable]="true"
              placeholder="Sélectionner ou saisir…"
              styleClass="w-full"
              (onChange)="onTypeMethodePicked()"
            />
          </div>
        <div class="gw-field">
          <label>Type de service *</label>
          <p-dropdown
            formControlName="serviceType"
            [options]="serviceTypeOptions"
            optionLabel="label"
            optionValue="value"
            styleClass="w-full"
          />
        </div>
        <div class="gw-field">
          <label>Type envoi</label>
          <p-dropdown
            formControlName="typeEnvoi"
            [options]="typeEnvoiOptions"
            optionLabel="label"
            optionValue="value"
            styleClass="w-full"
          />
        </div>
        <div class="gw-field">
          <label>Opération</label>
          <p-dropdown
            formControlName="providerOperationId"
            [options]="operationOptions()"
            optionLabel="label"
            optionValue="value"
            placeholder="—"
            [showClear]="true"
            [filter]="true"
            styleClass="w-full"
            (onChange)="onOperationPicked()"
          />
        </div>
        <div class="gw-field">
          <label>Endpoint (API)</label>
          <p-dropdown
            formControlName="providerApiId"
            [options]="apiOptions()"
            optionLabel="label"
            optionValue="value"
            placeholder="—"
            [showClear]="true"
            [filter]="true"
            styleClass="w-full"
          />
        </div>
        <div class="gw-field">
          <label>Code interface</label>
          <input pInputText formControlName="codeInterface" class="w-full" />
        </div>
        <div class="gw-field">
          <label>Libellé</label>
          <input pInputText formControlName="nomInterface" class="w-full" />
        </div>
        <div class="gw-field checkbox-field">
          <p-checkbox formControlName="actif" [binary]="true" inputId="iface-actif-po" />
          <label for="iface-actif-po">Actif</label>
        </div>

        <div class="params-editor">
          <div class="params-editor-head">
            <div>
              <h4>Paramètres</h4>
              <p class="hint">
                @if (editingId()) {
                  Modifiez la liste puis enregistrez l'interface.
                } @else {
                  Préremplis depuis le type méthode ; ajustez avant de créer.
                }
              </p>
            </div>
            <button
              pButton
              type="button"
              class="p-button-outlined p-button-sm"
              label="Ajouter"
              icon="pi pi-plus"
              (click)="openAddDraftParam()"
            ></button>
          </div>

          @if (!draftParams().length) {
            <p class="hint">Aucun paramètre. Cliquez sur « Ajouter ».</p>
          } @else {
            <table class="gw-data-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>→ Méthode</th>
                  <th>Attribut</th>
                  <th>Défaut</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                @for (p of draftParams(); track p.localId; let i = $index) {
                  <tr>
                    <td><code>{{ p.codeParametre || '—' }}</code></td>
                    <td><code>{{ p.codeParametreMethode || '—' }}</code></td>
                    <td>{{ p.attribut || '—' }}</td>
                    <td>{{ p.valeur || '—' }}</td>
                    <td class="row-actions">
                      <button
                        pButton
                        type="button"
                        class="p-button-text p-button-sm"
                        icon="pi pi-pencil"
                        (click)="openEditDraftParam(i)"
                      ></button>
                      <button
                        pButton
                        type="button"
                        class="p-button-text p-button-danger p-button-sm"
                        icon="pi pi-trash"
                        (click)="removeDraftParam(i)"
                      ></button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </div>

        <div class="dialog-actions">
          <button pButton type="button" class="p-button-text" label="Annuler" (click)="dialogVisible.set(false)"></button>
          <button
            pButton
            type="submit"
            [label]="editingId() ? 'Enregistrer' : 'Créer'"
            [disabled]="ifaceForm.invalid"
            [loading]="savingIface()"
          ></button>
        </div>
      </form>
    </p-dialog>

    <p-dialog
      [header]="paramDialogTitle()"
      [visible]="paramDialogVisible()"
      (visibleChange)="paramDialogVisible.set($event)"
      [modal]="true"
      appendTo="body"
      [baseZIndex]="12000"
      [style]="{ width: 'min(32rem, 96vw)' }"
      [draggable]="false"
    >
      <form [formGroup]="paramEditForm" (ngSubmit)="saveParamDialog()" class="dialog-form single-col">
        <div class="gw-field">
          <label>Code *</label>
          <input pInputText formControlName="codeParametre" class="w-full" placeholder="CODE" />
        </div>
        <div class="gw-field">
          <label>Code méthode (source payload)</label>
          <input pInputText formControlName="codeParametreMethode" class="w-full" placeholder="vignette, reference…" />
        </div>
        <div class="gw-field">
          <label>Attribut</label>
          <input
            pInputText
            formControlName="attribut"
            class="w-full"
            placeholder="REGEX:^(?:[^-]+-){3}(\d+)$ ou NUMERO_COMPTE"
          />
        </div>
        <div class="gw-field">
          <label>Script Groovy (si complexe)</label>
          <textarea formControlName="extractScript" class="w-full" rows="3"
            placeholder="def p = (source ?: '').tokenize('-'); return p[3]"></textarea>
        </div>
        <div class="gw-field">
          <label>Valeur défaut</label>
          <input pInputText formControlName="valeur" class="w-full" />
        </div>
        <div class="dialog-actions">
          <button pButton type="button" class="p-button-text" label="Annuler" (click)="paramDialogVisible.set(false)"></button>
          <button
            pButton
            type="submit"
            [label]="paramDialogSubmitLabel()"
            [disabled]="paramEditForm.invalid"
            [loading]="savingParam()"
          ></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .hint { color: var(--text-color-secondary, #64748b); font-size: 0.9rem; }
    .match-result { margin-top: 1rem; font-size: 0.9rem; }
    code { font-size: 0.85em; }
    tr.selected { background: color-mix(in srgb, var(--primary-color, #2563eb) 8%, transparent); }
    .param-form { margin-top: 1rem; }
    .header-actions { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
    .panel-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 0.5rem;
    }
    .panel-header h3 { margin: 0; }
    .panel-actions { display: flex; align-items: center; gap: 0.35rem; }
    .row-actions { display: flex; align-items: center; gap: 0.15rem; white-space: nowrap; }
    .gw-msg.error { color: #b91c1c; }
    .dialog-form {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.85rem 1rem;
    }
    .dialog-form.single-col { grid-template-columns: 1fr; }
    .dialog-form .gw-field { display: flex; flex-direction: column; gap: 0.35rem; }
    .dialog-form .checkbox-field {
      flex-direction: row;
      align-items: center;
      gap: 0.5rem;
      grid-column: 1 / -1;
    }
    .template-preview {
      grid-column: 1 / -1;
      padding: 0.75rem;
      border-radius: 8px;
      border: 1px solid #dbeafe;
      background: #f8fafc;
    }
    .params-editor {
      grid-column: 1 / -1;
      padding: 0.85rem;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      background: #f8fafc;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .params-editor-head {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 0.75rem;
    }
    .params-editor-head h4 {
      margin: 0 0 0.25rem;
      font-size: 0.95rem;
    }
    .params-editor-head .hint {
      margin: 0;
    }
    .draft-param-table {
      display: flex;
      flex-direction: column;
      gap: 0.45rem;
    }
    .draft-param-header,
    .draft-param-row {
      display: grid;
      grid-template-columns: 1.1fr 1.1fr 1.2fr 1fr auto;
      gap: 0.4rem;
      align-items: center;
    }
    .draft-param-header {
      font-size: 0.75rem;
      font-weight: 600;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: 0.02em;
    }
    .mono {
      font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
      font-size: 0.85rem;
    }
    @media (max-width: 720px) {
      .draft-param-header { display: none; }
      .draft-param-row {
        grid-template-columns: 1fr;
        padding: 0.5rem;
        border: 1px solid #e2e8f0;
        border-radius: 6px;
        background: #fff;
      }
    }
    .dialog-actions {
      grid-column: 1 / -1;
      display: flex;
      justify-content: flex-end;
      gap: 0.5rem;
      margin-top: 0.5rem;
    }
    :host ::ng-deep .w-full { width: 100%; }
    @media (max-width: 640px) {
      .dialog-form { grid-template-columns: 1fr; }
    }
  `
})
export class ProviderOrchestrationComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly providersApi = inject(ProviderService);
  private readonly orchestrationApi = inject(OrchestrationService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(true);
  readonly loadingInterfaces = signal(false);
  readonly seeding = signal(false);
  readonly seedingParams = signal(false);
  readonly matching = signal(false);
  readonly savingParam = signal(false);
  readonly savingIface = signal(false);
  readonly msg = signal<string | null>(null);
  readonly msgError = signal(false);
  readonly provider = signal<Provider | null>(null);
  readonly interfaces = signal<InterfaceUtilisateur[]>([]);
  readonly totalInterfaces = signal(0);
  readonly ifacePage = signal(0);
  readonly ifacePageSize = signal(10);
  readonly selectedInterface = signal<InterfaceUtilisateur | null>(null);
  readonly schemas = signal<SchemaEcriture[]>([]);
  readonly matchResult = signal<SchemaMatchResult | null>(null);
  readonly operations = signal<Operation[]>([]);
  readonly apis = signal<ProviderApi[]>([]);
  readonly typeMethodesCatalog = signal<string[]>([]);
  readonly templatePreview = signal<InterfaceTemplatePreview | null>(null);
  readonly draftParams = signal<DraftParametre[]>([]);
  readonly dialogVisible = signal(false);
  readonly paramDialogVisible = signal(false);
  readonly paramDialogMode = signal<'draft' | 'live'>('draft');
  readonly editingId = signal<string | null>(null);
  readonly editingParamId = signal<string | null>(null);
  readonly editingDraftIndex = signal<number | null>(null);
  readonly filterOperationId = signal<string | null>(null);

  readonly dialogTitle = computed(() =>
    this.editingId() ? "Modifier l'interface" : 'Nouvelle interface'
  );
  readonly paramDialogTitle = computed(() => {
    if (this.paramDialogMode() === 'draft') {
      return this.editingDraftIndex() === null ? 'Ajouter un paramètre' : 'Modifier le paramètre';
    }
    return this.editingParamId() === null ? 'Ajouter un paramètre' : 'Modifier le paramètre';
  });
  readonly paramDialogSubmitLabel = computed(() => {
    const isEdit =
      this.paramDialogMode() === 'draft'
        ? this.editingDraftIndex() !== null
        : this.editingParamId() !== null;
    return isEdit ? 'Enregistrer' : 'Ajouter';
  });

  readonly serviceTypeOptions = SERVICE_TYPES.map((value) => ({ label: value, value }));
  readonly typeEnvoiOptions = TYPE_ENVOI.map((value) => ({ label: value, value }));

  readonly operationOptions = computed(() =>
    this.operations().map((o) => ({ label: `${o.code} — ${o.nom}`, value: o.id }))
  );

  readonly apiOptions = computed(() => {
    const opId = this.filterOperationId();
    const list = this.apis().filter((a) => !opId || a.operationId === opId);
    return list.map((a) => ({
      label: `${a.code} · ${a.httpMethod} ${a.path}`,
      value: a.id
    }));
  });

  readonly typeMethodeOptions = computed(() => {
    const fromCatalog = this.typeMethodesCatalog();
    const fromOps = this.operations().map((o) => o.code);
    const current = this.ifaceForm.controls.typeMethode.value?.trim();
    const all = [...fromCatalog, ...fromOps];
    if (current) {
      all.push(current);
    }
    return [...new Set(all.filter((v) => !!v))]
      .sort((a, b) => a.localeCompare(b))
      .map((value) => ({ label: value, value }));
  });

  readonly matchForm = this.fb.nonNullable.group({
    sourceCodeFse: ['', Validators.required],
    destCodeFse: ['', Validators.required]
  });

  readonly paramEditForm = this.fb.nonNullable.group({
    codeParametre: ['', Validators.required],
    codeParametreMethode: [''],
    attribut: [''],
    sourceParametre: [''],
    extractScript: [''],
    valeur: ['']
  });

  readonly ifaceForm = this.fb.nonNullable.group({
    codeFse: ['', Validators.required],
    typeMethode: ['', Validators.required],
    serviceType: this.fb.nonNullable.control<ServiceTypeCode>('TRANSFERT', Validators.required),
    typeEnvoi: this.fb.nonNullable.control<TypeEnvoi>('REQUEST'),
    providerOperationId: this.fb.control<string | null>(null),
    providerApiId: this.fb.control<string | null>(null),
    codeInterface: [''],
    nomInterface: [''],
    actif: [true]
  });

  ngOnInit(): void {
    const providerId = this.route.parent!.snapshot.paramMap.get('id')!;
    forkJoin({
      provider: this.providersApi.get(providerId),
      interfaces: this.orchestrationApi.listInterfacesPage(providerId, 0, this.ifacePageSize()),
      schemas: this.orchestrationApi.listSchemas('TRANSFERT'),
      ops: this.providersApi.listOperations(providerId).pipe(catchError(() => of([] as Operation[]))),
      apis: this.providersApi.listApis(providerId).pipe(catchError(() => of([] as ProviderApi[]))),
      typeMethodes: this.providersApi.listOmnicanalTypeMethodes().pipe(catchError(() => of([] as string[])))
    }).subscribe({
      next: ({ provider, interfaces, schemas, ops, apis, typeMethodes }) => {
        this.provider.set(provider);
        this.interfaces.set(interfaces.content ?? []);
        this.totalInterfaces.set(interfaces.totalElements ?? 0);
        this.ifacePage.set(0);
        this.schemas.set(schemas);
        this.operations.set(ops);
        this.apis.set(apis);
        this.typeMethodesCatalog.set(typeMethodes);
        if (provider.codeFse) {
          this.matchForm.patchValue({
            sourceCodeFse: provider.codeFse,
            destCodeFse: provider.codeFse
          });
        }
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.setMsg(err?.message ?? 'Chargement orchestration échoué', true);
        this.loading.set(false);
      }
    });
  }

  onInterfacesLazyLoad(ev: TableLazyLoadEvent): void {
    if (this.loading() || !this.provider()) return;
    const rows = ev.rows ?? this.ifacePageSize();
    const first = ev.first ?? 0;
    this.ifacePageSize.set(rows);
    this.reloadInterfaces(undefined, Math.floor(first / rows));
  }

  selectInterface(iface: InterfaceUtilisateur): void {
    this.selectedInterface.set(iface);
  }

  openCreate(): void {
    const p = this.provider();
    if (!p) return;
    this.editingId.set(null);
    this.filterOperationId.set(null);
    this.templatePreview.set(null);
    this.draftParams.set([]);
    this.ifaceForm.reset({
      codeFse: p.codeFse || '',
      typeMethode: '',
      serviceType: 'TRANSFERT',
      typeEnvoi: 'REQUEST',
      providerOperationId: null,
      providerApiId: null,
      codeInterface: '',
      nomInterface: '',
      actif: true
    });
    this.dialogVisible.set(true);
  }

  openEdit(iface: InterfaceUtilisateur): void {
    this.editingId.set(iface.id);
    this.filterOperationId.set(iface.providerOperationId);
    this.templatePreview.set(null);
    this.draftParams.set(this.toDraftParams(iface.parametres ?? []));
    this.selectedInterface.set(iface);
    this.ifaceForm.reset({
      codeFse: iface.codeFse,
      typeMethode: iface.typeMethode,
      serviceType: iface.serviceType,
      typeEnvoi: iface.typeEnvoi || 'REQUEST',
      providerOperationId: iface.providerOperationId,
      providerApiId: iface.providerApiId,
      codeInterface: iface.codeInterface || '',
      nomInterface: iface.nomInterface || '',
      actif: iface.actif
    });
    this.dialogVisible.set(true);
  }

  onTypeMethodePicked(): void {
    if (this.editingId()) return;
    const p = this.provider();
    const typeMethode = this.ifaceForm.controls.typeMethode.value?.trim();
    if (!p || !typeMethode) {
      this.templatePreview.set(null);
      return;
    }
    this.orchestrationApi.previewInterfaceTemplate(p.id, typeMethode).subscribe({
      next: (tpl) => {
        this.templatePreview.set(tpl);
        this.ifaceForm.patchValue({
          serviceType: tpl.suggestedServiceType,
          providerOperationId: tpl.suggestedOperationId,
          providerApiId: tpl.suggestedApiId,
          codeInterface: tpl.typeMethode,
          nomInterface: `${p.libelle} / ${tpl.typeMethode}`
        });
        this.filterOperationId.set(tpl.suggestedOperationId);
        this.draftParams.set(
          tpl.defaultParametres.map((param) => ({
            localId: newDraftLocalId(),
            id: null,
            codeParametre: param.codeParametre,
            codeParametreMethode: param.codeParametreMethode || '',
            attribut: param.attribut || '',
            sourceParametre: '',
            extractScript: '',
            valeur: ''
          }))
        );
      },
      error: () => this.templatePreview.set(null)
    });
  }

  onOperationPicked(): void {
    const opId = this.ifaceForm.controls.providerOperationId.value;
    this.filterOperationId.set(opId);
    const apiId = this.ifaceForm.controls.providerApiId.value;
    if (apiId && opId) {
      const api = this.apis().find((a) => a.id === apiId);
      if (api && api.operationId !== opId) {
        this.ifaceForm.controls.providerApiId.setValue(null);
      }
    }
    if (!this.ifaceForm.controls.typeMethode.value && opId) {
      const op = this.operations().find((o) => o.id === opId);
      if (op) {
        this.ifaceForm.controls.typeMethode.setValue(op.code);
      }
    }
  }

  saveInterface(): void {
    const p = this.provider();
    if (!p || this.ifaceForm.invalid) return;
    const v = this.ifaceForm.getRawValue();
    const body: InterfaceUtilisateurRequest = {
      codeFse: v.codeFse.trim(),
      serviceType: v.serviceType,
      typeMethode: v.typeMethode.trim().toUpperCase(),
      typeEnvoi: v.typeEnvoi,
      providerOperationId: v.providerOperationId || null,
      providerApiId: v.providerApiId || null,
      codeInterface: v.codeInterface?.trim() || null,
      nomInterface: v.nomInterface?.trim() || null,
      actif: v.actif
    };
    const drafts = this.draftParams();
    this.savingIface.set(true);
    const editId = this.editingId();
    const req$ = editId
      ? this.orchestrationApi.updateInterface(p.id, editId, body)
      : this.orchestrationApi.createInterface(p.id, body);

    req$
      .pipe(
        concatMap((saved) =>
          this.orchestrationApi.listInterfaceParametres(p.id, saved.id).pipe(
            concatMap((existing) =>
              this.syncParametres(p.id, saved.id, drafts, existing).pipe(
                concatMap(() =>
                  this.orchestrationApi.getInterface(p.id, saved.id).pipe(
                    map((refreshed) => ({ editId, refreshed }))
                  )
                )
              )
            )
          )
        )
      )
      .subscribe({
        next: ({ editId: wasEdit, refreshed }) => {
          this.savingIface.set(false);
          this.dialogVisible.set(false);
          this.selectedInterface.set(refreshed);
          this.setMsg(wasEdit ? 'Interface et paramètres mis à jour' : 'Interface créée avec paramètres');
          this.reloadInterfaces(refreshed.id, wasEdit ? this.ifacePage() : 0);
        },
        error: (err: { error?: { message?: string }; message?: string }) => {
          this.savingIface.set(false);
          this.setMsg(err?.error?.message ?? err?.message ?? 'Enregistrement échoué', true);
        }
      });
  }

  addDraftParam(): void {
    this.openAddDraftParam();
  }

  openAddDraftParam(): void {
    this.paramDialogMode.set('draft');
    this.editingDraftIndex.set(null);
    this.editingParamId.set(null);
    this.paramEditForm.reset({
      codeParametre: '',
      codeParametreMethode: '',
      attribut: '',
      sourceParametre: '',
      extractScript: '',
      valeur: ''
    });
    this.paramDialogVisible.set(true);
  }

  openEditDraftParam(index: number): void {
    const draft = this.draftParams()[index];
    if (!draft) return;
    this.paramDialogMode.set('draft');
    this.editingDraftIndex.set(index);
    this.editingParamId.set(null);
    this.paramEditForm.reset({
      codeParametre: draft.codeParametre,
      codeParametreMethode: draft.codeParametreMethode,
      attribut: draft.attribut,
      sourceParametre: draft.sourceParametre,
      extractScript: draft.extractScript,
      valeur: draft.valeur
    });
    this.paramDialogVisible.set(true);
  }

  removeDraftParam(index: number): void {
    this.draftParams.update((list) => list.filter((_, i) => i !== index));
  }

  private saveDraftParamFromDialog(): void {
    if (this.paramEditForm.invalid) return;
    const v = this.paramEditForm.getRawValue();
    const index = this.editingDraftIndex();
    if (index === null) {
      this.draftParams.update((list) => [
        ...list,
        {
          localId: newDraftLocalId(),
          id: null,
          codeParametre: v.codeParametre.trim(),
          codeParametreMethode: v.codeParametreMethode.trim(),
          attribut: v.attribut.trim(),
          sourceParametre: v.sourceParametre.trim(),
          extractScript: v.extractScript.trim(),
          valeur: v.valeur.trim()
        }
      ]);
    } else {
      this.draftParams.update((list) =>
        list.map((item, i) =>
          i === index
            ? {
                ...item,
                codeParametre: v.codeParametre.trim(),
                codeParametreMethode: v.codeParametreMethode.trim(),
                attribut: v.attribut.trim(),
                sourceParametre: v.sourceParametre.trim(),
                extractScript: v.extractScript.trim(),
                valeur: v.valeur.trim()
              }
            : item
        )
      );
    }
    this.paramDialogVisible.set(false);
  }

  private toDraftParams(params: ParametreUtilisateur[]): DraftParametre[] {
    return params.map((param) => ({
      localId: param.id || newDraftLocalId(),
      id: param.id,
      codeParametre: param.codeParametre,
      codeParametreMethode: param.codeParametreMethode || '',
      attribut: param.attribut || '',
      sourceParametre: param.sourceParametre || '',
      extractScript: param.extractScript || '',
      valeur: param.valeur || ''
    }));
  }

  private normalizeDrafts(drafts: DraftParametre[]): DraftParametre[] {
    return drafts
      .map((d) => ({
        ...d,
        codeParametre: d.codeParametre.trim(),
        codeParametreMethode: d.codeParametreMethode.trim(),
        attribut: d.attribut.trim(),
        sourceParametre: d.sourceParametre.trim(),
        extractScript: d.extractScript.trim(),
        valeur: d.valeur.trim()
      }))
      .filter((d) => d.codeParametre.length > 0);
  }

  private syncParametres(
    providerId: string,
    interfaceId: string,
    drafts: DraftParametre[],
    existing: ParametreUtilisateur[]
  ) {
    const desired = this.normalizeDrafts(drafts);
    const existingById = new Map(existing.map((p) => [p.id, p]));
    const existingByCode = new Map(existing.map((p) => [p.codeParametre.toUpperCase(), p]));
    const keptIds = new Set<string>();
    const ops: Observable<unknown>[] = [];

    desired.forEach((draft, index) => {
      const body = {
        nomParametre: draft.codeParametre,
        codeParametre: draft.codeParametre,
        codeParametreMethode: draft.codeParametreMethode || null,
        attribut: draft.attribut || null,
        sourceParametre: draft.sourceParametre || null,
        extractScript: draft.extractScript || null,
        valeur: draft.valeur || null,
        typeParametre: 'STRING',
        ordre: index + 1
      };
      const byId = draft.id ? existingById.get(draft.id) : undefined;
      const byCode = existingByCode.get(draft.codeParametre.toUpperCase());
      const match = byId ?? byCode;
      if (match) {
        keptIds.add(match.id);
        const changed =
          match.codeParametre !== body.codeParametre ||
          (match.codeParametreMethode || '') !== (body.codeParametreMethode || '') ||
          (match.attribut || '') !== (body.attribut || '') ||
          (match.sourceParametre || '') !== (body.sourceParametre || '') ||
          (match.extractScript || '') !== (body.extractScript || '') ||
          (match.valeur || '') !== (body.valeur || '') ||
          match.ordre !== body.ordre;
        if (changed) {
          ops.push(
            this.orchestrationApi.updateInterfaceParametre(providerId, interfaceId, match.id, body)
          );
        }
      } else {
        ops.push(this.orchestrationApi.addInterfaceParametre(providerId, interfaceId, body));
      }
    });

    existing.forEach((param) => {
      if (!keptIds.has(param.id)) {
        ops.push(
          this.orchestrationApi.deleteInterfaceParametre(providerId, interfaceId, param.id).pipe(
            map(() => null)
          )
        );
      }
    });

    if (!ops.length) {
      return of(null);
    }
    return concat(...ops).pipe(last(), map(() => null));
  }

  deleteInterface(iface: InterfaceUtilisateur): void {
    const p = this.provider();
    if (!p) return;
    if (!confirm(`Supprimer l'interface ${iface.typeMethode} (${iface.codeFse}) ?`)) return;
    this.orchestrationApi.deleteInterface(p.id, iface.id).subscribe({
      next: () => {
        this.setMsg('Interface supprimée');
        if (this.selectedInterface()?.id === iface.id) {
          this.selectedInterface.set(null);
        }
        this.reloadInterfaces();
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.setMsg(err?.error?.message ?? err?.message ?? 'Suppression échouée', true);
      }
    });
  }

  seedInterfaces(): void {
    const p = this.provider();
    if (!p) return;
    this.seeding.set(true);
    this.orchestrationApi.seedInterfaces(p.id).subscribe({
      next: (res) => {
        this.seeding.set(false);
        this.setMsg(`Interfaces générées : ${res.typeMethodesUpserted.join(', ') || 'aucune'}`);
        this.reloadInterfaces(this.selectedInterface()?.id, 0);
      },
      error: (err: { message?: string }) => {
        this.seeding.set(false);
        this.setMsg(err?.message ?? 'Seed interfaces échoué', true);
      }
    });
  }

  seedDefaultParams(iface: InterfaceUtilisateur): void {
    const p = this.provider();
    if (!p) return;
    this.seedingParams.set(true);
    this.orchestrationApi.seedInterfaceDefaultParams(p.id, iface.id).subscribe({
      next: (updated) => {
        this.seedingParams.set(false);
        this.setMsg('Paramètres par défaut complétés');
        this.selectedInterface.set(updated);
        this.interfaces.update((list) => list.map((i) => (i.id === updated.id ? updated : i)));
      },
      error: (err: { message?: string }) => {
        this.seedingParams.set(false);
        this.setMsg(err?.message ?? 'Complément paramètres échoué', true);
      }
    });
  }

  openAddLiveParam(): void {
    this.paramDialogMode.set('live');
    this.editingParamId.set(null);
    this.editingDraftIndex.set(null);
    this.paramEditForm.reset({
      codeParametre: '',
      codeParametreMethode: '',
      attribut: '',
      sourceParametre: '',
      extractScript: '',
      valeur: ''
    });
    this.paramDialogVisible.set(true);
  }

  openEditParam(param: ParametreUtilisateur): void {
    this.paramDialogMode.set('live');
    this.editingParamId.set(param.id);
    this.editingDraftIndex.set(null);
    this.paramEditForm.reset({
      codeParametre: param.codeParametre,
      codeParametreMethode: param.codeParametreMethode || '',
      attribut: param.attribut || '',
      sourceParametre: param.sourceParametre || '',
      extractScript: param.extractScript || '',
      valeur: param.valeur || ''
    });
    this.paramDialogVisible.set(true);
  }

  saveParamDialog(): void {
    if (this.paramDialogMode() === 'draft') {
      this.saveDraftParamFromDialog();
      return;
    }
    if (this.editingParamId()) {
      this.saveParamEdit();
    } else {
      this.addParam();
    }
  }

  saveParamEdit(): void {
    const p = this.provider();
    const iface = this.selectedInterface();
    const paramId = this.editingParamId();
    if (!p || !iface || !paramId || this.paramEditForm.invalid) return;
    const v = this.paramEditForm.getRawValue();
    this.savingParam.set(true);
    this.orchestrationApi.updateInterfaceParametre(p.id, iface.id, paramId, {
      nomParametre: v.codeParametre,
      codeParametre: v.codeParametre,
      codeParametreMethode: v.codeParametreMethode || null,
      attribut: v.attribut || null,
      sourceParametre: v.sourceParametre || null,
      extractScript: v.extractScript || null,
      valeur: v.valeur || null,
      typeParametre: 'STRING',
      ordre: null
    }).subscribe({
      next: () => {
        this.savingParam.set(false);
        this.paramDialogVisible.set(false);
        this.reloadInterfaces(iface.id);
      },
      error: (err: { message?: string }) => {
        this.savingParam.set(false);
        this.setMsg(err?.message ?? 'Modification paramètre échouée', true);
      }
    });
  }

  addParam(): void {
    const p = this.provider();
    const iface = this.selectedInterface();
    if (!p || !iface || this.paramEditForm.invalid) return;
    const v = this.paramEditForm.getRawValue();
    this.savingParam.set(true);
    this.orchestrationApi.addInterfaceParametre(p.id, iface.id, {
      nomParametre: v.codeParametre,
      codeParametre: v.codeParametre,
      codeParametreMethode: v.codeParametreMethode || null,
      attribut: v.attribut || null,
      sourceParametre: v.sourceParametre || null,
      extractScript: v.extractScript || null,
      valeur: v.valeur || null,
      typeParametre: 'STRING',
      ordre: (iface.parametres?.length ?? 0) + 1
    }).subscribe({
      next: () => {
        this.savingParam.set(false);
        this.paramDialogVisible.set(false);
        this.paramEditForm.reset({ codeParametre: '', codeParametreMethode: '', attribut: '', sourceParametre: '', extractScript: '', valeur: '' });
        this.reloadInterfaces(iface.id);
      },
      error: (err: { message?: string }) => {
        this.savingParam.set(false);
        this.setMsg(err?.message ?? 'Ajout paramètre échoué', true);
      }
    });
  }

  deleteParam(param: ParametreUtilisateur): void {
    const p = this.provider();
    const iface = this.selectedInterface();
    if (!p || !iface) return;
    this.orchestrationApi.deleteInterfaceParametre(p.id, iface.id, param.id).subscribe({
      next: () => {
        const nextParams = (iface.parametres ?? []).filter((x) => x.id !== param.id);
        const updated = { ...iface, parametres: nextParams };
        this.selectedInterface.set(updated);
        this.interfaces.update((list) => list.map((i) => (i.id === iface.id ? updated : i)));
      },
      error: (err: { message?: string }) => {
        this.setMsg(err?.message ?? 'Suppression paramètre échouée', true);
      }
    });
  }

  runMatch(): void {
    if (this.matchForm.invalid) return;
    this.matching.set(true);
    const v = this.matchForm.getRawValue();
    this.orchestrationApi.matchSchema({
      serviceType: 'TRANSFERT',
      sourceCodeFse: v.sourceCodeFse,
      destCodeFse: v.destCodeFse
    }).subscribe({
      next: (res) => {
        this.matchResult.set(res);
        this.matching.set(false);
      },
      error: (err: { message?: string }) => {
        this.matchResult.set(null);
        this.matching.set(false);
        this.setMsg(err?.message ?? 'Matching échoué', true);
      }
    });
  }

  private reloadInterfaces(selectId?: string | null, pageIndex?: number): void {
    const p = this.provider();
    if (!p) return;
    const page = pageIndex ?? this.ifacePage();
    this.loadingInterfaces.set(true);
    this.ifacePage.set(page);
    this.orchestrationApi.listInterfacesPage(p.id, page, this.ifacePageSize()).subscribe({
      next: (result) => {
        this.interfaces.set(result.content ?? []);
        this.totalInterfaces.set(result.totalElements ?? 0);
        this.loadingInterfaces.set(false);
        if (selectId) {
          const fromPage = (result.content ?? []).find((i) => i.id === selectId);
          if (fromPage) {
            this.selectedInterface.set(fromPage);
          } else {
            this.orchestrationApi.getInterface(p.id, selectId).subscribe({
              next: (iface) => this.selectedInterface.set(iface),
              error: () => this.selectedInterface.set(null)
            });
          }
        }
      },
      error: (err: { message?: string }) => {
        this.loadingInterfaces.set(false);
        this.setMsg(err?.message ?? 'Chargement interfaces échoué', true);
      }
    });
  }

  private setMsg(text: string, error = false): void {
    this.msg.set(text);
    this.msgError.set(error);
  }
}
