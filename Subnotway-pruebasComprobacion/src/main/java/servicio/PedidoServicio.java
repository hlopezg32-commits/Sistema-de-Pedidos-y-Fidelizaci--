package servicio;

import config.ConexionDB;
import dao.impl.PedidoDetalleExtraDAO;
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

                // Validar los artículos y calcular el total,
                // los puntos y el consumo de inventario.
                for (PedidoDetalle detalle : detalles) {
                    validarDetalle(detalle);

                    int cantidad = detalle.getCantidad();

                    if (detalle.getIdProducto() != null) {
                        ProductoInfo producto = buscarProductoActivo(
                                conexion,
                                detalle.getIdProducto(),
                                false);

                        if (producto == null) {
                            throw new IllegalArgumentException(
                                    "El producto no existe o está inactivo: "
                                    + detalle.getIdProducto());
                        }

                        detalle.setPrecioUnitario(producto.precio);
                        total += producto.precio * cantidad;

                        if ("SANDWICH".equals(producto.categoria)) {
                            agregarConsumoReceta(
                                    conexion,
                                    producto.codigo,
                                    cantidad,
                                    consumo);

                            puntosGanados = Math.addExact(
                                    puntosGanados,
                                    Math.multiplyExact(cantidad, 2));
                        } else {
                            sumarConsumo(
                                    consumo,
                                    producto.id,
                                    cantidad);
                        }

                    } else {
                        MenuInfo menu = buscarMenuActivo(
                                conexion,
                                detalle.getIdMenu());

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

                    // Validar extras y sumarlos al total e inventario.
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

                // Verificar todo el inventario antes de descontarlo.
                validarExistencias(conexion, consumo);

                int idPedido = insertarPedido(
                        conexion,
                        idCliente,
                        total);

                // Guardar cada detalle principal y relacionar
                // sus extras mediante el ID generado por MySQL.
                PedidoDetalleExtraDAO extraDAO =
                        new PedidoDetalleExtraDAO();

                for (PedidoDetalle detalle : detalles) {
                    int idDetalle = insertarDetalle(
                            conexion,
                            idPedido,
                            detalle.getIdProducto(),
                            detalle.getIdMenu(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario());

                    List<PedidoDetalleExtra> extras = detalle.getExtras();

                    if (extras == null || extras.isEmpty()) {
                        continue;
                    }

                    for (PedidoDetalleExtra extra : extras) {
                        int cantidadExtraTotal = Math.multiplyExact(
                                detalle.getCantidad(),
                                extra.getCantidad());

                        PedidoDetalleExtra extraGuardado =
                                new PedidoDetalleExtra(
                                        0,
                                        idDetalle,
                                        extra.getIdProducto(),
                                        cantidadExtraTotal,
                                        extra.getPrecioUnitario());

                        boolean insertado = extraDAO.insertar(
                                conexion,
                                extraGuardado);

                        if (!insertado) {
                            throw new SQLException(
                                    "No se pudo guardar un extra "
                                    + "del detalle " + idDetalle);
                        }
                    }
                }

                // Descontar inventario una sola vez por producto.
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

        String sql = "SELECT id_producto, codigo, categoria, "
                + "precio, existencia "
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
                        rs.getString("codigo"),
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

        String sql = "SELECT mc.id_producto, mc.cantidad, "
                + "p.categoria, p.codigo "
                + "FROM menu_componentes mc "
                + "INNER JOIN productos p "
                + "ON p.id_producto = mc.id_producto "
                + "WHERE mc.id_menu = ? "
                + "ORDER BY mc.id_producto";

        boolean tieneComponentes = false;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tieneComponentes = true;

                    int cantidadNecesaria = Math.multiplyExact(
                            cantidadMenu,
                            rs.getInt("cantidad"));

                    String categoria = rs.getString("categoria");

                    if ("SANDWICH".equals(categoria)) {
                        agregarConsumoReceta(
                                conexion,
                                rs.getString("codigo"),
                                cantidadNecesaria,
                                consumo);
                    } else {
                        sumarConsumo(
                                consumo,
                                rs.getInt("id_producto"),
                                cantidadNecesaria);
                    }
                }
            }
        }

        if (!tieneComponentes) {
            throw new IllegalStateException(
                    "El menú ID " + idMenu
                    + " no tiene componentes de inventario asociados.");
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
                + "WHERE mc.id_menu = ? "
                + "AND p.estado = 'ACTIVO'";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String categoria = rs.getString("categoria");

                    if ("SANDWICH".equals(categoria)) {
                        sandwich = true;
                    } else if ("BEBIDA".equals(categoria)) {
                        bebida = true;
                    } else if ("ACOMPANAMIENTO".equals(categoria)) {
                        acompanamiento = true;
                    }
                }
            }
        }

        return sandwich && bebida && acompanamiento ? 8 : 0;
    }

    private void agregarConsumoReceta(
            Connection conexion,
            String codigoSandwich,
            int cantidadSandwiches,
            Map<Integer, Integer> consumo) throws SQLException {

        String[][] receta =
                CatalogoRecetas.obtenerReceta(codigoSandwich);

        if (receta == null || receta.length == 0) {
            throw new IllegalStateException(
                    "El sándwich con código " + codigoSandwich
                    + " no tiene una receta registrada.");
        }

        for (String[] ingrediente : receta) {
            if (ingrediente == null
                    || ingrediente.length < 2
                    || ingrediente[0] == null
                    || ingrediente[1] == null) {
                throw new IllegalStateException(
                        "La receta " + codigoSandwich
                        + " contiene un ingrediente inválido.");
            }

            String codigoIngrediente = ingrediente[0].trim();

            int cantidadPorSandwich;

            try {
                cantidadPorSandwich =
                        Integer.parseInt(ingrediente[1].trim());
            } catch (NumberFormatException e) {
                throw new IllegalStateException(
                        "Cantidad inválida para el ingrediente "
                        + codigoIngrediente, e);
            }

            if (cantidadPorSandwich <= 0) {
                throw new IllegalStateException(
                        "La cantidad del ingrediente "
                        + codigoIngrediente + " debe ser positiva.");
            }

            int cantidadNecesaria = Math.multiplyExact(
                    cantidadPorSandwich,
                    cantidadSandwiches);

            String sql = "SELECT id_producto "
                    + "FROM productos "
                    + "WHERE codigo = ? AND estado = 'ACTIVO'";

            try (PreparedStatement ps =
                    conexion.prepareStatement(sql)) {

                ps.setString(1, codigoIngrediente);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException(
                                "El ingrediente con código "
                                + codigoIngrediente
                                + " no existe o está inactivo.");
                    }

                    sumarConsumo(
                            consumo,
                            rs.getInt("id_producto"),
                            cantidadNecesaria);
                }
            }
        }
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

    private int insertarDetalle(
            Connection conexion,
            int idPedido,
            Integer idProducto,
            Integer idMenu,
            int cantidad,
            double precioUnitario) throws SQLException {

        String sql = "INSERT INTO pedido_detalles "
                + "(id_pedido, id_producto, id_menu, cantidad, precio_unitario) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

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

            int filas = ps.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "No se pudo insertar el detalle del pedido.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "No se pudo obtener el ID del detalle del pedido.");
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
        private final String codigo;
        private final String categoria;
        private final double precio;
        private final int existencia;

        private ProductoInfo(
                int id,
                String codigo,
                String categoria,
                double precio,
                int existencia) {

            this.id = id;
            this.codigo = codigo;
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
