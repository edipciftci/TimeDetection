package com.timer;

import java.io.File;

public class Main {

    public static int[] ROI = new int[]{110, 120, 320, 330}; // Region of interest boundaries [xi, yi, xf, yf]

    public static void main(String[] args) throws Exception {

        time("Run All 100 times", () -> runAll());

    }

    public static void time(String label, Runnable block) {
        long start = System.nanoTime();
        try {
            block.run();
        } finally {
            long end = System.nanoTime();
            long ms = (end - start) / 1_000_000;
            System.out.println(label + " took " + ms + " ms");
        }
    }

    public static void runAll(){
        String dirPath = System.getProperty("user.dir") + File.separator + "timer" + File.separator + "src";
        String vidDirPath = dirPath + File.separator + "main" + File.separator + "resources";
        String videoPath = vidDirPath + File.separator + "testVideo.mp4";
        videoObject video = new videoObject(videoPath, vidDirPath, ROI);

        int limit = 100;

        time("Process Images " + limit + " Times", ()->{
            try {
                video.processImages();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        time("Compare By RGB " + limit + " Times", () -> {for (int i = 0; i < limit; i++) {
            video.compareAllFrames("RGB");
        }});
        time("Compare By RGB " + limit + " Times - Multithread", () -> {for (int i = 0; i < limit; i++) {
            try {
                video.compareAllFramesByRGB();;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }});
        time("Compare By WBC " + limit + " Times", () -> {for (int i = 0; i < limit; i++) {
            video.compareAllFrames("WBC");
        }});
        time("Compare By WBC " + limit + " Times - Multithread", () -> {for (int i = 0; i < limit; i++) {
            try {
                video.compareAllFramesByWBC();;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }});
    }

}
