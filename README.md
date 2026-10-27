# Sylt – Rotes Kliff, das Projekt der Brandung

Der Weststrand von Sylt zwischen Wenningstedt und Kampen als 3D-Echtzeitszene: die Brandung über Riff und Rinnen, Auflauf und Rücklauf auf nassem Sand, Flugsand über Strand und Dünen, das Rote Kliff mit seinen Schichten, das Quermarkenfeuer Rotes Kliff und der Kampener Leuchtturm, vom stillen Sommerabend bis zur Sturmflut, in der das Kliff abbricht. Fortsetzung von Brocken („Projekt der Luft“), mit den Packages aus `lib/Renderer.jar` und zwei neuen, die im Projekt Renderer entstehen: `surf` (die Brandung) und `dune` (Flugsand und Dünen); erweitert werden `cliff`, `fracture` und `reed`, geholt wird `beacon` aus Lighthouse.

**Stand: Phase 4a von 19, das Kliff in der Szene** (27.10.2026): das Rote Kliff aus `com.dan.cliff` (Lockergestein) im Gelände: Wandkarte über dem Kern, Wand und Sturzkegel nur im Band aus `daten/kliff/kante.csv`; Schichten je Ort (Kaolinsand im Süden bis +7 m, nach Norden unter dem Strand; Flugsand oben 1 bis 4 m, je niedriger die Kante, desto mehr) [G]; Runsen und Hohlkehle als Feinform im Scheitelshader; die Grasnarbe als Überhang entlang 3 km Kante, Findlinge auf dem Strand vor dem Fuß, Strandhafer auf den Sturzkegeln (`ground`). Phase 4: `cliff` kann Lockergestein (`Cliff.rotesKliff()`, `StrataMap`, `Sod`; `CliffDemo --kliff`). Phase 3: das Grundbild (Himmel und Wolken aus `sky` und `cloud`, offene See aus `ocean`, Strand aus `mudflat.beach`, Biom „Sylt“ in `ground`, Bedienfeld WETTER). Phase 2: die Datenaufbereitung (`ant aufbereitung` schreibt `daten/`, rund 207 MB, mit `daten/INHALT.txt`; LOD2 vollständig: 15 162 Gebäude). Phase 1: das Gerüst (Engine aus Brocken entschlackt, Bedienfeld, HUD, Ansichten, Marken; im Projekt Renderer `surf`, `dune`, `beacon`). Plan und Fortschritt stehen im Artefakt „Sylt Werkbuch“.

## Bauen und starten

- NetBeans-Projekt mit Ant, JDK 21. In `lib/`: `Renderer.jar`, JOGL mit Windows-Natives, `FStyle.jar`, `ojdbc11.jar`, `sdo*.jar`.
- **Clean and Build**, dann **Run** (Hauptklasse `com.dan.sylt.app.SyltApp`). Voraussetzung: OpenGL 4.3.
- Nach einer Änderung im Projekt Renderer: dort „Clean and Build“, dann `dist/Renderer.jar` nach `lib/` kopieren; `check-packages` meldet, wenn etwas fehlt. `Renderer.jar` steht im Klassenpfad vor `FStyle.jar` (dieses bringt ältere Kopien von `com.dan.ground`, `forest`, `river`, `road` mit).
- **Prüfung:** `ant pruefung` endet mit „ERGEBNIS: alles in Ordnung“ (ohne Grafikkarte und Datenbank: Raum und Orte, die Bühne gegen die Profile der BAW, Gelände und Detailstufen, Ansichten, Uhr und Sonne, die Hüllen von `surf` und `dune` am Gelände, Renderer.jar, Shader, die aufbereiteten Daten mit Prüfsummen, Nähten, Kliff und Klassen, Regel „ohne C:\Daten“).
- **Aufbereitung (einmal):** `ant aufbereitung` (rund 3 Minuten; einzelne Schritte mit `-Dschritt=gelaende,kante,luftbild,bauten,inhalt`). Pfad der Rohdaten in `daten/rohdaten.properties` (legt der erste Lauf an, Vorgabe `C:\Daten\Sylt`); die BAW kommt beim ersten Lauf aus Rungholt nach `templates/rasters_BAW/`.
- **Statusbild:** `ant tempo` schreibt `docs/status/Status_Phase4a.png` (Grundansicht, Full HD, Stufe Mittel, volles HUD mit den Zeiten); `-Dansicht=Kliffkante` usw. wählt eine Ansicht, `-Dbild=…` den Dateinamen, `-Dwolken=aus`, `-Dboden=aus`, `-Dbedeckung=0..8` zum Vergleichen.
- **Vorführung cliff (Lockergestein):** `java -cp "lib/Renderer.jar;lib/*" com.dan.cliff.demo.CliffDemo --kliff` (Fenster) oder mit `--bild docs/status/Cliff_Phase4.png 1800 --kliff` sechs Ansichten als Bild.
- **Datenbank:** Schema SYLT auf PDBORCL (`db/User.sql`). `db/db.properties` aus der Vorlage; ist das Passwort leer, fragt das Startbild danach und trägt es ein (oder Umgebungsvariable `Sylt_DB_PASSWORD`). Tabellen kommen mit Phase 16.
- **Tasten:** F1 zeigt alle. Maus ziehen dreht und verschiebt, Rad zoomt, W A S D gleitet, F fliegt frei, R kreist, 1 bis 9 die Ansichten, H das HUD, F11 nur die Szene, F12 ein Foto nach `docs/status/`, F5 lädt die Shader neu.

## Raum

UTM Zone 32 Nord (EPSG:25832), Ursprung am Quermarkenfeuer Rotes Kliff [G] bei E 457 600, N 6 091 170: x nach Osten, z nach Süden (Nord = −z), y über NHN. Kern 2,3 × 3,6 km (O 455 700–458 000, N 6 088 000–6 091 600), Fernfeld 16 × 16 km, Fernring rund 100 km. Raster (`scene.Terrain`): Kern 4 096² zu 1 m, Hauptraster 2 048² zu 10 m, Fernring 1 024² zu 100 m.

## Daten

Die Rohdaten (DGM1, bDOM, DOP20, LOD2 unter `C:\Daten\Sylt`, die BAW-Küstenhöhen) liest nur `ant aufbereitung` (`com.dan.sylt.tools`), einmal; die App liest nur `daten/` und startet auch, wenn `C:\Daten\Sylt` fehlt (Werkbuch 2.4). Fehlt `daten/`, zeigt sie die Bühne von Phase 1 und das Startbild sagt, was zu tun ist. Jede Rohdatei mit Stand und Lizenz: `docs/QUELLEN.md`. Quellenvermerke: Geobasisdaten © GeoBasis-DE/LVermGeo SH (Datenlizenz Deutschland); Küstenhöhen © BAW/EasyGSH-DB.
