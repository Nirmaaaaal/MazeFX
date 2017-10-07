package com.vettiankal.mazefx.player;

import com.vettiankal.mazefx.Main;
import com.vettiankal.mazefx.controller.ControllerEscape;
import com.vettiankal.mazefx.game.Level;
import com.vettiankal.mazefx.game.Screen;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Player extends Scene {

    private static final int LOOP_EVERY_X_MILLIS = 20;
    private static final double SPEED = 0.06D;

    private double xPos;
    private double yPos;
    private double rot;
    private double fov;
    private boolean forward;
    private boolean back;
    private boolean left;
    private boolean right;
    private boolean clicked;
    private boolean movement;
    private boolean turnMovement;
    private boolean hasMoved;
    private boolean hasTurned;
    private Level level;
    private Robot r;
    private Screen screen;
    private Timer timer;

    public Player(Pane pane, Level level) {
        super(pane);
        this.level = level;
        xPos = (double)level.getSpawnX() + 0.5D;
        yPos = (double)level.getSpawnY() + 0.5D;
        rot = 0.0D;
        fov = -0.7D;
        timer = new Timer();

        try {
            r = new Robot();
        } catch (AWTException var4) {
            throw new RuntimeException("Robot failed to instantiate");
        }

        screen = new Screen(Main.WIDTH, Main.HEIGHT);
        pane.getChildren().add(screen);
        setCursor(Cursor.NONE);
        setOnKeyPressed((event) -> {
            switch(event.getCode()) {
                case W:
                    forward = true;
                    break;
                case A:
                    left = true;
                    break;
                case S:
                    back = true;
                    break;
                case D:
                    right = true;
                    break;
                case ESCAPE:
                    Platform.runLater(() -> {
                        try {
                            Main.window.setScene(ControllerEscape.escapeScene(this));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    });
            }

        });
        setOnKeyReleased((event) -> {
            switch(event.getCode()) {
                case W:
                    forward = false;
                    break;
                case A:
                    left = false;
                    break;
                case S:
                    back = false;
                    break;
                case D:
                    right = false;
            }

        });
        setOnMouseMoved((event) -> {
            if (!clicked) {
                int moved = ((int)Main.window.getX() * 2 + Main.WIDTH) / 2 - (int)event.getScreenX();
                if (turnMovement) {
                    rot += (double)moved * 0.001D;
                    hasTurned = true;
                }

                r.mouseMove(moved + (int)event.getScreenX(), ((int)Main.window.getY() * 2 + 500) / 2);
            }

        });
        setOnMousePressed((event) -> {
            clicked = !clicked;
            setCursor(clicked ? Cursor.DEFAULT : Cursor.NONE);
        });
        movement = true;
        turnMovement = true;
        timer.schedule(new GameLoop(), 0L, LOOP_EVERY_X_MILLIS);
    }

    public void update() {
        if (movement) {
            byte[][] map = level.getMap();
            double hMove = Math.cos(rot) * SPEED;
            double vMove = Math.sin(rot) * SPEED;

            // xor so that if both are pressed it doesn't move
            if (forward ^ back) {
                if (forward) {
                    if (map[(int)(xPos + hMove)][(int)yPos] != 1) {
                        xPos += hMove;
                    }

                    if (map[(int)xPos][(int)(yPos + vMove)] != 1) {
                        yPos += vMove;
                    }
                } else {
                    if (map[(int)(xPos - hMove)][(int)yPos] != 1) {
                        xPos -= hMove;
                    }

                    if (map[(int)xPos][(int)(yPos - vMove)] != 1) {
                        yPos -= vMove;
                    }
                }

                hasMoved = true;
            }

            if (left ^ right) {
                if (left) {
                    if (map[(int)(xPos - vMove)][(int)yPos] != 1) {
                        xPos -= vMove;
                    }

                    if (map[(int)xPos][(int)(yPos + hMove)] != 1) {
                        yPos += hMove;
                    }
                } else {
                    if (map[(int)(xPos + vMove)][(int)yPos] != 1) {
                        xPos += vMove;
                    }

                    if (map[(int)xPos][(int)(yPos - hMove)] != 1) {
                        yPos -= hMove;
                    }
                }

                hasMoved = true;
            }
        }

    }

    public double getXPos() {
        return xPos;
    }

    public double getYPos() {
        return yPos;
    }

    public double getRot() {
        return rot;
    }

    public double getFov() {
        return fov;
    }

    public Level getLevel() {
        return level;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setMovement(boolean movement) {
        this.movement = movement;
    }

    public void setTurnMovement(boolean turnMovement) {
        this.turnMovement = turnMovement;
    }

    public void setLevel(Level level) {
        this.level = level;
        xPos = (double)level.getSpawnX() + 0.5D;
        yPos = (double)level.getSpawnY() + 0.5D;
        rot = 0.0D;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public boolean hasTurned() {
        return hasTurned;
    }

    public abstract void updateScreen();

    public int getDistanceToEnd() {
        int xDis = (getLevel().getEndX() - (int)xPos) * (getLevel().getEndX() - (int)xPos);
        int yDis = (getLevel().getEndY() - (int)yPos) * (getLevel().getEndY() - (int)yPos);
        return (int)Math.sqrt((double)(xDis + yDis));
    }

    public abstract boolean hasWon();

    public abstract void endGame();

    public void pause() {
        timer.cancel();
    }

    public void resume() {
        timer = new Timer();
        timer.schedule(new GameLoop(), 0L, LOOP_EVERY_X_MILLIS);
    }

    class GameLoop extends TimerTask {

        private long lastPrint = 0;
        private int frames = 0;

        public void run() {
            if(System.currentTimeMillis() - lastPrint >= 1000) {
                debug(frames + " fps");
                frames = 0;
                lastPrint = System.currentTimeMillis();
            }
            update();
            updateScreen();
            if (hasWon()) {
                timer.cancel();
                setCursor(Cursor.DEFAULT);
                endGame();
            }
            frames++;
        }

        private void debug(String message) {
        //    System.out.println(message);
        }
    }
}
