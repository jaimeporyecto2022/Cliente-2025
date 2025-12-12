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
import jjn.forms.FormularioUsuario;
import jjn.modelos.Usuario;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;

public class UsuariosDashboard extends VBox {

    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private TableView<Usuario> tabla;
    @FXML
    private ScrollPane contentArea;

    public UsuariosDashboard(ScrollPane contentArea) {
        Label titulo = new Label("Usuarios");
        titulo.setStyle("-fx-font-size: 32px;");
        this.contentArea = contentArea;

        Button btnNuevoUsuario = crearBotonNuevoUsuario();

        HBox barraSuperior;
        if (Main.getUsuarioActual().esAdmin() || Main.getUsuarioActual().getNombreDepartamento() == "RecursosHumanos") {
            barraSuperior = new HBox(titulo,btnNuevoUsuario);
        }else{
            barraSuperior = new HBox(titulo);
        }

        barraSuperior.setSpacing(20);
        barraSuperior.setAlignment(Pos.CENTER_RIGHT);
        barraSuperior.setStyle("-fx-padding: 10 15 10 15;");

        HBox.setHgrow(titulo, Priority.ALWAYS);

        tabla = crearTabla();
        cargarUsuariosDesdeServidor();

        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: #0A0A0A; -fx-background-color: transparent;");

        getChildren().addAll(barraSuperior, scroll);
    }

    private Button crearBotonNuevoUsuario() {
        Button btn = new Button(" Usuario");

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
            FormularioUsuario user = new FormularioUsuario("insert", null);
            user.mostrar();        // Espera hasta cerrar…

            cargarUsuariosDesdeServidor();    // 🔄 REFRESCAR AL CERRAR
        });

        return btn;
    }

    private TableView<Usuario> crearTabla() {
        TableView<Usuario> table = new TableView<>();
        table.setItems(usuarios);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        tableColumnFactory(table);
        return table;
    }

    private void tableColumnFactory(TableView<Usuario> table) {
        TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));

        TableColumn<Usuario, String> colMail = new TableColumn<>("Mail");
        colMail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMail()));

        TableColumn<Usuario, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRol()));

        TableColumn<Usuario, String> colDepartamento = new TableColumn<>("Departamento");
        colDepartamento.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreDepartamento()));

        TableColumn<Usuario, String> colFechaAlta = new TableColumn<>("Fecha Alta");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colFechaAlta.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFechaAlta() != null ? c.getValue().getFechaAlta().format(fmt) : "—"
        ));

        // Botón ver
        TableColumn<Usuario, Void> colnomina = new TableColumn<>("");
        colnomina.setPrefWidth(30);
        colnomina.setCellFactory(tc -> crearBotonIcono("fas-file-invoice-dollar", "#FFD700", this::mostrarNominas));

        // Botón editar
        TableColumn<Usuario, Void> colEditar = new TableColumn<>("");
        colEditar.setPrefWidth(30);
        colEditar.setCellFactory(tc -> crearBotonIcono("fas-edit", "#00CCFF", this::editarUsuario));

        table.getColumns().addAll(colNombre, colMail, colRol, colDepartamento, colFechaAlta);

        if(Main.getUsuarioActual().esAdmin() || Main.getUsuarioActual().getNombreDepartamento()=="contabilidad") {
            table.getColumns().add(colnomina);
        }
        if(Main.getUsuarioActual().esAdmin() || Main.getUsuarioActual().getNombreDepartamento()=="Recursos Humanos") {
            table.getColumns().add(colEditar);
        }
    }

    private TableCell<Usuario, Void> crearBotonIcono(String icono, String color, java.util.function.Consumer<Usuario> accion) {
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

    private void cargarUsuariosDesdeServidor() {
        new Thread(() -> {
            try {
                var conexion = Main.getConexion();
                conexion.enviar("TODOS_USUARIOS");

                String respuesta = conexion.leerRespuestaCompleta();
                Platform.runLater(() -> usuarios.clear());

                String[] lineas = respuesta.split(Main.JUMP, -1);
                for (String linea : lineas) {
                    if (linea.trim().isEmpty()) continue;
                    String[] campos = linea.split(Main.SEP, -1);
                    if (campos.length < 8) continue;

                    try {
                        Usuario u = new Usuario();
                        u.setId(Integer.parseInt(campos[0]));
                        u.setNombre(campos[1]);      // nombre
                        u.setMail(campos[2]);      // mail
                        u.setRol(campos[3]);     // rol
                        u.setIdDepartamento(campos[4].isEmpty() ? null : Integer.parseInt(campos[4])); // idDepartamento
                        u.setNombreDepartamento(campos[5]);      // nombreDepartamento
                        u.setFechaAlta(campos[6].isEmpty() ? null : java.time.LocalDate.parse(campos[6])); // fechaAlta
                        u.setDireccion(campos[7]);
                        Platform.runLater(() -> usuarios.add(u));
                    } catch (Exception e) {
                        System.out.println("Error parseando usuario: " + linea);
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void mostrarNominas(Usuario usuario) {
        contentArea.setContent(null);
        contentArea.setContent(new NominasDashboard(usuario));
    }

    private void editarUsuario(Usuario usuario) {
        FormularioUsuario user = new FormularioUsuario("update", usuario);
        user.mostrar();
    }
}
