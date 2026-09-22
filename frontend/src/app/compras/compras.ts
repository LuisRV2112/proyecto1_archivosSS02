import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ComprasServicio } from './compras.service';
import { ProductosServicio } from '../productos/productos.service';
import { ProveedoresServicio } from '../proveedores/proveedores.service';
import { Compra } from '../core/modelos/compra.model';
import { Producto, Proveedor } from '../core/modelos/catalogo.model';
import { mensajeDeError } from '../core/modelos/error.model';
import { QuetzalesPipe } from '../compartido/pipes/quetzales.pipe';

@Component({
  selector: 'app-compras',
  imports: [CommonModule, ReactiveFormsModule, QuetzalesPipe],
  templateUrl: './compras.html',
  styleUrl: './compras.css',
})
export class Compras {
  private readonly comprasServicio = inject(ComprasServicio);
  private readonly productosServicio = inject(ProductosServicio);
  private readonly proveedoresServicio = inject(ProveedoresServicio);
  private readonly fb = inject(FormBuilder);

  protected readonly modo = signal<'lista' | 'nueva'>('lista');
  protected readonly compras = signal<Compra[]>([]);
  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = this.fb.group({
    proveedorId: this.fb.control<number | null>(null, Validators.required),
    fecha: this.fb.control<string>(this.fechaActualParaInput(), Validators.required),
    numeroDocumento: this.fb.control<string>(''),
    lineas: this.fb.array([this.crearLinea()]),
  });

  protected get lineas(): FormArray {
    return this.formulario.get('lineas') as FormArray;
  }

  constructor() {
    this.cargarCompras();
    this.proveedoresServicio.listar(undefined, true).subscribe((p) => this.proveedores.set(p));
    this.productosServicio.listar(undefined, true).subscribe((p) => this.productos.set(p));
  }

  protected subtotalLinea(cantidad: number | null, costo: number | null): number {
    return (cantidad ?? 0) * (costo ?? 0);
  }

  protected totalCompra(): number {
    const valor = this.lineas.value as Array<{ cantidad: number | null; costoUnitario: number | null }>;
    return valor.reduce((acumulado, l) => acumulado + this.subtotalLinea(l.cantidad, l.costoUnitario), 0);
  }

  protected agregarLinea(): void {
    this.lineas.push(this.crearLinea());
  }

  protected quitarLinea(indice: number): void {
    this.lineas.removeAt(indice);
  }

  protected nuevaCompra(): void {
    this.modo.set('nueva');
  }

  protected cancelar(): void {
    this.modo.set('lista');
    this.resetearFormulario();
  }

  protected registrarCompra(): void {
    if (this.formulario.invalid) {
      return;
    }
    this.cargando.set(true);
    this.error.set(null);

    const valor = this.formulario.getRawValue();
    this.comprasServicio
      .registrar({
        proveedorId: valor.proveedorId!,
        fecha: new Date(valor.fecha!).toISOString(),
        numeroDocumento: valor.numeroDocumento || null,
        observaciones: null,
        lineas: (valor.lineas ?? []).map((l) => ({
          productoId: l.productoId!,
          cantidad: l.cantidad!,
          costoUnitario: l.costoUnitario!,
        })),
      })
      .subscribe({
        next: () => {
          this.cargando.set(false);
          this.modo.set('lista');
          this.resetearFormulario();
          this.cargarCompras();
        },
        error: (err) => {
          this.cargando.set(false);
          this.error.set(mensajeDeError(err));
        },
      });
  }

  private cargarCompras(): void {
    this.cargando.set(true);
    this.comprasServicio.listar().subscribe({
      next: (c) => {
        this.compras.set(c);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }

  private crearLinea() {
    return this.fb.group({
      productoId: this.fb.control<number | null>(null, Validators.required),
      cantidad: this.fb.control<number | null>(1, [Validators.required, Validators.min(1)]),
      costoUnitario: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    });
  }

  private resetearFormulario(): void {
    this.lineas.clear();
    this.lineas.push(this.crearLinea());
    this.formulario.reset({ proveedorId: null, fecha: this.fechaActualParaInput(), numeroDocumento: '' });
  }

  private fechaActualParaInput(): string {
    const ahora = new Date();
    ahora.setMinutes(ahora.getMinutes() - ahora.getTimezoneOffset());
    return ahora.toISOString().slice(0, 16);
  }
}
