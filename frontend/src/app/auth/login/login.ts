import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AutenticacionServicio } from '../../core/servicios/autenticacion.service';
import { mensajeDeError } from '../../core/modelos/error.model';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AutenticacionServicio);
  private readonly router = inject(Router);

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  ingresar(): void {
    if (this.formulario.invalid) {
      return;
    }
    this.cargando.set(true);
    this.error.set(null);

    const { username, password } = this.formulario.getRawValue();
    this.auth.login(username!, password!).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(mensajeDeError(err));
      },
    });
  }
}
