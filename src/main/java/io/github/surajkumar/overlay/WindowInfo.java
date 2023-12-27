package io.github.surajkumar.overlay;

import com.sun.jna.platform.win32.WinDef;

public record WindowInfo(WinDef.HWND hwnd,
                         int x,
                         int y,
                         int width,
                         int height) {
}