package com.timer;

import java.io.File;

public class Main {

    public static int[] ROI = new int[]{110, 120, 320, 330}; // Region of interest boundaries [xi, yi, xf, yf]

    public static void main(String[] args) throws Exception {

        String dirPath = System.getProperty("user.dir") + File.separator + "timer" + File.separator + "src";
        String vidDirPath = dirPath + File.separator + "main" + File.separator + "resources";
        String videoPath = vidDirPath + File.separator + "testVideo.mp4";
        videoObject video = new videoObject(videoPath, vidDirPath, ROI);

        video.processImages();

        video.getFrames()[0].printWBCTable();

    }
}
