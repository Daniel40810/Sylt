package com.dan.sylt.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Die Kacheln des DGM1 Schleswig-Holstein als XYZ ({@code dgm1_32_E_N_1_sh_JAHR.xyz}: 1 km², 1 m, Punkte in den
 * Zellmitten, Höhen über NHN). Befund Phase 2 (alle 369 Kacheln gelesen): die von 2024 tragen auf Wasser den festen Wert
 * {@link #WASSER} (+1,30 m), die von 2005 liegen nur auf Wasser und Watt mit Werten, die zur See nicht passen (−0,9 m
 * dort, wo die BAW −10 m misst); darum gelten nur Kacheln ab {@link #AB_JAHR}, und ihre Wasserflächen werden als Lücke
 * gelesen. Wasser ist ein Punkt mit dem Wert {@link #WASSER}, dessen 5 × 5 Nachbarn alle denselben Wert tragen, dazu die Punkte
 * mit diesem Wert direkt daneben (echtes Land ist auf 5 m nie zentimetergenau eben).
 */
public final class Dgm1 {
    /** Höhe, mit der das DGM1 2024 Wasserflächen füllt (m NHN). */
    public static final float WASSER = 1.30f;
    /** Ältestes Jahr, das gilt. */
    public static final int AB_JAHR = 2020;
    static final Pattern NAME = Pattern.compile("dgm1_32_(\\d{3})_(\\d{4})_1_sh_(\\d{4})\\.xyz", Pattern.CASE_INSENSITIVE);

    /** Eine Kachel: Ost- und Nordwert der Südwestecke in km, Jahr, Datei. */
    public record Kachel(int ekm, int nkm, int jahr, File datei) { }

    /** Die gültigen Kacheln (je Ort die jüngste ab {@link #AB_JAHR}), Schlüssel ekm * 10000 + nkm. */
    public final Map<Integer, Kachel> kacheln = new TreeMap<>();
    public int alle, alt;

    public Dgm1(File dir) throws IOException {
        File[] fs = dir.listFiles();
        if (fs == null) throw new IOException("DGM1: Ordner " + dir + " fehlt");
        for (File f : fs) {
            Matcher m = NAME.matcher(f.getName());
            if (!m.matches()) continue;
            alle++;
            int e = Integer.parseInt(m.group(1)), n = Integer.parseInt(m.group(2)), j = Integer.parseInt(m.group(3));
            if (j < AB_JAHR) { alt++; continue; }
            Kachel k = new Kachel(e, n, j, f), alt0 = kacheln.get(e * 10000 + n);
            if (alt0 == null || alt0.jahr < j) kacheln.put(e * 10000 + n, k);
        }
    }

    public Kachel kachel(int ekm, int nkm) { return kacheln.get(ekm * 10000 + nkm); }

    /** Eine gelesene Kachel: 1000 × 1000 Höhen, Zeile 0 im Norden; NaN, wo kein Punkt ist oder Wasser. */
    public static final class Feld {
        public final int ekm, nkm;
        public final float[] h = new float[1000 * 1000];
        public int punkte, wasser;

        Feld(int e, int n) { ekm = e; nkm = n; java.util.Arrays.fill(h, Float.NaN); }

        /** Höhe am Punkt (Spalte x nach Osten, Zeile y nach Süden), NaN wenn keiner. */
        public float at(int x, int y) { return h[y * 1000 + x]; }
    }

    /** Liest eine Kachel (XYZ-Text, rund 28 MB) und nimmt die Wasserflächen heraus. */
    public static Feld lesen(Kachel k) throws IOException {
        byte[] b = Files.readAllBytes(k.datei.toPath());
        Feld f = new Feld(k.ekm, k.nkm);
        double e0 = k.ekm * 1000.0, n1 = (k.nkm + 1) * 1000.0;
        int p = 0, len = b.length;
        double[] v = new double[3];
        while (p < len) {
            int c = 0;
            while (c < 3 && p < len) {
                while (p < len && (b[p] == ' ' || b[p] == '\t' || b[p] == '\r' || b[p] == '\n' || b[p] == ';' || b[p] == ',')) {
                    if (b[p] == '\n' && c > 0) break;
                    p++;
                }
                if (p >= len || b[p] == '\n') break;
                // Zahl lesen
                boolean neg = false;
                if (b[p] == '-') { neg = true; p++; } else if (b[p] == '+') p++;
                long ip = 0, fp = 0, fd = 1;
                while (p < len && b[p] >= '0' && b[p] <= '9') ip = ip * 10 + (b[p++] - '0');
                if (p < len && b[p] == '.') {
                    p++;
                    while (p < len && b[p] >= '0' && b[p] <= '9') { fp = fp * 10 + (b[p++] - '0'); fd *= 10; }
                }
                double x = ip + fp / (double) fd;
                v[c++] = neg ? -x : x;
                while (p < len && b[p] != ' ' && b[p] != '\t' && b[p] != '\n' && b[p] != '\r' && b[p] != ';' && b[p] != ',') p++;
            }
            while (p < len && b[p] != '\n') p++;
            p++;
            if (c < 3) continue;
            int x = (int) Math.floor(v[0] - e0), y = (int) Math.floor(n1 - v[1]);
            if (x < 0 || y < 0 || x >= 1000 || y >= 1000) continue;
            f.h[y * 1000 + x] = (float) v[2];
            f.punkte++;
        }
        wasserHeraus(f);
        return f;
    }

    /** Wasser: Wert {@link #WASSER} mit 5 × 5 gleichen Nachbarn, dazu ein Punkt Rand; wird NaN. */
    static void wasserHeraus(Feld f) {
        int n = 1000;
        boolean[] w = new boolean[n * n];
        float[] h = f.h;
        for (int y = 0; y < n; y++)
            for (int x = 0; x < n; x++) {
                if (!gleich(h[y * n + x])) continue;
                boolean eben = true;
                for (int dy = -2; dy <= 2 && eben; dy++)
                    for (int dx = -2; dx <= 2 && eben; dx++) {
                        int xx = x + dx, yy = y + dy;
                        if (xx < 0 || yy < 0 || xx >= n || yy >= n) continue;
                        float q = h[yy * n + xx];
                        if (!Float.isNaN(q) && !gleich(q)) eben = false;
                    }
                w[y * n + x] = eben;
            }
        for (int y = 0; y < n; y++)
            for (int x = 0; x < n; x++) {
                boolean rand = w[y * n + x];
                for (int dy = -1; dy <= 1 && !rand; dy++)
                    for (int dx = -1; dx <= 1 && !rand; dx++) {
                        int xx = x + dx, yy = y + dy;
                        if (xx >= 0 && yy >= 0 && xx < n && yy < n && w[yy * n + xx] && gleich(h[y * n + x])) rand = true;
                    }
                if (w[y * n + x] || rand) {
                    if (!Float.isNaN(h[y * n + x])) f.wasser++;
                    h[y * n + x] = Float.NaN;
                }
            }
    }

    static boolean gleich(float v) { return Math.abs(v - WASSER) < 0.005f; }
}
