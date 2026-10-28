package com.dan.sylt.data;

import com.dan.sylt.gl.Renderer;
import com.dan.sylt.scene.Kliffabbruch;
import com.dan.sylt.scene.Regie;
import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Vorspuelung;
import com.dan.store.Ablage;
import com.dan.store.Dienst;
import com.dan.store.Protokoll;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Die Chronik der Szene (Phase 16, F36, F37 A): beobachtet die laufende Szene alle 0,5 s und schreibt über den gültigen
 * {@link Speicher} in die Datenbank oder in die Dateien. Das Bild wartet nie: beobachtet wird in einem eigenen Faden,
 * geschrieben in einem zweiten (der Reihe nach, damit ein Lauf seine Nummer hat, bevor er fortgeschrieben wird).
 * <ul>
 * <li><b>Abbrüche</b>: jedes gebrochene Stück aus fracture mit Ort, Bilanz (fest), Kegel (locker), Kehle und L_c, Pegel
 * und Auflauf, Rückgang; dazu die neue Kante dieses Stücks (F36 A: so gibt es die Kante zu jedem Zeitpunkt), die
 * abgebrochene Fläche zwischen alter und neuer Kante und die Findlinge (die Spur der Eiszeit).</li>
 * <li><b>Sturmfluten</b>: jeder Lauf des Fahrplans vom Beginn bis zum Ende mit höchstem Pegel (Scheitel), Windstau,
 * Auflauf, Stunden mit Wasser am Fuß des Kliffs, tiefster Kehle und Abbrüchen.</li>
 * <li><b>Vorspülungen</b>: jeder Körper mit Fläche, Menge und wie viel noch da ist.</li>
 * <li><b>Serien</b>: alle zehn Minuten echter Zeit die Wellen und Serien der Nahbrandung.</li>
 * <li><b>Regiebücher</b>: jeder Lauf mit Blatt (oder abgebrochen), ob im Kino, wie lange gewartet und wie oft
 * ausgewichen wurde.</li>
 * </ul>
 */
public final class Chronik {
    /** Echte Sekunden je Zeile der Serien. */
    public static final double SERIE_ALLE_S = 600;
    /** Echte Sekunden zwischen zwei Fortschreibungen eines laufenden Laufs. */
    public static final double FORTSCHREIBEN_S = 60;

    private final Renderer r;
    private final ScheduledExecutorService schauen;
    private final ExecutorService schreiben;
    private volatile boolean an = true;
    /** Zähler für Block CHRONIK und Prüfung. */
    public volatile int abbrueche, sturmfluten, vorspuelungen, serien, regielaeufe, fehler;
    public volatile String letzte = "";
    /** Seit Phase 19b: die verschiedenen Fehlertexte (höchstens 20), für Dauerlauf und Prüfung. */
    public final java.util.Set<String> fehlerTexte = java.util.Collections.synchronizedSet(new java.util.LinkedHashSet<>());

    private void merke(String t) { if (fehlerTexte.size() < 20) fehlerTexte.add(t); }

    private int ergebnisGesehen;
    // Sturmflut
    private Daten.SturmflutLauf lauf;
    private final AtomicLong laufId = new AtomicLong(-1);
    private int abbruchBeiBeginn;
    private double letzteUhr = Double.NaN, fussHoehe = Double.NaN;
    private long laufGeschrieben;
    // Vorspülung
    private double vorspBeginn = Double.NaN;
    private Daten.Vorspuelung vorsp;
    private final AtomicLong vorspId = new AtomicLong(-1);
    private long vorspGeschrieben;
    // Serien
    private long serieSeit = System.currentTimeMillis();
    private int wellenVorher, serienVorher;
    private long serieMs = Long.MIN_VALUE;
    // Regie
    private Regie.Book buch;
    private long buchBeginn;
    private boolean buchKino;
    private File blattVorher;
    private int ausgewichenVorher;

    public Chronik(Renderer r) {
        this.r = r;
        if (r.abbruch != null) ergebnisGesehen = r.abbruch.ergebnisse.size();
        schauen = Executors.newSingleThreadScheduledExecutor(x -> faden(x, "Sylt-Chronik"));
        schreiben = Executors.newSingleThreadExecutor(x -> faden(x, "Sylt-Chronik-Schreiben"));
        schauen.scheduleWithFixedDelay(this::schauen, 500, 500, TimeUnit.MILLISECONDS);
    }

    private static Thread faden(Runnable x, String n) {
        Thread t = new Thread(x, n);
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    }

    /** Die Chronik schreibt (true) oder nicht; die Beobachtung läuft weiter. */
    public void schreiben(boolean ja) { an = ja; }
    public boolean schreibt() { return an; }

    /** Für Prüfungen: einmal beobachten (im Faden des Aufrufers) und warten, bis alles geschrieben ist. */
    public void jetzt() {
        schauen();
        warten();
    }

    /** Warten, bis die Schreibschlange leer ist (höchstens 30 s). */
    public void warten() {
        try { schreiben.submit(() -> { }).get(30, TimeUnit.SECONDS); } catch (Exception e) { /* weiter */ }
    }

    /** Nach „Weiter wie zuletzt“: die Abbrüche des hergestellten Stands sind schon geschrieben. */
    public synchronized void hergestellt() {
        if (r.abbruch != null) ergebnisGesehen = r.abbruch.ergebnisse.size();
    }

    public void beenden() {
        try {
            synchronized (this) {
                if (lauf != null) sturmflutEnde(r.clock.epochMillis());
                if (vorsp != null) schreibeVorspuelung("Vorspülung beim Beenden");
                serieZeile(true);
            }
        } catch (RuntimeException e) { /* weiter */ }
        schauen.shutdownNow();
        schreiben.shutdown();
        try { schreiben.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static Long sitzung() { long s = Dienst.sitzung(); return s > 0 ? s : null; }

    private interface Schreib { void tun(Speicher s) throws Ablage.Fehler; }

    private void spaeter(String was, Schreib w) {
        if (!an) return;
        schreiben.submit(() -> {
            try {
                w.tun(Datenseite.speicher());
                letzte = was;
            } catch (Ablage.Fehler | RuntimeException e) {
                fehler++;
                merke(was.replaceAll("\\d", "#") + ": " + e.getMessage());
                Protokoll.warnung("Chronik", was + ": " + e.getMessage());
            }
        });
    }

    // ------------------------------------------------------------------ beobachten

    synchronized void schauen() {
        try {
            double jetzt = r.clock.epochMillis();
            abbrueche(jetzt);
            sturmflut(jetzt);
            vorspuelung(jetzt);
            serieZeile(false);
            regie();
        } catch (RuntimeException e) {
            fehler++;
            merke("Beobachten: " + e);
            Protokoll.fehler("Chronik", "Beobachten: " + e, e);
        }
    }

    // ------------------------------------------------------------------ Abbrüche

    private void abbrueche(double jetzt) {
        Kliffabbruch k = r.abbruch;
        if (k == null) return;
        List<Kliffabbruch.Ergebnis> l = k.ergebnisse;
        for (; ergebnisGesehen < l.size(); ergebnisGesehen++) {
            Daten.Abbruch a = abbruch(l.get(ergebnisGesehen), jetzt, lauf == null ? null : lauf.name());
            abbrueche++;
            spaeter("Abbruch bei N " + String.format(Locale.GERMAN, "%,.0f", a.nord()), s -> s.abbruch(a, sitzung()));
        }
    }

    /** Ein Ergebnis von fracture als Zeile der Chronik (auch für Prüfungen). */
    public static Daten.Abbruch abbruch(Kliffabbruch.Ergebnis e, double jetzt, String sturmflut) {
        long ms = (long) (Double.isNaN(e.ms()) ? jetzt : e.ms());
        double[][] vor = e.kanteVor(), nach = e.kanteNach();
        double[] mitte = vor[vor.length / 2];
        Daten.Geom flaeche = null;
        if (e.rueckgang() > 0.05) {
            double[][] p = new double[vor.length + nach.length][];
            for (int i = 0; i < vor.length; i++) p[i] = vor[i];
            for (int i = 0; i < nach.length; i++) p[vor.length + i] = nach[nach.length - 1 - i];
            flaeche = Daten.Geom.flaeche(p);
        }
        Daten.Kantenstueck kante = new Daten.Kantenstueck(0, "ABBRUCH", null, Math.max(e.nordVon(), e.nordBis()), Math.min(e.nordVon(), e.nordBis()),
                Daten.Geom.linie(nach), Daten.Geom.linie(e.abrisskante()), e.wandMax(), e.kanteMaxH(), ms);
        List<double[]> fl = e.findling() == null ? List.of() : List.<double[]>of(e.findling());
        return new Daten.Abbruch(0, ms, e.nord(), Math.max(e.nordVon(), e.nordBis()), Math.min(e.nordVon(), e.nordBis()), e.laenge(), e.volumen(), e.bloecke(), e.zerfallen(),
                e.ausgespuelt(), e.kegel(), e.kehle(), e.lc(), e.pegel(), e.auflauf(), e.rueckgang(), sturmflut, mitte.clone(), flaeche, kante, fl);
    }

    // ------------------------------------------------------------------ Sturmflut

    private double fussHoehe() {
        if (Double.isNaN(fussHoehe) && r.abbruch != null) {
            double[] h = r.abbruch.kante.hFuss.clone();
            Arrays.sort(h);
            fussHoehe = h.length == 0 ? 3 : h[h.length / 2];
        }
        return Double.isNaN(fussHoehe) ? 3 : fussHoehe;
    }

    private void sturmflut(double jetzt) {
        boolean aktiv = r.sturmflut.aktiv;
        if (aktiv && lauf == null) {
            java.time.LocalDate tag = r.sturmflut.datum;
            String name = "Sturmflut nach Xaver am " + String.format(Locale.GERMAN, "%td.%<tm.%<tY", tag);
            lauf = new Daten.SturmflutLauf(0, Startdaten.belegteSturmflut(tag), name, (long) jetzt, null, Double.NEGATIVE_INFINITY, null, 0, 0, 0, 0, 0);
            laufId.set(-1);
            abbruchBeiBeginn = r.abbruch == null ? 0 : r.abbruch.abbrueche;
            letzteUhr = jetzt;
            sturmfluten++;
            schreibeLauf("Sturmflut beginnt");
            return;
        }
        if (lauf == null) return;
        if (!aktiv || jetzt < lauf.beginnMs()) { sturmflutEnde(jetzt); return; }
        double pegel = r.wiring.pegel, r2 = r.brandung.auflaufR2(), dh = Math.max(0, Math.min(1, (jetzt - letzteUhr) / 3_600_000.0));
        letzteUhr = jetzt;
        Daten.SturmflutLauf l = lauf;
        double scheitel = l.scheitel();
        Long scheitelMs = l.scheitelMs();
        if (pegel > scheitel) { scheitel = pegel; scheitelMs = (long) jetzt; }
        double amFuss = l.stundenAmFuss() + (pegel + r2 >= fussHoehe() ? dh : 0);
        lauf = new Daten.SturmflutLauf(l.id(), l.belegt(), l.name(), l.beginnMs(), null, scheitel, scheitelMs, Math.max(l.windstauMax(), r.wiring.tide.surge),
                Math.max(l.auflaufMax(), r2), amFuss, Math.max(l.kehleMax(), r.abbruch == null ? 0 : r.abbruch.tiefsteKehle()),
                r.abbruch == null ? 0 : r.abbruch.abbrueche - abbruchBeiBeginn);
        if (System.currentTimeMillis() - laufGeschrieben > FORTSCHREIBEN_S * 1000) schreibeLauf("Sturmflut fortgeschrieben");
    }

    private void sturmflutEnde(double jetzt) {
        Daten.SturmflutLauf l = lauf;
        lauf = null;
        if (l == null) return;
        Daten.SturmflutLauf e = new Daten.SturmflutLauf(l.id(), l.belegt(), l.name(), l.beginnMs(), (long) Math.max(jetzt, l.beginnMs()), l.scheitel(), l.scheitelMs(),
                l.windstauMax(), l.auflaufMax(), l.stundenAmFuss(), l.kehleMax(), l.abbrueche());
        schreibeLauf(e, "Sturmflut zu Ende: " + e.abbrueche() + " Abbrüche");
    }

    private void schreibeLauf(String was) { schreibeLauf(lauf, was); }

    private void schreibeLauf(Daten.SturmflutLauf l, String was) {
        if (l == null) return;
        laufGeschrieben = System.currentTimeMillis();
        Daten.SturmflutLauf fest = Double.isInfinite(l.scheitel()) ? new Daten.SturmflutLauf(l.id(), l.belegt(), l.name(), l.beginnMs(), l.endeMs(), r.wiring.pegel,
                (long) r.clock.epochMillis(), l.windstauMax(), l.auflaufMax(), l.stundenAmFuss(), l.kehleMax(), l.abbrueche()) : l;
        spaeter(was, s -> {
            long id = laufId.get();
            laufId.set(s.sturmflutLauf(id > 0 ? fest.mitId(id) : fest, sitzung()));
        });
    }

    // ------------------------------------------------------------------ Vorspülung

    private void vorspuelung(double jetzt) {
        Vorspuelung v = r.vorspuelung;
        if (v == null || !v.liegt()) {
            if (vorsp != null) { schreibeVorspuelung("Vorspülung abgetragen"); vorsp = null; }
            return;
        }
        double b = v.beginnMs();
        if (vorsp == null || b != vorspBeginn) {
            vorspBeginn = b;
            vorspId.set(-1);
            vorspuelungen++;
            vorsp = vorspuelungAus(v, (long) b);
            schreibeVorspuelung("Vorspülung beginnt");
            return;
        }
        vorsp = vorspuelungAus(v, (long) b);
        if (System.currentTimeMillis() - vorspGeschrieben > FORTSCHREIBEN_S * 1000) schreibeVorspuelung("Vorspülung fortgeschrieben");
    }

    /** Die Vorspülung der App als Zeile: Fläche vom Fuß 70 m seewärts zwischen N 6 089 250 und 6 089 850. */
    Daten.Vorspuelung vorspuelungAus(Vorspuelung v, long beginn) {
        com.dan.sylt.scene.Kante k = r.abbruch != null ? r.abbruch.kante : null;
        Daten.Geom flaeche = null;
        if (k != null) {
            List<double[]> land = new ArrayList<>(), see = new ArrayList<>();
            for (double n = Vorspuelung.NORD1; n >= Vorspuelung.NORD0; n -= 25) {
                int row = k.rowNear(Site.N0 - n, 60);
                if (row < 0) continue;
                double fuss = k.fussX(row), z = Site.N0 - n;
                land.add(new double[]{fuss, z});
                see.add(new double[]{fuss - Vorspuelung.BREITE, z});
            }
            if (land.size() >= 2) {
                List<double[]> p = new ArrayList<>(land);
                for (int i = see.size() - 1; i >= 0; i--) p.add(see.get(i));
                flaeche = Daten.Geom.flaeche(p.toArray(new double[0][]));
            }
        }
        double halb = 60 * Math.pow(1.2 / Math.max(0.3, r.wiring.surf.hs), 2);
        String text = String.format(Locale.GERMAN, "Aufbau %.0f %%, noch %.0f %% da; Abtrag bei H_s %.1f m [G]", v.aufbau * 100, v.amplitude() * 100, r.wiring.surf.hs);
        return new Daten.Vorspuelung(0, "Vorspülung vom " + Daten.text(beginn), "APP", Daten.zeit(beginn).getYear(), Math.round(v.volumen), Vorspuelung.NORD0,
                Vorspuelung.NORD1, "vor dem Kliff, 600 m", flaeche, beginn, Vorspuelung.HOEHE * Math.min(1, v.aufbau * 1.15) * v.amplitude(), Vorspuelung.BREITE,
                Math.round(halb * 10) / 10.0, text, null);
    }

    private void schreibeVorspuelung(String was) {
        Daten.Vorspuelung v = vorsp;
        if (v == null) return;
        vorspGeschrieben = System.currentTimeMillis();
        spaeter(was, s -> {
            long id = vorspId.get();
            Daten.Vorspuelung mit = id > 0 ? new Daten.Vorspuelung(id, v.name(), v.art(), v.jahr(), v.menge(), v.nordVon(), v.nordBis(), v.strecke(), v.flaeche(), v.beginnMs(),
                    v.hoeheFuss(), v.breite(), v.halbwertTage(), v.bemerkung(), v.quelle()) : v;
            vorspId.set(s.vorspuelung(mit, sitzung()));
        });
    }

    // ------------------------------------------------------------------ Serien

    private void serieZeile(boolean ende) {
        long nun = System.currentTimeMillis();
        if (serieMs == Long.MIN_VALUE) serieMs = (long) r.clock.epochMillis();
        if (!ende && nun - serieSeit < SERIE_ALLE_S * 1000) return;
        com.dan.sylt.scene.Serie s = r.serie;
        int w = s.wellen - wellenVorher, n = s.serien - serienVorher;
        double dauer = (nun - serieSeit) / 1000.0;
        long beginn = serieMs;
        serieSeit = nun;
        serieMs = (long) r.clock.epochMillis();
        wellenVorher = s.wellen;
        serienVorher = s.serien;
        if (w <= 0) return;
        var st = r.nah.stueck();
        double x = st != null ? st.ox : Site.STRAND.x(), z = st != null ? st.oz : Site.STRAND.z();
        Daten.Serie zeile = new Daten.Serie(beginn, dauer, w, n, s.groesste, st != null ? st.hs : r.wiring.surf.hs, st != null ? st.tp : r.wiring.surf.tp, x, z);
        serien++;
        spaeter("Serien: " + n + " aus " + w + " Wellen", sp -> sp.serie(zeile, sitzung()));
    }

    // ------------------------------------------------------------------ Regiebücher

    private void regie() {
        Regie.Book b = r.regie.running() ? r.regie.book() : null;
        if (b != buch) {
            if (buch != null) regieEnde(buch);
            buch = b;
            if (b != null) { buchBeginn = System.currentTimeMillis(); buchKino = r.kino.laeuft(); blattVorher = r.lastSheet; ausgewichenVorher = r.regie.ausgewichen; }
        } else if (b != null && r.kino.laeuft()) buchKino = true;
    }

    private void regieEnde(Regie.Book b) {
        File blatt = r.lastSheet;
        boolean neu = blatt != null && blatt != blattVorher && blatt.isFile();
        byte[] png = null;
        if (neu) try { png = java.nio.file.Files.readAllBytes(blatt.toPath()); } catch (Exception e) { /* ohne Blatt */ }
        Daten.Regielauf l = new Daten.Regielauf(b.name(), buchBeginn, System.currentTimeMillis(), neu, buchKino, r.fps(), r.gpuMs(), r.regie.waitedTotal(),
                Math.max(0, r.regie.ausgewichen - ausgewichenVorher), b.fotos(), r.quality().name(), png, neu ? blatt.getPath() : null);
        regielaeufe++;
        spaeter("Regiebuch „" + b.name() + "“" + (neu ? " mit Blatt" : " abgebrochen"), s -> s.regielauf(l, sitzung()));
    }

    /** „Chronik: 3 Abbrüche, 1 Sturmflut, 1 Vorspülung, 4 Serien, 1 Regiebuch · zuletzt …“ */
    public String describe() {
        return String.format(Locale.GERMAN, "Chronik%s: %d Abbrüche, %d Sturmfluten, %d Vorspülungen, %d Serien, %d Regiebücher%s%s", an ? "" : " (angehalten)", abbrueche,
                sturmfluten, vorspuelungen, serien, regielaeufe, letzte.isEmpty() ? "" : " · zuletzt " + letzte, fehler > 0 ? " · " + fehler + " Fehler (Protokoll)" : "");
    }
}
