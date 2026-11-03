/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mvcproyecto.controlador;

import com.mycompany.mvcproyecto.dao.ProductoDAO;
import com.mycompany.mvcproyecto.modelo.Producto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author PC
 */
@Controller
@RequestMapping("/productos")
public class productoControlador {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @GetMapping
    public String listar(Model model) {

        model.addAttribute("productos", productoDAO.listarTodos());

        if (!model.containsAttribute("producto")) {
            model.addAttribute("producto", new Producto());
        }

        return "productos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable int id,
                         Model model,
                         RedirectAttributes ra) {

        Producto producto = productoDAO.buscarPorId(id);

        if (producto == null) {
            ra.addFlashAttribute("error",
                    "No se encontró el producto.");
            return "redirect:/productos";
        }

        model.addAttribute("productos", productoDAO.listarTodos());
        model.addAttribute("producto", producto);

        return "productos";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute("producto") Producto producto,
            RedirectAttributes ra) {

        try {
           
            if (producto.getCodigo() == null
                    || producto.getCodigo().isBlank()
                    || producto.getNombre() == null
                    || producto.getNombre().isBlank()
                    || producto.getCategoria() == null
                    || producto.getCategoria().isBlank()) {

                ra.addFlashAttribute("error",
                        "Completa todos los campos obligatorios.");
                return "redirect:/productos";
            }

         
            if (producto.getPrecio() < 0
                    || producto.getExistencia() < 0) {

                ra.addFlashAttribute("error",
                        "El precio y las existencias no pueden ser negativos.");
                return "redirect:/productos";
            }

         
            Producto existente =
                    productoDAO.buscarPorCodigo(
                            producto.getCodigo().trim());

            if (existente != null
                    && existente.getIdProducto()
                    != producto.getIdProducto()) {

                ra.addFlashAttribute("error",
                        "Ya existe un producto con ese código.");
                return "redirect:/productos";
            }

            producto.setCodigo(producto.getCodigo().trim());
            producto.setNombre(producto.getNombre().trim());

            boolean ok;

            if (producto.getIdProducto() == 0) {

                producto.setEstado("ACTIVO");
                ok = productoDAO.insertar(producto);

            } else {

                Producto original =
                        productoDAO.buscarPorId(
                                producto.getIdProducto());

                if (original == null) {
                    ra.addFlashAttribute("error",
                            "El producto que intentas actualizar no existe.");
                    return "redirect:/productos";
                }

             
                producto.setEstado(original.getEstado());

                ok = productoDAO.actualizar(producto);
            }

            if (ok) {
                ra.addFlashAttribute("mensaje",
                        "Producto guardado correctamente.");
            } else {
                ra.addFlashAttribute("error",
                        "No se pudo guardar el producto. "
                        + "Verifica los datos y la base de datos.");
            }

        } catch (Exception e) {
            ra.addFlashAttribute("error",
                    "Ocurrió un error al guardar el producto.");
        }

        return "redirect:/productos";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable int id,
                             RedirectAttributes ra) {

        Producto producto = productoDAO.buscarPorId(id);

        if (producto == null) {
            ra.addFlashAttribute("error",
                    "No se encontró el producto.");
        } else if ("INACTIVO".equalsIgnoreCase(
                producto.getEstado())) {
            ra.addFlashAttribute("error",
                    "El producto ya está desactivado.");
        } else if (productoDAO.eliminar(producto)) {
            ra.addFlashAttribute("mensaje",
                    "Producto desactivado correctamente.");
        } else {
            ra.addFlashAttribute("error",
                    "No se pudo desactivar el producto.");
        }

        return "redirect:/productos";
    }
    
    @PostMapping("/reactivar/{id}")
public String reactivarProducto(
        @PathVariable("id") int id,
        RedirectAttributes redirectAttributes) {

    boolean reactivado = productoDAO.reactivar(id);

    if (reactivado) {
        redirectAttributes.addFlashAttribute(
                "mensaje", "Producto reactivado correctamente.");
    } else {
        redirectAttributes.addFlashAttribute(
                "error", "No se pudo reactivar el producto.");
    }

    return "redirect:/productos";
}
  
}
