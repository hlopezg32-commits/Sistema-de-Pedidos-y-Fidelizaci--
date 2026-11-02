package dao.impl;

import com.subnotway.dao.CrudDAO;
import config.ConexionDB;
import modelo.Menu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO implements CrudDAO<Menu> {

    @Override
    public boolean insertar(Menu menu) {

        String sql = "INSERT INTO menus "
                + "(codigo, nombre, precio, tipo, id_producto) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, menu.getCodigo());
            ps.setString(2, menu.getNombre());
            ps.setDouble(3, menu.getPrecio());
            ps.setString(4, menu.getTipo());

            if (menu.getIdProducto() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, menu.getIdProducto());
            }

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        menu.setIdMenu(rs.getInt(1));
                    }
                }

                menu.setEstado("ACTIVO");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar menú: "
                    + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean actualizar(Menu menu) {

        String sql = "UPDATE menus SET codigo = ?, nombre = ?, "
                + "precio = ?, estado = ?, tipo = ?, id_producto = ? "
                + "WHERE id_menu = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, menu.getCodigo());
            ps.setString(2, menu.getNombre());
            ps.setDouble(3, menu.getPrecio());
            ps.setString(4, menu.getEstado());
            ps.setString(5, menu.getTipo());

            if (menu.getIdProducto() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, menu.getIdProducto());
            }

            ps.setInt(7, menu.getIdMenu());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar menú: "
                    + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(Menu menu) {

        String sql = "UPDATE menus SET estado = 'INACTIVO' "
                + "WHERE id_menu = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, menu.getIdMenu());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al desactivar menú: "
                    + e.getMessage());
        }

        return false;
    }

    @Override
    public Menu buscarPorId(int id) {

        String sql = "SELECT * FROM menus WHERE id_menu = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMenu(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar menú: "
                    + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Menu> listarTodos() {

        List<Menu> menus = new ArrayList<>();

        String sql = "SELECT * FROM menus ORDER BY id_menu";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                menus.add(mapearMenu(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar menús: "
                    + e.getMessage());
        }

        return menus;
    }

    public Menu buscarPorCodigo(String codigo) {

        String sql = "SELECT * FROM menus WHERE codigo = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMenu(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar menú por código: "
                    + e.getMessage());
        }

        return null;
    }

    private Menu mapearMenu(ResultSet rs) throws SQLException {

        int idProductoLeido = rs.getInt("id_producto");
        Integer idProducto = rs.wasNull()
                ? null : idProductoLeido;

        return new Menu(
                rs.getInt("id_menu"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getString("estado"),
                rs.getString("tipo"),
                idProducto
        );
    }
}
