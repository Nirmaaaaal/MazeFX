package com.vettiankal.mazefx.controller;

import com.vettiankal.mazefx.Main;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerEnd implements Initializable {
    @FXML
    private ListView<String> playerList;
    @FXML
    private Button back;
    @FXML
    private Button more;
    @FXML
    private Button menu;
    @FXML
    private Label timeTaken;
    @FXML
    private Label currLevel;
    @FXML
    private Label endDistance;

    private static int time;
    private static int level;
    private static int distanceToEnd;
    private static String[] listOfPlayers;
    private static boolean multiplayer = true;

    public static Scene endScene(int time, int level, int distanceToEnd, String[] listOfPlayers) throws IOException {
        ControllerEnd.time = time;
        ControllerEnd.level = level;
        ControllerEnd.distanceToEnd = distanceToEnd;
        multiplayer = listOfPlayers != null;
        ControllerEnd.listOfPlayers = listOfPlayers;
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("screenend.fxml"));
        return new Scene(loader.load());
    }

    public void initialize(URL location, ResourceBundle resources) {
        if (listOfPlayers != null) {
            playerList.setItems(FXCollections.observableArrayList(listOfPlayers));
        }

        String sTime = time / 3600000 + "h " + time % 3600000 / 60000 + "m " + time % 3600000 % 60000 / 1000 + "s";
        timeTaken.setText("Time Taken:\n" + sTime);
        currLevel.setText("Level: " + level);
        endDistance.setText("End Distance: " + distanceToEnd);
        back.setOnMouseClicked((event) -> {
            unanimation(false, playerList, back);
            unanimation(true, timeTaken, currLevel, endDistance, more, menu);
        });
        menu.setOnMouseClicked((event) -> Main.setMainScreen());
        more.setDisable(!multiplayer);
        more.setOnMouseClicked((event) -> {
            animation(true, playerList, back);
            animation(false, timeTaken, currLevel, endDistance, more, menu);
        });
    }

    public void animation(boolean add, Node... nodes) {
        for(Node node : nodes) {
            move(node, add, true);
        }
    }

    public void unanimation(boolean add, Node... nodes) {
        for(Node node : nodes) {
            move(node, add, false);
        }
    }

    public void move(Node node, boolean add, boolean right) {
        node.setVisible(true);
        Timeline tl = new Timeline();
        tl.setCycleCount(1);
        tl.getKeyFrames().add(new KeyFrame(Duration.seconds(1.0D), new KeyValue(node.translateXProperty(), right ? -275 : 0)));
        tl.play();
        tl.setOnFinished((event) -> node.setVisible(add));
    }
}

