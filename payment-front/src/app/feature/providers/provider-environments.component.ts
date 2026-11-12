import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { ProviderService } from '../../core/service/provider.service';
import { Environment, ProviderEnvironmentCode } from '../../core/model/api.models';

@Component({
  selector: 'app-provider-environments',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    InputSwitchModule,
    ProgressSpinnerModule
  ],
  template: `
    <div class="gw-page-header">
      <h2>Environnements</h2>
      <button
        pButton
        type="button"
        class="p-button-outlined"
        [label]="editingId() ? 'Annuler' : 'Ajouter'"
        [icon]="editingId() ? 'pi pi-times' : 'pi pi-plus'"
        (click)="toggleForm()"
      ></button>
    </div>

    @if (showForm()) {
      <div class="gw-panel">
        <h3>{{ editingId() ? "Modifier l'environnement" : 'Nouvel environnement' }}</h3>
        <form [formGroup]="form" (ngSubmit)="save()" class="gw-form-grid">
          <div class="gw-field">
            <label>Code</label>
            <p-dropdown
              formControlName="code"
              [options]="codeOptions"
              optionLabel="label"
              optionValue="value"
              placeholder="SANDBOX / DEVELOPPEMENT / PRODUCTION"
              styleClass="w-full"
            />
          </div>
          <div class="gw-field">
            <label>Base URL</label>
            <input pInputText formControlName="baseUrl" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Timeout (s)</label>
            <input pInputText type="number" formControlName="timeoutSeconds" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Actif</label>
            <p-inputSwitch formControlName="actif" />
            <small class="subtitle">Un seul environnement actif à la fois</small>
          </div>
          <div class="gw-actions full">
            <button
              pButton
              type="submit"
              [label]="editingId() ? 'Enregistrer' : 'Créer'"
              [disabled]="form.invalid || saving()"
              [loading]="saving()"
            ></button>
            @if (editingId()) {
              <button pButton type="button" class="p-button-text" label="Annuler" (click)="cancelEdit()"></button>
            }
          </div>
        </form>
      </div>
    }

    @if (loading()) {
      <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
    } @else {
      <div class="gw-panel">
        <p-table [value]="items()" styleClass="p-datatable-sm">
          <ng-template pTemplate="header">
            <tr>
              <th>Code</th>
              <th>Base URL</th>
              <th>Timeout</th>
              <th>Actif</th>
              <th style="width: 4rem"></th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-e>
            <tr>
              <td>{{ e.code }}</td>
              <td>{{ e.baseUrl }}</td>
              <td>{{ e.timeoutSeconds ?? '—' }}</td>
              <td>{{ e.actif ? 'Oui' : 'Non' }}</td>
              <td>
                <button
                  pButton
                  type="button"
                  class="p-button-rounded p-button-text"
                  icon="pi pi-pencil"
                  (click)="startEdit(e)"
                  aria-label="Modifier"
                ></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td colspan="5">Aucun environnement</td></tr>
          </ng-template>
        </p-table>
      </div>
    }
    @if (msg()) {
      <p class="gw-msg" [class.error]="isError()">{{ msg() }}</p>
    }
  `,
  styles: `
    .subtitle { display: block; margin-top: 0.25rem; color: var(--text-color-secondary, #64748b); font-size: 0.8rem; }
    h3 { margin: 0 0 0.75rem; font-size: 1rem; }
  `
})
export class ProviderEnvironmentsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ProviderService);
  private readonly fb = inject(FormBuilder);
  private providerId = '';

  readonly codeOptions: Array<{ label: string; value: ProviderEnvironmentCode }> = [
    { label: 'SANDBOX', value: 'SANDBOX' },
    { label: 'DEVELOPPEMENT', value: 'DEVELOPPEMENT' },
    { label: 'PRODUCTION', value: 'PRODUCTION' }
  ];

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly showForm = signal(false);
  readonly editingId = signal<string | null>(null);
  readonly items = signal<Environment[]>([]);
  readonly msg = signal<string | null>(null);
  readonly isError = signal(false);

  readonly form = this.fb.nonNullable.group({
    code: ['SANDBOX' as ProviderEnvironmentCode, Validators.required],
    baseUrl: ['', Validators.required],
    timeoutSeconds: [30],
    actif: [false]
  });

  ngOnInit(): void {
    this.providerId = this.route.parent!.snapshot.paramMap.get('id')!;
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.api.listEnvironments(this.providerId).subscribe({
      next: (list) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.msg.set(err?.message ?? 'Erreur');
        this.isError.set(true);
        this.loading.set(false);
      }
    });
  }

  toggleForm(): void {
    if (this.editingId() || this.showForm()) {
      this.cancelEdit();
      return;
    }
    this.showForm.set(true);
    this.form.reset({ code: 'SANDBOX', baseUrl: '', timeoutSeconds: 30, actif: false });
    this.form.controls.code.enable();
  }

  startEdit(env: Environment): void {
    this.editingId.set(env.id);
    this.showForm.set(true);
    this.msg.set(null);
    const code = (env.code?.toUpperCase() || 'SANDBOX') as ProviderEnvironmentCode;
    this.form.reset({
      code: this.codeOptions.some((o) => o.value === code) ? code : 'SANDBOX',
      baseUrl: env.baseUrl ?? '',
      timeoutSeconds: env.timeoutSeconds ?? 30,
      actif: !!env.actif
    });
    // Le code reste fixe en édition (évite les collisions / liens auth-API)
    this.form.controls.code.disable();
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.showForm.set(false);
    this.saving.set(false);
    this.form.controls.code.enable();
    this.form.reset({ code: 'SANDBOX', baseUrl: '', timeoutSeconds: 30, actif: false });
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }
    this.saving.set(true);
    this.msg.set(null);
    const raw = this.form.getRawValue();
    const body = {
      code: raw.code,
      baseUrl: raw.baseUrl,
      timeoutSeconds: raw.timeoutSeconds,
      actif: raw.actif
    };
    const id = this.editingId();
    const req$ = id
      ? this.api.updateEnvironment(this.providerId, id, body)
      : this.api.createEnvironment(this.providerId, body);

    req$.subscribe({
      next: () => {
        this.msg.set(id ? 'Environnement mis à jour' : 'Environnement créé');
        this.isError.set(false);
        this.saving.set(false);
        this.cancelEdit();
        this.reload();
      },
      error: (err: { message?: string }) => {
        this.msg.set(err?.message ?? 'Erreur');
        this.isError.set(true);
        this.saving.set(false);
      }
    });
  }
}
