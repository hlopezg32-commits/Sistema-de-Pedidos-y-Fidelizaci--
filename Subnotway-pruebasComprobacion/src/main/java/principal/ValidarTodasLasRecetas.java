
package principal;

import config.ConexionDB;
import servicio.CatalogoRecetas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ValidarTodasLasRecetas {

    public static void main(String[] args) {

        String sql = """
            SELECT nombre, existencia, estado
            FROM productos
            WHERE codigo = ?
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            Map<String, String[][]> recetas =
                    CatalogoRecetas.obtenerTodasLasRecetas();

            System.out.println("========================================");
            System.out.println(" VALIDACION DE RECETAS - SUBNOTWAY");
            System.out.println("========================================");
            System.out.println("Prueba de una unidad por receta.");
            System.out.println("No se modifica el inventario.");

            for (Map.Entry<String, String[][]> entrada
                    : recetas.entrySet()) {

                String codigoSandwich = entrada.getKey();
                String[][] ingredientes = entrada.getValue();

                boolean recetaValida = true;

                System.out.println("\n----------------------------------------");
                System.out.println("RECETA: " + codigoSandwich);

                for (String[] ingrediente : ingredientes) {

                    String codigo = ingrediente[0];

                    int cantidadNecesaria;

                    try {
                        cantidadNecesaria =
                                Integer.parseInt(ingrediente[1]);
                    } catch (NumberFormatException e) {
                        System.out.println(
                                "Cantidad invalida en receta: " + codigo);
                        recetaValida = false;
                        continue;
                    }

                    ps.setString(1, codigo);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            System.out.println(
                                    "[FALTA REGISTRAR] " + codigo);
                            recetaValida = false;
                            continue;
                        }

                        String nombre = rs.getString("nombre");
                        int existencia = rs.getInt("existencia");
                        String estado = rs.getString("estado");

                        if (!"ACTIVO".equals(estado)) {
                            System.out.println(
                                    "[INACTIVO] " + nombre
                                    + " (" + codigo + ")");
                            recetaValida = false;

                        } else if (existencia < cantidadNecesaria) {
                            System.out.println(
                                    "[STOCK INSUFICIENTE] " + nombre
                                    + " | Necesario: " + cantidadNecesaria
                                    + " | Disponible: " + existencia);
                            recetaValida = false;

                        } else {
                            System.out.println(
                                    "[OK] " + nombre
                                    + " | Disponible: " + existencia
                                    + " | Necesario: " + cantidadNecesaria);
                        }
                    }
                }

                if (recetaValida) {
                    System.out.println(
                            "RESULTADO: RECETA DISPONIBLE PARA PRUEBA");
                } else {
                    System.out.println(
                            "RESULTADO: RECETA PENDIENTE");
                }
            }

            System.out.println("\n========================================");
            System.out.println("VALIDACION TERMINADA");
            System.out.println("========================================");

        } catch (SQLException e) {
            System.out.println(
                    "Error al consultar MySQL: " + e.getMessage());
        }
    }
}


