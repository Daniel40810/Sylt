package com.dan.sylt.scene;

import com.dan.sylt.wiring.Wiring;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Locale;

/**
 * Die Sturmflut (seit Phase 12, F16 A): ein Szenario nach Orkan Xaver, 5. bis 7. Dezember 2013. Es führt Wind,
 * Richtung, Seegang und Bedeckung nach einem Fahrplan; den Pegel macht die Tide aus {@code com.dan.tide} mit dem
 * Windstau ({@code Tide.surgeTarget}, Zeitkonstante 3 h) aus diesem Wind.
 * <p>
 * Belegt: Orkan aus Nordwest über der Deutschen Bucht mit Windstärke 9 bis 10, Böen um 140 km/h, auf Sylt nachts
 * über 3 m über NN, Wellen in der Deutschen Bucht bis 6 m (BSH, Wikipedia „Cyclone Xaver“, Berichte von der Insel). Der
 * Verlauf von Stunde zu Stunde ist geschätzt [G]: Auffrischen aus West am Mittag des 5., Höhepunkt aus Nordwest in der
 * Nacht zum 6., Abflauen über Nordnordwest bis zum 7. morgens.
 */
public final class Sturmflut {
    /** Ein Punkt des Fahrplans: Stunden nach dem Start, Wind (m/s in 10 m), Richtung (woher, Grad), H_s, T_p (s). */
    public record Punkt(double stunde, double wind, double richtung, double hs, double tp) { }

    /** Beginn: 5. Dezember 2013, 12 Uhr (MEZ). */
    public static final LocalDate DATUM = LocalDate.of(2013, 12, 5);
    public static final double STUNDE = 12;
    /** Fahrplan nach Xaver [G, siehe oben]. */
    public static final Punkt[] XAVER = {
            new Punkt(0, 14, 255, 3.0, 9),
            new Punkt(6, 22, 285, 5.0, 11),
            new Punkt(12, 27, 300, 6.0, 12),
            new Punkt(18, 26, 305, 6.0, 12.5),
            new Punkt(24, 20, 310, 4.5, 11),
            new Punkt(30, 15, 320, 3.0, 10),
            new Punkt(42, 9, 340, 1.8, 8)};
    /** Böen um 140 km/h (m/s). */
    public static final double BOEEN = 39;

    public volatile boolean aktiv;
    /** Seit Phase 15: der Tag, an dem der Fahrplan beginnt (sonst Xaver, 5.12.2013). */
    public volatile LocalDate datum = DATUM;
    private double startMs = Double.NaN;
    private Punkt jetzt = XAVER[0];

    public static double startMillis() { return startMillis(DATUM); }

    /** Seit Phase 15: derselbe Fahrplan an einem anderen Tag (Regiebuch „Ein Jahr am Kliff“), Beginn um 12 Uhr. */
    public static double startMillis(LocalDate tag) {
        return ZonedDateTime.of(tag.atStartOfDay(), SceneClock.ZONE).plusMinutes((long) (STUNDE * 60)).toInstant().toEpochMilli();
    }

    /** Die Sturmflut beginnt: Uhr auf den 5. Dezember 2013 um {@code stunde}, Tide an, Windstau vom Wind des Fahrplans. */
    public void starten(SceneClock clock, Wiring w, double stunde) {
        clock.set(datum, stunde);
        startMs = startMillis(datum);
        aktiv = true;
        w.tideAn = true;
        schritt(clock, w);
        // der Stau hat beim Start schon den Wind der letzten Stunden: einmal lange nachführen
        w.tide.surge = com.dan.tide.Tide.surgeTarget(w.windSpeed, w.windFromDeg) * 0.8;
    }

    /** Stunden seit dem Beginn nach der Uhr der Szene. */
    public double stunden(SceneClock clock) { return (clock.epochMillis() - startMillis(datum)) / 3_600_000.0; }

    /** Den Fahrplan zur Uhr der Szene anwenden (jedes Bild); nach dem letzten Punkt endet das Szenario. */
    public void schritt(SceneClock clock, Wiring w) {
        if (!aktiv) return;
        double h = stunden(clock);
        jetzt = an(h);
        w.windSpeed = jetzt.wind();
        w.windFromDeg = jetzt.richtung();
        w.surf.hs = jetzt.hs();
        w.surf.tp = jetzt.tp();
        w.surf.seaFromDeg = jetzt.richtung();
        w.oktas = 8;
        w.visibilityKm = Math.max(4, 30 - jetzt.wind());
        // seit Phase 13: Schauer und Graupel aus tiefer Decke (Xaver: Regen- und Graupelschauer [G]), Böen nach dem Fahrplan
        w.cloudType = com.dan.cloud.CloudType.STRATOCUMULUS;
        w.regen = Math.max(0.5, Math.min(6, 1 + 4 * (jetzt.wind() - 14) / 13));
        w.nebel = 0;
        w.gewitter = 0;
        w.gustiness = 0.3;
        w.gustNow = jetzt.wind() * BOEEN / 27;
        if (h > XAVER[XAVER.length - 1].stunde()) aktiv = false;
    }

    /** Der Fahrplan nach h Stunden (linear zwischen den Punkten, vorher und nachher fest). */
    public static Punkt an(double h) {
        if (h <= XAVER[0].stunde()) return XAVER[0];
        for (int i = 0; i + 1 < XAVER.length; i++) {
            Punkt a = XAVER[i], b = XAVER[i + 1];
            if (h <= b.stunde()) {
                double t = (h - a.stunde()) / (b.stunde() - a.stunde());
                return new Punkt(h, a.wind() + (b.wind() - a.wind()) * t, a.richtung() + (b.richtung() - a.richtung()) * t,
                        a.hs() + (b.hs() - a.hs()) * t, a.tp() + (b.tp() - a.tp()) * t);
            }
        }
        return XAVER[XAVER.length - 1];
    }

    /** „Sturmflut (Xaver 2013), Stunde 14: Wind 27 m/s aus 301°, Böen 39 m/s, See H_s 6,0 m“ */
    public String describe(SceneClock clock) {
        if (!aktiv) return "Sturmflut: aus";
        return String.format(Locale.GERMAN, "Sturmflut (nach Xaver 2013), Stunde %.1f: Wind %.0f m/s aus %03.0f°, Böen bis %.0f m/s, See H_s %.1f m, T_p %.0f s",
                stunden(clock), jetzt.wind(), jetzt.richtung(), BOEEN, jetzt.hs(), jetzt.tp());
    }
}
