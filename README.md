# Sylt – Rotes Kliff, das Projekt der Brandung

Der Weststrand von Sylt zwischen Wenningstedt und Kampen als 3D-Echtzeitszene: die Brandung über Riff und Rinnen, Auflauf und Rücklauf auf nassem Sand, Flugsand über Strand und Dünen, das Rote Kliff mit seinen Schichten, das Quermarkenfeuer Rotes Kliff und der Kampener Leuchtturm, vom stillen Sommerabend bis zur Sturmflut, in der das Kliff abbricht. Fortsetzung von Brocken („Projekt der Luft“), mit den Packages aus `lib/Renderer.jar` und zwei neuen, die im Projekt Renderer entstehen: `surf` (die Brandung) und `dune` (Flugsand und Dünen); erweitert werden `cliff`, `fracture` und `reed`, geholt wird `beacon` aus Lighthouse.

**Stand: Phase 1 von 19, das Gerüst** (27.10.2026). Die Engine aus Brocken, entschlackt (F6 A): FFrame mit GL-Fläche, Startbild mit Anmeldung an die Datenbank, Bedienfeld mit allen Blöcken des Werkbuchs, HUD mit der Zeit jedes Durchgangs auf der Grafikkarte, Qualitätsstufen, Foto, Shader neu laden, Faden der Physik, Kinomodus als Anschluss. Die Bühne folgt den Querprofilen der BAW: See bis −13,8 m, Riff 450 m vor dem Strand, Strand, Kliff bis +28 m, Heide, Dünen mit der Uwe-Düne, Watt im Osten; im Kern zu 1 m, sonst zu 10 m. Die See ist ein Platzhalter mit Erdkrümmung bis zur Kimm. Neun Ansichten nach den Vorlagen, Marken (UTM-Raster, Höhenlinien alle 5 m, Wasserlinie, Fernfeld, Kern, Orte). Im Projekt Renderer: `surf` und `dune` als Hüllen nach `muster` (mit Prüfklassen), `beacon` aus Lighthouse. Plan und Fortschritt stehen im Artefakt „Sylt Werkbuch“, die Statusbilder in `docs/status/`.

## Bauen und starten

- NetBeans-Projekt mit Ant, JDK 21. In `lib/`: `Renderer.jar`, JOGL mit Windows-Natives, `FStyle.jar`, `ojdbc11.jar`, `sdo*.jar`.
- **Clean and Build**, dann **Run** (Hauptklasse `com.dan.sylt.app.SyltApp`). Voraussetzung: OpenGL 4.3.
- Nach einer Änderung im Projekt Renderer: dort „Clean and Build“, dann `dist/Renderer.jar` nach `lib/` kopieren; `check-packages` meldet, wenn etwas fehlt.
- **Prüfung:** `ant pruefung` endet mit „ERGEBNIS: alles in Ordnung“ (ohne Grafikkarte und Datenbank: Raum und Orte, die Bühne gegen die Profile der BAW, Gelände und Detailstufen, Ansichten, Uhr und Sonne, die Hüllen von `surf` und `dune` am Gelände, Renderer.jar, Shader, Regel „ohne C:\Daten“).
- **Statusbild:** `ant tempo` schreibt `docs/status/Status_Phase1.png` (Grundansicht, Full HD, Stufe Mittel, volles HUD mit den Zeiten); `-Dansicht=Kliffkante` usw. wählt eine Ansicht.
- **Datenbank:** Schema SYLT auf PDBORCL (`db/User.sql`). `db/db.properties` aus der Vorlage; ist das Passwort leer, fragt das Startbild danach und trägt es ein (oder Umgebungsvariable `Sylt_DB_PASSWORD`). Tabellen kommen mit Phase 16.
- **Tasten:** F1 zeigt alle. Maus ziehen dreht und verschiebt, Rad zoomt, W A S D gleitet, F fliegt frei, R kreist, 1 bis 9 die Ansichten, H das HUD, F11 nur die Szene, F12 ein Foto nach `docs/status/`, F5 lädt die Shader neu.

## Raum

UTM Zone 32 Nord (EPSG:25832), Ursprung am Quermarkenfeuer Rotes Kliff [G] bei E 457 600, N 6 091 170: x nach Osten, z nach Süden (Nord = −z), y über NHN. Kern 2,3 × 3,6 km (O 455 700–458 000, N 6 088 000–6 091 600), Fernfeld 16 × 16 km, Fernring rund 100 km. Raster (`scene.Terrain`): Kern 4 096² zu 1 m, Hauptraster 2 048² zu 10 m, Fernring 1 024² zu 100 m.

## Daten

Die Rohdaten (DGM1, bDOM, DOP20, LOD2 unter `C:\Daten\Sylt`, die BAW-Küstenhöhen) liest ab Phase 2 nur das Werkzeug in `com.dan.sylt.tools`, und zwar einmal; die App liest nie aus `C:\Daten` (Werkbuch 2.4). Quellenvermerke: Geobasisdaten © GeoBasis-DE/LVermGeo SH (Datenlizenz Deutschland); Küstenhöhen © BAW/EasyGSH-DB.
