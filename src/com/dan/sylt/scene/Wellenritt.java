package com.dan.sylt.scene;

import com.dan.surf.Surf;
import com.dan.surf.WaveTrain;

import java.util.Locale;
import java.util.function.DoubleBinaryOperator;

/**
 * Seit Phase 15: „Mit der Welle“, die Kür. Die Kamera reitet auf einer Welle der Nahbrandung (der höchsten, die 100 bis
 * 220 m vor der Wasserlinie noch nicht bricht) bis zum Auflauf:
 * <ol>
 * <li><b>Kamm</b>: 4 m hinter dem Kamm, über ihm, Blick zum Strand;</li>
 * <li><b>Hohlraum</b> (nur Sturzbrecher, während die Lippe fällt): vor dem Kamm unter der Lippe, Blick den Kamm entlang;</li>
 * <li><b>unter Wasser</b> („Die Welle von unten“): in der Rinne hinter dem Riff (oder wo die Welle in mehr als 0,8 m Wasser
 * bricht) taucht die Kamera vor ihr einen Meter unter den Spiegel und schaut zurück nach oben, 2,5 s lang läuft der Kamm
 * über sie hinweg (wie ein Surfer, der unter der Welle durchtaucht);</li>
 * <li><b>Walze</b>: hinter der Walze her zum Strand;</li>
 * <li><b>Schwall</b>: an der Spitze des Schwalls den Strand hinauf, bis er umkehrt.</li>
 * </ol>
 * Die Stellung gleitet zwischen den Abschnitten (nichts springt). Gestaltet [G].
 */
public final class Wellenritt {
    public enum Abschnitt {
        KAMM("auf dem Kamm"), HOHLRAUM("im Hohlraum"), UNTER("unter Wasser"), WALZE("mit der Walze"), SCHWALL("mit dem Schwall"), ENDE("zu Ende");
        public final String label;
        Abschnitt(String l) { label = l; }
    }

    private final Nahbrandung nah;
    private final DoubleBinaryOperator grund;
    private Nahbrandung.Stueck st;
    private int welle = -1, reihe;
    private double y, zeit, schwallSeit, tiefMax, unterSeit = -1, umkehr, tipMax = -1e9;
    private double[] glatt;
    private boolean sturz, getaucht;
    public volatile Abschnitt abschnitt = Abschnitt.KAMM;
    /** Die Abschnitte der laufenden (oder letzten) Fahrt, in der Folge, wie sie kamen. */
    public final java.util.List<Abschnitt> erlebt = new java.util.concurrent.CopyOnWriteArrayList<>();
    /** Neigung des Blicks (Grad, + nach unten) und ob das Auge unter Wasser darf. */
    public volatile double neigung = 10;
    public volatile boolean unterWasser;
    /** Der Wasserspiegel über dem Auge (m NHN; für das Bild unter Wasser). */
    public volatile double spiegel = Double.NaN;

    public Wellenritt(Nahbrandung nah, DoubleBinaryOperator grund) { this.nah = nah; this.grund = grund; }

    /** Eine Welle wählen: die größte der seewärtigen, die noch nicht bricht; y = Lage längs gegenüber dem Auge. Gibt false ohne Stück. */
    public boolean starten(double[] auge) {
        st = nah.stueck();
        if (st == null) return false;
        WaveTrain z = st.zug;
        double[] l = st.lokal(auge[0], auge[2]);
        y = Math.max(5, Math.min(z.width - 5, l[1]));
        reihe = Math.max(0, Math.min(z.rows - 1, (int) (y / z.width * z.rows)));
        WaveTrain.Wave best = null;
        for (WaveTrain.Wave w : z.waves) {
            // die höchste, die noch nicht bricht, 100 bis 220 m vor der Wasserlinie (so dauert die Fahrt rund eine Minute)
            if (w.broken(reihe) || w.x > st.xWasserlinie - 100 || w.x < st.xWasserlinie - 220) continue;
            if (best == null || w.hu > best.hu) best = w;
        }
        // sonst die nächste draußen, die noch nicht bricht (weiter draußen: die Fahrt dauert länger)
        if (best == null) for (WaveTrain.Wave w : z.waves) if (!w.broken(reihe) && w.x <= st.xWasserlinie - 100 && (best == null || w.x > best.x)) best = w;
        if (best == null) for (WaveTrain.Wave w : z.waves) if (!w.broken(reihe) && (best == null || w.x < best.x)) best = w;
        if (best == null) return false;
        welle = best.id;
        zeit = 0;
        unterSeit = -1;
        tipMax = -1e9;
        umkehr = 0;
        glatt = null;
        sturz = false;
        getaucht = false;
        tiefMax = 0;
        abschnitt = Abschnitt.KAMM;
        erlebt.clear();
        erlebt.add(abschnitt);
        return true;
    }

    public boolean laeuft() { return welle >= 0 && abschnitt != Abschnitt.ENDE; }

    /** Die Fahrt abbrechen (Schnitt). */
    public void halt() { if (welle >= 0 && abschnitt != Abschnitt.ENDE) { abschnitt = Abschnitt.ENDE; erlebt.add(abschnitt); } }

    WaveTrain.Wave finde() {
        if (st == null) return null;
        for (WaveTrain.Wave w : st.zug.waves) if (w.id == welle) return w;
        return null;
    }

    /** Je Bild (Echtzeit dt): Abschnitt weiter. */
    public void schritt(double dt) {
        if (!laeuft()) return;
        weiter(dt);
        if (erlebt.isEmpty() || erlebt.get(erlebt.size() - 1) != abschnitt) erlebt.add(abschnitt);
    }

    private void weiter(double dt) {
        zeit += dt;
        if (st != nah.stueck() || zeit > 150) { abschnitt = Abschnitt.ENDE; return; }
        WaveTrain z = st.zug;
        WaveTrain.Wave w = finde();
        if (abschnitt == Abschnitt.UNTER) {
            if (zeit - unterSeit > 2.5) abschnitt = Abschnitt.WALZE;
            return;
        }
        if (w == null || w.x > st.xWasserlinie - 8) {   // die Walze bis an den Strand, dann die Spitze des Schwalls
            if (abschnitt != Abschnitt.SCHWALL) { abschnitt = Abschnitt.SCHWALL; tipMax = -1e9; umkehr = 0; schwallSeit = zeit; }
            double tip = st.schwall.tipX(Math.max(0, Math.min(st.schwall.rows - 1, (int) (y / st.schwall.width * st.schwall.rows))));
            if (!Double.isNaN(tip)) {
                if (tip > tipMax + 0.05) { tipMax = tip; umkehr = 0; } else umkehr += dt;
            }
            if (umkehr > 1.5 || zeit - schwallSeit > 15 || zeit > 140) abschnitt = Abschnitt.ENDE;
            return;
        }
        if (w.type == Surf.Breaker.PLUNGING) sturz = true;
        double p = z.progress(w, reihe);
        // in der Rinne hinter dem Riff (die Tiefe nimmt nach mehr als 1,3 m wieder ab) taucht die Kamera unter der Welle durch
        double d = z.depth(w.x);
        tiefMax = Math.max(tiefMax, d);
        if (!getaucht && !w.broken(reihe) && tiefMax > 1.3 && d < tiefMax - 0.15) { getaucht = true; unterSeit = zeit; abschnitt = Abschnitt.UNTER; return; }
        if (!w.broken(reihe) || Double.isNaN(p)) abschnitt = Abschnitt.KAMM;
        else if (sturz && p < 1) abschnitt = Abschnitt.HOHLRAUM;
        else if (!getaucht && d > 0.8) { getaucht = true; unterSeit = zeit; abschnitt = Abschnitt.UNTER; }
        else abschnitt = Abschnitt.WALZE;
    }

    /** Stellung {x, y, z, Blickrichtung, 0, 0} für die Mitfahrt; dazu {@link #neigung} und {@link #unterWasser}. */
    public double[] pose() {
        if (st == null) return glatt != null ? glatt.clone() : new double[]{0, 10, 0, Kueste.SHOREWARD_DEG, 0, 0};
        WaveTrain z = st.zug;
        WaveTrain.Wave w = finde();
        double land = Kueste.SHOREWARD_DEG, x, h, yaw = land, pitch;
        boolean unter = false;
        switch (abschnitt) {
            case KAMM -> {
                double H = w == null ? 1 : z.heightRow(w, reihe), wx = w == null ? st.xWasserlinie : w.x;
                x = wx - 4;
                h = z.meanLevel(x) + 0.6 * H + 1.2;
                pitch = 20;
            }
            case HOHLRAUM -> {
                double H = w == null ? 1 : Math.max(z.heightRow(w, reihe), w.hb[reihe]);
                x = (w == null ? st.xWasserlinie : w.x) + 0.45 * H;
                h = z.meanLevel(x) + 0.35 * H;
                yaw = land - 70;
                pitch = -6;
            }
            case UNTER -> {
                // vor dem Kamm, der über das Auge läuft: zu Beginn 4 m davor, dann zieht die Welle darüber
                double vor = 3 - 5 * Math.min(1, (zeit - unterSeit) / 2.5);
                x = (w == null ? st.xWasserlinie : w.x) + vor;
                h = z.meanLevel(x) - 1.0;
                h = Math.max(h, st.zug.profile.bed(x) + 0.25);
                yaw = land + 180;
                pitch = -35;
                unter = true;
            }
            case WALZE -> {
                double H = w == null ? 0.5 : z.heightRow(w, reihe);
                x = (w == null ? st.schwall.boundaryX() : w.x) - 3;
                h = z.meanLevel(x) + 0.5 * H + 1.5;
                pitch = 14;
            }
            default -> {
                double tip = st.schwall.tipX(Math.max(0, Math.min(st.schwall.rows - 1, (int) (y / st.schwall.width * st.schwall.rows))));
                x = Double.isNaN(tip) ? st.xWasserlinie : tip + 0.5;
                double[] wp = st.welt(x, y);
                h = grund.applyAsDouble(wp[0], wp[1]) + 1.1;
                pitch = 16;
            }
        }
        double[] p = st.welt(x, y);
        double[] q = {p[0], h, p[1], yaw, 0, 0};
        neigung = pitch;
        unterWasser = unter;
        // der Spiegel über dem Auge: das Mittel, dazu der Kamm, wenn er über das Auge läuft (Glocke von 0,1 L) [G]
        double eta = 0;
        if (w != null) {
            double L = Math.max(8, z.tp * Math.sqrt(9.81 * Math.max(0.3, z.depth(x)))), d = (x - w.x) / (0.1 * L);
            eta = 0.7 * Math.max(z.heightRow(w, reihe), 0.3) * Math.exp(-d * d);
        }
        spiegel = z.meanLevel(x) + eta;
        if (glatt == null) glatt = q;
        else {
            double k = unter || abschnitt == Abschnitt.UNTER ? 0.25 : 0.12;
            for (int i = 0; i < 3; i++) glatt[i] += (q[i] - glatt[i]) * k;
            double d = ((q[3] - glatt[3] + 540) % 360) - 180;
            glatt[3] = (glatt[3] + d * (abschnitt == Abschnitt.UNTER ? 0.2 : 0.08) + 360) % 360;
        }
        return glatt.clone();
    }

    /** „Mit der Welle: im Hohlraum · 18 s“ */
    public String describe() {
        return welle < 0 ? "Mit der Welle: keine" : String.format(Locale.GERMAN, "Mit der Welle: %s · %.0f s%s", abschnitt.label, zeit, sturz ? " · Sturzbrecher" : "");
    }
}
