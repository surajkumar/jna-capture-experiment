package io.github.surajkumar.overlay;

import io.github.surajkumar.capture.NativeScreenCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static io.github.surajkumar.overlay.ExperimentalShaderOpenCV.allowPassThrough;

/**
 * This is an experiment of modifying pixels.
 * Class currently unused.
 */
public class ExperimentOverlayViewer extends JPanel {
    private BufferedImage image;
    private final Rectangle paintRegion;
    private final int width;
    private final int height;

    public ExperimentOverlayViewer(int width, int height) {
        this.width = width;
        this.height = height;
        this.paintRegion = new Rectangle(width, height);
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    /**
     * This method recolors a specific pixel, in this example all the greens are changed to pink
     */
    private Color fragmentShader(int x, int y) {
        try {
            Color original = new Color(image.getRGB(x, y), true);

            float red = original.getRed() / 255.0f;
            float green = original.getGreen() / 255.0f;
            float blue = original.getBlue() / 255.0f;

            // Determine if green is the dominant color
            if (green > red && green > blue) {
                // Set red and blue to maximum to create bright pink and reduce green
                red = 1.0f;
                green = 0.0f;
                blue = 1.0f;
            }

            return new Color(red, green, blue);
        } catch (Exception e) {
            return new Color(0, 0,0); //window was resized
        }
    }

    public void updateImage(BufferedImage image) {
        this.image = image;
        triggerUpdate();
    }

    public void set(BufferedImage image) {
        this.image = image;
    }

    private void triggerUpdate() {
        updateShader();
        repaint();
    }

    /**
     * Loops over all the pixels in image and run the fragmentShader on them
     */
    private void updateShader() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, fragmentShader(x, y).getRGB());
            }
        }
    }

    public Rectangle getRectangle() {
        return paintRegion;
    }

    public static void main(String[] args) throws Exception {
        NativeScreenCapture capture = new NativeScreenCapture();
        WindowInfo handle = NativeScreenCapture.getWindowHandle("RuneLite - Papa Joestar");
        ExperimentShaderPanel experimentShaderPanel = new ExperimentShaderPanel(handle.width(), handle.height());

        JFrame frame;
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setBackground(new Color(0, true));
        frame.setBounds(handle.x(), handle.y(), handle.width(), handle.height());
        frame.setFocusable(true);
        frame.add(experimentShaderPanel);
        frame.setVisible(true);

        allowPassThrough(frame);

        // Update location and size
        new Thread(() -> {

            int previousX = 0;
            int previousY = 0;
            int previousWidth = 0;
            int previousHeight = 0;

            while(true) {
                WindowInfo info = NativeScreenCapture.getWindowHandle("RuneLite - Papa Joestar");
                if(info.width() != previousWidth || info.height() != previousHeight) {
                    frame.setSize(info.width(), info.height());
                    previousWidth = info.width();
                    previousHeight = info.height();
                }
                if(info.x() != previousX || info.y() != previousY) {
                    frame.setLocation(info.x(), info.y());
                    previousX = info.x();
                    previousY = info.y();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        while(true) {
            experimentShaderPanel.updateImage(capture.capture(handle.hwnd()));
            Thread.sleep(75);
        }
    }
}

