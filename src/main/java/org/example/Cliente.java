package org.example;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

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

                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                // Enviar el nombre del archivo al servidor
                dataOutputStream.writeUTF(file.getName());

                // Enviar el tamaño del archivo al servidor
                dataOutputStream.writeLong(file.length());

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }

                System.out.println("Archivo enviado.");

                bufferedInputStream.close();
                dataOutputStream.close();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
