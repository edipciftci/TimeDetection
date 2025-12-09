package com.timer;

/**
 * Class for a video to hold and process its data and frames.
 */
public class videoObject {

    private Frame[] frames;                                                 // Frames of the video in order of appearance
    private final String path, dirPath;
    private int index = 0;                                                  // Frame insertion index
    private int[] boundaries = new int[4];                                  // Region of interest boundaries [xi, yi, xf, yf]
    private final ImageProcessor processor = new ImageProcessor(this);      // ImageProcessor used for all frames

    /**
     * Constructor for videoObject
     *
     * @param name (String) Filename without extension.
     * @param path (String) Absolute path to the video file.
     * @param dirPath (String) Directory that stores the video and generated artifacts.
     * @param type (String) File extension (e.g., mp4).
     * @param boundaries (int[]) Crop bounds [xi, yi, xf, yf] used for frame extraction.
     */
    public videoObject(String path, String dirPath, int[] boundaries){
        this.path = path;
        this.dirPath = dirPath;
        this.boundaries = boundaries;
    }

    /**
     * Getter for the path of the video
     * @return (String)
     */
    public String getVideoPath(){return this.path;}

    /**
     * Getter for the path of the video directory
     * @return (String) Parent directory of the video.
     */
    public String getVideoDirPath(){return this.dirPath;}

    /**
     * Getter for the ImageProcessor of the video
     * @return (ImageProcessor) Shared image processor instance.
     */
    public ImageProcessor getImageProcessor(){return this.processor;}
    
    /**
     * Getter for the frames array.
     * @return (Frame[])
     */
    public Frame[] getFrames(){return this.frames;}

    /**
     * Initialize storage for expected frame count.
     * @param numOfFrames (int) Number of frames the video contains.
     */
    public void setNumOfFrames(int numOfFrames){this.frames = new Frame[numOfFrames];}

    /**
     * Append a frame object to frames array.
     * @param frame (Frame) Frame to append.
     */
    public void addFrame(Frame frame){
        this.frames[this.index] = frame;
        this.index++;
    }

    /**
     * Initialize a new VideoData instance to process and store frames.
     * Use getFrames to extract frames from the video to store in this.frames.
     * Close the VideoData instance after processing.
     */
    public void processImages() throws Exception{

        VideoData vidData = new VideoData(this);
        vidData.getFrames(boundaries, this);
        vidData.close();
    }

    public void compareAllFrames(String method){
        int currIndex = 0;
        System.out.println("Comparing frames by " + method);
        if (method.equals("RGB")){
            while (currIndex + 1 < this.frames.length){
                Frame current = this.frames[currIndex];
                Frame next = this.frames[currIndex + 1];
                this.compareFramesbyRGB(current, next);
                currIndex++;
            }
            return;
        }
        if (method.equals("WBC")){
            while (currIndex + 1 < this.frames.length){
                Frame current = this.frames[currIndex];
                Frame next = this.frames[currIndex + 1];
                this.compareFramesbyWBC(current, next);
                currIndex++;
            }
        }
    }

    public void compareFramesbyRGB(Frame prevFrame, Frame nextFrame){

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

    public void compareFramesbyWBC(Frame prevFrame, Frame nextFrame){

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
