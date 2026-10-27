package com.dan.sylt.scene;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleBinaryOperator;

/**
 * Strandkörbe (seit Phase 10, F20 A): in Gruppen an den Strandaufgängen auf dem trockenen oberen Strand, in Reihen
 * parallel zur Wasserlinie [G: Kampen 48, Rotes Kliff 24, Wenningstedt 60].
 * <ul>
 * <li><b>Jahreszeit:</b> vom 1. Mai bis 30. September alle, in der zweiten Aprilhälfte und der ersten Oktoberhälfte
 * etwa die Hälfte, sonst keine (im Winter stehen sie im Lager) [G].</li>
 * <li><b>Wind:</b> der Rücken zum Wind, die Öffnung in Lee; bei Wind unter 4 m/s zur See und zur Sonne gedreht. Jeder
 * Korb streut ±20°.</li>
 * <li><b>Sturm:</b> ab Windstärke 8 (17,2 m/s) gibt es eine Sturmwarnung; die Vermieter räumen die Körbe weg [G].</li>
 * </ul>
 * Das Netz wird nur neu gebaut, wenn sich Zahl oder Richtung (auf 5° gerundet) ändern.
 */
public final class Strandkoerbe {
    /** Gruppen: Nordwert (UTM) und Zahl der Körbe [G]. */
    public static final double[][] GRUPPEN = {{6_090_640, 48}, {6_089_640, 24}, {6_087_860, 60}};
    /** Sturmwarnung ab Bft 8 (m/s). */
    public static final double STURM = 17.2;

    public record Korb(double x, double y, double z, double streuung, double zufall) { }

    public final List<Korb> koerbe = new ArrayList<>();
    public final Netz netz = new Netz();
    public volatile boolean an = true;
    private long schluessel = Long.MIN_VALUE;
    private int gezeigt;
    private double richtung;
    private String grund = "";

    public Strandkoerbe(DoubleBinaryOperator h) {
        java.util.Random r = new java.util.Random(1985);
        for (double[] g : GRUPPEN) {
            double nord = g[0], z0 = Site.N0 - nord, wl = Site.wasserlinieE(nord) - Site.E0;
            double fuss = wl + 60;
            for (double x = wl + 10; x < wl + 250; x += 1) if (h.applyAsDouble(x, z0) > 5.0) { fuss = x; break; }
            int n = (int) g[1], reihen = Math.max(1, Math.min(4, (int) ((fuss - wl - 30) / 4.2)));
            int jeReihe = (n + reihen - 1) / reihen;
            for (int i = 0; i < n; i++) {
                int reihe = i / jeReihe, spalte = i % jeReihe;
                double x = fuss - 5 - reihe * 4.2 + (r.nextDouble() - 0.5) * 0.6;
                double z = z0 + (spalte - jeReihe / 2.0) * 3.1 + (r.nextDouble() - 0.5) * 0.5 + (reihe % 2) * 1.5;
                double y = h.applyAsDouble(x, z);
                koerbe.add(new Korb(x, y, z, (r.nextDouble() - 0.5) * 40, r.nextDouble()));
            }
        }
    }

    /** Anteil der Körbe am Strand nach dem Tag [G]. */
    public static double saison(LocalDate d) {
        int m = d.getMonthValue(), t = d.getDayOfMonth();
        if (m >= 5 && m <= 9) return 1;
        if (m == 4 && t >= 15) return 0.5;
        if (m == 10 && t <= 15) return 0.5;
        return 0;
    }

    /** Wohin die Öffnung zeigt (Grad von Nord): in Lee, bei Schwachwind zur See (Westen) und zur Sonne. */
    public static double oeffnung(double windAus, double windStaerke, double sonneAzimut) {
        if (windStaerke >= 4) return (windAus + 180) % 360;
        double a = Math.toRadians(270), b = Math.toRadians(sonneAzimut);
        double w = windStaerke / 4;
        double lee = Math.toRadians(windAus + 180);
        double x = Math.sin(a) * 0.6 + Math.sin(b) * 0.4, z = Math.cos(a) * 0.6 + Math.cos(b) * 0.4;
        x = x * (1 - w) + Math.sin(lee) * w;
        z = z * (1 - w) + Math.cos(lee) * w;
        return (Math.toDegrees(Math.atan2(x, z)) + 360) % 360;
    }

    /** Neu stellen; true, wenn sich das Netz geändert hat. */
    public boolean stellen(LocalDate tag, double windAus, double windStaerke, double sonneAzimut) {
        double s = saison(tag);
        boolean sturm = windStaerke >= STURM;
        int zahl = !an || sturm ? 0 : (int) Math.round(koerbe.size() * s);
        double o = oeffnung(windAus, windStaerke, sonneAzimut);
        long key = zahl * 1000L + Math.round(o / 5);
        grund = !an ? "aus" : sturm ? "Sturmwarnung, weggeräumt" : s == 0 ? "außerhalb der Saison" : s < 1 ? "halbe Saison" : "Saison";
        if (key == schluessel) return false;
        schluessel = key;
        gezeigt = zahl;
        richtung = o;
        netz.leeren();
        for (Korb k : koerbe) {
            if (k.zufall() >= s || zahl == 0) continue;
            bauen(netz, k.x(), k.y(), k.z(), o + k.streuung(), (int) (k.zufall() * 511));
        }
        return true;
    }

    /** Ein Korb (Breite 1,25 m, Tiefe 0,85 m, 1,6 m hoch), Öffnung nach {@code richtung}. */
    public static void bauen(Netz n, double x, double y, double z, double richtung, int kennzahl) {
        int korb = Netz.merkmal(Netz.KORB, false, kennzahl, y), segel = Netz.merkmal(Netz.SEGEL, false, kennzahl, y);
        double a = Math.toRadians(richtung), fx = Math.sin(a), fz = -Math.cos(a);
        // Sockel (Fußkasten), Sitz, Rückwand, Seiten, Haube
        n.quader(x, y - 0.05, z, 1.25, 0.85, 0.50, richtung, korb);
        n.quader(x - fx * 0.32, y + 0.45, z - fz * 0.32, 1.25, 0.18, 1.10, richtung, korb);
        for (double s : new double[]{-0.58, 0.58}) {
            double rx = -fz * s, rz = fx * s;
            n.quader(x + rx - fx * 0.05, y + 0.45, z + rz - fz * 0.05, 0.09, 0.62, 0.75, richtung, korb);
        }
        double[] h0 = {x - fx * 0.42 + fz * 0.66, y + 1.62, z - fz * 0.42 - fx * 0.66}, h1 = {x - fx * 0.42 - fz * 0.66, y + 1.62, z - fz * 0.42 + fx * 0.66};
        double[] h2 = {x + fx * 0.20 - fz * 0.66, y + 1.25, z + fz * 0.20 + fx * 0.66}, h3 = {x + fx * 0.20 + fz * 0.66, y + 1.25, z + fz * 0.20 - fx * 0.66};
        n.viereck(h0, h1, h2, h3, korb);
        // Stoff innen an der Rückwand (gestreift)
        double cx = x - fx * 0.225, cz = z - fz * 0.225;
        double[] s0 = {cx + fz * 0.56, y + 0.5, cz - fx * 0.56}, s1 = {cx - fz * 0.56, y + 0.5, cz + fx * 0.56};
        double[] s2 = {cx - fz * 0.56, y + 1.5, cz + fx * 0.56}, s3 = {cx + fz * 0.56, y + 1.5, cz - fx * 0.56};
        n.dreieck(s0, s1, s2, new double[]{fx, 0, fz}, segel);
        n.dreieck(s0, s2, s3, new double[]{fx, 0, fz}, segel);
    }

    public int gezeigt() { return gezeigt; }

    /** „84 Strandkörbe (Saison), Öffnung nach 095°“ */
    public String describe() {
        return String.format(Locale.GERMAN, "%d von %d Strandkörben (%s), Öffnung nach %03.0f°", gezeigt, koerbe.size(), grund, richtung);
    }
}
