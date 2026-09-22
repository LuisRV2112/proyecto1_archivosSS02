import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../api-url';
import { LoginRespuesta, Rol } from '../modelos/usuario.model';

const CLAVE_STORAGE = 'minierp_sesion';

@Injectable({ providedIn: 'root' })
export class AutenticacionServicio {

  private readonly sesion = signal<LoginRespuesta | null>(this.leerSesionGuardada());

  readonly usuario = computed(() => this.sesion());
  readonly estaAutenticado = computed(() => this.sesion() !== null);
  readonly rol = computed<Rol | null>(() => this.sesion()?.rol ?? null);

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  login(username: string, password: string): Observable<LoginRespuesta> {
    return this.http
      .post<LoginRespuesta>(`${API_URL}/auth/login`, { username, password })
      .pipe(tap((respuesta) => this.guardarSesion(respuesta)));
  }

  cerrarSesion(): void {
    this.sesion.set(null);
    localStorage.removeItem(CLAVE_STORAGE);
    this.router.navigate(['/login']);
  }

  obtenerToken(): string | null {
    return this.sesion()?.token ?? null;
  }

  private guardarSesion(respuesta: LoginRespuesta): void {
    this.sesion.set(respuesta);
    localStorage.setItem(CLAVE_STORAGE, JSON.stringify(respuesta));
  }

  private leerSesionGuardada(): LoginRespuesta | null {
    const guardada = localStorage.getItem(CLAVE_STORAGE);
    if (!guardada) {
      return null;
    }
    try {
      return JSON.parse(guardada) as LoginRespuesta;
    } catch {
      return null;
    }
  }
}
