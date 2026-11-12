import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { SharedModule } from 'primeng/api';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { OrchestrationService } from '../../core/service/orchestration.service';
import {
  ImputeAgence,
  LigneSchemaEcriture,
  LigneSchemaEcritureRequest,
  OrchestrationCatalog,
  ParametreCategorie,
  ParametreSchema,
  ProprieteApi,
  SchemaEcriture,
  SERVICE_TYPE_CODES,
  ServiceTypeCode
} from '../../core/model/api.models';

const SERVICE_TYPES = SERVICE_TYPE_CODES;

const PROPRIETE_APIS: ProprieteApi[] = ['FSESOURCE', 'FSEDESTINATAIRE', 'FSEPARTENAIRE'];
const IMPUTE_AGENCES: ImputeAgence[] = ['AGENCESOURCE', 'AGENCEDEST', 'AGENCESIEGESOURCE', 'AGENCESIEGEDEST'];
const PARAM_CATEGORIES: ParametreCategorie[] = ['USER', 'COMPTE', 'AGENCE', 'SYSTEM'];

const EMPTY_CATALOG: OrchestrationCatalog = {
  patternCodes: [],
  typeMethodes: ['DEBIT', 'CREDIT', 'TRANSFERT', 'CONSULTATION'],
  executionModes: ['SYNC', 'ASYNC_CALLBACK', 'ASYNC_POLL', 'FIRE_AND_FORGET'],
  timeoutPolicies: ['SHORT_3S', 'STANDARD_5S', 'FINANCIAL_15S', 'LONG_30S'],
  callbackPolicies: ['NONE', 'HTTP_CALLBACK', 'POLL_STATUS'],
  retryPolicies: ['NONE', 'TRANSIENT_3X', 'TRANSIENT_2X_BACKOFF'],
  idempotencyPolicies: ['NONE', 'KEY_REFERENCE', 'KEY_OP_STEP'],
  compensationPolicies: ['NONE', 'AUTO_REVERSE', 'MANUAL'],
  compensationModes: ['NONE', 'AUTO_REVERSE', 'MANUAL'],
  criticalities: ['CRITICAL', 'REQUIRED', 'OPTIONAL', 'INFORMATIONAL'],
  errorActions: ['STOP_WORKFLOW', 'RETRY', 'CHECK_STATUS', 'COMPENSATE', 'CONTINUE'],
  dependencyConditions: ['ON_SUCCESS', 'ON_FAILURE', 'ALWAYS'],
  paramTypes: ['STRING', 'DOUBLE', 'INTEGER', 'FLOAT', 'LONG']
};

@Component({
  selector: 'app-schema-ecritures-admin',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    ButtonModule,
    CheckboxModule,
    DialogModule,
    SharedModule,
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
          <h1>Schémas d'écriture</h1>
          <p class="subtitle">SchemaEcrit, LigneSchemaEcrit et ParametreSchema</p>
        </div>
        <div class="header-actions">
          <button pButton type="button" label="Nouveau schéma" icon="pi pi-plus" (click)="startNewSchema()"></button>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }
      @if (msg()) {
        <p class="gw-msg">{{ msg() }}</p>
      }

      <div class="layout">
        <aside class="list-panel gw-panel">
          <div class="filter-row">
            <p-dropdown
              [options]="serviceTypeOptions"
              [(ngModel)]="filterServiceType"
              optionLabel="label"
              optionValue="value"
              placeholder="Tous services"
              [showClear]="true"
              (onChange)="reloadList()"
              styleClass="w-full"
            />
          </div>
          @if (loading()) {
            <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
          } @else {
            <ul>
              @for (s of schemas(); track s.id) {
                <li [class.active]="selected()?.id === s.id" (click)="selectSchema(s)">
                  <strong>{{ s.nomSchema }}</strong>
                  <span>{{ s.serviceType }} · <code>{{ s.patternCode }}</code></span>
                </li>
              }
            </ul>
          }
        </aside>

        <section class="detail-panel gw-panel">
          @if (editingSchema()) {
            <h2>{{ selected() ? 'Modifier schéma' : 'Nouveau schéma' }}</h2>
            <form [formGroup]="schemaForm" (ngSubmit)="saveSchema()" class="gw-form-grid">
              <div class="gw-field">
                <label for="nomSchema">Nom</label>
                <input pInputText id="nomSchema" formControlName="nomSchema" class="w-full" />
              </div>
              <div class="gw-field">
                <label>Service</label>
                <p-dropdown formControlName="serviceType" [options]="serviceTypeOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
              </div>
              <div class="gw-field">
                <label>Pattern code</label>
                <p-dropdown formControlName="patternCode" [options]="opt(catalog().patternCodes)" [editable]="true" optionLabel="label" optionValue="value" styleClass="w-full" />
              </div>
              <div class="gw-field">
                <label>Workflow BPMN</label>
                <input pInputText formControlName="workflowName" class="w-full" placeholder="same_fse, inter_fse" />
              </div>
              <div class="gw-field">
                <label>Version workflow</label>
                <input pInputText type="number" formControlName="workflowVersion" class="w-full" />
              </div>
              <div class="gw-field">
                <label>Moteur</label>
                <input pInputText formControlName="engine" class="w-full" />
              </div>
              <div class="gw-field">
                <label>Politique erreur schéma</label>
                <input pInputText formControlName="errorPolicyCode" class="w-full" />
              </div>
              <div class="gw-field full check-row">
                <p-checkbox formControlName="actif" [binary]="true" inputId="schemaActif" />
                <label for="schemaActif">Actif</label>
              </div>
              <div class="gw-actions full">
                <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelSchemaEdit()"></button>
                <button pButton type="submit" label="Enregistrer" [loading]="busy()"></button>
              </div>
            </form>
          } @else if (selected()) {
            <header class="detail-head">
              <div>
                <h2>{{ selected()!.nomSchema }}</h2>
                <p>
                  {{ selected()!.serviceType }} · <code>{{ selected()!.patternCode }}</code>
                  · {{ selected()!.workflowName ?? '—' }} v{{ selected()!.workflowVersion }}
                  · {{ selected()!.actif ? 'Actif' : 'Inactif' }}
                </p>
              </div>
              <div class="head-actions">
                @if (!selected()!.actif) {
                  <button
                    pButton
                    type="button"
                    label="Publier"
                    icon="pi pi-check"
                    class="p-button-sm p-button-success"
                    [disabled]="!canPublishSchema() || busy()"
                    [title]="publishBlockedReason() || 'Publier le schéma'"
                    (click)="publishSchema()"
                  ></button>
                } @else {
                  <button
                    pButton
                    type="button"
                    label="Dépublier"
                    icon="pi pi-ban"
                    class="p-button-sm p-button-outlined p-button-warning"
                    [disabled]="busy()"
                    (click)="unpublishSchema()"
                  ></button>
                }
                <button pButton type="button" icon="pi pi-pencil" class="p-button-rounded p-button-text" (click)="editSchema()" title="Modifier"></button>
                <button pButton type="button" icon="pi pi-trash" class="p-button-rounded p-button-text p-button-danger" (click)="deleteSchema()" title="Supprimer"></button>
              </div>
            </header>

            <div class="section-head">
              <h3>Lignes (LigneSchemaEcrit)</h3>
              <button pButton type="button" label="Ajouter une ligne" icon="pi pi-plus" class="p-button-outlined p-button-sm" (click)="startNewLigne()"></button>
            </div>

            <div class="table-wrap">
              <p-table
                [value]="lignes()"
                dataKey="id"
                styleClass="p-datatable-sm p-datatable-striped p-datatable-gridlines"
                [rowHover]="true"
                [expandedRowKeys]="expandedRows"
              >
                <ng-template pTemplate="header">
                  <tr>
                    <th style="width: 3rem"></th>
                    <th style="width: 4rem">#</th>
                    <th>Step BPMN</th>
                    <th>Type méthode</th>
                    <th>API</th>
                    <th>Agence</th>
                    <th>Exécution</th>
                    <th>Timeout</th>
                    <th>Retry</th>
                    <th>Compensation</th>
                    <th>Criticité</th>
                    <th style="width: 5rem">Params</th>
                    <th style="width: 8rem"></th>
                  </tr>
                </ng-template>
                <ng-template pTemplate="body" let-l let-expanded="expanded">
                  <tr>
                    <td>
                      <button
                        type="button"
                        pButton
                        [pRowToggler]="l"
                        class="p-button-text p-button-rounded p-button-plain"
                        [icon]="expanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
                        pTooltip="Paramètres"
                      ></button>
                    </td>
                    <td>{{ l.ordre }}</td>
                    <td><code>{{ l.stepId ?? '—' }}</code></td>
                    <td>{{ l.typeMethode }}</td>
                    <td><p-tag [value]="l.apiAUtiliser" severity="info" styleClass="text-xs" /></td>
                    <td>{{ l.agenceImputer ?? '—' }}</td>
                    <td>
                      <span>{{ l.executionMode ?? 'SYNC' }}</span>
                      @if (l.blocking) {
                        <p-tag value="blocking" severity="warning" styleClass="text-xs ml-tag" />
                      }
                    </td>
                    <td>{{ l.timeoutPolicy ?? l.timeoutDuration ?? '—' }}</td>
                    <td>{{ l.retryPolicy ?? '—' }}</td>
                    <td>{{ l.compensationPolicy ?? l.compensationMode ?? '—' }}</td>
                    <td>
                      @if (l.criticality) {
                        <p-tag [value]="l.criticality" [severity]="criticalitySeverity(l.criticality)" styleClass="text-xs" />
                      } @else {
                        —
                      }
                    </td>
                    <td>{{ l.parametres?.length ?? 0 }}</td>
                    <td class="gw-row-actions-cell">
                      <button
                        pButton
                        type="button"
                        icon="pi pi-pencil"
                        class="p-button-rounded p-button-text"
                        pTooltip="Modifier"
                        (click)="editLigne(l)"
                      ></button>
                      <button
                        pButton
                        type="button"
                        icon="pi pi-trash"
                        class="p-button-rounded p-button-text p-button-danger"
                        pTooltip="Supprimer"
                        (click)="deleteLigne(l)"
                      ></button>
                    </td>
                  </tr>
                </ng-template>
                <ng-template pTemplate="rowexpansion" let-l>
                  <tr>
                    <td colspan="13">
                      <div class="param-panel">
                        <div class="section-head">
                          <h4>ParametreSchema — {{ l.stepId || ('ligne ' + l.ordre) }}</h4>
                          <button
                            pButton
                            type="button"
                            label="Ajouter un paramètre"
                            icon="pi pi-plus"
                            class="p-button-outlined p-button-sm"
                            (click)="startNewParametre(l)"
                          ></button>
                        </div>
                        <p-table
                          [value]="l.parametres || []"
                          styleClass="p-datatable-sm p-datatable-gridlines"
                          [rowHover]="true"
                        >
                          <ng-template pTemplate="header">
                            <tr>
                              <th>Nom</th>
                              <th>Code</th>
                              <th>Code méthode</th>
                              <th>Externe</th>
                              <th>Type</th>
                              <th>Catégorie</th>
                              <th>Défaut</th>
                              <th style="width: 7rem"></th>
                            </tr>
                          </ng-template>
                          <ng-template pTemplate="body" let-p>
                            <tr>
                              <td>{{ p.nomParametre ?? '—' }}</td>
                              <td><code>{{ p.codeParametre }}</code></td>
                              <td><code>{{ p.codeParametreMethode ?? '—' }}</code></td>
                              <td>{{ p.nomExterne ?? '—' }}</td>
                              <td>{{ p.typeParametre ?? '—' }}</td>
                              <td><p-tag [value]="p.categorie" styleClass="text-xs" /></td>
                              <td>{{ p.valeurDefaut ?? '—' }}</td>
                              <td class="gw-row-actions-cell">
                                <button
                                  pButton
                                  type="button"
                                  icon="pi pi-pencil"
                                  class="p-button-rounded p-button-text"
                                  pTooltip="Modifier"
                                  (click)="editParametre(l, p)"
                                ></button>
                                <button
                                  pButton
                                  type="button"
                                  icon="pi pi-trash"
                                  class="p-button-rounded p-button-text p-button-danger"
                                  pTooltip="Supprimer"
                                  (click)="deleteParametre(p)"
                                ></button>
                              </td>
                            </tr>
                          </ng-template>
                          <ng-template pTemplate="emptymessage">
                            <tr>
                              <td colspan="8">Aucun binding ParametreSchema pour cette ligne.</td>
                            </tr>
                          </ng-template>
                        </p-table>
                      </div>
                    </td>
                  </tr>
                </ng-template>
                <ng-template pTemplate="emptymessage">
                  <tr>
                    <td colspan="13">Aucune ligne. Ajoutez une jambe (DEBIT, CREDIT, TRANSFERT…).</td>
                  </tr>
                </ng-template>
              </p-table>
            </div>
          } @else {
            <p class="hint">Sélectionnez un schéma ou créez-en un nouveau.</p>
          }
        </section>
      </div>
    </div>

    <p-dialog
      [header]="editingLigneId() ? 'Modifier la ligne' : 'Nouvelle ligne'"
      [visible]="ligneDialogOpen()"
      (visibleChange)="ligneDialogOpen.set($event)"
      [modal]="true"
      [style]="{ width: '760px' }"
      [dismissableMask]="true"
    >
      <form [formGroup]="ligneForm" class="gw-form-grid">
        <fieldset class="group full">
          <legend>Identité</legend>
          <div class="gw-form-grid">
            <div class="gw-field">
              <label>Ordre</label>
              <input pInputText type="number" formControlName="ordre" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Step ID BPMN</label>
              <input pInputText formControlName="stepId" class="w-full" placeholder="transfertSource" />
            </div>
            <div class="gw-field">
              <label>Type méthode</label>
              <p-dropdown formControlName="typeMethode" [options]="opt(catalog().typeMethodes)" [editable]="true" optionLabel="label" optionValue="value" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>API à utiliser</label>
              <p-dropdown formControlName="apiAUtiliser" [options]="proprieteApiOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Agence imputer</label>
              <p-dropdown formControlName="agenceImputer" [options]="imputeAgenceOptions" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
          </div>
        </fieldset>

        <fieldset class="group full">
          <legend>Exécution</legend>
          <div class="gw-form-grid">
            <div class="gw-field">
              <label>Mode</label>
              <p-dropdown formControlName="executionMode" [options]="opt(catalog().executionModes)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Criticité</label>
              <p-dropdown formControlName="criticality" [options]="opt(catalog().criticalities)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field check-row">
              <p-checkbox formControlName="blocking" [binary]="true" inputId="ligneBlocking" />
              <label for="ligneBlocking">Blocking</label>
            </div>
          </div>
        </fieldset>

        <fieldset class="group full">
          <legend>Timeout &amp; retry</legend>
          <div class="gw-form-grid">
            <div class="gw-field">
              <label>Timeout policy</label>
              <p-dropdown formControlName="timeoutPolicy" [options]="opt(catalog().timeoutPolicies)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Timeout duration</label>
              <p-dropdown formControlName="timeoutDuration" [options]="opt(catalog().timeoutPolicies)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Callback</label>
              <p-dropdown formControlName="callbackPolicy" [options]="opt(catalog().callbackPolicies)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Status method</label>
              <input pInputText formControlName="statusMethod" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Retry policy</label>
              <p-dropdown formControlName="retryPolicy" [options]="opt(catalog().retryPolicies)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Retry max</label>
              <input pInputText type="number" formControlName="retryMaxAttempts" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Délai initial (ms)</label>
              <input pInputText type="number" formControlName="retryInitialDelayMs" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Multiplicateur</label>
              <input pInputText type="number" step="0.1" formControlName="retryMultiplier" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Délai max (ms)</label>
              <input pInputText type="number" formControlName="retryMaxDelayMs" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Codes retryables</label>
              <input pInputText formControlName="retryableCodes" class="w-full" placeholder="TIMEOUT,503" />
            </div>
            <div class="gw-field">
              <label>Idempotency</label>
              <p-dropdown formControlName="idempotencyPolicy" [options]="opt(catalog().idempotencyPolicies)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
          </div>
        </fieldset>

        <fieldset class="group full">
          <legend>Compensation &amp; dépendances</legend>
          <div class="gw-form-grid">
            <div class="gw-field">
              <label>Compensation</label>
              <p-dropdown formControlName="compensationPolicy" [options]="opt(catalog().compensationPolicies)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Mode compensation</label>
              <p-dropdown formControlName="compensationMode" [options]="opt(catalog().compensationModes)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Step compensation</label>
              <input pInputText formControlName="compensationStepCode" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Max tentatives</label>
              <input pInputText type="number" formControlName="compensationMaxAttempts" class="w-full" />
            </div>
            <div class="gw-field check-row">
              <p-checkbox formControlName="compensationReverseDesc" [binary]="true" inputId="revDesc" />
              <label for="revDesc">Reverse desc</label>
            </div>
            <div class="gw-field check-row">
              <p-checkbox formControlName="compensationManualReview" [binary]="true" inputId="manRev" />
              <label for="manRev">Revue manuelle</label>
            </div>
            <div class="gw-field">
              <label>Dépend de (step)</label>
              <input pInputText formControlName="dependsOnStepCode" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Condition</label>
              <p-dropdown formControlName="dependencyCondition" [options]="opt(catalog().dependencyConditions)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field check-row">
              <p-checkbox formControlName="dependencyRequired" [binary]="true" inputId="depReq" />
              <label for="depReq">Dépendance requise</label>
            </div>
          </div>
        </fieldset>

        <fieldset class="group full">
          <legend>Erreur</legend>
          <div class="gw-form-grid">
            <div class="gw-field">
              <label>Error policy code</label>
              <input pInputText formControlName="errorPolicyCode" class="w-full" placeholder="DEBIT_SOURCE" />
            </div>
            <div class="gw-field">
              <label>Action par défaut</label>
              <p-dropdown formControlName="defaultErrorAction" [options]="opt(catalog().errorActions)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Check status method</label>
              <input pInputText formControlName="checkStatusMethodCode" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Max retries</label>
              <input pInputText type="number" formControlName="maxRetries" class="w-full" />
            </div>
          </div>
        </fieldset>
      </form>
      <ng-template pTemplate="footer">
        <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelLigneEdit()"></button>
        <button pButton type="button" label="Enregistrer" [loading]="busy()" [disabled]="ligneForm.invalid" (click)="saveLigne()"></button>
      </ng-template>
    </p-dialog>

    <p-dialog
      [header]="editingParametreId() ? 'Modifier le paramètre' : 'Nouveau paramètre'"
      [visible]="paramDialogOpen()"
      (visibleChange)="paramDialogOpen.set($event)"
      [modal]="true"
      [style]="{ width: '520px' }"
      [dismissableMask]="true"
    >
      <form [formGroup]="paramForm" class="gw-form-grid">
        <div class="gw-field">
          <label>Nom</label>
          <input pInputText formControlName="nomParametre" class="w-full" placeholder="Compte débiteur" />
        </div>
        <div class="gw-field">
          <label>Code paramètre</label>
          <input pInputText formControlName="codeParametre" class="w-full" placeholder="compteDebiteur" />
        </div>
        <div class="gw-field">
          <label>Code méthode FSE</label>
          <input pInputText formControlName="codeParametreMethode" class="w-full" placeholder="CPTSOURCE" />
        </div>
        <div class="gw-field">
          <label>Nom externe</label>
          <input pInputText formControlName="nomExterne" class="w-full" />
        </div>
        <div class="gw-field">
          <label>Type</label>
          <p-dropdown formControlName="typeParametre" [options]="opt(catalog().paramTypes)" optionLabel="label" optionValue="value" [showClear]="true" styleClass="w-full" />
        </div>
        <div class="gw-field">
          <label>Catégorie</label>
          <p-dropdown formControlName="categorie" [options]="paramCategorieOptions" optionLabel="label" optionValue="value" styleClass="w-full" />
        </div>
        <div class="gw-field full">
          <label>Valeur par défaut</label>
          <input pInputText formControlName="valeurDefaut" class="w-full" />
        </div>
      </form>
      <ng-template pTemplate="footer">
        <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelParamEdit()"></button>
        <button pButton type="button" label="Enregistrer" [loading]="busy()" [disabled]="paramForm.invalid" (click)="saveParametre()"></button>
      </ng-template>
    </p-dialog>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: 280px 1fr; gap: 1rem; align-items: start; }
    @media (max-width: 900px) { .layout { grid-template-columns: 1fr; } }
    .list-panel ul { list-style: none; margin: 0; padding: 0; }
    .list-panel li { padding: 0.65rem 0.75rem; border-radius: 6px; cursor: pointer; border: 1px solid transparent; }
    .list-panel li:hover { background: #f8fafc; }
    .list-panel li.active { background: #eff6ff; border-color: #bfdbfe; }
    .list-panel li strong { display: block; font-size: 0.95rem; }
    .list-panel li span { color: #64748b; font-size: 0.8rem; }
    .filter-row { margin-bottom: 0.75rem; }
    .detail-head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; }
    .detail-head h2 { margin: 0; }
    .detail-head p { margin: 0.25rem 0 0; color: #64748b; font-size: 0.9rem; }
    .head-actions { display: flex; gap: 0.25rem; }
    .section-head { display: flex; align-items: center; justify-content: space-between; margin: 1rem 0 0.75rem; gap: 0.75rem; }
    .section-head h3, .section-head h4 { margin: 0; font-size: 1rem; }
    .table-wrap { overflow-x: auto; }
    .param-panel {
      padding: 0.75rem 1rem 1rem;
      background: var(--surface-ground, #f8fafc);
      border-radius: 6px;
      margin: 0.25rem 0 0.5rem;
    }
    .gw-row-actions-cell { white-space: nowrap; text-align: right; }
    .ml-tag { margin-left: 0.35rem; }
    .text-xs { font-size: 0.75rem; }
    .hint { color: #64748b; }
    code { font-size: 0.85em; }
    .check-row { display: flex; align-items: center; gap: 0.5rem; }
    fieldset.group { border: 1px solid #e2e8f0; border-radius: 8px; padding: 0.75rem 1rem 1rem; margin: 0 0 0.75rem; }
    fieldset.group legend { padding: 0 0.4rem; font-size: 0.8rem; font-weight: 600; color: #475569; }
  `
})
export class SchemaEcrituresAdminComponent implements OnInit {
  private readonly api = inject(OrchestrationService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);
  readonly schemas = signal<SchemaEcriture[]>([]);
  readonly selected = signal<SchemaEcriture | null>(null);
  readonly lignes = signal<LigneSchemaEcriture[]>([]);
  readonly editingSchema = signal(false);
  readonly editingLigneId = signal<string | null>(null);
  readonly editingParametreId = signal<string | null>(null);
  readonly paramTargetLigne = signal<LigneSchemaEcriture | null>(null);
  readonly expandedRows: Record<string, boolean> = {};
  readonly catalog = signal<OrchestrationCatalog>(EMPTY_CATALOG);
  readonly ligneDialogOpen = signal(false);
  readonly paramDialogOpen = signal(false);

  filterServiceType: ServiceTypeCode | null = null;

  readonly serviceTypeOptions = SERVICE_TYPES.map(v => ({ label: v, value: v }));
  readonly proprieteApiOptions = PROPRIETE_APIS.map(v => ({ label: v, value: v }));
  readonly imputeAgenceOptions = IMPUTE_AGENCES.map(v => ({ label: v, value: v }));
  readonly paramCategorieOptions = PARAM_CATEGORIES.map(v => ({ label: v, value: v }));

  readonly schemaForm = this.fb.nonNullable.group({
    nomSchema: ['', Validators.required],
    serviceType: ['TRANSFERT' as ServiceTypeCode, Validators.required],
    patternCode: ['', Validators.required],
    workflowName: [''],
    workflowVersion: [1],
    engine: ['flowable'],
    errorPolicyCode: [''],
    actif: [true]
  });

  readonly ligneForm = this.fb.group({
    ordre: [1, Validators.required],
    stepId: [''],
    typeMethode: ['DEBIT', Validators.required],
    apiAUtiliser: ['FSESOURCE' as ProprieteApi, Validators.required],
    agenceImputer: [null as ImputeAgence | null],
    executionMode: ['SYNC'],
    timeoutPolicy: [null as string | null],
    timeoutDuration: [null as string | null],
    callbackPolicy: [null as string | null],
    statusMethod: [''],
    retryPolicy: [null as string | null],
    retryMaxAttempts: [null as number | null],
    retryInitialDelayMs: [null as number | null],
    retryMultiplier: [null as number | null],
    retryMaxDelayMs: [null as number | null],
    retryableCodes: [''],
    idempotencyPolicy: [null as string | null],
    compensationPolicy: [null as string | null],
    compensationMode: [null as string | null],
    compensationStepCode: [''],
    compensationReverseDesc: [false],
    compensationMaxAttempts: [null as number | null],
    compensationManualReview: [false],
    dependsOnStepCode: [''],
    dependencyCondition: [null as string | null],
    dependencyRequired: [false],
    errorPolicyCode: [''],
    defaultErrorAction: [null as string | null],
    checkStatusMethodCode: [''],
    maxRetries: [null as number | null],
    blocking: [true],
    criticality: [null as string | null]
  });

  readonly paramForm = this.fb.group({
    nomParametre: [''],
    codeParametre: ['', Validators.required],
    codeParametreMethode: [''],
    nomExterne: [''],
    typeParametre: ['STRING' as string | null],
    categorie: ['USER' as ParametreCategorie],
    valeurDefaut: ['']
  });

  ngOnInit(): void {
    this.api.catalog().subscribe({
      next: c => this.catalog.set(c),
      error: () => this.catalog.set(EMPTY_CATALOG)
    });
    this.reloadList();
    const preselectId = this.route.snapshot.queryParamMap.get('id');
    if (preselectId) {
      this.api.getSchema(preselectId).subscribe({
        next: schema => this.selectSchema(schema),
        error: () => {}
      });
    }
  }

  opt(values: string[]): { label: string; value: string }[] {
    return (values ?? []).map(v => ({ label: v, value: v }));
  }

  reloadList(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.listSchemas(this.filterServiceType ?? undefined).subscribe({
      next: list => {
        this.schemas.set(list);
        this.loading.set(false);
        const current = this.selected();
        if (current) {
          const refreshed = list.find(s => s.id === current.id);
          if (refreshed) {
            this.selectSchema(refreshed);
          } else {
            this.selected.set(null);
            this.lignes.set([]);
          }
        }
      },
      error: err => {
        const status = err?.status;
        const apiMsg = err?.error?.message;
        if (status === 404) {
          this.error.set(apiMsg ?? 'Endpoint orchestration introuvable — redémarrez le backend.');
        } else if (status === 401 || status === 403) {
          this.error.set(apiMsg ?? 'Session expirée — reconnectez-vous.');
        } else {
          this.error.set(apiMsg ?? 'Impossible de charger les schémas');
        }
        this.loading.set(false);
      }
    });
  }

  selectSchema(schema: SchemaEcriture): void {
    this.editingSchema.set(false);
    this.cancelLigneEdit();
    this.cancelParamEdit();
    this.api.getSchema(schema.id).subscribe({
      next: full => {
        this.selected.set(full);
        this.lignes.set(full.lignes ?? []);
      },
      error: err => this.error.set(err?.error?.message ?? 'Erreur chargement schéma')
    });
  }

  startNewSchema(): void {
    this.selected.set(null);
    this.lignes.set([]);
    this.editingSchema.set(true);
    this.schemaForm.reset({
      nomSchema: '',
      serviceType: 'TRANSFERT',
      patternCode: '',
      workflowName: '',
      workflowVersion: 1,
      engine: 'flowable',
      errorPolicyCode: '',
      actif: true
    });
  }

  editSchema(): void {
    const s = this.selected();
    if (!s) return;
    this.editingSchema.set(true);
    this.schemaForm.patchValue({
      nomSchema: s.nomSchema,
      serviceType: s.serviceType,
      patternCode: s.patternCode,
      workflowName: s.workflowName ?? '',
      workflowVersion: s.workflowVersion,
      engine: s.engine,
      errorPolicyCode: s.errorPolicyCode ?? '',
      actif: s.actif
    });
  }

  cancelSchemaEdit(): void {
    this.editingSchema.set(false);
  }

  saveSchema(): void {
    if (this.schemaForm.invalid) return;
    this.busy.set(true);
    this.msg.set(null);
    const body = this.schemaForm.getRawValue();
    // Activation uniquement via « Publier » (évite d'activer un schéma incomplet).
    if (!this.selected()) {
      body.actif = false;
    }
    const current = this.selected();
    const req$ = current ? this.api.updateSchema(current.id, body) : this.api.createSchema(body);
    req$.subscribe({
      next: saved => {
        this.busy.set(false);
        this.editingSchema.set(false);
        this.msg.set(current ? 'Schéma mis à jour' : 'Schéma créé');
        this.reloadList();
        this.selectSchema(saved);
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? 'Erreur enregistrement schéma');
      }
    });
  }

  /** Miroir des règles serveur SCHEMA_NOT_PUBLISHABLE. */
  canPublishSchema(): boolean {
    return !this.publishBlockedReason();
  }

  publishBlockedReason(): string | null {
    const s = this.selected();
    if (!s) return 'Aucun schéma sélectionné';
    if (!s.workflowName?.trim()) return 'workflowName manquant';
    const lines = this.lignes();
    if (!lines.length) return 'au moins une ligne est requise';
    for (const l of lines) {
      if (!l.typeMethode?.trim()) return `ligne #${l.ordre} : typeMethode manquant`;
      if (l.apiAUtiliser == null) return `ligne #${l.ordre} : apiAUtiliser manquant`;
    }
    return null;
  }

  publishSchema(): void {
    const s = this.selected();
    if (!s || !this.canPublishSchema()) return;
    this.busy.set(true);
    this.error.set(null);
    this.api.publishSchema(s.id).subscribe({
      next: saved => {
        this.busy.set(false);
        this.msg.set('Schéma publié');
        this.reloadList();
        this.selectSchema(saved);
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? 'Publication refusée');
      }
    });
  }

  unpublishSchema(): void {
    const s = this.selected();
    if (!s) return;
    this.busy.set(true);
    this.api.unpublishSchema(s.id).subscribe({
      next: saved => {
        this.busy.set(false);
        this.msg.set('Schéma dépublié');
        this.reloadList();
        this.selectSchema(saved);
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? 'Erreur dépublication');
      }
    });
  }

  deleteSchema(): void {
    const s = this.selected();
    if (!s || !confirm(`Supprimer le schéma « ${s.nomSchema} » ?`)) return;
    this.busy.set(true);
    this.api.deleteSchema(s.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.selected.set(null);
        this.lignes.set([]);
        this.msg.set('Schéma supprimé');
        this.reloadList();
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? 'Erreur suppression');
      }
    });
  }

  startNewLigne(): void {
    const s = this.selected();
    if (!s) return;
    this.editingLigneId.set(null);
    const nextOrdre = (this.lignes().reduce((m, l) => Math.max(m, l.ordre), 0) || 0) + 1;
    this.ligneForm.reset({
      ordre: nextOrdre,
      stepId: '',
      typeMethode: 'DEBIT',
      apiAUtiliser: 'FSESOURCE',
      agenceImputer: null,
      executionMode: 'SYNC',
      timeoutPolicy: null,
      timeoutDuration: null,
      callbackPolicy: null,
      statusMethod: '',
      retryPolicy: null,
      retryMaxAttempts: null,
      retryInitialDelayMs: null,
      retryMultiplier: null,
      retryMaxDelayMs: null,
      retryableCodes: '',
      idempotencyPolicy: null,
      compensationPolicy: null,
      compensationMode: null,
      compensationStepCode: '',
      compensationReverseDesc: false,
      compensationMaxAttempts: null,
      compensationManualReview: false,
      dependsOnStepCode: '',
      dependencyCondition: null,
      dependencyRequired: false,
      errorPolicyCode: '',
      defaultErrorAction: null,
      checkStatusMethodCode: '',
      maxRetries: null,
      blocking: true,
      criticality: 'CRITICAL'
    });
    this.ligneDialogOpen.set(true);
  }

  editLigne(l: LigneSchemaEcriture): void {
    this.editingLigneId.set(l.id);
    this.ligneForm.patchValue({
      ordre: l.ordre,
      stepId: l.stepId ?? '',
      typeMethode: l.typeMethode,
      apiAUtiliser: l.apiAUtiliser,
      agenceImputer: l.agenceImputer,
      executionMode: l.executionMode ?? 'SYNC',
      timeoutPolicy: l.timeoutPolicy,
      timeoutDuration: l.timeoutDuration,
      callbackPolicy: l.callbackPolicy,
      statusMethod: l.statusMethod ?? '',
      retryPolicy: l.retryPolicy,
      retryMaxAttempts: l.retryMaxAttempts,
      retryInitialDelayMs: l.retryInitialDelayMs,
      retryMultiplier: l.retryMultiplier,
      retryMaxDelayMs: l.retryMaxDelayMs,
      retryableCodes: l.retryableCodes ?? '',
      idempotencyPolicy: l.idempotencyPolicy,
      compensationPolicy: l.compensationPolicy,
      compensationMode: l.compensationMode,
      compensationStepCode: l.compensationStepCode ?? '',
      compensationReverseDesc: l.compensationReverseDesc ?? false,
      compensationMaxAttempts: l.compensationMaxAttempts,
      compensationManualReview: l.compensationManualReview ?? false,
      dependsOnStepCode: l.dependsOnStepCode ?? '',
      dependencyCondition: l.dependencyCondition,
      dependencyRequired: l.dependencyRequired ?? false,
      errorPolicyCode: l.errorPolicyCode ?? '',
      defaultErrorAction: l.defaultErrorAction,
      checkStatusMethodCode: l.checkStatusMethodCode ?? '',
      maxRetries: l.maxRetries,
      blocking: l.blocking ?? true,
      criticality: l.criticality
    });
    this.ligneDialogOpen.set(true);
  }

  cancelLigneEdit(): void {
    this.ligneDialogOpen.set(false);
    this.editingLigneId.set(null);
  }

  saveLigne(): void {
    const s = this.selected();
    if (!s || this.ligneForm.invalid) return;
    this.busy.set(true);
    const raw = this.ligneForm.getRawValue();
    const body: LigneSchemaEcritureRequest = {
      schemaEcritureId: s.id,
      ordre: raw.ordre!,
      stepId: raw.stepId || null,
      typeMethode: raw.typeMethode!,
      apiAUtiliser: raw.apiAUtiliser!,
      agenceImputer: raw.agenceImputer,
      executionMode: raw.executionMode || null,
      timeoutPolicy: raw.timeoutPolicy,
      timeoutDuration: raw.timeoutDuration,
      callbackPolicy: raw.callbackPolicy,
      statusMethod: raw.statusMethod || null,
      retryPolicy: raw.retryPolicy,
      retryMaxAttempts: raw.retryMaxAttempts,
      retryInitialDelayMs: raw.retryInitialDelayMs,
      retryMultiplier: raw.retryMultiplier,
      retryMaxDelayMs: raw.retryMaxDelayMs,
      retryableCodes: raw.retryableCodes || null,
      idempotencyPolicy: raw.idempotencyPolicy,
      compensationPolicy: raw.compensationPolicy,
      compensationMode: raw.compensationMode,
      compensationStepCode: raw.compensationStepCode || null,
      compensationReverseDesc: raw.compensationReverseDesc,
      compensationMaxAttempts: raw.compensationMaxAttempts,
      compensationManualReview: raw.compensationManualReview,
      dependsOnStepCode: raw.dependsOnStepCode || null,
      dependencyCondition: raw.dependencyCondition,
      dependencyRequired: raw.dependencyRequired,
      errorPolicyCode: raw.errorPolicyCode || null,
      defaultErrorAction: raw.defaultErrorAction,
      checkStatusMethodCode: raw.checkStatusMethodCode || null,
      maxRetries: raw.maxRetries,
      blocking: raw.blocking,
      criticality: raw.criticality
    };
    const editId = this.editingLigneId();
    const req$ = editId ? this.api.updateLigne(editId, body) : this.api.addLigne(body);
    req$.subscribe({
      next: () => {
        this.busy.set(false);
        this.cancelLigneEdit();
        this.msg.set('Ligne enregistrée');
        this.selectSchema(s);
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? 'Erreur enregistrement ligne');
      }
    });
  }

  deleteLigne(l: LigneSchemaEcriture): void {
    if (!confirm(`Supprimer la ligne ${l.ordre} (${l.typeMethode}) ?`)) return;
    this.busy.set(true);
    this.api.deleteLigne(l.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.msg.set('Ligne supprimée');
        const s = this.selected();
        if (s) this.selectSchema(s);
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? 'Erreur suppression ligne');
      }
    });
  }

  toggleParametres(l: LigneSchemaEcriture): void {
    this.expandedRows[l.id] = !this.expandedRows[l.id];
  }

  criticalitySeverity(criticality: string): 'danger' | 'warning' | 'info' | 'success' | 'secondary' {
    switch (criticality) {
      case 'CRITICAL':
        return 'danger';
      case 'REQUIRED':
        return 'warning';
      case 'OPTIONAL':
        return 'info';
      case 'INFORMATIONAL':
        return 'secondary';
      default:
        return 'secondary';
    }
  }

  startNewParametre(l: LigneSchemaEcriture): void {
    this.editingParametreId.set(null);
    this.paramTargetLigne.set(l);
    this.paramForm.reset({
      nomParametre: '',
      codeParametre: '',
      codeParametreMethode: '',
      nomExterne: '',
      typeParametre: 'STRING',
      categorie: 'USER',
      valeurDefaut: ''
    });
    this.paramDialogOpen.set(true);
  }

  editParametre(l: LigneSchemaEcriture, p: ParametreSchema): void {
    this.editingParametreId.set(p.id);
    this.paramTargetLigne.set(l);
    this.paramForm.patchValue({
      nomParametre: p.nomParametre ?? '',
      codeParametre: p.codeParametre,
      codeParametreMethode: p.codeParametreMethode ?? '',
      nomExterne: p.nomExterne ?? '',
      typeParametre: p.typeParametre,
      categorie: p.categorie,
      valeurDefaut: p.valeurDefaut ?? ''
    });
    this.paramDialogOpen.set(true);
  }

  cancelParamEdit(): void {
    this.paramDialogOpen.set(false);
    this.editingParametreId.set(null);
    this.paramTargetLigne.set(null);
  }

  saveParametre(): void {
    const l = this.paramTargetLigne();
    if (!l || this.paramForm.invalid) return;
    this.busy.set(true);
    const raw = this.paramForm.getRawValue();
    const body = {
      ligneSchemaEcritureId: l.id,
      nomParametre: raw.nomParametre || null,
      codeParametre: raw.codeParametre!,
      codeParametreMethode: raw.codeParametreMethode || null,
      nomExterne: raw.nomExterne || null,
      typeParametre: raw.typeParametre || null,
      categorie: raw.categorie ?? 'USER',
      valeurDefaut: raw.valeurDefaut || null
    };
    const editId = this.editingParametreId();
    const req$ = editId ? this.api.updateParametre(editId, body) : this.api.addParametre(body);
    req$.subscribe({
      next: () => {
        this.busy.set(false);
        this.cancelParamEdit();
        this.msg.set('Paramètre enregistré');
        const s = this.selected();
        if (s) this.selectSchema(s);
        this.expandedRows[l.id] = true;
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? 'Erreur enregistrement paramètre');
      }
    });
  }

  deleteParametre(p: ParametreSchema): void {
    if (!confirm(`Supprimer le paramètre ${p.codeParametre} ?`)) return;
    this.busy.set(true);
    this.api.deleteParametre(p.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.msg.set('Paramètre supprimé');
        const s = this.selected();
        if (s) this.selectSchema(s);
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? 'Erreur suppression paramètre');
      }
    });
  }
}
