package servicio;

import dao.impl.ClienteDAO;
import dao.impl.MenuComponenteDAO;
import dao.impl.MenuDAO;
import dao.impl.MovimientoInventarioDAO;
import dao.impl.PagoDAO;
import dao.impl.PedidoDAO;
import dao.impl.PedidoDetalleDAO;
import dao.impl.ProductoDAO;
import modelo.Cliente;
import modelo.ItemPedidoDTO;
import modelo.Menu;
import modelo.MenuComponente;
import modelo.MovimientoInventario;
import modelo.Pago;
import modelo.Pedido;
import modelo.PedidoDetalle;
import modelo.Producto;
import modelo.ResultadoVenta;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaServicio {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final MenuComponenteDAO menuComponenteDAO = new MenuComponenteDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final PedidoDetalleDAO pedidoDetalleDAO = new PedidoDetalleDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final MovimientoInventarioDAO movimientoInventarioDAO = new MovimientoInventarioDAO();
    private final FidelizacionService fidelizacionService;

    public VentaServicio(FidelizacionService fidelizacionService) {
        this.fidelizacionService = fidelizacionService;
    }

    public List<Producto> listarProductosParaVenta() {
        return productoDAO.listarActivos();
    }

    public List<Menu> listarMenusParaVenta() {
        return menuDAO.listarActivos();
    }

    public List<Cliente> listarClientesParaVenta() {
        return clienteDAO.listarTodos();
    }

    public List<Pedido> listarPedidosRecientes() {
        return pedidoDAO.listarTodos();
    }

    public ResultadoVenta registrarVenta(int idCliente, List<ItemPedidoDTO> items, String metodoPago, double montoRecibido, String referencia) {
        if (items == null || items.isEmpty()) {
            return new ResultadoVenta(false, "El pedido no contiene ningun producto o menu.");
        }

        Cliente cliente = null;
        if (idCliente > 0) {
            cliente = clienteDAO.buscarPorId(idCliente);
            if (cliente == null || !"ACTIVO".equalsIgnoreCase(cliente.getEstado())) {
                return new ResultadoVenta(false, "El cliente seleccionado no existe o esta inactivo.");
            }
        }

        double total = 0.0;
        int sandwiches = 0;
        int menus = 0;

        for (ItemPedidoDTO item : items) {
            if (item.getCantidad() <= 0) {
                return new ResultadoVenta(false, "La cantidad de " + item.getNombre() + " debe ser mayor a cero.");
            }

            if ("PRODUCTO".equalsIgnoreCase(item.getTipo())) {
                Producto prod = productoDAO.buscarPorId(item.getId());
                if (prod == null || !"ACTIVO".equalsIgnoreCase(prod.getEstado())) {
                    return new ResultadoVenta(false, "El producto seleccionado no esta disponible.");
                }
                if (prod.getExistencia() < item.getCantidad()) {
                    return new ResultadoVenta(false, "Existencias insuficientes para: " + prod.getNombre() + " (Disponibles: " + prod.getExistencia() + ")");
                }
                item.setPrecioUnitario(prod.getPrecio());
                item.setNombre(prod.getNombre());
                total += prod.getPrecio() * item.getCantidad();

                if ("SANDWICH".equalsIgnoreCase(prod.getCategoria())) {
                    sandwiches += item.getCantidad();
                }

            } else if ("MENU".equalsIgnoreCase(item.getTipo())) {
                Menu menu = menuDAO.buscarPorId(item.getId());
                if (menu == null || !"ACTIVO".equalsIgnoreCase(menu.getEstado())) {
                    return new ResultadoVenta(false, "El menu seleccionado no esta disponible.");
                }

                List<MenuComponente> componentes = menuComponenteDAO.listarPorMenu(menu.getIdMenu());
                if (componentes.isEmpty()) {
                    return new ResultadoVenta(false, "El menu '" + menu.getNombre() + "' no tiene componentes configurados.");
                }

                for (MenuComponente mc : componentes) {
                    Producto compProd = productoDAO.buscarPorId(mc.getIdProducto());
                    int requerida = mc.getCantidad() * item.getCantidad();
                    if (compProd == null || compProd.getExistencia() < requerida) {
                        String nom = compProd != null ? compProd.getNombre() : "ID " + mc.getIdProducto();
                        return new ResultadoVenta(false, "No hay existencias suficientes del componente: " + nom + " para el menu " + menu.getNombre());
                    }
                }

                item.setPrecioUnitario(menu.getPrecio());
                item.setNombre(menu.getNombre());
                total += menu.getPrecio() * item.getCantidad();
                menus += item.getCantidad();
            } else {
                return new ResultadoVenta(false, "Tipo de articulo no valido.");
            }
        }

        total = Math.round(total * 100.0) / 100.0;
        double cambio = 0.0;

        if ("EFECTIVO".equalsIgnoreCase(metodoPago)) {
            if (montoRecibido < total) {
                return new ResultadoVenta(false, "El monto recibido (Q" + String.format("%.2f", montoRecibido) + ") es menor al total a pagar (Q" + String.format("%.2f", total) + ").");
            }
            cambio = Math.round((montoRecibido - total) * 100.0) / 100.0;
        } else {
            montoRecibido = total;
            cambio = 0.0;
        }

        Pedido pedido = new Pedido(0, idCliente > 0 ? idCliente : 1, null, "PAGADO", total);
        boolean pedidoCreado = pedidoDAO.insertar(pedido);
        if (!pedidoCreado || pedido.getIdPedido() == 0) {
            return new ResultadoVenta(false, "No se pudo registrar el pedido en el sistema.");
        }

        for (ItemPedidoDTO item : items) {
            if ("PRODUCTO".equalsIgnoreCase(item.getTipo())) {
                PedidoDetalle det = new PedidoDetalle(0, pedido.getIdPedido(), item.getId(), null, item.getCantidad(), item.getPrecioUnitario());
                pedidoDetalleDAO.insertar(det);

                Producto prod = productoDAO.buscarPorId(item.getId());
                productoDAO.actualizarExistencia(prod.getIdProducto(), prod.getExistencia() - item.getCantidad());

                MovimientoInventario mov = new MovimientoInventario(0, prod.getIdProducto(), "VENTA", item.getCantidad(), null, "Venta en Pedido #" + pedido.getIdPedido());
                movimientoInventarioDAO.insertar(mov);

            } else if ("MENU".equalsIgnoreCase(item.getTipo())) {
                PedidoDetalle det = new PedidoDetalle(0, pedido.getIdPedido(), null, item.getId(), item.getCantidad(), item.getPrecioUnitario());
                pedidoDetalleDAO.insertar(det);

                List<MenuComponente> componentes = menuComponenteDAO.listarPorMenu(item.getId());
                for (MenuComponente mc : componentes) {
                    Producto compProd = productoDAO.buscarPorId(mc.getIdProducto());
                    int cantDescontar = mc.getCantidad() * item.getCantidad();
                    productoDAO.actualizarExistencia(compProd.getIdProducto(), compProd.getExistencia() - cantDescontar);

                    MovimientoInventario mov = new MovimientoInventario(0, compProd.getIdProducto(), "VENTA", cantDescontar, null, "Venta menu " + item.getNombre() + " en Pedido #" + pedido.getIdPedido());
                    movimientoInventarioDAO.insertar(mov);
                }
            }
        }

        Pago pago = new Pago(0, pedido.getIdPedido(), metodoPago.toUpperCase(), montoRecibido, cambio, "APROBADO", referencia, null);
        pagoDAO.insertar(pago);

        int puntosGanados = 0;
        int nuevoSaldo = 0;
        if (idCliente > 0) {
            fidelizacionService.acumularPuntosPorPedido(idCliente, pedido.getIdPedido(), sandwiches, menus);
            Cliente clActualizado = clienteDAO.buscarPorId(idCliente);
            if (clActualizado != null) {
                nuevoSaldo = clActualizado.getPuntos();
            }
            puntosGanados = fidelizacionService.calcularPuntos(sandwiches, menus);
        }

        String nombreCliente = (cliente != null) ? cliente.getNombre() : "Consumidor Final";

        return new ResultadoVenta(true, "Venta realizada exitosamente.", pedido.getIdPedido(), total, montoRecibido, cambio, puntosGanados, nuevoSaldo, nombreCliente, metodoPago.toUpperCase());
    }
}
