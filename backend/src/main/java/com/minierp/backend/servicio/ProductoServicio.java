package com.minierp.backend.servicio;

import com.minierp.backend.dto.ProductoPeticion;
import com.minierp.backend.dto.ProductoRespuesta;
import com.minierp.backend.excepcion.RecursoDuplicadoException;
import com.minierp.backend.excepcion.RecursoNoEncontradoException;
import com.minierp.backend.modelo.Categoria;
import com.minierp.backend.modelo.Producto;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.modelo.VExistencia;
import com.minierp.backend.repositorio.CategoriaRepositorio;
import com.minierp.backend.repositorio.ProductoRepositorio;
import com.minierp.backend.repositorio.VExistenciaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductoServicio {

    private final ProductoRepositorio productoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final VExistenciaRepositorio vExistenciaRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public ProductoServicio(
            ProductoRepositorio productoRepositorio,
            CategoriaRepositorio categoriaRepositorio,
            VExistenciaRepositorio vExistenciaRepositorio,
            BitacoraServicio bitacoraServicio) {
        this.productoRepositorio = productoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.vExistenciaRepositorio = vExistenciaRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    @Transactional(readOnly = true)
    public List<ProductoRespuesta> listar(String buscar, Boolean activo) {
        List<Producto> productos = productoRepositorio.buscar(vacioANull(buscar), activo);
        Map<Integer, Long> existencias = existenciasPorId(productos);
        return productos.stream()
                .map(p -> aRespuesta(p, existencias.getOrDefault(p.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoRespuesta obtener(Integer id) {
        Producto producto = obtenerEntidad(id);
        return aRespuesta(producto, existenciaDe(id));
    }

    @Transactional
    public ProductoRespuesta crear(ProductoPeticion peticion, Usuario actor) {
        if (productoRepositorio.existsByCodigo(peticion.codigo())) {
            throw new RecursoDuplicadoException("Ya existe un producto con el codigo '" + peticion.codigo() + "'");
        }
        Categoria categoria = obtenerCategoria(peticion.categoriaId());

        Producto producto = new Producto();
        producto.setCodigo(peticion.codigo());
        producto.setNombre(peticion.nombre());
        producto.setDescripcion(peticion.descripcion());
        producto.setCategoria(categoria);
        producto.setPrecioVenta(peticion.precioVenta());
        producto.setStockMinimo(peticion.stockMinimo());
        producto.setActivo(true);
        producto.setCreadoEn(LocalDateTime.now());
        productoRepositorio.save(producto);

        bitacoraServicio.registrar(actor, "PRODUCTOS", "CREACION", "producto", producto.getId(),
                "Producto creado: " + producto.getCodigo() + " - " + producto.getNombre());

        return aRespuesta(producto, 0L);
    }

    @Transactional
    public ProductoRespuesta actualizar(Integer id, ProductoPeticion peticion, Usuario actor) {
        Producto producto = obtenerEntidad(id);
        if (productoRepositorio.existsByCodigoAndIdNot(peticion.codigo(), id)) {
            throw new RecursoDuplicadoException("Ya existe un producto con el codigo '" + peticion.codigo() + "'");
        }
        Categoria categoria = obtenerCategoria(peticion.categoriaId());

        producto.setCodigo(peticion.codigo());
        producto.setNombre(peticion.nombre());
        producto.setDescripcion(peticion.descripcion());
        producto.setCategoria(categoria);
        producto.setPrecioVenta(peticion.precioVenta());
        producto.setStockMinimo(peticion.stockMinimo());

        bitacoraServicio.registrar(actor, "PRODUCTOS", "ACTUALIZACION", "producto", producto.getId(),
                "Producto actualizado: " + producto.getCodigo());
        return aRespuesta(producto, existenciaDe(id));
    }

    @Transactional
    public ProductoRespuesta activar(Integer id, Usuario actor) {
        return cambiarEstado(id, true, actor);
    }

    @Transactional
    public ProductoRespuesta desactivar(Integer id, Usuario actor) {
        return cambiarEstado(id, false, actor);
    }

    private ProductoRespuesta cambiarEstado(Integer id, boolean activo, Usuario actor) {

        Producto producto = obtenerEntidad(id);
        producto.setActivo(activo);

        bitacoraServicio.registrar(actor, "PRODUCTOS", activo ? "ACTIVACION" : "DESACTIVACION",
                "producto", producto.getId(),
                "Producto " + (activo ? "activado" : "desactivado") + ": " + producto.getCodigo());
        return aRespuesta(producto, existenciaDe(id));
    }

    private Producto obtenerEntidad(Integer id) {
        return productoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
    }

    private Categoria obtenerCategoria(Integer categoriaId) {
        return categoriaRepositorio.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada: " + categoriaId));
    }

    private long existenciaDe(Integer productoId) {
        return vExistenciaRepositorio.findById(productoId).map(VExistencia::getExistencia).orElse(0L);
    }

    private Map<Integer, Long> existenciasPorId(List<Producto> productos) {
        List<Integer> ids = productos.stream().map(Producto::getId).toList();
        return vExistenciaRepositorio.findAllById(ids).stream()
                .collect(Collectors.toMap(VExistencia::getProductoId, VExistencia::getExistencia));
    }

    private ProductoRespuesta aRespuesta(Producto producto, long existencia) {
        return new ProductoRespuesta(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getPrecioVenta(),
                producto.getStockMinimo(),
                producto.isActivo(),
                existencia);
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto;
    }
}
