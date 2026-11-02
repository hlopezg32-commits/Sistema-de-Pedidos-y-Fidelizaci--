
package servicio;

import dao.impl.MenuDAO;
import modelo.Menu;
import java.util.ArrayList;
import java.util.List;

public class MenuServicio {

    private final MenuDAO menuDAO;

    public MenuServicio() {
        this.menuDAO = new MenuDAO();
    }

    public List<Menu> listarMenusActivos() {
        List<Menu> resultado = new ArrayList<>();
        List<Menu> menus = menuDAO.listarTodos();

        if (menus == null) {
            return resultado;
        }

        for (Menu menu : menus) {
            if (menu != null
                    && "ACTIVO".equalsIgnoreCase(menu.getEstado())) {
                resultado.add(menu);
            }
        }

        return resultado;
    }

    public Menu buscarMenuActivoPorId(int idMenu) {
        if (idMenu <= 0) {
            return null;
        }

        Menu menu = menuDAO.buscarPorId(idMenu);

        if (menu == null
                || !"ACTIVO".equalsIgnoreCase(menu.getEstado())) {
            return null;
        }

        return menu;
    }
}
