package com.vettiankal.mazefx.player;

import com.vettiankal.mazefx.Main;
import com.vettiankal.mazefx.game.Level;
import javafx.application.Platform;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;

import java.util.Timer;
import java.util.TimerTask;

public class TutorialPlayer extends Player {

    private long timer = System.currentTimeMillis();
    private boolean over;
    private boolean temp;

    public TutorialPlayer() {
        super(new Pane(), Level.getTutorial());
        setMovement(false);
        setTurnMovement(false);
    }

    public void updateScreen() {
        int[] temp = getScreen().update(this, null);
        PixelWriter pw = getScreen().getGraphicsContext2D().getPixelWriter();
        long time = System.currentTimeMillis() - timer;
        Platform.runLater(() -> {
            pw.setPixels(0, 0, Main.WIDTH, Main.HEIGHT, PixelFormat.getIntArgbPreInstance(), temp, 0, Main.WIDTH);
            if (time < 5000L) {
                getScreen().drawMessage("Welcome to the tutorial!\nLeft click the mouse to gain control of your mouse\nHit ESC to see options");
                return;
            }

            setMovement(true);
            if (!hasMoved()) {
                getScreen().drawMessage("W - UP          A - LEFT\nS - DOWN     D - RIGHT\nTry Moving!");
                return;
            }

            setTurnMovement(true);
            if (!hasTurned()) {
                getScreen().drawMessage("Great job!\nNow use the mouse to turn left and right\nTry Turning!");
                return;
            }

            if ((this.getLevel().getEndX() != (int) this.getXPos() || this.getLevel().getEndY() != (int) getYPos()) && !this.temp) {
                getScreen().drawMessage("Nice!\nNow try finding the end of the maze!");
                return;
            }

            getScreen().drawMessage("You finished the tutorial!");
            this.temp = true;
            (new Timer()).schedule(new TimerTask() {
                public void run() {
                    over = true;
                }
            }, 3000L);
        });
    }

    public boolean hasWon() {
        return over;
    }

    public void endGame() {
        Main.setMainScreen();
    }
}

