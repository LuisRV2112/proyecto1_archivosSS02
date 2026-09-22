// Coincide con ErrorRespuesta del backend (CLAUDE.md, seccion 8):
// { "estado": 400, "error": "...", "detalle": "...", "fecha": "..." }
export interface ErrorApi {
  estado: number;
  error: string;
  detalle: string;
  fecha: string;
}

// Extrae un mensaje legible del error HTTP que devuelve HttpClient, con un
// mensaje generico de respaldo si el backend no respondio con el formato
// esperado (por ejemplo, si esta caido)
export function mensajeDeError(error: unknown): string {
  const httpError = error as { error?: Partial<ErrorApi>; message?: string };
  return httpError?.error?.detalle || httpError?.error?.error || httpError?.message || 'Ocurrio un error inesperado';
}
