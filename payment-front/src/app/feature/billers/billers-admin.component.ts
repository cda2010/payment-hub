import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { AmountPolicy, Biller, BillerProduct, JourneyCode, LookupMode, ProductField } from '../../core/service/bill.service';
import { BillerAdminService, BillerProductRequest, BillerRequest } from '../../core/service/biller-admin.service';
import { ProviderService } from '../../core/service/provider.service';
import { Provider } from '../../core/model/api.models';

@Component({
  selector: 'app-billers-admin',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    DropdownModule,
    CheckboxModule,
    ProgressSpinnerModule
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Facturiers</h1>
          <p class="subtitle">Configurer les facturiers, produits — rattachés à un fournisseur de service</p>
        </div>
        <div class="header-actions">
          <button pButton type="button" label="Nouveau facturier" (click)="startNewBiller()"></button>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      <div class="layout">
        <aside class="list-panel gw-panel">
          @if (loading()) {
            <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
          } @else {
            <ul>
              @for (item of billers(); track item.id) {
                <li [class.active]="selected()?.id === item.id" (click)="selectBiller(item)">
                  <strong>{{ item.name }}</strong>
                  <span>{{ item.code }} · {{ item.providerCode || 'sans provider' }} · {{ item.productCount }} produit(s)</span>
                </li>
              }
            </ul>
          }
        </aside>

        <section class="detail-panel gw-panel">
          @if (editingBiller()) {
            <h2>{{ selected() ? 'Modifier facturier' : 'Nouveau facturier' }}</h2>
            <form [formGroup]="billerForm" (ngSubmit)="saveBiller()" class="gw-form-grid">
              <div class="gw-field">
                <label for="billerCode">Code</label>
                <input pInputText id="billerCode" formControlName="code" class="w-full" />
              </div>
              <div class="gw-field">
                <label for="billerName">Nom</label>
                <input pInputText id="billerName" formControlName="name" class="w-full" />
              </div>
              <div class="gw-field full">
                <label for="billerProvider">Fournisseur</label>
                <p-dropdown
                  inputId="billerProvider"
                  formControlName="providerId"
                  [options]="facturierProviders()"
                  optionLabel="label"
                  optionValue="value"
                  placeholder="Sélectionner un fournisseur"
                  styleClass="w-full"
                  [filter]="true"
                />
              </div>
              <div class="gw-field">
                <label for="billerDesc">Description</label>
                <input pInputText id="billerDesc" formControlName="description" class="w-full" />
              </div>
              <div class="gw-field">
                <label for="billerCurrency">Devise</label>
                <input pInputText id="billerCurrency" formControlName="defaultCurrency" class="w-full" />
              </div>
              <div class="gw-field">
                <label for="billerOrdre">Ordre</label>
                <input pInputText id="billerOrdre" type="number" formControlName="ordreAffichage" class="w-full" />
              </div>
              <div class="gw-field full">
                <label for="billerCategory">Catégorie</label>
                <p-dropdown
                  inputId="billerCategory"
                  formControlName="categoryCode"
                  [options]="categoryOptions"
                  optionLabel="label"
                  optionValue="value"
                  placeholder="Sélectionner une catégorie"
                  styleClass="w-full"
                />
              </div>
              <div class="gw-field full check-row">
                <p-checkbox formControlName="actif" [binary]="true" inputId="billerActif" />
                <label for="billerActif">Actif</label>
              </div>
              <div class="gw-actions full">
                <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelBillerEdit()"></button>
                <button pButton type="submit" label="Enregistrer" [disabled]="busy() || billerForm.invalid"></button>
              </div>
            </form>
          } @else if (selected()) {
            <header class="detail-head">
              <div>
                <h2>{{ selected()!.name }}</h2>
                <p>
                  {{ selected()!.code }} · {{ selected()!.defaultCurrency }} ·
                  {{ selected()!.categoryLabel || selected()!.categoryCode || '—' }} ·
                  {{ selected()!.actif ? 'Actif' : 'Inactif' }} ·
                  provider {{ selected()!.providerCode || '—' }}
                  @if (selected()!.providerLibelle) {
                    ({{ selected()!.providerLibelle }})
                  }
                </p>
              </div>
              <button
                pButton
                type="button"
                icon="pi pi-pencil"
                class="p-button-rounded p-button-text"
                title="Modifier"
                aria-label="Modifier"
                (click)="editBiller()"
              ></button>
            </header>

            <div class="products-head">
              <h3>Produits</h3>
              <button
                pButton
                type="button"
                label="Ajouter un produit"
                icon="pi pi-plus"
                class="p-button-outlined"
                (click)="startNewProduct()"
              ></button>
            </div>

            @if (editingProduct()) {
              <form [formGroup]="productForm" (ngSubmit)="saveProduct()" class="gw-form-grid product-form">
                <div class="gw-field">
                  <label for="prodCode">Code</label>
                  <input pInputText id="prodCode" formControlName="code" class="w-full" />
                </div>
                <div class="gw-field">
                  <label for="prodName">Nom</label>
                  <input pInputText id="prodName" formControlName="name" class="w-full" />
                </div>
                <div class="gw-field full">
                  <label for="prodProvider">Fournisseur (optionnel)</label>
                  <p-dropdown
                    inputId="prodProvider"
                    formControlName="providerId"
                    [options]="facturierProvidersWithInherit()"
                    optionLabel="label"
                    optionValue="value"
                    placeholder="Hériter du facturier"
                    styleClass="w-full"
                    [showClear]="true"
                    [filter]="true"
                  />
                </div>
                <div class="gw-field">
                  <label for="journeyCode">Parcours</label>
                  <p-dropdown
                    inputId="journeyCode"
                    formControlName="journeyCode"
                    [options]="journeyOptions"
                    optionLabel="label"
                    optionValue="value"
                    styleClass="w-full"
                  />
                </div>
                <div class="gw-field">
                  <label for="lookupMode">Mode recherche</label>
                  <p-dropdown
                    inputId="lookupMode"
                    formControlName="lookupMode"
                    [options]="lookupOptions"
                    optionLabel="label"
                    optionValue="value"
                    styleClass="w-full"
                  />
                </div>
                <div class="gw-field">
                  <label for="amountPolicy">Politique montant</label>
                  <p-dropdown
                    inputId="amountPolicy"
                    formControlName="amountPolicy"
                    [options]="amountPolicyOptions"
                    optionLabel="label"
                    optionValue="value"
                    styleClass="w-full"
                  />
                </div>
                <div class="gw-field">
                  <label for="minAmount">Min</label>
                  <input pInputText id="minAmount" type="number" formControlName="minAmount" class="w-full" />
                </div>
                <div class="gw-field">
                  <label for="maxAmount">Max</label>
                  <input pInputText id="maxAmount" type="number" formControlName="maxAmount" class="w-full" />
                </div>
                <div class="gw-field full check-row">
                  <p-checkbox formControlName="allowOverpay" [binary]="true" inputId="allowOverpay" />
                  <label for="allowOverpay">Autoriser surpaiement</label>
                </div>
                <div class="gw-field full check-row">
                  <p-checkbox formControlName="actif" [binary]="true" inputId="prodActif" />
                  <label for="prodActif">Actif</label>
                </div>
                <div class="gw-field full">
                  <label for="lookupFieldsJson">Champs recherche (JSON)</label>
                  <textarea
                    pInputTextarea
                    id="lookupFieldsJson"
                    rows="6"
                    formControlName="lookupFieldsJson"
                    class="w-full"
                    placeholder='[{"name":"subscriberReference","label":"N° abonné","type":"text","required":true,"role":"SUBSCRIBER"}]'
                  ></textarea>
                </div>
                <div class="gw-field full">
                  <label for="confirmFieldsJson">Champs confirmation (JSON)</label>
                  <textarea pInputTextarea id="confirmFieldsJson" rows="4" formControlName="confirmFieldsJson" class="w-full"></textarea>
                </div>
                <div class="gw-field full">
                  <label for="amountFieldsJson">Champs montant (JSON)</label>
                  <textarea
                    pInputTextarea
                    id="amountFieldsJson"
                    rows="4"
                    formControlName="amountFieldsJson"
                    class="w-full"
                    placeholder='[{"name":"amount","label":"Montant","type":"number","required":true,"role":"AMOUNT"}]'
                  ></textarea>
                </div>
                <div class="gw-actions full">
                  <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancelProductEdit()"></button>
                  <button pButton type="submit" label="Enregistrer produit" [disabled]="busy()"></button>
                </div>
              </form>
            }

            <ul class="products">
              @for (p of products(); track p.id) {
                <li>
                  <div>
                    <strong>{{ p.name }}</strong>
                    <span>
                      {{ p.code }} · {{ p.journeyCode }} · {{ p.amountPolicy }} · {{ p.lookupMode }}
                      · provider {{ p.resolvedProviderCode || p.providerCode || selected()?.providerCode || '—' }}
                    </span>
                  </div>
                  <button
                    pButton
                    type="button"
                    icon="pi pi-pencil"
                    class="p-button-rounded p-button-text"
                    title="Modifier"
                    aria-label="Modifier"
                    (click)="editProduct(p)"
                  ></button>
                </li>
              } @empty {
                <li class="empty">Aucun produit</li>
              }
            </ul>
          } @else {
            <p class="empty">Sélectionnez un facturier ou créez-en un.</p>
          }
        </section>
      </div>
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: 280px 1fr; gap: 1rem; }
    @media (max-width: 900px) { .layout { grid-template-columns: 1fr; } }
    .w-full { width: 100%; }
    .full { grid-column: 1 / -1; }
    .check-row { flex-direction: row; align-items: center; gap: 0.5rem; }
    .list-panel ul { list-style: none; margin: 0; padding: 0; }
    .list-panel li {
      padding: 0.85rem 1rem; border-bottom: 1px solid #e2e8f0; cursor: pointer;
      display: flex; flex-direction: column; gap: 0.2rem;
    }
    .list-panel li span { font-size: 0.8rem; color: #64748b; }
    .list-panel li.active, .list-panel li:hover { background: #eff6ff; }
    .detail-head { display: flex; justify-content: space-between; gap: 1rem; margin-bottom: 1rem; }
    .detail-head p { margin: 0.25rem 0 0; color: #64748b; }
    .products-head { display: flex; justify-content: space-between; align-items: center; }
    .product-form { margin: 1rem 0; }
    .products { list-style: none; margin: 0; padding: 0; }
    .products li {
      display: flex; justify-content: space-between; align-items: center;
      padding: 0.75rem 0; border-bottom: 1px solid #e2e8f0;
    }
    .products li span { display: block; font-size: 0.8rem; color: #64748b; }
    .empty { color: #64748b; }
  `
})
export class BillersAdminComponent implements OnInit {
  private readonly api = inject(BillerAdminService);
  private readonly providersApi = inject(ProviderService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly billers = signal<Biller[]>([]);
  readonly selected = signal<Biller | null>(null);
  readonly products = signal<BillerProduct[]>([]);
  readonly editingBiller = signal(false);
  readonly editingProduct = signal(false);
  readonly editingProductId = signal<string | null>(null);
  readonly facturierProviders = signal<Array<{ label: string; value: string }>>([]);

  readonly journeyOptions: { label: string; value: JourneyCode }[] = [
    { label: 'Liste factures (mode 1)', value: 'OPEN_BILLS' },
    { label: 'Recherche infos (mode 2)', value: 'GET_INFOS' },
    { label: 'Saisie directe / top-up (mode 3)', value: 'PREPAID' }
  ];
  readonly lookupOptions: { label: string; value: LookupMode }[] = [
    { label: 'Abonné + n° facture', value: 'BOTH' },
    { label: 'Abonné seul', value: 'SUBSCRIBER' },
    { label: 'N° facture seul', value: 'BILL_REF' }
  ];
  readonly amountPolicyOptions: { label: string; value: AmountPolicy }[] = [
    { label: 'Montant dû exact', value: 'FULL_ONLY' },
    { label: 'Paiement partiel', value: 'PARTIAL' },
    { label: 'Montant libre', value: 'EDITABLE' }
  ];
  readonly categoryOptions: { label: string; value: string }[] = [
    { label: 'Énergie & eau', value: 'ENERGY_WATER' },
    { label: 'Télécoms & TV', value: 'TELECOM_MEDIA' },
    { label: 'Éducation', value: 'EDUCATION' },
    { label: 'État & taxes', value: 'GOVERNMENT' },
    { label: 'Assurance & santé', value: 'INSURANCE_HEALTH' },
    { label: 'Autres', value: 'OTHER' }
  ];

  readonly billerForm = this.fb.nonNullable.group({
    code: ['', Validators.required],
    name: ['', Validators.required],
    description: [''],
    defaultCurrency: ['XOF', Validators.required],
    ordreAffichage: [0 as number | null],
    categoryCode: ['OTHER', Validators.required],
    providerId: ['', Validators.required],
    actif: [true]
  });

  readonly productForm = this.fb.group({
    code: this.fb.nonNullable.control('', Validators.required),
    name: this.fb.nonNullable.control('', Validators.required),
    journeyCode: this.fb.nonNullable.control<JourneyCode>('OPEN_BILLS', Validators.required),
    lookupMode: this.fb.nonNullable.control<LookupMode>('BOTH', Validators.required),
    amountPolicy: this.fb.nonNullable.control<AmountPolicy>('FULL_ONLY', Validators.required),
    minAmount: this.fb.control<number | null>(null),
    maxAmount: this.fb.control<number | null>(null),
    allowOverpay: this.fb.nonNullable.control(false),
    actif: this.fb.nonNullable.control(true),
    providerId: this.fb.control<string | null>(null),
    lookupFieldsJson: this.fb.nonNullable.control('[]'),
    confirmFieldsJson: this.fb.nonNullable.control('[]'),
    amountFieldsJson: this.fb.nonNullable.control('[]')
  });

  ngOnInit(): void {
    this.loadFacturierProviders();
    this.reload();
  }

  facturierProvidersWithInherit(): Array<{ label: string; value: string | null }> {
    return [{ label: '— Hériter du facturier —', value: null }, ...this.facturierProviders()];
  }

  private loadFacturierProviders(): void {
    this.providersApi.list().subscribe({
      next: (list: Provider[]) => {
        this.facturierProviders.set(
          list
            .filter((p) => p.actif)
            .map((p) => ({ label: `${p.codeFse || p.code} — ${p.libelle}`, value: p.id }))
        );
      },
      error: () => this.error.set('Impossible de charger les fournisseurs')
    });
  }

  reload(): void {
    this.loading.set(true);
    this.api.list().subscribe({
      next: (list) => {
        this.billers.set(list);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Chargement impossible');
        this.loading.set(false);
      }
    });
  }

  selectBiller(b: Biller): void {
    this.selected.set(b);
    this.editingBiller.set(false);
    this.editingProduct.set(false);
    this.editingProductId.set(null);
    this.api.listProducts(b.id).subscribe({
      next: (products) => this.products.set(products),
      error: (err: { message?: string }) => this.error.set(err?.message ?? 'Produits indisponibles')
    });
  }

  startNewBiller(): void {
    this.selected.set(null);
    this.products.set([]);
    this.editingBiller.set(true);
    const defaultProvider = this.facturierProviders()[0]?.value ?? '';
    this.billerForm.reset({
      code: '',
      name: '',
      description: '',
      defaultCurrency: 'XOF',
      ordreAffichage: this.billers().length + 1,
      categoryCode: 'OTHER',
      providerId: defaultProvider,
      actif: true
    });
  }

  editBiller(): void {
    const b = this.selected();
    if (!b) return;
    this.editingBiller.set(true);
    this.billerForm.reset({
      code: b.code,
      name: b.name,
      description: b.description ?? '',
      defaultCurrency: b.defaultCurrency,
      ordreAffichage: b.ordreAffichage ?? 0,
      categoryCode: b.categoryCode ?? 'OTHER',
      providerId: b.providerId ?? '',
      actif: b.actif
    });
  }

  cancelBillerEdit(): void {
    this.editingBiller.set(false);
  }

  saveBiller(): void {
    if (this.billerForm.invalid) return;
    const v = this.billerForm.getRawValue();
    const body: BillerRequest = {
      code: v.code,
      name: v.name,
      description: v.description || undefined,
      actif: v.actif,
      defaultCurrency: v.defaultCurrency,
      ordreAffichage: v.ordreAffichage,
      categoryCode: v.categoryCode,
      providerId: v.providerId
    };
    this.busy.set(true);
    const selected = this.selected();
    const req$ = selected ? this.api.update(selected.id, body) : this.api.create(body);
    req$.subscribe({
      next: (saved) => {
        this.busy.set(false);
        this.editingBiller.set(false);
        this.reload();
        this.selectBiller(saved);
      },
      error: (err: { message?: string }) => {
        this.busy.set(false);
        this.error.set(err?.message ?? 'Enregistrement impossible');
      }
    });
  }

  startNewProduct(): void {
    this.editingProduct.set(true);
    this.editingProductId.set(null);
    this.productForm.reset({
      code: '',
      name: '',
      journeyCode: 'OPEN_BILLS',
      lookupMode: 'BOTH',
      amountPolicy: 'FULL_ONLY',
      minAmount: 100,
      maxAmount: null,
      allowOverpay: false,
      actif: true,
      providerId: null,
      lookupFieldsJson: '[]',
      confirmFieldsJson: '[]',
      amountFieldsJson: '[]'
    });
  }

  editProduct(p: BillerProduct): void {
    this.editingProduct.set(true);
    this.editingProductId.set(p.id);
    this.productForm.reset({
      code: p.code,
      name: p.name,
      journeyCode: p.journeyCode,
      lookupMode: p.lookupMode,
      amountPolicy: p.amountPolicy,
      minAmount: p.minAmount,
      maxAmount: p.maxAmount,
      allowOverpay: p.allowOverpay,
      actif: p.actif,
      providerId: p.providerId ?? null,
      lookupFieldsJson: this.stringifyFields(p.lookupFields),
      confirmFieldsJson: this.stringifyFields(p.confirmFields),
      amountFieldsJson: this.stringifyFields(p.amountFields)
    });
  }

  cancelProductEdit(): void {
    this.editingProduct.set(false);
    this.editingProductId.set(null);
  }

  saveProduct(): void {
    const biller = this.selected();
    if (!biller || this.productForm.invalid) return;
    const v = this.productForm.getRawValue();
    let lookupFields: ProductField[];
    let confirmFields: ProductField[];
    let amountFields: ProductField[];
    try {
      lookupFields = this.parseFields(v.lookupFieldsJson ?? '[]');
      confirmFields = this.parseFields(v.confirmFieldsJson ?? '[]');
      amountFields = this.parseFields(v.amountFieldsJson ?? '[]');
    } catch {
      this.error.set('JSON des champs UI invalide');
      return;
    }
    const body: BillerProductRequest = {
      code: v.code!,
      name: v.name!,
      actif: !!v.actif,
      lookupMode: v.lookupMode!,
      amountPolicy: v.amountPolicy!,
      minAmount: v.minAmount,
      maxAmount: v.maxAmount,
      allowOverpay: !!v.allowOverpay,
      journeyCode: v.journeyCode!,
      paymentFlowId: null,
      ordreAffichage: this.products().length + 1,
      providerId: v.providerId || null,
      lookupFields,
      confirmFields,
      amountFields
    };
    this.busy.set(true);
    const productId = this.editingProductId();
    const req$ = productId
      ? this.api.updateProduct(productId, body)
      : this.api.createProduct(biller.id, body);
    req$.subscribe({
      next: () => {
        this.busy.set(false);
        this.cancelProductEdit();
        this.selectBiller(biller);
        this.reload();
      },
      error: (err: { message?: string }) => {
        this.busy.set(false);
        this.error.set(err?.message ?? 'Enregistrement produit impossible');
      }
    });
  }

  private stringifyFields(fields?: ProductField[]): string {
    return JSON.stringify(fields ?? [], null, 2);
  }

  private parseFields(raw: string): ProductField[] {
    const parsed = JSON.parse(raw || '[]');
    if (!Array.isArray(parsed)) {
      throw new Error('Expected array');
    }
    return (parsed as ProductField[]).map((field) => ({
      ...field,
      role: this.normalizeFieldRole(field.role)
    }));
  }

  /** Alias Omnicanal → rôles gateway (REFERENCE → BILL_REF, MONTANT → AMOUNT, …). */
  private normalizeFieldRole(role: ProductField['role'] | string | null | undefined): ProductField['role'] {
    if (role == null || role === '') {
      return 'EXTRA';
    }
    const n = String(role).trim().toUpperCase().replace(/[-\s]/g, '_');
    switch (n) {
      case 'SUBSCRIBER':
      case 'ABONNE':
      case 'SUBSCRIBER_REF':
      case 'SUBSCRIBERREFERENCE':
        return 'SUBSCRIBER';
      case 'BILL_REF':
      case 'BILLREF':
      case 'BILL_REFERENCE':
      case 'BILLREFERENCE':
      case 'REFERENCE':
      case 'REF':
      case 'METER':
      case 'METERNUM':
      case 'METER_NUMBER':
      case 'METERNUMBER':
        return 'BILL_REF';
      case 'AMOUNT':
      case 'MONTANT':
      case 'AMOUNT_DUE':
      case 'AMOUNTDUE':
        return 'AMOUNT';
      case 'EXTRA':
      case 'OTHER':
      case 'AUTRE':
        return 'EXTRA';
      default:
        return (['SUBSCRIBER', 'BILL_REF', 'AMOUNT', 'EXTRA'].includes(n)
          ? n
          : 'EXTRA') as ProductField['role'];
    }
  }
}
