import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AuthService } from '../../core/auth/auth.service';
import {
  ApprovalPolicy,
  DossierFiche,
  IdentityService,
  KINDS_PARTY,
  OrgNode,
  Party,
  PartyKind,
  UtilisateurOrganisation
} from '../../core/service/identity.service';
import { PartyAliasesComponent } from './party-aliases.component';
import { PartyAppareilsComponent } from './party-appareils.component';
import { PartyDocumentsComponent } from './party-documents.component';
import { PartyFormDialogComponent } from './party-form-dialog.component';
import { PartyProfilsComponent } from './party-profils.component';

/** Fiche d'une identité (party) : coordonnées, profils, alias, appareils et documents. */
@Component({
  selector: 'app-party-detail',
  standalone: true,
  imports: [
    RouterLink,
    ButtonModule,
    ProgressSpinnerModule,
    TagModule,
    PartyAliasesComponent,
    PartyAppareilsComponent,
    PartyDocumentsComponent,
    PartyFormDialogComponent,
    PartyProfilsComponent
  ],
  template: `
    <div class="gw-console-page">
      @if (loading()) {
        <div class="gw-loading">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '40px', height: '40px' }" />
        </div>
      } @else if (!party()) {
        <p class="gw-msg error">{{ error() }}</p>
        <a routerLink="/admin/identite/entreprises">Retour aux entreprises</a>
      }
      @if (!loading() && party(); as p) {
        <div class="gw-page-header">
          <div>
            <nav class="fil">
              <a [routerLink]="listeRetour(p)">
                <i [class]="p.kind === 'ORGANIZATION' ? 'pi pi-building' : 'pi pi-user'"></i>
                {{ p.kind === 'ORGANIZATION' ? 'Entreprises' : 'Clients physiques' }}
              </a>
              <i class="pi pi-angle-right"></i>
              <span>{{ p.code }}</span>
            </nav>
            <h1>
              {{ p.code }}
              <p-tag [value]="kindLabel(p.kind)" [severity]="p.kind === 'ORGANIZATION' ? 'info' : 'success'" />
              <p-tag [value]="statutClient(p)" [severity]="severiteStatut(p)" />
            </h1>
            <p class="subtitle">{{ p.displayName }}</p>
          </div>
          @if (peutEcrire()) {
            <div class="header-actions">
              <button pButton type="button" class="p-button-outlined" icon="pi pi-pencil" label="Modifier" (click)="edition = true"></button>
            </div>
          }
        </div>

        @if (error()) {
          <p class="gw-msg error">{{ error() }}</p>
        }

        <div class="gw-panel">
          <h3>Identité</h3>
          <dl class="fiche">
            <dt>Type</dt><dd>{{ kindLabel(p.kind) }}</dd>
            <dt>Tenant</dt><dd>{{ p.tenantId }}</dd>
            <dt>Téléphone</dt><dd>{{ p.phone || '—' }}</dd>
            <dt>E-mail</dt><dd>{{ p.email || '—' }}</dd>
            <dt>Niveau KYC</dt><dd>{{ kycLabel(p.kycLevel) }}</dd>
            <dt>Statut client</dt><dd>{{ statutClient(p) }}</dd>
            <dt>Réf. externe</dt><dd>{{ p.externalRef || '—' }}</dd>
            <dt>Naissance</dt><dd>{{ dateNaissance() }}</dd>
            <dt>Sexe</dt><dd>{{ sexe() }}</dd>
            <dt>Lieu de naissance</dt><dd>{{ dossier()?.lieuNaissance || '—' }}</dd>
            <dt>Nationalité</dt><dd>{{ dossier()?.nationalite || '—' }}</dd>
          </dl>
        </div>

        @if (p.kind === 'ORGANIZATION') {
          <div class="gw-panel">
            <h3>Hiérarchie</h3>
            @if (hierarchieChargement()) {
              <p class="muted">Chargement de la hiérarchie…</p>
            } @else if (hierarchieErreur()) {
              <p class="gw-msg error">{{ hierarchieErreur() }}</p>
            } @else if (noeuds().length === 0) {
              <p class="muted">Aucune unité dans l’arbre de cette organisation.</p>
            } @else {
              <ul class="arbre">
                @for (ligne of noeuds(); track ligne.node.id) {
                  <li [style.padding-left.rem]="ligne.profondeur * 1.25">
                    <span class="code">{{ ligne.node.code }}</span>
                    {{ ligne.node.name }}
                    <p-tag [value]="typeOrg(ligne.node.type)" severity="secondary" />
                    @if (!ligne.node.active) {
                      <p-tag value="Inactive" severity="warning" />
                    }
                  </li>
                }
              </ul>
            }
          </div>

          <div class="gw-panel">
            <h3>Utilisateurs ({{ utilisateurs().length }})</h3>
            @if (utilisateursChargement()) {
              <p class="muted">Chargement des utilisateurs…</p>
            } @else if (utilisateursErreur()) {
              <p class="gw-msg error">{{ utilisateursErreur() }}</p>
            } @else if (utilisateurs().length === 0) {
              <p class="muted">Aucun utilisateur rattaché à cette organisation.</p>
            } @else {
              <table class="users">
                <thead>
                  <tr>
                    <th>Identifiant</th>
                    <th>Nom</th>
                    <th>Rôle</th>
                    <th>Périmètre</th>
                    <th>Statut</th>
                  </tr>
                </thead>
                <tbody>
                  @for (u of utilisateurs(); track u.id) {
                    <tr [class.inactif]="!u.enabled">
                      <td class="code">{{ u.username }}</td>
                      <td>{{ u.nom }}</td>
                      <td>{{ roleLabel(u.role) }}</td>
                      <td>
                        @for (r of u.rattachements; track r.id) {
                          <span class="rattachement">{{ codeNoeud(r.orgNodeId) }} · {{ scopeLabel(r.scope) }}</span>
                        }
                      </td>
                      <td>
                        <p-tag [value]="u.enabled ? 'Actif' : 'Inactif'" [severity]="u.enabled ? 'success' : 'secondary'" />
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            }
          </div>

          <div class="gw-panel">
            <h3>Règles d’approbation</h3>
            @if (reglesChargement()) {
              <p class="muted">Chargement des règles…</p>
            } @else if (reglesErreur()) {
              <p class="gw-msg error">{{ reglesErreur() }}</p>
            } @else if (politiques().length === 0) {
              <p class="muted">Aucune règle d’approbation pour cette organisation. Le produit applique sa propre politique.</p>
            } @else {
              @for (pol of politiques(); track pol.id) {
                <div class="politique">
                  <div class="pol-entete">
                    <strong>{{ pol.orgCode }}</strong>
                    <span class="code">{{ pol.productCode }}</span>
                    <p-tag [value]="pol.enabled ? 'Active' : 'Inactive'" [severity]="pol.enabled ? 'success' : 'secondary'" />
                    @if (pol.override) {
                      <p-tag value="Prioritaire sur le parent" severity="info" />
                    }
                  </div>
                  <ul class="regles">
                    @for (r of pol.rules; track r.code) {
                      <li>
                        <span class="code">{{ r.code }}</span>
                        {{ r.label }}
                        — {{ profilLabel(r.profile) }}
                        <span class="muted">{{ seuil(r) }}</span>
                      </li>
                    }
                  </ul>
                  @if (pol.assignees.length) {
                    <p class="muted validateurs">
                      Validateurs :
                      @for (a of pol.assignees; track a.level + a.value) {
                        {{ a.level }} {{ a.value }} ({{ a.scope === 'TENANT' ? 'tenant' : 'entité' }})
                      }
                    </p>
                  }
                </div>
              }
            }
          </div>
        }

        <div class="gw-panel">
          <h3>Adresse</h3>
          @if (lignesAdresse().length === 0) {
            <p class="muted">Aucune adresse enregistrée.</p>
          } @else {
            <dl class="fiche">
              @for (ligne of lignesAdresse(); track ligne.cle) {
                <dt>{{ ligne.libelle }}</dt><dd>{{ ligne.valeur }}</dd>
              }
            </dl>
          }
        </div>

        <div class="gw-panel">
          <h3>Comptes ouverts ({{ dossier()?.comptes?.length || 0 }})</h3>
          @if (!dossier()?.comptes?.length) {
            <p class="muted">Aucun compte ouvert.</p>
          } @else {
            <dl class="fiche">
              @for (compte of dossier()!.comptes; track compte.maison + compte.numero) {
                <dt>{{ maison(compte.maison) }}</dt><dd>{{ compte.numero }}</dd>
              }
            </dl>
          }
        </div>

        <app-party-profils [partyId]="p.id" [peutEcrire]="peutEcrire()" kind="SERVICE" [montreOffre]="true" />
        <app-party-profils [partyId]="p.id" [peutEcrire]="peutEcrire()" kind="TARIFF" />
        <app-party-profils [partyId]="p.id" [peutEcrire]="peutEcrire()" kind="LIMIT" />
        <app-party-aliases [partyId]="p.id" [peutEcrire]="peutEcrire()" />
        <app-party-documents [partyId]="p.id" [peutEcrire]="peutEcrire()" />
        <app-party-appareils [partyId]="p.id" [peutEcrire]="peutEcrire()" />
      }
    </div>

    <app-party-form-dialog [(visible)]="edition" [party]="party()" (saved)="apresSauve($event)" />
  `,
  styles: `
    .fil { display: flex; align-items: center; gap: 0.35rem; font-size: 0.85rem; color: #64748b; margin-bottom: 0.35rem; flex-wrap: wrap; }
    .fil a { color: var(--primary-color, #2563eb); text-decoration: none; }
    .fil a:hover { text-decoration: underline; }
    .fiche { display: grid; grid-template-columns: 10rem 1fr 10rem 1fr; gap: 0.4rem 0.75rem; margin: 0; font-size: 0.88rem; }
    .fiche dt { color: #64748b; }
    .fiche dd { margin: 0; }
    h3 { font-size: 0.95rem; margin: 0 0 0.75rem; }
    .muted { margin: 0; color: #64748b; font-size: 0.85rem; }
    .arbre { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.35rem; }
    .arbre li { display: flex; align-items: center; gap: 0.45rem; font-size: 0.88rem; }
    .code { font-family: ui-monospace, monospace; font-weight: 600; }
    .politique { border-top: 1px solid #e2e8f0; padding: 0.75rem 0; }
    .politique:first-of-type { border-top: 0; padding-top: 0; }
    .pol-entete { display: flex; flex-wrap: wrap; align-items: center; gap: 0.4rem; margin-bottom: 0.35rem; }
    .regles { margin: 0; padding-left: 1.1rem; font-size: 0.88rem; }
    .validateurs { margin-top: 0.35rem; }
    .users { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
    .users th { text-align: left; color: #64748b; font-weight: 600; padding: 0.35rem 0.5rem; border-bottom: 1px solid #e2e8f0; }
    .users td { padding: 0.45rem 0.5rem; border-bottom: 1px solid #f1f5f9; vertical-align: top; }
    .rattachement {
      display: inline-block; margin: 0 0.35rem 0.25rem 0; padding: 0.05rem 0.4rem;
      background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 4px; font-size: 0.78rem;
    }
    .inactif { opacity: 0.65; }
    @media (max-width: 900px) { .fiche { grid-template-columns: 9rem 1fr; } }
  `
})
export class PartyDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly identity = inject(IdentityService);
  private readonly auth = inject(AuthService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly party = signal<Party | null>(null);
  readonly dossier = signal<DossierFiche | null>(null);
  readonly noeuds = signal<{ node: OrgNode; profondeur: number }[]>([]);
  readonly politiques = signal<ApprovalPolicy[]>([]);
  readonly hierarchieErreur = signal<string | null>(null);
  readonly reglesErreur = signal<string | null>(null);
  readonly hierarchieChargement = signal(false);
  readonly reglesChargement = signal(false);
  readonly utilisateurs = signal<UtilisateurOrganisation[]>([]);
  readonly utilisateursErreur = signal<string | null>(null);
  readonly utilisateursChargement = signal(false);
  private readonly libellesAdresse: Record<string, string> = {
    countryCode: 'Pays',
    regionCode: 'Région',
    provinceCode: 'Province',
    communeCode: 'Commune',
    city: 'Ville / localité',
    arrondissement: 'Arrondissement',
    sector: 'Secteur',
    district: 'Quartier',
    street: 'Rue',
    buildingNumber: 'Numéro',
    building: 'Bâtiment',
    floor: 'Étage',
    unit: 'Lot',
    postalCode: 'Code postal',
    landmark: 'Repère',
    additionalInfo: 'Complément',
    latitude: 'Latitude',
    longitude: 'Longitude'
  };
  readonly peutEcrire = computed(
    () => !!this.auth.user() && this.auth.hasRole('ADMIN', 'CONFIG_MANAGER', 'OPERATOR', 'GROUP_ADMIN')
  );

  edition = false;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.loading.set(false);
      this.error.set('Identité introuvable.');
      return;
    }
    this.charger(id);
  }

  apresSauve(p: Party): void {
    this.party.set(p);
  }

  listeRetour(p: Party): string {
    return p.kind === 'ORGANIZATION' ? '/admin/identite/entreprises' : '/admin/identite/personnes';
  }

  typeOrg(type: string): string {
    switch (type) {
      case 'GROUP': return 'Groupe';
      case 'LEGAL_ENTITY': return 'Entité légale';
      case 'BRANCH': return 'Agence';
      case 'POS': return 'Point de vente';
      default: return type;
    }
  }

  codeNoeud(id: string): string {
    return this.noeuds().find((ligne) => ligne.node.id === id)?.node.code ?? '—';
  }

  roleLabel(role: string): string {
    switch (role) {
      case 'GROUP_ADMIN': return 'Administrateur groupe';
      case 'TREASURER': return 'Trésorier';
      case 'ACCOUNTANT': return 'Comptable';
      case 'CHECKER': return 'Contrôleur';
      case 'ADMIN': return 'Administrateur';
      default: return role;
    }
  }

  scopeLabel(scope: string): string {
    switch (scope) {
      case 'DIRECT': return 'cette entité';
      case 'DESCENDANTS': return 'descendants';
      case 'SUBTREE': return 'entité et descendants';
      default: return scope;
    }
  }

  profilLabel(code: string): string {
    switch (code) {
      case 'NO_APPROVAL': return 'sans approbation';
      case 'L1_CHECKER': return 'un niveau (contrôleur)';
      case 'L1_L2': return 'deux niveaux';
      default: return code;
    }
  }

  seuil(r: ApprovalPolicy['rules'][number]): string {
    if (r.elseRule) {
      return 'sinon';
    }
    const parts: string[] = [];
    if (r.totalAmountLte != null) {
      parts.push('montant ≤ ' + r.totalAmountLte);
    }
    if (r.lineCountLte != null) {
      parts.push('lignes ≤ ' + r.lineCountLte);
    }
    if (r.maxLineAmountLte != null) {
      parts.push('ligne ≤ ' + r.maxLineAmountLte);
    }
    return parts.join(', ');
  }

  kindLabel(k: PartyKind): string {
    return KINDS_PARTY.find((x) => x.value === k)?.label ?? k;
  }

  statutClient(p: Party): string {
    switch ((p.customerStatus || '').toUpperCase()) {
      case 'ACTIVE': return 'Actif';
      case 'SUSPENDED': return 'Suspendu';
      case 'CLOSED': return 'Clos';
      case 'INACTIVE': return 'Inactif';
      default: return p.customerStatus || (p.active ? 'Actif' : 'Inactif');
    }
  }

  severiteStatut(p: Party): 'success' | 'warning' | 'danger' | 'secondary' {
    switch ((p.customerStatus || '').toUpperCase()) {
      case 'ACTIVE': return 'success';
      case 'SUSPENDED': return 'warning';
      case 'CLOSED':
      case 'INACTIVE': return 'danger';
      default: return p.active ? 'success' : 'secondary';
    }
  }

  dateNaissance(): string {
    const iso = this.dossier()?.dateNaissance;
    if (!iso) return '—';
    const date = new Date(`${iso}T00:00:00`);
    return Number.isNaN(date.getTime()) ? iso : date.toLocaleDateString('fr-FR');
  }

  sexe(): string {
    switch ((this.dossier()?.sexe || '').toUpperCase()) {
      case 'M':
      case 'MASCULIN': return 'Masculin';
      case 'F':
      case 'FEMININ': return 'Féminin';
      default: return this.dossier()?.sexe || '—';
    }
  }

  lignesAdresse(): { cle: string; libelle: string; valeur: string }[] {
    const adresse = this.dossier()?.adresse ?? {};
    return Object.entries(adresse)
      .filter(([, valeur]) => !!valeur)
      .map(([cle, valeur]) => ({ cle, libelle: this.libellesAdresse[cle] ?? cle, valeur }));
  }

  maison(code: string | null | undefined): string {
    switch ((code || '').toUpperCase()) {
      case 'MONEY': return 'Coris Money';
      case 'BANK': return 'Banque';
      case 'MESO': return 'Meso Finance';
      default: return code || 'Compte';
    }
  }

  kycLabel(niveau: string | null | undefined): string {
    switch ((niveau || '').toUpperCase()) {
      case 'FULL': return 'Complet';
      case 'LIGHT': return 'Léger';
      case 'BASIC': return 'Basique';
      case 'STANDARD': return 'Standard';
      default: return niveau || '—';
    }
  }

  private charger(id: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.dossier.set(null);
    this.identity.dossier(id).subscribe({
      next: (dossier) => this.dossier.set(dossier),
      error: () => this.dossier.set(null)
    });
    this.identity.party(id).subscribe({
      next: (p) => {
        this.party.set(p);
        this.loading.set(false);
        if (p.kind === 'ORGANIZATION') {
          this.chargerOrganisation(p.tenantId);
        }
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.party.set(null);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }

  private chargerOrganisation(tenantId: string): void {
    this.hierarchieErreur.set(null);
    this.reglesErreur.set(null);
    this.hierarchieChargement.set(true);
    this.reglesChargement.set(true);
    this.identity.arbreOrg(tenantId).subscribe({
      next: (racines) => {
        this.noeuds.set(this.aplatir(racines, 0));
        this.hierarchieChargement.set(false);
      },
      error: (e: { message?: string }) => {
        this.hierarchieChargement.set(false);
        this.hierarchieErreur.set(e?.message ?? 'Hiérarchie indisponible.');
      }
    });
    this.chargerUtilisateurs(tenantId);
    this.identity.politiquesApprobation(tenantId).subscribe({
      next: (liste) => {
        this.politiques.set([...liste].sort((a, b) => a.orgCode.localeCompare(b.orgCode)));
        this.reglesChargement.set(false);
      },
      error: (e: { message?: string }) => {
        this.reglesChargement.set(false);
        this.reglesErreur.set(e?.message ?? 'Règles d’approbation indisponibles.');
      }
    });
  }

  private chargerUtilisateurs(tenantId: string): void {
    this.utilisateursErreur.set(null);
    this.utilisateursChargement.set(true);
    forkJoin({
      users: this.identity.listerUtilisateurs(tenantId, 'CORPORATE', true),
      parties: this.identity.listerParties(tenantId).pipe(catchError(() => of([] as Party[])))
    })
      .pipe(
        switchMap(({ users, parties }) => {
          const noms = new Map(parties.map((p) => [p.id, p.displayName]));
          if (users.length === 0) {
            return of([] as UtilisateurOrganisation[]);
          }
          return forkJoin(
            users.map((u) =>
              this.identity.membershipsUtilisateur(u.id).pipe(
                catchError(() => of([])),
                map((rattachements) => ({
                  id: u.id,
                  username: u.username,
                  nom: noms.get(u.partyId) || u.username,
                  role: u.role,
                  enabled: u.enabled,
                  rattachements
                }))
              )
            )
          );
        })
      )
      .subscribe({
        next: (liste) => {
          this.utilisateurs.set([...liste].sort((a, b) => a.username.localeCompare(b.username)));
          this.utilisateursChargement.set(false);
        },
        error: (e: { message?: string }) => {
          this.utilisateursChargement.set(false);
          this.utilisateursErreur.set(e?.message ?? 'Utilisateurs indisponibles.');
        }
      });
  }

  private aplatir(noeuds: OrgNode[], profondeur: number): { node: OrgNode; profondeur: number }[] {
    const lignes: { node: OrgNode; profondeur: number }[] = [];
    for (const node of noeuds) {
      lignes.push({ node, profondeur });
      lignes.push(...this.aplatir(node.children ?? [], profondeur + 1));
    }
    return lignes;
  }
}
