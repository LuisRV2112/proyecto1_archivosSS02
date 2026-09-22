import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';
import { Categoria, CategoriaPeticion } from '../core/modelos/catalogo.model';

@Injectable({ providedIn: 'root' })
export class CategoriasServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/categorias`;

  listar(buscar?: string, activo?: boolean): Observable<Categoria[]> {
    let params = new HttpParams();
    if (buscar) {
      params = params.set('buscar', buscar);
    }
    if (activo !== undefined) {
      params = params.set('activo', String(activo));
    }
    return this.http.get<Categoria[]>(this.base, { params });
  }

  crear(peticion: CategoriaPeticion): Observable<Categoria> {
    return this.http.post<Categoria>(this.base, peticion);
  }

  actualizar(id: number, peticion: CategoriaPeticion): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.base}/${id}`, peticion);
  }

  activar(id: number): Observable<Categoria> {
    return this.http.patch<Categoria>(`${this.base}/${id}/activar`, {});
  }

  desactivar(id: number): Observable<Categoria> {
    return this.http.patch<Categoria>(`${this.base}/${id}/desactivar`, {});
  }
}
