import { Component, computed } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AutenticacionServicio } from '../core/servicios/autenticacion.service';
import { MENU } from './menu.config';

@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class Layout {
  // Solo las opciones que el rol actual tiene permitidas (tabla, seccion 6)
  protected readonly menu = computed(() => {
    const rolActual = this.auth.rol();
    return MENU.filter((item) => rolActual !== null && item.roles.includes(rolActual));
  });

  constructor(protected readonly auth: AutenticacionServicio) {}

  cerrarSesion(): void {
    this.auth.cerrarSesion();
  }
}
