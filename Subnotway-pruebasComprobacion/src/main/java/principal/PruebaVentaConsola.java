package principal;

import config.ConexionDB;
import modelo.PedidoDetalle;
import modelo.PedidoDetalleExtra;
import servicio.PedidoServicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PruebaVentaConsola {

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" PRUEBA DE VENTA - SUBNOTWAY");
        System.out.println("========================================");
        System.out.println(
                "ATENCIÓN: esta prueba guarda una venta REAL en MySQL.");
        System.out.println(
                "Descontará inventario y podrá acumular puntos.\n");

        try {
            mostrarClientes();

            int idCliente = leerInt(
                    "ID del cliente activo: ");

            String tipoPrincipal = leerOpcion(
                    "¿Qué vas a vender? Escribe P para producto o M para menú: ",
                    "P", "M");

            Integer idProducto = null;
            Integer idMenu = null;
            double precioPrincipal;

            if ("P".equals(tipoPrincipal)) {
                mostrarProductos();

                idProducto = leerInt(
                        "ID del producto principal: ");

                precioPrincipal = obtenerPrecioProducto(idProducto);

            } else {
                mostrarMenus();

                idMenu = leerInt(
                        "ID del menú: ");

                precioPrincipal = obtenerPrecioMenu(idMenu);
            }

            int cantidadPrincipal = leerIntPositivo(
                    "Cantidad del producto/menú principal: ");

            List<PedidoDetalleExtra> extras = new ArrayList<>();

            double total = precioPrincipal * cantidadPrincipal;

            while (leerSiNo("¿Agregar un extra? (S/N): ")) {
                mostrarProductos();

                int idExtra = leerInt(
                        "ID del producto que será extra: ");

                int cantidadExtra = leerIntPositivo(
                        "Cantidad de ese extra POR CADA unidad principal: ");

                double precioExtra = obtenerPrecioProducto(idExtra);

                PedidoDetalleExtra extra = new PedidoDetalleExtra(
                        0,
                        0,
                        idExtra,
                        cantidadExtra,
                        precioExtra);

                extras.add(extra);

                total += precioExtra
                        * cantidadPrincipal
                        * cantidadExtra;
            }

            PedidoDetalle detalle = new PedidoDetalle(
                    0,
                    0,
                    idProducto,
                    idMenu,
                    cantidadPrincipal,
                    precioPrincipal);

            detalle.setExtras(extras);

            System.out.printf(
                    "\nTotal calculado: Q %.2f%n",
                    total);

            String metodoPago = leerOpcion(
                    "Método de pago (EFECTIVO/TARJETA): ",
                    "EFECTIVO", "TARJETA");

            double montoRecibido;
            boolean tarjetaAprobada = false;
            String referencia;

            if ("EFECTIVO".equals(metodoPago)) {
                montoRecibido = leerDoubleNoNegativo(
                        "Efectivo recibido en quetzales: Q ");

                referencia = leerTextoOpcional(
                        "Referencia (opcional; Enter para omitir): ");

            } else {
                System.out.println(
                        "Esta aplicación simula la aprobación de tarjeta.");

                tarjetaAprobada = leerSiNo(
                        "¿Marcar la tarjeta de prueba como aprobada? (S/N): ");

                montoRecibido = total;

                referencia = leerTextoOpcional(
                        "Referencia de prueba (opcional): ");
            }

            List<PedidoDetalle> detalles = new ArrayList<>();
            detalles.add(detalle);

            PedidoServicio servicio = new PedidoServicio();

            int idPedido = servicio.registrarVenta(
                    idCliente,
                    detalles,
                    metodoPago,
                    montoRecibido,
                    referencia,
                    tarjetaAprobada);

            System.out.println(
                    "\n¡VENTA REGISTRADA CORRECTAMENTE!");

            System.out.println(
                    "ID del pedido: " + idPedido);

            System.out.printf(
                    "Total: Q %.2f%n", total);

            System.out.println(
                    "El pedido, pago, inventario y puntos se procesaron.");

        } catch (NumberFormatException e) {
            System.out.println(
                    "Entrada inválida: escribí un número donde corresponde.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(
                    "No se pudo registrar la venta: " + e.getMessage());

        } catch (SQLException e) {
            System.out.println(
                    "Error de base de datos: " + e.getMessage());

            e.printStackTrace();

        } catch (Exception e) {
            System.out.println(
                    "Ocurrió un error: " + e.getMessage());

            e.printStackTrace();
        }
    }

    private static void mostrarClientes() throws SQLException {
        String sql = "SELECT id_cliente, nombre, puntos "
                + "FROM clientes WHERE estado = 'ACTIVO' "
                + "ORDER BY id_cliente";

        System.out.println("\n--- CLIENTES ACTIVOS ---");

        try (Connection c = ConexionDB.conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                System.out.printf(
                        "ID: %d | %s | Puntos: %d%n",
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getInt("puntos"));
            }

            if (!hay) {
                System.out.println("No hay clientes activos.");
            }
        }
    }

    private static void mostrarProductos() throws SQLException {
        String sql = "SELECT id_producto, codigo, nombre, categoria, "
                + "precio, existencia FROM productos "
                + "WHERE estado = 'ACTIVO' ORDER BY id_producto";

        System.out.println(
                "\n--- PRODUCTOS ACTIVOS ---");

        try (Connection c = ConexionDB.conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.printf(
                        "ID: %d | %s | %s | %s | Q %.2f "
                        + "| Existencia: %d%n",
                        rs.getInt("id_producto"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getInt("existencia"));
            }
        }
    }

    private static void mostrarMenus() throws SQLException {
        String sql = "SELECT id_menu, codigo, nombre, precio "
                + "FROM menus WHERE estado = 'ACTIVO' "
                + "ORDER BY id_menu";

        System.out.println("\n--- MENÚS ACTIVOS ---");

        try (Connection c = ConexionDB.conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.printf(
                        "ID: %d | %s | %s | Q %.2f%n",
                        rs.getInt("id_menu"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"));
            }
        }
    }

    private static double obtenerPrecioProducto(
            int idProducto) throws SQLException {

        String sql = "SELECT precio FROM productos "
                + "WHERE id_producto = ? AND estado = 'ACTIVO'";

        try (Connection c = ConexionDB.conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("precio");
                }
            }
        }

        throw new IllegalArgumentException(
                "No existe un producto activo con ID " + idProducto);
    }

    private static double obtenerPrecioMenu(
            int idMenu) throws SQLException {

        String sql = "SELECT precio FROM menus "
                + "WHERE id_menu = ? AND estado = 'ACTIVO'";

        try (Connection c = ConexionDB.conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("precio");
                }
            }
        }

        throw new IllegalArgumentException(
                "No existe un menú activo con ID " + idMenu);
    }

    private static int leerInt(String mensaje) {
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

    private static int leerIntPositivo(String mensaje) {
        while (true) {
            int valor = leerInt(mensaje);

            if (valor > 0) {
                return valor;
            }

            System.out.println(
                    "La cantidad debe ser mayor que cero.");
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
                    "Ingresá un importe válido, igual o mayor que cero.");
        }
    }

    private static String leerOpcion(
            String mensaje,
            String opcion1,
            String opcion2) {

        while (true) {
            System.out.print(mensaje);

            String valor = SC.nextLine().trim().toUpperCase();

            if (opcion1.equals(valor) || opcion2.equals(valor)) {
                return valor;
            }

            System.out.println("Opción no válida.");
        }
    }

    private static boolean leerSiNo(String mensaje) {
        while (true) {
            System.out.print(mensaje);

            String valor = SC.nextLine().trim().toUpperCase();

            if ("S".equals(valor)
                    || "SI".equals(valor)
                    || "SÍ".equals(valor)) {
                return true;
            }

            if ("N".equals(valor) || "NO".equals(valor)) {
                return false;
            }

            System.out.println("Respondé S o N.");
        }
    }

    private static String leerTextoOpcional(String mensaje) {
        System.out.print(mensaje);

        String valor = SC.nextLine().trim();

        return valor.isEmpty() ? null : valor;
    }
}