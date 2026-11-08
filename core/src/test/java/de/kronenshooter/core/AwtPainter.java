package de.kronenshooter.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/** Painter für Vorschaubilder ohne Uhr: zeichnet mit java.awt in ein Bild, außerhalb des Kreises grau. */
final class AwtPainter implements Painter {
    final BufferedImage image;
    private final Graphics2D g;
    private final double scale;
    private final BufferedImage[] normal = new BufferedImage[Sprites.COUNT], white = new BufferedImage[Sprites.COUNT];

    AwtPainter(int px) {
        image = new BufferedImage(px, px, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        scale = px / Tuning.SIZE;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, px, px);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        for (int id = 0; id < Sprites.COUNT; id++) {
            int c = Sprites.cols(id), r = Sprites.rowCount(id);
            normal[id] = new BufferedImage(c, r, BufferedImage.TYPE_INT_ARGB);
            white[id] = new BufferedImage(c, r, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < r; y++) {
                for (int x = 0; x < c; x++) {
                    int argb = Sprites.pixel(id, x, y);
                    normal[id].setRGB(x, y, argb);
                    white[id].setRGB(x, y, argb == 0 ? 0 : 0xFFFFFFFF);
                }
            }
        }
    }

    @Override
    public void sprite(int id, double cx, double cy, boolean flash) {
        double w = Sprites.w(id) * scale, h = Sprites.h(id) * scale;
        int x0 = (int) Math.round(cx * scale - w / 2), y0 = (int) Math.round(cy * scale - h / 2);
        g.drawImage(flash ? white[id] : normal[id], x0, y0, (int) Math.round(w), (int) Math.round(h), null);
    }

    @Override
    public void rect(double x0, double y0, double x1, double y1, int argb) {
        g.setColor(new Color(argb, true));
        g.fill(new Rectangle2D.Double(x0 * scale, y0 * scale, (x1 - x0) * scale, (y1 - y0) * scale));
    }

    @Override
    public void text(String s, double cx, double baseline, double size, int argb, boolean bold) {
        g.setFont(new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, (int) Math.round(size * scale)));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(argb, true));
        g.drawString(s, (float) (cx * scale - fm.stringWidth(s) / 2.0), (float) (baseline * scale));
    }

    /** Bereich außerhalb des runden Displays grau, Rand als Linie. */
    void maskRound() {
        int px = image.getWidth();
        Area outside = new Area(new Rectangle2D.Double(0, 0, px, px));
        outside.subtract(new Area(new Ellipse2D.Double(0, 0, px, px)));
        g.setColor(new Color(0x30, 0x30, 0x30));
        g.fill(outside);
        g.setColor(new Color(0x60, 0x60, 0x60));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(1, 1, px - 2, px - 2));
    }
}
