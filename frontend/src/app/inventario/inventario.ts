import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InventarioServicio } from './inventario.service';
import { AlertaStock, Existencia, Kardex } from '../core/modelos/inventario.model';
import { mensajeDeError } from '../core/modelos/error.model';
import { QuetzalesPipe } from '../compartido/pipes/quetzales.pipe';

type Pestana = 'existencias' | 'alertas' | 'kardex';

@Component({
  selector: 'app-inventario',
  imports: [CommonModule, FormsModule, QuetzalesPipe],
  templateUrl: './inventario.html',
  styleUrl: './inventario.css',
})
export class Inventario {
  private readonly inventarioServicio = inject(InventarioServicio);

  protected readonly pestana = signal<Pestana>('existencias');
  protected readonly existencias = signal<Existencia[]>([]);
  protected readonly alertas = signal<AlertaStock[]>([]);
  protected readonly kardex = signal<Kardex[]>([]);
  protected readonly productoSeleccionado = signal<number | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.cargarExistencias();
    this.cargarAlertas();
  }

  protected cambiarPestana(destino: Pestana): void {
    this.pestana.set(destino);
  }

  protected verKardex(productoId: number): void {
    this.pestana.set('kardex');
    this.productoSeleccionado.set(productoId);
    this.cargando.set(true);
    this.error.set(null);
    this.inventarioServicio.kardex(productoId).subscribe({
      next: (k) => {
        this.kardex.set(k);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }

  private cargarExistencias(): void {
    this.cargando.set(true);
    this.inventarioServicio.existencias().subscribe({
      next: (e) => {
        this.existencias.set(e);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }

  private cargarAlertas(): void {
    this.inventarioServicio.alertas().subscribe({
      next: (a) => this.alertas.set(a),
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }
}
