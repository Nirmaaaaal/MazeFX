package com.vettiankal.mazefx.player;

import com.vettiankal.mazefx.Main;
import com.vettiankal.mazefx.controller.ControllerEnd;
import com.vettiankal.mazefx.game.Level;
import javafx.application.Platform;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class PracticePlayer extends Player {

    private long millis = System.currentTimeMillis();

    public PracticePlayer(Level level) {
        super(new Pane(), level);
    }

    public void updateScreen() {
        int[] temp = getScreen().update(this, null);
        PixelWriter pw = getScreen().getGraphicsContext2D().getPixelWriter();
        Platform.runLater(() -> pw.setPixels(0, 0, Main.WIDTH, Main.HEIGHT, PixelFormat.getIntArgbPreInstance(), temp, 0, Main.WIDTH));
    }

    public boolean hasWon() {
        return (int)getXPos() == getLevel().getEndX() && (int)getYPos() == getLevel().getEndY();
    }

    public void endGame() {
        Platform.runLater(() -> {
            try {
                Main.window.setScene(ControllerEnd.endScene((int)(System.currentTimeMillis() - millis), getLevel().getLevel(), getDistanceToEnd(), null));
            } catch (IOException var2) {
                Main.setMainScreen();
            }

        });
    }
}