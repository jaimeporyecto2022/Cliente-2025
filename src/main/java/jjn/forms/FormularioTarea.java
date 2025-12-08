package jjn.forms;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jjn.ConexionCliente;
import jjn.Main;
import jjn.modelos.Tarea;
import jjn.modelos.Usuario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class FormularioTarea {

    private final Stage stage;
    private final boolean esUpdate;
    private final Tarea tareaOriginal;

    // UI
    private TextField txtTitulo;
    private TextArea txtDescripcion;
    private ComboBox<Usuario> cbUsuarios;
    private DatePicker dpInicio;
    private DatePicker dpFin;
    private ComboBox<String> cbEstado;

    public FormularioTarea(String accion, Tarea tarea) {

        this.esUpdate = accion.equalsIgnoreCase("update");
        this.tareaOriginal = tarea;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(esUpdate ? "Editar tarea" : "Nueva tarea");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0f0f0f;");

        // ===== CAMPOS =====
        txtTitulo = new TextField();
        txtTitulo.setPromptText("Título de la tarea");

        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Descripción...");
        txtDescripcion.setPrefRowCount(4);
        txtDescripcion.setWrapText(true);

        cbUsuarios = new ComboBox<>();
        cbUsuarios.setPromptText("Asignar a...");

        dpInicio = new DatePicker();
        dpInicio.setPromptText("Fecha inicio");

        dpFin = new DatePicker();
        dpFin.setPromptText("Fecha fin");

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("pendiente", "completado", "imposible");
        cbEstado.setPromptText("Estado");


        // === CARGAR USUARIOS CON NUEVO MÉTODO SIMPLE ===
        cargarUsuariosEnSegundoPlano();

        // ===== BOTONES =====
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        HBox botones = new HBox(15, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                new Label("Título:"), txtTitulo,
                new Label("Descripción:"), txtDescripcion,
                new Label("Asignar a:"), cbUsuarios,
                new Label("Estado:"), cbEstado,
                new Label("Fecha inicio:"), dpInicio,
                new Label("Fecha fin:"), dpFin,
                botones
        );
        root.getStylesheets().add(
                getClass().getResource("/css/formEstilo.css").toExternalForm()
        );

        if (esUpdate) {
            cargarDatosExistentes();
        }

        stage.setScene(new Scene(root, 600, 700));
    }

    public void mostrar() {
        stage.showAndWait();
    }

    private void cargarDatosExistentes() {
        txtTitulo.setText(tareaOriginal.getTitulo());
        txtDescripcion.setText(tareaOriginal.getDescripcion());
        dpInicio.setValue(tareaOriginal.getFechaInicio());
        dpFin.setValue(tareaOriginal.getFechaFin());
        cbEstado.setValue(tareaOriginal.getEstado());
    }

    // ============================================================
    // ========== CARGAR USUARIOS DESDE SERVIDOR ===================
    // ============================================================
    private void cargarUsuariosEnSegundoPlano() {
        new Thread(() -> {
            try {
                var con = Main.getConexion();
                var user = Main.getUsuarioActual();

                // === NUEVO USO DEL MÉTODO SIMPLE ===
                if (user.getRol().equalsIgnoreCase("admin")) {
                    con.enviar("USUARIOS_SIMPLE");System.out.println("entra en simple");
                } else {
                    con.enviar("USUARIOS_DEP_SIMPLE" + Main.SEP + user.getIdDepartamento());
                }

                String resp = con.leerRespuestaCompleta();
                System.out.println("respuestas"+ resp);

                List<Usuario> lista = parseUsuarios(resp);

                Platform.runLater(() -> {
                    cbUsuarios.getItems().setAll(lista);

                    if (esUpdate) {
                        for (Usuario u : lista) {
                            if (u.getId() == tareaOriginal.getIdAsignado()) {
                                cbUsuarios.setValue(u);
                                break;
                            }
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ============================================================
    // ========== PARSEAR RESPUESTA DE USUARIOS ====================
    // ============================================================
    private List<Usuario> parseUsuarios(String respuesta) {
        List<Usuario> lista = new ArrayList<>();

        if (respuesta == null || respuesta.isEmpty()) {
            System.out.println("⚠ No llegaron usuarios");
            return lista;
        }

        String[] filas = respuesta.split(Main.JUMP);

        for (String f : filas) {
            if (f.trim().isEmpty()) continue;

            String[] c = f.split(Main.SEP);

            if (c.length >= 2) {
                try {
                    Usuario u = new Usuario();
                    u.setId(Integer.parseInt(c[0]));
                    u.setNombre(c[1]);

                    lista.add(u);

                } catch (Exception ignored) {}
            }
        }

        return lista;
    }

    // ============================================================
    // ====================== GUARDAR ==============================
    // ============================================================
    private void guardar() {
        String titulo = txtTitulo.getText().trim();
        String desc = txtDescripcion.getText().trim();
        Usuario usuario = cbUsuarios.getValue();
        LocalDate inicio = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();
        String estado = cbEstado.getValue();


        if (titulo.isEmpty() || usuario == null || estado == null) {
            new Alert(Alert.AlertType.WARNING, "Completa los campos obligatorios").show();
            return;
        }

        jjn.ConexionCliente con = Main.getConexion();
        Usuario creador = Main.getUsuarioActual();

        if (esUpdate) {
            con.enviar("UPDATE_TAREA"
                    + Main.SEP + this.tareaOriginal.getId()
                    + Main.SEP + creador.getId()
                    + Main.SEP + usuario.getId()
                    + Main.SEP + desc
                    + Main.SEP + inicio
                    + Main.SEP + fin
                    + Main.SEP + estado
                    + Main.SEP + titulo
            );
        } else {
            con.enviar("INSERT_TAREA"
                    + Main.SEP + creador.getId()
                    + Main.SEP + usuario.getId()
                    + Main.SEP + desc
                    + Main.SEP + inicio
                    + Main.SEP + fin
                    + Main.SEP + estado
                    + Main.SEP + titulo
            );
        }

        stage.close();
    }

}