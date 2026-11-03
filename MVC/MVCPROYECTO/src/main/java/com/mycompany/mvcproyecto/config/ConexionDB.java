package com.mycompany.mvcproyecto.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    public static Connection conectar() throws SQLException {

        String url = "jdbc:mysql://localhost:3306/pan_puntos_premios";
        String usuario = "root";
        String contrasena = "jimena100";

        return DriverManager.getConnection(url, usuario, contrasena);
    }
}

