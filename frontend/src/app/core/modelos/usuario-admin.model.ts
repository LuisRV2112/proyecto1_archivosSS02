// Distinto de LoginRespuesta (usuario.model.ts): esta es la forma que usa el
// modulo de administracion de usuarios, no la sesion actual.
export interface UsuarioAdmin {
  id: number;
  username: string;
  nombreCompleto: string;
  rol: string;
  activo: boolean;
}

export interface UsuarioPeticion {
  username: string;
  password: string;
  nombreCompleto: string;
  rolId: number;
}

export interface UsuarioActualizacionPeticion {
  nombreCompleto: string;
  rolId: number;
}

export interface RolDisponible {
  id: number;
  nombre: string;
}
