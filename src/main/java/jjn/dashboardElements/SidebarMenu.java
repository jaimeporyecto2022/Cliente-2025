package jjn.dashboardElements;

import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public class SidebarMenu extends VBox {

    public SidebarMenu() {
        setPrefWidth(80);
        setStyle("-fx-background-color: #111111; -fx-padding: 20 10; -fx-spacing: 15;");
    }

    public void agregarBoton(String icono, String texto, Runnable accion) {
        FontIcon icon = new FontIcon("fas-" + icono + ":26");
        icon.setIconColor(Color.web("#FFD700"));

        Button btn = new Button(texto, icon);
        btn.setPrefSize(80, 80);
        btn.setContentDisplay(ContentDisplay.TOP);
        btn.setAlignment(Pos.CENTER);
        btn.setStyle("-fx-background-color: #111111; -fx-text-fill: #FFD700; -fx-font-size: 10px; -fx-background-radius: 12;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #FFD700; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 12;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #111111; -fx-text-fill: #FFD700; -fx-font-size: 10px; -fx-background-radius: 12;"));

        btn.setOnAction(e -> accion.run());
        getChildren().add(btn);
    }
}