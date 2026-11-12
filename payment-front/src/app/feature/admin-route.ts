import { Routes } from '@angular/router';
import { RF_SHELL_MODE } from '@rapidframe/frontend-angular';
import { roleGuard } from '../core/guards/auth.guard';
import { AdminDashboardComponent } from './dashboard/admin-dashboard.component';
import { ProvidersListComponent } from './providers/providers-list.component';
import { PaysAdminComponent } from './pays/pays-admin.component';
import { ProviderDetailComponent } from './providers/provider-detail.component';
import { ProviderGeneralComponent } from './providers/provider-general.component';
import { ProviderEnvironmentsComponent } from './providers/provider-environments.component';
import { ProviderApisComponent } from './providers/provider-apis.component';
import { ProviderOrchestrationComponent } from './providers/provider-orchestration.component';
import { ProviderAuthenticationComponent } from './providers/provider-authentication.component';
import { ProviderFlowsComponent } from './providers/provider-flows.component';
import { ProviderMappingsComponent } from './providers/provider-mappings.component';
import { ProviderWebhooksComponent } from './providers/provider-webhooks.component';
import { ProviderWizardComponent } from './providers/provider-wizard.component';
import { TransactionsListComponent } from './transactions/transactions-list.component';
import { TransactionDetailComponent } from './transactions/transaction-detail.component';
import { UsersAdminComponent } from './users/users-admin.component';
import { AuditListComponent } from './audit/audit-list.component';
import { BillersAdminComponent } from './billers/billers-admin.component';
import { TransfersAdminComponent } from './transfers-admin/transfers-admin.component';
import { CatalogAdminComponent } from './catalog/catalog-admin.component';
import { CatalogDeServiceAdminComponent } from './catalog/catalog-de-service-admin.component';
import { SchemaEcrituresAdminComponent } from './orchestration/schema-ecritures-admin.component';
import { InterfaceUtilisateursAdminComponent } from './orchestration/interface-utilisateurs-admin.component';
import { CorridorsFseAdminComponent } from './orchestration/corridors-fse-admin.component';
import { ComptesFseAdminComponent } from './orchestration/comptes-fse-admin.component';
import { ApprovalProfilesAdminComponent } from './approvals/approval-profiles-admin.component';
import { ApprovalInboxComponent } from './approvals/approval-inbox.component';
import { ContentPromosAdminComponent } from './content/content-promos-admin.component';
import { ContentCmsAdminComponent } from './content/content-cms-admin.component';
import { AdminLogoutComponent } from './admin-logout.component';
import { GrillesListComponent } from './tarification/grilles-list.component';
import { GrilleDetailComponent } from './tarification/grille-detail.component';
import { FormulesComponent } from './tarification/formules.component';
import { SimulateurComponent } from './tarification/simulateur.component';
import { GrillesCommissionListComponent } from './commission/grilles-commission-list.component';
import { GrilleCommissionDetailComponent } from './commission/grille-commission-detail.component';
import { SimulateurCommissionComponent } from './commission/simulateur-commission.component';
import { ReglesListComponent } from './rules/regles-list.component';
import { GroupeDetailComponent } from './rules/groupe-detail.component';
import { SimulateurReglesComponent } from './rules/simulateur-regles.component';
import { UnitesListComponent } from './reseau/unites-list.component';
import { UniteDetailComponent } from './reseau/unite-detail.component';
import { ArbreReseauComponent } from './reseau/arbre-reseau.component';
import { ZonesComponent } from './reseau/zones.component';
import { ImportReseauComponent } from './reseau/import-reseau.component';
import { PartiesListComponent } from './identite/parties-list.component';
import { PartyDetailComponent } from './identite/party-detail.component';
import { ProfilsListComponent } from './identite/profils-list.component';

const toolsShell = { [RF_SHELL_MODE]: 'tools' as const };

/**
 * Admin complet dans rf-shell (mode tools).
 * Providers / billers / transfers / users : UI riches (JSON, nested).
 * Grilles RF plates restent accessibles via /m/… (meta).
 */
const routes: Routes = [
  { path: '', component: AdminDashboardComponent, data: toolsShell },
  { path: 'providers', component: ProvidersListComponent, data: toolsShell },
  { path: 'pays', component: PaysAdminComponent, data: toolsShell },
  { path: 'providers/new', component: ProviderWizardComponent, data: toolsShell },
  {
    path: 'providers/:id',
    component: ProviderDetailComponent,
    data: toolsShell,
    children: [
      { path: '', redirectTo: 'apis', pathMatch: 'full' },
      { path: 'general', component: ProviderGeneralComponent, data: toolsShell },
      { path: 'environments', component: ProviderEnvironmentsComponent, data: toolsShell },
      { path: 'operations', redirectTo: 'apis', pathMatch: 'full' },
      { path: 'apis', component: ProviderApisComponent, data: toolsShell },
      { path: 'orchestration', component: ProviderOrchestrationComponent, data: toolsShell },
      { path: 'authentication', component: ProviderAuthenticationComponent, data: toolsShell },
      { path: 'flows', component: ProviderFlowsComponent, data: toolsShell },
      { path: 'mappings', component: ProviderMappingsComponent, data: toolsShell },
      { path: 'webhooks', component: ProviderWebhooksComponent, data: toolsShell },
    ],
  },
  { path: 'billers', component: BillersAdminComponent, data: toolsShell },
  { path: 'catalog', component: CatalogAdminComponent, data: toolsShell },
  { path: 'catalog-services', component: CatalogDeServiceAdminComponent, data: toolsShell },
  { path: 'schema-ecritures', component: SchemaEcrituresAdminComponent, data: toolsShell },
  { path: 'interface-utilisateurs', component: InterfaceUtilisateursAdminComponent, data: toolsShell },
  { path: 'corridors-fse', component: CorridorsFseAdminComponent, data: toolsShell },
  { path: 'comptes-fse', component: ComptesFseAdminComponent, data: toolsShell },
  { path: 'transfers', component: TransfersAdminComponent, data: toolsShell },
  { path: 'tarification', redirectTo: 'tarification/grilles', pathMatch: 'full' },
  { path: 'tarification/grilles', component: GrillesListComponent, data: toolsShell },
  { path: 'tarification/grilles/:id', component: GrilleDetailComponent, data: toolsShell },
  { path: 'tarification/formules', component: FormulesComponent, data: toolsShell },
  { path: 'tarification/simulateur', component: SimulateurComponent, data: toolsShell },
  { path: 'commission', redirectTo: 'commission/grilles', pathMatch: 'full' },
  { path: 'commission/grilles', component: GrillesCommissionListComponent, data: toolsShell },
  { path: 'commission/grilles/:id', component: GrilleCommissionDetailComponent, data: toolsShell },
  { path: 'commission/simulateur', component: SimulateurCommissionComponent, data: toolsShell },
  { path: 'rules', component: ReglesListComponent, data: toolsShell },
  { path: 'rules/groupes/:code', component: GroupeDetailComponent, data: toolsShell },
  { path: 'rules/simulateur', component: SimulateurReglesComponent, data: toolsShell },
  { path: 'reseau', redirectTo: 'reseau/arbre', pathMatch: 'full' },
  { path: 'reseau/arbre', component: ArbreReseauComponent, data: toolsShell },
  { path: 'reseau/unites', component: UnitesListComponent, data: toolsShell },
  { path: 'reseau/unites/:id', component: UniteDetailComponent, data: toolsShell },
  { path: 'reseau/regroupements', component: UnitesListComponent, data: { ...toolsShell, regroupements: true } },
  { path: 'reseau/zones', component: ZonesComponent, data: toolsShell },
  { path: 'reseau/import', component: ImportReseauComponent, data: toolsShell },
  { path: 'identite', redirectTo: 'identite/parties', pathMatch: 'full' },
  { path: 'identite/parties', component: PartiesListComponent, data: toolsShell },
  { path: 'identite/parties/:id', component: PartyDetailComponent, data: toolsShell },
  { path: 'identite/profils', redirectTo: 'identite/profils/services', pathMatch: 'full' },
  { path: 'identite/profils/services', component: ProfilsListComponent, data: { ...toolsShell, profileKind: 'SERVICE' } },
  { path: 'identite/profils/tarifs', component: ProfilsListComponent, data: { ...toolsShell, profileKind: 'TARIFF' } },
  { path: 'identite/profils/limites', component: ProfilsListComponent, data: { ...toolsShell, profileKind: 'LIMIT' } },
  { path: 'approval-profiles', component: ApprovalProfilesAdminComponent, data: toolsShell },
  { path: 'content-promos', component: ContentPromosAdminComponent, data: toolsShell },
  { path: 'content-cms', component: ContentCmsAdminComponent, data: toolsShell },
  { path: 'approvals', component: ApprovalInboxComponent, data: toolsShell },
  { path: 'transactions', component: TransactionsListComponent, data: toolsShell },
  {
    path: 'transactions/:paymentId',
    component: TransactionDetailComponent,
    data: toolsShell,
  },
  {
    path: 'users',
    component: UsersAdminComponent,
    canActivate: [roleGuard('ADMIN')],
    data: toolsShell,
  },
  {
    path: 'audit',
    component: AuditListComponent,
    canActivate: [roleGuard('ADMIN', 'AUDITOR')],
    data: toolsShell,
  },
  { path: 'logout', component: AdminLogoutComponent, data: toolsShell },
];

export default routes;
