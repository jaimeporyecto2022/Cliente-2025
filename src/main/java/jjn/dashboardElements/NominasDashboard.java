package jjn.dashboardElements;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jjn.Cliente;
import jjn.forms.FormularioNomina;
import jjn.modelos.Nomina;
import jjn.modelos.Usuario;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;

public class NominasDashboard extends VBox {

    private final ObservableList<Nomina> nominas = FXCollections.observableArrayList();
    private TableView<Nomina> tabla;
    private Usuario usuario;

    public NominasDashboard(Usuario usuario) {
        Label titulo = new Label("Nóminas");
        titulo.setStyle("-fx-font-size: 32px;");
        this.usuario = usuario;

        Button btnNuevaNomina = crearBotonNuevaNomina();

        HBox barraSuperior = new HBox(titulo, btnNuevaNomina);
        barraSuperior.setSpacing(20);
        barraSuperior.setAlignment(Pos.CENTER_RIGHT);
        barraSuperior.setStyle("-fx-padding: 10 15 10 15;");

        HBox.setHgrow(titulo, Priority.ALWAYS);

        tabla = crearTabla();
        cargarNominasDesdeServidor();

        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: #0A0A0A; -fx-background-color: transparent;");

        getChildren().addAll(barraSuperior, scroll);
    }

    private Button crearBotonNuevaNomina() {
        Button btn = new Button(" Nómina");

        FontIcon iconoPlus = new FontIcon("fas-plus");
        iconoPlus.setIconColor(Color.WHITE);
        iconoPlus.setIconSize(24);
        btn.setGraphic(iconoPlus);

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
            FormularioNomina f = new FormularioNomina("insert", null, usuario);
            f.setOnCloseCallback(this::cargarNominasDesdeServidor);
            f.mostrar();
        });

        return btn;
    }

    private TableView<Nomina> crearTabla() {
        TableView<Nomina> table = new TableView<>();
        table.setItems(nominas);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        tableColumnFactory(table);
        return table;
    }

    private void tableColumnFactory(TableView<Nomina> table) {

        TableColumn<Nomina, String> colImporte = new TableColumn<>("Importe");
        colImporte.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(String.format("%.2f €", c.getValue().getImporte()))
        );

        TableColumn<Nomina, String> colFecha = new TableColumn<>("Fecha");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colFecha.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFecha() != null ? c.getValue().getFecha().format(fmt) : "—"
                )
        );

        TableColumn<Nomina, String> colConcepto = new TableColumn<>("Concepto");
        colConcepto.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getConcepto())
        );

        TableColumn<Nomina, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTipo())
        );

        TableColumn<Nomina, Void> colEditar = new TableColumn<>("");
        colEditar.setPrefWidth(30);
        colEditar.setCellFactory(tc -> crearBotonIcono("fas-edit", "#00CCFF", this::editarNomina));

        // 🔥 NUEVA COLUMNA DUPLICAR
        TableColumn<Nomina, Void> colDuplicar = new TableColumn<>("");
        colDuplicar.setPrefWidth(30);
        colDuplicar.setCellFactory(tc -> crearBotonIcono("fas-copy", "#FFD700", this::duplicarNomina));

        table.getColumns().addAll(colImporte, colFecha, colConcepto, colTipo, colEditar, colDuplicar);
    }

    private TableCell<Nomina, Void> crearBotonIcono(String icono, String color, java.util.function.Consumer<Nomina> accion) {
        return new TableCell<>() {
            private final Button btn = new Button();

            {
                FontIcon fIcon = new FontIcon(icono);
                fIcon.setIconColor(Color.web(color));
                fIcon.setIconSize(22);
                btn.setGraphic(fIcon);
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

    private void cargarNominasDesdeServidor() {
        new Thread(() -> {
            try {
                var conexion = Cliente.getConexion();
                conexion.enviar("NOMINAS_USUARIO" + Cliente.SEP + usuario.getId());

                String respuesta = conexion.leerRespuestaCompleta();
                Platform.runLater(() -> nominas.clear());

                String[] lineas = respuesta.split(Cliente.JUMP, -1);
                for (String linea : lineas) {
                    if (linea.trim().isEmpty()) continue;

                    String[] campos = linea.split(Cliente.SEP, -1);
                    if (campos.length < 6) continue;

                    try {
                        Nomina n = new Nomina();
                        n.setId(Integer.parseInt(campos[0]));
                        n.setImporte(Double.parseDouble(campos[1]));
                        n.setFecha(campos[2].isEmpty() ? null : java.time.LocalDate.parse(campos[2]));
                        n.setConcepto(campos[3]);
                        n.setTipo(campos[4]);
                        n.setIdUsuario(Integer.parseInt(campos[5]));

                        Platform.runLater(() -> nominas.add(n));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void editarNomina(Nomina nomina) {
        FormularioNomina f = new FormularioNomina("update", nomina, usuario);
        f.setOnCloseCallback(this::cargarNominasDesdeServidor);
        f.mostrar();
    }

    // ======================================================
    // ================= DUPLICAR NOMINA ====================
    // ======================================================

    private void duplicarNomina(Nomina nomina) {

        new Thread(() -> {
            try {
                var con = Cliente.getConexion();

                con.enviar("INSERT_NOMINA"
                        + Cliente.SEP + usuario.getId()
                        + Cliente.SEP + nomina.getImporte()
                        + Cliente.SEP + nomina.getConcepto()
                        + Cliente.SEP + nomina.getTipo()
                );

                // refrescar tabla
                Platform.runLater(this::cargarNominasDesdeServidor);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}