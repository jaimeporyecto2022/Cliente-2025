package jjn;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import jjn.modelos.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;

public class DashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label lblUsuarioYRol;
    @FXML private VBox sidebar;
    @FXML private ScrollPane contentArea;

    private Usuario usuario;
    private ConexionCliente conexion;

    // Clase interna para las tareas
    public static class Tarea {
        private int id;
        private String titulo;
        private String descripcion;
        private LocalDate fechaCreacion;
        private String estado;

        public Tarea(int id, String titulo, String descripcion, LocalDate fechaCreacion, String estado) {
            this.id = id;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.fechaCreacion = fechaCreacion;
            this.estado = estado;
        }

        // Getters
        public int getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getDescripcion() { return descripcion; }
        public LocalDate getFechaCreacion() { return fechaCreacion; }
        public String getEstado() { return estado; }
    }

    private ObservableList<Tarea> listaTareas = FXCollections.observableArrayList();

    public void iniciarSesion(Usuario usuario, ConexionCliente conexion, Stage stage) {
        this.usuario = usuario;
        this.conexion = conexion;

        // CABECERA: NOMBRE - ROL
        String rolMayus = usuario.getRol().toUpperCase();
        if (usuario.esAdmin()) rolMayus = "ADMINISTRADOR";
        else if ("jefe".equalsIgnoreCase(usuario.getRol())) rolMayus = "JEFE";

        lblUsuarioYRol.setText(usuario.getNombre().toUpperCase() + " - " + rolMayus);
        if (usuario.esAdmin()) {
            lblUsuarioYRol.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 28px; -fx-font-weight: bold;");
        }

        construirMenuLateral();
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setResizable(false);
        stage.centerOnScreen();
    }

    private void construirMenuLateral() {
        sidebar.getChildren().clear();
        sidebar.setPrefWidth(80);
        sidebar.setStyle("-fx-background-color: #111111; -fx-padding: 20 10; -fx-spacing: 15;");

        agregarBoton("home", "Inicio", () -> mostrarMensaje("JJN IMPERIO"));
        agregarBoton("tasks", "Tareas", this::mostrarMisTareas);  // ← AQUÍ SE MUESTRAN LAS TAREAS
        agregarBoton("file-invoice-dollar", "Nóminas", () -> mostrarMensaje("NÓMINAS"));
        agregarBoton("chart-bar", "Reportes", () -> mostrarMensaje("REPORTES"));

        if (usuario.esJefeOSuperior()) {
            agregarBoton("users-cog", "Usuarios", () -> mostrarMensaje("GESTIÓN DE USUARIOS"));
        }

        if (usuario.esAdmin()) {
            agregarBoton("crown", "Admin", () -> mostrarMensaje("PANEL DE ADMINISTRADOR"));
        }

        agregarBoton("sign-out-alt", "Salir", this::cerrarSesion);
    }

    private void mostrarMisTareas() {
        // SIMULAMOS TAREAS REALES (luego las pedirás al servidor)
        listaTareas.clear();
        listaTareas.addAll(
                new Tarea(1, "Revisar nóminas", "Aprobar nóminas del mes", LocalDate.of(2025, 11, 20), "Pendiente"),
                new Tarea(2, "Reunión con proveedores", "Negociar nuevo contrato", LocalDate.of(2025, 11, 25), "En curso"),
                new Tarea(3, "Auditoría interna", "Revisar cuentas 2025", LocalDate.of(2025, 12, 1), "Pendiente"),
                new Tarea(4, "Formación equipo", "Curso de liderazgo", LocalDate.now(), "Completada")
        );

        TableView<Tarea> tabla = new TableView<>();
        tabla.setItems(listaTareas);
        tabla.setPrefHeight(600);

        TableColumn<Tarea, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Tarea, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colTitulo.setPrefWidth(250);

        TableColumn<Tarea, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDesc.setPrefWidth(400);

        TableColumn<Tarea, LocalDate> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colFecha.setPrefWidth(120);

        TableColumn<Tarea, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(120);
        colEstado.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Completada" -> setStyle("-fx-text-fill: #00FF00; -fx-font-weight: bold;");
                        case "En curso" -> setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
                        case "Pendiente" -> setStyle("-fx-text-fill: #FF5555;");
                    }
                }
            }
        });

        tabla.getColumns().addAll(colId, colTitulo, colDesc, colFecha, colEstado);

        Label titulo = new Label("MIS TAREAS");
        titulo.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 32px; -fx-font-weight: bold; -fx-padding: 20;");

        VBox contenido = new VBox(10, titulo, tabla);
        contenido.setStyle("-fx-padding: 20;");

        contentArea.setContent(contenido);
    }

    private void agregarBoton(String icono, String texto, Runnable accion) {
        FontIcon icon = new FontIcon("fas-" + icono + ":26");
        icon.setIconColor(Color.web("#FFD700"));

        Button btn = new Button(texto, icon);
        btn.setPrefSize(80, 80);
        btn.setContentDisplay(ContentDisplay.TOP);
        btn.setAlignment(Pos.CENTER);

        btn.setStyle("-fx-background-color: #111111; -fx-text-fill: #FFD700; -fx-font-size: 10px; -fx-background-radius: 12;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #FFD700; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 12;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #111111; -fx-text-fill: #FFD700; -fx-font-size: 10px; -fx-background-radius: 12;"));

        btn.setOnAction(e -> accion.run());
        sidebar.getChildren().add(btn);
    }

    private void mostrarMensaje(String msg) {
        Label label = new Label(msg);
        label.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 40px; -fx-padding: 100;");
        contentArea.setContent(label);
    }

    private void cerrarSesion() {
        Main.cerrarSesion();
        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml"))));
            stage.setWidth(500);
            stage.setHeight(700);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}