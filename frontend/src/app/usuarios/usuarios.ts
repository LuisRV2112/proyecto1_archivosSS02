import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsuariosServicio } from './usuarios.service';
import { RolDisponible, UsuarioAdmin } from '../core/modelos/usuario-admin.model';
import { mensajeDeError } from '../core/modelos/error.model';

@Component({
  selector: 'app-usuarios',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios {
  private readonly usuariosServicio = inject(UsuariosServicio);
  private readonly fb = inject(FormBuilder);

  protected readonly usuarios = signal<UsuarioAdmin[]>([]);
  protected readonly roles = signal<RolDisponible[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly editando = signal<UsuarioAdmin | null>(null);
  protected readonly formularioVisible = signal(false);

  protected readonly formulario = this.fb.group({
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]],
    nombreCompleto: ['', Validators.required],
    rolId: this.fb.control<number | null>(null, Validators.required),
  });

  constructor() {
    this.cargarUsuarios();
    this.usuariosServicio.roles().subscribe((r) => this.roles.set(r));
  }

  protected nuevo(): void {
    this.editando.set(null);
    this.formulario.reset();
    this.formulario.get('username')!.enable();
    this.formulario.get('password')!.enable();
    this.formularioVisible.set(true);
  }

  protected editar(usuario: UsuarioAdmin): void {
    this.editando.set(usuario);
    const rolActual = this.roles().find((r) => r.nombre === usuario.rol);
    this.formulario.setValue({
      username: usuario.username,
      password: '',
      nombreCompleto: usuario.nombreCompleto,
      rolId: rolActual?.id ?? null,
    });

    this.formulario.get('username')!.disable();
    this.formulario.get('password')!.disable();
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
    const usuarioEnEdicion = this.editando();

    const operacion = usuarioEnEdicion
      ? this.usuariosServicio.actualizar(usuarioEnEdicion.id, {
          nombreCompleto: valor.nombreCompleto!,
          rolId: valor.rolId!,
        })
      : this.usuariosServicio.crear({
          username: valor.username!,
          password: valor.password!,
          nombreCompleto: valor.nombreCompleto!,
          rolId: valor.rolId!,
        });

    operacion.subscribe({
      next: () => {
        this.formularioVisible.set(false);
        this.editando.set(null);
        this.cargarUsuarios();
      },
      error: (err) => this.error.set(mensajeDeError(err)),
    });
  }

  protected cambiarEstado(usuario: UsuarioAdmin): void {
    if (usuario.activo) {
      if (!confirm(`¿Desactivar al usuario "${usuario.username}"? Ya no podrá iniciar sesión.`)) {
        return;
      }
      this.usuariosServicio.desactivar(usuario.id).subscribe({
        next: () => this.cargarUsuarios(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    } else {
      this.usuariosServicio.activar(usuario.id).subscribe({
        next: () => this.cargarUsuarios(),
        error: (err) => this.error.set(mensajeDeError(err)),
      });
    }
  }

  private cargarUsuarios(): void {
    this.cargando.set(true);
    this.usuariosServicio.listar().subscribe({
      next: (u) => {
        this.usuarios.set(u);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }
}
