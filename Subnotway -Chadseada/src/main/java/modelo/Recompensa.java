package modelo;

public class Recompensa {

    private int idRecompensa;
    private String nombre;
    private int costoPuntos;
    private Integer idProducto;
    private Integer idMenu;
    private String estado;

    public Recompensa() {
    }

    public Recompensa(int idRecompensa, String nombre, int costoPuntos,
                      Integer idProducto, Integer idMenu, String estado) {
        this.idRecompensa = idRecompensa;
        this.nombre = nombre;
        this.costoPuntos = costoPuntos;
        this.idProducto = idProducto;
        this.idMenu = idMenu;
        this.estado = estado;
    }

    public int getIdRecompensa() {
        return idRecompensa;
    }

    public void setIdRecompensa(int idRecompensa) {
        this.idRecompensa = idRecompensa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCostoPuntos() {
        return costoPuntos;
    }

    public void setCostoPuntos(int costoPuntos) {
        this.costoPuntos = costoPuntos;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
