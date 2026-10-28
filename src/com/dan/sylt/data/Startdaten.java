package com.dan.sylt.data;

import com.dan.sylt.camera.Ansichten;
import com.dan.sylt.scene.Kante;
import com.dan.sylt.scene.Regie;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Sturmflut;
import com.dan.sylt.scene.Terrain;
import com.dan.sylt.wiring.Wetterregie;
import com.dan.store.Felder;
import com.dan.store.Zahl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Die mitgelieferten Daten (Phase 16, Werkbuch Abschnitt 10, F33–F45 „alle A“): Quellen, Befunde, Sturmfluten und
 * Vorspülungen aus {@code quellen.txt}, {@code befunde.txt}, {@code sturmfluten.txt} und {@code vorspuelungen.txt} in
 * diesem Package (recherchiert am 28.10.2026); die Kante 2024 aus {@code daten/kliff/kante.csv}; Gelände, Lagen,
 * Leuchtfeuer, Regiebücher, Ansichten, Fahrten und die Presets der Packages aus dem Code. Daraus schreibt
 * {@link StartdatenSql} das Skript {@code db/sql/04_startdaten.sql}; ohne Datenbank liest {@link DateiSpeicher}
 * dieselben Listen.
 */
public final class Startdaten {
    private Startdaten() { }

    // ------------------------------------------------------------------ Textdateien im Package

    static List<String[]> zeilen(String datei) {
        try (InputStream in = Startdaten.class.getResourceAsStream(datei)) {
            if (in == null) throw new IllegalStateException(datei + " fehlt im Package com.dan.sylt.data");
            List<String[]> l = new ArrayList<>();
            for (String z : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (z.isBlank() || z.startsWith("#")) continue;
                String[] p = z.split("\\|", -1);
                for (int i = 0; i < p.length; i++) p[i] = p[i].strip();
                l.add(p);
            }
            return l;
        } catch (IOException e) {
            throw new IllegalStateException(datei + " nicht lesbar: " + e.getMessage(), e);
        }
    }

    private static List<String> liste(String s) {
        List<String> l = new ArrayList<>();
        for (String x : s.split(",")) if (!x.isBlank()) l.add(x.strip());
        return l;
    }

    static String leer(String s) { return s == null || s.isEmpty() ? null : s; }

    static Integer ganz(String s) { return s == null || s.isBlank() ? null : Integer.valueOf(s.strip()); }

    static Double zahl(String s) { return s == null || s.isBlank() ? null : Zahl.lies(s.strip()); }

    static LocalDate datum(String s) {
        String[] d = s.strip().split("\\.");
        return LocalDate.of(Integer.parseInt(d[2]), Integer.parseInt(d[1]), Integer.parseInt(d[0]));
    }

    private static String feld(String[] p, int i) { return i < p.length ? p[i] : ""; }

    public static List<Daten.Quelle> quellen() {
        List<Daten.Quelle> l = new ArrayList<>();
        for (String[] p : zeilen("quellen.txt"))
            l.add(new Daten.Quelle(p[0], p[3], leer(feld(p, 4)), p[1], p[2].isEmpty() ? null : LocalDate.parse(p[2]), leer(feld(p, 5))));
        return l;
    }

    public static List<Daten.Befund> befunde() {
        List<Daten.Befund> l = new ArrayList<>();
        for (String[] p : zeilen("befunde.txt")) l.add(new Daten.Befund(p[0], p[1], p[2], p[3], liste(feld(p, 4))));
        return l;
    }

    /** Die belegten Sturmfluten seit 1962 (F38 A). */
    public static List<Daten.Sturmflut> sturmfluten() {
        List<Daten.Sturmflut> l = new ArrayList<>();
        for (String[] p : zeilen("sturmfluten.txt"))
            l.add(new Daten.Sturmflut(p[0], datum(p[1]), ganz(p[2]), ganz(p[3]), ganz(p[4]), leer(p[5]), zahl(p[6]), leer(p[7]), leer(p[8]), leer(feld(p, 9))));
        return l;
    }

    /** Der Name der belegten Sturmflut, nach der die App spielt (Xaver), wenn ihr Tag der der App ist. */
    public static String belegteSturmflut(LocalDate beginn) {
        if (!Sturmflut.DATUM.equals(beginn)) return null;
        for (Daten.Sturmflut s : sturmfluten()) if (s.name().startsWith("Xaver")) return s.name();
        return null;
    }

    /** Die belegten Vorspülungen vor Wenningstedt und Kampen (F39 A). */
    public static List<Daten.Vorspuelung> vorspuelungen() {
        List<Daten.Vorspuelung> l = new ArrayList<>();
        for (String[] p : zeilen("vorspuelungen.txt")) {
            Integer m = ganz(p[2]);
            l.add(new Daten.Vorspuelung(0, p[0], "BELEGT", ganz(p[1]), m == null ? null : (long) m, null, null, leer(p[3]), null, null, null, null, null, leer(p[4]),
                    leer(feld(p, 5))));
        }
        return l;
    }

    // ------------------------------------------------------------------ Gelände und Kante

    /** Das Gelände (Phase 2): Kern und Fernfeld als Flächen, Ursprung am Quermarkenfeuer, die Dateien mit SHA-256. */
    public static List<Daten.Gelaende> gelaende(File dir) {
        double[] k = Site.KERN.scene(), f = Site.FERNFELD.scene();
        Map<String, String> dateien = new LinkedHashMap<>();
        for (String n : new String[]{Terrain.KERN, Terrain.HOEHE, Terrain.FERN, Terrain.ARTEN, Terrain.INFO}) dateien.put(n, sha256(new File(dir, n)));
        return List.of(new Daten.Gelaende("Rotes Kliff", "2024", Daten.rechteck(k[0], k[1], k[2], k[3]), Daten.rechteck(f[0], f[1], f[2], f[3]), Site.E0, Site.N0,
                Terrain.KCELL, Terrain.CELL, Terrain.FCELL, dateien, "GEOBASIS_SH",
                "Kern 2,3 × 3,6 km aus dem DGM1 (1 m), das Fernfeld aus BAW und DGM1 (10 m, außen 100 m); Szenenmeter x nach Osten, z nach Süden, "
                        + "Ursprung am Quermarkenfeuer (E 457 600, N 6 091 170); Höhen NHN"));
    }

    /** Ab hier beginnt ein neues Stück der Kante (Lücke in Nord, m). */
    public static final double LUECKE = 3;
    /** Länge eines Stücks der Kante 2024 (Zeilen zu rund 1 m): so ersetzt ein Abbruch nur die Stücke, deren Mitte er trifft. */
    public static final int STUECK = 25;

    /**
     * Die Kante 2024 aus {@code daten/kliff/kante.csv} (F36 A): je {@link #STUECK} m eine Zeile mit Oberkante und Fuß
     * als Linie (ein Punkt je Meter, auf 2 m ausgedünnt; benachbarte Stücke teilen ihren Endpunkt). Ein Abbruch ersetzt in
     * SY_KANTE_V die Stücke, deren Mitte in seinem Abschnitt liegt.
     */
    public static List<Daten.Kantenstueck> kante2024(File daten) {
        Kante k = Kante.load(daten);
        if (k == null) return List.of();
        return kante2024(k);
    }

    public static List<Daten.Kantenstueck> kante2024(Kante k) {
        List<Daten.Kantenstueck> l = new ArrayList<>();
        int a = 0;
        for (int i = 1; i <= k.n; i++) {
            if (i < k.n && Math.abs(k.nord[i] - k.nord[i - 1]) <= LUECKE) continue;
            // der zusammenhängende Abschnitt a .. i-1 in Stücke zu STUECK Zeilen; ein kurzer Rest hängt am letzten Stück
            int ende = i - 1;
            if (ende - a >= 2) {
                int b = a;
                while (b < ende) {
                    int c = Math.min(ende, b + STUECK);
                    if (ende - c < STUECK / 2) c = ende;
                    l.add(stueck(k, b, c));
                    b = c;
                }
            }
            a = i;
        }
        return l;
    }

    static Daten.Kantenstueck stueck(Kante k, int a, int b) {
        int n = b - a + 1;
        double[][] o = new double[n][], f = new double[n][];
        double wand = 0, hk = 0;
        for (int i = a; i <= b; i++) {
            o[i - a] = new double[]{k.kanteX(i), k.z(i)};
            f[i - a] = new double[]{k.fussX(i), k.z(i)};
            wand = Math.max(wand, k.wand[i]);
            hk = Math.max(hk, k.hKante[i]);
        }
        return new Daten.Kantenstueck(0, "GEMESSEN_2024", null, k.nord[a], k.nord[b], Daten.Geom.linie(Daten.ausduennen(o, 2)), Daten.Geom.linie(Daten.ausduennen(f, 2)),
                r1(wand), r1(hk), null);
    }

    static double r1(double v) { return Math.round(v * 10) / 10.0; }
    static double r2(double v) { return Math.round(v * 100) / 100.0; }

    // ------------------------------------------------------------------ Lagen und Leuchtfeuer

    public static List<Daten.Lage> lagen() {
        List<Daten.Lage> l = new ArrayList<>();
        String[] text = {"Leichter Westwind, Haufenwolken, die Sonne geht über der See unter", "Frischer West, Wolken ziehen, Brandung mit Schaumkronen",
                "Ostwind vom Land: die Brecher stürzen, Gischt weht seewärts", "Schauer ziehen über die See, dazwischen Sonne", "Grauer Landregen aus Südwest",
                "Sturm aus Westsüdwest, die See bricht weit draußen", "Seenebel liegt über Strand und Kliff", "Kalte Luft über warmem Wasser: Seerauch im Winter",
                "Gewitter über der See mit Blitz und Donner"};
        int i = 0;
        for (Wetterregie.Lage g : Wetterregie.Lage.values())
            l.add(new Daten.Lage(g.label, g.name(), g.phase.name(), g.wind, g.dir, g.gust, g.oktas, g.high, g.cloud.name(), g.rain, g.vis, g.fog, g.fogTop, g.hs, g.tp,
                    g.seaDir, g.thunder, g.temp, i < text.length ? text[i++] + " [G]" : "[G]", Daten.MITGELIEFERT));
        Sturmflut.Punkt x = Sturmflut.an(12);
        l.add(new Daten.Lage("Xaver (Höhepunkt)", null, "ORKAN", x.wind(), x.richtung(), 0.3, 8, 0, "STRATOCUMULUS", 5, 3, 0, 0, x.hs(), x.tp(), x.richtung(), 0, 4,
                "Die Sturmflut nach Xaver in der Nacht zum 6.12.2013: Orkan aus Nordwest, Böen 39 m/s, See H_s 6 m (Fahrplan der App) [G]", Daten.MITGELIEFERT));
        return l;
    }

    /** Die beiden Türme (F40 A) mit den Sektoren aus {@code com.dan.beacon}. */
    public static List<Daten.Leuchtfeuer> leuchtfeuer() {
        return List.of(
                feuer("Kampener Leuchtturm", com.dan.beacon.Beacon.kampen(), "Iso WR 7s", 62, 40, 20, 1856, null, true, Site.LEUCHTTURM,
                        "„Langer Christian“, 1855/56; gelbe Bornholmer Ziegel mit Eisenringen, weiß mit schwarzem Band; seit 1975 mit rotem Sektor über dem "
                                + "Roten Kliff anstelle des Quermarkenfeuers (Grenzen des Sektors geschätzt [G])", "WIKI_KAMPEN"),
                feuer("Quermarkenfeuer Rotes Kliff", com.dan.beacon.Beacon.rotesKliff(), "F WRG", 23, 13, 21, 1913, 1975, false, Site.QUERMARKE,
                        "Achteckiger Backsteinturm 1912/13, 1936 elektrisch, 1975 außer Betrieb, seit 1993 restauriert; heute Tagesmarke; Sektoren wie früher "
                                + "(Grenzen und Tragweiten von Rot und Grün geschätzt [G])", "WIKI_QUERMARKE"));
    }

    private static Daten.Leuchtfeuer feuer(String name, com.dan.beacon.Beacon b, String kennung, double feuerhoehe, double turm, double tragweite, Integer von, Integer bis,
                                          boolean an, Site.Ort ort, String text, String quelle) {
        List<Daten.Sektor> s = new ArrayList<>();
        int nr = 0;
        for (com.dan.beacon.SectorTable.Sector x : b.sectors().sectors())
            s.add(new Daten.Sektor(++nr, x.colour().code(), x.from(), x.to(), x.rangeNm(), leer(x.name())));
        return new Daten.Leuchtfeuer(name, kennung, feuerhoehe, turm, tragweite, von, bis, an, ort.x(), ort.z(), text, quelle, s);
    }

    // ------------------------------------------------------------------ Regiebücher

    /** Kopfwerte eines Regiebuchs für SY_REGIEBUCH: Szenenzeit des ersten zeit (oder der Sturmflut), Zeitraffer, Fotos, Warten, Blatt. */
    public record Kopf(Long szeneMs, Double raffer, int fotos, int warten, boolean blatt) {
        public static Kopf aus(Regie.Book b) {
            Long t = null;
            Double r = null;
            int fotos = 0, warten = 0;
            boolean blatt = false;
            for (Regie.Zeile z : b.zeilen()) {
                switch (z.befehl()) {
                    case "zeit" -> {
                        if (t != null) break;
                        try {
                            String[] p = z.argumente().strip().split("\\s+");
                            String[] u = p.length > 1 ? p[1].split(":") : new String[]{"0", "0"};
                            t = ZonedDateTime.of(datum(p[0]), LocalTime.of(Integer.parseInt(u[0]), Integer.parseInt(u[1])), SceneClock.ZONE).toInstant().toEpochMilli();
                        } catch (RuntimeException e) { /* bleibt leer */ }
                    }
                    case "sturmflut" -> { if (t == null) t = (long) Sturmflut.startMillis(); }
                    case "raffer" -> { if (r == null) r = Zahl.lies(z.argumente().strip().split("\\s+")[0]); }
                    case "foto" -> fotos++;
                    case "warte" -> warten++;
                    case "blatt" -> blatt = true;
                    default -> { }
                }
            }
            return new Kopf(t, r == null || Double.isNaN(r) ? null : r, fotos, warten, blatt);
        }
    }

    public static List<Daten.Regiebuch> regiebuecher() {
        List<Daten.Regiebuch> l = new ArrayList<>();
        for (String d : Regie.DATEIEN) {
            String t = Regie.text(d);
            Regie.Book b = Regie.parse(t);
            l.add(new Daten.Regiebuch(b.name(), t, b.about(), d, Daten.MITGELIEFERT));
        }
        return l;
    }

    // ------------------------------------------------------------------ Ansichten und Fahrten

    /**
     * Die Ansichten: die neun festen (Tasten 1–9), der Blick „Sandsturm am Strand“ und die acht Vorlagen der Türme.
     * Kamera: x, Augenhöhe NaN (aus dem Gelände), z, Blick, Neigung.
     */
    public static List<Daten.Ansicht> ansichten() {
        List<Daten.Ansicht> l = new ArrayList<>();
        for (Ansichten.Ansicht a : Ansichten.ALLE) l.add(ansicht(a, a.key(), "FEST"));
        l.add(ansicht(Ansichten.SANDSTURM, null, "BLICK"));
        for (Ansichten.Vorlage v : Ansichten.VORLAGEN) l.add(ansicht(v.ansicht(), null, "VORLAGE"));
        return l;
    }

    private static Daten.Ansicht ansicht(Ansichten.Ansicht a, Integer taste, String art) {
        return new Daten.Ansicht(0, a.name(), taste, art, new double[]{a.x(), Double.NaN, a.z(), a.heading(), a.pitch()}, a.fovy(), a.eyeAboveGround(), null, null, null,
                Daten.MITGELIEFERT);
    }

    /** Die vier Fahrten der Regiebücher; ihre Wege rechnet die App aus der Szene (Kante, Möwen, Wellen). */
    public static List<Daten.Fahrt> fahrten() {
        return List.of(
                new Daten.Fahrt("Treppe", "TREPPE", 30.0, null, "Die Treppe am Kliff hinauf, oben der Blick nach Westsüdwest über die See", Daten.MITGELIEFERT, List.of()),
                new Daten.Fahrt("Drohne", "DROHNE", 40.0, 2000.0, "130 m vor dem Fuß, 45 m über dem Pegel von Süd nach Nord am Kliff entlang", Daten.MITGELIEFERT, List.of()),
                new Daten.Fahrt("Möwe", "MOEWE", null, null, "Mitfahrt 5 m hinter der nächsten fliegenden Möwe", Daten.MITGELIEFERT, List.of()),
                new Daten.Fahrt("Welle", "WELLE", null, null, "Mit der höchsten Welle vom Kamm bis in den Schwall, in der Rinne unter Wasser", Daten.MITGELIEFERT, List.of()));
    }

    // ------------------------------------------------------------------ Presets (F43 A)

    /** Die Klassen, deren Presets mitkommen: jede öffentliche statische Methode ohne Argument, die die Klasse liefert. */
    public static final String[] PAKETE = {"com.dan.surf.Surf", "com.dan.dune.Dune", "com.dan.ocean.Ocean", "com.dan.cloud.Clouds", "com.dan.rain.Rain", "com.dan.fog.Fog",
            "com.dan.lightning.Lightning", "com.dan.soundscape.Soundscape", "com.dan.tide.Tide"};
    /** Die Namen der Presets aus den Lagen der App (F43 A), für Surf und Dune. */
    public static final String[] LAGEN_PRESETS = {"Sommerabend", "Westwind", "Xaver"};
    private static List<Daten.PaketPreset> presets;

    public static synchronized List<Daten.PaketPreset> presets() {
        if (presets != null) return presets;
        List<Daten.PaketPreset> l = new ArrayList<>();
        for (String k : PAKETE) {
            try {
                Class<?> c = Class.forName(k);
                List<Method> ms = new ArrayList<>();
                for (Method m : c.getMethods())
                    if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0 && m.getReturnType() == c) ms.add(m);
                ms.sort(Comparator.comparing(Method::getName));
                for (Method m : ms) {
                    Object o = m.invoke(null);
                    l.add(new Daten.PaketPreset(c.getPackageName(), c.getSimpleName(), m.getName(), "Preset " + m.getName() + "() aus " + c.getPackageName(),
                            Daten.MITGELIEFERT, Felder.lesen(o)));
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Preset von " + k + " nicht lesbar: " + e, e);
            }
        }
        Sturmflut.Punkt x = Sturmflut.an(12);
        double[][] see = {{Wetterregie.Lage.SOMMERABEND.hs, Wetterregie.Lage.SOMMERABEND.tp, Wetterregie.Lage.SOMMERABEND.seaDir, Wetterregie.Lage.SOMMERABEND.wind,
                Wetterregie.Lage.SOMMERABEND.dir},
                {Wetterregie.Lage.WESTWIND.hs, Wetterregie.Lage.WESTWIND.tp, Wetterregie.Lage.WESTWIND.seaDir, Wetterregie.Lage.WESTWIND.wind, Wetterregie.Lage.WESTWIND.dir},
                {x.hs(), x.tp(), x.richtung(), x.wind(), x.richtung()}};
        String[] text = {"Die Lage „Sommerabend“: Dünung aus Westnordwest, leichter Wind", "Die Lage „Westwind“: frische Windsee aus West",
                "Die Sturmflut nach Xaver auf dem Höhepunkt (Nacht zum 6.12.2013)"};
        for (int i = 0; i < LAGEN_PRESETS.length; i++) {
            com.dan.surf.Surf s = com.dan.surf.Surf.sylt();
            s.hs = see[i][0];
            s.tp = see[i][1];
            s.seaFromDeg = see[i][2];
            l.add(new Daten.PaketPreset("com.dan.surf", "Surf", LAGEN_PRESETS[i], text[i], Daten.MITGELIEFERT, Felder.lesen(s)));
            com.dan.dune.Dune d = com.dan.dune.Dune.sylt();
            d.windSpeed = see[i][3];
            d.windFromDeg = see[i][4];
            l.add(new Daten.PaketPreset("com.dan.dune", "Dune", LAGEN_PRESETS[i], text[i], Daten.MITGELIEFERT, Felder.lesen(d)));
        }
        presets = Collections.unmodifiableList(l);
        return presets;
    }

    /** Die Typen der Felder eines Presets (DOUBLE, INT, BOOLEAN, STRING, ENUM) aus seiner Klasse. */
    public static Map<String, String> typen(Daten.PaketPreset p) {
        try {
            return Felder.typen(Class.forName(p.paket() + "." + p.klasse()));
        } catch (ClassNotFoundException e) {
            return Map.of();
        }
    }

    // ------------------------------------------------------------------ Hilfen

    /** SHA-256 einer Datei (hex) oder null, wenn sie fehlt. */
    public static String sha256(File f) {
        if (!f.isFile()) return null;
        try (InputStream in = new java.io.FileInputStream(f)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[1 << 16];
            for (int n; (n = in.read(buf)) > 0; ) md.update(buf, 0, n);
            StringBuilder b = new StringBuilder();
            for (byte x : md.digest()) b.append(String.format("%02x", x));
            return b.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /** Für Prüfungen: alle Zahlen auf einen Blick. */
    public static String zusammenfassung() {
        return String.format(Locale.GERMAN, "%d Quellen, %d Befunde, %d Sturmfluten, %d Vorspülungen, %d Lagen, %d Leuchtfeuer, %d Regiebücher, %d Ansichten, "
                        + "%d Fahrten, %d Presets", quellen().size(), befunde().size(), sturmfluten().size(), vorspuelungen().size(), lagen().size(), leuchtfeuer().size(),
                regiebuecher().size(), ansichten().size(), fahrten().size(), presets().size());
    }
}
