package com.dan.sylt.data;

import com.dan.sylt.camera.Camera;
import com.dan.sylt.gl.Quality;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.wiring.Wetterregie;
import com.dan.store.Felder;
import com.dan.store.Zahl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Der ganze Zustand der Szene als Text (F42 A, „Weiter wie zuletzt“): alle öffentlichen Felder der Packages, dazu Uhr,
 * Zeitraffer, Kamera, Wetterregie (Modus und Lage), Sturmflut und Windstau, das laufende Regiebuch mit Stelle und –
 * das Neue in Sylt – die Änderung des Geländes: Abbrüche, Sturzkegel, Kehle, Kante, Spur und die Vorspülung
 * ({@code kliff.stand}, {@code vorspuelung.stand}, gepackt). So steht das Kliff nach dem Neustart wie zuvor.
 * Dasselbe Format dient für gemerkte Ansichten (ohne Gelände) und die Dateien ohne Datenbank.
 * <p>
 * Was gerade fällt (ein Stück Kliff, das gerechnet wird), kommt nicht zurück: die Kehle bleibt, es bricht wieder.
 * Ein Regiebuch beginnt man im Block REGIEBÜCHER neu. Schlüssel der Form {@code teil.feld}: {@code surf.hs},
 * {@code kamera.x}, {@code regie.lage}.
 */
public final class Zustand {
    private Zustand() { }

    static final Set<String> OHNE = Set.of("quality");
    /** Anfragen, Zustände des Laufs, abgeleitete Größen. */
    static final Set<String> OHNE_RENDERER = Set.of("photoRequest", "frozen", "photoName", "ohneBlatt", "fotoAufschub", "fotosVerlangt", "renderScale", "quality");
    static final Set<String> OHNE_WIRING = Set.of("pegel", "cloudsDrawn", "gustNow", "quality");
    static final Set<String> OHNE_SKY = Set.of("epochMillis", "latDeg", "lonDeg", "quality");
    static final Set<String> OHNE_KLANG = Set.of("note", "offline", "on", "schwallbrecher", "sturzbrecher", "schwaelle", "moewen", "sanderlinge", "austernfischer", "seehunde",
            "kuehe", "donner", "abbrueche", "brocken", "rieseln", "stuecke", "wattsHier", "sturzAnteil", "kanteDb", "seeAnteil");

    /** Die Teile mit öffentlichen Feldern: Name im Zustand → Objekt. */
    static Map<String, Object> teile(Renderer r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("renderer", r);
        m.put("wiring", r.wiring);
        m.put("surf", r.wiring.surf);
        m.put("dune", r.wiring.dune);
        m.put("tide", r.wiring.tide);
        m.put("ocean", r.meer.ocean);
        m.put("sky", r.wetter.sky);
        m.put("clouds", r.wetter.clouds);
        m.put("unwetter", r.unwetter);
        m.put("rain", r.unwetter.rain);
        m.put("fog", r.unwetter.fog);
        m.put("blitz", r.unwetter.blitz);
        m.put("klang", r.klang);
        m.put("soundscape", r.klang.s);
        if (r.feuer != null) m.put("feuer", r.feuer);
        return m;
    }

    /** Das laufende Objekt einer Klasse mit Presets (Startdaten.PAKETE, einfacher Name) in der Szene, oder null. */
    public static Object objekt(Renderer r, String klasse) {
        for (Object o : teile(r).values()) if (o != null && o.getClass().getSimpleName().equals(klasse)) return o;
        return null;
    }

    private static Set<String> ohne(String teil) {
        return switch (teil) {
            case "renderer" -> OHNE_RENDERER;
            case "wiring" -> OHNE_WIRING;
            case "sky" -> OHNE_SKY;
            case "klang" -> OHNE_KLANG;
            default -> OHNE;
        };
    }

    /**
     * Erfasst den Zustand (aus dem Swing-Faden oder dem Faden der Daten; die Felder sind volatile). Mit {@code gelaende}
     * kommen der Stand des Kliffs und der Vorspülung mit (für die Sitzung; eine gemerkte Ansicht braucht sie nicht).
     */
    public static LinkedHashMap<String, String> erfassen(Renderer r, boolean gelaende) {
        LinkedHashMap<String, String> z = new LinkedHashMap<>();
        z.put("app.version", Renderer.VERSION);
        z.put("app.qualitaet", r.quality().name());
        z.put("app.ton", Boolean.toString(r.klang.on));
        z.put("uhr.ms", Long.toString((long) Math.floor(r.clock.epochMillis())));
        z.put("uhr.text", Daten.text(r.clock.epochMillis()));
        z.put("uhr.lapse", Zahl.text(r.clock.lapse));
        Camera c = r.camera;
        z.put("kamera.modus", c.mode == Camera.Mode.KREISEN ? "KREISEN" : "FREI");
        z.put("kamera.kreisen", Boolean.toString(c.autoOrbit));
        double[] k = kamera(r);
        String[] n = {"x", "y", "z", "gier", "nick"};
        for (int i = 0; i < 5; i++) z.put("kamera." + n[i], Zahl.text(Math.round(k[i] * 1000) / 1000.0));
        z.put("kamera.ziel", Zahl.text(c.tx) + " " + Zahl.text(c.ty) + " " + Zahl.text(c.tz) + " " + Zahl.text(c.yaw) + " " + Zahl.text(c.pitch) + " " + Zahl.text(c.dist));
        z.put("kamera.oeffnung", Zahl.text(c.fovy));
        Wetterregie w = r.wiring.regie;
        z.put("regie.modus", w.modus().name());
        z.put("regie.lage", w.lage().name());
        z.put("regie.lageText", w.lage().label);
        z.put("pegel.jetzt", Zahl.text(Math.round(r.wiring.pegel * 1000) / 1000.0));
        z.put("pegel.windstau", Zahl.text(Math.round(r.wiring.tide.surge * 1000) / 1000.0));
        z.put("sturmflut.aktiv", Boolean.toString(r.sturmflut.aktiv));
        if (r.sturmflut.aktiv) z.put("sturmflut.datum", r.sturmflut.datum.toString());
        var bk = r.regie.book();
        if (bk != null) {
            z.put("regiebuch.name", bk.name());
            z.put("regiebuch.sekunde", Zahl.text(Math.round(r.regie.time() * 100) / 100.0));
        }
        if (r.abbruch != null) {
            z.put("kliff.abbrueche", Integer.toString(r.abbruch.abbrueche));
            if (gelaende) z.put("kliff.stand", r.abbruch.stand());
        }
        if (gelaende && r.vorspuelung != null) {
            String v = r.vorspuelung.stand();
            if (v != null) z.put("vorspuelung.stand", v);
        }
        for (Map.Entry<String, Object> e : teile(r).entrySet())
            for (Map.Entry<String, String> f : Felder.lesen(e.getValue(), ohne(e.getKey())).entrySet()) z.put(e.getKey() + "." + f.getKey(), f.getValue());
        return z;
    }

    /** Die Spalten von SY_SITZUNG aus einem Zustand (für {@code Ablage.Sicherung}). */
    public static Map<String, String> spalten(Map<String, String> z) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(Speicher.SZENE_MS, z.getOrDefault("uhr.ms", ""));
        m.put(Speicher.ZEITRAFFER, z.getOrDefault("uhr.lapse", ""));
        m.put(Speicher.LAGE, z.getOrDefault("regie.lageText", ""));
        m.put(Speicher.PEGEL, z.getOrDefault("pegel.jetzt", ""));
        m.put(Speicher.WINDSTAU, z.getOrDefault("pegel.windstau", ""));
        m.put(Speicher.STURMFLUT, z.getOrDefault("sturmflut.aktiv", "false"));
        m.put(Speicher.ABBRUECHE, z.getOrDefault("kliff.abbrueche", ""));
        if (z.containsKey("regiebuch.name")) { m.put(Speicher.REGIEBUCH, z.get("regiebuch.name")); m.put(Speicher.REGIE_SEKUNDE, z.get("regiebuch.sekunde")); }
        return m;
    }

    /** Kamera für Ansichten: {x, y, z, Gier, Nick} aus dem laufenden Bild. */
    public static double[] kamera(Renderer r) {
        Camera c = r.camera;
        double[] e = c.eye, f = c.fwd;
        double nick = Math.toDegrees(-Math.asin(Math.max(-1, Math.min(1, f[1]))));
        return new double[]{e[0], e[1], e[2], c.heading(), nick};
    }

    /** Szenenzeit des Zustands (ms) oder null. */
    public static Long szeneMs(Map<String, String> z) {
        double v = Zahl.lies(z.get("uhr.ms"));
        return Double.isNaN(v) ? null : (long) v;
    }

    public static Quality qualitaet(Map<String, String> z) {
        try { return Quality.valueOf(z.get("app.qualitaet")); } catch (RuntimeException e) { return null; }
    }

    /**
     * Stellt den Zustand her (aus dem Swing-Faden) außer Qualität, Ton und Gelände (das macht {@code ui.Weiter}). Gibt
     * zurück, was nicht ging.
     */
    public static List<String> anwenden(Renderer r, Map<String, String> z) {
        List<String> fehler = new ArrayList<>();
        // erst die Regie, denn sie setzt Wind, Wolken und See; die Felder danach sind die gespeicherten
        Wetterregie w = r.wiring.regie;
        try {
            Wetterregie.Modus mo = Wetterregie.Modus.valueOf(z.getOrDefault("regie.modus", "LAGE"));
            Wetterregie.Lage la = Wetterregie.Lage.valueOf(z.getOrDefault("regie.lage", "SOMMERABEND"));
            switch (mo) {
                case LAGE -> w.lage(la);
                case FREI -> w.frei();
                case STURMTAG -> w.sturmtag();
                case HAND -> { w.lage(la); w.hand(); }
            }
        } catch (IllegalArgumentException e) {
            fehler.add("regie: " + e.getMessage());
        }
        for (Map.Entry<String, Object> e : teile(r).entrySet()) {
            Map<String, String> t = Felder.teil(z, e.getKey());
            t.keySet().removeIf(x -> x.contains("."));
            for (String f : Felder.schreiben(e.getValue(), t, ohne(e.getKey()))) fehler.add(e.getKey() + "." + f);
        }
        Long ms = szeneMs(z);
        if (ms != null) r.clock.setEpochMillis(ms);
        double lapse = Zahl.lies(z.get("uhr.lapse"));
        if (!Double.isNaN(lapse)) r.clock.lapse = lapse;
        double stau = Zahl.lies(z.get("pegel.windstau"));
        if (!Double.isNaN(stau)) r.wiring.tide.surge = stau;
        if ("true".equals(z.get("sturmflut.aktiv"))) {
            try { r.sturmflut.datum = LocalDate.parse(z.getOrDefault("sturmflut.datum", com.dan.sylt.scene.Sturmflut.DATUM.toString())); } catch (RuntimeException e) { fehler.add("sturmflut.datum"); }
            r.sturmflut.aktiv = true;
            r.wiring.tideAn = true;
        } else r.sturmflut.aktiv = false;
        Camera c = r.camera;
        c.stopFahrt();
        c.stopMitfahrt();
        c.diving = false;
        double oe = Zahl.lies(z.get("kamera.oeffnung"));
        if (!Double.isNaN(oe)) c.fovy = oe;
        String[] ziel = z.getOrDefault("kamera.ziel", "").split(" ");
        if ("KREISEN".equals(z.get("kamera.modus")) && ziel.length == 6) {
            c.mode = Camera.Mode.KREISEN;
            c.lookAt(Zahl.lies(ziel[0]), Zahl.lies(ziel[1]), Zahl.lies(ziel[2]), Zahl.lies(ziel[3]), Zahl.lies(ziel[4]), Zahl.lies(ziel[5]));
            c.autoOrbit = "true".equals(z.get("kamera.kreisen"));
            c.snap();
        } else {
            double x = Zahl.lies(z.get("kamera.x")), y = Zahl.lies(z.get("kamera.y")), zz = Zahl.lies(z.get("kamera.z"));
            if (!Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(zz)) {
                c.mode = Camera.Mode.FREI;
                c.place(x, y, zz, Zahl.lies(z.get("kamera.gier")), Zahl.lies(z.get("kamera.nick")));
                c.snap();
            }
        }
        return fehler;
    }
}
