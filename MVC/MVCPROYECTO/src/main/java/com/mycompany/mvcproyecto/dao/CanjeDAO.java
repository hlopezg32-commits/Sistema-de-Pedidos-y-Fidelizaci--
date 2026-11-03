
package com.mycompany.mvcproyecto.dao;

import com.mycompany.mvcproyecto.config.ConexionDB;
import com.mycompany.mvcproyecto.modelo.Canje;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CanjeDAO implements CrudDAO<Canje> {

    @Override
    public boolean insertar(Canje canje) {
        String sql = "INSERT INTO canjes "
                   + "(id_cliente, id_recompensa, puntos_utilizados) "
                   + "VALUES (?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, canje.getIdCliente());
            ps.setInt(2, canje.getIdRecompensa());
            ps.setInt(3, canje.getPuntosUtilizados());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        canje.setIdCanje(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar canje: " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean actualizar(Canje canje) {
        String sql = "UPDATE canjes SET id_cliente = ?, "
                   + "id_recompensa = ?, puntos_utilizados = ? "
                   + "WHERE id_canje = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, canje.getIdCliente());
            ps.setInt(2, canje.getIdRecompensa());
            ps.setInt(3, canje.getPuntosUtilizados());
            ps.setInt(4, canje.getIdCanje());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar canje: " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(Canje canje) {
        String sql = "DELETE FROM canjes WHERE id_canje = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, canje.getIdCanje());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar canje: " + e.getMessage());
        }

        return false;
    }

    @Override
    public Canje buscarPorId(int id) {
        String sql = "SELECT * FROM canjes WHERE id_canje = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCanje(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar canje: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Canje> listarTodos() {
        List<Canje> lista = new ArrayList<>();
        String sql = "SELECT * FROM canjes ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCanje(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar canjes: " + e.getMessage());
        }

        return lista;
    }

    public List<Canje> listarPorCliente(int idCliente) {
        List<Canje> lista = new ArrayList<>();
        String sql = "SELECT * FROM canjes "
                   + "WHERE id_cliente = ? ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCanje(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar canjes del cliente: "
                             + e.getMessage());
        }

        return lista;
    }

    private Canje mapearCanje(ResultSet rs) throws SQLException {
        Timestamp fechaSQL = rs.getTimestamp("fecha");

        return new Canje(
                rs.getInt("id_canje"),
                rs.getInt("id_cliente"),
                rs.getInt("id_recompensa"),
                rs.getInt("puntos_utilizados"),
                fechaSQL != null ? fechaSQL.toLocalDateTime() : null
        );
    }
}
