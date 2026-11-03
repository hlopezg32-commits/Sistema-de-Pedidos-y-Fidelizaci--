package controlador;

import dao.impl.MovimientoInventarioDAO;
import dao.impl.ProductoDAO;
import modelo.MovimientoInventario;
import modelo.Producto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoInventarioDAO movimientoInventarioDAO = new MovimientoInventarioDAO();

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoDAO.listarTodos());
        if (!model.containsAttribute("producto")) {
            model.addAttribute("producto", new Producto());
        }
        return "productos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable int id, Model model, RedirectAttributes ra) {
        Producto producto = productoDAO.buscarPorId(id);
        if (producto == null) {
            ra.addFlashAttribute("error", "No se encontro el producto.");
            return "redirect:/productos";
        }
        model.addAttribute("productos", productoDAO.listarTodos());
        model.addAttribute("producto", producto);
        return "productos";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("producto") Producto producto, RedirectAttributes ra) {
        try {
            if (producto.getCodigo() == null || producto.getCodigo().isBlank()
                    || producto.getNombre() == null || producto.getNombre().isBlank()
                    || producto.getCategoria() == null || producto.getCategoria().isBlank()) {
                ra.addFlashAttribute("error", "Completa todos los campos obligatorios.");
                return "redirect:/productos";
            }

            if (producto.getPrecio() < 0 || producto.getExistencia() < 0) {
                ra.addFlashAttribute("error", "El precio y las existencias no pueden ser negativos.");
                return "redirect:/productos";
            }

            Producto existente = productoDAO.buscarPorCodigo(producto.getCodigo().trim());
            if (existente != null && existente.getIdProducto() != producto.getIdProducto()) {
                ra.addFlashAttribute("error", "Ya existe un producto con ese codigo.");
                return "redirect:/productos";
            }

            producto.setCodigo(producto.getCodigo().trim());
            producto.setNombre(producto.getNombre().trim());

            boolean ok;
            if (producto.getIdProducto() == 0) {
                producto.setEstado("ACTIVO");
                ok = productoDAO.insertar(producto);
                if (ok && producto.getExistencia() > 0) {
                    MovimientoInventario mov = new MovimientoInventario(0, producto.getIdProducto(), "ENTRADA", producto.getExistencia(), null, "Inventario inicial");
                    movimientoInventarioDAO.insertar(mov);
                }
            } else {
                Producto original = productoDAO.buscarPorId(producto.getIdProducto());
                if (original == null) {
                    ra.addFlashAttribute("error", "El producto que intentas actualizar no existe.");
                    return "redirect:/productos";
                }
                producto.setEstado(original.getEstado());
                ok = productoDAO.actualizar(producto);
            }

            if (ok) {
                ra.addFlashAttribute("mensaje", "Producto guardado correctamente.");
            } else {
                ra.addFlashAttribute("error", "No se pudo guardar el producto.");
            }

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Ocurrio un error al guardar el producto.");
        }

        return "redirect:/productos";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable int id, RedirectAttributes ra) {
        Producto producto = productoDAO.buscarPorId(id);
        if (producto == null) {
            ra.addFlashAttribute("error", "No se encontro el producto.");
        } else if ("INACTIVO".equalsIgnoreCase(producto.getEstado())) {
            ra.addFlashAttribute("error", "El producto ya esta desactivado.");
        } else if (productoDAO.eliminar(producto)) {
            ra.addFlashAttribute("mensaje", "Producto desactivado correctamente.");
        } else {
            ra.addFlashAttribute("error", "No se pudo desactivar el producto.");
        }
        return "redirect:/productos";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable int id, RedirectAttributes ra) {
        boolean ok = productoDAO.reactivar(id);
        if (ok) {
            ra.addFlashAttribute("mensaje", "Producto reactivado correctamente.");
        } else {
            ra.addFlashAttribute("error", "No se pudo reactivar el producto.");
        }
        return "redirect:/productos";
    }

    @PostMapping("/reabastecer")
    public String reabastecer(@RequestParam int idProducto, @RequestParam int cantidad, @RequestParam(required = false) String observacion, RedirectAttributes ra) {
        if (cantidad <= 0) {
            ra.addFlashAttribute("error", "La cantidad a ingresar debe ser mayor a 0.");
            return "redirect:/productos";
        }
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (producto == null) {
            ra.addFlashAttribute("error", "El producto no existe.");
            return "redirect:/productos";
        }
        boolean ok = productoDAO.reabastecerExistencia(idProducto, cantidad);
        if (ok) {
            String obs = (observacion != null && !observacion.isBlank()) ? observacion : "Reabastecimiento de existencias";
            MovimientoInventario mov = new MovimientoInventario(0, idProducto, "ENTRADA", cantidad, null, obs);
            movimientoInventarioDAO.insertar(mov);
            ra.addFlashAttribute("mensaje", "Se han ingresado " + cantidad + " unidades de '" + producto.getNombre() + "' al inventario.");
        } else {
            ra.addFlashAttribute("error", "No se pudo actualizar el inventario.");
        }
        return "redirect:/productos";
    }
}
