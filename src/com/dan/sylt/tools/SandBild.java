package com.dan.sylt.tools;

import com.dan.dune.FluxField;
import com.dan.dune.Grains;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.scene.Sand;
import com.dan.sylt.scene.Site;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;

/**
 * Prüfbild des Flugsands ohne Grafikkarte (seit Phase 9a): „Sandsturm am Strand“ von oben, das Feld um das Auge zu
 * 0,5 m (240 × 160 m): Gelände schattiert, Fluss als Helligkeit (gold), nasser Sand blau, kein loser Sand grün, Wind
 * aus {@code com.dan.wind} als Pfeile, die Körner als Punkte (Schweb rot), das Auge als Kreuz.
 * <pre>
 * SandBild datei.png [Wind m/s] [Richtung]
 * </pre>
 */
public final class SandBild {
    private SandBild() { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Sand_Phase9a.png");
        Renderer r = new Renderer();
        r.wiring.tideAn = false;
        r.wiring.pegel = -0.3;
        r.sandsturm();
        if (a.length > 1) r.wiring.windSpeed = Double.parseDouble(a[1]);
        if (a.length > 2) r.wiring.windFromDeg = Double.parseDouble(a[2]);
        r.step(1 / 60.0);
        r.sandJetzt();
        Sand s = r.sand;
        FluxField f = s.feld();
        Grains g = s.koerner();
        double px = 0.25;
        int w = (int) (Sand.LAENGS / px), h = (int) (Sand.QUER / px);
        BufferedImage img = new BufferedImage(w, h + 40, BufferedImage.TYPE_INT_RGB);
        // Bild in Feldkoordinaten: x längs (Windrichtung, nach rechts), y quer
        double qMax = Math.max(1e-6, f.maxFlux());
        for (int j = 0; j < h; j++)
            for (int i = 0; i < w; i++) {
                double[] p = f.world(i * px / Sand.ZELLE - 0.5, j * px / Sand.ZELLE - 0.5);
                double gr = r.terrain.height(p[0], p[1]);
                double gx = r.terrain.height(p[0] + 1, p[1]) - r.terrain.height(p[0] - 1, p[1]), gz = r.terrain.height(p[0], p[1] + 1) - r.terrain.height(p[0], p[1] - 1);
                double shade = Math.max(0.35, Math.min(1.3, 1 + (-gx + gz) * 0.4));
                double q = f.fluxAt(p[0], p[1]), m = s.dune.moisture.moisture(p[0], p[1]), lam = s.dune.cover.lambda(p[0], p[1]);
                double[] c;
                if (gr < r.wiring.pegel) c = new double[]{0.10, 0.22, 0.26};
                else if (lam >= 1) c = new double[]{0.30 * shade, 0.36 * shade, 0.22 * shade};
                else if (m >= s.dune.boundMoisture) c = new double[]{0.38 * shade, 0.42 * shade, 0.48 * shade};
                else {
                    double v = Math.sqrt(Math.max(0, q) / qMax);
                    c = new double[]{(0.45 + 0.5 * v) * shade, (0.40 + 0.42 * v) * shade, (0.30 + 0.2 * v) * shade};
                }
                img.setRGB(i, j, rgb(c));
            }
        Graphics2D gg = img.createGraphics();
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Körner
        for (int k = 0; k < g.count; k++) {
            double[] l = f.local(g.x[k], g.z[k]);
            int x = (int) ((l[0] + 0.5) * Sand.ZELLE / px), y = (int) ((l[1] + 0.5) * Sand.ZELLE / px);
            if (x < 0 || y < 0 || x >= w || y >= h) continue;
            img.setRGB(x, y, g.kind[k] == 1 ? 0xE04A30 : 0xFFF4D8);
        }
        // Wind aus dem Feld von wind
        gg.setStroke(new BasicStroke(1.5f));
        gg.setColor(new Color(255, 255, 255, 190));
        for (double li = 10; li < Sand.LAENGS; li += 20)
            for (double qi = 10; qi < Sand.QUER; qi += 20) {
                double[] p = f.world(li / Sand.ZELLE - 0.5, qi / Sand.ZELLE - 0.5);
                double sp = s.dune.windSpeedAt(p[0], p[1]), to = Math.toRadians(s.dune.windFromAt(p[0], p[1]) + 180);
                double ex = Math.sin(to), ez = -Math.cos(to);                  // Ost, Süd
                double ls = ex * f.tx + ez * f.tz, lq = -ex * f.tz + ez * f.tx;  // längs, quer
                int x0 = (int) (li / px), y0 = (int) (qi / px), len = (int) (sp * 2.2);
                int x1 = (int) (x0 + ls * len), y1 = (int) (y0 + lq * len);
                gg.drawLine(x0, y0, x1, y1);
                double ang = Math.atan2(y1 - y0, x1 - x0);
                gg.drawLine(x1, y1, (int) (x1 - 7 * Math.cos(ang - 0.5)), (int) (y1 - 7 * Math.sin(ang - 0.5)));
                gg.drawLine(x1, y1, (int) (x1 - 7 * Math.cos(ang + 0.5)), (int) (y1 - 7 * Math.sin(ang + 0.5)));
            }
        double[] le = f.local(r.camera.eye[0], r.camera.eye[2]);
        int ex = (int) ((le[0] + 0.5) * Sand.ZELLE / px), ey = (int) ((le[1] + 0.5) * Sand.ZELLE / px);
        gg.setColor(Color.RED);
        gg.drawLine(ex - 10, ey, ex + 10, ey);
        gg.drawLine(ex, ey - 10, ex, ey + 10);
        gg.setColor(new Color(14, 20, 22));
        gg.fillRect(0, h, w, 40);
        gg.setColor(Color.WHITE);
        gg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        gg.drawString(String.format(Locale.GERMAN, "Sandsturm am Strand: %s · Wind nach rechts (Feld in Windrichtung) · gold: Fluss bis %.0f g/(m·s), blau: nass, grün: kein loser Sand, Punkte: %d Körner (rot Schweb), Kreuz: Auge",
                f.describe(), qMax * 1000, g.count), 10, h + 26);
        gg.dispose();
        if (out.getParentFile() != null) out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println(s.zeile(r.camera.eye[0], r.camera.eye[2]));
        System.out.println("Bild: " + out.getPath());
        System.exit(0);
    }

    static int rgb(double[] c) {
        int r = (int) Math.max(0, Math.min(255, c[0] * 255)), g = (int) Math.max(0, Math.min(255, c[1] * 255)), b = (int) Math.max(0, Math.min(255, c[2] * 255));
        return (r << 16) | (g << 8) | b;
    }
}
