package jjn.dashboardElements;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jjn.Main;
import jjn.modelos.Tarea;
import org.kordamp.ikonli.javafx.FontIcon;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TareasDashboard extends VBox {

    private final ObservableList<Tarea> tareas = FXCollections.observableArrayList();
    private TableView<Tarea> tabla;

    public TareasDashboard() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #1e1e1e;");

        Label titulo = new Label("Tareas que has asignado");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 28));
        titulo.setTextFill(Color.web("#FFD700"));

        tabla = crearTabla();
        cargarTareasDesdeServidor();

        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e1e1e; -fx-border-color: #333;");

        getChildren().addAll(titulo, scroll);
    }

    private TableView<Tarea> crearTabla() {
        TableView<Tarea> table = new TableView<>();
        table.setItems(tareas);

        // Esta es la línea clave que faltaba
        tableColumnFactory(table);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Tarea item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    String color = switch (item.getEstado() == null ? "" : item.getEstado().toLowerCase()) {
                        case "completado" -> "#55FF55";
                        case "pendiente" -> "#FFD700";
                        case "no puedo hacerlo", "imposible" -> "#FF5555";
                        default -> "#888888";
                    };
                    setStyle("-fx-background-color: " + color + "22; -fx-border-color: " + color + ";");
                }
            }
        });

        table.setPlaceholder(new Label("No has asignado ninguna tarea aún."));
        return table;
    }

    private void tableColumnFactory(TableView<Tarea> table) {
        TableColumn<Tarea, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitulo()));
        colTitulo.setPrefWidth(200);

        TableColumn<Tarea, String> colAsignado = new TableColumn<>("Asignado a");
        colAsignado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreAsignado()));
        colAsignado.setPrefWidth(150);

        TableColumn<Tarea, String> colFechas = new TableColumn<>("Fechas");
        colFechas.setCellValueFactory(c -> {
            Tarea t = c.getValue();
            String texto = "";
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (t.getFechaCreacion() != null) texto += "C: " + t.getFechaCreacion().format(fmt);
            if (t.getFechaInicio() != null) texto += "\nI: " + t.getFechaInicio().format(fmt);
            if (t.getFechaFin() != null) texto += "\nF: " + t.getFechaFin().format(fmt);
            return new javafx.beans.property.SimpleStringProperty(texto);
        });
        colFechas.setPrefWidth(140);

        TableColumn<Tarea, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEstado()));
        colEstado.setPrefWidth(120);
        colEstado.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toUpperCase());
                    setTextFill(switch (item.toLowerCase()) {
                        case "completado" -> Color.LIMEGREEN;
                        case "pendiente" -> Color.GOLD;
                        case "no puedo hacerlo", "imposible" -> Color.CRIMSON;
                        default -> Color.GRAY;
                    });
                    setFont(Font.font("System", FontWeight.BOLD, 12));
                }
            }
        });

        TableColumn<Tarea, Void> colAccion = new TableColumn<>("");
        colAccion.setPrefWidth(80);
        colAccion.setCellFactory(tc -> new TableCell<>() {
            private final Button btnVer = new Button();
            {
                FontIcon icon = new FontIcon("fas-search");
                icon.setIconColor(Color.web("#FFD700"));
                icon.setIconSize(20);
                btnVer.setGraphic(icon);
                btnVer.setStyle("-fx-background-color: transparent;");
                btnVer.setOnMouseEntered(e -> btnVer.setStyle("-fx-background-color: #FFD70033;"));
                btnVer.setOnMouseExited(e -> btnVer.setStyle("-fx-background-color: transparent;"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Tarea tarea = getTableView().getItems().get(getIndex());
                    btnVer.setOnAction(e -> mostrarDetallesTarea(tarea));
                    setGraphic(btnVer);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        table.getColumns().addAll(colTitulo, colAsignado, colFechas, colEstado, colAccion);
    }

    private void cargarTareasDesdeServidor() {
        new Thread(() -> {
            try {
                var conexion = Main.getConexion();
                int idCreador = Main.getUsuarioActual().getId();
                conexion.enviar("MIS_TAREAS_CREADAS" + Main.SEP + idCreador);

                String respuesta = conexion.leerRespuestaCompleta();
                tareas.clear();
                System.out.println(respuesta);
                String[] lineas = respuesta.split(Main.JUMP);
                System.out.println("linea ->" + lineas[0]);
                for (int i = 0; i < lineas.length; i++) {
                        System.out.println("if"+lineas[0]);
                        String[] campos = lineas[i].split(Main.SEP);
                        if (campos.length >= 9) {
                            System.out.println("if"+campos[0]);
                            Tarea t = new Tarea(
                                    Integer.parseInt(campos[0]),
                                    campos[1], // titulo
                                    campos[2], // descripcion
                                    LocalDate.parse(campos[3]),
                                    campos[4].isEmpty() || "null".equals(campos[4]) ? null : LocalDate.parse(campos[4]),
                                    campos[5].isEmpty() || "null".equals(campos[5]) ? null : LocalDate.parse(campos[5]),
                                    campos[6],
                                    campos[7], // nombre creador
                                    campos[8]  // nombre asignado
                            );
                            tareas.add(t);
                        }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void mostrarDetallesTarea(Tarea tarea) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle de la tarea");
        alert.setHeaderText(tarea.getTitulo());
        alert.getDialogPane().setPrefSize(600, 400);

        TextArea ta = new TextArea();
        ta.setText(
                "ID: " + tarea.getId() + "\n" +
                        "Creador: " + tarea.getNombreCreador() + "\n" +
                        "Asignado a: " + tarea.getNombreAsignado() + "\n" +
                        "Estado: " + tarea.getEstado().toUpperCase() + "\n" +
                        "Descripción:\n" + tarea.getDescripcion()
        );
        ta.setEditable(false);
        ta.setWrapText(true);
        alert.getDialogPane().setContent(ta);

        alert.showAndWait();
    }
}