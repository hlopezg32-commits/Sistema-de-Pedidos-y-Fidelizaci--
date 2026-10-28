
package principal;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {

        try (Connection conexion = ConexionDB.conectar()) {

            if (conexion.isValid(5)) {
                System.out.println(
                    "Conexión exitosa con pan_puntos_premios."
                );
            }

        } catch (SQLException e) {
            System.err.println(
                "No fue posible conectar con MySQL: "
                + e.getMessage()
            );
        }
    }
}
