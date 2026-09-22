import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { VentasServicio } from './ventas.service';
import { ProductosServicio } from '../productos/productos.service';
import { ClientesServicio } from '../clientes/clientes.service';
import { Venta } from '../core/modelos/venta.model';
import { Cliente, Producto } from '../core/modelos/catalogo.model';
import { mensajeDeError } from '../core/modelos/error.model';
import { QuetzalesPipe } from '../compartido/pipes/quetzales.pipe';

const TASA_IVA = 0.12;

@Component({
  selector: 'app-ventas',
  imports: [CommonModule, ReactiveFormsModule, QuetzalesPipe],
  templateUrl: './ventas.html',
  styleUrl: './ventas.css',
})
export class Ventas {
  private readonly ventasServicio = inject(VentasServicio);
  private readonly productosServicio = inject(ProductosServicio);
  private readonly clientesServicio = inject(ClientesServicio);
  private readonly fb = inject(FormBuilder);

  protected readonly modo = signal<'lista' | 'nueva'>('lista');
  protected readonly ventas = signal<Venta[]>([]);
  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly subtotal = signal(0);

  protected readonly formulario = this.fb.group({
    clienteId: this.fb.control<number | null>(null, Validators.required),
    lineas: this.fb.array([this.crearLinea()]),
  });

  protected get lineas(): FormArray {
    return this.formulario.get('lineas') as FormArray;
  }

  constructor() {
    this.cargarVentas();
    this.clientesServicio.listar(undefined, true).subscribe((c) => this.clientes.set(c));
    this.productosServicio.listar(undefined, true).subscribe((p) => {
      this.productos.set(p);
      this.recalcularSubtotal();
    });
    this.lineas.valueChanges.subscribe(() => this.recalcularSubtotal());
  }

  protected iva(): number {
    return Math.round(this.subtotal() * TASA_IVA * 100) / 100;
  }

  protected total(): number {
    return this.subtotal() + this.iva();
  }

  protected precioDe(productoId: number | null): number {
    if (!productoId) {
      return 0;
    }
    return this.productos().find((p) => p.id === productoId)?.precioVenta ?? 0;
  }

  protected subtotalLinea(productoId: number | null, cantidad: number | null): number {
    return this.precioDe(productoId) * (cantidad ?? 0);
  }

  protected agregarLinea(): void {
    this.lineas.push(this.crearLinea());
  }

  protected quitarLinea(indice: number): void {
    this.lineas.removeAt(indice);
  }

  protected nuevaVenta(): void {
    this.modo.set('nueva');
  }

  protected cancelar(): void {
    this.modo.set('lista');
    this.resetearFormulario();
  }

  protected registrarVenta(): void {
    if (this.formulario.invalid) {
      return;
    }
    this.cargando.set(true);
    this.error.set(null);

    const valor = this.formulario.getRawValue();
    this.ventasServicio
      .registrar({
        clienteId: valor.clienteId!,
        lineas: (valor.lineas ?? []).map((l) => ({ productoId: l.productoId!, cantidad: l.cantidad! })),
      })
      .subscribe({
        next: () => {
          this.cargando.set(false);
          this.modo.set('lista');
          this.resetearFormulario();
          this.cargarVentas();
        },
        error: (err) => {
          this.cargando.set(false);
          this.error.set(mensajeDeError(err));
        },
      });
  }

  protected descargarFactura(venta: Venta): void {
    this.ventasServicio.descargarFactura(venta.id, venta.numeroFactura).subscribe({
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }

  private cargarVentas(): void {
    this.cargando.set(true);
    this.ventasServicio.listar().subscribe({
      next: (v) => {
        this.ventas.set(v);
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
    });
  }

  private resetearFormulario(): void {
    this.lineas.clear();
    this.lineas.push(this.crearLinea());
    this.formulario.reset({ clienteId: null });
  }

  private recalcularSubtotal(): void {
    const productosMap = new Map(this.productos().map((p) => [p.id, p]));
    const valor = this.lineas.value as Array<{ productoId: number | null; cantidad: number | null }>;
    const total = valor.reduce((acumulado, linea) => {
      const producto = linea.productoId ? productosMap.get(linea.productoId) : undefined;
      return producto ? acumulado + producto.precioVenta * (linea.cantidad ?? 0) : acumulado;
    }, 0);
    this.subtotal.set(Math.round(total * 100) / 100);
  }
}
