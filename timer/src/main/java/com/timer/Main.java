package com.timer;

import java.io.File;
import java.io.IOException;

public class Main {

    public static int[] ROI = new int[]{110, 120, 320, 330};

    public static void main(String[] args) throws IOException {

        String dirPath = System.getProperty("user.dir") + File.separator + "timer" + File.separator + "src";
        String vidDirPath = dirPath + File.separator + "main" + File.separator + "resources";
        String videoName = "testVideo";
        String videoType = "mp4";
        String videoPath = vidDirPath + File.separator + videoName + "." + videoType;
        videoObject video = new videoObject(videoName, videoPath, vidDirPath, videoType, ROI);

        video.getVideoData();

        video.processImages();

        Frame frame = video.getFrames()[0];
        frame.printWBCTable();

    }
}