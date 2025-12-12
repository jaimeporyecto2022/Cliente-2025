package jjn.modelos;

import java.time.LocalDate;

public class Reporte {

    private Integer id;
    private LocalDate fechacreacion;
    private String informacion;
    private String estado;  // en_curso, finalizada, irrealizable, transferir
    private Integer idUsuarioReporte;
    private Integer idTarea;
    private String nombreUsuario; // viene del servidor

    public Reporte() {}

    public Reporte(Integer id, LocalDate fechacreacion,
                   String informacion, String estado,
                   Integer idUsuarioReporte, Integer idTarea,
                   String nombreUsuario) {
        this.id = id;
        this.fechacreacion = fechacreacion;
        this.informacion = informacion;
        this.estado = estado;
        this.idUsuarioReporte = idUsuarioReporte;
        this.idTarea = idTarea;
        this.nombreUsuario = nombreUsuario;
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }



    public String getInformacion() {
        return informacion;
    }
    public void setInformacion(String informacion) {
        this.informacion = informacion;
    }

    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdUsuarioReporte() {
        return idUsuarioReporte;
    }
    public void setIdUsuarioReporte(Integer idUsuarioReporte) {
        this.idUsuarioReporte = idUsuarioReporte;
    }

    public Integer getIdTarea() {
        return idTarea;
    }
    public void setIdTarea(Integer idTarea) {
        this.idTarea = idTarea;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public String toString() {
        return "Reporte{" +
                "id=" + id +
                ", fechaInicio="+ fechacreacion +
                ", estado='" + estado + '\'' +
                ", usuario='" + nombreUsuario + '\'' +
                '}';
    }

    public LocalDate getFechacreacion() {
        return fechacreacion;
    }

    public void setFechacreacion(LocalDate fechacreacion) {
        this.fechacreacion = fechacreacion;
    }
}
