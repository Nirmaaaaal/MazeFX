package com.vettiankal.mazefx.game;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Level {

    // Array to link level to texture
    private static Texture[] textures = new Texture[] {
            Texture.ONE,
            Texture.TWO,
            Texture.THREE,
            Texture.FOUR,
            Texture.FIVE,
            Texture.SIX,
            Texture.SEVEN
    };

    // Compressed version of the map for sending over a network without fragmentation
    private byte[] map;

    // 2D version of the map for easy/quick calculations
    private byte[][] maps;


    private int spawnX;
    private int spawnY;
    private int endX;
    private int endY;
    private int level;

    public Level(int level) {
        if (level < 1) {
            level = 1;
        }

        if (level > 7) {
            level = 7;
        }

        this.level = level;
        this.maps = generateMap(level * 8);
        this.map = deflate(maps);
        this.maps[endX][endY] = 2;
    }

    private Level() {
    }

    public Level(byte[] deflatedMap) {
        if (deflatedMap.length != 370) {
            throw new RuntimeException("byte array in com.vettiankal.mazefx.game.Level constructor is not valid");
        } else {
            this.spawnX = deflatedMap[365];
            this.spawnY = deflatedMap[366];
            this.endX = deflatedMap[367];
            this.endY = deflatedMap[368];
            this.level = deflatedMap[369];
            this.map = deflatedMap;
            this.maps = inflate(map);
            this.maps[endX][endY] = 2;
        }
    }

    // Algorithm from https://en.wikipedia.org/wiki/Maze_generation_algorithm#Randomized_Prim.27s_algorithm
    // Leaves the edges of the maze as 1's to represent the outter walls
    private byte[][] generateMap(int size) {
        ArrayList<Point> walls = new ArrayList<>();
        byte[][] coords = new byte[size][size];

        for(int i = 0; i < size; ++i) {
            for(int j = 0; j < size; j++) {
                coords[i][j] = 1;
            }
        }

        Random r = new Random();
        spawnX = r.nextInt(size - 2) + 1;
        spawnY = r.nextInt(size - 2) + 1;
        coords[spawnX][spawnY] = 0;
        if (spawnX + 1 < size - 1) {
            walls.add(new Point(spawnX + 1, spawnY));
        }

        if (spawnX - 1 > 0) {
            walls.add(new Point(spawnX - 1, spawnY));
        }

        if (spawnY + 1 < size - 1) {
            walls.add(new Point(spawnX, spawnY + 1));
        }

        if (spawnY - 1 > 0) {
            walls.add(new Point(spawnX, spawnY - 1));
        }

        for(int r2; !walls.isEmpty(); walls.remove(r2)) {
            r2 = r.nextInt(walls.size());
            Point random = walls.get(r2);
            int surround = 0;
            if (random.x + 1 < size - 1 && coords[random.x + 1][random.y] == 0) {
                ++surround;
            }

            if (random.x - 1 > 0 && coords[random.x - 1][random.y] == 0) {
                ++surround;
            }

            if (random.y + 1 < size - 1 && coords[random.x][random.y + 1] == 0) {
                ++surround;
            }

            if (random.y - 1 > 0 && coords[random.x][random.y - 1] == 0) {
                ++surround;
            }

            if (surround == 1) {
                coords[random.x][random.y] = 0;
                this.endX = random.x;
                this.endY = random.y;
                if (random.x + 1 < size - 1 && coords[random.x + 1][random.y] == 1) {
                    walls.add(new Point(random.x + 1, random.y));
                }

                if (random.x - 1 > 0 && coords[random.x - 1][random.y] == 1) {
                    walls.add(new Point(random.x - 1, random.y));
                }

                if (random.y + 1 < size - 1 && coords[random.x][random.y + 1] == 1) {
                    walls.add(new Point(random.x, random.y + 1));
                }

                if (random.y - 1 > 0 && coords[random.x][random.y - 1] == 1) {
                    walls.add(new Point(random.x, random.y - 1));
                }
            }
        }

        return convertTo56(coords);
    }

    // Compresses the 2D map to a 1D 370 byte array
    private byte[] deflate(byte[][] map) {
        byte[] doneMap = new byte[370];

        //Makes the last 4 spots on the map 1's
        doneMap[364] = 15;
        doneMap[365] = (byte)spawnX;
        doneMap[366] = (byte)spawnY;
        doneMap[367] = (byte)endX;
        doneMap[368] = (byte)endY;
        doneMap[369] = (byte)level;
        byte add = 0;
        int count = 7;
        int addCount = 0;

        // Since the map is represented as 1s and 0s we convert the bytes to bit sets
        for(int i = 1; i < 55; i++) {
            for(int j = 1; j < 55; count--, j++) {
                add = (byte)(add | map[i][j] << count);
                if (count == 0) {
                    count = 8;
                    doneMap[addCount] = add;
                    add = 0;
                    addCount++;
                }
            }
        }

        doneMap[addCount] |= add;
        return doneMap;
    }

    private byte[][] inflate(byte[] map) {
        byte[][] doneMap = new byte[56][56];

        for(int i = 0; i < 56; i++) {
            doneMap[0][i] = 1;
            doneMap[55][i] = 1;
            doneMap[i][0] = 1;
            doneMap[i][55] = 1;
        }

        for(int j = 0; j < map.length; j++) {
            for(int k = 7; k >= 0; k--) {
                int pob = j * 8 + 7 - k;
                doneMap[pob / 54 + 1][pob % 54 + 1] = (byte)(map[j] >> k & 1);
            }
        }

        return doneMap;
    }

    private static byte[][] convertTo56(byte[][] map) {
        if (map.length == 56) {
            return map;
        }

        byte[][] doneMap = new byte[56][56];
        for (int i = 0; i < 56; i++) {
            for (int j = 0; j < 56; j++) {
                if (i < map.length && j < map.length) {
                    doneMap[i][j] = map[i][j];
                } else {
                    doneMap[i][j] = 1;
                }
            }
        }
        return doneMap;
    }

    public static Level getTutorial() {
        Level l = new Level();
        l.spawnX = 1;
        l.spawnY = 1;
        l.endX = 6;
        l.endY = 6;
        l.level = 1;
        l.maps = convertTo56(new byte[][]{
                {1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 1, 1, 1},
                {1, 0, 1, 1, 0, 1, 1, 1},
                {1, 0, 1, 0, 0, 0, 1, 1},
                {1, 0, 1, 0, 1, 0, 0, 1},
                {1, 0, 1, 0, 0, 1, 1, 1},
                {1, 0, 0, 1, 0, 0, 2, 1},
                {1, 1, 1, 1, 1, 1, 1, 1}});
        l.map = l.deflate(l.maps);
        l.maps[l.endX][l.endY] = 2;
        return l;
    }

    public static Level getLobby() {
        Level l = new Level();
        l.spawnX = 4;
        l.spawnY = 4;
        l.endX = 10;
        l.endY = 10;
        l.level = 0;
        l.maps = convertTo56(new byte[][]{
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 1, 0, 0, 1, 1, 0, 1},
                {1, 0, 1, 1, 0, 0, 1, 1, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 1, 0, 0, 1, 1, 0, 1},
                {1, 0, 1, 1, 0, 0, 1, 1, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}});
        l.map = l.deflate(l.maps);
        return l;
    }

    public byte[][] getMap() {
        return this.maps;
    }

    public byte[] getDeflatedMap() {
        return this.map;
    }

    public int getSpawnX() {
        return this.spawnX;
    }

    public int getSpawnY() {
        return this.spawnY;
    }

    public int getEndX() {
        return this.endX;
    }

    public int getEndY() {
        return this.endY;
    }

    public int getLevel() {
        return this.level;
    }

    public Texture getTexture() {
        if(level < 1 || level > 7) {
            return Texture.END;
        }
        return textures[level - 1];
    }
}