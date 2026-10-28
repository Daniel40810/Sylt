package com.dan.sylt.wiring;

import com.dan.mudflat.Mudflat;
import com.dan.mudflat.WetField;
import com.dan.sylt.scene.KernKarte;
import com.dan.sylt.scene.Terrain;

/**
 * Die Nässe des Sands im Kern für {@code com.dan.mudflat} (Phase 3: „Strand aus mudflat.beach“). Ein {@link WetField}
 * zu 4 m über dem Kern: Sediment Sand, im Watt Mischwatt; Nässe nach der Höhe über dem Pegel [G]: unter dem Pegel 1, bis
 * 0,4 m darüber (Spülsaum, Grundwasser) noch fast nass, bis 1,6 m fallend auf die Restnässe des Sands (22 %). Neu
 * gerechnet, wenn der Pegel sich um mehr als 3 cm ändert.
 * <p>
 * Seit Phase 7a: der Auflauf der Brandung macht den Strand höher hinauf nass ({@code auflauf}, die Faustformel nach
 * Stockdon über die See draußen), und im Stück der {@link com.dan.sylt.scene.Nahbrandung} kommt die Nässe aus dem
 * Schwall selbst ({@link #ueberlagern}): Zunge für Zunge nass, und was der Rücklauf freigibt, trocknet wieder.
 */
public final class Strandnaesse {
    public static final double CELL = 4;
    public final WetField feld;
    private final float[] grund;
    private final boolean[] watt;
    private double pegel = Double.NaN, auflauf = Double.NaN, regenNass;
    /** Nässe nach Pegel und Auflauf, ohne das Stück der Nahbrandung. */
    private final float[] basis;
    /** Zellen, die das Stück zuletzt überschrieben hat (Rechteck i0, j0, i1, j1). */
    private int[] zuletzt;

    public Strandnaesse(Terrain t, KernKarte karte) {
        int n = (int) Math.round(Terrain.KN * Terrain.KCELL / CELL);
        Mudflat strand = Mudflat.beach();
        strand.lugwormPerM2 = 0;   // am Weststrand keine Wattwurmhaufen (nur im Watt im Osten, Phase 7a)
        feld = new WetField(strand, n, n, Terrain.KX0, Terrain.KZ0, CELL);
        grund = new float[n * n];
        basis = new float[n * n];
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
    public boolean update(double level) { return update(level, 0); }

    /**
     * Seit Phase 7a: dazu der Auflauf (m über dem Pegel, etwa R_2% nach Stockdon), bis zu dem der Strand nass ist; die
     * Stufen darüber verschieben sich mit ihm. Neu gerechnet, wenn Pegel oder Auflauf sich um mehr als 3 cm ändern.
     */
    public boolean update(double level, double r2) { return update(level, r2, regenNass); }

    /** Seit Phase 13: Nässe nach Regen 0..1 (der trockene Sand oben wird dunkel und fest); neu ab 3 % Änderung. */
    public boolean update(double level, double r2, double regen) {
        if (!Double.isNaN(pegel) && Math.abs(level - pegel) < 0.03 && Math.abs(r2 - auflauf) < 0.03 && Math.abs(regen - regenNass) < 0.03) return false;
        pegel = level;
        auflauf = r2;
        regenNass = regen;
        float[] w = basis;
        for (int k = 0; k < w.length; k++) {
            double ueber = grund[k] - level - (watt[k] ? 0 : Math.max(0, r2));
            float rest = WetField.residual(feld.sediment[k]);
            double v = ueber <= 0 ? 1 : ueber < 0.4 ? 1 - 0.15 * ueber / 0.4 : Math.max(rest, 0.85 - (0.85 - rest) * Math.min(1, (ueber - 0.4) / 1.2));
            w[k] = (float) Math.max(v, Math.min(0.75, regenNass * 0.75));   // seit Phase 13: Regen macht auch den oberen Strand nass
        }
        System.arraycopy(basis, 0, feld.wet, 0, basis.length);
        zuletzt = null;
        feld.version++;
        return true;
    }

    /**
     * Seit Phase 7a: die Nässe aus dem Schwall des Stücks der Nahbrandung an die Stelle der Basis setzen (über dem Pegel); die
     * Zellen des letzten Stücks bekommen vorher ihre Basis zurück. stueck null: nur zurücksetzen.
     */
    public void ueberlagern(com.dan.sylt.scene.Nahbrandung.Stueck s) {
        int n = feld.nx;
        float[] w = feld.wet;
        if (zuletzt != null)
            for (int j = zuletzt[1]; j <= zuletzt[3]; j++)
                for (int i = zuletzt[0]; i <= zuletzt[2]; i++) w[j * n + i] = basis[j * n + i];
        zuletzt = null;
        if (s != null) {
            double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
            for (double[] c : new double[][]{s.welt(s.schwall.xa, 0), s.welt(s.xEnde, 0), s.welt(s.schwall.xa, s.zug.width), s.welt(s.xEnde, s.zug.width)}) {
                minX = Math.min(minX, c[0]); maxX = Math.max(maxX, c[0]);
                minZ = Math.min(minZ, c[1]); maxZ = Math.max(maxZ, c[1]);
            }
            int i0 = Math.max(0, (int) Math.floor((minX - feld.x0) / CELL)), i1 = Math.min(n - 1, (int) Math.ceil((maxX - feld.x0) / CELL));
            int j0 = Math.max(0, (int) Math.floor((minZ - feld.z0) / CELL)), j1 = Math.min(feld.nz - 1, (int) Math.ceil((maxZ - feld.z0) / CELL));
            if (i0 <= i1 && j0 <= j1) {
                for (int j = j0; j <= j1; j++)
                    for (int i = i0; i <= i1; i++) {
                        double v = s.nass(feld.x0 + (i + 0.5) * CELL, feld.z0 + (j + 0.5) * CELL);
                        if (!Double.isNaN(v) && grund[j * n + i] > pegel) w[j * n + i] = (float) Math.max(WetField.residual(feld.sediment[j * n + i]), v);
                    }
                zuletzt = new int[]{i0, j0, i1, j1};
            }
        }
        feld.version++;
    }

    public double pegel() { return pegel; }
}
