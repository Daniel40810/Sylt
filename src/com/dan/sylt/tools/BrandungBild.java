package com.dan.sylt.tools;

import com.dan.sylt.gl.Renderer;
import com.dan.sylt.scene.Brandung;
import com.dan.sylt.scene.Buhnen;
import com.dan.sylt.scene.Kueste;
import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Terrain;

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
 * Prüfbild der Brandung ohne Grafikkarte (seit Phase 7a): der Kern von oben, 2 m je Bildpunkt. Das Gelände schattiert,
 * die See nach der Tiefe, darauf, was {@code ocean} aus der gebackenen Textur zeichnet: die Walzen zu einem Zeitpunkt (mit
 * der Phase der Wellen), der Schaumteppich und die Strömung als Pfeile (seewärts rot: Rippströme); die Buhnen schwarz.
 * Rechts drei Querprofile mit Grund, H_s und Anteil der Brecher.
 * <pre>
 * BrandungBild datei.png [H_s T_p Richtung] [Pegel]
 * </pre>
 */
public final class BrandungBild {
    private BrandungBild() { }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "docs/status/Brandung_Phase7a.png");
        Renderer r = new Renderer();
        if (a.length > 3) {
            r.wiring.surf.hs = Double.parseDouble(a[1]);
            r.wiring.surf.tp = Double.parseDouble(a[2]);
            r.wiring.surf.seaFromDeg = Double.parseDouble(a[3]);
        }
        if (a.length > 4) { r.wiring.tideAn = false; r.wiring.pegel = Double.parseDouble(a[4]); }
        else r.wiring.stepTide();
        Brandung b = r.brandung;
        b.computeNow(0);
        Terrain t = r.terrain;
        double e0 = 455_700, e1 = 457_900, n0 = 6_088_000, n1 = 6_091_600, px = 2;
        int w = (int) ((e1 - e0) / px), h = (int) ((n1 - n0) / px), side = 760;
        BufferedImage img = new BufferedImage(w + side, h, BufferedImage.TYPE_INT_RGB);
        double pegel = r.wiring.pegel, tp = r.wiring.surf.tp, zeit = 3.0;
        for (int j = 0; j < h; j++)
            for (int i = 0; i < w; i++) {
                double[] p = Site.fromUtm(e0 + (i + 0.5) * px, n1 - (j + 0.5) * px);
                double g = t.height(p[0], p[1]);
                double gx = t.height(p[0] + 2, p[1]) - t.height(p[0] - 2, p[1]), gz = t.height(p[0], p[1] + 2) - t.height(p[0], p[1] - 2);
                double shade = Math.max(0.35, Math.min(1.3, 1 + (-gx * 0.6 + gz * 0.6) / 4));
                double[] c;
                double tiefe = pegel + b.texAt(p[0], p[1], 3) - g;
                if (tiefe > 0) {
                    double k = Math.min(1, tiefe / 8);
                    c = new double[]{0.30 - 0.22 * k, 0.40 - 0.26 * k, 0.36 - 0.22 * k};
                    double qb = b.texAt(p[0], p[1], 0), ph = b.texAt(p[0], p[1], 1), sch = b.texAt(p[0], p[1], 2);
                    double s = ((zeit / tp - ph) % 1 + 1) % 1;
                    // wie surfBreaks in ocean: je Welle eigenes Rauschen längs des Kamms
                    double wave = Math.floor(zeit / tp - ph);
                    double nz = vnoise(p[0] / 38 + wave * 7.31, p[1] / 38 + wave * 3.17) * 0.7 + vnoise(p[0] / 11 + wave * 1.9, p[1] / 11 - wave * 5.3) * 0.3;
                    double brk = smooth(nz - 0.08, nz + 0.08, Math.min(1, qb * 1.15));
                    double walze = brk * (Math.exp(-s / 0.06) + 0.5 * Math.exp(-s / 0.22));
                    double f = Math.min(1, Math.max(walze, sch * 0.8));
                    for (int q = 0; q < 3; q++) c[q] = c[q] * (1 - f) + 0.92 * f;
                } else if (g < pegel + 3) {
                    double nass = Math.max(0, 1 - (g - pegel) / 1.2);
                    c = new double[]{(0.86 - 0.3 * nass) * shade, (0.80 - 0.28 * nass) * shade, (0.66 - 0.22 * nass) * shade};
                } else {
                    double rot = Math.max(0, Math.min(1, (g - 8) / 12));
                    c = new double[]{(0.42 + 0.25 * rot) * shade, (0.46 - 0.05 * rot) * shade, (0.30 - 0.05 * rot) * shade};
                }
                img.setRGB(i, j, rgb(c));
            }
        Graphics2D gr = img.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Strömung: Pfeile alle 60 m, rot wo sie seewärts zieht
        for (double e = e0 + 30; e < e1; e += 60)
            for (double n = n0 + 30; n < n1; n += 60) {
                double[] p = Site.fromUtm(e, n);
                double[] u = b.stromAt(p[0], p[1]);
                double sp = Math.hypot(u[0], u[1]);
                if (sp < 0.15) continue;
                double see = -(u[0] * Kueste.SX + u[1] * Kueste.SZ);
                gr.setColor(see > 0.15 && see > 0.6 * sp ? new Color(230, 70, 50) : new Color(255, 255, 255, 170));
                gr.setStroke(new BasicStroke(1.4f));
                double l = Math.min(45, 30 * sp) / px;
                int x0 = (int) ((e - e0) / px), y0 = (int) ((n1 - n) / px);
                int x1 = (int) (x0 + u[0] / sp * l), y1 = (int) (y0 + u[1] / sp * l);
                gr.drawLine(x0, y0, x1, y1);
                double ang = Math.atan2(y1 - y0, x1 - x0);
                gr.drawLine(x1, y1, (int) (x1 - 5 * Math.cos(ang - 0.5)), (int) (y1 - 5 * Math.sin(ang - 0.5)));
                gr.drawLine(x1, y1, (int) (x1 - 5 * Math.cos(ang + 0.5)), (int) (y1 - 5 * Math.sin(ang + 0.5)));
            }
        gr.setColor(Color.BLACK);
        for (Buhnen.Pfahl p : b.buhnen.pfaehle) {
            double[] u = Site.toUtm(p.x(), p.z());
            gr.fillRect((int) ((u[0] - e0) / px) - 1, (int) ((n1 - u[1]) / px) - 1, 2, 2);
        }
        // Querprofile rechts
        gr.setColor(new Color(14, 20, 22));
        gr.fillRect(w, 0, side, h);
        double[] nn = {6_091_000, 6_089_500, 6_088_400};
        String[] namen = {"Kampen (N 6 091 000)", "Kliffkante (N 6 089 500)", "Wenningstedt (N 6 088 400)"};
        int ph = h / 3;
        gr.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        for (int k = 0; k < 3; k++) {
            int y0 = k * ph + 30, hh = ph - 60, x0 = w + 40, ww = side - 70;
            gr.setColor(new Color(233, 236, 234));
            gr.drawString(namen[k] + ": Grund, H_s (blau), Brecher Q_b (weiß)", x0, y0 - 8);
            double dMin = -60, dMax = 800;
            gr.setColor(new Color(60, 70, 72));
            gr.drawRect(x0, y0, ww, hh);
            int yPegel = (int) (y0 + hh * (3 - pegel) / 13.0);
            gr.drawLine(x0, yPegel, x0 + ww, yPegel);
            int lx = -1, ly = -1, hx = -1, hy = -1, qx = -1, qy = -1;
            for (double d = dMin; d <= dMax; d += 4) {
                double[] p = Site.fromUtm(Site.wasserlinieE(nn[k]) - d, nn[k]);
                int x = (int) (x0 + ww * (dMax - d) / (dMax - dMin));
                double g = r.wiring.surf.bed.height(p[0], p[1]);
                int y = (int) (y0 + hh * (3 - g) / 13.0);
                double hs = b.feld.hsAt(p[0], p[1]), qb = b.feld.breakingAt(p[0], p[1]);
                int yh = (int) (y0 + hh * (1 - (Double.isNaN(hs) ? 0 : hs) / 3.0)), yq = (int) (y0 + hh * (1 - qb));
                if (lx >= 0) {
                    gr.setColor(new Color(216, 195, 154)); gr.drawLine(lx, ly, x, y);
                    gr.setColor(new Color(121, 163, 214)); gr.drawLine(hx, hy, x, yh);
                    gr.setColor(new Color(240, 240, 240, 180)); gr.drawLine(qx, qy, x, yq);
                }
                lx = x; ly = y; hx = x; hy = yh; qx = x; qy = yq;
            }
            gr.setColor(new Color(147, 160, 162));
            gr.drawString("800 m seewärts", x0 + 4, y0 + hh - 6);
            gr.drawString("Wasserlinie", x0 + ww - 95, y0 + hh - 6);
        }
        gr.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        gr.setColor(Color.WHITE);
        gr.drawString(String.format(Locale.GERMAN, "See H_s %.1f m, T_p %.0f s aus %03.0f° · Pegel %+.2f m NHN · Walzen zum Zeitpunkt %.0f s · Pfeile: Strömung (rot: Rippstrom)",
                r.wiring.surf.hs, tp, r.wiring.surf.seaFromDeg, pegel, zeit), 10, h - 12);
        gr.dispose();
        if (out.getParentFile() != null) out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println(b.describe());
        System.out.println("Bild: " + out.getPath());
        System.exit(0);
    }

    static double hash(double x, double y) { double v = Math.sin(x * 127.1 + y * 311.7) * 43758.5453; return v - Math.floor(v); }

    static double vnoise(double x, double y) {
        double ix = Math.floor(x), iy = Math.floor(y), fx = x - ix, fy = y - iy;
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);
        double a = hash(ix, iy), b = hash(ix + 1, iy), c = hash(ix, iy + 1), d = hash(ix + 1, iy + 1);
        return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy;
    }

    static double smooth(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    static int rgb(double[] c) {
        int r = (int) Math.max(0, Math.min(255, c[0] * 255)), g = (int) Math.max(0, Math.min(255, c[1] * 255)), b = (int) Math.max(0, Math.min(255, c[2] * 255));
        return (r << 16) | (g << 8) | b;
    }
}
