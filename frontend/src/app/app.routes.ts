import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Layout } from './layout/layout';
import { Inicio } from './paginas/inicio/inicio';
import { Ventas } from './ventas/ventas';
import { Compras } from './compras/compras';
import { Inventario } from './inventario/inventario';
import { Productos } from './productos/productos';
import { Clientes } from './clientes/clientes';
import { Proveedores } from './proveedores/proveedores';
import { Categorias } from './categorias/categorias';
import { Reportes } from './reportes/reportes';
import { Bitacora } from './bitacora/bitacora';
import { Usuarios } from './usuarios/usuarios';
import { authGuard } from './core/guards/auth.guard';
import { rolGuard } from './core/guards/rol.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: '',
    component: Layout,
    canActivate: [authGuard],
    children: [
      { path: '', component: Inicio },
      { path: 'ventas', component: Ventas, canActivate: [rolGuard(['ADMINISTRACION', 'VENTAS'])] },
      { path: 'compras', component: Compras, canActivate: [rolGuard(['ADMINISTRACION', 'COMPRAS'])] },
      {
        path: 'inventario',
        component: Inventario,
        canActivate: [rolGuard(['ADMINISTRACION', 'COMPRAS', 'INVENTARIO', 'VENTAS'])],
      },
      { path: 'productos', component: Productos, canActivate: [rolGuard(['ADMINISTRACION', 'INVENTARIO'])] },
      { path: 'clientes', component: Clientes, canActivate: [rolGuard(['ADMINISTRACION', 'VENTAS'])] },
      { path: 'proveedores', component: Proveedores, canActivate: [rolGuard(['ADMINISTRACION', 'COMPRAS'])] },
      { path: 'categorias', component: Categorias, canActivate: [rolGuard(['ADMINISTRACION', 'INVENTARIO'])] },
      {
        path: 'reportes',
        component: Reportes,
        canActivate: [rolGuard(['ADMINISTRACION', 'COMPRAS', 'INVENTARIO', 'VENTAS'])],
      },
      { path: 'bitacora', component: Bitacora, canActivate: [rolGuard(['ADMINISTRACION'])] },
      { path: 'usuarios', component: Usuarios, canActivate: [rolGuard(['ADMINISTRACION'])] },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
