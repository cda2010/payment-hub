import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { NetworkService, TypeUnite, UniteDetail, UniteResume } from '../../core/service/network.service';
import { ChoixUniteComponent } from './choix-unite.component';
import { typeLabel } from './reseau-format';

/** Rattache une unité à un autre parent de la même zone ; toute sa branche suit (un regroupement se déplace seul). */
@Component({
  selector: 'app-deplacement-dialog',
  standalone: true,
  imports: [FormsModule, ButtonModule, DialogModule, ChoixUniteComponent],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="visibleChange.emit($event)"
      [header]="unite ? 'Déplacer ' + unite.code : 'Déplacer'"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '36rem' }"
    >
      @if (unite) {
        <form (ngSubmit)="deplacer()">
          <p class="actuel">
            Rattachement actuel :
            <strong>{{ unite.parent ? unite.parent.code + ' — ' + unite.parent.libelle : 'aucun' }}</strong>
          </p>
          <div class="gw-field">
            <label for="d-parent">Nouveau parent *</label>
            <app-choix-unite
              inputId="d-parent"
              [zone]="unite.zone"
              [types]="typesParents()"
              [exclure]="exclus()"
              [(valeur)]="parent"
              [placeholder]="'Rechercher : ' + libelleTypes()"
              vide="Aucune autre unité active autorisée dans la zone"
            />
          </div>
          <p class="muted">
            @if (unite.nbEnfants) {
              Les {{ unite.nbEnfants }} unité(s) rattachée(s) à {{ unite.code }} et toute leur descendance suivent le déplacement.
            }
            @if (unite.type === 'REGROUPEMENT') {
              Tous les membres de {{ unite.code }} doivent se trouver dans la branche du nouveau parent.
            } @else {
              Le déplacement est refusé s'il fait sortir un membre de regroupement de la branche de son regroupement,
              ou si un regroupement couvrait ensuite deux fois la même unité.
            }
          </p>
          @if (erreur()) {
            <p class="gw-msg error">{{ erreur() }}</p>
          }
          <div class="dialog-actions">
            <button pButton type="button" label="Annuler" class="p-button-text" (click)="visibleChange.emit(false)"></button>
            <button pButton type="submit" label="Déplacer" icon="pi pi-arrow-right" [disabled]="!parent" [loading]="saving()"></button>
          </div>
        </form>
      }
    </p-dialog>
  `,
  styles: `
    .actuel { margin: 0 0 0.75rem; font-size: 0.88rem; }
    .muted { color: #64748b; font-size: 0.8rem; line-height: 1.45; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
    :host ::ng-deep .w-full { width: 100%; }
  `
})
export class DeplacementDialogComponent implements OnChanges {
  private readonly network = inject(NetworkService);

  @Input() visible = false;
  @Input() unite: UniteDetail | null = null;
  @Output() readonly visibleChange = new EventEmitter<boolean>();
  @Output() readonly saved = new EventEmitter<UniteDetail>();

  readonly typesParents = signal<TypeUnite[]>([]);
  readonly exclus = signal<string[]>([]);
  readonly saving = signal(false);
  readonly erreur = signal<string | null>(null);
  parent: UniteResume | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible && this.unite) {
      const u = this.unite;
      this.parent = null;
      this.erreur.set(null);
      this.exclus.set([u.id, ...(u.parent ? [u.parent.id] : [])]);
      this.typesParents.set([]);
      this.network.referentiel().subscribe({
        next: (r) => this.typesParents.set(r.types.find((t) => t.code === u.type)?.parentsAutorises ?? []),
        error: (e: { message?: string }) => this.erreur.set(e?.message ?? 'Référentiel indisponible.')
      });
    }
  }

  libelleTypes(): string {
    return this.typesParents().map(typeLabel).join(', ').toLowerCase() || 'unité';
  }

  deplacer(): void {
    if (!this.unite || !this.parent) {
      return;
    }
    this.saving.set(true);
    this.erreur.set(null);
    this.network.deplacer(this.unite.id, this.parent.id).subscribe({
      next: (u) => {
        this.saving.set(false);
        this.saved.emit(u);
        this.visibleChange.emit(false);
      },
      error: (e: { message?: string }) => {
        this.saving.set(false);
        this.erreur.set(e?.message ?? 'Déplacement impossible.');
      }
    });
  }
}
