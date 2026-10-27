package com.dan.sylt.wiring;

import com.dan.ground.Site;
import com.dan.sylt.scene.KernKarte;
import com.dan.sylt.scene.Terrain;

/**
 * Wo im Biom „Sylt“ ({@code com.dan.ground.Biome.sylt()}, F13 A) was wächst, nach der Art am Ort (im Kern die Karte zu
 * 1 m aus dem Luftbild, Phase 2) [G]:
 * <ul>
 * <li>Strandhafer: Weißdüne, dichte Bulten und offener Sand (Feuchte 1 wählt den Strandhafer);</li>
 * <li>Düne: offener Sand mit wenigen Bulten;</li>
 * <li>Heide: Krähenbeere, Besenheide, Silbergras, Flechte (Feuchte 0);</li>
 * <li>Gebüsch: Kartoffelrose (Schicht 1) über Heide;</li>
 * <li>Wiese: lockeres Silbergras und Sandsegge;</li>
 * <li>Strand, nasser Sand, Watt, See, Weg, Dach, Siedlung, Kliff: nichts.</li>
 * </ul>
 * An steilen Hängen (Kliffwand, Abrisse) wächst nichts. Flecken aus Rauschen machen die Decke ungleich.
 */
public final class Bodendecke implements Site {
    private final Terrain terrain;
    private final KernKarte karte;

    public Bodendecke(Terrain terrain, KernKarte karte) {
        this.terrain = terrain;
        this.karte = karte;
    }

    @Override public float height(double x, double z) { return (float) terrain.height(x, z); }

    @Override public void cover(double x, double z, float[] o) {
        byte a = karte.at(terrain, x, z);
        java.util.Arrays.fill(o, 0, 5, 0f);
        double hx = terrain.height(x + 1, z) - terrain.height(x - 1, z), hz = terrain.height(x, z + 1) - terrain.height(x, z - 1);
        double steil = Math.hypot(hx, hz) / 2;
        if (steil > 1.0) return;
        float f = (float) (0.7 + 0.6 * wert(x / 7, z / 7)), g = (float) wert(x / 23 + 5, z / 23 + 9);
        switch (a) {
            case Terrain.STRANDHAFER -> { o[0] = 0.85f * f; o[3] = 0.6f; o[4] = 1f; o[2] = 0.02f; }
            case Terrain.DUENE -> { o[0] = 0.25f * f; o[3] = 0.85f; o[4] = 1f; }
            case Terrain.HEIDE -> { o[0] = Math.min(1, 0.95f * f); o[3] = 0.35f + 0.3f * g; o[4] = 0.1f * g; o[2] = 0.03f; }
            case Terrain.GEBUESCH -> { o[0] = 0.6f * f; o[1] = 0.9f; o[3] = 0.2f; o[4] = 0.3f; }
            case Terrain.WIESE -> { o[0] = 0.55f * f; o[3] = 0.2f; o[4] = 0.4f; }
            default -> { }
        }
        if (steil > 0.6) for (int i = 0; i < 4; i++) o[i] *= (float) ((1.0 - steil) / 0.4);
    }

    /** Wertrauschen 0..1 (wie Buehne, eigener Startwert). */
    static double wert(double x, double y) {
        long ix = (long) Math.floor(x), iy = (long) Math.floor(y);
        double fx = x - ix, fy = y - iy;
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);
        double a = hash(ix, iy), b = hash(ix + 1, iy), c = hash(ix, iy + 1), d = hash(ix + 1, iy + 1);
        return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy;
    }

    static double hash(long x, long y) {
        long h = 1913 * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }
}
