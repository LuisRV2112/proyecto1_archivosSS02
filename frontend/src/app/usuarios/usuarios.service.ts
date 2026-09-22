import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';
import {
  RolDisponible,
  UsuarioActualizacionPeticion,
  UsuarioAdmin,
  UsuarioPeticion,
} from '../core/modelos/usuario-admin.model';

@Injectable({ providedIn: 'root' })
export class UsuariosServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/usuarios`;

  listar(): Observable<UsuarioAdmin[]> {
    return this.http.get<UsuarioAdmin[]>(this.base);
  }

  roles(): Observable<RolDisponible[]> {
    return this.http.get<RolDisponible[]>(`${this.base}/roles`);
  }

  crear(peticion: UsuarioPeticion): Observable<UsuarioAdmin> {
    return this.http.post<UsuarioAdmin>(this.base, peticion);
  }

  actualizar(id: number, peticion: UsuarioActualizacionPeticion): Observable<UsuarioAdmin> {
    return this.http.put<UsuarioAdmin>(`${this.base}/${id}`, peticion);
  }

  activar(id: number): Observable<UsuarioAdmin> {
    return this.http.patch<UsuarioAdmin>(`${this.base}/${id}/activar`, {});
  }

  desactivar(id: number): Observable<UsuarioAdmin> {
    return this.http.patch<UsuarioAdmin>(`${this.base}/${id}/desactivar`, {});
  }
}
