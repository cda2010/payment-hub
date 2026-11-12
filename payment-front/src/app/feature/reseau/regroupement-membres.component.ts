import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, forkJoin, map, of } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { Membre, NetworkService, UniteDetail, UniteResume } from '../../core/service/network.service';
import { ChoixUniteComponent } from './choix-unite.component';
import { statutLabel, statutSeverity, typeIcon, typeLabel, typeSeverity } from './reseau-format';

/** Membres d'un regroupement (ajout, retrait) et périmètre couvert : les membres et toute leur descendance. */
@Component({
  selector: 'app-regroupement-membres',
  standalone: true,
  imports: [DatePipe, FormsModule, RouterLink, ButtonModule, ChoixUniteComponent, TableModule, TagModule, TooltipModule],
  template: `
    <div class="gw-panel">
      <h3>Membres ({{ membres().length }})</h3>
      @if (peutEcrire && regroupement.statut === 'ACTIF') {
        <form class="ajout" (ngSubmit)="ajouter()">
          <app-choix-unite
            class="candidat"
            [zone]="regroupement.zone"
            [branche]="regroupement.parent?.id ?? null"
            [sansRegroupements]="true"
            [exclure]="idsMembres()"
            [(valeur)]="candidat"
            [placeholder]="'Unité active de la branche de ' + (regroupement.parent?.code ?? '')"
            vide="Aucune unité à ajouter"
          />
          <button pButton type="submit" icon="pi pi-plus" label="Ajouter" [disabled]="!candidat" [loading]="ajoutEnCours()"></button>
        </form>
        <p class="muted">
          Un membre est {{ regroupement.parent?.code }} lui-même ou une unité de sa branche, de n'importe quel niveau ; il
          apporte toute sa descendance. Une unité qui descend déjà
          d'un membre, ou dont un membre descend, est refusée (double couverture).
        </p>
      }
      @if (erreur()) {
        <p class="gw-msg error">{{ erreur() }}</p>
      }
      @if (msg()) {
        <p class="gw-msg">{{ msg() }}</p>
      }
      <p-table [value]="membres()" dataKey="unite.id" styleClass="p-datatable-sm" [rowHover]="true">
        <ng-template pTemplate="header">
          <tr>
            <th>Code</th>
            <th>Libellé</th>
            <th>Type</th>
            <th>Statut</th>
            <th class="num">Unités couvertes</th>
            <th>Ajouté</th>
            @if (peutEcrire) {
              <th></th>
            }
          </tr>
        </ng-template>
        <ng-template pTemplate="body" let-m>
          <tr>
            <td class="nowrap">
              <i [class]="icon(m.unite.type)" class="type-icon"></i>
              <a class="lien" [routerLink]="['/admin/reseau/unites', m.unite.id]">{{ m.unite.code }}</a>
            </td>
            <td>{{ m.unite.libelle }}</td>
            <td><p-tag [value]="type(m.unite.type)" [severity]="typeSev(m.unite.type)" /></td>
            <td><p-tag [value]="statut(m.unite.statut)" [severity]="statutSev(m.unite.statut)" /></td>
            <td class="num">{{ couvertesPar()[m.unite.id] ?? '…' }}</td>
            <td class="nowrap muted">
              {{ m.ajouteLe | date: 'dd/MM/yyyy' }} {{ m.ajoutePar ? '· ' + m.ajoutePar : '' }}
            </td>
            @if (peutEcrire) {
              <td class="gw-row-actions">
                <button
                  pButton
                  type="button"
                  icon="pi pi-times"
                  class="p-button-text p-button-sm p-button-danger"
                  pTooltip="Retirer du regroupement"
                  (click)="retirer(m)"
                ></button>
              </td>
            }
          </tr>
        </ng-template>
        <ng-template pTemplate="emptymessage">
          <tr><td [attr.colspan]="peutEcrire ? 7 : 6">Aucun membre pour l'instant.</td></tr>
        </ng-template>
      </p-table>
    </div>

    <div class="gw-panel">
      <h3>Périmètre couvert ({{ perimetreTotal() }} unité(s))</h3>
      <p class="muted">Les membres et toute leur descendance, du niveau le plus haut au plus bas.</p>
      <p-table
        [value]="perimetre()"
        dataKey="id"
        styleClass="p-datatable-sm"
        [lazy]="true"
        [lazyLoadOnInit]="false"
        (onLazyLoad)="pagePerimetre($event)"
        [paginator]="perimetreTotal() > taillePerimetre"
        [first]="debutPerimetre"
        [rows]="taillePerimetre"
        [totalRecords]="perimetreTotal()"
        [loading]="perimetreEnCours()"
      >
        <ng-template pTemplate="header">
          <tr>
            <th>Code</th>
            <th>Libellé</th>
            <th>Type</th>
            <th>Statut</th>
            <th>Couvert</th>
          </tr>
        </ng-template>
        <ng-template pTemplate="body" let-u>
          <tr>
            <td class="nowrap" [style.padding-left.rem]="0.5 + (u.niveau - niveauMin()) * 1.1">
              <i [class]="icon(u.type)" class="type-icon"></i>
              <a class="lien" [routerLink]="['/admin/reseau/unites', u.id]">{{ u.code }}</a>
            </td>
            <td>{{ u.libelle }}</td>
            <td><p-tag [value]="type(u.type)" [severity]="typeSev(u.type)" /></td>
            <td><p-tag [value]="statut(u.statut)" [severity]="statutSev(u.statut)" /></td>
            <td class="muted">{{ estMembre(u.id) ? 'Membre' : 'Par descendance' }}</td>
          </tr>
        </ng-template>
        <ng-template pTemplate="emptymessage">
          <tr><td colspan="5">Le regroupement ne couvre aucune unité.</td></tr>
        </ng-template>
      </p-table>
    </div>
  `,
  styles: `
    .ajout { display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.4rem; }
    .candidat { min-width: 26rem; }
    .muted { color: #64748b; font-size: 0.8rem; }
    .type-icon { color: #64748b; font-size: 0.8rem; margin-right: 0.35rem; }
    .lien { font-weight: 600; color: var(--primary-color, #2563eb); text-decoration: none; }
    .lien:hover { text-decoration: underline; }
    .num { text-align: right; }
    .nowrap { white-space: nowrap; }
  `
})
export class RegroupementMembresComponent implements OnChanges {
  private readonly network = inject(NetworkService);

  @Input({ required: true }) regroupement!: UniteDetail;
  @Input() peutEcrire = false;
  /** Émis après un ajout ou un retrait, pour rafraîchir la fiche. */
  @Output() readonly changed = new EventEmitter<void>();

  readonly membres = signal<Membre[]>([]);
  readonly idsMembres = computed(() => this.membres().map((m) => m.unite.id));
  /** Page affichée du périmètre (chargé par pages : un regroupement peut couvrir des milliers d'unités). */
  readonly perimetre = signal<UniteResume[]>([]);
  readonly perimetreTotal = signal(0);
  readonly perimetreEnCours = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly msg = signal<string | null>(null);
  readonly ajoutEnCours = signal(false);
  readonly niveauMin = computed(() => Math.min(...this.perimetre().map((u) => u.niveau)));
  /** Nombre d'unités couvertes par chaque membre, lui compris. */
  readonly couvertesPar = signal<Partial<Record<string, number>>>({});
  candidat: UniteResume | null = null;
  debutPerimetre = 0;
  readonly taillePerimetre = 20;

  ngOnChanges(changes: SimpleChanges): void {
    const c = changes['regroupement'];
    if (!c) {
      return;
    }
    const avant = c.previousValue as UniteDetail | undefined;
    const apres = this.regroupement;
    if (!avant || avant.id !== apres.id || avant.parent?.id !== apres.parent?.id || avant.statut !== apres.statut) {
      this.charger();
    }
  }

  pagePerimetre(e: TableLazyLoadEvent): void {
    this.debutPerimetre = e.first ?? 0;
    this.chargerPerimetre();
  }

  estMembre(id: string): boolean {
    return this.membres().some((m) => m.unite.id === id);
  }

  ajouter(): void {
    if (!this.candidat) {
      return;
    }
    const code = this.candidat.code;
    this.ajoutEnCours.set(true);
    this.appliquer(this.network.ajouterMembre(this.regroupement.id, this.candidat.id), `${code} ajouté au regroupement.`, () =>
      this.ajoutEnCours.set(false)
    );
  }

  retirer(m: Membre): void {
    if (!window.confirm(`Retirer ${m.unite.code} du regroupement ${this.regroupement.code} ? Sa descendance ne sera plus couverte.`)) {
      return;
    }
    this.appliquer(this.network.retirerMembre(this.regroupement.id, m.unite.id), `${m.unite.code} retiré du regroupement.`);
  }

  private appliquer(req: Observable<Membre[]>, message: string, fin?: () => void): void {
    this.erreur.set(null);
    this.msg.set(null);
    req.subscribe({
      next: (list) => {
        fin?.();
        this.candidat = null;
        this.membres.set(list);
        this.msg.set(message);
        this.compterCouvertes();
        this.debutPerimetre = 0;
        this.chargerPerimetre();
        this.changed.emit();
      },
      error: (e: { message?: string }) => {
        fin?.();
        this.erreur.set(e?.message ?? 'Opération impossible.');
      }
    });
  }

  private charger(): void {
    this.erreur.set(null);
    this.msg.set(null);
    this.candidat = null;
    this.network.membres(this.regroupement.id).subscribe({
      next: (list) => {
        this.membres.set(list);
        this.compterCouvertes();
      },
      error: (e: { message?: string }) => this.erreur.set(e?.message ?? 'Chargement des membres impossible.')
    });
    this.debutPerimetre = 0;
    this.chargerPerimetre();
  }

  private chargerPerimetre(): void {
    this.perimetreEnCours.set(true);
    this.network
      .perimetre(this.regroupement.id, Math.floor(this.debutPerimetre / this.taillePerimetre), this.taillePerimetre)
      .subscribe({
        next: (p) => {
          this.perimetre.set(p.contenu);
          this.perimetreTotal.set(p.total);
          this.perimetreEnCours.set(false);
        },
        error: () => {
          this.perimetre.set([]);
          this.perimetreTotal.set(0);
          this.perimetreEnCours.set(false);
        }
      });
  }

  /** Une requête de comptage par membre (taille 1 : seul le total compte). */
  private compterCouvertes(): void {
    const membres = this.membres();
    this.couvertesPar.set({});
    const comptes = membres.map((m) =>
      this.network.descendants(m.unite.id, 0, 1).pipe(map((p) => [m.unite.id, p.total + 1] as const))
    );
    (comptes.length ? forkJoin(comptes) : of([])).subscribe({
      next: (liste) => this.couvertesPar.set(Object.fromEntries(liste))
    });
  }

  type = typeLabel;
  typeSev = typeSeverity;
  icon = typeIcon;
  statut = statutLabel;
  statutSev = statutSeverity;
}
