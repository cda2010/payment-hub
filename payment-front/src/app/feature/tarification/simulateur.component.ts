import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ApiErrorDetail } from '../../core/interceptors/error.interceptor';
import { FormuleDetail, GrilleResume, ResultatFrais, TarifService } from '../../core/service/tarif.service';
import { CibleReseauComponent, ValeurCible, cibleVide, versLocalisation } from './cible-reseau.component';
import { ResultatFraisComponent } from './resultat-frais.component';
import { aujourdhui, montantSaisi } from './tarif-format';

/**
 * Écran E du cahier §5 : calcul des frais à partir du contexte d'une transaction (produit, canal, devise, montant,
 * date). Même chemin que le calcul en production : recherche de la grille publiée applicable, puis de la tranche.
 */
@Component({
  selector: 'app-simulateur',
  standalone: true,
  imports: [FormsModule, RouterLink, ButtonModule, InputTextModule, CibleReseauComponent, ResultatFraisComponent],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Simulateur de frais</h1>
          <p class="subtitle">
            Calcule les frais comme pour une vraie transaction, avec les grilles publiées. Aucune transaction n'est créée.
          </p>
        </div>
      </div>

      <form class="gw-panel" (ngSubmit)="calculer()">
        <div class="champs">
          <div class="gw-field">
            <label for="sim-produit">Produit *</label>
            <input pInputText id="sim-produit" name="produit" list="sim-produits" [(ngModel)]="produit" (ngModelChange)="produitChoisi.set($event)" />
            <datalist id="sim-produits">
              @for (p of produits(); track p) { <option [value]="p"></option> }
            </datalist>
          </div>
          <div class="gw-field">
            <label for="sim-canal">Canal</label>
            <input pInputText id="sim-canal" name="canal" list="sim-canaux" placeholder="Vide = générique" [(ngModel)]="canal" />
            <datalist id="sim-canaux">
              @for (c of canaux(); track c) { <option [value]="c"></option> }
            </datalist>
          </div>
          <div class="gw-field">
            <label for="sim-devise">Devise *</label>
            <input pInputText id="sim-devise" name="devise" list="sim-devises" maxlength="3" [(ngModel)]="devise" />
            <datalist id="sim-devises">
              @for (d of devises(); track d) { <option [value]="d"></option> }
            </datalist>
          </div>
          <div class="gw-field">
            <label for="sim-montant">Montant *</label>
            <input pInputText id="sim-montant" name="montant" inputmode="decimal" [(ngModel)]="montant" />
          </div>
          <div class="gw-field">
            <label for="sim-date">Date de la transaction</label>
            <input pInputText type="date" id="sim-date" name="date" [(ngModel)]="date" />
          </div>
        </div>
        <div class="cotes">
          <div class="gw-field">
            <label>Source de la transaction</label>
            <app-cible-reseau mode="transaction" nom="sim-source" [(valeur)]="source" />
          </div>
          <div class="gw-field">
            <label>Destination de la transaction</label>
            <app-cible-reseau mode="transaction" nom="sim-destination" [(valeur)]="destination" />
          </div>
        </div>
        <p class="aide">
          Unité : point de vente ou acteur réel (le plus précis). Pays ou zone : quand seule la localisation est connue.
          Sans destination, seules les grilles sans destination s'appliquent.
        </p>
        <button pButton type="submit" icon="pi pi-calculator" label="Calculer" [loading]="enCours()"></button>
      </form>

      @if (erreur()) {
        <div class="gw-panel echec">
          <strong>{{ erreur() }}</strong>
          @if (details().length) {
            <ul>
              @for (d of details(); track $index) { <li>{{ d.message }}</li> }
            </ul>
          }
        </div>
      }

      @if (resultat(); as r) {
        <div class="gw-panel">
          <app-resultat-frais [r]="r" [schema]="schema()?.parametres" />
          <p class="lien">
            <a [routerLink]="['/admin/tarification/grilles', r.grilleId]">Ouvrir la grille {{ r.grilleCode }}</a>
          </p>
        </div>
      }
    </div>
  `,
  styles: `
    .champs { display: flex; flex-wrap: wrap; gap: 0.75rem; align-items: flex-end; }
    .champs .gw-field { width: 11rem; }
    .cotes { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 1rem; }
    .aide { color: #64748b; font-size: 0.8rem; margin: 0.5rem 0 0.75rem; }
    .echec { border-color: #fca5a5; background: #fef2f2; color: #991b1b; font-size: 0.9rem; }
    .echec ul { margin: 0.4rem 0 0; padding-left: 1.2rem; }
    .lien { margin: 0.75rem 0 0; font-size: 0.85rem; }
  `
})
export class SimulateurComponent implements OnInit {
  private readonly tarif = inject(TarifService);

  readonly publiees = signal<GrilleResume[]>([]);
  readonly produitChoisi = signal('');
  readonly produits = computed(() => uniques(this.publiees().map((g) => g.produit)));
  readonly canaux = computed(() =>
    uniques(this.publiees().filter((g) => this.correspond(g)).map((g) => g.canal ?? ''))
  );
  readonly devises = computed(() => uniques(this.publiees().filter((g) => this.correspond(g)).map((g) => g.devise)));
  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly details = signal<ApiErrorDetail[]>([]);
  readonly resultat = signal<ResultatFrais | null>(null);
  readonly schema = signal<FormuleDetail | null>(null);

  produit = '';
  canal = '';
  devise = 'XOF';
  montant = '';
  date = aujourdhui();
  source: ValeurCible = cibleVide();
  destination: ValeurCible = cibleVide();

  ngOnInit(): void {
    this.tarif.rechercherGrilles({ statut: 'PUBLIEE' }, 0, 200).subscribe({ next: (page) => this.publiees.set(page.contenu) });
  }

  calculer(): void {
    const v = montantSaisi(this.montant);
    this.resultat.set(null);
    this.details.set([]);
    if (!this.produit.trim() || !this.devise.trim() || v === null || v === undefined || v < 0) {
      this.erreur.set('Produit, devise et montant positif sont obligatoires.');
      return;
    }
    this.erreur.set(null);
    this.enCours.set(true);
    this.tarif
      .calculer({
        produitCode: this.produit.trim().toUpperCase(),
        canalCode: this.canal.trim() ? this.canal.trim().toUpperCase() : null,
        devise: this.devise.trim().toUpperCase(),
        montant: v,
        dateCalcul: this.date || null,
        source: versLocalisation(this.source),
        destination: versLocalisation(this.destination)
      })
      .subscribe({
        next: (r) => {
          this.enCours.set(false);
          this.resultat.set(r);
          this.schema.set(null);
          this.tarif.formule(r.formuleCode, r.versionFormule).subscribe({ next: (f) => this.schema.set(f) });
        },
        error: (e: { message?: string; details?: ApiErrorDetail[] }) => {
          this.enCours.set(false);
          this.erreur.set(e?.message ?? 'Calcul impossible.');
          this.details.set(e?.details ?? []);
        }
      });
  }

  private correspond(g: GrilleResume): boolean {
    const p = this.produitChoisi().trim().toUpperCase();
    return !p || g.produit.toUpperCase() === p;
  }
}

function uniques(valeurs: string[]): string[] {
  return [...new Set(valeurs.filter((v) => !!v))].sort();
}
