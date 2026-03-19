package jjn.dashboardElements;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jjn.Cliente;
import jjn.forms.FormularioReporte;
import jjn.modelos.Reporte;
import jjn.modelos.Tarea;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportesDashboard extends VBox {

    private final ObservableList<Reporte> reportes = FXCollections.observableArrayList();
    private TableView<Reporte> tabla;
    private final Tarea tarea;
    private boolean hideit;

    public ReportesDashboard(Tarea tarea, boolean hideit) {
        this.hideit = hideit;
        this.tarea = tarea;

        Label titulo = new Label("Reportes Fecha Limite: " + tarea.getFechaFin());
        titulo.setStyle("-fx-font-size: 32px;");

        Button btnNuevoReporte = crearBotonNuevoReporte();

        HBox barraSuperior = hideit
                ? new HBox(titulo)
                : new HBox(titulo, btnNuevoReporte);

        barraSuperior.setSpacing(20);
        barraSuperior.setAlignment(Pos.CENTER_RIGHT);
        barraSuperior.setStyle("-fx-padding: 10 15 10 15;");
        HBox.setHgrow(titulo, Priority.ALWAYS);

        tabla = crearTabla();
        cargarReportesDesdeServidor();

        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: #0A0A0A; -fx-background-color: transparent;");

        getChildren().addAll(barraSuperior, scroll);
    }

    private Button crearBotonNuevoReporte() {
        Button btn = new Button(" Reporte");

        FontIcon iconoPlus = new FontIcon("fas-plus");
        iconoPlus.setIconColor(Color.WHITE);
        iconoPlus.setIconSize(24);
        btn.setGraphic(iconoPlus);

        //  ESTILO ORIGINAL RESTAURADO
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

        btn.setOnMouseEntered(e -> btn.setStyle(estiloHover));
        btn.setOnMouseExited(e -> btn.setStyle(estiloBase));

        btn.setOnAction(e -> {
            try {
                FormularioReporte f = new FormularioReporte("insert", null, tarea);

                f.mostrar();

                //  refrescar después de cerrar
                cargarReportesDesdeServidor();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return btn;
    }

    private TableView<Reporte> crearTabla() {
        TableView<Reporte> table = new TableView<>();
        table.setItems(reportes);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        var css = getClass().getResource("/css/styles.css");
        if (css != null) {
            table.getStylesheets().add(css.toExternalForm());
        }

        tableColumnFactory(table);
        return table;
    }

    private void tableColumnFactory(TableView<Reporte> table) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        TableColumn<Reporte, String> colFecha = new TableColumn<>("Creación");
        colFecha.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFechacreacion().format(fmt)));

        TableColumn<Reporte, String> colInfo = new TableColumn<>("Descripción");
        colInfo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getInformacion() != null
                                ? (c.getValue().getInformacion().length() > 20
                                ? c.getValue().getInformacion().substring(0, 20) + "..."
                                : c.getValue().getInformacion())
                                : ""
                ));

        TableColumn<Reporte, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getNombreUsuario()));

        TableColumn<Reporte, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getEstado()));

        // 🔍 BOTÓN VER (LUPA)
        TableColumn<Reporte, Void> colVer = new TableColumn<>("");
        colVer.setPrefWidth(30);
        colVer.setCellFactory(tc ->
                crearBotonIcono("fas-search", "#FFD700", this::verReporte));

        table.getColumns().addAll(colFecha, colInfo, colUsuario, colEstado, colVer);
    }

    private TableCell<Reporte, Void> crearBotonIcono(String icono, String color,
                                                     java.util.function.Consumer<Reporte> accion) {

        return new TableCell<>() {
            private final Button btn = new Button();

            {
                FontIcon fIcon = new FontIcon(icono);
                fIcon.setIconColor(Color.web(color));
                fIcon.setIconSize(22);
                btn.setGraphic(fIcon);

                //  ESTILO ORIGINAL
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                btn.setOnMouseEntered(e ->
                        btn.setStyle("-fx-background-color: #FFFFFF11; -fx-background-radius: 8;"));
                btn.setOnMouseExited(e ->
                        btn.setStyle("-fx-background-color: transparent;"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    btn.setOnAction(e ->
                            accion.accept(getTableView().getItems().get(getIndex())));
                    setGraphic(btn);
                    setAlignment(Pos.CENTER);
                }
            }
        };
    }

    private void verReporte(Reporte r) {
        FormularioReporte f = new FormularioReporte("view", r, tarea);

        f.mostrar();

        // refrescar al volver
        cargarReportesDesdeServidor();
    }

    private void cargarReportesDesdeServidor() {
        new Thread(() -> {
            try {
                var con = Cliente.getConexion();
                con.enviar("REPORTES" + Cliente.SEP + tarea.getId());

                String respuesta = con.leerRespuestaCompleta();

                Platform.runLater(reportes::clear);

                for (String linea : respuesta.split(Cliente.JUMP)) {
                    if (linea.trim().isEmpty()) continue;

                    String[] c = linea.split(Cliente.SEP);

                    Reporte r = new Reporte();
                    r.setId(Integer.parseInt(c[0]));
                    r.setFechacreacion(LocalDate.parse(c[1]));
                    r.setInformacion(c[2]);
                    r.setEstado(c[3]);
                    r.setIdUsuarioReporte(Integer.parseInt(c[4]));
                    r.setNombreUsuario(c[5]);

                    Platform.runLater(() -> reportes.add(r));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}