
package modelo;

public class Cliente {

    private int idCliente;
    private String identificador;
    private String nombre;
    private String telefono;
    private String correo;
    private int puntos;
    private String estado;

    public Cliente(int idCliente, String identificador,
            String nombre, String telefono, String correo,
            int puntos, String estado) {

        this.idCliente = idCliente;
        this.identificador = identificador;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.puntos = puntos;
        this.estado = estado;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
