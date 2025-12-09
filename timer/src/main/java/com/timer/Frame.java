package com.timer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Frame {

    private final String FrameID;
    private final int width, height;
    private int[] pixelRGBTable, pixelWBCTable;
    private final videoObject video;

    public Frame(String FrameID, int width, int height, videoObject video){
        this.FrameID = FrameID;
        this.width = width;
        this.height = height;

        this.pixelRGBTable = new int[ width * height ];
        this.pixelWBCTable = new int[ width * height ];

        this.video = video;
    }

    public void setPixelRGBTable(int[] table){this.pixelRGBTable=table;}

    public int[] getPixelRGBTable(){return this.pixelRGBTable;}

    public void setPixelRGB(int xPos, int yPos, int RGB, int brightness){
        this.pixelRGBTable[yPos*width + xPos] = RGB;
        if (brightness < 60){
            this.pixelWBCTable[yPos*width + xPos] = -1; // Black
        } else if (brightness > 200){
            this.pixelWBCTable[yPos*width + xPos] = 1; // White
        } else {
            this.pixelWBCTable[yPos*width + xPos] = 0; // Colored
        }
    }

    public int getPixelRGB(int xPos, int yPos){return this.pixelRGBTable[yPos*this.width + xPos];}

    // TODO: Delete Later
    public void printWBCTable(){
        String str = "\n\n";
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (this.pixelWBCTable[i*this.width+j] > -1){
                    str += " ";
                }
                str += this.pixelWBCTable[i*this.width+j];
            }
            str += "\n";
        }
        
        try  {
            String outputFilePath = this.video.getVideoDirPath() + File.separator + "wbc_table.txt";
            Files.writeString(java.nio.file.Paths.get(outputFilePath), str);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

}
