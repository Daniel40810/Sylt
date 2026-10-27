package com.dan.sylt.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;

/**
 * Eine Herde Belted Galloways auf der Weide am Kampener Leuchtturm (seit Phase 10, F24 A): schwarz mit weißem Gurt,
 * hornlos, zottelig [nach Kampen1: Rinder vor den Wärterhäusern]. Die Weide liegt westlich und nordwestlich des Turms
 * [G], die Tiere halten Abstand zu Häusern und Turm.
 * <p>
 * Jedes Tier wechselt zwischen <b>Grasen</b> (Kopf unten, langsam vorwärts, 0,1 m/s, Richtung schwankt), <b>Gehen</b>
 * (Kopf oben, 0,6 m/s zu einem neuen Fleck nahe der Herde) und <b>Stehen</b> (Kopf oben, Wiederkäuen); die Dauer jedes
 * Zustands ist zufällig (10 bis 60 s). Die Herde hält zusammen: wer mehr als 35 m von der Mitte steht, geht zurück.
 */
public final class Herde {
    /** Mitte der Weide (dx, dz zum Turm) und ihr Halbmesser, Abstand zum Turm und zu Häusern (m) [G]. */
    public static final double[] WEIDE = {-150, -55};
    public static final double WEIDE_R = 85, TURM_ABSTAND = 55, HAUS_ABSTAND = 12;

    public enum Tun { GRASEN, GEHEN, STEHEN }

    public static final class Tier {
        public double x, z, y, richtung, kopf, schritt, uhr, zielX, zielZ, groesse;
        public Tun tun = Tun.GRASEN;
        final int kennzahl;
        Tier(int k) { kennzahl = k; }
    }

    public final List<Tier> tiere = new ArrayList<>();
    public final Netz netz = new Netz();
    public volatile boolean an = true;
    public final double mx, mz;
    private final DoubleBinaryOperator h;
    private final Random rnd = new Random(1856);
    private final double[][] haeuser;

    /** zahl Tiere um den Turm (tx, tz); haeuser: Mitten (x, z) der Häuser in der Nähe. */
    public Herde(int zahl, double tx, double tz, DoubleBinaryOperator h, double[][] haeuser) {
        this.h = h;
        this.haeuser = haeuser;
        mx = tx + WEIDE[0];
        mz = tz + WEIDE[1];
        this.tx = tx;
        this.tz = tz;
        for (int i = 0; i < zahl; i++) {
            Tier t = new Tier(i * 37 + 5);
            double[] p = freierOrt(mx, mz, 30);
            t.x = p[0];
            t.z = p[1];
            t.richtung = rnd.nextDouble() * 360;
            t.uhr = rnd.nextDouble() * 30;
            t.groesse = 0.92 + rnd.nextDouble() * 0.16;
            t.y = h.applyAsDouble(t.x, t.z);
            tiere.add(t);
        }
    }

    private final double tx, tz;

    /** Darf hier ein Tier stehen? (auf der Weide, nicht am Turm, nicht an einem Haus) */
    public boolean erlaubt(double x, double z) {
        if (Math.hypot(x - mx, z - mz) > WEIDE_R) return false;
        if (Math.hypot(x - tx, z - tz) < TURM_ABSTAND) return false;
        for (double[] p : haeuser) if (Math.hypot(x - p[0], z - p[1]) < HAUS_ABSTAND) return false;
        return true;
    }

    private double[] freierOrt(double cx, double cz, double r) {
        for (int i = 0; i < 200; i++) {
            double a = rnd.nextDouble() * 2 * Math.PI, d = Math.sqrt(rnd.nextDouble()) * r;
            double x = cx + Math.sin(a) * d, z = cz - Math.cos(a) * d;
            if (erlaubt(x, z)) return new double[]{x, z};
        }
        return new double[]{mx, mz};
    }

    /** Mitte der Herde. */
    public double[] mitte() {
        double x = 0, z = 0;
        for (Tier t : tiere) { x += t.x; z += t.z; }
        return new double[]{x / tiere.size(), z / tiere.size()};
    }

    /** Weiter um dt Sekunden (Szenenzeit). */
    public void schritt(double dt) {
        if (dt <= 0) return;
        dt = Math.min(dt, 5);
        double[] m = mitte();
        for (Tier t : tiere) {
            t.uhr -= dt;
            if (t.uhr <= 0) {
                double far = Math.hypot(t.x - m[0], t.z - m[1]);
                double w = rnd.nextDouble();
                if (far > 35 || w < 0.2) {
                    t.tun = Tun.GEHEN;
                    double[] p = freierOrt(far > 35 ? m[0] : t.x, far > 35 ? m[1] : t.z, 20);
                    t.zielX = p[0];
                    t.zielZ = p[1];
                    t.uhr = 60;
                } else if (w < 0.75) {
                    t.tun = Tun.GRASEN;
                    t.uhr = 10 + rnd.nextDouble() * 50;
                } else {
                    t.tun = Tun.STEHEN;
                    t.uhr = 10 + rnd.nextDouble() * 40;
                }
            }
            double v = 0, kopfZiel = 0;
            switch (t.tun) {
                case GRASEN -> {
                    v = 0.10;
                    kopfZiel = 1;
                    t.richtung += (rnd.nextDouble() - 0.5) * 30 * dt;
                }
                case GEHEN -> {
                    double dx = t.zielX - t.x, dz = t.zielZ - t.z, d = Math.hypot(dx, dz);
                    if (d < 1) { t.tun = Tun.STEHEN; t.uhr = 5 + rnd.nextDouble() * 10; }
                    else {
                        double soll = Math.toDegrees(Math.atan2(dx, -dz));
                        double diff = ((soll - t.richtung) % 360 + 540) % 360 - 180;
                        t.richtung += Math.max(-40 * dt, Math.min(40 * dt, diff));
                        v = 0.6;
                    }
                }
                case STEHEN -> { }
            }
            double a = Math.toRadians(t.richtung), nx = t.x + Math.sin(a) * v * dt, nz = t.z - Math.cos(a) * v * dt;
            if (erlaubt(nx, nz)) { t.x = nx; t.z = nz; t.schritt += v * dt; }
            else t.richtung += 90 * dt + 20;
            t.kopf += (kopfZiel - t.kopf) * Math.min(1, dt * 1.5);
            t.y = h.applyAsDouble(t.x, t.z);
        }
    }

    /** Das Netz aller Tiere neu bauen. */
    public void bauen() {
        netz.leeren();
        if (!an) return;
        for (Tier t : tiere) galloway(netz, t);
    }

    /**
     * Ein Galloway (2,0 m lang, 1,25 m Widerrist) aus Teilen: Rumpf als Achteckröhre (hinten schwarz, Gurt weiß, vorn
     * schwarz), Hals und Kopf (gesenkt beim Grasen), Ohren, vier Beine (schwingen beim Gehen), Schwanz.
     */
    static void galloway(Netz n, Tier t) {
        double s = t.groesse, a = Math.toRadians(t.richtung), fx = Math.sin(a), fz = -Math.cos(a), rx = -fz, rz = fx;
        int schwarz = Netz.merkmal(Netz.FELL, false, t.kennzahl, t.y), weiss = Netz.merkmal(Netz.FELL_WEISS, false, t.kennzahl, t.y);
        double bx = t.x, bz = t.z, by = t.y;
        // Rumpf: Röhre längs der Achse, Querschnitt elliptisch (0,40 breit, 0,52 hoch)
        double[] teil = {-0.92, -0.70, -0.15, 0.32, 0.72, 0.86};
        double[] radius = {0.55, 0.92, 1.0, 1.0, 0.88, 0.55};
        for (int i = 0; i + 1 < teil.length; i++) {
            int m = (i == 2) ? weiss : schwarz;
            roehre(n, bx, by, bz, fx, fz, rx, rz, s, teil[i], teil[i + 1], radius[i], radius[i + 1], 0.40, 0.50, 0.98, m);
        }
        // Beine: schwingen beim Gehen
        double phase = t.schritt / 0.9 * 2 * Math.PI;
        double[][] bein = {{0.55, 0.22, 0}, {0.55, -0.22, Math.PI}, {-0.62, 0.22, Math.PI}, {-0.62, -0.22, 0}};
        for (double[] b : bein) {
            double sw = Math.sin(phase + b[2]) * 0.18 * Math.min(1, t.tun == Tun.GEHEN ? 1 : 0.3);
            double ox = bx + (fx * b[0] + rx * b[1]) * s, oz = bz + (fz * b[0] + rz * b[1]) * s;
            double[] top = {ox, by + 0.68 * s, oz}, foot = {ox + fx * sw * s, by - 0.02, oz + fz * sw * s};
            n.balken(foot, top, 0.15 * s, 0.17 * s, schwarz);
        }
        // Hals und Kopf: oben 1,05 m, beim Grasen bis zum Boden
        double k = t.kopf;
        double[] hals0 = {bx + fx * 0.70 * s, by + 1.00 * s, bz + fz * 0.70 * s};
        double hy = by + (1.02 - 0.85 * k) * s, hd = 1.05 + 0.08 * k;
        double[] kopf = {bx + fx * hd * s, hy, bz + fz * hd * s};
        n.balken(hals0, kopf, 0.30 * s, 0.38 * s, schwarz);
        double[] maul = {kopf[0] + fx * 0.38 * s, kopf[1] - (0.10 + 0.25 * k) * s, kopf[2] + fz * 0.38 * s};
        n.balken(kopf, maul, 0.26 * s, 0.30 * s, schwarz);
        for (double side : new double[]{-1, 1}) {
            double[] o0 = {kopf[0] + rx * 0.12 * side * s, kopf[1] + 0.12 * s, kopf[2] + rz * 0.12 * side * s};
            double[] o1 = {o0[0] + rx * 0.20 * side * s, o0[1] + 0.03 * s, o0[2] + rz * 0.20 * side * s};
            n.balken(o0, o1, 0.12 * s, 0.05 * s, schwarz);
        }
        // Schwanz
        double[] sch0 = {bx - fx * 0.92 * s, by + 1.05 * s, bz - fz * 0.92 * s}, sch1 = {bx - fx * 1.0 * s, by + 0.35 * s, bz - fz * 1.0 * s};
        n.balken(sch0, sch1, 0.06 * s, 0.06 * s, schwarz);
    }

    /** Ein Stück Röhre längs der Körperachse von u0 bis u1 (m), Querschnitt 8-eckig, Halbachsen w·r, h·r um die Höhe yc. */
    static void roehre(Netz n, double bx, double by, double bz, double fx, double fz, double rx, double rz, double s,
                       double u0, double u1, double r0, double r1, double w, double h, double yc, int m) {
        int seiten = 8;
        for (int i = 0; i < seiten; i++) {
            double a0 = 2 * Math.PI * i / seiten, a1 = 2 * Math.PI * (i + 1) / seiten;
            double[][] q = new double[4][];
            int[] nn = new int[4];
            double[][] uu = {{u0, r0, a0}, {u0, r0, a1}, {u1, r1, a1}, {u1, r1, a0}};
            for (int k = 0; k < 4; k++) {
                double u = uu[k][0], r = uu[k][1], c = Math.cos(uu[k][2]), sn = Math.sin(uu[k][2]);
                double side = c * w * r, up = sn * h * r;
                q[k] = new double[]{bx + (fx * u + rx * side) * s, by + (yc + up) * s, bz + (fz * u + rz * side) * s};
                nn[k] = Netz.packNormal(rx * c / w, sn / h, rz * c / w);
            }
            n.ecke(q[0][0], q[0][1], q[0][2], nn[0], m); n.ecke(q[1][0], q[1][1], q[1][2], nn[1], m); n.ecke(q[2][0], q[2][1], q[2][2], nn[2], m);
            n.ecke(q[0][0], q[0][1], q[0][2], nn[0], m); n.ecke(q[2][0], q[2][1], q[2][2], nn[2], m); n.ecke(q[3][0], q[3][1], q[3][2], nn[3], m);
        }
        // Stirnflächen an den Enden der Röhre
        for (double[] e : new double[][]{{u0, r0, -1}, {u1, r1, 1}}) {
            if (e[0] != -0.92 && e[0] != 0.86) continue;
            double[] c = {bx + fx * e[0] * s, by + yc * s, bz + fz * e[0] * s};
            for (int i = 0; i < seiten; i++) {
                double a0 = 2 * Math.PI * i / seiten, a1 = 2 * Math.PI * (i + 1) / seiten;
                double[] p0 = {c[0] + rx * Math.cos(a0) * w * e[1] * s, c[1] + Math.sin(a0) * h * e[1] * s, c[2] + rz * Math.cos(a0) * w * e[1] * s};
                double[] p1 = {c[0] + rx * Math.cos(a1) * w * e[1] * s, c[1] + Math.sin(a1) * h * e[1] * s, c[2] + rz * Math.cos(a1) * w * e[1] * s};
                n.dreieck(c, p0, p1, new double[]{fx * e[2], 0, fz * e[2]}, m);
            }
        }
    }

    /** „14 Galloways, 9 grasen, Mitte 160 m westlich des Turms“ */
    public String describe() {
        int g = 0;
        for (Tier t : tiere) if (t.tun == Tun.GRASEN) g++;
        double[] m = mitte();
        return String.format(Locale.GERMAN, "%d Galloways, %d grasen, Mitte %.0f m vom Turm", tiere.size(), g, Math.hypot(m[0] - tx, m[1] - tz));
    }
}
