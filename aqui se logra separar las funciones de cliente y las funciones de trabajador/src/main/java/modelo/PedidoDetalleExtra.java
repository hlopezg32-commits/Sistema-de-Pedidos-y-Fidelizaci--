package modelo;

public class PedidoDetalleExtra {

    private int idDetalleExtra;
    private int idDetalle;
    private int idProducto;
    private int cantidad;
    private double precioUnitario;

    public PedidoDetalleExtra() {
    }

    public PedidoDetalleExtra(
            int idDetalleExtra,
            int idDetalle,
            int idProducto,
            int cantidad,
            double precioUnitario) {

        this.idDetalleExtra = idDetalleExtra;
        this.idDetalle = idDetalle;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int getIdDetalleExtra() {
        return idDetalleExtra;
    }

    public void setIdDetalleExtra(int idDetalleExtra) {
        this.idDetalleExtra = idDetalleExtra;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
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

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
