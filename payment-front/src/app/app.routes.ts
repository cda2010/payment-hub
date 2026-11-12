import { Routes } from '@angular/router';
import { RF_SHELL_MODE } from '@rapidframe/frontend-angular';
import {
  BpmnDesignerPageComponent,
  DmnDesignerPageComponent,
} from '@rapidframe/bpmn-designer';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './core/auth/login/login.component';
import { AdminFormDesignerPageComponent } from './pages/admin-form-designer-page.component';

const toolsShell = { [RF_SHELL_MODE]: 'tools' as const };

/**
 * Pattern paymentHub / budgetApp :
 * - designers + pages custom admin en routes racine (rendues dans &lt;rf-shell&gt;)
 * - /m/{slug} injecté par RapidFrameModule (menus XML)
 * - products (hub + parcours) dans rf-shell ; champs formulaire en PrimeNG
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'login', component: LoginComponent },
  {
    path: 'designer',
    component: AdminFormDesignerPageComponent,
    canActivate: [authGuard],
    data: toolsShell,
  },
  {
    path: 'bpmn-designer',
    component: BpmnDesignerPageComponent,
    canActivate: [authGuard],
    data: toolsShell,
  },
  {
    path: 'dmn-designer',
    component: DmnDesignerPageComponent,
    canActivate: [authGuard],
    data: toolsShell,
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    loadChildren: () => import('./feature/admin-route'),
  },
  {
    path: '',
    loadChildren: () => import('./feature/payment-route'),
  },
];
