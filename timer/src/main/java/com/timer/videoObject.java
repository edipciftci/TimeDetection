package com.timer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class videoObject {

    private Frame[] frames;
    private String name, path, dirPath, type;
    private int numOfFrames;
    private int index = 0;

    public videoObject(String name, String path, String dirPath, String type){
        this.name = name;
        this.path = path;
        this.dirPath = dirPath;
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

    public void getVideoData(){
        try {
            VideoData vidData = new VideoData(this);
            String outDir = this.dirPath +  File.separator + "imageResults";
            Path imagePath = Path.of(outDir);
            // vidData.saveFramesAsImages(imagePath, "png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processImages() throws IOException{

        ImageProcessor processor = new ImageProcessor(this);

        Path dir = Paths.get(this.dirPath + File.separator + "imageResults");

        try (
            Stream<String> names =
            Files.list(dir)
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .sorted()
            ) {
                names.forEach(img -> {
                    try {
                        processor.processImage(this.dirPath + File.separator + "imageResults" + File.separator + img);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
    }

}
