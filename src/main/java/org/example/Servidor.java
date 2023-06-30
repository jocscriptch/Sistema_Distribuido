package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            ExecutorService executor = Executors.newFixedThreadPool(3); // Crea un pool de hilos con 3 hilos

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado.");

                // Crea un nuevo hilo para manejar la solicitud del cliente
                Runnable worker = new WorkerThread(socket);
                executor.execute(worker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Clase WorkerThread para manejar la solicitud de un cliente en un hilo separado
class WorkerThread implements Runnable {
    private Socket socket;

    public WorkerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
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

            // Cifrado del archivo
            cifrarArchivo("archivo_recibido.txt");
            System.out.println("Archivo cifrado.");

            // Obtener los nombres de archivo de las partes cifradas
            String[] nombresPartes = obtenerNombresPartes("archivo_cifrado.txt", 3);

            // Enviar cada parte cifrada a un servidor diferente
            for (int i = 0; i < nombresPartes.length; i++) {
                String nombreParte = nombresPartes[i];
                enviarArchivo(nombreParte, "servidor" + (i + 1) + ".com", 1234); // Reemplaza con los detalles de conexión adecuados
            }

            // Desfragmentación y reensamblaje del archivo cifrado
            desfragmentarArchivoCifrado("archivo_cifrado.txt", 3);
            System.out.println("Archivo cifrado desfragmentado y almacenado en los servidores.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para obtener los nombres de archivo de las partes cifradas
    private String[] obtenerNombresPartes(String archivo, int numPartes) {
        String[] nombresPartes = new String[numPartes];
        for (int i = 0; i < numPartes; i++) {
            nombresPartes[i] = "servidor_" + (i + 1) + ".txt";
        }
        return nombresPartes;
    }

    // Método para enviar un archivo a un servidor remoto
    private void enviarArchivo(String archivo, String host, int puerto) throws IOException {
        File file = new File(archivo);
        long fileSize = file.length();

        Socket socket = new Socket(host, puerto);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        // Enviar el nombre del archivo y su tamaño al servidor
        dos.writeUTF(file.getName());
        dos.writeLong(fileSize);

        // Enviar el contenido del archivo al servidor
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            dos.write(buffer, 0, bytesRead);
        }
        fis.close();

        dos.flush();
        dos.close();
        socket.close();
    }

    // Método para desfragmentar el archivo cifrado en partes y guardarlas en diferentes servidores
    private void desfragmentarArchivoCifrado(String archivo, int numPartes) throws Exception {
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
    private void guardarFragmento(byte[] fragmento, String nombreArchivo) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(nombreArchivo);
        outputStream.write(fragmento);
        outputStream.close();
    }

    // Método para cifrar el archivo utilizando AES
    private void cifrarArchivo(String archivo) throws Exception {
        byte[] fileContent = Files.readAllBytes(Paths.get(archivo));

        // Generar una clave secreta de cifrado
        String clave = "miClaveSecreta123";
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] claveBytes = sha.digest(clave.getBytes("UTF-8"));
        SecretKey secretKey = new SecretKeySpec(claveBytes, "AES");

        // Cifrar el archivo utilizando AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] archivoCifrado = cipher.doFinal(fileContent);

        // Guardar el archivo cifrado en disco
        FileOutputStream outputStream = new FileOutputStream("archivo_cifrado.txt");
        outputStream.write(archivoCifrado);
        outputStream.close();
    }
}
