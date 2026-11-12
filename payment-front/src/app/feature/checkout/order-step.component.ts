import { Component, Input, OnInit, output, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CheckoutDraft } from './checkout.utils';

@Component({
  selector: 'app-order-step',
  standalone: true,
  imports: [ReactiveFormsModule, InputTextModule, ButtonModule],
  template: `
    <header class="head">
      <h2>Que souhaitez-vous payer ?</h2>
      <p class="sub">Saisissez le montant et votre référence. Le moyen de paiement sera choisi à l'étape suivante.</p>
    </header>

    <form [formGroup]="form" (ngSubmit)="submit()" class="form">
      <div class="full">
        <label for="amount">Montant</label>
        <input pInputText id="amount" type="number" formControlName="amount" min="1" class="w-full" />
      </div>

      <div class="full">
        <label for="currency">Devise</label>
        <input pInputText id="currency" formControlName="currency" class="w-full" />
      </div>

      <div class="full">
        <label for="customerReference">Référence commande</label>
        <input pInputText id="customerReference" formControlName="customerReference" placeholder="CMD-123456" class="w-full" />
      </div>

      <div class="full">
        <label for="phoneNumber">Téléphone (optionnel)</label>
        <input pInputText id="phoneNumber" formControlName="phoneNumber" placeholder="22670000000" class="w-full" />
        <small class="hint">Mobile Money uniquement — ignoré pour Wave</small>
      </div>

      <button pButton type="submit" class="cta" label="Continuer" [disabled]="form.invalid"></button>
    </form>
  `,
  styles: `
    .head h2 { margin: 0 0 0.35rem; font-size: 1.35rem; }
    .sub { margin: 0 0 1.5rem; color: #64748b; line-height: 1.5; }
    .form { display: flex; flex-direction: column; gap: 0.75rem; max-width: 420px; }
    .full { width: 100%; }
    .full label { display: block; margin-bottom: 0.35rem; font-size: 0.875rem; color: #334155; }
    .hint { display: block; margin-top: 0.25rem; color: #64748b; font-size: 0.8rem; }
    .cta { margin-top: 0.5rem; align-self: flex-start; min-width: 160px; }
  `
})
export class OrderStepComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  @Input() initial: CheckoutDraft | null = null;
  readonly completed = output<CheckoutDraft>();

  readonly form = this.fb.nonNullable.group({
    amount: [1000, [Validators.required, Validators.min(1)]],
    currency: ['XOF', Validators.required],
    customerReference: ['', Validators.required],
    phoneNumber: ['']
  });

  ngOnInit(): void {
    if (this.initial) {
      this.form.patchValue(this.initial);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.completed.emit(this.form.getRawValue());
  }
}
