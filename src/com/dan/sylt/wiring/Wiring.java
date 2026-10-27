package com.dan.sylt.wiring;

import com.dan.cloud.CloudType;
import com.dan.cloud.Clouds;
import com.dan.dune.Dune;
import com.dan.ocean.Ocean;
import com.dan.sky.Sky;
import com.dan.surf.Surf;
import com.dan.sylt.scene.Terrain;

import java.util.Locale;

/**
 * Die Verdrahtung der App (Paketvertrag Punkt 4, aus Brocken, für Phase 1 neu und schlank): hier und nur hier reden
 * die Packages miteinander. Kein Package kennt ein anderes oder die Szene; die App reicht über ihre kleinen
 * Schnittstellen hindurch, was es braucht.
 * <p>
 * Phase 1 verdrahtet die Hüllen der zwei neuen Packages mit dem Gelände der Bühne, damit der Weg von Anfang an steht:
 * <ul>
 * <li>{@code surf}: {@code Surf.Bed} ← Gelände, {@code Surf.Level} ← {@link #pegel} (Phase 1 fest NHN 0, ab Phase 7a
 * aus {@code tide}, F25 A), die See draußen als offene Werte (ab Phase 3 aus {@code ocean}).</li>
 * <li>{@code dune}: {@code Dune.Ground} ← Gelände, der Wind als offener Wert (ab Phase 9a aus {@code wind}, F21 A),
 * die Feuchte am Strand nach der Höhe über dem Pegel [G] (ab Phase 7 aus {@code mudflat}).</li>
 * </ul>
 * Seit Phase 3 dazu Himmel, Wolken und die offene See ({@link #feed(Sky, double)}, {@link #feed(Clouds)},
 * {@link #feed(Ocean)}): das Wetter als offene Werte ({@link #oktas}, {@link #cloudType}, {@link #highCover},
 * {@link #visibilityKm}), die Wetterregie mit Lagen kommt mit Phase 13. Die Dünung der See ist dieselbe, die {@code surf}
 * draußen annimmt (H_s, T_p, Richtung), so laufen offene See und Brandung zusammen; ab Phase 5 das Wellenfeld, ab 9a das
 * Windfeld.
 */
public final class Wiring {
    public final Clock clock;
    /** Wasserstand der See über NHN (m): Phase 1 fest, ab Phase 7a Tide und Windstau. */
    public volatile double pegel = 0;
    /** Wind 10 m über Grund (m/s) und Richtung, aus der er kommt (Grad); Phase 1 offene Werte, ab Phase 3 aus der Regie. */
    public volatile double windSpeed = 6, windFromDeg = 270;

    /** Phase 3: Bedeckung in Achteln, Art der unteren Wolken, hoher Schleier 0..1, Sicht (km) [G: ein Sommerabend wie RotesKliff2]. */
    public volatile double oktas = 3, highCover = 0.35, visibilityKm = 40;
    public volatile CloudType cloudType = CloudType.CUMULUS;
    /** Ob die Wolken gezeichnet werden (sonst graut der Himmel nach der Bedeckung ein). */
    public volatile boolean cloudsDrawn;

    /** Die Brandung (Hülle seit Phase 1): Seegang draußen, Grund, Pegel. */
    public final Surf surf = Surf.sylt();
    /** Flugsand (Hülle seit Phase 1): Korn, Wind, Feuchte. */
    public final Dune dune = Dune.sylt();

    public Wiring(Clock clock) {
        this.clock = clock;
        surf.seed = 2026;
        dune.seed = 2026;
        surf.level = t -> pegel;
        dune.wind = new Dune.Wind() {
            public double speed(double x, double z) { return windSpeed; }
            public double fromDeg(double x, double z) { return windFromDeg; }
        };
        surf.wind = new Surf.Wind() {
            public double speed(double x, double z) { return windSpeed; }
            public double fromDeg(double x, double z) { return windFromDeg; }
        };
    }

    /** Das Gelände an alle, die den Grund brauchen (einmal nach dem Laden). */
    public void setTerrain(Terrain t) {
        surf.bed = t::height;
        dune.ground = t::height;
        // Feuchte des Sands [G bis Phase 7]: unter dem Pegel nass, im Bereich des Auflaufs (bis 1,5 m darüber) feucht, sonst trocken
        dune.moisture = (x, z) -> {
            double ueber = t.height(x, z) - pegel;
            return ueber < 0 ? 0.25 : ueber < 1.5 ? 0.25 * (1 - ueber / 1.5) + 0.003 : 0.003;
        };
    }

    /** Eine Zeile für das HUD: Brandung an der Kamera (Hülle). */
    public String surfZeile(double x, double z) {
        double t = clock.t(), h = surf.depthAt(x, z, t);
        String hier = h <= 0 ? "an Land" : surf.breakingAt(x, z, t)
                ? String.format(Locale.GERMAN, "bricht hier: H %.2f m in %.1f m Wasser, %s", surf.heightAt(x, z, t), h, typName(surf.typeAt(x, z, t)))
                : String.format(Locale.GERMAN, "H %.2f m in %.1f m Wasser", surf.heightAt(x, z, t), h);
        return String.format(Locale.GERMAN, "Brandung (Hülle): See H_s %.1f m, T_p %.0f s aus %03.0f° · Pegel %+.2f m NHN · %s",
                surf.hs, surf.tp, surf.seaFromDeg, pegel, hier);
    }

    /** Eine Zeile für das HUD: Flugsand an der Kamera (Hülle). */
    public String duneZeile(double x, double z) {
        double ut = dune.thresholdAt(x, z), us = dune.ustarAt(x, z), q = dune.fluxAt(x, z);
        return String.format(Locale.GERMAN, "Flugsand (Hülle): Wind %.0f m/s aus %03.0f° · u* %.2f m/s, Schwelle hier %.2f m/s · %s",
                windSpeed, windFromDeg, us, ut, q > 0 ? String.format(Locale.GERMAN, "fliegt: %.1f g/(m·s)", q * 1000) : "liegt");
    }

    // ------------------------------------------------------------------ Phase 3: Himmel, Wolken, See

    /** Dem Himmel Zeit, Ort, Augenhöhe, Sicht und (ohne gezeichnete Wolken) die Bedeckung. */
    public void feed(Sky sky, double eyeAsl) {
        sky.epochMillis = clock.epochMillis();
        sky.latDeg = clock.latDeg();
        sky.lonDeg = clock.lonDeg();
        sky.eyeHeight = Math.max(1, eyeAsl);
        sky.visibilityKm = visibilityKm;
        sky.groundAlbedo = 0.12;
        sky.cloudCover = cloudsDrawn ? 0 : oktas / 8;
        sky.cloudVeil = !cloudsDrawn;
    }

    /** Den Wolken Bedeckung, Art, hohe Schicht und den Wind in Wolkenhöhe (1,6-fach, 20° rechtsdrehend, wie Rungholt) [G]. */
    public void feed(Clouds c) {
        c.oktas = oktas;
        c.type = cloudType;
        c.highCover = highCover;
        c.windSpeed = Math.max(3, windSpeed * 1.6);
        c.windFromDeg = (windFromDeg + 20) % 360;
        c.lens = 0;
    }

    /** Der See Wind, Dünung (dieselbe wie draußen für {@code surf}) und Pegel. */
    public void feed(Ocean o) {
        o.windSpeed = windSpeed;
        o.windFromDeg = windFromDeg;
        o.swellHeight = surf.hs;
        o.swellPeriod = surf.tp;
        o.swellFromDeg = surf.seaFromDeg;
        o.waterLevel = pegel;
    }

    /** Eine Zeile für das HUD: Wetter. */
    public String wetterZeile() {
        return String.format(Locale.GERMAN, "Wetter: %.0f/8 %s, hoher Schleier %.0f %%, Sicht %.0f km, Wind %.0f m/s aus %03.0f°",
                oktas, cloudType.label, highCover * 100, visibilityKm, windSpeed, windFromDeg);
    }

    public static String typName(Surf.Breaker b) {
        return switch (b) {
            case NONE -> "bricht nicht";
            case SPILLING -> "Schwallbrecher";
            case PLUNGING -> "Sturzbrecher";
            case COLLAPSING -> "Reflexionsbrecher";
            case SURGING -> "Brandungsbrecher";
        };
    }
}
