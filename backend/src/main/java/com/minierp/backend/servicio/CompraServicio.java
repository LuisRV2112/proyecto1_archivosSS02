package com.minierp.backend.servicio;

import com.minierp.backend.dto.CompraDetalladaRespuesta;
import com.minierp.backend.dto.CompraPeticion;
import com.minierp.backend.dto.CompraRespuesta;
import com.minierp.backend.dto.DetalleCompraRespuesta;
import com.minierp.backend.dto.LineaCompraPeticion;
import com.minierp.backend.excepcion.RecursoNoEncontradoException;
import com.minierp.backend.excepcion.ReglaNegocioException;
import com.minierp.backend.modelo.Compra;
import com.minierp.backend.modelo.DetalleCompra;
import com.minierp.backend.modelo.Lote;
import com.minierp.backend.modelo.MovimientoInventario;
import com.minierp.backend.modelo.Producto;
import com.minierp.backend.modelo.Proveedor;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.repositorio.CompraRepositorio;
import com.minierp.backend.repositorio.DetalleCompraRepositorio;
import com.minierp.backend.repositorio.LoteRepositorio;
import com.minierp.backend.repositorio.MovimientoInventarioRepositorio;
import com.minierp.backend.repositorio.ProductoProveedorRepositorio;
import com.minierp.backend.repositorio.ProductoRepositorio;
import com.minierp.backend.repositorio.ProveedorRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraServicio {

    private final CompraRepositorio compraRepositorio;
    private final DetalleCompraRepositorio detalleCompraRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final MovimientoInventarioRepositorio movimientoInventarioRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final ProductoProveedorRepositorio productoProveedorRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public CompraServicio(
            CompraRepositorio compraRepositorio,
            DetalleCompraRepositorio detalleCompraRepositorio,
            LoteRepositorio loteRepositorio,
            MovimientoInventarioRepositorio movimientoInventarioRepositorio,
            ProveedorRepositorio proveedorRepositorio,
            ProductoRepositorio productoRepositorio,
            ProductoProveedorRepositorio productoProveedorRepositorio,
            BitacoraServicio bitacoraServicio) {
        this.compraRepositorio = compraRepositorio;
        this.detalleCompraRepositorio = detalleCompraRepositorio;
        this.loteRepositorio = loteRepositorio;
        this.movimientoInventarioRepositorio = movimientoInventarioRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.productoProveedorRepositorio = productoProveedorRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    // Flujo de compra (CLAUDE.md, seccion 5.1), en UNA sola transaccion: si
    // cualquier linea falla (producto no surtido por el proveedor, etc.), no
    // queda nada escrito, ni la compra ni sus lotes ni sus movimientos.
    @Transactional
    public CompraDetalladaRespuesta registrar(CompraPeticion peticion, Usuario actor) {
        Proveedor proveedor = proveedorRepositorio.findById(peticion.proveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado: " + peticion.proveedorId()));

        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setUsuario(actor);
        compra.setFecha(peticion.fecha());
        compra.setNumeroDocumento(peticion.numeroDocumento());
        compra.setObservaciones(peticion.observaciones());
        compra.setTotal(BigDecimal.ZERO); // se recalcula abajo; la columna es NOT NULL
        compraRepositorio.save(compra);

        BigDecimal total = BigDecimal.ZERO;
        for (LineaCompraPeticion linea : peticion.lineas()) {
            total = total.add(registrarLinea(compra, linea, proveedor, actor));
        }

        compra.setTotal(total.setScale(2, RoundingMode.HALF_UP));

        bitacoraServicio.registrar(actor, "COMPRAS", "CREACION", "compra", compra.getId(),
                "Compra registrada a '" + proveedor.getNombre() + "' por Q " + compra.getTotal());

        return obtener(compra.getId());
    }

    // 1. detalle_compra  2. lote (cantidad_inicial = cantidad_disponible = la
    // cantidad comprada)  3. movimiento_inventario ENTRADA. Devuelve el
    // subtotal de la linea para acumular el total de la compra.
    private BigDecimal registrarLinea(Compra compra, LineaCompraPeticion linea, Proveedor proveedor, Usuario actor) {
        Producto producto = productoRepositorio.findById(linea.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + linea.productoId()));

        // Regla 5.3 del CLAUDE.md: una compra solo puede incluir productos que
        // ese proveedor surte
        boolean proveedorSurteProducto = productoProveedorRepositorio
                .existsById_ProductoIdAndId_ProveedorId(producto.getId(), proveedor.getId());
        if (!proveedorSurteProducto) {
            throw new ReglaNegocioException("Producto no surtido por el proveedor",
                    "El proveedor '" + proveedor.getNombre() + "' no surte el producto '" + producto.getCodigo() + "'");
        }

        DetalleCompra detalle = new DetalleCompra();
        detalle.setCompra(compra);
        detalle.setProducto(producto);
        detalle.setCantidad(linea.cantidad());
        detalle.setCostoUnitario(linea.costoUnitario());
        detalleCompraRepositorio.save(detalle);

        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setDetalleCompra(detalle);
        lote.setFechaIngreso(compra.getFecha());
        lote.setCantidadInicial(linea.cantidad());
        lote.setCantidadDisponible(linea.cantidad());
        lote.setCostoUnitario(linea.costoUnitario());
        // saveAndFlush: el SUM de sumarExistenciaPorProducto necesita ver este
        // lote ya escrito para calcular bien la existencia_resultante
        loteRepositorio.saveAndFlush(lote);

        long existenciaResultante = loteRepositorio.sumarExistenciaPorProducto(producto.getId());

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setLote(lote);
        movimiento.setTipo("ENTRADA");
        movimiento.setOrigen("COMPRA");
        movimiento.setReferenciaId(compra.getId());
        movimiento.setCantidad(linea.cantidad());
        movimiento.setCostoUnitario(linea.costoUnitario());
        movimiento.setExistenciaResultante((int) existenciaResultante);
        movimiento.setUsuario(actor);
        movimiento.setFecha(compra.getFecha());
        movimientoInventarioRepositorio.save(movimiento);

        return linea.costoUnitario().multiply(BigDecimal.valueOf(linea.cantidad()));
    }

    @Transactional(readOnly = true)
    public List<CompraRespuesta> listar(Integer proveedorId, LocalDateTime desde, LocalDateTime hasta) {
        return compraRepositorio.buscar(proveedorId, desde, hasta).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public CompraDetalladaRespuesta obtener(Integer id) {
        Compra compra = compraRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Compra no encontrada: " + id));

        List<DetalleCompraRespuesta> detalles = detalleCompraRepositorio.findByCompraId(id).stream()
                .map(this::aRespuestaDetalle)
                .toList();

        return new CompraDetalladaRespuesta(aRespuesta(compra), detalles);
    }

    private CompraRespuesta aRespuesta(Compra compra) {
        return new CompraRespuesta(
                compra.getId(),
                compra.getProveedor().getId(),
                compra.getProveedor().getNombre(),
                compra.getUsuario().getUsername(),
                compra.getFecha(),
                compra.getNumeroDocumento(),
                compra.getTotal(),
                compra.getObservaciones());
    }

    private DetalleCompraRespuesta aRespuestaDetalle(DetalleCompra detalle) {
        // No se lee detalle.getSubtotal(): es GENERATED ALWAYS en la base y,
        // dentro de la misma transaccion en que se acaba de insertar, Hibernate
        // no la vuelve a leer (cache de primer nivel). Se calcula igual que la
        // base: cantidad * costo_unitario.
        BigDecimal subtotal = detalle.getCostoUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
        return new DetalleCompraRespuesta(
                detalle.getId(),
                detalle.getProducto().getId(),
                detalle.getProducto().getCodigo(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getCostoUnitario(),
                subtotal);
    }
}
