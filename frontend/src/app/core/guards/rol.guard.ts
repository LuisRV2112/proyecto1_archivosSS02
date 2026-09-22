import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AutenticacionServicio } from '../servicios/autenticacion.service';
import { Rol } from '../modelos/usuario.model';

// Factoria de guard: cada ruta declara que roles puede ver
// (canActivate: [rolGuard(['ADMINISTRACION', 'VENTAS'])]). Sin sesion manda
// al login; con sesion pero rol no autorizado, manda a la pagina de inicio
// (nunca a una ruta que a su vez podria rechazarlo tambien, para evitar
// loops de redireccion).
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
