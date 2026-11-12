import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Observable, Subject, catchError, debounceTime, map, of, switchMap, tap } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { ApiErrorDetail } from '../../core/interceptors/error.interceptor';
import {
  ApplicableRequest,
  BASES_CALCUL,
  BaseCalculFrais,
  Correspondance,
  GrilleApplicable,
  GrilleDetail,
  GrilleRequest,
  GrilleRetenue,
  MODES_ARRONDI,
  ModeArrondi,
  PERIODES_CUMUL,
  PeriodeCumul,
  SCOPES_CUMUL,
  ScopeCumul,
  TarifService,
  estBaseCumul
} from '../../core/service/tarif.service';
import { TypeUnite } from '../../core/service/network.service';
import {
  CibleReseauComponent,
  TYPES_UNITE_LIBELLES,
  ValeurCible,
  cibleVide,
  depuisCible,
  versCibleRequest,
  versLocalisation
} from './cible-reseau.component';
import { aujourdhui, erreursParChamp } from './tarif-format';

/** Résultat de la recherche du tarif actuel : la réponse du moteur, ou pourquoi on ne l'a pas demandée. */
type TarifActuel = { info: string } | { applicable: GrilleApplicable; date: string } | { erreur: string };

/** Au-delà de ce nombre de grilles sœurs, on suggère un regroupement. */
const SEUIL_VOISINES = 2;

interface Saisie {
  code: string;
  libelle: string;
  produit: string;
  canal: string;
  devise: string;
  dateDebut: string;
  dateFin: string;
  continuiteRequise: boolean;
  arrondiEchelle: string;
  arrondiMode: ModeArrondi;
  baseCalcul: BaseCalculFrais;
  cumulPeriod: PeriodeCumul | null;
  cumulScope: ScopeCumul | null;
  source: ValeurCible;
  destination: ValeurCible;
}

/** Création d'une grille en brouillon ou modification de l'en-tête d'un brouillon. */
@Component({
  selector: 'app-grille-form-dialog',
  standalone: true,
  imports: [FormsModule, CibleReseauComponent, ButtonModule, CheckboxModule, DialogModule, DropdownModule, InputTextModule],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="visibleChange.emit($event)"
      [header]="grille ? 'Modifier l’en-tête de ' + grille.code : 'Nouvelle grille (brouillon)'"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '48rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="enregistrer()">
        <div class="gw-field">
          <label for="g-code">Code *</label>
          <input pInputText id="g-code" name="code" [(ngModel)]="s.code" placeholder="ex. TRF_STD" maxlength="64" [readonly]="codeFige" />
          @if (codeFige) { <small class="muted">Le code reste celui des versions précédentes.</small> }
          @if (err()['code']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="g-libelle">Libellé *</label>
          <input pInputText id="g-libelle" name="libelle" [(ngModel)]="s.libelle" maxlength="150" />
          @if (err()['libelle']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="g-produit">Produit *</label>
          <input pInputText id="g-produit" name="produit" [(ngModel)]="s.produit" (ngModelChange)="demanderTarif()" list="g-produits" maxlength="80" />
          <datalist id="g-produits">
            @for (p of produits; track p) { <option [value]="p"></option> }
          </datalist>
          @if (err()['produit']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="g-canal">Canal</label>
          <input pInputText id="g-canal" name="canal" [(ngModel)]="s.canal" (ngModelChange)="demanderTarif()" placeholder="Vide = tous les canaux" maxlength="40" />
          @if (err()['canal']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="g-devise">Devise *</label>
          <input pInputText id="g-devise" name="devise" [(ngModel)]="s.devise" (ngModelChange)="demanderTarif()" maxlength="3" placeholder="XOF" />
          @if (err()['devise']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field check">
          <p-checkbox inputId="g-cont" name="continuite" [binary]="true" [(ngModel)]="s.continuiteRequise" />
          <label for="g-cont">Tranches continues exigées (aucun trou)</label>
        </div>
        <div class="gw-field">
          <label for="g-debut">Début de validité *</label>
          <input pInputText type="date" id="g-debut" name="dateDebut" [(ngModel)]="s.dateDebut" (ngModelChange)="demanderTarif()" />
          @if (err()['dateDebut']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="g-fin">Fin de validité</label>
          <input pInputText type="date" id="g-fin" name="dateFin" [(ngModel)]="s.dateFin" />
          @if (err()['dateFin']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="g-echelle">Arrondi : décimales (0 à 6)</label>
          <input pInputText id="g-echelle" name="arrondiEchelle" inputmode="numeric" [(ngModel)]="s.arrondiEchelle" />
          @if (err()['arrondiEchelle']; as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field">
          <label for="g-mode">Arrondi : mode</label>
          <p-dropdown
            inputId="g-mode"
            name="arrondiMode"
            [options]="modes"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="s.arrondiMode"
            styleClass="w-full"
            appendTo="body"
          />
        </div>
        <div class="gw-field">
          <label for="g-base">Base de calcul *</label>
          <p-dropdown
            inputId="g-base"
            name="baseCalcul"
            [options]="basesCalcul"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="s.baseCalcul"
            (ngModelChange)="onBaseChange()"
            styleClass="w-full"
            appendTo="body"
          />
          @if (err()['baseCalcul']; as e) { <small class="err">{{ e }}</small> }
        </div>
        @if (cumulVisible) {
          <div class="gw-field">
            <label for="g-period">Période du cumul *</label>
            <p-dropdown
              inputId="g-period"
              name="cumulPeriod"
              [options]="periodesCumul"
              optionLabel="label"
              optionValue="value"
              [(ngModel)]="s.cumulPeriod"
              placeholder="Choisir…"
              styleClass="w-full"
              appendTo="body"
            />
            @if (err()['cumulPeriod']; as e) { <small class="err">{{ e }}</small> }
          </div>
          <div class="gw-field">
            <label for="g-scope">Périmètre du cumul *</label>
            <p-dropdown
              inputId="g-scope"
              name="cumulScope"
              [options]="scopesCumul"
              optionLabel="label"
              optionValue="value"
              [(ngModel)]="s.cumulScope"
              placeholder="Choisir…"
              styleClass="w-full"
              appendTo="body"
            />
            @if (err()['cumulScope']; as e) { <small class="err">{{ e }}</small> }
          </div>
        }
        <div class="gw-field full">
          <label>Source</label>
          <app-cible-reseau nom="g-source" [(valeur)]="s.source" (valeurChange)="demanderTarif()" />
          <small class="muted">Point d'origine de la transaction. Une unité couvre sa descendance ; un regroupement, le périmètre de ses membres.</small>
          @if (erreurCible('source'); as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="gw-field full">
          <label>Destination</label>
          <app-cible-reseau nom="g-destination" [(valeur)]="s.destination" (valeurChange)="demanderTarif()" />
          <small class="muted">Toutes : la grille s'applique aussi aux transactions sans destination.</small>
          @if (erreurCible('destination'); as e) { <small class="err">{{ e }}</small> }
        </div>
        <div class="full tarif-actuel">
          <div class="tarif-titre">
            <i class="pi pi-history"></i>
            <strong>Tarif actuel de ce périmètre</strong>
            @if (chargementTarif()) { <i class="pi pi-spin pi-spinner muted"></i> }
          </div>
          @if (tarifActuel(); as t) {
            @if (info(t); as i) {
              <small class="muted">{{ i }}</small>
            }
            @if (erreurTarif(t); as e) {
              <small class="err">{{ e }}</small>
            }
            @if (reponse(t); as r) {
              @switch (r.applicable.statut) {
                @case ('TROUVEE') {
                  @if (r.applicable.grille; as g) {
                    <div>
                      {{ libelleDate(r.date) }}, le moteur applique
                      <a [href]="'/admin/tarification/grilles/' + g.id" target="_blank" rel="noopener">{{ g.code }} v{{ g.version }}</a>
                      · {{ g.libelle }}
                      <span class="muted">
                        (source : {{ corr(g.source) }} ; destination : {{ corr(g.destination) }}{{ g.canal ? ' ; canal ' + g.canal : '' }})
                      </span>
                    </div>
                    @switch (comparaison(g)) {
                      @case ('meme-code') {
                        <small class="note">C'est la version publiée de cette grille : la nouvelle version la remplacera à sa date de début.</small>
                      }
                      @case ('occupe') {
                        <small class="alerte">
                          {{ g.code }} vise exactement la même source, la même destination et le même canal : la publication de
                          cette grille sera refusée tant que {{ g.code }} reste publiée sur la même période. Créez plutôt une
                          nouvelle version de {{ g.code }}.
                        </small>
                      }
                      @case ('autre-canal') {
                        <small class="note">Même source et même destination, canal différent : les deux grilles coexisteront, celle du canal de la transaction l'emportera.</small>
                      }
                      @default {
                        <small class="ok">Une fois publiée, cette grille, plus précise, s'appliquera à la place de {{ g.code }} sur ce périmètre.</small>
                      }
                    }
                    @if (r.applicable.autres.length) {
                      <small class="muted">Déjà masquées par {{ g.code }} (moins précises) : {{ listeRetenues(r.applicable.autres) }}</small>
                    }
                  }
                }
                @case ('AUCUNE') {
                  <small class="note">
                    {{ libelleDate(r.date) }}, aucune grille publiée ne couvre ce périmètre : sans cette grille, ces transactions
                    seraient refusées faute de grille applicable.
                  </small>
                }
                @case ('AMBIGUE') {
                  <small class="alerte">{{ r.applicable.message }}</small>
                  @if (r.applicable.autres.length) {
                    <small class="muted">À égalité : {{ listeRetenues(r.applicable.autres) }}</small>
                  }
                }
              }
              @for (sugg of suggestionsRegroupement(r.applicable); track sugg) {
                <small class="suggestion"><i class="pi pi-lightbulb"></i> {{ sugg }}</small>
              }
            }
          }
        </div>
        @if (erreur()) {
          <p class="gw-msg error full">{{ erreur() }}</p>
        }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="visibleChange.emit(false)"></button>
          <button pButton type="submit" [label]="grille ? 'Enregistrer' : 'Créer le brouillon'" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .err { color: #b91c1c; font-size: 0.78rem; }
    .muted { color: #64748b; font-size: 0.78rem; }
    .check { flex-direction: row; align-items: center; gap: 0.5rem; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
    .tarif-actuel { display: flex; flex-direction: column; gap: 0.3rem; font-size: 0.85rem; background: #f8fafc;
      border: 1px solid #e2e8f0; border-radius: 6px; padding: 0.6rem 0.75rem; }
    .tarif-titre { display: flex; align-items: center; gap: 0.4rem; }
    .tarif-actuel a { font-weight: 600; color: var(--primary-color, #2563eb); }
    .note { color: #1e40af; font-size: 0.8rem; }
    .ok { color: #047857; font-size: 0.8rem; }
    .alerte { color: #b45309; font-size: 0.8rem; font-weight: 500; }
    .suggestion { color: #6d28d9; font-size: 0.8rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class GrilleFormDialogComponent implements OnChanges {
  private readonly tarif = inject(TarifService);

  @Input() visible = false;
  /** null : création. */
  @Input() grille: GrilleDetail | null = null;
  @Input() produits: string[] = [];
  @Output() readonly visibleChange = new EventEmitter<boolean>();
  @Output() readonly saved = new EventEmitter<GrilleDetail>();

  readonly modes = MODES_ARRONDI;
  readonly basesCalcul = BASES_CALCUL;
  readonly periodesCumul = PERIODES_CUMUL;
  readonly scopesCumul = SCOPES_CUMUL;
  readonly saving = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly err = signal<Record<string, string>>({});
  readonly tarifActuel = signal<TarifActuel | null>(null);
  readonly chargementTarif = signal(false);
  s: Saisie = this.vide();

  private readonly demandesTarif = new Subject<void>();

  constructor() {
    this.demandesTarif
      .pipe(
        debounceTime(400),
        switchMap(() => this.chercherTarif()),
        takeUntilDestroyed()
      )
      .subscribe((t) => {
        this.chargementTarif.set(false);
        this.tarifActuel.set(t);
      });
  }

  get codeFige(): boolean {
    return !!this.grille && this.grille.version > 1;
  }

  get cumulVisible(): boolean {
    return estBaseCumul(this.s.baseCalcul);
  }

  onBaseChange(): void {
    if (!estBaseCumul(this.s.baseCalcul)) {
      this.s.cumulPeriod = null;
      this.s.cumulScope = null;
    } else {
      this.s.cumulPeriod ??= 'MONTH';
      this.s.cumulScope ??= 'AGENT';
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible) {
      this.s = this.grille ? this.depuis(this.grille) : this.vide();
      this.erreur.set(null);
      this.err.set({});
      this.tarifActuel.set(null);
      this.demanderTarif();
    }
  }

  demanderTarif(): void {
    this.demandesTarif.next();
  }

  info(t: TarifActuel): string | null {
    return 'info' in t ? t.info : null;
  }

  erreurTarif(t: TarifActuel): string | null {
    return 'erreur' in t ? t.erreur : null;
  }

  reponse(t: TarifActuel): { applicable: GrilleApplicable; date: string } | null {
    return 'applicable' in t ? t : null;
  }

  libelleDate(date: string): string {
    return date === aujourdhui() ? "Aujourd'hui" : 'Le ' + date.split('-').reverse().join('/');
  }

  corr(c: Correspondance | null | undefined): string {
    if (!c || c.niveau === 'TOUTES') {
      return 'toutes';
    }
    if (c.niveau === 'ZONE') {
      return 'zone ' + c.cible;
    }
    return c.via ? `${c.cible} via ${c.via}` : (c.cible ?? '');
  }

  listeRetenues(liste: GrilleRetenue[]): string {
    return liste.map((g) => `${g.code} v${g.version} (${this.corr(g.source)} → ${this.corr(g.destination)})`).join(', ');
  }

  /**
   * Position de la grille saisie face à celle qu'applique le moteur. La requête porte sur la cible même de la saisie :
   * la grille trouvée la vise exactement, ou elle est moins précise.
   */
  comparaison(g: GrilleRetenue): 'meme-code' | 'occupe' | 'autre-canal' | 'plus-precise' {
    if (g.code === this.s.code.trim().toUpperCase()) {
      return 'meme-code';
    }
    if (!this.memeCible(this.s.source, g.source) || !this.memeCible(this.s.destination, g.destination)) {
      return 'plus-precise';
    }
    const canal = this.s.canal.trim().toUpperCase() || null;
    return (g.canal ?? null) === canal ? 'occupe' : 'autre-canal';
  }

  /** Plusieurs unités sœurs ont chacune leur grille : un regroupement les remplacerait par une seule. */
  suggestionsRegroupement(a: GrilleApplicable): string[] {
    const suggestions: string[] = [];
    const cotes = [
      { libelle: 'source', valeur: this.s.source, voisines: a.voisinesSource },
      { libelle: 'destination', valeur: this.s.destination, voisines: a.voisinesDestination }
    ];
    for (const c of cotes) {
      const u = c.valeur.unite;
      if (c.valeur.niveau !== 'UNITE' || !u || u.type === 'REGROUPEMENT' || c.voisines < SEUIL_VOISINES) {
        continue;
      }
      const type = (TYPES_UNITE_LIBELLES[u.type as TypeUnite] ?? u.type).toLowerCase();
      suggestions.push(
        `${c.voisines} grilles publiées visent déjà, en ${c.libelle}, d'autres unités de type ${type} sous le même parent que ` +
          `${u.code}. Si leur tarif est le même, un regroupement de ces unités (réseau › regroupements) permettrait une seule grille.`
      );
    }
    return suggestions;
  }

  private chercherTarif(): Observable<TarifActuel | null> {
    const s = this.s;
    if (!s.produit.trim() || !/^[A-Za-z]{3}$/.test(s.devise.trim())) {
      return of({ info: 'Renseignez le produit et la devise pour voir la grille appliquée aujourd’hui à ce périmètre.' });
    }
    for (const v of [s.source, s.destination]) {
      if ((v.niveau === 'UNITE' && !v.unite) || (v.niveau === 'ZONE' && !v.zone)) {
        return of({ info: 'Terminez le choix de la source et de la destination.' });
      }
    }
    const date = s.dateDebut || aujourdhui();
    const req: ApplicableRequest = {
      produitCode: s.produit.trim().toUpperCase(),
      canalCode: s.canal.trim() ? s.canal.trim().toUpperCase() : null,
      devise: s.devise.trim().toUpperCase(),
      dateCalcul: date,
      source: versLocalisation(s.source),
      destination: versLocalisation(s.destination)
    };
    this.chargementTarif.set(true);
    return this.tarif.grilleApplicable(req).pipe(
      map((applicable): TarifActuel => ({ applicable, date })),
      catchError((e: { message?: string }) => of({ erreur: e?.message ?? 'Tarif actuel indisponible.' })),
      tap(() => this.chargementTarif.set(false))
    );
  }

  private memeCible(v: ValeurCible, c: Correspondance): boolean {
    switch (v.niveau) {
      case 'UNITE':
        return c.niveau === 'UNITE' && !c.via && c.cible === v.unite?.code;
      case 'ZONE':
        return c.niveau === 'ZONE' && c.cible === v.zone;
      default:
        return c.niveau === 'TOUTES';
    }
  }

  enregistrer(): void {
    const local: Record<string, string> = {};
    if (!this.s.code.trim()) local['code'] = 'Obligatoire.';
    if (!this.s.libelle.trim()) local['libelle'] = 'Obligatoire.';
    if (!this.s.produit.trim()) local['produit'] = 'Obligatoire.';
    if (!/^[A-Za-z]{3}$/.test(this.s.devise.trim())) local['devise'] = 'Code ISO sur 3 lettres (ex. XOF).';
    if (!this.s.dateDebut) local['dateDebut'] = 'Obligatoire.';
    if (this.s.dateFin && this.s.dateDebut && this.s.dateFin < this.s.dateDebut) {
      local['dateFin'] = 'La date de fin précède la date de début.';
    }
    const echelle = this.s.arrondiEchelle.trim() === '' ? 0 : Number(this.s.arrondiEchelle);
    if (!Number.isInteger(echelle) || echelle < 0 || echelle > 6) local['arrondiEchelle'] = 'Entier de 0 à 6.';
    if (estBaseCumul(this.s.baseCalcul)) {
      if (!this.s.cumulPeriod) local['cumulPeriod'] = 'Obligatoire pour un cumul.';
      if (!this.s.cumulScope) local['cumulScope'] = 'Obligatoire pour un cumul.';
    }
    for (const cote of ['source', 'destination'] as const) {
      const v = this.s[cote];
      if (v.niveau === 'ZONE' && !v.zone) local[cote + '.zone'] = 'Choisir une zone.';
      if (v.niveau === 'UNITE' && !v.unite) local[cote + '.uniteId'] = 'Choisir une unité.';
    }
    this.err.set(local);
    if (Object.keys(local).length) {
      return;
    }
    const body: GrilleRequest = {
      code: this.s.code.trim().toUpperCase(),
      libelle: this.s.libelle.trim(),
      produit: this.s.produit.trim().toUpperCase(),
      canal: this.s.canal.trim() ? this.s.canal.trim().toUpperCase() : null,
      devise: this.s.devise.trim().toUpperCase(),
      dateDebut: this.s.dateDebut,
      dateFin: this.s.dateFin || null,
      continuiteRequise: this.s.continuiteRequise,
      arrondiEchelle: echelle,
      arrondiMode: this.s.arrondiMode,
      source: versCibleRequest(this.s.source),
      destination: versCibleRequest(this.s.destination),
      baseCalcul: this.s.baseCalcul,
      cumulPeriod: estBaseCumul(this.s.baseCalcul) ? this.s.cumulPeriod : null,
      cumulScope: estBaseCumul(this.s.baseCalcul) ? this.s.cumulScope : null
    };
    this.saving.set(true);
    this.erreur.set(null);
    const req = this.grille ? this.tarif.modifierGrille(this.grille.id, body) : this.tarif.creerGrille(body);
    req.subscribe({
      next: (g) => {
        this.saving.set(false);
        this.saved.emit(g);
        this.visibleChange.emit(false);
      },
      error: (e: { message?: string; details?: ApiErrorDetail[] }) => {
        this.saving.set(false);
        this.erreur.set(e?.message ?? 'Enregistrement impossible.');
        this.err.set(erreursParChamp(e?.details ?? []));
      }
    });
  }

  erreurCible(cote: 'source' | 'destination'): string | undefined {
    const e = this.err();
    return e[cote + '.uniteId'] ?? e[cote + '.zone'] ?? e[cote];
  }

  private vide(): Saisie {
    return {
      code: '',
      libelle: '',
      produit: '',
      canal: '',
      devise: 'XOF',
      dateDebut: aujourdhui(),
      dateFin: '',
      continuiteRequise: true,
      arrondiEchelle: '0',
      arrondiMode: 'HALF_UP',
      baseCalcul: 'TRANSACTION_AMOUNT',
      cumulPeriod: null,
      cumulScope: null,
      source: cibleVide(),
      destination: cibleVide()
    };
  }

  private depuis(g: GrilleDetail): Saisie {
    return {
      code: g.code,
      libelle: g.libelle,
      produit: g.produit,
      canal: g.canal ?? '',
      devise: g.devise,
      dateDebut: g.dateDebut,
      dateFin: g.dateFin ?? '',
      continuiteRequise: g.continuiteRequise,
      arrondiEchelle: String(g.arrondiEchelle),
      arrondiMode: g.arrondiMode,
      baseCalcul: g.baseCalcul ?? 'TRANSACTION_AMOUNT',
      cumulPeriod: g.cumulPeriod ?? null,
      cumulScope: g.cumulScope ?? null,
      source: depuisCible(g.source),
      destination: depuisCible(g.destination)
    };
  }
}
