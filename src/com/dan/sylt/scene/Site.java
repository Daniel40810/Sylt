package com.dan.sylt.scene;

/**
 * Der Ort der Szene: das Rote Kliff auf Sylt zwischen Wenningstedt und Kampen (Werkbuch, Abschnitte 2, 4 und 7).
 * <p>
 * Raum (F2 A): UTM Zone 32 Nord (ETRS89, EPSG:25832), echter Maßstab, Höhen über NHN, Ursprung am Quermarkenfeuer
 * Rotes Kliff [G, E 457 600, N 6 091 170]. 1 Einheit = 1 m, x nach Osten, y nach oben, z nach Süden, Nord = −z, y = 0
 * ist Normalhöhennull. Für die Datenbank und QGIS liegt die Lage zusätzlich in SRID 8307 (WGS84) vor.
 * <p>
 * Ebenen (F1 A, Werkbuch Abschnitt 4): der <b>Kern</b> mit 2,3 × 3,6 km (DGM1 1 m, Kante aus dem bDOM), die
 * <b>Brandungszone</b> seewärts bis rund −8 m NHN (Grund aus der BAW, Riffe und Rinnen auf 2 m gestaltet), das
 * <b>Fernfeld</b> mit rund 16 × 16 km aus BAW und DGM1 (10 m) und dahinter der <b>Fernring</b> bis zur Kimm. Die Lage der
 * Orte ist seit Phase 2 am Gelände gemessen (DGM1, bDOM).
 */
public final class Site {
    private Site() { }

    /** Ursprung der Szene: das Quermarkenfeuer Rotes Kliff [G], UTM 32 N (F2 A). */
    public static final double E0 = 457_600, N0 = 6_091_170;
    public static final int SRID = 8307, EPSG_UTM = 25832;
    /** Breite und Länge des Ursprungs (aus E0/N0 gerechnet). */
    public static final double LAT, LON;
    static {
        double[] ll = Geo.UTM32N.inverse(E0, N0);
        LAT = ll[0];
        LON = ll[1];
    }

    /** Mittlerer Erdradius in m und der wirksame Radius für die Sicht mit normaler Lichtbrechung (k = 0,13). */
    public static final double EARTH_R = 6_371_000, EARTH_R_OPTICAL = EARTH_R / (1 - 0.13);

    /** Ein rechteckiges Fenster in UTM 32 N (Ost- und Nordwerte in m). */
    public record Fenster(String name, double eMin, double nMin, double eMax, double nMax) {
        /** In der Szene: {xMin, zMin, xMax, zMax}. */
        public double[] scene() {
            double[] a = fromUtm(eMin, nMax), b = fromUtm(eMax, nMin);
            return new double[]{a[0], a[1], b[0], b[1]};
        }
        public double breite() { return eMax - eMin; }
        public double hoehe() { return nMax - nMin; }
        public boolean contains(double x, double z) {
            double[] u = toUtm(x, z);
            return u[0] >= eMin && u[0] <= eMax && u[1] >= nMin && u[1] <= nMax;
        }
    }

    /** F1 A: der Kern, 2,3 × 3,6 km: Brandungszone, Strand, Kliff, Heide, Dünen, beide Türme, Uwe-Düne. */
    public static final Fenster KERN = new Fenster("Kern", 455_700, 6_088_000, 458_000, 6_091_600);
    /** F1 A: das Fernfeld, rund 16 × 16 km um den Kern (BAW und DGM1, 10 m) [G]. */
    public static final Fenster FERNFELD = new Fenster("Fernfeld", 448_850, 6_081_800, 464_850, 6_097_800);
    /** F1 A: Halbmesser des Fernrings um die Mitte des Kerns bis über die Kimm (m) [G]. */
    public static final double FERNRING = 50_000;
    /** Seewärtige Grenze der Brandungszone (m NHN) [G]. */
    public static final double BRANDUNG_TIEFE = -8;

    /** Was ein Ort auf der Bühne ist (Farbe der Marke). */
    public enum Art { TURM, DUENE, ORT, STRAND, KLIFF }

    /** Ein benannter Ort [G] mit Lage in UTM 32 N und Höhe des Grunds über NHN (für die Bühne von Phase 1). */
    public record Ort(String name, double e, double n, double grund, Art art) {
        public double x() { return e - E0; }
        public double z() { return -(n - N0); }
    }

    /**
     * Die Orte aus dem Werkbuch (Abschnitt 2 und 7), seit Phase 2 am Gelände gemessen: die Türme als höchster Bewuchs im
     * bDOM nahe der Lage aus Wikipedia (Quermarkenfeuer 10 m, Kampener Leuchtturm 38 m über Grund), die Uwe-Düne als
     * höchster Punkt im DGM1 (50,8 m; die 52,5 m der Karten gelten mit Aussichtsplattform), Kliffkante und Strand aus
     * {@code daten/kliff/kante.csv} bei N 6 089 500, Haus Kliffende als größtes Dach auf der Wand an ihrem Nordende [aus DOP20
     * und bDOM; ob es das Haus ist, prüfen LOD2 und ein Foto]. Grund: Höhe im DGM1 (m NHN). Risgap und Kampen bleiben geschätzt [G].
     */
    public static final Ort QUERMARKE = new Ort("Quermarkenfeuer", 457_603, 6_091_174, 15.1, Art.TURM),
            LEUCHTTURM = new Ort("Kampener Leuchtturm", 457_770, 6_089_007, 27.4, Art.TURM),
            UWE_DUENE = new Ort("Uwe-Düne", 457_133, 6_089_904, 50.8, Art.DUENE),
            KLIFFKANTE = new Ort("Kliffkante", 456_751, 6_089_500, 27.0, Art.KLIFF),
            RISGAP = new Ort("Risgap (Wenningstedt)", 456_150, 6_088_150, 4.9, Art.ORT),
            KLIFFENDE = new Ort("Haus Kliffende", 457_290, 6_090_670, 16.5, Art.ORT),
            STRAND = new Ort("Strand am Kliff", 456_645, 6_089_500, 2.5, Art.STRAND),
            KAMPEN = new Ort("Kampen", 458_300, 6_090_400, 15.7, Art.ORT);
    public static final Ort[] ORTE = {QUERMARKE, LEUCHTTURM, UWE_DUENE, KLIFFKANTE, RISGAP, KLIFFENDE, STRAND, KAMPEN};

    /**
     * Die Wasserlinie (NHN 0) an der Westküste: je Nordwert der Ostwert, seit Phase 2 gemessen am Gelände aus DGM1 und BAW
     * ({@code daten/gelaende/wasserlinie.csv}, hier ausgedünnt). Dazwischen linear, außerhalb fortgesetzt. Die Bühne von
     * Phase 1 und die Ansichten richten sich danach.
     */
    public static final double[][] WASSERLINIE = {
            {6_080_000, 454_272}, {6_084_000, 454_774}, {6_086_000, 455_295}, {6_088_000, 455_972}, {6_089_000, 456_389}, {6_090_000, 456_793},
            {6_091_000, 457_231}, {6_092_500, 457_875}, {6_094_000, 458_511}, {6_097_000, 459_727}};

    /** Ostwert der Wasserlinie am Nordwert n (UTM). */
    public static double wasserlinieE(double n) {
        double[][] w = WASSERLINIE;
        if (n <= w[0][0]) return w[0][1] + (n - w[0][0]) * (w[1][1] - w[0][1]) / (w[1][0] - w[0][0]);
        for (int k = 1; k < w.length; k++)
            if (n <= w[k][0]) return w[k - 1][1] + (n - w[k - 1][0]) / (w[k][0] - w[k - 1][0]) * (w[k][1] - w[k - 1][1]);
        int m = w.length - 1;
        return w[m][1] + (n - w[m][0]) * (w[m][1] - w[m - 1][1]) / (w[m][0] - w[m - 1][0]);
    }

    /** Szenenpunkt (x, z) aus UTM 32 Nord (Ost- und Nordwert in m). */
    public static double[] fromUtm(double east, double north) { return new double[]{east - E0, -(north - N0)}; }

    /** UTM 32 Nord (Ost, Nord) aus einem Szenenpunkt (x, z). */
    public static double[] toUtm(double x, double z) { return new double[]{x + E0, N0 - z}; }

    /** Breite und Länge (Grad) eines Szenenpunkts. */
    public static double[] latLon(double x, double z) { return Geo.UTM32N.inverse(x + E0, N0 - z); }

    /** Szenenpunkt (x, z) aus Breite und Länge (Grad, als {lat, lon}). */
    public static double[] xz(double[] latLon) {
        double[] en = Geo.UTM32N.forward(latLon[0], latLon[1]);
        return fromUtm(en[0], en[1]);
    }

    static Ort ort(String name, double lat, double lon, double grund, Art art) {
        double[] en = Geo.UTM32N.forward(lat, lon);
        return new Ort(name, en[0], en[1], grund, art);
    }

    /** Absenkung der Oberfläche in d m Entfernung durch die Erdkrümmung: d²/2R. */
    public static double curvatureDrop(double d) { return d * d / (2 * EARTH_R); }

    /** Entfernung der Kimm für ein Auge h m über der Ebene, mit normaler Lichtbrechung (m). */
    public static double horizon(double h) { return Math.sqrt(2 * EARTH_R_OPTICAL * Math.max(0, h)); }

    /** „54°57′56″ N · 8°20′16″ E“ */
    public static String dms(double lat, double lon) {
        return dms(lat, lat >= 0 ? "N" : "S") + " · " + dms(lon, lon >= 0 ? "E" : "W");
    }

    private static String dms(double v, String h) {
        v = Math.abs(v);
        int d = (int) v;
        double mm = (v - d) * 60;
        int m = (int) mm;
        int s = (int) Math.round((mm - m) * 60);
        if (s == 60) { s = 0; m++; }
        if (m == 60) { m = 0; d++; }
        return String.format("%d°%02d′%02d″ %s", d, m, s, h);
    }
}
