package controlador;

import dao.impl.ClienteDAO;
import modelo.Cliente;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteApiController {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @GetMapping("/puntos/{identificador}")
    public ResponseEntity<Map<String, Object>> consultarPuntos(@PathVariable String identificador) {
        Map<String, Object> respuesta = new HashMap<>();

        if (identificador == null || identificador.trim().isEmpty()) {
            respuesta.put("encontrado", false);
            respuesta.put("mensaje", "Debe ingresar un identificador valido.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        try {
            Cliente cliente = clienteDAO.buscarPorIdentificador(identificador.trim());

            if (cliente == null) {
                String limpio = identificador.trim().replace("-", "").replace(" ", "");
                for (Cliente c : clienteDAO.listarTodos()) {
                    String cLimpio = c.getIdentificador().replace("-", "").replace(" ", "");
                    if (cLimpio.equalsIgnoreCase(limpio)
                            || cLimpio.replaceAll("(?i)^cl", "").equals(limpio.replaceAll("(?i)^cli?", ""))
                            || String.valueOf(c.getIdCliente()).equals(limpio)) {
                        cliente = c;
                        break;
                    }
                }
            }

            if (cliente == null) {
                respuesta.put("encontrado", false);
                respuesta.put("mensaje", "No se encontro ningun cliente con el identificador ingresado.");
                return ResponseEntity.ok(respuesta);
            }

            respuesta.put("encontrado", true);
            respuesta.put("idCliente", cliente.getIdCliente());
            respuesta.put("identificador", cliente.getIdentificador());
            respuesta.put("nombre", cliente.getNombre());
            respuesta.put("telefono", cliente.getTelefono() != null ? cliente.getTelefono() : "");
            respuesta.put("correo", cliente.getCorreo() != null ? cliente.getCorreo() : "");
            respuesta.put("puntos", cliente.getPuntos());
            respuesta.put("estado", cliente.getEstado());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            respuesta.put("encontrado", false);
            respuesta.put("mensaje", "Error interno al consultar el saldo del cliente.");
            return ResponseEntity.internalServerError().body(respuesta);
        }
    }
}
