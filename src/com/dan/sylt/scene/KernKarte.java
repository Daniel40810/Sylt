package com.dan.sylt.scene;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;

/**
 * Die Karte des Kerns zu 1 m aus der Aufbereitung (Phase 2, {@code daten/kern/klasse.u8}) und die Farben je Art, seit
 * Phase 3 am Luftbild geeicht ({@code daten/kern/farben.properties}, F5 A: das Luftbild selbst wird nicht gezeichnet).
 * <p>
 * Eichung [G]: Das DOP20 ist kein Maß der Rückstrahlung (bearbeitet, entsättigt, im Dunst), aber die Helligkeit der Arten
 * gegeneinander stimmt. Darum: Albedo der Art = 0,40 (trockener Sand, Messwerte 0,35 bis 0,45) · (Leuchtdichte der Art im
 * DOP / Leuchtdichte des Strands im DOP)^0,75; der Exponent dämpft, weil die Bearbeitung die Unterschiede spreizt. Der
 * Farbton kommt aus den Vorlagen (RotesKliff1–5, Kampen1–4) und hat dieselbe Leuchtdichte. Ohne Luftbild gelten die
 * Werte aus Phase 1.
 */
public final class KernKarte {
    /** Albedo des trockenen Strands (linear), an der die übrigen hängen [G nach Messwerten 0,35–0,45]. */
    public static final double SAND_ALBEDO = 0.40, EXPONENT = 0.75;
    /** Farbton je Art (sRGB) [G nach den Vorlagen]; Index wie {@link Terrain#ART}. */
    static final double[][] TON = {
            {0.55, 0.55, 0.45}, {0.58, 0.53, 0.42}, {0.42, 0.39, 0.33}, {0.76, 0.67, 0.52}, {0.56, 0.49, 0.38}, {0.66, 0.36, 0.20},
            {0.74, 0.67, 0.53}, {0.36, 0.32, 0.23}, {0.24, 0.31, 0.18}, {0.45, 0.55, 0.30}, {0.50, 0.46, 0.38}, {0.60, 0.58, 0.53},
            {0.60, 0.59, 0.44}, {0.44, 0.32, 0.26}};
    /** Albedo je Art ohne Luftbild (Phase 1) und für Arten, die das Luftbild nicht trägt (See: Grund unter Wasser, Kliff, Siedlung). */
    static final double[] ALBEDO_OHNE = {0.20, 0.25, 0.14, 0.40, 0.22, 0.24, 0.38, 0.09, 0.07, 0.15, 0.18, 0.26, 0.24, 0.13};

    /** Art je Kernzelle (1 m, Zeilen von Nord nach Süd wie {@link Terrain#kern}); null ohne Aufbereitung. */
    public final byte[] klasse;
    /** Albedo je Art, linear, je drei Werte (für den Shader). */
    public final float[] albedo = new float[Terrain.ART.length * 3];
    /** Woher die Farben kommen (für das HUD). */
    public final String eichung;

    private KernKarte(byte[] klasse, float[] alb, String eichung) {
        this.klasse = klasse;
        System.arraycopy(alb, 0, albedo, 0, albedo.length);
        this.eichung = eichung;
    }

    /** Liest {@code daten/kern} (fehlt es, ohne Karte mit den Farben von Phase 1). */
    public static KernKarte load(File daten) {
        File kd = new File(daten, "kern");
        byte[] k = null;
        Properties fp = null;
        try {
            File kf = new File(kd, "klasse.u8");
            if (kf.isFile()) {
                byte[] b = Files.readAllBytes(kf.toPath());
                if (b.length == Terrain.KN * Terrain.KN) k = b;
            }
            File pf = new File(kd, "farben.properties");
            if (pf.isFile()) {
                fp = new Properties();
                try (InputStreamReader r = new InputStreamReader(new FileInputStream(pf), StandardCharsets.UTF_8)) { fp.load(r); }
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Kernkarte: " + e.getMessage());
        }
        return new KernKarte(k, albedo(fp), fp == null ? "Farben aus Phase 1 (kein Luftbild)" : "Farben am DOP20 geeicht (Sand 0,40)");
    }

    /** Albedo je Art aus den Farben des Luftbilds (oder ohne). */
    public static float[] albedo(Properties fp) {
        int n = Terrain.ART.length;
        float[] out = new float[n * 3];
        double sandL = fp == null ? Double.NaN : lum(farbe(fp, Terrain.STRAND));
        for (int c = 0; c < n; c++) {
            double l = ALBEDO_OHNE[c];
            boolean ausBild = c != Terrain.KEINE && c != Terrain.SEE && c != Terrain.KLIFF && c != Terrain.SIEDLUNG;
            double[] f = fp == null ? null : farbe(fp, c);
            if (ausBild && f != null && sandL > 0) l = SAND_ALBEDO * Math.pow(lum(f) / sandL, EXPONENT);
            double[] ton = lin(TON[c]);
            double tl = Math.max(1e-6, 0.2126 * ton[0] + 0.7152 * ton[1] + 0.0722 * ton[2]);
            for (int i = 0; i < 3; i++) out[c * 3 + i] = (float) Math.min(0.95, ton[i] / tl * l);
        }
        return out;
    }

    /** Leuchtdichte (linear) einer sRGB-Farbe. */
    static double lum(double[] srgb) {
        double[] l = lin(srgb);
        return 0.2126 * l[0] + 0.7152 * l[1] + 0.0722 * l[2];
    }

    static double[] lin(double[] s) {
        double[] o = new double[3];
        for (int i = 0; i < 3; i++) o[i] = s[i] <= 0.04045 ? s[i] / 12.92 : Math.pow((s[i] + 0.055) / 1.055, 2.4);
        return o;
    }

    static double[] farbe(Properties p, int c) {
        String v = p.getProperty("farbe." + c);
        if (v == null) return null;
        String[] a = v.trim().split("\\s+");
        return new double[]{Double.parseDouble(a[0]), Double.parseDouble(a[1]), Double.parseDouble(a[2])};
    }

    public boolean hatKarte() { return klasse != null; }

    /** Art am Ort (x, z): im Kern aus der Karte zu 1 m, sonst die Art des Hauptrasters. */
    public byte at(Terrain t, double x, double z) {
        if (klasse != null) {
            int i = (int) Math.floor(x - Terrain.KX0), j = (int) Math.floor(z - Terrain.KZ0);
            if (i >= 0 && j >= 0 && i < Terrain.KN && j < Terrain.KN) return klasse[j * Terrain.KN + i];
        }
        return t.artAt(x, z);
    }

    /** Albedo (Leuchtdichte) einer Art. */
    public double albedoLum(int c) { return 0.2126 * albedo[c * 3] + 0.7152 * albedo[c * 3 + 1] + 0.0722 * albedo[c * 3 + 2]; }

    /** „Strand 0,40 · Heide 0,10 · Wiese 0,11 …“ */
    public String describe() {
        StringBuilder b = new StringBuilder(eichung).append(':');
        for (int c : new int[]{Terrain.STRAND, Terrain.NASS, Terrain.DUENE, Terrain.STRANDHAFER, Terrain.HEIDE, Terrain.GEBUESCH, Terrain.WIESE, Terrain.DACH})
            b.append(' ').append(Terrain.ART[c]).append(String.format(Locale.GERMAN, " %.2f", albedoLum(c)));
        return b.toString();
    }
}
