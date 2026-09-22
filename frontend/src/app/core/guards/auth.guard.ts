import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AutenticacionServicio } from '../servicios/autenticacion.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AutenticacionServicio);
  const router = inject(Router);
  return auth.estaAutenticado() || router.createUrlTree(['/login']);
};
