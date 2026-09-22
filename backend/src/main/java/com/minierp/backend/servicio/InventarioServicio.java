package com.minierp.backend.servicio;

import com.minierp.backend.dto.AlertaStockRespuesta;
import com.minierp.backend.dto.ExistenciaRespuesta;
import com.minierp.backend.dto.KardexRespuesta;
import com.minierp.backend.modelo.MovimientoInventario;
import com.minierp.backend.modelo.VAlertaStock;
import com.minierp.backend.modelo.VExistencia;
import com.minierp.backend.repositorio.MovimientoInventarioRepositorio;
import com.minierp.backend.repositorio.ProductoRepositorio;
import com.minierp.backend.repositorio.VAlertaStockRepositorio;
import com.minierp.backend.repositorio.VExistenciaRepositorio;
import com.minierp.backend.excepcion.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;

@Service
public class InventarioServicio {

    private final VExistenciaRepositorio vExistenciaRepositorio;
    private final VAlertaStockRepositorio vAlertaStockRepositorio;
    private final MovimientoInventarioRepositorio movimientoInventarioRepositorio;
    private final ProductoRepositorio productoRepositorio;

    public InventarioServicio(
            VExistenciaRepositorio vExistenciaRepositorio,
            VAlertaStockRepositorio vAlertaStockRepositorio,
            MovimientoInventarioRepositorio movimientoInventarioRepositorio,
            ProductoRepositorio productoRepositorio) {
        this.vExistenciaRepositorio = vExistenciaRepositorio;
        this.vAlertaStockRepositorio = vAlertaStockRepositorio;
        this.movimientoInventarioRepositorio = movimientoInventarioRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<ExistenciaRespuesta> existencias() {
        return vExistenciaRepositorio.findAll().stream()
                .sorted(Comparator.comparing(VExistencia::getNombre))
                .map(v -> new ExistenciaRespuesta(
                        v.getProductoId(), v.getCodigo(), v.getNombre(), v.getCategoria(),
                        v.getPrecioVenta(), v.getStockMinimo(), v.isActivo(), v.getExistencia(), v.getValorInventario()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaStockRespuesta> alertas() {
        return vAlertaStockRepositorio.findAll().stream()
                .sorted(Comparator.comparing(VAlertaStock::getExistencia))
                .map(a -> new AlertaStockRespuesta(
                        a.getProductoId(), a.getCodigo(), a.getNombre(), a.getCategoria(),
                        a.getExistencia(), a.getStockMinimo(), a.getFaltante(), a.getNivel()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KardexRespuesta> kardex(Integer productoId) {
        if (!productoRepositorio.existsById(productoId)) {
            throw new RecursoNoEncontradoException("Producto no encontrado: " + productoId);
        }
        return movimientoInventarioRepositorio.findByProductoIdOrderByFechaAscIdAsc(productoId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    private KardexRespuesta aRespuesta(MovimientoInventario movimiento) {
        return new KardexRespuesta(
                movimiento.getId(),
                movimiento.getFecha(),
                movimiento.getTipo(),
                movimiento.getOrigen(),
                movimiento.getReferenciaId(),
                movimiento.getLote() == null ? null : movimiento.getLote().getId(),
                movimiento.getCantidad(),
                movimiento.getCostoUnitario(),
                movimiento.getExistenciaResultante(),
                movimiento.getUsuario().getUsername());
    }
}
