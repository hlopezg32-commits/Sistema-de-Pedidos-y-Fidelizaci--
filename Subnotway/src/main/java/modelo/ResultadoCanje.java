package modelo;

public class ResultadoCanje {

    private boolean exitoso;
    private String mensaje;
    private int puntosGastados;
    private int saldoRestante;

    public ResultadoCanje() {
    }

    public ResultadoCanje(boolean exitoso, String mensaje, int puntosGastados, int saldoRestante) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.puntosGastados = puntosGastados;
        this.saldoRestante = saldoRestante;
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

    public int getPuntosGastados() {
        return puntosGastados;
    }

    public void setPuntosGastados(int puntosGastados) {
        this.puntosGastados = puntosGastados;
    }

    public int getSaldoRestante() {
        return saldoRestante;
    }

    public void setSaldoRestante(int saldoRestante) {
        this.saldoRestante = saldoRestante;
    }
}
