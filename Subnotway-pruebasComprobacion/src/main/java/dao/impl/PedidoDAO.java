package dao.impl;

import com.subnotway.dao.CrudDAO;
import config.ConexionDB;
import modelo.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO implements CrudDAO<Pedido> {
    
    
public boolean insertar(Connection conexion, Pedido pedido)
        throws SQLException {

    String sql = "INSERT INTO pedidos (id_cliente, estado, total) "
               + "VALUES (?, ?, ?)";

    try (PreparedStatement ps = conexion.prepareStatement(
            sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, pedido.getIdCliente());
        ps.setString(2, pedido.getEstado());
        ps.setDouble(3, pedido.getTotal());

        if (ps.executeUpdate() == 0) {
            return false;
        }

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                pedido.setIdPedido(rs.getInt(1));
                return true;
            }
        }
    }

    return false;
}


public boolean actualizarEstado(
        Connection conexion, int idPedido, String estado)
        throws SQLException {

    String sql = "UPDATE pedidos SET estado = ? "
               + "WHERE id_pedido = ?";

    try (PreparedStatement ps = conexion.prepareStatement(sql)) {
        ps.setString(1, estado);
        ps.setInt(2, idPedido);

        return ps.executeUpdate() > 0;
    }
}


    @Override
    public boolean insertar(Pedido pedido) {

        String sql = "INSERT INTO pedidos "
                + "(id_cliente, estado, total) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pedido.getIdCliente());
            ps.setString(2, pedido.getEstado());
            ps.setDouble(3, pedido.getTotal());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        pedido.setIdPedido(rs.getInt(1));
                    }
                }

                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar pedido: "
                    + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean actualizar(Pedido pedido) {

        String sql = "UPDATE pedidos SET "
                + "id_cliente = ?, estado = ?, total = ? "
                + "WHERE id_pedido = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, pedido.getIdCliente());
            ps.setString(2, pedido.getEstado());
            ps.setDouble(3, pedido.getTotal());
            ps.setInt(4, pedido.getIdPedido());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar pedido: "
                    + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(Pedido pedido) {

        String sql = "UPDATE pedidos SET estado = 'ANULADO' "
                + "WHERE id_pedido = ? AND estado <> 'ANULADO'";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, pedido.getIdPedido());

            boolean actualizado = ps.executeUpdate() > 0;

            if (actualizado) {
                pedido.setEstado("ANULADO");
            }

            return actualizado;

        } catch (SQLException e) {
            System.out.println("Error al anular pedido: "
                    + e.getMessage());
        }

        return false;
    }

    @Override
    public Pedido buscarPorId(int id) {

        String sql = "SELECT * FROM pedidos WHERE id_pedido = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPedido(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar pedido: "
                    + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Pedido> listarTodos() {

        List<Pedido> lista = new ArrayList<>();

        String sql = "SELECT * FROM pedidos ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPedido(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pedidos: "
                    + e.getMessage());
        }

        return lista;
    }

    public List<Pedido> listarPorCliente(int idCliente) {

        List<Pedido> lista = new ArrayList<>();

        String sql = "SELECT * FROM pedidos "
                + "WHERE id_cliente = ? ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPedido(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pedidos del cliente: "
                    + e.getMessage());
        }

        return lista;
    }

    private Pedido mapearPedido(ResultSet rs) throws SQLException {

        Timestamp fechaSQL = rs.getTimestamp("fecha");

        return new Pedido(
                rs.getInt("id_pedido"),
                rs.getInt("id_cliente"),
                fechaSQL != null
                        ? fechaSQL.toLocalDateTime() : null,
                rs.getString("estado"),
                rs.getDouble("total")
        );
    }
}

