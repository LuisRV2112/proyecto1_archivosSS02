import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AutenticacionServicio } from '../servicios/autenticacion.service';
import { Rol } from '../modelos/usuario.model';

export function rolGuard(rolesPermitidos: Rol[]): CanActivateFn {
  return () => {
    const auth = inject(AutenticacionServicio);
    const router = inject(Router);

    if (!auth.estaAutenticado()) {
      return router.createUrlTree(['/login']);
    }
    if (!rolesPermitidos.includes(auth.rol()!)) {
      return router.createUrlTree(['/']);
    }
    return true;
  };
}
