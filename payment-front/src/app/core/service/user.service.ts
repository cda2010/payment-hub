import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminUser, CreateUserRequest, Page } from '../model/api.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/users`;

  list(page = 0, size = 20): Observable<Page<AdminUser>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<AdminUser>>(this.base, { params });
  }

  create(body: CreateUserRequest): Observable<AdminUser> {
    return this.http.post<AdminUser>(this.base, body);
  }

  setEnabled(id: string, enabled: boolean): Observable<AdminUser> {
    const params = new HttpParams().set('enabled', String(enabled));
    return this.http.patch<AdminUser>(`${this.base}/${id}/enabled`, null, { params });
  }
}
