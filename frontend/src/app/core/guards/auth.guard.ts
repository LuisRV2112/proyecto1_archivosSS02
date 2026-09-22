import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AutenticacionServicio } from '../servicios/autenticacion.service';

// Exige solo estar autenticado (cualquier rol). Lo usa la ruta padre del
// layout: sin sesion, ninguna pantalla interna es alcanzable.
export const authGuard: CanActivateFn = () => {
  const auth = inject(AutenticacionServicio);
  const router = inject(Router);
  return auth.estaAutenticado() || router.createUrlTree(['/login']);
};
