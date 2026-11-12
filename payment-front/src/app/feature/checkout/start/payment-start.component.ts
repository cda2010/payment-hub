import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ProviderService } from '../../../core/service/provider.service';
import { PaymentService } from '../../../core/service/payment.service';
import { Provider } from '../../../core/model/api.models';

@Component({
  selector: 'app-payment-start',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    DropdownModule,
    InputTextModule,
    ProgressSpinnerModule
  ],
  template: `
    <section class="wrap">
      <div class="top">
        <h1>Nouveau paiement</h1>
        <a pButton class="p-button-text" routerLink="/login" label="Administration"></a>
      </div>

      <div class="card">
        @if (loadingProviders()) {
          <p-progressSpinner [style]="{ width: '36px', height: '36px' }" strokeWidth="4" />
        } @else if (!providers().length) {
          <p class="muted">Aucun fournisseur actif disponible.</p>
        } @else {
          <form [formGroup]="form" (ngSubmit)="submit()" class="form">
            <div class="field">
              <label for="provider">Fournisseur</label>
              <p-dropdown
                inputId="provider"
                formControlName="provider"
                [options]="providerOptions()"
                optionLabel="label"
                optionValue="code"
                styleClass="w-full"
              />
            </div>
            <div class="field">
              <label for="amount">Montant</label>
              <input pInputText id="amount" type="number" formControlName="amount" min="1" class="w-full" />
            </div>
            <div class="field">
              <label for="currency">Devise</label>
              <input pInputText id="currency" formControlName="currency" class="w-full" />
            </div>
            <div class="field">
              <label for="customerReference">Référence client</label>
              <input pInputText id="customerReference" formControlName="customerReference" class="w-full" />
            </div>
            <div class="field">
              <label for="phoneNumber">Téléphone (optionnel)</label>
              <input pInputText id="phoneNumber" formControlName="phoneNumber" class="w-full" />
            </div>
            <button pButton type="submit" [disabled]="form.invalid || submitting()">
              @if (submitting()) {
                <p-progressSpinner [style]="{ width: '20px', height: '20px' }" strokeWidth="4" />
              } @else {
                Payer
              }
            </button>
          </form>
        }
        @if (error()) {
          <p class="error">{{ error() }}</p>
        }
      </div>
    </section>
  `,
  styles: `
    .wrap {
      max-width: 480px;
      margin: 2.5rem auto;
      padding: 0 1rem;
    }
    .top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    h1 { margin: 0; font-size: 1.5rem; }
    .card {
      background: #fff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 1.25rem;
    }
    .form {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-top: 0.5rem;
    }
    .field label { display: block; margin-bottom: 0.35rem; font-size: 0.875rem; color: #334155; }
    .error { color: #b91c1c; }
    .muted { color: #64748b; }
  `
})
export class PaymentStartComponent implements OnInit {
  private readonly providersApi = inject(ProviderService);
  private readonly payments = inject(PaymentService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly loadingProviders = signal(true);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly providers = signal<Provider[]>([]);
  readonly providerOptions = computed(() =>
    this.providers().map((p) => ({ code: p.code, label: `${p.libelle} (${p.code})` }))
  );

  readonly form = this.fb.nonNullable.group({
    provider: ['', Validators.required],
    amount: [1000, [Validators.required, Validators.min(1)]],
    currency: ['XOF', Validators.required],
    customerReference: ['', Validators.required],
    phoneNumber: ['']
  });

  ngOnInit(): void {
    this.providersApi.listActive().subscribe({
      next: (list) => {
        this.providers.set(list);
        if (list.length) {
          this.form.patchValue({ provider: list[0].code });
        }
        this.loadingProviders.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Impossible de charger les fournisseurs');
        this.loadingProviders.set(false);
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const v = this.form.getRawValue();
    const key = crypto.randomUUID();
    this.payments
      .createPayment(
        {
          provider: v.provider,
          amount: v.amount,
          currency: v.currency,
          customerReference: v.customerReference,
          phoneNumber: v.phoneNumber || undefined
        },
        key
      )
      .subscribe({
        next: (payment) => {
          this.submitting.set(false);
          void this.router.navigate(['/payment', payment.paymentId]);
        },
        error: (err: { message?: string }) => {
          this.error.set(err?.message ?? 'Création du paiement impossible');
          this.submitting.set(false);
        }
      });
  }
}
