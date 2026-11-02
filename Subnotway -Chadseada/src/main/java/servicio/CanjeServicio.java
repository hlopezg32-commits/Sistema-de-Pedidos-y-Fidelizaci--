package servicio;

import config.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class CanjeServicio {

    public void realizarCanje(
            int idCliente, 
            int idRecompensa, 
            Integer idProducto, 
            Integer idMenu, 
            int cantidad) throws SQLException {
        
        if (idProducto == null && idMenu == null) {
            throw new IllegalArgumentException("Debe especificar un producto o un menú para el canje.");
        }
        if (idProducto != null && idMenu != null) {
            throw new IllegalArgumentException("Un canje no puede ser producto y menú al mismo tiempo.");
        }

        try (Connection conexion = ConexionDB.conectar()) {
            boolean autoCommitAnterior = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            
            try {
                int puntosRequeridos = obtenerPuntosRecompensa(conexion, idRecompensa) * cantidad;
                validarPuntosCliente(conexion, idCliente, puntosRequeridos);
                
                // Descontar inventario dependiendo si es producto directo o combo
                if (idProducto != null) {
                    validarYDescontarExistenciaProducto(conexion, idProducto, cantidad);
                    registrarMovimientoInventario(conexion, idProducto, cantidad);
                } else {
                    descontarExistenciasMenu(conexion, idMenu, cantidad);
                }
                
                descontarPuntos(conexion, idCliente, puntosRequeridos);
                registrarMovimientoPuntos(conexion, idCliente, puntosRequeridos);
                
                conexion.commit();
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            } finally {
                conexion.setAutoCommit(autoCommitAnterior);
            }
        }
    }

    private int obtenerPuntosRecompensa(Connection conexion, int idRecompensa) throws SQLException {
        String sql = "SELECT puntos_requeridos FROM recompensas WHERE id_recompensa = ? AND estado = 'ACTIVO'";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idRecompensa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("puntos_requeridos");
                throw new IllegalArgumentException("Recompensa no válida o inactiva.");
            }
        }
    }

    private void validarPuntosCliente(Connection conexion, int idCliente, int puntosRequeridos) throws SQLException {
        String sql = "SELECT puntos FROM clientes WHERE id_cliente = ? AND estado = 'ACTIVO' FOR UPDATE";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Cliente no válido.");
                if (rs.getInt("puntos") < puntosRequeridos) throw new IllegalArgumentException("Puntos insuficientes.");
            }
        }
    }

    private void validarYDescontarExistenciaProducto(Connection conexion, int idProducto, int cantidad) throws SQLException {
        String selectSql = "SELECT existencia FROM productos WHERE id_producto = ? AND estado = 'ACTIVO' FOR UPDATE";
        try (PreparedStatement ps = conexion.prepareStatement(selectSql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Producto no válido.");
                if (rs.getInt("existencia") < cantidad) throw new IllegalStateException("Existencia insuficiente para el producto ID " + idProducto);
            }
        }
        String updateSql = "UPDATE productos SET existencia = existencia - ? WHERE id_producto = ?";
        try (PreparedStatement ps = conexion.prepareStatement(updateSql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    private void descontarExistenciasMenu(Connection conexion, int idMenu, int cantidadMenu) throws SQLException {
        String sql = "SELECT id_producto, cantidad FROM menu_componentes WHERE id_menu = ?";
        boolean tieneComponentes = false;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMenu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tieneComponentes = true;
                    int idProducto = rs.getInt("id_producto");
                    int cantidadNecesaria = rs.getInt("cantidad") * cantidadMenu;
                    
                    validarYDescontarExistenciaProducto(conexion, idProducto, cantidadNecesaria);
                    registrarMovimientoInventario(conexion, idProducto, cantidadNecesaria);
                }
            }
        }
        
        if (!tieneComponentes) {
            throw new IllegalStateException("El menú ID " + idMenu + " no tiene componentes de inventario asociados.");
        }
    }

    private void descontarPuntos(Connection conexion, int idCliente, int puntosRequeridos) throws SQLException {
        String sql = "UPDATE clientes SET puntos = puntos - ? WHERE id_cliente = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, puntosRequeridos);
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
    }

    private void registrarMovimientoPuntos(Connection conexion, int idCliente, int puntos) throws SQLException {
        String sql = "INSERT INTO movimientos_puntos (id_cliente, id_pedido, tipo, puntos) VALUES (?, NULL, 'CANJE', ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setInt(2, puntos);
            ps.executeUpdate();
        }
    }

    private void registrarMovimientoInventario(Connection conexion, int idProducto, int cantidad) throws SQLException {
        String sql = "INSERT INTO movimientos_inventario (id_producto, tipo, cantidad, observacion) VALUES (?, 'CANJE', ?, 'Canje de recompensa')";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, cantidad);
            ps.executeUpdate();
        }
    }
}