package com.dan.sylt.data;

import com.dan.store.Db;
import com.dan.store.Einrichter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Das Schema SYLT (Werkbuch Abschnitt 10, F29 A, seit Phase 16 mit F33–F45 „alle A“): Ort von {@code db/}, Zugang aus
 * {@code db/db.properties} und der Einrichter mit allem, was er für Sylt prüft. Schema und Benutzer legt
 * {@code db/User.sql} an ({@code manage_user('Sylt', 'sy')} auf PDBORCL). Tabellen {@code SY_…} mit Sequenz und
 * Trigger, nie IDENTITY; Geometrien in UTM 32N (SRID 25832), die Sichten für QGIS in 8307 (F35 A).
 */
public final class Verbindung {
    private Verbindung() { }

    public static final String PRAEFIX = "SY_";
    /** Umgebungsvariable für ein leeres Passwort (wie in db.properties.example beschrieben). */
    public static final String ENV = "Sylt_DB_PASSWORD";
    /** Die Ausbaustufe dieser App (SY_SCHEMA.VERSION). */
    public static final int VERSION = 1;
    /** Die Koordinaten der Tabellen: ETRS89 / UTM 32N. */
    public static final int SRID = 25832;
    public static final String[] SICHTEN = {"SY_KANTE_V", "SY_RUECKGANG_V", "SY_ABBRUCH_V", "SY_FINDLING_V", "SY_VORSPUELUNG_V", "SY_LEUCHTFEUER_V",
            "SY_ANSICHT_V", "SY_KUESTE_V", "SY_RUECKGANG_JAHR_V", "SY_STURMFLUT_V", "SY_BEFUND_V", "SY_LETZTE_SITZUNG_V"};
    /** Die Geometriespalten der Tabellen (Metadaten in 25832 und je ein räumlicher Index). */
    public static final String[] METADATEN = {"SY_GELAENDE.KERN", "SY_GELAENDE.FERNFELD", "SY_GELAENDE.URSPRUNG", "SY_LEUCHTFEUER.LAGE", "SY_ABBRUCH.BRUCHSTELLE",
            "SY_ABBRUCH.RUECKGANG", "SY_KLIFFKANTE.OBERKANTE", "SY_KLIFFKANTE.FUSS", "SY_FINDLING.LAGE", "SY_VORSPUELUNG.FLAECHE", "SY_SERIE.LAGE", "SY_ANSICHT.LAGE"};
    /** Die Geometrien der Sichten (Metadaten in 8307 für QGIS, ohne Index). */
    public static final String[] SICHT_METADATEN = {"SY_KANTE_V.GEOM", "SY_RUECKGANG_V.GEOM", "SY_ABBRUCH_V.GEOM", "SY_FINDLING_V.GEOM", "SY_VORSPUELUNG_V.GEOM",
            "SY_LEUCHTFEUER_V.GEOM", "SY_ANSICHT_V.GEOM", "SY_KUESTE_V.GEOM"};

    /** Der Stand der Verbindung (für Startbild, Bedienfeld und Statuszeile). */
    private static volatile String stand = "ohne Datenbank";

    /** Der Ordner db: -Dsylt.db.dir, sonst ./db, sonst db neben dem Jar. */
    public static File ordner() { return Db.ordner("sylt.db.dir", Verbindung.class); }

    public static File skripte() { return new File(ordner(), "sql"); }

    /**
     * Der Zugang aus db/db.properties. Fehlt die Datei, wird sie aus der Vorlage db.properties.example angelegt (ohne
     * Passwort); das Startbild fragt dann danach und trägt es nach dem Verbinden ein.
     */
    public static Db.Konfig konfig() throws IOException {
        File f = new File(ordner(), "db.properties"), vorlage = new File(ordner(), "db.properties.example");
        if (!f.isFile() && vorlage.isFile()) Files.copy(vorlage.toPath(), f.toPath());
        Db.Konfig k = Db.konfig(f, "Sylt", ENV);
        if (!k.hatPasswort()) {
            // auch in Großbuchstaben (Windows kennt beide, andere Systeme nicht)
            String e = System.getenv(ENV.toUpperCase(java.util.Locale.ROOT));
            if (e != null && !e.isEmpty()) k = k.mitPasswort(e.toCharArray());
        }
        return k;
    }

    /** „SYLT auf PDBORCL · Oracle 21“ oder „ohne Datenbank“. */
    public static String stand() { return stand; }

    public static void stand(String s) { stand = s; }

    /** Die räumlichen Indizes (je Geometriespalte einer: SY_ABBRUCH_BRUCHSTELLE_SX …). */
    public static String[] raeumlich() {
        String[] r = new String[METADATEN.length];
        for (int i = 0; i < r.length; i++) r[i] = METADATEN[i].replace('.', '_') + "_SX";
        return r;
    }

    /** Die Zahlen der Startdaten aus der Zeile {@code -- ZAHLEN: Art=n; …} von 04_startdaten.sql. */
    public static Map<String, Integer> zahlen(File dir) {
        Map<String, Integer> m = new LinkedHashMap<>();
        try (var lines = Files.lines(new File(dir, "04_startdaten.sql").toPath(), StandardCharsets.UTF_8)) {
            String z = lines.filter(l -> l.startsWith("-- ZAHLEN:")).findFirst().orElse(null);
            if (z != null)
                for (String p : z.substring(10).split(";")) {
                    String[] kv = p.strip().split("=");
                    if (kv.length == 2) m.put(kv[0].strip(), Integer.parseInt(kv[1].strip()));
                }
        } catch (IOException | RuntimeException e) { /* dann prüft der Einrichter ohne Zahlen */ }
        return m;
    }

    /** Die SQL-Abfrage je Zahl der Startdaten. */
    static final Map<String, String> ZAEHLEN = new LinkedHashMap<>();
    static {
        String m = " WHERE HERKUNFT = 'MITGELIEFERT'";
        ZAEHLEN.put("Quellen", "SELECT COUNT(*) FROM SY_QUELLE" + m);
        ZAEHLEN.put("Befunde", "SELECT COUNT(*) FROM SY_BEFUND" + m);
        ZAEHLEN.put("Belege", "SELECT COUNT(*) FROM SY_BEFUND_QUELLE x JOIN SY_BEFUND b ON b.ID = x.BEFUND_ID WHERE b.HERKUNFT = 'MITGELIEFERT'");
        ZAEHLEN.put("Gelände", "SELECT COUNT(*) FROM SY_GELAENDE" + m);
        ZAEHLEN.put("Kante 2024", "SELECT COUNT(*) FROM SY_KLIFFKANTE" + m + " AND STAND = 'GEMESSEN_2024'");
        ZAEHLEN.put("Sturmfluten", "SELECT COUNT(*) FROM SY_STURMFLUT" + m);
        ZAEHLEN.put("Vorspülungen", "SELECT COUNT(*) FROM SY_VORSPUELUNG" + m);
        ZAEHLEN.put("Lagen", "SELECT COUNT(*) FROM SY_SEEGANG" + m);
        ZAEHLEN.put("Leuchtfeuer", "SELECT COUNT(*) FROM SY_LEUCHTFEUER" + m);
        ZAEHLEN.put("Sektoren", "SELECT COUNT(*) FROM SY_SEKTOR x JOIN SY_LEUCHTFEUER l ON l.ID = x.LEUCHTFEUER_ID WHERE l.HERKUNFT = 'MITGELIEFERT'");
        ZAEHLEN.put("Regiebücher", "SELECT COUNT(*) FROM SY_REGIEBUCH" + m);
        ZAEHLEN.put("Stichworte", "SELECT COUNT(*) FROM SY_STICHWORT x JOIN SY_REGIEBUCH r ON r.ID = x.REGIEBUCH_ID WHERE r.HERKUNFT = 'MITGELIEFERT'");
        ZAEHLEN.put("Ansichten", "SELECT COUNT(*) FROM SY_ANSICHT" + m);
        ZAEHLEN.put("Fahrten", "SELECT COUNT(*) FROM SY_FAHRT" + m);
        ZAEHLEN.put("Presets", "SELECT COUNT(*) FROM SY_PAKET_PRESET" + m);
        ZAEHLEN.put("Presetwerte", "SELECT COUNT(*) FROM SY_PAKET_WERT x JOIN SY_PAKET_PRESET p ON p.ID = x.PAKET_PRESET_ID WHERE p.HERKUNFT = 'MITGELIEFERT'");
        ZAEHLEN.put("Sichten in 8307", "SELECT COUNT(*) FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME LIKE 'SY\\_%\\_V' ESCAPE '\\' AND SRID = 8307");
    }

    /** Der Einrichter für das Schema SYLT. */
    public static Einrichter einrichter() {
        Einrichter e = new Einrichter();
        e.praefix = PRAEFIX;
        e.version = VERSION;
        e.srid = SRID;
        e.sichten = SICHTEN.clone();
        e.metadaten = METADATEN.clone();
        e.raeumlich = raeumlich();
        e.startdaten = () -> {
            List<Einrichter.Soll> l = new ArrayList<>();
            Map<String, Integer> z = zahlen(skripte());
            for (Map.Entry<String, String> q : ZAEHLEN.entrySet()) {
                if (z.containsKey(q.getKey())) l.add(new Einrichter.Soll(q.getKey(), q.getValue(), z.get(q.getKey())));
                else if (q.getKey().equals("Sichten in 8307")) l.add(new Einrichter.Soll(q.getKey(), q.getValue(), SICHT_METADATEN.length));
            }
            return l;
        };
        e.rechnet = () -> List.of(
                new Einrichter.Soll("Kante 2024 (km, Oberkante in 25832)",
                        "SELECT TO_CHAR(SUM(MDSYS.SDO_GEOM.SDO_LENGTH(OBERKANTE, 0.005)) / 1000, 'FM990.00') || ' km in ' || TO_CHAR(COUNT(*)) || ' Stücken' "
                                + "FROM SY_KLIFFKANTE WHERE STAND = 'GEMESSEN_2024'", -1),
                new Einrichter.Soll("Kern des Geländes (km², Szene 2,3 × 3,6 = 8,3)",
                        "SELECT TO_CHAR(MDSYS.SDO_GEOM.SDO_AREA(KERN, 0.005) / 1e6, 'FM990.0') FROM SY_GELAENDE WHERE ROWNUM = 1", -1),
                new Einrichter.Soll("Abstand der Türme (m auf dem Ellipsoid, Sicht in 8307)",
                        "SELECT TO_CHAR(MDSYS.SDO_GEOM.SDO_DISTANCE(a.GEOM, b.GEOM, 0.05, 'unit=M'), 'FM99990') FROM SY_LEUCHTFEUER_V a, SY_LEUCHTFEUER_V b "
                                + "WHERE a.NAME LIKE 'Kampen%' AND b.NAME LIKE 'Quermarke%'", -1),
                new Einrichter.Soll("Sturmfluten belegt, höchster Scheitel List (cm NHN)",
                        "SELECT TO_CHAR(COUNT(*)) || ', ' || TO_CHAR(MAX(CASE WHEN BEZUG = 'NHN' THEN SCHEITEL_LIST_CM END)) || ' cm' FROM SY_STURMFLUT", -1),
                new Einrichter.Soll("Chronik: Abbrüche, Sturmfluten der App, Vorspülungen der App, Serien",
                        "SELECT (SELECT COUNT(*) FROM SY_ABBRUCH) || ', ' || (SELECT COUNT(*) FROM SY_STURMFLUT_LAUF) || ', ' "
                                + "|| (SELECT COUNT(*) FROM SY_VORSPUELUNG WHERE ART = 'APP') || ', ' || (SELECT COUNT(*) FROM SY_SERIE) FROM DUAL", -1),
                new Einrichter.Soll("Küste: Objekte in SY_KUESTE_V", "SELECT TO_CHAR(COUNT(*)) FROM SY_KUESTE_V", -1));
        return e;
    }
}
