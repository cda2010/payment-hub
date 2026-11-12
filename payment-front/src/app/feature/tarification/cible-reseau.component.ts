import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AutoCompleteCompleteEvent, AutoCompleteModule } from 'primeng/autocomplete';
import { DropdownModule } from 'primeng/dropdown';
import { SelectButtonModule } from 'primeng/selectbutton';
import { FiltreUnites, NetworkService, TypeUnite, UniteResume, ZoneRef } from '../../core/service/network.service';
import { Cible, CibleRequest, Localisation } from '../../core/service/tarif.service';

export type NiveauSaisi = 'TOUTES' | 'ZONE' | 'PAYS' | 'UNITE';

export interface UniteChoisie {
  id: string;
  code: string;
  libelle: string;
  type: string;
  zone?: string | null;
}

/** Saisie d'un côté (source ou destination) : d'une grille, ou de la position d'une transaction. */
export interface ValeurCible {
  niveau: NiveauSaisi;
  zone: string | null;
  pays: string | null;
  unite: UniteChoisie | null;
}

interface Suggestion extends UniteChoisie {
  affichage: string;
}

/** Un niveau de la cascade : les enfants de parent (les pays de la zone pour le premier). */
interface Etage {
  parent: Suggestion | null;
  libelle: string;
  selection: Suggestion | null;
  suggestions: Suggestion[];
  total: number;
}

export const TYPES_UNITE_LIBELLES: Record<TypeUnite, string> = {
  PAYS: 'Pays',
  FSE: 'FSE',
  AGENCE: 'Agence',
  MASTER: 'Master',
  MARCHAND: 'Marchand',
  SOUS_DISTRIBUTEUR: 'Sous-distributeur',
  REGROUPEMENT: 'Regroupement'
};

/** Libellé du niveau qui suit une unité de ce type ; les regroupements s'ajoutent en mode grille. */
const NIVEAU_SUIVANT: Record<string, string> = {
  PAYS: 'FSE',
  FSE: 'Agence',
  AGENCE: 'Master ou marchand',
  MASTER: 'Sous-distributeur',
  MARCHAND: 'Regroupement',
  SOUS_DISTRIBUTEUR: 'Regroupement'
};

const SUGGESTIONS = 20;

export function cibleVide(): ValeurCible {
  return { niveau: 'TOUTES', zone: null, pays: null, unite: null };
}

export function depuisCible(c: Cible | null | undefined): ValeurCible {
  if (!c || c.niveau === 'TOUTES' || !c.zone) {
    return cibleVide();
  }
  if (c.niveau === 'ZONE' || !c.uniteId) {
    return { niveau: 'ZONE', zone: c.zone, pays: null, unite: null };
  }
  return {
    niveau: 'UNITE',
    zone: c.zone,
    pays: null,
    unite: { id: c.uniteId, code: c.uniteCode ?? '', libelle: c.uniteLibelle ?? '', type: c.uniteType ?? '', zone: c.zone }
  };
}

export function versCibleRequest(v: ValeurCible): CibleRequest | null {
  if (v.niveau === 'UNITE' && v.unite) {
    return { uniteId: v.unite.id, zone: v.unite.zone ?? null };
  }
  if (v.niveau === 'ZONE' && v.zone) {
    return { zone: v.zone };
  }
  return null;
}

export function versLocalisation(v: ValeurCible): Localisation | null {
  switch (v.niveau) {
    case 'UNITE':
      return v.unite ? { uniteId: v.unite.id } : null;
    case 'PAYS':
      return v.pays ? { pays: v.pays } : null;
    case 'ZONE':
      return v.zone ? { zone: v.zone } : null;
    default:
      return null;
  }
}

/** Libellé court d'une cible de grille : « Toutes », « Zone UEMOA », « MS-SAHEL (Master) ». */
export function texteCible(c: Cible | null | undefined): string {
  if (!c || c.niveau === 'TOUTES' || !c.zone) {
    return 'Toutes';
  }
  if (c.niveau === 'ZONE' || !c.uniteId) {
    return `Zone ${c.zone}`;
  }
  const type = TYPES_UNITE_LIBELLES[c.uniteType as TypeUnite] ?? c.uniteType ?? '';
  return `${c.uniteCode}${type ? ' (' + type + ')' : ''}`;
}

/**
 * Choix d'une cible dans le réseau de distribution. En mode « grille » : toutes, une zone ou une unité de n'importe
 * quel type (regroupement compris). En mode « transaction » : non renseignée, une unité opérationnelle, un pays ou une
 * zone.
 *
 * Une unité se choisit en descendant la cascade Pays › FSE › Agence › Master ou marchand › Sous-distributeur (on peut
 * s'arrêter à tout niveau), ou par la recherche rapide qui remplit la cascade avec le chemin de l'unité trouvée.
 * Chaque niveau ne propose que les enfants du niveau au-dessus : quelques dizaines d'unités au plus, même dans un
 * réseau de plusieurs milliers.
 */
@Component({
  selector: 'app-cible-reseau',
  standalone: true,
  imports: [FormsModule, AutoCompleteModule, DropdownModule, SelectButtonModule],
  template: `
    <div class="cible">
      <p-selectButton
        [options]="niveaux"
        optionLabel="label"
        optionValue="value"
        [ngModel]="valeur.niveau"
        (ngModelChange)="choisirNiveau($event)"
        [allowEmpty]="false"
        [name]="nom + '-niveau'"
        styleClass="niveaux"
      />
      @switch (valeur.niveau) {
        @case ('ZONE') {
          <p-dropdown
            [options]="zones()"
            optionLabel="affichage"
            optionValue="code"
            [ngModel]="valeur.zone"
            (ngModelChange)="emettre({ zone: $event })"
            placeholder="Choisir une zone"
            [name]="nom + '-zone'"
            styleClass="w-full"
            appendTo="body"
          />
        }
        @case ('PAYS') {
          <p-dropdown
            [options]="pays()"
            optionLabel="affichage"
            optionValue="code"
            [ngModel]="valeur.pays"
            (ngModelChange)="emettre({ pays: $event })"
            [filter]="true"
            filterBy="affichage"
            placeholder="Choisir un pays"
            [name]="nom + '-pays'"
            styleClass="w-full"
            appendTo="body"
          />
        }
        @case ('UNITE') {
          <div class="unite">
            <div class="entete">
              <p-dropdown
                [options]="zones()"
                optionLabel="code"
                optionValue="code"
                [ngModel]="valeur.zone"
                (ngModelChange)="choisirZone($event)"
                placeholder="Zone"
                [name]="nom + '-zone-unite'"
                styleClass="zone-filtre"
                appendTo="body"
              />
              <p-autoComplete
                [ngModel]="recherche()"
                (ngModelChange)="sauter($event)"
                [suggestions]="trouvees()"
                (completeMethod)="chercherPartout($event)"
                field="affichage"
                [minLength]="2"
                [delay]="250"
                placeholder="Recherche rapide : code ou libellé, à tous les niveaux"
                [name]="nom + '-recherche'"
                styleClass="recherche"
                appendTo="body"
              >
                <ng-template let-u pTemplate="item">
                  <div class="sugg">
                    <span><strong>{{ u.code }}</strong> {{ u.libelle }}</span>
                    <span class="muted">{{ typeLibelle(u.type) }} · {{ u.zone }}</span>
                  </div>
                </ng-template>
                <ng-template pTemplate="footer">
                  @if (totalTrouvees() > trouvees().length) {
                    <div class="plus">{{ trouvees().length }} sur {{ totalTrouvees() }} : précisez la saisie</div>
                  }
                </ng-template>
              </p-autoComplete>
            </div>

            @if (!valeur.zone) {
              <small class="muted">Choisissez une zone pour descendre dans le réseau, ou utilisez la recherche rapide.</small>
            } @else {
              <div class="cascade">
                @for (e of etages(); track e.parent?.id; let i = $index) {
                  <div class="etage" [style.padding-left.rem]="i * 0.9">
                    <span class="etage-libelle">
                      @if (i > 0) { <i class="pi pi-angle-right"></i> }
                      {{ e.libelle }}
                    </span>
                    <p-autoComplete
                      [ngModel]="e.selection"
                      (ngModelChange)="choisir(i, $event)"
                      [suggestions]="e.suggestions"
                      (completeMethod)="chercherEtage(i, $event)"
                      field="affichage"
                      [dropdown]="true"
                      [forceSelection]="true"
                      [showClear]="true"
                      [minLength]="0"
                      [delay]="250"
                      [placeholder]="i === 0 ? 'Choisir un pays' : 'Tous (s’arrêter au niveau au-dessus)'"
                      [name]="nom + '-etage-' + i"
                      styleClass="etage-choix"
                      appendTo="body"
                    >
                      <ng-template let-u pTemplate="item">
                        <div class="sugg">
                          <span>
                            @if (u.type === 'REGROUPEMENT') { <i class="pi pi-th-large regr"></i> }
                            <strong>{{ u.code }}</strong> {{ u.libelle }}
                          </span>
                          <span class="muted">{{ typeLibelle(u.type) }}</span>
                        </div>
                      </ng-template>
                      <ng-template pTemplate="footer">
                        @if (e.total > e.suggestions.length) {
                          <div class="plus">{{ e.suggestions.length }} sur {{ e.total }} : tapez un code ou un libellé</div>
                        }
                      </ng-template>
                    </p-autoComplete>
                  </div>
                }
                @if (chargement()) {
                  <small class="muted"><i class="pi pi-spin pi-spinner"></i> Chargement du chemin…</small>
                }
              </div>
            }

            @if (valeur.unite; as u) {
              <div class="chemin">
                <span class="muted">Cible :</span>
                <span>{{ chemin() }}</span>
                <span class="portee">{{ portee(u) }}</span>
              </div>
            }
          </div>
        }
      }
    </div>
  `,
  styles: `
    .cible { display: flex; flex-direction: column; gap: 0.4rem; }
    .unite { display: flex; flex-direction: column; gap: 0.45rem; }
    .entete { display: flex; gap: 0.4rem; }
    .cascade { display: flex; flex-direction: column; gap: 0.3rem; border-left: 2px solid #e2e8f0; padding-left: 0.5rem; }
    .etage { display: grid; grid-template-columns: 10.5rem 1fr; align-items: center; gap: 0.5rem; }
    .etage-libelle { font-size: 0.8rem; color: #475569; white-space: nowrap; }
    .etage-libelle .pi { font-size: 0.7rem; color: #94a3b8; }
    .sugg { display: flex; justify-content: space-between; gap: 1rem; width: 100%; }
    .regr { color: #7c3aed; font-size: 0.8rem; margin-right: 0.2rem; }
    .muted { color: #64748b; font-size: 0.8rem; }
    .plus { padding: 0.4rem 0.75rem; color: #64748b; font-size: 0.78rem; border-top: 1px solid #e2e8f0; }
    .chemin { display: flex; flex-wrap: wrap; gap: 0.4rem; align-items: baseline; font-size: 0.85rem;
      background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 0.35rem 0.6rem; }
    .portee { color: #0f766e; font-size: 0.8rem; }
    :host ::ng-deep .w-full { width: 100%; }
    :host ::ng-deep .recherche, :host ::ng-deep .etage-choix { flex: 1; width: 100%; }
    :host ::ng-deep .recherche .p-autocomplete-input, :host ::ng-deep .etage-choix .p-autocomplete-input { width: 100%; }
    :host ::ng-deep .zone-filtre { width: 8rem; }
    :host ::ng-deep .niveaux .p-button { padding: 0.35rem 0.7rem; font-size: 0.85rem; }
  `
})
export class CibleReseauComponent implements OnInit, OnChanges {
  private readonly reseau = inject(NetworkService);

  @Input() mode: 'grille' | 'transaction' = 'grille';
  @Input() nom = 'cible';
  @Input() valeur: ValeurCible = cibleVide();
  @Output() readonly valeurChange = new EventEmitter<ValeurCible>();

  readonly zones = signal<(ZoneRef & { affichage: string })[]>([]);
  readonly pays = signal<{ code: string; affichage: string }[]>([]);
  readonly etages = signal<Etage[]>([]);
  readonly chargement = signal(false);
  readonly recherche = signal<Suggestion | null>(null);
  readonly trouvees = signal<Suggestion[]>([]);
  readonly totalTrouvees = signal(0);
  niveaux: { label: string; value: NiveauSaisi }[] = [];

  /**
   * Zone dont les étages sont affichés et unité au bout de la cascade (même pendant le chargement de son chemin) ;
   * jeton : ignore les réponses arrivées pour une saisie dépassée.
   */
  private zoneEtages: string | null = null;
  private affichee: string | null = null;
  private jeton = 0;

  ngOnInit(): void {
    this.niveaux =
      this.mode === 'grille'
        ? [
            { label: 'Toutes', value: 'TOUTES' },
            { label: 'Zone', value: 'ZONE' },
            { label: 'Unité', value: 'UNITE' }
          ]
        : [
            { label: 'Non renseignée', value: 'TOUTES' },
            { label: 'Unité', value: 'UNITE' },
            { label: 'Pays', value: 'PAYS' },
            { label: 'Zone', value: 'ZONE' }
          ];
    this.reseau.referentiel().subscribe({
      next: (r) =>
        this.zones.set(
          r.zones
            .filter((z) => z.statut === 'ACTIF')
            .map((z) => ({ ...z, affichage: `${z.code} · ${z.libelle} (${z.devise})` }))
        )
    });
    if (this.mode === 'transaction') {
      this.reseau.rechercher({ type: 'PAYS', statut: 'ACTIF' }, { taille: 500, tri: 'code' }).subscribe({
        next: (page) =>
          this.pays.set(page.contenu.map((p) => ({ code: p.code, affichage: `${p.code} · ${p.libelle} (${p.zone})` })))
      });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['valeur'] || this.valeur?.niveau !== 'UNITE') {
      return;
    }
    const u = this.valeur.unite;
    if (u) {
      if (u.id !== this.affichee) {
        this.ouvrir(this.suggestion(u), false);
      }
    } else if (this.affichee || this.zoneEtages !== this.valeur.zone) {
      this.racine(this.valeur.zone);
    }
  }

  choisirNiveau(niveau: NiveauSaisi): void {
    const zones = this.zones();
    const zone = niveau === 'UNITE' && zones.length === 1 ? zones[0].code : null;
    this.valeurChange.emit({ ...cibleVide(), niveau, zone });
  }

  emettre(partiel: Partial<ValeurCible>): void {
    this.valeurChange.emit({ ...this.valeur, ...partiel });
  }

  choisirZone(zone: string | null): void {
    this.racine(zone);
    this.emettre({ zone, unite: null });
  }

  /** Choix (ou effacement) à un niveau : les niveaux plus profonds tombent, l'unité retenue est la plus profonde. */
  choisir(i: number, u: Suggestion | string | null): void {
    if (typeof u === 'string') {
      return;
    }
    const etages = this.etages().slice(0, i + 1);
    if (!etages[i] || (etages[i].selection?.id ?? null) === (u?.id ?? null)) {
      return;
    }
    this.jeton++;
    this.chargement.set(false);
    etages[i] = { ...etages[i], selection: u };
    this.etages.set(etages);
    const retenue = u ?? etages[i - 1]?.selection ?? null;
    this.affichee = retenue?.id ?? null;
    this.emettre({ unite: retenue, zone: retenue?.zone ?? this.valeur.zone });
    if (u) {
      this.ajouterEtageSiEnfants(u);
    }
  }

  chercherEtage(i: number, e: AutoCompleteCompleteEvent): void {
    const etage = this.etages()[i];
    if (!etage) {
      return;
    }
    const filtre: FiltreUnites = etage.parent
      ? { parentId: etage.parent.id }
      : { zone: this.valeur.zone, type: 'PAYS' };
    this.reseau
      .rechercher(
        { ...filtre, texte: e.query, statut: 'ACTIF', sansRegroupements: this.mode === 'transaction' },
        { taille: SUGGESTIONS, tri: 'code' }
      )
      .subscribe({
        next: (page) => this.majEtage(i, etage, page.contenu.map((x) => this.suggestion(x)), page.total),
        error: () => this.majEtage(i, etage, [], 0)
      });
  }

  chercherPartout(e: AutoCompleteCompleteEvent): void {
    this.reseau
      .rechercher(
        {
          texte: e.query,
          zone: this.valeur.zone,
          statut: 'ACTIF',
          sansRegroupements: this.mode === 'transaction'
        },
        { taille: SUGGESTIONS, tri: 'code' }
      )
      .subscribe({
        next: (page) => {
          this.trouvees.set(page.contenu.map((x) => this.suggestion(x)));
          this.totalTrouvees.set(page.total);
        },
        error: () => {
          this.trouvees.set([]);
          this.totalTrouvees.set(0);
        }
      });
  }

  /** Unité trouvée par la recherche rapide : on remplit la cascade avec son chemin, puis on vide la recherche. */
  sauter(u: Suggestion | string | null): void {
    if (!u || typeof u === 'string') {
      return;
    }
    this.recherche.set(u);
    this.ouvrir(u, true);
    setTimeout(() => this.recherche.set(null));
  }

  /** « UEMOA › BF › FSE-UEMOA › AG-OUAGA › MS-KAYA » */
  chemin(): string {
    const codes = this.etages()
      .map((e) => e.selection?.code)
      .filter((c): c is string => !!c);
    const zone = this.valeur.unite?.zone ?? this.valeur.zone;
    return [zone, ...codes].filter((c) => !!c).join(' › ');
  }

  portee(u: UniteChoisie): string {
    if (u.type === 'REGROUPEMENT') {
      return 'le périmètre de ses membres et leur descendance';
    }
    const etages = this.etages();
    const suivant = etages.find((e) => e.parent?.id === u.id);
    return suivant ? `et toute sa descendance (${suivant.libelle.toLowerCase()}…)` : 'unité sans descendance';
  }

  typeLibelle(type: string): string {
    return TYPES_UNITE_LIBELLES[type as TypeUnite] ?? type;
  }

  private racine(zone: string | null): void {
    this.jeton++;
    this.chargement.set(false);
    this.zoneEtages = zone;
    this.affichee = null;
    this.etages.set(zone ? [this.etage(null)] : []);
  }

  /** Reconstruit la cascade jusqu'à l'unité : ses ascendants (du pays au parent direct) puis elle-même. */
  private ouvrir(u: Suggestion, emettre: boolean): void {
    const jeton = ++this.jeton;
    this.affichee = u.id;
    this.chargement.set(true);
    if (emettre) {
      this.emettre({ zone: u.zone ?? this.valeur.zone, unite: u });
    }
    this.reseau.ascendants(u.id).subscribe({
      next: (ascendants) => {
        if (jeton !== this.jeton) {
          return;
        }
        const chaine = [...ascendants.map((a) => this.suggestion(a)), u];
        this.zoneEtages = u.zone ?? null;
        this.etages.set(chaine.map((s, k) => ({ ...this.etage(k === 0 ? null : chaine[k - 1]), selection: s })));
        this.chargement.set(false);
        this.ajouterEtageSiEnfants(u);
      },
      error: () => {
        if (jeton === this.jeton) {
          this.chargement.set(false);
        }
      }
    });
  }

  /** Ajoute le niveau suivant seulement s'il a quelque chose à proposer (un marchand n'a que d'éventuels regroupements). */
  private ajouterEtageSiEnfants(u: Suggestion): void {
    if (u.type === 'REGROUPEMENT') {
      return;
    }
    const jeton = this.jeton;
    this.reseau
      .rechercher(
        { parentId: u.id, statut: 'ACTIF', sansRegroupements: this.mode === 'transaction' },
        { taille: 1 }
      )
      .subscribe({
        next: (page) => {
          const etages = this.etages();
          if (jeton !== this.jeton || page.total === 0 || etages[etages.length - 1]?.selection?.id !== u.id) {
            return;
          }
          this.etages.set([...etages, { ...this.etage(u), total: page.total }]);
        }
      });
  }

  private etage(parent: Suggestion | null): Etage {
    let libelle = parent ? NIVEAU_SUIVANT[parent.type] ?? 'Unité' : 'Pays';
    if (parent && this.mode === 'grille' && libelle !== 'Regroupement') {
      libelle += ' / regroupement';
    }
    return { parent, libelle, selection: null, suggestions: [], total: 0 };
  }

  private majEtage(i: number, etage: Etage, suggestions: Suggestion[], total: number): void {
    const etages = [...this.etages()];
    if (etages[i]?.parent?.id !== etage.parent?.id) {
      return;
    }
    etages[i] = { ...etages[i], suggestions, total };
    this.etages.set(etages);
  }

  private suggestion(u: UniteChoisie | UniteResume): Suggestion {
    return {
      id: u.id,
      code: u.code,
      libelle: u.libelle,
      type: u.type,
      zone: u.zone,
      affichage: `${u.code} · ${u.libelle} (${this.typeLibelle(u.type)})`
    };
  }
}
