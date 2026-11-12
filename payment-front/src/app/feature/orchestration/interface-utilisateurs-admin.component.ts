import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { concat, forkJoin, Observable, of } from 'rxjs';
import { catchError, concatMap, last, map } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
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
  selector: 'app-interface-utilisateurs-admin',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    RouterLink,
    ButtonModule,
    CheckboxModule,
    DialogModule,
    DropdownModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule,
    TooltipModule
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Interfaces utilisateur</h1>
          <p class="subtitle">
            CRUD mapping codeFse + typeMethode → opération / endpoint (équivalent Omnicanal InterfaceUtilisateur)
          </p>
        </div>
        <div class="header-actions">
          @if (selectedProvider()?.codeFse) {
            <button
              pButton
              type="button"
              class="p-button-outlined"
              label="Générer depuis APIs"
              icon="pi pi-sync"
              [loading]="seeding()"
              [disabled]="!selectedProviderId()"
              (click)="seedInterfaces()"
            ></button>
          }
          <button
            pButton
            type="button"
            label="Nouvelle interface"
            icon="pi pi-plus"
            [disabled]="!selectedProviderId()"
            (click)="openCreate()"
          ></button>
        </div>
      </div>

      @if (loadingProviders()) {
        <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" /></div>
      } @else {
        <section class="gw-panel provider-bar">
          <label for="provider-select">Fournisseur</label>
          <p-dropdown
            inputId="provider-select"
            [options]="providers()"
            optionLabel="libelle"
            optionValue="id"
            placeholder="Choisir un fournisseur…"
            [ngModel]="selectedProviderId()"
            (ngModelChange)="onProviderChange($event)"
            [showClear]="true"
            [filter]="true"
            filterBy="libelle,code"
            styleClass="provider-dropdown"
          />
          @if (selectedProvider(); as p) {
            <div class="provider-meta">
              <span>Code {{ p.code }}</span>
              @if (p.codeFse) {
                <span>· FSE {{ p.codeFse }}</span>
              }
              <a
                pButton
                class="p-button-text p-button-sm"
                [routerLink]="['/admin/providers', p.id, 'apis']"
                label="APIs & Méthodes"
              ></a>
            </div>
          }
        </section>

        @if (msg()) {
          <p class="gw-msg" [class.error]="msgError()">{{ msg() }}</p>
        }

        @if (!selectedProviderId()) {
          <p class="hint">Sélectionnez un fournisseur pour gérer ses interfaces utilisateur.</p>
        } @else {
          <section class="gw-panel">
            <div class="panel-header">
              <h3>Interfaces · {{ totalInterfaces() }}</h3>
            </div>

            <p-table
              [value]="interfaces()"
              styleClass="p-datatable-sm p-datatable-striped p-datatable-gridlines"
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
                  <th>Envoi</th>
                  <th>Opération</th>
                  <th>Endpoint</th>
                  <th>Params</th>
                  <th>Actif</th>
                  <th style="width: 12rem"></th>
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-i>
                <tr [class.selected]="selectedInterface()?.id === i.id">
                  <td>{{ i.codeFse }}</td>
                  <td><code>{{ i.typeMethode }}</code></td>
                  <td>{{ i.serviceType }}</td>
                  <td>{{ i.typeEnvoi }}</td>
                  <td>{{ i.providerOperationCode ?? '—' }}</td>
                  <td>{{ i.providerApiCode ?? '—' }}</td>
                  <td>{{ i.parametres?.length ?? 0 }}</td>
                  <td>
                    <p-tag [value]="i.actif ? 'Oui' : 'Non'" [severity]="i.actif ? 'success' : 'secondary'" />
                  </td>
                  <td class="row-actions">
                    @if (!i.actif) {
                      <button
                        pButton
                        type="button"
                        class="p-button-text p-button-success p-button-sm"
                        icon="pi pi-check"
                        pTooltip="Publier"
                        [disabled]="!canPublishInterface(i)"
                        (click)="publishInterface(i)"
                      ></button>
                    } @else {
                      <button
                        pButton
                        type="button"
                        class="p-button-text p-button-warning p-button-sm"
                        icon="pi pi-ban"
                        pTooltip="Dépublier"
                        (click)="unpublishInterface(i)"
                      ></button>
                    }
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
                  <td colspan="9">
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
                <h3>Paramètres — {{ iface.typeMethode }}</h3>
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
                <p class="hint">Aucun paramètre. Cliquez sur « Ajouter ».</p>
              } @else {
                <p-table
                  [value]="iface.parametres!"
                  styleClass="p-datatable-sm p-datatable-striped"
                  dataKey="id"
                >
                  <ng-template pTemplate="header">
                    <tr>
                      <th>Code</th>
                      <th>→ Méthode (source)</th>
                      <th>Attribut</th>
                      <th>Défaut</th>
                      <th></th>
                    </tr>
                  </ng-template>
                  <ng-template pTemplate="body" let-p>
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
                  </ng-template>
                </p-table>
              }
            </section>
          }
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
        <form [formGroup]="ifaceForm" (ngSubmit)="saveInterface()" class="gw-form-grid dialog-form">
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
            <p-checkbox formControlName="actif" [binary]="true" inputId="iface-actif" />
            <label for="iface-actif">Actif</label>
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
              <p-table
                [value]="draftParams()"
                styleClass="p-datatable-sm p-datatable-striped"
                dataKey="localId"
              >
                <ng-template pTemplate="header">
                  <tr>
                    <th>Code</th>
                    <th>→ Méthode (source)</th>
                    <th>Attribut</th>
                    <th>Défaut</th>
                    <th style="width: 6rem"></th>
                  </tr>
                </ng-template>
                <ng-template pTemplate="body" let-p let-i="rowIndex">
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
                </ng-template>
              </p-table>
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
        <form [formGroup]="paramEditForm" (ngSubmit)="saveParamDialog()" class="gw-form-grid dialog-form single-col">
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
            <textarea
              formControlName="extractScript"
              class="w-full"
              rows="3"
              placeholder="def p = (source ?: '').tokenize('-'); return p[3]"
            ></textarea>
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
    </div>
  `,
  styles: `
    .hint { color: var(--text-color-secondary, #64748b); font-size: 0.9rem; }
    code { font-size: 0.85em; }
    tr.selected { background: color-mix(in srgb, var(--primary-color, #2563eb) 8%, transparent); }
    .param-form { margin-top: 1rem; }
    .provider-bar {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 1rem;
    }
    .provider-bar label { font-weight: 600; margin: 0; }
    :host ::ng-deep .provider-dropdown { min-width: 280px; }
    .provider-meta {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.5rem;
      color: var(--text-color-secondary, #64748b);
      font-size: 0.9rem;
    }
    .panel-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 0.75rem;
    }
    .panel-header h3 { margin: 0; }
    .panel-actions { display: flex; align-items: center; gap: 0.35rem; }
    .header-actions { display: flex; flex-wrap: wrap; gap: 0.5rem; }
    .row-actions { display: flex; align-items: center; gap: 0.15rem; white-space: nowrap; }
    .gw-msg.error { color: #b91c1c; }
    .dialog-form { display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem 1rem; }
    .dialog-form.single-col { grid-template-columns: 1fr; }
    .dialog-form .gw-field { display: flex; flex-direction: column; gap: 0.35rem; }
    .template-preview {
      grid-column: 1 / -1;
      padding: 0.75rem;
      border-radius: 8px;
      border: 1px solid #dbeafe;
      background: #f8fafc;
    }
    .preview-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.85rem;
      margin-top: 0.5rem;
    }
    .preview-table th,
    .preview-table td {
      border: 1px solid #e2e8f0;
      padding: 0.35rem 0.5rem;
      text-align: left;
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
    .dialog-form .checkbox-field {
      flex-direction: row;
      align-items: center;
      gap: 0.5rem;
      grid-column: 1 / -1;
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
export class InterfaceUtilisateursAdminComponent implements OnInit {
  private readonly providersApi = inject(ProviderService);
  private readonly orchestrationApi = inject(OrchestrationService);
  private readonly fb = inject(FormBuilder);

  readonly loadingProviders = signal(true);
  readonly loadingInterfaces = signal(false);
  readonly seeding = signal(false);
  readonly seedingParams = signal(false);
  readonly savingParam = signal(false);
  readonly savingIface = signal(false);
  readonly msg = signal<string | null>(null);
  readonly msgError = signal(false);
  readonly providers = signal<Provider[]>([]);
  readonly selectedProviderId = signal<string | null>(null);
  readonly selectedProvider = signal<Provider | null>(null);
  readonly interfaces = signal<InterfaceUtilisateur[]>([]);
  readonly totalInterfaces = signal(0);
  readonly ifacePage = signal(0);
  readonly ifacePageSize = signal(10);
  readonly selectedInterface = signal<InterfaceUtilisateur | null>(null);
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
    forkJoin({
      providers: this.providersApi.list(),
      typeMethodes: this.providersApi.listOmnicanalTypeMethodes().pipe(catchError(() => of([] as string[])))
    }).subscribe({
      next: ({ providers, typeMethodes }) => {
        this.providers.set(providers);
        this.typeMethodesCatalog.set(typeMethodes);
        this.loadingProviders.set(false);
        const withFse = providers.find((p) => p.codeFse);
        if (withFse) {
          this.onProviderChange(withFse.id);
        }
      },
      error: (err: { message?: string }) => {
        this.setMsg(err?.message ?? 'Chargement fournisseurs échoué', true);
        this.loadingProviders.set(false);
      }
    });
  }

  onProviderChange(providerId: string | null): void {
    this.selectedProviderId.set(providerId);
    this.selectedInterface.set(null);
    this.interfaces.set([]);
    this.totalInterfaces.set(0);
    this.ifacePage.set(0);
    this.operations.set([]);
    this.apis.set([]);
    this.msg.set(null);

    if (!providerId) {
      this.selectedProvider.set(null);
      return;
    }

    const provider = this.providers().find((p) => p.id === providerId) ?? null;
    this.selectedProvider.set(provider);
    this.loadInterfaces(0);
    forkJoin({
      ops: this.providersApi.listOperations(providerId).pipe(catchError(() => of([] as Operation[]))),
      apis: this.providersApi.listApis(providerId).pipe(catchError(() => of([] as ProviderApi[])))
    }).subscribe({
      next: ({ ops, apis }) => {
        this.operations.set(ops);
        this.apis.set(apis);
      }
    });
  }

  private loadInterfaces(pageIndex: number): void {
    const providerId = this.selectedProviderId();
    if (!providerId) return;
    this.loadingInterfaces.set(true);
    this.ifacePage.set(pageIndex);
    this.orchestrationApi.listInterfacesPage(providerId, pageIndex, this.ifacePageSize()).subscribe({
      next: (page) => {
        this.interfaces.set(page.content ?? []);
        this.totalInterfaces.set(page.totalElements ?? 0);
        this.loadingInterfaces.set(false);
        const selectedId = this.selectedInterface()?.id;
        if (selectedId) {
          const fromPage = (page.content ?? []).find((i) => i.id === selectedId);
          if (fromPage) {
            this.selectedInterface.set(fromPage);
          }
        }
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.setMsg(err?.error?.message ?? err?.message ?? 'Chargement interfaces échoué', true);
        this.interfaces.set([]);
        this.totalInterfaces.set(0);
        this.loadingInterfaces.set(false);
      }
    });
  }

  onInterfacesLazyLoad(ev: TableLazyLoadEvent): void {
    if (!this.selectedProviderId()) return;
    const rows = ev.rows ?? this.ifacePageSize();
    const first = ev.first ?? 0;
    this.ifacePageSize.set(rows);
    this.loadInterfaces(Math.floor(first / rows));
  }

  openCreate(): void {
    const p = this.selectedProvider();
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
    if (this.editingId()) {
      return;
    }
    const providerId = this.selectedProviderId();
    const typeMethode = this.ifaceForm.controls.typeMethode.value?.trim();
    if (!providerId || !typeMethode) {
      this.templatePreview.set(null);
      return;
    }
    this.orchestrationApi.previewInterfaceTemplate(providerId, typeMethode).subscribe({
      next: (tpl) => {
        this.templatePreview.set(tpl);
        this.applyTemplatePreview(tpl);
      },
      error: () => this.templatePreview.set(null)
    });
  }

  private applyTemplatePreview(tpl: InterfaceTemplatePreview): void {
    const p = this.selectedProvider();
    this.ifaceForm.patchValue({
      serviceType: tpl.suggestedServiceType,
      providerOperationId: tpl.suggestedOperationId,
      providerApiId: tpl.suggestedApiId,
      codeInterface: tpl.typeMethode,
      nomInterface: p ? `${p.libelle} / ${tpl.typeMethode}` : tpl.typeMethode
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
    const typeMethode = this.ifaceForm.controls.typeMethode.value;
    if (!typeMethode && opId) {
      const op = this.operations().find((o) => o.id === opId);
      if (op) {
        this.ifaceForm.controls.typeMethode.setValue(op.code);
      }
    }
  }

  saveInterface(): void {
    const providerId = this.selectedProviderId();
    if (!providerId || this.ifaceForm.invalid) return;
    const v = this.ifaceForm.getRawValue();
    const editId = this.editingId();
    const body: InterfaceUtilisateurRequest = {
      codeFse: v.codeFse.trim(),
      serviceType: v.serviceType,
      typeMethode: v.typeMethode.trim().toUpperCase(),
      typeEnvoi: v.typeEnvoi,
      providerOperationId: v.providerOperationId || null,
      providerApiId: v.providerApiId || null,
      codeInterface: v.codeInterface?.trim() || null,
      nomInterface: v.nomInterface?.trim() || null,
      // Création : toujours inactif ; activation via « Publier ».
      actif: editId ? v.actif : false
    };
    const drafts = this.draftParams();
    this.savingIface.set(true);
    const req$ = editId
      ? this.orchestrationApi.updateInterface(providerId, editId, body)
      : this.orchestrationApi.createInterface(providerId, body);

    req$
      .pipe(
        concatMap((saved) =>
          this.orchestrationApi.listInterfaceParametres(providerId, saved.id).pipe(
            concatMap((existing) =>
              this.syncParametres(providerId, saved.id, drafts, existing).pipe(
                concatMap(() =>
                  this.orchestrationApi.getInterface(providerId, saved.id).pipe(
                    map((refreshed) => refreshed)
                  )
                )
              )
            )
          )
        )
      )
      .subscribe({
        next: (refreshed) => {
          this.savingIface.set(false);
          this.dialogVisible.set(false);
          this.selectedInterface.set(refreshed);
          this.setMsg(editId ? 'Interface et paramètres mis à jour' : 'Interface créée avec paramètres');
          this.loadInterfaces(editId ? this.ifacePage() : 0);
        },
        error: (err: { error?: { message?: string }; message?: string }) => {
          this.savingIface.set(false);
          this.setMsg(err?.error?.message ?? err?.message ?? 'Enregistrement échoué', true);
        }
      });
  }

  /** Miroir des règles serveur INTERFACE_NOT_PUBLISHABLE. */
  canPublishInterface(iface: InterfaceUtilisateur): boolean {
    if (!iface.providerOperationId && !iface.providerApiId) return false;
    return (iface.parametres?.length ?? 0) > 0;
  }

  publishInterface(iface: InterfaceUtilisateur): void {
    const providerId = this.selectedProviderId();
    if (!providerId || !this.canPublishInterface(iface)) return;
    this.orchestrationApi.publishInterface(providerId, iface.id).subscribe({
      next: (saved) => {
        this.setMsg('Interface publiée');
        if (this.selectedInterface()?.id === iface.id) {
          this.selectedInterface.set(saved);
        }
        this.loadInterfaces(this.ifacePage());
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.setMsg(err?.error?.message ?? err?.message ?? 'Publication refusée', true);
      }
    });
  }

  unpublishInterface(iface: InterfaceUtilisateur): void {
    const providerId = this.selectedProviderId();
    if (!providerId) return;
    this.orchestrationApi.unpublishInterface(providerId, iface.id).subscribe({
      next: (saved) => {
        this.setMsg('Interface dépubliée');
        if (this.selectedInterface()?.id === iface.id) {
          this.selectedInterface.set(saved);
        }
        this.loadInterfaces(this.ifacePage());
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.setMsg(err?.error?.message ?? err?.message ?? 'Dépublication échouée', true);
      }
    });
  }

  deleteInterface(iface: InterfaceUtilisateur): void {
    const providerId = this.selectedProviderId();
    if (!providerId) return;
    if (!confirm(`Supprimer l'interface ${iface.typeMethode} (${iface.codeFse}) ?`)) return;
    this.orchestrationApi.deleteInterface(providerId, iface.id).subscribe({
      next: () => {
        this.setMsg('Interface supprimée');
        if (this.selectedInterface()?.id === iface.id) {
          this.selectedInterface.set(null);
        }
        this.loadInterfaces(this.ifacePage());
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.setMsg(err?.error?.message ?? err?.message ?? 'Suppression échouée', true);
      }
    });
  }

  selectInterface(iface: InterfaceUtilisateur): void {
    this.selectedInterface.set(iface);
  }

  seedInterfaces(): void {
    const providerId = this.selectedProviderId();
    if (!providerId) return;
    this.seeding.set(true);
    this.orchestrationApi.seedInterfaces(providerId).subscribe({
      next: (res) => {
        this.seeding.set(false);
        this.setMsg(`Interfaces générées : ${res.typeMethodesUpserted.join(', ') || 'aucune'}`);
        this.loadInterfaces(0);
      },
      error: (err: { message?: string }) => {
        this.seeding.set(false);
        this.setMsg(err?.message ?? 'Génération interfaces échouée', true);
      }
    });
  }

  seedDefaultParams(iface: InterfaceUtilisateur): void {
    const providerId = this.selectedProviderId();
    if (!providerId) return;
    this.seedingParams.set(true);
    this.orchestrationApi.seedInterfaceDefaultParams(providerId, iface.id).subscribe({
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
    const providerId = this.selectedProviderId();
    const iface = this.selectedInterface();
    const paramId = this.editingParamId();
    if (!providerId || !iface || !paramId || this.paramEditForm.invalid) return;
    const v = this.paramEditForm.getRawValue();
    this.savingParam.set(true);
    this.orchestrationApi.updateInterfaceParametre(providerId, iface.id, paramId, {
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
        this.refreshInterface(iface.id);
      },
      error: (err: { message?: string }) => {
        this.savingParam.set(false);
        this.setMsg(err?.message ?? 'Modification paramètre échouée', true);
      }
    });
  }

  addParam(): void {
    const providerId = this.selectedProviderId();
    const iface = this.selectedInterface();
    if (!providerId || !iface || this.paramEditForm.invalid) return;
    const v = this.paramEditForm.getRawValue();
    this.savingParam.set(true);
    this.orchestrationApi.addInterfaceParametre(providerId, iface.id, {
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
        this.refreshInterface(iface.id);
      },
      error: (err: { message?: string }) => {
        this.savingParam.set(false);
        this.setMsg(err?.message ?? 'Ajout paramètre échoué', true);
      }
    });
  }

  deleteParam(param: ParametreUtilisateur): void {
    const providerId = this.selectedProviderId();
    const iface = this.selectedInterface();
    if (!providerId || !iface) return;
    this.orchestrationApi.deleteInterfaceParametre(providerId, iface.id, param.id).subscribe({
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

  private refreshInterface(interfaceId: string): void {
    const providerId = this.selectedProviderId();
    if (!providerId) return;
    this.orchestrationApi.getInterface(providerId, interfaceId).subscribe({
      next: (updated) => {
        this.selectedInterface.set(updated);
        this.interfaces.update((list) => list.map((i) => (i.id === interfaceId ? updated : i)));
      }
    });
  }

  private setMsg(text: string, error = false): void {
    this.msg.set(text);
    this.msgError.set(error);
  }
}
