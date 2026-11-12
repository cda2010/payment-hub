import { Component, Input, OnChanges } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-redirect-handler',
  standalone: true,
  imports: [ButtonModule, MessageModule],
  template: `
    <div class="box">
      <p-message
        severity="info"
        [text]="message || 'Redirection vers le prestataire de paiement…'"
        styleClass="w-full mb"
      />
      @if (url) {
        <p class="url">{{ url }}</p>
        <div class="actions">
          <button pButton type="button" icon="pi pi-external-link" label="Continuer" (click)="go(false)"></button>
          <button
            pButton
            type="button"
            class="p-button-outlined"
            icon="pi pi-window-maximize"
            label="Nouvel onglet"
            (click)="go(true)"
          ></button>
        </div>
      } @else {
        <p-message severity="error" text="URL de redirection manquante." styleClass="w-full" />
      }
    </div>
  `,
  styles: `
    .box { text-align: center; }
    .url {
      word-break: break-all;
      font-size: 0.85rem;
      color: var(--text-color-secondary);
      margin: 0 0 1rem;
    }
    .actions { display: flex; gap: 0.75rem; justify-content: center; flex-wrap: wrap; }
    :host ::ng-deep .w-full { width: 100%; }
    :host ::ng-deep .mb { margin-bottom: 1rem; display: block; }
  `,
})
export class RedirectHandlerComponent implements OnChanges {
  @Input() url: string | null = null;
  @Input() message: string | null = null;
  @Input() autoRedirect = true;

  private redirected = false;

  ngOnChanges(): void {
    if (this.autoRedirect && this.url && !this.redirected) {
      this.redirected = true;
      setTimeout(() => this.go(false), 800);
    }
  }

  go(newTab: boolean): void {
    if (!this.url) {
      return;
    }
    if (newTab) {
      window.open(this.url, '_blank', 'noopener,noreferrer');
    } else {
      window.location.href = this.url;
    }
  }
}
