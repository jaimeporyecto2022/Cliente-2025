package jjn.dashboardElements;

import jjn.modelos.Tarea;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;

public class TareasTableView extends TableView<Tarea> {

    public TareasTableView(ObservableList<Tarea> datos) {
        super(datos);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Tarea, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setMaxWidth(70);

        TableColumn<Tarea, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colTitulo.setPrefWidth(500);
        colTitulo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    String texto = item.length() > 50 ? item.substring(0, 47) + "..." : item;
                    setText(texto);
                    setTooltip(item.length() > 50 ? new Tooltip(item) : null);
                }
            }
        });

        TableColumn<Tarea, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        TableColumn<Tarea, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
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

        getColumns().setAll(colId, colTitulo, colDesc, colEstado);
    }
}