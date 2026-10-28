package com.dan.sylt.tools;

import com.dan.sylt.camera.Ansichten;
import com.dan.sylt.gl.Klang;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.scene.Kante;
import com.dan.sylt.scene.Kliffabbruch;
import com.dan.sylt.scene.Kueste;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.wiring.Wetterregie;
import com.dan.soundscape.Soundscape;
import com.dan.soundscape.Wav;
import com.dan.soundscape.demo.Analysis;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Phase 14: die Hörproben als WAV mit Spektrogramm nach {@code docs/hoerproben}, ohne Grafikkarte. Die Szene läuft wie
 * im Bildlauf ({@code Renderer.step}: Uhr, Wetter, Tide, Brandung, Sand, Tiere, Abbruch) in Schritten von 1/60 s
 * Echtzeit, der Klang hört an der Kamera zu ({@link Klang} offline, ohne Anpassung des Ohrs, 44,1 kHz). Zum Anhören wird
 * jede Datei auf −1 dBFS Spitze angehoben (höchstens +30 dB); der Bericht nennt die Anhebung und den Pegel am Ohr in
 * dB SPL, so bleiben die Proben vergleichbar.
 * <p>
 * Aufruf: {@code java … com.dan.sylt.tools.Hoerproben [Ordner] [nur]}, etwa {@code docs/hoerproben abbruch}.
 */
public final class Hoerproben {
    static final double DT = 1 / 60.0;
    static final List<String> report = new ArrayList<>();

    public static void main(String[] a) throws Exception {
        File dir = new File(a.length > 0 ? a[0] : "docs/hoerproben");
        String only = a.length > 1 ? a[1].toLowerCase(Locale.ROOT) : "";
        dir.mkdirs();
        SceneClock clock = new SceneClock(LocalDate.of(2026, 9, 12), 18.5);
        clock.lapse = 1;
        Renderer r = new Renderer(clock);
        Klang k = r.klang;
        k.offline = true;
        k.s.autoGain = false;
        k.s.volume = 1;
        k.s.quality = Soundscape.Quality.HIGH;
        long t0 = System.nanoTime();
        if (only.isEmpty() || only.contains("saum")) saum(r, clock, dir);
        if (only.isEmpty() || only.contains("kliffuss")) kliffuss(r, clock, dir);
        if (only.isEmpty() || only.contains("kante")) kante(r, clock, dir);
        if (only.isEmpty() || only.contains("heide")) heide(r, clock, dir);
        if (only.isEmpty() || only.contains("sand")) sandsturm(r, clock, dir);
        if (only.isEmpty() || only.contains("gewitter")) gewitter(r, clock, dir);
        if (only.isEmpty() || only.contains("abbruch")) abbruch(r, clock, dir);   // zuletzt: verändert das Kliff
        Files.writeString(new File(dir, "hoerproben.txt").toPath(), String.join(System.lineSeparator(), report) + System.lineSeparator(), StandardCharsets.UTF_8);
        System.out.printf(Locale.GERMAN, "FERTIG in %.0f s%n", (System.nanoTime() - t0) / 1e9);
        System.exit(0);
    }

    // ------------------------------------------------------------------ die Proben

    /** Ein Septemberabend am Spülsaum: Schwallbrecher, der Schwall vor den Füßen, Sanderlinge, Möwen. */
    static void saum(Renderer r, SceneClock c, File dir) throws Exception {
        c.set(LocalDate.of(2026, 9, 12), 18.5);
        r.wiring.regie.lage(Wetterregie.Lage.SOMMERABEND);
        r.placeView(Ansichten.byName("Spülsaum"));
        rueck(r, 6, 200, 2);
        record(r, dir, "1_Abend_am_Spuelsaum", "Abend am Spülsaum: 12.9.2026 18:30, Lage Sommerabend (See 1 m, 8 s, Wind 6 m/s aus West), am Wasser, Blick die Küste entlang nach Süden", 40);
    }

    /** Westwind am Kliffuß: die Brandung mit 2 m, Sturzbrecher auf dem Riff, der Schwall bis an den Strand. */
    static void kliffuss(Renderer r, SceneClock c, File dir) throws Exception {
        c.set(LocalDate.of(2026, 10, 3), 14);
        r.wiring.regie.lage(Wetterregie.Lage.WESTWIND);
        r.placeView(Ansichten.byName("Spülsaum"));
        rueck(r, 18, 285, 1);
        record(r, dir, "2_Westwind_am_Strand", "Westwind am Strand: 3.10.2026 14 Uhr, Lage Westwind (See 2 m, 7 s, Wind 11 m/s aus 260°), 18 m vom Wasser, Blick auf die See", 40);
    }

    /** Oben an der Kliffkante bei Westwind: der Wind über der Kante, Strandhafer, Möwen im Aufwind, die Brandung von unten. */
    static void kante(Renderer r, SceneClock c, File dir) throws Exception {
        c.set(LocalDate.of(2026, 10, 3), 14.2);
        r.wiring.regie.lage(Wetterregie.Lage.WESTWIND);
        r.placeView(Ansichten.byName("Kliffkante"));
        record(r, dir, "3_Kliffkante_bei_Westwind", "Oben an der Kliffkante bei Westwind (11 m/s): Wind über der Kante, Strandhafer, Möwen im Aufwind, unten die Brandung", 40);
    }

    /** An der Uwe-Düne, gut 1 km landeinwärts: die Brandung nur noch als Rollen. */
    static void heide(Renderer r, SceneClock c, File dir) throws Exception {
        c.set(LocalDate.of(2026, 10, 3), 14.4);
        r.wiring.regie.lage(Wetterregie.Lage.WESTWIND);
        r.placeView(Ansichten.byName("Uwe-Düne"));
        record(r, dir, "4_Uwe-Duene", String.format(Locale.GERMAN, "Auf der Uwe-Düne (%.0f m von der Wasserlinie), Westwind 11 m/s: die Brandung nur noch als Rollen", -Kueste.offshore(r.camera.eye[0], r.camera.eye[2])), 30);
    }

    /** Sandsturm am Strand: Bft 9 aus West, trocken, Flugsand am Ohr. */
    static void sandsturm(Renderer r, SceneClock c, File dir) throws Exception {
        c.set(LocalDate.of(2026, 10, 3), 13);
        r.sandsturm();
        r.brandungJetzt();
        r.sandJetzt();
        record(r, dir, "5_Sandsturm_am_Strand", String.format(Locale.GERMAN, "Sandsturm am Strand: Bft 9 aus West (%.0f m/s), trocken, oben am Strand, Flugsand prasselt", r.wiring.windSpeed), 30);
    }

    /** Gewitter über der See, vom Strand am Kliff: zwei Blitze über der See, Regen. */
    static void gewitter(Renderer r, SceneClock c, File dir) throws Exception {
        c.set(LocalDate.of(2026, 8, 2), 21.5);
        r.wiring.regie.lage(Wetterregie.Lage.GEWITTER);
        r.placeView(Ansichten.byName("Am Kliff"));
        r.camera.place(r.camera.eye[0], r.camera.eye[1], r.camera.eye[2], 285, 0);
        record(r, dir, "6_Gewitter_ueber_der_See", "Gewitter über der See: 2.8.2026 21:30, Lage Gewitter (Regen, Wind 9 m/s), am Strand vor dem Kliff, Blick auf die See, Blitze über der See nach 4 und 24 s", 45,
                new double[]{4, 24});
    }

    /** Der Abbruch: Sturmflut nach Xaver, Vormittag danach, das Stück bei N 6 090 335 bricht. */
    static void abbruch(Renderer r, SceneClock c, File dir) throws Exception {
        Kliffabbruch k = r.abbruch;
        if (k == null) { report.add("Abbruch: keine Kante"); return; }
        k.manuell = true;
        k.asyncSolve = false;
        r.abbruchVorbereiten(Abbruch.STUNDE, Abbruch.NORD);
        Kliffabbruch.Schnitt s = k.schnitt();
        if (s == null) { report.add("Abbruch: kein Stück gerechnet"); return; }
        Kante kn = k.kante;
        int m = s.mitte;
        double fx = kn.fussX(m), fz = kn.z(m), hf = kn.hFuss[m], hk = kn.hKante[m];
        double ex = fx - 28, ez = fz + 20, tx = (fx + kn.kanteX(m)) / 2, ty = (hf + hk) / 2;
        double yaw = (Math.toDegrees(Math.atan2(tx - ex, -(fz - ez))) + 360) % 360;
        r.camera.place(ex, Math.max(r.groundHeight(ex, ez), r.wiring.pegel) + 1.7, ez, yaw, 0);
        r.brandungJetzt();
        r.sandJetzt();
        record(r, dir, "7_Abbruch_bei_Xaver", String.format(Locale.GERMAN,
                "Der Abbruch: Sturmflut nach Xaver, 6.12.2013 11 Uhr (Pegel %+.1f m NHN, Wind %.0f m/s), am Strand 35 m vor dem Stück bei N %,.0f; nach 6 s bricht das Kliff",
                r.wiring.pegel, r.wiring.windSpeed, kn.nord[m]), 40, null, 6.0);
        k.rechnen = false;
    }

    // ------------------------------------------------------------------ Hilfen

    /** Die Ansicht drehen (Blick dir, Neigung pitch) und das Ohr m Meter landwärts rücken, in 1,6 m über dem Grund. */
    static void rueck(Renderer r, double m, double dir, double pitch) {
        double x = r.camera.eye[0] + Kueste.SX * m, z = r.camera.eye[2] + Kueste.SZ * m;
        r.camera.place(x, Math.max(r.groundHeight(x, z), r.wiring.pegel) + 1.6, z, dir, pitch);
    }

    static void record(Renderer r, File dir, String name, String what, double sec) throws Exception { record(r, dir, name, what, sec, null, -1); }

    static void record(Renderer r, File dir, String name, String what, double sec, double[] blitze) throws Exception { record(r, dir, name, what, sec, blitze, -1); }

    /**
     * Eine Probe: Brandung und Sand jetzt, 8 s Vorlauf (verworfen; Tiere, Wellen und Schwall stellen sich ein), dann sec
     * Sekunden; WAV, Spektrogramm, Zeile im Bericht. blitze: Zeiten (s) für einen Blitz über der See; abbruchAb: ab dann
     * rechnet die Physik des Kliffs (s, −1 = nicht).
     */
    static void record(Renderer r, File dir, String name, String what, double sec, double[] blitze, double abbruchAb) throws Exception {
        Klang k = r.klang;
        Soundscape s = k.s;
        r.brandungJetzt();
        int sr = s.sampleRate();
        int[] c0 = counts(k);
        double[] lv = new double[Soundscape.PARTS.length];
        int nLv = 0;
        float[] all = new float[(int) Math.round(sec * sr) * 2 + 8192];
        int pos = 0;
        long t0 = System.nanoTime();
        int pre = (int) Math.round(8 / DT), frames = pre + (int) Math.round(sec / DT);
        int bi = 0;
        for (int i = 0; i < frames; i++) {
            double t = (i - pre) * DT;
            if (blitze != null && bi < blitze.length && t >= blitze[bi]) { r.unwetter.blitzVonHand(r.camera.eye, r.camera.fwd, false); bi++; }
            if (abbruchAb >= 0 && t >= abbruchAb && r.abbruch != null) { r.abbruch.physik(1 / 120.0); r.abbruch.physik(1 / 120.0); }
            r.step(DT);
            if (i == pre) c0 = counts(k);
            float[] o = s.render(DT);
            if (i >= pre) {
                int n = Math.min(o.length, all.length - pos);
                System.arraycopy(o, 0, all, pos, n);
                pos += n;
                double[] l = s.levels();
                for (int q = 0; q < lv.length; q++) lv[q] += Math.pow(10, l[q] / 10);
                nLv++;
            }
        }
        float[] out = java.util.Arrays.copyOf(all, pos);
        float peak = 1e-9f;
        for (float v : out) peak = Math.max(peak, Math.abs(v));
        double gainDb = Math.min(30, -1 - 20 * Math.log10(peak));
        float gl = (float) Math.pow(10, gainDb / 20);
        float[] loud = out.clone();
        for (int q = 0; q < loud.length; q++) loud[q] *= gl;
        Wav.write(new File(dir, name + ".wav"), loud, sr);
        double rms = Analysis.rms(out, sr, 0, pos / 2.0 / sr);
        StringBuilder parts = new StringBuilder();
        for (int q = 0; q < lv.length - 1; q++) {
            double db = 10 * Math.log10(lv[q] / Math.max(1, nLv) + 1e-30);
            if (db > 25) parts.append(String.format(Locale.GERMAN, "%s %.0f · ", Soundscape.PARTS[q], db));
        }
        int[] c1 = counts(k);
        String line = String.format(Locale.GERMAN, "%s (zum Anhören um %+.0f dB angehoben)%n  %s%n  am Ohr %.0f dB SPL (Mittel) · %s%n"
                        + "  Wellen: %d Schwallbrecher, %d Sturzbrecher, %d Schwälle · Rufe: %d Möwen, %d Sanderlinge, %d Austernfischer, %d Seehund, %d Kuh · %d Donner · Abbruch %d, Brocken %d, Rieseln %d%n"
                        + "  Brandung fern %.0f dB (Linie %d Stücke, nächstes %.0f m, %.0f W/m vergehen gegenüber, Sturzbrecher %.0f %%, über die Kante %.0f dB) · See %.0f %% · Sand %.3f kg/(m·s) · Strandhafer %.0f %% · gerechnet in %.1f s",
                name + ".wav", gainDb, what, 20 * Math.log10(rms + 1e-12) + Soundscape.REF_DB, parts.length() > 3 ? parts.substring(0, parts.length() - 3) : "—",
                c1[0] - c0[0], c1[1] - c0[1], c1[2] - c0[2], c1[3] - c0[3], c1[4] - c0[4], c1[5] - c0[5], c1[6] - c0[6], c1[7] - c0[7], c1[8] - c0[8], c1[9] - c0[9], c1[10] - c0[10], c1[11] - c0[11],
                s.surfLineDb(), k.stuecke, s.surfLineNearest(), k.wattsHier, k.sturzAnteil * 100, k.kanteDb, k.seeAnteil * 100, s.sandFlux, s.marramNear * 100,
                (System.nanoTime() - t0) / 1e9);
        System.out.println(line);
        report.add(line);
        spectrogram(new File(dir, name + ".png"), out, sr, what);
    }

    static int[] counts(Klang k) {
        return new int[]{k.schwallbrecher, k.sturzbrecher, k.schwaelle, k.moewen, k.sanderlinge, k.austernfischer, k.seehunde, k.kuehe, k.donner, k.abbrueche, k.brocken, k.rieseln};
    }

    static void spectrogram(File png, float[] st, int sr, String what) throws Exception {
        int W = 1600, H = 520, top = 56, left = 70;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(14, 18, 24));
        g.fillRect(0, 0, W, H);
        double lvl = 20 * Math.log10(Analysis.rms(st, sr, 0, st.length / 2.0 / sr) + 1e-9);
        Analysis.spectrogram(g, st, sr, left, top, W - left - 20, H - top - 70, lvl - 30);
        g.setColor(new Color(225, 230, 235));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        g.drawString(what.length() > 175 ? what.substring(0, 172) + " …" : what, left, 24);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        double fMin = 30, fMax = Math.min(16_000, sr / 2.0);
        int ph = H - top - 70;
        for (double f : new double[]{50, 100, 200, 500, 1000, 2000, 5000, 10000}) {
            int y = top + (int) ((ph - 1) * (1 - Math.log(f / fMin) / Math.log(fMax / fMin)));
            g.drawString(f >= 1000 ? (int) (f / 1000) + " kHz" : (int) f + " Hz", 8, y + 4);
        }
        double dur = st.length / 2.0 / sr;
        for (int s = 0; s <= (int) dur; s += 5) {
            int x = left + (int) ((W - left - 20) * s / dur);
            g.drawString(s + " s", x - 8, H - 50);
        }
        g.setColor(new Color(255, 210, 90));
        int px = -1, py = 0;
        for (double t = 0; t < dur - 0.25; t += 0.25) {
            double db = 20 * Math.log10(Analysis.rms(st, sr, t, 0.25) + 1e-9);
            int x = left + (int) ((W - left - 20) * t / dur), y = H - 8 - (int) Math.max(0, Math.min(30, (db + 70) / 70 * 30));
            if (px >= 0) g.drawLine(px, py, x, y);
            px = x; py = y;
        }
        g.dispose();
        ImageIO.write(img, "png", png);
    }
}
