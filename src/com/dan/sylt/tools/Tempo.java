package com.dan.sylt.tools;

import com.dan.sylt.camera.Ansichten;
import com.dan.sylt.gl.Quality;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.scene.SceneClock;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

import java.io.File;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Tempo-Probe und Standbild ohne Fenster (aus Brocken, entschlackt). Rechnet die Szene unsichtbar in fester Größe,
 * misst die Wanduhr je Bild bis glFinish und die Zeit jedes Durchgangs auf der Grafikkarte und schreibt auf Wunsch ein
 * PNG. So lässt sich jede Phase vorher und nachher vergleichen und im Leistungsbudget (Werkbuch, Abschnitt 13)
 * eintragen.
 * <pre>
 * Tempo [bilder] [breite] [höhe] [png|-] [NIEDRIG|MITTEL|HOCH|ULTRA] [uhrzeit] [datum JJJJ-MM-TT]
 * Tempo 120 1920 1080 docs/status/Status_Phase2.png MITTEL 20.5 2026-07-20
 * </pre>
 * Schalter (als -D… aus Ant oder als Umgebungsvariable in Großbuchstaben): {@code ansicht} = Name oder Taste einer
 * Ansicht (AmKliff, Treppe, Kliffkante, Spuelsaum, Quermarkenfeuer, KampenerLeuchtturm, UweDuene, Bohlenweg, Vogelauge),
 * {@code kamera} = x,z,Richtung,Neigung,Höhe über Grund (frei stehend), {@code hud} = kurz|voll|aus,
 * {@code marken} = an|aus (seit Phase 3 aus), {@code meer} = aus, {@code pegel} = m über NHN (von Hand), seit Phase 3
 * {@code wolken} = aus, {@code boden} = aus, {@code bedeckung} = Achtel, seit Phase 7a {@code see} = H_s,T_p,Richtung,
 * seit Phase 9a {@code sandsturm} = an und {@code wind} = m/s,Richtung, seit Phase 10 {@code vorlage} = Leuchturm1 … Kampen4
 * (Ansicht, Öffnung, Tag, Stunde und Bedeckung der Vorlage; Uhrzeit und Datum der Befehlszeile gelten dann nicht) und
 * {@code quermarke} = frueher (das Quermarkenfeuer brennt F WRG wie bis 1975); seit Phase 13 {@code lage} = Name einer
 * Wetterlage (Sommerabend, Westwind, Ablandig, Schauer, Landregen, Sturm, Seenebel, Seerauch, Gewitter), {@code regen} =
 * mm/h, {@code nebel} = 0..1[,Obergrenze m], {@code gewitter} = 0..4, {@code blitz} = see|turm (ein Blitz kurz vor dem
 * Bild), {@code leben} = aus, {@code leuchten} = an (Meeresleuchten mit voller Blüte), {@code fernrohr} = sonne (Fernrohr auf die Sonne von der Kliffkante), {@code blick} = Richtung,Neigung[,m landwärts], {@code wolke} = Wolkenart (CUMULUS … CUMULONIMBUS), {@code gruen} = Vergrößerung der
 * Spiegelung für das grüne Leuchten. Wetter von Hand ({@code bedeckung}, {@code see}, {@code wind}, {@code regen} …) stellt
 * die Regie auf „von Hand“.
 */
public final class Tempo {
    private Tempo() { }

    private static String opt(String prop) {
        String v = System.getProperty(prop);
        return v != null ? v : System.getenv(prop.toUpperCase(Locale.ROOT));
    }

    public static void main(String[] a) throws Exception {
        int frames = a.length > 0 ? Integer.parseInt(a[0]) : 120;
        int w = a.length > 1 ? Integer.parseInt(a[1]) : 1920, h = a.length > 2 ? Integer.parseInt(a[2]) : 1080;
        File png = a.length > 3 && !a[3].equals("-") ? new File(a[3]) : null;
        Quality q = a.length > 4 ? Quality.valueOf(a[4]) : Quality.MITTEL;
        double hour = a.length > 5 ? Double.parseDouble(a[5].replace(',', '.')) : SceneClock.START_HOUR;
        LocalDate day = a.length > 6 && !a[6].equals("-") ? LocalDate.parse(a[6]) : SceneClock.START_DATE;

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, w, h);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(day, hour);
        clock.lapse = 0;
        Renderer r = new Renderer(clock);
        r.frozen = true;
        r.setQuality(q);
        r.autoScale = false;
        String hud = opt("hud");
        if ("aus".equals(hud)) r.showHud = false;
        if ("voll".equals(hud)) r.hudFull = true;
        if ("aus".equals(opt("marken"))) r.showMarks = false;
        if ("an".equals(opt("marken"))) r.showMarks = true;
        if ("aus".equals(opt("meer"))) r.showMeer = false;
        if ("aus".equals(opt("wolken"))) r.showClouds = false;
        if ("aus".equals(opt("boden"))) r.showGround = false;
        // seit Phase 13: eine Lage der Wetterregie; Werte von Hand schalten die Regie ab
        String la = opt("lage");
        if (la != null && !la.isBlank()) {
            com.dan.sylt.wiring.Wetterregie.Lage lg = null;
            for (com.dan.sylt.wiring.Wetterregie.Lage x : com.dan.sylt.wiring.Wetterregie.Lage.values())
                if (x.name().equalsIgnoreCase(la) || x.label.equalsIgnoreCase(la) || x.label.toLowerCase(Locale.ROOT).startsWith(la.toLowerCase(Locale.ROOT))) lg = x;
            if (lg == null) System.err.println("FEHLER: keine Lage „" + la + "“");
            else r.wiring.regie.lage(lg);
        }
        for (String k : new String[]{"bedeckung", "see", "wind", "regen", "nebel", "gewitter"}) {
            String v = opt(k);
            if (v != null && !v.isBlank()) { r.wiring.regie.hand(); break; }
        }
        String rg = opt("regen"), nb = opt("nebel"), gw = opt("gewitter");
        if (rg != null && !rg.isBlank()) r.wiring.regen = Double.parseDouble(rg.replace(',', '.'));
        if (nb != null && !nb.isBlank()) {
            String[] v = nb.split(";");
            r.wiring.nebel = Double.parseDouble(v[0].replace(',', '.'));
            if (v.length > 1) r.wiring.nebelHoch = Double.parseDouble(v[1].replace(',', '.'));
        }
        if (gw != null && !gw.isBlank()) r.wiring.gewitter = Integer.parseInt(gw.trim());
        if ("aus".equals(opt("leben"))) r.showLeben = false;
        if ("an".equals(opt("leuchten"))) { r.bluete = 1; r.leuchtenGain = 1; }
        String gr = opt("gruen");
        if (gr != null && !gr.isBlank()) r.gruenFest = Double.parseDouble(gr.replace(',', '.'));
        String wk = opt("wolke");
        if (wk != null && !wk.isBlank()) { r.wiring.regie.hand(); r.wiring.cloudType = com.dan.cloud.CloudType.valueOf(wk.toUpperCase(Locale.ROOT)); }
        String bd = opt("bedeckung");
        if (bd != null && !bd.isBlank()) r.wiring.oktas = Double.parseDouble(bd.replace(',', '.'));
        String pg = opt("pegel");
        String see = opt("see");
        if (see != null && !see.isBlank()) {
            String[] v = see.split("[,;]");
            r.wiring.surf.hs = Double.parseDouble(v[0]);
            if (v.length > 1) r.wiring.surf.tp = Double.parseDouble(v[1]);
            if (v.length > 2) r.wiring.surf.seaFromDeg = Double.parseDouble(v[2]);
        }
        if (pg != null && !pg.isBlank()) { r.wiring.tideAn = false; r.wiring.pegel = Double.parseDouble(pg.replace(',', '.')); }
        String an = opt("ansicht"), ka = opt("kamera");
        if (ka != null && !ka.isBlank()) {
            String[] p = ka.split(",");
            double x = Double.parseDouble(p[0]), z = Double.parseDouble(p[1]);
            double hd = p.length > 2 ? Double.parseDouble(p[2]) : 0, pt = p.length > 3 ? Double.parseDouble(p[3]) : 10, up = p.length > 4 ? Double.parseDouble(p[4]) : 2;
            r.camera.place(x, Math.max(r.groundHeight(x, z), r.wiring.pegel) + up, z, hd, pt);
        } else if (an != null && !an.isBlank()) {
            Ansichten.Ansicht v = Ansichten.byName(an);
            if (v == null) System.err.println("FEHLER: keine Ansicht „" + an + "“");
            else r.placeView(v);
        }
        // seit Phase 10: -Dvorlage=Leuchturm1 … Kampen4, -Dquermarke=frueher
        String vl = opt("vorlage");
        if (vl != null && !vl.isBlank()) {
            Ansichten.Vorlage v = Ansichten.vorlage(vl);
            if (v == null) System.err.println("FEHLER: keine Vorlage „" + vl + "“");
            else r.placeVorlage(v);
        }
        // seit Phase 13: -Dblick=Richtung,Neigung[,m landwärts] dreht die Ansicht (und rückt das Auge quer zur Küste)
        String bk = opt("blick");
        if (bk != null && !bk.isBlank()) {
            String[] p = bk.split("[,;]");
            double sh = p.length > 2 ? Double.parseDouble(p[2]) : 0;
            double x = r.camera.eye[0] + com.dan.sylt.scene.Kueste.SX * sh, z = r.camera.eye[2] + com.dan.sylt.scene.Kueste.SZ * sh;
            double up = r.camera.eye[1] - Math.max(r.groundHeight(r.camera.eye[0], r.camera.eye[2]), r.wiring.pegel);
            r.camera.place(x, Math.max(r.groundHeight(x, z), r.wiring.pegel) + Math.max(0.4, up), z, Double.parseDouble(p[0]), p.length > 1 ? Double.parseDouble(p[1]) : 0);
        }
        if ("sonne".equals(opt("fernrohr"))) r.fernrohrZurSonne();   // seit Phase 13: grünes Leuchten
        if ("frueher".equals(opt("quermarke")) || "früher".equals(opt("quermarke"))) r.feuer.quermarkeFrueher = true;
        // seit Phase 9a: -Dsandsturm=an (Bft 9 aus West, Blick vom Strand), -Dwind=m/s,Richtung
        if ("an".equals(opt("sandsturm"))) r.sandsturm();
        String wi = opt("wind");
        if (wi != null && !wi.isBlank()) {
            String[] v = wi.split("[,;]");
            r.wiring.windSpeed = Double.parseDouble(v[0]);
            if (v.length > 1) r.wiring.windFromDeg = Double.parseDouble(v[1]);
        }
        d.addGLEventListener(r);
        d.display();   // init
        r.step(1 / 60.0);
        r.brandungJetzt();     // Phase 7a: Wellenfeld und Netz nahe am Auge vor dem Bild
        r.wetterJetzt();       // Phase 13: Regen und Nebel jetzt, im Regen ist der Sand schon nass
        r.sandJetzt();         // Phase 9a: Windfeld, Flugsand um das Auge und 3 s Körner
        r.wetter.sky.updateNow();   // Phase 3: das Licht des Himmels sofort, nicht im Hintergrund
        r.bautenJetzt();       // Phase 10: Strandkörbe, Herde, Schalter der Leuchtfeuer nach dem Licht
        for (int i = 0; i < 40; i++) { r.step(1 / 60.0); d.display(); }
        // seit Phase 13: ein Blitz kurz vor dem Bild (-Dblitz=see oder turm), dann die Bilder in seinem Licht
        String bl = opt("blitz");
        if (bl != null && !bl.isBlank()) {
            if (r.wiring.gewitter == 0) r.wiring.gewitter = 2;
            r.step(1 / 60.0);
            r.unwetter.blitzVonHand(r.camera.eye, r.camera.fwd, "turm".equals(bl));
            for (int i = 0; i < 4; i++) { r.step(1 / 60.0); d.display(); }
            frames = Math.min(frames, 2);
        }
        double best = Double.MAX_VALUE, sum = 0;
        for (int i = 0; i < frames; i++) {
            long t0 = System.nanoTime();
            r.step(1 / 60.0);
            d.display();
            d.getContext().makeCurrent();
            d.getGL().glFinish();
            d.getContext().release();
            double ms = (System.nanoTime() - t0) / 1e6;
            best = Math.min(best, ms);
            sum += ms;
        }
        if (r.error() != null) System.err.println("FEHLER: " + r.error());
        System.out.println(r.glInfo());
        System.out.println(r.compute() == null ? "Compute-Test: nicht gelaufen" : r.compute().line());
        System.out.printf(Locale.GERMAN, "%d×%d %s, %s: bestes Bild %.2f ms, Mittel %.2f ms (%.0f Bilder/s)%n", w, h, q.label, clock.label(), best, sum / frames, 1000 / (sum / frames));
        System.out.println("Durchgänge (ms): " + r.passTimes());
        for (String l : r.hudLines()) System.out.println("  " + l);
        if (png != null) {
            File dir = png.getAbsoluteFile().getParentFile();
            if (dir != null) dir.mkdirs();
            final java.awt.image.BufferedImage[] img = new java.awt.image.BufferedImage[1];
            d.invoke(true, x -> { img[0] = r.grab(x); return true; });
            javax.imageio.ImageIO.write(img[0], "png", png);
            System.out.println("Bild: " + png.getPath());
        }
        d.destroy();
        System.exit(r.error() == null ? 0 : 1);
    }
}
