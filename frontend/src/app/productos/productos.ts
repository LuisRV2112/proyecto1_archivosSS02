import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductosServicio } from './productos.service';
import { CategoriasServicio } from '../categorias/categorias.service';
import { Categoria, Producto } from '../core/modelos/catalogo.model';
import { mensajeDeError } from '../core/modelos/error.model';
import { QuetzalesPipe } from '../compartido/pipes/quetzales.pipe';

@Component({
  selector: 'app-productos',
  imports: [CommonModule, ReactiveFormsModule, QuetzalesPipe],
  templateUrl: './productos.html',
  styleUrl: './productos.css',
})
export class Productos {
  private readonly productosServicio = inject(ProductosServicio);
  private readonly categoriasServicio = inject(CategoriasServicio);
  private readonly fb = inject(FormBuilder);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly buscar = signal('');
  protected readonly mostrarInactivos = signal(false);
  protected readonly editando = signal<Producto | null>(null);
  protected readonly formularioVisible = signal(false);

  protected readonly formulario = this.fb.group({
    codigo: ['', Validators.required],
    nombre: ['', Validators.required],
    descripcion: [''],
    categoriaId: this.fb.control<number | null>(null, Validators.required),
    precioVenta: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    stockMinimo: this.fb.control<number | null>(0, [Validators.required, Validators.min(0)]),
  });

  constructor() {
    this.cargarProductos();
    this.categoriasServicio.listar(undefined, true).subscribe((c) => this.categorias.set(c));
  }

  protected buscarCambio(texto: string): void {
    this.buscar.set(texto);
    this.cargarProductos();
  }

  protected alternarInactivos(): void {
    this.mostrarInactivos.set(!this.mostrarInactivos());
    this.cargarProductos();
  }

  protected nuevo(): void {
    this.editando.set(null);
    this.formulario.reset({ stockMinimo: 0 });
    this.formularioVisible.set(true);
  }

  protected editar(producto: Producto): void {
    this.editando.set(producto);
    this.formulario.setValue({
      codigo: producto.codigo,
      nombre: producto.nombre,
      descripcion: producto.descripcion ?? '',
      categoriaId: producto.categoriaId,
      precioVenta: producto.precioVenta,
      stockMinimo: producto.stockMinimo,
    });
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
    const peticion = {
      codigo: valor.codigo!,
      nombre: valor.nombre!,
      descripcion: valor.descripcion || null,
      categoriaId: valor.categoriaId!,
      precioVenta: valor.precioVenta!,
      stockMinimo: valor.stockMinimo!,
    };

    const operacion = this.editando()
      ? this.productosServicio.actualizar(this.editando()!.id, peticion)
      : this.productosServicio.crear(peticion);

    operacion.subscribe({
      next: () => {
        this.formularioVisible.set(false);
        this.editando.set(null);
        this.cargarProductos();
      },
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }

  protected cambiarEstado(producto: Producto): void {
    if (producto.activo) {
      if (!confirm(`¿Desactivar el producto "${producto.nombre}"?`)) {
        return;
      }
      this.productosServicio.desactivar(producto.id).subscribe({
        next: () => this.cargarProductos(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    } else {
      this.productosServicio.activar(producto.id).subscribe({
        next: () => this.cargarProductos(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    }
  }

  private cargarProductos(): void {
    this.cargando.set(true);
    const activo = this.mostrarInactivos() ? undefined : true;
    this.productosServicio.listar(this.buscar() || undefined, activo).subscribe({
      next: (p) => {
        this.productos.set(p);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }
}
