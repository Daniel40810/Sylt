package com.dan.sylt.tools;

import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Terrain;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Die Kante aus dem bDOM (Phase 2, Schritt {@code kante}): das bildbasierte Oberflächenmodell 2024 zu 20 cm über dem Kern.
 * Braucht {@code daten/gelaende} (Schritt {@code gelaende}). Schreibt:
 * <ul>
 * <li>{@code daten/kern/bewuchs.u8}: je Kernzelle (1 m) die Höhe über dem Gelände, die das bDOM zeigt (größter der 25
 * Werte minus DGM1, Stufe {@link #BEWUCHS_STUFE} m, bis 51 m): Strandhafer, Gebüsch, Häuser, Türme;</li>
 * <li>{@code daten/kliff/kante.csv}: je Meter Nord der Fuß und die Oberkante des Kliffs aus dem DGM1, die Oberkante aus
 * dem bDOM auf 20 cm genau, die Höhe der Wand;</li>
 * <li>{@code daten/kliff/band20.r16}: das bDOM unverändert in einem 100 m breiten Band längs des Kliffs (Zeilen zu 20 cm
 * von Nord nach Süd, je Zeile ab dem Ostwert in {@code band20_e0.f32}); Höhen wie {@link Terrain#encode}.</li>
 * </ul>
 * Dazu das Prüfbild {@code docs/status/Kliff_Phase2.png} und die Lage der Türme (höchster Bewuchs nahe der Schätzung).
 */
public final class KanteImport {
    public static final double BEWUCHS_STUFE = 0.2;
    /** Kliff: Wand mindestens so hoch (m), steil ab dieser Neigung (m/m), Oberkante, wo die Neigung unter diese fällt [G]. */
    public static final double WAND_MIN = 5, STEIL = 0.55, FLACH = 0.15;
    /** Breite des 20-cm-Bands (m) und wie weit es seewärts vor dem Fuß beginnt (m). */
    public static final double BAND_B = 100, BAND_VOR = 30;
    public static final File BILD = new File("docs/status/Kliff_Phase2.png");
    static final double PX = 0.2;

    private KanteImport() { }

    public static void main(String[] a) throws Exception { importieren(Rohdaten.laden(), new File("daten"), System.out::println); }

    /** Eine Zeile der Kante (UTM, m NHN); NaN, wo kein Kliff ist. */
    public record Zeile(double n, double eFuss, double hFuss, double eKante, double hKante, double eKante20, double hKante20) {
        public boolean kliff() { return !Double.isNaN(eKante); }
        public double wand() { return hKante - hFuss; }
    }

    public static List<String> importieren(Rohdaten r, File daten, Consumer<String> log) throws IOException {
        long t0 = System.nanoTime();
        List<String> bericht = new ArrayList<>();
        Terrain t = Terrain.load(new File(daten, "gelaende"));
        if (!t.measured() || !t.hasKern()) throw new IOException("daten/gelaende fehlt oder ist die Bühne: erst Schritt gelaende");
        byte[] quelle = Files.readAllBytes(new File(new File(daten, "gelaende"), GelaendeImport.QUELLE).toPath());
        int KN = Terrain.KN;
        int kE = (int) Math.round(Terrain.KX0 + Site.E0), kN = (int) Math.round(Site.N0 - Terrain.KZ0);
        float[] kern = new float[KN * KN];
        for (int k = 0; k < kern.length; k++) kern[k] = (float) Terrain.decode(t.kern[k]);

        // 1. Kante aus dem DGM1, je Kernzeile
        Zeile[] z = new Zeile[KN];
        int nKliff = 0;
        for (int j = 0; j < KN; j++) {
            double n = kN - j - 0.5;
            z[j] = kanteDgm(kern, KN, j, kE, n, GelaendeImport.wasserlinie(t, n));
            if (z[j].kliff()) nKliff++;
        }
        // einzelne Zeilen ohne Nachbarn mit Kliff sind Dünenhänge, keine Wand: mindestens 20 m am Stück
        int lauf = 0;
        for (int j = 0; j <= KN; j++) {
            if (j < KN && z[j].kliff()) { lauf++; continue; }
            if (lauf > 0 && lauf < 20) for (int q = j - lauf; q < j; q++) z[q] = leer(z[q].n);
            lauf = 0;
        }
        int jN = KN, jS = -1;
        nKliff = 0;
        for (int j = 0; j < KN; j++) if (z[j].kliff()) { jN = Math.min(jN, j); jS = Math.max(jS, j); nKliff++; }
        if (jS < 0) throw new IOException("kein Kliff im Kern gefunden");
        log.accept(Raster.f("Kliff aus dem DGM1: %d m mit Wand ≥ %.0f m zwischen N %.0f und N %.0f", nKliff, WAND_MIN, z[jS].n, z[jN].n));

        // Band: Ostwert des Fußes, geglättet und über Lücken gezogen
        double[] fuss = new double[KN];
        java.util.Arrays.fill(fuss, Double.NaN);
        for (int j = jN; j <= jS; j++) if (z[j].kliff()) fuss[j] = z[j].eFuss;
        lueckenZiehen(fuss, jN, jS);
        double[] glatt = new double[KN];
        for (int j = jN; j <= jS; j++) {
            double s = 0;
            int c = 0;
            for (int q = Math.max(jN, j - 25); q <= Math.min(jS, j + 25); q++) { s += fuss[q]; c++; }
            glatt[j] = s / c;
        }
        double bandN = z[jN].n + 0.5 + 10, bandS = z[jS].n - 0.5 - 10;   // 10 m über die Enden hinaus
        int bRows = (int) Math.round((bandN - bandS) / PX), bCols = (int) Math.round(BAND_B / PX);
        float[] band = new float[bRows * bCols];
        java.util.Arrays.fill(band, Float.NaN);
        float[] bandE0 = new float[bRows];
        for (int r2 = 0; r2 < bRows; r2++) {
            double n = bandN - (r2 + 0.5) * PX;
            int j = Math.max(jN, Math.min(jS, (int) Math.floor(kN - n)));
            bandE0[r2] = (float) (Math.floor((glatt[j] - BAND_VOR) / PX) * PX);
        }

        // 2. bDOM kachelweise: Bewuchs, Kante zu 20 cm, Band
        byte[] bewuchs = new byte[KN * KN];
        double[] e20 = new double[KN], h20 = new double[KN];
        java.util.Arrays.fill(e20, Double.NaN);
        java.util.Arrays.fill(h20, Double.NaN);
        int kacheln = 0;
        List<String> fehlend = new ArrayList<>();
        for (int ek = (int) Math.floor(kE / 1000.0); ek * 1000 < kE + KN; ek++)
            for (int nk = (int) Math.floor((kN - KN) / 1000.0); nk * 1000 < kN; nk++) {
                File f = new File(r.bdom(), String.format("bdom20nc_32_%d_%d_1_sh_2024.tif", ek, nk));
                if (!f.isFile()) { fehlend.add(ek + "/" + nk); continue; }
                kacheln++;
                float[] v;
                int w, h;
                double e0, n0;
                try (GeoTiff g = GeoTiff.open(f)) {
                    w = g.width;
                    h = g.height;
                    e0 = g.east0;
                    n0 = g.north0;
                    if (Math.abs(g.sizeX - PX) > 1e-6) throw new IOException(f.getName() + ": Pixel " + g.sizeX + " m statt 0,2 m");
                    v = g.floats(0, 0, w, h);
                }
                int per = (int) Math.round(1 / PX);
                // Bewuchs je Meter
                for (int y = 0; y + per <= h; y += per) {
                    int kj = (int) Math.round(kN - (n0 - y * PX));
                    if (kj < 0 || kj >= KN) continue;
                    for (int x = 0; x + per <= w; x += per) {
                        int ki = (int) Math.round(e0 + x * PX - kE);
                        if (ki < 0 || ki >= KN) continue;
                        int k = kj * KN + ki;
                        if (quelle[k] != 1) continue;
                        float mx = -1e9f;
                        for (int b = 0; b < per; b++) for (int a = 0; a < per; a++) { float q = v[(y + b) * w + x + a]; if (q > mx) mx = q; }
                        if (mx < -1e8f) continue;
                        double d = (mx - kern[k]) / BEWUCHS_STUFE;
                        bewuchs[k] = (byte) Math.max(0, Math.min(255, Math.round(d)));
                    }
                }
                // Oberkante auf 20 cm: in der Bildzeile mitten in der Kernzeile, ±4 m um die Kante aus dem DGM1
                for (int j = jN; j <= jS; j++) {
                    if (!z[j].kliff()) continue;
                    double n = z[j].n;
                    int y = (int) Math.floor((n0 - n) / PX);
                    if (y < 2 || y >= h - 2) continue;
                    double eK = z[j].eKante;
                    int xa = (int) Math.floor((eK - 4 - e0) / PX), xb = (int) Math.ceil((eK + 4 - e0) / PX);
                    if (xa < 2 || xb >= w - 2) continue;
                    double[] res = kante20(v, w, y, xa, xb);
                    if (res != null) { e20[j] = e0 + (res[0] + 0.5) * PX; h20[j] = res[1]; }
                }
                // Band
                for (int r2 = 0; r2 < bRows; r2++) {
                    double n = bandN - (r2 + 0.5) * PX;
                    int y = (int) Math.floor((n0 - n) / PX);
                    if (y < 0 || y >= h) continue;
                    for (int c = 0; c < bCols; c++) {
                        double e = bandE0[r2] + (c + 0.5) * PX;
                        int x = (int) Math.floor((e - e0) / PX);
                        if (x < 0 || x >= w) continue;
                        band[r2 * bCols + c] = v[y * w + x];
                    }
                }
            }
        log.accept(Raster.f("bDOM: %d Kacheln im Kern gelesen%s", kacheln, fehlend.isEmpty() ? "" : ", fehlen (See): " + String.join(", ", fehlend)));
        for (int j = 0; j < KN; j++) if (z[j].kliff()) z[j] = new Zeile(z[j].n, z[j].eFuss, z[j].hFuss, z[j].eKante, z[j].hKante, e20[j], h20[j]);

        // 3. schreiben
        File kd = new File(daten, "kern"), kl = new File(daten, "kliff");
        kd.mkdirs();
        kl.mkdirs();
        Files.write(new File(kd, "bewuchs.u8").toPath(), bewuchs);
        StringBuilder csv = new StringBuilder("# Kliff am Roten Kliff je Meter Nord (UTM 32 N, m NHN), aus DGM1 2024 (Fuß, Kante) und bDOM 2024 (Kante20); KanteImport\n"
                + "nord;ost_fuss;h_fuss;ost_kante;h_kante;ost_kante20;h_kante20;wand\n");
        for (int j = jN; j <= jS; j++) {
            Zeile q = z[j];
            if (!q.kliff()) continue;
            csv.append(String.format(Locale.ROOT, "%.1f;%.2f;%.2f;%.2f;%.2f;%s;%s;%.2f%n", q.n, q.eFuss, q.hFuss, q.eKante, q.hKante,
                    Double.isNaN(q.eKante20) ? "" : String.format(Locale.ROOT, "%.2f", q.eKante20), Double.isNaN(q.hKante20) ? "" : String.format(Locale.ROOT, "%.2f", q.hKante20), q.wand()));
        }
        Files.writeString(new File(kl, "kante.csv").toPath(), csv.toString());
        ByteBuffer bb = ByteBuffer.allocate(band.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        int bandLeer = 0;
        for (float q : band) { if (Float.isNaN(q)) bandLeer++; bb.putShort(Terrain.encode(Float.isNaN(q) ? Terrain.HMIN : q)); }
        Files.write(new File(kl, "band20.r16").toPath(), bb.array());
        ByteBuffer be = ByteBuffer.allocate(bRows * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float q : bandE0) be.putFloat(q);
        Files.write(new File(kl, "band20_e0.f32").toPath(), be.array());
        Properties p = new Properties();
        p.setProperty("zeilen", "" + bRows);
        p.setProperty("spalten", "" + bCols);
        p.setProperty("zelle", "0.2");
        p.setProperty("nord_oben", String.format(Locale.ROOT, "%.1f", bandN));
        p.setProperty("ost_je_zeile", "band20_e0.f32 (float32 LE, Ostwert der linken Kante je Zeile)");
        p.setProperty("hoehen", "uint16 LE wie Terrain.encode (HMIN -40 bis HMAX 60), ohne Wert = 0");
        p.setProperty("quelle", "bDOM 2024 Schleswig-Holstein, 20 cm (bdom20nc); © GeoBasis-DE/LVermGeo SH, Datenlizenz Deutschland – Namensnennung – 2.0");
        p.setProperty("werkzeug", "com.dan.sylt.tools.KanteImport");
        try (var w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new File(kl, "band20.properties")), java.nio.charset.StandardCharsets.UTF_8)) {
            p.store(w, "Band des bDOM laengs des Kliffs");
        }

        // 4. Bericht: Wand, Kante, Türme
        double wSum = 0, wMax = 0, d20 = 0;
        int wc = 0, dc = 0;
        double nMax = 0;
        for (Zeile q : z) {
            if (!q.kliff()) continue;
            wSum += q.wand();
            if (q.wand() > wMax) { wMax = q.wand(); nMax = q.n; }
            wc++;
            if (!Double.isNaN(q.eKante20)) { d20 += Math.abs(q.eKante20 - q.eKante); dc++; }
        }
        bericht.add(Raster.f("Kliff: %d m lang (N %.0f–%.0f), Wand im Mittel %.1f m, höchste %.1f m bei N %.0f; Kante aus dem bDOM an %d Zeilen, im Mittel %.2f m neben der aus dem DGM1",
                wc, z[jS].n, z[jN].n, wSum / Math.max(1, wc), wMax, nMax, dc, d20 / Math.max(1, dc)));
        bericht.add(Raster.f("Band zu 20 cm: %d × %d Werte (%.0f %% ohne Wert)", bRows, bCols, 100.0 * bandLeer / band.length));
        for (Site.Ort o : new Site.Ort[]{Site.QUERMARKE, Site.LEUCHTTURM}) {
            double[] tm = turm(bewuchs, kern, KN, kE, kN, o.e(), o.n(), 80);
            if (tm == null) bericht.add(o.name() + ": im bDOM nichts Hohes binnen 80 m");
            else bericht.add(Raster.f("%s: höchster Punkt %.1f m über Grund (Grund %.1f m NHN) bei O %.0f, N %.0f, %.0f m von der Schätzung",
                    o.name(), tm[2], tm[3], tm[0], tm[1], Math.hypot(tm[0] - o.e(), tm[1] - o.n())));
        }
        long bew = 0, haus = 0;
        for (int k = 0; k < bewuchs.length; k++) { if ((bewuchs[k] & 0xff) * BEWUCHS_STUFE > 0.4) bew++; if ((bewuchs[k] & 0xff) * BEWUCHS_STUFE > 3) haus++; }
        bericht.add(Raster.f("Bewuchs: %.1f %% des Kerns über 0,4 m, %.1f %% über 3 m (Gebüsch, Häuser)", 100.0 * bew / bewuchs.length, 100.0 * haus / bewuchs.length));
        for (String s : bericht) log.accept("  " + s);
        bild(z, jN, jS, kern, KN, kE, kN, band, bRows, bCols, bandN, bandE0, bericht, BILD);
        log.accept(Raster.f("Kante: daten/kliff, daten/kern/bewuchs.u8 · %.1f s", (System.nanoTime() - t0) / 1e9));
        return bericht;
    }

    static Zeile leer(double n) { return new Zeile(n, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN); }

    /**
     * Kliff in einer Kernzeile aus dem DGM1. Befund Phase 2: die Wand steigt im DGM1 nicht gleichmäßig, sondern mit
     * Schuttfuß und Absätzen, im Mittel um 1:2, an der steilsten Stelle 1:1 bis 2:1. Darum: von der Wasserlinie landwärts
     * die erste Stelle steiler als {@link #STEIL}; von dort seewärts der Fuß, wo es auf 8 m im Mittel flacher als 1:10
     * wird, landwärts die Oberkante, wo es auf 8 m im Mittel flacher als {@link #FLACH} wird (folgt binnen 25 m eine
     * weitere steile Stufe, zählt der Absatz zur Wand). Kliff, wenn die Wand
     * mindestens {@link #WAND_MIN} hoch ist und der Fuß unter +8 m liegt.
     */
    static Zeile kanteDgm(float[] k, int KN, int j, int kE, double n, double eW) {
        if (Double.isNaN(eW)) return leer(n);
        int i0 = (int) Math.floor(eW - kE) + 10, i1 = Math.min(KN - 12, i0 + 260);
        if (i0 < 12 || i0 >= KN - 12) return leer(n);
        float[] row = new float[KN];
        System.arraycopy(k, j * KN, row, 0, KN);
        for (int i = i0; i < i1; i++) {
            if (neig(row, i) < STEIL) continue;
            int f = i;
            while (f > i0 && (row[f] - row[f - 8]) / 8.0 > 0.1) f--;
            int o = i;
            while (o < i1 && (row[o + 8] - row[o]) / 8.0 > FLACH) o++;
            // Absatz: folgt binnen 25 m noch eine steile Stufe, gehört sie zur Wand
            for (boolean weiter = true; weiter; ) {
                weiter = false;
                for (int q = o + 1; q < Math.min(i1, o + 25); q++)
                    if (neig(row, q) >= STEIL) {
                        o = q;
                        while (o < i1 && (row[o + 8] - row[o]) / 8.0 > FLACH) o++;
                        weiter = true;
                        break;
                    }
            }
            // Oberkante genauer: im letzten Stück der höchste Punkt vor der Ebene
            int top = o;
            for (int q = o; q <= o + 8 && q < KN; q++) if (row[q] > row[top] + 0.05) top = q;
            double hf = row[f], ho = row[top];
            if (ho - hf >= WAND_MIN && hf < 8) return new Zeile(n, kE + f + 0.5, hf, kE + top + 0.5, ho, Double.NaN, Double.NaN);
            i = top;
        }
        return leer(n);
    }

    /** Neigung an Zelle i über ±2 m. */
    static double neig(float[] r, int i) { return (r[i + 2] - r[i - 2]) / 4.0; }

    /** Oberkante in einer bDOM-Zeile: der erste Punkt (von See her), an dem die Neigung über 0,6 m nach steil wieder unter FLACH fällt. */
    static double[] kante20(float[] v, int w, int y, int xa, int xb) {
        boolean steil = false;
        for (int x = xa; x <= xb; x++) {
            double s = 0;
            int c = 0;
            for (int dy = -1; dy <= 1; dy++) {
                float a = v[(y + dy) * w + x - 2], b = v[(y + dy) * w + x + 2];
                if (Float.isNaN(a) || Float.isNaN(b)) continue;
                s += (b - a) / (4 * PX);
                c++;
            }
            if (c == 0) continue;
            s /= c;
            if (s > STEIL) steil = true;
            else if (steil && s < FLACH) return new double[]{x, v[y * w + x]};
        }
        return null;
    }

    static void lueckenZiehen(double[] a, int j0, int j1) {
        int last = -1;
        for (int j = j0; j <= j1; j++) {
            if (Double.isNaN(a[j])) continue;
            if (last < 0) for (int q = j0; q < j; q++) a[q] = a[j];
            else for (int q = last + 1; q < j; q++) a[q] = a[last] + (a[j] - a[last]) * (q - last) / (double) (j - last);
            last = j;
        }
        for (int q = last + 1; q <= j1; q++) a[q] = a[last];
    }

    /** Höchster Bewuchs (Turm) im Umkreis rad um (e, n): {e, n, Höhe über Grund, Grund}; null, wenn nichts über 6 m. */
    static double[] turm(byte[] bew, float[] k, int KN, int kE, int kN, double e, double n, double rad) {
        double best = 6;
        double[] r = null;
        for (int dj = (int) -rad; dj <= rad; dj++)
            for (int di = (int) -rad; di <= rad; di++) {
                if (di * di + dj * dj > rad * rad) continue;
                int i = (int) Math.floor(e - kE) + di, j = (int) Math.floor(kN - n) + dj;
                if (i < 0 || j < 0 || i >= KN || j >= KN) continue;
                double hb = (bew[j * KN + i] & 0xff) * BEWUCHS_STUFE;
                if (hb > best) { best = hb; r = new double[]{kE + i + 0.5, kN - j - 0.5, hb, k[j * KN + i]}; }
            }
        return r;
    }

    // ------------------------------------------------------------------ Prüfbild

    static void bild(Zeile[] z, int jN, int jS, float[] kern, int KN, int kE, int kN, float[] band, int bRows, int bCols, double bandN, float[] bandE0,
                     List<String> bericht, File png) throws IOException {
        // links: Schummerung des Kerns über dem Kliff (Ausschnitt), mit Fuß (blau) und Kante (rot); rechts vier Querprofile
        double eA = 456_250, eB = 457_650;
        double nA = z[jS].n - 150, nB = z[jN].n + 150;
        double sc = (nB - nA) / 1000.0;   // m je Bildpunkt
        int mw = (int) Math.round((eB - eA) / sc), mh = 1000;
        int W = mw + 20 + 900, H = mh + 20 + 30 + 18 * (bericht.size() + 2);
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(0x1d2328));
        g.fillRect(0, 0, W, H);
        for (int y = 0; y < mh; y++)
            for (int x = 0; x < mw; x++) {
                double e = eA + (x + 0.5) * sc, n = nB - (y + 0.5) * sc;
                int i = (int) Math.floor(e - kE), j = (int) Math.floor(kN - n);
                if (i < 1 || j < 1 || i >= KN - 1 || j >= KN - 1) continue;
                img.setRGB(10 + x, 10 + y, Raster.hoehenfarbe(kern[j * KN + i], Raster.schummer(kern, KN, i, j, 1, 1.2)));
            }
        g.setStroke(new BasicStroke(1.4f));
        Path2D fuss = new Path2D.Double(), kante = new Path2D.Double();
        boolean fOn = false, kOn = false;
        for (int j = jN; j <= jS; j++) {
            Zeile q = z[j];
            if (!q.kliff()) { fOn = false; kOn = false; continue; }
            double py = 10 + (nB - q.n) / sc;
            double fx = 10 + (q.eFuss - eA) / sc, kx = 10 + ((Double.isNaN(q.eKante20) ? q.eKante : q.eKante20) - eA) / sc;
            if (fOn) fuss.lineTo(fx, py); else { fuss.moveTo(fx, py); fOn = true; }
            if (kOn) kante.lineTo(kx, py); else { kante.moveTo(kx, py); kOn = true; }
        }
        g.setColor(new Color(0x50, 0xb0, 0xff));
        g.draw(fuss);
        g.setColor(new Color(0xff, 0x50, 0x40));
        g.draw(kante);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Rotes Kliff: Fuß (blau), Oberkante (rot)", 16, 28);
        // Profile
        double[] ns = {6_088_300, 6_088_900, 6_089_500, 6_090_100, 6_090_700};
        int px0 = mw + 30, pw = 860, ph = (mh - 20) / ns.length;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int s = 0; s < ns.length; s++) {
            int top = 10 + s * ph + 4;
            int j = (int) Math.floor(kN - ns[s]);
            if (j < 0 || j >= KN) continue;
            Zeile q = z[j];
            double ref = kE + KN / 2.0;
            for (int dq = 0; dq < 200; dq++) {
                if (j + dq <= jS && j + dq >= 0 && z[j + dq].kliff()) { ref = z[j + dq].eFuss; break; }
                if (j - dq >= jN && j - dq < KN && z[j - dq].kliff()) { ref = z[j - dq].eFuss; break; }
            }
            double e0 = ref - 60, e1 = ref + 140;
            double h0 = -4, h1 = 52;
            g.setColor(new Color(0x2a, 0x32, 0x38));
            g.fillRect(px0, top, pw, ph - 8);
            g.setColor(new Color(0x60, 0x6a, 0x70));
            int y0 = (int) (top + (ph - 8) * (h1 - 0) / (h1 - h0));
            g.drawLine(px0, y0, px0 + pw, y0);
            // DGM1
            Path2D d = new Path2D.Double();
            for (int x = 0; x < pw; x++) {
                double e = e0 + (e1 - e0) * x / pw;
                int i = (int) Math.floor(e - kE);
                if (i < 0 || i >= KN) continue;
                double yy = top + (ph - 8) * (h1 - kern[j * KN + i]) / (h1 - h0);
                if (x == 0) d.moveTo(px0 + x, yy); else d.lineTo(px0 + x, yy);
            }
            // bDOM-Band
            Path2D bd = new Path2D.Double();
            int r2 = (int) Math.floor((bandN - ns[s]) / PX);
            boolean on = false;
            if (r2 >= 0 && r2 < bRows)
                for (int x = 0; x < pw; x++) {
                    double e = e0 + (e1 - e0) * x / pw;
                    int c = (int) Math.floor((e - bandE0[r2]) / PX);
                    if (c < 0 || c >= bCols || Float.isNaN(band[r2 * bCols + c])) { on = false; continue; }
                    double yy = top + (ph - 8) * (h1 - band[r2 * bCols + c]) / (h1 - h0);
                    if (on) bd.lineTo(px0 + x, yy); else { bd.moveTo(px0 + x, yy); on = true; }
                }
            g.setColor(new Color(0x70, 0xd0, 0x70));
            g.draw(bd);
            g.setColor(new Color(0xf0, 0xe0, 0xc0));
            g.draw(d);
            g.setColor(Color.WHITE);
            g.drawString(Raster.f("N %.0f: %s", ns[s], q.kliff() ? Raster.f("Fuß %.1f m, Kante %.1f m, Wand %.1f m", q.hFuss, q.hKante, q.wand()) : "kein Kliff"), px0 + 6, top + 14);
            if (q.kliff()) {
                g.setColor(new Color(0xff, 0x50, 0x40));
                int kx = (int) (px0 + pw * ((Double.isNaN(q.eKante20) ? q.eKante : q.eKante20) - e0) / (e1 - e0));
                g.drawLine(kx, top + 18, kx, top + ph - 10);
            }
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString("Querprofile, 200 m ab 60 m vor dem Fuß: DGM1 (hell), bDOM 20 cm (grün), Kante (rot)", px0, H - 18 * (bericht.size() + 2) - 4);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int y = mh + 46;
        g.drawString("Sylt Phase 2 · Kliff aus DGM1 2024 und bDOM 2024 · " + java.time.LocalDate.now(), 12, y);
        for (String s : bericht) { y += 18; g.drawString(s, 12, y); }
        g.dispose();
        png.getAbsoluteFile().getParentFile().mkdirs();
        javax.imageio.ImageIO.write(img, "png", png);
    }
}
