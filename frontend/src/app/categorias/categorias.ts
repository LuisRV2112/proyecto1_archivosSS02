import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoriasServicio } from './categorias.service';
import { Categoria } from '../core/modelos/catalogo.model';
import { mensajeDeError } from '../core/modelos/error.model';

@Component({
  selector: 'app-categorias',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './categorias.html',
  styleUrl: './categorias.css',
})
export class Categorias {
  private readonly categoriasServicio = inject(CategoriasServicio);
  private readonly fb = inject(FormBuilder);

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly mostrarInactivos = signal(false);
  protected readonly editando = signal<Categoria | null>(null);
  protected readonly formularioVisible = signal(false);

  protected readonly formulario = this.fb.group({
    nombre: ['', Validators.required],
    descripcion: [''],
  });

  constructor() {
    this.cargarCategorias();
  }

  protected alternarInactivos(): void {
    this.mostrarInactivos.set(!this.mostrarInactivos());
    this.cargarCategorias();
  }

  protected nuevo(): void {
    this.editando.set(null);
    this.formulario.reset();
    this.formularioVisible.set(true);
  }

  protected editar(categoria: Categoria): void {
    this.editando.set(categoria);
    this.formulario.setValue({ nombre: categoria.nombre, descripcion: categoria.descripcion ?? '' });
    this.formularioVisible.set(true);
  }

  protected cancelar(): void {
    this.formularioVisible.set(false);
    this.editando.set(null);
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      return;
    }
    this.error.set(null);
    const valor = this.formulario.getRawValue();
    const peticion = { nombre: valor.nombre!, descripcion: valor.descripcion || null };

    const operacion = this.editando()
      ? this.categoriasServicio.actualizar(this.editando()!.id, peticion)
      : this.categoriasServicio.crear(peticion);

    operacion.subscribe({
      next: () => {
        this.formularioVisible.set(false);
        this.editando.set(null);
        this.cargarCategorias();
      },
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }

  protected cambiarEstado(categoria: Categoria): void {
    if (categoria.activo) {
      if (!confirm(`¿Desactivar la categoría "${categoria.nombre}"?`)) {
        return;
      }
      this.categoriasServicio.desactivar(categoria.id).subscribe({
        next: () => this.cargarCategorias(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    } else {
      this.categoriasServicio.activar(categoria.id).subscribe({
        next: () => this.cargarCategorias(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    }
  }

  private cargarCategorias(): void {
    this.cargando.set(true);
    const activo = this.mostrarInactivos() ? undefined : true;
    this.categoriasServicio.listar(undefined, activo).subscribe({
      next: (c) => {
        this.categorias.set(c);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }
}
