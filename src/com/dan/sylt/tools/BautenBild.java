package com.dan.sylt.tools;

import com.dan.sylt.camera.Ansichten;
import com.dan.sylt.scene.Bauten;
import com.dan.sylt.scene.Bauwerke;
import com.dan.sylt.scene.Daten;
import com.dan.sylt.scene.Herde;
import com.dan.sylt.scene.Kante;
import com.dan.sylt.scene.KernKarte;
import com.dan.sylt.scene.Netz;
import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Strandkoerbe;
import com.dan.sylt.scene.Terrain;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * Vorschau der Türme und Bauten ohne Grafikkarte (seit Phase 10): legt die Ansicht einer Vorlage (Leuchturm1–4,
 * Kampen1–4) neben die Vorlage aus {@code templates/}. Das Gelände wird je Bildpunkt abgetastet (Arten des Kerns als
 * Farbe, See unter dem Pegel), die Bauten, Strandkörbe und Galloways als Dreiecke mit Tiefenpuffer gezeichnet, Licht
 * aus einer festen Sonne. Das Bild prüft Lage, Größe und Gestalt, nicht das Licht der Szene.
 * <pre>
 * BautenBild [Vorlage|alle] [ordner]     z. B. BautenBild alle docs/status
 * </pre>
 */
public final class BautenBild {
    private BautenBild() { }

    static final double[][] ALB = {
            {0.31, 0.060, 0.035}, {0.72, 0.72, 0.69}, {0.19, 0.13, 0.075}, {0.060, 0.065, 0.072}, {0.30, 0.065, 0.035},
            {0.13, 0.40, 0.22}, {0.30, 0.32, 0.30}, {0.06, 0.06, 0.06}, {0.025, 0.025, 0.025}, {0.22, 0.19, 0.14},
            {0.19, 0.17, 0.155}, {0.51, 0.30, 0.13}, {0.43, 0.35, 0.24}, {0.10, 0.18, 0.55}, {0.58, 0.46, 0.26},
            {0.020, 0.018, 0.016}, {0.60, 0.57, 0.50}, {0.80, 0.80, 0.78}, {0.015, 0.015, 0.015}, {0.02, 0.025, 0.03},
            {0.02, 0.08, 0.05}, {0.70, 0.70, 0.68}, {0.60, 0.02, 0.02}, {0.80, 0.60, 0.02}, {0.42, 0.16, 0.05}, {0.62, 0.56, 0.45},
            {0.52, 0.43, 0.28}};

    public static void main(String[] a) throws Exception {
        String was = a.length > 0 ? a[0] : "alle";
        File dir = new File(a.length > 1 ? a[1] : "docs/status");
        dir.mkdirs();
        long t0 = System.nanoTime();
        Terrain t = Terrain.load(Terrain.defaultDir());
        KernKarte karte = KernKarte.load(Daten.ordner());
        double[][] tu = {{Site.QUERMARKE.x(), Site.QUERMARKE.z()}, {Site.LEUCHTTURM.x(), Site.LEUCHTTURM.z()}};
        Bauwerke w = new Bauwerke(Bauten.laden(Daten.ordner(), tu, 7), t::height, Kante.load(Daten.ordner()));
        Strandkoerbe k = new Strandkoerbe(t::height);
        k.stellen(java.time.LocalDate.of(2026, 7, 20), 270, 6, 290);
        java.util.List<double[]> nahe = new java.util.ArrayList<>();
        for (Bauten.Haus h : w.bauten.haeuser) if (Math.hypot(h.x() - Site.LEUCHTTURM.x(), h.z() - Site.LEUCHTTURM.z()) < 500) nahe.add(new double[]{h.x(), h.z()});
        Herde herde = new Herde(14, Site.LEUCHTTURM.x(), Site.LEUCHTTURM.z(), t::height, nahe.toArray(new double[0][]));
        herde.schritt(1);
        herde.bauen();
        Netz alle = new Netz();
        alle.anhaengen(w.netz);
        alle.anhaengen(k.netz);
        alle.anhaengen(herde.netz);
        System.out.println("Bauten: " + w.describe());
        System.out.printf(Locale.GERMAN, "geladen in %.1f s%n", (System.nanoTime() - t0) / 1e9);
        for (Ansichten.Vorlage v : Ansichten.VORLAGEN) {
            if (!was.equalsIgnoreCase("alle") && !was.equalsIgnoreCase(v.name())) continue;
            File tpl = new File("templates", v.bild());
            BufferedImage vorlage = tpl.isFile() ? ImageIO.read(tpl) : null;
            int h = 540, wd = vorlage != null ? (int) Math.round(h * vorlage.getWidth() / (double) vorlage.getHeight()) : 810;
            long t1 = System.nanoTime();
            BufferedImage img = bild(v.ansicht(), wd, h, t, karte, alle);
            BufferedImage out = new BufferedImage(wd * 2 + 10, h + 26, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, out.getWidth(), out.getHeight());
            if (vorlage != null) g.drawImage(vorlage, 0, 26, wd, h, null);
            g.drawImage(img, wd + 10, 26, null);
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            Ansichten.Ansicht an = v.ansicht();
            g.drawString(String.format(Locale.GERMAN, "%s: Vorlage | Vorschau (ohne Grafikkarte) – Blick %03.0f°, Neigung %.1f°, Öffnung %.1f°, Auge %.1f m",
                    v.name(), an.heading(), an.pitch(), an.fovy(), an.eyeAboveGround()), 6, 18);
            g.dispose();
            File f = new File(dir, "Vorschau_" + v.name() + ".png");
            ImageIO.write(out, "png", f);
            System.out.printf(Locale.GERMAN, "%s (%.1f s)%n", f.getPath(), (System.nanoTime() - t1) / 1e9);
        }
    }

    /** Ein Bild der Ansicht a: Gelände je Bildpunkt abgetastet, Bauten mit Tiefenpuffer. */
    static BufferedImage bild(Ansichten.Ansicht a, int w, int h, Terrain t, KernKarte karte, Netz n) {
        double g0 = Math.max(t.height(a.x(), a.z()), 0.5);
        double[] eye = {a.x(), g0 + a.eyeAboveGround(), a.z()};
        double hd = Math.toRadians(a.heading()), pt = Math.toRadians(a.pitch());
        double[] fw = {Math.sin(hd) * Math.cos(pt), -Math.sin(pt), -Math.cos(hd) * Math.cos(pt)};
        double[] rt = {Math.cos(hd), 0, Math.sin(hd)};
        double[] up = {rt[1] * fw[2] - rt[2] * fw[1], rt[2] * fw[0] - rt[0] * fw[2], rt[0] * fw[1] - rt[1] * fw[0]};
        double th = Math.tan(Math.toRadians(a.fovy()) / 2), tw = th * w / h;
        double[] sun = norm(new double[]{-0.45, 0.6, 0.45});
        double[] depth = new double[w * h];
        int[] rgb = new int[w * h];
        double pxRad = 2 * th / h;
        IntStream.range(0, h).parallel().forEach(j -> {
            for (int i = 0; i < w; i++) {
                double sx = ((i + 0.5) / w * 2 - 1) * tw, sy = (1 - (j + 0.5) / h * 2) * th;
                double[] d = norm(new double[]{fw[0] + rt[0] * sx + up[0] * sy, fw[1] + rt[1] * sx + up[1] * sy, fw[2] + rt[2] * sx + up[2] * sy});
                double dz = d[0] * fw[0] + d[1] * fw[1] + d[2] * fw[2];
                double[] c = null;
                double tt = 0.3, prev = 0;
                double pegel = 0.5;
                while (tt < 25_000) {
                    double x = eye[0] + d[0] * tt, y = eye[1] + d[1] * tt, z = eye[2] + d[2] * tt;
                    double drop = tt * tt / (2 * 6_371_000.0);
                    double gh = Math.max(t.height(x, z), pegel) - drop;
                    if (y < gh) {
                        double lo = prev, hi = tt;
                        for (int q = 0; q < 12; q++) {
                            double m = (lo + hi) / 2;
                            double mx = eye[0] + d[0] * m, mz = eye[2] + d[2] * m;
                            if (eye[1] + d[1] * m < Math.max(t.height(mx, mz), pegel) - m * m / 12_742_000.0) hi = m; else lo = m;
                        }
                        tt = hi;
                        x = eye[0] + d[0] * tt; z = eye[2] + d[2] * tt;
                        if (t.height(x, z) <= pegel) {
                            double f = 0.02 + 0.98 * Math.pow(1 - Math.max(0, -d[1]), 5);
                            c = mix(new double[]{0.03, 0.08, 0.12}, sky(new double[]{d[0], -d[1], d[2]}), Math.min(1, f + 0.25));
                        } else {
                            double e = Math.max(0.5, tt * pxRad * 2);
                            double hx = t.height(x + e, z) - t.height(x - e, z), hz = t.height(x, z + e) - t.height(x, z - e);
                            double[] nn = norm(new double[]{-hx, 2 * e, -hz});
                            int art = karte.at(t, x, z);
                            double[] al = artFarbe(art, karte);
                            double l = Math.max(0, dot(nn, sun)) * 0.85 + 0.35;
                            c = new double[]{al[0] * l, al[1] * l, al[2] * l};
                        }
                        c = dunst(c, d, tt);
                        break;
                    }
                    prev = tt;
                    tt += Math.max(0.15, tt * Math.max(0.002, pxRad * 3));
                }
                int k = j * w + i;
                if (c == null) { c = sky(d); depth[k] = Double.MAX_VALUE; } else depth[k] = tt * dz;
                rgb[k] = pack(c);
            }
        });
        // Bauten: Dreiecke in die Ansicht
        int m = n.ecken();
        double[][] v = new double[3][];
        double[] pv = new double[3];
        for (int q = 0; q + 2 < m; q += 3) {
            double cx = (n.x(q) + n.x(q + 1) + n.x(q + 2)) / 3 - eye[0], cz = (n.z(q) + n.z(q + 1) + n.z(q + 2)) / 3 - eye[2];
            if (cx * cx + cz * cz > 9e6 && a.fovy() > 10) continue;
            boolean drin = false;
            for (int r = 0; r < 3; r++) {
                double px = n.x(q + r) - eye[0], py = n.y(q + r) - eye[1], pz = n.z(q + r) - eye[2];
                double dist2 = px * px + pz * pz;
                py += -dist2 / (2 * 6_371_000.0);
                double zc = px * fw[0] + py * fw[1] + pz * fw[2];
                double xc = px * rt[0] + py * rt[1] + pz * rt[2], yc = px * up[0] + py * up[1] + pz * up[2];
                if (zc > 0.3) drin = true;
                v[r] = new double[]{(xc / (zc * tw) + 1) / 2 * w, (1 - yc / (zc * th)) / 2 * h, zc};
            }
            if (!drin || v[0][2] < 0.3 || v[1][2] < 0.3 || v[2][2] < 0.3) continue;
            double[] nn = n.normal(q);
            double[] toEye = {-(n.x(q) - eye[0]), -(n.y(q) - eye[1]), -(n.z(q) - eye[2])};
            if (dot(nn, toEye) < 0) nn = new double[]{-nn[0], -nn[1], -nn[2]};
            int mat = Netz.baustoff(n.merkmal(q));
            double[] al = ALB[Math.min(mat, ALB.length - 1)];
            double l = Math.max(0, dot(norm(nn), sun)) * 0.85 + 0.35 * (0.55 + 0.45 * nn[1]) + (mat == 6 || mat == 7 ? 0.3 : 0);
            double dist = Math.sqrt(cx * cx + cz * cz);
            double[] col = dunst(new double[]{al[0] * l, al[1] * l, al[2] * l}, norm(new double[]{cx, 0, cz}), dist);
            raster(v, depth, rgb, w, h, pack(col));
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, rgb, 0, w);
        return img;
    }

    static void raster(double[][] v, double[] depth, int[] rgb, int w, int h, int c) {
        double minX = Math.max(0, Math.floor(Math.min(v[0][0], Math.min(v[1][0], v[2][0]))));
        double maxX = Math.min(w - 1, Math.ceil(Math.max(v[0][0], Math.max(v[1][0], v[2][0]))));
        double minY = Math.max(0, Math.floor(Math.min(v[0][1], Math.min(v[1][1], v[2][1]))));
        double maxY = Math.min(h - 1, Math.ceil(Math.max(v[0][1], Math.max(v[1][1], v[2][1]))));
        if (minX > maxX || minY > maxY) return;
        double area = (v[1][0] - v[0][0]) * (v[2][1] - v[0][1]) - (v[2][0] - v[0][0]) * (v[1][1] - v[0][1]);
        if (Math.abs(area) < 1e-9) {
            // dünn: wenigstens ein Bildpunkt (Pfähle, Geländer in der Ferne)
            int px = (int) Math.round(v[0][0]), py = (int) Math.round(v[0][1]);
            if (px >= 0 && py >= 0 && px < w && py < h && v[0][2] < depth[py * w + px]) { depth[py * w + px] = v[0][2]; rgb[py * w + px] = c; }
            return;
        }
        for (int y = (int) minY; y <= maxY; y++)
            for (int x = (int) minX; x <= maxX; x++) {
                double px = x + 0.5, py = y + 0.5;
                double w0 = ((v[1][0] - px) * (v[2][1] - py) - (v[2][0] - px) * (v[1][1] - py)) / area;
                double w1 = ((v[2][0] - px) * (v[0][1] - py) - (v[0][0] - px) * (v[2][1] - py)) / area;
                double w2 = 1 - w0 - w1;
                if (w0 < 0 || w1 < 0 || w2 < 0) continue;
                double iz = w0 / v[0][2] + w1 / v[1][2] + w2 / v[2][2];
                double z = 1 / iz;
                int k = y * w + x;
                if (z < depth[k]) { depth[k] = z; rgb[k] = c; }
            }
    }

    static double[] artFarbe(int art, KernKarte k) {
        return new double[]{k.albedo[art * 3], k.albedo[art * 3 + 1], k.albedo[art * 3 + 2]};
    }

    static double[] sky(double[] d) {
        double u = Math.max(0, d[1]);
        return mix(new double[]{0.62, 0.72, 0.85}, new double[]{0.18, 0.34, 0.72}, Math.pow(u, 0.6));
    }

    static double[] dunst(double[] c, double[] d, double dist) {
        double f = 1 - Math.exp(-dist / 30_000);
        return mix(c, new double[]{0.62, 0.72, 0.85}, f);
    }

    static double[] mix(double[] a, double[] b, double t) {
        return new double[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
    }

    static double dot(double[] a, double[] b) { return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]; }

    static double[] norm(double[] a) {
        double l = Math.sqrt(dot(a, a));
        return new double[]{a[0] / l, a[1] / l, a[2] / l};
    }

    static int pack(double[] c) {
        int r = (int) Math.round(255 * Math.pow(Math.max(0, Math.min(1, c[0] * 1.1)), 1 / 2.2));
        int g = (int) Math.round(255 * Math.pow(Math.max(0, Math.min(1, c[1] * 1.1)), 1 / 2.2));
        int b = (int) Math.round(255 * Math.pow(Math.max(0, Math.min(1, c[2] * 1.1)), 1 / 2.2));
        return (r << 16) | (g << 8) | b;
    }
}
