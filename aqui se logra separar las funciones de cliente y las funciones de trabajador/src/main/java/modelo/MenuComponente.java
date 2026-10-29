package modelo;

public class MenuComponente {

    private int idMenu;
    private int idProducto;
    private int cantidad;
    private int cantidadExtra;

    // Constructor anterior, conservado por compatibilidad
    public MenuComponente(int idMenu, int idProducto, int cantidad) {
        this(idMenu, idProducto, cantidad, 0);
    }

    // Constructor nuevo con cantidad de extras
    public MenuComponente(int idMenu, int idProducto,
            int cantidad, int cantidadExtra) {
        this.idMenu = idMenu;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.cantidadExtra = cantidadExtra;
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

    public int getCantidadExtra() {
        return cantidadExtra;
    }

    public void setCantidadExtra(int cantidadExtra) {
        this.cantidadExtra = cantidadExtra;
    }
}
