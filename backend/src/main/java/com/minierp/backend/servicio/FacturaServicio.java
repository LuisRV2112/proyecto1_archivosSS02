package com.minierp.backend.servicio;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.minierp.backend.modelo.DetalleVenta;
import com.minierp.backend.modelo.Usuario;
import com.minierp.backend.modelo.Venta;
import com.minierp.backend.repositorio.DetalleVentaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

// Genera la factura de una venta en PDF con OpenPDF (no iText: su licencia
// AGPL no es apropiada aqui, CLAUDE.md seccion 1.2).
@Service
public class FacturaServicio {

    private static final String NOMBRE_EMPRESA = "Muebles del Hogar y Oficina, S.A.";
    private static final String NIT_EMPRESA = "1234567-8";
    private static final String DIRECCION_EMPRESA = "Zona 1, Quetzaltenango, Guatemala";

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat FORMATO_MONEDA =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private final VentaServicio ventaServicio;
    private final DetalleVentaRepositorio detalleVentaRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public FacturaServicio(
            VentaServicio ventaServicio,
            DetalleVentaRepositorio detalleVentaRepositorio,
            BitacoraServicio bitacoraServicio) {
        this.ventaServicio = ventaServicio;
        this.detalleVentaRepositorio = detalleVentaRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    // No es readOnly: ademas de leer la venta, escribe en bitacora
    // (registrar la factura generada), y Postgres rechaza un INSERT dentro de
    // una transaccion marcada de solo lectura.
    @Transactional
    public byte[] generar(Integer ventaId, Usuario actor) {
        Venta venta = ventaServicio.obtenerEntidad(ventaId);
        List<DetalleVenta> detalles = detalleVentaRepositorio.findByVentaId(ventaId);

        byte[] pdf = construirPdf(venta, detalles);

        bitacoraServicio.registrar(actor, "VENTAS", "FACTURA_GENERADA", "venta", venta.getId(),
                "Factura #" + venta.getNumeroFactura() + " generada en PDF");

        return pdf;
    }

    private byte[] construirPdf(Venta venta, List<DetalleVenta> detalles) {
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fuenteNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fuenteEncabezadoTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

        Document documento = new Document(PageSize.LETTER, 50, 50, 50, 50);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Paragraph titulo = new Paragraph(NOMBRE_EMPRESA, fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph datosEmpresa = new Paragraph("NIT: " + NIT_EMPRESA + "   |   " + DIRECCION_EMPRESA, fuenteSubtitulo);
            datosEmpresa.setAlignment(Element.ALIGN_CENTER);
            datosEmpresa.setSpacingAfter(15);
            documento.add(datosEmpresa);

            Paragraph datosFactura = new Paragraph();
            datosFactura.add(new Chunk("Factura No. " + venta.getNumeroFactura() + "\n", fuenteNegrita));
            datosFactura.add(new Chunk("Fecha: " + venta.getFecha().format(FORMATO_FECHA) + "\n", fuenteNormal));
            datosFactura.add(new Chunk("Cliente: " + venta.getCliente().getNombre() + "\n", fuenteNormal));
            datosFactura.add(new Chunk("NIT: " + venta.getCliente().getNit() + "\n", fuenteNormal));
            datosFactura.setSpacingAfter(15);
            documento.add(datosFactura);

            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{1.2f, 4.3f, 1.6f, 1.6f});

            agregarCeldaEncabezado(tabla, "Cantidad", fuenteEncabezadoTabla);
            agregarCeldaEncabezado(tabla, "Descripcion", fuenteEncabezadoTabla);
            agregarCeldaEncabezado(tabla, "Precio", fuenteEncabezadoTabla);
            agregarCeldaEncabezado(tabla, "Subtotal", fuenteEncabezadoTabla);

            for (DetalleVenta detalle : detalles) {
                tabla.addCell(new Phrase(String.valueOf(detalle.getCantidad()), fuenteNormal));
                tabla.addCell(new Phrase(detalle.getProducto().getNombre(), fuenteNormal));
                agregarCeldaMonto(tabla, detalle.getPrecioUnitario(), fuenteNormal);
                agregarCeldaMonto(tabla, detalle.getSubtotal(), fuenteNormal);
            }

            documento.add(tabla);

            Paragraph totales = new Paragraph();
            totales.setAlignment(Element.ALIGN_RIGHT);
            totales.setSpacingBefore(10);
            totales.add(new Chunk("Subtotal: " + formatoQuetzales(venta.getSubtotal()) + "\n", fuenteNormal));
            totales.add(new Chunk("IVA (12%): " + formatoQuetzales(venta.getIva()) + "\n", fuenteNormal));
            totales.add(new Chunk("Total: " + formatoQuetzales(venta.getTotal()) + "\n", fuenteNegrita));
            documento.add(totales);

            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar la factura PDF de la venta " + venta.getId(), e);
        }

        return salida.toByteArray();
    }

    private void agregarCeldaEncabezado(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(new Color(60, 60, 60));
        celda.setPadding(6);
        tabla.addCell(celda);
    }

    private void agregarCeldaMonto(PdfPTable tabla, BigDecimal monto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(formatoQuetzales(monto), fuente));
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celda.setPadding(4);
        tabla.addCell(celda);
    }

    private static String formatoQuetzales(BigDecimal monto) {
        return "Q " + FORMATO_MONEDA.format(monto);
    }
}
