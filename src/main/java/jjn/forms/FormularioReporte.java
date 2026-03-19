package jjn.forms;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jjn.Cliente;
import jjn.modelos.Reporte;
import jjn.modelos.Tarea;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class FormularioReporte {

    private final Stage stage;
    private final boolean esView;
    private final Reporte reporteOriginal;

    private TextArea txtInfo;
    private ComboBox<String> cbEstado;
    private Button btnSubirArchivo;

    private HBox contenedorArchivo;
    private Label lblArchivo;
    private Button btnDescargarArchivo;
    private Button btnEliminarArchivo;
    private String archivoActual;

    private final Tarea tarea;
    private Integer idReporteCreado;

    public FormularioReporte(String accion, Reporte reporte, Tarea tarea) {

        this.esView = accion.equalsIgnoreCase("view");
        this.reporteOriginal = reporte;
        this.tarea = tarea;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(esView ? "Ver reporte" : "Nuevo reporte");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0f0f0f;");

        Label lblInfo = new Label("Información");
        Label lblEstado = new Label("Estado");

        lblInfo.setStyle("-fx-text-fill: #00CCFF; -fx-font-size: 16px; -fx-font-weight: bold;");
        lblEstado.setStyle("-fx-text-fill: #00CCFF; -fx-font-size: 16px; -fx-font-weight: bold;");

        txtInfo = new TextArea();
        txtInfo.setPrefRowCount(12);
        txtInfo.setStyle("""
            -fx-control-inner-background: #1a1a1a;
            -fx-text-fill: white;
            -fx-border-color: #00CCFF;
            -fx-border-radius: 8;
        """);

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("pendiente", "imposible", "completado");

        btnSubirArchivo = new Button("Subir archivo");
        btnSubirArchivo.setDisable(true);
        btnSubirArchivo.setOnAction(e -> subirArchivo());

        contenedorArchivo = new HBox(10);
        contenedorArchivo.setAlignment(Pos.CENTER_LEFT);

        lblArchivo = new Label("Sin archivo");
        lblArchivo.setStyle("-fx-text-fill: white;");

        btnDescargarArchivo = new Button();
        btnEliminarArchivo = new Button();

        FontIcon iconDesc = new FontIcon("fas-download");
        iconDesc.setIconColor(Color.web("#00CCFF"));

        FontIcon iconDel = new FontIcon("fas-trash");
        iconDel.setIconColor(Color.RED);

        btnDescargarArchivo.setGraphic(iconDesc);
        btnEliminarArchivo.setGraphic(iconDel);

        btnDescargarArchivo.setOnAction(e -> {
            if (archivoActual != null) descargarArchivo(archivoActual);
        });

        btnEliminarArchivo.setOnAction(e -> {
            if (archivoActual != null) {
                eliminarArchivo(archivoActual);
                archivoActual = null;
                actualizarVistaArchivo();
            }
        });

        contenedorArchivo.getChildren().addAll(lblArchivo, btnDescargarArchivo, btnEliminarArchivo);

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardar());

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(e -> stage.close());

        HBox botones = new HBox(10, btnGuardar, btnCerrar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        // 🔒 modo view
        if (esView && reporteOriginal != null) {
            txtInfo.setText(reporteOriginal.getInformacion());
            cbEstado.setValue(reporteOriginal.getEstado());

            txtInfo.setEditable(false);
            cbEstado.setDisable(true);

            btnGuardar.setVisible(false);
            btnSubirArchivo.setVisible(false);
            btnEliminarArchivo.setVisible(false);
        }

        root.getChildren().addAll(
                lblInfo, txtInfo,
                lblEstado, cbEstado,
                new Separator(),
                new Label("Archivos"),
                btnSubirArchivo,
                contenedorArchivo,
                botones
        );

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);

        stage.setScene(new Scene(scroll, 700, 540));
    }

    public void mostrar() {

        if (!esView && reporteOriginal == null) {
            crearReporteVacio();
        }

        if (reporteOriginal != null) {
            cargarArchivosAsync();
            btnSubirArchivo.setDisable(false);
        }

        stage.showAndWait();
    }

    private void crearReporteVacio() {
        new Thread(() -> {
            try {
                var con = Cliente.getConexion();

                con.enviar("CREAR_REPORTE_VACIO"
                        + Cliente.SEP + Cliente.getUsuarioActual().getId()
                        + Cliente.SEP + tarea.getId());

                String res = con.leerRespuestaCompleta();
                String[] p = res.split(Cliente.SEP);

                if (p[0].equals("REPORTE_CREADO")) {
                    idReporteCreado = Integer.parseInt(p[1]);
                    Platform.runLater(() -> btnSubirArchivo.setDisable(false));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Integer getIdActual() {
        return (reporteOriginal != null)
                ? reporteOriginal.getId()
                : idReporteCreado;
    }

    private void guardar() {

        if (getIdActual() == null) return;

        if (cbEstado.getValue() == null) return;

        new Thread(() -> {
            try {
                var con = Cliente.getConexion();

                con.enviar("ACTUALIZAR_REPORTE"
                        + Cliente.SEP + getIdActual()
                        + Cliente.SEP + txtInfo.getText()
                        + Cliente.SEP + cbEstado.getValue());

                con.leerRespuestaCompleta();

                Platform.runLater(stage::close);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void subirArchivo() {

        if (getIdActual() == null) return;

        File f = new FileChooser().showOpenDialog(stage);
        if (f == null) return;

        btnSubirArchivo.setDisable(true);

        new Thread(() -> {
            try {
                byte[] b = Files.readAllBytes(f.toPath());

                String base64 = Base64.getEncoder()
                        .encodeToString(b)
                        .replace("\n", "")
                        .replace("\r", "");

                var con = Cliente.getConexion();

                con.enviar("INSERTAR_ARCHIVO"
                        + Cliente.SEP + f.getName()
                        + Cliente.SEP + f.getName()
                        + Cliente.SEP + "bin"
                        + Cliente.SEP + "application/octet-stream"
                        + Cliente.SEP + b.length
                        + Cliente.SEP + "reporte"
                        + Cliente.SEP + getIdActual());

                con.enviar("DATA");
                con.enviar(base64);
                con.enviar("FIN_DATA");

                con.leerRespuestaCompleta();

                Platform.runLater(() -> {
                    btnSubirArchivo.setDisable(false);
                    cargarArchivosAsync();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> btnSubirArchivo.setDisable(false));
            }
        }).start();
    }

    private void cargarArchivosAsync() {
        new Thread(() -> {
            try {
                var con = Cliente.getConexion();

                con.enviar("OBTENER_ARCHIVOS"
                        + Cliente.SEP + "reporte"
                        + Cliente.SEP + getIdActual());

                String res = con.leerRespuestaCompleta();

                if (res == null || res.isEmpty() || res.equals("VACIO")) {
                    archivoActual = null;
                } else {
                    int index = res.indexOf(Cliente.JUMP);
                    archivoActual = (index != -1)
                            ? res.substring(0, index).trim()
                            : res.trim();
                }

                Platform.runLater(this::actualizarVistaArchivo);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void actualizarVistaArchivo() {

        if (archivoActual == null || archivoActual.isEmpty()) {
            lblArchivo.setText("Sin archivo");
            btnDescargarArchivo.setDisable(true);
            btnEliminarArchivo.setDisable(true);
            return;
        }

        String nombre = extraerCampo(archivoActual, 1);
        lblArchivo.setText(nombre);

        btnDescargarArchivo.setDisable(false);
        btnEliminarArchivo.setDisable(esView);
    }

    private void eliminarArchivo(String fila) {
        String id = extraerCampo(fila, 0);
        Cliente.getConexion().enviar("ELIMINAR_ARCHIVO" + Cliente.SEP + id);
    }

    private void descargarArchivo(String fila) {
        try {
            String base64 = extraerCampo(fila, 6).trim();
            byte[] bytes = Base64.getDecoder().decode(base64);

            FileChooser fc = new FileChooser();
            fc.setInitialFileName(extraerCampo(fila, 1));

            File f = fc.showSaveDialog(stage);
            if (f != null) Files.write(f.toPath(), bytes);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extraerCampo(String fila, int index) {
        String[] p = fila.split("\\Q" + Cliente.SEP + "\\E");
        return p.length > index ? p[index] : "";
    }
}