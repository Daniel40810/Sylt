package com.dan.sylt.gl;

import com.dan.beacon.Beacon;
import com.dan.beacon.gl.BeaconGl;
import com.dan.sky.Sky;
import com.dan.sylt.scene.Leuchtfeuer;
import com.jogamp.opengl.GL4;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Leuchtfeuer auf der Grafikkarte (seit Phase 10): je Feuer ein {@link BeaconGl} (Lichtverteilung als Rundumbild,
 * Strahl in verkleinerter Auflösung, additiv nach der Szene mit ihrer Tiefe), dazu die Lichtpunkte beider Feuer.
 * Vorbelichtung: die Szene rechnet in Einheiten von {@code com.dan.sky} (eine Einheit = {@link Sky#CD_PER_UNIT} cd/m²),
 * {@code com.dan.beacon} in cd/m² und lx; darum pre / CD_PER_UNIT.
 */
final class FeuerGl {
    final BeaconGl kampen = new BeaconGl(), quermarke = new BeaconGl();
    boolean ok;
    String info = "";

    void init(GL4 gl) {
        try {
            kampen.init(gl);
            quermarke.init(gl);
            ok = true;
        } catch (RuntimeException e) {
            info = "Leuchtfeuer nicht verfügbar: " + e.getMessage();
            System.err.println("Sylt: " + info);
        }
    }

    /** Rundumbilder nach dem Zustand der Feuer (vor dem Zeichnen der Bauten: die Laternen lesen die Linse). */
    void update(GL4 gl, Leuchtfeuer f) {
        if (!ok) return;
        kampen.update(gl, f.kampen.snapshot(), f.gridKampen);
        if (f.quermarkeFrueher) quermarke.update(gl, f.quermarke.snapshot(), f.gridQuermarke);
    }

    /** Strahlen und Lichtpunkte ins Farbziel (ohne Tiefe; die Tiefe der Szene wird gelesen). */
    void draw(GL4 gl, Leuchtfeuer f, float[] vp, float[] inv, double[] eye, boolean reversed, int w, int h, double fovy,
              int depthTex, int targetFbo, double pre, boolean jitter) {
        if (!ok || !f.an) return;
        BeaconGl.View v = new BeaconGl.View();
        v.viewProj = vp;
        v.invViewProj = inv;
        v.eye = eye.clone();
        v.reversedZ = reversed;
        v.width = w;
        v.height = h;
        v.fovyDeg = fovy;
        v.depthTexture = depthTex;
        v.targetFbo = targetFbo;
        v.preExposure = pre / Sky.CD_PER_UNIT;
        v.maxDistance = 40_000;
        v.strength = f.staerke;
        v.jitter = jitter;
        List<BeaconGl.Glare> l = new ArrayList<>();
        Beacon.Snapshot k = f.kampen.snapshot();
        if (f.strahl && k.lit()) kampen.drawBeam(gl, v); else kampen.beamOff();
        if (k.lit()) l.add(glare(k, eye, fovy, h, f.kampen.hazePerM));
        if (f.quermarkeFrueher) {
            Beacon.Snapshot q = f.quermarke.snapshot();
            if (f.strahl && q.lit()) quermarke.drawBeam(gl, v); else quermarke.beamOff();
            if (q.lit()) l.add(glare(q, eye, fovy, h, f.quermarke.hazePerM));
        }
        if (!l.isEmpty()) kampen.drawGlare(gl, v, l);
    }

    /** Lichtpunkt eines Feuers am Auge (wie BeaconScene: ab zwei Bildpunkten Linsengröße zeigt die Laterne das Licht selbst). */
    static BeaconGl.Glare glare(Beacon.Snapshot s, double[] e, double fovy, int h, double sigma) {
        double[] lux = s.illuminanceAt(e[0], e[1], e[2], sigma);
        double dl = Math.sqrt((e[0] - s.x()) * (e[0] - s.x()) + (e[1] - s.y()) * (e[1] - s.y()) + (e[2] - s.z()) * (e[2] - s.z()));
        double px = 2 * Math.tan(Math.toRadians(fovy) / 2) / Math.max(1, h) * dl;
        return new BeaconGl.Glare(s.x(), s.y(), s.z(), lux, Math.max(0, Math.min(1, (2 * px - 0.9) / 0.9)), 2.5);
    }

    void dispose(GL4 gl) {
        if (!ok) return;
        kampen.dispose(gl);
        quermarke.dispose(gl);
    }
}
