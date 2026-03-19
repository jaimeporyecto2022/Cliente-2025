package jjn.dashboardElements;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import javafx.stage.FileChooser;
import jjn.Cliente;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import jjn.modelos.EstadisticaDepartamento;
import java.io.File;
import java.io.FileOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class EstadisticaDepartamentoDashboard extends VBox {

        private TableView<EstadisticaDepartamento> tabla;
        private final ObservableList<EstadisticaDepartamento> estadisticas =
                FXCollections.observableArrayList();

        private DatePicker dpInicio;
        private DatePicker dpFin;

        private ScrollPane contentArea;

        public EstadisticaDepartamentoDashboard(ScrollPane contentArea) {

            this.contentArea = contentArea;

            Label titulo = new Label("Estadísticas Departamentos");
            titulo.setStyle("-fx-font-size: 32px;");

            // 📅 DatePickers
            dpInicio = new DatePicker();
            dpFin = new DatePicker();

            int year = java.time.LocalDate.now().getYear();
            dpInicio.setValue(java.time.LocalDate.of(year, 1, 1));
            dpFin.setValue(java.time.LocalDate.of(year, 12, 31));

            dpInicio.setPrefWidth(140);
            dpFin.setPrefWidth(140);

            // 🔍 Botón buscar
            Button btnBuscar = new Button("Buscar");
            btnBuscar.setOnAction(e ->
                    cargarEstadisticasDesdeServidor(dpInicio.getValue(), dpFin.getValue())
            );

            // 📄 Botón PDF
            Button btnPdf = new Button("PDF");
            btnPdf.setOnAction(e -> generarPdf());

            // 🎛 Barra superior
            HBox filtros = new HBox(
                    new Label("Desde:"), dpInicio,
                    new Label("Hasta:"), dpFin,
                    btnBuscar,
                    btnPdf
            );

            filtros.setSpacing(10);
            filtros.setAlignment(Pos.CENTER_RIGHT);

            HBox barraSuperior = new HBox(titulo, filtros);
            barraSuperior.setSpacing(20);
            barraSuperior.setAlignment(Pos.CENTER_RIGHT);
            barraSuperior.setStyle("-fx-padding: 10 15 10 15;");
            HBox.setHgrow(titulo, Priority.ALWAYS);

            // 📊 Tabla
            tabla = crearTabla();
            tabla.setItems(estadisticas);

            // Carga inicial
            cargarEstadisticasDesdeServidor(dpInicio.getValue(), dpFin.getValue());

            ScrollPane scroll = new ScrollPane(tabla);
            scroll.setFitToWidth(true);
            scroll.setFitToHeight(true);
            scroll.setStyle("-fx-background: #0A0A0A; -fx-background-color: transparent;");

            getChildren().addAll(barraSuperior, scroll);
        }



        private TableView<EstadisticaDepartamento> crearTabla() {
        TableView<EstadisticaDepartamento> table = new TableView<>();
        table.setItems(estadisticas);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        tableColumnFactory(table);
        return table;
    }

    private void tableColumnFactory(TableView<EstadisticaDepartamento> table) {

        TableColumn<EstadisticaDepartamento, String> colDepto =
                new TableColumn<>("Departamento");
        colDepto.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getNombreDepartamento()
                ));

        TableColumn<EstadisticaDepartamento, String> colTotal =
                new TableColumn<>("Tareas");
        colTotal.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(c.getValue().getTotalTareas())
                ));

        TableColumn<EstadisticaDepartamento, String> colCompletadas =
                new TableColumn<>("Completadas");
        colCompletadas.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(c.getValue().getTareasCompletadas())
                ));

        TableColumn<EstadisticaDepartamento, String> colFueraTiempo =
                new TableColumn<>("Fuera de tiempo");
        colFueraTiempo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(c.getValue().getTareasFueraDeTiempo())
                ));

        TableColumn<EstadisticaDepartamento, String> colNominas =
                new TableColumn<>("Total nóminas");
        colNominas.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getTotalNominas().toPlainString()
                ));

        table.getColumns().addAll(
                colDepto,
                colTotal,
                colCompletadas,
                colFueraTiempo,
                colNominas
        );
    }

    //done
    private void cargarEstadisticasDesdeServidor(java.time.LocalDate fechaInicio,
                                                 java.time.LocalDate fechaFin) {

        new Thread(() -> {
            try {
                var conexion = Cliente.getConexion();

                // Enviar comando al servidor
                conexion.enviar(
                        "ESTADISTICAS_DEPARTAMENTO" + Cliente.SEP +
                                fechaInicio + Cliente.SEP +
                                fechaFin
                );
                String respuesta = conexion.leerRespuestaCompleta();
                System.out.println("linea "+respuesta);
                Platform.runLater(() -> estadisticas.clear());

                String[] lineas = respuesta.split(Cliente.JUMP, -1);

                for (String linea : lineas) {
                    if (linea == null || linea.trim().isEmpty()) continue;
                    String[] campos = linea.split(Cliente.SEP, -1);
                    if (campos.length < 5) continue;

                    try {
                        EstadisticaDepartamento est = new EstadisticaDepartamento(
                                campos[0],                               // nombreDepartamento
                                Integer.parseInt(campos[1]),             // totalTareas
                                Integer.parseInt(campos[2]),             // tareasCompletadas
                                Integer.parseInt(campos[3]),             // tareasFueraDeTiempo
                                new java.math.BigDecimal(campos[4])       // totalNominas
                        );

                        Platform.runLater(() -> estadisticas.add(est));

                    } catch (Exception e) {
                        System.out.println("Error parseando estadística: " + linea);
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void generarPdf() {
        if (estadisticas.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("PDF");
            alert.setHeaderText(null);
            alert.setContentText("No hay datos para generar el PDF.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        fileChooser.setInitialFileName("estadisticas_departamentos.pdf");

        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file == null) return;

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(file));

            document.open();

            //  Título
            Font tituloFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph titulo = new Paragraph("Estadísticas por Departamento", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);

            //  Fechas
            Font fechaFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph fechas = new Paragraph(
                    "Periodo: " + dpInicio.getValue() + "  →  " + dpFin.getValue(),
                    fechaFont
            );
            fechas.setAlignment(Element.ALIGN_CENTER);
            fechas.setSpacingAfter(15);
            document.add(fechas);

            //  Tabla
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new float[]{3, 2, 2, 3, 3});

            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);

            addHeaderCell(table, "Departamento", headerFont);
            addHeaderCell(table, "Tareas", headerFont);
            addHeaderCell(table, "Completadas", headerFont);
            addHeaderCell(table, "Fuera de tiempo", headerFont);
            addHeaderCell(table, "Total nóminas (€)", headerFont);

            Font bodyFont = new Font(Font.HELVETICA, 10);

            for (EstadisticaDepartamento e : estadisticas) {
                table.addCell(new Phrase(e.getNombreDepartamento(), bodyFont));
                table.addCell(new Phrase(String.valueOf(e.getTotalTareas()), bodyFont));
                table.addCell(new Phrase(String.valueOf(e.getTareasCompletadas()), bodyFont));
                table.addCell(new Phrase(String.valueOf(e.getTareasFueraDeTiempo()), bodyFont));
                table.addCell(new Phrase(e.getTotalNominas().toPlainString(), bodyFont));
            }

            document.add(table);
            document.close();

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("PDF generado");
            ok.setHeaderText(null);
            ok.setContentText("PDF generado correctamente.");
            ok.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error PDF");
            error.setHeaderText("No se pudo generar el PDF");
            error.setContentText(e.getMessage());
            error.showAndWait();
        }
    }
    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        //cell.setBackgroundColor(new BaseColor(220, 220, 220));
        cell.setPadding(6);
        table.addCell(cell);
    }




}