package com.dan.sylt.ui;

import com.dan.sylt.data.Chronik;
import com.dan.sylt.data.Daten;
import com.dan.sylt.data.Zustand;
import com.dan.sylt.gl.Quality;
import com.dan.sylt.gl.Renderer;
import com.dan.store.Felder;
import com.dan.store.Protokoll;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.List;
import java.util.Map;

/**
 * „Weiter wie zuletzt“ (F42 A, F44 A): einen gesicherten Zustand herstellen, sobald die Szene ihre ersten Bilder hat
 * (höchstens 60 s warten). Uhr, Zeitraffer, Wetterregie, Sturmflut mit Windstau, Kamera, alle Regler – und das Kliff,
 * wie es war: Abbrüche, Sturzkegel, Kehle, Kante, Spur der Eiszeit und die Vorspülung. Ein Stück, das gerade gerechnet
 * wurde, fällt weg (die Kehle bleibt, es bricht wieder); ein Regiebuch beginnt man neu.
 */
public final class Weiter {
    private Weiter() { }

    private static boolean bereit(Renderer r) { return r.frames() > 10; }

    /** Wartet höchstens 60 s auf die Szene, dann stellt es her. */
    public static void anwenden(Renderer r, Daten.Sitzung s, Chronik chronik) {
        if (s == null || s.zustand() == null) return;
        long bis = System.currentTimeMillis() + 60_000;
        Timer t = new Timer(250, null);
        t.addActionListener(e -> {
            if (!bereit(r) && System.currentTimeMillis() < bis) return;
            t.stop();
            jetzt(r, s, chronik);
        });
        t.start();
    }

    /** Ein Regiebuch im Kino, sobald die Szene steht (höchstens 60 s warten). */
    public static void kino(Renderer r, com.dan.sylt.scene.Regie.Book b) {
        long bis = System.currentTimeMillis() + 60_000;
        Timer t = new Timer(250, null);
        t.addActionListener(e -> {
            if (!bereit(r) && System.currentTimeMillis() < bis) return;
            t.stop();
            r.kinoStart(b);
        });
        t.start();
    }

    /** Sofort herstellen (die Szene steht schon; aus dem Swing-Faden). */
    public static void jetzt(Renderer r, Daten.Sitzung s, Chronik chronik) {
        Map<String, String> z = Felder.ausText(s.zustand());
        Quality q = Zustand.qualitaet(z);
        if (q != null && q != r.quality()) r.setQuality(q);
        if (z.containsKey("app.ton")) r.klang.setOn(Boolean.parseBoolean(z.get("app.ton")));
        if (r.regie.running()) r.regieStop();
        List<String> f = Zustand.anwenden(r, z);
        String rest = z.containsKey("regiebuch.name") ? " · „" + z.get("regiebuch.name") + "“ lief: Block REGIEBÜCHER" : "";
        String kliff = z.get("kliff.stand");
        Protokoll.info("Sitzung", "Weiter wie zuletzt (Sitzung " + s.id() + "): " + z.size() + " Werte" + (f.isEmpty() ? "" : ", nicht hergestellt: " + String.join(", ", f)));
        r.hudMeldung("Weiter wie zuletzt · " + z.getOrDefault("uhr.text", "") + rest);
        if (kliff == null) return;
        r.kliffstandSetzen(kliff, z.get("vorspuelung.stand"), ok -> SwingUtilities.invokeLater(() -> {
            if (chronik != null) chronik.hergestellt();
            String m = ok ? String.format("Weiter wie zuletzt · das Kliff mit %s Abbrüchen%s · %s", z.getOrDefault("kliff.abbrueche", "0"),
                    z.containsKey("vorspuelung.stand") ? " und der Vorspülung" : "", z.getOrDefault("uhr.text", "")) : "Weiter wie zuletzt: das Kliff ließ sich nicht herstellen (anderes Gelände?)";
            r.hudMeldung(m + rest);
            if (ok) Protokoll.info("Sitzung", m); else Protokoll.warnung("Sitzung", m);
        }));
    }
}
