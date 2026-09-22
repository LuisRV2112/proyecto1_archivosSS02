export interface Categoria {
  id: number;
  nombre: string;
  descripcion: string | null;
  activo: boolean;
}

export interface CategoriaPeticion {
  nombre: string;
  descripcion: string | null;
}

export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  categoriaId: number;
  categoriaNombre: string;
  precioVenta: number;
  stockMinimo: number;
  activo: boolean;
  existencia: number;
}

export interface ProductoPeticion {
  codigo: string;
  nombre: string;
  descripcion: string | null;
  categoriaId: number;
  precioVenta: number;
  stockMinimo: number;
}

export interface Proveedor {
  id: number;
  nit: string;
  nombre: string;
  telefono: string | null;
  correo: string | null;
  direccion: string | null;
  activo: boolean;
}

export interface ProveedorPeticion {
  nit: string;
  nombre: string;
  telefono: string | null;
  correo: string | null;
  direccion: string | null;
}

export interface ProductoProveedor {
  productoId: number;
  productoCodigo: string;
  productoNombre: string;
  costoReferencia: number;
}

export interface ProductoProveedorPeticion {
  productoId: number;
  costoReferencia: number;
}

export interface Cliente {
  id: number;
  nit: string;
  nombre: string;
  telefono: string | null;
  correo: string | null;
  direccion: string | null;
  activo: boolean;
}

export interface ClientePeticion {
  nit: string;
  nombre: string;
  telefono: string | null;
  correo: string | null;
  direccion: string | null;
}
