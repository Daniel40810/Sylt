package com.dan.sylt.gl;

import com.dan.ocean.gl.OceanGl;
import com.dan.sky.Sky;
import com.dan.surf.BreakerMesh;
import com.dan.surf.gl.BreakerGl;
import com.dan.sylt.scene.Brandung;
import com.dan.sylt.scene.Nahbrandung;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

import java.nio.FloatBuffer;
import java.util.Locale;

/**
 * Die Brandung auf der Grafikkarte (seit Phase 7a): die gebackenen Texturen aus {@link Brandung} für {@code ocean}
 * (Brandung von außen und Strömung), das Netz der {@link Nahbrandung} mit {@link BreakerGl} (lokal zur Küste gedreht)
 * und die Pfähle der Buhnen. Licht vorbelichtet wie das Gelände.
 */
final class BrandungGl {
    final BreakerGl breakerGl = new BreakerGl();
    int surfTex, flowTex;
    private int texVersion = -1;
    boolean ok;
    String info = "";
    private BreakerMesh pfahlNetz;
    private boolean pfahlUp;
    /** Ob die Strömung (Rippströme) in der See gezeigt wird und wie stark sie trübt (DLRG-Blick). */
    volatile boolean stroemung = true, dlrg;

    void init(GL4 gl, Brandung b) {
        try {
            breakerGl.init(gl);
            pfahlNetz = b.buhnen.netz();
            ok = true;
            info = breakerGl.info();
        } catch (RuntimeException e) {
            info = "Brecher nicht verfügbar: " + e.getMessage();
            System.err.println("Sylt: " + info);
        }
    }

    /** Neue Texturen hochladen, wenn das Feld neu gebacken wurde. */
    void upload(GL4 gl, Brandung b) {
        float[] t = b.texture(), f = b.flowTexture();
        int v = b.textureVersion();
        if (t == null || v == texVersion) return;
        texVersion = v;
        int n = b.texN;
        if (surfTex == 0) {
            surfTex = make(gl, GL4.GL_RGBA32F, n);
            flowTex = make(gl, GL4.GL_RG32F, n);
        }
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 4);
        gl.glBindTexture(GL.GL_TEXTURE_2D, surfTex);
        gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, n, n, GL.GL_RGBA, GL.GL_FLOAT, FloatBuffer.wrap(t));
        gl.glBindTexture(GL.GL_TEXTURE_2D, flowTex);
        gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, n, n, GL4.GL_RG, GL.GL_FLOAT, FloatBuffer.wrap(f));
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    private static int make(GL4 gl, int format, int n) {
        int[] tex = new int[1];
        gl.glGenTextures(1, tex, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, tex[0]);
        gl.glTexStorage2D(GL.GL_TEXTURE_2D, 1, format, n, n);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        return tex[0];
    }

    /** Die Eingänge für {@code ocean}: Brandung von außen, Strömung und das Loch für das Netz. */
    void feed(OceanGl.Light ol, Brandung b, Nahbrandung nah, boolean netzSichtbar) {
        if (surfTex != 0) {
            ol.surfTexture = surfTex;
            ol.surfRect = new double[]{b.texX0, b.texZ0, b.texSize, 1};
            ol.surfPeriod = b.surf.tp;
            if (stroemung) {
                ol.flowTexture = flowTex;
                ol.flowRect = new double[]{b.texX0, b.texZ0, Brandung.TEX_ZELLE, 1};
                ol.siltStrength = dlrg ? 0.85 : 0.35;
                ol.siltColor = new double[]{0.10, 0.085, 0.055};
            }
        }
        Nahbrandung.Stueck s = nah.stueck();
        if (netzSichtbar && s != null && ok) {
            // Loch: vom Rand zur See (+12 m) bis zum Ende, längs bis 12 m vor den Rändern, weicher Rand 10 m
            double xa = 12, xe = s.xEnde, cx = (xa + xe) / 2, cz = s.zug.width / 2;
            double[] c = s.welt(cx, cz);
            ol.holeCenter = c;
            ol.holeAxis = new double[]{com.dan.sylt.scene.Kueste.SX, com.dan.sylt.scene.Kueste.SZ};
            ol.holeHalf = new double[]{(xe - xa) / 2, s.zug.width / 2 - 12, 10};
        } else ol.holeHalf = new double[]{0, 0, 10};
    }

    /** Licht für die Brecher und die Pfähle in der Szene (vorbelichtet), Sonne lokal zum Stück gedreht. */
    private static BreakerGl.Light licht(Sky.State st, double pre, double[] sun, Wetter wetter, boolean clouds, double[] sunFallback, double fogFallback) {
        BreakerGl.Light l = new BreakerGl.Light();
        l.sunDir = sun.clone();
        if (st != null) {
            l.sunColor = Wetter.scale(st.sunColor, pre);
            double[] amb = Wetter.scale(wetter.ambient(st, clouds), pre / Math.PI);
            l.skyHorizon = amb;
            l.skyTop = new double[]{amb[0] * 0.8, amb[1] * 0.9, amb[2] * 1.15};
            l.fogColor = Wetter.scale(st.skyIrradiance, pre / Math.PI);
            l.fogDistance = Wetter.fogDistance(st);
        } else {
            l.sunColor = sunFallback;
            l.fogDistance = fogFallback;
        }
        // Nordsee am Weststrand: grau-grün, im Flachen sandig [G nach RotesKliff3]
        l.waterDeep = new double[]{0.020, 0.060, 0.058};
        l.waterShallow = new double[]{0.07, 0.17, 0.14};
        l.sand = new double[]{0.50, 0.45, 0.36};
        l.display = 0;
        return l;
    }

    /** Netz der Nahbrandung und die Pfähle zeichnen (nach der See, mit Tiefentest). */
    void draw(GL4 gl, float[] vp, double[] eye, boolean reversedZ, Nahbrandung nah, boolean netzSichtbar, boolean pfaehle,
              Sky.State st, double pre, double[] sun, Wetter wetter, boolean clouds, double[] sunFallback, double fogFallback, double time) {
        if (!ok) return;
        BreakerGl.Light l = licht(st, pre, sun, wetter, clouds, sunFallback, fogFallback);
        if (pfaehle && pfahlNetz != null) {
            if (!pfahlUp) { breakerGl.uploadBed(gl, 1, pfahlNetz); pfahlUp = true; }
            BreakerGl.Light holz = licht(st, pre, sun, wetter, clouds, sunFallback, fogFallback);
            holz.sand = new double[]{0.20, 0.16, 0.11};
            BreakerGl.View v = new BreakerGl.View();
            v.viewProj = vp;
            v.eye = eye.clone();
            v.reversedZ = reversedZ;
            breakerGl.draw(gl, 1, v, holz, time, 99);
        }
        Nahbrandung.Stueck s = nah.stueck();
        if (!netzSichtbar || s == null) return;
        breakerGl.upload(gl, 0, s.netz);
        float[] m = s.modell();
        BreakerGl.View v = new BreakerGl.View();
        v.viewProj = Mat4.mul(vp, m);
        double[] le = s.lokal(eye[0], eye[2]);
        v.eye = new double[]{le[0], eye[1], le[1]};
        v.reversedZ = reversedZ;
        double[] ls = {sun[0] * com.dan.sylt.scene.Kueste.SX + sun[2] * com.dan.sylt.scene.Kueste.SZ, sun[1],
                sun[0] * com.dan.sylt.scene.Kueste.AX + sun[2] * com.dan.sylt.scene.Kueste.AZ};
        l.sunDir = ls;
        breakerGl.draw(gl, 0, v, l, time, 99);
    }

    String describe(Brandung b, Nahbrandung nah) {
        return String.format(Locale.GERMAN, "Brandung: %s · nah: %s", texVersion < 0 ? "Feld wird gerechnet" : String.format(Locale.GERMAN,
                "Textur %d² (Version %d)", b.texN, texVersion), nah.meldung);
    }

    void dispose(GL4 gl) {
        if (ok) breakerGl.dispose(gl);
        Gpu.deleteTextures(gl, surfTex, flowTex);
        surfTex = flowTex = 0;
    }
}
