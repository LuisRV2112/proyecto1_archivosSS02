import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';
import { AlertaStock, Existencia, Kardex } from '../core/modelos/inventario.model';

@Injectable({ providedIn: 'root' })
export class InventarioServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/inventario`;

  existencias(): Observable<Existencia[]> {
    return this.http.get<Existencia[]>(`${this.base}/existencias`);
  }

  alertas(): Observable<AlertaStock[]> {
    return this.http.get<AlertaStock[]>(`${this.base}/alertas`);
  }

  kardex(productoId: number): Observable<Kardex[]> {
    return this.http.get<Kardex[]>(`${this.base}/productos/${productoId}/kardex`);
  }
}
