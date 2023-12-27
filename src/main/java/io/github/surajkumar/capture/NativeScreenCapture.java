package io.github.surajkumar.capture;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import static com.sun.jna.platform.win32.GDI32.SRCCOPY;

public class NativeScreenCapture {

    public BufferedImage capture(WinDef.HWND hWnd) {
        GDI32 gdi = GDI32.INSTANCE;
        User32 user = User32.INSTANCE;

        WinDef.RECT rect = new WinDef.RECT();
        user.GetWindowRect(hWnd, rect);

        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        WinDef.HDC windowDC = user.GetDC(hWnd);
        WinDef.HDC memoryDC = gdi.CreateCompatibleDC(windowDC);
        WinDef.HBITMAP bitmap = gdi.CreateCompatibleBitmap(windowDC, width, height);
        WinNT.HANDLE oldBitmap = gdi.SelectObject(memoryDC, bitmap);
        gdi.BitBlt(memoryDC, 0, 0, width, height, windowDC, 0, 0, SRCCOPY);
        gdi.SelectObject(memoryDC, oldBitmap);

        BufferedImage image = convertNativeToBufferedImage(width, height, gdi, windowDC, bitmap);

        gdi.DeleteObject(bitmap);
        gdi.DeleteDC(memoryDC);
        user.ReleaseDC(hWnd, windowDC);
        return image;
    }

    private static BufferedImage convertNativeToBufferedImage(
            int width,
            int height,
            GDI32 gdi,
            WinDef.HDC windowDC,
            WinDef.HBITMAP bitmap)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO(40);
        bmi.bmiHeader.biSize = 40;
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        Memory buffer = new Memory((long) width * height * 4);
        gdi.GetDIBits(windowDC, bitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);
        image.setRGB(0, 0, width, height, buffer.getIntArray(0, width * height), 0, width);

        return image;
    }

    public static WinDef.HWND getWindowHandle(String windowTitle) {
        User32 user32 = User32.INSTANCE;
        AtomicReference<WinDef.HWND> foundHandle = new AtomicReference<>();
        user32.EnumWindows((handle, arg1) -> {
            char[] windowText = new char[512];
            user32.GetWindowText(handle, windowText, 512);
            String wText = Native.toString(windowText).trim();
            if (!wText.isEmpty()) {
                if(wText.equals(windowTitle)) {
                    foundHandle.set(handle);
                    return false;
                }
            }
            return true;
        }, null);
        return foundHandle.get();
    }

    public static WinDef.HWND findWindowByTitle(String title) {
        return User32.INSTANCE.FindWindow(null, title);
    }

    public static void main(String[] args) throws Exception {
        NativeScreenCapture capture = new NativeScreenCapture();
        BufferedImage image = capture.capture(getWindowHandle(args[0]));
        ImageIO.write(image, "png", new File("screenshot.png"));
    }
}
