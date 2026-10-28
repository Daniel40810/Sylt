package com.dan.sylt.scene;

import java.util.Locale;

/**
 * Seit Phase 15 („Die Vorspülung“ aus dem Ideenspeicher): ein Sandkörper, den ein Spülrohr im Frühjahr vor das Kliff
 * legt; danach trägt ihn die Brandung ab. Seit Ende der 1970er-Jahre wird vor dem Roten Kliff fast jedes Jahr Sand
 * vorgespült [Wikipedia: Rotes Kliff]. Hier gestaltet [G]: auf 600 m längs (N 6 089 250 bis 6 089 850) vom Kliffuß
 * 70 m seewärts, am Fuß {@link #HOEHE} m hoch, zur See auf null auslaufend, an den Enden auf 60 m. Aufgespült in
 * {@link #AUFBAU} Stunden Szenenzeit (gerafft: in Wirklichkeit Wochen), dann trägt ihn die Brandung ab, umso schneller,
 * je höher die See: die Höhe halbiert sich bei H_s 1,2 m in 60 Tagen, bei 2,4 m in 15 Tagen.
 * <p>
 * Der Körper geht in die Änderung des Geländes ({@link Aenderung}, wie die Sturzkegel), so sehen ihn Bild, Brandung
 * und Sand. Die App ruft {@link #schritt} je Bild mit der Uhr der Szene.
 */
public final class Vorspuelung {
    public static final double NORD0 = 6_089_250, NORD1 = 6_089_850, BREITE = 70, HOEHE = 2.5, ENDEN = 60, AUFBAU = 72;

    private final Kante kante;
    private final Aenderung a;
    private float[] ist;
    private double startMs = Double.NaN, letzteMs = Double.NaN, amplitude = 1;
    private double takt;
    /** Volumen des Körpers jetzt (m³), wie weit er aufgespült ist (0..1). */
    public volatile double volumen, aufbau;
    public volatile boolean aktiv;

    public Vorspuelung(Kante kante, Aenderung a) { this.kante = kante; this.a = a; }

    /** Die Vorspülung beginnt jetzt (ein schon liegender Körper bleibt und wächst wieder auf). */
    public void starten(double epochMs) {
        if (ist == null) ist = new float[a.nx * a.nz];
        startMs = epochMs;
        letzteMs = epochMs;
        amplitude = 1;
        aktiv = true;
    }

    /** Ob der Körper fertig aufgespült ist. */
    public boolean fertig() { return aktiv && aufbau >= 0.999; }

    /** Je Bild: Aufbau nach der Uhr, Abtrag nach der See (H_s), höchstens alle 0,4 s neu in die Änderung. */
    public void schritt(double dt, double epochMs, double hs) {
        if (!aktiv || ist == null) return;
        takt -= dt;
        double stunden = (epochMs - startMs) / 3_600_000.0, seit = Double.isNaN(letzteMs) ? 0 : (epochMs - letzteMs) / 3_600_000.0;
        letzteMs = epochMs;
        double g = Math.max(0, Math.min(1, stunden / AUFBAU));
        aufbau = g;
        if (g >= 1 && seit > 0) {
            double halb = 60 * 24 * Math.pow(1.2 / Math.max(0.3, hs), 2);
            amplitude *= Math.pow(0.5, seit / halb);
        }
        if (takt > 0 && seit < 6) return;
        takt = 0.4;
        double soll0 = HOEHE * Math.min(1, g * 1.15) * amplitude;
        double v = 0;
        for (int j = 0; j < a.nz; j++) {
            double z = a.cz(j), nord = Site.N0 - z;
            if (nord < NORD0 || nord > NORD1) continue;
            int r = kante.rowNear(z, 60);
            if (r < 0) continue;
            double ende = Math.min(1, Math.min(nord - NORD0, NORD1 - nord) / ENDEN);
            ende = ende * ende * (3 - 2 * ende);
            double fuss = kante.fussX(r);
            // beim Aufbau wächst das Feld vom Fuß zur See (das Rohr liegt am Fuß)
            double reicht = BREITE * Math.min(1, 0.3 + g);
            for (int i = a.i(fuss - BREITE - 2); i <= a.i(fuss + 1); i++) {
                if (i < 0 || i >= a.nx) continue;
                double vor = fuss - a.cx(i);   // m vor dem Fuß
                double h = vor < -1 || vor > reicht ? 0 : soll0 * ende * Math.min(1, (vor + 1) / 3) * (1 - Math.max(0, vor) / BREITE);
                int k = j * a.nx + i;
                double d = h - ist[k];
                if (Math.abs(d) > 0.005) { a.add(i, j, d); ist[k] = (float) h; }
                v += ist[k];
            }
        }
        volumen = v * a.cell * a.cell;
        if (amplitude < 0.02 && g >= 1) aktiv = amplitude >= 0.02;
    }

    /** Den Körper wieder aus dem Gelände nehmen (Prüfung). */
    public void entfernen() {
        if (ist == null) return;
        for (int k = 0; k < ist.length; k++) if (ist[k] != 0) { a.add(k % a.nx, k / a.nx, -ist[k]); ist[k] = 0; }
        aktiv = false;
        volumen = 0;
        aufbau = 0;
        ist = null;
    }

    /** Seit Phase 16: Beginn der laufenden Vorspülung (ms seit 1970) oder NaN, Anteil, der noch da ist. */
    public double beginnMs() { return startMs; }
    public double amplitude() { return amplitude; }
    /** Ob ein Körper liegt (auch wenn er schon abgetragen wird). */
    public boolean liegt() { return ist != null; }

    /**
     * Seit Phase 16 („Weiter wie zuletzt“): der Stand als Text (Base64 über Deflate) oder null ohne Körper. Den Körper
     * selbst trägt die {@link Aenderung} (sie kommt mit dem Stand des Kliffs); hier nur, was er weiß, um weiter zu wachsen
     * oder abgetragen zu werden.
     */
    public String stand() {
        float[] f = ist;
        if (f == null) return null;
        try {
            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            try (java.io.DataOutputStream o = new java.io.DataOutputStream(new java.util.zip.DeflaterOutputStream(bo))) {
                o.writeInt(f.length);
                for (double v : new double[]{startMs, letzteMs, amplitude, aufbau, volumen}) o.writeDouble(v);
                o.writeBoolean(aktiv);
                for (float v : f) o.writeFloat(v);
            }
            return java.util.Base64.getEncoder().encodeToString(bo.toByteArray());
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Den Stand herstellen (im Faden der Physik, mit dem Stand des Kliffs); null: keine Vorspülung. */
    public boolean setzen(String text) {
        if (text == null || text.isBlank()) { ist = null; aktiv = false; volumen = 0; aufbau = 0; return true; }
        try (java.io.DataInputStream in = new java.io.DataInputStream(new java.util.zip.InflaterInputStream(
                new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode(text.strip()))))) {
            int n = in.readInt();
            if (n != a.nx * a.nz) return false;
            double[] v = new double[5];
            for (int i = 0; i < 5; i++) v[i] = in.readDouble();
            boolean ak = in.readBoolean();
            float[] f = new float[n];
            for (int i = 0; i < n; i++) f[i] = in.readFloat();
            ist = f;
            startMs = v[0]; letzteMs = v[1]; amplitude = v[2]; aufbau = v[3]; volumen = v[4];
            aktiv = ak;
            return true;
        } catch (java.io.IOException | RuntimeException e) {
            return false;
        }
    }

    /** „Vorspülung: 2,3 m am Fuß, 52 000 m³, aufgespült“ */
    public String describe() {
        if (ist == null) return "Vorspülung: keine";
        return String.format(Locale.GERMAN, "Vorspülung: %.1f m am Fuß, %,.0f m³%s", HOEHE * Math.min(1, aufbau * 1.15) * amplitude, volumen,
                aufbau < 1 ? String.format(Locale.GERMAN, ", Aufbau %.0f %%", aufbau * 100) : amplitude < 0.999 ? String.format(Locale.GERMAN, ", %.0f %% noch da", amplitude * 100) : ", aufgespült");
    }
}
