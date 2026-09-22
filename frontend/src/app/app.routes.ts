import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Layout } from './layout/layout';
import { Inicio } from './paginas/inicio/inicio';
import { Proximamente } from './compartido/proximamente/proximamente';
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
      {
        path: 'ventas',
        component: Proximamente,
        data: { titulo: 'Ventas' },
        canActivate: [rolGuard(['ADMINISTRACION', 'VENTAS'])],
      },
      {
        path: 'compras',
        component: Proximamente,
        data: { titulo: 'Compras' },
        canActivate: [rolGuard(['ADMINISTRACION', 'COMPRAS'])],
      },
      {
        path: 'inventario',
        component: Proximamente,
        data: { titulo: 'Inventario' },
        canActivate: [rolGuard(['ADMINISTRACION', 'COMPRAS', 'INVENTARIO', 'VENTAS'])],
      },
      {
        path: 'productos',
        component: Proximamente,
        data: { titulo: 'Productos' },
        canActivate: [rolGuard(['ADMINISTRACION', 'INVENTARIO'])],
      },
      {
        path: 'clientes',
        component: Proximamente,
        data: { titulo: 'Clientes' },
        canActivate: [rolGuard(['ADMINISTRACION', 'VENTAS'])],
      },
      {
        path: 'proveedores',
        component: Proximamente,
        data: { titulo: 'Proveedores' },
        canActivate: [rolGuard(['ADMINISTRACION', 'COMPRAS'])],
      },
      {
        path: 'categorias',
        component: Proximamente,
        data: { titulo: 'Categorías' },
        canActivate: [rolGuard(['ADMINISTRACION', 'INVENTARIO'])],
      },
      {
        path: 'reportes',
        component: Proximamente,
        data: { titulo: 'Reportes' },
        canActivate: [rolGuard(['ADMINISTRACION', 'COMPRAS', 'INVENTARIO', 'VENTAS'])],
      },
      {
        path: 'bitacora',
        component: Proximamente,
        data: { titulo: 'Bitácora' },
        canActivate: [rolGuard(['ADMINISTRACION'])],
      },
      {
        path: 'usuarios',
        component: Proximamente,
        data: { titulo: 'Usuarios' },
        canActivate: [rolGuard(['ADMINISTRACION'])],
      },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
