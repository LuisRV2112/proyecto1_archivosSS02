
export type Rol = 'ADMINISTRACION' | 'COMPRAS' | 'INVENTARIO' | 'VENTAS';

export interface LoginRespuesta {
  token: string;
  username: string;
  nombreCompleto: string;
  rol: Rol;
}
