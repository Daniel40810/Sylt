package com.dan.sylt.wiring;

import com.dan.cloud.CloudType;
import com.dan.weather.Scenario;
import com.dan.weather.Weather;

import java.util.Locale;

/**
 * Seit Phase 13: die Wetterregie für den Weststrand ({@code com.dan.weather}, Klima der Deutschen Bucht, die Monatsmittel
 * von List auf Sylt [G]). Drei Arten zu führen:
 * <ul>
 * <li><b>Lage</b> (Vorgabe „Sommerabend“, wie bis Phase 12): eine benannte Lage nach dem Werkbuch (Abschnitt 8) steht
 * fest, die Zeitleiste ruht. Wind, Wolken, Regen, Nebel, Gewitter und See kommen aus der Lage.</li>
 * <li><b>Freie Regie</b>: die Markow-Kette der Deutschen Bucht wählt die Lagen, mit Vorlauf (Barometer, Dünung, Cirren).</li>
 * <li><b>„Ein Sturmtag“</b>: das Regiebuch des Packages (Front, Sturm, Gewitter in der Dämmerung, Sturmnacht mit
 * Leuchtfeuer, Aufklaren mit Regenbogen).</li>
 * </ul>
 * „Von Hand“ schaltet die Regie ab: die Regler im Bedienfeld schreiben dann direkt in die Verdrahtung (wie bis Phase 12).
 * Während der Sturmflut nach Xaver (Phase 12) führt ihr Fahrplan, die Regie ruht.
 */
public final class Wetterregie {
    public enum Modus { HAND("von Hand"), LAGE("Lage"), FREI("freie Regie"), STURMTAG("Ein Sturmtag");
        public final String label;
        Modus(String l) { label = l; }
    }

    /**
     * Die Lagen nach dem Werkbuch (Abschnitt 8, Regie) [G]: Wind 10 m über Grund (m/s, aus Grad), Böigkeit, Bedeckung
     * (Achtel), hohe Schicht, Wolkenart, Regen (mm/h unter einem Vorhang), Sicht (km), Nebel 0..1, Obergrenze des
     * Nebels (m über dem Pegel), Dünung H_s (m), T_p (s), aus Grad, Gewitter 0..4, Temperatur (°C).
     */
    public enum Lage {
        SOMMERABEND("Sommerabend", Weather.Phase.KLAR, 6, 270, 0.14, 3, 0.35, CloudType.CUMULUS, 0, 40, 0, 50, 1.0, 8, 285, 0, 18),
        WESTWIND("Westwind", Weather.Phase.AUFZIEHEND, 11, 260, 0.20, 5, 0.2, CloudType.CUMULUS, 0, 30, 0, 50, 2.0, 7, 270, 0, 15),
        ABLANDIG("Ablandig", Weather.Phase.KLAR, 7, 95, 0.15, 1, 0.1, CloudType.CUMULUS, 0, 50, 0, 50, 1.2, 10, 290, 0, 17),
        SCHAUER("Schauer über See", Weather.Phase.ABKLINGEND, 12, 275, 0.30, 5, 0.1, CloudType.CUMULONIMBUS, 7, 28, 0, 50, 2.2, 8, 285, 0, 11),
        LANDREGEN("Landregen", Weather.Phase.FRONT, 10, 225, 0.20, 8, 0.2, CloudType.NIMBOSTRATUS, 2.5, 10, 0, 50, 1.6, 7, 250, 0, 12),
        STURM("Sturm", Weather.Phase.STURM, 22, 255, 0.22, 7.5, 0, CloudType.STRATOCUMULUS, 5, 8, 0, 50, 3.4, 10, 265, 0, 10),
        SEENEBEL("Seenebel", Weather.Phase.NEBEL, 2.5, 220, 0.08, 1, 0.25, CloudType.CUMULUS, 0, 14, 0.9, 55, 0.5, 9, 280, 0, 13),
        SEERAUCH("Seerauch", Weather.Phase.KLAR, 4, 110, 0.10, 0, 0.05, CloudType.CUMULUS, 0, 30, 0.55, 9, 0.6, 9, 290, 0, -4),
        GEWITTER("Gewitter über der See", Weather.Phase.GEWITTER, 9, 250, 0.30, 6, 0.1, CloudType.CUMULONIMBUS, 9, 18, 0, 50, 1.6, 8, 270, 2, 19);

        public final String label;
        public final Weather.Phase phase;
        public final double wind, dir, gust, oktas, high, rain, vis, fog, fogTop, hs, tp, seaDir, temp;
        public final CloudType cloud;
        public final int thunder;

        Lage(String label, Weather.Phase phase, double wind, double dir, double gust, double oktas, double high, CloudType cloud, double rain, double vis,
             double fog, double fogTop, double hs, double tp, double seaDir, int thunder, double temp) {
            this.label = label; this.phase = phase; this.wind = wind; this.dir = dir; this.gust = gust; this.oktas = oktas; this.high = high;
            this.cloud = cloud; this.rain = rain; this.vis = vis; this.fog = fog; this.fogTop = fogTop; this.hs = hs; this.tp = tp; this.seaDir = seaDir;
            this.thunder = thunder; this.temp = temp;
        }

        @Override public String toString() { return label; }
    }

    /** Monatsmittel der Lufttemperatur auf Sylt (List, 1991–2020, gerundet) [G]. */
    public static final double[] KLIMA = {2.0, 1.8, 3.8, 7.3, 11.5, 14.8, 17.1, 17.3, 14.6, 10.8, 6.6, 3.6};

    public final Weather weather = new Weather();
    private final Wiring w;
    private volatile Modus modus = Modus.LAGE;
    private volatile Lage lage = Lage.SOMMERABEND;
    private Weather.Snapshot snap;

    Wetterregie(Wiring w) {
        this.w = w;
        weather.seed = 2026;
        weather.zone = java.time.ZoneId.of("Europe/Berlin");
        weather.climate = KLIMA.clone();
    }

    public Modus modus() { return modus; }
    public Lage lage() { return lage; }
    /** Der letzte Stand der Regie (null, solange sie von Hand steht und noch nie lief). */
    public Weather.Snapshot snapshot() { return snap; }

    private double t() { return w.clock.epochMillis() / 1000.0; }

    /** Eine Lage halten (die Zeitleiste ruht, alle Größen stehen sofort). */
    public synchronized void lage(Lage l) {
        lage = l;
        modus = Modus.LAGE;
        weather.running = true;
        weather.release();
        weather.startFree(t(), l.phase);
        weather.set(Weather.Var.WIND, l.wind);
        weather.set(Weather.Var.DIRECTION, l.dir);
        weather.set(Weather.Var.GUST, l.gust);
        weather.set(Weather.Var.COVER, l.oktas);
        weather.set(Weather.Var.HIGH, l.high);
        weather.set(Weather.Var.CLOUD, l.cloud.ordinal());
        weather.set(Weather.Var.RAIN, l.rain);
        weather.set(Weather.Var.VISIBILITY, l.vis);
        weather.set(Weather.Var.FOG, l.fog);
        weather.set(Weather.Var.SWELL, l.hs);
        weather.set(Weather.Var.THUNDER, l.thunder);
        weather.set(Weather.Var.TEMPERATURE, l.temp);
        weather.running = false;
        anwenden(weather.update(t(), 0));
    }

    /** Eine Lage mit eigener Bedeckung und Wolkenart (für die Vorlagen der Türme). */
    public synchronized void lage(Lage l, double oktas, CloudType art) {
        lage(l);
        weather.set(Weather.Var.COVER, oktas);
        weather.set(Weather.Var.CLOUD, art.ordinal());
        anwenden(weather.update(t(), 0));
    }

    /** Freie Regie ab jetzt (in der Lage, die gerade gilt). */
    public synchronized void frei() {
        Weather.Phase p = snap != null ? snap.phase() : lage.phase;
        modus = Modus.FREI;
        weather.release();
        weather.running = true;
        weather.startFree(t(), p);
        anwenden(weather.update(t(), 0));
    }

    /** Das Regiebuch „Ein Sturmtag“ ab jetzt (die Uhr bleibt, wo sie steht). */
    public synchronized void sturmtag() {
        modus = Modus.STURMTAG;
        weather.release();
        weather.running = true;
        weather.start(Scenario.stormDay(), t());
        anwenden(weather.update(t(), 0));
    }

    /** Von Hand: die Regie ruht, die Regler schreiben direkt. */
    public synchronized void hand() { modus = Modus.HAND; }

    /** Je Bild (wenn keine Sturmflut läuft): Regie weiter nach der Uhr der Szene, die Werte in die Verdrahtung. */
    public synchronized void schritt(double realDt) {
        if (modus == Modus.HAND) return;
        anwenden(weather.update(t(), realDt));
    }

    /** Die Werte der Regie an die Verdrahtung (in einer Lage mit deren Periode und Richtung der See). */
    private void anwenden(Weather.Snapshot s) {
        if (s == null) return;
        snap = s;
        w.windSpeed = s.wind();
        w.windFromDeg = s.fromDeg();
        w.gustiness = s.gustiness();
        w.gustNow = s.gustNow();
        w.oktas = s.oktas();
        w.highCover = s.high();
        w.cloudType = CloudType.values()[Math.max(0, Math.min(CloudType.values().length - 1, s.cloud().ordinal()))];
        w.regen = s.rain();
        w.visibilityKm = s.visibilityKm();
        w.nebel = s.fog();
        w.gewitter = s.thunder();
        w.temperatur = s.temperature();
        boolean l = modus == Modus.LAGE;
        w.nebelHoch = l ? lage.fogTop : 18 + 60 * s.fog() * s.fog();
        w.surf.hs = Math.max(0.2, s.swellHeight());
        w.surf.tp = l ? lage.tp : s.swellPeriod();
        w.surf.seaFromDeg = l ? lage.seaDir : clampSea(s.swellFromDeg());
    }

    /** Die See kommt am Weststrand aus Südwest bis Nord (180° bis 360°); ablandiger Wind lässt die alte Dünung aus West. */
    static double clampSea(double deg) {
        double d = ((deg % 360) + 360) % 360;
        return d < 180 ? (d < 90 ? 330 : 240) : d;
    }

    /** „Lage Sommerabend“ / „freie Regie: Sturm (Bft 9), 987 hPa fallend“ / „Ein Sturmtag: Front mit Regenvorhang“ */
    public String zeile() {
        Weather.Snapshot s = snap;
        return switch (modus) {
            case HAND -> "Regie: von Hand";
            case LAGE -> "Lage " + lage.label;
            case FREI -> s == null ? "freie Regie" : String.format(Locale.GERMAN, "freie Regie: %s (Bft %d), %.0f hPa %s, dann %s in %.1f h",
                    s.phase().label, s.beaufort(), s.pressure(), s.tendencyText(), s.next().label, s.nextInH());
            case STURMTAG -> s == null ? "Ein Sturmtag" : String.format(Locale.GERMAN, "Ein Sturmtag, Schritt %d von %d: %s (Bft %d, %.0f hPa)",
                    s.step() + 1, s.steps(), s.stepText(), s.beaufort(), s.pressure());
        };
    }
}
