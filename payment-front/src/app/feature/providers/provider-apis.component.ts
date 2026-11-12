
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { OrchestrationService } from '../../core/service/orchestration.service';
import { ProviderService } from '../../core/service/provider.service';
import { AuthType, FseApi, FseApiRequest, Provider, ProviderApi } from '../../core/model/api.models';

interface MethodParam {
  codeParametre: string;
  nomExterne: string;
}

interface DraftMethodParam extends MethodParam {
  localId: string;
}

let draftParamSeq = 0;
function newDraftParamId(): string {
  draftParamSeq += 1;
  return `mp-${draftParamSeq}`;
}

@Component({
  selector: 'app-provider-apis',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    DropdownModule,
    InputSwitchModule,
    ProgressSpinnerModule
  ],
  template: `
    <div class="gw-page-header">
      <div>
        <h2>APIs & Méthodes</h2>
        <p class="subtitle">Modèle Omnicanal : chaque <strong>Api</strong> possède des <strong>Méthodes</strong> (tableau paginé) et des <strong>paramètres</strong> éditables</p>
      </div>
      <div class="header-actions">
        @if (canSeedBundle()) {
          <button
            pButton
            type="button"
            class="p-button-outlined"
            label="Charger le catalogue Coris"
            icon="pi pi-download"
            [loading]="seeding()"
            (click)="seedBundle()"
          ></button>
        }
        <button
          pButton
          type="button"
          class="p-button-outlined"
          label="Nouvelle Api"
          icon="pi pi-plus"
          (click)="toggleApiForm()"
        ></button>
      </div>
    </div>

    @if (showApiForm()) {
      <div class="gw-panel">
        <h3>{{ editingApiId() ? 'Modifier Api' : 'Nouvelle Api' }} (connectivité : host, URL, auth)</h3>
        <form [formGroup]="apiForm" (ngSubmit)="saveFseApi()" class="gw-form-grid">
          <div class="gw-field">
            <label>Code Api *</label>
            <input pInputText formControlName="code" class="w-full" placeholder="ex. 0007" />
          </div>
          <div class="gw-field">
            <label>Nom Api *</label>
            <input pInputText formControlName="nom" class="w-full" placeholder="ex. API CORIS Money" />
          </div>
          <div class="gw-field full">
            <label>Base URL *</label>
            <input pInputText formControlName="baseUrl" class="w-full" placeholder="http://localhost:9180" />
          </div>
          <div class="gw-field">
            <label>Host</label>
            <input pInputText formControlName="host" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Port</label>
            <input pInputText formControlName="port" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Actif</label>
            <p-inputSwitch formControlName="actif" />
          </div>

          <div class="gw-field full auth-section">
            <h4>Authentification (inline sur l'Api — modèle Omnicanal)</h4>
          </div>
          <div class="gw-field">
            <label>Type auth</label>
            <p-dropdown formControlName="authType" [options]="authTypeOptions" styleClass="w-full" />
          </div>
          @if (apiForm.value.authType === 'OAUTH2_CLIENT_CREDENTIALS') {
            <div class="gw-field">
              <label>Client ID</label>
              <input pInputText formControlName="clientId" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Client secret</label>
              <input pInputText formControlName="clientSecret" type="password" class="w-full"
                [placeholder]="editingApiId() && editingHasClientSecret() ? '(inchangé si vide)' : ''" />
            </div>
            <div class="gw-field full">
              <label>Token URL</label>
              <input pInputText formControlName="tokenUrl" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Grant type</label>
              <input pInputText formControlName="grantType" class="w-full" placeholder="client_credentials" />
            </div>
          }
          @if (apiForm.value.authType === 'BASIC') {
            <div class="gw-field">
              <label>Username</label>
              <input pInputText formControlName="username" class="w-full"
                [placeholder]="editingApiId() && editingHasUsername() ? '(inchangé si vide)' : ''" />
            </div>
            <div class="gw-field">
              <label>Password</label>
              <input pInputText formControlName="password" type="password" class="w-full"
                [placeholder]="editingApiId() && editingHasPassword() ? '(inchangé si vide)' : ''" />
            </div>
          }
          @if (apiForm.value.authType === 'API_KEY' || apiForm.value.authType === 'BEARER_TOKEN') {
            <div class="gw-field full">
              <label>{{ apiForm.value.authType === 'BEARER_TOKEN' ? 'Bearer token' : 'API key' }}</label>
              <input pInputText formControlName="apiKey" type="password" class="w-full"
                [placeholder]="editingApiId() && editingHasApiKey() ? '(inchangé si vide)' : ''" />
            </div>
          }
          @if (apiForm.value.authType === 'CUSTOM_HEADER') {
            <div class="gw-field">
              <label>Header name</label>
              <input pInputText formControlName="customHeaderName" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Header value</label>
              <input pInputText formControlName="customHeaderValue" type="password" class="w-full"
                [placeholder]="editingApiId() && editingHasCustomHeaderValue() ? '(inchangé si vide)' : ''" />
            </div>
          }

          <div class="gw-actions full">
            <button pButton type="submit" [label]="editingApiId() ? 'Enregistrer' : 'Creer Api'" [disabled]="apiForm.invalid || savingApi()" [loading]="savingApi()"></button>
            <button pButton type="button" class="p-button-text" label="Annuler" (click)="closeApiForm()"></button>
          </div>
        </form>
      </div>
    }

    <p-dialog
      [header]="methodDialogTitle()"
      [visible]="showMethodForm()"
      (visibleChange)="onMethodDialogVisible($event)"
      [modal]="true"
      appendTo="body"
      [style]="{ width: 'min(48rem, 96vw)' }"
      [draggable]="false"
      [blockScroll]="true"
    >
      <p class="method-dialog-subtitle">Api {{ methodFormApiLabel() }}</p>
      <form [formGroup]="methodForm" (ngSubmit)="saveMethod()" class="gw-form-grid method-dialog-form">
        <div class="gw-field">
          <label>Type méthode *</label>
          <p-dropdown
            formControlName="typeMethode"
            [options]="typeMethodeSelectOptions()"
            optionLabel="label"
            optionValue="value"
            [filter]="true"
            [editable]="true"
            appendTo="body"
            placeholder="Sélectionner ou saisir…"
            styleClass="w-full"
          />
        </div>
        <div class="gw-field">
          <label>Code méthode</label>
          <input pInputText formControlName="codeMethode" class="w-full" placeholder="ex. 000703" />
        </div>
        <div class="gw-field full">
          <label>Nom méthode (path URL) *</label>
          <input pInputText formControlName="nomMethode" class="w-full" placeholder="ex. api/gateway/solde" />
        </div>
        <div class="gw-field">
          <label>Type appel *</label>
          <p-dropdown formControlName="typeAppel" [options]="httpMethods" appendTo="body" styleClass="w-full" />
        </div>
        <div class="gw-field">
          <label>Type réponse</label>
          <p-dropdown formControlName="typeReponse" [options]="responseTypes" appendTo="body" styleClass="w-full" />
        </div>
        <div class="gw-field">
          <label>Actif</label>
          <p-inputSwitch formControlName="actif" />
        </div>
        <div class="gw-field full">
          <div class="params-header">
            <label>Paramètres (ParametreMethode)</label>
            <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-plus" label="Paramètre" (click)="addParam()"></button>
          </div>
          <div formArrayName="parametres" class="params-list">
            @for (ctrl of parametres.controls; track ctrl; let i = $index) {
              <div [formGroupName]="i" class="param-row">
                <input pInputText formControlName="codeParametre" placeholder="codeParametre (interne)" />
                <input pInputText formControlName="nomExterne" placeholder="nomExterne (clé JSON)" />
                <input pInputText formControlName="typeParametre" placeholder="STRING" />
                <button pButton type="button" class="p-button-rounded p-button-text p-button-danger" icon="pi pi-trash" (click)="removeParam(i)"></button>
              </div>
            }
          </div>
        </div>
        <div class="dialog-actions full">
          <button pButton type="button" class="p-button-text" label="Annuler" (click)="closeMethodForm()"></button>
          <button
            pButton
            type="submit"
            [label]="editingMethodId() ? 'Enregistrer' : 'Créer la méthode'"
            icon="pi pi-save"
            [disabled]="methodForm.invalid || savingMethod()"
            [loading]="savingMethod()"
          ></button>
        </div>
      </form>
    </p-dialog>

    @if (loading()) {
      <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
    } @else if (fseApis().length === 0 && methods().length === 0 && orphanMethods().length === 0) {
      <div class="gw-panel">
        <p>Aucune Api — créez-en une ou chargez le catalogue Coris.</p>
        <p class="page-hint">Onglet actif : <strong>APIs &amp; Méthodes</strong> — si vous ne voyez pas cet écran, cliquez l'onglet du même nom ci-dessus.</p>
      </div>
    } @else if (fseApis().length === 0 && methods().length === 0) {
      <div class="gw-panel">
        <p>Aucune Api FSE — certaines méthodes existent sans Api parente Omnicanal.</p>
        <p class="page-hint">Cliquez « Charger le catalogue Coris » ou « Nouvelle Api ».</p>
      </div>
      <div class="gw-panel">
        <h3>Méthodes (sans Api parente)</h3>
        <p-table
          [value]="orphanMethods()"
          styleClass="p-datatable-sm p-datatable-striped"
          [paginator]="true"
          [rows]="10"
          [rowsPerPageOptions]="[10, 20, 50]"
          [rowHover]="true"
          dataKey="id"
          [showCurrentPageReport]="true"
          currentPageReportTemplate="{first}-{last} sur {totalRecords}"
        >
          <ng-template pTemplate="header">
            <tr>
              <th>Code</th>
              <th>HTTP</th>
              <th>Path</th>
              <th>Actif</th>
                <th style="width: 14rem"></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-m>
            <tr
              class="method-row"
              [class.method-row-selected]="selectedMethod()?.id === m.id"
              (click)="onMethodRowClick(m)"
            >
              <td><code>{{ m.code }}</code></td>
              <td>{{ m.httpMethod }}</td>
              <td>{{ m.path }}</td>
              <td>{{ m.actif ? 'Oui' : 'Non' }}</td>
              <td class="col-actions" (click)="$event.stopPropagation()">
                <button
                  pButton
                  type="button"
                  class="p-button-text p-button-sm"
                  label="Params"
                  (click)="onMethodRowClick(m)"
                ></button>
                <button
                  pButton
                  type="button"
                  class="p-button-text p-button-sm"
                  icon="pi pi-share-alt"
                  title="Générer interface REQUEST"
                  [loading]="generatingInterfaceId() === m.id"
                  [disabled]="!!generatingInterfaceId()"
                  (click)="generateInterfaceFromMethod(m)"
                ></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td colspan="5">Aucune méthode orpheline</td></tr>
          </ng-template>
        </p-table>

        @if (selectedMethod(); as m) {
          @if (!m.fseApiId) {
            <div class="method-detail-panel">
              <div class="method-detail-header">
                <h4>Paramètres — <code>{{ m.code }}</code></h4>
                <div class="panel-actions">
                  <button
                    pButton
                    type="button"
                    class="p-button-outlined p-button-sm"
                    label="Ajouter"
                    icon="pi pi-plus"
                    (click)="openAddMethodParam()"
                  ></button>
                  <button
                    pButton
                    type="button"
                    class="p-button-sm"
                    label="Enregistrer"
                    icon="pi pi-save"
                    [loading]="savingParams()"
                    [disabled]="!paramsDirty()"
                    (click)="saveSelectedMethodParams()"
                  ></button>
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm"
                    icon="pi pi-times"
                    (click)="clearSelectedMethod()"
                  ></button>
                </div>
              </div>
              @if (!draftMethodParams().length) {
                <p class="no-params">Aucun paramètre — cliquez « Ajouter ».</p>
              } @else {
                <p-table [value]="draftMethodParams()" styleClass="p-datatable-sm" dataKey="localId">
                  <ng-template pTemplate="header">
                    <tr>
                      <th>codeParametre (interne)</th>
                      <th>nomExterne (clé JSON)</th>
                      <th style="width: 6rem"></th>
                    </tr>
                  </ng-template>
                  <ng-template pTemplate="body" let-p let-i="rowIndex">
                    <tr>
                      <td><code>{{ p.codeParametre }}</code></td>
                      <td><code>{{ p.nomExterne }}</code></td>
                      <td class="col-actions">
                        <button
                          pButton
                          type="button"
                          class="p-button-text p-button-sm"
                          icon="pi pi-pencil"
                          (click)="openEditMethodParam(i)"
                        ></button>
                        <button
                          pButton
                          type="button"
                          class="p-button-text p-button-danger p-button-sm"
                          icon="pi pi-trash"
                          (click)="removeDraftMethodParam(i)"
                        ></button>
                      </td>
                    </tr>
                  </ng-template>
                </p-table>
              }
            </div>
          }
        }
      </div>
    } @else {
      <div class="gw-panel">
        <h3>Apis (connectivité Omnicanal)</h3>
        <p class="table-hint">Cliquez sur une Api pour afficher ses méthodes</p>
        <div class="table-scroll-wrap">
          <table class="gw-data-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Nom</th>
                <th>Base URL</th>
                <th>Auth</th>
                <th>Méthodes</th>
                <th>Actif</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (api of fseApis(); track api.id) {
                <tr
                  class="api-row"
                  [class.api-row-selected]="selectedApiId() === api.id"
                  (click)="onApiRowClick(api)"
                >
                  <td><code>{{ api.code }}</code></td>
                  <td>{{ api.nom }}</td>
                  <td>{{ api.baseUrl }}</td>
                  <td>{{ api.authType && api.authType !== 'NONE' ? api.authType : '—' }}</td>
                  <td>{{ methodsForApi(api.id).length || api.methodCount }}</td>
                  <td>{{ api.actif ? 'Oui' : 'Non' }}</td>
                  <td (click)="$event.stopPropagation()">
                    <button type="button" class="gw-btn-modify" (click)="openEditApi(api)">Modifier</button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>

      @if (selectedApi(); as api) {
        <div class="gw-panel api-block">
          <div class="api-block-header">
            <div>
              <h3><code>{{ api.code }}</code> — {{ api.nom }}</h3>
              <p class="api-meta">{{ api.baseUrl }} · {{ methodsForApi(api.id).length }} méthode(s)</p>
            </div>
            <button
              pButton
              type="button"
              class="p-button-sm"
              label="Ajouter une méthode"
              icon="pi pi-plus"
              (click)="openMethodForm(api)"
            ></button>
          </div>

          <p-table
            [value]="methodsForApi(api.id)"
            styleClass="p-datatable-sm p-datatable-striped"
            [paginator]="true"
            [rows]="10"
            [rowsPerPageOptions]="[10, 20, 50]"
            [rowHover]="true"
            dataKey="id"
            [showCurrentPageReport]="true"
            currentPageReportTemplate="{first}-{last} sur {totalRecords}"
          >
            <ng-template pTemplate="header">
              <tr>
                <th>Type / code</th>
                <th>HTTP</th>
                <th>Path (nomMethode)</th>
                <th>Actif</th>
                <th style="width: 16rem"></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-m>
              <tr
                class="method-row"
                [class.method-row-selected]="selectedMethod()?.id === m.id"
                (click)="onMethodRowClick(m)"
              >
                <td><code>{{ m.code }}</code></td>
                <td>{{ m.httpMethod }}</td>
                <td>{{ m.path }}</td>
                <td>{{ m.actif ? 'Oui' : 'Non' }}</td>
                <td class="col-actions" (click)="$event.stopPropagation()">
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm"
                    icon="pi pi-pencil"
                    (click)="openEditMethod(m, api)"
                  ></button>
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm"
                    label="Params"
                    (click)="onMethodRowClick(m)"
                  ></button>
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm"
                    icon="pi pi-share-alt"
                    title="Générer interface REQUEST"
                    [loading]="generatingInterfaceId() === m.id"
                    [disabled]="!!generatingInterfaceId()"
                    (click)="generateInterfaceFromMethod(m)"
                  ></button>
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr>
                <td colspan="5">Aucune méthode sur cette Api — cliquez « Ajouter une méthode »</td>
              </tr>
            </ng-template>
          </p-table>

          @if (selectedMethod()?.fseApiId === api.id) {
            <div class="method-detail-panel">
              <div class="method-detail-header">
                <h4>Paramètres — <code>{{ selectedMethod()!.code }}</code></h4>
                <div class="panel-actions">
                  <button
                    pButton
                    type="button"
                    class="p-button-outlined p-button-sm"
                    label="Ajouter"
                    icon="pi pi-plus"
                    (click)="openAddMethodParam()"
                  ></button>
                  <button
                    pButton
                    type="button"
                    class="p-button-sm"
                    label="Enregistrer"
                    icon="pi pi-save"
                    [loading]="savingParams()"
                    [disabled]="!paramsDirty()"
                    (click)="saveSelectedMethodParams()"
                  ></button>
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm"
                    icon="pi pi-times"
                    (click)="clearSelectedMethod()"
                  ></button>
                </div>
              </div>

              @if (!draftMethodParams().length) {
                <p class="no-params">Aucun paramètre — cliquez « Ajouter ».</p>
              } @else {
                <p-table [value]="draftMethodParams()" styleClass="p-datatable-sm" dataKey="localId">
                  <ng-template pTemplate="header">
                    <tr>
                      <th>codeParametre (interne)</th>
                      <th>nomExterne (clé JSON)</th>
                      <th style="width: 6rem"></th>
                    </tr>
                  </ng-template>
                  <ng-template pTemplate="body" let-p let-i="rowIndex">
                    <tr>
                      <td><code>{{ p.codeParametre }}</code></td>
                      <td><code>{{ p.nomExterne }}</code></td>
                      <td class="col-actions">
                        <button
                          pButton
                          type="button"
                          class="p-button-text p-button-sm"
                          icon="pi pi-pencil"
                          (click)="openEditMethodParam(i)"
                        ></button>
                        <button
                          pButton
                          type="button"
                          class="p-button-text p-button-danger p-button-sm"
                          icon="pi pi-trash"
                          (click)="removeDraftMethodParam(i)"
                        ></button>
                      </td>
                    </tr>
                  </ng-template>
                </p-table>
              }

              @if (selectedMethod()!.requestMappingJson) {
                <details class="mapping-raw">
                  <summary>requestMappingJson</summary>
                  <pre>{{ formatMappingJson(selectedMethod()!.requestMappingJson) }}</pre>
                </details>
              }
            </div>
          }

          <p class="table-hint">Cliquez sur une ligne pour afficher / éditer les paramètres</p>
        </div>
      }
    }

    @if (msg()) { <p class="gw-msg" [class.error]="isError()">{{ msg() }}</p> }

    <p-dialog
      [header]="methodParamDialogTitle()"
      [visible]="methodParamDialogVisible()"
      (visibleChange)="methodParamDialogVisible.set($event)"
      [modal]="true"
      appendTo="body"
      [style]="{ width: 'min(28rem, 96vw)' }"
      [draggable]="false"
    >
      <form [formGroup]="methodParamForm" (ngSubmit)="saveMethodParamDialog()" class="param-dialog-form">
        <div class="gw-field">
          <label>codeParametre *</label>
          <input pInputText formControlName="codeParametre" class="w-full" placeholder="CPTSOURCE" />
        </div>
        <div class="gw-field">
          <label>nomExterne *</label>
          <input pInputText formControlName="nomExterne" class="w-full" placeholder="numeroCompte" />
        </div>
        <div class="dialog-actions">
          <button pButton type="button" class="p-button-text" label="Annuler" (click)="methodParamDialogVisible.set(false)"></button>
          <button
            pButton
            type="submit"
            [label]="editingMethodParamIndex() === null ? 'Ajouter' : 'Enregistrer'"
            [disabled]="methodParamForm.invalid"
          ></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .subtitle { margin: 0.15rem 0 0; color: var(--text-color-secondary, #64748b); font-size: 0.85rem; }
    .header-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; }
    h3 { margin: 0; font-size: 1rem; }
    code { font-size: 0.85rem; }
    .api-block { margin-bottom: 1rem; }
    .api-block-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; margin-bottom: 0.75rem; flex-wrap: wrap; }
    .api-row { cursor: pointer; }
    .api-row:hover { background: var(--surface-hover, #f1f5f9); }
    .api-row-selected { background: var(--highlight-bg, #eff6ff) !important; }
    .table-scroll-wrap {
      width: 100%;
      max-width: 100%;
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }
    .gw-data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.875rem;
    }
    .gw-data-table th,
    .gw-data-table td {
      padding: 0.55rem 0.65rem;
      text-align: left;
      border-bottom: 1px solid var(--surface-border, #e2e8f0);
      vertical-align: middle;
    }
    .gw-data-table th {
      font-weight: 600;
      color: var(--text-color-secondary, #64748b);
      background: var(--surface-50, #f8fafc);
    }
    .col-actions {
      white-space: nowrap;
      min-width: 11rem;
    }
    .gw-btn-modify,
    .gw-btn-secondary {
      display: inline-block;
      margin-right: 0.35rem;
      padding: 0.4rem 0.75rem;
      border-radius: 6px;
      font-size: 0.8rem;
      font-weight: 600;
      line-height: 1.2;
      cursor: pointer;
      font-family: inherit;
    }
    .gw-btn-modify {
      border: 1px solid #2563eb;
      background: #2563eb;
      color: #fff !important;
    }
    .gw-btn-modify:hover {
      background: #1d4ed8;
      border-color: #1d4ed8;
    }
    .gw-btn-secondary {
      border: 1px solid #cbd5e1;
      background: #fff;
      color: #334155;
    }
    .gw-btn-secondary:hover {
      background: #f1f5f9;
    }
    .auth-section h4 { margin: 0.5rem 0 0; font-size: 0.9rem; color: var(--text-color-secondary, #64748b); }
    .api-meta { margin: 0.25rem 0 0; color: var(--text-color-secondary, #64748b); font-size: 0.85rem; }
    .method-dialog-subtitle {
      margin: 0 0 1rem;
      color: var(--text-color-secondary, #64748b);
      font-size: 0.9rem;
    }
    .method-dialog-form .full,
    .method-dialog-form .dialog-actions.full {
      grid-column: 1 / -1;
    }
    .params-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .params-list { display: flex; flex-direction: column; gap: 0.5rem; }
    .param-row { display: grid; grid-template-columns: 1fr 1fr 7rem auto; gap: 0.5rem; align-items: center; }
    .method-row { cursor: pointer; }
    .method-row:hover { background: var(--surface-hover, #f1f5f9); }
    .method-row-selected { background: var(--highlight-bg, #eff6ff) !important; }
    .method-detail-panel {
      margin-top: 0.75rem;
      padding: 0.75rem 1rem;
      border: 1px solid var(--surface-border, #e2e8f0);
      border-radius: 6px;
      background: var(--surface-ground, #f8fafc);
    }
    .method-detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; gap: 0.75rem; flex-wrap: wrap; }
    .method-detail-header h4 { margin: 0; font-size: 0.9rem; }
    .panel-actions { display: flex; align-items: center; gap: 0.35rem; flex-wrap: wrap; }
    .param-dialog-form { display: flex; flex-direction: column; gap: 0.85rem; }
    .param-dialog-form .gw-field { display: flex; flex-direction: column; gap: 0.35rem; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem; }
    .no-params { margin: 0; color: var(--text-color-secondary, #64748b); font-size: 0.85rem; }
    .mapping-raw { margin-top: 0.75rem; font-size: 0.8rem; }
    .mapping-raw pre {
      margin: 0.35rem 0 0;
      padding: 0.5rem;
      background: var(--surface-card, #fff);
      border-radius: 4px;
      overflow-x: auto;
      font-size: 0.75rem;
    }
    .table-hint { margin: 0.35rem 0 0; font-size: 0.75rem; color: var(--text-color-secondary, #94a3b8); }
    .page-hint { margin: 0.5rem 0 0; font-size: 0.85rem; color: var(--text-color-secondary, #64748b); }
    @media (max-width: 720px) {
      .param-row { grid-template-columns: 1fr; }
    }
  `
})

export class ProviderApisComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ProviderService);
  private readonly orchestrationApi = inject(OrchestrationService);
  private readonly fb = inject(FormBuilder);
  private providerId = '';

  readonly httpMethods = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'];
  readonly responseTypes = ['JSON', 'XML'];
  readonly authTypeOptions: AuthType[] = [
    'NONE',
    'BASIC',
    'API_KEY',
    'BEARER_TOKEN',
    'OAUTH2_CLIENT_CREDENTIALS',
    'CUSTOM_HEADER'
  ];

  readonly loading = signal(true);
  readonly savingApi = signal(false);
  readonly savingMethod = signal(false);
  readonly seeding = signal(false);
  readonly showApiForm = signal(false);
  readonly editingApiId = signal<string | null>(null);
  readonly editingHasClientSecret = signal(false);
  readonly editingHasApiKey = signal(false);
  readonly editingHasUsername = signal(false);
  readonly editingHasPassword = signal(false);
  readonly editingHasCustomHeaderValue = signal(false);
  readonly showMethodForm = signal(false);
  readonly editingMethodId = signal<string | null>(null);
  readonly methodFormApiId = signal<string | null>(null);
  readonly methodFormApiLabel = signal('');
  readonly fseApis = signal<FseApi[]>([]);
  readonly allMethods = signal<ProviderApi[]>([]);
  readonly methods = signal<ProviderApi[]>([]);
  readonly provider = signal<Provider | null>(null);
  readonly typeMethodeOptions = signal<string[]>([]);
  readonly typeMethodeSelectOptions = computed(() => {
    const catalog = this.typeMethodeOptions();
    const fromMethods = this.methods().map((m) => m.code);
    const current = this.methodForm.controls.typeMethode.value?.trim();
    const all = [...catalog, ...fromMethods];
    if (current) {
      all.push(current);
    }
    return [...new Set(all.filter((v) => !!v))]
      .sort((a, b) => a.localeCompare(b))
      .map((value) => ({ label: value, value }));
  });
  readonly selectedApiId = signal<string | null>(null);
  readonly selectedApi = computed(() => {
    const id = this.selectedApiId();
    if (!id) return null;
    return this.fseApis().find((a) => a.id === id) ?? null;
  });
  readonly selectedMethod = signal<ProviderApi | null>(null);
  readonly selectedMethodParams = signal<MethodParam[]>([]);
  readonly draftMethodParams = signal<DraftMethodParam[]>([]);
  readonly paramsDirty = signal(false);
  readonly savingParams = signal(false);
  readonly generatingInterfaceId = signal<string | null>(null);
  readonly methodParamDialogVisible = signal(false);
  readonly editingMethodParamIndex = signal<number | null>(null);
  readonly msg = signal<string | null>(null);
  readonly isError = signal(false);

  readonly methodParamForm = this.fb.nonNullable.group({
    codeParametre: ['', Validators.required],
    nomExterne: ['', Validators.required]
  });

  readonly methodParamDialogTitle = () =>
    this.editingMethodParamIndex() === null ? 'Ajouter un paramètre' : 'Modifier le paramètre';

  readonly methodDialogTitle = computed(() =>
    this.editingMethodId() ? 'Modifier la méthode' : 'Ajouter une méthode'
  );

  readonly apiForm = this.fb.nonNullable.group({
    code: ['', Validators.required],
    nom: ['', Validators.required],
    baseUrl: ['', Validators.required],
    host: [''],
    port: [''],
    actif: [true],
    authType: ['NONE' as AuthType],
    clientId: [''],
    clientSecret: [''],
    tokenUrl: [''],
    grantType: [''],
    username: [''],
    password: [''],
    apiKey: [''],
    customHeaderName: [''],
    customHeaderValue: ['']
  });

  readonly methodForm = this.fb.nonNullable.group({
    typeMethode: ['', Validators.required],
    codeMethode: [''],
    nomMethode: ['', Validators.required],
    typeAppel: ['POST', Validators.required],
    typeReponse: ['JSON'],
    actif: [true],
    parametres: this.fb.array([])
  });

  get parametres(): FormArray {
    return this.methodForm.get('parametres') as FormArray;
  }

  ngOnInit(): void {
    this.providerId = this.route.parent!.snapshot.paramMap.get('id')!;
    this.api.get(this.providerId).subscribe({
      next: (p) => this.provider.set(p),
      error: () => this.provider.set(null)
    });
    this.api.listOmnicanalTypeMethodes().subscribe({
      next: (types) => this.typeMethodeOptions.set(types),
      error: () => this.typeMethodeOptions.set([])
    });
    this.reload();
  }

  orphanMethods(): ProviderApi[] {
    return this.allMethods().filter((m) => !m.fseApiId);
  }

  methodsForApi(fseApiId: string): ProviderApi[] {
    return this.methods().filter((m) => m.fseApiId === fseApiId);
  }

  onApiRowClick(api: FseApi): void {
    if (this.selectedApiId() === api.id) {
      this.selectedApiId.set(null);
      this.clearSelectedMethod();
      return;
    }
    this.selectedApiId.set(api.id);
    this.clearSelectedMethod();
  }

  onMethodRowClick(method: ProviderApi): void {
    if (this.selectedMethod()?.id === method.id) {
      this.clearSelectedMethod();
      return;
    }
    this.selectedMethod.set(method);
    const params = parseParamsFromMapping(method.requestMappingJson);
    this.selectedMethodParams.set(params);
    this.draftMethodParams.set(
      params.map((p) => ({ ...p, localId: newDraftParamId() }))
    );
    this.paramsDirty.set(false);
  }

  clearSelectedMethod(): void {
    this.selectedMethod.set(null);
    this.selectedMethodParams.set([]);
    this.draftMethodParams.set([]);
    this.paramsDirty.set(false);
    this.methodParamDialogVisible.set(false);
    this.editingMethodParamIndex.set(null);
  }

  openAddMethodParam(): void {
    this.editingMethodParamIndex.set(null);
    this.methodParamForm.reset({ codeParametre: '', nomExterne: '' });
    this.methodParamDialogVisible.set(true);
  }

  openEditMethodParam(index: number): void {
    const draft = this.draftMethodParams()[index];
    if (!draft) return;
    this.editingMethodParamIndex.set(index);
    this.methodParamForm.reset({
      codeParametre: draft.codeParametre,
      nomExterne: draft.nomExterne
    });
    this.methodParamDialogVisible.set(true);
  }

  saveMethodParamDialog(): void {
    if (this.methodParamForm.invalid) return;
    const v = this.methodParamForm.getRawValue();
    const codeParametre = v.codeParametre.trim();
    const nomExterne = v.nomExterne.trim();
    if (!codeParametre || !nomExterne) return;
    const index = this.editingMethodParamIndex();
    if (index === null) {
      this.draftMethodParams.update((list) => [
        ...list,
        { localId: newDraftParamId(), codeParametre, nomExterne }
      ]);
    } else {
      this.draftMethodParams.update((list) =>
        list.map((item, i) => (i === index ? { ...item, codeParametre, nomExterne } : item))
      );
    }
    this.paramsDirty.set(true);
    this.methodParamDialogVisible.set(false);
  }

  removeDraftMethodParam(index: number): void {
    this.draftMethodParams.update((list) => list.filter((_, i) => i !== index));
    this.paramsDirty.set(true);
  }

  saveSelectedMethodParams(): void {
    const method = this.selectedMethod();
    if (!method) return;
    const parametres = this.draftMethodParams()
      .map((p) => ({
        codeParametre: p.codeParametre.trim(),
        nomExterne: p.nomExterne.trim()
      }))
      .filter((p) => p.codeParametre && p.nomExterne);

    this.savingParams.set(true);
    this.msg.set(null);
    this.api
      .updateApi(this.providerId, method.id, {
        environmentId: method.environmentId,
        operationId: method.operationId,
        code: method.code,
        nom: method.nom,
        httpMethod: method.httpMethod,
        path: method.path,
        contentType: method.contentType ?? 'application/json',
        timeoutSeconds: method.timeoutSeconds ?? 30,
        requestMappingJson: buildRequestMapping(parametres),
        responseMappingJson: method.responseMappingJson,
        retryEnabled: method.retryEnabled,
        maxAttempts: method.maxAttempts,
        retryDelayMs: method.retryDelayMs,
        backoffMultiplier: method.backoffMultiplier,
        retryOn: method.retryOn,
        actif: method.actif
      })
      .subscribe({
        next: (updated) => {
          this.savingParams.set(false);
          this.paramsDirty.set(false);
          this.isError.set(false);
          this.msg.set(`Paramètres de ${updated.code} enregistrés`);
          this.allMethods.update((list) => list.map((m) => (m.id === updated.id ? updated : m)));
          this.methods.update((list) => list.map((m) => (m.id === updated.id ? updated : m)));
          this.selectedMethod.set(updated);
          const params = parseParamsFromMapping(updated.requestMappingJson);
          this.selectedMethodParams.set(params);
          this.draftMethodParams.set(params.map((p) => ({ ...p, localId: newDraftParamId() })));
        },
        error: (err: { message?: string }) => {
          this.savingParams.set(false);
          this.isError.set(true);
          this.msg.set(err?.message ?? 'Enregistrement des paramètres impossible');
        }
      });
  }

  formatMappingJson(json: string | null): string {
    if (!json) {
      return '';
    }
    try {
      return JSON.stringify(JSON.parse(json), null, 2);
    } catch {
      return json;
    }
  }

  canSeedBundle(): boolean {
    const p = this.provider();
    if (!p) return false;
    return p.codeFse === '0011' || p.codeFse === '0004'
      || p.code === 'CORIS_MONEY' || p.code === 'CORIS_BANK';
  }

  toggleApiForm(): void {
    this.editingApiId.set(null);
    this.resetEditingFlags();
    const next = !this.showApiForm();
    this.showApiForm.set(next);
    if (next) {
      this.apiForm.reset({
        code: '', nom: '', baseUrl: '', host: '', port: '', actif: true,
        authType: 'NONE', clientId: '', clientSecret: '', tokenUrl: '', grantType: '',
        username: '', password: '', apiKey: '', customHeaderName: '', customHeaderValue: ''
      });
    }
  }

  openEditApi(api: FseApi): void {
    this.closeMethodForm();
    this.editingApiId.set(api.id);
    this.editingHasClientSecret.set(api.hasClientSecret);
    this.editingHasApiKey.set(api.hasApiKey);
    this.editingHasUsername.set(api.hasUsername);
    this.editingHasPassword.set(api.hasPassword);
    this.editingHasCustomHeaderValue.set(api.hasCustomHeaderValue);
    this.showApiForm.set(true);
    this.showMethodForm.set(false);
    this.apiForm.reset({
      code: api.code,
      nom: api.nom,
      baseUrl: api.baseUrl,
      host: api.host ?? '',
      port: api.port ?? '',
      actif: api.actif,
      authType: api.authType ?? 'NONE',
      clientId: api.clientId ?? '',
      clientSecret: '',
      tokenUrl: api.tokenUrl ?? '',
      grantType: api.grantType ?? '',
      username: '',
      password: '',
      apiKey: '',
      customHeaderName: api.customHeaderName ?? '',
      customHeaderValue: ''
    });
  }

  closeApiForm(): void {
    this.showApiForm.set(false);
    this.editingApiId.set(null);
    this.resetEditingFlags();
  }

  private resetEditingFlags(): void {
    this.editingHasClientSecret.set(false);
    this.editingHasApiKey.set(false);
    this.editingHasUsername.set(false);
    this.editingHasPassword.set(false);
    this.editingHasCustomHeaderValue.set(false);
  }

  private buildFseApiPayload(): FseApiRequest {
    const v = this.apiForm.getRawValue();
    return {
      code: v.code,
      nom: v.nom,
      typeApi: 'REST',
      baseUrl: v.baseUrl,
      host: v.host || null,
      port: v.port || null,
      actif: v.actif,
      authType: v.authType,
      clientId: v.clientId || null,
      clientSecret: v.clientSecret || null,
      tokenUrl: v.tokenUrl || null,
      grantType: v.grantType || null,
      username: v.username || null,
      password: v.password || null,
      apiKey: v.apiKey || null,
      customHeaderName: v.customHeaderName || null,
      customHeaderValue: v.customHeaderValue || null
    };
  }

  saveFseApi(): void {
    if (this.apiForm.invalid) {
      return;
    }
    this.savingApi.set(true);
    this.msg.set(null);
    const payload = this.buildFseApiPayload();
    const editId = this.editingApiId();
    const req = editId
      ? this.api.updateFseApi(this.providerId, editId, payload)
      : this.api.createFseApi(this.providerId, payload);
    req.subscribe({
      next: () => {
        this.savingApi.set(false);
        this.closeApiForm();
        this.isError.set(false);
        this.msg.set(editId ? 'Api mise à jour' : 'Api créée');
        this.reload();
      },
      error: (err: { message?: string }) => {
        this.savingApi.set(false);
        this.isError.set(true);
        this.msg.set(err?.message ?? 'Enregistrement Api impossible');
      }
    });
  }

  openMethodForm(fseApi: FseApi): void {
    this.clearSelectedMethod();
    this.editingMethodId.set(null);
    this.methodFormApiId.set(fseApi.id);
    this.methodFormApiLabel.set(`${fseApi.code} — ${fseApi.nom}`);
    this.showMethodForm.set(true);
    this.showApiForm.set(false);
    this.methodForm.reset({
      typeMethode: '',
      codeMethode: '',
      nomMethode: '',
      typeAppel: 'POST',
      typeReponse: 'JSON',
      actif: true
    });
    this.parametres.clear();
    this.addParam();
  }

  openEditMethod(method: ProviderApi, fseApi: FseApi): void {
    this.clearSelectedMethod();
    this.editingMethodId.set(method.id);
    this.methodFormApiId.set(fseApi.id);
    this.methodFormApiLabel.set(`${fseApi.code} — ${fseApi.nom}`);
    this.showMethodForm.set(true);
    this.showApiForm.set(false);
    this.methodForm.reset({
      typeMethode: method.code,
      codeMethode: '',
      nomMethode: method.path,
      typeAppel: method.httpMethod,
      typeReponse: 'JSON',
      actif: method.actif
    });
    this.parametres.clear();
    const params = parseParamsFromMapping(method.requestMappingJson);
    if (params.length === 0) {
      this.addParam();
    } else {
      params.forEach((p) => this.parametres.push(
        this.fb.nonNullable.group({
          codeParametre: [p.codeParametre, Validators.required],
          nomExterne: [p.nomExterne, Validators.required],
          typeParametre: ['STRING'],
          categorie: ['USER']
        })
      ));
    }
  }

  closeMethodForm(): void {
    this.showMethodForm.set(false);
    this.editingMethodId.set(null);
    this.methodFormApiId.set(null);
    this.methodFormApiLabel.set('');
    this.parametres.clear();
  }

  onMethodDialogVisible(visible: boolean): void {
    if (!visible) {
      this.closeMethodForm();
    } else {
      this.showMethodForm.set(true);
    }
  }

  addParam(): void {
    this.parametres.push(
      this.fb.nonNullable.group({
        codeParametre: ['', Validators.required],
        nomExterne: ['', Validators.required],
        typeParametre: ['STRING'],
        categorie: ['USER']
      })
    );
  }

  removeParam(index: number): void {
    this.parametres.removeAt(index);
  }

  saveMethod(): void {
    const fseApiId = this.methodFormApiId();
    const editId = this.editingMethodId();
    if (!fseApiId || this.methodForm.invalid) {
      this.methodForm.markAllAsTouched();
      return;
    }
    this.savingMethod.set(true);
    this.msg.set(null);
    const v = this.methodForm.getRawValue();
    const parametres = (v.parametres as Array<{
      codeParametre: string;
      nomExterne: string;
      typeParametre: string;
      categorie: string;
    }>)
      .filter((p) => p.codeParametre?.trim() && p.nomExterne?.trim())
      .map((p) => ({
        codeParametre: p.codeParametre.trim(),
        nomExterne: p.nomExterne.trim(),
        typeParametre: p.typeParametre || 'STRING',
        categorie: p.categorie || 'USER'
      }));

    if (editId) {
      const existing = this.methods().find((m) => m.id === editId);
      if (!existing) {
        this.savingMethod.set(false);
        this.isError.set(true);
        this.msg.set('Méthode introuvable');
        return;
      }
      const typeMethode = v.typeMethode.trim().toUpperCase();
      const updateApi$ = this.api.updateApi(this.providerId, editId, {
        environmentId: existing.environmentId,
        operationId: existing.operationId,
        code: typeMethode,
        nom: v.nomMethode || typeMethode,
        httpMethod: v.typeAppel,
        path: v.nomMethode,
        contentType: existing.contentType ?? 'application/json',
        timeoutSeconds: existing.timeoutSeconds ?? 30,
        requestMappingJson: buildRequestMapping(parametres),
        responseMappingJson: existing.responseMappingJson,
        retryEnabled: existing.retryEnabled,
        maxAttempts: existing.maxAttempts,
        retryDelayMs: existing.retryDelayMs,
        backoffMultiplier: existing.backoffMultiplier,
        retryOn: existing.retryOn,
        actif: v.actif
      });

      const save$ = existing.operationId
        ? this.api
            .updateOperation(this.providerId, existing.operationId, {
              code: typeMethode,
              nom: v.nomMethode || typeMethode,
              description: null,
              idempotent: true,
              actif: v.actif
            })
            .pipe(switchMap(() => updateApi$))
        : updateApi$;

      save$.subscribe({
        next: () => {
          this.savingMethod.set(false);
          this.isError.set(false);
          this.msg.set(`Méthode ${typeMethode} mise à jour`);
          this.closeMethodForm();
          this.reload();
        },
        error: (err: { message?: string }) => {
          this.savingMethod.set(false);
          this.isError.set(true);
          this.msg.set(err?.message ?? 'Mise à jour impossible');
        }
      });
      return;
    }

    this.api
      .createOmnicanalMethod(this.providerId, {
        fseApiId,
        typeMethode: v.typeMethode,
        codeMethode: v.codeMethode || null,
        nomMethode: v.nomMethode,
        typeAppel: v.typeAppel,
        typeReponse: v.typeReponse,
        actif: v.actif,
        idempotent: false,
        parametres
      })
      .subscribe({
        next: (res) => {
          this.savingMethod.set(false);
          this.isError.set(false);
          this.msg.set(`Méthode ${res.operationCode} ajoutée à l'Api ${res.fseApiCode}`);
          this.closeMethodForm();
          this.reload();
        },
        error: (err: { message?: string }) => {
          this.savingMethod.set(false);
          this.isError.set(true);
          this.msg.set(err?.message ?? 'Enregistrement impossible');
        }
      });
  }

  seedBundle(): void {
    this.seeding.set(true);
    this.msg.set(null);
    this.api.seedOmnicanalConnectivity(this.providerId).subscribe({
      next: (res) => {
        this.seeding.set(false);
        this.isError.set(false);
        this.msg.set(`Catalogue Coris : Api + ${res.apisUpserted} méthode(s)`);
        this.reload();
      },
      error: (err: { message?: string }) => {
        this.seeding.set(false);
        this.isError.set(true);
        this.msg.set(err?.message ?? 'Import impossible');
      }
    });
  }

  generateInterfaceFromMethod(method: ProviderApi): void {
    if (!method?.id || this.generatingInterfaceId()) {
      return;
    }
    this.generatingInterfaceId.set(method.id);
    this.msg.set(null);
    this.orchestrationApi.generateInterfaceFromApi(this.providerId, method.id).subscribe({
      next: (iface) => {
        this.generatingInterfaceId.set(null);
        this.isError.set(false);
        const n = iface.parametres?.length ?? 0;
        this.msg.set(
          `Interface REQUEST créée : ${iface.typeMethode} (${n} paramètre${n > 1 ? 's' : ''})`
        );
      },
      error: (err: { message?: string }) => {
        this.generatingInterfaceId.set(null);
        this.isError.set(true);
        this.msg.set(err?.message ?? 'Génération interface impossible');
      }
    });
  }

  reload(): void {
    this.loading.set(true);
    const keepApiId = this.selectedApiId();
    this.clearSelectedMethod();
    forkJoin({
      fseApis: this.api.listFseApis(this.providerId).pipe(catchError(() => of([] as FseApi[]))),
      methods: this.api.listApis(this.providerId).pipe(catchError(() => of([] as ProviderApi[])))
    }).subscribe({
      next: ({ fseApis, methods }) => {
        this.allMethods.set(methods);
        this.fseApis.set(fseApis);
        this.methods.set(methods.filter((m) => !!m.fseApiId));
        if (keepApiId && fseApis.some((a) => a.id === keepApiId)) {
          this.selectedApiId.set(keepApiId);
        } else {
          this.selectedApiId.set(null);
        }
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.msg.set(err?.message ?? 'Erreur');
        this.isError.set(true);
        this.loading.set(false);
      }
    });
  }
}

function parseParamsFromMapping(json: string | null): MethodParam[] {
  if (!json?.trim()) {
    return [];
  }
  try {
    const map = JSON.parse(json) as Record<string, unknown>;
    return Object.entries(map).map(([nomExterne, rawValue]) => {
      const value = String(rawValue ?? '');
      const match = /^\$\{(.+)\}$/.exec(value);
      return {
        nomExterne,
        codeParametre: match ? match[1] : value
      };
    });
  } catch {
    return [];
  }
}

function buildRequestMapping(parametres: MethodParam[]): string | null {
  if (!parametres.length) {
    return null;
  }
  const map: Record<string, string> = {};
  for (const p of parametres) {
    map[p.nomExterne] = `\${${p.codeParametre}}`;
  }
  return JSON.stringify(map);
}
