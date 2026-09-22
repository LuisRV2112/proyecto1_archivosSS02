import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AutenticacionServicio } from '../servicios/autenticacion.service';

// Agrega "Authorization: Bearer <token>" a cada peticion (si hay sesion), y
// si el backend responde 401 (token vencido o invalido) cierra la sesion y
// redirige al login en vez de dejar la pantalla en un estado inconsistente.
export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const auth = inject(AutenticacionServicio);
  const router = inject(Router);
  const token = auth.obtenerToken();

  const peticionConToken = token
    ? peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : peticion;

  return siguiente(peticionConToken).pipe(
    catchError((error) => {
      if (error.status === 401) {
        auth.cerrarSesion();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
