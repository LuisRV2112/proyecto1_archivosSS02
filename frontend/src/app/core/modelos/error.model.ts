
export interface ErrorApi {
  estado: number;
  error: string;
  detalle: string;
  fecha: string;
}

export function mensajeDeError(error: unknown): string {
  const httpError = error as { error?: Partial<ErrorApi>; message?: string };
  return httpError?.error?.detalle || httpError?.error?.error || httpError?.message || 'Ocurrio un error inesperado';
}
