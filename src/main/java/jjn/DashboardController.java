package jjn;
import javafx.scene.Node;
import jjn.dashboardElements.TareasDashboard;
import jjn.dashboardElements.UsuariosDashboard;
import jjn.modelos.Tarea;
import jjn.modelos.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
public class DashboardController {
    @FXML private BorderPane rootPane;
    @FXML private Label lblUsuarioYRol;
    @FXML private VBox sidebar;
    @FXML private ScrollPane contentArea;
    private Usuario usuario;
    private ConexionCliente conexion;

    public void iniciarSesion(Usuario usuario, ConexionCliente conexion, Stage stage) {
        this.usuario = usuario;
        this.conexion = conexion;
        String rolMayus = switch (usuario.getRol().toLowerCase()) {
            case "admin" -> "ADMINISTRADOR";
            case "jefe"  -> "JEFE";
            default      -> "EMPLEADO";
        };
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
        agregarBoton("tasks", "Tareas", () -> mostrarMensaje("REPO3RTES"));
        agregarBoton("paper-plane", "Asignar", this::mostrarTareasAsignadas);
        agregarBoton("file-invoice-dollar", "Nóminas", () -> mostrarMensaje("NÓMINAS"));
        agregarBoton("chart-bar", "Reportes", () -> mostrarMensaje("REPORTES"));

        if (usuario.esJefeOSuperior()) {
            agregarBoton("users-cog", "Usuarios", this::mostrarUsuarios);
        }
        if (usuario.esAdmin()) {
            agregarBoton("crown", "Admin", () -> mostrarMensaje("PANEL DE ADMINISTRADOR"));
        }
        agregarBoton("sign-out-alt", "Salir", this::cerrarSesion);
    }




    private void mostrarError(String msg) {
        Label lbl = new Label("ERROR: " + msg);
        lbl.setStyle("-fx-text-fill: #FF5555; -fx-font-size: 36px;");
        contentArea.setContent(new StackPane(lbl));
    }

    private void mostrarMensaje(String msg) {
        Label label = new Label(msg);
        label.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 40px; -fx-padding: 100;");
        contentArea.setContent(new StackPane(label));
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
    private void mostrarTareasAsignadas() {
        contentArea.setContent(null);
        contentArea.setContent(new TareasDashboard(contentArea));
    }
    private void mostrarUsuarios() {
        contentArea.setContent(null);
        contentArea.setContent(new UsuariosDashboard(contentArea));
    }

}