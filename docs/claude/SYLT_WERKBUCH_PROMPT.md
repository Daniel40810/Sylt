# Sylt — Planungsprompt für das Werkbuch

> **Zweck dieser Datei:** Aus ihr erstellt Claude das **„Sylt Werkbuch“**.
> Noch **kein Code, keine Skripte**. Zuerst wird geplant: Ausgangspunkt klären, Themen und Struktur festlegen, offene Fragen stellen. Programmiert wird erst, wenn Phase 1 freigegeben ist.
>
> Stand: 27.10.2026 · NetBeans-Projekt `C:\Users\Dan40810\Documents\NetBeansProjects\Sylt` · Rohdaten `C:\Daten\Sylt`

---

## 1. Auftrag

**Rotes Kliff, Sylt: das Projekt der Brandung.** Das Werkbuch beschreibt eine animierte 3D-Echtzeitszene vom Weststrand zwischen Wenningstedt und Kampen. Zu sehen sind das rot-weiße Kliff, davor Sandbänke und Brandungsrinnen und dahinter Dünen mit Strandhafer, das Quermarkenfeuer Rotes Kliff und der Kampener Leuchtturm. Die Szene reicht vom stillen Sommerabend bis zur Sturmflut, in der die Brandung den Kliffuß ausräumt und das Kliff abbricht.

Auf Brocken („Projekt der Luft“) folgt mit Sylt das **„Projekt der Brandung“**. Brocken hat das Windfeld (`com.dan.wind`) gebracht, Sylt bringt das, was der Wind und das Meer am Ufer tun.

**Was neu entsteht:**

- `com.dan.surf`, die Brandung: Die Welle wird im Flachen höher, an den Bänken gebeugt und bricht, je nach Hang schäumend oder sich überschlagend. Danach kommen die Weißwasserwalze, Auflauf und Rücklauf auf nassem Sand (mit `mudflat`) und der Rückstrom.
- `com.dan.dune`, Flugsand und Dünen: Flugsand nach Bagnold, wandernde Dünen, Windrippel und Sandfahnen über den Kämmen. Strandhafer aus `reed` bremst den Sand. Den Wind liefert `wind` aus Brocken.

**Was erweitert wird:**

- `com.dan.cliff` bekommt ein Kliff aus Lockergestein mit Geschiebelehm und Kaolinsand. Bisher kann es nur Fels (Gneis).
- `com.dan.fracture` bekommt den Abbruch nach Unterspülung: Kohäsion, Kippen und Rutschen, Zerfall zu Sand.
- `com.dan.reed` bekommt die Art Strandhafer, die Übersandung verträgt.

**Rückfluss am Ende:** **Thunder** steigt auf `Renderer.jar` und GL um und bekommt eine echte Brandung (Ostsee). **Lighthouse** bekommt eine echte Brandung am Sockel.

**Detailtreue und Physik sind ausdrücklich gewünscht.** Wie bei Lighthouse, Glacier und Rungholt gilt: Effekte richtig ausreizen, eigene Ideen sind erwünscht, und wo nötig entstehen neue eigenständige Engines.

---

## 2. Ausgangslage (geprüft am 27.10.2026)

### 2.1 Das Projekt Sylt

| Bereich | Stand |
|---|---|
| JDK | `platform.active=JDK_21`, `javac.source/target=21` |
| `lib/` | `Renderer.jar` (26.10.2026, 5,9 MB, gleich dem Stand in `Renderer/dist`), `Avatar.jar`, `FStyle.jar`, JOGL + gluegen (win-amd64), `ojdbc11.jar`, `sdoapi.jar`, `sdogr.jar`, `sdoutl.jar`; alle im `javac.classpath` |
| `src/` | leer, `main.class` nicht gesetzt |
| Datenbank | `db/User.sql`: `manage_user('Sylt', 'sy')`, Rolle `DB_CONNECT`, Dienst `PDBORCL`; `db/db.properties(.example)` wie bei Lighthouse (Passwort leer → Startbild fragt und speichert, alternativ `Sylt_DB_PASSWORD`); `db/Role_Grantee.sql` und `db/Object_nach_Schema.sql` (Berichte aus SQL Developer) |
| `docs/` | `claude/` (diese Datei), `images/`, `status/`, alle sonst leer |
| `templates/` | 9 Vorlagenbilder (Abschnitt 3) |
| Git | noch kein Repository |

**Avatar.jar** wird nicht gebraucht, es gibt keine Menschen über Avatar (Entscheidung vom 27.10.). Es fliegt in Phase 1 aus `lib/` und aus dem Klassenpfad (F23).

### 2.2 Der Renderer: Packages als Bibliothek (neu seit Brocken)

Die Element-Packages liegen nicht mehr als Kopie in jedem Projekt. Sie liegen zentral im NetBeans-Projekt **Renderer** (`C:\Users\Dan40810\Documents\NetBeansProjects\Renderer`) und kommen als `Renderer.jar` in die Szene. Das Ant-Target `check-packages` (`tools/PaketPruefung.java`) übersetzt nach jedem Build jedes Package für sich: den Kern gegen das JDK, `gl/` zusätzlich gegen JOGL, `demo/` zusätzlich gegen FStyle. Es prüft auch, ob die Shader im Package liegen.

**Entscheidung vom 27.10.:** `surf` und `dune` entstehen **direkt im Renderer-Projekt**. Dort werden auch `cliff`, `fracture` und `reed` erweitert. Sylt bekommt nach jeder Package-Phase ein neues `Renderer.jar` (Ablauf in Abschnitt 4).

Im Renderer vorhanden (37 Packages): `buoyancy cliff cloud fall figure flock fog forest fracture ground hearth ice kino lightning mudflat muster ocean rail rain reed rime river road seal season sky smoke soundscape spray store tide timber wave weather weathering wetglass wind`.

Für Sylt sind diese Packages wichtig. Vor dem Werkbuch sind ihre READMEs zu lesen:

| Package | Stand laut README | Rolle in Sylt |
|---|---|---|
| `ocean` | FFT-Windsee + Gerstner-Dünung, Schaum, `bedTexture` (Grund und Ufer seit Haithabu), `longTexture` (lange Welle seit Glacier), Tide je Zelle seit Rungholt | die offene See bis zur Brandungszone; liefert Spektrum und Dünung an `surf` |
| `wave` | Flachwassergleichungen, zwei Gitter (8 m / 2 m), Auflaufen, `hold` (gehaltener Wasserstand) | Sturmflut über den Strand bis an den Kliffuß; Vergleich und Prüfung für den Auflauf in `surf` |
| `tide` | Partialtiden, Presets `husum`, `bremerhaven`, `roterSand` …, Windstau, `Flood` | Pegel Westerland (neues Preset), Windstau der Sturmflut |
| `mudflat` | Nässe als Zeit, Trocknen, Preset `beach()`, Spuren, Feinform | nasser Strand nach Auflauf und Rücklauf, Spülsaum, Trocknen |
| `spray` | Gischt an Körpern (Kegelstümpfe), Weißwasser-Ballen, Ereignisse | Gischt am Kliffuß bei Sturmflut, an Buhnen und Pfählen |
| `cliff` | Fels als Material und Feinform (Gneis, Klüfte, Überhänge), Wandkarte | wird zum **Lockergestein-Kliff** erweitert |
| `fracture` | Voronoi-Zellen, Rigid-Body-Spring, Mohr-Coulomb, Starrkörper, Zerschellen | wird um **Unterspülung und Abbruch von Lockergestein** erweitert |
| `reed` | Schilf, Binse, Gras im Wind, Schnee, Reif | bekommt die Art **Strandhafer** |
| `wind` | Profil nach Monin-Obukhov, Böen, massenkonsistentes Feld (MATHEW), **Stand: Brocken Phase 4** | Wind über Kliff und Dünen: Aufwind an der Kliffkante, Lee hinter den Kämmen; Antrieb für `dune` |
| `kino` | Kinomodus für Regiebücher (seit Glacier) | Regiebücher der Szene |
| `sky`, `cloud`, `rain`, `fog`, `weather`, `lightning`, `flock`, `soundscape`, `season`, `weathering`, `wetglass`, `ground`, `store` | aus Lighthouse, Haithabu, Glacier, Rungholt, Brocken | Himmel, Wetter, Möwen, Klang, Jahreszeit (Strandkörbe nur im Sommer), Heide, Datenseite |
| `muster` | Vorlage für ein Element-Package | Grundlage für `surf` und `dune` |
| `figure`, `seal` | Menschen, Seehunde | Menschen: nein (F23); Seehunde am Weststrand nur selten (F24) |

**Nicht im Renderer:** `beacon` (Leuchtfeuer). Es liegt nur als Kopie in Lighthouse. Für den Kampener Leuchtturm in der Nacht und für das historische Quermarkenfeuer wird es gebraucht (F18).

**Gleichzeitig mit Brocken:** `wind` stand bei der Prüfung auf Brocken Phase 4. Sylt verändert `wind` nicht, sondern nutzt es über eine eigene kleine Schnittstelle in `dune` (Paketvertrag Punkt 4). Was `dune` vom Wind braucht und Brocken noch nicht liefert, wird als Wunsch an Brocken notiert (F21).

### 2.3 Rohdaten unter `C:\Daten\Sylt` (rund 60 GB)

| Ordner | Inhalt | Abdeckung | Befund für den Ausschnitt |
|---|---|---|---|
| `DGM1/` | 369 Kacheln `dgm1_32_<O>_<N>_1_sh_<Jahr>.xyz`, 1 m, NHN, ETRS89/UTM 32 (EPSG:25832), 9,1 GB | O 452–467 km, N 6 062–6 102 km; 225 Kacheln von 2024, 144 von 2005 | Land, Strand und Kliff liegen ganz in Kacheln von **2024** (Kachel 456/6089 bis NHN +45,8 m, 457/6091 bis +27,4 m). Die Kacheln von 2005 im Ausschnitt (455/6089, 455/6090, 456/6091) enthalten nur Wasserflächen (alles ≤ NHN −0,2 m) und werden durch die BAW-Daten ersetzt. |
| `bDOM/` | 258 Kacheln `bdom20nc_…_2024.tif`, 20 cm, Oberflächenmodell (mit Bewuchs und Bauten), 26 GB | O 453–467, N 6 062–6 101 | Der ganze Ausschnitt liegt vor (455–457 / 6088–6091). Taugt für die feine Kliffkante, Grasnarben-Überhänge, Strandhafer-Höhe und Bauten. |
| `DOP20/` | 258 Kacheln `dop20rgbi_…_2024.tif`, 20 cm, RGB + Infrarot, 26 GB | wie bDOM; 8 Kacheln bei Westerland nur als `_bw` | Der Ausschnitt ist vollständig. Infrarot trennt Bewuchs, Sand und nassen Sand. Die `_bw`-Kacheln (455–456 / 6083–6086) liegen außerhalb. |
| `LOD2/` | 108 ZIPs mit CityGML `LoD2_32_<O>_<N>_1_SH.xml` + Übersicht `LOD2_SH_Massendownload.geojson` (ganz SH), 149 MB | Sylt | Häuser von Kampen und Wenningstedt am Rand, vermutlich auch beide Leuchttürme als Baukörper (in Phase 2 prüfen) |

**Meeresgrund, „[zu prüfen]“ aus dem Vorschlag, ist geprüft:** Die Küstenhöhen der BAW, die schon in Rungholt im Einsatz waren, liegen vollständig für die ganze deutsche Küste vor:
`Rungholt/templates/rasters_COP30/EasyGeo/topography_max_z_2015-2021_ger/…/GeoTiff/topography_max_z_2015-2021_ger.tif`. Die Datei ist 1,8 GB groß, Float32 unkomprimiert und Big-Endian, 10 m, O 312 895–523 105, N 5 898 795–6 113 105. Sie **deckt Sylt**. Gelesene Querprofile:

| Nord (UTM) | Wasserlinie bei Ost ≈ | Kliffoberkante | 8 km vor der Küste |
|---|---|---|---|
| 6 088 000 | 456 000 | NHN +17 bis +21 m | −13,2 m |
| 6 089 500 | 456 650 | NHN +23 bis +32 m | −13,3 m |
| 6 091 000 | 457 200 | NHN +6 bis +9 m (Dünen) | −13,7 m |
| 6 092 500 | 457 900 | — | −14,1 m |

Bei N 6 086 000 zeigt sich ein Riff etwa 500 m vor dem Strand (−5,4 / −2,2 / −3,3 m). 10 m sind für Brandungsrinnen zu grob, die Feinform unter 10 m wird also gestaltet [G] (F10). Dazu kommen aus demselben Ordner die Wattflächen 2021 (`topography_inter_supratidal_2021_ger.tif` mit Vertrauenskarten für Hoch- und Niedrigwasserlinie).

### 2.4 `C:\Daten` kommt nach Projektabschluss auf eine Wechselfestplatte

Diese Regel gilt verbindlich und wird im Werkbuch zur Abnahme-Bedingung:

1. **Rohdaten werden nur einmal gelesen**, und zwar von Aufbereitungswerkzeugen in `com.dan.sylt.tools` (Phase 2). Den Pfad legt `daten/rohdaten.properties` fest. Die Datei steht in `.gitignore`, Vorgabe ist `C:\Daten\Sylt`.
2. Die Werkzeuge schneiden zu, vereinfachen und schreiben **alles, was die App braucht, ins Projekt** nach `daten/` (Gelände, Grund, Materialkarten, Bauten, Prüfbilder). `daten/` steht ebenfalls in `.gitignore`, wird aber mit einer Prüfsumme je Datei in `daten/INHALT.txt` beschrieben.
3. Der BAW-Zuschnitt für Sylt kommt nach `templates/rasters_BAW/` (wie in Rungholt), weil die 1,8-GB-Datei in Rungholt liegt und nicht unter `C:\Daten`.
4. **Die App liest nie aus `C:\Daten`.** Abnahme von Phase 2: `C:\Daten\Sylt` wird vorübergehend umbenannt, und die App startet trotzdem vollständig. Fehlt `daten/`, meldet das Startbild, welches Werkzeug mit welchem Rohdatenpfad zu laufen hat.
5. `docs/QUELLEN.md` nennt jede Rohdatei mit Stand, Lizenz (Datenlizenz Deutschland, GeoBasis-DE/LVermGeo SH; BAW/EasyGSH) und dem Ordner auf der Wechselfestplatte.

---

## 3. Vorlagen (`templates/`)

| Datei | Inhalt | Was übernommen wird |
|---|---|---|
| `RotesKliff1.jpg` | Kliff im Abendlicht von unten am Strand: senkrechte Wand in Orange-Rost, Abbruchkanten in Stufen, oben **Grasnarbe als Überhang**, Runsen und Rinnen, Sandfuß mit Strandhafer-Büscheln, weißer Strandsand | Farbe und Schichtung, Runsen, Überhang der Grasnarbe, Sturzkegel; Licht der tiefen Sonne aus West |
| `RotesKliff2.jpg` | Strand von einer Holztreppe: Kliff links, **Strandkörbe** (weiß, blau-weiß gestreift, mit Nummern), flache Brandung rechts, Spülsaum | Strandkorb-Modell und -Verteilung, Abendstimmung, Kamera-Preset „Treppe“ |
| `RotesKliff3.jpg` | Von der Kliffkante hinunter bei Wind: **Brandung mit mehreren Brecherlinien**, Schaumstreifen, grünes Wasser, Strandkörbe, Möwe, **Holzsteg mit Strandlokal auf Stelzen** | Maßstab der Brandungszone, mehrere Riff-Brecherlinien, Schaum, Kamera-Preset „Kliffkante“ |
| `RotesKliff4.jpg` | **Bohlenweg mit Geländer** durch Dünen und Heide, hinten eine Wanderdüne mit offener Sandflanke | Bohlenweg als Bauteil, Krähenbeere und Heide, offene Sandflanke für `dune` |
| `RotesKliff5.jpg` | Treppe zum Strand, Kliff links, Strandkörbe, **Buhnen** im Wasser (Wenningstedt), Abendlicht | Strandzugang, Buhnen als Hindernis für `surf`, Sandfuß am Kliff |
| `Leuchturm1.jpg` | **Quermarkenfeuer Rotes Kliff** (achteckiger Backsteinturm, weiße Laterne, grünes Kupferdach), Langzeitbelichtung mit ziehenden Wolken, Sandweg, Pfähle | Turm-Modell, Wolkenzug als Stimmung „Abendwind“ |
| `Leuchturm2.jpg` | Quermarkenfeuer in der Blauen Stunde, **Laterne beleuchtet**, Strandhafer im Wind | Stimmung Nacht, historische Beleuchtung (F19) |
| `Leuchturm3.jpg` | Quermarkenfeuer bei Tag mit Weg, Bank, Abfalleimer, Kartoffelrose; Dünen dahinter | Umgebung, Kleinkram für Detailtreue |
| `Leuchturm4.jpg` | Quermarkenfeuer im grauen Wetter, **Bohlenweg**, hohe Dünen mit Strandhafer, das Meer am Horizont | Stimmung „Schlechtwetter“, Dünenrelief, Bohlenweg |

**Recherche vor dem Werkbuch** (Ergebnisse in `docs/QUELLEN.md`, je **belegt / gemessen / gestaltet [G]** wie in Rungholt):

- **Quermarkenfeuer Rotes Kliff:** gebaut 1912/13, achteckiger Backsteinturm 13 m, Feuerhöhe 23 m, Kennung F WRG (Sektorenfeuer), seit 1975 außer Betrieb, Tagesmarke, Denkmal; Lage 54°57′56″ N 8°20′16″ E (UTM ≈ 457 605 / 6 091 171). Maße der Laterne und Sektorgrenzen recherchieren.
- **Kampener Leuchtturm** („Langer Christian“): 1855/56, 40 m, Feuerhöhe 62 m, weiß mit breitem schwarzem Band, gelbe Bornholmer Ziegel und Eisenringe, Kennung Iso 7 s (laut Wikipedia), weiß 20 sm, rot 16 sm; Lage 54°56′46″ N 8°20′27″ E (UTM ≈ 457 778 / 6 089 003). Die Sektoren recherchieren, denn der rote Sektor hat 1975 die Aufgabe des Quermarkenfeuers übernommen.
- **Rotes Kliff:** rund 30 m hoch, von Wenningstedt (Parkplatz Risgap) bis Haus Kliffende in Kampen. Aufbau von oben nach unten: Dünen- und Flugsand, rot oxidierter Geschiebelehm (Saale-Eiszeit, rund 120 000 Jahre) mit Findlingen (Flint, Rhombenporphyr, Rapakivi-Granit), darunter Kaolinsand (weiß). Prüfen, ob am Roten Kliff auch Limonitsandstein oder Glimmerton anstehen, oder ob das nur für das Morsum-Kliff gilt. Sandvorspülungen seit den 1970er-Jahren, Abbrüche bei Sturmfluten (etwa Anatol 1999 und Xaver 2013, weitere recherchieren), Rückgang in Metern je Jahr.
- **Brandung am Sylter Weststrand:** Riffe (Sandbänke) und Rinnen, Lage und Tiefe, Rippströmungen (Warnungen der DLRG), mittlere Wellenhöhen und Perioden (Seegangsmessungen Westerland), Buhnen und Tetrapoden.
- **Tide Westerland:** Tidenhub (rund 1,8 m, prüfen), Hoch- und Niedrigwasserzeiten aus einem Gezeitenkalender als Grundlage für ein Preset `Tide.westerland()` (wie `husum()` aus 475 Zeiten).
- **Flugsand:** Bagnold (1941), Schwellen-Schubspannungsgeschwindigkeit für Dünensand (d ≈ 0,25–0,35 mm), Einfluss der Feuchte (Belly 1964, McKenna Neuman), Strandhafer (Ammophila arenaria) und Schubspannungs-Aufteilung (Raupach 1992), Windrippel (Wellenlänge, Wanderung).
- **Brechertypen:** Iribarren-Zahl ξ = tan β / √(H₀/L₀) mit Schwellen für Schwallbrecher (spilling, ξ < 0,4), Sturzbrecher (plunging, 0,4–2), Reflexions- und Brandungsbrecher (collapsing/surging, > 2); Brechkriterium H/h ≈ 0,78 (McCowan) und Dissipation nach Battjes-Janssen.

---

## 4. Kernprinzip: der Paketvertrag (unverändert aus Lighthouse, ergänzt um den Renderer)

Jedes Package erfüllt alle Punkte. Das Werkbuch übernimmt die Liste als Abnahme-Checkliste.

1. **Unabhängig:** nur Java 21, für `gl/` zusätzlich JOGL, für `demo/` zusätzlich FStyle. Kein Package hängt von `com.dan.sylt` oder von einem anderen Element-Package ab.
2. **Zum Herauskopieren:** `src/com/dan/<paket>/` reicht, die Shader liegen im Package.
3. **Kern, `gl/` und `demo/` getrennt.** Der Kern beantwortet Fragen ohne Grafikkarte, etwa `surf.breakingAt(x, z)` oder `dune.fluxAt(x, z)`.
4. **Kopplung nur über schmale Schnittstellen**, verdrahtet in `com.dan.sylt.wiring`. Beispiel: `surf` fragt den Grund über `Surf.Bed`, den Wind über `Surf.Wind` und den Pegel über `Surf.Level`. Die App reicht `ocean`, `wind` und `tide` hindurch.
5. **Presets + offene Werte:** Beaufort-Presets (`calm()`, `breeze()`, `gale()`, `storm()`, `beaufort(n)`) und dazu Orts-Presets (`sylt()`, `ostsee()` für Thunder).
6. **Reproduzierbar** über `seed`.
7. **Qualitätsstufen** `LOW` / `MEDIUM` / `HIGH` / `ULTRA` für die integrierte Radeon (2 GB).
8. **README.md** im Stil der vorhandenen: Stand, „Was es kann“, „Benutzung“, „Aufbau“, „Grenzen“, ab jetzt auch „Seit welcher Phase“.
9. **Isolationsprüfung** über `check-packages` im Renderer: rot heißt nicht fertig.
10. **Abnahme je Package:** eigene Demo ohne Sylt-Code, `…Check` ohne Fenster endet mit „Alles in Ordnung.“, `--bild datei.png` für das Statusbild.
11. **Neu, Ablauf mit dem Renderer:** Package-Arbeit geschieht nur in `Renderer/src`. Danach wird gebaut, `check-packages` muss grün sein, `Renderer/dist/Renderer.jar` kommt nach `Sylt/lib/`, und das Werkbuch notiert Datum und Größe des JARs. Sylt enthält nie Kopien von Element-Packages.
12. **Neu:** Eine Erweiterung bestehender Packages (`cliff`, `fracture`, `reed`) darf nichts brechen, was andere Szenen schon nutzen (Geiranger, Glacier, Haithabu). Die vorhandenen `…Check` müssen weiter „Alles in Ordnung.“ melden, und neue Fähigkeiten sind abschaltbar und stehen in der Vorgabe auf aus.

---

## 5. Package-Katalog (Vorschlag, im Werkbuch zu bestätigen)

### 5.1 `com.dan.surf` — die Brandung *(Prunkstück 1)*

**Seegang über dem Grund (Kern, stationär, CPU):**
- Eingang: Spektrum oder Dünung von außen (H_s, T_p, Richtung, Streuung), Grund als Höhenfeld (`Bed`), Pegel (`Level`: Tide + Windstau), Wind (`Wind`).
- **Shoaling** (die Welle wird im Flachen höher, über die Gruppengeschwindigkeit), **Refraktion** an Riffen und Rinnen (Wellenstrahlen oder Wellenwirkung auf einem Gitter), schwache **Diffraktion** hinter Buhnen.
- **Brechen:** Kriterium H/h, Dissipation nach Battjes-Janssen, **Brechertyp je Ort aus der Iribarren-Zahl**. Am Riff bricht die Welle schäumend, auf dem steilen Strandhang überschlägt sie sich, und bei Sturmflut am Kliffuß wird sie reflektiert.
- Ergebnis je Zelle: Wellenhöhe, Richtung, Wellenzahl, Phase (über ∫k·dx), Anteil gebrochener Wellen, Brechertyp, Strahlungsspannung.

**Strömung in der Brandungszone (Kern):**
- **Brandungsstau** (Set-up) und **Längsströmung** aus der Strahlungsspannung (Longuet-Higgins).
- **Rückstrom und Rippströmung:** Durch die Rinnen läuft das Wasser seewärts zurück, sichtbar als ruhige, dunkle Lücke in der Brecherlinie mit Schaumfahne nach außen.
- Abfragen: `currentAt`, `setupAt`, `breakingAt`, `foamAt`, `typeAt`, `heightAt(x, z, t)` (phasenaufgelöst, für Kamera, Gischt und Möwen).

**Die einzelne Welle (gl, phasenaufgelöst):**
- Wellenzüge, die über dem Grund langsamer werden, sich drehen und steiler werden. Vor dem Brechen wird der Kamm asymmetrisch (vorn steil, hinten flach).
- **Sturzbrecher** mit überschlagender Lippe und Hohlraum (Tube) als Netzverformung, mit durchscheinendem Kamm gegen die Sonne (Licht im Wellenkamm aus `ocean`).
- **Schwallbrecher:** Der Schaum rollt vorn den Kamm hinunter.
- **Weißwasserwalze** (Bore) nach dem Brechen: weiße, turbulente Front, die zum Strand läuft und dabei zerfällt. Dahinter bleibt **Schaumteppich** mit Nachleuchten, Schaumstreifen bilden sich in der Strömung.
- **Brandungsschwebung** (Surf Beat): Wellengruppen, sodass alle paar Minuten eine Serie größerer Brecher kommt.

**Auflauf und Rücklauf (Kern + gl):**
- Je Querprofil im Abstand von 1–2 m eine **1D-Flachwasserrechnung im Schwall** (oder `wave` im feinen Gitter, siehe F8): Die Zunge läuft auf, wird dünn, versickert zum Teil und läuft zurück. Wo die Rücklaufzunge auf die nächste Welle trifft, entsteht ein kleiner Stoß.
- **Spülsaum** (Schaumlinie, Tang, Muschelschill) am höchsten Auflauf. **Nasser Sand** über `mudflat.beach()`: Die Nässe springt beim Auflauf auf 1, danach glänzt der Sand als Spiegel und trocknet.
- Ereignisse: `onSwash(x, z, Reichweite)` für `mudflat` und `dune` (nasser Sand fliegt nicht), `onImpact(x, y, z, Stärke)` am Kliffuß für `fracture` und `spray`, `onSound(Art, Stärke)` für `soundscape` (Donnern des Sturzbrechers, Zischen des Weißwassers, Rollen der Kiesel im Rücklauf).

**Orte:** `sylt()` (Weststrand, Dünung aus West bis Nordwest, Riff), `ostsee()` für Thunder (kurze Windsee, keine Tide, steiler Strand), `sockel()` für Lighthouse (Brandung an Caisson und Steinschüttung).

### 5.2 `com.dan.dune` — Flugsand und Dünen *(Prunkstück 2)*

- **Flugsand nach Bagnold:** Transport q ∝ (u* − u*t)·u*², darunter nichts. Die Schwelle u*t steigt mit der Korngröße und mit der **Feuchte**, die aus `mudflat` (Nässe des Strands) und aus `rain` kommt. Bei nassem Strand nach dem Auflauf fliegt nichts, auf dem trockenen oberen Strand schon.
- **Wind am Boden:** u* aus dem Feld von `wind`, dazu der Aufwind an der Kliffkante und der Lee-Wirbel hinter den Kämmen. **Strandhafer** aus `reed` nimmt einen Teil der Schubspannung auf (Aufteilung nach Raupach), also wird dort Sand abgelagert.
- **Sandfahnen** (Streamers) über den flachen Strand, **Sandschleier über den Kämmen** (Kammfahnen) und **Saltation** knapp über dem Boden. Das sind GPU-Partikel, die dem Wind folgen und sanft verblassen.
- **Windrippel** quer zum Wind (Wellenlänge 5–15 cm, wandern mit cm/min), im Shader wie die Rippel in `mudflat`, aber trocken.
- **Wandernde Dünen** im Zeitraffer: Höhenfeld des Sands mit Abtrag im Luv, Ablagerung im Lee und Lawinen auf der Rutschfläche (Schüttwinkel ≈ 34°), etwa als Plattenmodell nach Werner (1995) oder als Kontinuum. Echtzeit (Rippel, Fahnen) ist von Zeitraffer (Wochen bis Jahre) getrennt.
- **Sandfangzäune** (Buschzäune) und Strandhafer-Pflanzungen als Hindernisse, die Sand fangen.
- Ereignisse: `onDeposit(x, z, dh)` (Strandhafer wird übersandet und wächst durch), `onScour`.

### 5.3 Erweiterung `com.dan.cliff` — Kliff aus Lockergestein

- **Schichtprofil** statt Gneis: eine Liste von Schichten (Material, Ober- und Unterkante relativ zur Kliffoberkante oder absolut, Farbe, Korn, Festigkeit). Ein Preset `rotesKliff()` mit Flugsand → Geschiebelehm (rot, mit Findlingen) → Kaolinsand (weiß, geschichtet).
- **Runsen** (Erosionsrinnen vom Regen), Rieselsand, Trockenrisse im Lehm, **Grasnarbe als Überhang** oben (Vorlage RotesKliff1/3), **Sturzkegel** und Sandfuß unten, Strandhafer auf dem Fuß.
- **Nässe** dunkelt den Lehm, Kaolinsand bleibt hell. Nach dem Regen laufen Sickerstreifen aus der Grenze zwischen Lehm und Sand.
- Die Wandkarte kennt die **Hohlkehle**, die die Brandung am Fuß auswäscht (Eingang von `surf` über die App).
- Der Fels (Gneis) bleibt unverändert und ist die Vorgabe (Paketvertrag Punkt 12).

### 5.4 Erweiterung `com.dan.fracture` — Abbruch nach Unterspülung

- **Material mit Kohäsion:** Mohr-Coulomb mit c > 0 und φ für Geschiebelehm und Kaolinsand, **Feuchte macht schwächer**. Presets `Material.geschiebelehm()` und `Material.kaolinsand()`.
- **Unterspülung als Last:** Zellen am Fuß werden von der Brandung abgetragen (`Solid.meltCells` gibt es schon). Das Kliff kippt, rutscht oder bricht schalenförmig (Kippen, Rutschung nach Bishop, Abscheren).
- **Zerfall zu Sand:** Lockere Stücke zerschellen beim Aufprall und **werden Teil des Höhenfelds** (Sturzkegel), statt als Starrkörper liegen zu bleiben. Dafür gibt es ein neues Ereignis `onDisintegrate(Volumen, Ort)`, das die App an das Gelände, an `dune` und an `surf` weiterreicht. Die nächste Flut trägt den Kegel ab.
- Staubwolke aus trockenem Sand beim Abbruch, als Partikel aus `dune` oder `smoke` (F15).

### 5.5 Erweiterung `com.dan.reed` — Strandhafer

- Neue Art `STRANDHAFER` (Ammophila arenaria): bis 1 m, eingerollte, graugrüne, steife Blätter, Büschel, Ähre im Sommer. Er wird im Wind silbrig (Unterseite, Vorlage Leuchturm2) und verträgt **Übersandung** (`burial`): Die Halme ragen aus dem Sand und wachsen nach.
- Nebenher, falls es passt: `HEIDE` (Calluna) und `KRAEHENBEERE` als niedrige Polster für die Kampener Heide. Alternativ kommen sie über `ground` (F13).

### 5.6 Wiederverwendung ohne Änderung

`ocean` (offene See), `tide` (Preset Westerland neu, das ist nur ein Preset), `wave` (Sturmflut über den Strand), `mudflat.beach()`, `spray` (Kliffuß, Buhnen), `wind`, `sky`, `cloud`, `rain`, `fog` (Seenebel, Seerauch), `weather`, `lightning`, `flock` (Möwen im Aufwind an der Kliffkante), `soundscape`, `season`, `kino`, `store`, `ground`, `weathering`.

---

## 6. Die Szene (projektspezifisch, `com.dan.sylt`)

**Ausschnitt (Entscheidung vom 27.10.):** nur das Rote Kliff von Wenningstedt bis nördlich Kampen samt Leuchtturm, etwa 3 × 2 km. Vorschlag für Phase 2, am Höhenbild festzulegen:

| Bereich | Ausdehnung (ETRS89/UTM 32) | Auflösung | Quelle |
|---|---|---|---|
| **Kern** | O 455 700–458 000, N 6 088 000–6 091 600 (2,3 × 3,6 km) | 1 m Gelände, 20 cm Kliffkante aus bDOM, Material aus DOP20 | DGM1 2024, bDOM, DOP20, LOD2 |
| **Brandungszone** | Kern, seewärts bis etwa −8 m NHN | Grund 10 m (BAW), darauf Riff und Rinnen gestaltet [G] auf 2 m | BAW, Feinform [G] |
| **Fernfeld** | etwa 16 × 16 km um den Kern | 10 m, in der Ferne gröber | BAW (See), DGM1 (Insel) |

Beide Türme liegen im Kern: das Quermarkenfeuer bei ≈ 457 605 / 6 091 171, der Kampener Leuchtturm bei ≈ 457 778 / 6 089 003. Die Uwe-Düne (52,5 m) liegt dazwischen (Lage in Phase 2 am Höhenbild prüfen).

**Bauteile und Details:**
- **Quermarkenfeuer Rotes Kliff** nach Leuchturm1–4: Achteck aus Backstein mit Verband, Fensterchen, Sockel mit Blechabdeckung, Galerie mit Geländer, weiße Laterne mit Sprossen, Uhr- bzw. Lüftungsscheibe, grünes Kupferdach mit Knauf und Blitzableiter.
- **Kampener Leuchtturm**: weiß mit schwarzem Band, Eisenringe, Galerie, Laterne. Er steht in der Ferne, ist aber nachts das Feuer der Szene (Iso 7 s, Sektoren).
- **Strandaufgänge**: Holztreppen und Bohlenwege mit Geländer (RotesKliff2/4/5, Leuchturm4), Strandlokal auf Stelzen (RotesKliff3), Rettungsturm der DLRG, Hinweisschilder, Abfalleimer, Bank (Leuchturm3).
- **Strandkörbe** weiß bzw. blau-weiß, mit Nummern, im Sommer verteilt, in Gruppen, in Windrichtung gedreht. Im Winter sind sie fort (`season`), bei Sturmflut stehen sie nicht mehr am Strand.
- **Buhnen** bei Wenningstedt (RotesKliff5) als Hindernis für `surf`.
- **Kampen und Wenningstedt** am Rand: Reetdachhäuser aus LOD2, Dächer mit Reet (Material aus `timber` oder `weathering`).
- **Bewuchs:** Strandhafer auf Vor- und Weißdünen, graue Dünen mit Krähenbeere und Heide, Kartoffelrose an den Wegen, Grasnarbe an der Kliffkante.
- **Leben:** Möwen, Austernfischer am Spülsaum und Sanderlinge, die vor der Welle herlaufen (beides über `flock`). Keine Menschen (F23).

### Schaustücke (Kamera-Presets und Regie)

1. **Abend am Kliff** (RotesKliff1): tiefe Sonne, das Kliff glüht, Sturzbrecher im Gegenlicht mit durchscheinender Lippe
2. **Von der Kliffkante** (RotesKliff3): Blick auf die Brandungszone mit drei Brecherlinien über Riff und Strand, Rippströmung als Lücke, Möwen stehen im Aufwind
3. **Mit der Welle:** Die Kamera reitet auf einem Wellenkamm vom Riff bis zum Auflauf, mit Überschlag, Tube und Weißwasser
4. **Spülsaum ganz nah:** Auflauf, Rücklauf, nasser Sand als Spiegel, Sanderlinge, Schaumblasen platzen
5. **Sandsturm am Strand:** Sandfahnen ziehen über den Strand, Sandschleier stehen über den Dünenkämmen, der Strandhafer peitscht
6. **Der Abbruch** (Sturmflut): Wasser bis an den Kliffuß, Brecher schlagen die Hohlkehle aus, Risse in der Grasnarbe, das Kliff kippt, eine Staubwolke steigt auf und ein Sturzkegel bleibt liegen
7. **Ein Jahr am Kliff** (Zeitraffer): Sommer mit Strandkörben, Herbststürme, Wintersturmflut mit Abbruch, Frühjahr mit Sandvorspülung, Dünenwanderung
8. **Nacht:** Kampener Leuchtturm mit Sektoren, das Quermarkenfeuer wie früher (historisch, F19), Mondglitzer in der Brandung, Meeresleuchten
9. **Bohlenweg zum Quermarkenfeuer** (Leuchturm4, RotesKliff4): Fahrt durch die Dünen bei grauem Wetter
10. Freie Kamera: Orbit, Zoom, Rundblick, Bodennähe

---

## 7. Erfahrungen aus bisherigen Werkbüchern (übernehmen)

Aus Lighthouse, Haithabu, Glacier, Rungholt und Brocken:

- **Werkbuch zuerst**, als Artefakt „**Sylt Werkbuch**“. Es wird über das ganze Projekt fortgeschrieben (Entscheidungen, Phasenstand, Statusbilder, Datum).
- **Fragen nummeriert**, je mit A/B/C und einer Zeile Begründung. **A ist immer die echte Empfehlung**, denn die Antwort lautet oft „alle A, los!“. Ein zweiter Fragenblock für Oberfläche und Datenbank kommt in der späten Phase.
- **Phasenweise freigeben**, je Phase ein Statusbild `docs/status/Status_PhaseN.png`. Wo nötig, gibt es Teilphasen (5a, 5b). „Bitte_Fixen.png“ kommt sofort in die nächste Phase.
- **Selbst prüfen statt behaupten:** Für jedes Package ein `…Check` ohne Fenster, das mit „Alles in Ordnung.“ endet, `--bild` für Statusbilder, Pixelprüfung und Vorher/Nachher-Bilder. Normalen und Wicklung früh testen. Swing-Panels sind deckend und haben eine ausdrückliche Hintergrundfarbe.
- **Quellen wie in Rungholt:** `docs/QUELLEN.md` und Tabellen in der DB, jeder Befund ist **belegt**, **gemessen** oder **gestaltet [G]**. `[G]` steht auch im Code an jeder gestalteten Zahl.
- **Kinomodus** für die Regiebücher von Anfang an einplanen (Wunsch aus Glacier: „Drehbücher wirken dann noch schöner“).
- **Hörproben** wie in Rungholt (`docs/hoerproben/*.wav` + Bild + `hoerproben.txt`): Brandung je Brechertyp, Sandsturm, Abbruch, Nacht am Leuchtturm.
- **Gelände-Import als Werkzeug** wie `GelaendeImport` in Rungholt: Rohdaten → `daten/gelaende` + Prüfbild `docs/status/Gelaende_Import.png`. Neu ist die Regel aus 2.4 (kein Zugriff auf `C:\Daten` zur Laufzeit).
- **GL-Engine** aus der jüngsten Szene übernehmen (Brocken bzw. Rungholt), nicht neu bauen. FPS-/GPU-Zeit-Anzeige ab Phase 1.
- **FFrame** als Hülle, beim Start maximiert. Bedienelemente aus FStyle in **FTheme-Farben**, die Szene hat ihren eigenen Look. Dialoge über **FOptionPane/FDialog**.
- **Datenbank:** Schema **SYLT** (laut `User.sql`), Präfix `SY_`. Autoinkrement über **Sequence + Trigger, nie IDENTITY**. Spatial in SRID 25832 für Daten aus SH und 8307 wo nötig, mit einer Sicht zur Umrechnung. Verbindung über `db/db.properties`.
- **Git** ab Phase 1. In `.gitignore` gehören `templates/`, `daten/`, `db/db.properties` und `daten/rohdaten.properties`.
- **Schliff** als letzte Phase: GitHub-README mit PNGs in `docs/bilder`, `Sylt.bat`, `Sylt_Konsole.bat`, `Sylt_Verknuepfung.bat`, Programmsymbol `.ico` **mit Goldkante wie `heidelberg.ico`**, Vorschau-Video wie `Rungholt_Vorschau.mp4`, **Dauerlauf** als Abnahme.
- **Rückfluss** wie in Rungholt: vor dem Rückfluss Prüfsummen der Zielprojekte (`vorher_*.md5`), Änderungen als Paket, danach Abnahme im Zielprojekt.
- **Leistung von Anfang an messen:** Ziel ist der Ryzen 7 7735HS mit integrierter Radeon (2 GB VRAM) und 64 GB RAM.

---

## 8. Gliederung des Werkbuchs (zu erstellen)

1. Auftrag und Leitidee („Projekt der Brandung“)
2. Vorlagen, Recherche und was daraus folgt
3. Daten: Rohdaten, Ausschnitt, Aufbereitung, Regel „ohne `C:\Daten`“
4. Grundsatzentscheidungen (Renderer-Ablauf, Engine, Paketvertrag)
5. **Package-Katalog**: `surf`, `dune`, Erweiterungen `cliff`, `fracture`, `reed`, mit Schnittstellen und Abhängigkeitsbild (die App in der Mitte, die Packages ringsum ohne Querverbindungen)
6. Die Physik in Kurzform: Formeln mit Quelle und [G] je Annahme
7. Szene: Kliff, Strand, Dünen, Türme, Bauten, Bewuchs, Leben
8. Regie: Tide, Wetter, Jahreszeit, Sturmflut, Kamera-Presets, Regiebücher, Kinomodus
9. Oberfläche (FFrame, Regler, Presets, Export)
10. Datenbank (später ausgearbeitet)
11. **Phasenplan** mit Lieferumfang und Abnahme je Phase, getrennt nach Renderer und Sylt
12. Rückfluss nach Lighthouse und Thunder
13. Leistungsbudget und Risiken
14. **Offene Fragen** (nummeriert, A/B/C)
15. Ideenspeicher und Zugaben
16. Ausblick (die übrigen Vorschläge aus Brocken: Kieler Förde/Windjammerparade, Lofoten/Polarnacht)
17. Fortschritt (Statusbilder, Freigaben, JAR-Stände, Datum)

---

## 9. Phasenplan — Entwurf (im Werkbuch verfeinern)

„R“ heißt Arbeit im Renderer-Projekt, „S“ heißt Arbeit in Sylt.

| Phase | Ort | Inhalt | Abnahme |
|---|---|---|---|
| 1 | S | Grundgerüst: Git, `.gitignore`, `Avatar.jar` raus, FFrame, GL-Engine übernehmen, Kamera, HUD mit FPS, `wiring`, Startbild mit DB-Anmeldung | Fenster läuft, leere Szene mit Horizont |
| 2 | S | **Datenaufbereitung** (`tools`): DGM1, BAW-Zuschnitt, bDOM, DOP20 (Infrarot → Bewuchs, Sand, nasser Sand), LOD2 → `daten/`; Prüfbilder; `INHALT.txt` | Prüfbilder stimmen mit dem Luftbild, **Start mit umbenanntem `C:\Daten`** gelingt |
| 3 | S | Gelände und Grundbild: Strand (`mudflat.beach`), Dünen, Heide (`ground`), Meer (`ocean` mit `bedTexture`), Himmel und Wolken; Kliff vorerst mit Grundmaterial | Vogelschau und Strandblick wie RotesKliff2 |
| 4 | R | `cliff` erweitern: Schichtprofil, `rotesKliff()`, Runsen, Grasnarben-Überhang, Sturzkegel; Gneis unverändert | `CliffCheck` + neues Bild; Geiranger-Bilder unverändert |
| 4a | S | Kliff in die Szene, Kante aus bDOM | Abend am Kliff wie RotesKliff1 |
| 5 | R | `surf` I: Seegangsfeld über dem Grund (Shoaling, Refraktion, Brechen, Iribarren), Riff und Rinnen [G], Set-up, Längs- und Rückstrom | `SurfCheck`: Grenzfälle gegen Handrechnung (ebener Hang, Green, McCowan); Demo mit Testbecken |
| 6 | R | `surf` II: phasenaufgelöste Brecher (Schwall- und Sturzbrecher), Weißwasserwalze, Schaumteppich, Brandungsschwebung | Demo: drei Brechertypen nebeneinander |
| 7 | R | `surf` III: Auflauf und Rücklauf je Profil, Spülsaum, Ereignisse; Kopplung mit `mudflat` und `spray` über die Demo | Demo: Auflauf auf nassem Sand im Zeitraffer |
| 7a | S | Brandung in der Szene; `Tide.westerland()` | Blick von der Kliffkante wie RotesKliff3 |
| 8 | R | `dune` I: Bagnold, Feuchte-Schwelle, Saltation, Sandfahnen, Kammfahnen, Windrippel | `DuneCheck`: Fluss gegen Bagnold, Schwelle; Demo |
| 9 | R | `dune` II + `reed` Strandhafer: Ablagerung im Bewuchs, Übersandung, Dünenwanderung im Zeitraffer, Sandfangzäune | Demo: eine Düne wandert, Strandhafer wächst durch |
| 9a | S | Dünen und Flugsand in der Szene, Wind aus `wind` | Sandsturm am Strand |
| 10 | S | Türme und Bauten: Quermarkenfeuer, Kampener Leuchtturm, Treppen, Bohlenwege, Strandlokal, Strandkörbe, Buhnen, Häuser aus LOD2; `beacon` (F18) | Ansichten wie Leuchturm1–4 |
| 11 | R | `fracture` erweitern: Kohäsion, Feuchte, Unterspülung, Kippen und Rutschen, Zerfall zu Sand | `FractureCheck`; Glacier-Kalbung unverändert |
| 12 | S | **Sturmflut**: `tide`-Windstau + `wave` bis zum Kliffuß, `surf` schlägt die Hohlkehle, `fracture` bricht, Sturzkegel, Abtrag in der nächsten Flut | Der Abbruch als Bildfolge |
| 13 | S | Wetter, Licht, Leben: `weather`, `rain`, `fog`, `flock` (Möwen, Sanderlinge), Nacht, Meeresleuchten | Nacht und Schlechtwetter wie Leuchturm2/4 |
| 14 | S | `soundscape`: Brandung je Brechertyp und Entfernung, Sand, Wind am Kliff, Abbruch; Hörproben | Hörproben abgenommen |
| 15 | S | Regiebücher + Kinomodus: „Ein Abend am Kliff“, „Die Sturmflut“, „Ein Jahr am Kliff“ | Kino läuft durch |
| 16 | S | Oberfläche + Datenbank (eigener Fragenblock) | Tabellen freigegeben, Speichern/Laden |
| 17 | Lighthouse | Rückfluss: Brandung am Sockel (`surf.sockel()`); Umstellung auf `Renderer.jar` nach F22 | Lighthouse-Dauerlauf, Bild vorher/nachher |
| 18 | Thunder | Rückfluss: **Umstieg auf `Renderer.jar` und GL** (Entscheidung vom 27.10.), Ostsee-Brandung (`surf.ostsee()`), Blitze aus `lightning`, eigener Look und Gewitterstufen bleiben | Thunder-Bilder 0–4 neu gerechnet, gleiche Stimmung |
| 19 | S | Schliff: Qualitätsstufen, READMEs, GitHub-README, Icon, Verknüpfungen, Video, Dauerlauf | „fertig“ |

---

## 10. Fragen-Kandidaten für das Werkbuch

*(Claude formuliert sie im Werkbuch aus, ergänzt sie und setzt jeweils die Empfehlung als A.)*

| Nr. | Frage | A (Empfehlung) | B | C |
|---|---|---|---|---|
| F1 | Ausschnitt | Kern 2,3 × 3,6 km wie in Abschnitt 6, Fernfeld 16 km aus BAW | Nur Kern, Horizont als Kulisse | Bis Westerland |
| F2 | Maßstab und Lage | Echter Maßstab, echte Lage (UTM 32, NHN) | Echter Maßstab, Lage frei | — |
| F3 | Zeitstand des Geländes | Heute (DGM1/DOP 2024), Abbrüche verändern das Gelände während des Laufs (und lassen sich zurücksetzen) | Fest 2024 | — |
| F4 | Daten im Projekt | Dateien in `daten/` (nicht in Git), DB nur für Quellen, Presets und Regie | Gelände als GeoRaster in der DB | Beides |
| F5 | Luftbild | DOP20 nur für Klassen (Sand, nass, Bewuchs, Weg) und zum Eichen der Farben, Bild prozedural | DOP20 als Textur in der Ferne, prozedural in der Nähe | Nur Textur |
| F6 | Engine | GL-Engine aus der jüngsten Szene (Brocken/Rungholt) | Neu aufsetzen | — |
| F7 | Seegang über Grund | Stationäres Wellenfeld auf dem Gitter (Wellenwirkung, Refraktion, Battjes-Janssen), neu gerechnet bei Wechsel von See, Pegel oder Grund | Wellenstrahlen (Ray-Tracing) | Nur Shoaling ohne Refraktion |
| F8 | Auflauf und Rücklauf | 1D-Schwallrechnung je Querprofil (billig, fein) | `wave` im feinen Gitter (2 m) | Nur gestaltet |
| F9 | Brecher im Bild | Netzverformung mit Lippe und Tube nahe der Kamera, in der Ferne Schaum und Normalen | Nur Schaum und Normalen | Partikel |
| F10 | Riff und Rinnen | Aus BAW (10 m), Feinform 2 m gestaltet [G], Riffbögen und Rinnen in typischer Lage | Frei gestaltet | Nur BAW |
| F11 | Flugsand-Modell | Bagnold mit Feuchte-Schwelle und Aufteilung durch Bewuchs | Kawamura/Lettau | Nur Partikel ohne Transport |
| F12 | Dünenwanderung | Zeitraffer im eigenen Faden (Plattenmodell), Echtzeit nur Rippel und Fahnen | Kontinuumsmodell | Keine Wanderung |
| F13 | Heide und Krähenbeere | Über `ground` (niedrige Polster) | Als Arten in `reed` | Weglassen |
| F14 | Kliffaufbau | Recherchiertes Profil (Flugsand, Geschiebelehm, Kaolinsand) mit Findlingen | Vereinfacht: rot oben, weiß unten | Frei |
| F15 | Staub beim Abbruch | Partikel aus `dune` (Sand ist Sand) | Aus `smoke` | Keiner |
| F16 | Sturmflut | `tide`-Windstau + `wave` bis an den Kliffuß, Szenario nach Xaver 2013 (belegte Werte) | Frei gestaltete Sturmflut | Keine Flutdynamik, nur höherer Pegel |
| F17 | Wann bricht das Kliff? | Nach Physik (Unterspülung, Spannung), auslösbar per Knopf für das Regiebuch | Nur nach Drehbuch | Nur nach Physik |
| F18 | `beacon` | Aus Lighthouse in den Renderer übernehmen und erweitern (Iso, Sektoren) | In Sylt neu bauen | Ohne Feuer |
| F19 | Quermarkenfeuer nachts | Wählbar: heute dunkel (Tagesmarke) oder historisch beleuchtet (F WRG, 1913–1975) | Immer dunkel | Immer beleuchtet |
| F20 | Strandkörbe | Nach Jahreszeit (`season`): Sommer verteilt, Winter fort, bei Sturm weggeräumt | Immer da | Keine |
| F21 | Wind aus Brocken | `dune` nimmt den Wind über eigene Schnittstelle; Wünsche an `wind` als Liste an Brocken, keine Änderung an `wind` in Sylt | `wind` in Sylt mit erweitern | — |
| F22 | Lighthouse beim Rückfluss | Auf `Renderer.jar` umstellen, die Kopien fallen weg, `beacon` geht in den Renderer | `surf` als Kopie hinein | Nur Bild |
| F23 | Menschen und Avatar | Keine Menschen, `Avatar.jar` aus `lib/` und Klassenpfad entfernen | Spaziergänger über `figure` in der Ferne | — |
| F24 | Tiere | Möwen, Austernfischer, Sanderlinge am Spülsaum; Seehund nur selten | Nur Möwen | Keine |
| F25 | Tide | Preset `Tide.westerland()` aus Gezeitenkalender (Recherche) | `husum().shifted(…)` [G] | Fester Pegel |
| F26 | Ton | Erzeugt, ohne Audiodateien, Brecher je Typ unterscheidbar | Kein Ton | Audiodateien |
| F27 | Leistungsziel | Hoch: ≥ 30 fps Full HD; Mittel: 60 fps | Nur 30 fps | Frei |
| F28 | Schema | SYLT laut `User.sql`, Präfix `SY_` | DEMO | — |
| F29 | Programmsymbol | Neues `.ico` mit Goldkante wie `heidelberg.ico`, Motiv Quermarkenfeuer vor dem Kliff | Motiv Brecher | — |
| F30 | Thunder nach dem Umstieg | Szene neu auf `Renderer.jar` (`ocean`, `surf.ostsee`, `sky`, `cloud`, `lightning`, `soundscape`), Gewitterstufen und Bedienung bleiben, Schema bleibt DEMO | Eigenes Schema THUNDER | — |

---

## 11. Datenbank-Ideen (für Phase 16, noch nicht ausarbeiten)

- `SY_QUELLE`, `SY_BEFUND`, `SY_BEFUND_QUELLE` wie in Rungholt (belegt / gemessen / gestaltet)
- `SY_STURMFLUT`: belegte Sturmfluten an Sylt mit Scheitel (Pegel Westerland, Hörnum, List) und belegtem Kliffverlust
- `SY_KLIFFKANTE`: Kliffoberkante als Linie (`SDO_GEOMETRY`, SRID 25832) je Stand: gemessen 2024 und jeder Abbruch aus der App. So lässt sich der Rückgang zeigen.
- `SY_VORSPUELUNG`: Sandvorspülungen (Jahr, Menge, Strecke), soweit belegt
- `SY_SEEGANG_PRESET`: benannte Lagen (H_s, T_p, Richtung, Pegel, Wind)
- `SY_SZENARIO` / `SY_SZENARIO_SCHRITT`: Regiebücher (wie Rungholt und Glacier)
- `SY_KAMERA_FAHRT`, `SY_SCHNAPPSCHUSS` (Vorschau als BLOB)
- `SY_LEUCHTFEUER`: beide Türme mit Kennung und Sektoren, gemeinsam nutzbar mit Lighthouse (`LH_LIGHTHOUSE`)

---

## 12. Ideenspeicher (eingebracht von Claude)

- **Rippströmung lesen lernen:** Ein Schalter „DLRG-Blick“ markiert Rinnen und Rückströme, so wie Rettungsschwimmer sie am ruhigen, dunklen Wasser erkennen.
- **Der siebte Brecher:** Wellengruppen aus der Brandungsschwebung machen Serien sichtbar. Ein Zähler zeigt die Serie an.
- **Die Welle von unten:** Kamera unter Wasser im Sturzbrecher, mit Luftblasen und dem hellen Kamm über einem (`ocean` hat `underwater.frag`)
- **Sanderlinge** rennen vor jeder Welle her und hinter jeder zurück, gekoppelt an `onSwash`
- **Spuren im Sand:** Möwen- und Vogeltritte im nassen Sand (`mudflat.addTrack`), die der nächste Auflauf löscht
- **Schaumblasen** im Spülsaum glänzen in Regenbogenfarben und platzen
- **Sand singt:** Im Sandsturm rauscht der Sand am Ohr anders als die Brandung (`soundscape`)
- **Kliffkante 2024 gegen heute:** Nach einem Abbruch zeigt eine Linie in der Szene, wo die Kante vorher lag (`SY_KLIFFKANTE`)
- **Findlinge** fallen beim Abbruch aus dem Geschiebelehm und bleiben am Strand liegen, als die Spur der Eiszeit
- **Grünes Leuchten** der Sonne beim Untergang über der Nordsee (selten, zuschaltbar)
- **Meeresleuchten** in Sommernächten, wo die Brecher stürzen
- **Seerauch** über der Brandung an kalten Wintermorgen (`fog`)
- **Brandung als Messung:** Ein Fenster zeigt das Brechertypen-Diagramm (Iribarren) und ein Pegelbild (Schwall, Set-up), passend zum Bild
- **Sandvorspülung im Zeitraffer:** Ein Spülrohr legt einen neuen Sandkörper vor das Kliff, und die Brandung formt ihn in den Folgewochen um

---

## 13. Arbeitsanweisung an Claude

1. **Projektordner sichten** (Abschnitt 2): Sylt, Renderer (READMEs aller Packages in Abschnitt 2.2, `muster`, `tools/PaketPruefung.java`), Rungholt (`GelaendeImport`, `QUELLEN.md`, `tide` und `wave` in Gebrauch), Lighthouse (`beacon`, Aufbau `src`), Thunder (Aufbau, Stimmungen in `docs/`).
2. **Vorlagenbilder ansehen** (Abschnitt 3).
3. **Kurz recherchieren** (Liste in Abschnitt 3) und alles mit Quelle in die Fragen und in den Abschnitt „Physik in Kurzform“ übernehmen.
4. Das **„Sylt Werkbuch“** als Artefakt anlegen, nach der Gliederung in Abschnitt 8, mit Paketvertrag, Package-Katalog, Phasenplan und **nummerierten Fragen** (A = Empfehlung).
5. **Keinen Code und keine Skripte schreiben**, bevor die Fragen beantwortet sind und Phase 1 freigegeben ist.
6. Nach jeder Phase das Werkbuch fortschreiben, ein Statusbild ablegen und den Stand des `Renderer.jar` notieren.
7. Die Regel „ohne `C:\Daten`“ (2.4) in jeder Phase einhalten. Wer eine neue Rohdatei braucht, ergänzt sie in den Werkzeugen und in `QUELLEN.md`, nie im Laufzeitcode.
