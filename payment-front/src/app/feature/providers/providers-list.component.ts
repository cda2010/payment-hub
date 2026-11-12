import { Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { map } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { ProviderService } from '../../core/service/provider.service';
import { ExecutionMode, PaymentMethodType, Provider, ProviderConfigExport } from '../../core/model/api.models';
import { methodTypeLabel } from '../checkout/checkout.utils';

@Component({
  selector: 'app-providers-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    RouterLink,
    TableModule,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    DropdownModule,
    InputSwitchModule,
    ProgressSpinnerModule
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Fournisseurs</h1>
          <p class="subtitle">Configuration des moyens de paiement et des parcours</p>
        </div>
        <div class="header-actions">
          <button pButton type="button" class="p-button-outlined" label="Importer JSON" icon="pi pi-upload" (click)="openImport()"></button>
          <a pButton class="p-button-outlined" routerLink="/admin/providers/new" label="Assistant (modèle)" icon="pi pi-sitemap"></a>
          <button pButton type="button" label="Nouveau fournisseur" icon="pi pi-plus" (click)="toggleCreate()"></button>
        </div>
      </div>

      @if (showCreate()) {
        <div class="gw-panel">
          <h2>Créer un fournisseur</h2>
          <p class="subtitle" style="margin-bottom: 1rem">
            Création rapide — vous pourrez ensuite configurer environnements, APIs et parcours.
            Ou utilisez l'<a routerLink="/admin/providers/new">assistant avec modèle</a> pour un parcours pré-rempli.
          </p>
          <form [formGroup]="createForm" (ngSubmit)="create()" class="gw-form-grid">
            <div class="gw-field">
              <label>Code</label>
              <input pInputText formControlName="code" placeholder="MON_FOURNISSEUR" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Libellé</label>
              <input pInputText formControlName="libelle" placeholder="Mon Fournisseur" class="w-full" />
            </div>
            <div class="gw-field">
              <label>Type de moyen</label>
              <p-dropdown
                formControlName="paymentMethodType"
                [options]="methodTypeOptions"
                optionLabel="label"
                optionValue="value"
                styleClass="w-full"
              />
            </div>
            <div class="gw-field">
              <label>Mode d'exécution</label>
              <p-dropdown formControlName="modeExecution" [options]="modes" styleClass="w-full" />
            </div>
            <div class="gw-field">
              <label>Ordre d'affichage</label>
              <input pInputText type="number" formControlName="ordreAffichage" class="w-full" />
            </div>
            <div class="gw-field full">
              <label>Description</label>
              <textarea pInputTextarea rows="2" formControlName="description" class="w-full"></textarea>
            </div>
            <div class="gw-field full">
              <label>Actif dans le checkout</label>
              <p-inputSwitch formControlName="actif" />
            </div>
            <div class="gw-actions full">
              <button pButton type="button" class="p-button-text" label="Annuler" (click)="showCreate.set(false)"></button>
              <button pButton type="submit" label="Créer" icon="pi pi-check" [disabled]="createForm.invalid || saving()" [loading]="saving()"></button>
            </div>
          </form>
          @if (formError()) {
            <p class="gw-msg error">{{ formError() }}</p>
          }
        </div>
      }

      <input #importFile type="file" accept="application/json,.json" hidden (change)="onImportFile($event)" />

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      @if (!loading() && !providers().length && total() === 0 && !searchQuery() && !error()) {
        <div class="gw-panel empty-state">
          <i class="pi pi-sitemap empty-icon"></i>
          <h2>Aucun fournisseur</h2>
          <p class="subtitle">Créez votre premier moyen de paiement ou importez une configuration JSON.</p>
          <div class="gw-actions" style="justify-content: center">
            <button pButton type="button" label="Nouveau fournisseur" icon="pi pi-plus" (click)="toggleCreate(true)"></button>
            <a pButton class="p-button-outlined" routerLink="/admin/providers/new" label="Assistant (modèle)"></a>
          </div>
        </div>
      } @else {
        <div class="gw-panel">
          <div class="providers-toolbar">
            <input
              pInputText
              class="search-input"
              placeholder="Rechercher code, libellé, FSE…"
              [ngModel]="searchDraft()"
              (ngModelChange)="searchDraft.set($event)"
              (keyup.enter)="applySearch()"
            />
            <button
              pButton
              type="button"
              label="Rechercher"
              icon="pi pi-search"
              (click)="applySearch()"
            ></button>
            @if (searchQuery()) {
              <button
                pButton
                type="button"
                class="p-button-text"
                label="Effacer"
                icon="pi pi-times"
                (click)="clearSearch()"
              ></button>
            }
          </div>
          <p-table
            [value]="providers()"
            [lazy]="true"
            [loading]="loading()"
            [paginator]="true"
            [rows]="size()"
            [totalRecords]="total()"
            [first]="page() * size()"
            [rowsPerPageOptions]="[10, 20, 50]"
            (onLazyLoad)="onLazyLoad($event)"
            styleClass="p-datatable-sm"
            [rowHover]="true"
          >
            <ng-template pTemplate="header">
              <tr>
                <th>Code</th>
                <th>Libellé</th>
                <th>Type</th>
                <th>Mode</th>
                <th>Actif</th>
                <th></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-p>
              <tr>
                <td>{{ p.code }}</td>
                <td>{{ p.libelle }}</td>
                <td>{{ methodLabel(p.paymentMethodType) }}</td>
                <td>{{ p.modeExecution }}</td>
                <td>
                  <p-inputSwitch
                    [ngModel]="p.actif"
                    [ngModelOptions]="{ standalone: true }"
                    (onChange)="toggle(p, $event.checked)"
                  />
                </td>
                <td>
                  <div class="gw-row-actions">
                    <a pButton class="p-button-text p-button-sm" [routerLink]="['/admin/providers', p.id, 'apis']" label="Configurer"></a>
                    <button pButton type="button" class="p-button-text p-button-sm" label="Tester" (click)="test(p)"></button>
                  </div>
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr>
                <td colspan="6">
                  {{ searchQuery() ? 'Aucun fournisseur ne correspond à la recherche.' : 'Aucun fournisseur' }}
                </td>
              </tr>
            </ng-template>
          </p-table>
        </div>
        @if (testMsg()) {
          <p class="gw-msg">{{ testMsg() }}</p>
        }
        @if (importMsg()) {
          <p class="gw-msg">{{ importMsg() }}</p>
        }
      }
    </div>
  `,
  styles: `
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      padding: 3rem 1.5rem;
    }
    .empty-icon {
      font-size: 2.5rem;
      color: var(--primary-color, #2563eb);
      margin-bottom: 0.75rem;
    }
    .empty-state h2 { margin: 0 0 0.5rem; }
    .providers-toolbar {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }
    .search-input {
      min-width: min(22rem, 100%);
      flex: 1;
    }
    a[routerLink] { text-decoration: none; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})

export class ProvidersListComponent implements OnInit {
  private readonly providersApi = inject(ProviderService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly importFileRef = viewChild<ElementRef<HTMLInputElement>>('importFile');

  readonly modes: ExecutionMode[] = ['OTP', 'REDIRECT', 'PUSH', 'USSD', 'DIRECT', 'ASYNC'];
  readonly methodTypes: PaymentMethodType[] = ['MOBILE_MONEY', 'CARD', 'BANK_TRANSFER', 'OTHER'];
  readonly methodLabel = methodTypeLabel;
  readonly methodTypeOptions = this.methodTypes.map((t) => ({ label: methodTypeLabel(t), value: t }));
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly showCreate = signal(false);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly testMsg = signal<string | null>(null);
  readonly importMsg = signal<string | null>(null);
  readonly providers = signal<Provider[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(20);
  readonly searchDraft = signal('');
  readonly searchQuery = signal('');
  private searchCacheQ = '';
  private searchCacheAll: Provider[] | null = null;

  readonly createForm = this.fb.nonNullable.group({
    code: ['', Validators.required],
    libelle: ['', Validators.required],
    description: [''],
    ordreAffichage: [0],
    paymentMethodType: ['MOBILE_MONEY' as PaymentMethodType, Validators.required],
    modeExecution: ['DIRECT' as ExecutionMode, Validators.required],
    actif: [false]
  });

  ngOnInit(): void {
    // Premier chargement via onLazyLoad du p-table
  }

  toggleCreate(open?: boolean): void {
    this.showCreate.set(open ?? !this.showCreate());
    this.formError.set(null);
  }

  applySearch(): void {
    const q = this.searchDraft().trim();
    this.searchQuery.set(q);
    this.searchCacheQ = '';
    this.searchCacheAll = null;
    this.reload(0);
  }

  clearSearch(): void {
    this.searchDraft.set('');
    this.searchQuery.set('');
    this.searchCacheQ = '';
    this.searchCacheAll = null;
    this.reload(0);
  }

  reload(pageIndex: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.page.set(pageIndex);
    const q = this.searchQuery().trim();
    const size = this.size();

    if (q) {
      const cached$ =
        this.searchCacheQ === q && this.searchCacheAll
          ? of(this.searchCacheAll)
          : this.providersApi.listPage(0, 500, q).pipe(
              map((page) => {
                const raw = page.content ?? [];
                // Filtre client : marche même si l'API ignore encore ?q=
                const filtered = raw.filter((p) => this.matchesQuery(p, q));
                this.searchCacheAll = filtered;
                this.searchCacheQ = q;
                return filtered;
              })
            );

      cached$.subscribe({
        next: (all) => {
          const start = pageIndex * size;
          this.total.set(all.length);
          this.providers.set(all.slice(start, start + size));
          this.loading.set(false);
        },
        error: (err: { message?: string }) => {
          this.error.set(err?.message ?? 'Erreur de chargement');
          this.loading.set(false);
        }
      });
      return;
    }

    this.searchCacheQ = '';
    this.searchCacheAll = null;
    this.providersApi.listPage(pageIndex, size).subscribe({
      next: (page) => {
        this.providers.set(page.content ?? []);
        this.total.set(page.totalElements ?? 0);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.error.set(err?.message ?? 'Erreur de chargement');
        this.loading.set(false);
      }
    });
  }

  private matchesQuery(p: Provider, q: string): boolean {
    const needle = q.toLowerCase();
    return [p.code, p.libelle, p.codeFse, p.description]
      .filter((v): v is string => !!v && v.trim().length > 0)
      .some((v) => v.toLowerCase().includes(needle));
  }

  onLazyLoad(ev: TableLazyLoadEvent): void {
    const rows = ev.rows ?? this.size();
    const first = ev.first ?? 0;
    this.size.set(rows);
    this.reload(Math.floor(first / rows));
  }

  create(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.formError.set(null);
    const v = this.createForm.getRawValue();
    this.providersApi
      .create({
        code: v.code.trim().toUpperCase(),
        libelle: v.libelle.trim(),
        description: v.description?.trim() || null,
        actif: v.actif,
        ordreAffichage: v.ordreAffichage,
        paymentMethodType: v.paymentMethodType,
        modeExecution: v.modeExecution
      })
      .subscribe({
        next: (provider) => {
          this.saving.set(false);
          this.showCreate.set(false);
          this.createForm.reset({
            code: '',
            libelle: '',
            description: '',
            ordreAffichage: 0,
            paymentMethodType: 'MOBILE_MONEY',
            modeExecution: 'DIRECT',
            actif: false
          });
          void this.router.navigate(['/admin/providers', provider.id, 'apis']);
        },
        error: (err: { message?: string }) => {
          this.formError.set(err?.message ?? 'Création impossible');
          this.saving.set(false);
        }
      });
  }

  toggle(p: Provider, enabled: boolean): void {
    this.providersApi.setEnabled(p.id, enabled).subscribe({
      next: (updated) => {
        this.providers.update((list) => list.map((x) => (x.id === updated.id ? updated : x)));
      },
      error: (err: { message?: string }) => this.error.set(err?.message ?? 'Mise à jour impossible')
    });
  }

  test(p: Provider): void {
    this.testMsg.set(`Test de ${p.code}…`);
    this.providersApi.test(p.id).subscribe({
      next: (res) => {
        this.testMsg.set(
          `${p.code}: ${res.success ? 'OK' : 'Échec'} — ${res.message}` +
            (res.latencyMs != null ? ` (${res.latencyMs} ms)` : '')
        );
      },
      error: (err: { message?: string }) => this.testMsg.set(err?.message ?? 'Test échoué')
    });
  }

  openImport(): void {
    this.importFileRef()?.nativeElement.click();
  }

  onImportFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      try {
        const config = JSON.parse(String(reader.result)) as ProviderConfigExport;
        const overwrite = confirm(
          `Importer ${config.provider?.code ?? 'ce fournisseur'} ?\nOK = écraser si existant`
        );
        this.providersApi.importConfig({ config, overwrite }).subscribe({
          next: (provider) => {
            this.importMsg.set(`Import réussi : ${provider.code}`);
            this.reload(0);
          },
          error: (err: { message?: string }) => this.importMsg.set(err?.message ?? 'Import échoué')
        });
      } catch {
        this.importMsg.set('Fichier JSON invalide');
      } finally {
        input.value = '';
      }
    };
    reader.readAsText(file);
  }
}
