//$Header: /home/cvs/iag/brian/mbari/src/main/java/org/mbari/util/SystemUtilities.java,v 1.1 2006/01/09 21:16:59 brian Exp $
package mbarix4j.util;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.net.*;

/**
 * <p>Static methods (i.e. functions) for general services.</p><hr>
 *
 * @author  : $Author: brian $
 * @version : $Revision: 1.1 $
 *
 * <hr><p><font size="-1" color="#336699"><a href="http://www.mbari.org">
 * The Monterey Bay Aquarium Research Institute (MBARI)</a> provides this
 * documentation and code &quot;as is&quot;, with no warranty, express or
 * implied, of its quality or consistency. It is provided without support and
 * without obligation on the part of MBARI to assist in its use, correction,
 * modification, or enhancement. This information should not be published or
 * distributed to third parties without specific written permission from
 * MBARI.</font></p><br>
 *
 * <font size="-1" color="#336699">Copyright 2002 MBARI.<br>
 * MBARI Proprietary Information. All rights reserved.</font><br><hr><br>
 *
 */
public class SystemUtilities {

    

    public static URL getURL(String path) {
        class ClassOnCurrentClassLoader {};
        Object c = new ClassOnCurrentClassLoader();
        return c.getClass().getResource(path);  // it looks relative to the class.
        // return c.getClass().getClassLoader().getResource(path); // it looks in the top level directories of your class path.
    }

    public static String getFile(String path) {
        return SystemUtilities.getURL(path).getFile();
    }

    public static boolean isMacOS() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Mac OS");
    }

    public static boolean isWindowsOS() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Windows");
    }

    /**
     * Set a few common properties for the given application if we are running
     * under MacOS. Usage Example:
     *
     * <pre>
     * if (MacOSUtil.isMacOS()) {
     *   MacOSUtil.configureMacOSApplication(&quot;JabaDex&quot;);
     * }
     * </pre>
     *
     * @param appName -
     *            the name of the Application.
     */
    public static void configureMacOSApplication(String appName) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.showGrowBox", "true");
        //System.setProperty("apple.awt.fileDialogForDirectories", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
    }

    /**
     * Set common properties and dock image for the given application is we are running
     * on Mac OS X
     * @param appName The name of the Application
     * @param dockImagePath The String path of the image to use as the dock icon
     */
    public static void configureMacOSApplication(String appName, String dockImagePath) {
        configureMacOSApplication(appName);
        try {
            // Use reflection to invoke Apple's proprietary methods
            Class<?> clazz = Class.forName("com.apple.eawt.Application");
            Method method = clazz.getMethod("getApplication", null);
            Object application = method.invoke(null, null);
            Method method2 = application.getClass().getMethod("setDockIconImage", Image.class);
            URL dockImageURL = "".getClass().getResource(dockImagePath);
            Image dockImage = new ImageIcon(dockImageURL).getImage();
            method2.invoke(application, dockImage);
        }
        catch (Exception e) {
            // Do nothing
        }

    }

    /**
     * Open the systems web browser.
     * 
     * @param url
     */
    public static void openBrowserWithURL(URL url) {
        String osName = System.getProperty("os.name");
        try {
            if (isMacOS()) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                openURL.invoke(null, new Object[]{url.toExternalForm()});
            }
            else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
            else { //assume Unix or Linux

                String[] browsers = {"chrome", "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(
                            new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }

                if (browser == null) {
                    throw new RuntimeException("Could not find web browser");
                }
                else {
                    Runtime.getRuntime().exec(new String[]{browser, url.toExternalForm()});
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to open web browser", e);
        }
    }

    /**
     * Handle method for locating the jar that contains a particular class
     *
     * @param clazz The class of interest
     * @return the name of the jar file containing the class
     */
    public static String findJarForClass(Class clazz) {
        String name = clazz.getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        String jar = clazz.getResource(name + ".class").toString(); //NOI18N
        return jar.substring(0, jar.indexOf('!'));
    }
}