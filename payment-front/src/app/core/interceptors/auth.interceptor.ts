import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable, catchError, from, switchMap, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private readonly auth: AuthService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const lang = (typeof navigator !== 'undefined' && navigator.language)
      ? navigator.language
      : 'fr';
    // Le token n'est envoyé qu'aux API de l'application (URL relatives ou même origine).
    const ownApi = req.url.startsWith('/') || req.url.startsWith(location.origin);
    const token$ = ownApi ? from(this.auth.getValidAccessToken()) : from(Promise.resolve(null));

    return token$.pipe(
      switchMap((token) => {
        const headers: Record<string, string> = { 'Accept-Language': lang };
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
        }
        return next.handle(req.clone({ setHeaders: headers }));
      }),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && ownApi && this.auth.isAuthenticated()) {
          this.auth.expireSession();
        }
        return throwError(() => error);
      })
    );
  }
}
