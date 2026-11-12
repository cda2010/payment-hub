import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AutoCompleteCompleteEvent, AutoCompleteModule } from 'primeng/autocomplete';
import { NetworkService, TypeUnite, UniteResume, Zone } from '../../core/service/network.service';
import { typeIcon, typeLabel } from './reseau-format';

/**
 * Choix d'une unité active par recherche côté serveur (20 propositions par frappe) : utilisable quel que soit le
 * nombre d'unités du réseau. La flèche liste les premières unités correspondant aux critères.
 */
@Component({
  selector: 'app-choix-unite',
  standalone: true,
  imports: [FormsModule, AutoCompleteModule],
  template: `
    <p-autoComplete
      [inputId]="inputId"
      [(ngModel)]="saisie"
      (ngModelChange)="choisir($event)"
      [suggestions]="suggestions()"
      (completeMethod)="chercher($event)"
      (onClear)="choisir(null)"
      field="code"
      [dropdown]="true"
      [forceSelection]="true"
      [delay]="250"
      [placeholder]="placeholder"
      [disabled]="disabled"
      [emptyMessage]="vide"
      [showEmptyMessage]="true"
      styleClass="w-full"
      inputStyleClass="w-full"
      appendTo="body"
    >
      <ng-template let-u pTemplate="item">
        <i [class]="icon(u.type)" class="type-icon"></i>
        <strong>{{ u.code }}</strong> · {{ u.libelle }} <span class="type">{{ type(u.type) }}</span>
      </ng-template>
      <ng-template let-u pTemplate="selectedItem">{{ u.code }} — {{ u.libelle }}</ng-template>
    </p-autoComplete>
    @if (plus()) {
      <small class="muted">Plus de {{ suggestions().length }} résultats : précisez le code ou le libellé.</small>
    }
  `,
  styles: `
    :host { display: block; }
    :host ::ng-deep .w-full { width: 100%; }
    .type-icon { color: #64748b; font-size: 0.8rem; margin-right: 0.35rem; }
    .type { color: #64748b; font-size: 0.8rem; margin-left: 0.3rem; }
    .muted { color: #64748b; font-size: 0.78rem; }
  `
})
export class ChoixUniteComponent {
  private readonly network = inject(NetworkService);

  @Input() inputId?: string;
  @Input() zone: Zone | null = null;
  @Input() types: TypeUnite[] | null = null;
  /** Limite aux unités de cette branche (l'unité comprise). */
  @Input() branche: string | null = null;
  @Input() sansRegroupements = false;
  /** Identifiants à ne pas proposer (unité elle-même, parent actuel, membres déjà présents…). */
  @Input() exclure: string[] = [];
  @Input() placeholder = 'Rechercher par code ou libellé';
  @Input() vide = 'Aucune unité active ne correspond';
  @Input() disabled = false;
  /** Le retour de notre propre émission (null pendant la frappe) ne doit pas effacer le texte saisi. */
  @Input() set valeur(v: UniteResume | null) {
    if (v !== null || typeof this.saisie !== 'string') {
      this.saisie = v;
    }
  }
  @Output() readonly valeurChange = new EventEmitter<UniteResume | null>();

  readonly suggestions = signal<UniteResume[]>([]);
  readonly plus = signal(false);
  saisie: UniteResume | string | null = null;

  chercher(e: AutoCompleteCompleteEvent): void {
    const taille = 20;
    this.network
      .rechercher(
        {
          zone: this.zone,
          type: this.types,
          statut: 'ACTIF',
          branche: this.branche,
          sansRegroupements: this.sansRegroupements,
          texte: e.query
        },
        { taille: taille + this.exclure.length, tri: 'code' }
      )
      .subscribe({
        next: (p) => {
          const exclus = new Set(this.exclure);
          const liste = p.contenu.filter((u) => !exclus.has(u.id)).slice(0, taille);
          this.suggestions.set(liste);
          this.plus.set(p.total - this.exclure.length > liste.length);
        },
        error: () => this.suggestions.set([])
      });
  }

  choisir(v: UniteResume | string | null): void {
    const unite = v && typeof v === 'object' ? v : null;
    this.valeurChange.emit(unite);
    this.plus.set(false);
  }

  type = typeLabel;
  icon = typeIcon;
}
