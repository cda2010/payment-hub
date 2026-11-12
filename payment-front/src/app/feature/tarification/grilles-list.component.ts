import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../core/auth/auth.service';
import {
  FiltreGrilles,
  GrilleDetail,
  GrilleResume,
  STATUTS_GRILLE,
  TarifService
} from '../../core/service/tarif.service';
import { GrilleFormDialogComponent } from './grille-form-dialog.component';
import { texteCible } from './cible-reseau.component';
import { statutLabel, statutSeverity } from './tarif-format';

/** Écran A du cahier §5 : recherche des grilles tarifaires et création d'un brouillon. */
@Component({
  selector: 'app-grilles-list',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    ButtonModule,
    DropdownModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule,
    GrilleFormDialogComponent
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Grilles tarifaires</h1>
          <p class="subtitle">
            Frais par produit, canal et devise, découpés en tranches de montant
            @if (!loading()) {
              · <strong>{{ total() }}</strong> grille(s)
            }
          </p>
        </div>
        <div class="header-actions">
          <a pButton class="p-button-outlined" icon="pi pi-calculator" label="Simulateur" routerLink="/admin/tarification/simulateur"></a>
          @if (peutEcrire()) {
            <button pButton type="button" icon="pi pi-plus" label="Nouvelle grille" (click)="creation = true"></button>
          }
        </div>
      </div>

      <form class="gw-panel filtres" (ngSubmit)="rechercher()">
        <input pInputText name="code" placeholder="Code" [(ngModel)]="filtre.code" />
        <input pInputText name="libelle" placeholder="Libellé" [(ngModel)]="filtre.libelle" />
        <input pInputText name="produit" placeholder="Produit" [(ngModel)]="filtre.produit" list="f-produits" />
        <datalist id="f-produits">
          @for (p of produits(); track p) { <option [value]="p"></option> }
        </datalist>
        <input pInputText name="canal" placeholder="Canal" [(ngModel)]="filtre.canal" />
        <input pInputText name="devise" placeholder="Devise" maxlength="3" class="court" [(ngModel)]="filtre.devise" />
        <input pInputText name="source" placeholder="Source (zone, unité)" title="Code de zone ou code d'unité visé par la grille" [(ngModel)]="filtre.source" />
        <input pInputText name="destination" placeholder="Destination" title="Code de zone ou code d'unité visé par la grille" [(ngModel)]="filtre.destination" />
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
        <span class="date">
          <label for="f-date">Valide le</label>
          <input pInputText type="date" id="f-date" name="date" [(ngModel)]="filtre.date" />
        </span>
        <button pButton type="submit" icon="pi pi-search" label="Rechercher" [disabled]="loading()"></button>
        <button pButton type="button" class="p-button-text" label="Effacer" (click)="effacer()"></button>
      </form>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      @if (loading() && !grilles().length) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else {
        <div class="gw-panel">
          <p-table
            [value]="grilles()"
            [lazy]="true"
            [lazyLoadOnInit]="false"
            (onLazyLoad)="changerPage($event)"
            [totalRecords]="total()"
            [paginator]="total() > taille"
            [rows]="taille"
            [first]="premier"
            [rowsPerPageOptions]="[25, 50, 100, 200]"
            [loading]="loading()"
            [rowHover]="true"
            dataKey="id"
            styleClass="p-datatable-sm"
          >
            <ng-template pTemplate="header">
              <tr>
                <th>Code</th>
                <th>Libellé</th>
                <th>Produit</th>
                <th>Canal</th>
                <th>Devise</th>
                <th>Source</th>
                <th>Destination</th>
                <th>Version</th>
                <th>Validité</th>
                <th>Statut</th>
                <th class="num">Tranches</th>
                <th>Modifiée</th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-g>
              <tr>
                <td><a class="lien" [routerLink]="['/admin/tarification/grilles', g.id]">{{ g.code }}</a></td>
                <td>{{ g.libelle }}</td>
                <td><code>{{ g.produit }}</code></td>
                <td>{{ g.canal || 'Tous' }}</td>
                <td>{{ g.devise }}</td>
                <td class="nowrap" [title]="g.source?.uniteLibelle ?? ''">{{ cible(g.source) }}</td>
                <td class="nowrap" [title]="g.destination?.uniteLibelle ?? ''">{{ cible(g.destination) }}</td>
                <td>v{{ g.version }}</td>
                <td class="nowrap">
                  {{ g.dateDebut | date: 'dd/MM/yyyy' }} → {{ g.dateFin ? (g.dateFin | date: 'dd/MM/yyyy') : '∞' }}
                </td>
                <td><p-tag [value]="label(g.statut)" [severity]="severity(g.statut)" /></td>
                <td class="num">{{ g.nbTranches }}</td>
                <td class="nowrap">
                  {{ g.updatedAt | date: 'dd/MM/yyyy HH:mm' }}
                  @if (g.updatedBy) {
                    <span class="muted">· {{ g.updatedBy }}</span>
                  }
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td colspan="12">Aucune grille ne correspond à ces critères.</td></tr>
            </ng-template>
          </p-table>
        </div>
      }
    </div>

    <app-grille-form-dialog [(visible)]="creation" [produits]="produits()" (saved)="ouvrir($event)" />
  `,
  styles: `
    .filtres { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
    .filtres input { width: 9rem; }
    .filtres input.court { width: 5.5rem; }
    .date { display: inline-flex; align-items: center; gap: 0.35rem; font-size: 0.8rem; color: #64748b; }
    .date input { width: 10rem; }
    :host ::ng-deep .filtre-dd { min-width: 9rem; }
    .lien { font-weight: 600; color: var(--primary-color, #2563eb); text-decoration: none; }
    .lien:hover { text-decoration: underline; }
    .num { text-align: right; }
    .nowrap { white-space: nowrap; }
    .muted { color: #94a3b8; font-size: 0.8rem; }
  `
})
export class GrillesListComponent implements OnInit {
  private readonly tarif = inject(TarifService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly grilles = signal<GrilleResume[]>([]);
  readonly total = signal(0);
  readonly produits = computed(() => [...new Set(this.grilles().map((g) => g.produit))].sort());
  readonly peutEcrire = computed(() => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER'));
  readonly statuts = STATUTS_GRILLE;

  filtre: FiltreGrilles = {};
  creation = false;
  taille = 50;
  premier = 0;

  ngOnInit(): void {
    this.rechercher();
  }

  rechercher(): void {
    this.premier = 0;
    this.charger();
  }

  changerPage(e: TableLazyLoadEvent): void {
    this.taille = e.rows ?? this.taille;
    this.premier = e.first ?? 0;
    this.charger();
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.tarif.rechercherGrilles(this.filtre, Math.floor(this.premier / this.taille), this.taille).subscribe({
      next: (page) => {
        this.grilles.set(page.contenu);
        this.total.set(page.total);
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

  ouvrir(g: GrilleDetail): void {
    this.router.navigate(['/admin/tarification/grilles', g.id]);
  }

  label = statutLabel;
  severity = statutSeverity;
  cible = texteCible;
}
