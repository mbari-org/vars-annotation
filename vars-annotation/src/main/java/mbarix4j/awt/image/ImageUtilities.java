/*
 * @(#)ImageUtilities.java   2011.12.10 at 08:52:59 PST
 *
 * Copyright 2009 MBARI
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package mbarix4j.awt.image;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 *
 * @author brian
 */
public class ImageUtilities {

    private ImageUtilities() {

        // NO instantiation allowed
    }

    /**
     * Adds text as a watermark to an image as 50% transparent of the supplied color.
     *
     * @param bufferedImage The image to add the watermark to
     * @param watermark The text to add as a watermark
     * @param color The color of the watermarked text [default = white]
     * @param font The font to use for the watermark [default = Arial Bold 30pt.]
     * @param alpha
     * @return The bufferedImage that was passed in (of course it now has the watermark). This
     *      was added to allow method chaining.
     */
    public static BufferedImage addWatermark(BufferedImage bufferedImage, String watermark, Color color, Font font,
            float alpha) {

        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();

        //Create an alpha composite of 50% or whatever the user specified
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(color);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics();
        Rectangle2D rect = fontMetrics.getStringBounds(watermark, g2d);
        g2d.drawString(watermark, (int) ((bufferedImage.getWidth() - rect.getWidth()) / 2),
                       (int) ((bufferedImage.getHeight() - rect.getHeight()) / 2));

        //Free graphic resources
        g2d.dispose();

        return bufferedImage;

    }

    /**
     * This method returns true if the specified image has transparent pixels.
     * This code is from <a href="http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html" />
     * The Java Developers Almanac 1.4</a>
     * @param image
     * @return
     */
    public static boolean hasAlpha(Image image) {

        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;

            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        }
        catch (InterruptedException e) {}

        // Get the image's color model
        ColorModel cm = pg.getColorModel();

        return cm.hasAlpha();
    }

    /**
     * Saves the image to the target file. The format of the saved
     * file is determined by it's extension
     *
     * @param image The image to save
     * @param target The file to save the image to.
     *
     * @throws IOException
     */
    public static void saveImage(RenderedImage image, File target) throws IOException {

        /*
         * Extract the type from the extension
         */
        String path = target.getAbsolutePath();
        int dotIdx = path.lastIndexOf(".");
        String ext = path.substring(dotIdx + 1);
        ImageIO.write(image, ext, target);
    }

    /**
     * Converts a RenderedImage into a BufferedImage. If the RenderedImage
     * is already a BufferedImage then the input argument is returned (i.e. no
     * operations are performed on the image). Found this at
     * {@link http://www.mambo.net/cgi-bin/TempProcessor/view/77}
     *
     * @param image The RenderedImage to convert
     * @return The input converted to a BufferedImage
     */
    public static BufferedImage toBufferedImage(RenderedImage image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        ColorModel colorModel = image.getColorModel();
        int width = image.getWidth();
        int height = image.getHeight();
        WritableRaster raster = colorModel.createCompatibleWritableRaster(width, height);
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        Hashtable<String, Object> props = new Hashtable<>();
        String[] keys = image.getPropertyNames();

        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                props.put(keys[i], image.getProperty(keys[i]));
            }
        }
        BufferedImage bufferedImage = new BufferedImage(colorModel, raster, isAlphaPremultiplied, props);
        image.copyData(raster);

        return bufferedImage;

    }

    /**
     * This method returns a buffered image with the contents of an image. This
     * code is from <a href="http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html?l=rel" />
     * The Java Developers Almanac 1.4</a>
     * @param image The Image to convert to a BufferedImage
     * @return
     */
    public static BufferedImage toBufferedImage(Image image) {

        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {

            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        }
        catch (HeadlessException e) {

            // The system does not have a screen
        }

        if (bimage == null) {

            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    /**
     * This method returns an Image object from a buffered image. This code is
     * from <a href="http://www.exampledepot.com/egs/java.awt.image/Buf2Image.html?l=rel" />
     * The Java Developers Almanac 1.4</a>
     * @param bufferedImage
     * @return
     */
    public static Image toImage(BufferedImage bufferedImage) {
        return Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
    }
}
