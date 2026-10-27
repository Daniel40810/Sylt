package com.dan.sylt.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleBinaryOperator;

/**
 * Türme und Bauten (seit Phase 10, F18, F24, F26 A). Ein festes Netz für die Grafikkarte aus
 * <ul>
 * <li><b>den Häusern aus LOD2</b> ({@link Bauten}) mit Baustoffen nach dem Ortsbild (Reet, Backstein, Putz, Klinker),
 * die Wärterhäuser am Kampener Leuchtturm weiß mit grauem Dach;</li>
 * <li><b>dem Quermarkenfeuer Rotes Kliff</b>: Achteck aus rotem Backstein auf Granitsockel, schwarze Galerie mit
 * Geländer, weiße Laterne mit Sprossen, grünes Kupferdach mit Kugel und Spitze [G nach Leuchturm1–4: Schaft 6,8 m,
 * Laterne bis 10,6 m, Spitze 12 m; LOD2: 4,1 m breit, 11,0 m hoch];</li>
 * <li><b>dem Kampener Leuchtturm</b>: weißer, sich verjüngender Rundturm mit schwarzem Band, Galerie, weißes Wachgeschoss,
 * zweite Galerie, schwarze Laterne mit Rautensprossen und Kuppel [G nach Kampen1–4: Fuß 7,3 m (LOD2), Band auf 42–64 %
 * des Schafts; Feuerhöhe 62 m NHN, 40 m hoch (Wikipedia)];</li>
 * <li><b>Treppe</b> am Kliff (Stufen, Holme, Handlauf, Pfosten bis zum Hang), <b>Bohlenweg</b> durch die Dünen zum
 * Quermarkenfeuer, <b>Kiesweg</b> von Süden mit Bank, Abfallkorb und Pfählen mit Draht, <b>Strandlokal</b> auf Stelzen
 * am Kampener Strand, <b>Rettungstürme</b>.</li>
 * </ul>
 * Die Lagen der Wege, Treppe, Strandlokal und Rettungstürme sind geschätzt [G]. Strandkörbe ({@link Strandkoerbe}) und die
 * Galloways ({@link Herde}) ändern sich und haben eigene Netze.
 */
public final class Bauwerke {
    /** Nordwerte (UTM) der Treppe, des Strandlokals und der Rettungstürme [G]. */
    public static final double TREPPE_N = 6_089_900, LOKAL_N = 6_090_600;
    public static final double[] RETTUNG_N = {6_090_560, 6_089_640, 6_087_800};
    /** Feuerhöhe über dem Grund am Turm (m): Mitte der Laterne. */
    public static final double FOKUS_Q = 8.65, FOKUS_K = 34.65;

    public final Bauten bauten;
    public final Netz netz;
    /** Brennpunkte der Feuer (x, y, z) und Grund der Türme. */
    public final double[] fokusQuermarke, fokusKampen, turmQ, turmK;
    /** Häuser je Dach: Reet, grau, rot. */
    public final int[] daecher = new int[3];
    public final List<String> teile = new ArrayList<>();
    /** Linie des Bohlenwegs (x, y, z) für Prüfungen. */
    public final List<double[]> bohlenweg = new ArrayList<>();
    public final long ms;
    private final DoubleBinaryOperator grund;

    public Bauwerke(Bauten bauten, DoubleBinaryOperator grund, Kante kante) {
        long t0 = System.nanoTime();
        this.bauten = bauten;
        this.grund = grund;
        double qx = Site.QUERMARKE.x(), qz = Site.QUERMARKE.z(), kx = Site.LEUCHTTURM.x(), kz = Site.LEUCHTTURM.z();
        double gq = grund.applyAsDouble(qx, qz), gk = grund.applyAsDouble(kx, kz);
        turmQ = new double[]{qx, gq, qz};
        turmK = new double[]{kx, gk, kz};
        fokusQuermarke = new double[]{qx, gq + FOKUS_Q, qz};
        fokusKampen = new double[]{kx, gk + FOKUS_K, kz};
        Netz n = bauten.netz(new double[]{kx, kz}, daecher);
        int lod2 = n.dreiecke();
        teile.add(String.format(Locale.GERMAN, "%,d Häuser (Reet %d, grau %d, rot %d)", bauten.haeuser.size(), daecher[0], daecher[1], daecher[2]));
        int a = n.dreiecke();
        quermarke(n, qx, qz, gq);
        kampen(n, kx, kz, gk);
        teile.add(String.format(Locale.GERMAN, "2 Türme (%,d Dreiecke)", n.dreiecke() - a));
        a = n.dreiecke();
        if (kante != null) treppe(n, kante, TREPPE_N);
        bohlenweg(n, qx, qz);
        kiesweg(n, qx, qz);
        strandlokal(n, LOKAL_N);
        for (double rn : RETTUNG_N) rettungsturm(n, rn);
        teile.add(String.format(Locale.GERMAN, "Treppe, Bohlenweg, Kiesweg, Strandlokal, %d Rettungstürme (%,d Dreiecke)", RETTUNG_N.length, n.dreiecke() - a));
        netz = n;
        ms = (System.nanoTime() - t0) / 1_000_000;
        if (lod2 == 0) teile.add(0, "!ohne LOD2 (daten/bauten)");
    }

    double h(double x, double z) { return grund.applyAsDouble(x, z); }

    private static double[] p(double x, double y, double z) { return new double[]{x, y, z}; }

    /** Tiefster Grund im Kreis r um (x, z). */
    double tiefster(double x, double z, double r) {
        double m = h(x, z);
        for (int i = 0; i < 16; i++) {
            double a = 2 * Math.PI * i / 16;
            m = Math.min(m, h(x + Math.sin(a) * r, z - Math.cos(a) * r));
        }
        return m;
    }

    /** Ein Fenster (oder eine Tür) als Viereck auf der Seite mit Richtung phi (Grad) eines Vielecks mit Abstand ap. */
    static void aufSeite(Netz n, double cx, double cz, double ap, double phi, double w, double y0, double y1, int m) {
        double a = Math.toRadians(phi), ox = Math.sin(a), oz = -Math.cos(a), tx = -oz, tz = ox;
        double x0 = cx + ox * (ap + 0.025), z0 = cz + oz * (ap + 0.025);
        double[] q0 = p(x0 - tx * w / 2, y0, z0 - tz * w / 2), q1 = p(x0 + tx * w / 2, y0, z0 + tz * w / 2);
        double[] q2 = p(x0 + tx * w / 2, y1, z0 + tz * w / 2), q3 = p(x0 - tx * w / 2, y1, z0 - tz * w / 2);
        n.dreieck(q0, q1, q2, new double[]{ox, 0, oz}, m);
        n.dreieck(q0, q2, q3, new double[]{ox, 0, oz}, m);
    }

    // ------------------------------------------------------------------ Quermarkenfeuer

    /** Das Quermarkenfeuer (1913) am Grund g (Maße über g). */
    void quermarke(Netz n, double x, double z, double g) {
        int k = 1;
        double tief = tiefster(x, z, 2.6) - 0.3, ap = 2.10 * Math.cos(Math.PI / 8);
        n.prisma(x, z, tief, g + 0.45, 2.32, 2.30, 8, 22.5, false, true, false, Netz.merkmal(Netz.GRANIT, false, k, g));
        n.prisma(x, z, g + 0.45, g + 6.80, 2.10, 2.10, 8, 22.5, false, false, false, Netz.merkmal(Netz.BACKSTEIN, false, k, g));
        // Fenster und Tür [G nach Leuchturm1–3]
        int fe = Netz.merkmal(Netz.FENSTER, false, k, g), tu = Netz.merkmal(Netz.TUER, false, k, g);
        aufSeite(n, x, z, ap, 180, 0.55, g + 1.55, g + 2.35, fe);
        aufSeite(n, x, z, ap, 180, 0.55, g + 4.45, g + 5.25, fe);
        aufSeite(n, x, z, ap, 0, 0.55, g + 4.45, g + 5.25, fe);
        aufSeite(n, x, z, ap, 90, 0.95, g + 0.45, g + 2.45, tu);
        // Galerie: schwarze Platte, Geländer
        n.scheibe(x, z, g + 7.05, 0, 2.55, 0.25, 8, 22.5, Netz.merkmal(Netz.LACK_SCHWARZ, false, k, g));
        n.gelaender(x, z, g + 7.05, 2.45, 1.0, 8, 22.5, 0.045, Netz.merkmal(Netz.EISEN, false, k, g));
        // Laterne: weißer Sockel, Glas, Gesims, Kupferdach, Kugel, Spitze
        int weiss = Netz.merkmal(Netz.LACK, false, k, g);
        n.prisma(x, z, g + 7.05, g + 8.15, 1.42, 1.42, 8, 22.5, false, false, false, weiss);
        n.prisma(x, z, g + 8.15, g + 9.15, 1.42, 1.42, 8, 22.5, false, false, false, Netz.merkmal(Netz.GLAS_Q, false, k, g));
        n.prisma(x, z, g + 9.15, g + 9.37, 1.62, 1.62, 8, 22.5, false, false, true, weiss);
        int ku = Netz.merkmal(Netz.KUPFER, false, k, g);
        n.prisma(x, z, g + 9.37, g + 9.95, 1.62, 0.95, 8, 22.5, false, false, false, ku);
        n.prisma(x, z, g + 9.95, g + 10.62, 0.95, 0.16, 8, 22.5, false, true, false, ku);
        n.kugel(x, g + 10.84, z, 0.24, ku);
        n.balken(p(x, g + 10.6, z), p(x, g + 12.0, z), 0.05, 0.05, Netz.merkmal(Netz.EISEN, false, k, g));
    }

    // ------------------------------------------------------------------ Kampener Leuchtturm

    /** Halbmesser des Schafts in der Höhe y über dem Grund (Fuß 3,65 m, oben 2,75 m). */
    public static double kampenRadius(double y) { return 3.65 - 0.90 * Math.max(0, Math.min(30, y)) / 30; }

    /** Der Kampener Leuchtturm („Langer Christian“, 1856) am Grund g. */
    void kampen(Netz n, double x, double z, double g) {
        int k = 2;
        double tief = tiefster(x, z, 4) - 0.3;
        int weiss = Netz.merkmal(Netz.LACK, false, k, g), schwarz = Netz.merkmal(Netz.LACK_SCHWARZ, false, k, g);
        n.prisma(x, z, tief, g + 0.8, 3.95, 3.90, 24, 0, true, true, false, Netz.merkmal(Netz.GRANIT, false, k, g));
        double[] b = {0.8, 12.6, 19.2, 30.0};
        for (int i = 0; i < 3; i++)
            n.prisma(x, z, g + b[i], g + b[i + 1], kampenRadius(b[i]), kampenRadius(b[i + 1]), 28, 0, true, false, false, i == 1 ? schwarz : weiss);
        // kleine Fenster übereinander nach West und Ost, Tür nach Süden [G]
        int fe = Netz.merkmal(Netz.FENSTER, false, k, g);
        for (double y : new double[]{5.5, 10.5, 16.0, 22.0, 26.5})
            for (double phi : new double[]{270, 90}) aufSeite(n, x, z, kampenRadius(y + 0.5) - 0.02, phi, 0.45, g + y, g + y + 0.95, fe);
        aufSeite(n, x, z, kampenRadius(1.5), 180, 1.1, g + 0.8, g + 3.0, Netz.merkmal(Netz.TUER, false, k, g));
        // Galerie 1 (weiß), Wachgeschoss, Galerie 2 (schwarz)
        n.scheibe(x, z, g + 30.0, 0, 3.55, 0.40, 28, 0, weiss);
        n.gelaender(x, z, g + 30.0, 3.45, 1.0, 20, 0, 0.05, weiss);
        n.prisma(x, z, g + 30.0, g + 32.7, 2.55, 2.55, 24, 0, true, false, false, weiss);
        for (double phi = 0; phi < 360; phi += 90) aufSeite(n, x, z, 2.53, phi + 45, 0.55, g + 30.9, g + 32.0, fe);
        n.scheibe(x, z, g + 32.7, 0, 2.95, 0.25, 24, 0, weiss);
        n.gelaender(x, z, g + 32.7, 2.85, 0.95, 16, 0, 0.04, schwarz);
        // Laterne mit Rautensprossen, Gesims, Kuppel, Kugel, Spitze
        n.prisma(x, z, g + 32.7, g + 33.1, 1.98, 1.98, 16, 0, false, false, false, schwarz);
        n.prisma(x, z, g + 33.1, g + 36.2, 1.98, 1.98, 16, 0, false, false, false, Netz.merkmal(Netz.GLAS_K, false, k, g));
        n.prisma(x, z, g + 36.2, g + 36.45, 2.12, 2.12, 16, 0, false, false, true, schwarz);
        n.prisma(x, z, g + 36.45, g + 37.9, 2.12, 0.34, 20, 0, true, true, false, schwarz);
        n.kugel(x, g + 38.2, z, 0.32, schwarz);
        n.balken(p(x, g + 38.4, z), p(x, g + 40.6, z), 0.06, 0.06, schwarz);
    }

    // ------------------------------------------------------------------ Treppe am Kliff

    /** Die Strandtreppe am Kliff bei Nordwert nord: vom Strand vor dem Fuß bis hinter die Kante, Stufen 18 cm. */
    void treppe(Netz n, Kante kante, double nord) {
        double z = Site.N0 - nord - 1.5;
        int r = kante.row(z);
        if (r < 0) return;
        double x0 = kante.fussX(r) - 5, x1 = kante.kanteX(r) + 5;
        if (x1 < x0) { double t = x0; x0 = x1; x1 = t; }
        // Linie der Treppe: laufendes Maximum des Grunds von See (Westen) nach Land, 35 cm darüber
        int m = (int) Math.ceil((x1 - x0) / 0.1);
        double[] lx = new double[m + 1], ly = new double[m + 1];
        double mx = -1e9;
        for (int i = 0; i <= m; i++) {
            lx[i] = x0 + (x1 - x0) * i / m;
            mx = Math.max(mx, Math.max(h(lx[i], z - 0.7), h(lx[i], z + 0.7)) + 0.35);
            ly[i] = mx;
        }
        int k = 3;
        int holz = Netz.merkmal(Netz.HOLZ, false, k, ly[0]);
        double rise = 0.18, y = ly[0];
        double xPrev = lx[0], yPrev = y;
        List<double[]> stufen = new ArrayList<>();
        stufen.add(new double[]{xPrev, yPrev});
        int i = 0;
        while (i < m) {
            while (i < m && ly[i] < y + rise) i++;
            double xs = lx[i];
            // waagerecht bis zur Stufe (Podest), dann die Stufe
            for (double xx = xPrev; xx < xs - 0.01; xx += 0.32) {
                double w = Math.min(0.30, xs - xx);
                n.quader(xx + w / 2, y - 0.04, z, 1.30, w, 0.04, 90, holz);
            }
            y += rise;
            if (y > ly[m] + 0.01) break;
            xPrev = xs;
            stufen.add(new double[]{xs, y});
        }
        // Holme, Handläufe und Pfosten
        for (int s = 0; s + 1 < stufen.size(); s++) {
            double[] a = stufen.get(s), b = stufen.get(s + 1);
            for (double side : new double[]{-0.68, 0.68}) {
                n.balken(p(a[0], a[1] - 0.12, z + side), p(b[0], b[1] - 0.12, z + side), 0.06, 0.20, holz);
                n.balken(p(a[0], a[1] + 0.90, z + side), p(b[0], b[1] + 0.90, z + side), 0.07, 0.05, holz);
            }
        }
        double last = -1e9;
        for (double[] s : stufen) {
            if (s[0] - last < 1.8) continue;
            last = s[0];
            for (double side : new double[]{-0.68, 0.68}) {
                double gy = h(s[0], z + side);
                n.balken(p(s[0], Math.min(gy, s[1]) - 0.3, z + side), p(s[0], s[1] + 0.95, z + side), 0.09, 0.09, holz);
            }
        }
        teile.add(String.format(Locale.GERMAN, "Treppe bei N %,.0f: %d Stufen, %.0f m", nord, stufen.size(), x1 - x0));
    }

    // ------------------------------------------------------------------ Bohlenweg und Kiesweg am Quermarkenfeuer

    /** Punkte des Bohlenwegs: von Südost durch die Dünen zum Turm [G nach Leuchturm4] (dx, dz zum Turm). */
    public static final double[][] BOHLENWEG = {{235, 255}, {190, 212}, {160, 190}, {118, 146}, {82, 100}, {58, 70}, {34, 40}, {16, 17}, {7, 6}};

    /** Glatte Linie durch die Punkte, alle d Meter (Catmull-Rom). */
    static List<double[]> linie(double[][] pts, double d) {
        List<double[]> out = new ArrayList<>();
        for (int i = 0; i + 1 < pts.length; i++) {
            double[] p0 = pts[Math.max(0, i - 1)], p1 = pts[i], p2 = pts[i + 1], p3 = pts[Math.min(pts.length - 1, i + 2)];
            double len = Math.hypot(p2[0] - p1[0], p2[1] - p1[1]);
            int m = Math.max(1, (int) Math.round(len / d));
            for (int j = 0; j < m; j++) {
                double t = j / (double) m, t2 = t * t, t3 = t2 * t;
                double[] q = new double[2];
                for (int c = 0; c < 2; c++)
                    q[c] = 0.5 * (2 * p1[c] + (-p0[c] + p2[c]) * t + (2 * p0[c] - 5 * p1[c] + 4 * p2[c] - p3[c]) * t2 + (-p0[c] + 3 * p1[c] - 3 * p2[c] + p3[c]) * t3);
                out.add(q);
            }
        }
        out.add(pts[pts.length - 1].clone());
        return out;
    }

    void bohlenweg(Netz n, double qx, double qz) {
        double[][] w = new double[BOHLENWEG.length][];
        for (int i = 0; i < w.length; i++) w[i] = new double[]{qx + BOHLENWEG[i][0], qz + BOHLENWEG[i][1]};
        List<double[]> l = linie(w, 0.16);
        int m = l.size();
        double[] g = new double[m], y = new double[m];
        for (int i = 0; i < m; i++) g[i] = h(l.get(i)[0], l.get(i)[1]);
        // Höhe: Grund über ±6 m gemittelt, nie weniger als 15 cm über dem Grund
        for (int i = 0; i < m; i++) {
            double s = 0;
            int c = 0;
            for (int j = Math.max(0, i - 38); j <= Math.min(m - 1, i + 38); j++) { s += g[j]; c++; }
            y[i] = Math.max(s / c + 0.30, g[i] + 0.15);
        }
        int holz = Netz.merkmal(Netz.HOLZ, false, 4, g[0]);
        for (int i = 0; i < m; i++) {
            double[] a = l.get(Math.max(0, i - 1)), b = l.get(Math.min(m - 1, i + 1));
            double dir = Math.toDegrees(Math.atan2(b[0] - a[0], -(b[1] - a[1])));
            n.quader(l.get(i)[0], y[i] - 0.04, l.get(i)[1], 1.20, 0.135, 0.04, dir, holz);
            bohlenweg.add(new double[]{l.get(i)[0], y[i], l.get(i)[1]});
        }
        // Längshölzer und Pfosten je 2,4 m
        for (int i = 0; i + 15 < m; i += 15) {
            double[] a = l.get(i), b = l.get(i + 15);
            double dx = b[0] - a[0], dz = b[1] - a[1], dl = Math.hypot(dx, dz), sx = -dz / dl, sz = dx / dl;
            for (double side : new double[]{-0.5, 0.5}) {
                n.balken(p(a[0] + sx * side, y[i] - 0.12, a[1] + sz * side), p(b[0] + sx * side, y[i + 15] - 0.12, b[1] + sz * side), 0.08, 0.14, holz);
                double gy = h(a[0] + sx * side, a[1] + sz * side);
                if (y[i] - 0.2 > gy) n.balken(p(a[0] + sx * side, gy - 0.2, a[1] + sz * side), p(a[0] + sx * side, y[i] - 0.18, a[1] + sz * side), 0.1, 0.1, holz);
            }
        }
    }

    /** Kiesweg von Süden zum Turm [G nach Leuchturm3] mit Bank, Abfallkorb und Pfählen mit Draht [nach Leuchturm1]. */
    public static final double[][] KIESWEG = {{-6, 150}, {-4, 105}, {-1, 62}, {0, 30}, {0, 9}};

    void kiesweg(Netz n, double qx, double qz) {
        double[][] w = new double[KIESWEG.length][];
        for (int i = 0; i < w.length; i++) w[i] = new double[]{qx + KIESWEG[i][0], qz + KIESWEG[i][1]};
        List<double[]> l = linie(w, 0.8);
        int kies = Netz.merkmal(Netz.KIES, false, 5, h(w[0][0], w[0][1]));
        for (int i = 0; i + 1 < l.size(); i++) {
            double[] a = l.get(i), b = l.get(i + 1);
            double dx = b[0] - a[0], dz = b[1] - a[1], dl = Math.hypot(dx, dz), sx = -dz / dl * 1.25, sz = dx / dl * 1.25;
            double[] a0 = p(a[0] - sx, h(a[0] - sx, a[1] - sz) + 0.06, a[1] - sz), a1 = p(a[0] + sx, h(a[0] + sx, a[1] + sz) + 0.06, a[1] + sz);
            double[] b0 = p(b[0] - sx, h(b[0] - sx, b[1] - sz) + 0.06, b[1] - sz), b1 = p(b[0] + sx, h(b[0] + sx, b[1] + sz) + 0.06, b[1] + sz);
            n.dreieck(a0, b0, b1, new double[]{0, 1, 0}, kies);
            n.dreieck(a0, b1, a1, new double[]{0, 1, 0}, kies);
        }
        // Bank und Abfallkorb links am Weg, 28 m vor dem Turm
        double bx = qx - 3.2, bz = qz + 30, by = h(bx, bz);
        int holz = Netz.merkmal(Netz.HOLZ, false, 6, by), eisen = Netz.merkmal(Netz.EISEN, false, 6, by);
        n.quader(bx, by + 0.42, bz, 1.8, 0.45, 0.06, 90, holz);
        n.balken(p(bx - 0.24, by + 0.55, bz - 0.9), p(bx - 0.24, by + 0.55, bz + 0.9), 0.05, 0.35, holz);
        for (double s : new double[]{-0.75, 0.75}) n.quader(bx, by - 0.1, bz + s, 0.06, 0.4, 0.52, 90, eisen);
        n.prisma(bx - 0.4, bz - 1.6, by - 0.05, by + 0.85, 0.28, 0.28, 10, 0, true, false, false, Netz.merkmal(Netz.HOLZ, false, 7, by));
        // Pfähle mit Draht beiderseits des Wegs bis 15 m vor dem Turm
        int pfahl = Netz.merkmal(Netz.HOLZ, false, 8, by), draht = Netz.merkmal(Netz.EISEN, false, 8, by);
        for (double side : new double[]{-2.4, 2.4}) {
            double[] prev = null;
            for (int i = 0; i < l.size(); i += 5) {
                double[] a = l.get(i), b = l.get(Math.min(l.size() - 1, i + 1));
                if (Math.hypot(a[0] - qx, a[1] - qz) < 15) break;
                double dx = b[0] - a[0], dz = b[1] - a[1], dl = Math.max(1e-6, Math.hypot(dx, dz));
                double px = a[0] - dz / dl * side, pz = a[1] + dx / dl * side, gy = h(px, pz);
                n.balken(p(px, gy - 0.3, pz), p(px, gy + 0.95, pz), 0.12, 0.12, pfahl);
                double[] top = p(px, gy + 0.78, pz);
                if (prev != null) n.balken(prev, top, 0.012, 0.012, draht);
                prev = top;
            }
        }
    }

    // ------------------------------------------------------------------ Strand: Lokal, Rettungstürme

    /** Strand am Nordwert: x der Wasserlinie (Szene) und x des Dünenfußes (erste Stelle landwärts über 5 m NHN). */
    double[] strand(double nord) {
        double z = Site.N0 - nord, wl = Site.wasserlinieE(nord) - Site.E0, fuss = wl + 60;
        for (double x = wl + 10; x < wl + 250; x += 1) if (h(x, z) > 5.0) { fuss = x; break; }
        return new double[]{wl, fuss, z};
    }

    void strandlokal(Netz n, double nord) {
        double[] s = strand(nord);
        double x = s[1] - 9, z = s[2];
        double boden = Math.max(Math.max(h(x - 7, z), h(x + 7, z)), Math.max(h(x, z - 5), h(x, z + 5))) + 0.9;
        int k = 9;
        int holz = Netz.merkmal(Netz.HOLZ, false, k, boden), wand = Netz.merkmal(Netz.HOLZ, true, k, boden);
        // Stelzen, Boden, Terrasse zur See (Westen), Hütte, Dach
        for (double dx = -6; dx <= 6.01; dx += 3)
            for (double dz = -7; dz <= 4.01; dz += 2.75) {
                double gy = h(x + dx, z + dz);
                n.balken(p(x + dx, gy - 0.5, z + dz), p(x + dx, boden, z + dz), 0.25, 0.25, holz);
            }
        n.quader(x, boden - 0.2, z - 1.5, 13.5, 11.5, 0.2, 0, holz);
        n.quader(x + 1.5, boden, z + 1.0, 9.0, 6.5, 3.0, 0, wand);
        n.quader(x + 1.5, boden + 3.0, z + 1.0, 9.8, 7.3, 0.15, 0, holz);
        double[] r0 = p(x - 3.4, boden + 3.15, z - 2.65), r1 = p(x + 6.4, boden + 3.15, z - 2.65), r2 = p(x + 6.4, boden + 3.15, z + 4.65), r3 = p(x - 3.4, boden + 3.15, z + 4.65);
        double[] f0 = p(x - 3.4, boden + 4.4, z + 1.0), f1 = p(x + 6.4, boden + 4.4, z + 1.0);
        int dach = Netz.merkmal(Netz.DACH_GRAU, false, k, boden);
        n.viereck(r0, r1, f1, f0, dach);
        n.viereck(r2, r3, f0, f1, dach);
        n.dreieck(r0, f0, r3, Netz.merkmal(Netz.HOLZ, false, k, boden));
        n.dreieck(r1, r2, f1, Netz.merkmal(Netz.HOLZ, false, k, boden));
        // Geländer der Terrasse
        double ty = boden;
        for (double[] seg : new double[][]{{-6.7, -7.2, -6.7, 4.2}, {-6.7, -7.2, 6.7, -7.2}}) {
            double[] a = p(x + seg[0], ty + 1.0, z + seg[1]), b = p(x + seg[2], ty + 1.0, z + seg[3]);
            n.balken(a, b, 0.08, 0.06, holz);
            double len = Math.hypot(seg[2] - seg[0], seg[3] - seg[1]);
            for (double t = 0; t <= len + 0.01; t += 1.5) {
                double px = x + seg[0] + (seg[2] - seg[0]) * t / len, pz = z + seg[1] + (seg[3] - seg[1]) * t / len;
                n.balken(p(px, ty, pz), p(px, ty + 1.0, pz), 0.07, 0.07, holz);
            }
        }
        teile.add(String.format(Locale.GERMAN, "Strandlokal bei N %,.0f auf %.1f m NHN", nord, boden));
    }

    void rettungsturm(Netz n, double nord) {
        double[] s = strand(nord);
        double x = s[0] + Math.min(24, (s[1] - s[0]) * 0.45), z = s[2], g = h(x, z);
        double boden = g + 2.6;
        int k = 10;
        int holz = Netz.merkmal(Netz.HOLZ, false, k, boden), weiss = Netz.merkmal(Netz.HOLZ_WEISS, false, k, boden);
        for (double dx : new double[]{-1.0, 1.0})
            for (double dz : new double[]{-1.0, 1.0})
                n.balken(p(x + dx * 1.15, g - 0.6, z + dz * 1.15), p(x + dx, boden, z + dz), 0.14, 0.14, holz);
        n.balken(p(x - 1.1, g + 1.2, z - 1.1), p(x + 1.1, g + 1.2, z + 1.1), 0.08, 0.08, holz);
        n.balken(p(x - 1.1, g + 1.2, z + 1.1), p(x + 1.1, g + 1.2, z - 1.1), 0.08, 0.08, holz);
        n.quader(x, boden - 0.12, z, 2.6, 2.6, 0.12, 0, holz);
        n.quader(x, boden, z, 2.0, 1.8, 1.0, 0, weiss);
        n.quader(x, boden + 1.0, z, 2.0, 1.8, 0.9, 0, Netz.merkmal(Netz.FENSTER, false, k, boden));
        n.quader(x, boden + 1.9, z, 2.3, 2.1, 0.15, 0, Netz.merkmal(Netz.ROT, false, k, boden));
        // Leiter zur See
        n.balken(p(x - 1.9, g, z - 0.3), p(x - 1.3, boden, z - 0.3), 0.06, 0.06, holz);
        n.balken(p(x - 1.9, g, z + 0.3), p(x - 1.3, boden, z + 0.3), 0.06, 0.06, holz);
        // Mast mit der Flagge der DLRG (gelb, rot)
        n.balken(p(x + 0.9, boden + 2.0, z + 0.8), p(x + 0.9, boden + 5.0, z + 0.8), 0.05, 0.05, holz);
        n.quader(x + 0.9, boden + 4.4, z + 1.25, 0.02, 0.9, 0.3, 0, Netz.merkmal(Netz.GELB, false, k, boden));
        n.quader(x + 0.9, boden + 4.1, z + 1.25, 0.02, 0.9, 0.3, 0, Netz.merkmal(Netz.ROT, false, k, boden));
    }

    public String describe() {
        return String.join(" · ", teile) + String.format(Locale.GERMAN, " · %,d Dreiecke · %d ms", netz.dreiecke(), ms);
    }
}
