package com.dan.sylt.scene;

import com.dan.surf.WaveTrain;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Seit Phase 15 („Die Serie“ aus dem Ideenspeicher): Die Brandungsschwebung bringt die Wellen in Gruppen; alle paar
 * Minuten kommen größere. Hier gezählt an den Wellen der Nahbrandung, wenn sie ins Stück kommen: groß ist eine Welle mit
 * mehr als dem 1,25-fachen der mittleren Höhe der letzten 30 Wellen; zwei große hintereinander sind eine Serie (eine
 * kleinere dazwischen unterbricht sie nicht). Für das HUD und das Regiebuch „Mit der Welle“ ({@code warte serie}).
 */
public final class Serie {
    public static final double GROSS = 1.25;
    private final ArrayDeque<Double> letzte = new ArrayDeque<>();
    private final Set<Integer> gesehen = new HashSet<>();
    private WaveTrain zug;
    private int lauf, kleine;
    private double zeit, letzteSerie = Double.NaN, abstandSumme;
    /** Gezählte Serien, große Wellen in der laufenden Serie, größte Serie, Wellen gesehen. */
    public volatile int serien, jetzt, groesste, wellen;
    /** Ob gerade eine Serie läuft (mindestens zwei große Wellen hintereinander). */
    public volatile boolean laeuft;

    /** Je Bild (Echtzeit dt) mit dem Wellenzug des Stücks (neues Stück: neu zählen). */
    public void schritt(double dt, WaveTrain z) {
        zeit += dt;
        if (z == null) return;
        if (z != zug) { zug = z; gesehen.clear(); }
        for (WaveTrain.Wave w : z.waves) {
            if (!gesehen.add(w.id)) continue;
            wellen++;
            double mittel = mittel();
            boolean gross = letzte.size() >= 8 && w.hu > GROSS * mittel;
            letzte.addLast(w.hu);
            if (letzte.size() > 30) letzte.removeFirst();
            if (gross) {
                lauf++;
                kleine = 0;
                if (lauf == 2) {
                    serien++;
                    if (!Double.isNaN(letzteSerie)) abstandSumme += zeit - letzteSerie;
                    letzteSerie = zeit;
                }
            } else if (++kleine > 1) lauf = 0;
            jetzt = lauf;
            laeuft = lauf >= 2;
            groesste = Math.max(groesste, lauf);
        }
        if (gesehen.size() > 400) gesehen.clear();
    }

    double mittel() {
        if (letzte.isEmpty()) return 0;
        double s = 0;
        for (double h : letzte) s += h;
        return s / letzte.size();
    }

    /** „Serie: 3 große Wellen hintereinander · 5 Serien in 9 min, im Mittel alle 1,8 min, größte 4“ */
    public String describe() {
        if (wellen == 0) return "Serie: noch keine Welle";
        String jetztT = laeuft ? String.format(Locale.GERMAN, "läuft, %d große Wellen hintereinander", jetzt) : "ruhig";
        double ab = serien > 1 ? abstandSumme / (serien - 1) / 60 : Double.NaN;
        return String.format(Locale.GERMAN, "Serie: %s · %d Serien aus %d Wellen%s, größte %d", jetztT, serien, wellen,
                Double.isNaN(ab) ? "" : String.format(Locale.GERMAN, ", im Mittel alle %.1f min", ab), groesste);
    }
}
