package dao.impl;

import com.subnotway.dao.CrudDAO;
import config.ConexionDB;
import modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO implements CrudDAO<Producto> {

    @Override
    public boolean insertar(Producto producto) {

        String sql = "INSERT INTO productos "
                + "(codigo, nombre, categoria, precio, existencia) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getCategoria());
            ps.setDouble(4, producto.getPrecio());
            ps.setInt(5, producto.getExistencia());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        producto.setIdProducto(rs.getInt(1));
                    }
                }
                producto.setEstado("ACTIVO");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar producto: "
                    + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean actualizar(Producto producto) {

        String sql = "UPDATE productos SET codigo = ?, nombre = ?, "
                + "categoria = ?, precio = ?, existencia = ?, estado = ? "
                + "WHERE id_producto = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getCategoria());
            ps.setDouble(4, producto.getPrecio());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getEstado());
            ps.setInt(7, producto.getIdProducto());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar producto: "
                    + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(Producto producto) {

        String sql = "UPDATE productos SET estado = 'INACTIVO' "
                + "WHERE id_producto = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, producto.getIdProducto());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al desactivar producto: "
                    + e.getMessage());
        }

        return false;
    }

    @Override
    public Producto buscarPorId(int id) {

        String sql = "SELECT * FROM productos WHERE id_producto = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar producto: "
                    + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Producto> listarTodos() {

        List<Producto> productos = new ArrayList<>();

        String sql = "SELECT * FROM productos ORDER BY id_producto";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar productos: "
                    + e.getMessage());
        }

        return productos;
    }

    public Producto buscarPorCodigo(String codigo) {

        String sql = "SELECT * FROM productos WHERE codigo = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar producto por código: "
                    + e.getMessage());
        }

        return null;
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {

        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getDouble("precio"),
                rs.getInt("existencia"),
                rs.getString("estado")
        );
    }
}
