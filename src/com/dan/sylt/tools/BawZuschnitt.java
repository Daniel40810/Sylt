package com.dan.sylt.tools;

import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Terrain;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Der Zuschnitt der BAW-Küstenhöhen für Sylt (Werkbuch Phase 2: „BAW-Zuschnitt nach templates/rasters_BAW“). Quelle ist
 * {@code topography_max_z_2015-2021_ger.tif} der BAW/EasyGSH-DB (10 m, ganz Deutsche Bucht, 1,8 GB), im Projekt Rungholt.
 * Danach braucht Sylt Rungholt nicht mehr:
 * <ul>
 * <li>{@code sylt_topo_10m.tif}: die Pixel der BAW unverändert über dem Hauptraster (20,5 km) mit 20 m Rand;</li>
 * <li>{@code sylt_topo_100m.tif}: Mittel der BAW über die Zellen des Fernrings (102,4 km, 100 m); wo die BAW nichts
 * misst (Festland, Dänemark nördlich von N 6 113 105), NaN.</li>
 * </ul>
 */
public final class BawZuschnitt {
    private BawZuschnitt() { }

    public static void main(String[] a) throws IOException { schneiden(Rohdaten.laden(), System.out::println); }

    public static void schneiden(Rohdaten r, java.util.function.Consumer<String> log) throws IOException {
        long t0 = System.nanoTime();
        if (!r.baw.isFile()) throw new IOException("BAW-Quelle fehlt: " + r.baw + " (Pfad baw in " + Rohdaten.DATEI + ")");
        try (GeoTiff t = GeoTiff.open(r.baw)) {
            log.accept("BAW: " + t);
            // 10 m: Fenster über dem Hauptraster, an den Pixeln der BAW ausgerichtet
            double eW = Terrain.X0 + Site.E0 - 20, eO = Terrain.X0 + Terrain.N * Terrain.CELL + Site.E0 + 20;
            double nN = Site.N0 - Terrain.Z0 + 20, nS = Site.N0 - (Terrain.Z0 + Terrain.N * Terrain.CELL) - 20;
            int c0 = (int) Math.floor(t.col(eW)), c1 = (int) Math.ceil(t.col(eO)), r0 = (int) Math.floor(t.row(nN)), r1 = (int) Math.ceil(t.row(nS));
            int w = c1 - c0, h = r1 - r0;
            float[] v = t.floats(c0, r0, w, h);
            GeoTiff.write(Rohdaten.BAW_10, v, w, h, t.east0 + c0 * t.sizeX, t.north0 - r0 * t.sizeY, t.sizeX, Site.EPSG_UTM);
            log.accept(String.format(Locale.GERMAN, "  %s: %d × %d Pixel zu 10 m, Ost %.0f–%.0f, Nord %.0f–%.0f, %s",
                    Rohdaten.BAW_10.getPath(), w, h, t.east0 + c0 * t.sizeX, t.east0 + c1 * t.sizeX, t.north0 - r1 * t.sizeY, t.north0 - r0 * t.sizeY, spanne(v)));
            // 100 m: Mittel über die Zellen des Fernrings
            int fn = Terrain.FN;
            float[] f = new float[fn * fn];
            double fe0 = Terrain.FX0 + Site.E0, fn0 = Site.N0 - Terrain.FZ0;
            int fc0 = (int) Math.floor(t.col(fe0)), fc1 = (int) Math.ceil(t.col(fe0 + fn * Terrain.FCELL));
            int fw = fc1 - fc0;
            for (int j = 0; j < fn; j++) {
                double nTop = fn0 - j * Terrain.FCELL;
                int rr0 = (int) Math.floor(t.row(nTop) + 1e-6), rr1 = (int) Math.floor(t.row(nTop - Terrain.FCELL) + 1e-6);
                float[] rows = t.floats(fc0, rr0, fw, rr1 - rr0);
                for (int i = 0; i < fn; i++) {
                    double eL = fe0 + i * Terrain.FCELL;
                    int cc0 = (int) Math.floor(t.col(eL) + 1e-6) - fc0, cc1 = (int) Math.floor(t.col(eL + Terrain.FCELL) + 1e-6) - fc0;
                    double s = 0;
                    int c = 0;
                    for (int y = 0; y < rr1 - rr0; y++)
                        for (int x = Math.max(0, cc0); x < Math.min(fw, cc1); x++) {
                            float q = rows[y * fw + x];
                            if (!Float.isNaN(q)) { s += q; c++; }
                        }
                    f[j * fn + i] = c * 2 >= (rr1 - rr0) * (cc1 - cc0) && c > 0 ? (float) (s / c) : Float.NaN;
                }
            }
            GeoTiff.write(Rohdaten.BAW_100, f, fn, fn, fe0, fn0, Terrain.FCELL, Site.EPSG_UTM);
            log.accept(String.format(Locale.GERMAN, "  %s: %d × %d Zellen zu 100 m, %s · %.1f s", Rohdaten.BAW_100.getPath(), fn, fn, spanne(f), (System.nanoTime() - t0) / 1e9));
        }
    }

    static String spanne(float[] v) {
        double mn = 1e9, mx = -1e9;
        long nan = 0;
        for (float q : v) if (Float.isNaN(q)) nan++; else { mn = Math.min(mn, q); mx = Math.max(mx, q); }
        return String.format(Locale.GERMAN, "%.2f … %.2f m, %.0f %% ohne Wert", mn, mx, 100.0 * nan / v.length);
    }
}
