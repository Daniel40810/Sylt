package com.dan.sylt.ui;

import com.dan.fstyle.FTheme;

import java.awt.Color;

/**
 * Die Farben der Oberfläche aus FTheme (Bedienelemente in FTheme-Farben, die Szene hat ihren eigenen Look; aus
 * Brocken). Liefert das Thema keine dunklen Farben, nimmt die App den Abend am Roten Kliff, damit der Rand um die Szene
 * nicht hell aufleuchtet. Dazu die Farben des Werkbuchs: Nordsee-Grün, Kliffrot, Kaolinweiß, Sand, Strandhafer, Heide,
 * Strandkorbblau, Abendgold.
 */
public final class Theme {
    private Theme() { }

    public static final Color NACHT = new Color(14, 20, 22), SEE = new Color(125, 189, 179), KLIFF = new Color(224, 127, 72),
            KAOLIN = new Color(230, 221, 201), SAND = new Color(216, 195, 154), HAFER = new Color(178, 185, 140), HEIDE = new Color(184, 146, 182),
            KORB = new Color(121, 163, 214), GOLD = new Color(232, 178, 92), TURM = new Color(217, 221, 224);

    public static Color bg() { return pick(FTheme::getBackground, NACHT, Role.DARK); }
    public static Color surface() { return pick(FTheme::getSurface, new Color(21, 28, 31), Role.DARK); }
    public static Color ink() { return pick(FTheme::getText, new Color(233, 236, 234), Role.LIGHT); }
    public static Color muted() { return pick(FTheme::getTextMuted, new Color(147, 160, 162), Role.LIGHT); }
    public static Color accent() { return pick(FTheme::getAccent, SEE, Role.ANY); }
    public static Color status() { return new Color(9, 13, 14); }
    public static Color line() { return new Color(38, 50, 54); }

    private enum Role { DARK, LIGHT, ANY }

    private static Color pick(java.util.function.Function<FTheme, Color> f, Color fallback, Role role) {
        try {
            Color c = f.apply(FTheme.getInstance());
            if (c == null) return fallback;
            int lum = (c.getRed() * 3 + c.getGreen() * 6 + c.getBlue()) / 10;
            if (role == Role.DARK && lum > 70) return fallback;
            if (role == Role.LIGHT && lum < 120) return fallback;
            return c;
        } catch (RuntimeException | Error e) {
            return fallback;
        }
    }

    /** Die Farbleiste des Werkbuchs (See bis Abendgold) mit ihren Anteilen, für Startbild und Symbol. */
    public static final Color[] BAND = {new Color(47, 111, 106), new Color(143, 199, 189), new Color(243, 241, 234), new Color(230, 211, 168),
            new Color(217, 115, 58), new Color(236, 228, 210), new Color(169, 176, 131), new Color(167, 127, 165), new Color(62, 111, 168), GOLD};
    public static final double[] BAND_AT = {0, 0.16, 0.26, 0.32, 0.46, 0.60, 0.66, 0.76, 0.84, 0.92, 1};
}
