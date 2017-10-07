package com.vettiankal.mazefx;

import com.vettiankal.mazefx.game.Level;
import com.vettiankal.mazefx.server.PlayerInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ServerMain {

    public static ArrayList<Level> levels = new ArrayList<>();
    private static volatile boolean isRunning = true;
    private static int players;
    private static int time;
    private static final Scanner scanner = new Scanner(System.in);
    private static final ArrayList<String> bannedIP = new ArrayList<>();

    public static void main(String... args) throws IOException {
        players = getIntegerFromUser(2, 10, "Enter value of how many players to start the game (2-10): ");
        time = getIntegerFromUser(60, 3600, "Enter value of how much time to run the game in seconds (60-3600): ");
        System.out.println("Server started ");

        try {
            System.out.println("Server IP: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                if (PlayerInfo.getNumberOfPlayers() == players) {
                    time--;
                    if (time % 60 == 0) {
                        System.out.println(time / 60 + " minutes remaining");
                    } else if (time < 15) {
                        System.out.println(time + " seconds remaining");
                    }

                    if (time < -5) {
                        System.exit(0);
                    }
                }

            }
        }, 1000L, 1000L);
        new Thread(() -> {
            while (isRunning) {
                //TODO make a proper command system
                String input = scanner.nextLine();
                String[] arg = input.split(" ");
                if (arg[0].equalsIgnoreCase("ban")) {
                    if (arg.length > 1) {

                        // Make sure input thread has sole access of the list before modifying it
                        synchronized (bannedIP) {
                            bannedIP.add(arg[1]);
                        }

                        System.out.println(arg[1] + " has been banned");
                    } else {
                        System.out.println("Usage: ban (IP address)");
                    }
                }

                if (arg[0].equalsIgnoreCase("unban")) {
                    if (arg.length > 1) {
                        synchronized (bannedIP) {
                            if (bannedIP.size() == 0) {
                                continue;
                            }

                            Iterator i = bannedIP.iterator();

                            while (i.hasNext()) {
                                String ip = (String) i.next();
                                if (ip.equalsIgnoreCase(arg[1])) {
                                    i.remove();
                                    System.out.println(ip + " has been unbanned");
                                }
                            }
                        }
                    } else {
                        System.out.println("Usage: unban (IP address)");
                    }
                }

                if (arg[0].equalsIgnoreCase("info")) {
                    if (PlayerInfo.getNumberOfPlayers() == 0) {
                        continue;
                    }

                    if (arg.length > 1) {
                        System.out.println(PlayerInfo.getPlayerInfo(arg[1]));
                    } else {
                        System.out.println("Usage: info (Player name)");
                    }
                }

                if (arg[0].equalsIgnoreCase("time")) {
                    System.out.println(time);
                }

                if (arg[0].equalsIgnoreCase("send")) {
                    if (PlayerInfo.getNumberOfPlayers() == 0) {
                        continue;
                    }

                    if (arg.length == 3) {
                        int level = 0;

                        try {
                            level = Integer.parseInt(arg[2]);
                        } catch (NumberFormatException var6) {
                            System.out.println("Level must be an integer");
                        }

                        if (level > 0 && level < 8) {
                            PlayerInfo player = PlayerInfo.getPlayer(arg[1]);
                            if (player != null) {
                                player.setLevel((byte) level);
                                System.out.println(player.getName() + " was send to level " + level);
                            } else {
                                System.out.println("Player not found");
                            }
                        } else {
                            System.out.println("Level must be between 1 and 7");
                        }
                    } else {
                        System.out.println("Usage: send (IP address) (level)");
                    }
                }

                if (!arg[0].equalsIgnoreCase("list") || PlayerInfo.getNumberOfPlayers() != 0) {
                    System.out.println(PlayerInfo.getNames());
                }
            }

        }).start();
        DatagramSocket serverSocket = new DatagramSocket(8901);
        byte[] receiveData = new byte[PlayerInfo.PLAYER_INFO_BYTE_LENGTH];
        levels.add(Level.getLobby());

        for (int i = 1; i < 8; ++i) {
            levels.add(new Level(i));
        }

        outter:
        while (isRunning) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            byte[] data = receivePacket.getData();


            for (String address : bannedIP) {
                if (address.equalsIgnoreCase(receivePacket.getAddress().getHostAddress())) {
                    System.out.println(address + " tried connecting but is banned");
                    continue outter;
                }
            }

            if (data[0] == -1 && data.length > 1) {
                serverSocket.send(new DatagramPacket(new byte[]{data[1]}, 1, receivePacket.getAddress(), receivePacket.getPort()));
                System.out.println("Sent ping packet to " + receivePacket.getAddress().getHostAddress());
            } else {
                PlayerInfo player = PlayerInfo.getPlayer(receivePacket.getAddress());
                if (player != null) {
                    player.updateInfo(receiveData);
                    if (PlayerInfo.getNumberOfPlayers() == players && player.getLevel() == 0) {
                        player.setLevel((byte) 1);
                        System.out.println(player.getName() + " has been moved from the lobby");
                    }
                } else {
                    if (PlayerInfo.getNumberOfPlayers() >= players) {
                        continue;
                    }

                    PlayerInfo.add(new PlayerInfo(receiveData[0], receiveData[1], receiveData[2], new String(receiveData, 3, 10), receivePacket.getAddress()));
                    player = PlayerInfo.getPlayer(receivePacket.getAddress());
                    System.out.println(player.getName() + " has connected");
                }

                byte[] sendData = totalBytes(PlayerInfo.getNumberOfPlayers() < players ? Level.getLobby().getDeflatedMap() : levels.get(player.getLevel()).getDeflatedMap(), time, PlayerInfo.getList(player));
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                serverSocket.send(sendPacket);
            }
        }

    }

    private static byte[] totalBytes(byte[] map, int timeInSeconds, byte[] playerInfo) {
        byte[] packet = new byte[playerInfo.length + 376];
        packet[0] = (byte)(packet.length >> 8 & 255);
        packet[1] = (byte)(packet.length & 255);

        System.arraycopy(map, 0, packet, 2, 370);
        for(int i = 0; i < 4; ++i) {
            packet[map.length + i + 2] = (byte)(timeInSeconds >> 8 * (3 - i) & 255);
        }
        System.arraycopy(playerInfo, 0, packet, map.length + 6, playerInfo.length);

        return packet;
    }

    private static int getIntegerFromUser(int min, int max, String message) {
        boolean valid = false;
        int value = 0;
        while (!valid) {
            System.out.println(message);
            try {
                value = Integer.parseInt(scanner.nextLine());
                if (value >= min && value <= max) {
                    valid = true;
                }
            } catch (NumberFormatException ignored) {

            }
        }
        return value;
    }
}
