package jjn.forms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jjn.Cliente;
import jjn.modelos.Nomina;
import jjn.modelos.Usuario;

public class FormularioNomina {

    private final Stage stage;
    private final boolean esUpdate;
    private final Nomina nominaOriginal;

    // ===== UI =====
    private TextField txtImporte;
    private TextField txtConcepto;
    private ComboBox<String> cbTipo;
    private Usuario usuario;

    // ===== CALLBACK PARA NOTIFICAR CIERRE =====
    private Runnable onCloseCallback;

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public FormularioNomina(String accion, Nomina nomina, Usuario usuario) {

        this.usuario = usuario;
        this.esUpdate = accion.equalsIgnoreCase("update");
        this.nominaOriginal = nomina;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(esUpdate ? "Editar nómina" : "Nueva nómina");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0f0f0f;");

        // ===== CAMPOS =====

        txtImporte = new TextField();
        txtImporte.setPromptText("Importe (€)");

        txtConcepto = new TextField();
        txtConcepto.setPromptText("Concepto");

        cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll("salario", "hora_extra", "plus", "deduccion");
        cbTipo.setPromptText("Tipo de nómina");

        // ===== BOTONES =====

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setStyle("-fx-background-color:#8b0000; -fx-text-fill:white;");
        btnEliminar.setOnAction(e -> eliminarNomina());

        HBox botones;

        if (esUpdate) {
            botones = new HBox(15, btnEliminar, btnGuardar, btnCancelar);
        } else {
            botones = new HBox(15, btnGuardar, btnCancelar);
        }

        botones.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                new Label("Importe:"), txtImporte,
                new Label("Concepto:"), txtConcepto,
                new Label("Tipo:"), cbTipo,
                botones
        );

        root.getStylesheets().add(
                getClass().getResource("/css/formEstilo.css").toExternalForm()
        );

        if (esUpdate) cargarDatosExistentes();

        Scene scene = new Scene(root, 500, 600);

        // CUANDO LA VENTANA SE CIERRE, EJECUTAR CALLBACK
        stage.setOnHidden(e -> {
            if (onCloseCallback != null) onCloseCallback.run();
        });

        stage.setScene(scene);
    }

    public void mostrar() {
        stage.showAndWait();
    }

    // ============================================================
    // CARGAR DATOS EXISTENTES
    // ============================================================

    private void cargarDatosExistentes() {
        txtImporte.setText(String.valueOf(nominaOriginal.getImporte()));
        txtConcepto.setText(nominaOriginal.getConcepto());
        cbTipo.setValue(nominaOriginal.getTipo());
    }

    // ============================================================
    // GUARDAR
    // ============================================================

    private void guardar() {

        String strImporte = txtImporte.getText().trim();
        String concepto = txtConcepto.getText().trim();
        String tipo = cbTipo.getValue();

        double importe;

        try {
            importe = Double.parseDouble(strImporte);
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Ingresa un importe válido").show();
            return;
        }

        var con = Cliente.getConexion();

        // UPDATE
        if (esUpdate) {
            con.enviar("UPDATE_NOMINA"
                    + Cliente.SEP + nominaOriginal.getId()
                    + Cliente.SEP + importe
                    + Cliente.SEP + concepto
                    + Cliente.SEP + tipo
            );
        }
        // INSERT
        else {
            con.enviar("INSERT_NOMINA"
                    + Cliente.SEP + this.usuario.getId()
                    + Cliente.SEP + importe
                    + Cliente.SEP + concepto
                    + Cliente.SEP + tipo
            );
        }

        stage.close();
    }

    // ============================================================
    // ELIMINAR NOMINA
    // ============================================================

    private void eliminarNomina() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Eliminar nómina");
        confirm.setHeaderText("Confirmar eliminación");
        confirm.setContentText("¿Seguro que deseas eliminar esta nómina?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {

            var con = Cliente.getConexion();

            con.enviar("ELIMINAR_NOMINA"
                    + Cliente.SEP + nominaOriginal.getId()
            );
            //////////////////////////////error corregido
            con.leerRespuestaCompleta();

            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
            stage.close();

        } catch (Exception e) {

            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Error al eliminar nómina"
            ).show();
        }
    }
}