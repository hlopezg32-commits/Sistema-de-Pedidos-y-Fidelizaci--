package dao.impl;

import com.subnotway.dao.CrudDAO;
import config.ConexionDB;
import modelo.Recompensa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecompensaDAO implements CrudDAO<Recompensa> {

    @Override
    public boolean insertar(Recompensa recompensa) {
        String sql = """
            INSERT INTO recompensas
            (nombre, costo_puntos, id_producto, id_menu, estado)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, recompensa.getNombre());
            ps.setInt(2, recompensa.getCostoPuntos());

            if (recompensa.getIdProducto() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, recompensa.getIdProducto());
            }

            if (recompensa.getIdMenu() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, recompensa.getIdMenu());
            }

            ps.setString(5, recompensa.getEstado());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar recompensa: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean actualizar(Recompensa recompensa) {
        String sql = """
            UPDATE recompensas
            SET nombre = ?, costo_puntos = ?, id_producto = ?,
                id_menu = ?, estado = ?
            WHERE id_recompensa = ?
        """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, recompensa.getNombre());
            ps.setInt(2, recompensa.getCostoPuntos());

            if (recompensa.getIdProducto() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, recompensa.getIdProducto());
            }

            if (recompensa.getIdMenu() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, recompensa.getIdMenu());
            }

            ps.setString(5, recompensa.getEstado());
            ps.setInt(6, recompensa.getIdRecompensa());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar recompensa: " + e.getMessage());
            return false;
        }
    }

   
@Override
public boolean eliminar(Recompensa recompensa) {
    String sql = "DELETE FROM recompensas WHERE id_recompensa = ?";

    try (Connection conexion = ConexionDB.conectar();
         PreparedStatement ps = conexion.prepareStatement(sql)) {

        ps.setInt(1, recompensa.getIdRecompensa());
        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        System.err.println("Error al eliminar recompensa: " + e.getMessage());
        return false;
    }
}

    @Override
    public Recompensa buscarPorId(int idRecompensa) {
        String sql = """
            SELECT id_recompensa, nombre, costo_puntos,
                   id_producto, id_menu, estado
            FROM recompensas
            WHERE id_recompensa = ?
        """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idRecompensa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearRecompensa(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar recompensa: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Recompensa> listarTodos() {
        List<Recompensa> recompensas = new ArrayList<>();

        String sql = """
            SELECT id_recompensa, nombre, costo_puntos,
                   id_producto, id_menu, estado
            FROM recompensas
            ORDER BY costo_puntos
        """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                recompensas.add(mapearRecompensa(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar recompensas: " + e.getMessage());
        }

        return recompensas;
    }

    public List<Recompensa> listarActivas() {
        List<Recompensa> recompensas = new ArrayList<>();

        String sql = """
            SELECT id_recompensa, nombre, costo_puntos,
                   id_producto, id_menu, estado
            FROM recompensas
            WHERE estado = 'ACTIVA'
            ORDER BY costo_puntos
        """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                recompensas.add(mapearRecompensa(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar recompensas activas: " + e.getMessage());
        }

        return recompensas;
    }

    private Recompensa mapearRecompensa(ResultSet rs) throws SQLException {
        int idProducto = rs.getInt("id_producto");
        Integer producto = rs.wasNull() ? null : idProducto;

        int idMenu = rs.getInt("id_menu");
        Integer menu = rs.wasNull() ? null : idMenu;

        return new Recompensa(
            rs.getInt("id_recompensa"),
            rs.getString("nombre"),
            rs.getInt("costo_puntos"),
            producto,
            menu,
            rs.getString("estado")
        );
    }
}
