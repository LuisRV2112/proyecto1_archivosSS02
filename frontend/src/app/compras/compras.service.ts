import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';
import { Compra, CompraDetallada, CompraPeticion } from '../core/modelos/compra.model';

@Injectable({ providedIn: 'root' })
export class ComprasServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/compras`;

  listar(proveedorId?: number, desde?: string, hasta?: string): Observable<Compra[]> {
    let params = new HttpParams();
    if (proveedorId) {
      params = params.set('proveedorId', String(proveedorId));
    }
    if (desde) {
      params = params.set('desde', desde);
    }
    if (hasta) {
      params = params.set('hasta', hasta);
    }
    return this.http.get<Compra[]>(this.base, { params });
  }

  obtener(id: number): Observable<CompraDetallada> {
    return this.http.get<CompraDetallada>(`${this.base}/${id}`);
  }

  registrar(peticion: CompraPeticion): Observable<CompraDetallada> {
    return this.http.post<CompraDetallada>(this.base, peticion);
  }
}
