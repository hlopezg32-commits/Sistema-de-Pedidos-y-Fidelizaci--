package servicio;

import dao.impl.CanjeDAO;
import dao.impl.ClienteDAO;
import dao.impl.MenuComponenteDAO;
import dao.impl.MenuDAO;
import dao.impl.MovimientoInventarioDAO;
import dao.impl.MovimientoPuntosDAO;
import dao.impl.ProductoDAO;
import dao.impl.RecompensaDAO;
import modelo.Canje;
import modelo.Cliente;
import modelo.Menu;
import modelo.MenuComponente;
import modelo.MovimientoInventario;
import modelo.MovimientoPuntos;
import modelo.Producto;
import modelo.Recompensa;
import modelo.ResultadoCanje;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FidelizacionService {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final RecompensaDAO recompensaDAO = new RecompensaDAO();
    private final MovimientoPuntosDAO movimientoPuntosDAO = new MovimientoPuntosDAO();
    private final CanjeDAO canjeDAO = new CanjeDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final MenuComponenteDAO menuComponenteDAO = new MenuComponenteDAO();
    private final MovimientoInventarioDAO movimientoInventarioDAO = new MovimientoInventarioDAO();

    public int calcularPuntos(int sandwiches, int menus) {
        int totalSandwiches = Math.max(0, sandwiches);
        int totalMenus = Math.max(0, menus);
        return (totalSandwiches * 2) + (totalMenus * 8);
    }

    public boolean acumularPuntosPorPedido(int idCliente, int idPedido, int sandwiches, int menus) {
        int puntos = calcularPuntos(sandwiches, menus);
        if (puntos <= 0) {
            return false;
        }

        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null || !"ACTIVO".equalsIgnoreCase(cliente.getEstado())) {
            return false;
        }

        int nuevoSaldo = cliente.getPuntos() + puntos;
        boolean actualizado = clienteDAO.actualizarPuntos(idCliente, nuevoSaldo);
        if (!actualizado) {
            return false;
        }

        MovimientoPuntos movimiento = new MovimientoPuntos(
                0,
                idCliente,
                idPedido,
                "ACUMULACION",
                puntos,
                null
        );
        return movimientoPuntosDAO.insertar(movimiento);
    }

    public ResultadoCanje procesarCanje(int idCliente, int idRecompensa) {
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) {
            return new ResultadoCanje(false, "El cliente no existe.", 0, 0);
        }

        if (!"ACTIVO".equalsIgnoreCase(cliente.getEstado())) {
            return new ResultadoCanje(false, "El cliente no está activo.", 0, cliente.getPuntos());
        }

        Recompensa recompensa = recompensaDAO.buscarPorId(idRecompensa);
        if (recompensa == null) {
            return new ResultadoCanje(false, "La recompensa no existe.", 0, cliente.getPuntos());
        }

        if (!"ACTIVO".equalsIgnoreCase(recompensa.getEstado()) && !"ACTIVA".equalsIgnoreCase(recompensa.getEstado())) {
            return new ResultadoCanje(false, "La recompensa no está activa.", 0, cliente.getPuntos());
        }

        if (cliente.getPuntos() < recompensa.getCostoPuntos()) {
            return new ResultadoCanje(false, "Puntos insuficientes para este canje.", 0, cliente.getPuntos());
        }

        if (recompensa.getIdProducto() != null) {
            Producto producto = productoDAO.buscarPorId(recompensa.getIdProducto());
            if (producto == null || !"ACTIVO".equalsIgnoreCase(producto.getEstado())) {
                return new ResultadoCanje(false, "El producto no está disponible.", 0, cliente.getPuntos());
            }

            if (producto.getExistencia() < 1) {
                return new ResultadoCanje(false, "No hay existencias de: " + producto.getNombre(), 0, cliente.getPuntos());
            }

            productoDAO.actualizarExistencia(producto.getIdProducto(), producto.getExistencia() - 1);
            MovimientoInventario movInv = new MovimientoInventario(
                    0,
                    producto.getIdProducto(),
                    "CANJE",
                    1,
                    null,
                    "Canje de recompensa: " + recompensa.getNombre()
            );
            movimientoInventarioDAO.insertar(movInv);

        } else if (recompensa.getIdMenu() != null) {
            Menu menu = menuDAO.buscarPorId(recompensa.getIdMenu());
            if (menu == null || !"ACTIVO".equalsIgnoreCase(menu.getEstado())) {
                return new ResultadoCanje(false, "El menú no está disponible.", 0, cliente.getPuntos());
            }

            List<MenuComponente> componentes = menuComponenteDAO.listarPorMenu(recompensa.getIdMenu());
            if (componentes.isEmpty()) {
                return new ResultadoCanje(false, "El menú no tiene componentes.", 0, cliente.getPuntos());
            }

            for (MenuComponente mc : componentes) {
                Producto compProd = productoDAO.buscarPorId(mc.getIdProducto());
                if (compProd == null || compProd.getExistencia() < mc.getCantidad()) {
                    String nombreComp = compProd != null ? compProd.getNombre() : "ID " + mc.getIdProducto();
                    return new ResultadoCanje(false, "No hay existencia del componente: " + nombreComp, 0, cliente.getPuntos());
                }
            }

            for (MenuComponente mc : componentes) {
                Producto compProd = productoDAO.buscarPorId(mc.getIdProducto());
                productoDAO.actualizarExistencia(compProd.getIdProducto(), compProd.getExistencia() - mc.getCantidad());
                MovimientoInventario movInv = new MovimientoInventario(
                        0,
                        compProd.getIdProducto(),
                        "CANJE",
                        mc.getCantidad(),
                        null,
                        "Canje de componente de menú: " + recompensa.getNombre()
                );
                movimientoInventarioDAO.insertar(movInv);
            }
        }

        int nuevoSaldo = cliente.getPuntos() - recompensa.getCostoPuntos();
        clienteDAO.actualizarPuntos(cliente.getIdCliente(), nuevoSaldo);

        Canje canje = new Canje(
                0,
                cliente.getIdCliente(),
                recompensa.getIdRecompensa(),
                recompensa.getCostoPuntos(),
                null
        );
        canjeDAO.insertar(canje);

        MovimientoPuntos movPuntos = new MovimientoPuntos(
                0,
                cliente.getIdCliente(),
                null,
                "CANJE",
                recompensa.getCostoPuntos(),
                null
        );
        movimientoPuntosDAO.insertar(movPuntos);

        return new ResultadoCanje(true, "Canje realizado correctamente.", recompensa.getCostoPuntos(), nuevoSaldo);
    }

    public List<Recompensa> listarRecompensasActivas() {
        return recompensaDAO.listarActivas();
    }

    public Recompensa buscarRecompensa(int idRecompensa) {
        return recompensaDAO.buscarPorId(idRecompensa);
    }

    public Cliente consultarCliente(int idCliente) {
        return clienteDAO.buscarPorId(idCliente);
    }

    public Cliente consultarClientePorIdentificador(String identificador) {
        return clienteDAO.buscarPorIdentificador(identificador);
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listarTodos();
    }

    public List<MovimientoPuntos> listarMovimientosPorCliente(int idCliente) {
        return movimientoPuntosDAO.listarPorCliente(idCliente);
    }

    public List<Canje> listarCanjesPorCliente(int idCliente) {
        return canjeDAO.listarPorCliente(idCliente);
    }
}
