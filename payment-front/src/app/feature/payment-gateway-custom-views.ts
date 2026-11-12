import { Type, Component } from '@angular/core';
import { RF_CUSTOM_ACTION_VIEWS } from '@rapidframe/frontend-angular';
import { AdminDashboardComponent } from '../feature/dashboard/admin-dashboard.component';
import { BillersAdminComponent } from '../feature/billers/billers-admin.component';
import { TransactionsListComponent } from '../feature/transactions/transactions-list.component';
import { UsersAdminComponent } from '../feature/users/users-admin.component';
import { AuditListComponent } from '../feature/audit/audit-list.component';
import { ProvidersListComponent } from '../feature/providers/providers-list.component';
import { CatalogAdminComponent } from '../feature/catalog/catalog-admin.component';
import { CatalogDeServiceAdminComponent } from '../feature/catalog/catalog-de-service-admin.component';
import { SchemaEcrituresAdminComponent } from '../feature/orchestration/schema-ecritures-admin.component';
import { InterfaceUtilisateursAdminComponent } from '../feature/orchestration/interface-utilisateurs-admin.component';
import { ComptesFseAdminComponent } from '../feature/orchestration/comptes-fse-admin.component';
import { ApprovalProfilesAdminComponent } from '../feature/approvals/approval-profiles-admin.component';
import { ApprovalInboxComponent } from '../feature/approvals/approval-inbox.component';
import { ContentPromosAdminComponent } from '../feature/content/content-promos-admin.component';
import { ContentCmsAdminComponent } from '../feature/content/content-cms-admin.component';
import { GrillesListComponent } from '../feature/tarification/grilles-list.component';
import { FormulesComponent } from '../feature/tarification/formules.component';
import { SimulateurComponent } from '../feature/tarification/simulateur.component';
import { GrillesCommissionListComponent } from '../feature/commission/grilles-commission-list.component';
import { SimulateurCommissionComponent } from '../feature/commission/simulateur-commission.component';
import { ReglesListComponent } from '../feature/rules/regles-list.component';
import { SimulateurReglesComponent } from '../feature/rules/simulateur-regles.component';
import { ArbreReseauComponent } from '../feature/reseau/arbre-reseau.component';
import { UnitesListComponent } from '../feature/reseau/unites-list.component';
import { ZonesComponent } from '../feature/reseau/zones.component';
import { ImportReseauComponent } from '../feature/reseau/import-reseau.component';
import { PartiesListComponent } from '../feature/identite/parties-list.component';
import { ProfilsListComponent } from '../feature/identite/profils-list.component';

@Component({
  standalone: true,
  imports: [UnitesListComponent],
  template: `<app-unites-list [seulementRegroupements]="true" />`,
})
class RegroupementsViewComponent {}

@Component({
  standalone: true,
  imports: [ProfilsListComponent],
  template: `<app-profils-list [kindForce]="'SERVICE'" />`,
})
class ProfilsServiceViewComponent {}

@Component({
  standalone: true,
  imports: [ProfilsListComponent],
  template: `<app-profils-list [kindForce]="'TARIFF'" />`,
})
class ProfilsTarifViewComponent {}

@Component({
  standalone: true,
  imports: [ProfilsListComponent],
  template: `<app-profils-list [kindForce]="'LIMIT'" />`,
})
class ProfilsLimiteViewComponent {}

/**
 * Action-views du menu XML. L'écran Angular s'ouvre dans un onglet du module.
 */
export const PAYMENT_GATEWAY_CUSTOM_ACTION_VIEWS: Readonly<Record<string, Type<unknown>>> = {
  'admin-dashboard': AdminDashboardComponent,
  'provider-all': ProvidersListComponent,
  'catalog-all': CatalogAdminComponent,
  'catalog-services-all': CatalogDeServiceAdminComponent,
  'schema-ecritures-all': SchemaEcrituresAdminComponent,
  'interface-utilisateurs-all': InterfaceUtilisateursAdminComponent,
  'comptes-fse-all': ComptesFseAdminComponent,
  'biller-all': BillersAdminComponent,
  'approval-profile-all': ApprovalProfilesAdminComponent,
  'content-promo-all': ContentPromosAdminComponent,
  'content-cms-all': ContentCmsAdminComponent,
  'approval-inbox': ApprovalInboxComponent,
  'transaction-all': TransactionsListComponent,
  'user-all': UsersAdminComponent,
  'audit-all': AuditListComponent,
  'tarif-grilles-all': GrillesListComponent,
  'tarif-formules-all': FormulesComponent,
  'tarif-simulateur-all': SimulateurComponent,
  'commission-grilles-all': GrillesCommissionListComponent,
  'commission-simulateur-all': SimulateurCommissionComponent,
  'rules-all': ReglesListComponent,
  'rules-simulateur-all': SimulateurReglesComponent,
  'reseau-arbre-all': ArbreReseauComponent,
  'reseau-unites-all': UnitesListComponent,
  'reseau-regroupements-all': RegroupementsViewComponent,
  'reseau-zones-all': ZonesComponent,
  'reseau-import-all': ImportReseauComponent,
  'identite-parties-all': PartiesListComponent,
  'identite-profils-service-all': ProfilsServiceViewComponent,
  'identite-profils-tarif-all': ProfilsTarifViewComponent,
  'identite-profils-limite-all': ProfilsLimiteViewComponent,
};

export function providePaymentGatewayCustomActionViews() {
  return {
    provide: RF_CUSTOM_ACTION_VIEWS,
    useValue: PAYMENT_GATEWAY_CUSTOM_ACTION_VIEWS,
  };
}
