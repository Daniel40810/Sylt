package com.dan.sylt.scene;

import com.dan.flock.Flock;
import com.dan.flock.Shorebirds;
import com.dan.flock.WaderSpecies;
import com.dan.mudflat.TrackMap;
import com.dan.mudflat.WetField;
import com.dan.seal.Colony;
import com.dan.seal.Seal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleBinaryOperator;

/**
 * Seit Phase 13: das Leben am Strand (F24 A), alles aus {@code Renderer.jar}:
 * <ul>
 * <li><b>Möwen</b> ({@code com.dan.flock}): Silbermöwen um das Kliff gegenüber dem Auge. Bei auflandigem Wind stehen sie im
 * Hangaufwind vor der Kante (die Kante aus {@code kante.csv} als {@link Flock.Ridge} je rund 100 m, F21: den Aufwind an
 * der senkrechten Kante regelt die App); sie sitzen auf den Pfählen der Buhnen und auf der Grasnarbe, abends fliegen sie
 * ins Watt.</li>
 * <li><b>Sanderlinge, Austernfischer und Möwen am Spülsaum</b> ({@link Shorebirds}) im Stück der {@link Nahbrandung}:
 * die Kante des Schwalls Reihe für Reihe, die Sanderlinge rennen vor jeder Welle her und hinter ihr zurück.</li>
 * <li><b>Spuren im nassen Sand</b> (Ideenspeicher): jeder Tritt in 40 m um das Auge auf nassem Sand wird ein Abdruck
 * ({@link TrackMap}, 2,5 cm Zellen), der nächste Schwall löscht ihn.</li>
 * <li><b>Seehund, selten</b> ({@code com.dan.seal}, F24 A): in etwa einer von sechs Stunden schwimmt einer 100 bis 150 m
 * vor dem Strand und taucht.</li>
 * </ul>
 */
public final class Strandleben {
    public final Flock moewen = new Flock();
    public final Shorebirds strand = new Shorebirds();
    public final Colony seehund = new Colony(Seal.harbourSeal());
    /** Spuren der Vögel im nassen Sand und ihr Raster um das Auge. */
    public final TrackMap spurenKarte = new TrackMap(1024, 0.025);
    private final List<WetField.Track> spuren = new ArrayList<>();
    public static final int MAX_SPUREN = 9000;
    public volatile boolean anMoewen = true, anStrand = true, anSpuren = true, anSeehund = true;
    private final DoubleBinaryOperator grund;
    private final Kante kante;
    private double homeTakt, spurTakt, augeX, augeZ;
    private Nahbrandung.Stueck stueck;
    private boolean seehundDa;
    private long seehundStunde = Long.MIN_VALUE;
    private double pegel;
    public int spurenGeloescht;

    public Strandleben(DoubleBinaryOperator grund, Kante kante, List<Buhnen.Pfahl> pfaehle) {
        this.grund = grund;
        this.kante = kante;
        moewen.seed = 2026;
        moewen.gullCount = 14;
        moewen.homeRadius = 260;
        moewen.coastDeg = 100;   // abends und im Sturm ins Watt im Osten
        moewen.ground = grund::applyAsDouble;
        // die Kante als Hangkanten (je rund 100 m), die Wand zur See
        if (kante != null) {
            int step = Math.max(1, kante.n / 31);
            for (int i = 0; i + step < kante.n; i += step) {
                int j = i + step;
                double top = Math.max(kante.hKante[i], kante.hKante[j]), h = Math.max(4, top - Math.min(kante.hFuss[i], kante.hFuss[j]));
                moewen.ridges.add(new Flock.Ridge(kante.kanteX(i), kante.z(i), kante.kanteX(j), kante.z(j), top, h, -Kueste.SX, -Kueste.SZ));
            }
            // Sitzplätze auf der Grasnarbe an der Kante (alle rund 150 m)
            for (int i = 0; i < kante.n; i += Math.max(1, kante.n / 20)) {
                double x = kante.kanteX(i) + Kueste.SX * 1.5, z = kante.z(i) + Kueste.SZ * 1.5;
                moewen.perches.add(new Flock.Perch("Kante N " + Math.round(kante.nord[i]), x, grund.applyAsDouble(x, z), z).appeal(0.6));
            }
        }
        // Sitzplätze auf den Pfählen der Buhnen (jeder vierte), nur über Wasser frei
        for (int k = 0; k < pfaehle.size(); k += 4) {
            Buhnen.Pfahl p = pfaehle.get(k);
            moewen.perches.add(new Flock.Perch("Buhne " + p.buhne(), new Flock.Spot() {
                public double[] position() { return new double[]{p.x(), p.kopf(), p.z()}; }
                public boolean usable() { return p.kopf() > pegel + 0.25; }
            }).appeal(1.5));
        }
        strand.seed = 2026;
        strand.onStep = (x, z, dx, dz, art) -> tritt(x, z, dx, dz, art);
        seehund.seal.seed = 2026;
        seehund.count = 1;
    }

    /** Schalter für Stufen: Zahl der Strandvögel und Möwen. */
    public void stufe(int q) {
        moewen.gullCount = q == 0 ? 8 : q == 1 ? 14 : q == 2 ? 20 : 28;
        strand.quality = com.dan.flock.Waders.Quality.values()[Math.max(0, Math.min(3, q))];
    }

    /**
     * Je Bild: Wind, Licht, Jahr, Auge als Störer; das Revier der Möwen folgt dem Ziel der Kamera auf die Kante; der
     * Spülsaum im Stück der Nahbrandung; Spuren löschen, wo der Schwall läuft; der Seehund in seiner Stunde.
     */
    public void schritt(double dt, double[] auge, double tx, double tz, double windSpeed, double windFromDeg, double lux, double sonne,
                        java.time.LocalDate tag, double stunde, double pegel, Nahbrandung.Stueck s, double wellen) {
        this.pegel = pegel;
        augeX = auge[0];
        augeZ = auge[2];
        // Möwen
        if (anMoewen) {
            moewen.windSpeed = windSpeed;
            moewen.windFromDeg = windFromDeg;
            moewen.ambientLux = lux;
            moewen.dayOfYear = tag.getDayOfYear();
            homeTakt -= dt;
            if (homeTakt <= 0) {
                homeTakt = 8;
                double[] h = kantePunkt(tx, tz);
                moewen.homeX = h[0];
                moewen.homeZ = h[1];
            }
            moewen.disturbers.clear();
            moewen.disturbers.add(new double[]{auge[0], auge[1], auge[2]});
            moewen.step(dt);
        }
        // Spülsaum
        stueck = s;
        if (anStrand && s != null) {
            double w = s.zug.width;
            strand.frame(s.ox, s.oz, Kueste.SX, Kueste.SZ, Kueste.AX, Kueste.AZ, 6, w - 6);
            strand.shore = new Shorebirds.Shore() {
                public double ground(double x, double z) { return grund.applyAsDouble(x, z); }
                public double depth(double x, double z) { return tiefe(s, x, z); }
                public double edge(double v) {
                    if (v < 0 || v > s.zug.width) return Double.NaN;
                    int r = Math.min(s.zug.rows - 1, Math.max(0, (int) (v / s.zug.width * s.zug.rows)));
                    return s.schwall.tipX(r);
                }
                public double top(double v) { return s.xEnde; }
            };
            strand.month = tag.getMonthValue();
            strand.sunAltDeg = sonne;
            strand.windSpeed = windSpeed;
            strand.windFromDeg = windFromDeg;
            strand.disturbers.clear();
            strand.disturbers.add(new double[]{auge[0], auge[2], auge[1] - grund.applyAsDouble(auge[0], auge[2]) < 6 ? 1 : 0.3});
            strand.step(dt);
        }
        // Spuren: wo das Wasser steht, sind sie fort; das Raster folgt dem Auge
        spurTakt -= dt;
        if (spurTakt <= 0) {
            spurTakt = 0.2;
            if (s != null) {
                int vor = spuren.size();
                spuren.removeIf(t -> tiefe(s, t.x(), t.z()) > 0.004);
                spurenGeloescht += vor - spuren.size();
            }
            if (!anSpuren) spuren.clear();
            spurenKarte.update(spuren, auge[0], auge[2]);
        }
        // Seehund: selten (je Stunde gewürfelt), 100 bis 150 m vor dem Strand gegenüber dem Auge
        long std = (long) Math.floor(tag.toEpochDay() * 24 + stunde);
        if (std != seehundStunde) {
            seehundStunde = std;
            long hsh = (std * 0x9E3779B97F4A7C15L) ^ 2026;
            hsh ^= hsh >>> 31;
            seehundDa = anSeehund && Math.floorMod(hsh, 6) == 0 && wellen < 3.5;
            if (seehundDa) {
                double[] wl = Kueste.waterline(tx, tz);
                double off = 100 + 50 * Math.floorMod(hsh >>> 8, 100) / 100.0;
                seehund.banks.clear();
                seehund.banks.add(new Colony.Bank(wl[0] - Kueste.SX * off, wl[1] - Kueste.SZ * off, 25));
                seehund.world = new Colony.World() {
                    public double ground(double x, double z) { return grund.applyAsDouble(x, z); }
                    public double water(double x, double z) { return grund.applyAsDouble(x, z) < Strandleben.this.pegel - 0.4 ? Strandleben.this.pegel : Double.NaN; }
                };
                seehund.month = tag.getMonthValue();
                seehund.settle();
            }
        }
        if (seehundDa) {
            seehund.sunAltDeg = sonne;
            seehund.disturbers.clear();
            seehund.step(Math.min(1, dt));
        }
    }

    public boolean seehundDa() { return seehundDa && anSeehund; }

    /** Wassertiefe am Ort im Stück (Schwall eingeschlossen); außerhalb: unter dem Pegel tief, darüber trocken. */
    double tiefe(Nahbrandung.Stueck s, double x, double z) {
        double[] l = s.lokal(x, z);
        if (l[1] >= 0 && l[1] <= s.zug.width && l[0] >= s.schwall.boundaryX() && l[0] <= s.xEnde) {
            int r = Math.min(s.zug.rows - 1, Math.max(0, (int) (l[1] / s.zug.width * s.zug.rows)));
            return Math.max(0, s.schwall.depth(r, l[0]));
        }
        return Math.max(0, pegel - grund.applyAsDouble(x, z));
    }

    /** Ein Tritt: nur in 40 m um das Auge, nur auf nassem Sand (Nässe aus dem Schwall über 0,35), höchstens MAX_SPUREN. */
    private void tritt(double x, double z, double dx, double dz, WaderSpecies art) {
        if (!anSpuren || stueck == null || Math.hypot(x - augeX, z - augeZ) > 40) return;
        double n = stueck.nass(x, z);
        if (Double.isNaN(n) || n < 0.35) return;
        spuren.add(new WetField.Track(x, z, dx, dz, art == WaderSpecies.SANDERLING ? 5 : 6, 0));
        if (spuren.size() > MAX_SPUREN) spuren.remove(0);
    }

    /** Der Punkt auf der Kliffkante gegenüber (x, z), etwas vor der Wand; ohne Kante der Punkt selbst. */
    double[] kantePunkt(double x, double z) {
        if (kante == null) return new double[]{x, z};
        int r = kante.row(z);
        if (r < 0) {
            double nn = Site.N0 - z;
            r = nn > kante.nord[0] ? 0 : nn < kante.nord[kante.n - 1] ? kante.n - 1 : -1;
            if (r < 0) return new double[]{x, z};
        }
        return new double[]{kante.kanteX(r) - Kueste.SX * 40, kante.z(r) - Kueste.SZ * 40};
    }

    public int spuren() { return spuren.size(); }

    /** Für das HUD: zwei Zeilen (Möwen und Spülsaum, Spuren und Seehund). */
    public String[] describe() {
        String a = (anMoewen ? moewen.describe() : "Möwen: aus") + " · " + (anStrand ? (stueck == null ? "Spülsaum: kein Stück der Brandung nahe" : strand.describe()) : "Spülsaum: aus");
        String b = String.format(Locale.GERMAN, "Spuren im nassen Sand: %,d (%,d gelöscht vom Schwall) · %s", spuren.size(), spurenGeloescht,
                seehundDa() ? "ein Seehund vor dem Strand: " + seehund.describe() : "kein Seehund (selten: etwa eine Stunde von sechs)");
        return new String[]{a, b};
    }
}
