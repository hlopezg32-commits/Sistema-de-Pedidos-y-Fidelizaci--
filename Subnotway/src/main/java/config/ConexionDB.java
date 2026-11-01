package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    public static Connection conectar() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/pan_puntos_premios?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String usuario = "root";
        String contrasena = "";

        return DriverManager.getConnection(url, usuario, contrasena);
    }
}
