package com.timer;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Image processing utility to convert BufferedImages into Frame objects.
 */
public class ImageProcessor {

    private final videoObject video;

    /**
     * Constructor for ImageProcessor
     *
     * @param video (videoObject) Owning video container.
     */
    public ImageProcessor(videoObject video){
        this.video = video;
    }

    /**
     * Walk pixels, compute and stash RGB/brightness into the frame.
     *
     * @param img (BufferedImage) Image to process.
     * @param frame (Frame) Frame object to write pixel data into.
     * @param width (int) Width of the region of interest.
     * @param height (int) Height of the region of interest.
     */
    public void processImage(BufferedImage img, Frame frame, int width, int height) throws IOException{

        this.video.addFrame(frame);

        int[] argb = img.getRGB(0, 0, width, height, null, 0, width);

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                /**
                 * Extract RGB channels from ARGB integer.
                 * Compute brightness as average of R, G, B.
                 * Store RGB as a 24-bit integer in the frame.
                 * Store brightness in the frame.
                 */

                int pixel = argb[index++];

                int r = (pixel >>> 16) & 0xFF;  // Red as the leftmost 8 bits
                int g = (pixel >>> 8)  & 0xFF;  // Green as the middle 8 bits
                int b =  pixel         & 0xFF;  // Blue as the rightmost 8 bits

                // Simple brightness heuristic from average channel value.
                int brightness = (r+g+b) / 3;

                int rgb = (r << 16) | (g << 8) | b;

                frame.setPixelRGB(x, y, rgb, brightness);
            }
        }
    }

}
