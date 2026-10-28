package com.dan.sylt.data;

import com.dan.sylt.scene.Regie;
import com.dan.store.Zahl;

import java.io.File;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Schreibt die mitgelieferten Daten als SQL: {@code db/sql/04_startdaten.sql} (erzeugt von {@code ant startdaten},
 * nicht von Hand ändern). MERGE je Eintrag mit Namen; Kinder (Sektoren, Stichworte, Werte, Belege) und die Kante 2024
 * werden gelöscht und neu angelegt. Eigene Zeilen (HERKUNFT = 'EIGEN') bleiben unberührt. Beliebig oft wiederholbar.
 * <p>
 * Geometrien in UTM 32N (SRID 25832, F35 A): Punkte als {@code SDO_POINT_TYPE}, Linien und Flächen als WKT über
 * {@code SDO_GEOMETRY(wkt, 25832)}; lange Texte (WKT, Regiebücher) in Stücken zu höchstens 1 000 Zeichen als
 * {@code TO_CLOB(…) || TO_CLOB(…)}. Szenenzeit (F34 A) als ms seit 1970 und TIMESTAMP WITH TIME ZONE in Europe/Berlin.
 */
public final class StartdatenSql {
    private StartdatenSql() { }

    static final String M = "'MITGELIEFERT'";

    static String s(String v) {
        if (v == null) return "NULL";
        return "'" + v.replace("'", "''").replace("\r", "").replace("\n", "' || CHR(10) || '") + "'";
    }

    /** Ein langer Text als CLOB in Stücken. */
    static String clob(String v) {
        if (v == null) return "NULL";
        String t = v.replace("\r", "");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < t.length(); ) {
            int e = Math.min(t.length(), i + 1000);
            if (e < t.length() && Character.isHighSurrogate(t.charAt(e - 1))) e--;
            // ein Zeilenumbruch wird in SQL zu „' || CHR(10) || '“: das Stück bleibt unter 4000 Byte
            b.append(i > 0 ? "\n    || " : "").append("TO_CLOB(").append(s(t.substring(i, e))).append(")");
            i = e;
        }
        return b.toString();
    }

    static String n(Double v) { return v == null || Double.isNaN(v) || Double.isInfinite(v) ? "NULL" : Zahl.text(Math.round(v * 1e6) / 1e6); }

    static String n(double v) { return n((Double) v); }

    static String i(Integer v) { return v == null ? "NULL" : v.toString(); }

    static String l(Long v) { return v == null ? "NULL" : v.toString(); }

    static String b(boolean v) { return v ? "1" : "0"; }

    static String d(LocalDate d) { return d == null ? "NULL" : "DATE '" + d + "'"; }

    /** Szenenzeit als TIMESTAMP WITH TIME ZONE in Europe/Berlin aus ms seit 1970 (F34 A). */
    public static String tstz(Long ms) {
        if (ms == null) return "NULL";
        return "FROM_TZ(TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(" + ms + " / 1000, 'SECOND'), 'UTC') AT TIME ZONE 'Europe/Berlin'";
    }

    static String quelle(String kurz) { return kurz == null ? "NULL" : "(SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = " + s(kurz) + ")"; }

    /** Ein Punkt in UTM aus Szenenmetern. */
    static String punkt(double x, double z) {
        double[] u = Daten.utm(x, z);
        return "MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(" + Daten.n2(u[0]) + ", " + Daten.n2(u[1]) + ", NULL), NULL, NULL)";
    }

    static String geom(Daten.Geom g) {
        if (g == null) return "NULL";
        if (g.art().equals("PUNKT")) return punkt(g.xz()[0][0], g.xz()[0][1]);
        String w = g.wkt();
        if (w.length() <= 1000) return "MDSYS.SDO_GEOMETRY(" + s(w) + ", 25832)";
        return "MDSYS.SDO_GEOMETRY(" + clob(w) + ", 25832)";
    }

    private static void kopf(StringBuilder b, String t) {
        b.append("\n-- ").append("-".repeat(Math.max(4, 100 - t.length()))).append(' ').append(t).append('\n');
    }

    /** Die mitgelieferten Daten auf einmal (Gelände und Kante brauchen daten/). */
    public record Alles(List<Daten.Gelaende> gelaende, List<Daten.Kantenstueck> kante) {
        public static Alles aus(File daten) {
            return new Alles(Startdaten.gelaende(new File(daten, "gelaende")), Startdaten.kante2024(daten));
        }
    }

    /** Die Zahlen der mitgelieferten Zeilen je Art (Zeile „-- ZAHLEN:“, der Einrichter prüft sie). */
    public static Map<String, Integer> zahlen(Alles a) {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("Quellen", Startdaten.quellen().size());
        m.put("Befunde", Startdaten.befunde().size());
        int belege = 0;
        for (Daten.Befund f : Startdaten.befunde()) belege += f.quellen().size();
        m.put("Belege", belege);
        m.put("Gelände", a.gelaende().size());
        m.put("Kante 2024", a.kante().size());
        m.put("Sturmfluten", Startdaten.sturmfluten().size());
        m.put("Vorspülungen", Startdaten.vorspuelungen().size());
        m.put("Lagen", Startdaten.lagen().size());
        m.put("Leuchtfeuer", Startdaten.leuchtfeuer().size());
        int se = 0;
        for (Daten.Leuchtfeuer f : Startdaten.leuchtfeuer()) se += f.sektoren().size();
        m.put("Sektoren", se);
        m.put("Regiebücher", Startdaten.regiebuecher().size());
        int sw = 0;
        for (Daten.Regiebuch r : Startdaten.regiebuecher()) sw += Regie.parse(r.text()).zeilen().size();
        m.put("Stichworte", sw);
        m.put("Ansichten", Startdaten.ansichten().size());
        m.put("Fahrten", Startdaten.fahrten().size());
        m.put("Presets", Startdaten.presets().size());
        int pw = 0;
        for (Daten.PaketPreset p : Startdaten.presets()) pw += p.felder().size();
        m.put("Presetwerte", pw);
        return m;
    }

    /** MERGE INTO t USING (SELECT key AS col …) q ON (…) WHEN MATCHED THEN UPDATE … WHERE HERKUNFT … WHEN NOT MATCHED THEN INSERT …. */
    static void merge(StringBuilder b, String tabelle, String[] schluessel, String[] schluesselWerte, String[] spalten, String[] werte, boolean herkunft) {
        StringBuilder using = new StringBuilder(), on = new StringBuilder(), upd = new StringBuilder(), cols = new StringBuilder(), vals = new StringBuilder();
        for (int k = 0; k < schluessel.length; k++) {
            using.append(k > 0 ? ", " : "").append(schluesselWerte[k]).append(" AS ").append(schluessel[k]);
            on.append(k > 0 ? " AND " : "").append("t.").append(schluessel[k]).append(" = q.").append(schluessel[k]);
            cols.append(k > 0 ? ", " : "").append(schluessel[k]);
            vals.append(k > 0 ? ", " : "").append(schluesselWerte[k]);
        }
        for (int k = 0; k < spalten.length; k++) {
            upd.append(k > 0 ? ", " : "").append("t.").append(spalten[k]).append(" = ").append(werte[k]);
            cols.append(", ").append(spalten[k]);
            vals.append(", ").append(werte[k]);
        }
        b.append("MERGE INTO ").append(tabelle).append(" t USING (SELECT ").append(using).append(" FROM DUAL) q ON (").append(on).append(")\n");
        if (spalten.length > 0) b.append("WHEN MATCHED THEN UPDATE SET ").append(upd).append(herkunft ? " WHERE t.HERKUNFT = " + M : "").append('\n');
        b.append("WHEN NOT MATCHED THEN INSERT (").append(cols).append(herkunft ? ", HERKUNFT" : "").append(")\n  VALUES (").append(vals)
                .append(herkunft ? ", " + M : "").append(");\n");
    }

    /** Die Spalten je Tabelle, in die dieses Skript schreibt (die Prüfung vergleicht sie mit 01_tabellen.sql). */
    public static final Map<String, List<String>> SPALTEN = new LinkedHashMap<>();
    static {
        SPALTEN.put("SY_SCHEMA", List.of("VERSION", "APP_VERSION", "TEXT"));
        SPALTEN.put("SY_QUELLE", List.of("KURZ", "TITEL", "URL", "ART", "ABGERUFEN", "BEMERKUNG", "HERKUNFT"));
        SPALTEN.put("SY_BEFUND", List.of("THEMA", "GEGENSTAND", "BEFUND", "TEXT", "HERKUNFT"));
        SPALTEN.put("SY_BEFUND_QUELLE", List.of("BEFUND_ID", "QUELLE_ID"));
        SPALTEN.put("SY_GELAENDE", List.of("NAME", "STAND", "KERN", "FERNFELD", "URSPRUNG", "URSPRUNG_OST", "URSPRUNG_NORD", "ZELLE_KERN_M", "ZELLE_M", "ZELLE_FERN_M",
                "DATEIEN", "QUELLE_ID", "BEMERKUNG", "HERKUNFT"));
        SPALTEN.put("SY_KLIFFKANTE", List.of("STAND", "NORD_VON", "NORD_BIS", "OBERKANTE", "FUSS", "WAND_MAX_M", "OBERKANTE_MAX_M", "HERKUNFT"));
        SPALTEN.put("SY_STURMFLUT", List.of("NAME", "DATUM", "DATUM_TEXT", "JAHR", "SCHEITEL_LIST_CM", "SCHEITEL_HOERNUM_CM", "SCHEITEL_WESTERLAND_CM", "BEZUG",
                "KLIFFVERLUST_M", "KLIFFVERLUST_TEXT", "FOLGEN", "QUELLE_ID", "HERKUNFT"));
        SPALTEN.put("SY_VORSPUELUNG", List.of("NAME", "ART", "JAHR", "MENGE_M3", "STRECKE_TEXT", "BEMERKUNG", "QUELLE_ID", "HERKUNFT"));
        SPALTEN.put("SY_SEEGANG", List.of("NAME", "SCHLUESSEL", "PHASE", "WIND_MS", "RICHTUNG_GRAD", "BOEIGKEIT", "BEDECKUNG_ACHTEL", "HOHE_SCHICHT", "WOLKENART",
                "REGEN_MMH", "SICHT_KM", "NEBEL", "NEBEL_HOCH_M", "HS_M", "TP_S", "SEE_RICHTUNG_GRAD", "GEWITTER", "TEMPERATUR_C", "BESCHREIBUNG", "HERKUNFT"));
        SPALTEN.put("SY_LEUCHTFEUER", List.of("NAME", "KENNUNG", "FEUERHOEHE_M", "TURMHOEHE_M", "TRAGWEITE_SM", "IN_BETRIEB_VON", "IN_BETRIEB_BIS", "HEUTE_AN", "LAGE",
                "OST", "NORD", "BESCHREIBUNG", "QUELLE_ID", "HERKUNFT"));
        SPALTEN.put("SY_SEKTOR", List.of("LEUCHTFEUER_ID", "NR", "FARBE", "VON_GRAD", "BIS_GRAD", "TRAGWEITE_SM", "TEXT"));
        SPALTEN.put("SY_REGIEBUCH", List.of("NAME", "BESCHREIBUNG", "DATEI", "SZENE_MS", "SZENE_ZEIT", "ZEITRAFFER", "LAENGE_S", "FOTOS", "WARTEN", "BLATT", "TEXT", "HERKUNFT"));
        SPALTEN.put("SY_STICHWORT", List.of("REGIEBUCH_ID", "NR", "SEKUNDE", "BEFEHL", "ARGUMENTE", "TEXT"));
        SPALTEN.put("SY_ANSICHT", List.of("NAME", "ART", "TASTE", "KAM_X", "KAM_Z", "KAM_GIER_GRAD", "KAM_NICK_GRAD", "OEFFNUNG_GRAD", "AUGE_UEBER_GRUND_M", "LAGE", "HERKUNFT"));
        SPALTEN.put("SY_FAHRT", List.of("NAME", "ART", "DAUER_S", "LAENGE_M", "BESCHREIBUNG", "HERKUNFT"));
        SPALTEN.put("SY_PAKET_PRESET", List.of("PAKET", "KLASSE", "NAME", "BESCHREIBUNG", "HERKUNFT"));
        SPALTEN.put("SY_PAKET_WERT", List.of("PAKET_PRESET_ID", "FELD", "TYP", "WERT"));
    }

    /** Das ganze Skript. */
    public static String sql(Alles a) {
        StringBuilder b = new StringBuilder();
        Map<String, Integer> z = zahlen(a);
        b.append("""
                -- =====================================================================================================
                -- Sylt · Datenbank · 04_startdaten.sql
                -- ERZEUGT aus com.dan.sylt.data.Startdaten (ant startdaten) – nicht von Hand ändern.
                -- Mitgelieferte Zeilen (HERKUNFT = 'MITGELIEFERT') werden angelegt oder wiederhergestellt; eigene Zeilen
                -- bleiben unberührt. Beliebig oft wiederholbar. Quellen, Befunde, die belegten Sturmfluten seit 1962 und
                -- Vorspülungen vor dem Kliff (recherchiert am 28.10.2026, F38/F39 A), das Gelände und die Kante 2024 aus
                -- daten/ (F36 A), Lagen, Leuchtfeuer mit Sektoren (F40 A), Regiebücher, Ansichten, Fahrten (F41 A) und die
                -- Presets der Packages (F43 A). Geometrien in UTM 32N (SRID 25832).
                -- =====================================================================================================
                """);
        StringBuilder zl = new StringBuilder("-- ZAHLEN:");
        for (Map.Entry<String, Integer> e : z.entrySet()) zl.append(' ').append(e.getKey()).append('=').append(e.getValue()).append(';');
        b.append(zl).append("\n\n-- Die Quellen enthalten „&“: in SQL Developer keine Ersetzungsvariablen\nSET DEFINE OFF\n");

        kopf(b, "Ausbaustufe");
        b.append("MERGE INTO SY_SCHEMA t USING (SELECT 1 AS VERSION FROM DUAL) q ON (t.VERSION = q.VERSION)\n")
                .append("WHEN NOT MATCHED THEN INSERT (VERSION, APP_VERSION, TEXT) VALUES (1, 'Phase 16', ")
                .append(s("Datenbank: 25 Tabellen, Kante 2024 und Chronik der Abbrüche, Sturmfluten und Vorspülungen, Leuchtfeuer, Regie und Sitzungen"))
                .append(");\n");

        kopf(b, "Quellen");
        for (Daten.Quelle q : Startdaten.quellen())
            merge(b, "SY_QUELLE", new String[]{"KURZ"}, new String[]{s(q.kurz())}, new String[]{"TITEL", "URL", "ART", "ABGERUFEN", "BEMERKUNG"},
                    new String[]{s(q.titel()), s(q.url()), s(q.art()), d(q.abgerufen()), s(q.bemerkung())}, true);

        kopf(b, "Befunde");
        for (Daten.Befund f : Startdaten.befunde())
            merge(b, "SY_BEFUND", new String[]{"THEMA", "GEGENSTAND"}, new String[]{s(f.thema()), s(f.gegenstand())}, new String[]{"BEFUND", "TEXT"},
                    new String[]{s(f.befund()), s(f.text())}, true);
        b.append("DELETE FROM SY_BEFUND_QUELLE WHERE BEFUND_ID IN (SELECT ID FROM SY_BEFUND WHERE HERKUNFT = ").append(M).append(");\n");
        for (Daten.Befund f : Startdaten.befunde())
            for (String q : f.quellen())
                b.append("INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = ").append(s(f.thema()))
                        .append(" AND b.GEGENSTAND = ").append(s(f.gegenstand())).append(" AND b.HERKUNFT = ").append(M).append(" AND q.KURZ = ").append(s(q)).append(";\n");

        kopf(b, "Gelände (Phase 2)");
        for (Daten.Gelaende g : a.gelaende())
            merge(b, "SY_GELAENDE", new String[]{"NAME"}, new String[]{s(g.name())},
                    new String[]{"STAND", "KERN", "FERNFELD", "URSPRUNG", "URSPRUNG_OST", "URSPRUNG_NORD", "ZELLE_KERN_M", "ZELLE_M", "ZELLE_FERN_M", "DATEIEN", "QUELLE_ID",
                            "BEMERKUNG"},
                    new String[]{s(g.stand()), geom(g.kern()), geom(g.fernfeld()), punkt(0, 0), n(g.ursprungOst()), n(g.ursprungNord()), n(g.zelleKern()), n(g.zelle()),
                            n(g.zelleFern()), s(g.dateien().isEmpty() ? null : g.dateienText()), quelle(g.quelle()), s(g.bemerkung())}, true);

        kopf(b, "Kante des Kliffs 2024 (F36 A): die gemessenen Stücke neu, die Stücke der Abbrüche bleiben");
        b.append("DELETE FROM SY_KLIFFKANTE WHERE STAND = 'GEMESSEN_2024' AND HERKUNFT = ").append(M).append(";\n");
        for (Daten.Kantenstueck k : a.kante())
            b.append("INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)\n  VALUES ('GEMESSEN_2024', ")
                    .append(n(k.nordVon())).append(", ").append(n(k.nordBis())).append(",\n  ").append(geom(k.oberkante())).append(",\n  ").append(geom(k.fuss()))
                    .append(",\n  ").append(n(k.wandMax())).append(", ").append(n(k.oberkanteMax())).append(", ").append(M).append(");\n");

        kopf(b, "Sturmfluten seit 1962 (F38 A, belegt)");
        for (Daten.Sturmflut f : Startdaten.sturmfluten())
            merge(b, "SY_STURMFLUT", new String[]{"NAME"}, new String[]{s(f.name())},
                    new String[]{"DATUM", "DATUM_TEXT", "JAHR", "SCHEITEL_LIST_CM", "SCHEITEL_HOERNUM_CM", "SCHEITEL_WESTERLAND_CM", "BEZUG", "KLIFFVERLUST_M", "KLIFFVERLUST_TEXT",
                            "FOLGEN", "QUELLE_ID"},
                    new String[]{d(f.datum()), s(String.format(Locale.GERMAN, "%td.%<tm.%<tY", f.datum())), Integer.toString(f.datum().getYear()), i(f.list()), i(f.hoernum()),
                            i(f.westerland()), s(f.bezug()), n(f.kliffverlustM()), s(f.kliffverlustText()), s(f.folgen()), quelle(f.quelle())}, true);

        kopf(b, "Vorspülungen vor Wenningstedt und Kampen (F39 A, belegt)");
        for (Daten.Vorspuelung v : Startdaten.vorspuelungen())
            merge(b, "SY_VORSPUELUNG", new String[]{"NAME"}, new String[]{s(v.name())},
                    new String[]{"ART", "JAHR", "MENGE_M3", "STRECKE_TEXT", "BEMERKUNG", "QUELLE_ID"},
                    new String[]{"'BELEGT'", i(v.jahr()), l(v.menge()), s(v.strecke()), s(v.bemerkung()), quelle(v.quelle())}, true);

        kopf(b, "Lagen der Wetterregie");
        for (Daten.Lage g : Startdaten.lagen())
            merge(b, "SY_SEEGANG", new String[]{"NAME"}, new String[]{s(g.name())},
                    new String[]{"SCHLUESSEL", "PHASE", "WIND_MS", "RICHTUNG_GRAD", "BOEIGKEIT", "BEDECKUNG_ACHTEL", "HOHE_SCHICHT", "WOLKENART", "REGEN_MMH", "SICHT_KM", "NEBEL",
                            "NEBEL_HOCH_M", "HS_M", "TP_S", "SEE_RICHTUNG_GRAD", "GEWITTER", "TEMPERATUR_C", "BESCHREIBUNG"},
                    new String[]{s(g.schluessel()), s(g.phase()), n(g.wind()), n(g.richtung()), n(g.boeig()), n(g.oktas()), n(g.hoch()), s(g.wolke()), n(g.regen()), n(g.sicht()),
                            n(g.nebel()), n(g.nebelHoch()), n(g.hs()), n(g.tp()), n(g.seeRichtung()), Integer.toString(g.gewitter()), n(g.temperatur()), s(g.beschreibung())}, true);

        kopf(b, "Leuchtfeuer und Sektoren (F40 A)");
        for (Daten.Leuchtfeuer f : Startdaten.leuchtfeuer()) {
            double[] u = Daten.utm(f.x(), f.z());
            merge(b, "SY_LEUCHTFEUER", new String[]{"NAME"}, new String[]{s(f.name())},
                    new String[]{"KENNUNG", "FEUERHOEHE_M", "TURMHOEHE_M", "TRAGWEITE_SM", "IN_BETRIEB_VON", "IN_BETRIEB_BIS", "HEUTE_AN", "LAGE", "OST", "NORD", "BESCHREIBUNG",
                            "QUELLE_ID"},
                    new String[]{s(f.kennung()), n(f.feuerhoehe()), n(f.turmhoehe()), n(f.tragweite()), i(f.vonJahr()), i(f.bisJahr()), b(f.heuteAn()), punkt(f.x(), f.z()),
                            Daten.n2(u[0]), Daten.n2(u[1]), s(f.beschreibung()), quelle(f.quelle())}, true);
            String eltern = "FROM SY_LEUCHTFEUER WHERE NAME = " + s(f.name()) + " AND HERKUNFT = " + M;
            b.append("DELETE FROM SY_SEKTOR WHERE LEUCHTFEUER_ID IN (SELECT ID ").append(eltern).append(");\n");
            for (Daten.Sektor x : f.sektoren())
                b.append("INSERT INTO SY_SEKTOR (LEUCHTFEUER_ID, NR, FARBE, VON_GRAD, BIS_GRAD, TRAGWEITE_SM, TEXT) SELECT ID, ").append(x.nr()).append(", ").append(s(x.farbe()))
                        .append(", ").append(n(x.von())).append(", ").append(n(x.bis())).append(", ").append(n(x.tragweite())).append(", ").append(s(x.text())).append(' ')
                        .append(eltern).append(";\n");
        }

        kopf(b, "Regiebücher");
        for (Daten.Regiebuch r : Startdaten.regiebuecher()) {
            Regie.Book bk = Regie.parse(r.text());
            Startdaten.Kopf k = Startdaten.Kopf.aus(bk);
            String[] cols = {"BESCHREIBUNG", "DATEI", "SZENE_MS", "SZENE_ZEIT", "ZEITRAFFER", "LAENGE_S", "FOTOS", "WARTEN", "BLATT"};
            String[] vals = {s(r.beschreibung()), s(r.datei()), l(k.szeneMs()), tstz(k.szeneMs()), n(k.raffer()), n(bk.length()), Integer.toString(k.fotos()),
                    Integer.toString(k.warten()), b(k.blatt())};
            b.append("MERGE INTO SY_REGIEBUCH t USING (SELECT ").append(s(r.name())).append(" AS NAME FROM DUAL) q ON (t.NAME = q.NAME)\n")
                    .append("WHEN NOT MATCHED THEN INSERT (NAME, LAENGE_S, TEXT, HERKUNFT) VALUES (").append(s(r.name())).append(", ").append(n(bk.length()))
                    .append(", '-', ").append(M).append(");\n");
            StringBuilder upd = new StringBuilder();
            for (int c = 0; c < cols.length; c++) upd.append(c > 0 ? ", " : "").append(cols[c]).append(" = ").append(vals[c]);
            b.append("UPDATE SY_REGIEBUCH SET ").append(upd).append(",\n  TEXT = ").append(clob(r.text())).append("\n  WHERE NAME = ").append(s(r.name()))
                    .append(" AND HERKUNFT = ").append(M).append(";\n");
            String eltern = "FROM SY_REGIEBUCH WHERE NAME = " + s(r.name()) + " AND HERKUNFT = " + M;
            b.append("DELETE FROM SY_STICHWORT WHERE REGIEBUCH_ID IN (SELECT ID ").append(eltern).append(");\n");
            int nr = 0;
            for (Regie.Zeile zz : bk.zeilen())
                b.append("INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, ").append(++nr).append(", ").append(n(zz.sekunde())).append(", ")
                        .append(s(zz.befehl().toUpperCase(Locale.ROOT))).append(", ").append(s(zz.argumente().isEmpty() ? null : zz.argumente())).append(", ")
                        .append(s(zz.text().isEmpty() ? null : zz.text())).append(' ').append(eltern).append(";\n");
        }

        kopf(b, "Ansichten (Tasten 1–9, Blick, Vorlagen der Türme)");
        for (Daten.Ansicht an : Startdaten.ansichten()) {
            double[] c = an.kamera();
            String[] cols = {"TASTE", "KAM_X", "KAM_Z", "KAM_GIER_GRAD", "KAM_NICK_GRAD", "OEFFNUNG_GRAD", "AUGE_UEBER_GRUND_M", "LAGE"};
            String[] vals = {i(an.taste()), n(Math.round(c[0] * 100) / 100.0), n(Math.round(c[2] * 100) / 100.0), n(Math.round(c[3] * 100) / 100.0),
                    n(Math.round(c[4] * 100) / 100.0), n(an.oeffnung()), n(an.augeUeberGrund()), punkt(c[0], c[2])};
            merge(b, "SY_ANSICHT", new String[]{"NAME", "ART"}, new String[]{s(an.name()), s(an.art())}, cols, vals, true);
        }

        kopf(b, "Kamerafahrten");
        for (Daten.Fahrt f : Startdaten.fahrten())
            merge(b, "SY_FAHRT", new String[]{"NAME"}, new String[]{s(f.name())}, new String[]{"ART", "DAUER_S", "LAENGE_M", "BESCHREIBUNG"},
                    new String[]{s(f.art()), n(f.dauerS()), n(f.laengeM()), s(f.beschreibung())}, true);

        kopf(b, "Presets der Packages (F43 A)");
        for (Daten.PaketPreset p : Startdaten.presets()) {
            merge(b, "SY_PAKET_PRESET", new String[]{"PAKET", "KLASSE", "NAME"}, new String[]{s(p.paket()), s(p.klasse()), s(p.name())}, new String[]{"BESCHREIBUNG"},
                    new String[]{s(p.beschreibung())}, true);
            String eltern = "FROM SY_PAKET_PRESET WHERE PAKET = " + s(p.paket()) + " AND KLASSE = " + s(p.klasse()) + " AND NAME = " + s(p.name()) + " AND HERKUNFT = " + M;
            b.append("DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID ").append(eltern).append(");\n");
            Map<String, String> typ = Startdaten.typen(p);
            for (Map.Entry<String, String> e : p.felder().entrySet())
                b.append("INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, ").append(s(e.getKey())).append(", ").append(s(typ.getOrDefault(e.getKey(), "STRING")))
                        .append(", ").append(s(e.getValue().isEmpty() ? null : e.getValue())).append(' ').append(eltern).append(";\n");
        }
        b.append("\nCOMMIT;\n\n-- Ende 04_startdaten.sql\n");
        return b.toString();
    }

    /** Werkzeug: schreibt db/sql/04_startdaten.sql (Argument: Ordner daten, sonst ./daten). */
    public static void main(String[] args) throws Exception {
        File daten = new File(args.length > 0 ? args[0] : "daten");
        Alles a = Alles.aus(daten);
        if (a.kante().isEmpty()) throw new IllegalStateException("Keine Kante in " + new File(daten, "kliff/kante.csv") + " – erst ant aufbereitung");
        File f = new File(Verbindung.skripte(), "04_startdaten.sql");
        java.nio.file.Files.writeString(f.toPath(), sql(a), java.nio.charset.StandardCharsets.UTF_8);
        System.out.println(f + ": " + zahlen(a));
    }
}
