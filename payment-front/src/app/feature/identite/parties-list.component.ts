import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../core/auth/auth.service';
import { AccountAlias, AliasService } from '../../core/service/alias.service';
import {
  FiltreParties,
  IdentityService,
  KINDS_PARTY,
  PageRequeteParties,
  Party,
  PartyKind
} from '../../core/service/identity.service';
import { PartyFormDialogComponent } from './party-form-dialog.component';

const STATUTS = [
  { value: true, label: 'Actif' },
  { value: false, label: 'Inactif' }
];

/** Référentiel Identity : parties (personnes / organisations) avec recherche avancée. */
@Component({
  selector: 'app-parties-list',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    ButtonModule,
    DropdownModule,
    InputTextModule,
    TableModule,
    TagModule,
    PartyFormDialogComponent
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Identités</h1>
          <p class="subtitle">
            Parties du référentiel — chaque identité a des profils d’offre et des alias de comptes
            @if (!loading()) {
              · <strong>{{ total() }}</strong> résultat(s)
            }
          </p>
        </div>
        <div class="header-actions">
          @if (peutEcrire()) {
            <button pButton type="button" icon="pi pi-plus" label="Nouvelle identité" (click)="ouvrirForm(null)"></button>
          }
        </div>
      </div>

      <form class="gw-panel filtres" (ngSubmit)="rechercher()">
        <input
          pInputText
          name="texte"
          placeholder="Code, nom, téléphone, e-mail…"
          [(ngModel)]="filtre.texte"
        />
        <p-dropdown
          name="kind"
          [options]="kinds"
          optionLabel="label"
          optionValue="value"
          placeholder="Type"
          [showClear]="true"
          [(ngModel)]="filtre.kind"
          styleClass="filtre-dd"
        />
        <p-dropdown
          name="active"
          [options]="statuts"
          optionLabel="label"
          optionValue="value"
          placeholder="Statut"
          [showClear]="true"
          [(ngModel)]="filtre.active"
          styleClass="filtre-dd"
        />
        <input
          pInputText
          name="tenantId"
          placeholder="Tenant"
          [(ngModel)]="filtre.tenantId"
          class="tenant"
        />
        <button pButton type="submit" icon="pi pi-search" label="Rechercher" [disabled]="loading()"></button>
        <button pButton type="button" class="p-button-text" label="Effacer" (click)="effacer()"></button>
      </form>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      <div class="gw-panel">
        <p-table
          [value]="parties()"
          [lazy]="true"
          (onLazyLoad)="changerPage($event)"
          [paginator]="true"
          [first]="first"
          [rows]="taille"
          [totalRecords]="total()"
          [rowsPerPageOptions]="[25, 50, 100, 200]"
          [loading]="loading()"
          [rowHover]="true"
          dataKey="id"
          styleClass="p-datatable-sm"
          currentPageReportTemplate="{first} – {last} sur {totalRecords}"
          [showCurrentPageReport]="true"
        >
          <ng-template pTemplate="header">
            <tr>
              <th pSortableColumn="code">Code <p-sortIcon field="code" /></th>
              <th pSortableColumn="displayName">Nom <p-sortIcon field="displayName" /></th>
              <th pSortableColumn="kind">Type <p-sortIcon field="kind" /></th>
              <th pSortableColumn="phone">Téléphone <p-sortIcon field="phone" /></th>
              <th pSortableColumn="email">E-mail <p-sortIcon field="email" /></th>
              <th pSortableColumn="tenantId">Tenant <p-sortIcon field="tenantId" /></th>
              <th pSortableColumn="active">Statut <p-sortIcon field="active" /></th>
              <th></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-p>
            <tr [class.inactif]="!p.active">
              <td class="code">
                <a class="lien" [routerLink]="['/admin/identite/parties', p.id]">{{ p.code }}</a>
              </td>
              <td>
                <div>{{ p.displayName }}</div>
                @if (aliasesDe(p.id); as als) {
                  @if (als.length) {
                    <div class="aliases">
                      @for (a of als; track a.id) {
                        <span class="alias" [title]="a.aliasType + ' · ' + a.externalRef">{{ a.aliasCode }}</span>
                      }
                    </div>
                  }
                }
              </td>
              <td><p-tag [value]="kindLabel(p.kind)" [severity]="p.kind === 'ORGANIZATION' ? 'info' : 'success'" /></td>
              <td>{{ p.phone || '—' }}</td>
              <td>{{ p.email || '—' }}</td>
              <td>{{ p.tenantId }}</td>
              <td>
                <p-tag [value]="p.active ? 'Actif' : 'Inactif'" [severity]="p.active ? 'success' : 'secondary'" />
              </td>
              <td class="actions">
                @if (peutEcrire()) {
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm"
                    icon="pi pi-pencil"
                    title="Modifier"
                    (click)="ouvrirForm(p)"
                  ></button>
                }
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td colspan="8">Aucune identité ne correspond à ces critères.</td></tr>
          </ng-template>
        </p-table>
      </div>
    </div>

    <app-party-form-dialog [(visible)]="formVisible" [party]="edition" (saved)="apresSauve($event)" />
  `,
  styles: `
    .filtres { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
    .filtres input { width: 16rem; }
    .filtres input.tenant { width: 10rem; }
    :host ::ng-deep .filtre-dd { min-width: 10rem; }
    .code { font-family: ui-monospace, monospace; font-weight: 600; }
    .lien { font-weight: 600; color: var(--primary-color, #2563eb); text-decoration: none; }
    .lien:hover { text-decoration: underline; }
    .aliases { display: flex; flex-wrap: wrap; gap: 0.25rem; margin-top: 0.2rem; }
    .alias {
      font-size: 0.7rem; font-family: ui-monospace, monospace; color: #475569;
      background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 4px; padding: 0.05rem 0.35rem;
    }
    .inactif { opacity: 0.65; }
    .actions { white-space: nowrap; text-align: right; }
  `
})
export class PartiesListComponent implements OnInit {
  private readonly identity = inject(IdentityService);
  private readonly aliasesApi = inject(AliasService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly parties = signal<Party[]>([]);
  readonly aliasesParParty = signal<Record<string, AccountAlias[]>>({});
  readonly total = signal(0);
  readonly peutEcrire = computed(
    () => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER', 'OPERATOR', 'GROUP_ADMIN')
  );
  readonly kinds = KINDS_PARTY;
  readonly statuts = STATUTS;

  filtre: FiltreParties = {};
  formVisible = false;
  edition: Party | null = null;
  first = 0;
  taille = 50;
  private tri: PageRequeteParties['tri'] = null;
  private sens: PageRequeteParties['sens'] = null;

  ngOnInit(): void {
    this.aliasesApi.lister().subscribe({
      next: (list) => {
        const map: Record<string, AccountAlias[]> = {};
        for (const a of list) {
          if (a.ownerKind !== 'PARTY' || !a.active) {
            continue;
          }
          (map[a.ownerId] ??= []).push(a);
        }
        this.aliasesParParty.set(map);
      },
      error: () => this.aliasesParParty.set({})
    });
  }

  aliasesDe(partyId: string): AccountAlias[] {
    return this.aliasesParParty()[partyId] ?? [];
  }

  changerPage(e: TableLazyLoadEvent): void {
    this.first = e.first ?? 0;
    this.taille = e.rows ?? this.taille;
    const champ = typeof e.sortField === 'string' ? e.sortField : null;
    this.tri = (champ as PageRequeteParties['tri']) ?? null;
    this.sens = champ ? (e.sortOrder === -1 ? 'desc' : 'asc') : null;
    this.charger();
  }

  rechercher(): void {
    this.first = 0;
    this.charger();
  }

  effacer(): void {
    this.filtre = {};
    this.rechercher();
  }

  ouvrirForm(p: Party | null): void {
    this.edition = p;
    this.formVisible = true;
  }

  apresSauve(p: Party): void {
    if (!this.edition) {
      this.router.navigate(['/admin/identite/parties', p.id]);
      return;
    }
    this.charger();
  }

  kindLabel(k: PartyKind): string {
    return KINDS_PARTY.find((x) => x.value === k)?.label ?? k;
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    const page: PageRequeteParties = {
      page: Math.floor(this.first / this.taille),
      taille: this.taille,
      tri: this.tri,
      sens: this.sens
    };
    this.identity.rechercherParties(this.filtre, page).subscribe({
      next: (p) => {
        this.parties.set(p.contenu);
        this.total.set(p.total);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }
}
