package jjn.dashboardElements;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jjn.Main;
import jjn.forms.FormularioTarea;
import jjn.modelos.Tarea;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TareasUsuarioDashboard extends VBox {
    private final ObservableList<Tarea> tareas = FXCollections.observableArrayList();
    private TableView<Tarea> tabla;
    @FXML
    private ScrollPane contentArea;


    public TareasUsuarioDashboard(ScrollPane contentArea) {
        this.contentArea=contentArea;
        Label titulo = new Label("Tareas: "+Main.getUsuarioActual().getNombre());
        titulo.setStyle("-fx-font-size: 32px;");
        HBox barraSuperior = new HBox(titulo);
        barraSuperior.setSpacing(20);
        barraSuperior.setAlignment(Pos.CENTER_RIGHT);
        barraSuperior.setStyle("-fx-padding: 10 15 10 15;");

        HBox.setHgrow(titulo, Priority.ALWAYS); // empuja el botón a la derecha

        tabla = crearTabla();
        cargarTareasDesdeServidor();

        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: #0A0A0A; -fx-background-color: transparent;");

        getChildren().addAll(barraSuperior, scroll);
    }



    private Button crearBotonNuevaTarea() {
        Button btn = new Button(" Tarea");

        FontIcon iconoPlus = new FontIcon("fas-plus");
        iconoPlus.setIconColor(Color.WHITE);
        iconoPlus.setIconSize(24);
        btn.setGraphic(iconoPlus);

        // Estilo base
        String estiloBase = """
        -fx-background-color: #0d1b2f;
        -fx-text-fill: #00CCFF;
        -fx-font-weight: bold;
        -fx-font-size: 18px;
        -fx-padding: 14 32;
        -fx-background-radius: 30;
        -fx-border-color: #00CCFF;
        -fx-border-width: 2;
        -fx-border-radius: 30;
        -fx-effect: dropshadow(gaussian, #00CCFF44, 15, 0.5, 0, 0);
        """;

        String estiloHover = estiloBase.replace("#0d1b2f", "#152c52");

        btn.setStyle(estiloBase);
        btn.setCursor(javafx.scene.Cursor.HAND);

        // Hover suave
        btn.setOnMouseEntered(e -> btn.setStyle(estiloHover));
        btn.setOnMouseExited(e -> btn.setStyle(estiloBase));

        // Acción
        btn.setOnAction(e -> {
            FormularioTarea f = new FormularioTarea("insert", null);
            f.setOnCloseCallback(() -> cargarTareasDesdeServidor()); // 🔥 refrescar
            f.mostrar();
        });

        return btn;
    }
    private TableView<Tarea> crearTabla() {
        TableView<Tarea> table = new TableView<>();
        table.setItems(tareas);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        tableColumnFactory(table);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Tarea item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: #111111;");
                } else {
                    String colorFondo = switch (item.getEstado() == null ? "" : item.getEstado().toLowerCase()) {
                        case "completado" -> "#1a4d1a";
                        case "pendiente" -> "#3d3d00";
                        case "no puedo hacerlo", "imposible" -> "#660000";
                        default -> "#222222";
                    };

                }
            }
        });


        return table;
    }

    private void tableColumnFactory(TableView<Tarea> table) {
        // CABECERAS DORADAS
        TableColumn<Tarea, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitulo()));
        colTitulo.setPrefWidth(120);
        TableColumn<Tarea, String> colAsignado = new TableColumn<>("Asignado a");
        colAsignado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreAsignado()));

        TableColumn<Tarea, String> colFechas = new TableColumn<>("Fechas");
        colFechas.setCellValueFactory(c -> {
            Tarea t = c.getValue();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yy");
            StringBuilder sb = new StringBuilder();
            if (t.getFechaCreacion() != null) sb.append("Creacion: ").append(t.getFechaCreacion().format(fmt));
            if (t.getFechaInicio() != null) sb.append("\nfrom ").append(t.getFechaInicio().format(fmt));
            if (t.getFechaFin() != null) sb.append("- to ").append(t.getFechaFin().format(fmt)+"");
            return new javafx.beans.property.SimpleStringProperty(sb.toString());
        });

        TableColumn<Tarea, String> colEstado = new TableColumn<>("Estado");
        colEstado.setPrefWidth(50);
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEstado()));
        colEstado.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toUpperCase());
                    setFont(Font.font("System", FontWeight.BOLD, 13));
                    setTextFill(switch (item.toLowerCase()) {
                        case "completado" -> Color.web("#00FF88");
                        case "pendiente" -> Color.web("#FFD700");
                        case "no puedo hacerlo", "imposible" -> Color.web("#FF4444");
                        default -> Color.GRAY;
                    });
                    setStyle("-fx-background-color: #00000066; -fx-background-radius: 6px; -fx-padding: 6 16;");
                }
            }
        });
        // BOTÓN REPORTE
        TableColumn<Tarea, Void> colReporte = new TableColumn<>("");
        colReporte.setPrefWidth(30);
        colReporte.setCellFactory(tc -> crearBotonIcono("fas-file-alt", "#00CCFF", this::mostrarReporteTarea));

        table.getColumns().addAll(colTitulo, colAsignado, colFechas, colEstado, colReporte);
    }

    private TableCell<Tarea, Void> crearBotonIcono(String icono, String color, java.util.function.Consumer<Tarea> accion) {
        return new TableCell<>() {
            private final Button btn = new Button();
            {
                FontIcon icon = new FontIcon(icono);
                icon.setIconColor(Color.web(color));
                icon.setIconSize(22);
                btn.setGraphic(icon);
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #FFFFFF11; -fx-background-radius: 8;"));
                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent;"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    btn.setOnAction(e -> accion.accept(getTableView().getItems().get(getIndex())));
                    setGraphic(btn);
                    setAlignment(Pos.CENTER);
                }
            }
        };
    }

    // TU CARGA ORIGINAL RESPETADA AL 100%
    private void cargarTareasDesdeServidor() {

        new Thread(() -> {
            try {
                var conexion = Main.getConexion();
                conexion.enviar("MIS_TAREAS" + Main.SEP + Main.getUsuarioActual().getId());

                String respuesta = conexion.leerRespuestaCompleta();
                Platform.runLater(() -> tareas.clear());

                String[] lineas = respuesta.split(Main.JUMP, -1);

                for (String linea : lineas) {

                    if (linea.trim().isEmpty() || linea.equals("FIN_COMANDO"))
                        continue;

                    String[] c = linea.split(Main.SEP, -1);
                    if (c.length < 6) continue;

                    Tarea t = new Tarea();
                    t.setId(Integer.parseInt(c[0]));
                    t.setTitulo(c[1]);
                    t.setDescripcion(c[2]);
                    t.setFechaInicio("Sin fecha".equals(c[2]) ? null : LocalDate.parse(c[3]));
                    t.setFechaFin("Sin fecha".equals(c[3]) ? null : LocalDate.parse(c[4]));
                    t.setEstado(c[5]);
                    t.setNombreAsignado(c[6]);

                    Platform.runLater(() -> tareas.add(t));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }



    private void mostrarReporteTarea(Tarea tarea) {
        contentArea.setContent(null);
        contentArea.setContent(new ReportesDashboard(tarea,false));
    }
}