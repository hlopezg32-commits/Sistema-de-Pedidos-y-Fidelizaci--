package modelo;

public class MenuComponente {

    private int idMenu;
    private int idProducto;
    private int cantidad;

    public MenuComponente(int idMenu, int idProducto, int cantidad) {
        this.idMenu = idMenu;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
    }

    public int getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(int idMenu) {
        this.idMenu = idMenu;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
