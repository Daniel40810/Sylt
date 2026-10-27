package com.dan.sylt.scene;

import com.dan.ocean.ObstacleField;
import com.dan.surf.BreakerMesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Die Buhnen am Roten Kliff (seit Phase 7a): Pfahlbuhnen aus Holz, wie sie das Luftbild (DOP20 2024) im Kern zeigt –
 * eine Reihe bei Kliffende, 64 m sichtbar vom Strand bis an das Wasser, und ein Rest bei Kampen, 17 m sichtbar (der Rest
 * liegt im Sand). Seewärts laufen sie unter Wasser weiter [G: 50 und 40 m]. Weiter südlich bei Wenningstedt (RotesKliff5)
 * liegen sie außerhalb des Kerns und fehlen im Luftbild des Kerns; die Lage dort ist offen.
 * <p>
 * Gibt: die Pfähle als Netz (Format {@link BreakerMesh}, Holz in uMode 1) und als Abstandsraster für {@code ocean}
 * ({@link ObstacleField}: um jeden Pfahl der Brandungsring, zwischen den Pfählen bleibt Wasser).
 */
public final class Buhnen {
    /** Eine Buhne: landwärtiges Ende und seewärts sichtbares Ende (UTM), seewärtige Verlängerung unter Wasser (m) [G], Name. */
    public record Buhne(String name, double eLand, double nLand, double eSee, double nSee, double unterWasser) {
        /** Länge der ganzen Reihe (m). */
        public double laenge() { return Math.hypot(eSee - eLand, nSee - nLand) + unterWasser; }
    }

    /** Aus dem Luftbild gemessen (DOP20 2024, Kern zu 1 m). */
    public static final Buhne[] LISTE = {
            new Buhne("Buhne bei Kliffende", 457_150, 6_090_818, 457_087, 6_090_829, 50),
            new Buhne("Buhnenrest bei Kampen", 457_493, 6_091_505, 457_477, 6_091_511, 40)};
    /** Abstand der Pfähle (m), Kante (m), Höhe über dem Sand: am Strand, im Wasser (m) [G nach RotesKliff5]. */
    public static final double ABSTAND = 0.45, KANTE = 0.24, HOCH_STRAND = 0.35, HOCH_WASSER = 0.7;

    /** Ein Pfahl: Mitte (x, z), Fuß (m NHN), Kopf (m NHN). */
    public record Pfahl(double x, double z, double fuss, double kopf) { }

    public final List<Pfahl> pfaehle = new ArrayList<>();

    public Buhnen(Terrain t) {
        for (Buhne b : LISTE) {
            double[] a = Site.fromUtm(b.eLand(), b.nLand()), s = Site.fromUtm(b.eSee(), b.nSee());
            double dx = s[0] - a[0], dz = s[1] - a[1], l = Math.hypot(dx, dz);
            dx /= l;
            dz /= l;
            double len = b.laenge();
            for (double d = 0; d <= len; d += ABSTAND) {
                double x = a[0] + dx * d, z = a[1] + dz * d;
                double g = t.height(x, z);
                // über NHN +0,5 m am Strand niedrig (vom Sand zugeweht), zum Wasser höher [G]
                double frei = g > 0.5 ? HOCH_STRAND : HOCH_WASSER;
                pfaehle.add(new Pfahl(x, z, g - 0.6, g + frei));
            }
        }
    }

    /** Die Pfähle als Abstandsraster (Zelle 0,5 m) für die Brandungsringe in {@code ocean}. */
    public ObstacleField hindernisse() {
        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        List<double[]> c = new ArrayList<>();
        for (Pfahl p : pfaehle) {
            c.add(new double[]{p.x(), p.z(), KANTE * 0.6});
            minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
            minZ = Math.min(minZ, p.z()); maxZ = Math.max(maxZ, p.z());
        }
        double m = 12;
        return ObstacleField.of(c, minX - m, minZ - m, maxX - minX + 2 * m, maxZ - minZ + 2 * m, 0.5, 8);
    }

    /** Die Pfähle als Netz: je Pfahl ein Quader ohne Boden (Kopf und vier Seiten), Holz grau (Nässe 1 im Attribut). */
    public BreakerMesh netz() {
        BreakerMesh m = new BreakerMesh();
        int n = pfaehle.size();
        m.vertices = new float[n * 20 * BreakerMesh.STRIDE];
        m.indices = new int[n * 30];
        double h = KANTE / 2;
        int v = 0, ix = 0;
        double[][] seiten = {{0, 1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
        for (Pfahl p : pfaehle) {
            for (double[] nrm : seiten) {
                double[][] ecken = new double[4][];
                if (nrm[1] > 0) ecken = new double[][]{{-h, p.kopf(), -h}, {h, p.kopf(), -h}, {h, p.kopf(), h}, {-h, p.kopf(), h}};
                else if (nrm[0] != 0) { double s = nrm[0] * h; ecken = new double[][]{{s, p.fuss(), -h}, {s, p.fuss(), h}, {s, p.kopf(), h}, {s, p.kopf(), -h}}; }
                else { double s = nrm[2] * h; ecken = new double[][]{{-h, p.fuss(), s}, {h, p.fuss(), s}, {h, p.kopf(), s}, {-h, p.kopf(), s}}; }
                int base = v / BreakerMesh.STRIDE;
                for (double[] e : ecken) {
                    m.vertices[v] = (float) (p.x() + e[0]); m.vertices[v + 1] = (float) e[1]; m.vertices[v + 2] = (float) (p.z() + e[2]);
                    m.vertices[v + 3] = (float) nrm[0]; m.vertices[v + 4] = (float) nrm[1]; m.vertices[v + 5] = (float) nrm[2];
                    m.vertices[v + 8] = -1; m.vertices[v + 9] = (float) e[1];
                    v += BreakerMesh.STRIDE;
                }
                int[] q = {0, 1, 2, 0, 2, 3};
                for (int k : q) m.indices[ix++] = base + k;
            }
        }
        m.vertexCount = v / BreakerMesh.STRIDE;
        m.indexCount = ix;
        m.ns = m.vertexCount;
        m.ny = 1;
        return m;
    }

    /** „2 Buhnen (DOP20 2024), 409 Pfähle“ */
    public String describe() {
        return String.format(Locale.GERMAN, "%d Buhnen (Luftbild DOP20 2024: Kliffende, Kampen), %d Pfähle", LISTE.length, pfaehle.size());
    }
}
