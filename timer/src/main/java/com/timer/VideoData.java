package com.timer;
import java.awt.image.BufferedImage;
import java.io.File;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * VideoData class to handle video frame extraction using FFmpeg.
 */
public class VideoData implements AutoCloseable {

    private final FFmpegFrameGrabber grabber;       // FFmpeg grabber used to pull raw frames
    private final Java2DFrameConverter converter;   // Converter to turn FFmpeg frames into BufferedImages

    /**
     * Constructor for VideoData
     *
     * @param video (videoObject) Target video container.
     */
    public VideoData(videoObject video) throws Exception {
        File videoFile = new File(video.getVideoPath());
        if (videoFile == null || !videoFile.exists()) {
            throw new IllegalArgumentException("Video file does not exist: " + videoFile);
        }

        this.grabber = new FFmpegFrameGrabber(videoFile);
        this.grabber.start();

        this.converter = new Java2DFrameConverter();

        System.out.println("Opened video: " + videoFile.getAbsolutePath());
        System.out.println("  Resolution: " + grabber.getImageWidth() + "x" + grabber.getImageHeight()); // 478 x 850
        System.out.println("  FPS: " + grabber.getVideoFrameRate()); // 60
        System.out.println("  Frames: " + grabber.getLengthInFrames()); // 321

        video.setNumOfFrames(grabber.getLengthInFrames());
    }

    /**
     * Get the next video frame as a BufferedImage.
     * Returns null when the video ends.
     *
     * @return (BufferedImage) Next frame image or null if stream ended.
     */
    public BufferedImage nextFrame() throws Exception {
        try {
            BufferedImage img = converter.convert(grabber.grabImage());
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Iterate through the video, crop to the ROI, and hand frames to the processor.
     *
     * @param boundaries (int[]) Region of interest boundaries [xi, yi, xf, yf]
     * @param video (videoObject) Target video container
     */
    public void getFrames(int[] boundaries, videoObject video) throws Exception{

        int width = boundaries[2]-boundaries[0];
        int height = boundaries[3]-boundaries[1];

        ImageProcessor processor = video.getImageProcessor();

        BufferedImage img;
        com.timer.Frame tempFrame;
        int ID = 0;
        while ((img = nextFrame()) != null) {
            tempFrame = new com.timer.Frame(String.format("FR_%06d", ID++), width, height, video);
            processor.processImage(img.getSubimage(boundaries[0], boundaries[1], width, height), tempFrame, width, height);
        }

    }

    /**
     * Release FFmpeg and converter resources.
     */
    @Override
    public void close() throws Exception {
        grabber.stop();
        grabber.release();
        converter.close();
    }

}
