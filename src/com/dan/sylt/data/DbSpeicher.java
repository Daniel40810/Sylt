package com.dan.sylt.data;

import com.dan.sylt.scene.Regie;
import com.dan.store.Db;
import com.dan.store.Protokoll;
import com.dan.store.Zahl;

import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Speichern im Schema SYLT (Phase 16). Eine Verbindung, alle Zugriffe synchronisiert (der Faden der Daten, die
 * Chronik und die Oberfläche teilen sie). Geometrien in UTM 32N (SRID 25832, F35 A): Punkte als
 * {@code SDO_POINT_TYPE}, Linien und Flächen als WKT über {@code SDO_GEOMETRY(clob, 25832)}; gelesen mit
 * {@code SDO_UTIL.TO_WKTGEOMETRY}, ohne {@code sdoapi}. Szenenzeit als ms seit 1970 und als TIMESTAMP WITH TIME ZONE in
 * Europe/Berlin (F34 A). Mitgelieferte Zeilen werden nie überschrieben.
 */
public final class DbSpeicher implements Speicher {
    private final Db.Konfig konfig;
    private Connection c;
    private final String wo;

    public DbSpeicher(Db.Konfig k) throws Fehler {
        this.konfig = k;
        try {
            c = Db.oeffnen(k);
            String w = Db.einWert(c, "select user || ' auf ' || sys_context('USERENV','CON_NAME') from dual");
            wo = "Datenbank " + w;
        } catch (SQLException e) {
            throw new Fehler("Keine Verbindung zu " + k.kurz() + ": " + Db.erste(e), e);
        }
    }

    /** Für Prüfungen und den Einrichter: die offene Verbindung. */
    public synchronized Connection verbindung() throws Fehler {
        try {
            if (c == null || c.isClosed() || !c.isValid(3)) c = Db.oeffnen(konfig);
            return c;
        } catch (SQLException e) {
            throw new Fehler("Verbindung verloren: " + Db.erste(e), e);
        }
    }

    @Override public String wo() { return wo; }
    @Override public boolean datenbank() { return true; }

    private interface Arbeit<T> { T tun(Connection c) throws SQLException; }

    private synchronized <T> T mit(String was, Arbeit<T> a) throws Fehler {
        Connection cc = verbindung();
        try {
            return a.tun(cc);
        } catch (SQLException e) {
            throw new Fehler(was + ": " + Db.erste(e), e);
        }
    }

    /** In einer Transaktion: alles oder nichts. */
    private synchronized <T> T zusammen(String was, Arbeit<T> a) throws Fehler {
        Connection cc = verbindung();
        try {
            cc.setAutoCommit(false);
            T t = a.tun(cc);
            cc.commit();
            return t;
        } catch (SQLException e) {
            try { cc.rollback(); } catch (SQLException e2) { /* weiter */ }
            throw new Fehler(was + ": " + Db.erste(e), e);
        } finally {
            try { cc.setAutoCommit(true); } catch (SQLException e) { /* weiter */ }
        }
    }

    // ------------------------------------------------------------------ Hilfen

    private static int zahl(Connection c, String sql, Object... a) throws SQLException {
        String v = Db.einWert(c, sql, a);
        return v == null ? 0 : Integer.parseInt(v);
    }

    private static long neueId(Connection c, String seq) throws SQLException { return Long.parseLong(Db.einWert(c, "SELECT TO_CHAR(" + seq + ".CURRVAL) FROM DUAL")); }

    static void setD(PreparedStatement st, int i, Double v) throws SQLException {
        if (v == null || Double.isNaN(v) || Double.isInfinite(v)) st.setNull(i, Types.NUMERIC); else st.setDouble(i, v);
    }

    static void setS(PreparedStatement st, int i, String v) throws SQLException {
        if (v == null || v.isEmpty()) st.setNull(i, Types.VARCHAR);
        else if (v.length() > 3900) st.setCharacterStream(i, new StringReader(v), v.length());
        else st.setString(i, v);
    }

    private static void setClob(PreparedStatement st, int i, String v) throws SQLException {
        if (v == null) st.setNull(i, Types.CLOB); else st.setCharacterStream(i, new StringReader(v), v.length());
    }

    private static void setL(PreparedStatement st, int i, Long v) throws SQLException {
        if (v == null || v < 0) st.setNull(i, Types.NUMERIC); else st.setLong(i, v);
    }

    private static void setMs(PreparedStatement st, int i, Long ms) throws SQLException {
        if (ms == null) st.setNull(i, Types.NUMERIC); else st.setLong(i, ms);
    }

    /** Platzhalter für eine Szenenzeit als TIMESTAMP WITH TIME ZONE (ein Platzhalter: ms). */
    static final String TSTZ = "CASE WHEN ? IS NULL THEN NULL ELSE FROM_TZ(TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(? / 1000, 'SECOND'), 'UTC') AT TIME ZONE 'Europe/Berlin' END";

    /** Die beiden Platzhalter von {@link #TSTZ} füllen; gibt den nächsten Index zurück. */
    private static int setTz(PreparedStatement st, int i, Long ms) throws SQLException {
        setMs(st, i, ms);
        setMs(st, i + 1, ms);
        return i + 2;
    }

    private static Long ms(ResultSet r, String col) throws SQLException {
        long v = r.getLong(col);
        return r.wasNull() ? null : v;
    }

    private static Double dbl(ResultSet r, String col) throws SQLException {
        double v = r.getDouble(col);
        return r.wasNull() ? null : v;
    }

    private static double d(ResultSet r, String col) throws SQLException {
        double v = r.getDouble(col);
        return r.wasNull() ? Double.NaN : v;
    }

    private static Integer integer(ResultSet r, String col) throws SQLException {
        int v = r.getInt(col);
        return r.wasNull() ? null : v;
    }

    private static String clob(ResultSet r, String col) throws SQLException {
        Clob cl = r.getClob(col);
        return cl == null ? null : cl.getSubString(1, (int) cl.length());
    }

    private static Daten.Geom geom(ResultSet r, String col) throws SQLException {
        String w = clob(r, col);
        return w == null ? null : Daten.Geom.ausWkt(w);
    }

    /** SQL für eine Geometrie: Punkt als SDO_POINT_TYPE mit zwei Platzhaltern, sonst WKT mit einem; null: NULL. */
    static String geomSql(Daten.Geom g) {
        if (g == null) return "NULL";
        if (g.art().equals("PUNKT")) return "MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(?, ?, NULL), NULL, NULL)";
        return "MDSYS.SDO_GEOMETRY(?, 25832)";
    }

    /** Die Platzhalter von {@link #geomSql} füllen; gibt den nächsten Index zurück. */
    private static int setGeom(PreparedStatement st, int i, Daten.Geom g) throws SQLException {
        if (g == null) return i;
        if (g.art().equals("PUNKT")) {
            double[] u = Daten.utm(g.xz()[0][0], g.xz()[0][1]);
            st.setDouble(i, Math.round(u[0] * 100) / 100.0);
            st.setDouble(i + 1, Math.round(u[1] * 100) / 100.0);
            return i + 2;
        }
        setClob(st, i, g.wkt());
        return i + 1;
    }

    static Double r1(double v) { return Double.isNaN(v) ? null : Math.round(v * 10) / 10.0; }
    static Double r2(double v) { return Double.isNaN(v) ? null : Math.round(v * 100) / 100.0; }
    static Double r3(double v) { return Double.isNaN(v) ? null : Math.round(v * 1000) / 1000.0; }

    private static boolean mitgeliefert(Connection c, String tabelle, String name) throws SQLException {
        return zahl(c, "SELECT COUNT(*) FROM " + tabelle + " WHERE NAME = ? AND HERKUNFT = 'MITGELIEFERT'", name) > 0;
    }

    private void loeschen(String tabelle, String name) throws Fehler {
        zusammen("Löschen", c -> {
            if (mitgeliefert(c, tabelle, name)) throw new SQLException("„" + name + "“ ist mitgeliefert und wird nicht gelöscht");
            try (PreparedStatement st = c.prepareStatement("DELETE FROM " + tabelle + " WHERE NAME = ? AND HERKUNFT = 'EIGEN'")) { st.setString(1, name); st.executeUpdate(); }
            return null;
        });
    }

    private static final String REGIEBUCH_ID = "(SELECT MAX(ID) FROM SY_REGIEBUCH WHERE NAME = ?)";

    // ------------------------------------------------------------------ Stand

    @Override public Daten.Stand stand() throws Fehler {
        return mit("Stand", c -> new Daten.Stand(zahl(c, "SELECT COUNT(*) FROM SY_QUELLE"), zahl(c, "SELECT COUNT(*) FROM SY_BEFUND"),
                zahl(c, "SELECT COUNT(*) FROM SY_STURMFLUT"), zahl(c, "SELECT COUNT(*) FROM SY_VORSPUELUNG WHERE ART = 'BELEGT'"),
                zahl(c, "SELECT COUNT(*) FROM SY_KLIFFKANTE WHERE STAND = 'GEMESSEN_2024'"), zahl(c, "SELECT COUNT(*) FROM SY_ABBRUCH"),
                zahl(c, "SELECT COUNT(*) FROM SY_FINDLING"), zahl(c, "SELECT COUNT(*) FROM SY_STURMFLUT_LAUF"),
                zahl(c, "SELECT COUNT(*) FROM SY_VORSPUELUNG WHERE ART = 'APP'"), zahl(c, "SELECT COUNT(*) FROM SY_SERIE"), zahl(c, "SELECT COUNT(*) FROM SY_REGIEBUCH"),
                zahl(c, "SELECT COUNT(*) FROM SY_REGIELAUF"), zahl(c, "SELECT COUNT(*) FROM SY_ANSICHT"), zahl(c, "SELECT COUNT(*) FROM SY_ANSICHT WHERE ART = 'GEMERKT'"),
                zahl(c, "SELECT COUNT(*) FROM SY_PAKET_PRESET"), zahl(c, "SELECT COUNT(*) FROM SY_SITZUNG"), letzte(c, -1)));
    }

    // ------------------------------------------------------------------ Stammdaten

    @Override public List<Daten.Quelle> quellen() throws Fehler {
        return mit("Quellen", c -> {
            List<Daten.Quelle> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG FROM SY_QUELLE ORDER BY ID")) {
                while (r.next()) {
                    java.sql.Date ab = r.getDate(5);
                    l.add(new Daten.Quelle(r.getString(1), r.getString(2), r.getString(3), r.getString(4), ab == null ? null : ab.toLocalDate(), r.getString(6)));
                }
            }
            return l;
        });
    }

    @Override public List<Daten.Befund> befunde() throws Fehler {
        return mit("Befunde", c -> {
            Map<Long, List<String>> q = new LinkedHashMap<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT x.BEFUND_ID, q.KURZ FROM SY_BEFUND_QUELLE x JOIN SY_QUELLE q ON q.ID = x.QUELLE_ID ORDER BY x.ID")) {
                while (r.next()) q.computeIfAbsent(r.getLong(1), k -> new ArrayList<>()).add(r.getString(2));
            }
            List<Daten.Befund> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT ID, THEMA, GEGENSTAND, BEFUND, TEXT FROM SY_BEFUND ORDER BY ID")) {
                while (r.next()) l.add(new Daten.Befund(r.getString(2), r.getString(3), r.getString(4), r.getString(5), q.getOrDefault(r.getLong(1), List.of())));
            }
            return l;
        });
    }

    @Override public List<Daten.Sturmflut> sturmfluten() throws Fehler {
        return mit("Sturmfluten", c -> {
            List<Daten.Sturmflut> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT s.NAME, s.DATUM, s.SCHEITEL_LIST_CM, s.SCHEITEL_HOERNUM_CM, s.SCHEITEL_WESTERLAND_CM, "
                    + "s.BEZUG, s.KLIFFVERLUST_M, s.KLIFFVERLUST_TEXT, s.FOLGEN, q.KURZ AS QUELLE FROM SY_STURMFLUT s LEFT JOIN SY_QUELLE q ON q.ID = s.QUELLE_ID ORDER BY s.DATUM")) {
                while (r.next())
                    l.add(new Daten.Sturmflut(r.getString(1), r.getDate(2).toLocalDate(), integer(r, "SCHEITEL_LIST_CM"), integer(r, "SCHEITEL_HOERNUM_CM"),
                            integer(r, "SCHEITEL_WESTERLAND_CM"), r.getString("BEZUG"), dbl(r, "KLIFFVERLUST_M"), r.getString("KLIFFVERLUST_TEXT"), r.getString("FOLGEN"),
                            r.getString("QUELLE")));
            }
            return l;
        });
    }

    @Override public List<Daten.Leuchtfeuer> leuchtfeuer() throws Fehler {
        return mit("Leuchtfeuer", c -> {
            Map<Long, List<Daten.Sektor>> se = new LinkedHashMap<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT LEUCHTFEUER_ID, NR, FARBE, VON_GRAD, BIS_GRAD, TRAGWEITE_SM, TEXT FROM SY_SEKTOR "
                    + "ORDER BY LEUCHTFEUER_ID, NR")) {
                while (r.next())
                    se.computeIfAbsent(r.getLong(1), k -> new ArrayList<>()).add(new Daten.Sektor(r.getInt(2), r.getString(3), r.getDouble(4), r.getDouble(5),
                            dbl(r, "TRAGWEITE_SM"), r.getString(7)));
            }
            List<Daten.Leuchtfeuer> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT f.ID, f.NAME, f.KENNUNG, f.FEUERHOEHE_M, f.TURMHOEHE_M, f.TRAGWEITE_SM, "
                    + "f.IN_BETRIEB_VON, f.IN_BETRIEB_BIS, f.HEUTE_AN, f.OST, f.NORD, f.BESCHREIBUNG, q.KURZ AS QUELLE FROM SY_LEUCHTFEUER f "
                    + "LEFT JOIN SY_QUELLE q ON q.ID = f.QUELLE_ID ORDER BY f.ID")) {
                while (r.next()) {
                    double[] xz = Daten.xz(r.getDouble("OST"), r.getDouble("NORD"));
                    l.add(new Daten.Leuchtfeuer(r.getString("NAME"), r.getString("KENNUNG"), d(r, "FEUERHOEHE_M"), d(r, "TURMHOEHE_M"), d(r, "TRAGWEITE_SM"),
                            integer(r, "IN_BETRIEB_VON"), integer(r, "IN_BETRIEB_BIS"), r.getInt("HEUTE_AN") == 1, xz[0], xz[1], r.getString("BESCHREIBUNG"),
                            r.getString("QUELLE"), se.getOrDefault(r.getLong(1), List.of())));
                }
            }
            return l;
        });
    }

    @Override public List<Daten.Lage> lagen() throws Fehler {
        return mit("Lagen", c -> {
            List<Daten.Lage> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT * FROM SY_SEEGANG ORDER BY CASE HERKUNFT WHEN 'MITGELIEFERT' THEN 0 ELSE 1 END, ID")) {
                while (r.next())
                    l.add(new Daten.Lage(r.getString("NAME"), r.getString("SCHLUESSEL"), r.getString("PHASE"), d(r, "WIND_MS"), d(r, "RICHTUNG_GRAD"), d(r, "BOEIGKEIT"),
                            d(r, "BEDECKUNG_ACHTEL"), d(r, "HOHE_SCHICHT"), r.getString("WOLKENART"), d(r, "REGEN_MMH"), d(r, "SICHT_KM"), d(r, "NEBEL"), d(r, "NEBEL_HOCH_M"),
                            d(r, "HS_M"), d(r, "TP_S"), d(r, "SEE_RICHTUNG_GRAD"), r.getInt("GEWITTER"), d(r, "TEMPERATUR_C"), r.getString("BESCHREIBUNG"),
                            r.getString("HERKUNFT")));
            }
            return l;
        });
    }

    // ------------------------------------------------------------------ Chronik

    @Override public long abbruch(Daten.Abbruch a, Long sitzung) throws Fehler {
        Daten.Geom bs = a.bruchstelle() == null ? null : Daten.Geom.punkt(a.bruchstelle()[0], a.bruchstelle()[1]);
        return zusammen("Abbruch schreiben", c -> {
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_ABBRUCH (SZENE_MS, SZENE_ZEIT, NORD, NORD_VON, NORD_BIS, LAENGE_M, VOLUMEN_M3, BLOECKE_M3, "
                    + "ZERFALLEN_M3, AUSGESPUELT_M3, KEGEL_M3, KEHLE_M, LC_M, PEGEL_M, AUFLAUF_M, RUECKGANG_M, STURMFLUT, BRUCHSTELLE, RUECKGANG, STURMFLUT_LAUF_ID, SITZUNG_ID) "
                    + "VALUES (?, " + TSTZ + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + geomSql(bs) + ", " + geomSql(a.rueckgangFlaeche()) + ", "
                    + "(SELECT MAX(ID) FROM SY_STURMFLUT_LAUF WHERE SITZUNG_ID = ? AND ENDE_MS IS NULL), ?)")) {
                st.setLong(1, a.ms());
                int i = setTz(st, 2, a.ms());
                double[] v = {a.nord(), a.nordVon(), a.nordBis(), a.laenge(), a.volumen(), a.bloecke(), a.zerfallen(), a.ausgespuelt(), a.kegel()};
                for (int k = 0; k < v.length; k++) setD(st, i++, r1(v[k]));
                setD(st, i++, r2(a.kehle()));
                setD(st, i++, r2(a.lc()));
                setD(st, i++, r3(a.pegel()));
                setD(st, i++, r3(a.auflauf()));
                setD(st, i++, r2(a.rueckgang()));
                setS(st, i++, a.sturmflut());
                i = setGeom(st, i, bs);
                i = setGeom(st, i, a.rueckgangFlaeche());
                setL(st, i++, sitzung);
                setL(st, i, sitzung);
                st.executeUpdate();
            }
            long id = neueId(c, "SY_ABBRUCH_SEQ");
            Daten.Kantenstueck k = a.kante();
            if (k != null)
                try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_KLIFFKANTE (STAND, ABBRUCH_ID, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, "
                        + "SZENE_MS, SZENE_ZEIT, SITZUNG_ID, HERKUNFT) VALUES ('ABBRUCH', ?, ?, ?, " + geomSql(k.oberkante()) + ", " + geomSql(k.fuss()) + ", ?, ?, ?, " + TSTZ
                        + ", ?, 'EIGEN')")) {
                    st.setLong(1, id);
                    setD(st, 2, r1(k.nordVon()));
                    setD(st, 3, r1(k.nordBis()));
                    int i = setGeom(st, 4, k.oberkante());
                    i = setGeom(st, i, k.fuss());
                    setD(st, i++, r1(k.wandMax()));
                    setD(st, i++, r1(k.oberkanteMax()));
                    setMs(st, i++, a.ms());
                    i = setTz(st, i, a.ms());
                    setL(st, i, sitzung);
                    st.executeUpdate();
                }
            if (!a.findlinge().isEmpty())
                try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_FINDLING (ABBRUCH_ID, LAGE, HALBMESSER_M, SZENE_MS, SITZUNG_ID) VALUES (?, "
                        + "MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(?, ?, NULL), NULL, NULL), ?, ?, ?)")) {
                    for (double[] f : a.findlinge()) {
                        double[] u = Daten.utm(f[0], f[1]);
                        st.setLong(1, id);
                        st.setDouble(2, Math.round(u[0] * 100) / 100.0);
                        st.setDouble(3, Math.round(u[1] * 100) / 100.0);
                        setD(st, 4, f.length > 2 ? r1(f[2]) : null);
                        st.setLong(5, a.ms());
                        setL(st, 6, sitzung);
                        st.addBatch();
                    }
                    st.executeBatch();
                }
            return id;
        });
    }

    @Override public List<Daten.Abbruch> abbrueche(int n) throws Fehler {
        return mit("Abbrüche", c -> {
            List<Daten.Abbruch> l = new ArrayList<>();
            try (PreparedStatement st = c.prepareStatement("SELECT a.*, a.BRUCHSTELLE.SDO_POINT.X AS BRX, a.BRUCHSTELLE.SDO_POINT.Y AS BRY, "
                    + "MDSYS.SDO_UTIL.TO_WKTGEOMETRY(a.RUECKGANG) AS RWKT, k.ID AS KID, k.NORD_VON AS KV, k.NORD_BIS AS KB, MDSYS.SDO_UTIL.TO_WKTGEOMETRY(k.OBERKANTE) AS OWKT, "
                    + "MDSYS.SDO_UTIL.TO_WKTGEOMETRY(k.FUSS) AS FWKT, k.WAND_MAX_M AS KW, k.OBERKANTE_MAX_M AS KO FROM SY_ABBRUCH a "
                    + "LEFT JOIN SY_KLIFFKANTE k ON k.ABBRUCH_ID = a.ID ORDER BY a.SZENE_MS DESC, a.ID DESC FETCH FIRST ? ROWS ONLY")) {
                st.setInt(1, n);
                try (ResultSet r = st.executeQuery()) {
                    while (r.next()) {
                        Double bx = dbl(r, "BRX"), by = dbl(r, "BRY");
                        long ms = r.getLong("SZENE_MS");
                        Long kid = ms(r, "KID");
                        Daten.Kantenstueck k = kid == null ? null : new Daten.Kantenstueck(kid, "ABBRUCH", r.getLong("ID"), r.getDouble("KV"), r.getDouble("KB"),
                                geom(r, "OWKT"), geom(r, "FWKT"), d(r, "KW"), d(r, "KO"), ms);
                        l.add(new Daten.Abbruch(r.getLong("ID"), ms, d(r, "NORD"), d(r, "NORD_VON"), d(r, "NORD_BIS"), d(r, "LAENGE_M"), d(r, "VOLUMEN_M3"), d(r, "BLOECKE_M3"),
                                d(r, "ZERFALLEN_M3"), d(r, "AUSGESPUELT_M3"), d(r, "KEGEL_M3"), d(r, "KEHLE_M"), d(r, "LC_M"), d(r, "PEGEL_M"), d(r, "AUFLAUF_M"),
                                d(r, "RUECKGANG_M"), r.getString("STURMFLUT"), bx == null || by == null ? null : Daten.xz(bx, by), geom(r, "RWKT"), k, null));
                    }
                }
            }
            return l;
        });
    }

    @Override public List<Daten.Kantenstueck> kanten() throws Fehler {
        return mit("Kanten", c -> {
            List<Daten.Kantenstueck> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT ID, STAND, ABBRUCH_ID, NORD_VON, NORD_BIS, MDSYS.SDO_UTIL.TO_WKTGEOMETRY(OBERKANTE) AS OWKT, "
                    + "MDSYS.SDO_UTIL.TO_WKTGEOMETRY(FUSS) AS FWKT, WAND_MAX_M, OBERKANTE_MAX_M, SZENE_MS FROM SY_KLIFFKANTE ORDER BY NVL(SZENE_MS, 0), ID")) {
                while (r.next())
                    l.add(new Daten.Kantenstueck(r.getLong(1), r.getString(2), ms(r, "ABBRUCH_ID"), r.getDouble(4), r.getDouble(5), geom(r, "OWKT"), geom(r, "FWKT"),
                            d(r, "WAND_MAX_M"), d(r, "OBERKANTE_MAX_M"), ms(r, "SZENE_MS")));
            }
            return l;
        });
    }

    @Override public long sturmflutLauf(Daten.SturmflutLauf l, Long sitzung) throws Fehler {
        return zusammen("Lauf der Sturmflut", c -> {
            boolean neu = l.id() <= 0;
            String werte = "STURMFLUT_ID = (SELECT MAX(ID) FROM SY_STURMFLUT WHERE NAME = ?), NAME = ?, BEGINN_MS = ?, BEGINN_ZEIT = " + TSTZ + ", ENDE_MS = ?, ENDE_ZEIT = "
                    + TSTZ + ", SCHEITEL_M = ?, SCHEITEL_MS = ?, WINDSTAU_MAX_M = ?, AUFLAUF_MAX_M = ?, STUNDEN_AM_FUSS = ?, KEHLE_MAX_M = ?, ABBRUECHE = ?";
            String sql = neu ? "INSERT INTO SY_STURMFLUT_LAUF (STURMFLUT_ID, NAME, BEGINN_MS, BEGINN_ZEIT, ENDE_MS, ENDE_ZEIT, SCHEITEL_M, SCHEITEL_MS, WINDSTAU_MAX_M, "
                    + "AUFLAUF_MAX_M, STUNDEN_AM_FUSS, KEHLE_MAX_M, ABBRUECHE, SITZUNG_ID) VALUES ((SELECT MAX(ID) FROM SY_STURMFLUT WHERE NAME = ?), ?, ?, " + TSTZ + ", ?, "
                    + TSTZ + ", ?, ?, ?, ?, ?, ?, ?, ?)"
                    : "UPDATE SY_STURMFLUT_LAUF SET " + werte + " WHERE ID = ?";
            try (PreparedStatement st = c.prepareStatement(sql)) {
                setS(st, 1, l.belegt());
                st.setString(2, l.name());
                st.setLong(3, l.beginnMs());
                int i = setTz(st, 4, l.beginnMs());
                setMs(st, i++, l.endeMs());
                i = setTz(st, i, l.endeMs());
                setD(st, i++, r3(l.scheitel()));
                setMs(st, i++, l.scheitelMs());
                setD(st, i++, r3(l.windstauMax()));
                setD(st, i++, r3(l.auflaufMax()));
                setD(st, i++, r2(l.stundenAmFuss()));
                setD(st, i++, r2(l.kehleMax()));
                st.setInt(i++, l.abbrueche());
                if (neu) setL(st, i, sitzung); else st.setLong(i, l.id());
                st.executeUpdate();
            }
            return neu ? neueId(c, "SY_STURMFLUT_LAUF_SEQ") : l.id();
        });
    }

    @Override public List<Daten.SturmflutLauf> sturmflutLaeufe(int n) throws Fehler {
        return mit("Läufe der Sturmflut", c -> {
            List<Daten.SturmflutLauf> l = new ArrayList<>();
            try (PreparedStatement st = c.prepareStatement("SELECT x.*, s.NAME AS BELEGT FROM SY_STURMFLUT_LAUF x LEFT JOIN SY_STURMFLUT s ON s.ID = x.STURMFLUT_ID "
                    + "ORDER BY x.ID DESC FETCH FIRST ? ROWS ONLY")) {
                st.setInt(1, n);
                try (ResultSet r = st.executeQuery()) {
                    while (r.next())
                        l.add(new Daten.SturmflutLauf(r.getLong("ID"), r.getString("BELEGT"), r.getString("NAME"), r.getLong("BEGINN_MS"), ms(r, "ENDE_MS"), d(r, "SCHEITEL_M"),
                                ms(r, "SCHEITEL_MS"), d(r, "WINDSTAU_MAX_M"), d(r, "AUFLAUF_MAX_M"), d(r, "STUNDEN_AM_FUSS"), d(r, "KEHLE_MAX_M"), r.getInt("ABBRUECHE")));
                }
            }
            return l;
        });
    }

    @Override public long vorspuelung(Daten.Vorspuelung v, Long sitzung) throws Fehler {
        return zusammen("Vorspülung", c -> {
            boolean neu = v.id() <= 0;
            String sql = neu ? "INSERT INTO SY_VORSPUELUNG (NAME, ART, JAHR, MENGE_M3, NORD_VON, NORD_BIS, STRECKE_TEXT, FLAECHE, BEGINN_MS, SZENE_ZEIT, HOEHE_FUSS_M, BREITE_M, "
                    + "HALBWERT_TAGE, BEMERKUNG, SITZUNG_ID, HERKUNFT) VALUES (?, 'APP', ?, ?, ?, ?, ?, " + geomSql(v.flaeche()) + ", ?, " + TSTZ + ", ?, ?, ?, ?, ?, 'EIGEN')"
                    : "UPDATE SY_VORSPUELUNG SET NAME = ?, JAHR = ?, MENGE_M3 = ?, NORD_VON = ?, NORD_BIS = ?, STRECKE_TEXT = ?, FLAECHE = " + geomSql(v.flaeche())
                    + ", BEGINN_MS = ?, SZENE_ZEIT = " + TSTZ + ", HOEHE_FUSS_M = ?, BREITE_M = ?, HALBWERT_TAGE = ?, BEMERKUNG = ? WHERE ID = ? AND HERKUNFT = 'EIGEN'";
            try (PreparedStatement st = c.prepareStatement(sql)) {
                st.setString(1, v.name());
                if (v.jahr() == null) st.setNull(2, Types.NUMERIC); else st.setInt(2, v.jahr());
                setMs(st, 3, v.menge());
                setD(st, 4, v.nordVon());
                setD(st, 5, v.nordBis());
                setS(st, 6, v.strecke());
                int i = setGeom(st, 7, v.flaeche());
                setMs(st, i++, v.beginnMs());
                i = setTz(st, i, v.beginnMs());
                setD(st, i++, v.hoeheFuss());
                setD(st, i++, v.breite());
                setD(st, i++, v.halbwertTage());
                setS(st, i++, v.bemerkung());
                if (neu) setL(st, i, sitzung); else st.setLong(i, v.id());
                st.executeUpdate();
            }
            return neu ? neueId(c, "SY_VORSPUELUNG_SEQ") : v.id();
        });
    }

    @Override public List<Daten.Vorspuelung> vorspuelungen() throws Fehler {
        return mit("Vorspülungen", c -> {
            List<Daten.Vorspuelung> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT v.*, MDSYS.SDO_UTIL.TO_WKTGEOMETRY(v.FLAECHE) AS WKT, q.KURZ AS QUELLE "
                    + "FROM SY_VORSPUELUNG v LEFT JOIN SY_QUELLE q ON q.ID = v.QUELLE_ID ORDER BY CASE v.ART WHEN 'BELEGT' THEN 0 ELSE 1 END, "
                    + "CASE v.ART WHEN 'BELEGT' THEN v.JAHR ELSE -v.ID END, v.ID")) {
                while (r.next()) {
                    Long m = ms(r, "MENGE_M3");
                    l.add(new Daten.Vorspuelung(r.getLong("ID"), r.getString("NAME"), r.getString("ART"), integer(r, "JAHR"), m, dbl(r, "NORD_VON"), dbl(r, "NORD_BIS"),
                            r.getString("STRECKE_TEXT"), geom(r, "WKT"), ms(r, "BEGINN_MS"), dbl(r, "HOEHE_FUSS_M"), dbl(r, "BREITE_M"), dbl(r, "HALBWERT_TAGE"),
                            r.getString("BEMERKUNG"), r.getString("QUELLE")));
                }
            }
            return l;
        });
    }

    @Override public void serie(Daten.Serie s, Long sitzung) throws Fehler {
        Daten.Geom lage = Daten.Geom.punkt(s.x(), s.z());
        zusammen("Serie", c -> {
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_SERIE (BEGINN_MS, SZENE_ZEIT, DAUER_S, WELLEN, SERIEN, GROESSTE, HS_M, TP_S, LAGE, SITZUNG_ID) "
                    + "VALUES (?, " + TSTZ + ", ?, ?, ?, ?, ?, ?, " + geomSql(lage) + ", ?)")) {
                st.setLong(1, s.ms());
                int i = setTz(st, 2, s.ms());
                setD(st, i++, r1(s.dauerS()));
                st.setInt(i++, s.wellen());
                st.setInt(i++, s.serien());
                st.setInt(i++, s.groesste());
                setD(st, i++, r2(s.hs()));
                setD(st, i++, r2(s.tp()));
                i = setGeom(st, i, lage);
                setL(st, i, sitzung);
                st.executeUpdate();
            }
            return null;
        });
    }

    @Override public void regielauf(Daten.Regielauf l, Long sitzung) throws Fehler {
        zusammen("Lauf eines Regiebuchs", c -> {
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_REGIELAUF (REGIEBUCH_ID, NAME, BEGINN, ENDE, VOLLSTAENDIG, KINO, BILDER_S_MITTEL, GPU_MS_MITTEL, "
                    + "GEWARTET_S, AUSGEWICHEN, FOTOS, QUALITAET, BLATT, BLATT_DATEI, SITZUNG_ID) VALUES (" + REGIEBUCH_ID + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                st.setString(1, l.name());
                st.setString(2, l.name());
                st.setTimestamp(3, new Timestamp(l.beginnMs()));
                if (l.endeMs() == null) st.setNull(4, Types.TIMESTAMP); else st.setTimestamp(4, new Timestamp(l.endeMs()));
                st.setInt(5, l.vollstaendig() ? 1 : 0);
                st.setInt(6, l.kino() ? 1 : 0);
                setD(st, 7, r2(l.bilderMittel()));
                setD(st, 8, r3(l.gpuMs()));
                setD(st, 9, r1(l.gewartetS()));
                st.setInt(10, l.ausgewichen());
                st.setInt(11, l.fotos());
                setS(st, 12, l.qualitaet());
                if (l.blatt() == null) st.setNull(13, Types.BLOB); else st.setBytes(13, l.blatt());
                setS(st, 14, l.blattDatei());
                setL(st, 15, sitzung);
                st.executeUpdate();
            }
            return null;
        });
    }

    @Override public int chronikLeeren(long sitzung) throws Fehler {
        return zusammen("Chronik leeren", c -> {
            int n = 0;
            for (String t : new String[]{"SY_FINDLING", "SY_KLIFFKANTE", "SY_ABBRUCH", "SY_STURMFLUT_LAUF", "SY_SERIE", "SY_REGIELAUF"})
                try (PreparedStatement st = c.prepareStatement("DELETE FROM " + t + " WHERE SITZUNG_ID = ?" + (t.equals("SY_KLIFFKANTE") ? " AND HERKUNFT = 'EIGEN'" : ""))) {
                    st.setLong(1, sitzung);
                    n += st.executeUpdate();
                }
            try (PreparedStatement st = c.prepareStatement("DELETE FROM SY_VORSPUELUNG WHERE SITZUNG_ID = ? AND ART = 'APP'")) { st.setLong(1, sitzung); n += st.executeUpdate(); }
            return n;
        });
    }

    // ------------------------------------------------------------------ Regiebücher

    @Override public List<Daten.Regiebuch> regiebuecher() throws Fehler {
        return mit("Regiebücher", c -> {
            List<Daten.Regiebuch> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT NAME, TEXT, BESCHREIBUNG, DATEI, HERKUNFT FROM SY_REGIEBUCH "
                    + "ORDER BY CASE HERKUNFT WHEN 'MITGELIEFERT' THEN 0 ELSE 1 END, ID")) {
                while (r.next()) l.add(new Daten.Regiebuch(r.getString(1), clob(r, "TEXT"), r.getString(3), r.getString(4), r.getString(5)));
            }
            return l;
        });
    }

    @Override public void speichern(Daten.Regiebuch rb) throws Fehler {
        Regie.Book b;
        try { b = Regie.parse(rb.text()); } catch (RuntimeException e) { throw new Fehler("Regiebuch nicht lesbar: " + e.getMessage(), e); }
        if (!b.name().equals(rb.name())) throw new Fehler("Der Name im Text („" + b.name() + "“) passt nicht zu „" + rb.name() + "“");
        Startdaten.Kopf k = Startdaten.Kopf.aus(b);
        zusammen("Regiebuch speichern", c -> {
            if (mitgeliefert(c, "SY_REGIEBUCH", b.name())) throw new SQLException("„" + b.name() + "“ ist mitgeliefert; bitte unter einem eigenen Namen speichern");
            try (PreparedStatement st = c.prepareStatement("DELETE FROM SY_REGIEBUCH WHERE NAME = ? AND HERKUNFT = 'EIGEN'")) { st.setString(1, b.name()); st.executeUpdate(); }
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_REGIEBUCH (NAME, BESCHREIBUNG, DATEI, SZENE_MS, SZENE_ZEIT, ZEITRAFFER, LAENGE_S, FOTOS, WARTEN, BLATT, "
                    + "TEXT, HERKUNFT) VALUES (?, ?, NULL, ?, " + TSTZ + ", ?, ?, ?, ?, ?, ?, 'EIGEN')")) {
                st.setString(1, b.name());
                setS(st, 2, b.about());
                setMs(st, 3, k.szeneMs());
                int i = setTz(st, 4, k.szeneMs());
                setD(st, i++, k.raffer());
                st.setDouble(i++, b.length());
                st.setInt(i++, k.fotos());
                st.setInt(i++, k.warten());
                st.setInt(i++, k.blatt() ? 1 : 0);
                setClob(st, i, rb.text());
                st.executeUpdate();
            }
            long id = Long.parseLong(Db.einWert(c, "SELECT TO_CHAR(ID) FROM SY_REGIEBUCH WHERE NAME = ?", b.name()));
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) VALUES (?, ?, ?, ?, ?, ?)")) {
                int nr = 0;
                for (Regie.Zeile z : b.zeilen()) {
                    st.setLong(1, id);
                    st.setInt(2, ++nr);
                    st.setDouble(3, z.sekunde());
                    st.setString(4, z.befehl().toUpperCase(Locale.ROOT));
                    setS(st, 5, z.argumente());
                    setS(st, 6, z.text());
                    st.addBatch();
                }
                st.executeBatch();
            }
            return null;
        });
    }

    @Override public void loeschenRegiebuch(String name) throws Fehler { loeschen("SY_REGIEBUCH", name); }

    // ------------------------------------------------------------------ Fahrten

    @Override public List<Daten.Fahrt> fahrten() throws Fehler {
        return mit("Fahrten", c -> {
            Map<Long, List<Daten.Stuetzpunkt>> p = new LinkedHashMap<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT FAHRT_ID, NR, ZEIT_S, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD "
                    + "FROM SY_STUETZPUNKT ORDER BY FAHRT_ID, NR")) {
                while (r.next())
                    p.computeIfAbsent(r.getLong(1), k -> new ArrayList<>()).add(new Daten.Stuetzpunkt(r.getInt(2), r.getDouble(3), d(r, "KAM_X"), d(r, "KAM_Y"), d(r, "KAM_Z"),
                            d(r, "KAM_GIER_GRAD"), d(r, "KAM_NICK_GRAD")));
            }
            List<Daten.Fahrt> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT ID, NAME, ART, DAUER_S, LAENGE_M, BESCHREIBUNG, HERKUNFT FROM SY_FAHRT "
                    + "ORDER BY CASE HERKUNFT WHEN 'MITGELIEFERT' THEN 0 ELSE 1 END, ID")) {
                while (r.next())
                    l.add(new Daten.Fahrt(r.getString(2), r.getString(3), dbl(r, "DAUER_S"), dbl(r, "LAENGE_M"), r.getString(6), r.getString(7),
                            p.getOrDefault(r.getLong(1), List.of())));
            }
            return l;
        });
    }

    // ------------------------------------------------------------------ Ansichten

    private static final String ANSICHT_SQL = "SELECT ID, NAME, TASTE, ART, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, SZENE_MS, HERKUNFT";

    private static Daten.Ansicht ansicht(ResultSet r, String zustand, byte[] png) throws SQLException {
        double[] k = {d(r, "KAM_X"), d(r, "KAM_Y"), d(r, "KAM_Z"), d(r, "KAM_GIER_GRAD"), d(r, "KAM_NICK_GRAD")};
        return new Daten.Ansicht(r.getLong("ID"), r.getString("NAME"), integer(r, "TASTE"), r.getString("ART"), k, dbl(r, "OEFFNUNG_GRAD"), dbl(r, "AUGE_UEBER_GRUND_M"),
                ms(r, "SZENE_MS"), zustand, png, r.getString("HERKUNFT"));
    }

    @Override public List<Daten.Ansicht> ansichten() throws Fehler {
        return mit("Ansichten", c -> {
            List<Daten.Ansicht> l = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(ANSICHT_SQL + " FROM SY_ANSICHT ORDER BY CASE ART WHEN 'FEST' THEN 0 WHEN 'BLICK' THEN 1 "
                    + "WHEN 'VORLAGE' THEN 2 ELSE 3 END, TASTE, CASE ART WHEN 'GEMERKT' THEN -ID ELSE ID END")) {
                while (r.next()) l.add(ansicht(r, null, null));
            }
            return l;
        });
    }

    @Override public Daten.Ansicht ansicht(long id) throws Fehler {
        return mit("Ansicht", c -> {
            try (PreparedStatement st = c.prepareStatement(ANSICHT_SQL + ", ZUSTAND, VORSCHAU FROM SY_ANSICHT WHERE ID = ?")) {
                st.setLong(1, id);
                try (ResultSet r = st.executeQuery()) {
                    if (!r.next()) throw new SQLException("Ansicht " + id + " gibt es nicht");
                    return ansicht(r, clob(r, "ZUSTAND"), r.getBytes("VORSCHAU"));
                }
            }
        });
    }

    @Override public long speichern(Daten.Ansicht a, Long sitzung) throws Fehler {
        Daten.Geom lage = Daten.Geom.punkt(a.kamera()[0], a.kamera()[2]);
        return zusammen("Ansicht merken", c -> {
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_ANSICHT (NAME, ART, KAM_X, KAM_Y, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, LAGE, "
                    + "SZENE_MS, SZENE_ZEIT, ZUSTAND, VORSCHAU, SITZUNG_ID, HERKUNFT) VALUES (?, 'GEMERKT', ?, ?, ?, ?, ?, ?, " + geomSql(lage) + ", ?, " + TSTZ
                    + ", ?, ?, ?, 'EIGEN')")) {
                st.setString(1, a.name());
                for (int i = 0; i < 5; i++) setD(st, 2 + i, r2(a.kamera()[i]));
                setD(st, 7, a.oeffnung());
                int i = setGeom(st, 8, lage);
                setMs(st, i++, a.szeneMs());
                i = setTz(st, i, a.szeneMs());
                setClob(st, i++, a.zustand());
                if (a.vorschau() == null) st.setNull(i++, Types.BLOB); else st.setBytes(i++, a.vorschau());
                setL(st, i, sitzung);
                st.executeUpdate();
            }
            return neueId(c, "SY_ANSICHT_SEQ");
        });
    }

    @Override public void loeschenAnsicht(long id) throws Fehler {
        zusammen("Ansicht löschen", c -> {
            try (PreparedStatement st = c.prepareStatement("DELETE FROM SY_ANSICHT WHERE ID = ? AND HERKUNFT = 'EIGEN'")) { st.setLong(1, id); st.executeUpdate(); }
            return null;
        });
    }

    // ------------------------------------------------------------------ Presets

    @Override public List<Daten.PaketPreset> presets(String paket, String klasse) throws Fehler {
        return mit("Presets", c -> {
            Map<Long, Map<String, String>> w = new LinkedHashMap<>();
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT PAKET_PRESET_ID, FELD, WERT FROM SY_PAKET_WERT ORDER BY PAKET_PRESET_ID, ID")) {
                while (r.next()) w.computeIfAbsent(r.getLong(1), k -> new LinkedHashMap<>()).put(r.getString(2), r.getString(3) == null ? "" : r.getString(3));
            }
            List<Daten.PaketPreset> l = new ArrayList<>();
            try (PreparedStatement st = c.prepareStatement("SELECT ID, PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT FROM SY_PAKET_PRESET WHERE (? IS NULL OR PAKET = ?) "
                    + "AND (? IS NULL OR KLASSE = ?) ORDER BY PAKET, KLASSE, CASE HERKUNFT WHEN 'MITGELIEFERT' THEN 0 ELSE 1 END, NAME")) {
                st.setString(1, paket);
                st.setString(2, paket);
                st.setString(3, klasse);
                st.setString(4, klasse);
                try (ResultSet r = st.executeQuery()) {
                    while (r.next())
                        l.add(new Daten.PaketPreset(r.getString(2), r.getString(3), r.getString(4), r.getString(5), r.getString(6), w.getOrDefault(r.getLong(1), Map.of())));
                }
            }
            return l;
        });
    }

    @Override public void speichern(Daten.PaketPreset p) throws Fehler {
        Map<String, String> typ = Startdaten.typen(p);
        zusammen("Preset speichern", c -> {
            if (zahl(c, "SELECT COUNT(*) FROM SY_PAKET_PRESET WHERE PAKET = ? AND KLASSE = ? AND NAME = ? AND HERKUNFT = 'MITGELIEFERT'", p.paket(), p.klasse(), p.name()) > 0)
                throw new SQLException("„" + p.name() + "“ ist mitgeliefert; bitte unter einem eigenen Namen speichern");
            try (PreparedStatement st = c.prepareStatement("DELETE FROM SY_PAKET_PRESET WHERE PAKET = ? AND KLASSE = ? AND NAME = ? AND HERKUNFT = 'EIGEN'")) {
                st.setString(1, p.paket()); st.setString(2, p.klasse()); st.setString(3, p.name()); st.executeUpdate();
            }
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_PAKET_PRESET (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT) VALUES (?, ?, ?, ?, 'EIGEN')")) {
                st.setString(1, p.paket()); st.setString(2, p.klasse()); st.setString(3, p.name()); setS(st, 4, p.beschreibung()); st.executeUpdate();
            }
            long id = neueId(c, "SY_PAKET_PRESET_SEQ");
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) VALUES (?, ?, ?, ?)")) {
                for (Map.Entry<String, String> e : p.felder().entrySet()) {
                    st.setLong(1, id);
                    st.setString(2, e.getKey());
                    st.setString(3, typ.getOrDefault(e.getKey(), "STRING"));
                    setS(st, 4, e.getValue());
                    st.addBatch();
                }
                st.executeBatch();
            }
            return null;
        });
    }

    @Override public void loeschenPreset(String paket, String klasse, String name) throws Fehler {
        zusammen("Preset löschen", c -> {
            try (PreparedStatement st = c.prepareStatement("DELETE FROM SY_PAKET_PRESET WHERE PAKET = ? AND KLASSE = ? AND NAME = ? AND HERKUNFT = 'EIGEN'")) {
                st.setString(1, paket); st.setString(2, klasse); st.setString(3, name);
                if (st.executeUpdate() == 0) throw new SQLException("„" + name + "“ ist mitgeliefert oder gibt es nicht");
            }
            return null;
        });
    }

    // ------------------------------------------------------------------ Sitzungen

    @Override public long sitzungBeginnen(String version, String gpu, String qualitaet) throws Fehler {
        return zusammen("Sitzung beginnen", c -> {
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_SITZUNG (RECHNER, BENUTZER, APP_VERSION, GPU, QUALITAET) VALUES (?, ?, ?, ?, ?)")) {
                setS(st, 1, com.dan.store.DateiAblage.rechner());
                setS(st, 2, System.getProperty("user.name", ""));
                st.setString(3, version.length() > 20 ? version.substring(0, 20) : version);
                setS(st, 4, gpu == null ? null : gpu.length() > 120 ? gpu.substring(0, 120) : gpu);
                setS(st, 5, qualitaet);
                st.executeUpdate();
            }
            long id = neueId(c, "SY_SITZUNG_SEQ");
            try (Statement st = c.createStatement()) { st.executeUpdate("DELETE FROM SY_LOG WHERE ZEIT < SYSTIMESTAMP - INTERVAL '90' DAY"); }
            return id;
        });
    }

    @Override public void sitzungSichern(long id, Sicherung s) throws Fehler {
        Map<String, String> sp = s.spalten();
        double rs = Zahl.lies(sp.get(REGIE_SEKUNDE)), zr = Zahl.lies(sp.get(ZEITRAFFER)), ms = Zahl.lies(sp.get(SZENE_MS)), pg = Zahl.lies(sp.get(PEGEL)),
                ws = Zahl.lies(sp.get(WINDSTAU)), ab = Zahl.lies(sp.get(ABBRUECHE));
        Long szene = Double.isNaN(ms) ? null : (long) ms;
        zusammen("Sitzung sichern", c -> {
            try (PreparedStatement st = c.prepareStatement("UPDATE SY_SITZUNG SET ZUSTAND = ?, ZUSTAND_ZEIT = SYSTIMESTAMP, REGIEBUCH_ID = " + REGIEBUCH_ID + ", REGIE_SEKUNDE = ?, "
                    + "SZENE_MS = ?, SZENE_ZEIT = " + TSTZ + ", ZEITRAFFER = ?, LAGE = ?, PEGEL_M = ?, WINDSTAU_M = ?, STURMFLUT = ?, ABBRUECHE = ? WHERE ID = ?")) {
                setClob(st, 1, s.zustand());
                setS(st, 2, sp.get(REGIEBUCH));
                setD(st, 3, rs);
                setMs(st, 4, szene);
                int i = setTz(st, 5, szene);
                setD(st, i++, zr);
                String lage = sp.get(LAGE);
                setS(st, i++, lage == null ? null : lage.length() > 30 ? lage.substring(0, 30) : lage);
                setD(st, i++, r3(pg));
                setD(st, i++, r3(ws));
                st.setInt(i++, "true".equals(sp.get(STURMFLUT)) ? 1 : 0);
                if (Double.isNaN(ab)) st.setNull(i++, Types.NUMERIC); else st.setInt(i++, (int) ab);
                st.setLong(i, id);
                st.executeUpdate();
            }
            return null;
        });
    }

    @Override public void vorschau(long sitzung, byte[] png) throws Fehler {
        zusammen("Vorschau", c -> {
            try (PreparedStatement st = c.prepareStatement("UPDATE SY_SITZUNG SET VORSCHAU = ? WHERE ID = ?")) {
                st.setBytes(1, png);
                st.setLong(2, sitzung);
                st.executeUpdate();
            }
            return null;
        });
    }

    @Override public void sitzungBeenden(long id) throws Fehler {
        zusammen("Sitzung beenden", c -> {
            try (PreparedStatement st = c.prepareStatement("UPDATE SY_SITZUNG SET ENDE = SYSTIMESTAMP, SAUBER_BEENDET = 1 WHERE ID = ?")) { st.setLong(1, id); st.executeUpdate(); }
            return null;
        });
    }

    private static Daten.Sitzung letzte(Connection c, long ausser) throws SQLException {
        try (PreparedStatement st = c.prepareStatement("SELECT s.ID, s.BEGINN, s.ENDE, s.SAUBER_BEENDET, s.ZUSTAND, s.ZUSTAND_ZEIT, r.NAME AS REGIEBUCH, s.REGIE_SEKUNDE, "
                + "s.SZENE_MS, s.LAGE, s.PEGEL_M, s.ABBRUECHE, s.STURMFLUT, s.VORSCHAU FROM SY_SITZUNG s LEFT JOIN SY_REGIEBUCH r ON r.ID = s.REGIEBUCH_ID "
                + "WHERE s.ZUSTAND IS NOT NULL AND s.ID <> ? ORDER BY s.ZUSTAND_ZEIT DESC NULLS LAST, s.ID DESC FETCH FIRST 1 ROWS ONLY")) {
            st.setLong(1, ausser);
            try (ResultSet r = st.executeQuery()) {
                if (!r.next()) return null;
                Timestamp b = r.getTimestamp(2), e = r.getTimestamp(3), zz = r.getTimestamp(6);
                return new Daten.Sitzung(r.getLong(1), b.getTime(), e == null ? null : e.getTime(), r.getInt(4) == 1, clob(r, "ZUSTAND"), zz == null ? null : zz.getTime(),
                        r.getString("REGIEBUCH"), dbl(r, "REGIE_SEKUNDE"), ms(r, "SZENE_MS"), r.getString("LAGE"), dbl(r, "PEGEL_M"), integer(r, "ABBRUECHE"),
                        r.getInt("STURMFLUT") == 1, r.getBytes("VORSCHAU"));
            }
        }
    }

    @Override public Daten.Sitzung letzteSitzung(long ausser) throws Fehler { return mit("Letzte Sitzung", c -> letzte(c, ausser)); }

    // ------------------------------------------------------------------ Protokoll

    @Override public void protokoll(List<Protokoll.Zeile> zeilen, Long sitzung) throws Fehler {
        if (zeilen.isEmpty()) return;
        zusammen("Protokoll", c -> {
            List<Long> ids = new ArrayList<>();
            try (PreparedStatement st = c.prepareStatement("SELECT SY_LOG_SEQ.NEXTVAL FROM DUAL CONNECT BY LEVEL <= ?")) {
                st.setInt(1, zeilen.size());
                try (ResultSet r = st.executeQuery()) { while (r.next()) ids.add(r.getLong(1)); }
            }
            try (PreparedStatement st = c.prepareStatement("INSERT INTO SY_LOG (ID, SITZUNG_ID, ZEIT, STUFE, QUELLE, TEXT, SZENENZEIT, ORT, DETAIL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (int i = 0; i < zeilen.size(); i++) {
                    Protokoll.Zeile z = zeilen.get(i);
                    st.setLong(1, ids.get(i));
                    setL(st, 2, sitzung);
                    st.setTimestamp(3, new Timestamp(z.zeitMs()));
                    st.setString(4, z.stufe());
                    st.setString(5, z.quelle().length() > 60 ? z.quelle().substring(0, 60) : z.quelle());
                    st.setString(6, z.text().length() > 1000 ? z.text().substring(0, 997) + " …" : z.text());
                    setS(st, 7, z.szenenzeit() == null ? null : z.szenenzeit().length() > 40 ? z.szenenzeit().substring(0, 40) : z.szenenzeit());
                    setS(st, 8, z.ort() == null ? null : z.ort().length() > 40 ? z.ort().substring(0, 40) : z.ort());
                    setClob(st, 9, z.detail());
                    st.addBatch();
                }
                st.executeBatch();
            }
            return null;
        });
    }

    @Override public List<String> letzteZeilen(int n) throws Fehler {
        return mit("Protokoll lesen", c -> {
            List<String> l = new ArrayList<>();
            try (PreparedStatement st = c.prepareStatement("SELECT ZEIT, STUFE, QUELLE, TEXT FROM SY_LOG ORDER BY ID DESC FETCH FIRST ? ROWS ONLY")) {
                st.setInt(1, n);
                try (ResultSet r = st.executeQuery()) {
                    while (r.next())
                        l.add(0, Protokoll.zeile(new Protokoll.Zeile(r.getTimestamp(1).getTime(), r.getString(2), r.getString(3), r.getString(4), null, null, null)));
                }
            }
            return l;
        });
    }

    @Override public synchronized void schliessen() {
        try { if (c != null) c.close(); } catch (SQLException e) { /* zu */ }
        c = null;
    }
}
