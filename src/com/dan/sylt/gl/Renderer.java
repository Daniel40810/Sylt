package com.dan.sylt.gl;

import com.dan.cloud.Clouds;
import com.dan.cloud.gl.CloudGl;
import com.dan.ground.Biome;
import com.dan.ground.Meadow;
import com.dan.ground.gl.GroundGl;
import com.dan.mudflat.gl.MudflatGl;
import com.dan.sky.Sky;
import com.dan.sky.gl.SkyGl;
import com.dan.sylt.app.Bibliothek;
import com.dan.sylt.camera.Ansichten;
import com.dan.sylt.camera.Camera;
import com.dan.sylt.scene.Buehne;
import com.dan.sylt.scene.Daten;
import com.dan.cliff.gl.CliffGl;
import com.dan.sylt.scene.KernKarte;
import com.dan.sylt.scene.Physik;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Sun;
import com.dan.sylt.scene.Terrain;
import com.dan.sylt.wiring.Bodendecke;
import com.dan.sylt.wiring.Strandnaesse;
import com.dan.sylt.wiring.Wiring;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Die Engine auf der Grafikkarte, übernommen aus Brocken und entschlackt (F6 A); seit Phase 3 das Grundbild mit den
 * Packages aus {@code Renderer.jar}. Je Bild:
 * <ol>
 * <li><b>Himmel</b>: {@code com.dan.sky} (Streuung, Sonne, Mond, Sterne) in einen HDR-Puffer (RGBA16F, umgekehrte
 * 32F-Tiefe), dazu das Rundumbild mit Wolken (Spiegelung, Dunst, Himmelslicht) und die Schattenkarte der Wolken
 * ({@link Wetter}); ohne {@code com.dan.sky} der Verlauf aus Phase 1 ({@code sky.frag}).</li>
 * <li><b>Gelände</b>: Kern, Hauptraster und Fernring aus {@link Terrain} als CDLOD ({@link TerrainLod}), im Kern bis 1 m
 * Punktabstand, mit Erdkrümmung; Farben nach der Art des Grunds (im Kern zu 1 m aus dem Luftbild, {@link KernKarte}),
 * am DOP20 geeicht; Strand und Watt aus {@code com.dan.mudflat} (Nässe nach dem Pegel, {@link Strandnaesse}); das Kliff
 * mit Grundmaterial; Wolkenschatten; zuschaltbar die Marken.</li>
 * <li><b>Boden</b>: Bodendecke um das Auge aus {@code com.dan.ground} mit dem Biom „Sylt“ (Strandhafer, Silbergras,
 * Krähenbeere, Besenheide, Kartoffelrose, F13 A), nur zu Fuß.</li>
 * <li><b>Meer</b>: die offene See aus {@code com.dan.ocean} mit dem Gelände als Grund ({@link Meer}); ohne Compute der
 * Platzhalter aus Phase 1 ({@code meer.frag}).</li>
 * <li><b>Wolken</b>: {@code com.dan.cloud} nach dem Gelände mit seiner Tiefe (aus Brocken).</li>
 * <li><b>Nachbearbeitung</b>: Belichtung aus der Augenanpassung an Himmel und Bild ({@link Belichtung}), Filmkurve.</li>
 * <li><b>HUD</b>: Tempo (Bilder/s, Zeit der Grafikkarte je Durchgang, Prozessor), Uhr, Sonne, Kamera, Ort in UTM.</li>
 * </ol>
 * Geblieben aus Brocken: Qualitätsstufen mit Auflösungsregelung, Zeitmessung jedes Durchgangs auf der Grafikkarte
 * (Zeitstempel, ein Bild versetzt gelesen), Compute-Test beim Start, HUD kurz/voll/aus, Foto (F12), Shader neu laden
 * (F5), Standbilder ohne Fenster ({@code tools.Tempo}), der Faden der Physik ({@link Physik}) mit festem Schritt und der
 * Kinomodus ({@code com.dan.kino}) als Anschluss für die Regiebücher in Phase 15. Im Bildlauf werden keine Dateien gelesen.
 */
public final class Renderer implements GLEventListener {
    public static final int MIN_MAJOR = 4, MIN_MINOR = 3;
    public static final String VERSION = "Phase 19b";
    /** Phase des Stands (für Ansichten und Bedienfeld). */
    public static final int PHASE = 3;
    public static final String[] PASSES = {"Himmel", "Gelände", "Boden", "Meer", "Wolken", "Nachbearbeitung", "HUD"};
    private static final int MARKS = PASSES.length + 1;
    /** Nah- und Fernebene (m): bis über den Fernring. */
    static final double NEAR = 0.3, FAR = 120_000;

    public final SceneClock clock;
    public final Camera camera = new Camera();
    /** Der Faden der Physik (F6 A); Phase 1 ohne Aufgaben. */
    public final Physik physik = new Physik();
    /** Das Gelände (Kern, Hauptraster, Fernring; Phase 1 aus der Bühne) und seine Detailstufen. */
    public final Terrain terrain;
    public final TerrainLod lod = new TerrainLod();
    /** Die Verdrahtung der Packages (Phase 1: Hüllen von surf und dune am Gelände). */
    public final Wiring wiring;
    /**
     * Seit Phase 15: der Kinomodus (com.dan.kino). Ein Regiebuch im Kino: ohne Bedienfeld und HUD, Balken 2,39 : 1, Vor-
     * und Abspann, Untertitel aus den Stichworten, Überblendung bei jedem Schnitt, ruhiger Schwenk, der Ton folgt dem Schwarz.
     */
    public final com.dan.kino.Kino kino = new com.dan.kino.Kino();
    /** Phase 3: Himmel und Wolken, die See, die Arten des Kerns zu 1 m, die Nässe des Sands, die Bodendecke. */
    public final Wetter wetter = new Wetter();
    public final Meer meer = new Meer();
    public final KernKarte karte;
    public final Strandnaesse naesse;
    /** Seit Phase 7a: die Brandung (Wellenfeld, gebackene Texturen, Buhnen) und das Netz nahe am Auge. */
    public final com.dan.sylt.scene.Brandung brandung;
    public final com.dan.sylt.scene.Nahbrandung nah;
    final BrandungGl brandungGl = new BrandungGl();
    /** Netz der Brecher nahe am Auge, Pfähle der Buhnen zeigen. */
    public volatile boolean showNetz = true, showBuhnen = true;
    /** Seit Phase 9a: der Flugsand in der Szene ({@code dune} mit dem Wind aus {@code wind}) und ob die Körner gezeigt werden. */
    public final com.dan.sylt.scene.Sand sand;
    public volatile boolean showKoerner = true;
    final SandGl sandGl = new SandGl();
    /**
     * Seit Phase 10: Türme und Bauten (Häuser aus LOD2, Quermarkenfeuer, Kampener Leuchtturm, Treppe, Wege, Strandlokal,
     * Rettungstürme), Strandkörbe nach Jahreszeit und Wind, die Galloways am Kampener Leuchtturm und die Leuchtfeuer.
     */
    public final com.dan.sylt.scene.Bauwerke bauwerke;
    public final com.dan.sylt.scene.Strandkoerbe koerbe;
    public final com.dan.sylt.scene.Herde herde;
    public final com.dan.sylt.scene.Leuchtfeuer feuer;
    final BautenGl bautenGl = new BautenGl();
    final FeuerGl feuerGl = new FeuerGl();
    public volatile boolean showBauten = true, showKoerbe = true, showHerde = true;
    private boolean koerbeNeu = true;
    private double umgebungLux = 1e4;
    /** Strömung der Brandung in der See zeigen (Rippströme trüben das Wasser), stärker als „DLRG-Blick“. */
    public volatile boolean showStroemung = true, dlrgBlick;
    private double naesseTakt;
    public final Meadow meadow;
    /** Phase 4a: das Rote Kliff aus com.dan.cliff (Lockergestein), Grasnarbe, Findlinge. */
    public final Kliff kliff;
    /**
     * Seit Phase 12: die Sturmflut nach Xaver 2013 (Wind, See, Windstau über die Tide) und der Abbruch des Kliffs
     * (Hohlkehle, {@code fracture}, Sturzkegel und Abtrag als Änderung des Geländes, F15 bis F17 A).
     */
    public final com.dan.sylt.scene.Sturmflut sturmflut = new com.dan.sylt.scene.Sturmflut();
    public final com.dan.sylt.scene.Kliffabbruch abbruch;
    public volatile boolean showBrocken = true;
    /**
     * Seit Phase 13: Wetter, Licht, Leben – Regen, Seenebel und Gewitter ({@link Unwetter}), das Leben am Strand (Möwen
     * im Hangaufwind, Sanderlinge und Austernfischer am Spülsaum mit Spuren, selten ein Seehund), Meeresleuchten in
     * Sommernächten und das grüne Leuchten beim Sonnenuntergang (beide zuschaltbar).
     */
    public final Unwetter unwetter = new Unwetter();
    public final com.dan.sylt.scene.Strandleben leben;
    /** Seit Phase 14: der Klang (com.dan.soundscape); Ton an im Bedienfeld, KLANG. */
    public final Klang klang;
    final LebenGl lebenGl = new LebenGl();
    public volatile boolean showLeben = true, meeresleuchten = true, gruenesLeuchten = true;
    /** Verstärkung des Meeresleuchtens (1 = wie gemessen) und ob eine Blüte erzwungen wird (für Vorführungen, NaN: nach Jahr). */
    public volatile double leuchtenGain = 1, bluete = Double.NaN;
    /** Vergrößerung der Spiegelung für das grüne Leuchten von Hand (NaN: nach dem Tag). */
    public volatile double gruenFest = Double.NaN;
    /** Nässe nach Regen 0..1 (Sand, Kliff), integriert in Szenenzeit. */
    private double sandNass, kliffNass;
    private long nebelStand;
    private double nebelTakt;
    private final com.dan.sylt.scene.Netz brockenNetz = new com.dan.sylt.scene.Netz();
    private boolean brockenDa;
    private int korrTex;
    private double abbruchMs = Double.NaN;
    private int fertigGesehen;
    private final GroundGl groundGl = new GroundGl();
    private final MudflatGl mudGl = new MudflatGl();
    private final Belichtung belichtung = new Belichtung();
    private boolean groundOk;
    private int klasseTex;
    /** Ebenen zum Abschalten (Bedienfeld). */
    public volatile boolean showClouds = true, showGround = true, showOcean = true;
    /** Phase 4a: das Kliff aus com.dan.cliff (aus: die Wand wie in Phase 3). */
    public volatile boolean showCliff = true;
    /** Belichtung im Bild messen (sonst nur aus dem Himmel geschätzt). */
    public volatile boolean bildMessung = !Boolean.getBoolean("sylt.belichtung.schaetzen");
    /** Anpassung des Auges: 0 = feste Belichtung des Tags, 1 = volle Anpassung an das Bild (wie com.dan.sky). */
    public volatile double adaptation = 0.85;
    /** Gewicht der Bildmessung gegen den Himmel (Rest) und ihr Faktor (mittleres Grau statt Mittel), aus Brocken. */
    public static final double BILD_FOLGT = 0.75, BILD_GAIN = 2.0;
    /** Wie stark eine dunkle Decke die Anpassung mitnimmt (Exponent auf com.dan.cloud.brightnessFactor). */
    public static final double STORM_ADAPT = 0.6;
    /** Dunst ohne com.dan.sky (wie common.glsl: 60 km), m. */
    static final double FOG_DISTANCE = 60_000;
    private double adaptLum = -1, viewLum = -1, viewTimer, cloudShade = 1;
    private boolean frameSky;

    private volatile Quality quality = Quality.MITTEL;
    public volatile double renderScale = quality.renderScale;
    /** Auflösung nach der Zeit der Grafikkarte regeln (nie unter {@link #MIN_SCALE}, nie über der Stufe). */
    public volatile boolean autoScale = true;
    public volatile double targetFps = 60;
    public static final double MIN_SCALE = 0.5;
    private long scaleNanos;
    private int scaleSteps;
    /** HUD an, kurz oder voll (H). */
    public volatile boolean showHud = true, hudFull;
    /** Die Marken zur Ausrichtung (UTM-Raster, Höhenlinien, Wasserlinie, Fernfeld, Kern, Orte); seit Phase 3 aus (M). */
    public volatile boolean showMarks = Boolean.getBoolean("sylt.marken");
    /** Die See zeigen (seit Phase 3 com.dan.ocean, sonst der Platzhalter). */
    public volatile boolean showMeer = true;
    /** Szene steht (für Standbilder und die Leertaste im Kino später). */
    public volatile boolean frozen;
    /** Belichtung von Hand in Blenden. */
    public volatile double exposureBias;
    public volatile boolean photoRequest;
    /** Ein Bild abholen (einmal, im GL-Faden), etwa für Werkzeuge. */
    public volatile Consumer<BufferedImage> grabRequest;
    /** Ordner und Name des nächsten Fotos (ohne Angabe docs/status und {@code Sylt_Datum_Zeit}). */
    public volatile File photoDir;
    public volatile String photoName;

    private final Shader skyShader = new Shader("Himmel", null, "fullscreen.vert", "sky.frag"),
            terrainShader = new Shader("Gelände", null, "terrain.vert", "terrain.frag"),
            meerShader = new Shader("Meer", null, "fullscreen.vert", "meer.frag"),
            post = new Shader("Nachbearbeitung", null, "fullscreen.vert", "post.frag"),
            overlay = new Shader("Überlagerung", null, "fullscreen.vert", "overlay.frag"),
            lumShader = new Shader("Belichtung", null, "fullscreen.vert", "lum.frag"),
            bautenShader = new Shader("Bauten", null, "bauten.vert", "bauten.frag");
    private final Shader[] all = {skyShader, terrainShader, meerShader, post, overlay, lumShader, bautenShader};
    private final HudOverlay hud = new HudOverlay();
    private int emptyVao, hdrTex, depthTex, hdrFbo, hdrColorFbo, hdrW, hdrH, surfW = 1, surfH = 1, defaultFbo;
    private boolean reversedZ, ready;
    private final int[][] stamp = new int[2][MARKS];
    private final double[] passMs = new double[MARKS - 1];
    private int frame;
    private long lastNanos, statsNanos;
    private int statsFrames;
    private double fps, gpuMs, cpuMs, worstMs;
    private volatile String glInfo = "", error;
    private volatile ComputeProbe.Result compute;
    private volatile boolean reloadShaders;
    private Consumer<String> statusListener = s -> { };
    private Consumer<String> errorListener = s -> { };
    private volatile String photoMsg = "", meldung = "";
    private volatile long photoMsgUntil, meldungUntil;
    /** Arbeiten aus anderen Fäden, im Bildlauf vor dem Schritt ausgeführt. */
    private final java.util.concurrent.ConcurrentLinkedQueue<Runnable> imBild = new java.util.concurrent.ConcurrentLinkedQueue<>();

    public Renderer(SceneClock clock) { this(clock, Terrain.load(Terrain.defaultDir())); }

    public Renderer(SceneClock clock, Terrain terrain) {
        this.clock = clock;
        this.terrain = terrain;
        this.wiring = new Wiring(clock);
        wiring.setTerrain(terrain);
        lod.use(terrain);
        karte = KernKarte.load(Daten.ordner());
        naesse = terrain.hasKern() ? new Strandnaesse(terrain, karte) : null;
        brandung = new com.dan.sylt.scene.Brandung(wiring.surf, terrain);
        wiring.surf.field = brandung.feld;
        nah = new com.dan.sylt.scene.Nahbrandung(brandung, terrain);
        sand = new com.dan.sylt.scene.Sand(terrain::height, (x, z) -> karte.at(terrain, x, z), (x, z) -> {
            if (naesse == null) return Double.NaN;
            int k = naesse.feld.index(x, z);
            return k < 0 ? Double.NaN : naesse.feld.wet[k];
        }, () -> wiring.pegel);
        wiring.stepTide();
        kliff = new Kliff(terrain, com.dan.sylt.scene.Kante.load(Daten.ordner()));
        abbruch = kliff.strata != null ? new com.dan.sylt.scene.Kliffabbruch(terrain, kliff.kante, kliff.cliff, kliff.strata) : null;
        if (abbruch != null) {
            // Staub beim Zerfall (F15 A): Feinkorn aus dem Lehm steigt auf und treibt mit dem Wind (Körner im Bildlauf)
            abbruch.onStaub = q -> imBild.add(() -> sand.koerner().burst(q[0], q[1], q[2], (int) Math.min(400, 20 + q[3] * 30), 2.5));
            physik.add(abbruch);
        }
        // seit Phase 10: Bauten aus LOD2 (die Türme dort weg, sie werden genauer gebaut), Strandkörbe, Herde, Leuchtfeuer
        double[][] tuerme = {{Site.QUERMARKE.x(), Site.QUERMARKE.z()}, {Site.LEUCHTTURM.x(), Site.LEUCHTTURM.z()}};
        bauwerke = new com.dan.sylt.scene.Bauwerke(com.dan.sylt.scene.Bauten.laden(Daten.ordner(), tuerme, 7), terrain::height, kliff.kante);
        koerbe = new com.dan.sylt.scene.Strandkoerbe(terrain::height);
        herde = new com.dan.sylt.scene.Herde(14, Site.LEUCHTTURM.x(), Site.LEUCHTTURM.z(), terrain::height, haeuserNahe(Site.LEUCHTTURM.x(), Site.LEUCHTTURM.z(), 500));
        feuer = new com.dan.sylt.scene.Leuchtfeuer(bauwerke.fokusKampen, bauwerke.fokusQuermarke, com.dan.beacon.Beacon.Quality.MEDIUM);
        leben = new com.dan.sylt.scene.Strandleben(terrain::height, kliff.kante, brandung.buhnen.pfaehle);
        klang = new Klang(this);
        // seit Phase 15: Vorspülung (in die Änderung des Geländes wie die Sturzkegel), Fahrt mit der Welle
        vorspuelung = abbruch != null ? new com.dan.sylt.scene.Vorspuelung(kliff.kante, abbruch.aenderung) : null;
        ritt = new com.dan.sylt.scene.Wellenritt(nah, terrain::height);
        // seit Phase 13: ein Blitz lässt den Himmel um seine Richtung aufleuchten (com.dan.sky), Licht je Schlag
        unwetter.blitz.onStroke = (b, k, x, y, z, cd) -> wetter.sky.flash(x - camera.eye[0], y - camera.eye[1], z - camera.eye[2],
                Math.min(1, cd / 2e8), wetter.sky.time(), 0.5);
        meadow = new Meadow(Biome.sylt(), new Bodendecke(terrain, karte, kliff::talus));
        meadow.near = 6.5f;
        meadow.seed = 2026;
        wetter.sky.latDeg = Site.LAT;
        wetter.sky.lonDeg = Site.LON;
        wetter.sky.epochMillis = clock.epochMillis();
        // das Rauschen der Wolken (8 MB, 0,5 bis 3 s) schon jetzt im Hintergrund rechnen (aus Rungholt)
        Thread nt = new Thread(() -> { com.dan.cloud.Noise.base(); com.dan.cloud.Noise.detail(); com.dan.cloud.Noise.weather(); }, "wolken-rauschen");
        nt.setDaemon(true);
        nt.start();
        setQuality(quality);
        camera.waterLevel = wiring.pegel;
        camera.groundLevel = groundHeight(camera.eye[0], camera.eye[2]);
        camera.targetGround = groundHeight(camera.tx, camera.tz);
        camera.snap();
    }

    /** Seit Phase 7a, für Standbilder: Tide, Wellenfeld mit Textur und das Netz nahe am Auge jetzt (im aufrufenden Faden). */
    public void brandungJetzt() {
        wiring.stepTide();
        brandung.computeNow(clock.t());
        nah.an = showNetz && showMeer;
        if (nah.an) nah.jetzt(camera.tx, camera.tz, wiring.pegel);
    }

    /** Mitten der Häuser im Umkreis r um (x, z) (für die Weide der Galloways). */
    double[][] haeuserNahe(double x, double z, double r) {
        java.util.List<double[]> l = new java.util.ArrayList<>();
        for (com.dan.sylt.scene.Bauten.Haus h : bauwerke.bauten.haeuser)
            if (Math.hypot(h.x() - x, h.z() - z) < r) l.add(new double[]{h.x(), h.z()});
        return l.toArray(new double[0][]);
    }

    /** Seit Phase 10, für Standbilder: Strandkörbe, Herde und die Schalter der Leuchtfeuer sofort nach Licht und Wetter. */
    public void bautenJetzt() {
        double[] sp = clock.sun();
        koerbe.stellen(clock.date(), wiring.windFromDeg, wiring.windSpeed, sp[1]);
        koerbeNeu = true;
        umgebungLux = umgebungLux();
        feuer.schritt(10, umgebungLux, wiring.visibilityKm, 0);
        feuer.sofort(umgebungLux);
    }

    /** Licht am Kampener Turm in lx (für den Dämmerungsschalter): aus com.dan.sky, sonst aus der Sonnenhöhe. */
    double umgebungLux() {
        double[] sp = clock.sun();
        if (wetter.skyOk) {
            Sky.State st = wetter.state();
            return com.dan.sylt.scene.Leuchtfeuer.lux(st.sunColor, st.sunDir[1], st.skyIrradiance) * (wetter.cloudsOk && showClouds ? 0.5 + 0.5 * cloudShade : 1);
        }
        return 30_000 * Sun.skyBrightness(sp[0]);
    }

    /** Für Prüfungen und Werkzeuge: Uhr am Start, Gelände aus daten/gelaende (sonst die Bühne). */
    public Renderer() { this(new SceneClock()); }

    public void setStatusListener(Consumer<String> l) { statusListener = l; }
    public void setErrorListener(Consumer<String> l) { errorListener = l; }
    public String glInfo() { return glInfo; }
    public String error() { return error; }
    public double fps() { return fps; }
    public double gpuMs() { return gpuMs; }
    public double cpuMs() { return cpuMs; }
    public boolean reversedDepth() { return reversedZ; }
    public ComputeProbe.Result compute() { return compute; }
    public Quality quality() { return quality; }
    public void requestShaderReload() { reloadShaders = true; }
    public double[] lastPassTimes() { return passMs.clone(); }
    public long frames() { return frame; }
    public double takeWorstFrameMs() { double w = worstMs; worstMs = 0; return w; }
    /** Öffnung der Kamera senkrecht (Grad); seit Phase 10 je Ansicht (Fernrohr für Kampen1–3). */
    public double fovy() { return camera.fovy; }

    /** Alle Shader-Programme mit ihren Dateien (für die Prüfung). */
    public String[][] shaderFiles() {
        String[][] f = new String[all.length][];
        for (int i = 0; i < f.length; i++) f[i] = all[i].files();
        return f;
    }

    public void setQuality(Quality q) {
        quality = q;
        renderScale = q.renderScale;
        lod.lodRange = q.terrainRange;
        lod.finest = q.terrainFine;
        lod.finestKern = q.terrainKern;
        wiring.surf.quality = com.dan.surf.Surf.Quality.values()[q.ordinal()];
        wiring.dune.quality = com.dan.dune.Dune.Quality.values()[q.ordinal()];
        meer.ocean.quality = q.ocean;
        nah.reihen = q == Quality.NIEDRIG ? 72 : q == Quality.MITTEL ? 120 : 168;   // seit Phase 19c auf 640 m (vorher 24/40/56 auf 160 m)
        sand.dune.quality = q == Quality.NIEDRIG ? com.dan.dune.Dune.Quality.LOW : q == Quality.MITTEL ? com.dan.dune.Dune.Quality.MEDIUM
                : q == Quality.HOCH ? com.dan.dune.Dune.Quality.HIGH : com.dan.dune.Dune.Quality.ULTRA;
        sand.koerner().budget = sand.dune.grainBudget();
        wetter.clouds.quality = q.clouds;
        if (meadow != null) meadow.radius = q.groundRadius;
        // seit Phase 13: Regen, Nebel, Blitze und Tiere nach der Stufe
        unwetter.rain.quality = com.dan.rain.Rain.Quality.values()[q.ordinal()];
        unwetter.fog.quality = com.dan.fog.Fog.Quality.values()[q.ordinal()];
        unwetter.blitz.quality = com.dan.lightning.Lightning.Quality.values()[q.ordinal()];
        if (leben != null) leben.stufe(q.ordinal());
    }

    /** Höhe des Grunds am Ort: das Gelände (ohne daten/gelaende die Bühne). */
    public double groundHeight(double x, double z) { return terrain.height(x, z); }

    /** Stellt die Kamera auf eine Ansicht nach Namen. */
    public boolean placeView(Ansichten.Ansicht a) {
        camera.waterLevel = wiring.pegel;
        boolean ok = Ansichten.apply(a, camera, PHASE, this::groundHeight);
        if (ok) hudMeldung("Ansicht " + a);
        return ok;
    }

    /**
     * Seit Phase 10: eine Vorlage der Türme (Leuchturm1–4, Kampen1–4) mit Tag, Stunde, Bedeckung und dem Quermarkenfeuer
     * (wie früher, wenn die Vorlage es zeigt); danach Augenanpassung neu.
     */
    public void placeVorlage(Ansichten.Vorlage v) {
        clock.set(v.datum(), v.uhr());
        // seit Phase 13: das Wetter der Vorlage als Lage (bedeckt mit Schauern über der See für Leuchturm4, sonst heiter)
        if (!Double.isNaN(v.bedeckung())) {
            if (v.bedeckung() >= 7.5) wiring.regie.lage(com.dan.sylt.wiring.Wetterregie.Lage.SCHAUER, 7, com.dan.cloud.CloudType.STRATOCUMULUS);
            else wiring.regie.lage(com.dan.sylt.wiring.Wetterregie.Lage.SOMMERABEND, v.bedeckung(), com.dan.cloud.CloudType.CUMULUS);
        }
        feuer.quermarkeFrueher = v.frueher();
        placeView(v.ansicht());
        wetter.sky.epochMillis = clock.epochMillis();
        readapt();
        hudMeldung("Vorlage " + v.name() + " (" + v.bild() + ")");
    }

    /**
     * Seit Phase 9a: „Sandsturm am Strand“ – Wind Bft 9 aus 280°, trocken (kein Regen), mäßige See (der obere Strand bleibt
     * trocken), Flugsand und Körner an, Blick vom Strand längs nach Nordnordost.
     */
    public void sandsturm() {
        wiring.regie.hand();   // seit Phase 13: von Hand (die Regie hielte sonst ihre Lage)
        wiring.windSpeed = 22;
        wiring.windFromDeg = 280;
        wiring.surf.hs = 1.4;
        wiring.surf.tp = 7;
        wiring.surf.seaFromDeg = 285;
        sand.regen = 0;
        sand.an = true;
        showKoerner = true;
        // seit Phase 10: auf trockenem Sand stehen – bei Hochwasser (Pegel +0,7 m) war die Stelle 22 m vor der Wasserlinie nass
        Ansichten.Ansicht a = Ansichten.SANDSTURM;
        double n = Site.N0 - a.z(), wl = Site.wasserlinieE(n), trocken = wiring.pegel + Math.max(0.8, brandung.auflaufR2() * 0.8) + 0.3;
        int row = kliff.kante != null ? kliff.kante.row(a.z()) : -1;
        double s0 = a.x() + Site.E0 - wl, sMax = row >= 0 ? kliff.kante.fussE[row] - wl - 4 : s0 + 12, sBest = Math.max(s0, sMax);
        for (double sq = s0; sq <= sMax; sq += 1) {   // höchstens bis 4 m vor den Fuß des Kliffs
            double[] xz = Site.fromUtm(wl + sq, n);
            if (terrain.height(xz[0], xz[1]) >= trocken) { sBest = Math.min(sMax, sq + 3); break; }
        }
        double[] xz = Site.fromUtm(wl + sBest, n);
        placeView(new Ansichten.Ansicht(a.key(), a.name(), xz[0], xz[1], a.heading(), a.pitch(), a.eyeAboveGround(), a.phase(), a.fovy()));
        hudMeldung(String.format(Locale.GERMAN, "Sandsturm am Strand: Bft 9 aus West, trockener Sand (%.0f m vor der Wasserlinie)", sBest));
    }

    /**
     * Seit Phase 12: die Kehle wächst mit der Szenenzeit (Stunden seit dem letzten Bild), Pegel und Auflauf R2 % der
     * Brandung; ein fertiger Abbruch legt Wandkarte und Grasnarbe an die neue Kante.
     */
    void abbruchSchritt() {
        if (abbruch == null) return;
        double ms = clock.epochMillis(), dh = Double.isNaN(abbruchMs) ? 0 : (ms - abbruchMs) / 3_600_000.0;
        abbruchMs = ms;
        if (dh < 0 || dh > 24) dh = 0;   // die Uhr wurde gestellt
        abbruch.uhrMs = ms;
        abbruch.flut(dh, wiring.pegel, brandung.auflaufR2());
        if (kliffstandNeu) {
            // seit Phase 16: nach „Weiter wie zuletzt“ Wandkarte und Grasnarbe an den Kanten der früheren Abbrüche
            kliffstandNeu = false;
            for (double[] b : abbruch.bereiche) kliff.erneuern(b[0], b[1], b[2], b[3], terrain::height);
            fertigGesehen = abbruch.fertige().size();
        }
        java.util.List<com.dan.sylt.scene.Kliffabbruch.Schnitt> f = abbruch.fertige();
        for (; fertigGesehen < f.size(); fertigGesehen++) {
            com.dan.sylt.scene.Kliffabbruch.Schnitt s = f.get(fertigGesehen);
            if (s.gebrochen()) kliff.erneuern(s.x0, s.z0, s.x1, s.z1, terrain::height);
        }
    }

    private volatile boolean kliffstandNeu;

    /**
     * Seit Phase 16 („Weiter wie zuletzt“, F42 A): den Stand des Kliffs (Abbrüche, Kegel, Kehle, Kante) und der Vorspülung
     * herstellen. Gearbeitet wird im Faden der Physik; danach legt der Bildlauf Wandkarte und Grasnarbe neu. {@code fertig}
     * bekommt, ob es ging (im Faden der Physik).
     */
    public void kliffstandSetzen(String kliffText, String vorspuelungText, java.util.function.Consumer<Boolean> fertig) {
        if (abbruch == null || kliffText == null) { if (fertig != null) fertig.accept(false); return; }
        abbruch.imFaden(() -> {
            boolean ok = abbruch.standSetzen(kliffText);
            if (ok && vorspuelung != null) ok = vorspuelung.setzen(vorspuelungText);
            abbruchMs = Double.NaN;
            kliffstandNeu = true;
            if (fertig != null) fertig.accept(ok);
        });
    }

    /**
     * Seit Phase 13: das Fernrohr auf die Sonne an der Kimm (für das grüne Leuchten): von der Kliffkante beim
     * Quermarkenfeuer, Öffnung 1,2°, Blick auf die Sonne (unter der Kimm: auf die Stelle, wo sie unterging).
     */
    public void fernrohrZurSonne() {
        double[] sp = clock.sun();
        double x = Site.KLIFFKANTE.x(), z = Site.KLIFFKANTE.z(), y = groundHeight(x, z) + 1.7;
        camera.place(x, y, z, sp[1], -Math.max(0.1, sp[0]));
        camera.fovy = 1.2;
        hudMeldung(String.format(Locale.GERMAN, "Fernrohr auf die Sonne (Öffnung 1,2°): Sonne %.2f° hoch aus %03.0f°", sp[0], sp[1]));
    }

    /** Seit Phase 12 (F16 A): die Sturmflut nach Xaver beginnt jetzt (5. Dezember 2013, {@code stunde} Uhr). */
    public void sturmflutStarten(double stunde) {
        sturmflut.starten(clock, wiring, stunde);
        abbruchMs = clock.epochMillis();
        hudMeldung("Sturmflut nach Xaver (5. Dezember 2013): Orkan aus Nordwest, Windstau über 3 m in der Nacht");
    }

    /** Seit Phase 12 (F17 A): Abbruch am Ziel der Kamera auslösen (die Kehle dort kritisch machen). */
    public void abbruchHier() {
        if (abbruch == null) { hudMeldung("Abbruch: keine Kante geladen"); return; }
        double x = camera.tx, z = camera.tz;
        abbruch.imFaden(() -> {
            int r = abbruch.ausloesen(x, z);
            hudMeldung(r < 0 ? "Abbruch: hier ist keine Wand über 6 m (Ziel auf das Kliff richten)"
                    : String.format(Locale.GERMAN, "Abbruch bei N %,.0f ausgelöst: Kehle %.1f m", abbruch.kante.nord[r], abbruch.kehle[r]));
        });
    }

    /**
     * Seit Phase 12, für Bildfolgen und Prüfungen: die Uhr der Szene in Schritten von {@code schrittMin} Minuten bis
     * {@code bisStunde} Stunden nach Beginn der Sturmflut vorstellen (Fahrplan, Tide mit Windstau, Hohlkehle, Abtrag).
     * Mit {@code bisAbbruch} hält es an, sobald ein Stück Kliff gerechnet wird; gibt zurück, ob eines gerechnet wird.
     */
    public boolean vorspulen(double bisStunde, double schrittMin, boolean bisAbbruch) {
        if (Double.isNaN(abbruchMs)) abbruchMs = clock.epochMillis();
        while (sturmflut.stunden(clock) < bisStunde) {
            if (bisAbbruch && abbruch != null && abbruch.schnitt() != null) return true;
            clock.setEpochMillis(clock.epochMillis() + schrittMin * 60_000);
            sturmflut.schritt(clock, wiring);
            wiring.stepTide();
            abbruchSchritt();
        }
        return abbruch != null && abbruch.schnitt() != null;
    }

    /**
     * Seit Phase 12, für die Bildfolge: Xaver von Beginn bis {@code stunde} im Zeitraffer, die Kehle wächst überall, ohne
     * dass schon gerechnet wird; dann wird das Stück am Nordwert {@code nord} gerechnet (die Kehle dort, wie die Flut sie
     * geschlagen hat, mindestens 1,25 · L_c). Gibt das Stück zurück (null: dort ist keine Wand).
     */
    public com.dan.sylt.scene.Kliffabbruch.Schnitt abbruchVorbereiten(double stunde, double nord) {
        if (abbruch == null) return null;
        sturmflutStarten(com.dan.sylt.scene.Sturmflut.STUNDE);
        abbruch.rechnen = false;
        vorspulen(stunde, 5, false);
        abbruch.rechnen = true;
        double[] xz = Site.fromUtm(Site.E0, nord);
        abbruch.imFaden(() -> abbruch.ausloesen(0, xz[1]));
        return abbruch.schnitt();
    }

    /**
     * Seit Phase 13, für Standbilder: regnet es am Auge, sind Sand und Kliff schon nass (sonst flöge im Regen noch Sand,
     * denn die Uhr steht); Regen und Nebel einmal rechnen.
     */
    public void wetterJetzt() {
        unwetter.schritt(1 / 60.0, wiring, wetter.clouds, wetter.cloudsOk && showClouds, camera.eye, terrain::height);
        double hier = unwetter.regnet() ? Math.max(unwetter.rain.snapshot().rateAt(camera.eye[0], camera.eye[2]), wiring.regen / 3) : 0;
        if (hier > 0.2) { sandNass = 1; kliffNass = 1; sand.regen = 0.06; }
    }

    /** Seit Phase 9a: Windfeld und Flugsand um das Auge sofort, Körner 3 s vorlaufen (für Standbilder). */
    public void sandJetzt() {
        if (naesse != null) naesse.update(wiring.pegel, brandung.auflaufR2() * 0.8);
        sand.bereit(camera.eye, wiring.windSpeed, wiring.windFromDeg, 3);
    }

    /** Eine Meldung für 5 s im HUD (und in der Statuszeile). */
    public void hudMeldung(String text) {
        meldung = text;
        meldungUntil = System.currentTimeMillis() + 5000;
    }

    /**
     * Belichtung: seit Phase 3 aus der Augenanpassung an Himmel und Bild ({@code com.dan.sky}, gemessen mit
     * {@link Belichtung}, aus Brocken), ohne Himmel aus der Sonnenhöhe des Platzhalters; dazu die Blenden von Hand.
     */
    public double exposure() {
        if (wetter.skyOk && adaptLum > 0) return Sky.State.exposureFor(adaptLum, adaptation) * Math.pow(2, exposureBias);
        double b = Sun.skyBrightness(clock.sun()[0]);
        return 0.6 * Math.pow(b, -0.85) * Math.pow(2, exposureBias);
    }

    /** Augenanpassung neu (nach Sprüngen der Uhr oder der Kamera). */
    public void readapt() {
        viewLum = -1;
        adaptLum = -1;
        belichtung.reset();
        if (wetter.skyOk) adapt(wetter.state(), 1);
    }

    /** Wolkenschatten am Auge (1 = Sonne). */
    public double cloudShade() { return cloudShade; }
    public boolean skyRunning() { return wetter.skyOk; }
    public boolean cloudsRunning() { return wetter.cloudsOk; }
    public boolean oceanRunning() { return meer.ok; }

    private void adapt(Sky.State st, double dt) {
        viewTimer -= dt;
        if (viewLum <= 0 || viewTimer <= 0) {
            viewLum = st.viewLuminance(camera.fwd.clone(), new double[]{0, 1, 0}, fovy(), surfW / (double) Math.max(1, surfH));
            viewTimer = 0.2;
        }
        double gemessen = bildMessung ? belichtung.lum() : -1;
        double target = gemessen > 0
                ? Math.pow(Math.max(1e-14, st.adaptation), 1 - BILD_FOLGT) * Math.pow(Math.max(1e-14, gemessen * BILD_GAIN), BILD_FOLGT)
                : Math.sqrt(Math.max(1e-14, st.adaptation) * Math.max(1e-14, viewLum))
                * (wetter.cloudsOk && showClouds ? Math.pow(wetter.clouds.brightnessFactor(), STORM_ADAPT) : 1);
        if (adaptLum <= 0 || frozen || dt > 0.5) adaptLum = target;
        else adaptLum = Math.exp(Math.log(adaptLum) + (Math.log(target) - Math.log(adaptLum)) * Math.min(1, dt / 1.0));
    }

    /** Kinomodus verlassen (Esc im Kino); das Regiebuch läuft im Fenster weiter. */
    public void kinoVerlassen() { frozen = false; kino.abbrechen(); }

    /** Eine Arbeit im Faden des Bildlaufs (vor dem nächsten Schritt). */
    public void imBild(Runnable r) { imBild.add(r); }

    // ------------------------------------------------------------------ GL

    @Override
    public void init(GLAutoDrawable d) {
        initGl(d.getGL().getGL4(), d.getContext().getGLVersionNumber().getMajor(), d.getContext().getGLVersionNumber().getMinor(),
                d.getContext().getDefaultDrawFramebuffer());
    }

    /** Start auf einer Grafikkarte ohne Fenster-Hülle (für {@link #init} und Prüfstände): Version, Bildpuffer des Ziels. */
    public void initGl(GL4 gl, int major, int minor, int targetFbo) {
        String renderer = gl.glGetString(GL.GL_RENDERER), vendor = gl.glGetString(GL.GL_VENDOR), version = gl.glGetString(GL.GL_VERSION);
        glInfo = renderer + " · OpenGL " + major + "." + minor;
        System.out.println("Sylt: " + vendor + " · " + renderer + " · " + version);
        if (major < MIN_MAJOR || (major == MIN_MAJOR && minor < MIN_MINOR)) {
            fail("Die Grafikkarte meldet OpenGL " + major + "." + minor + " (" + renderer + ", Treiber " + version
                    + "). Sylt braucht OpenGL " + MIN_MAJOR + "." + MIN_MINOR + ".\nBitte den Grafiktreiber aktualisieren.");
            return;
        }
        gl.setSwapInterval(1);
        defaultFbo = targetFbo;
        reversedZ = (major > 4 || minor >= 5) || gl.isExtensionAvailable("GL_ARB_clip_control");
        if (reversedZ) gl.glClipControl(GL4.GL_LOWER_LEFT, GL4.GL_ZERO_TO_ONE);
        try {
            for (Shader s : all) s.build(gl);
        } catch (RuntimeException e) {
            fail(e.getMessage());
            return;
        }
        int[] v = new int[1];
        gl.glGenVertexArrays(1, v, 0);
        emptyVao = v[0];
        for (int[] s : stamp) gl.glGenQueries(MARKS, s, 0);
        compute = ComputeProbe.run(gl);
        System.out.println("Sylt: " + compute.line());
        lod.upload(gl, terrain);
        System.out.println("Sylt: Gelände " + terrain.describe());
        // Phase 3: Himmel und Wolken, See, Arten zu 1 m, Bodendecke, Belichtung (jedes für sich; was nicht startet, fehlt)
        wetter.init(gl, errorListener);
        wiring.cloudsDrawn = wetter.cloudsOk;
        meer.init(gl, terrain, compute.ok(), errorListener);
        brandungGl.init(gl, brandung);
        sandGl.init(gl);
        bautenGl.init(gl);
        bautenGl.upload(gl, BautenGl.FEST, bauwerke.netz);
        feuerGl.init(gl);
        System.out.println("Sylt: Bauten " + bauwerke.describe() + " · " + bautenGl.info);
        unwetter.init(gl, errorListener);   // seit Phase 13
        lebenGl.init(gl, errorListener);
        klasseTex = klasseTexture(gl, karte);
        kliff.init(gl);
        System.out.println("Sylt: " + karte.describe() + (karte.hatKarte() ? " · Arten im Kern zu 1 m" : " · ohne Karte des Kerns"));
        try {
            groundGl.init(gl);
            groundOk = true;
        } catch (RuntimeException e) {
            System.err.println("Sylt: Bodendecke nicht verfügbar: " + e.getMessage());
        }
        belichtung.init(gl);
        try {   // seit Phase 15: der Kinomodus
            com.dan.kino.KinoMaler km = new com.dan.kino.KinoMaler();
            km.kopfFarbe = new java.awt.Color(226, 180, 160);
            kinoGl = new com.dan.kino.gl.KinoGl(kino, km);
            kinoGl.init(gl);
        } catch (RuntimeException e) {
            System.err.println("Sylt: Kino nicht verfügbar (ohne Balken und Blenden): " + e.getMessage());
            kinoGl = null;
        }
        physik.start();
        lastNanos = System.nanoTime();
        statsNanos = lastNanos;
        camera.snap();
        ready = true;
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
        surfW = Math.max(1, d.getSurfaceWidth());
        surfH = Math.max(1, d.getSurfaceHeight());
    }

    /** Uhr, Physik und Kamera um dt weiterrechnen (vom Bildlauf aufgerufen; Standbilder rufen es mit festem dt). */
    public void step(double dt) {
        for (Runnable r; (r = imBild.poll()) != null; ) {
            try { r.run(); } catch (RuntimeException e) { System.err.println("Sylt: Arbeit im Bildlauf: " + e); }
        }
        clock.step(dt);
        regie.step(dt, regieCtx);   // seit Phase 15: fällige Stichworte des Regiebuchs
        kinoSchritt(dt);
        physik.advance(dt);
        camera.groundLevel = groundHeight(camera.eye[0], camera.eye[2]);
        camera.waterLevel = wiring.pegel;
        camera.targetGround = Math.max(groundHeight(camera.tx, camera.tz), wiring.pegel);
        camera.step(dt);
        // Phase 3: Himmel, Wolken und See bekommen ihre Eingänge aus der Verdrahtung, dann ein Schritt
        boolean cloudsShown = wetter.cloudsOk && showClouds;
        wiring.cloudsDrawn = cloudsShown;
        // seit Phase 13: die Wetterregie (Lage, freie Regie, Regiebuch); während der Sturmflut führt deren Fahrplan
        if (!sturmflut.aktiv) wiring.regie.schritt(dt);
        sturmflut.schritt(clock, wiring);
        wiring.stepTide();
        abbruchSchritt();
        phase15Schritt(dt);
        wiring.feed(wetter.sky, camera.eye[1]);
        wiring.feed(wetter.clouds);
        wiring.feed(meer.ocean);
        meer.ocean.step(dt);
        // Phase 7a: die Brandung (Feld im eigenen Faden), das Netz nahe am Auge, der Sand nach Auflauf und Schwall
        brandung.update(clock.t());
        double[] sp7 = clock.sun();
        nah.an = showNetz && showMeer;
        nah.wetter = new com.dan.surf.Swash.Weather(Math.max(0, Math.sin(Math.toRadians(sp7[0]))) * (1 - wiring.oktas / 8 * 0.8), wiring.windSpeed, 14, 0);
        nah.zoom = camera.fovy / com.dan.sylt.camera.Camera.FOVY;
        nah.schritt(dt, camera.eye, camera.tx, camera.tz, wiring.pegel);
        // seit Phase 8: die Brandungsringe nur um die Buhnen nahe am Auge (die Reihen liegen über 4 km verteilt)
        meer.ocean.obstacleField = brandung.buhnen.hindernisseNahe(camera.eye[0], camera.eye[2], 900);
        // seit Phase 13: Regen macht Sand und Kliff nass (in Szenenzeit: nass in rund 10 min, trocken in rund 3 h) [G]
        // (in einer Schauerlage zieht immer wieder einer durch: ein Drittel der Rate zählt auch zwischen den Vorhängen)
        double sceneDt = dt * Math.max(0, clock.lapse), regenHier = unwetter.regenFaellt()
                ? Math.max(unwetter.rain.snapshot().rateAt(camera.eye[0], camera.eye[2]), wiring.regen / 3) : 0;
        if (regenHier > 0.2) { sandNass += (1 - sandNass) * Math.min(1, Math.max(dt, sceneDt) / 300); kliffNass += (1 - kliffNass) * Math.min(1, Math.max(dt, sceneDt) / 400); }
        else { sandNass -= sandNass * Math.min(1, sceneDt / (3 * 3600)); kliffNass -= kliffNass * Math.min(1, sceneDt / (5 * 3600)); }
        if (sandNass > 0.01 || wiring.regie.modus() != com.dan.sylt.wiring.Wetterregie.Modus.HAND) sand.regen = 0.06 * sandNass;
        if (naesse != null) {
            boolean neu = naesse.update(wiring.pegel, brandung.auflaufR2() * 0.8, sandNass);
            naesseTakt += dt;
            if (neu || naesseTakt > 0.25) {
                naesseTakt = 0;
                naesse.ueberlagern(nah.sichtbar ? nah.stueck() : null);
            }
        }
        // seit Phase 9a: Flugsand (Windfeld und Feld um das Auge im Hintergrund, Körner hier)
        sand.schritt(dt, camera.eye, wiring.windSpeed, wiring.windFromDeg);
        // seit Phase 10: Galloways, Strandkörbe (nur bei Änderung neu), Leuchtfeuer mit Dämmerungsschalter
        herde.schritt(dt);
        if (koerbe.stellen(clock.date(), wiring.windFromDeg, wiring.windSpeed, sp7[1])) koerbeNeu = true;
        umgebungLux = umgebungLux();
        // seit Phase 13: Regen und Seenebel streuen das Licht der Feuer (Nebel zieht: alle 3 s neu gefüllt)
        nebelTakt -= dt;
        if (unwetter.nebelt() && nebelTakt <= 0) { nebelTakt = 3; nebelStand++; }
        double regenTurm = unwetter.regnet() ? unwetter.rain.snapshot().rateAt(feuer.kampen.x, feuer.kampen.z) : 0;
        feuer.schritt(dt, umgebungLux, wiring.visibilityKm, regenTurm, unwetter.nebelt() ? unwetter.fog::scatter : null, unwetter.nebelt() ? nebelStand : 0);
        // seit Phase 13: Regen, Seenebel, Gewitter; das Leben am Strand
        unwetter.schritt(dt, wiring, wetter.clouds, cloudsShown, camera.eye, terrain::height);
        if (showLeben) leben.schritt(dt, camera.eye, camera.tx, camera.tz, wiring.windSpeed, wiring.windFromDeg, umgebungLux, sp7[0], clock.date(), clock.hour(),
                wiring.pegel, nah.sichtbar ? nah.stueck() : null, wiring.surf.hs);
        wetter.sky.greenFlash = gruenesLeuchten ? (Double.isNaN(gruenFest) ? gruenesLeuchtenHeute() : gruenFest) : 0;
        // seit Phase 14: der Klang hört an der Kamera zu
        klang.feed(dt);
        if (wetter.skyOk) {
            Sky.State st = wetter.sky.step(dt);
            wetter.clouds.step(dt);
            cloudShade = cloudsShown ? Clouds.shadow(wetter.clouds.snapshot(), camera.eye[0], camera.eye[2], Wetter.keyDir(st)) : 1;
            adapt(st, dt);
        }
    }

    /**
     * Seit Phase 13: das grüne Leuchten. Die Brechung trennt die Farben immer (1); an einem Abend mit Luftspiegelung über
     * dem Wasser (warmes Wasser unter kühlerer, klarer Luft) wird der grüne Rand zum Blitz von einigen Sekunden (3). Wann
     * das ist, würfelt der Tag [G: an rund jedem vierten klaren Abend von Juni bis Oktober]; bei Dunst, Wolken an der Kimm
     * oder Regen nur die reine Brechung.
     */
    public double gruenesLeuchtenHeute() {
        java.time.LocalDate d = clock.date();
        long h = d.toEpochDay() * 0x9E3779B97F4A7C15L ^ 0x5EEDL;
        h ^= h >>> 29;
        boolean warmesWasser = d.getMonthValue() >= 6 && d.getMonthValue() <= 10;
        boolean klar = wiring.visibilityKm > 25 && wiring.oktas < 5 && wiring.regen < 0.05 && wiring.nebel < 0.05;
        return klar && warmesWasser && Math.floorMod(h, 4) == 0 ? 3 : 1;
    }

    /** Seit Phase 13: Meeresleuchten jetzt (Blüte nach Monat und Wasser, nur wenn es dunkel ist), cd/m² je Kanal für frisch aufgewühltes Wasser. */
    public double[] meeresleuchtenJetzt() {
        if (!meeresleuchten) return new double[]{0, 0, 0};
        int m = clock.date().getMonthValue();
        double b = Double.isNaN(bluete) ? com.dan.surf.Bioluminescence.bloom(m, com.dan.surf.Bioluminescence.northSeaTemperature(m)) : bluete;
        // am Tag sieht man es nicht (es leuchtet trotzdem): ab bürgerlicher Dämmerung einblenden
        double dunkel = smooth(-2, -8, clock.sun()[0]);
        return com.dan.surf.Bioluminescence.luminance(b * dunkel, 1, leuchtenGain);
    }

    @Override
    public void display(GLAutoDrawable d) {
        displayGl(d.getGL().getGL4(), d.getSurfaceWidth(), d.getSurfaceHeight(), d.getContext().getDefaultDrawFramebuffer());
    }

    /** Ein Bild in das Ziel targetFbo der Größe w × h (für {@link #display} und Prüfstände). */
    public void displayGl(GL4 gl, int width, int height, int targetFbo) {
        if (!ready) {
            gl.glClearColor(0.06f, 0.075f, 0.08f, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            return;
        }
        if (reloadShaders) {
            reloadShaders = false;
            try {
                for (Shader s : all) s.build(gl);
                hudMeldung("Shader neu geladen");
                System.out.println("Shader neu geladen.");
            } catch (RuntimeException e) {
                System.err.println(e.getMessage());
                errorListener.accept(e.getMessage());
            }
        }
        defaultFbo = targetFbo;
        readMarks(gl);
        long now = System.nanoTime();
        double rawMs = (now - lastNanos) / 1e6;
        if (frame > 2 && rawMs > worstMs) worstMs = rawMs;
        double dt = Math.min(0.1, (now - lastNanos) / 1e9);
        lastNanos = now;
        if (!frozen) step(dt);

        surfW = Math.max(1, width);
        surfH = Math.max(1, height);
        regulateScale(now);
        int w = Math.max(64, (int) Math.round(surfW * renderScale)), h = Math.max(64, (int) Math.round(surfH * renderScale));
        if (w != hdrW || h != hdrH) resizeTargets(gl, w, h);
        renderFrame(gl, w, h);
        regieBild(gl);       // seit Phase 15: Fotos des Regiebuchs (ohne HUD und Balken) und das Blatt am Ende
        kinoBild(gl, now);   // Kino über das fertige Bild; F12 und Tempo mit
        if (photoRequest) { photoRequest = false; takePhoto(gl, surfW, surfH); }
        Consumer<BufferedImage> gr = grabRequest;
        if (gr != null) { grabRequest = null; try { gr.accept(readDefault(gl, surfW, surfH)); } catch (RuntimeException e) { System.err.println("Sylt: Bild nicht abgeholt: " + e); } }
        cpuMs = cpuMs * 0.95 + (System.nanoTime() - now) / 1e6 * 0.05;
        frame++;
        stats(now, w, h);
    }

    void renderFrame(GL4 gl, int w, int h) {
        mark(gl, 0);
        frameSky = wetter.skyOk;
        Sky.State st = frameSky ? wetter.state() : null;
        if (frameSky && adaptLum <= 0) adapt(st, 1);
        double pre = frameSky ? exposure() : 1;
        double[] sp = clock.sun();
        double[] sun = frameSky ? st.sunDir : Sun.direction(sp[0], sp[1]);
        float[] proj = Mat4.perspective(fovy(), w / (double) h, NEAR, FAR, reversedZ);
        float[] vp = Mat4.mul(proj, camera.view());
        float[] inv = Mat4.invert(vp);
        boolean cloudsNow = frameSky && wetter.cloudsOk && showClouds;
        Clouds.Snapshot cs = wetter.clouds.snapshot();
        CloudGl.Light cl = null;
        // seit Phase 13: Ringe des Regens rechnen, Blitze hochladen, das Blitzlicht am Auge für Szene, Wolken und See
        unwetter.hochladen(gl, camera.eye);
        wetter.blitz = unwetter.blitzE.clone();
        double[] glow = meeresleuchtenJetzt();
        for (int k = 0; k < 3; k++) glow[k] *= (frameSky ? pre : exposure()) / Sky.CD_PER_UNIT;

        // 1 Himmel: com.dan.sky ins Bild, dann das Rundumbild mit Wolken und die Wolkenschatten (aus Brocken);
        //   ohne com.dan.sky der Verlauf aus Phase 1
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glViewport(0, 0, w, h);
        gl.glClearDepth(reversedZ ? 0 : 1);
        gl.glClearColor(0, 0, 0, 1);
        gl.glDepthMask(true);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_BLEND);
        gl.glBindVertexArray(emptyVao);
        if (frameSky) {
            wetter.skyGl.update(gl, st);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            SkyGl.View sv = new SkyGl.View();
            sv.viewProj = vp;
            sv.invViewProj = inv;
            sv.reversedZ = reversedZ;
            sv.width = w;
            sv.height = h;
            sv.fovyDeg = fovy();
            sv.preExposure = pre;
            sv.time = clock.t();
            wetter.skyGl.draw(gl, st, sv);
            if (wetter.skyCheck.isEmpty() && frame > 30) wetter.skyCheck = wetter.skyGl.selfCheck(gl, st);
            gl.glBindVertexArray(emptyVao);
            if (cloudsNow) {
                cl = wetter.cloudLight(st, pre);
                unwetter.wolken(cl, pre);
                wetter.cloudGl.update(gl, cs, cl, camera.eye.clone());
                if (wetter.cloudCheck.isEmpty() && frame > 40) wetter.cloudCheck = wetter.cloudGl.selfCheck(gl, cs, cl);
            }
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, wetter.panoFbo);
            gl.glViewport(0, 0, Wetter.PANO_W, Wetter.PANO_H);
            wetter.skyGl.drawPanorama(gl, st, pre);
            if (cloudsNow) wetter.cloudGl.drawPanorama(gl, cs, cl, camera.eye.clone());
            unwetter.rundum(gl, Wetter.PANO_W, Wetter.PANO_H, camera.eye.clone(), pre, wiring.visibilityKm);   // seit Phase 13: Blitze spiegeln
            gl.glBindTexture(GL.GL_TEXTURE_2D, wetter.panoTex);
            gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            gl.glBindVertexArray(emptyVao);
        } else {
            common(gl, skyShader, vp, inv, sun);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
        }
        mark(gl, 1);

        // 2 Gelände: Kern, Hauptraster und Fernring als CDLOD; Arten zu 1 m im Kern (Einheit 12), Wolkenschatten (13),
        //   Nässe des Sands aus mudflat (14, Spuren 15), mit Marken
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
        common(gl, terrainShader, vp, inv, sun);
        lod.bind(gl, terrainShader);
        bindMarks(gl, terrainShader);
        korrektur(gl, terrainShader);
        terrainShader.vec4Array(gl, "uAlb", albedo4());
        gl.glActiveTexture(GL.GL_TEXTURE0 + 12);
        gl.glBindTexture(GL.GL_TEXTURE_2D, klasseTex);
        gl.glActiveTexture(GL.GL_TEXTURE0 + 13);
        gl.glBindTexture(GL.GL_TEXTURE_2D, cloudsNow ? wetter.cloudGl.shadowTexture() : 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        terrainShader.set(gl, "uKlasse", 12);
        terrainShader.set(gl, "uKlasseOn", klasseTex != 0 ? 1 : 0);
        terrainShader.set(gl, "uCloudShadow", 13);
        double[] cr = cloudsNow ? wetter.cloudGl.shadowRect() : new double[]{0, 0, 1};
        terrainShader.set(gl, "uCloudRect", (float) cr[0], (float) cr[1], (float) Math.max(1, cr[2]), cloudsNow ? 1f : 0f);
        if (naesse != null) mudGl.upload(gl, naesse.feld);
        mudGl.time = clock.t();
        mudGl.ripples = 0.7;
        // seit Phase 4a: Nässe auf 4, Spuren auf 5 (15 trägt das Rauschen von cliff), Wandkarte 2, Schichtkarte 3
        mudGl.bind(gl, terrainShader.id(), 4, naesse != null ? naesse.feld : null, naesse != null ? naesse.feld.material : com.dan.mudflat.Mudflat.beach());
        terrainShader.set(gl, "uMudTracks", 5);
        kliff.bind(gl, terrainShader.id());
        terrainShader.set(gl, "uCliffOn", kliff.ok && showCliff ? 1 : 0);
        terrainShader.set(gl, "uCliffPx", (float) (Math.toRadians(fovy()) / Math.max(1, h)));
        terrainShader.set(gl, "uCliffRange", (float) kliff.cliff.quality.reliefRange);
        terrainShader.set(gl, "uCliffRain", (float) kliffNass);   // seit Phase 13: nass nach Regen
        // seit Phase 13: Spuren der Vögel im nassen Sand (Raster um das Auge)
        mudGl.uploadTracks(gl, leben.anSpuren ? leben.spurenKarte : null);
        mudGl.bindTracks(gl, terrainShader.id(), 5);
        lod.draw(gl, terrainShader, camera.eye, vp);
        gl.glBindVertexArray(emptyVao);
        // Grasnarbe und Findlinge (Licht wie das Gelände, vorbelichtet)
        if (kliff.ok && showCliff) {
            CliffGl.View kv = new CliffGl.View();
            kv.viewProj = vp;
            kv.eye = camera.eye.clone();
            kv.reversedZ = reversedZ;
            kv.pixelAngle = Math.toRadians(fovy()) / Math.max(1, h);
            CliffGl.Light kl = new CliffGl.Light();
            kl.sunDir = sun;
            if (frameSky) {
                kl.sunColor = Wetter.scale(st.sunColor, pre);
                kl.ambient = Wetter.scale(wetter.ambient(st, showClouds), pre / Math.PI);
                kl.fogColor = Wetter.scale(st.skyIrradiance, pre / Math.PI);
                kl.fogDistance = Wetter.fogDistance(st);
                kl.shade = cloudShade;
            } else {
                kl.sunColor = sunLight(sp[0]);
                kl.ambient = new double[]{0.25, 0.3, 0.38};
                kl.fogDistance = FOG_DISTANCE;
            }
            kliff.drawExtras(gl, kv, kl);
            if (kliff.check.isEmpty() && frame > 50) {
                kliff.check = kliff.gl.selfCheck(gl, kliff.cliff);
                gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
                gl.glViewport(0, 0, w, h);
            }
            gl.glBindVertexArray(emptyVao);
        }
        // seit Phase 10: Türme und Bauten, Strandkörbe, Galloways (Licht wie das Gelände, vorbelichtet)
        if (showBauten && bautenGl.ok) drawBauten(gl, vp, inv, sun, st, pre, cloudsNow);
        // seit Phase 13: Möwen, Vögel am Spülsaum, Seehund (nach dem Gelände, vor der See)
        if (showLeben && frameSky) {
            double a = Math.toRadians(camera.heading());
            double[][] L = Unwetter.licht(st, pre, wetter.ambient(st, showClouds), cloudShade, new double[]{Math.sin(a), 0.03, -Math.cos(a)}, unwetter.blitzE, unwetter.blitzDir);
            lebenGl.draw(gl, leben, vp, camera.eye, reversedZ, L, Wetter.fogDistance(st), cloudShade, frame);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            gl.glBindVertexArray(emptyVao);
        }
        mark(gl, 2);

        // 3 Boden: die Bodendecke um das Auge (nur zu Fuß, Biom Sylt)
        double[] wv = windVector();
        boolean groundNow = groundOk && showGround && camera.eye[1] - camera.groundLevel < 25;
        if (groundOk) {
            meadow.setSeason(clock.date().getDayOfYear(), 0);
            meadow.wind.speed = (float) wiring.windSpeed;
            meadow.wind.direction = (float) Math.toDegrees(Math.atan2(wv[2], wv[0]));
            groundGl.update(gl, meadow, (float) (clock.t() % 3600), camera.eye[0], camera.eye[2], !groundNow);
        }
        if (groundNow) {
            GroundGl.View gv = new GroundGl.View();
            gv.viewProj = vp;
            gv.eye = camera.eye.clone();
            gv.reversedZ = reversedZ;
            GroundGl.Light gL = new GroundGl.Light();
            gL.sunDir = sun;
            if (frameSky) {
                gL.skyTexture = wetter.panoTex;
                gL.sunColor = Wetter.scale(st.sunColor, pre);
                gL.moonDir = st.moonDir;
                gL.moonColor = Wetter.scale(st.moonColor, pre);
                gL.skyColor = Wetter.scale(wetter.ambient(st, showClouds), pre / Math.PI);
                gL.skyWeight = 1.2;
                gL.fogDistance = Wetter.fogDistance(st);
                if (cloudsNow) {
                    gL.shadowTexture = wetter.cloudGl.shadowTexture();
                    gL.shadowRect = wetter.cloudGl.shadowRect();
                }
            } else {
                gL.sunColor = sunLight(sp[0]);
                gL.fogDistance = FOG_DISTANCE;
            }
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
            groundGl.draw(gl, gv, gL);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            gl.glBindVertexArray(emptyVao);
        }
        mark(gl, 3);

        // 4 Meer: com.dan.ocean mit dem Gelände als Grund (nach dem Gelände, mit Tiefentest); ohne Compute der Platzhalter
        if (showMeer && meer.ok && showOcean) {
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(true);
            brandungGl.upload(gl, brandung);
            brandungGl.stroemung = showStroemung;
            brandungGl.dlrg = dlrgBlick;
            boolean netzNow = showNetz && nah.sichtbar;
            meer.draw(gl, vp, camera.eye, reversedZ, st, pre, frameSky ? wetter.panoTex : 0, cloudsNow ? wetter.cloudGl.shadowTexture() : 0,
                    cloudsNow ? wetter.cloudGl.shadowRect() : new double[]{0, 0, 1}, lod.heightTexture(), sun, sunLight(sp[0]), FOG_DISTANCE,
                    ol -> {
                        brandungGl.feed(ol, brandung, nah, netzNow);
                        unwetter.see(ol, pre);   // seit Phase 13: Ringe des Regens, Blitzlicht
                        ol.glow = glow;          // seit Phase 13: Meeresleuchten
                    });
            brandungGl.glow = glow;
            brandungGl.trackTex = leben.anSpuren ? mudGl.trackTexture() : 0;
            brandungGl.trackRect = mudGl.trackRect();
            // Phase 7a: das Netz der Brecher nahe am Auge und die Pfähle der Buhnen (nach der See, mit Tiefentest)
            brandungGl.draw(gl, vp, camera.eye, reversedZ, nah, netzNow, showBuhnen, st, pre, sun, wetter, showClouds, sunLight(sp[0]), FOG_DISTANCE, clock.t());
            if (meer.check.isEmpty() && frame > 30) meer.check = meer.oceanGl.selfCheck(gl, meer.ocean);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            gl.glBindVertexArray(emptyVao);
        } else if (showMeer) {
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrColorFbo);
            gl.glViewport(0, 0, w, h);
            common(gl, meerShader, vp, inv, sun);
            lod.bind(gl, meerShader);
            korrektur(gl, meerShader);
            gl.glActiveTexture(GL.GL_TEXTURE6);
            gl.glBindTexture(GL.GL_TEXTURE_2D, depthTex);
            gl.glActiveTexture(GL.GL_TEXTURE0);
            meerShader.set(gl, "uDepth", 6);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
            gl.glActiveTexture(GL.GL_TEXTURE6);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            gl.glActiveTexture(GL.GL_TEXTURE0);
        }
        // seit Phase 9a: die Körner des Flugsands (durchscheinend, mit Tiefentest, ohne Tiefe zu schreiben)
        if (showKoerner && sand.an && sand.feld() != null) {
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
            gl.glViewport(0, 0, w, h);
            gl.glEnable(GL.GL_DEPTH_TEST);
            sandGl.draw(gl, vp, camera.eye, sand, st, pre, sun, wetter, showClouds, sunLight(sp[0]), FOG_DISTANCE);
            gl.glDepthMask(true);
            gl.glBindVertexArray(emptyVao);
        }
        mark(gl, 4);

        // 5 Wolken nach der Szene mit ihrer Tiefe (ins Farbziel ohne Tiefe: die Tiefe wird gelesen; aus Brocken)
        gl.glDisable(GL.GL_DEPTH_TEST);
        if (cloudsNow) {
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrColorFbo);
            gl.glViewport(0, 0, w, h);
            CloudGl.View cv = new CloudGl.View();
            cv.viewProj = vp;
            cv.invViewProj = inv;
            cv.eye = camera.eye.clone();
            cv.reversedZ = reversedZ;
            cv.width = w;
            cv.height = h;
            cv.depthTexture = depthTex;
            wetter.cloudGl.draw(gl, cs, cl, cv);
            gl.glBindVertexArray(emptyVao);
        }
        // seit Phase 13: Seenebel (über das Bild mit der Tiefe), Regen (mit einer Kopie der Tiefe), Blitze und ihr Leuchten
        if (frameSky && (unwetter.nebelt() || unwetter.regnet() || unwetter.blitzt())) {
            double a = Math.toRadians(camera.heading());
            double[][] L = Unwetter.licht(st, pre, wetter.ambient(st, showClouds), cloudShade, new double[]{Math.sin(a), 0.03, -Math.cos(a)}, unwetter.blitzE, unwetter.blitzDir);
            if (unwetter.nebelt()) {
                gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrColorFbo);
                gl.glViewport(0, 0, w, h);
                unwetter.zeichneNebel(gl, inv, camera.eye, reversedZ, w, h, depthTex, L, frozen, frame);
            }
            if (unwetter.regnet() || unwetter.blitzt()) {
                int kopie = unwetter.tiefe(gl, hdrFbo, w, h);
                double unter = wetter.cloudsOk && showClouds ? Math.min(1, wetter.clouds.type.overcastBrightness * 1.6) : 1;
                unwetter.zeichneRegen(gl, vp, inv, camera.eye, camera.fwd, reversedZ, fovy(), w, h, kopie, L, unter, Wetter.fogDistance(st));
                unwetter.zeichneBlitze(gl, vp, inv, camera.eye, reversedZ, fovy(), w, h, hdrFbo, kopie, pre, wiring.visibilityKm);
            }
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrColorFbo);
            gl.glViewport(0, 0, w, h);
            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(true);
            gl.glBindVertexArray(emptyVao);
        }
        // seit Phase 10: Strahlen und Lichtpunkte der Leuchtfeuer (additiv, mit der Tiefe der Szene)
        if (feuer.an && feuerGl.ok) {
            feuerGl.draw(gl, feuer, vp, inv, camera.eye, reversedZ, w, h, fovy(), depthTex, hdrColorFbo, frameSky ? pre : exposure(), !frozen);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrColorFbo);
            gl.glViewport(0, 0, w, h);
            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glBindVertexArray(emptyVao);
        }
        unterWasser(gl, w, h, inv, st, pre, sun);   // seit Phase 15: mit der Welle unter Wasser
        // Belichtung im Bild messen (alle 0,2 s, gelesen ohne Warten beim nächsten Mal)
        if (frameSky && bildMessung) {
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrColorFbo);
            belichtung.step(gl, lumShader, hdrTex, emptyVao, pre, frozen);
        }
        mark(gl, 5);

        // 6 Nachbearbeitung ins Fenster (mit com.dan.sky ist das Bild schon vorbelichtet)
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, defaultFbo);
        gl.glViewport(0, 0, surfW, surfH);
        post.use(gl);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, hdrTex);
        post.set(gl, "uHdr", 0);
        post.set(gl, "uExposure", frameSky ? 1f : (float) exposure());
        post.set(gl, "uScotopic", (float) (frameSky ? Wetter.scotopic(st) : 0));
        gl.glBindVertexArray(emptyVao);
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
        mark(gl, 6);

        // 7 HUD (vorher abschicken: die Grafikkarte arbeitet, während das HUD im selben Faden gemalt wird; aus Brocken)
        gl.glFlush();
        if (showHud && !kino.laeuft() && captureCaption == null) {   // seit Phase 15: nicht im Kino und nicht auf den Fotos des Regiebuchs
            hud.refresh(gl, 250, this::paintHud, frozen);
            hud.draw(gl, overlay, emptyVao, 12, Math.max(0, surfH - hud.height - 12), surfW, surfH);
        }
        gl.glBindVertexArray(0);
        gl.glUseProgram(0);
        mark(gl, 7);
    }

    /** Seit Phase 10: Bauten zeichnen (in hdrFbo, mit Tiefe), die Laternen aus dem Zustand der Leuchtfeuer. */
    private void drawBauten(GL4 gl, float[] vp, float[] inv, double[] sun, Sky.State st, double pre, boolean cloudsNow) {
        if (koerbeNeu) { bautenGl.upload(gl, BautenGl.KOERBE, koerbe.netz); koerbeNeu = false; }
        if (showHerde) {
            herde.bauen();
            bautenGl.upload(gl, BautenGl.HERDE, herde.netz);
        }
        if (abbruch != null && showBrocken && (abbruch.schnitt() != null || brockenDa)) {
            abbruch.stuecke(brockenNetz);
            bautenGl.upload(gl, BautenGl.BROCKEN, brockenNetz);
            brockenDa = brockenNetz.ecken() > 0;
        }
        if (spurNeu) { synchronized (spurNetz) { bautenGl.upload(gl, BautenGl.SPUR, spurNetz); } spurNeu = false; }   // seit Phase 15
        if (feuer.an && feuerGl.ok && umgebungLux < 2000) feuerGl.update(gl, feuer);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrFbo);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDepthFunc(reversedZ ? GL.GL_GREATER : GL.GL_LESS);
        gl.glDisable(GL.GL_BLEND);
        common(gl, bautenShader, vp, inv, sun);
        gl.glActiveTexture(GL.GL_TEXTURE0 + 13);
        gl.glBindTexture(GL.GL_TEXTURE_2D, cloudsNow ? wetter.cloudGl.shadowTexture() : 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        bautenShader.set(gl, "uCloudShadow", 13);
        double[] cr = cloudsNow ? wetter.cloudGl.shadowRect() : new double[]{0, 0, 1};
        bautenShader.set(gl, "uCloudRect", (float) cr[0], (float) cr[1], (float) Math.max(1, cr[2]), cloudsNow ? 1f : 0f);
        double e = clock.sun()[0];
        bautenShader.set(gl, "uNight", (float) smooth(-1, -6, e));
        // erleuchtetes Fenster rund 15 cd/m², warm [G]; Laternen aus com.dan.beacon (cd/m² → Szeneneinheiten)
        double k = (frameSky ? pre : exposure()) / Sky.CD_PER_UNIT;
        bautenShader.set(gl, "uWarm", (float) (15 * k), (float) (15 * k * 0.72), (float) (15 * k * 0.42));
        laterne(gl, "Q", feuer.quermarke.snapshot(), feuer.quermarkeFrueher, k);
        // seit Phase 13: Anstrahlung des Quermarkenfeuers bei Nacht (ein warmer Strahler am Kiesweg, 3 000 cd, 2 800 K) [G]
        if (feuer.anstrahlung && feuer.quermarkeFrueher && umgebungLux < 400) {
            double[] f = bauwerke.fokusQuermarke;
            double a = Math.toRadians(215), sx = f[0] + Math.sin(a) * 9, sz = f[2] - Math.cos(a) * 9, sy = groundHeight(sx, sz) + 0.6;
            double i = 3000 * smooth(400, 50, umgebungLux) * k;
            bautenShader.set(gl, "uSpotPos", (float) sx, (float) sy, (float) sz, 1f);
            bautenShader.set(gl, "uSpotI", (float) (i * 1.32), (float) (i * 0.95), (float) (i * 0.58));
        } else bautenShader.set(gl, "uSpotPos", 0f, 0f, 0f, 0f);
        laterne(gl, "K", feuer.kampen.snapshot(), true, k);
        bautenGl.draw(gl, true, showKoerbe, showHerde, showBrocken);
        gl.glBindVertexArray(emptyVao);
    }

    /** Die Uniforms einer Laterne: Lage, Brennpunkt, Linse zum Auge und Streulicht (vorbelichtet). */
    private void laterne(GL4 gl, String t, com.dan.beacon.Beacon.Snapshot s, boolean an, double k) {
        double half = s.lensHalfHeight();
        bautenShader.set(gl, "uTurm" + t, (float) s.x(), (float) s.z(), (float) s.y(), (float) half);
        if (!an || !s.lit()) {
            bautenShader.set(gl, "uLens" + t, 0f, 0f, 0f);
            bautenShader.set(gl, "uGlow" + t, 0f, 0f, 0f);
            return;
        }
        double dx = camera.eye[0] - s.x(), dy = camera.eye[1] - s.y(), dz = camera.eye[2] - s.z(), dl = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double g = s.lensGlow();
        double[] tint = s.tint(), L = s.lensLuminance(dx / dl, dy / dl, dz / dl);
        for (int c = 0; c < 3; c++) L[c] = Math.max(0, L[c] - g * tint[c]);
        // die Linse zum Auge: begrenzt (der Lichtpunkt zeigt die volle Leuchtdichte)
        double cap = 5e4;
        bautenShader.set(gl, "uLens" + t, (float) (Math.min(cap, L[0]) * k), (float) (Math.min(cap, L[1]) * k), (float) (Math.min(cap, L[2]) * k));
        bautenShader.set(gl, "uGlow" + t, (float) (g * tint[0] * k), (float) (g * tint[1] * k), (float) (g * tint[2] * k));
    }

    /** Richtung, in die der Wind weht (x Ost, z Süd), als Einheitsvektor. */
    double[] windVector() {
        double a = Math.toRadians(wiring.windFromDeg);
        return new double[]{-Math.sin(a), 0, Math.cos(a)};
    }

    /** Albedo je Art als vec4 (für uAlb). */
    float[] albedo4() {
        int n = Terrain.ART.length;
        float[] a = new float[n * 4];
        for (int c = 0; c < n; c++) {
            a[c * 4] = karte.albedo[c * 3];
            a[c * 4 + 1] = karte.albedo[c * 3 + 1];
            a[c * 4 + 2] = karte.albedo[c * 3 + 2];
            a[c * 4 + 3] = 1;
        }
        return a;
    }

    /** Die Arten des Kerns zu 1 m als R8UI-Textur (0 ohne Karte). */
    static int klasseTexture(GL4 gl, KernKarte k) {
        if (!k.hatKarte()) return 0;
        int n = Terrain.KN;
        int[] tex = new int[1];
        gl.glGenTextures(1, tex, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, tex[0]);
        gl.glTexStorage2D(GL.GL_TEXTURE_2D, 1, GL4.GL_R8UI, n, n);
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, n, n, GL4.GL_RED_INTEGER, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(k.texturWerte()));
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 4);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        return tex[0];
    }

    /** Sonnenlicht des Platzhalter-Himmels (wie sunLight in common.glsl), linear (aus Rungholt). */
    public static double[] sunLight(double elevDeg) {
        double e = Math.sin(Math.toRadians(elevDeg));
        double vis = smooth(-0.015, 0.01, e), t = smooth(0.02, 0.35, e), f = 2.6 * vis * smooth(-0.02, 0.12, e);
        return new double[]{f, (0.55 + 0.40 * t) * f, (0.28 + 0.60 * t) * f};
    }

    static double smooth(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    /**
     * Seit Phase 12: die Änderung des Geländes (Abbrüche, Sturzkegel, Abtrag) als R32F-Textur auf Einheit
     * {@link #KORR_UNIT}, nur geänderte Rechtecke werden hochgeladen; die Zellmitten sind die der {@code Aenderung}.
     */
    private void korrektur(GL4 gl, Shader s) {
        com.dan.sylt.scene.Aenderung a = abbruch == null ? null : abbruch.aenderung;
        if (a == null) { s.set(gl, "uKorrOn", 0); return; }
        if (korrTex == 0) {
            int[] t = new int[1];
            gl.glGenTextures(1, t, 0);
            korrTex = t[0];
            gl.glBindTexture(GL.GL_TEXTURE_2D, korrTex);
            gl.glTexStorage2D(GL.GL_TEXTURE_2D, 1, GL4.GL_R32F, a.nx, a.nz);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
            a.alles();
        }
        int[] r = a.nimm();
        if (r != null) {
            int w = r[2] - r[0] + 1, h = r[3] - r[1] + 1;
            float[] f = new float[w * h];
            for (int j = 0; j < h; j++) System.arraycopy(a.d, (r[1] + j) * a.nx + r[0], f, j * w, w);
            gl.glBindTexture(GL.GL_TEXTURE_2D, korrTex);
            gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 4);
            gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, r[0], r[1], w, h, GL4.GL_RED, GL.GL_FLOAT, java.nio.FloatBuffer.wrap(f));
        }
        gl.glActiveTexture(GL.GL_TEXTURE0 + KORR_UNIT);
        gl.glBindTexture(GL.GL_TEXTURE_2D, korrTex);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        s.set(gl, "uKorr", KORR_UNIT);
        s.set(gl, "uKorrRect", (float) a.x0, (float) a.z0, (float) (a.nx * a.cell), (float) (a.nz * a.cell));
        s.set(gl, "uKorrOn", 1);
    }

    /** Einheit der Änderung des Geländes (0 bis 15 sind belegt). */
    static final int KORR_UNIT = 16;

    private void bindMarks(GL4 gl, Shader s) {
        s.set(gl, "uMarks", showMarks ? 1 : 0);
        rect(gl, s, "uMarkHaupt", Site.FERNFELD);
        rect(gl, s, "uMarkKern", Site.KERN);
        float[] pts = points();
        s.vec4Array(gl, "uPoints", pts);
        s.set(gl, "uPointCount", pts.length / 4);
        s.set(gl, "uGridOff", (float) Math.floorMod((long) Site.E0, 5000L), (float) Math.floorMod(-(long) Site.N0, 5000L));
    }

    private static void rect(GL4 gl, Shader s, String u, Site.Fenster f) {
        double[] r = f.scene();
        s.set(gl, u, (float) r[0], (float) r[1], (float) r[2], (float) r[3]);
    }

    /** Die Orte als vec4 (x, z, Halbmesser m, Art) für marken.glsl. */
    static float[] points() {
        Site.Ort[] o = Site.ORTE;
        float[] p = new float[Math.min(16, o.length) * 4];
        for (int i = 0; i < p.length / 4; i++) {
            p[i * 4] = (float) o[i].x();
            p[i * 4 + 1] = (float) o[i].z();
            p[i * 4 + 2] = o[i].art() == Site.Art.TURM ? 9f : 22f;
            p[i * 4 + 3] = o[i].art().ordinal();
        }
        return p;
    }

    private void common(GL4 gl, Shader s, float[] vp, float[] inv, double[] sun) {
        s.use(gl);
        // seit Phase 3: mit com.dan.sky Himmel aus dem Rundumbild, Sonne und Mond vorbelichtet, Dunst aus der Sicht
        boolean sm = frameSky && s != skyShader;
        s.set(gl, "uSkyMode", sm ? 1 : 0);
        if (sm) {
            Sky.State st = wetter.state();
            double pre = exposure();
            gl.glActiveTexture(GL.GL_TEXTURE7);
            gl.glBindTexture(GL.GL_TEXTURE_2D, wetter.panoTex);
            gl.glActiveTexture(GL.GL_TEXTURE0);
            s.set(gl, "uPano", 7);
            s.set(gl, "uSunColor", Wetter.scale(st.sunColor, pre));
            s.set(gl, "uMoonColor", Wetter.scale(st.moonColor, pre));
            s.set(gl, "uMoonDir", st.moonDir);
            s.set(gl, "uFogDist", (float) Wetter.fogDistance(st));
            s.set(gl, "uPre", (float) pre);
            s.set(gl, "uSkyAmb", Wetter.scale(wetter.ambient(st, showClouds), pre / Math.PI));
        }
        s.mat(gl, "uVP", vp);
        s.mat(gl, "uInvVP", inv);
        s.set(gl, "uCam", camera.eye);
        s.set(gl, "uSun", sun);
        s.set(gl, "uTime", (float) (clock.t() % 10_000));
        s.set(gl, "uLevel", (float) wiring.pegel);
        s.set(gl, "uReversed", reversedZ ? 1 : 0);
    }

    private void resizeTargets(GL4 gl, int w, int h) {
        Gpu.deleteFbos(gl, hdrFbo, hdrColorFbo);
        Gpu.deleteTextures(gl, hdrTex, depthTex);
        hdrTex = Gpu.texture(gl, w, h, GL.GL_RGBA16F, false);
        depthTex = Gpu.texture(gl, w, h, GL4.GL_DEPTH_COMPONENT32F, false);
        gl.glBindTexture(GL.GL_TEXTURE_2D, depthTex);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        hdrFbo = Gpu.fbo(gl, hdrTex, depthTex);
        hdrColorFbo = Gpu.fbo(gl, hdrTex, 0);
        hdrW = w;
        hdrH = h;
    }

    // ------------------------------------------------------------------ Zeitmessung (Tempo)

    private void mark(GL4 gl, int i) { gl.glQueryCounter(stamp[frame & 1][i], GL4.GL_TIMESTAMP); }

    /** Liest die Zeitstempel des vorigen Bilds, sobald sie da sind (kein Warten im Bildlauf). */
    private void readMarks(GL4 gl) {
        if (frame < 2) return;
        int o = (frame - 1) & 1;
        int[] av = new int[1];
        gl.glGetQueryObjectiv(stamp[o][MARKS - 1], GL4.GL_QUERY_RESULT_AVAILABLE, av, 0);
        if (av[0] == 0) return;
        long[] t = new long[MARKS];
        for (int i = 0; i < MARKS; i++) gl.glGetQueryObjecti64v(stamp[o][i], GL4.GL_QUERY_RESULT, t, i);
        for (int i = 0; i < MARKS - 1; i++) passMs[i] = passMs[i] * 0.9 + Math.max(0, t[i + 1] - t[i]) / 1e6 * 0.1;
        gpuMs = gpuMs * 0.9 + Math.max(0, t[MARKS - 1] - t[0]) / 1e6 * 0.1;
    }

    /** „Himmel 0,10 · Gelände 0,60 · …“ in ms (gleitendes Mittel). */
    public String passTimes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < passMs.length; i++)
            sb.append(i > 0 ? " · " : "").append(PASSES[i]).append(' ').append(String.format(Locale.GERMAN, "%.2f", passMs[i]));
        return sb.toString();
    }

    /** Die n teuersten Durchgänge: „Gelände 1,20 · Meer 0,40“. */
    public String topPasses(int n) {
        Integer[] idx = new Integer[passMs.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> Double.compare(passMs[b], passMs[a]));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(n, idx.length); i++)
            sb.append(i > 0 ? " · " : "").append(PASSES[idx[i]]).append(' ').append(String.format(Locale.GERMAN, "%.2f", passMs[idx[i]]));
        return sb.toString();
    }

    private void stats(long now, int w, int h) {
        statsFrames++;
        if (now - statsNanos < 500_000_000L) return;
        fps = statsFrames / ((now - statsNanos) / 1e9);
        statsFrames = 0;
        statsNanos = now;
        statusListener.accept(statusLine(w, h));
    }

    /** Auflösung nach der Zeit der Grafikkarte regeln (siehe {@link #autoScale}). */
    private void regulateScale(long now) {
        if (!autoScale || frame < 60 || now - scaleNanos < 1_500_000_000L) return;
        scaleNanos = now;
        double budget = 1000 / Math.max(10, targetFps), top = quality.renderScale, s = renderScale;
        double want = s * Math.sqrt(budget * 0.9 / Math.max(0.1, gpuMs));
        double next = s;
        if (gpuMs > budget * 1.05) next = Math.max(MIN_SCALE, Math.max(want, s - 0.15));
        else if (gpuMs < budget * 0.7 && s < top) next = Math.min(top, Math.min(want, s + 0.05));
        next = Math.round(next * 20) / 20.0;
        if (Math.abs(next - s) >= 0.045) { renderScale = next; scaleSteps++; }
    }

    /** Wie oft die Regelung die Auflösung geändert hat. */
    public int scaleSteps() { return scaleSteps; }

    /** Für Prüfungen: die Regelung mit vorgegebener Grafikkartenzeit und Bildnummer. */
    public void regulateForTest(double gpuMsNow, long nowNanos) {
        gpuMs = gpuMsNow;
        frame = Math.max(frame, 60);
        regulateScale(nowNanos);
    }

    public String statusLine(int w, int h) {
        String s = String.format(Locale.GERMAN, "%s · %.0f Bilder/s · Grafikkarte %.2f ms · Prozessor %.2f ms · %d×%d (%s%s) · %s",
                clock.label(), fps, gpuMs, cpuMs, w, h, quality.label, autoScale ? String.format(Locale.GERMAN, ", %.0f %%", renderScale * 100) : "", glInfo);
        long nowMs = System.currentTimeMillis();
        if (nowMs < meldungUntil) s = meldung + " · " + s;
        if (nowMs < photoMsgUntil) s = photoMsg + " · " + s;
        return s;
    }

    // ------------------------------------------------------------------ HUD

    BufferedImage paintHud() {
        String[] lines = hudFull ? hudLines() : hudShort();
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        java.awt.FontMetrics fm = pg.getFontMetrics(font);
        int tw = 0;
        for (String l : lines) tw = Math.max(tw, fm.stringWidth(l.startsWith("!") ? l.substring(1) : l));
        pg.dispose();
        int w = Math.max(200, tw + 22), lh = 17, h = 16 + lines.length * lh;
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(14, 20, 22, 190));
        g.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        g.setColor(new Color(125, 189, 179, 225));   // Nordsee-Grün, wie das Werkbuch
        g.setStroke(new BasicStroke(2f));
        g.drawLine(8, 1, 60, 1);
        g.setColor(new Color(224, 127, 72, 225));    // Kliffrot
        g.drawLine(62, 1, 80, 1);
        g.setFont(font);
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i];
            boolean warn = l.startsWith("!");
            if (warn) l = l.substring(1);
            g.setColor(i == 0 ? new Color(233, 236, 234) : warn ? new Color(224, 163, 63) : new Color(186, 196, 196));
            g.drawString(l, 10, 18 + i * lh);
        }
        g.dispose();
        return im;
    }

    /** Seit Phase 13: „Licht: Meeresleuchten 0,9 (August), am Kamm 5,4e-03 cd/m² · grünes Leuchten: Spiegelung heute (3-fach)“ */
    public String[] unwetterZeilen() { return unwetter.describe(camera.eye); }

    public String lichtZeile() {
        int m = clock.date().getMonthValue();
        double b = Double.isNaN(bluete) ? com.dan.surf.Bioluminescence.bloom(m, com.dan.surf.Bioluminescence.northSeaTemperature(m)) : bluete;
        double[] g = meeresleuchtenJetzt();
        double l = 0.2126 * g[0] + 0.7152 * g[1] + 0.0722 * g[2];
        double gf = wetter.sky.greenFlash;
        return String.format(Locale.GERMAN, "Licht: Meeresleuchten %s · grünes Leuchten %s", !meeresleuchten ? "aus" : b <= 0 ? "keine Blüte (Juni bis September, Wasser über 14 °C)"
                        : String.format(Locale.GERMAN, "Blüte %.2f%s, im stürzenden Kamm %.1e cd/m²", b, Double.isNaN(bluete) ? "" : " (von Hand)", l),
                gf <= 0 ? "aus" : gf > 1.5 ? String.format(Locale.GERMAN, "mit Luftspiegelung heute (%.0f-fach)", gf) : "nur die Brechung (heute keine Spiegelung)");
    }

    /** Das kurze HUD: Tempo, Zeiten der Durchgänge, Uhr und Sonne, Kamera. */
    String[] hudShort() {
        java.util.List<String> l = new java.util.ArrayList<>();
        String q = autoScale ? String.format(Locale.GERMAN, "%s %.0f %%", quality.label, renderScale * 100) : quality.label;
        l.add(String.format(Locale.GERMAN, "%.0f Bilder/s · Grafikkarte %.1f ms · Prozessor %.1f ms · %s %d×%d", fps, gpuMs, cpuMs, q, hdrW, hdrH));
        l.add(passTimes());
        double[] sp = clock.sun();
        l.add(String.format(Locale.GERMAN, "%s · Sonne %.0f° aus %03.0f° · Pegel %+.2f m NHN", clock.label(), sp[0], sp[1], wiring.pegel));
        l.add(cameraLine() + " · H: volles HUD, F1: Tasten");
        if (regie.running()) l.add(regieLine());   // seit Phase 15
        if (ritt.laeuft()) l.add(ritt.describe());
        if (System.currentTimeMillis() < meldungUntil) l.add(meldung);
        if (error != null) l.add("!" + error.split("\n")[0]);
        return l.toArray(new String[0]);
    }

    /** „Kreisen · Auge 24 m über NHN, 1,7 m über Grund · Blick 258° · Kern · 30 m bis Kliffkante“ */
    public String cameraLine() {
        return String.format(Locale.GERMAN, "%s · Auge %.1f m über NHN, %.1f m über Grund · Blick %03.0f° · %s · %s", camera.modeName(), camera.eye[1],
                camera.eye[1] - Math.max(camera.groundLevel, wiring.pegel), camera.heading(), Buehne.zone(camera.eye[0], camera.eye[2]),
                Buehne.naechsterOrt(camera.eye[0], camera.eye[2]));
    }

    /** Das volle HUD (H): alles, was die Phase weiß. */
    public String[] hudLines() {
        double[] sp = clock.sun();
        ComputeProbe.Result c = compute;
        double[] ll = Site.latLon(camera.eye[0], camera.eye[2]);
        double[] utm = Site.toUtm(camera.eye[0], camera.eye[2]);
        double kimm = Site.horizon(camera.eye[1] - wiring.pegel);
        return new String[]{
                String.format(Locale.GERMAN, "%.0f Bilder/s   Grafikkarte %.2f ms   Prozessor %.2f ms   Budget %.1f ms", fps, gpuMs, cpuMs, quality.budgetMs()),
                passTimes(),
                (c == null ? "!Compute-Test läuft …" : (c.ok() ? "" : "!") + c.line()),
                String.format(Locale.GERMAN, "%s · Sonne %.1f° / %03.0f° · Belichtung %.3g%s", clock.label(), sp[0], sp[1], exposure(),
                        wetter.skyOk ? (bildMessung ? " (Auge, im Bild gemessen)" : " (Auge, aus dem Himmel)") : " (Platzhalter-Himmel)"),
                wetter.describe(showClouds, cloudShade)[0],
                wetter.describe(showClouds, cloudShade)[1],
                wiring.wetterZeile(),
                meer.describe(),
                (groundOk ? "" : "!") + "Boden (ground, Biom " + meadow.biome + "): " + (groundOk ? groundGl.info() : "nicht gestartet"),
                karte.describe(),
                (kliff.ok ? "" : "!") + "Kliff (cliff, Lockergestein): " + kliff.hudLine(),
                wiring.surfZeile(camera.eye[0], camera.eye[2]),
                "Tide: " + wiring.tideZeile(),
                brandungGl.describe(brandung, nah),
                sand.zeile(camera.eye[0], camera.eye[2]),
                sturmflut.describe(clock) + (abbruch == null ? "" : " · " + abbruch.describe()),
                unwetter.describe(camera.eye)[0],
                unwetter.describe(camera.eye)[1],
                unwetter.describe(camera.eye)[2],
                leben.describe()[0],
                leben.describe()[1] + " · " + lebenGl.describe(),
                lichtZeile(),
                klang.describe(),
                regieLine(),
                serie.describe() + (ritt.laeuft() ? " · " + ritt.describe() : ""),
                (vorspuelung != null ? vorspuelung.describe() + " · " : "") + spurLine(),
                "Bauten: " + bauwerke.describe(),
                "Leuchtfeuer (beacon): " + feuer.describe(),
                koerbe.describe() + " · " + herde.describe(),
                physik.describe(),
                (terrain.measured() ? "" : "!") + "Gelände: " + terrain.describe(),
                String.format(Locale.GERMAN, "Gelände %d Knoten, %,d Punkte · fein bis %d m, im Kern %d m · Grund hier %.1f m über NHN · %s",
                        lod.nodes, lod.vertices, Quality.spacing(lod.finest), Quality.spacing(lod.finestKern), camera.groundLevel,
                        Terrain.ART[karte.at(terrain, camera.eye[0], camera.eye[2])]),
                cameraLine(),
                String.format(Locale.GERMAN, "UTM 32 N  E %.0f  N %.0f · %s · Kimm %.1f km", utm[0], utm[1], Site.dms(ll[0], ll[1]), kimm / 1000),
                "Renderer.jar: " + Bibliothek.kurz(),
                "Neu für Sylt: " + Bibliothek.neuStand(),
                String.format(Locale.GERMAN, "%s · %d×%d · %s", quality.label, hdrW, hdrH, reversedZ ? "umgekehrte Tiefe" : "klassische Tiefe"),
                glInfo};
    }

    // ------------------------------------------------------------------ Bilder

    private void takePhoto(GL4 gl, int w, int h) {
        try {
            BufferedImage img = readDefault(gl, w, h);
            File dir = photoDir != null ? photoDir : new File("docs/status");
            if (!dir.isDirectory() && !dir.mkdirs()) throw new java.io.IOException("Ordner " + dir + " lässt sich nicht anlegen");
            String name = photoName != null ? photoName : "Sylt_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            photoName = null;
            File f = new File(dir, name + ".png");
            javax.imageio.ImageIO.write(img, "png", f);
            photoMsg = "Foto gespeichert: " + f.getPath();
            System.out.println(photoMsg);
        } catch (Exception e) {
            photoMsg = "Foto fehlgeschlagen: " + e.getMessage();
            System.err.println(photoMsg);
        }
        photoMsgUntil = System.currentTimeMillis() + 5000;
    }

    /** Liest das fertige Fensterbild (nach Nachbearbeitung und HUD). */
    BufferedImage readDefault(GL4 gl, int w, int h) {
        ByteBuffer bb = Buffers.newDirectByteBuffer(w * h * 4);
        gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, defaultFbo);
        gl.glReadBuffer(defaultFbo == 0 ? GL.GL_BACK : GL.GL_COLOR_ATTACHMENT0);
        gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
        gl.glReadPixels(0, 0, w, h, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, bb);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] row = new int[w];
        for (int y = 0; y < h; y++) {
            int o = (h - 1 - y) * w * 4;
            for (int x = 0; x < w; x++) {
                int i = o + x * 4;
                row[x] = ((bb.get(i) & 255) << 16) | ((bb.get(i + 1) & 255) << 8) | (bb.get(i + 2) & 255);
            }
            img.setRGB(0, y, w, 1, row, 0, w);
        }
        return img;
    }

    /** Für Werkzeuge: das letzte Bild als Bild (im GL-Faden aufrufen). */
    public BufferedImage grab(GLAutoDrawable d) {
        GL4 gl = d.getGL().getGL4();
        defaultFbo = d.getContext().getDefaultDrawFramebuffer();
        return readDefault(gl, d.getSurfaceWidth(), d.getSurfaceHeight());
    }

    // ------------------------------------------------------------------ Ende

    private void fail(String msg) {
        error = msg;
        System.err.println(msg);
        errorListener.accept(msg);
    }

    @Override
    public void dispose(GLAutoDrawable d) {
        physik.stop();
        GL4 gl = d.getGL().getGL4();
        for (Shader s : all) s.dispose(gl);
        hud.dispose(gl);
        wetter.dispose(gl);
        meer.dispose(gl);
        brandungGl.dispose(gl);
        sandGl.dispose(gl);
        bautenGl.dispose(gl);
        if (kinoGl != null) kinoGl.dispose(gl);
        feuerGl.dispose(gl);
        unwetter.dispose(gl);
        lebenGl.dispose(gl);
        kliff.dispose(gl);
        if (korrTex != 0) gl.glDeleteTextures(1, new int[]{korrTex}, 0);
        korrTex = 0;
        if (groundOk) groundGl.dispose(gl);
        mudGl.dispose(gl);
        belichtung.dispose(gl);
        Gpu.deleteTextures(gl, klasseTex);
        lod.dispose(gl);
        Gpu.deleteFbos(gl, hdrFbo, hdrColorFbo);
        Gpu.deleteTextures(gl, hdrTex, depthTex);
        if (emptyVao != 0) gl.glDeleteVertexArrays(1, new int[]{emptyVao}, 0);
        for (int[] s : stamp) if (s[0] != 0) gl.glDeleteQueries(MARKS, s, 0);
        ready = false;
    }

    // ================================================================== Phase 15: Regiebücher, Kino, Vorspülung, Serie, mit der Welle

    /** Phase 15: die Regiebücher (Text mit Blatt am Ende), ihre Fotos und was sie sagen. */
    public final com.dan.sylt.scene.Regie regie = new com.dan.sylt.scene.Regie();
    private final RegieCtx regieCtx = new RegieCtx();
    private com.dan.kino.gl.KinoGl kinoGl;
    private final java.util.List<Runnable> kinoSchnitte = new java.util.ArrayList<>();
    private double kinoYaw, kinoTonBasis = Double.NaN;
    private boolean kinoFensterAn;
    private com.dan.sylt.scene.Regie.Book kinoBuch;
    /** true, wenn das Kino beginnt (Bedienfeld weg, Vollbild), false, wenn es zu Ende ist; im Bildlauf gerufen. */
    public volatile Consumer<Boolean> kinoFenster = an -> { };
    private volatile String captureCaption, sheetName, regieMsg = "";
    private final java.util.List<BufferedImage> album = new java.util.ArrayList<>();
    private final java.util.List<String> albumCaps = new java.util.ArrayList<>();
    private double albumFps, albumGpu;
    private int albumSamples;
    /** Das zuletzt geschriebene Blatt (für Tempo und die Prüfung); null: noch keins. */
    public volatile File lastSheet;
    /** Ohne Blatt (Prüfung). */
    public volatile boolean ohneBlatt;
    /** Foto und Blatt noch nicht abholen (Tempo: erst ein paar Bilder zum Einschwingen von Belichtung und Himmel). */
    public volatile boolean fotoAufschub;
    /** Fotos und Blatt des Regiebuchs (für die Prüfung: wie viele Fotos verlangt wurden). */
    public int fotosVerlangt;
    /** Abbrüche, als das Regiebuch den letzten auslöste (für {@code warte abbruch}). */
    private int abbruchZahl;
    /** Seit Phase 15: die Vorspülung vor dem Kliff (null ohne Kante). */
    public final com.dan.sylt.scene.Vorspuelung vorspuelung;
    /** Seit Phase 15: die Serien großer Wellen der Nahbrandung. */
    public final com.dan.sylt.scene.Serie serie = new com.dan.sylt.scene.Serie();
    /** Seit Phase 15: die Fahrt mit der Welle (Kür). */
    public final com.dan.sylt.scene.Wellenritt ritt;
    private java.util.function.Supplier<double[]> rittPose;
    /** Seit Phase 15: Pflöcke an der Linie, wo die Wand vor dem Abbruch stand („Die Spur der Eiszeit“). */
    private final com.dan.sylt.scene.Netz spurNetz = new com.dan.sylt.scene.Netz();
    private int spurGesehen;
    private volatile boolean spurNeu;
    public volatile boolean showSpur = true;

    /** Ein Regiebuch von vorn (das Album beginnt leer). */
    public void regieStart(com.dan.sylt.scene.Regie.Book b) {
        album.clear();
        albumCaps.clear();
        albumFps = albumGpu = 0;
        albumSamples = 0;
        fotosVerlangt = 0;
        regieMsg = "";
        frozen = false;
        abbruchZahl = abbruch != null ? abbruch.abbrueche : 0;
        regie.start(b, regieCtx);
    }

    public void regieStop() {
        regie.stop();
        if (kino.laeuft()) kino.abbrechen();
    }

    /** Ein Regiebuch im Kino (Vorspann mit Titel und Beschreibung, am Ende der Abspann). */
    public void kinoStart(com.dan.sylt.scene.Regie.Book b) {
        frozen = false;
        regie.beiText = kino::untertitel;
        kinoBuch = b;
        kino.beginnen("Sylt · Rotes Kliff, das Projekt der Brandung", b.name(), b.about());
        regieStart(b);
    }

    /** Die Zeilen des Abspanns (erste Zeile groß). */
    java.util.List<String> abspann(com.dan.sylt.scene.Regie.Book b) {
        java.util.List<String> z = new java.util.ArrayList<>();
        z.add(b != null ? b.name() : "Sylt");
        z.add("Ort: das Rote Kliff zwischen Wenningstedt und Kampen, Sylt");
        z.add("Szene: " + clock.dateLabel());
        z.add("Gelände: DGM1 Schleswig-Holstein, Küstenvorfeld der BAW, Kante aus kante.csv");
        z.add("Brandung, Sand, Kliff, Wetter, Licht, Vögel und Klang: com.dan");
        if (!album.isEmpty()) z.add("Fotos: " + album.size() + " auf dem Blatt in docs/status");
        z.add("Sylt · Rotes Kliff, das Projekt der Brandung · Phase 15");
        return z;
    }

    /** Ein Schnitt (Ansicht, Fahrt). Im Kino wartet er auf das Merken des Bilds für die Überblendung. */
    private void schnitt(Runnable r) {
        if (kino.schnitt()) kinoSchnitte.add(r);
        else { r.run(); kinoYaw = 0; }
    }

    /** Die wartenden Schnitte ausführen (nach dem Merken oder ohne Kino). */
    private void schnitteJetzt() {
        if (kinoSchnitte.isEmpty()) return;
        java.util.List<Runnable> l = new java.util.ArrayList<>(kinoSchnitte);
        kinoSchnitte.clear();
        for (Runnable x : l) x.run();
        kinoYaw = 0;
    }

    /** Kino weiter, Abspann am Ende des Regiebuchs, Schwenk und Ton. */
    private void kinoSchritt(double dt) {
        com.dan.kino.Kino.Lage l = kino.lage();
        if ((l == com.dan.kino.Kino.Lage.VORSPANN || l == com.dan.kino.Kino.Lage.FILM) && !regie.running() && !regieWantsFrame())
            kino.beenden(abspann(kinoBuch));
        kino.schritt(l == com.dan.kino.Kino.Lage.VORSPANN && regie.waiting() != null ? 0 : dt);
        if (!kino.laeuft() && !kinoSchnitte.isEmpty() && !kino.schnittFaellig()) schnitteJetzt();
        // der Schwenk: nur bei stehender Kamera, als Änderung der Blickrichtung
        double s = kino.schwenk();
        if (camera.fahrt == null && camera.mitfahrt == null && kinoSchnitte.isEmpty()) {
            double d = s - kinoYaw;
            if (camera.mode == com.dan.sylt.camera.Camera.Mode.FREI) camera.fyaw += d; else camera.yaw += d;
        }
        kinoYaw = s;
        // der Ton folgt dem Schwarz
        com.dan.soundscape.Soundscape ss = klang.s;
        if (kino.sichtbar()) {
            if (Double.isNaN(kinoTonBasis)) kinoTonBasis = ss.volume;
            ss.volume = kinoTonBasis * kino.ton();
        } else if (!Double.isNaN(kinoTonBasis)) { ss.volume = kinoTonBasis; kinoTonBasis = Double.NaN; }
    }

    /** Ohne Bild (Prüfung): was der Bildlauf nach dem Zeichnen tut – Schnitte ausführen, Fotos und Blatt verwerfen. */
    public void bildlos() {
        if (kino.schnittFaellig()) { schnitteJetzt(); kino.geschnitten(); }
        captureCaption = null;
        sheetName = null;
    }

    /** Ob ein Foto oder das Blatt auf das nächste Bild wartet (Tempo zeichnet dann sofort). */
    public boolean regieWantsFrame() { return captureCaption != null || sheetName != null || kino.schnittFaellig(); }

    /** „Regie: Ein Abend am Kliff · 21:52: die Sonne berührt die Kimm · 2:10 von 4:30 · 5 Fotos“ (HUD). */
    public String regieLine() {
        return "Regie: " + regie.describe() + (album.isEmpty() ? "" : " · " + album.size() + " Fotos") + (regieMsg.isEmpty() ? "" : " · " + regieMsg);
    }

    /** Seit Phase 15, je Schritt: Vorspülung, Serie, Fahrt mit der Welle, Spur nach dem Abbruch. */
    private void phase15Schritt(double dt) {
        if (vorspuelung != null) vorspuelung.schritt(dt, clock.epochMillis(), wiring.surf.hs);
        if (strandlaeuferIn > 0) {
            strandlaeuferT += dt;
            if (strandlaeuferAusrichten(strandlaeuferT > 8 ? 1e9 : 40)) strandlaeuferIn = 0;
        }
        com.dan.sylt.scene.Nahbrandung.Stueck st = nah.stueck();
        serie.schritt(dt, st != null ? st.zug : null);
        if (rittPose != null) {
            ritt.schritt(dt);
            if (ritt.laeuft() && camera.mitfahrt == rittPose) camera.mitNeigung(ritt.neigung);
            else {
                if (camera.mitfahrt == rittPose) camera.stopMitfahrt();
                camera.diving = false;
                rittPose = null;
            }
        }
        if (abbruch != null && abbruch.spurVersion != spurGesehen) {
            spurGesehen = abbruch.spurVersion;
            kliff.neueFindlinge.clear();
            kliff.neueFindlinge.addAll(abbruch.findlingStellen);
            kliff.findlinge.invalidate();
            spurBauen();
        }
    }

    private int strandlaeuferIn;
    private double strandlaeuferT;

    /** Das Strandläufer-Auge: 6 m landwärts und 4 m längs eines Trupps am Spülsaum (Sanderlinge zuerst), 25 cm über dem Sand, Blick auf ihn. */
    private boolean strandlaeuferAusrichten(double hoechstens) {
        double[] a = camera.eye.clone();
        double bx = Double.NaN, bz = 0, bd = 1e18;
        for (com.dan.flock.Waders.Group g : leben.strand.groups()) {
            if (g.size() == 0) continue;
            // Sanderlinge zuerst (im Sommer sind sie selten, dann Austernfischer oder Möwen)
            double d = Math.hypot(g.cx - a[0], g.cz - a[2]) * (g.species == com.dan.flock.WaderSpecies.SANDERLING ? 0.3 : 1);
            if (d < bd) { bd = d; bx = g.cx; bz = g.cz; }
        }
        if (Double.isNaN(bx) && hoechstens < 1e8) return false;
        if (Double.isNaN(bx) || bd > hoechstens) return hoechstens > 1e8;
        double ex = bx + com.dan.sylt.scene.Kueste.SX * 6 + com.dan.sylt.scene.Kueste.AX * 4, ez = bz + com.dan.sylt.scene.Kueste.SZ * 6 + com.dan.sylt.scene.Kueste.AZ * 4;
        blick(ex, ez, 0.25, bx, Math.max(groundHeight(bx, bz), wiring.pegel) + 0.15, bz, 50);
        return true;
    }

    /** Pflöcke alle 6 m an der alten Linie des Fußes, rot und weiß, und eine Leine dazwischen [G]. */
    private void spurBauen() {
        synchronized (spurNetz) {
            spurNetz.leeren();
            java.util.List<double[]> l = new java.util.ArrayList<>(abbruch.spur);
            l.sort(java.util.Comparator.comparingDouble(p -> p[1]));
            double[] vorher = null;
            int k = 0;
            for (double[] p : l) {
                if (vorher != null && Math.abs(p[1] - vorher[1]) < 6) continue;
                double g = groundHeight(p[0], p[1]);
                int m = com.dan.sylt.scene.Netz.merkmal(k++ % 2 == 0 ? com.dan.sylt.scene.Netz.ROT : com.dan.sylt.scene.Netz.LACK, false, 0, g);
                spurNetz.balken(new double[]{p[0], g - 0.4, p[1]}, new double[]{p[0], g + 1.1, p[1]}, 0.08, 0.08, m);
                if (vorher != null && Math.abs(p[1] - vorher[1]) < 14) {
                    double gv = groundHeight(vorher[0], vorher[1]);
                    spurNetz.balken(new double[]{vorher[0], gv + 0.95, vorher[1]}, new double[]{p[0], g + 0.95, p[1]}, 0.015, 0.015,
                            com.dan.sylt.scene.Netz.merkmal(com.dan.sylt.scene.Netz.GELB, false, 0, g));
                }
                vorher = p;
            }
        }
        spurNeu = true;
    }

    /** „Spur der Eiszeit: alte Linie auf 120 m, Findlinge an 3 Stellen“ */
    public String spurLine() {
        if (abbruch == null || abbruch.spur.isEmpty()) return "Spur der Eiszeit: noch kein Abbruch";
        return String.format(Locale.GERMAN, "Spur der Eiszeit: alte Linie auf %,d m, Findlinge an %d Stellen", abbruch.spur.size(), abbruch.findlingStellen.size());
    }

    /** Seit Phase 15: die Fahrt „Mit der Welle“ beginnen; false ohne Stück der Nahbrandung. */
    public boolean wellenritt() {
        if (!ritt.starten(camera.eye)) return false;
        camera.stopFahrt();
        camera.diving = true;
        rittPose = ritt::pose;
        camera.startMitfahrt(rittPose);
        camera.mitNeigung(ritt.neigung);
        camera.fovy = 62;
        return true;
    }

    /** Die Fahrt „Treppe“: von der Kante die Strandtreppe hinunter und 25 m auf den Strand (30 s). */
    public com.dan.sylt.camera.Fahrt treppenFahrt() {
        com.dan.sylt.scene.Kante k = kliff.kante;
        if (k == null) return null;
        double z = Site.N0 - com.dan.sylt.scene.Bauwerke.TREPPE_N - 1.5;
        int r = k.row(z);
        if (r < 0) return null;
        double x0 = k.fussX(r) - 5, x1 = k.kanteX(r) + 5;
        int m = (int) Math.ceil(x1 - x0);
        double[] ly = new double[m + 1];
        double mx = -1e9;
        for (int i = 0; i <= m; i++) {
            double x = x0 + (x1 - x0) * i / m;
            mx = Math.max(mx, Math.max(groundHeight(x, z - 0.7), groundHeight(x, z + 0.7)) + 0.35);
            ly[i] = mx;
        }
        java.util.List<double[]> pts = new java.util.ArrayList<>();
        pts.add(new double[]{x1 + 12, groundHeight(x1 + 12, z) + 1.7, z});
        for (int i = m; i >= 0; i -= 2) pts.add(new double[]{x0 + (x1 - x0) * i / m, ly[i] + 1.65, z});
        for (double d = 3; d <= 25; d += 3) pts.add(new double[]{x0 - d, Math.max(groundHeight(x0 - d, z), wiring.pegel) + 1.7, z});
        com.dan.sylt.camera.Fahrt f = new com.dan.sylt.camera.Fahrt("Treppe", pts.toArray(new double[0][]), 30, 0, 14);
        f.endHeading = 250;
        f.endTurn = 12;
        return f;
    }

    /** Die Fahrt „Drohne“: 130 m vor dem Fuß, 45 m hoch, von N 6 088 800 nach N 6 090 800, Blick schräg zum Kliff (40 s). */
    public com.dan.sylt.camera.Fahrt drohnenFahrt() {
        com.dan.sylt.scene.Kante k = kliff.kante;
        java.util.List<double[]> pts = new java.util.ArrayList<>();
        for (double n = 6_088_800; n <= 6_090_800; n += 50) {
            double z = Site.N0 - n;
            int r = k == null ? -1 : k.row(z);
            double x = r >= 0 ? k.fussX(r) - 130 : Site.fromUtm(Site.wasserlinieE(n) - 70, n)[0];
            pts.add(new double[]{x, wiring.pegel + 45, z});
        }
        return new com.dan.sylt.camera.Fahrt("Drohne", pts.toArray(new double[0][]), 40, 68, 12);
    }

    /** Die Fahrt „Möwe“: hinter einer Möwe im Flug (die nächste fliegende am Auge), 5 m hinter und 1 m über ihr. */
    public boolean moewenFlug() {
        com.dan.flock.Flock.Gull best = null;
        double bd = 1e18;
        for (com.dan.flock.Flock.Gull g : leben.moewen.gulls()) {
            if (g.state != com.dan.flock.Flock.State.SOAR && g.state != com.dan.flock.Flock.State.FLY) continue;
            double d = Math.hypot(g.pos[0] - camera.eye[0], g.pos[2] - camera.eye[2]);
            if (d < bd) { bd = d; best = g; }
        }
        if (best == null) return false;
        final com.dan.flock.Flock.Gull g = best;
        final double[] kurs = {Double.NaN};
        camera.stopFahrt();
        camera.startMitfahrt(() -> {
            double vx = g.vel[0], vz = g.vel[2], v = Math.hypot(vx, vz);
            double h = v > 0.5 ? (Math.toDegrees(Math.atan2(vx, -vz)) + 360) % 360 : (Double.isNaN(kurs[0]) ? 0 : kurs[0]);
            if (Double.isNaN(kurs[0])) kurs[0] = h;
            else kurs[0] = (kurs[0] + (((h - kurs[0] + 540) % 360) - 180) * 0.04 + 360) % 360;
            double a = Math.toRadians(kurs[0]);
            return new double[]{g.pos[0] - Math.sin(a) * 5, g.pos[1] + 1.0, g.pos[2] + Math.cos(a) * 5, kurs[0], 0, 0};
        });
        camera.mitNeigung(6);
        return true;
    }

    /**
     * Phase 15: eine Ansicht nach Namen: die Tasten 1 bis 9 und die Vorlagen, dazu die Blicke der Regiebücher
     * (Strandkörbe, Strandläufer, Sonnenuntergang, Vor dem Kliff, Abbruch, Vorspülung) [G].
     */
    public boolean placeNamed(String name) {
        camera.minEye = com.dan.sylt.camera.Camera.MIN_EYE;
        camera.fovy = com.dan.sylt.camera.Camera.FOVY;
        String k = com.dan.sylt.scene.Regie.plainName(name);
        com.dan.sylt.scene.Kante kn = kliff.kante;
        switch (k) {
            case "strandkoerbe" -> {
                double nord = com.dan.sylt.scene.Strandkoerbe.GRUPPEN[1][0], z0 = Site.N0 - nord, wl = Site.wasserlinieE(nord) - Site.E0, fuss = wl + 60;
                for (double x = wl + 10; x < wl + 250; x += 1) if (groundHeight(x, z0) > 5.0) { fuss = x; break; }
                double ex = fuss - 30, ez = z0 + 22, tx = fuss - 9, tz = z0;
                return blick(ex, ez, 1.6, tx, groundHeight(tx, tz) + 0.6, tz, 50);
            }
            case "strandlaeufer" -> {
                // erst an den Spülsaum (die Vögel folgen dem Auge, sobald dort das Stück der Nahbrandung steht), dann auf einen Trupp richten
                placeView(com.dan.sylt.camera.Ansichten.byName("Spülsaum"));
                camera.minEye = 0.25;
                if (nah.an) nah.jetzt(camera.tx, camera.tz, wiring.pegel);   // das Stück der Nahbrandung gleich hier (die Vögel leben darin)
                strandlaeuferIn = 1;
                strandlaeuferT = 0;
                return true;
            }
            case "sonnenuntergang" -> {
                double x = Site.KLIFFKANTE.x(), z = Site.KLIFFKANTE.z(), y = groundHeight(x, z) + 1.7;
                double[] sp = clock.sun();
                camera.place(x, y, z, sp[1], 0.6);
                camera.fovy = 40;
                return true;
            }
            case "vordemkliff" -> {
                if (kn == null) break;
                int r = zeile(kn, 6_089_500);
                double fx = kn.fussX(r), fz = kn.z(r);
                double a = Math.toRadians(com.dan.sylt.scene.Kueste.SHOREWARD_DEG), ex = fx - Math.sin(a) * 60, ez = fz + Math.cos(a) * 60;
                return blick(ex, ez, 1.7, (fx + kn.kanteX(r)) / 2, (kn.hFuss[r] + kn.hKante[r]) / 2, fz, 60);
            }
            case "abbruch" -> {
                if (kn == null) break;
                int r = zeile(kn, 6_090_335);
                double fx = kn.fussX(r), fz = kn.z(r);
                return blick(fx - 48, fz + 34, 2.2, (fx + kn.kanteX(r)) / 2, (kn.hFuss[r] + kn.hKante[r]) / 2, fz, 55);
            }
            case "vorspuelung" -> {
                if (kn == null) break;
                int r = zeile(kn, 6_089_950), r2 = zeile(kn, 6_089_550);
                double ex = kn.kanteX(r) + 1, ez = kn.z(r);
                return blick(ex, ez, 1.7, kn.fussX(r2) - 30, wiring.pegel + 1, kn.z(r2), 55);
            }
            default -> { }
        }
        com.dan.sylt.camera.Ansichten.Ansicht a = com.dan.sylt.camera.Ansichten.byName(name);
        if (a == null && kn == null) return false;
        if (a == null) return false;
        return placeView(a);
    }

    /** Die Zeile der Kante am Nordwert (oder die nächste bis 150 m daneben; 0 ohne). */
    public static int zeile(com.dan.sylt.scene.Kante kn, double nord) {
        int r = kn.rowNear(Site.N0 - nord, 150);
        return Math.max(0, r);
    }

    /**
     * Seit Phase 16 (Block CHRONIK): die Kamera auf eine Stelle am Kliff richten, 110 m seewärts und 16 m über Grund
     * oder Wasser, Blick auf die Wand dort.
     */
    public void zurStelle(double x, double z, String text) {
        double ex = x - com.dan.sylt.scene.Kueste.SX * 110, ez = z - com.dan.sylt.scene.Kueste.SZ * 110;
        camera.mode = com.dan.sylt.camera.Camera.Mode.FREI;
        blick(ex, ez, 16, x, groundHeight(x, z) + 6, z, com.dan.sylt.camera.Camera.FOVY);
        camera.snap();
        if (text != null) hudMeldung(text);
    }

    /** Frei stehen bei (ex, ez), hoch über Grund oder Wasser, Blick auf (tx, ty, tz), Öffnung fovy. */
    private boolean blick(double ex, double ez, double hoch, double tx, double ty, double tz, double fovy) {
        camera.stopFahrt();
        camera.stopMitfahrt();
        camera.diving = false;
        camera.waterLevel = wiring.pegel;
        camera.groundLevel = groundHeight(ex, ez);
        double ey = Math.max(groundHeight(ex, ez), wiring.pegel) + hoch;
        double yaw = (Math.toDegrees(Math.atan2(tx - ex, -(tz - ez))) + 360) % 360;
        double pitch = -Math.toDegrees(Math.atan2(ty - ey, Math.hypot(tx - ex, tz - ez)));
        camera.place(ex, ey, ez, yaw, pitch);
        camera.fovy = fovy;
        return true;
    }

    /** Was die Regiebücher in der App tun. */
    private final class RegieCtx implements com.dan.sylt.scene.Regie.Ctx {
        @Override public void time(java.time.LocalDate date, double hour) {
            clock.set(date, hour);
            if (sturmflut.aktiv && Math.abs(sturmflut.stunden(clock) - 24) > 30) sturmflut.aktiv = false;
            wetter.sky.epochMillis = clock.epochMillis();
            koerbeNeu = true;
            readapt();
        }
        @Override public void lapse(double x) { clock.lapse = x; }
        @Override public void lage(String name) {
            com.dan.sylt.wiring.Wetterregie.Lage l = lageNachName(name);
            if (l == null) { regieMsg = "Lage „" + name + "“ gibt es nicht"; return; }
            sturmflut.aktiv = false;
            wiring.regie.lage(l);
        }
        @Override public void regie(String modus) {
            sturmflut.aktiv = false;
            switch (modus) {
                case "frei" -> wiring.regie.frei();
                case "sturmtag" -> wiring.regie.sturmtag();
                default -> wiring.regie.hand();
            }
        }
        @Override public void sturmflut(java.time.LocalDate tag, double stunde) {
            sturmflut.datum = tag;
            sturmflutStarten(stunde);
            wetter.sky.epochMillis = clock.epochMillis();
            koerbeNeu = true;
            readapt();
        }
        @Override public void abbruch(double nord) {
            if (abbruch == null) { regieMsg = "kein Kliff zum Abbrechen"; return; }
            abbruchZahl = abbruch.abbrueche;
            double z = Site.fromUtm(Site.E0, nord)[1];
            abbruch.imFaden(() -> {
                int r = abbruch.ausloesen(0, z);
                if (r < 0) regieMsg = String.format(Locale.GERMAN, "bei N %,.0f ist keine Wand über 6 m", nord);
            });
        }
        @Override public void vorspuelung() {
            if (vorspuelung == null) { regieMsg = "keine Kante für die Vorspülung"; return; }
            vorspuelung.starten(clock.epochMillis());
        }
        @Override public void view(String name) {
            schnitt(() -> {
                if (rittPose != null) ritt.halt();
                camera.stopFahrt();
                camera.stopMitfahrt();
                if (!placeNamed(name)) regieMsg = "Ansicht „" + name + "“ gibt es nicht";
                readapt();
            });
        }
        @Override public void fahrt(String name) {
            schnitt(() -> {
                String k = com.dan.sylt.scene.Regie.plainName(name);
                camera.minEye = com.dan.sylt.camera.Camera.MIN_EYE;
                camera.fovy = com.dan.sylt.camera.Camera.FOVY;
                if (rittPose != null) ritt.halt();
                switch (k) {
                    case "treppe" -> {
                        com.dan.sylt.camera.Fahrt f = treppenFahrt();
                        if (f == null) regieMsg = "die Treppe hat keinen Weg (ohne Kante)"; else camera.startFahrt(f);
                    }
                    case "drohne" -> camera.startFahrt(drohnenFahrt());
                    case "moewe" -> { if (!moewenFlug()) { regieMsg = "keine Möwe im Flug, die Drohne fliegt"; camera.startFahrt(drohnenFahrt()); } }
                    case "welle" -> { if (!wellenritt()) regieMsg = "keine Welle (Nahbrandung aus?)"; }
                    default -> regieMsg = "Fahrt „" + name + "“ gibt es nicht";
                }
                readapt();
            });
        }
        @Override public boolean ready(com.dan.sylt.scene.Regie.Warte w) {
            return switch (w.was()) {
                case "uhr" -> clock.epochMillis() / 3_600_000.0 >= w.hours();
                case "abbruch" -> abbruch == null || abbruch.abbrueche > abbruchZahl;
                case "ruhe" -> abbruch == null || abbruch.schnitt() == null;
                case "serie" -> serie.laeuft;
                case "unter" -> !ritt.laeuft() || ritt.abschnitt == com.dan.sylt.scene.Wellenritt.Abschnitt.UNTER;
                case "welle" -> !ritt.laeuft() && kinoSchnitte.isEmpty();
                case "vorspuelung" -> vorspuelung == null || vorspuelung.fertig();
                default -> true;
            };
        }
        @Override public void capture(String caption) { captureCaption = caption; fotosVerlangt++; }
        @Override public void sheet(String name) { sheetName = name; }
        @Override public void say(String text) { regieMsg = text; kino.untertitel(text); }
    }

    /** Eine Wetterlage nach Namen (Anfang des Namens im Bedienfeld oder des Schlüssels, ohne Umlaute). */
    public static com.dan.sylt.wiring.Wetterregie.Lage lageNachName(String name) {
        String k = com.dan.sylt.scene.Regie.plainName(name);
        for (com.dan.sylt.wiring.Wetterregie.Lage l : com.dan.sylt.wiring.Wetterregie.Lage.values())
            if (com.dan.sylt.scene.Regie.plainName(l.label).equals(k) || com.dan.sylt.scene.Regie.plainName(l.name()).equals(k)) return l;
        for (com.dan.sylt.wiring.Wetterregie.Lage l : com.dan.sylt.wiring.Wetterregie.Lage.values())
            if (com.dan.sylt.scene.Regie.plainName(l.label).startsWith(k)) return l;
        return null;
    }

    /** Die Fotos des Regiebuchs als ein Blatt nach docs/status (zwei Spalten, Unterschrift in zwei Zeilen; aus Glacier und Rungholt). */
    private void writeSheet(String name) {
        if (album.isEmpty() || ohneBlatt) return;
        try {
            int n = album.size(), cols = 2, rows = (n + 1) / 2, cw = 960, ch = (int) Math.round(album.get(0).getHeight() * cw / (double) album.get(0).getWidth()), cap = 50, head = 44;
            BufferedImage out = new BufferedImage(cols * cw, head + rows * (ch + cap), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = out.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new java.awt.Color(24, 14, 12));
            g.fillRect(0, 0, out.getWidth(), out.getHeight());
            g.setColor(new java.awt.Color(240, 230, 222));
            g.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 20));
            g.drawString(String.format(Locale.GERMAN, "Sylt · Regiebuch „%s“ · %s · Ø %.0f Bilder/s · Grafikkarte Ø %.2f ms · %s", name, quality.label,
                    albumFps / Math.max(1, albumSamples), albumGpu / Math.max(1, albumSamples), glInfo.replaceAll(" ·.*", "")), 14, 29);
            g.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 16));
            for (int i = 0; i < n; i++) {
                int cx = (i % cols) * cw, cy = head + (i / cols) * (ch + cap);
                g.drawImage(album.get(i), cx, cy, cw, ch, null);
                g.setColor(new java.awt.Color(196, 170, 160));
                String[] ln = albumCaps.get(i).split("\n");
                for (int k = 0; k < ln.length; k++) g.drawString(ln[k], cx + 10, cy + ch + 21 + 20 * k);
            }
            g.dispose();
            File dir = photoDir != null ? photoDir : new File("docs/status");
            if (!dir.isDirectory() && !dir.mkdirs()) throw new java.io.IOException("Ordner " + dir + " lässt sich nicht anlegen");
            String fn = "Regiebuch_" + name.replace("ü", "ue").replace("ö", "oe").replace("ä", "ae").replace("ß", "ss").replace(' ', '_') + "_"
                    + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";
            File f = new File(dir, fn);
            javax.imageio.ImageIO.write(out, "png", f);
            photoMsg = "Regiebuch gespeichert: " + f.getPath();
            lastSheet = f;
            System.out.println(photoMsg);
        } catch (Exception e) {
            photoMsg = "Blatt des Regiebuchs fehlgeschlagen: " + e.getMessage();
            System.err.println(photoMsg);
        }
        photoMsgUntil = System.currentTimeMillis() + 6000;
    }

    /** Fotos und Blatt eines Regiebuchs nach dem fertigen Bild (ohne HUD und Balken). */
    private void regieBild(GL4 gl) {
        if (fotoAufschub) return;
        String cap = captureCaption;
        if (cap != null) {
            captureCaption = null;
            album.add(readDefault(gl, surfW, surfH));
            albumCaps.add(clock.dateLabel() + " " + clock.timeLabel() + " · " + cap + "\n" + String.format(Locale.GERMAN, "%.0f Bilder/s · Grafikkarte %.1f ms", fps, gpuMs));
        }
        if (regie.running()) { albumFps += fps; albumGpu += gpuMs; albumSamples++; }
        String sn = sheetName;
        if (sn != null) { sheetName = null; writeSheet(sn); }
    }

    /** Schnitt mit gemerktem Bild, dann Balken, Schwarz, Untertitel; Fenster auf und zu. */
    private void kinoBild(GL4 gl, long now) {
        if (kinoGl != null && kinoGl.bereit()) {
            try {
                if (kino.schnittFaellig()) {
                    kinoGl.merken(gl, defaultFbo, surfW, surfH);
                    schnitteJetzt();
                    kino.geschnitten();
                }
                kinoGl.zeichnen(gl, defaultFbo, surfW, surfH, now / 1e9);
            } catch (RuntimeException e) {
                System.err.println("Sylt: Kino: " + e);
                kinoGl = null;
            }
        } else if (kino.schnittFaellig()) { schnitteJetzt(); kino.geschnitten(); }
        boolean an = kino.laeuft();
        if (an != kinoFensterAn) {
            kinoFensterAn = an;
            try { kinoFenster.accept(an); } catch (RuntimeException e) { System.err.println("Sylt: Kinofenster: " + e); }
        }
        if (kino.endeMelden()) hudMeldung("Kino zu Ende" + (lastSheet != null ? " · Blatt: " + lastSheet.getName() : ""));
    }

    /** Seit Phase 15: unter Wasser (mit der Welle): trübes Wasser der Brandung über die Szene (Tiefe als Kopie). */
    private void unterWasser(GL4 gl, int w, int h, float[] inv, Sky.State st, double pre, double[] sun) {
        double spiegel = ritt.spiegel;
        if (rittPose == null || !camera.diving || Double.isNaN(spiegel) || camera.eye[1] > spiegel - 0.05) return;
        int kopie = unwetter.tiefe(gl, hdrFbo, w, h);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrColorFbo);
        gl.glViewport(0, 0, w, h);
        com.dan.ocean.gl.OceanGl.Under u = new com.dan.ocean.gl.OceanGl.Under();
        u.invViewProj = inv;
        u.eye = camera.eye.clone();
        u.reversedZ = reversedZ;
        u.width = w;
        u.height = h;
        u.depthTexture = kopie;
        u.surface = spiegel;
        double[] e;
        if (st != null) {
            double[][] L = Unwetter.licht(st, pre, wetter.ambient(st, showClouds), cloudShade, new double[]{0, 1, 0}, new double[3], new double[]{0, 1, 0});
            double sy = Math.max(0, sun[1]);
            e = new double[]{L[1][0] * sy + L[4][0] * Math.PI, L[1][1] * sy + L[4][1] * Math.PI, L[1][2] * sy + L[4][2] * Math.PI};
        } else e = new double[]{1, 1, 1};
        // Brandungswasser: Sand und Blasen in Schwebe, grünlich [G]; Streulicht rund 5 % der Bestrahlung, trüb (σ 0,45/m)
        double[] wc = {0.045, 0.075, 0.065};
        u.scatter = new double[]{wc[0] * e[0] * 0.6, wc[1] * e[1] * 0.6, wc[2] * e[2] * 0.6};
        u.sigma = 0.45;
        u.darkening = 0.3;
        meer.oceanGl.drawUnderwater(gl, u);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, hdrColorFbo);
        gl.glViewport(0, 0, w, h);
        gl.glBindVertexArray(emptyVao);
    }
}
