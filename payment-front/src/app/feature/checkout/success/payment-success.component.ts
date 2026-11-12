import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { Payment } from '../../../core/model/api.models';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [RouterLink, ButtonModule],
  template: `
    <div class="box success">
      <h2>Paiement réussi</h2>
      @if (payment) {
        <p>{{ payment.amount }} {{ payment.currency }}</p>
        <p class="ref">Référence : {{ payment.paymentId }}</p>
        @if (payment.providerReference) {
          <p class="ref">Réf. fournisseur : {{ payment.providerReference }}</p>
        }
      }
      @if (message) {
        <p>{{ message }}</p>
      }
      <a pButton routerLink="/payment" label="Nouveau paiement"></a>
    </div>
  `,
  styles: `
    .box { text-align: center; padding: 1rem 0; }
    .success h2 { color: #15803d; }
    .ref { color: #64748b; font-size: 0.9rem; }
  `
})
export class PaymentSuccessComponent {
  @Input() payment: Payment | null = null;
  @Input() message: string | null = null;
}
