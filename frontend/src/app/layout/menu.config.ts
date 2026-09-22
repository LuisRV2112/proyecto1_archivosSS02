import { Rol } from '../core/modelos/usuario.model';

export interface ItemMenu {
  etiqueta: string;
  ruta: string;
  roles: Rol[];
}

// Que ve cada rol en el menu lateral: replica exactamente la tabla de
// permisos de la seccion 6 del CLAUDE.md. ADMINISTRACION aparece en todos
// porque "puede ver todo".
export const MENU: ItemMenu[] = [
  { etiqueta: 'Ventas', ruta: '/ventas', roles: ['ADMINISTRACION', 'VENTAS'] },
  { etiqueta: 'Compras', ruta: '/compras', roles: ['ADMINISTRACION', 'COMPRAS'] },
  { etiqueta: 'Inventario', ruta: '/inventario', roles: ['ADMINISTRACION', 'COMPRAS', 'INVENTARIO', 'VENTAS'] },
  { etiqueta: 'Productos', ruta: '/productos', roles: ['ADMINISTRACION', 'INVENTARIO'] },
  { etiqueta: 'Clientes', ruta: '/clientes', roles: ['ADMINISTRACION', 'VENTAS'] },
  { etiqueta: 'Proveedores', ruta: '/proveedores', roles: ['ADMINISTRACION', 'COMPRAS'] },
  { etiqueta: 'Categorías', ruta: '/categorias', roles: ['ADMINISTRACION', 'INVENTARIO'] },
  {
    etiqueta: 'Reportes',
    ruta: '/reportes',
    roles: ['ADMINISTRACION', 'COMPRAS', 'INVENTARIO', 'VENTAS'],
  },
  { etiqueta: 'Bitácora', ruta: '/bitacora', roles: ['ADMINISTRACION'] },
  { etiqueta: 'Usuarios', ruta: '/usuarios', roles: ['ADMINISTRACION'] },
];
