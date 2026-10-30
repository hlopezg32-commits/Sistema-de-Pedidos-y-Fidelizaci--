
package com.mycompany.mvcproyecto.dao;

import com.mycompany.mvcproyecto.config.ConexionDB;
import com.mycompany.mvcproyecto.modelo.Pago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO implements CrudDAO<Pago> {

    @Override
    public boolean insertar(Pago pago) {
        String sql = "INSERT INTO pagos "
                   + "(id_pedido, metodo, monto, cambio, estado, referencia) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pago.getIdPedido());
            ps.setString(2, pago.getMetodo());
            ps.setDouble(3, pago.getMonto());
            ps.setDouble(4, pago.getCambio());
            ps.setString(5, pago.getEstado());
            ps.setString(6, pago.getReferencia());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        pago.setIdPago(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar pago: " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean actualizar(Pago pago) {
        String sql = "UPDATE pagos SET id_pedido = ?, metodo = ?, "
                   + "monto = ?, cambio = ?, estado = ?, referencia = ? "
                   + "WHERE id_pago = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, pago.getIdPedido());
            ps.setString(2, pago.getMetodo());
            ps.setDouble(3, pago.getMonto());
            ps.setDouble(4, pago.getCambio());
            ps.setString(5, pago.getEstado());
            ps.setString(6, pago.getReferencia());
            ps.setInt(7, pago.getIdPago());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar pago: " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(Pago pago) {
        String sql = "DELETE FROM pagos WHERE id_pago = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, pago.getIdPago());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar pago: " + e.getMessage());
        }

        return false;
    }

    @Override
    public Pago buscarPorId(int id) {
        String sql = "SELECT * FROM pagos WHERE id_pago = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPago(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar pago: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Pago> listarTodos() {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPago(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pagos: " + e.getMessage());
        }

        return lista;
    }

    public List<Pago> listarPorPedido(int idPedido) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE id_pedido = ? "
                   + "ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPago(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pagos del pedido: "
                             + e.getMessage());
        }

        return lista;
    }

    private Pago mapearPago(ResultSet rs) throws SQLException {
        Timestamp fechaSQL = rs.getTimestamp("fecha");

        return new Pago(
                rs.getInt("id_pago"),
                rs.getInt("id_pedido"),
                rs.getString("metodo"),
                rs.getDouble("monto"),
                rs.getDouble("cambio"),
                rs.getString("estado"),
                rs.getString("referencia"),
                fechaSQL != null ? fechaSQL.toLocalDateTime() : null
        );
    }
}
