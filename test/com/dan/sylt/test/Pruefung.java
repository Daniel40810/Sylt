package com.dan.sylt.test;

import com.dan.sylt.app.Bibliothek;
import com.dan.sylt.camera.Ansichten;
import com.dan.sylt.camera.Camera;
import com.dan.sylt.gl.Mat4;
import com.dan.sylt.gl.Quality;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.gl.TerrainLod;
import com.dan.sylt.scene.Buehne;
import com.dan.sylt.scene.Geo;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Sun;
import com.dan.sylt.scene.Terrain;
import com.dan.sylt.wiring.Wiring;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Prüfung der App ohne Grafikkarte und ohne Datenbank (Ant-Target {@code pruefung}), Stand Phase 1: Raum und Orte,
 * die Bühne gegen die Profile der BAW, das Gelände (16-Bit-Werte, Raster, Höhe gleich Bühne), die Auswahl der
 * Detailstufen, Ansichten und Kamera, Uhr und Sonne am Start, die Verdrahtung der Hüllen von surf und dune, Renderer.jar
 * mit den neuen Packages, die Shader-Dateien und die Regel aus dem Werkbuch 2.4 (kein Laufzeitcode liest C:\Daten).
 * Endet mit „ERGEBNIS: alles in Ordnung“ oder der Zahl der Fehler (Rückgabewert 1).
 */
public final class Pruefung {
    private static int bad, n;

    public static void main(String[] args) throws Exception {
        long t0 = System.nanoTime();
        System.out.println("Sylt " + Renderer.VERSION + " · Prüfung ohne Grafikkarte");

        teil("Raum und Orte (F2 A)");
        double[] q = Site.QUERMARKE.x() == 0 ? new double[2] : new double[]{Site.QUERMARKE.x(), Site.QUERMARKE.z()};
        check(Math.hypot(q[0], q[1]) < 30, f("Ursprung am Quermarkenfeuer: Turm bei x %.1f, z %.1f m", q[0], q[1]));
        check(Math.abs(Site.QUERMARKE.e() - 457_605) < 30 && Math.abs(Site.QUERMARKE.n() - 6_091_171) < 30,
                f("Quermarkenfeuer aus 54°57′56″ N 8°20′16″ O: E %.0f N %.0f (Prompt ≈ 457 605 / 6 091 171)", Site.QUERMARKE.e(), Site.QUERMARKE.n()));
        check(Math.abs(Site.LEUCHTTURM.e() - 457_778) < 30 && Math.abs(Site.LEUCHTTURM.n() - 6_089_003) < 30,
                f("Kampener Leuchtturm aus 54°56′46″ N 8°20′27″ O: E %.0f N %.0f (Prompt ≈ 457 778 / 6 089 003)", Site.LEUCHTTURM.e(), Site.LEUCHTTURM.n()));
        double[] ll = Geo.UTM32N.inverse(457_600, 6_091_170), en = Geo.UTM32N.forward(ll[0], ll[1]);
        check(Math.abs(en[0] - 457_600) < 0.001 && Math.abs(en[1] - 6_091_170) < 0.001, "UTM hin und zurück auf 1 mm");
        for (Site.Ort o : new Site.Ort[]{Site.QUERMARKE, Site.LEUCHTTURM, Site.UWE_DUENE, Site.KLIFFKANTE})
            check(Site.KERN.contains(o.x(), o.z()), o.name() + " liegt im Kern");
        double[] k = Site.KERN.scene();
        check(Math.abs((k[2] - k[0]) - 2300) < 1e-6 && Math.abs((k[3] - k[1]) - 3600) < 1e-6, "Kern 2,3 × 3,6 km (F1 A)");
        check(Site.FERNFELD.breite() == 16_000 && Site.FERNFELD.hoehe() == 16_000, "Fernfeld 16 × 16 km (F1 A)");
        check(Math.abs(Site.horizon(30) / 1000 - 21.0) < 0.6, f("Kimm aus 30 m: %.1f km", Site.horizon(30) / 1000));

        teil("Bühne gegen die Profile der BAW (Werkbuch 2.3)");
        double[][] profil = {{6_088_000, 456_000}, {6_089_500, 456_650}, {6_091_000, 457_200}, {6_092_500, 457_900}};
        for (double[] p : profil) {
            double[] xz = Site.fromUtm(p[1], p[0]);
            check(Math.abs(Buehne.height(xz[0], xz[1])) < 0.6, f("Wasserlinie bei N %.0f, E %.0f: %.2f m NHN", p[0], p[1], Buehne.height(xz[0], xz[1])));
            double[] see = Site.fromUtm(p[1] - 8000, p[0]);
            double hs = Buehne.height(see[0], see[1]);
            check(hs < -12.5 && hs > -14.5, f("8 km draußen bei N %.0f: %.1f m (BAW −13,2 bis −14,1 m)", p[0], hs));
        }
        double[] riff = Site.fromUtm(Site.wasserlinieE(6_088_000) - 450, 6_088_000);
        double hr = Buehne.height(riff[0], riff[1]);
        check(hr > -3.0 && hr < -1.0, f("Riff 450 m vor dem Strand: %.1f m (BAW bei Wenningstedt −2,2 m)", hr));
        double top = maxQuer(6_089_500, 60, 200);
        check(top > 24 && top < 34, f("Kliffoberkante bei N 6 089 500: %.1f m (BAW +23 bis +32 m)", top));
        double top2 = maxQuer(6_088_000, 60, 120);
        check(top2 > 16 && top2 < 23, f("Kliffoberkante bei N 6 088 000: %.1f m (BAW +17 bis +21 m)", top2));
        double steil = 0;
        for (double s = 55; s < 80; s += 0.5) {
            double[] a = Site.fromUtm(Site.wasserlinieE(6_089_500) + s, 6_089_500), b = Site.fromUtm(Site.wasserlinieE(6_089_500) + s + 0.5, 6_089_500);
            steil = Math.max(steil, (Buehne.height(b[0], b[1]) - Buehne.height(a[0], a[1])) / 0.5);
        }
        check(steil > 1.2, f("die Wand ist steil: bis %.1f m je m (über 50°)", steil));
        check(Math.abs(Buehne.height(Site.UWE_DUENE.x(), Site.UWE_DUENE.z()) - 52.5) < 6,
                f("Uwe-Düne: %.1f m (52,5 m)", Buehne.height(Site.UWE_DUENE.x(), Site.UWE_DUENE.z())));
        double[] watt = Site.fromUtm(Site.wasserlinieE(6_090_000) + 4000, 6_090_000);
        check(Buehne.height(watt[0], watt[1]) < 0, f("Watt im Osten: %.1f m", Buehne.height(watt[0], watt[1])));
        double[] nord = Site.fromUtm(459_000, 6_110_000);
        check(Buehne.height(nord[0], nord[1]) < 0, "nördlich der Insel See");

        teil("Gelände");
        check(Terrain.decode(Terrain.encode(0)) == 0, "NHN genau ein 16-Bit-Wert");
        check(Math.abs(Terrain.decode(Terrain.encode(52.5)) - 52.5) < 0.001 && Math.abs(Terrain.decode(Terrain.encode(-13.7)) + 13.7) < 0.001, "Stufe unter 1 mm");
        check(Terrain.N * Terrain.CELL >= Site.FERNFELD.breite() && Terrain.KN * Terrain.KCELL >= Site.KERN.hoehe(), "Raster decken Fernfeld und Kern");
        check(Math.abs(Terrain.FERN_STUFE - Math.log(10) / Math.log(2)) < 1e-9 && Math.abs(Terrain.KERN_STUFE - Math.log(10) / Math.log(2)) < 1e-9,
                "Mip-Stufen für Fernring und Kern: log2(10)");
        long tt = System.nanoTime();
        Terrain t = Terrain.ausBuehne();
        System.out.printf(Locale.GERMAN, "    Bühne gerechnet in %.1f s: %s%n", (System.nanoTime() - tt) / 1e9, t.describe());
        check(!t.measured() && t.hasKern(), "ohne daten/gelaende: Bühne, der Kern aus der Bühne zu 1 m");
        double kw = 0;
        java.util.Random rnd0 = new java.util.Random(11);
        for (int i = 0; i < 2000; i++) {
            int ci = 200 + rnd0.nextInt(Terrain.KN - 400), cj = 200 + rnd0.nextInt(Terrain.KN - 400);
            double x = Terrain.kcx(ci), z = Terrain.kcz(cj);
            kw = Math.max(kw, Math.abs(t.height(x, z) - Buehne.height(x, z)));
        }
        check(kw < 0.01, f("Kern trifft die Bühne an den Zellmitten auf %.4f m", kw));
        double wand = 0;
        for (double s = 55; s < 80; s += 1) {
            double[] a = Site.fromUtm(Site.wasserlinieE(6_089_500) + s, 6_089_500), b = Site.fromUtm(Site.wasserlinieE(6_089_500) + s + 1, 6_089_500);
            wand = Math.max(wand, t.height(b[0], b[1]) - t.height(a[0], a[1]));
        }
        check(wand > 1.2, f("die Wand im Gelände: bis %.1f m Anstieg je m (im 10-m-Raster allein wäre sie ein Hang)", wand));
        double worst = 0;
        java.util.Random rnd = new java.util.Random(7);
        for (int i = 0; i < 2000; i++) {
            int ci = 10 + rnd.nextInt(Terrain.N - 20), cj = 10 + rnd.nextInt(Terrain.N - 20);
            double x = Terrain.cx(ci), z = Terrain.cz(cj);
            if (Terrain.fernShare(x, z) > 0 || t.kernShare(x, z) > 0) continue;
            worst = Math.max(worst, Math.abs(t.height(x, z) - Buehne.height(x, z)));
        }
        check(worst < 0.01, f("Gelände trifft die Bühne an den Zellmitten auf %.4f m", worst));
        double[] r2 = new double[2];
        t.range(k[0], k[1], 2300, r2);
        check(r2[0] < -1 && r2[1] > 45, f("Höhenbereich im Kern %.1f bis %.1f m", r2[0], r2[1]));
        double[] sh = t.shares();
        check(sh[Terrain.SEE] > 0.3 && sh[Terrain.KLIFF] > 0 && sh[Terrain.STRAND] > 0 && sh[Terrain.HEIDE] > 0 && sh[Terrain.DUENE] > 0,
                "Arten: See, Strand, Kliff, Düne, Heide vorhanden");

        teil("Detailstufen (CDLOD)");
        TerrainLod lod = new TerrainLod(t);
        Camera cam = new Camera();
        cam.groundLevel = t.height(cam.eye[0], cam.eye[2]);
        cam.snap();
        float[] vp = Mat4.mul(Mat4.perspective(Camera.FOVY, 16 / 9.0, 0.3, 120_000, true), cam.view());
        for (Quality ql : Quality.values()) {
            lod.lodRange = ql.terrainRange;
            lod.finest = ql.terrainFine;
            lod.finestKern = ql.terrainKern;
            lod.collect(cam.eye, vp);
            check(lod.nodes > 20 && lod.nodes < 4000, f("%s: %d Knoten, %,d Punkte", ql.label, lod.nodes, lod.vertices));
        }
        check(TerrainLod.ROOT / (1 << (TerrainLod.LEVELS - 1)) / TerrainLod.DIM == 1.0, "feinste Stufe 1 m Punktabstand (Kern)");

        teil("Ansichten und Kamera");
        for (Ansichten.Ansicht a : Ansichten.ALLE) {
            Camera c = new Camera();
            c.waterLevel = 0;
            Ansichten.apply(a, c, Renderer.PHASE, t::height);
            c.snap();
            double g = Math.max(t.height(c.eye[0], c.eye[2]), 0);
            boolean drin = Site.FERNFELD.contains(c.eye[0], c.eye[2]);
            check(drin && c.eye[1] >= g + Math.min(a.eyeAboveGround(), Camera.MIN_EYE) - 0.05,
                    f("%s: Auge %.1f m über NHN, %.1f m über Grund/Wasser, Blick %03.0f°", a, c.eye[1], c.eye[1] - g, c.heading()));
        }
        Ansichten.Ansicht kl = Ansichten.byName("Kliffkante");
        double gk = t.height(kl.x(), kl.z());
        check(gk > 20, f("Kliffkante steht oben: Grund %.1f m", gk));
        Ansichten.Ansicht sp = Ansichten.byName("Spuelsaum");
        double gs = t.height(sp.x(), sp.z());
        check(Math.abs(gs) < 1.0, f("Spülsaum an der Wasserlinie: Grund %+.2f m", gs));
        check(Ansichten.byName("UweDuene") != null && Ansichten.byName("7") != null && Ansichten.byName("KampenerLeuchtturm") != null, "Namen ohne Umlaute und Tasten");
        Camera h = new Camera();
        h.snap();
        check(h.eye[0] < Site.KLIFFKANTE.x() && h.heading() > 30 && h.heading() < 100, f("Grundansicht von See auf die Küste, Blick %03.0f°", h.heading()));

        teil("Uhr und Sonne am Start");
        SceneClock clock = new SceneClock();
        double[] s = clock.sun();
        check(clock.label().contains("20.7.2026") && clock.label().contains("20:30") && clock.zoneLabel().equals("MESZ"), "Start " + clock.label());
        check(s[0] > 3 && s[0] < 14 && s[1] > 280 && s[1] < 310, f("Sonne am Start %.1f° hoch aus %03.0f° (tief über der See)", s[0], s[1]));
        double day = LocalDate.of(2026, 6, 21).atStartOfDay(SceneClock.ZONE).toInstant().toEpochMilli();
        double down = Sun.crossing(day, Site.LAT, Site.LON, false);
        String dn = String.format("%tR", java.time.Instant.ofEpochMilli((long) down).atZone(SceneClock.ZONE));
        check(dn.compareTo("21:55") > 0 && dn.compareTo("22:20") < 0, "Sonnenuntergang 21.6. um " + dn + " MESZ");
        double[] dir = Sun.direction(s[0], s[1]);
        check(dir[0] < 0 && dir[1] > 0, "Licht am Abend von Westen auf die Wand");

        teil("Verdrahtung der Hüllen");
        Wiring w = new Wiring(clock);
        w.setTerrain(t);
        double[] see = Site.fromUtm(Site.wasserlinieE(6_089_500) - 300, 6_089_500);
        double[] land = Site.fromUtm(Site.wasserlinieE(6_089_500) + 500, 6_089_500);
        check(w.surf.depthAt(see[0], see[1], 0) > 0 && w.surf.depthAt(land[0], land[1], 0) < 0, "surf sieht See und Land über Surf.Bed und Surf.Level");
        double xb = w.surf.breakPointAlong(see[0] - 3000, see[1], land[0], land[1], 0);
        check(!Double.isNaN(xb), f("Sylter See bricht vor dem Strand: %.0f m vom Startpunkt", xb));
        w.pegel = 1.0;
        double d1 = w.surf.depthAt(see[0], see[1], 0);
        w.pegel = 0;
        check(Math.abs(d1 - w.surf.depthAt(see[0], see[1], 0) - 1) < 1e-9, "Pegel von Hand geht durch (+1 m Wasser)");
        w.windSpeed = 14;
        double[] nass = Site.fromUtm(Site.wasserlinieE(6_089_500) + 5, 6_089_500), trocken = Site.fromUtm(Site.wasserlinieE(6_089_500) + 45, 6_089_500);
        check(w.dune.fluxAt(nass[0], nass[1]) == 0 && w.dune.fluxAt(trocken[0], trocken[1]) > 0, "Wind 14 m/s: nasser Strand fliegt nicht, oberer Strand schon");
        System.out.println("    " + w.surfZeile(see[0], see[1]));
        System.out.println("    " + w.duneZeile(trocken[0], trocken[1]));

        teil("Renderer.jar und Shader");
        List<String> pk = Bibliothek.pakete();
        check(Bibliothek.fehlend().isEmpty(), "Renderer.jar: " + Bibliothek.kurz());
        check(pk.contains("surf") && pk.contains("dune") && pk.contains("beacon"), "neu: " + Bibliothek.neuStand());
        Renderer rd = new Renderer(clock, t);
        for (String[] prog : rd.shaderFiles())
            for (String file : prog)
                check(Renderer.class.getResource("/com/dan/sylt/gl/shader/" + file) != null, "Shader " + file + " im Jar");
        for (String c : new String[]{"com.dan.surf.demo.SurfCheck", "com.dan.dune.demo.DuneCheck"}) {
            Process p = new ProcessBuilder(javaBin(), "-Dstdout.encoding=UTF-8", "-cp", System.getProperty("java.class.path"), c).redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            check(p.waitFor() == 0 && out.contains("Alles in Ordnung."), c + ": " + (out.contains("Alles in Ordnung.") ? "Alles in Ordnung." : out.strip()));
        }

        teil("Regel 2.4: die App liest nie aus C:\\Daten");
        File src = new File("src");
        if (src.isDirectory()) {
            int treffer = 0;
            try (Stream<Path> st = Files.walk(src.toPath())) {
                for (Path p : (Iterable<Path>) st.filter(x -> x.toString().endsWith(".java"))::iterator) {
                    String txt = Files.readString(p, StandardCharsets.UTF_8);
                    boolean tool = p.toString().replace('\\', '/').contains("/sylt/tools/");
                    if (!tool && txt.replace("\\\\", "\\").toLowerCase(Locale.ROOT).contains("c:\\daten") && txt.contains("new File(\"C:"))
                        treffer++;
                }
            }
            check(treffer == 0, "kein Laufzeitcode öffnet C:\\Daten (nur Werkzeuge in com.dan.sylt.tools, ab Phase 2)");
        } else System.out.println("    (src nicht gefunden, übersprungen)");

        System.out.printf(Locale.GERMAN, "%n%d Prüfungen in %.1f s%n", n, (System.nanoTime() - t0) / 1e9);
        System.out.println(bad == 0 ? "ERGEBNIS: alles in Ordnung" : "ERGEBNIS: " + bad + " Fehler");
        System.exit(bad == 0 ? 0 : 1);
    }

    /** Höchster Punkt quer zur Küste zwischen s0 und s1 m landwärts der Wasserlinie am Nordwert n. */
    static double maxQuer(double n, double s0, double s1) {
        double m = -1e9;
        for (double s = s0; s <= s1; s += 1) {
            double[] xz = Site.fromUtm(Site.wasserlinieE(n) + s, n);
            m = Math.max(m, Buehne.height(xz[0], xz[1]));
        }
        return m;
    }

    static String javaBin() { return new File(System.getProperty("java.home"), "bin/java").getPath(); }

    static String f(String fmt, Object... a) { return String.format(Locale.GERMAN, fmt, a); }

    static void teil(String t) { System.out.println("\n— " + t); }

    static void check(boolean c, String what) {
        n++;
        if (!c) bad++;
        System.out.println((c ? "  ok    " : "  FEHLER ") + what);
    }

    private Pruefung() { }
}
