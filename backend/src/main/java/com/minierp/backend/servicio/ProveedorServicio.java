package com.minierp.backend.servicio;

import com.minierp.backend.dto.ProductoProveedorPeticion;
import com.minierp.backend.dto.ProductoProveedorRespuesta;
import com.minierp.backend.dto.ProveedorPeticion;
import com.minierp.backend.dto.ProveedorRespuesta;
import com.minierp.backend.excepcion.RecursoDuplicadoException;
import com.minierp.backend.excepcion.RecursoNoEncontradoException;
import com.minierp.backend.modelo.Producto;
import com.minierp.backend.modelo.ProductoProveedor;
import com.minierp.backend.modelo.ProductoProveedorId;
import com.minierp.backend.modelo.Proveedor;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.repositorio.ProductoProveedorRepositorio;
import com.minierp.backend.repositorio.ProductoRepositorio;
import com.minierp.backend.repositorio.ProveedorRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProveedorServicio {

    private final ProveedorRepositorio proveedorRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final ProductoProveedorRepositorio productoProveedorRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public ProveedorServicio(
            ProveedorRepositorio proveedorRepositorio,
            ProductoRepositorio productoRepositorio,
            ProductoProveedorRepositorio productoProveedorRepositorio,
            BitacoraServicio bitacoraServicio) {
        this.proveedorRepositorio = proveedorRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.productoProveedorRepositorio = productoProveedorRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    @Transactional(readOnly = true)
    public List<ProveedorRespuesta> listar(String buscar, Boolean activo) {
        return proveedorRepositorio.buscar(vacioANull(buscar), activo).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ProveedorRespuesta obtener(Integer id) {
        return aRespuesta(obtenerEntidad(id));
    }

    @Transactional
    public ProveedorRespuesta crear(ProveedorPeticion peticion, Usuario actor) {
        if (proveedorRepositorio.existsByNit(peticion.nit())) {
            throw new RecursoDuplicadoException("Ya existe un proveedor con el NIT '" + peticion.nit() + "'");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNit(peticion.nit());
        proveedor.setNombre(peticion.nombre());
        proveedor.setTelefono(peticion.telefono());
        proveedor.setCorreo(peticion.correo());
        proveedor.setDireccion(peticion.direccion());
        proveedor.setActivo(true);
        proveedor.setCreadoEn(LocalDateTime.now());
        proveedorRepositorio.save(proveedor);

        bitacoraServicio.registrar(actor, "PROVEEDORES", "CREACION", "proveedor", proveedor.getId(),
                "Proveedor creado: " + proveedor.getNombre());
        return aRespuesta(proveedor);
    }

    @Transactional
    public ProveedorRespuesta actualizar(Integer id, ProveedorPeticion peticion, Usuario actor) {
        Proveedor proveedor = obtenerEntidad(id);
        if (proveedorRepositorio.existsByNitAndIdNot(peticion.nit(), id)) {
            throw new RecursoDuplicadoException("Ya existe un proveedor con el NIT '" + peticion.nit() + "'");
        }

        proveedor.setNit(peticion.nit());
        proveedor.setNombre(peticion.nombre());
        proveedor.setTelefono(peticion.telefono());
        proveedor.setCorreo(peticion.correo());
        proveedor.setDireccion(peticion.direccion());

        bitacoraServicio.registrar(actor, "PROVEEDORES", "ACTUALIZACION", "proveedor", proveedor.getId(),
                "Proveedor actualizado: " + proveedor.getNombre());
        return aRespuesta(proveedor);
    }

    @Transactional
    public ProveedorRespuesta activar(Integer id, Usuario actor) {
        return cambiarEstado(id, true, actor);
    }

    @Transactional
    public ProveedorRespuesta desactivar(Integer id, Usuario actor) {
        return cambiarEstado(id, false, actor);
    }

    @Transactional(readOnly = true)
    public List<ProductoProveedorRespuesta> listarProductos(Integer proveedorId) {
        obtenerEntidad(proveedorId);
        return productoProveedorRepositorio.findById_ProveedorId(proveedorId).stream()
                .map(this::aRespuestaAsociacion)
                .toList();
    }

    @Transactional
    public ProductoProveedorRespuesta asociarProducto(Integer proveedorId, ProductoProveedorPeticion peticion, Usuario actor) {
        Proveedor proveedor = obtenerEntidad(proveedorId);
        Producto producto = productoRepositorio.findById(peticion.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + peticion.productoId()));

        ProductoProveedorId id = new ProductoProveedorId(producto.getId(), proveedor.getId());
        if (productoProveedorRepositorio.existsById(id)) {
            throw new RecursoDuplicadoException(
                    "El proveedor '" + proveedor.getNombre() + "' ya surte el producto '" + producto.getCodigo() + "'");
        }

        ProductoProveedor asociacion = new ProductoProveedor();
        asociacion.setId(id);
        asociacion.setProducto(producto);
        asociacion.setProveedor(proveedor);
        asociacion.setCostoReferencia(peticion.costoReferencia());
        productoProveedorRepositorio.save(asociacion);

        bitacoraServicio.registrar(actor, "PROVEEDORES", "ASOCIAR_PRODUCTO", "producto_proveedor", producto.getId(),
                "Proveedor '" + proveedor.getNombre() + "' ahora surte '" + producto.getCodigo() + "'");
        return aRespuestaAsociacion(asociacion);
    }

    @Transactional
    public void desasociarProducto(Integer proveedorId, Integer productoId, Usuario actor) {
        Proveedor proveedor = obtenerEntidad(proveedorId);
        ProductoProveedorId id = new ProductoProveedorId(productoId, proveedorId);
        if (!productoProveedorRepositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("El proveedor no surte el producto " + productoId);
        }
        productoProveedorRepositorio.deleteById(id);

        bitacoraServicio.registrar(actor, "PROVEEDORES", "DESASOCIAR_PRODUCTO", "producto_proveedor", productoId,
                "Proveedor '" + proveedor.getNombre() + "' ya no surte el producto " + productoId);
    }

    private Proveedor obtenerEntidad(Integer id) {
        return proveedorRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado: " + id));
    }

    private ProveedorRespuesta cambiarEstado(Integer id, boolean activo, Usuario actor) {
        Proveedor proveedor = obtenerEntidad(id);
        proveedor.setActivo(activo);

        bitacoraServicio.registrar(actor, "PROVEEDORES", activo ? "ACTIVACION" : "DESACTIVACION",
                "proveedor", proveedor.getId(),
                "Proveedor " + (activo ? "activado" : "desactivado") + ": " + proveedor.getNombre());
        return aRespuesta(proveedor);
    }

    private ProveedorRespuesta aRespuesta(Proveedor proveedor) {
        return new ProveedorRespuesta(proveedor.getId(), proveedor.getNit(), proveedor.getNombre(),
                proveedor.getTelefono(), proveedor.getCorreo(), proveedor.getDireccion(), proveedor.isActivo());
    }

    private ProductoProveedorRespuesta aRespuestaAsociacion(ProductoProveedor asociacion) {
        Producto producto = asociacion.getProducto();
        return new ProductoProveedorRespuesta(producto.getId(), producto.getCodigo(), producto.getNombre(),
                asociacion.getCostoReferencia());
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto;
    }
}
