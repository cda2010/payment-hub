import { NgTemplateOutlet } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MultiSelectModule } from 'primeng/multiselect';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import {
  BpmProcessOption,
  CatalogDeService,
  CatalogDeServiceAdminService,
  CatalogDeServiceVersion,
  CatalogProductBinding,
  CatalogProductParticipant,
  CatalogProductRole,
  CatalogServiceStatus,
  CatalogWorkflowBinding,
  ConfigurationTranslations,
  LocalizedText
} from '../../core/service/catalog-de-service-admin.service';
import { I18nApiService, SupportedLanguage } from '../../core/service/i18n-api.service';
import { ProviderService } from '../../core/service/provider.service';
import { Provider, SERVICE_TYPE_CODES, ServiceTypeCode } from '../../core/model/api.models';
import { ACCESS_CHANNELS, AccessChannel, PRODUCT_AUDIENCES, ProductAudience, productJourneyMeta } from '../../core/service/gateway.models';

const DEFAULT_STEPS = [
  'SELECT_SOURCE_ACCOUNT',
  'CAPTURE_DESTINATION',
  'VALIDATE_ACCOUNT',
  'CAPTURE_AMOUNT',
  'CHECK_BALANCE',
  'CHECK_LIMITS',
  'CALCULATE_FEES',
  'SELECT_RAIL',
  'CONFIRM',
  'AUTHENTICATE',
  'DEBIT',
  'EXECUTE_TRANSFER',
  'WAIT_CONFIRMATION',
  'NOTIFY'
];

const DEFAULT_ROLES: CatalogProductRole[] = [
  { code: 'SOURCE', label: 'Compte émetteur', cardinality: 'ONE_REQUIRED', selection: 'USER' },
  { code: 'DESTINATION', label: 'Compte destinataire', cardinality: 'ONE_REQUIRED', selection: 'USER' }
];

const DEFAULT_PARTICIPANTS: CatalogProductParticipant[] = [
  { role: 'SOURCE', providerCode: 'CORIS_MONEY', default: true },
  { role: 'SOURCE', providerCode: 'WAVE', default: false },
  { role: 'DESTINATION', providerCode: 'CORIS_MONEY', default: true },
  { role: 'DESTINATION', providerCode: 'WAVE', default: false }
];

const DEFAULT_BINDINGS: CatalogProductBinding[] = [
  {
    step: 'SELECT_SOURCE_ACCOUNT',
    kind: 'UI',
    actorRole: 'SOURCE',
    formSource: 'FORM_KEY',
    formKey: 'account-transfer-source'
  },
  {
    step: 'CAPTURE_DESTINATION',
    kind: 'UI',
    actorRole: 'DESTINATION',
    formSource: 'FORM_KEY',
    formKey: 'account-transfer-destination'
  },
  {
    step: 'EXECUTE_TRANSFER',
    kind: 'INVOKE',
    actorRole: 'SOURCE',
    operation: 'TRANSFER_EXECUTE'
  },
  {
    step: 'generateOtp',
    kind: 'INVOKE',
    actorRole: 'SOURCE',
    operation: 'GENERATE_OTP'
  },
  {
    step: 'enterOtp',
    kind: 'UI',
    actorRole: 'SOURCE',
    formSource: 'FORM_KEY',
    formKey: 'provider-otp-code'
  }
];

const ROLE_CODE_OPTIONS: Array<{ label: string; value: string; defaultLabel: string }> = [
  { value: 'SOURCE', label: 'SOURCE — Compte émetteur', defaultLabel: 'Compte émetteur' },
  { value: 'DESTINATION', label: 'DESTINATION — Compte destinataire', defaultLabel: 'Compte destinataire' },
  { value: 'PAYER', label: 'PAYER — Payeur', defaultLabel: 'Payeur' },
  { value: 'BILLER', label: 'BILLER — Facturier', defaultLabel: 'Facturier' },
  { value: 'BENEFICIARY', label: 'BENEFICIARY — Bénéficiaire', defaultLabel: 'Bénéficiaire' },
  { value: 'CORRESPONDENT', label: 'CORRESPONDENT — Correspondant', defaultLabel: 'Correspondant' },
  { value: 'PROVIDER', label: 'PROVIDER — Opérateur métier', defaultLabel: 'Opérateur / fournisseur métier' },
  { value: 'AGENT_NETWORK', label: 'AGENT_NETWORK — Réseau agent', defaultLabel: 'Réseau agent' }
];

const SELECTION_OPTIONS = [
  { label: 'USER (choix client)', value: 'USER' },
  { label: 'FIXED (imposé)', value: 'FIXED' },
  { label: 'DERIVED (autre rôle)', value: 'DERIVED' }
];

const CARDINALITY_OPTIONS = [
  { label: 'ONE_REQUIRED', value: 'ONE_REQUIRED' },
  { label: 'ONE_OPTIONAL', value: 'ONE_OPTIONAL' },
  { label: 'MANY', value: 'MANY' }
];

const BINDING_KIND_OPTIONS = [
  { label: 'UI', value: 'UI' },
  { label: 'INVOKE', value: 'INVOKE' },
  { label: 'SOFT', value: 'SOFT' },
  { label: 'WAIT', value: 'WAIT' },
  { label: 'CAPABILITY', value: 'CAPABILITY' }
];

const FORM_SOURCE_OPTIONS = [
  { label: 'Form.io (formKey)', value: 'FORM_KEY' },
  { label: 'Produit facturier (BillerProduct)', value: 'BILLER_PRODUCT' },
  { label: 'Récap catalogue (RECAP)', value: 'RECAP' }
];

const BILLER_SCREEN_OPTIONS = [
  { label: 'LOOKUP — saisie / recherche', value: 'LOOKUP' },
  { label: 'CONFIRM — confirmation', value: 'CONFIRM' },
  { label: 'AMOUNT — montant', value: 'AMOUNT' },
  { label: 'LIST — sélection dans une liste', value: 'LIST' }
];

const EMPTY_PROVIDER_CODES: string[] = [];

const DEFAULT_APPROVAL_POLICY = {
  enabled: false,
  code: 'TRANSFER_APPROVAL',
  strategy: 'SEQUENTIAL',
  dmnDecisionKey: 'decision_TransferApproval',
  initiatorCanApprove: false,
  sameUserAcrossLevelsAllowed: false,
  formKey: 'account-transfer-approval',
  criticalFields: ['amount', 'currency', 'destAccount', 'sourceAccount'] as string[]
};

type ApprovalPolicyView = {
  enabled: boolean;
  code?: string;
  strategy?: string;
  dmnDecisionKey?: string;
  initiatorCanApprove?: boolean;
  sameUserAcrossLevelsAllowed?: boolean;
  formKey?: string;
  criticalFields?: string[];
};

type ApprovalPolicyEdit = {
  enabled: boolean;
  code: string;
  strategy: string;
  dmnDecisionKey: string;
  initiatorCanApprove: boolean;
  sameUserAcrossLevelsAllowed: boolean;
  formKey: string;
  criticalFieldsText: string;
};

type JourneyEdit = {
  journeyCode: string;
  supportedChannels: AccessChannel[];
  defaultChannel: AccessChannel | null;
  supportedAudiences: ProductAudience[];
  defaultAudience: ProductAudience | null;
};

type PresentationSectionEdit = {
  id: string;
  displayKind: string;
  dataSource: string;
  origin: string;
  title: string;
  bindsTo: string;
  searchable: boolean;
  pageable: boolean;
  itemsJson: string;
  paramsJson: string;
};

type PresentationLayerEdit = {
  version: number;
  enabled: boolean;
  sections: PresentationSectionEdit[];
};

/** Onglet éditeur : base produit ou overlay canal. */
type PresentationTab = 'BASE' | AccessChannel;

type PresentationEdit = {
  base: PresentationLayerEdit;
  overrides: Partial<Record<AccessChannel, PresentationLayerEdit>>;
  activeTab: PresentationTab;
  jsonMode: boolean;
  jsonDraft: string;
};

/** Libellés d'historique produit (templates i18n). */
type HistoryEdit = {
  kindLabel: string;
  titleTemplate: string;
  subtitleTemplate: string;
};

/** Message sortant catalogue (SMS/push…). */
type NotificationEdit = {
  code: string;
  channelHint: string;
  title: string;
  body: string;
};

const DEFAULT_HISTORY: HistoryEdit = {
  kindLabel: '',
  titleTemplate: '',
  subtitleTemplate: ''
};

const CHANNEL_HINT_OPTIONS = [
  { label: 'SMS', value: 'SMS' },
  { label: 'PUSH', value: 'PUSH' },
  { label: 'ALL', value: 'ALL' }
];

const DISPLAY_KIND_OPTIONS = [
  { label: 'FILTER_CHIPS', value: 'FILTER_CHIPS' },
  { label: 'SHORTCUT_SLIDE', value: 'SHORTCUT_SLIDE' },
  { label: 'LEAF_LIST', value: 'LEAF_LIST' },
  { label: 'SECTIONED_LIST', value: 'SECTIONED_LIST' },
  { label: 'ENTRY_GRID', value: 'ENTRY_GRID' },
  { label: 'DRILL_NAV', value: 'DRILL_NAV' },
];

const DATA_SOURCE_OPTIONS = [
  { label: 'BILLER_CATEGORIES', value: 'BILLER_CATEGORIES' },
  { label: 'BILLERS', value: 'BILLERS' },
  { label: 'BILL_FAVORITES', value: 'BILL_FAVORITES' },
  { label: 'STATIC_ITEMS', value: 'STATIC_ITEMS' },
  { label: 'TRANSFER_PARTICIPANTS (params.role = SOURCE | DESTINATION)', value: 'TRANSFER_PARTICIPANTS' },
  { label: 'TRANSFER_BENEFICIARIES (carnet bénéficiaires)', value: 'TRANSFER_BENEFICIARIES' },
  { label: 'CATALOG_PARTICIPANTS (params.role = PAYER | …)', value: 'CATALOG_PARTICIPANTS' },
  { label: 'CUSTOM (saisie libre)', value: 'CUSTOM' },
];

const ORIGIN_OPTIONS = [
  { label: 'LOCAL', value: 'LOCAL' },
  { label: 'OMNICANAL (typeMethode DATASOURCE)', value: 'OMNICANAL' },
];

const EMPTY_SECTION = (): PresentationSectionEdit => ({
  id: '',
  displayKind: 'LEAF_LIST',
  dataSource: 'BILLERS',
  origin: 'LOCAL',
  title: '',
  bindsTo: '',
  searchable: true,
  pageable: true,
  itemsJson: '[]',
  paramsJson: '{}',
});

const EMPTY_LAYER = (enabled = true): PresentationLayerEdit => ({
  version: 1,
  enabled,
  sections: [],
});

const DEFAULT_BILL_PRESENTATION_LAYER = (): PresentationLayerEdit => ({
  version: 1,
  enabled: true,
  sections: [
    {
      id: 'favorites',
      displayKind: 'SHORTCUT_SLIDE',
      dataSource: 'BILL_FAVORITES',
      origin: 'LOCAL',
      title: 'Vos favoris',
      bindsTo: '',
      searchable: false,
      pageable: false,
      itemsJson: '[]',
      paramsJson: JSON.stringify({ limit: 8 }, null, 2),
    },
    {
      id: 'categories',
      displayKind: 'FILTER_CHIPS',
      dataSource: 'BILLER_CATEGORIES',
      origin: 'LOCAL',
      title: 'Catégories',
      bindsTo: 'billers',
      searchable: false,
      pageable: false,
      itemsJson: '[]',
      paramsJson: '{}',
    },
    {
      id: 'billers',
      displayKind: 'LEAF_LIST',
      dataSource: 'BILLERS',
      origin: 'LOCAL',
      title: '',
      bindsTo: '',
      searchable: true,
      pageable: true,
      itemsJson: '[]',
      paramsJson: JSON.stringify({ size: 30 }, null, 2),
    },
  ],
});

/** Minuscules, sans accents : « Liaison » trouve « liaison », « Évaluer » trouve « evaluer ». */
function normalizeSearch(value: string | null | undefined): string {
  return (value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

@Component({
  selector: 'app-catalog-de-service-admin',
  standalone: true,
  imports: [
    NgTemplateOutlet,
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    DropdownModule,
    MultiSelectModule,
    ProgressSpinnerModule,
    TagModule,
    TooltipModule
  ],
  template: `
    <div class="page">
      <header class="hero">
        <div>
          <p class="eyebrow">Configuration produit</p>
          <h1>Catalogue de services</h1>
          <p class="lede">
            Rôles, participants et process RapidBPMN — le graphe se dessine dans le concepteur,
            le catalogue définit qui joue et avec quels écrans.
          </p>
        </div>
        <div class="hero-actions">
          <a pButton routerLink="/bpmn-designer" label="Concepteur BPMN" icon="pi pi-sitemap"
             class="p-button-outlined p-button-secondary"></a>
          <button pButton type="button" label="Nouveau produit" icon="pi pi-plus"
                  (click)="startNewService()"></button>
        </div>
      </header>

      @if (error()) {
        <p class="banner err">{{ error() }}</p>
      }
      @if (info()) {
        <p class="banner ok">{{ info() }}</p>
      }

      <div class="shell">
        <aside class="rail">
          <div class="rail-head">
            <span>Produits</span>
            <span class="count">
              {{ productFilterActive() ? filteredServices().length + ' / ' + services().length : services().length }}
            </span>
          </div>
          @if (services().length) {
            <div class="rail-filters">
              <div class="search-box">
                <i class="pi pi-search" aria-hidden="true"></i>
                <input pInputText type="search" class="w-full"
                       placeholder="Rechercher : nom, code, catégorie…"
                       aria-label="Rechercher un produit"
                       [ngModel]="productSearch()" (ngModelChange)="productSearch.set($event)"
                       (keydown.escape)="productSearch.set('')" />
                @if (productSearch()) {
                  <button type="button" class="search-clear" aria-label="Effacer la recherche"
                          (click)="productSearch.set('')">
                    <i class="pi pi-times"></i>
                  </button>
                }
              </div>
              @if (productStatuses().length > 1) {
                <div class="status-chips" role="group" aria-label="Filtrer par statut">
                  <button type="button" class="chip" [class.on]="!productStatusFilter()"
                          (click)="productStatusFilter.set(null)">Tous</button>
                  @for (s of productStatuses(); track s.status) {
                    <button type="button" class="chip" [class.on]="productStatusFilter() === s.status"
                            (click)="toggleProductStatus(s.status)">
                      {{ s.status }} <span class="chip-count">{{ s.count }}</span>
                    </button>
                  }
                </div>
              }
            </div>
          }
          @if (loading()) {
            <div class="center">
              <p-progressSpinner strokeWidth="4" [style]="{ width: '32px', height: '32px' }" />
            </div>
          } @else if (!services().length) {
            <p class="muted pad">Aucun produit.</p>
          } @else if (!filteredServices().length) {
            <p class="muted pad">
              Aucun produit ne correspond.
              <button type="button" class="link-btn" (click)="clearProductFilters()">Réinitialiser</button>
            </p>
          } @else {
            <ul class="product-list">
              @for (item of filteredServices(); track item.id) {
                <li [class.active]="selected()?.id === item.id" (click)="selectService(item)">
                  <div class="prod-top">
                    <strong>{{ item.name }}</strong>
                    <p-tag [value]="item.status" [severity]="statusSeverity(item.status)" />
                  </div>
                  <span class="prod-meta">{{ item.code }} · {{ item.categoryCode || '—' }} · v{{ item.currentVersion || '?' }}</span>
                </li>
              }
            </ul>
          }
        </aside>

        <main class="canvas">
          @if (editingService()) {
            <section class="card">
              <h2>{{ selected() ? 'Métadonnées produit' : 'Nouveau produit' }}</h2>
              <form [formGroup]="serviceForm" (ngSubmit)="saveService()" class="form-grid">
                <div class="field">
                  <label for="svcCode">Code</label>
                  <input pInputText id="svcCode" formControlName="code" class="w-full" [readonly]="!!selected()" />
                </div>
                <div class="field">
                  <label for="svcName">Nom ({{ defaultLanguage() }})</label>
                  <input pInputText id="svcName" formControlName="name" class="w-full" />
                </div>
                @for (lang of otherLanguages(); track lang.code) {
                  <div class="field">
                    <label [for]="'svcName-' + lang.code">Nom ({{ lang.code }})</label>
                    <input pInputText [id]="'svcName-' + lang.code" class="w-full"
                           [placeholder]="lang.label"
                           [ngModel]="localizedName(lang.code)" [ngModelOptions]="{standalone: true}"
                           (ngModelChange)="patchNameI18n(lang.code, $event)" />
                  </div>
                }
                <div class="field span-2">
                  <label for="svcCategory">Catégorie</label>
                  <p-dropdown
                    inputId="svcCategory"
                    formControlName="categoryCode"
                    [options]="categoryOptions()"
                    optionLabel="label"
                    optionValue="value"
                    placeholder="Catégorie"
                    styleClass="w-full"
                    [filter]="true"
                  />
                </div>
                <div class="field span-2">
                  <label for="svcDesc">Description ({{ defaultLanguage() }})</label>
                  <textarea pInputTextarea id="svcDesc" formControlName="description" rows="2" class="w-full"></textarea>
                </div>
                @for (lang of otherLanguages(); track lang.code) {
                  <div class="field span-2">
                    <label [for]="'svcDesc-' + lang.code">Description ({{ lang.code }})</label>
                    <textarea pInputTextarea [id]="'svcDesc-' + lang.code" rows="2" class="w-full"
                              [placeholder]="lang.label"
                              [ngModel]="localizedDescription(lang.code)" [ngModelOptions]="{standalone: true}"
                              (ngModelChange)="patchDescriptionI18n(lang.code, $event)"></textarea>
                  </div>
                }
                <div class="field">
                  <label for="svcStatus">Statut</label>
                  <p-dropdown
                    inputId="svcStatus"
                    formControlName="status"
                    [options]="statusOptions"
                    optionLabel="label"
                    optionValue="value"
                    styleClass="w-full"
                  />
                </div>
                @if (!selected()) {
                  <div class="field">
                    <label for="svcVersion">Version initiale</label>
                    <input pInputText id="svcVersion" formControlName="initialVersion" class="w-full" />
                  </div>
                  <div class="field span-2">
                    <label for="svcWorkflow">Process RapidBPMN</label>
                    <p-dropdown
                      inputId="svcWorkflow"
                      formControlName="workflowKey"
                      [options]="processOptions()"
                      optionLabel="label"
                      optionValue="value"
                      [editable]="true"
                      styleClass="w-full"
                      [filter]="true"
                    />
                  </div>
                  <div class="field span-2">
                    <label for="svcSteps">Steps (documentation)</label>
                    <textarea pInputTextarea id="svcSteps" formControlName="stepsText" rows="5" class="w-full mono"></textarea>
                  </div>

                  <div class="field span-2">
                    <ng-container [ngTemplateOutlet]="rolesEditor" />
                  </div>
                  <div class="field span-2">
                    <ng-container [ngTemplateOutlet]="participantsEditor" />
                  </div>
                  <div class="field span-2">
                    <ng-container [ngTemplateOutlet]="bindingsEditor" />
                  </div>
                }
                <div class="actions span-2">
                  <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelServiceEdit()"></button>
                  <button pButton type="submit" label="Enregistrer" [disabled]="busy() || serviceForm.invalid"></button>
                </div>
              </form>
            </section>
          } @else if (selected()) {
            <section class="card head-card">
              <div>
                <p class="eyebrow">{{ selected()!.code }}</p>
                <h2>{{ selected()!.name }}</h2>
                <p class="meta">
                  {{ selected()!.categoryName || selected()!.categoryCode || '—' }}
                  · version courante <strong>v{{ selected()!.currentVersion || '—' }}</strong>
                </p>
              </div>
              <div class="head-actions">
                <a pButton routerLink="/bpmn-designer" label="RapidBPMN" icon="pi pi-sitemap"
                   class="p-button-outlined p-button-sm"></a>
                <button pButton type="button" icon="pi pi-pencil" class="p-button-outlined p-button-sm"
                        label="Métadonnées" (click)="editService()"></button>
                <button pButton type="button" icon="pi pi-plus" class="p-button-sm"
                        label="Nouvelle version" (click)="startNewVersion()"></button>
              </div>
            </section>

            @if (editingVersion()) {
              <section class="card">
                <h3>Nouvelle version</h3>
                <form [formGroup]="versionForm" (ngSubmit)="saveVersion()" class="form-grid">
                  <div class="field">
                    <label for="verLabel">Version</label>
                    <input pInputText id="verLabel" formControlName="version" class="w-full" placeholder="1.1" />
                  </div>
                  <div class="field">
                    <label for="verStatus">Statut</label>
                    <p-dropdown
                      inputId="verStatus"
                      formControlName="status"
                      [options]="statusOptions"
                      optionLabel="label"
                      optionValue="value"
                      styleClass="w-full"
                    />
                  </div>
                  <div class="field span-2">
                    <label for="verWorkflow">Process RapidBPMN</label>
                    <p-dropdown
                      inputId="verWorkflow"
                      formControlName="workflowKey"
                      [options]="processOptions()"
                      optionLabel="label"
                      optionValue="value"
                      [editable]="true"
                      styleClass="w-full"
                      [filter]="true"
                    />
                  </div>
                  <div class="field">
                    <label for="verServiceType">Type de service</label>
                    <p-dropdown
                      inputId="verServiceType"
                      formControlName="serviceType"
                      [options]="serviceTypeOptions"
                      optionLabel="label"
                      optionValue="value"
                      styleClass="w-full"
                      [filter]="true"
                    />
                  </div>
                  <div class="field span-2">
                    <label for="verSteps">Steps (doc)</label>
                    <textarea pInputTextarea id="verSteps" formControlName="stepsText" rows="4" class="w-full mono"></textarea>
                  </div>
                  <div class="field span-2">
                    <ng-container [ngTemplateOutlet]="rolesEditor" />
                  </div>
                  <div class="field span-2">
                    <ng-container [ngTemplateOutlet]="participantsEditor" />
                  </div>
                  <div class="field span-2">
                    <ng-container [ngTemplateOutlet]="bindingsEditor" />
                  </div>
                  <div class="actions span-2">
                    <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelVersionEdit()"></button>
                    <button pButton type="submit" label="Créer version" [disabled]="busy() || versionForm.invalid"></button>
                  </div>
                </form>
              </section>
            } @else {
              <section class="card">
                <div class="section-head">
                  <h3>Versions</h3>
                </div>
                @if (versionsLoading()) {
                  <div class="center"><p-progressSpinner strokeWidth="4" [style]="{ width: '28px', height: '28px' }" /></div>
                } @else if (!versions().length) {
                  <p class="muted">Aucune version.</p>
                } @else {
                  <div class="version-grid">
                    @for (v of versions(); track v.id) {
                      <div
                        class="version-tile"
                        [class.active]="selectedVersion()?.id === v.id"
                        (click)="selectVersion(v)"
                        (keydown.enter)="selectVersion(v)"
                        tabindex="0"
                        role="button"
                      >
                        <div class="vt-top">
                          <strong>v{{ v.version }}</strong>
                          <p-tag [value]="v.status" [severity]="statusSeverity(v.status)" />
                        </div>
                        <code>{{ v.workflowKey || '—' }}</code>
                        <span class="vt-meta">{{ roleCount(v) }} rôles · {{ participantCount(v) }} participants · {{ stepCount(v) }} steps</span>
                        <div class="vt-actions" (click)="$event.stopPropagation()">
                          <button pButton type="button" label="Modifier" class="p-button-outlined p-button-sm"
                                  [disabled]="busy()" (click)="beginModifyCasting(v)"></button>
                          @if (v.status !== 'PUBLISHED' && v.status !== 'ARCHIVED') {
                            <button pButton type="button" label="Publier" class="p-button-sm"
                                    [disabled]="busy()" (click)="publish(v)"></button>
                          }
                          <button pButton type="button" label="Valider BPMN" class="p-button-text p-button-sm"
                                  [disabled]="busy() || !v.workflowKey" (click)="validateWorkflow(v)"></button>
                        </div>
                      </div>
                    }
                  </div>
                }
              </section>

              @if (selectedVersion(); as ver) {
                <section class="card">
                  <div class="section-head">
                    <div>
                      <h3>Casting · v{{ ver.version }}</h3>
                      <p class="meta">
                        RapidBPMN <code>{{ ver.workflowKey || '—' }}</code>
                        @if (configValue(ver, 'serviceType'); as st) {
                          · serviceType <code>{{ st }}</code>
                        }
                      </p>
                    </div>
                    <div class="head-actions">
                      @if (editingCasting() && canEditCasting(ver)) {
                        <button pButton type="button" label="Annuler" class="p-button-text p-button-sm"
                                (click)="cancelCastingEdit(ver)"></button>
                        <button pButton type="button" label="Enregistrer" icon="pi pi-check" class="p-button-sm"
                                [disabled]="busy()" (click)="saveDraftConfig(ver)"></button>
                      } @else {
                        <button pButton type="button" label="Modifier" icon="pi pi-pencil"
                                class="p-button-outlined p-button-sm"
                                [disabled]="busy() || ver.status === 'ARCHIVED'"
                                (click)="beginModifyCasting(ver)"
                                [pTooltip]="canEditCasting(ver)
                                  ? 'Modifier rôles et participants'
                                  : 'Crée une nouvelle version brouillon (version publiée figée)'"
                        ></button>
                      }
                    </div>
                  </div>

                  @if (editingCasting() && canEditCasting(ver)) {
                    <div class="draft-box open">
                      <ng-container [ngTemplateOutlet]="rolesEditor" />
                      <ng-container [ngTemplateOutlet]="participantsEditor" />
                      <ng-container [ngTemplateOutlet]="bindingsEditor" />
                    </div>
                  } @else {
                    <div class="cast-cols">
                      <div>
                        <h4>Rôles</h4>
                        @if (rolesOf(ver).length) {
                          <ul class="chip-list">
                            @for (r of rolesOf(ver); track r.code) {
                              <li>
                                <strong>{{ r.label || r.code }}</strong>
                                <span><code>{{ r.code }}</code> · {{ r.selection || 'USER' }} · {{ r.cardinality || 'ONE_REQUIRED' }}</span>
                              </li>
                            }
                          </ul>
                        } @else {
                          <p class="muted">Aucun rôle.</p>
                        }
                      </div>
                      <div>
                        <h4>Participants</h4>
                        @if (participantsGrouped(ver).length) {
                          @for (g of participantsGrouped(ver); track g.role) {
                            <div class="part-group">
                              <div class="part-role">{{ g.role }}</div>
                              <div class="part-providers">
                                @for (p of g.items; track p.providerCode) {
                                  <span class="provider-pill" [class.def]="p.default">
                                    {{ providerLabel(p.providerCode) }}
                                    @if (p.default) {
                                      <em>défaut</em>
                                    }
                                  </span>
                                }
                              </div>
                            </div>
                          }
                        } @else {
                          <p class="muted">Aucun participant.</p>
                        }
                      </div>
                    </div>

                    <h4>Bindings</h4>
                    @if (bindingsOf(ver).length) {
                      <div class="bind-table-wrap">
                        <table class="data-table">
                          <thead>
                            <tr><th>Step</th><th>Kind</th><th>Rôle</th><th>Provider</th><th>UI / Opération</th></tr>
                          </thead>
                          <tbody>
                            @for (b of bindingsOf(ver); track b.step + b.kind + (b.providerCode || '') + (b.formKey || '') + (b.billerScreen || '')) {
                              <tr>
                                <td><code>{{ b.step }}</code></td>
                                <td><p-tag [value]="b.kind" [severity]="kindSeverity(b.kind)" /></td>
                                <td>{{ b.actorRole || '—' }}</td>
                                <td>{{ b.providerCode || 'tous' }}</td>
                                <td>{{ bindingSummary(b) }}</td>
                              </tr>
                            }
                          </tbody>
                        </table>
                      </div>
                    } @else {
                      <p class="muted">Aucun binding.</p>
                    }

                    @if (ver.status === 'PUBLISHED') {
                      <p class="hint locked">Version publiée figée — cliquez Modifier pour créer un brouillon.</p>
                    }
                  }
                </section>

                <section class="card messaging-card">
                  <div class="section-head">
                    <div>
                      <h3>Historique &amp; messages</h3>
                      <p class="meta">
                        Libellés relevé (title/subtitle) et SMS/push — templates
                        <code ngNonBindable>{{var}}</code> + i18n
                      </p>
                    </div>
                    <div class="head-actions">
                      @if (editingMessaging() && canEditCasting(ver)) {
                        <button pButton type="button" label="Annuler" class="p-button-text p-button-sm"
                                (click)="cancelMessagingEdit(ver)"></button>
                        <button pButton type="button" label="Enregistrer" icon="pi pi-check" class="p-button-sm"
                                [disabled]="busy()" (click)="saveMessagingSettings(ver)"></button>
                      } @else {
                        <button pButton type="button" label="Modifier" icon="pi pi-pencil"
                                class="p-button-outlined p-button-sm"
                                [disabled]="busy() || ver.status === 'ARCHIVED'"
                                (click)="beginModifyMessaging(ver)"
                                [pTooltip]="canEditCasting(ver)
                                  ? 'Configurer history + notifications'
                                  : 'Crée une nouvelle version brouillon (version publiée figée)'"
                        ></button>
                      }
                    </div>
                  </div>

                  @if (editingMessaging() && canEditCasting(ver)) {
                    <div class="i18n-matrix" [style.--i18n-cols]="messagingLangCodes().length">
                      <div class="i18n-matrix-head">
                        <span class="i18n-row-label">Champ</span>
                        @for (lang of messagingLangCodes(); track lang) {
                          <span class="i18n-col-head" [class.default]="lang === defaultLanguage()">
                            {{ lang }}
                            @if (lang === defaultLanguage()) {
                              <em>défaut</em>
                            }
                          </span>
                        }
                      </div>
                      @for (field of historyFields; track field.key) {
                        <div class="i18n-matrix-row">
                          <span class="i18n-row-label">
                            <strong>{{ field.label }}</strong>
                            <small>{{ field.key }}</small>
                          </span>
                          @for (lang of messagingLangCodes(); track lang) {
                            <div class="i18n-col-cell">
                              @if (field.multiline) {
                                <textarea pInputTextarea rows="2" class="w-full mono"
                                          [ngModel]="historyText(field.key, lang)"
                                          [ngModelOptions]="{standalone: true}"
                                          (ngModelChange)="setHistoryText(field.key, lang, $event)"
                                          [placeholder]="field.placeholder"></textarea>
                              } @else {
                                <input pInputText class="w-full" [class.mono]="field.mono"
                                       [ngModel]="historyText(field.key, lang)"
                                       [ngModelOptions]="{standalone: true}"
                                       (ngModelChange)="setHistoryText(field.key, lang, $event)"
                                       [placeholder]="field.placeholder" />
                              }
                            </div>
                          }
                        </div>
                      }
                    </div>
                    <p class="hint">
                      Variables :
                      <code>kind</code>, <code>party</code>, <code>channel</code>,
                      <code>agentDisplay</code>, <code>statusLabel</code>, <code>amount</code>,
                      <code>currency</code>, <code>when</code>, <code>paymentId</code>…
                      — vide = heuristique gateway.
                    </p>

                    <div class="editor-block" style="margin-top: 1rem">
                      <div class="editor-head">
                        <div>
                          <h4>Notifications</h4>
                          <p class="hint">Codes événement (SUCCESS, WAITING_CODE…) → corps SMS/push</p>
                        </div>
                        <button pButton type="button" icon="pi pi-plus" label="Ajouter"
                                class="p-button-outlined p-button-sm"
                                (click)="addNotification()"></button>
                      </div>
                      @if (!editNotifications().length) {
                        <p class="muted">Aucune notification — le NOTIFY publiera un événement sans corps.</p>
                      }
                      @for (n of editNotifications(); track i; let i = $index) {
                        <div class="notif-card">
                          <div class="notif-meta">
                            <div class="field">
                              <label>Code</label>
                              <input pInputText class="w-full"
                                     [ngModel]="n.code" [ngModelOptions]="{standalone: true}"
                                     (ngModelChange)="patchNotification(i, { code: $event })"
                                     placeholder="SUCCESS" />
                            </div>
                            <div class="field">
                              <label>Canal</label>
                              <p-dropdown [options]="channelHintOptions" optionLabel="label" optionValue="value"
                                          [ngModel]="n.channelHint" [ngModelOptions]="{standalone: true}"
                                          (ngModelChange)="patchNotification(i, { channelHint: $event })"
                                          styleClass="w-full"></p-dropdown>
                            </div>
                            <button pButton type="button" icon="pi pi-trash"
                                    class="p-button-text p-button-danger p-button-sm notif-del"
                                    (click)="removeNotification(i)"
                                    pTooltip="Supprimer"></button>
                          </div>
                          <div class="i18n-matrix" [style.--i18n-cols]="messagingLangCodes().length">
                            <div class="i18n-matrix-head">
                              <span class="i18n-row-label">Texte</span>
                              @for (lang of messagingLangCodes(); track lang) {
                                <span class="i18n-col-head" [class.default]="lang === defaultLanguage()">
                                  {{ lang }}
                                  @if (lang === defaultLanguage()) {
                                    <em>défaut</em>
                                  }
                                </span>
                              }
                            </div>
                            <div class="i18n-matrix-row">
                              <span class="i18n-row-label"><strong>Titre</strong><small>optionnel</small></span>
                              @for (lang of messagingLangCodes(); track lang) {
                                <div class="i18n-col-cell">
                                  <input pInputText class="w-full"
                                         [ngModel]="notifText(n, 'title', lang)"
                                         [ngModelOptions]="{standalone: true}"
                                         (ngModelChange)="setNotifText(i, n.code, 'title', lang, $event)" />
                                </div>
                              }
                            </div>
                            <div class="i18n-matrix-row">
                              <span class="i18n-row-label"><strong>Corps</strong><small>body</small></span>
                              @for (lang of messagingLangCodes(); track lang) {
                                <div class="i18n-col-cell">
                                  <textarea pInputTextarea rows="3" class="w-full"
                                            [ngModel]="notifText(n, 'body', lang)"
                                            [ngModelOptions]="{standalone: true}"
                                            (ngModelChange)="setNotifText(i, n.code, 'body', lang, $event)"
                                            [placeholder]="notificationBodyPlaceholder"></textarea>
                                </div>
                              }
                            </div>
                          </div>
                        </div>
                      }
                    </div>
                  } @else {
                    <div class="approval-summary">
                      @if (historySummary(ver); as h) {
                        <ul class="chip-list">
                          <li><strong>Kind</strong><span>{{ h.kindLabel || '—' }}</span></li>
                          <li><strong>Titre</strong><span class="mono">{{ h.titleTemplate || 'heuristique' }}</span></li>
                          <li><strong>Sous-titre</strong><span class="mono">{{ h.subtitleTemplate || 'heuristique' }}</span></li>
                          <li><strong>Notifications</strong><span>{{ notificationsOf(ver).length }} modèle(s)</span></li>
                        </ul>
                      } @else {
                        <p class="muted">Non configuré — heuristique gateway + NOTIFY sans corps.</p>
                      }
                    </div>
                    @if (ver.status === 'PUBLISHED') {
                      <p class="hint locked">Version publiée figée — cliquez Modifier pour créer un brouillon.</p>
                    }
                  }
                </section>


                <section class="card">
                  <div class="section-head">
                    <div>
                      <h3>Journey · Channel</h3>
                      <p class="meta">
                        PRODUCT → JOURNEY → CHANNEL (MOBILE / WEB / GAB / USSD / BACKOFFICE)
                      </p>
                    </div>
                    <div class="head-actions">
                      @if (editingJourney() && canEditCasting(ver)) {
                        <button pButton type="button" label="Annuler" class="p-button-text p-button-sm"
                                (click)="cancelJourneyEdit(ver)"></button>
                        <button pButton type="button" label="Enregistrer" icon="pi pi-check" class="p-button-sm"
                                [disabled]="busy()" (click)="saveJourneySettings(ver)"></button>
                      } @else {
                        <button pButton type="button" label="Modifier" icon="pi pi-pencil"
                                class="p-button-outlined p-button-sm"
                                [disabled]="busy() || ver.status === 'ARCHIVED'"
                                (click)="beginModifyJourney(ver)"
                                [pTooltip]="canEditCasting(ver)
                                  ? 'Configurer journey, canaux et audiences'
                                  : 'Crée une nouvelle version brouillon (version publiée figée)'"
                        ></button>
                      }
                    </div>
                  </div>

                  @if (editingJourney() && canEditCasting(ver)) {
                    <div class="form-grid">
                      <div class="field">
                        <label>journeyCode</label>
                        <input pInputText class="w-full mono" [ngModel]="editJourney().journeyCode"
                               [ngModelOptions]="{standalone: true}"
                               (ngModelChange)="patchJourney({ journeyCode: $event })"
                               placeholder="STANDARD_CASH_OUT" />
                      </div>
                      <div class="field">
                        <label>defaultChannel</label>
                        <p-dropdown
                          [options]="accessChannelOptions"
                          optionLabel="label"
                          optionValue="value"
                          [ngModel]="editJourney().defaultChannel"
                          [ngModelOptions]="{standalone: true}"
                          (ngModelChange)="patchJourney({ defaultChannel: $event })"
                          placeholder="Canal défaut"
                          styleClass="w-full"
                          [showClear]="true"
                        />
                      </div>
                      <div class="field span-2">
                        <label>supportedChannels</label>
                        <p-multiSelect
                          [options]="accessChannelOptions"
                          optionLabel="label"
                          optionValue="value"
                          [ngModel]="editJourney().supportedChannels"
                          [ngModelOptions]="{standalone: true}"
                          (ngModelChange)="patchJourney({ supportedChannels: $event || [] })"
                          placeholder="Canaux autorisés"
                          styleClass="w-full"
                          display="chip"
                        />
                      </div>
                      <div class="field">
                        <label>defaultAudience</label>
                        <p-dropdown
                          [options]="productAudienceOptions"
                          optionLabel="label"
                          optionValue="value"
                          [ngModel]="editJourney().defaultAudience"
                          [ngModelOptions]="{standalone: true}"
                          (ngModelChange)="patchJourney({ defaultAudience: $event })"
                          placeholder="Audience défaut"
                          styleClass="w-full"
                          [showClear]="true"
                        />
                      </div>
                      <div class="field span-2">
                        <label>supportedAudiences</label>
                        <p-multiSelect
                          [options]="productAudienceOptions"
                          optionLabel="label"
                          optionValue="value"
                          [ngModel]="editJourney().supportedAudiences"
                          [ngModelOptions]="{standalone: true}"
                          (ngModelChange)="patchJourney({ supportedAudiences: $event || [] })"
                          placeholder="Audiences cibles (RETAIL, CORPORATE…)"
                          styleClass="w-full"
                          display="chip"
                        />
                      </div>
                    </div>
                  } @else {
                    <dl class="journey-dl">
                      <div>
                        <dt>Journey</dt>
                        <dd><code>{{ journeyOf(ver).journeyCode || '—' }}</code></dd>
                      </div>
                      <div>
                        <dt>Canal défaut</dt>
                        <dd><code>{{ journeyOf(ver).defaultChannel || '—' }}</code></dd>
                      </div>
                      <div class="span-2">
                        <dt>Canaux supportés</dt>
                        <dd>
                          @if (journeyOf(ver).supportedChannels.length) {
                            @for (ch of journeyOf(ver).supportedChannels; track ch) {
                              <span class="provider-pill">{{ ch }}</span>
                            }
                          } @else {
                            —
                          }
                        </dd>
                      </div>
                      <div>
                        <dt>Audience défaut</dt>
                        <dd><code>{{ journeyOf(ver).defaultAudience || '—' }}</code></dd>
                      </div>
                      <div class="span-2">
                        <dt>Audiences supportées</dt>
                        <dd>
                          @if (journeyOf(ver).supportedAudiences.length) {
                            @for (a of journeyOf(ver).supportedAudiences; track a) {
                              <span class="provider-pill">{{ a }}</span>
                            }
                          } @else {
                            —
                          }
                        </dd>
                      </div>
                    </dl>
                  }
                </section>

                <section class="card">
                  <div class="section-head">
                    <div>
                      <h3>Présentation catalogue</h3>
                      <p class="meta">
                        displayKind + dataSource — base produit, overlays par canal (Mobile, Web…)
                      </p>
                    </div>
                    <div class="head-actions">
                      @if (editingPresentation() && canEditCasting(ver)) {
                        <button pButton type="button" label="Annuler" class="p-button-text p-button-sm"
                                (click)="cancelPresentationEdit(ver)"></button>
                        <button pButton type="button" label="Enregistrer" icon="pi pi-check" class="p-button-sm"
                                [disabled]="busy()" (click)="savePresentationSettings(ver)"></button>
                      } @else {
                        <button pButton type="button" label="Modifier" icon="pi pi-pencil"
                                class="p-button-outlined p-button-sm"
                                [disabled]="busy() || ver.status === 'ARCHIVED'"
                                (click)="beginModifyPresentation(ver)"
                                [pTooltip]="canEditCasting(ver)
                                  ? 'Configurer la présentation catalogue par canal'
                                  : 'Crée une nouvelle version brouillon (version publiée figée)'"
                        ></button>
                      }
                    </div>
                  </div>

                  @if (editingPresentation() && canEditCasting(ver)) {
                    <div class="pres-tabs">
                      <button type="button" class="pres-tab"
                              [class.on]="editPresentation().activeTab === 'BASE'"
                              (click)="setPresentationTab('BASE')">Base</button>
                      @for (ch of accessChannels; track ch.code) {
                        <button type="button" class="pres-tab"
                                [class.on]="editPresentation().activeTab === ch.code"
                                [class.has-ov]="!!presentationOverrideSummary(ver, ch.code) || !!editPresentation().overrides[ch.code]?.enabled"
                                (click)="setPresentationTab(ch.code)"
                                [pTooltip]="ch.hint">{{ ch.label }}</button>
                      }
                    </div>

                    <div class="pres-toolbar">
                      <label class="check-row">
                        <input type="checkbox"
                               [ngModel]="editPresentation().jsonMode"
                               [ngModelOptions]="{standalone: true}"
                               (ngModelChange)="togglePresentationJsonMode($event)" />
                        Mode JSON
                      </label>
                      @if (editPresentation().activeTab !== 'BASE') {
                        <label class="check-row">
                          <input type="checkbox"
                                 [ngModel]="!!activePresentationLayer()?.enabled"
                                 [ngModelOptions]="{standalone: true}"
                                 (ngModelChange)="setChannelOverrideEnabled($event)" />
                          Personnaliser ce canal (sinon hérite de la base)
                        </label>
                      }
                      @if (!editPresentation().jsonMode && (editPresentation().activeTab === 'BASE' || activePresentationLayer()?.enabled)) {
                        <button pButton type="button" label="Section" icon="pi pi-plus"
                                class="p-button-outlined p-button-sm"
                                (click)="addPresentationSection()"></button>
                        <button pButton type="button" label="Modèle factures" icon="pi pi-replay"
                                class="p-button-text p-button-sm"
                                (click)="applyBillPresentationTemplate()"
                                pTooltip="Remplit favoris + catégories + liste"></button>
                      }
                    </div>

                    @if (editPresentation().activeTab !== 'BASE' && !activePresentationLayer()?.enabled && !editPresentation().jsonMode) {
                      <p class="muted pad-sm">Ce canal utilise la présentation <strong>Base</strong>.</p>
                    } @else if (editPresentation().jsonMode) {
                      <textarea pInputTextarea class="w-full mono" rows="14"
                                [ngModel]="editPresentation().jsonDraft"
                                [ngModelOptions]="{standalone: true}"
                                (ngModelChange)="patchPresentationJsonDraft($event)"
                                placeholder='{ "version": 1, "sections": [ … ] }'></textarea>
                    } @else {
                      <div class="field" style="max-width: 8rem; margin-bottom: 0.75rem;">
                        <label>version</label>
                        <input pInputText type="number" class="w-full mono"
                               [ngModel]="activePresentationLayer()?.version ?? 1"
                               [ngModelOptions]="{standalone: true}"
                               (ngModelChange)="patchActivePresentationLayer({ version: +$event || 1 })" />
                      </div>
                      @if (!(activePresentationLayer()?.sections?.length)) {
                        <p class="muted">Aucune section — ajoutez-en ou chargez le modèle factures.</p>
                      }
                      @for (sec of activePresentationLayer()?.sections || []; let i = $index; track i) {
                        <div class="pres-section">
                          <div class="pres-section-head">
                            <strong>Section {{ i + 1 }}</strong>
                            <div class="head-actions">
                              <button pButton type="button" icon="pi pi-arrow-up" class="p-button-text p-button-sm"
                                      [disabled]="i === 0" (click)="movePresentationSection(i, -1)"></button>
                              <button pButton type="button" icon="pi pi-arrow-down" class="p-button-text p-button-sm"
                                      [disabled]="i >= (activePresentationLayer()?.sections?.length || 0) - 1"
                                      (click)="movePresentationSection(i, 1)"></button>
                              <button pButton type="button" icon="pi pi-trash" class="p-button-text p-button-danger p-button-sm"
                                      (click)="removePresentationSection(i)"></button>
                            </div>
                          </div>
                          <div class="form-grid">
                            <div class="field">
                              <label>id</label>
                              <input pInputText class="w-full mono" [ngModel]="sec.id"
                                     [ngModelOptions]="{standalone: true}"
                                     (ngModelChange)="patchPresentationSection(i, { id: $event })" />
                            </div>
                            <div class="field">
                              <label>title</label>
                              <input pInputText class="w-full" [ngModel]="sec.title"
                                     [ngModelOptions]="{standalone: true}"
                                     (ngModelChange)="patchPresentationSection(i, { title: $event })" />
                            </div>
                            <div class="field">
                              <label>displayKind</label>
                              <p-dropdown [options]="displayKindOptions" optionLabel="label" optionValue="value"
                                          [ngModel]="sec.displayKind" [ngModelOptions]="{standalone: true}"
                                          (ngModelChange)="patchPresentationSection(i, { displayKind: $event })"
                                          styleClass="w-full" />
                            </div>
                            <div class="field">
                              <label>dataSource</label>
                              <p-dropdown [options]="dataSourceOptions" optionLabel="label" optionValue="value"
                                          [ngModel]="sec.dataSource" [ngModelOptions]="{standalone: true}"
                                          (ngModelChange)="patchPresentationSection(i, { dataSource: $event })"
                                          [editable]="true"
                                          styleClass="w-full" />
                            </div>
                            <div class="field">
                              <label>origin</label>
                              <p-dropdown [options]="originOptions" optionLabel="label" optionValue="value"
                                          [ngModel]="sec.origin" [ngModelOptions]="{standalone: true}"
                                          (ngModelChange)="patchPresentationSection(i, { origin: $event })"
                                          styleClass="w-full" />
                            </div>
                            <div class="field">
                              <label>bindsTo</label>
                              <input pInputText class="w-full mono" [ngModel]="sec.bindsTo"
                                     [ngModelOptions]="{standalone: true}"
                                     (ngModelChange)="patchPresentationSection(i, { bindsTo: $event })"
                                     placeholder="billers" />
                            </div>
                            <div class="field check-pair">
                              <label class="check-row">
                                <input type="checkbox" [ngModel]="sec.searchable" [ngModelOptions]="{standalone: true}"
                                       (ngModelChange)="patchPresentationSection(i, { searchable: $event })" />
                                searchable
                              </label>
                              <label class="check-row">
                                <input type="checkbox" [ngModel]="sec.pageable" [ngModelOptions]="{standalone: true}"
                                       (ngModelChange)="patchPresentationSection(i, { pageable: $event })" />
                                pageable
                              </label>
                            </div>
                            @if (sec.dataSource === 'STATIC_ITEMS' || sec.dataSource === 'BILL_FAVORITES') {
                              <div class="field span-2">
                                <label>items (JSON)</label>
                                <textarea pInputTextarea class="w-full mono" rows="4"
                                          [ngModel]="sec.itemsJson" [ngModelOptions]="{standalone: true}"
                                          (ngModelChange)="patchPresentationSection(i, { itemsJson: $event })"></textarea>
                              </div>
                            }
                            <div class="field span-2">
                              <label>params (JSON) — {{ paramsHint(sec) }}</label>
                              <textarea pInputTextarea class="w-full mono" rows="3"
                                        [ngModel]="sec.paramsJson" [ngModelOptions]="{standalone: true}"
                                        (ngModelChange)="patchPresentationSection(i, { paramsJson: $event })"
                                        [placeholder]="paramsPlaceholder(sec)"></textarea>
                            </div>
                          </div>
                        </div>
                      }
                    }
                  } @else {
                    <div class="pres-readonly">
                      <div>
                        <dt class="pres-dt">Base</dt>
                        <dd>{{ presentationSummary(ver, null) }}</dd>
                      </div>
                      @for (ch of accessChannels; track ch.code) {
                        <div>
                          <dt class="pres-dt">{{ ch.label }}</dt>
                          <dd>{{ presentationOverrideSummary(ver, ch.code) || 'Hérite de la base' }}</dd>
                        </div>
                      }
                    </div>
                  }
                </section>

                <section class="card">
                  <div class="section-head">
                    <div>
                      <h3>Approbation</h3>
                      <p class="meta">
                        Policy produit → DMN →
                        <a routerLink="/admin/approval-profiles">profils d'approbation</a>
                      </p>
                    </div>
                    <div class="head-actions">
                      @if (editingApproval() && canEditCasting(ver)) {
                        <button pButton type="button" label="Annuler" class="p-button-text p-button-sm"
                                (click)="cancelApprovalEdit(ver)"></button>
                        <button pButton type="button" label="Enregistrer" icon="pi pi-check" class="p-button-sm"
                                [disabled]="busy()" (click)="saveApprovalPolicy(ver)"></button>
                      } @else {
                        <button pButton type="button" label="Modifier" icon="pi pi-pencil"
                                class="p-button-outlined p-button-sm"
                                [disabled]="busy() || ver.status === 'ARCHIVED'"
                                (click)="beginModifyApproval(ver)"
                                [pTooltip]="canEditCasting(ver)
                                  ? 'Activer / paramétrer approvalPolicy'
                                  : 'Crée une nouvelle version brouillon (version publiée figée)'"
                        ></button>
                      }
                    </div>
                  </div>

                  @if (editingApproval() && canEditCasting(ver)) {
                    <div class="form-grid approval-grid">
                      <div class="field span-2 check-row">
                        <label>
                          <input type="checkbox" [ngModel]="editApproval().enabled"
                                 [ngModelOptions]="{standalone: true}"
                                 (ngModelChange)="patchApproval({ enabled: $event })" />
                          Activer l'approbation multi-niveaux
                        </label>
                      </div>
                      <div class="field">
                        <label>Code policy</label>
                        <input pInputText class="w-full mono" [ngModel]="editApproval().code"
                               [ngModelOptions]="{standalone: true}"
                               (ngModelChange)="patchApproval({ code: $event })" />
                      </div>
                      <div class="field">
                        <label>Clé DMN</label>
                        <input pInputText class="w-full mono" [ngModel]="editApproval().dmnDecisionKey"
                               [ngModelOptions]="{standalone: true}"
                               (ngModelChange)="patchApproval({ dmnDecisionKey: $event })"
                               placeholder="decision_TransferApproval" />
                      </div>
                      <div class="field">
                        <label>Stratégie</label>
                        <input pInputText class="w-full mono" [ngModel]="editApproval().strategy"
                               [ngModelOptions]="{standalone: true}"
                               (ngModelChange)="patchApproval({ strategy: $event })"
                               placeholder="SEQUENTIAL" />
                      </div>
                      <div class="field">
                        <label>Formulaire approbation (formKey)</label>
                        <input pInputText class="w-full mono" [ngModel]="editApproval().formKey"
                               [ngModelOptions]="{standalone: true}"
                               (ngModelChange)="patchApproval({ formKey: $event })" />
                      </div>
                      <div class="field span-2 check-row">
                        <label>
                          <input type="checkbox" [ngModel]="editApproval().initiatorCanApprove"
                                 [ngModelOptions]="{standalone: true}"
                                 (ngModelChange)="patchApproval({ initiatorCanApprove: $event })" />
                          L'initiateur peut s'auto-approuver
                        </label>
                      </div>
                      <div class="field span-2 check-row">
                        <label>
                          <input type="checkbox" [ngModel]="editApproval().sameUserAcrossLevelsAllowed"
                                 [ngModelOptions]="{standalone: true}"
                                 (ngModelChange)="patchApproval({ sameUserAcrossLevelsAllowed: $event })" />
                          Même utilisateur autorisé sur plusieurs niveaux
                        </label>
                      </div>
                      <div class="field span-2">
                        <label>Champs critiques (1 par ligne) — resoumission si modifié</label>
                        <textarea pInputTextarea rows="4" class="w-full mono"
                                  [ngModel]="editApproval().criticalFieldsText"
                                  [ngModelOptions]="{standalone: true}"
                                  (ngModelChange)="patchApproval({ criticalFieldsText: $event })"></textarea>
                      </div>
                      <p class="hint span-2">
                        Les seuils montant → code profil sont dans le DMN.
                        Les rôles validateurs se configurent dans Profils d'approbation.
                      </p>
                    </div>
                  } @else {
                    @if (approvalPolicyOf(ver); as pol) {
                      <div class="approval-summary">
                        <p-tag [value]="pol.enabled ? 'ACTIVÉE' : 'DÉSACTIVÉE'"
                               [severity]="pol.enabled ? 'success' : 'secondary'" />
                        <ul class="chip-list">
                          <li><strong>Code</strong><span><code>{{ pol.code || '—' }}</code></span></li>
                          <li><strong>DMN</strong><span><code>{{ pol.dmnDecisionKey || '—' }}</code></span></li>
                          <li><strong>Stratégie</strong><span>{{ pol.strategy || 'SEQUENTIAL' }}</span></li>
                          <li><strong>Formulaire</strong><span><code>{{ pol.formKey || '—' }}</code></span></li>
                          <li>
                            <strong>Règles</strong>
                            <span>
                              auto-approbation {{ pol.initiatorCanApprove ? 'oui' : 'non' }}
                              · multi-niveaux même user {{ pol.sameUserAcrossLevelsAllowed ? 'oui' : 'non' }}
                            </span>
                          </li>
                          <li>
                            <strong>Champs critiques</strong>
                            <span>{{ (pol.criticalFields || []).join(', ') || '—' }}</span>
                          </li>
                        </ul>
                      </div>
                    } @else {
                      <p class="muted">Aucune policy — cliquez Modifier pour en créer une.</p>
                    }
                    @if (ver.status === 'PUBLISHED') {
                      <p class="hint locked">Version publiée figée — cliquez Modifier pour créer un brouillon.</p>
                    }
                  }
                </section>
              }

              @if (workflowBinding(); as bind) {
                <section class="card bpmn-ok">
                  <div class="section-head">
                    <h3>Binding RapidBPMN · {{ bind.workflowKey }}</h3>
                    <button pButton type="button" icon="pi pi-times" class="p-button-text p-button-sm"
                            (click)="workflowBinding.set(null)"></button>
                  </div>
                  <p class="meta">
                    {{ bind.processName || bind.workflowKey }}
                    @if (bind.processVersion != null) {
                      · def v{{ bind.processVersion }}
                    }
                    · {{ bind.deployed ? 'déployé' : 'non déployé' }}
                  </p>
                </section>
              }
            }
          } @else {
            <section class="card empty-state">
              <h2>Sélectionnez un produit</h2>
              <p>Ou créez-en un pour configurer rôles, participants et RapidBPMN.</p>
              <button pButton type="button" label="Nouveau produit" icon="pi pi-plus" (click)="startNewService()"></button>
            </section>
          }
        </main>
      </div>
    </div>

    <ng-template #rolesEditor>
      <div class="editor-block">
        <div class="editor-head">
          <div>
            <h4>Rôles du produit</h4>
            <p class="hint">Référentiel plateforme — sélectionnez SOURCE, DESTINATION, PAYER…</p>
          </div>
          <button pButton type="button" icon="pi pi-plus" label="Ajouter" class="p-button-outlined p-button-sm"
                  [disabled]="!availableRoleCodes().length"
                  [pTooltip]="availableRoleCodes().length ? '' : 'Tous les rôles plateforme sont déjà déclarés'"
                  (click)="addRole()"></button>
        </div>
        @if (!editRoles().length) {
          <p class="muted">Aucun rôle — ajoutez SOURCE / DESTINATION par exemple.</p>
        }
        @for (role of editRoles(); track i; let i = $index) {
          <div class="role-row">
            <p-dropdown
              [options]="roleCodeOptionsForRow(i)"
              optionLabel="label"
              optionValue="value"
              [ngModel]="role.code"
              [ngModelOptions]="{standalone: true}"
              (ngModelChange)="onRoleCodeChange(i, $event)"
              placeholder="Rôle"
              styleClass="w-full"
              [filter]="true"
            />
            <input pInputText [placeholder]="'Libellé (' + defaultLanguage() + ')'"
                   [ngModel]="role.label || ''" [ngModelOptions]="{standalone: true}"
                   (ngModelChange)="patchRole(i, { label: $event })" />
            <p-dropdown
              [options]="selectionOptions"
              optionLabel="label"
              optionValue="value"
              [ngModel]="role.selection || 'USER'"
              [ngModelOptions]="{standalone: true}"
              (ngModelChange)="patchRole(i, { selection: $event })"
              styleClass="w-full"
            />
            <p-dropdown
              [options]="cardinalityOptions"
              optionLabel="label"
              optionValue="value"
              [ngModel]="role.cardinality || 'ONE_REQUIRED'"
              [ngModelOptions]="{standalone: true}"
              (ngModelChange)="patchRole(i, { cardinality: $event })"
              styleClass="w-full"
            />
            <button pButton type="button" icon="pi pi-trash" class="p-button-text p-button-danger p-button-sm"
                    (click)="removeRole(i)" pTooltip="Supprimer le rôle"></button>
          </div>
          @if (otherLanguages().length && role.code) {
            <div class="role-i18n-row">
              @for (lang of otherLanguages(); track lang.code) {
                <input pInputText [placeholder]="'Libellé (' + lang.code + ')'"
                       [ngModel]="localizedRoleLabel(role.code, lang.code)" [ngModelOptions]="{standalone: true}"
                       (ngModelChange)="patchRoleLabelI18n(role.code, lang.code, $event)" />
              }
            </div>
          }
        }
      </div>
    </ng-template>

    <ng-template #participantsEditor>
      <div class="editor-block">
        <div class="editor-head">
          <div>
            <h4>Participants (providers)</h4>
            <p class="hint">Sélectionnez les providers éligibles pour chaque rôle, puis le défaut.</p>
          </div>
        </div>
        @if (providersLoading()) {
          <div class="center"><p-progressSpinner strokeWidth="4" [style]="{ width: '28px', height: '28px' }" /></div>
        } @else if (!editRoles().length) {
          <p class="muted">Définissez d’abord au moins un rôle.</p>
        } @else {
          @for (role of editRoles(); track i; let i = $index) {
            <div class="participant-card">
              <div class="pc-head">
                <strong>{{ role.label || role.code || ('Rôle ' + (i + 1)) }}</strong>
                <code>{{ role.code || '—' }}</code>
              </div>
              <p-multiSelect
                [options]="providerOptions()"
                optionLabel="label"
                optionValue="value"
                [ngModel]="selectedProvidersForRole(role.code)"
                [ngModelOptions]="{standalone: true}"
                (ngModelChange)="setProvidersForRole(role.code, $event || [])"
                placeholder="Choisir des providers"
                display="chip"
                [filter]="true"
                [showClear]="true"
                styleClass="w-full"
                appendTo="body"
                [disabled]="!(role.code || '').trim()"
              />
              @if (selectedProvidersForRole(role.code).length) {
                <div class="default-row">
                  <span>Défaut</span>
                  <div class="default-choices">
                    @for (code of selectedProvidersForRole(role.code); track code) {
                      <label class="radio-pill">
                        <input
                          type="radio"
                          [name]="'default-' + i + '-' + (role.code || 'role')"
                          [checked]="isDefaultProvider(role.code, code)"
                          (change)="setDefaultProvider(role.code, code)"
                        />
                        {{ providerLabel(code) }}
                      </label>
                    }
                  </div>
                </div>
              }
            </div>
          }
        }
      </div>
    </ng-template>

    <ng-template #bindingsEditor>
      <div class="editor-block">
        <div class="editor-head">
          <div>
            <h4>Bindings (écrans & invokes)</h4>
            <p class="hint">
              UI partagée → formKey Form.io · UI par provider → formKey vide
              (nomenclature <code>product-role-provider</code>) ·
              Facture → écran <strong>BillerProduct</strong> (LOOKUP / CONFIRM / AMOUNT).
            </p>
          </div>
          <button pButton type="button" icon="pi pi-plus" label="Ajouter" class="p-button-outlined p-button-sm"
                  (click)="addBinding()"></button>
        </div>
        @if (!editBindings().length) {
          <p class="muted">Aucun binding — liez les activityId BPMN aux écrans / opérations.</p>
        }
        @for (b of editBindings(); track i; let i = $index) {
          <div class="binding-card" [class.ui-biller]="b.kind === 'UI' && b.formSource === 'BILLER_PRODUCT'">
            <div class="binding-row">
              <div class="field">
                <label>Step (activityId)</label>
                <input pInputText [ngModel]="b.step" [ngModelOptions]="{standalone: true}"
                       (ngModelChange)="patchBinding(i, { step: $event })" class="w-full mono" placeholder="CAPTURE_DESTINATION" />
              </div>
              <div class="field">
                <label>Kind</label>
                <p-dropdown
                  [options]="bindingKindOptionsForRow(i)"
                  optionLabel="label"
                  optionValue="value"
                  [ngModel]="b.kind || 'UI'"
                  [ngModelOptions]="{standalone: true}"
                  (ngModelChange)="onBindingKindChange(i, $event)"
                  styleClass="w-full"
                />
              </div>
              <div class="field">
                <label>Rôle acteur</label>
                <p-dropdown
                  [options]="actorRoleOptions()"
                  optionLabel="label"
                  optionValue="value"
                  [ngModel]="b.actorRole || null"
                  [ngModelOptions]="{standalone: true}"
                  (ngModelChange)="patchBinding(i, { actorRole: $event || undefined, providerCode: undefined })"
                  [showClear]="true"
                  placeholder="—"
                  styleClass="w-full"
                />
              </div>
              <div class="field">
                <label>Provider (override)</label>
                <p-dropdown
                  [options]="providerOptionsForBinding(b.actorRole)"
                  optionLabel="label"
                  optionValue="value"
                  [ngModel]="b.providerCode || null"
                  [ngModelOptions]="{standalone: true}"
                  (ngModelChange)="patchBinding(i, { providerCode: $event || undefined })"
                  [showClear]="true"
                  placeholder="Tous les providers du rôle"
                  styleClass="w-full"
                  [filter]="true"
                />
              </div>
              <button pButton type="button" icon="pi pi-trash" class="p-button-text p-button-danger p-button-sm bind-del"
                      (click)="removeBinding(i)" pTooltip="Supprimer"></button>
            </div>

            @if (b.kind === 'UI') {
              <div class="binding-row ui-row">
                <div class="field">
                  <label>Source d’écran</label>
                  <p-dropdown
                    [options]="formSourceOptions"
                    optionLabel="label"
                    optionValue="value"
                    [ngModel]="b.formSource || 'FORM_KEY'"
                    [ngModelOptions]="{standalone: true}"
                    (ngModelChange)="onFormSourceChange(i, $event)"
                    styleClass="w-full"
                  />
                </div>
                @if ((b.formSource || 'FORM_KEY') === 'FORM_KEY') {
                  <div class="field span-grow">
                    <label>formKey Form.io (optionnel si multi-provider)</label>
                    <div class="formkey-row">
                      <input pInputText [ngModel]="b.formKey || ''" [ngModelOptions]="{standalone: true}"
                             (ngModelChange)="patchBinding(i, { formKey: $event })"
                             class="w-full mono"
                             placeholder="vide = nomenclature product-role-provider" />
                      @if ((b.formKey || '').trim()) {
                        <a pButton
                           class="p-button-outlined p-button-sm"
                           [routerLink]="['/designer']"
                           [queryParams]="{ module: 'payment-admin-module', form: (b.formKey || '').trim() }"
                           label="Concepteur"
                           icon="pi pi-pencil"
                           pTooltip="Ouvrir dans le concepteur Form.io"></a>
                      }
                    </div>
                    <p class="hint biller-hint nomenclature-hint">
                      <strong>Écran partagé</strong> : renseignez un formKey (ex. <code>account-transfer-source</code>).<br />
                      <strong>UI par provider</strong> : laissez vide →
                      <code>{{ conventionPreview(b) }}</code>
                      @if (conventionExamples(b).length) {
                        <span> — ex. {{ conventionExamples(b).join(', ') }}</span>
                      }
                    </p>
                  </div>
                } @else {
                  <div class="field">
                    <label>Écran BillerProduct</label>
                    <p-dropdown
                      [options]="billerScreenOptions"
                      optionLabel="label"
                      optionValue="value"
                      [ngModel]="b.billerScreen || 'LOOKUP'"
                      [ngModelOptions]="{standalone: true}"
                      (ngModelChange)="patchBinding(i, { billerScreen: $event })"
                      styleClass="w-full"
                    />
                  </div>
                  <div class="field span-grow">
                    <p class="hint biller-hint">
                      Champs lus sur le produit facturier au runtime
                      (<code>lookupFields</code> / <code>confirmFields</code> / <code>amountFields</code>
                      — pas de formKey Form.io.
                    </p>
                  </div>
                }
              </div>
            } @else if (b.kind === 'CAPABILITY') {
              <div class="binding-row">
                <div class="field span-grow">
                  <label>Capability catalogue</label>
                  <input pInputText [ngModel]="b.capability || ''" [ngModelOptions]="{standalone: true}"
                         (ngModelChange)="patchBinding(i, { capability: $event })"
                         class="w-full mono" placeholder="VALIDATE_AMOUNT / GET_QUOTE / LOOKUP_REMITTANCE…" />
                </div>
              </div>
            } @else if (b.kind === 'INVOKE' || b.kind === 'SOFT') {
              <div class="binding-row">
                <div class="field span-grow">
                  <label>Opération provider</label>
                  <input pInputText [ngModel]="b.operation || ''" [ngModelOptions]="{standalone: true}"
                         (ngModelChange)="patchBinding(i, { operation: $event })"
                         class="w-full mono" placeholder="TRANSFER_EXECUTE / LOOKUP_BILL / GENERATE_OTP" />
                </div>
              </div>
            }
          </div>
        }
        <details class="advanced">
          <summary>JSON brut (avancé)</summary>
          <textarea pInputTextarea [ngModel]="bindingsJsonDraft()" (ngModelChange)="onBindingsJsonDraftChange($event)"
                    [ngModelOptions]="{standalone: true}" rows="6" class="w-full mono"></textarea>
        </details>
      </div>
    </ng-template>
  `,
  styles: `
    .page {
      max-width: 1280px; margin: 0 auto; padding: 1.5rem 1.25rem 3rem;
      color: #0f172a;
    }
    .hero {
      display: flex; justify-content: space-between; gap: 1.5rem; align-items: flex-end;
      margin-bottom: 1.25rem; padding-bottom: 1.25rem; border-bottom: 1px solid #e2e8f0;
    }
    .eyebrow { margin: 0 0 0.35rem; font-size: 0.75rem; letter-spacing: 0.08em; text-transform: uppercase; color: #64748b; }
    .hero h1 { margin: 0; font-size: 1.85rem; font-weight: 700; letter-spacing: -0.02em; }
    .lede { margin: 0.5rem 0 0; max-width: 42rem; color: #475569; line-height: 1.5; }
    .hero-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; }
    .banner {
      padding: 0.75rem 1rem; border-radius: 10px; margin-bottom: 1rem; font-size: 0.9rem;
    }
    .banner.err { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; }
    .banner.ok { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }

    .shell {
      display: grid; grid-template-columns: minmax(240px, 300px) 1fr; gap: 1.25rem; align-items: start;
    }
    .rail {
      background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; overflow: hidden;
      position: sticky; top: 1rem;
    }
    .rail-head {
      display: flex; justify-content: space-between; align-items: center;
      padding: 0.85rem 1rem; border-bottom: 1px solid #e2e8f0; font-weight: 600; font-size: 0.85rem;
    }
    .count {
      background: #f1f5f9; color: #475569; border-radius: 999px; padding: 0.1rem 0.55rem; font-size: 0.75rem;
    }
    .rail-filters { padding: 0.65rem 0.75rem; border-bottom: 1px solid #e2e8f0; display: grid; gap: 0.5rem; }
    .search-box { position: relative; }
    .search-box .pi-search {
      position: absolute; left: 0.7rem; top: 50%; transform: translateY(-50%); color: #94a3b8; font-size: 0.8rem;
    }
    .search-box input { padding-left: 2rem; padding-right: 2rem; font-size: 0.85rem; }
    .search-clear {
      position: absolute; right: 0.4rem; top: 50%; transform: translateY(-50%);
      border: none; background: transparent; color: #94a3b8; cursor: pointer; padding: 0.25rem; line-height: 1;
    }
    .search-clear:hover { color: #475569; }
    .status-chips { display: flex; flex-wrap: wrap; gap: 0.3rem; }
    .chip {
      border: 1px solid #e2e8f0; background: #fff; color: #475569; border-radius: 999px;
      padding: 0.15rem 0.55rem; font-size: 0.7rem; font-weight: 600; cursor: pointer;
    }
    .chip:hover { background: #f8fafc; }
    .chip.on { background: #eff6ff; border-color: #93c5fd; color: #1d4ed8; }
    .chip-count { margin-left: 0.2rem; color: #94a3b8; font-weight: 500; }
    .chip.on .chip-count { color: #3b82f6; }
    .link-btn {
      border: none; background: none; padding: 0; margin-left: 0.25rem;
      color: #2563eb; cursor: pointer; font: inherit; text-decoration: underline;
    }
    .product-list { list-style: none; margin: 0; padding: 0.4rem; max-height: calc(100vh - 18rem); overflow: auto; }
    .product-list li {
      padding: 0.75rem 0.85rem; border-radius: 10px; cursor: pointer; border: 1px solid transparent;
      transition: background 0.15s, border-color 0.15s;
    }
    .product-list li:hover { background: #f8fafc; }
    .product-list li.active { background: #eff6ff; border-color: #bfdbfe; }
    .prod-top { display: flex; justify-content: space-between; gap: 0.5rem; align-items: center; }
    .prod-top strong { font-size: 0.92rem; }
    .prod-meta { display: block; margin-top: 0.25rem; color: #64748b; font-size: 0.78rem; }

    .canvas { display: flex; flex-direction: column; gap: 1rem; min-width: 0; }
    .card {
      background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 1.25rem 1.35rem;
    }
    .head-card { display: flex; justify-content: space-between; gap: 1rem; align-items: flex-start; }
    .head-card h2 { margin: 0.15rem 0 0.35rem; font-size: 1.45rem; }
    .head-actions { display: flex; gap: 0.4rem; flex-wrap: wrap; }
    .section-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; margin-bottom: 0.85rem; }
    .section-head h3, .card > h2, .card > h3 { margin: 0 0 0.35rem; }
    h4 { margin: 0 0 0.5rem; font-size: 0.95rem; color: #1e293b; }
    .meta { margin: 0; color: #64748b; font-size: 0.85rem; }
    .muted { color: #94a3b8; font-size: 0.9rem; }
    .pad { padding: 1rem; }
    .center { display: grid; place-items: center; padding: 1.5rem; }

    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem 1rem; }
    .journey-dl {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem 1rem;
      margin: 0;
    }
    .journey-dl .span-2 { grid-column: 1 / -1; }
    .journey-dl dt {
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--text-color-secondary, #64748b);
      margin: 0 0 0.15rem;
    }
    .journey-dl dd {
      margin: 0;
      display: flex;
      flex-wrap: wrap;
      gap: 0.35rem;
      align-items: center;
    }
    .field { display: flex; flex-direction: column; gap: 0.3rem; }
    .field.span-2, .actions.span-2 { grid-column: 1 / -1; }
    .field label { font-size: 0.78rem; color: #475569; font-weight: 500; }
    .actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem; }
    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 0.8rem; }
    .hint { margin: 0; color: #64748b; font-size: 0.8rem; }
    .hint.span-2 { grid-column: 1 / -1; }
    .check-row label { display: flex; align-items: center; gap: 0.5rem; font-size: 0.9rem; color: #334155; cursor: pointer; }
    .check-row input[type='checkbox'] { width: 1rem; height: 1rem; }
    .approval-summary { display: flex; flex-direction: column; gap: 0.75rem; align-items: flex-start; }
    .approval-grid { margin-top: 0.25rem; }

    .version-grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 0.75rem;
    }
    .version-tile {
      text-align: left; border: 1px solid #e2e8f0; border-radius: 12px; padding: 0.9rem;
      background: #fafbfc; cursor: pointer; display: flex; flex-direction: column; gap: 0.35rem;
      outline: none;
    }
    .version-tile:focus-visible { box-shadow: 0 0 0 2px #93c5fd; }
    .version-tile:hover { border-color: #cbd5e1; background: #fff; }
    .version-tile.active { border-color: #93c5fd; background: #eff6ff; }
    .vt-top { display: flex; justify-content: space-between; align-items: center; }
    .vt-meta { font-size: 0.75rem; color: #64748b; }
    .vt-actions { display: flex; gap: 0.35rem; flex-wrap: wrap; margin-top: 0.35rem; }

    .cast-cols { display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; margin-bottom: 1.25rem; }
    .chip-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.5rem; }
    .chip-list li {
      padding: 0.65rem 0.8rem; border-radius: 10px; background: #f8fafc; border: 1px solid #e2e8f0;
    }
    .chip-list span { display: block; margin-top: 0.2rem; font-size: 0.78rem; color: #64748b; }
    .part-group { margin-bottom: 0.75rem; }
    .part-role { font-size: 0.75rem; font-weight: 600; color: #475569; margin-bottom: 0.35rem; letter-spacing: 0.04em; }
    .part-providers { display: flex; flex-wrap: wrap; gap: 0.35rem; }
    .provider-pill {
      display: inline-flex; align-items: center; gap: 0.35rem;
      padding: 0.3rem 0.65rem; border-radius: 999px; background: #f1f5f9; font-size: 0.8rem;
      border: 1px solid #e2e8f0;
    }
    .provider-pill.def { background: #ecfdf5; border-color: #a7f3d0; color: #065f46; }
    .provider-pill em { font-style: normal; font-size: 0.7rem; opacity: 0.8; }

    .bind-table-wrap { overflow: auto; border: 1px solid #e2e8f0; border-radius: 10px; }
    .data-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
    .data-table th, .data-table td { padding: 0.55rem 0.75rem; border-bottom: 1px solid #e2e8f0; text-align: left; }
    .data-table th { background: #f8fafc; color: #475569; font-weight: 600; font-size: 0.75rem; }
    .data-table tr:last-child td { border-bottom: 0; }

    .draft-box {
      margin-top: 0.5rem;
      display: flex; flex-direction: column; gap: 1rem;
    }
    .draft-box.open { margin-top: 0; }
    .messaging-card {
      border-color: #93c5fd;
      box-shadow: 0 0 0 1px #dbeafe;
    }
    .i18n-matrix {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      margin-bottom: 0.75rem;
      overflow-x: auto;
    }
    .i18n-matrix-head,
    .i18n-matrix-row {
      display: grid;
      grid-template-columns: minmax(7.5rem, 9.5rem) repeat(var(--i18n-cols, 2), minmax(11rem, 1fr));
      gap: 0.55rem 0.75rem;
      align-items: start;
    }
    .i18n-matrix-head {
      padding-bottom: 0.35rem;
      border-bottom: 1px solid #e2e8f0;
    }
    .i18n-col-head {
      font-size: 0.78rem;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: #475569;
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
    }
    .i18n-col-head.default { color: #1d4ed8; }
    .i18n-col-head em {
      font-style: normal;
      font-size: 0.65rem;
      font-weight: 700;
      text-transform: none;
      letter-spacing: 0;
      padding: 0.1rem 0.35rem;
      border-radius: 999px;
      background: #dbeafe;
      color: #1e40af;
    }
    .i18n-row-label {
      display: flex;
      flex-direction: column;
      gap: 0.1rem;
      padding-top: 0.45rem;
      font-size: 0.82rem;
      color: #0f172a;
    }
    .i18n-row-label small { color: #94a3b8; font-size: 0.7rem; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
    .i18n-col-cell { min-width: 0; }
    .notif-card {
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 0.85rem;
      background: #fff;
      margin-bottom: 0.75rem;
    }
    .notif-meta {
      display: grid;
      grid-template-columns: 1fr 1fr auto;
      gap: 0.65rem;
      align-items: end;
      margin-bottom: 0.85rem;
    }
    .notif-meta .field { display: flex; flex-direction: column; gap: 0.25rem; }
    .notif-meta .field label { font-size: 0.72rem; color: #64748b; font-weight: 600; }
    .notif-del { margin-bottom: 0.15rem; }
    .hint.locked {
      margin-top: 1rem; padding: 0.65rem 0.85rem; border-radius: 8px;
      background: #fffbeb; border: 1px solid #fde68a; color: #92400e;
    }
    .pres-tabs {
      display: flex; flex-wrap: wrap; gap: 0.4rem; margin-bottom: 0.85rem;
    }
    .pres-tab {
      border: 1px solid #e2e8f0; background: #fff; border-radius: 999px;
      padding: 0.4rem 0.85rem; font-size: 0.8rem; font-weight: 600; cursor: pointer; color: #334155;
    }
    .pres-tab.on { background: #1d4ed8; border-color: #1d4ed8; color: #fff; }
    .pres-tab.has-ov:not(.on) { border-color: #93c5fd; background: #eff6ff; }
    .pres-toolbar {
      display: flex; flex-wrap: wrap; gap: 0.75rem 1rem; align-items: center; margin-bottom: 0.85rem;
    }
    .check-row {
      display: inline-flex; align-items: center; gap: 0.4rem; font-size: 0.85rem; color: #334155;
    }
    .check-pair { display: flex; flex-wrap: wrap; gap: 0.75rem; align-items: center; padding-top: 1.4rem; }
    .pres-section {
      border: 1px solid #e2e8f0; border-radius: 12px; padding: 0.85rem; margin-bottom: 0.75rem; background: #fafbfc;
    }
    .pres-section-head {
      display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.65rem;
    }
    .pres-readonly {
      display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.65rem 1rem; font-size: 0.88rem;
    }
    .pres-dt { font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.04em; color: #64748b; margin-bottom: 0.15rem; }
    .pad-sm { padding: 0.35rem 0; }
    .editor-block {
      border: 1px solid #e2e8f0; border-radius: 12px; padding: 1rem; background: #fafbfc;
    }
    .editor-head {
      display: flex; justify-content: space-between; gap: 1rem; align-items: flex-start; margin-bottom: 0.85rem;
    }
    .role-row {
      display: grid; grid-template-columns: 1.4fr 1.1fr 1.1fr 1fr auto; gap: 0.5rem; align-items: center;
      margin-bottom: 0.5rem;
    }
    .role-i18n-row {
      display: flex; flex-wrap: wrap; gap: 0.5rem; margin: -0.25rem 0 0.75rem 1.4rem;
    }
    .role-i18n-row input { flex: 1 1 12rem; }
    .participant-card {
      border: 1px solid #e2e8f0; border-radius: 10px; padding: 0.85rem; background: #fff; margin-bottom: 0.65rem;
    }
    .pc-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.55rem; }
    .binding-card {
      border: 1px solid #e2e8f0; border-radius: 10px; padding: 0.85rem; background: #fff; margin-bottom: 0.65rem;
    }
    .binding-card.ui-biller { border-color: #a7f3d0; background: linear-gradient(180deg, #f0fdf4, #fff); }
    .binding-row {
      display: grid; grid-template-columns: 1.2fr 0.8fr 1fr 1.1fr auto; gap: 0.5rem; align-items: end;
    }
    .binding-row.ui-row { grid-template-columns: 1.2fr 1fr 1.4fr; margin-top: 0.65rem; }
    .binding-row .field { display: flex; flex-direction: column; gap: 0.25rem; min-width: 0; }
    .binding-row .field label { font-size: 0.72rem; color: #64748b; font-weight: 500; }
    .binding-row .span-grow { grid-column: span 2; }
    .formkey-row {
      display: flex;
      gap: 0.5rem;
      align-items: center;
    }
    .formkey-row input { flex: 1; min-width: 0; }
    .bind-del { align-self: end; margin-bottom: 0.1rem; }
    .biller-hint { margin: 0.4rem 0 0; padding: 0.5rem 0.65rem; background: #ecfdf5; border-radius: 8px; border: 1px solid #a7f3d0; }
    .default-row { margin-top: 0.65rem; }
    .default-row > span { display: block; font-size: 0.75rem; color: #64748b; margin-bottom: 0.35rem; }
    .default-choices { display: flex; flex-wrap: wrap; gap: 0.4rem; }
    .radio-pill {
      display: inline-flex; align-items: center; gap: 0.35rem;
      padding: 0.35rem 0.7rem; border-radius: 999px; border: 1px solid #e2e8f0;
      background: #f8fafc; font-size: 0.8rem; cursor: pointer;
    }
    .radio-pill:has(input:checked) { background: #ecfdf5; border-color: #6ee7b7; }

    .advanced { margin-top: 0.5rem; }
    .advanced summary { cursor: pointer; color: #475569; font-size: 0.85rem; margin-bottom: 0.5rem; }
    .empty-state { text-align: center; padding: 3rem 1.5rem; }
    .empty-state h2 { margin: 0 0 0.5rem; }
    .empty-state p { margin: 0 0 1rem; color: #64748b; }
    .bpmn-ok { border-color: #bbf7d0; background: linear-gradient(180deg, #f0fdf4, #fff); }
    code { font-size: 0.82em; }

    @media (max-width: 960px) {
      .shell { grid-template-columns: 1fr; }
      .rail { position: static; }
      .cast-cols, .form-grid, .role-row, .binding-row, .binding-row.ui-row { grid-template-columns: 1fr; }
      .notif-meta { grid-template-columns: 1fr; }
      .i18n-matrix-head,
      .i18n-matrix-row { grid-template-columns: minmax(6.5rem, 8rem) repeat(var(--i18n-cols, 2), minmax(9rem, 1fr)); }
      .role-i18n-row { margin-left: 0; }
      .binding-row .span-grow { grid-column: auto; }
      .hero { flex-direction: column; align-items: flex-start; }
    }
  `
})
export class CatalogDeServiceAdminComponent implements OnInit {
  private readonly api = inject(CatalogDeServiceAdminService);
  private readonly providersApi = inject(ProviderService);
  private readonly i18nApi = inject(I18nApiService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(true);
  readonly versionsLoading = signal(false);
  readonly providersLoading = signal(false);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly info = signal<string | null>(null);

  readonly services = signal<CatalogDeService[]>([]);
  readonly productSearch = signal('');
  readonly productStatusFilter = signal<string | null>(null);
  readonly productFilterActive = computed(() => !!this.productSearch().trim() || !!this.productStatusFilter());
  /** Statuts présents dans le catalogue, avec leur effectif. */
  readonly productStatuses = computed(() => {
    const counts = new Map<string, number>();
    for (const s of this.services()) {
      counts.set(s.status, (counts.get(s.status) ?? 0) + 1);
    }
    return [...counts.entries()].map(([status, count]) => ({ status, count }));
  });
  /** Tous les mots saisis doivent apparaître (nom, code, catégorie, description, traductions). */
  readonly filteredServices = computed(() => {
    const terms = normalizeSearch(this.productSearch()).split(/\s+/).filter(Boolean);
    const status = this.productStatusFilter();
    return this.services().filter((s) => {
      if (status && s.status !== status) {
        return false;
      }
      if (!terms.length) {
        return true;
      }
      const haystack = normalizeSearch(
        [
          s.name,
          s.code,
          s.categoryCode,
          s.categoryName,
          s.description,
          s.currentVersion,
          ...Object.values(s.nameI18n ?? {}),
        ]
          .filter(Boolean)
          .join(' '),
      );
      return terms.every((t) => haystack.includes(t));
    });
  });
  readonly selected = signal<CatalogDeService | null>(null);
  readonly versions = signal<CatalogDeServiceVersion[]>([]);
  readonly selectedVersion = signal<CatalogDeServiceVersion | null>(null);
  readonly editingService = signal(false);
  readonly editingVersion = signal(false);
  readonly editingCasting = signal(false);
  readonly editingApproval = signal(false);
  readonly editingJourney = signal(false);
  readonly editingPresentation = signal(false);
  readonly editingMessaging = signal(false);
  readonly editApproval = signal<ApprovalPolicyEdit>(this.toApprovalEdit(null));
  readonly editJourney = signal<JourneyEdit>(this.toJourneyEdit(null));
  readonly editPresentation = signal<PresentationEdit>(this.emptyPresentationEdit());
  readonly editHistory = signal<HistoryEdit>({ ...DEFAULT_HISTORY });
  readonly editNotifications = signal<NotificationEdit[]>([]);
  /** Traductions history : clé → langue → texte. */
  readonly historyI18n = signal<Record<string, LocalizedText>>({});
  /** Traductions notifications : code → { title?, body? } → langue → texte. */
  readonly notificationI18n = signal<Record<string, { title?: LocalizedText; body?: LocalizedText }>>({});
  readonly channelHintOptions = CHANNEL_HINT_OPTIONS;
  readonly historyTitlePlaceholder = '{{kind}} · via {{agentDisplay}}';
  readonly historySubtitlePlaceholder = '{{statusLabel}} · {{when}}';
  readonly notificationBodyPlaceholder =
    'Votre retrait de {{amount}} {{currency}} chez {{agentDisplay}} est confirmé.';
  readonly accessChannels = ACCESS_CHANNELS;
  readonly accessChannelOptions = ACCESS_CHANNELS.map((c) => ({
    label: `${c.label} (${c.code})`,
    value: c.code,
  }));
  readonly productAudienceOptions = PRODUCT_AUDIENCES.map((a) => ({
    label: `${a.label} (${a.code})`,
    value: a.code,
  }));
  readonly displayKindOptions = DISPLAY_KIND_OPTIONS;
  readonly dataSourceOptions = DATA_SOURCE_OPTIONS;
  readonly originOptions = ORIGIN_OPTIONS;
  readonly workflowBinding = signal<CatalogWorkflowBinding | null>(null);
  readonly categoryOptions = signal<Array<{ label: string; value: string }>>([]);
  readonly processOptions = signal<Array<{ label: string; value: string }>>([]);
  readonly providers = signal<Provider[]>([]);
  readonly editRoles = signal<CatalogProductRole[]>([]);
  readonly editParticipants = signal<CatalogProductParticipant[]>([]);
  readonly editBindings = signal<CatalogProductBinding[]>([]);
  readonly bindingsJsonDraft = signal(JSON.stringify(DEFAULT_BINDINGS, null, 2));

  /** Langues déclarées par le gateway (GET /i18n/languages). */
  readonly languages = signal<SupportedLanguage[]>([]);
  readonly defaultLanguage = signal('fr');
  /** Langues à saisir en plus du libellé principal, déjà porté par le formulaire. */
  readonly otherLanguages = computed(() =>
    this.languages().filter(l => l.code !== this.defaultLanguage())
  );
  /** Langues pour la grille historique/messages : défaut en tête, puis les autres. */
  readonly messagingLangCodes = computed(() => {
    const def = this.defaultLanguage();
    const codes = this.languages().map(l => l.code).filter(Boolean);
    if (!codes.length) {
      return [def];
    }
    const rest = codes.filter(c => c !== def);
    return codes.includes(def) ? [def, ...rest] : codes;
  });
  readonly historyFields: Array<{
    key: keyof HistoryEdit;
    label: string;
    placeholder: string;
    mono?: boolean;
    multiline?: boolean;
  }> = [
    { key: 'kindLabel', label: "Type d'opération", placeholder: "Ex. Retrait d'espèces" },
    {
      key: 'titleTemplate',
      label: 'Template titre',
      placeholder: '{{kind}} · via {{agentDisplay}}',
      mono: true
    },
    {
      key: 'subtitleTemplate',
      label: 'Template sous-titre',
      placeholder: '{{statusLabel}} · {{when}}',
      mono: true
    }
  ];
  readonly nameI18n = signal<LocalizedText>({});
  readonly descriptionI18n = signal<LocalizedText>({});
  /** Libellés de rôle par code rôle puis par langue. */
  readonly roleLabelI18n = signal<Record<string, LocalizedText>>({});

  readonly selectionOptions = SELECTION_OPTIONS;
  readonly cardinalityOptions = CARDINALITY_OPTIONS;
  readonly roleCodeOptions = ROLE_CODE_OPTIONS;
  readonly bindingKindOptions = BINDING_KIND_OPTIONS;
  readonly formSourceOptions = FORM_SOURCE_OPTIONS;
  readonly billerScreenOptions = BILLER_SCREEN_OPTIONS;

  readonly availableRoleCodes = computed(() => {
    const used = new Set(this.editRoles().map(r => (r.code || '').toUpperCase()).filter(Boolean));
    return ROLE_CODE_OPTIONS.filter(o => !used.has(o.value));
  });

  readonly statusOptions = [
    { label: 'DRAFT', value: 'DRAFT' },
    { label: 'VALIDATED', value: 'VALIDATED' },
    { label: 'PUBLISHED', value: 'PUBLISHED' },
    { label: 'SUSPENDED', value: 'SUSPENDED' },
    { label: 'ARCHIVED', value: 'ARCHIVED' }
  ];

  readonly providerOptions = computed(() =>
    this.providers().map(p => ({
      label: `${p.libelle || p.code} (${p.code})`,
      value: p.code
    }))
  );

  /** Stable map role → provider codes (évite plantage MultiSelect / CD). */
  readonly providersByRole = computed(() => {
    const map: Record<string, string[]> = {};
    for (const p of this.editParticipants()) {
      const role = (p.role || '').trim().toUpperCase();
      const code = (p.providerCode || '').trim();
      if (!role || !code) {
        continue;
      }
      (map[role] ??= []).push(code);
    }
    return map;
  });

  readonly serviceForm = this.fb.nonNullable.group({
    code: ['', Validators.required],
    name: ['', Validators.required],
    description: [''],
    categoryCode: ['', Validators.required],
    status: ['DRAFT' as CatalogServiceStatus, Validators.required],
    initialVersion: ['1.0'],
    workflowKey: ['accountTransfer'],
    stepsText: [DEFAULT_STEPS.join('\n')],
    bindingsJson: [JSON.stringify(DEFAULT_BINDINGS, null, 2)]
  });

  readonly serviceTypeOptions = SERVICE_TYPE_CODES.map((value) => ({
    label: value === 'DEFAUT' ? 'DEFAUT (générique)' : value,
    value
  }));

  readonly versionForm = this.fb.nonNullable.group({
    version: ['', Validators.required],
    status: ['DRAFT' as CatalogServiceStatus, Validators.required],
    workflowKey: ['', Validators.required],
    serviceType: ['DEFAUT' as ServiceTypeCode, Validators.required],
    stepsText: [DEFAULT_STEPS.join('\n')],
    bindingsJson: [JSON.stringify(DEFAULT_BINDINGS, null, 2)]
  });

  ngOnInit(): void {
    this.loadLanguages();
    this.loadProviders();
    this.reload();
  }

  localizedName(lang: string): string {
    return this.nameI18n()[lang] ?? '';
  }

  patchNameI18n(lang: string, value: string): void {
    this.nameI18n.update(current => this.withLang(current, lang, value));
  }

  localizedDescription(lang: string): string {
    return this.descriptionI18n()[lang] ?? '';
  }

  patchDescriptionI18n(lang: string, value: string): void {
    this.descriptionI18n.update(current => this.withLang(current, lang, value));
  }

  localizedRoleLabel(roleCode: string, lang: string): string {
    return this.roleLabelI18n()[(roleCode || '').toUpperCase()]?.[lang] ?? '';
  }

  patchRoleLabelI18n(roleCode: string, lang: string, value: string): void {
    const code = (roleCode || '').toUpperCase();
    if (!code) {
      return;
    }
    this.roleLabelI18n.update(current => ({
      ...current,
      [code]: this.withLang(current[code] ?? {}, lang, value)
    }));
  }

  /** Traductions des libellés de rôle, au format attendu par l'API (chemin → langues). */
  private roleLabelTranslations(): ConfigurationTranslations | undefined {
    const out: ConfigurationTranslations = {};
    for (const role of this.editRoles()) {
      const code = (role.code || '').trim().toUpperCase();
      const byLang = code ? this.nonEmpty(this.roleLabelI18n()[code] ?? {}) : undefined;
      if (byLang) {
        out[`configuration.roles[${code}].label`] = byLang;
      }
    }
    return Object.keys(out).length ? out : undefined;
  }

  private nonEmpty(byLang: LocalizedText): LocalizedText | undefined {
    return Object.keys(byLang).length ? byLang : undefined;
  }

  private withLang(current: LocalizedText, lang: string, value: string): LocalizedText {
    const next = { ...current };
    if (value?.trim()) {
      next[lang] = value.trim();
    } else {
      delete next[lang];
    }
    return next;
  }

  private loadLanguages(): void {
    this.i18nApi.languages().subscribe({
      next: res => {
        this.languages.set(res.languages ?? []);
        if (res.defaultLanguage) {
          this.defaultLanguage.set(res.defaultLanguage);
        }
      },
      // Sans le référentiel, on reste sur la seule langue par défaut.
      error: () => this.languages.set([])
    });
  }

  toggleProductStatus(status: string): void {
    this.productStatusFilter.update((current) => (current === status ? null : status));
  }

  clearProductFilters(): void {
    this.productSearch.set('');
    this.productStatusFilter.set(null);
  }

  statusSeverity(status: string): 'success' | 'info' | 'warning' | 'danger' | 'secondary' {
    switch (status) {
      case 'PUBLISHED':
        return 'success';
      case 'DRAFT':
        return 'secondary';
      case 'VALIDATED':
        return 'info';
      case 'SUSPENDED':
        return 'warning';
      case 'ARCHIVED':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  kindSeverity(kind?: string): 'success' | 'info' | 'warning' | 'danger' | 'secondary' {
    switch (kind) {
      case 'UI':
        return 'info';
      case 'INVOKE':
        return 'warning';
      case 'WAIT':
        return 'secondary';
      default:
        return 'success';
    }
  }

  providerLabel(code: string): string {
    const p = this.providers().find(x => x.code === code);
    return p?.libelle ? `${p.libelle}` : code;
  }

  selectedProvidersForRole(roleCode: string): string[] {
    const role = (roleCode || '').trim().toUpperCase();
    if (!role) {
      return EMPTY_PROVIDER_CODES;
    }
    return this.providersByRole()[role] ?? EMPTY_PROVIDER_CODES;
  }

  isDefaultProvider(roleCode: string, providerCode: string): boolean {
    const role = (roleCode || '').trim().toUpperCase();
    return this.editParticipants().some(
      p =>
        p.role.toUpperCase() === role
        && p.providerCode === providerCode
        && !!p.default
    );
  }

  setProvidersForRole(roleCode: string, providerCodes: string[] | null | undefined): void {
    const role = (roleCode || '').trim().toUpperCase();
    if (!role) {
      return;
    }
    const codes = Array.isArray(providerCodes)
      ? [...new Set(providerCodes.filter(c => !!c && String(c).trim()))]
      : [];
    const others = this.editParticipants().filter(p => p.role.toUpperCase() !== role);
    const previous = this.editParticipants().filter(p => p.role.toUpperCase() === role);
    const prevDefault = previous.find(p => p.default)?.providerCode;
    const next: CatalogProductParticipant[] = codes.map((code, idx) => ({
      role,
      providerCode: String(code).trim(),
      default: prevDefault ? code === prevDefault : idx === 0
    }));
    if (next.length && !next.some(p => p.default)) {
      next[0].default = true;
    }
    this.editParticipants.set([...others, ...next]);
  }

  setDefaultProvider(roleCode: string, providerCode: string): void {
    const role = roleCode.toUpperCase();
    this.editParticipants.set(
      this.editParticipants().map(p =>
        p.role.toUpperCase() === role
          ? { ...p, default: p.providerCode === providerCode }
          : p
      )
    );
  }

  addBinding(): void {
    const defaultRole = this.editRoles()[0]?.code || 'SOURCE';
    this.editBindings.update(list => [
      ...list,
      {
        step: 'NEW_STEP',
        kind: 'UI',
        actorRole: defaultRole,
        formSource: 'FORM_KEY',
        formKey: ''
      }
    ]);
    this.syncBindingsJsonDraft();
  }

  removeBinding(index: number): void {
    this.editBindings.update(list => list.filter((_, i) => i !== index));
    this.syncBindingsJsonDraft();
  }

  patchBinding(index: number, patch: Partial<CatalogProductBinding>): void {
    const list = [...this.editBindings()];
    const current = list[index];
    if (!current) {
      return;
    }
    list[index] = { ...current, ...patch };
    this.editBindings.set(list);
    this.syncBindingsJsonDraft();
  }

  onBindingKindChange(index: number, kind: CatalogProductBinding['kind']): void {
    if (kind === 'UI') {
      this.patchBinding(index, {
        kind,
        formSource: 'FORM_KEY',
        operation: undefined,
        capability: undefined
      });
    } else if (kind === 'WAIT') {
      this.patchBinding(index, {
        kind,
        formSource: undefined,
        formKey: undefined,
        billerScreen: undefined,
        operation: undefined,
        capability: undefined
      });
    } else if (kind === 'CAPABILITY') {
      this.patchBinding(index, {
        kind,
        formSource: undefined,
        formKey: undefined,
        billerScreen: undefined,
        operation: undefined,
        capability: this.editBindings()[index]?.capability || ''
      });
    } else {
      this.patchBinding(index, {
        kind,
        formSource: undefined,
        formKey: undefined,
        billerScreen: undefined,
        capability: undefined
      });
    }
  }

  onFormSourceChange(index: number, formSource: CatalogProductBinding['formSource']): void {
    if (formSource === 'BILLER_PRODUCT') {
      this.patchBinding(index, {
        formSource,
        formKey: undefined,
        billerScreen: this.editBindings()[index]?.billerScreen || 'LOOKUP'
      });
    } else {
      this.patchBinding(index, {
        formSource: 'FORM_KEY',
        billerScreen: undefined
      });
    }
  }

  actorRoleOptions(): Array<{ label: string; value: string }> {
    const fromRoles = this.editRoles()
      .map(r => (r.code || '').trim().toUpperCase())
      .filter(Boolean)
      .map(code => {
        const meta = ROLE_CODE_OPTIONS.find(o => o.value === code);
        return { value: code, label: meta?.label || code };
      });
    if (fromRoles.length) {
      return fromRoles;
    }
    return ROLE_CODE_OPTIONS.map(o => ({ value: o.value, label: o.label }));
  }

  providerOptionsForBinding(actorRole?: string | null): Array<{ label: string; value: string }> {
    const role = (actorRole || '').trim().toUpperCase();
    const fromParticipants = role
      ? this.selectedProvidersForRole(role)
      : this.editParticipants().map(p => p.providerCode);
    const codes = [...new Set(fromParticipants.filter(Boolean))];
    if (codes.length) {
      return codes.map(code => ({
        value: code,
        label: `${this.providerLabel(code)} (${code})`
      }));
    }
    return this.providerOptions();
  }

  bindingSummary(b: CatalogProductBinding): string {
    if (b.kind === 'UI') {
      if (b.formSource === 'BILLER_PRODUCT') {
        return `BillerProduct · ${b.billerScreen || 'LOOKUP'}`;
      }
      if (b.providerCode && b.formKey) {
        return `Override ${b.providerCode} · ${b.formKey}`;
      }
      if (b.formKey) {
        return `Partagé · ${b.formKey}`;
      }
      return `Convention · ${this.conventionPreview(b)}`;
    }
    if (b.kind === 'WAIT') {
      return 'Attente';
    }
    return b.operation || '—';
  }

  /** Aligné sur CatalogFormKeyNomenclature Java. */
  formKeySlug(raw?: string | null): string {
    if (!raw || !String(raw).trim()) {
      return '';
    }
    return String(raw)
      .trim()
      .toLowerCase()
      .replace(/[\s_]+/g, '-')
      .replace(/[^a-z0-9-]/g, '')
      .replace(/-{2,}/g, '-')
      .replace(/^-|-$/g, '');
  }

  conventionalFormKey(productCode: string, actorRole: string, providerCode: string): string {
    const product = this.formKeySlug(productCode);
    const role = this.formKeySlug(actorRole);
    const provider = this.formKeySlug(providerCode);
    if (!product || !role || !provider) {
      return '';
    }
    return `${product}-${role}-${provider}`;
  }

  conventionPreview(b: CatalogProductBinding): string {
    const product = this.selected()?.code || 'PRODUCT';
    const role = b.actorRole || 'ROLE';
    const provider = b.providerCode || '{provider}';
    return this.conventionalFormKey(product, role, provider)
      || `${this.formKeySlug(product)}-${this.formKeySlug(role)}-{provider}`;
  }

  conventionExamples(b: CatalogProductBinding): string[] {
    if (!b.actorRole || (b.formKey || '').trim()) {
      return [];
    }
    const product = this.selected()?.code || 'PRODUCT';
    const providers = b.providerCode
      ? [b.providerCode]
      : this.selectedProvidersForRole(b.actorRole).slice(0, 3);
    return providers
      .map(p => this.conventionalFormKey(product, b.actorRole!, p))
      .filter(Boolean);
  }

  onBindingsJsonDraftChange(text: string): void {
    this.bindingsJsonDraft.set(text);
    try {
      const parsed = this.parseBindingsJson(text);
      this.editBindings.set(parsed.map(b => ({ ...b })));
      this.error.set(null);
    } catch {
      // laisse l'utilisateur finir de taper ; validation à l'enregistrement
    }
  }

  addRole(): void {
    const next = this.availableRoleCodes()[0];
    if (!next) {
      this.error.set('Tous les rôles plateforme sont déjà déclarés');
      return;
    }
    this.editRoles.update(list => [
      ...list,
      {
        code: next.value,
        label: next.defaultLabel,
        cardinality: 'ONE_REQUIRED',
        selection: 'USER'
      }
    ]);
  }

  roleCodeOptionsForRow(index: number): Array<{ label: string; value: string; defaultLabel: string }> {
    const current = this.editRoles()[index]?.code?.toUpperCase();
    const usedElsewhere = new Set(
      this.editRoles()
        .map((r, i) => (i === index ? '' : (r.code || '').toUpperCase()))
        .filter(Boolean)
    );
    const opts = ROLE_CODE_OPTIONS.filter(o => !usedElsewhere.has(o.value));
    if (current && !ROLE_CODE_OPTIONS.some(o => o.value === current)) {
      return [
        { value: current, label: `${current} (hors référentiel)`, defaultLabel: current },
        ...opts
      ];
    }
    return opts;
  }

  bindingKindOptionsForRow(index: number): Array<{ label: string; value: string }> {
    const current = String(this.editBindings()[index]?.kind || 'UI').toUpperCase();
    if (current && !BINDING_KIND_OPTIONS.some(o => o.value === current)) {
      return [{ label: `${current} (legacy)`, value: current }, ...BINDING_KIND_OPTIONS];
    }
    return BINDING_KIND_OPTIONS;
  }

  onRoleCodeChange(index: number, code: string): void {
    const next = (code || '').trim().toUpperCase();
    const current = this.editRoles()[index];
    if (!current || !next) {
      return;
    }
    const meta = ROLE_CODE_OPTIONS.find(o => o.value === next);
    const prevDefault = ROLE_CODE_OPTIONS.find(o => o.value === current.code)?.defaultLabel;
    const keepLabel =
      current.label
      && current.label.trim() !== ''
      && current.label !== prevDefault
      && current.label !== current.code;
    this.patchRole(index, {
      code: next,
      label: keepLabel ? current.label : (meta?.defaultLabel ?? next)
    });
  }

  removeRole(index: number): void {
    const removed = this.editRoles()[index];
    this.editRoles.update(list => list.filter((_, i) => i !== index));
    if (removed) {
      this.editParticipants.update(list =>
        list.filter(p => p.role.toUpperCase() !== removed.code.toUpperCase())
      );
    }
  }

  patchRole(index: number, patch: Partial<CatalogProductRole>): void {
    const list = [...this.editRoles()];
    const current = list[index];
    if (!current) {
      return;
    }
    const nextCode = patch.code != null ? String(patch.code).trim().toUpperCase() : current.code;
    if (patch.code != null && nextCode !== current.code) {
      this.editParticipants.update(parts =>
        parts.map(p =>
          p.role.toUpperCase() === current.code.toUpperCase()
            ? { ...p, role: nextCode }
            : p
        )
      );
    }
    list[index] = {
      ...current,
      ...patch,
      code: nextCode,
      label: patch.label !== undefined ? patch.label : current.label
    };
    this.editRoles.set(list);
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.listActiveCategories().subscribe({
      next: cats => this.categoryOptions.set(cats.map(c => ({ label: `${c.name} (${c.code})`, value: c.code }))),
      error: () => this.categoryOptions.set([])
    });
    this.api.listBpmProcesses().subscribe({
      next: (procs: BpmProcessOption[]) => {
        const opts = procs.map(p => ({ label: p.label, value: p.key }));
        if (!opts.some(o => o.value === 'accountTransfer')) {
          opts.unshift({ label: 'accountTransfer (Account Transfer)', value: 'accountTransfer' });
        }
        this.processOptions.set(opts);
      },
      error: () =>
        this.processOptions.set([
          { label: 'accountTransfer', value: 'accountTransfer' },
          { label: 'providerWalletTransfer', value: 'providerWalletTransfer' }
        ])
    });
    this.api.listServices().subscribe({
      next: list => {
        this.services.set(list);
        this.loading.set(false);
        const sel = this.selected();
        if (sel) {
          const refreshed = list.find(s => s.id === sel.id);
          if (refreshed) {
            this.selectService(refreshed);
          }
        }
      },
      error: err => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Chargement catalogue impossible');
      }
    });
  }

  canEditCasting(v: CatalogDeServiceVersion | null | undefined): boolean {
    if (!v) {
      return false;
    }
    return v.status !== 'PUBLISHED' && v.status !== 'ARCHIVED';
  }

  beginModifyCasting(v: CatalogDeServiceVersion): void {
    try {
      this.error.set(null);
      this.selectVersion(v);
      if (this.canEditCasting(v)) {
        this.editingCasting.set(true);
        this.info.set(null);
        return;
      }
      if (v.status === 'ARCHIVED') {
        this.error.set('Version archivée — non modifiable');
        return;
      }
      this.info.set(`v${v.version} est publiée (figée) — créez un brouillon pour modifier le casting`);
      this.startNewVersionFrom(v);
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'Impossible d’ouvrir la modification');
      this.editingCasting.set(false);
      this.editingVersion.set(false);
    }
  }

  cancelCastingEdit(ver: CatalogDeServiceVersion): void {
    this.editingCasting.set(false);
    this.setCastingEditors(this.rolesOf(ver), this.participantsOf(ver), this.bindingsOf(ver));
    this.hydrateRoleTranslations(ver);
  }

  beginModifyApproval(v: CatalogDeServiceVersion): void {
    try {
      this.error.set(null);
      this.selectVersion(v);
      if (this.canEditCasting(v)) {
        this.editingApproval.set(true);
        this.info.set(null);
        return;
      }
      if (v.status === 'ARCHIVED') {
        this.error.set('Version archivée — non modifiable');
        return;
      }
      this.info.set(`v${v.version} est publiée (figée) — créez un brouillon pour modifier l'approbation`);
      this.startNewVersionFrom(v);
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'Impossible d’ouvrir la modification');
      this.editingApproval.set(false);
      this.editingVersion.set(false);
    }
  }

  cancelApprovalEdit(ver: CatalogDeServiceVersion): void {
    this.editingApproval.set(false);
    this.editApproval.set(this.toApprovalEdit(this.approvalPolicyOf(ver)));
  }

  beginModifyMessaging(v: CatalogDeServiceVersion): void {
    try {
      this.error.set(null);
      this.selectVersion(v);
      this.hydrateMessaging(v);
      if (this.canEditCasting(v)) {
        this.editingMessaging.set(true);
        this.info.set(null);
        return;
      }
      if (v.status === 'ARCHIVED') {
        this.error.set('Version archivée — non modifiable');
        return;
      }
      this.info.set(`v${v.version} est publiée (figée) — créez un brouillon pour modifier historique/messages`);
      this.startNewVersionFrom(v);
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'Impossible d’ouvrir la modification');
      this.editingMessaging.set(false);
      this.editingVersion.set(false);
    }
  }

  cancelMessagingEdit(ver: CatalogDeServiceVersion): void {
    this.editingMessaging.set(false);
    this.hydrateMessaging(ver);
  }

  patchHistory(patch: Partial<HistoryEdit>): void {
    this.editHistory.update(cur => ({ ...cur, ...patch }));
  }

  patchHistoryI18n(field: keyof HistoryEdit, lang: string, value: string): void {
    this.historyI18n.update(cur => ({
      ...cur,
      [field]: this.withLang(cur[field] ?? {}, lang, value)
    }));
  }

  historyText(field: keyof HistoryEdit, lang: string): string {
    if (lang === this.defaultLanguage()) {
      return this.editHistory()[field] || '';
    }
    return this.historyI18n()[field]?.[lang] || '';
  }

  setHistoryText(field: keyof HistoryEdit, lang: string, value: string): void {
    if (lang === this.defaultLanguage()) {
      this.patchHistory({ [field]: value } as Partial<HistoryEdit>);
      return;
    }
    this.patchHistoryI18n(field, lang, value);
  }

  notifText(n: NotificationEdit, field: 'title' | 'body', lang: string): string {
    if (lang === this.defaultLanguage()) {
      return n[field] || '';
    }
    return this.notifI18nText(n.code, field, lang);
  }

  setNotifText(
    index: number,
    code: string,
    field: 'title' | 'body',
    lang: string,
    value: string
  ): void {
    if (lang === this.defaultLanguage()) {
      this.patchNotification(index, { [field]: value } as Partial<NotificationEdit>);
      return;
    }
    this.patchNotificationI18n(code, field, lang, value);
  }

  addNotification(): void {
    this.editNotifications.update(list => [
      ...list,
      { code: 'SUCCESS', channelHint: 'SMS', title: '', body: '' }
    ]);
  }

  removeNotification(index: number): void {
    this.editNotifications.update(list => list.filter((_, i) => i !== index));
  }

  patchNotification(index: number, patch: Partial<NotificationEdit>): void {
    this.editNotifications.update(list =>
      list.map((n, i) => (i === index ? { ...n, ...patch } : n))
    );
  }

  patchNotificationI18n(code: string, field: 'title' | 'body', lang: string, value: string): void {
    const key = (code || '').trim().toUpperCase();
    if (!key) {
      return;
    }
    this.notificationI18n.update(cur => {
      const entry = { ...(cur[key] ?? {}) };
      entry[field] = this.withLang(entry[field] ?? {}, lang, value);
      return { ...cur, [key]: entry };
    });
  }

  notifI18nText(code: string, field: 'title' | 'body', lang: string): string {
    const key = (code || '').trim().toUpperCase();
    return this.notificationI18n()[key]?.[field]?.[lang] || '';
  }

  beginModifyJourney(v: CatalogDeServiceVersion): void {
    try {
      this.error.set(null);
      this.selectVersion(v);
      if (this.canEditCasting(v)) {
        this.editingJourney.set(true);
        this.info.set(null);
        return;
      }
      if (v.status === 'ARCHIVED') {
        this.error.set('Version archivée — non modifiable');
        return;
      }
      this.info.set(`v${v.version} est publiée (figée) — créez un brouillon pour modifier journey/channel`);
      this.startNewVersionFrom(v);
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'Impossible d’ouvrir la modification');
      this.editingJourney.set(false);
      this.editingVersion.set(false);
    }
  }

  cancelJourneyEdit(ver: CatalogDeServiceVersion): void {
    this.editingJourney.set(false);
    this.editJourney.set(this.toJourneyEdit(ver));
  }

  patchJourney(patch: Partial<JourneyEdit>): void {
    this.editJourney.update((cur) => ({ ...cur, ...patch }));
  }

  beginModifyPresentation(v: CatalogDeServiceVersion): void {
    try {
      this.error.set(null);
      this.selectVersion(v);
      this.editPresentation.set(this.toPresentationEdit(v));
      if (this.canEditCasting(v)) {
        this.editingPresentation.set(true);
        this.info.set(null);
        return;
      }
      if (v.status === 'ARCHIVED') {
        this.error.set('Version archivée — non modifiable');
        return;
      }
      this.info.set(`v${v.version} est publiée (figée) — créez un brouillon pour modifier la présentation`);
      this.startNewVersionFrom(v);
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'Impossible d’ouvrir la modification');
      this.editingPresentation.set(false);
      this.editingVersion.set(false);
    }
  }

  cancelPresentationEdit(ver: CatalogDeServiceVersion): void {
    this.editingPresentation.set(false);
    this.editPresentation.set(this.toPresentationEdit(ver));
  }

  setPresentationTab(tab: PresentationTab): void {
    this.syncPresentationJsonFromLayer();
    this.editPresentation.update((cur) => {
      const next = { ...cur, activeTab: tab };
      return { ...next, jsonDraft: this.layerToJson(this.layerForTab(next, tab)) };
    });
  }

  activePresentationLayer(): PresentationLayerEdit | null {
    return this.layerForTab(this.editPresentation(), this.editPresentation().activeTab);
  }

  setChannelOverrideEnabled(enabled: boolean): void {
    const tab = this.editPresentation().activeTab;
    if (tab === 'BASE') {
      return;
    }
    this.editPresentation.update((cur) => {
      const overrides = { ...cur.overrides };
      const existing = overrides[tab];
      if (enabled) {
        overrides[tab] = {
          ...(existing || this.cloneLayer(cur.base)),
          enabled: true,
        };
      } else if (existing) {
        overrides[tab] = { ...existing, enabled: false, sections: [] };
      } else {
        overrides[tab] = { ...EMPTY_LAYER(false) };
      }
      const next = { ...cur, overrides };
      return { ...next, jsonDraft: this.layerToJson(this.layerForTab(next, tab)) };
    });
  }

  togglePresentationJsonMode(jsonMode: boolean): void {
    if (jsonMode) {
      this.syncPresentationJsonFromLayer();
      this.editPresentation.update((cur) => ({ ...cur, jsonMode: true }));
      return;
    }
    try {
      const parsed = this.parseLayerJson(this.editPresentation().jsonDraft);
      this.applyLayerToActiveTab(parsed);
      this.editPresentation.update((cur) => ({ ...cur, jsonMode: false }));
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'JSON présentation invalide');
    }
  }

  patchPresentationJsonDraft(text: string): void {
    this.editPresentation.update((cur) => ({ ...cur, jsonDraft: text }));
  }

  patchActivePresentationLayer(patch: Partial<PresentationLayerEdit>): void {
    this.mutateActiveLayer((layer) => ({ ...layer, ...patch }));
  }

  addPresentationSection(): void {
    this.mutateActiveLayer((layer) => ({
      ...layer,
      enabled: true,
      sections: [...layer.sections, EMPTY_SECTION()],
    }));
  }

  removePresentationSection(index: number): void {
    this.mutateActiveLayer((layer) => ({
      ...layer,
      sections: layer.sections.filter((_, i) => i !== index),
    }));
  }

  movePresentationSection(index: number, delta: number): void {
    this.mutateActiveLayer((layer) => {
      const sections = [...layer.sections];
      const target = index + delta;
      if (target < 0 || target >= sections.length) {
        return layer;
      }
      const tmp = sections[index];
      sections[index] = sections[target];
      sections[target] = tmp;
      return { ...layer, sections };
    });
  }

  patchPresentationSection(index: number, patch: Partial<PresentationSectionEdit>): void {
    this.mutateActiveLayer((layer) => ({
      ...layer,
      sections: layer.sections.map((s, i) => (i === index ? { ...s, ...patch } : s)),
    }));
  }

  /**
   * Chaque source lit ses propres `params` : l'éditeur rappelle lesquels, sinon
   * l'exploitant doit deviner ce que la section attend.
   */
  paramsHint(sec: PresentationSectionEdit): string {
    if (sec.origin === 'OMNICANAL') {
      return 'codeFse, dataKey…';
    }
    switch (sec.dataSource) {
      case 'TRANSFER_PARTICIPANTS':
        return 'role = SOURCE | DESTINATION';
      case 'CATALOG_PARTICIPANTS':
        return 'role = PAYER | SOURCE | DESTINATION…';
      case 'BILL_FAVORITES':
        return 'limit';
      case 'BILLERS':
        return 'size';
      default:
        return 'facultatif';
    }
  }

  paramsPlaceholder(sec: PresentationSectionEdit): string {
    if (sec.origin === 'OMNICANAL') {
      return '{"codeFse":"FSE_0001","dataKey":"PARTICIPANTS"}';
    }
    switch (sec.dataSource) {
      case 'TRANSFER_PARTICIPANTS':
        return '{"role":"DESTINATION"}';
      case 'CATALOG_PARTICIPANTS':
        return '{"role":"PAYER"}';
      case 'BILL_FAVORITES':
        return '{"limit":8}';
      case 'BILLERS':
        return '{"size":30}';
      default:
        return '{}';
    }
  }

  applyBillPresentationTemplate(): void {
    this.applyLayerToActiveTab(DEFAULT_BILL_PRESENTATION_LAYER());
  }

  savePresentationSettings(ver: CatalogDeServiceVersion): void {
    if (!this.canEditCasting(ver)) {
      this.error.set('Cette version est figée — créez une nouvelle version pour modifier');
      return;
    }
    if (this.editPresentation().jsonMode) {
      try {
        this.applyLayerToActiveTab(this.parseLayerJson(this.editPresentation().jsonDraft));
      } catch (e) {
        this.error.set(e instanceof Error ? e.message : 'JSON présentation invalide');
        return;
      }
    }

    let basePayload: Record<string, unknown>;
    const channelPayloads: Partial<Record<AccessChannel, Record<string, unknown> | null>> = {};
    try {
      const edit = this.editPresentation();
      basePayload = this.layerToConfig(edit.base);
      for (const ch of ACCESS_CHANNELS) {
        const ov = edit.overrides[ch.code];
        if (ov?.enabled && ov.sections.length) {
          channelPayloads[ch.code] = this.layerToConfig(ov);
        } else {
          channelPayloads[ch.code] = null;
        }
      }
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'Présentation invalide');
      return;
    }

    const configuration: Record<string, unknown> = {
      ...(ver.configuration ?? {}),
      presentation: basePayload,
    };

    const existingDefaults =
      configuration['channelDefaults'] && typeof configuration['channelDefaults'] === 'object'
        ? { ...(configuration['channelDefaults'] as Record<string, unknown>) }
        : configuration['channel_defaults'] && typeof configuration['channel_defaults'] === 'object'
          ? { ...(configuration['channel_defaults'] as Record<string, unknown>) }
          : {};

    for (const ch of ACCESS_CHANNELS) {
      const key = ch.code;
      const prev =
        existingDefaults[key] && typeof existingDefaults[key] === 'object'
          ? { ...(existingDefaults[key] as Record<string, unknown>) }
          : {};
      const payload = channelPayloads[key];
      if (payload) {
        prev['presentation'] = payload;
        existingDefaults[key] = prev;
      } else {
        delete prev['presentation'];
        if (Object.keys(prev).length) {
          existingDefaults[key] = prev;
        } else {
          delete existingDefaults[key];
        }
      }
    }

    if (Object.keys(existingDefaults).length) {
      configuration['channelDefaults'] = existingDefaults;
    } else {
      delete configuration['channelDefaults'];
    }
    delete configuration['channel_defaults'];

    this.busy.set(true);
    this.error.set(null);
    this.api.updateDraftConfiguration(ver.id, configuration).subscribe({
      next: (updated) => {
        this.busy.set(false);
        this.editingPresentation.set(false);
        this.info.set(`Présentation catalogue v${updated.version} enregistrée`);
        const svc = this.selected();
        if (svc) {
          this.loadVersions(svc.id, updated.id);
        }
      },
      error: (err) => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Échec enregistrement présentation');
      },
    });
  }

  presentationSummary(ver: CatalogDeServiceVersion, channel: AccessChannel | null): string {
    const layer = this.readPresentationLayer(ver, channel);
    if (!layer || !layer.sections.length) {
      return channel ? '' : 'Aucune (fallback serveur si BILL_PAYMENT)';
    }
    return `${layer.sections.length} section(s) · ${layer.sections.map((s) => s.displayKind).join(' → ')}`;
  }

  presentationOverrideSummary(ver: CatalogDeServiceVersion, channel: AccessChannel): string {
    const layer = this.readPresentationLayer(ver, channel);
    if (!layer || !layer.sections.length) {
      return '';
    }
    return `${layer.sections.length} section(s) · ${layer.sections.map((s) => s.displayKind).join(' → ')}`;
  }

  saveJourneySettings(ver: CatalogDeServiceVersion): void {
    if (!this.canEditCasting(ver)) {
      this.error.set('Cette version est figée — créez une nouvelle version pour modifier');
      return;
    }
    const edit = this.editJourney();
    const journeyCode = (edit.journeyCode || '').trim();
    const supportedChannels = [...(edit.supportedChannels || [])];
    const defaultChannel = edit.defaultChannel;
    const supportedAudiences = [...(edit.supportedAudiences || [])];
    const defaultAudience = edit.defaultAudience;
    const configuration: Record<string, unknown> = {
      ...(ver.configuration ?? {}),
    };
    if (journeyCode) {
      configuration['journeyCode'] = journeyCode;
    } else {
      delete configuration['journeyCode'];
    }
    if (supportedChannels.length) {
      configuration['supportedChannels'] = supportedChannels;
    } else {
      delete configuration['supportedChannels'];
    }
    if (defaultChannel) {
      configuration['defaultChannel'] = defaultChannel;
    } else {
      delete configuration['defaultChannel'];
    }
    if (supportedAudiences.length) {
      configuration['supportedAudiences'] = supportedAudiences;
    } else {
      delete configuration['supportedAudiences'];
    }
    if (defaultAudience) {
      configuration['defaultAudience'] = defaultAudience;
    } else {
      delete configuration['defaultAudience'];
    }
    this.busy.set(true);
    this.error.set(null);
    this.api.updateDraftConfiguration(ver.id, configuration).subscribe({
      next: (updated) => {
        this.busy.set(false);
        this.editingJourney.set(false);
        this.info.set(`Journey / Channel / Audience v${updated.version} enregistré`);
        const svc = this.selected();
        if (svc) {
          this.loadVersions(svc.id, updated.id);
        }
      },
      error: (err) => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Échec enregistrement journey');
      },
    });
  }

  patchApproval(patch: Partial<ApprovalPolicyEdit>): void {
    this.editApproval.update(cur => ({ ...cur, ...patch }));
  }

  saveApprovalPolicy(ver: CatalogDeServiceVersion): void {
    if (!this.canEditCasting(ver)) {
      this.error.set('Cette version est figée — créez une nouvelle version pour modifier');
      return;
    }
    const edit = this.editApproval();
    const criticalFields = edit.criticalFieldsText
      .split(/[\n,]+/)
      .map(s => s.trim())
      .filter(Boolean);
    const approvalPolicy = {
      code: (edit.code || DEFAULT_APPROVAL_POLICY.code).trim(),
      enabled: !!edit.enabled,
      strategy: (edit.strategy || DEFAULT_APPROVAL_POLICY.strategy).trim(),
      dmnDecisionKey: (edit.dmnDecisionKey || DEFAULT_APPROVAL_POLICY.dmnDecisionKey).trim(),
      initiatorCanApprove: !!edit.initiatorCanApprove,
      sameUserAcrossLevelsAllowed: !!edit.sameUserAcrossLevelsAllowed,
      formKey: (edit.formKey || DEFAULT_APPROVAL_POLICY.formKey).trim(),
      criticalFields: criticalFields.length ? criticalFields : [...DEFAULT_APPROVAL_POLICY.criticalFields]
    };
    const configuration: Record<string, unknown> = {
      ...(ver.configuration ?? {}),
      approvalPolicy
    };
    this.busy.set(true);
    this.error.set(null);
    this.api.updateDraftConfiguration(ver.id, configuration).subscribe({
      next: updated => {
        this.busy.set(false);
        this.editingApproval.set(false);
        this.info.set(
          approvalPolicy.enabled
            ? `Approbation activée sur v${updated.version}`
            : `Approbation désactivée sur v${updated.version}`
        );
        const svc = this.selected();
        if (svc) {
          this.loadVersions(svc.id, updated.id);
        }
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Échec enregistrement approbation');
      }
    });
  }

  saveMessagingSettings(ver: CatalogDeServiceVersion): void {
    if (!this.canEditCasting(ver)) {
      this.error.set('Cette version est figée — créez une nouvelle version pour modifier');
      return;
    }
    const h = this.editHistory();
    const history: Record<string, unknown> = {};
    if (h.kindLabel?.trim()) history['kindLabel'] = h.kindLabel.trim();
    if (h.titleTemplate?.trim()) history['titleTemplate'] = h.titleTemplate.trim();
    if (h.subtitleTemplate?.trim()) history['subtitleTemplate'] = h.subtitleTemplate.trim();

    const notifications = this.editNotifications()
      .map(n => ({
        code: (n.code || '').trim().toUpperCase(),
        channelHint: (n.channelHint || 'SMS').trim().toUpperCase() || 'SMS',
        title: n.title?.trim() || undefined,
        body: n.body?.trim() || undefined
      }))
      .filter(n => !!n.code);

    const configuration: Record<string, unknown> = {
      ...(ver.configuration ?? {})
    };
    if (Object.keys(history).length) {
      configuration['history'] = history;
    } else {
      delete configuration['history'];
    }
    if (notifications.length) {
      configuration['notifications'] = notifications;
    } else {
      delete configuration['notifications'];
    }

    this.busy.set(true);
    this.error.set(null);
    this.api.updateDraftConfiguration(ver.id, configuration, this.messagingTranslations()).subscribe({
      next: updated => {
        this.busy.set(false);
        this.editingMessaging.set(false);
        this.info.set(`Historique & messages enregistrés sur v${updated.version}`);
        const svc = this.selected();
        if (svc) {
          this.loadVersions(svc.id, updated.id);
        }
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Échec enregistrement historique/messages');
      }
    });
  }

  private messagingTranslations(): ConfigurationTranslations | undefined {
    const out: ConfigurationTranslations = {};
    for (const [field, byLang] of Object.entries(this.historyI18n())) {
      const cleaned = this.nonEmpty(byLang ?? {});
      if (cleaned) {
        out[`configuration.history.${field}`] = cleaned;
      }
    }
    for (const n of this.editNotifications()) {
      const code = (n.code || '').trim().toUpperCase();
      if (!code) continue;
      const entry = this.notificationI18n()[code];
      const title = this.nonEmpty(entry?.title ?? {});
      const body = this.nonEmpty(entry?.body ?? {});
      if (title) out[`configuration.notifications[${code}].title`] = title;
      if (body) out[`configuration.notifications[${code}].body`] = body;
    }
    return Object.keys(out).length ? out : undefined;
  }

  selectService(item: CatalogDeService): void {
    this.selected.set(item);
    this.editingService.set(false);
    this.editingVersion.set(false);
    this.editingCasting.set(false);
    this.editingApproval.set(false);
    this.editingMessaging.set(false);
    this.editingJourney.set(false);
    this.workflowBinding.set(null);
    this.selectedVersion.set(null);
    this.loadVersions(item.id);
  }

  startNewService(): void {
    this.selected.set(null);
    this.versions.set([]);
    this.selectedVersion.set(null);
    this.editingVersion.set(false);
    this.workflowBinding.set(null);
    this.editingService.set(true);
    this.setCastingEditors(DEFAULT_ROLES, DEFAULT_PARTICIPANTS, DEFAULT_BINDINGS);
    this.nameI18n.set({});
    this.descriptionI18n.set({});
    this.roleLabelI18n.set({});
    this.serviceForm.reset({
      code: 'ACCOUNT_TRANSFER',
      name: 'Transfert de compte',
      description: 'Transfert entre comptes',
      categoryCode: 'TRANSFER',
      status: 'DRAFT',
      initialVersion: '1.0',
      workflowKey: 'accountTransfer',
      stepsText: DEFAULT_STEPS.join('\n'),
      bindingsJson: JSON.stringify(DEFAULT_BINDINGS, null, 2)
    });
  }

  editService(): void {
    const s = this.selected();
    if (!s) {
      return;
    }
    this.editingService.set(true);
    this.nameI18n.set({ ...(s.nameI18n ?? {}) });
    this.descriptionI18n.set({ ...(s.descriptionI18n ?? {}) });
    const lang = this.defaultLanguage();
    this.serviceForm.patchValue({
      code: s.code,
      // s.name est résolu pour la langue du navigateur : on édite bien la langue par défaut.
      name: s.nameI18n?.[lang] ?? s.name,
      description: s.descriptionI18n?.[lang] ?? s.description ?? '',
      categoryCode: s.categoryCode ?? '',
      status: s.status,
      initialVersion: s.currentVersion ?? '1.0',
      workflowKey: 'accountTransfer',
      stepsText: DEFAULT_STEPS.join('\n'),
      bindingsJson: JSON.stringify(DEFAULT_BINDINGS, null, 2)
    });
  }

  cancelServiceEdit(): void {
    this.editingService.set(false);
  }

  saveService(): void {
    if (this.serviceForm.invalid) {
      return;
    }
    const v = this.serviceForm.getRawValue();
    this.busy.set(true);
    this.error.set(null);
    this.info.set(null);

    const selected = this.selected();
    if (selected) {
      this.api
        .updateService(selected.id, {
          code: v.code,
          name: v.name,
          description: v.description || undefined,
          nameI18n: this.nonEmpty(this.nameI18n()),
          descriptionI18n: this.nonEmpty(this.descriptionI18n()),
          categoryCode: v.categoryCode,
          status: v.status
        })
        .subscribe({
          next: () => {
            this.busy.set(false);
            this.editingService.set(false);
            this.info.set('Produit mis à jour');
            this.reload();
          },
          error: err => {
            this.busy.set(false);
            this.error.set(err?.error?.message ?? err?.message ?? 'Échec mise à jour');
          }
        });
      return;
    }

    let bindings: CatalogProductBinding[];
    try {
      bindings = this.normalizedBindings();
    } catch (e) {
      this.busy.set(false);
      this.error.set(e instanceof Error ? e.message : 'Bindings invalides');
      return;
    }
    const roles = this.normalizedRoles();
    const participants = this.normalizedParticipants();
    this.api
      .createService({
        code: v.code,
        name: v.name,
        description: v.description || undefined,
        nameI18n: this.nonEmpty(this.nameI18n()),
        descriptionI18n: this.nonEmpty(this.descriptionI18n()),
        categoryCode: v.categoryCode,
        status: v.status,
        initialVersion: v.initialVersion || '1.0',
        workflowKey: v.workflowKey || undefined,
        translations: this.roleLabelTranslations(),
        configuration: {
          serviceType: 'DEFAUT',
          steps: this.parseSteps(v.stepsText),
          roles,
          participants,
          bindings
        }
      })
      .subscribe({
        next: created => {
          this.busy.set(false);
          this.editingService.set(false);
          this.info.set(`Produit ${created.code} créé`);
          this.reload();
          this.selected.set(created);
          this.loadVersions(created.id);
        },
        error: err => {
          this.busy.set(false);
          this.error.set(err?.error?.message ?? err?.message ?? 'Échec création');
        }
      });
  }

  startNewVersion(): void {
    const current =
      this.selectedVersion()
      ?? this.versions()[this.versions().length - 1]
      ?? null;
    this.startNewVersionFrom(current);
  }

  startNewVersionFrom(current: CatalogDeServiceVersion | null): void {
    const next = this.suggestNextVersion(
      current?.version ?? this.versions()[this.versions().length - 1]?.version
    );
    this.editingVersion.set(true);
    this.editingCasting.set(false);
    this.workflowBinding.set(null);
    this.setCastingEditors(
      this.rolesOf(current).length ? this.rolesOf(current) : DEFAULT_ROLES,
      this.participantsOf(current).length ? this.participantsOf(current) : DEFAULT_PARTICIPANTS,
      this.bindingsOf(current).length ? this.bindingsOf(current) : DEFAULT_BINDINGS
    );
    this.hydrateRoleTranslations(current);
    this.versionForm.reset({
      version: next,
      status: 'DRAFT',
      workflowKey:
        current?.workflowKey
        || (this.selected()?.code ? this.toCamel(this.selected()!.code) : 'accountTransfer'),
      serviceType: String(this.configValue(current, 'serviceType') ?? 'DEFAUT') as ServiceTypeCode,
      stepsText: this.stepsOf(current).join('\n') || DEFAULT_STEPS.join('\n'),
      bindingsJson: JSON.stringify(
        this.bindingsOf(current).length ? this.bindingsOf(current) : DEFAULT_BINDINGS,
        null,
        2
      )
    });
  }

  cancelVersionEdit(): void {
    this.editingVersion.set(false);
  }

  saveVersion(): void {
    const svc = this.selected();
    if (!svc || this.versionForm.invalid) {
      return;
    }
    const v = this.versionForm.getRawValue();
    let bindings: CatalogProductBinding[];
    try {
      bindings = this.normalizedBindings();
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'Bindings invalides');
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.api
      .createVersion(svc.id, {
        version: v.version,
        status: v.status,
        workflowKey: v.workflowKey || undefined,
        translations: this.roleLabelTranslations(),
        configuration: {
          serviceType: v.serviceType || 'DEFAUT',
          steps: this.parseSteps(v.stepsText),
          roles: this.normalizedRoles(),
          participants: this.normalizedParticipants(),
          bindings
        }
      })
      .subscribe({
        next: created => {
          this.busy.set(false);
          this.editingVersion.set(false);
          this.info.set(
            created.status === 'PUBLISHED'
              ? `Version v${created.version} publiée`
              : `Version v${created.version} créée`
          );
          this.reload();
          this.loadVersions(svc.id, created.id);
        },
        error: err => {
          this.busy.set(false);
          this.error.set(err?.error?.message ?? err?.message ?? 'Échec création version');
        }
      });
  }

  selectVersion(v: CatalogDeServiceVersion): void {
    this.selectedVersion.set(v);
    this.workflowBinding.set(null);
    this.editingCasting.set(false);
    this.editingApproval.set(false);
    this.editingMessaging.set(false);
    this.editingJourney.set(false);
    this.editingPresentation.set(false);
    this.setCastingEditors(this.rolesOf(v), this.participantsOf(v), this.bindingsOf(v));
    this.hydrateRoleTranslations(v);
    this.editApproval.set(this.toApprovalEdit(this.approvalPolicyOf(v)));
    this.editJourney.set(this.toJourneyEdit(v));
    this.editPresentation.set(this.toPresentationEdit(v));
    this.hydrateMessaging(v);
  }

  saveDraftConfig(ver: CatalogDeServiceVersion): void {
    if (!this.canEditCasting(ver)) {
      this.error.set('Cette version est figée — créez une nouvelle version pour modifier');
      return;
    }
    let bindings: CatalogProductBinding[];
    try {
      bindings = this.normalizedBindings();
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : 'Bindings invalides');
      return;
    }
    const configuration: Record<string, unknown> = {
      ...(ver.configuration ?? {}),
      roles: this.normalizedRoles(),
      participants: this.normalizedParticipants(),
      bindings
    };
    this.busy.set(true);
    this.error.set(null);
    this.api.updateDraftConfiguration(ver.id, configuration, this.roleLabelTranslations()).subscribe({
      next: updated => {
        this.busy.set(false);
        this.editingCasting.set(false);
        this.info.set(`Casting v${updated.version} enregistré`);
        const svc = this.selected();
        if (svc) {
          this.loadVersions(svc.id, updated.id);
        }
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Échec enregistrement casting');
      }
    });
  }

  publish(v: CatalogDeServiceVersion): void {
    this.busy.set(true);
    this.error.set(null);
    this.api.publishVersion(v.id).subscribe({
      next: published => {
        this.busy.set(false);
        this.info.set(`v${published.version} publiée — RapidBPMN ${published.workflowKey || '—'}`);
        const svc = this.selected();
        if (svc) {
          this.reload();
          this.loadVersions(svc.id, published.id);
        }
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Échec publication');
      }
    });
  }

  validateWorkflow(v: CatalogDeServiceVersion): void {
    this.busy.set(true);
    this.error.set(null);
    this.api.resolveWorkflow(v.id).subscribe({
      next: binding => {
        this.busy.set(false);
        this.selectedVersion.set(v);
        this.workflowBinding.set(binding);
        this.info.set(`Process ${binding.workflowKey} déployé (def v${binding.processVersion})`);
      },
      error: err => {
        this.busy.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Process RapidBPMN introuvable');
      }
    });
  }

  stepCount(v: CatalogDeServiceVersion | null | undefined): number {
    return this.stepsOf(v).length;
  }

  roleCount(v: CatalogDeServiceVersion | null | undefined): number {
    return this.rolesOf(v).length;
  }

  participantCount(v: CatalogDeServiceVersion | null | undefined): number {
    return this.participantsOf(v).length;
  }

  rolesOf(v: CatalogDeServiceVersion | null | undefined): CatalogProductRole[] {
    const raw = v?.configuration?.['roles'];
    if (!Array.isArray(raw)) {
      return [];
    }
    return raw
      .filter((item): item is Record<string, unknown> => !!item && typeof item === 'object')
      .map(item => ({
        code: String(item['code'] ?? ''),
        label: item['label'] != null ? String(item['label']) : undefined,
        cardinality: item['cardinality'] as CatalogProductRole['cardinality'],
        selection: item['selection'] as CatalogProductRole['selection'],
        derivedFrom: item['derivedFrom'] != null ? String(item['derivedFrom']) : undefined
      }))
      .filter(r => !!r.code);
  }

  participantsOf(v: CatalogDeServiceVersion | null | undefined): CatalogProductParticipant[] {
    const raw = v?.configuration?.['participants'];
    if (!Array.isArray(raw)) {
      return [];
    }
    return raw
      .filter((item): item is Record<string, unknown> => !!item && typeof item === 'object')
      .map(item => ({
        role: String(item['role'] ?? ''),
        providerCode: String(item['providerCode'] ?? item['provider'] ?? ''),
        default: item['default'] === true || item['default'] === 'true'
      }))
      .filter(p => !!p.role && !!p.providerCode);
  }

  participantsGrouped(
    v: CatalogDeServiceVersion | null | undefined
  ): Array<{ role: string; items: CatalogProductParticipant[] }> {
    const map = new Map<string, CatalogProductParticipant[]>();
    for (const p of this.participantsOf(v)) {
      const list = map.get(p.role) ?? [];
      list.push(p);
      map.set(p.role, list);
    }
    return [...map.entries()].map(([role, items]) => ({ role, items }));
  }

  bindingsOf(v: CatalogDeServiceVersion | null | undefined): CatalogProductBinding[] {
    const raw = v?.configuration?.['bindings'];
    if (!Array.isArray(raw)) {
      return [];
    }
    return raw
      .filter((item): item is Record<string, unknown> => !!item && typeof item === 'object')
      .map(item => ({
        step: String(item['step'] ?? ''),
        kind: item['kind'] as CatalogProductBinding['kind'],
        actorRole: item['actorRole'] != null ? String(item['actorRole']) : undefined,
        providerCode: item['providerCode'] != null ? String(item['providerCode']) : undefined,
        formKey: item['formKey'] != null ? String(item['formKey']) : undefined,
        formSource: item['formSource'] as CatalogProductBinding['formSource'],
        billerScreen: item['billerScreen'] != null ? String(item['billerScreen']) : undefined,
        operation: item['operation'] != null ? String(item['operation']) : undefined,
        capability: item['capability'] != null ? String(item['capability']) : undefined
      }))
      .filter(b => !!b.step);
  }

  approvalPolicyOf(v: CatalogDeServiceVersion | null | undefined): ApprovalPolicyView | null {
    const raw = v?.configuration?.['approvalPolicy'];
    if (!raw || typeof raw !== 'object') {
      return null;
    }
    const m = raw as Record<string, unknown>;
    const criticalRaw = m['criticalFields'];
    const criticalFields = Array.isArray(criticalRaw)
      ? criticalRaw.map(x => String(x)).filter(Boolean)
      : undefined;
    const dmn =
      m['dmnDecisionKey'] != null
        ? String(m['dmnDecisionKey'])
        : m['decisionKey'] != null
          ? String(m['decisionKey'])
          : undefined;
    return {
      enabled: m['enabled'] !== false && m['enabled'] !== 'false',
      code: m['code'] != null ? String(m['code']) : undefined,
      strategy: m['strategy'] != null ? String(m['strategy']) : undefined,
      dmnDecisionKey: dmn,
      initiatorCanApprove:
        m['initiatorCanApprove'] === true ||
        m['initiatorCanApprove'] === 'true' ||
        m['selfApprovalAllowed'] === true ||
        m['selfApprovalAllowed'] === 'true',
      sameUserAcrossLevelsAllowed:
        m['sameUserAcrossLevelsAllowed'] === true || m['sameUserAcrossLevelsAllowed'] === 'true',
      formKey: m['formKey'] != null ? String(m['formKey']) : undefined,
      criticalFields
    };
  }

  private toApprovalEdit(pol: ApprovalPolicyView | null): ApprovalPolicyEdit {
    const fields = pol?.criticalFields?.length
      ? pol.criticalFields
      : DEFAULT_APPROVAL_POLICY.criticalFields;
    return {
      enabled: pol ? !!pol.enabled : DEFAULT_APPROVAL_POLICY.enabled,
      code: pol?.code || DEFAULT_APPROVAL_POLICY.code,
      strategy: pol?.strategy || DEFAULT_APPROVAL_POLICY.strategy,
      dmnDecisionKey: pol?.dmnDecisionKey || DEFAULT_APPROVAL_POLICY.dmnDecisionKey,
      initiatorCanApprove: !!pol?.initiatorCanApprove,
      sameUserAcrossLevelsAllowed: !!pol?.sameUserAcrossLevelsAllowed,
      formKey: pol?.formKey || DEFAULT_APPROVAL_POLICY.formKey,
      criticalFieldsText: fields.join('\n')
    };
  }

  stepsOf(v: CatalogDeServiceVersion | null | undefined): string[] {
    if (!v?.configuration) {
      return [];
    }
    const raw = v.configuration['steps'];
    if (!Array.isArray(raw)) {
      return [];
    }
    return raw.map(item => {
      if (typeof item === 'string') {
        return item;
      }
      if (item && typeof item === 'object' && 'code' in item) {
        return String((item as { code: unknown }).code);
      }
      return String(item);
    });
  }

  configValue(v: CatalogDeServiceVersion | null | undefined, key: string): string | null {
    const val = v?.configuration?.[key];
    return val != null && String(val).trim() !== '' ? String(val) : null;
  }

  journeyOf(v: CatalogDeServiceVersion | null | undefined) {
    const code = this.selected()?.code || v?.catalogDeServiceCode || '';
    return productJourneyMeta(code, { configuration: v?.configuration ?? null });
  }

  private toJourneyEdit(v: CatalogDeServiceVersion | null): JourneyEdit {
    if (!v) {
      return {
        journeyCode: '',
        supportedChannels: [],
        defaultChannel: null,
        supportedAudiences: [],
        defaultAudience: null,
      };
    }
    const meta = this.journeyOf(v);
    return {
      journeyCode: meta.journeyCode || '',
      supportedChannels: [...meta.supportedChannels],
      defaultChannel: meta.defaultChannel,
      supportedAudiences: [...meta.supportedAudiences],
      defaultAudience: meta.defaultAudience,
    };
  }

  private emptyPresentationEdit(): PresentationEdit {
    const base = EMPTY_LAYER(true);
    return {
      base,
      overrides: {},
      activeTab: 'BASE',
      jsonMode: false,
      jsonDraft: this.layerToJson(base),
    };
  }

  private toPresentationEdit(v: CatalogDeServiceVersion | null): PresentationEdit {
    if (!v) {
      return this.emptyPresentationEdit();
    }
    const base = this.readPresentationLayer(v, null) || EMPTY_LAYER(true);
    const overrides: Partial<Record<AccessChannel, PresentationLayerEdit>> = {};
    for (const ch of ACCESS_CHANNELS) {
      const layer = this.readPresentationLayer(v, ch.code);
      if (layer && layer.sections.length) {
        overrides[ch.code] = { ...layer, enabled: true };
      }
    }
    return {
      base,
      overrides,
      activeTab: 'BASE',
      jsonMode: false,
      jsonDraft: this.layerToJson(base),
    };
  }

  private readPresentationLayer(
    v: CatalogDeServiceVersion,
    channel: AccessChannel | null
  ): PresentationLayerEdit | null {
    const cfg = v.configuration ?? {};
    if (!channel) {
      return this.configToLayer(cfg['presentation']);
    }
    const defaults =
      (cfg['channelDefaults'] as Record<string, unknown> | undefined) ||
      (cfg['channel_defaults'] as Record<string, unknown> | undefined) ||
      {};
    const chCfg = defaults[channel];
    if (!chCfg || typeof chCfg !== 'object') {
      return null;
    }
    return this.configToLayer((chCfg as Record<string, unknown>)['presentation']);
  }

  private configToLayer(raw: unknown): PresentationLayerEdit | null {
    if (!raw || typeof raw !== 'object') {
      return null;
    }
    const obj = raw as Record<string, unknown>;
    const sectionsRaw = Array.isArray(obj['sections']) ? obj['sections'] : [];
    const sections: PresentationSectionEdit[] = sectionsRaw
      .filter((s): s is Record<string, unknown> => !!s && typeof s === 'object')
      .map((s) => {
        const displayKind = String(s['displayKind'] ?? s['display_kind'] ?? 'LEAF_LIST');
        return {
          id: String(s['id'] ?? s['code'] ?? ''),
          displayKind,
          dataSource: String(s['dataSource'] ?? s['data_source'] ?? 'STATIC_ITEMS'),
          origin: String(s['origin'] ?? s['dataOrigin'] ?? 'LOCAL').toUpperCase(),
          title: String(s['title'] ?? s['titleKey'] ?? ''),
          bindsTo: String(s['bindsTo'] ?? s['binds_to'] ?? ''),
          searchable:
            typeof s['searchable'] === 'boolean' ? s['searchable'] : displayKind === 'LEAF_LIST',
          pageable:
            typeof s['pageable'] === 'boolean' ? s['pageable'] : displayKind === 'LEAF_LIST',
          itemsJson: JSON.stringify(Array.isArray(s['items']) ? s['items'] : [], null, 2),
          paramsJson: JSON.stringify(
            s['params'] && typeof s['params'] === 'object' ? s['params'] : {},
            null,
            2
          ),
        };
      });
    const version =
      typeof obj['version'] === 'number'
        ? obj['version']
        : Number(obj['version']) || 1;
    return {
      version,
      enabled: sections.length > 0,
      sections,
    };
  }

  private layerToConfig(layer: PresentationLayerEdit): Record<string, unknown> {
    const sections = layer.sections
      .map((s) => {
        const id = (s.id || '').trim();
        if (!id) {
          throw new Error('Chaque section doit avoir un id');
        }
        let items: unknown[] = [];
        if (s.dataSource === 'STATIC_ITEMS' || s.dataSource === 'BILL_FAVORITES') {
          try {
            const parsed = JSON.parse(s.itemsJson || '[]');
            if (!Array.isArray(parsed)) {
              throw new Error('items doit être un tableau JSON');
            }
            items = parsed;
          } catch {
            throw new Error(`JSON items invalide (section ${id})`);
          }
        }
        let params: Record<string, unknown> = {};
        try {
          const parsed = JSON.parse(s.paramsJson || '{}');
          if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
            throw new Error('params doit être un objet JSON');
          }
          params = parsed as Record<string, unknown>;
        } catch {
          throw new Error(`JSON params invalide (section ${id})`);
        }
        const out: Record<string, unknown> = {
          id,
          displayKind: s.displayKind || 'LEAF_LIST',
          dataSource: s.dataSource || 'STATIC_ITEMS',
          origin: (s.origin || 'LOCAL').toUpperCase(),
          searchable: !!s.searchable,
          pageable: !!s.pageable,
        };
        if ((s.title || '').trim()) {
          out['title'] = s.title.trim();
        }
        if ((s.bindsTo || '').trim()) {
          out['bindsTo'] = s.bindsTo.trim();
        }
        if (items.length) {
          out['items'] = items;
        }
        if (Object.keys(params).length) {
          out['params'] = params;
        }
        return out;
      });
    return {
      version: layer.version || 1,
      sections,
    };
  }

  private layerToJson(layer: PresentationLayerEdit | null): string {
    if (!layer) {
      return JSON.stringify({ version: 1, sections: [] }, null, 2);
    }
    try {
      return JSON.stringify(this.layerToConfig({ ...layer, enabled: true }), null, 2);
    } catch {
      return JSON.stringify({ version: layer.version || 1, sections: [] }, null, 2);
    }
  }

  private parseLayerJson(text: string): PresentationLayerEdit {
    let parsed: unknown;
    try {
      parsed = JSON.parse(text || '{}');
    } catch {
      throw new Error('JSON présentation invalide');
    }
    const layer = this.configToLayer(parsed);
    if (!layer) {
      throw new Error('JSON présentation : objet { version, sections } attendu');
    }
    return { ...layer, enabled: true };
  }

  private layerForTab(edit: PresentationEdit, tab: PresentationTab): PresentationLayerEdit {
    if (tab === 'BASE') {
      return edit.base;
    }
    return edit.overrides[tab] || EMPTY_LAYER(false);
  }

  private cloneLayer(layer: PresentationLayerEdit): PresentationLayerEdit {
    return {
      version: layer.version,
      enabled: true,
      sections: layer.sections.map((s) => ({ ...s })),
    };
  }

  private applyLayerToActiveTab(layer: PresentationLayerEdit): void {
    const tab = this.editPresentation().activeTab;
    this.editPresentation.update((cur) => {
      if (tab === 'BASE') {
        const next = { ...cur, base: { ...layer, enabled: true } };
        return { ...next, jsonDraft: this.layerToJson(next.base) };
      }
      const overrides = {
        ...cur.overrides,
        [tab]: { ...layer, enabled: true },
      };
      const next = { ...cur, overrides };
      return { ...next, jsonDraft: this.layerToJson(overrides[tab]!) };
    });
  }

  private mutateActiveLayer(fn: (layer: PresentationLayerEdit) => PresentationLayerEdit): void {
    const tab = this.editPresentation().activeTab;
    this.editPresentation.update((cur) => {
      const current = this.layerForTab(cur, tab);
      const updated = fn({ ...current, enabled: tab === 'BASE' ? true : true });
      if (tab === 'BASE') {
        const next = { ...cur, base: updated };
        return { ...next, jsonDraft: this.layerToJson(updated) };
      }
      const overrides = { ...cur.overrides, [tab]: updated };
      const next = { ...cur, overrides };
      return { ...next, jsonDraft: this.layerToJson(updated) };
    });
  }

  private syncPresentationJsonFromLayer(): void {
    const edit = this.editPresentation();
    if (!edit.jsonMode) {
      this.editPresentation.update((cur) => ({
        ...cur,
        jsonDraft: this.layerToJson(this.layerForTab(cur, cur.activeTab)),
      }));
    }
  }

  private loadProviders(): void {
    this.providersLoading.set(true);
    this.providersApi.listActive().subscribe({
      next: list => {
        this.providers.set(list);
        this.providersLoading.set(false);
      },
      error: () => {
        this.providersApi.list().subscribe({
          next: list => {
            this.providers.set(list);
            this.providersLoading.set(false);
          },
          error: () => {
            this.providers.set([]);
            this.providersLoading.set(false);
          }
        });
      }
    });
  }

  private loadVersions(serviceId: string, selectVersionId?: string): void {
    this.versionsLoading.set(true);
    this.api.listVersions(serviceId).subscribe({
      next: list => {
        this.versions.set(list);
        this.versionsLoading.set(false);
        if (selectVersionId) {
          this.selectedVersion.set(list.find(x => x.id === selectVersionId) ?? null);
        } else {
          const current = this.selected()?.currentVersionId;
          this.selectedVersion.set(
            (current ? list.find(x => x.id === current) : null) ?? list[list.length - 1] ?? null
          );
        }
        const selectedVer = this.selectedVersion();
        if (selectedVer) {
          this.setCastingEditors(
            this.rolesOf(selectedVer),
            this.participantsOf(selectedVer),
            this.bindingsOf(selectedVer)
          );
          this.hydrateRoleTranslations(selectedVer);
          this.editApproval.set(this.toApprovalEdit(this.approvalPolicyOf(selectedVer)));
          this.editJourney.set(this.toJourneyEdit(selectedVer));
          this.editPresentation.set(this.toPresentationEdit(selectedVer));
          this.hydrateMessaging(selectedVer);
          this.editingApproval.set(false);
          this.editingMessaging.set(false);
          this.editingJourney.set(false);
          this.editingPresentation.set(false);
        }
      },
      error: err => {
        this.versionsLoading.set(false);
        this.error.set(err?.error?.message ?? err?.message ?? 'Chargement versions impossible');
      }
    });
  }

  /** Reconstitue les libellés de rôle par langue depuis `version.translations`. */
  private hydrateRoleTranslations(ver: CatalogDeServiceVersion | null): void {
    const byRole: Record<string, LocalizedText> = {};
    for (const [path, byLang] of Object.entries(ver?.translations ?? {})) {
      const code = /^configuration\.roles\[(.+)]\.label$/.exec(path)?.[1];
      if (code && byLang) {
        byRole[code.toUpperCase()] = { ...byLang };
      }
    }
    this.roleLabelI18n.set(byRole);
  }

  historySummary(ver: CatalogDeServiceVersion): HistoryEdit | null {
    const h = this.historyOf(ver);
    if (!h.kindLabel && !h.titleTemplate && !h.subtitleTemplate && !this.notificationsOf(ver).length) {
      return null;
    }
    return h;
  }

  historyOf(ver: CatalogDeServiceVersion): HistoryEdit {
    const raw = (ver.configuration?.['history'] ?? {}) as Record<string, unknown>;
    return {
      kindLabel: String(raw['kindLabel'] ?? ''),
      titleTemplate: String(raw['titleTemplate'] ?? ''),
      subtitleTemplate: String(raw['subtitleTemplate'] ?? '')
    };
  }

  notificationsOf(ver: CatalogDeServiceVersion): NotificationEdit[] {
    const raw = ver.configuration?.['notifications'];
    if (!Array.isArray(raw)) {
      return [];
    }
    return raw
      .map((item: unknown) => {
        const n = (item ?? {}) as Record<string, unknown>;
        return {
          code: String(n['code'] ?? ''),
          channelHint: String(n['channelHint'] ?? 'SMS'),
          title: String(n['title'] ?? ''),
          body: String(n['body'] ?? '')
        };
      })
      .filter(n => !!n.code);
  }

  private hydrateMessaging(ver: CatalogDeServiceVersion | null): void {
    if (!ver) {
      this.editHistory.set({ ...DEFAULT_HISTORY });
      this.editNotifications.set([]);
      this.historyI18n.set({});
      this.notificationI18n.set({});
      return;
    }
    this.editHistory.set(this.historyOf(ver));
    this.editNotifications.set(this.notificationsOf(ver).map(n => ({ ...n })));

    const histI18n: Record<string, LocalizedText> = {};
    const notifI18n: Record<string, { title?: LocalizedText; body?: LocalizedText }> = {};
    for (const [path, byLang] of Object.entries(ver.translations ?? {})) {
      const hist = /^configuration\.history\.(kindLabel|titleTemplate|subtitleTemplate)$/.exec(path);
      if (hist?.[1] && byLang) {
        histI18n[hist[1]] = { ...byLang };
        continue;
      }
      const notif = /^configuration\.notifications\[(.+)]\.(title|body)$/.exec(path);
      if (notif?.[1] && notif[2] && byLang) {
        const code = notif[1].toUpperCase();
        const field = notif[2] as 'title' | 'body';
        const entry = { ...(notifI18n[code] ?? {}) };
        entry[field] = { ...byLang };
        notifI18n[code] = entry;
      }
    }
    this.historyI18n.set(histI18n);
    this.notificationI18n.set(notifI18n);
  }

  private setCastingEditors(
    roles: CatalogProductRole[],
    participants: CatalogProductParticipant[],
    bindings: CatalogProductBinding[]
  ): void {
    this.editRoles.set(roles.length ? roles.map(r => ({ ...r })) : [...DEFAULT_ROLES]);
    this.editParticipants.set(
      participants.length ? participants.map(p => ({ ...p })) : [...DEFAULT_PARTICIPANTS]
    );
    const nextBindings = bindings.length ? bindings.map(b => ({ ...b })) : [...DEFAULT_BINDINGS];
    this.editBindings.set(nextBindings);
    this.bindingsJsonDraft.set(JSON.stringify(nextBindings, null, 2));
  }

  private syncBindingsJsonDraft(): void {
    this.bindingsJsonDraft.set(JSON.stringify(this.editBindings(), null, 2));
  }

  private normalizedRoles(): CatalogProductRole[] {
    return this.editRoles()
      .map(r => ({
        ...r,
        code: (r.code || '').trim().toUpperCase(),
        label: r.label?.trim() || undefined,
        selection: r.selection || 'USER',
        cardinality: r.cardinality || 'ONE_REQUIRED'
      }))
      .filter(r => !!r.code);
  }

  private normalizedParticipants(): CatalogProductParticipant[] {
    return this.editParticipants()
      .map(p => ({
        role: (p.role || '').trim().toUpperCase(),
        providerCode: (p.providerCode || '').trim().toUpperCase(),
        default: !!p.default
      }))
      .filter(p => !!p.role && !!p.providerCode);
  }

  private normalizedBindings(): CatalogProductBinding[] {
    const out: CatalogProductBinding[] = [];
    for (const b of this.editBindings()) {
      const step = (b.step || '').trim();
      if (!step) {
        continue;
      }
      const kind = b.kind || 'UI';
      const actorRole = b.actorRole ? String(b.actorRole).trim().toUpperCase() : undefined;
      const providerCode = b.providerCode ? String(b.providerCode).trim().toUpperCase() : undefined;
      if (kind === 'UI') {
        const formSource =
          b.formSource === 'BILLER_PRODUCT'
            ? 'BILLER_PRODUCT'
            : b.formSource === 'RECAP'
              ? 'RECAP'
              : 'FORM_KEY';
        if (formSource === 'BILLER_PRODUCT') {
          out.push({
            step,
            kind: 'UI',
            actorRole,
            providerCode,
            formSource: 'BILLER_PRODUCT',
            billerScreen: (b.billerScreen || 'LOOKUP').toUpperCase()
          });
        } else {
          const formKey = (b.formKey || '').trim();
          if (!formKey && !actorRole && formSource !== 'RECAP') {
            throw new Error(
              `UI step ${step} : formKey partagé OU actorRole (nomenclature product-role-provider)`
            );
          }
          out.push({
            step,
            kind: 'UI',
            actorRole,
            providerCode,
            formSource,
            ...(formKey ? { formKey } : {})
          });
        }
      } else if (kind === 'WAIT') {
        out.push({ step, kind: 'WAIT', actorRole, providerCode });
      } else if (kind === 'CAPABILITY') {
        const capability = (b.capability || b.operation || '').trim();
        if (!capability) {
          throw new Error(`capability requise pour CAPABILITY step ${step}`);
        }
        out.push({
          step,
          kind: 'CAPABILITY',
          actorRole,
          providerCode,
          capability
        });
      } else {
        const operation = (b.operation || '').trim();
        if (!operation) {
          throw new Error(`operation requise pour ${kind} step ${step}`);
        }
        out.push({
          step,
          kind,
          actorRole,
          providerCode,
          operation
        });
      }
    }
    return out;
  }

  private parseSteps(text: string): string[] {
    return text
      .split(/\r?\n/)
      .map(s => s.trim())
      .filter(s => !!s && !s.startsWith('#'));
  }

  private parseBindingsJson(text: string): CatalogProductBinding[] {
    const trimmed = (text || '').trim();
    if (!trimmed) {
      return [];
    }
    let parsed: unknown;
    try {
      parsed = JSON.parse(trimmed);
    } catch {
      throw new Error('JSON bindings invalide');
    }
    if (!Array.isArray(parsed)) {
      throw new Error('bindings doit être un tableau JSON');
    }
    return parsed as CatalogProductBinding[];
  }

  private suggestNextVersion(current?: string | null): string {
    if (!current || !/^\d+\.\d+$/.test(current)) {
      return '1.0';
    }
    const [major, minor] = current.split('.').map(Number);
    return `${major}.${minor + 1}`;
  }

  private toCamel(code: string): string {
    const parts = code.toLowerCase().split(/[_-\s]+/);
    return parts[0] + parts.slice(1).map(p => p.charAt(0).toUpperCase() + p.slice(1)).join('');
  }
}
