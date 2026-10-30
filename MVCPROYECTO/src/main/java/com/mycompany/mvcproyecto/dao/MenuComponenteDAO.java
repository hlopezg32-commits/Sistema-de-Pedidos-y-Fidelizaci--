package com.mycompany.mvcproyecto.dao;

import com.mycompany.mvcproyecto.config.ConexionDB;
import com.mycompany.mvcproyecto.modelo.MenuComponente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuComponenteDAO {

    public boolean insertar(MenuComponente componente) {

        String sql = "INSERT INTO menu_componentes "
                + "(id_menu, id_producto, cantidad) "
                + "VALUES (?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, componente.getIdMenu());
            ps.setInt(2, componente.getIdProducto());
            ps.setInt(3, componente.getCantidad());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al insertar componente: "
                    + e.getMessage());
        }

        return false;
    }

    public boolean actualizarCantidad(int idMenu, int idProducto,
            int cantidad) {

        String sql = "UPDATE menu_componentes SET cantidad = ? "
                + "WHERE id_menu = ? AND id_producto = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, cantidad);
            ps.setInt(2, idMenu);
            ps.setInt(3, idProducto);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar componente: "
                    + e.getMessage());
        }

        return false;
    }

    public boolean eliminar(int idMenu, int idProducto) {

        String sql = "DELETE FROM menu_componentes "
                + "WHERE id_menu = ? AND id_producto = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMenu);
            ps.setInt(2, idProducto);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar componente: "
                    + e.getMessage());
        }

        return false;
    }

    public List<MenuComponente> listarPorMenu(int idMenu) {

        List<MenuComponente> componentes = new ArrayList<>();

        String sql = "SELECT id_menu, id_producto, cantidad "
                + "FROM menu_componentes WHERE id_menu = ? "
                + "ORDER BY id_producto";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMenu);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MenuComponente componente = new MenuComponente(
                            rs.getInt("id_menu"),
                            rs.getInt("id_producto"),
                            rs.getInt("cantidad")
                    );

                    componentes.add(componente);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar componentes: "
                    + e.getMessage());
        }

        return componentes;
    }

    public MenuComponente buscar(int idMenu, int idProducto) {

        String sql = "SELECT id_menu, id_producto, cantidad "
                + "FROM menu_componentes "
                + "WHERE id_menu = ? AND id_producto = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMenu);
            ps.setInt(2, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MenuComponente(
                            rs.getInt("id_menu"),
                            rs.getInt("id_producto"),
                            rs.getInt("cantidad")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar componente: "
                    + e.getMessage());
        }

        return null;
    }
}
