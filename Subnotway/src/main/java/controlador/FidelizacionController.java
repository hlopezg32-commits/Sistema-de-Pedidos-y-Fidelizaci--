package controlador;

import dao.impl.PedidoDAO;
import modelo.Canje;
import modelo.Cliente;
import modelo.MovimientoPuntos;
import modelo.Pedido;
import modelo.Recompensa;
import modelo.ResultadoCanje;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import servicio.FidelizacionService;

import java.util.List;

@Controller
@RequestMapping("/fidelizacion")
public class FidelizacionController {

    private final FidelizacionService fidelizacionService;
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    public FidelizacionController(FidelizacionService fidelizacionService) {
        this.fidelizacionService = fidelizacionService;
    }

    @GetMapping
    public String verFidelizacion(
            @RequestParam(name = "identificador", required = false) String identificador,
            @RequestParam(name = "idCliente", required = false) Integer idCliente,
            Model model) {

        List<Cliente> clientes = fidelizacionService.listarClientes();
        model.addAttribute("clientes", clientes);

        List<Recompensa> recompensas = fidelizacionService.listarRecompensasActivas();
        model.addAttribute("recompensas", recompensas);

        Cliente clienteSeleccionado = null;
        if (identificador != null && !identificador.trim().isEmpty()) {
            clienteSeleccionado = fidelizacionService.consultarClientePorIdentificador(identificador.trim());
        } else if (idCliente != null && idCliente > 0) {
            clienteSeleccionado = fidelizacionService.consultarCliente(idCliente);
        } else if (!clientes.isEmpty()) {
            clienteSeleccionado = clientes.get(0);
        }

        model.addAttribute("clienteSeleccionado", clienteSeleccionado);

        if (clienteSeleccionado != null) {
            List<MovimientoPuntos> movimientos = fidelizacionService.listarMovimientosPorCliente(clienteSeleccionado.getIdCliente());
            List<Canje> canjes = fidelizacionService.listarCanjesPorCliente(clienteSeleccionado.getIdCliente());
            List<Pedido> pedidosCliente = pedidoDAO.listarPorCliente(clienteSeleccionado.getIdCliente());
            model.addAttribute("movimientos", movimientos);
            model.addAttribute("canjes", canjes);
            model.addAttribute("pedidosCliente", pedidosCliente);
        }

        return "fidelizacion";
    }

    @PostMapping("/canjear")
    public String canjearRecompensa(
            @RequestParam("idCliente") int idCliente,
            @RequestParam("idRecompensa") int idRecompensa,
            RedirectAttributes redirectAttributes) {

        ResultadoCanje resultado = fidelizacionService.procesarCanje(idCliente, idRecompensa);

        if (resultado.isExitoso()) {
            redirectAttributes.addFlashAttribute("mensajeExito", resultado.getMensaje());
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", resultado.getMensaje());
        }

        return "redirect:/fidelizacion?idCliente=" + idCliente;
    }

    @PostMapping("/acumular")
    public String acumularPuntos(
            @RequestParam("idCliente") int idCliente,
            @RequestParam(name = "idPedido", defaultValue = "0") int idPedido,
            @RequestParam("sandwiches") int sandwiches,
            @RequestParam("menus") int menus,
            RedirectAttributes redirectAttributes) {

        int puntos = fidelizacionService.calcularPuntos(sandwiches, menus);
        boolean exito = fidelizacionService.acumularPuntosPorPedido(idCliente, idPedido > 0 ? idPedido : 1, sandwiches, menus);

        if (exito) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Puntos acumulados correctamente (+ " + puntos + " pts)");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudieron acumular puntos. Verifique el cliente y las cantidades.");
        }

        return "redirect:/fidelizacion?idCliente=" + idCliente;
    }
}
