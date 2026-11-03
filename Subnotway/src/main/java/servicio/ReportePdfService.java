package servicio;

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
import dao.impl.ReporteDAO;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ReportePdfService {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final Color COLOR_PRIMARIO = new Color(0, 137, 56);
    private static final Color COLOR_SECUNDARIO = new Color(245, 245, 245);
    private static final Font FUENTE_EMPRESA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_PRIMARIO);
    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.DARK_GRAY);
    private static final Font FUENTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
    private static final Font FUENTE_TH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FUENTE_TD = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
    private static final Font FUENTE_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);

    private void agregarEncabezado(Document document, String titulo) throws DocumentException {
        Paragraph pEmpresa = new Paragraph("SUBNOTWAY - PAN, PUNTOS Y PREMIOS", FUENTE_EMPRESA);
        pEmpresa.setAlignment(Element.ALIGN_CENTER);
        document.add(pEmpresa);

        Paragraph pTitulo = new Paragraph(titulo.toUpperCase(), FUENTE_TITULO);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        pTitulo.setSpacingBefore(4);
        document.add(pTitulo);

        Paragraph pFecha = new Paragraph("Fecha: " + LocalDateTime.now().format(FORMATO_FECHA), FUENTE_SUBTITULO);
        pFecha.setAlignment(Element.ALIGN_CENTER);
        pFecha.setSpacingAfter(15);
        document.add(pFecha);
    }

    private PdfPCell crearCeldaEncabezado(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_TH));
        celda.setBackgroundColor(COLOR_PRIMARIO);
        celda.setPadding(6);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celda;
    }

    private PdfPCell crearCeldaDato(String texto, int alineacion, boolean alternar) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_TD));
        if (alternar) {
            celda.setBackgroundColor(COLOR_SECUNDARIO);
        }
        celda.setPadding(5);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celda;
    }

    public void generarReporteVentasPeriodo(OutputStream os, LocalDateTime inicio, LocalDateTime fin) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, os);
        document.open();

        String rango = inicio.format(FORMATO_FECHA) + " al " + fin.format(FORMATO_FECHA);
        agregarEncabezado(document, "Reporte de Ventas por Período (" + rango + ")");

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{30, 20, 25, 25});

        tabla.addCell(crearCeldaEncabezado("Método de Pago"));
        tabla.addCell(crearCeldaEncabezado("Operaciones"));
        tabla.addCell(crearCeldaEncabezado("Total Recaudado"));
        tabla.addCell(crearCeldaEncabezado("Total Cambio"));

        List<Map<String, Object>> datos = reporteDAO.obtenerVentasPorPeriodo(inicio, fin);
        int totalOps = 0;
        double totalRec = 0.0;
        double totalCam = 0.0;
        boolean alternar = false;

        for (Map<String, Object> fila : datos) {
            String metodo = String.valueOf(fila.get("metodo"));
            int ops = (int) fila.get("operaciones");
            double rec = (double) fila.get("recaudado");
            double cam = (double) fila.get("cambio");

            totalOps += ops;
            totalRec += rec;
            totalCam += cam;

            tabla.addCell(crearCeldaDato(metodo, Element.ALIGN_LEFT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(ops), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.format("Q %.2f", rec), Element.ALIGN_RIGHT, alternar));
            tabla.addCell(crearCeldaDato(String.format("Q %.2f", cam), Element.ALIGN_RIGHT, alternar));
            alternar = !alternar;
        }

        PdfPCell celdaTotLabel = new PdfPCell(new Phrase("TOTAL GENERAL", FUENTE_TOTAL));
        celdaTotLabel.setPadding(6);
        celdaTotLabel.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTotLabel);

        PdfPCell celdaTotOps = new PdfPCell(new Phrase(String.valueOf(totalOps), FUENTE_TOTAL));
        celdaTotOps.setHorizontalAlignment(Element.ALIGN_CENTER);
        celdaTotOps.setPadding(6);
        celdaTotOps.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTotOps);

        PdfPCell celdaTotRec = new PdfPCell(new Phrase(String.format("Q %.2f", totalRec), FUENTE_TOTAL));
        celdaTotRec.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTotRec.setPadding(6);
        celdaTotRec.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTotRec);

        PdfPCell celdaTotCam = new PdfPCell(new Phrase(String.format("Q %.2f", totalCam), FUENTE_TOTAL));
        celdaTotCam.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTotCam.setPadding(6);
        celdaTotCam.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTotCam);

        document.add(tabla);
        document.close();
    }

    public void generarReporteProductosMasVendidos(OutputStream os) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, os);
        document.open();

        agregarEncabezado(document, "Productos Más Vendidos (Incluye Componentes de Menú)");

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{20, 45, 20, 15});

        tabla.addCell(crearCeldaEncabezado("Código"));
        tabla.addCell(crearCeldaEncabezado("Producto"));
        tabla.addCell(crearCeldaEncabezado("Categoría"));
        tabla.addCell(crearCeldaEncabezado("Unidades"));

        List<Map<String, Object>> datos = reporteDAO.obtenerProductosMasVendidos();
        int totalUnidades = 0;
        boolean alternar = false;

        for (Map<String, Object> fila : datos) {
            int unidades = (int) fila.get("unidades");
            totalUnidades += unidades;

            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("codigo")), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("nombre")), Element.ALIGN_LEFT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("categoria")), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(unidades), Element.ALIGN_RIGHT, alternar));
            alternar = !alternar;
        }

        PdfPCell celdaTot = new PdfPCell(new Phrase("TOTAL UNIDADES VENDIDAS", FUENTE_TOTAL));
        celdaTot.setColspan(3);
        celdaTot.setPadding(6);
        celdaTot.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTot);

        PdfPCell celdaTotU = new PdfPCell(new Phrase(String.valueOf(totalUnidades), FUENTE_TOTAL));
        celdaTotU.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTotU.setPadding(6);
        celdaTotU.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTotU);

        document.add(tabla);
        document.close();
    }

    public void generarReporteStockBajo(OutputStream os) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, os);
        document.open();

        agregarEncabezado(document, "Productos Agotados o con Existencia Menor o Igual a 5");

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{15, 35, 18, 12, 10, 10});

        tabla.addCell(crearCeldaEncabezado("Código"));
        tabla.addCell(crearCeldaEncabezado("Nombre"));
        tabla.addCell(crearCeldaEncabezado("Categoría"));
        tabla.addCell(crearCeldaEncabezado("Precio"));
        tabla.addCell(crearCeldaEncabezado("Stock"));
        tabla.addCell(crearCeldaEncabezado("Estado"));

        List<Map<String, Object>> datos = reporteDAO.obtenerProductosStockBajo();
        boolean alternar = false;

        for (Map<String, Object> fila : datos) {
            double precio = (double) fila.get("precio");
            int stock = (int) fila.get("existencia");

            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("codigo")), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("nombre")), Element.ALIGN_LEFT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("categoria")), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.format("Q %.2f", precio), Element.ALIGN_RIGHT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(stock), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("estado")), Element.ALIGN_CENTER, alternar));
            alternar = !alternar;
        }

        document.add(tabla);
        document.close();
    }

    public void generarReportePuntosClientes(OutputStream os) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, os);
        document.open();

        agregarEncabezado(document, "Puntos Acumulados por Cliente");

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{15, 28, 15, 22, 10, 10});

        tabla.addCell(crearCeldaEncabezado("Identificador"));
        tabla.addCell(crearCeldaEncabezado("Cliente"));
        tabla.addCell(crearCeldaEncabezado("Teléfono"));
        tabla.addCell(crearCeldaEncabezado("Correo"));
        tabla.addCell(crearCeldaEncabezado("Puntos"));
        tabla.addCell(crearCeldaEncabezado("Estado"));

        List<Map<String, Object>> datos = reporteDAO.obtenerPuntosAcumuladosClientes();
        int totalPuntos = 0;
        boolean alternar = false;

        for (Map<String, Object> fila : datos) {
            int puntos = (int) fila.get("puntos");
            totalPuntos += puntos;

            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("identificador")), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("nombre")), Element.ALIGN_LEFT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("telefono") != null ? fila.get("telefono") : ""), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("correo") != null ? fila.get("correo") : ""), Element.ALIGN_LEFT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(puntos), Element.ALIGN_RIGHT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("estado")), Element.ALIGN_CENTER, alternar));
            alternar = !alternar;
        }

        PdfPCell celdaTot = new PdfPCell(new Phrase("TOTAL PUNTOS VIGENTES", FUENTE_TOTAL));
        celdaTot.setColspan(4);
        celdaTot.setPadding(6);
        celdaTot.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTot);

        PdfPCell celdaTotP = new PdfPCell(new Phrase(String.valueOf(totalPuntos), FUENTE_TOTAL));
        celdaTotP.setColspan(2);
        celdaTotP.setHorizontalAlignment(Element.ALIGN_CENTER);
        celdaTotP.setPadding(6);
        celdaTotP.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTotP);

        document.add(tabla);
        document.close();
    }

    public void generarReporteHistorialCanjes(OutputStream os) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, os);
        document.open();

        agregarEncabezado(document, "Historial General de Canjes Realizados");

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{10, 15, 25, 25, 10, 15});

        tabla.addCell(crearCeldaEncabezado("ID"));
        tabla.addCell(crearCeldaEncabezado("Identificador"));
        tabla.addCell(crearCeldaEncabezado("Cliente"));
        tabla.addCell(crearCeldaEncabezado("Recompensa"));
        tabla.addCell(crearCeldaEncabezado("Puntos"));
        tabla.addCell(crearCeldaEncabezado("Fecha"));

        List<Map<String, Object>> datos = reporteDAO.obtenerHistorialCanjes();
        int totalPuntosCanjeados = 0;
        boolean alternar = false;

        for (Map<String, Object> fila : datos) {
            int puntos = (int) fila.get("puntos");
            totalPuntosCanjeados += puntos;

            tabla.addCell(crearCeldaDato("#" + fila.get("idCanje"), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("identificador")), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("cliente")), Element.ALIGN_LEFT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("recompensa")), Element.ALIGN_LEFT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(puntos), Element.ALIGN_RIGHT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("fecha")), Element.ALIGN_CENTER, alternar));
            alternar = !alternar;
        }

        PdfPCell celdaTot = new PdfPCell(new Phrase("TOTAL PUNTOS CANJEADOS", FUENTE_TOTAL));
        celdaTot.setColspan(4);
        celdaTot.setPadding(6);
        celdaTot.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTot);

        PdfPCell celdaTotP = new PdfPCell(new Phrase(String.valueOf(totalPuntosCanjeados), FUENTE_TOTAL));
        celdaTotP.setColspan(2);
        celdaTotP.setHorizontalAlignment(Element.ALIGN_CENTER);
        celdaTotP.setPadding(6);
        celdaTotP.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTotP);

        document.add(tabla);
        document.close();
    }

    public void generarReporteVentasMenus(OutputStream os) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, os);
        document.open();

        agregarEncabezado(document, "Ventas de Menús Completos");

        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{20, 35, 15, 15, 15});

        tabla.addCell(crearCeldaEncabezado("Código"));
        tabla.addCell(crearCeldaEncabezado("Nombre Menú"));
        tabla.addCell(crearCeldaEncabezado("Precio Catálogo"));
        tabla.addCell(crearCeldaEncabezado("Unidades"));
        tabla.addCell(crearCeldaEncabezado("Total Recaudado"));

        List<Map<String, Object>> datos = reporteDAO.obtenerVentasMenusCompletos();
        int totalU = 0;
        double totalRec = 0.0;
        boolean alternar = false;

        for (Map<String, Object> fila : datos) {
            double precio = (double) fila.get("precio");
            int unidades = (int) fila.get("unidades");
            double total = (double) fila.get("total");

            totalU += unidades;
            totalRec += total;

            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("codigo")), Element.ALIGN_CENTER, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(fila.get("nombre")), Element.ALIGN_LEFT, alternar));
            tabla.addCell(crearCeldaDato(String.format("Q %.2f", precio), Element.ALIGN_RIGHT, alternar));
            tabla.addCell(crearCeldaDato(String.valueOf(unidades), Element.ALIGN_RIGHT, alternar));
            tabla.addCell(crearCeldaDato(String.format("Q %.2f", total), Element.ALIGN_RIGHT, alternar));
            alternar = !alternar;
        }

        PdfPCell celdaTot = new PdfPCell(new Phrase("TOTAL GENERAL MENÚS", FUENTE_TOTAL));
        celdaTot.setColspan(3);
        celdaTot.setPadding(6);
        celdaTot.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTot);

        PdfPCell celdaTotU = new PdfPCell(new Phrase(String.valueOf(totalU), FUENTE_TOTAL));
        celdaTotU.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTotU.setPadding(6);
        celdaTotU.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTotU);

        PdfPCell celdaTotR = new PdfPCell(new Phrase(String.format("Q %.2f", totalRec), FUENTE_TOTAL));
        celdaTotR.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTotR.setPadding(6);
        celdaTotR.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(celdaTotR);

        document.add(tabla);
        document.close();
    }
}
