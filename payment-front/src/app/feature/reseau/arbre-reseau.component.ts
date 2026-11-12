import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TreeNode } from 'primeng/api';
import { AutoCompleteCompleteEvent, AutoCompleteModule, AutoCompleteSelectEvent } from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { TreeTableModule, TreeTableNodeExpandEvent } from 'primeng/treetable';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { AccountAlias, AliasService } from '../../core/service/alias.service';
import { NetworkService, NoeudArbre, StatutUnite, TypeUnite, UniteResume, Zone, ZoneRef } from '../../core/service/network.service';
import { optionsZones, statutLabel, statutSeverity, typeIcon, typeLabel, typeSeverity } from './reseau-format';
import { UniteFormDialogComponent } from './unite-form-dialog.component';

/** Taille d'une page d'enfants : au-delà, une ligne « Afficher la suite » charge la page suivante. */
const PAGE_ENFANTS = 100;

/**
 * Ligne de l'arbre : une unité, un membre affiché sous son regroupement (lien, pas un enfant), ou la ligne
 * « Afficher la suite » d'un nœud dont les enfants ne sont pas tous chargés.
 */
interface Ligne {
  id: string;
  code: string;
  libelle: string;
  type: TypeUnite;
  statut: StatutUnite;
  membre: boolean;
  compte: string;
  regroupement?: boolean;
  /** Enfants (ou membres) déjà demandés au serveur. */
  charge?: boolean;
  chargement?: boolean;
  /** Ligne « Afficher la suite » : nœud parent, page suivante et nombre d'enfants restant à charger. */
  suite?: { parent: TreeNode<Ligne>; page: number; reste: number };
}

/**
 * Arbre du réseau d'une zone, chargé à la demande : les pays d'abord, puis les enfants d'un nœud quand on le déplie
 * (par pages de 100). La recherche ouvre directement le chemin d'une unité, sans charger le reste du réseau.
 */
@Component({
  selector: 'app-arbre-reseau',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    AutoCompleteModule,
    ButtonModule,
    ProgressSpinnerModule,
    SelectButtonModule,
    TagModule,
    TooltipModule,
    TreeTableModule,
    UniteFormDialogComponent
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Arbre du réseau</h1>
          <p class="subtitle">
            Pays › FSE › Agence › Master ou Marchand › Sous-distributeur · regroupements à tout niveau
            @if (total() !== null) {
              · <strong>{{ total() }}</strong> unité(s) en zone {{ zone }}
            }
          </p>
        </div>
        <div class="header-actions">
          <p-selectButton
            [options]="zones()"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="zone"
            [allowEmpty]="false"
            (onChange)="charger()"
          />
          @if (peutEcrire()) {
            <a pButton class="p-button-outlined" icon="pi pi-globe" label="Zones" routerLink="/admin/reseau/zones"></a>
            <button
              pButton
              type="button"
              icon="pi pi-plus"
              label="Nouveau pays"
              [disabled]="!zoneActive()"
              (click)="creationPays = true"
            ></button>
          }
        </div>
      </div>
      @if (peutEcrire()) {
        <p class="aide">
          <i class="pi pi-info-circle"></i>
          @if (zoneActive()) {
            Un pays se crée à la racine de la zone (code ISO sur 2 lettres). Pour créer un FSE, ouvrez la fiche du pays
            puis « Rattacher une unité » ; un FSE reste ensuite dans son pays. Un regroupement se crée de la même façon
            depuis la fiche de l'unité qui le porte.
          } @else {
            La zone {{ zone }} est inactive : réactivez-la depuis l'écran des zones pour y créer des pays.
          }
        </p>
      }

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }

      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else {
        <div class="gw-panel">
          <div class="outils">
            <p-autoComplete
              [(ngModel)]="recherche"
              [suggestions]="suggestions()"
              (completeMethod)="chercher($event)"
              (onSelect)="ouvrirChemin($event)"
              field="code"
              [delay]="300"
              [minLength]="2"
              placeholder="Trouver une unité (code ou libellé)"
              emptyMessage="Aucune unité"
              [showEmptyMessage]="true"
              styleClass="recherche"
              inputStyleClass="recherche-input"
            >
              <ng-template let-u pTemplate="item">
                <i [class]="icon(u.type)" class="type-icon"></i>
                <strong>{{ u.code }}</strong> · {{ u.libelle }} <span class="type">{{ type(u.type) }}</span>
              </ng-template>
            </p-autoComplete>
            @if (ouverture()) {
              <i class="pi pi-spin pi-spinner"></i>
            }
            <span class="espace"></span>
            <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-minus" label="Tout replier" (click)="replier()"></button>
          </div>
          <p-treeTable [value]="noeuds()" styleClass="p-treetable-sm" (onNodeExpand)="deplie($event)">
            <ng-template pTemplate="header">
              <tr>
                <th>Code</th>
                <th>Libellé</th>
                <th>Type</th>
                <th>Statut</th>
                <th class="num">Rattachées / membres</th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-rowNode let-n="rowData">
              @if (n.suite) {
                <tr [ttRow]="rowNode" class="suite">
                  <td colspan="5">
                    <p-treeTableToggler [rowNode]="rowNode" [style]="{ visibility: 'hidden' }" />
                    <button
                      pButton
                      type="button"
                      class="p-button-text p-button-sm"
                      icon="pi pi-angle-double-down"
                      [label]="'Afficher la suite (' + n.suite.reste + ' restante' + (n.suite.reste > 1 ? 's' : '') + ')'"
                      [loading]="!!n.chargement"
                      (click)="suite(rowNode.node)"
                    ></button>
                  </td>
                </tr>
              } @else {
                <tr
                  [ttRow]="rowNode"
                  [class.inactif]="n.statut === 'INACTIF'"
                  [class.membre]="n.membre"
                  [class.cible]="!n.membre && n.id === cible()"
                  [attr.id]="n.membre ? null : 'noeud-' + n.id"
                >
                  <td class="nowrap">
                    <p-treeTableToggler [rowNode]="rowNode" />
                    @if (n.membre) {
                      <i class="pi pi-link type-icon" pTooltip="Membre du regroupement : couvert avec toute sa descendance"></i>
                    }
                    <i [class]="icon(n.type)" class="type-icon"></i>
                    <a class="lien" [routerLink]="['/admin/reseau/unites', n.id]">{{ n.code }}</a>
                    @if (n.chargement) {
                      <i class="pi pi-spin pi-spinner chargement"></i>
                    }
                  </td>
                  <td>
                    <div>{{ n.libelle }}</div>
                    @if (aliasesDe(n.id); as als) {
                      @if (als.length) {
                        <div class="aliases">
                          @for (a of als; track a.id) {
                            <span class="alias" [pTooltip]="a.aliasType + ' · ' + a.externalRef">{{ a.aliasCode }}</span>
                          }
                        </div>
                      }
                    }
                  </td>
                  <td><p-tag [value]="type(n.type)" [severity]="typeSev(n.type)" /></td>
                  <td><p-tag [value]="statut(n.statut)" [severity]="statutSev(n.statut)" /></td>
                  <td class="num">{{ n.compte }}</td>
                </tr>
              }
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td colspan="5">Aucune unité en zone {{ zone }}.</td></tr>
            </ng-template>
          </p-treeTable>
          <p class="legende">
            <i class="pi pi-th-large"></i> Un regroupement se rattache à une unité de n'importe quel niveau ; ses membres
            (<i class="pi pi-link"></i>), pris dans la branche de cette unité, sont couverts avec toute leur descendance.
            <a routerLink="/admin/reseau/regroupements">Tous les regroupements</a>
          </p>
        </div>
      }
    </div>

    <app-unite-form-dialog
      [(visible)]="creationPays"
      typeInitial="PAYS"
      [zoneInitiale]="zone"
      (saved)="charger()"
    />
  `,
  styles: `
    .aide { font-size: 0.82rem; color: #475569; margin: 0 0 0.75rem; }
    .aide i { color: #64748b; margin-right: 0.3rem; }
    .outils { display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.5rem; }
    .espace { flex: 1; }
    :host ::ng-deep .recherche-input { width: 22rem; }
    .type { color: #64748b; font-size: 0.8rem; margin-left: 0.3rem; }
    .type-icon { color: #64748b; font-size: 0.8rem; margin: 0 0.35rem 0 0.15rem; }
    .chargement { color: #64748b; font-size: 0.8rem; margin-left: 0.4rem; }
    .lien { font-weight: 600; color: var(--primary-color, #2563eb); text-decoration: none; }
    .lien:hover { text-decoration: underline; }
    .num { text-align: right; }
    .nowrap { white-space: nowrap; }
    tr.inactif td { opacity: 0.6; }
    tr.membre td { background: #f8fafc; font-size: 0.85rem; }
    tr.cible td { background: #fef9c3; }
    tr.suite td { padding-top: 0; padding-bottom: 0; }
    .aliases { display: flex; flex-wrap: wrap; gap: 0.25rem; margin-top: 0.2rem; }
    .alias {
      font-size: 0.7rem; font-family: ui-monospace, monospace; color: #475569;
      background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 4px; padding: 0.05rem 0.35rem;
    }
    .legende { font-size: 0.8rem; color: #64748b; margin: 0.75rem 0 0; }
    .legende a { color: var(--primary-color, #2563eb); text-decoration: none; margin-left: 0.3rem; }
  `
})
export class ArbreReseauComponent implements OnInit {
  private readonly network = inject(NetworkService);
  private readonly aliasesApi = inject(AliasService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly noeuds = signal<TreeNode<Ligne>[]>([]);
  /** Alias actifs (UNITE) indexés par id d'unité. */
  readonly aliasesParUnite = signal<Record<string, AccountAlias[]>>({});
  readonly peutEcrire = computed(() => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER'));
  readonly total = signal<number | null>(null);
  readonly zonesRef = signal<ZoneRef[]>([]);
  readonly zones = computed(() => optionsZones(this.zonesRef()));
  readonly suggestions = signal<UniteResume[]>([]);
  readonly cible = signal<string | null>(null);
  readonly ouverture = signal(false);

  zone: Zone = '';
  creationPays = false;
  recherche: UniteResume | string | null = null;

  ngOnInit(): void {
    this.network.zones().subscribe({
      next: (zones) => {
        this.zonesRef.set(zones);
        const demandee = this.route.snapshot.queryParamMap.get('zone');
        this.zone =
          zones.find((z) => z.code === demandee)?.code ??
          zones.find((z) => z.statut === 'ACTIF')?.code ??
          zones[0]?.code ??
          '';
        this.charger();
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Zones indisponibles.');
      }
    });
  }

  zoneActive(): boolean {
    return this.zonesRef().some((z) => z.code === this.zone && z.statut === 'ACTIF');
  }

  /** Pays de la zone et leurs FSE, dépliés d'office ; le reste se charge en dépliant. */
  charger(): void {
    if (!this.zone) {
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.cible.set(null);
    this.total.set(null);
    this.chargerAliases();
    this.network.rechercher({ zone: this.zone }, { taille: 1 }).subscribe({ next: (p) => this.total.set(p.total) });
    this.network.arbre(this.zone).subscribe({
      next: async (racines) => {
        const noeuds = racines.map((n) => this.versNoeud(n));
        this.noeuds.set(noeuds);
        this.loading.set(false);
        await Promise.all(noeuds.filter((n) => !n.leaf).map((n) => this.ouvrir(n)));
        const fse = noeuds.flatMap((n) => n.children ?? []).filter((n) => n.data?.type === 'FSE' && !n.leaf);
        await Promise.all(fse.map((n) => this.ouvrir(n)));
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }

  aliasesDe(uniteId: string): AccountAlias[] {
    return this.aliasesParUnite()[uniteId] ?? [];
  }

  deplie(e: TreeTableNodeExpandEvent): void {
    const node = e.node as TreeNode<Ligne>;
    if (!node.data?.charge) {
      this.ouvrir(node);
    }
  }

  /** Charge la page suivante des enfants du nœud parent de la ligne « Afficher la suite ». */
  async suite(ligne: TreeNode<Ligne>): Promise<void> {
    const s = ligne.data?.suite;
    if (!s || ligne.data?.chargement) {
      return;
    }
    ligne.data!.chargement = true;
    this.rafraichir();
    try {
      await this.chargerPage(s.parent, s.page);
    } catch (e) {
      ligne.data!.chargement = false;
      this.signaler(e);
    }
    this.rafraichir();
  }

  replier(): void {
    const replier = (ns: TreeNode<Ligne>[] | undefined): void =>
      ns?.forEach((n) => {
        n.expanded = false;
        replier(n.children);
      });
    replier(this.noeuds());
    this.rafraichir();
  }

  chercher(e: AutoCompleteCompleteEvent): void {
    this.network.rechercher({ zone: this.zone, texte: e.query }, { taille: 20, tri: 'code' }).subscribe({
      next: (p) => this.suggestions.set(p.contenu),
      error: () => this.suggestions.set([])
    });
  }

  /** Déplie les ascendants de l'unité choisie, page après page si besoin, et la met en évidence. */
  async ouvrirChemin(e: AutoCompleteSelectEvent): Promise<void> {
    const u = e.value as UniteResume;
    this.ouverture.set(true);
    try {
      const ascendants = await firstValueFrom(this.network.ascendants(u.id));
      let niveau = this.noeuds();
      for (const a of ascendants) {
        const node = await this.trouver(niveau, a.id);
        if (!node) {
          break;
        }
        if (!node.data!.charge) {
          await this.chargerPage(node, 0);
        }
        node.expanded = true;
        niveau = node.children ?? [];
      }
      this.cible.set(u.id);
      this.rafraichir();
      setTimeout(() => document.getElementById(`noeud-${u.id}`)?.scrollIntoView({ block: 'center', behavior: 'smooth' }));
    } catch (err) {
      this.signaler(err);
    } finally {
      this.ouverture.set(false);
      this.recherche = null;
    }
  }

  type = typeLabel;
  typeSev = typeSeverity;
  icon = typeIcon;
  statut = statutLabel;
  statutSev = statutSeverity;

  private async ouvrir(node: TreeNode<Ligne>): Promise<void> {
    node.expanded = true;
    node.data!.chargement = true;
    this.rafraichir();
    try {
      await this.chargerPage(node, 0);
    } catch (e) {
      node.expanded = false;
      node.data!.chargement = false;
      this.signaler(e);
    }
    this.rafraichir();
  }

  /** Page {@code page} des enfants du nœud (les membres d'un regroupement viennent en une fois). */
  private async chargerPage(node: TreeNode<Ligne>, page: number): Promise<void> {
    const ligne = node.data!;
    const enfants = (node.children ?? []).filter((c) => !c.data?.suite);
    if (ligne.regroupement) {
      const membres = await firstValueFrom(this.network.membres(ligne.id));
      node.children = membres.map((m) => this.versMembre(ligne.id, m.unite));
    } else {
      const p = await firstValueFrom(this.network.enfants(ligne.id, page, PAGE_ENFANTS));
      enfants.push(...p.contenu.map((n) => this.versNoeud(n)));
      const reste = p.total - enfants.length;
      if (reste > 0) {
        enfants.push({
          key: `${ligne.id}/suite`,
          leaf: true,
          data: { ...ligne, membre: false, compte: '', charge: true, chargement: false, suite: { parent: node, page: page + 1, reste } }
        });
      }
      node.children = enfants;
    }
    node.leaf = node.children.length === 0;
    ligne.charge = true;
    ligne.chargement = false;
  }

  /** Nœud de l'unité parmi les frères chargés ; charge les pages suivantes s'il n'y figure pas encore. */
  private async trouver(freres: TreeNode<Ligne>[], id: string): Promise<TreeNode<Ligne> | null> {
    let node = freres.find((n) => n.data?.id === id && !n.data.membre);
    let suite = freres.find((n) => n.data?.suite);
    while (!node && suite) {
      const parent = suite.data!.suite!.parent;
      await this.chargerPage(parent, suite.data!.suite!.page);
      const enfants = parent.children ?? [];
      node = enfants.find((n) => n.data?.id === id && !n.data.membre);
      suite = enfants.find((n) => n.data?.suite);
    }
    return node ?? null;
  }

  private versNoeud(n: NoeudArbre): TreeNode<Ligne> {
    const regroupement = n.type === 'REGROUPEMENT';
    const nb = regroupement ? n.nbMembres : n.nbEnfants;
    return {
      key: n.id,
      data: {
        id: n.id,
        code: n.code,
        libelle: n.libelle,
        type: n.type,
        statut: n.statut,
        membre: false,
        regroupement,
        compte: !nb ? '' : regroupement ? `${nb} membre${nb > 1 ? 's' : ''}` : `${nb}`
      },
      expanded: false,
      leaf: nb === 0,
      children: []
    };
  }

  private versMembre(regroupementId: string, m: UniteResume): TreeNode<Ligne> {
    return {
      key: `${regroupementId}/${m.id}`,
      data: { id: m.id, code: m.code, libelle: m.libelle, type: m.type, statut: m.statut, membre: true, compte: '' },
      leaf: true
    };
  }

  /** Le TreeTable ne relit l'arbre que si la référence de sa valeur change. */
  private rafraichir(): void {
    this.noeuds.update((n) => [...n]);
  }

  private signaler(e: unknown): void {
    this.error.set((e as { message?: string })?.message ?? 'Chargement impossible.');
  }

  private chargerAliases(): void {
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
}
