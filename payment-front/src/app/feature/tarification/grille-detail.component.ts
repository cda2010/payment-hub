import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { ChipModule } from 'primeng/chip';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { AuthService } from '../../core/auth/auth.service';
import {
  Anomalie,
  BASES_CALCUL,
  BaseCalculFrais,
  FormuleDetail,
  FormuleResume,
  GrilleDetail,
  PERIODES_CUMUL,
  PeriodeCumul,
  RapportValidation,
  ResultatFrais,
  SCOPES_CUMUL,
  ScopeCumul,
  TarifService,
  Tranche
} from '../../core/service/tarif.service';
import { GrilleFormDialogComponent } from './grille-form-dialog.component';
import { ResultatFraisComponent } from './resultat-frais.component';
import { TrancheDialogComponent } from './tranche-dialog.component';
import { texteCible } from './cible-reseau.component';
import {
  enTexte,
  montant,
  montantSaisi,
  nbDecimales,
  pourSaisie,
  resumeParametres,
  statutLabel,
  statutSeverity
} from './tarif-format';

interface GroupeAnomalies {
  titre: string;
  tranche: number | null;
  lignes: Anomalie[];
}

/** Écran C du cahier §5 : une grille, ses tranches, le contrôle de cohérence et la simulation. */
@Component({
  selector: 'app-grille-detail',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    ButtonModule,
    ChipModule,
    InputTextModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule,
    TooltipModule,
    GrilleFormDialogComponent,
    ResultatFraisComponent,
    TrancheDialogComponent
  ],
  template: `
    <div class="gw-console-page">
      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else if (!grille()) {
        <p class="gw-msg error">{{ error() }}</p>
        <a routerLink="/admin/tarification/grilles">Retour aux grilles</a>
      }
      @if (!loading() && grille(); as g) {
        <div class="gw-page-header">
          <div>
            <a class="retour" routerLink="/admin/tarification/grilles"><i class="pi pi-arrow-left"></i> Grilles tarifaires</a>
            <h1>
              {{ g.code }} <span class="version">v{{ g.version }}</span>
              <p-tag [value]="label(g.statut)" [severity]="severity(g.statut)" />
            </h1>
            <p class="subtitle">{{ g.libelle }}</p>
          </div>
          <div class="header-actions">
            <button pButton type="button" class="p-button-outlined" icon="pi pi-check-circle" label="Contrôler la cohérence" [loading]="controleEnCours()" (click)="controler()"></button>
            @if (editable()) {
              <button pButton type="button" class="p-button-outlined" icon="pi pi-pencil" label="Modifier l'en-tête" (click)="enteteVisible = true"></button>
              <button pButton type="button" class="p-button-outlined" icon="pi pi-plus" label="Ajouter une tranche" (click)="ouvrirTranche(null)"></button>
              <button pButton type="button" icon="pi pi-send" label="Publier" [loading]="publicationEnCours()" (click)="publier()"></button>
            } @else if (!g.modifiable && peutEcrire()) {
              <button pButton type="button" icon="pi pi-pencil" label="Modifier" [loading]="versionEnCours()" (click)="nouvelleVersion()"></button>
            }
          </div>
        </div>

        @if (!g.modifiable) {
          <p class="bandeau">
            <i class="pi pi-lock"></i>
            Grille {{ label(g.statut).toLowerCase() }} : une grille publiée ne se modifie pas directement.
            @if (peutEcrire()) {
              « Modifier » prépare une version {{ g.version + 1 }} en brouillon ; elle remplacera celle-ci à sa publication.
            }
          </p>
        } @else if (peutEcrire() && g.version > 1) {
          <p class="bandeau">
            <i class="pi pi-info-circle"></i>
            Brouillon de la version {{ g.version }} : la version publiée reste appliquée jusqu'à la publication de celle-ci.
          </p>
        } @else if (!peutEcrire()) {
          <p class="bandeau"><i class="pi pi-eye"></i> Consultation seule : votre profil ne permet pas de modifier les grilles.</p>
        }
        @if (error()) {
          <p class="gw-msg error">{{ error() }}</p>
        }
        @if (msg()) {
          <p class="gw-msg">{{ msg() }}</p>
        }

        <div class="gw-panel">
          <h3>Périmètre et règles</h3>
          <dl class="perimetre">
            <dt>Produit</dt><dd><code>{{ g.produit }}</code></dd>
            <dt>Canal</dt><dd>{{ g.canal || 'Tous les canaux' }}</dd>
            <dt>Devise</dt><dd>{{ g.devise }}</dd>
            <dt>Source</dt><dd>{{ cible(g.source) }}@if (g.source.uniteLibelle) { <span class="muted"> · {{ g.source.uniteLibelle }} ({{ g.source.zone }})</span> }</dd>
            <dt>Destination</dt><dd>{{ cible(g.destination) }}@if (g.destination.uniteLibelle) { <span class="muted"> · {{ g.destination.uniteLibelle }} ({{ g.destination.zone }})</span> }</dd>
            <dt>Validité</dt>
            <dd>du {{ g.dateDebut | date: 'dd/MM/yyyy' }} {{ g.dateFin ? 'au ' + (g.dateFin | date: 'dd/MM/yyyy') : '(sans fin)' }}</dd>
            <dt>Base de calcul</dt>
            <dd>{{ libelleBase(g.baseCalcul) }}
              @if (g.cumulPeriod || g.cumulScope) {
                <span class="muted"> · {{ libellePeriode(g.cumulPeriod) }} / {{ libelleScope(g.cumulScope) }}</span>
              }
            </dd>
            <dt>Tranches continues</dt><dd>{{ g.continuiteRequise ? 'Exigées' : 'Trous autorisés' }}</dd>
            <dt>Arrondi des frais</dt><dd>{{ g.arrondiEchelle }} décimale(s), {{ g.arrondiMode }}</dd>
            <dt>Créée</dt><dd>{{ g.createdAt | date: 'dd/MM/yyyy HH:mm' }} {{ g.createdBy ? 'par ' + g.createdBy : '' }}</dd>
            <dt>Modifiée</dt><dd>{{ g.updatedAt | date: 'dd/MM/yyyy HH:mm' }} {{ g.updatedBy ? 'par ' + g.updatedBy : '' }}</dd>
          </dl>
        </div>

        @if (rapport(); as r) {
          <div class="gw-panel rapport" [class.ok]="r.valide">
            <h3>
              @if (r.valide) {
                <i class="pi pi-check-circle"></i> Grille cohérente : aucune anomalie
              } @else {
                <i class="pi pi-exclamation-triangle"></i> {{ r.erreurs.length }} anomalie(s) à corriger
              }
              <button pButton type="button" icon="pi pi-times" class="p-button-text p-button-sm fermer" (click)="rapport.set(null)"></button>
            </h3>
            @for (grp of groupes(); track grp.titre) {
              <h4>{{ grp.titre }}</h4>
              <ul>
                @for (a of grp.lignes; track $index) {
                  <li>
                    {{ a.message }}
                    <span class="code">{{ a.code }}{{ a.champ ? ' · ' + a.champ : '' }}</span>
                  </li>
                }
              </ul>
            }
          </div>
        }

        <div class="gw-panel">
          <h3>Tranches ({{ g.tranches.length }})</h3>
          <p-table [value]="g.tranches" dataKey="id" styleClass="p-datatable-sm" [rowHover]="true">
            <ng-template pTemplate="header">
              <tr>
                <th>N°</th>
                <th class="num">Min ({{ g.devise }})</th>
                <th class="num">Max ({{ g.devise }})</th>
                <th>Formule</th>
                <th>Paramètres</th>
                <th class="num">Frais min</th>
                <th class="num">Frais max</th>
                <th>Exemple</th>
                @if (editable()) {
                  <th></th>
                }
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-t>
              <tr [class.en-erreur]="tranchesEnErreur().has(t.ordre)">
                <td>{{ t.ordre }}</td>
                <td class="num">{{ m(t.montantMin) }}</td>
                <td class="num">{{ t.montantMax !== undefined && t.montantMax !== null ? m(t.montantMax) : '∞' }}</td>
                <td>
                  <code [pTooltip]="t.formuleLibelle">{{ t.formule.code }}</code>
                  <span class="muted"> v{{ t.formule.version }}</span>
                </td>
                <td class="params">{{ parametres(t) }}</td>
                <td class="num">{{ m(t.fraisMin) }}</td>
                <td class="num">{{ m(t.fraisMax) }}</td>
                <td class="nowrap">
                  @if (exemples()[t.id]; as ex) {
                    <span class="muted">{{ ex }}</span>
                  }
                </td>
                @if (editable()) {
                  <td class="gw-row-actions">
                    <button pButton type="button" icon="pi pi-pencil" class="p-button-text p-button-sm" pTooltip="Modifier" (click)="ouvrirTranche(t)"></button>
                    <button pButton type="button" icon="pi pi-copy" class="p-button-text p-button-sm" pTooltip="Dupliquer" (click)="dupliquer(t)"></button>
                    <button pButton type="button" icon="pi pi-trash" class="p-button-text p-button-sm p-button-danger" pTooltip="Supprimer" (click)="supprimer(t)"></button>
                  </td>
                }
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td [attr.colspan]="editable() ? 9 : 8">Aucune tranche. {{ editable() ? 'Ajoutez-en une pour commencer.' : '' }}</td></tr>
            </ng-template>
          </p-table>
        </div>

        <div class="gw-panel">
          <h3>Simuler avec cette grille</h3>
          <form class="simu" (ngSubmit)="simuler()">
            <div class="gw-field">
              <label for="s-montant">Montant ({{ g.devise }})</label>
              <input pInputText id="s-montant" name="montant" inputmode="decimal" [(ngModel)]="montantSimu" />
            </div>
            <div class="gw-field">
              <label for="s-date">Date de calcul</label>
              <input pInputText type="date" id="s-date" name="date" [(ngModel)]="dateSimu" />
            </div>
            <button pButton type="submit" icon="pi pi-play" label="Simuler" [loading]="simuEnCours()"></button>
          </form>
          @if (bornes().length) {
            <div class="bornes">
              <span class="muted">Tester les bornes :</span>
              @for (b of bornes(); track b) {
                <p-chip [label]="m(b)" styleClass="borne" (click)="simulerMontant(b)" />
              }
            </div>
          }
          @if (simuErreur()) {
            <p class="gw-msg error">{{ simuErreur() }}</p>
          }
          @if (resultat(); as r) {
            <div class="resultat">
              <app-resultat-frais [r]="r" [schema]="schemas()[r.formuleCode + '@' + r.versionFormule]?.parametres" />
            </div>
          }
          <p class="muted note">Simulation sans écriture : aucune transaction n'est créée, la grille est appliquée quel que soit son statut.</p>
        </div>

        <app-grille-form-dialog [(visible)]="enteteVisible" [grille]="g" (saved)="recharger($event, 'En-tête enregistré.')" />
        <app-tranche-dialog
          [(visible)]="trancheVisible"
          [grille]="g"
          [tranche]="trancheEditee"
          [formules]="formules()"
          (saved)="recharger($event, trancheEditee ? 'Tranche enregistrée.' : 'Tranche ajoutée.')"
        />
      }
    </div>
  `,
  styles: `
    .retour { font-size: 0.8rem; color: #64748b; text-decoration: none; display: inline-block; margin-bottom: 0.35rem; }
    .retour:hover { color: var(--primary-color, #2563eb); }
    h1 { display: flex; align-items: center; gap: 0.6rem; }
    .version { font-weight: 400; color: #64748b; font-size: 1rem; }
    .bandeau { background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0.55rem 0.8rem; font-size: 0.85rem; color: #334155; margin: 0 0 1rem; }
    .bandeau i { margin-right: 0.35rem; color: #64748b; }
    .perimetre { display: grid; grid-template-columns: 10rem 1fr 10rem 1fr; gap: 0.4rem 0.75rem; margin: 0; font-size: 0.88rem; }
    .perimetre dt { color: #64748b; }
    .perimetre dd { margin: 0; }
    .rapport { border-color: #fca5a5; background: #fef2f2; }
    .rapport.ok { border-color: #86efac; background: #f0fdf4; }
    .rapport h3 { display: flex; align-items: center; gap: 0.5rem; }
    .rapport h3 .fermer { margin-left: auto; }
    .rapport h4 { margin: 0.6rem 0 0.25rem; font-size: 0.85rem; }
    .rapport ul { margin: 0; padding-left: 1.2rem; font-size: 0.87rem; line-height: 1.5; }
    .rapport .code { color: #94a3b8; font-size: 0.75rem; margin-left: 0.35rem; }
    .num { text-align: right; }
    .nowrap { white-space: nowrap; }
    .params { font-size: 0.85rem; }
    .muted { color: #64748b; font-size: 0.82rem; }
    tr.en-erreur > td { background: #fef2f2 !important; }
    .simu { display: flex; align-items: flex-end; gap: 0.75rem; flex-wrap: wrap; }
    .simu input { width: 11rem; }
    .bornes { display: flex; flex-wrap: wrap; gap: 0.35rem; align-items: center; margin-top: 0.75rem; }
    :host ::ng-deep .borne { cursor: pointer; font-size: 0.8rem; }
    :host ::ng-deep .borne:hover { background: #dbeafe; }
    .resultat { margin-top: 1rem; }
    .note { margin: 0.75rem 0 0; }
    @media (max-width: 900px) { .perimetre { grid-template-columns: 9rem 1fr; } }
  `
})
export class GrilleDetailComponent implements OnInit {
  private readonly tarif = inject(TarifService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);
  readonly grille = signal<GrilleDetail | null>(null);
  readonly formules = signal<FormuleResume[]>([]);
  readonly schemas = signal<Partial<Record<string, FormuleDetail>>>({});
  readonly exemples = signal<Record<string, string>>({});
  readonly rapport = signal<RapportValidation | null>(null);
  readonly controleEnCours = signal(false);
  readonly versionEnCours = signal(false);
  readonly publicationEnCours = signal(false);
  readonly resultat = signal<ResultatFrais | null>(null);
  readonly simuErreur = signal<string | null>(null);
  readonly simuEnCours = signal(false);

  readonly peutEcrire = computed(() => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER'));
  readonly editable = computed(() => !!this.grille()?.modifiable && this.peutEcrire());

  readonly groupes = computed<GroupeAnomalies[]>(() => {
    const r = this.rapport();
    if (!r || r.valide) {
      return [];
    }
    const parTranche = new Map<number | null, Anomalie[]>();
    for (const a of r.erreurs) {
      const cle = a.tranche ?? null;
      parTranche.set(cle, [...(parTranche.get(cle) ?? []), a]);
    }
    return [...parTranche.entries()]
      .sort(([a], [b]) => (a ?? -1) - (b ?? -1))
      .map(([tranche, lignes]) => ({ titre: tranche === null ? 'Grille' : `Tranche ${tranche}`, tranche, lignes }));
  });

  readonly tranchesEnErreur = computed(
    () => new Set((this.rapport()?.erreurs ?? []).map((a) => a.tranche).filter((t): t is number => typeof t === 'number'))
  );

  /** Montants qui encadrent chaque changement de tranche : min, max et juste après max. */
  readonly bornes = computed(() => {
    const g = this.grille();
    if (!g) {
      return [];
    }
    const set = new Set<number>();
    for (const t of g.tranches) {
      set.add(t.montantMin);
      if (t.montantMax !== undefined && t.montantMax !== null) {
        set.add(t.montantMax);
        const echelle = nbDecimales(enTexte(t.montantMax));
        set.add(Number((t.montantMax + 10 ** -echelle).toFixed(echelle)));
      }
    }
    return [...set].sort((a, b) => a - b).slice(0, 24);
  });

  enteteVisible = false;
  trancheVisible = false;
  trancheEditee: Tranche | null = null;
  montantSimu = '';
  dateSimu = '';
  private id = '';

  ngOnInit(): void {
    this.route.paramMap.subscribe((p) => {
      this.id = p.get('id') ?? '';
      this.charger();
    });
    this.tarif.listerFormules().subscribe({ next: (list) => this.formules.set(list) });
  }

  charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.tarif.grille(this.id).subscribe({
      next: (g) => {
        this.loading.set(false);
        this.appliquer(g);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Grille introuvable.');
      }
    });
  }

  recharger(g: GrilleDetail, message: string): void {
    this.msg.set(message);
    this.error.set(null);
    this.appliquer(g);
    if (this.rapport()) {
      this.controler();
    }
  }

  ouvrirTranche(t: Tranche | null): void {
    this.trancheEditee = t;
    this.trancheVisible = true;
  }

  dupliquer(t: Tranche): void {
    const g = this.grille()!;
    this.tarif.dupliquerTranche(g.id, t.id).subscribe({
      next: (res) => this.recharger(res, `Tranche ${t.ordre} dupliquée : ajustez les bornes de la copie.`),
      error: (e: { message?: string }) => this.error.set(e?.message ?? 'Duplication impossible.')
    });
  }

  supprimer(t: Tranche): void {
    if (!window.confirm(`Supprimer la tranche ${t.ordre} (${this.m(t.montantMin)} → ${t.montantMax ?? '∞'}) ?`)) {
      return;
    }
    const g = this.grille()!;
    this.tarif.supprimerTranche(g.id, t.id).subscribe({
      next: (res) => this.recharger(res, `Tranche ${t.ordre} supprimée.`),
      error: (e: { message?: string }) => this.error.set(e?.message ?? 'Suppression impossible.')
    });
  }

  nouvelleVersion(): void {
    const g = this.grille()!;
    this.versionEnCours.set(true);
    this.tarif.nouvelleVersion(g.id).subscribe({
      next: (v) => {
        this.versionEnCours.set(false);
        this.rapport.set(null);
        this.resultat.set(null);
        this.error.set(null);
        this.msg.set(`Version ${v.version} ouverte en brouillon : modifiez-la puis publiez-la pour remplacer la v${g.version}.`);
        this.router.navigate(['/admin/tarification/grilles', v.id]);
      },
      error: (e: { message?: string }) => {
        this.versionEnCours.set(false);
        this.error.set(e?.message ?? 'Création de la nouvelle version impossible.');
      }
    });
  }

  publier(): void {
    const g = this.grille()!;
    const remplace = g.version > 1 ? ` Elle remplacera la version publiée de ${g.code}.` : '';
    if (!window.confirm(`Publier ${g.code} v${g.version} ? Elle s'appliquera aux calculs de frais.${remplace}`)) {
      return;
    }
    this.publicationEnCours.set(true);
    this.tarif.publier(g.id).subscribe({
      next: (res) => {
        this.publicationEnCours.set(false);
        this.rapport.set(null);
        this.recharger(res, `${res.code} v${res.version} publiée : elle s'applique désormais aux calculs de frais.`);
      },
      error: (e: { message?: string; details?: unknown[] }) => {
        this.publicationEnCours.set(false);
        this.msg.set(null);
        this.error.set(e?.message ?? 'Publication impossible.');
        if (e?.details?.length) {
          this.controler();
        }
      }
    });
  }

  controler(): void {
    this.controleEnCours.set(true);
    this.tarif.controler(this.id).subscribe({
      next: (r) => {
        this.controleEnCours.set(false);
        this.rapport.set(r);
      },
      error: (e: { message?: string }) => {
        this.controleEnCours.set(false);
        this.error.set(e?.message ?? 'Contrôle impossible.');
      }
    });
  }

  simulerMontant(v: number): void {
    this.montantSimu = pourSaisie(v);
    this.simuler();
  }

  simuler(): void {
    const v = montantSaisi(this.montantSimu);
    if (v === null || v === undefined || v < 0) {
      this.simuErreur.set('Saisissez un montant positif.');
      return;
    }
    this.simuErreur.set(null);
    this.simuEnCours.set(true);
    this.tarif.simuler(this.id, v, this.dateSimu || null).subscribe({
      next: (r) => {
        this.simuEnCours.set(false);
        this.resultat.set(r);
        this.chargerSchema(r.formuleCode, r.versionFormule);
      },
      error: (e: { message?: string }) => {
        this.simuEnCours.set(false);
        this.resultat.set(null);
        this.simuErreur.set(e?.message ?? 'Simulation impossible.');
      }
    });
  }

  parametres(t: Tranche): string {
    const schema = this.schemas()[`${t.formule.code}@${t.formule.version}`];
    return resumeParametres(schema?.parametres, t.parametres ?? {}, this.grille()?.devise ?? '');
  }

  m(v: number | null | undefined): string {
    return montant(v);
  }

  label = statutLabel;
  cible = texteCible;
  severity = statutSeverity;

  libelleBase(b?: BaseCalculFrais | null): string {
    return BASES_CALCUL.find((x) => x.value === (b ?? 'TRANSACTION_AMOUNT'))?.label ?? b ?? '—';
  }

  libellePeriode(p?: PeriodeCumul | null): string {
    return PERIODES_CUMUL.find((x) => x.value === p)?.label ?? p ?? '—';
  }

  libelleScope(s?: ScopeCumul | null): string {
    return SCOPES_CUMUL.find((x) => x.value === s)?.label ?? s ?? '—';
  }

  private appliquer(g: GrilleDetail): void {
    this.grille.set(g);
    for (const t of g.tranches) {
      this.chargerSchema(t.formule.code, t.formule.version);
    }
    this.calculerExemples(g);
  }

  private chargerSchema(code: string, version: number): void {
    const cle = `${code}@${version}`;
    if (this.schemas()[cle]) {
      return;
    }
    this.tarif.formule(code, version).subscribe({
      next: (f) => this.schemas.update((s) => ({ ...s, [cle]: f }))
    });
  }

  /** Un exemple par tranche, au montant maximum (ou minimum pour la tranche ouverte). */
  private calculerExemples(g: GrilleDetail): void {
    if (!g.tranches.length) {
      this.exemples.set({});
      return;
    }
    const appels = g.tranches.map((t) => {
      const v = t.montantMax ?? t.montantMin;
      return this.tarif.simuler(g.id, v).pipe(
        map((r) => [t.id, `${this.m(v)} → ${montant(r.frais, g.devise)}`] as const),
        catchError(() => of([t.id, ''] as const))
      );
    });
    forkJoin(appels).subscribe((paires) => this.exemples.set(Object.fromEntries(paires)));
  }
}
