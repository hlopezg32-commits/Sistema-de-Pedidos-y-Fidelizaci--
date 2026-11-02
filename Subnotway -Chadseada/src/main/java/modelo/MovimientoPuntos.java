package modelo;

import java.time.LocalDateTime;

public class MovimientoPuntos {

    private int idMovimiento;
    private int idCliente;
    private Integer idPedido;
    private String tipo;
    private int puntos;
    private LocalDateTime fecha;

    public MovimientoPuntos(int idMovimiento, int idCliente,
            Integer idPedido, String tipo, int puntos,
            LocalDateTime fecha) {
        this.idMovimiento = idMovimiento;
        this.idCliente = idCliente;
        this.idPedido = idPedido;
        this.tipo = tipo;
        this.puntos = puntos;
        this.fecha = fecha;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
