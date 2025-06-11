package com.modelisation;

import com.modelisation.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Classe principale de l'application JavaFX
 * Point d'entrée de l'application de dessin de formes géométriques
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            MainView mainView = new MainView();
            mainView.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
