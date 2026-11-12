import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { RadioButtonModule } from 'primeng/radiobutton';
import { finalize } from 'rxjs';
import { PaymentService } from '../../core/service/payment.service';
import { ProviderService } from '../../core/service/provider.service';
import {
  CatalogDeService,
  CatalogDeServiceAdminService,
  CatalogProductCasting,
  CatalogProductParticipant,
  CatalogProductRole
} from '../../core/service/catalog-de-service-admin.service';
import { Payment, Provider } from '../../core/model/api.models';
import { DynamicFormComponent } from '../../shared/components/dynamic-form/dynamic-form.component';
import { OtpInputComponent } from '../../shared/components/otp-input/otp-input.component';
import { RedirectHandlerComponent } from '../checkout/redirect-handler/redirect-handler.component';
import { WaitingComponent } from '../checkout/waiting/waiting.component';
import { PaymentSuccessComponent } from '../checkout/success/payment-success.component';
import { PaymentFailureComponent } from '../checkout/failure/payment-failure.component';

const PRODUCT_CODE = 'ACCOUNT_TRANSFER';

type TransferPhase = 'start' | 'execute' | 'result';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    ProgressSpinnerModule,
    RadioButtonModule,
    DynamicFormComponent,
    OtpInputComponent,
    RedirectHandlerComponent,
    WaitingComponent,
    PaymentSuccessComponent,
    PaymentFailureComponent
  ],
  template: `
    <div class="page">
      <header class="topbar">
        <div class="brand">
          <strong>{{ product()?.name || 'Transfert' }}</strong>
          <span class="tag">
            Produit {{ product()?.code || PRODUCT_CODE }}
            @if (product()?.currentVersion) {
              · v{{ product()!.currentVersion }}
            }
            · RapidBPMN
          </span>
        </div>
        <div class="links">
          <a pButton class="p-button-text p-button-plain" routerLink="/payment" label="Checkout"></a>
          <a pButton class="p-button-text p-button-plain" routerLink="/bills" label="Factures"></a>
          <a pButton class="p-button-text p-button-plain" routerLink="/admin/catalog-services" label="Catalogue"></a>
        </div>
      </header>

      <main class="main">
        @if (phase() === 'start') {
          <ol class="steps">
            <li class="on">1. Produit &amp; casting</li>
            <li>2. Parcours BPMN</li>
            <li>3. Résultat</li>
          </ol>
        } @else if (phase() === 'execute') {
          <ol class="steps">
            <li>1. Produit &amp; casting</li>
            <li class="on">2. Parcours BPMN</li>
            <li>3. Résultat</li>
          </ol>
        } @else {
          <ol class="steps">
            <li>1. Produit &amp; casting</li>
            <li>2. Parcours BPMN</li>
            <li class="on">3. Résultat</li>
          </ol>
        }

        @if (loading()) {
          <div class="center">
            <p-progressSpinner [style]="{ width: '40px', height: '40px' }" strokeWidth="4" />
            <p>Chargement…</p>
          </div>
        } @else {
          @switch (phase()) {
            @case ('start') {
              <section class="product-card">
                <h2>{{ product()?.name || 'Transfert de compte' }}</h2>
                <p class="desc">
                  {{ product()?.description || 'Parcours catalogue ACCOUNT_TRANSFER piloté par RapidBPMN.' }}
                </p>
                @if (productSteps().length) {
                  <div class="step-tags">
                    @for (s of productSteps(); track s) {
                      <span class="op-tag">{{ s }}</span>
                    }
                  </div>
                }
              </section>

              <form [formGroup]="startForm" (ngSubmit)="startTransfer()" class="start-form">
                <h3>Casting des rôles</h3>
                <p class="hint">Assignez un provider à chaque rôle du produit (SOURCE / DESTINATION).</p>

                @if (providersLoading()) {
                  <p-progressSpinner [style]="{ width: '28px', height: '28px' }" strokeWidth="4" />
                } @else if (!providers().length) {
                  <p class="error">Aucun provider actif pour TRANSFERT.</p>
                } @else {
                  @for (role of castingRoles(); track role.code) {
                    <div class="role-block">
                      <h4>{{ role.label || role.code }}</h4>
                      <p class="hint">Rôle <code>{{ role.code }}</code> · {{ role.selection || 'USER' }}</p>
                      <div class="provider-list">
                        @for (p of participantsFor(role.code); track p.code) {
                          <label class="provider-row" [attr.for]="role.code + '-' + p.code">
                            <p-radioButton
                              [name]="roleControlName(role.code)"
                              [value]="p.code"
                              [formControlName]="roleControlName(role.code)"
                              [inputId]="role.code + '-' + p.code"
                            />
                            <span class="provider-text">
                              <strong>{{ p.libelle || p.code }}</strong>
                              <span>{{ p.code }}</span>
                            </span>
                          </label>
                        }
                      </div>
                    </div>
                  }
                }

                <div class="fields">
                  <div class="field">
                    <label for="amount">Montant indicatif</label>
                    <input pInputText id="amount" type="number" formControlName="amount" class="w-full" />
                    <small class="field-hint">Peut être affiné dans les étapes BPMN (CAPTURE_AMOUNT)</small>
                  </div>
                  <div class="field">
                    <label for="customerReference">Référence client</label>
                    <input pInputText id="customerReference" formControlName="customerReference" class="w-full" />
                  </div>
                  <div class="field">
                    <label for="phoneNumber">Téléphone (optionnel)</label>
                    <input pInputText id="phoneNumber" formControlName="phoneNumber" class="w-full" />
                  </div>
                </div>

                <button
                  pButton
                  type="submit"
                  [label]="'Démarrer ' + (product()?.code || PRODUCT_CODE)"
                  [disabled]="busy() || startForm.invalid || !providers().length"
                ></button>
              </form>
            }

            @case ('execute') {
              @if (payment(); as p) {
                <header class="head">
                  <h2>Parcours produit</h2>
                  <p class="sub">
                    {{ p.catalogServiceCode || PRODUCT_CODE }}
                    @if (p.catalogServiceVersion) {
                      v{{ p.catalogServiceVersion }}
                    }
                    · {{ p.provider }} · étape {{ p.currentStep || '—' }}
                  </p>
                </header>

                @switch (actionType()) {
                  @case ('FORM') {
                    <app-dynamic-form
                      [fields]="p.nextAction?.fields || []"
                      [apiFormSchema]="p.nextAction?.formSchema || null"
                      [payment]="p"
                      [message]="p.nextAction?.message || null"
                      [submitting]="busy()"
                      (submitted)="onForm($event)"
                    />
                  }
                  @case ('ENTER_OTP') {
                    <app-otp-input
                      [message]="p.nextAction?.message || 'Saisissez le code reçu'"
                      [length]="p.nextAction?.otpLength || 6"
                      [submitting]="busy()"
                      (otpSubmitted)="onOtp($event)"
                    />
                  }
                  @case ('REDIRECT') {
                    <app-redirect-handler
                      [url]="p.nextAction?.url || null"
                      [message]="p.nextAction?.message || null"
                    />
                  }
                  @case ('WAIT') {
                    <app-waiting
                      [paymentId]="p.paymentId"
                      [pollAfterSeconds]="p.nextAction?.pollAfterSeconds || 3"
                      [message]="p.nextAction?.message || null"
                      (updated)="apply($event)"
                    />
                  }
                  @case ('DISPLAY_MESSAGE') {
                    <p>{{ p.nextAction?.message }}</p>
                  }
                  @default {
                    <p class="muted">Traitement ({{ p.status }})…</p>
                    <app-waiting
                      [paymentId]="p.paymentId"
                      [pollAfterSeconds]="3"
                      (updated)="apply($event)"
                    />
                  }
                }

                <div class="secondary">
                  <button pButton type="button" class="p-button-text" label="Annuler" (click)="cancel()" [disabled]="busy()"></button>
                </div>
              }
            }

            @case ('result') {
              @if (payment(); as p) {
                @if (p.status === 'SUCCESS' || actionType() === 'SUCCESS') {
                  <app-payment-success [payment]="p" [message]="p.nextAction?.message || null" />
                } @else {
                  <app-payment-failure
                    [payment]="p"
                    [message]="p.nextAction?.message || p.errorMessage || null"
                    (retry)="retry()"
                  />
                }
                <button pButton type="button" class="p-button-outlined again" label="Nouveau transfert" (click)="reset()"></button>
              }
            }
          }
        }

        @if (error()) {
          <p class="error">{{ error() }}</p>
        }
      </main>
    </div>
  `,
  styles: `
    .page { min-height: 100vh; background: #f8fafc; }
    .topbar {
      display: flex; justify-content: space-between; align-items: center; gap: 1rem;
      padding: 0.85rem 1.5rem; background: #0f172a; color: #f8fafc;
    }
    .brand { display: flex; flex-direction: column; gap: 0.15rem; }
    .tag { font-size: 0.8rem; color: #94a3b8; }
    .links a { color: #e2e8f0; }
    :host ::ng-deep .links .p-button {
      color: #e2e8f0;
    }
    .main { max-width: 720px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .steps {
      display: flex; gap: 0.75rem; list-style: none; padding: 0; margin: 0 0 1.25rem;
      font-size: 0.85rem; color: #94a3b8;
    }
    .steps li.on { color: #1d4ed8; font-weight: 600; }
    .product-card {
      background: #fff; border: 1px solid #e2e8f0; border-radius: 12px;
      padding: 1.25rem; margin-bottom: 1.25rem;
    }
    .product-card h2 { margin: 0 0 0.35rem; font-size: 1.35rem; }
    .desc { margin: 0 0 0.75rem; color: #64748b; }
    .step-tags { display: flex; flex-wrap: wrap; gap: 0.35rem; }
    .op-tag {
      font-size: 0.7rem; padding: 0.15rem 0.45rem; border-radius: 4px;
      background: #f1f5f9; color: #334155;
    }
    .start-form h3 { margin: 0 0 0.25rem; }
    .hint { margin: 0 0 0.75rem; color: #64748b; font-size: 0.85rem; }
    .provider-list { display: grid; gap: 0.5rem; margin-bottom: 1rem; }
    .provider-row {
      display: flex; gap: 0.75rem; align-items: center;
      padding: 0.75rem 1rem; border: 1px solid #e2e8f0; border-radius: 10px; background: #fff;
      cursor: pointer;
    }
    .provider-text { display: flex; gap: 0.35rem; align-items: baseline; }
    .provider-text span { color: #64748b; font-size: 0.85rem; }
    .role-block { margin-bottom: 1.25rem; }
    .role-block h4 { margin: 0 0 0.25rem; font-size: 1rem; }
    .fields { display: grid; gap: 0.75rem; margin-bottom: 1rem; }
    .field label { display: block; margin-bottom: 0.35rem; font-size: 0.875rem; color: #334155; }
    .field-hint { display: block; margin-top: 0.25rem; color: #64748b; font-size: 0.8rem; }
    .head h2 { margin: 0 0 0.25rem; }
    .sub { margin: 0 0 1rem; color: #64748b; font-size: 0.9rem; }
    .secondary, .again { margin-top: 1rem; }
    .center { display: grid; place-items: center; gap: 0.75rem; padding: 2rem; color: #64748b; }
    .muted { color: #64748b; }
    .error { color: #b91c1c; margin-top: 1rem; }
  `
})
export class TransferComponent implements OnInit {
  readonly PRODUCT_CODE = PRODUCT_CODE;

  private readonly payments = inject(PaymentService);
  private readonly providersApi = inject(ProviderService);
  private readonly catalogApi = inject(CatalogDeServiceAdminService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly phase = signal<TransferPhase>('start');
  readonly product = signal<CatalogDeService | null>(null);
  readonly productSteps = signal<string[]>([]);
  readonly casting = signal<CatalogProductCasting | null>(null);
  readonly castingRoles = signal<CatalogProductRole[]>([]);
  readonly castingParticipants = signal<CatalogProductParticipant[]>([]);
  readonly providers = signal<Provider[]>([]);
  readonly payment = signal<Payment | null>(null);

  readonly loading = signal(false);
  readonly providersLoading = signal(false);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);

  readonly startForm = this.fb.nonNullable.group({
    roleSource: ['', Validators.required],
    roleDestination: ['', Validators.required],
    amount: [1000, [Validators.required, Validators.min(1)]],
    customerReference: ['TRF-' + Date.now()],
    phoneNumber: ['']
  });

  roleControlName(code: string): 'roleSource' | 'roleDestination' {
    return code.toUpperCase() === 'DESTINATION' ? 'roleDestination' : 'roleSource';
  }

  participantsFor(roleCode: string): Provider[] {
    const allowed = new Set(
      this.castingParticipants()
        .filter((p) => p.role.toUpperCase() === roleCode.toUpperCase())
        .map((p) => p.providerCode.toUpperCase())
    );
    const all = this.providers();
    if (!allowed.size) {
      return all;
    }
    const filtered = all.filter((p) => allowed.has(p.code.toUpperCase()));
    return filtered.length ? filtered : all;
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('paymentId');
    if (id) {
      this.loadPayment(id);
      return;
    }
    this.bootstrapProduct();
  }

  startTransfer(): void {
    if (this.startForm.invalid) {
      return;
    }
    const v = this.startForm.getRawValue();
    this.busy.set(true);
    this.error.set(null);
    this.payments
      .createPayment(
        {
          provider: v.roleSource,
          catalogServiceCode: PRODUCT_CODE,
          amount: v.amount,
          currency: 'XOF',
          customerReference: v.customerReference || undefined,
          phoneNumber: v.phoneNumber || undefined,
          data: {
            catalogProductCode: PRODUCT_CODE,
            source: 'transfer-ui',
            roleAssignments: {
              SOURCE: v.roleSource,
              DESTINATION: v.roleDestination
            }
          }
        },
        crypto.randomUUID()
      )
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (p) => {
          this.apply(p);
          void this.router.navigate(['/transfer', p.paymentId], { replaceUrl: true });
        },
        error: (err: { message?: string; error?: { message?: string } }) => {
          this.error.set(err?.error?.message ?? err?.message ?? 'Impossible de démarrer le transfert produit');
        }
      });
  }

  actionType(): string | null {
    return this.payment()?.nextAction?.type ?? null;
  }

  onForm(data: Record<string, unknown>): void {
    this.submit('SUBMIT_FORM', data);
  }

  onOtp(otp: string): void {
    this.submit('SUBMIT_OTP', { otp });
  }

  apply(p: Payment): void {
    this.payment.set(p);
    this.error.set(null);
    if (this.isTerminal(p)) {
      this.phase.set('result');
    } else {
      this.phase.set('execute');
    }
  }

  cancel(): void {
    const id = this.payment()?.paymentId;
    if (!id) {
      return;
    }
    this.busy.set(true);
    this.payments.cancel(id).subscribe({
      next: (p) => {
        this.apply(p);
        this.busy.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Annulation impossible');
        this.busy.set(false);
      }
    });
  }

  retry(): void {
    const id = this.payment()?.paymentId;
    if (!id) {
      return;
    }
    this.busy.set(true);
    this.payments.retry(id).subscribe({
      next: (p) => {
        this.apply(p);
        this.busy.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Relance impossible');
        this.busy.set(false);
      }
    });
  }

  reset(): void {
    this.payment.set(null);
    this.phase.set('start');
    this.error.set(null);
    this.startForm.patchValue({
      customerReference: 'TRF-' + Date.now(),
      amount: 1000,
      phoneNumber: ''
    });
    void this.router.navigate(['/transfer']);
    this.bootstrapProduct();
  }

  private bootstrapProduct(): void {
    this.loading.set(true);
    this.error.set(null);
    this.catalogApi.listPublishedServices().subscribe({
      next: (list) => {
        const product = list.find((s) => s.code === PRODUCT_CODE) ?? null;
        this.product.set(product);
        this.loading.set(false);
        if (product?.currentVersionId) {
          this.loadProductSteps(product.id, product.currentVersionId);
        } else {
          this.productSteps.set([]);
        }
        this.loadProviders();
      },
      error: () => {
        // fallback admin list si /published indisponible
        this.catalogApi.listServices().subscribe({
          next: (list) => {
            const product =
              list.find((s) => s.code === PRODUCT_CODE && s.status === 'PUBLISHED')
              ?? list.find((s) => s.code === PRODUCT_CODE)
              ?? null;
            this.product.set(
              product ?? {
                id: '',
                code: PRODUCT_CODE,
                name: 'Transfert de compte',
                description: 'Produit catalogue ACCOUNT_TRANSFER',
                status: 'PUBLISHED'
              }
            );
            this.loading.set(false);
            if (product?.currentVersionId) {
              this.loadProductSteps(product.id, product.currentVersionId);
            } else {
              this.productSteps.set([]);
            }
            this.loadProviders();
          },
          error: () => {
            this.product.set({
              id: '',
              code: PRODUCT_CODE,
              name: 'Transfert de compte',
              description: 'Produit catalogue ACCOUNT_TRANSFER',
              status: 'PUBLISHED'
            });
            this.productSteps.set([]);
            this.loading.set(false);
            this.loadProviders();
          }
        });
      }
    });
  }

  private loadProductSteps(serviceId: string, versionId: string): void {
    this.catalogApi.listVersions(serviceId).subscribe({
      next: (versions) => {
        const ver = versions.find((v) => v.id === versionId) ?? versions[versions.length - 1];
        const raw = ver?.configuration?.['steps'];
        if (!Array.isArray(raw)) {
          this.productSteps.set([]);
          return;
        }
        this.productSteps.set(
          raw.map((item) => (typeof item === 'string' ? item : String((item as { code?: string })?.code ?? item)))
        );
      },
      error: () => this.productSteps.set([])
    });
  }

  private loadProviders(): void {
    this.providersLoading.set(true);
    this.catalogApi.getPublishedCasting(PRODUCT_CODE).subscribe({
      next: (casting) => {
        this.casting.set(casting);
        const roles = (casting.roles?.length
          ? casting.roles
          : [
              { code: 'SOURCE', label: 'Compte émetteur', selection: 'USER' as const },
              { code: 'DESTINATION', label: 'Compte destinataire', selection: 'USER' as const }
            ]
        ).filter((r) => (r.selection || 'USER') === 'USER');
        this.castingRoles.set(roles.length ? roles : [
          { code: 'SOURCE', label: 'Compte émetteur', selection: 'USER' },
          { code: 'DESTINATION', label: 'Compte destinataire', selection: 'USER' }
        ]);
        this.castingParticipants.set(casting.participants || []);
        this.loadProviderList(casting.participants || []);
      },
      error: () => {
        this.castingRoles.set([
          { code: 'SOURCE', label: 'Compte émetteur', selection: 'USER' },
          { code: 'DESTINATION', label: 'Compte destinataire', selection: 'USER' }
        ]);
        this.castingParticipants.set([]);
        this.loadProviderList([]);
      }
    });
  }

  private loadProviderList(participants: CatalogProductParticipant[]): void {
    this.providersApi.listActive('TRANSFERT').subscribe({
      next: (list) => {
        const usable = list.filter((p) => p.paymentMethodType !== 'OTHER' || p.code === 'WALLET_TRANSFER');
        const finalList = usable.length ? usable : list;
        this.providers.set(finalList);
        const defaults = new Map(
          participants.filter((p) => p.default).map((p) => [p.role.toUpperCase(), p.providerCode])
        );
        const sourceDefault = defaults.get('SOURCE') || finalList[0]?.code || '';
        const destDefault = defaults.get('DESTINATION') || finalList[0]?.code || '';
        this.startForm.patchValue({
          roleSource: this.startForm.controls.roleSource.value || sourceDefault,
          roleDestination: this.startForm.controls.roleDestination.value || destDefault
        });
        this.providersLoading.set(false);
      },
      error: (err: { message?: string }) => {
        this.providersLoading.set(false);
        this.error.set(err?.message ?? 'Providers TRANSFERT indisponibles');
      }
    });
  }

  private loadPayment(paymentId: string): void {
    this.loading.set(true);
    this.payments.getPayment(paymentId).subscribe({
      next: (p) => {
        this.apply(p);
        this.loading.set(false);
        if (!this.product()) {
          this.bootstrapProduct();
        }
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Transfert introuvable');
        this.loading.set(false);
        this.phase.set('start');
        this.bootstrapProduct();
      }
    });
  }

  private submit(action: string, data?: Record<string, unknown>): void {
    const id = this.payment()?.paymentId;
    if (!id) {
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.payments
      .submitAction(id, action, data)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (p) => this.apply(p),
        error: (err: { message?: string; error?: { message?: string } }) => {
          this.error.set(err?.error?.message ?? err?.message ?? 'Action impossible');
        }
      });
  }

  private isTerminal(p: Payment): boolean {
    return (
      p.status === 'SUCCESS' ||
      p.status === 'FAILED' ||
      p.status === 'CANCELLED' ||
      p.status === 'EXPIRED'
    );
  }
}
