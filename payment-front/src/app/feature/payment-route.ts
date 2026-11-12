import { Routes } from '@angular/router';
import { RF_SHELL_MODE } from '@rapidframe/frontend-angular';
import { authGuard } from '../core/guards/auth.guard';
import { ProductHubComponent } from './products/product-hub.component';
import { ProductFlowComponent } from './products/product-flow.component';
import { TransactionHistoryComponent } from './products/transaction-history.component';

const toolsShell = { [RF_SHELL_MODE]: 'tools' as const };

/**
 * Hub + parcours dans rf-shell (cadres rf-card / rf-panel).
 * Champs de formulaire restent PrimeNG.
 */
const routes: Routes = [
  {
    path: 'products',
    component: ProductHubComponent,
    canActivate: [authGuard],
    data: toolsShell,
  },
  {
    path: 'history',
    component: TransactionHistoryComponent,
    canActivate: [authGuard],
    data: toolsShell,
  },
  {
    path: 'products/:code/:paymentId',
    component: ProductFlowComponent,
    canActivate: [authGuard],
    data: toolsShell,
  },
  {
    path: 'products/:code',
    component: ProductFlowComponent,
    canActivate: [authGuard],
    data: toolsShell,
  },

  // Alias → parcours générique
  { path: 'payment', redirectTo: 'products/CHECKOUT', pathMatch: 'full' },
  { path: 'payment/:paymentId', redirectTo: 'products/CHECKOUT/:paymentId' },
  { path: 'bills', redirectTo: 'products/BILL_PAYMENT', pathMatch: 'full' },
  { path: 'bills/:paymentId', redirectTo: 'products/BILL_PAYMENT/:paymentId' },
  { path: 'transfer', redirectTo: 'products/ACCOUNT_TRANSFER', pathMatch: 'full' },
  { path: 'transfer/:paymentId', redirectTo: 'products/ACCOUNT_TRANSFER/:paymentId' },
];

export default routes;
