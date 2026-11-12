import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ApiErrorDetail } from '../../core/interceptors/error.interceptor';
import {
  AUDIENCES,
  IdentityAudience,
  IdentityService,
  Profile,
  ProfileGridLine,
  ProfileKind,
  ProfileServiceLine,
  SENS_LIMITE,
  SensLimite
} from '../../core/service/identity.service';
import { GrilleResume, TarifService } from '../../core/service/tarif.service';
import { RulesService } from '../../core/service/rules.service';

interface Saisie {
  code: string;
  libelle: string;
  description: string;
  audience: IdentityAudience | null;
  services: ProfileServiceLine[];
  /** Lignes de grilles : le code sélectionné pilote produit / canal / devise. */
  grilles: { grilleCode: string; produitCode: string; canalCode: string; devise: string }[];
  limites: { produitCode: string; sens: SensLimite | null; groupeCode: string }[];
}

interface GrilleOption {
  code: string;
  affichage: string;
  produit: string;
  canal: string;
  devise: string;
  libelle: string;
}

interface GroupeOption {
  code: string;
  affichage: string;
  produit: string;
  libelle: string;
}

/** Création ou modification d'un profil SERVICE (produits), TARIFF (grilles) ou LIMIT (plafonds). */
@Component({
  selector: 'app-profil-form-dialog',
  standalone: true,
  imports: [FormsModule, ButtonModule, DialogModule, DropdownModule, InputTextModule, InputTextareaModule],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="visibleChange.emit($event)"
      [header]="titre()"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: estService() ? '40rem' : '56rem' }"
    >
      <form class="gw-form-grid" (ngSubmit)="enregistrer()">
        <div class="gw-field">
          <label for="p-code">Code *</label>
          <input
            pInputText
            id="p-code"
            name="code"
            [(ngModel)]="s.code"
            maxlength="64"
            placeholder="ex. AGENT-STD"
            [readonly]="!!profil"
          />
          <small class="muted">{{ profil ? 'Le code ne change pas.' : 'Mis en majuscules à l’enregistrement.' }}</small>
        </div>
        <div class="gw-field">
          <label for="p-audience">Audience</label>
          <p-dropdown
            inputId="p-audience"
            name="audience"
            [options]="audiences"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="s.audience"
            [showClear]="true"
            placeholder="Optionnel"
            styleClass="w-full"
            appendTo="body"
          />
        </div>
        <div class="gw-field full">
          <label for="p-libelle">Libellé *</label>
          <input pInputText id="p-libelle" name="libelle" [(ngModel)]="s.libelle" maxlength="200" />
        </div>
        <div class="gw-field full">
          <label for="p-desc">Description</label>
          <textarea pInputTextarea id="p-desc" name="description" [(ngModel)]="s.description" rows="2" maxlength="500"></textarea>
        </div>

        @if (estService()) {
          <div class="full bloc">
            <div class="bloc-titre">
              <strong>Services disponibles</strong>
              <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-plus" label="Ajouter" (click)="ajouterService()"></button>
            </div>
            @if (!s.services.length) {
              <p class="muted">Aucun service : le profil n’ouvrira aucun produit.</p>
            }
            @for (svc of s.services; track i; let i = $index) {
              <div class="ligne">
                <p-dropdown
                  [options]="produits()"
                  [(ngModel)]="svc.serviceCode"
                  [name]="'svc-code-' + i"
                  [editable]="true"
                  [filter]="true"
                  placeholder="Produit *"
                  styleClass="w-full"
                  appendTo="body"
                />
                <input pInputText [(ngModel)]="svc.libelle" [name]="'svc-lib-' + i" placeholder="Libellé" maxlength="200" />
                <button pButton type="button" class="p-button-text p-button-danger p-button-sm" icon="pi pi-trash" (click)="s.services.splice(i, 1)"></button>
              </div>
            }
          </div>
        }

        @if (estTarif()) {
          <div class="full bloc">
            <div class="bloc-titre">
              <strong>Grilles tarifaires de référence</strong>
              <button
                pButton
                type="button"
                class="p-button-text p-button-sm"
                icon="pi pi-plus"
                label="Ajouter"
                [disabled]="!grillesOptions().length"
                (click)="ajouterGrille()"
              ></button>
            </div>
            @if (chargementGrilles()) {
              <p class="muted"><i class="pi pi-spin pi-spinner"></i> Chargement des grilles publiées…</p>
            } @else if (!grillesOptions().length) {
              <p class="muted">Aucune grille publiée dans payment-pricing.</p>
            }
            @for (g of s.grilles; track i; let i = $index) {
              <div class="ligne grille">
                <p-dropdown
                  [options]="grillesOptions()"
                  optionLabel="affichage"
                  optionValue="code"
                  [(ngModel)]="g.grilleCode"
                  [name]="'g-code-' + i"
                  (ngModelChange)="appliquerGrille(i, $event)"
                  [filter]="true"
                  filterBy="affichage,code,produit,libelle"
                  placeholder="Choisir une grille *"
                  styleClass="w-full"
                  appendTo="body"
                >
                  <ng-template let-opt pTemplate="item">
                    <div class="opt">
                      <span><strong>{{ opt.code }}</strong> · {{ opt.libelle }}</span>
                      <span class="muted">{{ opt.produit }}@if (opt.canal) { / {{ opt.canal }} } · {{ opt.devise }}</span>
                    </div>
                  </ng-template>
                </p-dropdown>
                <span class="resume" [title]="resumeGrille(g)">{{ resumeGrille(g) || '—' }}</span>
                <button pButton type="button" class="p-button-text p-button-danger p-button-sm" icon="pi pi-trash" (click)="s.grilles.splice(i, 1)"></button>
              </div>
            }
          </div>
        }

        @if (estLimite()) {
          <div class="full bloc">
            <div class="bloc-titre">
              <strong>Groupes de plafonds</strong>
              <button
                pButton
                type="button"
                class="p-button-text p-button-sm"
                icon="pi pi-plus"
                label="Ajouter"
                [disabled]="!groupesOptions().length"
                (click)="ajouterLimite()"
              ></button>
            </div>
            @if (chargementGroupes()) {
              <p class="muted"><i class="pi pi-spin pi-spinner"></i> Chargement des groupes publiés…</p>
            } @else if (!groupesOptions().length) {
              <p class="muted">Aucun groupe de règles dans payment-rules.</p>
            }
            @for (l of s.limites; track i; let i = $index) {
              <div class="ligne limite">
                <p-dropdown
                  [options]="produits()"
                  [(ngModel)]="l.produitCode"
                  [name]="'lim-prod-' + i"
                  [editable]="true"
                  [filter]="true"
                  placeholder="Produit *"
                  styleClass="w-full"
                  appendTo="body"
                />
                <p-dropdown
                  [options]="sensOptions"
                  optionLabel="label"
                  optionValue="value"
                  [(ngModel)]="l.sens"
                  [name]="'lim-sens-' + i"
                  placeholder="Sens *"
                  styleClass="w-full"
                  appendTo="body"
                />
                <p-dropdown
                  [options]="groupesOptions()"
                  optionLabel="affichage"
                  optionValue="code"
                  [(ngModel)]="l.groupeCode"
                  [name]="'lim-grp-' + i"
                  (ngModelChange)="appliquerGroupe(i, $event)"
                  [filter]="true"
                  filterBy="affichage,code,produit,libelle"
                  placeholder="Groupe de règles *"
                  styleClass="w-full"
                  appendTo="body"
                />
                <button pButton type="button" class="p-button-text p-button-danger p-button-sm" icon="pi pi-trash" (click)="s.limites.splice(i, 1)"></button>
              </div>
            }
          </div>
        }

        @if (erreur()) {
          <p class="gw-msg error full">{{ erreur() }}</p>
        }
        <div class="full dialog-actions">
          <button pButton type="button" label="Annuler" class="p-button-text" (click)="visibleChange.emit(false)"></button>
          <button pButton type="submit" [label]="profil ? 'Enregistrer' : 'Créer'" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    .muted { color: #64748b; font-size: 0.78rem; }
    .bloc { display: flex; flex-direction: column; gap: 0.4rem; border: 1px solid #e2e8f0; border-radius: 6px; padding: 0.6rem 0.75rem; }
    .bloc-titre { display: flex; justify-content: space-between; align-items: center; }
    .ligne { display: grid; grid-template-columns: 1fr 1fr auto; gap: 0.4rem; align-items: center; }
    .ligne.grille { grid-template-columns: 1fr minmax(8rem, 12rem) auto; }
    .ligne.limite { grid-template-columns: 1fr 11rem 1.4fr auto; }
    .resume { font-size: 0.8rem; color: #475569; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .opt { display: flex; justify-content: space-between; gap: 1rem; width: 100%; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class ProfilFormDialogComponent implements OnChanges {
  private readonly identity = inject(IdentityService);
  private readonly tarif = inject(TarifService);
  private readonly rules = inject(RulesService);

  @Input() visible = false;
  @Input() profil: Profile | null = null;
  /** Type de profil à créer (ignoré en édition : on garde le kind du profil). */
  @Input() kind: ProfileKind = 'SERVICE';
  @Output() readonly visibleChange = new EventEmitter<boolean>();
  @Output() readonly saved = new EventEmitter<Profile>();

  readonly audiences = AUDIENCES;
  readonly sensOptions = SENS_LIMITE;
  readonly saving = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly chargementGrilles = signal(false);
  readonly chargementGroupes = signal(false);
  readonly grillesOptions = signal<GrilleOption[]>([]);
  readonly groupesOptions = signal<GroupeOption[]>([]);
  readonly produits = signal<string[]>([]);
  s: Saisie = this.vide();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible) {
      this.s = this.profil ? this.depuis(this.profil) : this.vide();
      this.erreur.set(null);
      if (this.estTarif()) {
        this.chargerGrilles();
      } else if (this.estLimite()) {
        this.chargerLimites();
      } else {
        this.chargerProduits();
      }
    }
  }

  titre(): string {
    if (this.profil) {
      return `Modifier ${this.profil.code}`;
    }
    if (this.estTarif()) {
      return 'Nouveau profil tarifaire';
    }
    if (this.estLimite()) {
      return 'Nouveau profil de limite';
    }
    return 'Nouveau profil service';
  }

  estService(): boolean {
    return (this.profil?.kind ?? this.kind) === 'SERVICE';
  }

  estTarif(): boolean {
    return (this.profil?.kind ?? this.kind) === 'TARIFF';
  }

  estLimite(): boolean {
    return (this.profil?.kind ?? this.kind) === 'LIMIT';
  }

  ajouterService(): void {
    this.s.services.push({ serviceCode: '', libelle: '', active: true });
  }

  ajouterGrille(): void {
    this.s.grilles.push({ grilleCode: '', produitCode: '', canalCode: '', devise: '' });
  }

  ajouterLimite(): void {
    this.s.limites.push({ produitCode: '', sens: 'EMETTEUR', groupeCode: '' });
  }

  appliquerGroupe(i: number, code: string | null): void {
    const ligne = this.s.limites[i];
    if (!ligne) {
      return;
    }
    const opt = this.groupesOptions().find((g) => g.code === code);
    if (opt?.produit) {
      ligne.produitCode = opt.produit;
    }
  }

  appliquerGrille(i: number, code: string | null): void {
    const ligne = this.s.grilles[i];
    if (!ligne) {
      return;
    }
    const opt = this.grillesOptions().find((g) => g.code === code);
    if (!opt) {
      ligne.produitCode = '';
      ligne.canalCode = '';
      ligne.devise = '';
      return;
    }
    ligne.grilleCode = opt.code;
    ligne.produitCode = opt.produit;
    ligne.canalCode = opt.canal;
    ligne.devise = opt.devise;
  }

  resumeGrille(g: { produitCode: string; canalCode: string; devise: string }): string {
    if (!g.produitCode) {
      return '';
    }
    return [g.produitCode, g.canalCode || 'tous canaux', g.devise].filter(Boolean).join(' · ');
  }

  enregistrer(): void {
    if (!this.s.code.trim() || !this.s.libelle.trim()) {
      this.erreur.set('Code et libellé sont obligatoires.');
      return;
    }
    const kind: ProfileKind = this.profil?.kind ?? this.kind;
    if (kind === 'SERVICE') {
      for (const svc of this.s.services) {
        if (!svc.serviceCode.trim()) {
          this.erreur.set('Chaque service doit avoir un code produit.');
          return;
        }
      }
    } else if (kind === 'TARIFF') {
      for (const g of this.s.grilles) {
        if (!g.grilleCode.trim() || !g.produitCode.trim()) {
          this.erreur.set('Choisissez une grille tarifaire pour chaque ligne.');
          return;
        }
      }
    } else {
      for (const l of this.s.limites) {
        if (!l.produitCode.trim() || !l.sens || !l.groupeCode.trim()) {
          this.erreur.set('Chaque limite a un produit, un sens et un groupe de règles.');
          return;
        }
      }
    }
    const body = {
      code: this.s.code.trim(),
      libelle: this.s.libelle.trim(),
      description: this.s.description.trim() || null,
      kind,
      audience: this.s.audience,
      services:
        kind === 'SERVICE'
          ? this.s.services.map((x) => ({
              serviceCode: x.serviceCode.trim(),
              libelle: x.libelle?.trim() || null,
              active: true
            }))
          : [],
      grilles:
        kind === 'TARIFF'
          ? this.s.grilles.map((x) => ({
              produitCode: x.produitCode.trim(),
              grilleCode: x.grilleCode.trim(),
              canalCode: x.canalCode?.trim() || null,
              devise: x.devise?.trim() || null
            }))
          : [],
      limites:
        kind === 'LIMIT'
          ? this.s.limites.map((x) => ({
              produitCode: x.produitCode.trim(),
              sens: x.sens as SensLimite,
              groupeCode: x.groupeCode.trim()
            }))
          : []
    };
    this.saving.set(true);
    this.erreur.set(null);
    const req = this.profil ? this.identity.modifierProfil(this.profil.id, body) : this.identity.creerProfil(body);
    req.subscribe({
      next: (p) => {
        this.saving.set(false);
        this.saved.emit(p);
        this.visibleChange.emit(false);
      },
      error: (e: { message?: string; details?: ApiErrorDetail[] }) => {
        this.saving.set(false);
        this.erreur.set(e?.message ?? 'Enregistrement impossible.');
      }
    });
  }

  private chargerProduits(): void {
    this.tarif.rechercherGrilles({ statut: 'PUBLIEE' }, 0, 200).subscribe({
      next: (page) => {
        this.produits.set([...new Set(page.contenu.map((g) => g.produit))].sort());
      },
      error: () => this.produits.set([])
    });
  }

  private chargerGrilles(): void {
    this.chargementGrilles.set(true);
    this.tarif.rechercherGrilles({ statut: 'PUBLIEE' }, 0, 200).subscribe({
      next: (page) => {
        const options = this.dedupeParCode(page.contenu);
        this.grillesOptions.set(options);
        this.produits.set([...new Set(options.map((o) => o.produit))].sort());
        this.chargementGrilles.set(false);
        this.s.grilles.forEach((g, i) => {
          if (g.grilleCode && !g.produitCode) {
            this.appliquerGrille(i, g.grilleCode);
          }
        });
      },
      error: () => {
        this.chargementGrilles.set(false);
        this.grillesOptions.set([]);
        this.erreur.set('Impossible de charger les grilles tarifaires (payment-pricing).');
      }
    });
  }

  /** Une entrée par code : la version la plus élevée (déjà triée version desc par l’API). */
  private dedupeParCode(list: GrilleResume[]): GrilleOption[] {
    const vus = new Set<string>();
    const out: GrilleOption[] = [];
    for (const g of list) {
      if (vus.has(g.code)) {
        continue;
      }
      vus.add(g.code);
      out.push({
        code: g.code,
        libelle: g.libelle,
        produit: g.produit,
        canal: g.canal ?? '',
        devise: g.devise,
        affichage: `${g.code} · ${g.libelle} (${g.produit}${g.canal ? ' / ' + g.canal : ''} · ${g.devise})`
      });
    }
    return out.sort((a, b) => a.code.localeCompare(b.code));
  }

  private chargerLimites(): void {
    this.chargementGroupes.set(true);
    this.chargerProduits();
    this.rules.listerGroupes().subscribe({
      next: (list) => {
        this.groupesOptions.set(
          list
            .map((g) => ({
              code: g.code,
              libelle: g.libelle,
              produit: g.productCode ?? '',
              affichage: g.productCode ? `${g.code} · ${g.libelle} (${g.productCode})` : `${g.code} · ${g.libelle}`
            }))
            .sort((a, b) => a.code.localeCompare(b.code))
        );
        this.chargementGroupes.set(false);
      },
      error: () => {
        this.chargementGroupes.set(false);
        this.groupesOptions.set([]);
        this.erreur.set('Impossible de charger les groupes de règles (payment-rules).');
      }
    });
  }

  private vide(): Saisie {
    return { code: '', libelle: '', description: '', audience: null, services: [], grilles: [], limites: [] };
  }

  private depuis(p: Profile): Saisie {
    return {
      code: p.code,
      libelle: p.libelle,
      description: p.description ?? '',
      audience: p.audience ?? null,
      services: p.services.map((s) => ({ ...s })),
      grilles: p.grilles.map((g) => ({
        grilleCode: g.grilleCode,
        produitCode: g.produitCode,
        canalCode: g.canalCode ?? '',
        devise: g.devise ?? ''
      })),
      limites: (p.limites ?? []).map((l) => ({
        produitCode: l.produitCode,
        sens: l.sens,
        groupeCode: l.groupeCode
      }))
    };
  }
}
