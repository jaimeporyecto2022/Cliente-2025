package jjn.dashboardElements;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class HeaderPanel extends VBox {
    private final Label lblUsuarioYRol = new Label();

    public HeaderPanel() {
        lblUsuarioYRol.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 28px; -fx-font-weight: bold;");
        getChildren().add(lblUsuarioYRol);
        setStyle("-fx-background-color: #111111; -fx-padding: 18; -fx-spacing: 8;");
        getChildren().add(new javafx.scene.control.Separator());
    }

    public void setUsuario(String nombre, String rol) {
        String rolTexto = switch (rol.toLowerCase()) {
            case "admin" -> "ADMINISTRADOR";
            case "jefe"  -> "JEFE";
            default      -> "EMPLEADO";
        };
        lblUsuarioYRol.setText(nombre.toUpperCase() + " - " + rolTexto);
        if ("admin".equalsIgnoreCase(rol)) {
            lblUsuarioYRol.setTextFill(Color.GOLD);
        }
    }
}