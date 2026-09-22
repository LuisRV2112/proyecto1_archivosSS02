import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

// Marcador de posicion para los modulos que se implementan en los bloques
// 7.2 y 7.3: deja las rutas, guards y el menu ya funcionando desde el
// bloque 7.1, listos para reemplazar el componente sin tocar la navegacion.
@Component({
  selector: 'app-proximamente',
  template: `
    <h2>{{ titulo }}</h2>
    <p>Este modulo se implementa en el siguiente bloque de la Fase 7.</p>
  `,
})
export class Proximamente {
  private readonly route = inject(ActivatedRoute);
  protected readonly titulo = (this.route.snapshot.data['titulo'] as string) ?? '';
}
