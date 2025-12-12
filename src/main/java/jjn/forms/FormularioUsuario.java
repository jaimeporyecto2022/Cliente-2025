package jjn.forms;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jjn.Main;
import jjn.modelos.Usuario;

import java.time.LocalDate;
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
    //private DatePicker dpFechaAlta;
    private TextField txtDireccion;
    private Runnable onCloseCallback;   // 🔥 NUEVO
    public void setOnCloseCallback(Runnable callback) {  // 🔥 NUEVO
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

        // ===== CAMPOS =====
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
        cargarDepartamentosEnSegundoPlano();

        /*dpFechaAlta = new DatePicker(LocalDate.now());
        dpFechaAlta.setPromptText("Fecha alta");*/

        txtDireccion = new TextField();
        txtDireccion.setPromptText("Dirección del usuario");

        // ===== BOTONES =====
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        HBox botones = new HBox(15, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                new Label("Nombre:"), txtNombre,
                new Label("Mail:"), txtMail,
                new Label("Contraseña:"), txtPassword,
                new Label("Rol:"), cbRol,
                new Label("Departamento:"), cbDepartamento,
                //new Label("Fecha de alta:"), dpFechaAlta,
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

    private void cargarDatosExistentes() {
        txtNombre.setText(usuarioOriginal.getNombre());
        txtMail.setText(usuarioOriginal.getMail());
        cbRol.setValue(usuarioOriginal.getRol());
        cbDepartamento.setValue(usuarioOriginal.getNombreDepartamento());
        //dpFechaAlta.setValue(usuarioOriginal.getFechaAlta());
        txtDireccion.setText(usuarioOriginal.getDireccion());

        // Contraseña no se muestra por seguridad
        txtPassword.setDisable(true);
        txtPassword.setPromptText("No modificable");
    }

    // ============================================================
    // ========== CARGAR DEPARTAMENTOS =================
    // ============================================================
    private void cargarDepartamentosEnSegundoPlano() {
        System.out.println("entra en departamento");
        new Thread(() -> {
            try {
                var con = Main.getConexion();
                con.enviar("LISTAR_DEPARTAMENTOS_SIMPLE");

                String resp = con.leerRespuestaCompleta();
                System.out.println("departamentos"+resp);
                List<String> deps = new ArrayList<>();
                for (String linea : resp.split(Main.JUMP)) {
                    if (linea.trim().isEmpty()) continue;
                    deps.add(linea.trim());
                }

                Platform.runLater(() -> cbDepartamento.getItems().setAll(deps));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ============================================================
    // ========================== GUARDAR ==========================
    // ============================================================
    private void guardar() {

        String nombre = txtNombre.getText().trim();
        String mail = txtMail.getText().trim();
        String rol = cbRol.getValue();
        String departamento = cbDepartamento.getValue();
        //LocalDate fechaAlta = dpFechaAlta.getValue();
        String direccion = txtDireccion.getText().trim();

        if (nombre.isEmpty() || mail.isEmpty() || rol == null) {
            new Alert(Alert.AlertType.WARNING, "Por favor completa todos los campos obligatorios").show();
            return;
        }

        var con = Main.getConexion();

        // UPDATE
        if (esUpdate) {
            con.enviar("UPDATE_USUARIO"
                    + Main.SEP + usuarioOriginal.getId()
                    + Main.SEP + nombre
                    + Main.SEP + mail
                    + Main.SEP + rol
                    + Main.SEP + departamento
                    //+ Main.SEP + fechaAlta
                    + Main.SEP + direccion
            );
        }
        // INSERT
        else {
            String pass = txtPassword.getText().trim();
            if (pass.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Debes ingresar una contraseña para el nuevo usuario").show();
                return;
            }

            con.enviar("CREAR_USUARIO"
                    + Main.SEP + nombre
                    + Main.SEP + mail
                    + Main.SEP + hashPassword(pass)
                    + Main.SEP + rol
                    + Main.SEP + departamento
                    //+ Main.SEP + fechaAlta
                    + Main.SEP + direccion
            );
        }

        stage.close();
    }
    private String hashPassword(String password) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
    }
}