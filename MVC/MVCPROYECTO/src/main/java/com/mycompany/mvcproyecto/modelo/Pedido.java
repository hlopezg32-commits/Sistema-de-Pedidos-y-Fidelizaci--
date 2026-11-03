package com.mycompany.mvcproyecto.modelo;

import java.time.LocalDateTime;

public class Pedido {

    private int idPedido;
    private int idCliente;
    private LocalDateTime fecha;
    private String estado;
    private double total;

    public Pedido(int idPedido, int idCliente, LocalDateTime fecha,
                  String estado, double total) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
