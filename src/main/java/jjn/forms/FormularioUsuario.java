package jjn.forms;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jjn.Cliente;
import jjn.modelos.Usuario;

import java.util.ArrayList;
import java.util.List;

public class FormularioUsuario {

    private final Stage stage;
    private final boolean esUpdate;
    private final Usuario usuarioOriginal;

    // UI
    private TextField txtNombre;
    private TextField txtMail;
    private PasswordField txtPassword;
    private ComboBox<String> cbRol;
    private ComboBox<String> cbDepartamento;
    private TextField txtDireccion;

    private Runnable onCloseCallback;

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public FormularioUsuario(String accion, Usuario usuario) {

        this.esUpdate = accion.equalsIgnoreCase("update");
        this.usuarioOriginal = usuario;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(esUpdate ? "Editar usuario" : "Nuevo usuario");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0f0f0f;");

        // ================= CAMPOS =================

        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo");

        txtMail = new TextField();
        txtMail.setPromptText("Correo electrónico");

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña (solo en creación)");

        cbRol = new ComboBox<>();
        cbRol.getItems().addAll("admin", "jefe", "empleado");
        cbRol.setPromptText("Rol");

        cbDepartamento = new ComboBox<>();
        cbDepartamento.setPromptText("Departamento");

        txtDireccion = new TextField();
        txtDireccion.setPromptText("Dirección del usuario");

        cargarDepartamentosEnSegundoPlano();

        // ================= BOTONES =================

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setStyle("-fx-background-color:#8b0000; -fx-text-fill:white;");
        btnEliminar.setOnAction(e -> eliminarUsuario());

        HBox botones;

        if (esUpdate) {
            botones = new HBox(15, btnEliminar, btnGuardar, btnCancelar);
        } else {
            botones = new HBox(15, btnGuardar, btnCancelar);
        }

        botones.setAlignment(Pos.CENTER_RIGHT);

        // ================= LAYOUT =================

        root.getChildren().addAll(
                new Label("Nombre:"), txtNombre,
                new Label("Mail:"), txtMail,
                new Label("Contraseña:"), txtPassword,
                new Label("Rol:"), cbRol,
                new Label("Departamento:"), cbDepartamento,
                new Label("Dirección:"), txtDireccion,
                botones
        );

        root.getStylesheets().add(
                getClass().getResource("/css/formEstilo.css").toExternalForm()
        );

        if (esUpdate) {
            cargarDatosExistentes();
        }

        stage.setScene(new Scene(root, 500, 650));
    }

    public void mostrar() {
        stage.showAndWait();
    }

    // =====================================================
    // CARGAR DATOS EXISTENTES
    // =====================================================

    private void cargarDatosExistentes() {

        txtNombre.setText(usuarioOriginal.getNombre());
        txtMail.setText(usuarioOriginal.getMail());
        cbRol.setValue(usuarioOriginal.getRol());
        cbDepartamento.setValue(usuarioOriginal.getNombreDepartamento());
        txtDireccion.setText(usuarioOriginal.getDireccion());

        txtPassword.setDisable(true);
        txtPassword.setPromptText("No modificable");
    }

    // =====================================================
    // CARGAR DEPARTAMENTOS
    // =====================================================

    private void cargarDepartamentosEnSegundoPlano() {

        new Thread(() -> {

            try {

                var con = Cliente.getConexion();
                con.enviar("LISTAR_DEPARTAMENTOS_SIMPLE");

                String resp = con.leerRespuestaCompleta();

                List<String> deps = new ArrayList<>();

                for (String linea : resp.split(Cliente.JUMP)) {

                    if (linea.trim().isEmpty()) continue;

                    deps.add(linea.trim());
                }

                Platform.runLater(() ->
                        cbDepartamento.getItems().setAll(deps)
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    // =====================================================
    // GUARDAR
    // =====================================================

    private void guardar() {

        String nombre = txtNombre.getText().trim();
        String mail = txtMail.getText().trim();
        String rol = cbRol.getValue();
        String departamento = cbDepartamento.getValue();
        String direccion = txtDireccion.getText().trim();

        if (nombre.isEmpty() || mail.isEmpty() || rol == null) {

            new Alert(Alert.AlertType.WARNING,
                    "Por favor completa todos los campos obligatorios"
            ).show();

            return;
        }

        var con = Cliente.getConexion();

        if (esUpdate) {

            con.enviar("UPDATE_USUARIO"
                    + Cliente.SEP + usuarioOriginal.getId()
                    + Cliente.SEP + nombre
                    + Cliente.SEP + mail
                    + Cliente.SEP + rol
                    + Cliente.SEP + departamento
                    + Cliente.SEP + direccion
            );

        } else {

            String pass = txtPassword.getText().trim();

            if (pass.isEmpty()) {

                new Alert(Alert.AlertType.WARNING,
                        "Debes ingresar una contraseña"
                ).show();

                return;
            }

            con.enviar("CREAR_USUARIO"
                    + Cliente.SEP + nombre
                    + Cliente.SEP + mail
                    + Cliente.SEP + hashPassword(pass)
                    + Cliente.SEP + rol
                    + Cliente.SEP + departamento
                    + Cliente.SEP + direccion
            );
        }

        if (onCloseCallback != null) {
            onCloseCallback.run();
        }

        stage.close();
    }

    // =====================================================
    // ELIMINAR USUARIO
    // =====================================================

    private void eliminarUsuario() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Eliminar usuario");
        confirm.setHeaderText("Confirmar eliminación");
        confirm.setContentText("¿Seguro que deseas eliminar este usuario?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {

            var con = Cliente.getConexion();

            con.enviar("ELIMINAR_USUARIO"
                    + Cliente.SEP + usuarioOriginal.getId()
            );
            con.leerRespuestaCompleta();

            if (onCloseCallback != null) {
                onCloseCallback.run();
            }

            stage.close();

        } catch (Exception e) {

            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Error al eliminar usuario"
            ).show();
        }
    }

    // =====================================================
    // HASH PASSWORD
    // =====================================================

    private String hashPassword(String password) {

        return org.mindrot.jbcrypt.BCrypt.hashpw(
                password,
                org.mindrot.jbcrypt.BCrypt.gensalt()
        );
    }
}