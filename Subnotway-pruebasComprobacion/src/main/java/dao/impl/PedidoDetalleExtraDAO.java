package dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.PedidoDetalleExtra;

public class PedidoDetalleExtraDAO {

    public boolean insertar(Connection conexion, PedidoDetalleExtra extra)
            throws SQLException {

        String sql = "INSERT INTO pedido_detalle_extras "
                + "(id_detalle, id_producto, cantidad, precio_unitario) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, extra.getIdDetalle());
            ps.setInt(2, extra.getIdProducto());
            ps.setInt(3, extra.getCantidad());
            ps.setDouble(4, extra.getPrecioUnitario());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        extra.setIdDetalleExtra(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public List<PedidoDetalleExtra> listarPorDetalle(Connection conexion,
            int idDetalle) throws SQLException {

        List<PedidoDetalleExtra> extras = new ArrayList<>();

        String sql = "SELECT id_detalle_extra, id_detalle, id_producto, "
                + "cantidad, precio_unitario "
                + "FROM pedido_detalle_extras "
                + "WHERE id_detalle = ? "
                + "ORDER BY id_detalle_extra";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PedidoDetalleExtra extra = new PedidoDetalleExtra(
                            rs.getInt("id_detalle_extra"),
                            rs.getInt("id_detalle"),
                            rs.getInt("id_producto"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario")
                    );

                    extras.add(extra);
                }
            }
        }

        return extras;
    }

    public boolean eliminarPorDetalle(Connection conexion, int idDetalle)
            throws SQLException {

        String sql = "DELETE FROM pedido_detalle_extras "
                + "WHERE id_detalle = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            return ps.executeUpdate() > 0;
        }
    }
}
