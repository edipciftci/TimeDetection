package com.example.timer.logic;

/**
 * Class representing a single frame of video data.
 */
public class frame_object {

    private final String FrameID;
    private final int width, height;
    private int[] pixelRGBTable;    // RGB values per pixel from top-left to bottom-right
    private int[] pixelWBCTable;    // White/Black/Color table per pixel. White=1, Black=-1, Color=0
    private final video_object video;

    /**
     * Constructor for Frame
     *
     * @param FrameID (String) Identifier for the frame.
     * @param width (int) Frame width.
     * @param height (int) Frame height.
     * @param video (videoObject) Owning video container.
     */
    public frame_object(String FrameID, int width, int height, video_object video){
        this.FrameID = FrameID;
        this.width = width;
        this.height = height;

        this.pixelRGBTable = new int[ width * height ]; // Initialize RGB table
        this.pixelWBCTable = new int[ width * height ]; // Initialize WBC table

        this.video = video;
    }

    /**
     * Setter for the RGB pixel table.
     * @param table (int[]) RGB values to copy into the frame.
     */
    public void setPixelRGBTable(int[] table){this.pixelRGBTable=table;}

    /**
     * Getter for the RGB pixel table.
     * @return (int[])
     */
    public int[] getPixelRGBTable(){return this.pixelRGBTable;}

    /**
     * Getter for the WBC pixel table.
     * @return (int[])
     */
    public int[] getPixelWBCTable(){return this.pixelWBCTable;}

    /**
     * Setter for a single pixel's RGB and brightness.
     * @param xPos (int) X-coordinate.
     * @param yPos (int) Y-coordinate.
     * @param RGB (int) Packed RGB value.
     * @param brightness (int) Average brightness used to bucket color.
     */
    public void setPixelRGB(int xPos, int yPos, int RGB, int brightness){
        this.pixelRGBTable[yPos*this.width + xPos] = RGB;
        // Classify pixel into White/Black/Color based on brightness thresholds.
        if (brightness < 60){
            this.pixelWBCTable[yPos*this.width + xPos] = -1; // Black : Threshold below 60
        } else if (brightness > 200){
            this.pixelWBCTable[yPos*this.width + xPos] = 1; // White : Threshold above 200
        } else {
            this.pixelWBCTable[yPos*this.width + xPos] = 0; // Colored : In-between
        }
    }

    /**
     * Getter for a single pixel's RGB value.
     * @param xPos (int) X-coordinate.
     * @param yPos (int) Y-coordinate.
     * @return (int) Packed RGB at the requested pixel.
     */
    public int getPixelRGB(int xPos, int yPos){return this.pixelRGBTable[yPos*this.width + xPos];}

    /**
     * Getter for the frame ID.
     * @return (String) Frame id.
     */
    public String getFrameID(){return this.FrameID;}

}
