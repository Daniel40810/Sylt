package com.dan.sylt.scene;

import com.dan.dune.Dune;
import com.dan.dune.FluxField;
import com.dan.dune.Grains;
import com.dan.wind.WindField;
import com.dan.wind.WindSolution;

import java.util.Locale;
import java.util.function.DoubleBinaryOperator;

/**
 * Der Flugsand in der Szene (seit Phase 9a): {@code com.dan.dune} mit dem Wind aus {@code com.dan.wind} (F21 A).
 * <ul>
 * <li><b>Wind:</b> ein massenkonsistentes Feld über dem Kern ({@link WindField#solve}, im eigenen Faden, neu bei
 * anderem Wind); der Sand nimmt den Wind in 10 m über dem Grund am Ort, also schwächer im Lee des Kliffs und der Dünen,
 * stärker über den Kämmen. Rauigkeit nach der Art des Grunds.</li>
 * <li><b>Nässe:</b> aus dem nassen Sand der Szene ({@code Strandnaesse}: Pegel, Auflauf, Schwall; ab 0,6 der Skala zählt
 * sie, gesättigt 22 % Wasser); nach Regen ein Boden für alles ({@link #regen}). Der nasse Strand fliegt nicht.</li>
 * <li><b>Bewuchs:</b> nach der Art des Grunds (KernKarte): Strand und Düne kahl oder licht, Strandhafer λ ≈ 0,08,
 * Heide, Gebüsch, Wiese, Siedlung, Kliff und Hänge steiler als der Schüttwinkel geben keinen Sand.</li>
 * <li><b>Feld und Körner:</b> ein {@link FluxField} um das Auge (240 m in Windrichtung, 160 m quer, 0,5 m), im eigenen
 * Faden neu, wenn das Auge 50 m gewandert ist oder Wind, Pegel oder Regen sich geändert haben; die Körner
 * ({@link Grains}) springen um das Auge.</li>
 * </ul>
 */
public final class Sand {
    /** Das Feld um das Auge: Länge in Windrichtung, quer, Zelle (m), Weg des Auges bis zum Neubau (m). */
    public static final double LAENGS = 240, QUER = 160, ZELLE = 0.5, WANDERN = 50;

    /** Art des Grunds am Ort (Terrain.STRAND …), Nässe des Sands 0..1 aus der Szene (NaN außerhalb), Höhe des Grunds. */
    public interface Art { byte at(double x, double z); }

    public final Dune dune;
    public final WindField wind = WindField.westlage();
    private final DoubleBinaryOperator grund;
    private final Art art;
    private final DoubleBinaryOperator nass;
    private final java.util.function.DoubleSupplier pegel;
    /** Ob Sand fliegt (Block SAND) und Nässe nach Regen als Massenanteil (0 trocken … 0,06 durchnässt). */
    public volatile boolean an = true;
    public volatile double regen;
    private volatile FluxField feld;
    private volatile Grains koerner;
    private volatile Thread bauFeld, bauWind;
    private double windV = Double.NaN, windR = Double.NaN, pegelF = Double.NaN, regenF = Double.NaN, cxF = Double.NaN, czF = Double.NaN;
    private double windGeloest = Double.NaN, richtungGeloest = Double.NaN;
    public volatile String meldung = "noch kein Feld";
    public volatile double windMs, feldMs, koernerMs;
    private double t;

    /**
     * @param grund Höhe des Geländes, @param art Art des Grunds, @param nass Nässe des Sands 0..1 (NaN: unbekannt),
     * @param pegel Wasserstand (m NHN)
     */
    public Sand(DoubleBinaryOperator grund, Art art, DoubleBinaryOperator nass, java.util.function.DoubleSupplier pegel) {
        this.grund = grund;
        this.art = art;
        this.nass = nass;
        this.pegel = pegel;
        dune = Dune.sylt();
        dune.seed = 2026;
        dune.ground = grund::applyAsDouble;
        dune.moisture = this::feuchte;
        dune.cover = this::bewuchs;
        wind.seed = 2026;
        wind.quality = WindField.Quality.MEDIUM;
        wind.ground = grund::applyAsDouble;
        wind.roughness = this::rauigkeit;
        dune.wind = new Dune.Wind() {
            public double speed(double x, double z) { double[] v = wind10(x, z); return Math.hypot(v[0], v[2]); }
            public double fromDeg(double x, double z) {
                double[] v = wind10(x, z);
                if (Math.hypot(v[0], v[2]) < 1e-3) return wind.fromDeg;
                return (Math.toDegrees(Math.atan2(v[0], -v[2])) + 180 + 360) % 360;
            }
        };
        koerner = new Grains(dune);
    }

    /** Wind 10 m über dem Grund am Ort {Ost, oben, Süd}. */
    double[] wind10(double x, double z) {
        double[] v = wind.windAt(x, grund.applyAsDouble(x, z) + 10, z, 0);
        // bis das neue Feld fertig ist: das alte auf die Stärke der Lage skaliert
        double k = wind.solution != null && geloestMit > 0.1 ? wind.speed / geloestMit : 1;
        return new double[]{v[0] * k, v[1] * k, v[2] * k};
    }

    private volatile double geloestMit;

    /** Nässe des Sands als Massenanteil: aus der Szene (0,22 Rest = trocken, 1 = gesättigt), unter dem Pegel nass, nach Regen mindestens {@link #regen}. */
    double feuchte(double x, double z) {
        double w = nass.applyAsDouble(x, z), m;
        if (Double.isNaN(w)) {
            double ueber = grund.applyAsDouble(x, z) - pegel.getAsDouble();
            m = ueber < 0 ? 0.25 : ueber < 1.5 ? 0.25 * (1 - ueber / 1.5) + 0.0003 : 0.0003;
        } else {
            // die Nässe der Szene ist das Bild des Sands (dunkel bis zum Rand des Auflaufs und ein Stück darüber); die
            // Oberfläche trocknet im Wind schnell: erst über 0,6 zählt sie für den Flug, gesättigt 22 % [G]
            double a = Math.max(0, (w - 0.6) / 0.35);
            m = 0.22 * Math.min(1, a * a) + 0.0003;
        }
        if (grund.applyAsDouble(x, z) < pegel.getAsDouble()) m = 0.25;
        return Math.max(m, regen);
    }

    /** Bewuchs λ nach der Art des Grunds (1 = kein loser Sand). */
    double bewuchs(double x, double z) {
        // steiler als der Schüttwinkel (Kliffwand): dort liegt kein loser Sand
        double gx = grund.applyAsDouble(x + 1, z) - grund.applyAsDouble(x - 1, z), gz = grund.applyAsDouble(x, z + 1) - grund.applyAsDouble(x, z - 1);
        if (Math.hypot(gx, gz) / 2 > Math.tan(Math.toRadians(dune.reposeDeg))) return 1;
        return switch (art.at(x, z)) {
            // die Karte kennt den Strand nur, wie ihn das Luftbild bei seinem Wasserstand zeigt: über dem Pegel ist „See“ Strand
            case Terrain.STRAND, Terrain.NASS, Terrain.KEINE, Terrain.SEE -> 0;
            case Terrain.DUENE -> 0.02;
            case Terrain.STRANDHAFER -> 0.06 + 0.04 * Math.sin(x * 0.9 + Math.sin(z * 0.7) * 2) * Math.sin(z * 1.3);
            default -> 1;
        };
    }

    /** Rauigkeit für das Windfeld (m). */
    double rauigkeit(double x, double z) {
        return switch (art.at(x, z)) {
            case Terrain.SEE, Terrain.WATT -> 0.0002;
            case Terrain.STRAND, Terrain.NASS -> 0.001;
            case Terrain.DUENE, Terrain.STRANDHAFER -> 0.03;
            case Terrain.HEIDE, Terrain.WIESE, Terrain.KLIFF -> 0.05;
            case Terrain.GEBUESCH -> 0.3;
            case Terrain.SIEDLUNG, Terrain.DACH -> 0.8;
            default -> 0.03;
        };
    }

    /**
     * Je Bild: Wind der Lage übernehmen (neues Windfeld im Hintergrund, wenn er sich deutlich ändert), Feld um das Auge
     * bei Bedarf neu (Hintergrund), Körner um das Auge einen Schritt.
     */
    public void schritt(double dt, double[] auge, double windSpeed, double windFromDeg) {
        wind.speed = windSpeed;
        wind.fromDeg = windFromDeg;
        loeseWind(windSpeed, windFromDeg, false);
        if (!an) return;
        double p = pegel.getAsDouble();
        boolean neu = feld == null || Math.hypot(auge[0] - cxF, auge[2] - czF) > WANDERN || Math.abs(windSpeed - windV) > 0.8
                || Math.abs(winkel(windFromDeg - windR)) > 6 || Math.abs(p - pegelF) > 0.1 || Math.abs(regen - regenF) > 0.002;
        if (neu && bauFeld == null) {
            final double cx = auge[0], cz = auge[2];
            windV = windSpeed; windR = windFromDeg; pegelF = p; regenF = regen; cxF = cx; czF = cz;
            Thread th = new Thread(() -> {
                try { baueFeld(cx, cz); } catch (RuntimeException e) { meldung = "Feld: " + e.getMessage(); } finally { bauFeld = null; }
            }, "sand-feld");
            th.setDaemon(true);
            bauFeld = th;
            th.start();
        }
        FluxField f = feld;
        if (f == null || dt <= 0) return;
        long t0 = System.nanoTime();
        t += dt;
        koerner.step(Math.min(0.05, dt), f, auge[0], auge[2]);
        koernerMs = koernerMs * 0.9 + (System.nanoTime() - t0) / 1e6 * 0.1;
    }

    static double winkel(double d) { return ((d % 360) + 540) % 360 - 180; }

    /** Das Windfeld über dem Kern neu (im eigenen Faden, wenn {@code sofort} falsch). */
    public void loeseWind(double v, double r, boolean sofort) {
        Thread laeuft = bauWind;
        if (sofort && laeuft != null) {
            try { laeuft.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        boolean gleich = !Double.isNaN(windGeloest) && Math.abs(v - windGeloest) < 1.5 && Math.abs(winkel(r - richtungGeloest)) < 10;
        if (gleich && (!sofort || wind.solution != null)) return;
        if (bauWind != null && !sofort) return;
        windGeloest = v;
        richtungGeloest = r;
        double[] k = Site.KERN.scene();
        WindField w = wind.copy();
        w.speed = v;
        w.fromDeg = r;
        Runnable job = () -> {
            long t0 = System.nanoTime();
            WindSolution s = w.solve(Math.min(k[0], k[2]), Math.min(k[1], k[3]), Math.max(k[0], k[2]), Math.max(k[1], k[3]));
            wind.solution = s;
            geloestMit = v;
            windMs = (System.nanoTime() - t0) / 1e6;
            regenF = Double.NaN;   // Feld mit dem neuen Wind neu
        };
        if (sofort) { job.run(); return; }
        Thread th = new Thread(() -> { try { job.run(); } finally { bauWind = null; } }, "sand-wind");
        th.setDaemon(true);
        bauWind = th;
        th.start();
    }

    /** Das Feld um (cx, cz) neu (im Faden des Aufrufers). */
    public FluxField baueFeld(double cx, double cz) {
        long t0 = System.nanoTime();
        FluxField f = new FluxField(dune, cx, cz, LAENGS, QUER, ZELLE).compute();
        feld = f;
        feldMs = (System.nanoTime() - t0) / 1e6;
        meldung = f.describe();
        return f;
    }

    public FluxField feld() { return feld; }

    /** Für Standbilder und Prüfung: Windfeld und Feld um das Auge sofort rechnen und die Körner so viele Sekunden vorlaufen lassen. */
    public void bereit(double[] auge, double windSpeed, double windFromDeg, double sekunden) {
        wind.speed = windSpeed;
        wind.fromDeg = windFromDeg;
        loeseWind(windSpeed, windFromDeg, true);
        windV = windSpeed; windR = windFromDeg; pegelF = pegel.getAsDouble(); regenF = regen; cxF = auge[0]; czF = auge[2];
        baueFeld(auge[0], auge[2]);
        for (double s = 0; s < sekunden; s += 1 / 60.0) { t += 1 / 60.0; koerner.step(1 / 60.0, feld, auge[0], auge[2]); }
    }

    public Grains koerner() { return koerner; }

    /** „Flugsand: Wind hier 21 m/s aus 278° (Feld: 3,1 s) · u* 0,74 m/s, Schwelle 0,26 m/s · fliegt 160 g/(m·s) · 18 400 Körner“ */
    public String zeile(double x, double z) {
        if (!an) return "Flugsand: aus";
        FluxField f = feld;
        double[] v = wind10(x, z);
        double sp = Math.hypot(v[0], v[2]);
        String w = String.format(Locale.GERMAN, "Wind hier %.0f m/s aus %03.0f°%s", sp, dune.wind.fromDeg(x, z), wind.solution != null
                ? String.format(Locale.GERMAN, " (Feld aus wind, %.1f s)", windMs / 1000) : " (Näherung, Feld rechnet)");
        if (f == null) return "Flugsand: " + w + " · Feld wird gerechnet";
        double q = f.fluxAt(x, z), us = f.ustarAt(x, z), ut = f.thresholdAt(x, z);
        String hier = Double.isNaN(q) ? "außerhalb des Felds" : q > 1e-5 ? String.format(Locale.GERMAN, "fliegt %.0f g/(m·s)", q * 1000)
                : dune.moisture.moisture(x, z) > 0.01 ? "nass, liegt" : us < ut ? "liegt (unter der Schwelle)" : "liegt";
        return String.format(Locale.GERMAN, "Flugsand: %s · u* %.2f m/s, Schwelle %.2f m/s · %s · %d Körner (%.1f ms)", w,
                Double.isNaN(us) ? 0 : us, Double.isNaN(ut) ? 0 : ut, hier, koerner.count, koernerMs);
    }
}
