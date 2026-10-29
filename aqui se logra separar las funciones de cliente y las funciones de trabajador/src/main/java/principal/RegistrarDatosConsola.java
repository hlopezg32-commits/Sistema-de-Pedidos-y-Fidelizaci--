package principal;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class RegistrarDatosConsola {

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        Connection conexion = null;

        try {
            conexion = ConexionDB.conectar();

            if (conexion == null || conexion.isClosed()) {
                throw new SQLException(
                        "No se pudo establecer la conexión con MySQL.");
            }

            int opcion;

            do {
                mostrarMenuPrincipal();
                opcion = leerOpcionRango(
                        "Seleccioná una opción: ", 0, 2);

                if (opcion == 1) {
                    menuCliente(conexion);
                } else if (opcion == 2) {
                    menuEmpleado(conexion);
                } else {
                    System.out.println("Saliendo de Subnotway.");
                }

            } while (opcion != 0);

        } catch (SQLException e) {
            System.out.println(
                    "Error de conexión: " + e.getMessage());

        } finally {
            if (conexion != null) {
                try {
                    conexion.close();
                } catch (SQLException e) {
                    System.out.println(
                            "Error al cerrar la conexión: "
                            + e.getMessage());
                }
            }
        }
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n========================================");
        System.out.println("             SUBNOTWAY");
        System.out.println("========================================");
        System.out.println("1. Área del cliente");
        System.out.println("2. Área del empleado");
        System.out.println("0. Salir");
    }

    private static void menuCliente(Connection conexion) {
        int opcion;

        do {
            System.out.println("\n========================================");
            System.out.println("           ÁREA DEL CLIENTE");
            System.out.println("========================================");
            System.out.println("1. Registrarse como cliente");
            System.out.println("2. Consultar catálogo de ventas");
            System.out.println("3. Consultar saldo de puntos");
            System.out.println("0. Volver al menú principal");

            opcion = leerOpcionRango(
                    "Seleccioná una opción: ", 0, 3);

            try {
                switch (opcion) {
                    case 1:
                        registrarCliente(conexion);
                        break;
                    case 2:
                        listarCatalogoCliente(conexion);
                        break;
                    case 3:
                        consultarPuntosCliente(conexion);
                        break;
                    case 0:
                        System.out.println(
                                "Volviendo al menú principal.");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }

            } catch (SQLException e) {
                System.out.println(
                        "No se pudo completar la operación: "
                        + e.getMessage());

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Dato inválido: " + e.getMessage());
            }

        } while (opcion != 0);
    }

    private static void menuEmpleado(Connection conexion) {
        int opcion;

        do {
            System.out.println("\n========================================");
            System.out.println("           ÁREA DEL EMPLEADO");
            System.out.println("========================================");
            System.out.println("1. Registrar producto de inventario");
            System.out.println("2. Registrar elemento del menú de ventas");
            System.out.println("3. Registrar recompensa");
            System.out.println("4. Agregar componente a un elemento del menú");
            System.out.println("5. Consultar datos registrados");
            System.out.println("6. Consultar pedidos y pagos");
            System.out.println("7. Marcar pedido como entregado");
            System.out.println("8. Anular pedido pendiente");
            System.out.println("0. Volver al menú principal");

            opcion = leerOpcionRango(
                    "Seleccioná una opción: ", 0, 8);

            try {
                switch (opcion) {
                    case 1:
                        registrarProducto(conexion);
                        break;
                    case 2:
                        registrarMenu(conexion);
                        break;
                    case 3:
                        registrarRecompensa(conexion);
                        break;
                    case 4:
                        registrarComponenteMenu(conexion);
                        break;
                    case 5:
                        listarDatos(conexion);
                        break;
                    case 6:
                        listarPedidos(conexion);
                        break;
                    case 7:
                        cambiarEstadoPedido(conexion, "ENTREGADO");
                        break;
                    case 8:
                        cambiarEstadoPedido(conexion, "ANULADO");
                        break;
                    case 0:
                        System.out.println(
                                "Volviendo al menú principal.");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }

            } catch (SQLException e) {
                System.out.println(
                        "No se pudo completar la operación: "
                        + e.getMessage());

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Dato inválido: " + e.getMessage());
            }

        } while (opcion != 0);
    }

    private static void listarCatalogoCliente(Connection conexion)
            throws SQLException {

        String sql = "SELECT m.id_menu, m.codigo, m.nombre, m.precio "
                + "FROM menus m "
                + "WHERE m.estado = 'ACTIVO' "
                + "AND EXISTS ("
                + "SELECT 1 FROM menu_componentes mc "
                + "WHERE mc.id_menu = m.id_menu) "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM menu_componentes mc "
                + "INNER JOIN productos p "
                + "ON p.id_producto = mc.id_producto "
                + "WHERE mc.id_menu = m.id_menu "
                + "AND (p.estado <> 'ACTIVO' "
                + "OR p.existencia < mc.cantidad)) "
                + "ORDER BY m.nombre";

        System.out.println("\n--- CATÁLOGO DE VENTAS ---");

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                System.out.printf(
                        "ID %d | %s | %s | Q %.2f%n",
                        rs.getInt("id_menu"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"));
            }

            if (!hay) {
                System.out.println(
                        "Todavía no hay elementos activos en el catálogo.");
            }
        }
    }

    private static void consultarPuntosCliente(Connection conexion)
            throws SQLException {

        String identificador = leerTexto(
                "Ingresá tu identificador de cliente: ");

        String sql = "SELECT nombre, puntos, estado "
                + "FROM clientes WHERE identificador = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, identificador);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println(
                            "Cliente: " + rs.getString("nombre"));
                    System.out.println(
                            "Puntos acumulados: " + rs.getInt("puntos"));
                    System.out.println(
                            "Estado: " + rs.getString("estado"));
                } else {
                    System.out.println(
                            "No existe un cliente con ese identificador.");
                }
            }
        }
    }

    private static void registrarProducto(Connection conexion)
            throws SQLException {

        String codigo = leerTexto("Código único del producto: ");
        String nombre = leerTexto("Nombre del producto: ");
        String categoria = leerCategoria();

        double precio = leerDoubleNoNegativo(
                "Precio/costo registrado (Q): ");

        int existencia = leerEnteroNoNegativo(
                "Existencia inicial: ");

        boolean autoCommitAnterior = conexion.getAutoCommit();

        try {
            conexion.setAutoCommit(false);

            String sqlProducto = "INSERT INTO productos "
                    + "(codigo, nombre, categoria, precio, existencia, estado) "
                    + "VALUES (?, ?, ?, ?, ?, 'ACTIVO')";

            int idProducto;

            try (PreparedStatement ps = conexion.prepareStatement(
                    sqlProducto, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, codigo);
                ps.setString(2, nombre);
                ps.setString(3, categoria);
                ps.setDouble(4, precio);
                ps.setInt(5, existencia);

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "MySQL no devolvió el ID del producto.");
                    }

                    idProducto = rs.getInt(1);
                }
            }

            if (existencia > 0) {
                String sqlMovimiento = "INSERT INTO movimientos_inventario "
                        + "(id_producto, tipo, cantidad, observacion) "
                        + "VALUES (?, 'ENTRADA', ?, ?)";

                try (PreparedStatement ps = conexion.prepareStatement(
                        sqlMovimiento)) {

                    ps.setInt(1, idProducto);
                    ps.setInt(2, existencia);
                    ps.setString(3, "Registro inicial desde Java");
                    ps.executeUpdate();
                }
            }

            conexion.commit();

            System.out.println(
                    "Producto registrado. ID: " + idProducto);

        } catch (SQLException e) {
            conexion.rollback();
            throw e;

        } finally {
            conexion.setAutoCommit(autoCommitAnterior);
        }
    }

    private static void registrarMenu(Connection conexion)
            throws SQLException {

        String codigo = leerTexto("Código único del menú: ");

        String nombre = leerTexto(
                "Nombre comercial (sándwich, bebida, complemento o combo): ");

        double precio = leerDoubleNoNegativo(
                "Precio de venta (Q): ");

        String sql = "INSERT INTO menus "
                + "(codigo, nombre, precio, estado) "
                + "VALUES (?, ?, ?, 'ACTIVO')";

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setDouble(3, precio);

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "No se registró el elemento del menú.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println(
                            "Elemento del menú registrado. ID: "
                            + rs.getInt(1));
                }
            }
        }
    }

    private static void registrarCliente(Connection conexion)
            throws SQLException {

        String identificador = leerTexto(
                "Identificador único del cliente: ");

        String nombre = leerTexto("Nombre completo: ");

        String telefono = leerTextoOpcional(
                "Teléfono (Enter si no tiene): ");

        String correo = leerTextoOpcional(
                "Correo (Enter si no tiene): ");

        String sql = "INSERT INTO clientes "
                + "(identificador, nombre, telefono, correo, puntos, estado) "
                + "VALUES (?, ?, ?, ?, 0, 'ACTIVO')";

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, identificador);
            ps.setString(2, nombre);
            ps.setString(3, telefono);
            ps.setString(4, correo);

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "No se registró el cliente.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println(
                            "Cliente registrado con 0 puntos. ID: "
                            + rs.getInt(1));
                }
            }
        }
    }

    private static void registrarRecompensa(Connection conexion)
            throws SQLException {

        String nombre = leerTexto("Nombre de la recompensa: ");

        int costoPuntos = leerEnteroPositivo(
                "Puntos necesarios: ");

        System.out.println("¿Qué se entregará como recompensa?");
        System.out.println("1. Un producto de inventario");
        System.out.println("2. Un elemento del menú");

        int tipo = leerOpcionRango("Opción: ", 1, 2);

        Integer idProducto = null;
        Integer idMenu = null;

        if (tipo == 1) {
            listarProductos(conexion);

            idProducto = leerEnteroPositivo(
                    "ID del producto asociado: ");
        } else {
            listarMenus(conexion);

            idMenu = leerEnteroPositivo(
                    "ID del elemento del menú asociado: ");
        }

        String sql = "INSERT INTO recompensas "
                + "(nombre, costo_puntos, id_producto, id_menu, estado) "
                + "VALUES (?, ?, ?, ?, 'ACTIVA')";

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setInt(2, costoPuntos);

            if (idProducto == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, idProducto);
            }

            if (idMenu == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, idMenu);
            }

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "No se registró la recompensa.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println(
                            "Recompensa registrada. ID: "
                            + rs.getInt(1));
                }
            }
        }
    }

    private static void registrarComponenteMenu(Connection conexion)
            throws SQLException {

        listarMenus(conexion);

        int idMenu = leerEnteroPositivo(
                "ID del menú que tendrá el componente: ");

        listarProductos(conexion);

        int idProducto = leerEnteroPositivo(
                "ID del producto que se asociará: ");

        int cantidad = leerEnteroPositivo(
                "Cantidad del componente: ");

        String sql = "INSERT INTO menu_componentes "
                + "(id_menu, id_producto, cantidad) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);

            ps.executeUpdate();

            System.out.println(
                    "Componente asociado al menú correctamente.");
        }
    }

    private static void listarDatos(Connection conexion)
            throws SQLException {

        listarProductos(conexion);
        listarMenus(conexion);
        listarClientes(conexion);
        listarRecompensas(conexion);
        listarPedidos(conexion);
    }

    private static void listarPedidos(Connection conexion)
            throws SQLException {

        String sql = "SELECT p.id_pedido, p.fecha, p.estado, p.total, "
                + "c.nombre AS cliente, pa.metodo, "
                + "pa.estado AS estado_pago "
                + "FROM pedidos p "
                + "INNER JOIN clientes c ON c.id_cliente = p.id_cliente "
                + "LEFT JOIN pagos pa ON pa.id_pedido = p.id_pedido "
                + "ORDER BY p.fecha DESC, p.id_pedido DESC";

        System.out.println("\n--- PEDIDOS Y PAGOS ---");

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                System.out.printf(
                        "Pedido %d | Cliente: %s | Fecha: %s | Estado: %s "
                        + "| Total: Q %.2f | Método: %s | Pago: %s%n",
                        rs.getInt("id_pedido"),
                        rs.getString("cliente"),
                        rs.getTimestamp("fecha"),
                        rs.getString("estado"),
                        rs.getDouble("total"),
                        rs.getString("metodo"),
                        rs.getString("estado_pago"));
            }

            if (!hay) {
                System.out.println(
                        "Todavía no hay pedidos registrados.");
            }
        }
    }

    private static void cambiarEstadoPedido(
            Connection conexion,
            String destino) throws SQLException {

        listarPedidos(conexion);

        int idPedido = leerEnteroPositivo(
                "ID del pedido: ");

        String estadoActualPermitido =
                "ENTREGADO".equals(destino)
                ? "PAGADO"
                : "PENDIENTE";

        String sql = "UPDATE pedidos SET estado = ? "
                + "WHERE id_pedido = ? AND estado = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, destino);
            ps.setInt(2, idPedido);
            ps.setString(3, estadoActualPermitido);

            if (ps.executeUpdate() > 0) {
                System.out.println(
                        "Pedido " + idPedido
                        + " actualizado a " + destino + ".");
            } else {
                System.out.println(
                        "No se pudo actualizar. Verificá que el pedido "
                        + "exista y esté en estado "
                        + estadoActualPermitido + ".");
            }
        }
    }

    private static void listarProductos(Connection conexion)
            throws SQLException {

        String sql = "SELECT id_producto, codigo, nombre, categoria, "
                + "precio, existencia, estado "
                + "FROM productos ORDER BY id_producto";

        System.out.println("\n--- PRODUCTOS ---");

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                System.out.printf(
                        "ID %d | %s | %s | Categoría: %s | Q %.2f "
                        + "| Existencia: %d | %s%n",
                        rs.getInt("id_producto"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getInt("existencia"),
                        rs.getString("estado"));
            }

            if (!hay) {
                System.out.println("Sin registros.");
            }
        }
    }

    private static void listarMenus(Connection conexion)
            throws SQLException {

        String sql = "SELECT id_menu, codigo, nombre, precio, estado "
                + "FROM menus ORDER BY id_menu";

        System.out.println("\n--- MENÚ DE VENTAS ---");

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                System.out.printf(
                        "ID %d | %s | %s | Q %.2f | %s%n",
                        rs.getInt("id_menu"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getString("estado"));
            }

            if (!hay) {
                System.out.println("Sin registros.");
            }
        }
    }

    private static void listarClientes(Connection conexion)
            throws SQLException {

        String sql = "SELECT id_cliente, identificador, nombre, puntos, estado "
                + "FROM clientes ORDER BY id_cliente";

        System.out.println("\n--- CLIENTES ---");

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                System.out.printf(
                        "ID %d | %s | %s | Puntos: %d | %s%n",
                        rs.getInt("id_cliente"),
                        rs.getString("identificador"),
                        rs.getString("nombre"),
                        rs.getInt("puntos"),
                        rs.getString("estado"));
            }

            if (!hay) {
                System.out.println("Sin registros.");
            }
        }
    }

    private static void listarRecompensas(Connection conexion)
            throws SQLException {

        String sql = "SELECT id_recompensa, nombre, costo_puntos, "
                + "id_producto, id_menu, estado "
                + "FROM recompensas ORDER BY id_recompensa";

        System.out.println("\n--- RECOMPENSAS ---");

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                int idProducto = rs.getInt("id_producto");
                boolean productoNulo = rs.wasNull();

                int idMenu = rs.getInt("id_menu");
                boolean menuNulo = rs.wasNull();

                String asociado = productoNulo
                        ? "Menú ID " + idMenu
                        : "Producto ID " + idProducto;

                if (productoNulo && menuNulo) {
                    asociado = "Sin elemento asociado";
                }

                System.out.printf(
                        "ID %d | %s | %d puntos | %s | %s%n",
                        rs.getInt("id_recompensa"),
                        rs.getString("nombre"),
                        rs.getInt("costo_puntos"),
                        asociado,
                        rs.getString("estado"));
            }

            if (!hay) {
                System.out.println("Sin registros.");
            }
        }
    }

    private static String leerCategoria() {
        System.out.println("Categorías permitidas por la tabla actual:");
        System.out.println("1. SANDWICH");
        System.out.println("2. BEBIDA");
        System.out.println("3. ACOMPANAMIENTO");

        int opcion = leerOpcionRango(
                "Categoría: ", 1, 3);

        return switch (opcion) {
            case 1 -> "SANDWICH";
            case 2 -> "BEBIDA";
            default -> "ACOMPANAMIENTO";
        };
    }

    private static String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);

            String valor = SC.nextLine().trim();

            if (!valor.isEmpty()) {
                return valor;
            }

            System.out.println("Este campo es obligatorio.");
        }
    }

    private static String leerTextoOpcional(String mensaje) {
        System.out.print(mensaje);

        String valor = SC.nextLine().trim();

        return valor.isEmpty() ? null : valor;
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);

            try {
                return Integer.parseInt(
                        SC.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println(
                        "Ingresá un número entero válido.");
            }
        }
    }

    private static int leerEnteroPositivo(String mensaje) {
        while (true) {
            int valor = leerEntero(mensaje);

            if (valor > 0) {
                return valor;
            }

            System.out.println(
                    "El valor debe ser mayor que cero.");
        }
    }

    private static int leerEnteroNoNegativo(String mensaje) {
        while (true) {
            int valor = leerEntero(mensaje);

            if (valor >= 0) {
                return valor;
            }

            System.out.println(
                    "El valor no puede ser negativo.");
        }
    }

    private static int leerOpcionRango(
            String mensaje,
            int minimo,
            int maximo) {

        while (true) {
            int valor = leerEntero(mensaje);

            if (valor >= minimo && valor <= maximo) {
                return valor;
            }

            System.out.printf(
                    "Elegí un número entre %d y %d.%n",
                    minimo, maximo);
        }
    }

    private static double leerDoubleNoNegativo(String mensaje) {
        while (true) {
            System.out.print(mensaje);

            try {
                double valor = Double.parseDouble(
                        SC.nextLine().trim().replace(',', '.'));

                if (Double.isFinite(valor) && valor >= 0) {
                    return valor;
                }

            } catch (NumberFormatException ignored) {
            }

            System.out.println(
                    "Ingresá una cantidad válida, igual o mayor que cero.");
        }
    }
}