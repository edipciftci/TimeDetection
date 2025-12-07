package com.timer;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String dirPath = System.getProperty("user.dir") + File.separator + "timer" + File.separator + "src";
        String vidDirPath = dirPath + File.separator + "main" + File.separator + "resources";
        String videoName = "testVideo";
        String videoType = "mp4";
        String videoPath = vidDirPath + File.separator + videoName + "." + videoType;
        videoObject video = new videoObject(videoName, videoPath, vidDirPath, videoType);

        video.getVideoData();

        video.processImages();

    }
}