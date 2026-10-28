package com.dan.sylt.scene;

import com.dan.beacon.Beacon;
import com.dan.beacon.Scatter;
import com.dan.beacon.ScatterGrid;

import java.util.Locale;

/**
 * Die Leuchtfeuer der Szene (seit Phase 10, F18 und F19 A) aus {@code com.dan.beacon}:
 * <ul>
 * <li><b>Kampen</b> ({@link Beacon#kampen()}): Iso WR 7s, Feuerhöhe 62 m, Gürtellinse; der Dämmerungsschalter folgt dem
 * Licht am Turm (an unter 150 lx, aus über 300 lx).</li>
 * <li><b>Quermarkenfeuer Rotes Kliff</b> ({@link Beacon#rotesKliff()}): heute dunkel, eine Tagesmarke; „wie früher“
 * (bis 1975) F WRG mit seinen Sektoren, mit dem gleichen Dämmerungsschalter.</li>
 * </ul>
 * Brennpunkte aus {@link Bauwerke} (Mitte der Laterne). Das Streugitter des Strahls bekommt den Regen (Phase 13; bis
 * dahin trocken), der Dunst kommt aus der Sichtweite.
 */
public final class Leuchtfeuer {
    public final Beacon kampen = Beacon.kampen(), quermarke = Beacon.rotesKliff();
    public final ScatterGrid gridKampen, gridQuermarke;
    /** Feuer zeigen (Strahl und Lichtpunkt), Strahl, Quermarkenfeuer wie früher (F19). */
    public volatile boolean an = true, strahl = true, quermarkeFrueher;
    /** Seit Phase 13: das Quermarkenfeuer nachts angestrahlt (warm, vom Kiesweg; wie auf Leuchturm2) [G]. */
    public volatile boolean anstrahlung = true;
    /** Stärke des Strahls (1 = physikalisch). */
    public volatile double staerke = 1;
    private double regenGefuellt = -1;
    private long nebelGefuellt;
    private double lux;

    public Leuchtfeuer(double[] fokusKampen, double[] fokusQuermarke, Beacon.Quality q) {
        for (Beacon b : new Beacon[]{kampen, quermarke}) {
            b.quality = q;
            b.automatic = true;
        }
        kampen.x = fokusKampen[0];
        kampen.y = fokusKampen[1];
        kampen.z = fokusKampen[2];
        quermarke.x = fokusQuermarke[0];
        quermarke.y = fokusQuermarke[1];
        quermarke.z = fokusQuermarke[2];
        gridKampen = new ScatterGrid(q.gridAz, q.gridEl, q.gridR);
        gridQuermarke = new ScatterGrid(q.gridAz, q.gridEl, q.gridR);
        // Lage und Kennung gelten erst mit dem nächsten Zustand
        kampen.setSectors(kampen.sectors());
        quermarke.setSectors(quermarke.sectors());
    }

    /** Streukoeffizient des Regens (1/m) nach der Regenmenge in mm/h [wie BeaconScene]. */
    public static double regenSigma(double mmh) { return mmh <= 0 ? 0 : 0.25 * Math.pow(mmh, 0.63) / 1000; }

    /**
     * Weiter um dt: Umgebungslicht (lx) für den Dämmerungsschalter, Sichtweite (km), Regen (mm/h).
     */
    public void schritt(double dt, double umgebungLux, double sichtKm, double regen) { schritt(dt, umgebungLux, sichtKm, regen, null, 0); }

    /**
     * Seit Phase 13: dazu der Seenebel als Streufeld (1/m, etwa {@code Fog::scatter}) und ein Stand, der sich ändert, wenn
     * der Nebel gezogen ist (dann werden die Gitter der Strahlen neu gefüllt).
     */
    public void schritt(double dt, double umgebungLux, double sichtKm, double regen, com.dan.beacon.Scatter nebel, long nebelStand) {
        lux = umgebungLux;
        double haze = 2.996 / (Math.max(0.5, sichtKm) * 1000);
        kampen.ambientLux = umgebungLux;
        kampen.hazePerM = haze;
        quermarke.ambientLux = umgebungLux;
        quermarke.hazePerM = haze;
        quermarke.automatic = quermarkeFrueher;
        quermarke.manualOn = false;
        if (Math.abs(regen - regenGefuellt) > 0.05 || nebelStand != nebelGefuellt) {
            double s = regenSigma(regen);
            Scatter sc = nebel == null ? Scatter.uniform(s) : (x, y, z) -> s + nebel.sigma(x, y, z);
            gridKampen.fill(sc, kampen.x, kampen.y, kampen.z);
            gridQuermarke.fill(sc, quermarke.x, quermarke.y, quermarke.z);
            regenGefuellt = regen;
            nebelGefuellt = nebelStand;
        }
        kampen.step(dt);
        quermarke.step(dt);
    }

    /** Für Standbilder: Schalter sofort nach dem Licht stellen. */
    public void sofort(double umgebungLux) {
        kampen.snapOn(umgebungLux < kampen.onLux);
        quermarke.snapOn(quermarkeFrueher && umgebungLux < quermarke.onLux);
    }

    /** Umgebungslicht in lx aus Sonne und Himmel (Szeneneinheiten von com.dan.sky). */
    public static double lux(double[] sonne, double sonneHoch, double[] himmel) {
        double l = 0.2126 * himmel[0] + 0.7152 * himmel[1] + 0.0722 * himmel[2];
        double s = 0.2126 * sonne[0] + 0.7152 * sonne[1] + 0.0722 * sonne[2];
        return (l + s * Math.max(0, sonneHoch)) * com.dan.sky.Sky.CD_PER_UNIT;
    }

    /** „Kampen Iso WR 7s an (12 lx) · Quermarke dunkel“ */
    public String describe() {
        return String.format(Locale.GERMAN, "Kampen %s %s · Quermarkenfeuer %s · Licht am Turm %s lx",
                kampen.characteristic().text, kampen.isOn() ? "an" : "aus",
                quermarkeFrueher ? quermarke.characteristic().text + (quermarke.isOn() ? " an (wie früher)" : " aus (wie früher)") : "dunkel (seit 1975)",
                lux < 10 ? String.format(Locale.GERMAN, "%.2g", lux) : String.format(Locale.GERMAN, "%,.0f", lux));
    }
}
