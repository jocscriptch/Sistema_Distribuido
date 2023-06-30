package org.example;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;

public class Cliente extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                Socket socket = new Socket("localhost", 1234);
                System.out.println("Conexión establecida con el servidor.");

                // Cifrado del archivo antes de enviarlo
                byte[] archivoCifrado = cifrarArchivo(file);

                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(archivoCifrado);
                System.out.println("Archivo enviado.");

                outputStream.close();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para cifrar el archivo utilizando AES
    private static byte[] cifrarArchivo(File file) throws Exception {
        byte[] fileContent = Files.readAllBytes(file.toPath());

        // Genera una clave secreta para el cifrado
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest("clave_secreta".getBytes());
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        // Crea una instancia del cifrador AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Cifra el archivo
        byte[] archivoCifrado = cipher.doFinal(fileContent);

        return archivoCifrado;
    }
}
