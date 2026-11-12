
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ProviderService } from '../../core/service/provider.service';
import { Provider } from '../../core/model/api.models';
import { methodTypeLabel } from '../checkout/checkout.utils';

@Component({
  selector: 'app-provider-detail',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ButtonModule, ProgressSpinnerModule],
  template: `
    <div class="gw-console-page">
      @if (loading()) {
        <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" /></div>
      } @else if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
        <a pButton class="p-button-text" routerLink="/admin/providers" label="Retour"></a>
      } @else {
        @if (provider(); as p) {
          <div class="gw-page-header">
            <div>
              <a pButton class="p-button-text p-button-sm" routerLink="/admin/providers" label="← Fournisseurs"></a>
              <h1>{{ p.libelle }}</h1>
              <p class="subtitle">Code {{ p.code }} · {{ methodLabel(p.paymentMethodType) }} · {{ p.modeExecution }}</p>
              @if (p.codeFse) {
                <p class="subtitle">APIs, méthodes, orchestration, général, environnements et authentification lus depuis l'omnicanal (FSE {{ p.codeFse }}).</p>
              }
            </div>
            <div class="header-actions">
              <button pButton type="button" class="p-button-outlined" label="Exporter JSON" icon="pi pi-download" (click)="exportConfig()"></button>
              @if (canSeedOmnicanal()) {
                <button
                  pButton
                  type="button"
                  class="p-button-outlined"
                  label="Enregistrer les méthodes Omnicanal"
                  icon="pi pi-save"
                  [loading]="seeding()"
                  (click)="seedOmnicanal()"
                ></button>
              }
              <button pButton type="button" label="Tester la connectivité" icon="pi pi-bolt" (click)="runTest()"></button>
            </div>
          </div>

          @if (testMsg()) {
            <p class="gw-msg">{{ testMsg() }}</p>
          }

          <nav class="gw-tabs-nav">
            @for (tab of tabs; track tab.path) {
              <a
                [routerLink]="['/admin/providers', p.id, tab.path]"
                routerLinkActive="active"
                [routerLinkActiveOptions]="{ exact: true }"
              >{{ tab.label }}</a>
            }
          </nav>

          <router-outlet />
        }
      }
    </div>
  `,
  styles: `
    a[routerLink] { text-decoration: none; }
    .gw-tabs-nav a {
      padding: 0.45rem 0.85rem;
      border-radius: 6px;
      color: var(--text-color-secondary, #64748b);
      font-size: 0.9rem;
      font-weight: 500;
    }
    .gw-tabs-nav a.active {
      background: var(--primary-color, #2563eb);
      color: #fff;
    }
  `
})

export class ProviderDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly providersApi = inject(ProviderService);

  readonly tabs = [
    { path: 'apis', label: 'APIs & Méthodes' },
    { path: 'orchestration', label: 'Orchestration' },
    { path: 'general', label: 'Général' },
    { path: 'environments', label: 'Environnements' },
    { path: 'authentication', label: 'Authentification' },
    { path: 'flows', label: 'Flows' },
    { path: 'mappings', label: 'Mappings' },
    { path: 'webhooks', label: 'Webhooks' }
  ];

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly testMsg = signal<string | null>(null);
  readonly seeding = signal(false);
  readonly provider = signal<Provider | null>(null);
  readonly methodLabel = methodTypeLabel;

  canSeedOmnicanal(): boolean {
    const p = this.provider();
    if (!p) return false;
    return p.codeFse === '0011' || p.codeFse === '0004'
      || p.code === 'CORIS_MONEY' || p.code === 'CORIS_BANK';
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.providersApi.get(id).subscribe({
      next: (p) => {
        this.provider.set(p);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Fournisseur introuvable');
        this.loading.set(false);
      }
    });
  }

  runTest(): void {
    const p = this.provider();
    if (!p) {
      return;
    }
    this.testMsg.set('Test en cours…');
    this.providersApi.test(p.id).subscribe({
      next: (res) =>
        this.testMsg.set(
          `${res.success ? 'OK' : 'Échec'} — ${res.message}` +
            (res.latencyMs != null ? ` (${res.latencyMs} ms)` : '')
        ),
      error: (err: { message?: string }) => this.testMsg.set(err?.message ?? 'Test échoué')
    });
  }

  exportConfig(): void {
    const p = this.provider();
    if (!p) {
      return;
    }
    this.providersApi.exportConfig(p.id).subscribe({
      next: (config) => {
        const blob = new Blob([JSON.stringify(config, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${p.code}-config.json`;
        a.click();
        URL.revokeObjectURL(url);
        this.testMsg.set(`Configuration exportée (${p.code})`);
      },
      error: (err: { message?: string }) => this.testMsg.set(err?.message ?? 'Export échoué')
    });
  }

  seedOmnicanal(): void {
    const p = this.provider();
    if (!p) {
      return;
    }
    this.seeding.set(true);
    this.providersApi.seedOmnicanalConnectivity(p.id).subscribe({
      next: (res) => {
        this.seeding.set(false);
        this.testMsg.set(
          `Méthodes Omnicanal enregistrées → ${res.environmentCode}: ${res.apisUpserted} API(s) (${res.operationCodes.slice(0, 5).join(', ')}${res.operationCodes.length > 5 ? '…' : ''})`
        );
      },
      error: (err: { message?: string }) => {
        this.seeding.set(false);
        this.testMsg.set(err?.message ?? 'Import Omnicanal échoué');
      }
    });
  }
}
