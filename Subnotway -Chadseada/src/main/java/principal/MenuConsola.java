package principal;

import dao.impl.MenuDAO;
import modelo.Cliente;
import modelo.Menu;
import modelo.PedidoDetalle;
import modelo.Producto;
import servicio.ClienteServicio;
import servicio.PedidoServicio;
import servicio.ProductoServicio;
import servicio.CanjeServicio; // Importación agregada

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuConsola {

    private static final java.util.Scanner teclado = new java.util.Scanner(System.in);
    private static final ClienteServicio clienteServicio = new ClienteServicio();
    private static final ProductoServicio productoServicio = new ProductoServicio();
    private static final PedidoServicio pedidoServicio = new PedidoServicio();
    private static final MenuDAO menuDAO = new MenuDAO();
    private static final CanjeServicio canjeServicio = new CanjeServicio(); // Instancia agregada

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
            System.out.println("11. Canjear puntos"); // Nueva opción agregada
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
                    case 11:
                        realizarCanjePuntos(); // Nuevo caso agregado
                        break;
                    case 0:
                        System.out.println("Saliendo de Subnotway...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Operación no completada: " + e.getMessage());
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

        Cliente cliente = new Cliente(0, identificador, nombre, telefono, correo, 0, "ACTIVO");

        if (clienteServicio.registrarCliente(cliente)) {
            System.out.println("Cliente registrado correctamente.");
        } else {
            System.out.println("No se pudo registrar el cliente.");
        }
    }

    private static void buscarClientePorId() {
        System.out.print("ID del cliente: ");
        Cliente cliente = clienteServicio.buscarClientePorId(leerEntero());

        if (cliente == null) {
            System.out.println("No se encontró el cliente.");
        } else {
            mostrarCliente(cliente);
        }
    }

    private static void buscarClientePorIdentificador() {
        System.out.print("Identificador del cliente: ");
        Cliente cliente = clienteServicio.buscarClientePorIdentificador(teclado.nextLine().trim());

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
        Cliente cliente = clienteServicio.buscarClientePorId(leerEntero());

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
        Cliente cliente = clienteServicio.buscarClientePorId(leerEntero());

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
        System.out.println("Identificador: " + cliente.getIdentificador());
        System.out.println("Nombre: " + cliente.getNombre());
        System.out.println("Teléfono: " + cliente.getTelefono());
        System.out.println("Correo: " + cliente.getCorreo());
        System.out.println("Puntos: " + cliente.getPuntos());
        System.out.println("Estado: " + cliente.getEstado());
        System.out.println("-------------------------");
    }

    private static void consultarProductos() {
        List<Producto> productos = productoServicio.listarProductosActivos();

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

    private static void registrarVenta() throws SQLException {
        System.out.println("\n--- REGISTRAR VENTA ---");
        System.out.print("ID del cliente: ");
        int idCliente = leerEntero();

        Cliente cliente = clienteServicio.buscarClientePorId(idCliente);

        if (cliente == null || !"ACTIVO".equals(cliente.getEstado())) {
            System.out.println("El cliente no existe o está inactivo.");
            return;
        }

        List<PedidoDetalle> detalles = new ArrayList<>();
        int agregar;

        do {
            System.out.println("\nSeleccione la categoría:");
            System.out.println("1. Sándwiches");
            System.out.println("2. Bebidas");
            System.out.println("3. Acompañamientos");
            System.out.println("4. Combos");
            System.out.print("Selecciona: ");
            int categoria = leerEntero();

            switch (categoria) {
                case 1:
                    System.out.println("Seleccione el Sándwich:");
                    System.out.println("1. Sandwich de ensalada de atun (Q75)");
                    System.out.println("2. Cheese party (Q80)");
                    System.out.println("3. Pork Jhon (Q95)");
                    System.out.println("4. Sandwich de chompipe (Q75)");
                    System.out.println("5. Pollo al granjero (Q55)");
                    System.out.println("6. Ensalada panizada (Q65)");
                    int opcionSandwich = leerEntero();
                    System.out.print("Cantidad: ");
                    int cantSandwich = leerEntero();

                    if (opcionSandwich >= 1 && opcionSandwich <= 6) {
                        detalles.add(new PedidoDetalle(0, 0, opcionSandwich, null, cantSandwich, 0));
                    } else {
                        System.out.println("Sándwich no válido.");
                    }
                    break;

                case 2:
                    System.out.println("Seleccione la Bebida:");
                    System.out.println("1. Monster\n2. Monster blanca\n3. Monster rosa\n4. Monster tropical");
                    System.out.println("5. Monster verde\n6. Tampico\n7. Agua pura\n8. Pepsi\n9. Cocacola\n10. Horchata");
                    int opcionBebida = leerEntero();
                    System.out.print("Cantidad: ");
                    int cantBebida = leerEntero();

                    if (opcionBebida >= 1 && opcionBebida <= 10) {
                        int idProductoBebida = opcionBebida + 6;
                        detalles.add(new PedidoDetalle(0, 0, idProductoBebida, null, cantBebida, 0));
                    } else {
                        System.out.println("Bebida no válida.");
                    }
                    break;

                case 3:
                    System.out.println("Seleccione el Acompañamiento:");
                    System.out.println("1. Doritos\n2. Cheetos\n3. Cheetos flaming hot\n4. Lays originales");
                    System.out.println("5. Lays crema y especias\n6. Lays de pollo campero\n7. Doritos verdes");
                    System.out.println("8. Doritos incognita\n9. Tortrix barbacoa\n10. Tortrix cobanero");
                    System.out.println("11. Tortrix de salsa criolla\n12. Galleta de queso con frambuesa\n13. Coockie chocolate explosion");
                    int opcionAcomp = leerEntero();
                    System.out.print("Cantidad: ");
                    int cantAcomp = leerEntero();

                    if (opcionAcomp >= 1 && opcionAcomp <= 13) {
                        int idProductoAcomp = opcionAcomp + 16;
                        detalles.add(new PedidoDetalle(0, 0, idProductoAcomp, null, cantAcomp, 0));
                    } else {
                        System.out.println("Acompañamiento no válido.");
                    }
                    break;

                case 4:
                    System.out.println("Seleccione el Combo:");
                    System.out.println("1. Combo 1: 3 panes, 3 acompañamientos, 3 bebidas");
                    System.out.println("2. Combo 2: 10 panes, 10 acompañamientos, 10 bebidas");
                    System.out.println("3. Combo 3: 5 panes, 5 acompañamientos, 5 bebidas");
                    System.out.println("4. Combo 4: 2 panes, 2 acompañamientos, 2 bebidas");
                    System.out.println("5. Combo 5: 1 pan, 1 bebida, 1 acompañamiento");
                    int opcionCombo = leerEntero();
                    System.out.print("Cantidad: ");
                    int cantCombo = leerEntero();

                    if (opcionCombo >= 1 && opcionCombo <= 5) {
                        detalles.add(new PedidoDetalle(0, 0, null, opcionCombo, cantCombo, 0));
                    } else {
                        System.out.println("Combo no válido.");
                    }
                    break;

                default:
                    System.out.println("Categoría no válida.");
            }

            System.out.print("¿Agregar otro artículo? (1=Sí, 0=No): ");
            agregar = leerEntero();

        } while (agregar == 1);

        if (detalles.isEmpty()) {
            System.out.println("No se agregaron productos al pedido.");
            return;
        }

        double totalEstimado = calcularTotalEstimado(detalles);

        System.out.printf(java.util.Locale.US, "%nTotal estimado: Q%.2f%n", totalEstimado);

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

        int idPedido = pedidoServicio.registrarVenta(idCliente, detalles, metodoPago, totalEstimado, null, true);

        System.out.println("\n¡Venta registrada correctamente!");
        System.out.println("Número de pedido: " + idPedido);
        System.out.println("Cliente: " + cliente.getNombre());
        System.out.printf(java.util.Locale.US, "Total estimado: Q%.2f%n", totalEstimado);

        Cliente clienteActualizado = clienteServicio.buscarClientePorId(idCliente);

        if (clienteActualizado != null) {
            System.out.println("Puntos actuales: " + clienteActualizado.getPuntos());
        }
    }

    private static double calcularTotalEstimado(List<PedidoDetalle> detalles) {
        double total = 0;

        for (PedidoDetalle detalle : detalles) {
            double precio;

            if (detalle.getIdProducto() != null) {
                Producto producto = productoServicio.buscarProductoActivoPorId(detalle.getIdProducto());
                if (producto == null) {
                    throw new IllegalArgumentException("Producto inexistente o inactivo: " + detalle.getIdProducto());
                }
                precio = producto.getPrecio();
            } else {
                Menu menu = menuDAO.buscarPorId(detalle.getIdMenu());
                if (menu == null || !"ACTIVO".equals(menu.getEstado())) {
                    throw new IllegalArgumentException("Menú inexistente o inactivo: " + detalle.getIdMenu());
                }
                precio = menu.getPrecio();
            }

            detalle.setPrecioUnitario(precio);
            total += precio * detalle.getCantidad();
        }

        if (!Double.isFinite(total) || total <= 0) {
            throw new IllegalArgumentException("El total estimado no es válido.");
        }

        return Math.round(total * 100.0) / 100.0;
    }

    private static void marcarEntregado() throws SQLException {
        System.out.print("ID del pedido pagado: ");
        int idPedido = leerEntero();

        if (pedidoServicio.marcarEntregado(idPedido)) {
            System.out.println("Pedido marcado como ENTREGADO.");
        } else {
            System.out.println("No se encontró un pedido PAGADO con ese ID.");
        }
    }

    private static void anularPedido() throws SQLException {
        System.out.print("ID del pedido pendiente: ");
        int idPedido = leerEntero();

        if (pedidoServicio.anularPedidoPendiente(idPedido)) {
            System.out.println("Pedido anulado.");
        } else {
            System.out.println("No se encontró un pedido PENDIENTE con ese ID.");
        }
    }

    // Nuevo método implementado
    private static void realizarCanjePuntos() {
        System.out.println("\n--- CANJE DE PUNTOS ---");
        System.out.print("ID del cliente: ");
        int idCliente = leerEntero();

        Cliente cliente = clienteServicio.buscarClientePorId(idCliente);

        if (cliente == null || !"ACTIVO".equals(cliente.getEstado())) {
            System.out.println("El cliente no existe o está inactivo.");
            return;
        }

        int puntosActuales = cliente.getPuntos();

        System.out.println("=== Canje de Puntos ===");
        System.out.println("Tiene " + puntosActuales + " puntos. Seleccione una recompensa:");
        System.out.println("1. Combo 1 (4000 puntos)");
        System.out.println("2. Combo 2 (6500 puntos)");
        System.out.println("3. Combo 3 (4500 puntos)");
        System.out.println("4. Combo 4 (3500 puntos)");
        System.out.println("5. Combo 5 (2500 puntos)");
        System.out.println("6. Bebida extra (1500 puntos)");
        System.out.println("7. Acompañamiento extra (1500 puntos)");
        System.out.print("Selecciona una opción: ");

        int opcionCanje = leerEntero();

        try {
            switch (opcionCanje) {
                case 1:
                    canjeServicio.realizarCanje(idCliente, 1, null, 1, 1);
                    break;
                case 2:
                    canjeServicio.realizarCanje(idCliente, 2, null, 2, 1);
                    break;
                case 3:
                    canjeServicio.realizarCanje(idCliente, 3, null, 3, 1);
                    break;
                case 4:
                    canjeServicio.realizarCanje(idCliente, 4, null, 4, 1);
                    break;
                case 5:
                    canjeServicio.realizarCanje(idCliente, 5, null, 5, 1);
                    break;
                case 6:
                    System.out.print("Ingrese ID de la bebida a canjear: ");
                    int idBebida = leerEntero();
                    canjeServicio.realizarCanje(idCliente, 6, idBebida, null, 1);
                    break;
                case 7:
                    System.out.print("Ingrese ID del acompañamiento a canjear: ");
                    int idAcomp = leerEntero();
                    canjeServicio.realizarCanje(idCliente, 7, idAcomp, null, 1);
                    break;
                default:
                    System.out.println("Opción no válida.");
                    return;
            }
            System.out.println("¡Canje realizado con éxito!");
            
            Cliente clienteActualizado = clienteServicio.buscarClientePorId(idCliente);
            System.out.println("Puntos restantes: " + clienteActualizado.getPuntos());
            
        } catch (Exception e) {
            System.out.println("Error al procesar el canje: " + e.getMessage());
        }
    }
}