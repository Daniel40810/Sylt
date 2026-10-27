package com.dan.sylt.gl;

import com.dan.ocean.Ocean;
import com.dan.ocean.gl.OceanGl;
import com.dan.sky.Sky;
import com.dan.sylt.scene.Terrain;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

import java.util.ArrayDeque;
import java.util.Locale;

/**
 * Phase 3: die offene See aus {@code com.dan.ocean} (aus Rungholt): Windsee aus dem Wind der Verdrahtung, Dünung
 * dieselbe, die {@code surf} draußen annimmt (H_s 1 m, T_p 8 s aus 285° im Sommer), Pegel aus der Verdrahtung. Der Grund ist
 * das Hauptraster (Höhentextur des Geländes, 10 m): zum Ufer hin werden die Wellen flacher, im seichten Wasser scheint der
 * Sand durch, am Strand liegt ein Schaumsaum; außerhalb des Hauptrasters offene See (−20 m). Seit Phase 7a zeichnet die
 * See die Brandung aus dem Wellenfeld von {@code surf} (Walzen, Schaumteppich, Set-up, Rippströme) und lässt nahe am Auge
 * ein Loch, in dem das Netz der Brecher liegt; um die Pfähle der Buhnen legt sie Brandungsringe. Senken im Land, die nicht
 * mit der See verbunden sind, blendet eine Maske aus ({@link #binnen}). Brandung, Auflauf und Schwall kommen mit
 * {@code surf} in den Phasen 5 bis 7. Startet das Meer nicht (kein Compute), bleibt der Platzhalter aus Phase 1.
 */
public final class Meer {
    public final Ocean ocean = Ocean.beaufort(4);
    final OceanGl oceanGl = new OceanGl();
    boolean ok;
    String check = "";
    int maskTex;
    /** Grund außerhalb des Hauptrasters (m NHN) [G]. */
    public static final double DRAUSSEN = -20;
    /** Höhe, bis zu der eine Zelle mit der See verbunden sein kann (m NHN): Watt und Strand liegen darunter. */
    public static final double VERBUNDEN_BIS = 1.5;

    Meer() { ocean.seed = 2026; }

    void init(GL4 gl, Terrain t, boolean compute, java.util.function.Consumer<String> err) {
        maskTex = maskTexture(gl, binnen(t));
        if (!compute) {
            System.err.println("Sylt: Meer braucht Compute-Shader, es bleibt der Platzhalter");
            return;
        }
        try {
            oceanGl.init(gl);
            ok = true;
            System.out.println("Sylt: Meer " + oceanGl.info());
        } catch (RuntimeException e) {
            System.err.println("Sylt: Meer nicht verfügbar: " + e.getMessage());
            err.accept("Das Meer (com.dan.ocean) lässt sich nicht starten, es bleibt der Platzhalter aus Phase 1.\n" + e.getMessage());
        }
    }

    /**
     * Maske der Senken im Land (1 = nicht mit der See verbunden): von den Rändern des Hauptrasters aus über alle Zellen
     * unter {@link #VERBUNDEN_BIS} m geflutet; was dabei nicht erreicht wird und tiefer liegt, ist Binnenland.
     */
    public static byte[] binnen(Terrain t) {
        int n = Terrain.N;
        boolean[] see = new boolean[n * n];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++)
            for (int k : new int[]{i, (n - 1) * n + i, i * n, i * n + n - 1})
                if (!see[k] && t.at(k % n, k / n) < VERBUNDEN_BIS) { see[k] = true; q.add(k); }
        while (!q.isEmpty()) {
            int k = q.poll(), i = k % n, j = k / n;
            int[][] nb = {{i - 1, j}, {i + 1, j}, {i, j - 1}, {i, j + 1}};
            for (int[] c : nb) {
                if (c[0] < 0 || c[1] < 0 || c[0] >= n || c[1] >= n) continue;
                int m = c[1] * n + c[0];
                if (see[m] || t.at(c[0], c[1]) >= VERBUNDEN_BIS) continue;
                see[m] = true;
                q.add(m);
            }
        }
        byte[] mask = new byte[n * n];
        for (int k = 0; k < mask.length; k++) mask[k] = (byte) (see[k] ? 0 : 255);
        return mask;
    }

    static int maskTexture(GL4 gl, byte[] m) {
        int n = Terrain.N;
        int[] tex = new int[1];
        gl.glGenTextures(1, tex, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, tex[0]);
        gl.glTexStorage2D(GL.GL_TEXTURE_2D, 1, GL4.GL_R8, n, n);
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, n, n, GL4.GL_RED, GL.GL_UNSIGNED_BYTE, java.nio.ByteBuffer.wrap(m));
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 4);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        return tex[0];
    }

    /**
     * Die See zeichnen (nach dem Gelände, mit Tiefentest): Licht aus dem Himmelszustand (vorbelichtet) oder dem
     * Platzhalter, Spiegelung aus dem Rundumbild, Wolkenschatten, Grund aus der Höhentextur.
     */
    void draw(GL4 gl, float[] vp, double[] eye, boolean reversedZ, Sky.State st, double pre, int panoTex, int cloudTex, double[] cloudRect,
              int bedTex, double[] sun, double[] sunFallback, double fogFallback, java.util.function.Consumer<OceanGl.Light> extra) {
        oceanGl.update(gl, ocean);
        OceanGl.View ov = new OceanGl.View();
        ov.viewProj = vp;
        ov.eye = eye.clone();
        ov.reversedZ = reversedZ;
        OceanGl.Light ol = new OceanGl.Light();
        ol.sunDir = sun;
        ol.skyTexture = panoTex;
        if (st != null) {
            ol.sunColor = Wetter.scale(st.sunColor, pre);
            ol.moonDir = st.moonDir;
            ol.moonColor = Wetter.scale(st.moonColor, pre);
            ol.fogDistance = Wetter.fogDistance(st);
            if (cloudTex != 0) {
                ol.shadowTexture = cloudTex;
                ol.shadowRect = cloudRect;
            }
        } else {
            ol.sunColor = sunFallback;
            ol.fogDistance = fogFallback;
        }
        ol.bedTexture = bedTex;
        ol.bedRect = new double[]{Terrain.X0, Terrain.Z0, Terrain.N * Terrain.CELL};
        ol.bedScale = Terrain.HMAX - Terrain.HMIN;
        ol.bedOffset = Terrain.HMIN;
        ol.bedOutside = DRAUSSEN;
        ol.bedTurbidity = 0.9;
        ol.bedColor = new double[]{0.24, 0.20, 0.14};
        ol.maskTexture = maskTex;
        ol.maskRect = new double[]{Terrain.X0, Terrain.Z0, Terrain.N * Terrain.CELL};
        ol.maskMode = -1;
        ol.tileBreak = 0.6;
        // seit Phase 7a: die Brandung von außen (Wellenfeld aus surf), Strömung und das Loch für das Netz
        if (extra != null) extra.accept(ol);
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
        oceanGl.draw(gl, ocean, ov, ol);
    }

    String describe() {
        return ok ? String.format(Locale.GERMAN, "See (ocean) %s · Wind %.1f m/s aus %03.0f° · Dünung %.1f m, %.0f s aus %03.0f° · Pegel %+.2f m%s",
                oceanGl.info(), ocean.windSpeed, ocean.windFromDeg, ocean.swellHeight, ocean.swellPeriod, ocean.swellFromDeg, ocean.waterLevel,
                check.isEmpty() ? "" : " · " + check) : "!See: com.dan.ocean nicht gestartet, Platzhalter aus Phase 1";
    }

    void dispose(GL4 gl) {
        if (ok) oceanGl.dispose(gl);
        Gpu.deleteTextures(gl, maskTex);
    }
}
