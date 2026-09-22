export interface RegistroBitacora {
  id: number;
  usuarioUsername: string | null;
  modulo: string;
  accion: string;
  entidad: string | null;
  entidadId: number | null;
  detalle: string;
  fecha: string;
}
