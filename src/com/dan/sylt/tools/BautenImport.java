package com.dan.sylt.tools;

import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Terrain;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Die Baukörper aus LOD2 Schleswig-Holstein (Phase 2, Schritt {@code bauten}): CityGML 1.0 in ZIPs aus dem
 * Mehrfachdownload. Befund Phase 2: die Namen der Dateien in den ZIPs stimmen nicht mit dem Inhalt überein, und von 108
 * ZIPs waren nur zwei verschieden; darum gilt die Lage aus dem {@code gml:Envelope}, doppelte Inhalte und doppelte
 * Gebäude (gml:id) werden übergangen. Gelesen werden Dach-, Wand- und Bodenflächen (nur die äußeren Ringe),
 * in Dreiecke zerlegt, nur Gebäude mit der Mitte im Fernfeld.
 * <p>
 * Schreibt {@code daten/bauten/bauten.bin} (DataOutputStream, Big-Endian: „SYBAU1“, Zahl der Gebäude, Zahl der Dreiecke; je Gebäude id, Mitte x z, Grund, Höhe, Dreiecke mit Art 0 Dach,
 * 1 Wand, 2 Boden und neun Koordinaten in der Szene) und {@code bauten.properties}; dazu
 * {@code docs/LOD2_nachladen.txt}: welche Kacheln mit Land im Fernfeld fehlen, mit den Links aus der Übersicht.
 */
public final class BautenImport {
    public static final File NACHLADEN = new File("docs/LOD2_nachladen.txt");

    private BautenImport() { }

    public static void main(String[] a) throws Exception { importieren(Rohdaten.laden(), new File("daten"), System.out::println); }

    static final class Gebaeude {
        String id;
        final List<double[]> tri = new ArrayList<>();   // je Dreieck: typ, 9 Koordinaten (UTM, NHN)
        double minZ = 1e9, maxZ = -1e9, sx, sy;
        int n;
    }

    public static List<String> importieren(Rohdaten r, File daten, Consumer<String> log) throws IOException {
        long t0 = System.nanoTime();
        List<String> bericht = new ArrayList<>();
        List<File> liste = new ArrayList<>();
        File dir = null;
        for (File d : r.lod2) {
            File[] fs = d.listFiles((dd, n) -> n.toLowerCase(Locale.ROOT).endsWith(".zip") || n.toLowerCase(Locale.ROOT).endsWith(".xml") || n.toLowerCase(Locale.ROOT).endsWith(".gml"));
            if (fs == null) continue;
            if (dir == null || new File(d, "LOD2_SH_Massendownload.geojson").isFile()) dir = d;   // die Übersicht liegt im ersten Ordner
            java.util.Arrays.sort(fs);
            liste.addAll(java.util.Arrays.asList(fs));
        }
        if (dir == null) throw new IOException("LOD2: kein Ordner da (" + r.lod2 + ")");
        File[] zips = liste.toArray(new File[0]);
        Set<String> inhalte = new HashSet<>(), ids = new HashSet<>();
        TreeSet<String> kacheln = new TreeSet<>();
        List<Gebaeude> geb = new ArrayList<>();
        int doppelt = 0, dateien = 0;
        Site.Fenster ff = Site.FERNFELD;
        for (File z : zips) {
            if (z.getName().toLowerCase(Locale.ROOT).endsWith(".zip")) {
                try (ZipFile zf = new ZipFile(z)) {
                    for (ZipEntry e : java.util.Collections.list(zf.entries())) {
                        if (e.isDirectory() || !(e.getName().toLowerCase(Locale.ROOT).endsWith(".xml") || e.getName().toLowerCase(Locale.ROOT).endsWith(".gml"))) continue;
                        String key = e.getCrc() + ":" + e.getSize();
                        if (!inhalte.add(key)) { doppelt++; continue; }
                        dateien++;
                        try (InputStream in = zf.getInputStream(e)) { lesen(in, ff, geb, ids, kacheln); }
                    }
                }
            } else {
                String key = "datei:" + z.length() + ":" + z.getName();
                if (!inhalte.add(key)) continue;
                dateien++;
                try (InputStream in = Files.newInputStream(z.toPath())) { lesen(in, ff, geb, ids, kacheln); }
            }
        }
        long tris = 0;
        for (Gebaeude g : geb) tris += g.tri.size();
        bericht.add(Raster.f("LOD2: %d ZIP/XML, %d verschiedene Inhalte (%d doppelt), Kacheln %s; %d Gebäude mit der Mitte im Fernfeld, %,d Dreiecke",
                zips.length, dateien, doppelt, kacheln.isEmpty() ? "keine" : String.join(", ", kacheln), geb.size(), tris));

        File bd = new File(daten, "bauten");
        bd.mkdirs();
        try (DataOutputStream o = new DataOutputStream(new java.io.BufferedOutputStream(Files.newOutputStream(new File(bd, "bauten.bin").toPath())))) {
            o.write("SYBAU1".getBytes(StandardCharsets.US_ASCII));
            o.writeInt(geb.size());
            o.writeLong(tris);
            for (Gebaeude g : geb) {
                o.writeUTF(g.id);
                double[] c = Site.fromUtm(g.sx / g.n, g.sy / g.n);
                o.writeFloat((float) c[0]);
                o.writeFloat((float) c[1]);
                o.writeFloat((float) g.minZ);
                o.writeFloat((float) g.maxZ);
                o.writeInt(g.tri.size());
                for (double[] t : g.tri) {
                    o.writeByte((int) t[0]);
                    for (int v = 0; v < 3; v++) {
                        double[] s = Site.fromUtm(t[1 + v * 3], t[2 + v * 3]);
                        o.writeFloat((float) s[0]);
                        o.writeFloat((float) t[3 + v * 3]);
                        o.writeFloat((float) s[1]);
                    }
                }
            }
        }
        Properties p = new Properties();
        p.setProperty("gebaeude", "" + geb.size());
        p.setProperty("dreiecke", "" + tris);
        p.setProperty("kacheln", String.join(",", kacheln));
        p.setProperty("format", "DataOutputStream (Big-Endian): SYBAU1, int Gebaeude, long Dreiecke; je Gebaeude UTF id, float x z grund hoehe, int n, n x (byte art 0 Dach 1 Wand 2 Boden, 9 float x y z der Szene)");
        p.setProperty("quelle", "LOD2 Schleswig-Holstein (CityGML 1.0); © GeoBasis-DE/LVermGeo SH, Datenlizenz Deutschland – Namensnennung – 2.0");
        p.setProperty("werkzeug", "com.dan.sylt.tools.BautenImport");
        try (var w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new File(bd, "bauten.properties")), StandardCharsets.UTF_8)) { p.store(w, "Baukoerper aus LOD2"); }

        // fehlende Kacheln mit Land im Fernfeld (Land: das Hauptraster liegt irgendwo über +2 m)
        Terrain t = Terrain.load(new File(daten, "gelaende"));
        List<String> fehlen = new ArrayList<>();
        for (int ek = (int) Math.floor(ff.eMin() / 1000); ek * 1000 < ff.eMax(); ek++)
            for (int nk = (int) Math.floor(ff.nMin() / 1000); nk * 1000 < ff.nMax(); nk++) {
                if (kacheln.contains(ek + "_" + nk)) continue;
                boolean land = false;
                for (int dy = 5; dy < 1000 && !land; dy += 10)
                    for (int dx = 5; dx < 1000 && !land; dx += 10) {
                        double[] xz = Site.fromUtm(ek * 1000 + dx, nk * 1000 + dy);
                        if (t.measured() && t.roh(xz[0], xz[1]) > 2) land = true;
                    }
                if (land) fehlen.add(ek + "_" + nk);
            }
        StringBuilder sb = new StringBuilder("LOD2 Schleswig-Holstein: Kacheln mit Land im Fernfeld, die im Ordner LOD2 der Rohdaten fehlen (BautenImport, "
                + java.time.LocalDate.now() + ").\nGelesen: " + (kacheln.isEmpty() ? "keine" : String.join(", ", kacheln)) + " (die übrigen ZIPs waren Doppel).\n"
                + "Einzeln laden (Links aus LOD2_SH_Massendownload.geojson), in den Ordner LOD2 legen (Pfad in daten/rohdaten.properties)\nund `ant aufbereitung -Dschritt=bauten,inhalt` laufen lassen.\n"
                + "Der Kern (Kampen, Wenningstedt, Türme) braucht 455_6087 bis 458_6091.\n\n");
        String geo = "";
        File gj = new File(dir, "LOD2_SH_Massendownload.geojson");
        if (gj.isFile()) geo = Files.readString(gj.toPath(), StandardCharsets.UTF_8);
        int ohneGebaeude = 0;
        for (String k : new ArrayList<>(fehlen)) {
            String name = "LoD2_32_" + k + "_1_SH.xml";
            // Kacheln, die die Übersicht nicht kennt, haben keine Gebäude (See, Strand, Watt)
            if (!geo.isEmpty() && !geo.contains("file=" + name)) { fehlen.remove(k); ohneGebaeude++; continue; }
            int i = geo.indexOf("file=" + name);
            String link = "";
            if (i > 0) {
                int s0 = geo.lastIndexOf('"', i) + 1, s1 = geo.indexOf('"', i);
                link = geo.substring(s0, s1);
            }
            boolean kern = Site.KERN.eMin() < (Integer.parseInt(k.substring(0, 3)) + 1) * 1000.0 && Site.KERN.eMax() > Integer.parseInt(k.substring(0, 3)) * 1000.0
                    && Site.KERN.nMin() < (Integer.parseInt(k.substring(4)) + 1) * 1000.0 && Site.KERN.nMax() > Integer.parseInt(k.substring(4)) * 1000.0;
            sb.append(name).append(kern ? "  (Kern)" : "").append(link.isEmpty() ? "" : "\n    " + link).append('\n');
        }
        if (fehlen.isEmpty()) sb.append("Keine: alle Kacheln mit Gebäuden im Fernfeld sind da.\n");
        sb.append("\n").append(ohneGebaeude).append(" Kacheln mit Land im Fernfeld stehen nicht in der Übersicht: dort gibt es keine Gebäude.\n");
        NACHLADEN.getAbsoluteFile().getParentFile().mkdirs();
        Files.writeString(NACHLADEN.toPath(), sb.toString());
        bericht.add(Raster.f("LOD2: %d Kacheln mit Gebäuden im Fernfeld fehlen (%d weitere ohne Gebäude), Liste in %s", fehlen.size(), ohneGebaeude, NACHLADEN.getPath()));
        for (String s : bericht) log.accept("  " + s);
        log.accept(Raster.f("Bauten: daten/bauten · %.1f s", (System.nanoTime() - t0) / 1e9));
        return bericht;
    }

    /** Liest eine CityGML-Datei: Envelope für die Kachel, Gebäude mit Flächen. */
    static void lesen(InputStream in, Site.Fenster ff, List<Gebaeude> out, Set<String> ids, Set<String> kacheln) throws IOException {
        try {
            XMLInputFactory f = XMLInputFactory.newInstance();
            f.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            XMLStreamReader x = f.createXMLStreamReader(in, "UTF-8");
            Gebaeude g = null;
            int tiefe = 0, gebTiefe = -1, typ = -1, innen = 0;
            boolean envelope = false, lower = false, pos = false;
            StringBuilder text = new StringBuilder();
            while (x.hasNext()) {
                int ev = x.next();
                if (ev == XMLStreamConstants.START_ELEMENT) {
                    tiefe++;
                    String n = x.getLocalName();
                    switch (n) {
                        case "Envelope" -> envelope = true;
                        case "lowerCorner" -> { lower = envelope; text.setLength(0); }
                        case "Building" -> {
                            if (g == null) {
                                g = new Gebaeude();
                                gebTiefe = tiefe;
                                String id = x.getAttributeValue("http://www.opengis.net/gml", "id");
                                g.id = id == null ? "ohne-id-" + out.size() : id;
                            }
                        }
                        case "RoofSurface" -> typ = 0;
                        case "WallSurface" -> typ = 1;
                        case "GroundSurface" -> typ = 2;
                        case "interior" -> innen++;
                        case "posList", "pos" -> { pos = g != null && typ >= 0 && innen == 0; text.setLength(0); }
                        default -> { }
                    }
                } else if (ev == XMLStreamConstants.CHARACTERS || ev == XMLStreamConstants.CDATA) {
                    if (lower || pos) text.append(x.getText());
                } else if (ev == XMLStreamConstants.END_ELEMENT) {
                    String n = x.getLocalName();
                    switch (n) {
                        case "lowerCorner" -> {
                            if (lower) {
                                String[] a = text.toString().trim().split("\\s+");
                                if (a.length >= 2) kacheln.add((int) Math.floor(Double.parseDouble(a[0]) / 1000) + "_" + (int) Math.floor(Double.parseDouble(a[1]) / 1000));
                            }
                            lower = false;
                        }
                        case "Envelope" -> envelope = false;
                        case "posList" -> {
                            if (pos) ring(g, typ, text.toString());
                            pos = false;
                        }
                        case "pos" -> {
                            if (pos) punkt(text.toString());   // einzelne Punkte, der Ring endet mit LinearRing
                            pos = false;
                        }
                        case "LinearRing" -> { if (g != null && innen == 0) punkteRing(g, typ); }
                        case "interior" -> innen--;
                        case "RoofSurface", "WallSurface", "GroundSurface" -> typ = -1;
                        default -> { }
                    }
                    if (n.equals("Building") && tiefe == gebTiefe && g != null) {
                        if (g.n > 0 && ids.add(g.id)) {
                            double e = g.sx / g.n, nn = g.sy / g.n;
                            if (e >= ff.eMin() && e <= ff.eMax() && nn >= ff.nMin() && nn <= ff.nMax()) out.add(g);
                        }
                        g = null;
                        gebTiefe = -1;
                    }
                    tiefe--;
                }
            }
            x.close();
        } catch (XMLStreamException e) {
            throw new IOException("LOD2: " + e.getMessage(), e);
        }
    }

    // einzelne gml:pos sammeln, bis der Ring endet
    private static final ThreadLocal<List<double[]>> PUNKTE = ThreadLocal.withInitial(ArrayList::new);

    static void punkt(String s) {
        String[] a = s.trim().split("\\s+");
        if (a.length >= 3) PUNKTE.get().add(new double[]{Double.parseDouble(a[0]), Double.parseDouble(a[1]), Double.parseDouble(a[2])});
    }

    static void punkteRing(Gebaeude g, int typ) {
        List<double[]> p = PUNKTE.get();
        if (p.size() >= 3 && typ >= 0) flaeche(g, typ, p);
        p.clear();
    }

    static void ring(Gebaeude g, int typ, String s) {
        String[] a = s.trim().split("\\s+");
        List<double[]> p = new ArrayList<>();
        for (int i = 0; i + 2 < a.length; i += 3) p.add(new double[]{Double.parseDouble(a[i]), Double.parseDouble(a[i + 1]), Double.parseDouble(a[i + 2])});
        flaeche(g, typ, p);
    }

    /** Ein ebenes Vieleck (geschlossen, letzter Punkt = erster) in Dreiecke (Ohrenschneiden in der Ebene der Fläche). */
    static void flaeche(Gebaeude g, int typ, List<double[]> p) {
        if (p.size() > 1 && java.util.Arrays.equals(p.get(0), p.get(p.size() - 1))) p = new ArrayList<>(p.subList(0, p.size() - 1));
        int n = p.size();
        if (n < 3) return;
        double nx = 0, ny = 0, nz = 0;
        for (int i = 0; i < n; i++) {
            double[] a = p.get(i), b = p.get((i + 1) % n);
            nx += (a[1] - b[1]) * (a[2] + b[2]);
            ny += (a[2] - b[2]) * (a[0] + b[0]);
            nz += (a[0] - b[0]) * (a[1] + b[1]);
        }
        int drop = Math.abs(nx) > Math.abs(ny) ? (Math.abs(nx) > Math.abs(nz) ? 0 : 2) : (Math.abs(ny) > Math.abs(nz) ? 1 : 2);
        int u = drop == 0 ? 1 : 0, v = drop == 2 ? 1 : 2;
        double area = 0;
        for (int i = 0; i < n; i++) { double[] a = p.get(i), b = p.get((i + 1) % n); area += a[u] * b[v] - b[u] * a[v]; }
        double sgn = area >= 0 ? 1 : -1;
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < n; i++) idx.add(i);
        int guard = 0;
        while (idx.size() > 3 && guard++ < 10 * n) {
            boolean cut = false;
            for (int k = 0; k < idx.size(); k++) {
                int ia = idx.get((k + idx.size() - 1) % idx.size()), ib = idx.get(k), ic = idx.get((k + 1) % idx.size());
                double[] a = p.get(ia), b = p.get(ib), c = p.get(ic);
                double cr = ((b[u] - a[u]) * (c[v] - a[v]) - (b[v] - a[v]) * (c[u] - a[u])) * sgn;
                if (cr <= 0) continue;
                boolean frei = true;
                for (int q : idx) {
                    if (q == ia || q == ib || q == ic) continue;
                    if (innen(p.get(q), a, b, c, u, v, sgn)) { frei = false; break; }
                }
                if (!frei) continue;
                dreieck(g, typ, a, b, c);
                idx.remove(k);
                cut = true;
                break;
            }
            if (!cut) break;
        }
        if (idx.size() >= 3)
            for (int k = 1; k + 1 < idx.size(); k++) dreieck(g, typ, p.get(idx.get(0)), p.get(idx.get(k)), p.get(idx.get(k + 1)));
        for (double[] q : p) {
            g.minZ = Math.min(g.minZ, q[2]);
            g.maxZ = Math.max(g.maxZ, q[2]);
            g.sx += q[0];
            g.sy += q[1];
            g.n++;
        }
    }

    static boolean innen(double[] p, double[] a, double[] b, double[] c, int u, int v, double sgn) {
        double d1 = ((b[u] - a[u]) * (p[v] - a[v]) - (b[v] - a[v]) * (p[u] - a[u])) * sgn;
        double d2 = ((c[u] - b[u]) * (p[v] - b[v]) - (c[v] - b[v]) * (p[u] - b[u])) * sgn;
        double d3 = ((a[u] - c[u]) * (p[v] - c[v]) - (a[v] - c[v]) * (p[u] - c[u])) * sgn;
        return d1 >= 0 && d2 >= 0 && d3 >= 0;
    }

    static void dreieck(Gebaeude g, int typ, double[] a, double[] b, double[] c) {
        g.tri.add(new double[]{typ, a[0], a[1], a[2], b[0], b[1], b[2], c[0], c[1], c[2]});
    }
}
