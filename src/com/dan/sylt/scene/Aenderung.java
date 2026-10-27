package com.dan.sylt.scene;

/**
 * Die Änderung der Oberfläche (seit Phase 12, F3 A): ein Raster zu 1 m über dem Band des Kliffs, in dem Abbrüche,
 * Sturzkegel und der Abtrag in der nächsten Flut die Höhe ändern (m, auf das gemessene Gelände). Es ist die
 * {@link Terrain.Korrektur} des Geländes: {@link Terrain#height} rechnet es mit, und die Grafikkarte bekommt es als
 * R32F-Textur ({@code uKorr} in {@code terrain_height.glsl}) mit denselben Zellmitten, darum stimmen Bild und Physik.
 * Geändert wird nur im Faden, der es besitzt; gelesen wird ohne Sperre (eine Zelle ist ein float).
 */
public final class Aenderung implements Terrain.Korrektur {
    public final double x0, z0, cell;
    public final int nx, nz;
    public final float[] d;
    private double lo, hi;
    private int di0 = Integer.MAX_VALUE, dj0 = Integer.MAX_VALUE, di1 = -1, dj1 = -1;

    public Aenderung(double x0, double z0, double cell, int nx, int nz) {
        this.x0 = x0;
        this.z0 = z0;
        this.cell = cell;
        this.nx = nx;
        this.nz = nz;
        d = new float[nx * nz];
    }

    public double cx(int i) { return x0 + (i + 0.5) * cell; }
    public double cz(int j) { return z0 + (j + 0.5) * cell; }
    public int i(double x) { return (int) Math.floor((x - x0) / cell); }
    public int j(double z) { return (int) Math.floor((z - z0) / cell); }

    /** Änderung der Zelle (i, j) um dh (m); merkt das Rechteck für die Grafikkarte. Gibt die Volumenänderung zurück (m³). */
    public double add(int i, int j, double dh) {
        if (i < 0 || j < 0 || i >= nx || j >= nz || dh == 0) return 0;
        int k = j * nx + i;
        d[k] += (float) dh;
        lo = Math.min(lo, d[k]);
        hi = Math.max(hi, d[k]);
        synchronized (this) {
            di0 = Math.min(di0, i); dj0 = Math.min(dj0, j); di1 = Math.max(di1, i); dj1 = Math.max(dj1, j);
        }
        return dh * cell * cell;
    }

    /** Wert der Zelle (m). */
    public double at(int i, int j) { return i < 0 || j < 0 || i >= nx || j >= nz ? 0 : d[j * nx + i]; }

    /** Bilinear zwischen den Zellmitten (wie die Textur mit linearem Filter), außen 0. */
    @Override public double delta(double x, double z) {
        double u = (x - x0) / cell - 0.5, v = (z - z0) / cell - 0.5;
        if (u < -0.5 || v < -0.5 || u > nx - 0.5 || v > nz - 0.5) return 0;
        int i = (int) Math.floor(u), j = (int) Math.floor(v);
        double fx = u - i, fz = v - j;
        double a = clampAt(i, j), b = clampAt(i + 1, j), c = clampAt(i, j + 1), e = clampAt(i + 1, j + 1);
        return (a * (1 - fx) + b * fx) * (1 - fz) + (c * (1 - fx) + e * fx) * fz;
    }

    private double clampAt(int i, int j) {
        i = Math.max(0, Math.min(nx - 1, i));
        j = Math.max(0, Math.min(nz - 1, j));
        return d[j * nx + i];
    }

    @Override public double[] box() { return new double[]{x0, z0, x0 + nx * cell, z0 + nz * cell}; }

    @Override public double[] spanne() { return new double[]{Math.min(0, lo), Math.max(0, hi)}; }

    /** Das geänderte Rechteck seit dem letzten Abholen {i0, j0, i1, j1} oder null (für die Grafikkarte). */
    public synchronized int[] nimm() {
        if (di1 < 0) return null;
        int[] r = {di0, dj0, di1, dj1};
        di0 = dj0 = Integer.MAX_VALUE;
        di1 = dj1 = -1;
        return r;
    }

    /** Alles neu hochladen lassen. */
    public synchronized void alles() { di0 = 0; dj0 = 0; di1 = nx - 1; dj1 = nz - 1; }

    /** Summe der Änderung (m³), positiv und negativ getrennt. */
    public double[] volumen() {
        double p = 0, n = 0, a = cell * cell;
        for (float v : d) { if (v > 0) p += v * a; else n += v * a; }
        return new double[]{p, n};
    }
}
