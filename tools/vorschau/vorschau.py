"""
Sylt – Vorschauvideo (30 s, 1920x1080, 30 Bilder/s), Phase 19b (F55 A, nach Rungholts vorschau.py).

Anders als bei Rungholt sind die Einstellungen keine Standbilder, sondern Bildfolgen aus der App: `ant vorschaubilder`
rechnet sie auf der Grafikkarte nach docs/bilder/clip_*/bild_0000.jpg … (6 s, 30 Bilder/s, ohne HUD). Dazu kommt der
Abbruch des Kliffs als Folge von Standbildern (docs/bilder/Abbruch_2..6.png ohne HUD, sonst docs/status aus `ant abbruch`) und der Ton aus den
Hörproben (docs/hoerproben/*.wav aus `ant hoerproben`), gemischt mit einem leisen Grundton.

Übergänge: Die neue Einstellung läuft wie ein Schwall über den Strand von links ins Bild, mit einem Saum aus Gischt.
Unten rechts eine Tafel mit Ort, Tag, Uhrzeit und Wind der Einstellung (die Werte, mit denen sie gerechnet wurde),
bei der Sturmflut der Pegel.

Aufruf (aus dem Projektverzeichnis):
    python3 tools/vorschau/vorschau.py [ausgabe.mp4] [--bilder]
Braucht Python 3 mit Pillow und NumPy sowie ffmpeg im PATH. Mit --bilder nur zehn Probebilder neben der Ausgabe.
Schriften: Caladea (Titel) und Inter (Beschriftung); fehlen sie, nimmt es DejaVu.
Fehlt eine Bildfolge, nimmt es das Standbild docs/bilder/<Name>.png mit einer langsamen Fahrt (Ken Burns).
"""
import glob
import math
import os
import subprocess
import sys
import wave

import numpy as np
from PIL import Image, ImageDraw, ImageFont

W, H, FPS, DAUER = 1920, 1080, 30, 30.0
N = int(DAUER * FPS)
WURZEL = os.environ.get("SYLT_WURZEL") or os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
BILDER = os.path.join(WURZEL, "docs", "bilder")
STATUS = os.path.join(WURZEL, "docs", "status")
HOER = os.path.join(WURZEL, "docs", "hoerproben")
ARGS = [a for a in sys.argv[1:] if not a.startswith("--")]
AUSGABE = ARGS[0] if ARGS else os.path.join(WURZEL, "docs", "video", "Sylt_Vorschau.mp4")

# ---------------------------------------------------------------- Schriften

def schrift(namen, groesse):
    orte = ["/usr/share/fonts/truetype/crosextra", "/usr/share/fonts/opentype/inter",
            "/usr/share/fonts/truetype/dejavu", "C:/Windows/Fonts"]
    for n in namen:
        for o in orte:
            p = os.path.join(o, n)
            if os.path.exists(p):
                return ImageFont.truetype(p, groesse)
    return ImageFont.load_default(groesse)

SERIF = ["Caladea-Regular.ttf", "DejaVuSerif.ttf", "georgia.ttf"]
SERIF_K = ["Caladea-Italic.ttf", "DejaVuSerif-Italic.ttf", "georgiai.ttf"]
SANS_L = ["Inter-Light.otf", "DejaVuSans-ExtraLight.ttf", "segoeuil.ttf"]
SANS_M = ["Inter-Medium.otf", "DejaVuSans.ttf", "segoeui.ttf"]

F_TITEL = schrift(SERIF, 190)
F_UNTER = schrift(SERIF_K, 52)
F_ZEILE = schrift(SERIF, 62)
F_KAPITEL = schrift(SANS_M, 22)
F_HUD = schrift(SANS_M, 18)
F_HUD_WERT = schrift(SANS_L, 40)
F_KLEIN = schrift(SANS_L, 22)

# ---------------------------------------------------------------- Farben

SAND = (238, 228, 206)
KLIFF = (205, 92, 58)       # das Rote Kliff
SEE = (110, 170, 190)
GISCHT = np.array([245, 248, 250], dtype=np.float32)
NACHT = np.array([10, 16, 26], dtype=np.float32)

# ---------------------------------------------------------------- Hilfen

def clamp(x, a=0.0, b=1.0):
    return max(a, min(b, x))

def glatt(x):
    x = clamp(x)
    return x * x * (3 - 2 * x)

def aus(x):
    x = clamp(x)
    return 1 - (1 - x) ** 3

def ein_aus(x):
    x = clamp(x)
    return 4 * x ** 3 if x < 0.5 else 1 - (-2 * x + 2) ** 3 / 2

def fenster(t, a, b, rein=0.5, raus=0.5):
    if t < a or t > b:
        return 0.0
    return min(aus((t - a) / rein) if rein > 0 else 1.0, glatt((b - t) / raus) if raus > 0 else 1.0)

def auf_format(img):
    """Auf 1920x1080 füllen (Mitte), RGB."""
    img = img.convert("RGB")
    s = max(W / img.width, H / img.height)
    if abs(s - 1) > 1e-3:
        img = img.resize((round(img.width * s), round(img.height * s)), Image.LANCZOS)
    x0, y0 = (img.width - W) // 2, (img.height - H) // 2
    return img.crop((x0, y0, x0 + W, y0 + H))

# ---------------------------------------------------------------- Einstellungen

class Einstellung:
    """Eine Bildfolge (oder ersatzweise ein Standbild mit Fahrt) von a bis b Sekunden."""

    def __init__(self, name, a, b, kapitel, zeile, tafel, zoom=(1.0, 1.06), stand=None):
        self.a, self.b = a, b
        self.kapitel, self.zeile, self.tafel = kapitel, zeile, tafel
        self.zoom = zoom
        self.folge = sorted(glob.glob(os.path.join(BILDER, name, "bild_*.jpg")))
        self.stand = None
        self.cache = {}
        if not self.folge:
            p = stand
            if p is None:   # das Standbild gleichen Namens, Groß- und Kleinschreibung egal (clip_brecher → Brecher.png)
                kurz = name.replace("clip_", "").lower()
                treffer = [q for q in glob.glob(os.path.join(BILDER, "*.png")) if os.path.basename(q).lower().startswith(kurz)]
                p = treffer[0] if treffer else None
            if p is None or not os.path.exists(p):
                kandidaten = glob.glob(os.path.join(BILDER, "*.png"))
                p = kandidaten[0] if kandidaten else None
            self.stand = auf_format(Image.open(p)) if p else Image.new("RGB", (W, H), tuple(int(x) for x in NACHT))
            print(f"  {name}: keine Bildfolge, Standbild {p}")
        else:
            print(f"  {name}: {len(self.folge)} Bilder")

    def _lade(self, i):
        if i not in self.cache:
            if len(self.cache) > 6:
                self.cache.pop(next(iter(self.cache)))
            self.cache[i] = np.asarray(auf_format(Image.open(self.folge[i])), dtype=np.float32)
        return self.cache[i]

    def bild(self, t):
        u = clamp((t - self.a) / (self.b - self.a))
        if self.folge:
            # die Folge über die ganze Einstellung strecken; zwischen zwei Bildern überblenden
            x = u * (len(self.folge) - 1)
            i = int(math.floor(x))
            f = x - i
            a = self._lade(i)
            out = a if f < 1e-3 or i + 1 >= len(self.folge) else a * (1 - f) + self._lade(i + 1) * f
        else:
            out = None
        # leichte Fahrt in das Bild (auch auf der Folge, wie eine Kamera, die nachrückt)
        z = self.zoom[0] + (self.zoom[1] - self.zoom[0]) * ein_aus(u)
        src = self.stand if out is None else None
        if src is not None:
            bw, bh = W / z, H / z
            x0, y0 = (W - bw) / 2, (H - bh) / 2
            return np.asarray(src.resize((W, H), Image.BICUBIC, box=(x0, y0, x0 + bw, y0 + bh)), dtype=np.float32)
        if z > 1.001:
            bw, bh = W / z, H / z
            x0, y0 = (W - bw) / 2, (H - bh) / 2
            img = Image.fromarray(np.clip(out, 0, 255).astype(np.uint8))
            return np.asarray(img.resize((W, H), Image.BICUBIC, box=(x0, y0, x0 + bw, y0 + bh)), dtype=np.float32)
        return out


class Abbruchfolge(Einstellung):
    """Die Standbilder des Abbruchs nacheinander, je mit kurzer Überblendung und kleiner Fahrt."""

    def __init__(self, a, b, kapitel, zeile, tafel):
        self.a, self.b = a, b
        self.kapitel, self.zeile, self.tafel = kapitel, zeile, tafel
        # zuerst die Folge ohne HUD aus `ant vorschaubilder` (docs/bilder), sonst die aus `ant abbruch` (docs/status)
        namen = []
        for ordner in (BILDER, STATUS):
            namen = [n for n in (os.path.join(ordner, f"Abbruch_{k}.png") for k in range(2, 7)) if os.path.exists(n)]
            if namen:
                break
        namen = namen or glob.glob(os.path.join(BILDER, "Sturmflut.png"))
        self.bilder = [auf_format(Image.open(n)) for n in namen]
        print(f"  Abbruch: {len(self.bilder)} Standbilder")

    def _fahrt(self, k, u):
        z = 1.0 + 0.07 * u
        bw, bh = W / z, H / z
        x0, y0 = (W - bw) / 2 + 30 * u, (H - bh) / 2
        return np.asarray(self.bilder[k].resize((W, H), Image.BICUBIC, box=(x0, y0, x0 + bw, y0 + bh)), dtype=np.float32)

    def bild(self, t):
        n = len(self.bilder)
        x = clamp((t - self.a) / (self.b - self.a)) * n
        k = min(n - 1, int(x))
        u = x - k
        out = self._fahrt(k, u)
        if u > 0.82 and k + 1 < n:   # die letzten 18 % jedes Bildes blenden ins nächste
            f = glatt((u - 0.82) / 0.18)
            out = out * (1 - f) + self._fahrt(k + 1, 0) * f
        return out


EINST = [
    Einstellung("clip_abend", 0.0, 7.6, "01  ·  AM KLIFF", "Das Rote Kliff von Wenningstedt",
                ("WENNINGSTEDT · 20.07.2026", "20:30", "Wind 6 m/s aus West")),
    Einstellung("clip_brecher", 7.0, 11.8, "02  ·  BRANDUNG", "Jeder Brecher gerechnet",
                ("SPÜLSAUM · 20.09.2026", "15:00", "Dünung 3,5 m · 10 s")),
    Einstellung("clip_drohne", 11.2, 16.0, "03  ·  VON OBEN", "Mit der Drohne das Kliff entlang",
                ("KAMPEN · 20.07.2026", "17:00", "45 m über der See")),
    Einstellung("clip_sturmflut", 15.4, 19.8, "04  ·  XAVER", "Sturmflut am 5. Dezember 2013",
                ("PEGEL WENNINGSTEDT · 05.12.2013", "+2,60 m", "Wind 22 m/s aus WSW")),
    Abbruchfolge(19.2, 23.6, "05  ·  ABBRUCH", "Die Kehle wächst, die Wand bricht",
                 ("ROTES KLIFF · 06.12.2013", "11:00", "N 6 090 335")),
    Einstellung("clip_nacht", 23.0, 27.6, "06  ·  NACHT", "Das Kampener Feuer kreist",
                ("KAMPEN · 15.08.2026", "23:30", "Kennung Iso WR 7s")),
]
UEBERGANG = 0.6
STURM = 3   # Index der Sturmflut (Tafel in Kliffrot)

# ---------------------------------------------------------------- Schwall (Übergang)

XS = np.arange(W, dtype=np.float32)[None, :]
YS = np.arange(H, dtype=np.float32)[:, None]

def schwallmaske(p, phase):
    """Maske 0..1: die neue Einstellung läuft wie ein Schwall von links ins Bild; dazu die Gischtkante."""
    if p <= 0:
        return np.zeros((1, W), np.float32), None
    if p >= 1:
        return np.ones((1, W), np.float32), None
    vorne = -260 + (W + 520) * ein_aus(p)
    # der Saum: unten (am Wasser) weiter vorn als oben, mit Zungen
    saum = (vorne - 160 * (YS / H - 0.5)
            + (34 * np.sin(YS / 61 + phase * 5) + 17 * np.sin(YS / 23 - phase * 8)) * math.sin(math.pi * p))
    m = np.clip((saum - XS) / 10 + 0.5, 0, 1)
    gischt = np.exp(-((XS - saum) / 9.0) ** 2) * 0.75 * math.sin(math.pi * p)
    return m.astype(np.float32), gischt.astype(np.float32)

# ---------------------------------------------------------------- Vignette, Korn

yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
rr = np.sqrt(((xx - W / 2) / (W / 2)) ** 2 + ((yy - H / 2) / (H / 2)) ** 2)
VIGNETTE = (1 - 0.34 * np.clip(rr - 0.5, 0, 1) ** 1.6)[..., None].astype(np.float32)
del yy, xx, rr
RNG = np.random.default_rng(2013)
KORN = [RNG.normal(0, 1.5, (H // 2, W // 2)).astype(np.float32) for _ in range(8)]

# ---------------------------------------------------------------- Texte

def gesperrt(d, xy, text, font, fill, sperr, anker="la"):
    breiten = [d.textlength(c, font=font) for c in text]
    gesamt = sum(breiten) + sperr * (len(text) - 1)
    x, y = xy
    if anker == "ma":
        x -= gesamt / 2
    for c, b in zip(text, breiten):
        d.text((x, y), c, font=font, fill=fill)
        x += b + sperr
    return gesamt

def zeichne_zeile(d, e, t):
    a = fenster(t, max(e.a, 3.2) + 0.5, e.b - 0.35, 0.7, 0.45)
    if a <= 0:
        return
    beginn = max(e.a, 3.2)
    hoch = (1 - aus((t - beginn - 0.5) / 0.9)) * 30
    x, y = 120, H - 250 + hoch
    laenge = 70 * aus((t - beginn - 0.4) / 0.8)
    d.line((x, y - 22, x + laenge, y - 22), fill=(*KLIFF, int(255 * a)), width=3)
    gesperrt(d, (x, y), e.kapitel, F_KAPITEL, (*SAND, int(220 * a)), 6)
    d.text((x + 2, y + 44), e.zeile, font=F_ZEILE, fill=(0, 0, 0, int(120 * a)))
    d.text((x, y + 42), e.zeile, font=F_ZEILE, fill=(255, 255, 255, int(255 * a)))

def zeichne_tafel(d, t):
    """Unten rechts: Ort und Tag, groß die Uhrzeit (bei der Sturmflut der Pegel), darunter Wind oder See."""
    for k, e in enumerate(EINST):
        a = fenster(t, max(e.a, 3.4) + 0.3, e.b - 0.3, 0.5, 0.4)
        if a <= 0:
            continue
        x0, y0, bw, bh = W - 500, H - 200, 430, 128
        alpha = int(255 * a)
        farbe = KLIFF if k == STURM else SEE
        d.rectangle((x0, y0, x0 + bw, y0 + bh), fill=(8, 14, 22, int(125 * a)))
        d.line((x0, y0, x0 + 46, y0), fill=(*farbe, alpha), width=3)
        oben, wert, unten = e.tafel
        d.text((x0 + 16, y0 + 12), oben, font=F_HUD, fill=(*SAND, int(200 * a)))
        if k == STURM:
            # der Pegel steigt in der Einstellung bis +2,60 m
            p = 2.6 * (0.55 + 0.45 * aus((t - e.a) / (e.b - e.a) * 1.6))
            wert = f"+{p:.2f} m".replace(".", ",")
        d.text((x0 + 16, y0 + 38), wert, font=F_HUD_WERT, fill=(*SAND, alpha))
        d.text((x0 + 16, y0 + 92), unten, font=F_KLEIN, fill=(*SAND, int(170 * a)))
        # rechts eine kleine Welle, die läuft
        pts = [(x0 + 250 + i * 2.2, y0 + 70 + 10 * math.sin(i / 9 - t * 3) * (1.8 if k == STURM else 1))
               for i in range(76)]
        d.line(pts, fill=(*farbe, int(150 * a)), width=2)

def titelkarte(d, a, unter, extra=None):
    if a <= 0:
        return
    breite = gesperrt(d, (0, -600), "SYLT", F_TITEL, (0, 0, 0, 0), 46)
    x = W / 2 - breite / 2
    for i, c in enumerate("SYLT"):
        u = aus((a * 1.6) - i * 0.09)
        yb = H / 2 - 150 + (1 - u) * 50
        d.text((x, yb), c, font=F_TITEL, fill=(*SAND, int(255 * u * a)))
        x += d.textlength(c, font=F_TITEL) + 46
    lw = 520 * aus(a * 1.3 - 0.2)
    d.line((W / 2 - lw / 2, H / 2 + 72, W / 2 + lw / 2, H / 2 + 72), fill=(*KLIFF, int(255 * a)), width=3)
    au = clamp(a * 1.5 - 0.5)
    d.text((W / 2, H / 2 + 96), unter, font=F_UNTER, fill=(*SAND, int(230 * au)), anchor="ma")
    if extra:
        gesperrt(d, (W / 2, H / 2 + 182), extra, F_KAPITEL, (*SAND, int(170 * au)), 6, "ma")

def saumlinie(d, t, a, y):
    """Ein laufender Spülsaum quer durchs Bild (Titel und Ende)."""
    if a <= 0:
        return
    pts = []
    for i in range(0, int(W * aus(a * 1.2)) + 1, 8):
        pts.append((i, y + 12 * math.sin(i / 170 - t * 1.8) + 5 * math.sin(i / 51 + t * 2.7)))
    if len(pts) > 1:
        d.line(pts, fill=(*SAND, int(150 * a)), width=2)

# ---------------------------------------------------------------- Bild

def hintergrund(t):
    aktiv = [e for e in EINST if e.a - 1e-6 <= t <= e.b]
    if not aktiv:
        return None
    out = aktiv[0].bild(t)
    for e in aktiv[1:]:
        p = (t - e.a) / UEBERGANG
        m, gischt = schwallmaske(p, t)
        neu = e.bild(t)
        out = out * (1 - m[..., None]) + neu * m[..., None]
        if gischt is not None:
            out = out * (1 - gischt[..., None]) + GISCHT * gischt[..., None]
    return out

def frame(i):
    t = i / FPS
    bg = hintergrund(t)
    if bg is None:
        bg = np.broadcast_to(NACHT, (H, W, 3)).copy()
    dunkel = max(0.62 * (1 - glatt((t - 2.6) / 0.9)),     # unter dem Titel gedämpft
                 glatt((t - 27.2) / 0.8) * 0.8)             # in die Endtafel
    bg = bg * (1 - dunkel) + NACHT * dunkel
    bg = bg * VIGNETTE
    k = KORN[i % len(KORN)]
    bg = bg + np.repeat(np.repeat(k, 2, 0), 2, 1)[..., None]
    img = Image.fromarray(np.clip(bg, 0, 255).astype(np.uint8)).convert("RGBA")

    lage = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(lage)
    titelkarte(d, fenster(t, 0.2, 3.2, 1.6, 0.6), "Das Rote Kliff in Echtzeit-3D")
    saumlinie(d, t, fenster(t, 0.0, 3.2, 1.2, 0.6), H / 2 + 300)
    for e in EINST:
        zeichne_zeile(d, e, t)
    zeichne_tafel(d, t)
    a = fenster(t, 27.5, 30.5, 1.3, 0.1)
    titelkarte(d, a, "Brandung, Sturmflut, Abbruch – in Echtzeit gerechnet", "JAVA 21  ·  JOGL  ·  OPENGL 4.3")
    saumlinie(d, t, fenster(t, 27.4, 30.5, 1.2, 0.1), H / 2 + 300)
    d.rectangle((0, 0, W, 44), fill=(0, 0, 0, 255))
    d.rectangle((0, H - 44, W, H), fill=(0, 0, 0, 255))
    img = Image.alpha_composite(img, lage).convert("RGB")
    if t > 29.4:
        img = Image.blend(img, Image.new("RGB", (W, H)), glatt((t - 29.4) / 0.6))
    if t < 0.4:
        img = Image.blend(Image.new("RGB", (W, H)), img, glatt(t / 0.4))
    return img

# ---------------------------------------------------------------- Klang

def wav(name, sr):
    """Eine Hörprobe als Stereo-Array (float, -1..1) in der Rate sr; fehlt sie, Stille."""
    p = os.path.join(HOER, name)
    if not os.path.exists(p):
        print(f"  Ton: {name} fehlt, Stille")
        return np.zeros((int(DAUER * sr), 2))
    with wave.open(p, "rb") as w:
        ch, rate, n = w.getnchannels(), w.getframerate(), w.getnframes()
        a = np.frombuffer(w.readframes(n), dtype=np.int16).astype(np.float64) / 32768
    a = a.reshape(-1, ch)
    if ch == 1:
        a = np.repeat(a, 2, axis=1)
    if rate != sr:
        x = np.arange(int(len(a) * sr / rate)) * rate / sr
        a = np.stack([np.interp(x, np.arange(len(a)), a[:, c]) for c in range(2)], axis=1)
    return a

def spur(a, von, bis, ab=0.0, rein=0.8, raus=1.0, sr=48000):
    """Ausschnitt ab Sekunde ab der Probe, gelegt auf von..bis, mit weichen Rändern."""
    n = int(DAUER * sr)
    out = np.zeros((n, 2))
    i0, i1 = int(von * sr), min(n, int(bis * sr))
    s0 = int(ab * sr)
    stueck = a[s0:s0 + (i1 - i0)]
    out[i0:i0 + len(stueck)] = stueck
    t = np.arange(n) / sr
    g = np.clip((t - von) / rein, 0, 1) * np.clip((bis - t) / raus, 0, 1)
    return out * g[:, None]

def klang(pfad, sr=48000):
    n = int(DAUER * sr)
    t = np.arange(n) / sr
    abend = wav("1_Abend_am_Spuelsaum.wav", sr)
    west = wav("2_Westwind_am_Strand.wav", sr)
    xaver = wav("7_Abbruch_bei_Xaver.wav", sr)
    def norm(x):
        return x / (np.max(np.abs(x)) + 1e-9)
    abend, west, xaver = norm(abend), norm(west), norm(xaver)
    mix = (0.55 * spur(abend, 0.0, 8.0, 2.0, 1.5, 1.2, sr)
           + 0.6 * spur(west, 7.0, 16.4, 5.0, 0.8, 1.0, sr)
           + 0.8 * spur(xaver, 15.2, 24.0, 4.0, 0.6, 1.2, sr)
           + 0.4 * spur(abend, 23.0, 30.0, 20.0, 1.2, 2.0, sr))
    # Grundton: D2 / A2 / D3, leise schwebend; unter dem Sturm tiefer und dunkler
    grund = sum(g * np.sin(2 * np.pi * f * t + 0.3 * np.sin(2 * np.pi * 0.08 * t))
                for f, g in ((73.4, 0.32), (110.0, 0.2), (146.8, 0.12), (220.0, 0.04)))
    grund *= np.clip(t / 3, 0, 1) * np.clip((DAUER - t) / 2.5, 0, 1)
    grund *= 1 - 0.6 * np.clip((t - 15.4) / 0.6, 0, 1) * np.clip((23.6 - t) / 0.6, 0, 1)
    mix += 0.22 * np.stack([grund, np.roll(grund, 300)], axis=1)
    # ein dumpfer Schlag beim Abbruch (zweites Bild der Folge)
    for tb in (20.1, 21.0):
        tt = t - tb
        m = tt >= 0
        schlag = np.zeros(n)
        schlag[m] = np.sin(2 * np.pi * 42 * tt[m]) * np.exp(-tt[m] * 4.5) * (1 - np.exp(-tt[m] * 60))
        mix += 0.5 * schlag[:, None]
    mix *= (np.clip(t / 0.8, 0, 1) * np.clip((DAUER - t) / 1.2, 0, 1))[:, None]
    mix /= np.max(np.abs(mix)) + 1e-9
    mix *= 0.85
    pcm = (mix * 32767).astype(np.int16)
    with wave.open(pfad, "wb") as w:
        w.setnchannels(2)
        w.setsampwidth(2)
        w.setframerate(sr)
        w.writeframes(pcm.tobytes())

# ---------------------------------------------------------------- Ausgabe

def main():
    os.makedirs(os.path.dirname(os.path.abspath(AUSGABE)), exist_ok=True)
    if "--bilder" in sys.argv:
        for s in (1.6, 5, 7.3, 10, 13.5, 15.7, 18, 20.5, 23.3, 25.5, 28.6):
            frame(int(s * FPS)).save(AUSGABE + f".{s:04.1f}s.jpg", quality=88)
        print("Probebilder neben", AUSGABE)
        return
    ton = AUSGABE + ".klang.wav"
    klang(ton)
    cmd = ["ffmpeg", "-y", "-loglevel", "error", "-f", "rawvideo", "-pix_fmt", "rgb24",
           "-s", f"{W}x{H}", "-r", str(FPS), "-i", "-", "-i", ton,
           "-c:v", "libx264", "-preset", "slow", "-crf", "21", "-pix_fmt", "yuv420p",
           "-movflags", "+faststart", "-c:a", "aac", "-b:a", "160k", "-shortest", AUSGABE]
    ff = subprocess.Popen(cmd, stdin=subprocess.PIPE)
    for i in range(N):
        ff.stdin.write(frame(i).tobytes())
        if i % 60 == 0:
            print(f"{i / FPS:5.1f} s", flush=True)
    ff.stdin.close()
    ff.wait()
    os.remove(ton)
    print("fertig:", AUSGABE)

if __name__ == "__main__":
    main()
