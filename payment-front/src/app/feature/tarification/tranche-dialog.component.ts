import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DividerModule } from 'primeng/divider';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ApiErrorDetail } from '../../core/interceptors/error.interceptor';
import {
  EssaiFormule,
  FormuleDetail,
  FormuleResume,
  GrilleDetail,
  TarifService,
  Tranche,
  TrancheRequest
} from '../../core/service/tarif.service';
import { ParametresFormComponent } from './parametres-form.component';
import {
  SaisieParametres,
  enTexte,
  erreursParChamp,
  montant,
  montantSaisi,
  nbDecimales,
  pourSaisie,
  saisieDepuisValeurs,
  valeursDepuisSaisie
} from './tarif-format';

const CHAMPS_TRANCHE = ['montantMin', 'montantMax', 'formule', 'fraisMin', 'fraisMax'];

/** Écran D du cahier §5 : saisie d'une tranche, formulaire de paramètres généré depuis la formule. */
@Component({
  selector: 'app-tranche-dialog',
  standalone: true,
  imports: [FormsModule, ButtonModule, DialogModule, DividerModule, DropdownModule, InputTextModule, TagModule, ParametresFormComponent],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="visibleChange.emit($event)"
      [header]="tranche ? 'Tranche ' + tranche.ordre + ' — ' + grille.code : 'Nouvelle tranche — ' + grille.code"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '50rem' }"
    >
      <form (ngSubmit)="enregistrer()">
        <h3 class="section">Tranche de montant ({{ grille.devise }})</h3>
        <div class="gw-form-grid">
          <div class="gw-field">
            <label for="t-min">Montant minimum (inclus) *</label>
            <input pInputText id="t-min" name="min" inputmode="decimal" [(ngModel)]="min" [class.ng-invalid]="!!err()['montantMin']" [class.ng-dirty]="!!err()['montantMin']" />
            @if (err()['montantMin']; as e) { <small class="err">{{ e }}</small> }
          </div>
          <div class="gw-field">
            <label for="t-max">Montant maximum (inclus)</label>
            <input pInputText id="t-max" name="max" inputmode="decimal" placeholder="Vide = sans limite" [(ngModel)]="max" [class.ng-invalid]="!!err()['montantMax']" [class.ng-dirty]="!!err()['montantMax']" />
            @if (err()['montantMax']; as e) { <small class="err">{{ e }}</small> }
          </div>
        </div>

        <h3 class="section">Formule</h3>
        <div class="gw-field">
          <p-dropdown
            inputId="t-formule"
            name="formule"
            [options]="options()"
            optionLabel="label"
            optionValue="value"
            [ngModel]="formuleCle()"
            (ngModelChange)="choisirFormule($event)"
            placeholder="Choisir une formule…"
            styleClass="w-full"
            appendTo="body"
          />
          @if (err()['formule']; as e) { <small class="err">{{ e }}</small> }
          @if (schema(); as f) {
            <code class="expr">{{ f.expression }}</code>
          }
        </div>

        @if (schema(); as f) {
          <h3 class="section">Paramètres</h3>
          <app-parametres-form
            prefixe="tp-"
            [schema]="f.parametres"
            [saisie]="saisie"
            [erreurs]="errParams()"
            [devise]="grille.devise"
            (changed)="essai.set(null)"
          />
        }

        <h3 class="section">Plancher et plafond des frais (optionnels)</h3>
        <div class="gw-form-grid">
          <div class="gw-field">
            <label for="t-fmin">Frais minimum ({{ grille.devise }})</label>
            <input pInputText id="t-fmin" name="fmin" inputmode="decimal" [(ngModel)]="fraisMin" />
            @if (err()['fraisMin']; as e) { <small class="err">{{ e }}</small> }
          </div>
          <div class="gw-field">
            <label for="t-fmax">Frais maximum ({{ grille.devise }})</label>
            <input pInputText id="t-fmax" name="fmax" inputmode="decimal" [(ngModel)]="fraisMax" />
            @if (err()['fraisMax']; as e) { <small class="err">{{ e }}</small> }
          </div>
        </div>

        @if (schema()) {
          <p-divider />
          <div class="essai">
            <label for="t-essai">Tester avec le montant</label>
            <input pInputText id="t-essai" name="essai" inputmode="decimal" [(ngModel)]="montantEssai" />
            <button pButton type="button" class="p-button-outlined" icon="pi pi-play" label="Tester" [loading]="essaiEnCours()" (click)="tester()"></button>
          </div>
          @if (essai(); as r) {
            @if (r.valide) {
              <div class="resultat">
                <strong>{{ m(r.frais, grille.devise) }}</strong>
                @if (r.ajustement) {
                  <p-tag [value]="r.ajustement === 'PLANCHER' ? 'Plancher appliqué' : 'Plafond appliqué'" severity="warning" />
                }
                <code>{{ r.details }}</code>
                @if (r.ajustement) {
                  <span class="muted">Résultat de la formule : {{ m(r.fraisFormule, grille.devise) }}</span>
                }
                <span class="muted">Avant l'arrondi de la grille ({{ grille.arrondiEchelle }} décimale(s), {{ grille.arrondiMode }}).</span>
                @if (horsTranche()) {
                  <span class="avert">Ce montant est en dehors de la tranche saisie.</span>
                }
              </div>
            }
          }
        }

        @if (erreur()) {
          <p class="gw-msg error">{{ erreur() }}</p>
        }
        @if (autresErreurs().length) {
          <ul class="autres">
            @for (e of autresErreurs(); track $index) { <li>{{ e }}</li> }
          </ul>
        }
        <div class="dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="visibleChange.emit(false)"></button>
          <button pButton type="submit" [label]="tranche ? 'Enregistrer' : 'Ajouter la tranche'" [loading]="saving()" [disabled]="!schema()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .section { font-size: 0.85rem; font-weight: 600; color: #334155; margin: 1rem 0 0.5rem; }
    .section:first-of-type { margin-top: 0; }
    .err { color: #b91c1c; font-size: 0.78rem; }
    .expr { display: block; margin-top: 0.4rem; font-size: 0.82rem; color: #475569; background: #f8fafc; padding: 0.35rem 0.5rem; border-radius: 6px; }
    .essai { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; font-size: 0.85rem; }
    .essai input { width: 10rem; }
    .resultat { display: flex; flex-wrap: wrap; align-items: center; gap: 0.5rem 0.75rem; margin-top: 0.6rem; font-size: 0.88rem; }
    .resultat strong { font-size: 1.15rem; color: var(--primary-color, #2563eb); }
    .muted { color: #64748b; font-size: 0.8rem; }
    .avert { color: #b45309; font-size: 0.8rem; }
    .autres { color: #b91c1c; font-size: 0.85rem; margin: 0.5rem 0 0; padding-left: 1.2rem; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class TrancheDialogComponent implements OnChanges {
  private readonly tarif = inject(TarifService);

  @Input() visible = false;
  @Input({ required: true }) grille!: GrilleDetail;
  /** null : ajout. */
  @Input() tranche: Tranche | null = null;
  @Input() formules: FormuleResume[] = [];
  @Output() readonly visibleChange = new EventEmitter<boolean>();
  @Output() readonly saved = new EventEmitter<GrilleDetail>();

  readonly schema = signal<FormuleDetail | null>(null);
  readonly formuleCle = signal<string | null>(null);
  readonly saving = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly err = signal<Record<string, string>>({});
  readonly errParams = signal<Record<string, string>>({});
  readonly autresErreurs = signal<string[]>([]);
  readonly essai = signal<EssaiFormule | null>(null);
  readonly essaiEnCours = signal(false);
  readonly options = signal<{ label: string; value: string }[]>([]);

  min = '';
  max = '';
  fraisMin = '';
  fraisMax = '';
  montantEssai = '';
  saisie: SaisieParametres = {};

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['formules'] || changes['tranche']) {
      this.options.set(this.construireOptions());
    }
    if (changes['visible'] && this.visible) {
      this.initialiser();
    }
  }

  choisirFormule(cle: string | null): void {
    this.formuleCle.set(cle);
    this.essai.set(null);
    this.errParams.set({});
    if (!cle) {
      this.schema.set(null);
      return;
    }
    const [code, version] = cle.split('@');
    const precedente = this.schema();
    const conserve = precedente ? valeursDepuisSaisie(precedente.parametres, this.saisie).valeurs : this.tranche?.parametres;
    this.tarif.formule(code, Number(version)).subscribe({
      next: (f) => {
        this.saisie = saisieDepuisValeurs(f.parametres, conserve);
        this.schema.set(f);
      },
      error: (e: { message?: string }) => this.erreur.set(e?.message ?? 'Formule introuvable.')
    });
  }

  tester(): void {
    const f = this.schema();
    if (!f) {
      return;
    }
    const conv = valeursDepuisSaisie(f.parametres, this.saisie);
    const montantTest = montantSaisi(this.montantEssai);
    this.errParams.set(conv.erreurs);
    if (Object.keys(conv.erreurs).length || montantTest === undefined || montantTest === null) {
      this.essai.set(null);
      if (montantTest === undefined || montantTest === null) {
        this.erreur.set("Saisissez un montant d'essai valide.");
      }
      return;
    }
    this.erreur.set(null);
    this.essaiEnCours.set(true);
    this.tarif
      .essayerFormule(
        { code: f.code, version: f.version },
        {
          montant: montantTest,
          parametres: conv.valeurs,
          fraisMin: montantSaisi(this.fraisMin) ?? null,
          fraisMax: montantSaisi(this.fraisMax) ?? null
        }
      )
      .subscribe({
        next: (r) => {
          this.essaiEnCours.set(false);
          this.essai.set(r);
          if (!r.valide) {
            this.repartir(r.erreurs, f);
          }
        },
        error: (e: { message?: string }) => {
          this.essaiEnCours.set(false);
          this.erreur.set(e?.message ?? 'Essai impossible.');
        }
      });
  }

  horsTranche(): boolean {
    const v = montantSaisi(this.montantEssai);
    const min = montantSaisi(this.min);
    const max = montantSaisi(this.max);
    if (v === null || v === undefined) {
      return false;
    }
    return (typeof min === 'number' && v < min) || (typeof max === 'number' && v > max);
  }

  enregistrer(): void {
    const f = this.schema();
    if (!f) {
      return;
    }
    const local: Record<string, string> = {};
    const min = montantSaisi(this.min);
    const max = montantSaisi(this.max);
    const fraisMin = montantSaisi(this.fraisMin);
    const fraisMax = montantSaisi(this.fraisMax);
    if (min === null) local['montantMin'] = 'Obligatoire.';
    if (min === undefined) local['montantMin'] = 'Nombre attendu.';
    if (max === undefined) local['montantMax'] = 'Nombre attendu.';
    if (fraisMin === undefined) local['fraisMin'] = 'Nombre attendu.';
    if (fraisMax === undefined) local['fraisMax'] = 'Nombre attendu.';
    const conv = valeursDepuisSaisie(f.parametres, this.saisie);
    this.err.set(local);
    this.errParams.set(conv.erreurs);
    this.autresErreurs.set([]);
    if (Object.keys(local).length || Object.keys(conv.erreurs).length) {
      return;
    }
    const body: TrancheRequest = {
      montantMin: min as number,
      montantMax: max ?? null,
      formule: { code: f.code, version: f.version },
      parametres: conv.valeurs,
      fraisMin: fraisMin ?? null,
      fraisMax: fraisMax ?? null
    };
    this.saving.set(true);
    this.erreur.set(null);
    const req = this.tranche
      ? this.tarif.modifierTranche(this.grille.id, this.tranche.id, body)
      : this.tarif.ajouterTranche(this.grille.id, body);
    req.subscribe({
      next: (g) => {
        this.saving.set(false);
        this.saved.emit(g);
        this.visibleChange.emit(false);
      },
      error: (e: { message?: string; details?: ApiErrorDetail[] }) => {
        this.saving.set(false);
        this.erreur.set(e?.message ?? 'Enregistrement impossible.');
        this.repartir(e?.details ?? [], f);
      }
    });
  }

  m(v: number | null | undefined, devise?: string): string {
    return montant(v, devise);
  }

  /** Range les erreurs du serveur sous leurs champs ; le reste est listé sous le formulaire. */
  private repartir(details: ApiErrorDetail[], f: FormuleDetail): void {
    const parChamp = erreursParChamp(details, f.parametres);
    const champs: Record<string, string> = {};
    const params: Record<string, string> = {};
    const autres: string[] = [];
    for (const [champ, message] of Object.entries(parChamp)) {
      if (champ.startsWith('parametres.')) {
        params[champ.slice('parametres.'.length)] = message;
      } else if (CHAMPS_TRANCHE.includes(champ) || champ.startsWith('formule.')) {
        champs[champ.startsWith('formule.') ? 'formule' : champ] = message;
      } else {
        autres.push(message);
      }
    }
    this.err.set(champs);
    this.errParams.set(params);
    this.autresErreurs.set(autres);
  }

  private initialiser(): void {
    const t = this.tranche;
    this.min = t ? pourSaisie(t.montantMin) : this.minSuggere();
    this.max = t ? pourSaisie(t.montantMax) : '';
    this.fraisMin = t ? pourSaisie(t.fraisMin) : '';
    this.fraisMax = t ? pourSaisie(t.fraisMax) : '';
    this.montantEssai = t ? pourSaisie(t.montantMax ?? t.montantMin) : this.min;
    this.saisie = {};
    this.schema.set(null);
    this.essai.set(null);
    this.erreur.set(null);
    this.err.set({});
    this.errParams.set({});
    this.autresErreurs.set([]);
    const cle = t ? `${t.formule.code}@${t.formule.version}` : null;
    this.formuleCle.set(null);
    if (cle) {
      this.choisirFormule(cle);
    }
  }

  /** Nouvelle tranche : on propose de reprendre juste après la dernière tranche bornée. */
  private minSuggere(): string {
    const derniere = [...this.grille.tranches].sort((a, b) => a.montantMin - b.montantMin).pop();
    if (!derniere || derniere.montantMax === undefined || derniere.montantMax === null) {
      return derniere ? '' : '0';
    }
    const echelle = nbDecimales(enTexte(derniere.montantMax));
    return pourSaisie(Number((derniere.montantMax + 10 ** -echelle).toFixed(echelle)));
  }

  private construireOptions(): { label: string; value: string }[] {
    const opts = this.formules
      .filter((f) => f.statut === 'ACTIVE')
      .map((f) => ({ label: `${f.libelle} — ${f.code} v${f.version}`, value: `${f.code}@${f.version}` }));
    const t = this.tranche;
    if (t && !opts.some((o) => o.value === `${t.formule.code}@${t.formule.version}`)) {
      opts.push({
        label: `${t.formuleLibelle ?? t.formule.code} — ${t.formule.code} v${t.formule.version} (inactive)`,
        value: `${t.formule.code}@${t.formule.version}`
      });
    }
    return opts;
  }
}
