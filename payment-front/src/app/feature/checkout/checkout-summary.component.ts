import { Component, Input } from '@angular/core';
import { Payment, Provider } from '../../core/model/api.models';
import { CheckoutDraft, formatMoney } from './checkout.utils';

@Component({
  selector: 'app-checkout-summary',
  standalone: true,
  template: `
    <aside class="summary">
      <p class="eyebrow">Récapitulatif</p>
      <p class="amount">{{ formatMoney(displayAmount, displayCurrency) }}</p>

      @if (draft?.customerReference) {
        <dl>
          <dt>Référence</dt>
          <dd>{{ draft!.customerReference }}</dd>
        </dl>
      }

      @if (selectedProvider) {
        <dl>
          <dt>Moyen de paiement</dt>
          <dd>{{ selectedProvider.libelle }}</dd>
        </dl>
      } @else if (payment?.provider) {
        <dl>
          <dt>Moyen de paiement</dt>
          <dd>{{ payment!.provider }}</dd>
        </dl>
      }

      @if (payment?.paymentId) {
        <dl>
          <dt>Transaction</dt>
          <dd class="mono">{{ payment!.paymentId }}</dd>
        </dl>
      }

      @if (payment?.status) {
        <dl>
          <dt>Statut</dt>
          <dd><span class="status">{{ payment!.status }}</span></dd>
        </dl>
      }

      <div class="secure">
        <span aria-hidden="true">🔒</span>
        Paiement sécurisé · chiffrement TLS
      </div>
    </aside>
  `,
  styles: `
    .summary {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 1.5rem;
      position: sticky;
      top: 1.5rem;
    }
    .eyebrow {
      margin: 0 0 0.25rem;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #64748b;
    }
    .amount {
      margin: 0 0 1.25rem;
      font-size: 1.75rem;
      font-weight: 700;
      color: #0f172a;
    }
    dl {
      margin: 0 0 0.75rem;
      display: grid;
      gap: 0.15rem;
    }
    dt {
      font-size: 0.75rem;
      color: #64748b;
    }
    dd {
      margin: 0;
      font-size: 0.95rem;
      color: #1e293b;
    }
    .mono {
      font-family: ui-monospace, monospace;
      font-size: 0.8rem;
      word-break: break-all;
    }
    .status {
      display: inline-block;
      padding: 0.15rem 0.5rem;
      border-radius: 999px;
      background: #e0f2fe;
      color: #0369a1;
      font-size: 0.8rem;
    }
    .secure {
      margin-top: 1.25rem;
      padding-top: 1rem;
      border-top: 1px solid #e2e8f0;
      font-size: 0.8rem;
      color: #64748b;
      display: flex;
      align-items: center;
      gap: 0.35rem;
    }
  `
})
export class CheckoutSummaryComponent {
  @Input() draft: CheckoutDraft | null = null;
  @Input() payment: Payment | null = null;
  @Input() selectedProvider: Provider | null = null;

  readonly formatMoney = formatMoney;

  get displayAmount(): number {
    return this.payment?.amount ?? this.draft?.amount ?? 0;
  }

  get displayCurrency(): string {
    return this.payment?.currency ?? this.draft?.currency ?? 'XOF';
  }
}
