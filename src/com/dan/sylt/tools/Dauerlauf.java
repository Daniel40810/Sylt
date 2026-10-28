package com.dan.sylt.tools;

import com.dan.store.Dienst;
import com.dan.store.Felder;
import com.dan.sylt.camera.Ansichten;
import com.dan.sylt.data.Chronik;
import com.dan.sylt.data.Daten;
import com.dan.sylt.data.Datenseite;
import com.dan.sylt.data.Speicher;
import com.dan.sylt.data.Zustand;
import com.dan.sylt.gl.Quality;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.scene.Regie;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Sturmflut;
import com.dan.sylt.wiring.Wetterregie;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Phase 19b (F55 A, nach Lighthouse Phase 16): der Dauerlauf. Die ganze Szene läuft ohne Fenster so lange wie angegeben
 * (Vorgabe 30 Minuten) im Zeitraffer 300 : 1, mit der Wetterregie und allen Lagen, den Regiebüchern (auch als Kino),
 * allen neun Ansichten, den vier Fahrten, Blitzen, der Sturmflut nach Xaver mit Abbruch und Vorspulen, der Vorspülung,
 * allen vier Qualitätsstufen, der Regelung der Auflösung und dem Klang (offline gerechnet, ohne Lautsprecher). Die
 * Chronik schreibt in den Speicher der App (die Datenbank SYLT, wenn sie ohne Rückfrage erreichbar und eingerichtet ist,
 * sonst Dateien); Sitzung und Protokoll laufen wie in der App über {@code com.dan.store.Dienst}. Jede Minute geht der
 * Zustand hin und zurück. Am Ende werden die Zeilen der Chronik dieser Sitzung gelesen und wieder gelöscht
 * ({@code -Dbehalten=ja} lässt sie stehen).
 * <p>
 * Bestanden heißt (wie bei Lighthouse): keine Ausnahme in irgendeinem Faden, kein Grafikfehler, der Speicher nach der
 * fünften Minute nicht mehr als 25 % gewachsen, kein Bild über 2 s (oder das 20-fache des Mittels, auf Mesa; die zwei
 * Sekunden nach einem Stufenwechsel zählen getrennt und dürfen bis 5 s brauchen), die Chronik hat geschrieben und ist
 * lesbar, und am Ende sind Zustand und Felder noch lesbar. Die letzte Zeile ist „Dauerlauf bestanden“ oder „Dauerlauf
 * NICHT bestanden“ mit den Gründen.
 * <pre>
 * Dauerlauf [Minuten] [Breite] [Datei]
 * Dauerlauf 30 1920 docs/status/Dauerlauf_Phase19.txt
 * </pre>
 */
public final class Dauerlauf {
    private Dauerlauf() { }

    static final List<String> FEHLER = new ArrayList<>();
    /** Beginn des laufenden Bildes (für den Wächter, der bei langen Bildern den Stapel ins Protokoll schreibt). */
    static volatile long frameStart;
    /** Mittlere Bildzeit (ns), gleitend; der Wächter meldet erst über dem Vierfachen (auf Mesa braucht jedes Bild fast 1 s). */
    static volatile double mittelNs = 16e6;
    public static final String[] EREIGNIS = {"Ansicht", "Regiebuch", "Blitz", "Ansicht", "Sturmflut", "Fahrt", "Ansicht", "Stufenwechsel", "Lage",
            "Ansicht", "Abbruch, Vorspulen, Vorspülung", "Kreisen"};
    static final String[] FAHRTEN = {"Drohne", "Treppe", "Möwe", "Welle"};

    public static void main(String[] a) throws Exception {
        double minuten = a.length > 0 ? Double.parseDouble(a[0].replace(',', '.')) : 30;
        int width = a.length > 1 ? Integer.parseInt(a[1]) : 1920;
        File out = new File(a.length > 2 ? a[2] : "docs/status/Dauerlauf_Phase19.txt");
        if (out.getAbsoluteFile().getParentFile() != null) out.getAbsoluteFile().getParentFile().mkdirs();
        int height = width * 9 / 16;
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> { synchronized (FEHLER) { FEHLER.add("Faden " + t.getName() + ": " + e); } e.printStackTrace(); });
        PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out.toPath(), StandardCharsets.UTF_8));
        Consumer<String> say = s -> { System.out.println(s); synchronized (pw) { pw.println(s); pw.flush(); } };
        say.accept(String.format(Locale.GERMAN, "Sylt · Dauerlauf · %s · %.0f Minuten · %d×%d · %s", Renderer.VERSION, minuten, width, height,
                java.time.LocalDateTime.now().withNano(0)));
        say.accept("Rechner: " + System.getProperty("os.name") + " · Java " + System.getProperty("java.version") + " · " + Runtime.getRuntime().availableProcessors() + " Kerne · Xmx "
                + Runtime.getRuntime().maxMemory() / 1048576 + " MB");

        // der Speicher wie beim Start ohne Startbild: die Datenbank, wenn es ohne Rückfrage geht, sonst Dateien
        com.dan.sylt.ui.Startbild.stumm();
        Speicher sp = Datenseite.speicher();
        say.accept("Speicher: " + sp.wo());

        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(prof);
        caps.setOnscreen(false);
        caps.setFBO(true);
        caps.setDepthBits(24);
        GLOffscreenAutoDrawable d = GLDrawableFactory.getFactory(prof).createOffscreenAutoDrawable(null, caps, null, width, height);
        d.setAutoSwapBufferMode(false);
        SceneClock clock = new SceneClock(LocalDate.of(2026, 7, 20), 6.0);
        clock.lapse = 300;
        Renderer r = new Renderer(clock);
        r.showHud = true;
        r.autoScale = true;
        r.targetFps = 60;
        r.setQuality(Quality.MITTEL);
        r.klang.offline = true;
        d.addGLEventListener(r);
        d.display();
        if (r.error() != null) { say.accept("FEHLER Grafik: " + r.error()); say.accept("Dauerlauf NICHT bestanden"); pw.close(); System.exit(2); }
        say.accept("Grafik: " + r.glInfo());

        // Sitzung, Protokoll und Chronik wie in der App
        Dienst.protokollS = 5;
        Dienst.sichernS = 120;
        Dienst.starten(Renderer.VERSION, r.glInfo(), r.quality().name(), () -> {
            LinkedHashMap<String, String> z = Zustand.erfassen(r, false);
            return new com.dan.store.Ablage.Sicherung(Felder.alsText(z, "Sylt · Dauerlauf · " + Renderer.VERSION), Zustand.spalten(z));
        });
        Chronik ch = new Chronik(r);
        long sid = Dienst.sitzung();
        say.accept("Sitzung " + sid + " · " + ch.describe());

        float[] ton = new float[2 * 2048];
        List<Ansichten.Ansicht> views = Ansichten.ALLE;
        List<Regie.Book> buecher = Regie.all();
        Wetterregie.Lage[] lagen = Wetterregie.Lage.values();
        Quality[] qs = Quality.values();
        Random rnd = new Random(2013);

        final Thread gl = Thread.currentThread();
        Thread waechter = new Thread(() -> {
            long gemeldet = 0;
            while (true) {
                try { Thread.sleep(200); } catch (InterruptedException e) { return; }
                long fs = frameStart;
                if (fs != 0 && fs != gemeldet && System.nanoTime() - fs > Math.max(800e6, 4 * mittelNs)) {
                    gemeldet = fs;
                    StringBuilder b = new StringBuilder(String.format(Locale.GERMAN, "  Bild hängt seit %.1f s in:", (System.nanoTime() - fs) / 1e9));
                    StackTraceElement[] tr = gl.getStackTrace();
                    for (int i = 0; i < Math.min(8, tr.length); i++) b.append("\n    ").append(tr[i]);
                    say.accept(b.toString());
                }
            }
        }, "Wächter");
        waechter.setDaemon(true);
        waechter.start();
        // höchstens zwei Bilder in Arbeit, wie im Fenster der Tausch der Puffer (aus Lighthouse)
        final long[] fences = new long[2];
        final int[] fenceI = {0};
        GLRunnable takt = drawable -> {
            GL4 g = drawable.getGL().getGL4();
            int k = fenceI[0]++ % fences.length;
            if (fences[k] != 0) { g.glClientWaitSync(fences[k], GL4.GL_SYNC_FLUSH_COMMANDS_BIT, 5_000_000_000L); g.glDeleteSync(fences[k]); }
            fences[k] = g.glFenceSync(GL4.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            return true;
        };
        long start = System.nanoTime(), ende = start + (long) (minuten * 60e9), lastMinute = start, lastShot = start;
        long frames = 0;
        String lastEvent = "Start";
        long lastEventAt = start;
        double worst = 0, worstAll = 0, worstSwitch = 0;
        long switchUntil = 0;
        long heap5 = -1, heapEnd = -1;
        int minute = 0, scenes = 0, bolts = 0, zustaende = 0, stufen = 0, fluten = 0, fahrten = 0, regie = 0, kino = 0, abbr = 0, vorsp = 0;
        double sumFps = 0;
        int fpsN = 0;
        long lastNanos = System.nanoTime();
        while (System.nanoTime() < ende) {
            long t0 = System.nanoTime();
            frameStart = t0;
            d.display();
            d.invoke(true, takt);
            frameStart = 0;
            long t1 = System.nanoTime();
            double ms = (t1 - t0) / 1e6;
            frames++;
            mittelNs += ((t1 - t0) - mittelNs) * 0.05;
            if (frames > 30 && t0 < switchUntil) worstSwitch = Math.max(worstSwitch, ms);
            else if (frames > 30) { worst = Math.max(worst, ms); worstAll = Math.max(worstAll, ms); }
            if (frames > 30 && ms > 1000) say.accept(String.format(Locale.GERMAN, "  langes Bild %.0f ms, %.1f s nach „%s“", ms, (t0 - lastEventAt) / 1e9, lastEvent));
            // der Klang wie die Ausgabe, nur ohne Lautsprecher
            double dt = (t1 - lastNanos) / 1e9;
            lastNanos = t1;
            int n = Math.min(2048, (int) Math.round(dt * r.klang.s.sampleRate()));
            if (n > 0) r.klang.s.render(ton, n);
            if (r.error() != null) { synchronized (FEHLER) { FEHLER.add("Grafik: " + r.error()); } break; }
            // alle 12 s etwas tun
            if (t1 - lastShot > 12_000_000_000L) {
                lastShot = t1;
                int k = scenes++ % EREIGNIS.length;
                lastEvent = EREIGNIS[k];
                lastEventAt = t1;
                try {
                    switch (k) {
                        case 0, 3, 6, 9 -> { aufhoeren(r); r.placeView(views.get(rnd.nextInt(views.size()))); }
                        case 1 -> {
                            aufhoeren(r);
                            Regie.Book b = buecher.get(rnd.nextInt(buecher.size()));
                            if (rnd.nextInt(3) == 0) { r.kinoStart(b); kino++; } else r.regieStart(b);
                            regie++;
                            lastEvent = "Regiebuch „" + b.name() + "“";
                        }
                        case 2 -> { if (r.wiring.gewitter == 0) r.wiring.gewitter = 2; r.unwetter.blitzVonHand(r.camera.eye, r.camera.fwd, rnd.nextInt(3) == 0); bolts++; }
                        case 4 -> { aufhoeren(r); r.sturmflutStarten(Sturmflut.STUNDE); r.placeNamed("AmKliff"); fluten++; }
                        case 5 -> {
                            aufhoeren(r);
                            String f = FAHRTEN[rnd.nextInt(FAHRTEN.length)];
                            boolean ok = switch (f) {
                                case "Drohne" -> { var x = r.drohnenFahrt(); if (x != null) r.camera.startFahrt(x); yield x != null; }
                                case "Treppe" -> { var x = r.treppenFahrt(); if (x != null) r.camera.startFahrt(x); yield x != null; }
                                case "Möwe" -> r.moewenFlug();
                                default -> r.wellenritt();
                            };
                            if (ok) fahrten++;
                            lastEvent = "Fahrt " + f + (ok ? "" : " (nicht möglich)");
                        }
                        case 7 -> { r.setQuality(qs[rnd.nextInt(qs.length)]); stufen++; switchUntil = System.nanoTime() + 2_000_000_000L; }
                        case 8 -> { Wetterregie.Lage l = lagen[rnd.nextInt(lagen.length)]; r.wiring.regie.lage(l); lastEvent = "Lage " + l.label; }
                        case 10 -> {
                            if (r.sturmflut.aktiv && (abbr + vorsp) % 2 == 0) {   // abwechselnd, sonst käme die Vorspülung nie dran
                                r.placeNamed("AmKliff");
                                r.abbruchHier();
                                r.vorspulen(r.sturmflut.stunden(clock) + 1, 5, true);
                                abbr++;
                                lastEvent = "Abbruch hier und eine Stunde vorspulen";
                            } else {
                                r.vorspuelung.starten(clock.epochMillis());
                                vorsp++;
                                lastEvent = "Vorspülung";
                            }
                        }
                        case 11 -> { aufhoeren(r); r.camera.autoOrbit = true; r.camera.orbitSpeed = 12; }
                        default -> { }
                    }
                } catch (RuntimeException e) {
                    synchronized (FEHLER) { FEHLER.add(lastEvent + ": " + e); }
                    e.printStackTrace();
                }
            }
            // jede Minute: Zustand hin und zurück, Speicher, Zeile
            if (t1 - lastMinute > 60_000_000_000L) {
                lastMinute = t1;
                minute++;
                LinkedHashMap<String, String> z = Zustand.erfassen(r, false);
                String text = Felder.alsText(z, "Dauerlauf");
                List<String> f = Zustand.anwenden(r, Felder.ausText(text));
                if (!f.isEmpty()) synchronized (FEHLER) { FEHLER.add("Minute " + minute + ": Zustand nicht ganz hergestellt: " + f); }
                zustaende++;
                System.gc();
                Thread.sleep(50);
                long heap = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
                if (minute == 5) heap5 = heap;
                heapEnd = heap;
                double fps = r.fps();
                sumFps += fps;
                fpsN++;
                say.accept(String.format(Locale.GERMAN, "Minute %2d · %s · %.0f Bilder/s · Grafikkarte %.1f ms · Prozessor %.1f ms · schlechtestes Bild %.0f ms · Auflösung %.0f %% (%s) · Speicher %d MB · %s · %s · %d Werte",
                        minute, clock.label(), fps, r.gpuMs(), r.cpuMs(), worst, r.renderScale * 100, r.quality().label, heap,
                        r.sturmflut.aktiv ? String.format(Locale.GERMAN, "Sturmflut Stunde %.1f", r.sturmflut.stunden(clock)) : r.wiring.regie.lage().label,
                        ch.describe(), z.size()));
                worst = 0;
            }
        }
        aufhoeren(r);
        // Abschluss: Chronik und Sitzung beenden, die Zeilen der Chronik lesen (und wieder löschen)
        ch.beenden();
        ch.warten();
        Dienst.beenden();
        boolean chronikOk = true;
        try {
            int ab = sp.abbrueche(1000).size(), sf = sp.sturmflutLaeufe(1000).size();
            say.accept(String.format(Locale.GERMAN, "Chronik: %d Abbrüche, %d Sturmfluten, %d Vorspülungen, %d Serien, %d Regieläufe geschrieben, %d Fehler · lesbar: %d Abbrüche, %d Läufe in %s",
                    ch.abbrueche, ch.sturmfluten, ch.vorspuelungen, ch.serien, ch.regielaeufe, ch.fehler, ab, sf, sp.wo()));
            chronikOk = ch.fehler == 0 && ch.serien + ch.sturmfluten + ch.abbrueche + ch.regielaeufe > 0;
            if (sid > 0 && !"ja".equals(System.getProperty("behalten"))) say.accept("Chronik der Sitzung " + sid + " geleert: " + sp.chronikLeeren(sid) + " Zeilen");
        } catch (Exception e) {
            chronikOk = false;
            synchronized (FEHLER) { FEHLER.add("Chronik am Ende: " + e); }
        }
        boolean lesbar;
        try {
            LinkedHashMap<String, String> z = Zustand.erfassen(r, true);
            lesbar = z.size() > 100 && Zustand.anwenden(r, Felder.ausText(Felder.alsText(z, "Ende"))).isEmpty();
        } catch (RuntimeException e) {
            lesbar = false;
            synchronized (FEHLER) { FEHLER.add("Zustand am Ende: " + e); }
        }
        double sek = (System.nanoTime() - start) / 1e9;
        say.accept(String.format(Locale.GERMAN, "Ende nach %.1f Minuten: %d Bilder, im Mittel %.1f Bilder/s, schlechtestes Bild %.0f ms, %d Ereignisse, %d Regiebücher (%d als Kino), %d Blitze, %d Sturmfluten, "
                        + "%d× Abbruch mit Vorspulen, %d Vorspülungen, %d Fahrten, %d Stufenwechsel, %d× Zustand hin und zurück, Auflösung %d× geregelt, Speicher Minute 5: %d MB, Ende: %d MB, "
                        + "größter Ruck beim Stufenwechsel %.0f ms",
                sek / 60, frames, fpsN > 0 ? sumFps / fpsN : frames / sek, worstAll, scenes, regie, kino, bolts, fluten, abbr, vorsp, fahrten, stufen, zustaende, r.scaleSteps(), heap5, heapEnd, worstSwitch));
        List<String> gruende = new ArrayList<>();
        synchronized (FEHLER) { gruende.addAll(FEHLER); }
        if (heap5 > 0 && heapEnd > heap5 * 1.25 + 64) gruende.add(String.format(Locale.GERMAN, "Speicher wächst: %d MB nach Minute 5, %d MB am Ende", heap5, heapEnd));
        double mean = sek / Math.max(1, frames) * 1000;
        if (worstAll > Math.max(2000, 20 * mean)) gruende.add(String.format(Locale.GERMAN, "ein Bild brauchte %.0f ms (Mittel %.0f ms)", worstAll, mean));
        if (worstSwitch > 5000) gruende.add(String.format(Locale.GERMAN, "ein Stufenwechsel brauchte %.0f ms", worstSwitch));
        if (!chronikOk) gruende.add("die Chronik hat nichts oder fehlerhaft geschrieben");
        if (!lesbar) gruende.add("Zustand am Ende nicht mehr herstellbar");
        if (minute < 1) gruende.add("keine volle Minute gelaufen");
        for (String g : gruende) say.accept("FEHLER " + g);
        say.accept(gruende.isEmpty() ? "Dauerlauf bestanden" : "Dauerlauf NICHT bestanden (" + gruende.size() + " Gründe)");
        pw.close();
        System.exit(gruende.isEmpty() ? 0 : 1);
    }

    /** Laufende Regie, Kino, Fahrt und Kreisen beenden, bevor etwas Neues beginnt. */
    static void aufhoeren(Renderer r) {
        if (r.kino.laeuft()) r.kinoVerlassen();
        if (r.regie.running()) r.regieStop();
        r.camera.stopFahrt();
        r.camera.stopMitfahrt();
        r.camera.autoOrbit = false;
        r.frozen = false;
    }
}
