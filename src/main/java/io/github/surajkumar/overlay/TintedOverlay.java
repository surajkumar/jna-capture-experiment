package io.github.surajkumar.overlay;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import javax.swing.*;
import java.awt.*;

/**
 * Adds a solid color overlay to the desktop.
 */
public class TintedOverlay {
    private final JFrame frame;

    public TintedOverlay(Color color, float opacity, int monitor) {
        Dimension screenSize = new Dimension(getBoundsForMonitor(monitor).getSize());
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setSize(screenSize);
        frame.setUndecorated(true);
        frame.setLocation(0, 0);
        frame.setOpacity(opacity);

        JPanel overlay = new JPanel();
        overlay.setBackground(color);
        overlay.setOpaque(true);

        frame.add(overlay);
        frame.setVisible(true);
        allowPassThrough(frame);
    }

    /**
     * This method lets mouse events to be "passed through" the JFrame. If the frame has transparency this
     * works already but when there is no transparency then we need this function to allow the pass through.
     * @param frame The visible JFrame that can have user input pass through.
     */
    private static void allowPassThrough(JFrame frame) {
        if(!frame.isVisible()) {
            throw new RuntimeException("JFrame must be set to visible for this to allow pass through");
        }
        WinDef.HWND hwnd = new WinDef.HWND(Native.getComponentPointer(frame));
        int wl = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
        wl = wl | WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, wl);
    }

    private static Rectangle getBoundsForMonitor(int monitor) {
        return GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getScreenDevices()[monitor]
                .getDefaultConfiguration()
                .getBounds();
    }

    public static void main(String[] args) {
        new TintedOverlay(Color.BLUE, 0.05F, 0);
    }
}
