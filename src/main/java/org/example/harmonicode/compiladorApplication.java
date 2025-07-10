package org.example.harmonicode;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class compiladorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(compiladorApplication.class.getResource("compilador.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1360, 690);
        stage.setTitle("HarmoniCode");
        stage.setScene(scene);
        //CARGAR LOGO
        stage.getIcons().add(new Image(getClass().getResourceAsStream("img/hclogo.png")));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}