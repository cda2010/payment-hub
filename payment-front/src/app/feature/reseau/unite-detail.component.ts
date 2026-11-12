import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../core/auth/auth.service';
import { Couverture, NetworkService, NoeudArbre, UniteDetail } from '../../core/service/network.service';
import { DeplacementDialogComponent } from './deplacement-dialog.component';
import { RegroupementMembresComponent } from './regroupement-membres.component';
import { statutLabel, statutSeverity, typeIcon, typeLabel, typeSeverity } from './reseau-format';
import { UniteAliasesComponent } from './unite-aliases.component';
import { UniteFormDialogComponent } from './unite-form-dialog.component';
import { UniteProfilsComponent } from './unite-profils.component';

/** Fiche d'une unité : position dans l'arbre, unités rattachées et regroupements qui la couvrent ; ou fiche d'un regroupement. */
@Component({
  selector: 'app-unite-detail',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    ButtonModule,
    ProgressSpinnerModule,
    TableModule,
    TagModule,
    DeplacementDialogComponent,
    RegroupementMembresComponent,
    UniteAliasesComponent,
    UniteFormDialogComponent,
    UniteProfilsComponent
  ],
  template: `
    <div class="gw-console-page">
      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else if (!unite()) {
        <p class="gw-msg error">{{ error() }}</p>
        <a routerLink="/admin/reseau/unites">Retour aux unités</a>
      }
      @if (!loading() && unite(); as u) {
        <div class="gw-page-header">
          <div>
            <nav class="fil">
              <a routerLink="/admin/reseau/arbre"><i class="pi pi-sitemap"></i> {{ u.zone }}</a>
              @for (a of u.ascendants; track a.id) {
                <i class="pi pi-angle-right"></i>
                <a [routerLink]="['/admin/reseau/unites', a.id]">{{ a.code }}</a>
              }
            </nav>
            <h1>
              <i [class]="icon(u.type)" class="type-icon"></i> {{ u.code }}
              <p-tag [value]="type(u.type)" [severity]="typeSev(u.type)" />
              <p-tag [value]="statut(u.statut)" [severity]="statutSev(u.statut)" />
            </h1>
            <p class="subtitle">{{ u.libelle }}</p>
          </div>
          @if (peutEcrire()) {
            <div class="header-actions">
              <button pButton type="button" class="p-button-outlined" icon="pi pi-pencil" label="Modifier" (click)="edition = true"></button>
              @if (estDeplacable()) {
                <button pButton type="button" class="p-button-outlined" icon="pi pi-arrows-alt" label="Déplacer" (click)="deplacement = true"></button>
              }
              @if (u.statut === 'ACTIF') {
                <button pButton type="button" class="p-button-outlined p-button-danger" icon="pi pi-ban" label="Désactiver" [loading]="statutEnCours()" (click)="changerStatut(false)"></button>
              } @else {
                <button pButton type="button" class="p-button-outlined p-button-success" icon="pi pi-check" label="Réactiver" [loading]="statutEnCours()" (click)="changerStatut(true)"></button>
              }
              @if (u.typesEnfantsAutorises.length && u.statut === 'ACTIF') {
                <button pButton type="button" icon="pi pi-plus" label="Rattacher une unité" (click)="creationEnfant = true"></button>
              }
            </div>
          }
        </div>

        @if (!peutEcrire()) {
          <p class="bandeau"><i class="pi pi-eye"></i> Consultation seule : votre profil ne permet pas de modifier le réseau.</p>
        }
        @if (u.type === 'PAYS') {
          <p class="bandeau">
            <i class="pi pi-globe"></i>
            Pays de la zone {{ u.zone }}, à la racine du réseau : il ne change ni de zone ni de place ; seul son libellé
            se modifie. Les FSE s'y rattachent et n'en changent plus.
          </p>
        } @else if (u.type === 'FSE') {
          <p class="bandeau">
            <i class="pi pi-lock"></i>
            Un FSE reste rattaché à son pays : il ne peut pas être déplacé.
            @if (u.codeFse) {
              Code FSE Omnicanal : <strong>{{ u.codeFse }}</strong>.
            }
          </p>
        } @else if (estRegroupement()) {
          <p class="bandeau">
            <i class="pi pi-th-large"></i>
            Regroupement rattaché à {{ u.parent?.code }} : il n'accueille aucune unité. Ses membres sont pris dans la
            branche de {{ u.parent?.code }} et couverts avec toute leur descendance. Il peut être déplacé si tous ses
            membres restent dans la branche du nouveau parent.
          </p>
        }
        @if (error()) {
          <p class="gw-msg error">{{ error() }}</p>
        }
        @if (msg()) {
          <p class="gw-msg">{{ msg() }}</p>
        }

        <div class="gw-panel">
          <h3>Identité</h3>
          <dl class="identite">
            <dt>Zone</dt><dd>{{ u.zone }} ({{ u.devise }})</dd>
            @if (u.type === 'FSE' && u.codeFse) {
              <dt>Code FSE</dt><dd>{{ u.codeFse }}</dd>
            }
            <dt>Niveau</dt><dd>{{ u.niveau + ' · ' + type(u.type) }}</dd>
            <dt>Rattachée à</dt>
            <dd>
              @if (u.parent; as p) {
                <a [routerLink]="['/admin/reseau/unites', p.id]">{{ p.code }}</a> — {{ p.libelle }}
              } @else {
                Racine du réseau
              }
            </dd>
            @if (estRegroupement()) {
              <dt>Membres directs</dt><dd>{{ u.nbMembres }}</dd>
            } @else {
              <dt>Peut recevoir</dt>
              <dd>{{ u.typesEnfantsAutorises.length ? typesEnfants(u) : 'Aucune unité (dernier niveau)' }}</dd>
            }
            <dt>Créée</dt><dd>{{ u.createdAt | date: 'dd/MM/yyyy HH:mm' }} {{ u.createdBy ? 'par ' + u.createdBy : '' }}</dd>
            <dt>Modifiée</dt><dd>{{ u.updatedAt | date: 'dd/MM/yyyy HH:mm' }} {{ u.updatedBy ? 'par ' + u.updatedBy : '' }}</dd>
          </dl>
        </div>

        <app-unite-profils [uniteId]="u.id" [peutEcrire]="peutEcrire()" kind="SERVICE" [montreOffre]="true" />
        <app-unite-profils [uniteId]="u.id" [peutEcrire]="peutEcrire()" kind="TARIFF" />
        <app-unite-profils [uniteId]="u.id" [peutEcrire]="peutEcrire()" kind="LIMIT" />
        <app-unite-aliases [uniteId]="u.id" [peutEcrire]="peutEcrire()" [devise]="u.devise" />

        @if (estRegroupement()) {
          <app-regroupement-membres [regroupement]="u" [peutEcrire]="peutEcrire()" (changed)="rafraichir()" />
        } @else {
          <div class="gw-panel">
            <h3>Unités rattachées ({{ enfantsTotal() }})</h3>
            <p-table
              [value]="enfants()"
              dataKey="id"
              styleClass="p-datatable-sm"
              [rowHover]="true"
              [lazy]="true"
              [lazyLoadOnInit]="false"
              (onLazyLoad)="pageEnfants($event)"
              [paginator]="enfantsTotal() > tailleEnfants"
              [first]="debutEnfants"
              [rows]="tailleEnfants"
              [totalRecords]="enfantsTotal()"
            >
              <ng-template pTemplate="header">
                <tr>
                  <th>Code</th>
                  <th>Libellé</th>
                  <th>Type</th>
                  <th>Statut</th>
                  <th class="num">Rattachées / membres</th>
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-e>
                <tr>
                  <td class="nowrap">
                    <i [class]="icon(e.type)" class="petite-icone"></i>
                    <a class="lien" [routerLink]="['/admin/reseau/unites', e.id]">{{ e.code }}</a>
                  </td>
                  <td>{{ e.libelle }}</td>
                  <td><p-tag [value]="type(e.type)" [severity]="typeSev(e.type)" /></td>
                  <td><p-tag [value]="statut(e.statut)" [severity]="statutSev(e.statut)" /></td>
                  <td class="num">{{ (e.type === 'REGROUPEMENT' ? e.nbMembres : e.nbEnfants) || '' }}</td>
                </tr>
              </ng-template>
              <ng-template pTemplate="emptymessage">
                <tr><td colspan="5">Aucune unité rattachée.</td></tr>
              </ng-template>
            </p-table>
          </div>

          <div class="gw-panel">
            <h3>Regroupements qui couvrent cette unité ({{ couvertures().length }})</h3>
            <p class="muted">Directement (l'unité est membre) ou par un ascendant membre du regroupement.</p>
            <p-table [value]="couvertures()" styleClass="p-datatable-sm" [rowHover]="true">
              <ng-template pTemplate="header">
                <tr>
                  <th>Regroupement</th>
                  <th>Libellé</th>
                  <th>Couverture</th>
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-c>
                <tr>
                  <td><a class="lien" [routerLink]="['/admin/reseau/unites', c.regroupement.id]">{{ c.regroupement.code }}</a></td>
                  <td>{{ c.regroupement.libelle }}</td>
                  <td>
                    @if (c.direct) {
                      <p-tag value="Membre direct" severity="info" />
                    } @else {
                      Par <a [routerLink]="['/admin/reseau/unites', c.via.id]">{{ c.via.code }}</a>
                      <span class="muted">({{ type(c.via.type) }})</span>
                    }
                  </td>
                </tr>
              </ng-template>
              <ng-template pTemplate="emptymessage">
                <tr><td colspan="3">Aucun regroupement ne couvre cette unité.</td></tr>
              </ng-template>
            </p-table>
          </div>
        }

        <app-unite-form-dialog [(visible)]="edition" [unite]="u" (saved)="maj($event, 'Unité enregistrée.')" />
        <app-unite-form-dialog [(visible)]="creationEnfant" [parent]="u" (saved)="enfantCree($event)" />
        <app-deplacement-dialog
          [(visible)]="deplacement"
          [unite]="u"
          (saved)="maj($event, messageDeplacement($event))"
        />
      }
    </div>
  `,
  styles: `
    .fil { display: flex; flex-wrap: wrap; align-items: center; gap: 0.35rem; font-size: 0.8rem; margin-bottom: 0.35rem; }
    .fil a { color: #64748b; text-decoration: none; }
    .fil a:hover { color: var(--primary-color, #2563eb); }
    .fil .pi-angle-right { color: #cbd5e1; font-size: 0.7rem; }
    h1 { display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap; }
    .type-icon { color: #64748b; font-size: 1.1rem; }
    .bandeau { background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0.55rem 0.8rem; font-size: 0.85rem; color: #334155; margin: 0 0 1rem; }
    .bandeau i { margin-right: 0.35rem; color: #64748b; }
    .identite { display: grid; grid-template-columns: 10rem 1fr 10rem 1fr; gap: 0.4rem 0.75rem; margin: 0; font-size: 0.88rem; }
    .identite dt { color: #64748b; }
    .identite dd { margin: 0; }
    .identite a, .lien { font-weight: 600; color: var(--primary-color, #2563eb); text-decoration: none; }
    .lien:hover, .identite a:hover { text-decoration: underline; }
    .petite-icone { color: #64748b; font-size: 0.8rem; margin-right: 0.35rem; }
    .muted { color: #64748b; font-size: 0.8rem; }
    .nowrap { white-space: nowrap; }
    .num { text-align: right; }
    @media (max-width: 900px) { .identite { grid-template-columns: 9rem 1fr; } }
  `
})
export class UniteDetailComponent implements OnInit {
  private readonly network = inject(NetworkService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);
  readonly unite = signal<UniteDetail | null>(null);
  readonly enfants = signal<NoeudArbre[]>([]);
  readonly enfantsTotal = signal(0);
  readonly tailleEnfants = 25;
  debutEnfants = 0;
  readonly couvertures = signal<Couverture[]>([]);
  readonly statutEnCours = signal(false);
  readonly peutEcrire = computed(() => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER'));
  readonly estRegroupement = computed(() => this.unite()?.type === 'REGROUPEMENT');
  /** Un pays est la racine et un FSE ne change pas de pays. */
  readonly estDeplacable = computed(() => {
    const t = this.unite()?.type;
    return !!t && t !== 'PAYS' && t !== 'FSE';
  });

  edition = false;
  creationEnfant = false;
  deplacement = false;
  private id = '';

  ngOnInit(): void {
    this.route.paramMap.subscribe((p) => {
      this.id = p.get('id') ?? '';
      this.msg.set(null);
      this.charger();
    });
  }

  charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.network.unite(this.id).subscribe({
      next: (u) => {
        this.loading.set(false);
        this.appliquer(u);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.unite.set(null);
        this.error.set(e?.message ?? 'Unité introuvable.');
      }
    });
  }

  /** Après un changement de membres : met à jour les compteurs sans recharger la section. */
  rafraichir(): void {
    this.network.unite(this.id).subscribe({ next: (u) => this.unite.set(u) });
  }

  maj(u: UniteDetail, message: string): void {
    this.error.set(null);
    this.msg.set(message);
    this.appliquer(u);
  }

  enfantCree(e: UniteDetail): void {
    this.msg.set(`${e.code} rattaché à ${this.unite()?.code}.`);
    this.rafraichir();
    this.debutEnfants = 0;
    this.chargerEnfants(this.id);
  }

  pageEnfants(e: TableLazyLoadEvent): void {
    this.debutEnfants = e.first ?? 0;
    this.chargerEnfants(this.unite()!.id);
  }

  changerStatut(activer: boolean): void {
    const u = this.unite()!;
    const question = activer
      ? `Réactiver ${u.code} ?`
      : `Désactiver ${u.code} ? On ne pourra plus rien y rattacher ni l'ajouter à un regroupement.`;
    if (!window.confirm(question)) {
      return;
    }
    this.statutEnCours.set(true);
    this.error.set(null);
    (activer ? this.network.activer(u.id) : this.network.desactiver(u.id)).subscribe({
      next: (res) => {
        this.statutEnCours.set(false);
        this.maj(res, `${res.code} ${activer ? 'réactivé' : 'désactivé'}.`);
      },
      error: (e: { message?: string }) => {
        this.statutEnCours.set(false);
        this.msg.set(null);
        this.error.set(e?.message ?? 'Changement de statut impossible.');
      }
    });
  }

  messageDeplacement(u: UniteDetail): string {
    const parent = u.parent?.code ?? '';
    return u.type === 'REGROUPEMENT'
      ? `${u.code} est désormais rattaché à ${parent}.`
      : `${u.code} et sa branche sont désormais rattachés à ${parent}.`;
  }

  typesEnfants(u: UniteDetail): string {
    return u.typesEnfantsAutorises.map((t) => typeLabel(t)).join(', ');
  }

  type = typeLabel;
  typeSev = typeSeverity;
  icon = typeIcon;
  statut = statutLabel;
  statutSev = statutSeverity;

  private appliquer(u: UniteDetail): void {
    this.unite.set(u);
    if (u.type === 'REGROUPEMENT') {
      this.enfants.set([]);
      this.enfantsTotal.set(0);
      this.couvertures.set([]);
      return;
    }
    this.debutEnfants = 0;
    this.chargerEnfants(u.id);
    this.network.couvertures(u.id).subscribe({ next: (list) => this.couvertures.set(list) });
  }

  private chargerEnfants(id: string): void {
    this.network.enfants(id, Math.floor(this.debutEnfants / this.tailleEnfants), this.tailleEnfants).subscribe({
      next: (p) => {
        this.enfants.set(p.contenu);
        this.enfantsTotal.set(p.total);
      }
    });
  }
}
