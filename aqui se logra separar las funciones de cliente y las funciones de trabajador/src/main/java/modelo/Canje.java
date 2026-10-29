package modelo;

import java.time.LocalDateTime;

public class Canje {

    private int idCanje;
    private int idCliente;
    private int idRecompensa;
    private int puntosUtilizados;
    private LocalDateTime fecha;

    public Canje(int idCanje, int idCliente, int idRecompensa,
            int puntosUtilizados, LocalDateTime fecha) {
        this.idCanje = idCanje;
        this.idCliente = idCliente;
        this.idRecompensa = idRecompensa;
        this.puntosUtilizados = puntosUtilizados;
        this.fecha = fecha;
    }

    public int getIdCanje() {
        return idCanje;
    }

    public void setIdCanje(int idCanje) {
        this.idCanje = idCanje;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdRecompensa() {
        return idRecompensa;
    }

    public void setIdRecompensa(int idRecompensa) {
        this.idRecompensa = idRecompensa;
    }

    public int getPuntosUtilizados() {
        return puntosUtilizados;
    }

    public void setPuntosUtilizados(int puntosUtilizados) {
        this.puntosUtilizados = puntosUtilizados;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
