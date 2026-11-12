import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SupportedLanguage {
  code: string;
  label: string;
}

export interface LanguagesResponse {
  defaultLanguage: string;
  languages: SupportedLanguage[];
}

@Injectable({ providedIn: 'root' })
export class I18nApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/i18n`;

  languages(): Observable<LanguagesResponse> {
    return this.http.get<LanguagesResponse>(`${this.base}/languages`);
  }
}
