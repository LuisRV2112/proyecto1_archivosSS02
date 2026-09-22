package com.minierp.backend.servicio;

import com.minierp.backend.dto.DetalleVentaRespuesta;
import com.minierp.backend.dto.LineaVentaPeticion;
import com.minierp.backend.dto.LoteConsumidoRespuesta;
import com.minierp.backend.dto.VentaDetalladaRespuesta;
import com.minierp.backend.dto.VentaPeticion;
import com.minierp.backend.dto.VentaRespuesta;
import com.minierp.backend.excepcion.RecursoNoEncontradoException;
import com.minierp.backend.excepcion.ReglaNegocioException;
import com.minierp.backend.modelo.Cliente;
import com.minierp.backend.modelo.DetalleVenta;
import com.minierp.backend.modelo.DetalleVentaLote;
import com.minierp.backend.modelo.Lote;
import com.minierp.backend.modelo.MovimientoInventario;
import com.minierp.backend.modelo.Producto;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.modelo.Venta;
import com.minierp.backend.repositorio.ClienteRepositorio;
import com.minierp.backend.repositorio.DetalleVentaLoteRepositorio;
import com.minierp.backend.repositorio.DetalleVentaRepositorio;
import com.minierp.backend.repositorio.LoteRepositorio;
import com.minierp.backend.repositorio.MovimientoInventarioRepositorio;
import com.minierp.backend.repositorio.ProductoRepositorio;
import com.minierp.backend.repositorio.VentaRepositorio;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaServicio {

    private static final BigDecimal TASA_IVA = new BigDecimal("0.12");

    private final VentaRepositorio ventaRepositorio;
    private final DetalleVentaRepositorio detalleVentaRepositorio;
    private final DetalleVentaLoteRepositorio detalleVentaLoteRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final MovimientoInventarioRepositorio movimientoInventarioRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final BitacoraServicio bitacoraServicio;
    private final EntityManager entityManager;

    public VentaServicio(
            VentaRepositorio ventaRepositorio,
            DetalleVentaRepositorio detalleVentaRepositorio,
            DetalleVentaLoteRepositorio detalleVentaLoteRepositorio,
            LoteRepositorio loteRepositorio,
            MovimientoInventarioRepositorio movimientoInventarioRepositorio,
            ClienteRepositorio clienteRepositorio,
            ProductoRepositorio productoRepositorio,
            BitacoraServicio bitacoraServicio,
            EntityManager entityManager) {
        this.ventaRepositorio = ventaRepositorio;
        this.detalleVentaRepositorio = detalleVentaRepositorio;
        this.detalleVentaLoteRepositorio = detalleVentaLoteRepositorio;
        this.loteRepositorio = loteRepositorio;
        this.movimientoInventarioRepositorio = movimientoInventarioRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.bitacoraServicio = bitacoraServicio;
        this.entityManager = entityManager;
    }

    @Transactional
    public VentaDetalladaRespuesta registrar(VentaPeticion peticion, Usuario actor) {
        Cliente cliente = clienteRepositorio.findById(peticion.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + peticion.clienteId()));
        if (!cliente.isActivo()) {
            throw new ReglaNegocioException("Cliente inactivo",
                    "No se puede vender a un cliente inactivo: '" + cliente.getNombre() + "'");
        }

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(actor);
        venta.setFecha(LocalDateTime.now());

        venta.setSubtotal(BigDecimal.ZERO);
        venta.setIva(BigDecimal.ZERO);
        venta.setTotal(BigDecimal.ZERO);
        ventaRepositorio.save(venta);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetalleVentaRespuesta> detallesRespuesta = new ArrayList<>();
        for (LineaVentaPeticion linea : peticion.lineas()) {
            DetalleVentaRespuesta detalleRespuesta = registrarLinea(venta, linea, actor);
            subtotal = subtotal.add(detalleRespuesta.subtotal());
            detallesRespuesta.add(detalleRespuesta);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = subtotal.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva);

        venta.setSubtotal(subtotal);
        venta.setIva(iva);
        venta.setTotal(total);
        ventaRepositorio.saveAndFlush(venta);

        entityManager.refresh(venta);

        bitacoraServicio.registrar(actor, "VENTAS", "CREACION", "venta", venta.getId(),
                "Venta #" + venta.getNumeroFactura() + " registrada a '" + cliente.getNombre() + "' por Q " + total);

        return new VentaDetalladaRespuesta(aRespuesta(venta), detallesRespuesta);
    }

    private DetalleVentaRespuesta registrarLinea(Venta venta, LineaVentaPeticion lineaPeticion, Usuario actor) {
        Producto producto = productoRepositorio.findById(lineaPeticion.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + lineaPeticion.productoId()));
        if (!producto.isActivo()) {
            throw new ReglaNegocioException("Producto inactivo",
                    "No se puede vender el producto inactivo '" + producto.getCodigo() + "'");
        }

        int cantidadSolicitada = lineaPeticion.cantidad();

        List<Lote> lotesDisponibles = loteRepositorio.buscarConExistenciaParaActualizar(producto.getId());

        long existenciaTotal = lotesDisponibles.stream().mapToLong(Lote::getCantidadDisponible).sum();
        if (existenciaTotal < cantidadSolicitada) {

            throw new ReglaNegocioException("Existencia insuficiente",
                    "Producto '" + producto.getCodigo() + "': solicitado " + cantidadSolicitada
                            + ", disponible " + existenciaTotal);
        }

        BigDecimal precioUnitario = producto.getPrecioVenta();

        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidadSolicitada);
        detalle.setPrecioUnitario(precioUnitario);
        detalleVentaRepositorio.save(detalle);

        int pendiente = cantidadSolicitada;
        List<LoteConsumidoRespuesta> lotesConsumidos = new ArrayList<>();
        for (Lote lote : lotesDisponibles) {
            if (pendiente == 0) {
                break;
            }
            int consumidoDeEsteLote = Math.min(pendiente, lote.getCantidadDisponible());
            lote.setCantidadDisponible(lote.getCantidadDisponible() - consumidoDeEsteLote);
            pendiente -= consumidoDeEsteLote;

            loteRepositorio.saveAndFlush(lote);

            DetalleVentaLote consumo = new DetalleVentaLote();
            consumo.setDetalleVenta(detalle);
            consumo.setLote(lote);
            consumo.setCantidad(consumidoDeEsteLote);
            consumo.setCostoUnitario(lote.getCostoUnitario());
            detalleVentaLoteRepositorio.save(consumo);

            long existenciaResultante = loteRepositorio.sumarExistenciaPorProducto(producto.getId());

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setLote(lote);
            movimiento.setTipo("SALIDA");
            movimiento.setOrigen("VENTA");
            movimiento.setReferenciaId(venta.getId());
            movimiento.setCantidad(consumidoDeEsteLote);
            movimiento.setCostoUnitario(lote.getCostoUnitario());
            movimiento.setExistenciaResultante((int) existenciaResultante);
            movimiento.setUsuario(actor);
            movimiento.setFecha(venta.getFecha());
            movimientoInventarioRepositorio.save(movimiento);

            lotesConsumidos.add(new LoteConsumidoRespuesta(
                    lote.getId(), lote.getFechaIngreso(), consumidoDeEsteLote, lote.getCostoUnitario()));
        }

        BigDecimal subtotalLinea = precioUnitario.multiply(BigDecimal.valueOf(cantidadSolicitada));
        return new DetalleVentaRespuesta(
                detalle.getId(), producto.getId(), producto.getCodigo(), producto.getNombre(),
                cantidadSolicitada, precioUnitario, subtotalLinea, lotesConsumidos);
    }

    @Transactional(readOnly = true)
    public List<VentaRespuesta> listar(Integer clienteId, LocalDateTime desde, LocalDateTime hasta) {
        Specification<Venta> especificacion = (raiz, consulta, cb) -> cb.conjunction();

        if (clienteId != null) {
            especificacion = especificacion.and((raiz, consulta, cb) -> cb.equal(raiz.get("cliente").get("id"), clienteId));
        }
        if (desde != null) {
            especificacion = especificacion.and((raiz, consulta, cb) -> cb.greaterThanOrEqualTo(raiz.get("fecha"), desde));
        }
        if (hasta != null) {
            especificacion = especificacion.and((raiz, consulta, cb) -> cb.lessThanOrEqualTo(raiz.get("fecha"), hasta));
        }

        return ventaRepositorio.findAll(especificacion, Sort.by(Sort.Direction.DESC, "fecha"))
                .stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public VentaDetalladaRespuesta obtener(Integer id) {
        Venta venta = obtenerEntidad(id);
        List<DetalleVentaRespuesta> detalles = detalleVentaRepositorio.findByVentaId(id).stream()
                .map(this::aRespuestaDetalle)
                .toList();
        return new VentaDetalladaRespuesta(aRespuesta(venta), detalles);
    }

    Venta obtenerEntidad(Integer id) {
        return ventaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada: " + id));
    }

    private DetalleVentaRespuesta aRespuestaDetalle(DetalleVenta detalle) {
        List<LoteConsumidoRespuesta> lotes = detalleVentaLoteRepositorio.findByDetalleVentaId(detalle.getId()).stream()
                .map(dvl -> new LoteConsumidoRespuesta(
                        dvl.getLote().getId(), dvl.getLote().getFechaIngreso(), dvl.getCantidad(), dvl.getCostoUnitario()))
                .toList();

        return new DetalleVentaRespuesta(
                detalle.getId(), detalle.getProducto().getId(), detalle.getProducto().getCodigo(),
                detalle.getProducto().getNombre(), detalle.getCantidad(), detalle.getPrecioUnitario(),
                detalle.getSubtotal(), lotes);
    }

    private VentaRespuesta aRespuesta(Venta venta) {
        return new VentaRespuesta(
                venta.getId(),
                venta.getNumeroFactura(),
                venta.getCliente().getId(),
                venta.getCliente().getNombre(),
                venta.getCliente().getNit(),
                venta.getUsuario().getUsername(),
                venta.getFecha(),
                venta.getSubtotal(),
                venta.getIva(),
                venta.getTotal());
    }
}
