
package principal;

import java.util.List;
import java.util.Scanner;
import modelo.Cliente;
import servicio.ClienteServicio;

public class MenuConsola {

    private static final Scanner teclado = new Scanner(System.in);
    private static final ClienteServicio clienteServicio =
            new ClienteServicio();

    public static void main(String[] args) {

        int opcion;

        do {
            System.out.println("\n===== SUBNOTWAY =====");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Buscar cliente por ID");
            System.out.println("3. Buscar cliente por identificador");
            System.out.println("4. Listar clientes");
            System.out.println("5. Modificar cliente");
            System.out.println("6. Desactivar cliente");
            System.out.println("0. Salir");
            System.out.print("Selecciona una opción: ");

            opcion = leerEntero();

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
                case 0:
                    System.out.println("Saliendo de Subnotway...");
                    break;
                default:
                    System.out.println("Opción no válida.");
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

        Cliente cliente = new Cliente(
                0,
                identificador,
                nombre,
                telefono,
                correo,
                0,
                "ACTIVO"
        );

        if (clienteServicio.registrarCliente(cliente)) {
            System.out.println("Cliente registrado correctamente.");
        } else {
            System.out.println(
                    "No se pudo registrar el cliente. "
                    + "Verifica los datos y la consola."
            );
        }
    }

    private static void buscarClientePorId() {
        System.out.print("ID del cliente: ");
        int id = leerEntero();

        Cliente cliente = clienteServicio.buscarClientePorId(id);

        if (cliente != null) {
            mostrarCliente(cliente);
        } else {
            System.out.println("No se encontró el cliente.");
        }
    }

    private static void buscarClientePorIdentificador() {
        System.out.print("Identificador del cliente: ");
        String identificador = teclado.nextLine().trim();

        Cliente cliente = clienteServicio
                .buscarClientePorIdentificador(identificador);

        if (cliente != null) {
            mostrarCliente(cliente);
        } else {
            System.out.println("No se encontró el cliente.");
        }
    }

    private static void listarClientes() {
        List<Cliente> clientes = clienteServicio.listarClientes();

        if (clientes == null || clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }

        System.out.println("\n--- CLIENTES REGISTRADOS ---");

        for (Cliente cliente : clientes) {
            mostrarCliente(cliente);
        }
    }

    private static void modificarCliente() {
        System.out.print("ID del cliente que deseas modificar: ");
        int id = leerEntero();

        Cliente cliente = clienteServicio.buscarClientePorId(id);

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
            System.out.println("Cliente actualizado correctamente.");
        } else {
            System.out.println("No se pudo actualizar el cliente.");
        }
    }

    private static void desactivarCliente() {
        System.out.print("ID del cliente que deseas desactivar: ");
        int id = leerEntero();

        Cliente cliente = clienteServicio.buscarClientePorId(id);

        if (cliente == null) {
            System.out.println("No se encontró el cliente.");
            return;
        }

        System.out.print("¿Confirmas la desactivación? (S/N): ");
        String respuesta = teclado.nextLine().trim();

        if (respuesta.equalsIgnoreCase("S")) {
            if (clienteServicio.desactivarCliente(cliente)) {
                System.out.println("Cliente desactivado.");
            } else {
                System.out.println("No se pudo desactivar el cliente.");
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
}
