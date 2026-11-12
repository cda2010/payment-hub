import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../core/auth/auth.service';
import { IdentityService, KINDS_PARTY, Party, PartyKind } from '../../core/service/identity.service';
import { PartyAliasesComponent } from './party-aliases.component';
import { PartyFormDialogComponent } from './party-form-dialog.component';
import { PartyProfilsComponent } from './party-profils.component';

/** Fiche d'une identité (party) : coordonnées, profils d'offre et alias de comptes. */
@Component({
  selector: 'app-party-detail',
  standalone: true,
  imports: [
    RouterLink,
    ButtonModule,
    ProgressSpinnerModule,
    TagModule,
    PartyAliasesComponent,
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
        <a routerLink="/admin/identite/parties">Retour aux identités</a>
      }
      @if (!loading() && party(); as p) {
        <div class="gw-page-header">
          <div>
            <nav class="fil">
              <a routerLink="/admin/identite/parties"><i class="pi pi-users"></i> Identités</a>
              <i class="pi pi-angle-right"></i>
              <span>{{ p.code }}</span>
            </nav>
            <h1>
              {{ p.code }}
              <p-tag [value]="kindLabel(p.kind)" [severity]="p.kind === 'ORGANIZATION' ? 'info' : 'success'" />
              <p-tag [value]="p.active ? 'Actif' : 'Inactif'" [severity]="p.active ? 'success' : 'secondary'" />
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
            <dt>Réf. externe</dt><dd>{{ p.externalRef || '—' }}</dd>
            <dt>Statut</dt><dd>{{ p.active ? 'Actif' : 'Inactif' }}</dd>
          </dl>
        </div>

        <app-party-profils [partyId]="p.id" [peutEcrire]="peutEcrire()" kind="SERVICE" [montreOffre]="true" />
        <app-party-profils [partyId]="p.id" [peutEcrire]="peutEcrire()" kind="TARIFF" />
        <app-party-profils [partyId]="p.id" [peutEcrire]="peutEcrire()" kind="LIMIT" />
        <app-party-aliases [partyId]="p.id" [peutEcrire]="peutEcrire()" />
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

  kindLabel(k: PartyKind): string {
    return KINDS_PARTY.find((x) => x.value === k)?.label ?? k;
  }

  private charger(id: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.identity.party(id).subscribe({
      next: (p) => {
        this.party.set(p);
        this.loading.set(false);
      },
      error: (e: { message?: string }) => {
        this.loading.set(false);
        this.party.set(null);
        this.error.set(e?.message ?? 'Chargement impossible.');
      }
    });
  }
}
