package com.example.timer.logic;

import android.media.Image;
import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Single-pass YUV-based video decoder using MediaExtractor + MediaCodec.
 * Decodes frames in order, converts only the ROI to RGB/brightness, and
 * stores them in video_object via addFrame(...).
 */
public class video_data implements AutoCloseable {

    private static final long TIMEOUT_US = 10_000;

    private final MediaExtractor extractor;
    private final MediaCodec decoder;
    private boolean inputDone = false;
    private boolean outputDone = false;

    public video_data(video_object video) throws Exception {
        File videoFile = video.getVideoFile();
        if (videoFile == null || !videoFile.exists()) {
            throw new IllegalArgumentException("Video file does not exist: " + videoFile);
        }

        extractor = new MediaExtractor();
        extractor.setDataSource(videoFile.getAbsolutePath());

        int videoTrackIndex = -1;
        MediaFormat format = null;

        // Find the video track
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat f = extractor.getTrackFormat(i);
            String mime = f.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("video/")) {
                videoTrackIndex = i;
                format = f;
                break;
            }
        }

        if (videoTrackIndex < 0 || format == null) {
            extractor.release();
            throw new IllegalStateException("No video track found in file");
        }

        extractor.selectTrack(videoTrackIndex);

        String mime = format.getString(MediaFormat.KEY_MIME);
        decoder = MediaCodec.createDecoderByType(mime);
        decoder.configure(format, /* surface */ null, /* crypto */ null, 0);
        decoder.start();
    }

    /**
     * Decode all frames once, process ROI in YUV, and push frames into video_object.
     *
     * @param boundaries [xi, yi, xf, yf]
     * @param video      video_object that owns the frames
     */
    public void getFrames(int[] boundaries, video_object video) throws Exception {

        int xi = boundaries[0];
        int yi = boundaries[1];
        int roiWidth = boundaries[2] - boundaries[0];
        int roiHeight = boundaries[3] - boundaries[1];

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int frameIndex = 0;

        while (!outputDone) {

            // ---------- Feed input ----------
            if (!inputDone) {
                int inIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
                if (inIndex >= 0) {
                    ByteBuffer inputBuffer = decoder.getInputBuffer(inIndex);
                    if (inputBuffer != null) {
                        int sampleSize = extractor.readSampleData(inputBuffer, 0);
                        if (sampleSize < 0) {
                            // End of stream
                            decoder.queueInputBuffer(
                                    inIndex,
                                    0,
                                    0,
                                    0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            );
                            inputDone = true;
                        } else {
                            long ptsUs = extractor.getSampleTime();
                            decoder.queueInputBuffer(
                                    inIndex,
                                    0,
                                    sampleSize,
                                    ptsUs,
                                    0
                            );
                            extractor.advance();
                        }
                    }
                }
            }

            // ---------- Drain output ----------
            int outIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
            if (outIndex >= 0) {

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    outputDone = true;
                }

                Image image = decoder.getOutputImage(outIndex);
                if (image != null) {
                    try {
                        if (image.getFormat() != ImageFormat.YUV_420_888) {
                            throw new IllegalStateException(
                                    "Unexpected image format: " + image.getFormat());
                        }

                        frame_object frame = processYuvFrame(
                                image,
                                video,
                                xi,
                                yi,
                                roiWidth,
                                roiHeight,
                                frameIndex
                        );

                        video.addFrame(frame);
                        frameIndex++;

                    } finally {
                        image.close();
                    }
                }

                decoder.releaseOutputBuffer(outIndex, false);

            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // We could inspect new format here if needed.
            } else if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // No output yet, just loop again.
                if (inputDone) {
                    // If input is done and there's still nothing coming, bail out
                    // to avoid spinning forever.
                    outputDone = true;
                }
            }
        }

    }

    /**
     * Convert the ROI of a single YUV_420_888 frame to RGB+brightness into a frame_object.
     */
    private frame_object processYuvFrame(
            Image image,
            video_object video,
            int xi,
            int yi,
            int roiWidth,
            int roiHeight,
            int frameIndex
    ) {

        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        int safeWidth = Math.min(roiWidth, imgWidth - xi);
        int safeHeight = Math.min(roiHeight, imgHeight - yi);
        if (safeWidth <= 0 || safeHeight <= 0) {
            return new frame_object(
                    String.format("FR_%06d", frameIndex),
                    0,
                    0,
                    video
            );
        }

        frame_object frame = new frame_object(
                String.format("FR_%06d", frameIndex),
                safeWidth,
                safeHeight,
                video
        );

        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int yRowStride = planes[0].getRowStride();
        int yPixelStride = planes[0].getPixelStride();

        int uRowStride = planes[1].getRowStride();
        int uPixelStride = planes[1].getPixelStride();

        int vRowStride = planes[2].getRowStride();
        int vPixelStride = planes[2].getPixelStride();

        for (int y = 0; y < safeHeight; y++) {
            int imgY = yi + y;
            int yRowOffset = yRowStride * imgY;

            int chromaRow = imgY / 2;
            int uRowOffset = uRowStride * chromaRow;
            int vRowOffset = vRowStride * chromaRow;

            for (int x = 0; x < safeWidth; x++) {
                int imgX = xi + x;

                int yIndex = yRowOffset + imgX * yPixelStride;

                int chromaCol = imgX / 2;
                int uIndex = uRowOffset + chromaCol * uPixelStride;
                int vIndex = vRowOffset + chromaCol * vPixelStride;

                int Y = yBuffer.get(yIndex) & 0xFF;
                int U = uBuffer.get(uIndex) & 0xFF;
                int V = vBuffer.get(vIndex) & 0xFF;

                // YUV (BT.601) → RGB
                float yf = (Y - 16) * 1.164f;
                float uf = (U - 128);
                float vf = (V - 128);

                int r = (int) (yf + 1.596f * vf);
                int g = (int) (yf - 0.813f * vf - 0.391f * uf);
                int b = (int) (yf + 2.018f * uf);

                if (r < 0) r = 0; else if (r > 255) r = 255;
                if (g < 0) g = 0; else if (g > 255) g = 255;
                if (b < 0) b = 0; else if (b > 255) b = 255;

                int brightness = (r + g + b) / 3;
                int rgb = (r << 16) | (g << 8) | b;

                frame.setPixelRGB(x, y, rgb, brightness);
            }
        }

        return frame;
    }

    @Override
    public void close() {
        try {
            decoder.stop();
        } catch (Exception ignored) {
        }
        decoder.release();
        extractor.release();
    }
}
