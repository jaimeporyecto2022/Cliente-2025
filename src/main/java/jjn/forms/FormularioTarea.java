package jjn.forms;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jjn.Cliente;
import jjn.modelos.Tarea;
import jjn.modelos.Usuario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FormularioTarea {

    private final Stage stage;
    private final boolean esUpdate;
    private final Tarea tareaOriginal;

    private TextField txtTitulo;
    private TextArea txtDescripcion;
    private ComboBox<Usuario> cbUsuarios;
    private ObservableList<Usuario> usuariosOriginales;

    private DatePicker dpInicio;
    private DatePicker dpFin;
    private ComboBox<String> cbEstado;
    private Runnable onCloseCallback;

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

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

        // ===== COMBO BUSCABLE =====
        cbUsuarios = new ComboBox<>();
        cbUsuarios.setPromptText("Buscar usuario...");
        cbUsuarios.setEditable(true);

        // Mostrar nombre
        cbUsuarios.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });

        cbUsuarios.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });

        // Estilo editor
        Platform.runLater(() -> {
            TextField editor = cbUsuarios.getEditor();
            editor.getStyleClass().add("combo-editor");
        });

        // ===== FILTRO =====
        cbUsuarios.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (usuariosOriginales == null) return;

            String filtro = newVal.toLowerCase();

            List<Usuario> filtrados = usuariosOriginales.stream()
                    .filter(u -> u.getNombre().toLowerCase().contains(filtro))
                    .collect(Collectors.toList());

            cbUsuarios.getItems().setAll(filtrados);
            cbUsuarios.show();
        });

        dpInicio = new DatePicker();
        dpFin = new DatePicker();

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("pendiente", "completado", "imposible");

        // Cargar usuarios
        cargarUsuariosEnSegundoPlano();

        // ===== BOTONES =====
        Button btnGuardar = new Button(esUpdate ? "Guardar" : "Crear");
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

        stage.setOnHidden(e -> {
            if (onCloseCallback != null) onCloseCallback.run();
        });
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
    // CARGAR USUARIOS
    // ============================================================
    private void cargarUsuariosEnSegundoPlano() {
        new Thread(() -> {
            try {
                var con = Cliente.getConexion();
                var user = Cliente.getUsuarioActual();

                if (user.getRol().equalsIgnoreCase("admin")) {
                    con.enviar("USUARIOS_SIMPLE");
                } else {
                    con.enviar("EMPLEADOS_DEPARTAMENTOS_SIMPLE" + Cliente.SEP + user.getIdDepartamento());
                }

                String resp = con.leerRespuestaCompleta();

                List<Usuario> lista = parseUsuarios(resp);

                Platform.runLater(() -> {
                    usuariosOriginales = FXCollections.observableArrayList(lista);
                    cbUsuarios.setItems(usuariosOriginales);

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
    // PARSEAR USUARIOS
    // ============================================================
    private List<Usuario> parseUsuarios(String respuesta) {
        List<Usuario> lista = new ArrayList<>();

        if (respuesta == null || respuesta.isEmpty()) return lista;

        String[] filas = respuesta.split(Cliente.JUMP);

        for (String f : filas) {
            if (f.trim().isEmpty()) continue;

            String[] c = f.split(Cliente.SEP);

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
    // GUARDAR (FIX IMPORTANTE AQUÍ)
    // ============================================================
    private void guardar() {

        String titulo = txtTitulo.getText().trim();
        String desc = txtDescripcion.getText().trim();

        Object valor = cbUsuarios.getValue();
        Usuario usuario = null;

        if (valor instanceof Usuario u) {
            usuario = u;
        } else if (valor instanceof String texto) {
            usuario = usuariosOriginales.stream()
                    .filter(u -> u.getNombre().equalsIgnoreCase(texto))
                    .findFirst()
                    .orElse(null);
        }

        LocalDate inicio = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();
        String estado = cbEstado.getValue();

        if (titulo.isEmpty() || usuario == null || estado == null) {
            new Alert(Alert.AlertType.WARNING, "Completa los campos obligatorios").show();
            return;
        }

        var con = Cliente.getConexion();
        Usuario creador = Cliente.getUsuarioActual();

        if (esUpdate) {
            con.enviar("UPDATE_TAREA"
                    + Cliente.SEP + tareaOriginal.getId()
                    + Cliente.SEP + creador.getId()
                    + Cliente.SEP + usuario.getId()
                    + Cliente.SEP + desc
                    + Cliente.SEP + inicio
                    + Cliente.SEP + fin
                    + Cliente.SEP + estado
                    + Cliente.SEP + titulo
            );
        } else {
            con.enviar("INSERT_TAREA"
                    + Cliente.SEP + creador.getId()
                    + Cliente.SEP + usuario.getId()
                    + Cliente.SEP + desc
                    + Cliente.SEP + inicio
                    + Cliente.SEP + fin
                    + Cliente.SEP + estado
                    + Cliente.SEP + titulo
            );
        }

        stage.close();
    }
}