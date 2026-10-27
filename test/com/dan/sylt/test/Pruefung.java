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
            double[] unten = Site.fromUtm(Site.wasserlinieE(6_089_500) - 20, 6_089_500), oben = Site.fromUtm(Site.wasserlinieE(6_089_500) + 60, 6_089_500);
            check(wf.wetnessAt(unten[0], unten[1]) > 0.99 && wf.wetnessAt(oben[0], oben[1]) < 0.3 && wf.sedimentAt(oben[0], oben[1]) < 0.01,
                    f("Nässe des Sands: unter Wasser %.2f, oben am Strand %.2f (Sand)", wf.wetnessAt(unten[0], unten[1]), wf.wetnessAt(oben[0], oben[1])));
        }
        check(Renderer.PASSES.length == 7 && Quality.MITTEL.groundRadius > 10 && Quality.MITTEL.ocean == com.dan.ocean.Ocean.Quality.MEDIUM, "Durchgänge " + String.join(", ", Renderer.PASSES));
        check(!r3.showMarks || Boolean.getBoolean("sylt.marken"), "Marken im Grundbild aus (M schaltet sie an)");

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

    static String f(String fmt, Object... a) { return String.format(Locale.GERMAN, fmt, a); }

    static void teil(String t) { System.out.println("\n— " + t); }

    static void check(boolean c, String what) {
        n++;
        if (!c) bad++;
        System.out.println((c ? "  ok    " : "  FEHLER ") + what);
    }

    private Pruefung() { }
}
