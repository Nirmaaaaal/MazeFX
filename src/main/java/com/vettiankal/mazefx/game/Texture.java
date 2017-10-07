package com.vettiankal.mazefx.game;

import com.vettiankal.mazefx.Main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public enum Texture {

    ONE(Main.class.getClassLoader().getResource("one.png"), 1200),
    TWO(Main.class.getClassLoader().getResource("two.png"), 1800),
    THREE(Main.class.getClassLoader().getResource("three.png"), 300),
    FOUR(Main.class.getClassLoader().getResource("four.png"), 500),
    FIVE(Main.class.getClassLoader().getResource("five.png"), 1200),
    SIX(Main.class.getClassLoader().getResource("six.png"), 512),
    SEVEN(Main.class.getClassLoader().getResource("seven.png"), 1200),
    END(Main.class.getClassLoader().getResource("end.png"), 512);

    private int[] pixels;
    private int size;

    Texture(URL location, int size) {
        this.size = size;
        this.pixels = new int[size * size];

        try {
            BufferedImage image = ImageIO.read(location);
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        } catch (IOException var6) {
            throw new RuntimeException("Image not found");
        }
    }

    public int getSize() {
        return size;
    }

    public int[] getPixels() {
        return pixels;
    }
}
