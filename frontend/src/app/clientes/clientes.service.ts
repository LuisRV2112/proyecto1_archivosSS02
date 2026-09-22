import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';
import { Cliente, ClientePeticion } from '../core/modelos/catalogo.model';

@Injectable({ providedIn: 'root' })
export class ClientesServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/clientes`;

  listar(buscar?: string, activo?: boolean): Observable<Cliente[]> {
    let params = new HttpParams();
    if (buscar) {
      params = params.set('buscar', buscar);
    }
    if (activo !== undefined) {
      params = params.set('activo', String(activo));
    }
    return this.http.get<Cliente[]>(this.base, { params });
  }

  crear(peticion: ClientePeticion): Observable<Cliente> {
    return this.http.post<Cliente>(this.base, peticion);
  }

  actualizar(id: number, peticion: ClientePeticion): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.base}/${id}`, peticion);
  }

  activar(id: number): Observable<Cliente> {
    return this.http.patch<Cliente>(`${this.base}/${id}/activar`, {});
  }

  desactivar(id: number): Observable<Cliente> {
    return this.http.patch<Cliente>(`${this.base}/${id}/desactivar`, {});
  }
}
