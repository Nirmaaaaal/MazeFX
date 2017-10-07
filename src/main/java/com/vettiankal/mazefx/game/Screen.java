package com.vettiankal.mazefx.game;

import com.vettiankal.mazefx.Main;
import com.vettiankal.mazefx.player.Player;
import com.vettiankal.mazefx.server.PlayerInfo;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.awt.*;
import java.util.ArrayList;

public class Screen extends Canvas {

    // Default resolution scale
    public static int SCALE = 2;

    public Screen(int width, int height) {
        super((double)width, (double)height);
        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    public int[] update(Player player, byte[] playerData) {
        int[] pixels = new int[Main.WIDTH * Main.HEIGHT];
        byte[][] map = player.getLevel().getMap();

        // Give the ceiling and floor a grey tone
        for(int x = 0; x < pixels.length; ++x) {
            pixels[x] = x < pixels.length / 2 ? Color.DARK_GRAY.getRGB() : Color.GRAY.getRGB();
        }

        // Loops through the width of the screen so it renders by column
        outter:
        for(int x = 0; x + SCALE - 1 < (int)getWidth(); x += SCALE) {
            int mapX = (int)player.getXPos();
            int mapY = (int)player.getYPos();
            double column = 2.0D * (double)x / getWidth() - 1.0D;

            // Calculate what direction they are looking in with respect to the map
            double xDir = Math.cos(player.getRot()) - Math.sin(player.getRot()) * player.getFov() * column;
            double yDir = Math.sin(player.getRot()) + Math.cos(player.getRot()) * player.getFov() * column;
            double sideDistX;
            byte stepX;
            if (xDir < 0.0D) {
                stepX = -1;
                sideDistX = (player.getXPos() - (double)mapX) * 1.0D / Math.abs(xDir);
            } else {
                stepX = 1;
                sideDistX = ((double)(mapX + 1) - player.getXPos()) * 1.0D / Math.abs(xDir);
            }

            double sideDistY;
            byte stepY;
            if (yDir < 0.0D) {
                stepY = -1;
                sideDistY = (player.getYPos() - (double)mapY) * 1.0D / Math.abs(yDir);
            } else {
                stepY = 1;
                sideDistY = ((double)(mapY + 1) - player.getYPos()) * 1.0D / Math.abs(yDir);
            }

            // Calculates whether to make the block face dark or not
            boolean space;
            boolean light;
            do {
                light = sideDistX < sideDistY;
                if (light) {
                    sideDistX += 1.0D / Math.abs(xDir);
                    mapX += stepX;
                } else {
                    sideDistY += 1.0D / Math.abs(yDir);
                    mapY += stepY;
                }

                space = player.getLevel().getMap()[mapX][mapY] == 0;
            } while(space);

            // Calculates the distance to the wall
            double wallDis;
            double wallX;
            double xDis;
            if (light) {
                xDis = ((double)mapX - player.getXPos() + (double)((1 - stepX) / 2)) / xDir;
                wallDis = Math.abs(xDis);
                wallX = player.getYPos() + xDis * yDir - Math.floor(player.getYPos() + xDis * yDir);
            } else {
                xDis = ((double)mapY - player.getYPos() + (double)((1 - stepY) / 2)) / yDir;
                wallDis = Math.abs(xDis);
                wallX = player.getXPos() + xDis * xDir - Math.floor(player.getXPos() + xDis * xDir);
            }

            // Gets the texture to display for the column
            Texture tex = player.getLevel().getTexture();
            if (map[mapX][mapY] == 2) {
                tex = Texture.END;
            }

            // Calculates what part of the texture to render
            int texX = (int)(wallX * (double)tex.getSize());
            if (light && xDir > 0.0D || !light && yDir < 0.0D) {
                texX = tex.getSize() - texX - 1;
            }

            // Calculates how much of the column to draw on based on the distance to the wall
            int drawLine = wallDis > 0.0D ? (int)(getHeight() / wallDis) : (int)getHeight();
            int yTop = (-drawLine + (int)getHeight()) / 2;
            int yBot = yTop + drawLine;
            int botCut = 0;
            int topCut = 0;
            if (yTop < 0) {
                topCut = -yTop;
                yTop = 0;
            }

            if (yBot >= (int)getHeight()) {
                botCut = yBot - (int)getHeight();
                yBot = (int)getHeight() - 1;
            }

            for(int y = yTop; y < yBot; y += SCALE) {
                int texY = (int)(((double)(topCut + y) - (double)yTop * 1.0D) / (double)(topCut + botCut + yBot - yTop) * (double)tex.getSize());
                int color = tex.getPixels()[texX + texY * tex.getSize()];
                if (!light) {
                    // -8421505 makes the most significant RBG bit 0 which shades certain sides of the blocks
                    color = color >> 1 & -8421505;
                }

                // Adjusts resolution accordingly
                if (SCALE <= 1) {
                    pixels[x + y * (int)getWidth()] = color;
                } else {
                    for(int i = 0; i < SCALE; i++) {
                        for(int j = 0; j < SCALE; j++) {
                            int z = x + j + (y + i) * (int)getWidth();
                            if(z >= 0 && z < pixels.length) {
                                pixels[z] = color;
                            }
                        }
                    }
                }
            }
        }

        if (playerData == null) {
            pixels = addMiniMap(pixels, player, map);
        } else {
            pixels = addMiniMap(pixels, player, playerData, map);
        }

        return pixels;
    }

    public int[] addMiniMap(int[] pixels, Player player, byte[] playerData, byte[][] map) {
        ArrayList<Point> locations = new ArrayList<>();

        for(int data = 2; data < playerData.length; data += PlayerInfo.PLAYER_INFO_BYTE_LENGTH) {
            if (playerData[data] == player.getLevel().getLevel()) {
                data = (int)player.getXPos() - playerData[data - 2];
                int yDis = (int)player.getYPos() - playerData[data - 1];
                if (Math.abs(data) <= 3 && Math.abs(yDis) <= 3) {
                    locations.add(new Point(-data, -yDis));
                }
            }
        }

        int xDis = player.getLevel().getEndX() - (int)player.getXPos();
        int yDis = player.getLevel().getEndY() - (int)player.getYPos();
        if (Math.abs(xDis) <= 3 && Math.abs(yDis) <= 3) {
            locations.add(new Point(xDis + 110, yDis + 110));
        }

        locations.add(new Point(0, 0));
        return this.addMiniMap(pixels, locations, player, map);
    }

    public int[] addMiniMap(int[] pixels, Player player, byte[][] map) {
        ArrayList<Point> location = new ArrayList<>();
        location.add(new Point(0, 0));
        int xDis = player.getLevel().getEndX() - (int)player.getXPos();
        int yDis = player.getLevel().getEndY() - (int)player.getYPos();
        if (Math.abs(xDis) <= 3 && Math.abs(yDis) <= 3) {
            location.add(new Point(xDis + 110, yDis + 110));
        }

        return this.addMiniMap(pixels, location, player, map);
    }

    private int[] addMiniMap(int[] pixels, ArrayList<Point> locations, Player player, byte[][] map) {
        int size = 77;
        int startX = (int)this.getWidth() - 1 - 10 - size;
        int startY = 10;

        for(int i = startX; i < startX + size; ++i) {
            for(int j = startY; j < startY + size; ++j) {
                int x = (i - startX) / (size / 7) - 3;
                int y = (j - startY) / (size / 7) - 3;

                try {
                    if (map[(int)player.getXPos() + y][(int)player.getYPos() + x] == 0) {
                        pixels[i + j * (int)this.getWidth()] = Color.WHITE.getRGB();
                    } else {
                        pixels[i + j * (int)this.getWidth()] = Color.BLACK.getRGB();
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    pixels[i + j * (int)this.getWidth()] = Color.BLACK.getRGB();
                }
            }
        }

        for (Point p : locations) {
            Color c = p.x == 0 && p.y == 0 ? Color.RED : Color.CYAN;
            if (p.x > 100 && p.y > 100) {
                p.x -= 110;
                p.y -= 110;
                c = Color.MAGENTA;
            }

            for (int k = 33 + p.y * 11; k < 44 + p.y * 11; k++) {
                for (int l = 33 + p.x * 11; l < 44 + p.x * 11; l++) {
                    pixels[startX + k + (startY + l) * (int) this.getWidth()] = c.getRGB();
                }
            }
        }

        return pixels;
    }

    public void drawTimer(String timer) {
        this.getGraphicsContext2D().setFont(new Font("Algerian", 18.0D));
        this.getGraphicsContext2D().fillText(timer, (double)((int)this.getWidth() - 88), 105.0D);
    }

    public void drawPlaces(byte[] playerData) {
        for(int i = 1; i <= 3; ++i) {
            if (playerData.length >= i * 13) {
                getGraphicsContext2D().setFont(new Font("Algerian", 16.0D));
                getGraphicsContext2D().fillText(i + ". " + new String(playerData, i * 13 - 10, 10), 0.0D, (double)((i - 1) * 30 + 15));
            }
        }

    }

    public void drawMessage(String message) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(javafx.scene.paint.Color.AQUA);
        gc.fillRoundRect(200.0D, 400.0D, 400.0D, 75.0D, 10.0D, 10.0D);
        gc.setFont(new Font("Arial", 18.0D));
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillText(message, 400.0D, 435.0D);
    }
}

