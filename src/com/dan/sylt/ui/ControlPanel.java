package com.dan.sylt.ui;

import com.dan.sylt.app.Bibliothek;
import com.dan.sylt.camera.Ansichten;
import com.dan.sylt.camera.Camera;
import com.dan.sylt.data.Verbindung;
import com.dan.sylt.gl.ComputeProbe;
import com.dan.sylt.gl.Quality;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.gl.SceneView;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Sun;
import com.dan.fbutton.FButton;
import com.dan.fcheckbox.FCheckBox;
import com.dan.fcollapsiblepanel.FCollapsiblePanel;
import com.dan.fcombobox.FComboBox;
import com.dan.fslider.FSlider;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Das Bedienfeld rechts als Klappblöcke (aus Brocken, entschlackt). Die Blöcke stehen nach dem Werkbuch (Abschnitt 9)
 * schon alle da: KAMERA, ZEIT, WETTER, SEE, BRANDUNG, SAND, KLIFF, TÜRME, KLANG, REGIEBÜCHER, DATEN, TECHNIK. In Phase 1
 * arbeiten KAMERA (neun Ansichten, frei fliegen, kreisen), ZEIT (Datum, Uhrzeit, Zeitraffer, Sonne), SEE (Pegel von
 * Hand, die See als Platzhalter an/aus), BRANDUNG und SAND (die Hüllen von {@code surf} und {@code dune} mit ihren
 * offenen Werten), DATEN (Verbindung) und TECHNIK (Qualität, Auflösung, Belichtung, HUD, Marken, Foto, Shader,
 * Compute-Test, Physik, Packages); die übrigen sagen, in welcher Phase sie kommen. Offen bleibt, was zuletzt offen war
 * (je Rechner gemerkt). Jeder Regler schreibt direkt in die öffentlichen Felder von Uhr, Kamera, Verdrahtung und
 * Renderer; es gibt keinen zweiten Zustand.
 */
public final class ControlPanel extends JPanel implements javax.swing.Scrollable {
    private final SceneView view;
    private final Renderer r;
    private final SceneClock clock;
    private final JLabel dateLbl = label(""), timeLbl = label(""), sunLbl = small(""), camLbl = small(""), perfLbl = small(""), computeLbl = small(""),
            glLbl = small(""), physLbl = small(""), gelLbl = small(""), hudLbl = small(""), scaleLbl = label(""), qualLbl = small(""), pkgLbl = small(""),
            pegelLbl = label(""), surfLbl = small(""), hsLbl = label(""), tpLbl = label(""), dirLbl = label(""), duneLbl = small(""), windLbl = label(""),
            windDirLbl = label(""), dbLbl = small(""), oktasLbl = label(""), highLbl = label(""), sichtLbl = label(""), wetterLbl = small("");
    private final FSlider time = new FSlider(0, 24 * 60 - 1, (int) (SceneClock.START_HOUR * 60)), scaleSl = new FSlider(50, 100, 85), expSl = new FSlider(-30, 30, 0),
            pegelSl = new FSlider(-150, 400, 0), hsSl = new FSlider(1, 60, 10), tpSl = new FSlider(3, 16, 8), dirSl = new FSlider(180, 360, 285),
            windSl = new FSlider(0, 30, 6), windDirSl = new FSlider(0, 359, 270), oktasSl = new FSlider(0, 8, 3), highSl = new FSlider(0, 100, 35),
            sichtSl = new FSlider(2, 80, 40);
    private final FCheckBox free = new FCheckBox("Frei fliegen (F)"), orbit = new FCheckBox("Von selbst kreisen (R)"),
            marks = new FCheckBox("Marken (UTM-Raster, Höhenlinien, Wasserlinie, Kern, Orte)"), autoBox = new FCheckBox("Auflösung selbst regeln"),
            meerBox = new FCheckBox("See zeigen (com.dan.ocean)"), wolkenBox = new FCheckBox("Wolken zeigen (com.dan.cloud)"),
            bodenBox = new FCheckBox("Bodendecke zeigen (com.dan.ground, Biom Sylt)");
    private FComboBox lapseBox, qualBox, hudBox, artBox;
    private boolean fromScene;
    private int ticks;
    private JPanel target = this;
    private static final Preferences PREFS = prefs();
    private final java.util.List<Object[]> bloecke = new java.util.ArrayList<>();
    private final java.util.List<FCollapsiblePanel> zu = new java.util.ArrayList<>();
    private static final double[] LAPSE = {0, 1, 10, 60, 180, 720, 3600};

    /** Die Reihenfolge der Blöcke (Werkbuch, Abschnitt 9). */
    public static final String[] REIHENFOLGE = {"KAMERA", "ZEIT", "WETTER", "SEE", "BRANDUNG", "SAND", "KLIFF", "TÜRME", "KLANG", "REGIEBÜCHER", "DATEN", "TECHNIK"};

    private static Preferences prefs() {
        try { return Preferences.userRoot().node("com/dan/sylt/bedienfeld"); } catch (RuntimeException | Error e) { return null; }
    }

    public ControlPanel(SceneView view) {
        this.view = view;
        this.r = view.renderer();
        this.clock = r.clock;
        setBackground(Theme.bg());
        setOpaque(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 8, 16, 10));

        // ============================================================== KAMERA
        block("KAMERA", true);
        put(camLbl);
        JPanel v1 = grid(2), v2 = grid(2), v3 = grid(2), v4 = grid(2), v5 = grid(2);
        JPanel[] rows = {v1, v1, v2, v2, v3, v3, v4, v4, v5};
        for (Ansichten.Ansicht a : Ansichten.ALLE) {
            FButton b = new FButton(a.key() + " " + a.name());
            b.setToolTipText(Ansichten.describe(a, Renderer.PHASE));
            b.addActionListener(e -> ansicht(a));
            rows[a.key() - 1].add(b);
            view.setKeyAction(java.awt.event.KeyEvent.VK_0 + a.key(), () -> javax.swing.SwingUtilities.invokeLater(() -> ansicht(a)));
            view.setKeyAction(java.awt.event.KeyEvent.VK_NUMPAD0 + a.key(), () -> javax.swing.SwingUtilities.invokeLater(() -> ansicht(a)));
        }
        put(v1);
        put(v2);
        put(v3);
        put(v4);
        put(v5);
        free.addActionListener(e -> { view.setMode(free.isSelected() ? Camera.Mode.FREI : Camera.Mode.KREISEN); view.requestFocusInWindow(); });
        orbit.addActionListener(e -> { view.setAutoOrbit(orbit.isSelected()); view.requestFocusInWindow(); });
        put(free);
        put(orbit);
        view.setModeListener(m -> javax.swing.SwingUtilities.invokeLater(() -> free.setSelected(m == Camera.Mode.FREI)));
        view.setOrbitListener(b -> javax.swing.SwingUtilities.invokeLater(() -> orbit.setSelected(b)));
        JPanel home = grid(1);
        FButton hb = new FButton("Pos1 Grundansicht");
        hb.addActionListener(e -> { view.goHome(); view.requestFocusInWindow(); });
        home.add(hb);
        put(home);
        put(small("<html><body style='width:215px'>Die Ansichten stehen in Phase 1 auf der Bühne (Küste nach den Profilen der BAW); "
                + "Phase 2 legt sie auf das DGM1. „Mit der Welle“ kommt mit Phase 6.</body></html>"));

        // ============================================================== ZEIT
        block("ZEIT", true);
        put(dateLbl);
        JPanel days = grid(3);
        FButton minus = new FButton("− Tag"), heute = new FButton("20.7.2026"), plus = new FButton("+ Tag");
        minus.addActionListener(e -> { clock.shiftDays(-1); syncFromScene(); view.requestFocusInWindow(); });
        plus.addActionListener(e -> { clock.shiftDays(1); syncFromScene(); view.requestFocusInWindow(); });
        heute.setToolTipText("Zurück zum Start der Szene: " + new SceneClock().label());
        heute.addActionListener(e -> { clock.set(SceneClock.START_DATE, SceneClock.START_HOUR); syncFromScene(); view.requestFocusInWindow(); });
        days.add(minus); days.add(heute); days.add(plus);
        put(days);
        FComboBox monthBox = new FComboBox(new Object[]{"Monat springen …", "20. Januar", "20. Februar", "20. März", "20. April", "20. Mai", "20. Juni",
                "20. Juli", "20. August", "20. September", "20. Oktober", "20. November", "20. Dezember"});
        monthBox.setToolTipText("Zum 20. eines Monats im laufenden Jahr, gleiche Uhrzeit (Strandkörbe und Jahreszeit kommen mit season)");
        monthBox.addActionListener(e -> {
            int i = monthBox.getSelectedIndex();
            if (i <= 0) return;
            clock.set(LocalDate.of(clock.date().getYear(), i, 20), clock.hour());
            monthBox.setSelectedIndex(0);
            syncFromScene();
            view.requestFocusInWindow();
        });
        put(monthBox);
        put(timeLbl);
        put(time);
        time.addChangeListener(e -> { if (!fromScene) clock.setHour(time.getValue() / 60.0); labels(); });
        put(label("Zeitraffer"));
        lapseBox = new FComboBox(new Object[]{"Uhr steht", "Echtzeit", "10-fach", "1 min/s", "3 min/s", "12 min/s (ein Tag in 2 min)", "1 h/s"});
        lapseBox.setSelectedIndex(1);
        lapseBox.addActionListener(e -> { clock.lapse = LAPSE[Math.max(0, lapseBox.getSelectedIndex())]; view.requestFocusInWindow(); });
        put(lapseBox);
        put(sunLbl);
        put(small("<html><body style='width:215px'>Deutsche Zeit (MEZ, im Sommer MESZ). Am Roten Kliff steht die Sonne rund 27 Minuten nach "
                + "12 Uhr MEZ im Süden. Himmel, Sonne, Mond und Sterne rechnet ab Phase 3 com.dan.sky.</body></html>"));

        // ============================================================== WETTER (Phase 3)
        block("WETTER", true);
        put(wetterLbl);
        put(oktasLbl);
        put(oktasSl);
        oktasSl.addChangeListener(e -> { if (!fromScene) r.wiring.oktas = oktasSl.getValue(); labels(); });
        put(label("Art der unteren Wolken"));
        artBox = new FComboBox(com.dan.cloud.CloudType.values());
        artBox.setSelectedItem(r.wiring.cloudType);
        artBox.addActionListener(e -> { if (!fromScene) r.wiring.cloudType = (com.dan.cloud.CloudType) artBox.getSelectedItem(); view.requestFocusInWindow(); });
        put(artBox);
        put(highLbl);
        put(highSl);
        highSl.addChangeListener(e -> { if (!fromScene) r.wiring.highCover = highSl.getValue() / 100.0; labels(); });
        put(sichtLbl);
        put(sichtSl);
        sichtSl.addChangeListener(e -> { if (!fromScene) r.wiring.visibilityKm = sichtSl.getValue(); labels(); });
        JPanel lagen = grid(2), lagen2 = grid(2);
        FButton abend = new FButton("Sommerabend"), heiter = new FButton("Heiter"), bedeckt = new FButton("Bedeckt"), grau = new FButton("Grau");
        abend.addActionListener(e -> wetter(3, com.dan.cloud.CloudType.CUMULUS, 0.35, 40));
        heiter.addActionListener(e -> wetter(1, com.dan.cloud.CloudType.CUMULUS, 0.1, 60));
        bedeckt.addActionListener(e -> wetter(6, com.dan.cloud.CloudType.STRATOCUMULUS, 0.3, 25));
        grau.addActionListener(e -> wetter(8, com.dan.cloud.CloudType.STRATUS, 0, 8));
        lagen.add(abend); lagen.add(heiter); lagen2.add(bedeckt); lagen2.add(grau);
        put(lagen);
        put(lagen2);
        wolkenBox.setSelected(r.showClouds);
        wolkenBox.addActionListener(e -> { r.showClouds = wolkenBox.isSelected(); view.requestFocusInWindow(); });
        put(wolkenBox);
        bodenBox.setSelected(r.showGround);
        bodenBox.addActionListener(e -> { r.showGround = bodenBox.isSelected(); view.requestFocusInWindow(); });
        put(bodenBox);
        put(small("<html><body style='width:215px'>Seit Phase 3: Himmel aus com.dan.sky (Sonne, Mond, Sterne, Streuung über 54,9° N), "
                + "Wolken aus com.dan.cloud mit Schatten, Bodendecke um das Auge aus com.dan.ground. Die Wetterregie mit Lagen bis zur "
                + "Sturmflut (com.dan.weather) kommt mit Phase 13.</body></html>"));

        // ============================================================== SEE
        block("SEE", true);
        put(pegelLbl);
        put(pegelSl);
        pegelSl.addChangeListener(e -> { if (!fromScene) r.wiring.pegel = pegelSl.getValue() / 100.0; labels(); });
        meerBox.setSelected(r.showMeer);
        meerBox.addActionListener(e -> { r.showMeer = meerBox.isSelected(); view.requestFocusInWindow(); });
        put(meerBox);
        put(small("<html><body style='width:215px'>Pegel von Hand (m über NHN). Ab Phase 7a kommt er aus Tide.westerland() (F25 A) mit "
                + "Windstau. Die offene See (com.dan.ocean, seit Phase 3) nimmt den Wind aus SAND und die Dünung aus BRANDUNG; "
                + "der Grund ist das Gelände.</body></html>"));

        // ============================================================== BRANDUNG (Hülle)
        block("BRANDUNG", true);
        put(surfLbl);
        put(hsLbl);
        put(hsSl);
        put(tpLbl);
        put(tpSl);
        put(dirLbl);
        put(dirSl);
        hsSl.addChangeListener(e -> { if (!fromScene) r.wiring.surf.hs = hsSl.getValue() / 10.0; labels(); });
        tpSl.addChangeListener(e -> { if (!fromScene) r.wiring.surf.tp = tpSl.getValue(); labels(); });
        dirSl.addChangeListener(e -> { if (!fromScene) r.wiring.surf.seaFromDeg = dirSl.getValue(); labels(); });
        JPanel sp = grid(3);
        FButton ruhig = new FButton("Sommer"), west = new FButton("Westwind"), sturm = new FButton("Sturm");
        ruhig.addActionListener(e -> seegang(com.dan.surf.Surf.sylt()));
        west.addActionListener(e -> seegang(com.dan.surf.Surf.beaufort(6)));
        sturm.addActionListener(e -> seegang(com.dan.surf.Surf.beaufort(10)));
        sp.add(ruhig); sp.add(west); sp.add(sturm);
        put(sp);
        put(small("<html><body style='width:215px'>Hülle von com.dan.surf (Phase 1): Höhe nach Shoaling, Brechen nach McCowan und der "
                + "Brechertyp nach Battjes, an der Kamera ausgewertet. Wellenfeld ab Phase 5, Brecher im Bild ab Phase 6, Auflauf ab Phase 7.</body></html>"));

        // ============================================================== SAND (Hülle)
        block("SAND", true);
        put(duneLbl);
        put(windLbl);
        put(windSl);
        put(windDirLbl);
        put(windDirSl);
        windSl.addChangeListener(e -> { if (!fromScene) r.wiring.windSpeed = windSl.getValue(); labels(); });
        windDirSl.addChangeListener(e -> { if (!fromScene) r.wiring.windFromDeg = windDirSl.getValue(); labels(); });
        put(small("<html><body style='width:215px'>Hülle von com.dan.dune (Phase 1): Schwelle nach Bagnold mit Feuchte (Belly) und "
                + "Bewuchs (Raupach), Fluss an der Kamera. Fahnen und Rippel ab Phase 8, Dünenwanderung ab Phase 9, Wind aus com.dan.wind ab 9a.</body></html>"));

        // ============================================================== spätere Blöcke
        block("KLIFF", false);
        put(small("<html><body style='width:215px'>Kommt mit Phase 4 und 4a (Lockergestein aus com.dan.cliff: Flugsand, Geschiebelehm, "
                + "Kaolinsand, Runsen, Grasnarbe) und Phase 11 und 12 (Abbruch aus com.dan.fracture bei Sturmflut).</body></html>"));
        block("TÜRME", false);
        put(small("<html><body style='width:215px'>Kommt mit Phase 10: Quermarkenfeuer und Kampener Leuchtturm, Leuchtfeuer aus com.dan.beacon "
                + "(Iso, Sektoren; das Quermarkenfeuer heute dunkel oder wie früher, F19 A), Treppen, Bohlenwege, Strandkörbe, Buhnen.</body></html>"));
        block("KLANG", false);
        put(small("<html><body style='width:215px'>Kommt mit Phase 14: Brandung je Brechertyp und Entfernung, Sand am Ohr, Wind an der Kante, Abbruch, Vögel.</body></html>"));
        block("REGIEBÜCHER", false);
        put(small("<html><body style='width:215px'>Kommt mit Phase 15: „Ein Abend am Kliff“, „Die Sturmflut“, „Ein Jahr am Kliff“, als Kür "
                + "„Mit der Welle“; im Kinomodus aus com.dan.kino.</body></html>"));

        // ============================================================== DATEN
        block("DATEN", false);
        put(dbLbl);
        put(small("<html><body style='width:215px'>Schema SYLT auf PDBORCL (db/User.sql, Präfix SY_). Tabellen, Einrichter, Chronik und "
                + "„Weiter wie zuletzt“ kommen mit Phase 16.</body></html>"));

        // ============================================================== TECHNIK
        block("TECHNIK", false);
        put(perfLbl);
        put(label("Qualität"));
        qualBox = new FComboBox(Quality.values());
        qualBox.setSelectedItem(r.quality());
        qualBox.addActionListener(e -> { r.setQuality((Quality) qualBox.getSelectedItem()); view.requestFocusInWindow(); });
        put(qualBox);
        put(qualLbl);
        autoBox.setSelected(r.autoScale);
        autoBox.addActionListener(e -> { r.autoScale = autoBox.isSelected(); if (!r.autoScale) r.renderScale = scaleSl.getValue() / 100.0; view.requestFocusInWindow(); });
        put(autoBox);
        put(scaleLbl);
        put(scaleSl);
        scaleSl.addChangeListener(e -> {
            if (fromScene) return;
            r.autoScale = false;
            autoBox.setSelected(false);
            r.renderScale = scaleSl.getValue() / 100.0;
        });
        put(label("Belichtung (Blenden von Hand)"));
        put(expSl);
        expSl.addChangeListener(e -> { if (!fromScene) r.exposureBias = expSl.getValue() / 10.0; });
        put(label("HUD"));
        hudBox = new FComboBox(new Object[]{"kurz", "voll", "aus"});
        hudBox.addActionListener(e -> {
            int i = hudBox.getSelectedIndex();
            r.showHud = i != 2;
            r.hudFull = i == 1;
            view.requestFocusInWindow();
        });
        put(hudBox);
        marks.setSelected(r.showMarks);
        marks.addActionListener(e -> { r.showMarks = marks.isSelected(); view.requestFocusInWindow(); });
        put(marks);
        JPanel pic = grid(2);
        FButton photo = new FButton("Foto (F12)"), shaders = new FButton("Shader neu (F5)");
        photo.addActionListener(e -> { r.photoRequest = true; view.requestFocusInWindow(); });
        shaders.addActionListener(e -> { r.requestShaderReload(); view.requestFocusInWindow(); });
        pic.add(photo); pic.add(shaders);
        put(pic);
        put(small("<html><body style='width:215px'>Fotos gehen nach docs/status. Marken (seit Phase 3 aus, M): weiß der Rand des Fernfelds, gold der Kern "
                + "(dort ab Phase 2 das DGM1 zu 1 m), blau das 1-km-Raster in UTM und die Höhenlinien alle 5 m, hellblau die Wasserlinie NHN 0; "
                + "die Orte weiß (Türme), sandfarben (Dünen), hell (Orte), blau (Strand), rot (Kliff).</body></html>"));
        sub("Gelände");
        put(gelLbl);
        sub("Grafikkarte");
        put(computeLbl);
        put(glLbl);
        put(physLbl);
        sub("Packages");
        put(pkgLbl);
        sub("Alle Zeilen des HUD");
        put(hudLbl);

        reihenfolge();
        for (Object[] b : bloecke) ((FCollapsiblePanel) b[0]).setBodyComponent((JComponent) b[1]);
        for (FCollapsiblePanel p : zu) p.setExpanded(false, false);
        add(Box.createVerticalGlue());
        view.setKeyAction(java.awt.event.KeyEvent.VK_F1, () -> javax.swing.SwingUtilities.invokeLater(this::tasten));
        view.setKeyAction(java.awt.event.KeyEvent.VK_M, () -> javax.swing.SwingUtilities.invokeLater(() -> { r.showMarks = !r.showMarks; marks.setSelected(r.showMarks); }));
        view.setKeyAction(java.awt.event.KeyEvent.VK_SPACE, () -> javax.swing.SwingUtilities.invokeLater(() ->
                lapseBox.setSelectedIndex(clock.lapse == 0 ? 1 : 0)));
        syncFromScene();
        new Timer(500, e -> {
            // Befund aus Glacier: solange eine Auswahlliste offen ist, nichts auffrischen
            if (popupOpen(this) || menuOpen()) return;
            syncFromScene();
            if (ticks++ % 2 == 0) technik();
            nachmessen();
        }).start();
    }

    private void wetter(double oktas, com.dan.cloud.CloudType art, double hoch, double sicht) {
        r.wiring.oktas = oktas;
        r.wiring.cloudType = art;
        r.wiring.highCover = hoch;
        r.wiring.visibilityKm = sicht;
        syncFromScene();
        view.requestFocusInWindow();
    }

    private void seegang(com.dan.surf.Surf p) {
        r.wiring.surf.hs = p.hs;
        r.wiring.surf.tp = p.tp;
        r.wiring.surf.seaFromDeg = p.seaFromDeg;
        syncFromScene();
        view.requestFocusInWindow();
    }

    private void reihenfolge() {
        java.util.Map<String, FCollapsiblePanel> m = new java.util.LinkedHashMap<>();
        for (Object[] b : bloecke) m.putIfAbsent(((FCollapsiblePanel) b[0]).getTitle(), (FCollapsiblePanel) b[0]);
        removeAll();
        boolean erst = true;
        for (String t : REIHENFOLGE) {
            FCollapsiblePanel p = m.remove(t);
            if (p == null) continue;
            if (!erst) add(Box.createVerticalStrut(6));
            add(p);
            erst = false;
        }
        for (FCollapsiblePanel p : m.values()) { add(Box.createVerticalStrut(6)); add(p); }
    }

    /** Für Prüfungen: die Titel der Blöcke, wie sie im Bedienfeld stehen. */
    public java.util.List<String> bloecke() {
        java.util.List<String> l = new java.util.ArrayList<>();
        for (Component c : getComponents()) if (c instanceof FCollapsiblePanel p) l.add(p.getTitle());
        return l;
    }

    private final java.util.Map<FCollapsiblePanel, Integer> hoehe = new java.util.HashMap<>();

    /**
     * Wie in Geiranger: FCollapsiblePanel merkt sich die Höhe des Inhalts beim Aufklappen. Wachsen danach Zeilen,
     * schneidet der Block unten ab. Darum alle halbe Sekunde nachmessen und offene Blöcke neu setzen.
     */
    private void nachmessen() {
        boolean changed = false;
        for (Object[] b : bloecke) {
            FCollapsiblePanel p = (FCollapsiblePanel) b[0];
            if (!p.isExpanded()) { hoehe.remove(p); continue; }
            JComponent body = (JComponent) b[1];
            body.invalidate();
            int h = body.getPreferredSize().height;
            Integer old = hoehe.put(p, h);
            if (old == null || old != h) { p.setExpanded(true, false); changed = true; }
        }
        if (changed) { revalidate(); repaint(); }
    }

    /** Ist irgendwo im Bedienfeld eine Auswahlliste aufgeklappt? */
    private static boolean popupOpen(java.awt.Container c) {
        for (Component k : c.getComponents()) {
            if (k instanceof javax.swing.JComboBox<?> b && b.isPopupVisible()) return true;
            if (k instanceof java.awt.Container cc && popupOpen(cc)) return true;
        }
        return false;
    }

    /** Ist irgendein Aufklappmenü offen (auch die Liste einer Auswahl)? Dann darf sich nichts neu auslegen. */
    public static boolean menuOpen() { return javax.swing.MenuSelectionManager.defaultManager().getSelectedPath().length > 0; }

    /** Text nur setzen, wenn er sich ändert (sonst legt Swing den Block bei jedem Takt neu aus). */
    private static void txt(JLabel l, String t) { if (!t.equals(l.getText())) l.setText(t); }

    private void ansicht(Ansichten.Ansicht a) {
        if (r.placeView(a)) {
            free.setSelected(r.camera.mode == Camera.Mode.FREI);
            orbit.setSelected(false);
        }
        view.requestFocusInWindow();
    }

    private void tasten() {
        StringBuilder an = new StringBuilder();
        for (Ansichten.Ansicht a : Ansichten.ALLE) an.append(an.length() > 0 ? ", " : "").append(a.key()).append(' ').append(a.name());
        com.dan.foptionpane.FOptionPane.showMessageDialog(this, "<html><body style='width:420px'>"
                + "<b>Szene</b><br>Links ziehen: drehen · rechts ziehen: verschieben · Rad, Bild↑/↓, +/−: zoomen<br>"
                + "Pfeile: drehen · W A S D: gleiten · Q/E: ab und auf (frei) · Umschalt: schneller<br>"
                + "F: frei fliegen · R: von selbst kreisen · Pos1: Grundansicht · H: HUD kurz, voll, aus<br>"
                + "Leertaste: Uhr anhalten und weiter · F12: Foto nach docs/status · F5: Shader neu laden · M: Marken an und aus<br>"
                + "Ansichten: " + an + "<br><br>"
                + "<b>Fenster</b><br>F1: diese Übersicht · F11: nur die Szene (Bedienfeld und Statuszeile weg und wieder da)</body></html>",
                "Sylt · Tasten", com.dan.foptionpane.FOptionPane.INFORMATION_MESSAGE);
    }

    /** Regler nach der Szene stellen (alle 0,5 s). */
    private void syncFromScene() {
        fromScene = true;
        int m = (int) Math.round(clock.hour() * 60) % (24 * 60);
        if (!time.getValueIsAdjusting()) time.setValue(m);
        if (!scaleSl.getValueIsAdjusting()) scaleSl.setValue((int) Math.round(r.renderScale * 100));
        if (!expSl.getValueIsAdjusting()) expSl.setValue((int) Math.round(r.exposureBias * 10));
        if (!pegelSl.getValueIsAdjusting()) pegelSl.setValue((int) Math.round(r.wiring.pegel * 100));
        if (!hsSl.getValueIsAdjusting()) hsSl.setValue((int) Math.round(r.wiring.surf.hs * 10));
        if (!tpSl.getValueIsAdjusting()) tpSl.setValue((int) Math.round(r.wiring.surf.tp));
        if (!dirSl.getValueIsAdjusting()) dirSl.setValue((int) Math.round(r.wiring.surf.seaFromDeg));
        if (!windSl.getValueIsAdjusting()) windSl.setValue((int) Math.round(r.wiring.windSpeed));
        if (!windDirSl.getValueIsAdjusting()) windDirSl.setValue((int) Math.round(r.wiring.windFromDeg));
        if (!oktasSl.getValueIsAdjusting()) oktasSl.setValue((int) Math.round(r.wiring.oktas));
        if (!highSl.getValueIsAdjusting()) highSl.setValue((int) Math.round(r.wiring.highCover * 100));
        if (!sichtSl.getValueIsAdjusting()) sichtSl.setValue((int) Math.round(r.wiring.visibilityKm));
        if (artBox != null && artBox.getSelectedItem() != r.wiring.cloudType) artBox.setSelectedItem(r.wiring.cloudType);
        fromScene = false;
        labels();
    }

    private void labels() {
        txt(dateLbl, clock.local().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d. MMMM u", Locale.GERMANY)));
        txt(timeLbl, "Uhrzeit " + clock.timeLabel());
        double[] s = clock.sun();
        double day = clock.local().toLocalDate().atStartOfDay(SceneClock.ZONE).toInstant().toEpochMilli();
        double up = Sun.crossing(day, clock.lat, clock.lon, true), down = Sun.crossing(day, clock.lat, clock.lon, false);
        txt(sunLbl, String.format(Locale.GERMAN, "<html><body style='width:215px'>Sonne %.1f° hoch, aus %03.0f° (%s) · Aufgang %s, Untergang %s</body></html>",
                s[0], s[1], compass(s[1]), hm(up), hm(down)));
        txt(camLbl, "<html><body style='width:215px'>" + esc(r.cameraLine()) + "</body></html>");
        txt(scaleLbl, String.format(Locale.GERMAN, "Auflösung %.0f %% (%s)", r.renderScale * 100, r.autoScale ? "geregelt, " + r.scaleSteps() + "× geändert" : "fest"));
        txt(qualLbl, "<html><body style='width:215px'>" + r.quality().summary() + "</body></html>");
        txt(pegelLbl, String.format(Locale.GERMAN, "Pegel %+.2f m NHN (von Hand)", r.wiring.pegel));
        com.dan.surf.Surf sf = r.wiring.surf;
        txt(hsLbl, String.format(Locale.GERMAN, "Wellenhöhe draußen H_s %.1f m", sf.hs));
        txt(tpLbl, String.format(Locale.GERMAN, "Periode T_p %.0f s (L₀ = %.0f m)", sf.tp, com.dan.surf.Surf.deepLength(sf.tp)));
        txt(dirLbl, String.format(Locale.GERMAN, "aus %03.0f° (%s)", sf.seaFromDeg, compass(sf.seaFromDeg)));
        txt(surfLbl, "<html><body style='width:215px'>" + esc(r.wiring.surfZeile(r.camera.eye[0], r.camera.eye[2])) + "</body></html>");
        txt(windLbl, String.format(Locale.GERMAN, "Wind in 10 m: %.0f m/s", r.wiring.windSpeed));
        txt(windDirLbl, String.format(Locale.GERMAN, "aus %03.0f° (%s)", r.wiring.windFromDeg, compass(r.wiring.windFromDeg)));
        txt(duneLbl, "<html><body style='width:215px'>" + esc(r.wiring.duneZeile(r.camera.eye[0], r.camera.eye[2])) + "</body></html>");
        txt(oktasLbl, String.format(Locale.GERMAN, "Bedeckung %.0f/8", r.wiring.oktas));
        txt(highLbl, String.format(Locale.GERMAN, "Hoher Schleier %.0f %%", r.wiring.highCover * 100));
        txt(sichtLbl, String.format(Locale.GERMAN, "Sicht %.0f km", r.wiring.visibilityKm));
        txt(wetterLbl, "<html><body style='width:215px'>" + esc(String.format(Locale.GERMAN, "%s · Sonne am Auge %.0f %%%s", r.wiring.wetterZeile(),
                r.cloudShade() * 100, r.skyRunning() ? "" : " · Himmel nicht gestartet")) + "</body></html>");
    }

    private void technik() {
        txt(perfLbl, String.format(Locale.GERMAN, "<html><body style='width:215px'>%.0f Bilder/s · Grafikkarte %.2f ms · Prozessor %.2f ms<br>%s</body></html>",
                r.fps(), r.gpuMs(), r.cpuMs(), r.passTimes()));
        ComputeProbe.Result c = r.compute();
        txt(computeLbl, "<html><body style='width:215px'>" + (c == null ? "Compute-Test läuft …" : c.line()) + "</body></html>");
        txt(glLbl, "<html><body style='width:215px'>" + r.glInfo() + (r.reversedDepth() ? " · umgekehrte Tiefe" : "") + "</body></html>");
        txt(physLbl, "<html><body style='width:215px'>" + r.physik.describe() + "</body></html>");
        txt(gelLbl, "<html><body style='width:215px'>" + esc(r.terrain.describe()) + (r.terrain.measured() ? ""
                : "<br>Bühne von Phase 1: das Gelände kommt mit Phase 2 aus daten/gelaende.") + "</body></html>");
        txt(pkgLbl, "<html><body style='width:215px'>" + esc(Bibliothek.kurz().replace("!", "")) + ": " + String.join(", ", Bibliothek.pakete())
                + ".<br>Neu für Sylt: " + esc(Bibliothek.neuStand()) + " (Hüllen seit Phase 1). Die Prüfung <i>check-packages</i> läuft im Projekt Renderer nach jedem Übersetzen.</body></html>");
        txt(dbLbl, "<html><body style='width:215px'>" + esc(Verbindung.stand()) + "</body></html>");
        StringBuilder h = new StringBuilder("<html><body style='width:215px'>");
        for (String l : r.hudLines()) h.append(esc(l.startsWith("!") ? l.substring(1) : l)).append("<br>");
        txt(hudLbl, h.append("</body></html>").toString());
    }

    private static String esc(String t) { return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    static String hm(double epochMs) {
        if (Double.isNaN(epochMs)) return "—";
        return String.format("%tR", Instant.ofEpochMilli((long) epochMs).atZone(SceneClock.ZONE));
    }

    static String compass(double deg) {
        String[] n = {"Nord", "Nordnordost", "Nordost", "Ostnordost", "Ost", "Ostsüdost", "Südost", "Südsüdost",
                "Süd", "Südsüdwest", "Südwest", "Westsüdwest", "West", "Westnordwest", "Nordwest", "Nordnordwest"};
        return n[(int) Math.round(((deg % 360) + 360) % 360 / 22.5) % 16];
    }

    // ------------------------------------------------------------ Aufbau

    /** Ein Klappblock (FCollapsiblePanel); offen bleibt, was zuletzt offen war. */
    private void block(String title, boolean offen) {
        FCollapsiblePanel p = new FCollapsiblePanel() {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        p.setTitle(title);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel inhalt = new JPanel();
        inhalt.setOpaque(false);
        inhalt.setLayout(new BoxLayout(inhalt, BoxLayout.Y_AXIS));
        inhalt.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 8));
        bloecke.add(new Object[]{p, inhalt});
        String key = "offen." + title;
        boolean auf = PREFS == null ? offen : PREFS.getBoolean(key, offen);
        if (!auf) zu.add(p);
        p.addFCollapsiblePanelListener((panel, expanded) -> {
            if (PREFS != null) try { PREFS.putBoolean(key, expanded); } catch (RuntimeException ex) { /* nicht gemerkt */ }
        });
        if (getComponentCount() > 0) add(Box.createVerticalStrut(6));
        add(p);
        target = inhalt;
    }

    /** Eine Zwischenüberschrift im Block. */
    private void sub(String text) {
        JLabel h = new JLabel(text);
        h.setForeground(Theme.accent());
        h.setFont(new Font("SansSerif", Font.BOLD, 11));
        h.setBorder(BorderFactory.createEmptyBorder(10, 0, 3, 0));
        put(h);
    }

    private void put(Component c) { target.add(row(c)); }

    private static JPanel grid(int n) {
        JPanel p = new JPanel(new java.awt.GridLayout(1, n, 6, 0));
        p.setOpaque(false);
        return p;
    }

    /** Eine Zeile, die mit ihrem Inhalt wächst. */
    private static JComponent row(Component c) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        p.add(c, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    static JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Theme.ink());
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    static JLabel small(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Theme.muted());
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    // ------------------------------------------------------------ Scrollable: nur senkrecht
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle rr, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle rr, int o, int d) { return 120; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
