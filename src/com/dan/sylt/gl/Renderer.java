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
    public static final String VERSION = "Phase 3";
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
    /** Phase 15: der Kinomodus (com.dan.kino); bis dahin nur der Anschluss für die Tasten der Szene. */
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
    /** Strömung der Brandung in der See zeigen (Rippströme trüben das Wasser), stärker als „DLRG-Blick“. */
    public volatile boolean showStroemung = true, dlrgBlick;
    private double naesseTakt;
    public final Meadow meadow;
    /** Phase 4a: das Rote Kliff aus com.dan.cliff (Lockergestein), Grasnarbe, Findlinge. */
    public final Kliff kliff;
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
            lumShader = new Shader("Belichtung", null, "fullscreen.vert", "lum.frag");
    private final Shader[] all = {skyShader, terrainShader, meerShader, post, overlay, lumShader};
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
        wiring.stepTide();
        kliff = new Kliff(terrain, com.dan.sylt.scene.Kante.load(Daten.ordner()));
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
    public double fovy() { return Camera.FOVY; }

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
        nah.reihen = q == Quality.NIEDRIG ? 24 : q == Quality.MITTEL ? 40 : 56;
        wetter.clouds.quality = q.clouds;
        if (meadow != null) meadow.radius = q.groundRadius;
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

    /** Kinomodus verlassen (Esc im Kino; ab Phase 15 läuft das Regiebuch dabei weiter). */
    public void kinoVerlassen() { kino.abbrechen(); }

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
        kino.schritt(dt);
        physik.advance(dt);
        camera.groundLevel = groundHeight(camera.eye[0], camera.eye[2]);
        camera.waterLevel = wiring.pegel;
        camera.targetGround = Math.max(groundHeight(camera.tx, camera.tz), wiring.pegel);
        camera.step(dt);
        // Phase 3: Himmel, Wolken und See bekommen ihre Eingänge aus der Verdrahtung, dann ein Schritt
        boolean cloudsShown = wetter.cloudsOk && showClouds;
        wiring.cloudsDrawn = cloudsShown;
        wiring.stepTide();
        wiring.feed(wetter.sky, camera.eye[1]);
        wiring.feed(wetter.clouds);
        wiring.feed(meer.ocean);
        meer.ocean.step(dt);
        // Phase 7a: die Brandung (Feld im eigenen Faden), das Netz nahe am Auge, der Sand nach Auflauf und Schwall
        brandung.update(clock.t());
        double[] sp7 = clock.sun();
        nah.an = showNetz && showMeer;
        nah.wetter = new com.dan.surf.Swash.Weather(Math.max(0, Math.sin(Math.toRadians(sp7[0]))) * (1 - wiring.oktas / 8 * 0.8), wiring.windSpeed, 14, 0);
        nah.schritt(dt, camera.eye, camera.tx, camera.tz, wiring.pegel);
        // seit Phase 8: die Brandungsringe nur um die Buhnen nahe am Auge (die Reihen liegen über 4 km verteilt)
        meer.ocean.obstacleField = brandung.buhnen.hindernisseNahe(camera.eye[0], camera.eye[2], 900);
        if (naesse != null) {
            boolean neu = naesse.update(wiring.pegel, brandung.auflaufR2() * 0.8);
            naesseTakt += dt;
            if (neu || naesseTakt > 0.25) {
                naesseTakt = 0;
                naesse.ueberlagern(nah.sichtbar ? nah.stueck() : null);
            }
        }
        if (wetter.skyOk) {
            Sky.State st = wetter.sky.step(dt);
            wetter.clouds.step(dt);
            cloudShade = cloudsShown ? Clouds.shadow(wetter.clouds.snapshot(), camera.eye[0], camera.eye[2], Wetter.keyDir(st)) : 1;
            adapt(st, dt);
        }
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
                wetter.cloudGl.update(gl, cs, cl, camera.eye.clone());
                if (wetter.cloudCheck.isEmpty() && frame > 40) wetter.cloudCheck = wetter.cloudGl.selfCheck(gl, cs, cl);
            }
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, wetter.panoFbo);
            gl.glViewport(0, 0, Wetter.PANO_W, Wetter.PANO_H);
            wetter.skyGl.drawPanorama(gl, st, pre);
            if (cloudsNow) wetter.cloudGl.drawPanorama(gl, cs, cl, camera.eye.clone());
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
        terrainShader.set(gl, "uKorrOn", 0);
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
        terrainShader.set(gl, "uCliffRain", 0f);
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
                    ol -> brandungGl.feed(ol, brandung, nah, netzNow));
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
            meerShader.set(gl, "uKorrOn", 0);
            gl.glActiveTexture(GL.GL_TEXTURE6);
            gl.glBindTexture(GL.GL_TEXTURE_2D, depthTex);
            gl.glActiveTexture(GL.GL_TEXTURE0);
            meerShader.set(gl, "uDepth", 6);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
            gl.glActiveTexture(GL.GL_TEXTURE6);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            gl.glActiveTexture(GL.GL_TEXTURE0);
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
        if (showHud && !kino.laeuft()) {
            hud.refresh(gl, 250, this::paintHud, frozen);
            hud.draw(gl, overlay, emptyVao, 12, Math.max(0, surfH - hud.height - 12), surfW, surfH);
        }
        gl.glBindVertexArray(0);
        gl.glUseProgram(0);
        mark(gl, 7);
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

    /** Das kurze HUD: Tempo, Zeiten der Durchgänge, Uhr und Sonne, Kamera. */
    String[] hudShort() {
        java.util.List<String> l = new java.util.ArrayList<>();
        String q = autoScale ? String.format(Locale.GERMAN, "%s %.0f %%", quality.label, renderScale * 100) : quality.label;
        l.add(String.format(Locale.GERMAN, "%.0f Bilder/s · Grafikkarte %.1f ms · Prozessor %.1f ms · %s %d×%d", fps, gpuMs, cpuMs, q, hdrW, hdrH));
        l.add(passTimes());
        double[] sp = clock.sun();
        l.add(String.format(Locale.GERMAN, "%s · Sonne %.0f° aus %03.0f° · Pegel %+.2f m NHN", clock.label(), sp[0], sp[1], wiring.pegel));
        l.add(cameraLine() + " · H: volles HUD, F1: Tasten");
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
                wiring.duneZeile(camera.eye[0], camera.eye[2]),
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
        kliff.dispose(gl);
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
}
