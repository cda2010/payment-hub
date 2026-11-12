import { Component, Input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { Payment } from '../../../core/model/api.models';

@Component({
  selector: 'app-payment-failure',
  standalone: true,
  imports: [RouterLink, ButtonModule],
  template: `
    <div class="box failure">
      <h2>Paiement échoué</h2>
      @if (payment) {
        <p class="ref">Référence : {{ payment.paymentId }}</p>
        @if (payment.errorMessage) {
          <p class="error">{{ payment.errorCode ? payment.errorCode + ' — ' : '' }}{{ payment.errorMessage }}</p>
        }
      }
      @if (message) {
        <p class="error">{{ message }}</p>
      }
      <div class="actions">
        <button pButton type="button" class="p-button-outlined" label="Réessayer" (click)="retry.emit()"></button>
        <a pButton routerLink="/payment" label="Nouveau paiement"></a>
      </div>
    </div>
  `,
  styles: `
    .box { text-align: center; padding: 1rem 0; }
    .failure h2 { color: #b91c1c; }
    .error { color: #b91c1c; }
    .ref { color: #64748b; font-size: 0.9rem; }
    .actions { display: flex; gap: 0.75rem; justify-content: center; flex-wrap: wrap; margin-top: 1rem; }
  `
})
export class PaymentFailureComponent {
  @Input() payment: Payment | null = null;
  @Input() message: string | null = null;
  readonly retry = output<void>();
}
