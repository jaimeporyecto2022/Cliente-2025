package jjn;

import java.io.*;
import java.net.Socket;

public class ConexionCliente {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String ultimaRespuesta = "";

    public ConexionCliente(String host, int puerto) throws IOException {
        socket = new Socket(host, puerto);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void enviar(String mensaje) {
        out.println(mensaje);
    }

    public String leerLinea() throws IOException {
        return in.readLine();
    }

    // Lee todo hasta FIN_COMANDO
    public String leerRespuestaCompleta() throws IOException {
        StringBuilder sb = new StringBuilder();
        String linea;
        while ((linea = in.readLine()) != null) {
            if (linea.equals("FIN_COMANDO")) break;
            sb.append(linea).append("\n");
        }
        ultimaRespuesta = sb.toString().trim();
        return ultimaRespuesta;
    }

    public String getUltimaRespuesta() {
        return ultimaRespuesta;
    }

    public void cerrar() {
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}