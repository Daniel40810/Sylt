package com.dan.sylt.scene;

import com.dan.surf.Bars;
import com.dan.surf.Surf;
import com.dan.surf.Swash;
import com.dan.surf.WaveField;

import java.util.Locale;

/**
 * Die Brandung in der Szene (seit Phase 7a): das Wellenfeld aus {@code com.dan.surf} vor dem ganzen Kern, auf dem
 * Grund aus dem Gelände mit Riffen und Rinnen ({@link Bars}, F10 A) und gerechnet im eigenen Faden, sobald sich See,
 * Pegel oder Grund ändern (F7 A). Aus jedem Ergebnis wird eine Textur gebacken, die {@code com.dan.ocean} als „Brandung
 * von außen“ zeichnet (Übergabe von {@code ocean} an {@code surf}): je Zelle der Anteil der Wellen, die als Walze laufen
 * (Weißwasser: aus dem Anteil brechender Wellen Q_b seewärts aufsummiert, im Trog vergehend), die Phase der
 * Wellen (die Walzen laufen damit zur Küste, je Periode eine), der Schaumteppich hinter den Brechern und der Set-up;
 * dazu die mittlere Strömung (Rippströme, Längsstrom) als zweite Textur. Nahe am Auge zeichnet {@link Nahbrandung} die
 * Brecher als Netz mit Auflauf.
 * <p>
 * Das Feld: Rand zur See 1 300 m vor der Wasserlinie (rund −9 m NHN), 200 m landwärts, längs 4,8 km um die Mitte des
 * Kerns, Zellen zu 8 m, Blick zur Küste {@link Kueste#SHOREWARD_DEG}.
 */
public final class Brandung {
    public static final double SEE = 1300, LAND = 200, LAENGS = 4800, ZELLE = 8;
    /** Textur: Kantenlänge der Zelle (m). */
    public static final double TEX_ZELLE = 6;
    /** Hang des Strands für die Faustformel des Auflaufs (Stockdon) [G: Vorstrand am Roten Kliff rund 1:30]. */
    public static final double STRANDHANG = 1 / 30.0;
    /** Strömung nur bis so weit (m) von der Brandung und höchstens so schnell (m/s) [G]. */
    public static final double STROM_WEITE = 120, STROM_MAX = 1.2;
    /** Glättung des Grunds für die Brandung (m Abstand der Nachbarn) [G]. */
    public static final double GLAETTEN = 12;
    /**
     * Weißwasser [G]: auf WEISS_LAENGE m Brechzone mit Q_b = 1 ist rund 63 % der Wellen gebrochen; über WIEDER_LAENGE m ohne
     * Brechen fangen sie sich wieder (im Trog hinter einem Riff).
     */
    public static final double WEISS_LAENGE = 25, WIEDER_LAENGE = 120;
    /** Länge, über die der Schaum hinter den Brechern zerfällt (m) [G]. */
    public static final double SCHAUM_LAENGE = 70;

    public final Surf surf;
    public final Bars riffe = Bars.sylt();
    public final WaveField feld;
    public final Buhnen buhnen;
    private final Terrain terrain;

    public final int texN;
    public final double texX0, texZ0, texSize;
    /** Gebackene Texturen: RGBA (Q_b, Phase, Schaum, Set-up) und RG (Strömung x, z); mit Version. */
    private volatile float[] tex, flow;
    private volatile int texVersion;
    private int bakedFrom = -1;
    private volatile Thread baker;
    public volatile double bakeMs;

    public Brandung(Surf surf, Terrain t) {
        this.surf = surf;
        this.terrain = t;
        riffe.seed = 2026;
        Bars.Coast coast = new Bars.Coast() {
            public double offshore(double x, double z) { return Kueste.offshore(x, z); }
            public double along(double x, double z) { return Kueste.along(x, z); }
        };
        // der Grund der Brandung: das Gelände über ±12 m geglättet (das DGM1 und die BAW tragen Höcker von wenigen Metern,
        // an denen ein Feld mit einer Richtung und einer Periode Brennlinien und Schatten bildet), dazu Riffe und Rinnen
        Surf.Bed glatt = (x, z) -> {
            double s = 0;
            for (int a = -1; a <= 1; a++)
                for (int c = -1; c <= 1; c++) s += t.height(x + a * GLAETTEN, z + c * GLAETTEN) * (a == 0 && c == 0 ? 4 : a == 0 || c == 0 ? 2 : 1);
            return s / 16;
        };
        surf.bed = riffe.apply(glatt, coast);
        double[] w0 = Site.fromUtm(Site.wasserlinieE(6_089_800), 6_089_800);
        double x0 = w0[0] - SEE * Kueste.SX - LAENGS / 2 * Kueste.AX, z0 = w0[1] - SEE * Kueste.SZ - LAENGS / 2 * Kueste.AZ;
        feld = new WaveField(surf, x0, z0, Kueste.SHOREWARD_DEG, SEE + LAND, LAENGS, ZELLE);
        // eine echte See streut in der Richtung und wird gebeugt: seitlich glätten [G, so dass an keinem Querprofil des
        // Kerns die Brandung ganz ausbleibt]
        feld.spread = 0.5;
        feld.kvSmooth = 0.5;
        buhnen = new Buhnen(t);
        // Textur: das Quadrat um das Feld
        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (int i : new int[]{0, feld.nu - 1})
            for (int j : new int[]{0, feld.nv - 1}) {
                minX = Math.min(minX, feld.x(i, j)); maxX = Math.max(maxX, feld.x(i, j));
                minZ = Math.min(minZ, feld.z(i, j)); maxZ = Math.max(maxZ, feld.z(i, j));
            }
        double size = Math.max(maxX - minX, maxZ - minZ);
        texN = (int) Math.ceil(size / TEX_ZELLE);
        texSize = texN * TEX_ZELLE;
        texX0 = (minX + maxX - texSize) / 2;
        texZ0 = (minZ + maxZ - texSize) / 2;
    }

    /** Ein Schritt: das Feld neu, wenn sich etwas geändert hat (eigener Faden), danach backen (eigener Faden). */
    public void update(double t) {
        feld.update(t);
        WaveField.Result r = feld.result();
        if (r != null && r.version != bakedFrom && baker == null) {
            bakedFrom = r.version;
            Thread th = new Thread(() -> {
                try { bake(r); } finally { baker = null; }
            }, "brandung-backen");
            th.setDaemon(true);
            baker = th;
            th.start();
        }
    }

    /** Für Prüfung und Standbild: jetzt rechnen und backen (im aufrufenden Faden). */
    public void computeNow(double t) {
        WaveField.Result r = feld.compute(t);
        bakedFrom = r.version;
        bake(r);
    }

    public float[] texture() { return tex; }
    public float[] flowTexture() { return flow; }
    public int textureVersion() { return texVersion; }

    /** Phase (Wellenlängen ab dem Rand zur See) und Schaum je Zelle des Felds, dann auf das Raster der Textur. */
    void bake(WaveField.Result r) {
        long t0 = System.nanoTime();
        int nu = r.nu, nv = r.nv;
        float[] phase = new float[nu * nv], foam = new float[nu * nv], eta = new float[nu * nv], weiss = new float[nu * nv];
        double tp = r.tp;
        for (int j = 0; j < nv; j++) {
            // Phase am Rand zur See: längs mit k·sin θ aufsummiert
            if (j > 0) {
                double k0 = Surf.wavenumber(tp, Math.max(0.3, r.depth[(j - 1)]));
                phase[j] = (float) (phase[j - 1] + (Double.isNaN(k0) ? 0 : k0 * Math.sin(r.theta[j - 1]) * ZELLE / (2 * Math.PI)));
            }
        }
        for (int j = 0; j < nv; j++) {
            double kLast = Surf.wavenumber(tp, Math.max(0.3, r.depth[j]));
            double f = 0, e = 0, ww = 0;
            for (int i = 0; i < nu; i++) {
                int q = i * nv + j;
                double h = r.depth[q];
                if (i > 0) {
                    double k = h > 0.05 ? Surf.wavenumber(tp, h) : kLast;
                    if (!Double.isNaN(k)) kLast = k;
                    phase[q] = (float) (phase[q - nv] + kLast * Math.cos(r.theta[q]) * ZELLE / (2 * Math.PI));
                }
                // Weißwasser: Anteil der Wellen, die seewärts schon gebrochen sind und als Walze laufen; wächst mit Q_b
                // (je WEISS_LAENGE m Brechzone), vergeht, wo die Wellen sich im Trog wieder fangen (über WIEDER_LAENGE m)
                double keep = ww * Math.exp(-ZELLE / WIEDER_LAENGE);
                ww = keep + (1 - keep) * (1 - Math.exp(-r.qb[q] * ZELLE / WEISS_LAENGE));
                if (h <= 0.05) ww = 0;
                weiss[q] = (float) Math.max(ww, r.qb[q]);
                // Schaumteppich: frisch, wo es bricht, zerfällt landwärts über SCHAUM_LAENGE
                f = Math.max(r.qb[q], f * Math.exp(-ZELLE / SCHAUM_LAENGE));
                foam[q] = (float) (h > 0.05 ? 0.75 * f : f * 0.75);
                // Set-up landwärts der letzten nassen Zelle weiterführen (die Wasserlinie steigt mit)
                if (h > 0.05) e = r.eta[q];
                eta[q] = (float) e;
            }
        }
        // Strömung nur in der Brandung und kurz dahinter (Rippströme laufen ein Stück hinaus): Gewicht aus dem stärksten
        // Brechen im Umkreis von STROM_WEITE m quer zur Küste; gedeckelt auf STROM_MAX [G: das Feld rechnet die
        // Querströmung aus der Masse, auf dem echten Grund wird sie weit draußen unruhig]
        float[] wStrom = new float[nu * nv];
        int win = (int) Math.round(STROM_WEITE / ZELLE);
        for (int j = 0; j < nv; j++)
            for (int i = 0; i < nu; i++) {
                double m = 0;
                for (int ii = Math.max(0, i - win); ii <= Math.min(nu - 1, i + win); ii++) m = Math.max(m, r.qb[ii * nv + j]);
                wStrom[i * nv + j] = (float) smooth(0.02, 0.25, m);
            }
        int n = texN;
        float[] t = new float[n * n * 4], fl = new float[n * n * 2];
        double sx = feld.sx, sz = feld.sz, ax = feld.ax, az = feld.az;
        java.util.stream.IntStream.range(0, n).parallel().forEach(jj -> {
            for (int ii = 0; ii < n; ii++) {
                double x = texX0 + (ii + 0.5) * TEX_ZELLE, z = texZ0 + (jj + 0.5) * TEX_ZELLE;
                double dx = x - feld.x0, dz = z - feld.z0;
                double u = (dx * sx + dz * sz) / ZELLE, v = (dx * ax + dz * az) / ZELLE;
                if (u < 0 || v < 0 || u > nu - 1 || v > nv - 1) continue;
                int i = (int) Math.floor(u), j = (int) Math.floor(v);
                int i1 = Math.min(nu - 1, i + 1), j1 = Math.min(nv - 1, j + 1);
                double fi = u - i, fj = v - j;
                int a = i * nv + j, b = i * nv + j1, c = i1 * nv + j, d = i1 * nv + j1;
                double w00 = (1 - fi) * (1 - fj), w01 = (1 - fi) * fj, w10 = fi * (1 - fj), w11 = fi * fj;
                int o = (jj * n + ii) * 4;
                t[o] = (float) (weiss[a] * w00 + weiss[b] * w01 + weiss[c] * w10 + weiss[d] * w11);
                t[o + 1] = (float) (phase[a] * w00 + phase[b] * w01 + phase[c] * w10 + phase[d] * w11);
                t[o + 2] = (float) (foam[a] * w00 + foam[b] * w01 + foam[c] * w10 + foam[d] * w11);
                t[o + 3] = (float) (eta[a] * w00 + eta[b] * w01 + eta[c] * w10 + eta[d] * w11);
                double ws = wStrom[a] * w00 + wStrom[b] * w01 + wStrom[c] * w10 + wStrom[d] * w11;
                double cu = (r.cu[a] * w00 + r.cu[b] * w01 + r.cu[c] * w10 + r.cu[d] * w11) * ws;
                double cv = (r.cv[a] * w00 + r.cv[b] * w01 + r.cv[c] * w10 + r.cv[d] * w11) * ws;
                double sp = Math.hypot(cu, cv);
                if (sp > STROM_MAX) { cu *= STROM_MAX / sp; cv *= STROM_MAX / sp; }
                int p = (jj * n + ii) * 2;
                fl[p] = (float) (cu * sx + cv * ax);
                fl[p + 1] = (float) (cu * sz + cv * az);
            }
        });
        tex = t;
        flow = fl;
        texVersion++;
        bakeMs = (System.nanoTime() - t0) / 1e6;
    }

    /** Wert der gebackenen Textur am Ort (Kanal 0..3), bilinear nicht nötig: nächste Zelle (für Prüfung und HUD). */
    public double texAt(double x, double z, int ch) {
        float[] t = tex;
        if (t == null) return Double.NaN;
        int i = (int) Math.floor((x - texX0) / TEX_ZELLE), j = (int) Math.floor((z - texZ0) / TEX_ZELLE);
        if (i < 0 || j < 0 || i >= texN || j >= texN) return 0;
        return t[(j * texN + i) * 4 + ch];
    }

    /** Auflauf nach der Faustformel (Stockdon 2006) für die See draußen und den Strandhang (m über Ruhewasser). */
    public double auflaufR2() { return Swash.stockdon(Math.max(0.05, surf.hs), surf.tp, STRANDHANG); }

    /** Set-up an der Küste nahe (x, z) (m, das Höchste auf dem Querprofil von 300 m seewärts bis 100 m landwärts der Wasserlinie NHN 0). */
    public double setupAnDerKueste(double x, double z) {
        double[] w = Kueste.waterline(x, z);
        double best = 0;
        for (double d = -100; d <= 300; d += 4) best = Math.max(best, feld.setupAt(w[0] - d * Kueste.SX, w[1] - d * Kueste.SZ));
        return best;
    }

    static double smooth(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    /** Strömung aus der gebackenen Textur (x, z in m/s) oder {0, 0}. */
    public double[] stromAt(double x, double z) {
        float[] f = flow;
        if (f == null) return new double[2];
        int i = (int) Math.floor((x - texX0) / TEX_ZELLE), j = (int) Math.floor((z - texZ0) / TEX_ZELLE);
        if (i < 0 || j < 0 || i >= texN || j >= texN) return new double[2];
        return new double[]{f[(j * texN + i) * 2], f[(j * texN + i) * 2 + 1]};
    }

    /** „Brandung: Feld 188 × 601 zu 8 m, … · Riffe … · 2 Buhnen … · Textur 835² zu 6 m (gebacken in 60 ms)“ */
    public String describe() {
        return String.format(Locale.GERMAN, "%s · %s · %s · Textur %d² zu %.0f m (gebacken in %.0f ms)", feld.describe(), riffe.describe(), buhnen.describe(),
                texN, TEX_ZELLE, bakeMs);
    }
}
