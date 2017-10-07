package com.vettiankal.mazefx.player;

import com.vettiankal.mazefx.Main;
import com.vettiankal.mazefx.controller.ControllerEnd;
import com.vettiankal.mazefx.game.Level;
import javafx.application.Platform;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.text.DecimalFormat;

public class SinglePlayer extends Player {

    private String timer;
    private long start = System.currentTimeMillis();
    private int seconds;
    private int timeInSeconds;
    private DecimalFormat df = new DecimalFormat("00");
    private long elapsedTime;

    public SinglePlayer(Level level, int timeInSeconds) {
        super(new Pane(), level);
        if (timeInSeconds < 60) {
            timeInSeconds = 60;
        }

        this.timeInSeconds = timeInSeconds;
    }

    public void updateScreen() {
        int[] temp = getScreen().update(this, null);
        PixelWriter pw = getScreen().getGraphicsContext2D().getPixelWriter();
        seconds = timeInSeconds - (int)(System.currentTimeMillis() - start) / 1000 + (int)elapsedTime / 1000;
        timer = seconds / 60 + ":" + df.format((long)(seconds % 60));
        Platform.runLater(() -> {
            pw.setPixels(0, 0, Main.WIDTH, Main.HEIGHT, PixelFormat.getIntArgbPreInstance(), temp, 0, Main.HEIGHT);
            getScreen().drawTimer(timer);
        });
        if (getLevel().getEndX() == (int)getXPos() && getLevel().getEndY() == (int)getYPos()) {
            setLevel(new Level(getLevel().getLevel() + 1));
        }

    }

    public boolean hasWon() {
        return seconds == 0 || getLevel().getLevel() == 7 && (int)getXPos() == getLevel().getEndX() && (int)getYPos() == getLevel().getEndY();
    }

    public void endGame() {
        Platform.runLater(() -> {
            try {
                Main.window.setScene(ControllerEnd.endScene((int)(System.currentTimeMillis() - start), getLevel().getLevel(), getDistanceToEnd(), null));
            } catch (IOException var2) {
                Main.setMainScreen();
            }

        });
    }

    public void pause() {
        super.pause();
        elapsedTime = System.currentTimeMillis();
    }

    public void resume() {
        super.resume();
        elapsedTime = System.currentTimeMillis() - elapsedTime;
    }
}
