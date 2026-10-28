package com.dan.sylt.gl;

import com.dan.sylt.scene.Kante;
import com.dan.sylt.scene.Kueste;
import com.dan.sylt.scene.Nahbrandung;
import com.dan.sylt.scene.Terrain;
import com.dan.soundscape.AudioOut;
import com.dan.soundscape.Edge;
import com.dan.soundscape.Soundscape;
import com.dan.soundscape.Soundscape.Kind;
import com.dan.surf.Surf;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SplittableRandom;

/**
 * Phase 14: der Klang der Szene (F27 A) mit {@code com.dan.soundscape}, erzeugt, ohne Audiodateien. Das Ohr ist die
 * Kamera. Diese Klasse verdrahtet:
 * <ul>
 * <li><b>Ferne Brandung</b>: aus dem Wellenfeld von {@code surf} (Leistung, die je m Küste in der Brandung vergeht, und
 * die Art der Brecher) eine Linie von Stücken zu 25 m auf ±2,4 km um das Ohr; das Stück der Nahbrandung bleibt
 * ausgespart, dort klingen die einzelnen Wellen. Steht das Ohr oben hinter der Kliffkante, beugt sich der Schall über
 * die Kante (Maekawa, um 500 Hz) und wird leiser.</li>
 * <li><b>Einzelne Wellen</b> im Stück der Nahbrandung ({@code WaveTrain.onBreaking}, {@code onImpact},
 * {@code Swash.onSwash}): Schwallbrecher zischen, die Lippe des Sturzbrechers schlägt, der Schwall läuft den Strand
 * hinauf und zurück. Je Welle klingen drei Stellen des Kamms: gegenüber dem Ohr und 50 m zu beiden Seiten.</li>
 * <li><b>Wind</b> aus dem Windfeld von {@code wind} (über der Kante schneller), die Kliffkante heult im Sturm
 * ({@link Edge}), Strandhafer raschelt um das Ohr, Flugsand prasselt (Fluss aus {@code dune}).</li>
 * <li><b>Tiere</b>: Möwen ({@code flock.Flock.onCall}), Sanderlinge, Austernfischer und Silbermöwen am Spülsaum
 * ({@code Shorebirds.onCall}), der Seehund ({@code seal}), selten eine Kuh der Galloways.</li>
 * <li><b>Wetter</b>: Regen am Ohr, Donner der Blitze ({@code lightning.onThunder}) mit Laufzeit.</li>
 * <li><b>Abbruch</b> ({@code Kliffabbruch.onKlang}): das Kliff bricht, Brocken schlagen auf den Strand, Sand rieselt.</li>
 * </ul>
 * Ereignisse werden in einem eigenen Faden gerechnet (nur bei Ton an), {@link AudioOut} spielt in seinem. Offline
 * (Hörproben, Prüfung) läuft alles im rufenden Faden.
 */
public final class Klang {
    /** Halbe Länge der Brandungslinie (m) und Länge eines Stücks (m). */
    public static final double LINIE = 2400, STUECK = 25;
    /** Je Welle drei Stellen des Kamms: gegenüber dem Ohr und so weit zu beiden Seiten (m). */
    public static final double SEITE = 50;

    public final Soundscape s = new Soundscape();
    private final Renderer r;
    private AudioOut out;
    public volatile boolean on;
    /** Teile an und aus (Bedienfeld). */
    public volatile boolean brandungAn = true, windAn = true, sandAn = true, tiereAn = true, wetterAn = true, kliffAn = true;
    /** Offline (Hörproben, Prüfung): Ereignisse gleich im rufenden Faden, auch ohne Lautsprecher. */
    public volatile boolean offline;
    private final SplittableRandom rng = new SplittableRandom(2026);
    private final java.util.concurrent.ExecutorService synth = java.util.concurrent.Executors.newSingleThreadExecutor(q -> {
        Thread t = new Thread(q, "Klang-Synthese");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });
    private final java.util.concurrent.atomic.AtomicInteger queued = new java.util.concurrent.atomic.AtomicInteger();

    /** Gezählt (HUD, Hörproben, Prüfung). */
    public volatile int schwallbrecher, sturzbrecher, schwaelle, moewen, sanderlinge, austernfischer, seehunde, kuehe, donner, abbrueche, brocken, rieseln;
    public volatile String note = "";
    /** Was die Linie gerade gibt: Stücke mit Leistung, Leistung je m Küste am Ohr (W/m), Anteil Sturzbrecher, Beugung über die Kante (dB). */
    public volatile int stuecke;
    public volatile double wattsHier, sturzAnteil, kanteDb, seeAnteil;

    private Nahbrandung.Stueck hooked;
    private volatile double yOhr = Double.NaN;
    private final double[] lastRun = new double[3], lastPlunge = new double[3], lastSpill = new double[3];
    private double lineT = 99, lineX = 1e18, lineZ = 1e18, edgeX = 1e18, edgeZ = 1e18, cowT = 20, brockenT, rieselT;
    private double time, moeweT;

    Klang(Renderer r) {
        this.r = r;
        s.seed = 2026;
        s.quality = Soundscape.Quality.MEDIUM;
        s.windCarry = true;
        var lb = r.leben;
        lb.moewen.onCall = (x, y, z, str, g) -> {
            if (!tiereAn || time - moeweT < 0.9) return;   // ein Schwarm ruft durcheinander, aber nicht ohne Pause
            moeweT = time;
            moewen++;
            ev(Kind.MOEWE, x, y, z, str);
        };
        lb.strand.onCall = (x, y, z, str, sp) -> {
            if (!tiereAn) return;
            switch (sp.key()) {
                case "sanderling" -> { sanderlinge++; ev(Kind.SANDERLING, x, y + 0.1, z, Math.min(1, str + 0.2), 1 + rng.nextInt(3)); }
                case "austernfischer" -> { austernfischer++; ev(Kind.AUSTERNFISCHER, x, y + 0.2, z, Math.min(1, str + 0.2), 0.5); }
                default -> { moewen++; ev(Kind.MOEWE, x, y + 0.3, z, str); }
            }
        };
        lb.seehund.seal.onCall = (x, z, str) -> {
            if (!tiereAn) return;
            seehunde++;
            ev(Kind.SEEHUND, x, r.wiring.pegel + 0.3, z, str > 0.55 ? 1 : 0.7);
        };
        r.unwetter.blitz.onThunder = th -> {
            if (!wetterAn) return;
            donner++;
            run(() -> s.thunder(th.x(), th.y(), th.z(), th.levelDb(), null, null, null, true));
        };
        if (r.abbruch != null) r.abbruch.onKlang = (art, x, y, z, vol, speed) -> {
            if (!kliffAn) return;
            double now = time;
            switch (art) {
                case 0 -> { abbrueche++; ev(Kind.ABBRUCH, x, y, z, 1, Math.max(100, vol)); }   // das erste Stück steht für den ganzen Abbruch (in Phase 12 rund 200 m³)
                case 1 -> {
                    // viele Brocken schlagen fast zugleich auf: höchstens einer je 0,12 s, die großen zuerst
                    if (now - brockenT < 0.12 && vol < 2) return;
                    brockenT = now;
                    brocken++;
                    ev(Kind.ERDBROCKEN, x, y, z, Math.min(1, 0.4 + speed / 10), vol);
                }
                default -> {
                    if (now - rieselT < 0.6) return;
                    rieselT = now;
                    rieseln++;
                    ev(Kind.RIESELN, x, y, z, Math.min(1, 0.4 + vol), 1.5 + Math.min(6, vol * 3));
                }
            }
        };
    }

    /** Ein Ereignis rechnen lassen: im Bildlauf in einem eigenen Faden, nur bei Ton an; offline gleich. */
    private void run(Runnable job) {
        if (offline) { job.run(); return; }
        if (!on || queued.get() > 24) return;
        queued.incrementAndGet();
        synth.execute(() -> { try { job.run(); } catch (RuntimeException e) { note = "!" + e; } finally { queued.decrementAndGet(); } });
    }

    private void ev(Kind k, double x, double y, double z, double str) { run(() -> s.event(k, x, y, z, str)); }

    private void ev(Kind k, double x, double y, double z, double str, double tone) { run(() -> s.event(k, x, y, z, str, tone)); }

    /** Ton an oder aus (Lautsprecher im eigenen Faden). */
    public synchronized void setOn(boolean b) {
        on = b;
        if (b && out == null) {
            try { out = new AudioOut(s); out.start(); } catch (RuntimeException | LinkageError e) { note = "!kein Ton: " + e.getMessage(); out = null; on = false; }
        } else if (!b && out != null) { out.stop(); out = null; }
    }

    public synchronized void dispose() {
        if (out != null) { out.stop(); out = null; }
        on = false;
        synth.shutdownNow();
    }

    /** „Klang an · Wind 61 dB · … · Brandung fern 64 dB (78 m) · Wellen: 12 Schwall-, 3 Sturzbrecher, 9 Schwälle“ */
    public String describe() {
        double nl = s.surfLineNearest();
        return (on ? "Klang an · " : offline ? "Klang (Probe) · " : "Klang aus · ") + s.describe()
                + (Double.isNaN(nl) ? "" : String.format(Locale.GERMAN, " · Brandung fern %.0f dB, Linie %d Stücke, nächstes %.0f m%s", s.surfLineDb(), stuecke, nl,
                kanteDb < -0.5 ? String.format(Locale.GERMAN, ", über die Kante %.0f dB", kanteDb) : ""))
                + String.format(Locale.GERMAN, " · Wellen: %d Schwallbrecher, %d Sturzbrecher, %d Schwälle · Rufe: %d Möwen, %d Sanderlinge, %d Austernfischer, %d Seehund, %d Kuh",
                schwallbrecher, sturzbrecher, schwaelle, moewen, sanderlinge, austernfischer, seehunde, kuehe)
                + (donner + abbrueche + brocken > 0 ? String.format(Locale.GERMAN, " · %d Donner, %d Abbruch, %d Brocken", donner, abbrueche, brocken) : "")
                + (note.isEmpty() ? "" : " · " + note);
    }

    /** Je Bild (Echtzeit dt): alle Eingänge setzen, Linie und Kanten bei Bedarf neu, Ereignisse planen. */
    public void feed(double dt) {
        time += dt;
        var w = r.wiring;
        var cam = r.camera;
        double ex = cam.eye[0], ey = cam.eye[1], ez = cam.eye[2];
        double grund = r.groundHeight(ex, ez), unter = Math.max(grund, w.pegel), hoch = ey - unter;
        s.earX = ex; s.earY = ey; s.earZ = ez;
        s.earYawDeg = cam.heading();
        s.windSpeed = w.windSpeed;
        s.windFromDeg = w.windFromDeg;
        s.gustiness = Math.max(0.1, w.gustiness);
        s.temperatureC = w.temperatur;
        s.waterLevel = unter;
        s.surfLineLevel = w.pegel;
        s.windGainDb = windAn ? 0 : -80;
        s.wind = windAn ? r.sand::windBei : null;
        s.rainRate = wetterAn && r.unwetter.regenFaellt() ? r.unwetter.rain.snapshot().rateAt(ex, ez) : 0;

        // die offene See: so laut, wie das Wasser nah ist
        double off = Kueste.offshore(ex, ez) + w.pegel * 30;   // bei Hochwasser liegt die Wasserlinie weiter landwärts (Strand 1:30)
        double sea = off >= 0 ? 1 : 1 / (1 + -off / 60);
        seeAnteil = sea;
        s.waveHeight = brandungAn ? Math.max(0.05, w.surf.hs) * sea : 0;
        s.wavePeriod = Math.max(3, w.surf.tp);

        // Sand und Halme am Ohr
        var feld = r.sand.feld();
        s.sandFlux = sandAn && r.sand.an && feld != null && hoch < 4 ? Math.max(0, feld.fluxAt(ex, ez)) : 0;
        double m = 0;
        if (sandAn && hoch < 4) {
            for (int k = 0; k < 9; k++) {
                double a = k * 2.39996, rad = k == 0 ? 0 : 4;
                byte art = r.karte.at(r.terrain, ex + Math.cos(a) * rad, ez + Math.sin(a) * rad);
                m += art == Terrain.STRANDHAFER ? 1 : art == Terrain.DUENE ? 0.3 : art == Terrain.HEIDE ? 0.1 : 0;
            }
            m /= 9;
        }
        s.marramNear = m;

        // die Kliffkante: im Sturm heult sie (die nächsten Stücke zu 30 m in 300 m)
        Kante kt = r.kliff.kante;
        if (!windAn || kt == null) { if (!s.edges.isEmpty()) s.edges.clear(); edgeX = 1e18; }
        else if (Math.hypot(ex - edgeX, ez - edgeZ) > 60) {
            edgeX = ex; edgeZ = ez;
            List<Edge> el = new ArrayList<>();
            for (int i = 0; i < kt.n; i += 30) {
                double x = kt.kanteX(i), z = kt.z(i);
                if (Math.hypot(x - ex, z - ez) > 320) continue;
                el.add(new Edge("Kliffkante", x, kt.hKante[i] + 0.3, z, 2.5, 10));   // die Kante braust mehr, als dass sie heult: nur ein Drittel zählt [G]
            }
            s.edges.clear();
            s.edges.addAll(el);
        }

        // die ferne Brandung: Linie neu, wenn das Ohr gewandert ist oder jede Sekunde (die Brecher ändern sich mit der Tide)
        lineT += dt;
        Nahbrandung.Stueck st = r.nah.sichtbar ? r.nah.stueck() : null;
        if (!brandungAn) { s.surfLine = null; stuecke = 0; }
        else if (lineT > 1 || Math.hypot(ex - lineX, ez - lineZ) > 20) {
            lineT = 0;
            lineX = ex; lineZ = ez;
            s.surfLine = linie(ex, ez, st);
            kanteDb = kante(ex, ey, ez, w.pegel);
            s.surfLineGainDb = kanteDb;
        }

        // einzelne Wellen im Stück der Nahbrandung
        if (st != hooked) {
            if (hooked != null) { hooked.zug.onBreaking = null; hooked.zug.onImpact = null; }
            angekommen.clear();
            hooked = st;
            if (st != null) haken(st);
        }
        if (st != null) {
            double[] l = st.lokal(ex, ez);
            yOhr = Math.max(0, Math.min(st.zug.width, l[1]));
            schwall(st, dt);
        }

        // Vögel am Spülsaum rufen beim Fressen (je Trupp im Mittel alle 9 bis 16 s, größere öfter) [G]
        if (tiereAn && r.showLeben && r.leben.anStrand) {
            for (var g : r.leben.strand.groups()) {
                if (g.birds.isEmpty() || g.mode != com.dan.flock.Waders.Mode.FRESSEN) continue;
                double d = Math.hypot(g.cx - ex, g.cz - ez);
                if (d > 400) continue;
                String key = g.species.key();
                double per = (key.equals("sanderling") ? 12 : key.equals("austernfischer") ? 9 : 16) / Math.sqrt(Math.max(1, g.birds.size() / 6.0));
                if (rng.nextDouble() >= dt / per) continue;
                var b = g.birds.get(rng.nextInt(g.birds.size()));
                switch (key) {
                    case "sanderling" -> { sanderlinge++; ev(Kind.SANDERLING, b.x, b.y + 0.1, b.z, 0.5 + 0.4 * rng.nextDouble(), 1 + rng.nextInt(Math.min(4, Math.max(1, g.birds.size() / 5)))); }
                    case "austernfischer" -> { austernfischer++; ev(Kind.AUSTERNFISCHER, b.x, b.y + 0.2, b.z, 0.5 + 0.3 * rng.nextDouble(), 0.5); }
                    default -> { moewen++; ev(Kind.MOEWE, b.x, b.y + 0.3, b.z, 0.4 + 0.3 * rng.nextDouble()); }
                }
            }
        }
        // eine Kuh der Galloways, selten
        cowT -= dt;
        if (cowT <= 0) {
            cowT = 25 + 50 * rng.nextDouble();
            var tiere = r.herde.tiere;
            if (tiereAn && r.showHerde && r.herde.an && !tiere.isEmpty()) {
                var t = tiere.get(rng.nextInt(tiere.size()));
                if (Math.hypot(t.x - ex, t.z - ez) < 1500) { kuehe++; ev(Kind.RIND, t.x, t.y + 1.2, t.z, 0.5 + 0.5 * rng.nextDouble(), 0.5); }
            }
        }
    }

    /**
     * Die Brandungslinie: je 25 m längs der Küste die Leistung, die seewärts der Wasserlinie in der Brandung
     * vergeht (W/m², über die Breite bis 1 300 m integriert, so weit das Feld reicht), als Schallleistung je m; der Ort ist der Schwerpunkt der Brandung, die
     * Art nach dem Anteil der Sturzbrecher. Im Stück der Nahbrandung bleibt sie still (dort klingen die Wellen einzeln).
     */
    double[][] linie(double ex, double ez, Nahbrandung.Stueck st) {
        var feld = r.brandung.feld;
        double tp = Math.max(3, r.wiring.surf.tp);
        double nahA = st != null ? st.laengs - st.zug.width / 2 : Double.NaN, nahB = st != null ? st.laengs + st.zug.width / 2 : Double.NaN;
        List<double[]> out = new ArrayList<>();
        double hier = 0, best = 1e18, plHier = 0;
        for (double sa = -LINIE; sa <= LINIE; sa += STUECK) {
            double px = ex + Kueste.AX * sa, pz = ez + Kueste.AZ * sa;
            double[] wl = Kueste.waterline(px, pz);
            double along = Kueste.along(wl[0], wl[1]);
            if (st != null && along > nahA && along < nahB) continue;
            double sum = 0, cx = 0, cz = 0, plunge = 0;
            for (double o = -60; o <= 1300; o += 10) {
                double x = wl[0] - Kueste.SX * o, z = wl[1] - Kueste.SZ * o;
                double d = feld.dissipationAt(x, z);
                if (!(d > 0)) continue;
                double e = d * 10;
                sum += e;
                cx += x * e;
                cz += z * e;
                Surf.Breaker b = feld.typeAt(x, z);
                plunge += e * (b == Surf.Breaker.PLUNGING ? 1 : b == Surf.Breaker.COLLAPSING || b == Surf.Breaker.SURGING ? 0.6 : 0);
            }
            if (sum < 1) continue;
            double pw = Soundscape.surfAcousticPower(sum);
            double[] seg = {cx / sum, cz / sum, pw, plunge / sum, tp, STUECK};
            out.add(seg);
            double d = Math.abs(sa);
            if (d < best) { best = d; hier = sum; plHier = plunge / sum; }
        }
        stuecke = out.size();
        wattsHier = hier;
        sturzAnteil = plHier;
        return out.isEmpty() ? null : out.toArray(new double[0][]);
    }

    /**
     * Beugung über die Kliffkante (dB, ≤ 0): steht das Ohr landwärts der Kante und liegt die Kante über der Sichtlinie zur
     * Brandung (50 m seewärts der Wasserlinie), dämpft der Umweg δ nach Maekawa: 10 · lg(3 + 20 N), N = 2δ/λ bei 500 Hz.
     */
    public double kante(double ex, double ey, double ez, double pegel) {
        Kante kt = r.kliff.kante;
        if (kt == null) return 0;
        int i = kt.row(ez);
        if (i < 0) return 0;
        double oE = Kueste.offshore(ex, ez), oK = Kueste.offshore(kt.kanteX(i), kt.z(i)), hK = kt.hKante[i];
        if (oE >= oK - 1) return 0;   // vor der Kante oder auf ihr
        double oS = 50, hS = pegel;
        // liegt die Kante über der Geraden vom Ohr zur Brandung?
        double yLine = ey + (hS - ey) * (oK - oE) / (oS - oE);
        if (hK <= yLine) return 0;
        double a = Math.hypot(oK - oE, hK - ey), b = Math.hypot(oS - oK, hS - hK), c = Math.hypot(oS - oE, hS - ey);
        double n = 2 * (a + b - c) / (343.0 / 500);
        return -Math.min(22, 10 * Math.log10(3 + 20 * n) - 4.8);
    }

    private final java.util.Set<Integer> angekommen = new java.util.HashSet<>();

    /**
     * Der Schwall: erreicht eine Walze den Rand des Schwalls (die Wasserlinie mit dem Set-up), läuft sie den Strand hinauf
     * und wieder zurück. Je Welle klingt das an drei Stellen (gegenüber dem Ohr und 50 m zu beiden Seiten), dort, wo die
     * Spitze des Schwalls steht; die Höhe des Auflaufs nach der Höhe der Walze in dieser Reihe [G: 0,6 · H + 0,1 m].
     */
    private void schwall(Nahbrandung.Stueck st, double dt) {
        if (!brandungAn) return;
        var zug = st.zug;
        var sw = st.schwall;
        double rand = sw.boundaryX() - 3, yo = yOhr;
        java.util.Set<Integer> da = new java.util.HashSet<>();
        for (var w : zug.waves) {
            da.add(w.id);
            if (w.x < rand || angekommen.contains(w.id)) continue;
            angekommen.add(w.id);
            for (int k = 0; k < 3; k++) {
                double yt = yo + (k - 1) * SEITE;
                if (yt < 0 || yt > sw.width) continue;
                int row = Math.max(0, Math.min(zug.rows - 1, (int) (yt / zug.width * zug.rows)));
                double hw = zug.heightRow(w, row);
                if (!(hw > 0.04)) continue;
                double h = Math.max(0.05, Math.min(1.5, 0.6 * hw + 0.1)) * (0.85 + 0.3 * rng.nextDouble());
                int srow = Math.max(0, Math.min(sw.rows - 1, (int) (yt / sw.width * sw.rows)));
                double tip = sw.tipX(srow);
                double[] p = st.welt(Double.isNaN(tip) ? st.xWasserlinie : tip + 1, yt);
                schwaelle++;
                ev(Kind.SCHWALL, p[0], r.groundHeight(p[0], p[1]) + 0.05, p[1], 0.85, h);
            }
        }
        angekommen.retainAll(da);
    }

    /** Die Ereignisse eines neuen Stücks der Nahbrandung verdrahten. */
    private void haken(Nahbrandung.Stueck st) {
        st.zug.onBreaking = (x, y, hb, type) -> {
            if (!brandungAn || type == Surf.Breaker.PLUNGING) return;   // der Sturzbrecher klingt beim Aufschlag der Lippe
            double yo = yOhr;
            if (Double.isNaN(yo)) return;
            for (int k = 0; k < 3; k++) {
                double yt = yo + (k - 1) * SEITE;
                if (yt < 0 || yt > st.zug.width || time - lastSpill[k] < 1.2) continue;
                lastSpill[k] = time;
                double[] p = st.welt(x, yt);
                schwallbrecher++;
                ev(Kind.SCHWALLBRECHER, p[0], r.wiring.pegel + 0.3 * hb, p[1], 0.85 + 0.15 * rng.nextDouble(), hb * (type == Surf.Breaker.SPILLING ? 1 : 0.8));
            }
        };
        st.zug.onImpact = (x, y, z, vol, speed) -> {
            if (!brandungAn) return;
            double yo = yOhr;
            if (Double.isNaN(yo)) return;
            for (int k = 0; k < 3; k++) {
                double yt = yo + (k - 1) * SEITE;
                if (Math.abs(y - yt) > SEITE / 2 || time - lastPlunge[k] < 1.2) continue;
                lastPlunge[k] = time;
                double hb = Math.sqrt(Math.max(0, vol) * st.zug.rows / (0.12 * st.zug.width));
                double[] p = st.welt(x, y);
                sturzbrecher++;
                ev(Kind.STURZBRECHER, p[0], z, p[1], 0.85 + 0.15 * rng.nextDouble(), hb);
            }
        };
    }
}
