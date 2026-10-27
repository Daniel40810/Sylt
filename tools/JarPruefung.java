import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Prüfung von {@code lib/Renderer.jar} (Ant-Target {@code check-packages}, aus Brocken). Die Element-Packages liegen
 * nicht im Projekt Sylt: ihre Quelle ist das Projekt Renderer (Paketvertrag Punkt 11), dort prüft
 * {@code tools/PaketPruefung.java} jedes Package einzeln (Punkt 9). Hier wird nur geprüft, ob das Jar alles hat, was
 * Sylt braucht: jedes Package mit Klassen und README, Klassen für Java 21 (Version 65) oder älter, und bei den neuen
 * Packages {@code surf} und {@code dune} (Hüllen seit Phase 1) und dem geholten {@code beacon} (F18 A) Kern, Darstellung,
 * Shader und Prüfklasse. Aufruf ohne Ant: {@code java tools/JarPruefung.java lib/Renderer.jar}.
 */
public final class JarPruefung {
    /** Was Sylt aus Renderer.jar braucht (wie {@code app.Bibliothek}: GEBRAUCHT und NEU). */
    static final String[] GEBRAUCHT = {"ocean", "wave", "tide", "mudflat", "spray", "cliff", "fracture", "reed", "wind", "sky", "cloud", "rain", "fog",
            "weather", "lightning", "flock", "soundscape", "season", "kino", "store", "ground", "weathering", "wetglass", "surf", "dune", "beacon"};
    static final String[][] SHADER = {
            {"surf", "Surf.class", "gl/SurfGl.class", "gl/surf.vert", "gl/surf.frag", "demo/SurfCheck.class", "demo/SurfDemo.class"},
            {"dune", "Dune.class", "gl/DuneGl.class", "gl/dune.vert", "gl/dune.frag", "demo/DuneCheck.class", "demo/DuneDemo.class"},
            {"beacon", "Beacon.class", "Characteristic.class", "SectorTable.class", "gl/BeaconGl.class", "gl/beacon.glsl", "gl/beam.frag", "demo/BeaconCheck.class"}};

    public static void main(String[] a) throws Exception {
        File jar = new File(a.length > 0 ? a[0] : "lib/Renderer.jar");
        if (!jar.isFile()) { System.err.println("check-packages: " + jar + " fehlt"); System.exit(1); }
        Map<String, int[]> p = new TreeMap<>();   // Klassen, README, höchste Version
        try (ZipFile z = new ZipFile(jar)) {
            for (ZipEntry e : java.util.Collections.list(z.entries())) {
                String n = e.getName();
                if (!n.startsWith("com/dan/") || e.isDirectory()) continue;
                String[] t = n.split("/");
                if (t.length < 4) continue;
                int[] v = p.computeIfAbsent(t[2], k -> new int[3]);
                if (n.endsWith(".class")) {
                    v[0]++;
                    try (InputStream in = z.getInputStream(e); DataInputStream d = new DataInputStream(in)) {
                        d.readInt();
                        d.readUnsignedShort();
                        v[2] = Math.max(v[2], d.readUnsignedShort());
                    }
                } else if (t.length == 4 && t[3].equals("README.md")) v[1] = 1;
            }
            int fehler = 0;
            System.out.println("check-packages: " + jar.getName() + " mit " + p.size() + " Packages (Quelle: Projekt Renderer)");
            System.out.println("  Package        Klassen  README  Java");
            for (String g : GEBRAUCHT) {
                int[] v = p.get(g);
                boolean ok = v != null && v[0] > 0 && v[1] == 1 && v[2] <= 65;
                if (!ok) fehler++;
                System.out.printf("  %-14s %-8s %-7s %-5s %s%n", g, v == null ? "–" : v[0], v == null || v[1] == 0 ? "fehlt" : "ok", v == null ? "–" : v[2] - 44, ok ? "" : "FEHLER");
            }
            for (String[] s : SHADER)
                for (int i = 1; i < s.length; i++)
                    if (z.getEntry("com/dan/" + s[0] + "/" + s[i]) == null) { fehler++; System.out.println("  FEHLER: com/dan/" + s[0] + "/" + s[i] + " fehlt"); }
            if (fehler > 0) { System.out.println("check-packages: " + fehler + " Fehler – Renderer neu bauen (Clean and Build) und dist/Renderer.jar nach lib/ kopieren"); System.exit(1); }
            System.out.println("check-packages: alle " + GEBRAUCHT.length + " gebrauchten Packages in " + jar.getName()
                    + ", dazu Kern, Darstellung, Shader und Prüfklassen von surf und dune (Hüllen seit Phase 1) und beacon (aus Lighthouse, F18 A).");
        }
    }
}
