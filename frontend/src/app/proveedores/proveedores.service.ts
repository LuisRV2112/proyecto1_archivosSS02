import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';
import {
  Proveedor,
  ProveedorPeticion,
  ProductoProveedor,
  ProductoProveedorPeticion,
} from '../core/modelos/catalogo.model';

@Injectable({ providedIn: 'root' })
export class ProveedoresServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/proveedores`;

  listar(buscar?: string, activo?: boolean): Observable<Proveedor[]> {
    let params = new HttpParams();
    if (buscar) {
      params = params.set('buscar', buscar);
    }
    if (activo !== undefined) {
      params = params.set('activo', String(activo));
    }
    return this.http.get<Proveedor[]>(this.base, { params });
  }

  crear(peticion: ProveedorPeticion): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.base, peticion);
  }

  actualizar(id: number, peticion: ProveedorPeticion): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.base}/${id}`, peticion);
  }

  activar(id: number): Observable<Proveedor> {
    return this.http.patch<Proveedor>(`${this.base}/${id}/activar`, {});
  }

  desactivar(id: number): Observable<Proveedor> {
    return this.http.patch<Proveedor>(`${this.base}/${id}/desactivar`, {});
  }

  listarProductos(proveedorId: number): Observable<ProductoProveedor[]> {
    return this.http.get<ProductoProveedor[]>(`${this.base}/${proveedorId}/productos`);
  }

  asociarProducto(proveedorId: number, peticion: ProductoProveedorPeticion): Observable<ProductoProveedor> {
    return this.http.post<ProductoProveedor>(`${this.base}/${proveedorId}/productos`, peticion);
  }

  desasociarProducto(proveedorId: number, productoId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${proveedorId}/productos/${productoId}`);
  }
}
