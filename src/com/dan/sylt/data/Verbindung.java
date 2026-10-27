package com.dan.sylt.data;

import com.dan.store.Db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Das Schema SYLT (Werkbuch Abschnitt 10, F29 A): Ort von {@code db/}, Zugang aus {@code db/db.properties}. Schema und
 * Benutzer legt {@code db/User.sql} an ({@code manage_user('Sylt', 'sy')} auf PDBORCL, Rolle DB_CONNECT). Phase 1
 * verbindet nur und meldet, wer man ist; Tabellen ({@code SY_…}, Sequenz und Trigger, nie IDENTITY), Einrichter,
 * Chronik und „Weiter wie zuletzt“ kommen mit Phase 16 und ihrem eigenen Fragenblock.
 */
public final class Verbindung {
    private Verbindung() { }

    public static final String PRAEFIX = "SY_";
    /** Umgebungsvariable für ein leeres Passwort (wie in db.properties.example beschrieben). */
    public static final String ENV = "Sylt_DB_PASSWORD";

    /** Der Stand der Verbindung (für Startbild, Bedienfeld und Statuszeile). */
    private static volatile String stand = "ohne Datenbank";

    /** Der Ordner db: -Dsylt.db.dir, sonst ./db, sonst db neben dem Jar. */
    public static File ordner() { return Db.ordner("sylt.db.dir", Verbindung.class); }

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
}
