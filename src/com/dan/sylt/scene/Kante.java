package com.dan.sylt.scene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Die Kante des Roten Kliffs je Meter Nord aus der Aufbereitung ({@code daten/kliff/kante.csv}, Phase 2, KanteImport):
 * Fuß und Oberkante (Ost, Höhe) aus dem DGM1, die Kante im bDOM, die Höhe der Wand. Seit Phase 4a die Grundlage für
 * das Kliff in der Szene (Band, Schichten je Ort, Grasnarbe, Findlinge).
 */
public final class Kante {
    /** Je Zeile: Nord, Ost des Fußes, Höhe des Fußes, Ost der Kante, Höhe der Kante, Höhe der Wand (m). */
    public final double[] nord, fussE, hFuss, kanteE, hKante, wand;
    public final int n;

    private Kante(List<double[]> rows) {
        n = rows.size();
        nord = new double[n]; fussE = new double[n]; hFuss = new double[n]; kanteE = new double[n]; hKante = new double[n]; wand = new double[n];
        for (int i = 0; i < n; i++) {
            double[] r = rows.get(i);
            nord[i] = r[0]; fussE[i] = r[1]; hFuss[i] = r[2]; kanteE[i] = r[3]; hKante[i] = r[4]; wand[i] = r[5];
        }
    }

    /** Liest {@code daten/kliff/kante.csv}; null, wenn sie fehlt oder leer ist. */
    public static Kante load(File daten) {
        File f = new File(new File(daten, "kliff"), "kante.csv");
        if (!f.isFile()) return null;
        try {
            List<double[]> rows = new ArrayList<>();
            for (String l : Files.readAllLines(f.toPath())) {
                if (l.startsWith("#") || l.startsWith("nord")) continue;
                String[] p = l.split(";", -1);
                if (p.length < 8 || p[1].isEmpty() || p[3].isEmpty()) continue;
                double hk = Double.parseDouble(p[4]), hf = Double.parseDouble(p[2]);
                rows.add(new double[]{Double.parseDouble(p[0]), Double.parseDouble(p[1]), hf, Double.parseDouble(p[3]), hk,
                        p[7].isBlank() ? hk - hf : Double.parseDouble(p[7].trim())});
            }
            rows.sort((a, b) -> Double.compare(b[0], a[0]));   // von Nord nach Süd
            return rows.isEmpty() ? null : new Kante(rows);
        } catch (IOException | RuntimeException e) {
            System.err.println("Kante: " + e.getMessage());
            return null;
        }
    }

    /** Index der nächsten Zeile zu z (Szene), −1, wenn keine näher als 3 m liegt (die Kante hat Lücken). */
    public int row(double z) {
        double nn = Site.N0 - z;
        int lo = 0, hi = n - 1;   // nord fällt
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (nord[mid] > nn) lo = mid + 1; else hi = mid;
        }
        int best = lo;
        if (lo > 0 && Math.abs(nord[lo - 1] - nn) < Math.abs(nord[lo] - nn)) best = lo - 1;
        return Math.abs(nord[best] - nn) <= 3 ? best : -1;
    }

    /** Seit Phase 15: die nächste Zeile bis {@code maxM} Meter neben z (über Lücken der Kante hinweg), sonst -1. */
    public int rowNear(double z, double maxM) {
        double nn = Site.N0 - z;
        int lo = 0, hi = n - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (nord[mid] > nn) lo = mid + 1; else hi = mid;
        }
        int best = lo;
        if (lo > 0 && Math.abs(nord[lo - 1] - nn) < Math.abs(nord[lo] - nn)) best = lo - 1;
        return Math.abs(nord[best] - nn) <= maxM ? best : -1;
    }

    /** Szene: x des Fußes, x der Kante (Zeile i). */
    public double fussX(int i) { return fussE[i] - Site.E0; }
    public double kanteX(int i) { return kanteE[i] - Site.E0; }
    public double z(int i) { return Site.N0 - nord[i]; }

    /** „Kante 3074 m (N 6 088 649 bis 6 091 722) · Wand bis 35,4 m“ */
    public String describe() {
        double m = 0;
        for (double w : wand) m = Math.max(m, w);
        return String.format(Locale.GERMAN, "Kante %d m (N %,.0f bis %,.0f) · Wand bis %.1f m", n, nord[n - 1], nord[0], m);
    }
}
