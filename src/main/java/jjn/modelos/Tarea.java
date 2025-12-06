// src/main/java/jjn/modelos/Tarea.java
package jjn.modelos;

import java.time.LocalDate;

public class Tarea {
    private int id;
    private String titulo;
    private String descripcion;
    private LocalDate fechaCreacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private String nombreCreador;     // ← NUEVO
    private String nombreAsignado;    // ← NUEVO

    public Tarea(int id, String titulo, String descripcion,
                 LocalDate fechaCreacion, LocalDate fechaInicio, LocalDate fechaFin,
                 String estado, String nombreCreador, String nombreAsignado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.nombreCreador = nombreCreador;
        this.nombreAsignado = nombreAsignado;
    }

    // ==================== GETTERS ====================
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getEstado() { return estado; }
    public String getNombreCreador() { return nombreCreador; }
    public String getNombreAsignado() { return nombreAsignado; }

    // ==================== SETTERS (opcional, para futuro) ====================
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return String.format("%s → %s (%s)", titulo, nombreAsignado, estado);
    }
}