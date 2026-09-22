import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';
import { Producto, ProductoPeticion } from '../core/modelos/catalogo.model';

@Injectable({ providedIn: 'root' })
export class ProductosServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/productos`;

  listar(buscar?: string, activo?: boolean): Observable<Producto[]> {
    let params = new HttpParams();
    if (buscar) {
      params = params.set('buscar', buscar);
    }
    if (activo !== undefined) {
      params = params.set('activo', String(activo));
    }
    return this.http.get<Producto[]>(this.base, { params });
  }

  crear(peticion: ProductoPeticion): Observable<Producto> {
    return this.http.post<Producto>(this.base, peticion);
  }

  actualizar(id: number, peticion: ProductoPeticion): Observable<Producto> {
    return this.http.put<Producto>(`${this.base}/${id}`, peticion);
  }

  activar(id: number): Observable<Producto> {
    return this.http.patch<Producto>(`${this.base}/${id}/activar`, {});
  }

  desactivar(id: number): Observable<Producto> {
    return this.http.patch<Producto>(`${this.base}/${id}/desactivar`, {});
  }
}
