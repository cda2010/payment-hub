import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

export interface ApiErrorDetail {
  code: string;
  message: string;
  tranche?: number | null;
  champ?: string | null;
}

export interface ApiErrorBody {
  code?: string;
  message?: string;
  details?: ApiErrorDetail[];
}

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: unknown) => {
        // Erreurs déjà typées RapidFrame : ne pas les réécrire
        if (error && typeof error === 'object' && 'type' in error && 'message' in error) {
          return throwError(() => error);
        }
        if (!(error instanceof HttpErrorResponse)) {
          return throwError(() => error);
        }
        const body = error.error as ApiErrorBody | null;
        const friendly = this.toUserMessage(error.status, body);
        return throwError(() => ({
          status: error.status,
          code: body?.code ?? 'HTTP_ERROR',
          message: friendly,
          details: body?.details ?? [],
          raw: error
        }));
      })
    );
  }

  private toUserMessage(status: number, body: ApiErrorBody | null): string {
    if (body?.message) {
      return body.message;
    }
    switch (status) {
      case 0:
        return 'Impossible de joindre le serveur. Vérifiez votre connexion.';
      case 401:
        return 'Session expirée. Veuillez vous reconnecter.';
      case 403:
        return 'Vous n’avez pas les droits pour cette action.';
      case 404:
        return 'Ressource introuvable.';
      case 503:
        return 'Service momentanément indisponible. Veuillez réessayer.';
      default:
        return 'Une erreur est survenue. Veuillez réessayer.';
    }
  }
}
