package modelo;

import java.time.LocalDateTime;

public class Pago {

    private int idPago;
    private int idPedido;
    private String metodo;
    private double monto;
    private double cambio;
    private String estado;
    private String referencia;
    private LocalDateTime fecha;

    public Pago(int idPago, int idPedido, String metodo, double monto,
                double cambio, String estado, String referencia,
                LocalDateTime fecha) {
        this.idPago = idPago;
        this.idPedido = idPedido;
        this.metodo = metodo;
        this.monto = monto;
        this.cambio = cambio;
        this.estado = estado;
        this.referencia = referencia;
        this.fecha = fecha;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public double getCambio() {
        return cambio;
    }

    public void setCambio(double cambio) {
        this.cambio = cambio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
