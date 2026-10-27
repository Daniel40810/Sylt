package com.dan.sylt.tools;

import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Terrain;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Das Luftbild DOP20 zu Klassen (Phase 2, Schritt {@code luftbild}, F5 A: das Bild dient nur den Klassen und dem Eichen
 * der Farben; gezeichnet wird prozedural). Braucht {@code daten/gelaende} und {@code daten/kern/bewuchs.u8}.
 * <ul>
 * <li>Kern: das DOP20 je Meter gemittelt ({@code daten/kern/luftbild.rgbi}, 4 096² × R, G, B, Infrarot) und die Klasse je
 * Meter ({@code daten/kern/klasse.u8}, Codes wie {@link Terrain#ART}) aus Farbe, Infrarot (NDVI), Bewuchshöhe (bDOM),
 * Höhe, Neigung und Abstand zur Wasserlinie ({@link #klasse}).</li>
 * <li>Hauptraster: die Art je 10-m-Zelle als häufigste Klasse ihrer Meter (im Kern) oder ihrer 2-m-Flecken (außerhalb, ohne
 * Bewuchshöhe); wo kein Luftbild ist (offene See, Watt), bleibt die Art aus {@code gelaende}.</li>
 * <li>{@code daten/kern/farben.properties}: mittlere Farbe je Klasse im Kern (sRGB 0..1), zum Eichen der Farben in Phase 3.</li>
 * </ul>
 * Prüfbild {@code docs/status/Luftbild_Phase2.png}: links das Luftbild, rechts die Klassen.
 */
public final class LuftbildImport {
    public static final File BILD = new File("docs/status/Luftbild_Phase2.png");
    static final Pattern NAME = Pattern.compile("dop20rgbi_32_(\\d{3})_(\\d{4})_1_sh_(\\d{4})(_bw)?\\.tif", Pattern.CASE_INSENSITIVE);
    /** Farben der Klassen im Prüfbild. */
    static final int[] FARBE = {0x000000, 0x2f5f8f, 0x6f7f86, 0xeadfb8, 0xa69a78, 0xc8643c, 0xf4ecd0, 0x7a6a5a, 0x2f5f2f, 0x7fbf4f,
            0xa0a0a0, 0x505050, 0xb8c070, 0xc03030};

    private LuftbildImport() { }

    public static void main(String[] a) throws Exception { importieren(Rohdaten.laden(), new File("daten"), System.out::println); }

    /** Was über einen Ort bekannt ist, wenn er klassiert wird. */
    record Ort(double h, double neigung, double bewuchs, double kueste, boolean wasser, byte vorher) { }

    /**
     * Die Klasse eines Flecks aus R, G, B, Infrarot (0..255) und dem Ort [Schwellen G, am Kern geeicht in Phase 2]:
     * Wasserfläche der Befliegung oder dunkel im Infrarot → See/Watt; Pflanzen (NDVI &gt; 0,12) nach Höhe und Farbe
     * (Gebüsch über 2 m, Wiese satt grün, Strandhafer hell, sonst Heide); kahl: Dach über 2,5 m, steiler Hang rot →
     * Kliff, hell → Strand an der Küste, sonst Düne; dunkel und tief an der Küste → nasser Sand, tief sonst Watt; grau → Weg.
     */
    static byte klasse(int r, int g, int b, int ir, Ort o) {
        double nd = (ir - r) / (double) Math.max(1, ir + r), br = (r + g + b) / 3.0;
        boolean wasserFarbe = ir < 60 && nd < 0.05 && br < 120;
        if (o.wasser || (wasserFarbe && o.h < 1.5))
            return o.vorher == Terrain.WATT || (o.kueste > 400 && o.h > -3) ? Terrain.WATT : Terrain.SEE;
        boolean pflanze = nd > 0.12;
        if (pflanze) {
            if (o.bewuchs > 2.0) return Terrain.GEBUESCH;
            if (nd > 0.33 && g > r + 4) return Terrain.WIESE;
            if (br > 135 && nd < 0.30) return Terrain.STRANDHAFER;
            return Terrain.HEIDE;
        }
        if (o.bewuchs > 2.5) return Terrain.DACH;
        if (o.neigung > 0.6) return r > b + 22 ? Terrain.KLIFF : Terrain.DUENE;
        if (br > 165) return o.kueste < 150 && o.h < 8 ? Terrain.STRAND : Terrain.DUENE;
        if (o.h < 3 && o.kueste < 200) return Terrain.NASS;
        if (o.h < 1.5) return Terrain.WATT;   // grauer Schlick im Osten (Befund Phase 2: sonst „Weg“)
        double sat = Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b));
        if (sat < 22 && br > 50) return Terrain.WEG;
        return br > 120 ? Terrain.DUENE : Terrain.HEIDE;
    }

    public static List<String> importieren(Rohdaten r, File daten, Consumer<String> log) throws IOException {
        long t0 = System.nanoTime();
        List<String> bericht = new ArrayList<>();
        File gd = new File(daten, "gelaende"), kd = new File(daten, "kern");
        Terrain t = Terrain.load(gd);
        if (!t.measured() || !t.hasKern()) throw new IOException("daten/gelaende fehlt: erst Schritt gelaende");
        File bf = new File(kd, "bewuchs.u8");
        if (!bf.isFile()) throw new IOException("daten/kern/bewuchs.u8 fehlt: erst Schritt kante");
        byte[] bew = Files.readAllBytes(bf.toPath());
        byte[] quelle = Files.readAllBytes(new File(gd, GelaendeImport.QUELLE).toPath());
        int KN = Terrain.KN, N = Terrain.N;
        int kE = (int) Math.round(Terrain.KX0 + Site.E0), kN = (int) Math.round(Site.N0 - Terrain.KZ0);
        int hE = (int) Math.round(Terrain.X0 + Site.E0), hN = (int) Math.round(Site.N0 - Terrain.Z0);
        float[] kern = new float[KN * KN];
        for (int k = 0; k < kern.length; k++) kern[k] = (float) Terrain.decode(t.kern[k]);
        float[] haupt = new float[N * N];
        for (int k = 0; k < haupt.length; k++) haupt[k] = (float) Terrain.decode(t.h[k]);
        TreeMap<Double, Double> wl = wasserlinie(new File(gd, GelaendeImport.WASSERLINIE));
        byte[] vorher = t.art.clone();

        // Kacheln
        TreeMap<Integer, File> kacheln = new TreeMap<>();
        int bwKacheln = 0;
        File[] fs = r.dop20().listFiles();
        if (fs == null) throw new IOException("DOP20: Ordner " + r.dop20() + " fehlt");
        for (File f : fs) {
            Matcher m = NAME.matcher(f.getName());
            if (!m.matches()) continue;
            int key = Integer.parseInt(m.group(1)) * 10000 + Integer.parseInt(m.group(2));
            if (m.group(4) != null) { bwKacheln++; kacheln.putIfAbsent(key, f); }
            else kacheln.put(key, f);   // die farbige vor der schwarz-weißen
        }

        byte[] rgbi = new byte[KN * KN * 4];
        byte[] klasse = new byte[KN * KN];
        int nk = Terrain.ART.length;
        byte[] stimmen = new byte[N * N * nk];
        int gelesen = 0, ohneFarbe = 0;
        for (var en : kacheln.entrySet()) {
            int ek = en.getKey() / 10000, nkm = en.getKey() % 10000;
            boolean imHaupt = (ek + 1) * 1000 > hE && ek * 1000 < hE + N * 10 && (nkm + 1) * 1000 > hN - N * 10 && nkm * 1000 < hN;
            if (!imHaupt) continue;
            byte[] px;
            int w, h, spp;
            try (GeoTiff g = GeoTiff.open(en.getValue())) {
                w = g.width;
                h = g.height;
                spp = g.samples;
                if (spp < 4) { ohneFarbe++; continue; }
                px = g.bytes(0, 0, w, h);
            }
            gelesen++;
            // Kern: je Meter (5 × 5 Bildpunkte)
            for (int y = 0; y + 5 <= h; y += 5) {
                int kj = kN - ((nkm + 1) * 1000 - y / 5);
                if (kj < 0 || kj >= KN) continue;
                for (int x = 0; x + 5 <= w; x += 5) {
                    int ki = ek * 1000 + x / 5 - kE;
                    if (ki < 0 || ki >= KN) continue;
                    int[] s = mittel(px, w, spp, x, y, 5);
                    int k = kj * KN + ki;
                    for (int c = 0; c < 4; c++) rgbi[k * 4 + c] = (byte) s[c];
                    double e = kE + ki + 0.5, n = kN - kj - 0.5;
                    Ort o = new Ort(kern[k], neigung(kern, KN, ki, kj, 1), (bew[k] & 0xff) * KanteImport.BEWUCHS_STUFE, e - kueste(wl, n),
                            quelle[k] == 2, vorherAt(vorher, hE, hN, e, n));
                    klasse[k] = klasse(s[0], s[1], s[2], s[3], o);
                }
            }
            // außerhalb des Kerns: je 2 m (10 × 10 Bildpunkte), Stimmen je 10-m-Zelle
            for (int y = 0; y + 10 <= h; y += 10)
                for (int x = 0; x + 10 <= w; x += 10) {
                    double e = ek * 1000 + (x + 5) * 0.2, n = (nkm + 1) * 1000 - (y + 5) * 0.2;
                    if (e >= kE && e < kE + KN && n <= kN && n > kN - KN) continue;
                    int hi = (int) Math.floor((e - hE) / 10), hj = (int) Math.floor((hN - n) / 10);
                    if (hi < 0 || hj < 0 || hi >= N || hj >= N) continue;
                    int[] s = mittel(px, w, spp, x, y, 10);
                    if (s[0] + s[1] + s[2] + s[3] == 0) continue;
                    int q = hj * N + hi;
                    Ort o = new Ort(haupt[q], neigung(haupt, N, hi, hj, 10), 0, e - kueste(wl, n), false, vorher[q]);
                    byte c = klasse(s[0], s[1], s[2], s[3], o);
                    stimmen[q * nk + c]++;
                }
            if (gelesen % 25 == 0) log.accept(Raster.f("  DOP20 %d Kacheln gelesen …", gelesen));
        }
        // Kern in die Stimmen: je 10-m-Zelle die 100 Meter
        for (int kj = 0; kj < KN; kj++)
            for (int ki = 0; ki < KN; ki++) {
                double e = kE + ki + 0.5, n = kN - kj - 0.5;
                int hi = (int) Math.floor((e - hE) / 10), hj = (int) Math.floor((hN - n) / 10);
                int k = kj * KN + ki;
                if (klasse[k] == 0) continue;
                int q = (hj * N + hi) * nk + klasse[k];
                if (stimmen[q] < 100) stimmen[q]++;
            }
        long geaendert = 0, mitBild = 0;
        for (int q = 0; q < N * N; q++) {
            int best = 0, bc = 0;
            for (int c = 1; c < nk; c++) if (stimmen[q * nk + c] > bc) { bc = stimmen[q * nk + c]; best = c; }
            if (bc == 0) continue;
            mitBild++;
            // Kliff, Dach und Weg sind schmal: schon ein Viertel der Stimmen genügt gegen Heide und Düne
            for (int c : new int[]{Terrain.KLIFF, Terrain.DACH}) if (stimmen[q * nk + c] * 4 >= sum(stimmen, q * nk, nk) && c != best) { best = c; break; }
            if (t.art[q] != best) geaendert++;
            t.art[q] = (byte) best;
        }
        // schreiben
        kd.mkdirs();
        Files.write(new File(kd, "luftbild.rgbi").toPath(), rgbi);
        Files.write(new File(kd, "klasse.u8").toPath(), klasse);
        Files.write(new File(gd, Terrain.ARTEN).toPath(), t.art);
        Properties gp = new Properties();
        try (var in = new java.io.InputStreamReader(new java.io.FileInputStream(new File(gd, Terrain.INFO)), java.nio.charset.StandardCharsets.UTF_8)) { gp.load(in); }
        gp.setProperty("arten", "DOP20 2024 (RGBI 20 cm) mit bDOM und DGM1, LuftbildImport; See und Watt ohne Luftbild aus der Höhe");
        try (var o = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new File(gd, Terrain.INFO)), java.nio.charset.StandardCharsets.UTF_8)) {
            gp.store(o, "Gelaende der Szene Sylt (tools.GelaendeImport, Arten tools.LuftbildImport)");
        }
        // Farben je Klasse im Kern
        double[][] sum = new double[nk][4];
        long[] cnt = new long[nk];
        for (int k = 0; k < klasse.length; k++) {
            int c = klasse[k];
            if (c == 0) continue;
            cnt[c]++;
            for (int b = 0; b < 4; b++) sum[c][b] += rgbi[k * 4 + b] & 0xff;
        }
        Properties fp = new Properties();
        StringBuilder anteile = new StringBuilder();
        for (int c = 1; c < nk; c++) {
            if (cnt[c] == 0) continue;
            fp.setProperty("farbe." + c, String.format(Locale.ROOT, "%.3f %.3f %.3f", sum[c][0] / cnt[c] / 255, sum[c][1] / cnt[c] / 255, sum[c][2] / cnt[c] / 255));
            fp.setProperty("infrarot." + c, String.format(Locale.ROOT, "%.3f", sum[c][3] / cnt[c] / 255));
            fp.setProperty("anteil." + c, String.format(Locale.ROOT, "%.4f", cnt[c] / (double) klasse.length));
            fp.setProperty("name." + c, Terrain.ART[c]);
            anteile.append(anteile.length() == 0 ? "" : ", ").append(Terrain.ART[c]).append(String.format(Locale.GERMAN, " %.1f %%", 100.0 * cnt[c] / klasse.length));
        }
        try (var o = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new File(kd, "farben.properties")), java.nio.charset.StandardCharsets.UTF_8)) {
            fp.store(o, "Mittlere Farbe je Klasse im Kern aus DOP20 2024 (sRGB 0..1), LuftbildImport");
        }
        bericht.add(Raster.f("DOP20: %d Kacheln gelesen (%d nur schwarz-weiß übergangen, %d _bw im Ordner)", gelesen, ohneFarbe, bwKacheln));
        bericht.add("Klassen im Kern: " + anteile);
        bericht.add(Raster.f("Hauptraster: %.0f %% der Zellen mit Luftbild, %,d Arten gegenüber gelaende geändert; %s", 100.0 * mitBild / (N * N), geaendert, t.describe()));
        for (String s : bericht) log.accept("  " + s);
        bild(rgbi, klasse, t, bericht, BILD);
        log.accept(Raster.f("Luftbild: daten/kern/luftbild.rgbi, klasse.u8, farben.properties, daten/gelaende/art.u8 · %.1f s", (System.nanoTime() - t0) / 1e9));
        return bericht;
    }

    static int sum(byte[] s, int off, int n) {
        int t = 0;
        for (int i = 0; i < n; i++) t += s[off + i];
        return t;
    }

    static int[] mittel(byte[] px, int w, int spp, int x, int y, int n) {
        int[] s = new int[4];
        for (int b = 0; b < n; b++)
            for (int a = 0; a < n; a++) {
                int p = ((y + b) * w + x + a) * spp;
                for (int c = 0; c < 4; c++) s[c] += px[p + c] & 0xff;
            }
        for (int c = 0; c < 4; c++) s[c] /= n * n;
        return s;
    }

    static double neigung(float[] h, int n, int i, int j, double cell) { return GelaendeImport.neigung(h, n, i, j, cell); }

    static byte vorherAt(byte[] art, int hE, int hN, double e, double n) {
        int i = (int) Math.floor((e - hE) / 10), j = (int) Math.floor((hN - n) / 10);
        if (i < 0 || j < 0 || i >= Terrain.N || j >= Terrain.N) return Terrain.KEINE;
        return art[j * Terrain.N + i];
    }

    static TreeMap<Double, Double> wasserlinie(File f) throws IOException {
        TreeMap<Double, Double> m = new TreeMap<>();
        for (String l : Files.readAllLines(f.toPath())) {
            if (l.isEmpty() || !Character.isDigit(l.charAt(0))) continue;
            String[] a = l.split(";");
            m.put(Double.parseDouble(a[0]), Double.parseDouble(a[1]));
        }
        return m;
    }

    /** Ostwert der Wasserlinie am Nordwert n (linear zwischen den Stützstellen). */
    static double kueste(TreeMap<Double, Double> wl, double n) {
        var lo = wl.floorEntry(n);
        var hi = wl.ceilingEntry(n);
        if (lo == null) return hi.getValue();
        if (hi == null || hi.getKey().equals(lo.getKey())) return lo.getValue();
        return lo.getValue() + (hi.getValue() - lo.getValue()) * (n - lo.getKey()) / (hi.getKey() - lo.getKey());
    }

    static void bild(byte[] rgbi, byte[] klasse, Terrain t, List<String> bericht, File png) throws IOException {
        int S = 1024, KN = Terrain.KN, N = Terrain.N;
        int W = 3 * S + 40, H = S + 60 + 18 * (bericht.size() + 3);
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(0x1d2328));
        g.fillRect(0, 0, W, H);
        for (int y = 0; y < S; y++)
            for (int x = 0; x < S; x++) {
                int rr = 0, gg = 0, bb = 0;
                int[] votes = new int[Terrain.ART.length];
                for (int b = 0; b < 4; b++)
                    for (int a = 0; a < 4; a++) {
                        int k = (y * 4 + b) * KN + x * 4 + a;
                        rr += rgbi[k * 4] & 0xff;
                        gg += rgbi[k * 4 + 1] & 0xff;
                        bb += rgbi[k * 4 + 2] & 0xff;
                        votes[klasse[k]]++;
                    }
                img.setRGB(10 + x, 10 + y, 0xff000000 | (rr / 16) << 16 | (gg / 16) << 8 | bb / 16);
                int best = 0;
                for (int c = 1; c < votes.length; c++) if (votes[c] > votes[best]) best = c;
                img.setRGB(20 + S + x, 10 + y, 0xff000000 | FARBE[best]);
            }
        for (int y = 0; y < S; y++)
            for (int x = 0; x < S; x++) img.setRGB(30 + 2 * S + x, 10 + y, 0xff000000 | FARBE[t.art[(y * 2) * N + x * 2]]);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(Color.WHITE);
        g.drawString("DOP20 2024 im Kern, je Meter", 16, 30);
        g.drawString("Klassen im Kern (1 m)", 26 + S, 30);
        g.drawString("Arten im Hauptraster (10 m)", 36 + 2 * S, 30);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        int lx = 16, ly = S + 34;
        for (int c = 1; c < Terrain.ART.length; c++) {
            g.setColor(new Color(FARBE[c]));
            g.fillRect(lx, ly - 11, 14, 14);
            g.setColor(Color.WHITE);
            g.drawString(Terrain.ART[c], lx + 18, ly);
            lx += 18 + g.getFontMetrics().stringWidth(Terrain.ART[c]) + 22;
        }
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int y = ly + 24;
        g.drawString("Sylt Phase 2 · Klassen aus DOP20, bDOM und DGM1 · " + java.time.LocalDate.now(), 12, y);
        for (String s : bericht) for (String l : GelaendeImport.wrap(s, 200)) { y += 18; g.drawString(l, 12, y); }
        g.dispose();
        png.getAbsoluteFile().getParentFile().mkdirs();
        javax.imageio.ImageIO.write(img, "png", png);
    }
}
