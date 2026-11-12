
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { ProviderService } from '../../core/service/provider.service';
import { Flow, SERVICE_TYPE_CODES, ServiceTypeCode, StepType } from '../../core/model/api.models';
import { StepFormioConfigComponent } from '../../shared/components/step-formio-config/step-formio-config.component';
import {
  parseConfigurationJsonToFormio,
  serializeFormioConfiguration,
} from '../../shared/formio/formio-fields.builder';

@Component({
  selector: 'app-provider-flows',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    InputSwitchModule,
    ProgressSpinnerModule,
    StepFormioConfigComponent
  ],
  template: `
    <div class="gw-page-header">
      <h2>Flows de paiement</h2>
      <button pButton type="button" class="p-button-outlined" label="Nouveau flow" icon="pi pi-plus" (click)="showCreate.set(!showCreate())"></button>
    </div>

    @if (showCreate()) {
      <div class="gw-panel">
        <form [formGroup]="createForm" (ngSubmit)="createFlow()" class="gw-form-grid">
          <div class="gw-field">
            <label>Code</label>
            <input pInputText formControlName="code" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Nom</label>
            <input pInputText formControlName="nom" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Type de service</label>
            <p-dropdown formControlName="serviceType" [options]="serviceTypeOptions" styleClass="w-full" />
          </div>
          <div class="gw-field">
            <label>Clé BPMN</label>
            <input pInputText formControlName="bpmProcessKey" class="w-full" placeholder="ex. providerOtpCheckout" />
          </div>
          <div class="gw-field full">
            <label>Description</label>
            <input pInputText formControlName="description" class="w-full" />
          </div>
          <div class="gw-field">
            <label>Actif</label>
            <p-inputSwitch formControlName="actif" />
          </div>
          <div class="gw-actions full">
            <button pButton type="submit" label="Créer" [disabled]="createForm.invalid"></button>
          </div>
        </form>
      </div>
    }

    @if (loading()) {
      <div class="gw-loading"><p-progressSpinner strokeWidth="4" [style]="{ width: '36px', height: '36px' }" /></div>
    } @else {
      <div class="gw-panel">
        <p-table [value]="flows()" styleClass="p-datatable-sm">
          <ng-template pTemplate="header">
            <tr><th>Code</th><th>Service</th><th>Nom</th><th>BPMN</th><th>Étapes</th><th></th></tr>
          </ng-template>
          <ng-template pTemplate="body" let-f>
            <tr>
              <td>{{ f.code }}</td>
              <td>{{ f.serviceType }}</td>
              <td>{{ f.nom }}</td>
              <td>{{ f.bpmProcessKey || '—' }}</td>
              <td>{{ f.steps?.length ?? 0 }}</td>
              <td class="gw-row-actions-cell">
                <button
                  pButton
                  type="button"
                  icon="pi pi-pencil"
                  class="p-button-rounded p-button-text"
                  title="Éditer étapes"
                  aria-label="Éditer étapes"
                  (click)="editSteps(f)"
                ></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr><td colspan="6">Aucun flow</td></tr>
          </ng-template>
        </p-table>
      </div>
    }

    @if (selectedFlow(); as flow) {
      <div class="gw-panel">
        <h3>Étapes — {{ flow.code }}</h3>
        <form [formGroup]="stepsForm" (ngSubmit)="saveSteps()">
          <div formArrayName="steps">
            @for (ctrl of steps.controls; track ctrl; let i = $index) {
              <div [formGroupName]="i" class="step-block">
                <div class="gw-form-grid step-row">
                  <div class="gw-field">
                    <label>Code</label>
                    <input pInputText formControlName="code" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Type</label>
                    <p-dropdown formControlName="stepType" [options]="stepTypes" styleClass="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Ordre</label>
                    <input pInputText type="number" formControlName="ordre" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Opération API</label>
                    <input pInputText formControlName="apiOperationCode" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Succès →</label>
                    <input pInputText formControlName="nextStepSuccess" class="w-full" />
                  </div>
                  <div class="gw-field">
                    <label>Échec →</label>
                    <input pInputText formControlName="nextStepFailure" class="w-full" />
                  </div>
                  <div class="gw-actions">
                    <button pButton type="button" class="p-button-danger p-button-text p-button-sm" label="Suppr." (click)="removeStep(i)"></button>
                  </div>
                </div>
                <app-step-formio-config
                  formControlName="configurationJson"
                  [stepType]="ctrl.get('stepType')?.value"
                />
              </div>
            }
          </div>
          <div class="gw-actions">
            <button pButton type="button" class="p-button-outlined" label="Ajouter étape" icon="pi pi-plus" (click)="addStep()"></button>
            <button pButton type="submit" label="Remplacer les étapes" icon="pi pi-save"></button>
          </div>
        </form>
      </div>
    }
    @if (msg()) { <p class="gw-msg">{{ msg() }}</p> }
  `,
  styles: `
    .step-block {
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 0.75rem;
      margin-bottom: 0.75rem;
    }
    .gw-row-actions-cell {
      width: 3.5rem;
      text-align: center;
      white-space: nowrap;
    }
  `
})

export class ProviderFlowsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ProviderService);
  private readonly fb = inject(FormBuilder);
  private providerId = '';

  readonly stepTypes: StepType[] = [
    'START',
    'FORM',
    'API_CALL',
    'OTP',
    'ENTER_OTP',
    'REDIRECT',
    'WAIT_CALLBACK',
    'CHECK_STATUS',
    'SUCCESS',
    'FAILURE'
  ];
  readonly serviceTypeOptions: Array<{ label: string; value: ServiceTypeCode }> =
    SERVICE_TYPE_CODES.map((value) => ({ label: value, value }));
  readonly columns = ['code', 'serviceType', 'nom', 'bpm', 'steps', 'actions'];
  readonly loading = signal(true);
  readonly showCreate = signal(false);
  readonly flows = signal<Flow[]>([]);
  readonly selectedFlow = signal<Flow | null>(null);
  readonly msg = signal<string | null>(null);

  readonly createForm = this.fb.nonNullable.group({
    code: ['', Validators.required],
    nom: ['', Validators.required],
    serviceType: ['PAYEMENT' as ServiceTypeCode, Validators.required],
    bpmProcessKey: [''],
    description: [''],
    actif: [true]
  });

  readonly stepsForm = this.fb.group({
    steps: this.fb.array([])
  });

  get steps(): FormArray {
    return this.stepsForm.get('steps') as FormArray;
  }

  ngOnInit(): void {
    this.providerId = this.route.parent!.snapshot.paramMap.get('id')!;
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.api.listFlows(this.providerId).subscribe({
      next: (list) => {
        this.flows.set(list);
        this.loading.set(false);
      },
      error: (err: { message?: string }) => {
        this.msg.set(err?.message ?? 'Erreur');
        this.loading.set(false);
      }
    });
  }

  createFlow(): void {
    if (this.createForm.invalid) {
      return;
    }
    const v = this.createForm.getRawValue();
    this.api
      .createFlow(this.providerId, {
        code: v.code,
        nom: v.nom,
        serviceType: v.serviceType,
        bpmProcessKey: v.bpmProcessKey || null,
        description: v.description || null,
        actif: v.actif
      })
      .subscribe({
        next: () => {
          this.showCreate.set(false);
          this.reload();
        },
        error: (err: { message?: string }) => this.msg.set(err?.message ?? 'Erreur')
      });
  }

  editSteps(flow: Flow): void {
    this.api.getFlow(flow.id).subscribe({
      next: (full) => {
        this.selectedFlow.set(full);
        this.steps.clear();
        (full.steps ?? []).forEach((s) => {
          this.steps.push(
            this.fb.nonNullable.group({
              code: [s.code, Validators.required],
              stepType: [s.stepType, Validators.required],
              ordre: [s.ordre, Validators.required],
              apiOperationCode: [s.apiOperationCode ?? ''],
              nextStepSuccess: [s.nextStepSuccess ?? ''],
              nextStepFailure: [s.nextStepFailure ?? ''],
              configurationJson: [s.configurationJson ?? ''],
              actif: [s.actif]
            })
          );
        });
        if (!this.steps.length) {
          this.addStep();
        }
      },
      error: (err: { message?: string }) => this.msg.set(err?.message ?? 'Erreur')
    });
  }

  addStep(): void {
    this.steps.push(
      this.fb.nonNullable.group({
        code: ['', Validators.required],
        stepType: ['API_CALL' as StepType, Validators.required],
        ordre: [this.steps.length + 1, Validators.required],
        apiOperationCode: [''],
        nextStepSuccess: [''],
        nextStepFailure: [''],
        configurationJson: [''],
        actif: [true]
      })
    );
  }

  removeStep(i: number): void {
    this.steps.removeAt(i);
  }

  saveSteps(): void {
    const flow = this.selectedFlow();
    if (!flow) {
      return;
    }
    const body = this.steps.getRawValue().map((s: {
      code: string;
      stepType: StepType;
      ordre: number;
      apiOperationCode: string;
      nextStepSuccess: string;
      nextStepFailure: string;
      configurationJson: string;
      actif: boolean;
    }) => ({
      code: s.code,
      stepType: s.stepType,
      ordre: s.ordre,
      apiOperationCode: s.apiOperationCode || null,
      nextStepSuccess: s.nextStepSuccess || null,
      nextStepFailure: s.nextStepFailure || null,
      configurationJson:
        s.stepType === 'FORM'
          ? serializeFormioConfiguration(parseConfigurationJsonToFormio(s.configurationJson))
          : s.configurationJson || null,
      actif: s.actif
    }));
    this.api.replaceSteps(flow.id, body).subscribe({
      next: (updated) => {
        this.selectedFlow.set(updated);
        this.msg.set('Étapes enregistrées');
        this.reload();
      },
      error: (err: { message?: string }) => this.msg.set(err?.message ?? 'Erreur')
    });
  }
}
