/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8255439
 * @key headful
 * @library /java/awt/regtesthelpers
 * @build PassFailJFrame
 * @summary To test tray icon scaling with on-the-fly DPI/Scale changes on Windows
 * @run main/manual TrayIconScalingTest
 * @requires (os.family == "windows")
 */


import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.font.TextLayout;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class TrayIconScalingTest {

    private static SystemTray tray;
    private static TrayIcon icon;

    private static final String INSTRUCTIONS =
            "This test case checks the scaling of tray icons for on-the-fly" +
                    " DPI/ Scale changes on Windows.\n\n" +
                    "STEPS: \n\n" +
                    "1. When you run this test check the system tray/" +
                    " notification area on windows, a white multi-resolution" +
                    " image (MRI) icon should be visible.\n\n"+
                    "2. Navigate to Settings > System > Display and change the" +
                    " display scale by selecting any value from" +
                    " Scale & Layout dropdown.\n\n"+
                    "3. On scale changes observe the white tray icon," +
                    " if there is NO distortion then press PASS.\n\n";

    private static Font font = new Font("Dialog", Font.BOLD, 12);

    public static void main(String[] args) throws InterruptedException,
            InvocationTargetException {

        // Check if SystemTray supported on the machine
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        PassFailJFrame passFailJFrame = new PassFailJFrame("TrayIcon " +
                "Test Instructions", INSTRUCTIONS, 8, 18, 85);
        createAndShowGUI();
        try {
            passFailJFrame.awaitAndCheck();
        } finally {
            tray.remove(icon);
        }
    }

    private static void createAndShowGUI() {
        // Create Multi Resolution Image
        ArrayList<Image> images = new ArrayList<>();
        for (int size = 16; size <= 48; size += 4) {
            createIcon(size, images);
        }
        Image mRImage =
                new BaseMultiResolutionImage(images.toArray(new Image[0]));

        tray = SystemTray.getSystemTray();
        icon = new TrayIcon(mRImage);

        try {
            tray.add(icon);
        } catch (AWTException e) {
            throw new RuntimeException("Error while adding icon to system tray");
        }
    }

    // to create different size icons for MRI
    private static void createIcon(int size, ArrayList<Image> imageArrayList) {

        BufferedImage image = new BufferedImage(size, size,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.setFont(font);
        g.setColor(Color.BLACK);

        TextLayout layout = new TextLayout(String.valueOf(size),
                g.getFont(), g.getFontRenderContext());
        int height = (int) layout.getBounds().getHeight();
        int width = (int) layout.getBounds().getWidth();
        layout.draw(g, (size - width) / 2f - 1, (size + height) / 2f);
        imageArrayList.add(image);
        g.dispose();
    }
}

