package com.dan.sylt.data;

import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Site;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Die Datensätze, die {@link Speicher} liest und schreibt (Phase 16, Werkbuch Abschnitt 10, F33–F45 „alle A“). Je
 * Tabelle ein Record mit den Spalten, die die App braucht; ID, ANGELEGT und GEAENDERT führt die Datenbank selbst.
 * Lagen stehen in Szenenmetern (x nach Osten, z nach Süden, Ursprung am Quermarkenfeuer); in Tabellen und Dateien in
 * UTM 32N (SRID 25832, F35 A): Ost = x + 457 600, Nord = 6 091 170 − z.
 * <p>
 * Szenenzeit (F34 A): Millisekunden seit 1970 wie die Uhr der Szene, dazu als TIMESTAMP WITH TIME ZONE in
 * Europe/Berlin (in der Datenbank) und als Text („06.12.2013 02:30 MEZ“).
 * <p>
 * Nicht zu verwechseln mit {@code com.dan.sylt.scene.Daten} (Zustand der aufbereiteten Dateien in daten/).
 */
public final class Daten {
    private Daten() { }

    /** HERKUNFT: mitgeliefert (stellt der Einrichter wieder her) oder eigen (fasst er nie an). */
    public static final String MITGELIEFERT = "MITGELIEFERT", EIGEN = "EIGEN";
    /** BEFUND: belegt, gemessen oder gestaltet [G]. */
    public static final String BELEGT = "BELEGT", GEMESSEN = "GEMESSEN", GESTALTET = "GESTALTET";

    private static final DateTimeFormatter ZEIT = DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm", Locale.GERMANY);

    /** {Ost, Nord} eines Szenenpunkts. */
    public static double[] utm(double x, double z) { return Site.toUtm(x, z); }

    /** Szenenpunkt {x, z} aus Ost und Nord. */
    public static double[] xz(double ost, double nord) { return Site.fromUtm(ost, nord); }

    /** Zeit der Szene aus ms seit 1970 in deutscher Zeit. */
    public static ZonedDateTime zeit(double epochMs) { return Instant.ofEpochMilli((long) Math.floor(epochMs)).atZone(SceneClock.ZONE); }

    /** „06.12.2013 02:30 MEZ“, „20.07.2026 20:30 MESZ“. */
    public static String text(double epochMs) {
        ZonedDateTime z = zeit(epochMs);
        return ZEIT.format(z) + " " + (SceneClock.ZONE.getRules().isDaylightSavings(z.toInstant()) ? "MESZ" : "MEZ");
    }

    /** „2013-12-06“ */
    public static String tag(double epochMs) { return zeit(epochMs).toLocalDate().toString(); }

    /** Eine Zahl mit höchstens zwei Stellen nach dem Punkt (Meter in UTM: Zentimeter). */
    static String n2(double v) {
        String s = String.format(Locale.ROOT, "%.2f", v);
        while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }

    /**
     * Eine Geometrie in Szenenmetern: PUNKT, LINIE, MULTILINIE oder FLAECHE (ein Ring außen, ohne Löcher). Als WKT in UTM
     * 32N mit Zentimetern.
     */
    public record Geom(String art, List<double[][]> teile) {
        public Geom { teile = List.copyOf(teile); }
        public static Geom punkt(double x, double z) { return new Geom("PUNKT", List.<double[][]>of(new double[][]{{x, z}})); }
        public static Geom linie(double[][] p) { return new Geom("LINIE", List.<double[][]>of(p)); }
        public static Geom multilinie(List<double[][]> p) { return new Geom("MULTILINIE", p); }
        public static Geom flaeche(double[][] p) { return new Geom("FLAECHE", List.<double[][]>of(p)); }
        public double[][] xz() { return teile.get(0); }

        private static String folge(double[][] p, boolean zu) {
            StringBuilder b = new StringBuilder("(");
            int n = zu ? p.length + 1 : p.length;
            for (int i = 0; i < n; i++) {
                double[] u = utm(p[i % p.length][0], p[i % p.length][1]);
                b.append(i > 0 ? ", " : "").append(n2(u[0])).append(' ').append(n2(u[1]));
            }
            return b.append(')').toString();
        }

        /** Als WKT in UTM 32N (Fläche geschlossen und außen gegen den Uhrzeigersinn in Ost/Nord, wie Oracle es will). */
        public String wkt() {
            return switch (art) {
                case "PUNKT" -> { double[] u = utm(xz()[0][0], xz()[0][1]); yield "POINT (" + n2(u[0]) + " " + n2(u[1]) + ")"; }
                case "LINIE" -> "LINESTRING " + folge(xz(), false);
                case "FLAECHE" -> {
                    double[][] p = xz();
                    // in Ost/Nord ist die Drehung wegen z = −Nord gespiegelt: gegen den Uhrzeigersinn heißt hier Fläche < 0
                    double a = 0;
                    for (int i = 0; i < p.length; i++) { double[] u = p[i], v = p[(i + 1) % p.length]; a += u[0] * v[1] - v[0] * u[1]; }
                    if (a > 0) {
                        double[][] q = new double[p.length][];
                        for (int i = 0; i < p.length; i++) q[i] = p[p.length - 1 - i];
                        p = q;
                    }
                    yield "POLYGON (" + folge(p, true) + ")";
                }
                default -> {
                    StringBuilder b = new StringBuilder("MULTILINESTRING (");
                    for (int i = 0; i < teile.size(); i++) b.append(i > 0 ? ", " : "").append(folge(teile.get(i), false));
                    yield b.append(')').toString();
                }
            };
        }

        /** Aus WKT in UTM (wie {@code SDO_UTIL.TO_WKTGEOMETRY} es liefert) zurück in Szenenmeter. */
        public static Geom ausWkt(String wkt) {
            if (wkt == null || wkt.isBlank()) return null;
            String w = wkt.strip().toUpperCase(Locale.ROOT);
            String art = w.startsWith("POINT") ? "PUNKT" : w.startsWith("MULTILINESTRING") ? "MULTILINIE" : w.startsWith("POLYGON") ? "FLAECHE" : "LINIE";
            List<double[][]> teile = new ArrayList<>();
            int tiefe = 0, start = -1, innen = art.equals("MULTILINIE") || art.equals("FLAECHE") ? 2 : 1;
            for (int i = 0; i < w.length(); i++) {
                char c = w.charAt(i);
                if (c == '(') { if (++tiefe == innen) start = i + 1; }
                else if (c == ')') {
                    if (tiefe == innen && start >= 0) {
                        List<double[]> pts = new ArrayList<>();
                        for (String p : w.substring(start, i).split(",")) {
                            String[] v = p.strip().split("\\s+");
                            if (v.length >= 2) pts.add(Daten.xz(Double.parseDouble(v[0]), Double.parseDouble(v[1])));
                        }
                        if (art.equals("FLAECHE") && pts.size() > 3) pts.remove(pts.size() - 1);
                        teile.add(pts.toArray(new double[0][]));
                        start = -1;
                        if (art.equals("FLAECHE")) break;   // nur der Ring außen
                    }
                    tiefe--;
                }
            }
            return teile.isEmpty() ? null : new Geom(art, teile);
        }

        /** Länge aller Linien (m). */
        public double laenge() {
            double s = 0;
            for (double[][] p : teile) for (int i = 0; i + 1 < p.length; i++) s += Math.hypot(p[i + 1][0] - p[i][0], p[i + 1][1] - p[i][1]);
            return s;
        }

        /** Fläche (m², nach Gauß). */
        public double flaecheM2() {
            double[][] p = xz();
            double a = 0;
            for (int i = 0; i < p.length; i++) { double[] u = p[i], v = p[(i + 1) % p.length]; a += u[0] * v[1] - v[0] * u[1]; }
            return Math.abs(a) / 2;
        }

        public int punkte() { int n = 0; for (double[][] p : teile) n += p.length; return n; }
    }

    /** Ein Rechteck als Fläche (Szenenmeter). */
    public static Geom rechteck(double x0, double z0, double x1, double z1) {
        return Geom.flaeche(new double[][]{{x0, z0}, {x1, z0}, {x1, z1}, {x0, z1}});
    }

    /** Eine Linie mit höchstens einem Punkt je {@code abstand} m (erste und letzte bleiben). */
    public static double[][] ausduennen(double[][] p, double abstand) {
        if (p.length < 3) return p;
        List<double[]> l = new ArrayList<>();
        l.add(p[0]);
        double[] letzter = p[0];
        for (int i = 1; i < p.length - 1; i++) if (Math.hypot(p[i][0] - letzter[0], p[i][1] - letzter[1]) >= abstand) { l.add(p[i]); letzter = p[i]; }
        l.add(p[p.length - 1]);
        return l.toArray(new double[0][]);
    }

    // ================================================================== Stammdaten

    /** Eine Quelle (SY_QUELLE). */
    public record Quelle(String kurz, String titel, String url, String art, LocalDate abgerufen, String bemerkung) { }

    /** Ein Befund (SY_BEFUND) mit den Kurzzeichen seiner Quellen. */
    public record Befund(String thema, String gegenstand, String befund, String text, List<String> quellen) {
        public Befund { quellen = List.copyOf(quellen); }
    }

    /** Das Gelände (SY_GELAENDE); dateien: Datei → SHA-256 (oder leer). */
    public record Gelaende(String name, String stand, Geom kern, Geom fernfeld, double ursprungOst, double ursprungNord, double zelleKern, double zelle,
                           double zelleFern, Map<String, String> dateien, String quelle, String bemerkung) {
        public Gelaende { dateien = new LinkedHashMap<>(dateien); }
        public String dateienText() {
            StringBuilder b = new StringBuilder();
            for (Map.Entry<String, String> e : dateien.entrySet()) b.append(b.length() > 0 ? "; " : "").append(e.getKey()).append('=').append(e.getValue() == null ? "" : e.getValue());
            return b.toString();
        }
    }

    /** Eine Lage der Wetterregie (SY_SEEGANG). */
    public record Lage(String name, String schluessel, String phase, double wind, double richtung, double boeig, double oktas, double hoch, String wolke, double regen,
                       double sicht, double nebel, double nebelHoch, double hs, double tp, double seeRichtung, int gewitter, double temperatur, String beschreibung,
                       String herkunft) {
        @Override public String toString() { return name; }
    }

    /** Ein Sektor eines Leuchtfeuers (SY_SEKTOR). */
    public record Sektor(int nr, String farbe, double von, double bis, Double tragweite, String text) { }

    /** Ein Leuchtfeuer (SY_LEUCHTFEUER, F40 A) mit seinen Sektoren. */
    public record Leuchtfeuer(String name, String kennung, double feuerhoehe, double turmhoehe, double tragweite, Integer vonJahr, Integer bisJahr, boolean heuteAn,
                              double x, double z, String beschreibung, String quelle, List<Sektor> sektoren) {
        public Leuchtfeuer { sektoren = List.copyOf(sektoren); }
        @Override public String toString() { return name; }
    }

    /** Eine belegte Sturmflut (SY_STURMFLUT, F38 A); Scheitel in cm (Bezug NHN oder Pegelnull List), leer: nicht belegt. */
    public record Sturmflut(String name, LocalDate datum, Integer list, Integer hoernum, Integer westerland, String bezug, Double kliffverlustM, String kliffverlustText,
                            String folgen, String quelle) {
        @Override public String toString() { return name; }
    }

    // ================================================================== Chronik (F36, F37)

    /** Ein Stück Kante (SY_KLIFFKANTE): der Stand 2024 oder das Stück nach einem Abbruch; Linien in Szenenmetern. */
    public record Kantenstueck(long id, String stand, Long abbruch, double nordVon, double nordBis, Geom oberkante, Geom fuss, double wandMax, double oberkanteMax,
                               Long szeneMs) { }

    /**
     * Ein Abbruch (SY_ABBRUCH) mit seinem Stück Kante und den Findlingen: Ort, Bilanz, Kegel, Kehle und L_c, Pegel und
     * Auflauf, Rückgang; Bruchstelle {x, z}, Rückgang als Fläche zwischen alter und neuer Kante.
     */
    public record Abbruch(long id, long ms, double nord, double nordVon, double nordBis, double laenge, double volumen, double bloecke, double zerfallen,
                          double ausgespuelt, double kegel, double kehle, double lc, double pegel, double auflauf, double rueckgang, String sturmflut,
                          double[] bruchstelle, Geom rueckgangFlaeche, Kantenstueck kante, List<double[]> findlinge) {
        public Abbruch { findlinge = findlinge == null ? List.of() : List.copyOf(findlinge); }
        public String text() {
            return String.format(Locale.GERMAN, "%s · Abbruch bei N %,.0f: %,.0f m³, Kante %.1f m zurück%s", Daten.text(ms), nord, volumen, rueckgang,
                    sturmflut == null ? "" : " (" + sturmflut + ")");
        }
    }

    /** Ein Lauf einer Sturmflut in der App (SY_STURMFLUT_LAUF); id 0: neu. */
    public record SturmflutLauf(long id, String belegt, String name, long beginnMs, Long endeMs, double scheitel, Long scheitelMs, double windstauMax,
                                double auflaufMax, double stundenAmFuss, double kehleMax, int abbrueche) {
        public SturmflutLauf mitId(long n) {
            return new SturmflutLauf(n, belegt, name, beginnMs, endeMs, scheitel, scheitelMs, windstauMax, auflaufMax, stundenAmFuss, kehleMax, abbrueche);
        }
        public String text() {
            return String.format(Locale.GERMAN, "%s · %s: Scheitel NHN %+.2f m, %.1f h am Fuß, %d Abbrüche%s", Daten.text(beginnMs), name, scheitel, stundenAmFuss, abbrueche,
                    endeMs == null ? " (läuft)" : "");
        }
    }

    /** Eine Vorspülung (SY_VORSPUELUNG): belegt (Jahr, Menge, Strecke) oder aus der App (Fläche, Szenenzeit). */
    public record Vorspuelung(long id, String name, String art, Integer jahr, Long menge, Double nordVon, Double nordBis, String strecke, Geom flaeche, Long beginnMs,
                              Double hoeheFuss, Double breite, Double halbwertTage, String bemerkung, String quelle) {
        public String text() {
            return beginnMs != null ? String.format(Locale.GERMAN, "%s · Vorspülung %s: %,d m³", Daten.text(beginnMs), name, menge == null ? 0 : menge)
                    : (jahr == null ? "" : jahr + " · ") + name + (menge == null ? "" : String.format(Locale.GERMAN, ": %,d m³", menge));
        }
    }

    /** Zehn Minuten Serien der Nahbrandung (SY_SERIE). */
    public record Serie(long ms, double dauerS, int wellen, int serien, int groesste, double hs, double tp, double x, double z) { }

    /** Ein Lauf eines Regiebuchs (SY_REGIELAUF) mit dem Blatt als PNG (oder null). */
    public record Regielauf(String name, long beginnMs, Long endeMs, boolean vollstaendig, boolean kino, double bilderMittel, double gpuMs, double gewartetS,
                            int ausgewichen, int fotos, String qualitaet, byte[] blatt, String blattDatei) { }

    /** Ein Eintrag der Chronik für den Block CHRONIK: Art, Zeit, Text und wohin die Kamera springt. */
    public record Eintrag(String art, long ms, String text, double x, double z, long id) {
        @Override public String toString() { return text; }
    }

    // ================================================================== Regie und Kamera

    /** Ein Regiebuch als ganzer Text (Format von {@code scene.Regie.parse}). */
    public record Regiebuch(String name, String text, String beschreibung, String datei, String herkunft) {
        @Override public String toString() { return name; }
    }

    /** Ein Stützpunkt einer Kamerafahrt (SY_STUETZPUNKT); Szenenmeter und Grad. */
    public record Stuetzpunkt(int nr, double zeitS, double x, double y, double z, double gier, double nick) { }

    /** Eine Kamerafahrt (SY_FAHRT). */
    public record Fahrt(String name, String art, Double dauerS, Double laengeM, String beschreibung, String herkunft, List<Stuetzpunkt> punkte) {
        public Fahrt { punkte = List.copyOf(punkte); }
        @Override public String toString() { return name; }
    }

    /**
     * Eine Ansicht (SY_ANSICHT): FEST (Tasten 1–9), BLICK, VORLAGE oder GEMERKT (Taste V) mit Zustand und Vorschau (PNG
     * oder null). kamera = {x, y, z, Gier, Nick}; y NaN heißt: aus dem Gelände (augeUeberGrund).
     */
    public record Ansicht(long id, String name, Integer taste, String art, double[] kamera, Double oeffnung, Double augeUeberGrund, Long szeneMs, String zustand,
                          byte[] vorschau, String herkunft) {
        @Override public String toString() { return (taste != null && taste > 0 ? taste + " " : "") + name; }
    }

    /** Ein benanntes Preset eines Package (SY_PAKET_PRESET mit SY_PAKET_WERT). */
    public record PaketPreset(String paket, String klasse, String name, String beschreibung, String herkunft, Map<String, String> felder) {
        public PaketPreset { felder = new LinkedHashMap<>(felder); }
        @Override public String toString() { return name; }
    }

    // ================================================================== Betrieb

    /** Eine Sitzung für „Weiter wie zuletzt“ (mit Zustand und Vorschau). */
    public record Sitzung(long id, long beginnMs, Long endeMs, boolean sauber, String zustand, Long zustandMs, String regiebuch, Double regieSekunde, Long szeneMs,
                          String lage, Double pegelM, Integer abbrueche, boolean sturmflut, byte[] vorschau) {
        public String szeneText() { return szeneMs == null ? "" : Daten.text(szeneMs); }
    }

    /** Was das Startbild und der Block DATEN über den Stand zeigen. */
    public record Stand(int quellen, int befunde, int sturmfluten, int vorspuelungenBelegt, int kantenstuecke, int abbrueche, int findlinge, int sturmflutLaeufe,
                        int vorspuelungenApp, int serien, int regiebuecher, int regielaeufe, int ansichten, int gemerkt, int presets, int sitzungen, Sitzung letzte) { }
}
