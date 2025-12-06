// src/main/java/jjn/dashboardElements/AsignadasTableView.java
package jjn.dashboardElements;

import javafx.stage.Stage;
import jjn.modelos.Tarea;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class AsignadasTableView extends TableView<Tarea> {
    public AsignadasTableView(ObservableList<Tarea> datos) {
        this(datos, null);
    }

    public AsignadasTableView(ObservableList<Tarea> datos, Runnable onNuevaTarea) {
        super(datos);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        configurarColumnas(onNuevaTarea);
    }

    private void configurarColumnas(Runnable onNuevaTarea) {
        TableColumn<Tarea, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setMaxWidth(70);

        TableColumn<Tarea, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colTitulo.setPrefWidth(300);
        colTitulo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.length() > 50 ? item.substring(0, 47) + "..." : item);
            }
        });

        TableColumn<Tarea, String> colAsignado = new TableColumn<>("Asignado a");
        colAsignado.setCellValueFactory(new PropertyValueFactory<>("nombreAsignado"));
        colAsignado.setPrefWidth(180);

        TableColumn<Tarea, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(120);
        colEstado.setStyle("-fx-alignment: CENTER;");
        colEstado.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    switch (item) {
                        case "Completada" -> setStyle("-fx-text-fill: #00FF00; -fx-font-weight: bold;");
                        case "En curso"   -> setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
                        case "Pendiente"  -> setStyle("-fx-text-fill: #FF5555; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // COLUMNA ACCIONES: EDITAR
        TableColumn<Tarea, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(100);
        colAcciones.setCellFactory(tc -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");

            {
                btnEditar.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black; -fx-font-weight: bold;");
                btnEditar.setOnAction(e -> {
                    Tarea tarea = getTableView().getItems().get(getIndex());
                    new NuevaTareaDialog((Stage) getScene().getWindow(), tarea, () -> {
                        // Aquí recargarás desde servidor
                    }).showAndWait();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnEditar);
            }
        });

        getColumns().setAll(colId, colTitulo, colAsignado, colEstado, colAcciones);
    }
}