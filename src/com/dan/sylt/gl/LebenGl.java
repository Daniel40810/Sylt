package com.dan.sylt.gl;

import com.dan.flock.gl.FlockGl;
import com.dan.flock.gl.WaderGl;
import com.dan.seal.gl.SealGl;
import com.dan.sylt.scene.Strandleben;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Seit Phase 13: Möwen ({@code FlockGl}), Vögel am Spülsaum ({@code WaderGl}, dieselben Netze wie im Watt) und der
 * seltene Seehund ({@code SealGl}) auf der Grafikkarte. Licht vorbelichtet wie das Gelände: Sonne und Mond (im Blitz der
 * Blitz), Himmel oben und am Horizont, Dunst. Nach dem Gelände, vor der See (wer im Schwall steht, steht mit den Beinen
 * im Wasser).
 */
final class LebenGl {
    final FlockGl flockGl = new FlockGl();
    final WaderGl waderGl = new WaderGl();
    final SealGl sealGl = new SealGl();
    boolean flockOk, waderOk, sealOk;
    String flockCheck = "";

    void init(GL4 gl, Consumer<String> err) {
        try { flockGl.init(gl); flockOk = true; } catch (RuntimeException e) { fail(err, "Die Möwen (com.dan.flock) lassen sich nicht starten", e); }
        try { waderGl.init(gl); waderOk = true; } catch (RuntimeException e) { fail(err, "Die Vögel am Spülsaum (com.dan.flock) lassen sich nicht starten", e); }
        try { sealGl.init(gl); sealOk = true; } catch (RuntimeException e) { fail(err, "Der Seehund (com.dan.seal) lässt sich nicht starten", e); }
    }

    private static void fail(Consumer<String> err, String what, RuntimeException e) {
        System.err.println("Sylt: " + what + ": " + e.getMessage());
        err.accept(what + ".\n" + e.getMessage());
    }

    /** Licht vorbelichtet (wie {@code Unwetter.licht}): {sunDir, sunColor, moonDir, moonColor, skyTop, skyHorizon, fog}. */
    void draw(GL4 gl, Strandleben l, float[] vp, double[] eye, boolean reversed, double[][] L, double fogDist, double shade, int frame) {
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDepthFunc(reversed ? GL.GL_GREATER : GL.GL_LESS);
        if (flockOk && l.anMoewen) {
            FlockGl.View v = new FlockGl.View();
            v.viewProj = vp;
            v.eye = eye.clone();
            v.reversedZ = reversed;
            FlockGl.Light fl = new FlockGl.Light();
            fl.sunDir = L[0]; fl.sunColor = L[1]; fl.moonDir = L[2]; fl.moonColor = L[3]; fl.skyTop = L[4]; fl.skyHorizon = L[5]; fl.fogDistance = fogDist;
            flockGl.draw(gl, l.moewen, v, fl);
            if (flockCheck.isEmpty() && frame > 95 && !l.moewen.gulls().isEmpty()) flockCheck = flockGl.selfCheck(gl, l.moewen);
        }
        if (waderOk && l.anStrand && !l.strand.groups().isEmpty()) {
            WaderGl.View v = new WaderGl.View();
            v.viewProj = vp;
            v.eye = eye.clone();
            WaderGl.Light wl = new WaderGl.Light();
            wl.sunDir = L[0]; wl.sunColor = L[1]; wl.moonDir = L[2]; wl.moonColor = L[3]; wl.skyTop = L[4]; wl.skyHorizon = L[5]; wl.fogDistance = fogDist;
            waderGl.draw(gl, l.strand.groups(), l.strand.month, v, wl);
        }
        if (sealOk && l.seehundDa()) {
            sealGl.update(gl, l.seehund);
            SealGl.View v = new SealGl.View();
            v.viewProj = vp;
            v.eye = eye.clone();
            SealGl.Light sl = new SealGl.Light();
            sl.sunDir = L[0]; sl.sunColor = L[1]; sl.moonDir = L[2]; sl.moonColor = L[3]; sl.skyTop = L[4]; sl.skyHorizon = L[5]; sl.fogDistance = fogDist;
            sl.sunShadow = shade;
            sealGl.draw(gl, v, sl);
        }
        gl.glDepthFunc(reversed ? GL.GL_GREATER : GL.GL_LESS);
        gl.glEnable(GL.GL_DEPTH_TEST);
    }

    String describe() {
        return String.format(Locale.GERMAN, "Tiere: Möwen %s · Spülsaum %s · Seehund %s%s", flockOk ? flockGl.info() : "fehlen",
                waderOk ? waderGl.info() : "fehlt", sealOk ? sealGl.info() : "fehlt", flockCheck.isEmpty() ? "" : " · " + flockCheck);
    }

    void dispose(GL4 gl) {
        if (flockOk) flockGl.dispose(gl);
        if (waderOk) waderGl.dispose(gl);
        if (sealOk) sealGl.dispose(gl);
    }
}
