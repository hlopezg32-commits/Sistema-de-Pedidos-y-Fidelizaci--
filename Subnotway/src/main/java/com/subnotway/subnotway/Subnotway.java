package com.subnotway.subnotway;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.SQLException;

public class Subnotway {

    public static void main(String[] args) {
        try {
            Connection conexion = ConexionDB.conectar();

            System.out.println("CONEXION EXITOSA A MYSQL");

            conexion.close();

        } catch (SQLException e) {
            System.out.println("ERROR AL CONECTAR A MYSQL");
            System.out.println(e.getMessage());
        }
    }
}