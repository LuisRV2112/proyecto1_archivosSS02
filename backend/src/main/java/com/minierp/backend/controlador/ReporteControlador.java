package com.minierp.backend.controlador;

import com.minierp.backend.dto.CompraRespuesta;
import com.minierp.backend.dto.ExistenciaRespuesta;
import com.minierp.backend.dto.KardexRespuesta;
import com.minierp.backend.dto.ReporteClienteTopRespuesta;
import com.minierp.backend.dto.ReporteProductoFrecuenciaRespuesta;
import com.minierp.backend.dto.ReporteProductoIngresoRespuesta;
import com.minierp.backend.dto.ReporteProductoMovimientosRespuesta;
import com.minierp.backend.dto.ReporteProductoVendidoRespuesta;
import com.minierp.backend.dto.ReporteProveedorTopRespuesta;
import com.minierp.backend.dto.ReporteResumenPeriodoRespuesta;
import com.minierp.backend.dto.VentaRespuesta;
import com.minierp.backend.servicio.ReporteServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

// Los 12 reportes de la seccion 7 del CLAUDE.md. Las rutas siguen el mismo
// agrupamiento que la tabla de permisos de la seccion 6: productos/inventario
// (INVENTARIO), compras/proveedores (COMPRAS), ventas/clientes (VENTAS) - ya
// configurado en SecurityConfig desde la Fase 2.
@RestController
@RequestMapping("/api/reportes")
public class ReporteControlador {

    private final ReporteServicio reporteServicio;

    public ReporteControlador(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    // 1. Top 10 productos mas vendidos
    @GetMapping("/productos/mas-vendidos")
    public List<ReporteProductoVendidoRespuesta> productosMasVendidos() {
        return reporteServicio.productosMasVendidos();
    }

    // 2. Productos con menor existencia disponible
    @GetMapping("/productos/menor-existencia")
    public List<ExistenciaRespuesta> productosMenorExistencia() {
        return reporteServicio.productosMenorExistencia();
    }

    // 3. Productos con mayor cantidad de movimientos
    @GetMapping("/productos/mas-movimientos")
    public List<ReporteProductoMovimientosRespuesta> productosMasMovimientos() {
        return reporteServicio.productosMasMovimientos();
    }

    // 4. Kardex de un producto seleccionado
    @GetMapping("/inventario/productos/{id}/kardex")
    public List<KardexRespuesta> kardexProducto(@PathVariable Integer id) {
        return reporteServicio.kardexProducto(id);
    }

    // 5. Compras dentro de un rango de fechas
    @GetMapping("/compras")
    public List<CompraRespuesta> comprasEnRango(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return reporteServicio.comprasEnRango(desde, hasta);
    }

    // 6. Top 5 proveedores con mayor monto acumulado de compras
    @GetMapping("/proveedores/mas-compras")
    public List<ReporteProveedorTopRespuesta> proveedoresTop() {
        return reporteServicio.proveedoresTop();
    }

    // 7. Productos adquiridos con mayor frecuencia
    @GetMapping("/compras/productos-frecuentes")
    public List<ReporteProductoFrecuenciaRespuesta> productosMasFrecuentesEnCompras() {
        return reporteServicio.productosMasFrecuentesEnCompras();
    }

    // 8. Ventas dentro de un rango de fechas
    @GetMapping("/ventas")
    public List<VentaRespuesta> ventasEnRango(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return reporteServicio.ventasEnRango(desde, hasta);
    }

    // 9. Top 10 clientes con mayor monto acumulado de compras
    @GetMapping("/clientes/mas-compras")
    public List<ReporteClienteTopRespuesta> clientesTop() {
        return reporteServicio.clientesTop();
    }

    // 10. Top 10 productos que han generado mayores ingresos
    @GetMapping("/ventas/productos-mas-ingresos")
    public List<ReporteProductoIngresoRespuesta> productosMayorIngreso() {
        return reporteServicio.productosMayorIngreso();
    }

    // 11. Resumen de ventas por periodo: dia, semana, mes o anio
    @GetMapping("/ventas/resumen-periodo")
    public List<ReporteResumenPeriodoRespuesta> resumenVentasPorPeriodo(@RequestParam String periodo) {
        return reporteServicio.resumenVentasPorPeriodo(periodo);
    }
}
