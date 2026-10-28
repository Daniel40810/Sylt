package com.dan.sylt.tools;

import com.dan.sylt.data.Daten;
import com.dan.sylt.data.Startdaten;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Phase 19 (F56 A, aus Brocken): {@code docs/QUELLEN.md} aus denselben Zeilen wie die Tabellen {@code SY_QUELLE},
 * {@code SY_BEFUND}, {@code SY_STURMFLUT} und {@code SY_VORSPUELUNG} ({@code data/quellen.txt}, {@code befunde.txt},
 * {@code sturmfluten.txt}, {@code vorspuelungen.txt}), dazu die Rohdaten und ihre Aufbereitung aus
 * {@code data/rohdaten.md} (von Hand gepflegt). {@code ant quellen}; die Prüfung meldet, wenn die Datei nicht mehr zu
 * den Listen passt.
 */
public final class Quellenliste {
    private Quellenliste() { }

    public static void main(String[] args) throws IOException {
        File f = new File(args.length > 0 ? args[0] : "docs/QUELLEN.md");
        if (f.getParentFile() != null) f.getParentFile().mkdirs();
        String t = text();
        Files.writeString(f.toPath(), t, StandardCharsets.UTF_8);
        System.out.println("Quellenliste: " + f + " (" + t.length() + " Zeichen)");
    }

    private static String zelle(Object o) { return o == null ? "" : o.toString().replace("|", "\\|").replace("\n", " ").strip(); }

    private static final Map<String, String> BEFUND = Map.of("BELEGT", "belegt", "GEMESSEN", "gemessen", "GESTALTET", "gestaltet [G]");

    static String rohdaten() {
        try (InputStream in = Startdaten.class.getResourceAsStream("rohdaten.md")) {
            if (in == null) return "";
            String s = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return s.replaceFirst("(?s)^<!--.*?-->\\s*", "");
        } catch (IOException e) {
            return "";
        }
    }

    /** Der ganze Text (Markdown). */
    public static String text() {
        List<Daten.Quelle> q = Startdaten.quellen();
        List<Daten.Befund> b = Startdaten.befunde();
        List<Daten.Sturmflut> sf = Startdaten.sturmfluten();
        List<Daten.Vorspuelung> vs = Startdaten.vorspuelungen();
        Map<String, Integer> art = new TreeMap<>();
        for (Daten.Befund x : b) art.merge(x.befund(), 1, Integer::sum);
        Map<String, Integer> nutzung = new LinkedHashMap<>();
        for (Daten.Quelle z : q) nutzung.put(z.kurz(), 0);
        for (Daten.Befund x : b) for (String k : x.quellen()) nutzung.merge(k, 1, Integer::sum);
        for (Daten.Sturmflut x : sf) if (x.quelle() != null) for (String k : x.quelle().split("\\s*,\\s*")) nutzung.merge(k, 1, Integer::sum);
        for (Daten.Vorspuelung x : vs) if (x.quelle() != null) for (String k : x.quelle().split("\\s*,\\s*")) nutzung.merge(k, 1, Integer::sum);
        StringBuilder s = new StringBuilder();
        s.append("# Sylt – Quellen und Befunde\n\n");
        s.append("Erzeugt von `tools.Quellenliste` (`ant quellen`) aus `src/com/dan/sylt/data/quellen.txt`, `befunde.txt`, `sturmfluten.txt`, `vorspuelungen.txt` ")
                .append("und `rohdaten.md`. Dieselben Zeilen stehen in den Tabellen `SY_QUELLE`, `SY_BEFUND`, `SY_STURMFLUT` und `SY_VORSPUELUNG` (Schema SYLT). ")
                .append("Jeder Befund ist **belegt** (eine Quelle nennt es), **gemessen** (aus Daten wie dem DGM1 oder den Küstenhöhen der BAW abgeleitet) ")
                .append("oder **gestaltet** [G] (so gebaut, dass es glaubhaft ist, ohne Beleg).\n\n");
        s.append(String.format(Locale.GERMAN, "%d Quellen · %d Befunde (%d belegt, %d gemessen, %d gestaltet) · %d Sturmfluten · %d Vorspülungen%n%n",
                q.size(), b.size(), art.getOrDefault("BELEGT", 0), art.getOrDefault("GEMESSEN", 0), art.getOrDefault("GESTALTET", 0), sf.size(), vs.size()));
        s.append("Geodaten: © GeoBasis-DE/LVermGeo SH, Datenlizenz Deutschland – Namensnennung – Version 2.0 (Daten verändert). Küstenhöhen: © Bundesanstalt für Wasserbau (BAW), ")
                .append("EasyGSH-DB. Die Lizenzvermerke der einzelnen Daten stehen in den Quellen und unten bei den Rohdaten.\n\n");
        s.append("## Quellen\n\n| Kurz | Art | Titel | Abgerufen | Bemerkung | genutzt |\n|---|---|---|---|---|---|\n");
        for (Daten.Quelle z : q) {
            String titel = zelle(z.titel()), url = z.url();
            s.append("| ").append(zelle(z.kurz())).append(" | ").append(zelle(z.art())).append(" | ")
                    .append(url == null || url.isBlank() ? titel : "[" + titel + "](" + url.strip() + ")").append(" | ")
                    .append(zelle(z.abgerufen())).append(" | ").append(zelle(z.bemerkung())).append(" | ")
                    .append(nutzung.getOrDefault(z.kurz(), 0)).append(" |\n");
        }
        s.append("\n## Befunde\n\n| Thema | Gegenstand | Befund | Text | Quellen |\n|---|---|---|---|---|\n");
        for (Daten.Befund x : b)
            s.append("| ").append(zelle(x.thema())).append(" | ").append(zelle(x.gegenstand())).append(" | ")
                    .append(BEFUND.getOrDefault(x.befund(), zelle(x.befund()))).append(" | ").append(zelle(x.text())).append(" | ")
                    .append(String.join(", ", x.quellen())).append(" |\n");
        s.append("\n## Belegte Sturmfluten seit 1962\n\nScheitel in cm (Bezug je Zeile). Ein Kliffverlust in Metern ist für kein Ereignis belegt; ")
                .append("die App rechnet ihn aus Pegel, Auflauf und Kehle.\n\n| Name | Datum | List | Hörnum | Bezug | Kliff | Folgen | Quelle |\n|---|---|---|---|---|---|---|---|\n");
        for (Daten.Sturmflut x : sf)
            s.append("| ").append(zelle(x.name())).append(" | ").append(zelle(x.datum())).append(" | ").append(zelle(x.list())).append(" | ")
                    .append(zelle(x.hoernum())).append(" | ").append(zelle(x.bezug())).append(" | ").append(zelle(x.kliffverlustText())).append(" | ")
                    .append(zelle(x.folgen())).append(" | ").append(zelle(x.quelle())).append(" |\n");
        s.append("\n## Belegte Vorspülungen vor Wenningstedt und Kampen\n\n| Name | Jahr | Menge (m³) | Strecke | Bemerkung | Quelle |\n|---|---|---|---|---|---|\n");
        for (Daten.Vorspuelung x : vs)
            s.append("| ").append(zelle(x.name())).append(" | ").append(zelle(x.jahr())).append(" | ")
                    .append(x.menge() == null ? "" : String.format(Locale.GERMAN, "%,d", x.menge())).append(" | ").append(zelle(x.strecke())).append(" | ")
                    .append(zelle(x.bemerkung())).append(" | ").append(zelle(x.quelle())).append(" |\n");
        s.append("\n# Rohdaten und Aufbereitung\n\nDie Rohdaten liest nur `ant aufbereitung` (`com.dan.sylt.tools`), einmal; die App liest nur `daten/` (Werkbuch 2.4). ")
                .append("Der Pfad der Rohdaten steht in `daten/rohdaten.properties`; was die Aufbereitung geschrieben hat, steht mit Prüfsummen in `daten/INHALT.txt`.\n\n");
        s.append(rohdaten());
        s.append("\n## Gestaltet [G] in der Szene\n\nWas die Szene zeigt, ohne dass eine Quelle es so nennt, ist im Werkbuch und im Code mit [G] markiert: etwa Riffe und Rinnen ")
                .append("unter dem 10-m-Raster, die Mächtigkeiten der Deckschichten des Kliffs, die Strandkörbe und Leute nach Wetter und Stunde, die Vögel, die Regiebücher. ")
                .append("Die Befunde oben mit „gestaltet“ nennen die wichtigsten.\n");
        return s.toString();
    }
}
