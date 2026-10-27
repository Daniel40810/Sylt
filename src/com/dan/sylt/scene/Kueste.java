package com.dan.sylt.scene;

/**
 * Die Küste am Roten Kliff als Achsen (seit Phase 7a): die Wasserlinie aus {@link Site#WASSERLINIE} läuft im Kern unter
 * rund 23° (von 455 972 / 6 088 000 nach 457 463 / 6 091 500), zur Küste blickt man darum unter 113°. Für die Brandung
 * gibt es zwei Maße: wie weit ein Ort seewärts der Wasserlinie liegt ({@link #offshore}, senkrecht zur Küste, die
 * wirkliche Wasserlinie mit ihren Bögen) und wo er längs der Küste liegt ({@link #along}, Projektion auf die Achse,
 * nach Süden wachsend).
 */
public final class Kueste {
    /** Blick vom Meer zur Küste (Grad von Nord) [gemessen an der Wasserlinie im Kern: 113,1°]. */
    public static final double SHOREWARD_DEG = 113;
    /** Einheitsvektoren in der Szene (x Ost, z Süd): zur Küste (SX, SZ) und längs nach Süden (AX, AZ). */
    public static final double SX, SZ, AX, AZ;
    static {
        double a = Math.toRadians(SHOREWARD_DEG);
        SX = Math.sin(a);
        SZ = -Math.cos(a);
        AX = -SZ;
        AZ = SX;
    }

    private Kueste() { }

    /** Abstand seewärts der Wasserlinie (NHN 0) senkrecht zur Küste (m; landwärts negativ). */
    public static double offshore(double x, double z) {
        double[] en = Site.toUtm(x, z);
        return (Site.wasserlinieE(en[1]) - en[0]) * SX;
    }

    /** Lage längs der Küste (m, nach Süden wachsend, 0 durch den Ursprung). */
    public static double along(double x, double z) { return x * AX + z * AZ; }

    /** Der Punkt der Wasserlinie (NHN 0) senkrecht zur Küste vom Ort aus (x, z). */
    public static double[] waterline(double x, double z) {
        double[] p = {x, z};
        for (int k = 0; k < 3; k++) {
            double o = offshore(p[0], p[1]);
            p[0] += o * SX;
            p[1] += o * SZ;
        }
        return p;
    }
}
