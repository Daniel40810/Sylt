package com.dan.sylt.data;

import com.dan.store.Ablage;
import com.dan.store.Protokoll;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Übernahme aus den Dateien in die Datenbank (F33 A, „Übernehmen“ im Block DATEN): die Chronik (Abbrüche mit Kante und
 * Findlingen, Läufe der Sturmflut, Vorspülungen, Serien, Läufe der Regiebücher) und die eigenen Regiebücher, Ansichten
 * und Presets. Die Nummern der Sitzungen aus den Dateien gibt es in der Datenbank nicht; die Zeilen kommen ohne Sitzung
 * hinein. Danach heißen die CSV {@code *.uebernommen}, damit sie kein zweites Mal kommen; die übrigen Dateien bleiben.
 */
public final class Uebernahme {
    private Uebernahme() { }

    /** Was übernommen wurde, als Text („3 Abbrüche, 1 Sturmflut …“). */
    public static String uebernehmen(DateiSpeicher von, Speicher nach) throws Ablage.Fehler {
        int ab = 0, sf = 0, vs = 0, se = 0, rl = 0, rb = 0, an = 0, pr = 0;
        for (Daten.Abbruch a : von.abbruecheAlle()) { nach.abbruch(a, null); ab++; }
        List<Daten.SturmflutLauf> sl = new ArrayList<>(von.sturmflutLaeufe(Integer.MAX_VALUE));
        java.util.Collections.reverse(sl);
        for (Daten.SturmflutLauf l : sl) { nach.sturmflutLauf(l.mitId(0), null); sf++; }
        List<Daten.Vorspuelung> vl = new ArrayList<>();
        for (Daten.Vorspuelung v : von.vorspuelungen()) if ("APP".equals(v.art())) vl.add(v);
        java.util.Collections.reverse(vl);
        for (Daten.Vorspuelung v : vl) {
            nach.vorspuelung(new Daten.Vorspuelung(0, v.name(), v.art(), v.jahr(), v.menge(), v.nordVon(), v.nordBis(), v.strecke(), v.flaeche(), v.beginnMs(), v.hoeheFuss(),
                    v.breite(), v.halbwertTage(), v.bemerkung(), null), null);
            vs++;
        }
        for (Daten.Serie s : von.serienAlle()) { nach.serie(s, null); se++; }
        for (Daten.Regielauf l : von.regielaeufe()) {
            byte[] png = null;
            if (l.blattDatei() != null && !l.blattDatei().isEmpty()) {
                try { png = java.nio.file.Files.readAllBytes(new File(l.blattDatei()).toPath()); } catch (Exception x) { /* ohne Blatt */ }
            }
            nach.regielauf(new Daten.Regielauf(l.name(), l.beginnMs(), l.endeMs(), l.vollstaendig(), l.kino(), l.bilderMittel(), l.gpuMs(), l.gewartetS(), l.ausgewichen(),
                    l.fotos(), l.qualitaet(), png, l.blattDatei()), null);
            rl++;
        }
        for (Daten.Regiebuch r : von.regiebuecher()) if (Daten.EIGEN.equals(r.herkunft())) { nach.speichern(r); rb++; }
        for (Daten.Ansicht a : von.ansichten()) if (Daten.EIGEN.equals(a.herkunft())) { nach.speichern(von.ansicht(a.id()), null); an++; }
        for (Daten.PaketPreset p : von.presets(null, null)) if (Daten.EIGEN.equals(p.herkunft())) { nach.speichern(p); pr++; }
        List<String> umbenannt = new ArrayList<>();
        for (File f : von.chronikDateien()) {
            if (!f.isFile()) continue;
            File neu = new File(f.getPath() + ".uebernommen");
            for (int i = 2; neu.exists(); i++) neu = new File(f.getPath() + "." + i + ".uebernommen");
            if (f.renameTo(neu)) umbenannt.add(f.getName());
        }
        String t = String.format(java.util.Locale.GERMAN, "%d Abbrüche, %d Sturmfluten, %d Vorspülungen, %d Serien, %d Läufe der Regiebücher, %d Regiebücher, "
                + "%d Ansichten, %d Presets", ab, sf, vs, se, rl, rb, an, pr);
        Protokoll.info("Übernahme", t + " aus " + von.wo() + " nach " + nach.wo() + (umbenannt.isEmpty() ? "" : "; umbenannt: " + String.join(", ", umbenannt)));
        return t;
    }

    /** Ob es in den Dateien etwas zu übernehmen gibt. */
    public static boolean etwasDa(DateiSpeicher d) {
        for (File f : d.chronikDateien()) if (f.isFile()) return true;
        return false;
    }
}
