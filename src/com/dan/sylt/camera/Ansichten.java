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
    public record Ansicht(int key, String name, double x, double z, double heading, double pitch, double eyeAboveGround, int phase) {
        @Override public String toString() { return key + " " + name; }
    }

    /** Ein Punkt s m landwärts der Wasserlinie (negativ: seewärts) am Nordwert n, als Szenenpunkt (x, z). */
    static double[] quer(double n, double s) { return Site.fromUtm(Site.wasserlinieE(n) + s, n); }

    private static Ansicht at(int key, String name, double[] xz, double heading, double pitch, double above) {
        return new Ansicht(key, name, xz[0], xz[1], heading, pitch, above, 1);
    }

    public static final List<Ansicht> ALLE = List.of(
            at(1, "Am Kliff", quer(6_089_300, 38), 18, -10, 1.7),
            at(2, "Treppe", quer(6_089_900, 66), 192, 8, 1.7),
            at(3, "Kliffkante", quer(6_089_500, 74), 258, 24, 1.7),
            at(4, "Spülsaum", quer(6_089_000, 1), 292, 2, 0.6),
            at(5, "Quermarkenfeuer", new double[]{Site.QUERMARKE.x() - 85, Site.QUERMARKE.z() + 85}, 45, -6, 1.7),
            at(6, "Kampener Leuchtturm", new double[]{Site.LEUCHTTURM.x() - 260, Site.LEUCHTTURM.z()}, 90, -7, 1.7),
            at(7, "Uwe-Düne", new double[]{Site.UWE_DUENE.x(), Site.UWE_DUENE.z()}, 270, 6, 1.7),
            at(8, "Bohlenweg", new double[]{Site.QUERMARKE.x() + 160, Site.QUERMARKE.z() + 190}, 320, -2, 1.7),
            at(9, "Vogelauge", quer(6_089_200, -3000), 74, 11, 600));

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
        return true;
    }

    /** Wie oben, über der Bühne von Phase 1. */
    public static boolean apply(Ansicht a, Camera c, int phase) { return apply(a, c, phase, Buehne::height); }

    /** „1 Am Kliff (Auge 1,7 m über Grund, Blick 018°)“ */
    public static String describe(Ansicht a, int phase) {
        if (!ready(a, phase)) return a + " (folgt in Phase " + a.phase() + ")";
        return String.format(Locale.GERMAN, "%s (Auge %.1f m über Grund, Blick %03.0f°)", a, a.eyeAboveGround(), a.heading());
    }

    private Ansichten() { }
}
