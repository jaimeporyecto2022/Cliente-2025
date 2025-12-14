package jjn.modelos;

import java.math.BigDecimal;

public class EstadisticaDepartamento {

    private String nombreDepartamento;
    private int totalTareas;
    private int tareasCompletadas;
    private int tareasFueraDeTiempo;
    private BigDecimal totalNominas;

    public EstadisticaDepartamento() {
    }

    public EstadisticaDepartamento(String nombreDepartamento,
                                   int totalTareas,
                                   int tareasCompletadas,
                                   int tareasFueraDeTiempo,
                                   BigDecimal totalNominas) {
        this.nombreDepartamento = nombreDepartamento;
        this.totalTareas = totalTareas;
        this.tareasCompletadas = tareasCompletadas;
        this.tareasFueraDeTiempo = tareasFueraDeTiempo;
        this.totalNominas = totalNominas;
    }

    public String getNombreDepartamento() {
        return nombreDepartamento;
    }

    public void setNombreDepartamento(String nombreDepartamento) {
        this.nombreDepartamento = nombreDepartamento;
    }

    public int getTotalTareas() {
        return totalTareas;
    }

    public void setTotalTareas(int totalTareas) {
        this.totalTareas = totalTareas;
    }

    public int getTareasCompletadas() {
        return tareasCompletadas;
    }

    public void setTareasCompletadas(int tareasCompletadas) {
        this.tareasCompletadas = tareasCompletadas;
    }

    public int getTareasFueraDeTiempo() {
        return tareasFueraDeTiempo;
    }

    public void setTareasFueraDeTiempo(int tareasFueraDeTiempo) {
        this.tareasFueraDeTiempo = tareasFueraDeTiempo;
    }

    public BigDecimal getTotalNominas() {
        return totalNominas;
    }

    public void setTotalNominas(BigDecimal totalNominas) {
        this.totalNominas = totalNominas;
    }

    @Override
    public String toString() {
        return "EstadisticaDepartamento{" +
                "nombreDepartamento='" + nombreDepartamento + '\'' +
                ", totalTareas=" + totalTareas +
                ", tareasCompletadas=" + tareasCompletadas +
                ", tareasFueraDeTiempo=" + tareasFueraDeTiempo +
                ", totalNominas=" + totalNominas +
                '}';
    }
}