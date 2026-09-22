import { Component } from '@angular/core';
import { AutenticacionServicio } from '../../core/servicios/autenticacion.service';

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
