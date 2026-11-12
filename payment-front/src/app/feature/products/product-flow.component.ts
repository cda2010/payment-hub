import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { WizardStepDefinition } from '@rapidframe/core';
import { WizardComponent } from '@rapidframe/frontend-angular';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Subscription, switchMap, timer } from 'rxjs';
import { FormField, FormioSchemaDto } from '../../core/model/api.models';
import {
  CatalogItem,
  CatalogSection,
  GatewayBiller,
  GatewayBillerProduct,
  GatewayService,
} from '../../core/service/gateway.service';
import { CatalogDeServiceAdminService } from '../../core/service/catalog-de-service-admin.service';
import {
  AccessChannel,
  IntegrationScreen,
  ProductAudience,
  ProductJourneyMeta,
  channelOptionsFor,
  defaultProductStartBody,
  parseAccessChannel,
  parseProductAudience,
  productJourneyMeta,
  productRequiresAccessChannel,
  screenAccessChannel,
  screenJourneyCode,
  screenPaymentId,
} from '../../core/service/gateway.models';
import { GatewayPrimengFormComponent } from './gateway-primeng-form.component';
import { OtpInputComponent } from '../../shared/components/otp-input/otp-input.component';
import { RedirectHandlerComponent } from '../checkout/redirect-handler/redirect-handler.component';

/**
 * Parcours produit — cadre RF + stepper.
 * Hierarchy: PRODUCT → JOURNEY → CHANNEL → AUDIENCE
 */
@Component({
  selector: 'app-product-flow',
  standalone: true,
  imports: [
    RouterLink,
    ButtonModule,
    DividerModule,
    MessageModule,
    ProgressSpinnerModule,
    WizardComponent,
    GatewayPrimengFormComponent,
    OtpInputComponent,
    RedirectHandlerComponent,
  ],
  template: `
    <div class="gw-console-page product-flow">
      <div class="gw-page-header">
        <div>
          <h1>{{ title() }}</h1>
          <p class="subtitle hierarchy">
            <span class="level"><span class="lvl">PRODUCT</span> <span class="code">{{ productCode() }}</span></span>
            @if (selectedBillerCode()) {
              <span class="sep">→</span>
              <span class="level"><span class="lvl">BILLER</span> <span class="code">{{ selectedBillerCode() }}</span></span>
            }
            @if (selectedBillerProductCode()) {
              <span class="sep">→</span>
              <span class="level"><span class="lvl">BILLER_PRODUCT</span> <span class="code">{{ selectedBillerProductCode() }}</span></span>
            }
            @if (journeyCode()) {
              <span class="sep">→</span>
              <span class="level"><span class="lvl">JOURNEY</span> <span class="code">{{ journeyCode() }}</span></span>
            }
            @if (accessChannel()) {
              <span class="sep">→</span>
              <span class="level"><span class="lvl">CHANNEL</span> <span class="code">{{ accessChannel() }}</span></span>
            }
            @if (productAudience()) {
              <span class="sep">→</span>
              <span class="level"><span class="lvl">AUDIENCE</span> <span class="code">{{ productAudience() }}</span></span>
            }
          </p>
        </div>
        <div class="header-actions">
          <a pButton routerLink="/products" label="Produits" icon="pi pi-th-large" class="p-button-outlined p-button-sm"></a>
        </div>
      </div>

      <div class="card rf-panel wizard-panel">
        <div class="card-header">
          <div class="wizard-head">
            <h6>
              @if (selectingChannel()) {
                Choisir le canal d'accès
              } @else if (selectingBiller()) {
                Choisir le facturier et le produit
              } @else {
                {{ screen() ? screenTitle(screen()!) : 'Démarrage' }}
              }
            </h6>
            @if (paymentId()) {
              <span class="subtitle">Réf. {{ paymentId() }}</span>
            }
          </div>
        </div>

        <div class="panel-content">
          @if (selectingChannel()) {
            <p class="muted mb">
              Produit <strong>{{ productCode() }}</strong>
              @if (journeyCode()) {
                — journey <span class="code">{{ journeyCode() }}</span>
              }
            </p>
            <div class="channel-grid">
              @for (ch of channelChoices(); track ch.code) {
                <button
                  type="button"
                  class="channel-card"
                  [class.selected]="pendingChannel() === ch.code"
                  (click)="pendingChannel.set(ch.code)"
                >
                  <div class="channel-title">{{ ch.label }}</div>
                  <div class="channel-code">{{ ch.code }}</div>
                  <div class="channel-hint">{{ ch.hint }}</div>
                </button>
              }
            </div>
            @if (actionError()) {
              <p-message severity="error" [text]="actionError()!" styleClass="w-full mt" />
            }
            <div class="actions mt">
              <button
                pButton
                type="button"
                label="Continuer"
                icon="pi pi-arrow-right"
                [disabled]="!pendingChannel()"
                (click)="confirmChannelAndStart()"
              ></button>
              <a pButton routerLink="/products" label="Retour" class="p-button-outlined"></a>
            </div>
          } @else if (selectingBiller()) {
            <p class="muted mb">
              Sélectionnez un facturier, puis le produit à payer.
            </p>
            @if (loadingBillers()) {
              <div class="center">
                <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
                <p class="muted">Chargement du catalogue…</p>
              </div>
            } @else if (catalogSections().length) {
              @for (sec of catalogSections(); track sec.id) {
                @if (sec.title) {
                  <p class="section-label">{{ sec.title }}</p>
                }
                @if (isFilterChips(sec)) {
                  <div class="chip-row">
                    <button
                      type="button"
                      class="chip"
                      [class.selected]="!selectedCategory()"
                      (click)="onSelectCategory(null, sec)"
                    >Tous</button>
                    @for (it of sec.items; track it.id) {
                      <button
                        type="button"
                        class="chip"
                        [class.selected]="selectedCategory() === it.id"
                        (click)="onSelectCategory(it.id, sec)"
                      >{{ it.label }}</button>
                    }
                  </div>
                } @else if (isShortcutOrList(sec)) {
                  <div class="channel-grid" [class.shortcut-row]="isShortcut(sec)">
                    @for (it of sec.items; track it.id) {
                      <button
                        type="button"
                        class="channel-card"
                        [class.selected]="pendingBiller()?.code === it.id"
                        (click)="onSelectCatalogItem(it)"
                      >
                        <div class="channel-title">{{ it.label }}</div>
                        <div class="channel-code">{{ it.id }}</div>
                        @if (it.sub) {
                          <div class="channel-hint">{{ it.sub }}</div>
                        }
                      </button>
                    }
                  </div>
                  @if (!sec.items.length) {
                    <p class="muted">Aucun élément dans cette section.</p>
                  }
                }
              }
            } @else {
              <div class="channel-grid">
                @for (b of billers(); track b.code) {
                  <button
                    type="button"
                    class="channel-card"
                    [class.selected]="pendingBiller()?.code === b.code"
                    (click)="onSelectBiller(b)"
                  >
                    <div class="channel-title">{{ b.name }}</div>
                    <div class="channel-code">{{ b.code }}</div>
                    @if (b.productCount != null) {
                      <div class="channel-hint">{{ b.productCount }} produit(s)</div>
                    }
                  </button>
                }
              </div>
            }

            @if (pendingBiller(); as biller) {
              <p class="section-label">Produit — {{ biller.name }}</p>
              @if (loadingProducts()) {
                <div class="center">
                  <p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" />
                </div>
              } @else if (billerProducts().length === 0) {
                <p-message severity="warn" text="Aucun produit actif pour ce facturier" styleClass="w-full" />
              } @else {
                <div class="channel-grid">
                  @for (p of billerProducts(); track p.code) {
                    <button
                      type="button"
                      class="channel-card"
                      [class.selected]="pendingProduct()?.code === p.code"
                      (click)="onSelectProduct(p)"
                    >
                      <div class="channel-title">{{ p.name }}</div>
                      <div class="channel-code">{{ p.code }}</div>
                      @if (p.journeyCode) {
                        <div class="channel-hint">journey {{ p.journeyCode }}</div>
                      }
                    </button>
                  }
                </div>
              }
            }

            @if (actionError()) {
              <p-message severity="error" [text]="actionError()!" styleClass="w-full mt" />
            }
            <div class="actions mt">
              <button
                pButton
                type="button"
                label="Démarrer"
                icon="pi pi-play"
                [disabled]="!pendingBiller() || !pendingProduct() || busy()"
                (click)="confirmBillerAndStart()"
              ></button>
              <a pButton routerLink="/products" label="Retour" class="p-button-outlined"></a>
            </div>
          } @else {
            <rf-wizard
              class="product-rf-wizard"
              [steps]="wizardSteps"
              [activeIndex]="activeStep()"
              [linear]="true"
              footerMode="none"
            >
              @if (booting()) {
                <div class="center">
                  <p-progressSpinner strokeWidth="4" [style]="{ width: '48px', height: '48px' }" />
                  <p class="muted">Démarrage du produit…</p>
                </div>
              } @else {
                @if (screen(); as s) {
                  @switch (s.type) {
                    @case ('FORM') {
                      @if (s.message) {
                        <p-message severity="info" [text]="s.message" styleClass="mb w-full" />
                      }
                      <app-gateway-primeng-form
                        [apiFields]="formFields()"
                        [apiFormSchema]="formSchema()"
                        [initialData]="formData()"
                        [productCode]="productCode()"
                        [submitting]="busy()"
                        (submitted)="onForm($event)"
                      />
                    }
                    @case ('ENTER_OTP') {
                      <app-otp-input
                        [message]="s.message || 'Saisissez le code reçu'"
                        [length]="s.otpLength || 6"
                        [submitting]="busy()"
                        (otpSubmitted)="onOtp($event)"
                      />
                    }
                    @case ('REDIRECT') {
                      <app-redirect-handler [url]="s.url || null" [message]="s.message || null" />
                    }
                    @case ('WAIT') {
                      <div class="center">
                        <p-progressSpinner strokeWidth="4" [style]="{ width: '48px', height: '48px' }" />
                        <p>{{ s.message || 'Traitement en cours…' }}</p>
                        <p class="muted">Actualisation automatique</p>
                      </div>
                    }
                    @case ('DISPLAY_MESSAGE') {
                      <p-message severity="info" [text]="s.message || ''" styleClass="w-full" />
                    }
                    @case ('SUCCESS') {
                      <p-message
                        severity="success"
                        [text]="s.message || 'Paiement réussi'"
                        styleClass="w-full mb"
                      />
                      @if (paymentId()) {
                        <p class="muted">Référence : {{ paymentId() }}</p>
                      }
                      <p-divider />
                      <div class="actions">
                        <button pButton type="button" label="Nouveau" icon="pi pi-plus" (click)="restart()"></button>
                        <a
                          pButton
                          routerLink="/products"
                          label="Produits"
                          icon="pi pi-th-large"
                          class="p-button-outlined"
                        ></a>
                      </div>
                    }
                    @case ('FAILURE') {
                      <p-message
                        severity="error"
                        [text]="s.message || 'Paiement échoué'"
                        styleClass="w-full mb"
                      />
                      <p-divider />
                      <div class="actions">
                        <button pButton type="button" label="Réessayer" icon="pi pi-refresh" (click)="restart()"></button>
                        <a
                          pButton
                          routerLink="/products"
                          label="Produits"
                          icon="pi pi-th-large"
                          class="p-button-outlined"
                        ></a>
                      </div>
                    }
                    @default {
                      <div class="center">
                        <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
                        <p>{{ s.message || ('État : ' + s.type) }}</p>
                      </div>
                    }
                  }

                  @if (actionError()) {
                    <p-message severity="error" [text]="actionError()!" styleClass="w-full mt" />
                  }

                  @if (s.type !== 'SUCCESS' && s.type !== 'FAILURE' && paymentId()) {
                    <p-divider />
                    <button
                      pButton
                      type="button"
                      label="Annuler"
                      icon="pi pi-times"
                      class="p-button-text p-button-danger"
                      [disabled]="busy()"
                      (click)="cancel()"
                    ></button>
                  }
                } @else if (actionError()) {
                  <p-message severity="error" [text]="actionError()!" styleClass="w-full mb" />
                  <button pButton type="button" label="Réessayer" icon="pi pi-refresh" (click)="restart()"></button>
                }
              }
            </rf-wizard>
          }
        </div>
      </div>
    </div>
  `,
  styles: `
    .product-flow { max-width: 760px; }
    .code { font-family: ui-monospace, monospace; }
    .hierarchy {
      display: flex; flex-wrap: wrap; align-items: center; gap: 0.35rem 0.5rem;
      font-size: 0.875rem; color: var(--text-color-secondary, #64748b);
    }
    .hierarchy .lvl {
      font-size: 0.65rem; font-weight: 700; letter-spacing: 0.04em;
      color: #94a3b8; margin-right: 0.25rem;
    }
    .hierarchy .sep { color: #cbd5e1; }
    .card.rf-panel {
      background: var(--surface-card, #fff);
      box-sizing: border-box;
      padding: 1.5rem 1.5rem 1.25rem;
      margin-bottom: 1.25rem;
      box-shadow: var(--card-shadow, 0 2px 8px rgba(15, 23, 42, 0.06));
      border-radius: 12px;
      border: 1px solid var(--surface-border, #e2e8f0);
    }
    .card.rf-panel .card-header {
      display: flex;
      flex-direction: column;
      gap: 0.85rem;
      padding-bottom: 1rem;
      margin-bottom: 0.75rem;
      border-bottom: 1px solid var(--surface-border, #e2e8f0);
    }
    .wizard-head {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      gap: 0.75rem;
      flex-wrap: wrap;
    }
    .wizard-head h6 {
      margin: 0;
      font-size: 1.125rem;
      font-weight: 600;
      color: var(--text-color, #1e293b);
    }
    .wizard-head .subtitle {
      font-weight: 500;
      font-size: 0.85rem;
      color: var(--text-color-secondary, #64748b);
    }
    .panel-content { padding: 0.25rem 0.15rem 0.25rem; }
    .center {
      display: flex; flex-direction: column; align-items: center; gap: 0.75rem;
      padding: 1.5rem 0; text-align: center;
    }
    .muted { color: var(--text-color-secondary, #64748b); margin: 0; font-size: 0.9rem; }
    .actions { display: flex; gap: 0.75rem; flex-wrap: wrap; justify-content: center; }
    .channel-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: 0.75rem;
      margin: 0.5rem 0 1rem;
    }
    .channel-card {
      text-align: left;
      border: 1px solid var(--surface-border, #e2e8f0);
      border-radius: 10px;
      padding: 0.85rem;
      background: var(--surface-ground, #f8fafc);
      cursor: pointer;
    }
    .channel-card.selected {
      border-color: var(--primary-color, #2563eb);
      box-shadow: 0 0 0 1px var(--primary-color, #2563eb);
      background: #fff;
    }
    .channel-title { font-weight: 600; margin-bottom: 0.15rem; }
    .channel-code { font-family: ui-monospace, monospace; font-size: 0.75rem; color: #64748b; }
    .channel-hint { font-size: 0.8rem; color: #64748b; margin-top: 0.35rem; }
    .chip-row {
      display: flex; flex-wrap: wrap; gap: 0.5rem;
      margin: 0.35rem 0 1rem;
    }
    .chip {
      border: 1px solid var(--surface-border, #e2e8f0);
      border-radius: 999px;
      padding: 0.35rem 0.75rem;
      background: var(--surface-ground, #f8fafc);
      cursor: pointer;
      font-size: 0.85rem;
    }
    .chip.selected {
      border-color: var(--primary-color, #2563eb);
      background: #eff6ff;
      color: var(--primary-color, #2563eb);
      font-weight: 600;
    }
    .shortcut-row {
      grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
    }
    .section-label {
      margin: 1rem 0 0.5rem;
      font-size: 0.9rem;
      font-weight: 600;
      color: var(--text-color, #1e293b);
    }
    :host ::ng-deep .w-full { width: 100%; }
    :host ::ng-deep .mb { margin-bottom: 1rem; display: block; }
    :host ::ng-deep .mt { margin-top: 1rem; display: block; }
    :host ::ng-deep .product-rf-wizard.rf-wizard,
    :host ::ng-deep .product-rf-wizard .rf-wizard {
      gap: 1rem;
    }
    :host ::ng-deep .product-rf-wizard .rf-wizard__stepper {
      width: 100%;
    }
    :host ::ng-deep .product-rf-wizard .rf-wizard-step {
      pointer-events: none;
    }
  `,
})
export class ProductFlowComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly gateway = inject(GatewayService);
  private readonly catalog = inject(CatalogDeServiceAdminService);

  private pollSub?: Subscription;
  private paramSub?: Subscription;

  readonly wizardSteps: WizardStepDefinition[] = [
    { id: 'saisie', label: 'Saisie' },
    { id: 'validation', label: 'Validation' },
    { id: 'traitement', label: 'Traitement' },
    { id: 'resultat', label: 'Résultat' },
  ];

  readonly productCode = signal('');
  readonly title = signal('Produit');
  readonly screen = signal<IntegrationScreen | null>(null);
  readonly booting = signal(true);
  readonly busy = signal(false);
  readonly actionError = signal<string | null>(null);
  readonly paymentId = signal<string | null>(null);
  readonly formFields = signal<FormField[]>([]);
  readonly formSchema = signal<FormioSchemaDto | null>(null);
  readonly formData = signal<Record<string, unknown>>({});
  readonly selectingChannel = signal(false);
  readonly pendingChannel = signal<AccessChannel | null>(null);
  readonly accessChannel = signal<AccessChannel | null>(null);
  readonly productAudience = signal<ProductAudience | null>(null);
  readonly journeyCode = signal<string | null>(null);
  readonly journeyMeta = signal<ProductJourneyMeta | null>(null);

  readonly selectingBiller = signal(false);
  readonly loadingBillers = signal(false);
  readonly loadingProducts = signal(false);
  readonly billers = signal<GatewayBiller[]>([]);
  readonly catalogSections = signal<CatalogSection[]>([]);
  readonly selectedCategory = signal<string | null>(null);
  readonly billerProducts = signal<GatewayBillerProduct[]>([]);
  readonly pendingBiller = signal<GatewayBiller | null>(null);
  readonly pendingProduct = signal<GatewayBillerProduct | null>(null);
  readonly selectedBillerCode = signal<string | null>(null);
  readonly selectedBillerProductCode = signal<string | null>(null);

  readonly channelChoices = computed(() => channelOptionsFor(this.journeyMeta()));
  readonly activeStep = computed(() => this.stepIndexFor(this.screen()?.type, this.booting()));

  ngOnInit(): void {
    this.paramSub = this.route.paramMap.subscribe((params) => {
      const code = (params.get('code') || '').trim().toUpperCase();
      const paymentId = params.get('paymentId');
      this.productCode.set(code);
      this.title.set(code || 'Produit');
      this.stopPoll();
      this.actionError.set(null);
      if (paymentId) {
        this.selectingChannel.set(false);
        this.selectingBiller.set(false);
        this.paymentId.set(paymentId);
        this.resume(paymentId);
      } else if (code) {
        this.beginProduct(code);
      } else {
        this.booting.set(false);
        this.actionError.set('Code produit manquant');
      }
    });
  }

  ngOnDestroy(): void {
    this.paramSub?.unsubscribe();
    this.stopPoll();
  }

  confirmChannelAndStart(): void {
    const channel = this.pendingChannel();
    if (!channel) {
      return;
    }
    this.accessChannel.set(channel);
    this.selectingChannel.set(false);
    this.afterChannelResolved(this.productCode(), channel);
  }

  isFilterChips(sec: CatalogSection): boolean {
    return String(sec.displayKind || '').toUpperCase() === 'FILTER_CHIPS';
  }

  isShortcut(sec: CatalogSection): boolean {
    return String(sec.displayKind || '').toUpperCase() === 'SHORTCUT_SLIDE';
  }

  isShortcutOrList(sec: CatalogSection): boolean {
    const kind = String(sec.displayKind || '').toUpperCase();
    return (
      kind === 'SHORTCUT_SLIDE' ||
      kind === 'LEAF_LIST' ||
      kind === 'ENTRY_GRID' ||
      kind === 'SECTIONED_LIST' ||
      kind === 'DRILL_NAV'
    );
  }

  onSelectCategory(categoryId: string | null, _sec: CatalogSection): void {
    this.selectedCategory.set(categoryId);
    this.pendingBiller.set(null);
    this.pendingProduct.set(null);
    this.billerProducts.set([]);
    this.reloadCatalog();
  }

  onSelectCatalogItem(item: CatalogItem): void {
    this.onSelectBiller(this.catalogItemToBiller(item));
  }

  onSelectBiller(biller: GatewayBiller): void {
    this.pendingBiller.set(biller);
    this.pendingProduct.set(null);
    this.billerProducts.set([]);
    this.actionError.set(null);
    this.loadingProducts.set(true);
    this.gateway.listBillerProducts(biller.code).subscribe({
      next: (products) => {
        this.loadingProducts.set(false);
        this.billerProducts.set(products);
        if (products.length === 1) {
          this.pendingProduct.set(products[0]);
        }
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.loadingProducts.set(false);
        this.actionError.set(
          err?.error?.message || err?.message || 'Impossible de charger les produits',
        );
      },
    });
  }

  onSelectProduct(product: GatewayBillerProduct): void {
    this.pendingProduct.set(product);
  }

  confirmBillerAndStart(): void {
    const biller = this.pendingBiller();
    const product = this.pendingProduct();
    if (!biller || !product) {
      return;
    }
    this.selectedBillerCode.set(biller.code);
    this.selectedBillerProductCode.set(product.code);
    // product.journeyCode = mode d'enquête facture (OPEN_BILLS/PREPAID…), pas le journey catalogue.
    // Le backend le résout via billerCode + productCode → billInquiryMode.
    this.selectingBiller.set(false);
    this.actionError.set(null);
    const channel = this.accessChannel() ?? 'WEB';
    this.accessChannel.set(channel);
    this.start(this.productCode(), channel);
  }

  screenTitle(s: IntegrationScreen): string {
    switch (String(s.type || '').toUpperCase()) {
      case 'FORM':
        return s.title || 'Saisie';
      case 'ENTER_OTP':
        return 'Validation OTP';
      case 'WAIT':
        return 'En cours';
      case 'REDIRECT':
        return 'Redirection';
      case 'SUCCESS':
        return 'Succès';
      case 'FAILURE':
        return 'Échec';
      default:
        return s.title || this.productCode();
    }
  }

  restart(): void {
    const code = this.productCode();
    void this.router.navigate(['/products', code]);
    this.beginProduct(code);
  }

  onForm(data: Record<string, unknown>): void {
    this.dispatch('SUBMIT_FORM', data);
  }

  onOtp(otp: string): void {
    this.dispatch('SUBMIT_OTP', { otp });
  }

  cancel(): void {
    this.dispatch('CANCEL', {});
  }

  private beginProduct(code: string): void {
    this.screen.set(null);
    this.paymentId.set(null);
    this.booting.set(true);
    this.selectingChannel.set(false);
    this.resetBillerSelection();

    this.catalog.getPublishedCasting(code).subscribe({
      next: (casting) => {
        const meta = productJourneyMeta(code, casting);
        this.applyJourneyMeta(meta);
        this.continueBegin(code);
      },
      error: () => {
        const meta = productJourneyMeta(code);
        this.applyJourneyMeta(meta);
        this.continueBegin(code);
      },
    });
  }

  private resetBillerSelection(): void {
    this.selectingBiller.set(false);
    this.loadingBillers.set(false);
    this.loadingProducts.set(false);
    this.billers.set([]);
    this.catalogSections.set([]);
    this.selectedCategory.set(null);
    this.billerProducts.set([]);
    this.pendingBiller.set(null);
    this.pendingProduct.set(null);
    this.selectedBillerCode.set(null);
    this.selectedBillerProductCode.set(null);
  }

  private applyJourneyMeta(meta: ProductJourneyMeta): void {
    this.journeyMeta.set(meta);
    if (meta.journeyCode) {
      this.journeyCode.set(meta.journeyCode);
    }
    if (!this.productAudience() && meta.defaultAudience) {
      this.productAudience.set(meta.defaultAudience);
    }
  }

  private continueBegin(code: string): void {
    const queryChannel = parseAccessChannel(this.route.snapshot.queryParamMap.get('channel'));
    const queryAudience = parseProductAudience(this.route.snapshot.queryParamMap.get('audience'));
    if (queryAudience) {
      this.productAudience.set(queryAudience);
    } else if (!this.productAudience() && this.journeyMeta()?.defaultAudience) {
      this.productAudience.set(this.journeyMeta()!.defaultAudience);
    }
    const meta = this.journeyMeta();
    const needsChannel = productRequiresAccessChannel(code, meta);

    if (needsChannel && !queryChannel) {
      this.booting.set(false);
      this.selectingChannel.set(true);
      this.pendingChannel.set(meta?.defaultChannel ?? 'MOBILE');
      this.accessChannel.set(null);
      return;
    }

    this.selectingChannel.set(false);
    this.afterChannelResolved(code, queryChannel ?? this.accessChannel() ?? undefined);
  }

  private afterChannelResolved(code: string, channel?: AccessChannel): void {
    if (channel) {
      this.accessChannel.set(channel);
    }

    if (code === 'BILL_PAYMENT') {
      const qBiller = this.route.snapshot.queryParamMap.get('billerCode')?.trim();
      const qProduct = this.route.snapshot.queryParamMap.get('productCode')?.trim();
      const qJourney = this.route.snapshot.queryParamMap.get('journeyCode')?.trim();
      if (qBiller && qProduct) {
        this.selectedBillerCode.set(qBiller);
        this.selectedBillerProductCode.set(qProduct);
        if (qJourney) {
          this.journeyCode.set(qJourney);
        }
        this.start(code, channel);
        return;
      }
      this.enterBillerSelection();
      return;
    }

    this.start(code, channel);
  }

  private enterBillerSelection(): void {
    this.booting.set(false);
    this.selectingBiller.set(true);
    this.loadingBillers.set(true);
    this.actionError.set(null);
    this.selectedCategory.set(null);
    this.reloadCatalog(true);
  }

  private reloadCatalog(showSpinner = false): void {
    if (showSpinner) {
      this.loadingBillers.set(true);
    }
    const channel = this.accessChannel() ?? 'WEB';
    this.gateway
      .productCatalog(this.productCode() || 'BILL_PAYMENT', {
        channel,
        category: this.selectedCategory() ?? undefined,
        size: 50,
      })
      .subscribe({
        next: (layout) => {
          const raw = layout?.sections ?? [];
          // Ne garder que les sections utiles : chips (même vides) + listes/favoris non vides.
          const sections = raw.filter((s) => {
            const items = s.items ?? [];
            if (this.isFilterChips(s)) {
              return items.length > 0;
            }
            return this.isShortcutOrList(s) && items.length > 0;
          });

          const selectableCount = sections
            .filter((s) => this.isShortcutOrList(s))
            .reduce((n, s) => n + (s.items?.length ?? 0), 0);

          // Sans facturier sélectionnable → grille API classique (sinon parcours bloqué).
          if (selectableCount === 0) {
            this.fallbackListBillers();
            return;
          }

          this.loadingBillers.set(false);
          this.catalogSections.set(sections);
          this.billers.set([]);
          this.actionError.set(null);

          const leafItems = sections
            .filter((s) => this.isShortcutOrList(s) && !this.isShortcut(s))
            .flatMap((s) => s.items ?? []);
          if (leafItems.length === 1 && !this.selectedCategory()) {
            this.onSelectCatalogItem(leafItems[0]);
          }
        },
        error: () => this.fallbackListBillers(),
      });
  }

  private fallbackListBillers(): void {
    this.catalogSections.set([]);
    this.loadingBillers.set(true);
    this.gateway
      .listBillers({
        size: 50,
        category: this.selectedCategory() ?? undefined,
      })
      .subscribe({
        next: (items) => {
          this.loadingBillers.set(false);
          this.billers.set(items);
          if (items.length === 0) {
            this.actionError.set('Aucun facturier actif disponible');
          } else if (items.length === 1) {
            this.onSelectBiller(items[0]);
          }
        },
        error: (err: { error?: { message?: string }; message?: string }) => {
          this.loadingBillers.set(false);
          this.actionError.set(
            err?.error?.message || err?.message || 'Impossible de charger les facturiers',
          );
        },
      });
  }

  private catalogItemToBiller(item: CatalogItem): GatewayBiller {
    const meta = item.meta ?? {};
    const productCount =
      typeof meta['billerCount'] === 'number'
        ? meta['billerCount']
        : typeof meta['productCount'] === 'number'
          ? meta['productCount']
          : undefined;
    return {
      id: String(meta['billerId'] ?? item.id),
      code: item.id,
      name: item.label,
      description: item.sub ?? null,
      actif: true,
      productCount,
    };
  }

  private stepIndexFor(type: string | null | undefined, booting: boolean): number {
    if (booting || !type) {
      return 0;
    }
    switch (String(type).toUpperCase()) {
      case 'FORM':
      case 'DISPLAY_MESSAGE':
        return 0;
      case 'ENTER_OTP':
        return 1;
      case 'WAIT':
      case 'REDIRECT':
        return 2;
      case 'SUCCESS':
      case 'FAILURE':
        return 3;
      default:
        return 0;
    }
  }

  private start(code: string, channel?: AccessChannel): void {
    this.booting.set(true);
    this.screen.set(null);
    const resolvedChannel = channel ?? this.accessChannel() ?? undefined;
    if (resolvedChannel) {
      this.accessChannel.set(resolvedChannel);
    }
    const body = defaultProductStartBody(code, {
      channel: resolvedChannel,
      audience: this.productAudience() ?? this.journeyMeta()?.defaultAudience ?? undefined,
      journeyCode: this.journeyCode() ?? undefined,
      billerCode: this.selectedBillerCode() ?? undefined,
      billerProductCode: this.selectedBillerProductCode() ?? undefined,
    });
    this.gateway.startProduct(code, body, `front-${code}-${Date.now()}`).subscribe({
      next: (screen) => {
        this.booting.set(false);
        this.applyScreen(screen);
        const id = screenPaymentId(screen);
        if (id) {
          void this.router.navigate(['/products', code, id], { replaceUrl: true });
        }
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.booting.set(false);
        this.actionError.set(
          err?.error?.message || err?.message || `Impossible de démarrer ${code}`,
        );
        if (code === 'BILL_PAYMENT') {
          this.selectingBiller.set(true);
          // Recharger la grille si le catalogue était vide / invalidé.
          if (!this.catalogSections().length && !this.billers().length) {
            this.reloadCatalog(true);
          }
        } else if (productRequiresAccessChannel(code, this.journeyMeta())) {
          this.selectingChannel.set(true);
        }
      },
    });
  }

  private resume(paymentId: string): void {
    this.booting.set(true);
    this.gateway.getPayment(paymentId).subscribe({
      next: (screen) => {
        this.booting.set(false);
        this.applyScreen(screen);
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.booting.set(false);
        this.actionError.set(err?.error?.message || err?.message || 'Paiement introuvable');
      },
    });
  }

  private dispatch(action: string, data: Record<string, unknown>): void {
    const id = this.paymentId();
    if (!id) {
      this.actionError.set('Identifiant de paiement manquant');
      return;
    }
    this.busy.set(true);
    this.actionError.set(null);
    this.gateway.submitAction(id, action, data).subscribe({
      next: (screen) => {
        this.busy.set(false);
        this.applyScreen(screen);
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.busy.set(false);
        this.actionError.set(err?.error?.message || err?.message || 'Action refusée');
      },
    });
  }

  private applyScreen(screen: IntegrationScreen): void {
    this.screen.set(screen);
    if (screen.title) {
      this.title.set(String(screen.title));
    }
    const id = screenPaymentId(screen);
    if (id) {
      this.paymentId.set(id);
    }
    const channel = screenAccessChannel(screen);
    if (channel) {
      this.accessChannel.set(channel);
    }
    const journey = screenJourneyCode(screen);
    if (journey) {
      this.journeyCode.set(journey);
    }
    const data = screen.data ?? {};
    const biller = data['billerCode'];
    if (typeof biller === 'string' && biller.trim()) {
      this.selectedBillerCode.set(biller.trim());
    }
    const billerProduct = data['productCode'];
    if (typeof billerProduct === 'string' && billerProduct.trim()) {
      this.selectedBillerProductCode.set(billerProduct.trim());
    }

    const fields = (screen.fields ?? []).map((f) => ({
      name: String(f['name'] ?? ''),
      label: String(f['label'] ?? f['name'] ?? ''),
      type: String(f['type'] ?? 'text'),
      required: Boolean(f['required']),
      placeholder: f['placeholder'] != null ? String(f['placeholder']) : undefined,
    })) as FormField[];
    this.formFields.set(fields.filter((f) => f.name));
    this.formSchema.set((screen.formSchema as FormioSchemaDto) ?? null);
    this.formData.set({ ...data });

    this.stopPoll();
    const type = String(screen.type || '').toUpperCase();
    if (type === 'WAIT' || type === 'REDIRECT') {
      const seconds = Math.max(1, screen.pollAfterSeconds ?? 3);
      const paymentId = this.paymentId();
      if (paymentId) {
        this.pollSub = timer(seconds * 1000, seconds * 1000)
          .pipe(switchMap(() => this.gateway.getPayment(paymentId)))
          .subscribe({
            next: (next) => this.applyScreen(next),
            error: () => undefined,
          });
      }
    }
  }

  private stopPoll(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = undefined;
  }
}
