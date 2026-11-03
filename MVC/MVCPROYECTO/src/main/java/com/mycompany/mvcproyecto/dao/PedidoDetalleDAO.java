
package com.mycompany.mvcproyecto.dao;

import com.mycompany.mvcproyecto.config.ConexionDB;
import com.mycompany.mvcproyecto.modelo.PedidoDetalle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PedidoDetalleDAO implements CrudDAO<PedidoDetalle> {

    @Override
    public boolean insertar(PedidoDetalle detalle) {
        String sql = "INSERT INTO pedido_detalles "
                   + "(id_pedido, id_producto, id_menu, cantidad, precio_unitario) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, detalle.getIdPedido());

            if (detalle.getIdProducto() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, detalle.getIdProducto());
            }

            if (detalle.getIdMenu() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, detalle.getIdMenu());
            }

            ps.setInt(4, detalle.getCantidad());
            ps.setDouble(5, detalle.getPrecioUnitario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al insertar detalle del pedido: "
                             + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean actualizar(PedidoDetalle detalle) {
        String sql = "UPDATE pedido_detalles SET "
                   + "id_pedido = ?, id_producto = ?, id_menu = ?, "
                   + "cantidad = ?, precio_unitario = ? "
                   + "WHERE id_detalle = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, detalle.getIdPedido());

            if (detalle.getIdProducto() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, detalle.getIdProducto());
            }

            if (detalle.getIdMenu() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, detalle.getIdMenu());
            }

            ps.setInt(4, detalle.getCantidad());
            ps.setDouble(5, detalle.getPrecioUnitario());
            ps.setInt(6, detalle.getIdDetalle());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar detalle del pedido: "
                             + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(PedidoDetalle detalle) {
        String sql = "DELETE FROM pedido_detalles WHERE id_detalle = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, detalle.getIdDetalle());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar detalle del pedido: "
                             + e.getMessage());
        }

        return false;
    }

    @Override
    public PedidoDetalle buscarPorId(int id) {
        String sql = "SELECT * FROM pedido_detalles WHERE id_detalle = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearDetalle(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar detalle: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<PedidoDetalle> listarTodos() {
        List<PedidoDetalle> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido_detalles ORDER BY id_detalle";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearDetalle(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar detalles: " + e.getMessage());
        }

        return lista;
    }

    public List<PedidoDetalle> listarPorPedido(int idPedido) {
        List<PedidoDetalle> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido_detalles WHERE id_pedido = ? "
                   + "ORDER BY id_detalle";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearDetalle(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar detalles del pedido: "
                             + e.getMessage());
        }

        return lista;
    }

    private PedidoDetalle mapearDetalle(ResultSet rs) throws SQLException {
        int idProductoSQL = rs.getInt("id_producto");
        Integer idProducto = rs.wasNull() ? null : idProductoSQL;

        int idMenuSQL = rs.getInt("id_menu");
        Integer idMenu = rs.wasNull() ? null : idMenuSQL;

        return new PedidoDetalle(
                rs.getInt("id_detalle"),
                rs.getInt("id_pedido"),
                idProducto,
                idMenu,
                rs.getInt("cantidad"),
                rs.getDouble("precio_unitario")
        );
    }
}
