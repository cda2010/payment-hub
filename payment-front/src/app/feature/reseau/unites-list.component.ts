import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../core/auth/auth.service';
import { AccountAlias, AliasService } from '../../core/service/alias.service';
import { FiltreUnites, NetworkService, PageRequete, UniteDetail, UniteResume } from '../../core/service/network.service';
import {
  STATUTS_UNITE,
  TYPES_UNITE,
  optionsZones,
  statutLabel,
  statutSeverity,
  typeIcon,
  typeLabel,
  typeSeverity
} from './reseau-format';
import { UniteFormDialogComponent } from './unite-form-dialog.component';

/**
 * Recherche des unités d'organisation. Avec la donnée de route {@code regroupements}, la liste se limite aux
 * regroupements et la création propose directement ce type.
 */
@Component({
  selector: 'app-unites-list',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    ButtonModule,
    DropdownModule,
    InputTextModule,
    TableModule,
    TagModule,
    UniteFormDialogComponent
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>{{ regroupements ? 'Regroupements' : 'Unités d’organisation' }}</h1>
          <p class="subtitle">
            @if (regroupements) {
              Rattachés à une unité de n'importe quel niveau, ils couvrent leurs membres et toute leur descendance
            } @else {
              Réseau de distribution par zone : pays, FSE, agences, masters, marchands, sous-distributeurs et regroupements
            }
            @if (!loading()) {
              · <strong>{{ total() }}</strong> résultat(s)
            }
          </p>
        </div>
        <div class="header-actions">
          <a pButton class="p-button-outlined" icon="pi pi-sitemap" label="Arbre du réseau" routerLink="/admin/reseau/arbre"></a>
          @if (peutEcrire()) {
            <a pButton class="p-button-outlined" icon="pi pi-upload" label="Import en masse" routerLink="/admin/reseau/import"></a>
            <button
              pButton
              type="button"
              icon="pi pi-plus"
              [label]="regroupements ? 'Nouveau regroupement' : 'Nouvelle unité'"
              (click)="creation = true"
            ></button>
          }
        </div>
      </div>

      <form class="gw-panel filtres" (ngSubmit)="rechercher()">
        <input pInputText name="texte" placeholder="Code ou libellé" [(ngModel)]="filtre.texte" />
        <p-dropdown
          name="zone"
          [options]="zones()"
          optionLabel="label"
          optionValue="value"
          placeholder="Zone"
          [showClear]="true"
          [(ngModel)]="filtre.zone"
          styleClass="filtre-dd"
        />
        @if (!regroupements) {
          <p-dropdown
            name="type"
            [options]="types"
            optionLabel="label"
            optionValue="value"
            placeholder="Type"
            [showClear]="true"
            [(ngModel)]="filtre.type"
            styleClass="filtre-dd"
          />
        }
        <p-dropdown
          name="statut"
          [options]="statuts"
          optionLabel="label"
          optionValue="value"
          placeholder="Statut"
          [showClear]="true"
          [(ngModel)]="filtre.statut"
          styleClass="filtre-dd"
        />
        <button pButton type="submit" icon="pi pi-search" label="Rechercher" [disabled]="loading()"></button>
        <button pButton type="button" class="p-button-text" label="Effacer" (click)="effacer()"></button>
      </form>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      <div class="gw-panel">
          <p-table
            [value]="unites()"
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
                <th pSortableColumn="libelle">Libellé <p-sortIcon field="libelle" /></th>
                <th pSortableColumn="type">Type <p-sortIcon field="type" /></th>
                <th pSortableColumn="zone">Zone <p-sortIcon field="zone" /></th>
                <th pSortableColumn="niveau">Niveau <p-sortIcon field="niveau" /></th>
                <th pSortableColumn="statut">Statut <p-sortIcon field="statut" /></th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-u>
              <tr>
                <td>
                  <a class="lien" [routerLink]="['/admin/reseau/unites', u.id]"><i [class]="icon(u.type)"></i> {{ u.code }}</a>
                </td>
                <td>
                  <div>{{ u.libelle }}</div>
                  @if (aliasesDe(u.id); as als) {
                    @if (als.length) {
                      <div class="aliases">
                        @for (a of als; track a.id) {
                          <span class="alias">{{ a.aliasCode }}</span>
                        }
                      </div>
                    }
                  }
                </td>
                <td><p-tag [value]="type(u.type)" [severity]="typeSev(u.type)" /></td>
                <td>{{ u.zone }}</td>
                <td>{{ u.niveau }}</td>
                <td><p-tag [value]="statut(u.statut)" [severity]="statutSev(u.statut)" /></td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td colspan="6">Aucune unité ne correspond à ces critères.</td></tr>
            </ng-template>
          </p-table>
      </div>
    </div>

    <app-unite-form-dialog
      [(visible)]="creation"
      [typeInitial]="regroupements ? 'REGROUPEMENT' : null"
      [zoneInitiale]="filtre.zone ?? null"
      (saved)="ouvrir($event)"
    />
  `,
  styles: `
    .filtres { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
    .filtres input { width: 13rem; }
    :host ::ng-deep .filtre-dd { min-width: 10rem; }
    .lien { font-weight: 600; color: var(--primary-color, #2563eb); text-decoration: none; white-space: nowrap; }
    .lien i { font-size: 0.8rem; margin-right: 0.2rem; color: #64748b; }
    .lien:hover { text-decoration: underline; }
    .aliases { display: flex; flex-wrap: wrap; gap: 0.25rem; margin-top: 0.2rem; }
    .alias {
      font-size: 0.7rem; font-family: ui-monospace, monospace; color: #475569;
      background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 4px; padding: 0.05rem 0.35rem;
    }
  `
})
export class UnitesListComponent implements OnInit {
  private readonly network = inject(NetworkService);
  private readonly aliasesApi = inject(AliasService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly unites = signal<UniteResume[]>([]);
  readonly aliasesParUnite = signal<Record<string, AccountAlias[]>>({});
  readonly total = signal(0);
  readonly peutEcrire = computed(() => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER'));
  readonly zones = signal<{ value: string; label: string }[]>([]);
  readonly types = TYPES_UNITE;
  readonly statuts = STATUTS_UNITE;

  /** Forcé par l'action-view RapidFrame ; sinon lu sur la route /admin. */
  @Input() seulementRegroupements = false;

  regroupements = false;
  filtre: FiltreUnites = {};
  creation = false;
  first = 0;
  taille = 50;
  private tri: PageRequete['tri'] = null;
  private sens: PageRequete['sens'] = null;

  ngOnInit(): void {
    this.regroupements = this.seulementRegroupements || !!this.route.snapshot.data['regroupements'];
    this.network.zones().subscribe({ next: (z) => this.zones.set(optionsZones(z)) });
    this.aliasesApi.lister().subscribe({
      next: (list) => {
        const map: Record<string, AccountAlias[]> = {};
        for (const a of list) {
          if (a.ownerKind !== 'UNITE' || !a.active) {
            continue;
          }
          (map[a.ownerId] ??= []).push(a);
        }
        this.aliasesParUnite.set(map);
      },
      error: () => this.aliasesParUnite.set({})
    });
  }

  aliasesDe(uniteId: string): AccountAlias[] {
    return this.aliasesParUnite()[uniteId] ?? [];
  }

  /** Appelé par le tableau au chargement, au changement de page et au tri : seule la page affichée est chargée. */
  changerPage(e: TableLazyLoadEvent): void {
    this.first = e.first ?? 0;
    this.taille = e.rows ?? this.taille;
    const champ = typeof e.sortField === 'string' ? e.sortField : null;
    this.tri = (champ as PageRequete['tri']) ?? null;
    this.sens = champ ? (e.sortOrder === -1 ? 'desc' : 'asc') : null;
    this.charger();
  }

  rechercher(): void {
    this.first = 0;
    this.charger();
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    const filtre = this.regroupements ? { ...this.filtre, type: 'REGROUPEMENT' as const } : this.filtre;
    const page = { page: Math.floor(this.first / this.taille), taille: this.taille, tri: this.tri, sens: this.sens };
    this.network.rechercher(filtre, page).subscribe({
      next: (p) => {
        this.unites.set(p.contenu);
        this.total.set(p.total);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }

  effacer(): void {
    this.filtre = {};
    this.rechercher();
  }

  ouvrir(u: UniteDetail): void {
    this.router.navigate(['/admin/reseau/unites', u.id]);
  }

  type = typeLabel;
  typeSev = typeSeverity;
  icon = typeIcon;
  statut = statutLabel;
  statutSev = statutSeverity;
}
