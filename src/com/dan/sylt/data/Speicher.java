package com.dan.sylt.data;

import com.dan.store.Ablage;

import java.util.List;

/**
 * Wohin die App speichert: in das Schema SYLT ({@link DbSpeicher}) oder, ohne Datenbank, in Dateien im Ordner
 * {@code daten/} ({@link DateiSpeicher}, F33 A). Beide können dasselbe; die Oberfläche kennt nur diese Schnittstelle.
 * Sitzung und Protokoll kommen aus {@link Ablage} (com.dan.store).
 * <p>
 * Die Chronik (Abbrüche mit Kante und Findlingen, Sturmfluten, Vorspülungen, Serien, Läufe der Regiebücher) schreibt
 * {@link Chronik} im Faden der Daten. Mitgelieferte Einträge werden nie überschrieben, sondern abgelehnt.
 */
public interface Speicher extends Ablage {

    /** Schlüssel der Werte, die eine {@link Ablage.Sicherung} über die Sitzung mitbringt (Spalten von SY_SITZUNG). */
    String REGIEBUCH = "regiebuch", REGIE_SEKUNDE = "regieSekunde", SZENE_MS = "szeneMs", ZEITRAFFER = "zeitraffer", LAGE = "lage", PEGEL = "pegel",
            WINDSTAU = "windstau", STURMFLUT = "sturmflut", ABBRUECHE = "abbrueche";

    Daten.Stand stand() throws Fehler;

    // ------------------------------------------------------------------ Stammdaten

    List<Daten.Quelle> quellen() throws Fehler;

    List<Daten.Befund> befunde() throws Fehler;

    /** Die belegten Sturmfluten seit 1962 (F38 A). */
    List<Daten.Sturmflut> sturmfluten() throws Fehler;

    List<Daten.Leuchtfeuer> leuchtfeuer() throws Fehler;

    List<Daten.Lage> lagen() throws Fehler;

    // ------------------------------------------------------------------ Chronik (F36, F37)

    /** Einen Abbruch mit seinem Stück Kante und den Findlingen schreiben; gibt seine Nummer zurück. */
    long abbruch(Daten.Abbruch a, Long sitzung) throws Fehler;

    /** Die Abbrüche, neueste zuerst, höchstens n (mit Kante, ohne Findlinge). */
    List<Daten.Abbruch> abbrueche(int n) throws Fehler;

    /** Alle Stücke der Kante: der Stand 2024 und die nach den Abbrüchen (in der Reihenfolge der Szenenzeit). */
    List<Daten.Kantenstueck> kanten() throws Fehler;

    /** Einen Lauf einer Sturmflut anlegen (id 0) oder fortschreiben; gibt seine Nummer zurück. */
    long sturmflutLauf(Daten.SturmflutLauf l, Long sitzung) throws Fehler;

    List<Daten.SturmflutLauf> sturmflutLaeufe(int n) throws Fehler;

    /** Eine Vorspülung der App anlegen (id 0) oder fortschreiben; gibt ihre Nummer zurück. */
    long vorspuelung(Daten.Vorspuelung v, Long sitzung) throws Fehler;

    /** Alle Vorspülungen: belegte (nach Jahr), dann die der App (neueste zuerst). */
    List<Daten.Vorspuelung> vorspuelungen() throws Fehler;

    void serie(Daten.Serie s, Long sitzung) throws Fehler;

    void regielauf(Daten.Regielauf l, Long sitzung) throws Fehler;

    /** Die eigenen Zeilen der Chronik einer Sitzung löschen; gibt zurück, wie viele. */
    int chronikLeeren(long sitzung) throws Fehler;

    // ------------------------------------------------------------------ Regie und Kamera

    List<Daten.Regiebuch> regiebuecher() throws Fehler;
    void speichern(Daten.Regiebuch r) throws Fehler;
    void loeschenRegiebuch(String name) throws Fehler;

    List<Daten.Fahrt> fahrten() throws Fehler;

    /** Ansichten ohne Zustand und Vorschau (für Listen): feste, Blicke, Vorlagen, dann gemerkte (neueste zuerst). */
    List<Daten.Ansicht> ansichten() throws Fehler;
    /** Eine gemerkte Ansicht ganz, mit Zustand und Vorschau. */
    Daten.Ansicht ansicht(long id) throws Fehler;
    /** Speichert eine neue gemerkte Ansicht und gibt ihre Nummer zurück. */
    long speichern(Daten.Ansicht a, Long sitzung) throws Fehler;
    void loeschenAnsicht(long id) throws Fehler;

    // ------------------------------------------------------------------ Packages (F43)

    /** Presets eines Package (paket = „com.dan.surf“, klasse = „Surf“); null für alle. */
    List<Daten.PaketPreset> presets(String paket, String klasse) throws Fehler;
    void speichern(Daten.PaketPreset p) throws Fehler;
    void loeschenPreset(String paket, String klasse, String name) throws Fehler;

    // ------------------------------------------------------------------ Betrieb

    /** Das Vorschaubild der laufenden Sitzung (PNG, alle 2 min mit dem Zustand, F42 A). */
    void vorschau(long sitzung, byte[] png) throws Fehler;

    /** Die letzte Sitzung mit Zustand vor der laufenden (oder null), mit Vorschau. */
    Daten.Sitzung letzteSitzung(long ausser) throws Fehler;

    /** Die letzten n Zeilen des Protokolls zum Anzeigen. */
    List<String> letzteZeilen(int n) throws Fehler;
}
