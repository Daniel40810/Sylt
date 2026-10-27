package com.dan.sylt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Wo die Rohdaten liegen (Werkbuch 2.4). Nur die Werkzeuge in {@code com.dan.sylt.tools} lesen sie, einmal in Phase 2; die
 * App nie. Der Pfad steht in {@code daten/rohdaten.properties} (nicht in Git, wird beim ersten Lauf mit der Vorgabe
 * angelegt); {@code -Dsylt.rohdaten=…} und {@code -Dsylt.baw=…} setzen ihn für einen Lauf anders.
 * <ul>
 * <li>{@code rohdaten}: der Ordner mit {@code DGM1/}, {@code bDOM/}, {@code DOP20/}, {@code LOD2/} (Vorgabe C:\Daten\Sylt,
 * später die Wechselfestplatte);</li>
 * <li>{@code baw}: die Küstenhöhen der BAW ({@code topography_max_z_2015-2021_ger.tif}), Vorgabe im Projekt Rungholt;
 * nach dem ersten Lauf liest {@code gelaende} den Zuschnitt in {@code templates/rasters_BAW/}.</li>
 * </ul>
 */
public final class Rohdaten {
    public static final File DATEI = new File("daten", "rohdaten.properties");
    public static final String VORGABE = "C:\\Daten\\Sylt",
            BAW_VORGABE = "../Rungholt/templates/rasters_COP30/EasyGeo/topography_max_z_2015-2021_ger/topography_max_z_2015-2021_ger/GeoTiff/topography_max_z_2015-2021_ger.tif";
    /** Der Zuschnitt der BAW im Projekt (Werkbuch: „BAW-Zuschnitt nach templates/rasters_BAW“): 10 m über dem Hauptraster, 100 m über dem Fernring. */
    public static final File BAW_10 = new File("templates/rasters_BAW/sylt_topo_10m.tif"), BAW_100 = new File("templates/rasters_BAW/sylt_topo_100m.tif");

    public final File ordner, baw;

    private Rohdaten(File ordner, File baw) { this.ordner = ordner; this.baw = baw; }

    public File dgm1() { return new File(ordner, "DGM1"); }
    public File bdom() { return new File(ordner, "bDOM"); }
    public File dop20() { return new File(ordner, "DOP20"); }
    public File lod2() { return new File(ordner, "LOD2"); }

    /** Liest {@code daten/rohdaten.properties} (legt sie mit der Vorgabe an, wenn sie fehlt). */
    public static Rohdaten laden() throws IOException {
        Properties p = new Properties();
        if (DATEI.isFile()) try (InputStreamReader r = new InputStreamReader(new FileInputStream(DATEI), StandardCharsets.UTF_8)) { p.load(r); }
        else {
            File d = DATEI.getParentFile();
            if (!d.isDirectory() && !d.mkdirs()) throw new IOException("Ordner " + d + " lässt sich nicht anlegen");
            p.setProperty("rohdaten", VORGABE);
            p.setProperty("baw", BAW_VORGABE);
            try (Writer w = new OutputStreamWriter(new java.io.FileOutputStream(DATEI), StandardCharsets.UTF_8)) {
                p.store(w, " Sylt: wo die Rohdaten liegen (nur fuer com.dan.sylt.tools, Werkbuch 2.4). Nicht in Git.");
            }
            System.out.println("Angelegt: " + DATEI + " (rohdaten = " + VORGABE + ")");
        }
        String o = System.getProperty("sylt.rohdaten", p.getProperty("rohdaten", VORGABE));
        String b = System.getProperty("sylt.baw", p.getProperty("baw", BAW_VORGABE));
        return new Rohdaten(new File(o), new File(b));
    }

    /** Was fehlt (leer, wenn alles da ist). */
    public List<String> fehlt() {
        List<String> f = new ArrayList<>();
        if (!ordner.isDirectory()) f.add("Ordner " + ordner + " fehlt (Pfad in " + DATEI + ")");
        else for (File d : new File[]{dgm1(), bdom(), dop20(), lod2()}) if (!d.isDirectory()) f.add("Ordner " + d + " fehlt");
        if (!BAW_10.isFile() && !baw.isFile()) f.add("BAW " + baw + " fehlt und kein Zuschnitt in " + BAW_10.getParent());
        return f;
    }

    @Override public String toString() { return "Rohdaten " + ordner + " · BAW " + (BAW_10.isFile() ? BAW_10 + " (Zuschnitt)" : baw.getPath()); }
}
