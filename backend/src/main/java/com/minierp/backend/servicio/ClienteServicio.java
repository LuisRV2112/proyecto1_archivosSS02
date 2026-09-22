package com.minierp.backend.servicio;

import com.minierp.backend.dto.ClientePeticion;
import com.minierp.backend.dto.ClienteRespuesta;
import com.minierp.backend.excepcion.RecursoDuplicadoException;
import com.minierp.backend.excepcion.RecursoNoEncontradoException;
import com.minierp.backend.modelo.Cliente;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.repositorio.ClienteRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteServicio {

    private static final String NIT_CONSUMIDOR_FINAL = "CF";

    private final ClienteRepositorio clienteRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public ClienteServicio(ClienteRepositorio clienteRepositorio, BitacoraServicio bitacoraServicio) {
        this.clienteRepositorio = clienteRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    @Transactional(readOnly = true)
    public List<ClienteRespuesta> listar(String buscar, Boolean activo) {
        return clienteRepositorio.buscar(vacioANull(buscar), activo).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ClienteRespuesta obtener(Integer id) {
        return aRespuesta(obtenerEntidad(id));
    }

    @Transactional
    public ClienteRespuesta crear(ClientePeticion peticion, Usuario actor) {
        validarNitUnico(peticion.nit(), null);

        Cliente cliente = new Cliente();
        cliente.setNit(peticion.nit());
        cliente.setNombre(peticion.nombre());
        cliente.setTelefono(peticion.telefono());
        cliente.setCorreo(peticion.correo());
        cliente.setDireccion(peticion.direccion());
        cliente.setActivo(true);
        cliente.setCreadoEn(LocalDateTime.now());
        clienteRepositorio.save(cliente);

        bitacoraServicio.registrar(actor, "CLIENTES", "CREACION", "cliente", cliente.getId(),
                "Cliente creado: " + cliente.getNombre());
        return aRespuesta(cliente);
    }

    @Transactional
    public ClienteRespuesta actualizar(Integer id, ClientePeticion peticion, Usuario actor) {
        Cliente cliente = obtenerEntidad(id);
        validarNitUnico(peticion.nit(), id);

        cliente.setNit(peticion.nit());
        cliente.setNombre(peticion.nombre());
        cliente.setTelefono(peticion.telefono());
        cliente.setCorreo(peticion.correo());
        cliente.setDireccion(peticion.direccion());

        bitacoraServicio.registrar(actor, "CLIENTES", "ACTUALIZACION", "cliente", cliente.getId(),
                "Cliente actualizado: " + cliente.getNombre());
        return aRespuesta(cliente);
    }

    @Transactional
    public ClienteRespuesta activar(Integer id, Usuario actor) {
        return cambiarEstado(id, true, actor);
    }

    @Transactional
    public ClienteRespuesta desactivar(Integer id, Usuario actor) {
        return cambiarEstado(id, false, actor);
    }

    private ClienteRespuesta cambiarEstado(Integer id, boolean activo, Usuario actor) {
        Cliente cliente = obtenerEntidad(id);
        cliente.setActivo(activo);

        bitacoraServicio.registrar(actor, "CLIENTES", activo ? "ACTIVACION" : "DESACTIVACION",
                "cliente", cliente.getId(),
                "Cliente " + (activo ? "activado" : "desactivado") + ": " + cliente.getNombre());
        return aRespuesta(cliente);
    }

    private void validarNitUnico(String nit, Integer idExcluido) {
        if (NIT_CONSUMIDOR_FINAL.equals(nit)) {
            return;
        }
        boolean duplicado = (idExcluido == null)
                ? clienteRepositorio.existsByNit(nit)
                : clienteRepositorio.existsByNitAndIdNot(nit, idExcluido);
        if (duplicado) {
            throw new RecursoDuplicadoException("Ya existe un cliente con el NIT '" + nit + "'");
        }
    }

    private Cliente obtenerEntidad(Integer id) {
        return clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));
    }

    private ClienteRespuesta aRespuesta(Cliente cliente) {
        return new ClienteRespuesta(cliente.getId(), cliente.getNit(), cliente.getNombre(),
                cliente.getTelefono(), cliente.getCorreo(), cliente.getDireccion(), cliente.isActivo());
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto;
    }
}
