package jjn;

import jjn.modelos.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class LoginController {

    @FXML private TextField tfUsuario;
    @FXML private PasswordField pfPassword;
    @FXML private Label lblMensaje;

    private ConexionCliente conexion;

    @FXML
    private void initialize() {
        lblMensaje.setText("");
    }

    @FXML
    private void handleLogin() {
        String identificador = tfUsuario.getText().trim();
        String password = pfPassword.getText();

        if (identificador.isEmpty() || password.isEmpty()) {
            mostrarError("Completa todos los campos");
            return;
        }

        lblMensaje.setText("Conectando al Imperio...");
        lblMensaje.setStyle("-fx-text-fill: #FFD700;");

        new Thread(() -> {
            try {
                conexion = new ConexionCliente("localhost", 5000);
                conexion.enviar("LOGIN"+ Main.SEP + identificador + Main.SEP + password);

                String respuesta = conexion.leerRespuestaCompleta();

                Platform.runLater(() -> {
                    System.out.println(respuesta+"esto");
                    if (respuesta.startsWith("LOGIN_OK")) {
                        crearUsuarioYEntrar(respuesta);
                    } else if (respuesta.startsWith("LOGIN_ERROR")) {
                        String mensaje = respuesta.split("@Tr&m", 2)[1];
                        mostrarError(mensaje.isEmpty() ? "Acceso denegado" : mensaje);
                        conexion.cerrar();
                    } else {
                        mostrarError("Respuesta desconocida del servidor");
                        conexion.cerrar();
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() -> mostrarError("No se pudo conectar al servidor"));
            } catch (Exception e) {
                Platform.runLater(() -> mostrarError("Error inesperado"));
                e.printStackTrace();
            }
        }).start();
    }

    private void crearUsuarioYEntrar(String respuesta) {
        try {
            System.out.println(respuesta);
            String[] datos = respuesta.split(Main.SEP);
            Usuario usuario = new Usuario(
                    Integer.parseInt(datos[1]),                                       // id
                    datos[2],                                                         // nombre
                    datos[3],                                                         // mail
                    datos[4],                                                         // rol
                    safeParseInt(datos[5]),                                           // idDepartamento → ahora seguro
                    datos[6],                                                         // nombreDepartamento
                    LocalDate.parse(datos[7]),                                        // fechaAlta
                    datos.length > 9 ? datos[8] : ""                                  // direccion
            );

            // Guardamos en Main (accesible desde cualquier parte del cliente)
            Main.setUsuarioActual(usuario);
            Main.setConexion(conexion);

            // Abrimos el Dashboard
            Stage stage = (Stage) tfUsuario.getScene().getWindow();  // ← este sí existe

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            DashboardController dashboard = loader.getController();
            dashboard.iniciarSesion(usuario, conexion, stage);  // ← PASA EL STAGE

            stage.setScene(scene);
            stage.setTitle(usuario.getNombre().toUpperCase());
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al cargar el sistema");
        }
    }

    private void mostrarError(String texto) {
        lblMensaje.setText(texto);
        lblMensaje.setStyle("-fx-text-fill: #FF4444;");
    }
    private Integer safeParseInt(String valor) {
        if (valor == null || valor.isEmpty() || "null".equalsIgnoreCase(valor.trim())) {
            return 0;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}