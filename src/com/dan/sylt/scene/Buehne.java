package com.dan.sylt.scene;

import java.util.Locale;

/**
 * Die Bühne von Phase 1: ein gerechneter Platzhalter für das Gelände, bis Phase 2 DGM1, BAW und bDOM aufbereitet
 * ({@code daten/gelaende}). Sie folgt den vier Querprofilen, die für das Werkbuch aus der BAW gelesen wurden
 * (Abschnitt 2.3), und ist sonst gestaltet [G], damit Ansichten, Marken und Kamera in Phase 1 schon am richtigen Ort
 * stehen. Quer zur Küste, gemessen als Abstand s von der Wasserlinie (NHN 0, landwärts positiv):
 * <ul>
 * <li><b>See</b> (s &lt; 0): h = H∞·(1 − e^(s/L)) + Vorstrand mit H∞ = 13,3 m und L = 1,8 km [G], so dass 8 km draußen −13,8 m
 * erreicht sind (BAW: −13,2 bis −14,1 m); darauf ein Riff rund 450 m vor dem Strand mit einer Rinne landwärts (BAW bei
 * Wenningstedt seewärts −5,4, Riff −2,2, Rinne −3,3 m). Ein Gleichgewichtsprofil nach Dean (h = A·d^⅔) passt nicht
 * zugleich zu beidem: mit 8 km auf −13,5 m läge das Riff nur 0,4 m tief (Befund der Prüfung in Phase 1). Der Vorstrand
 * unter Wasser fällt in den ersten 40 m mit 1:30 [G].</li>
 * <li><b>Strand</b> (0 bis 60 m): steigt auf rund NHN +3 m.</li>
 * <li><b>Kliff</b> (60 bis 72 m): die Wand bis zur Oberkante, die längs der Küste wie in der BAW läuft: bei N 6 088 000
 * rund +19 m, bei N 6 089 500 rund +28 m, nach Norden zu den Dünen bei N 6 091 000 auf +8 m.</li>
 * <li><b>Heide</b> dahinter, landwärts fallend bis zum Watt rund 2,8 km östlich der Wasserlinie [G]; darauf die Uwe-Düne
 * (52,5 m), die Düne des Quermarkenfeuers und Dünenkuppen im Norden [G].</li>
 * <li><b>Insel</b> nur von N 6 070 000 bis 6 103 000 [G]; außerhalb und im Osten Wattenmeer und See.</li>
 * </ul>
 * Seit Phase 1 entsteht daraus mit {@link Terrain#ausBuehne()} das Gelände, solange {@code daten/gelaende} fehlt;
 * gezeichnet wird es wie das echte (mit den Marken aus {@code shader/marken.glsl}).
 */
public final class Buehne {
    private Buehne() { }

    /** Tiefe weit draußen (m) und Länge, auf der der Grund abfällt (m) [G]. */
    public static final double SEE_TIEFE = 13.3, SEE_L = 1800;
    /** Riff: Abstand vor der Wasserlinie (m), Höhe über dem Profil (m), halbe Breite (m); Rinne landwärts: Abstand, Tiefe, Breite [G]. */
    public static final double RIFF_S = -450, RIFF_H = 1.0, RIFF_B = 80, RINNE_S = -300, RINNE_T = 0.9, RINNE_B = 60;

    /** Neigung des nassen Strands unter Wasser (Vorstrand) bis {@link #VORSTRAND_B} m [G]. */
    public static final double VORSTRAND = 1 / 30.0, VORSTRAND_B = 40;

    /** Tiefe des Grunds (m, positiv) im Abstand d vor der Wasserlinie, ohne Riff; die ersten 40 m mit dem Vorstrand. */
    public static double seeTiefe(double d) {
        d = Math.max(0, d);
        double vor = VORSTRAND * VORSTRAND_B / 2 * (1 - Math.exp(-d / (VORSTRAND_B / 2)));
        return SEE_TIEFE * (1 - Math.exp(-d / SEE_L)) + vor;
    }
    /** Strand: Breite (m) und Höhe am Kliffuß (m NHN) [G]; Kliff: Breite der Wand (m) [G]. */
    public static final double STRAND_B = 60, STRAND_H = 3, KLIFF_B = 12;
    /** Abstand der Ostküste der Insel von der Wasserlinie im Westen (m) und Tiefe des Watts (m NHN) [G]. */
    public static final double WATT_S = 2800, WATT_H = -1.2;
    /** Nord- und Südende der Insel (UTM Nord) [G]. */
    public static final double INSEL_N = 6_103_000, INSEL_S = 6_070_000;

    /** Oberkante des Kliffs (m NHN) am Nordwert n, aus den Profilen der BAW [gemessen an drei Stellen, sonst G]. */
    public static double kliffOberkante(double n) {
        double[][] k = {{6_085_000, 12}, {6_088_000, 19}, {6_089_500, 28}, {6_090_400, 22}, {6_091_000, 8}, {6_093_000, 9}};
        if (n <= k[0][0]) return k[0][1];
        for (int i = 1; i < k.length; i++)
            if (n <= k[i][0]) {
                double t = (n - k[i - 1][0]) / (k[i][0] - k[i - 1][0]);
                t = t * t * (3 - 2 * t);
                return k[i - 1][1] + t * (k[i][1] - k[i - 1][1]);
            }
        return k[k.length - 1][1];
    }

    /** Höhe der Bühne über NHN (m) am Ort (x, z). */
    public static double height(double x, double z) {
        double e = x + Site.E0, n = Site.N0 - z;
        double s = e - Site.wasserlinieE(n);
        double see = -seeTiefe(-s);
        double dr = (s - RIFF_S) / RIFF_B, dt = (s - RINNE_S) / RINNE_B;
        see += RIFF_H * Math.exp(-dr * dr) - RINNE_T * Math.exp(-dt * dt);
        see = Math.max(see, -25);
        if (s < 0) return insel(see, s, n);
        double top = kliffOberkante(n);
        double land;
        if (s < STRAND_B) land = STRAND_H * s / STRAND_B;
        else if (s < STRAND_B + KLIFF_B) {
            double t = (s - STRAND_B) / KLIFF_B;
            land = STRAND_H + (top - STRAND_H) * Math.pow(t, 0.7);
        } else {
            double q = s - STRAND_B - KLIFF_B;
            land = top - (top - 4) * smooth(0, WATT_S - 400, q) + 3 * (noise(e / 180, n / 180) - 0.5);
            land += duenen(e, n, q);
            if (q > WATT_S - 400) land += (WATT_H - 4) * smooth(WATT_S - 400, WATT_S, q);
            if (s > WATT_S) land = WATT_H - 2 * smooth(WATT_S, WATT_S + 6000, s);
        }
        return insel(land, s, n);
    }

    /** Art des Grunds der Bühne am Ort mit der Höhe y (für die Farben bis Phase 3) [G]. */
    public static byte art(double x, double z, double y) {
        double e = x + Site.E0, n = Site.N0 - z, s = e - Site.wasserlinieE(n);
        if (y < -1.5 && s < 0) return Terrain.SEE;
        if (s < 0) return y < 0.3 ? Terrain.NASS : Terrain.STRAND;
        if (s > WATT_S - 200 || y < 0.3) return y < 0.6 ? Terrain.WATT : Terrain.WIESE;
        if (s < STRAND_B * 0.35) return Terrain.NASS;
        if (s < STRAND_B) return Terrain.STRAND;
        if (s < STRAND_B + KLIFF_B) return kliffOberkante(n) > 12 ? Terrain.KLIFF : Terrain.DUENE;
        double q = s - STRAND_B - KLIFF_B;
        if (q < 25) return Terrain.DUENE;
        // Kampen östlich des Leuchtturms und Wenningstedt im Süden, in Flecken [G bis DOP20 in Phase 2]
        boolean ort = (e > 457_950 && n > 6_088_600 && n < 6_090_900) || (n < 6_087_900 && q > 300);
        if (ort && noise(e / 70 + 3, n / 70) > 0.45) return Terrain.SIEDLUNG;
        if (ort) return Terrain.WIESE;
        if (duenen(e, n, q) > 4) return Terrain.DUENE;
        return noise(e / 120 + 11, n / 120) > 0.72 ? Terrain.GEBUESCH : Terrain.HEIDE;
    }

    /** Außerhalb der Insel (Nord, Süd) geht das Land weich in See über. */
    static double insel(double h, double s, double n) {
        double aus = Math.max(smooth(INSEL_N - 1500, INSEL_N + 1500, n), 1 - smooth(INSEL_S - 1500, INSEL_S + 1500, n));
        if (aus <= 0) return h;
        double offen = s < 0 ? h : -0.6 * seeTiefe(600 + s);
        return h + (Math.min(h, offen) - h) * aus;
    }

    /** Dünen auf der Heide [G]: Uwe-Düne, die Düne des Quermarkenfeuers, Kuppen im Norden. */
    static double duenen(double e, double n, double q) {
        double h = 0;
        h += kuppe(e, n, Site.UWE_DUENE.e(), Site.UWE_DUENE.n(), 52.5 - 20, 140);
        h += kuppe(e, n, Site.QUERMARKE.e(), Site.QUERMARKE.n(), 8, 90);
        double nord = smooth(6_090_500, 6_091_200, n) * (1 - smooth(6_093_500, 6_094_500, n));
        if (nord > 0 && q < 900) h += nord * 9 * Math.pow(noise(e / 90 + 7, n / 140 + 3), 2) * (1 - q / 900);
        return h;
    }

    static double kuppe(double e, double n, double ce, double cn, double hoehe, double r) {
        double d = Math.hypot(e - ce, n - cn) / r;
        return hoehe * Math.exp(-d * d);
    }

    static double smooth(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    /** Wertrauschen 0..1, fest (seed 2026). */
    static double noise(double x, double y) {
        long ix = (long) Math.floor(x), iy = (long) Math.floor(y);
        double fx = x - ix, fy = y - iy;
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);
        double a = hash(ix, iy), b = hash(ix + 1, iy), c = hash(ix, iy + 1), d = hash(ix + 1, iy + 1);
        return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy;
    }

    static double hash(long x, long y) {
        long h = 2026 * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }

    /** „Kern“, „Fernfeld“, „Fernring“ oder „außerhalb“ am Ort (x, z). */
    public static String zone(double x, double z) {
        if (Site.KERN.contains(x, z)) return Site.KERN.name();
        if (Site.FERNFELD.contains(x, z)) return Site.FERNFELD.name();
        if (Math.hypot(x - Terrain.MX, z - Terrain.MZ) <= Site.FERNRING) return "Fernring";
        return "außerhalb";
    }

    /** Der nächste Ort aus {@link Site#ORTE} und sein Abstand in der Ebene: „240 m bis Uwe-Düne“. */
    public static String naechsterOrt(double x, double z) {
        Site.Ort best = null;
        double bd = Double.MAX_VALUE;
        for (Site.Ort o : Site.ORTE) {
            double d = Math.hypot(x - o.x(), z - o.z());
            if (d < bd) { bd = d; best = o; }
        }
        if (best == null) return "—";
        return bd < 1000 ? String.format(Locale.GERMAN, "%.0f m bis %s", bd, best.name()) : String.format(Locale.GERMAN, "%.1f km bis %s", bd / 1000, best.name());
    }
}
