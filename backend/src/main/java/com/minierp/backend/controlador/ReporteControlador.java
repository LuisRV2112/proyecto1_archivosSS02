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

@RestController
@RequestMapping("/api/reportes")
public class ReporteControlador {

    private final ReporteServicio reporteServicio;

    public ReporteControlador(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    @GetMapping("/productos/mas-vendidos")
    public List<ReporteProductoVendidoRespuesta> productosMasVendidos() {
        return reporteServicio.productosMasVendidos();
    }

    @GetMapping("/productos/menor-existencia")
    public List<ExistenciaRespuesta> productosMenorExistencia() {
        return reporteServicio.productosMenorExistencia();
    }

    @GetMapping("/productos/mas-movimientos")
    public List<ReporteProductoMovimientosRespuesta> productosMasMovimientos() {
        return reporteServicio.productosMasMovimientos();
    }

    @GetMapping("/inventario/productos/{id}/kardex")
    public List<KardexRespuesta> kardexProducto(@PathVariable Integer id) {
        return reporteServicio.kardexProducto(id);
    }

    @GetMapping("/compras")
    public List<CompraRespuesta> comprasEnRango(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return reporteServicio.comprasEnRango(desde, hasta);
    }

    @GetMapping("/proveedores/mas-compras")
    public List<ReporteProveedorTopRespuesta> proveedoresTop() {
        return reporteServicio.proveedoresTop();
    }

    @GetMapping("/compras/productos-frecuentes")
    public List<ReporteProductoFrecuenciaRespuesta> productosMasFrecuentesEnCompras() {
        return reporteServicio.productosMasFrecuentesEnCompras();
    }

    @GetMapping("/ventas")
    public List<VentaRespuesta> ventasEnRango(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return reporteServicio.ventasEnRango(desde, hasta);
    }

    @GetMapping("/clientes/mas-compras")
    public List<ReporteClienteTopRespuesta> clientesTop() {
        return reporteServicio.clientesTop();
    }

    @GetMapping("/ventas/productos-mas-ingresos")
    public List<ReporteProductoIngresoRespuesta> productosMayorIngreso() {
        return reporteServicio.productosMayorIngreso();
    }

    @GetMapping("/ventas/resumen-periodo")
    public List<ReporteResumenPeriodoRespuesta> resumenVentasPorPeriodo(@RequestParam String periodo) {
        return reporteServicio.resumenVentasPorPeriodo(periodo);
    }
}
