// Los 4 roles tal como los devuelve el backend (rol.nombre, sin prefijo ROLE_)
export type Rol = 'ADMINISTRACION' | 'COMPRAS' | 'INVENTARIO' | 'VENTAS';

export interface LoginRespuesta {
  token: string;
  username: string;
  nombreCompleto: string;
  rol: Rol;
}
