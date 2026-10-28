package com.dan.sylt.tools;

import com.dan.sylt.gl.Quality;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.scene.Kante;
import com.dan.sylt.scene.Kliffabbruch;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Sturmflut;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

import java.io.File;
import java.util.Locale;

/**
 * Seit Phase 12: der Abbruch als Bildfolge auf der Grafikkarte, {@code docs/status/Abbruch_1.png} bis {@code _6.png}.
 * Die Sturmflut nach Xaver läuft im Zeitraffer bis Stunde {@link #STUNDE} (seit Phase 13 der Vormittag danach, 6.12.2013 11 Uhr,
 * damit man im Bild etwas sieht; die Kehle hat die Nacht mit dem Höhepunkt hinter sich), die Kehle wächst überall; dann
 * wird das Stück am Nordwert {@link #NORD} (oder {@code -Dnord=…}) gerechnet; die Kamera steht am Strand schräg davor, die
 * Physik läuft fest mit 1/120 s.
 * <ol>
 * <li>Hohlkehle vor dem Bruch, Brandung am Fuß</li>
 * <li>der Bruch (1,5 s)</li>
 * <li>Sturz (3 s)</li>
 * <li>Aufschlag, Zerfall, Staub (6 s)</li>
 * <li>Sturzkegel, wenn alles ruht</li>
 * <li>einen Tag später, nach zwei Fluten (24 h): Abtrag, neue Kante</li>
 * </ol>
 * <pre>ant abbruch [-Dnord=6090600]</pre>
 */
public final class Abbruch {
    private Abbruch() { }

    /**
     * Stelle und Stunde der Bildfolge: im Norden des Kliffs, wo der Fuß tief liegt; seit Phase 13 der Vormittag nach der
     * Nacht mit dem Höhepunkt (Stunde 23 = 6.12.2013 11 Uhr; bis Phase 12 Stunde 15 = 3 Uhr, da war im Bild nichts zu sehen).
     */
    public static final double NORD = 6_090_335, STUNDE = 23;

    public static void main(String[] a) throws Exception {
        int w = a.length > 0 ? Integer.parseInt(a[0]) : 1920, h = a.length > 1 ? Integer.parseInt(a[1]) : 1080;
        File dir = new File(a.length > 2 ? a[2] : "docs/status");
        String nordOpt = System.getProperty("nord", System.getenv("NORD"));
        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, w, h);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock();
        clock.lapse = 0;
        Renderer r = new Renderer(clock);
        r.setQuality(Quality.MITTEL);
        r.autoScale = false;
        r.hudFull = false;
        if ("aus".equals(System.getProperty("hud"))) r.showHud = false;   // seit Phase 19b: für das Video ohne HUD
        Kliffabbruch k = r.abbruch;
        if (k == null) { System.err.println("FEHLER: keine Kante (daten/kliff/kante.csv)"); System.exit(1); }
        k.manuell = true;
        k.asyncSolve = false;
        d.addGLEventListener(r);
        d.display();
        double nord = nordOpt == null || nordOpt.isBlank() ? NORD : Double.parseDouble(nordOpt);
        String so = System.getProperty("stunde");
        double stunde = so == null || so.isBlank() ? STUNDE : Double.parseDouble(so.replace(',', '.'));
        r.abbruchVorbereiten(stunde, nord);
        Kliffabbruch.Schnitt s = k.schnitt();
        if (s == null) { System.err.println("FEHLER: kein Stück Kliff gerechnet"); System.exit(1); }
        Kante kn = k.kante;
        int m = s.mitte;
        double fx = kn.fussX(m), fz = kn.z(m), hf = kn.hFuss[m], hk = kn.hKante[m];
        // Auge am Strand schräg vor dem Stück (Südwesten), Blick auf die Mitte der Wand
        double ex = fx - 48, ez = fz + 34, tx = (fx + kn.kanteX(m)) / 2, ty = (hf + hk) / 2;
        double yaw = (Math.toDegrees(Math.atan2(tx - ex, -(fz - ez))) + 360) % 360;
        double ey = Math.max(r.groundHeight(ex, ez), r.wiring.pegel) + 2.2;
        double pitch = -Math.toDegrees(Math.atan2(ty - ey, Math.hypot(tx - ex, fz - ez)));
        r.camera.place(ex, ey, ez, yaw, pitch);
        System.out.printf(Locale.GERMAN, "Abbruch bei N %,.0f (%s), Stunde %.1f, Pegel %+.2f m, Auflauf %.2f m%n", kn.nord[m],
                String.format(Locale.GERMAN, "Kehle %.1f m, L_c %.1f m", k.kehle[m], k.lc(m)), r.sturmflut.stunden(clock), r.wiring.pegel, r.brandung.auflaufR2());
        r.brandungJetzt();
        r.wetter.sky.updateNow();
        r.bautenJetzt();
        // seit Phase 13: Augenanpassung neu nach dem Zeitraffer (vorher stand sie noch auf dem Licht vor dem Sprung: Bild 1 schwarz)
        r.readapt();
        warm(r, d, 60);
        bild(r, d, new File(dir, "Abbruch_1.png"), "1 Hohlkehle");
        double t = 0;
        double[] zeiten = {1.5, 3, 6};
        for (int i = 0; i < zeiten.length; i++) {
            t = lauf(r, d, k, t, zeiten[i]);
            bild(r, d, new File(dir, "Abbruch_" + (i + 2) + ".png"), (i + 2) + " nach " + zeiten[i] + " s");
        }
        while (k.schnitt() != null && t < 120) t = lauf(r, d, k, t, t + 1);
        bild(r, d, new File(dir, "Abbruch_5.png"), "5 Sturzkegel nach " + Math.round(t) + " s");
        k.rechnen = false;   // die nächste Flut: nur Kehle und Abtrag, kein neues Stück
        r.vorspulen(r.sturmflut.stunden(clock) + 24, 5, false);
        r.brandungJetzt();
        r.wetter.sky.updateNow();
        r.readapt();
        warm(r, d, 60);
        bild(r, d, new File(dir, "Abbruch_6.png"), "6 einen Tag später (zwei Fluten)");
        for (String l : r.hudLines()) if (l.startsWith("Sturmflut")) System.out.println("  " + l);
        double[] b = k.bilanz();
        System.out.printf(Locale.GERMAN, "Bilanz %.0f m³ = %.0f steht + %.0f Blöcke + %.0f zerfallen + %.0f ausgespült · Kegel %.0f m³ · Abtrag %.0f m³ · Kante bis %.1f m zurück%n",
                b[0], b[1], b[2], b[3], b[4], k.vKegel, k.vAbtrag, k.rueckgang(s.r0, s.r1));
        if (r.error() != null) System.err.println("FEHLER: " + r.error());
        d.destroy();
        System.exit(r.error() == null ? 0 : 1);
    }

    /** Physik von t bis bis (s) fest mit 1/120 s, je 1/60 s ein Bild im Speicher (Körner, Brandung laufen mit). */
    static double lauf(Renderer r, GLOffscreenAutoDrawable d, Kliffabbruch k, double t, double bis) {
        while (t < bis - 1e-9) {
            k.physik(1 / 120.0);
            k.physik(1 / 120.0);
            r.step(1 / 60.0);
            t += 1 / 60.0;
            if (((int) Math.round(t * 60)) % 6 == 0) d.display();
        }
        return t;
    }

    static void warm(Renderer r, GLOffscreenAutoDrawable d, int n) {
        for (int i = 0; i < n; i++) { r.step(1 / 60.0); d.display(); }
    }

    static void bild(Renderer r, GLOffscreenAutoDrawable d, File png, String was) throws Exception {
        r.hudMeldung("Abbruch " + was);
        r.step(1 / 60.0);
        d.display();
        d.display();
        File dir = png.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        final java.awt.image.BufferedImage[] img = new java.awt.image.BufferedImage[1];
        d.invoke(true, x -> { img[0] = r.grab(x); return true; });
        javax.imageio.ImageIO.write(img[0], "png", png);
        System.out.println("Bild: " + png.getPath() + " · " + was + " · " + r.abbruch.describe());
    }
}
