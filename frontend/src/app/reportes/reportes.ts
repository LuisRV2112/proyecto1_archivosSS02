import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FilaReporte, ReportesServicio } from './reportes.service';
import { AutenticacionServicio } from '../core/servicios/autenticacion.service';
import { Rol } from '../core/modelos/usuario.model';
import { mensajeDeError } from '../core/modelos/error.model';

interface DefinicionReporte {
  id: string;
  etiqueta: string;
  roles: Rol[];
  filtroFechas?: boolean;
  filtroPeriodo?: boolean;
}

// Coincide con los 12 reportes de la seccion 7 del CLAUDE.md (el kardex de
// producto -reporte 4- y las compras/ventas por fecha -5 y 8- viven en sus
// propios modulos: Inventario y Compras/Ventas). Los roles replican la
// tabla de permisos de la seccion 6.
const REPORTES: DefinicionReporte[] = [
  { id: 'productos-mas-vendidos', etiqueta: 'Top 10 productos más vendidos', roles: ['ADMINISTRACION', 'INVENTARIO'] },
  {
    id: 'productos-menor-existencia',
    etiqueta: 'Productos con menor existencia',
    roles: ['ADMINISTRACION', 'INVENTARIO'],
  },
  {
    id: 'productos-mas-movimientos',
    etiqueta: 'Productos con más movimientos',
    roles: ['ADMINISTRACION', 'INVENTARIO'],
  },
  {
    id: 'compras-rango',
    etiqueta: 'Compras en un rango de fechas',
    roles: ['ADMINISTRACION', 'COMPRAS'],
    filtroFechas: true,
  },
  { id: 'proveedores-top', etiqueta: 'Top 5 proveedores con mayor monto', roles: ['ADMINISTRACION', 'COMPRAS'] },
  {
    id: 'productos-frecuentes',
    etiqueta: 'Productos más frecuentes en compras',
    roles: ['ADMINISTRACION', 'COMPRAS'],
  },
  {
    id: 'ventas-rango',
    etiqueta: 'Ventas en un rango de fechas',
    roles: ['ADMINISTRACION', 'VENTAS'],
    filtroFechas: true,
  },
  { id: 'clientes-top', etiqueta: 'Top 10 clientes con mayor monto', roles: ['ADMINISTRACION', 'VENTAS'] },
  {
    id: 'productos-mayor-ingreso',
    etiqueta: 'Top 10 productos con mayores ingresos',
    roles: ['ADMINISTRACION', 'VENTAS'],
  },
  {
    id: 'resumen-periodo',
    etiqueta: 'Resumen de ventas por período',
    roles: ['ADMINISTRACION', 'VENTAS'],
    filtroPeriodo: true,
  },
];

@Component({
  selector: 'app-reportes',
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css',
})
export class Reportes {
  private readonly reportesServicio = inject(ReportesServicio);
  private readonly auth = inject(AutenticacionServicio);

  protected readonly reportesDisponibles = computed(() => {
    const rol = this.auth.rol();
    return REPORTES.filter((r) => rol !== null && r.roles.includes(rol));
  });

  protected readonly reporteActivo = signal<DefinicionReporte | null>(null);
  protected readonly filas = signal<FilaReporte[]>([]);
  protected readonly columnas = signal<string[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected desde = '';
  protected hasta = '';
  protected periodo: 'dia' | 'semana' | 'mes' | 'anio' = 'mes';

  protected seleccionar(reporte: DefinicionReporte): void {
    this.reporteActivo.set(reporte);
    this.filas.set([]);
    this.columnas.set([]);
    if (!reporte.filtroFechas) {
      this.ejecutar();
    }
  }

  protected ejecutar(): void {
    const reporte = this.reporteActivo();
    if (!reporte) {
      return;
    }
    this.cargando.set(true);
    this.error.set(null);

    this.observableParaReporte(reporte).subscribe({
      next: (filas) => {
        this.filas.set(filas);
        this.columnas.set(filas.length > 0 ? Object.keys(filas[0]) : []);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }

  protected etiquetaColumna(clave: string): string {
    const conEspacios = clave.replace(/([A-Z])/g, ' $1');
    return conEspacios.charAt(0).toUpperCase() + conEspacios.slice(1);
  }

  private observableParaReporte(reporte: DefinicionReporte) {
    switch (reporte.id) {
      case 'productos-mas-vendidos':
        return this.reportesServicio.productosMasVendidos();
      case 'productos-menor-existencia':
        return this.reportesServicio.productosMenorExistencia();
      case 'productos-mas-movimientos':
        return this.reportesServicio.productosMasMovimientos();
      case 'compras-rango':
        return this.reportesServicio.comprasEnRango(this.aIso(this.desde), this.aIso(this.hasta));
      case 'proveedores-top':
        return this.reportesServicio.proveedoresTop();
      case 'productos-frecuentes':
        return this.reportesServicio.productosFrecuentesCompras();
      case 'ventas-rango':
        return this.reportesServicio.ventasEnRango(this.aIso(this.desde), this.aIso(this.hasta));
      case 'clientes-top':
        return this.reportesServicio.clientesTop();
      case 'productos-mayor-ingreso':
        return this.reportesServicio.productosMayorIngreso();
      case 'resumen-periodo':
        return this.reportesServicio.resumenPorPeriodo(this.periodo);
      default:
        throw new Error('Reporte desconocido: ' + reporte.id);
    }
  }

  private aIso(fecha: string): string | undefined {
    return fecha ? new Date(fecha).toISOString() : undefined;
  }
}
