import { Component } from '@angular/core';
import { AutenticacionServicio } from '../../core/servicios/autenticacion.service';

// Pagina de aterrizaje tras el login: accesible a cualquier rol
// autenticado (a diferencia de los modulos, que si tienen rolGuard). Sirve
// tambien como destino seguro cuando rolGuard rechaza una ruta, sin riesgo
// de generar un loop de redirecciones.
@Component({
  selector: 'app-inicio',
  template: `
    <h2>Bienvenido, {{ auth.usuario()?.nombreCompleto }}</h2>
    <p>Rol: {{ auth.usuario()?.rol }}</p>
  `,
})
export class Inicio {
  constructor(protected readonly auth: AutenticacionServicio) {}
}
