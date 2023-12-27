package io.github.surajkumar.overlay;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An experimentation using OpenCV (This class is broken)
 */
public class ExperimentalShaderOpenCV extends JPanel {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private BufferedImage image;
    private JFrame frame;

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public ExperimentalShaderOpenCV() throws Exception {
        this.setDoubleBuffered(true);

        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setBackground(new Color(0, true));
        frame.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        frame.setFocusable(true);
        frame.add(this);
        frame.setVisible(true);

        allowPassThrough(frame);

        Robot robot = new Robot();

        executorService.scheduleAtFixedRate(() -> {
            frame.setVisible(false);

            updateImage(processImage(robot.createScreenCapture(GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds())));

            frame.setVisible(true);

        }, 0, getMonitorRefreshRate(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    /**
     * This method lets mouse events to be "passed through" the JFrame. If the frame has transparency this
     * works already but when there is no transparency then we need this function to allow the pass through.
     * @param frame The visible JFrame that can have user input pass through.
     */
    public static void allowPassThrough(JFrame frame) {
        if(!frame.isVisible()) {
            throw new RuntimeException("JFrame must be set to visible for this to allow pass through");
        }
        WinDef.HWND hwnd = new WinDef.HWND(Native.getComponentPointer(frame));
        int wl = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
        wl = wl | WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, wl);
    }

    public void updateImage(BufferedImage image) {
        this.image = image;
        this.repaint();
    }

    private BufferedImage processImage(BufferedImage image) {
        Mat matImage = bufferedImageToMat(image);
        Scalar lowerGreen = new Scalar(35, 100, 50);
        Scalar upperGreen = new Scalar(85, 255, 255);
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(matImage, hsvImage, Imgproc.COLOR_BGR2HSV);
        Mat greenMask = new Mat();
        Core.inRange(hsvImage, lowerGreen, upperGreen, greenMask);
        Mat pinkMat = new Mat(matImage.size(), matImage.type(), new Scalar(180, 105, 255));
        pinkMat.copyTo(matImage, greenMask);
        return matToBufferedImage(matImage);
    }

    private static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat;
        int imageType = bi.getType();
        switch (imageType) {
            case BufferedImage.TYPE_3BYTE_BGR:
                mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
                mat.put(0, 0, ((DataBufferByte) bi.getRaster().getDataBuffer()).getData());
                break;
            case BufferedImage.TYPE_INT_RGB:
                mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
                int[] dataRGB = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
                byte[] pixelDataRGB = new byte[dataRGB.length * 3];
                for (int i = 0; i < dataRGB.length; i++) {
                    pixelDataRGB[i * 3] = (byte) ((dataRGB[i] >> 16) & 0xFF);
                    pixelDataRGB[i * 3 + 1] = (byte) ((dataRGB[i] >> 8) & 0xFF);
                    pixelDataRGB[i * 3 + 2] = (byte) (dataRGB[i] & 0xFF);
                }
                mat.put(0, 0, pixelDataRGB);
                break;
            default:
                throw new IllegalArgumentException("Unsupported BufferedImage type: " + imageType);
        }
        return mat;
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        byte[] data = new byte[mat.rows() * mat.cols() * (int)(mat.elemSize())];
        mat.get(0, 0, data);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_3BYTE_BGR); // Only works with TYPE_3BYTE_BGR?
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }

    private static int getMonitorRefreshRate() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        int refreshRate = dm.getRefreshRate();
        if(refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
            throw new RuntimeException("Cannot determine refresh rate");
        }
        return refreshRate;
    }

    public static void main(String[] args) throws Exception {
        new ExperimentalShaderOpenCV();
    }
}
