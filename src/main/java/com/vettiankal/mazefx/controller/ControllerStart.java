package com.vettiankal.mazefx.controller;

import com.vettiankal.mazefx.Main;
import com.vettiankal.mazefx.game.Level;
import com.vettiankal.mazefx.player.MultiPlayer;
import com.vettiankal.mazefx.player.PracticePlayer;
import com.vettiankal.mazefx.player.SinglePlayer;
import com.vettiankal.mazefx.player.TutorialPlayer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.io.IOException;
import java.net.*;
import java.util.ResourceBundle;

public class ControllerStart implements Initializable {
    @FXML
    private Button practice;
    @FXML
    private Button singlePlayer;
    @FXML
    private Button tutorial;
    @FXML
    private Button multiplayer;
    @FXML
    private Button start;
    @FXML
    private Button back;
    @FXML
    private Button pStart;
    @FXML
    private Button pBack;
    @FXML
    private TextField ipaddress;
    @FXML
    private TextField name;
    @FXML
    private Label levelLabel;
    @FXML
    private Slider levelSlider;

    private DatagramSocket socket;
    private InetAddress address;

    private static volatile int connectionAttempt;
    private BooleanProperty disableStart = new SimpleBooleanProperty(true);

    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
        } catch (SocketException var4) {
            multiplayer.setDisable(true);
        }

        tutorial.setOnMouseClicked((event) -> {
            Scene scene = new TutorialPlayer();
            Main.window.setScene(scene);
        });
        practice.setOnMouseClicked((event) -> {
            practiceDisplay();
            animationLeft(false, tutorial, practice, singlePlayer, multiplayer);
            animationLeft(true, levelLabel, levelSlider, pStart, pBack);
        });
        singlePlayer.setOnMouseClicked((event) -> {
            speedDisplay();
            animationLeft(false, tutorial, practice, singlePlayer, multiplayer);
            animationLeft(true, levelLabel, levelSlider, pStart, pBack);
        });
        multiplayer.setOnMouseClicked((event) -> {
            animationRight(false, tutorial, practice, singlePlayer, multiplayer);
            animationRight(true, ipaddress, name, start, back);
        });
        disableStart.addListener((a, b, c) -> {
            start.setDisable(c);
            Platform.runLater(() -> this.start.setText(c ? "Pinging..." : "Start"));
        });
        ipaddress.textProperty().addListener((observable, oldValue, newValue) -> startConnection(ipaddress.getText().trim().equals("") || name.getText().trim().equals("")));
        name.textProperty().addListener((observable, oldValue, newValue) -> startConnection(ipaddress.getText().trim().equals("") || name.getText().trim().equals("")));
        start.setText("Pinging...");
        start.setOnMouseClicked((event) -> {
            start.setDisable(true);
            Scene scene = new MultiPlayer(name.getText().trim().replace(" ", "_"), address, socket);
            Main.window.setScene(scene);
        });
        levelSlider.valueProperty().addListener((observable, oldValue, newValue) -> levelLabel.setText(levelLabel.getText().split(" ")[0] + " " + newValue.intValue()));
        pBack.setOnMouseClicked((event) -> {
            unanimationRight(true, tutorial, practice, singlePlayer, multiplayer);
            unanimationRight(false, levelLabel, levelSlider, pStart, pBack);
        });
        pStart.setOnMouseClicked((event) -> {
            if (levelLabel.getText().split(" ")[0].equalsIgnoreCase("time")) {
                Scene scene = new SinglePlayer(new Level(1), (int)levelSlider.getValue());
                Main.window.setScene(scene);
            } else {
                Scene scene = new PracticePlayer(new Level((int)levelSlider.getValue()));
                Main.window.setScene(scene);
            }

        });
        back.setOnMouseClicked((event) -> {
            unanimationLeft(false, ipaddress, name, start, back);
            unanimationLeft(true, tutorial, practice, singlePlayer, multiplayer);
        });
    }

    public void startConnection(boolean init) {
        if (!init) {
            if (connectionAttempt == 255) {
                connectionAttempt = 0;
            }

            connectionAttempt++;
            new Thread(new ControllerStart.Ping(connectionAttempt)).start();
        }

        disableStart.set(true);
    }

    public void animationLeft(boolean add, Node... nodes) {
        for(Node node : nodes) {
            moveLeft(node, add, true);
        }
    }

    public void unanimationLeft(boolean add, Node... nodes) {
        for(Node node : nodes) {
            moveLeft(node, add, false);
        }
    }

    public void animationRight(boolean add, Node... nodes) {
        for(Node node : nodes) {
            moveRight(node, add, true);
        }
    }

    public void unanimationRight(boolean add, Node... nodes) {
        for(Node node : nodes) {
            moveRight(node, add, false);
        }
    }

    public void moveLeft(Node node, boolean add, boolean left) {
        node.setVisible(true);
        Timeline tl = new Timeline();
        tl.setCycleCount(1);
        tl.getKeyFrames().add(new KeyFrame(Duration.seconds(1.0D), new KeyValue(node.translateXProperty(), left ? -300 : 0)));
        tl.play();
        tl.setOnFinished((event) -> node.setVisible(add));
    }

    public void moveRight(Node node, boolean add, boolean right) {
        node.setVisible(true);
        Timeline tl = new Timeline();
        tl.setCycleCount(1);
        tl.getKeyFrames().add(new KeyFrame(Duration.seconds(1.0D), new KeyValue(node.translateXProperty(), right ? 300 : 0)));
        tl.play();
        tl.setOnFinished((event) -> node.setVisible(add));
    }

    public void practiceDisplay() {
        levelLabel.setText("Level 1");
        levelSlider.setValue(1.0D);
        levelSlider.setMin(1.0D);
        levelSlider.setMax(7.0D);
        levelSlider.setMajorTickUnit(1.0D);
    }

    public void speedDisplay() {
        levelLabel.setText("Time 60");
        levelSlider.setMin(0.0D);
        levelSlider.setMax(3600.0D);
        levelSlider.setValue(60.0D);
        levelSlider.setMajorTickUnit(60.0D);
    }

    class Ping implements Runnable {

        private final int connectionAttempts;

        public Ping(int connectionAttempt) {
            connectionAttempts = connectionAttempt;
        }

        public void run() {
            try {
                address = InetAddress.getByName(ipaddress.getText());
                socket.send(new DatagramPacket(new byte[]{-1, (byte)connectionAttempts}, 2, address, 8901));
                byte[] recieve = new byte[1];
                DatagramPacket receivePacket = new DatagramPacket(recieve, recieve.length);
                socket.receive(receivePacket);
                byte[] info = receivePacket.getData();
                if (info[0] == ControllerStart.connectionAttempt) {
                    disableStart.set(false);
                }
            } catch (IOException var4) {
                if (connectionAttempts == ControllerStart.connectionAttempt) {
                    disableStart.set(true);
                }
            }

        }
    }
}

