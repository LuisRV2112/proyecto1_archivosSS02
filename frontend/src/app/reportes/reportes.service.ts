import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';

export type FilaReporte = Record<string, string | number | boolean | null>;

@Injectable({ providedIn: 'root' })
export class ReportesServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/reportes`;

  productosMasVendidos(): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/productos/mas-vendidos`);
  }

  productosMenorExistencia(): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/productos/menor-existencia`);
  }

  productosMasMovimientos(): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/productos/mas-movimientos`);
  }

  comprasEnRango(desde?: string, hasta?: string): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/compras`, { params: this.parametrosFecha(desde, hasta) });
  }

  proveedoresTop(): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/proveedores/mas-compras`);
  }

  productosFrecuentesCompras(): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/compras/productos-frecuentes`);
  }

  ventasEnRango(desde?: string, hasta?: string): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/ventas`, { params: this.parametrosFecha(desde, hasta) });
  }

  clientesTop(): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/clientes/mas-compras`);
  }

  productosMayorIngreso(): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/ventas/productos-mas-ingresos`);
  }

  resumenPorPeriodo(periodo: string): Observable<FilaReporte[]> {
    return this.http.get<FilaReporte[]>(`${this.base}/ventas/resumen-periodo`, { params: { periodo } });
  }

  private parametrosFecha(desde?: string, hasta?: string): HttpParams {
    let params = new HttpParams();
    if (desde) {
      params = params.set('desde', desde);
    }
    if (hasta) {
      params = params.set('hasta', hasta);
    }
    return params;
  }
}
