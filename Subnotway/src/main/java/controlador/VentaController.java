package controlador;

import dao.impl.ClienteDAO;
import dao.impl.PagoDAO;
import dao.impl.PedidoDAO;
import dao.impl.PedidoDetalleDAO;
import modelo.ItemPedidoDTO;
import modelo.Pago;
import modelo.Pedido;
import modelo.PedidoDetalle;
import modelo.ResultadoVenta;
import servicio.VentaServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final VentaServicio ventaServicio;
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final PedidoDetalleDAO pedidoDetalleDAO = new PedidoDetalleDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    public VentaController(VentaServicio ventaServicio) {
        this.ventaServicio = ventaServicio;
    }

    @GetMapping
    public String nuevaVenta(Model model) {
        model.addAttribute("clientes", ventaServicio.listarClientesParaVenta());
        model.addAttribute("productos", ventaServicio.listarProductosParaVenta());
        model.addAttribute("menus", ventaServicio.listarMenusParaVenta());
        model.addAttribute("pedidosRecientes", ventaServicio.listarPedidosRecientes());
        return "ventas";
    }

    @PostMapping("/guardar")
    public String guardarVenta(
            @RequestParam int idCliente,
            @RequestParam(name = "itemTipo", required = false) List<String> tipos,
            @RequestParam(name = "itemId", required = false) List<Integer> ids,
            @RequestParam(name = "itemCantidad", required = false) List<Integer> cantidades,
            @RequestParam String metodoPago,
            @RequestParam double montoRecibido,
            @RequestParam(required = false) String referencia,
            RedirectAttributes ra) {

        if (tipos == null || ids == null || cantidades == null || tipos.isEmpty()) {
            ra.addFlashAttribute("error", "Debes agregar al menos un producto o menu al pedido.");
            return "redirect:/ventas";
        }

        List<ItemPedidoDTO> items = new ArrayList<>();
        for (int i = 0; i < tipos.size(); i++) {
            int cant = (i < cantidades.size() && cantidades.get(i) != null) ? cantidades.get(i) : 0;
            if (cant > 0) {
                items.add(new ItemPedidoDTO(tipos.get(i), ids.get(i), "", cant, 0.0));
            }
        }

        if (items.isEmpty()) {
            ra.addFlashAttribute("error", "Debes indicar una cantidad valida mayor a 0.");
            return "redirect:/ventas";
        }

        ResultadoVenta resultado = ventaServicio.registrarVenta(idCliente, items, metodoPago, montoRecibido, referencia);

        if (resultado.isExitoso()) {
            ra.addFlashAttribute("mensaje", resultado.getMensaje());
            ra.addFlashAttribute("resultadoVenta", resultado);
            return "redirect:/ventas/comprobante/" + resultado.getIdPedido();
        } else {
            ra.addFlashAttribute("error", resultado.getMensaje());
            return "redirect:/ventas";
        }
    }

    @GetMapping("/comprobante/{idPedido}")
    public String verComprobante(@PathVariable int idPedido, Model model, RedirectAttributes ra) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido);
        if (pedido == null) {
            ra.addFlashAttribute("error", "El pedido no fue encontrado.");
            return "redirect:/ventas";
        }

        List<PedidoDetalle> detalles = pedidoDetalleDAO.listarPorPedido(idPedido);
        List<Pago> pagos = pagoDAO.listarPorPedido(idPedido);
        Pago pago = pagos.isEmpty() ? null : pagos.get(0);

        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", clienteDAO.buscarPorId(pedido.getIdCliente()));
        model.addAttribute("detalles", detalles);
        model.addAttribute("pago", pago);

        return "ventas_comprobante";
    }

    @PostMapping("/entregar/{idPedido}")
    public String entregarPedido(@PathVariable int idPedido, RedirectAttributes ra) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido);
        if (pedido == null) {
            ra.addFlashAttribute("error", "El pedido no fue encontrado.");
            return "redirect:/ventas";
        }

        if (!"PAGADO".equalsIgnoreCase(pedido.getEstado())) {
            ra.addFlashAttribute("error", "Solo los pedidos pagados pueden ser marcados como entregados.");
            return "redirect:/ventas";
        }

        boolean ok = pedidoDAO.cambiarEstado(idPedido, "ENTREGADO");
        if (ok) {
            ra.addFlashAttribute("mensaje", "Pedido #" + idPedido + " marcado como ENTREGADO.");
        } else {
            ra.addFlashAttribute("error", "No se pudo actualizar el estado del pedido.");
        }

        return "redirect:/ventas";
    }

    @GetMapping("/historial")
    public String historialVentas(Model model) {
        model.addAttribute("pedidos", pedidoDAO.listarTodos());
        return "ventas";
    }
}
