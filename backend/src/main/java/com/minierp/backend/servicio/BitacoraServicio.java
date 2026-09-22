package com.minierp.backend.servicio;

import com.minierp.backend.dto.BitacoraRespuesta;
import com.minierp.backend.modelo.Bitacora;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.repositorio.BitacoraRepositorio;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BitacoraServicio {

    private final BitacoraRepositorio bitacoraRepositorio;

    public BitacoraServicio(BitacoraRepositorio bitacoraRepositorio) {
        this.bitacoraRepositorio = bitacoraRepositorio;
    }

    public void registrar(Usuario usuario, String modulo, String accion, String entidad, Integer entidadId, String detalle) {
        Bitacora bitacora = new Bitacora();
        bitacora.setUsuario(usuario);
        bitacora.setModulo(modulo);
        bitacora.setAccion(accion);
        bitacora.setEntidad(entidad);
        bitacora.setEntidadId(entidadId);
        bitacora.setDetalle(detalle);
        bitacora.setFecha(LocalDateTime.now());
        bitacoraRepositorio.save(bitacora);
    }

    @Transactional(readOnly = true)
    public List<BitacoraRespuesta> consultar(Integer usuarioId, String modulo, LocalDateTime desde, LocalDateTime hasta) {
        Specification<Bitacora> especificacion = (raiz, consulta, cb) -> cb.conjunction();

        if (usuarioId != null) {
            especificacion = especificacion.and(
                    (raiz, consulta, cb) -> cb.equal(raiz.get("usuario").get("id"), usuarioId));
        }
        if (modulo != null && !modulo.isBlank()) {
            especificacion = especificacion.and((raiz, consulta, cb) -> cb.equal(raiz.get("modulo"), modulo));
        }
        if (desde != null) {
            especificacion = especificacion.and((raiz, consulta, cb) -> cb.greaterThanOrEqualTo(raiz.get("fecha"), desde));
        }
        if (hasta != null) {
            especificacion = especificacion.and((raiz, consulta, cb) -> cb.lessThanOrEqualTo(raiz.get("fecha"), hasta));
        }

        Sort masRecienteAlPrincipio = Sort.by(Sort.Direction.DESC, "fecha");
        return bitacoraRepositorio.findAll(especificacion, masRecienteAlPrincipio).stream()
                .map(this::aRespuesta)
                .toList();
    }

    private BitacoraRespuesta aRespuesta(Bitacora bitacora) {
        return new BitacoraRespuesta(
                bitacora.getId(),
                bitacora.getUsuario() == null ? null : bitacora.getUsuario().getUsername(),
                bitacora.getModulo(),
                bitacora.getAccion(),
                bitacora.getEntidad(),
                bitacora.getEntidadId(),
                bitacora.getDetalle(),
                bitacora.getFecha());
    }
}
