package modelo;

import java.util.ArrayList;
import java.util.List;

public class PedidoDetalle {

    private int idDetalle;
    private int idPedido;
    private Integer idProducto;
    private Integer idMenu;
    private int cantidad;
    private double precioUnitario;
    private List<PedidoDetalleExtra> extras = new ArrayList<>();

    public PedidoDetalle() {
    }

    public PedidoDetalle(
            int idDetalle,
            int idPedido,
            Integer idProducto,
            Integer idMenu,
            int cantidad,
            double precioUnitario) {

        this.idDetalle = idDetalle;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.idMenu = idMenu;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
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

    public List<PedidoDetalleExtra> getExtras() {
        return extras;
    }

    public void setExtras(List<PedidoDetalleExtra> extras) {
        this.extras = extras == null
                ? new ArrayList<>()
                : new ArrayList<>(extras);
    }
}
