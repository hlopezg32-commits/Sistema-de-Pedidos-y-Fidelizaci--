package principal;

import dao.impl.MenuDAO;
import modelo.Cliente;
import modelo.Menu;
import modelo.PedidoDetalle;
import modelo.Producto;
import servicio.ClienteServicio;
import servicio.PedidoServicio;
import servicio.ProductoServicio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MenuConsola {

    private static final java.util.Scanner teclado =
            new java.util.Scanner(System.in);

    private static final ClienteServicio clienteServicio =
            new ClienteServicio();

    private static final ProductoServicio productoServicio =
            new ProductoServicio();

    private static final PedidoServicio pedidoServicio =
            new PedidoServicio();

    private static final MenuDAO menuDAO = new MenuDAO();

    public static void main(String[] args) {
        int opcion;

        do {
            System.out.println("\n===== SUBNOTWAY =====");
            System.out.println("--- CLIENTES ---");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Buscar cliente por ID");
            System.out.println("3. Buscar cliente por identificador");
            System.out.println("4. Listar clientes");
            System.out.println("5. Modificar cliente");
            System.out.println("6. Desactivar cliente");

            System.out.println("\n--- VENTAS ---");
            System.out.println("7. Consultar productos");
            System.out.println("8. Registrar venta");
            System.out.println("9. Marcar pedido como entregado");
            System.out.println("10. Anular pedido pendiente");
            System.out.println("0. Salir");
            System.out.print("Selecciona una opción: ");

            opcion = leerEntero();

            try {
                switch (opcion) {
                    case 1:
                        registrarCliente();
                        break;
                    case 2:
                        buscarClientePorId();
                        break;
                    case 3:
                        buscarClientePorIdentificador();
                        break;
                    case 4:
                        listarClientes();
                        break;
                    case 5:
                        modificarCliente();
                        break;
                    case 6:
                        desactivarCliente();
                        break;
                    case 7:
                        consultarProductos();
                        break;
                    case 8:
                        registrarVenta();
                        break;
                    case 9:
                        marcarEntregado();
                        break;
                    case 10:
                        anularPedido();
                        break;
                    case 0:
                        System.out.println("Saliendo de Subnotway...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                System.out.println("Error de base de datos: "
                        + e.getMessage());
            } catch (IllegalArgumentException
                    | IllegalStateException e) {
                System.out.println("Operación no completada: "
                        + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static int leerEntero() {
        while (!teclado.hasNextInt()) {
            System.out.print("Ingresa un número válido: ");
            teclado.nextLine();
        }

        int numero = teclado.nextInt();
        teclado.nextLine();
        return numero;
    }

    // ================= CLIENTES =================

    private static void registrarCliente() {
        System.out.println("\n--- REGISTRAR CLIENTE ---");

        System.out.print("Identificador: ");
        String identificador = teclado.nextLine().trim();

        System.out.print("Nombre completo: ");
        String nombre = teclado.nextLine().trim();

        System.out.print("Teléfono: ");
        String telefono = teclado.nextLine().trim();

        System.out.print("Correo electrónico: ");
        String correo = teclado.nextLine().trim();

        Cliente cliente = new Cliente(
                0, identificador, nombre, telefono, correo, 0, "ACTIVO");

        if (clienteServicio.registrarCliente(cliente)) {
            System.out.println("Cliente registrado correctamente.");
        } else {
            System.out.println("No se pudo registrar el cliente.");
        }
    }

    private static void buscarClientePorId() {
        System.out.print("ID del cliente: ");
        Cliente cliente = clienteServicio.buscarClientePorId(
                leerEntero());

        if (cliente == null) {
            System.out.println("No se encontró el cliente.");
        } else {
            mostrarCliente(cliente);
        }
    }

    private static void buscarClientePorIdentificador() {
        System.out.print("Identificador del cliente: ");
        Cliente cliente = clienteServicio.buscarClientePorIdentificador(
                teclado.nextLine().trim());

        if (cliente == null) {
            System.out.println("No se encontró el cliente.");
        } else {
            mostrarCliente(cliente);
        }
    }

    private static void listarClientes() {
        List<Cliente> clientes = clienteServicio.listarClientes();

        if (clientes == null || clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }

        for (Cliente cliente : clientes) {
            mostrarCliente(cliente);
        }
    }

    private static void modificarCliente() {
        System.out.print("ID del cliente: ");
        Cliente cliente = clienteServicio.buscarClientePorId(
                leerEntero());

        if (cliente == null) {
            System.out.println("No se encontró el cliente.");
            return;
        }

        mostrarCliente(cliente);

        System.out.print("Nuevo identificador: ");
        cliente.setIdentificador(teclado.nextLine().trim());

        System.out.print("Nuevo nombre: ");
        cliente.setNombre(teclado.nextLine().trim());

        System.out.print("Nuevo teléfono: ");
        cliente.setTelefono(teclado.nextLine().trim());

        System.out.print("Nuevo correo: ");
        cliente.setCorreo(teclado.nextLine().trim());

        if (clienteServicio.actualizarCliente(cliente)) {
            System.out.println("Cliente actualizado.");
        } else {
            System.out.println("No se pudo actualizar.");
        }
    }

    private static void desactivarCliente() {
        System.out.print("ID del cliente: ");
        Cliente cliente = clienteServicio.buscarClientePorId(
                leerEntero());

        if (cliente == null) {
            System.out.println("No se encontró el cliente.");
            return;
        }

        System.out.print("¿Confirmas la desactivación? (S/N): ");

        if (teclado.nextLine().trim().equalsIgnoreCase("S")) {
            if (clienteServicio.desactivarCliente(cliente)) {
                System.out.println("Cliente desactivado.");
            } else {
                System.out.println("No se pudo desactivar.");
            }
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private static void mostrarCliente(Cliente cliente) {
        System.out.println("\n-------------------------");
        System.out.println("ID: " + cliente.getIdCliente());
        System.out.println("Identificador: "
                + cliente.getIdentificador());
        System.out.println("Nombre: " + cliente.getNombre());
        System.out.println("Teléfono: " + cliente.getTelefono());
        System.out.println("Correo: " + cliente.getCorreo());
        System.out.println("Puntos: " + cliente.getPuntos());
        System.out.println("Estado: " + cliente.getEstado());
        System.out.println("-------------------------");
    }

    // ================= PRODUCTOS =================

    private static void consultarProductos() {
        List<Producto> productos =
                productoServicio.listarProductosActivos();

        if (productos == null || productos.isEmpty()) {
            System.out.println("No hay productos activos.");
            return;
        }

        System.out.println("\n--- PRODUCTOS ACTIVOS ---");

        for (Producto p : productos) {
            System.out.println(
                    "ID: " + p.getIdProducto()
                    + " | Código: " + p.getCodigo()
                    + " | Nombre: " + p.getNombre()
                    + " | Categoría: " + p.getCategoria()
                    + " | Precio: Q" + p.getPrecio()
                    + " | Existencia: " + p.getExistencia());
        }
    }

    // ================= VENTAS =================

    private static void registrarVenta() throws SQLException {
        System.out.println("\n--- REGISTRAR VENTA ---");

        System.out.print("ID del cliente: ");
        int idCliente = leerEntero();

        Cliente cliente = clienteServicio.buscarClientePorId(idCliente);

        if (cliente == null
                || !"ACTIVO".equals(cliente.getEstado())) {
            System.out.println("El cliente no existe o está inactivo.");
            return;
        }

        List<PedidoDetalle> detalles = new ArrayList<>();

        int agregar;
        do {
            System.out.println("\nTipo de artículo:");
            System.out.println("1. Producto individual");
            System.out.println("2. Menú");
            System.out.print("Selecciona: ");

            int tipo = leerEntero();

            if (tipo == 1) {
                consultarProductos();

                System.out.print("ID del producto: ");
                int idProducto = leerEntero();

                System.out.print("Cantidad: ");
                int cantidad = leerEntero();

                if (cantidad <= 0) {
                    System.out.println(
                            "La cantidad debe ser positiva.");
                    return;
                }

                detalles.add(new PedidoDetalle(
                        0, 0, idProducto, null, cantidad, 0));

            } else if (tipo == 2) {
                List<Menu> menus = menuDAO.listarTodos();
                boolean hayMenus = false;

                if (menus != null) {
                    for (Menu menu : menus) {
                        if ("ACTIVO".equals(menu.getEstado())) {
                            hayMenus = true;
                            System.out.println(
                                    "ID: " + menu.getIdMenu()
                                    + " | Código: " + menu.getCodigo()
                                    + " | Nombre: " + menu.getNombre()
                                    + " | Precio: Q" + menu.getPrecio());
                        }
                    }
                }

                if (!hayMenus) {
                    System.out.println("No hay menús activos.");
                    return;
                }

                System.out.print("ID del menú: ");
                int idMenu = leerEntero();

                System.out.print("Cantidad: ");
                int cantidad = leerEntero();

                if (cantidad <= 0) {
                    System.out.println(
                            "La cantidad debe ser positiva.");
                    return;
                }

                detalles.add(new PedidoDetalle(
                        0, 0, null, idMenu, cantidad, 0));

            } else {
                System.out.println("Tipo no válido.");
                return;
            }

            System.out.print(
                    "¿Agregar otro artículo? (1=Sí, 0=No): ");
            agregar = leerEntero();

        } while (agregar == 1);

        // Calcular un importe provisional usando los precios actuales
        // de la base de datos. PedidoServicio vuelve a validar los
        // precios durante el registro de la venta.
        double totalEstimado = calcularTotalEstimado(detalles);

        System.out.printf(
                java.util.Locale.US,
                "%nTotal estimado: Q%.2f%n",
                totalEstimado);

        System.out.println("\nMétodo de pago:");
        System.out.println("1. Efectivo");
        System.out.println("2. Tarjeta");
        System.out.print("Selecciona: ");

        int opcionPago = leerEntero();
        String metodoPago;

        if (opcionPago == 1) {
            metodoPago = "EFECTIVO";
        } else if (opcionPago == 2) {
            metodoPago = "TARJETA";
        } else {
            System.out.println("Método de pago no válido.");
            return;
        }

        /*
         * PedidoServicio todavía recibe montoRecibido y exige que
         * coincida con el total para tarjeta. Se le pasa el total
         * estimado para evitar solicitar dinero o referencia.
         *
         * Esto representa un registro escolar del método elegido;
         * no procesa una transacción bancaria real.
         */
        int idPedido = pedidoServicio.registrarVenta(
                idCliente,
                detalles,
                metodoPago,
                totalEstimado,
                null,
                true);

        System.out.println("\n¡Venta registrada correctamente!");
        System.out.println("Número de pedido: " + idPedido);
        System.out.println("Cliente: " + cliente.getNombre());
        System.out.printf(
                java.util.Locale.US,
                "Total estimado: Q%.2f%n",
                totalEstimado);

        Cliente clienteActualizado =
                clienteServicio.buscarClientePorId(idCliente);

        if (clienteActualizado != null) {
            System.out.println(
                    "Puntos actuales: "
                    + clienteActualizado.getPuntos());
        }
    }

    private static double calcularTotalEstimado(
            List<PedidoDetalle> detalles) {

        double total = 0;

        for (PedidoDetalle detalle : detalles) {
            double precio;

            if (detalle.getIdProducto() != null) {
                Producto producto =
                        productoServicio.buscarProductoActivoPorId(
                                detalle.getIdProducto());

                if (producto == null) {
                    throw new IllegalArgumentException(
                            "Producto inexistente o inactivo: "
                            + detalle.getIdProducto());
                }

                precio = producto.getPrecio();

            } else {
                Menu menu = menuDAO.buscarPorId(
                        detalle.getIdMenu());

                if (menu == null
                        || !"ACTIVO".equals(menu.getEstado())) {
                    throw new IllegalArgumentException(
                            "Menú inexistente o inactivo: "
                            + detalle.getIdMenu());
                }

                precio = menu.getPrecio();
            }

            detalle.setPrecioUnitario(precio);
            total += precio * detalle.getCantidad();

            // Este menú no solicita extras, por lo que no se suman aquí.
        }

        if (!Double.isFinite(total) || total <= 0) {
            throw new IllegalArgumentException(
                    "El total estimado no es válido.");
        }

        return Math.round(total * 100.0) / 100.0;
    }

    private static void marcarEntregado() throws SQLException {
        System.out.print("ID del pedido pagado: ");
        int idPedido = leerEntero();

        if (pedidoServicio.marcarEntregado(idPedido)) {
            System.out.println("Pedido marcado como ENTREGADO.");
        } else {
            System.out.println(
                    "No se encontró un pedido PAGADO con ese ID.");
        }
    }

    private static void anularPedido() throws SQLException {
        System.out.print("ID del pedido pendiente: ");
        int idPedido = leerEntero();

        if (pedidoServicio.anularPedidoPendiente(idPedido)) {
            System.out.println("Pedido anulado.");
        } else {
            System.out.println(
                    "No se encontró un pedido PENDIENTE con ese ID.");
        }
    }
}
