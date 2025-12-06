package com.timer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageProcessor {

    public ImageProcessor(){}

    public void processImage(String imgPath) throws IOException{

        BufferedImage img = ImageIO.read(new File(imgPath));
        int width = img.getWidth();
        int height = img.getHeight();
        String[] parts = imgPath.split(File.separator);
        String ID = parts[parts.length - 1];
        Frame frame = new Frame(ID, width, height);

        int[] argb = img.getRGB(0, 0, width, height, null, 0, width);

        int xPos = 0;
        int yPos = 0;

        for (int pixel : argb) {

            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8)  & 0xFF;
            int b =  pixel        & 0xFF;

            frame.setPixelRGBTable();
            
            xPos++;
            yPos++;
            xPos = xPos % width;
            yPos = yPos % height;
        }


        // int[] temp = img.getRGB(0, 0, width, height, null, 0, width);

        // String str = "";
        // for (int i = 0; i < temp.length; i=i+3) {
        //     str += "Red:\t" + temp[i];
        //     str += "\t\tBlue:\t" + temp[i+1];
        //     str += "\t\tGreen:\t" + temp[i+2];
        //     str += "\n";
        // }

        // System.out.println(str);
    }

}
