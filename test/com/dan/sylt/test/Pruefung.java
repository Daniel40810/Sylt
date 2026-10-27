package com.dan.sylt.test;

import com.dan.sylt.app.Bibliothek;
import com.dan.sylt.camera.Ansichten;
import com.dan.sylt.camera.Camera;
import com.dan.sylt.gl.Mat4;
import com.dan.sylt.gl.Quality;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.gl.TerrainLod;
import com.dan.sylt.scene.Buehne;
import com.dan.sylt.scene.Daten;
import com.dan.sylt.scene.Geo;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Sun;
import com.dan.sylt.scene.Terrain;
import com.dan.sylt.tools.GelaendeImport;
import com.dan.sylt.tools.Inhalt;
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
 * Prüfung der App ohne Grafikkarte und ohne Datenbank (Ant-Target {@code pruefung}), Stand Phase 3: Raum und Orte,
 * die Bühne gegen die Profile der BAW, das Gelände (16-Bit-Werte, Raster, Höhe gleich Bühne), die Auswahl der
 * Detailstufen, Ansichten und Kamera, Uhr und Sonne am Start, die Verdrahtung der Hüllen von surf und dune, Renderer.jar
 * mit den neuen Packages, die Shader-Dateien, die aufbereiteten Daten (Prüfsummen, Gelände, Nähte, Kliff, Klassen), das
 * Grundbild (Himmel, Wolken, See, Albedo, Biom, Nässe des Sands) und
 * die Regel aus dem Werkbuch 2.4 (kein Laufzeitcode liest C:\Daten oder benutzt die Werkzeuge).
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
        double[][] profil = {{6_088_000, Site.wasserlinieE(6_088_000)}, {6_089_500, Site.wasserlinieE(6_089_500)}, {6_091_000, Site.wasserlinieE(6_091_000)}, {6_092_500, Site.wasserlinieE(6_092_500)}};
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

        teil("Aufbereitete Daten (Phase 2, Werkbuch 2.4)");
        System.out.println("    " + Daten.zustand());
        File inh = new File(Daten.ordner(), Daten.INHALT);
        if (!inh.isFile()) System.out.println("    (keine aufbereiteten Daten: ant aufbereitung; Prüfungen der Daten übersprungen)");
        else {
            List<String> falsch = Inhalt.pruefen(Daten.ordner());
            check(falsch.isEmpty(), "INHALT.txt: " + (falsch.isEmpty() ? "alle Prüfsummen stimmen" : String.join(", ", falsch.subList(0, Math.min(3, falsch.size())))));
            Terrain m = Terrain.load(Terrain.defaultDir());
            check(m.measured() && m.hasKern(), "Gelände aus daten/gelaende: " + m.source);
            double hu = m.height(Site.UWE_DUENE.x(), Site.UWE_DUENE.z()), hl = m.height(Site.LEUCHTTURM.x(), Site.LEUCHTTURM.z());
            check(hu > 49 && hu < 53, f("Uwe-Düne im DGM1: %.1f m (52,5 m mit Plattform)", hu));
            check(Math.abs(hl - Site.LEUCHTTURM.grund()) < 1.5, f("Grund am Kampener Leuchtturm %.1f m (Site %.1f m)", hl, Site.LEUCHTTURM.grund()));
            for (double[] wl : Site.WASSERLINIE) {
                if (wl[0] < 6_082_000 || wl[0] > 6_098_000) continue;
                double[] xz = Site.fromUtm(wl[1], wl[0]);
                check(Math.abs(m.height(xz[0], xz[1])) < 0.8, f("Wasserlinie bei N %.0f: %.2f m NHN", wl[0], m.height(xz[0], xz[1])));
            }
            double[] see8 = Site.fromUtm(Site.wasserlinieE(6_089_500) - 8000, 6_089_500);
            double hs8 = m.height(see8[0], see8[1]);
            check(hs8 < -11 && hs8 > -16, f("8 km draußen (BAW): %.1f m", hs8));
            Ansichten.Ansicht kk = Ansichten.byName("Kliffkante"), ss = Ansichten.byName("Spuelsaum");
            check(m.height(kk.x(), kk.z()) > 20, f("Ansicht Kliffkante oben: Grund %.1f m", m.height(kk.x(), kk.z())));
            check(Math.abs(m.height(ss.x(), ss.z())) < 1.0, f("Ansicht Spülsaum an der Wasserlinie: %+.2f m", m.height(ss.x(), ss.z())));
            byte[] quelle = Files.readAllBytes(new File(Terrain.defaultDir(), "quelle.u8").toPath());
            boolean[] valid = new boolean[quelle.length];
            for (int i = 0; i < quelle.length; i++) valid[i] = quelle[i] == 1;
            double[] nt = GelaendeImport.naehte(m, valid, quelle);
            check(nt[0] - nt[6] < 0.3 && nt[7] < 0.05, f("keine Naht an den Kacheln des DGM1: Rand 99 %% %.2f m, Mitte %.2f m, Überschuss im Mittel %+.3f m", nt[0], nt[6], nt[7]));
            check(nt[2] < 0.3, f("DGM1 → BAW ohne Stufe: 99 %% unter %.2f m", nt[2]));
            check(nt[4] < 0.3 && nt[5] < 0.3, f("Bandkanten Kern → Hauptraster %.3f m, Hauptraster → Fernring %.3f m", nt[4], nt[5]));
            List<String> kante = Files.readAllLines(new File(Daten.ordner(), "kliff/kante.csv").toPath());
            double wmax = 0;
            int zeilen = 0;
            for (String l : kante) {
                if (l.isEmpty() || !Character.isDigit(l.charAt(0))) continue;
                zeilen++;
                wmax = Math.max(wmax, Double.parseDouble(l.split(";")[7]));
            }
            check(zeilen > 2500 && wmax > 25 && wmax < 40, f("Kliff: %d m mit Wand, höchste %.1f m", zeilen, wmax));
            byte[] kls = Files.readAllBytes(new File(Daten.ordner(), "kern/klasse.u8").toPath());
            long[] c = new long[Terrain.ART.length];
            for (byte b : kls) c[b]++;
            check(c[Terrain.STRAND] > 0 && c[Terrain.KLIFF] > 0 && c[Terrain.HEIDE] > 0 && c[Terrain.DACH] > 0 && c[Terrain.SEE] > 0,
                    f("Klassen im Kern: Strand %.1f %%, Kliff %.2f %%, Heide %.1f %%, Dach %.1f %%", 100.0 * c[3] / kls.length, 100.0 * c[5] / kls.length, 100.0 * c[7] / kls.length, 100.0 * c[13] / kls.length));
        }

        teil("Grundbild (Phase 3): Himmel, Wolken, See, Farben, Bodendecke");
        Renderer r3 = new Renderer(clock, Terrain.load(Terrain.defaultDir()));
        r3.step(1 / 60.0);
        com.dan.sky.Sky.State sst = r3.wetter.sky.updateNow();
        double[] sc = clock.sun();
        check(Math.abs(sst.sunAlt - sc[0]) < 0.6, f("com.dan.sky und die Uhr sehen dieselbe Sonne: %.2f° gegen %.2f°", sst.sunAlt, sc[0]));
        check(r3.wetter.clouds.oktas == 3 && r3.wetter.clouds.type == com.dan.cloud.CloudType.CUMULUS, "Wolken am Start: 3/8 Cumulus (Sommerabend wie RotesKliff2)");
        com.dan.ocean.Ocean oc = r3.meer.ocean;
        check(Math.abs(oc.swellHeight - r3.wiring.surf.hs) < 1e-9 && Math.abs(oc.swellFromDeg - r3.wiring.surf.seaFromDeg) < 1e-9 && Math.abs(oc.swellPeriod - r3.wiring.surf.tp) < 1e-9,
                f("See und Brandung mit derselben Dünung: %.1f m, %.0f s aus %03.0f°", oc.swellHeight, oc.swellPeriod, oc.swellFromDeg));
        check(Math.abs(oc.windSpeed - r3.wiring.windSpeed) < 1e-9 && oc.windFromDeg == r3.wiring.windFromDeg, "See mit dem Wind der Verdrahtung");
        com.dan.sylt.scene.KernKarte kk = r3.karte;
        double aS = kk.albedoLum(Terrain.STRAND), aH = kk.albedoLum(Terrain.HEIDE), aW = kk.albedoLum(Terrain.WIESE), aN = kk.albedoLum(Terrain.NASS);
        check(Math.abs(aS - 0.40) < 0.01 && aH > 0.05 && aH < 0.16 && aW > 0.05 && aW < 0.25 && aN < aS, f("Albedo: %s", kk.describe()));
        boolean alleUnter = true;
        for (float v : kk.albedo) alleUnter &= v > 0 && v < 0.96;
        check(alleUnter, "Albedo jeder Art zwischen 0 und 0,95");
        if (kk.hatKarte() && kk.kliffband != null) {
            byte[] tw = kk.texturWerte();
            long inBand = 0, kliffAussen = 0;
            for (int qq = 0; qq < tw.length; qq++) {
                if ((tw[qq] & com.dan.sylt.scene.KernKarte.BAND_BIT) != 0) inBand++;
                else if (tw[qq] == Terrain.KLIFF) kliffAussen++;
            }
            double[] fuss = Site.fromUtm(456_724.5, 6_089_604.5), heide = Site.fromUtm(457_400, 6_089_604.5);
            int i1 = (int) Math.floor(fuss[0] - Terrain.KX0) + 20, j1 = (int) Math.floor(fuss[1] - Terrain.KZ0), i2 = (int) Math.floor(heide[0] - Terrain.KX0);
            check(kliffAussen == 0 && (tw[j1 * Terrain.KN + i1] & com.dan.sylt.scene.KernKarte.BAND_BIT) != 0 && (tw[j1 * Terrain.KN + i2] & com.dan.sylt.scene.KernKarte.BAND_BIT) == 0,
                    f("Band des Kliffs aus kante.csv: %d Zeilen, %.2f km², nur dort die Wand; „Kliff“ außerhalb zur Düne (Befund Phase 3)", kk.bandZeilen, inBand / 1e6));
        }
        // Phase 4a: das Kliff in der Szene
        com.dan.sylt.gl.Kliff kf = r3.kliff;
        if (kf.map != null) {
            com.dan.sylt.scene.Kante kt = kf.kante;
            int kr = kt.row(Site.fromUtm(0, 6_089_600)[1]);
            double kxm = (kt.fussX(kr) + kt.kanteX(kr)) / 2, kzr = kt.z(kr);
            double wallBand = 0, wallInland = 0;
            for (double x = Math.min(kt.fussX(kr), kt.kanteX(kr)); x <= Math.max(kt.fussX(kr), kt.kanteX(kr)); x += 1) wallBand = Math.max(wallBand, kf.wall(x, kzr));
            for (double dx = 300; dx <= 900; dx += 4) wallInland = Math.max(wallInland, kf.wall(kt.kanteX(kr) + dx, kzr));
            check(wallBand > 0.5 && wallInland == 0, f("Kliff: Wand im Band (bis %.2f), landeinwärts keine (Dünen bleiben Sand); %s", wallBand, kf.map.describe()));
            double[] ksu = kf.strata.valuesAt(kxm, Site.fromUtm(0, 6_088_750)[1]), kno = kf.strata.valuesAt(kxm, Site.fromUtm(0, 6_091_400)[1]);
            check(ksu[1] > 5 && kno[1] < 0 && ksu[2] < kno[2] + 0.01, f("Schichten je Ort: Kaolinsand im Süden bis %.1f m, im Norden unter dem Strand (%.1f m); Flugsand %.1f m im Süden, %.1f m im Norden", ksu[1], kno[1], ksu[2], kno[2]));
            double topHere = kf.strata.valuesAt(kxm, kzr)[0];
            check(Math.abs(topHere - kt.hKante[kr]) < 4, f("Oberkante aus dem Gelände %.1f m, Kante aus dem bDOM %.1f m (N 6 089 600)", topHere, kt.hKante[kr]));
            int spts = 0;
            for (com.dan.cliff.Sod sd : kf.sods) spts += sd.points();
            check(kf.sods.size() > 0 && spts * 2 > 1500, f("Grasnarbe entlang der Kante: %d Stücke, %.1f km", kf.sods.size(), spts * 2 / 1000.0));
            double fdl = kf.findlinge.density.at(kt.fussX(kr) - 5, kzr), fdlLand = kf.findlinge.density.at(kt.kanteX(kr) + 50, kzr);
            check(fdl > 0 && fdlLand == 0, f("Findlinge nur auf dem Strand vor dem Fuß (Dichte %.2f, hinter der Kante 0)", fdl));
            // Strandhafer auf den Sturzkegeln: irgendwo vor dem Fuß im Band
            com.dan.sylt.wiring.Bodendecke kbd = new com.dan.sylt.wiring.Bodendecke(r3.terrain, r3.karte, kf::talus);
            float[] kco = new float[5];
            int hafer = 0, nT = 0;
            for (int i = 0; i < kt.n; i += 7)
                for (double dx = -12; dx <= 4; dx += 2) {
                    double x = kt.fussX(i) + dx, z = kt.z(i);
                    if (kf.talus(x, z) < 0.3) continue;
                    nT++;
                    kbd.cover(x, z, kco);
                    if (kco[0] > 0.2) hafer++;
                }
            check(nT > 50 && hafer > nT / 10, f("Strandhafer am Fuß: %d von %d Stellen mit Sturzkegel", hafer, nT));
        } else check(false, "Kliff: keine Kante (daten/kliff/kante.csv)");
        check(com.dan.ground.Biome.sylt().entries.stream().anyMatch(eb -> eb.plant.name.equals("Strandhafer"))
                && com.dan.ground.Biome.sylt().entries.stream().anyMatch(eb -> eb.plant.name.equals("Kartoffelrose")), "Biom Sylt mit Strandhafer, Krähenbeere, Besenheide, Kartoffelrose");
        byte[] mask = com.dan.sylt.gl.Meer.binnen(r3.terrain);
        double[] off = Site.fromUtm(Site.wasserlinieE(6_089_500) - 8000, 6_089_500), wattO = Site.fromUtm(463_000, 6_090_000);
        int iO = (int) Math.floor((off[0] - Terrain.X0) / Terrain.CELL), jO = (int) Math.floor((off[1] - Terrain.Z0) / Terrain.CELL);
        int iW = (int) Math.floor((wattO[0] - Terrain.X0) / Terrain.CELL), jW = (int) Math.floor((wattO[1] - Terrain.Z0) / Terrain.CELL);
        long binnen = 0;
        for (byte b : mask) if (b != 0) binnen++;
        check(mask[jO * Terrain.N + iO] == 0 && mask[jW * Terrain.N + iW] == 0, f("See draußen und Watt im Osten mit der See verbunden; %.0f %% des Hauptrasters Land oder Binnensenke", 100.0 * binnen / mask.length));
        if (r3.terrain.measured() && r3.naesse != null) {
            com.dan.mudflat.WetField wf = r3.naesse.feld;
            // seit Phase 7a mit Tide und Auflauf: „oben“ ist der erste Punkt landwärts, der 1,7 m über Pegel und Auflauf liegt
            double nb = r3.wiring.pegel + r3.brandung.auflaufR2() * 0.8 + 1.7;
            double[] unten = Site.fromUtm(Site.wasserlinieE(6_089_500) - 20, 6_089_500), oben = Site.fromUtm(Site.wasserlinieE(6_089_500) + 60, 6_089_500);
            for (double d = 0; d < 200; d += 2) {
                double[] p = Site.fromUtm(Site.wasserlinieE(6_089_500) + d, 6_089_500);
                if (r3.terrain.height(p[0], p[1]) > nb) { oben = p; break; }
            }
            check(wf.wetnessAt(unten[0], unten[1]) > 0.99 && wf.wetnessAt(oben[0], oben[1]) < 0.3 && wf.sedimentAt(oben[0], oben[1]) < 0.01,
                    f("Nässe des Sands: unter Wasser %.2f, oben am Strand %.2f (Sand)", wf.wetnessAt(unten[0], unten[1]), wf.wetnessAt(oben[0], oben[1])));
        }
        check(Renderer.PASSES.length == 7 && Quality.MITTEL.groundRadius > 10 && Quality.MITTEL.ocean == com.dan.ocean.Ocean.Quality.MEDIUM, "Durchgänge " + String.join(", ", Renderer.PASSES));
        check(!r3.showMarks || Boolean.getBoolean("sylt.marken"), "Marken im Grundbild aus (M schaltet sie an)");

        teil("Brandung in der Szene (Phase 7a)");
        brandung(r3, clock);

        teil("Flugsand in der Szene (Phase 9a)");
        sand(r3);

        teil("Türme und Bauten (Phase 10)");
        tuerme(r3);

        teil("Renderer.jar und Shader");
        List<String> pk = Bibliothek.pakete();
        check(Bibliothek.fehlend().isEmpty(), "Renderer.jar: " + Bibliothek.kurz());
        check(pk.contains("surf") && pk.contains("dune") && pk.contains("beacon"), "neu: " + Bibliothek.neuStand());
        Renderer rd = new Renderer(clock, t);
        for (String[] prog : rd.shaderFiles())
            for (String file : prog)
                check(Renderer.class.getResource("/com/dan/sylt/gl/shader/" + file) != null, "Shader " + file + " im Jar");
        for (String c : new String[]{"com.dan.surf.demo.SurfCheck", "com.dan.dune.demo.DuneCheck", "com.dan.tide.demo.TideCheck", "com.dan.beacon.demo.BeaconCheck"}) {
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
            int werkzeug = 0;
            try (Stream<Path> st = Files.walk(src.toPath())) {
                for (Path p : (Iterable<Path>) st.filter(x -> x.toString().endsWith(".java"))::iterator) {
                    String pp = p.toString().replace('\\', '/');
                    if (pp.contains("/sylt/tools/")) continue;
                    String txt = Files.readString(p, StandardCharsets.UTF_8);
                    if (txt.contains("import com.dan.sylt.tools.") || txt.contains("Rohdaten.")) werkzeug++;
                }
            }
            check(werkzeug == 0, "die App benutzt keine Werkzeuge (Rohdaten, Aufbereitung) zur Laufzeit");
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

    /** Seit Phase 7a: Tide, Wellenfeld vor dem Kern, gebackene Textur, Riffe, Buhnen aus dem Luftbild, Netz nahe am Auge, Nässe aus dem Schwall. */
    static void brandung(Renderer r, SceneClock clock) {
        com.dan.sylt.wiring.Wiring w = r.wiring;
        check(w.tide.gauge.startsWith("Westerland") && w.tideAn, "Pegel aus Tide.westerland() (F25 A): " + w.tideZeile());
        w.stepTide();
        double soll = w.tide.levelAtMillis(clock.epochMillis());
        w.tideAn = false;
        w.pegel = 0.3;
        w.stepTide();
        boolean hand = Math.abs(w.pegel - 0.3) < 1e-12;
        w.tideAn = true;
        w.stepTide();
        check(Math.abs(w.pegel - soll) < 0.01 && hand, f("Pegel jetzt %+.2f m NHN aus der Tide (mit Windstau %+.2f m); von Hand bleibt er stehen", w.pegel, w.tide.surge));
        com.dan.sylt.scene.Brandung b = r.brandung;
        long t0 = System.nanoTime();
        b.computeNow(clock.t());
        double ms = (System.nanoTime() - t0) / 1e6;
        double[] k = Site.fromUtm(Site.KLIFFKANTE.e(), Site.KLIFFKANTE.n());
        double[] wl = com.dan.sylt.scene.Kueste.waterline(k[0], k[1]);
        double qbUfer = 0, qbDraussen = 0, ph0 = 0, ph1 = 0;
        for (double d = 0; d <= 40; d += 8) qbUfer = Math.max(qbUfer, b.feld.breakingAt(wl[0] - d * com.dan.sylt.scene.Kueste.SX, wl[1] - d * com.dan.sylt.scene.Kueste.SZ));
        double[] aus = {wl[0] - 1000 * com.dan.sylt.scene.Kueste.SX, wl[1] - 1000 * com.dan.sylt.scene.Kueste.SZ};
        qbDraussen = b.feld.breakingAt(aus[0], aus[1]);
        ph0 = b.texAt(aus[0], aus[1], 1);
        ph1 = b.texAt(wl[0] - 30 * com.dan.sylt.scene.Kueste.SX, wl[1] - 30 * com.dan.sylt.scene.Kueste.SZ, 1);
        double su = b.setupAnDerKueste(k[0], k[1]);
        check(b.feld.result() != null && qbUfer > 0.5 && qbDraussen < 0.01, f("Wellenfeld vor dem Kern (%d × %d zu %.0f m, %.1f s): an der Wasserlinie vor der Kliffkante brechen %.0f %%, 1 km draußen %.1f %%",
                b.feld.nu, b.feld.nv, b.feld.cell, ms / 1000, qbUfer * 100, qbDraussen * 100));
        double lam = com.dan.surf.Surf.deepLength(w.surf.tp);
        check(ph1 - ph0 > 970 / lam && su > 0.02 && su < 0.6, f("Textur %d² zu %.0f m: Phase wächst zur Küste um %.1f Wellenlängen auf 970 m (tief wären es %.1f), Set-up an der Wasserlinie %+.2f m",
                b.texN, com.dan.sylt.scene.Brandung.TEX_ZELLE, ph1 - ph0, 970 / lam, su));
        int abschnitte = 0, mitBrandung = 0;
        for (double n = 6_088_100; n < 6_091_500; n += 200) {
            abschnitte++;
            double best = 0;
            for (double d = -40; d <= 250; d += 6) {
                double[] p = Site.fromUtm(Site.wasserlinieE(n) - d, n);
                best = Math.max(best, b.texAt(p[0], p[1], 0));
            }
            if (best > 0.3) mitBrandung++;
        }
        check(mitBrandung >= 0.85 * abschnitte, f("an der Küste des Kerns ist die Brandung weiß (Walzen über 30 %% auf %d von %d Querprofilen alle 200 m; in den Rinnen schwächer)", mitBrandung, abschnitte));
        double[] riff = {wl[0] - 150 * com.dan.sylt.scene.Kueste.SX, wl[1] - 150 * com.dan.sylt.scene.Kueste.SZ};
        double bar = b.riffe.offset(com.dan.sylt.scene.Kueste.offshore(riff[0], riff[1]), com.dan.sylt.scene.Kueste.along(riff[0], riff[1]));
        double[] rinnen = b.riffe.ripsBetween(com.dan.sylt.scene.Kueste.along(Site.fromUtm(457_463, 6_091_500)[0], Site.fromUtm(457_463, 6_091_500)[1]),
                com.dan.sylt.scene.Kueste.along(Site.fromUtm(455_972, 6_088_000)[0], Site.fromUtm(455_972, 6_088_000)[1]));
        double dg = w.surf.bed.height(riff[0], riff[1]) - r.terrain.height(riff[0], riff[1]);
        check(Math.abs(dg - bar) < 0.5 && rinnen.length >= 6,
                f("Grund der Brandung: Gelände (über ±%.0f m geglättet) mit Riffen (hier %+.2f m, Grund %+.2f m über dem Gelände) und %d Rinnen vor dem Kern (F10 A)",
                        com.dan.sylt.scene.Brandung.GLAETTEN, bar, dg, rinnen.length));
        com.dan.sylt.scene.Buhnen bu = b.buhnen;
        boolean lage = true, amStrand = true;
        for (com.dan.sylt.scene.Buhnen.Pfahl p : bu.pfaehle) {
            // gemessene im Kern, geschätzte bei Wenningstedt südlich davon (seit Phase 8)
            boolean g = com.dan.sylt.scene.Buhnen.LISTE[p.buhne()].geschaetzt();
            double[] u = Site.toUtm(p.x(), p.z());
            if (g ? u[1] > 6_088_000 || u[1] < 6_087_000 : !Site.KERN.contains(p.x(), p.z())) lage = false;
            if (p.kopf() < -4 || p.kopf() > 4) amStrand = false;
        }
        com.dan.sylt.scene.Buhnen.Pfahl p0 = bu.pfaehle.get(0);
        com.dan.ocean.ObstacleField of = bu.hindernisseNahe(p0.x(), p0.z(), 900);
        double[] wen = bu.mitte(com.dan.sylt.scene.Buhnen.LISTE.length - 1);
        com.dan.ocean.ObstacleField ofW = bu.hindernisseNahe(wen[0], wen[1], 900);
        boolean wenRing = ofW != null && ofW != of && ofW.distance(wen[0], wen[1]) < 0.3 && ofW.w() * ofW.h() < 2_000_000;
        check(bu.pfaehle.size() > 600 && lage && amStrand && of.distance(p0.x(), p0.z()) < 0.3 && of.distance(p0.x() + 30, p0.z()) > 5 && wenRing,
                bu.describe() + f(", gemessene im Kern, geschätzte bei Wenningstedt; Köpfe zwischen %+.1f und %+.1f m NHN; Brandungsringe in ocean nahe am Auge", bu.pfaehle.stream().mapToDouble(com.dan.sylt.scene.Buhnen.Pfahl::kopf).min().orElse(0),
                        bu.pfaehle.stream().mapToDouble(com.dan.sylt.scene.Buhnen.Pfahl::kopf).max().orElse(0)));
        t0 = System.nanoTime();
        com.dan.sylt.scene.Nahbrandung.Stueck st = r.nah.baue(wl, com.dan.sylt.scene.Kueste.along(wl[0], wl[1]), w.pegel, 24);
        double bauS = (System.nanoTime() - t0) / 1e9;
        for (int i = 0; i < 20 * 30; i++) { st.zug.step(1 / 30.0); st.schwall.step(1 / 30.0, st.zug); }
        st.netz.build(st.zug, st.xEnde, st.schwall);
        boolean fin = true;
        for (int i = 0; i < st.netz.vertexCount * com.dan.surf.BreakerMesh.STRIDE; i++) if (!Float.isFinite(st.netz.vertices[i])) fin = false;
        double[] mitte = st.welt(st.xWasserlinie, st.zug.width / 2), zurueck = st.lokal(mitte[0], mitte[1]);
        check(st.schwall.runups > 0 && st.zug.spawned > 0 && fin && Math.abs(zurueck[0] - st.xWasserlinie) < 1e-6 && Math.abs(zurueck[1] - st.zug.width / 2) < 1e-6,
                f("Netz nahe am Auge vor der Kliffkante: %.0f m Strand (%d Reihen auf %.0f m), gebaut und eingeschwungen in %.1f s, %d Auflaufspitzen in 20 s, Netz %d Ecken",
                        st.xEnde, st.zug.rows, st.zug.width, bauS, st.schwall.runups, st.netz.vertexCount));
        if (r.naesse != null) {
            r.naesse.update(w.pegel, b.auflaufR2() * 0.8);
            r.naesse.ueberlagern(st);
            int nass = 0, zellen = 0;
            for (double x = st.schwall.boundaryX(); x < st.xEnde; x += 2) {
                double[] p = st.welt(x, st.zug.width / 2);
                double v = st.nass(p[0], p[1]);
                if (Double.isNaN(v) || r.terrain.height(p[0], p[1]) <= w.pegel) continue;
                zellen++;
                if (Math.abs(r.naesse.feld.wetnessAt(p[0], p[1]) - Math.max(com.dan.mudflat.WetField.residual(0), v)) < 0.35) nass++;
            }
            r.naesse.ueberlagern(null);
            check(zellen > 5 && nass > zellen * 0.7, f("Nässe des Sands im Stück aus dem Schwall: %d von %d Stellen über dem Pegel folgen ihm", nass, zellen));
        }
        String[] hud = r.hudLines();
        boolean tideZ = false, brZ = false;
        for (String l : hud) { tideZ |= l.startsWith("Tide: "); brZ |= l.startsWith("Brandung: "); }
        check(tideZ && brZ, "HUD: Zeilen für Tide und Brandung");
    }

    /** Seit Phase 9a: Windfeld aus wind, Flugsand um das Auge, nasser Strand, Regen, Körner, „Sandsturm am Strand“. */
    static void sand(Renderer r) {
        com.dan.sylt.scene.Sand s = r.sand;
        r.wiring.tideAn = false;
        r.wiring.pegel = -0.3;
        r.sandsturm();
        long t0 = System.nanoTime();
        r.sandJetzt();
        double ms = (System.nanoTime() - t0) / 1e6;
        double[] e = r.camera.eye;
        com.dan.dune.FluxField f = s.feld();
        double sp = s.dune.windSpeedAt(e[0], e[2]), dir = s.dune.windFromAt(e[0], e[2]);
        check(s.wind.solution != null && sp > 12 && sp < 30 && Math.abs(((dir - 280) % 360 + 540) % 360 - 180) < 25,
                f("Windfeld aus com.dan.wind über dem Kern (F21 A): am Strand %.1f m/s aus %03.0f° bei Bft 9 aus 280° · %s", sp, dir, s.wind.solution.describe()));
        double q = f.fluxAt(e[0], e[2]);
        // nasser Strand: an der Wasserlinie unter dem Auge
        double[] wl = Site.fromUtm(Site.wasserlinieE(6_089_300) + 3, 6_089_300);
        double qn = f.fluxAt(wl[0], wl[1]), mn = s.dune.moisture.moisture(wl[0], wl[1]);
        check(q > 0.01 && (Double.isNaN(qn) || qn < 1e-4) && mn > 0.05,
                f("Sandsturm am Strand: oben am Strand fliegen %.0f g/(m·s), an der Wasserlinie nichts (Sand %.0f %% nass) · %s", q * 1000, mn * 100, f.describe()));
        com.dan.dune.Grains g = s.koerner();
        check(g.count > 500, f("Körner um das Auge: %s; Windfeld, Feld und 3 s Körner in %.0f ms", g.describe(), ms));
        // Heide hinter der Kante gibt keinen Sand
        double[] heide = null;
        for (double d = 200; d < 600 && heide == null; d += 10) {
            double[] p = Site.fromUtm(Site.wasserlinieE(6_089_300) + d, 6_089_300);
            if (r.karte.at(r.terrain, p[0], p[1]) == com.dan.sylt.scene.Terrain.HEIDE) heide = p;
        }
        check(heide == null || s.dune.cover.lambda(heide[0], heide[1]) >= 1, "Heide und Kliff geben keinen Sand (Bewuchs)");
        s.regen = 0.06;
        s.bereit(e, r.wiring.windSpeed, r.wiring.windFromDeg, 0.5);
        double qr = s.feld().fluxAt(e[0], e[2]);
        check(qr == 0, f("nach starkem Regen (6 %% Wasser im Sand, gebunden) fliegt bei Bft 9 nichts (Schwelle %.2f m/s, u* %.2f m/s)", s.feld().thresholdAt(e[0], e[2]), s.feld().ustarAt(e[0], e[2])));
        s.regen = 0;
        String z = s.zeile(e[0], e[2]);
        check(z.startsWith("Flugsand:") && java.util.Arrays.stream(r.hudLines()).anyMatch(l -> l.startsWith("Flugsand:")), "HUD: " + z);
        r.wiring.tideAn = true;
    }

    /**
     * Seit Phase 10: Häuser aus LOD2 mit Baustoffen, die beiden Türme (Maße, Feuerhöhe, Band), Wärterhäuser, Treppe und
     * Bohlenweg, Strandkörbe (Saison, Wind, Sturm), Galloways, Leuchtfeuer (Dämmerungsschalter, Quermarke heute und
     * früher), die Vorlagen und der trockene Platz für „Sandsturm am Strand“.
     */
    static void tuerme(Renderer r) {
        com.dan.sylt.scene.Bauwerke b = r.bauwerke;
        com.dan.sylt.scene.Bauten lod = b.bauten;
        boolean daten = !lod.haeuser.isEmpty();
        check(!daten || (lod.haeuser.size() > 15_000 && lod.ausgelassen == 2), "LOD2: " + lod.describe());
        double naechst = Double.MAX_VALUE;
        for (com.dan.sylt.scene.Bauten.Haus h : lod.haeuser)
            for (Site.Ort o : new Site.Ort[]{Site.QUERMARKE, Site.LEUCHTTURM}) naechst = Math.min(naechst, Math.hypot(h.x() - o.x(), h.z() - o.z()));
        check(!daten || naechst > 7, f("die Türme aus LOD2 sind weg (nächstes Haus %.0f m), sie werden genauer gebaut", naechst));
        int m = com.dan.sylt.scene.Netz.merkmal(com.dan.sylt.scene.Netz.REET, true, 300, 27.35);
        check(com.dan.sylt.scene.Netz.baustoff(m) == com.dan.sylt.scene.Netz.REET && com.dan.sylt.scene.Netz.fenster(m)
                && com.dan.sylt.scene.Netz.kennzahl(m) == 300 && Math.abs(com.dan.sylt.scene.Netz.grund(m) - 27.35) < 0.026, "Merkmal je Ecke: Baustoff, Fenster, Kennzahl, Grund auf 5 cm");
        // Türme: höchste Ecke an der Achse, Feuerhöhe
        double[] qT = b.turmQ, kT = b.turmK;
        double qTop = 0, kTop = 0;
        int band = 0;
        com.dan.sylt.scene.Netz n = b.netz;
        for (int i = 0; i < n.ecken(); i++) {
            double dq = Math.hypot(n.x(i) - qT[0], n.z(i) - qT[2]), dk = Math.hypot(n.x(i) - kT[0], n.z(i) - kT[2]);
            if (dq < 3) qTop = Math.max(qTop, n.y(i) - qT[1]);
            if (dk < 5) {
                kTop = Math.max(kTop, n.y(i) - kT[1]);
                if (com.dan.sylt.scene.Netz.baustoff(n.merkmal(i)) == com.dan.sylt.scene.Netz.LACK_SCHWARZ && n.y(i) - kT[1] > 12 && n.y(i) - kT[1] < 20) band++;
            }
        }
        check(qTop > 11.5 && qTop < 12.5 && Math.abs(b.fokusQuermarke[1] - 23) < 1.5,
                f("Quermarkenfeuer: Spitze %.1f m über Grund (LOD2 bis zum Dach 11,0 m), Feuerhöhe %.1f m NHN (23 m)", qTop, b.fokusQuermarke[1]));
        check(kTop > 39 && kTop < 41.5 && Math.abs(b.fokusKampen[1] - 62) < 0.5 && band > 50,
                f("Kampener Leuchtturm: %.1f m hoch (40 m), Feuerhöhe %.1f m NHN (62 m), schwarzes Band (%d Ecken)", kTop, b.fokusKampen[1], band));
        if (daten) {
            int weiss = 0, wh = 0, reetK = 0, nK = 0, reetW = 0, nW = 0;
            for (com.dan.sylt.scene.Bauten.Haus h : lod.haeuser) {
                com.dan.sylt.scene.Bauten.Stil st = lod.stil(h, new double[]{kT[0], kT[2]});
                if (Math.hypot(h.x() - kT[0], h.z() - kT[2]) < com.dan.sylt.scene.Bauten.WAERTER) {
                    wh++;
                    if (st.wand() == com.dan.sylt.scene.Netz.PUTZ && st.dach() == com.dan.sylt.scene.Netz.DACH_GRAU) weiss++;
                }
                double[] u = Site.toUtm(h.x(), h.z());
                boolean kampen = com.dan.sylt.scene.Bauten.reetAnteil(u[0], u[1]) > 0.5, west = u[1] < 6_085_500;
                if (kampen) { nK++; if (st.dach() == com.dan.sylt.scene.Netz.REET) reetK++; }
                if (west) { nW++; if (st.dach() == com.dan.sylt.scene.Netz.REET) reetW++; }
            }
            check(wh >= 3 && weiss == wh, f("Wärterhäuser am Kampener Leuchtturm: %d von %d weiß mit grauem Dach (Kampen1–4)", weiss, wh));
            check(nK > 50 && reetK / (double) nK > 2 * reetW / Math.max(1.0, nW),
                    f("Reetdächer: Kampen %.0f %% der Häuser, Westerland %.0f %%", 100.0 * reetK / nK, 100.0 * reetW / Math.max(1, nW)));
        }
        check(b.teile.stream().anyMatch(t -> t.startsWith("Treppe bei")) || r.kliff.kante == null, "Treppe am Kliff: " + b.teile.stream().filter(t -> t.startsWith("Treppe bei")).findFirst().orElse("–"));
        double[] ende = b.bohlenweg.get(b.bohlenweg.size() - 1);
        double unter = Double.MAX_VALUE;
        for (double[] p : b.bohlenweg) unter = Math.min(unter, p[1] - r.terrain.height(p[0], p[2]));
        check(Math.hypot(ende[0] - qT[0], ende[2] - qT[2]) < 12 && unter > 0.1,
                f("Bohlenweg: %d Bohlen bis %.0f m vor das Quermarkenfeuer, überall mindestens %.2f m über dem Grund", b.bohlenweg.size(), Math.hypot(ende[0] - qT[0], ende[2] - qT[2]), unter));
        // Strandkörbe (F20 A)
        com.dan.sylt.scene.Strandkoerbe k = r.koerbe;
        k.stellen(java.time.LocalDate.of(2026, 7, 20), 270, 8, 200);
        int sommer = k.gezeigt();
        double oeff = com.dan.sylt.scene.Strandkoerbe.oeffnung(270, 8, 200);
        k.stellen(java.time.LocalDate.of(2026, 1, 20), 270, 8, 200);
        int winter = k.gezeigt();
        k.stellen(java.time.LocalDate.of(2026, 7, 20), 270, 19, 200);
        int sturm = k.gezeigt();
        k.stellen(java.time.LocalDate.of(2026, 4, 20), 270, 8, 200);
        int april = k.gezeigt();
        check(sommer > 100 && winter == 0 && sturm == 0 && april > 0 && april < sommer && Math.abs(oeff - 90) < 1,
                f("Strandkörbe: Juli %d, 20. April %d, Januar %d, bei Bft 8 (Sturmwarnung) %d; Westwind 8 m/s: Öffnung nach %03.0f° (Lee)", sommer, april, winter, sturm, oeff));
        k.stellen(r.clock.date(), r.wiring.windFromDeg, r.wiring.windSpeed, 200);
        // Galloways (F24 A)
        com.dan.sylt.scene.Herde h = r.herde;
        double[][] vorher = new double[h.tiere.size()][];
        for (int i = 0; i < vorher.length; i++) vorher[i] = new double[]{h.tiere.get(i).x, h.tiere.get(i).z};
        for (int i = 0; i < 600; i++) h.schritt(1);
        boolean erlaubt = true;
        double weg = 0;
        for (int i = 0; i < vorher.length; i++) {
            com.dan.sylt.scene.Herde.Tier t = h.tiere.get(i);
            erlaubt &= h.erlaubt(t.x, t.z);
            weg += Math.hypot(t.x - vorher[i][0], t.z - vorher[i][1]);
        }
        h.bauen();
        check(h.tiere.size() >= 10 && erlaubt && weg > 10 && h.netz.dreiecke() > 1000,
                f("Galloways: nach 10 min alle auf der Weide, zusammen %.0f m gegangen · %s · %,d Dreiecke", weg, h.describe(), h.netz.dreiecke()));
        // Leuchtfeuer (F18, F19 A)
        com.dan.sylt.scene.Leuchtfeuer lf = r.feuer;
        lf.quermarkeFrueher = false;
        lf.schritt(10, 20, 30, 0);
        lf.sofort(20);
        boolean nachtK = lf.kampen.isOn(), nachtQ = lf.quermarke.isOn();
        lf.schritt(10, 30_000, 30, 0);
        lf.sofort(30_000);
        boolean tagK = lf.kampen.isOn();
        lf.quermarkeFrueher = true;
        lf.schritt(10, 20, 30, 0);
        lf.sofort(20);
        boolean frueher = lf.quermarke.isOn();
        lf.quermarkeFrueher = false;
        check(nachtK && !tagK && !nachtQ && frueher && lf.kampen.characteristic().text.startsWith("Iso"),
                f("Leuchtfeuer: Kampen nachts an, am Tag aus; Quermarkenfeuer heute dunkel, wie früher an (%s)", lf.quermarke.characteristic().text));
        double[] lx = {com.dan.sylt.scene.Leuchtfeuer.lux(new double[]{2.3, 2.3, 2.3}, 0.8, new double[]{0.5, 0.5, 0.5}), com.dan.sylt.scene.Leuchtfeuer.lux(new double[3], -0.1, new double[]{1e-4, 1e-4, 1e-4})};
        check(lx[0] > 50_000 && lx[1] < 50, f("Licht am Turm: Tag %,.0f lx, Dämmerung %.1f lx", lx[0], lx[1]));
        // Vorlagen
        int gut = 0;
        for (Ansichten.Vorlage v : Ansichten.VORLAGEN) {
            Ansichten.Ansicht a = v.ansicht();
            Site.Ort ziel = v.name().startsWith("Kampen") ? Site.LEUCHTTURM : Site.QUERMARKE;
            double brg = Math.toDegrees(Math.atan2(ziel.x() - a.x(), -(ziel.z() - a.z())));
            double d = ((brg - a.heading()) % 360 + 540) % 360 - 180;
            if (Site.FERNFELD.contains(a.x(), a.z()) && Math.abs(d) < a.fovy() * 0.6 && a.fovy() >= 2 && a.fovy() <= 55) gut++;
        }
        Ansichten.Ansicht k1 = Ansichten.byName("Kampen1");
        check(gut == 8 && k1 != null && k1.fovy() < 5, f("Vorlagen: %d von 8 haben ihren Turm im Bild; Kampen1 mit Fernrohr (Öffnung %.1f°)", gut, k1 == null ? 0 : k1.fovy()));
        // Sandsturm bei Hochwasser auf trockenem Sand
        boolean tide = r.wiring.tideAn;
        r.wiring.tideAn = false;
        r.wiring.pegel = 0.68;
        r.sandsturm();
        double gE = r.terrain.height(r.camera.eye[0], r.camera.eye[2]);
        r.wiring.tideAn = tide;
        check(gE > 0.68 + 0.8, f("Sandsturm am Strand bei Pegel +0,68 m: das Auge steht über %.1f m NHN (trocken, bis 4 m vor dem Fuß des Kliffs)", gE));
        check(java.util.Arrays.stream(r.hudLines()).anyMatch(l -> l.startsWith("Bauten:")) && java.util.Arrays.stream(r.hudLines()).anyMatch(l -> l.startsWith("Leuchtfeuer")),
                "HUD: Bauten und Leuchtfeuer");
    }

    static String f(String fmt, Object... a) { return String.format(Locale.GERMAN, fmt, a); }

    static void teil(String t) { System.out.println("\n— " + t); }

    static void check(boolean c, String what) {
        n++;
        if (!c) bad++;
        System.out.println((c ? "  ok    " : "  FEHLER ") + what);
    }

    private Pruefung() { }
}
