package com.dan.sylt.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Das Programmsymbol (F32 A): das Quermarkenfeuer Rotes Kliff über dem roten Kliff, davor die Brandung, dahinter der
 * Abendhimmel über der Nordsee, Goldkante wie {@code heidelberg.ico}. Phase 1 malt es, Phase 19 legt die PNGs ins Jar
 * ({@code com/dan/sylt/ui/icon/sylt_NN.png}) und schreibt {@code Sylt.ico} für die Verknüpfung. Fehlt eine Größe,
 * malt die App sie selbst.
 */
public final class AppIcon {
    public static final int[] SIZES = {16, 24, 32, 48, 64, 128, 256};

    private AppIcon() { }

    public static List<Image> images() {
        List<Image> l = new ArrayList<>();
        for (int s : SIZES) l.add(load(s));
        return l;
    }

    public static BufferedImage load(int size) {
        try (InputStream in = AppIcon.class.getResourceAsStream("icon/sylt_" + size + ".png")) {
            if (in != null) {
                BufferedImage b = javax.imageio.ImageIO.read(in);
                if (b != null) return b;
            }
        } catch (java.io.IOException e) { /* gemaltes Symbol */ }
        return paint(size);
    }

    /** Das Symbol, in jeder Größe neu gemalt. */
    public static BufferedImage paint(int s) {
        BufferedImage im = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        double r = s * 0.18, inset = Math.max(1, s * 0.035);
        // Goldkante
        g.setPaint(new GradientPaint(0, 0, new Color(240, 210, 120), s, s, new Color(150, 105, 30)));
        g.fill(new RoundRectangle2D.Double(0, 0, s, s, r * 2, r * 2));
        RoundRectangle2D inner = new RoundRectangle2D.Double(inset, inset, s - 2 * inset, s - 2 * inset, r * 1.7, r * 1.7);
        g.setClip(inner);
        // Abendhimmel: oben Blau, an der Kimm Gold
        double kimm = s * 0.62;
        g.setPaint(new GradientPaint(0, 0, new Color(30, 52, 78), 0, (float) kimm, new Color(244, 186, 110)));
        g.fill(new Rectangle2D.Double(0, 0, s, kimm + 1));
        // Sonne tief über der See
        if (s >= 24) {
            double sr = s * 0.07;
            g.setColor(new Color(255, 226, 160, 230));
            g.fill(new Ellipse2D.Double(s * 0.16 - sr, kimm - sr * 1.3, sr * 2, sr * 2));
        }
        // See
        g.setPaint(new GradientPaint(0, (float) kimm, new Color(47, 111, 106), 0, s, new Color(22, 52, 54)));
        g.fill(new Rectangle2D.Double(0, kimm, s, s - kimm));
        // das Kliff von rechts: oben Grasnarbe, Geschiebelehm rot, unten Kaolinsand
        Path2D k = new Path2D.Double();
        k.moveTo(s + 1, s + 1);
        k.lineTo(s * 0.50, s + 1);
        k.lineTo(s * 0.56, s * 0.80);
        k.lineTo(s * 0.52, s * 0.66);
        k.lineTo(s * 0.58, s * 0.50);
        k.lineTo(s + 1, s * 0.47);
        k.closePath();
        g.setPaint(new GradientPaint((float) (s * 0.5), 0, new Color(217, 115, 58), s, 0, new Color(160, 70, 34)));
        g.fill(k);
        Path2D kaolin = new Path2D.Double();
        kaolin.moveTo(s + 1, s + 1);
        kaolin.lineTo(s * 0.50, s + 1);
        kaolin.lineTo(s * 0.55, s * 0.80);
        kaolin.lineTo(s + 1, s * 0.76);
        kaolin.closePath();
        g.setColor(new Color(236, 228, 210));
        g.fill(kaolin);
        g.setColor(new Color(120, 132, 70));
        g.fill(new Rectangle2D.Double(s * 0.57, s * 0.455, s * 0.45, Math.max(1, s * 0.03)));
        // das Quermarkenfeuer: Achteck aus Backstein, weiße Laterne, grünes Kupferdach
        double tx = s * 0.76, tw = Math.max(2, s * 0.13), th = s * 0.24, base = s * 0.47;
        g.setColor(new Color(150, 62, 44));
        g.fill(new Rectangle2D.Double(tx - tw / 2, base - th, tw, th));
        if (s >= 32) {
            g.setColor(new Color(110, 44, 32));
            g.fill(new Rectangle2D.Double(tx - tw / 2, base - th, tw * 0.25, th));
        }
        double lw = tw * 0.9, lh = s * 0.08;
        g.setColor(new Color(244, 242, 236));
        g.fill(new Rectangle2D.Double(tx - lw / 2, base - th - lh, lw, lh));
        if (s >= 48) {
            g.setColor(new Color(255, 214, 120));
            g.fill(new Rectangle2D.Double(tx - lw * 0.3, base - th - lh * 0.8, lw * 0.6, lh * 0.55));
        }
        Path2D dach = new Path2D.Double();
        dach.moveTo(tx - lw * 0.62, base - th - lh);
        dach.lineTo(tx, base - th - lh - s * 0.08);
        dach.lineTo(tx + lw * 0.62, base - th - lh);
        dach.closePath();
        g.setColor(new Color(92, 160, 128));
        g.fill(dach);
        // Brandung: zwei weiße Linien vor dem Kliff
        if (s >= 24) {
            g.setStroke(new BasicStroke((float) Math.max(1, s * 0.022), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 2; i++) {
                double y = s * (0.74 + 0.11 * i);
                Path2D w = new Path2D.Double();
                w.moveTo(-1, y);
                w.curveTo(s * 0.15, y - s * 0.03, s * 0.30, y + s * 0.02, s * 0.53 - i * s * 0.02, y - s * 0.01);
                g.setColor(new Color(240, 244, 242, 220 - i * 50));
                g.draw(w);
            }
        }
        g.setClip(null);
        g.setColor(new Color(255, 236, 180, 120));
        g.setStroke(new BasicStroke((float) Math.max(0.6, s * 0.012)));
        g.draw(inner);
        g.dispose();
        return im;
    }

    /** Setzt das Symbol am Fenster und, wo unterstützt, in der Taskleiste. */
    public static void install(java.awt.Window w) {
        List<Image> l = images();
        w.setIconImages(l);
        try {
            if (java.awt.Taskbar.isTaskbarSupported()) {
                java.awt.Taskbar tb = java.awt.Taskbar.getTaskbar();
                if (tb.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) tb.setIconImage(l.get(l.size() - 1));
            }
        } catch (Exception | Error ignored) {
            // Windows nimmt ohnehin die Fenstersymbole
        }
    }
}
