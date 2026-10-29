package servicio;

import config.ConexionDB;
import modelo.PedidoDetalle;
import modelo.PedidoDetalleExtra;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoServicio {

    public int registrarVenta(
            int idCliente,
            List<PedidoDetalle> detalles,
            String metodoPago,
            double montoRecibido,
            String referencia,
            boolean tarjetaAprobada) throws SQLException {

        validarEntrada(detalles, metodoPago, montoRecibido);

        if ("TARJETA".equals(metodoPago) && !tarjetaAprobada) {
            throw new IllegalArgumentException(
                    "El pago con tarjeta fue rechazado.");
        }

        try (Connection conexion = ConexionDB.conectar()) {
            if (conexion == null || conexion.isClosed()) {
                throw new SQLException(
                        "No se pudo conectar a la base de datos.");
            }

            boolean autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            try {
                validarClienteActivo(conexion, idCliente);

                double total = 0;
                int puntosGanados = 0;
                Map<Integer, Integer> consumo = new LinkedHashMap<>();

                for (PedidoDetalle detalle : detalles) {
                    validarDetalle(detalle);

                    int cantidad = detalle.getCantidad();

                    if (detalle.getIdProducto() != null) {
                        ProductoInfo producto = buscarProductoActivo(
                                conexion, detalle.getIdProducto(), false);

                        if (producto == null) {
                            throw new IllegalArgumentException(
                                    "El producto no existe o está inactivo: "
                                    + detalle.getIdProducto());
                        }

                        detalle.setPrecioUnitario(producto.precio);
                        total += producto.precio * cantidad;

                        sumarConsumo(
                                consumo,
                                producto.id,
                                cantidad);

                        if ("SANDWICH".equals(producto.categoria)) {
                            puntosGanados = Math.addExact(
                                    puntosGanados,
                                    Math.multiplyExact(cantidad, 2));
                        }

                    } else {
                        MenuInfo menu = buscarMenuActivo(
                                conexion, detalle.getIdMenu());

                        if (menu == null) {
                            throw new IllegalArgumentException(
                                    "El elemento del menú no existe o está "
                                    + "inactivo: "
                                    + detalle.getIdMenu());
                        }

                        detalle.setPrecioUnitario(menu.precio);
                        total += menu.precio * cantidad;

                        agregarConsumoMenu(
                                conexion,
                                menu.id,
                                cantidad,
                                consumo);

                        puntosGanados = Math.addExact(
                                puntosGanados,
                                Math.multiplyExact(
                                        puntosPorMenu(conexion, menu.id),
                                        cantidad));
                    }

                    List<PedidoDetalleExtra> extras = detalle.getExtras();

                    if (extras == null) {
                        continue;
                    }

                    for (PedidoDetalleExtra extra : extras) {
                        if (extra == null
                                || extra.getIdProducto() <= 0
                                || extra.getCantidad() <= 0) {
                            throw new IllegalArgumentException(
                                    "Cada extra debe tener un producto "
                                    + "válido y una cantidad positiva.");
                        }

                        ProductoInfo productoExtra = buscarProductoActivo(
                                conexion,
                                extra.getIdProducto(),
                                false);

                        if (productoExtra == null) {
                            throw new IllegalArgumentException(
                                    "El producto extra no existe o está "
                                    + "inactivo: "
                                    + extra.getIdProducto());
                        }

                        extra.setPrecioUnitario(productoExtra.precio);

                        int cantidadExtraTotal = Math.multiplyExact(
                                cantidad,
                                extra.getCantidad());

                        total += productoExtra.precio * cantidadExtraTotal;

                        sumarConsumo(
                                consumo,
                                productoExtra.id,
                                cantidadExtraTotal);
                    }
                }

                if (!Double.isFinite(total) || total <= 0) {
                    throw new IllegalArgumentException(
                            "El total del pedido debe ser mayor que cero.");
                }

                double cambio = 0;

                if ("EFECTIVO".equals(metodoPago)) {
                    if (montoRecibido < total) {
                        throw new IllegalArgumentException(
                                "El efectivo recibido es insuficiente.");
                    }

                    cambio = redondear(montoRecibido - total);

                } else if (Math.abs(montoRecibido - total) > 0.001) {
                    throw new IllegalArgumentException(
                            "El importe de tarjeta debe coincidir "
                            + "con el total.");
                }

                validarExistencias(conexion, consumo);

                int idPedido = insertarPedido(
                        conexion,
                        idCliente,
                        total);

                for (PedidoDetalle detalle : detalles) {
                    insertarDetalle(
                            conexion,
                            idPedido,
                            detalle.getIdProducto(),
                            detalle.getIdMenu(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario());

                    List<PedidoDetalleExtra> extras = detalle.getExtras();

                    if (extras != null) {
                        for (PedidoDetalleExtra extra : extras) {
                            int cantidadExtraTotal = Math.multiplyExact(
                                    detalle.getCantidad(),
                                    extra.getCantidad());

                            insertarDetalle(
                                    conexion,
                                    idPedido,
                                    extra.getIdProducto(),
                                    null,
                                    cantidadExtraTotal,
                                    extra.getPrecioUnitario());
                        }
                    }
                }

                descontarExistenciasYRegistrarMovimientos(
                        conexion,
                        idPedido,
                        consumo);

                insertarPago(
                        conexion,
                        idPedido,
                        metodoPago,
                        total,
                        cambio,
                        referencia);

                actualizarPedidoPagado(conexion, idPedido);

                if (puntosGanados > 0) {
                    actualizarPuntosCliente(
                            conexion,
                            idCliente,
                            puntosGanados);

                    insertarMovimientoPuntos(
                            conexion,
                            idCliente,
                            idPedido,
                            puntosGanados);
                }

                conexion.commit();

                return idPedido;

            } catch (SQLException | RuntimeException e) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    e.addSuppressed(errorRollback);
                }

                throw e;

            } finally {
                try {
                    conexion.setAutoCommit(autoCommitAnterior);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public boolean marcarEntregado(int idPedido) throws SQLException {
        String sql = "UPDATE pedidos SET estado = 'ENTREGADO' "
                + "WHERE id_pedido = ? AND estado = 'PAGADO'";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean anularPedidoPendiente(int idPedido) throws SQLException {
        String sql = "UPDATE pedidos SET estado = 'ANULADO' "
                + "WHERE id_pedido = ? AND estado = 'PENDIENTE'";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            return ps.executeUpdate() > 0;
        }
    }

    private void validarEntrada(
            List<PedidoDetalle> detalles,
            String metodoPago,
            double montoRecibido) {

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException(
                    "El pedido debe contener al menos un artículo.");
        }

        if (!"EFECTIVO".equals(metodoPago)
                && !"TARJETA".equals(metodoPago)) {
            throw new IllegalArgumentException(
                    "Método de pago no válido.");
        }

        if (!Double.isFinite(montoRecibido) || montoRecibido < 0) {
            throw new IllegalArgumentException(
                    "El importe recibido no es válido.");
        }
    }

    private void validarDetalle(PedidoDetalle detalle) {
        if (detalle == null || detalle.getCantidad() <= 0) {
            throw new IllegalArgumentException(
                    "Cada artículo debe tener una cantidad positiva.");
        }

        boolean tieneProducto = detalle.getIdProducto() != null;
        boolean tieneMenu = detalle.getIdMenu() != null;

        if (tieneProducto == tieneMenu) {
            throw new IllegalArgumentException(
                    "Cada línea debe indicar exactamente un producto "
                    + "o un elemento del menú.");
        }
    }

    private void validarClienteActivo(
            Connection conexion,
            int idCliente) throws SQLException {

        String sql = "SELECT id_cliente FROM clientes "
                + "WHERE id_cliente = ? AND estado = 'ACTIVO' FOR UPDATE";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "El cliente no existe o está inactivo.");
                }
            }
        }
    }

    private ProductoInfo buscarProductoActivo(
            Connection conexion,
            int idProducto,
            boolean bloquear) throws SQLException {

        String sql = "SELECT id_producto, categoria, precio, existencia "
                + "FROM productos WHERE id_producto = ? "
                + "AND estado = 'ACTIVO'"
                + (bloquear ? " FOR UPDATE" : "");

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new ProductoInfo(
                        rs.getInt("id_producto"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getInt("existencia"));
            }
        }
    }

    private MenuInfo buscarMenuActivo(
            Connection conexion,
            int idMenu) throws SQLException {

        String sql = "SELECT id_menu, nombre, precio FROM menus "
                + "WHERE id_menu = ? AND estado = 'ACTIVO' FOR UPDATE";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new MenuInfo(
                        rs.getInt("id_menu"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"));
            }
        }
    }

    private void agregarConsumoMenu(
            Connection conexion,
            int idMenu,
            int cantidadMenu,
            Map<Integer, Integer> consumo) throws SQLException {

        String sql = "SELECT id_producto, cantidad FROM menu_componentes "
                + "WHERE id_menu = ? ORDER BY id_producto";

        boolean tieneComponentes = false;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tieneComponentes = true;

                    int idProducto = rs.getInt("id_producto");

                    int cantidad = Math.multiplyExact(
                            cantidadMenu,
                            rs.getInt("cantidad"));

                    sumarConsumo(consumo, idProducto, cantidad);
                }
            }
        }

        if (!tieneComponentes) {
            throw new IllegalStateException(
                    "El elemento del menú ID " + idMenu
                    + " no tiene componentes de inventario asociados. "
                    + "Registrá sus componentes desde el área del empleado.");
        }
    }

    private int puntosPorMenu(
            Connection conexion,
            int idMenu) throws SQLException {

        boolean sandwich = false;
        boolean bebida = false;
        boolean acompanamiento = false;

        String sql = "SELECT DISTINCT p.categoria "
                + "FROM menu_componentes mc "
                + "INNER JOIN productos p "
                + "ON p.id_producto = mc.id_producto "
                + "WHERE mc.id_menu = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String categoria = rs.getString("categoria");

                    if ("SANDWICH".equals(categoria)) {
                        sandwich = true;
                    }

                    if ("BEBIDA".equals(categoria)) {
                        bebida = true;
                    }

                    if ("ACOMPANAMIENTO".equals(categoria)) {
                        acompanamiento = true;
                    }
                }
            }
        }

        if (sandwich && bebida && acompanamiento) {
            return 8;
        }

        if (sandwich) {
            return 2;
        }

        return 0;
    }

    private void sumarConsumo(
            Map<Integer, Integer> consumo,
            int idProducto,
            int cantidad) {

        if (idProducto <= 0 || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "El consumo de inventario no es válido.");
        }

        consumo.put(
                idProducto,
                Math.addExact(
                        consumo.getOrDefault(idProducto, 0),
                        cantidad));
    }

    private void validarExistencias(
            Connection conexion,
            Map<Integer, Integer> consumo) throws SQLException {

        for (Map.Entry<Integer, Integer> entrada : consumo.entrySet()) {
            ProductoInfo producto = buscarProductoActivo(
                    conexion,
                    entrada.getKey(),
                    true);

            if (producto == null) {
                throw new IllegalArgumentException(
                        "El producto de inventario ID "
                        + entrada.getKey()
                        + " no existe o está inactivo.");
            }

            if (producto.existencia < entrada.getValue()) {
                throw new IllegalStateException(
                        "Existencias insuficientes para "
                        + entrada.getKey()
                        + ". Disponibles: " + producto.existencia
                        + ", necesarias: " + entrada.getValue() + ".");
            }
        }
    }

    private int insertarPedido(
            Connection conexion,
            int idCliente,
            double total) throws SQLException {

        String sql = "INSERT INTO pedidos "
                + "(id_cliente, estado, total) "
                + "VALUES (?, 'PENDIENTE', ?)";

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idCliente);
            ps.setDouble(2, total);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "No se pudo obtener el ID del pedido.");
    }

    private void insertarDetalle(
            Connection conexion,
            int idPedido,
            Integer idProducto,
            Integer idMenu,
            int cantidad,
            double precioUnitario) throws SQLException {

        String sql = "INSERT INTO pedido_detalles "
                + "(id_pedido, id_producto, id_menu, cantidad, precio_unitario) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPedido);

            if (idProducto == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, idProducto);
            }

            if (idMenu == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, idMenu);
            }

            ps.setInt(4, cantidad);
            ps.setDouble(5, precioUnitario);

            ps.executeUpdate();
        }
    }

    private void descontarExistenciasYRegistrarMovimientos(
            Connection conexion,
            int idPedido,
            Map<Integer, Integer> consumo) throws SQLException {

        String actualizar = "UPDATE productos "
                + "SET existencia = existencia - ? "
                + "WHERE id_producto = ? "
                + "AND estado = 'ACTIVO' AND existencia >= ?";

        String movimiento = "INSERT INTO movimientos_inventario "
                + "(id_producto, tipo, cantidad, observacion) "
                + "VALUES (?, 'VENTA', ?, ?)";

        for (Map.Entry<Integer, Integer> entrada : consumo.entrySet()) {
            int idProducto = entrada.getKey();
            int cantidad = entrada.getValue();

            try (PreparedStatement ps = conexion.prepareStatement(actualizar)) {
                ps.setInt(1, cantidad);
                ps.setInt(2, idProducto);
                ps.setInt(3, cantidad);

                if (ps.executeUpdate() == 0) {
                    throw new IllegalStateException(
                            "No se pudo descontar el stock del producto ID "
                            + idProducto);
                }
            }

            try (PreparedStatement ps = conexion.prepareStatement(movimiento)) {
                ps.setInt(1, idProducto);
                ps.setInt(2, cantidad);
                ps.setString(3, "Consumo del pedido " + idPedido);
                ps.executeUpdate();
            }
        }
    }

    private void insertarPago(
            Connection conexion,
            int idPedido,
            String metodo,
            double total,
            double cambio,
            String referencia) throws SQLException {

        String sql = "INSERT INTO pagos "
                + "(id_pedido, metodo, monto, cambio, estado, referencia) "
                + "VALUES (?, ?, ?, ?, 'APROBADO', ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setString(2, metodo);
            ps.setDouble(3, total);
            ps.setDouble(4, cambio);

            if (referencia == null || referencia.isBlank()) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, referencia);
            }

            ps.executeUpdate();
        }
    }

    private void actualizarPedidoPagado(
            Connection conexion,
            int idPedido) throws SQLException {

        String sql = "UPDATE pedidos SET estado = 'PAGADO' "
                + "WHERE id_pedido = ? AND estado = 'PENDIENTE'";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPedido);

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo cambiar el pedido a PAGADO.");
            }
        }
    }

    private void actualizarPuntosCliente(
            Connection conexion,
            int idCliente,
            int puntos) throws SQLException {

        String sql = "UPDATE clientes "
                + "SET puntos = puntos + ? "
                + "WHERE id_cliente = ? AND estado = 'ACTIVO'";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, puntos);
            ps.setInt(2, idCliente);

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudieron actualizar los puntos.");
            }
        }
    }

    private void insertarMovimientoPuntos(
            Connection conexion,
            int idCliente,
            int idPedido,
            int puntos) throws SQLException {

        String sql = "INSERT INTO movimientos_puntos "
                + "(id_cliente, id_pedido, tipo, puntos) "
                + "VALUES (?, ?, 'ACUMULACION', ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setInt(2, idPedido);
            ps.setInt(3, puntos);
            ps.executeUpdate();
        }
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private static final class ProductoInfo {
        private final int id;
        private final String categoria;
        private final double precio;
        private final int existencia;

        private ProductoInfo(
                int id,
                String categoria,
                double precio,
                int existencia) {

            this.id = id;
            this.categoria = categoria;
            this.precio = precio;
            this.existencia = existencia;
        }
    }

    private static final class MenuInfo {
        private final int id;
        private final String nombre;
        private final double precio;

        private MenuInfo(int id, String nombre, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }
    }
}