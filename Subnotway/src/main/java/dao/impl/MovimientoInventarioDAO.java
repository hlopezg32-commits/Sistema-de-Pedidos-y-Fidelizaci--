
package dao.impl;

import com.subnotway.dao.CrudDAO;
import config.ConexionDB;
import modelo.MovimientoInventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioDAO
        implements CrudDAO<MovimientoInventario> {

    @Override
    public boolean insertar(MovimientoInventario movimiento) {
        String sql = "INSERT INTO movimientos_inventario "
                   + "(id_producto, tipo, cantidad, observacion) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, movimiento.getIdProducto());
            ps.setString(2, movimiento.getTipo());
            ps.setInt(3, movimiento.getCantidad());
            ps.setString(4, movimiento.getObservacion());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        movimiento.setIdMovimiento(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar movimiento de inventario: "
                             + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean actualizar(MovimientoInventario movimiento) {
        String sql = "UPDATE movimientos_inventario "
                   + "SET id_producto = ?, tipo = ?, cantidad = ?, "
                   + "observacion = ? WHERE id_movimiento = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, movimiento.getIdProducto());
            ps.setString(2, movimiento.getTipo());
            ps.setInt(3, movimiento.getCantidad());
            ps.setString(4, movimiento.getObservacion());
            ps.setInt(5, movimiento.getIdMovimiento());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar movimiento: "
                             + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(MovimientoInventario movimiento) {
        String sql = "DELETE FROM movimientos_inventario "
                   + "WHERE id_movimiento = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, movimiento.getIdMovimiento());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar movimiento: "
                             + e.getMessage());
        }

        return false;
    }

    @Override
    public MovimientoInventario buscarPorId(int id) {
        String sql = "SELECT * FROM movimientos_inventario "
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
            System.out.println("Error al buscar movimiento: "
                             + e.getMessage());
        }

        return null;
    }

    @Override
    public List<MovimientoInventario> listarTodos() {
        List<MovimientoInventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_inventario "
                   + "ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMovimiento(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar movimientos: "
                             + e.getMessage());
        }

        return lista;
    }

    public List<MovimientoInventario> listarPorProducto(int idProducto) {
        List<MovimientoInventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_inventario "
                   + "WHERE id_producto = ? ORDER BY fecha DESC";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMovimiento(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar movimientos del producto: "
                             + e.getMessage());
        }

        return lista;
    }

    private MovimientoInventario mapearMovimiento(ResultSet rs)
            throws SQLException {

        Timestamp fechaSQL = rs.getTimestamp("fecha");

        return new MovimientoInventario(
                rs.getInt("id_movimiento"),
                rs.getInt("id_producto"),
                rs.getString("tipo"),
                rs.getInt("cantidad"),
                fechaSQL != null ? fechaSQL.toLocalDateTime() : null,
                rs.getString("observacion")
        );
    }
}
