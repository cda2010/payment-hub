import { DatePipe } from '@angular/common';
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
  ContentAdminService,
  ContentItemDto,
  ContentLocaleDto,
  ContentUpsertRequest,
} from '../../core/service/content-admin.service';
import {
  ACCESS_CHANNELS,
  AccessChannel,
  PRODUCT_AUDIENCES,
  ProductAudience,
} from '../../core/service/gateway.models';
import { I18nApiService, SupportedLanguage } from '../../core/service/i18n-api.service';

const HOME_PROMO_SLOT = 'home.promo';

const TONE_OPTIONS = [
  { label: 'Bleu', value: 'blue' },
  { label: 'Mint', value: 'mint' },
  { label: 'Navy', value: 'navy' },
  { label: 'Sand', value: 'sand' },
];

type LocaleDraft = {
  locale: string;
  eyebrow: string;
  title: string;
  body: string;
  cta: string;
};

type PromoForm = {
  code: string;
  slot: string;
  enabled: boolean;
  priority: number;
  pluginId: string;
  route: string;
  icon: string;
  tone: string;
  imageUrl: string;
  imageAlt: string;
  startsAt: string;
  endsAt: string;
  channels: AccessChannel[];
  audiences: ProductAudience[];
  locales: LocaleDraft[];
};

@Component({
  selector: 'app-content-promos-admin',
  standalone: true,
  imports: [
    DatePipe,
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
          <h1>Publicité home</h1>
          <p class="subtitle">
            Bannières CMS (slot <code>{{ slot }}</code>) — visibilité par canal × audience
          </p>
        </div>
        <div class="actions">
          <button
            pButton
            type="button"
            label="Actualiser"
            icon="pi pi-refresh"
            class="p-button-outlined"
            [disabled]="loading()"
            (click)="reload()"
          ></button>
          <button
            pButton
            type="button"
            label="Nouvelle promo"
            icon="pi pi-plus"
            (click)="openCreate()"
          ></button>
        </div>
      </div>

      @if (error()) {
        <p class="gw-msg error">{{ error() }}</p>
      }
      @if (msg()) {
        <p class="gw-msg">{{ msg() }}</p>
      }

      <div class="gw-panel">
        <p-table
          [value]="rows()"
          [loading]="loading()"
          styleClass="p-datatable-sm"
          sortField="priority"
          [sortOrder]="-1"
        >
          <ng-template pTemplate="header">
            <tr>
              <th pSortableColumn="priority">Priorité <p-sortIcon field="priority" /></th>
              <th>Code</th>
              <th>Titre</th>
              <th>Route / plugin</th>
              <th>Canaux</th>
              <th>Audiences</th>
              <th>Actif</th>
              <th></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-row>
            <tr>
              <td>{{ row.priority }}</td>
              <td><code>{{ row.code }}</code></td>
              <td>
                <div class="title-cell">
                  @if (row.imageUrl) {
                    <img [src]="row.imageUrl" [alt]="row.imageAlt || row.code" class="thumb" />
                  }
                  <div>
                    <strong>{{ displayTitle(row) }}</strong>
                    @if (displayEyebrow(row); as eye) {
                      <div class="muted">{{ eye }}</div>
                    }
                  </div>
                </div>
              </td>
              <td>
                <div><code>{{ row.route || '—' }}</code></div>
                <div class="muted">{{ row.pluginId || 'tous plugins' }}</div>
              </td>
              <td>
                <div class="chip-row">
                  @for (ch of displayList(row.channels); track ch) {
                    <span class="mini-chip">{{ ch }}</span>
                  } @empty {
                    <span class="muted">Tous</span>
                  }
                </div>
              </td>
              <td>
                <div class="chip-row">
                  @for (aud of displayList(row.audiences); track aud) {
                    <span class="mini-chip aud">{{ aud }}</span>
                  } @empty {
                    <span class="muted">Toutes</span>
                  }
                </div>
              </td>
              <td>
                <p-tag
                  [value]="row.enabled ? 'Oui' : 'Non'"
                  [severity]="row.enabled ? 'success' : 'secondary'"
                />
              </td>
              <td class="row-actions">
                <button
                  pButton
                  type="button"
                  class="p-button-text p-button-sm"
                  label="Éditer"
                  (click)="openEdit(row)"
                ></button>
                <button
                  pButton
                  type="button"
                  class="p-button-text p-button-danger p-button-sm"
                  icon="pi pi-trash"
                  pTooltip="Supprimer"
                  (click)="remove(row)"
                ></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr>
              <td colspan="8">Aucune publicité — créez-en une ou démarrez payment-content (:8098).</td>
            </tr>
          </ng-template>
        </p-table>
      </div>
    </div>

    <p-dialog
      [(visible)]="dialogVisible"
      [header]="editId ? 'Éditer la promo' : 'Nouvelle promo'"
      [modal]="true"
      [style]="{ width: 'min(56rem, 96vw)' }"
      [draggable]="false"
      [maximizable]="true"
    >
      <div class="form-grid">
        <label>
          Code
          <input pInputText [(ngModel)]="form.code" [disabled]="!!editId" placeholder="promo-loyalty" />
        </label>
        <label>
          Priorité
          <p-inputNumber [(ngModel)]="form.priority" [showButtons]="true" [min]="0" [max]="999" />
        </label>
        <label>
          Route mobile
          <input pInputText [(ngModel)]="form.route" placeholder="/fidelite" />
        </label>
        <label>
          Plugin cible
          <input pInputText [(ngModel)]="form.pluginId" placeholder="fidelite (optionnel)" />
        </label>
        <label>
          Icône Ionicons
          <input pInputText [(ngModel)]="form.icon" placeholder="ribbon-outline" />
        </label>
        <label>
          Ton
          <p-dropdown
            [options]="toneOptions"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="form.tone"
            styleClass="w-full"
          />
        </label>
        <label class="check">
          <p-checkbox [(ngModel)]="form.enabled" [binary]="true" inputId="promo-enabled" />
          Publiée
        </label>
        <label class="full">
          Canaux d’accès
          <p-multiSelect
            [options]="channelOptions"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="form.channels"
            placeholder="Tous les canaux (vide)"
            styleClass="w-full"
            display="chip"
            [showClear]="true"
          />
          <small class="hint">Vide = visible sur tous les canaux (MOBILE, WEB…)</small>
        </label>
        <label class="full">
          Audiences
          <p-multiSelect
            [options]="audienceOptions"
            optionLabel="label"
            optionValue="value"
            [(ngModel)]="form.audiences"
            placeholder="Toutes les audiences (vide)"
            styleClass="w-full"
            display="chip"
            [showClear]="true"
          />
          <small class="hint">Vide = toutes (RETAIL, CORPORATE, AGENT…)</small>
        </label>
        <label>
          Alt image
          <input pInputText [(ngModel)]="form.imageAlt" placeholder="Description image" />
        </label>
        <label class="full">
          Image
          <div class="image-row">
            <input pInputText class="flex-1" [(ngModel)]="form.imageUrl" placeholder="URL absolue ou upload" />
            <input #fileInput type="file" accept="image/png,image/jpeg,image/webp,image/gif" hidden
                   (change)="onFileSelected($event)" />
            <button
              pButton
              type="button"
              class="p-button-outlined"
              icon="pi pi-upload"
              label="Upload"
              [loading]="uploading()"
              [disabled]="uploading()"
              (click)="fileInput.click()"
            ></button>
          </div>
          @if (form.imageUrl) {
            <img [src]="form.imageUrl" [alt]="form.imageAlt || form.code" class="preview" />
          }
        </label>
        <label>
          Début (ISO optionnel)
          <input pInputText [(ngModel)]="form.startsAt" placeholder="2026-10-18T00:00:00Z" />
        </label>
        <label>
          Fin (ISO optionnel)
          <input pInputText [(ngModel)]="form.endsAt" placeholder="2026-12-31T23:59:59Z" />
        </label>
      </div>

      <h3 class="section-title">Textes par langue</h3>
      <div class="i18n-matrix" [style.--i18n-cols]="langCodes().length">
        <div class="i18n-matrix-head">
          <span class="i18n-row-label">Champ</span>
          @for (lang of langCodes(); track lang) {
            <span class="i18n-col-head" [class.default]="lang === defaultLanguage()">
              {{ lang }}
              @if (lang === defaultLanguage()) {
                <em>défaut</em>
              }
            </span>
          }
        </div>
        @for (field of localeFields; track field.key) {
          <div class="i18n-matrix-row">
            <span class="i18n-row-label">
              <strong>{{ field.label }}</strong>
              @if (field.required) {
                <small>requis</small>
              }
            </span>
            @for (lang of langCodes(); track lang) {
              <div class="i18n-col-cell">
                @if (field.multiline) {
                  <textarea
                    pInputTextarea
                    rows="3"
                    class="w-full"
                    [ngModel]="localeText(lang, field.key)"
                    (ngModelChange)="setLocaleText(lang, field.key, $event)"
                  ></textarea>
                } @else {
                  <input
                    pInputText
                    class="w-full"
                    [ngModel]="localeText(lang, field.key)"
                    (ngModelChange)="setLocaleText(lang, field.key, $event)"
                  />
                }
              </div>
            }
          </div>
        }
      </div>

      <ng-template pTemplate="footer">
        <button pButton type="button" class="p-button-text" label="Annuler" (click)="dialogVisible = false"></button>
        <button
          pButton
          type="button"
          label="Enregistrer"
          [loading]="saving()"
          [disabled]="saving()"
          (click)="save()"
        ></button>
      </ng-template>
    </p-dialog>
  `,
  styles: `
    .gw-page-header {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: flex-start;
    }
    .subtitle { margin: 0.25rem 0 0; color: #64748b; font-size: 0.9rem; }
    .actions { display: flex; gap: 0.5rem; flex-wrap: wrap; }
    .title-cell { display: flex; gap: 0.65rem; align-items: center; }
    .thumb {
      width: 40px; height: 40px; border-radius: 8px; object-fit: cover;
      background: #f1f5f9; flex-shrink: 0;
    }
    .muted { color: #94a3b8; font-size: 0.78rem; }
    .row-actions { white-space: nowrap; }
    .tone {
      display: inline-block; padding: 0.15rem 0.5rem; border-radius: 999px;
      font-size: 0.75rem; font-weight: 700; background: #eff6ff; color: #1d4ed8;
    }
    .tone[data-tone='mint'] { background: #ecfdf5; color: #047857; }
    .tone[data-tone='navy'] { background: #e2e8f0; color: #0f172a; }
    .tone[data-tone='sand'] { background: #fff7ed; color: #c2410c; }
    .form-grid {
      display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem 1rem;
    }
    .form-grid label {
      display: flex; flex-direction: column; gap: 0.35rem; font-size: 0.88rem; color: #334155;
    }
    .form-grid .full { grid-column: 1 / -1; }
    .form-grid .check {
      flex-direction: row; align-items: center; gap: 0.5rem; padding-top: 1.4rem;
    }
    .form-grid .hint { color: #94a3b8; font-size: 0.72rem; font-weight: 600; }
    .chip-row { display: flex; flex-wrap: wrap; gap: 0.25rem; }
    .mini-chip {
      display: inline-block; padding: 0.12rem 0.4rem; border-radius: 999px;
      font-size: 0.65rem; font-weight: 800; background: #eff6ff; color: #1d4ed8;
    }
    .mini-chip.aud { background: #ecfdf5; color: #047857; }
    .image-row { display: flex; gap: 0.5rem; align-items: center; }
    .image-row .flex-1 { flex: 1; min-width: 0; }
    .preview {
      margin-top: 0.5rem; max-height: 120px; border-radius: 10px; object-fit: cover;
      border: 1px solid #e2e8f0;
    }
    .section-title {
      margin: 1.25rem 0 0.65rem; font-size: 0.95rem; color: #0f172a;
    }
    .i18n-matrix {
      display: flex; flex-direction: column; gap: 0.35rem; overflow-x: auto;
    }
    .i18n-matrix-head, .i18n-matrix-row {
      display: grid;
      grid-template-columns: minmax(6.5rem, 8.5rem) repeat(var(--i18n-cols, 2), minmax(10rem, 1fr));
      gap: 0.5rem 0.65rem; align-items: start;
    }
    .i18n-matrix-head { padding-bottom: 0.35rem; border-bottom: 1px solid #e2e8f0; }
    .i18n-col-head {
      font-size: 0.75rem; font-weight: 800; text-transform: uppercase;
      letter-spacing: 0.04em; color: #475569;
      display: inline-flex; align-items: center; gap: 0.35rem;
    }
    .i18n-col-head.default { color: #1d4ed8; }
    .i18n-col-head em {
      font-style: normal; font-size: 0.65rem; font-weight: 700; text-transform: none;
      letter-spacing: 0; padding: 0.1rem 0.35rem; border-radius: 999px;
      background: #dbeafe; color: #1e40af;
    }
    .i18n-row-label {
      display: flex; flex-direction: column; gap: 0.1rem; padding-top: 0.4rem;
      font-size: 0.82rem; color: #0f172a;
    }
    .i18n-row-label small { color: #94a3b8; font-size: 0.68rem; }
    .i18n-col-cell { min-width: 0; }
    .gw-msg { margin: 0.5rem 0; }
    .gw-msg.error { color: #b91c1c; }
    @media (max-width: 800px) {
      .form-grid { grid-template-columns: 1fr; }
      .form-grid .check { padding-top: 0; }
    }
  `,
})
export class ContentPromosAdminComponent implements OnInit {
  private readonly api = inject(ContentAdminService);
  private readonly i18nApi = inject(I18nApiService);

  readonly slot = HOME_PROMO_SLOT;
  readonly toneOptions = TONE_OPTIONS;
  readonly channelOptions = ACCESS_CHANNELS.map((c) => ({
    label: `${c.label} (${c.code})`,
    value: c.code,
  }));
  readonly audienceOptions = PRODUCT_AUDIENCES.map((a) => ({
    label: `${a.label} (${a.code})`,
    value: a.code,
  }));
  readonly localeFields: Array<{
    key: keyof Omit<LocaleDraft, 'locale'>;
    label: string;
    required?: boolean;
    multiline?: boolean;
  }> = [
    { key: 'eyebrow', label: 'Pastille' },
    { key: 'title', label: 'Titre', required: true },
    { key: 'body', label: 'Texte', multiline: true },
    { key: 'cta', label: 'CTA' },
  ];

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly uploading = signal(false);
  readonly error = signal<string | null>(null);
  readonly msg = signal<string | null>(null);
  readonly rows = signal<ContentItemDto[]>([]);
  readonly languages = signal<SupportedLanguage[]>([]);
  readonly defaultLanguage = signal('fr');
  readonly langCodes = computed(() => {
    const def = this.defaultLanguage();
    const codes = this.languages().map((l) => l.code).filter(Boolean);
    if (!codes.length) return [def];
    const rest = codes.filter((c) => c !== def);
    return codes.includes(def) ? [def, ...rest] : codes;
  });

  dialogVisible = false;
  editId: string | null = null;
  form: PromoForm = this.emptyForm();

  ngOnInit(): void {
    this.loadLanguages();
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.list(this.slot).subscribe({
      next: (items) => {
        this.rows.set(items ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.rows.set([]);
        this.error.set(
          err?.error?.message ||
            err?.message ||
            'Impossible de charger les publicités (payment-content :8098 ?)'
        );
      },
    });
  }

  openCreate(): void {
    this.editId = null;
    this.form = this.emptyForm();
    this.ensureLocales();
    this.dialogVisible = true;
    this.msg.set(null);
    this.error.set(null);
  }

  openEdit(row: ContentItemDto): void {
    this.editId = row.id;
    this.form = {
      code: row.code,
      slot: row.slot || this.slot,
      enabled: !!row.enabled,
      priority: row.priority ?? 0,
      pluginId: row.pluginId || '',
      route: row.route || '',
      icon: row.icon || '',
      tone: row.tone || 'blue',
      imageUrl: row.imageUrl || '',
      imageAlt: row.imageAlt || '',
      startsAt: row.startsAt || '',
      endsAt: row.endsAt || '',
      channels: [...(row.channels ?? [])] as AccessChannel[],
      audiences: [...(row.audiences ?? [])] as ProductAudience[],
      locales: (row.locales ?? []).map((l) => ({
        locale: l.locale,
        eyebrow: l.eyebrow || '',
        title: l.title || '',
        body: l.body || '',
        cta: l.cta || '',
      })),
    };
    this.ensureLocales();
    this.dialogVisible = true;
    this.msg.set(null);
    this.error.set(null);
  }

  localeText(lang: string, field: keyof Omit<LocaleDraft, 'locale'>): string {
    return this.form.locales.find((l) => l.locale === lang)?.[field] || '';
  }

  setLocaleText(lang: string, field: keyof Omit<LocaleDraft, 'locale'>, value: string): void {
    const idx = this.form.locales.findIndex((l) => l.locale === lang);
    if (idx < 0) {
      this.form.locales = [
        ...this.form.locales,
        { locale: lang, eyebrow: '', title: '', body: '', cta: '', [field]: value },
      ];
      return;
    }
    this.form.locales = this.form.locales.map((l, i) =>
      i === idx ? { ...l, [field]: value } : l
    );
  }

  onFileSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;
    this.uploading.set(true);
    this.error.set(null);
    this.api.uploadMedia(file).subscribe({
      next: (res) => {
        this.uploading.set(false);
        this.form.imageUrl = res.url;
        if (!this.form.imageAlt) {
          this.form.imageAlt = file.name;
        }
      },
      error: (err) => {
        this.uploading.set(false);
        this.error.set(err?.error?.message || err?.message || 'Échec upload image');
      },
    });
  }

  save(): void {
    const body = this.toRequest();
    if (!body) return;
    this.saving.set(true);
    this.error.set(null);
    const req$ = this.editId
      ? this.api.update(this.editId, body)
      : this.api.create(body);
    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogVisible = false;
        this.msg.set(this.editId ? 'Publicité mise à jour' : 'Publicité créée');
        this.reload();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(err?.error?.message || err?.message || 'Enregistrement impossible');
      },
    });
  }

  remove(row: ContentItemDto): void {
    if (!confirm(`Supprimer la publicité « ${row.code} » ?`)) return;
    this.api.delete(row.id).subscribe({
      next: () => {
        this.msg.set(`« ${row.code} » supprimée`);
        this.reload();
      },
      error: (err) => {
        this.error.set(err?.error?.message || err?.message || 'Suppression refusée');
      },
    });
  }

  displayTitle(row: ContentItemDto): string {
    const def = this.defaultLanguage();
    const loc =
      row.locales?.find((l) => l.locale === def) ||
      row.locales?.find((l) => l.locale?.startsWith('fr')) ||
      row.locales?.[0];
    return loc?.title || row.code;
  }

  displayEyebrow(row: ContentItemDto): string | null {
    const def = this.defaultLanguage();
    const loc =
      row.locales?.find((l) => l.locale === def) ||
      row.locales?.[0];
    return loc?.eyebrow?.trim() || null;
  }

  displayList(values?: string[] | null): string[] {
    return (values ?? []).filter(Boolean);
  }

  private toRequest(): ContentUpsertRequest | null {
    const code = this.form.code.trim();
    if (!code) {
      this.error.set('Code requis');
      return null;
    }
    const locales: ContentLocaleDto[] = this.form.locales
      .map((l) => ({
        locale: l.locale,
        eyebrow: l.eyebrow.trim() || null,
        title: l.title.trim(),
        body: l.body.trim() || null,
        cta: l.cta.trim() || null,
      }))
      .filter((l) => !!l.title);
    if (!locales.length) {
      this.error.set('Au moins un titre localisé est requis');
      return null;
    }
    return {
      code,
      slot: this.slot,
      enabled: !!this.form.enabled,
      priority: Number(this.form.priority) || 0,
      pluginId: this.form.pluginId.trim() || null,
      route: this.form.route.trim() || null,
      icon: this.form.icon.trim() || null,
      tone: this.form.tone.trim() || 'blue',
      imageUrl: this.form.imageUrl.trim() || null,
      imageAlt: this.form.imageAlt.trim() || null,
      startsAt: this.form.startsAt.trim() || null,
      endsAt: this.form.endsAt.trim() || null,
      channels: [...(this.form.channels || [])],
      audiences: [...(this.form.audiences || [])],
      locales,
    };
  }

  private emptyForm(): PromoForm {
    return {
      code: '',
      slot: this.slot,
      enabled: true,
      priority: 10,
      pluginId: '',
      route: '',
      icon: '',
      tone: 'blue',
      imageUrl: '',
      imageAlt: '',
      startsAt: '',
      endsAt: '',
      channels: ['MOBILE'],
      audiences: ['RETAIL'],
      locales: [],
    };
  }

  private ensureLocales(): void {
    const existing = new Map(this.form.locales.map((l) => [l.locale, l]));
    this.form.locales = this.langCodes().map(
      (code) =>
        existing.get(code) || {
          locale: code,
          eyebrow: '',
          title: '',
          body: '',
          cta: '',
        }
    );
  }

  private loadLanguages(): void {
    this.i18nApi.languages().subscribe({
      next: (res) => {
        this.languages.set(res.languages ?? []);
        if (res.defaultLanguage) {
          this.defaultLanguage.set(res.defaultLanguage);
        }
        if (this.dialogVisible) {
          this.ensureLocales();
        }
      },
      error: () => this.languages.set([{ code: 'fr', label: 'Français' }, { code: 'en', label: 'English' }]),
    });
  }
}
