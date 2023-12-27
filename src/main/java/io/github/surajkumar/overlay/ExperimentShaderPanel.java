package io.github.surajkumar.overlay;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This is an experiment of modifying pixels.
 * Class currently unused.
 */
public class ExperimentShaderPanel extends JPanel {
    private BufferedImage image;
    private final Rectangle paintRegion;
    private final int width;
    private final int height;

    public ExperimentShaderPanel(int width, int height) {
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
    }

    public void updateImage(BufferedImage image) {
        this.image = image;
        triggerUpdate();
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
}
