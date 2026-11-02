package principal;

import config.ConexionDB;
import modelo.PedidoDetalle;
import modelo.PedidoDetalleExtra;
import servicio.PedidoServicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class RegistrarDatosConsola {

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection conexion = ConexionDB.conectar()) {
            if (conexion == null || conexion.isClosed()) {
                throw new SQLException(
                        "No se pudo establecer la conexión con MySQL.");
            }

            int opcion;
            do {
                mostrarMenuPrincipal();
                opcion = leerOpcionRango("Seleccioná una opción: ", 0, 2);

                if (opcion == 1) {
                    menuCliente(conexion);
                } else if (opcion == 2) {
                    menuEmpleado(conexion);
                } else {
                    System.out.println("Saliendo de Subnotway.");
                }
            } while (opcion != 0);

        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
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
            System.out.println("2. Consultar catálogo disponible");
            System.out.println("3. Consultar saldo de puntos");
            System.out.println("4. Realizar pedido y pagar");
            System.out.println("5. Canjear recompensa con puntos");
            System.out.println("0. Volver al menú principal");

            opcion = leerOpcionRango("Seleccioná una opción: ", 0, 5);

            try {
                switch (opcion) {
                    case 1 -> registrarCliente(conexion);
                    case 2 -> listarCatalogoCliente(conexion);
                    case 3 -> consultarPuntosCliente(conexion);
                    case 4 -> realizarPedidoCliente(conexion);
                    case 5 -> canjearRecompensa(conexion);
                    case 0 -> System.out.println(
                            "Volviendo al menú principal.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                System.out.println(
                        "No se pudo completar la operación: "
                        + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato inválido: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println(
                        "No se pudo completar la operación: "
                        + e.getMessage());
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
            System.out.println("2. Reponer inventario de un producto");
            System.out.println("3. Modificar producto");
            System.out.println("4. Desactivar producto");
            System.out.println("5. Registrar elemento del menú de ventas");
            System.out.println("6. Modificar elemento del menú");
            System.out.println("7. Desactivar elemento del menú");
            System.out.println("8. Registrar recompensa");
            System.out.println("9. Desactivar recompensa");
            System.out.println("10. Agregar componente a un elemento del menú");
            System.out.println("11. Consultar datos registrados");
            System.out.println("12. Consultar pedidos y pagos");
            System.out.println("13. Consultar detalle de un pedido");
            System.out.println("14. Marcar pedido como entregado");
            System.out.println("15. Anular pedido pendiente");
            System.out.println("0. Volver al menú principal");

            opcion = leerOpcionRango("Seleccioná una opción: ", 0, 15);

            try {
                switch (opcion) {
                    case 1 -> registrarProducto(conexion);
                    case 2 -> reponerInventario(conexion);
                    case 3 -> modificarProducto(conexion);
                    case 4 -> cambiarEstadoRegistro(
                            conexion, "productos", "id_producto", "INACTIVO");
                    case 5 -> registrarMenu(conexion);
                    case 6 -> modificarMenu(conexion);
                    case 7 -> cambiarEstadoRegistro(
                            conexion, "menus", "id_menu", "INACTIVO");
                    case 8 -> registrarRecompensa(conexion);
                    case 9 -> desactivarRecompensa(conexion);
                    case 10 -> registrarComponenteMenu(conexion);
                    case 11 -> listarDatos(conexion);
                    case 12 -> listarPedidos(conexion);
                    case 13 -> listarDetallePedido(conexion);
                    case 14 -> cambiarEstadoPedido(conexion, "ENTREGADO");
                    case 15 -> cambiarEstadoPedido(conexion, "ANULADO");
                    case 0 -> System.out.println(
                            "Volviendo al menú principal.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                System.out.println(
                        "No se pudo completar la operación: "
                        + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato inválido: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void listarCatalogoCliente(Connection conexion)
            throws SQLException {

        String sql = "SELECT m.id_menu, m.codigo, m.nombre, m.precio "
                + "FROM menus m WHERE m.estado = 'ACTIVO' "
                + "AND EXISTS (SELECT 1 FROM menu_componentes mc "
                + "WHERE mc.id_menu = m.id_menu) "
                + "AND NOT EXISTS (SELECT 1 FROM menu_componentes mc "
                + "INNER JOIN productos p "
                + "ON p.id_producto = mc.id_producto "
                + "WHERE mc.id_menu = m.id_menu "
                + "AND (p.estado <> 'ACTIVO' "
                + "OR p.existencia < mc.cantidad)) "
                + "ORDER BY m.nombre";

        System.out.println("\n--- CATÁLOGO DISPONIBLE ---");

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
                        "No hay elementos disponibles. "
                        + "Revisá el menú y sus componentes.");
            }
        }
    }

    private static void realizarPedidoCliente(Connection conexion)
            throws SQLException {

        String identificador = leerTexto(
                "Identificador del cliente: ");

        int idCliente = obtenerIdClienteActivo(
                conexion, identificador);

        if (idCliente <= 0) {
            System.out.println(
                    "No existe un cliente activo con ese identificador.");
            return;
        }

        List<PedidoDetalle> detalles = new ArrayList<>();
        double totalEstimado = 0;
        boolean continuar = true;

        while (continuar) {
            System.out.println("\n--- AGREGAR ARTÍCULO AL PEDIDO ---");
            System.out.println("1. Producto de inventario");
            System.out.println("2. Elemento del menú");
            System.out.println("0. Terminar de agregar artículos");

            int tipo = leerOpcionRango(
                    "Tipo de artículo: ", 0, 2);

            if (tipo == 0) {
                if (detalles.isEmpty()) {
                    System.out.println("El pedido está vacío.");
                    return;
                }
                break;
            }

            PedidoDetalle detalle;
            double subtotal;

            if (tipo == 1) {
                listarProductosVendibles(conexion);

                int idProducto = leerEnteroPositivo(
                        "ID del producto: ");

                ProductoPedido producto = obtenerProductoVendible(
                        conexion, idProducto);

                if (producto == null) {
                    System.out.println(
                            "El producto no existe, está inactivo "
                            + "o no tiene stock.");
                    continue;
                }

                int cantidad = leerEnteroPositivo("Cantidad: ");

                if (cantidad > producto.existencia) {
                    System.out.println(
                            "Stock disponible: " + producto.existencia);
                    continue;
                }

                detalle = new PedidoDetalle(
                        0, 0, idProducto, null,
                        cantidad, producto.precio);

                List<PedidoDetalleExtra> extras = new ArrayList<>();

                if ("SANDWICH".equals(producto.categoria)) {
                    while (leerSiNo(
                            "¿Agregar un producto extra a este sándwich? (S/N): ")) {

                        listarProductosVendibles(conexion);

                        int idExtra = leerEnteroPositivo(
                                "ID del producto extra: ");

                        ProductoPedido extra = obtenerProductoVendible(
                                conexion, idExtra);

                        if (extra == null) {
                            System.out.println(
                                    "El producto extra no existe, "
                                    + "está inactivo o no tiene stock.");
                            continue;
                        }

                        int cantidadExtra = leerEnteroPositivo(
                                "Cantidad extra POR CADA sándwich: ");

                        int consumoExtra = cantidad * cantidadExtra;

                        if (consumoExtra > extra.existencia) {
                            System.out.println(
                                    "Stock insuficiente. Disponibles: "
                                    + extra.existencia
                                    + ". Necesarios para este pedido: "
                                    + consumoExtra);
                            continue;
                        }

                        extras.add(new PedidoDetalleExtra(
                                0, 0, idExtra,
                                cantidadExtra, extra.precio));

                        System.out.printf(
                                "Extra agregado: %s x%d por sándwich.%n",
                                extra.nombre, cantidadExtra);
                    }
                }

                detalle.setExtras(extras);

                subtotal = producto.precio * cantidad;

                for (PedidoDetalleExtra extra : extras) {
                    ProductoPedido infoExtra = obtenerProductoVendible(
                            conexion, extra.getIdProducto());

                    if (infoExtra != null) {
                        subtotal += infoExtra.precio
                                * cantidad
                                * extra.getCantidad();
                    }
                }

                System.out.printf(
                        "Agregado: %s x%d | Subtotal estimado: Q %.2f%n",
                        producto.nombre, cantidad, subtotal);

            } else {
                listarMenusDisponibles(conexion);

                int idMenu = leerEnteroPositivo(
                        "ID del elemento del menú: ");

                MenuPedido menu = obtenerMenuDisponible(
                        conexion, idMenu);

                if (menu == null) {
                    System.out.println(
                            "El elemento no existe, está inactivo "
                            + "o sus componentes no tienen existencias suficientes.");
                    continue;
                }

                int cantidad = leerEnteroPositivo("Cantidad: ");

                if (!menuTieneExistenciasParaCantidad(
                        conexion, idMenu, cantidad)) {

                    System.out.println(
                            "No hay componentes suficientes para esa cantidad.");
                    continue;
                }

                detalle = new PedidoDetalle(
                        0, 0, null, idMenu,
                        cantidad, menu.precio);

                detalle.setExtras(new ArrayList<>());

                subtotal = menu.precio * cantidad;

                System.out.printf(
                        "Agregado: %s x%d | Subtotal estimado: Q %.2f%n",
                        menu.nombre, cantidad, subtotal);
            }

            detalles.add(detalle);
            totalEstimado += subtotal;

            System.out.printf(
                    "Total estimado del pedido: Q %.2f%n",
                    totalEstimado);

            continuar = leerSiNo(
                    "¿Agregar otro artículo diferente? (S/N): ");
        }

        if (detalles.isEmpty()) {
            System.out.println(
                    "No se registró el pedido porque el carrito está vacío.");
            return;
        }

        System.out.printf(
                "\nTOTAL ESTIMADO: Q %.2f%n", totalEstimado);

        System.out.println("1. Efectivo");
        System.out.println("2. Tarjeta (simulada)");

        int pago = leerOpcionRango("Método de pago: ", 1, 2);

        String metodoPago = pago == 1 ? "EFECTIVO" : "TARJETA";
        double montoRecibido;
        String referencia = null;
        boolean tarjetaAprobada = true;

        if ("EFECTIVO".equals(metodoPago)) {
            montoRecibido = leerDoubleNoNegativo(
                    "Efectivo recibido (Q): ");

            if (montoRecibido < totalEstimado) {
                System.out.println(
                        "El efectivo es insuficiente. "
                        + "No se registró la venta.");
                return;
            }

        } else {
            tarjetaAprobada = leerSiNo(
                    "¿Aprobar el pago de tarjeta de prueba? (S/N): ");

            if (!tarjetaAprobada) {
                System.out.println(
                        "Pago rechazado. No se registró la venta "
                        + "ni se descontó inventario.");
                return;
            }

            referencia = leerTextoOpcional(
                    "Referencia de prueba (opcional): ");

            montoRecibido = totalEstimado;
        }

        int confirmacion = leerOpcionRango(
                "Confirmar pedido y pago: 1=Sí, 0=No: ", 0, 1);

        if (confirmacion == 0) {
            System.out.println(
                    "Pedido cancelado antes de guardarse.");
            return;
        }

        int idPedido = new PedidoServicio().registrarVenta(
                idCliente,
                detalles,
                metodoPago,
                montoRecibido,
                referencia,
                tarjetaAprobada);

        System.out.println("\nPEDIDO REGISTRADO CORRECTAMENTE.");
        System.out.println("Número de pedido: " + idPedido);

        System.out.printf(
                "Total estimado: Q %.2f%n", totalEstimado);

        if ("EFECTIVO".equals(metodoPago)) {
            System.out.printf(
                    "Cambio estimado: Q %.2f%n",
                    montoRecibido - totalEstimado);
        }

        System.out.println(
                "El servicio volvió a validar precios "
                + "y existencias al registrar la venta.");
    }

    private static int obtenerIdClienteActivo(
            Connection conexion,
            String identificador) throws SQLException {

        String sql = "SELECT id_cliente FROM clientes "
                + "WHERE identificador = ? AND estado = 'ACTIVO'";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, identificador);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id_cliente") : -1;
            }
        }
    }

    
private static void canjearRecompensa(Connection conexion)
        throws SQLException {

    String identificador = leerTexto(
            "Identificador del cliente: ");

    int idCliente;

    String sqlCliente = """
        SELECT id_cliente, puntos
        FROM clientes
        WHERE identificador = ?
          AND estado = 'ACTIVO'
        """;

    try (PreparedStatement ps = conexion.prepareStatement(sqlCliente)) {
        ps.setString(1, identificador);

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new IllegalArgumentException(
                        "No existe un cliente activo con ese identificador.");
            }

            idCliente = rs.getInt("id_cliente");

            System.out.println(
                    "Puntos disponibles: " + rs.getInt("puntos"));
        }
    }

    // Mostrar las recompensas disponibles antes de elegir.
    listarRecompensasCanjeables(conexion);

    int idRecompensa = leerEnteroPositivo(
            "ID de la recompensa que deseas canjear: ");

    // Delegar el procesamiento al servicio.
    servicio.CanjeServicio servicioCanje =
            new servicio.CanjeServicio();

    int idCanje = servicioCanje.realizarCanje(
            conexion, idCliente, idRecompensa);

    System.out.println("¡Canje realizado correctamente!");
    System.out.println("Número de canje: " + idCanje);

    // Consultar el saldo actualizado desde la base de datos.
    String sqlPuntos = """
        SELECT puntos
        FROM clientes
        WHERE id_cliente = ?
        """;

    try (PreparedStatement ps = conexion.prepareStatement(sqlPuntos)) {
        ps.setInt(1, idCliente);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println(
                        "Saldo restante: " + rs.getInt("puntos"));
            }
        }
    }
}


    private static void listarRecompensasCanjeables(
            Connection conexion) throws SQLException {

        String sql = "SELECT r.id_recompensa, r.nombre, r.costo_puntos, "
                + "CASE WHEN r.id_producto IS NOT NULL "
                + "THEN p.nombre ELSE m.nombre END AS articulo "
                + "FROM recompensas r "
                + "LEFT JOIN productos p "
                + "ON p.id_producto = r.id_producto "
                + "LEFT JOIN menus m "
                + "ON m.id_menu = r.id_menu "
                + "WHERE r.estado = 'ACTIVA' "
                + "ORDER BY r.costo_puntos, r.nombre";

        System.out.println("\n--- RECOMPENSAS ACTIVAS ---");

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                String articulo = rs.getString("articulo");

                if (articulo == null) {
                    articulo = "Artículo no asociado";
                }

                System.out.printf(
                        "ID %d | %s | Entrega: %s | Costo: %d puntos%n",
                        rs.getInt("id_recompensa"),
                        rs.getString("nombre"),
                        articulo,
                        rs.getInt("costo_puntos"));
            }

            if (!hay) {
                System.out.println(
                        "No hay recompensas activas registradas.");
            }
        }
    }



    private static void listarProductosVendibles(Connection conexion)
            throws SQLException {

        String sql = "SELECT id_producto, codigo, nombre, categoria, "
                + "precio, existencia FROM productos "
                + "WHERE estado = 'ACTIVO' AND existencia > 0 "
                + "ORDER BY nombre";

        System.out.println("\n--- PRODUCTOS ACTIVOS CON STOCK ---");

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                System.out.printf(
                        "ID %d | %s | %s | %s | Q %.2f | Stock: %d%n",
                        rs.getInt("id_producto"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getInt("existencia"));
            }

            if (!hay) {
                System.out.println(
                        "No hay productos activos con existencias.");
            }
        }
    }

    private static ProductoPedido obtenerProductoVendible(
            Connection conexion,
            int idProducto) throws SQLException {

        String sql = "SELECT id_producto, nombre, categoria, precio, existencia "
                + "FROM productos WHERE id_producto = ? "
                + "AND estado = 'ACTIVO' AND existencia > 0";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new ProductoPedido(
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getInt("existencia"));
            }
        }
    }

    private static void listarMenusDisponibles(Connection conexion)
            throws SQLException {

        String sql = "SELECT m.id_menu, m.codigo, m.nombre, m.precio "
                + "FROM menus m WHERE m.estado = 'ACTIVO' "
                + "AND EXISTS (SELECT 1 FROM menu_componentes mc "
                + "WHERE mc.id_menu = m.id_menu) "
                + "AND NOT EXISTS (SELECT 1 FROM menu_componentes mc "
                + "INNER JOIN productos p "
                + "ON p.id_producto = mc.id_producto "
                + "WHERE mc.id_menu = m.id_menu "
                + "AND (p.estado <> 'ACTIVO' "
                + "OR p.existencia < mc.cantidad)) "
                + "ORDER BY m.nombre";

        System.out.println("\n--- ELEMENTOS DEL MENÚ DISPONIBLES ---");

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
                        "No hay elementos del menú disponibles "
                        + "con componentes y stock suficientes.");
            }
        }
    }

    private static MenuPedido obtenerMenuDisponible(
            Connection conexion,
            int idMenu) throws SQLException {

        String sql = "SELECT m.id_menu, m.nombre, m.precio "
                + "FROM menus m "
                + "WHERE m.id_menu = ? AND m.estado = 'ACTIVO' "
                + "AND EXISTS (SELECT 1 FROM menu_componentes mc "
                + "WHERE mc.id_menu = m.id_menu) "
                + "AND NOT EXISTS (SELECT 1 FROM menu_componentes mc "
                + "INNER JOIN productos p "
                + "ON p.id_producto = mc.id_producto "
                + "WHERE mc.id_menu = m.id_menu "
                + "AND (p.estado <> 'ACTIVO' "
                + "OR p.existencia < mc.cantidad))";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new MenuPedido(
                        rs.getInt("id_menu"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"));
            }
        }
    }

    private static boolean menuTieneExistenciasParaCantidad(
            Connection conexion,
            int idMenu,
            int cantidadMenu) throws SQLException {

        String sql = "SELECT p.existencia, p.estado, mc.cantidad "
                + "FROM menu_componentes mc "
                + "INNER JOIN productos p "
                + "ON p.id_producto = mc.id_producto "
                + "WHERE mc.id_menu = ?";

        boolean tiene = false;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tiene = true;

                    long necesario = (long) rs.getInt("cantidad")
                            * cantidadMenu;

                    if (!"ACTIVO".equals(rs.getString("estado"))
                            || rs.getInt("existencia") < necesario) {
                        return false;
                    }
                }
            }
        }

        return tiene;
    }

    private static boolean leerSiNo(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = SC.nextLine().trim().toUpperCase();

            if (valor.equals("S")
                    || valor.equals("SI")
                    || valor.equals("SÍ")) {
                return true;
            }

            if (valor.equals("N") || valor.equals("NO")) {
                return false;
            }

            System.out.println("Respondé S o N.");
        }
    }

    private static final class ProductoPedido {

        private final int id;
        private final String nombre;
        private final String categoria;
        private final double precio;
        private final int existencia;

        private ProductoPedido(
                int id,
                String nombre,
                String categoria,
                double precio,
                int existencia) {

            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.precio = precio;
            this.existencia = existencia;
        }
    }

    private static final class MenuPedido {

        private final int id;
        private final String nombre;
        private final double precio;

        private MenuPedido(
                int id,
                String nombre,
                double precio) {

            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
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
                insertarMovimientoInventario(
                        conexion,
                        idProducto,
                        existencia,
                        "Registro inicial desde Java");
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

    private static void reponerInventario(Connection conexion)
            throws SQLException {

        listarProductos(conexion);

        int idProducto = leerEnteroPositivo(
                "ID del producto que se repondrá: ");

        int cantidad = leerEnteroPositivo(
                "Cantidad que ingresará al inventario: ");

        boolean autoCommitAnterior = conexion.getAutoCommit();

        try {
            conexion.setAutoCommit(false);

            String actualizar = "UPDATE productos "
                    + "SET existencia = existencia + ? "
                    + "WHERE id_producto = ? AND estado = 'ACTIVO'";

            try (PreparedStatement ps = conexion.prepareStatement(actualizar)) {
                ps.setInt(1, cantidad);
                ps.setInt(2, idProducto);

                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException(
                            "El producto no existe o está inactivo.");
                }
            }

            insertarMovimientoInventario(
                    conexion,
                    idProducto,
                    cantidad,
                    "Reposición de inventario desde área de empleado");

            conexion.commit();

            System.out.println(
                    "Inventario actualizado. Se ingresaron "
                    + cantidad + " unidades.");

        } catch (SQLException | IllegalArgumentException e) {
            conexion.rollback();
            throw e;

        } finally {
            conexion.setAutoCommit(autoCommitAnterior);
        }
    }

    private static void insertarMovimientoInventario(
            Connection conexion,
            int idProducto,
            int cantidad,
            String observacion) throws SQLException {

        String sql = "INSERT INTO movimientos_inventario "
                + "(id_producto, tipo, cantidad, observacion) "
                + "VALUES (?, 'ENTRADA', ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, cantidad);
            ps.setString(3, observacion);
            ps.executeUpdate();
        }
    }

    private static void modificarProducto(Connection conexion)
            throws SQLException {

        listarProductos(conexion);

        int idProducto = leerEnteroPositivo(
                "ID del producto que modificarás: ");

        String nombre = leerTexto(
                "Nuevo nombre del producto: ");

        String categoria = leerCategoria();

        double precio = leerDoubleNoNegativo(
                "Nuevo precio/costo (Q): ");

        String sql = "UPDATE productos "
                + "SET nombre = ?, categoria = ?, precio = ? "
                + "WHERE id_producto = ? AND estado = 'ACTIVO'";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, categoria);
            ps.setDouble(3, precio);
            ps.setInt(4, idProducto);

            if (ps.executeUpdate() > 0) {
                System.out.println(
                        "Producto actualizado correctamente.");
            } else {
                System.out.println(
                        "No se encontró un producto activo con ese ID.");
            }
        }
    }

    private static void registrarMenu(Connection conexion)
            throws SQLException {

        String codigo = leerTexto("Código único del menú: ");

        String nombre = leerTexto(
                "Nombre comercial del elemento "
                + "(sándwich, bebida, acompañamiento o combo): ");

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
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println(
                            "Elemento del menú registrado. ID: "
                            + rs.getInt(1));
                }
            }
        }
    }

    private static void modificarMenu(Connection conexion)
            throws SQLException {

        listarMenus(conexion);

        int idMenu = leerEnteroPositivo(
                "ID del elemento del menú que modificarás: ");

        String nombre = leerTexto(
                "Nuevo nombre del elemento: ");

        double precio = leerDoubleNoNegativo(
                "Nuevo precio de venta (Q): ");

        String sql = "UPDATE menus SET nombre = ?, precio = ? "
                + "WHERE id_menu = ? AND estado = 'ACTIVO'";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setDouble(2, precio);
            ps.setInt(3, idMenu);

            if (ps.executeUpdate() > 0) {
                System.out.println(
                        "Elemento del menú actualizado correctamente.");
            } else {
                System.out.println(
                        "No se encontró un elemento activo con ese ID.");
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
            ps.executeUpdate();

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

        String nombre = leerTexto(
                "Nombre de la recompensa: ");

        int costoPuntos = leerEnteroPositivo(
                "Puntos necesarios: ");

        System.out.println(
                "¿Qué se entregará como recompensa?");
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
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, idProducto);
            }

            if (idMenu == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, idMenu);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println(
                            "Recompensa registrada. ID: "
                            + rs.getInt(1));
                }
            }
        }
    }

    private static void desactivarRecompensa(Connection conexion)
            throws SQLException {

        listarRecompensas(conexion);

        int id = leerEnteroPositivo(
                "ID de la recompensa que desactivarás: ");

        String sql = "UPDATE recompensas SET estado = 'INACTIVA' "
                + "WHERE id_recompensa = ? AND estado = 'ACTIVA'";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            if (ps.executeUpdate() > 0) {
                System.out.println("Recompensa desactivada.");
            } else {
                System.out.println(
                        "No se encontró una recompensa activa con ese ID.");
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
                + "(id_menu, id_producto, cantidad) "
                + "VALUES (?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            ps.executeUpdate();

            System.out.println(
                    "Componente asociado al menú correctamente.");
        }
    }

    private static void cambiarEstadoRegistro(
            Connection conexion,
            String tabla,
            String columnaId,
            String estado) throws SQLException {

        if (!"productos".equals(tabla)
                && !"menus".equals(tabla)) {
            throw new IllegalArgumentException(
                    "Tipo de registro no permitido.");
        }

        if ("productos".equals(tabla)) {
            listarProductos(conexion);
        } else {
            listarMenus(conexion);
        }

        int id = leerEnteroPositivo(
                "ID del registro que desactivarás: ");

        String sql = "UPDATE " + tabla
                + " SET estado = ? WHERE " + columnaId
                + " = ? AND estado = 'ACTIVO'";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);

            if (ps.executeUpdate() > 0) {
                System.out.println(
                        "Registro desactivado correctamente.");
            } else {
                System.out.println(
                        "No se encontró un registro activo con ese ID.");
            }
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
                + "INNER JOIN clientes c "
                + "ON c.id_cliente = p.id_cliente "
                + "LEFT JOIN pagos pa "
                + "ON pa.id_pedido = p.id_pedido "
                + "ORDER BY p.fecha DESC, p.id_pedido DESC";

        System.out.println("\n--- PEDIDOS Y PAGOS ---");

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                System.out.printf(
                        "Pedido %d | Cliente: %s | Fecha: %s "
                        + "| Estado: %s | Total: Q %.2f "
                        + "| Método: %s | Pago: %s%n",
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

    private static void listarDetallePedido(Connection conexion)
            throws SQLException {

        listarPedidos(conexion);

        int idPedido = leerEnteroPositivo(
                "ID del pedido que deseas consultar: ");

        String sql = "SELECT d.id_detalle, d.cantidad, "
                + "d.precio_unitario, p.nombre AS producto, "
                + "m.nombre AS menu "
                + "FROM pedido_detalles d "
                + "LEFT JOIN productos p "
                + "ON p.id_producto = d.id_producto "
                + "LEFT JOIN menus m "
                + "ON m.id_menu = d.id_menu "
                + "WHERE d.id_pedido = ? "
                + "ORDER BY d.id_detalle";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hay = false;

                System.out.println(
                        "\n--- DETALLE DEL PEDIDO "
                        + idPedido + " ---");

                while (rs.next()) {
                    hay = true;

                    String articulo = rs.getString("producto");

                    if (articulo == null) {
                        articulo = rs.getString("menu");
                    }

                    System.out.printf(
                            "Detalle %d | %s | Cantidad: %d "
                            + "| Precio unitario: Q %.2f "
                            + "| Subtotal: Q %.2f%n",
                            rs.getInt("id_detalle"),
                            articulo,
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"),
                            rs.getInt("cantidad")
                            * rs.getDouble("precio_unitario"));
                }

                if (!hay) {
                    System.out.println(
                            "No hay detalles para ese pedido.");
                }
            }
        }
    }

    private static void cambiarEstadoPedido(
            Connection conexion,
            String destino) throws SQLException {

        listarPedidos(conexion);

        int idPedido = leerEnteroPositivo("ID del pedido: ");

        String estadoPermitido = "ENTREGADO".equals(destino)
                ? "PAGADO"
                : "PENDIENTE";

        String sql = "UPDATE pedidos SET estado = ? "
                + "WHERE id_pedido = ? AND estado = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, destino);
            ps.setInt(2, idPedido);
            ps.setString(3, estadoPermitido);

            if (ps.executeUpdate() > 0) {
                System.out.println(
                        "Pedido " + idPedido
                        + " actualizado a " + destino + ".");
            } else {
                System.out.println(
                        "No se pudo actualizar. El pedido debe existir "
                        + "y estar en estado "
                        + estadoPermitido + ".");
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
                        "ID %d | %s | %s | Categoría: %s "
                        + "| Q %.2f | Existencia: %d | %s%n",
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

        String sql = "SELECT id_cliente, identificador, nombre, "
                + "puntos, estado FROM clientes ORDER BY id_cliente";

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

                hay = true;

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
        System.out.println("Categorías permitidas:");
        System.out.println("1. SANDWICH");
        System.out.println("2. BEBIDA");
        System.out.println("3. ACOMPANAMIENTO");

        int opcion = leerOpcionRango("Categoría: ", 1, 3);

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