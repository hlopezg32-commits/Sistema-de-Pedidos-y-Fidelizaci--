
package servicio;

import dao.impl.ProductoDAO;
import modelo.Producto;

import java.util.ArrayList;
import java.util.List;

public class ProductoServicio {

    private final ProductoDAO productoDAO = new ProductoDAO();

    public List<Producto> listarProductosActivos() {
        List<Producto> resultado = new ArrayList<>();

        List<Producto> productos = productoDAO.listarTodos();

        if (productos == null) {
            return resultado;
        }

        for (Producto producto : productos) {
            if (producto != null
                    && "ACTIVO".equalsIgnoreCase(producto.getEstado())) {
                resultado.add(producto);
            }
        }

        return resultado;
    }

    public Producto buscarProductoActivoPorId(int idProducto) {
        if (idProducto <= 0) {
            return null;
        }

        Producto producto = productoDAO.buscarPorId(idProducto);

        if (producto == null
                || !"ACTIVO".equalsIgnoreCase(producto.getEstado())) {
            return null;
        }

        return producto;
    }
}
