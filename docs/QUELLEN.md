# Sylt – Quellen der Daten

Stand: Phase 2, 27.10.2026. Die Rohdaten liest nur `ant aufbereitung` (`com.dan.sylt.tools`), einmal; die App liest nur `daten/` (Werkbuch 2.4). Der Pfad der Rohdaten steht in `daten/rohdaten.properties` (Vorgabe `C:\Daten\Sylt`, nach dem Projekt die Wechselfestplatte, Ordner `Sylt\`). Was die Aufbereitung daraus geschrieben hat, steht mit Prüfsummen in `daten/INHALT.txt`.

## Geobasisdaten Schleswig-Holstein

Quellenvermerk: **© GeoBasis-DE/LVermGeo SH**, Lizenz **Datenlizenz Deutschland – Namensnennung – Version 2.0** (dl-de/by-2-0). Bezug: Open-Data-Portal Schleswig-Holstein (Massendownload).

| Ordner auf der Wechselfestplatte | Inhalt | Stand | Verwendet in |
|---|---|---|---|
| `Sylt\DGM1\` | 369 Kacheln `dgm1_32_E_N_1_sh_JAHR.xyz`, 1 km², 1 m, Höhen über NHN (DHHN2016), EPSG:25832, 9,1 GB | 225 Kacheln 2024, 144 Kacheln 2005 | `gelaende`: nur 2024 (138 Kacheln im Hauptraster, 16 im Kern). Die von 2005 liegen nur auf See und Watt und passen nicht zur See (−0,9 m, wo die BAW −10 m misst); übergangen. Die Kacheln von 2024 füllen Wasser mit +1,30 m; diese Flächen ersetzt die BAW. |
| `Sylt\bDOM\` | 258 Kacheln `bdom20nc_32_E_N_1_sh_2024.tif`, bildbasiertes Oberflächenmodell, 20 cm, float32, 26 GB | 2024 | `kante`: 18 Kacheln im Kern (Bewuchs, Kliffkante, 20-cm-Band); die übrigen sieben Kernkacheln gibt es nicht (offene See). |
| `Sylt\DOP20\` | 258 Kacheln `dop20rgbi_32_E_N_1_sh_2024.tif`, Orthofoto RGB und Infrarot, 20 cm, 26 GB; 8 davon nur als `_bw` (Westerland) | 2024 | `luftbild`: 156 Kacheln im Hauptraster, Klassen je Meter (Kern) und je 10 m (F5 A). |
| `Sylt\LOD2\` | 108 ZIPs `Mehrfachdownload_*.zip` mit CityGML 1.0, dazu `LOD2_SH_Massendownload.geojson` (Übersicht ganz SH) | Grundrisse 30.06.2023, Dateien 25.03.2025 | `bauten`. **Befund:** nur zwei verschiedene Inhalte (Kachel 455/6085 64-mal, 464/6100 44-mal), die Namen in den ZIPs stimmen nicht. Kampen, Wenningstedt und die Türme fehlen; Liste zum Nachladen: `docs/LOD2_nachladen.txt`. |
| `LOD2\` (neben `Sylt\`, nachgeladen 27.10.2026) | die fehlenden Kacheln aus `docs/LOD2_nachladen.txt` | wie oben | `bauten` liest beide Ordner (`-Dsylt.lod2` überschreibt): 67 Kacheln, 15 162 Gebäude, 545 287 Dreiecke; Kacheln ohne Eintrag in der Übersicht (19, alle im Meer) gelten als ohne Gebäude. Quermarke 26,1 m, Leuchtturm Kampen 66,2 m über NHN. |

## Küstenhöhen der BAW

Quellenvermerk: **© Bundesanstalt für Wasserbau (BAW), EasyGSH-DB** (mFUND-Projekt EasyGSH, https://mdi-de.baw.de/easygsh/). Die Nutzungsbedingungen der EasyGSH-DB sind vor einer Weitergabe von Bildern oder Daten noch zu prüfen [offen].

| Datei | Inhalt | Verwendet in |
|---|---|---|
| `Rungholt\templates\rasters_COP30\EasyGeo\topography_max_z_2015-2021_ger\…\GeoTiff\topography_max_z_2015-2021_ger.tif` | Topographie der Deutschen Bucht 2015–2021, 10 m, float32 Big-Endian, O 312 895–523 105, N 5 898 795–6 113 105, 1,8 GB | `baw`: Zuschnitt nach `templates/rasters_BAW/sylt_topo_10m.tif` (Hauptraster, 10 m) und `sylt_topo_100m.tif` (Fernring, 100 m); danach liest `gelaende` nur den Zuschnitt, Rungholt wird nicht mehr gebraucht. |
| `…\wattflaechen_2021_ger\…` | Wattflächen 2021 mit Vertrauenskarten (PackBits) | nicht verwendet (Phase 7a prüft sie für das Watt). |

## Gestaltet [G]

- Fernring nördlich von N 6 113 105 (außerhalb der BAW): die nördlichste Zeile der BAW fortgesetzt.
- Festland hinter dem Watt (BAW ohne Wert): Marsch um +1,5 m mit Rauschen, ab O 490 000 zur Geest steigend.
- Seegrund unter den Wasserflächen der Befliegung: BAW mit dem Versatz zum DGM1 am Ufer (klingt mit 20 m im Kern, 30 m im Hauptraster ab), unter +1,25 m gehalten.

## Weitere Quellen

- Lage der Türme: Wikipedia (Quermarkenfeuer Rotes Kliff 54°57′56″ N 8°20′16″ O; Leuchtturm Kampen 54°56′46″ N 8°20′27″ O), am bDOM bestätigt (1 m).
- Gezeiten Kampen (West): https://de.tideschart.com/Denmark/South-Denmark/Tonder-Kommune/Kampen-(West)/ (Phase 7a).
- Vorlagen in `templates/` (RotesKliff1–5, Leuchturm1–4, Kampen1–4): Bilder des Nutzers, nur zum Abgleich.


## Geologie des Roten Kliffs (Phase 4, Schichtprofil in `Cliff.rotesKliff()`)

- Wikipedia (en): Rotes Kliff – rund 30 m hoch, von Risgap (Wenningstedt) bis Haus Kliffende; rostroter Geschiebelehm der Saale-Eiszeit, Geschiebe aus Flint, Rhombenporphyr, Rapakivi-Granit; seit Ende der 1970er Sandvorspülungen, das Kliff heute großteils hinter Dünen. https://en.wikipedia.org/wiki/Rotes_Kliff
- Nordfriisk Instituut, Nordfriesland-Lexikon: rund 4,5 km lang, fast 30 m mächtige Grundmoräne, Landverlust 1 bis 4 m im Jahr. https://www.nordfriiskinstituut.eu/futuur/nordfrieslandlexikon/rotes-kliff/
- GeoTours Sylt: Geschiebelehm, darüber Steinsohle, dunkelbrauner Bodenhorizont (Atlantikum), Flugsand und Dünen; bis 28 m. https://geotourssylt.de/geologie/rotes-kliff.html
- Universität Kiel, Lackfilmprofile Sylt: Saale-Moräne mit verwitterten Geschieben und Kaolinsandschollen; an der Grenze Pliozän/Pleistozän bei Wenningstedt Kaolinsand unter Geschiebelehm mit Eisenschwarte; Eisen-Humus-Podsol und Steinsohle an der Oberfläche der Moräne. https://web.ifg.uni-kiel.de/Museum/museumsfuehrer/GeoSH/unterkapitel1.html
- Mächtigkeiten der Deckschichten, Farben und die Lage der Kaolinsand-Oberkante sind gesetzt [G nach RotesKliff1–5]; Limonitsandstein und Glimmerton stehen am Morsum-Kliff an, nicht am Roten Kliff.
