package dao.impl;

import com.subnotway.dao.CrudDAO;
import config.ConexionDB;
import modelo.MovimientoPuntos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MovimientoPuntosDAO
        implements CrudDAO<MovimientoPuntos> {

    @Override
    public boolean insertar(MovimientoPuntos movimiento) {
        String sql = "INSERT INTO movimientos_puntos "
                   + "(id_cliente, id_pedido, tipo, puntos) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, movimiento.getIdCliente());

            if (movimiento.getIdPedido() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, movimiento.getIdPedido());
            }

            ps.setString(3, movimiento.getTipo());
            ps.setInt(4, movimiento.getPuntos());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        movimiento.setIdMovimiento(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar movimiento de puntos: "
                             + e.getMessage());
        }

        return false;
    }
    
public boolean insertar(
        Connection conexion, MovimientoPuntos movimiento)
        throws SQLException {

    String sql = "INSERT INTO movimientos_puntos "
            + "(id_cliente, id_pedido, tipo, puntos) "
            + "VALUES (?, ?, ?, ?)";

    try (PreparedStatement ps = conexion.prepareStatement(
            sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, movimiento.getIdCliente());

        if (movimiento.getIdPedido() == null) {
            ps.setNull(2, java.sql.Types.INTEGER);
        } else {
            ps.setInt(2, movimiento.getIdPedido());
        }

        ps.setString(3, movimiento.getTipo());
        ps.setInt(4, movimiento.getPuntos());

        if (ps.executeUpdate() == 0) {
            return false;
        }

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                movimiento.setIdMovimiento(rs.getInt(1));
                return true;
            }
        }
    }

    return false;
}


    @Override
    public boolean actualizar(MovimientoPuntos movimiento) {
        String sql = "UPDATE movimientos_puntos "
                   + "SET id_cliente = ?, id_pedido = ?, tipo = ?, "
                   + "puntos = ? WHERE id_movimiento = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, movimiento.getIdCliente());

            if (movimiento.getIdPedido() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, movimiento.getIdPedido());
            }

            ps.setString(3, movimiento.getTipo());
            ps.setInt(4, movimiento.getPuntos());
            ps.setInt(5, movimiento.getIdMovimiento());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar movimiento de puntos: "
                             + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(MovimientoPuntos movimiento) {
        String sql = "DELETE FROM movimientos_puntos "
                   + "WHERE id_movimiento = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, movimiento.getIdMovimiento());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar movimiento de puntos: "
                             + e.getMessage());
        }

        return false;
    }

    @Override
    public MovimientoPuntos buscarPorId(int id) {
        String sql = "SELECT * FROM movimientos_puntos "
                   + "WHERE id_movimiento = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMovimiento(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar movimiento de puntos: "
                             + e.getMessage());
        }

        return null;
    }

    @Override
    public List<MovimientoPuntos> listarTodos() {
        List<MovimientoPuntos> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_puntos "
                   + "ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMovimiento(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar movimientos de puntos: "
                             + e.getMessage());
        }

        return lista;
    }

    public List<MovimientoPuntos> listarPorCliente(int idCliente) {
        List<MovimientoPuntos> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_puntos "
                   + "WHERE id_cliente = ? ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMovimiento(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar puntos del cliente: "
                             + e.getMessage());
        }

        return lista;
    }

    private MovimientoPuntos mapearMovimiento(ResultSet rs)
            throws SQLException {

        Timestamp fechaSQL = rs.getTimestamp("fecha");

        int idPedidoSQL = rs.getInt("id_pedido");
        Integer idPedido = rs.wasNull() ? null : idPedidoSQL;

        return new MovimientoPuntos(
                rs.getInt("id_movimiento"),
                rs.getInt("id_cliente"),
                idPedido,
                rs.getString("tipo"),
                rs.getInt("puntos"),
                fechaSQL != null ? fechaSQL.toLocalDateTime() : null
        );
    }
}
