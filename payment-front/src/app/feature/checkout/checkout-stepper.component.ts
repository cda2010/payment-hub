import { Component, Input } from '@angular/core';
import { CHECKOUT_STEPS, CheckoutPhase, phaseIndex } from './checkout.utils';

@Component({
  selector: 'app-checkout-stepper',
  standalone: true,
  template: `
    <nav class="stepper" aria-label="Étapes du paiement">
      @for (label of steps; track label; let i = $index) {
        <div class="step" [class.active]="i === activeIndex" [class.done]="i < activeIndex">
          <span class="bullet">{{ i < activeIndex ? '✓' : i + 1 }}</span>
          <span class="label">{{ label }}</span>
        </div>
        @if (i < steps.length - 1) {
          <div class="line" [class.done]="i < activeIndex"></div>
        }
      }
    </nav>
  `,
  styles: `
    .stepper {
      display: flex;
      align-items: center;
      gap: 0;
      margin-bottom: 2rem;
      flex-wrap: wrap;
    }
    .step {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #94a3b8;
      font-size: 0.85rem;
    }
    .step.active {
      color: #0f172a;
      font-weight: 600;
    }
    .step.done {
      color: #059669;
    }
    .bullet {
      width: 1.75rem;
      height: 1.75rem;
      border-radius: 999px;
      border: 2px solid currentColor;
      display: grid;
      place-items: center;
      font-size: 0.75rem;
      flex-shrink: 0;
    }
    .step.active .bullet {
      background: #2563eb;
      border-color: #2563eb;
      color: #fff;
    }
    .step.done .bullet {
      background: #059669;
      border-color: #059669;
      color: #fff;
    }
    .line {
      flex: 1;
      min-width: 1.5rem;
      height: 2px;
      background: #e2e8f0;
      margin: 0 0.5rem;
    }
    .line.done {
      background: #059669;
    }
    @media (max-width: 640px) {
      .label { display: none; }
      .step.active .label { display: inline; }
    }
  `
})
export class CheckoutStepperComponent {
  readonly steps = CHECKOUT_STEPS;

  @Input() phase: CheckoutPhase = 'order';

  get activeIndex(): number {
    return phaseIndex(this.phase);
  }
}
