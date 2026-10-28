-- =====================================================================================================
-- Sylt · Datenbank · 03_sichten.sql
-- Zwölf Sichten. Mit Geometrie (für QGIS in 8307, WGS 84, aus den Tabellen in 25832 umgerechnet, F35 A):
--   SY_KANTE_V        die Kante des Kliffs zu jedem Zeitpunkt (F36 A): Stand 2024 und jedes Stück nach einem
--                     Abbruch mit GUELTIG_AB_MS/GUELTIG_BIS_MS; QGIS-Filter für einen Zeitpunkt t (ms seit 1970):
--                     "GUELTIG_AB_MS" <= t AND ("GUELTIG_BIS_MS" IS NULL OR "GUELTIG_BIS_MS" > t)
--   SY_RUECKGANG_V    die abgebrochenen Flächen je Abbruch mit Jahr und Rückgang
--   SY_ABBRUCH_V      die Bruchstellen mit allen Werten der Chronik
--   SY_FINDLING_V     die Spur der Eiszeit
--   SY_VORSPUELUNG_V  die Vorspülungen der App als Fläche (die belegten haben keine Fläche, nur Strecke)
--   SY_LEUCHTFEUER_V  die beiden Türme mit ihren Sektoren als Text
--   SY_ANSICHT_V      die Standorte der Ansichten (feste, Blicke, Vorlagen, gemerkte)
--   SY_KUESTE_V       alles zusammen als eine Ebene (Schlüssel OBJEKT_ID, Stil nach EBENE)
-- Ohne Geometrie: SY_RUECKGANG_JAHR_V (Bilanz je Szenenjahr), SY_STURMFLUT_V (belegte und gespielte
-- Sturmfluten), SY_BEFUND_V (Befunde mit Quellen), SY_LETZTE_SITZUNG_V (für „Weiter wie zuletzt“).
-- Szenenzeit (F34 A): SZENE_MS wie gespeichert, SZENE_ZEIT in Europe/Berlin.
-- CREATE OR REPLACE: beliebig oft wiederholbar.
-- =====================================================================================================

-- Die Kante zu jedem Zeitpunkt: ein Stück gilt ab seinem Abbruch (der Stand 2024, in Stücken zu 25 m, ab 0) bis zum
-- nächsten Abbruch, dessen Abschnitt die Mitte des Stücks trifft. Nach „Neu“ beginnt die App wieder beim Stand 2024;
-- wer Sitzungen trennen will, filtert zusätzlich nach SITZUNG_ID.
CREATE OR REPLACE VIEW SY_KANTE_V AS
SELECT k.ID,
       k.STAND,
       k.ABBRUCH_ID,
       k.NORD_VON,
       k.NORD_BIS,
       NVL(k.SZENE_MS, 0) AS GUELTIG_AB_MS,
       (SELECT MIN(n.SZENE_MS) FROM SY_KLIFFKANTE n
         WHERE n.STAND = 'ABBRUCH'
           AND n.SZENE_MS > NVL(k.SZENE_MS, 0)
           AND (k.NORD_VON + k.NORD_BIS) / 2 BETWEEN LEAST(n.NORD_VON, n.NORD_BIS) AND GREATEST(n.NORD_VON, n.NORD_BIS)) AS GUELTIG_BIS_MS,
       k.SZENE_ZEIT,
       k.WAND_MAX_M,
       k.OBERKANTE_MAX_M,
       k.SITZUNG_ID,
       k.HERKUNFT,
       MDSYS.SDO_CS.TRANSFORM(k.OBERKANTE, 8307) AS GEOM
FROM SY_KLIFFKANTE k
WHERE k.OBERKANTE IS NOT NULL;

-- Die abgebrochenen Flächen (zwischen alter und neuer Kante) mit Jahr, Volumen und Rückgang.
CREATE OR REPLACE VIEW SY_RUECKGANG_V AS
SELECT a.ID,
       a.SZENE_MS,
       a.SZENE_ZEIT,
       EXTRACT(YEAR FROM a.SZENE_ZEIT) AS JAHR,
       a.NORD,
       a.LAENGE_M,
       a.RUECKGANG_M,
       ROUND(MDSYS.SDO_GEOM.SDO_AREA(a.RUECKGANG, 0.005), 1) AS FLAECHE_M2,
       a.VOLUMEN_M3,
       a.STURMFLUT,
       a.SITZUNG_ID,
       MDSYS.SDO_CS.TRANSFORM(a.RUECKGANG, 8307) AS GEOM
FROM SY_ABBRUCH a
WHERE a.RUECKGANG IS NOT NULL;

-- Die Bruchstellen mit allen Werten der Chronik (F37 A).
CREATE OR REPLACE VIEW SY_ABBRUCH_V AS
SELECT a.ID,
       a.SZENE_MS,
       a.SZENE_ZEIT,
       a.NORD,
       a.NORD_VON,
       a.NORD_BIS,
       a.LAENGE_M,
       a.VOLUMEN_M3,
       a.BLOECKE_M3,
       a.ZERFALLEN_M3,
       a.AUSGESPUELT_M3,
       a.KEGEL_M3,
       a.KEHLE_M,
       a.LC_M,
       a.PEGEL_M,
       a.AUFLAUF_M,
       a.RUECKGANG_M,
       a.STURMFLUT,
       (SELECT COUNT(*) FROM SY_FINDLING f WHERE f.ABBRUCH_ID = a.ID) AS FINDLINGE,
       a.SITZUNG_ID,
       MDSYS.SDO_CS.TRANSFORM(a.BRUCHSTELLE, 8307) AS GEOM
FROM SY_ABBRUCH a;

-- Die Spur der Eiszeit: Findlinge am Strand mit dem Abbruch, der sie freigelegt hat.
CREATE OR REPLACE VIEW SY_FINDLING_V AS
SELECT f.ID,
       f.ABBRUCH_ID,
       f.HALBMESSER_M,
       f.SZENE_MS,
       a.SZENE_ZEIT,
       a.STURMFLUT,
       f.SITZUNG_ID,
       MDSYS.SDO_CS.TRANSFORM(f.LAGE, 8307) AS GEOM
FROM SY_FINDLING f
LEFT JOIN SY_ABBRUCH a ON a.ID = f.ABBRUCH_ID;

-- Die Vorspülungen der App als Fläche des Körpers.
CREATE OR REPLACE VIEW SY_VORSPUELUNG_V AS
SELECT v.ID,
       v.NAME,
       v.ART,
       v.JAHR,
       v.MENGE_M3,
       v.NORD_VON,
       v.NORD_BIS,
       v.BEGINN_MS,
       v.SZENE_ZEIT,
       v.HOEHE_FUSS_M,
       v.BREITE_M,
       ROUND(MDSYS.SDO_GEOM.SDO_AREA(v.FLAECHE, 0.005)) AS FLAECHE_M2,
       v.SITZUNG_ID,
       MDSYS.SDO_CS.TRANSFORM(v.FLAECHE, 8307) AS GEOM
FROM SY_VORSPUELUNG v
WHERE v.FLAECHE IS NOT NULL;

-- Die Türme mit ihren Sektoren als Text (F40 A), z. B. „W 120–105 20 sm; R 105–120 16 sm“.
CREATE OR REPLACE VIEW SY_LEUCHTFEUER_V AS
SELECT l.ID,
       l.NAME,
       l.KENNUNG,
       l.FEUERHOEHE_M,
       l.TURMHOEHE_M,
       l.TRAGWEITE_SM,
       l.IN_BETRIEB_VON,
       l.IN_BETRIEB_BIS,
       l.HEUTE_AN,
       (SELECT LISTAGG(s.FARBE || ' ' || TO_CHAR(s.VON_GRAD) || '–' || TO_CHAR(s.BIS_GRAD)
                       || NVL2(s.TRAGWEITE_SM, ' ' || TO_CHAR(s.TRAGWEITE_SM) || ' sm', '')
                       || NVL2(s.TEXT, ' ' || s.TEXT, ''), '; ') WITHIN GROUP (ORDER BY s.NR)
          FROM SY_SEKTOR s WHERE s.LEUCHTFEUER_ID = l.ID) AS SEKTOREN,
       l.BESCHREIBUNG,
       q.KURZ AS QUELLE,
       MDSYS.SDO_CS.TRANSFORM(l.LAGE, 8307) AS GEOM
FROM SY_LEUCHTFEUER l
LEFT JOIN SY_QUELLE q ON q.ID = l.QUELLE_ID;

-- Die Standorte der Ansichten.
CREATE OR REPLACE VIEW SY_ANSICHT_V AS
SELECT a.ID,
       a.NAME,
       a.TASTE,
       a.ART,
       a.KAM_GIER_GRAD,
       a.KAM_NICK_GRAD,
       a.OEFFNUNG_GRAD,
       a.AUGE_UEBER_GRUND_M,
       a.SZENE_ZEIT,
       CASE WHEN a.VORSCHAU IS NULL THEN 0 ELSE 1 END AS MIT_VORSCHAU,
       a.HERKUNFT,
       MDSYS.SDO_CS.TRANSFORM(a.LAGE, 8307) AS GEOM
FROM SY_ANSICHT a
WHERE a.LAGE IS NOT NULL;

-- Alles als eine Ebene für QGIS. OBJEKT_ID = Ebene × 10^9 + ID; ZEIT_MS für den Zeitregler.
CREATE OR REPLACE VIEW SY_KUESTE_V AS
SELECT 1000000000 + l.ID AS OBJEKT_ID, 'LEUCHTFEUER' AS EBENE, l.NAME AS TEXT, l.FEUERHOEHE_M AS WERT,
       CAST(NULL AS NUMBER(15)) AS ZEIT_MS, MDSYS.SDO_CS.TRANSFORM(l.LAGE, 8307) AS GEOM
FROM SY_LEUCHTFEUER l WHERE l.LAGE IS NOT NULL
UNION ALL
SELECT 2000000000 + k.ID, 'KANTE_' || k.STAND, TO_CHAR(k.NORD_VON) || '–' || TO_CHAR(k.NORD_BIS), k.OBERKANTE_MAX_M,
       k.SZENE_MS, MDSYS.SDO_CS.TRANSFORM(k.OBERKANTE, 8307)
FROM SY_KLIFFKANTE k WHERE k.OBERKANTE IS NOT NULL
UNION ALL
SELECT 3000000000 + a.ID, 'ABBRUCH', a.STURMFLUT, a.VOLUMEN_M3, a.SZENE_MS, MDSYS.SDO_CS.TRANSFORM(a.BRUCHSTELLE, 8307)
FROM SY_ABBRUCH a WHERE a.BRUCHSTELLE IS NOT NULL
UNION ALL
SELECT 4000000000 + a.ID, 'RUECKGANG', a.STURMFLUT, a.RUECKGANG_M, a.SZENE_MS, MDSYS.SDO_CS.TRANSFORM(a.RUECKGANG, 8307)
FROM SY_ABBRUCH a WHERE a.RUECKGANG IS NOT NULL
UNION ALL
SELECT 5000000000 + f.ID, 'FINDLING', NULL, f.HALBMESSER_M, f.SZENE_MS, MDSYS.SDO_CS.TRANSFORM(f.LAGE, 8307)
FROM SY_FINDLING f WHERE f.LAGE IS NOT NULL
UNION ALL
SELECT 6000000000 + v.ID, 'VORSPUELUNG', v.NAME, v.MENGE_M3, v.BEGINN_MS, MDSYS.SDO_CS.TRANSFORM(v.FLAECHE, 8307)
FROM SY_VORSPUELUNG v WHERE v.FLAECHE IS NOT NULL
UNION ALL
SELECT 7000000000 + s.ID, 'SERIE', TO_CHAR(s.SERIEN) || ' Serien', s.GROESSTE, s.BEGINN_MS, MDSYS.SDO_CS.TRANSFORM(s.LAGE, 8307)
FROM SY_SERIE s WHERE s.LAGE IS NOT NULL
UNION ALL
SELECT 8000000000 + a.ID, 'ANSICHT', a.NAME, a.TASTE, a.SZENE_MS, MDSYS.SDO_CS.TRANSFORM(a.LAGE, 8307)
FROM SY_ANSICHT a WHERE a.LAGE IS NOT NULL;

-- Bilanz je Szenenjahr: Abbrüche, Volumen, Fläche, mittlerer und größter Rückgang.
CREATE OR REPLACE VIEW SY_RUECKGANG_JAHR_V AS
SELECT EXTRACT(YEAR FROM a.SZENE_ZEIT) AS JAHR,
       COUNT(*) AS ABBRUECHE,
       ROUND(SUM(a.VOLUMEN_M3)) AS VOLUMEN_M3,
       ROUND(SUM(a.KEGEL_M3)) AS KEGEL_M3,
       ROUND(SUM(MDSYS.SDO_GEOM.SDO_AREA(a.RUECKGANG, 0.005))) AS FLAECHE_M2,
       ROUND(AVG(a.RUECKGANG_M), 2) AS RUECKGANG_MITTEL_M,
       ROUND(MAX(a.RUECKGANG_M), 2) AS RUECKGANG_MAX_M,
       COUNT(DISTINCT a.STURMFLUT) AS STURMFLUTEN
FROM SY_ABBRUCH a
GROUP BY EXTRACT(YEAR FROM a.SZENE_ZEIT);

-- Sturmfluten: die belegten (ART = BELEGT) und die in der App gespielten (ART = GESPIELT) in einer Liste.
CREATE OR REPLACE VIEW SY_STURMFLUT_V AS
SELECT s.ID,
       'BELEGT' AS ART,
       s.NAME,
       s.DATUM,
       s.JAHR,
       s.SCHEITEL_LIST_CM,
       s.SCHEITEL_HOERNUM_CM,
       s.SCHEITEL_WESTERLAND_CM,
       CAST(NULL AS NUMBER(6,3)) AS SCHEITEL_M,
       CAST(NULL AS NUMBER(6,2)) AS STUNDEN_AM_FUSS,
       CAST(NULL AS NUMBER(5)) AS ABBRUECHE,
       s.KLIFFVERLUST_M,
       s.KLIFFVERLUST_TEXT,
       q.KURZ AS QUELLE
FROM SY_STURMFLUT s
LEFT JOIN SY_QUELLE q ON q.ID = s.QUELLE_ID
UNION ALL
SELECT l.ID,
       'GESPIELT',
       l.NAME,
       CAST(l.BEGINN_ZEIT AS DATE),
       EXTRACT(YEAR FROM l.BEGINN_ZEIT),
       NULL,
       NULL,
       NULL,
       l.SCHEITEL_M,
       l.STUNDEN_AM_FUSS,
       l.ABBRUECHE,
       NULL,
       NULL,
       'App'
FROM SY_STURMFLUT_LAUF l;

-- Befunde mit ihren Quellen.
CREATE OR REPLACE VIEW SY_BEFUND_V AS
SELECT b.ID,
       b.THEMA,
       b.GEGENSTAND,
       b.BEFUND,
       b.TEXT,
       (SELECT LISTAGG(q.KURZ || NVL2(bq.FUNDSTELLE, ' (' || bq.FUNDSTELLE || ')', ''), '; ') WITHIN GROUP (ORDER BY q.KURZ)
          FROM SY_BEFUND_QUELLE bq JOIN SY_QUELLE q ON q.ID = bq.QUELLE_ID
         WHERE bq.BEFUND_ID = b.ID) AS QUELLEN,
       b.HERKUNFT
FROM SY_BEFUND b;

-- Die letzte Sitzung mit gesichertem Zustand (für „Weiter wie zuletzt“ auf dem Startbild).
CREATE OR REPLACE VIEW SY_LETZTE_SITZUNG_V AS
SELECT s.ID,
       s.BEGINN,
       s.ENDE,
       s.SAUBER_BEENDET,
       s.ZUSTAND_ZEIT,
       s.SZENE_MS,
       s.SZENE_ZEIT,
       s.LAGE,
       s.PEGEL_M,
       s.ABBRUECHE,
       s.STURMFLUT,
       r.NAME AS REGIEBUCH,
       s.REGIE_SEKUNDE,
       CASE WHEN s.VORSCHAU IS NULL THEN 0 ELSE 1 END AS MIT_VORSCHAU
FROM SY_SITZUNG s
LEFT JOIN SY_REGIEBUCH r ON r.ID = s.REGIEBUCH_ID
WHERE s.ID = (SELECT MAX(x.ID) FROM SY_SITZUNG x WHERE x.ZUSTAND IS NOT NULL);

-- Ende 03_sichten.sql
