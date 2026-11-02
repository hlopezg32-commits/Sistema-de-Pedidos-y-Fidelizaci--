
package servicio;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventarioRecetas {

    /**
     * Comprueba si hay suficientes ingredientes para preparar
     * una o más unidades de un sándwich.
     *
     * Los códigos de los ingredientes deben coincidir con
     * productos.codigo en MySQL.
     */
    public boolean hayIngredientesSuficientes(
            String codigoSandwich, int cantidadSandwiches)
            throws SQLException {

        if (cantidadSandwiches <= 0) {
            return false;
        }

        String[][] receta =
                CatalogoRecetas.obtenerReceta(codigoSandwich);

        if (receta == null) {
            return false;
        }

        String sql = """
            SELECT existencia, estado
            FROM productos
            WHERE codigo = ?
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            for (String[] ingrediente : receta) {
                String codigoIngrediente = ingrediente[0];
                int cantidadPorSandwich =
                        Integer.parseInt(ingrediente[1]);

                int cantidadNecesaria =
                        cantidadPorSandwich * cantidadSandwiches;

                ps.setString(1, codigoIngrediente);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println(
                                "Ingrediente no encontrado: "
                                + codigoIngrediente);
                        return false;
                    }

                    int existencia = rs.getInt("existencia");
                    String estado = rs.getString("estado");

                    if (!"ACTIVO".equals(estado)
                            || existencia < cantidadNecesaria) {

                        System.out.println(
                                "Stock insuficiente para: "
                                + codigoIngrediente
                                + ". Necesitas: "
                                + cantidadNecesaria
                                + ", disponible: "
                                + existencia);

                        return false;
                    }
                }
            }
        }

        return true;
    }
}

