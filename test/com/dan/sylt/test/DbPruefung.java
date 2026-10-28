package com.dan.sylt.test;

import com.dan.sylt.data.DateiSpeicher;
import com.dan.sylt.data.Daten;
import com.dan.sylt.data.DbSpeicher;
import com.dan.sylt.data.Kliffkarte;
import com.dan.sylt.data.Speicher;
import com.dan.sylt.data.Startdaten;
import com.dan.sylt.data.Uebernahme;
import com.dan.sylt.data.Verbindung;
import com.dan.sylt.scene.Kante;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Site;
import com.dan.store.Ablage;
import com.dan.store.Db;
import com.dan.store.Einrichter;
import com.dan.store.Felder;
import com.dan.store.Protokoll;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Phase 16: die Prüfung gegen die echte Datenbank (PDBORCL, Schema SYLT aus {@code db/db.properties}). In NetBeans:
 * Rechtsklick, „Run File“, oder {@code ant dbpruefung}. Sie richtet ein (wiederholbar), läuft den Einrichter ein
 * zweites Mal (muss ohne Fehler durchgehen), prüft mit ihm, rechnet Kante, Kern und den Abstand der Türme nach (in
 * 25832 und über die Sichten in 8307), prüft alle Geometrien auf Gültigkeit, die belegten Sturmfluten und die Sektoren,
 * schreibt eine Chronik für den 31.12.2099 (Abbruch mit Kante, Rückgang und Findling, Lauf einer Sturmflut, Vorspülung,
 * Serie, Lauf eines Regiebuchs mit Blatt) und liest sie über die Sichten zurück – die Kante zu jedem Zeitpunkt
 * (SY_KANTE_V), die Szenenzeit als TIMESTAMP WITH TIME ZONE, die Kliffkarte –, speichert Regiebuch, Ansicht und Preset
 * unter „~Prüfung …“, sichert eine Sitzung mit Vorschau, übernimmt Dateien und räumt hinterher auf.
 * Ende: „Alles in Ordnung.“ Die Ausgabe bitte als Text schicken; jede Zeile mit FEHLER sagt, was nicht ging.
 */
public final class DbPruefung {
    private static int ok, bad;
    private static final String P = "~Prüfung ";
    /** Der Tag der Prüfchronik: kommt in keiner Szene vor. */
    static final LocalDate TAG = LocalDate.of(2099, 12, 31);

    static void check(boolean c, String was) {
        if (c) ok++; else bad++;
        System.out.println((c ? "  ok     " : "  FEHLER ") + was);
    }

    static double zahl(Connection c, String sql) throws Exception {
        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(sql)) { return r.next() ? r.getDouble(1) : Double.NaN; }
    }

    static String text(Connection c, String sql) throws Exception {
        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(sql)) { return r.next() ? r.getString(1) : null; }
    }

    static long ms(LocalDate d, int h, int m) { return ZonedDateTime.of(d.getYear(), d.getMonthValue(), d.getDayOfMonth(), h, m, 0, 0, SceneClock.ZONE).toInstant().toEpochMilli(); }

    /** Ein Abbruch zum Prüfen: 30 m Kante ab Nord n, die Kante um rueck m zurück, ein Findling vor dem Fuß. */
    static Daten.Abbruch abbruch(Kante k, double nord, double rueck, long t) {
        int r0 = k.rowNear(Site.N0 - nord, 20), n = 31;
        double[][] vor = new double[n][], nach = new double[n][], fuss = new double[n][];
        for (int i = 0; i < n; i++) {
            int r = Math.min(k.n - 1, r0 + i);
            vor[i] = new double[]{k.kanteX(r), k.z(r)};
            nach[i] = new double[]{k.kanteX(r) + rueck * Math.sin(Math.PI * i / (n - 1)), k.z(r)};
            fuss[i] = new double[]{k.fussX(r) + 2, k.z(r)};
        }
        double[][] p = new double[2 * n][];
        for (int i = 0; i < n; i++) { p[i] = vor[i]; p[n + i] = nach[n - 1 - i]; }
        double nv = k.nord[r0], nb = k.nord[Math.min(k.n - 1, r0 + n - 1)];
        Daten.Kantenstueck ks = new Daten.Kantenstueck(0, "ABBRUCH", null, Math.max(nv, nb), Math.min(nv, nb), Daten.Geom.linie(nach), Daten.Geom.linie(fuss), 28.4, 30.1, t);
        double[] mitte = vor[n / 2];
        return new Daten.Abbruch(0, t, k.nord[r0 + n / 2], Math.max(nv, nb), Math.min(nv, nb), 30, 412.5, 280.2, 120.3, 12, 590.4, 2.8, 2.2, 3.41, 1.92, rueck,
                P + "Sturmflut", mitte.clone(), Daten.Geom.flaeche(p), ks, List.of(new double[]{mitte[0] - 40, mitte[1], 9}));
    }

    public static void main(String[] args) throws Exception {
        long t0 = System.nanoTime();
        System.out.println("Sylt · Prüfung der Datenbank (Phase 16)");
        Db.Konfig k = Verbindung.konfig();
        System.out.println("Verbindung " + k.kurz());
        if (!k.hatPasswort()) {
            System.out.println("  FEHLER Kein Passwort in db/db.properties und keine Umgebungsvariable " + Verbindung.ENV
                    + ". Einmal mit dem Startbild verbinden (es trägt das Passwort ein) oder es von Hand eintragen.");
            System.exit(1);
        }
        DbSpeicher db = new DbSpeicher(k);
        System.out.println(db.wo());
        Connection c = db.verbindung();
        Einrichter ein = Verbindung.einrichter();

        System.out.println("Einrichter");
        Einrichter.Ergebnis e1 = ein.einrichten(c, Verbindung.skripte(), (t, a) -> { });
        for (String z : e1.zeilen()) if (!z.startsWith("ok")) System.out.println("    " + z);
        check(e1.ok(), "Erster Lauf: " + e1.kurz() + " (" + e1.ausgefuehrt() + " ausgeführt, " + e1.vorhanden() + " schon da)");
        Einrichter.Ergebnis e2 = ein.einrichten(c, Verbindung.skripte(), (t, a) -> { });
        check(e2.ok() && e2.fehler() == 0, "Zweiter Lauf geht ohne Fehler durch (wiederholbar): " + e2.kurz());
        Einrichter.Ergebnis pr = ein.pruefen(c, Verbindung.skripte());
        for (String z : pr.zeilen()) System.out.println("    " + z);
        check(pr.ok(), "Einrichter prüft: " + pr.kurz());
        check(ein.stufe(c) == Verbindung.VERSION, "Ausbaustufe " + ein.stufe(c));

        System.out.println("Trigger, Geometrien, Sichten in 8307");
        try (Statement st = c.createStatement()) {
            st.executeUpdate("INSERT INTO SY_SEEGANG (NAME, HERKUNFT) VALUES ('" + P + "Trigger', 'EIGEN')");
            try (ResultSet r = st.executeQuery("SELECT ID, ANGELEGT, GEAENDERT FROM SY_SEEGANG WHERE NAME = '" + P + "Trigger'")) {
                check(r.next() && r.getLong(1) > 0 && r.getTimestamp(2) != null && r.getTimestamp(3) != null, "Trigger setzt ID, ANGELEGT und GEAENDERT");
            }
            st.executeUpdate("DELETE FROM SY_SEEGANG WHERE NAME = '" + P + "Trigger'");
        }
        Kante kante = Kante.load(new File("daten"));
        List<Daten.Kantenstueck> k24 = Startdaten.kante2024(kante);
        double laengeSzene = 0;
        for (Daten.Kantenstueck s : k24) laengeSzene += s.oberkante().laenge();
        double laengeDb = zahl(c, "SELECT SUM(MDSYS.SDO_GEOM.SDO_LENGTH(OBERKANTE, 0.005)) FROM SY_KLIFFKANTE WHERE STAND = 'GEMESSEN_2024'"),
                kern = zahl(c, "SELECT MDSYS.SDO_GEOM.SDO_AREA(KERN, 0.005) / 1e6 FROM SY_GELAENDE WHERE NAME = 'Rotes Kliff'"),
                tuerme = zahl(c, "SELECT MDSYS.SDO_GEOM.SDO_DISTANCE(a.GEOM, b.GEOM, 0.05, 'unit=M') FROM SY_LEUCHTFEUER_V a, SY_LEUCHTFEUER_V b "
                        + "WHERE a.NAME LIKE 'Kampen%' AND b.NAME LIKE 'Quermarke%'"),
                tuermeUtm = Math.hypot(Site.LEUCHTTURM.e() - Site.QUERMARKE.e(), Site.LEUCHTTURM.n() - Site.QUERMARKE.n());
        check(Math.abs(laengeDb / laengeSzene - 1) < 0.001 && Math.abs(kern - 8.28) < 0.01 && Math.abs(tuerme / tuermeUtm - 1) < 0.002, String.format(Locale.GERMAN,
                "Kante 2024 in %d Stücken %.3f km (Szene %.3f km), Kern %.2f km² (in 25832); Türme %.1f m auseinander auf dem Ellipsoid (SY_LEUCHTFEUER_V in 8307, UTM %.1f m)",
                k24.size(), laengeDb / 1000, laengeSzene / 1000, kern, tuerme, tuermeUtm));
        double ungueltig = 0;
        for (String m : Verbindung.METADATEN) {
            String[] tc = m.split("\\.");
            double u = zahl(c, "SELECT COUNT(*) FROM " + tc[0] + " WHERE " + tc[1] + " IS NOT NULL AND MDSYS.SDO_GEOM.VALIDATE_GEOMETRY_WITH_CONTEXT(" + tc[1] + ", 0.005) <> 'TRUE'");
            if (u > 0) System.out.println("    ungültig in " + m + ": " + (int) u + " · " + text(c, "SELECT MDSYS.SDO_GEOM.VALIDATE_GEOMETRY_WITH_CONTEXT(" + tc[1]
                    + ", 0.005) FROM " + tc[0] + " WHERE " + tc[1] + " IS NOT NULL AND MDSYS.SDO_GEOM.VALIDATE_GEOMETRY_WITH_CONTEXT(" + tc[1] + ", 0.005) <> 'TRUE' AND ROWNUM = 1"));
            ungueltig += u;
        }
        check(ungueltig == 0, "Alle Geometrien gültig (VALIDATE_GEOMETRY_WITH_CONTEXT) in " + Verbindung.METADATEN.length + " Spalten");
        double v8307 = zahl(c, "SELECT COUNT(*) FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME LIKE 'SY\\_%\\_V' ESCAPE '\\' AND SRID = 8307");
        double lon = zahl(c, "SELECT v.GEOM.SDO_POINT.X FROM SY_LEUCHTFEUER_V v WHERE v.NAME LIKE 'Quermarke%'");
        check(v8307 == Verbindung.SICHT_METADATEN.length && Math.abs(lon - Site.latLon(Site.QUERMARKE.x(), Site.QUERMARKE.z())[1]) < 1e-5,
                String.format(Locale.GERMAN, "%.0f Sichten mit Metadaten in 8307; Quermarkenfeuer in SY_LEUCHTFEUER_V bei %.6f° O (aus UTM umgerechnet)", v8307, lon));
        String sek = text(c, "SELECT SEKTOREN FROM SY_LEUCHTFEUER_V WHERE NAME LIKE 'Kampen%'");
        check(sek != null && sek.contains("R 105") && sek.contains("W 120"), "Sektoren des Kampener Leuchtturms: " + sek);
        double sf = zahl(c, "SELECT COUNT(*) FROM SY_STURMFLUT"), hoechst = zahl(c, "SELECT MAX(SCHEITEL_LIST_CM) FROM SY_STURMFLUT WHERE BEZUG = 'NHN'");
        String xaver = text(c, "SELECT NAME FROM SY_STURMFLUT WHERE DATUM = DATE '2013-12-06'");
        check(sf == Startdaten.sturmfluten().size() && hoechst == 405 && xaver != null, String.format(Locale.GERMAN,
                "%.0f belegte Sturmfluten seit 1962; höchster Scheitel List NHN +%.2f m (24.11.1981); %s", sf, hoechst / 100, xaver));
        String befund = text(c, "SELECT QUELLEN FROM SY_BEFUND_V WHERE GEGENSTAND = 'Quermarkenfeuer'");
        check(befund != null && befund.contains("WIKI_QUERMARKE"), "SY_BEFUND_V mit Quellen: „Quermarkenfeuer“ ← " + befund);

        System.out.println("Sitzung und Chronik (31.12.2099)");
        long sid = db.sitzungBeginnen(P.strip(), "Prüfung", "MITTEL");
        Long s = sid;
        long ta = ms(TAG, 3, 30);
        long lid = db.sturmflutLauf(new Daten.SturmflutLauf(0, xaver, P + "Sturmflut", ms(TAG, 0, 0), null, 2.1, null, 1.4, 1.6, 0, 1.2, 0), s);
        Daten.Abbruch a = abbruch(kante, 6_089_480, 3.0, ta);
        long aid = db.abbruch(a, s);
        db.sturmflutLauf(new Daten.SturmflutLauf(lid, xaver, P + "Sturmflut", ms(TAG, 0, 0), ms(TAG, 18, 0), 3.62, ms(TAG, 2, 45), 2.95, 2.1, 7.5, 3.4, 1), s);
        double laufAb = zahl(c, "SELECT STURMFLUT_LAUF_ID FROM SY_ABBRUCH WHERE ID = " + aid);
        String laufZeit = text(c, "SELECT TO_CHAR(BEGINN_ZEIT, 'DD.MM.YYYY HH24:MI TZH:TZM') || ' · ' || TO_CHAR(ENDE_ZEIT, 'HH24:MI') FROM SY_STURMFLUT_LAUF WHERE ID = " + lid);
        String belegt = text(c, "SELECT s.NAME FROM SY_STURMFLUT_LAUF l JOIN SY_STURMFLUT s ON s.ID = l.STURMFLUT_ID WHERE l.ID = " + lid);
        check(laufAb == lid && "31.12.2099 00:00 +01:00 · 18:00".equals(laufZeit) && xaver.equals(belegt),
                "Lauf der Sturmflut angelegt und beendet: " + laufZeit + " (TIMESTAMP WITH TIME ZONE), zur belegten „" + belegt + "“; der Abbruch hängt an ihm");
        double flaecheDb = zahl(c, "SELECT FLAECHE_M2 FROM SY_RUECKGANG_V WHERE ID = " + aid), findl = zahl(c, "SELECT FINDLINGE FROM SY_ABBRUCH_V WHERE ID = " + aid),
                kid = zahl(c, "SELECT ID FROM SY_KLIFFKANTE WHERE ABBRUCH_ID = " + aid);
        check(Math.abs(flaecheDb - a.rueckgangFlaeche().flaecheM2()) < 1 && findl == 1 && kid > 0, String.format(Locale.GERMAN,
                "Abbruch %d: Rückgang als Fläche %.1f m² (Szene %.1f m²), ein Findling, das neue Stück Kante %.0f", aid, flaecheDb, a.rueckgangFlaeche().flaecheM2(), kid));
        // die Kante zu jedem Zeitpunkt (F36 A): die Stücke 2024, deren Mitte der Abbruch trifft, gelten bis zu ihm, das neue danach
        String imAbschnitt = "(k.NORD_VON + k.NORD_BIS) / 2 BETWEEN " + a.nordBis() + " AND " + a.nordVon();
        double ersetzt = zahl(c, "SELECT COUNT(*) FROM SY_KANTE_V k WHERE k.STAND = 'GEMESSEN_2024' AND " + imAbschnitt + " AND k.GUELTIG_BIS_MS <= " + ta),
                vorher = zahl(c, "SELECT COUNT(*) FROM SY_KANTE_V WHERE GUELTIG_AB_MS <= " + (ta - 1) + " AND (GUELTIG_BIS_MS IS NULL OR GUELTIG_BIS_MS > " + (ta - 1) + ")"),
                weg = zahl(c, "SELECT COUNT(*) FROM SY_KANTE_V k WHERE " + imAbschnitt + " AND k.GUELTIG_AB_MS <= " + (ta - 1) + " AND (k.GUELTIG_BIS_MS IS NULL OR k.GUELTIG_BIS_MS > " + (ta - 1) + ")"),
                nachher = zahl(c, "SELECT COUNT(*) FROM SY_KANTE_V WHERE GUELTIG_AB_MS <= " + ta + " AND (GUELTIG_BIS_MS IS NULL OR GUELTIG_BIS_MS > " + ta + ")"),
                neuGilt = zahl(c, "SELECT COUNT(*) FROM SY_KANTE_V WHERE ABBRUCH_ID = " + aid + " AND GUELTIG_AB_MS = " + ta + " AND GUELTIG_BIS_MS IS NULL");
        check(ersetzt >= 1 && weg >= 1 && nachher == vorher - weg + 1 && neuGilt == 1, String.format(Locale.GERMAN,
                "SY_KANTE_V: um %s gelten %.0f Stücke, danach %.0f (%.0f Stücke 2024 bei N %,.0f ersetzt durch das neue)", Daten.text(ta), vorher, nachher, weg, a.nord()));
        Daten.Vorspuelung v = new Daten.Vorspuelung(0, P + "Vorspülung", "APP", 2099, 61_000L, 6_089_250.0, 6_089_850.0, "vor dem Kliff", Daten.rechteck(-1300, 1400, -1230, 2000),
                ms(TAG, 6, 0), 2.5, 70.0, 60.0, "Prüfung", null);
        long vid = db.vorspuelung(v, s);
        double vf = zahl(c, "SELECT FLAECHE_M2 FROM SY_VORSPUELUNG_V WHERE ID = " + vid);
        db.serie(new Daten.Serie(ms(TAG, 7, 0), 600, 72, 5, 4, 1.6, 9, -1250, 1500), s);
        byte[] png = new byte[7000];
        for (int i = 0; i < png.length; i++) png[i] = (byte) (i * 13);
        long now = System.currentTimeMillis();
        db.regielauf(new Daten.Regielauf("Ein Abend am Kliff", now - 260_000, now, true, true, 53.2, 9.4, 21, 0, 9, "MITTEL", png, "docs/status/x.png"), s);
        double blob = zahl(c, "SELECT DBMS_LOB.GETLENGTH(BLATT) FROM SY_REGIELAUF WHERE SITZUNG_ID = " + sid),
                rbid = zahl(c, "SELECT COUNT(*) FROM SY_REGIELAUF WHERE SITZUNG_ID = " + sid + " AND REGIEBUCH_ID IS NOT NULL AND KINO = 1");
        double kueste = zahl(c, "SELECT COUNT(*) FROM SY_KUESTE_V WHERE ZEIT_MS BETWEEN " + ms(TAG, 0, 0) + " AND " + ms(TAG, 23, 59));
        String ebenen = text(c, "SELECT LISTAGG(EBENE, ',') WITHIN GROUP (ORDER BY EBENE) FROM (SELECT DISTINCT EBENE FROM SY_KUESTE_V WHERE ZEIT_MS BETWEEN "
                + ms(TAG, 0, 0) + " AND " + ms(TAG, 23, 59) + ")");
        check(Math.abs(vf - 70 * 600) < 1 && blob == png.length && rbid == 1 && kueste == 6 && "ABBRUCH,FINDLING,KANTE_ABBRUCH,RUECKGANG,SERIE,VORSPUELUNG".equals(ebenen),
                String.format(Locale.GERMAN, "Vorspülung %.0f m², Serie, Lauf eines Regiebuchs mit Blatt (BLOB %d Bytes) im Kino; SY_KUESTE_V am 31.12.2099: %.0f Objekte (%s)",
                        vf, (int) blob, kueste, ebenen));
        String jahr = text(c, "SELECT TO_CHAR(JAHR) || ':' || TO_CHAR(ABBRUECHE) FROM SY_RUECKGANG_JAHR_V WHERE JAHR = 2099");
        check("2099:1".equals(jahr), "SY_RUECKGANG_JAHR_V: " + jahr);
        List<Daten.Abbruch> al = db.abbrueche(5);
        Daten.Abbruch zur = al.stream().filter(x -> x.id() == aid).findFirst().orElse(null);
        check(zur != null && zur.kante() != null && Math.abs(zur.rueckgangFlaeche().flaecheM2() - a.rueckgangFlaeche().flaecheM2()) < 0.5
                        && Math.abs(zur.bruchstelle()[0] - a.bruchstelle()[0]) < 0.02 && zur.kante().oberkante().punkte() == a.kante().oberkante().punkte(),
                "Abbruch zurückgelesen: Bruchstelle, Fläche und Kante aus WKT wieder in Szenenmetern (auf 2 cm gleich)");
        File tmp = Files.createTempDirectory("sylt-dbpruefung").toFile();
        File karte = Kliffkarte.schreiben(db, tmp);
        check(karte.isFile() && karte.length() > 20_000, "Kliffkarte aus der Datenbank: " + karte.getName() + " (" + karte.length() / 1024 + " KB)");

        System.out.println("Regiebuch, Ansicht, Preset, Sitzung");
        String rt = "Regiebuch: " + P + "Abend\nLänge: 0:30\n0:00 zeit 20.07.2099 20:30 | Abend\n0:05 ansicht Am Kliff\n0:20 foto Abend\n0:29 blatt\n";
        db.speichern(new Daten.Regiebuch(P + "Abend", rt, "", null, Daten.EIGEN));
        long ans = db.speichern(new Daten.Ansicht(0, P + "Ansicht", null, "GEMERKT", new double[]{-1280, 32, 1520, 72, -4}, 50.0, null, ms(TAG, 20, 15), "a=1\nb=2\n", png,
                Daten.EIGEN), s);
        var surf = com.dan.surf.Surf.sylt();
        surf.hs = 2.75;
        db.speichern(new Daten.PaketPreset("com.dan.surf", "Surf", P + "See", "Prüfung", Daten.EIGEN, Felder.lesen(surf)));
        Daten.Regiebuch rb = db.regiebuecher().stream().filter(x -> x.name().equals(P + "Abend")).findFirst().orElseThrow();
        Daten.Ansicht an = db.ansicht(ans);
        double sw = zahl(c, "SELECT COUNT(*) FROM SY_STICHWORT x JOIN SY_REGIEBUCH r ON r.ID = x.REGIEBUCH_ID WHERE r.NAME = '" + P + "Abend'");
        String aZeit = text(c, "SELECT TO_CHAR(SZENE_ZEIT, 'DD.MM.YYYY HH24:MI') FROM SY_ANSICHT WHERE ID = " + ans);
        var surf2 = com.dan.surf.Surf.sylt();
        Felder.schreiben(surf2, db.presets("com.dan.surf", "Surf").stream().filter(x -> x.name().equals(P + "See")).findFirst().orElseThrow().felder());
        boolean lagen = db.presets("com.dan.surf", "Surf").stream().filter(x -> List.of(Startdaten.LAGEN_PRESETS).contains(x.name())).count() == 3;
        check(rb.text().equals(rt) && sw == 4 && java.util.Arrays.equals(an.vorschau(), png) && an.szeneMs() == ms(TAG, 20, 15) && "31.12.2099 20:15".equals(aZeit)
                        && an.oeffnung() == 50 && Felder.lesen(surf2).equals(Felder.lesen(surf)) && lagen,
                "Regiebuch (Text als CLOB, 4 Stichworte), Ansicht mit Vorschau (BLOB) und Szenenzeit " + aZeit + ", Preset gespeichert und gelesen; Sommerabend, Westwind, Xaver für Surf");
        boolean abgelehnt = false;
        try { db.speichern(new Daten.Regiebuch("Ein Abend am Kliff", rt.replace(P + "Abend", "Ein Abend am Kliff"), "", null, Daten.EIGEN)); } catch (Ablage.Fehler e) { abgelehnt = true; }
        check(abgelehnt, "Ein mitgeliefertes Regiebuch wird nicht überschrieben");
        Map<String, String> sp = new LinkedHashMap<>();
        sp.put(Speicher.SZENE_MS, Long.toString(ms(TAG, 2, 30)));
        sp.put(Speicher.REGIEBUCH, P + "Abend");
        sp.put(Speicher.REGIE_SEKUNDE, "12.5");
        sp.put(Speicher.ZEITRAFFER, "60");
        sp.put(Speicher.LAGE, "Sturm");
        sp.put(Speicher.PEGEL, "3.41");
        sp.put(Speicher.WINDSTAU, "2.9");
        sp.put(Speicher.STURMFLUT, "true");
        sp.put(Speicher.ABBRUECHE, "1");
        db.sitzungSichern(sid, new Ablage.Sicherung("uhr.ms=" + sp.get(Speicher.SZENE_MS) + "\nkliff.stand=AAAA\n", sp));
        db.vorschau(sid, png);
        Daten.Sitzung ls = db.letzteSitzung(-1);
        String lsv = text(c, "SELECT TO_CHAR(SZENE_ZEIT, 'YYYY-MM-DD HH24:MI') || ' · ' || LAGE || ' · ' || MIT_VORSCHAU FROM SY_LETZTE_SITZUNG_V");
        check(ls != null && ls.id() == sid && !ls.sauber() && ls.szeneMs() == ms(TAG, 2, 30) && (P + "Abend").equals(ls.regiebuch()) && ls.sturmflut()
                        && ls.abbrueche() == 1 && java.util.Arrays.equals(ls.vorschau(), png) && "2099-12-31 02:30 · Sturm · 1".equals(lsv),
                "Sitzung gesichert mit Vorschau: " + lsv + ", Regiebuch mit Stelle, Sturmflut, ein Abbruch; noch nicht beendet");
        Protokoll.info("DbPrüfung", "eine Zeile aus der Prüfung");
        db.protokoll(Protokoll.abholen(), sid);
        db.sitzungBeenden(sid);
        double logz = zahl(c, "SELECT COUNT(*) FROM SY_LOG WHERE SITZUNG_ID = " + sid);
        check(logz >= 1 && zahl(c, "SELECT SAUBER_BEENDET FROM SY_SITZUNG WHERE ID = " + sid) == 1, "Protokoll in SY_LOG (" + (int) logz + " Zeilen), Sitzung sauber beendet");

        System.out.println("Übernahme aus Dateien (F33)");
        File dir = Files.createTempDirectory("sylt-dbpruefung-dateien").toFile();
        DateiSpeicher d = new DateiSpeicher(dir);
        long tb = ms(TAG, 9, 0);
        d.abbruch(abbruch(kante, 6_090_100, 1.5, tb), 7L);
        d.sturmflutLauf(new Daten.SturmflutLauf(0, null, P + "aus Datei", tb - 3_600_000, tb + 3_600_000, 3.0, tb, 2.5, 1.9, 2, 2.1, 1), 7L);
        d.serie(new Daten.Serie(tb, 600, 60, 4, 3, 1.2, 8, -1250, 1500), 7L);
        String ue = Uebernahme.uebernehmen(d, db);
        double aD = zahl(c, "SELECT COUNT(*) FROM SY_ABBRUCH a JOIN SY_KLIFFKANTE k ON k.ABBRUCH_ID = a.ID WHERE a.SZENE_MS = " + tb + " AND a.RUECKGANG IS NOT NULL"),
                fD = zahl(c, "SELECT COUNT(*) FROM SY_FINDLING WHERE SZENE_MS = " + tb), sD = zahl(c, "SELECT COUNT(*) FROM SY_STURMFLUT_LAUF WHERE NAME = '" + P + "aus Datei'");
        check(aD == 1 && fD == 1 && sD == 1 && new File(dir, "chronik/abbrueche.csv.uebernommen").isFile(), "Übernommen: " + ue + "; Kante und Findling hängen am Abbruch, die CSV heißen *.uebernommen");
        Daten.Stand st = db.stand();
        check(st.kantenstuecke() == k24.size() && st.presets() >= Startdaten.presets().size() && st.befunde() >= Startdaten.befunde().size() && st.abbrueche() >= 2,
                "Stand für das Startbild: Kante 2024 in " + st.kantenstuecke() + " Stücken, " + st.abbrueche() + " Abbrüche, " + st.sturmfluten() + " belegte Sturmfluten, "
                        + st.vorspuelungenBelegt() + " belegte Vorspülungen, " + st.regiebuecher() + " Regiebücher, " + st.befunde() + " Befunde");

        System.out.println("Aufräumen");
        int weg1 = db.chronikLeeren(sid);
        db.loeschenRegiebuch(P + "Abend");
        db.loeschenAnsicht(ans);
        db.loeschenPreset("com.dan.surf", "Surf", P + "See");
        long a0 = ms(TAG, 0, 0) - 7_200_000L, a1 = ms(TAG, 23, 59);
        try (Statement x = c.createStatement()) {
            x.executeUpdate("DELETE FROM SY_ABBRUCH WHERE SZENE_MS BETWEEN " + a0 + " AND " + a1);
            x.executeUpdate("DELETE FROM SY_STURMFLUT_LAUF WHERE NAME LIKE '~%'");
            x.executeUpdate("DELETE FROM SY_SERIE WHERE BEGINN_MS BETWEEN " + a0 + " AND " + a1);
            x.executeUpdate("DELETE FROM SY_VORSPUELUNG WHERE NAME LIKE '~%' AND HERKUNFT = 'EIGEN'");
            x.executeUpdate("DELETE FROM SY_LOG WHERE SITZUNG_ID = " + sid);
            x.executeUpdate("DELETE FROM SY_SITZUNG WHERE ID = " + sid);
        }
        double rest = zahl(c, "SELECT (SELECT COUNT(*) FROM SY_REGIEBUCH WHERE NAME LIKE '~%') + (SELECT COUNT(*) FROM SY_ANSICHT WHERE NAME LIKE '~%') "
                + "+ (SELECT COUNT(*) FROM SY_PAKET_PRESET WHERE NAME LIKE '~%') + (SELECT COUNT(*) FROM SY_KUESTE_V WHERE ZEIT_MS BETWEEN " + a0 + " AND " + a1 + ") "
                + "+ (SELECT COUNT(*) FROM SY_KLIFFKANTE WHERE STAND = 'ABBRUCH' AND SZENE_MS BETWEEN " + a0 + " AND " + a1 + ") FROM DUAL");
        check(rest == 0 && weg1 >= 6, "Aufgeräumt: " + weg1 + " Zeilen der Prüfsitzung (Chronik leeren), nichts mit „~“ und nichts am 31.12.2099 bleibt");
        db.schliessen();

        System.out.printf(Locale.GERMAN, "%d geprüft, %d Fehler · %.1f s%n", ok + bad, bad, (System.nanoTime() - t0) / 1e9);
        System.out.println(bad == 0 ? "Alles in Ordnung." : "Nicht alles in Ordnung.");
        System.exit(bad == 0 ? 0 : 1);
    }
}
