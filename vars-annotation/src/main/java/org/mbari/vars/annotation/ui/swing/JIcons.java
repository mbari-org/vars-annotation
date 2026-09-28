package org.mbari.vars.annotation.ui.swing;

import org.kordamp.ikonli.AbstractIkonResolver;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import org.mbari.vars.annotation.ui.swing.annotable.Colors;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds Swing icons from Ikonli icons.
 * <p>
 * We deliberately do not use ikonli-swing's FontIcon. Since Ikonli 12.4.0 the ikon handlers are
 * shared by a single global resolver, and the first toolkit (JavaFX or Swing) to touch it decides
 * whether the handler's font is a JavaFX or AWT font. As this app uses both, FontIcon fails with a
 * ClassCastException. Here we load our own AWT fonts from the handlers' font resources and never
 * touch the handlers' shared font state.
 */
public class JIcons {

    private static final Map<String, Font> BASE_FONTS = new ConcurrentHashMap<>();

    private JIcons() {
        // no instantiation
    }

    public static Icon asSwing(org.mbari.vars.annotation.ui.javafx.Icons icon, int size, Color color) {
        var c = color == null ? Colors.DEFAULT_TEXT.getColor() : color;
        var ikon = icon.getIkon();
        return new AwtFontIcon(ikon, awtFont(ikon).deriveFont(Font.PLAIN, (float) size), size, c);
    }

    private static Font awtFont(Ikon ikon) {
        var description = ikon.getDescription();
        var handler = findHandler(description);
        return BASE_FONTS.computeIfAbsent(handler.getFontFamily(), family -> loadFont(handler));
    }

    private static IkonHandler findHandler(String description) {
        for (var handler : AbstractIkonResolver.resolveServiceLoader()) {
            if (handler.supports(description)) {
                return handler;
            }
        }
        throw new IllegalArgumentException("No IkonHandler found for '" + description + "'");
    }

    private static Font loadFont(IkonHandler handler) {
        try (InputStream in = handler.getFontResourceAsStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, in);
        }
        catch (IOException | FontFormatException e) {
            throw new IllegalStateException("Failed to load font " + handler.getFontFamily(), e);
        }
    }

    private static class AwtFontIcon implements Icon {
        private final String glyph;
        private final Font font;
        private final int size;
        private final Color color;

        AwtFontIcon(Ikon ikon, Font font, int size, Color color) {
            this.glyph = new String(Character.toChars(ikon.getCode()));
            this.font = font;
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            var g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(font);
                g2.setColor(color);
                g2.drawString(glyph, x, y + g2.getFontMetrics().getAscent());
            }
            finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
