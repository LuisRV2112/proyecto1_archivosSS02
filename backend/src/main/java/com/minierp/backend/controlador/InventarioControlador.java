package com.minierp.backend.controlador;

import com.minierp.backend.dto.AlertaStockRespuesta;
import com.minierp.backend.dto.ExistenciaRespuesta;
import com.minierp.backend.dto.KardexRespuesta;
import com.minierp.backend.servicio.InventarioServicio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioControlador {

    private final InventarioServicio inventarioServicio;

    public InventarioControlador(InventarioServicio inventarioServicio) {
        this.inventarioServicio = inventarioServicio;
    }

    @GetMapping("/existencias")
    public List<ExistenciaRespuesta> existencias() {
        return inventarioServicio.existencias();
    }

    @GetMapping("/alertas")
    public List<AlertaStockRespuesta> alertas() {
        return inventarioServicio.alertas();
    }

    @GetMapping("/productos/{id}/kardex")
    public List<KardexRespuesta> kardex(@PathVariable Integer id) {
        return inventarioServicio.kardex(id);
    }
}
