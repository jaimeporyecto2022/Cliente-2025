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
import jjn.modelos.Tarea;

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
    private ComboBox<UsuarioItem> cbUsuarios;
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

        cbUsuarios = new ComboBox<UsuarioItem>();
        cbUsuarios.setPromptText("Asignar a...");

        dpInicio = new DatePicker();
        dpInicio.setPromptText("Fecha inicio");

        dpFin = new DatePicker();
        dpFin.setPromptText("Fecha fin");

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("pendiente", "completado", "imposible");
        cbEstado.setPromptText("Estado");

        cargarUsuariosEnSegundoPlano();

        // ===== BOTONES =====
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        HBox botones = new HBox(15, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        // ===== AGREGAR AL LAYOUT =====
        root.getChildren().addAll(
                new Label("Título:"), txtTitulo,
                new Label("Descripción:"), txtDescripcion,
                new Label("Asignar a:"), cbUsuarios,
                new Label("Fecha inicio:"), dpInicio,
                new Label("Fecha fin:"), dpFin,
                new Label("Estado:"), cbEstado,
                botones
        );

        if (esUpdate) {
            cargarDatosExistentes();
        }

        stage.setScene(new Scene(root, 400, 600));
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


    private void cargarUsuariosEnSegundoPlano() {
        new Thread(() -> {
            try {
                var con = Main.getConexion();
                var user = Main.getUsuarioActual();

                if (user.getRol().equalsIgnoreCase("admin")) {
                    con.enviar("TODOS_USUARIOS");
                } else {
                    con.enviar("USUARIOS_DEP" + Main.SEP + user.getIdDepartamento());
                }

                String resp = con.leerRespuestaCompleta();
                System.out.println("entra en from t admin" +  resp);
                List<UsuarioItem> lista = parseUsuarios(resp);

                Platform.runLater(() -> cbUsuarios.getItems().setAll(lista));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List<UsuarioItem> parseUsuarios(String respuesta) {
        List<UsuarioItem> lista = new ArrayList<>();
        String[] filas = respuesta.split(Main.JUMP);

        for (String f : filas) {
            String[] c = f.split(Main.SEP);
            if (c.length >= 4) {
                int id = Integer.parseInt(c[0]);
                lista.add(new UsuarioItem(id, c[1]));
            }
        }
        return lista;
    }


    private void guardar() {
        String titulo = txtTitulo.getText().trim();
        String desc = txtDescripcion.getText().trim();
        UsuarioItem usuario = cbUsuarios.getValue();
        LocalDate inicio = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();
        String estado = cbEstado.getValue();

        if (titulo.isEmpty() || usuario == null || estado == null) {
            new Alert(Alert.AlertType.WARNING, "Completa los campos obligatorios").show();
            return;
        }

        var con = Main.getConexion();
        var creador = Main.getUsuarioActual();

        if (esUpdate) {
            con.enviar("UPDATE_TAREA"
                    + Main.SEP + tareaOriginal.getId()
                    + Main.SEP + titulo
                    + Main.SEP + desc
                    + Main.SEP + inicio
                    + Main.SEP + fin
                    + Main.SEP + estado
                    + Main.SEP + usuario.id
            );
        } else {
            con.enviar("INSERT_TAREA"
                    + Main.SEP + titulo
                    + Main.SEP + desc
                    + Main.SEP + creador.getId()
                    + Main.SEP + usuario.id
                    + Main.SEP + inicio
                    + Main.SEP + fin
                    + Main.SEP + estado
            );
        }

        stage.close();
    }
    private static class UsuarioItem {
        int id;
        String nombre;

        public UsuarioItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }



}
