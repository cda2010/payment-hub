import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import {
  CatalogDeService,
  CatalogDeServiceAdminService,
} from '../../core/service/catalog-de-service-admin.service';
import {
  AccessChannel,
  ProductJourneyMeta,
  audienceOptionsFor,
  channelOptionsFor,
  productJourneyMeta,
  productRequiresAccessChannel,
} from '../../core/service/gateway.models';

interface ProductCard extends CatalogDeService {
  journey: ProductJourneyMeta;
}

/**
 * Cartes produits catalogue — PRODUCT → JOURNEY → CHANNEL → AUDIENCE.
 */
@Component({
  selector: 'app-product-hub',
  standalone: true,
  imports: [ButtonModule, ProgressSpinnerModule],
  template: `
    <div class="rf-dynamic-cards gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Produits catalogue</h1>
          <p class="subtitle">
            PRODUCT → JOURNEY → CHANNEL — ex. CASH_OUT / STANDARD_CASH_OUT / MOBILE.
          </p>
        </div>
        <div class="header-actions">
          <button
            pButton
            type="button"
            label="Historique"
            icon="pi pi-history"
            class="p-button-outlined p-button-sm"
            (click)="goHistory()"
          ></button>
          <button
            pButton
            type="button"
            label="Actualiser"
            icon="pi pi-refresh"
            class="p-button-outlined p-button-sm"
            (click)="load()"
          ></button>
        </div>
      </div>

      @if (loading()) {
        <p class="rf-cards-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </p>
      } @else if (error()) {
        <div class="gw-panel">
          <p class="gw-msg error">{{ error() }}</p>
          <button pButton type="button" label="Réessayer" icon="pi pi-refresh" (click)="load()"></button>
        </div>
      } @else if (!products().length) {
        <div class="gw-panel">
          <p class="subtitle">Aucun produit PUBLISHED.</p>
          <button
            pButton
            type="button"
            label="Ouvrir le catalogue"
            icon="pi pi-book"
            (click)="goCatalog()"
          ></button>
        </div>
      } @else {
        <div class="rf-cards-grid">
          @for (p of products(); track p.code) {
            <div class="rf-card">
              <button type="button" class="rf-card-main" (click)="open(p)">
                <div class="rf-card-title">{{ p.name }}</div>
                <div class="rf-card-subtitle">
                  <span class="lvl">PRODUCT</span> {{ p.code }}
                </div>
                <dl class="rf-card-fields">
                  @if (p.journey.journeyCode) {
                    <dt>Journey</dt>
                    <dd><code>{{ p.journey.journeyCode }}</code></dd>
                  }
                  @if (p.description) {
                    <dt>Description</dt>
                    <dd>{{ p.description }}</dd>
                  }
                  @if (p.categoryName) {
                    <dt>Catégorie</dt>
                    <dd>{{ p.categoryName }}</dd>
                  }
                  @if (p.currentVersion) {
                    <dt>Version</dt>
                    <dd>v{{ p.currentVersion }}</dd>
                  }
                </dl>
              </button>
              @if (needsChannel(p)) {
                <div class="channel-row">
                  <span class="channel-label">CHANNEL</span>
                  @for (ch of channelsFor(p); track ch.code) {
                    <button
                      type="button"
                      class="channel-chip"
                      (click)="openChannel(p, ch.code)"
                    >
                      {{ ch.label }}
                    </button>
                  }
                </div>
              }
              @if (audiencesFor(p).length) {
                <div class="channel-row audience-row">
                  <span class="channel-label">AUDIENCE</span>
                  @for (a of audiencesFor(p); track a.code) {
                    <span class="channel-chip audience-chip">{{ a.label }}</span>
                  }
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: `
    .rf-dynamic-cards { padding: 1rem; }
    .rf-cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
      gap: 1rem;
    }
    .rf-card {
      text-align: left;
      border: 1px solid var(--surface-border, #dee2e6);
      border-radius: 8px;
      padding: 0;
      background: var(--surface-card, #fff);
      overflow: hidden;
      transition: box-shadow 0.15s ease;
    }
    .rf-card:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
    .rf-card-main {
      display: block;
      width: 100%;
      text-align: left;
      border: 0;
      background: transparent;
      padding: 1rem 1rem 0.75rem;
      cursor: pointer;
    }
    .rf-card-title {
      font-weight: 600;
      margin-bottom: 0.25rem;
      color: var(--text-color, #1e293b);
    }
    .rf-card-subtitle {
      color: var(--text-color-secondary, #6c757d);
      font-size: 0.875rem;
      margin-bottom: 0.5rem;
      font-family: ui-monospace, monospace;
    }
    .lvl {
      font-size: 0.65rem;
      font-weight: 700;
      letter-spacing: 0.04em;
      color: #94a3b8;
      margin-right: 0.35rem;
      font-family: system-ui, sans-serif;
    }
    .rf-card-fields {
      margin: 0;
      font-size: 0.8125rem;
    }
    .rf-card-fields dt {
      font-weight: 500;
      margin-top: 0.25rem;
      color: var(--text-color-secondary, #64748b);
    }
    .rf-card-fields dd {
      margin: 0 0 0.25rem;
      color: var(--text-color, #334155);
    }
    .rf-card-fields code {
      font-size: 0.85em;
    }
    .channel-row {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.35rem;
      padding: 0.65rem 1rem 0.9rem;
      border-top: 1px solid var(--surface-border, #e2e8f0);
      background: var(--surface-ground, #f8fafc);
    }
    .channel-label {
      font-size: 0.65rem;
      font-weight: 700;
      letter-spacing: 0.04em;
      color: #94a3b8;
      margin-right: 0.25rem;
    }
    .channel-chip {
      border: 1px solid var(--surface-border, #e2e8f0);
      border-radius: 6px;
      background: #fff;
      padding: 0.2rem 0.55rem;
      font-size: 0.75rem;
      cursor: pointer;
    }
    .channel-chip:hover {
      border-color: var(--primary-color, #2563eb);
      color: var(--primary-color, #2563eb);
    }
    .audience-chip {
      cursor: default;
      background: #f1f5f9;
    }
    .audience-chip:hover {
      border-color: var(--surface-border, #e2e8f0);
      color: inherit;
    }
    .rf-cards-loading {
      padding: 2rem;
      text-align: center;
      display: grid;
      place-items: center;
    }
    .gw-msg.error { color: var(--red-500, #b91c1c); margin-bottom: 0.75rem; }
  `,
})
export class ProductHubComponent implements OnInit {
  private readonly catalog = inject(CatalogDeServiceAdminService);
  private readonly router = inject(Router);

  readonly products = signal<ProductCard[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.catalog.listPublishedServices().subscribe({
      next: (rows: CatalogDeService[]) => {
        const list = rows ?? [];
        if (!list.length) {
          this.products.set([]);
          this.loading.set(false);
          return;
        }
        forkJoin(
          list.map((p) =>
            this.catalog.getPublishedCasting(p.code).pipe(
              map((casting) => ({
                ...p,
                journey: productJourneyMeta(p.code, casting),
              })),
              catchError(() =>
                of({
                  ...p,
                  journey: productJourneyMeta(p.code),
                }),
              ),
            ),
          ),
        ).subscribe({
          next: (cards) => {
            this.products.set(cards);
            this.loading.set(false);
          },
          error: (err: { message?: string }) => {
            this.loading.set(false);
            this.error.set(err?.message ?? 'Impossible de charger les journeys produit');
          },
        });
      },
      error: (err: { message?: string }) => {
        this.loading.set(false);
        this.error.set(err?.message ?? 'Impossible de charger le catalogue publié');
      },
    });
  }

  needsChannel(p: ProductCard): boolean {
    return productRequiresAccessChannel(p.code, p.journey);
  }

  channelsFor(p: ProductCard) {
    return channelOptionsFor(p.journey);
  }

  audiencesFor(p: ProductCard) {
    if (!p.journey.supportedAudiences.length) {
      return [];
    }
    return audienceOptionsFor(p.journey);
  }

  open(p: ProductCard): void {
    void this.router.navigate(['/products', p.code]);
  }

  openChannel(p: ProductCard, channel: AccessChannel): void {
    void this.router.navigate(['/products', p.code], { queryParams: { channel } });
  }

  goCatalog(): void {
    void this.router.navigate(['/admin/catalog-services']);
  }

  goHistory(): void {
    void this.router.navigate(['/history']);
  }
}
