import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProveedoresServicio } from './proveedores.service';
import { ProductosServicio } from '../productos/productos.service';
import { Producto, ProductoProveedor, Proveedor } from '../core/modelos/catalogo.model';
import { mensajeDeError } from '../core/modelos/error.model';
import { QuetzalesPipe } from '../compartido/pipes/quetzales.pipe';

@Component({
  selector: 'app-proveedores',
  imports: [CommonModule, ReactiveFormsModule, QuetzalesPipe],
  templateUrl: './proveedores.html',
  styleUrl: './proveedores.css',
})
export class Proveedores {
  private readonly proveedoresServicio = inject(ProveedoresServicio);
  private readonly productosServicio = inject(ProductosServicio);
  private readonly fb = inject(FormBuilder);

  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly buscar = signal('');
  protected readonly mostrarInactivos = signal(false);
  protected readonly editando = signal<Proveedor | null>(null);
  protected readonly formularioVisible = signal(false);

  protected readonly proveedorExpandido = signal<number | null>(null);
  protected readonly productosAsociados = signal<ProductoProveedor[]>([]);

  protected readonly formulario = this.fb.group({
    nit: ['', Validators.required],
    nombre: ['', Validators.required],
    telefono: [''],
    correo: [''],
    direccion: [''],
  });

  protected readonly formularioAsociacion = this.fb.group({
    productoId: this.fb.control<number | null>(null, Validators.required),
    costoReferencia: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
  });

  constructor() {
    this.cargarProveedores();
    this.productosServicio.listar(undefined, true).subscribe((p) => this.productos.set(p));
  }

  protected buscarCambio(texto: string): void {
    this.buscar.set(texto);
    this.cargarProveedores();
  }

  protected alternarInactivos(): void {
    this.mostrarInactivos.set(!this.mostrarInactivos());
    this.cargarProveedores();
  }

  protected nuevo(): void {
    this.editando.set(null);
    this.formulario.reset();
    this.formularioVisible.set(true);
  }

  protected editar(proveedor: Proveedor): void {
    this.editando.set(proveedor);
    this.formulario.setValue({
      nit: proveedor.nit,
      nombre: proveedor.nombre,
      telefono: proveedor.telefono ?? '',
      correo: proveedor.correo ?? '',
      direccion: proveedor.direccion ?? '',
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
      nit: valor.nit!,
      nombre: valor.nombre!,
      telefono: valor.telefono || null,
      correo: valor.correo || null,
      direccion: valor.direccion || null,
    };

    const operacion = this.editando()
      ? this.proveedoresServicio.actualizar(this.editando()!.id, peticion)
      : this.proveedoresServicio.crear(peticion);

    operacion.subscribe({
      next: () => {
        this.formularioVisible.set(false);
        this.editando.set(null);
        this.cargarProveedores();
      },
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }

  protected cambiarEstado(proveedor: Proveedor): void {
    if (proveedor.activo) {
      if (!confirm(`¿Desactivar el proveedor "${proveedor.nombre}"?`)) {
        return;
      }
      this.proveedoresServicio.desactivar(proveedor.id).subscribe({
        next: () => this.cargarProveedores(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    } else {
      this.proveedoresServicio.activar(proveedor.id).subscribe({
        next: () => this.cargarProveedores(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    }
  }

  protected verProductos(proveedor: Proveedor): void {
    if (this.proveedorExpandido() === proveedor.id) {
      this.proveedorExpandido.set(null);
      return;
    }
    this.proveedorExpandido.set(proveedor.id);
    this.formularioAsociacion.reset();
    this.cargarProductosAsociados(proveedor.id);
  }

  protected asociarProducto(proveedorId: number): void {
    if (this.formularioAsociacion.invalid) {
      return;
    }
    const valor = this.formularioAsociacion.getRawValue();
    this.proveedoresServicio
      .asociarProducto(proveedorId, { productoId: valor.productoId!, costoReferencia: valor.costoReferencia! })
      .subscribe({
        next: () => {
          this.formularioAsociacion.reset();
          this.cargarProductosAsociados(proveedorId);
        },
        error: (err) => this.error.set(mensajeDeError(err)),
      });
  }

  protected desasociarProducto(proveedorId: number, productoId: number): void {
    if (!confirm('¿Quitar este producto de los que surte el proveedor?')) {
      return;
    }
    this.proveedoresServicio.desasociarProducto(proveedorId, productoId).subscribe({
      next: () => this.cargarProductosAsociados(proveedorId),
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }

  private cargarProductosAsociados(proveedorId: number): void {
    this.proveedoresServicio.listarProductos(proveedorId).subscribe({
      next: (p) => this.productosAsociados.set(p),
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }

  private cargarProveedores(): void {
    this.cargando.set(true);
    const activo = this.mostrarInactivos() ? undefined : true;
    this.proveedoresServicio.listar(this.buscar() || undefined, activo).subscribe({
      next: (p) => {
        this.proveedores.set(p);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }
}
