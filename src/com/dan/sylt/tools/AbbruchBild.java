package com.dan.sylt.tools;

import com.dan.fracture.Fracture;
import com.dan.fracture.Piece;
import com.dan.fracture.Poly;
import com.dan.fracture.Solid;
import com.dan.sylt.gl.Renderer;
import com.dan.sylt.scene.Kante;
import com.dan.sylt.scene.Kliffabbruch;
import com.dan.sylt.scene.SceneClock;
import com.dan.sylt.scene.Site;
import com.dan.sylt.scene.Sturmflut;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Seit Phase 12: der Abbruch als Bildfolge ohne Grafikkarte, im Querschnitt durch die Mitte des Stücks
 * ({@code docs/status/Abbruch_Schnitt.png}, sechs Felder): gemessenes Gelände (gestrichelt), Gelände mit Änderung,
 * Zellen des Körpers im Schnitt (Lehm rot, ausgespült hell), fallende Stücke, Pegel und Auflauf, die Linie der alten Kante.
 * Läuft dieselbe Folge wie {@link Abbruch}: Xaver im Zeitraffer bis zum ersten Stück (oder {@code -Dnord=…}), Physik fest
 * mit 1/120 s, nach dem Ruhen die nächste Flut 12 h später. Druckt die Sandbilanz.
 * <pre>ant abbruchbild [-Dnord=6090600]</pre>
 */
public final class AbbruchBild {
    private AbbruchBild() { }

    static final double NORD = Abbruch.NORD, STUNDE = Abbruch.STUNDE;

    static final int W = 640, H = 400;

    public static void main(String[] a) throws Exception {
        File png = new File(a.length > 0 ? a[0] : "docs/status/Abbruch_Schnitt.png");
        String nordOpt = System.getProperty("nord", System.getenv("NORD"));
        SceneClock clock = new SceneClock();
        clock.lapse = 0;
        Renderer r = new Renderer(clock);
        Kliffabbruch k = r.abbruch;
        if (k == null) { System.err.println("FEHLER: keine Kante"); System.exit(1); }
        k.manuell = true;
        k.asyncSolve = false;
        double nord = nordOpt == null || nordOpt.isBlank() ? NORD : Double.parseDouble(nordOpt);
        String so = System.getProperty("stunde");
        double stunde = so == null || so.isBlank() ? STUNDE : Double.parseDouble(so.replace(',', '.'));
        r.abbruchVorbereiten(stunde, nord);
        Kliffabbruch.Schnitt s = k.schnitt();
        if (s == null) { System.err.println("FEHLER: kein Stück Kliff gerechnet"); System.exit(1); }
        Kante kn = k.kante;
        int m = s.mitte;
        double zm = kn.z(m);
        System.out.printf(Locale.GERMAN, "Stück bei N %,.0f (%s), Stunde %.1f, Pegel %+.2f m, Auflauf %.2f m, %d Zellen, %.0f m³, %d Flächen für den Bestand angehoben%n",
                kn.nord[m], String.format(Locale.GERMAN, "Kehle %.1f m, L_c %.1f m", k.kehle[m], k.lc(m)), r.sturmflut.stunden(clock), r.wiring.pegel, r.brandung.auflaufR2(),
                s.solid.cells(), s.volumen0, s.gestaerkt);
        BufferedImage sheet = new BufferedImage(W * 3, H * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double t = 0;
        feld(g, 0, r, k, s, zm, "1 Hohlkehle, Stunde " + String.format(Locale.GERMAN, "%.1f", r.sturmflut.stunden(clock)));
        double[] zeiten = {1.5, 3, 6};
        for (int i = 0; i < zeiten.length; i++) {
            t = lauf(k, t, zeiten[i]);
            feld(g, i + 1, r, k, s, zm, (i + 2) + " nach " + zeiten[i] + " s");
        }
        while (k.schnitt() != null && t < 150) t = lauf(k, t, t + 1);
        feld(g, 4, r, k, s, zm, "5 Ruhe nach " + Math.round(t) + " s: Sturzkegel");
        k.rechnen = false;   // die nächste Flut: nur Kehle und Abtrag, kein neues Stück
        r.vorspulen(r.sturmflut.stunden(clock) + 12, 5, false);
        feld(g, 5, r, k, s, zm, String.format(Locale.GERMAN, "6 nächste Flut (Stunde %.0f): Abtrag", r.sturmflut.stunden(clock)));
        g.dispose();
        File dir = png.getAbsoluteFile().getParentFile();
        if (dir != null) dir.mkdirs();
        ImageIO.write(sheet, "png", png);
        double[] b = k.bilanz();
        System.out.printf(Locale.GERMAN, "Bilanz %.1f m³ = %.1f steht + %.1f Blöcke + %.1f zerfallen + %.1f ausgespült (Rest %.3f)%n",
                b[0], b[1], b[2], b[3], b[4], b[0] - b[1] - b[2] - b[3] - b[4]);
        System.out.printf(Locale.GERMAN, "Gelände: abgesenkt %.0f m³ (Blöcke + zerfallen + ausgespült %.0f), Kegel %.0f m³ locker (= %.0f · %.2f), Abtrag %.0f m³ · Abrisskante bis %.1f m hinter dem alten Fuß · Kante bis %.1f m zurück%n",
                k.vAbgesenkt, b[2] + b[3] + b[4], k.vKegel, b[2] + b[3], s.welt.material.bulking, k.vAbtrag, k.abrissTiefe(s.r0, s.r1), k.rueckgang(s.r0, s.r1));
        System.out.println("Bild: " + png.getPath());
        System.exit(0);
    }

    static double lauf(Kliffabbruch k, double t, double bis) {
        while (t < bis - 1e-9 && k.schnitt() != null) {
            k.physik(1 / 120.0);
            t += 1 / 120.0;
        }
        return t;
    }

    /** Ein Feld: Querschnitt längs x (Ost) durch z = zm, vom Strand bis hinter die Kante. */
    static void feld(Graphics2D g0, int i, Renderer r, Kliffabbruch k, Kliffabbruch.Schnitt s, double zm, String titel) {
        Graphics2D g = (Graphics2D) g0.create(W * (i % 3), H * (i / 3), W, H);
        Kante kn = k.kante;
        int m = s.mitte;
        double xa = kn.fussX(m) - 20, xb = kn.fussX(m) + 50, ya = Math.min(-1, kn.hFuss[m] - 3), yb = r.terrain.roh(xb, zm) + 3;
        double sk = Math.min((W - 40) / (xb - xa), (H - 60) / (yb - ya));
        java.util.function.DoubleUnaryOperator px = x -> 20 + (x - xa) * sk, py = y -> H - 20 - (y - ya) * sk;
        g.setColor(new Color(0xEEF3F7));
        g.fillRect(0, 0, W, H);
        // Wasser: Pegel und Auflauf
        double pegel = r.wiring.pegel, auf = pegel + r.brandung.auflaufR2();
        g.setColor(new Color(0x9CC3DD));
        g.fillRect(0, (int) py.applyAsDouble(pegel), W, H);
        g.setColor(new Color(0x2E6F99));
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{6, 4}, 0));
        g.drawLine(0, (int) py.applyAsDouble(auf), W, (int) py.applyAsDouble(auf));
        // Gelände mit Änderung (gefüllt) und gemessen (gestrichelt)
        Path2D.Double geh = new Path2D.Double(), roh = new Path2D.Double();
        geh.moveTo(px.applyAsDouble(xa), H);
        for (double x = xa; x <= xb; x += 0.25) {
            geh.lineTo(px.applyAsDouble(x), py.applyAsDouble(r.terrain.height(x, zm)));
            if (x == xa) roh.moveTo(px.applyAsDouble(x), py.applyAsDouble(r.terrain.roh(x, zm)));
            else roh.lineTo(px.applyAsDouble(x), py.applyAsDouble(r.terrain.roh(x, zm)));
        }
        geh.lineTo(px.applyAsDouble(xb), H);
        geh.closePath();
        g.setColor(new Color(0xD9C29A));
        g.fill(geh);
        // Zellen des Körpers im Schnitt (|z − zm| < 1,2 m): stehend rot, ausgespült/abgefallen hell umrandet
        Solid so = s.solid;
        for (int c = 0; c < so.cells(); c++) {
            double[] cc = so.center(c);
            if (Math.abs(cc[2] - zm) > 1.2) continue;
            Path2D p = umriss(so.cell(c), null, null, px, py);
            if (p == null) continue;
            if (so.attached(c)) {
                g.setColor(cc[1] < kliffKaolin(zm) ? new Color(0xE8DCC0) : new Color(0xA24B26));
                g.fill(p);
                g.setColor(new Color(0x5A2A15));
                g.setStroke(new BasicStroke(0.6f));
                g.draw(p);
            }
        }
        // Stücke (alle, deren Zellen nahe am Schnitt liegen)
        if (!s.beendet()) for (Fracture.Pose pose : s.welt.poses()) {
            Piece pc = pose.piece();
            for (int c = 0; c < pc.cellCount(); c++) {
                Path2D p = umriss(pc.cell(c), pose.rotation(), pose.position(), px, py);
                if (p == null) continue;
                double[] q = pose.position();
                if (Math.abs(q[2] - zm) > 6) continue;
                g.setColor(new Color(0xC8642E));
                g.fill(p);
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(0.8f));
                g.draw(p);
            }
        }
        g.setColor(new Color(0x3A2A1A));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Path2D.Double(geh.createTransformedShape(null)));
        g.setColor(new Color(0x444444));
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{3, 3}, 0));
        g.draw(roh);
        // die alte Kante (Werkbuch: Linie der alten Kante) und die Abrisskante
        double kx = k.kanteAlt[m] - Site.E0;
        g.setColor(new Color(0x1A5E20));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        if (kx < xb) {
            g.drawLine((int) px.applyAsDouble(kx), 18, (int) px.applyAsDouble(kx), H - 20);
            g.drawString("alte Kante", (int) px.applyAsDouble(kx) + 3, 30);
        } else g.drawString(String.format(Locale.GERMAN, "alte Kante %.0f m weiter landwärts →", kx - xb), W - 230, 30);
        double fx = kn.fussE[m] - Site.E0;
        g.drawLine((int) px.applyAsDouble(fx), (int) py.applyAsDouble(kn.hFuss[m]) - 30, (int) px.applyAsDouble(fx), (int) py.applyAsDouble(kn.hFuss[m]) + 6);
        g.drawString("alter Fuß", (int) px.applyAsDouble(fx) - 20, (int) py.applyAsDouble(kn.hFuss[m]) - 34);
        if (!Double.isNaN(k.abriss[m])) {
            double ax = k.abriss[m] - Site.E0;
            g.setColor(new Color(0xB01010));
            g.drawLine((int) px.applyAsDouble(ax), 40, (int) px.applyAsDouble(ax), H - 20);
            g.drawString("Abriss", (int) px.applyAsDouble(ax) + 3, 52);
        }
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.drawString(titel, 10, 14);
        double[] b = k.bilanz();
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.drawString(String.format(Locale.GERMAN, "N %,.0f · Pegel %+.2f m, Auflauf bis %+.2f m · %d Stücke · Blöcke %.0f, zerfallen %.0f, ausgespült %.0f m³ · Kegel %.0f, Abtrag %.0f m³",
                kn.nord[m], pegel, auf, s.welt.pieceCount(), b[2], b[3], b[4], k.vKegel, k.vAbtrag), 10, H - 6);
        g.setColor(new Color(0x666666));
        g.drawRect(0, 0, W - 1, H - 1);
        g.dispose();
    }

    static double kliffKaolin(double z) {
        double nord = Site.N0 - z, t = Math.max(0, Math.min(1, (nord - 6_088_700) / 1_000.0));
        return 7 - 10 * t;
    }

    /** Umriss der Zelle in der x-y-Ebene (Hülle der Ecken), mit Lage (R, t) eines Stücks oder ohne. */
    static Path2D umriss(Poly c, double[] R, double[] t, java.util.function.DoubleUnaryOperator px, java.util.function.DoubleUnaryOperator py) {
        double[] v = c.vertices();
        List<double[]> pts = new ArrayList<>();
        for (int q = 0; q + 2 < v.length; q += 3) {
            double x = v[q], y = v[q + 1], z = v[q + 2];
            if (R != null) {
                double wx = R[0] * x + R[1] * y + R[2] * z + t[0], wy = R[3] * x + R[4] * y + R[5] * z + t[1];
                x = wx;
                y = wy;
            }
            pts.add(new double[]{px.applyAsDouble(x), py.applyAsDouble(y)});
        }
        if (pts.size() < 3) return null;
        pts.sort((p, q) -> p[0] != q[0] ? Double.compare(p[0], q[0]) : Double.compare(p[1], q[1]));
        List<double[]> hull = new ArrayList<>();
        for (int pass = 0; pass < 2; pass++) {
            int start = hull.size();
            for (int k = 0; k < pts.size(); k++) {
                double[] p = pts.get(pass == 0 ? k : pts.size() - 1 - k);
                while (hull.size() >= start + 2) {
                    double[] o = hull.get(hull.size() - 2), aa = hull.get(hull.size() - 1);
                    if ((aa[0] - o[0]) * (p[1] - o[1]) - (aa[1] - o[1]) * (p[0] - o[0]) <= 0) hull.remove(hull.size() - 1);
                    else break;
                }
                hull.add(p);
            }
            hull.remove(hull.size() - 1);
        }
        Path2D.Double p = new Path2D.Double();
        for (int k = 0; k < hull.size(); k++) {
            if (k == 0) p.moveTo(hull.get(k)[0], hull.get(k)[1]);
            else p.lineTo(hull.get(k)[0], hull.get(k)[1]);
        }
        p.closePath();
        return p;
    }
}
