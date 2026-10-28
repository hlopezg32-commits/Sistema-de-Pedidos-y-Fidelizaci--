package modelo;

public class PedidoDetalle {

    private int idDetalle;
    private int idPedido;
    private Integer idProducto;
    private Integer idMenu;
    private int cantidad;
    private double precioUnitario;
    private String extras;

    // Constructor anterior, conservado por compatibilidad.
    public PedidoDetalle(int idDetalle, int idPedido,
            Integer idProducto, Integer idMenu,
            int cantidad, double precioUnitario) {

        this(idDetalle, idPedido, idProducto, idMenu,
                cantidad, precioUnitario, null);
    }

    // Constructor nuevo que incluye los extras.
    public PedidoDetalle(int idDetalle, int idPedido,
            Integer idProducto, Integer idMenu,
            int cantidad, double precioUnitario, String extras) {

        this.idDetalle = idDetalle;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.idMenu = idMenu;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.extras = extras;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(Integer idMenu) {
        this.idMenu = idMenu;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }
}
