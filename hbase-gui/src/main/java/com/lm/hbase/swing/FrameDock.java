package com.lm.hbase.swing;

import java.awt.Image;

import com.lm.hbase.common.ImageIconConstons;
import com.lm.hbase.util.OSinfo;

public class FrameDock {

    @SuppressWarnings("restriction")
    public static void setDockIconImage() {
        Image image = ImageIconConstons.DOCK_ICON.getImage();
        if (OSinfo.isMacOS() || OSinfo.isMacOSX()) {
            com.apple.eawt.Application.getApplication().setDockIconImage(image);
        }

    }

}
