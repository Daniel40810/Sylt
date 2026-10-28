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
            // seit Phase 15 nur, wo der Grund gleich vor der Kante abbricht (vorher 3 km, ein Teil lag als roter Wulst auf Dünenkämmen)
            check(kf.sods.size() > 0 && spts * 2 > 150, f("Grasnarbe an der Abbruchkante: %d Stücke, %.1f km", kf.sods.size(), spts * 2 / 1000.0));
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

        teil("Sturmflut und Abbruch des Kliffs (Phase 12)");
        sturmflut(r3, clock);

        teil("Wetter, Licht, Leben (Phase 13)");
        wetter(r3, clock);

        teil("Klang (Phase 14)");
        klang(r3, clock);

        teil("Regie und Kino (Phase 15)");
        regie(r3, clock);

        teil("Renderer.jar und Shader");
        List<String> pk = Bibliothek.pakete();
        check(Bibliothek.fehlend().isEmpty(), "Renderer.jar: " + Bibliothek.kurz());
        check(pk.contains("surf") && pk.contains("dune") && pk.contains("beacon"), "neu: " + Bibliothek.neuStand());
        Renderer rd = new Renderer(clock, t);
        for (String[] prog : rd.shaderFiles())
            for (String file : prog)
                check(Renderer.class.getResource("/com/dan/sylt/gl/shader/" + file) != null, "Shader " + file + " im Jar");
        for (String c : new String[]{"com.dan.surf.demo.SurfCheck", "com.dan.dune.demo.DuneCheck", "com.dan.tide.demo.TideCheck", "com.dan.beacon.demo.BeaconCheck", "com.dan.fracture.demo.FractureCheck",
                "com.dan.flock.demo.FlockCheck", "com.dan.flock.demo.WaderCheck", "com.dan.sky.demo.SkyCheck", "com.dan.weather.demo.WeatherCheck",
                "com.dan.rain.demo.RainCheck", "com.dan.fog.demo.FogCheck", "com.dan.lightning.demo.LightningCheck", "com.dan.mudflat.demo.MudflatCheck",
                "com.dan.seal.demo.SealCheck", "com.dan.soundscape.demo.SoundscapeCheck"}) {
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

    /**
     * Seit Phase 13: die Wetterregie (Lagen, freie Regie, „Ein Sturmtag“, von Hand, Ruhe während der Sturmflut), Regen
     * unter den Wolken und nasser Sand, Seenebel und Seerauch, Gewitter mit den Türmen als Fangpunkte, Möwen im
     * Hangaufwind, Vögel am Spülsaum mit Spuren, der seltene Seehund, Meeresleuchten, grünes Leuchten, HUD.
     */
    static void wetter(Renderer r, SceneClock clock) {
        com.dan.sylt.wiring.Wiring w = r.wiring;
        var rg = w.regie;
        r.sturmflut.aktiv = false;
        clock.lapse = 0;
        clock.set(java.time.LocalDate.of(2026, 9, 20), 14);
        w.tideAn = true;
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND);
        check(w.windSpeed == 6 && w.windFromDeg == 270 && w.oktas == 3 && w.cloudType == com.dan.cloud.CloudType.CUMULUS && Math.abs(w.highCover - 0.35) < 1e-9
                        && w.visibilityKm == 40 && w.surf.hs == 1.0 && w.surf.tp == 8 && w.surf.seaFromDeg == 285 && w.regen == 0 && w.nebel == 0,
                "Lage „Sommerabend“ (Vorgabe): dieselben Werte wie bis Phase 12 (Wind 6 m/s aus 270°, 3/8 Cumulus, Sicht 40 km, See 1,0 m, 8 s aus 285°)");
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.STURM);
        r.step(1 / 60.0);
        check(w.windSpeed == 22 && w.regen == 5 && w.surf.hs == 3.4 && w.oktas == 7.5, f("Lage „Sturm“: %.0f m/s aus %03.0f°, Regen %.0f mm/h, See %.1f m", w.windSpeed, w.windFromDeg, w.regen, w.surf.hs));
        // freie Regie über drei Tage (Uhr in Schritten von 20 min)
        rg.frei();
        java.util.Set<com.dan.weather.Weather.Phase> lagen = new java.util.HashSet<>();
        double pMin = 1e9, pMax = -1e9;
        for (int i = 0; i < 3 * 72; i++) {
            clock.setEpochMillis(clock.epochMillis() + 20 * 60_000);
            rg.schritt(0.1);
            lagen.add(rg.snapshot().phase());
            pMin = Math.min(pMin, rg.snapshot().pressure());
            pMax = Math.max(pMax, rg.snapshot().pressure());
        }
        check(lagen.size() >= 3 && pMax - pMin > 8, f("freie Regie über drei Tage: %d Lagen (%s), Luftdruck %.0f bis %.0f hPa", lagen.size(), lagen, pMin, pMax));
        clock.set(java.time.LocalDate.of(2026, 10, 20), 6);
        rg.sturmtag();
        clock.set(java.time.LocalDate.of(2026, 10, 20), 16);
        rg.schritt(0.1);
        check(rg.snapshot().phase() == com.dan.weather.Weather.Phase.STURM && w.windSpeed > 17, f("„Ein Sturmtag“ um 16 Uhr: %s", rg.zeile()));
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND);
        rg.hand();
        w.windSpeed = 13;
        r.step(1 / 60.0);
        check(w.windSpeed == 13, "von Hand: die Regie ruht, der Wert bleibt");
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.WESTWIND);
        r.sturmflutStarten(com.dan.sylt.scene.Sturmflut.STUNDE);
        r.step(1 / 60.0);
        check(w.windSpeed > 13.5 && w.regen > 0.4 && w.cloudType == com.dan.cloud.CloudType.STRATOCUMULUS, f("Sturmflut: der Fahrplan nach Xaver führt (Wind %.0f m/s, Schauer %.1f mm/h), die Regie ruht", w.windSpeed, w.regen));
        r.sturmflut.aktiv = false;
        clock.set(java.time.LocalDate.of(2026, 9, 20), 14);

        // Regen: Schauer unter Gewitterwolken auf einem Teil der Fläche, Landregen überall
        Unw u = new Unw(r);
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.SCHAUER);
        double teil = u.regenAnteil();
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.LANDREGEN);
        double land = u.regenAnteil();
        check(teil > 0.03 && teil < 0.7 && land > 0.85, f("Regen unter den Wolken: Schauer (5/8 Cumulonimbus) auf %.0f %% der Fläche in 40 km, Landregen (8/8 Nimbostratus) auf %.0f %%", teil * 100, land * 100));
        double sicht = com.dan.rain.Rain.visibilityKm(w.regen);
        check(sicht > 5 && sicht < 20, f("Sicht im Landregen (%.1f mm/h) %.1f km", w.regen, sicht));
        // nass nach Regen (Uhr läuft 60-fach)
        r.sand.regen = 0;
        clock.lapse = 60;
        r.placeView(com.dan.sylt.camera.Ansichten.SANDSTURM);
        for (int i = 0; i < 400; i++) r.step(0.5);
        clock.lapse = 0;
        check(r.sand.regen > 0.05, f("nach 200 min Landregen ist der Sand nass (%.1f %% Wasser, er fliegt nicht mehr)", r.sand.regen * 100));
        // Seenebel und Seerauch
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.SEENEBEL);
        r.step(1 / 60.0);
        com.dan.fog.Fog fg = r.unwetter.fog;
        double vMin = 1e9;
        double[] wl = Site.fromUtm(Site.wasserlinieE(6_089_500) - 200, 6_089_500);
        for (int i = 0; i < 40; i++) vMin = Math.min(vMin, fg.visibilityAt(wl[0] + i * 37, w.pegel + 2, wl[1] + i * 53));
        check(fg.amount > 0.9 && vMin < 300, f("Seenebel: Bänke bis %.0f m über dem Pegel, Sicht darin bis %.0f m", fg.top, vMin));
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.SEERAUCH);
        r.step(1 / 60.0);
        double oben = 1e9;
        for (int i = 0; i < 40; i++) oben = Math.min(oben, fg.visibilityAt(wl[0] + i * 37, w.pegel + 25, wl[1] + i * 53));
        check(fg.top < 12 && oben > 5000, f("Seerauch: flach (bis %.0f m), 25 m darüber klare Sicht (%.0f km)", fg.top, oben / 1000));
        w.windSpeed = 13;
        r.unwetter.schritt(0.1, w, r.wetter.clouds, true, r.camera.eye, r.terrain::height);
        check(fg.amount < 0.05, "Wind Bft 6: der Nebel ist fort");
        // Gewitter: Stufe aus der Lage, Blitz in den Kampener Leuchtturm
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.GEWITTER);
        r.step(1 / 60.0);
        com.dan.lightning.Lightning bl = r.unwetter.blitz;
        int hits = bl.attractorHits();
        bl.strikeAttractor(0);
        for (int i = 0; i < 30; i++) r.unwetter.schritt(1 / 30.0, w, r.wetter.clouds, true, r.camera.eye, r.terrain::height);
        check(bl.level() == com.dan.lightning.Lightning.Level.MODERATE && bl.attractorHits() > hits && bl.attractors.size() == 2,
                f("Gewitter über der See: Stufe %s, Fangpunkte %s, Blitz in den Kampener Leuchtturm", com.dan.sylt.gl.Unwetter.stufe(bl.level()), bl.attractors.stream().map(com.dan.lightning.Lightning.Attractor::name).toList()));
        // Möwen im Hangaufwind an der Kliffkante bei Westwind
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.WESTWIND);
        r.placeView(com.dan.sylt.camera.Ansichten.byName("Am Kliff"));
        com.dan.sylt.scene.Strandleben lb = r.leben;
        int soar = 0;
        for (int i = 0; i < 240 * 10; i++) {
            lb.schritt(0.1, r.camera.eye, r.camera.tx, r.camera.tz, 11, 260, 20_000, 30, clock.date(), 14, w.pegel, null, 2);
            if (i > 1800) for (com.dan.flock.Flock.Gull g : lb.moewen.gulls()) if (g.state == com.dan.flock.Flock.State.SOAR) soar++;
        }
        double mittel = soar / 600.0;
        check(lb.moewen.ridges.size() > 20 && mittel >= 3, f("Möwen bei Westwind 11 m/s: %d Hangkanten aus kante.csv, im Mittel %.1f Möwen im Hangaufwind vor der Kante", lb.moewen.ridges.size(), mittel));
        // Spülsaum: Stück der Nahbrandung gegenüber dem Auge, eine Minute
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND);
        r.placeView(com.dan.sylt.camera.Ansichten.byName("Spülsaum"));
        r.brandungJetzt();
        com.dan.sylt.scene.Nahbrandung.Stueck st = r.nah.stueck();
        if (st == null) { check(false, "Spülsaum: kein Stück der Nahbrandung"); }
        else {
            // ein Schritt setzt die Vögel ins Stück; dann steht der Betrachter 25 m landwärts eines Trupps Sanderlinge
            // (Spuren gibt es nur in 40 m um das Auge, weiter weg sind die Abdrücke nicht zu sehen)
            lb.schritt(1 / 30.0, r.camera.eye, r.camera.tx, r.camera.tz, 6, 270, 20_000, 30, clock.date(), 14, w.pegel, st, 1);
            double[] auge = r.camera.eye.clone();
            for (com.dan.flock.Waders.Group g : lb.strand.groups())
                if (g.species == com.dan.flock.WaderSpecies.SANDERLING) {
                    double ax = g.cx + com.dan.sylt.scene.Kueste.SX * 25, az = g.cz + com.dan.sylt.scene.Kueste.SZ * 25;
                    auge = new double[]{ax, r.groundHeight(ax, az) + 1.6, az};
                    break;
                }
            for (int i = 0; i < 60 * 30; i++) {
                r.nah.schritt(1 / 30.0, r.camera.eye, r.camera.tx, r.camera.tz, w.pegel);
                // baut die Nahbrandung neu (See oder Pegel der Lage), hier warten: sonst hängt der Wechsel des Stücks
                // von der Geschwindigkeit des Rechners ab
                while (r.nah.baut()) { try { Thread.sleep(2); } catch (InterruptedException e) { break; } }
                lb.schritt(1 / 30.0, auge, r.camera.tx, r.camera.tz, 6, 270, 20_000, 30, clock.date(), 14, w.pegel, r.nah.stueck(), 1);
            }
            int[] c = lb.strand.census();
            double gap = lb.strand.meanGap();
            check(c[0] > 10 && c[1] > 0 && !Double.isNaN(gap) && gap > -0.2 && gap < 2.5, f("Spülsaum im September: %s; Sanderlinge im Mittel %.2f m vor der Kante", lb.strand.describe(), gap));
            check(lb.strand.steps > 1000 && lb.spuren() > 50 && lb.spurenGeloescht > 0, f("Spuren im nassen Sand: %,d Tritte, %,d Abdrücke im Raster, %,d vom Schwall gelöscht", lb.strand.steps, lb.spuren(), lb.spurenGeloescht));
        }
        // Seehund: selten
        int da = 0;
        for (int hh = 0; hh < 600; hh++) {
            java.time.LocalDate d = java.time.LocalDate.of(2026, 6, 1).plusDays(hh / 24);
            lb.schritt(0.1, r.camera.eye, r.camera.tx, r.camera.tz, 6, 270, 20_000, 30, d, hh % 24 + 0.5, w.pegel, null, 1);
            if (lb.seehundDa()) da++;
        }
        check(da > 600 * 0.08 && da < 600 * 0.3, f("Seehund in %d von 600 Stunden (selten, etwa eine von sechs)", da));
        // Meeresleuchten: Augustnacht ja, Januarnacht und Tag nein
        clock.set(java.time.LocalDate.of(2026, 8, 15), 23.5);
        double aug = lum(r.meeresleuchtenJetzt());
        clock.set(java.time.LocalDate.of(2026, 1, 15), 23.5);
        double jan = lum(r.meeresleuchtenJetzt());
        clock.set(java.time.LocalDate.of(2026, 8, 15), 14);
        double tag = lum(r.meeresleuchtenJetzt());
        check(aug > 1e-3 && jan == 0 && tag == 0, f("Meeresleuchten: Augustnacht %.1e cd/m² im stürzenden Kamm, Januarnacht %.0f, am Tag nicht sichtbar", aug, jan));
        // grünes Leuchten: an rund jedem vierten klaren Abend mit Spiegelung
        int spiegel = 0, n0 = 0;
        for (int d = 0; d < 120; d++) {
            clock.set(java.time.LocalDate.of(2026, 6, 1).plusDays(d), 21);
            n0++;
            if (r.gruenesLeuchtenHeute() > 1.5) spiegel++;
        }
        w.regen = 3;
        double regenTag = r.gruenesLeuchtenHeute();
        w.regen = 0;
        check(spiegel > n0 * 0.12 && spiegel < n0 * 0.4 && regenTag == 1, f("grünes Leuchten: an %d von %d klaren Sommerabenden mit Spiegelung, im Regen nur die Brechung", spiegel, n0));
        // HUD
        // ohne Grafikkarte melden Regen, Nebel und Gewitter „!… nicht gestartet“, die Zeilen sind trotzdem da
        java.util.List<String> hud = java.util.Arrays.stream(r.hudLines()).map(l -> l.startsWith("!") ? l.substring(1) : l).toList();
        boolean alle = hud.stream().anyMatch(l -> l.startsWith("Wetter (Lage")) && hud.stream().anyMatch(l -> l.startsWith("Regen"))
                && hud.stream().anyMatch(l -> l.startsWith("Seenebel") || l.startsWith("Nebel")) && hud.stream().anyMatch(l -> l.startsWith("Gewitter"))
                && hud.stream().anyMatch(l -> l.startsWith("Licht:")) && hud.stream().anyMatch(l -> l.startsWith("Spuren im nassen Sand"));
        check(alle, "HUD: Zeilen für Wetter, Regen, Seenebel, Gewitter, Leben und Licht");
        check(com.dan.sylt.tools.Abbruch.STUNDE == 23, "Bildfolge des Abbruchs seit Phase 13 am Vormittag danach (Stunde 23, hell)");
        rg.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND);
    }

    /**
     * Phase 14: der Klang (com.dan.soundscape) ohne Lautsprecher. Am Spülsaum: Brandungslinie aus dem Wellenfeld, einzelne
     * Wellen, Vögel; auf der Uwe-Düne nur noch das Rollen; oben hinter der Kante dämpft sie; Flugsand im Sandsturm;
     * Abbruch und Donner verdrahtet; Rechenzeit.
     */
    static void klang(Renderer r, SceneClock clock) {
        com.dan.sylt.gl.Klang k = r.klang;
        com.dan.soundscape.Soundscape s = k.s;
        k.offline = true;
        s.autoGain = false;
        s.quality = com.dan.soundscape.Soundscape.Quality.MEDIUM;
        clock.set(java.time.LocalDate.of(2026, 9, 12), 18.5);
        clock.lapse = 1;
        r.wiring.regie.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND);
        r.placeView(com.dan.sylt.camera.Ansichten.byName("Spülsaum"));
        double x = r.camera.eye[0] + com.dan.sylt.scene.Kueste.SX * 6, z = r.camera.eye[2] + com.dan.sylt.scene.Kueste.SZ * 6;
        r.camera.place(x, Math.max(r.groundHeight(x, z), r.wiring.pegel) + 1.6, z, 200, 2);
        r.brandungJetzt();
        int w0 = k.schwallbrecher + k.sturzbrecher, sw0 = k.schwaelle, b0 = k.moewen + k.sanderlinge + k.austernfischer;
        long t0 = System.nanoTime();
        double[] lv = new double[com.dan.soundscape.Soundscape.PARTS.length];
        int n = 0;
        for (int i = 0; i < 60 * 40; i++) {
            r.step(1 / 60.0);
            s.render(1 / 60.0);
            if (i > 60 * 8) { double[] l = s.levels(); for (int q = 0; q < lv.length; q++) lv[q] += Math.pow(10, l[q] / 10); n++; }
        }
        double ms = s.msPerSecond();
        double fern = 10 * Math.log10(lv[12] / n), alles = 10 * Math.log10(lv[lv.length - 1] / n), ev = 10 * Math.log10(lv[14] / n);
        int wellen = k.schwallbrecher + k.sturzbrecher - w0, schw = k.schwaelle - sw0, voegel = k.moewen + k.sanderlinge + k.austernfischer - b0;
        check(k.stuecke > 100 && fern > 45 && fern < 80, f("Spülsaum, Sommerabend: Brandung fern %.0f dB aus %d Stücken (%.0f W/m vergehen gegenüber), am Ohr %.0f dB", fern, k.stuecke, k.wattsHier, alles));
        check(wellen >= 3 && schw >= 1 && ev > 40, f("einzelne Wellen am Ohr in 40 s: %d Brecher, %d Schwälle, Ereignisse %.0f dB", wellen, schw, ev));
        check(voegel >= 2, f("Vögel rufen: %d in 40 s (Möwen %d, Sanderlinge %d, Austernfischer %d)", voegel, k.moewen, k.sanderlinge, k.austernfischer));
        check(ms < 60, f("Rechenzeit des Klangs am Strand (32 kHz): %.0f ms je s Ton", ms));
        // fern: auf der Uwe-Düne nur das Rollen
        double strand = s.surfLineDb();
        r.placeView(com.dan.sylt.camera.Ansichten.byName("Uwe-Düne"));
        for (int i = 0; i < 90; i++) { r.step(1 / 60.0); s.render(1 / 60.0); }
        double[] bd = s.surfLineBandsDb();
        check(s.surfLineDb() < strand - 4 && bd[0] > bd[2] + 10, f("Uwe-Düne: Brandung fern %.0f dB (am Strand %.0f dB), Rollen %.0f dB über dem Zischen", s.surfLineDb(), strand, bd[0] - bd[2]));
        // hinter der Kante: oben 40 m landwärts der Kliffkante dämpft sie
        com.dan.sylt.scene.Kante kt = r.kliff.kante;
        int row = kt.row(com.dan.sylt.scene.Site.N0 - 6_089_500);
        if (row < 0) row = kt.n / 2;
        double hx = kt.kanteX(row) + com.dan.sylt.scene.Kueste.SX * 40, hz = kt.z(row) + com.dan.sylt.scene.Kueste.SZ * 40;
        int r2 = kt.row(hz);
        double hk = r2 < 0 ? 25 : kt.hKante[r2];
        // das Ohr 40 m landwärts der Kante, 1 m über ihrer Höhe (der Grund steigt dort meist an, dann sieht man über sie hinweg)
        double kd = k.kante(hx, hk + 1, hz, r.wiring.pegel), vor = k.kante(kt.kanteX(row) - com.dan.sylt.scene.Kueste.SX * 30, 2, kt.z(row) - com.dan.sylt.scene.Kueste.SZ * 30, r.wiring.pegel);
        check(kd < -3 && vor == 0, f("40 m hinter der Kliffkante, 1 m über ihr: die Kante nimmt %.0f dB (Beugung nach Maekawa); am Strand davor nichts", -kd));
        // Sandsturm: Flugsand am Ohr, Wind an der Kante
        r.sandsturm();
        r.sandJetzt();
        for (int i = 0; i < 60; i++) { r.step(1 / 60.0); s.render(1 / 60.0); }
        check(s.sandFlux > 0.005, f("Sandsturm: Flugsand am Ohr %.3f kg/(m·s), %s", s.sandFlux, s.describe()));
        // Abbruch und Donner sind verdrahtet; ein Abbruch klingt
        int a0 = k.abbrueche;
        if (r.abbruch != null && r.abbruch.onKlang != null) r.abbruch.onKlang.klang(0, r.camera.eye[0] + 40, 10, r.camera.eye[2], 300, 0);
        s.render(0.2);
        int voices = s.activeVoices();
        check(r.abbruch != null && k.abbrueche == a0 + 1 && voices > 0 && r.unwetter.blitz.onThunder != null, f("Abbruch klingt (%d Stimmen), Donner verdrahtet", voices));
        check(k.describe().startsWith("Klang") && java.util.Arrays.stream(r.hudLines()).anyMatch(l -> l.startsWith("Klang")), "HUD: Zeile für den Klang");
        k.offline = false;
        clock.lapse = 0;
        r.wiring.regie.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND);
        System.out.printf(Locale.GERMAN, "  (Klang gerechnet in %.1f s)%n", (System.nanoTime() - t0) / 1e9);
    }

    static double lum(double[] c) { return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]; }

    /** Prüfhilfe: Regen über einem Raster von 40 km um das Auge nach der Lage (die Zellen der Wolken liegen kilometerweit auseinander). */
    static final class Unw {
        final Renderer r;
        Unw(Renderer r) { this.r = r; }
        double regenAnteil() {
            r.step(1 / 60.0);
            r.wetter.clouds.step(1);
            r.unwetter.schritt(1 / 60.0, r.wiring, r.wetter.clouds, true, r.camera.eye, r.terrain::height);
            com.dan.rain.Rain.Curtain c = r.unwetter.rain.curtain;
            int nass = 0, n = 0;
            for (int i = -20; i <= 20; i++)
                for (int j = -20; j <= 20; j++) { n++; if (c == null || c.density(r.camera.eye[0] + i * 1000, r.camera.eye[2] + j * 1000) > 0.3) nass++; }
            return nass / (double) n;
        }
    }

    /** Phase 12: Xaver (F16 A), Hohlkehle, Abbruch nach der Physik (F17 A), Staub (F15 A), Sandbilanz, Abtrag, neue Wand. */
    static void sturmflut(Renderer r, SceneClock clock) {
        com.dan.sylt.scene.Sturmflut.Punkt hp = com.dan.sylt.scene.Sturmflut.an(12);
        check(hp.wind() == 27 && hp.richtung() == 300 && hp.hs() == 6 && com.dan.sylt.scene.Sturmflut.BOEEN * 3.6 > 135,
                f("Fahrplan nach Xaver: Höhepunkt Wind %.0f m/s aus %.0f°, H_s %.0f m, Böen %.0f km/h (belegt: NW Bft 9–10, Böen um 140 km/h, Wellen bis 6 m)",
                        hp.wind(), hp.richtung(), hp.hs(), com.dan.sylt.scene.Sturmflut.BOEEN * 3.6));
        com.dan.sylt.scene.Kliffabbruch k = r.abbruch;
        if (k == null) { check(false, "Kliffabbruch: keine Kante geladen"); return; }
        k.manuell = true;
        k.asyncSolve = false;
        // die Flut bis zum Morgen des 6. ohne Stücke: Pegel, Windstau, Kehle
        r.sturmflutStarten(com.dan.sylt.scene.Sturmflut.STUNDE);
        k.rechnen = false;
        double peak = -9, peakH = 0, kehle6 = 0, r2max = 0, stauPeak = 0;
        for (double h = 0.5; h <= 30; h += 0.5) {
            r.vorspulen(h, 5, false);
            if (r.wiring.pegel > peak) { peak = r.wiring.pegel; peakH = h; stauPeak = r.wiring.tide.surge; }
            r2max = Math.max(r2max, r.brandung.auflaufR2());
            if (Math.abs(h - 6) < 1e-9) kehle6 = k.tiefsteKehle();
        }
        check(peak > 3.0 && peak < 4.2 && peakH > 8 && peakH < 22, f("Pegel bis %+.2f m NHN in Stunde %.1f (Windstau dann %+.2f m; Xaver: über 3 m in der Nacht zum 6.)", peak, peakH, stauPeak));
        check(r2max > 1.4 && r2max < 2.5, f("Auflauf R2 %% bis %.2f m (Stockdon, H_s 6 m, T_p 12,5 s, Strand 1:30)", r2max));
        check(k.tiefsteKehle() > kehle6 + 1 && k.vKehle > 0, f("Hohlkehle: %.1f m nach 6 h, %.1f m nach 30 h (%.0f m³ ausgespült, ohne Abbruch)", kehle6, k.tiefsteKehle(), k.vKehle));
        double flutHoehe = peak + r2max;
        int erreicht = 0, nie = 0;
        for (int i = 0; i < k.kante.n; i++) if (k.kante.wand[i] >= 6) { if (k.kehle[i] > 0) erreicht++; else nie++; }
        check(erreicht > 0 && nie > 0, f("die See erreicht den Fuß an %d m der Wand, an %d m nicht (Flut bis %+.1f m)", erreicht, nie, flutHoehe));
        // der Abbruch an der Stelle der Bildfolge, auf dem Höhepunkt
        com.dan.sylt.scene.Kliffabbruch.Schnitt s = r.abbruchVorbereiten(com.dan.sylt.tools.Abbruch.STUNDE, com.dan.sylt.tools.Abbruch.NORD);
        check(s != null && s.gestaerkt > 0, s == null ? "kein Stück" : f("Stück bei N %,.0f: %d Zellen, %.0f m³; %d Flächen für den Bestand angehoben (ohne Kehle steht es)",
                k.kante.nord[s.mitte], s.solid.cells(), s.volumen0, s.gestaerkt));
        if (s == null) return;
        double kehleS = k.kehle[s.mitte];
        int[] staub = {0};
        java.util.function.Consumer<double[]> alt = k.onStaub;
        k.onStaub = q -> staub[0]++;
        double t = 0;
        while (k.schnitt() != null && t < 150) { k.physik(1 / 120.0); t += 1 / 120.0; }
        k.onStaub = alt;
        double[] b = k.bilanz();
        double rest = b[0] - b[1] - b[2] - b[3] - b[4];
        check(s.gebrochen() && b[2] + b[3] > 50, f("bricht, Ruhe nach %.0f s: %.0f m³ Blöcke, %.0f m³ zerfallen (Kehle %.1f m, L_c %.1f m)", t, b[2], b[3], kehleS, k.lc(s.mitte)));
        check(Math.abs(rest) < 1e-6 * b[0], f("Sandbilanz fest: %.1f m³ = %.1f steht + %.1f Blöcke + %.1f zerfallen + %.1f ausgespült (Rest %.1e m³)", b[0], b[1], b[2], b[3], b[4], rest));
        double soll = (b[2] + b[3]) * s.welt.material.bulking;
        check(Math.abs(k.vKegel - soll) < 0.01 * soll, f("Sturzkegel im Gelände %.1f m³ locker = (Blöcke + zerfallen) · %.2f = %.1f m³", k.vKegel, s.welt.material.bulking, soll));
        check(k.vAbgesenkt <= (b[2] + b[3] + b[4]) * 1.05 && k.vAbgesenkt > b[2] * 0.5,
                f("Gelände abgesenkt %.0f m³ (was fehlt: %.0f m³; unter dem Dach der Kehle bleibt die Oberfläche)", k.vAbgesenkt, b[2] + b[3] + b[4]));
        double dx = 0;
        for (int i = 0; i < 20; i++) {
            double x = s.x0 + (s.x1 - s.x0) * i / 19.0, z = (s.z0 + s.z1) / 2;
            dx = Math.max(dx, Math.abs(r.terrain.height(x, z) - r.terrain.roh(x, z) - k.aenderung.delta(x, z)));
        }
        check(dx < 1e-9, "Gelände = gemessen + Änderung (dieselbe Zahl geht als R32F-Textur an die Grafikkarte)");
        check(staub[0] > 0, f("Staub beim Aufschlag und Zerfall (F15 A): %d Stöße als Dünenkörner", staub[0]));
        int vorher = r.sand.koerner().count;
        int neu = r.sand.koerner().burst(s.x0, 5, (s.z0 + s.z1) / 2, 50, 2.5);
        check(neu > 0, f("Grains.burst: %d Körner aufgewirbelt (vorher %d)", neu, vorher));
        double abriss = k.abrissTiefe(s.r0, s.r1);
        check(abriss > 1, f("neue Wand: Abrisskante bis %.1f m hinter dem alten Fuß; Kante oben %.1f m zurück", abriss, k.rueckgang(s.r0, s.r1)));
        double kegel = k.vKegel;
        k.rechnen = false;
        r.vorspulen(r.sturmflut.stunden(clock) + 12, 5, false);
        check(k.vAbtrag > 0 && k.vAbtrag <= kegel + 1e-6, f("die nächste Flut trägt %.0f m³ vom Kegel ab (%.0f m³)", k.vAbtrag, kegel));
        k.rechnen = true;
        k.manuell = false;
        clock.set(SceneClock.START_DATE, SceneClock.START_HOUR);
        r.sturmflut.aktiv = false;
    }

    /**
     * Phase 15: die Regiebücher als Text, die Blicke und Fahrten, die Vorspülung, das Kino (Vor- und Abspann, Schnitte,
     * Ton), die Vögel im Gewitter; dann alle vier Regiebücher ohne Bild bis zum Ende, ohne Eingriff und ohne dass ein
     * {@code warte} ausweichen muss (DoD); dabei Serie, Fahrt mit der Welle, Abbruch mit Spur und Findlingen.
     */
    static void regie(Renderer r, SceneClock clock) {
        java.util.List<com.dan.sylt.scene.Regie.Book> bs = com.dan.sylt.scene.Regie.all();
        StringBuilder nm = new StringBuilder();
        int fotos = 0;
        boolean blicke = true;
        java.util.Set<String> fahrten = new java.util.TreeSet<>();
        String fehlt = "";
        for (com.dan.sylt.scene.Regie.Book b : bs) {
            nm.append(nm.length() > 0 ? ", " : "").append(b.name());
            fotos += b.fotos();
            for (com.dan.sylt.scene.Regie.Zeile z : b.zeilen()) {
                if (z.befehl().equals("ansicht") && !r.placeNamed(z.argumente())) { blicke = false; fehlt += " " + z.argumente(); }
                if (z.befehl().equals("fahrt")) fahrten.add(com.dan.sylt.scene.Regie.plainName(z.argumente()));
                if (z.befehl().equals("lage") && Renderer.lageNachName(z.argumente()) == null) { blicke = false; fehlt += " Lage " + z.argumente(); }
            }
        }
        check(bs.size() == 4 && fotos >= 30, f("4 Regiebücher als Text (scene/regie/*.txt): %s; %d Fotos", nm, fotos));
        check(blicke, "alle Ansichten und Lagen der Regiebücher gibt es" + (fehlt.isEmpty() ? " (Strandkörbe, Strandläufer, Sonnenuntergang, Vor dem Kliff, Abbruch, Vorspülung dazu)" : ": es fehlen" + fehlt));
        check(fahrten.equals(new java.util.TreeSet<>(java.util.List.of("drohne", "moewe", "treppe", "welle"))), "Fahrten der Regiebücher: " + fahrten);
        boolean falsch = false;
        try { com.dan.sylt.scene.Regie.parse("Regiebuch: X\n0:00 tanze Kliff\n"); } catch (IllegalArgumentException e) { falsch = e.getMessage().startsWith("Zeile 2"); }
        check(falsch, "ein falscher Befehl nennt die Zeile");
        com.dan.sylt.camera.Fahrt tr = r.treppenFahrt();
        double[] t0 = tr == null ? null : tr.at(0), t1 = tr == null ? null : tr.at(tr.length);
        check(tr != null && t0[1] - t1[1] > 15 && tr.length > 40, tr == null ? "Treppe: kein Weg" : f("Fahrt „Treppe“: %.0f m von %.1f m auf %.1f m NHN hinunter in %.0f s", tr.length, t0[1], t1[1], tr.duration));
        com.dan.sylt.camera.Fahrt dr = r.drohnenFahrt();
        check(dr.length > 1900 && dr.length < 2600, f("Fahrt „Drohne“: %.0f m längs des Kliffs, 45 m hoch, %.0f s", dr.length, dr.duration));

        // Vögel im Gewitter (Befund aus Phase 14: im Gewitter hört man keine Vögel)
        r.wiring.regie.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND);
        r.step(1 / 30.0);
        double ruhig = r.klang.rufe();
        r.wiring.regie.lage(com.dan.sylt.wiring.Wetterregie.Lage.GEWITTER);
        r.step(1 / 30.0);
        double gew = r.klang.rufe();
        check(ruhig > 0.9 && gew < 0.1, f("Vögel rufen am Sommerabend zu %.0f %%, im Gewitter zu %.0f %% (nach einem Donner 30 s gar nicht)", ruhig * 100, gew * 100));

        // Vorspülung: aufspülen, dann trägt die Brandung ab
        com.dan.sylt.scene.Vorspuelung v = r.vorspuelung;
        if (v == null) check(false, "Vorspülung: keine Kante");
        else {
            com.dan.sylt.scene.Kante kn = r.kliff.kante;
            int row = Renderer.zeile(kn, 6_089_550);
            double fx = kn.fussX(row) - 10, fz = kn.z(row), h0 = r.terrain.height(fx, fz);
            double ms = clock.epochMillis();
            v.starten(ms);
            for (int i = 0; i <= 80; i++) v.schritt(1, ms + i * 3_600_000.0, 1.2);
            double h1 = r.terrain.height(fx, fz), vol = v.volumen;
            check(v.fertig() && vol > 35_000 && vol < 60_000 && h1 - h0 > 1.5, f("Vorspülung in 72 h aufgespült: %.0f m³ auf 600 m, 10 m vor dem Fuß %.1f m höher · %s", vol, h1 - h0, v.describe()));
            for (int i = 1; i <= 60; i++) v.schritt(1, ms + (80 + i * 24) * 3_600_000.0, 1.2);
            double halb = v.volumen;
            check(halb > 0.4 * vol && halb < 0.6 * vol, f("bei H_s 1,2 m nach 60 Tagen noch %.0f %% (Halbwert 60 Tage)", 100 * halb / vol));
            v.entfernen();
            check(Math.abs(r.terrain.height(fx, fz) - h0) < 0.01, "Vorspülung wieder entfernt (Gelände wie vorher)");
        }

        // Kino: Vorspann, Schnitte mit Überblendung, Untertitel, Abspann, Ton folgt dem Schwarz
        com.dan.sylt.scene.Regie.Book probe = com.dan.sylt.scene.Regie.parse("Regiebuch: Probe\nBeschreibung: Kino ohne Bild\n0:00 zeit 20.07.2026 21:00 | Anfang\n"
                + "0:00 ansicht Kliffkante\n0:06 ansicht Spülsaum | Schnitt\n0:12 sage Ende\nLänge: 0:14\n");
        double vol0 = r.klang.s.volume;
        r.kinoStart(probe);
        java.util.Set<com.dan.kino.Kino.Lage> lagen = java.util.EnumSet.noneOf(com.dan.kino.Kino.Lage.class);
        double tonMin = 9;
        for (int i = 0; i < 60 * 40 && (i < 10 || r.kino.laeuft()); i++) {
            r.step(1 / 30.0);
            r.bildlos();
            lagen.add(r.kino.lage());
            if (r.kino.sichtbar()) tonMin = Math.min(tonMin, r.klang.s.volume / Math.max(1e-9, vol0));
        }
        check(!r.kino.laeuft() && lagen.containsAll(java.util.List.of(com.dan.kino.Kino.Lage.VORSPANN, com.dan.kino.Kino.Lage.FILM, com.dan.kino.Kino.Lage.ABSPANN))
                        && Math.abs(r.klang.s.volume - vol0) < 1e-9 && tonMin < 0.3,
                f("Kino: %s, Ton im Schwarz bis %.0f %%, danach wie vorher", lagen, tonMin * 100));

        // alle vier Regiebücher ohne Eingriff bis zum Ende (DoD)
        r.ohneBlatt = true;
        int abbr0 = r.abbruch != null ? r.abbruch.abbrueche : 0;
        for (com.dan.sylt.scene.Regie.Book b : bs) {
            long ns = System.nanoTime();
            r.regieStart(b);
            int n = 0;
            java.util.Set<com.dan.sylt.scene.Wellenritt.Abschnitt> ritt = java.util.EnumSet.noneOf(com.dan.sylt.scene.Wellenritt.Abschnitt.class);
            while ((r.regie.running() || r.regieWantsFrame()) && n < 30_000) {
                r.step(0.1);
                r.physik.drain();
                r.bildlos();
                if (r.ritt.laeuft()) ritt.addAll(r.ritt.erlebt);
                n++;
            }
            ritt.addAll(r.ritt.erlebt);
            int stichworte = b.cues().size();
            check(!r.regie.running() && r.regie.fired() == stichworte && r.regie.ausgewichen == 0 && r.fotosVerlangt == b.fotos(),
                    f("„%s“ läuft ohne Eingriff durch: %d Stichworte, %d Fotos, %.0f s gewartet, %d Mal ausgewichen (%.0f s Rechenzeit) · %s", b.name(), r.regie.fired(), r.fotosVerlangt,
                            r.regie.waitedTotal(), r.regie.ausgewichen, (System.nanoTime() - ns) / 1e9, r.regie.describe()));
            if (b.name().contains("Welle"))
                check(r.serie.serien > 0 && ritt.contains(com.dan.sylt.scene.Wellenritt.Abschnitt.KAMM) && ritt.contains(com.dan.sylt.scene.Wellenritt.Abschnitt.ENDE)
                                && (ritt.contains(com.dan.sylt.scene.Wellenritt.Abschnitt.WALZE) || ritt.contains(com.dan.sylt.scene.Wellenritt.Abschnitt.SCHWALL)),
                        f("Mit der Welle: %s · Abschnitte %s", r.serie.describe(), ritt));
        }
        if (r.abbruch != null)
            check(r.abbruch.abbrueche > abbr0 && !r.abbruch.spur.isEmpty() && !r.kliff.neueFindlinge.isEmpty(),
                    f("Die Spur der Eiszeit: %d Abbrüche in den Regiebüchern, %s", r.abbruch.abbrueche - abbr0, r.spurLine()));
        r.ohneBlatt = false;
        r.camera.diving = false;
        r.camera.minEye = com.dan.sylt.camera.Camera.MIN_EYE;
        if (r.vorspuelung != null) r.vorspuelung.entfernen();
        clock.lapse = 1;
        clock.set(SceneClock.START_DATE, SceneClock.START_HOUR);
        r.sturmflut.aktiv = false;
        r.wiring.regie.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND);
    }

    static void teil(String t) { System.out.println("\n— " + t); }

    static void check(boolean c, String what) {
        n++;
        if (!c) bad++;
        System.out.println((c ? "  ok    " : "  FEHLER ") + what);
    }

    private Pruefung() { }
}
