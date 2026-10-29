package com.dan.sylt.scene;

import com.dan.surf.BreakerMesh;
import com.dan.surf.Surf;
import com.dan.surf.Swash;
import com.dan.surf.WaveTrain;

import java.util.Locale;

/**
 * Die Brandung nahe am Auge als Netz (seit Phase 7a): ein Stück Strand von {@link #BREITE} m, senkrecht zur Küste an der
 * Wasserlinie gegenüber dem Blickpunkt, mit den Brechern aus {@link WaveTrain} (Phase 6) und dem Auflauf aus
 * {@link Swash} (Phase 7). Der Grund des Stücks ist das Mittel des Grunds der Brandung (Gelände mit Riffen) über seine
 * Breite; die See am Rand zur See kommt aus dem Wellenfeld ({@link Brandung#feld}). In diesem Stück zeichnet nicht
 * {@code ocean}, sondern das Netz (die See lässt dort ein Loch), und der Sand nimmt die Nässe aus dem Auflauf.
 * <p>
 * Wandert der Blick die Küste entlang (mehr als {@link #WANDERN} m), wird ein neues Stück im eigenen Faden gebaut und
 * eingeschwungen (25 s Szenenzeit), das alte läuft so lange weiter.
 * <p>
 * Seit Phase 19c ist das Stück 320 statt 160 m breit, und die Brecher laufen zu den Enden hin aus ({@link #auslaufen}):
 * vorher standen sie bis an die Enden voll da und brachen dort als Rechteck ab, daneben lag nur die flache See.
 */
public final class Nahbrandung {
    /** Breite des Stücks längs der Küste (m), Wanderweg bis zum Neubau (m), Einschwingzeit (s). */
    public static final double BREITE = 320, WANDERN = 80, EINSCHWINGEN = 25;
    /**
     * An den Enden: so weit (m) zeichnet die See noch ganz (dort ist das Netz flach), und über so viele Meter danach
     * wachsen die Brecher auf ihre volle Höhe.
     */
    public static final double RAND = 12, AUSLAUF = 60;
    /** Höchstens so weit (m) vom Auge zur Wasserlinie und so hoch (m über dem Pegel) wird das Netz gezeichnet. */
    public static final double REICHWEITE = 900, HOEHE = 160;

    /** Ein Stück Strand. */
    public static final class Stueck {
        public final WaveTrain zug;
        public final Swash schwall;
        public final BreakerMesh netz = new BreakerMesh();
        /** Ursprung (Rand zur See, Mitte links längs: z_lokal = 0) in der Szene, Ende landwärts (m lokal), Lage längs. */
        public final double ox, oz, xEnde, laengs, pegel, hs, tp;
        /** Wo der Rand der See lokal liegt (m) und wo die Wasserlinie (m). */
        public final double xWasserlinie;
        public int auflaeufe, stoesse;
        /** Unterschied echter Sand − gemittelter Grund je Reihe und 0,5 m (zum Anlegen des Wassers an den Sand). */
        float[] anlegen;
        int anlegenN;
        Stueck(WaveTrain zug, Swash schwall, double ox, double oz, double xEnde, double laengs, double pegel, double hs, double tp, double xWasserlinie) {
            this.zug = zug;
            this.schwall = schwall;
            this.ox = ox;
            this.oz = oz;
            this.xEnde = xEnde;
            this.laengs = laengs;
            this.pegel = pegel;
            this.hs = hs;
            this.tp = tp;
            this.xWasserlinie = xWasserlinie;
        }

        /** Szenenpunkt aus lokal (x zur Küste, z längs). */
        public double[] welt(double x, double z) {
            return new double[]{ox + x * Kueste.SX + z * Kueste.AX, oz + x * Kueste.SZ + z * Kueste.AZ};
        }

        /** Lokal (x, z) aus der Szene. */
        public double[] lokal(double wx, double wz) {
            double dx = wx - ox, dz = wz - oz;
            return new double[]{dx * Kueste.SX + dz * Kueste.SZ, dx * Kueste.AX + dz * Kueste.AZ};
        }

        /**
         * Matrix lokal → Szene (Spalten zuerst): x lokal entlang der Blickrichtung zur Küste, y nach oben (die Höhen sind
         * schon NHN), z lokal längs.
         */
        public float[] modell() {
            return new float[]{(float) Kueste.SX, 0, (float) Kueste.SZ, 0, 0, 1, 0, 0, (float) Kueste.AX, 0, (float) Kueste.AZ, 0, (float) ox, 0, (float) oz, 1};
        }

        /** Nässe des Sands (0..1) am Szenenpunkt oder NaN außerhalb des Stücks. */
        public double nass(double wx, double wz) {
            double[] l = lokal(wx, wz);
            if (l[1] < 0 || l[1] > zug.width || l[0] < schwall.boundaryX() || l[0] > xEnde) return Double.NaN;
            int r = Math.min(zug.rows - 1, (int) (l[1] / zug.width * zug.rows));
            return schwall.wetnessAt(r, l[0]);
        }
    }

    private final Brandung brandung;
    private volatile Stueck aktiv;
    private volatile Thread bau;
    /** Reihen längs (nach Qualität) und ob das Netz gezeichnet werden soll. */
    public volatile int reihen = 72;
    public volatile boolean an = true;
    /** Zeit zum Bauen des Netzes im letzten Bild (ms) und ob es zu sehen ist. */
    public volatile double netzMs, schrittMs;
    public volatile boolean sichtbar;
    public volatile String meldung = "noch kein Stück";
    public volatile Swash.Weather wetter = Swash.Weather.OVERCAST;

    private final Terrain gelaende;

    public Nahbrandung(Brandung b, Terrain t) { this.brandung = b; this.gelaende = t; }

    public Stueck stueck() { return aktiv; }

    /**
     * Je Bild: entscheidet, ob ein Stück gezeigt wird (Auge nahe genug), baut neu, wenn der Blick gewandert ist oder
     * sich See oder Pegel stark geändert haben, und rechnet das aktive Stück um dt weiter.
     */
    public void schritt(double dt, double[] auge, double tx, double tz, double pegel) {
        double[] w = Kueste.waterline(tx, tz);
        double weit = Math.hypot(auge[0] - w[0], auge[2] - w[1]);
        sichtbar = an && weit < REICHWEITE && auge[1] - pegel < HOEHE;
        if (!sichtbar) { meldung = an ? String.format(Locale.GERMAN, "Netz aus: Auge %.0f m von der Wasserlinie", weit) : "Netz abgeschaltet"; return; }
        Stueck s = aktiv;
        Surf sf = brandung.surf;
        boolean neu = s == null || Math.abs(Kueste.along(w[0], w[1]) - s.laengs) > WANDERN || Math.abs(pegel - s.pegel) > 0.4
                || Math.abs(sf.hs - s.hs) > 0.15 * Math.max(0.3, s.hs) || Math.abs(sf.tp - s.tp) > 0.9 || s.zug.rows != reihen;
        if (neu && bau == null) {
            final double along = Kueste.along(w[0], w[1]);
            final double[] wl = w;
            final int rows = reihen;
            Thread th = new Thread(() -> {
                try {
                    Stueck st = baue(wl, along, pegel, rows);
                    if (st != null) aktiv = st;
                } catch (RuntimeException e) {
                    meldung = "Netz: " + e.getMessage();
                } finally { bau = null; }
            }, "brandung-nah");
            th.setDaemon(true);
            bau = th;
            th.start();
        }
        if (s == null) return;
        long t0 = System.nanoTime();
        double h = Math.min(0.05, Math.max(0, dt));
        s.zug.level = pegel;
        s.schwall.weather = wetter;
        if (h > 0) {
            s.zug.step(h);
            s.schwall.step(h, s.zug);
        }
        long t1 = System.nanoTime();
        s.netz.build(s.zug, s.xEnde, s.schwall);
        anlegen(s);
        auslaufen(s);
        netzMs = netzMs * 0.9 + (System.nanoTime() - t1) / 1e6 * 0.1;
        schrittMs = schrittMs * 0.9 + (t1 - t0) / 1e6 * 0.1;
        // seit Phase 13: ohne gezählte Spitzen (Schwall noch jung, oder er läuft bis an den Kliffuß) kein „NaN“ im HUD
        double r2 = s.schwall.r2();
        String r2s = Double.isNaN(r2) ? "noch keine Spitze" : String.format(Locale.GERMAN, "%.2f m", r2);
        meldung = String.format(Locale.GERMAN, "Netz %d Reihen auf %.0f m, Wasserlinie %.0f m vom Rand zur See · %d Wellen · R_2%% %s (Stockdon %.2f m) · Rechnen %.1f ms, Netz %.1f ms",
                s.zug.rows, s.zug.width, s.xWasserlinie, s.zug.waves.size(), r2s, brandung.auflaufR2(), schrittMs, netzMs);
    }

    /** Ein Stück an der Wasserlinie w bauen und einschwingen (im Faden des Aufrufers). */
    public Stueck baue(double[] w, double along, double pegel, int rows) {
        Surf sf = brandung.surf;
        double hs = Math.max(0.05, sf.hs), tp = sf.tp;
        // seewärts bis in 2,2·H_s + 1 m Tiefe (höchstens 700 m), landwärts bis 3,5 m über den Pegel (Kliffuß, höchstens 160 m)
        double tiefe = 2.2 * hs + 1;
        double xs = 0;
        while (xs < 700 && pegel - mittel(w, -xs, along) < tiefe) xs += 5;
        double xl = 0;
        while (xl < 160 && mittel(w, xl, along) < pegel + 3.5) xl += 2;
        double ox = w[0] - xs * Kueste.SX - BREITE / 2 * Kueste.AX, oz = w[1] - xs * Kueste.SZ - BREITE / 2 * Kueste.AZ;
        double xEnde = xs + xl;
        // Grund des Stücks: das Mittel über die Breite, alle 0,5 m vorab
        int n = (int) Math.ceil(xEnde / 0.5) + 2;
        double[] grund = new double[n];
        for (int k = 0; k < n; k++) {
            double x = k * 0.5, s = 0;
            for (int q = 0; q < 7; q++) {
                double zl = (q + 0.5) / 7 * BREITE;
                s += sf.bed.height(ox + x * Kueste.SX + zl * Kueste.AX, oz + x * Kueste.SZ + zl * Kueste.AZ);
            }
            grund[k] = s / 7;
        }
        WaveTrain.Profile prof = x -> {
            double u = Math.max(0, Math.min(n - 1.001, x / 0.5));
            int i = (int) u;
            return grund[i] + (grund[i + 1] - grund[i]) * (u - i);
        };
        WaveTrain zug = new WaveTrain(rows, 0, xEnde, BREITE);
        zug.profile = prof;
        zug.level = pegel;
        zug.tp = tp;
        // die See am Rand aus dem Feld (H_s dort), sonst aus der See draußen mit Shoaling
        double h0 = Math.max(0.3, pegel - grund[0]);
        double[] r0 = {ox + BREITE / 2 * Kueste.AX, oz + BREITE / 2 * Kueste.AZ};
        double hsFeld = brandung.feld.hsAt(r0[0], r0[1]);
        zug.hs = Double.isNaN(hsFeld) || hsFeld <= 0 ? Math.min(hs * Surf.shoaling(tp, h0), 0.78 * h0) : hsFeld;
        zug.seed = 2026 + (long) Math.round(along / 10);
        // der Schwall ab 1,6·H_s + 0,3 m Tiefe (dort liegt sein Rand höchstens)
        double xa = 0;
        double ta = Math.min(h0 - 0.1, 1.6 * zug.hs + 0.3);
        while (xa < xEnde - 5 && pegel - prof.bed(xa) > ta) xa += 0.5;
        Swash sw = new Swash(zug, Math.max(0, xa - 2), xEnde, 0.5);
        sw.weather = wetter;
        Stueck st = new Stueck(zug, sw, ox, oz, xEnde, along, pegel, hs, tp, xs);
        sw.onSwash = (x, y, z, kind, s) -> {
            if (kind == Swash.Kind.RUNUP) st.auflaeufe++;
            else if (kind == Swash.Kind.COLLISION) st.stoesse++;
        };
        for (double t = 0; t < EINSCHWINGEN; t += 1 / 30.0) {
            zug.step(1 / 30.0);
            sw.step(1 / 30.0, zug);
        }
        sw.resetStatistics();
        // je Reihe der echte Sand gegen den gemittelten Grund: das flache Wasser legt sich an den Sand der Szene
        int nx = (int) Math.ceil(xEnde / 0.5) + 2;
        st.anlegenN = nx;
        st.anlegen = new float[rows * nx];
        for (int r = 0; r < rows; r++)
            for (int i = 0; i < nx; i++) {
                double x = i * 0.5;
                double[] p = st.welt(x, zug.rowY(r));
                st.anlegen[r * nx + i] = (float) (gelaende.height(p[0], p[1]) - prof.bed(x));
            }
        st.netz.build(zug, xEnde, sw);
        anlegen(st);
        auslaufen(st);
        return st;
    }

    /**
     * Das Netz an den Sand der Szene legen: wo das Wasser flach ist (unter 1,5 m), hebt oder senkt es sich um den
     * Unterschied zwischen dem Sand an der Stelle und dem gemittelten Grund des Stücks; im Tiefen bleibt es, wie es ist.
     */
    static void anlegen(Stueck s) {
        float[] v = s.netz.vertices, d = s.anlegen;
        if (d == null) return;
        int n = s.anlegenN, rows = s.zug.rows, ns = s.netz.ns;
        for (int r = 0; r < Math.min(rows, s.netz.ny); r++)
            for (int k = 0; k < ns; k++) {
                int o = (r * ns + k) * BreakerMesh.STRIDE;
                double x = v[o], tiefe = v[o + 8];
                double w = tiefe >= 1.5 ? 0 : tiefe <= 0.3 ? 1 : 1 - (tiefe - 0.3) / 1.2;
                if (w <= 0) continue;
                double u = Math.max(0, Math.min(n - 1.001, x / 0.5));
                int i = (int) u;
                double dd = d[r * n + i] + (d[r * n + i + 1] - d[r * n + i]) * (u - i);
                v[o + 1] += (float) (dd * w);
                v[o + 9] += (float) dd;
            }
    }

    /**
     * Die Brecher zu den Enden des Stücks hin auslaufen lassen: auf den letzten {@link #RAND} m liegt das Wasser flach
     * (knapp unter dem mittleren Spiegel, dort zeichnet die See darüber), über {@link #AUSLAUF} m danach wachsen Kämme
     * und Täler weich auf ihre volle Höhe, die Normalen drehen mit. Der dünne Schwall auf dem Sand bleibt, wie er ist.
     */
    static void auslaufen(Stueck s) {
        float[] v = s.netz.vertices;
        int ns = s.netz.ns;
        double breite = s.zug.width;
        // der mittlere Spiegel (mit Schwebung) je 2 m, nicht je Ecke
        int nm = (int) Math.ceil(s.xEnde / 2) + 2;
        double[] spiegel = new double[nm];
        for (int i = 0; i < nm; i++) spiegel[i] = s.zug.meanLevel(i * 2.0);
        // Reihen parallel wie im BreakerMesh
        java.util.stream.IntStream.range(0, s.netz.ny).parallel().forEach(r -> {
            // eine Reihe liegt längs an einer Stelle (auch die Lippe); die Mitte bleibt, wie sie ist
            double d = Math.min(v[r * ns * BreakerMesh.STRIDE + 2], breite - v[r * ns * BreakerMesh.STRIDE + 2]);
            double u = Math.max(0, Math.min(1, (d - RAND) / AUSLAUF)), aus = 1 - u * u * (3 - 2 * u);
            if (aus <= 0) return;
            for (int k = 0; k < ns; k++) {
                int o = (r * ns + k) * BreakerMesh.STRIDE;
                double t = aus * Math.max(0, Math.min(1, (v[o + 8] - 0.15) / 0.6));
                if (t <= 0) continue;
                double q = Math.max(0, Math.min(nm - 1.001, v[o] / 2.0));
                int i = (int) q;
                double flach = Math.max(v[o + 9], spiegel[i] + (spiegel[i + 1] - spiegel[i]) * (q - i) - 0.1 * t);
                v[o + 1] += (float) ((flach - v[o + 1]) * t);
                v[o + 8] = Math.max(0, v[o + 1] - v[o + 9]);
                double nx = v[o + 3] * (1 - t), ny = v[o + 4] * (1 - t) + t, nz = v[o + 5] * (1 - t);
                double l = Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (l > 1e-6) {
                    v[o + 3] = (float) (nx / l);
                    v[o + 4] = (float) (ny / l);
                    v[o + 5] = (float) (nz / l);
                }
                v[o + 7] *= (float) (1 - t);
            }
        });
    }

    /** Mittlerer Grund über die Breite in x m landwärts (negativ: seewärts) der Wasserlinie w. */
    private double mittel(double[] w, double x, double along) {
        Surf sf = brandung.surf;
        double s = 0;
        for (int q = 0; q < 5; q++) {
            double zl = ((q + 0.5) / 5 - 0.5) * BREITE;
            s += sf.bed.height(w[0] + x * Kueste.SX + zl * Kueste.AX, w[1] + x * Kueste.SZ + zl * Kueste.AZ);
        }
        return s / 5;
    }

    /** Für Standbilder: das Stück für den Blick jetzt bauen und einschwingen (im aufrufenden Faden). */
    public void jetzt(double tx, double tz, double pegel) {
        double[] w = Kueste.waterline(tx, tz);
        aktiv = baue(w, Kueste.along(w[0], w[1]), pegel, reihen);
    }

    /** Ob gerade ein Stück gebaut wird. */
    public boolean baut() { return bau != null; }
}
