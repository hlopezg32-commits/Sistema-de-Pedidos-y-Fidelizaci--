
package servicio;

import dao.impl.ClienteDAO;
import modelo.Cliente;

import java.util.List;

public class ClienteServicio {

    private final ClienteDAO clienteDAO;

    public ClienteServicio() {
        this.clienteDAO = new ClienteDAO();
    }

    public boolean registrarCliente(Cliente cliente) {
        if (cliente == null) {
            System.out.println("El cliente no puede estar vacío.");
            return false;
        }

        if (cliente.getIdentificador() == null
                || cliente.getIdentificador().trim().isEmpty()) {
            System.out.println("El identificador es obligatorio.");
            return false;
        }

        if (cliente.getNombre() == null
                || cliente.getNombre().trim().isEmpty()) {
            System.out.println("El nombre es obligatorio.");
            return false;
        }

        cliente.setPuntos(0);
        cliente.setEstado("ACTIVO");

        return clienteDAO.insertar(cliente);
    }

    public Cliente buscarClientePorId(int id) {
        if (id <= 0) {
            return null;
        }

        return clienteDAO.buscarPorId(id);
    }

    public Cliente buscarClientePorIdentificador(String identificador) {
        if (identificador == null || identificador.trim().isEmpty()) {
            return null;
        }

        return clienteDAO.buscarPorIdentificador(identificador.trim());
    }

    public boolean actualizarCliente(Cliente cliente) {
        if (cliente == null || cliente.getIdCliente() <= 0) {
            return false;
        }

        if (cliente.getIdentificador() == null
                || cliente.getIdentificador().trim().isEmpty()) {
            return false;
        }

        if (cliente.getNombre() == null
                || cliente.getNombre().trim().isEmpty()) {
            return false;
        }

        return clienteDAO.actualizar(cliente);
    }

    public boolean desactivarCliente(Cliente cliente) {
        if (cliente == null || cliente.getIdCliente() <= 0) {
            return false;
        }

        return clienteDAO.eliminar(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listarTodos();
    }
}
