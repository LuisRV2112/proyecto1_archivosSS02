import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-url';
import { Venta, VentaDetallada, VentaPeticion } from '../core/modelos/venta.model';

@Injectable({ providedIn: 'root' })
export class VentasServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/ventas`;

  listar(clienteId?: number, desde?: string, hasta?: string): Observable<Venta[]> {
    let params = new HttpParams();
    if (clienteId) {
      params = params.set('clienteId', String(clienteId));
    }
    if (desde) {
      params = params.set('desde', desde);
    }
    if (hasta) {
      params = params.set('hasta', hasta);
    }
    return this.http.get<Venta[]>(this.base, { params });
  }

  obtener(id: number): Observable<VentaDetallada> {
    return this.http.get<VentaDetallada>(`${this.base}/${id}`);
  }

  registrar(peticion: VentaPeticion): Observable<VentaDetallada> {
    return this.http.post<VentaDetallada>(this.base, peticion);
  }

  descargarFactura(id: number, numeroFactura: number): Observable<void> {
    return new Observable<void>((observer) => {
      this.http.get(`${this.base}/${id}/factura`, { responseType: 'blob' }).subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const enlace = document.createElement('a');
          enlace.href = url;
          enlace.download = `factura-${numeroFactura}.pdf`;
          enlace.click();
          window.URL.revokeObjectURL(url);
          observer.next();
          observer.complete();
        },
        error: (err) => observer.error(err),
      });
    });
  }
}
