package com.dan.sylt.data;

import com.dan.sylt.scene.Regie;
import com.dan.store.DateiAblage;
import com.dan.store.Felder;
import com.dan.store.Zahl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Speichern ohne Datenbank (F33 A): Textdateien im Ordner {@code daten/} des Projekts. Die mitgelieferten Einträge
 * kommen aus {@link Startdaten} und stehen nicht in Dateien; eigene liegen je in einer Datei. Die Chronik liegt als CSV
 * mit Semikolon und einer Spalte {@code WKT} in UTM 32N (QGIS: „Getrennte Texte“, Geometrie WKT, EPSG:25832):
 * <pre>
 * daten/chronik/abbrueche.csv  sturmfluten.csv  vorspuelungen.csv  serien.csv  regielaeufe.csv
 * daten/regiebuecher/*.txt   daten/ansichten/N.txt + N.zustand + N.png   daten/presets/paket/Klasse/*.txt
 * daten/sitzungen/N.txt + N.zustand + N.png   daten/protokoll.txt
 * </pre>
 * Beim nächsten Verbinden übernimmt {@link Uebernahme} die Chronik in die Datenbank und benennt die Dateien in
 * {@code *.uebernommen} um. Die Kante 2024 liest dieser Speicher aus {@code daten/kliff/kante.csv}.
 */
public final class DateiSpeicher extends DateiAblage implements Speicher {

    public DateiSpeicher(File dir) { super(dir); }

    /** Der Ordner daten (-Dsylt.daten, sonst ./daten). */
    public static File ordner() {
        String p = System.getProperty("sylt.daten");
        return new File(p != null && !p.isEmpty() ? p : "daten");
    }

    private static void eigen(boolean mitgeliefert, String was) throws Fehler {
        if (mitgeliefert) throw new Fehler("„" + was + "“ ist mitgeliefert und wird nicht überschrieben; bitte unter einem eigenen Namen speichern");
    }

    // ------------------------------------------------------------------ CSV

    /** Eine CSV-Datei: Semikolon, Text in Anführungszeichen, erste Zeile die Spalten. */
    static final class Csv {
        final File f;
        final String[] spalten;
        List<LinkedHashMap<String, String>> zeilen;

        Csv(File f, String... spalten) { this.f = f; this.spalten = spalten; }

        synchronized List<LinkedHashMap<String, String>> lesen() throws Fehler {
            if (zeilen != null) return zeilen;
            zeilen = new ArrayList<>();
            if (!f.isFile()) return zeilen;
            try {
                List<String> l = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
                if (l.isEmpty()) return zeilen;
                String[] kopf = teile(l.get(0));
                for (int i = 1; i < l.size(); i++) {
                    if (l.get(i).isBlank()) continue;
                    String[] v = teile(l.get(i));
                    LinkedHashMap<String, String> m = new LinkedHashMap<>();
                    for (int k = 0; k < kopf.length; k++) m.put(kopf[k], k < v.length ? v[k] : "");
                    zeilen.add(m);
                }
                return zeilen;
            } catch (IOException e) {
                throw new Fehler(f + " nicht lesbar: " + e.getMessage(), e);
            }
        }

        synchronized void schreiben() throws Fehler {
            StringBuilder b = new StringBuilder(String.join(";", spalten)).append('\n');
            for (Map<String, String> m : zeilen) {
                for (int k = 0; k < spalten.length; k++) b.append(k > 0 ? ";" : "").append(feld(m.getOrDefault(spalten[k], "")));
                b.append('\n');
            }
            schreibe(f, b.toString());
        }

        /** Eine Zeile anlegen (id ≤ 0) oder ersetzen; gibt die Nummer zurück. */
        synchronized long setzen(long id, LinkedHashMap<String, String> m) throws Fehler {
            long n = id > 0 ? id : naechste();
            LinkedHashMap<String, String> z = new LinkedHashMap<>();
            z.put("ID", Long.toString(n));
            z.putAll(m);
            List<LinkedHashMap<String, String>> l = lesen();
            String sid = Long.toString(n);
            boolean da = false;
            for (int i = 0; i < l.size(); i++) if (sid.equals(l.get(i).get("ID"))) { l.set(i, z); da = true; break; }
            if (!da) l.add(z);
            schreiben();
            return n;
        }

        synchronized long naechste() throws Fehler {
            long n = 0;
            for (Map<String, String> m : lesen()) n = Math.max(n, (long) Zahl.lies(m.getOrDefault("ID", "0")));
            return n + 1;
        }

        static String feld(String v) {
            if (v == null) return "";
            if (v.indexOf(';') < 0 && v.indexOf('"') < 0 && v.indexOf('\n') < 0 && v.indexOf(',') < 0) return v;
            return "\"" + v.replace("\"", "\"\"").replace("\n", " ") + "\"";
        }

        static String[] teile(String z) {
            List<String> l = new ArrayList<>();
            StringBuilder b = new StringBuilder();
            boolean q = false;
            for (int i = 0; i < z.length(); i++) {
                char c = z.charAt(i);
                if (q) {
                    if (c == '"' && i + 1 < z.length() && z.charAt(i + 1) == '"') { b.append('"'); i++; }
                    else if (c == '"') q = false;
                    else b.append(c);
                } else if (c == '"') q = true;
                else if (c == ';') { l.add(b.toString()); b.setLength(0); }
                else b.append(c);
            }
            l.add(b.toString());
            return l.toArray(new String[0]);
        }
    }

    private Csv abbr, sturm, vors, serien, regie;

    private synchronized Csv ab() {
        if (abbr == null) abbr = new Csv(sub("chronik", "abbrueche.csv"), "ID", "SITZUNG", "SZENE_MS", "SZENE_TEXT", "NORD", "NORD_VON", "NORD_BIS", "LAENGE_M", "VOLUMEN_M3",
                "BLOECKE_M3", "ZERFALLEN_M3", "AUSGESPUELT_M3", "KEGEL_M3", "KEHLE_M", "LC_M", "PEGEL_M", "AUFLAUF_M", "RUECKGANG_M", "STURMFLUT", "WAND_MAX_M",
                "OBERKANTE_MAX_M", "FINDLINGE", "RUECKGANG_WKT", "OBERKANTE_WKT", "FUSS_WKT", "WKT");
        return abbr;
    }
    private synchronized Csv st() {
        if (sturm == null) sturm = new Csv(sub("chronik", "sturmfluten.csv"), "ID", "SITZUNG", "BELEGT", "NAME", "BEGINN_MS", "BEGINN_TEXT", "ENDE_MS", "SCHEITEL_M",
                "SCHEITEL_MS", "WINDSTAU_MAX_M", "AUFLAUF_MAX_M", "STUNDEN_AM_FUSS", "KEHLE_MAX_M", "ABBRUECHE");
        return sturm;
    }
    private synchronized Csv vo() {
        if (vors == null) vors = new Csv(sub("chronik", "vorspuelungen.csv"), "ID", "SITZUNG", "NAME", "JAHR", "MENGE_M3", "NORD_VON", "NORD_BIS", "STRECKE", "BEGINN_MS",
                "SZENE_TEXT", "HOEHE_FUSS_M", "BREITE_M", "HALBWERT_TAGE", "BEMERKUNG", "WKT");
        return vors;
    }
    private synchronized Csv se() {
        if (serien == null) serien = new Csv(sub("chronik", "serien.csv"), "ID", "SITZUNG", "SZENE_MS", "SZENE_TEXT", "DAUER_S", "WELLEN", "SERIEN", "GROESSTE", "HS_M",
                "TP_S", "X_M", "Z_M", "WKT");
        return serien;
    }
    private synchronized Csv rl() {
        if (regie == null) regie = new Csv(sub("chronik", "regielaeufe.csv"), "ID", "SITZUNG", "NAME", "BEGINN_MS", "ENDE_MS", "VOLLSTAENDIG", "KINO", "BILDER_S_MITTEL",
                "GPU_MS_MITTEL", "GEWARTET_S", "AUSGEWICHEN", "FOTOS", "QUALITAET", "BLATT_DATEI");
        return regie;
    }

    /** Die Dateien der Chronik (für die Übernahme). */
    public List<File> chronikDateien() { return List.of(ab().f, st().f, vo().f, se().f, rl().f); }

    private static String n(double v) { return Double.isNaN(v) ? "" : Zahl.text(Math.round(v * 1000) / 1000.0); }
    private static String n(Double v) { return v == null ? "" : n((double) v); }
    private static double d(String s) { return s == null || s.isEmpty() ? Double.NaN : Zahl.lies(s); }
    private static Double dd(String s) { return s == null || s.isEmpty() ? null : Zahl.lies(s); }
    private static Long ln(String s) { return s == null || s.isEmpty() ? null : (long) Zahl.lies(s); }
    private static Integer in(String s) { return s == null || s.isEmpty() ? null : (int) Zahl.lies(s); }
    private static String s(Object v) { return v == null ? "" : v.toString(); }
    private static String w(Daten.Geom g) { return g == null ? "" : g.wkt(); }
    private static String leer(String s) { return s == null || s.isEmpty() ? null : s; }

    // ------------------------------------------------------------------ Stand

    @Override public Daten.Stand stand() throws Fehler {
        int app = 0, findl = 0;
        for (Map<String, String> m : vo().lesen()) app++;
        for (Map<String, String> m : ab().lesen()) findl += m.getOrDefault("FINDLINGE", "").isEmpty() ? 0 : m.get("FINDLINGE").split(";").length;
        int gemerkt = 0;
        for (Daten.Ansicht a : ansichten()) if ("GEMERKT".equals(a.art())) gemerkt++;
        return new Daten.Stand(quellen().size(), befunde().size(), sturmfluten().size(), Startdaten.vorspuelungen().size(), Startdaten.kante2024(dir()).size(),
                ab().lesen().size(), findl, st().lesen().size(), app, se().lesen().size(), regiebuecher().size(), rl().lesen().size(), ansichten().size(), gemerkt,
                presets(null, null).size(), dateien(sub("sitzungen")).size(), letzteSitzung(-1));
    }

    @Override public List<Daten.Quelle> quellen() { return Startdaten.quellen(); }
    @Override public List<Daten.Befund> befunde() { return Startdaten.befunde(); }
    @Override public List<Daten.Sturmflut> sturmfluten() { return Startdaten.sturmfluten(); }
    @Override public List<Daten.Leuchtfeuer> leuchtfeuer() { return Startdaten.leuchtfeuer(); }
    @Override public List<Daten.Lage> lagen() { return Startdaten.lagen(); }

    // ------------------------------------------------------------------ Chronik

    /** Findlinge als „x z r; x z r“ (Szenenmeter). */
    static String findlinge(List<double[]> l) {
        StringBuilder b = new StringBuilder();
        for (double[] f : l) b.append(b.length() > 0 ? "; " : "").append(Zahl.text(Math.round(f[0] * 10) / 10.0)).append(' ').append(Zahl.text(Math.round(f[1] * 10) / 10.0))
                .append(' ').append(f.length > 2 ? Zahl.text(Math.round(f[2] * 10) / 10.0) : "0");
        return b.toString();
    }

    static List<double[]> findlinge(String s) {
        List<double[]> l = new ArrayList<>();
        if (s == null || s.isBlank()) return l;
        for (String p : s.split(";")) {
            String[] v = p.strip().split("\\s+");
            if (v.length >= 2) l.add(new double[]{Zahl.lies(v[0]), Zahl.lies(v[1]), v.length > 2 ? Zahl.lies(v[2]) : 0});
        }
        return l;
    }

    @Override public long abbruch(Daten.Abbruch a, Long sitzung) throws Fehler {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("SITZUNG", s(sitzung));
        m.put("SZENE_MS", Long.toString(a.ms()));
        m.put("SZENE_TEXT", Daten.text(a.ms()));
        m.put("NORD", n(a.nord()));
        m.put("NORD_VON", n(a.nordVon()));
        m.put("NORD_BIS", n(a.nordBis()));
        m.put("LAENGE_M", n(a.laenge()));
        m.put("VOLUMEN_M3", n(a.volumen()));
        m.put("BLOECKE_M3", n(a.bloecke()));
        m.put("ZERFALLEN_M3", n(a.zerfallen()));
        m.put("AUSGESPUELT_M3", n(a.ausgespuelt()));
        m.put("KEGEL_M3", n(a.kegel()));
        m.put("KEHLE_M", n(a.kehle()));
        m.put("LC_M", n(a.lc()));
        m.put("PEGEL_M", n(a.pegel()));
        m.put("AUFLAUF_M", n(a.auflauf()));
        m.put("RUECKGANG_M", n(a.rueckgang()));
        m.put("STURMFLUT", s(a.sturmflut()));
        Daten.Kantenstueck k = a.kante();
        m.put("WAND_MAX_M", k == null ? "" : n(k.wandMax()));
        m.put("OBERKANTE_MAX_M", k == null ? "" : n(k.oberkanteMax()));
        m.put("FINDLINGE", findlinge(a.findlinge()));
        m.put("RUECKGANG_WKT", w(a.rueckgangFlaeche()));
        m.put("OBERKANTE_WKT", k == null ? "" : w(k.oberkante()));
        m.put("FUSS_WKT", k == null ? "" : w(k.fuss()));
        m.put("WKT", a.bruchstelle() == null ? "" : Daten.Geom.punkt(a.bruchstelle()[0], a.bruchstelle()[1]).wkt());
        return ab().setzen(0, m);
    }

    private static Daten.Abbruch abbruchAus(Map<String, String> m) {
        long id = (long) d(m.get("ID")), ms = (long) d(m.get("SZENE_MS"));
        Daten.Geom bs = Daten.Geom.ausWkt(m.get("WKT")), ok = Daten.Geom.ausWkt(m.get("OBERKANTE_WKT"));
        Daten.Kantenstueck k = ok == null ? null : new Daten.Kantenstueck(id, "ABBRUCH", id, d(m.get("NORD_VON")), d(m.get("NORD_BIS")), ok,
                Daten.Geom.ausWkt(m.get("FUSS_WKT")), d(m.get("WAND_MAX_M")), d(m.get("OBERKANTE_MAX_M")), ms);
        return new Daten.Abbruch(id, ms, d(m.get("NORD")), d(m.get("NORD_VON")), d(m.get("NORD_BIS")), d(m.get("LAENGE_M")), d(m.get("VOLUMEN_M3")),
                d(m.get("BLOECKE_M3")), d(m.get("ZERFALLEN_M3")), d(m.get("AUSGESPUELT_M3")), d(m.get("KEGEL_M3")), d(m.get("KEHLE_M")), d(m.get("LC_M")),
                d(m.get("PEGEL_M")), d(m.get("AUFLAUF_M")), d(m.get("RUECKGANG_M")), leer(m.get("STURMFLUT")), bs == null ? null : bs.xz()[0],
                Daten.Geom.ausWkt(m.get("RUECKGANG_WKT")), k, findlinge(m.get("FINDLINGE")));
    }

    @Override public List<Daten.Abbruch> abbrueche(int n) throws Fehler {
        List<Daten.Abbruch> l = new ArrayList<>();
        Csv c = ab();
        synchronized (c) { for (Map<String, String> m : c.lesen()) l.add(abbruchAus(m)); }
        l.sort((a, b) -> a.ms() != b.ms() ? Long.compare(b.ms(), a.ms()) : Long.compare(b.id(), a.id()));
        return l.size() > n ? new ArrayList<>(l.subList(0, n)) : l;
    }

    /** Alle Abbrüche mit Sitzung (für die Übernahme), in der Reihenfolge der Datei. */
    public List<Daten.Abbruch> abbruecheAlle() throws Fehler {
        List<Daten.Abbruch> l = new ArrayList<>();
        Csv c = ab();
        synchronized (c) { for (Map<String, String> m : c.lesen()) l.add(abbruchAus(m)); }
        return l;
    }

    @Override public List<Daten.Kantenstueck> kanten() throws Fehler {
        List<Daten.Kantenstueck> l = new ArrayList<>(Startdaten.kante2024(dir()));
        List<Daten.Abbruch> a = abbruecheAlle();
        a.sort((x, y) -> Long.compare(x.ms(), y.ms()));
        for (Daten.Abbruch x : a) if (x.kante() != null) l.add(x.kante());
        return l;
    }

    @Override public long sturmflutLauf(Daten.SturmflutLauf l, Long sitzung) throws Fehler {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("SITZUNG", s(sitzung));
        m.put("BELEGT", s(l.belegt()));
        m.put("NAME", l.name());
        m.put("BEGINN_MS", Long.toString(l.beginnMs()));
        m.put("BEGINN_TEXT", Daten.text(l.beginnMs()));
        m.put("ENDE_MS", s(l.endeMs()));
        m.put("SCHEITEL_M", n(l.scheitel()));
        m.put("SCHEITEL_MS", s(l.scheitelMs()));
        m.put("WINDSTAU_MAX_M", n(l.windstauMax()));
        m.put("AUFLAUF_MAX_M", n(l.auflaufMax()));
        m.put("STUNDEN_AM_FUSS", n(l.stundenAmFuss()));
        m.put("KEHLE_MAX_M", n(l.kehleMax()));
        m.put("ABBRUECHE", Integer.toString(l.abbrueche()));
        return st().setzen(l.id(), m);
    }

    @Override public List<Daten.SturmflutLauf> sturmflutLaeufe(int n) throws Fehler {
        List<Daten.SturmflutLauf> l = new ArrayList<>();
        Csv c = st();
        synchronized (c) {
            for (Map<String, String> m : c.lesen())
                l.add(new Daten.SturmflutLauf((long) d(m.get("ID")), leer(m.get("BELEGT")), m.get("NAME"), (long) d(m.get("BEGINN_MS")), ln(m.get("ENDE_MS")),
                        d(m.get("SCHEITEL_M")), ln(m.get("SCHEITEL_MS")), d(m.get("WINDSTAU_MAX_M")), d(m.get("AUFLAUF_MAX_M")), d(m.get("STUNDEN_AM_FUSS")),
                        d(m.get("KEHLE_MAX_M")), (int) Math.max(0, d(m.get("ABBRUECHE")))));
        }
        Collections.reverse(l);
        return l.size() > n ? new ArrayList<>(l.subList(0, n)) : l;
    }

    @Override public long vorspuelung(Daten.Vorspuelung v, Long sitzung) throws Fehler {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("SITZUNG", s(sitzung));
        m.put("NAME", v.name());
        m.put("JAHR", s(v.jahr()));
        m.put("MENGE_M3", s(v.menge()));
        m.put("NORD_VON", n(v.nordVon()));
        m.put("NORD_BIS", n(v.nordBis()));
        m.put("STRECKE", s(v.strecke()));
        m.put("BEGINN_MS", s(v.beginnMs()));
        m.put("SZENE_TEXT", v.beginnMs() == null ? "" : Daten.text(v.beginnMs()));
        m.put("HOEHE_FUSS_M", n(v.hoeheFuss()));
        m.put("BREITE_M", n(v.breite()));
        m.put("HALBWERT_TAGE", n(v.halbwertTage()));
        m.put("BEMERKUNG", s(v.bemerkung()));
        m.put("WKT", w(v.flaeche()));
        return vo().setzen(v.id(), m);
    }

    @Override public List<Daten.Vorspuelung> vorspuelungen() throws Fehler {
        List<Daten.Vorspuelung> l = new ArrayList<>(Startdaten.vorspuelungen());
        List<Daten.Vorspuelung> e = new ArrayList<>();
        Csv c = vo();
        synchronized (c) {
            for (Map<String, String> m : c.lesen())
                e.add(new Daten.Vorspuelung((long) d(m.get("ID")), m.get("NAME"), "APP", in(m.get("JAHR")), ln(m.get("MENGE_M3")), dd(m.get("NORD_VON")), dd(m.get("NORD_BIS")),
                        leer(m.get("STRECKE")), Daten.Geom.ausWkt(m.get("WKT")), ln(m.get("BEGINN_MS")), dd(m.get("HOEHE_FUSS_M")), dd(m.get("BREITE_M")),
                        dd(m.get("HALBWERT_TAGE")), leer(m.get("BEMERKUNG")), null));
        }
        Collections.reverse(e);
        l.addAll(e);
        return l;
    }

    @Override public void serie(Daten.Serie x, Long sitzung) throws Fehler {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("SITZUNG", s(sitzung));
        m.put("SZENE_MS", Long.toString(x.ms()));
        m.put("SZENE_TEXT", Daten.text(x.ms()));
        m.put("DAUER_S", n(x.dauerS()));
        m.put("WELLEN", Integer.toString(x.wellen()));
        m.put("SERIEN", Integer.toString(x.serien()));
        m.put("GROESSTE", Integer.toString(x.groesste()));
        m.put("HS_M", n(x.hs()));
        m.put("TP_S", n(x.tp()));
        m.put("X_M", n(Math.round(x.x() * 10) / 10.0));
        m.put("Z_M", n(Math.round(x.z() * 10) / 10.0));
        m.put("WKT", Daten.Geom.punkt(x.x(), x.z()).wkt());
        se().setzen(0, m);
    }

    /** Alle Serien (für die Übernahme). */
    public List<Daten.Serie> serienAlle() throws Fehler {
        List<Daten.Serie> l = new ArrayList<>();
        for (Map<String, String> m : se().lesen())
            l.add(new Daten.Serie((long) d(m.get("SZENE_MS")), d(m.get("DAUER_S")), (int) d(m.get("WELLEN")), (int) d(m.get("SERIEN")), (int) d(m.get("GROESSTE")),
                    d(m.get("HS_M")), d(m.get("TP_S")), d(m.get("X_M")), d(m.get("Z_M"))));
        return l;
    }

    @Override public void regielauf(Daten.Regielauf l, Long sitzung) throws Fehler {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("SITZUNG", s(sitzung));
        m.put("NAME", l.name());
        m.put("BEGINN_MS", Long.toString(l.beginnMs()));
        m.put("ENDE_MS", s(l.endeMs()));
        m.put("VOLLSTAENDIG", l.vollstaendig() ? "1" : "0");
        m.put("KINO", l.kino() ? "1" : "0");
        m.put("BILDER_S_MITTEL", n(l.bilderMittel()));
        m.put("GPU_MS_MITTEL", n(l.gpuMs()));
        m.put("GEWARTET_S", n(l.gewartetS()));
        m.put("AUSGEWICHEN", Integer.toString(l.ausgewichen()));
        m.put("FOTOS", Integer.toString(l.fotos()));
        m.put("QUALITAET", s(l.qualitaet()));
        m.put("BLATT_DATEI", s(l.blattDatei()));
        rl().setzen(0, m);
    }

    public List<Daten.Regielauf> regielaeufe() throws Fehler {
        List<Daten.Regielauf> l = new ArrayList<>();
        for (Map<String, String> m : rl().lesen())
            l.add(new Daten.Regielauf(m.get("NAME"), (long) d(m.get("BEGINN_MS")), ln(m.get("ENDE_MS")), "1".equals(m.get("VOLLSTAENDIG")), "1".equals(m.get("KINO")),
                    d(m.get("BILDER_S_MITTEL")), d(m.get("GPU_MS_MITTEL")), d(m.get("GEWARTET_S")), (int) Math.max(0, d(m.get("AUSGEWICHEN"))),
                    (int) Math.max(0, d(m.get("FOTOS"))), leer(m.get("QUALITAET")), null, leer(m.get("BLATT_DATEI"))));
        return l;
    }

    @Override public int chronikLeeren(long sitzung) throws Fehler {
        int n = 0;
        String sid = Long.toString(sitzung);
        for (Csv c : new Csv[]{ab(), st(), vo(), se(), rl()}) {
            synchronized (c) {
                int vor = c.lesen().size();
                c.lesen().removeIf(m -> sid.equals(m.get("SITZUNG")));
                n += vor - c.lesen().size();
                if (vor != c.lesen().size()) c.schreiben();
            }
        }
        return n;
    }

    // ------------------------------------------------------------------ Regiebücher, Fahrten

    @Override public List<Daten.Regiebuch> regiebuecher() throws Fehler {
        List<Daten.Regiebuch> l = new ArrayList<>(Startdaten.regiebuecher());
        for (File f : dateien(sub("regiebuecher"))) {
            try {
                String t = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                Regie.Book b = Regie.parse(t);
                l.add(new Daten.Regiebuch(b.name(), t, b.about(), null, Daten.EIGEN));
            } catch (IOException | RuntimeException e) {
                com.dan.store.Protokoll.warnung("Dateien", "Regiebuch " + f.getName() + " nicht lesbar: " + e.getMessage());
            }
        }
        return l;
    }

    private static boolean mitgeliefertesBuch(String name) {
        for (Daten.Regiebuch r : Startdaten.regiebuecher()) if (r.name().equals(name)) return true;
        return false;
    }

    @Override public void speichern(Daten.Regiebuch r) throws Fehler {
        eigen(mitgeliefertesBuch(r.name()), r.name());
        try { Regie.parse(r.text()); } catch (RuntimeException e) { throw new Fehler("Regiebuch nicht lesbar: " + e.getMessage(), e); }
        schreibe(sub("regiebuecher", dateiname(r.name())), r.text());
    }

    @Override public void loeschenRegiebuch(String name) throws Fehler { eigen(mitgeliefertesBuch(name), name); loeschen("regiebuecher", name); }

    @Override public List<Daten.Fahrt> fahrten() { return Startdaten.fahrten(); }

    // ------------------------------------------------------------------ Ansichten

    private static final String[] KAM = {"x", "y", "z", "gier", "nick"};

    private static Daten.Ansicht ansichtAus(Map<String, String> m, String zustand, byte[] png) {
        double[] k = new double[5];
        for (int i = 0; i < 5; i++) k[i] = Zahl.lies(m.get("kamera." + KAM[i]));
        String sm = m.get("szeneMs");
        double oe = Zahl.lies(m.get("oeffnung"));
        return new Daten.Ansicht(Long.parseLong(m.get("id")), m.get("name"), null, "GEMERKT", k, Double.isNaN(oe) ? null : oe, null,
                sm == null || sm.isEmpty() ? null : Long.parseLong(sm), zustand, png, Daten.EIGEN);
    }

    @Override public List<Daten.Ansicht> ansichten() throws Fehler {
        List<Daten.Ansicht> l = new ArrayList<>(Startdaten.ansichten());
        List<Daten.Ansicht> g = new ArrayList<>();
        for (File f : dateien(sub("ansichten"))) {
            LinkedHashMap<String, String> m = lies(f);
            if (m.get("id") != null) g.add(ansichtAus(m, null, null));
        }
        g.sort((a, b) -> Long.compare(b.id(), a.id()));
        l.addAll(g);
        return l;
    }

    @Override public Daten.Ansicht ansicht(long id) throws Fehler {
        File f = sub("ansichten", id + ".txt");
        if (!f.isFile()) throw new Fehler("Ansicht " + id + " gibt es nicht");
        LinkedHashMap<String, String> m = lies(f);
        File z = sub("ansichten", id + ".zustand"), p = sub("ansichten", id + ".png");
        try {
            return ansichtAus(m, z.isFile() ? Files.readString(z.toPath(), StandardCharsets.UTF_8) : null, p.isFile() ? Files.readAllBytes(p.toPath()) : null);
        } catch (IOException e) {
            throw new Fehler("Ansicht " + id + " nicht lesbar: " + e.getMessage(), e);
        }
    }

    @Override public long speichern(Daten.Ansicht a, Long sitzung) throws Fehler {
        long id = naechsteNummer(sub("ansichten"));
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("id", Long.toString(id));
        m.put("name", a.name());
        for (int i = 0; i < 5; i++) m.put("kamera." + KAM[i], Zahl.text(a.kamera()[i]));
        if (a.oeffnung() != null) m.put("oeffnung", Zahl.text(a.oeffnung()));
        if (a.szeneMs() != null) { m.put("szeneMs", a.szeneMs().toString()); m.put("szeneText", Daten.text(a.szeneMs())); }
        if (sitzung != null) m.put("sitzung", sitzung.toString());
        schreibe(sub("ansichten", id + ".txt"), Felder.alsText(m, "Ansicht " + a.name()));
        if (a.zustand() != null) schreibe(sub("ansichten", id + ".zustand"), a.zustand());
        if (a.vorschau() != null) schreibe(sub("ansichten", id + ".png"), a.vorschau());
        return id;
    }

    @Override public void loeschenAnsicht(long id) throws Fehler {
        loesche(sub("ansichten", id + ".txt"), "Ansicht " + id);
        loesche(sub("ansichten", id + ".zustand"), "Ansicht " + id);
        loesche(sub("ansichten", id + ".png"), "Ansicht " + id);
    }

    // ------------------------------------------------------------------ Presets

    @Override public List<Daten.PaketPreset> presets(String paket, String klasse) throws Fehler {
        List<Daten.PaketPreset> l = new ArrayList<>();
        for (Daten.PaketPreset p : Startdaten.presets())
            if ((paket == null || p.paket().equals(paket)) && (klasse == null || p.klasse().equals(klasse))) l.add(p);
        File base = sub("presets");
        File[] ps = base.listFiles(File::isDirectory);
        if (ps == null) return l;
        for (File pd : ps) {
            if (paket != null && !pd.getName().equals(paket)) continue;
            File[] ks = pd.listFiles(File::isDirectory);
            if (ks == null) continue;
            for (File kd : ks) {
                if (klasse != null && !kd.getName().equals(klasse)) continue;
                for (File f : dateien(kd)) {
                    LinkedHashMap<String, String> m = lies(f);
                    String name = m.remove("#name"), about = m.remove("#beschreibung");
                    l.add(new Daten.PaketPreset(pd.getName(), kd.getName(), name == null ? f.getName().replace(".txt", "") : name, about, Daten.EIGEN, m));
                }
            }
        }
        return l;
    }

    private static boolean presetMitgeliefert(String paket, String klasse, String name) {
        for (Daten.PaketPreset p : Startdaten.presets()) if (p.paket().equals(paket) && p.klasse().equals(klasse) && p.name().equals(name)) return true;
        return false;
    }

    @Override public void speichern(Daten.PaketPreset p) throws Fehler {
        eigen(presetMitgeliefert(p.paket(), p.klasse(), p.name()), p.name());
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("#name", p.name());
        m.put("#beschreibung", p.beschreibung() == null ? "" : p.beschreibung());
        m.putAll(p.felder());
        schreibe(sub("presets", p.paket(), p.klasse(), dateiname(p.name())), Felder.alsText(m, "Preset " + p.paket() + "." + p.klasse() + " · " + p.name()));
    }

    @Override public void loeschenPreset(String paket, String klasse, String name) throws Fehler {
        eigen(presetMitgeliefert(paket, klasse, name), name);
        loesche(sub("presets", paket, klasse, dateiname(name)), name);
    }

    // ------------------------------------------------------------------ Sitzung

    @Override public void vorschau(long sitzung, byte[] png) throws Fehler { schreibe(sub("sitzungen", sitzung + ".png"), png); }

    @Override public Daten.Sitzung letzteSitzung(long ausser) throws Fehler {
        LinkedHashMap<String, String> m = super.letzteSitzungText(ausser);
        if (m == null) return null;
        String ende = m.get("ende"), zz = m.get("zustandZeit");
        double rs = Zahl.lies(m.get(REGIE_SEKUNDE)), ms = Zahl.lies(m.get(SZENE_MS)), pg = Zahl.lies(m.get(PEGEL)), ab = Zahl.lies(m.get(ABBRUECHE));
        long id = Long.parseLong(m.get("id"));
        File png = sub("sitzungen", id + ".png");
        byte[] bild = null;
        try { if (png.isFile()) bild = Files.readAllBytes(png.toPath()); } catch (IOException e) { /* ohne Bild */ }
        return new Daten.Sitzung(id, Long.parseLong(m.get("beginn")), ende == null || ende.isEmpty() ? null : Long.parseLong(ende), "true".equals(m.get("sauber")),
                m.get("zustand"), zz == null ? null : Long.parseLong(zz), leer(m.get(REGIEBUCH)), Double.isNaN(rs) ? null : rs, Double.isNaN(ms) ? null : (long) ms,
                leer(m.get(LAGE)), Double.isNaN(pg) ? null : pg, Double.isNaN(ab) ? null : (int) ab, "true".equals(m.get(STURMFLUT)), bild);
    }

    /** „Dateien in daten (3 Abbrüche, …)“ für Prüfungen. */
    public String beschreibung() throws Fehler {
        return String.format(Locale.GERMAN, "%s: %d Abbrüche, %d Sturmfluten, %d Vorspülungen, %d Serien, %d Läufe", wo(), ab().lesen().size(), st().lesen().size(),
                vo().lesen().size(), se().lesen().size(), rl().lesen().size());
    }
}
