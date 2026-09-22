export interface LineaCompra {
  productoId: number;
  cantidad: number;
  costoUnitario: number;
}

export interface CompraPeticion {
  proveedorId: number;
  fecha: string;
  numeroDocumento: string | null;
  observaciones: string | null;
  lineas: LineaCompra[];
}

export interface Compra {
  id: number;
  proveedorId: number;
  proveedorNombre: string;
  usuarioUsername: string;
  fecha: string;
  numeroDocumento: string | null;
  total: number;
  observaciones: string | null;
}

export interface DetalleCompra {
  id: number;
  productoId: number;
  productoCodigo: string;
  productoNombre: string;
  cantidad: number;
  costoUnitario: number;
  subtotal: number;
}

export interface CompraDetallada {
  compra: Compra;
  detalles: DetalleCompra[];
}
