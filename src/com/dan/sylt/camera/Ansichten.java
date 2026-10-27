package com.dan.sylt.camera;

import com.dan.sylt.scene.Buehne;
import com.dan.sylt.scene.Site;

import java.util.List;
import java.util.Locale;

/**
 * Die neun Ansichten nach Namen auf den Tasten 1 bis 9 (Werkbuch, Schaustücke und Vorlagen). Seit Phase 1 auf der
 * Bühne, ab Phase 2 auf dem Gelände; die Lagen sind aus den Orten in {@link Site} und der Wasserlinie gesetzt [G], Phase 2
 * legt sie auf das gemessene Gelände:
 * <ol>
 * <li><b>Am Kliff</b> (RotesKliff1): unten am Strand, Blick die Wand entlang nach Nordnordost; abends glüht sie.</li>
 * <li><b>Treppe</b> (RotesKliff2): auf halber Höhe einer Strandtreppe, Blick nach Süden, das Kliff links, die See rechts.</li>
 * <li><b>Kliffkante</b> (RotesKliff3): oben an der Kante, Blick hinunter auf Strand und Brandungszone.</li>
 * <li><b>Spülsaum</b>: an der Wasserlinie, Auge 0,6 m, Blick nach Westnordwest in die anlaufende See.</li>
 * <li><b>Quermarkenfeuer</b> (Leuchturm3): 120 m südwestlich des Turms, Blick nach Nordost auf das Achteck.</li>
 * <li><b>Kampener Leuchtturm</b> (Kampen1–4): 260 m westlich, Blick nach Osten auf den Turm.</li>
 * <li><b>Uwe-Düne</b>: oben auf der Düne (52,5 m), Rundblick, zuerst nach Westen über die See.</li>
 * <li><b>Bohlenweg</b> (Leuchturm4, RotesKliff4): in den Dünen südöstlich des Quermarkenfeuers, Blick nach Nordwest.</li>
 * <li><b>Vogelauge</b>: 3 km draußen über der See, 600 m hoch, Blick nach Ostnordost auf die ganze Küste.</li>
 * </ol>
 */
public final class Ansichten {
    /**
     * Eine Ansicht: Taste, Name, Lage (x, z), Blickrichtung (Grad von Nord), Neigung (Grad nach unten, negativ nach
     * oben), Augenhöhe über Grund (m) und ab welcher Phase sie einen Ort hat.
     */
    public record Ansicht(int key, String name, double x, double z, double heading, double pitch, double eyeAboveGround, int phase, double fovy) {
        /** Mit der üblichen Öffnung (55°). */
        public Ansicht(int key, String name, double x, double z, double heading, double pitch, double eyeAboveGround, int phase) {
            this(key, name, x, z, heading, pitch, eyeAboveGround, phase, Camera.FOVY);
        }
        @Override public String toString() { return key > 0 ? key + " " + name : name; }
    }

    /**
     * Seit Phase 10: eine Vorlage (Leuchturm1–4, Kampen1–4) mit Ansicht, Tag, Uhrzeit, Bedeckung (Achtel, NaN: wie sie
     * ist) und ob das Quermarkenfeuer wie früher brennt (F19).
     */
    public record Vorlage(Ansicht ansicht, java.time.LocalDate datum, double uhr, double bedeckung, boolean frueher, String bild) {
        public String name() { return ansicht.name(); }
    }

    /** Ein Punkt s m landwärts der Wasserlinie (negativ: seewärts) am Nordwert n, als Szenenpunkt (x, z). */
    static double[] quer(double n, double s) { return Site.fromUtm(Site.wasserlinieE(n) + s, n); }

    private static Ansicht at(int key, String name, double[] xz, double heading, double pitch, double above) {
        return new Ansicht(key, name, xz[0], xz[1], heading, pitch, above, 1);
    }

    public static final List<Ansicht> ALLE = List.of(
            at(1, "Am Kliff", quer(6_089_300, 38), 18, -10, 1.7),
            at(2, "Treppe", quer(6_089_900, 66), 192, 8, 1.7),
            at(3, "Kliffkante", quer(6_089_500, 157), 258, 24, 1.7),   // Kante seit Phase 2 gemessen: 154 m landwärts (kante.csv)
            at(4, "Spülsaum", quer(6_089_000, 1), 292, 2, 0.6),
            at(5, "Quermarkenfeuer", new double[]{Site.QUERMARKE.x() - 85, Site.QUERMARKE.z() + 85}, 45, -6, 1.7),
            at(6, "Kampener Leuchtturm", new double[]{Site.LEUCHTTURM.x() - 260, Site.LEUCHTTURM.z()}, 90, -7, 1.7),
            at(7, "Uwe-Düne", new double[]{Site.UWE_DUENE.x(), Site.UWE_DUENE.z()}, 270, 6, 1.7),
            at(8, "Bohlenweg", new double[]{Site.QUERMARKE.x() + 160, Site.QUERMARKE.z() + 190}, 320, -2, 1.7),
            at(9, "Vogelauge", quer(6_089_200, -3000), 74, 11, 600));

    /** Seit Phase 9a: „Sandsturm am Strand“ – auf dem trockenen oberen Strand vor dem Kliff, Blick längs nach Nordnordost, der Westwind quer durchs Bild. */
    public static final Ansicht SANDSTURM = at(0, "Sandsturm am Strand", quer(6_089_300, 22), 25, -3, 1.6);

    /** Ein Punkt in Abstand d (m) und Richtung peilung (Grad von Nord) vom Ort o. */
    static double[] von(Site.Ort o, double peilung, double d) {
        double a = Math.toRadians(peilung);
        return new double[]{o.x() + Math.sin(a) * d, o.z() - Math.cos(a) * d};
    }

    /** Richtung (Grad von Nord) von p zum Ort o. */
    static double zu(double[] p, Site.Ort o) {
        return (Math.toDegrees(Math.atan2(o.x() - p[0], -(o.z() - p[1]))) + 360) % 360;
    }

    private static Ansicht blick(String name, double[] xz, Site.Ort ziel, double dHeading, double pitch, double above, double fovy) {
        return new Ansicht(0, name, xz[0], xz[1], (zu(xz, ziel) + dHeading + 360) % 360, pitch, above, 1, fovy);
    }

    /**
     * Seit Phase 10: die Vorlagen der Türme, je mit Tag und Stunde wie im Bild [G: Lage und Öffnung aus den Bildern
     * geschätzt, an der Vorschau (tools.BautenBild) nachgestellt]:
     * <ol>
     * <li><b>Leuchturm1</b>: Quermarkenfeuer nach Sonnenuntergang von der Düne im Norden, Blick nach Südsüdwest, die See rechts.</li>
     * <li><b>Leuchturm2</b>: in der Dämmerung aus dem Strandhafer (Südwest), die Laterne brennt (wie früher).</li>
     * <li><b>Leuchturm3</b>: vom Kiesweg im Süden, Bank am Weg, Sommerwolken.</li>
     * <li><b>Leuchturm4</b>: vom Bohlenweg im Südosten, bedeckt.</li>
     * <li><b>Kampen1</b>: Fernrohr von der Uwe-Düne nach Südost, das Watt dahinter, die Wärterhäuser, Galloways.</li>
     * <li><b>Kampen2</b>: Fernrohr aus den Dünen im Westnordwesten über Reetdächer.</li>
     * <li><b>Kampen3</b>: vom Golfplatz, Fernrohr, Haufenwolken.</li>
     * <li><b>Kampen4</b>: aus der Luft von Südwest, der Turm zwischen den Wärterhäusern, Weiden und Hügel.</li>
     * </ol>
     */
    public static final List<Vorlage> VORLAGEN = List.of(
            new Vorlage(blick("Leuchturm1", von(Site.QUERMARKE, 15, 24), Site.QUERMARKE, -7, -6, 1.5, 50), java.time.LocalDate.of(2026, 6, 21), 22.25, 5, false, "Leuchturm1.jpg"),
            new Vorlage(blick("Leuchturm2", von(Site.QUERMARKE, 235, 21), Site.QUERMARKE, -9, -9, 1.5, 45), java.time.LocalDate.of(2026, 9, 15), 20.25, 1, true, "Leuchturm2.jpg"),
            new Vorlage(blick("Leuchturm3", von(Site.QUERMARKE, 182, 32), Site.QUERMARKE, 0, -5, 1.6, 42), java.time.LocalDate.of(2026, 8, 10), 14.0, 3, false, "Leuchturm3.jpg"),
            new Vorlage(blick("Leuchturm4", new double[]{Site.QUERMARKE.x() + 48, Site.QUERMARKE.z() + 44}, Site.QUERMARKE, 0, -6, 1.6, 45), java.time.LocalDate.of(2026, 10, 20), 15.0, 8, false, "Leuchturm4.jpg"),
            new Vorlage(blick("Kampen1", new double[]{Site.UWE_DUENE.x(), Site.UWE_DUENE.z()}, Site.LEUCHTTURM, 0, 0.6, 1.7, 2.8), java.time.LocalDate.of(2026, 7, 25), 11.0, 0, false, "Kampen1.jpg"),
            new Vorlage(blick("Kampen2", von(Site.LEUCHTTURM, 320, 900), Site.LEUCHTTURM, 0, 0.3, 1.7, 5.7), java.time.LocalDate.of(2026, 7, 25), 16.0, 0, false, "Kampen2.jpg"),
            new Vorlage(blick("Kampen3", von(Site.LEUCHTTURM, 150, 800), Site.LEUCHTTURM, 0, -1.2, 1.6, 6.0), java.time.LocalDate.of(2026, 6, 10), 13.0, 4, false, "Kampen3.jpg"),
            new Vorlage(blick("Kampen4", von(Site.LEUCHTTURM, 235, 140), Site.LEUCHTTURM, 0, 36, 95, 40), java.time.LocalDate.of(2026, 6, 10), 15.0, 2, false, "Kampen4.jpg"));

    /** Die Vorlage nach Namen (Leuchturm1 … Kampen4), sonst null. */
    public static Vorlage vorlage(String name) {
        for (Vorlage v : VORLAGEN) if (v.name().equalsIgnoreCase(name.replace(" ", ""))) return v;
        return null;
    }

    /** Taste der Ansicht „Am Kliff“ (Schaustück 1, „Abend am Kliff“). */
    public static final int AM_KLIFF = 1;

    /** Ob die Ansicht schon einen Ort hat. */
    public static boolean ready(Ansicht a, int phase) { return a.phase() <= phase; }

    public static Ansicht byName(String name) {
        for (Ansicht a : ALLE) if (a.name().equalsIgnoreCase(name) || a.name().replace(" ", "").equalsIgnoreCase(name)) return a;
        // ohne Sonderzeichen (Umgebungsvariablen unter Windows): „Spuelsaum“, „UweDuene“ …
        String n = name.replace(" ", "").replace("-", "").toLowerCase(Locale.ROOT);
        for (Ansicht a : ALLE) {
            String k = a.name().replace(" ", "").replace("-", "").toLowerCase(Locale.ROOT).replace("ü", "ue").replace("ö", "oe").replace("ä", "ae").replace("ß", "ss");
            if (k.equals(n)) return a;
        }
        Vorlage v = vorlage(name);
        if (v != null) return v.ansicht();
        if (SANDSTURM.name().replace(" ", "").equalsIgnoreCase(name.replace(" ", ""))) return SANDSTURM;
        try { return byKey(Integer.parseInt(name.trim())); } catch (NumberFormatException e) { return null; }
    }

    public static Ansicht byKey(int key) {
        for (Ansicht a : ALLE) if (a.key() == key) return a;
        return null;
    }

    /** Stellt die Kamera auf die Ansicht über dem Grund {@code ground}; false, wenn sie in dieser Phase noch keinen Ort hat. */
    public static boolean apply(Ansicht a, Camera c, int phase, java.util.function.DoubleBinaryOperator ground) {
        if (!ready(a, phase)) return false;
        c.stopFahrt();
        c.stopMitfahrt();
        c.diving = false;
        double g = Math.max(ground.applyAsDouble(a.x(), a.z()), c.waterLevel);
        c.groundLevel = ground.applyAsDouble(a.x(), a.z());
        c.place(a.x(), g + a.eyeAboveGround(), a.z(), a.heading(), a.pitch());
        c.fovy = a.fovy();
        return true;
    }

    /** Wie oben, über der Bühne von Phase 1. */
    public static boolean apply(Ansicht a, Camera c, int phase) { return apply(a, c, phase, Buehne::height); }

    /** „1 Am Kliff (Auge 1,7 m über Grund, Blick 018°)“ */
    public static String describe(Ansicht a, int phase) {
        if (!ready(a, phase)) return a + " (folgt in Phase " + a.phase() + ")";
        return String.format(Locale.GERMAN, "%s (Auge %.1f m über Grund, Blick %03.0f°%s)", a, a.eyeAboveGround(), a.heading(),
                a.fovy() != Camera.FOVY ? String.format(Locale.GERMAN, ", Öffnung %.1f°", a.fovy()) : "");
    }

    private Ansichten() { }
}
