package com.dan.sylt.gl;

import com.dan.cliff.Blockfield;
import com.dan.cliff.Cliff;
import com.dan.cliff.CliffMap;
import com.dan.cliff.Sod;
import com.dan.cliff.StrataMap;
import com.dan.cliff.gl.CliffGl;
import com.dan.sylt.scene.Kante;
import com.dan.sylt.scene.Terrain;
import com.jogamp.opengl.GL4;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Phase 4a: das Rote Kliff in der Szene mit {@code com.dan.cliff} (Lockergestein, {@link Cliff#rotesKliff()}).
 * <ul>
 * <li><b>Wandkarte</b> über dem Kern zu 2 m aus dem DGM1; Wand und Sturzkegel nur im Band des Kliffs (die Kante aus
 * {@code daten/kliff/kante.csv} mit Rand), sonst blieben steile Dünen Lehmwände.</li>
 * <li><b>Schichten je Ort</b> ({@link StrataMap} zu 1 m über dem Streifen): die Oberkante aus dem Gelände, die
 * Oberfläche des Kaolinsands [G: im Süden bei Wenningstedt bis +7 m NHN über dem Strand, nach Norden unter den Strand
 * abtauchend], der Flugsand oben um so mächtiger, je niedriger die Kante (im Norden liegen Dünen auf dem Kliff) [G].</li>
 * <li><b>Grasnarbe</b> als Überhang entlang der Kante, wo die Wand mindestens 6 m hoch ist.</li>
 * <li><b>Findlinge</b> am Fuß: Blöcke aus dem Geschiebelehm, die die Brandung freigespült hat (Granit, Porphyr, Flint),
 * locker auf dem Strand vor der Wand [G].</li>
 * </ul>
 * Der Strandhafer am Fuß wächst über {@code ground} ({@link com.dan.sylt.wiring.Bodendecke} fragt {@link #talus}).
 */
public final class Kliff {
    public final Cliff cliff = Cliff.rotesKliff();
    /** Die Findlinge: Granit, grauer als am Brocken, ohne Flechten (die Brandung schleift sie). */
    public final Cliff findlingFels = Cliff.granit();
    public final Kante kante;
    public final CliffMap map;
    public final StrataMap strata;
    public final List<Sod> sods = new ArrayList<>();
    public final Blockfield findlinge = new Blockfield();
    final CliffGl gl = new CliffGl();
    boolean ok;
    String check = "";
    /** Einheiten der Wandkarte und der Schichtkarte im Geländeprogramm (die Rauschtextur liegt fest auf 15). */
    public static final int MAP_UNIT = 2, STRATA_UNIT = 3;
    /** Rand des Bands um Fuß und Kante (m) für Wand und Sturzkegel. */
    public static final double RAND = 12, RAND_FUSS = 25;
    /** Zellen der Wandkarte (m) und ihre Zahl je Seite (über den Kern). */
    public static final double MAP_CELL = 2;
    long buildMs;

    public Kliff(Terrain t, Kante k) {
        long t0 = System.nanoTime();
        this.kante = k;
        // das DGM1 zu 1 m glättet die Wand: zwischen Fuß und Kante steiler als 41° nur ein Zehntel, darum Fels ab 30°,
        // Wand ab 38° [G, gemessen am Band: Median 18°, 90 % unter 41°]
        cliff.rockSlopeDeg = 30;
        cliff.wallSlopeDeg = 38;
        findlingFels.lichenAmount = 0.1;
        findlingFels.mossAmount = 0;
        findlingFels.gneissLight = new double[]{0.30, 0.28, 0.26};
        findlingFels.gneissDark = new double[]{0.14, 0.13, 0.12};
        if (k == null || !t.hasKern()) {
            map = null;
            strata = null;
            return;
        }
        Cliff.Heights h = t::roh;
        int n = (int) Math.round(Terrain.KN * Terrain.KCELL / MAP_CELL);
        CliffMap m = CliffMap.build(cliff, h, Terrain.KX0, Terrain.KZ0, MAP_CELL, n, 4, 18);
        // Wand und Geröll nur im Band des Kliffs
        for (int j = 0; j < n; j++) {
            double z = m.cz(j);
            int r = k.row(z);
            for (int i = 0; i < n; i++) {
                int q = (j * n + i) * 4;
                double x = m.cx(i);
                boolean wandHier = false, fussHier = false;
                if (r >= 0) {
                    double lo = Math.min(k.fussX(r), k.kanteX(r)), hi = Math.max(k.fussX(r), k.kanteX(r));
                    wandHier = x >= lo - RAND && x <= hi + RAND;
                    fussHier = x >= lo - RAND_FUSS && x <= hi + RAND;
                }
                if (!wandHier) { m.rgba[q] = 0; m.rgba[q + 3] = 0; }
                if (!fussHier) m.rgba[q + 1] = 0;
            }
        }
        map = m;
        // Schichtkarte über dem Streifen (1 m)
        double xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE;
        for (int i = 0; i < k.n; i++) {
            xmin = Math.min(xmin, Math.min(k.fussX(i), k.kanteX(i)));
            xmax = Math.max(xmax, Math.max(k.fussX(i), k.kanteX(i)));
        }
        double x0 = Math.floor(xmin - 60), x1 = Math.ceil(xmax + 40), z0 = Math.floor(k.z(0) - 40), z1 = Math.ceil(k.z(k.n - 1) + 40);
        strata = StrataMap.build(h, x0, z0, 1, (int) (x1 - x0), (int) (z1 - z0), 25,
                (x, z) -> kaolinOben(k, z), (x, z) -> flugsand(k, z), 1.5);
        cliff.strata = strata;
        sodsBauen(h);
        // Findlinge am Fuß: auf dem trockenen Strand bis 15 m vor der Wand, locker
        findlinge.ground = h;
        findlinge.seed = 2026;
        findlinge.perM2 = 0.06;
        findlinge.sizeMedian = 0.55;
        findlinge.sizeSpread = 0.5;
        findlinge.sink = 0.35;
        findlinge.radius = 300;
        findlinge.density = (x, z) -> {
            int r = k.row(z);
            if (r < 0 || k.wand[r] < 8) return 0;
            double vor = k.fussX(r) - x;   // m vor dem Fuß (zur See)
            return vor < -2 || vor > 15 ? 0 : 0.35 * (1 - vor / 15);
        };
        buildMs = (System.nanoTime() - t0) / 1_000_000;
    }

    /** Oberfläche des Kaolinsands (m NHN) an z [G]: im Süden bei Wenningstedt +7 m, nach Norden bis N 6 089 700 unter den Strand. */
    static double kaolinOben(Kante k, double z) {
        double nord = com.dan.sylt.scene.Site.N0 - z;
        double t = Math.max(0, Math.min(1, (nord - 6_088_700) / 1_000.0));
        return 7 - 10 * t;
    }

    /** Flugsand oben (m) an z [G]: 1 m über der hohen Wand, bis 4 m, wo die Kante unter 10 m liegt (Dünen auf dem Kliff). */
    static double flugsand(Kante k, double z) {
        int r = k.row(z);
        double hk = r < 0 ? 15 : k.hKante[r];
        return 1 + 3 * Cliff.smooth(22, 10, hk);
    }


    /** Grasnarbe: entlang der Kante, wo die Wand mindestens 6 m hoch ist, je 2 m ein Punkt (seit Phase 12 auch nach einem Abbruch). */
    void sodsBauen(Cliff.Heights h) {
        Kante k = kante;
        sods.clear();
        List<Double> seg = new ArrayList<>();
        int seed = 11;
        for (int i = 0; i < k.n; i += 2) {
            boolean an = k.wand[i] >= 6 && (i < 2 || k.nord[i - 2] - k.nord[i] < 4);   // an Lücken der Kante neu ansetzen
            if (an) {
                int a = Math.max(0, i - 3), b = Math.min(k.n - 1, i + 3);
                double tx = k.kanteX(b) - k.kanteX(a), tz = k.z(b) - k.z(a), tl = Math.hypot(tx, tz);
                double ox = tl > 0 ? tz / tl : -1, oz = tl > 0 ? -tx / tl : 0;
                if (ox > 0) { ox = -ox; oz = -oz; }   // aus der Wand heraus: zur See (Westen)
                double x = k.kanteX(i), z = k.z(i);
                seg.add(x); seg.add(h.height(x, z)); seg.add(z); seg.add(ox); seg.add(oz);
            }
            if ((!an || i + 2 >= k.n) && seg.size() >= 20) {
                double[] e = new double[seg.size()];
                for (int q = 0; q < e.length; q++) e[q] = seg.get(q);
                Sod s = new Sod(e, seed++);
                s.reach = 0.55;
                s.gaps = 0.3;
                sods.add(s);
            }
            if (!an) seg.clear();
        }
    }

    private volatile boolean mapNeu;

    /**
     * Seit Phase 12: nach einem Abbruch die Wandkarte im Rechteck aus dem neuen Gelände (mit der {@link Terrain.Korrektur})
     * neu rechnen und die Grasnarbe an die neue Kante legen; hochgeladen wird im nächsten {@link #bind}.
     */
    public void erneuern(double ax, double az, double bx, double bz, Cliff.Heights hNeu) {
        if (map == null) return;
        int i0 = Math.max(0, (int) Math.floor((ax - map.x0) / map.cell) - 12), j0 = Math.max(0, (int) Math.floor((az - map.z0) / map.cell) - 12);
        int i1 = Math.min(map.n - 1, (int) Math.ceil((bx - map.x0) / map.cell) + 12), j1 = Math.min(map.n - 1, (int) Math.ceil((bz - map.z0) / map.cell) + 12);
        int nn = Math.max(i1 - i0, j1 - j0) + 1;
        CliffMap teil = CliffMap.build(cliff, hNeu, map.x0 + i0 * map.cell, map.z0 + j0 * map.cell, map.cell, nn, map.talusDrop, map.talusReach);
        // der Rand des Ausschnitts (6 Zellen) bleibt, wie er war: dort fehlt dem Ausschnitt die Nachbarschaft
        for (int j = j0 + 6; j <= j1 - 6; j++)
            for (int i = i0 + 6; i <= i1 - 6; i++) {
                int q = (j * map.n + i) * 4, p = ((j - j0) * nn + (i - i0)) * 4;
                double z = map.cz(j), x = map.cx(i);
                int r = kante.row(z);
                boolean wandHier = false, fussHier = false;
                if (r >= 0) {
                    double lo = Math.min(kante.fussX(r), kante.kanteX(r)), hi = Math.max(kante.fussX(r), kante.kanteX(r));
                    wandHier = x >= lo - RAND && x <= hi + RAND;
                    fussHier = x >= lo - RAND_FUSS && x <= hi + RAND;
                }
                map.rgba[q] = wandHier ? teil.rgba[p] : 0;
                map.rgba[q + 1] = fussHier ? teil.rgba[p + 1] : 0;
                map.rgba[q + 3] = wandHier ? teil.rgba[p + 3] : 0;
            }
        sodsBauen(hNeu);
        mapNeu = true;
    }

    /** Sturzkegel am Ort 0..1 (für den Strandhafer am Fuß); 0 ohne Karte. */
    public double talus(double x, double z) { return map == null ? 0 : map.valuesAt(x, z)[1]; }

    /** Wand am Ort 0..1 aus der Karte. */
    public double wall(double x, double z) { return map == null ? 0 : map.valuesAt(x, z)[0]; }

    void init(GL4 g) {
        if (map == null) return;
        try {
            gl.init(g);
            gl.upload(g, map);
            gl.strataUnit = STRATA_UNIT;
            gl.setSods(sods);
            ok = true;
            System.out.println("Sylt: Kliff " + describe());
        } catch (RuntimeException e) {
            System.err.println("Sylt: Kliff nicht verfügbar: " + e.getMessage());
        }
    }

    /** Die Uniforms im laufenden Geländeprogramm setzen. */
    void bind(GL4 g, int program) {
        if (!ok) return;
        gl.strataUnit = STRATA_UNIT;
        if (mapNeu) {
            mapNeu = false;
            gl.upload(g, map);
            gl.setSods(sods);
        }
        gl.bind(g, program, cliff, MAP_UNIT);
    }

    /** Grasnarbe und Findlinge zeichnen (Licht vorbelichtet wie das Gelände). */
    void drawExtras(GL4 g, CliffGl.View v, CliffGl.Light l) {
        if (!ok) return;
        gl.drawSods(g, cliff, v, l);
        gl.drawBlocks(g, findlingFels, findlinge, v, l);
    }

    String describe() {
        if (map == null) return "!Kliff: keine Kante (daten/kliff/kante.csv) – Wand aus der Neigung wie in Phase 3";
        int pts = 0;
        for (Sod s : sods) pts += s.points();
        return String.format(Locale.GERMAN, "%s · %s · %s · Grasnarbe %d Stücke, %.1f km · %d Findlinge in Sicht · %d ms%s",
                kante.describe(), map.describe(), strata.describe(), sods.size(), pts * 2 / 1000.0, findlinge.count(), buildMs,
                check.isEmpty() ? "" : " · " + check);
    }

    /** Kurz für das HUD (die volle Zeile kostete auf der Radeon 4 ms Text je Bild). */
    String hudLine() {
        if (map == null) return describe();
        int pts = 0;
        for (Sod s : sods) pts += s.points();
        double wm = 0;
        for (double w : kante.wand) wm = Math.max(wm, w);
        return String.format(Locale.GERMAN, "Kante %.1f km, Wand bis %.0f m · Grasnarbe %.1f km · %d Findlinge in Sicht%s",
                kante.n / 1000.0, wm, pts * 2 / 1000.0, findlinge.count(), check.isEmpty() ? "" : " · " + check);
    }

    void dispose(GL4 g) { if (ok) gl.dispose(g); }
}
