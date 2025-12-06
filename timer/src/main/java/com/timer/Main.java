package com.timer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        String dirPath = System.getProperty("user.dir") + File.separator + "timer" + File.separator + "src";
        String videoPath = dirPath + File.separator + "main" + File.separator + "resources" + File.separator + "testVideo.mp4";
        // try {
        //     VideoData vidData = new VideoData(videoPath);
        //     String outDir = dirPath + File.separator + "main" + File.separator + "resources" + File.separator + "imageResults";
        //     Path imagePath = Path.of(outDir);
        //     vidData.saveFramesAsImages(imagePath, "png");
        // } catch (Exception e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

        ImageProcessor processor = new ImageProcessor();
        String imgPath = dirPath + File.separator + "main" + File.separator + "resources" + File.separator + "imageResults" + File.separator + "frame_000000.png";
        try {
            processor.processImage(imgPath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}