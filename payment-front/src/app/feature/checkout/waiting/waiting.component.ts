import { Component, Input, OnDestroy, OnInit, inject, output } from '@angular/core';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Subscription, switchMap, timer } from 'rxjs';
import { PaymentService } from '../../../core/service/payment.service';
import { Payment } from '../../../core/model/api.models';

@Component({
  selector: 'app-waiting',
  standalone: true,
  imports: [ProgressSpinnerModule],
  template: `
    <div class="box">
      <p-progressSpinner [style]="{ width: '48px', height: '48px' }" strokeWidth="4" />
      <p>{{ message || 'Paiement en cours de traitement…' }}</p>
      <p class="hint">Actualisation automatique toutes les {{ pollAfterSeconds }} s</p>
    </div>
  `,
  styles: `
    .box {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
      padding: 1.5rem 0;
      text-align: center;
    }
    .hint { color: #64748b; font-size: 0.875rem; margin: 0; }
  `
})
export class WaitingComponent implements OnInit, OnDestroy {
  private readonly payments = inject(PaymentService);
  private sub?: Subscription;

  @Input({ required: true }) paymentId!: string;
  @Input() pollAfterSeconds = 3;
  @Input() message: string | null = null;

  readonly updated = output<Payment>();

  ngOnInit(): void {
    const seconds = Math.max(1, this.pollAfterSeconds || 3);
    this.sub = timer(0, seconds * 1000)
      .pipe(switchMap(() => this.payments.getStatus(this.paymentId)))
      .subscribe({
        next: (payment) => this.updated.emit(payment),
        error: () => undefined
      });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
