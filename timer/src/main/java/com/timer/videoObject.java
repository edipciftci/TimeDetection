package com.timer;

public class videoObject {

    private Frame[] frames;
    private String name, path, type;
    private int numOfFrames;
    private int index = 0;

    public videoObject(String name, String path, String type){
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public String getVideoName(){
        return this.name;
    }

    public String getTypedVideoName(){
        return this.name + "." + this.type;
    }

    public String getVideoType(){
        return this.type;
    }

    public String getVideoPath(){
        return this.path;
    }

    public void setNumOfFrames(int numOfFrames){
        this.numOfFrames = numOfFrames;
        this.frames = new Frame[numOfFrames];
    }

    public void addFrame(Frame frame){
        this.frames[this.index] = frame;
        this.index++;
    }

}
