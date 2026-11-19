import { Component, Input, OnChanges, OnDestroy, inject, signal } from '@angular/core';
import { DomSanitizer, SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { DocumentEnregistre, DocumentsService } from '../../core/service/documents.service';
import { IdentityService, PieceDeclaree } from '../../core/service/identity.service';

/** Documents de la personne, lus dans payment-documents. */
@Component({
  selector: 'app-party-documents',
  standalone: true,
  imports: [ButtonModule, DialogModule, ProgressSpinnerModule, TableModule, TagModule],
  template: `
    <div class="gw-panel">
      <h3>Documents ({{ documents().length }})</h3>
      <p class="muted">Pièces enregistrées dans le coffre documentaire pour cette personne.</p>
      @if (declaration()) {
        <p class="declaration">{{ declaration() }}</p>
      }
      @if (message()) {
        <p class="gw-msg error">{{ message() }}</p>
      }
      @if (loading()) {
        <div class="gw-loading compact">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '28px', height: '28px' }" />
        </div>
      } @else if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      } @else {
        <p-table [value]="documents()" styleClass="p-datatable-sm" [rowHover]="true" dataKey="id">
          <ng-template pTemplate="header">
            <tr>
              <th>Document</th>
              <th>Fichier</th>
              <th>Statut</th>
              <th>Taille</th>
              <th>Déposé le</th>
              <th></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-d>
            <tr>
              <td>{{ libelle(d.code) }}</td>
              <td>{{ d.originalName }}</td>
              <td>
                <p-tag [value]="statut(d.status)" [severity]="severite(d.status)" />
              </td>
              <td>{{ taille(d.sizeBytes) }}</td>
              <td>{{ format(d.createdAt) }}</td>
              <td class="actions">
                @if (peutEcrire && d.status !== 'VERIFIED') {
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm p-button-success"
                    icon="pi pi-check"
                    label="Vérifier"
                    [disabled]="enCours() === d.id"
                    (click)="decider(d, 'VERIFIED')"
                  ></button>
                }
                @if (peutEcrire && d.status !== 'REJECTED') {
                  <button
                    pButton
                    type="button"
                    class="p-button-text p-button-sm p-button-danger"
                    icon="pi pi-times"
                    label="Refuser"
                    [disabled]="enCours() === d.id"
                    (click)="decider(d, 'REJECTED')"
                  ></button>
                }
                <button
                  pButton
                  type="button"
                  class="p-button-text p-button-sm"
                  icon="pi pi-eye"
                  label="Ouvrir"
                  [disabled]="enCours() === d.id"
                  (click)="ouvrir(d)"
                ></button>
                <button
                  pButton
                  type="button"
                  class="p-button-text p-button-sm"
                  icon="pi pi-download"
                  label="Télécharger"
                  [disabled]="enCours() === d.id"
                  (click)="telecharger(d)"
                ></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr>
              <td colspan="6">Aucun document enregistré.</td>
            </tr>
          </ng-template>
        </p-table>
      }
    </div>

    <p-dialog
      [header]="apercuTitre"
      [(visible)]="apercuOuvert"
      [modal]="true"
      [draggable]="false"
      [style]="{ width: '52rem', maxWidth: '95vw' }"
      (onHide)="fermerApercu()"
    >
      @if (apercuChargement()) {
        <div class="gw-loading compact">
          <p-progressSpinner strokeWidth="4" [style]="{ width: '28px', height: '28px' }" />
        </div>
      } @else if (apercuErreur()) {
        <p class="gw-msg error">{{ apercuErreur() }}</p>
      } @else if (apercuImage()) {
        <img class="apercu" [src]="apercuSrc" [alt]="apercuTitre" />
      } @else if (apercuPdf()) {
        <iframe class="apercu-pdf" [src]="apercuSrc" [title]="apercuTitre"></iframe>
      } @else if (apercuOuvert) {
        <p class="muted">Ce format s’ouvre par téléchargement.</p>
      }
    </p-dialog>
  `,
  styles: `
    .muted { margin: 0 0 0.75rem; color: #64748b; font-size: 0.85rem; }
    .declaration { margin: 0 0 0.75rem; font-size: 0.9rem; }
    h3 { font-size: 0.95rem; margin: 0 0 0.35rem; }
    .actions { text-align: right; }
    .actions button + button { margin-left: 0.15rem; }
    .apercu { display: block; max-width: 100%; max-height: 70vh; margin: 0 auto; }
    .apercu-pdf { display: block; width: 100%; height: 70vh; border: 0; }
  `
})
export class PartyDocumentsComponent implements OnChanges, OnDestroy {
  private readonly documentsApi = inject(DocumentsService);
  private readonly identity = inject(IdentityService);
  private readonly sanitizer = inject(DomSanitizer);

  @Input({ required: true }) partyId!: string;
  @Input() peutEcrire = false;

  readonly documents = signal<DocumentEnregistre[]>([]);
  readonly declaration = signal<string | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly enCours = signal<string | null>(null);
  readonly apercuChargement = signal(false);
  readonly apercuErreur = signal<string | null>(null);
  readonly apercuImage = signal(false);
  readonly apercuPdf = signal(false);

  apercuOuvert = false;
  apercuTitre = 'Document';
  apercuSrc: SafeUrl | SafeResourceUrl | null = null;
  private apercuObjet: string | null = null;

  ngOnChanges(): void {
    this.charger();
  }

  ngOnDestroy(): void {
    this.libererApercu();
  }

  ouvrir(doc: DocumentEnregistre): void {
    this.message.set(null);
    this.apercuTitre = this.libelle(doc.code);
    this.apercuErreur.set(null);
    this.apercuImage.set(false);
    this.apercuPdf.set(false);
    this.apercuChargement.set(true);
    this.apercuOuvert = true;
    this.enCours.set(doc.id);
    this.documentsApi.fichier(doc.id).subscribe({
      next: (blob) => {
        this.libererApercu();
        const type = (blob.type || doc.contentType || '').toLowerCase();
        this.apercuObjet = URL.createObjectURL(blob);
        const image = type.startsWith('image/');
        const pdf = type === 'application/pdf';
        this.apercuSrc = image
          ? this.sanitizer.bypassSecurityTrustUrl(this.apercuObjet)
          : this.sanitizer.bypassSecurityTrustResourceUrl(this.apercuObjet);
        this.apercuImage.set(image);
        this.apercuPdf.set(pdf);
        this.apercuChargement.set(false);
        this.enCours.set(null);
      },
      error: () => {
        this.apercuChargement.set(false);
        this.apercuErreur.set('Le document n’a pas pu être ouvert.');
        this.enCours.set(null);
      }
    });
  }

  fermerApercu(): void {
    this.libererApercu();
    this.apercuChargement.set(false);
    this.apercuImage.set(false);
    this.apercuPdf.set(false);
  }

  decider(doc: DocumentEnregistre, status: 'VERIFIED' | 'REJECTED'): void {
    this.message.set(null);
    this.enCours.set(doc.id);
    this.documentsApi.statut(doc.id, status).subscribe({
      next: (maj) => {
        this.documents.update((liste) => liste.map((item) => item.id === maj.id ? { ...item, status: maj.status } : item));
        this.enCours.set(null);
      },
      error: () => {
        this.message.set(status === 'VERIFIED'
          ? 'La pièce n’a pas pu être vérifiée.'
          : 'La pièce n’a pas pu être refusée.');
        this.enCours.set(null);
      }
    });
  }

  telecharger(doc: DocumentEnregistre): void {
    this.message.set(null);
    this.enCours.set(doc.id);
    this.documentsApi.fichier(doc.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const lien = document.createElement('a');
        lien.href = url;
        lien.download = doc.originalName || 'document';
        lien.click();
        URL.revokeObjectURL(url);
        this.enCours.set(null);
      },
      error: () => {
        this.message.set('Le document n’a pas pu être téléchargé.');
        this.enCours.set(null);
      }
    });
  }

  private libererApercu(): void {
    if (this.apercuObjet) {
      URL.revokeObjectURL(this.apercuObjet);
      this.apercuObjet = null;
      this.apercuSrc = null;
    }
  }

  private textePiece(piece: PieceDeclaree | null): string | null {
    if (!piece?.typeDocument && !piece?.numeroPiece && !piece?.dateValidite && !piece?.nationalite) {
      return null;
    }
    const morceaux = [this.typePiece(piece.typeDocument)];
    if (piece.numeroPiece) morceaux.push(`n° ${piece.numeroPiece}`);
    if (piece.dateValidite) morceaux.push(`valable jusqu’au ${this.formatDate(piece.dateValidite)}`);
    if (piece.nationalite) morceaux.push(`nationalité ${piece.nationalite}`);
    return morceaux.filter(Boolean).join(' · ');
  }

  private typePiece(type: string | null | undefined): string {
    switch ((type || '').toUpperCase()) {
      case 'CNI': return 'Carte nationale';
      case 'PASSEPORT':
      case 'PASSPORT': return 'Passeport';
      case 'CARTE_CONSULAIRE': return 'Carte consulaire';
      default: return type || 'Pièce déclarée';
    }
  }

  private formatDate(iso: string): string {
    const date = new Date(`${iso}T00:00:00`);
    return Number.isNaN(date.getTime()) ? iso : date.toLocaleDateString('fr-FR');
  }

  libelle(code: string): string {
    const face = code?.includes('_') ? code.slice(code.lastIndexOf('_') + 1) : '';
    const type = code?.includes('_') ? code.slice(0, code.lastIndexOf('_')) : code;
    const nom = type === 'CNI' ? 'Carte nationale' : type === 'PASSEPORT' ? 'Passeport' : type || 'Document';
    const cote = face === 'RECTO' ? 'recto' : face === 'VERSO' ? 'verso' : face === 'SELFIE' ? 'selfie' : '';
    return cote ? `${nom} — ${cote}` : nom;
  }

  statut(status: string): string {
    switch (status) {
      case 'VERIFIED': return 'Vérifié';
      case 'REJECTED': return 'Refusé';
      case 'UPLOADED': return 'Déposé';
      default: return status || '—';
    }
  }

  severite(status: string): 'success' | 'danger' | 'info' | 'secondary' {
    switch (status) {
      case 'VERIFIED': return 'success';
      case 'REJECTED': return 'danger';
      case 'UPLOADED': return 'info';
      default: return 'secondary';
    }
  }

  taille(octets: number): string {
    if (!octets) return '—';
    if (octets < 1024) return `${octets} o`;
    if (octets < 1024 * 1024) return `${Math.round(octets / 1024)} Ko`;
    return `${(octets / (1024 * 1024)).toFixed(1)} Mo`;
  }

  format(iso: string): string {
    if (!iso) return '—';
    const date = new Date(iso);
    return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('fr-FR');
  }

  private charger(): void {
    if (!this.partyId) return;
    this.loading.set(true);
    this.error.set(null);
    this.declaration.set(null);
    this.identity.pieceDeclaree(this.partyId).subscribe({
      next: (piece) => this.declaration.set(this.textePiece(piece)),
      error: () => this.declaration.set(null)
    });
    this.documentsApi.listerPersonne(this.partyId).subscribe({
      next: (liste) => {
        this.documents.set(liste ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.documents.set([]);
        this.error.set('Les documents n’ont pas pu être lus.');
        this.loading.set(false);
      }
    });
  }
}
