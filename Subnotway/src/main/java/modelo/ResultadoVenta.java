package modelo;

public class ResultadoVenta {

    private boolean exitoso;
    private String mensaje;
    private int idPedido;
    private double total;
    private double montoRecibido;
    private double cambio;
    private int puntosGanados;
    private int nuevoSaldoPuntos;
    private String nombreCliente;
    private String metodoPago;

    public ResultadoVenta() {
    }

    public ResultadoVenta(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }

    public ResultadoVenta(boolean exitoso, String mensaje, int idPedido, double total, double montoRecibido, double cambio, int puntosGanados, int nuevoSaldoPuntos, String nombreCliente, String metodoPago) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.idPedido = idPedido;
        this.total = total;
        this.montoRecibido = montoRecibido;
        this.cambio = cambio;
        this.puntosGanados = puntosGanados;
        this.nuevoSaldoPuntos = nuevoSaldoPuntos;
        this.nombreCliente = nombreCliente;
        this.metodoPago = metodoPago;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(double montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    public double getCambio() {
        return cambio;
    }

    public void setCambio(double cambio) {
        this.cambio = cambio;
    }

    public int getPuntosGanados() {
        return puntosGanados;
    }

    public void setPuntosGanados(int puntosGanados) {
        this.puntosGanados = puntosGanados;
    }

    public int getNuevoSaldoPuntos() {
        return nuevoSaldoPuntos;
    }

    public void setNuevoSaldoPuntos(int nuevoSaldoPuntos) {
        this.nuevoSaldoPuntos = nuevoSaldoPuntos;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}
