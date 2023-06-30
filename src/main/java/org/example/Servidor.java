package org.example;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Servidor {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Servidor iniciado. Esperando conexión...");

            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado.");

            InputStream inputStream = socket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream("archivo_recibido.txt");

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Archivo recibido y guardado.");

            fileOutputStream.close();
            inputStream.close();
            socket.close();
            serverSocket.close();

            // Desfragmentación y reensamblaje del archivo
            desfragmentarArchivo("archivo_recibido.txt", 3);
            System.out.println("Archivo desfragmentado y almacenado en diferentes servidores.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para desfragmentar el archivo en partes y guardarlas en diferentes servidores
    private static void desfragmentarArchivo(String archivo, int numPartes) throws Exception {
        byte[] fileContent = Files.readAllBytes(Paths.get(archivo));
        int fileSize = fileContent.length;
        int partSize = fileSize / numPartes;

        for (int i = 0; i < numPartes; i++) {
            byte[] fragmento = new byte[partSize];
            System.arraycopy(fileContent, i * partSize, fragmento, 0, partSize);
            guardarFragmento(fragmento, "servidor_" + (i + 1) + ".txt");
        }
    }

    // Método para guardar cada fragmento en un archivo en el servidor
    private static void guardarFragmento(byte[] fragmento, String nombreArchivo) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(nombreArchivo);
        outputStream.write(fragmento);
        outputStream.close();
    }
}