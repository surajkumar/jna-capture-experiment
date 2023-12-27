package io.github.surajkumar.capture;

import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class HardwareAccelerationDetector {
    public static boolean isHardwareAccelerated(String windowTitle) {
        WinDef.HWND hWnd = User32.INSTANCE.FindWindow(null, windowTitle);
        WinDef.HDC hdc = User32.INSTANCE.GetDC(hWnd);
        int technology = GDI32.INSTANCE.GetDeviceCaps(hdc, 2);
        boolean isHardwareAccelerated = technology == 2 ||
                technology == 4 ||
                technology == 6;
        User32.INSTANCE.ReleaseDC(hWnd, hdc);
        return isHardwareAccelerated;
    }
}
