import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PaymentService } from '../../core/service/payment.service';
import { Payment } from '../../core/model/api.models';
import { DynamicFormComponent } from '../components/dynamic-form/dynamic-form.component';
import { OtpInputComponent } from '../components/otp-input/otp-input.component';
import { RedirectHandlerComponent } from '../../feature/checkout/redirect-handler/redirect-handler.component';
import { WaitingComponent } from '../../feature/checkout/waiting/waiting.component';
import { PaymentSuccessComponent } from '../../feature/checkout/success/payment-success.component';
import { PaymentFailureComponent } from '../../feature/checkout/failure/payment-failure.component';

@Component({
  selector: 'app-payment-shell',
  standalone: true,
  imports: [
    RouterLink,
    ButtonModule,
    ProgressSpinnerModule,
    DynamicFormComponent,
    OtpInputComponent,
    RedirectHandlerComponent,
    WaitingComponent,
    PaymentSuccessComponent,
    PaymentFailureComponent
  ],
  template: `
    <section class="wrap">
      <div class="top">
        <h1>Paiement</h1>
        <a pButton class="p-button-text" routerLink="/payment" label="Retour"></a>
      </div>

      @if (loading()) {
        <p-progressSpinner [style]="{ width: '40px', height: '40px' }" strokeWidth="4" />
      } @else if (error()) {
        <div class="card">
          <p class="error">{{ error() }}</p>
          <button pButton type="button" class="p-button-outlined" label="Réessayer" (click)="reload()"></button>
        </div>
      } @else {
        @if (payment(); as p) {
          <div class="card">
            <header class="card-head">
              <h2>{{ p.provider }} — {{ p.amount }} {{ p.currency }}</h2>
              <p class="sub">{{ p.paymentId }} · {{ p.status }}</p>
            </header>
            <div class="card-body">
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
                    [message]="p.nextAction?.message || null"
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
                    (updated)="applyPayment($event)"
                  />
                }
                @case ('DISPLAY_MESSAGE') {
                  <p>{{ p.nextAction?.message }}</p>
                  <button pButton type="button" label="Continuer" (click)="continueAction()"></button>
                }
                @case ('SUCCESS') {
                  <app-payment-success [payment]="p" [message]="p.nextAction?.message || null" />
                }
                @case ('FAILURE') {
                  <app-payment-failure
                    [payment]="p"
                    [message]="p.nextAction?.message || null"
                    (retry)="onRetry()"
                  />
                }
                @default {
                  <p class="muted">En attente d'instruction ({{ p.status }}).</p>
                  <button pButton type="button" class="p-button-outlined" label="Actualiser" (click)="reload()"></button>
                }
              }
              @if (actionError()) {
                <p class="error">{{ actionError() }}</p>
              }
            </div>
          </div>
        }
      }
    </section>
  `,
  styles: `
    .wrap {
      max-width: 520px;
      margin: 2rem auto;
      padding: 0 1rem;
    }
    .top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    h1 { margin: 0; font-size: 1.35rem; }
    .card {
      background: #fff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 1.25rem;
    }
    .card-head h2 { margin: 0 0 0.25rem; font-size: 1.15rem; }
    .sub { margin: 0 0 1rem; color: #64748b; font-size: 0.9rem; }
    .card-body { padding-top: 0.25rem; }
    .error { color: #b91c1c; }
    .muted { color: #64748b; }
  `
})
export class PaymentShellComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly payments = inject(PaymentService);

  readonly loading = signal(true);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);
  readonly payment = signal<Payment | null>(null);

  private paymentId = '';

  ngOnInit(): void {
    this.paymentId = this.route.snapshot.paramMap.get('paymentId')!;
    this.reload();
  }

  actionType(): string | null {
    return this.payment()?.nextAction?.type ?? null;
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.payments.getPayment(this.paymentId).subscribe({
      next: (p) => {
        this.applyPayment(p);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Paiement introuvable');
        this.loading.set(false);
      }
    });
  }

  applyPayment(p: Payment): void {
    this.payment.set(p);
  }

  onForm(data: Record<string, unknown>): void {
    this.submitAction('SUBMIT_FORM', data);
  }

  onOtp(otp: string): void {
    this.submitAction('SUBMIT_OTP', { otp });
  }

  continueAction(): void {
    this.submitAction('CONTINUE');
  }

  onRetry(): void {
    this.busy.set(true);
    this.actionError.set(null);
    this.payments.retry(this.paymentId).subscribe({
      next: (p) => {
        this.applyPayment(p);
        this.busy.set(false);
      },
      error: (err: { message?: string }) => {
        this.actionError.set(err?.message ?? 'Relance impossible');
        this.busy.set(false);
      }
    });
  }

  private submitAction(action: string, data?: Record<string, unknown>): void {
    this.busy.set(true);
    this.actionError.set(null);
    this.payments.submitAction(this.paymentId, action, data).subscribe({
      next: (p) => {
        this.applyPayment(p);
        this.busy.set(false);
      },
      error: (err: { message?: string }) => {
        this.actionError.set(err?.message ?? 'Action impossible');
        this.busy.set(false);
      }
    });
  }
}
