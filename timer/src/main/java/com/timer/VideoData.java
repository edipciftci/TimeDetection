package com.timer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * Wraps a video file and exposes it as a stream of image frames.
 */
public class VideoData implements AutoCloseable {

    private final FFmpegFrameGrabber grabber;
    private final Java2DFrameConverter converter;
    private long frameIndex = 0;
    private String videoName;
    private videoObject video;

    /**
     * Create VideoData from a File.
     */
    public VideoData(videoObject video) throws Exception {
        this.video = video;
        File videoFile = new File(video.getVideoPath());
        this.videoName = video.getVideoName();
        if (videoFile == null || !videoFile.exists()) {
            throw new IllegalArgumentException("Video file does not exist: " + videoFile);
        }

        this.grabber = new FFmpegFrameGrabber(videoFile);
        this.grabber.start();

        this.converter = new Java2DFrameConverter();

        System.out.println("Opened video: " + videoFile.getAbsolutePath());
        System.out.println("  Resolution: " + grabber.getImageWidth() + "x" + grabber.getImageHeight());
        System.out.println("  FPS: " + grabber.getVideoFrameRate());
        System.out.println("  Frames: " + grabber.getLengthInFrames());

        this.video.setNumOfFrames(grabber.getLengthInFrames());
    }

    /**
     * Get the next video frame as a BufferedImage.
     * Returns null when the video ends.
     */
    public BufferedImage nextFrame() throws Exception {
        Frame frame = grabber.grabImage();
        if (frame == null) {
            return null; // end of stream
        }

        BufferedImage img = converter.convert(frame);
        if (img == null) {
            return null;
        }

        System.out.println("Frame " + frameIndex + " created");
        frameIndex++;
        return img;
    }

    /**
     * Convenience: turn the whole video into a sequence of image files.
     *
     * @param outputDir   directory where images will be written
     * @param imageFormat format for ImageIO (e.g. "png", "jpg")
     * @return number of frames written
     */
    public int saveFramesAsImages(Path outputDir, String imageFormat) throws Exception {
        if (imageFormat == null || imageFormat.isEmpty()) {
            throw new IllegalArgumentException("imageFormat must be non-empty (e.g. \"png\" or \"jpg\")");
        }

        // Make sure the directory exists
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new IOException("Could not create output directory: " + outputDir, e);
        }

        int count = 0;
        BufferedImage frame;
        while ((frame = nextFrame()) != null) {
            String fileName = String.format("%s_frame_%06d.%s", this.videoName, count, imageFormat);
            File outFile = outputDir.resolve(fileName).toFile();
            ImageIO.write(frame, imageFormat, outFile);
            count++;
        }

        System.out.println("Saved " + count + " frames to " + outputDir.toAbsolutePath());
        return count;
    }

    /**
     * Current frame index (how many frames have been read so far).
     */
    public long getFrameIndex() {
        return frameIndex;
    }

    @Override
    public void close() throws Exception {
        grabber.stop();
        grabber.release();
        converter.close();
    }

}
