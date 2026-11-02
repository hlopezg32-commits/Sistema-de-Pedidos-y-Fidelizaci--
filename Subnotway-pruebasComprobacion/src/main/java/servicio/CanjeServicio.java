
package servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CanjeServicio {

    /**
     * Realiza un canje usando la conexión que ya tiene abierta
     * RegistrarDatosConsola.
     *
     * Devuelve el ID del canje si la operación se completa.
     */
    public int realizarCanje(
            Connection conexion,
            int idCliente,
            int idRecompensa) throws SQLException {

        if (conexion == null || conexion.isClosed()) {
            throw new SQLException("La conexión a MySQL no está disponible.");
        }

        boolean autoCommitAnterior = conexion.getAutoCommit();

        try {
            conexion.setAutoCommit(false);

            int puntosActuales;
            int costoPuntos;
            Integer idProducto;
            Integer idMenu;

            // Bloquear al cliente mientras se procesa el canje.
            String sqlCliente = """
                SELECT puntos
                FROM clientes
                WHERE id_cliente = ?
                  AND estado = 'ACTIVO'
                FOR UPDATE
                """;

            try (PreparedStatement ps =
                    conexion.prepareStatement(sqlCliente)) {

                ps.setInt(1, idCliente);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "El cliente no existe o está inactivo.");
                    }

                    puntosActuales = rs.getInt("puntos");
                }
            }

            // Consultar y bloquear la recompensa.
            String sqlRecompensa = """
                SELECT costo_puntos, id_producto, id_menu
                FROM recompensas
                WHERE id_recompensa = ?
                  AND estado = 'ACTIVA'
                FOR UPDATE
                """;

            try (PreparedStatement ps =
                    conexion.prepareStatement(sqlRecompensa)) {

                ps.setInt(1, idRecompensa);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "La recompensa no existe o está inactiva.");
                    }

                    costoPuntos = rs.getInt("costo_puntos");

                    int producto = rs.getInt("id_producto");
                    idProducto = rs.wasNull() ? null : producto;

                    int menu = rs.getInt("id_menu");
                    idMenu = rs.wasNull() ? null : menu;
                }
            }

            if (costoPuntos <= 0) {
                throw new SQLException(
                        "El costo de la recompensa no es válido.");
            }

            if (puntosActuales < costoPuntos) {
                throw new SQLException(
                        "El cliente no tiene suficientes puntos.");
            }

            // Calcular los productos que se deben descontar del inventario.
            Map<Integer, Integer> consumo = new TreeMap<>();

            if (idProducto != null) {
                sumarConsumo(consumo, idProducto, 1);
            } else if (idMenu != null) {
                agregarConsumoMenu(conexion, idMenu, consumo);
            } else {
                throw new SQLException(
                        "La recompensa no tiene un producto o menú asociado.");
            }

            // Descontar existencias y registrar los movimientos.
            for (Map.Entry<Integer, Integer> entrada
                    : consumo.entrySet()) {

                int id = entrada.getKey();
                int cantidad = entrada.getValue();

                descontarExistencia(conexion, id, cantidad);

                registrarMovimientoInventario(
                        conexion, id, cantidad, idRecompensa);
            }

            // Descontar puntos solo después de validar el inventario.
            String sqlDescontarPuntos = """
                UPDATE clientes
                SET puntos = puntos - ?
                WHERE id_cliente = ?
                  AND estado = 'ACTIVO'
                  AND puntos >= ?
                """;

            try (PreparedStatement ps =
                    conexion.prepareStatement(sqlDescontarPuntos)) {

                ps.setInt(1, costoPuntos);
                ps.setInt(2, idCliente);
                ps.setInt(3, costoPuntos);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "No se pudieron descontar los puntos.");
                }
            }

            // Registrar el canje y recuperar su identificador.
            int idCanje;

            String sqlCanje = """
                INSERT INTO canjes
                    (id_cliente, id_recompensa, puntos_utilizados, fecha)
                VALUES (?, ?, ?, NOW())
                """;

            try (PreparedStatement ps = conexion.prepareStatement(
                    sqlCanje,
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, idCliente);
                ps.setInt(2, idRecompensa);
                ps.setInt(3, costoPuntos);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "No se pudo registrar el canje.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "No se obtuvo el identificador del canje.");
                    }

                    idCanje = rs.getInt(1);
                }
            }

            // Registrar el movimiento de puntos.
            String sqlMovimiento = """
                INSERT INTO movimientos_puntos
                    (id_cliente, id_pedido, tipo, puntos, fecha)
                VALUES (?, NULL, 'CANJE', ?, NOW())
                """;

            try (PreparedStatement ps =
                    conexion.prepareStatement(sqlMovimiento)) {

                ps.setInt(1, idCliente);
                ps.setInt(2, costoPuntos);
                ps.executeUpdate();
            }

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
                conexion.setAutoCommit(autoCommitAnterior);
            } catch (SQLException e) {
                System.err.println(
                        "No se pudo restaurar autoCommit: "
                        + e.getMessage());
            }
        }
    }

    /**
     * Obtiene los componentes de un menú y calcula las cantidades.
     */
    private void agregarConsumoMenu(
            Connection conexion,
            int idMenu,
            Map<Integer, Integer> consumo) throws SQLException {

        String sqlMenu = """
            SELECT id_menu
            FROM menus
            WHERE id_menu = ?
              AND estado = 'ACTIVO'
            """;

        try (PreparedStatement ps = conexion.prepareStatement(sqlMenu)) {
            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "El menú de la recompensa no está activo.");
                }
            }
        }

        String sqlComponentes = """
            SELECT mc.id_producto, mc.cantidad, p.codigo, p.categoria, p.estado
            FROM menu_componentes mc
            INNER JOIN productos p ON p.id_producto = mc.id_producto
            WHERE mc.id_menu = ?
            ORDER BY mc.id_producto
            """;

        int componentesEncontrados = 0;

        try (PreparedStatement ps =
                conexion.prepareStatement(sqlComponentes)) {

            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idProducto = rs.getInt("id_producto");
                    int cantidad = rs.getInt("cantidad");
                    String categoria = rs.getString("categoria");
                    String codigo = rs.getString("codigo");
                    String estado = rs.getString("estado");

                    if (cantidad <= 0) {
                        throw new SQLException(
                                "Un componente del menú tiene una cantidad inválida.");
                    }

                    if (!"ACTIVO".equalsIgnoreCase(estado)) {
                        throw new SQLException(
                                "El componente de inventario ID " + idProducto
                                + " está inactivo.");
                    }

                    if ("SANDWICH".equalsIgnoreCase(categoria)) {
                        agregarConsumoReceta(
                                conexion, codigo, cantidad, consumo);
                    } else {
                        sumarConsumo(consumo, idProducto, cantidad);
                    }
                    componentesEncontrados++;
                }
            }
        }

        if (componentesEncontrados == 0) {
            throw new SQLException(
                    "El menú de la recompensa no tiene componentes.");
        }
    }

    private void agregarConsumoReceta(
            Connection conexion,
            String codigoSandwich,
            int cantidadSandwiches,
            Map<Integer, Integer> consumo) throws SQLException {

        String[][] receta = CatalogoRecetas.obtenerReceta(codigoSandwich);

        if (receta == null || receta.length == 0) {
            throw new SQLException(
                    "El sándwich " + codigoSandwich
                    + " no tiene una receta registrada.");
        }

        String sqlIngrediente = """
            SELECT id_producto, estado
            FROM productos
            WHERE codigo = ?
            """;

        for (String[] ingrediente : receta) {
            if (ingrediente == null || ingrediente.length < 2
                    || ingrediente[0] == null || ingrediente[1] == null) {
                throw new SQLException(
                        "La receta " + codigoSandwich
                        + " contiene un ingrediente inválido.");
            }

            int cantidadPorSandwich;
            try {
                cantidadPorSandwich = Integer.parseInt(ingrediente[1].trim());
            } catch (NumberFormatException e) {
                throw new SQLException(
                        "Cantidad inválida en la receta " + codigoSandwich, e);
            }

            if (cantidadPorSandwich <= 0) {
                throw new SQLException(
                        "La cantidad de un ingrediente debe ser positiva.");
            }

            int necesaria;
            try {
                necesaria = Math.multiplyExact(
                        cantidadPorSandwich, cantidadSandwiches);
            } catch (ArithmeticException e) {
                throw new SQLException(
                        "La cantidad de ingredientes es demasiado grande.", e);
            }

            try (PreparedStatement ps =
                    conexion.prepareStatement(sqlIngrediente)) {
                ps.setString(1, ingrediente[0].trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()
                            || !"ACTIVO".equalsIgnoreCase(rs.getString("estado"))) {
                        throw new SQLException(
                                "El ingrediente " + ingrediente[0]
                                + " no existe o está inactivo.");
                    }

                    sumarConsumo(
                            consumo, rs.getInt("id_producto"), necesaria);
                }
            }
        }
    }

    private void sumarConsumo(
            Map<Integer, Integer> consumo,
            int idProducto,
            int cantidad) throws SQLException {

        if (idProducto <= 0 || cantidad <= 0) {
            throw new SQLException(
                    "El producto o la cantidad no son válidos.");
        }

        try {
            consumo.merge(idProducto, cantidad, Math::addExact);
        } catch (ArithmeticException e) {
            throw new SQLException(
                    "La cantidad total de un producto es demasiado grande.",
                    e);
        }
    }

    /**
     * Descuenta existencias únicamente si el producto está activo
     * y hay suficientes unidades disponibles.
     */
    private void descontarExistencia(
            Connection conexion,
            int idProducto,
            int cantidad) throws SQLException {

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
                        "El producto con ID " + idProducto
                        + " está inactivo o no tiene existencias suficientes.");
            }
        }
    }

    private void registrarMovimientoInventario(
            Connection conexion,
            int idProducto,
            int cantidad,
            int idRecompensa) throws SQLException {

        String sql = """
            INSERT INTO movimientos_inventario
                (id_producto, tipo, cantidad, fecha, observacion)
            VALUES (?, 'CANJE', ?, NOW(), ?)
            """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, cantidad);
            ps.setString(
                    3,
                    "Canje de recompensa ID " + idRecompensa);
            ps.executeUpdate();
        }
    }
}
