import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';
import { RegistroBitacora } from '../core/modelos/bitacora.model';

@Injectable({ providedIn: 'root' })
export class BitacoraServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/bitacora`;

  consultar(usuarioId?: number, modulo?: string, desde?: string, hasta?: string): Observable<RegistroBitacora[]> {
    let params = new HttpParams();
    if (usuarioId) {
      params = params.set('usuarioId', String(usuarioId));
    }
    if (modulo) {
      params = params.set('modulo', modulo);
    }
    if (desde) {
      params = params.set('desde', desde);
    }
    if (hasta) {
      params = params.set('hasta', hasta);
    }
    return this.http.get<RegistroBitacora[]>(this.base, { params });
  }
}
