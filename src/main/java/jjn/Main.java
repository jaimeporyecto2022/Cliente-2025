// src/main/java/jjn/Main.java
package jjn;

import jjn.modelos.Usuario;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

    public class Main extends Application {
        // ==================== VARIABLES GLOBALES ====================
        private static Usuario usuarioActual;        // Usuario logueado (con todos los datos)
        private static ConexionCliente conexion;      // Conexión viva con el servidor
        public static final String SEP = "@Tr&m"; //separador comunicación
        public static final String JUMP = "@Jump";
        // ==================== GETTERS Y SETTERS ====================
        public static Usuario getUsuarioActual() {return usuarioActual;}
        public static void setUsuarioActual(Usuario usuario) {
            usuarioActual = usuario;
            if (usuario != null) {
                System.out.println("USUARIO LOGUEADO → " + usuario.getNombre() +
                        " | Rol: " + usuario.getRol() +
                        " | Departamento: " + usuario.getNombreDepartamento());
            }
        }
        public static ConexionCliente getConexion() {return conexion;}
        public static void setConexion(ConexionCliente c) {conexion = c;}
        // ==================== CERRAR SESIÓN ====================
        public static void cerrarSesion() {
            if (conexion != null) {
                try { conexion.cerrar(); } catch (Exception ignored) {}
                conexion = null;
            }
            usuarioActual = null;
            System.out.println("ESION CERRADA");
        }
        // ==================== INICIO DE LA APLICACIÓN ====================
        @Override
        public void start(Stage stage) throws Exception {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 700);

            stage.setScene(scene);
            stage.setTitle("Cliente");
            stage.setResizable(false);
       //     stage.getIcons().add(new javafx.scene.image.Image(
//                    getClass().getResourceAsStream("/resources/img/jjn_icon.png"))); // opcional: añade tu logo
            stage.centerOnScreen();
            stage.show();
        }
        // ==================== MAIN ====================
        public static void main(String[] args) {
            launch(args);
        }
    }