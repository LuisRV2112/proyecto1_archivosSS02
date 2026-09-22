package com.minierp.backend.dto;

import java.util.List;

public record CompraDetalladaRespuesta(CompraRespuesta compra, List<DetalleCompraRespuesta> detalles) {
}
