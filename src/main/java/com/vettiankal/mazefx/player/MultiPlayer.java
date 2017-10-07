package com.vettiankal.mazefx.player;

import com.vettiankal.mazefx.Main;
import com.vettiankal.mazefx.controller.ControllerEnd;
import com.vettiankal.mazefx.game.Level;
import com.vettiankal.mazefx.server.PlayerInfo;
import javafx.application.Platform;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;

public class MultiPlayer extends Player {

    private String name;
    private InetAddress address;
    private DatagramSocket socket;
    private int check;
    private byte[] playerData;
    private int time;
    private PixelWriter pw;
    private static final DecimalFormat df = new DecimalFormat("00");
    private int millis;

    public MultiPlayer(String name, InetAddress address, DatagramSocket socket) {
        super(new Pane(), Level.getLobby());
        this.name = name + "          ";
        this.address = address;
        this.socket = socket;
        this.pw = getScreen().getGraphicsContext2D().getPixelWriter();
    }

    public void updateScreen() {
        // Every 10th loop it sends an update packet to the server
        if (check == 0) {
            // Loads byte array with our player data
            byte[] sendData = new byte[PlayerInfo.PLAYER_INFO_BYTE_LENGTH];
            sendData[0] = (byte)((int)getXPos());
            sendData[1] = (byte)((int)getYPos());
            sendData[2] = (byte)getLevel().getLevel();

            for(int i = 3; i < PlayerInfo.PLAYER_INFO_BYTE_LENGTH; ++i) {
                sendData[i] = name.getBytes()[i - 3];
            }

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, 8901);
            byte[] receiveData = new byte[506];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            // Send and receive data
            // TODO send/receive packets async so that high latency connections won't stall the main rendering thread
            try {
                socket.send(sendPacket);
                socket.receive(receivePacket);
            } catch (IOException var10) {
                Main.setMainScreen();
            }

            byte[] tempdata = receivePacket.getData();
            int length = (tempdata[0] & 255) << 8 | tempdata[1] & 255;
            if (length >= 379) {
                byte[] data = new byte[length];
                System.arraycopy(tempdata, 0, data, 0, length);
                time = 0;

                for(int a = 0; a < 4; a++) {
                    time |= (data[372 + a] & 255) << 8 * (3 - a);
                }

                if (millis == 0 && time > 0) {
                    millis = time;
                }

                playerData = new byte[data.length - 376];
                System.arraycopy(data, 376, playerData, 0, data.length - 376);

                if (data[371] != getLevel().getLevel()) {
                    byte[] deflatedMap = new byte[370];
                    System.arraycopy(data, 2, deflatedMap, 0, 370);
                    setLevel(new Level(deflatedMap));
                }
            }

            check = 10;
        }

        check--;
        int[] temp = getScreen().update(this, playerData);
        Platform.runLater(() -> {
            pw.setPixels(0, 0, Main.WIDTH, Main.HEIGHT, PixelFormat.getIntArgbPreInstance(), temp, 0, Main.WIDTH);
            getScreen().drawPlaces(playerData);
            getScreen().drawTimer(time / 60 + ":" + df.format((long)(time % 60)));
        });
    }

    public boolean hasWon() {
        return time == 0 || getLevel().getLevel() == 7 && (int)getXPos() == getLevel().getEndX() && (int)getYPos() == getLevel().getEndY();
    }

    private String[] playerNames() {
        String[] names = new String[playerData.length / PlayerInfo.PLAYER_INFO_BYTE_LENGTH];

        for(int i = 0; i < names.length; ++i) {
            names[i] = i + 1 + ". " + (new String(playerData, (i + 1) * PlayerInfo.PLAYER_INFO_BYTE_LENGTH - 10, 10)).trim();
        }

        return names;
    }

    public void endGame() {
        Platform.runLater(() -> {
            try {
                Main.window.setScene(ControllerEnd.endScene(millis * 1000 - time * 1000, getLevel().getLevel(), getDistanceToEnd(), playerNames()));
            } catch (IOException var2) {
                Main.setMainScreen();
            }

        });
    }
}
