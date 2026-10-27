package com.dan.sylt.scene;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Die Häuser aus LOD2 (seit Phase 10), gelesen aus {@code daten/bauten/bauten.bin} (Phase 2, {@code tools.BautenImport}):
 * je Gebäude Mitte, Grund, Höhe und Dreiecke mit Art (Dach, Wand, Boden) in Koordinaten der Szene. Hier bekommt jedes
 * Haus seine Baustoffe [G nach Kampen1–4 und dem Ortsbild]: Friesenhäuser aus rotem Backstein mit Reetdach (in Kampen
 * die meisten), weiße Häuser mit grauem Dach (die Wärterhäuser am Leuchtturm), dazwischen gelbe Klinker und rote Ziegel.
 * Die zwei Türme stehen auch in LOD2; sie werden hier ausgelassen und in {@link Bauwerke} genauer gebaut.
 */
public final class Bauten {
    /** Ein Gebäude: Kennung, Mitte (x, z), Grund und First (m NHN), erstes Dreieck und Zahl der Dreiecke in {@link #tri}. */
    public record Haus(String id, float x, float z, float grund, float hoehe, int erstes, int zahl) {
        public double hoeheUeberGrund() { return hoehe - grund; }
    }

    /** Dreiecke: je 10 Werte (Art 0 Dach, 1 Wand, 2 Boden; dann x y z dreimal). */
    public final float[] tri;
    public final List<Haus> haeuser = new ArrayList<>();
    public final int ausgelassen;
    public final String quelle;

    private Bauten(float[] tri, List<Haus> h, int aus, String q) {
        this.tri = tri;
        haeuser.addAll(h);
        ausgelassen = aus;
        quelle = q;
    }

    /** Ohne Daten: keine Häuser. */
    public static Bauten leer() { return new Bauten(new float[0], List.of(), 0, "ohne daten/bauten"); }

    /** Liest {@code bauten.bin}; Gebäude näher als {@code frei} m an einem der Punkte {@code ausnahmen} (x, z) bleiben weg. */
    public static Bauten laden(File dir, double[][] ausnahmen, double frei) {
        File f = new File(dir, "bauten/bauten.bin");
        if (!f.isFile()) return leer();
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f), 1 << 20))) {
            byte[] mag = new byte[6];
            in.readFully(mag);
            if (!"SYBAU1".equals(new String(mag, StandardCharsets.US_ASCII))) throw new IOException("kein SYBAU1");
            int n = in.readInt();
            long nt = in.readLong();
            float[] t = new float[(int) Math.min(Integer.MAX_VALUE - 16, nt * 10)];
            List<Haus> hs = new ArrayList<>(n);
            int k = 0, aus = 0;
            for (int i = 0; i < n; i++) {
                String id = in.readUTF();
                float x = in.readFloat(), z = in.readFloat(), g = in.readFloat(), h = in.readFloat();
                int m = in.readInt();
                boolean weg = false;
                for (double[] a : ausnahmen) if (Math.hypot(x - a[0], z - a[1]) < frei) weg = true;
                int erstes = k / 10;
                for (int j = 0; j < m; j++) {
                    byte art = in.readByte();
                    float[] v = new float[9];
                    for (int q = 0; q < 9; q++) v[q] = in.readFloat();
                    if (weg) continue;
                    t[k++] = art;
                    System.arraycopy(v, 0, t, k, 9);
                    k += 9;
                }
                if (weg) aus++;
                else hs.add(new Haus(id, x, z, g, h, erstes, k / 10 - erstes));
            }
            return new Bauten(java.util.Arrays.copyOf(t, k), hs, aus, f.getPath());
        } catch (IOException e) {
            System.err.println("Sylt: Bauten nicht lesbar: " + e.getMessage());
            return leer();
        }
    }

    public int dreiecke() { return tri.length / 10; }

    /** Baustoffe eines Hauses: Wand, Dach, ob die Wände Fenster bekommen; Neigung des Dachs (Grad) und Grundfläche (m²). */
    public record Stil(int wand, int dach, boolean fenster, double neigung, double flaeche) { }

    /** Kennzahl 0..511 aus der Kennung (Spielarten im Shader, Zufall der Baustoffe). */
    public static int kennzahl(String id) {
        int h = id.hashCode();
        h ^= h >>> 16;
        h *= 0x45d9f3b;
        h ^= h >>> 16;
        return h & 511;
    }

    /** Anteil der Reetdächer [G nach dem Ortsbild]: Kampen 55 %, Wenningstedt und Braderup 30 %, sonst 12 %. */
    public static double reetAnteil(double e, double n) {
        if (n >= 6_089_250 && n <= 6_091_600 && e >= 457_400) return 0.55;
        if (n >= 6_086_300 && n < 6_089_250) return 0.30;
        return 0.12;
    }

    /**
     * Die Baustoffe eines Hauses [G nach Kampen1–4 und dem Ortsbild]: steile Dächer (ab 38°) kleiner Häuser je nach Ort
     * oft aus Reet über rotem Backstein (Friesenhaus), sonst rote Ziegel oder graue Dächer über weißem Putz, Backstein
     * oder gelbem Klinker; flache Dächer grau. Am Kampener Leuchtturm (bis {@link #WAERTER} m) weiß mit grauem Dach.
     */
    public Stil stil(Haus h, double[] kampen) {
        double steil = 0, flaeche = 0, dach = 0;
        for (int t = h.erstes(); t < h.erstes() + h.zahl(); t++) {
            int o = t * 10;
            double ux = tri[o + 4] - tri[o + 1], uy = tri[o + 5] - tri[o + 2], uz = tri[o + 6] - tri[o + 3];
            double vx = tri[o + 7] - tri[o + 1], vy = tri[o + 8] - tri[o + 2], vz = tri[o + 9] - tri[o + 3];
            double nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx, l = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (l < 1e-9) continue;
            if (tri[o] == 0) {
                dach += l / 2;
                steil += l / 2 * Math.toDegrees(Math.acos(Math.min(1, Math.abs(ny) / l)));
                flaeche += Math.abs(ny) / 2;
            }
        }
        double neigung = dach > 0 ? steil / dach : 0;
        int k = kennzahl(h.id());
        double z1 = (k & 63) / 64.0, z2 = ((k >> 6) & 7) / 8.0 + (k & 7) / 64.0;
        double[] utm = Site.toUtm(h.x(), h.z());
        if (kampen != null && Math.hypot(h.x() - kampen[0], h.z() - kampen[1]) < WAERTER)
            return new Stil(Netz.PUTZ, Netz.DACH_GRAU, true, neigung, flaeche);
        if (neigung < 12) {
            int w = z1 < 0.6 ? Netz.PUTZ : z1 < 0.85 ? Netz.KLINKER : Netz.BACKSTEIN;
            return new Stil(w, Netz.DACH_GRAU, true, neigung, flaeche);
        }
        boolean reet = neigung >= 38 && flaeche < 420 && h.hoeheUeberGrund() < 11.5 && z1 < reetAnteil(utm[0], utm[1]);
        if (reet) return new Stil(z2 < 0.75 ? Netz.BACKSTEIN : Netz.PUTZ, Netz.REET, true, neigung, flaeche);
        int w = z2 < 0.40 ? Netz.PUTZ : z2 < 0.78 ? Netz.BACKSTEIN : z2 < 0.92 ? Netz.KLINKER : Netz.PUTZ;
        boolean rot = (w == Netz.PUTZ ? z1 > 0.7 : z1 > 0.4);
        return new Stil(w, rot ? Netz.DACH_ROT : Netz.DACH_GRAU, true, neigung, flaeche);
    }

    /** Umkreis der Wärterhäuser um den Kampener Leuchtturm (m): weiß, graues Dach. */
    public static final double WAERTER = 80;

    /**
     * Das Netz aller Häuser mit ihren Baustoffen (Böden aus LOD2 bleiben weg): Dächer mit der Normale nach oben, Wände nach
     * außen (von der Mitte des Hauses weg). {@code zaehler}: Häuser je Dach (Reet, grau, rot).
     */
    public Netz netz(double[] kampen, int[] zaehler) {
        Netz nz = new Netz();
        for (Haus h : haeuser) {
            Stil s = stil(h, kampen);
            int k = kennzahl(h.id());
            if (zaehler != null) zaehler[s.dach() == Netz.REET ? 0 : s.dach() == Netz.DACH_GRAU ? 1 : 2]++;
            int mDach = Netz.merkmal(s.dach(), false, k, h.grund()), mWand = Netz.merkmal(s.wand(), s.fenster(), k, h.grund());
            for (int t = h.erstes(); t < h.erstes() + h.zahl(); t++) {
                int o = t * 10;
                int art = (int) tri[o];
                if (art == 2) continue;
                double[] a = {tri[o + 1], tri[o + 2], tri[o + 3]}, b = {tri[o + 4], tri[o + 5], tri[o + 6]}, c = {tri[o + 7], tri[o + 8], tri[o + 9]};
                double ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2], vx = c[0] - a[0], vy = c[1] - a[1], vz = c[2] - a[2];
                double nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nzz = ux * vy - uy * vx;
                if (nx * nx + ny * ny + nzz * nzz < 1e-12) continue;
                boolean flip;
                if (art == 0) flip = ny < 0;
                else {
                    double mx = (a[0] + b[0] + c[0]) / 3 - h.x(), mz = (a[2] + b[2] + c[2]) / 3 - h.z();
                    flip = nx * mx + nzz * mz < 0;
                }
                if (flip) { nx = -nx; ny = -ny; nzz = -nzz; double[] tt = b; b = c; c = tt; }
                nz.dreieck(a, b, c, new double[]{nx, ny, nzz}, art == 0 ? mDach : mWand);
            }
        }
        return nz;
    }

    /** „15 160 Häuser, 545 000 Dreiecke (LOD2), 2 Türme ausgelassen“ */
    public String describe() {
        return String.format(Locale.GERMAN, "%,d Häuser, %,d Dreiecke (LOD2)%s", haeuser.size(), dreiecke(),
                ausgelassen > 0 ? String.format(Locale.GERMAN, ", %d an den Türmen ausgelassen", ausgelassen) : "");
    }
}
