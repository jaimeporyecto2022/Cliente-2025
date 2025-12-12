package jjn.modelos;

import java.time.LocalDate;

public class Usuario {

    private int id;
    private String nombre;
    private String mail;
    private String password;
    private String rol;
    private Integer idDepartamento;
    private String nombreDepartamento;   // ← NUEVO
    private LocalDate fechaAlta;         // ← NUEVO
    private String direccion;            // ← NUEVO

    // Constructor vacío
    public Usuario() {}

    // Constructor completo (el que usarás al hacer login)
    public Usuario(int id, String nombre, String mail, String rol,
                   Integer idDepartamento, String nombreDepartamento,
                   LocalDate fechaAlta, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.mail = mail;
        this.rol = rol;
        this.idDepartamento = idDepartamento != null ? idDepartamento : 0;
        this.nombreDepartamento = nombreDepartamento != null ? nombreDepartamento : "Sin departamento";
        this.fechaAlta = fechaAlta;
        this.direccion = direccion != null ? direccion : "";
    }
    public Usuario(int id, String nombre, String mail, String rol,
                   Integer idDepartamento, String nombreDepartamento,
                    String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.mail = mail;
        this.rol = rol;
        this.idDepartamento = idDepartamento != null ? idDepartamento : 0;
        this.nombreDepartamento = nombreDepartamento != null ? nombreDepartamento : "Sin departamento";
        this.direccion = direccion != null ? direccion : "";
    }

    // ==================== GETTERS & SETTERS ====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) {
        this.idDepartamento = idDepartamento != null ? idDepartamento : 0;
    }

    public String getNombreDepartamento() { return nombreDepartamento; }
    public void setNombreDepartamento(String nombreDepartamento) {
        this.nombreDepartamento = nombreDepartamento != null ? nombreDepartamento : "Sin departamento";
    }


    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion != null ? direccion : ""; }

    // ==================== MÉTODOS ÚTILES ====================

    public boolean esAdmin() {
        return "admin".equalsIgnoreCase(rol);
    }
    public boolean esJefe() {
        return "jefe".equalsIgnoreCase(rol);
    }
    public boolean esEmpleado() {
        return "empleado".equalsIgnoreCase(rol);
    }
    public boolean esAdminOSuperior() {
        return esAdmin();
    }
    public boolean esJefeOSuperior() {
        return esAdmin() || esJefe();
    }
    /*@Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", mail='" + mail + '\'' +
                ", rol='" + rol + '\'' +
                ", departamento='" + nombreDepartamento + '\'' +
                ", fechaAlta=" + fechaAlta +
                ", direccion='" + direccion + '\'' +
                '}';
    }*/
    @Override
    public String toString() {
        return nombre;   // solo el nombre
    }
}