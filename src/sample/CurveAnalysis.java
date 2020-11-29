package sample;

import javafx.application.Application;
import javafx.stage.Stage;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class CurveAnalysis extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("CurveAnalysis.fxml"));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Curve Analysis");
        primaryStage.setScene(scene);

        primaryStage.show();


    }
}
