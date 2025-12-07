package com.timer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        String dirPath = System.getProperty("user.dir") + File.separator + "timer" + File.separator + "src";
        String videoName = "testVideo";
        String videoType = "mp4";
        String videoPath = dirPath + File.separator + "main" + File.separator + "resources" + File.separator + videoName + "." + videoType;
        videoObject video = new videoObject(videoName, videoPath, videoType);
        try {
            VideoData vidData = new VideoData(video);
            String outDir = dirPath + File.separator + "main" + File.separator + "resources" + File.separator + "imageResults";
            Path imagePath = Path.of(outDir);
            vidData.saveFramesAsImages(imagePath, "png");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageProcessor processor = new ImageProcessor(video);
        String imgPath = dirPath + File.separator + "main" + File.separator + "resources" + File.separator + "imageResults" + File.separator + "testVideo_frame_000000.png";
        try {
            processor.processImage(imgPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}