package dao.impl;

import config.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteDAO {

    public List<Map<String, Object>> obtenerVentasPorPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT metodo, COUNT(id_pago) AS total_operaciones,
                   COALESCE(SUM(monto), 0) AS total_recaudado,
                   COALESCE(SUM(cambio), 0) AS total_cambio
            FROM pagos
            WHERE estado IN ('APROBADO', 'COMPLETADO') AND (fecha BETWEEN ? AND ?)
            GROUP BY metodo
            ORDER BY metodo
            """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("metodo", rs.getString("metodo"));
                    fila.put("operaciones", rs.getInt("total_operaciones"));
                    fila.put("recaudado", rs.getDouble("total_recaudado"));
                    fila.put("cambio", rs.getDouble("total_cambio"));
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Map<String, Object>> obtenerProductosMasVendidos() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT prod.codigo, prod.nombre, prod.categoria,
                   SUM(t.unidades) AS total_unidades
            FROM (
                SELECT pd.id_producto, pd.cantidad AS unidades
                FROM pedido_detalles pd
                JOIN pedidos p ON pd.id_pedido = p.id_pedido
                WHERE pd.id_producto IS NOT NULL AND p.estado IN ('PAGADO', 'ENTREGADO', 'COMPLETADO')
                
                UNION ALL
                
                SELECT mc.id_producto, (pd.cantidad * mc.cantidad) AS unidades
                FROM pedido_detalles pd
                JOIN pedidos p ON pd.id_pedido = p.id_pedido
                JOIN menu_componentes mc ON pd.id_menu = mc.id_menu
                WHERE pd.id_menu IS NOT NULL AND p.estado IN ('PAGADO', 'ENTREGADO', 'COMPLETADO')
            ) t
            JOIN productos prod ON t.id_producto = prod.id_producto
            GROUP BY prod.id_producto, prod.codigo, prod.nombre, prod.categoria
            ORDER BY total_unidades DESC
            """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("codigo", rs.getString("codigo"));
                fila.put("nombre", rs.getString("nombre"));
                fila.put("categoria", rs.getString("categoria"));
                fila.put("unidades", rs.getInt("total_unidades"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Map<String, Object>> obtenerProductosStockBajo() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT codigo, nombre, categoria, precio, existencia, estado
            FROM productos
            WHERE existencia <= 5
            ORDER BY existencia ASC, nombre ASC
            """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("codigo", rs.getString("codigo"));
                fila.put("nombre", rs.getString("nombre"));
                fila.put("categoria", rs.getString("categoria"));
                fila.put("precio", rs.getDouble("precio"));
                fila.put("existencia", rs.getInt("existencia"));
                fila.put("estado", rs.getString("estado"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Map<String, Object>> obtenerPuntosAcumuladosClientes() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT identificador, nombre, telefono, correo, puntos, estado
            FROM clientes
            ORDER BY puntos DESC, nombre ASC
            """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("identificador", rs.getString("identificador"));
                fila.put("nombre", rs.getString("nombre"));
                fila.put("telefono", rs.getString("telefono"));
                fila.put("correo", rs.getString("correo"));
                fila.put("puntos", rs.getInt("puntos"));
                fila.put("estado", rs.getString("estado"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Map<String, Object>> obtenerHistorialCanjes() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT c.id_canje, cl.identificador, cl.nombre AS cliente,
                   r.nombre AS recompensa, c.puntos_utilizados, c.fecha
            FROM canjes c
            JOIN clientes cl ON c.id_cliente = cl.id_cliente
            JOIN recompensas r ON c.id_recompensa = r.id_recompensa
            ORDER BY c.fecha DESC
            """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("idCanje", rs.getInt("id_canje"));
                fila.put("identificador", rs.getString("identificador"));
                fila.put("cliente", rs.getString("cliente"));
                fila.put("recompensa", rs.getString("recompensa"));
                fila.put("puntos", rs.getInt("puntos_utilizados"));
                fila.put("fecha", rs.getTimestamp("fecha"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Map<String, Object>> obtenerVentasMenusCompletos() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = """
            SELECT m.codigo, m.nombre, m.precio,
                   COALESCE(SUM(pd.cantidad), 0) AS unidades_vendidas,
                   COALESCE(SUM(pd.cantidad * pd.precio_unitario), 0) AS total_recaudado
            FROM menus m
            LEFT JOIN pedido_detalles pd ON m.id_menu = pd.id_menu
            LEFT JOIN pedidos p ON pd.id_pedido = p.id_pedido AND p.estado IN ('PAGADO', 'ENTREGADO', 'COMPLETADO')
            GROUP BY m.id_menu, m.codigo, m.nombre, m.precio
            ORDER BY unidades_vendidas DESC
            """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("codigo", rs.getString("codigo"));
                fila.put("nombre", rs.getString("nombre"));
                fila.put("precio", rs.getDouble("precio"));
                fila.put("unidades", rs.getInt("unidades_vendidas"));
                fila.put("total", rs.getDouble("total_recaudado"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
