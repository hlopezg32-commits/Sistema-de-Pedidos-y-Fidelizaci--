/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mvcproyecto.controlador;

import com.mycompany.mvcproyecto.dao.ClienteDAO;
import com.mycompany.mvcproyecto.modelo.Cliente;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/**
 *
 * @author PC
 */
@Controller
@RequestMapping("/clientes")   
public class clienteControlador {

    private final ClienteDAO clienteDAO;

    public clienteControlador() {
        this.clienteDAO = new ClienteDAO();
    }


    @GetMapping
    public String listar(Model model) {
        try {
            model.addAttribute("clientes", clienteDAO.listarTodos());
            return "clientes/lista";
        } catch (Exception e) {
            model.addAttribute(
                    "error", "No se pudieron cargar los clientes.");
            model.addAttribute("clientes",
                    java.util.Collections.emptyList());
            return "clientes/lista";
        }
    }


    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new Cliente(
                0, "", "", "", "", 0, "ACTIVO"));
        model.addAttribute("modoEdicion", false);

        return "clientes/formulario";
    }


    @PostMapping("/guardar")
    public String guardar(
            @RequestParam String identificador,
            @RequestParam String nombre,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String correo,
            RedirectAttributes redirectAttributes) {

        identificador = limpiar(identificador);
        nombre = limpiar(nombre);
        telefono = limpiar(telefono);
        correo = limpiar(correo);

        if (identificador.isEmpty() || nombre.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "El identificador y el nombre son obligatorios.");
            return "redirect:/clientes/nuevo";
        }

        try {
            Cliente existente =
                    clienteDAO.buscarPorIdentificador(identificador);

            if (existente != null) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Ya existe un cliente con ese identificador.");
                return "redirect:/clientes/nuevo";
            }

            Cliente cliente = new Cliente(
                    0,
                    identificador,
                    nombre,
                    telefono,
                    correo,
                    0,
                    "ACTIVO"
            );

            if (clienteDAO.insertar(cliente)) {
                redirectAttributes.addFlashAttribute(
                        "mensaje",
                        "Cliente registrado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute(
                        "error", "No se pudo registrar el cliente.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ocurrió un error al registrar el cliente.");
        }

        return "redirect:/clientes";
    }


    @GetMapping("/buscar")
    public String buscar(
            @RequestParam(required = false) String identificador,
            Model model) {

        try {
            if (identificador == null || identificador.isBlank()) {
                model.addAttribute(
                        "clientes", clienteDAO.listarTodos());
            } else {
                Cliente cliente =
                        clienteDAO.buscarPorIdentificador(
                                identificador.trim());

                java.util.List<Cliente> resultados =
                        new java.util.ArrayList<>();

                if (cliente != null) {
                    resultados.add(cliente);
                }

                model.addAttribute("clientes", resultados);

                if (cliente == null) {
                    model.addAttribute(
                            "error",
                            "No se encontró ningún cliente con ese identificador.");
                }
            }
        } catch (Exception e) {
            model.addAttribute(
                    "clientes",
                    java.util.Collections.emptyList());
            model.addAttribute(
                    "error", "No se pudo realizar la búsqueda.");
        }

        return "clientes/lista";
    }


    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable int id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Cliente cliente = clienteDAO.buscarPorId(id);

            if (cliente == null) {
                redirectAttributes.addFlashAttribute(
                        "error", "El cliente no existe.");
                return "redirect:/clientes";
            }

            model.addAttribute("cliente", cliente);
            model.addAttribute("modoEdicion", true);

            return "clientes/formulario";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", "No se pudo cargar el cliente.");
            return "redirect:/clientes";
        }
    }

    // Actualizar los datos del cliente
    @PostMapping("/actualizar")
    public String actualizar(
            @RequestParam int idCliente,
            @RequestParam String identificador,
            @RequestParam String nombre,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String correo,
            RedirectAttributes redirectAttributes) {

        identificador = limpiar(identificador);
        nombre = limpiar(nombre);
        telefono = limpiar(telefono);
        correo = limpiar(correo);

        if (identificador.isEmpty() || nombre.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "El identificador y el nombre son obligatorios.");
            return "redirect:/clientes/editar/" + idCliente;
        }

        try {
            Cliente cliente = clienteDAO.buscarPorId(idCliente);

            if (cliente == null) {
                redirectAttributes.addFlashAttribute(
                        "error", "El cliente no existe.");
                return "redirect:/clientes";
            }

            Cliente otro =
                    clienteDAO.buscarPorIdentificador(identificador);

            if (otro != null && otro.getIdCliente() != idCliente) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Ese identificador ya pertenece a otro cliente.");
                return "redirect:/clientes/editar/" + idCliente;
            }


            cliente.setIdentificador(identificador);
            cliente.setNombre(nombre);
            cliente.setTelefono(telefono);
            cliente.setCorreo(correo);

            if (clienteDAO.actualizar(cliente)) {
                redirectAttributes.addFlashAttribute(
                        "mensaje",
                        "Cliente actualizado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute(
                        "error", "No se pudo actualizar el cliente.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ocurrió un error al actualizar el cliente.");
        }

        return "redirect:/clientes";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(
            @PathVariable int id,
            RedirectAttributes redirectAttributes) {

        try {
            Cliente cliente = clienteDAO.buscarPorId(id);

            if (cliente == null) {
                redirectAttributes.addFlashAttribute(
                        "error", "El cliente no existe.");
                return "redirect:/clientes";
            }

            if ("INACTIVO".equalsIgnoreCase(cliente.getEstado())) {
                redirectAttributes.addFlashAttribute(
                        "error", "El cliente ya está inactivo.");
                return "redirect:/clientes";
            }

            if (clienteDAO.eliminar(cliente)) {
                redirectAttributes.addFlashAttribute(
                        "mensaje",
                        "Cliente desactivado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute(
                        "error", "No se pudo desactivar el cliente.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ocurrió un error al desactivar el cliente.");
        }

        return "redirect:/clientes";
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    } 
}
