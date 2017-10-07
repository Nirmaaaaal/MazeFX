package com.vettiankal.mazefx.server;

import com.vettiankal.mazefx.ServerMain;
import com.vettiankal.mazefx.game.Level;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PlayerInfo implements Comparable {

    public static final int PLAYER_INFO_BYTE_LENGTH = 13;

    private byte xPos;
    private byte yPos;
    private byte level;
    private String name;
    private static HashMap<InetAddress, PlayerInfo> list = new HashMap<>();
    private InetAddress address;
    private byte setLevel;

    public PlayerInfo(byte xPos, byte yPos, byte level, String name, InetAddress address) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.level = level;
        this.name = name;
        this.address = address;
    }

    private byte[] convertToBytes() {
        byte[] nameBytes = name.getBytes();
        byte[] list = new byte[PLAYER_INFO_BYTE_LENGTH];
        list[0] = xPos;
        list[1] = yPos;
        list[2] = level;

        for(int i = 0; i < 10; i++) {
            if (nameBytes.length > i) {
                list[3 + i] = nameBytes[i];
            } else {
                list[3 + i] = 32;
            }
        }

        return list;
    }

    public void setLevel(byte level) {
        this.setLevel = level;
    }

    public static byte[] getList(PlayerInfo player) {
        ArrayList<PlayerInfo> players = new ArrayList<>(list.values());
        Collections.sort(players);
        byte[] list = new byte[PLAYER_INFO_BYTE_LENGTH * players.size()];
        int counter = 0;

        for (PlayerInfo pi : players) {
            byte[] temp = pi.convertToBytes();
            if(pi == player) {
                temp[2] = -1;
            }
            for(byte b : temp) {
                list[counter] = b;
                counter++;
            }
        }

        return list;
    }

    public static PlayerInfo getPlayer(InetAddress address) {
        return list.get(address);
    }

    public static int getNumberOfPlayers() {
        return list.size();
    }

    public void updateInfo(byte[] packet) {
        xPos = packet[0];
        yPos = packet[1];
        if (level != packet[2]) {
            System.out.println(name + " has moved from level " + level + " to " + packet[2]);
            level = packet[2];
        }

    }

    public static void add(PlayerInfo player) {
        list.put(player.address, player);
    }

    public int getLevel() {
        Level l = ServerMain.levels.get(level);
        if (xPos == l.getEndX() && yPos == l.getEndY()) {
            level++;
        }

        if (setLevel != 0) {
            level = setLevel;
        }

        setLevel = 0;
        return level;
    }

    public int compareTo(Object o) {
        if (o != null && o instanceof PlayerInfo) {
            PlayerInfo other = (PlayerInfo)o;
            if (other.level < level) {
                return -1;
            } else if (other.level > level) {
                return 1;
            } else {
                return other.getDistanceToEnd() < getDistanceToEnd() ? 1 : -1;
            }
        } else {
            return -1;
        }
    }

    private double getDistanceToEnd() {
        double xDis = (double)((ServerMain.levels.get(level).getEndX() - xPos) * ServerMain.levels.get(level).getEndX() - xPos);
        double yDis = (double)((ServerMain.levels.get(level).getEndY() - yPos) * ServerMain.levels.get(level).getEndY() - yPos);
        return Math.sqrt(xDis + yDis);
    }

    public String getName() {
        return name;
    }

    public static String getPlayerInfo(String name) {
        StringBuffer info = new StringBuffer("");
        list.values().stream().filter((pi) -> pi.getName().trim().equalsIgnoreCase(name.trim()))
                .forEach((pi) -> info.append("\nPlayer Name: ").append(pi.name).append("\nX Position: ").append(pi.xPos).append("\nY Position: ").append(pi.yPos).append("\nLevel: ").append(pi.level).append("\nDistance to End of Level: ").append(pi.getDistanceToEnd()).append("\nIP Address: ").append(pi.address.getHostAddress()).append("\n\n"));
        if (info.length() == 0) {
            info.append("Player not found");
        }

        return info.toString();
    }

    public static PlayerInfo getPlayer(String address) {
        try {
            return list.get(InetAddress.getByName(address));
        } catch (UnknownHostException var2) {
            return null;
        }
    }

    public static String getNames() {
        ArrayList<PlayerInfo> players = new ArrayList<>(list.values());
        Collections.sort(players);
        StringBuilder names = new StringBuilder("\n");

        for (PlayerInfo pi : players) {
            names.append("\n").append(pi.getName());
        }

        names.append("\n");
        return names.toString();
    }
}
