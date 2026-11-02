
package principal;

import servicio.InventarioRecetas;
import java.sql.SQLException;
import java.util.Scanner;

public class PruebaInventarioRecetas {

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);
        InventarioRecetas inventario = new InventarioRecetas();

        System.out.println("=== PRUEBA DE INVENTARIO POR RECETA ===");
        System.out.println("Sándwiches disponibles:");

        System.out.println("1. SANDWICH_ATUN");
        System.out.println("2. CHEESE_PARTY");
        System.out.println("3. EL_COCHE");
        System.out.println("4. SANDWICH_CHOMPIPE");
        System.out.println("5. POLLO_GRANJERO");
        System.out.println("6. ENSALADA_PANIZADA");
        System.out.println("7. SANDWICH_CLASICO");

        System.out.print("\nSelecciona un sándwich (1-7): ");
String seleccion = teclado.nextLine().trim();

String[] codigos = {
    "SANDWICH_ATUN",
    "CHEESE_PARTY",
    "EL_COCHE",
    "SANDWICH_CHOMPIPE",
    "POLLO_GRANJERO",
    "ENSALADA_PANIZADA",
    "SANDWICH_CLASICO"
};

String codigo;

try {
    int opcion = Integer.parseInt(seleccion);

    if (opcion < 1 || opcion > codigos.length) {
        System.out.println("Opción inválida.");
        return;
    }

    codigo = codigos[opcion - 1];

} catch (NumberFormatException e) {
    codigo = seleccion.toUpperCase();
}

        System.out.print("¿Cuántas unidades deseas comprobar?: ");
        int cantidad;

        try {
            cantidad = Integer.parseInt(teclado.nextLine().trim());

            if (cantidad <= 0) {
                System.out.println("La cantidad debe ser mayor que cero.");
                return;
            }

            boolean disponible =
                    inventario.hayIngredientesSuficientes(
                            codigo, cantidad);

            if (disponible) {
                System.out.println(
                        "RESULTADO: Hay ingredientes suficientes.");
            } else {
                System.out.println(
                        "RESULTADO: No se puede preparar esa cantidad.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Debes ingresar una cantidad válida.");
        } catch (SQLException e) {
            System.out.println("Error al consultar MySQL: "
                    + e.getMessage());
        }
    }
}
