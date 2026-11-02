
package principal;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultarProductos {

    public static void main(String[] args) {

        String sql = """
            SELECT id_producto, codigo, nombre, categoria,
                   existencia, estado
            FROM productos
            ORDER BY id_producto
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("=== PRODUCTOS REGISTRADOS ===");

            boolean hayProductos = false;

            while (rs.next()) {
                hayProductos = true;

                System.out.println(
                        "ID: " + rs.getInt("id_producto")
                        + " | Código: " + rs.getString("codigo")
                        + " | Nombre: " + rs.getString("nombre")
                        + " | Categoría: " + rs.getString("categoria")
                        + " | Existencia: " + rs.getInt("existencia")
                        + " | Estado: " + rs.getString("estado")
                );
            }

            if (!hayProductos) {
                System.out.println("No hay productos registrados.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al consultar productos: " + e.getMessage()
            );
        }
    }
}

