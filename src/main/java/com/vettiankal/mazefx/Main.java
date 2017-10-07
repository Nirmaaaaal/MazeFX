package com.vettiankal.mazefx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    private static Scene titleScene;
    public static Stage window;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 500;

    public static void main(String... args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("MazesFX");
        window.setOnCloseRequest((event) -> System.exit(0));

        try {
            //It will almost never be null, just to get rid of warning
            URL url = getClass().getClassLoader().getResource("icon.png");
            if(url != null) {
                window.getIcons().add(SwingFXUtils.toFXImage(ImageIO.read(url), null));
            }
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("screenstart.fxml"));
        titleScene = new Scene(loader.load());
        window.setScene(titleScene);
        window.sizeToScene();
        window.setResizable(false);
        window.show();
    }

    public static void setMainScreen() {
        Platform.runLater(() -> window.setScene(titleScene));
    }
}
