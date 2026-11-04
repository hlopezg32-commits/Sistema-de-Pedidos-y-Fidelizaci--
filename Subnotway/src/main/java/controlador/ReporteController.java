package controlador;

import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import servicio.ReportePdfService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReportePdfService reportePdfService;

    public ReporteController(ReportePdfService reportePdfService) {
        this.reportePdfService = reportePdfService;
    }

    @GetMapping
    public String verCentroReportes(Model model) {
        model.addAttribute("fechaInicioPredeterminada", LocalDate.now().minusDays(30));
        model.addAttribute("fechaFinPredeterminada", LocalDate.now());
        return "reportes";
    }

    @GetMapping("/pdf/ventas")
    public void exportarVentasPorPeriodo(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response) throws IOException, DocumentException {

        LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.now().minusDays(30);
        LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

        LocalDateTime dtInicio = inicio.atStartOfDay();
        LocalDateTime dtFin = fin.atTime(LocalTime.MAX);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Reporte_Ventas_Periodo.pdf");
        reportePdfService.generarReporteVentasPeriodo(response.getOutputStream(), dtInicio, dtFin);
    }

    @GetMapping("/pdf/mas-vendidos")
    public void exportarProductosMasVendidos(HttpServletResponse response) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Reporte_Productos_Mas_Vendidos.pdf");
        reportePdfService.generarReporteProductosMasVendidos(response.getOutputStream());
    }

    @GetMapping({"/pdf/stock-bajo", "/pdf/stock-critico"})
    public void exportarProductosStockBajo(HttpServletResponse response) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Reporte_Stock_Critico.pdf");
        reportePdfService.generarReporteStockBajo(response.getOutputStream());
    }

    @GetMapping("/pdf/puntos-clientes")
    public void exportarPuntosClientes(HttpServletResponse response) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Reporte_Puntos_Clientes.pdf");
        reportePdfService.generarReportePuntosClientes(response.getOutputStream());
    }

    @GetMapping("/pdf/canjes")
    public void exportarHistorialCanjes(HttpServletResponse response) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Reporte_Historial_Canjes.pdf");
        reportePdfService.generarReporteHistorialCanjes(response.getOutputStream());
    }

    @GetMapping("/pdf/ventas-menus")
    public void exportarVentasMenus(HttpServletResponse response) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Reporte_Ventas_Menus_Completos.pdf");
        reportePdfService.generarReporteVentasMenus(response.getOutputStream());
    }
}
