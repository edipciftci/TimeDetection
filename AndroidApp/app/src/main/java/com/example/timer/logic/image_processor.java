package com.example.timer.logic;

import android.graphics.Bitmap;   // CHANGED: use Bitmap instead of BufferedImage
import java.io.IOException;

/**
 * Image processing utility to convert Bitmaps into Frame objects.
 */
public class image_processor {

    private final video_object video;

    /**
     * Constructor for ImageProcessor
     *
     * @param video (videoObject) Owning video container.
     */
    public image_processor(video_object video){
        this.video = video;
    }

    /**
     * Walk pixels, compute and stash RGB/brightness into the frame.
     *
     * @param img (Bitmap) Image to process.
     * @param frame (Frame) Frame object to write pixel data into.
     * @param width (int) Width of the region of interest.
     * @param height (int) Height of the region of interest.
     */
    public void processImage(Bitmap img, frame_object frame, int width, int height) throws IOException {

        this.video.addFrame(frame);

        // CHANGED: use Bitmap.getPixels instead of BufferedImage.getRGB
        int[] argb = new int[width * height];
        img.getPixels(argb, 0, width, 0, 0, width, height);

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
                int brightness = (r + g + b) / 3;

                int rgb = (r << 16) | (g << 8) | b;

                frame.setPixelRGB(x, y, rgb, brightness);
            }
        }
    }

}
