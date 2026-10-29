package servicio;

import config.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class CanjeServicio {

    /**
     * Canjea una recompensa utilizando los puntos del cliente.
     * Devuelve el ID del canje registrado.
     *
     * Si ocurre un error, se revierten todos los cambios.
     */
    public int realizarCanje(int idCliente, int idRecompensa)
            throws SQLException {

        try (Connection conexion = ConexionDB.conectar()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();

            try {
                conexion.setAutoCommit(false);

                // 1. Bloquear el registro del cliente.
                int puntosCliente = obtenerPuntosCliente(conexion, idCliente);

                // 2. Obtener y bloquear la recompensa.
                RecompensaDatos recompensa =
                        obtenerRecompensa(conexion, idRecompensa);

                if (!"ACTIVA".equals(recompensa.estado)) {
                    throw new SQLException("La recompensa no está activa.");
                }

                if (recompensa.costoPuntos <= 0) {
                    throw new SQLException(
                            "El costo de la recompensa debe ser mayor que cero.");
                }

                if (puntosCliente < recompensa.costoPuntos) {
                    throw new SQLException(
                            "El cliente no tiene suficientes puntos.");
                }

                // 3. Calcular todos los productos que se consumirán.
                Map<Integer, Integer> consumo = new HashMap<>();

                if (recompensa.idMenu != null) {
                    agregarConsumoMenu(
                            conexion, recompensa.idMenu, 1, consumo);
                } else if (recompensa.idProducto != null) {
                    agregarConsumoProducto(
                            conexion, recompensa.idProducto, 1, consumo);
                } else {
                    throw new SQLException(
                            "La recompensa no tiene un producto o menú asociado.");
                }

                if (consumo.isEmpty()) {
                    throw new SQLException(
                            "No se encontraron productos para realizar el canje.");
                }

                // 4. Descontar existencias y registrar movimientos.
                for (Map.Entry<Integer, Integer> entrada
                        : consumo.entrySet()) {

                    int idProducto = entrada.getKey();
                    int cantidad = entrada.getValue();

                    descontarExistencia(
                            conexion, idProducto, cantidad);

                    registrarMovimientoInventario(
                            conexion,
                            idProducto,
                            cantidad,
                            "Canje de recompensa " + idRecompensa);
                }

                // 5. Descontar puntos únicamente si el saldo alcanza.
                descontarPuntos(
                        conexion, idCliente, recompensa.costoPuntos);

                // 6. Registrar el canje.
                int idCanje = registrarCanje(
                        conexion,
                        idCliente,
                        idRecompensa,
                        recompensa.costoPuntos);

                // 7. Registrar el movimiento de puntos.
                registrarMovimientoPuntos(
                        conexion,
                        idCliente,
                        recompensa.costoPuntos);

                conexion.commit();
                return idCanje;

            } catch (SQLException | RuntimeException e) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    e.addSuppressed(errorRollback);
                }

                throw e;

            } finally {
                try {
                    conexion.setAutoCommit(autoCommitOriginal);
                } catch (SQLException e) {
                    System.err.println(
                            "No se pudo restaurar autoCommit: "
                            + e.getMessage());
                }
            }
        }
    }

    private int obtenerPuntosCliente(Connection conexion, int idCliente)
            throws SQLException {

        String sql = """
            SELECT puntos, estado
            FROM clientes
            WHERE id_cliente = ?
            FOR UPDATE
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El cliente no existe.");
                }

                if (!"ACTIVO".equals(rs.getString("estado"))) {
                    throw new SQLException("El cliente está inactivo.");
                }

                return rs.getInt("puntos");
            }
        }
    }

    private RecompensaDatos obtenerRecompensa(
            Connection conexion, int idRecompensa) throws SQLException {

        String sql = """
            SELECT id_recompensa, costo_puntos, id_producto, id_menu, estado
            FROM recompensas
            WHERE id_recompensa = ?
            FOR UPDATE
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idRecompensa);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("La recompensa no existe.");
                }

                int idProducto = rs.getInt("id_producto");
                Integer producto = rs.wasNull() ? null : idProducto;

                int idMenu = rs.getInt("id_menu");
                Integer menu = rs.wasNull() ? null : idMenu;

                return new RecompensaDatos(
                        rs.getInt("costo_puntos"),
                        producto,
                        menu,
                        rs.getString("estado"));
            }
        }
    }

   
private void agregarConsumoProducto(
        Connection conexion,
        int idProducto,
        int cantidad,
        Map<Integer, Integer> consumo) throws SQLException {

    agregarConsumoProducto(
            conexion, idProducto, cantidad, consumo,
            new java.util.HashSet<>());
}

private void agregarConsumoProducto(
        Connection conexion,
        int idProducto,
        int cantidad,
        Map<Integer, Integer> consumo,
        java.util.Set<Integer> ruta) throws SQLException {

    if (cantidad <= 0) {
        throw new SQLException("La cantidad debe ser positiva.");
    }

    // Verificar primero que el producto asociado esté activo.
    verificarProductoActivo(conexion, idProducto);

    Integer idMenuReceta =
            buscarRecetaPorProducto(conexion, idProducto);

    if (idMenuReceta == null) {
        sumarConsumo(consumo, idProducto, cantidad);
        return;
    }

    agregarConsumoMenu(
            conexion, idMenuReceta, cantidad, consumo, ruta);
}



private void agregarConsumoMenu(
        Connection conexion,
        int idMenu,
        int cantidadMenu,
        Map<Integer, Integer> consumo) throws SQLException {

    agregarConsumoMenu(
            conexion, idMenu, cantidadMenu, consumo,
            new java.util.HashSet<>());
}

private void agregarConsumoMenu(
        Connection conexion,
        int idMenu,
        int cantidadMenu,
        Map<Integer, Integer> consumo,
        java.util.Set<Integer> ruta) throws SQLException {

    if (cantidadMenu <= 0) {
        throw new SQLException("La cantidad del menú debe ser positiva.");
    }

    if (!ruta.add(idMenu)) {
        throw new SQLException(
                "Se detectó una receta circular en el menú " + idMenu);
    }

    try {
        String sqlMenu = """
            SELECT tipo, estado
            FROM menus
            WHERE id_menu = ?
            FOR UPDATE
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sqlMenu)) {
            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El menú no existe: " + idMenu);
                }

                if (!"ACTIVO".equals(rs.getString("estado"))) {
                    throw new SQLException("El menú está inactivo: " + idMenu);
                }
            }
        }

        String sqlComponentes = """
            SELECT id_producto, cantidad, cantidad_extra
            FROM menu_componentes
            WHERE id_menu = ?
            ORDER BY id_producto
        """;

        boolean encontrado = false;

        try (PreparedStatement ps =
                     conexion.prepareStatement(sqlComponentes)) {

            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    encontrado = true;

                    int idProducto = rs.getInt("id_producto");
                    int cantidadBase = rs.getInt("cantidad");
                    int cantidadExtra = rs.getInt("cantidad_extra");

                    if (cantidadBase <= 0 || cantidadExtra < 0) {
                        throw new SQLException(
                                "Cantidad inválida en la receta del menú "
                                + idMenu);
                    }

                    /*
                     * Supuesto: cantidad_extra representa unidades
                     * adicionales incluidas por cada unidad del menú.
                     * Si representa extras elegidos por el cliente,
                     * no debe sumarse automáticamente aquí.
                     */
                    int porUnidad = Math.addExact(
                            cantidadBase, cantidadExtra);

                    int totalComponente = Math.multiplyExact(
                            porUnidad, cantidadMenu);

                    agregarConsumoProducto(
                            conexion,
                            idProducto,
                            totalComponente,
                            consumo,
                            ruta);
                }
            }
        }

        if (!encontrado) {
            throw new SQLException(
                    "El menú no tiene componentes registrados: " + idMenu);
        }

    } catch (ArithmeticException e) {
        throw new SQLException(
                "La cantidad calculada excede el límite permitido.", e);
    } finally {
        ruta.remove(idMenu);
    }
}


    private Integer buscarRecetaPorProducto(
            Connection conexion, int idProducto) throws SQLException {

        String sql = """
            SELECT id_menu
            FROM menus
            WHERE id_producto = ?
              AND tipo = 'SANDWICH'
              AND estado = 'ACTIVO'
            LIMIT 1
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id_menu") : null;
            }
        }
    }

    private void verificarProductoActivo(
            Connection conexion, int idProducto) throws SQLException {

        String sql = """
            SELECT id_producto
            FROM productos
            WHERE id_producto = ?
              AND estado = 'ACTIVO'
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "Un producto asociado a la recompensa no existe o está inactivo.");
                }
            }
        }
    }

    private void descontarExistencia(
            Connection conexion, int idProducto, int cantidad)
            throws SQLException {

        String sql = """
            UPDATE productos
            SET existencia = existencia - ?
            WHERE id_producto = ?
              AND estado = 'ACTIVO'
              AND existencia >= ?
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "Existencias insuficientes para el producto ID "
                        + idProducto + ".");
            }
        }
    }

    private void registrarMovimientoInventario(
            Connection conexion,
            int idProducto,
            int cantidad,
            String observacion) throws SQLException {

        String sql = """
            INSERT INTO movimientos_inventario
                (id_producto, tipo, cantidad, fecha, observacion)
            VALUES (?, 'CANJE', ?, NOW(), ?)
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, cantidad);
            ps.setString(3, observacion);
            ps.executeUpdate();
        }
    }

    private void descontarPuntos(
            Connection conexion, int idCliente, int puntos)
            throws SQLException {

        String sql = """
            UPDATE clientes
            SET puntos = puntos - ?
            WHERE id_cliente = ?
              AND estado = 'ACTIVO'
              AND puntos >= ?
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, puntos);
            ps.setInt(2, idCliente);
            ps.setInt(3, puntos);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No se pudieron descontar los puntos. "
                        + "Verifica el saldo del cliente.");
            }
        }
    }

    private int registrarCanje(
            Connection conexion,
            int idCliente,
            int idRecompensa,
            int puntos) throws SQLException {

        String sql = """
            INSERT INTO canjes
                (id_cliente, id_recompensa, puntos_utilizados, fecha)
            VALUES (?, ?, ?, NOW())
        """;

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idCliente);
            ps.setInt(2, idRecompensa);
            ps.setInt(3, puntos);

            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el canje.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("No se obtuvo el ID del canje.");
    }

    private void registrarMovimientoPuntos(
            Connection conexion, int idCliente, int puntos)
            throws SQLException {

        String sql = """
            INSERT INTO movimientos_puntos
                (id_cliente, id_pedido, tipo, puntos, fecha)
            VALUES (?, NULL, 'CANJE', ?, NOW())
        """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setInt(2, puntos);
            ps.executeUpdate();
        }
    }

    private void sumarConsumo(
            Map<Integer, Integer> consumo,
            int idProducto,
            int cantidad) {

        consumo.merge(idProducto, cantidad, Math::addExact);
    }

    private static class RecompensaDatos {
        private final int costoPuntos;
        private final Integer idProducto;
        private final Integer idMenu;
        private final String estado;

        private RecompensaDatos(
                int costoPuntos,
                Integer idProducto,
                Integer idMenu,
                String estado) {
            this.costoPuntos = costoPuntos;
            this.idProducto = idProducto;
            this.idMenu = idMenu;
            this.estado = estado;
        }
    }
}
