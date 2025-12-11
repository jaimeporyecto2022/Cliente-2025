package jjn.modelos;

import java.time.LocalDate;

public class Nomina {

    private int id;
    private double importe;
    private LocalDate fecha;
    private String concepto;
    private String tipo;          // salario, hora_extra, plus, deduccion
    private int idUsuario;

    // ======= CONSTRUCTORES =======

    public Nomina() {}

    public Nomina(int id, double importe, LocalDate fecha, String concepto, String tipo, int idUsuario) {
        this.id = id;
        this.importe = importe;
        this.fecha = fecha;
        this.concepto = concepto;
        this.tipo = tipo;
        this.idUsuario = idUsuario;
    }

    public Nomina(double importe, LocalDate fecha, String concepto, String tipo, int idUsuario) {
        this.importe = importe;
        this.fecha = fecha;
        this.concepto = concepto;
        this.tipo = tipo;
        this.idUsuario = idUsuario;
    }

    // ======= GETTERS & SETTERS =======

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    // ======= MÉTODOS ÚTILES =======

    public boolean esSalario() { return "salario".equalsIgnoreCase(tipo); }
    public boolean esHoraExtra() { return "hora_extra".equalsIgnoreCase(tipo); }
    public boolean esPlus() { return "plus".equalsIgnoreCase(tipo); }
    public boolean esDeduccion() { return "deduccion".equalsIgnoreCase(tipo); }

    @Override
    public String toString() {
        return "Nomina{" +
                "id=" + id +
                ", importe=" + importe +
                ", fecha=" + fecha +
                ", concepto='" + concepto + '\'' +
                ", tipo='" + tipo + '\'' +
                ", idUsuario=" + idUsuario +
                '}';
    }
}