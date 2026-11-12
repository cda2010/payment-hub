import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import {
  EssaiFormule,
  FormuleDetail,
  FormuleResume,
  ParametreFormule,
  TarifService
} from '../../core/service/tarif.service';
import { ParametresFormComponent } from './parametres-form.component';
import {
  SaisieParametres,
  decaler,
  enTexte,
  estTaux,
  erreursParChamp,
  montant,
  montantSaisi,
  saisieDepuisValeurs,
  valeursDepuisSaisie
} from './tarif-format';

/** Écran B du cahier §5 : catalogue des formules, schéma de paramètres et essai. */
@Component({
  selector: 'app-formules',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, ProgressSpinnerModule, TableModule, TagModule, ParametresFormComponent],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Formules de calcul</h1>
          <p class="subtitle">
            Formules disponibles pour les tranches. Une version de formule est figée : toute évolution crée une nouvelle version.
          </p>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else {
        <div class="layout">
          <div class="gw-panel liste">
            <p-table
              [value]="formules()"
              selectionMode="single"
              [selection]="selection()"
              (onRowSelect)="ouvrir($event.data)"
              dataKey="id"
              styleClass="p-datatable-sm"
            >
              <ng-template pTemplate="header">
                <tr>
                  <th>Code</th>
                  <th>Libellé</th>
                  <th>Version</th>
                  <th>Statut</th>
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-f>
                <tr [pSelectableRow]="f">
                  <td><code>{{ f.code }}</code></td>
                  <td>{{ f.libelle }}</td>
                  <td>v{{ f.version }}</td>
                  <td>
                    <p-tag [value]="f.statut === 'ACTIVE' ? 'Active' : 'Inactive'" [severity]="f.statut === 'ACTIVE' ? 'success' : 'secondary'" />
                  </td>
                </tr>
              </ng-template>
            </p-table>
          </div>

          <div class="detail">
            @if (detail(); as f) {
              <div class="gw-panel">
                <h2>{{ f.libelle }} <span class="muted">{{ f.code }} v{{ f.version }} · {{ f.type === 'PREDEFINIE' ? 'prédéfinie' : 'expression' }}</span></h2>
                <code class="expr">{{ f.expression }}</code>
                @if (f.description) {
                  <p class="desc">{{ f.description }}</p>
                }
                <h3>Paramètres</h3>
                <p-table [value]="f.parametres" styleClass="p-datatable-sm">
                  <ng-template pTemplate="header">
                    <tr>
                      <th>Code</th>
                      <th>Libellé</th>
                      <th>Type</th>
                      <th>Oblig.</th>
                      <th>Bornes</th>
                      <th>Décimales</th>
                      <th>Défaut</th>
                    </tr>
                  </ng-template>
                  <ng-template pTemplate="body" let-p>
                    <tr>
                      <td><code>{{ p.code }}</code></td>
                      <td>{{ p.libelle }}</td>
                      <td>{{ type(p) }}</td>
                      <td>{{ p.obligatoire ? 'oui' : 'non' }}</td>
                      <td>{{ bornes(p) }}</td>
                      <td>{{ decimales(p) }}</td>
                      <td>{{ p.valeurDefaut ?? '—' }}</td>
                    </tr>
                  </ng-template>
                  <ng-template pTemplate="emptymessage">
                    <tr><td colspan="7">Aucun paramètre.</td></tr>
                  </ng-template>
                </p-table>
              </div>

              <div class="gw-panel">
                <h3>Essayer la formule</h3>
                <app-parametres-form prefixe="fe-" [schema]="f.parametres" [saisie]="saisie" [erreurs]="errParams()" (changed)="essai.set(null)" />
                <div class="essai">
                  <div class="gw-field">
                    <label for="fe-montant">Montant de la transaction</label>
                    <input pInputText id="fe-montant" inputmode="decimal" [(ngModel)]="montantEssai" />
                  </div>
                  <button pButton type="button" icon="pi pi-play" label="Calculer" [loading]="essaiEnCours()" (click)="essayer(f)"></button>
                </div>
                @if (essaiErreur()) {
                  <p class="gw-msg error">{{ essaiErreur() }}</p>
                }
                @if (essai(); as r) {
                  @if (r.valide) {
                    <div class="resultat">
                      <strong>{{ m(r.frais) }}</strong>
                      <code>{{ r.details }}</code>
                      <span class="muted">Base taxable : {{ m(r.baseTaxable) }} · sans plancher, plafond ni arrondi de grille.</span>
                    </div>
                  }
                }
              </div>
            } @else {
              <div class="gw-panel vide">Sélectionnez une formule pour voir ses paramètres et l'essayer.</div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: minmax(18rem, 2fr) 3fr; gap: 1rem; align-items: start; }
    @media (max-width: 1000px) { .layout { grid-template-columns: 1fr; } }
    h2 .muted { font-size: 0.8rem; font-weight: 400; margin-left: 0.4rem; }
    h3 { margin-top: 1rem !important; }
    .expr { display: block; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 0.45rem 0.65rem; font-size: 0.85rem; }
    .desc { font-size: 0.88rem; color: #334155; margin: 0.6rem 0 0; }
    .muted { color: #64748b; font-size: 0.82rem; }
    .essai { display: flex; align-items: flex-end; gap: 0.75rem; margin-top: 0.75rem; }
    .essai input { width: 12rem; }
    .resultat { display: flex; flex-wrap: wrap; align-items: center; gap: 0.5rem 0.75rem; margin-top: 0.75rem; }
    .resultat strong { font-size: 1.2rem; color: var(--primary-color, #2563eb); }
    .vide { color: #64748b; font-size: 0.9rem; }
  `
})
export class FormulesComponent implements OnInit {
  private readonly tarif = inject(TarifService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly formules = signal<FormuleResume[]>([]);
  readonly selection = signal<FormuleResume | null>(null);
  readonly detail = signal<FormuleDetail | null>(null);
  readonly errParams = signal<Record<string, string>>({});
  readonly essai = signal<EssaiFormule | null>(null);
  readonly essaiErreur = signal<string | null>(null);
  readonly essaiEnCours = signal(false);

  saisie: SaisieParametres = {};
  montantEssai = '20000';

  ngOnInit(): void {
    this.tarif.listerFormules().subscribe({
      next: (list) => {
        this.formules.set(list);
        this.loading.set(false);
        if (list.length) {
          this.ouvrir(list[0]);
        }
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }

  ouvrir(f: FormuleResume): void {
    this.selection.set(f);
    this.essai.set(null);
    this.essaiErreur.set(null);
    this.errParams.set({});
    this.tarif.formule(f.code, f.version).subscribe({
      next: (d) => {
        this.saisie = saisieDepuisValeurs(d.parametres, {});
        this.detail.set(d);
      },
      error: (e: { message?: string }) => this.error.set(e?.message ?? 'Formule introuvable.')
    });
  }

  essayer(f: FormuleDetail): void {
    const conv = valeursDepuisSaisie(f.parametres, this.saisie);
    const v = montantSaisi(this.montantEssai);
    this.errParams.set(conv.erreurs);
    this.essai.set(null);
    if (v === null || v === undefined) {
      this.essaiErreur.set('Saisissez un montant valide.');
      return;
    }
    if (Object.keys(conv.erreurs).length) {
      return;
    }
    this.essaiErreur.set(null);
    this.essaiEnCours.set(true);
    this.tarif.essayerFormule({ code: f.code, version: f.version }, { montant: v, parametres: conv.valeurs }).subscribe({
      next: (r) => {
        this.essaiEnCours.set(false);
        this.essai.set(r);
        if (!r.valide) {
          const parChamp = erreursParChamp(r.erreurs, f.parametres);
          this.errParams.set(
            Object.fromEntries(
              Object.entries(parChamp).map(([k, msg]) => [k.replace(/^parametres\./, ''), msg])
            )
          );
        }
      },
      error: (e: { message?: string }) => {
        this.essaiEnCours.set(false);
        this.essaiErreur.set(e?.message ?? 'Essai impossible.');
      }
    });
  }

  type(p: ParametreFormule): string {
    if (estTaux(p)) {
      return 'Taux (%)';
    }
    if (p.typeDonnee === 'DECIMAL' && p.unite === 'MONTANT') {
      return 'Montant';
    }
    return { DECIMAL: 'Décimal', INTEGER: 'Entier', BOOLEAN: 'Oui / non', STRING: 'Texte', ENUM: 'Liste' }[p.typeDonnee];
  }

  bornes(p: ParametreFormule): string {
    if (p.valeursAutorisees?.length) {
      return p.valeursAutorisees.join(', ');
    }
    const f = (v: number | undefined) =>
      v === undefined ? null : estTaux(p) ? `${montant(Number(decaler(enTexte(v), 2)))} %` : montant(v);
    const min = f(p.valeurMin);
    const max = f(p.valeurMax);
    if (min && max) return `${min} à ${max}`;
    if (min) return `≥ ${min}`;
    if (max) return `≤ ${max}`;
    return '—';
  }

  decimales(p: ParametreFormule): string {
    if (p.nbDecimales === undefined || p.nbDecimales === null) {
      return '—';
    }
    return estTaux(p) ? `${Math.max(p.nbDecimales - 2, 0)} (en %)` : String(p.nbDecimales);
  }

  m(v: number | undefined): string {
    return montant(v);
  }
}
