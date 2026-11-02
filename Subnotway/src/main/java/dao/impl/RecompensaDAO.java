package dao.impl;

import com.subnotway.dao.CrudDAO;
import config.ConexionDB;
import modelo.Recompensa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class RecompensaDAO implements CrudDAO<Recompensa> {

    @Override
    public boolean insertar(Recompensa r) {
        String sql = "INSERT INTO recompensas (nombre, puntos_requeridos, id_producto, id_menu, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNombre());
            ps.setInt(2, r.getCostoPuntos());
            if (r.getIdProducto() != null) {
                ps.setInt(3, r.getIdProducto());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (r.getIdMenu() != null) {
                ps.setInt(4, r.getIdMenu());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, r.getEstado() != null ? r.getEstado() : "ACTIVO");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        r.setIdRecompensa(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            String fallbackSql = "INSERT INTO recompensas (nombre, costo_puntos, id_producto, id_menu, estado) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = ConexionDB.conectar();
                 PreparedStatement ps = con.prepareStatement(fallbackSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, r.getNombre());
                ps.setInt(2, r.getCostoPuntos());
                if (r.getIdProducto() != null) {
                    ps.setInt(3, r.getIdProducto());
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                if (r.getIdMenu() != null) {
                    ps.setInt(4, r.getIdMenu());
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setString(5, r.getEstado() != null ? r.getEstado() : "ACTIVO");

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            r.setIdRecompensa(rs.getInt(1));
                        }
                    }
                    return true;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean actualizar(Recompensa r) {
        String sql = "UPDATE recompensas SET nombre = ?, puntos_requeridos = ?, id_producto = ?, id_menu = ?, estado = ? WHERE id_recompensa = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getNombre());
            ps.setInt(2, r.getCostoPuntos());
            if (r.getIdProducto() != null) {
                ps.setInt(3, r.getIdProducto());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (r.getIdMenu() != null) {
                ps.setInt(4, r.getIdMenu());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, r.getEstado());
            ps.setInt(6, r.getIdRecompensa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            String fallbackSql = "UPDATE recompensas SET nombre = ?, costo_puntos = ?, id_producto = ?, id_menu = ?, estado = ? WHERE id_recompensa = ?";
            try (Connection con = ConexionDB.conectar();
                 PreparedStatement ps = con.prepareStatement(fallbackSql)) {
                ps.setString(1, r.getNombre());
                ps.setInt(2, r.getCostoPuntos());
                if (r.getIdProducto() != null) {
                    ps.setInt(3, r.getIdProducto());
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                if (r.getIdMenu() != null) {
                    ps.setInt(4, r.getIdMenu());
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setString(5, r.getEstado());
                ps.setInt(6, r.getIdRecompensa());
                return ps.executeUpdate() > 0;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean eliminar(Recompensa r) {
        String sql = "UPDATE recompensas SET estado = 'INACTIVO' WHERE id_recompensa = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getIdRecompensa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Recompensa buscarPorId(int id) {
        String sql = "SELECT * FROM recompensas WHERE id_recompensa = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearRecompensa(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Recompensa> listarActivas() {
        List<Recompensa> lista = new ArrayList<>();
        String sql = "SELECT * FROM recompensas WHERE estado IN ('ACTIVO', 'ACTIVA') ORDER BY id_recompensa ASC";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearRecompensa(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Recompensa> listarTodos() {
        List<Recompensa> lista = new ArrayList<>();
        String sql = "SELECT * FROM recompensas ORDER BY id_recompensa ASC";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearRecompensa(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Recompensa mapearRecompensa(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_recompensa");
        String nombre = rs.getString("nombre");
        int costo = 0;
        try {
            costo = rs.getInt("puntos_requeridos");
        } catch (SQLException e) {
            costo = rs.getInt("costo_puntos");
        }
        int prodVal = rs.getInt("id_producto");
        Integer idProducto = rs.wasNull() ? null : prodVal;
        int menuVal = rs.getInt("id_menu");
        Integer idMenu = rs.wasNull() ? null : menuVal;
        String estado = rs.getString("estado");
        return new Recompensa(id, nombre, costo, idProducto, idMenu, estado);
    }
}
