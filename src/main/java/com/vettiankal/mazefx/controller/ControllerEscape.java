package com.vettiankal.mazefx.controller;

import com.vettiankal.mazefx.Main;
import com.vettiankal.mazefx.game.Screen;
import com.vettiankal.mazefx.player.Player;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerEscape implements Initializable {
    @FXML
    private Slider resolution;
    @FXML
    private Button back;
    @FXML
    private Button mainMenu;
    private static Player player;

    public static Scene escapeScene(Player player) throws IOException {
        ControllerEscape.player = player;
        player.pause();
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("screenescape.fxml"));
        return new Scene(loader.load());
    }

    public void initialize(URL location, ResourceBundle resources) {
        resolution.setValue((double)Screen.SCALE);
        back.setOnMouseClicked((event) -> {
            Platform.runLater(() -> Main.window.setScene(player));
            Screen.SCALE = (int)resolution.getValue();
            player.resume();
        });
        mainMenu.setOnMouseClicked((event) -> Main.setMainScreen());
    }
}