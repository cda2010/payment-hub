import { Component, Input, OnChanges, SimpleChanges, output, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PaymentMethodType, Provider } from '../../core/model/api.models';
import { METHOD_TYPE_ORDER, methodTypeLabel, modeLabel, providerInitials } from './checkout.utils';

interface ProviderGroup {
  type: PaymentMethodType | 'OTHER';
  label: string;
  providers: Provider[];
}

@Component({
  selector: 'app-method-step',
  standalone: true,
  imports: [ButtonModule, ProgressSpinnerModule],
  template: `
    <header class="head">
      <h2>Choisissez votre moyen de paiement</h2>
      <p class="sub">Mobile Money, carte bancaire ou virement — sélectionnez un service.</p>
    </header>

    @if (loading) {
      <p-progressSpinner [style]="{ width: '40px', height: '40px' }" strokeWidth="4" />
    } @else if (!providers.length) {
      <p class="empty">Aucun moyen de paiement disponible pour le moment.</p>
    } @else {
      @for (group of groups(); track group.type) {
        <section class="group">
          <h3 class="group-title">{{ group.label }}</h3>
          <div class="grid" role="listbox" [attr.aria-label]="group.label">
            @for (p of group.providers; track p.id) {
              <button
                type="button"
                class="card"
                role="option"
                [class.selected]="selection() === p.code"
                [attr.aria-selected]="selection() === p.code"
                (click)="pick(p.code)"
              >
                <span class="avatar">{{ initials(p) }}</span>
                <span class="name">{{ p.libelle }}</span>
                <span class="badge">{{ modeLabel(p.modeExecution) }}</span>
              </button>
            }
          </div>
        </section>
      }

      @if (!selection()) {
        <p class="hint">Cliquez sur un moyen de paiement pour activer le bouton.</p>
      }

      <div class="actions">
        <button pButton type="button" class="p-button-outlined" label="Retour" (click)="back.emit()"></button>
        <button
          pButton
          type="button"
          [disabled]="!selection() || submitting"
          (click)="confirm()"
        >
          @if (submitting) {
            <p-progressSpinner [style]="{ width: '20px', height: '20px' }" strokeWidth="4" />
          } @else {
            Payer maintenant
          }
        </button>
      </div>
    }
  `,
  styles: `
    .head h2 { margin: 0 0 0.35rem; font-size: 1.35rem; }
    .sub { margin: 0 0 1.25rem; color: #64748b; }
    .group { margin-bottom: 1.25rem; }
    .group-title {
      margin: 0 0 0.65rem;
      font-size: 0.8rem;
      font-weight: 700;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: #64748b;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 0.75rem;
    }
    .card {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      gap: 0.35rem;
      padding: 1rem;
      border: 2px solid #e2e8f0;
      border-radius: 12px;
      background: #fff;
      cursor: pointer;
      text-align: left;
      transition: border-color 0.15s, box-shadow 0.15s;
    }
    .card:hover { border-color: #93c5fd; }
    .card.selected {
      border-color: #2563eb;
      box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
    }
    .avatar {
      width: 2.5rem;
      height: 2.5rem;
      border-radius: 10px;
      background: #eff6ff;
      color: #1d4ed8;
      display: grid;
      place-items: center;
      font-weight: 700;
      font-size: 0.85rem;
    }
    .name { font-weight: 600; color: #0f172a; }
    .badge {
      font-size: 0.75rem;
      color: #64748b;
      background: #f1f5f9;
      padding: 0.15rem 0.5rem;
      border-radius: 999px;
    }
    .hint { margin: 0 0 1rem; font-size: 0.875rem; color: #64748b; }
    .actions { display: flex; gap: 0.75rem; flex-wrap: wrap; }
    .empty { color: #64748b; }
  `
})
export class MethodStepComponent implements OnChanges {
  @Input() providers: Provider[] = [];
  @Input() loading = false;
  @Input() submitting = false;
  @Input() selectedCode: string | null = null;

  readonly back = output<void>();
  readonly confirmed = output<string>();
  readonly providerSelected = output<string>();

  readonly selection = signal<string | null>(null);
  readonly groups = signal<ProviderGroup[]>([]);

  readonly modeLabel = modeLabel;
  readonly initials = providerInitials;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['providers']) {
      this.groups.set(this.buildGroups(this.providers));
    }
    if (changes['selectedCode'] && this.selectedCode) {
      this.selection.set(this.selectedCode);
    }
    if (changes['providers'] && this.providers.length && !this.selection()) {
      this.pick(this.providers[0].code);
    }
  }

  pick(code: string): void {
    this.selection.set(code);
    this.providerSelected.emit(code);
  }

  confirm(): void {
    const code = this.selection();
    if (code) {
      this.confirmed.emit(code);
    }
  }

  private buildGroups(providers: Provider[]): ProviderGroup[] {
    const map = new Map<string, Provider[]>();
    for (const p of providers) {
      const key = p.paymentMethodType || 'OTHER';
      const list = map.get(key) ?? [];
      list.push(p);
      map.set(key, list);
    }
    const ordered: ProviderGroup[] = [];
    for (const type of METHOD_TYPE_ORDER) {
      const list = map.get(type);
      if (list?.length) {
        ordered.push({ type, label: methodTypeLabel(type), providers: list });
        map.delete(type);
      }
    }
    for (const [type, list] of map) {
      ordered.push({
        type: type as PaymentMethodType | 'OTHER',
        label: methodTypeLabel(type),
        providers: list
      });
    }
    return ordered;
  }
}
