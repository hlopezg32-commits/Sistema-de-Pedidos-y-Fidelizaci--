
package servicio;

import modelo.Cliente;
import modelo.Producto;
import modelo.PedidoDetalle;
import java.sql.SQLException;
import java.util.List;

public class VentaServicio {

    private final ClienteServicio clienteServicio;
    private final ProductoServicio productoServicio;
    private final PedidoServicio pedidoServicio;

    public VentaServicio() {
        clienteServicio = new ClienteServicio();
        productoServicio = new ProductoServicio();
        pedidoServicio = new PedidoServicio();
    }

    public Cliente buscarClienteActivo(String identificador) {
        Cliente cliente =
                clienteServicio.buscarClientePorIdentificador(
                        identificador
                );

        if (cliente == null
                || !"ACTIVO".equalsIgnoreCase(cliente.getEstado())) {
            return null;
        }

        return cliente;
    }

    public List<Producto> listarProductosActivos() {
        return productoServicio.listarProductosActivos();
    }

    public int registrarVenta(
            int idCliente,
            List<PedidoDetalle> detalles,
            String metodoPago,
            double montoRecibido,
            String referencia,
            boolean tarjetaAprobada) throws SQLException {

        return pedidoServicio.registrarVenta(
                idCliente,
                detalles,
                metodoPago,
                montoRecibido,
                referencia,
                tarjetaAprobada
        );
    }
}
