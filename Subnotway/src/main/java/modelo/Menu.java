package modelo;

public class Menu {

    private int idMenu;
    private String codigo;
    private String nombre;
    private double precio;
    private String estado;

    public Menu(int idMenu, String codigo, String nombre,
            double precio, String estado) {
        this.idMenu = idMenu;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.estado = estado;
    }

    public int getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(int idMenu) {
        this.idMenu = idMenu;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
