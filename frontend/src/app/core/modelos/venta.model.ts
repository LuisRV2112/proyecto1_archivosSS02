export interface LineaVenta {
  productoId: number;
  cantidad: number;
}

export interface VentaPeticion {
  clienteId: number;
  lineas: LineaVenta[];
}

export interface Venta {
  id: number;
  numeroFactura: number;
  clienteId: number;
  clienteNombre: string;
  clienteNit: string;
  usuarioUsername: string;
  fecha: string;
  subtotal: number;
  iva: number;
  total: number;
}

export interface LoteConsumido {
  loteId: number;
  fechaIngresoLote: string;
  cantidad: number;
  costoUnitario: number;
}

export interface DetalleVenta {
  id: number;
  productoId: number;
  productoCodigo: string;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  lotes: LoteConsumido[];
}

export interface VentaDetallada {
  venta: Venta;
  detalles: DetalleVenta[];
}
