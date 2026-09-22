export interface Existencia {
  productoId: number;
  codigo: string;
  nombre: string;
  categoria: string;
  precioVenta: number;
  stockMinimo: number;
  activo: boolean;
  existencia: number;
  valorInventario: number;
}

export interface AlertaStock {
  productoId: number;
  codigo: string;
  nombre: string;
  categoria: string;
  existencia: number;
  stockMinimo: number;
  faltante: number;
  nivel: 'AGOTADO' | 'BAJO_MINIMO';
}

export interface Kardex {
  id: number;
  fecha: string;
  tipo: 'ENTRADA' | 'SALIDA';
  origen: 'COMPRA' | 'VENTA' | 'AJUSTE';
  referenciaId: number | null;
  loteId: number | null;
  cantidad: number;
  costoUnitario: number;
  existenciaResultante: number;
  usuarioUsername: string;
}
