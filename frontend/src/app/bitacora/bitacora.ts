import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BitacoraServicio } from './bitacora.service';
import { UsuariosServicio } from '../usuarios/usuarios.service';
import { RegistroBitacora } from '../core/modelos/bitacora.model';
import { UsuarioAdmin } from '../core/modelos/usuario-admin.model';
import { mensajeDeError } from '../core/modelos/error.model';

const MODULOS = [
  'AUTENTICACION',
  'CATEGORIAS',
  'PRODUCTOS',
  'PROVEEDORES',
  'CLIENTES',
  'COMPRAS',
  'VENTAS',
  'USUARIOS',
];

@Component({
  selector: 'app-bitacora',
  imports: [CommonModule, FormsModule],
  templateUrl: './bitacora.html',
  styleUrl: './bitacora.css',
})
export class Bitacora {
  private readonly bitacoraServicio = inject(BitacoraServicio);
  private readonly usuariosServicio = inject(UsuariosServicio);

  protected readonly registros = signal<RegistroBitacora[]>([]);
  protected readonly usuarios = signal<UsuarioAdmin[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly modulos = MODULOS;

  protected usuarioId: number | null = null;
  protected modulo = '';
  protected desde = '';
  protected hasta = '';

  constructor() {
    this.usuariosServicio.listar().subscribe((u) => this.usuarios.set(u));
    this.consultar();
  }

  protected consultar(): void {
    this.cargando.set(true);
    this.error.set(null);
    const desdeIso = this.desde ? new Date(this.desde).toISOString() : undefined;
    const hastaIso = this.hasta ? new Date(this.hasta).toISOString() : undefined;

    this.bitacoraServicio.consultar(this.usuarioId ?? undefined, this.modulo || undefined, desdeIso, hastaIso).subscribe({
      next: (r) => {
        this.registros.set(r);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }

  protected limpiarFiltros(): void {
    this.usuarioId = null;
    this.modulo = '';
    this.desde = '';
    this.hasta = '';
    this.consultar();
  }
}
