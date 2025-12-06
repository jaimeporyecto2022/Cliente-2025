// jjn/dashboardElements/NuevaTareaDialog.java
package jjn.dashboardElements;

import jjn.modelos.Tarea;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NuevaTareaDialog extends Stage {

    // Constructor para CREAR nueva tarea
    public NuevaTareaDialog(Stage owner, Runnable onCrear) {
        this(owner, null, onCrear);
    }

    // Constructor para EDITAR tarea existente
    public NuevaTareaDialog(Stage owner, Tarea tarea, Runnable onGuardar) {
        setTitle(tarea == null ? "NUEVA TAREA" : "EDITAR TAREA");
        initOwner(owner);
        setResizable(false);

        VBox form = new VBox(20);
        form.setStyle("-fx-padding: 30; -fx-background-color: #111111;");

        Label lblTitulo = new Label(tarea == null ? "CREAR NUEVA TAREA" : "EDITAR TAREA");
        lblTitulo.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField tfTitulo = new TextField(tarea != null ? tarea.getTitulo() : "");
        tfTitulo.setPromptText("Título (máx 50)");

        TextArea taDesc = new TextArea(tarea != null ? tarea.getDescripcion() : "");
        taDesc.setPromptText("Descripción");
        taDesc.setPrefRowCount(5);

        ComboBox<String> cbEmpleado = new ComboBox<>();
        cbEmpleado.getItems().addAll("Juan Pérez", "María García", "Carlos López", "Ana Martínez");
        cbEmpleado.setValue(tarea != null ? tarea.getNombreAsignado() : null);
        cbEmpleado.setPromptText("Asignar a...");

        Button btnGuardar = new Button(tarea == null ? "CREAR TAREA" : "GUARDAR CAMBIOS");
        btnGuardar.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12 40; -fx-background-radius: 30;");
        btnGuardar.setOnAction(e -> {
            if (tfTitulo.getText().trim().isEmpty() || cbEmpleado.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Completa título y empleado", ButtonType.OK).show();
            } else {
                onGuardar.run();
                close();
            }
        });

        form.getChildren().addAll(lblTitulo, tfTitulo, taDesc, cbEmpleado, btnGuardar);
        setScene(new Scene(form, 500, 500));
        showAndWait();
    }
}