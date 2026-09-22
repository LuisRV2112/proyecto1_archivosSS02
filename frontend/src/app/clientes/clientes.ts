import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClientesServicio } from './clientes.service';
import { Cliente } from '../core/modelos/catalogo.model';
import { mensajeDeError } from '../core/modelos/error.model';

@Component({
  selector: 'app-clientes',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clientes.html',
  styleUrl: './clientes.css',
})
export class Clientes {
  private readonly clientesServicio = inject(ClientesServicio);
  private readonly fb = inject(FormBuilder);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly buscar = signal('');
  protected readonly mostrarInactivos = signal(false);
  protected readonly editando = signal<Cliente | null>(null);
  protected readonly formularioVisible = signal(false);

  protected readonly formulario = this.fb.group({
    nit: ['', Validators.required],
    nombre: ['', Validators.required],
    telefono: [''],
    correo: [''],
    direccion: [''],
  });

  constructor() {
    this.cargarClientes();
  }

  protected buscarCambio(texto: string): void {
    this.buscar.set(texto);
    this.cargarClientes();
  }

  protected alternarInactivos(): void {
    this.mostrarInactivos.set(!this.mostrarInactivos());
    this.cargarClientes();
  }

  protected nuevo(): void {
    this.editando.set(null);
    this.formulario.reset();
    this.formularioVisible.set(true);
  }

  protected editar(cliente: Cliente): void {
    this.editando.set(cliente);
    this.formulario.setValue({
      nit: cliente.nit,
      nombre: cliente.nombre,
      telefono: cliente.telefono ?? '',
      correo: cliente.correo ?? '',
      direccion: cliente.direccion ?? '',
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
      ? this.clientesServicio.actualizar(this.editando()!.id, peticion)
      : this.clientesServicio.crear(peticion);

    operacion.subscribe({
      next: () => {
        this.formularioVisible.set(false);
        this.editando.set(null);
        this.cargarClientes();
      },
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }

  protected cambiarEstado(cliente: Cliente): void {
    if (cliente.activo) {
      if (!confirm(`¿Desactivar el cliente "${cliente.nombre}"?`)) {
        return;
      }
      this.clientesServicio.desactivar(cliente.id).subscribe({
        next: () => this.cargarClientes(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    } else {
      this.clientesServicio.activar(cliente.id).subscribe({
        next: () => this.cargarClientes(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    }
  }

  private cargarClientes(): void {
    this.cargando.set(true);
    const activo = this.mostrarInactivos() ? undefined : true;
    this.clientesServicio.listar(this.buscar() || undefined, activo).subscribe({
      next: (c) => {
        this.clientes.set(c);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }
}
