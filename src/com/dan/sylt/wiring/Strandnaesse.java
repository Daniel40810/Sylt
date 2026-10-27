package com.dan.sylt.wiring;

import com.dan.mudflat.Mudflat;
import com.dan.mudflat.WetField;
import com.dan.sylt.scene.KernKarte;
import com.dan.sylt.scene.Terrain;

/**
 * Die Nässe des Sands im Kern für {@code com.dan.mudflat} (Phase 3: „Strand aus mudflat.beach“). Ein {@link WetField}
 * zu 4 m über dem Kern: Sediment Sand, im Watt Mischwatt; Nässe nach der Höhe über dem Pegel [G bis Phase 7, dann trocknet
 * der Sand nach dem Auflauf]: unter dem Pegel 1, bis 0,4 m darüber (Spülsaum, Grundwasser) noch fast nass, bis 1,6 m
 * fallend auf die Restnässe des Sands (22 %). Neu gerechnet, wenn der Pegel sich um mehr als 3 cm ändert.
 */
public final class Strandnaesse {
    public static final double CELL = 4;
    public final WetField feld;
    private final float[] grund;
    private final boolean[] watt;
    private double pegel = Double.NaN;

    public Strandnaesse(Terrain t, KernKarte karte) {
        int n = (int) Math.round(Terrain.KN * Terrain.KCELL / CELL);
        Mudflat strand = Mudflat.beach();
        strand.lugwormPerM2 = 0;   // am Weststrand keine Wattwurmhaufen (nur im Watt im Osten, Phase 7a)
        feld = new WetField(strand, n, n, Terrain.KX0, Terrain.KZ0, CELL);
        grund = new float[n * n];
        watt = new boolean[n * n];
        for (int j = 0; j < n; j++)
            for (int i = 0; i < n; i++) {
                double x = Terrain.KX0 + (i + 0.5) * CELL, z = Terrain.KZ0 + (j + 0.5) * CELL;
                grund[j * n + i] = (float) t.roh(x, z);
                byte a = karte.at(t, x, z);
                watt[j * n + i] = a == Terrain.WATT;
                feld.sediment[j * n + i] = watt[j * n + i] ? 0.5f : 0f;
            }
    }

    /** Nässe neu, wenn der Pegel sich geändert hat; true, wenn neu gerechnet. */
    public boolean update(double level) {
        if (!Double.isNaN(pegel) && Math.abs(level - pegel) < 0.03) return false;
        pegel = level;
        float[] w = feld.wet;
        for (int k = 0; k < w.length; k++) {
            double ueber = grund[k] - level;
            float rest = WetField.residual(feld.sediment[k]);
            double v = ueber <= 0 ? 1 : ueber < 0.4 ? 1 - 0.15 * ueber / 0.4 : Math.max(rest, 0.85 - (0.85 - rest) * Math.min(1, (ueber - 0.4) / 1.2));
            w[k] = (float) v;
        }
        feld.version++;
        return true;
    }

    public double pegel() { return pegel; }
}
