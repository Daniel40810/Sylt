package com.dan.sylt.data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Die Kliffkarte (F45 A, Knopf im Block CHRONIK): alle Kanten des Kliffs in einem Bild. Oben der Grundriss längs der
 * Küste (Nord nach rechts, Ost nach oben, Ost überhöht): die Kante 2024 grau, der Fuß dunkel, jedes Stück nach einem
 * Abbruch farbig nach der Zeit (früh blau, spät rot), die Bruchstellen als Punkte. Unten, wie weit die Kante gegenüber
 * 2024 zurückgegangen ist (m), je Abbruch.
 */
public final class Kliffkarte {
    private Kliffkarte() { }

    public static final int B = 1600, H = 900;
    private static final double N0 = 6_088_500, N1 = 6_091_800;

    /** Das Bild aus den Kanten (Stand 2024 und nach den Abbrüchen) und den Abbrüchen. */
    public static BufferedImage bild(List<Daten.Kantenstueck> kanten, List<Daten.Abbruch> abbrueche, String titel) {
        BufferedImage img = new BufferedImage(B, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(16, 22, 24));
        g.fillRect(0, 0, B, H);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(new Color(232, 220, 200));
        g.drawString(titel, 30, 38);

        List<Daten.Kantenstueck> alt = new ArrayList<>(), neu = new ArrayList<>();
        for (Daten.Kantenstueck k : kanten) (k.abbruch() == null && k.szeneMs() == null ? alt : neu).add(k);
        long t0 = Long.MAX_VALUE, t1 = Long.MIN_VALUE;
        for (Daten.Kantenstueck k : neu) if (k.szeneMs() != null) { t0 = Math.min(t0, k.szeneMs()); t1 = Math.max(t1, k.szeneMs()); }

        // Ost-Bereich aus den Kanten 2024
        double e0 = Double.MAX_VALUE, e1 = -Double.MAX_VALUE;
        TreeMap<Double, Double> kante2024 = new TreeMap<>();
        for (Daten.Kantenstueck k : alt)
            for (Daten.Geom g2 : new Daten.Geom[]{k.oberkante(), k.fuss()}) {
                if (g2 == null) continue;
                for (double[] p : g2.xz()) {
                    double[] u = Daten.utm(p[0], p[1]);
                    e0 = Math.min(e0, u[0]);
                    e1 = Math.max(e1, u[0]);
                    if (g2 == k.oberkante()) kante2024.put(u[1], u[0]);
                }
            }
        if (e0 > e1) { e0 = 456_000; e1 = 457_700; }
        e0 -= 20;
        e1 += 20;

        // oben: Grundriss
        int ox = 90, oy = 70, w = B - 140, h = 470;
        rahmen(g, ox, oy, w, h, "Grundriss: Nord →, Ost ↑ (überhöht)");
        double fe0 = e0, fe1 = e1;
        java.util.function.DoubleUnaryOperator px = n -> ox + (n - N0) / (N1 - N0) * w, py = e -> oy + h - (e - fe0) / (fe1 - fe0) * h;
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (double n = 6_089_000; n <= N1; n += 500) {
            int x = (int) px.applyAsDouble(n);
            g.setColor(new Color(40, 52, 56));
            g.drawLine(x, oy, x, oy + h);
            g.setColor(new Color(150, 160, 160));
            g.drawString(String.format(Locale.GERMAN, "N %,.0f", n), x - 40, oy + h + 16);
        }
        for (Daten.Kantenstueck k : alt) {
            linie(g, k.fuss(), px, py, new Color(110, 84, 70), 1.2f);
            linie(g, k.oberkante(), px, py, new Color(190, 190, 184), 1.6f);
        }
        for (Daten.Kantenstueck k : neu) linie(g, k.oberkante(), px, py, zeitFarbe(k.szeneMs(), t0, t1), 2.6f);
        for (Daten.Abbruch a : abbrueche) {
            if (a.bruchstelle() == null) continue;
            double[] u = Daten.utm(a.bruchstelle()[0], a.bruchstelle()[1]);
            g.setColor(zeitFarbe(a.ms(), t0, t1));
            int x = (int) px.applyAsDouble(u[1]), y = (int) py.applyAsDouble(u[0]);
            g.fillOval(x - 4, y - 4, 8, 8);
        }
        g.setColor(new Color(150, 160, 160));
        g.drawString(String.format(Locale.GERMAN, "E %,.0f", e1), 10, oy + 12);
        g.drawString(String.format(Locale.GERMAN, "E %,.0f", e0), 10, oy + h);

        // unten: Rückgang gegenüber 2024
        int uy = oy + h + 50, uh = H - uy - 60;
        double rmax = 2;
        for (Daten.Abbruch a : abbrueche) rmax = Math.max(rmax, Math.ceil(a.rueckgang() + 0.5));
        for (Daten.Kantenstueck k : neu) if (k.oberkante() != null) for (double[] p : k.oberkante().xz()) {
            double[] u = Daten.utm(p[0], p[1]);
            Double alt0 = naechste(kante2024, u[1]);
            if (alt0 != null) rmax = Math.max(rmax, Math.ceil(u[0] - alt0 + 0.5));
        }
        rahmen(g, ox, uy, w, uh, String.format(Locale.GERMAN, "Rückgang der Kante gegenüber 2024 (m, bis %.0f)", rmax));
        double rm = rmax;
        java.util.function.DoubleUnaryOperator ry = d -> uy + uh - Math.max(0, Math.min(1, d / rm)) * uh;
        for (Daten.Kantenstueck k : neu) {
            if (k.oberkante() == null) continue;
            Path2D.Double p = new Path2D.Double();
            boolean erst = true;
            for (double[] q : k.oberkante().xz()) {
                double[] u = Daten.utm(q[0], q[1]);
                Double alt0 = naechste(kante2024, u[1]);
                if (alt0 == null) continue;
                double x = px.applyAsDouble(u[1]), y = ry.applyAsDouble(u[0] - alt0);
                if (erst) { p.moveTo(x, y); erst = false; } else p.lineTo(x, y);
            }
            g.setColor(zeitFarbe(k.szeneMs(), t0, t1));
            g.setStroke(new BasicStroke(2f));
            g.draw(p);
        }
        // Legende
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        int ly = H - 22;
        g.setColor(new Color(190, 190, 184));
        g.drawString("Kante 2024 (gemessen)", ox, ly);
        g.setColor(new Color(110, 84, 70));
        g.drawString("Fuß 2024", ox + 190, ly);
        String zeit = neu.isEmpty() ? "noch kein Abbruch" : String.format(Locale.GERMAN, "%d Abbrüche · %s (blau) bis %s (rot)", abbrueche.size(),
                Daten.text(t0), Daten.text(t1));
        for (int i = 0; i < 60; i++) { g.setColor(zeitFarbe((long) i, 0, 59)); g.fillRect(ox + 300 + i * 2, ly - 10, 2, 10); }
        g.setColor(new Color(220, 210, 190));
        g.drawString(zeit, ox + 430, ly);
        g.dispose();
        return img;
    }

    static Double naechste(TreeMap<Double, Double> m, double n) {
        var a = m.floorEntry(n);
        var b = m.ceilingEntry(n);
        var best = a == null ? b : b == null ? a : (n - a.getKey() <= b.getKey() - n ? a : b);
        return best == null || Math.abs(best.getKey() - n) > 4 ? null : best.getValue();
    }

    private static void rahmen(Graphics2D g, int x, int y, int w, int h, String t) {
        g.setColor(new Color(24, 32, 35));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(60, 74, 78));
        g.drawRect(x, y, w, h);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(170, 182, 184));
        g.drawString(t, x + 6, y - 6);
    }

    private static void linie(Graphics2D g, Daten.Geom geom, java.util.function.DoubleUnaryOperator px, java.util.function.DoubleUnaryOperator py, Color c, float breite) {
        if (geom == null) return;
        for (double[][] teil : geom.teile()) {
            Path2D.Double p = new Path2D.Double();
            for (int i = 0; i < teil.length; i++) {
                double[] u = Daten.utm(teil[i][0], teil[i][1]);
                double x = px.applyAsDouble(u[1]), y = py.applyAsDouble(u[0]);
                if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
            }
            g.setColor(c);
            g.setStroke(new BasicStroke(breite));
            g.draw(p);
        }
    }

    /** Früh blau, spät rot. */
    static Color zeitFarbe(Long t, long t0, long t1) {
        double a = t == null || t1 <= t0 ? 1 : Math.max(0, Math.min(1, (t - t0) / (double) (t1 - t0)));
        return Color.getHSBColor((float) (0.62 - 0.62 * a), 0.75f, 0.95f);
    }

    /** Die Karte aus dem Speicher als PNG in den Ordner {@code daten/kliffkarte} schreiben; gibt die Datei zurück. */
    public static File schreiben(Speicher s, File ordner) throws com.dan.store.Ablage.Fehler, IOException {
        List<Daten.Kantenstueck> k = s.kanten();
        List<Daten.Abbruch> a = s.abbrueche(10_000);
        BufferedImage img = bild(k, a, "Das Rote Kliff · alle Kanten · " + s.wo());
        ordner.mkdirs();
        File f = new File(ordner, "Kliffkarte_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png");
        javax.imageio.ImageIO.write(img, "png", f);
        return f;
    }
}
