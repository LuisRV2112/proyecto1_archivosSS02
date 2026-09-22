package com.minierp.backend.dto;

import java.util.List;

public record VentaDetalladaRespuesta(VentaRespuesta venta, List<DetalleVentaRespuesta> detalles) {
}
