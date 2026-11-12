import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MultiSelectModule } from 'primeng/multiselect';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import {
  ACCESS_CHANNELS,
  PRODUCT_AUDIENCES,
} from '../../core/service/gateway.models';
import {
  CmsAdminService,
  CmsCollectionDto,
  CmsCollectionUpsert,
  CmsDocumentDto,
  CmsDocumentUpsert,
  CmsFieldType,
  CmsSchemaField,
  schemaFieldsOf,
} from '../../core/service/cms-admin.service';

/**
 * Modèle :
 *  - Rubrique = conteneur + schéma d’attributs personnalisés
 *  - Article  = entrée sous une rubrique, formulaire = attributs de CETTE rubrique
 */

const FIELD_TYPE_OPTIONS: { label: string; value: CmsFieldType }[] = [
  { label: 'Texte court', value: 'string' },
  { label: 'Texte long', value: 'text' },
  { label: 'Nombre', value: 'number' },
  { label: 'Oui / Non', value: 'boolean' },
  { label: 'Choix (liste)', value: 'enum' },
  { label: 'Liste de valeurs', value: 'string[]' },
];

type RubriqueForm = {
  code: string;
  kind: string;
  enabled: boolean;
  priority: number;
  icon: string;
  titleFr: string;
  titleEn: string;
  descriptionFr: string;
  channels: string[];
  audiences: string[];
  attributes: CmsSchemaField[];
};

type ArticleForm = {
  code: string;
  enabled: boolean;
  priority: number;
  titleFr: string;
  titleEn: string;
  summaryFr: string;
  bodyFr: string;
  bodyEn: string;
  attrs: Record<string, unknown>;
};

@Component({
  selector: 'app-content-cms-admin',
  standalone: true,
  imports: [
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    InputTextareaModule,
    InputNumberModule,
    CheckboxModule,
    DropdownModule,
    MultiSelectModule,
    TagModule,
    TooltipModule,
  ],
  template: `
    <div class="gw-console-page">
      <div class="gw-page-header">
        <div>
          <h1>Rubriques & articles</h1>
          <p class="subtitle">
            Chaque rubrique a ses propres attributs. Les articles se créent
            <em>dans</em> une rubrique : le formulaire suit son schéma.
          </p>
        </div>
        <div class="actions">
          <button
            pButton
            type="button"
            class="p-button-outlined"
            icon="pi pi-refresh"
            label="Actualiser"
            [disabled]="loading()"
            (click)="reload()"
          ></button>
          <button
            pButton
            type="button"
            icon="pi pi-plus"
            label="Nouvelle rubrique"
            (click)="openCreateRubrique()"
          ></button>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }
      @if (msg()) {
        <p class="gw-msg">{{ msg() }}</p>
      }

      <div class="layout">
        <aside class="panel side">
          <div class="panel-head">
            <span class="label">Rubriques</span>
            <button
              pButton
              type="button"
              class="p-button-text p-button-sm"
              icon="pi pi-plus"
              pTooltip="Nouvelle rubrique"
              (click)="openCreateRubrique()"
            ></button>
          </div>

          @for (r of rubriques(); track r.id) {
            <button
              type="button"
              class="rubrique"
              [class.on]="selected()?.code === r.code"
              (click)="selectRubrique(r)"
            >
              <span class="rubrique-title">{{ titleOf(r) }}</span>
              <span class="rubrique-meta">
                <code>{{ r.code }}</code>
                · {{ attrCount(r) }} attribut{{ attrCount(r) === 1 ? '' : 's' }}
              </span>
            </button>
          } @empty {
            <div class="empty-box">
              <p>Aucune rubrique.</p>
              <button
                pButton
                type="button"
                label="Créer une rubrique"
                (click)="openCreateRubrique()"
              ></button>
            </div>
          }
        </aside>

        <section class="panel main">
          @if (selected(); as rubrique) {
            <div class="panel-head articles-head">
              <div>
                <div class="eyebrow">Articles</div>
                <h2>{{ titleOf(rubrique) }}</h2>
              </div>
              <div class="head-actions">
                <button
                  pButton
                  type="button"
                  class="p-button-outlined p-button-sm"
                  icon="pi pi-cog"
                  label="Attributs"
                  (click)="openEditRubrique(rubrique)"
                ></button>
                <button
                  pButton
                  type="button"
                  icon="pi pi-plus"
                  label="Nouvel article"
                  (click)="openCreateArticle()"
                ></button>
              </div>
            </div>

            @if (schemaFields().length) {
              <div class="schema-banner">
                @for (f of schemaFields(); track f.key) {
                  <span class="chip">{{ f.label }}</span>
                }
              </div>
            } @else {
              <div class="schema-banner empty-schema">
                Aucun attribut personnalisé —
                <button
                  pButton
                  type="button"
                  class="p-button-link p-button-sm"
                  label="en définir"
                  (click)="openEditRubrique(rubrique)"
                ></button>
                (optionnel : un article peut n’avoir qu’un titre).
              </div>
            }

            <p-table
              [value]="articles()"
              [loading]="loadingArticles()"
              styleClass="p-datatable-sm"
            >
              <ng-template pTemplate="header">
                <tr>
                  <th>Titre</th>
                  @for (f of tableAttrCols(); track f.key) {
                    <th>{{ f.label }}</th>
                  }
                  <th>Publié</th>
                  <th style="width: 7rem"></th>
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-row>
                <tr>
                  <td>
                    <strong>{{ articleTitle(row) }}</strong>
                    <div class="code-sub"><code>{{ row.code }}</code></div>
                  </td>
                  @for (f of tableAttrCols(); track f.key) {
                    <td class="attr-cell">{{ attrCell(row, f) }}</td>
                  }
                  <td>
                    <p-tag
                      [value]="row.enabled ? 'Oui' : 'Non'"
                      [severity]="row.enabled ? 'success' : 'secondary'"
                    />
                  </td>
                  <td>
                    <button
                      pButton
                      type="button"
                      class="p-button-text p-button-sm"
                      icon="pi pi-pencil"
                      (click)="openEditArticle(row)"
                    ></button>
                    <button
                      pButton
                      type="button"
                      class="p-button-text p-button-danger p-button-sm"
                      icon="pi pi-trash"
                      (click)="removeArticle(row)"
                    ></button>
                  </td>
                </tr>
              </ng-template>
              <ng-template pTemplate="emptymessage">
                <tr>
                  <td [attr.colspan]="3 + tableAttrCols().length">
                    Aucun article. Créez-en un : le formulaire utilisera les
                    attributs de « {{ titleOf(rubrique) }} ».
                  </td>
                </tr>
              </ng-template>
            </p-table>
          } @else {
            <div class="empty-box wide">
              <h2>Créez d’abord une rubrique</h2>
              <p>
                Définissez ses attributs, puis ajoutez des articles sous cette
                rubrique.
              </p>
              <button
                pButton
                type="button"
                label="Nouvelle rubrique"
                (click)="openCreateRubrique()"
              ></button>
            </div>
          }
        </section>
      </div>
    </div>

    <!-- RUBRIQUE = identité + schéma d’attributs -->
    <p-dialog
      [(visible)]="rubriqueDialog"
      [header]="rubriqueEditId ? 'Modifier la rubrique' : 'Nouvelle rubrique'"
      [modal]="true"
      [style]="{ width: 'min(52rem, 96vw)' }"
      [draggable]="false"
      [maximizable]="true"
    >
      <div class="form-grid">
        <label class="full">
          Nom (fr) *
          <input
            pInputText
            [(ngModel)]="rubriqueForm.titleFr"
            placeholder="ex. Actualités, Pharmacies de garde…"
          />
        </label>
        <label>
          Code technique *
          <input
            pInputText
            [(ngModel)]="rubriqueForm.code"
            [disabled]="!!rubriqueEditId"
            placeholder="ex. actualites"
          />
        </label>
        <label>
          Priorité
          <p-inputNumber
            [(ngModel)]="rubriqueForm.priority"
            [showButtons]="true"
            [min]="0"
          />
        </label>
        <label class="full">
          Description
          <textarea
            pInputTextarea
            rows="2"
            [(ngModel)]="rubriqueForm.descriptionFr"
          ></textarea>
        </label>
        <label>
          Nom (en)
          <input pInputText [(ngModel)]="rubriqueForm.titleEn" />
        </label>
        <label class="check">
          <p-checkbox
            [(ngModel)]="rubriqueForm.enabled"
            [binary]="true"
            inputId="rub-en"
          />
          Publiée
        </label>
        <label class="full">
          Canaux
          <p-multiSelect
            [options]="channelOptions"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="rubriqueForm.channels"
            placeholder="Tous"
            display="chip"
            styleClass="w-full"
            [showClear]="true"
          />
        </label>
        <label class="full">
          Audiences
          <p-multiSelect
            [options]="audienceOptions"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="rubriqueForm.audiences"
            placeholder="Toutes"
            display="chip"
            styleClass="w-full"
            [showClear]="true"
          />
        </label>
      </div>

      <div class="schema-block">
        <div class="schema-block-head">
          <div>
            <h3>Attributs personnalisés</h3>
            <p>
              Chaque article de cette rubrique aura ces champs dans son
              formulaire.
            </p>
          </div>
          <button
            pButton
            type="button"
            class="p-button-sm"
            icon="pi pi-plus"
            label="Ajouter un attribut"
            (click)="addAttribute()"
          ></button>
        </div>

        <div class="attr-list">
          @for (a of rubriqueForm.attributes; track a; let i = $index) {
            <div class="attr-row">
              <input
                pInputText
                [(ngModel)]="a.key"
                placeholder="clé technique (ex. author)"
              />
              <input
                pInputText
                [(ngModel)]="a.label"
                placeholder="libellé affiché (ex. Auteur)"
              />
              <p-dropdown
                [options]="fieldTypeOptions"
                optionLabel="label"
                optionValue="value"
                [(ngModel)]="a.type"
                styleClass="w-full"
              />
              @if (a.type === 'enum') {
                <input
                  pInputText
                  [ngModel]="(a.values || []).join(', ')"
                  (ngModelChange)="a.values = splitCsv($event)"
                  placeholder="valeurs : ECO, SPORT…"
                />
              } @else {
                <span class="dash">—</span>
              }
              <button
                pButton
                type="button"
                class="p-button-text p-button-danger p-button-sm"
                icon="pi pi-times"
                (click)="removeAttribute(i)"
              ></button>
            </div>
          } @empty {
            <p class="muted pad">
              Aucun attribut pour l’instant. Ajoutez-en selon le besoin de
              cette rubrique (adresse, auteur, tags…).
            </p>
          }
        </div>
      </div>

      <ng-template pTemplate="footer">
        @if (rubriqueEditId) {
          <button
            pButton
            type="button"
            class="p-button-danger p-button-text"
            label="Supprimer"
            (click)="removeRubrique()"
          ></button>
        }
        <button
          pButton
          type="button"
          class="p-button-text"
          label="Annuler"
          (click)="rubriqueDialog = false"
        ></button>
        <button
          pButton
          type="button"
          label="Enregistrer"
          [loading]="saving()"
          (click)="saveRubrique()"
        ></button>
      </ng-template>
    </p-dialog>

    <!-- ARTICLE = titre + attributs de la rubrique sélectionnée -->
    <p-dialog
      [(visible)]="articleDialog"
      [header]="
        articleEditId
          ? 'Modifier l’article'
          : 'Nouvel article — ' + selectedTitle()
      "
      [modal]="true"
      [style]="{ width: 'min(44rem, 96vw)' }"
      [draggable]="false"
      [maximizable]="true"
    >
      <div class="form-grid">
        <label class="full">
          Titre *
          <input pInputText [(ngModel)]="articleForm.titleFr" />
        </label>
        <label>
          Code *
          <input
            pInputText
            [(ngModel)]="articleForm.code"
            [disabled]="!!articleEditId"
            placeholder="auto ou saisi"
          />
        </label>
        <label class="check">
          <p-checkbox
            [(ngModel)]="articleForm.enabled"
            [binary]="true"
            inputId="art-en"
          />
          Publié
        </label>
      </div>

      @if (schemaFields().length) {
        <div class="schema-block">
          <div class="schema-block-head">
            <div>
              <h3>Attributs — {{ selectedTitle() }}</h3>
              <p>Champs propres à cette rubrique.</p>
            </div>
          </div>
          <div class="form-grid">
            @for (f of schemaFields(); track f.key) {
              <label [class.full]="f.type === 'text' || f.type === 'string[]'">
                {{ f.label }}
                @switch (f.type) {
                  @case ('boolean') {
                    <span class="check inline">
                      <p-checkbox
                        [(ngModel)]="articleForm.attrs[f.key]"
                        [binary]="true"
                        [inputId]="'af-' + f.key"
                      />
                    </span>
                  }
                  @case ('number') {
                    <p-inputNumber
                      [(ngModel)]="articleForm.attrs[f.key]"
                      [maxFractionDigits]="6"
                      styleClass="w-full"
                    />
                  }
                  @case ('enum') {
                    <p-dropdown
                      [options]="enumOptions(f)"
                      optionLabel="label"
                      optionValue="value"
                      [(ngModel)]="articleForm.attrs[f.key]"
                      styleClass="w-full"
                      [showClear]="true"
                    />
                  }
                  @case ('text') {
                    <textarea
                      pInputTextarea
                      rows="3"
                      [(ngModel)]="articleForm.attrs[f.key]"
                    ></textarea>
                  }
                  @case ('string[]') {
                    <input
                      pInputText
                      [ngModel]="arrayToCsv(articleForm.attrs[f.key])"
                      (ngModelChange)="
                        articleForm.attrs[f.key] = splitCsv($event)
                      "
                      placeholder="valeur1, valeur2"
                    />
                  }
                  @default {
                    <input
                      pInputText
                      [(ngModel)]="articleForm.attrs[f.key]"
                    />
                  }
                }
              </label>
            }
          </div>
        </div>
      }

      <details class="extra-details">
        <summary>Texte libre optionnel (résumé / corps)</summary>
        <div class="form-grid" style="margin-top: 0.75rem">
          <label class="full">
            Résumé (fr)
            <input pInputText [(ngModel)]="articleForm.summaryFr" />
          </label>
          <label class="full">
            Corps (fr)
            <textarea
              pInputTextarea
              rows="4"
              [(ngModel)]="articleForm.bodyFr"
            ></textarea>
          </label>
          <label class="full">
            Titre (en)
            <input pInputText [(ngModel)]="articleForm.titleEn" />
          </label>
          <label class="full">
            Corps (en)
            <textarea
              pInputTextarea
              rows="3"
              [(ngModel)]="articleForm.bodyEn"
            ></textarea>
          </label>
          <label>
            Priorité
            <p-inputNumber
              [(ngModel)]="articleForm.priority"
              [showButtons]="true"
              [min]="0"
            />
          </label>
        </div>
      </details>

      <ng-template pTemplate="footer">
        <button
          pButton
          type="button"
          class="p-button-text"
          label="Annuler"
          (click)="articleDialog = false"
        ></button>
        <button
          pButton
          type="button"
          label="Enregistrer l’article"
          [loading]="saving()"
          (click)="saveArticle()"
        ></button>
      </ng-template>
    </p-dialog>
  `,
  styles: `
    .gw-page-header {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      flex-wrap: wrap;
    }
    .subtitle {
      margin: 0.35rem 0 0;
      color: #64748b;
      font-size: 0.9rem;
      line-height: 1.45;
      max-width: 36rem;
    }
    .actions,
    .head-actions {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
      align-items: center;
    }
    .layout {
      display: grid;
      grid-template-columns: 16rem 1fr;
      gap: 1rem;
      margin-top: 1rem;
    }
    .panel {
      background: #fff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 0.85rem;
      min-height: 18rem;
    }
    .panel.main {
      padding: 1rem 1.1rem 1.25rem;
    }
    .panel-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 0.75rem;
      margin-bottom: 0.75rem;
    }
    .label {
      font-size: 0.72rem;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: #64748b;
    }
    .eyebrow {
      font-size: 0.72rem;
      font-weight: 800;
      text-transform: uppercase;
      color: #0369a1;
      margin-bottom: 0.15rem;
    }
    .articles-head h2 {
      margin: 0;
      font-size: 1.2rem;
    }
    .rubrique {
      display: flex;
      flex-direction: column;
      gap: 0.15rem;
      width: 100%;
      text-align: left;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 0.7rem 0.8rem;
      margin-bottom: 0.4rem;
      background: #fff;
      cursor: pointer;
      font-family: inherit;
    }
    .rubrique.on {
      border-color: #0284c7;
      background: #f0f9ff;
    }
    .rubrique-title {
      font-weight: 700;
      font-size: 0.92rem;
      color: #0f172a;
    }
    .rubrique-meta {
      font-size: 0.72rem;
      color: #64748b;
    }
    .schema-banner {
      display: flex;
      flex-wrap: wrap;
      gap: 0.35rem;
      padding: 0.55rem 0.7rem;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      margin-bottom: 0.85rem;
      font-size: 0.8rem;
      color: #334155;
      align-items: center;
    }
    .schema-banner.empty-schema {
      color: #64748b;
    }
    .chip {
      padding: 0.1rem 0.45rem;
      border-radius: 6px;
      background: #e0f2fe;
      color: #075985;
      font-size: 0.72rem;
      font-weight: 700;
    }
    .code-sub {
      margin-top: 0.15rem;
      font-size: 0.72rem;
      color: #94a3b8;
    }
    .attr-cell {
      font-size: 0.8rem;
      color: #475569;
      max-width: 10rem;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .empty-box {
      display: grid;
      gap: 0.65rem;
      place-items: start;
      padding: 1rem 0.25rem;
      color: #64748b;
    }
    .empty-box.wide {
      place-items: center;
      text-align: center;
      padding: 3rem 1rem;
    }
    .empty-box h2 {
      margin: 0;
      color: #0f172a;
    }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem 1rem;
    }
    .form-grid label {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      font-size: 0.88rem;
      color: #334155;
    }
    .form-grid .full {
      grid-column: 1 / -1;
    }
    .form-grid .check {
      flex-direction: row;
      align-items: center;
      gap: 0.5rem;
      padding-top: 1.2rem;
    }
    .form-grid .check.inline {
      padding-top: 0.35rem;
    }
    .schema-block {
      margin-top: 1.15rem;
      padding: 0.85rem;
      border: 1px solid #bae6fd;
      border-radius: 10px;
      background: #f0f9ff;
    }
    .schema-block-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 0.75rem;
      margin-bottom: 0.75rem;
    }
    .schema-block-head h3 {
      margin: 0;
      font-size: 0.95rem;
      color: #0c4a6e;
    }
    .schema-block-head p {
      margin: 0.2rem 0 0;
      font-size: 0.8rem;
      color: #0369a1;
    }
    .attr-list {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
    }
    .attr-row {
      display: grid;
      grid-template-columns: 1fr 1fr 9rem 1fr auto;
      gap: 0.4rem;
      align-items: center;
    }
    .dash,
    .muted {
      color: #94a3b8;
      font-size: 0.82rem;
    }
    .muted.pad {
      padding: 0.5rem 0;
    }
    .extra-details {
      margin-top: 1rem;
      border-top: 1px solid #e2e8f0;
      padding-top: 0.75rem;
    }
    .extra-details summary {
      cursor: pointer;
      color: #64748b;
      font-size: 0.85rem;
      font-weight: 600;
    }
    .gw-msg {
      margin: 0.5rem 0;
    }
    .gw-msg.error {
      color: #b91c1c;
    }
    @media (max-width: 900px) {
      .layout {
        grid-template-columns: 1fr;
      }
      .attr-row {
        grid-template-columns: 1fr;
      }
    }
  `,
})
export class ContentCmsAdminComponent implements OnInit {
  private readonly api = inject(CmsAdminService);

  readonly fieldTypeOptions = FIELD_TYPE_OPTIONS;
  readonly channelOptions = ACCESS_CHANNELS.map((c) => ({
    label: `${c.label} (${c.code})`,
    value: c.code,
  }));
  readonly audienceOptions = PRODUCT_AUDIENCES.map((a) => ({
    label: `${a.label} (${a.code})`,
    value: a.code,
  }));

  readonly rubriques = signal<CmsCollectionDto[]>([]);
  readonly articles = signal<CmsDocumentDto[]>([]);
  readonly selected = signal<CmsCollectionDto | null>(null);
  readonly loading = signal(false);
  readonly loadingArticles = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);

  readonly schemaFields = computed(() => schemaFieldsOf(this.selected()));
  readonly tableAttrCols = computed(() => this.schemaFields().slice(0, 4));
  readonly selectedTitle = computed(() => {
    const r = this.selected();
    return r ? this.titleOf(r) : '';
  });

  rubriqueDialog = false;
  rubriqueEditId: string | null = null;
  rubriqueForm: RubriqueForm = this.emptyRubriqueForm();

  articleDialog = false;
  articleEditId: string | null = null;
  articleForm: ArticleForm = this.emptyArticleForm();

  private pendingSelectCode: string | null = null;

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.listCollections().subscribe({
      next: (rows) => {
        this.rubriques.set(rows ?? []);
        this.loading.set(false);
        const want = this.pendingSelectCode;
        this.pendingSelectCode = null;
        const current = this.selected();
        const next =
          (want && rows.find((c) => c.code === want)) ||
          (current && rows.find((c) => c.code === current.code)) ||
          rows[0] ||
          null;
        if (next) this.selectRubrique(next);
        else {
          this.selected.set(null);
          this.articles.set([]);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || err?.message || 'Chargement impossible');
      },
    });
  }

  selectRubrique(r: CmsCollectionDto): void {
    this.selected.set(r);
    this.loadingArticles.set(true);
    this.api.listDocuments(r.code).subscribe({
      next: (docs) => {
        this.articles.set(docs ?? []);
        this.loadingArticles.set(false);
      },
      error: (err) => {
        this.loadingArticles.set(false);
        this.articles.set([]);
        this.error.set(err?.error?.message || err?.message || 'Articles introuvables');
      },
    });
  }

  titleOf(r: CmsCollectionDto): string {
    return r.locales?.find((l) => l.locale === 'fr')?.title || r.locales?.[0]?.title || r.code;
  }

  attrCount(r: CmsCollectionDto): number {
    return schemaFieldsOf(r).length;
  }

  articleTitle(row: CmsDocumentDto): string {
    return row.locales?.find((l) => l.locale === 'fr')?.title || row.locales?.[0]?.title || row.code;
  }

  attrCell(row: CmsDocumentDto, f: CmsSchemaField): string {
    const v = row.attrs?.[f.key];
    if (v == null || v === '') return '—';
    if (Array.isArray(v)) return v.join(', ');
    if (typeof v === 'boolean') return v ? 'Oui' : 'Non';
    return String(v);
  }

  openCreateRubrique(): void {
    this.rubriqueEditId = null;
    this.rubriqueForm = this.emptyRubriqueForm();
    this.rubriqueDialog = true;
  }

  openEditRubrique(r: CmsCollectionDto): void {
    this.rubriqueEditId = r.id;
    this.rubriqueForm = {
      code: r.code,
      kind: r.kind || 'CUSTOM',
      enabled: !!r.enabled,
      priority: r.priority ?? 0,
      icon: r.icon || '',
      titleFr: r.locales?.find((l) => l.locale === 'fr')?.title || '',
      titleEn: r.locales?.find((l) => l.locale === 'en')?.title || '',
      descriptionFr: r.locales?.find((l) => l.locale === 'fr')?.description || '',
      channels: [...(r.channels || [])],
      audiences: [...(r.audiences || [])],
      attributes: schemaFieldsOf(r).map((f) => ({
        key: f.key,
        type: f.type,
        label: f.label,
        values: f.values ? [...f.values] : undefined,
      })),
    };
    this.rubriqueDialog = true;
  }

  addAttribute(): void {
    this.rubriqueForm.attributes = [
      ...this.rubriqueForm.attributes,
      { key: '', type: 'string', label: '' },
    ];
  }

  removeAttribute(index: number): void {
    this.rubriqueForm.attributes = this.rubriqueForm.attributes.filter((_, i) => i !== index);
  }

  saveRubrique(): void {
    const code = this.rubriqueForm.code.trim();
    const titleFr = this.rubriqueForm.titleFr.trim();
    if (!code || !titleFr) {
      this.error.set('Code et nom de rubrique requis');
      return;
    }
    const fields = this.rubriqueForm.attributes
      .map((f) => ({
        key: f.key.trim(),
        type: f.type,
        label: (f.label || f.key).trim(),
        values: f.type === 'enum' ? (f.values || []).filter(Boolean) : undefined,
      }))
      .filter((f) => !!f.key);

    const body: CmsCollectionUpsert = {
      code,
      kind: (this.rubriqueForm.kind || 'CUSTOM').toUpperCase(),
      enabled: !!this.rubriqueForm.enabled,
      priority: Number(this.rubriqueForm.priority) || 0,
      icon: this.rubriqueForm.icon.trim() || null,
      channels: [...(this.rubriqueForm.channels || [])],
      audiences: [...(this.rubriqueForm.audiences || [])],
      schema: { fields },
      locales: [
        {
          locale: 'fr',
          title: titleFr,
          description: this.rubriqueForm.descriptionFr.trim() || null,
        },
        {
          locale: 'en',
          title: this.rubriqueForm.titleEn.trim() || titleFr,
          description: null,
        },
      ],
    };

    this.saving.set(true);
    const req$ = this.rubriqueEditId
      ? this.api.updateCollection(this.rubriqueEditId, body)
      : this.api.createCollection(body);
    req$.subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.rubriqueDialog = false;
        this.msg.set(
          `Rubrique « ${saved.code} » enregistrée` +
            (fields.length ? ` · ${fields.length} attribut(s)` : '')
        );
        this.pendingSelectCode = saved.code;
        this.reload();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(err?.error?.message || err?.message || 'Enregistrement rubrique impossible');
      },
    });
  }

  removeRubrique(): void {
    if (!this.rubriqueEditId) return;
    if (!confirm('Supprimer cette rubrique et tous ses articles ?')) return;
    this.api.deleteCollection(this.rubriqueEditId).subscribe({
      next: () => {
        this.rubriqueDialog = false;
        this.msg.set('Rubrique supprimée');
        this.selected.set(null);
        this.reload();
      },
      error: (err) => {
        this.error.set(err?.error?.message || err?.message || 'Suppression refusée');
      },
    });
  }

  openCreateArticle(): void {
    const rubrique = this.selected();
    if (!rubrique) return;
    this.articleEditId = null;
    this.articleForm = this.emptyArticleForm(this.schemaFields());
    if (!this.articleForm.code) {
      this.articleForm.code = `${rubrique.code}-${Date.now().toString(36)}`;
    }
    this.articleDialog = true;
  }

  openEditArticle(row: CmsDocumentDto): void {
    const fields = this.schemaFields();
    const attrs: Record<string, unknown> = {};
    for (const f of fields) {
      attrs[f.key] = row.attrs?.[f.key] ?? defaultAttrValue(f);
    }
    this.articleEditId = row.id;
    this.articleForm = {
      code: row.code,
      enabled: !!row.enabled,
      priority: row.priority ?? 0,
      titleFr: row.locales?.find((l) => l.locale === 'fr')?.title || '',
      titleEn: row.locales?.find((l) => l.locale === 'en')?.title || '',
      summaryFr: row.locales?.find((l) => l.locale === 'fr')?.summary || '',
      bodyFr: row.locales?.find((l) => l.locale === 'fr')?.body || '',
      bodyEn: row.locales?.find((l) => l.locale === 'en')?.body || '',
      attrs,
    };
    this.articleDialog = true;
  }

  enumOptions(f: CmsSchemaField): { label: string; value: string }[] {
    return (f.values || []).map((v) => ({ label: v, value: v }));
  }

  splitCsv(raw: string): string[] {
    return (raw || '')
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
  }

  arrayToCsv(value: unknown): string {
    return Array.isArray(value) ? value.map(String).join(', ') : '';
  }

  saveArticle(): void {
    const rubrique = this.selected();
    if (!rubrique) return;
    const titleFr = this.articleForm.titleFr.trim();
    if (!this.articleForm.code.trim() || !titleFr) {
      this.error.set('Code et titre requis');
      return;
    }

    const fields = this.schemaFields();
    const attrs: Record<string, unknown> = {};
    for (const f of fields) {
      let v = this.articleForm.attrs[f.key];
      if (f.type === 'number' && (v === '' || v == null)) v = null;
      if (f.type === 'string[]' && !Array.isArray(v)) v = this.splitCsv(String(v ?? ''));
      if (f.type === 'boolean') v = !!v;
      attrs[f.key] = v;
    }

    const body: CmsDocumentUpsert = {
      code: this.articleForm.code.trim(),
      collectionCode: rubrique.code,
      enabled: !!this.articleForm.enabled,
      priority: Number(this.articleForm.priority) || 0,
      icon: rubrique.icon || null,
      attrs,
      channels: [],
      audiences: [],
      locales: [
        {
          locale: 'fr',
          title: titleFr,
          summary: this.articleForm.summaryFr.trim() || null,
          body: this.articleForm.bodyFr.trim() || null,
        },
        {
          locale: 'en',
          title: this.articleForm.titleEn.trim() || titleFr,
          summary: null,
          body: this.articleForm.bodyEn.trim() || null,
        },
      ],
    };

    this.saving.set(true);
    const req$ = this.articleEditId
      ? this.api.updateDocument(this.articleEditId, body)
      : this.api.createDocument(body);
    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.articleDialog = false;
        this.msg.set(`Article enregistré dans « ${rubrique.code} »`);
        this.selectRubrique(rubrique);
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(err?.error?.message || err?.message || 'Enregistrement article impossible');
      },
    });
  }

  removeArticle(row: CmsDocumentDto): void {
    if (!confirm(`Supprimer l’article « ${row.code} » ?`)) return;
    const rubrique = this.selected();
    this.api.deleteDocument(row.id).subscribe({
      next: () => {
        this.msg.set(`Article « ${row.code} » supprimé`);
        if (rubrique) this.selectRubrique(rubrique);
      },
      error: (err) => {
        this.error.set(err?.error?.message || err?.message || 'Suppression refusée');
      },
    });
  }

  private emptyRubriqueForm(): RubriqueForm {
    return {
      code: '',
      kind: 'CUSTOM',
      enabled: true,
      priority: 10,
      icon: '',
      titleFr: '',
      titleEn: '',
      descriptionFr: '',
      channels: ['MOBILE', 'WEB'],
      audiences: ['RETAIL'],
      attributes: [],
    };
  }

  private emptyArticleForm(fields: CmsSchemaField[] = []): ArticleForm {
    const attrs: Record<string, unknown> = {};
    for (const f of fields) attrs[f.key] = defaultAttrValue(f);
    return {
      code: '',
      enabled: true,
      priority: 10,
      titleFr: '',
      titleEn: '',
      summaryFr: '',
      bodyFr: '',
      bodyEn: '',
      attrs,
    };
  }
}

function defaultAttrValue(f: CmsSchemaField): unknown {
  switch (f.type) {
    case 'boolean':
      return false;
    case 'number':
      return null;
    case 'string[]':
      return [];
    case 'enum':
      return f.values?.[0] || '';
    default:
      return '';
  }
}
