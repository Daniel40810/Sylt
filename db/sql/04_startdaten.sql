-- =====================================================================================================
-- Sylt · Datenbank · 04_startdaten.sql
-- ERZEUGT aus com.dan.sylt.data.Startdaten (ant startdaten) – nicht von Hand ändern.
-- Mitgelieferte Zeilen (HERKUNFT = 'MITGELIEFERT') werden angelegt oder wiederhergestellt; eigene Zeilen
-- bleiben unberührt. Beliebig oft wiederholbar. Quellen, Befunde, die belegten Sturmfluten seit 1962 und
-- Vorspülungen vor dem Kliff (recherchiert am 28.10.2026, F38/F39 A), das Gelände und die Kante 2024 aus
-- daten/ (F36 A), Lagen, Leuchtfeuer mit Sektoren (F40 A), Regiebücher, Ansichten, Fahrten (F41 A) und die
-- Presets der Packages (F43 A). Geometrien in UTM 32N (SRID 25832).
-- =====================================================================================================
-- ZAHLEN: Quellen=28; Befunde=28; Belege=40; Gelände=1; Kante 2024=121; Sturmfluten=20; Vorspülungen=11; Lagen=10; Leuchtfeuer=2; Sektoren=5; Regiebücher=4; Stichworte=144; Ansichten=18; Fahrten=4; Presets=57; Presetwerte=956;

-- Die Quellen enthalten „&“: in SQL Developer keine Ersetzungsvariablen
SET DEFINE OFF

-- ----------------------------------------------------------------------------------------- Ausbaustufe
MERGE INTO SY_SCHEMA t USING (SELECT 1 AS VERSION FROM DUAL) q ON (t.VERSION = q.VERSION)
WHEN NOT MATCHED THEN INSERT (VERSION, APP_VERSION, TEXT) VALUES (1, 'Phase 16', 'Datenbank: 25 Tabellen, Kante 2024 und Chronik der Abbrüche, Sturmfluten und Vorspülungen, Leuchtfeuer, Regie und Sitzungen');

-- --------------------------------------------------------------------------------------------- Quellen
MERGE INTO SY_QUELLE t USING (SELECT 'WIKI_ROTES_KLIFF' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Rotes Kliff – Wikipedia', t.URL = 'https://de.wikipedia.org/wiki/Rotes_Kliff', t.ART = 'WEB', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Höhe rund 30 m, Risgap bis Haus Kliffende, Saale-Eiszeit, Farbe, Findlinge, Vorspülungen seit Ende der 1970er-Jahre, Anatol 1999, Sturmfluten 1862, 1881, 1928' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('WIKI_ROTES_KLIFF', 'Rotes Kliff – Wikipedia', 'https://de.wikipedia.org/wiki/Rotes_Kliff', 'WEB', DATE '2026-10-28', 'Höhe rund 30 m, Risgap bis Haus Kliffende, Saale-Eiszeit, Farbe, Findlinge, Vorspülungen seit Ende der 1970er-Jahre, Anatol 1999, Sturmfluten 1862, 1881, 1928', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'WIKI_QUERMARKE' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Rotes Kliff Lighthouse – Wikipedia', t.URL = 'https://en.wikipedia.org/wiki/Rotes_Kliff_Lighthouse', t.ART = 'WEB', t.ABGERUFEN = DATE '2026-10-25', t.BEMERKUNG = 'Quermarkenfeuer 1912/13, 13 m, Feuerhöhe 23 m, F WRG, 21 sm, 1936 elektrisch, 1975 außer Betrieb, 1993 restauriert' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('WIKI_QUERMARKE', 'Rotes Kliff Lighthouse – Wikipedia', 'https://en.wikipedia.org/wiki/Rotes_Kliff_Lighthouse', 'WEB', DATE '2026-10-25', 'Quermarkenfeuer 1912/13, 13 m, Feuerhöhe 23 m, F WRG, 21 sm, 1936 elektrisch, 1975 außer Betrieb, 1993 restauriert', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'WIKI_KAMPEN' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Kampen Lighthouse – Wikipedia', t.URL = 'https://en.wikipedia.org/wiki/Kampen_Lighthouse', t.ART = 'WEB', t.ABGERUFEN = DATE '2026-10-25', t.BEMERKUNG = '1855/56, 40 m, Feuerhöhe 62 m, Bornholmer Ziegel, Kennung, Reichweiten 20 und 16 sm, Lage' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('WIKI_KAMPEN', 'Kampen Lighthouse – Wikipedia', 'https://en.wikipedia.org/wiki/Kampen_Lighthouse', 'WEB', DATE '2026-10-25', '1855/56, 40 m, Feuerhöhe 62 m, Bornholmer Ziegel, Kennung, Reichweiten 20 und 16 sm, Lage', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'WIKI_SYLT' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Sylt – Wikipedia', t.URL = 'https://de.wikipedia.org/wiki/Sylt', t.ART = 'WEB', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Sturmflut 1962, Küstenrückgang, Sandverlust rund 1,1 Mio. m³ im Jahr; Kostenangabe überholt' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('WIKI_SYLT', 'Sylt – Wikipedia', 'https://de.wikipedia.org/wiki/Sylt', 'WEB', DATE '2026-10-28', 'Sturmflut 1962, Küstenrückgang, Sandverlust rund 1,1 Mio. m³ im Jahr; Kostenangabe überholt', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'WIKI_XAVER' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Orkan Xaver – Wikipedia', t.URL = 'https://de.wikipedia.org/wiki/Orkan_Xaver', t.ART = 'WEB', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Dünen auf 23 km beschädigt, Hörnum-Odde rund 20 m, Böe 174 km/h' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('WIKI_XAVER', 'Orkan Xaver – Wikipedia', 'https://de.wikipedia.org/wiki/Orkan_Xaver', 'WEB', DATE '2026-10-28', 'Dünen auf 23 km beschädigt, Hörnum-Odde rund 20 m, Böe 174 km/h', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'WIKI_XAVER_EN' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Cyclone Xaver – Wikipedia', t.URL = 'https://en.wikipedia.org/wiki/Cyclone_Xaver', t.ART = 'WEB', t.ABGERUFEN = DATE '2026-10-25', t.BEMERKUNG = 'Orkan aus Nordwest, Bft 9–10, Böen um 140 km/h (Phase 12)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('WIKI_XAVER_EN', 'Cyclone Xaver – Wikipedia', 'https://en.wikipedia.org/wiki/Cyclone_Xaver', 'WEB', DATE '2026-10-25', 'Orkan aus Nordwest, Bft 9–10, Böen um 140 km/h (Phase 12)', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'LKN_FACHPLAN' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Fachplan Küstenschutz Sylt – Grundlagen (LKN.SH)', t.URL = 'https://www.schleswig-holstein.de/DE/fachinhalte/K/kuestenschutz_fachplaene/2_Sylt/2_Grundlagen/2_Grundlagen.html', t.ART = 'DATEN', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Sturmflutwerte Pegel List (NHN), Abbruchbilanzen 2015–2024, Küstenabschnitte' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('LKN_FACHPLAN', 'Fachplan Küstenschutz Sylt – Grundlagen (LKN.SH)', 'https://www.schleswig-holstein.de/DE/fachinhalte/K/kuestenschutz_fachplaene/2_Sylt/2_Grundlagen/2_Grundlagen.html', 'DATEN', DATE '2026-10-28', 'Sturmflutwerte Pegel List (NHN), Abbruchbilanzen 2015–2024, Küstenabschnitte', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'LKN_PM_2020' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'LKN.SH Pressemeldung Sturmschäden Westküste, 13.02.2020', t.URL = 'https://www.schleswig-holstein.de/DE/Landesregierung/LKN/Presse/Pressemeldungen/2020/200213_Sturmschaeden_Westkueste.html', t.ART = 'ARTIKEL', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Sturmserie Februar 2020 (Sabine), Kliff an der Sturmhaube auf rund 300 bis 350 m angegriffen' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('LKN_PM_2020', 'LKN.SH Pressemeldung Sturmschäden Westküste, 13.02.2020', 'https://www.schleswig-holstein.de/DE/Landesregierung/LKN/Presse/Pressemeldungen/2020/200213_Sturmschaeden_Westkueste.html', 'ARTIKEL', DATE '2026-10-28', 'Sturmserie Februar 2020 (Sabine), Kliff an der Sturmhaube auf rund 300 bis 350 m angegriffen', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'FACHPLAN_1997' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Fortschreibung Fachplan Küstenschutz Sylt, ALW Husum, 11.08.1997 (OURCOAST Doc4)', t.URL = 'https://discomap.eea.europa.eu/map/Data/Milieu/OURCOAST_195_DE/OURCOAST_195_DE_Doc4_FachplanSylt.pdf', t.ART = 'DATEN', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Pegelbezug List: 905 cm PN = NN +4,05 m; Rückgang 1950–1984 rund 1,5 m im Jahr' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('FACHPLAN_1997', 'Fortschreibung Fachplan Küstenschutz Sylt, ALW Husum, 11.08.1997 (OURCOAST Doc4)', 'https://discomap.eea.europa.eu/map/Data/Milieu/OURCOAST_195_DE/OURCOAST_195_DE_Doc4_FachplanSylt.pdf', 'DATEN', DATE '2026-10-28', 'Pegelbezug List: 905 cm PN = NN +4,05 m; Rückgang 1950–1984 rund 1,5 m im Jahr', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'LKN_BERICHT_2009' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Westküste Sylt – Morphologie, Bericht 02/2009, Husum 27.02.2009 (OURCOAST Doc3)', t.URL = 'https://discomap.eea.europa.eu/map/Data/Milieu/OURCOAST_195_DE/OURCOAST_195_DE_Doc3_WestCoastMorphologySylt.pdf', t.ART = 'DATEN', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Die 20 höchsten Sturmfluten am Pegel List seit 1900 (cm PN), Vorspülungen Wenningstedt und Kampen 2004 und 2008' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('LKN_BERICHT_2009', 'Westküste Sylt – Morphologie, Bericht 02/2009, Husum 27.02.2009 (OURCOAST Doc3)', 'https://discomap.eea.europa.eu/map/Data/Milieu/OURCOAST_195_DE/OURCOAST_195_DE_Doc3_WestCoastMorphologySylt.pdf', 'DATEN', DATE '2026-10-28', 'Die 20 höchsten Sturmfluten am Pegel List seit 1900 (cm PN), Vorspülungen Wenningstedt und Kampen 2004 und 2008', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'OURCOAST_SCHUTZ' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'West Coast Protection Sylt (OURCOAST Doc2)', t.URL = 'https://discomap.eea.europa.eu/map/Data/Milieu/OURCOAST_195_DE/OURCOAST_195_DE_Doc2_WestCoastProtectionSylt.pdf', t.ART = 'DATEN', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Sandaufspülungen seit 1972, seit 1984 an der ganzen Westküste' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('OURCOAST_SCHUTZ', 'West Coast Protection Sylt (OURCOAST Doc2)', 'https://discomap.eea.europa.eu/map/Data/Milieu/OURCOAST_195_DE/OURCOAST_195_DE_Doc2_WestCoastProtectionSylt.pdf', 'DATEN', DATE '2026-10-28', 'Sandaufspülungen seit 1972, seit 1984 an der ganzen Westküste', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'PM_2017' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'MELUR-Pressemitteilung Sandvorspülungen Sylt, 27.03.2017', t.URL = 'https://www.schleswig-holstein.de/DE/Landesregierung/V/Presse/PI/2017/0317/MELUR_170327_Sandvorspuelungen_Sylt.html', t.ART = 'ARTIKEL', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = NULL WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('PM_2017', 'MELUR-Pressemitteilung Sandvorspülungen Sylt, 27.03.2017', 'https://www.schleswig-holstein.de/DE/Landesregierung/V/Presse/PI/2017/0317/MELUR_170327_Sandvorspuelungen_Sylt.html', 'ARTIKEL', DATE '2026-10-28', NULL, 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'PM_2019' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Pressemitteilung Strandbereisung Sylt, 08.04.2019', t.URL = 'https://www.schleswig-holstein.de/DE/Landesregierung/V/Presse/PI/2019/0419/190408_Strandbereisung_Sylt.html', t.ART = 'ARTIKEL', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Kampen Hauptstrand 140 000 m³ geplant' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('PM_2019', 'Pressemitteilung Strandbereisung Sylt, 08.04.2019', 'https://www.schleswig-holstein.de/DE/Landesregierung/V/Presse/PI/2019/0419/190408_Strandbereisung_Sylt.html', 'ARTIKEL', DATE '2026-10-28', 'Kampen Hauptstrand 140 000 m³ geplant', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'PM_2024' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Pressemitteilung Küstenschutz Sylt, 18.03.2024', t.URL = 'https://www.schleswig-holstein.de/DE/landesregierung/ministerien-behoerden/V/Presse/PI/2024/03/240318_Sylt', t.ART = 'ARTIKEL', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Saison 2024: Kampen Nord und Süd je 100 000 m³' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('PM_2024', 'Pressemitteilung Küstenschutz Sylt, 18.03.2024', 'https://www.schleswig-holstein.de/DE/landesregierung/ministerien-behoerden/V/Presse/PI/2024/03/240318_Sylt', 'ARTIKEL', DATE '2026-10-28', 'Saison 2024: Kampen Nord und Süd je 100 000 m³', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'PM_2025' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Pressemitteilung Sandvorspülungen 2025–2028, 18.02.2025', t.URL = 'https://www.schleswig-holstein.de/DE/landesregierung/ministerien-behoerden/V/Presse/PI/2025/02/250218_Sandvorsp%C3%BClungen', t.ART = 'ARTIKEL', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Seit 1972 rund 61,3 Mio. m³ und 289 Mio. €' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('PM_2025', 'Pressemitteilung Sandvorspülungen 2025–2028, 18.02.2025', 'https://www.schleswig-holstein.de/DE/landesregierung/ministerien-behoerden/V/Presse/PI/2025/02/250218_Sandvorsp%C3%BClungen', 'ARTIKEL', DATE '2026-10-28', 'Seit 1972 rund 61,3 Mio. m³ und 289 Mio. €', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'LKN_PM_2026' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'LKN.SH: Sandaufspülung startet in Hörnum, 15.04.2026', t.URL = 'https://www.schleswig-holstein.de/DE/landesregierung/ministerien-behoerden/LKN/Presse/Pressemeldungen/2026/MeldungSandaufspuelungSylt_04-2026', t.ART = 'ARTIKEL', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Saison 2026: 1,3 Mio. m³ auf 8,15 km' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('LKN_PM_2026', 'LKN.SH: Sandaufspülung startet in Hörnum, 15.04.2026', 'https://www.schleswig-holstein.de/DE/landesregierung/ministerien-behoerden/LKN/Presse/Pressemeldungen/2026/MeldungSandaufspuelungSylt_04-2026', 'ARTIKEL', DATE '2026-10-28', 'Saison 2026: 1,3 Mio. m³ auf 8,15 km', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'TAZ_2025' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'taz, Sandaufspülungen Sylt 2025–2028, 19.02.2025', t.URL = 'https://taz.de/nachrichten/!6067064/', t.ART = 'ARTIKEL', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Ohne Vorspülung 1 bis 4 m Rückgang im Jahr' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('TAZ_2025', 'taz, Sandaufspülungen Sylt 2025–2028, 19.02.2025', 'https://taz.de/nachrichten/!6067064/', 'ARTIKEL', DATE '2026-10-28', 'Ohne Vorspülung 1 bis 4 m Rückgang im Jahr', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'SPEKTRUM_2007' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Spektrum.de: Bröckelnde Bastion, 31.01.2007', t.URL = 'https://www.spektrum.de/news/broeckelnde-bastion/863961', t.ART = 'ARTIKEL', t.ABGERUFEN = DATE '2026-10-28', t.BEMERKUNG = 'Winter 2006/07: bis zu 6 m Strand abgetragen' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('SPEKTRUM_2007', 'Spektrum.de: Bröckelnde Bastion, 31.01.2007', 'https://www.spektrum.de/news/broeckelnde-bastion/863961', 'ARTIKEL', DATE '2026-10-28', 'Winter 2006/07: bis zu 6 m Strand abgetragen', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'EASYGSH' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Sievers et al. 2021: Integrated marine data collection for the German Bight – Part 1 (EasyGSH-DB), ESSD 13', t.URL = NULL, t.ART = 'ARTIKEL', t.ABGERUFEN = NULL, t.BEMERKUNG = 'Grund vor dem Kliff' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('EASYGSH', 'Sievers et al. 2021: Integrated marine data collection for the German Bight – Part 1 (EasyGSH-DB), ESSD 13', NULL, 'ARTIKEL', NULL, 'Grund vor dem Kliff', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'BAW' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'BAW Küstenhöhen topography_max_z 2015–2021 und Wattflächen 2021 (10 m, ETRS89/UTM 32, NHN)', t.URL = NULL, t.ART = 'DATEN', t.ABGERUFEN = DATE '2026-10-27', t.BEMERKUNG = 'Grund und Fernfeld (Phase 2)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('BAW', 'BAW Küstenhöhen topography_max_z 2015–2021 und Wattflächen 2021 (10 m, ETRS89/UTM 32, NHN)', NULL, 'DATEN', DATE '2026-10-27', 'Grund und Fernfeld (Phase 2)', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'GEOBASIS_SH' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'DGM1, bDOM, DOP20, LOD2 Schleswig-Holstein, © GeoBasis-DE/LVermGeo SH, Datenlizenz Deutschland', t.URL = NULL, t.ART = 'DATEN', t.ABGERUFEN = DATE '2026-10-27', t.BEMERKUNG = 'Kern 1 m, Kante des Kliffs 2024 (Phase 2)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('GEOBASIS_SH', 'DGM1, bDOM, DOP20, LOD2 Schleswig-Holstein, © GeoBasis-DE/LVermGeo SH, Datenlizenz Deutschland', NULL, 'DATEN', DATE '2026-10-27', 'Kern 1 m, Kante des Kliffs 2024 (Phase 2)', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'VORLAGEN' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Vorlagen in templates/: RotesKliff1–5, Leuchturm1–4, Kampen1–4', t.URL = NULL, t.ART = 'WEB', t.ABGERUFEN = NULL, t.BEMERKUNG = 'Bilder, nach denen Türme und Ansichten gestellt sind' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('VORLAGEN', 'Vorlagen in templates/: RotesKliff1–5, Leuchturm1–4, Kampen1–4', NULL, 'WEB', NULL, 'Bilder, nach denen Türme und Ansichten gestellt sind', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'TIDESCHART' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'tideschart: Gezeiten Kampen (West)', t.URL = NULL, t.ART = 'WEB', t.ABGERUFEN = DATE '2026-10-27', t.BEMERKUNG = 'Tide.westerland() (Phase 7a)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('TIDESCHART', 'tideschart: Gezeiten Kampen (West)', NULL, 'WEB', DATE '2026-10-27', 'Tide.westerland() (Phase 7a)', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'STOCKDON06' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Stockdon et al. 2006: Empirical parameterization of setup, swash, and runup', t.URL = NULL, t.ART = 'ARTIKEL', t.ABGERUFEN = NULL, t.BEMERKUNG = 'Auflauf R2 % (Phase 12)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('STOCKDON06', 'Stockdon et al. 2006: Empirical parameterization of setup, swash, and runup', NULL, 'ARTIKEL', NULL, 'Auflauf R2 % (Phase 12)', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'BATTJES' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Battjes 1974; Battjes und Janssen 1978; Longuet-Higgins und Stewart 1964', t.URL = NULL, t.ART = 'ARTIKEL', t.ABGERUFEN = NULL, t.BEMERKUNG = 'Brechertyp, Dissipation, Strahlungsspannung (Phasen 5–7)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('BATTJES', 'Battjes 1974; Battjes und Janssen 1978; Longuet-Higgins und Stewart 1964', NULL, 'ARTIKEL', NULL, 'Brechertyp, Dissipation, Strahlungsspannung (Phasen 5–7)', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'BAGNOLD41' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Bagnold 1941: The Physics of Blown Sand and Desert Dunes', t.URL = NULL, t.ART = 'BUCH', t.ABGERUFEN = NULL, t.BEMERKUNG = 'Flugsand (Phase 8)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('BAGNOLD41', 'Bagnold 1941: The Physics of Blown Sand and Desert Dunes', NULL, 'BUCH', NULL, 'Flugsand (Phase 8)', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'DWD_LIST' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'DWD-Klimamittel List auf Sylt 1991–2020', t.URL = NULL, t.ART = 'DATEN', t.ABGERUFEN = NULL, t.BEMERKUNG = 'Monatsmittel der Temperatur (Phase 13)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('DWD_LIST', 'DWD-Klimamittel List auf Sylt 1991–2020', NULL, 'DATEN', NULL, 'Monatsmittel der Temperatur (Phase 13)', 'MITGELIEFERT');
MERGE INTO SY_QUELLE t USING (SELECT 'APP' AS KURZ FROM DUAL) q ON (t.KURZ = q.KURZ)
WHEN MATCHED THEN UPDATE SET t.TITEL = 'Sylt · Rotes Kliff, eigene Rechnung und Gestaltung', t.URL = NULL, t.ART = 'DATEN', t.ABGERUFEN = NULL, t.BEMERKUNG = 'Was die App rechnet oder setzt [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (KURZ, TITEL, URL, ART, ABGERUFEN, BEMERKUNG, HERKUNFT)
  VALUES ('APP', 'Sylt · Rotes Kliff, eigene Rechnung und Gestaltung', NULL, 'DATEN', NULL, 'Was die App rechnet oder setzt [G]', 'MITGELIEFERT');

-- --------------------------------------------------------------------------------------------- Befunde
MERGE INTO SY_BEFUND t USING (SELECT 'GELAENDE' AS THEMA, 'Gelände des Kerns' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GEMESSEN', t.TEXT = 'DGM1 2024 mit 1 m und bDOM 2024 mit 20 cm um das Kliff (Kern 2,3 × 3,6 km); Höhen NHN, UTM 32N.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('GELAENDE', 'Gelände des Kerns', 'GEMESSEN', 'DGM1 2024 mit 1 m und bDOM 2024 mit 20 cm um das Kliff (Kern 2,3 × 3,6 km); Höhen NHN, UTM 32N.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'GELAENDE' AS THEMA, 'Grund und Fernfeld' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GEMESSEN', t.TEXT = 'BAW-Küstenhöhen 2015–2021 mit 10 m, außen 100 m; 8 km draußen −13 bis −14 m, sanft ansteigend.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('GELAENDE', 'Grund und Fernfeld', 'GEMESSEN', 'BAW-Küstenhöhen 2015–2021 mit 10 m, außen 100 m; 8 km draußen −13 bis −14 m, sanft ansteigend.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'GELAENDE' AS THEMA, 'Feinform unter dem Raster' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GESTALTET', t.TEXT = 'Riffe und Rinnen auf 2 m, Rippel, Runsen am Kliff prozedural.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('GELAENDE', 'Feinform unter dem Raster', 'GESTALTET', 'Riffe und Rinnen auf 2 m, Rippel, Runsen am Kliff prozedural.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KLIFF' AS THEMA, 'Lage und Länge' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Westküste von Sylt vom Parkplatz Risgap in Wenningstedt bis Haus Kliffende in Kampen; die gemessene Kante ist rund 3 km lang.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KLIFF', 'Lage und Länge', 'BELEGT', 'Westküste von Sylt vom Parkplatz Risgap in Wenningstedt bis Haus Kliffende in Kampen; die gemessene Kante ist rund 3 km lang.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KLIFF' AS THEMA, 'Höhe' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GEMESSEN', t.TEXT = 'Rund 30 m; die Wand nach der Kante 2024 bis rund 35 m.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KLIFF', 'Höhe', 'GEMESSEN', 'Rund 30 m; die Wand nach der Kante 2024 bis rund 35 m.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KLIFF' AS THEMA, 'Aufbau' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Oben Dünen- und Flugsand, darunter rot oxidierter Geschiebelehm der Saale-Eiszeit mit Findlingen, unten heller Kaolinsand.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KLIFF', 'Aufbau', 'BELEGT', 'Oben Dünen- und Flugsand, darunter rot oxidierter Geschiebelehm der Saale-Eiszeit mit Findlingen, unten heller Kaolinsand.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KLIFF' AS THEMA, 'Mächtigkeit der Schichten' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GESTALTET', t.TEXT = 'Die Schichten je Ort sind gesetzt [G].' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KLIFF', 'Mächtigkeit der Schichten', 'GESTALTET', 'Die Schichten je Ort sind gesetzt [G].', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KLIFF' AS THEMA, 'Abbrüche bei Sturmflut' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Sturmfluten 1862, 1881 und 1928 kosteten je mehrere Meter, Anatol 1999 brachte starke Abbrüche; Meter je Ereignis sind nicht belegt.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KLIFF', 'Abbrüche bei Sturmflut', 'BELEGT', 'Sturmfluten 1862, 1881 und 1928 kosteten je mehrere Meter, Anatol 1999 brachte starke Abbrüche; Meter je Ereignis sind nicht belegt.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KLIFF' AS THEMA, 'Abbruch in der App' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GESTALTET', t.TEXT = 'Hohlkehle nach Auflauf (Stockdon), Bruch nach Mohr-Coulomb mit fracture; Zeitpunkt, Form und Menge kommen aus der Rechnung.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KLIFF', 'Abbruch in der App', 'GESTALTET', 'Hohlkehle nach Auflauf (Stockdon), Bruch nach Mohr-Coulomb mit fracture; Zeitpunkt, Form und Menge kommen aus der Rechnung.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KLIFF' AS THEMA, 'Rückgang' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Düne und Kliff der Westküste wichen 1950–1984 um rund 1,5 m im Jahr zurück.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KLIFF', 'Rückgang', 'BELEGT', 'Düne und Kliff der Westküste wichen 1950–1984 um rund 1,5 m im Jahr zurück.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KUESTE' AS THEMA, 'Sandvorspülungen' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Seit 1972 auf Sylt, seit 1984 an der ganzen Westküste; bis Ende 2024 rund 61,3 Mio. m³ für rund 289 Mio. €.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KUESTE', 'Sandvorspülungen', 'BELEGT', 'Seit 1972 auf Sylt, seit 1984 an der ganzen Westküste; bis Ende 2024 rund 61,3 Mio. m³ für rund 289 Mio. €.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KUESTE' AS THEMA, 'Vorspülungen vor dem Kliff' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Belegt sind Wenningstedt 2004 und 2008, Kampen 2008 und 2024; eine lückenlose Liste für den Abschnitt fehlt.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KUESTE', 'Vorspülungen vor dem Kliff', 'BELEGT', 'Belegt sind Wenningstedt 2004 und 2008, Kampen 2008 und 2024; eine lückenlose Liste für den Abschnitt fehlt.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KUESTE' AS THEMA, 'Vorspülung in der App' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GESTALTET', t.TEXT = '600 m vor dem Kliff, 70 m breit, am Fuß 2,5 m hoch, Abtrag nach der See [G].' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KUESTE', 'Vorspülung in der App', 'GESTALTET', '600 m vor dem Kliff, 70 m breit, am Fuß 2,5 m hoch, Abtrag nach der See [G].', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KUESTE' AS THEMA, 'Sandverlust' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Rund 1 bis 1,2 Mio. m³ im Jahr an der Westküste.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KUESTE', 'Sandverlust', 'BELEGT', 'Rund 1 bis 1,2 Mio. m³ im Jahr an der Westküste.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KUESTE' AS THEMA, 'Sturmfluten am Pegel List' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GEMESSEN', t.TEXT = 'Höchster Wert seit 1900 am 24.11.1981 mit NHN +4,05 m; 2015–2024 höchster am 19.02.2022 mit NHN +3,18 m.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KUESTE', 'Sturmfluten am Pegel List', 'GEMESSEN', 'Höchster Wert seit 1900 am 24.11.1981 mit NHN +4,05 m; 2015–2024 höchster am 19.02.2022 mit NHN +3,18 m.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KUESTE' AS THEMA, 'Kliff an der Sturmhaube 2020' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Die Sturmserie im Februar 2020 legte das Kliff an der Sturmhaube Kampen auf rund 350 m frei.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KUESTE', 'Kliff an der Sturmhaube 2020', 'BELEGT', 'Die Sturmserie im Februar 2020 legte das Kliff an der Sturmhaube Kampen auf rund 350 m frei.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'BRANDUNG' AS THEMA, 'Physik der Wellen' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Shoaling, Brechen nach McCowan, Brechertyp nach Iribarren und Battjes, Dissipation nach Battjes und Janssen.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('BRANDUNG', 'Physik der Wellen', 'BELEGT', 'Shoaling, Brechen nach McCowan, Brechertyp nach Iribarren und Battjes, Dissipation nach Battjes und Janssen.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'BRANDUNG' AS THEMA, 'Seegang vor Westerland' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GESTALTET', t.TEXT = 'Die Seegangsstatistik ist gesetzt, bis ein Beleg da ist [G].' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('BRANDUNG', 'Seegang vor Westerland', 'GESTALTET', 'Die Seegangsstatistik ist gesetzt, bis ein Beleg da ist [G].', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'BRANDUNG' AS THEMA, 'Tide' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Pegel Kampen (West) aus dem Gezeitenkalender; Tidenhub rund 1,8 m.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('BRANDUNG', 'Tide', 'BELEGT', 'Pegel Kampen (West) aus dem Gezeitenkalender; Tidenhub rund 1,8 m.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'SAND' AS THEMA, 'Flugsand' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Transport nach Bagnold mit Feuchte und Strandhafer; Korngröße am Ort gesetzt [G].' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('SAND', 'Flugsand', 'BELEGT', 'Transport nach Bagnold mit Feuchte und Strandhafer; Korngröße am Ort gesetzt [G].', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'TUERME' AS THEMA, 'Quermarkenfeuer' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = '1912/13, achteckiger Backsteinturm 13 m, Feuerhöhe 23 m, F WRG, 21 sm; 1975 außer Betrieb, heute Tagesmarke.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('TUERME', 'Quermarkenfeuer', 'BELEGT', '1912/13, achteckiger Backsteinturm 13 m, Feuerhöhe 23 m, F WRG, 21 sm; 1975 außer Betrieb, heute Tagesmarke.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'TUERME' AS THEMA, 'Kampener Leuchtturm' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = '1855/56, 40 m, Feuerhöhe 62 m, Iso WR 7 s, weiß 20 sm, rot 16 sm; seit 1975 mit rotem Sektor anstelle des Quermarkenfeuers.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('TUERME', 'Kampener Leuchtturm', 'BELEGT', '1855/56, 40 m, Feuerhöhe 62 m, Iso WR 7 s, weiß 20 sm, rot 16 sm; seit 1975 mit rotem Sektor anstelle des Quermarkenfeuers.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'TUERME' AS THEMA, 'Einzelheiten der Laterne' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GESTALTET', t.TEXT = 'Maße der Laterne und Einzelheiten nach den Vorlagen [G].' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('TUERME', 'Einzelheiten der Laterne', 'GESTALTET', 'Maße der Laterne und Einzelheiten nach den Vorlagen [G].', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'WETTER' AS THEMA, 'Sturmflut Xaver' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = '5. bis 7.12.2013: Orkan aus Nordwest, Bft 9–10, Böen um 140 km/h; der Verlauf je Stunde ist geschätzt [G].' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('WETTER', 'Sturmflut Xaver', 'BELEGT', '5. bis 7.12.2013: Orkan aus Nordwest, Bft 9–10, Böen um 140 km/h; der Verlauf je Stunde ist geschätzt [G].', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'WETTER' AS THEMA, 'Klima' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'BELEGT', t.TEXT = 'Monatsmittel der Temperatur von List 1991–2020, gerundet.' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('WETTER', 'Klima', 'BELEGT', 'Monatsmittel der Temperatur von List 1991–2020, gerundet.', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'LEBEN' AS THEMA, 'Strandleben' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GESTALTET', t.TEXT = 'Strandkörbe im Sommer, Möwen, Austernfischer, Sanderlinge: Verteilung und Verhalten gesetzt [G].' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('LEBEN', 'Strandleben', 'GESTALTET', 'Strandkörbe im Sommer, Möwen, Austernfischer, Sanderlinge: Verteilung und Verhalten gesetzt [G].', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'KLANG' AS THEMA, 'Klang' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GESTALTET', t.TEXT = 'Gestalt der Klänge nach Gehör und Beschreibung, Ausbreitung nach Maekawa und ISO 9613-1 [G].' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('KLANG', 'Klang', 'GESTALTET', 'Gestalt der Klänge nach Gehör und Beschreibung, Ausbreitung nach Maekawa und ISO 9613-1 [G].', 'MITGELIEFERT');
MERGE INTO SY_BEFUND t USING (SELECT 'REGIE' AS THEMA, 'Lagen der Wetterregie' AS GEGENSTAND FROM DUAL) q ON (t.THEMA = q.THEMA AND t.GEGENSTAND = q.GEGENSTAND)
WHEN MATCHED THEN UPDATE SET t.BEFUND = 'GESTALTET', t.TEXT = 'Neun benannte Lagen vom Sommerabend bis zum Gewitter über der See [G].' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (THEMA, GEGENSTAND, BEFUND, TEXT, HERKUNFT)
  VALUES ('REGIE', 'Lagen der Wetterregie', 'GESTALTET', 'Neun benannte Lagen vom Sommerabend bis zum Gewitter über der See [G].', 'MITGELIEFERT');
DELETE FROM SY_BEFUND_QUELLE WHERE BEFUND_ID IN (SELECT ID FROM SY_BEFUND WHERE HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'GELAENDE' AND b.GEGENSTAND = 'Gelände des Kerns' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'GEOBASIS_SH';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'GELAENDE' AND b.GEGENSTAND = 'Grund und Fernfeld' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'BAW';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'GELAENDE' AND b.GEGENSTAND = 'Grund und Fernfeld' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'EASYGSH';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'GELAENDE' AND b.GEGENSTAND = 'Feinform unter dem Raster' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Lage und Länge' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_ROTES_KLIFF';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Lage und Länge' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'GEOBASIS_SH';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Höhe' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_ROTES_KLIFF';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Höhe' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'GEOBASIS_SH';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Aufbau' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_ROTES_KLIFF';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Mächtigkeit der Schichten' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Abbrüche bei Sturmflut' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_ROTES_KLIFF';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Abbruch in der App' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'STOCKDON06';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Abbruch in der App' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Rückgang' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'FACHPLAN_1997';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLIFF' AND b.GEGENSTAND = 'Rückgang' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_SYLT';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Sandvorspülungen' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'OURCOAST_SCHUTZ';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Sandvorspülungen' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'PM_2025';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Vorspülungen vor dem Kliff' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'LKN_BERICHT_2009';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Vorspülungen vor dem Kliff' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'PM_2024';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Vorspülung in der App' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Sandverlust' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'LKN_PM_2026';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Sandverlust' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_SYLT';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Sturmfluten am Pegel List' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'LKN_FACHPLAN';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Sturmfluten am Pegel List' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'FACHPLAN_1997';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KUESTE' AND b.GEGENSTAND = 'Kliff an der Sturmhaube 2020' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'LKN_PM_2020';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'BRANDUNG' AND b.GEGENSTAND = 'Physik der Wellen' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'BATTJES';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'BRANDUNG' AND b.GEGENSTAND = 'Seegang vor Westerland' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'BRANDUNG' AND b.GEGENSTAND = 'Tide' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'TIDESCHART';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'SAND' AND b.GEGENSTAND = 'Flugsand' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'BAGNOLD41';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'SAND' AND b.GEGENSTAND = 'Flugsand' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'TUERME' AND b.GEGENSTAND = 'Quermarkenfeuer' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_QUERMARKE';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'TUERME' AND b.GEGENSTAND = 'Kampener Leuchtturm' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_KAMPEN';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'TUERME' AND b.GEGENSTAND = 'Einzelheiten der Laterne' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'VORLAGEN';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'TUERME' AND b.GEGENSTAND = 'Einzelheiten der Laterne' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'WETTER' AND b.GEGENSTAND = 'Sturmflut Xaver' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_XAVER_EN';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'WETTER' AND b.GEGENSTAND = 'Sturmflut Xaver' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'WIKI_XAVER';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'WETTER' AND b.GEGENSTAND = 'Klima' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'DWD_LIST';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'LEBEN' AND b.GEGENSTAND = 'Strandleben' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'KLANG' AND b.GEGENSTAND = 'Klang' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';
INSERT INTO SY_BEFUND_QUELLE (BEFUND_ID, QUELLE_ID) SELECT b.ID, q.ID FROM SY_BEFUND b, SY_QUELLE q WHERE b.THEMA = 'REGIE' AND b.GEGENSTAND = 'Lagen der Wetterregie' AND b.HERKUNFT = 'MITGELIEFERT' AND q.KURZ = 'APP';

-- ----------------------------------------------------------------------------------- Gelände (Phase 2)
MERGE INTO SY_GELAENDE t USING (SELECT 'Rotes Kliff' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.STAND = '2024', t.KERN = MDSYS.SDO_GEOMETRY('POLYGON ((455700 6088000, 458000 6088000, 458000 6091600, 455700 6091600, 455700 6088000))', 25832), t.FERNFELD = MDSYS.SDO_GEOMETRY('POLYGON ((448850 6081800, 464850 6081800, 464850 6097800, 448850 6097800, 448850 6081800))', 25832), t.URSPRUNG = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457600, 6091170, NULL), NULL, NULL), t.URSPRUNG_OST = 457600, t.URSPRUNG_NORD = 6091170, t.ZELLE_KERN_M = 1, t.ZELLE_M = 10, t.ZELLE_FERN_M = 100, t.DATEIEN = 'kern.r16=5f14fafc2dd738eb36875cd12a45446af23406f3972389c362704e0305728b0a; hoehe.r16=9624c6154df1f0438e8b292797c8aa661691ed1a9f6255509b0b59a5a6352acd; fern.r16=eec049805b0fc79bf63b1731ad88a8441333b423fbef8be9148d8b7f871d15a7; art.u8=fa6397ee4e1f6bc60e8d80f3f949c5e49d77fb0e23b6aac490610923c4dce43c; gelaende.properties=d12a440719c68bdebb237f3a18554a2e2192a70376715ed7d265a674865faba3', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'GEOBASIS_SH'), t.BEMERKUNG = 'Kern 2,3 × 3,6 km aus dem DGM1 (1 m), das Fernfeld aus BAW und DGM1 (10 m, außen 100 m); Szenenmeter x nach Osten, z nach Süden, Ursprung am Quermarkenfeuer (E 457 600, N 6 091 170); Höhen NHN' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, STAND, KERN, FERNFELD, URSPRUNG, URSPRUNG_OST, URSPRUNG_NORD, ZELLE_KERN_M, ZELLE_M, ZELLE_FERN_M, DATEIEN, QUELLE_ID, BEMERKUNG, HERKUNFT)
  VALUES ('Rotes Kliff', '2024', MDSYS.SDO_GEOMETRY('POLYGON ((455700 6088000, 458000 6088000, 458000 6091600, 455700 6091600, 455700 6088000))', 25832), MDSYS.SDO_GEOMETRY('POLYGON ((448850 6081800, 464850 6081800, 464850 6097800, 448850 6097800, 448850 6081800))', 25832), MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457600, 6091170, NULL), NULL, NULL), 457600, 6091170, 1, 10, 100, 'kern.r16=5f14fafc2dd738eb36875cd12a45446af23406f3972389c362704e0305728b0a; hoehe.r16=9624c6154df1f0438e8b292797c8aa661691ed1a9f6255509b0b59a5a6352acd; fern.r16=eec049805b0fc79bf63b1731ad88a8441333b423fbef8be9148d8b7f871d15a7; art.u8=fa6397ee4e1f6bc60e8d80f3f949c5e49d77fb0e23b6aac490610923c4dce43c; gelaende.properties=d12a440719c68bdebb237f3a18554a2e2192a70376715ed7d265a674865faba3', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'GEOBASIS_SH'), 'Kern 2,3 × 3,6 km aus dem DGM1 (1 m), das Fernfeld aus BAW und DGM1 (10 m, außen 100 m); Szenenmeter x nach Osten, z nach Süden, Ursprung am Quermarkenfeuer (E 457 600, N 6 091 170); Höhen NHN', 'MITGELIEFERT');

-- ----------- Kante des Kliffs 2024 (F36 A): die gemessenen Stücke neu, die Stücke der Abbrüche bleiben
DELETE FROM SY_KLIFFKANTE WHERE STAND = 'GEMESSEN_2024' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091722.5, 6091695.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457697.5 6091722.5, 457698.5 6091720.5, 457696.5 6091718.5, 457699.5 6091717.5, 457698.5 6091715.5, 457693.5 6091713.5, 457691.5 6091711.5, 457696.5 6091710.5, 457691.5 6091709.5, 457691.5 6091707.5, 457690.5 6091705.5, 457693.5 6091704.5, 457688.5 6091702.5, 457689.5 6091700.5, 457693.5 6091697.5, 457688.5 6091695.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457671.5 6091722.5, 457671.5 6091720.5, 457670.5 6091718.5, 457670.5 6091716.5, 457669.5 6091714.5, 457668.5 6091712.5, 457667.5 6091710.5, 457667.5 6091708.5, 457666.5 6091706.5, 457665.5 6091704.5, 457664.5 6091702.5, 457664.5 6091700.5, 457663.5 6091697.5, 457662.5 6091695.5)', 25832),
  9, 14, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091695.5, 6091670.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457688.5 6091695.5, 457686.5 6091693.5, 457686.5 6091691.5, 457687.5 6091689.5, 457688.5 6091687.5, 457686.5 6091686.5, 457679.5 6091685.5, 457683.5 6091684.5, 457681.5 6091683.5, 457676.5 6091681.5, 457679.5 6091680.5, 457681.5 6091679.5, 457681.5 6091677.5, 457676.5 6091675.5, 457677.5 6091673.5, 457678.5 6091671.5, 457678.5 6091670.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457662.5 6091695.5, 457661.5 6091693.5, 457660.5 6091691.5, 457659.5 6091689.5, 457658.5 6091687.5, 457657.5 6091685.5, 457656.5 6091683.5, 457655.5 6091681.5, 457654.5 6091679.5, 457653.5 6091677.5, 457652.5 6091675.5, 457651.5 6091673.5, 457650.5 6091671.5, 457650.5 6091670.5)', 25832),
  9, 14.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091670.5, 6091644.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457678.5 6091670.5, 457679.5 6091668.5, 457677.5 6091666.5, 457675.5 6091665.5, 457673.5 6091664.5, 457672.5 6091662.5, 457671.5 6091660.5, 457666.5 6091658.5, 457672.5 6091657.5, 457668.5 6091655.5, 457665.5 6091653.5, 457663.5 6091651.5, 457666.5 6091649.5, 457665.5 6091647.5, 457661.5 6091646.5, 457662.5 6091644.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457650.5 6091670.5, 457649.5 6091668.5, 457648.5 6091666.5, 457647.5 6091664.5, 457647.5 6091662.5, 457646.5 6091660.5, 457645.5 6091658.5, 457644.5 6091656.5, 457644.5 6091654.5, 457642.5 6091652.5, 457642.5 6091650.5, 457641.5 6091648.5, 457640.5 6091646.5, 457639.5 6091644.5)', 25832),
  8.2, 13.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091644.5, 6091618.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457662.5 6091644.5, 457659.5 6091643.5, 457659.5 6091641.5, 457657.5 6091639.5, 457661.5 6091637.5, 457662.5 6091635.5, 457655.5 6091634.5, 457654.5 6091632.5, 457652.5 6091630.5, 457650.5 6091628.5, 457650.5 6091626.5, 457649.5 6091624.5, 457648.5 6091622.5, 457646.5 6091620.5, 457646.5 6091618.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457639.5 6091644.5, 457638.5 6091642.5, 457637.5 6091640.5, 457636.5 6091638.5, 457635.5 6091636.5, 457634.5 6091634.5, 457633.5 6091632.5, 457632.5 6091630.5, 457631.5 6091628.5, 457630.5 6091626.5, 457629.5 6091624.5, 457628.5 6091622.5, 457627.5 6091620.5, 457626.5 6091618.5)', 25832),
  7.7, 13.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091618.5, 6091583.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457646.5 6091618.5, 457644.5 6091617.5, 457643.5 6091615.5, 457636.5 6091613.5, 457642.5 6091612.5, 457639.5 6091611.5, 457641.5 6091610.5, 457640.5 6091608.5, 457633.5 6091606.5, 457639.5 6091605.5, 457632.5 6091604.5, 457637.5 6091603.5, 457637.5 6091601.5, 457636.5 6091599.5, 457635.5 6091597.5, 457634.5 6091595.5, 457633.5 6091593.5, 457635.5 6091592.5, 457632.5 6091591.5, 457634.5 6091590.5, 457632.5 6091588.5, 457630.5 6091587.5, 457629.5 6091585.5, 457623.5 6091583.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457626.5 6091618.5, 457625.5 6091616.5, 457624.5 6091614.5, 457622.5 6091612.5, 457621.5 6091610.5, 457620.5 6091608.5, 457619.5 6091606.5, 457619.5 6091604.5, 457618.5 6091602.5, 457617.5 6091600.5, 457616.5 6091598.5, 457615.5 6091596.5, 457614.5 6091594.5, 457613.5 6091592.5, 457612.5 6091590.5, 457611.5 6091588.5, 457610.5 6091586.5, 457608.5 6091584.5, 457608.5 6091583.5)', 25832),
  7.2, 12.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091567.5, 6091542.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457628.5 6091567.5, 457628.5 6091565.5, 457621.5 6091564.5, 457618.5 6091563.5, 457611.5 6091562.5, 457613.5 6091560.5, 457616.5 6091559.5, 457615.5 6091557.5, 457615.5 6091555.5, 457618.5 6091553.5, 457612.5 6091551.5, 457611.5 6091549.5, 457616.5 6091547.5, 457617.5 6091545.5, 457614.5 6091544.5, 457612.5 6091542.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457601.5 6091567.5, 457599.5 6091565.5, 457598.5 6091563.5, 457597.5 6091561.5, 457596.5 6091559.5, 457595.5 6091557.5, 457594.5 6091555.5, 457593.5 6091553.5, 457592.5 6091551.5, 457591.5 6091549.5, 457590.5 6091547.5, 457589.5 6091545.5, 457588.5 6091543.5, 457588.5 6091542.5)', 25832),
  6.9, 12.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091542.5, 6091517.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457612.5 6091542.5, 457614.5 6091541.5, 457606.5 6091539.5, 457605.5 6091537.5, 457604.5 6091535.5, 457603.5 6091533.5, 457605.5 6091531.5, 457602.5 6091529.5, 457605.5 6091528.5, 457607.5 6091526.5, 457606.5 6091524.5, 457601.5 6091523.5, 457604.5 6091522.5, 457600.5 6091521.5, 457602.5 6091520.5, 457599.5 6091519.5, 457596.5 6091518.5, 457597.5 6091517.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457588.5 6091542.5, 457587.5 6091540.5, 457586.5 6091538.5, 457585.5 6091536.5, 457584.5 6091534.5, 457583.5 6091532.5, 457582.5 6091530.5, 457581.5 6091528.5, 457580.5 6091526.5, 457579.5 6091524.5, 457579.5 6091522.5, 457577.5 6091520.5, 457576.5 6091518.5, 457575.5 6091517.5)', 25832),
  7.2, 12.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091517.5, 6091492.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457597.5 6091517.5, 457598.5 6091515.5, 457599.5 6091513.5, 457601.5 6091512.5, 457597.5 6091510.5, 457595.5 6091509.5, 457597.5 6091508.5, 457598.5 6091506.5, 457597.5 6091504.5, 457592.5 6091503.5, 457588.5 6091502.5, 457592.5 6091501.5, 457589.5 6091500.5, 457585.5 6091498.5, 457588.5 6091497.5, 457590.5 6091496.5, 457587.5 6091495.5, 457584.5 6091493.5, 457585.5 6091492.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457575.5 6091517.5, 457575.5 6091515.5, 457574.5 6091513.5, 457573.5 6091511.5, 457572.5 6091509.5, 457571.5 6091507.5, 457570.5 6091505.5, 457569.5 6091503.5, 457568.5 6091501.5, 457567.5 6091499.5, 457567.5 6091497.5, 457566.5 6091495.5, 457565.5 6091493.5, 457565.5 6091492.5)', 25832),
  7.3, 13, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091492.5, 6091466.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457585.5 6091492.5, 457583.5 6091490.5, 457584.5 6091488.5, 457587.5 6091486.5, 457582.5 6091484.5, 457581.5 6091482.5, 457582.5 6091480.5, 457581.5 6091478.5, 457581.5 6091476.5, 457580.5 6091474.5, 457578.5 6091473.5, 457582.5 6091472.5, 457578.5 6091471.5, 457583.5 6091469.5, 457579.5 6091468.5, 457582.5 6091467.5, 457581.5 6091466.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457565.5 6091492.5, 457564.5 6091490.5, 457563.5 6091488.5, 457563.5 6091486.5, 457562.5 6091484.5, 457561.5 6091482.5, 457560.5 6091480.5, 457559.5 6091478.5, 457558.5 6091476.5, 457558.5 6091474.5, 457557.5 6091472.5, 457556.5 6091470.5, 457555.5 6091468.5, 457554.5 6091466.5)', 25832),
  7.5, 13.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6091466.5, 6091435.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457581.5 6091466.5, 457578.5 6091464.5, 457575.5 6091463.5, 457573.5 6091461.5, 457575.5 6091459.5, 457573.5 6091457.5, 457571.5 6091455.5, 457569.5 6091453.5, 457568.5 6091451.5, 457565.5 6091449.5, 457569.5 6091448.5, 457568.5 6091446.5, 457568.5 6091444.5, 457565.5 6091443.5, 457564.5 6091441.5, 457564.5 6091439.5, 457562.5 6091438.5, 457564.5 6091436.5, 457563.5 6091435.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457554.5 6091466.5, 457553.5 6091464.5, 457552.5 6091462.5, 457551.5 6091460.5, 457550.5 6091458.5, 457549.5 6091456.5, 457548.5 6091454.5, 457548.5 6091452.5, 457547.5 6091450.5, 457546.5 6091448.5, 457545.5 6091446.5, 457544.5 6091444.5, 457543.5 6091442.5, 457542.5 6091440.5, 457541.5 6091438.5, 457540.5 6091436.5, 457540.5 6091435.5)', 25832),
  7.8, 13.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090895.5, 6090870.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457287.5 6090895.5, 457284.5 6090894.5, 457283.5 6090892.5, 457282.5 6090890.5, 457282.5 6090888.5, 457282.5 6090886.5, 457280.5 6090885.5, 457280.5 6090883.5, 457278.5 6090881.5, 457276.5 6090879.5, 457275.5 6090877.5, 457274.5 6090875.5, 457274.5 6090873.5, 457273.5 6090871.5, 457273.5 6090870.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457265.5 6090895.5, 457265.5 6090893.5, 457264.5 6090891.5, 457263.5 6090889.5, 457261.5 6090887.5, 457261.5 6090885.5, 457259.5 6090883.5, 457258.5 6090881.5, 457258.5 6090879.5, 457256.5 6090877.5, 457256.5 6090875.5, 457254.5 6090873.5, 457253.5 6090871.5, 457253.5 6090870.5)', 25832),
  9.9, 14.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090870.5, 6090845.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457273.5 6090870.5, 457268.5 6090869.5, 457267.5 6090867.5, 457274.5 6090865.5, 457271.5 6090863.5, 457273.5 6090862.5, 457266.5 6090861.5, 457272.5 6090860.5, 457266.5 6090859.5, 457272.5 6090858.5, 457269.5 6090857.5, 457264.5 6090856.5, 457265.5 6090854.5, 457263.5 6090853.5, 457264.5 6090851.5, 457262.5 6090849.5, 457263.5 6090847.5, 457262.5 6090845.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457253.5 6090870.5, 457252.5 6090868.5, 457251.5 6090866.5, 457251.5 6090864.5, 457251.5 6090862.5, 457250.5 6090860.5, 457246.5 6090859.5, 457248.5 6090858.5, 457249.5 6090856.5, 457250.5 6090854.5, 457249.5 6090852.5, 457248.5 6090850.5, 457248.5 6090848.5, 457246.5 6090847.5, 457223.5 6090846.5, 457223.5 6090845.5)', 25832),
  10.8, 14.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090845.5, 6090820.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457262.5 6090845.5, 457261.5 6090843.5, 457262.5 6090841.5, 457261.5 6090839.5, 457259.5 6090837.5, 457259.5 6090835.5, 457258.5 6090833.5, 457257.5 6090831.5, 457257.5 6090829.5, 457255.5 6090827.5, 457255.5 6090825.5, 457256.5 6090823.5, 457258.5 6090822.5, 457260.5 6090820.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457223.5 6090845.5, 457223.5 6090843.5, 457222.5 6090841.5, 457222.5 6090839.5, 457222.5 6090837.5, 457221.5 6090835.5, 457221.5 6090833.5, 457221.5 6090831.5, 457220.5 6090829.5, 457220.5 6090827.5, 457220.5 6090825.5, 457219.5 6090823.5, 457218.5 6090821.5, 457218.5 6090820.5)', 25832),
  12, 15.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090820.5, 6090795.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457260.5 6090820.5, 457259.5 6090818.5, 457254.5 6090816.5, 457253.5 6090814.5, 457255.5 6090813.5, 457252.5 6090812.5, 457247.5 6090811.5, 457251.5 6090810.5, 457245.5 6090808.5, 457248.5 6090806.5, 457246.5 6090804.5, 457245.5 6090802.5, 457243.5 6090801.5, 457249.5 6090799.5, 457245.5 6090798.5, 457248.5 6090797.5, 457248.5 6090795.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457218.5 6090820.5, 457217.5 6090818.5, 457216.5 6090816.5, 457215.5 6090814.5, 457214.5 6090812.5, 457213.5 6090810.5, 457212.5 6090808.5, 457211.5 6090806.5, 457210.5 6090804.5, 457208.5 6090802.5, 457207.5 6090800.5, 457207.5 6090798.5, 457205.5 6090796.5, 457205.5 6090795.5)', 25832),
  11.9, 15.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090795.5, 6090770.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457248.5 6090795.5, 457250.5 6090794.5, 457246.5 6090793.5, 457249.5 6090791.5, 457245.5 6090790.5, 457244.5 6090788.5, 457246.5 6090787.5, 457243.5 6090785.5, 457243.5 6090783.5, 457241.5 6090781.5, 457241.5 6090779.5, 457237.5 6090778.5, 457238.5 6090776.5, 457237.5 6090774.5, 457236.5 6090772.5, 457234.5 6090770.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457205.5 6090795.5, 457204.5 6090793.5, 457203.5 6090791.5, 457202.5 6090789.5, 457201.5 6090787.5, 457200.5 6090785.5, 457199.5 6090783.5, 457198.5 6090781.5, 457197.5 6090779.5, 457197.5 6090777.5, 457196.5 6090775.5, 457195.5 6090773.5, 457194.5 6090771.5, 457194.5 6090770.5)', 25832),
  13.1, 16.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090770.5, 6090745.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457234.5 6090770.5, 457233.5 6090768.5, 457232.5 6090766.5, 457234.5 6090765.5, 457233.5 6090763.5, 457232.5 6090761.5, 457225.5 6090760.5, 457224.5 6090758.5, 457231.5 6090756.5, 457230.5 6090754.5, 457228.5 6090753.5, 457228.5 6090751.5, 457226.5 6090750.5, 457226.5 6090748.5, 457223.5 6090746.5, 457223.5 6090745.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457194.5 6090770.5, 457193.5 6090768.5, 457192.5 6090766.5, 457191.5 6090764.5, 457190.5 6090762.5, 457189.5 6090760.5, 457187.5 6090758.5, 457187.5 6090756.5, 457186.5 6090754.5, 457185.5 6090752.5, 457184.5 6090750.5, 457184.5 6090748.5, 457210.5 6090746.5, 457209.5 6090745.5)', 25832),
  12.8, 16.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090745.5, 6090720.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457223.5 6090745.5, 457223.5 6090743.5, 457222.5 6090741.5, 457215.5 6090739.5, 457219.5 6090737.5, 457214.5 6090736.5, 457212.5 6090734.5, 457213.5 6090732.5, 457215.5 6090731.5, 457214.5 6090729.5, 457213.5 6090727.5, 457213.5 6090725.5, 457212.5 6090723.5, 457214.5 6090722.5, 457214.5 6090720.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457209.5 6090745.5, 457209.5 6090743.5, 457208.5 6090741.5, 457207.5 6090739.5, 457206.5 6090737.5, 457205.5 6090735.5, 457204.5 6090733.5, 457178.5 6090732.5, 457177.5 6090730.5, 457176.5 6090728.5, 457175.5 6090726.5, 457174.5 6090724.5, 457201.5 6090723.5, 457200.5 6090721.5, 457200.5 6090720.5)', 25832),
  12.9, 16.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090720.5, 6090695.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457214.5 6090720.5, 457216.5 6090719.5, 457213.5 6090718.5, 457206.5 6090716.5, 457207.5 6090714.5, 457205.5 6090713.5, 457202.5 6090711.5, 457200.5 6090709.5, 457201.5 6090707.5, 457199.5 6090706.5, 457198.5 6090704.5, 457197.5 6090702.5, 457197.5 6090700.5, 457195.5 6090698.5, 457194.5 6090696.5, 457194.5 6090695.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457200.5 6090720.5, 457171.5 6090718.5, 457170.5 6090716.5, 457196.5 6090715.5, 457195.5 6090713.5, 457194.5 6090711.5, 457193.5 6090709.5, 457191.5 6090707.5, 457191.5 6090705.5, 457190.5 6090703.5, 457163.5 6090701.5, 457162.5 6090699.5, 457187.5 6090698.5, 457162.5 6090697.5, 457161.5 6090695.5)', 25832),
  14.1, 18.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090695.5, 6090670.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457194.5 6090695.5, 457193.5 6090693.5, 457192.5 6090691.5, 457192.5 6090689.5, 457191.5 6090687.5, 457192.5 6090685.5, 457190.5 6090684.5, 457189.5 6090682.5, 457187.5 6090680.5, 457185.5 6090678.5, 457185.5 6090676.5, 457184.5 6090674.5, 457183.5 6090672.5, 457184.5 6090670.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457161.5 6090695.5, 457160.5 6090693.5, 457160.5 6090691.5, 457159.5 6090689.5, 457158.5 6090687.5, 457157.5 6090685.5, 457156.5 6090683.5, 457155.5 6090681.5, 457154.5 6090679.5, 457154.5 6090677.5, 457153.5 6090675.5, 457152.5 6090673.5, 457152.5 6090671.5, 457151.5 6090670.5)', 25832),
  15.2, 19.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090670.5, 6090645.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457184.5 6090670.5, 457183.5 6090668.5, 457186.5 6090667.5, 457185.5 6090665.5, 457184.5 6090663.5, 457183.5 6090661.5, 457182.5 6090659.5, 457182.5 6090657.5, 457182.5 6090655.5, 457173.5 6090653.5, 457173.5 6090651.5, 457172.5 6090649.5, 457180.5 6090647.5, 457180.5 6090645.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457151.5 6090670.5, 457151.5 6090668.5, 457149.5 6090666.5, 457171.5 6090664.5, 457147.5 6090662.5, 457146.5 6090660.5, 457146.5 6090658.5, 457145.5 6090656.5, 457144.5 6090654.5, 457142.5 6090652.5, 457142.5 6090650.5, 457142.5 6090648.5, 457141.5 6090646.5, 457159.5 6090645.5)', 25832),
  15.6, 19.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090645.5, 6090620.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457180.5 6090645.5, 457179.5 6090643.5, 457173.5 6090642.5, 457173.5 6090640.5, 457171.5 6090639.5, 457169.5 6090637.5, 457168.5 6090635.5, 457170.5 6090634.5, 457169.5 6090632.5, 457167.5 6090630.5, 457167.5 6090628.5, 457165.5 6090627.5, 457165.5 6090625.5, 457163.5 6090623.5, 457164.5 6090621.5, 457162.5 6090620.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457159.5 6090645.5, 457158.5 6090643.5, 457137.5 6090642.5, 457158.5 6090641.5, 457138.5 6090640.5, 457140.5 6090639.5, 457137.5 6090638.5, 457139.5 6090637.5, 457157.5 6090636.5, 457155.5 6090634.5, 457137.5 6090633.5, 457132.5 6090632.5, 457134.5 6090631.5, 457134.5 6090629.5, 457134.5 6090627.5, 457152.5 6090625.5, 457151.5 6090623.5, 457149.5 6090621.5, 457150.5 6090620.5)', 25832),
  16.1, 20.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090620.5, 6090595.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457162.5 6090620.5, 457164.5 6090618.5, 457162.5 6090616.5, 457160.5 6090615.5, 457160.5 6090613.5, 457159.5 6090611.5, 457159.5 6090609.5, 457158.5 6090607.5, 457157.5 6090605.5, 457157.5 6090603.5, 457156.5 6090601.5, 457155.5 6090599.5, 457151.5 6090597.5, 457153.5 6090595.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457150.5 6090620.5, 457149.5 6090618.5, 457149.5 6090616.5, 457149.5 6090614.5, 457147.5 6090612.5, 457147.5 6090610.5, 457146.5 6090608.5, 457145.5 6090606.5, 457144.5 6090604.5, 457142.5 6090603.5, 457143.5 6090601.5, 457142.5 6090599.5, 457142.5 6090597.5, 457141.5 6090595.5)', 25832),
  15, 21.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090595.5, 6090570.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457153.5 6090595.5, 457150.5 6090594.5, 457152.5 6090592.5, 457152.5 6090590.5, 457152.5 6090588.5, 457152.5 6090586.5, 457149.5 6090585.5, 457148.5 6090583.5, 457148.5 6090581.5, 457150.5 6090579.5, 457147.5 6090578.5, 457150.5 6090576.5, 457153.5 6090574.5, 457151.5 6090573.5, 457150.5 6090571.5, 457150.5 6090570.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457141.5 6090595.5, 457114.5 6090593.5, 457114.5 6090591.5, 457113.5 6090589.5, 457113.5 6090587.5, 457112.5 6090585.5, 457111.5 6090583.5, 457110.5 6090581.5, 457110.5 6090579.5, 457109.5 6090577.5, 457108.5 6090575.5, 457108.5 6090573.5, 457107.5 6090571.5, 457106.5 6090570.5)', 25832),
  18.3, 22.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090570.5, 6090545.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457150.5 6090570.5, 457144.5 6090569.5, 457141.5 6090568.5, 457148.5 6090566.5, 457142.5 6090565.5, 457138.5 6090563.5, 457138.5 6090561.5, 457146.5 6090559.5, 457145.5 6090557.5, 457145.5 6090555.5, 457143.5 6090553.5, 457141.5 6090551.5, 457140.5 6090549.5, 457134.5 6090547.5, 457135.5 6090545.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457106.5 6090570.5, 457106.5 6090568.5, 457105.5 6090566.5, 457104.5 6090564.5, 457104.5 6090562.5, 457103.5 6090560.5, 457102.5 6090558.5, 457102.5 6090556.5, 457101.5 6090554.5, 457101.5 6090552.5, 457099.5 6090550.5, 457099.5 6090548.5, 457098.5 6090546.5, 457097.5 6090545.5)', 25832),
  20.2, 24.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090545.5, 6090520.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457135.5 6090545.5, 457134.5 6090543.5, 457132.5 6090542.5, 457133.5 6090540.5, 457132.5 6090538.5, 457132.5 6090536.5, 457131.5 6090534.5, 457131.5 6090532.5, 457131.5 6090530.5, 457131.5 6090528.5, 457131.5 6090526.5, 457131.5 6090524.5, 457130.5 6090522.5, 457128.5 6090520.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457097.5 6090545.5, 457096.5 6090543.5, 457096.5 6090541.5, 457116.5 6090540.5, 457115.5 6090538.5, 457115.5 6090536.5, 457113.5 6090534.5, 457113.5 6090532.5, 457112.5 6090530.5, 457111.5 6090528.5, 457110.5 6090526.5, 457109.5 6090524.5, 457108.5 6090522.5, 457107.5 6090520.5)', 25832),
  20.2, 26.3, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090520.5, 6090495.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457128.5 6090520.5, 457127.5 6090518.5, 457120.5 6090517.5, 457121.5 6090515.5, 457122.5 6090513.5, 457119.5 6090512.5, 457118.5 6090510.5, 457128.5 6090508.5, 457118.5 6090507.5, 457124.5 6090506.5, 457128.5 6090505.5, 457128.5 6090503.5, 457127.5 6090501.5, 457127.5 6090499.5, 457127.5 6090497.5, 457121.5 6090496.5, 457121.5 6090495.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457107.5 6090520.5, 457106.5 6090518.5, 457105.5 6090516.5, 457084.5 6090514.5, 457102.5 6090513.5, 457102.5 6090511.5, 457082.5 6090510.5, 457082.5 6090508.5, 457100.5 6090506.5, 457080.5 6090504.5, 457077.5 6090502.5, 457067.5 6090500.5, 457067.5 6090498.5, 457068.5 6090496.5, 457067.5 6090495.5)', 25832),
  24.6, 28.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090495.5, 6090470.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457121.5 6090495.5, 457121.5 6090493.5, 457121.5 6090491.5, 457125.5 6090489.5, 457121.5 6090488.5, 457120.5 6090486.5, 457119.5 6090484.5, 457119.5 6090482.5, 457114.5 6090481.5, 457117.5 6090480.5, 457117.5 6090478.5, 457108.5 6090477.5, 457118.5 6090476.5, 457118.5 6090474.5, 457117.5 6090472.5, 457117.5 6090470.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457067.5 6090495.5, 457067.5 6090493.5, 457067.5 6090491.5, 457066.5 6090489.5, 457065.5 6090487.5, 457065.5 6090485.5, 457064.5 6090483.5, 457064.5 6090481.5, 457063.5 6090479.5, 457061.5 6090477.5, 457045.5 6090476.5, 457045.5 6090474.5, 457044.5 6090472.5, 457044.5 6090470.5)', 25832),
  27.7, 29.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090470.5, 6090445.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457117.5 6090470.5, 457117.5 6090468.5, 457117.5 6090466.5, 457118.5 6090464.5, 457119.5 6090462.5, 457119.5 6090460.5, 457120.5 6090458.5, 457120.5 6090456.5, 457122.5 6090455.5, 457100.5 6090453.5, 457099.5 6090451.5, 457097.5 6090449.5, 457097.5 6090447.5, 457098.5 6090445.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457044.5 6090470.5, 457043.5 6090468.5, 457043.5 6090466.5, 457042.5 6090464.5, 457042.5 6090462.5, 457041.5 6090460.5, 457041.5 6090458.5, 457041.5 6090456.5, 457040.5 6090454.5, 457040.5 6090452.5, 457039.5 6090450.5, 457039.5 6090448.5, 457038.5 6090446.5, 457038.5 6090445.5)', 25832),
  28, 29.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090445.5, 6090420.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457098.5 6090445.5, 457098.5 6090443.5, 457102.5 6090442.5, 457096.5 6090441.5, 457094.5 6090439.5, 457094.5 6090437.5, 457092.5 6090436.5, 457090.5 6090435.5, 457088.5 6090433.5, 457114.5 6090432.5, 457086.5 6090431.5, 457086.5 6090429.5, 457086.5 6090427.5, 457087.5 6090425.5, 457084.5 6090424.5, 457084.5 6090422.5, 457091.5 6090421.5, 457092.5 6090420.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457038.5 6090445.5, 457037.5 6090443.5, 457037.5 6090441.5, 457036.5 6090439.5, 457036.5 6090437.5, 457035.5 6090435.5, 457034.5 6090433.5, 457034.5 6090431.5, 457033.5 6090429.5, 457032.5 6090427.5, 457032.5 6090425.5, 457031.5 6090423.5, 457031.5 6090421.5, 457030.5 6090420.5)', 25832),
  28.2, 30.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090420.5, 6090395.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457092.5 6090420.5, 457083.5 6090419.5, 457083.5 6090417.5, 457082.5 6090415.5, 457084.5 6090414.5, 457103.5 6090413.5, 457102.5 6090411.5, 457101.5 6090409.5, 457083.5 6090407.5, 457083.5 6090405.5, 457084.5 6090403.5, 457078.5 6090402.5, 457078.5 6090400.5, 457078.5 6090398.5, 457078.5 6090396.5, 457079.5 6090395.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457030.5 6090420.5, 457030.5 6090418.5, 457029.5 6090416.5, 457028.5 6090414.5, 457028.5 6090412.5, 457027.5 6090410.5, 457026.5 6090408.5, 457026.5 6090406.5, 457025.5 6090404.5, 457024.5 6090402.5, 457024.5 6090400.5, 457023.5 6090398.5, 457022.5 6090396.5, 457022.5 6090395.5)', 25832),
  29.2, 31.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090395.5, 6090370.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457079.5 6090395.5, 457081.5 6090393.5, 457077.5 6090391.5, 457081.5 6090389.5, 457074.5 6090388.5, 457074.5 6090386.5, 457076.5 6090385.5, 457076.5 6090383.5, 457075.5 6090381.5, 457073.5 6090380.5, 457073.5 6090378.5, 457072.5 6090376.5, 457079.5 6090375.5, 457079.5 6090373.5, 457076.5 6090372.5, 457074.5 6090371.5, 457074.5 6090370.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457022.5 6090395.5, 457021.5 6090393.5, 457020.5 6090391.5, 457020.5 6090389.5, 457019.5 6090387.5, 457018.5 6090385.5, 457017.5 6090383.5, 457016.5 6090381.5, 457016.5 6090379.5, 457015.5 6090377.5, 457014.5 6090375.5, 457013.5 6090373.5, 457013.5 6090371.5, 457012.5 6090370.5)', 25832),
  26.9, 28.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090370.5, 6090345.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457074.5 6090370.5, 457070.5 6090369.5, 457076.5 6090368.5, 457070.5 6090367.5, 457069.5 6090365.5, 457069.5 6090363.5, 457067.5 6090362.5, 457087.5 6090360.5, 457087.5 6090358.5, 457087.5 6090356.5, 457087.5 6090354.5, 457087.5 6090352.5, 457071.5 6090350.5, 457071.5 6090348.5, 457071.5 6090346.5, 457067.5 6090345.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457012.5 6090370.5, 457011.5 6090368.5, 457010.5 6090366.5, 457010.5 6090364.5, 457009.5 6090362.5, 457008.5 6090360.5, 457008.5 6090358.5, 457007.5 6090356.5, 457006.5 6090354.5, 457005.5 6090352.5, 457004.5 6090350.5, 457003.5 6090348.5, 457002.5 6090346.5, 457002.5 6090345.5)', 25832),
  31, 33, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090345.5, 6090320.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457067.5 6090345.5, 457079.5 6090344.5, 457071.5 6090342.5, 457066.5 6090341.5, 457070.5 6090339.5, 457073.5 6090338.5, 457074.5 6090336.5, 457077.5 6090335.5, 457069.5 6090334.5, 457066.5 6090332.5, 457064.5 6090331.5, 457072.5 6090329.5, 457070.5 6090328.5, 457068.5 6090326.5, 457067.5 6090324.5, 457068.5 6090322.5, 457064.5 6090320.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (457002.5 6090345.5, 457001.5 6090343.5, 457000.5 6090341.5, 457000.5 6090339.5, 456999.5 6090337.5, 456998.5 6090335.5, 456997.5 6090333.5, 456996.5 6090331.5, 456995.5 6090329.5, 456994.5 6090327.5, 456993.5 6090325.5, 456992.5 6090323.5, 456992.5 6090321.5, 456991.5 6090320.5)', 25832),
  31.4, 33.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090320.5, 6090295.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457064.5 6090320.5, 457067.5 6090319.5, 457066.5 6090317.5, 457064.5 6090316.5, 457058.5 6090315.5, 457063.5 6090314.5, 457063.5 6090312.5, 457061.5 6090311.5, 457061.5 6090309.5, 457062.5 6090307.5, 457015.5 6090306.5, 457013.5 6090305.5, 457010.5 6090304.5, 457007.5 6090303.5, 457062.5 6090302.5, 457062.5 6090300.5, 457010.5 6090299.5, 457061.5 6090298.5, 457061.5 6090296.5, 457062.5 6090295.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456991.5 6090320.5, 456990.5 6090318.5, 456989.5 6090316.5, 456989.5 6090314.5, 456987.5 6090312.5, 456986.5 6090310.5, 456985.5 6090308.5, 456984.5 6090306.5, 456983.5 6090304.5, 456982.5 6090302.5, 456981.5 6090300.5, 456980.5 6090298.5, 456979.5 6090296.5, 456979.5 6090295.5)', 25832),
  33.6, 35.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090295.5, 6090270.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457062.5 6090295.5, 457062.5 6090293.5, 457062.5 6090291.5, 457062.5 6090289.5, 457061.5 6090287.5, 457061.5 6090285.5, 457056.5 6090283.5, 457059.5 6090282.5, 457058.5 6090280.5, 457059.5 6090278.5, 457058.5 6090276.5, 457058.5 6090274.5, 457060.5 6090273.5, 457055.5 6090272.5, 457054.5 6090270.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456979.5 6090295.5, 456978.5 6090293.5, 456977.5 6090291.5, 456976.5 6090289.5, 456975.5 6090287.5, 456974.5 6090285.5, 456973.5 6090283.5, 456972.5 6090281.5, 456971.5 6090279.5, 456969.5 6090277.5, 456969.5 6090275.5, 456968.5 6090273.5, 456967.5 6090271.5, 456967.5 6090270.5)', 25832),
  34.9, 37, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090270.5, 6090245.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457054.5 6090270.5, 457042.5 6090269.5, 457041.5 6090267.5, 457046.5 6090265.5, 457039.5 6090263.5, 457039.5 6090261.5, 457038.5 6090259.5, 457036.5 6090257.5, 457030.5 6090256.5, 457033.5 6090255.5, 457035.5 6090253.5, 457036.5 6090251.5, 457038.5 6090249.5, 457038.5 6090247.5, 457038.5 6090245.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456967.5 6090270.5, 456966.5 6090268.5, 456965.5 6090266.5, 456964.5 6090264.5, 456963.5 6090262.5, 456965.5 6090261.5, 456963.5 6090260.5, 456963.5 6090258.5, 456962.5 6090256.5, 456961.5 6090254.5, 456960.5 6090252.5, 456959.5 6090250.5, 456957.5 6090249.5, 456960.5 6090248.5, 456959.5 6090246.5, 456959.5 6090245.5)', 25832),
  34.3, 36.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090245.5, 6090220.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457038.5 6090245.5, 457038.5 6090243.5, 457037.5 6090241.5, 457036.5 6090239.5, 457035.5 6090237.5, 457034.5 6090235.5, 457034.5 6090233.5, 457036.5 6090231.5, 457036.5 6090229.5, 457034.5 6090228.5, 457036.5 6090227.5, 457034.5 6090225.5, 457034.5 6090223.5, 457034.5 6090221.5, 457034.5 6090220.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456959.5 6090245.5, 456958.5 6090243.5, 456957.5 6090241.5, 456956.5 6090239.5, 456956.5 6090237.5, 456954.5 6090235.5, 456953.5 6090233.5, 456953.5 6090231.5, 456951.5 6090229.5, 456950.5 6090227.5, 456949.5 6090225.5, 456957.5 6090223.5, 456948.5 6090222.5, 456947.5 6090220.5)', 25832),
  32.1, 34.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090220.5, 6090195.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457034.5 6090220.5, 456978.5 6090219.5, 457033.5 6090217.5, 457033.5 6090215.5, 457032.5 6090213.5, 457033.5 6090211.5, 457037.5 6090210.5, 457039.5 6090209.5, 457040.5 6090207.5, 457038.5 6090205.5, 457039.5 6090203.5, 457038.5 6090201.5, 457038.5 6090199.5, 457035.5 6090197.5, 457035.5 6090195.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456947.5 6090220.5, 456948.5 6090218.5, 456947.5 6090216.5, 456946.5 6090214.5, 456945.5 6090212.5, 456945.5 6090210.5, 456947.5 6090209.5, 456951.5 6090207.5, 456950.5 6090205.5, 456948.5 6090203.5, 456948.5 6090201.5, 456947.5 6090199.5, 456946.5 6090197.5, 456945.5 6090195.5)', 25832),
  32.8, 35.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090195.5, 6090170.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457035.5 6090195.5, 457034.5 6090193.5, 457035.5 6090191.5, 457034.5 6090189.5, 457010.5 6090188.5, 457006.5 6090187.5, 457030.5 6090185.5, 457029.5 6090183.5, 457000.5 6090182.5, 457029.5 6090181.5, 457028.5 6090179.5, 457000.5 6090178.5, 456998.5 6090177.5, 456999.5 6090175.5, 457029.5 6090174.5, 457029.5 6090172.5, 457031.5 6090171.5, 457029.5 6090170.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456945.5 6090195.5, 456944.5 6090193.5, 456939.5 6090191.5, 456938.5 6090189.5, 456937.5 6090187.5, 456938.5 6090185.5, 456937.5 6090183.5, 456937.5 6090181.5, 456937.5 6090179.5, 456937.5 6090177.5, 456937.5 6090175.5, 456936.5 6090173.5, 456935.5 6090171.5, 456934.5 6090170.5)', 25832),
  32.5, 35.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090170.5, 6090145.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457029.5 6090170.5, 457030.5 6090168.5, 457031.5 6090166.5, 456994.5 6090165.5, 456994.5 6090163.5, 456988.5 6090162.5, 456991.5 6090161.5, 456991.5 6090159.5, 456990.5 6090157.5, 456989.5 6090155.5, 456989.5 6090153.5, 456989.5 6090151.5, 456998.5 6090149.5, 456997.5 6090147.5, 456994.5 6090145.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456934.5 6090170.5, 456933.5 6090168.5, 456932.5 6090166.5, 456930.5 6090164.5, 456929.5 6090162.5, 456928.5 6090160.5, 456927.5 6090158.5, 456927.5 6090156.5, 456928.5 6090154.5, 456929.5 6090152.5, 456929.5 6090150.5, 456928.5 6090148.5, 456932.5 6090147.5, 456921.5 6090145.5)', 25832),
  33.2, 36.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090145.5, 6090120.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456994.5 6090145.5, 456992.5 6090143.5, 456992.5 6090141.5, 456990.5 6090140.5, 456987.5 6090138.5, 456985.5 6090137.5, 456982.5 6090136.5, 456984.5 6090135.5, 456978.5 6090133.5, 456980.5 6090131.5, 456982.5 6090130.5, 456933.5 6090128.5, 456981.5 6090126.5, 456980.5 6090124.5, 456973.5 6090123.5, 457011.5 6090121.5, 457010.5 6090120.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456921.5 6090145.5, 456920.5 6090143.5, 456919.5 6090141.5, 456919.5 6090139.5, 456917.5 6090137.5, 456916.5 6090135.5, 456915.5 6090133.5, 456914.5 6090131.5, 456916.5 6090130.5, 456912.5 6090129.5, 456914.5 6090128.5, 456910.5 6090127.5, 456913.5 6090126.5, 456912.5 6090124.5, 456902.5 6090123.5, 456909.5 6090122.5, 456910.5 6090120.5)', 25832),
  32.7, 35.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090120.5, 6090095.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457010.5 6090120.5, 457008.5 6090119.5, 457006.5 6090117.5, 457004.5 6090116.5, 457001.5 6090114.5, 457001.5 6090112.5, 456999.5 6090111.5, 456926.5 6090110.5, 457000.5 6090108.5, 456925.5 6090107.5, 457000.5 6090106.5, 456964.5 6090105.5, 457001.5 6090104.5, 456966.5 6090103.5, 456999.5 6090101.5, 456966.5 6090100.5, 456975.5 6090099.5, 456999.5 6090098.5, 456964.5 6090097.5, 457000.5 6090096.5, 457000.5 6090095.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456910.5 6090120.5, 456907.5 6090118.5, 456899.5 6090117.5, 456899.5 6090115.5, 456898.5 6090113.5, 456897.5 6090111.5, 456896.5 6090109.5, 456896.5 6090107.5, 456895.5 6090105.5, 456895.5 6090103.5, 456894.5 6090101.5, 456894.5 6090099.5, 456893.5 6090097.5, 456894.5 6090095.5)', 25832),
  34.1, 36.3, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090095.5, 6090070.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (457000.5 6090095.5, 456960.5 6090093.5, 456920.5 6090092.5, 456920.5 6090090.5, 456953.5 6090089.5, 456917.5 6090087.5, 456953.5 6090086.5, 456954.5 6090084.5, 456955.5 6090082.5, 456953.5 6090081.5, 456948.5 6090080.5, 456953.5 6090079.5, 456953.5 6090077.5, 456953.5 6090075.5, 456952.5 6090073.5, 456908.5 6090072.5, 456950.5 6090070.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456894.5 6090095.5, 456895.5 6090093.5, 456895.5 6090091.5, 456895.5 6090089.5, 456895.5 6090087.5, 456895.5 6090085.5, 456894.5 6090083.5, 456894.5 6090081.5, 456893.5 6090079.5, 456892.5 6090077.5, 456891.5 6090075.5, 456891.5 6090073.5, 456890.5 6090071.5, 456890.5 6090070.5)', 25832),
  34, 36.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090070.5, 6090045.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456950.5 6090070.5, 456948.5 6090068.5, 456944.5 6090066.5, 456949.5 6090064.5, 456948.5 6090062.5, 456946.5 6090061.5, 456946.5 6090059.5, 456944.5 6090058.5, 456939.5 6090056.5, 456939.5 6090054.5, 456946.5 6090053.5, 456944.5 6090051.5, 456945.5 6090049.5, 456937.5 6090047.5, 456944.5 6090046.5, 456943.5 6090045.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456890.5 6090070.5, 456889.5 6090068.5, 456889.5 6090066.5, 456888.5 6090064.5, 456887.5 6090062.5, 456887.5 6090060.5, 456886.5 6090058.5, 456907.5 6090057.5, 456885.5 6090056.5, 456885.5 6090054.5, 456885.5 6090052.5, 456884.5 6090050.5, 456883.5 6090048.5, 456882.5 6090046.5, 456881.5 6090045.5)', 25832),
  23.9, 26.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090045.5, 6090019.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456943.5 6090045.5, 456938.5 6090043.5, 456941.5 6090042.5, 456933.5 6090041.5, 456939.5 6090039.5, 456937.5 6090038.5, 456900.5 6090035.5, 456898.5 6090034.5, 456930.5 6090033.5, 456932.5 6090032.5, 456929.5 6090031.5, 456932.5 6090030.5, 456934.5 6090028.5, 456933.5 6090026.5, 456928.5 6090025.5, 456931.5 6090024.5, 456934.5 6090023.5, 456934.5 6090021.5, 456935.5 6090019.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456881.5 6090045.5, 456880.5 6090043.5, 456880.5 6090041.5, 456879.5 6090039.5, 456878.5 6090037.5, 456877.5 6090035.5, 456876.5 6090033.5, 456875.5 6090031.5, 456896.5 6090029.5, 456895.5 6090027.5, 456874.5 6090026.5, 456892.5 6090024.5, 456873.5 6090023.5, 456872.5 6090021.5, 456872.5 6090019.5)', 25832),
  23.2, 26.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6090019.5, 6089994.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456935.5 6090019.5, 456931.5 6090018.5, 456931.5 6090016.5, 456930.5 6090014.5, 456929.5 6090012.5, 456933.5 6090011.5, 456931.5 6090009.5, 456930.5 6090007.5, 456928.5 6090005.5, 456928.5 6090003.5, 456928.5 6090001.5, 456927.5 6089999.5, 456924.5 6089998.5, 456927.5 6089997.5, 456924.5 6089995.5, 456923.5 6089994.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456872.5 6090019.5, 456870.5 6090017.5, 456870.5 6090015.5, 456870.5 6090013.5, 456869.5 6090011.5, 456869.5 6090009.5, 456868.5 6090007.5, 456867.5 6090005.5, 456866.5 6090003.5, 456866.5 6090001.5, 456865.5 6089999.5, 456865.5 6089997.5, 456864.5 6089995.5, 456864.5 6089994.5)', 25832),
  23, 26.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089994.5, 6089958.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456923.5 6089994.5, 456923.5 6089992.5, 456926.5 6089991.5, 456925.5 6089989.5, 456919.5 6089988.5, 456913.5 6089986.5, 456916.5 6089984.5, 456919.5 6089983.5, 456922.5 6089981.5, 456920.5 6089980.5, 456974.5 6089978.5, 456917.5 6089977.5, 456910.5 6089975.5, 456910.5 6089973.5, 456913.5 6089971.5, 456917.5 6089970.5, 456915.5 6089968.5, 456914.5 6089966.5, 456912.5 6089965.5, 456911.5 6089963.5, 456905.5 6089962.5, 456906.5 6089960.5, 456910.5 6089959.5, 456909.5 6089958.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456864.5 6089994.5, 456863.5 6089992.5, 456862.5 6089990.5, 456861.5 6089988.5, 456861.5 6089986.5, 456860.5 6089984.5, 456860.5 6089982.5, 456859.5 6089980.5, 456859.5 6089978.5, 456858.5 6089976.5, 456857.5 6089974.5, 456856.5 6089972.5, 456856.5 6089970.5, 456855.5 6089968.5, 456854.5 6089966.5, 456853.5 6089964.5, 456853.5 6089962.5, 456852.5 6089960.5, 456851.5 6089958.5)', 25832),
  35.4, 38.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089947.5, 6089922.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456900.5 6089947.5, 456898.5 6089946.5, 456898.5 6089944.5, 456899.5 6089942.5, 456897.5 6089940.5, 456895.5 6089939.5, 456897.5 6089938.5, 456899.5 6089936.5, 456900.5 6089934.5, 456927.5 6089932.5, 456900.5 6089931.5, 456905.5 6089930.5, 456900.5 6089929.5, 456930.5 6089928.5, 456930.5 6089926.5, 456902.5 6089925.5, 456930.5 6089924.5, 456894.5 6089923.5, 456894.5 6089922.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456848.5 6089947.5, 456847.5 6089945.5, 456846.5 6089943.5, 456846.5 6089941.5, 456845.5 6089939.5, 456844.5 6089937.5, 456843.5 6089935.5, 456843.5 6089933.5, 456842.5 6089931.5, 456842.5 6089929.5, 456841.5 6089927.5, 456840.5 6089925.5, 456839.5 6089923.5, 456839.5 6089922.5)', 25832),
  28.5, 32.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089922.5, 6089897.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456894.5 6089922.5, 456893.5 6089920.5, 456891.5 6089918.5, 456929.5 6089917.5, 456885.5 6089916.5, 456885.5 6089914.5, 456913.5 6089912.5, 456881.5 6089910.5, 456881.5 6089908.5, 456881.5 6089906.5, 456880.5 6089904.5, 456886.5 6089903.5, 456888.5 6089901.5, 456900.5 6089899.5, 456898.5 6089898.5, 456897.5 6089897.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456839.5 6089922.5, 456838.5 6089920.5, 456838.5 6089918.5, 456836.5 6089916.5, 456835.5 6089914.5, 456834.5 6089912.5, 456833.5 6089910.5, 456833.5 6089908.5, 456831.5 6089906.5, 456830.5 6089904.5, 456829.5 6089902.5, 456828.5 6089900.5, 456827.5 6089898.5, 456827.5 6089897.5)', 25832),
  28.9, 32.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089897.5, 6089876.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456897.5 6089897.5, 456895.5 6089895.5, 456878.5 6089893.5, 456878.5 6089891.5, 456885.5 6089890.5, 456877.5 6089889.5, 456883.5 6089887.5, 456880.5 6089886.5, 456880.5 6089884.5, 456881.5 6089882.5, 456881.5 6089880.5, 456879.5 6089878.5, 456879.5 6089876.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456827.5 6089897.5, 456826.5 6089895.5, 456825.5 6089893.5, 456824.5 6089891.5, 456843.5 6089890.5, 456843.5 6089888.5, 456842.5 6089886.5, 456822.5 6089885.5, 456821.5 6089883.5, 456839.5 6089881.5, 456819.5 6089879.5, 456838.5 6089877.5, 456838.5 6089876.5)', 25832),
  24.9, 28.3, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089843.5, 6089818.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456863.5 6089843.5, 456863.5 6089841.5, 456856.5 6089840.5, 456856.5 6089838.5, 456860.5 6089837.5, 456855.5 6089836.5, 456861.5 6089835.5, 456860.5 6089833.5, 456852.5 6089831.5, 456859.5 6089830.5, 456856.5 6089828.5, 456858.5 6089827.5, 456857.5 6089825.5, 456856.5 6089823.5, 456856.5 6089821.5, 456852.5 6089819.5, 456854.5 6089818.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456808.5 6089843.5, 456807.5 6089841.5, 456808.5 6089839.5, 456808.5 6089837.5, 456807.5 6089835.5, 456806.5 6089833.5, 456806.5 6089831.5, 456805.5 6089829.5, 456804.5 6089827.5, 456802.5 6089825.5, 456802.5 6089823.5, 456801.5 6089821.5, 456801.5 6089819.5, 456801.5 6089818.5)', 25832),
  22.2, 26, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089818.5, 6089798.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456854.5 6089818.5, 456845.5 6089816.5, 456847.5 6089815.5, 456867.5 6089813.5, 456866.5 6089811.5, 456866.5 6089809.5, 456868.5 6089807.5, 456868.5 6089805.5, 456869.5 6089803.5, 456869.5 6089801.5, 456871.5 6089799.5, 456871.5 6089798.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456801.5 6089818.5, 456799.5 6089816.5, 456799.5 6089814.5, 456798.5 6089812.5, 456798.5 6089810.5, 456797.5 6089808.5, 456796.5 6089806.5, 456796.5 6089804.5, 456795.5 6089802.5, 456795.5 6089800.5, 456794.5 6089798.5)', 25832),
  26.2, 30.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089789.5, 6089762.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456878.5 6089789.5, 456879.5 6089787.5, 456884.5 6089786.5, 456885.5 6089784.5, 456892.5 6089782.5, 456877.5 6089780.5, 456876.5 6089778.5, 456875.5 6089776.5, 456873.5 6089775.5, 456869.5 6089774.5, 456871.5 6089772.5, 456870.5 6089770.5, 456864.5 6089769.5, 456864.5 6089767.5, 456864.5 6089765.5, 456863.5 6089763.5, 456865.5 6089762.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456789.5 6089789.5, 456788.5 6089787.5, 456786.5 6089785.5, 456786.5 6089783.5, 456785.5 6089781.5, 456785.5 6089779.5, 456783.5 6089777.5, 456783.5 6089775.5, 456782.5 6089773.5, 456781.5 6089771.5, 456780.5 6089769.5, 456779.5 6089767.5, 456778.5 6089765.5, 456777.5 6089763.5, 456776.5 6089762.5)', 25832),
  32.8, 36.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089753.5, 6089728.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456835.5 6089753.5, 456839.5 6089751.5, 456860.5 6089750.5, 456834.5 6089748.5, 456860.5 6089747.5, 456834.5 6089746.5, 456860.5 6089744.5, 456862.5 6089742.5, 456832.5 6089741.5, 456836.5 6089740.5, 456835.5 6089738.5, 456816.5 6089737.5, 456824.5 6089736.5, 456817.5 6089735.5, 456819.5 6089733.5, 456819.5 6089731.5, 456816.5 6089729.5, 456816.5 6089728.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456772.5 6089753.5, 456771.5 6089751.5, 456770.5 6089749.5, 456770.5 6089747.5, 456770.5 6089745.5, 456769.5 6089743.5, 456769.5 6089741.5, 456769.5 6089739.5, 456769.5 6089737.5, 456769.5 6089735.5, 456769.5 6089733.5, 456768.5 6089731.5, 456768.5 6089729.5, 456768.5 6089728.5)', 25832),
  28.1, 31.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089728.5, 6089703.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456816.5 6089728.5, 456819.5 6089727.5, 456817.5 6089726.5, 456815.5 6089725.5, 456815.5 6089723.5, 456815.5 6089721.5, 456817.5 6089720.5, 456815.5 6089719.5, 456815.5 6089717.5, 456819.5 6089715.5, 456812.5 6089713.5, 456819.5 6089711.5, 456817.5 6089710.5, 456855.5 6089709.5, 456855.5 6089707.5, 456854.5 6089705.5, 456859.5 6089704.5, 456862.5 6089703.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456768.5 6089728.5, 456767.5 6089726.5, 456767.5 6089724.5, 456767.5 6089722.5, 456767.5 6089720.5, 456766.5 6089718.5, 456765.5 6089716.5, 456765.5 6089714.5, 456764.5 6089712.5, 456763.5 6089710.5, 456762.5 6089708.5, 456761.5 6089706.5, 456758.5 6089704.5, 456759.5 6089703.5)', 25832),
  29, 32.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089703.5, 6089678.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456862.5 6089703.5, 456856.5 6089702.5, 456854.5 6089701.5, 456858.5 6089700.5, 456810.5 6089699.5, 456810.5 6089697.5, 456809.5 6089695.5, 456805.5 6089693.5, 456802.5 6089691.5, 456801.5 6089689.5, 456802.5 6089687.5, 456802.5 6089685.5, 456802.5 6089683.5, 456799.5 6089682.5, 456801.5 6089681.5, 456804.5 6089679.5, 456798.5 6089678.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456759.5 6089703.5, 456757.5 6089702.5, 456757.5 6089700.5, 456755.5 6089698.5, 456755.5 6089696.5, 456754.5 6089694.5, 456767.5 6089693.5, 456753.5 6089692.5, 456752.5 6089690.5, 456752.5 6089688.5, 456752.5 6089686.5, 456751.5 6089684.5, 456751.5 6089682.5, 456752.5 6089680.5, 456750.5 6089679.5, 456750.5 6089678.5)', 25832),
  29.2, 33, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089678.5, 6089653.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456798.5 6089678.5, 456803.5 6089677.5, 456797.5 6089676.5, 456798.5 6089674.5, 456797.5 6089672.5, 456796.5 6089670.5, 456796.5 6089668.5, 456794.5 6089666.5, 456793.5 6089664.5, 456799.5 6089663.5, 456801.5 6089662.5, 456801.5 6089660.5, 456794.5 6089658.5, 456806.5 6089656.5, 456841.5 6089654.5, 456810.5 6089653.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456750.5 6089678.5, 456750.5 6089676.5, 456749.5 6089674.5, 456750.5 6089672.5, 456750.5 6089670.5, 456749.5 6089668.5, 456749.5 6089666.5, 456748.5 6089664.5, 456748.5 6089662.5, 456748.5 6089660.5, 456747.5 6089658.5, 456746.5 6089656.5, 456746.5 6089654.5, 456745.5 6089653.5)', 25832),
  29.8, 33.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089653.5, 6089628.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456810.5 6089653.5, 456812.5 6089652.5, 456814.5 6089650.5, 456789.5 6089649.5, 456790.5 6089647.5, 456816.5 6089646.5, 456810.5 6089644.5, 456815.5 6089643.5, 456816.5 6089641.5, 456812.5 6089640.5, 456814.5 6089638.5, 456848.5 6089637.5, 456785.5 6089635.5, 456793.5 6089633.5, 456810.5 6089631.5, 456809.5 6089629.5, 456809.5 6089628.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456745.5 6089653.5, 456744.5 6089651.5, 456741.5 6089649.5, 456741.5 6089647.5, 456741.5 6089645.5, 456740.5 6089643.5, 456739.5 6089641.5, 456738.5 6089639.5, 456738.5 6089637.5, 456736.5 6089635.5, 456736.5 6089633.5, 456735.5 6089631.5, 456735.5 6089629.5, 456735.5 6089628.5)', 25832),
  34.5, 38.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089628.5, 6089603.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456809.5 6089628.5, 456808.5 6089626.5, 456808.5 6089624.5, 456779.5 6089623.5, 456779.5 6089621.5, 456779.5 6089619.5, 456789.5 6089618.5, 456779.5 6089617.5, 456780.5 6089615.5, 456781.5 6089613.5, 456783.5 6089612.5, 456786.5 6089611.5, 456787.5 6089609.5, 456784.5 6089607.5, 456785.5 6089605.5, 456785.5 6089603.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456735.5 6089628.5, 456734.5 6089626.5, 456735.5 6089624.5, 456735.5 6089622.5, 456734.5 6089620.5, 456733.5 6089618.5, 456732.5 6089616.5, 456732.5 6089614.5, 456730.5 6089612.5, 456729.5 6089610.5, 456727.5 6089608.5, 456725.5 6089606.5, 456724.5 6089604.5, 456723.5 6089603.5)', 25832),
  26.3, 30, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089603.5, 6089573.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456785.5 6089603.5, 456780.5 6089601.5, 456780.5 6089599.5, 456778.5 6089597.5, 456771.5 6089596.5, 456769.5 6089594.5, 456769.5 6089592.5, 456764.5 6089590.5, 456764.5 6089588.5, 456761.5 6089586.5, 456759.5 6089584.5, 456756.5 6089582.5, 456757.5 6089580.5, 456757.5 6089578.5, 456757.5 6089576.5, 456757.5 6089574.5, 456756.5 6089573.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456723.5 6089603.5, 456722.5 6089601.5, 456721.5 6089599.5, 456720.5 6089597.5, 456718.5 6089595.5, 456718.5 6089593.5, 456716.5 6089591.5, 456715.5 6089589.5, 456714.5 6089587.5, 456713.5 6089585.5, 456711.5 6089583.5, 456710.5 6089581.5, 456709.5 6089579.5, 456708.5 6089577.5, 456707.5 6089575.5, 456706.5 6089573.5)', 25832),
  24.3, 28, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089502.5, 6089477.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456750.5 6089502.5, 456747.5 6089501.5, 456751.5 6089500.5, 456753.5 6089499.5, 456758.5 6089497.5, 456762.5 6089496.5, 456761.5 6089494.5, 456719.5 6089492.5, 456719.5 6089490.5, 456717.5 6089488.5, 456716.5 6089486.5, 456715.5 6089484.5, 456715.5 6089482.5, 456715.5 6089480.5, 456714.5 6089478.5, 456714.5 6089477.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456678.5 6089502.5, 456677.5 6089500.5, 456676.5 6089498.5, 456676.5 6089496.5, 456675.5 6089494.5, 456674.5 6089492.5, 456674.5 6089490.5, 456673.5 6089488.5, 456673.5 6089486.5, 456672.5 6089484.5, 456671.5 6089482.5, 456671.5 6089480.5, 456670.5 6089478.5, 456670.5 6089477.5)', 25832),
  27, 30.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089477.5, 6089452.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456714.5 6089477.5, 456714.5 6089475.5, 456765.5 6089473.5, 456763.5 6089472.5, 456762.5 6089470.5, 456757.5 6089469.5, 456757.5 6089467.5, 456711.5 6089466.5, 456709.5 6089465.5, 456707.5 6089464.5, 456706.5 6089462.5, 456704.5 6089461.5, 456704.5 6089459.5, 456703.5 6089457.5, 456704.5 6089455.5, 456704.5 6089453.5, 456703.5 6089452.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456670.5 6089477.5, 456669.5 6089475.5, 456668.5 6089473.5, 456668.5 6089471.5, 456667.5 6089469.5, 456666.5 6089467.5, 456665.5 6089465.5, 456664.5 6089463.5, 456664.5 6089461.5, 456663.5 6089459.5, 456662.5 6089457.5, 456662.5 6089455.5, 456660.5 6089453.5, 456660.5 6089452.5)', 25832),
  28.3, 31.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089452.5, 6089427.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456703.5 6089452.5, 456703.5 6089450.5, 456701.5 6089448.5, 456698.5 6089447.5, 456700.5 6089446.5, 456762.5 6089445.5, 456700.5 6089444.5, 456700.5 6089442.5, 456694.5 6089441.5, 456694.5 6089439.5, 456694.5 6089437.5, 456694.5 6089435.5, 456695.5 6089433.5, 456694.5 6089431.5, 456692.5 6089429.5, 456692.5 6089427.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456660.5 6089452.5, 456660.5 6089450.5, 456659.5 6089448.5, 456658.5 6089446.5, 456657.5 6089444.5, 456657.5 6089442.5, 456656.5 6089440.5, 456654.5 6089438.5, 456654.5 6089436.5, 456653.5 6089434.5, 456652.5 6089432.5, 456651.5 6089430.5, 456651.5 6089428.5, 456650.5 6089427.5)', 25832),
  31, 34.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089427.5, 6089402.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456692.5 6089427.5, 456693.5 6089425.5, 456686.5 6089423.5, 456686.5 6089421.5, 456692.5 6089420.5, 456690.5 6089419.5, 456689.5 6089417.5, 456687.5 6089416.5, 456687.5 6089414.5, 456685.5 6089413.5, 456683.5 6089411.5, 456683.5 6089409.5, 456683.5 6089407.5, 456683.5 6089405.5, 456684.5 6089403.5, 456683.5 6089402.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456650.5 6089427.5, 456650.5 6089425.5, 456649.5 6089423.5, 456648.5 6089421.5, 456647.5 6089419.5, 456647.5 6089417.5, 456646.5 6089415.5, 456645.5 6089413.5, 456644.5 6089411.5, 456643.5 6089409.5, 456642.5 6089407.5, 456641.5 6089405.5, 456640.5 6089403.5, 456640.5 6089402.5)', 25832),
  17.4, 21, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089402.5, 6089377.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456683.5 6089402.5, 456679.5 6089400.5, 456681.5 6089399.5, 456681.5 6089397.5, 456681.5 6089395.5, 456680.5 6089393.5, 456680.5 6089391.5, 456680.5 6089389.5, 456680.5 6089387.5, 456679.5 6089385.5, 456678.5 6089383.5, 456674.5 6089382.5, 456672.5 6089381.5, 456670.5 6089379.5, 456669.5 6089377.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456640.5 6089402.5, 456639.5 6089400.5, 456638.5 6089398.5, 456637.5 6089396.5, 456637.5 6089394.5, 456636.5 6089392.5, 456635.5 6089390.5, 456634.5 6089388.5, 456633.5 6089386.5, 456633.5 6089384.5, 456632.5 6089382.5, 456631.5 6089380.5, 456630.5 6089378.5, 456630.5 6089377.5)', 25832),
  16.8, 20.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089377.5, 6089352.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456669.5 6089377.5, 456668.5 6089375.5, 456667.5 6089373.5, 456666.5 6089371.5, 456668.5 6089370.5, 456669.5 6089368.5, 456664.5 6089367.5, 456663.5 6089365.5, 456663.5 6089363.5, 456662.5 6089361.5, 456662.5 6089359.5, 456664.5 6089357.5, 456669.5 6089356.5, 456661.5 6089354.5, 456672.5 6089353.5, 456666.5 6089352.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456630.5 6089377.5, 456629.5 6089375.5, 456628.5 6089373.5, 456627.5 6089371.5, 456626.5 6089369.5, 456626.5 6089367.5, 456624.5 6089365.5, 456624.5 6089363.5, 456622.5 6089361.5, 456621.5 6089359.5, 456620.5 6089357.5, 456620.5 6089355.5, 456620.5 6089353.5, 456619.5 6089352.5)', 25832),
  15, 18.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089352.5, 6089326.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456666.5 6089352.5, 456664.5 6089350.5, 456657.5 6089349.5, 456663.5 6089348.5, 456658.5 6089347.5, 456656.5 6089345.5, 456656.5 6089343.5, 456655.5 6089341.5, 456654.5 6089339.5, 456657.5 6089338.5, 456652.5 6089337.5, 456652.5 6089335.5, 456651.5 6089333.5, 456651.5 6089331.5, 456651.5 6089329.5, 456650.5 6089327.5, 456650.5 6089326.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456619.5 6089352.5, 456619.5 6089350.5, 456618.5 6089348.5, 456617.5 6089346.5, 456616.5 6089344.5, 456615.5 6089342.5, 456614.5 6089340.5, 456613.5 6089338.5, 456612.5 6089336.5, 456611.5 6089334.5, 456609.5 6089332.5, 456608.5 6089330.5, 456607.5 6089328.5, 456607.5 6089326.5)', 25832),
  15.5, 19.3, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089326.5, 6089290.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456650.5 6089326.5, 456650.5 6089324.5, 456648.5 6089322.5, 456649.5 6089320.5, 456647.5 6089318.5, 456647.5 6089316.5, 456646.5 6089314.5, 456645.5 6089312.5, 456644.5 6089310.5, 456648.5 6089309.5, 456646.5 6089308.5, 456645.5 6089306.5, 456643.5 6089305.5, 456644.5 6089303.5, 456644.5 6089301.5, 456643.5 6089299.5, 456641.5 6089297.5, 456639.5 6089295.5, 456638.5 6089293.5, 456693.5 6089291.5, 456673.5 6089290.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456607.5 6089326.5, 456606.5 6089324.5, 456605.5 6089322.5, 456604.5 6089320.5, 456603.5 6089318.5, 456602.5 6089316.5, 456601.5 6089314.5, 456601.5 6089312.5, 456600.5 6089310.5, 456598.5 6089308.5, 456598.5 6089306.5, 456596.5 6089304.5, 456596.5 6089302.5, 456595.5 6089300.5, 456594.5 6089298.5, 456593.5 6089296.5, 456592.5 6089294.5, 456591.5 6089292.5, 456591.5 6089290.5)', 25832),
  26.4, 30.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089274.5, 6089249.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456684.5 6089274.5, 456680.5 6089272.5, 456677.5 6089271.5, 456677.5 6089269.5, 456678.5 6089267.5, 456676.5 6089266.5, 456669.5 6089265.5, 456669.5 6089263.5, 456667.5 6089262.5, 456666.5 6089260.5, 456665.5 6089258.5, 456625.5 6089257.5, 456624.5 6089255.5, 456631.5 6089254.5, 456668.5 6089253.5, 456667.5 6089251.5, 456629.5 6089249.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456584.5 6089274.5, 456584.5 6089272.5, 456583.5 6089270.5, 456582.5 6089268.5, 456601.5 6089267.5, 456601.5 6089265.5, 456601.5 6089263.5, 456599.5 6089261.5, 456579.5 6089260.5, 456578.5 6089258.5, 456578.5 6089256.5, 456577.5 6089254.5, 456576.5 6089252.5, 456575.5 6089250.5, 456575.5 6089249.5)', 25832),
  33.6, 37.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089249.5, 6089224.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456629.5 6089249.5, 456627.5 6089247.5, 456620.5 6089246.5, 456626.5 6089245.5, 456619.5 6089243.5, 456625.5 6089242.5, 456625.5 6089240.5, 456617.5 6089238.5, 456616.5 6089236.5, 456623.5 6089234.5, 456623.5 6089232.5, 456621.5 6089230.5, 456619.5 6089228.5, 456654.5 6089226.5, 456650.5 6089225.5, 456612.5 6089224.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456575.5 6089249.5, 456574.5 6089247.5, 456573.5 6089245.5, 456573.5 6089243.5, 456571.5 6089241.5, 456570.5 6089239.5, 456570.5 6089237.5, 456569.5 6089235.5, 456568.5 6089233.5, 456567.5 6089231.5, 456567.5 6089229.5, 456565.5 6089227.5, 456565.5 6089225.5, 456564.5 6089224.5)', 25832),
  25.8, 29.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089224.5, 6089199.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456612.5 6089224.5, 456618.5 6089223.5, 456611.5 6089222.5, 456610.5 6089220.5, 456609.5 6089218.5, 456646.5 6089216.5, 456640.5 6089214.5, 456639.5 6089212.5, 456636.5 6089211.5, 456636.5 6089209.5, 456634.5 6089208.5, 456634.5 6089206.5, 456637.5 6089205.5, 456643.5 6089204.5, 456642.5 6089202.5, 456638.5 6089200.5, 456601.5 6089199.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456564.5 6089224.5, 456564.5 6089222.5, 456563.5 6089220.5, 456562.5 6089218.5, 456561.5 6089216.5, 456561.5 6089214.5, 456560.5 6089212.5, 456560.5 6089210.5, 456560.5 6089208.5, 456559.5 6089206.5, 456558.5 6089204.5, 456557.5 6089202.5, 456556.5 6089200.5, 456556.5 6089199.5)', 25832),
  27.8, 31.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089199.5, 6089174.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456601.5 6089199.5, 456603.5 6089198.5, 456601.5 6089197.5, 456600.5 6089195.5, 456600.5 6089193.5, 456599.5 6089191.5, 456599.5 6089189.5, 456598.5 6089187.5, 456598.5 6089185.5, 456597.5 6089183.5, 456597.5 6089181.5, 456596.5 6089179.5, 456592.5 6089177.5, 456594.5 6089176.5, 456591.5 6089175.5, 456593.5 6089174.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456556.5 6089199.5, 456554.5 6089197.5, 456554.5 6089195.5, 456552.5 6089193.5, 456551.5 6089191.5, 456550.5 6089189.5, 456549.5 6089187.5, 456548.5 6089185.5, 456547.5 6089183.5, 456546.5 6089181.5, 456546.5 6089179.5, 456545.5 6089177.5, 456544.5 6089175.5, 456543.5 6089174.5)', 25832),
  16.3, 20.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089174.5, 6089152.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456593.5 6089174.5, 456593.5 6089172.5, 456590.5 6089171.5, 456591.5 6089169.5, 456592.5 6089167.5, 456592.5 6089165.5, 456591.5 6089163.5, 456592.5 6089161.5, 456627.5 6089160.5, 456587.5 6089158.5, 456583.5 6089157.5, 456585.5 6089155.5, 456587.5 6089153.5, 456587.5 6089152.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456543.5 6089174.5, 456542.5 6089172.5, 456541.5 6089170.5, 456541.5 6089168.5, 456539.5 6089166.5, 456539.5 6089164.5, 456538.5 6089162.5, 456538.5 6089160.5, 456537.5 6089158.5, 456536.5 6089156.5, 456535.5 6089154.5, 456535.5 6089152.5)', 25832),
  24.7, 28.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089146.5, 6089121.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456582.5 6089146.5, 456583.5 6089144.5, 456581.5 6089142.5, 456580.5 6089140.5, 456579.5 6089138.5, 456580.5 6089136.5, 456578.5 6089134.5, 456577.5 6089132.5, 456576.5 6089130.5, 456576.5 6089128.5, 456575.5 6089126.5, 456574.5 6089124.5, 456574.5 6089122.5, 456574.5 6089121.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456532.5 6089146.5, 456532.5 6089144.5, 456531.5 6089142.5, 456530.5 6089140.5, 456530.5 6089138.5, 456528.5 6089136.5, 456528.5 6089134.5, 456528.5 6089132.5, 456527.5 6089130.5, 456526.5 6089128.5, 456525.5 6089126.5, 456524.5 6089124.5, 456523.5 6089122.5, 456523.5 6089121.5)', 25832),
  20.5, 24.3, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089121.5, 6089096.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456574.5 6089121.5, 456574.5 6089119.5, 456572.5 6089117.5, 456572.5 6089115.5, 456571.5 6089113.5, 456571.5 6089111.5, 456572.5 6089109.5, 456570.5 6089108.5, 456570.5 6089106.5, 456569.5 6089104.5, 456568.5 6089102.5, 456568.5 6089100.5, 456565.5 6089099.5, 456565.5 6089097.5, 456565.5 6089096.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456523.5 6089121.5, 456522.5 6089119.5, 456521.5 6089117.5, 456521.5 6089115.5, 456520.5 6089113.5, 456519.5 6089111.5, 456518.5 6089109.5, 456518.5 6089107.5, 456517.5 6089105.5, 456517.5 6089103.5, 456515.5 6089101.5, 456515.5 6089099.5, 456514.5 6089097.5, 456514.5 6089096.5)', 25832),
  20.8, 24.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089096.5, 6089071.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456565.5 6089096.5, 456566.5 6089094.5, 456565.5 6089092.5, 456565.5 6089090.5, 456564.5 6089088.5, 456562.5 6089086.5, 456559.5 6089085.5, 456558.5 6089083.5, 456557.5 6089081.5, 456556.5 6089079.5, 456559.5 6089078.5, 456559.5 6089076.5, 456552.5 6089075.5, 456552.5 6089073.5, 456553.5 6089071.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456514.5 6089096.5, 456513.5 6089094.5, 456512.5 6089092.5, 456511.5 6089090.5, 456510.5 6089088.5, 456509.5 6089086.5, 456508.5 6089084.5, 456507.5 6089082.5, 456506.5 6089080.5, 456505.5 6089078.5, 456505.5 6089076.5, 456504.5 6089074.5, 456503.5 6089072.5, 456502.5 6089071.5)', 25832),
  20.4, 24.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089071.5, 6089046.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456553.5 6089071.5, 456553.5 6089069.5, 456553.5 6089067.5, 456551.5 6089065.5, 456551.5 6089063.5, 456551.5 6089061.5, 456552.5 6089059.5, 456552.5 6089057.5, 456546.5 6089056.5, 456546.5 6089054.5, 456546.5 6089052.5, 456545.5 6089050.5, 456565.5 6089048.5, 456571.5 6089047.5, 456576.5 6089046.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456502.5 6089071.5, 456502.5 6089069.5, 456501.5 6089067.5, 456501.5 6089065.5, 456500.5 6089063.5, 456499.5 6089061.5, 456499.5 6089059.5, 456498.5 6089057.5, 456497.5 6089055.5, 456496.5 6089053.5, 456496.5 6089051.5, 456495.5 6089049.5, 456494.5 6089047.5, 456494.5 6089046.5)', 25832),
  21.9, 25.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6089046.5, 6089015.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456576.5 6089046.5, 456600.5 6089045.5, 456572.5 6089044.5, 456572.5 6089042.5, 456571.5 6089040.5, 456570.5 6089038.5, 456602.5 6089037.5, 456571.5 6089036.5, 456568.5 6089035.5, 456570.5 6089034.5, 456568.5 6089032.5, 456567.5 6089030.5, 456562.5 6089029.5, 456561.5 6089027.5, 456563.5 6089026.5, 456562.5 6089024.5, 456561.5 6089022.5, 456556.5 6089021.5, 456553.5 6089020.5, 456552.5 6089018.5, 456555.5 6089016.5, 456555.5 6089015.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456494.5 6089046.5, 456493.5 6089044.5, 456493.5 6089042.5, 456493.5 6089040.5, 456491.5 6089039.5, 456491.5 6089037.5, 456490.5 6089035.5, 456489.5 6089033.5, 456488.5 6089031.5, 456487.5 6089029.5, 456486.5 6089027.5, 456485.5 6089025.5, 456484.5 6089023.5, 456484.5 6089021.5, 456483.5 6089019.5, 456483.5 6089017.5, 456482.5 6089015.5)', 25832),
  26.4, 30.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088953.5, 6088928.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456523.5 6088953.5, 456522.5 6088951.5, 456523.5 6088949.5, 456504.5 6088947.5, 456500.5 6088945.5, 456498.5 6088944.5, 456496.5 6088942.5, 456497.5 6088940.5, 456497.5 6088938.5, 456499.5 6088937.5, 456500.5 6088935.5, 456499.5 6088933.5, 456502.5 6088932.5, 456501.5 6088930.5, 456495.5 6088928.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456480.5 6088953.5, 456479.5 6088951.5, 456479.5 6088949.5, 456479.5 6088947.5, 456477.5 6088945.5, 456479.5 6088943.5, 456478.5 6088941.5, 456476.5 6088940.5, 456474.5 6088939.5, 456473.5 6088937.5, 456472.5 6088935.5, 456471.5 6088933.5, 456469.5 6088931.5, 456468.5 6088929.5, 456468.5 6088928.5)', 25832),
  16, 22, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088928.5, 6088903.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456495.5 6088928.5, 456493.5 6088926.5, 456493.5 6088924.5, 456498.5 6088922.5, 456492.5 6088921.5, 456498.5 6088920.5, 456491.5 6088919.5, 456491.5 6088917.5, 456489.5 6088915.5, 456488.5 6088913.5, 456488.5 6088911.5, 456487.5 6088909.5, 456486.5 6088907.5, 456492.5 6088905.5, 456491.5 6088903.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456468.5 6088928.5, 456467.5 6088926.5, 456467.5 6088924.5, 456465.5 6088922.5, 456465.5 6088920.5, 456464.5 6088918.5, 456464.5 6088916.5, 456464.5 6088914.5, 456465.5 6088912.5, 456466.5 6088910.5, 456467.5 6088908.5, 456467.5 6088906.5, 456467.5 6088904.5, 456466.5 6088903.5)', 25832),
  13.1, 18.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088903.5, 6088878.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456491.5 6088903.5, 456491.5 6088901.5, 456484.5 6088900.5, 456483.5 6088898.5, 456499.5 6088896.5, 456482.5 6088895.5, 456481.5 6088893.5, 456480.5 6088891.5, 456480.5 6088889.5, 456518.5 6088888.5, 456517.5 6088886.5, 456530.5 6088884.5, 456532.5 6088883.5, 456476.5 6088882.5, 456475.5 6088880.5, 456474.5 6088878.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456466.5 6088903.5, 456459.5 6088902.5, 456458.5 6088900.5, 456457.5 6088898.5, 456459.5 6088896.5, 456458.5 6088894.5, 456456.5 6088893.5, 456456.5 6088891.5, 456455.5 6088889.5, 456454.5 6088887.5, 456453.5 6088885.5, 456452.5 6088883.5, 456450.5 6088881.5, 456448.5 6088879.5, 456448.5 6088878.5)', 25832),
  24.7, 31.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088860.5, 6088834.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456469.5 6088860.5, 456468.5 6088858.5, 456465.5 6088856.5, 456463.5 6088854.5, 456465.5 6088853.5, 456464.5 6088851.5, 456486.5 6088849.5, 456462.5 6088847.5, 456462.5 6088845.5, 456460.5 6088844.5, 456459.5 6088842.5, 456458.5 6088840.5, 456457.5 6088838.5, 456456.5 6088836.5, 456455.5 6088834.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456416.5 6088860.5, 456416.5 6088858.5, 456415.5 6088856.5, 456413.5 6088855.5, 456413.5 6088853.5, 456412.5 6088851.5, 456411.5 6088849.5, 456410.5 6088847.5, 456410.5 6088845.5, 456409.5 6088843.5, 456410.5 6088841.5, 456409.5 6088838.5, 456408.5 6088836.5, 456408.5 6088834.5)', 25832),
  19, 22.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088834.5, 6088809.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456455.5 6088834.5, 456453.5 6088833.5, 456454.5 6088831.5, 456453.5 6088829.5, 456453.5 6088827.5, 456453.5 6088825.5, 456451.5 6088824.5, 456453.5 6088823.5, 456449.5 6088822.5, 456452.5 6088821.5, 456448.5 6088819.5, 456450.5 6088818.5, 456450.5 6088816.5, 456448.5 6088815.5, 456448.5 6088813.5, 456447.5 6088811.5, 456455.5 6088809.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456408.5 6088834.5, 456407.5 6088832.5, 456407.5 6088830.5, 456406.5 6088828.5, 456405.5 6088826.5, 456405.5 6088824.5, 456404.5 6088822.5, 456403.5 6088820.5, 456402.5 6088818.5, 456402.5 6088816.5, 456401.5 6088814.5, 456400.5 6088812.5, 456399.5 6088810.5, 456398.5 6088809.5)', 25832),
  16.3, 19.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088809.5, 6088784.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456455.5 6088809.5, 456447.5 6088808.5, 456455.5 6088807.5, 456444.5 6088805.5, 456443.5 6088803.5, 456442.5 6088801.5, 456441.5 6088799.5, 456476.5 6088797.5, 456479.5 6088796.5, 456441.5 6088795.5, 456481.5 6088794.5, 456440.5 6088793.5, 456438.5 6088791.5, 456468.5 6088790.5, 456438.5 6088789.5, 456437.5 6088787.5, 456435.5 6088785.5, 456435.5 6088784.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456398.5 6088809.5, 456397.5 6088807.5, 456396.5 6088805.5, 456396.5 6088803.5, 456396.5 6088801.5, 456395.5 6088799.5, 456394.5 6088797.5, 456393.5 6088795.5, 456393.5 6088793.5, 456393.5 6088791.5, 456391.5 6088789.5, 456391.5 6088787.5, 456389.5 6088786.5, 456388.5 6088784.5)', 25832),
  23, 26.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088784.5, 6088759.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456435.5 6088784.5, 456458.5 6088783.5, 456436.5 6088781.5, 456453.5 6088779.5, 456453.5 6088777.5, 456453.5 6088775.5, 456431.5 6088773.5, 456430.5 6088771.5, 456464.5 6088769.5, 456462.5 6088768.5, 456457.5 6088767.5, 456459.5 6088766.5, 456455.5 6088764.5, 456427.5 6088762.5, 456427.5 6088760.5, 456428.5 6088759.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456388.5 6088784.5, 456387.5 6088782.5, 456387.5 6088780.5, 456387.5 6088778.5, 456386.5 6088776.5, 456385.5 6088774.5, 456385.5 6088772.5, 456385.5 6088770.5, 456384.5 6088768.5, 456383.5 6088766.5, 456382.5 6088764.5, 456382.5 6088762.5, 456382.5 6088760.5, 456383.5 6088759.5)', 25832),
  22.3, 25.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088759.5, 6088734.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456428.5 6088759.5, 456425.5 6088757.5, 456442.5 6088755.5, 456438.5 6088753.5, 456424.5 6088752.5, 456440.5 6088751.5, 456440.5 6088749.5, 456421.5 6088747.5, 456421.5 6088745.5, 456421.5 6088743.5, 456445.5 6088741.5, 456445.5 6088739.5, 456445.5 6088737.5, 456444.5 6088735.5, 456418.5 6088734.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456383.5 6088759.5, 456380.5 6088757.5, 456382.5 6088755.5, 456387.5 6088754.5, 456388.5 6088752.5, 456387.5 6088750.5, 456387.5 6088748.5, 456386.5 6088746.5, 456385.5 6088744.5, 456385.5 6088742.5, 456384.5 6088740.5, 456383.5 6088738.5, 456382.5 6088736.5, 456381.5 6088734.5)', 25832),
  21.2, 26.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088734.5, 6088709.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456418.5 6088734.5, 456446.5 6088733.5, 456417.5 6088732.5, 456414.5 6088730.5, 456414.5 6088728.5, 456414.5 6088726.5, 456414.5 6088724.5, 456390.5 6088723.5, 456389.5 6088721.5, 456389.5 6088719.5, 456386.5 6088717.5, 456384.5 6088716.5, 456384.5 6088714.5, 456386.5 6088712.5, 456388.5 6088710.5, 456387.5 6088709.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456381.5 6088734.5, 456380.5 6088732.5, 456380.5 6088730.5, 456378.5 6088728.5, 456377.5 6088726.5, 456377.5 6088724.5, 456376.5 6088722.5, 456375.5 6088720.5, 456374.5 6088718.5, 456373.5 6088716.5, 456373.5 6088714.5, 456374.5 6088712.5, 456374.5 6088710.5, 456374.5 6088709.5)', 25832),
  21, 25.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088709.5, 6088688.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456387.5 6088709.5, 456408.5 6088708.5, 456407.5 6088706.5, 456407.5 6088704.5, 456406.5 6088702.5, 456406.5 6088700.5, 456405.5 6088698.5, 456404.5 6088696.5, 456403.5 6088694.5, 456402.5 6088692.5, 456401.5 6088690.5, 456400.5 6088688.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456374.5 6088709.5, 456374.5 6088707.5, 456375.5 6088705.5, 456375.5 6088703.5, 456376.5 6088701.5, 456375.5 6088699.5, 456374.5 6088697.5, 456349.5 6088696.5, 456374.5 6088694.5, 456348.5 6088693.5, 456347.5 6088691.5, 456374.5 6088690.5, 456346.5 6088689.5, 456345.5 6088688.5)', 25832),
  17.3, 20.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088639.5, 6088614.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456410.5 6088639.5, 456411.5 6088637.5, 456383.5 6088636.5, 456401.5 6088634.5, 456401.5 6088632.5, 456419.5 6088631.5, 456381.5 6088629.5, 456380.5 6088627.5, 456419.5 6088625.5, 456417.5 6088624.5, 456415.5 6088623.5, 456417.5 6088622.5, 456415.5 6088621.5, 456413.5 6088619.5, 456410.5 6088618.5, 456412.5 6088617.5, 456414.5 6088616.5, 456414.5 6088614.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456328.5 6088639.5, 456328.5 6088637.5, 456327.5 6088635.5, 456326.5 6088633.5, 456325.5 6088631.5, 456350.5 6088629.5, 456324.5 6088628.5, 456349.5 6088627.5, 456323.5 6088625.5, 456323.5 6088623.5, 456322.5 6088621.5, 456322.5 6088619.5, 456321.5 6088617.5, 456321.5 6088615.5, 456320.5 6088614.5)', 25832),
  25.9, 29.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088614.5, 6088589.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456414.5 6088614.5, 456412.5 6088612.5, 456409.5 6088611.5, 456377.5 6088610.5, 456410.5 6088609.5, 456377.5 6088608.5, 456407.5 6088607.5, 456405.5 6088606.5, 456404.5 6088604.5, 456375.5 6088603.5, 456375.5 6088601.5, 456358.5 6088600.5, 456356.5 6088599.5, 456356.5 6088597.5, 456356.5 6088595.5, 456354.5 6088593.5, 456354.5 6088591.5, 456352.5 6088589.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456320.5 6088614.5, 456320.5 6088612.5, 456319.5 6088610.5, 456318.5 6088608.5, 456317.5 6088606.5, 456317.5 6088604.5, 456316.5 6088602.5, 456315.5 6088600.5, 456314.5 6088598.5, 456313.5 6088596.5, 456313.5 6088594.5, 456312.5 6088592.5, 456312.5 6088590.5, 456312.5 6088589.5)', 25832),
  25.9, 29.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088589.5, 6088564.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456352.5 6088589.5, 456351.5 6088587.5, 456351.5 6088585.5, 456350.5 6088583.5, 456349.5 6088581.5, 456349.5 6088579.5, 456348.5 6088577.5, 456348.5 6088575.5, 456348.5 6088573.5, 456348.5 6088571.5, 456350.5 6088569.5, 456353.5 6088567.5, 456406.5 6088565.5, 456400.5 6088564.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456312.5 6088589.5, 456311.5 6088587.5, 456310.5 6088585.5, 456310.5 6088583.5, 456309.5 6088581.5, 456308.5 6088579.5, 456307.5 6088577.5, 456306.5 6088575.5, 456305.5 6088573.5, 456304.5 6088571.5, 456303.5 6088569.5, 456302.5 6088567.5, 456302.5 6088565.5, 456301.5 6088564.5)', 25832),
  25.5, 28.9, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088564.5, 6088539.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456400.5 6088564.5, 456397.5 6088563.5, 456353.5 6088562.5, 456352.5 6088560.5, 456352.5 6088558.5, 456349.5 6088557.5, 456349.5 6088555.5, 456347.5 6088553.5, 456348.5 6088551.5, 456346.5 6088550.5, 456346.5 6088548.5, 456343.5 6088546.5, 456343.5 6088544.5, 456341.5 6088542.5, 456344.5 6088541.5, 456343.5 6088539.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456301.5 6088564.5, 456300.5 6088562.5, 456299.5 6088560.5, 456298.5 6088558.5, 456298.5 6088556.5, 456297.5 6088554.5, 456297.5 6088552.5, 456296.5 6088550.5, 456296.5 6088548.5, 456295.5 6088546.5, 456294.5 6088544.5, 456293.5 6088542.5, 456292.5 6088540.5, 456291.5 6088539.5)', 25832),
  24.9, 28.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088539.5, 6088514.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456343.5 6088539.5, 456342.5 6088537.5, 456342.5 6088535.5, 456354.5 6088533.5, 456353.5 6088531.5, 456354.5 6088529.5, 456353.5 6088527.5, 456354.5 6088525.5, 456334.5 6088523.5, 456356.5 6088521.5, 456358.5 6088519.5, 456358.5 6088517.5, 456360.5 6088516.5, 456362.5 6088515.5, 456352.5 6088514.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456291.5 6088539.5, 456291.5 6088537.5, 456290.5 6088535.5, 456289.5 6088533.5, 456288.5 6088531.5, 456288.5 6088529.5, 456287.5 6088527.5, 456286.5 6088525.5, 456286.5 6088523.5, 456286.5 6088521.5, 456285.5 6088519.5, 456284.5 6088517.5, 456284.5 6088515.5, 456283.5 6088514.5)', 25832),
  26, 29.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088514.5, 6088489.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456352.5 6088514.5, 456355.5 6088513.5, 456352.5 6088511.5, 456350.5 6088510.5, 456351.5 6088508.5, 456350.5 6088506.5, 456350.5 6088504.5, 456348.5 6088502.5, 456343.5 6088500.5, 456348.5 6088498.5, 456351.5 6088496.5, 456330.5 6088495.5, 456346.5 6088493.5, 456368.5 6088492.5, 456370.5 6088490.5, 456371.5 6088489.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456283.5 6088514.5, 456283.5 6088512.5, 456282.5 6088510.5, 456281.5 6088508.5, 456280.5 6088506.5, 456279.5 6088504.5, 456278.5 6088502.5, 456276.5 6088500.5, 456275.5 6088498.5, 456274.5 6088496.5, 456273.5 6088494.5, 456272.5 6088492.5, 456287.5 6088491.5, 456271.5 6088489.5)', 25832),
  26.8, 30.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088489.5, 6088464.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456371.5 6088489.5, 456364.5 6088488.5, 456369.5 6088487.5, 456364.5 6088485.5, 456346.5 6088483.5, 456322.5 6088482.5, 456319.5 6088481.5, 456320.5 6088479.5, 456317.5 6088478.5, 456316.5 6088476.5, 456316.5 6088474.5, 456317.5 6088472.5, 456314.5 6088471.5, 456314.5 6088469.5, 456316.5 6088468.5, 456316.5 6088466.5, 456317.5 6088464.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456271.5 6088489.5, 456286.5 6088488.5, 456271.5 6088487.5, 456271.5 6088485.5, 456271.5 6088483.5, 456271.5 6088481.5, 456270.5 6088479.5, 456270.5 6088477.5, 456269.5 6088475.5, 456269.5 6088473.5, 456268.5 6088471.5, 456268.5 6088469.5, 456267.5 6088467.5, 456265.5 6088465.5, 456265.5 6088464.5)', 25832),
  26.8, 30.3, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088464.5, 6088439.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456317.5 6088464.5, 456322.5 6088463.5, 456336.5 6088462.5, 456324.5 6088461.5, 456336.5 6088459.5, 456335.5 6088457.5, 456333.5 6088456.5, 456332.5 6088454.5, 456330.5 6088452.5, 456324.5 6088450.5, 456318.5 6088448.5, 456318.5 6088446.5, 456316.5 6088445.5, 456316.5 6088443.5, 456317.5 6088441.5, 456318.5 6088439.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456265.5 6088464.5, 456264.5 6088462.5, 456263.5 6088460.5, 456261.5 6088458.5, 456259.5 6088456.5, 456258.5 6088454.5, 456271.5 6088453.5, 456272.5 6088451.5, 456271.5 6088449.5, 456270.5 6088447.5, 456270.5 6088445.5, 456269.5 6088443.5, 456268.5 6088441.5, 456268.5 6088439.5)', 25832),
  25.1, 28.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088439.5, 6088414.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456318.5 6088439.5, 456315.5 6088438.5, 456317.5 6088436.5, 456317.5 6088434.5, 456316.5 6088432.5, 456318.5 6088431.5, 456318.5 6088429.5, 456319.5 6088427.5, 456318.5 6088425.5, 456315.5 6088423.5, 456311.5 6088422.5, 456311.5 6088420.5, 456311.5 6088418.5, 456310.5 6088416.5, 456315.5 6088415.5, 456315.5 6088414.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456268.5 6088439.5, 456267.5 6088437.5, 456267.5 6088435.5, 456266.5 6088433.5, 456266.5 6088431.5, 456265.5 6088429.5, 456264.5 6088427.5, 456263.5 6088425.5, 456262.5 6088423.5, 456261.5 6088421.5, 456260.5 6088419.5, 456259.5 6088417.5, 456258.5 6088415.5, 456258.5 6088414.5)', 25832),
  22.9, 28.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088414.5, 6088389.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456315.5 6088414.5, 456316.5 6088412.5, 456311.5 6088411.5, 456312.5 6088409.5, 456310.5 6088408.5, 456306.5 6088406.5, 456304.5 6088404.5, 456303.5 6088402.5, 456301.5 6088401.5, 456299.5 6088399.5, 456297.5 6088397.5, 456296.5 6088395.5, 456294.5 6088394.5, 456289.5 6088393.5, 456287.5 6088392.5, 456289.5 6088391.5, 456293.5 6088390.5, 456292.5 6088389.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456258.5 6088414.5, 456257.5 6088412.5, 456257.5 6088410.5, 456257.5 6088408.5, 456256.5 6088406.5, 456256.5 6088404.5, 456255.5 6088402.5, 456254.5 6088400.5, 456253.5 6088398.5, 456252.5 6088396.5, 456252.5 6088394.5, 456251.5 6088392.5, 456250.5 6088390.5, 456250.5 6088389.5)', 25832),
  22.9, 28.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088389.5, 6088364.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456292.5 6088389.5, 456290.5 6088387.5, 456279.5 6088386.5, 456288.5 6088385.5, 456286.5 6088383.5, 456287.5 6088381.5, 456285.5 6088379.5, 456287.5 6088377.5, 456288.5 6088375.5, 456287.5 6088373.5, 456288.5 6088371.5, 456289.5 6088369.5, 456289.5 6088367.5, 456291.5 6088365.5, 456291.5 6088364.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456250.5 6088389.5, 456250.5 6088387.5, 456249.5 6088385.5, 456248.5 6088383.5, 456248.5 6088381.5, 456247.5 6088379.5, 456246.5 6088377.5, 456246.5 6088375.5, 456245.5 6088373.5, 456244.5 6088371.5, 456243.5 6088369.5, 456243.5 6088367.5, 456242.5 6088365.5, 456241.5 6088364.5)', 25832),
  21.3, 26.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088364.5, 6088339.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456291.5 6088364.5, 456294.5 6088362.5, 456291.5 6088361.5, 456291.5 6088359.5, 456291.5 6088357.5, 456290.5 6088355.5, 456291.5 6088353.5, 456293.5 6088351.5, 456293.5 6088349.5, 456292.5 6088347.5, 456293.5 6088345.5, 456292.5 6088343.5, 456294.5 6088342.5, 456293.5 6088340.5, 456294.5 6088339.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456241.5 6088364.5, 456242.5 6088362.5, 456241.5 6088360.5, 456241.5 6088358.5, 456240.5 6088356.5, 456240.5 6088354.5, 456240.5 6088352.5, 456239.5 6088350.5, 456238.5 6088348.5, 456238.5 6088346.5, 456238.5 6088344.5, 456237.5 6088342.5, 456237.5 6088340.5, 456236.5 6088339.5)', 25832),
  23.2, 28.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088339.5, 6088314.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456294.5 6088339.5, 456293.5 6088337.5, 456292.5 6088335.5, 456291.5 6088333.5, 456294.5 6088332.5, 456291.5 6088331.5, 456289.5 6088330.5, 456288.5 6088328.5, 456287.5 6088326.5, 456285.5 6088324.5, 456286.5 6088322.5, 456285.5 6088320.5, 456285.5 6088318.5, 456286.5 6088316.5, 456288.5 6088315.5, 456289.5 6088314.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456236.5 6088339.5, 456235.5 6088337.5, 456235.5 6088335.5, 456234.5 6088333.5, 456233.5 6088331.5, 456233.5 6088329.5, 456231.5 6088327.5, 456230.5 6088325.5, 456229.5 6088323.5, 456224.5 6088321.5, 456223.5 6088319.5, 456221.5 6088317.5, 456221.5 6088315.5, 456219.5 6088314.5)', 25832),
  23.8, 29.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088314.5, 6088302.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456289.5 6088314.5, 456290.5 6088312.5, 456292.5 6088310.5, 456290.5 6088308.5, 456292.5 6088307.5, 456258.5 6088306.5, 456256.5 6088305.5, 456256.5 6088303.5, 456260.5 6088302.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456219.5 6088314.5, 456219.5 6088312.5, 456218.5 6088310.5, 456217.5 6088308.5, 456217.5 6088306.5, 456217.5 6088304.5, 456218.5 6088302.5)', 25832),
  23.1, 28.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088297.5, 6088272.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456254.5 6088297.5, 456256.5 6088296.5, 456256.5 6088294.5, 456259.5 6088292.5, 456258.5 6088290.5, 456254.5 6088289.5, 456253.5 6088287.5, 456251.5 6088286.5, 456253.5 6088285.5, 456253.5 6088283.5, 456251.5 6088282.5, 456260.5 6088280.5, 456258.5 6088279.5, 456255.5 6088278.5, 456257.5 6088277.5, 456260.5 6088275.5, 456259.5 6088273.5, 456250.5 6088272.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456216.5 6088297.5, 456216.5 6088295.5, 456216.5 6088293.5, 456216.5 6088291.5, 456215.5 6088289.5, 456214.5 6088287.5, 456212.5 6088285.5, 456211.5 6088283.5, 456211.5 6088281.5, 456210.5 6088279.5, 456208.5 6088278.5, 456207.5 6088276.5, 456206.5 6088274.5, 456205.5 6088272.5)', 25832),
  16.9, 22.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088272.5, 6088247.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456250.5 6088272.5, 456249.5 6088270.5, 456251.5 6088268.5, 456256.5 6088266.5, 456255.5 6088264.5, 456249.5 6088263.5, 456249.5 6088261.5, 456217.5 6088260.5, 456213.5 6088258.5, 456250.5 6088256.5, 456218.5 6088255.5, 456249.5 6088254.5, 456219.5 6088253.5, 456251.5 6088252.5, 456218.5 6088251.5, 456217.5 6088249.5, 456252.5 6088248.5, 456253.5 6088247.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456205.5 6088272.5, 456204.5 6088270.5, 456203.5 6088268.5, 456202.5 6088266.5, 456201.5 6088264.5, 456200.5 6088262.5, 456199.5 6088260.5, 456198.5 6088258.5, 456197.5 6088256.5, 456196.5 6088254.5, 456195.5 6088252.5, 456194.5 6088250.5, 456191.5 6088248.5, 456190.5 6088247.5)', 25832),
  17.7, 23.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088247.5, 6088222.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456253.5 6088247.5, 456215.5 6088246.5, 456252.5 6088245.5, 456252.5 6088243.5, 456252.5 6088241.5, 456247.5 6088239.5, 456214.5 6088238.5, 456248.5 6088237.5, 456250.5 6088236.5, 456219.5 6088235.5, 456221.5 6088233.5, 456206.5 6088231.5, 456204.5 6088230.5, 456220.5 6088228.5, 456217.5 6088227.5, 456204.5 6088226.5, 456250.5 6088224.5, 456249.5 6088222.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456190.5 6088247.5, 456189.5 6088245.5, 456188.5 6088243.5, 456187.5 6088241.5, 456186.5 6088239.5, 456185.5 6088237.5, 456185.5 6088235.5, 456185.5 6088233.5, 456184.5 6088231.5, 456183.5 6088229.5, 456183.5 6088227.5, 456182.5 6088225.5, 456181.5 6088223.5, 456181.5 6088222.5)', 25832),
  19.8, 25.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088222.5, 6088197.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456249.5 6088222.5, 456249.5 6088220.5, 456247.5 6088218.5, 456202.5 6088217.5, 456247.5 6088216.5, 456241.5 6088214.5, 456246.5 6088213.5, 456243.5 6088211.5, 456213.5 6088210.5, 456212.5 6088208.5, 456243.5 6088206.5, 456246.5 6088205.5, 456213.5 6088204.5, 456211.5 6088203.5, 456210.5 6088201.5, 456212.5 6088199.5, 456217.5 6088198.5, 456218.5 6088197.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456181.5 6088222.5, 456180.5 6088220.5, 456179.5 6088218.5, 456179.5 6088216.5, 456178.5 6088214.5, 456178.5 6088212.5, 456177.5 6088210.5, 456177.5 6088208.5, 456176.5 6088206.5, 456176.5 6088204.5, 456175.5 6088202.5, 456175.5 6088200.5, 456175.5 6088198.5, 456174.5 6088197.5)', 25832),
  20, 25.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088197.5, 6088172.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456218.5 6088197.5, 456235.5 6088195.5, 456233.5 6088194.5, 456230.5 6088192.5, 456222.5 6088191.5, 456216.5 6088190.5, 456220.5 6088189.5, 456223.5 6088187.5, 456221.5 6088186.5, 456223.5 6088185.5, 456225.5 6088184.5, 456225.5 6088182.5, 456228.5 6088180.5, 456228.5 6088178.5, 456227.5 6088176.5, 456227.5 6088174.5, 456227.5 6088172.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456174.5 6088197.5, 456174.5 6088195.5, 456173.5 6088193.5, 456173.5 6088191.5, 456172.5 6088189.5, 456171.5 6088187.5, 456170.5 6088185.5, 456170.5 6088183.5, 456169.5 6088181.5, 456169.5 6088179.5, 456168.5 6088177.5, 456167.5 6088175.5, 456167.5 6088173.5, 456166.5 6088172.5)', 25832),
  20.5, 26.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088172.5, 6088147.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456227.5 6088172.5, 456226.5 6088170.5, 456227.5 6088168.5, 456227.5 6088166.5, 456227.5 6088164.5, 456227.5 6088162.5, 456226.5 6088160.5, 456223.5 6088158.5, 456222.5 6088156.5, 456217.5 6088155.5, 456213.5 6088154.5, 456210.5 6088152.5, 456208.5 6088150.5, 456207.5 6088148.5, 456207.5 6088147.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456166.5 6088172.5, 456168.5 6088170.5, 456165.5 6088168.5, 456164.5 6088166.5, 456163.5 6088164.5, 456163.5 6088162.5, 456162.5 6088160.5, 456161.5 6088158.5, 456161.5 6088156.5, 456160.5 6088154.5, 456160.5 6088152.5, 456159.5 6088150.5, 456158.5 6088148.5, 456157.5 6088147.5)', 25832),
  21.3, 27, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088147.5, 6088121.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456207.5 6088147.5, 456207.5 6088145.5, 456207.5 6088143.5, 456207.5 6088141.5, 456207.5 6088139.5, 456206.5 6088137.5, 456206.5 6088134.5, 456205.5 6088132.5, 456204.5 6088130.5, 456203.5 6088128.5, 456201.5 6088126.5, 456201.5 6088124.5, 456200.5 6088122.5, 456202.5 6088121.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456157.5 6088147.5, 456156.5 6088145.5, 456155.5 6088143.5, 456154.5 6088141.5, 456153.5 6088139.5, 456152.5 6088137.5, 456151.5 6088134.5, 456150.5 6088132.5, 456149.5 6088130.5, 456149.5 6088128.5, 456148.5 6088126.5, 456148.5 6088124.5, 456147.5 6088122.5, 456147.5 6088121.5)', 25832),
  19.3, 24.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088121.5, 6088096.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456202.5 6088121.5, 456200.5 6088120.5, 456198.5 6088118.5, 456197.5 6088116.5, 456198.5 6088114.5, 456197.5 6088112.5, 456194.5 6088110.5, 456193.5 6088108.5, 456192.5 6088106.5, 456189.5 6088105.5, 456188.5 6088103.5, 456186.5 6088102.5, 456185.5 6088100.5, 456184.5 6088098.5, 456183.5 6088096.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456147.5 6088121.5, 456147.5 6088119.5, 456146.5 6088117.5, 456146.5 6088115.5, 456146.5 6088113.5, 456145.5 6088111.5, 456145.5 6088109.5, 456144.5 6088107.5, 456143.5 6088105.5, 456143.5 6088103.5, 456142.5 6088101.5, 456141.5 6088099.5, 456140.5 6088097.5, 456140.5 6088096.5)', 25832),
  18.9, 24.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088096.5, 6088060.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456183.5 6088096.5, 456183.5 6088094.5, 456185.5 6088093.5, 456182.5 6088092.5, 456184.5 6088091.5, 456184.5 6088089.5, 456184.5 6088087.5, 456182.5 6088086.5, 456181.5 6088084.5, 456183.5 6088083.5, 456183.5 6088081.5, 456180.5 6088080.5, 456178.5 6088078.5, 456172.5 6088077.5, 456172.5 6088075.5, 456174.5 6088074.5, 456172.5 6088072.5, 456169.5 6088070.5, 456168.5 6088068.5, 456170.5 6088066.5, 456167.5 6088065.5, 456166.5 6088063.5, 456167.5 6088061.5, 456167.5 6088060.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456140.5 6088096.5, 456139.5 6088094.5, 456138.5 6088092.5, 456137.5 6088090.5, 456137.5 6088088.5, 456136.5 6088086.5, 456135.5 6088084.5, 456135.5 6088082.5, 456134.5 6088080.5, 456133.5 6088078.5, 456133.5 6088076.5, 456133.5 6088074.5, 456132.5 6088072.5, 456131.5 6088070.5, 456131.5 6088068.5, 456130.5 6088066.5, 456130.5 6088064.5, 456129.5 6088062.5, 456128.5 6088060.5)', 25832),
  17.3, 23, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088033.5, 6088008.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456157.5 6088033.5, 456156.5 6088031.5, 456155.5 6088029.5, 456154.5 6088027.5, 456153.5 6088025.5, 456154.5 6088023.5, 456152.5 6088022.5, 456153.5 6088020.5, 456150.5 6088019.5, 456150.5 6088017.5, 456148.5 6088016.5, 456150.5 6088015.5, 456151.5 6088013.5, 456148.5 6088012.5, 456149.5 6088010.5, 456147.5 6088009.5, 456147.5 6088008.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456117.5 6088033.5, 456127.5 6088032.5, 456116.5 6088030.5, 456117.5 6088028.5, 456126.5 6088027.5, 456127.5 6088025.5, 456128.5 6088023.5, 456126.5 6088021.5, 456114.5 6088020.5, 456113.5 6088018.5, 456113.5 6088016.5, 456113.5 6088014.5, 456114.5 6088012.5, 456113.5 6088010.5, 456113.5 6088008.5)', 25832),
  12.1, 17.7, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6088008.5, 6087989.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456147.5 6088008.5, 456147.5 6088006.5, 456145.5 6088004.5, 456145.5 6088002.5, 456145.5 6088000.5, 456145.5 6087998.5, 456143.5 6087996.5, 456144.5 6087994.5, 456143.5 6087992.5, 456142.5 6087990.5, 456143.5 6087989.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456113.5 6088008.5, 456112.5 6088006.5, 456104.5 6088005.5, 456105.5 6088003.5, 456106.5 6088001.5, 456107.5 6087999.5, 456106.5 6087997.5, 456106.5 6087995.5, 456106.5 6087993.5, 456105.5 6087991.5, 456102.5 6087989.5)', 25832),
  12.4, 17.6, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6087981.5, 6087956.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456107.5 6087981.5, 456110.5 6087980.5, 456112.5 6087979.5, 456109.5 6087978.5, 456110.5 6087976.5, 456109.5 6087974.5, 456107.5 6087972.5, 456106.5 6087970.5, 456105.5 6087968.5, 456109.5 6087967.5, 456107.5 6087966.5, 456107.5 6087964.5, 456105.5 6087962.5, 456103.5 6087960.5, 456107.5 6087958.5, 456107.5 6087956.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456096.5 6087981.5, 456095.5 6087979.5, 456094.5 6087977.5, 456093.5 6087975.5, 456092.5 6087973.5, 456091.5 6087971.5, 456091.5 6087969.5, 456091.5 6087967.5, 456090.5 6087965.5, 456089.5 6087963.5, 456088.5 6087961.5, 456088.5 6087959.5, 456087.5 6087957.5, 456087.5 6087956.5)', 25832),
  7.6, 13.4, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6087956.5, 6087931.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456107.5 6087956.5, 456103.5 6087955.5, 456103.5 6087953.5, 456105.5 6087952.5, 456106.5 6087950.5, 456102.5 6087949.5, 456105.5 6087948.5, 456103.5 6087947.5, 456105.5 6087946.5, 456103.5 6087945.5, 456105.5 6087944.5, 456105.5 6087942.5, 456102.5 6087940.5, 456100.5 6087938.5, 456102.5 6087936.5, 456102.5 6087934.5, 456102.5 6087932.5, 456102.5 6087931.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456087.5 6087956.5, 456087.5 6087954.5, 456086.5 6087952.5, 456085.5 6087950.5, 456085.5 6087948.5, 456084.5 6087946.5, 456083.5 6087944.5, 456082.5 6087942.5, 456082.5 6087940.5, 456081.5 6087938.5, 456081.5 6087936.5, 456080.5 6087934.5, 456080.5 6087932.5, 456080.5 6087931.5)', 25832),
  8.5, 14.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6087931.5, 6087906.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456102.5 6087931.5, 456098.5 6087929.5, 456095.5 6087928.5, 456097.5 6087926.5, 456097.5 6087924.5, 456097.5 6087922.5, 456096.5 6087920.5, 456093.5 6087919.5, 456096.5 6087918.5, 456096.5 6087916.5, 456094.5 6087914.5, 456094.5 6087912.5, 456092.5 6087911.5, 456092.5 6087909.5, 456093.5 6087907.5, 456092.5 6087906.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456080.5 6087931.5, 456079.5 6087929.5, 456078.5 6087927.5, 456078.5 6087925.5, 456077.5 6087923.5, 456076.5 6087921.5, 456076.5 6087919.5, 456075.5 6087917.5, 456075.5 6087915.5, 456074.5 6087913.5, 456074.5 6087911.5, 456073.5 6087909.5, 456073.5 6087907.5, 456073.5 6087906.5)', 25832),
  8.6, 14.2, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6087906.5, 6087881.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456092.5 6087906.5, 456091.5 6087904.5, 456090.5 6087902.5, 456090.5 6087900.5, 456088.5 6087898.5, 456088.5 6087896.5, 456089.5 6087894.5, 456090.5 6087892.5, 456085.5 6087891.5, 456085.5 6087889.5, 456086.5 6087887.5, 456084.5 6087886.5, 456086.5 6087884.5, 456083.5 6087882.5, 456084.5 6087881.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456073.5 6087906.5, 456072.5 6087904.5, 456071.5 6087902.5, 456070.5 6087900.5, 456069.5 6087898.5, 456070.5 6087896.5, 456069.5 6087894.5, 456068.5 6087892.5, 456067.5 6087890.5, 456067.5 6087888.5, 456065.5 6087886.5, 456064.5 6087884.5, 456064.5 6087882.5, 456063.5 6087881.5)', 25832),
  8.3, 14.1, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6087881.5, 6087856.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456084.5 6087881.5, 456085.5 6087879.5, 456084.5 6087877.5, 456082.5 6087875.5, 456082.5 6087873.5, 456080.5 6087872.5, 456079.5 6087870.5, 456080.5 6087868.5, 456078.5 6087866.5, 456079.5 6087864.5, 456079.5 6087862.5, 456078.5 6087860.5, 456077.5 6087858.5, 456076.5 6087856.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456063.5 6087881.5, 456063.5 6087879.5, 456062.5 6087877.5, 456062.5 6087875.5, 456061.5 6087873.5, 456061.5 6087871.5, 456062.5 6087869.5, 456061.5 6087867.5, 456061.5 6087865.5, 456061.5 6087863.5, 456061.5 6087861.5, 456060.5 6087859.5, 456058.5 6087857.5, 456058.5 6087856.5)', 25832),
  8.6, 14.5, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6087856.5, 6087831.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456076.5 6087856.5, 456077.5 6087854.5, 456079.5 6087852.5, 456077.5 6087851.5, 456079.5 6087850.5, 456073.5 6087849.5, 456073.5 6087847.5, 456072.5 6087845.5, 456074.5 6087844.5, 456072.5 6087843.5, 456071.5 6087841.5, 456070.5 6087839.5, 456070.5 6087837.5, 456071.5 6087835.5, 456070.5 6087833.5, 456069.5 6087831.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456058.5 6087856.5, 456058.5 6087854.5, 456057.5 6087852.5, 456057.5 6087850.5, 456056.5 6087848.5, 456056.5 6087846.5, 456055.5 6087844.5, 456054.5 6087842.5, 456054.5 6087840.5, 456053.5 6087838.5, 456053.5 6087836.5, 456052.5 6087834.5, 456051.5 6087832.5, 456051.5 6087831.5)', 25832),
  8.9, 14.8, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6087831.5, 6087806.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456069.5 6087831.5, 456070.5 6087829.5, 456069.5 6087827.5, 456068.5 6087825.5, 456066.5 6087824.5, 456065.5 6087822.5, 456063.5 6087821.5, 456063.5 6087819.5, 456061.5 6087817.5, 456062.5 6087815.5, 456062.5 6087813.5, 456059.5 6087811.5, 456061.5 6087810.5, 456060.5 6087808.5, 456061.5 6087806.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456051.5 6087831.5, 456050.5 6087829.5, 456049.5 6087827.5, 456049.5 6087825.5, 456049.5 6087823.5, 456048.5 6087821.5, 456047.5 6087819.5, 456046.5 6087817.5, 456045.5 6087815.5, 456044.5 6087813.5, 456044.5 6087811.5, 456043.5 6087809.5, 456043.5 6087807.5, 456042.5 6087806.5)', 25832),
  8.6, 14, 'MITGELIEFERT');
INSERT INTO SY_KLIFFKANTE (STAND, NORD_VON, NORD_BIS, OBERKANTE, FUSS, WAND_MAX_M, OBERKANTE_MAX_M, HERKUNFT)
  VALUES ('GEMESSEN_2024', 6087806.5, 6087774.5,
  MDSYS.SDO_GEOMETRY('LINESTRING (456061.5 6087806.5, 456060.5 6087804.5, 456058.5 6087803.5, 456059.5 6087801.5, 456057.5 6087800.5, 456056.5 6087798.5, 456055.5 6087796.5, 456054.5 6087794.5, 456053.5 6087792.5, 456048.5 6087791.5, 456050.5 6087790.5, 456051.5 6087788.5, 456048.5 6087786.5, 456048.5 6087784.5, 456049.5 6087782.5, 456049.5 6087780.5, 456047.5 6087778.5, 456048.5 6087776.5, 456051.5 6087774.5)', 25832),
  MDSYS.SDO_GEOMETRY('LINESTRING (456042.5 6087806.5, 456041.5 6087804.5, 456040.5 6087802.5, 456039.5 6087800.5, 456038.5 6087798.5, 456038.5 6087796.5, 456038.5 6087794.5, 456038.5 6087792.5, 456037.5 6087790.5, 456036.5 6087788.5, 456035.5 6087786.5, 456034.5 6087784.5, 456034.5 6087782.5, 456034.5 6087780.5, 456033.5 6087778.5, 456033.5 6087776.5, 456027.5 6087774.5)', 25832),
  8, 13.7, 'MITGELIEFERT');

-- --------------------------------------------------------------- Sturmfluten seit 1962 (F38 A, belegt)
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 16.02.1962' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '1962-02-16', t.DATUM_TEXT = '16.02.1962', t.JAHR = 1962, t.SCHEITEL_LIST_CM = 365, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Hörnum zeitweise vom Rest der Insel getrennt; am Pegel List 865 cm PN, 645 min über 700 cm PN', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 16.02.1962', DATE '1962-02-16', '16.02.1962', 1962, 365, NULL, NULL, 'NHN', NULL, NULL, 'Hörnum zeitweise vom Rest der Insel getrennt; am Pegel List 865 cm PN, 645 min über 700 cm PN', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 03.01.1976 [Capella]' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '1976-01-03', t.DATUM_TEXT = '03.01.1976', t.JAHR = 1976, t.SCHEITEL_LIST_CM = 394, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Zweithöchster Wert am Pegel List seit 1900 (894 cm PN)', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 03.01.1976 [Capella]', DATE '1976-01-03', '03.01.1976', 1976, 394, NULL, NULL, 'NHN', NULL, NULL, 'Zweithöchster Wert am Pegel List seit 1900 (894 cm PN)', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 21.01.1976' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '1976-01-21', t.DATUM_TEXT = '21.01.1976', t.JAHR = 1976, t.SCHEITEL_LIST_CM = 847, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'PN List', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Rang 7 der 20 höchsten am Pegel List seit 1900', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 21.01.1976', DATE '1976-01-21', '21.01.1976', 1976, 847, NULL, NULL, 'PN List', NULL, NULL, 'Rang 7 der 20 höchsten am Pegel List seit 1900', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 24.11.1981' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '1981-11-24', t.DATUM_TEXT = '24.11.1981', t.JAHR = 1981, t.SCHEITEL_LIST_CM = 405, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Höchster Wasserstand am Pegel List seit 1900 (905 cm PN)', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 24.11.1981', DATE '1981-11-24', '24.11.1981', 1981, 405, NULL, NULL, 'NHN', NULL, NULL, 'Höchster Wasserstand am Pegel List seit 1900 (905 cm PN)', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 26.01.1990' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '1990-01-26', t.DATUM_TEXT = '26.01.1990', t.JAHR = 1990, t.SCHEITEL_LIST_CM = 858, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'PN List', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Rang 5 am Pegel List; im Winter 1990 Sandzuwachs von 1,6 Mio. m³ an der Westküste', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'FACHPLAN_1997') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 26.01.1990', DATE '1990-01-26', '26.01.1990', 1990, 858, NULL, NULL, 'PN List', NULL, NULL, 'Rang 5 am Pegel List; im Winter 1990 Sandzuwachs von 1,6 Mio. m³ an der Westküste', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'FACHPLAN_1997'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 26.02.1990 [Vivian]' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '1990-02-26', t.DATUM_TEXT = '26.02.1990', t.JAHR = 1990, t.SCHEITEL_LIST_CM = 824, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'PN List', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Serie Februar 1990, Rang 13 am Pegel List', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 26.02.1990 [Vivian]', DATE '1990-02-26', '26.02.1990', 1990, 824, NULL, NULL, 'PN List', NULL, NULL, 'Serie Februar 1990, Rang 13 am Pegel List', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 27.02.1990 [Vivian/Wiebke]' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '1990-02-27', t.DATUM_TEXT = '27.02.1990', t.JAHR = 1990, t.SCHEITEL_LIST_CM = 849, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'PN List', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Serie Februar 1990, Rang 6 am Pegel List', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 27.02.1990 [Vivian/Wiebke]', DATE '1990-02-27', '27.02.1990', 1990, 849, NULL, NULL, 'PN List', NULL, NULL, 'Serie Februar 1990, Rang 6 am Pegel List', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 28.02.1990 [Wiebke]' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '1990-02-28', t.DATUM_TEXT = '28.02.1990', t.JAHR = 1990, t.SCHEITEL_LIST_CM = 827, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'PN List', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Serie Februar 1990, Rang 10 am Pegel List', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 28.02.1990 [Wiebke]', DATE '1990-02-28', '28.02.1990', 1990, 827, NULL, NULL, 'PN List', NULL, NULL, 'Serie Februar 1990, Rang 10 am Pegel List', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Anatol 03.12.1999' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '1999-12-03', t.DATUM_TEXT = '03.12.1999', t.JAHR = 1999, t.SCHEITEL_LIST_CM = 361, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = 'Starke Abbrüche am Roten Kliff (ohne Meterangabe)', t.FOLGEN = 'Am Pegel List 863 cm PN, Rang 4 seit 1900', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'WIKI_ROTES_KLIFF') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Anatol 03.12.1999', DATE '1999-12-03', '03.12.1999', 1999, 361, NULL, NULL, 'NHN', NULL, 'Starke Abbrüche am Roten Kliff (ohne Meterangabe)', 'Am Pegel List 863 cm PN, Rang 4 seit 1900', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'WIKI_ROTES_KLIFF'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 28.01.2002' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2002-01-28', t.DATUM_TEXT = '28.01.2002', t.JAHR = 2002, t.SCHEITEL_LIST_CM = 820, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'PN List', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Rang 15 am Pegel List seit 1900', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 28.01.2002', DATE '2002-01-28', '28.01.2002', 2002, 820, NULL, NULL, 'PN List', NULL, NULL, 'Rang 15 am Pegel List seit 1900', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 08.01.2005' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2005-01-08', t.DATUM_TEXT = '08.01.2005', t.JAHR = 2005, t.SCHEITEL_LIST_CM = 815, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'PN List', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Rang 16 am Pegel List seit 1900', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 08.01.2005', DATE '2005-01-08', '08.01.2005', 2005, 815, NULL, NULL, 'PN List', NULL, NULL, 'Rang 16 am Pegel List seit 1900', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 01.03.2008' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2008-03-01', t.DATUM_TEXT = '01.03.2008', t.JAHR = 2008, t.SCHEITEL_LIST_CM = 811, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'PN List', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Einzige des Winters 2007/08 unter den 20 höchsten; rund 725 000 m³ Sand verlagert', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 01.03.2008', DATE '2008-03-01', '01.03.2008', 2008, 811, NULL, NULL, 'PN List', NULL, NULL, 'Einzige des Winters 2007/08 unter den 20 höchsten; rund 725 000 m³ Sand verlagert', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Xaver 06.12.2013' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2013-12-06', t.DATUM_TEXT = '06.12.2013', t.JAHR = 2013, t.SCHEITEL_LIST_CM = NULL, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = NULL, t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'Dünen auf 23 km beschädigt, Hörnum-Odde rund 20 m verloren, Deichbruch in Keitum, Böe 174 km/h; Pegelwerte für Sylt nicht gefunden; Vorbild der Sturmflut in der App', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'WIKI_XAVER') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Xaver 06.12.2013', DATE '2013-12-06', '06.12.2013', 2013, NULL, NULL, NULL, NULL, NULL, NULL, 'Dünen auf 23 km beschädigt, Hörnum-Odde rund 20 m verloren, Deichbruch in Keitum, Böe 174 km/h; Pegelwerte für Sylt nicht gefunden; Vorbild der Sturmflut in der App', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'WIKI_XAVER'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 14.11.2015' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2015-11-14', t.DATUM_TEXT = '14.11.2015', t.JAHR = 2015, t.SCHEITEL_LIST_CM = 300, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = 'Randdünen- und Kliffabbrüche 0,8 km; das Depot an der Sturmhaube danach fast aufgebraucht', t.FOLGEN = 'List 2,12 m über MThw; Vordünenabbrüche 5,1 km', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 14.11.2015', DATE '2015-11-14', '14.11.2015', 2015, 300, NULL, NULL, 'NHN', NULL, 'Randdünen- und Kliffabbrüche 0,8 km; das Depot an der Sturmhaube danach fast aufgebraucht', 'List 2,12 m über MThw; Vordünenabbrüche 5,1 km', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 27.12.2016' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2016-12-27', t.DATUM_TEXT = '27.12.2016', t.JAHR = 2016, t.SCHEITEL_LIST_CM = 299, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = 'Keine Kliffabbrüche: an der Sturmhaube hat das Depot die Erosion des Kliffs verhindert', t.FOLGEN = 'List 2,11 m über MThw; Vordünenabbrüche 14,1 km', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 27.12.2016', DATE '2016-12-27', '27.12.2016', 2016, 299, NULL, NULL, 'NHN', NULL, 'Keine Kliffabbrüche: an der Sturmhaube hat das Depot die Erosion des Kliffs verhindert', 'List 2,11 m über MThw; Vordünenabbrüche 14,1 km', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 13.09.2017' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2017-09-13', t.DATUM_TEXT = '13.09.2017', t.JAHR = 2017, t.SCHEITEL_LIST_CM = 261, t.SCHEITEL_HOERNUM_CM = 285, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = 'Keine Kliffabbrüche; am Kliffende in Kampen Abbrüche an Aufspülkörpern', t.FOLGEN = 'List 1,73 m, Hörnum 1,82 m über MThw', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 13.09.2017', DATE '2017-09-13', '13.09.2017', 2017, 261, 285, NULL, 'NHN', NULL, 'Keine Kliffabbrüche; am Kliffende in Kampen Abbrüche an Aufspülkörpern', 'List 1,73 m, Hörnum 1,82 m über MThw', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sabine 10.02.2020' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2020-02-10', t.DATUM_TEXT = '10.02.2020', t.JAHR = 2020, t.SCHEITEL_LIST_CM = 299, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = 'Kliff an der Sturmhaube Kampen auf rund 350 m bloßgelegt (Pressemeldung: auf annähernd 300 m angegriffen)', t.FOLGEN = 'Serie 10. bis 12.02.2020 mit fünf Scheiteln (NHN +2,39 bis +2,99 m); Randdünen- und Kliffabbrüche 1,80 km', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_PM_2020') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sabine 10.02.2020', DATE '2020-02-10', '10.02.2020', 2020, 299, NULL, NULL, 'NHN', NULL, 'Kliff an der Sturmhaube Kampen auf rund 350 m bloßgelegt (Pressemeldung: auf annähernd 300 m angegriffen)', 'Serie 10. bis 12.02.2020 mit fünf Scheiteln (NHN +2,39 bis +2,99 m); Randdünen- und Kliffabbrüche 1,80 km', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_PM_2020'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 19.02.2022 [Ylenia/Zeynep/Antonia]' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2022-02-19', t.DATUM_TEXT = '19.02.2022', t.JAHR = 2022, t.SCHEITEL_LIST_CM = 318, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = 'Randdünen- und Kliffabbrüche 0,71 km', t.FOLGEN = 'List 2,28 m über MThw, höchster Wert 2015–2024; Vordünenabbrüche 8,13 km', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 19.02.2022 [Ylenia/Zeynep/Antonia]', DATE '2022-02-19', '19.02.2022', 2022, 318, NULL, NULL, 'NHN', NULL, 'Randdünen- und Kliffabbrüche 0,71 km', 'List 2,28 m über MThw, höchster Wert 2015–2024; Vordünenabbrüche 8,13 km', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 15.10.2023' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2023-10-15', t.DATUM_TEXT = '15.10.2023', t.JAHR = 2023, t.SCHEITEL_LIST_CM = 245, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = NULL, t.FOLGEN = 'List 1,56 m über MThw', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 15.10.2023', DATE '2023-10-15', '15.10.2023', 2023, 245, NULL, NULL, 'NHN', NULL, NULL, 'List 1,56 m über MThw', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN'), 'MITGELIEFERT');
MERGE INTO SY_STURMFLUT t USING (SELECT 'Sturmflut 22.12.2023' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.DATUM = DATE '2023-12-22', t.DATUM_TEXT = '22.12.2023', t.JAHR = 2023, t.SCHEITEL_LIST_CM = 274, t.SCHEITEL_HOERNUM_CM = NULL, t.SCHEITEL_WESTERLAND_CM = NULL, t.BEZUG = 'NHN', t.KLIFFVERLUST_M = NULL, t.KLIFFVERLUST_TEXT = 'Winter 2023/24: Randdünen- und Kliffabbrüche 0,25 km', t.FOLGEN = 'Doppelscheitel 21.12. (NHN +2,69 m) und 22.12.2023 (NHN +2,74 m)', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, DATUM, DATUM_TEXT, JAHR, SCHEITEL_LIST_CM, SCHEITEL_HOERNUM_CM, SCHEITEL_WESTERLAND_CM, BEZUG, KLIFFVERLUST_M, KLIFFVERLUST_TEXT, FOLGEN, QUELLE_ID, HERKUNFT)
  VALUES ('Sturmflut 22.12.2023', DATE '2023-12-22', '22.12.2023', 2023, 274, NULL, NULL, 'NHN', NULL, 'Winter 2023/24: Randdünen- und Kliffabbrüche 0,25 km', 'Doppelscheitel 21.12. (NHN +2,69 m) und 22.12.2023 (NHN +2,74 m)', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_FACHPLAN'), 'MITGELIEFERT');

-- -------------------------------------------- Vorspülungen vor Wenningstedt und Kampen (F39 A, belegt)
MERGE INTO SY_VORSPUELUNG t USING (SELECT '1992 Kampen (Depot mit Höft)' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 1992, t.MENGE_M3 = NULL, t.STRECKE_TEXT = 'Kampen', t.BEMERKUNG = 'Depotaufspülung mit Höft, ohne Menge', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'FACHPLAN_1997') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('1992 Kampen (Depot mit Höft)', 'BELEGT', 1992, NULL, 'Kampen', 'Depotaufspülung mit Höft, ohne Menge', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'FACHPLAN_1997'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2004 Wenningstedt' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2004, t.MENGE_M3 = 233000, t.STRECKE_TEXT = 'Wenningstedt, rund 0,99 km', t.BEMERKUNG = 'Vorstrandaufspülung, 235 m³/m, 07.08.–11.11.2004', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2004 Wenningstedt', 'BELEGT', 2004, 233000, 'Wenningstedt, rund 0,99 km', 'Vorstrandaufspülung, 235 m³/m, 07.08.–11.11.2004', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2008 Kampen' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2008, t.MENGE_M3 = 301000, t.STRECKE_TEXT = 'Kampen, 1,65 km', t.BEMERKUNG = '182 m³/m, 12.05.–05.07.2008; im Profilaufmaß nur rund 126 000 m³ (42 %) wiedergefunden', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2008 Kampen', 'BELEGT', 2008, 301000, 'Kampen, 1,65 km', '182 m³/m, 12.05.–05.07.2008; im Profilaufmaß nur rund 126 000 m³ (42 %) wiedergefunden', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2008 Westerland-Nord/Wenningstedt' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2008, t.MENGE_M3 = 81000, t.STRECKE_TEXT = 'Wenningstedt (Südrand), rund 0,70 km', t.BEMERKUNG = '115 m³/m, 27.08.–09.09.2008; rund 51 000 m³ (63 %) wiedergefunden', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2008 Westerland-Nord/Wenningstedt', 'BELEGT', 2008, 81000, 'Wenningstedt (Südrand), rund 0,70 km', '115 m³/m, 27.08.–09.09.2008; rund 51 000 m³ (63 %) wiedergefunden', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2008 Westküste gesamt' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2008, t.MENGE_M3 = 1236000, t.STRECKE_TEXT = 'ganze Westküste, 6,25 km', t.BEMERKUNG = '198 m³/m; enthält Kampen und Wenningstedt', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2008 Westküste gesamt', 'BELEGT', 2008, 1236000, 'ganze Westküste, 6,25 km', '198 m³/m; enthält Kampen und Wenningstedt', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2009 Kampen (Planung)' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2009, t.MENGE_M3 = 150000, t.STRECKE_TEXT = 'Kampen, 1,558 km', t.BEMERKUNG = 'Nur Planung: 96 m³/m, Priorität 1', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2009 Kampen (Planung)', 'BELEGT', 2009, 150000, 'Kampen, 1,558 km', 'Nur Planung: 96 m³/m, Priorität 1', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2009 Wenningstedt (Planung)' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2009, t.MENGE_M3 = 400000, t.STRECKE_TEXT = 'Wenningstedt, 0,987 km', t.BEMERKUNG = 'Nur Planung: Vorstrandaufspülung 405 m³/m, Priorität 2', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2009 Wenningstedt (Planung)', 'BELEGT', 2009, 400000, 'Wenningstedt, 0,987 km', 'Nur Planung: Vorstrandaufspülung 405 m³/m, Priorität 2', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_BERICHT_2009'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2019 Kampen Hauptstrand (Planung)' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2019, t.MENGE_M3 = 140000, t.STRECKE_TEXT = 'Kampen Hauptstrand', t.BEMERKUNG = 'Geplant laut Pressemitteilung vom 08.04.2019', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'PM_2019') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2019 Kampen Hauptstrand (Planung)', 'BELEGT', 2019, 140000, 'Kampen Hauptstrand', 'Geplant laut Pressemitteilung vom 08.04.2019', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'PM_2019'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2024 Kampen Nord' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2024, t.MENGE_M3 = 100000, t.STRECKE_TEXT = 'Kampen Nord', t.BEMERKUNG = 'Saison 2024', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'PM_2024') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2024 Kampen Nord', 'BELEGT', 2024, 100000, 'Kampen Nord', 'Saison 2024', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'PM_2024'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2024 Kampen Süd' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2024, t.MENGE_M3 = 100000, t.STRECKE_TEXT = 'Kampen Süd', t.BEMERKUNG = 'Saison 2024; ob genau vor dem Kliff, ist nicht belegt', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'PM_2024') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2024 Kampen Süd', 'BELEGT', 2024, 100000, 'Kampen Süd', 'Saison 2024; ob genau vor dem Kliff, ist nicht belegt', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'PM_2024'), 'MITGELIEFERT');
MERGE INTO SY_VORSPUELUNG t USING (SELECT '2026 Wenningstedt/Kampen' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'BELEGT', t.JAHR = 2026, t.MENGE_M3 = NULL, t.STRECKE_TEXT = 'Wenningstedt/Kampen', t.BEMERKUNG = 'Teil von 1,3 Mio. m³ auf 8,15 km; keine Teilmenge genannt', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_PM_2026') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, JAHR, MENGE_M3, STRECKE_TEXT, BEMERKUNG, QUELLE_ID, HERKUNFT)
  VALUES ('2026 Wenningstedt/Kampen', 'BELEGT', 2026, NULL, 'Wenningstedt/Kampen', 'Teil von 1,3 Mio. m³ auf 8,15 km; keine Teilmenge genannt', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'LKN_PM_2026'), 'MITGELIEFERT');

-- ------------------------------------------------------------------------------- Lagen der Wetterregie
MERGE INTO SY_SEEGANG t USING (SELECT 'Sommerabend' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = 'SOMMERABEND', t.PHASE = 'KLAR', t.WIND_MS = 6, t.RICHTUNG_GRAD = 270, t.BOEIGKEIT = 0.14, t.BEDECKUNG_ACHTEL = 3, t.HOHE_SCHICHT = 0.35, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 40, t.NEBEL = 0, t.NEBEL_HOCH_M = 50, t.HS_M = 1, t.TP_S = 8, t.SEE_RICHTUNG_GRAD = 285, t.GEWITTER = 0, t.TEMPERATUR_C = 18, t.BESCHREIBUNG = 'Leichter Westwind, Haufenwolken, die Sonne geht über der See unter [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Sommerabend', 'SOMMERABEND', 'KLAR', 6, 270, 0.14, 3, 0.35, 'CUMULUS', 0, 40, 0, 50, 1, 8, 285, 0, 18, 'Leichter Westwind, Haufenwolken, die Sonne geht über der See unter [G]', 'MITGELIEFERT');
MERGE INTO SY_SEEGANG t USING (SELECT 'Westwind' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = 'WESTWIND', t.PHASE = 'AUFZIEHEND', t.WIND_MS = 11, t.RICHTUNG_GRAD = 260, t.BOEIGKEIT = 0.2, t.BEDECKUNG_ACHTEL = 5, t.HOHE_SCHICHT = 0.2, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 30, t.NEBEL = 0, t.NEBEL_HOCH_M = 50, t.HS_M = 2, t.TP_S = 7, t.SEE_RICHTUNG_GRAD = 270, t.GEWITTER = 0, t.TEMPERATUR_C = 15, t.BESCHREIBUNG = 'Frischer West, Wolken ziehen, Brandung mit Schaumkronen [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Westwind', 'WESTWIND', 'AUFZIEHEND', 11, 260, 0.2, 5, 0.2, 'CUMULUS', 0, 30, 0, 50, 2, 7, 270, 0, 15, 'Frischer West, Wolken ziehen, Brandung mit Schaumkronen [G]', 'MITGELIEFERT');
MERGE INTO SY_SEEGANG t USING (SELECT 'Ablandig' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = 'ABLANDIG', t.PHASE = 'KLAR', t.WIND_MS = 7, t.RICHTUNG_GRAD = 95, t.BOEIGKEIT = 0.15, t.BEDECKUNG_ACHTEL = 1, t.HOHE_SCHICHT = 0.1, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 50, t.NEBEL = 0, t.NEBEL_HOCH_M = 50, t.HS_M = 1.2, t.TP_S = 10, t.SEE_RICHTUNG_GRAD = 290, t.GEWITTER = 0, t.TEMPERATUR_C = 17, t.BESCHREIBUNG = 'Ostwind vom Land: die Brecher stürzen, Gischt weht seewärts [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Ablandig', 'ABLANDIG', 'KLAR', 7, 95, 0.15, 1, 0.1, 'CUMULUS', 0, 50, 0, 50, 1.2, 10, 290, 0, 17, 'Ostwind vom Land: die Brecher stürzen, Gischt weht seewärts [G]', 'MITGELIEFERT');
MERGE INTO SY_SEEGANG t USING (SELECT 'Schauer über See' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = 'SCHAUER', t.PHASE = 'ABKLINGEND', t.WIND_MS = 12, t.RICHTUNG_GRAD = 275, t.BOEIGKEIT = 0.3, t.BEDECKUNG_ACHTEL = 5, t.HOHE_SCHICHT = 0.1, t.WOLKENART = 'CUMULONIMBUS', t.REGEN_MMH = 7, t.SICHT_KM = 28, t.NEBEL = 0, t.NEBEL_HOCH_M = 50, t.HS_M = 2.2, t.TP_S = 8, t.SEE_RICHTUNG_GRAD = 285, t.GEWITTER = 0, t.TEMPERATUR_C = 11, t.BESCHREIBUNG = 'Schauer ziehen über die See, dazwischen Sonne [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Schauer über See', 'SCHAUER', 'ABKLINGEND', 12, 275, 0.3, 5, 0.1, 'CUMULONIMBUS', 7, 28, 0, 50, 2.2, 8, 285, 0, 11, 'Schauer ziehen über die See, dazwischen Sonne [G]', 'MITGELIEFERT');
MERGE INTO SY_SEEGANG t USING (SELECT 'Landregen' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = 'LANDREGEN', t.PHASE = 'FRONT', t.WIND_MS = 10, t.RICHTUNG_GRAD = 225, t.BOEIGKEIT = 0.2, t.BEDECKUNG_ACHTEL = 8, t.HOHE_SCHICHT = 0.2, t.WOLKENART = 'NIMBOSTRATUS', t.REGEN_MMH = 2.5, t.SICHT_KM = 10, t.NEBEL = 0, t.NEBEL_HOCH_M = 50, t.HS_M = 1.6, t.TP_S = 7, t.SEE_RICHTUNG_GRAD = 250, t.GEWITTER = 0, t.TEMPERATUR_C = 12, t.BESCHREIBUNG = 'Grauer Landregen aus Südwest [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Landregen', 'LANDREGEN', 'FRONT', 10, 225, 0.2, 8, 0.2, 'NIMBOSTRATUS', 2.5, 10, 0, 50, 1.6, 7, 250, 0, 12, 'Grauer Landregen aus Südwest [G]', 'MITGELIEFERT');
MERGE INTO SY_SEEGANG t USING (SELECT 'Sturm' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = 'STURM', t.PHASE = 'STURM', t.WIND_MS = 22, t.RICHTUNG_GRAD = 255, t.BOEIGKEIT = 0.22, t.BEDECKUNG_ACHTEL = 7.5, t.HOHE_SCHICHT = 0, t.WOLKENART = 'STRATOCUMULUS', t.REGEN_MMH = 5, t.SICHT_KM = 8, t.NEBEL = 0, t.NEBEL_HOCH_M = 50, t.HS_M = 3.4, t.TP_S = 10, t.SEE_RICHTUNG_GRAD = 265, t.GEWITTER = 0, t.TEMPERATUR_C = 10, t.BESCHREIBUNG = 'Sturm aus Westsüdwest, die See bricht weit draußen [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Sturm', 'STURM', 'STURM', 22, 255, 0.22, 7.5, 0, 'STRATOCUMULUS', 5, 8, 0, 50, 3.4, 10, 265, 0, 10, 'Sturm aus Westsüdwest, die See bricht weit draußen [G]', 'MITGELIEFERT');
MERGE INTO SY_SEEGANG t USING (SELECT 'Seenebel' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = 'SEENEBEL', t.PHASE = 'NEBEL', t.WIND_MS = 2.5, t.RICHTUNG_GRAD = 220, t.BOEIGKEIT = 0.08, t.BEDECKUNG_ACHTEL = 1, t.HOHE_SCHICHT = 0.25, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 14, t.NEBEL = 0.9, t.NEBEL_HOCH_M = 55, t.HS_M = 0.5, t.TP_S = 9, t.SEE_RICHTUNG_GRAD = 280, t.GEWITTER = 0, t.TEMPERATUR_C = 13, t.BESCHREIBUNG = 'Seenebel liegt über Strand und Kliff [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Seenebel', 'SEENEBEL', 'NEBEL', 2.5, 220, 0.08, 1, 0.25, 'CUMULUS', 0, 14, 0.9, 55, 0.5, 9, 280, 0, 13, 'Seenebel liegt über Strand und Kliff [G]', 'MITGELIEFERT');
MERGE INTO SY_SEEGANG t USING (SELECT 'Seerauch' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = 'SEERAUCH', t.PHASE = 'KLAR', t.WIND_MS = 4, t.RICHTUNG_GRAD = 110, t.BOEIGKEIT = 0.1, t.BEDECKUNG_ACHTEL = 0, t.HOHE_SCHICHT = 0.05, t.WOLKENART = 'CUMULUS', t.REGEN_MMH = 0, t.SICHT_KM = 30, t.NEBEL = 0.55, t.NEBEL_HOCH_M = 9, t.HS_M = 0.6, t.TP_S = 9, t.SEE_RICHTUNG_GRAD = 290, t.GEWITTER = 0, t.TEMPERATUR_C = -4, t.BESCHREIBUNG = 'Kalte Luft über warmem Wasser: Seerauch im Winter [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Seerauch', 'SEERAUCH', 'KLAR', 4, 110, 0.1, 0, 0.05, 'CUMULUS', 0, 30, 0.55, 9, 0.6, 9, 290, 0, -4, 'Kalte Luft über warmem Wasser: Seerauch im Winter [G]', 'MITGELIEFERT');
MERGE INTO SY_SEEGANG t USING (SELECT 'Gewitter über der See' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = 'GEWITTER', t.PHASE = 'GEWITTER', t.WIND_MS = 9, t.RICHTUNG_GRAD = 250, t.BOEIGKEIT = 0.3, t.BEDECKUNG_ACHTEL = 6, t.HOHE_SCHICHT = 0.1, t.WOLKENART = 'CUMULONIMBUS', t.REGEN_MMH = 9, t.SICHT_KM = 18, t.NEBEL = 0, t.NEBEL_HOCH_M = 50, t.HS_M = 1.6, t.TP_S = 8, t.SEE_RICHTUNG_GRAD = 270, t.GEWITTER = 2, t.TEMPERATUR_C = 19, t.BESCHREIBUNG = 'Gewitter über der See mit Blitz und Donner [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Gewitter über der See', 'GEWITTER', 'GEWITTER', 9, 250, 0.3, 6, 0.1, 'CUMULONIMBUS', 9, 18, 0, 50, 1.6, 8, 270, 2, 19, 'Gewitter über der See mit Blitz und Donner [G]', 'MITGELIEFERT');
MERGE INTO SY_SEEGANG t USING (SELECT 'Xaver (Höhepunkt)' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.SCHLUESSEL = NULL, t.PHASE = 'ORKAN', t.WIND_MS = 27, t.RICHTUNG_GRAD = 300, t.BOEIGKEIT = 0.3, t.BEDECKUNG_ACHTEL = 8, t.HOHE_SCHICHT = 0, t.WOLKENART = 'STRATOCUMULUS', t.REGEN_MMH = 5, t.SICHT_KM = 3, t.NEBEL = 0, t.NEBEL_HOCH_M = 0, t.HS_M = 6, t.TP_S = 12, t.SEE_RICHTUNG_GRAD = 300, t.GEWITTER = 0, t.TEMPERATUR_C = 4, t.BESCHREIBUNG = 'Die Sturmflut nach Xaver in der Nacht zum 6.12.2013: Orkan aus Nordwest, Böen 39 m/s, See H_s 6 m (Fahrplan der App) [G]' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, SCHLUESSEL, PHASE, WIND_MS, RICHTUNG_GRAD, BOEIGKEIT, BEDECKUNG_ACHTEL, HOHE_SCHICHT, WOLKENART, REGEN_MMH, SICHT_KM, NEBEL, NEBEL_HOCH_M, HS_M, TP_S, SEE_RICHTUNG_GRAD, GEWITTER, TEMPERATUR_C, BESCHREIBUNG, HERKUNFT)
  VALUES ('Xaver (Höhepunkt)', NULL, 'ORKAN', 27, 300, 0.3, 8, 0, 'STRATOCUMULUS', 5, 3, 0, 0, 6, 12, 300, 0, 4, 'Die Sturmflut nach Xaver in der Nacht zum 6.12.2013: Orkan aus Nordwest, Böen 39 m/s, See H_s 6 m (Fahrplan der App) [G]', 'MITGELIEFERT');

-- -------------------------------------------------------------------- Leuchtfeuer und Sektoren (F40 A)
MERGE INTO SY_LEUCHTFEUER t USING (SELECT 'Kampener Leuchtturm' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.KENNUNG = 'Iso WR 7s', t.FEUERHOEHE_M = 62, t.TURMHOEHE_M = 40, t.TRAGWEITE_SM = 20, t.IN_BETRIEB_VON = 1856, t.IN_BETRIEB_BIS = NULL, t.HEUTE_AN = 1, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457770, 6089007, NULL), NULL, NULL), t.OST = 457770, t.NORD = 6089007, t.BESCHREIBUNG = '„Langer Christian“, 1855/56; gelbe Bornholmer Ziegel mit Eisenringen, weiß mit schwarzem Band; seit 1975 mit rotem Sektor über dem Roten Kliff anstelle des Quermarkenfeuers (Grenzen des Sektors geschätzt [G])', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'WIKI_KAMPEN') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, KENNUNG, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, IN_BETRIEB_VON, IN_BETRIEB_BIS, HEUTE_AN, LAGE, OST, NORD, BESCHREIBUNG, QUELLE_ID, HERKUNFT)
  VALUES ('Kampener Leuchtturm', 'Iso WR 7s', 62, 40, 20, 1856, NULL, 1, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457770, 6089007, NULL), NULL, NULL), 457770, 6089007, '„Langer Christian“, 1855/56; gelbe Bornholmer Ziegel mit Eisenringen, weiß mit schwarzem Band; seit 1975 mit rotem Sektor über dem Roten Kliff anstelle des Quermarkenfeuers (Grenzen des Sektors geschätzt [G])', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'WIKI_KAMPEN'), 'MITGELIEFERT');
DELETE FROM SY_SEKTOR WHERE LEUCHTFEUER_ID IN (SELECT ID FROM SY_LEUCHTFEUER WHERE NAME = 'Kampener Leuchtturm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_SEKTOR (LEUCHTFEUER_ID, NR, FARBE, VON_GRAD, BIS_GRAD, TRAGWEITE_SM, TEXT) SELECT ID, 1, 'W', 120, 105, 20, NULL FROM SY_LEUCHTFEUER WHERE NAME = 'Kampener Leuchtturm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_SEKTOR (LEUCHTFEUER_ID, NR, FARBE, VON_GRAD, BIS_GRAD, TRAGWEITE_SM, TEXT) SELECT ID, 2, 'R', 105, 120, 16, 'Rotes Kliff' FROM SY_LEUCHTFEUER WHERE NAME = 'Kampener Leuchtturm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_LEUCHTFEUER t USING (SELECT 'Quermarkenfeuer Rotes Kliff' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.KENNUNG = 'F WRG', t.FEUERHOEHE_M = 23, t.TURMHOEHE_M = 13, t.TRAGWEITE_SM = 21, t.IN_BETRIEB_VON = 1913, t.IN_BETRIEB_BIS = 1975, t.HEUTE_AN = 0, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457603, 6091174, NULL), NULL, NULL), t.OST = 457603, t.NORD = 6091174, t.BESCHREIBUNG = 'Achteckiger Backsteinturm 1912/13, 1936 elektrisch, 1975 außer Betrieb, seit 1993 restauriert; heute Tagesmarke; Sektoren wie früher (Grenzen und Tragweiten von Rot und Grün geschätzt [G])', t.QUELLE_ID = (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'WIKI_QUERMARKE') WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, KENNUNG, FEUERHOEHE_M, TURMHOEHE_M, TRAGWEITE_SM, IN_BETRIEB_VON, IN_BETRIEB_BIS, HEUTE_AN, LAGE, OST, NORD, BESCHREIBUNG, QUELLE_ID, HERKUNFT)
  VALUES ('Quermarkenfeuer Rotes Kliff', 'F WRG', 23, 13, 21, 1913, 1975, 0, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457603, 6091174, NULL), NULL, NULL), 457603, 6091174, 'Achteckiger Backsteinturm 1912/13, 1936 elektrisch, 1975 außer Betrieb, seit 1993 restauriert; heute Tagesmarke; Sektoren wie früher (Grenzen und Tragweiten von Rot und Grün geschätzt [G])', (SELECT MAX(ID) FROM SY_QUELLE WHERE KURZ = 'WIKI_QUERMARKE'), 'MITGELIEFERT');
DELETE FROM SY_SEKTOR WHERE LEUCHTFEUER_ID IN (SELECT ID FROM SY_LEUCHTFEUER WHERE NAME = 'Quermarkenfeuer Rotes Kliff' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_SEKTOR (LEUCHTFEUER_ID, NR, FARBE, VON_GRAD, BIS_GRAD, TRAGWEITE_SM, TEXT) SELECT ID, 1, 'G', 30, 87, 17, NULL FROM SY_LEUCHTFEUER WHERE NAME = 'Quermarkenfeuer Rotes Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_SEKTOR (LEUCHTFEUER_ID, NR, FARBE, VON_GRAD, BIS_GRAD, TRAGWEITE_SM, TEXT) SELECT ID, 2, 'W', 87, 93, 21, 'Quermarke' FROM SY_LEUCHTFEUER WHERE NAME = 'Quermarkenfeuer Rotes Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_SEKTOR (LEUCHTFEUER_ID, NR, FARBE, VON_GRAD, BIS_GRAD, TRAGWEITE_SM, TEXT) SELECT ID, 3, 'R', 93, 150, 16, NULL FROM SY_LEUCHTFEUER WHERE NAME = 'Quermarkenfeuer Rotes Kliff' AND HERKUNFT = 'MITGELIEFERT';

-- ----------------------------------------------------------------------------------------- Regiebücher
MERGE INTO SY_REGIEBUCH t USING (SELECT 'Ein Abend am Kliff' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN NOT MATCHED THEN INSERT (NAME, LAENGE_S, TEXT, HERKUNFT) VALUES ('Ein Abend am Kliff', 250, '-', 'MITGELIEFERT');
UPDATE SY_REGIEBUCH SET BESCHREIBUNG = '20. Juli 2026, ein Sommerabend am Roten Kliff: die Treppe hinunter an den Strand, Strandkörbe im letzten Licht, Vögel am Spülsaum aus der Höhe eines Strandläufers, eine Möwe im Aufwind, die Sonne geht über der See unter, das Kliff glüht, der Kampener Leuchtturm geht an', DATEI = 'abend.txt', SZENE_MS = 1784572800000, SZENE_ZEIT = FROM_TZ(TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(1784572800000 / 1000, 'SECOND'), 'UTC') AT TIME ZONE 'Europe/Berlin', ZEITRAFFER = 1, LAENGE_S = 250, FOTOS = 9, WARTEN = 2, BLATT = 1,
  TEXT = TO_CLOB('# Sylt · Regiebuch (Phase 15). Eine Zeile je Stichwort: Zeit (Minuten:Sekunden echte Zeit), Befehl, Argumente, nach' || CHR(10) || '# „ | “ der Text fürs HUD und die Untertitel im Kino. Mehrere Befehle zur selben Zeit sind ein Stichwort. Befehle: zeit,' || CHR(10) || '# raffer, lage, regie, sturmflut, abbruch, vorspuelung, ansicht, fahrt, warte, foto, blatt, sage.' || CHR(10) || '# „warte uhr [TT.MM.] hh:mm“ hält die Uhr des Regiebuchs an, bis die Szene so weit ist. Gestaltet [G].' || CHR(10) || 'Regiebuch: Ein Abend am Kliff' || CHR(10) || 'Beschreibung: 20. Juli 2026, ein Sommerabend am Roten Kliff: die Treppe hinunter an den Strand, Strandkörbe im letzten Licht, Vögel am Spülsaum aus der Höhe eines Strandläufers, eine Möwe im Aufwind, die Sonne geht über der See unter, das Kliff glüht, der Kampener Leuchtturm geht an' || CHR(10) || 'Länge: 4:10' || CHR(10) || '0:00 zeit 20.07.2026 20:40 | 20:40: ein Sommerabend am Roten Kliff' || CHR(10) || '0:00 lage Sommerabend' || CHR(10) || '0:00 raffer 1' || CHR(10) || '0:00 ansicht Kliffkante' || CHR(10) || '0:08 foto Kliffkante: der Abend über der See' || CHR(10) || '0:12 fahrt Treppe | die Treppe hinunter an den Strand' || CHR(10) || '0:44 foto ')
    || TO_CLOB('Treppe: unten am Strand' || CHR(10) || '0:48 raffer 30' || CHR(10) || '0:48 ansicht Strandkörbe | 20:45: die Strandkörbe im letzten Licht' || CHR(10) || '1:00 foto Strandkörbe im Abendlicht' || CHR(10) || '1:06 ansicht Strandläufer | am Spülsaum laufen Austernfischer und Möwen dem Schwall nach' || CHR(10) || '1:18 foto Strandläufer-Auge: Vögel am Spülsaum' || CHR(10) || '1:24 fahrt Möwe | mit einer Möwe im Aufwind vor der Kante' || CHR(10) || '1:50 foto Möwenflug vor der Kante' || CHR(10) || '1:56 ansicht Sonnenuntergang | die Sonne sinkt zur Kimm' || CHR(10) || '2:08 warte uhr 21:52' || CHR(10) || '2:08 raffer 4 | 21:52: die Sonne berührt die Kimm' || CHR(10) || '2:14 foto Sonnenuntergang über der See' || CHR(10) || '2:34 raffer 20' || CHR(10) || '2:34 ansicht Vor dem Kliff | das Kliff glüht im letzten Licht' || CHR(10) || '2:44 foto Vor dem Kliff: das Kliff glüht' || CHR(10) || '2:52 raffer 40' || CHR(10) || '2:52 ansicht Kampener Leuchtturm | der Kampener Leuchtturm wartet auf die Dämmerung' || CHR(10) || '3:00 warte uhr 22:25' || CHR(10) || '3:00 foto Kampener Leuchtturm: das Feuer ist an | 22:25: Kampen brennt, alle 7 Sekunden ein Blitz' || CHR(10) || '3:20 ansicht Spülsaum | die Nacht kommt vom Meer' || CHR(10) || '3:45 foto Spülsaum in der Dämmerung' || CHR(10) || '4:00 blatt Ein Abend am Kliff' || CHR(10) || '4:00 raffer 1')
    || TO_CLOB('' || CHR(10) || '')
  WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
DELETE FROM SY_STICHWORT WHERE REGIEBUCH_ID IN (SELECT ID FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 1, 0, 'ZEIT', '20.07.2026 20:40', '20:40: ein Sommerabend am Roten Kliff' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 2, 0, 'LAGE', 'Sommerabend', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 3, 0, 'RAFFER', '1', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 4, 0, 'ANSICHT', 'Kliffkante', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 5, 8, 'FOTO', 'Kliffkante: der Abend über der See', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 6, 12, 'FAHRT', 'Treppe', 'die Treppe hinunter an den Strand' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 7, 44, 'FOTO', 'Treppe: unten am Strand', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 8, 48, 'RAFFER', '30', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 9, 48, 'ANSICHT', 'Strandkörbe', '20:45: die Strandkörbe im letzten Licht' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 10, 60, 'FOTO', 'Strandkörbe im Abendlicht', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 11, 66, 'ANSICHT', 'Strandläufer', 'am Spülsaum laufen Austernfischer und Möwen dem Schwall nach' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 12, 78, 'FOTO', 'Strandläufer-Auge: Vögel am Spülsaum', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 13, 84, 'FAHRT', 'Möwe', 'mit einer Möwe im Aufwind vor der Kante' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 14, 110, 'FOTO', 'Möwenflug vor der Kante', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 15, 116, 'ANSICHT', 'Sonnenuntergang', 'die Sonne sinkt zur Kimm' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 16, 128, 'WARTE', 'uhr 21:52', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 17, 128, 'RAFFER', '4', '21:52: die Sonne berührt die Kimm' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 18, 134, 'FOTO', 'Sonnenuntergang über der See', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 19, 154, 'RAFFER', '20', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 20, 154, 'ANSICHT', 'Vor dem Kliff', 'das Kliff glüht im letzten Licht' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 21, 164, 'FOTO', 'Vor dem Kliff: das Kliff glüht', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 22, 172, 'RAFFER', '40', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 23, 172, 'ANSICHT', 'Kampener Leuchtturm', 'der Kampener Leuchtturm wartet auf die Dämmerung' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 24, 180, 'WARTE', 'uhr 22:25', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 25, 180, 'FOTO', 'Kampener Leuchtturm: das Feuer ist an', '22:25: Kampen brennt, alle 7 Sekunden ein Blitz' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 26, 200, 'ANSICHT', 'Spülsaum', 'die Nacht kommt vom Meer' FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 27, 225, 'FOTO', 'Spülsaum in der Dämmerung', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 28, 240, 'BLATT', 'Ein Abend am Kliff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 29, 240, 'RAFFER', '1', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Abend am Kliff' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_REGIEBUCH t USING (SELECT 'Die Sturmflut' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN NOT MATCHED THEN INSERT (NAME, LAENGE_S, TEXT, HERKUNFT) VALUES ('Die Sturmflut', 250, '-', 'MITGELIEFERT');
UPDATE SY_REGIEBUCH SET BESCHREIBUNG = 'Orkan Xaver, 5. bis 7. Dezember 2013: der Sturm frischt auf, der Windstau hebt das Wasser bis an den Kliffuß, die Brecher schlagen die Hohlkehle, das Kliff bricht, am Morgen danach liegen Sturzkegel und Findlinge am Strand, eine Linie zeigt, wo die Wand stand', DATEI = 'sturmflut.txt', SZENE_MS = 1386241200000, SZENE_ZEIT = FROM_TZ(TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(1386241200000 / 1000, 'SECOND'), 'UTC') AT TIME ZONE 'Europe/Berlin', ZEITRAFFER = 120, LAENGE_S = 250, FOTOS = 10, WARTEN = 5, BLATT = 1,
  TEXT = TO_CLOB('# Sylt · Regiebuch (Phase 15), Befehle wie in abend.txt. Gestaltet [G]; der Fahrplan des Sturms nach Xaver (Phase 12).' || CHR(10) || 'Regiebuch: Die Sturmflut' || CHR(10) || 'Beschreibung: Orkan Xaver, 5. bis 7. Dezember 2013: der Sturm frischt auf, der Windstau hebt das Wasser bis an den Kliffuß, die Brecher schlagen die Hohlkehle, das Kliff bricht, am Morgen danach liegen Sturzkegel und Findlinge am Strand, eine Linie zeigt, wo die Wand stand' || CHR(10) || 'Länge: 4:10' || CHR(10) || '0:00 sturmflut 05.12.2013 12:00 | 5.12.2013, 12 Uhr: Xaver frischt aus West auf' || CHR(10) || '0:00 raffer 120' || CHR(10) || '0:00 ansicht Am Kliff' || CHR(10) || '0:12 foto Am Kliff: der Sturm kommt aus West' || CHR(10) || '0:16 ansicht Kliffkante | Sturm über der Kante, die Gischt weht landeinwärts' || CHR(10) || '0:28 foto Kliffkante im Sturm' || CHR(10) || '0:32 fahrt Drohne | die Brandung weiß bis zum Riff' || CHR(10) || '1:12 foto Drohne: die Brandungszone weiß bis zum Riff' || CHR(10) || '1:20 raffer 1800' || CHR(10) || '1:20 ansicht Kampener Leuchtturm | die Nacht: Orkan aus Nordwest, der Windstau hebt das Wasser über drei Meter' || CHR(10) || '1:40 foto Kampener Leuchtturm in der Sturmnacht' || CHR(10) || '1:44 warte uhr 06.1')
    || TO_CLOB('2. 09:30' || CHR(10) || '1:44 raffer 120 | 6.12., 9:30: der Morgen, die See steht am Kliffuß' || CHR(10) || '1:44 ansicht Vor dem Kliff' || CHR(10) || '1:56 foto Vor dem Kliff: Brecher am Fuß' || CHR(10) || '2:00 ansicht Abbruch | die Hohlkehle unter der Wand' || CHR(10) || '2:12 foto Die Hohlkehle vor dem Abbruch' || CHR(10) || '2:16 warte uhr 06.12. 11:00' || CHR(10) || '2:16 raffer 1 | 11 Uhr: die Wand bricht' || CHR(10) || '2:16 abbruch 6090335' || CHR(10) || '2:18 warte abbruch' || CHR(10) || '2:24 foto Der Abbruch' || CHR(10) || '2:26 warte ruhe' || CHR(10) || '2:26 foto Der Sturzkegel, kaum dass alles liegt' || CHR(10) || '2:32 raffer 1800 | die nächste Flut trägt am Kegel' || CHR(10) || '2:32 ansicht Kliffkante' || CHR(10) || '2:44 warte uhr 07.12. 10:00' || CHR(10) || '2:44 raffer 60 | 7.12., der Morgen danach: Sturzkegel, Findlinge, die Linie, wo die Wand stand' || CHR(10) || '2:44 ansicht Abbruch' || CHR(10) || '2:58 foto Der Morgen danach: Sturzkegel und Findlinge' || CHR(10) || '3:04 fahrt Drohne | die neue Wand vom Wasser aus' || CHR(10) || '3:44 foto Drohne: das Kliff nach der Sturmflut' || CHR(10) || '3:54 blatt Die Sturmflut' || CHR(10) || '3:54 raffer 1' || CHR(10) || '')
  WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
DELETE FROM SY_STICHWORT WHERE REGIEBUCH_ID IN (SELECT ID FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 1, 0, 'STURMFLUT', '05.12.2013 12:00', '5.12.2013, 12 Uhr: Xaver frischt aus West auf' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 2, 0, 'RAFFER', '120', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 3, 0, 'ANSICHT', 'Am Kliff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 4, 12, 'FOTO', 'Am Kliff: der Sturm kommt aus West', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 5, 16, 'ANSICHT', 'Kliffkante', 'Sturm über der Kante, die Gischt weht landeinwärts' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 6, 28, 'FOTO', 'Kliffkante im Sturm', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 7, 32, 'FAHRT', 'Drohne', 'die Brandung weiß bis zum Riff' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 8, 72, 'FOTO', 'Drohne: die Brandungszone weiß bis zum Riff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 9, 80, 'RAFFER', '1800', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 10, 80, 'ANSICHT', 'Kampener Leuchtturm', 'die Nacht: Orkan aus Nordwest, der Windstau hebt das Wasser über drei Meter' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 11, 100, 'FOTO', 'Kampener Leuchtturm in der Sturmnacht', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 12, 104, 'WARTE', 'uhr 06.12. 09:30', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 13, 104, 'RAFFER', '120', '6.12., 9:30: der Morgen, die See steht am Kliffuß' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 14, 104, 'ANSICHT', 'Vor dem Kliff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 15, 116, 'FOTO', 'Vor dem Kliff: Brecher am Fuß', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 16, 120, 'ANSICHT', 'Abbruch', 'die Hohlkehle unter der Wand' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 17, 132, 'FOTO', 'Die Hohlkehle vor dem Abbruch', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 18, 136, 'WARTE', 'uhr 06.12. 11:00', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 19, 136, 'RAFFER', '1', '11 Uhr: die Wand bricht' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 20, 136, 'ABBRUCH', '6090335', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 21, 138, 'WARTE', 'abbruch', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 22, 144, 'FOTO', 'Der Abbruch', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 23, 146, 'WARTE', 'ruhe', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 24, 146, 'FOTO', 'Der Sturzkegel, kaum dass alles liegt', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 25, 152, 'RAFFER', '1800', 'die nächste Flut trägt am Kegel' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 26, 152, 'ANSICHT', 'Kliffkante', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 27, 164, 'WARTE', 'uhr 07.12. 10:00', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 28, 164, 'RAFFER', '60', '7.12., der Morgen danach: Sturzkegel, Findlinge, die Linie, wo die Wand stand' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 29, 164, 'ANSICHT', 'Abbruch', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 30, 178, 'FOTO', 'Der Morgen danach: Sturzkegel und Findlinge', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 31, 184, 'FAHRT', 'Drohne', 'die neue Wand vom Wasser aus' FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 32, 224, 'FOTO', 'Drohne: das Kliff nach der Sturmflut', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 33, 234, 'BLATT', 'Die Sturmflut', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 34, 234, 'RAFFER', '1', NULL FROM SY_REGIEBUCH WHERE NAME = 'Die Sturmflut' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_REGIEBUCH t USING (SELECT 'Ein Jahr am Kliff' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN NOT MATCHED THEN INSERT (NAME, LAENGE_S, TEXT, HERKUNFT) VALUES ('Ein Jahr am Kliff', 280, '-', 'MITGELIEFERT');
UPDATE SY_REGIEBUCH SET BESCHREIBUNG = 'Juni 2026 bis Juni 2027 am Roten Kliff: Sommer mit Strandkörben, ein Gewitter im August, die Sanderlinge im September, der erste Herbststurm, die Wintersturmflut mit dem Abbruch, die Vorspülung im Frühjahr, der Sommer danach', DATEI = 'jahr.txt', SZENE_MS = 1781532000000, SZENE_ZEIT = FROM_TZ(TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(1781532000000 / 1000, 'SECOND'), 'UTC') AT TIME ZONE 'Europe/Berlin', ZEITRAFFER = 60, LAENGE_S = 280, FOTOS = 13, WARTEN = 4, BLATT = 1,
  TEXT = TO_CLOB('# Sylt · Regiebuch (Phase 15), Befehle wie in abend.txt. Gestaltet [G]. Die Wintersturmflut folgt dem Fahrplan von' || CHR(10) || '# Xaver, an einem Tag im Dezember 2026. Die Vorspülung ist gerafft: in Wirklichkeit dauert sie Wochen.' || CHR(10) || 'Regiebuch: Ein Jahr am Kliff' || CHR(10) || 'Beschreibung: Juni 2026 bis Juni 2027 am Roten Kliff: Sommer mit Strandkörben, ein Gewitter im August, die Sanderlinge im September, der erste Herbststurm, die Wintersturmflut mit dem Abbruch, die Vorspülung im Frühjahr, der Sommer danach' || CHR(10) || 'Länge: 4:40' || CHR(10) || '0:00 zeit 15.06.2026 16:00 | Juni: Sommer am Roten Kliff' || CHR(10) || '0:00 lage Sommerabend' || CHR(10) || '0:00 raffer 60' || CHR(10) || '0:00 ansicht Kliffkante' || CHR(10) || '0:10 foto Juni: Sommer am Kliff' || CHR(10) || '0:14 ansicht Strandkörbe | die Strandkörbe stehen von Mai bis September' || CHR(10) || '0:24 foto Juni: Strandkörbe' || CHR(10) || '0:28 zeit 02.08.2026 21:15 | August: ein Gewitter über der See' || CHR(10) || '0:28 lage Gewitter' || CHR(10) || '0:28 raffer 2' || CHR(10) || '0:28 ansicht Am Kliff' || CHR(10) || '0:44 foto August: Gewitter über der See' || CHR(10) || '0:48 zeit 20.09.2026 18:00 | September: die Sanderlinge ziehen durch' || CHR(10) || '0:48 lage Sommerabend' || CHR(10) || '0:4')
    || TO_CLOB('8 raffer 30' || CHR(10) || '0:48 ansicht Strandläufer' || CHR(10) || '1:00 foto September: Sanderlinge am Spülsaum' || CHR(10) || '1:04 zeit 25.10.2026 13:00 | Oktober: der erste Herbststurm, die Strandkörbe sind fort' || CHR(10) || '1:04 lage Sturm' || CHR(10) || '1:04 raffer 60' || CHR(10) || '1:04 ansicht Am Kliff' || CHR(10) || '1:16 foto Oktober: Herbststurm' || CHR(10) || '1:20 ansicht Kliffkante' || CHR(10) || '1:30 foto Oktober: Sturm über der Kante' || CHR(10) || '1:34 sturmflut 05.12.2026 12:00 | Dezember: die Wintersturmflut, wie Xaver' || CHR(10) || '1:34 raffer 1800' || CHR(10) || '1:34 ansicht Vor dem Kliff' || CHR(10) || '1:52 warte uhr 06.12. 10:30' || CHR(10) || '1:52 raffer 120 | 6.12.: die See steht am Kliffuß' || CHR(10) || '1:52 ansicht Abbruch' || CHR(10) || '2:02 foto Dezember: die Hohlkehle' || CHR(10) || '2:06 raffer 1 | die Wand bricht' || CHR(10) || '2:06 abbruch 6090335' || CHR(10) || '2:08 warte abbruch' || CHR(10) || '2:14 foto Dezember: der Abbruch' || CHR(10) || '2:16 warte ruhe' || CHR(10) || '2:16 raffer 3600 | Winter: die Fluten tragen am Sturzkegel' || CHR(10) || '2:16 ansicht Kliffkante' || CHR(10) || '2:30 zeit 10.03.2027 10:00 | März: die Vorspülung legt Sand vor das Kliff' || CHR(10) || '2:30 lage Westwind' || CHR(10) || '2:30 vorspuelung' || CHR(10) || '2:30 raffer 3600' || CHR(10) || '2:30 ansicht Vorspülung' || CHR(10) || '2:33 foto März: das Spülfeld wächst' || CHR(10) || '2:42 warte vorspuelung' || CHR(10) || '2:42 foto März: d')
    || TO_CLOB('er neue Strand vor dem Kliff' || CHR(10) || '2:48 raffer 3600 | die Brandung formt den Sand um' || CHR(10) || '2:48 ansicht Abbruch' || CHR(10) || '3:10 foto April: der Sand vor dem Kliff, von der Brandung geformt' || CHR(10) || '3:16 zeit 15.06.2027 16:00 | Juni 2027: ein Jahr ist um, die Strandkörbe stehen wieder' || CHR(10) || '3:16 lage Sommerabend' || CHR(10) || '3:16 raffer 60' || CHR(10) || '3:16 ansicht Strandkörbe' || CHR(10) || '3:28 foto Juni 2027: Strandkörbe auf dem vorgespülten Strand' || CHR(10) || '3:34 fahrt Drohne | das Kliff ein Jahr später' || CHR(10) || '4:14 foto Drohne: das Kliff ein Jahr später' || CHR(10) || '4:24 blatt Ein Jahr am Kliff' || CHR(10) || '4:24 raffer 1' || CHR(10) || '')
  WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
DELETE FROM SY_STICHWORT WHERE REGIEBUCH_ID IN (SELECT ID FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 1, 0, 'ZEIT', '15.06.2026 16:00', 'Juni: Sommer am Roten Kliff' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 2, 0, 'LAGE', 'Sommerabend', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 3, 0, 'RAFFER', '60', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 4, 0, 'ANSICHT', 'Kliffkante', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 5, 10, 'FOTO', 'Juni: Sommer am Kliff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 6, 14, 'ANSICHT', 'Strandkörbe', 'die Strandkörbe stehen von Mai bis September' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 7, 24, 'FOTO', 'Juni: Strandkörbe', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 8, 28, 'ZEIT', '02.08.2026 21:15', 'August: ein Gewitter über der See' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 9, 28, 'LAGE', 'Gewitter', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 10, 28, 'RAFFER', '2', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 11, 28, 'ANSICHT', 'Am Kliff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 12, 44, 'FOTO', 'August: Gewitter über der See', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 13, 48, 'ZEIT', '20.09.2026 18:00', 'September: die Sanderlinge ziehen durch' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 14, 48, 'LAGE', 'Sommerabend', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 15, 48, 'RAFFER', '30', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 16, 48, 'ANSICHT', 'Strandläufer', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 17, 60, 'FOTO', 'September: Sanderlinge am Spülsaum', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 18, 64, 'ZEIT', '25.10.2026 13:00', 'Oktober: der erste Herbststurm, die Strandkörbe sind fort' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 19, 64, 'LAGE', 'Sturm', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 20, 64, 'RAFFER', '60', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 21, 64, 'ANSICHT', 'Am Kliff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 22, 76, 'FOTO', 'Oktober: Herbststurm', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 23, 80, 'ANSICHT', 'Kliffkante', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 24, 90, 'FOTO', 'Oktober: Sturm über der Kante', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 25, 94, 'STURMFLUT', '05.12.2026 12:00', 'Dezember: die Wintersturmflut, wie Xaver' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 26, 94, 'RAFFER', '1800', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 27, 94, 'ANSICHT', 'Vor dem Kliff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 28, 112, 'WARTE', 'uhr 06.12. 10:30', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 29, 112, 'RAFFER', '120', '6.12.: die See steht am Kliffuß' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 30, 112, 'ANSICHT', 'Abbruch', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 31, 122, 'FOTO', 'Dezember: die Hohlkehle', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 32, 126, 'RAFFER', '1', 'die Wand bricht' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 33, 126, 'ABBRUCH', '6090335', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 34, 128, 'WARTE', 'abbruch', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 35, 134, 'FOTO', 'Dezember: der Abbruch', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 36, 136, 'WARTE', 'ruhe', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 37, 136, 'RAFFER', '3600', 'Winter: die Fluten tragen am Sturzkegel' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 38, 136, 'ANSICHT', 'Kliffkante', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 39, 150, 'ZEIT', '10.03.2027 10:00', 'März: die Vorspülung legt Sand vor das Kliff' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 40, 150, 'LAGE', 'Westwind', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 41, 150, 'VORSPUELUNG', NULL, NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 42, 150, 'RAFFER', '3600', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 43, 150, 'ANSICHT', 'Vorspülung', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 44, 153, 'FOTO', 'März: das Spülfeld wächst', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 45, 162, 'WARTE', 'vorspuelung', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 46, 162, 'FOTO', 'März: der neue Strand vor dem Kliff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 47, 168, 'RAFFER', '3600', 'die Brandung formt den Sand um' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 48, 168, 'ANSICHT', 'Abbruch', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 49, 190, 'FOTO', 'April: der Sand vor dem Kliff, von der Brandung geformt', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 50, 196, 'ZEIT', '15.06.2027 16:00', 'Juni 2027: ein Jahr ist um, die Strandkörbe stehen wieder' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 51, 196, 'LAGE', 'Sommerabend', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 52, 196, 'RAFFER', '60', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 53, 196, 'ANSICHT', 'Strandkörbe', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 54, 208, 'FOTO', 'Juni 2027: Strandkörbe auf dem vorgespülten Strand', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 55, 214, 'FAHRT', 'Drohne', 'das Kliff ein Jahr später' FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 56, 254, 'FOTO', 'Drohne: das Kliff ein Jahr später', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 57, 264, 'BLATT', 'Ein Jahr am Kliff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 58, 264, 'RAFFER', '1', NULL FROM SY_REGIEBUCH WHERE NAME = 'Ein Jahr am Kliff' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_REGIEBUCH t USING (SELECT 'Mit der Welle' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN NOT MATCHED THEN INSERT (NAME, LAENGE_S, TEXT, HERKUNFT) VALUES ('Mit der Welle', 90, '-', 'MITGELIEFERT');
UPDATE SY_REGIEBUCH SET BESCHREIBUNG = '3. Oktober 2026, ablandiger Wind und Dünung aus West, die Brecher stürzen: auf dem Kamm einer Welle vom Riff bis an den Strand, durch den Hohlraum unter der Lippe, kurz unter Wasser, mit der Walze und dem Schwall den Strand hinauf', DATEI = 'welle.txt', SZENE_MS = 1791028800000, SZENE_ZEIT = FROM_TZ(TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(1791028800000 / 1000, 'SECOND'), 'UTC') AT TIME ZONE 'Europe/Berlin', ZEITRAFFER = 1, LAENGE_S = 90, FOTOS = 7, WARTEN = 5, BLATT = 1,
  TEXT = TO_CLOB('# Sylt · Regiebuch (Phase 15), Befehle wie in abend.txt. Gestaltet [G]. Die Kür: die Kamera reitet auf einer Welle der' || CHR(10) || '# Nahbrandung, vom Riff bis zum Auflauf; bei einem Sturzbrecher durch den Hohlraum unter der Lippe und kurz unter Wasser.' || CHR(10) || 'Regiebuch: Mit der Welle' || CHR(10) || 'Beschreibung: 3. Oktober 2026, ablandiger Wind und Dünung aus West, die Brecher stürzen: auf dem Kamm einer Welle vom Riff bis an den Strand, durch den Hohlraum unter der Lippe, kurz unter Wasser, mit der Walze und dem Schwall den Strand hinauf' || CHR(10) || 'Länge: 1:30' || CHR(10) || '0:00 zeit 03.10.2026 14:00 | 3. Oktober, 14 Uhr: ablandiger Wind, Dünung aus West, die Brecher stürzen' || CHR(10) || '0:00 lage Ablandig' || CHR(10) || '0:00 raffer 1' || CHR(10) || '0:00 ansicht Spülsaum' || CHR(10) || '0:08 foto Am Spülsaum: Dünung bei ablandigem Wind' || CHR(10) || '0:12 warte serie' || CHR(10) || '0:12 sage eine Serie großer Wellen läuft an' || CHR(10) || '0:12 fahrt Welle' || CHR(10) || '0:20 foto Auf dem Kamm über dem Riff' || CHR(10) || '0:22 warte welle' || CHR(10) || '0:22 foto Der Schwall läuft aus' || CHR(10) || '0:28 ansicht Spülsaum | noch einmal, mit der nächsten Serie' || CHR(10) || '0:34 warte serie' || CHR(10) || '0:34 fahrt Welle' || CHR(10) || '0:40 foto M')
    || TO_CLOB('it der Welle' || CHR(10) || '0:42 warte unter' || CHR(10) || '0:42 foto Die Welle von unten: in der Rinne läuft sie über die Kamera | unter der Welle durch' || CHR(10) || '0:44 warte welle' || CHR(10) || '0:44 foto Am Ende des Auflaufs' || CHR(10) || '0:52 ansicht Kliffkante | von oben: die Serien kommen alle paar Minuten' || CHR(10) || '1:10 foto Kliffkante: die Brandung von oben' || CHR(10) || '1:20 blatt Mit der Welle' || CHR(10) || '1:20 raffer 1' || CHR(10) || '')
  WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
DELETE FROM SY_STICHWORT WHERE REGIEBUCH_ID IN (SELECT ID FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 1, 0, 'ZEIT', '03.10.2026 14:00', '3. Oktober, 14 Uhr: ablandiger Wind, Dünung aus West, die Brecher stürzen' FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 2, 0, 'LAGE', 'Ablandig', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 3, 0, 'RAFFER', '1', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 4, 0, 'ANSICHT', 'Spülsaum', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 5, 8, 'FOTO', 'Am Spülsaum: Dünung bei ablandigem Wind', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 6, 12, 'WARTE', 'serie', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 7, 12, 'SAGE', 'eine Serie großer Wellen läuft an', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 8, 12, 'FAHRT', 'Welle', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 9, 20, 'FOTO', 'Auf dem Kamm über dem Riff', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 10, 22, 'WARTE', 'welle', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 11, 22, 'FOTO', 'Der Schwall läuft aus', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 12, 28, 'ANSICHT', 'Spülsaum', 'noch einmal, mit der nächsten Serie' FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 13, 34, 'WARTE', 'serie', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 14, 34, 'FAHRT', 'Welle', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 15, 40, 'FOTO', 'Mit der Welle', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 16, 42, 'WARTE', 'unter', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 17, 42, 'FOTO', 'Die Welle von unten: in der Rinne läuft sie über die Kamera', 'unter der Welle durch' FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 18, 44, 'WARTE', 'welle', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 19, 44, 'FOTO', 'Am Ende des Auflaufs', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 20, 52, 'ANSICHT', 'Kliffkante', 'von oben: die Serien kommen alle paar Minuten' FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 21, 70, 'FOTO', 'Kliffkante: die Brandung von oben', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 22, 80, 'BLATT', 'Mit der Welle', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_STICHWORT (REGIEBUCH_ID, NR, SEKUNDE, BEFEHL, ARGUMENTE, TEXT) SELECT ID, 23, 80, 'RAFFER', '1', NULL FROM SY_REGIEBUCH WHERE NAME = 'Mit der Welle' AND HERKUNFT = 'MITGELIEFERT';

-- --------------------------------------------------- Ansichten (Tasten 1–9, Blick, Vorlagen der Türme)
MERGE INTO SY_ANSICHT t USING (SELECT 'Am Kliff' AS NAME, 'FEST' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = 1, t.KAM_X = -1051.8, t.KAM_Z = 1870, t.KAM_GIER_GRAD = 18, t.KAM_NICK_GRAD = -10, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 1.7, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456548.2, 6089300, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Am Kliff', 'FEST', 1, -1051.8, 1870, 18, -10, 55, 1.7, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456548.2, 6089300, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Treppe' AS NAME, 'FEST' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = 2, t.KAM_X = -781.4, t.KAM_Z = 1270, t.KAM_GIER_GRAD = 192, t.KAM_NICK_GRAD = 8, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 1.7, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456818.6, 6089900, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Treppe', 'FEST', 2, -781.4, 1270, 192, 8, 55, 1.7, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456818.6, 6089900, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Kliffkante' AS NAME, 'FEST' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = 3, t.KAM_X = -852, t.KAM_Z = 1670, t.KAM_GIER_GRAD = 258, t.KAM_NICK_GRAD = 24, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 1.7, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456748, 6089500, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Kliffkante', 'FEST', 3, -852, 1670, 258, 24, 55, 1.7, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456748, 6089500, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Spülsaum' AS NAME, 'FEST' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = 4, t.KAM_X = -1210, t.KAM_Z = 2170, t.KAM_GIER_GRAD = 292, t.KAM_NICK_GRAD = 2, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 0.6, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456390, 6089000, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Spülsaum', 'FEST', 4, -1210, 2170, 292, 2, 55, 0.6, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456390, 6089000, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Quermarkenfeuer' AS NAME, 'FEST' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = 5, t.KAM_X = -82, t.KAM_Z = 81, t.KAM_GIER_GRAD = 45, t.KAM_NICK_GRAD = -6, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 1.7, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457518, 6091089, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Quermarkenfeuer', 'FEST', 5, -82, 81, 45, -6, 55, 1.7, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457518, 6091089, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Kampener Leuchtturm' AS NAME, 'FEST' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = 6, t.KAM_X = -90, t.KAM_Z = 2163, t.KAM_GIER_GRAD = 90, t.KAM_NICK_GRAD = -7, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 1.7, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457510, 6089007, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Kampener Leuchtturm', 'FEST', 6, -90, 2163, 90, -7, 55, 1.7, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457510, 6089007, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Uwe-Düne' AS NAME, 'FEST' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = 7, t.KAM_X = -467, t.KAM_Z = 1266, t.KAM_GIER_GRAD = 270, t.KAM_NICK_GRAD = 6, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 1.7, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457133, 6089904, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Uwe-Düne', 'FEST', 7, -467, 1266, 270, 6, 55, 1.7, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457133, 6089904, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Bohlenweg' AS NAME, 'FEST' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = 8, t.KAM_X = 163, t.KAM_Z = 186, t.KAM_GIER_GRAD = 320, t.KAM_NICK_GRAD = -2, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 1.7, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457763, 6090984, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Bohlenweg', 'FEST', 8, 163, 186, 320, -2, 55, 1.7, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457763, 6090984, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Vogelauge' AS NAME, 'FEST' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = 9, t.KAM_X = -4130.2, t.KAM_Z = 1970, t.KAM_GIER_GRAD = 74, t.KAM_NICK_GRAD = 11, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 600, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(453469.8, 6089200, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Vogelauge', 'FEST', 9, -4130.2, 1970, 74, 11, 55, 600, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(453469.8, 6089200, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Sandsturm am Strand' AS NAME, 'BLICK' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = NULL, t.KAM_X = -1067.8, t.KAM_Z = 1870, t.KAM_GIER_GRAD = 25, t.KAM_NICK_GRAD = -3, t.OEFFNUNG_GRAD = 55, t.AUGE_UEBER_GRUND_M = 1.6, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456532.2, 6089300, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Sandsturm am Strand', 'BLICK', NULL, -1067.8, 1870, 25, -3, 55, 1.6, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(456532.2, 6089300, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Leuchturm1' AS NAME, 'VORLAGE' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = NULL, t.KAM_X = 9.21, t.KAM_Z = -27.18, t.KAM_GIER_GRAD = 188, t.KAM_NICK_GRAD = -6, t.OEFFNUNG_GRAD = 50, t.AUGE_UEBER_GRUND_M = 1.5, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457609.21, 6091197.18, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Leuchturm1', 'VORLAGE', NULL, 9.21, -27.18, 188, -6, 50, 1.5, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457609.21, 6091197.18, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Leuchturm2' AS NAME, 'VORLAGE' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = NULL, t.KAM_X = -14.2, t.KAM_Z = 8.05, t.KAM_GIER_GRAD = 46, t.KAM_NICK_GRAD = -9, t.OEFFNUNG_GRAD = 45, t.AUGE_UEBER_GRUND_M = 1.5, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457585.8, 6091161.95, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Leuchturm2', 'VORLAGE', NULL, -14.2, 8.05, 46, -9, 45, 1.5, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457585.8, 6091161.95, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Leuchturm3' AS NAME, 'VORLAGE' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = NULL, t.KAM_X = 1.88, t.KAM_Z = 27.98, t.KAM_GIER_GRAD = 2, t.KAM_NICK_GRAD = -5, t.OEFFNUNG_GRAD = 42, t.AUGE_UEBER_GRUND_M = 1.6, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457601.88, 6091142.02, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Leuchturm3', 'VORLAGE', NULL, 1.88, 27.98, 2, -5, 42, 1.6, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457601.88, 6091142.02, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Leuchturm4' AS NAME, 'VORLAGE' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = NULL, t.KAM_X = 51, t.KAM_Z = 40, t.KAM_GIER_GRAD = 312.51, t.KAM_NICK_GRAD = -6, t.OEFFNUNG_GRAD = 45, t.AUGE_UEBER_GRUND_M = 1.6, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457651, 6091130, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Leuchturm4', 'VORLAGE', NULL, 51, 40, 312.51, -6, 45, 1.6, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457651, 6091130, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Kampen1' AS NAME, 'VORLAGE' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = NULL, t.KAM_X = -467, t.KAM_Z = 1266, t.KAM_GIER_GRAD = 144.62, t.KAM_NICK_GRAD = 0.6, t.OEFFNUNG_GRAD = 2.8, t.AUGE_UEBER_GRUND_M = 1.7, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457133, 6089904, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Kampen1', 'VORLAGE', NULL, -467, 1266, 144.62, 0.6, 2.8, 1.7, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457133, 6089904, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Kampen2' AS NAME, 'VORLAGE' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = NULL, t.KAM_X = -408.51, t.KAM_Z = 1473.56, t.KAM_GIER_GRAD = 140, t.KAM_NICK_GRAD = 0.3, t.OEFFNUNG_GRAD = 5.7, t.AUGE_UEBER_GRUND_M = 1.7, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457191.49, 6089696.44, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Kampen2', 'VORLAGE', NULL, -408.51, 1473.56, 140, 0.3, 5.7, 1.7, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457191.49, 6089696.44, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Kampen3' AS NAME, 'VORLAGE' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = NULL, t.KAM_X = 570, t.KAM_Z = 2855.82, t.KAM_GIER_GRAD = 330, t.KAM_NICK_GRAD = -1.2, t.OEFFNUNG_GRAD = 6, t.AUGE_UEBER_GRUND_M = 1.6, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(458170, 6088314.18, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Kampen3', 'VORLAGE', NULL, 570, 2855.82, 330, -1.2, 6, 1.6, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(458170, 6088314.18, NULL), NULL, NULL), 'MITGELIEFERT');
MERGE INTO SY_ANSICHT t USING (SELECT 'Kampen4' AS NAME, 'VORLAGE' AS ART FROM DUAL) q ON (t.NAME = q.NAME AND t.ART = q.ART)
WHEN MATCHED THEN UPDATE SET t.TASTE = NULL, t.KAM_X = 55.32, t.KAM_Z = 2243.3, t.KAM_GIER_GRAD = 55, t.KAM_NICK_GRAD = 36, t.OEFFNUNG_GRAD = 40, t.AUGE_UEBER_GRUND_M = 95, t.LAGE = MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457655.32, 6088926.7, NULL), NULL, NULL) WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, TASTE, KAM_X, KAM_Z, KAM_GIER_GRAD, KAM_NICK_GRAD, OEFFNUNG_GRAD, AUGE_UEBER_GRUND_M, LAGE, HERKUNFT)
  VALUES ('Kampen4', 'VORLAGE', NULL, 55.32, 2243.3, 55, 36, 40, 95, MDSYS.SDO_GEOMETRY(2001, 25832, MDSYS.SDO_POINT_TYPE(457655.32, 6088926.7, NULL), NULL, NULL), 'MITGELIEFERT');

-- --------------------------------------------------------------------------------------- Kamerafahrten
MERGE INTO SY_FAHRT t USING (SELECT 'Treppe' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'TREPPE', t.DAUER_S = 30, t.LAENGE_M = NULL, t.BESCHREIBUNG = 'Die Treppe am Kliff hinauf, oben der Blick nach Westsüdwest über die See' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, DAUER_S, LAENGE_M, BESCHREIBUNG, HERKUNFT)
  VALUES ('Treppe', 'TREPPE', 30, NULL, 'Die Treppe am Kliff hinauf, oben der Blick nach Westsüdwest über die See', 'MITGELIEFERT');
MERGE INTO SY_FAHRT t USING (SELECT 'Drohne' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'DROHNE', t.DAUER_S = 40, t.LAENGE_M = 2000, t.BESCHREIBUNG = '130 m vor dem Fuß, 45 m über dem Pegel von Süd nach Nord am Kliff entlang' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, DAUER_S, LAENGE_M, BESCHREIBUNG, HERKUNFT)
  VALUES ('Drohne', 'DROHNE', 40, 2000, '130 m vor dem Fuß, 45 m über dem Pegel von Süd nach Nord am Kliff entlang', 'MITGELIEFERT');
MERGE INTO SY_FAHRT t USING (SELECT 'Möwe' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'MOEWE', t.DAUER_S = NULL, t.LAENGE_M = NULL, t.BESCHREIBUNG = 'Mitfahrt 5 m hinter der nächsten fliegenden Möwe' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, DAUER_S, LAENGE_M, BESCHREIBUNG, HERKUNFT)
  VALUES ('Möwe', 'MOEWE', NULL, NULL, 'Mitfahrt 5 m hinter der nächsten fliegenden Möwe', 'MITGELIEFERT');
MERGE INTO SY_FAHRT t USING (SELECT 'Welle' AS NAME FROM DUAL) q ON (t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.ART = 'WELLE', t.DAUER_S = NULL, t.LAENGE_M = NULL, t.BESCHREIBUNG = 'Mit der höchsten Welle vom Kamm bis in den Schwall, in der Rinne unter Wasser' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (NAME, ART, DAUER_S, LAENGE_M, BESCHREIBUNG, HERKUNFT)
  VALUES ('Welle', 'WELLE', NULL, NULL, 'Mit der höchsten Welle vom Kamm bis in den Schwall, in der Rinne unter Wasser', 'MITGELIEFERT');

-- ------------------------------------------------------------------------ Presets der Packages (F43 A)
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'breeze' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus com.dan.surf' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'breeze', 'Preset breeze() aus com.dan.surf', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '1.100189' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '285' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'calm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus com.dan.surf' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'calm', 'Preset calm() aus com.dan.surf', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '0.3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '285' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'gale' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus com.dan.surf' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'gale', 'Preset gale() aus com.dan.surf', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '8.847591' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '285' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'ostsee' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset ostsee() aus com.dan.surf' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'ostsee', 'Preset ostsee() aus com.dan.surf', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'ostsee' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'ostsee' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'ostsee' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '0.6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'ostsee' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '4' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'ostsee' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'ostsee' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'ostsee' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'ostsee' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'sockel' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset sockel() aus com.dan.surf' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'sockel', 'Preset sockel() aus com.dan.surf', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sockel' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sockel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sockel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sockel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sockel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sockel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sockel' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sockel' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'storm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus com.dan.surf' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'storm', 'Preset storm() aus com.dan.surf', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '285' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'sylt' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset sylt() aus com.dan.surf' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'sylt', 'Preset sylt() aus com.dan.surf', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '285' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.dune' AS PAKET, 'Dune' AS KLASSE, 'breeze' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus com.dan.dune' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.dune', 'Dune', 'breeze', 'Preset breeze() aus com.dan.dune', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'grain', 'DOUBLE', '0.0003' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoSand', 'DOUBLE', '2650' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoAir', 'DOUBLE', '1.225' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldA', 'DOUBLE', '0.1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldC', 'DOUBLE', '1.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'z0', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'refHeight', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachBeta', 'DOUBLE', '90' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachSigma', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachM', 'DOUBLE', '0.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6.7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'satFactor', 'DOUBLE', '2.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bulkDensity', 'DOUBLE', '1600' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reposeDeg', 'DOUBLE', '34' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyA', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyB', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowDeg', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launch', 'DOUBLE', '1.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launchDeg', 'DOUBLE', '40' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesFraction', 'DOUBLE', '0.03' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesGrain', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.22' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerLength', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerWidth', 'DOUBLE', '0.45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleLambda0', 'DOUBLE', '0.075' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleGrowth', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleIndex', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFormTime', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleReptation', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFlatBed', 'DOUBLE', '3.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eventRate', 'DOUBLE', '0.000002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'boundMoisture', 'DOUBLE', '0.06' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.dune' AS PAKET, 'Dune' AS KLASSE, 'calm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus com.dan.dune' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.dune', 'Dune', 'calm', 'Preset calm() aus com.dan.dune', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'grain', 'DOUBLE', '0.0003' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoSand', 'DOUBLE', '2650' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoAir', 'DOUBLE', '1.225' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldA', 'DOUBLE', '0.1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldC', 'DOUBLE', '1.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'z0', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'refHeight', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachBeta', 'DOUBLE', '90' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachSigma', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachM', 'DOUBLE', '0.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'satFactor', 'DOUBLE', '2.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bulkDensity', 'DOUBLE', '1600' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reposeDeg', 'DOUBLE', '34' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyA', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyB', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowDeg', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launch', 'DOUBLE', '1.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launchDeg', 'DOUBLE', '40' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesFraction', 'DOUBLE', '0.03' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesGrain', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.22' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerLength', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerWidth', 'DOUBLE', '0.45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleLambda0', 'DOUBLE', '0.075' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleGrowth', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleIndex', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFormTime', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleReptation', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFlatBed', 'DOUBLE', '3.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eventRate', 'DOUBLE', '0.000002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'boundMoisture', 'DOUBLE', '0.06' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.dune' AS PAKET, 'Dune' AS KLASSE, 'gale' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus com.dan.dune' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.dune', 'Dune', 'gale', 'Preset gale() aus com.dan.dune', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'grain', 'DOUBLE', '0.0003' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoSand', 'DOUBLE', '2650' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoAir', 'DOUBLE', '1.225' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldA', 'DOUBLE', '0.1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldC', 'DOUBLE', '1.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'z0', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'refHeight', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachBeta', 'DOUBLE', '90' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachSigma', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachM', 'DOUBLE', '0.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '19' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'satFactor', 'DOUBLE', '2.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bulkDensity', 'DOUBLE', '1600' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reposeDeg', 'DOUBLE', '34' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyA', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyB', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowDeg', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launch', 'DOUBLE', '1.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launchDeg', 'DOUBLE', '40' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesFraction', 'DOUBLE', '0.03' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesGrain', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.22' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerLength', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerWidth', 'DOUBLE', '0.45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleLambda0', 'DOUBLE', '0.075' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleGrowth', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleIndex', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFormTime', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleReptation', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFlatBed', 'DOUBLE', '3.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eventRate', 'DOUBLE', '0.000002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'boundMoisture', 'DOUBLE', '0.06' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.dune' AS PAKET, 'Dune' AS KLASSE, 'storm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus com.dan.dune' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.dune', 'Dune', 'storm', 'Preset storm() aus com.dan.dune', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'grain', 'DOUBLE', '0.0003' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoSand', 'DOUBLE', '2650' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoAir', 'DOUBLE', '1.225' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldA', 'DOUBLE', '0.1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldC', 'DOUBLE', '1.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'z0', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'refHeight', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachBeta', 'DOUBLE', '90' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachSigma', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachM', 'DOUBLE', '0.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'satFactor', 'DOUBLE', '2.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bulkDensity', 'DOUBLE', '1600' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reposeDeg', 'DOUBLE', '34' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyA', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyB', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowDeg', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launch', 'DOUBLE', '1.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launchDeg', 'DOUBLE', '40' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesFraction', 'DOUBLE', '0.03' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesGrain', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.22' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerLength', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerWidth', 'DOUBLE', '0.45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleLambda0', 'DOUBLE', '0.075' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleGrowth', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleIndex', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFormTime', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleReptation', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFlatBed', 'DOUBLE', '3.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eventRate', 'DOUBLE', '0.000002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'boundMoisture', 'DOUBLE', '0.06' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.dune' AS PAKET, 'Dune' AS KLASSE, 'sylt' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset sylt() aus com.dan.dune' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.dune', 'Dune', 'sylt', 'Preset sylt() aus com.dan.dune', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'grain', 'DOUBLE', '0.0003' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoSand', 'DOUBLE', '2650' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoAir', 'DOUBLE', '1.225' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldA', 'DOUBLE', '0.1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldC', 'DOUBLE', '1.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'z0', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'refHeight', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachBeta', 'DOUBLE', '90' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachSigma', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachM', 'DOUBLE', '0.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'satFactor', 'DOUBLE', '2.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bulkDensity', 'DOUBLE', '1600' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reposeDeg', 'DOUBLE', '34' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyA', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyB', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowDeg', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launch', 'DOUBLE', '1.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launchDeg', 'DOUBLE', '40' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesFraction', 'DOUBLE', '0.03' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesGrain', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.22' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerLength', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerWidth', 'DOUBLE', '0.45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleLambda0', 'DOUBLE', '0.075' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleGrowth', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleIndex', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFormTime', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleReptation', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFlatBed', 'DOUBLE', '3.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eventRate', 'DOUBLE', '0.000002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'boundMoisture', 'DOUBLE', '0.06' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'sylt' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.ocean' AS PAKET, 'Ocean' AS KLASSE, 'breeze' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus com.dan.ocean' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.ocean', 'Ocean', 'breeze', 'Preset breeze() aus com.dan.ocean', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6.7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetch', 'DOUBLE', '150000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'depth', 'DOUBLE', '10000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'choppiness', 'DOUBLE', '0.85' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'heightOverride', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetchLimited', 'BOOLEAN', 'false' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellHeight', 'DOUBLE', '0.48' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellPeriod', 'DOUBLE', '9.4' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellFromDeg', 'DOUBLE', '290' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamDecay', 'DOUBLE', '2.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamAmount', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsLow', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsHigh', 'DOUBLE', '1.6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.ocean' AS PAKET, 'Ocean' AS KLASSE, 'calm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus com.dan.ocean' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.ocean', 'Ocean', 'calm', 'Preset calm() aus com.dan.ocean', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetch', 'DOUBLE', '150000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'depth', 'DOUBLE', '10000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'choppiness', 'DOUBLE', '0.85' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'heightOverride', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetchLimited', 'BOOLEAN', 'false' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellHeight', 'DOUBLE', '0.318' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellPeriod', 'DOUBLE', '8.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellFromDeg', 'DOUBLE', '290' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamDecay', 'DOUBLE', '2.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamAmount', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsLow', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsHigh', 'DOUBLE', '1.6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.ocean' AS PAKET, 'Ocean' AS KLASSE, 'gale' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus com.dan.ocean' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.ocean', 'Ocean', 'gale', 'Preset gale() aus com.dan.ocean', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '19' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetch', 'DOUBLE', '150000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'depth', 'DOUBLE', '10000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'choppiness', 'DOUBLE', '0.85' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'heightOverride', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetchLimited', 'BOOLEAN', 'false' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellHeight', 'DOUBLE', '1.29' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellPeriod', 'DOUBLE', '10.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellFromDeg', 'DOUBLE', '290' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamDecay', 'DOUBLE', '2.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamAmount', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsLow', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsHigh', 'DOUBLE', '1.6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.ocean' AS PAKET, 'Ocean' AS KLASSE, 'storm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus com.dan.ocean' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.ocean', 'Ocean', 'storm', 'Preset storm() aus com.dan.ocean', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetch', 'DOUBLE', '150000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'depth', 'DOUBLE', '10000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'choppiness', 'DOUBLE', '0.85' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'heightOverride', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'fetchLimited', 'BOOLEAN', 'false' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellHeight', 'DOUBLE', '1.92' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellPeriod', 'DOUBLE', '11.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'swellFromDeg', 'DOUBLE', '290' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamDecay', 'DOUBLE', '2.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'foamAmount', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsLow', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'ringHsHigh', 'DOUBLE', '1.6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.ocean' AND KLASSE = 'Ocean' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.cloud' AS PAKET, 'Clouds' AS KLASSE, 'fair' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset fair() aus com.dan.cloud' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.cloud', 'Clouds', 'fair', 'Preset fair() aus com.dan.cloud', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'oktas', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'type', 'ENUM', 'CUMULUS' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'highCover', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'precipitation', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '250' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'evolve', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lens', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lensFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxDistance', 'DOUBLE', '60000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'baseHeight', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowHeight', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'fair' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.cloud' AS PAKET, 'Clouds' AS KLASSE, 'overcast' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset overcast() aus com.dan.cloud' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.cloud', 'Clouds', 'overcast', 'Preset overcast() aus com.dan.cloud', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'oktas', 'DOUBLE', '8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'type', 'ENUM', 'STRATOCUMULUS' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'highCover', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'precipitation', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '250' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'evolve', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lens', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lensFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxDistance', 'DOUBLE', '60000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'baseHeight', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowHeight', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'overcast' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.cloud' AS PAKET, 'Clouds' AS KLASSE, 'storm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus com.dan.cloud' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.cloud', 'Clouds', 'storm', 'Preset storm() aus com.dan.cloud', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'oktas', 'DOUBLE', '6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'type', 'ENUM', 'CUMULONIMBUS' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'highCover', 'DOUBLE', '0.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'precipitation', 'DOUBLE', '0.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '22' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '250' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'evolve', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lens', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'lensFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxDistance', 'DOUBLE', '60000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'baseHeight', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowHeight', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.cloud' AND KLASSE = 'Clouds' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'breeze' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus com.dan.rain' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'breeze', 'Preset breeze() aus com.dan.rain', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '9.3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'calm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus com.dan.rain' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'calm', 'Preset calm() aus com.dan.rain', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '0.3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.13' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'cloudburst' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset cloudburst() aus com.dan.rain' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'cloudburst', 'Preset cloudburst() aus com.dan.rain', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '60' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'cloudburst' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'drizzle' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset drizzle() aus com.dan.rain' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'drizzle', 'Preset drizzle() aus com.dan.rain', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '0.3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'drizzle' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'gale' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus com.dan.rain' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'gale', 'Preset gale() aus com.dan.rain', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '18.9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.34' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'heavy' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset heavy() aus com.dan.rain' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'heavy', 'Preset heavy() aus com.dan.rain', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'heavy' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'light' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset light() aus com.dan.rain' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'light', 'Preset light() aus com.dan.rain', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '1.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'light' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'moderate' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset moderate() aus com.dan.rain' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'moderate', 'Preset moderate() aus com.dan.rain', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'moderate' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.rain' AS PAKET, 'Rain' AS KLASSE, 'storm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus com.dan.rain' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.rain', 'Rain', 'storm', 'Preset storm() aus com.dan.rain', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rate', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.4' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gust', 'DOUBLE', '0.4' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1885' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeY', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eyeZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.rain' AND KLASSE = 'Rain' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'banks' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset banks() aus com.dan.fog' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'banks', 'Preset banks() aus com.dan.fog', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '250' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '55' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'banks' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'breeze' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus com.dan.fog' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'breeze', 'Preset breeze() aus com.dan.fog', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '0.55' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '200' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.4' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6.7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'calm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus com.dan.fog' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'calm', 'Preset calm() aus com.dan.fog', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '200' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '50' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'dense' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset dense() aus com.dan.fog' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'dense', 'Preset dense() aus com.dan.fog', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '70' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '90' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'dense' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'gale' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus com.dan.fog' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'gale', 'Preset gale() aus com.dan.fog', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '200' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.4' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '19' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'haze' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset haze() aus com.dan.fog' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'haze', 'Preset haze() aus com.dan.fog', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '3000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '160' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'haze' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'shallow' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset shallow() aus com.dan.fog' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'shallow', 'Preset shallow() aus com.dan.fog', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '120' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'shallow' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.fog' AS PAKET, 'Fog' AS KLASSE, 'storm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus com.dan.fog' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.fog', 'Fog', 'storm', 'Preset storm() aus com.dan.fog', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'amount', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'visibilityM', 'DOUBLE', '200' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'coverage', 'DOUBLE', '0.4' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'top', 'DOUBLE', '35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'base', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'softness', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bankSize', 'DOUBLE', '1800' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wisps', 'DOUBLE', '0.35' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'signalBelowM', 'DOUBLE', '3704' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hazeVisibilityM', 'DOUBLE', '30000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.fog' AND KLASSE = 'Fog' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.lightning' AS PAKET, 'Lightning' AS KLASSE, 'breeze' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus com.dan.lightning' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.lightning', 'Lightning', 'breeze', 'Preset breeze() aus com.dan.lightning', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'perMinute', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cgFraction', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'spiderFraction', 'DOUBLE', '0.18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cx', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cz', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'radius', 'DOUBLE', '12000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '1200' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'chargeAbove', 'DOUBLE', '2500' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'groundLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerY', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'leaderSpeed', 'DOUBLE', '200000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sheetDistance', 'DOUBLE', '28000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'candelaPerMetre', 'DOUBLE', '5000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxBolts', 'INT', '6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.lightning' AS PAKET, 'Lightning' AS KLASSE, 'calm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus com.dan.lightning' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.lightning', 'Lightning', 'calm', 'Preset calm() aus com.dan.lightning', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'perMinute', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cgFraction', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'spiderFraction', 'DOUBLE', '0.18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cx', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cz', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'radius', 'DOUBLE', '12000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '1200' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'chargeAbove', 'DOUBLE', '2500' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'groundLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerY', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'leaderSpeed', 'DOUBLE', '200000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sheetDistance', 'DOUBLE', '28000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'candelaPerMetre', 'DOUBLE', '5000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxBolts', 'INT', '6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.lightning' AS PAKET, 'Lightning' AS KLASSE, 'gale' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus com.dan.lightning' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.lightning', 'Lightning', 'gale', 'Preset gale() aus com.dan.lightning', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'perMinute', 'DOUBLE', '5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cgFraction', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'spiderFraction', 'DOUBLE', '0.18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cx', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cz', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'radius', 'DOUBLE', '12000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '1200' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'chargeAbove', 'DOUBLE', '2500' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'groundLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerY', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'leaderSpeed', 'DOUBLE', '200000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sheetDistance', 'DOUBLE', '28000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'candelaPerMetre', 'DOUBLE', '5000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxBolts', 'INT', '6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.lightning' AS PAKET, 'Lightning' AS KLASSE, 'storm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus com.dan.lightning' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.lightning', 'Lightning', 'storm', 'Preset storm() aus com.dan.lightning', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'perMinute', 'DOUBLE', '8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cgFraction', 'DOUBLE', '0.28' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'spiderFraction', 'DOUBLE', '0.18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cx', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cz', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'radius', 'DOUBLE', '12000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cloudBase', 'DOUBLE', '1200' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'chargeAbove', 'DOUBLE', '2500' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'groundLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerY', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'listenerZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'leaderSpeed', 'DOUBLE', '200000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sheetDistance', 'DOUBLE', '28000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'candelaPerMetre', 'DOUBLE', '5000' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxBolts', 'INT', '6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.lightning' AND KLASSE = 'Lightning' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.soundscape' AS PAKET, 'Soundscape' AS KLASSE, 'breeze' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset breeze() aus com.dan.soundscape' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.soundscape', 'Soundscape', 'breeze', 'Preset breeze() aus com.dan.soundscape', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'volume', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGain', 'BOOLEAN', 'true' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGainMaxDb', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windGainDb', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6.7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wavePeriod', 'DOUBLE', '5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfRadius', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfStrength', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '11' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earY', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earYawDeg', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'inside', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'glassDrops', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roofDrops', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reedNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'thatchNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mudCrackle', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windCarry', 'BOOLEAN', 'false' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'obstruction', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roughness', 'DOUBLE', '0.0002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'walpurgis', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'walpurgisFromDeg', 'DOUBLE', '125' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfLineLevel', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfLineGainDb', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sandFlux', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'marramNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'breeze' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.soundscape' AS PAKET, 'Soundscape' AS KLASSE, 'calm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset calm() aus com.dan.soundscape' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.soundscape', 'Soundscape', 'calm', 'Preset calm() aus com.dan.soundscape', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'volume', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGain', 'BOOLEAN', 'true' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGainMaxDb', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windGainDb', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '0.9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '0.1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wavePeriod', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfRadius', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfStrength', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '11' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earY', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earYawDeg', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'inside', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'glassDrops', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roofDrops', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reedNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'thatchNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mudCrackle', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windCarry', 'BOOLEAN', 'false' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'obstruction', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roughness', 'DOUBLE', '0.0002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'walpurgis', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'walpurgisFromDeg', 'DOUBLE', '125' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfLineLevel', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfLineGainDb', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sandFlux', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'marramNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'calm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.soundscape' AS PAKET, 'Soundscape' AS KLASSE, 'gale' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset gale() aus com.dan.soundscape' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.soundscape', 'Soundscape', 'gale', 'Preset gale() aus com.dan.soundscape', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'volume', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGain', 'BOOLEAN', 'true' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGainMaxDb', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windGainDb', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '18.9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '5.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wavePeriod', 'DOUBLE', '9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfRadius', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfStrength', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '11' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earY', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earYawDeg', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'inside', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'glassDrops', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roofDrops', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reedNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'thatchNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mudCrackle', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windCarry', 'BOOLEAN', 'false' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'obstruction', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roughness', 'DOUBLE', '0.0002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'walpurgis', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'walpurgisFromDeg', 'DOUBLE', '125' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfLineLevel', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfLineGainDb', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sandFlux', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'marramNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'gale' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.soundscape' AS PAKET, 'Soundscape' AS KLASSE, 'storm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset storm() aus com.dan.soundscape' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.soundscape', 'Soundscape', 'storm', 'Preset storm() aus com.dan.soundscape', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'volume', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGain', 'BOOLEAN', 'true' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'autoGainMaxDb', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windGainDb', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '26.4' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '240' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rainRate', 'DOUBLE', '6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waveHeight', 'DOUBLE', '9' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'wavePeriod', 'DOUBLE', '11' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfRadius', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfStrength', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'waterLevel', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'temperatureC', 'DOUBLE', '11' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earX', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earY', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earZ', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'earYawDeg', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'inside', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'glassDrops', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roofDrops', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reedNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'thatchNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mudCrackle', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windCarry', 'BOOLEAN', 'false' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'obstruction', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'roughness', 'DOUBLE', '0.0002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'walpurgis', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'walpurgisFromDeg', 'DOUBLE', '125' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfLineLevel', 'DOUBLE', 'NaN' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surfLineGainDb', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'sandFlux', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'marramNear', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.soundscape' AND KLASSE = 'Soundscape' AND NAME = 'storm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.tide' AS PAKET, 'Tide' AS KLASSE, 'basin' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset basin() aus com.dan.tide' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.tide', 'Tide', 'basin', 'Preset basin() aus com.dan.tide', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'basin' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'basin' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'basin' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gauge', 'STRING', 'Becken' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'basin' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mean', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'basin' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surge', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'basin' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cellSize', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'basin' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seepage', 'DOUBLE', '0.02' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'basin' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxPuddle', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'basin' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.tide' AS PAKET, 'Tide' AS KLASSE, 'bremerhaven' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset bremerhaven() aus com.dan.tide' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.tide', 'Tide', 'bremerhaven', 'Preset bremerhaven() aus com.dan.tide', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'bremerhaven' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'bremerhaven' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'bremerhaven' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gauge', 'STRING', 'Bremerhaven [G]' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'bremerhaven' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mean', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'bremerhaven' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surge', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'bremerhaven' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cellSize', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'bremerhaven' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seepage', 'DOUBLE', '0.02' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'bremerhaven' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxPuddle', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'bremerhaven' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.tide' AS PAKET, 'Tide' AS KLASSE, 'hever' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset hever() aus com.dan.tide' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.tide', 'Tide', 'hever', 'Preset hever() aus com.dan.tide', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'hever' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'hever' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'hever' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gauge', 'STRING', 'Norderhever [G]' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'hever' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mean', 'DOUBLE', '-0.1861' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'hever' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surge', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'hever' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cellSize', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'hever' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seepage', 'DOUBLE', '0.02' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'hever' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxPuddle', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'hever' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.tide' AS PAKET, 'Tide' AS KLASSE, 'husum' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset husum() aus com.dan.tide' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.tide', 'Tide', 'husum', 'Preset husum() aus com.dan.tide', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'husum' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'husum' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'husum' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gauge', 'STRING', 'Husum' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'husum' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mean', 'DOUBLE', '-0.1861' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'husum' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surge', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'husum' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cellSize', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'husum' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seepage', 'DOUBLE', '0.02' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'husum' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxPuddle', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'husum' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.tide' AS PAKET, 'Tide' AS KLASSE, 'pellworm' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset pellworm() aus com.dan.tide' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.tide', 'Tide', 'pellworm', 'Preset pellworm() aus com.dan.tide', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'pellworm' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'pellworm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'pellworm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gauge', 'STRING', 'Pellworm [G]' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'pellworm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mean', 'DOUBLE', '-0.1861' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'pellworm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surge', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'pellworm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cellSize', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'pellworm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seepage', 'DOUBLE', '0.02' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'pellworm' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxPuddle', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'pellworm' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.tide' AS PAKET, 'Tide' AS KLASSE, 'roterSand' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset roterSand() aus com.dan.tide' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.tide', 'Tide', 'roterSand', 'Preset roterSand() aus com.dan.tide', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'roterSand' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'roterSand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'roterSand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gauge', 'STRING', 'Roter Sand [G]' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'roterSand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mean', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'roterSand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surge', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'roterSand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cellSize', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'roterSand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seepage', 'DOUBLE', '0.02' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'roterSand' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxPuddle', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'roterSand' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.tide' AS PAKET, 'Tide' AS KLASSE, 'westerland' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Preset westerland() aus com.dan.tide' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.tide', 'Tide', 'westerland', 'Preset westerland() aus com.dan.tide', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'westerland' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'westerland' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'MEDIUM' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'westerland' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gauge', 'STRING', 'Westerland [G]' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'westerland' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'mean', 'DOUBLE', '0.0803' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'westerland' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'surge', 'DOUBLE', '0' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'westerland' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'cellSize', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'westerland' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seepage', 'DOUBLE', '0.02' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'westerland' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'maxPuddle', 'DOUBLE', '0.15' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.tide' AND KLASSE = 'Tide' AND NAME = 'westerland' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'Sommerabend' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Die Lage „Sommerabend“: Dünung aus Westnordwest, leichter Wind' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'Sommerabend', 'Die Lage „Sommerabend“: Dünung aus Westnordwest, leichter Wind', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '285' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.dune' AS PAKET, 'Dune' AS KLASSE, 'Sommerabend' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Die Lage „Sommerabend“: Dünung aus Westnordwest, leichter Wind' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.dune', 'Dune', 'Sommerabend', 'Die Lage „Sommerabend“: Dünung aus Westnordwest, leichter Wind', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'grain', 'DOUBLE', '0.0003' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoSand', 'DOUBLE', '2650' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoAir', 'DOUBLE', '1.225' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldA', 'DOUBLE', '0.1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldC', 'DOUBLE', '1.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'z0', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'refHeight', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachBeta', 'DOUBLE', '90' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachSigma', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachM', 'DOUBLE', '0.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'satFactor', 'DOUBLE', '2.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bulkDensity', 'DOUBLE', '1600' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reposeDeg', 'DOUBLE', '34' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyA', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyB', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowDeg', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launch', 'DOUBLE', '1.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launchDeg', 'DOUBLE', '40' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesFraction', 'DOUBLE', '0.03' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesGrain', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.22' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerLength', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerWidth', 'DOUBLE', '0.45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleLambda0', 'DOUBLE', '0.075' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleGrowth', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleIndex', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFormTime', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleReptation', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFlatBed', 'DOUBLE', '3.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eventRate', 'DOUBLE', '0.000002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'boundMoisture', 'DOUBLE', '0.06' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Sommerabend' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'Westwind' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Die Lage „Westwind“: frische Windsee aus West' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'Westwind', 'Die Lage „Westwind“: frische Windsee aus West', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '7' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '270' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.dune' AS PAKET, 'Dune' AS KLASSE, 'Westwind' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Die Lage „Westwind“: frische Windsee aus West' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.dune', 'Dune', 'Westwind', 'Die Lage „Westwind“: frische Windsee aus West', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'grain', 'DOUBLE', '0.0003' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoSand', 'DOUBLE', '2650' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoAir', 'DOUBLE', '1.225' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldA', 'DOUBLE', '0.1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldC', 'DOUBLE', '1.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'z0', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'refHeight', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachBeta', 'DOUBLE', '90' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachSigma', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachM', 'DOUBLE', '0.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '11' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '260' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'satFactor', 'DOUBLE', '2.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bulkDensity', 'DOUBLE', '1600' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reposeDeg', 'DOUBLE', '34' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyA', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyB', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowDeg', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launch', 'DOUBLE', '1.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launchDeg', 'DOUBLE', '40' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesFraction', 'DOUBLE', '0.03' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesGrain', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.22' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerLength', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerWidth', 'DOUBLE', '0.45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleLambda0', 'DOUBLE', '0.075' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleGrowth', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleIndex', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFormTime', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleReptation', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFlatBed', 'DOUBLE', '3.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eventRate', 'DOUBLE', '0.000002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'boundMoisture', 'DOUBLE', '0.06' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Westwind' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.surf' AS PAKET, 'Surf' AS KLASSE, 'Xaver' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Die Sturmflut nach Xaver auf dem Höhepunkt (Nacht zum 6.12.2013)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.surf', 'Surf', 'Xaver', 'Die Sturmflut nach Xaver auf dem Höhepunkt (Nacht zum 6.12.2013)', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'hs', 'DOUBLE', '6' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'tp', 'DOUBLE', '12' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seaFromDeg', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gamma', 'DOUBLE', '0.78' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'slopeBase', 'DOUBLE', '20' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.surf' AND KLASSE = 'Surf' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
MERGE INTO SY_PAKET_PRESET t USING (SELECT 'com.dan.dune' AS PAKET, 'Dune' AS KLASSE, 'Xaver' AS NAME FROM DUAL) q ON (t.PAKET = q.PAKET AND t.KLASSE = q.KLASSE AND t.NAME = q.NAME)
WHEN MATCHED THEN UPDATE SET t.BESCHREIBUNG = 'Die Sturmflut nach Xaver auf dem Höhepunkt (Nacht zum 6.12.2013)' WHERE t.HERKUNFT = 'MITGELIEFERT'
WHEN NOT MATCHED THEN INSERT (PAKET, KLASSE, NAME, BESCHREIBUNG, HERKUNFT)
  VALUES ('com.dan.dune', 'Dune', 'Xaver', 'Die Sturmflut nach Xaver auf dem Höhepunkt (Nacht zum 6.12.2013)', 'MITGELIEFERT');
DELETE FROM SY_PAKET_WERT WHERE PAKET_PRESET_ID IN (SELECT ID FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT');
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'seed', 'INT', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'quality', 'ENUM', 'HIGH' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'grain', 'DOUBLE', '0.0003' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoSand', 'DOUBLE', '2650' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rhoAir', 'DOUBLE', '1.225' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldA', 'DOUBLE', '0.1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bagnoldC', 'DOUBLE', '1.8' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'z0', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'refHeight', 'DOUBLE', '10' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachBeta', 'DOUBLE', '90' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachSigma', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'raupachM', 'DOUBLE', '0.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windSpeed', 'DOUBLE', '27' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'windFromDeg', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'satFactor', 'DOUBLE', '2.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'bulkDensity', 'DOUBLE', '1600' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'reposeDeg', 'DOUBLE', '34' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyA', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'kroyB', 'DOUBLE', '0.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'shadowDeg', 'DOUBLE', '14' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launch', 'DOUBLE', '1.2' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'launchDeg', 'DOUBLE', '40' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesFraction', 'DOUBLE', '0.03' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'finesGrain', 'DOUBLE', '0.0001' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'gustiness', 'DOUBLE', '0.22' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerLength', 'DOUBLE', '3' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'streamerWidth', 'DOUBLE', '0.45' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleLambda0', 'DOUBLE', '0.075' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleGrowth', 'DOUBLE', '1' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleIndex', 'DOUBLE', '18' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFormTime', 'DOUBLE', '300' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleReptation', 'DOUBLE', '0.25' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'rippleFlatBed', 'DOUBLE', '3.5' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'eventRate', 'DOUBLE', '0.000002' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';
INSERT INTO SY_PAKET_WERT (PAKET_PRESET_ID, FELD, TYP, WERT) SELECT ID, 'boundMoisture', 'DOUBLE', '0.06' FROM SY_PAKET_PRESET WHERE PAKET = 'com.dan.dune' AND KLASSE = 'Dune' AND NAME = 'Xaver' AND HERKUNFT = 'MITGELIEFERT';

COMMIT;

-- Ende 04_startdaten.sql
