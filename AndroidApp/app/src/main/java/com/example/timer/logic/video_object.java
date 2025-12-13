package com.example.timer.logic;

import java.io.File;
import java.util.ArrayList;

/**
 * Class for a video to hold and process its data and frames.
 */
public class video_object {

    private final ArrayList<frame_object> frames = new ArrayList<>();                                                 // Frames of the video in order of appearance
    private final File videoFile;
    private int[] boundaries = new int[4];                                  // Region of interest boundaries [xi, yi, xf, yf]
    private final image_processor processor = new image_processor(this);      // ImageProcessor used for all frames

    /**
     * Constructor for videoObject
     * @param boundaries (int[]) Crop bounds [xi, yi, xf, yf] used for frame extraction.
     */
    public video_object(File videoFile, int[] boundaries){
        this.videoFile = videoFile;
        this.boundaries = boundaries;
    }

    public File getVideoFile(){return this.videoFile;}

    /**
     * Getter for the ImageProcessor of the video
     * @return (ImageProcessor) Shared image processor instance.
     */
    public image_processor getImageProcessor(){return this.processor;}

    /**
     * Getter for the frames array.
     * @return (Frame[])
     */
    public ArrayList<frame_object> getFrames(){return this.frames;}

    /**
     * Append a frame object to frames array.
     * @param frame (Frame) Frame to append.
     */
    public void addFrame(frame_object frame){
        this.frames.add(frame);
    }

    /**
     * Initialize a new VideoData instance to process and store frames.
     * Use getFrames to extract frames from the video to store in this.frames.
     * Close the VideoData instance after processing.
     */
    public void processImages() throws Exception{

        video_data vidData = new video_data(this);
        vidData.getFrames(boundaries, this);
        vidData.close();
    }

    public void compareAllFrames(String method){
        int currIndex = 0;
        if (method.equals("RGB")){
            while (this.frames.get(currIndex+1) != null){
                frame_object current = this.frames.get(currIndex);
                frame_object next = this.frames.get(currIndex + 1);
                this.compareFramesByRGB(current, next);
                currIndex++;
            }
            return;
        }
        if (method.equals("WBC")){
            while (this.frames.get(currIndex+1) != null){
                frame_object current = this.frames.get(currIndex);
                frame_object next = this.frames.get(currIndex + 1);
                this.compareFramesByWBC(current, next);
                currIndex++;
            }
        }
    }

    public void compareAllFramesByRGB() throws InterruptedException {

        if (this.frames == null || this.frames.size() < 2) {
            System.out.println("Video is too short to compute");
            return;
        }

        final int pairCount = this.frames.size() - 1; // (0,1), (1,2), ..., (n-2,n-1)
        final int threadCount = 6;

        Thread[] threads = new Thread[threadCount];

        // how many pairs per thread (last one may get fewer)
        final int chunkSize = (pairCount + threadCount - 1) / threadCount;

        for (int t = 0; t < threadCount; t++) {
            final int start = t * chunkSize;
            if (start >= pairCount) {
                threads[t] = null;
                continue;
            }
            final int end = Math.min(start + chunkSize, pairCount); // exclusive

            threads[t] = new Thread(() -> {
                for (int i = start; i < end; i++) {
                    frame_object current = this.frames.get(i);
                    frame_object next    = this.frames.get(i + 1);
                    compareFramesByRGB(current, next);
                }
            }, "compare-thread-" + t);

            threads[t].start();
        }

        // wait for all threads
        for (Thread thread : threads) {
            if (thread != null) {
                thread.join();
            }
        }
    }

    public void compareAllFramesByWBC() throws InterruptedException {

        if (this.frames == null || this.frames.size() < 2) {
            System.out.println("Video is too short to compute");
            return;
        }

        final int pairCount = this.frames.size() - 1; // (0,1), (1,2), ..., (n-2,n-1)
        final int threadCount = 6;

        Thread[] threads = new Thread[threadCount];

        // how many pairs per thread (last one may get fewer)
        final int chunkSize = (pairCount + threadCount - 1) / threadCount;

        for (int t = 0; t < threadCount; t++) {
            final int start = t * chunkSize;
            if (start >= pairCount) {
                threads[t] = null;
                continue;
            }
            final int end = Math.min(start + chunkSize, pairCount);

            threads[t] = new Thread(() -> {
                for (int i = start; i < end; i++) {
                    frame_object current = this.frames.get(i);
                    frame_object next    = this.frames.get(i + 1);
                    compareFramesByWBC(current, next);
                }
            }, "compare-thread-" + t);

            threads[t].start();
        }

        for (Thread thread : threads) {
            if (thread != null) {
                thread.join();
            }
        }
    }

    public void compareFramesByRGB(frame_object prevFrame, frame_object nextFrame){

        int changeCount = 0;
        int colorChanges;

        int[] prevTable = prevFrame.getPixelRGBTable();
        int[] nextTable = nextFrame.getPixelRGBTable();

        int prevColor, nextColor;

        int tableSize = prevTable.length;
        int movementLimit = (int) (tableSize * 0.1);

        for (int position = 0; position < tableSize; position++) {
            colorChanges = 0;
            prevColor = prevTable[position];
            nextColor = nextTable[position];

            if (Math.abs((prevColor & 0xFF) - (nextColor & 0xFF)) > 5){colorChanges++;}
            if (Math.abs((prevColor >>> 8 & 0xFF) - (nextColor >>> 8 & 0xFF)) > 5){colorChanges++;}
            if (Math.abs((prevColor >>> 16 & 0xFF) - (nextColor >>> 16 & 0xFF)) > 5){colorChanges++;}

            if (colorChanges > 1){changeCount++;}
        }

        if (changeCount > movementLimit){
            System.out.println(String.format("There is movement during frames %s and %s with respect to RGB", prevFrame.getFrameID(), nextFrame.getFrameID()));
        }

    }

    public void compareFramesByWBC(frame_object prevFrame, frame_object nextFrame){

        int changeCount = 0;

        int[] prevTable = prevFrame.getPixelWBCTable();
        int[] nextTable = nextFrame.getPixelWBCTable();

        int tableSize = prevTable.length;
        int movementLimit = (int) (tableSize * 0.1);

        for (int position = 0; position < tableSize; position++) {
            if (prevTable[position] != nextTable[position]){changeCount++;}
        }

         if (changeCount > movementLimit){
             System.out.println(String.format("There is movement during frames %s and %s with respect to WBC", prevFrame.getFrameID(), nextFrame.getFrameID()));
         }

    }

}

