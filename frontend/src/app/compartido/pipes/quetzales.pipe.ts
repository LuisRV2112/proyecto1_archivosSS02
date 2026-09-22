import { Pipe, PipeTransform } from '@angular/core';

const FORMATO = new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

// Formato de moneda que pide el CLAUDE.md en todos los modulos: "Q 1,234.56"
@Pipe({ name: 'quetzales' })
export class QuetzalesPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    if (valor === null || valor === undefined) {
      return '';
    }
    return `Q ${FORMATO.format(valor)}`;
  }
}
