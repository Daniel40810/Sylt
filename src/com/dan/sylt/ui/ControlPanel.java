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
 * schon alle da: KAMERA, ZEIT, WETTER, SEE, BRANDUNG, SAND, KLIFF, TÜRME, KLANG, REGIEBÜCHER, CHRONIK, DATEN, TECHNIK. In Phase 1
 * arbeiten KAMERA (neun Ansichten, frei fliegen, kreisen), ZEIT (Datum, Uhrzeit, Zeitraffer, Sonne), SEE (Pegel von
 * Hand, die See als Platzhalter an/aus), BRANDUNG und SAND (die Hüllen von {@code surf} und {@code dune} mit ihren
 * offenen Werten), DATEN (seit Phase 16 mit Einrichter, Übernahme, Presets, gemerkten Ansichten, dazu CHRONIK) und TECHNIK (Qualität, Auflösung, Belichtung, HUD, Marken, Foto, Shader,
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
            bodenBox = new FCheckBox("Bodendecke zeigen (com.dan.ground, Biom Sylt)"),
            tideBox = new FCheckBox("Tide Westerland (Pegel aus der Gezeit)"), netzBox = new FCheckBox("Brecher nahe am Auge als Netz"),
            buhnenBox = new FCheckBox("Buhnen zeigen"), stromBox = new FCheckBox("Strömung zeigen (Rippströme)"),
            dlrgBox = new FCheckBox("DLRG-Blick: Rinnen und Rückstrom deutlich"),
            sandBox = new FCheckBox("Flugsand (com.dan.dune, Wind aus com.dan.wind)"), koernerBox = new FCheckBox("Körner zeigen (Fahnen, Kammfahnen)");
    private final FSlider regenSl = new FSlider(0, 60, 0);
    private final FCheckBox bautenBox = new FCheckBox("Türme und Bauten (LOD2, Treppe, Wege, Strand)"),
            feuerBox = new FCheckBox("Leuchtfeuer (com.dan.beacon)"), strahlBox = new FCheckBox("Strahl im Dunst"),
            frueherBox = new FCheckBox("Quermarkenfeuer wie früher (F WRG, bis 1975)"),
            koerbeBox = new FCheckBox("Strandkörbe (Saison, Wind, Sturmwarnung)"), herdeBox = new FCheckBox("Galloways am Kampener Leuchtturm");
    private final JLabel tuermeLbl = small(""), kliffLbl = small("");
    private final FCheckBox brockenBox = new FCheckBox("Brocken zeigen");
    private final JLabel regenLbl = label("");
    /** Seit Phase 13: Wetterregie, Regen, Seenebel, Gewitter, Licht und Leben. */
    private FComboBox regieBox, gewitterBox;
    private final FSlider regenMmSl = new FSlider(0, 300, 0), nebelSl = new FSlider(0, 100, 0);
    private final JLabel regenMmLbl = label(""), nebelLbl = label(""), unwetterLbl = small(""), lebenLbl = small(""), lichtLbl = small("");
    private final JLabel klangLbl = small(""), lautLbl = label("");
    /** Seit Phase 15: Stand des Regiebuchs, Serie und Vorspülung. */
    private final JLabel regieLbl = small(""), serieLbl = small("");
    private final FCheckBox regenBox = new FCheckBox("Regen zeigen (com.dan.rain)"), nebelBox = new FCheckBox("Seenebel zeigen (com.dan.fog)"),
            gewitterZeigen = new FCheckBox("Gewitter zeigen (com.dan.lightning)"), leuchtenBox = new FCheckBox("Meeresleuchten in Sommernächten"),
            leuchtenFest = new FCheckBox("Blüte erzwingen (jede Nacht, volle Stärke)"), gruenBox = new FCheckBox("Grünes Leuchten beim Sonnenuntergang"),
            lebenBox = new FCheckBox("Leben am Strand (com.dan.flock, com.dan.seal)"), moewenBox = new FCheckBox("Möwen im Hangaufwind am Kliff"),
            saumBox = new FCheckBox("Sanderlinge und Austernfischer am Spülsaum"), spurenBox = new FCheckBox("Spuren im nassen Sand"),
            seehundBox = new FCheckBox("Seehund (selten)");
    private final JLabel tideLbl = small(""), brandungLbl = small("");
    private FComboBox lapseBox, qualBox, hudBox, artBox;
    private boolean fromScene;
    private int ticks;
    private JPanel target = this;
    private static final Preferences PREFS = prefs();
    private final java.util.List<Object[]> bloecke = new java.util.ArrayList<>();
    private final java.util.List<FCollapsiblePanel> zu = new java.util.ArrayList<>();
    private static final double[] LAPSE = {0, 1, 10, 60, 180, 720, 3600};

    /** Die Reihenfolge der Blöcke (Werkbuch, Abschnitt 9). */
    public static final String[] REIHENFOLGE = {"KAMERA", "ZEIT", "WETTER", "SEE", "BRANDUNG", "SAND", "KLIFF", "TÜRME", "KLANG", "REGIEBÜCHER", "CHRONIK", "DATEN", "TECHNIK"};

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

        // ============================================================== WETTER (Phase 3, seit Phase 13 mit Regie)
        block("WETTER", true);
        put(wetterLbl);
        put(label("Wetterregie"));
        java.util.List<Object> regieItems = new java.util.ArrayList<>();
        regieItems.add("von Hand");
        for (com.dan.sylt.wiring.Wetterregie.Lage l : com.dan.sylt.wiring.Wetterregie.Lage.values()) regieItems.add(l);
        regieItems.add("freie Regie (Deutsche Bucht)");
        regieItems.add("Regiebuch „Ein Sturmtag“");
        regieBox = new FComboBox(regieItems.toArray());
        regieBox.setSelectedItem(r.wiring.regie.lage());
        regieBox.setToolTipText("Eine Lage steht fest; die freie Regie wählt die Lagen selbst (mit Vorlauf: Barometer, Dünung, Cirren); "
                + "„Ein Sturmtag“ ist das Regiebuch von com.dan.weather. Die Regler stellen auf „von Hand“.");
        regieBox.addActionListener(e -> {
            if (fromScene) return;
            Object o = regieBox.getSelectedItem();
            var rg = r.wiring.regie;
            if (o instanceof com.dan.sylt.wiring.Wetterregie.Lage l) rg.lage(l);
            else if (regieBox.getSelectedIndex() == 0) rg.hand();
            else if (String.valueOf(o).startsWith("freie")) rg.frei();
            else rg.sturmtag();
            syncFromScene();
            view.requestFocusInWindow();
        });
        put(regieBox);
        put(oktasLbl);
        put(oktasSl);
        oktasSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.oktas = oktasSl.getValue(); } labels(); });
        put(label("Art der unteren Wolken"));
        artBox = new FComboBox(com.dan.cloud.CloudType.values());
        artBox.setSelectedItem(r.wiring.cloudType);
        artBox.addActionListener(e -> { if (!fromScene) { hand(); r.wiring.cloudType = (com.dan.cloud.CloudType) artBox.getSelectedItem(); } view.requestFocusInWindow(); });
        put(artBox);
        put(highLbl);
        put(highSl);
        highSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.highCover = highSl.getValue() / 100.0; } labels(); });
        put(sichtLbl);
        put(sichtSl);
        sichtSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.visibilityKm = sichtSl.getValue(); } labels(); });
        // seit Phase 13: Regen, Seenebel, Gewitter
        put(regenMmLbl);
        put(regenMmSl);
        regenMmSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.regen = regenMmSl.getValue() / 10.0; } labels(); });
        put(nebelLbl);
        put(nebelSl);
        nebelSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.nebel = nebelSl.getValue() / 100.0; } labels(); });
        put(label("Gewitter"));
        gewitterBox = new FComboBox(new Object[]{"kein Gewitter", "Wetterleuchten", "mäßig", "kräftig", "schwer"});
        gewitterBox.addActionListener(e -> { if (!fromScene) { hand(); r.wiring.gewitter = gewitterBox.getSelectedIndex(); } view.requestFocusInWindow(); });
        put(gewitterBox);
        JPanel bg = grid(2);
        FButton blitzSee = new FButton("Blitz über der See"), blitzTurm = new FButton("Blitz in den Leuchtturm");
        blitzSee.setToolTipText("Ein Erdblitz 2 bis 5 km in Blickrichtung (ohne Gewitter: Gewitter mäßig von Hand)");
        blitzSee.addActionListener(e -> { blitz(false); view.requestFocusInWindow(); });
        blitzTurm.addActionListener(e -> { blitz(true); view.requestFocusInWindow(); });
        bg.add(blitzSee); bg.add(blitzTurm);
        put(bg);
        regenBox.setSelected(r.unwetter.showRegen);
        regenBox.addActionListener(e -> { r.unwetter.showRegen = regenBox.isSelected(); view.requestFocusInWindow(); });
        nebelBox.setSelected(r.unwetter.showNebel);
        nebelBox.addActionListener(e -> { r.unwetter.showNebel = nebelBox.isSelected(); view.requestFocusInWindow(); });
        gewitterZeigen.setSelected(r.unwetter.showGewitter);
        gewitterZeigen.addActionListener(e -> { r.unwetter.showGewitter = gewitterZeigen.isSelected(); view.requestFocusInWindow(); });
        put(regenBox);
        put(nebelBox);
        put(gewitterZeigen);
        put(unwetterLbl);
        JPanel lagen = grid(2), lagen2 = grid(2);
        FButton abend = new FButton("Sommerabend"), heiter = new FButton("Heiter"), bedeckt = new FButton("Bedeckt"), grau = new FButton("Grau");
        abend.addActionListener(e -> { r.wiring.regie.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND); syncFromScene(); view.requestFocusInWindow(); });
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
        sub("Licht");
        leuchtenBox.setSelected(r.meeresleuchten);
        leuchtenBox.addActionListener(e -> { r.meeresleuchten = leuchtenBox.isSelected(); view.requestFocusInWindow(); });
        put(leuchtenBox);
        leuchtenFest.addActionListener(e -> { r.bluete = leuchtenFest.isSelected() ? 1 : Double.NaN; view.requestFocusInWindow(); });
        put(leuchtenFest);
        gruenBox.setSelected(r.gruenesLeuchten);
        gruenBox.addActionListener(e -> { r.gruenesLeuchten = gruenBox.isSelected(); view.requestFocusInWindow(); });
        put(gruenBox);
        FButton fern = new FButton("Fernrohr auf die Sonne");
        fern.setToolTipText("Von der Kliffkante, Öffnung 1,2°: der grüne Rand beim Untergang (Pos1 oder eine Ansicht beendet es)");
        fern.addActionListener(e -> { r.fernrohrZurSonne(); free.setSelected(true); orbit.setSelected(false); view.requestFocusInWindow(); });
        put(fern);
        put(lichtLbl);
        put(small("<html><body style='width:215px'>Seit Phase 3: Himmel aus com.dan.sky (Sonne, Mond, Sterne, Streuung über 54,9° N), "
                + "Wolken aus com.dan.cloud mit Schatten, Bodendecke um das Auge aus com.dan.ground. Seit Phase 13 die Wetterregie aus "
                + "com.dan.weather mit den Lagen des Werkbuchs, Regen mit Schauerwänden über der See (com.dan.rain), Seenebel und "
                + "Seerauch (com.dan.fog), Gewitter über der See mit den Türmen als Fangpunkte (com.dan.lightning); nach Regen sind "
                + "Sand und Kliff dunkel. Meeresleuchten (Noctiluca, Juni bis September) leuchtet in Kamm, Schaum und Schwall; das grüne "
                + "Leuchten zeigt sich an Abenden mit Luftspiegelung, im Fernrohr.</body></html>"));

        // ============================================================== SEE
        block("SEE", true);
        tideBox.setSelected(r.wiring.tideAn);
        tideBox.addActionListener(e -> { r.wiring.tideAn = tideBox.isSelected(); r.wiring.stepTide(); syncFromScene(); view.requestFocusInWindow(); });
        put(tideBox);
        put(tideLbl);
        put(pegelLbl);
        put(pegelSl);
        // der Regler übernimmt den Pegel von Hand (die Tide schaltet sich ab)
        pegelSl.addChangeListener(e -> {
            if (fromScene) return;
            r.wiring.tideAn = false;
            tideBox.setSelected(false);
            r.wiring.pegel = pegelSl.getValue() / 100.0;
            labels();
        });
        meerBox.setSelected(r.showMeer);
        meerBox.addActionListener(e -> { r.showMeer = meerBox.isSelected(); view.requestFocusInWindow(); });
        put(meerBox);
        put(small("<html><body style='width:215px'>Seit Phase 7a kommt der Pegel aus Tide.westerland() (F25 A, zehn Partialtiden, "
                + "Hub rund 1,8 m) mit Windstau aus dem Wind in SAND; der Regler stellt ihn von Hand (die Tide schaltet sich ab). "
                + "Die offene See (com.dan.ocean) nimmt die Dünung aus BRANDUNG; der Grund ist das Gelände.</body></html>"));

        // ============================================================== BRANDUNG (Hülle)
        block("BRANDUNG", true);
        put(surfLbl);
        put(hsLbl);
        put(hsSl);
        put(tpLbl);
        put(tpSl);
        put(dirLbl);
        put(dirSl);
        hsSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.surf.hs = hsSl.getValue() / 10.0; } labels(); });
        tpSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.surf.tp = tpSl.getValue(); } labels(); });
        dirSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.surf.seaFromDeg = dirSl.getValue(); } labels(); });
        JPanel sp = grid(3);
        FButton ruhig = new FButton("Sommer"), west = new FButton("Westwind"), sturm = new FButton("Sturm");
        ruhig.addActionListener(e -> seegang(com.dan.surf.Surf.sylt()));
        west.addActionListener(e -> seegang(com.dan.surf.Surf.beaufort(6)));
        sturm.addActionListener(e -> seegang(com.dan.surf.Surf.beaufort(10)));
        sp.add(ruhig); sp.add(west); sp.add(sturm);
        put(sp);
        sub("Brandung in der Szene");
        netzBox.setSelected(r.showNetz);
        netzBox.addActionListener(e -> { r.showNetz = netzBox.isSelected(); view.requestFocusInWindow(); });
        put(netzBox);
        buhnenBox.setSelected(r.showBuhnen);
        buhnenBox.addActionListener(e -> { r.showBuhnen = buhnenBox.isSelected(); view.requestFocusInWindow(); });
        put(buhnenBox);
        stromBox.setSelected(r.showStroemung);
        stromBox.addActionListener(e -> { r.showStroemung = stromBox.isSelected(); view.requestFocusInWindow(); });
        put(stromBox);
        dlrgBox.setSelected(r.dlrgBlick);
        dlrgBox.addActionListener(e -> { r.dlrgBlick = dlrgBox.isSelected(); if (r.dlrgBlick) { r.showStroemung = true; stromBox.setSelected(true); } view.requestFocusInWindow(); });
        put(dlrgBox);
        FButton stormB = new FButton("Sturm verlagert Riffe und Rinnen");
        stormB.addActionListener(e -> { r.brandung.riffe.storm(); r.brandung.feld.bedVersion++; view.requestFocusInWindow(); });
        put(stormB);
        put(brandungLbl);
        sub("Leben am Strand (seit Phase 13)");
        lebenBox.setSelected(r.showLeben);
        lebenBox.addActionListener(e -> { r.showLeben = lebenBox.isSelected(); view.requestFocusInWindow(); });
        put(lebenBox);
        moewenBox.setSelected(r.leben.anMoewen);
        moewenBox.addActionListener(e -> { r.leben.anMoewen = moewenBox.isSelected(); view.requestFocusInWindow(); });
        saumBox.setSelected(r.leben.anStrand);
        saumBox.addActionListener(e -> { r.leben.anStrand = saumBox.isSelected(); view.requestFocusInWindow(); });
        spurenBox.setSelected(r.leben.anSpuren);
        spurenBox.addActionListener(e -> { r.leben.anSpuren = spurenBox.isSelected(); view.requestFocusInWindow(); });
        seehundBox.setSelected(r.leben.anSeehund);
        seehundBox.addActionListener(e -> { r.leben.anSeehund = seehundBox.isSelected(); view.requestFocusInWindow(); });
        put(moewenBox);
        put(saumBox);
        put(spurenBox);
        put(seehundBox);
        put(lebenLbl);
        put(small("<html><body style='width:215px'>Seit Phase 7a: das Wellenfeld aus com.dan.surf vor dem ganzen Kern (Riffe und Rinnen, Set-up, "
                + "Strömung), neu gerechnet im eigenen Faden, wenn sich See, Pegel oder Grund ändern; com.dan.ocean zeichnet daraus Walzen, "
                + "Schaumteppich und Rippströme. Nahe am Auge die Brecher als Netz mit Auflauf und nassem Sand (Phase 6 und 7). Die Pfähle "
                + "der Buhnen stehen, wo das Luftbild sie zeigt (Kliffende, Kampen), bei Wenningstedt mit geschätzter Lage (Phase 8).</body></html>"));

        // ============================================================== SAND (Hülle)
        block("SAND", true);
        put(duneLbl);
        put(windLbl);
        put(windSl);
        put(windDirLbl);
        put(windDirSl);
        windSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.windSpeed = windSl.getValue(); } labels(); });
        windDirSl.addChangeListener(e -> { if (!fromScene) { hand(); r.wiring.windFromDeg = windDirSl.getValue(); } labels(); });
        sub("Flugsand in der Szene");
        sandBox.setSelected(r.sand.an);
        sandBox.addActionListener(e -> { r.sand.an = sandBox.isSelected(); view.requestFocusInWindow(); });
        put(sandBox);
        koernerBox.setSelected(r.showKoerner);
        koernerBox.addActionListener(e -> { r.showKoerner = koernerBox.isSelected(); view.requestFocusInWindow(); });
        put(koernerBox);
        put(regenLbl);
        put(regenSl);
        regenSl.addChangeListener(e -> { if (!fromScene) r.sand.regen = regenSl.getValue() / 1000.0; labels(); });
        FButton sturmB = new FButton("Sandsturm am Strand");
        sturmB.addActionListener(e -> {
            r.sandsturm();
            fromScene = true;
            windSl.setValue((int) Math.round(r.wiring.windSpeed));
            windDirSl.setValue((int) Math.round(r.wiring.windFromDeg));
            regenSl.setValue(0);
            fromScene = false;
            sandBox.setSelected(true);
            koernerBox.setSelected(true);
            labels();
            view.requestFocusInWindow();
        });
        put(sturmB);
        put(small("<html><body style='width:215px'>Seit Phase 9a: Flugsand aus com.dan.dune mit dem Wind aus com.dan.wind (Feld über dem "
                + "Kern, im Lee des Kliffs schwächer, über den Kämmen stärker). Der nasse Strand nach Pegel, Auflauf und Schwall fliegt "
                + "nicht, nach Regen nichts. Sandfahnen über den Strand, Kammfahnen über die Dünen, um das Auge. „Sandsturm am Strand“: "
                + "Bft 9 aus West, trocken, Blick vom Strand.</body></html>"));

        // ============================================================== spätere Blöcke
        // ============================================================== KLIFF (seit Phase 12: Sturmflut und Abbruch)
        block("KLIFF", true);
        put(kliffLbl);
        FButton flutB = new FButton("Sturmflut (Xaver 2013)"), hierB = new FButton("Abbruch hier");
        flutB.setToolTipText("5. Dezember 2013 ab 12 Uhr, Zeitraffer 12 min/s: Orkan aus Nordwest, Windstau über 3 m in der Nacht");
        flutB.addActionListener(e -> {
            r.sturmflutStarten(com.dan.sylt.scene.Sturmflut.STUNDE);
            lapseBox.setSelectedIndex(5);
            syncFromScene();
            view.requestFocusInWindow();
        });
        hierB.setToolTipText("Die Hohlkehle am Ziel der Kamera kritisch machen: fracture bricht das Kliff dort");
        hierB.addActionListener(e -> { r.abbruchHier(); view.requestFocusInWindow(); });
        JPanel kg = grid(2);
        kg.add(flutB);
        kg.add(hierB);
        put(kg);
        brockenBox.setSelected(r.showBrocken);
        brockenBox.addActionListener(e -> { r.showBrocken = brockenBox.isSelected(); view.requestFocusInWindow(); });
        put(brockenBox);
        put(small("<html><body style='width:215px'>Lockergestein aus com.dan.cliff (Flugsand, Geschiebelehm, Kaolinsand, Grasnarbe). "
                + "Seit Phase 12: bei Sturmflut reicht der Auflauf an den Fuß, die Brandung schlägt die Hohlkehle; wird sie tiefer als "
                + "rund 0,6 der kritischen Tiefe des Überhangs, rechnet com.dan.fracture das Stück Kliff darüber (Spannung, Nässe), "
                + "es bricht, die Brocken zerfallen zum Sturzkegel mit Staub, die nächste Flut trägt ihn ab. „Abbruch hier“ löst es am "
                + "Ziel der Kamera aus.</body></html>"));
        // ============================================================== TÜRME (seit Phase 10)
        block("TÜRME", true);
        bautenBox.setSelected(r.showBauten);
        bautenBox.addActionListener(e -> { r.showBauten = bautenBox.isSelected(); view.requestFocusInWindow(); });
        put(bautenBox);
        feuerBox.setSelected(r.feuer.an);
        feuerBox.addActionListener(e -> { r.feuer.an = feuerBox.isSelected(); view.requestFocusInWindow(); });
        put(feuerBox);
        strahlBox.setSelected(r.feuer.strahl);
        strahlBox.addActionListener(e -> { r.feuer.strahl = strahlBox.isSelected(); view.requestFocusInWindow(); });
        put(strahlBox);
        frueherBox.setSelected(r.feuer.quermarkeFrueher);
        frueherBox.addActionListener(e -> { r.feuer.quermarkeFrueher = frueherBox.isSelected(); labels(); view.requestFocusInWindow(); });
        put(frueherBox);
        FCheckBox strahlerBox = new FCheckBox("Dazu nachts angestrahlt (wie Leuchturm2)");
        strahlerBox.setSelected(r.feuer.anstrahlung);
        strahlerBox.addActionListener(e -> { r.feuer.anstrahlung = strahlerBox.isSelected(); view.requestFocusInWindow(); });
        put(strahlerBox);
        koerbeBox.setSelected(r.koerbe.an);
        koerbeBox.addActionListener(e -> { r.koerbe.an = koerbeBox.isSelected(); r.showKoerbe = koerbeBox.isSelected(); view.requestFocusInWindow(); });
        put(koerbeBox);
        herdeBox.setSelected(r.showHerde);
        herdeBox.addActionListener(e -> { r.showHerde = herdeBox.isSelected(); view.requestFocusInWindow(); });
        put(herdeBox);
        put(tuermeLbl);
        sub("Vorlagen (Tag und Stunde wie im Bild)");
        JPanel vg = grid(2);
        for (Ansichten.Vorlage v : Ansichten.VORLAGEN) {
            FButton b = new FButton(v.name());
            b.setToolTipText(Ansichten.describe(v.ansicht(), Renderer.PHASE) + " · " + v.datum() + " " + String.format(Locale.GERMAN, "%.2f Uhr", v.uhr()));
            b.addActionListener(e -> {
                r.placeVorlage(v);
                frueherBox.setSelected(r.feuer.quermarkeFrueher);
                free.setSelected(r.camera.mode == Camera.Mode.FREI);
                orbit.setSelected(false);
                syncFromScene();
                view.requestFocusInWindow();
            });
            vg.add(b);
        }
        put(vg);
        put(small("<html><body style='width:215px'>Seit Phase 10: die Häuser aus LOD2 mit Reet, Backstein, Putz und Klinker, das "
                + "Quermarkenfeuer (Backstein, Kupferdach) und der Kampener Leuchtturm (weiß, schwarzes Band) genauer gebaut, "
                + "Treppe am Kliff, Bohlenweg und Kiesweg zum Quermarkenfeuer, Strandlokal, Rettungstürme. Kampen brennt Iso WR 7s "
                + "mit Dämmerungsschalter; das Quermarkenfeuer ist seit 1975 dunkel, „wie früher“ brennt es F WRG. Strandkörbe von "
                + "Mai bis September, mit dem Rücken zum Wind, bei Sturmwarnung (ab Bft 8) weggeräumt. Die Vorlagen stellen "
                + "Ansicht, Öffnung (Fernrohr für Kampen1–3), Tag und Stunde.</body></html>"));
        // ============================================================== KLANG (seit Phase 14)
        block("KLANG", true);
        var kl = r.klang;
        FCheckBox tonBox = new FCheckBox("Ton an (Lautsprecher)");
        boolean tonPref = PREFS == null || PREFS.getBoolean("ton", true);
        tonBox.setSelected(tonPref);
        if (tonPref) kl.setOn(true);
        tonBox.addActionListener(e -> {
            kl.setOn(tonBox.isSelected());
            if (tonBox.isSelected() && !kl.on) tonBox.setSelected(false);
            if (PREFS != null) PREFS.putBoolean("ton", tonBox.isSelected());
            view.requestFocusInWindow();
        });
        put(tonBox);
        FSlider lautSl = new FSlider(0, 200, (int) Math.round(kl.s.volume * 100));
        lautSl.addChangeListener(e -> { kl.s.volume = lautSl.getValue() / 100.0; txt(lautLbl, "Lautstärke " + lautSl.getValue() + " %"); });
        txt(lautLbl, "Lautstärke " + lautSl.getValue() + " %");
        put(lautLbl);
        put(lautSl);
        FCheckBox ohrBox = new FCheckBox("Das Ohr passt sich an (leise Nacht hörbar, Sturm erträglich)");
        ohrBox.setSelected(kl.s.autoGain);
        ohrBox.addActionListener(e -> { kl.s.autoGain = ohrBox.isSelected(); view.requestFocusInWindow(); });
        put(ohrBox);
        sub("Was klingt");
        FCheckBox[] teile = {new FCheckBox("Brandung (fern und die einzelnen Wellen)"), new FCheckBox("Wind, Kliffkante"), new FCheckBox("Flugsand und Strandhafer"),
                new FCheckBox("Möwen, Sanderlinge, Austernfischer, Seehund, Kühe"), new FCheckBox("Regen und Donner"), new FCheckBox("Abbruch des Kliffs")};
        boolean[] an = {kl.brandungAn, kl.windAn, kl.sandAn, kl.tiereAn, kl.wetterAn, kl.kliffAn};
        for (int i = 0; i < teile.length; i++) {
            final int k = i;
            teile[i].setSelected(an[i]);
            teile[i].addActionListener(e -> {
                boolean b = teile[k].isSelected();
                switch (k) {
                    case 0 -> kl.brandungAn = b;
                    case 1 -> kl.windAn = b;
                    case 2 -> kl.sandAn = b;
                    case 3 -> kl.tiereAn = b;
                    case 4 -> kl.wetterAn = b;
                    default -> kl.kliffAn = b;
                }
                view.requestFocusInWindow();
            });
            put(teile[i]);
        }
        put(klangLbl);
        put(small("<html><body style='width:215px'>Seit Phase 14: erzeugt mit com.dan.soundscape, ohne Audiodateien. Das Ohr ist die "
                + "Kamera. Die ferne Brandung ist eine Linie aus dem Wellenfeld (Leistung und Art der Brecher, je 25 m), nah klingt "
                + "jede Welle: der Schwallbrecher zischt, der Sturzbrecher schlägt, der Schwall läuft hinauf und zischt über den Sand "
                + "zurück. Fern bleibt das Rollen, die Luft nimmt die Höhen. Oben hinter der Kliffkante dämpft die Kante. Wind aus dem "
                + "Windfeld, die Kante heult im Sturm, Strandhafer raschelt, Flugsand prasselt. Hörproben: ant hoerproben.</body></html>"));
        // ============================================================== REGIEBÜCHER (Phase 15)
        block("REGIEBÜCHER", true);
        put(regieLbl);
        java.util.List<com.dan.sylt.scene.Regie.Book> books;
        JLabel buchInfo = small("");
        try { books = com.dan.sylt.scene.Regie.all(); }
        catch (RuntimeException ex) { books = java.util.List.of(); buchInfo.setText("Regiebücher nicht lesbar: " + ex.getMessage()); }
        FComboBox bookBox = new FComboBox(books.toArray());
        put(bookBox);
        Runnable showAbout = () -> {
            if (bookBox.getSelectedItem() instanceof com.dan.sylt.scene.Regie.Book b)
                buchInfo.setText("<html><body style='width:215px'>" + esc(b.about()) + String.format(Locale.GERMAN, " · %d:%02d min ohne Warten, %d Fotos",
                        (int) b.length() / 60, (int) b.length() % 60, b.fotos()) + "</body></html>");
        };
        bookBox.addActionListener(e -> { showAbout.run(); view.requestFocusInWindow(); });
        showAbout.run();
        JPanel bookRow = grid(2);
        FButton bookGo = new FButton("Von vorn"), bookStop = new FButton("Anhalten");
        bookGo.setToolTipText("Das Regiebuch von vorn: Datum, Uhrzeit, Wetter, Ansichten, Fahrten; am Ende ein Blatt mit den Fotos in docs/status");
        FCheckBox imKino = new FCheckBox("Im Kino zeigen");
        imKino.setSelected(PREFS.getBoolean("kino", true));
        imKino.setToolTipText("Ganzer Bildschirm ohne Bedienfeld, Breitwand mit Balken, Vor- und Abspann, Untertitel aus den Stichworten, "
                + "Überblendung bei jedem Schnitt, der Ton blendet mit. Esc beendet das Kino (das Regiebuch läuft weiter), Leertaste hält an, F12 fotografiert.");
        imKino.addActionListener(e -> { PREFS.putBoolean("kino", imKino.isSelected()); view.requestFocusInWindow(); });
        bookGo.addActionListener(e -> {
            if (bookBox.getSelectedItem() instanceof com.dan.sylt.scene.Regie.Book b) { if (imKino.isSelected()) r.kinoStart(b); else r.regieStart(b); }
            view.requestFocusInWindow();
        });
        bookStop.addActionListener(e -> { r.regieStop(); view.requestFocusInWindow(); });
        bookRow.add(bookGo);
        bookRow.add(bookStop);
        put(bookRow);
        put(imKino);
        put(buchInfo);
        JPanel extra = grid(2);
        FButton welleGo = new FButton("Mit der Welle"), spuelGo = new FButton("Vorspülung");
        welleGo.setToolTipText("Jetzt auf der nächsten Welle der Nahbrandung mitfahren (vom Riff bis zum Auflauf; erst eine Ansicht nahe am Strand wählen)");
        welleGo.addActionListener(e -> { r.imBild(() -> { if (!r.wellenritt()) r.hudMeldung("Mit der Welle: keine Nahbrandung am Auge (Spülsaum wählen)"); }); view.requestFocusInWindow(); });
        spuelGo.setToolTipText("Die Vorspülung beginnt jetzt: ein Sandkörper vor dem Kliff (N 6 089 250 bis 6 089 850), aufgespült in 72 h Szenenzeit, danach trägt ihn die Brandung ab");
        spuelGo.addActionListener(e -> { r.imBild(() -> { if (r.vorspuelung != null) r.vorspuelung.starten(r.clock.epochMillis()); }); view.requestFocusInWindow(); });
        extra.add(welleGo);
        extra.add(spuelGo);
        put(extra);
        put(serieLbl);
        put(small("<html><body style='width:215px'>Vier Regiebücher als Text (scene/regie/*.txt): ein Abend am Kliff, die Sturmflut nach Xaver, "
                + "ein Jahr am Kliff, als Kür mit der Welle. Jedes setzt Datum, Uhr, Zeitraffer und Wetter, wechselt Ansichten und Fahrten "
                + "(Treppe, Möwe, Drohne, Welle), wartet auf die Uhr, den Abbruch, die Serie oder die Welle („warte“) und fotografiert; "
                + "am Ende liegt ein Blatt mit allen Fotos in docs/status (Regiebuch_….png). Ohne Fenster: ant regiebuecher.</body></html>"));

        // ============================================================== CHRONIK und DATEN (Phase 16)
        chronikBlock();
        datenBlock();

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
            if (ticks++ % 2 == 0) { technik(); datenZeilen(); }
            nachmessen();
        }).start();
    }

    // ============================================================== Phase 16: CHRONIK und DATEN

    private com.dan.sylt.data.Chronik chronik;
    private final JLabel chronikLbl = small(""), eintragLbl = small(""), datenLbl = small(""), presetLbl = small(""), protLbl = small(""), gemerktLbl = small(""),
            flutLbl = small(""), karteLbl = small("");
    private FComboBox eintragBox, paketBox, presetBox, sitzungBox, gemerktBox, flutBox;
    private java.util.List<com.dan.sylt.data.Daten.PaketPreset> presetListe = java.util.List.of();
    private java.util.List<com.dan.sylt.data.Daten.Ansicht> gemerktListe = java.util.List.of();
    private volatile String datenNote = "";
    private long naechsteListe;

    /** Phase 16: die Chronik der laufenden Szene (aus der App, sobald sie steht). */
    public void chronik(com.dan.sylt.data.Chronik c) {
        chronik = c;
        neuLesen();
    }

    private static com.dan.sylt.data.Speicher sp() { return com.dan.sylt.data.Datenseite.speicher(); }

    /** Eine Combo neu füllen, ohne dass ihr Hörer feuert; die Auswahl bleibt, wenn es den Eintrag noch gibt. */
    private void fuellen(FComboBox box, java.util.List<?> l, String wahl) {
        fromScene = true;
        try {
            box.removeAllItems();
            int k = -1;
            for (int i = 0; i < l.size(); i++) { box.addItem(l.get(i)); if (l.get(i).toString().equals(wahl)) k = i; }
            if (k >= 0) box.setSelectedIndex(k); else if (!l.isEmpty()) box.setSelectedIndex(0);
        } finally { fromScene = false; }
    }

    private static String gewaehlt(FComboBox box) { Object o = box.getSelectedItem(); return o == null ? null : o.toString(); }

    /** Im Faden der Daten arbeiten, danach auf dem Swing-Faden weiter; Fehler landen im Protokoll und in einer Meldung. */
    private <T> void hinten(String was, java.util.concurrent.Callable<T> arbeit, java.util.function.Consumer<T> danach) {
        com.dan.store.Dienst.spaeter(() -> {
            try {
                T t = arbeit.call();
                javax.swing.SwingUtilities.invokeLater(() -> danach.accept(t));
            } catch (Exception ex) {
                com.dan.store.Protokoll.fehler("Bedienfeld", was + ": " + ex.getMessage(), ex instanceof RuntimeException ? ex : null);
                javax.swing.SwingUtilities.invokeLater(() -> com.dan.foptionpane.FOptionPane.showMessageDialog(this, was + " ging nicht:\n" + ex.getMessage(),
                        "Sylt · " + was, com.dan.foptionpane.FOptionPane.ERROR_MESSAGE));
            }
        });
    }

    /** Eine Sitzung mit Chronik in der Auswahl. */
    private record Sitzungszeile(long id) {
        @Override public String toString() { return "Sitzung " + id + (id == com.dan.store.Dienst.sitzung() ? " (diese)" : ""); }
    }

    /**
     * Block CHRONIK (F45 A): Zähler, die Liste der Abbrüche, Sturmfluten und Vorspülungen (Auswahl springt mit der Kamera
     * an die Stelle und mit der Uhr an den Zeitpunkt), die Kliffkarte aller Kanten, Chronik an/aus und leeren.
     */
    private void chronikBlock() {
        block("CHRONIK", false);
        put(chronikLbl);
        sub("Abbrüche, Sturmfluten, Vorspülungen");
        eintragBox = new FComboBox();
        eintragBox.setToolTipText("Neueste zuerst; die Auswahl führt die Kamera an die Stelle");
        eintragBox.addActionListener(e -> {
            if (fromScene) return;
            if (eintragBox.getSelectedItem() instanceof com.dan.sylt.data.Daten.Eintrag en) hinZu(en, false);
        });
        put(eintragBox);
        JPanel g1 = grid(2);
        FButton hin = new FButton("Hin"), uhr = new FButton("Uhr dorthin");
        hin.setToolTipText("Die Kamera vor die Stelle (110 m seewärts, 16 m hoch)");
        hin.addActionListener(e -> { if (eintragBox.getSelectedItem() instanceof com.dan.sylt.data.Daten.Eintrag en) hinZu(en, false); });
        uhr.setToolTipText("Die Uhr auf den Zeitpunkt des Eintrags (das Gelände bleibt, wie es jetzt ist)");
        uhr.addActionListener(e -> { if (eintragBox.getSelectedItem() instanceof com.dan.sylt.data.Daten.Eintrag en) hinZu(en, true); });
        g1.add(hin);
        g1.add(uhr);
        put(g1);
        put(eintragLbl);
        JPanel g2 = grid(2);
        FButton karte = new FButton("Kliffkarte"), schreib = new FButton("Chronik aus");
        karte.setToolTipText("Alle Kanten in einem Bild als PNG nach docs/status: die Kante 2024 und jedes Stück nach einem Abbruch, farbig nach der Zeit, "
                + "darunter der Rückgang gegenüber 2024");
        karte.addActionListener(e -> {
            if (chronik != null) chronik.warten();
            java.io.File ordner = r.photoDir != null ? r.photoDir : new java.io.File("docs/status");
            hinten("Kliffkarte", () -> com.dan.sylt.data.Kliffkarte.schreiben(sp(), ordner), f -> {
                r.hudMeldung("Kliffkarte: " + f.getPath());
                txt(karteLbl, "<html><body style='width:215px'>Kliffkarte: " + esc(f.getName()) + "</body></html>");
            });
        });
        schreib.setToolTipText("Die Chronik anhalten oder wieder schreiben lassen (die Szene läuft weiter)");
        schreib.addActionListener(e -> {
            if (chronik == null) return;
            chronik.schreiben(!chronik.schreibt());
            schreib.setText(chronik.schreibt() ? "Chronik aus" : "Chronik an");
        });
        g2.add(karte);
        g2.add(schreib);
        put(g2);
        put(karteLbl);
        sub("Chronik leeren (eigene Zeilen einer Sitzung)");
        sitzungBox = new FComboBox();
        put(sitzungBox);
        JPanel g3 = grid(1);
        FButton leeren = new FButton("Leeren …");
        leeren.addActionListener(e -> {
            if (!(sitzungBox.getSelectedItem() instanceof Sitzungszeile s)) return;
            if (com.dan.foptionpane.FOptionPane.showConfirmDialog(this, "Alle Abbrüche (mit Kanten und Findlingen), Sturmfluten, Vorspülungen, Serien und Läufe der "
                    + "Regiebücher der Sitzung " + s.id + " löschen?\nDie Kante 2024 und alles Mitgelieferte bleiben.", "Sylt · Chronik leeren",
                    com.dan.foptionpane.FOptionPane.YES_NO_OPTION) != com.dan.foptionpane.FOptionPane.YES_OPTION) return;
            hinten("Chronik leeren", () -> sp().chronikLeeren(s.id), n -> { r.hudMeldung("Chronik der Sitzung " + s.id + ": " + n + " Zeilen gelöscht"); neuLesen(); });
        });
        g3.add(leeren);
        put(g3);
        put(small("<html><body style='width:215px'>Die Chronik schreibt jeden Abbruch mit Ort, Bilanz, Kegel, Kehle, Pegel und Auflauf, dazu das neue Stück "
                + "Kante, die abgebrochene Fläche und die Findlinge; jede Sturmflut mit Scheitel, Windstau und Stunden am Fuß; jede Vorspülung; alle "
                + "zehn Minuten die Serien der Brandung; jeden Lauf eines Regiebuchs mit Blatt. In QGIS: SY_KANTE_V (Kante zu jedem Zeitpunkt, Filter "
                + "auf GUELTIG_AB_MS/GUELTIG_BIS_MS), SY_RUECKGANG_V, SY_KUESTE_V; ohne Datenbank daten/chronik/*.csv mit Spalte WKT (EPSG:25832).</body></html>"));
    }

    /** Auf einen Eintrag der Chronik: Kamera an die Stelle, mit {@code uhr} auch die Uhr. */
    private void hinZu(com.dan.sylt.data.Daten.Eintrag en, boolean uhr) {
        if (uhr && en.ms() > 0) clock.setEpochMillis(en.ms());
        if (!Double.isNaN(en.x())) r.zurStelle(en.x(), en.z(), en.text());
        else r.hudMeldung(en.text());
        view.setMode(r.camera.mode);
        syncFromScene();
        view.requestFocusInWindow();
    }

    /** Block DATEN (F45 A): Verbindung, Einrichten, Prüfen, Übernehmen, Weiter wie zuletzt, Sturmfluten, Presets, gemerkte Ansichten, Protokoll. */
    private void datenBlock() {
        block("DATEN", false);
        put(datenLbl);
        put(dbLbl);
        JPanel g = new JPanel(new java.awt.GridLayout(0, 2, 6, 4));
        g.setOpaque(false);
        FButton verb = new FButton("Verbinden …"), einr = new FButton("Einrichten"), pruef = new FButton("Prüfen"), ohne = new FButton("Ohne Datenbank"),
                weiter = new FButton("Weiter wie zuletzt"), ueber = new FButton("Übernehmen");
        verb.setToolTipText("Mit dem Schema aus db/db.properties verbinden (fragt nach dem Passwort, wenn keins hinterlegt ist)");
        verb.addActionListener(e -> verbinden());
        einr.setToolTipText("Die Skripte aus db/sql ausführen und alles prüfen; löscht nie etwas");
        einr.addActionListener(e -> einrichter(false));
        pruef.setToolTipText("Nur prüfen: Tabellen, Spalten, Sequenzen, Trigger, Sichten, Metadaten, räumliche Indizes, Zahl der Startdaten");
        pruef.addActionListener(e -> einrichter(true));
        ohne.setToolTipText("Mit Dateien in daten/ weiterarbeiten");
        ohne.addActionListener(e -> {
            com.dan.sylt.data.Datenseite.setzen(new com.dan.sylt.data.DateiSpeicher(com.dan.sylt.data.DateiSpeicher.ordner()));
            neuLesen();
        });
        weiter.setToolTipText("Die letzte gesicherte Sitzung herstellen: Uhr, Wetter, Sturmflut, Kamera, Regler und das Kliff mit allen Abbrüchen, Kegeln und der Vorspülung");
        weiter.addActionListener(e -> weiterWieZuletzt());
        ueber.setToolTipText("Chronik, eigene Regiebücher, Ansichten und Presets aus daten/ in die Datenbank (die CSV heißen danach *.uebernommen)");
        ueber.addActionListener(e -> uebernehmen());
        g.add(verb); g.add(ohne); g.add(einr); g.add(pruef); g.add(weiter); g.add(ueber);
        put(g);
        sub("Sturmfluten an Sylt seit 1962 (belegt)");
        flutBox = new FComboBox();
        flutBox.addActionListener(e -> {
            if (fromScene) return;
            if (flutBox.getSelectedItem() instanceof com.dan.sylt.data.Daten.Sturmflut s) {
                String sch = s.list() == null ? "Scheitel List nicht gefunden" : "PN List".equals(s.bezug())
                        ? String.format(Locale.GERMAN, "Scheitel List %d cm über Pegelnull", s.list())
                        : String.format(Locale.GERMAN, "Scheitel List NHN %+.2f m", s.list() / 100.0);
                if (s.hoernum() != null) sch += String.format(Locale.GERMAN, ", Hörnum NHN %+.2f m", s.hoernum() / 100.0);
                txt(flutLbl, "<html><body style='width:215px'>" + esc(s.name()) + " · " + sch + (s.kliffverlustText() == null ? "" : "<br>Kliff: " + esc(s.kliffverlustText()))
                        + (s.folgen() == null ? "" : "<br>" + esc(s.folgen())) + (s.quelle() == null ? "" : " [" + s.quelle() + "]") + "</body></html>");
            }
        });
        put(flutBox);
        put(flutLbl);
        sub("Presets der Packages");
        java.util.List<String> pk = new java.util.ArrayList<>();
        for (String k : com.dan.sylt.data.Startdaten.PAKETE) pk.add(k.substring(k.lastIndexOf('.') + 1) + " · " + k.substring(0, k.lastIndexOf('.')));
        paketBox = new FComboBox(pk.toArray());
        presetBox = new FComboBox();
        paketBox.addActionListener(e -> { if (!fromScene) listePresets(null); });
        put(paketBox);
        put(presetBox);
        FButton an = new FButton("Anwenden"), unter = new FButton("Speichern unter …");
        an.setToolTipText("Alle Felder des Presets in das laufende Objekt der Szene schreiben (Surf und Dune: die Wetterregie geht auf „von Hand“)");
        an.addActionListener(e -> presetAnwenden());
        unter.setToolTipText("Die Felder, wie sie jetzt in der Szene stehen, als eigenes Preset");
        unter.addActionListener(e -> presetSpeichern());
        JPanel pg = grid(2);
        pg.add(an);
        pg.add(unter);
        put(pg);
        put(presetLbl);
        sub("Gemerkte Ansichten (Taste V merkt)");
        gemerktBox = new FComboBox();
        put(gemerktBox);
        FButton gh = new FButton("Hin"), gl = new FButton("Löschen");
        gh.addActionListener(e -> gemerkteAnsicht());
        gl.addActionListener(e -> {
            int i = gemerktBox.getSelectedIndex();
            if (i < 0 || i >= gemerktListe.size()) return;
            long id = gemerktListe.get(i).id();
            hinten("Ansicht löschen", () -> { sp().loeschenAnsicht(id); return null; }, x -> listeGemerkt());
        });
        JPanel gg = grid(2);
        gg.add(gh);
        gg.add(gl);
        put(gg);
        put(gemerktLbl);
        view.setKeyAction(java.awt.event.KeyEvent.VK_V, () -> javax.swing.SwingUtilities.invokeLater(this::ansichtMerken));
        sub("Protokoll");
        put(protLbl);
        put(small("<html><body style='width:215px'>Das Protokoll geht alle 5 s in SY_LOG (90 Tage) oder daten/protokoll.txt, der Zustand mit dem Kliff alle "
                + "2 min und beim Ende in SY_SITZUNG oder daten/sitzungen, dazu ein Vorschaubild. Schema SYLT auf PDBORCL, Geometrien in UTM 32N (25832), "
                + "die Sichten _V in WGS 84 (8307) für QGIS.</body></html>"));
    }

    private String paket() { String s = gewaehlt(paketBox); return s == null ? null : s.substring(s.indexOf(" · ") + 3); }
    private String klasse() { String s = gewaehlt(paketBox); return s == null ? null : s.substring(0, s.indexOf(" · ")); }

    private void listePresets(String wahl) {
        String p = paket(), k = klasse();
        if (p == null) return;
        hinten("Presets lesen", () -> sp().presets(p, k), l -> {
            presetListe = l;
            fuellen(presetBox, l, wahl);
            Object o = com.dan.sylt.data.Zustand.objekt(r, k);
            txt(presetLbl, "<html><body style='width:215px'>" + l.size() + " Presets · " + (o == null ? "nicht in der Szene" : "in der Szene: " + o.getClass().getSimpleName()) + "</body></html>");
        });
    }

    private void presetAnwenden() {
        int i = presetBox.getSelectedIndex();
        if (i < 0 || i >= presetListe.size()) return;
        var pr = presetListe.get(i);
        Object o = com.dan.sylt.data.Zustand.objekt(r, pr.klasse());
        if (o == null) { txt(presetLbl, pr.klasse() + " läuft nicht in der Szene."); return; }
        if (o == r.wiring.surf || o == r.wiring.dune) hand();
        java.util.List<String> f = com.dan.store.Felder.schreiben(o, pr.felder());
        txt(presetLbl, "<html><body style='width:215px'>„" + esc(pr.name()) + "“ angewandt: " + (pr.felder().size() - f.size()) + " Felder"
                + (f.isEmpty() ? "" : ", nicht: " + esc(String.join(", ", f))) + "</body></html>");
        com.dan.store.Protokoll.info("Preset", pr.klasse() + " ← „" + pr.name() + "“");
        syncFromScene();
        view.requestFocusInWindow();
    }

    private void presetSpeichern() {
        String p = paket(), k = klasse();
        Object o = com.dan.sylt.data.Zustand.objekt(r, k);
        if (o == null) { txt(presetLbl, k + " läuft nicht in der Szene."); return; }
        String n = com.dan.foptionpane.FOptionPane.showInputDialog(this, "Name des Presets für " + k, "Sylt · Preset speichern", com.dan.foptionpane.FOptionPane.QUESTION_MESSAGE);
        if (n == null || n.isBlank()) return;
        var pr = new com.dan.sylt.data.Daten.PaketPreset(p, k, n.strip(), "Aus der Szene am " + LocalDate.now(), com.dan.sylt.data.Daten.EIGEN, com.dan.store.Felder.lesen(o));
        hinten("Preset speichern", () -> { sp().speichern(pr); return null; }, x -> listePresets(pr.name()));
    }

    private void listeGemerkt() {
        hinten("Ansichten lesen", () -> sp().ansichten(), l -> {
            java.util.List<com.dan.sylt.data.Daten.Ansicht> g = new java.util.ArrayList<>();
            for (var a : l) if ("GEMERKT".equals(a.art())) g.add(a);
            gemerktListe = g;
            fuellen(gemerktBox, g, null);
            txt(gemerktLbl, g.size() + " gemerkte Ansichten in " + sp().wo());
        });
    }

    /** Ein Bild der Szene auf 320 px Breite als PNG (für gemerkte Ansichten und die Vorschau der Sitzung). */
    public static byte[] vorschau(java.awt.image.BufferedImage img) {
        try {
            java.awt.image.BufferedImage s = new java.awt.image.BufferedImage(320, (int) Math.round(320.0 * img.getHeight() / img.getWidth()), java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = s.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, 0, 0, s.getWidth(), s.getHeight(), null);
            g2.dispose();
            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(s, "png", bo);
            return bo.toByteArray();
        } catch (Exception ex) {
            return null;
        }
    }

    /** Taste V (F41 A): die Ansicht mit Zustand und Vorschau merken. */
    private void ansichtMerken() {
        double[] k = com.dan.sylt.data.Zustand.kamera(r);
        long ms = (long) clock.epochMillis();
        String name = com.dan.sylt.data.Daten.text(ms) + " · " + r.wiring.regie.lage().label;
        String z = com.dan.store.Felder.alsText(com.dan.sylt.data.Zustand.erfassen(r, false), "Sylt · gemerkte Ansicht");
        double oe = r.camera.fovy;
        r.grabRequest = img -> {
            var a = new com.dan.sylt.data.Daten.Ansicht(0, name, null, "GEMERKT", k, oe, null, ms, z, vorschau(img), com.dan.sylt.data.Daten.EIGEN);
            long s = com.dan.store.Dienst.sitzung();
            hinten("Ansicht merken", () -> sp().speichern(a, s > 0 ? s : null), id -> { r.hudMeldung("Ansicht gemerkt: " + name); listeGemerkt(); });
        };
    }

    private void gemerkteAnsicht() {
        int i = gemerktBox.getSelectedIndex();
        if (i < 0 || i >= gemerktListe.size()) return;
        long id = gemerktListe.get(i).id();
        hinten("Ansicht lesen", () -> sp().ansicht(id), a -> {
            if (a.zustand() != null) {
                java.util.List<String> f = com.dan.sylt.data.Zustand.anwenden(r, com.dan.store.Felder.ausText(a.zustand()));
                if (!f.isEmpty()) com.dan.store.Protokoll.warnung("Ansicht", "nicht hergestellt: " + String.join(", ", f));
            } else {
                double[] k = a.kamera();
                r.camera.stopFahrt();
                r.camera.stopMitfahrt();
                r.camera.mode = Camera.Mode.FREI;
                r.camera.place(k[0], k[1], k[2], k[3], k[4]);
                r.camera.snap();
                if (a.szeneMs() != null) clock.setEpochMillis(a.szeneMs());
            }
            r.hudMeldung("Ansicht: " + a.name());
            view.setMode(r.camera.mode);
            syncFromScene();
            view.requestFocusInWindow();
        });
    }

    /** Nach einem Wechsel des Speichers alle Listen neu lesen. */
    private void neuLesen() {
        if (paketBox == null) return;
        listePresets(null);
        listeGemerkt();
        hinten("Sturmfluten lesen", () -> sp().sturmfluten(), l -> fuellen(flutBox, l, gewaehlt(flutBox)));
        naechsteListe = 0;
        datenZeilen();
    }

    private void verbinden() {
        datenNote = "verbinde …";
        hinten("Verbinden", Verbindung::konfig, k -> {
            com.dan.store.Db.Konfig kk = k;
            char[] neu = null;
            if (!k.hatPasswort()) {
                com.dan.ftextfield.FPasswordfield pw = new com.dan.ftextfield.FPasswordfield();
                JPanel p = new JPanel(new BorderLayout(0, 6));
                p.add(new JLabel("Passwort für " + k.kurz() + ":"), BorderLayout.NORTH);
                p.add(pw, BorderLayout.CENTER);
                if (com.dan.foptionpane.FOptionPane.showConfirmDialog(this, p, "Sylt · Datenbank", com.dan.foptionpane.FOptionPane.OK_CANCEL_OPTION)
                        != com.dan.foptionpane.FOptionPane.OK_OPTION || pw.getPassword().length == 0) { datenNote = ""; return; }
                neu = pw.getPassword();
                kk = k.mitPasswort(neu);
            }
            char[] eingabe = neu;
            com.dan.store.Db.Konfig kf = kk;
            hinten("Verbinden", () -> {
                String[] grund = {""};
                com.dan.sylt.data.DbSpeicher d = com.dan.sylt.data.Datenseite.verbinden(kf, grund);
                if (d == null) throw new com.dan.store.Ablage.Fehler(grund[0]);
                if (eingabe != null) try { com.dan.store.Db.passwortSpeichern(kf, eingabe); } catch (Exception ex) { com.dan.store.Protokoll.warnung("Daten", "Passwort nicht eingetragen: " + ex.getMessage()); }
                int st = Verbindung.einrichter().stufe(d.verbindung());
                return new Object[]{d, st};
            }, o -> {
                var d = (com.dan.sylt.data.DbSpeicher) o[0];
                int st = (Integer) o[1];
                if (st < Verbindung.VERSION) {
                    if (com.dan.foptionpane.FOptionPane.showConfirmDialog(this, d.wo() + " ist noch nicht eingerichtet (Stufe " + st + ").\nJetzt einrichten? (löscht nie etwas)",
                            "Sylt · Datenbank", com.dan.foptionpane.FOptionPane.YES_NO_OPTION) != com.dan.foptionpane.FOptionPane.YES_OPTION) { d.schliessen(); datenNote = "nicht eingerichtet"; return; }
                    com.dan.sylt.data.Datenseite.setzen(d);
                    einrichter(false);
                    return;
                }
                com.dan.sylt.data.Datenseite.setzen(d);
                datenNote = "verbunden";
                neuLesen();
            });
        });
    }

    private void einrichter(boolean nurPruefen) {
        if (!(sp() instanceof com.dan.sylt.data.DbSpeicher d)) {
            com.dan.foptionpane.FOptionPane.showMessageDialog(this, "Erst mit der Datenbank verbinden („Verbinden …“).", "Sylt · Einrichter", com.dan.foptionpane.FOptionPane.ERROR_MESSAGE);
            return;
        }
        datenNote = nurPruefen ? "prüfe …" : "richte ein …";
        hinten(nurPruefen ? "Prüfen" : "Einrichten", () -> {
            var e = Verbindung.einrichter();
            return nurPruefen ? e.pruefen(d.verbindung(), Verbindung.skripte())
                    : e.einrichten(d.verbindung(), Verbindung.skripte(), (t, a) -> datenNote = String.format(Locale.GERMAN, "%.0f %% · %s", a * 100, t));
        }, er -> {
            Startbild.merkeEinrichter(er.kurz());
            datenNote = "";
            if (er.ok()) com.dan.store.Dienst.spaeter(com.dan.store.Dienst::neuBeginnen);
            com.dan.store.Protokoll.info("Einrichter", (nurPruefen ? "Prüfen: " : "Einrichten: ") + er.kurz());
            for (String z : er.zeilen()) if (!z.startsWith("ok") && !z.contains("alles in Ordnung")) com.dan.store.Protokoll.warnung("Einrichter", z);
            JLabel t = small("<html><body style='width:560px'>" + String.join("<br>", er.zeilen()) + "</body></html>");
            javax.swing.JScrollPane spn = new javax.swing.JScrollPane(t);
            spn.setPreferredSize(new Dimension(640, 420));
            com.dan.foptionpane.FOptionPane.showMessageDialog(this, spn, "Sylt · " + (nurPruefen ? "Prüfen: " : "Einrichten: ") + er.kurz(),
                    er.ok() ? com.dan.foptionpane.FOptionPane.INFORMATION_MESSAGE : com.dan.foptionpane.FOptionPane.WARNING_MESSAGE);
            neuLesen();
        });
    }

    private void uebernehmen() {
        if (!sp().datenbank()) {
            com.dan.foptionpane.FOptionPane.showMessageDialog(this, "Erst mit der Datenbank verbinden („Verbinden …“).", "Sylt · Übernehmen", com.dan.foptionpane.FOptionPane.ERROR_MESSAGE);
            return;
        }
        var von = new com.dan.sylt.data.DateiSpeicher(com.dan.sylt.data.DateiSpeicher.ordner());
        datenNote = "übernehme …";
        hinten("Übernehmen", () -> com.dan.sylt.data.Uebernahme.uebernehmen(von, sp()), z -> {
            datenNote = "übernommen";
            com.dan.foptionpane.FOptionPane.showMessageDialog(this, "Aus " + von.wo() + " nach " + sp().wo() + ":\n" + z, "Sylt · Übernahme",
                    com.dan.foptionpane.FOptionPane.INFORMATION_MESSAGE);
            neuLesen();
        });
    }

    private void weiterWieZuletzt() {
        hinten("Weiter wie zuletzt", () -> {
            var s = sp().letzteSitzung(com.dan.store.Dienst.sitzung());
            if (s == null || s.zustand() == null) throw new com.dan.store.Ablage.Fehler("Keine gesicherte Sitzung in " + sp().wo());
            return s;
        }, s -> { Weiter.jetzt(r, s, chronik); view.setMode(r.camera.mode); syncFromScene(); });
    }

    /** Die Einträge der Chronik für die Liste (neueste zuerst). */
    public static java.util.List<com.dan.sylt.data.Daten.Eintrag> eintraege(com.dan.sylt.data.Speicher sp, int n) throws com.dan.store.Ablage.Fehler {
        java.util.List<com.dan.sylt.data.Daten.Eintrag> l = new java.util.ArrayList<>();
        for (var a : sp.abbrueche(n))
            l.add(new com.dan.sylt.data.Daten.Eintrag("ABBRUCH", a.ms(), a.text(), a.bruchstelle() == null ? Double.NaN : a.bruchstelle()[0],
                    a.bruchstelle() == null ? 0 : a.bruchstelle()[1], a.id()));
        for (var s : sp.sturmflutLaeufe(n)) {
            double[] xz = com.dan.sylt.scene.Site.fromUtm(com.dan.sylt.scene.Site.wasserlinieE(6_089_500) + 60, 6_089_500);
            l.add(new com.dan.sylt.data.Daten.Eintrag("STURMFLUT", s.beginnMs(), s.text(), xz[0], xz[1], s.id()));
        }
        for (var v : sp.vorspuelungen()) {
            if (!"APP".equals(v.art()) || v.beginnMs() == null) continue;
            double[] m = v.flaeche() == null ? null : v.flaeche().xz()[0];
            l.add(new com.dan.sylt.data.Daten.Eintrag("VORSPUELUNG", v.beginnMs(), v.text(), m == null ? Double.NaN : m[0], m == null ? 0 : m[1], v.id()));
        }
        l.sort((a, b) -> Long.compare(b.ms(), a.ms()));
        return l.size() > n ? new java.util.ArrayList<>(l.subList(0, n)) : l;
    }

    /** Jede Sekunde: Zeilen von CHRONIK und DATEN; alle 10 s die Listen aus dem Speicher. */
    private void datenZeilen() {
        if (eintragBox == null) return;
        var c = chronik;
        txt(chronikLbl, "<html><body style='width:215px'>" + esc(c == null ? "Chronik: startet mit der Szene" : c.describe()) + " · " + esc(sp().wo()) + "</body></html>");
        long gs = com.dan.store.Dienst.gesichert();
        txt(datenLbl, "<html><body style='width:215px'>" + esc(sp().wo()) + (datenNote.isEmpty() ? "" : " · " + esc(datenNote))
                + "<br>Sitzung " + com.dan.store.Dienst.sitzung() + (gs > 0 ? ", gesichert " + String.format("%tR", gs) + " Uhr (Rechner)" : ", noch nicht gesichert")
                + (com.dan.sylt.data.Datenseite.einrichterStand.isEmpty() ? "" : "<br>Einrichter: " + esc(com.dan.sylt.data.Datenseite.einrichterStand))
                + (com.dan.store.Dienst.letzterFehler().isEmpty() ? "" : "<br>Zuletzt: " + esc(com.dan.store.Dienst.letzterFehler())) + "</body></html>");
        long now = System.currentTimeMillis();
        if (now < naechsteListe) return;
        naechsteListe = now + 10_000;
        com.dan.store.Dienst.spaeter(() -> {
            try {
                var sp = sp();
                var ein = eintraege(sp, 40);
                var stand = sp.stand();
                var prot = sp.letzteZeilen(6);
                java.util.List<Sitzungszeile> sl = new java.util.ArrayList<>();
                long dies = com.dan.store.Dienst.sitzung();
                if (dies > 0) sl.add(new Sitzungszeile(dies));
                if (stand.letzte() != null && stand.letzte().id() != dies) sl.add(new Sitzungszeile(stand.letzte().id()));
                String st = String.format(Locale.GERMAN, "<html><body style='width:215px'>%d Abbrüche (%d Findlinge), %d Sturmfluten, %d Vorspülungen, %d Serien · "
                                + "Kante 2024 in %d Stücken · %d Sitzungen</body></html>", stand.abbrueche(), stand.findlinge(), stand.sturmflutLaeufe(), stand.vorspuelungenApp(),
                        stand.serien(), stand.kantenstuecke(), stand.sitzungen());
                StringBuilder pb = new StringBuilder("<html><body style='width:215px'>");
                for (String z : prot) pb.append(esc(z.length() > 140 ? z.substring(0, 140) + " …" : z)).append("<br>");
                String ps = pb.append("</body></html>").toString();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (popupOpen(this) || menuOpen()) return;
                    fuellen(eintragBox, ein, gewaehlt(eintragBox));
                    fuellen(sitzungBox, sl, gewaehlt(sitzungBox));
                    txt(eintragLbl, st);
                    txt(protLbl, ps);
                });
            } catch (Exception ex) {
                javax.swing.SwingUtilities.invokeLater(() -> txt(eintragLbl, "Chronik nicht lesbar: " + esc(String.valueOf(ex.getMessage()))));
            }
        });
    }

    /** Seit Phase 13: ein Regler von Hand stellt die Wetterregie ab (die Lage hielte sonst ihre Werte). */
    private void hand() {
        if (r.wiring.regie.modus() == com.dan.sylt.wiring.Wetterregie.Modus.HAND) return;
        r.wiring.regie.hand();
        fromScene = true;
        regieBox.setSelectedIndex(0);
        fromScene = false;
    }

    /** Seit Phase 13: ein Blitz von Hand (ohne Gewitter wird es „mäßig“ von Hand). */
    private void blitz(boolean turm) {
        if (r.wiring.gewitter == 0) { hand(); r.wiring.gewitter = 2; }
        r.unwetter.blitzVonHand(r.camera.eye, r.camera.fwd, turm);
        syncFromScene();
    }

    private void wetter(double oktas, com.dan.cloud.CloudType art, double hoch, double sicht) {
        hand();
        r.wiring.oktas = oktas;
        r.wiring.cloudType = art;
        r.wiring.highCover = hoch;
        r.wiring.visibilityKm = sicht;
        syncFromScene();
        view.requestFocusInWindow();
    }

    private void seegang(com.dan.surf.Surf p) {
        hand();
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
        // seit Phase 13: Regie, Regen, Nebel, Gewitter nach der Szene
        if (!regenMmSl.getValueIsAdjusting()) regenMmSl.setValue((int) Math.round(Math.min(30, r.wiring.regen) * 10));
        if (!nebelSl.getValueIsAdjusting()) nebelSl.setValue((int) Math.round(r.wiring.nebel * 100));
        if (gewitterBox != null && gewitterBox.getSelectedIndex() != Math.min(4, r.wiring.gewitter)) gewitterBox.setSelectedIndex(Math.min(4, r.wiring.gewitter));
        if (regieBox != null) {
            var rg = r.wiring.regie;
            int idx = switch (rg.modus()) {
                case HAND -> 0;
                case LAGE -> 1 + rg.lage().ordinal();
                case FREI -> 1 + com.dan.sylt.wiring.Wetterregie.Lage.values().length;
                case STURMTAG -> 2 + com.dan.sylt.wiring.Wetterregie.Lage.values().length;
            };
            if (regieBox.getSelectedIndex() != idx) regieBox.setSelectedIndex(idx);
        }
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
        txt(pegelLbl, String.format(Locale.GERMAN, "Pegel %+.2f m NHN (%s)", r.wiring.pegel, r.wiring.tideAn ? "Tide" : "von Hand"));
        txt(tideLbl, "<html><body style='width:215px'>" + esc(r.wiring.tideZeile()) + "</body></html>");
        if (tideBox.isSelected() != r.wiring.tideAn) tideBox.setSelected(r.wiring.tideAn);
        com.dan.surf.WaveField.Result fr = r.brandung.feld.result();
        txt(brandungLbl, "<html><body style='width:215px'>" + esc((fr == null ? "Wellenfeld wird gerechnet …" : String.format(Locale.GERMAN,
                "Wellenfeld %d × %d zu %.0f m, %.0f ms%s · Auflauf R_2%% %.2f m (Stockdon)", fr.nu, fr.nv, r.brandung.feld.cell, fr.ms,
                r.brandung.feld.busy() ? " (rechnet neu)" : "", r.brandung.auflaufR2())) + " · " + r.nah.meldung) + "</body></html>");
        com.dan.surf.Surf sf = r.wiring.surf;
        txt(hsLbl, String.format(Locale.GERMAN, "Wellenhöhe draußen H_s %.1f m", sf.hs));
        txt(tpLbl, String.format(Locale.GERMAN, "Periode T_p %.0f s (L₀ = %.0f m)", sf.tp, com.dan.surf.Surf.deepLength(sf.tp)));
        txt(dirLbl, String.format(Locale.GERMAN, "aus %03.0f° (%s)", sf.seaFromDeg, compass(sf.seaFromDeg)));
        txt(surfLbl, "<html><body style='width:215px'>" + esc(r.wiring.surfZeile(r.camera.eye[0], r.camera.eye[2])) + "</body></html>");
        txt(windLbl, String.format(Locale.GERMAN, "Wind in 10 m: %.0f m/s", r.wiring.windSpeed));
        txt(kliffLbl, "<html><body style='width:215px'>" + esc(r.sturmflut.describe(r.clock) + (r.abbruch == null ? "" : " · " + r.abbruch.describe())) + "</body></html>");
        txt(tuermeLbl, "<html><body style='width:215px'>" + esc(r.feuer.describe() + " · " + r.koerbe.describe() + " · " + r.herde.describe()) + "</body></html>");
        txt(windDirLbl, String.format(Locale.GERMAN, "aus %03.0f° (%s)", r.wiring.windFromDeg, compass(r.wiring.windFromDeg)));
        txt(duneLbl, "<html><body style='width:215px'>" + esc(r.sand.zeile(r.camera.eye[0], r.camera.eye[2])) + "</body></html>");
        txt(regenLbl, r.sand.regen <= 0 ? "Strand trocken (kein Regen)" : String.format(Locale.GERMAN, "nach Regen: Sand %.1f %% nass", r.sand.regen * 100));
        txt(oktasLbl, String.format(Locale.GERMAN, "Bedeckung %.0f/8", r.wiring.oktas));
        txt(highLbl, String.format(Locale.GERMAN, "Hoher Schleier %.0f %%", r.wiring.highCover * 100));
        txt(sichtLbl, String.format(Locale.GERMAN, "Sicht %.0f km", r.wiring.visibilityKm));
        txt(wetterLbl, "<html><body style='width:215px'>" + esc(String.format(Locale.GERMAN, "%s · Sonne am Auge %.0f %%%s", r.wiring.wetterZeile(),
                r.cloudShade() * 100, r.skyRunning() ? "" : " · Himmel nicht gestartet")) + "</body></html>");
        txt(regenMmLbl, r.wiring.regen <= 0.05 ? "Regen: trocken" : String.format(Locale.GERMAN, "Regen %.1f mm/h (unter den Vorhängen)", r.wiring.regen));
        txt(nebelLbl, r.wiring.nebel <= 0.02 ? "Seenebel: keiner" : String.format(Locale.GERMAN, "Seenebel %.0f %% (bis %.0f m über dem Pegel)", r.wiring.nebel * 100, r.wiring.nebelHoch));
        String[] uw = r.unwetterZeilen();
        txt(unwetterLbl, "<html><body style='width:215px'>" + esc(uw[0]) + "<br>" + esc(uw[1]) + "<br>" + esc(uw[2]) + "</body></html>");
        String[] lb = r.leben.describe();
        txt(lebenLbl, "<html><body style='width:215px'>" + esc(lb[0]) + "<br>" + esc(lb[1]) + "</body></html>");
        txt(lichtLbl, "<html><body style='width:215px'>" + esc(r.lichtZeile()) + "</body></html>");
        txt(klangLbl, "<html><body style='width:215px'>" + esc(r.klang.describe()) + "</body></html>");
        txt(regieLbl, "<html><body style='width:215px'>" + esc(r.regieLine()) + "</body></html>");
        txt(serieLbl, "<html><body style='width:215px'>" + esc(r.serie.describe()) + "<br>" + esc(r.vorspuelung != null ? r.vorspuelung.describe() : "Vorspülung: ohne Kante")
                + "<br>" + esc(r.spurLine()) + (r.ritt.laeuft() ? "<br>" + esc(r.ritt.describe()) : "") + "</body></html>");
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

    /** n Spalten, Zeilen nach Bedarf (seit Phase 19c; vorher eine Zeile, die Vorlagen standen zu acht nebeneinander und zeigten nur „…“). */
    private static JPanel grid(int n) {
        JPanel p = new JPanel(new java.awt.GridLayout(0, n, 6, 4));
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
