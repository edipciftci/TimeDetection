package com.timer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageProcessor {

    private videoObject video;

    public ImageProcessor(videoObject video){
        this.video = video;
    }

    public void processImage(String imgPath) throws IOException{

        BufferedImage img = ImageIO.read(new File(imgPath));
        int width = img.getWidth();
        int height = img.getHeight();
        String[] parts = imgPath.split(File.separator);
        String ID = parts[parts.length - 1];
        Frame frame = new Frame(ID, width, height);
        this.video.addFrame(frame);

        int[] argb = img.getRGB(0, 0, width, height, null, 0, width);

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int pixel = argb[index++];

                int r = (pixel >>> 16) & 0xFF;
                int g = (pixel >>> 8)  & 0xFF;
                int b =  pixel         & 0xFF;

                int rgb = (r << 16) | (g << 8) | b;

                frame.setPixelRGB(x, y, rgb);
            }
        }
        System.out.println("Here");
    }

}
