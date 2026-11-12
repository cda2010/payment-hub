import { ApplicationConfig, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ConfirmationService, MessageService } from 'primeng/api';

import { routes } from './app.routes';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { ErrorInterceptor } from './core/interceptors/error.interceptor';
import { RapidFrameModule } from '@rapidframe/frontend-angular';
import { provideRapidFrameFormioDesigner } from '@rapidframe/formio-designer';
import { provideRapidFrameBpmnDesigner } from '@rapidframe/bpmn-designer';
import { rapidframeApplication } from './application.config';
import { environment } from '../environments/environment';

const rapidframeApiUrl = environment.rapidframeApiUrl;

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    provideAnimationsAsync(),
    MessageService,
    ConfirmationService,
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    provideRapidFrameFormioDesigner({ apiUrl: rapidframeApiUrl }),
    provideRapidFrameBpmnDesigner({
      apiUrl: rapidframeApiUrl,
      propertiesPanel: {
        width: 340,
        frenchLabels: true,
        enableCamundaProvider: false,
      },
      stepCapabilities: {
        vocabularyUrl: `${environment.apiUrl}/catalog/capabilities`,
      },
    }),
    importProvidersFrom(
      RapidFrameModule.forRoot({
        apiUrl: rapidframeApiUrl,
        enableCache: false,
        uiLibrary: 'primeng',
        application: rapidframeApplication,
        moduleSync: 'off',
        /** Pas de catalogue sur '/' — le checkout reste la landing publique. */
        applicationRoutes: {
          enableHomeCatalog: false,
          enableMetaRoute: true,
          enableLegacyNameRedirects: false,
        },
        auth: {
          tokenKey: 'gw_access_token',
          loginUrl: '/login',
        },
      }),
    ),
  ],
};
