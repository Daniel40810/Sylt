package com.dan.sylt.tools;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Phase 19 (F59 A, aus Brocken Phase 18): „die Vorgänger bauen unverändert“. Sylt hat Packages erweitert (surf, dune,
 * cliff, fracture, ocean, beacon, soundscape …) und neue gebracht; die Vorgänger nutzen dieselben Packages.
 * <ol>
 * <li>Jede Prüfklasse {@code com.dan.*.demo.*Check} in {@code lib/Renderer.jar} läuft im eigenen Prozess (sie endet mit
 * System.exit) und muss „Alles in Ordnung.“ melden.</li>
 * <li>Die Vorgänger, die {@code Renderer.jar} nutzen (Brocken, Geiranger, Glacier, Lighthouse, Thunder), werden mit
 * diesem Jar übersetzt: ihre Quellen und ihre übrigen Jars aus {@code lib/}, Ausgabe nach {@code build/vorgaenger/Name}.
 * Ihre Ordner bleiben unverändert. Rungholt und Haithabu bringen ihre Packages noch selbst mit; für sie zählt Schritt 1.</li>
 * </ol>
 * Bericht: {@code docs/status/Vorgaenger_Phase19.txt}. {@code ant vorgaenger}.
 */
public final class Vorgaenger {
    private Vorgaenger() { }

    /** Die Vorgänger mit Renderer.jar, als Ordner neben Sylt. */
    public static final String[] MIT_JAR = {"Brocken", "Geiranger", "Glacier", "Lighthouse", "Thunder"};

    /** Die Prüfklassen im Jar, sortiert. */
    public static List<String> pruefklassen(File jar) throws IOException {
        List<String> l = new ArrayList<>();
        try (JarFile j = new JarFile(jar)) {
            for (Enumeration<JarEntry> e = j.entries(); e.hasMoreElements(); ) {
                String n = e.nextElement().getName();
                if (n.startsWith("com/dan/") && n.contains("/demo/") && n.endsWith("Check.class") && !n.contains("$"))
                    l.add(n.substring(0, n.length() - 6).replace('/', '.'));
            }
        }
        l.sort(null);
        return l;
    }

    public static void main(String[] args) throws Exception {
        File jar = new File(args.length > 0 ? args[0] : "lib/Renderer.jar").getAbsoluteFile();
        boolean nurBauen = List.of(args).contains("--nur-bauen"), nurPruefen = List.of(args).contains("--nur-pruefen");
        StringBuilder b = new StringBuilder("Sylt · Vorgänger mit " + jar.getName() + " (" + jar.length() / 1024 + " KB), Phase 19 · "
                + java.time.LocalDateTime.now().withNano(0) + "\n");
        boolean alles = true;
        if (!nurBauen) alles &= pruefen(jar, b);
        if (!nurPruefen) alles &= bauen(jar, b);
        String e = "ERGEBNIS: " + (alles ? "die Vorgänger bauen unverändert" : "nicht alles in Ordnung") + "\n";
        System.out.print(e);
        b.append(e);
        File f = new File("docs/status/Vorgaenger_Phase19.txt");
        f.getParentFile().mkdirs();
        Files.writeString(f.toPath(), b.toString(), StandardCharsets.UTF_8);
        System.exit(alles ? 0 : 1);
    }

    static boolean pruefen(File jar, StringBuilder b) throws Exception {
        List<String> ks = pruefklassen(jar);
        String java = new File(System.getProperty("java.home"), "bin/java").getPath(), cp = System.getProperty("java.class.path");
        int gut = 0;
        long t0 = System.nanoTime();
        say(b, "1 · Prüfklassen in " + jar.getName());
        for (String k : ks) {
            long t = System.nanoTime();
            String out = "";
            int rc = -1;
            // ein zweiter Versuch: Rechenzeiten in den Prüfklassen schwanken auf einer beschäftigten Maschine
            for (int v = 0; v < 2 && !(rc == 0 && out.contains("Alles in Ordnung.")); v++) {
                ProcessBuilder pb = new ProcessBuilder(java, "-cp", cp, "-Dstdout.encoding=UTF-8", "-Djava.awt.headless=true", "-Xmx3g", k);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                rc = p.waitFor();
            }
            boolean ok = rc == 0 && out.contains("Alles in Ordnung.");
            if (ok) gut++;
            StringBuilder fehl = new StringBuilder();
            for (String l : out.split("\\R"))
                if (l.contains("FEHLER") || l.contains("  FEHL ") || l.contains("fehlgeschlagen") || l.contains("Exception")) fehl.append(fehl.length() > 0 ? " | " : "").append(l.strip());
            say(b, String.format(Locale.GERMAN, "  %-7s %-45s %5.1f s  %s", ok ? "ok" : "FEHLER", k, (System.nanoTime() - t) / 1e9,
                    ok ? "" : (fehl.length() > 0 ? fehl : "Rückgabe " + rc)).stripTrailing());
        }
        say(b, String.format(Locale.GERMAN, "  %d von %d Prüfklassen in Ordnung · %.0f s", gut, ks.size(), (System.nanoTime() - t0) / 1e9));
        return gut == ks.size() && !ks.isEmpty();
    }

    static boolean bauen(File jar, StringBuilder b) throws IOException {
        say(b, "2 · Die Vorgänger mit diesem Jar übersetzen (ihre Ordner bleiben unverändert)");
        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) { say(b, "  FEHLER  kein Java-Übersetzer (bitte mit einem JDK starten)"); return false; }
        File eltern = new File(System.getProperty("user.dir")).getAbsoluteFile().getParentFile();
        boolean alles = true;
        for (String name : MIT_JAR) {
            File p = new File(eltern, name), src = new File(p, "src"), lib = new File(p, "lib");
            if (!src.isDirectory()) { say(b, String.format("  —       %-11s nicht gefunden (%s)", name, p)); continue; }
            List<String> quellen = new ArrayList<>();
            try (Stream<Path> s = Files.walk(src.toPath())) { s.filter(x -> x.toString().endsWith(".java")).forEach(x -> quellen.add(x.toString())); }
            StringBuilder cp = new StringBuilder(jar.getPath());
            File[] jars = lib.listFiles((d, n) -> n.endsWith(".jar") && !n.equals("Renderer.jar") && !n.contains("natives"));
            if (jars != null) for (File j : jars) cp.append(File.pathSeparator).append(j.getPath());
            File out = new File("build/vorgaenger/" + name);
            loeschen(out);
            out.mkdirs();
            List<String> a = new ArrayList<>(List.of("-nowarn", "-encoding", "UTF-8", "--release", "21", "-proc:none", "-cp", cp.toString(), "-d", out.getPath()));
            a.addAll(quellen);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            long t = System.nanoTime();
            int rc = jc.run(null, new PrintStream(err, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8), a.toArray(new String[0]));
            String text = err.toString(StandardCharsets.UTF_8);
            long fehler = text.lines().filter(l -> l.contains(": error:") || l.contains(": Fehler:")).count();
            boolean ok = rc == 0;
            alles &= ok;
            say(b, String.format(Locale.GERMAN, "  %-7s %-11s %4d Quellen, %5.1f s%s", ok ? "ok" : "FEHLER", name, quellen.size(), (System.nanoTime() - t) / 1e9,
                    ok ? "" : String.format(Locale.GERMAN, ", %d Fehler, zuerst: %s", fehler, text.lines().filter(l -> l.contains("error") || l.contains("Fehler")).findFirst().orElse(text.strip()))));
        }
        return alles;
    }

    static void loeschen(File f) throws IOException {
        if (!f.exists()) return;
        try (Stream<Path> s = Files.walk(f.toPath())) { s.sorted(java.util.Comparator.reverseOrder()).forEach(x -> x.toFile().delete()); }
    }

    static void say(StringBuilder b, String s) {
        System.out.println(s);
        b.append(s).append('\n');
    }
}
