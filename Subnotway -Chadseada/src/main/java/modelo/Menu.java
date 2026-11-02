package modelo;

public class Menu {

    private int idMenu;
    private String codigo;
    private String nombre;
    private double precio;
    private String estado;
    private String tipo;
    private Integer idProducto;

    // Constructor anterior: se conserva por compatibilidad
    public Menu(int idMenu, String codigo, String nombre,
            double precio, String estado) {
        this(idMenu, codigo, nombre, precio, estado, "SANDWICH", null);
    }

    // Constructor nuevo con los campos agregados
    public Menu(int idMenu, String codigo, String nombre,
            double precio, String estado, String tipo,
            Integer idProducto) {
        this.idMenu = idMenu;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.estado = estado;
        this.tipo = tipo;
        this.idProducto = idProducto;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }
}
