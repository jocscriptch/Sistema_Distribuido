package org.example;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Cliente_Servidor {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("ServidorDestino iniciado. Esperando conexión...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Conexión establecida.");

                // Crea un nuevo hilo para manejar la solicitud del cliente
                Runnable worker = new WorkerThread(socket);
                new Thread(worker).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class WorkerThread implements Runnable {
        private Socket socket;

        public WorkerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());

                // Lee el nombre del archivo y su tamaño
                String fileName = dis.readUTF();
                long fileSize = dis.readLong();

                // Define el directorio de destino para guardar el archivo cifrado
                String directorioDestino = "/resources/ServidorDestino";

                // Crea un flujo de salida para escribir el archivo cifrado
                FileOutputStream fos = new FileOutputStream(Paths.get(directorioDestino, fileName).toString());

                // Lee y guarda el contenido del archivo cifrado
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                // Cierra los flujos y la conexión del socket
                fos.close();
                dis.close();
                socket.close();

                System.out.println("Archivo cifrado recibido y guardado en el ServidorDestino: " + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
