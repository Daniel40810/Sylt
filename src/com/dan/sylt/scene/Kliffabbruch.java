package com.dan.sylt.scene;

import com.dan.cliff.Cliff;
import com.dan.cliff.StrataMap;
import com.dan.fracture.Fracture;
import com.dan.fracture.Material;
import com.dan.fracture.Piece;
import com.dan.fracture.Poly;
import com.dan.fracture.Shape;
import com.dan.fracture.Solid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Der Abbruch des Kliffs (seit Phase 12, F17 A): die Brandung schlägt bei Sturmflut die Hohlkehle, {@code fracture}
 * bricht das Kliff darüber, die Stücke fallen auf den Strand und zerfallen zum Sturzkegel, die nächste Flut trägt ihn ab,
 * die Kante liegt danach weiter landwärts.
 * <ol>
 * <li><b>Hohlkehle je Zeile der Kante</b> (1 m): erreichen Pegel und Auflauf (R2 % nach Stockdon) den Fuß, frisst die
 * Brandung die Kehle mit K·a^1,5 m/h (a = Höhe des Auflaufs über dem Fuß, K = {@link #K_KEHLE} [G]); im Kaolinsand
 * schneller ({@link #SAND}), hinter einem Sturzkegel langsamer (er schützt den Fuß). Die Kehle geht in die Schichtkarte
 * von {@code cliff} (die Wand weicht im Bild zurück, ab {@link #KEHLE_UNTEN} m NHN {@link #KEHLE_HOCH} m hoch); im Körper
 * liegt sie je Zeile vom Fuß an {@link #KEHLE_HOCH} m hoch.</li>
 * <li><b>Abbruch nach der Physik:</b> erreicht die Kehle irgendwo {@link #AUSLOESEN} der kritischen Tiefe des
 * Überhangs L_c = √(f_t·h / 3ρg) (Phase 11), wird dort ein Stück Kliff ({@link #LAENGE} m längs, vom Fuß bis
 * {@link #LAND} m hinter die Kante) als Körper aus Geschiebelehm über Kaolinsand in Zellen zerlegt, mit der Kehle
 * ausgespült und der Spannung überlassen. Bricht er, fallen die Stücke; per Knopf lässt er sich an einer gewählten Stelle
 * auslösen ({@link #ausloesen}).</li>
 * <li><b>Gelände:</b> wo Zellen fehlen, sinkt die Oberfläche auf den verbliebenen Körper; was zerfällt, liegt als Kegel
 * (Böschung 34°, Auflockerung des Stoffs) auf dem Strand ({@link Aenderung}). Die nächste Flut trägt Kegel ab, die sie
 * erreicht, nie unter das alte Gelände.</li>
 * <li><b>Sandbilanz:</b> Kliff vorher = steht + Blöcke + zerfallen + ausgespült (aufs m³, aus {@code fracture});
 * Sturzkegel im Gelände = zerfallen · Auflockerung; dazu Abtrag und die Kehle ohne Abbruch.</li>
 * </ol>
 */
public final class Kliffabbruch implements Physik.Aufgabe {
    /** Kehle: m/h je m^1,5 Auflauf über dem Fuß [G: Xaver mit 1,2 m Auflauf rund 0,5 m/h], Faktor im Kaolinsand. */
    public static final double K_KEHLE = 0.38, SAND = 1.6;
    /** Band der Kehle im Bild (m NHN, der Fuß liegt meist um +2 bis +3,5 m) und ihre Höhe über dem Fuß (m). */
    public static final double KEHLE_UNTEN = 2.4, KEHLE_HOCH = 2.6;
    /** Ein Abbruch wird gerechnet ab diesem Anteil der kritischen Tiefe des Überhangs. */
    public static final double AUSLOESEN = 0.6;
    /** Stück Kliff, das gerechnet wird: Länge längs (m), Tiefe hinter der Kante (m), Zellen auf „Mittel“. */
    public static final double LAENGE = 30, LAND = 8;
    public static final int ZELLEN = 1200;
    /**
     * Dicke des gerechneten Körpers am Hang (m) [G]: Rutschungen am Roten Kliff reichen einige Meter tief. So bleibt der
     * Körper unter der Höhe, die das Modell ohne Seitendruck (ν = 0, Phase 11) zerdrücken würde (σ_c/ρg ≈ 20 m).
     */
    public static final double TIEFE = 10;
    /**
     * Der Lehm des Kliffs im großen Körper [G]: die Festigkeit im Gebirge, nicht die einer Probe (Klüfte, Wurzeln,
     * Sandlinsen): c' 30 kPa, φ 32°, Zug 10 kPa (Phase 11 rechnete die kleine Wand mit 120 kPa, damit sie ohne
     * Seitendruck nicht zerdrückt wird; das übernimmt hier {@code Solid.stabilize}). Senkrecht stünde so ein Anschnitt
     * rund 4c/γ ≈ 6 m hoch, der Hang mit 20 bis 30° steht – wie das Kliff.
     */
    public static Material lehm() {
        Material m = Material.geschiebelehm().mohrCoulomb(LEHM_C, 32, LEHM_ZUG);
        m.suction = Math.min(m.suction, LEHM_C / 4);
        return m;
    }
    public static double LEHM_C = 3.0e4, LEHM_ZUG = 1.0e4;
    /** Auslastung, auf die {@code Solid.stabilize} den Bestand hebt: Platz für Nässe (gesättigt 0,54 der Festigkeit) und Kehle. */
    public static double BESTAND = 0.5;

    /** So weit über dem Auflauf ist die Wand bei Sturm noch durchnässt (m) [G: Gischt und Regen]. */
    public static final double GISCHT = 2;
    /** Sättigung der durchnässten Wand [G: nicht ganz 1, Luft bleibt im Lehm]. */
    public static double NASS = 0.85;
    /** Abtrag der Kegel in der Flut: m/h je m Auflauf über ihnen [G]. */
    public static final double ABTRAG = 0.5;
    /** Böschung der Kegel (Schüttwinkel 34°). */
    public static final double BOESCHUNG = Math.tan(Math.toRadians(34));

    public final Terrain terrain;
    public final Kante kante;
    public final Cliff cliff;
    public final StrataMap strata;
    public final Aenderung aenderung;
    /** Nur was vom Kliff fehlt (m, ≤ 0), ohne Kegel: der Bestand für das nächste Stück und den Abtrag. */
    public final Aenderung weg;
    /** Tiefe der Kehle je Zeile der Kante (m) und was davon in der Schichtkarte steht. */
    public final double[] kehle;
    private final double[] gemalt, geprueft;
    /** Neue Kante je Zeile nach einem Abbruch (Ost, m; NaN: unverändert) und die alte. */
    public final double[] kanteNeu, kanteAlt;
    /** Abrisskante je Zeile (Ost, m; NaN: kein Abbruch): die landwärtigste Stelle, an der das Gelände um mehr als 0,3 m fehlt. */
    public final double[] abriss;
    /** Sedimente (m³): Kehle ohne Abbruch (fest), Abtrag (locker), Sturzkegel im Gelände (gemessen, locker). */
    public double vKehle, vAbtrag, vKegel;
    /** Wo Kliff fehlt, sank das Gelände um so viel (m³): zur Probe gegen Blöcke, zerfallen und ausgespült. */
    public double vAbgesenkt;
    /** Die Bilanz aller Abbrüche (m³ fest): Kliff vorher, steht, Blöcke, zerfallen, ausgespült. */
    public double vKliff0, vSteht, vBloecke, vZerfallen, vAusgespuelt;
    public int abbrueche;
    public volatile String meldung = "";
    /** Seit dem Schritt der Physik zählt die Zeit des Abbruchs in Echtzeit; die Kehle in Szenenzeit. */
    public volatile boolean manuell;
    /** Ruft die App beim Zerfall (Staub, F15 A): Ort und Volumen. */
    public volatile java.util.function.Consumer<double[]> onStaub;
    /**
     * Seit Phase 14: für den Klang. Art 0 = der erste Bruch eines Abschnitts (Ort an der Kante über der Kehle, Volumen
     * des Stücks), 1 = ein Brocken schlägt auf (Ort, Volumen, Tempo), 2 = ein Stück zerfällt zu Sand (Ort, Volumen).
     * Gerufen aus dem Faden der Physik.
     */
    @FunctionalInterface public interface Klang { void klang(int art, double x, double y, double z, double volumen, double tempo); }
    public volatile Klang onKlang;
    /**
     * Seit Phase 15 („Die Spur der Eiszeit“): nach jedem Abbruch, wo der Fuß der Wand vorher lag ({x, z} je Zeile, für
     * Pflöcke und eine Linie), und wo Findlinge aus dem Geschiebelehm am Strand liegen ({x, z, Halbmesser}).
     */
    public final java.util.List<double[]> spur = new java.util.concurrent.CopyOnWriteArrayList<>(), findlingStellen = new java.util.concurrent.CopyOnWriteArrayList<>();
    public volatile int spurVersion;
    public volatile Fracture.Quality qualitaet = Fracture.Quality.MEDIUM;
    public volatile boolean asyncSolve = true;
    /** Ob ein kritisch gewordenes Stück von selbst gerechnet wird (aus: nur die Kehle wächst, etwa für Prüfungen). */
    public volatile boolean rechnen = true;

    private volatile Schnitt schnitt;
    private final List<Schnitt> fertig = new ArrayList<>();
    private final java.util.Set<Integer> kegelZellen = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** Ein Stück Kliff, das gerechnet wird. */
    public final class Schnitt {
        public final int r0, r1, mitte;
        public final double x0, z0, x1, z1, yBase;
        public final Fracture welt;
        public final Solid solid;
        public final double volumen0;
        final double[] fuss, kant;   // je Zeile r0..r1: x des Fußes und der Kante
        final boolean[] verarbeitet;
        final double[] kehleImKoerper, wandX;
        /** Seit Phase 16: die Kante vor diesem Stück (Ost je Zeile r0..r1) und der Kegel beim Beginn (für die Chronik). */
        final double[] kanteVor;
        double kegel0;
        /** Flächen, die für den Bestand angehoben wurden ({@code Solid.stabilize}). */
        public int gestaerkt;
        double ruhig, alter;
        boolean gebrochen, beendet;

        public boolean gebrochen() { return gebrochen; }
        public boolean beendet() { return beendet; }
        int version = -1;

        Schnitt(int mitte, int r0, int r1) {
            this.mitte = mitte;
            this.r0 = r0;
            this.r1 = r1;
            int n = r1 - r0 + 1;
            fuss = new double[n];
            kant = new double[n];
            kanteVor = new double[n];
            for (int r = r0; r <= r1; r++) kanteVor[r - r0] = kante.kanteE[r];
            double xa = Double.MAX_VALUE, xb = -Double.MAX_VALUE, hb = Double.MAX_VALUE;
            for (int r = r0; r <= r1; r++) {
                fuss[r - r0] = Math.min(kante.fussX(r), kante.kanteX(r));
                // die Kante geglättet (±6 Zeilen): der Rand des Körpers landwärts soll keine Treppe sein
                double ks = 0;
                int nk = 0;
                for (int q = Math.max(r0, r - 6); q <= Math.min(r1, r + 6); q++) { ks += Math.max(kante.fussX(q), kante.kanteX(q)); nk++; }
                kant[r - r0] = ks / nk;
                xa = Math.min(xa, fuss[r - r0]);
                xb = Math.max(xb, kant[r - r0]);
                hb = Math.min(hb, kante.hFuss[r]);
            }
            x0 = Math.floor(xa - 1);
            x1 = Math.ceil(xb + LAND);
            double za = kante.z(r0), zb = kante.z(r1);
            z0 = Math.min(za, zb);
            z1 = Math.max(za, zb);
            yBase = hb - 0.4;
            welt = Fracture.geschiebelehm();
            welt.material = lehm();
            welt.seed = 2013 + mitte;
            welt.quality = qualitaet;
            welt.asyncSolve = asyncSolve;
            welt.maxPieces = 400;
            welt.ground = (x, z) -> innen(x, z) ? yBase : terrain.height(x, z);
            welt.water = (x, z, t) -> pegelJetzt;
            double kaolinFaktor = 0.8;
            Shape form = Shape.field(x0, z0, x1, z1, this::oben, this::unten, this::innen);
            Solid s = welt.prepare(form, welt.cellsFor(ZELLEN),
                    (x, y, z) -> 0.15 + 0.85 * Cliff.smooth(fussAn(z) + 25, fussAn(z) + 3, x),
                    (x, y, z) -> y < kaolinOben(z) ? kaolinFaktor : y > terrain.roh(x, z) - flugsand(z) ? 0.35 : 1);
            welt.add(s);
            // der Grund hält den Körper an seiner Unterseite: am Strand die Ebene unter dem Fuß, am Hang die Gleitfläche
            s.anchor(f -> f.ny < -0.2, 3);   // Randflächen nach unten: nur die Unterseite (die Oberseite ist ein Höhenfeld)
            s.support(c -> c[0] > kantAn(c[2]) + LAND - 3 || c[2] < z0 + 2.5 || c[2] > z1 - 2.5);
            solid = s;
            // der gemessene Hang steht: was das Zellmodell ohne Kehle schon bräche, ist Modell, nicht Hang (Auslastung 0,8)
            gestaerkt = s.stabilize(BESTAND);
            double v = 0;
            for (int i = 0; i < s.cells(); i++) v += s.volume(i);
            volumen0 = v;
            verarbeitet = new boolean[s.cells()];
            kehleImKoerper = new double[n];
            // die Wand auf halber Höhe der Kehle: dort setzt die Brandung an, von dort zählt ihre Tiefe
            wandX = new double[n];
            for (int r = r0; r <= r1; r++) {
                double z = kante.z(r), y = kante.hFuss[r] + KEHLE_HOCH / 2, x = fuss[r - r0] - 2;
                while (x < kant[r - r0] && oben(x, z) < y) x += 0.25;
                wandX[r - r0] = x;
            }
            welt.onDisintegrate = (vol, x, y, z, r, id) -> {
                if (id < 0) return;   // die Kehle im Körper zählt schon als ausgespült
                double locker = vol * welt.material.bulking;
                vKegel += kegel(x, z, locker);
                java.util.function.Consumer<double[]> st = onStaub;
                if (st != null) st.accept(new double[]{x, y, z, vol});
                Klang kl = onKlang;
                if (kl != null) kl.klang(2, x, y, z, vol, 0);
            };
            // Staub (F15 A) auch beim Aufschlag: Lehm, der auf den Strand schlägt, staubt, ohne ganz zu zerfallen
            welt.onImpact = (x, y, z, vol, speed, wasser) -> {
                java.util.function.Consumer<double[]> st = onStaub;
                if (st != null && !wasser && speed > 1.5 && vol > 0.02) st.accept(new double[]{x, y, z, vol * 0.3});
                Klang kl = onKlang;
                if (kl != null && speed > 1.0 && vol > 0.01) kl.klang(1, x, y, z, vol, speed);
            };
            welt.onBreak = (id, vol, m) -> {
                if (vol > 1 && !gebrochen) {
                    gebrochen = true;
                    abbrueche++;
                    meldung = String.format(Locale.GERMAN, "Abbruch bei N %,.0f: %.0f m³ brechen", kante.nord[mitte], vol);
                    Klang kl = onKlang;
                    if (kl != null) kl.klang(0, kante.kanteX(mitte) - 3, (kante.hKante[mitte] + kante.hFuss[mitte]) / 2, kante.z(mitte), vol, 0);
                }
            };
            kehleAuf();
            naesse(pegelJetzt + r2Jetzt);
        }

        private double nassBis = -99;

        /**
         * Nässe (F17 A): bei Sturmflut sind Fuß und Wand bis über den Auflauf durchnässt (Brandung, Gischt, Regen); dort
         * ist der Lehm gesättigt (S = 1: die Saugspannung fehlt, die eigene Kohäsion sinkt, {@code Material.strengthAt}).
         * Nur steigend: was einmal nass ist, bleibt es für die Dauer des Abbruchs.
         */
        void naesse(double wasser) {
            if (wasser < nassBis + 0.2) return;
            nassBis = wasser;
            double bis = wasser + GISCHT;
            solid.soak((x, y, z) -> y < bis && x - wandX[zeile(z)] < 10 ? NASS : Double.NaN);
        }

        /** Unterseite des Körpers: die Ebene unter dem Fuß, am Hang {@link #TIEFE} m unter dem Gelände (Gleitfläche). */
        public double unten(double x, double z) { return Math.max(yBase, oben(x, z) - TIEFE); }

        /**
         * Oberseite: das gemessene Gelände über 3 m gemittelt. Die Zellen werden an der Tangentialebene ihrer Saat
         * geschnitten; am rauen DGM1 gäbe das schiefe Splitter, die von selbst abbrechen.
         */
        public double oben(double x, double z) {
            double s = 0;
            for (int dj = -1; dj <= 1; dj++) for (int di = -1; di <= 1; di++) s += bestand(x + 1.5 * di, z + 1.5 * dj);
            return s / 9;
        }

        /** Liegt (x, z) im Körper (Grundriss: landwärts des Fußes)? */
        public boolean innen(double x, double z) {
            if (z < z0 || z > z1 || x > x1) return false;
            return x >= fussAn(z) - 0.5 && x <= kantAn(z) + LAND;
        }

        private int zeile(double z) {
            int r = kante.row(z);
            if (r < r0 || r > r1) r = z < (z0 + z1) / 2 ? (kante.z(r0) < kante.z(r1) ? r0 : r1) : (kante.z(r0) < kante.z(r1) ? r1 : r0);
            return Math.max(0, Math.min(fuss.length - 1, r - r0));
        }

        double fussAn(double z) { return fuss[zeile(z)]; }

        double kantAn(double z) { return kant[zeile(z)]; }

        /** Die Kehle der Zeilen in den Körper übertragen (ausspülen), wenn sie um mehr als 0,15 m gewachsen ist. */
        void kehleAuf() {
            boolean neu = false;
            for (int r = r0; r <= r1; r++) if (kehle[r] - kehleImKoerper[r - r0] > 0.15) { neu = true; break; }
            if (!neu) return;
            for (int r = r0; r <= r1; r++) kehleImKoerper[r - r0] = kehle[r];
            Solid s = solid;
            double v = s.erode(i -> {
                double[] c = s.center(i);
                int r = kante.row(c[2]);
                if (r < r0 || r > r1) return false;
                double tiefe = kehle[r], q = (c[1] - (kante.hFuss[r] + KEHLE_HOCH / 2)) / (KEHLE_HOCH / 2);
                if (Math.abs(q) >= 1 || tiefe <= 0) return false;
                return c[0] - wandX[r - r0] < tiefe * Math.sqrt(1 - q * q);
            });
            if (v > 0) meldung = String.format(Locale.GERMAN, "Hohlkehle bei N %,.0f: %.1f m³ ausgespült", kante.nord[mitte], welt.erodedVolume());
        }

        /** Gelände nachführen: wo Zellen fehlen, sinkt die Oberfläche auf den verbliebenen Körper (oder den Grund). */
        void gelaende() {
            Solid s = solid;
            int v = s.version();
            if (v == version) return;
            version = v;
            List<Integer> neu = new ArrayList<>();
            for (int i = 0; i < s.cells(); i++) if (!s.attached(i) && !verarbeitet[i]) { verarbeitet[i] = true; neu.add(i); }
            if (neu.isEmpty()) return;
            // betroffene Säulen zu 1 m
            java.util.Set<Long> saeulen = new java.util.HashSet<>();
            for (int i : neu) {
                double[] b = bbox(s.cell(i));
                for (int j = aenderung.j(b[2]); j <= aenderung.j(b[3]); j++)
                    for (int ii = aenderung.i(b[0]); ii <= aenderung.i(b[1]); ii++) {
                        double x = aenderung.cx(ii), z = aenderung.cz(j);
                        if (!innen(x, z)) continue;
                        if (!Double.isNaN(oberkante(s.cell(i), x, z))) saeulen.add(((long) j << 32) | (ii & 0xffffffffL));
                    }
            }
            // für jede Säule: Oberkante der Zellen, die noch stehen
            List<Integer> stehen = new ArrayList<>();
            for (int i = 0; i < s.cells(); i++) if (s.attached(i)) stehen.add(i);
            double[][] boxes = new double[s.cells()][];
            for (int i : stehen) boxes[i] = bbox(s.cell(i));
            for (long key : saeulen) {
                int j = (int) (key >> 32), ii = (int) key;
                double x = aenderung.cx(ii), z = aenderung.cz(j), top = Double.NaN;
                for (int i : stehen) {
                    double[] b = boxes[i];
                    if (x < b[0] || x > b[1] || z < b[2] || z > b[3]) continue;
                    double t = oberkante(s.cell(i), x, z);
                    if (!Double.isNaN(t) && (Double.isNaN(top) || t > top)) top = t;
                }
                double b0 = terrain.roh(x, z) + weg.at(ii, j);   // der Bestand ohne Kegel
                double ziel = Double.isNaN(top) ? yBase : Math.min(top, b0);
                if (ziel < b0 - 0.01) {
                    vAbgesenkt -= aenderung.add(ii, j, ziel - b0);
                    weg.add(ii, j, ziel - b0);
                }
            }
        }
    }

    private volatile double pegelJetzt, r2Jetzt;

    public Kliffabbruch(Terrain t, Kante k, Cliff c, StrataMap strata) {
        terrain = t;
        kante = k;
        cliff = c;
        this.strata = strata;
        kehle = new double[k.n];
        gemalt = new double[k.n];
        geprueft = new double[k.n];
        kanteNeu = new double[k.n];
        kanteAlt = new double[k.n];
        java.util.Arrays.fill(kanteNeu, Double.NaN);
        abriss = new double[k.n];
        java.util.Arrays.fill(abriss, Double.NaN);
        for (int i = 0; i < k.n; i++) kanteAlt[i] = k.kanteE[i];
        double xa = Double.MAX_VALUE, xb = -Double.MAX_VALUE, za = Double.MAX_VALUE, zb = -Double.MAX_VALUE;
        for (int i = 0; i < k.n; i++) {
            xa = Math.min(xa, Math.min(k.fussX(i), k.kanteX(i)));
            xb = Math.max(xb, Math.max(k.fussX(i), k.kanteX(i)));
            za = Math.min(za, k.z(i));
            zb = Math.max(zb, k.z(i));
        }
        double ax = Math.floor(xa - 90), az = Math.floor(za - 40);
        aenderung = new Aenderung(ax, az, 1, (int) Math.ceil(xb + LAND + 30 - ax), (int) Math.ceil(zb + 40 - az));
        weg = new Aenderung(ax, az, 1, aenderung.nx, aenderung.nz);
        t.korrektur = aenderung;
        if (c != null) {
            c.notchBase = KEHLE_UNTEN;
            c.notchHeight = KEHLE_HOCH;
        }
    }

    /** Das Gelände ohne Sturzkegel: gemessen, abzüglich dessen, was abgebrochen ist (m). */
    public double bestand(double x, double z) { return terrain.roh(x, z) + weg.delta(x, z); }

    /** Lose Sedimente über dem Bestand (m). */
    public double kegelHoehe(double x, double z) { return Math.max(0, aenderung.delta(x, z) - weg.delta(x, z)); }

    /** Oberfläche des Kaolinsands (m NHN) am Nordwert der Szene z [G, wie Kliff]: im Süden +7 m, nach Norden darunter. */
    static double kaolinOben(double z) {
        double nord = Site.N0 - z, t = Math.max(0, Math.min(1, (nord - 6_088_700) / 1_000.0));
        return 7 - 10 * t;
    }

    /** Flugsand oben auf dem Kliff (m) [G, wie Kliff]. */
    double flugsand(double z) {
        int r = kante.row(z);
        double hk = r < 0 ? 15 : kante.hKante[r];
        return 1 + 3 * Cliff.smooth(22, 10, hk);
    }

    /** Kritische Tiefe der Kehle (m) an der Zeile r: L_c = √(f_t·h / 3ρg), h = Kliff über der Kehle. */
    public double lc(int r) {
        Material m = lehm();
        double h = Math.max(2, Math.min(TIEFE + 4, kante.hKante[r] - kante.hFuss[r] - KEHLE_HOCH));
        return Math.sqrt(m.tensile * h / (3 * m.density * 9.81));
    }

    /** Wie hoch der Auflauf über den Fuß der Zeile r reicht (m; ≤ 0: die See erreicht ihn nicht). */
    public double reichweite(int r, double pegel, double r2) {
        double x = kante.fussX(r) - 1.5, z = kante.z(r);
        double fuss = Math.max(kante.hFuss[r], terrain.height(x, z));   // ein Sturzkegel hebt den Fuß
        return pegel + r2 - fuss;
    }

    /**
     * Ein Schritt in Szenenzeit (dh Stunden): die Kehle wächst, wo der Auflauf den Fuß erreicht; die Kegel werden
     * abgetragen; die Schichtkarte bekommt die Kehle; ein Abbruch beginnt, wo die Kehle kritisch wird. Aus dem Bildlauf.
     */
    public void kehlenSchritt(double dh, double pegel, double r2) {
        pegelJetzt = pegel;
        r2Jetzt = r2;
        if (dh <= 0) return;
        dh = Math.min(dh, 2);
        int best = -1;
        double bestQ = 0;
        for (int r = 0; r < kante.n; r++) {
            if (kante.wand[r] < 6) continue;
            double a = reichweite(r, pegel, r2);
            if (a > 0) {
                double sand = kaolinOben(kante.z(r)) > kante.hFuss[r] + 1 ? SAND : 1;
                double schutz = 1 / (1 + 2 * kegelHoehe(kante.fussX(r) - 1.5, kante.z(r)));
                double d = K_KEHLE * Math.pow(a, 1.5) * sand * schutz * dh;
                double alt = kehle[r];
                kehle[r] = Math.min(6, kehle[r] + d);
                vKehle += (kehle[r] - alt) * KEHLE_HOCH * Math.PI / 4;   // halbe Ellipse je m Länge
            }
            double q = kehle[r] / lc(r);
            // ein Stück, das gehalten hat, wird erst wieder gerechnet, wenn die Kehle dort 1 m tiefer ist
            if (q > bestQ && kehle[r] >= geprueft[r] + 1) { bestQ = q; best = r; }
            if (strata != null && kehle[r] - gemalt[r] > 0.1) {
                strata.paintNotch(kante.fussX(r), kante.z(r), 2.2, kehle[r]);
                gemalt[r] = kehle[r];
            }
        }
        abtrag(dh, pegel, r2);
        Schnitt s = schnitt;
        if (s == null && rechnen && best >= 0 && bestQ >= AUSLOESEN) beginne(best);
        else if (s != null) {
            s.kehleAuf();
            s.naesse(pegel + r2);
        }
    }

    /** Abtrag der Kegel, die der Auflauf erreicht (nie unter das gemessene Gelände). */
    void abtrag(double dh, double pegel, double r2) {
        if (kegelZellen.isEmpty()) return;
        double wasser = pegel + r2;
        List<Integer> leer = new ArrayList<>();
        for (int k : kegelZellen) {
            int i = k % aenderung.nx, j = k / aenderung.nx;
            double plus = aenderung.at(i, j) - weg.at(i, j);
            if (plus <= 0) { leer.add(k); continue; }
            double h = terrain.roh(aenderung.cx(i), aenderung.cz(j)) + aenderung.at(i, j);
            double a = wasser - h;
            if (a <= 0) continue;
            double weg = Math.min(plus, ABTRAG * a * dh);
            vAbtrag += -aenderung.add(i, j, -weg);
            if (plus - weg <= 1e-4) leer.add(k);
        }
        kegelZellen.removeAll(leer);
    }

    /** Einen Kegel aus losem Sediment (m³) um (x, z) aufschütten (Böschung 34°); gibt das aufgeschüttete Volumen zurück. */
    double kegel(double x, double z, double locker) {
        if (locker <= 0) return 0;
        double r = Math.cbrt(3 * locker / (Math.PI * BOESCHUNG)), h0 = BOESCHUNG * r;
        r = Math.max(r, 0.8);
        h0 = 3 * locker / (Math.PI * r * r);
        int ri = (int) Math.ceil(r) + 1;
        int ci = aenderung.i(x), cj = aenderung.j(z);
        // erst die Gewichte (Kegel auf dem Raster), dann genau das Volumen verteilen
        double sum = 0;
        double[] w = new double[(2 * ri + 1) * (2 * ri + 1)];
        for (int dj = -ri; dj <= ri; dj++)
            for (int di = -ri; di <= ri; di++) {
                double d = Math.hypot(aenderung.cx(ci + di) - x, aenderung.cz(cj + dj) - z);
                double v = Math.max(0, 1 - d / r);
                w[(dj + ri) * (2 * ri + 1) + di + ri] = v;
                sum += v;
            }
        if (sum <= 0) return 0;
        double gesamt = 0;
        for (int dj = -ri; dj <= ri; dj++)
            for (int di = -ri; di <= ri; di++) {
                double v = w[(dj + ri) * (2 * ri + 1) + di + ri];
                if (v <= 0) continue;
                int i = ci + di, j = cj + dj;
                if (i < 0 || j < 0 || i >= aenderung.nx || j >= aenderung.nz) continue;
                gesamt += aenderung.add(i, j, locker * v / sum / (aenderung.cell * aenderung.cell));
                kegelZellen.add(j * aenderung.nx + i);
            }
        return gesamt;
    }

    /** Ein Stück Kliff um die Zeile r rechnen lassen. */
    void beginne(int r) {
        int half = (int) Math.round(LAENGE / 2);
        // Zeilen der Kante liegen je rund 1 m; der Abschnitt reicht, soweit die Kante lückenlos ist
        int r0 = r, r1 = r;
        while (r0 > 0 && r - r0 < half && Math.abs(kante.nord[r0 - 1] - kante.nord[r0]) < 3 && kante.wand[r0 - 1] >= 4) r0--;
        while (r1 < kante.n - 1 && r1 - r < half && Math.abs(kante.nord[r1 + 1] - kante.nord[r1]) < 3 && kante.wand[r1 + 1] >= 4) r1++;
        if (r1 - r0 < 10) { kehle[r] = Math.min(kehle[r], 0.5 * lc(r)); return; }   // zu kurz: hier nicht rechnen
        Schnitt s = new Schnitt(r, r0, r1);
        vKliff0 += s.volumen0;
        s.kegel0 = vKegel;
        schnitt = s;
        meldung = String.format(Locale.GERMAN, "Kliff bei N %,.0f wird gerechnet: %d Zellen, %.0f m³, Kehle %.1f m (L_c %.1f m)",
                kante.nord[r], s.solid.cells(), s.volumen0, kehle[r], lc(r));
    }

    /**
     * Abbruch an einer gewählten Stelle (F17 A, Knopf): die Kehle der Zeilen ±{@link #LAENGE}/2 wird auf 1,25 · L_c
     * gesetzt, das Stück wird gerechnet (wenn nicht schon eines läuft). Gibt die Zeile zurück (−1: kein Kliff dort).
     */
    public int ausloesen(double x, double z) {
        int r = kante.row(z);
        if (r < 0) {
            double best = Double.MAX_VALUE;
            for (int i = 0; i < kante.n; i++) if (Math.abs(kante.z(i) - z) < best) { best = Math.abs(kante.z(i) - z); r = i; }
        }
        if (r < 0 || kante.wand[r] < 6) return -1;
        int half = (int) Math.round(LAENGE / 2);
        for (int i = Math.max(0, r - half); i <= Math.min(kante.n - 1, r + half); i++) {
            double ziel = 1.25 * lc(i) * Math.max(0, 1 - Math.abs(i - r) / (double) (half + 4));
            if (ziel > kehle[i]) { vKehle += (ziel - kehle[i]) * KEHLE_HOCH * Math.PI / 4; kehle[i] = ziel; }
            if (strata != null) { strata.paintNotch(kante.fussX(i), kante.z(i), 2.2, kehle[i]); gemalt[i] = kehle[i]; }
        }
        if (schnitt == null) beginne(r);
        else schnitt.kehleAuf();
        return r;
    }

    /** Der Schritt der Physik (Faden „Physik“, 1/120 s Echtzeit): das Stück Kliff, das gerechnet wird. */
    @Override public void step(double t, double dt) {
        if (manuell) return;
        for (Runnable r; (r = auftraege.poll()) != null; ) r.run();
        double[] f;
        synchronized (this) { f = flut; flut = null; }
        if (f != null) kehlenSchritt(f[0], f[1], f[2]);
        physik(dt);
    }

    private final java.util.concurrent.ConcurrentLinkedQueue<Runnable> auftraege = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private double[] flut;

    /**
     * Aus dem Bildlauf: dh Stunden Szenenzeit mit Pegel und Auflauf. Die Arbeit macht der Faden der Physik (er allein
     * ändert Körper und Gelände); ohne ihn ({@link #manuell}) sofort.
     */
    public void flut(double dh, double pegel, double r2) {
        pegelJetzt = pegel;
        if (manuell) { kehlenSchritt(dh, pegel, r2); return; }
        synchronized (this) {
            if (flut == null) flut = new double[]{dh, pegel, r2};
            else { flut[0] += dh; flut[1] = pegel; flut[2] = r2; }
        }
    }

    /** Eine Arbeit im Faden der Physik (ohne ihn sofort), etwa der Knopf „Abbruch hier“. */
    public void imFaden(Runnable r) { if (manuell) r.run(); else auftraege.add(r); }

    @Override public String name() { return "Kliffabbruch"; }

    /** Ein Schritt der Physik des aktiven Stücks (auch von Werkzeugen und Prüfungen aufgerufen). */
    public void physik(double dt) {
        Schnitt s = schnitt;
        if (s == null || s.beendet) return;
        s.welt.step(dt);
        s.alter += dt;
        s.gelaende();
        boolean ruhe = s.welt.awakeCount() == 0 && !s.solid.cracking();
        s.ruhig = ruhe ? s.ruhig + dt : 0;
        // fertig: nach einem Bruch 8 s Ruhe, ohne Bruch nach 20 s (die Kehle reicht noch nicht; erst 1 m tiefer wieder)
        if ((s.gebrochen && s.ruhig > 8) || (!s.gebrochen && s.alter > 20)) beende(s);
    }

    /** Ein Stück abschließen: liegende Stücke werden Blöcke im Gelände (Kegel), die Bilanz, die neue Kante. */
    void beende(Schnitt s) {
        s.beendet = true;
        if (s.welt.asyncSolve) s.welt.asyncSolve = false;
        double bloecke = 0;
        for (Piece p : s.welt.pieces()) {
            bloecke += p.volume();
            // liegende Brocken bleiben als Kegel im Gelände (im Bild verschwinden die Stücke, das Volumen bleibt)
            double[] q = p.position();
            vKegel += kegel(q[0], q[2], p.volume() * s.welt.material.bulking);
        }
        double steht = 0;
        for (int i = 0; i < s.solid.cells(); i++) if (s.solid.attached(i)) steht += s.solid.volume(i);
        vSteht += steht;
        vBloecke += bloecke;
        vZerfallen += s.welt.disintegratedVolume();
        vAusgespuelt += s.welt.erodedVolume();
        // neue Kante je Zeile: von landwärts her die erste Stelle, an der das Gelände 1,5 m unter seine Höhe dahinter fällt,
        // vorher (gemessen) und nachher (mit der Änderung); liegt sie nachher weiter landwärts, ist die Kante zurückgegangen
        double kehleMax = 0;
        for (int r = s.r0; r <= s.r1; r++) kehleMax = Math.max(kehleMax, kehle[r]);
        if (s.gebrochen) {
            for (int r = s.r0; r <= s.r1; r++) {
                double z = kante.z(r), xa = s.kant[r - s.r0] + LAND - 2, xb = s.fuss[r - s.r0] - 2;
                double vor = kantenSuche(terrain::roh, xa, xb, z), nach = kantenSuche(terrain::height, xa, xb, z);
                if (!Double.isNaN(vor) && !Double.isNaN(nach) && nach > vor + 0.5) {
                    kanteNeu[r] = kanteAlt[r] + (nach - vor);
                    kante.kanteE[r] = kanteNeu[r];
                }
                for (double x = xa; x > xb; x -= 0.5)
                    if (weg.delta(x, z) < -0.3) { abriss[r] = Math.max(Double.isNaN(abriss[r]) ? -1e9 : abriss[r], x + Site.E0); break; }
                kehle[r] = 0;
                gemalt[r] = 0;
                geprueft[r] = 0;
            }
            if (strata != null) strata.clearNotch(s.x0 - 3, s.z0, s.x1, s.z1);
            // die Spur: alter Fuß, wo die Wand zurückwich; Findlinge vor dem Kegel (aus dem Lehm gefallen) [G]
            for (int r = s.r0; r <= s.r1; r++) if (!Double.isNaN(abriss[r])) spur.add(new double[]{s.fuss[r - s.r0], kante.z(r)});
            double[] fl = {s.fuss[s.mitte - s.r0] - 6, kante.z(s.mitte), Math.max(8, (s.r1 - s.r0) * 0.6)};
            findlingStellen.add(fl);
            spurVersion++;
            bereiche.add(new double[]{s.x0, s.z0, s.x1, s.z1});
            ergebnisse.add(ergebnis(s, s.volumen0 - steht - s.welt.erodedVolume(), bloecke, kehleMax, fl));
        } else for (int r = s.r0; r <= s.r1; r++) geprueft[r] = kehle[r];
        synchronized (fertig) { fertig.add(s); }
        meldung = String.format(Locale.GERMAN, "Abbruch bei N %,.0f fertig: %s · Kante bis %.1f m zurück", kante.nord[s.mitte],
                s.gebrochen ? String.format(Locale.GERMAN, "%.0f m³ gebrochen", s.volumen0 - steht - s.welt.erodedVolume()) : "hält", rueckgang(s.r0, s.r1));
        schnitt = null;
    }

    // ------------------------------------------------------------------ seit Phase 16: Chronik und „Weiter wie zuletzt“

    /** Die Uhr der Szene (ms seit 1970), vom Bildlauf gesetzt; damit trägt ein Ergebnis seine Szenenzeit. */
    public volatile double uhrMs = Double.NaN;

    /**
     * Ein gebrochenes Stück für die Chronik (F37 A): Ort, Bilanz, Kegel, Kehle und L_c, Pegel und Auflauf, Rückgang;
     * die Kante davor und danach und die Abrisskante als Linien in Szenenmetern {x, z}, die Findlinge {x, z, Halbmesser}.
     */
    public record Ergebnis(double ms, double nord, double nordVon, double nordBis, double volumen, double bloecke, double zerfallen, double ausgespuelt,
                           double kegel, double kehle, double lc, double pegel, double auflauf, double rueckgang, double wandMax, double kanteMaxH,
                           double[][] kanteVor, double[][] kanteNach, double[][] abrisskante, double[] findling) {
        public double laenge() { return Math.abs(nordVon - nordBis); }
    }

    /** Die gebrochenen Stücke in der Reihenfolge des Bruchs (die Chronik holt sie ab). */
    public final List<Ergebnis> ergebnisse = new java.util.concurrent.CopyOnWriteArrayList<>();
    /** Die Rechtecke {x0, z0, x1, z1} der gebrochenen Stücke (für Wandkarte und Grasnarbe nach „Weiter wie zuletzt“). */
    public final List<double[]> bereiche = new java.util.concurrent.CopyOnWriteArrayList<>();

    private Ergebnis ergebnis(Schnitt s, double volumen, double bloecke, double kehleMax, double[] findling) {
        int n = s.r1 - s.r0 + 1;
        double[][] vor = new double[n][], nach = new double[n][], fuss = new double[n][];
        double wand = 0, hk = 0;
        for (int r = s.r0; r <= s.r1; r++) {
            double z = kante.z(r);
            vor[r - s.r0] = new double[]{s.kanteVor[r - s.r0] - Site.E0, z};
            nach[r - s.r0] = new double[]{kante.kanteE[r] - Site.E0, z};
            fuss[r - s.r0] = new double[]{(Double.isNaN(abriss[r]) ? kante.fussE[r] : abriss[r]) - Site.E0, z};
            wand = Math.max(wand, kante.wand[r]);
            hk = Math.max(hk, kante.hKante[r]);
        }
        double zerf = s.welt.disintegratedVolume(), aus = s.welt.erodedVolume();
        return new Ergebnis(uhrMs, kante.nord[s.mitte], kante.nord[s.r0], kante.nord[s.r1], volumen, bloecke, zerf, aus, vKegel - s.kegel0, kehleMax, lc(s.mitte),
                pegelJetzt, r2Jetzt, rueckgang(s.r0, s.r1), wand, hk, vor, nach, fuss, findling.clone());
    }

    private static final int STAND_MAGIC = 0x53594b31;   // „SYK1“

    /**
     * Der Stand des Kliffs als Text (Base64 über Deflate): Änderung und Abbruch des Geländes, Kehle, Kante, Abrisskante,
     * Bilanz, Spur, Findlinge und Kegel. Ein Stück, das gerade gerechnet wird, kommt nicht mit (es fällt beim
     * Herstellen weg, die Kehle bleibt). Im Faden der Daten gelesen: die Felder sind Zahlen, ein halber Schritt schadet nicht.
     */
    public String stand() {
        try {
            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            try (java.io.DataOutputStream o = new java.io.DataOutputStream(new java.util.zip.DeflaterOutputStream(bo, new java.util.zip.Deflater(6)))) {
                o.writeInt(STAND_MAGIC);
                o.writeInt(kante.n);
                o.writeInt(aenderung.nx);
                o.writeInt(aenderung.nz);
                for (float v : aenderung.d) o.writeFloat(v);
                for (float v : weg.d) o.writeFloat(v);
                for (double[] a : new double[][]{kehle, gemalt, geprueft, kanteNeu, abriss, kante.kanteE}) for (double v : a) o.writeDouble(v);
                o.writeInt(abbrueche);
                for (double v : new double[]{vKehle, vAbtrag, vKegel, vAbgesenkt, vKliff0, vSteht, vBloecke, vZerfallen, vAusgespuelt}) o.writeDouble(v);
                for (List<double[]> l : List.of(spur, findlingStellen, bereiche)) {
                    o.writeInt(l.size());
                    for (double[] p : l) { o.writeInt(p.length); for (double v : p) o.writeDouble(v); }
                }
                List<Integer> kz;
                kz = new ArrayList<>(kegelZellen);
                o.writeInt(kz.size());
                for (int k : kz) o.writeInt(k);
            }
            return java.util.Base64.getEncoder().encodeToString(bo.toByteArray());
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Stand des Kliffs: " + e.getMessage(), e);
        }
    }

    /**
     * Den Stand herstellen (im Faden der Physik, über {@link #imFaden}): ein laufendes Stück fällt weg. Gibt false zurück,
     * wenn der Text nicht zu dieser Kante oder Änderung passt (dann bleibt alles, wie es war).
     */
    public boolean standSetzen(String text) {
        try (java.io.DataInputStream in = new java.io.DataInputStream(new java.util.zip.InflaterInputStream(
                new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode(text.strip()))))) {
            if (in.readInt() != STAND_MAGIC || in.readInt() != kante.n || in.readInt() != aenderung.nx || in.readInt() != aenderung.nz) return false;
            float[] a = new float[aenderung.d.length], w = new float[weg.d.length];
            for (int i = 0; i < a.length; i++) a[i] = in.readFloat();
            for (int i = 0; i < w.length; i++) w[i] = in.readFloat();
            double[][] arr = new double[6][kante.n];
            for (double[] x : arr) for (int i = 0; i < x.length; i++) x[i] = in.readDouble();
            int ab = in.readInt();
            double[] v = new double[9];
            for (int i = 0; i < v.length; i++) v[i] = in.readDouble();
            List<List<double[]>> listen = new ArrayList<>();
            for (int l = 0; l < 3; l++) {
                List<double[]> li = new ArrayList<>();
                int n = in.readInt();
                for (int k = 0; k < n; k++) { double[] p = new double[in.readInt()]; for (int q = 0; q < p.length; q++) p[q] = in.readDouble(); li.add(p); }
                listen.add(li);
            }
            int nk = in.readInt();
            List<Integer> kz = new ArrayList<>();
            for (int k = 0; k < nk; k++) kz.add(in.readInt());
            // gelesen: jetzt erst setzen
            schnitt = null;
            aenderung.setzen(a);
            weg.setzen(w);
            System.arraycopy(arr[0], 0, kehle, 0, kante.n);
            System.arraycopy(arr[1], 0, gemalt, 0, kante.n);
            System.arraycopy(arr[2], 0, geprueft, 0, kante.n);
            System.arraycopy(arr[3], 0, kanteNeu, 0, kante.n);
            System.arraycopy(arr[4], 0, abriss, 0, kante.n);
            System.arraycopy(arr[5], 0, kante.kanteE, 0, kante.n);
            abbrueche = ab;
            vKehle = v[0]; vAbtrag = v[1]; vKegel = v[2]; vAbgesenkt = v[3]; vKliff0 = v[4]; vSteht = v[5]; vBloecke = v[6]; vZerfallen = v[7]; vAusgespuelt = v[8];
            spur.clear(); spur.addAll(listen.get(0));
            findlingStellen.clear(); findlingStellen.addAll(listen.get(1));
            bereiche.clear(); bereiche.addAll(listen.get(2));
            kegelZellen.clear();
            kegelZellen.addAll(kz);
            if (strata != null) {
                for (int r = 0; r < kante.n; r++) if (kehle[r] > 0.05) strata.paintNotch(kante.fussX(r), kante.z(r), 2.2, kehle[r]);
            }
            spurVersion++;
            meldung = String.format(Locale.GERMAN, "Stand hergestellt: %d Abbrüche, Kehle bis %.1f m", abbrueche, tiefsteKehle());
            return true;
        } catch (java.io.IOException | RuntimeException e) {
            meldung = "Stand des Kliffs nicht lesbar: " + e.getMessage();
            return false;
        }
    }

    /** Kante auf der Zeile z: von xa seewärts bis xb die erste Stelle 1,5 m unter der höchsten Stelle dahinter (NaN: keine). */
    static double kantenSuche(Cliff.Heights h, double xa, double xb, double z) {
        double top = h.height(xa, z);
        for (double x = xa; x > xb; x -= 0.25) {
            double y = h.height(x, z);
            if (y < top - 1.5) return x + 0.25;
            top = Math.max(top, y);
        }
        return Double.NaN;
    }

    /** Wie weit die Abrisskante landwärts des alten Fußes liegt, höchstens (m), in den Zeilen r0..r1. */
    public double abrissTiefe(int r0, int r1) {
        double m = 0;
        for (int r = Math.max(0, r0); r <= Math.min(kante.n - 1, r1); r++) if (!Double.isNaN(abriss[r])) m = Math.max(m, abriss[r] - kante.fussE[r]);
        return m;
    }

    /** Größter Rückgang der Kante (m) in den Zeilen r0..r1. */
    public double rueckgang(int r0, int r1) {
        double m = 0;
        for (int r = Math.max(0, r0); r <= Math.min(kante.n - 1, r1); r++) if (!Double.isNaN(kanteNeu[r])) m = Math.max(m, kanteNeu[r] - kanteAlt[r]);
        return m;
    }

    public Schnitt schnitt() { return schnitt; }
    public List<Schnitt> fertige() { synchronized (fertig) { return new ArrayList<>(fertig); } }
    public double tiefsteKehle() { double m = 0; for (double k : kehle) m = Math.max(m, k); return m; }

    /** Stand der Bilanz des laufenden Stücks einbeziehen: {Kliff vorher, steht, Blöcke, zerfallen, ausgespült}. */
    public double[] bilanz() {
        double k0 = vKliff0, st = vSteht, bl = vBloecke, ze = vZerfallen, au = vAusgespuelt;
        Schnitt s = schnitt;
        if (s != null) {
            for (int i = 0; i < s.solid.cells(); i++) if (s.solid.attached(i)) st += s.solid.volume(i);
            for (Piece p : s.welt.pieces()) bl += p.volume();
            ze += s.welt.disintegratedVolume();
            au += s.welt.erodedVolume();
        }
        return new double[]{k0, st, bl, ze, au};
    }

    /** Die Stücke, die gerade fallen oder liegen, als Netz (Lehm, Kaolinsand, Flugsand nach der Höhe ihrer Zellen). */
    public void stuecke(Netz n) {
        n.leeren();
        Schnitt s = schnitt;
        if (s == null) return;
        for (Fracture.Pose pose : s.welt.poses()) {
            Piece p = pose.piece();
            double[] R = pose.rotation(), t = pose.position();
            for (int i = 0; i < p.cellCount(); i++) {
                int oc = p.originCell(i);
                double y0 = oc >= 0 && oc < s.solid.cells() ? s.solid.center(oc)[1] : 10;
                double zc = oc >= 0 && oc < s.solid.cells() ? s.solid.center(oc)[2] : s.z0;
                int mat = y0 < kaolinOben(zc) ? Netz.KAOLIN : y0 > terrain.roh(s.solid.center(Math.max(0, oc))[0], zc) - flugsand(zc) ? Netz.FLUGSAND : Netz.LEHM;
                int m = Netz.merkmal(mat, false, p.id & 511, y0);
                for (Poly.Face f : p.cell(i).faces) {
                    int k = f.p.length / 3;
                    if (k < 3) continue;
                    double[] nr = {R[0] * f.nx + R[1] * f.ny + R[2] * f.nz, R[3] * f.nx + R[4] * f.ny + R[5] * f.nz, R[6] * f.nx + R[7] * f.ny + R[8] * f.nz};
                    double[] a = welt(R, t, f.p, 0);
                    for (int q = 1; q + 1 < k; q++) n.dreieck(a, welt(R, t, f.p, q), welt(R, t, f.p, q + 1), nr, m);
                }
            }
        }
    }

    private static double[] welt(double[] R, double[] t, double[] p, int q) {
        double x = p[q * 3], y = p[q * 3 + 1], z = p[q * 3 + 2];
        return new double[]{R[0] * x + R[1] * y + R[2] * z + t[0], R[3] * x + R[4] * y + R[5] * z + t[1], R[6] * x + R[7] * y + R[8] * z + t[2]};
    }

    /** {x0, x1, z0, z1} einer Zelle. */
    static double[] bbox(Poly c) {
        double[] v = c.vertices();
        double xa = Double.MAX_VALUE, xb = -Double.MAX_VALUE, za = Double.MAX_VALUE, zb = -Double.MAX_VALUE;
        for (int k = 0; k + 2 < v.length; k += 3) {
            xa = Math.min(xa, v[k]); xb = Math.max(xb, v[k]);
            za = Math.min(za, v[k + 2]); zb = Math.max(zb, v[k + 2]);
        }
        return new double[]{xa, xb, za, zb};
    }

    /** Oberkante der konvexen Zelle über (x, z) (NaN: die Senkrechte trifft sie nicht). */
    static double oberkante(Poly c, double x, double z) {
        double lo = -Double.MAX_VALUE, hi = Double.MAX_VALUE;
        for (Poly.Face f : c.faces) {
            double rest = f.d - f.nx * x - f.nz * z;
            if (Math.abs(f.ny) < 1e-9) { if (rest < 0) return Double.NaN; continue; }
            double y = rest / f.ny;
            if (f.ny > 0) hi = Math.min(hi, y); else lo = Math.max(lo, y);
        }
        return lo <= hi && hi < Double.MAX_VALUE ? hi : Double.NaN;
    }

    /** „Kehle bis 1,8 m · Abbruch läuft bei N 6 089 450: 12 Stücke · Kegel 140 m³“ */
    public String describe() {
        double[] b = bilanz();
        Schnitt s = schnitt;
        return String.format(Locale.GERMAN, "Kliff: Kehle bis %.1f m%s · %d Abbrüche · Kegel %.0f m³, Abtrag %.0f m³ · Bilanz %.0f = %.0f steht + %.0f Blöcke + %.0f zerfallen + %.0f ausgespült%s",
                tiefsteKehle(), s == null ? "" : String.format(Locale.GERMAN, " · rechnet bei N %,.0f (%d Stücke)", kante.nord[s.mitte], s.welt.pieceCount()),
                abbrueche, vKegel, vAbtrag, b[0], b[1], b[2], b[3], b[4], meldung.isEmpty() ? "" : " · " + meldung);
    }
}
