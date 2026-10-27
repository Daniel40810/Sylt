package com.dan.sylt.tools;

import com.dan.sylt.scene.Terrain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Die Aufbereitung von Phase 2 ({@code ant aufbereitung}): liest die Rohdaten einmal und schreibt alles, was die App
 * braucht, nach {@code daten/} (Werkbuch 2.4). Schritte in dieser Folge, einzeln mit {@code -Dschritt=gelaende,kante}:
 * <ol>
 * <li>{@code baw}: Zuschnitt der BAW nach {@code templates/rasters_BAW/} ({@link BawZuschnitt});</li>
 * <li>{@code gelaende}: DGM1 und BAW zum Gelände ({@link GelaendeImport});</li>
 * <li>{@code kante}: bDOM zu Bewuchs, Kliffkante und 20-cm-Band ({@link KanteImport});</li>
 * <li>{@code luftbild}: DOP20 zu Klassen und Arten ({@link LuftbildImport});</li>
 * <li>{@code bauten}: LOD2 zu Baukörpern ({@link BautenImport});</li>
 * <li>{@code inhalt}: {@code daten/INHALT.txt} mit Prüfsummen ({@link Inhalt}).</li>
 * </ol>
 * Protokoll in {@code daten/aufbereitung.log}, Prüfbilder in {@code docs/status/}.
 */
public final class Aufbereitung {
    public static final List<String> SCHRITTE = List.of("baw", "gelaende", "kante", "luftbild", "bauten", "inhalt");

    private Aufbereitung() { }

    public static void main(String[] a) throws Exception {
        String sel = a.length > 0 ? String.join(",", a) : System.getProperty("schritt", "alle");
        if (sel.isBlank() || sel.startsWith("$")) sel = "alle";
        List<String> wahl = sel.equals("alle") ? SCHRITTE : Arrays.asList(sel.toLowerCase(Locale.ROOT).split("[,; ]+"));
        for (String s : wahl) if (!SCHRITTE.contains(s)) throw new IllegalArgumentException("Schritt " + s + " gibt es nicht (" + String.join(", ", SCHRITTE) + ")");
        File daten = new File("daten");
        daten.mkdirs();
        long t0 = System.nanoTime();
        try (PrintWriter logf = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(daten, "aufbereitung.log"), true), StandardCharsets.UTF_8), true)) {
            Consumer<String> log = s -> { System.out.println(s); logf.println(s); };
            log.accept("=== Sylt Aufbereitung " + java.time.LocalDateTime.now().withNano(0) + " · Schritte " + String.join(", ", wahl));
            Rohdaten r = Rohdaten.laden();
            log.accept(r.toString());
            boolean braucht = wahl.stream().anyMatch(s -> !s.equals("inhalt"));
            List<String> fehlt = r.fehlt();
            if (braucht && !fehlt.isEmpty()) {
                fehlt.forEach(f -> log.accept("FEHLT: " + f));
                throw new IOException("Rohdaten fehlen; Pfad in " + Rohdaten.DATEI + " prüfen");
            }
            for (String s : SCHRITTE) {
                if (!wahl.contains(s)) continue;
                long t1 = System.nanoTime();
                log.accept("--- " + s);
                switch (s) {
                    case "baw" -> BawZuschnitt.schneiden(r, log);
                    case "gelaende" -> GelaendeImport.bild(GelaendeImport.importieren(r, Terrain.defaultDir(), log), GelaendeImport.BILD);
                    case "kante" -> KanteImport.importieren(r, daten, log);
                    case "luftbild" -> LuftbildImport.importieren(r, daten, log);
                    case "bauten" -> BautenImport.importieren(r, daten, log);
                    case "inhalt" -> Inhalt.schreiben(daten, log);
                    default -> { }
                }
                log.accept(String.format(Locale.GERMAN, "    %s fertig in %.1f s", s, (System.nanoTime() - t1) / 1e9));
            }
            log.accept(String.format(Locale.GERMAN, "=== fertig in %.1f s", (System.nanoTime() - t0) / 1e9));
        }
    }
}
