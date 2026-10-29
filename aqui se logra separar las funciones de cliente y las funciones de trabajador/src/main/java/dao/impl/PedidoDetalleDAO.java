package dao.impl;

import com.subnotway.dao.CrudDAO;
import config.ConexionDB;
import modelo.PedidoDetalle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PedidoDetalleDAO implements CrudDAO<PedidoDetalle> {

    @Override
    public boolean insertar(PedidoDetalle detalle) {
        String sql = "INSERT INTO pedido_detalles "
                + "(id_pedido, id_producto, id_menu, cantidad, "
                + "precio_unitario) VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, detalle.getIdPedido());
            establecerIdNullable(ps, 2, detalle.getIdProducto());
            establecerIdNullable(ps, 3, detalle.getIdMenu());
            ps.setInt(4, detalle.getCantidad());
            ps.setDouble(5, detalle.getPrecioUnitario());

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return false;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    detalle.setIdDetalle(rs.getInt(1));
                }
            }

            return detalle.getIdDetalle() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Error al insertar detalle del pedido: "
                    + e.getMessage());
            return false;
        }
    }

    public void insertar(
            Connection conexion,
            PedidoDetalle detalle) throws SQLException {

        String sql = "INSERT INTO pedido_detalles "
                + "(id_pedido, id_producto, id_menu, cantidad, "
                + "precio_unitario) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, detalle.getIdPedido());
            establecerIdNullable(ps, 2, detalle.getIdProducto());
            establecerIdNullable(ps, 3, detalle.getIdMenu());
            ps.setInt(4, detalle.getCantidad());
            ps.setDouble(5, detalle.getPrecioUnitario());

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo registrar el detalle del pedido.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    detalle.setIdDetalle(rs.getInt(1));
                } else {
                    throw new SQLException(
                            "No se pudo obtener el ID del detalle.");
                }
            }
        }
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
            establecerIdNullable(ps, 2, detalle.getIdProducto());
            establecerIdNullable(ps, 3, detalle.getIdMenu());
            ps.setInt(4, detalle.getCantidad());
            ps.setDouble(5, detalle.getPrecioUnitario());
            ps.setInt(6, detalle.getIdDetalle());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Error al actualizar detalle del pedido: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(PedidoDetalle detalle) {
        String sql = "DELETE FROM pedido_detalles WHERE id_detalle = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, detalle.getIdDetalle());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Error al eliminar detalle del pedido: "
                    + e.getMessage());
            return false;
        }
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
            System.out.println(
                    "Error al buscar detalle: " + e.getMessage());
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
            System.out.println(
                    "Error al listar detalles: " + e.getMessage());
        }

        return lista;
    }

    public List<PedidoDetalle> listarPorPedido(int idPedido) {
        List<PedidoDetalle> lista = new ArrayList<>();

        String sql = "SELECT * FROM pedido_detalles "
                + "WHERE id_pedido = ? ORDER BY id_detalle";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearDetalle(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al listar detalles del pedido: "
                    + e.getMessage());
        }

        return lista;
    }

    private void establecerIdNullable(
            PreparedStatement ps,
            int posicion,
            Integer id) throws SQLException {

        if (id == null) {
            ps.setNull(posicion, Types.INTEGER);
        } else {
            ps.setInt(posicion, id);
        }
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
                rs.getDouble("precio_unitario"));
    }
}