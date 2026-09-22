package com.minierp.backend.servicio;

import com.minierp.backend.dto.CategoriaPeticion;
import com.minierp.backend.dto.CategoriaRespuesta;
import com.minierp.backend.excepcion.RecursoDuplicadoException;
import com.minierp.backend.excepcion.RecursoNoEncontradoException;
import com.minierp.backend.modelo.Categoria;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.repositorio.CategoriaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaServicio {

    private final CategoriaRepositorio categoriaRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public CategoriaServicio(CategoriaRepositorio categoriaRepositorio, BitacoraServicio bitacoraServicio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    @Transactional(readOnly = true)
    public List<CategoriaRespuesta> listar(String buscar, Boolean activo) {
        return categoriaRepositorio.buscar(vacioANull(buscar), activo).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaRespuesta obtener(Integer id) {
        return aRespuesta(obtenerEntidad(id));
    }

    @Transactional
    public CategoriaRespuesta crear(CategoriaPeticion peticion, Usuario actor) {
        if (categoriaRepositorio.existsByNombre(peticion.nombre())) {
            throw new RecursoDuplicadoException("Ya existe una categoria con el nombre '" + peticion.nombre() + "'");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(peticion.nombre());
        categoria.setDescripcion(peticion.descripcion());
        categoria.setActivo(true);
        categoriaRepositorio.save(categoria);

        bitacoraServicio.registrar(actor, "CATEGORIAS", "CREACION", "categoria", categoria.getId(),
                "Categoria creada: " + categoria.getNombre());
        return aRespuesta(categoria);
    }

    @Transactional
    public CategoriaRespuesta actualizar(Integer id, CategoriaPeticion peticion, Usuario actor) {
        Categoria categoria = obtenerEntidad(id);
        if (categoriaRepositorio.existsByNombreAndIdNot(peticion.nombre(), id)) {
            throw new RecursoDuplicadoException("Ya existe una categoria con el nombre '" + peticion.nombre() + "'");
        }

        categoria.setNombre(peticion.nombre());
        categoria.setDescripcion(peticion.descripcion());

        bitacoraServicio.registrar(actor, "CATEGORIAS", "ACTUALIZACION", "categoria", categoria.getId(),
                "Categoria actualizada: " + categoria.getNombre());
        return aRespuesta(categoria);
    }

    @Transactional
    public CategoriaRespuesta activar(Integer id, Usuario actor) {
        return cambiarEstado(id, true, actor);
    }

    @Transactional
    public CategoriaRespuesta desactivar(Integer id, Usuario actor) {
        return cambiarEstado(id, false, actor);
    }

    private CategoriaRespuesta cambiarEstado(Integer id, boolean activo, Usuario actor) {
        Categoria categoria = obtenerEntidad(id);
        categoria.setActivo(activo);

        bitacoraServicio.registrar(actor, "CATEGORIAS", activo ? "ACTIVACION" : "DESACTIVACION",
                "categoria", categoria.getId(),
                "Categoria " + (activo ? "activada" : "desactivada") + ": " + categoria.getNombre());
        return aRespuesta(categoria);
    }

    private Categoria obtenerEntidad(Integer id) {
        return categoriaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada: " + id));
    }

    private CategoriaRespuesta aRespuesta(Categoria categoria) {
        return new CategoriaRespuesta(categoria.getId(), categoria.getNombre(), categoria.getDescripcion(), categoria.isActivo());
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto;
    }
}
