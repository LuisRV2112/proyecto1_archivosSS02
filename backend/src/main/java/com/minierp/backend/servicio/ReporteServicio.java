package com.minierp.backend.servicio;

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
import com.minierp.backend.excepcion.ReglaNegocioException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteServicio {

    private final JdbcTemplate jdbcTemplate;

    public ReporteServicio(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReporteProductoVendidoRespuesta> productosMasVendidos() {
        String sql = """
                SELECT p.id AS producto_id, p.codigo, p.nombre, COALESCE(SUM(dv.cantidad), 0) AS unidades
                FROM producto p
                JOIN detalle_venta dv ON dv.producto_id = p.id
                GROUP BY p.id, p.codigo, p.nombre
                ORDER BY unidades DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, (rs, i) -> new ReporteProductoVendidoRespuesta(
                rs.getInt("producto_id"), rs.getString("codigo"), rs.getString("nombre"), rs.getLong("unidades")));
    }

    public List<ExistenciaRespuesta> productosMenorExistencia() {
        String sql = """
                SELECT producto_id, codigo, nombre, categoria, precio_venta, stock_minimo, activo, existencia, valor_inventario
                FROM v_existencia
                ORDER BY existencia ASC
                """;
        return jdbcTemplate.query(sql, (rs, i) -> new ExistenciaRespuesta(
                rs.getInt("producto_id"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("categoria"),
                rs.getBigDecimal("precio_venta"), rs.getInt("stock_minimo"), rs.getBoolean("activo"),
                rs.getLong("existencia"), rs.getBigDecimal("valor_inventario")));
    }

    public List<ReporteProductoMovimientosRespuesta> productosMasMovimientos() {
        String sql = """
                SELECT p.id AS producto_id, p.codigo, p.nombre, COUNT(mi.id) AS cantidad_movimientos
                FROM producto p
                JOIN movimiento_inventario mi ON mi.producto_id = p.id
                GROUP BY p.id, p.codigo, p.nombre
                ORDER BY cantidad_movimientos DESC
                """;
        return jdbcTemplate.query(sql, (rs, i) -> new ReporteProductoMovimientosRespuesta(
                rs.getInt("producto_id"), rs.getString("codigo"), rs.getString("nombre"), rs.getLong("cantidad_movimientos")));
    }

    public List<KardexRespuesta> kardexProducto(Integer productoId) {
        String sql = """
                SELECT mi.id, mi.fecha, mi.tipo, mi.origen, mi.referencia_id, mi.lote_id, mi.cantidad,
                       mi.costo_unitario, mi.existencia_resultante, u.username
                FROM movimiento_inventario mi
                JOIN usuario u ON u.id = mi.usuario_id
                WHERE mi.producto_id = ?
                ORDER BY mi.fecha ASC, mi.id ASC
                """;
        return jdbcTemplate.query(sql, (rs, i) -> new KardexRespuesta(
                rs.getInt("id"), rs.getObject("fecha", LocalDateTime.class), rs.getString("tipo"), rs.getString("origen"),
                (Integer) rs.getObject("referencia_id"), (Integer) rs.getObject("lote_id"), rs.getInt("cantidad"),
                rs.getBigDecimal("costo_unitario"), rs.getInt("existencia_resultante"), rs.getString("username")),
                productoId);
    }

    public List<CompraRespuesta> comprasEnRango(LocalDateTime desde, LocalDateTime hasta) {
        StringBuilder sql = new StringBuilder("""
                SELECT c.id, c.proveedor_id, pr.nombre AS proveedor_nombre, u.username AS usuario_username,
                       c.fecha, c.numero_documento, c.total, c.observaciones
                FROM compra c
                JOIN proveedor pr ON pr.id = c.proveedor_id
                JOIN usuario u ON u.id = c.usuario_id
                WHERE 1=1
                """);
        List<Object> parametros = new ArrayList<>();
        if (desde != null) {
            sql.append(" AND c.fecha >= ?");
            parametros.add(desde);
        }
        if (hasta != null) {
            sql.append(" AND c.fecha <= ?");
            parametros.add(hasta);
        }
        sql.append(" ORDER BY c.fecha DESC");

        return jdbcTemplate.query(sql.toString(), (rs, i) -> new CompraRespuesta(
                rs.getInt("id"), rs.getInt("proveedor_id"), rs.getString("proveedor_nombre"),
                rs.getString("usuario_username"), rs.getObject("fecha", LocalDateTime.class),
                rs.getString("numero_documento"), rs.getBigDecimal("total"), rs.getString("observaciones")),
                parametros.toArray());
    }

    public List<ReporteProveedorTopRespuesta> proveedoresTop() {
        String sql = """
                SELECT p.id AS proveedor_id, p.nombre, p.nit, COALESCE(SUM(c.total), 0) AS monto_acumulado,
                       COUNT(c.id) AS cantidad_compras
                FROM proveedor p
                JOIN compra c ON c.proveedor_id = p.id
                GROUP BY p.id, p.nombre, p.nit
                ORDER BY monto_acumulado DESC
                LIMIT 5
                """;
        return jdbcTemplate.query(sql, (rs, i) -> new ReporteProveedorTopRespuesta(
                rs.getInt("proveedor_id"), rs.getString("nombre"), rs.getString("nit"),
                rs.getBigDecimal("monto_acumulado"), rs.getLong("cantidad_compras")));
    }

    public List<ReporteProductoFrecuenciaRespuesta> productosMasFrecuentesEnCompras() {
        String sql = """
                SELECT p.id AS producto_id, p.codigo, p.nombre, COUNT(dc.id) AS veces_comprado,
                       COALESCE(SUM(dc.cantidad), 0) AS cantidad_total
                FROM producto p
                JOIN detalle_compra dc ON dc.producto_id = p.id
                GROUP BY p.id, p.codigo, p.nombre
                ORDER BY veces_comprado DESC, cantidad_total DESC
                """;
        return jdbcTemplate.query(sql, (rs, i) -> new ReporteProductoFrecuenciaRespuesta(
                rs.getInt("producto_id"), rs.getString("codigo"), rs.getString("nombre"),
                rs.getLong("veces_comprado"), rs.getLong("cantidad_total")));
    }

    public List<VentaRespuesta> ventasEnRango(LocalDateTime desde, LocalDateTime hasta) {
        StringBuilder sql = new StringBuilder("""
                SELECT v.id, v.numero_factura, v.cliente_id, cl.nombre AS cliente_nombre, cl.nit AS cliente_nit,
                       u.username AS usuario_username, v.fecha, v.subtotal, v.iva, v.total
                FROM venta v
                JOIN cliente cl ON cl.id = v.cliente_id
                JOIN usuario u ON u.id = v.usuario_id
                WHERE 1=1
                """);
        List<Object> parametros = new ArrayList<>();
        if (desde != null) {
            sql.append(" AND v.fecha >= ?");
            parametros.add(desde);
        }
        if (hasta != null) {
            sql.append(" AND v.fecha <= ?");
            parametros.add(hasta);
        }
        sql.append(" ORDER BY v.fecha DESC");

        return jdbcTemplate.query(sql.toString(), (rs, i) -> new VentaRespuesta(
                rs.getInt("id"), rs.getInt("numero_factura"), rs.getInt("cliente_id"), rs.getString("cliente_nombre"),
                rs.getString("cliente_nit"), rs.getString("usuario_username"), rs.getObject("fecha", LocalDateTime.class),
                rs.getBigDecimal("subtotal"), rs.getBigDecimal("iva"), rs.getBigDecimal("total")),
                parametros.toArray());
    }

    public List<ReporteClienteTopRespuesta> clientesTop() {
        String sql = """
                SELECT cl.id AS cliente_id, cl.nombre, cl.nit, COALESCE(SUM(v.total), 0) AS monto_acumulado,
                       COUNT(v.id) AS cantidad_compras
                FROM cliente cl
                JOIN venta v ON v.cliente_id = cl.id
                GROUP BY cl.id, cl.nombre, cl.nit
                ORDER BY monto_acumulado DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, (rs, i) -> new ReporteClienteTopRespuesta(
                rs.getInt("cliente_id"), rs.getString("nombre"), rs.getString("nit"),
                rs.getBigDecimal("monto_acumulado"), rs.getLong("cantidad_compras")));
    }

    public List<ReporteProductoIngresoRespuesta> productosMayorIngreso() {
        String sql = """
                SELECT p.id AS producto_id, p.codigo, p.nombre, COALESCE(SUM(dv.subtotal), 0) AS ingreso_total,
                       COALESCE(SUM(dv.cantidad), 0) AS unidades_vendidas
                FROM producto p
                JOIN detalle_venta dv ON dv.producto_id = p.id
                GROUP BY p.id, p.codigo, p.nombre
                ORDER BY ingreso_total DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, (rs, i) -> new ReporteProductoIngresoRespuesta(
                rs.getInt("producto_id"), rs.getString("codigo"), rs.getString("nombre"),
                rs.getBigDecimal("ingreso_total"), rs.getLong("unidades_vendidas")));
    }

    public List<ReporteResumenPeriodoRespuesta> resumenVentasPorPeriodo(String periodo) {

        String claveDateTrunc = switch (periodo) {
            case "dia" -> "day";
            case "semana" -> "week";
            case "mes" -> "month";
            case "anio" -> "year";
            default -> throw new ReglaNegocioException("Periodo invalido",
                    "El periodo debe ser uno de: dia, semana, mes, anio (recibido: '" + periodo + "')");
        };

        String sql = "SELECT date_trunc('" + claveDateTrunc + "', v.fecha) AS periodo, "
                + "COUNT(v.id) AS cantidad_ventas, COALESCE(SUM(v.total), 0) AS monto_total "
                + "FROM venta v GROUP BY periodo ORDER BY periodo";

        return jdbcTemplate.query(sql, (rs, i) -> new ReporteResumenPeriodoRespuesta(
                rs.getObject("periodo", LocalDateTime.class), rs.getLong("cantidad_ventas"), rs.getBigDecimal("monto_total")));
    }
}
