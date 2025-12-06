package com.timer;

public class Frame {

    private String FrameID;
    private int width, height;
    private int[] pixelRGBTable;

    public Frame(String FrameID, int width, int height){
        this.FrameID = FrameID;
        this.width = width;
        this.height = height;

        this.pixelRGBTable = new int[ width * height ];
    }

    public void setPixelRGBTable(){}

    public int[] getPixelRGBTable(){
        return this.pixelRGBTable;
    }

    public void setPixelRGB(int xPos, int yPos, int RGB){
        int index = yPos*width + xPos;
        this.pixelRGBTable[index] = RGB;
    }

    public int[] getPixelRGB(int xPos, int yPos){
        return this.pixelRGBTable;
    }

}
