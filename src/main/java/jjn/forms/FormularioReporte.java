package jjn.forms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jjn.Main;
import jjn.modelos.Reporte;
import jjn.modelos.Tarea;
import java.time.LocalDate;

public class FormularioReporte {

    private final Stage stage;
    private final boolean esUpdate;
    private final Reporte reporteOriginal;
    private Runnable onCloseCallback;

    private DatePicker dpInicio;
    private DatePicker dpFin;
    private TextArea txtInfo;
    private ComboBox<String> cbEstado;

    private final Tarea tarea;

    public FormularioReporte(String accion, Reporte reporte, Tarea tarea) {

        this.esUpdate = accion.equalsIgnoreCase("update");
        this.reporteOriginal = reporte;
        this.tarea = tarea;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(esUpdate ? "Editar reporte" : "Nuevo reporte");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0f0f0f;");

        // ===== CAMPOS =====

        txtInfo = new TextArea();
        txtInfo.setPromptText("Información del reporte");
        txtInfo.setPrefRowCount(5);

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("en_curso", "finalizada", "irrealizable", "transferir");
        cbEstado.setPromptText("Estado del reporte");

        // ===== BOTONES =====
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        HBox botones = new HBox(15, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                new Label("Información:"), txtInfo,
                new Label("Estado:"), cbEstado,
                botones
        );

        root.getStylesheets().add(
                getClass().getResource("/css/formEstilo.css").toExternalForm()
        );

        stage.setScene(new Scene(root, 500, 600));
    }

    public void mostrar() {
        stage.showAndWait();
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }



    // ============================================================
    // ========================== GUARDAR ==========================
    // ============================================================
    private void guardar() {

        LocalDate inicio = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();
        String info = txtInfo.getText().trim();
        String estado = cbEstado.getValue();

        if (inicio == null || info.isEmpty() || estado == null) {
            new Alert(Alert.AlertType.WARNING, "Completa todos los campos obligatorios").show();
            return;
        }

        var con = Main.getConexion();

        /*// UPDATE
        if (esUpdate) es siempre insert despues del cambio de la 0.8{

            con.enviar(
                    "UPDATE_REPORTE"
                            + Main.SEP + reporteOriginal.getId()
                            + Main.SEP + inicio
                            + Main.SEP + (fin == null ? "" : fin)
                            + Main.SEP + info
                            + Main.SEP + estado
            );
        }
        // INSERT
        else */

            con.enviar(
                    "INSERT_REPORTE"
                            + Main.SEP + tarea.getId()
                            + Main.SEP + inicio
                            + Main.SEP + (fin == null ? "" : fin)
                            + Main.SEP + info
                            + Main.SEP + estado
            );


        if (onCloseCallback != null) onCloseCallback.run();
        stage.close();
    }
}
