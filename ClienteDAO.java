
package dao.impl;

import config.ConexionDB;
import com.subnotway.dao.CrudDAO;
import modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements CrudDAO<Cliente> {

    @Override
    public boolean insertar(Cliente cliente) {
        String sql = """
            INSERT INTO clientes
            (identificador, nombre, telefono, correo)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getIdentificador());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());

            if (ps.executeUpdate() == 0) {
                return false;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setIdCliente(rs.getInt(1));
                    cliente.setPuntos(0);
                    cliente.setEstado("ACTIVO");
                    return true;
                }
            }

            return false;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo registrar el cliente.", e);
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        String sql = """
            UPDATE clientes
            SET identificador = ?, nombre = ?, telefono = ?,
                correo = ?, estado = ?
            WHERE id_cliente = ?
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, cliente.getIdentificador());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());
            ps.setString(5, cliente.getEstado());
            ps.setInt(6, cliente.getIdCliente());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo actualizar el cliente.", e);
        }
    }

    @Override
    public boolean eliminar(Cliente cliente) {
        // Se desactiva el cliente en lugar de borrar su registro.
        String sql = """
            UPDATE clientes
            SET estado = 'INACTIVO'
            WHERE id_cliente = ?
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, cliente.getIdCliente());

            boolean actualizado = ps.executeUpdate() > 0;

            if (actualizado) {
                cliente.setEstado("INACTIVO");
            }

            return actualizado;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo desactivar el cliente.", e);
        }
    }

    @Override
    public Cliente buscarPorId(int id) {
        String sql = """
            SELECT id_cliente, identificador, nombre, telefono,
                   correo, puntos, estado
            FROM clientes
            WHERE id_cliente = ?
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo buscar el cliente por ID.", e);
        }
    }

    public Cliente buscarPorIdentificador(String identificador) {
        String sql = """
            SELECT id_cliente, identificador, nombre, telefono,
                   correo, puntos, estado
            FROM clientes
            WHERE identificador = ?
            """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, identificador);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo buscar el cliente por identificador.", e);
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        String sql = """
            SELECT id_cliente, identificador, nombre, telefono,
                   correo, puntos, estado
            FROM clientes
            ORDER BY id_cliente
            """;

        List<Cliente> clientes = new ArrayList<>();

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }

            return clientes;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo listar los clientes.", e);
        }
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("identificador"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getInt("puntos"),
                rs.getString("estado")
        );
    }
}
