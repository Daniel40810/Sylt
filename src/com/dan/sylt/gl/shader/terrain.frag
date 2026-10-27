// terrain.frag — das Gelände (Sylt Phase 1, aus Brocken entschlackt): Farbe nach der Art des Grunds (Terrain.ART; in
// Phase 1 aus der Bühne, ab Phase 2 aus DOP20), das Kliff nach Neigung und Höhe in zwei Schichten (oben rot oxidierter
// Geschiebelehm, unten heller Kaolinsand [G], Phase 4 ersetzt das durch cliff), dazu die Marken (marken.glsl).
// Unter dem Wasser zeichnet meer.frag (Platzhalter bis ocean in Phase 3). Licht: ohne com.dan.sky die Sonne und der
// Himmelsverlauf aus common.glsl; der Zweig uSkyMode = 1 (com.dan.sky, Phase 3) ist aus Brocken vorbereitet.
#include "common.glsl"
#include "terrain_height.glsl"
#include "marken.glsl"
in vec3 vWorld;
in vec3 vNormal;
in float vDist;
out vec4 frag;

vec3 srgb(vec3 c) { return pow(c, vec3(2.2)); }

float hash2(vec2 i) { return fract(sin(dot(i, vec2(127.1, 311.7))) * 43758.5453); }
float vnoise(vec2 x) {
    vec2 i = floor(x), f = x - i;
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash2(i), hash2(i + vec2(1, 0)), f.x), mix(hash2(i + vec2(0, 1)), hash2(i + vec2(1, 1)), f.x), f.y);
}
// Flecken der Größe s (m); in der Ferne (Bildpunkt pxm m) zum Mittel 0,5
float fleck(vec2 p, float s, float pxm) { return mix(vnoise(p / s), 0.5, smoothstep(0.15 * s, 0.6 * s, pxm)); }

// Farben je Art [G nach RotesKliff1–5 und Leuchturm1–4; Phase 2 eicht sie am DOP20]. Albedo linear: trockener Sand
// rund 0,35–0,45, nasser 0,2, Heide 0,1 (Befund Phase 1: mit den hellen Werten aus Brocken war der Strand im Abendlicht weiß)
vec3 artColor(uint a, vec2 p, float pxm) {
    float f = fleck(p, 6.0, pxm), g = fleck(p + 17.0, 25.0, pxm);
    if (a == 1u) return srgb(vec3(0.58, 0.53, 0.42));                                        // See: Sand unter Wasser
    if (a == 2u) return srgb(mix(vec3(0.38, 0.36, 0.31), vec3(0.45, 0.42, 0.36), f));         // Watt
    if (a == 3u) return srgb(mix(vec3(0.68, 0.63, 0.53), vec3(0.73, 0.69, 0.60), f));         // Strand: heller Sand
    if (a == 4u) return srgb(mix(vec3(0.50, 0.45, 0.36), vec3(0.55, 0.50, 0.40), f));         // nasser Sand
    if (a == 5u) return srgb(vec3(0.64, 0.36, 0.21));                                         // Kliff (Wand nach Höhe unten)
    if (a == 6u) return srgb(mix(vec3(0.66, 0.62, 0.50), vec3(0.50, 0.54, 0.40), 0.55 * g)); // Düne: Sand mit Strandhafer
    if (a == 7u) return srgb(mix(vec3(0.31, 0.31, 0.21), vec3(0.37, 0.30, 0.31), 0.45 * f)); // Heide und Krähenbeere
    if (a == 8u) return srgb(mix(vec3(0.22, 0.30, 0.18), vec3(0.28, 0.34, 0.20), f));         // Gebüsch, Kartoffelrose
    if (a == 9u) return srgb(mix(vec3(0.45, 0.55, 0.32), vec3(0.52, 0.58, 0.36), f));         // Wiese, Weide
    if (a == 10u) return srgb(mix(vec3(0.52, 0.48, 0.42), vec3(0.42, 0.40, 0.33), g));        // Siedlung: Reet, Gärten
    if (a == 11u) return srgb(vec3(0.78, 0.74, 0.64));                                        // Weg
    return srgb(vec3(0.55, 0.55, 0.45));
}

void main() {
    vec2 p = vWorld.xz;
    // Normale im Bildpunkt aus der Höhe (fein in der Nähe, gröber in der Ferne), nie enger als das Raster an diesem Ort
    float kern = uKernOn == 1 ? smoothstep(0.0, uKNaht, insideOf(p, uKernGrid, uKernN)) : 0.0;
    float e = clamp(vDist * 0.003, mix(uGrid.z * 0.8, 1.0, kern), 80.0);
    float lvl = heightLevel(vDist) - 1.0;
    float hx = heightAt(p + vec2(e, 0.0), lvl) - heightAt(p - vec2(e, 0.0), lvl);
    float hz = heightAt(p + vec2(0.0, e), lvl) - heightAt(p - vec2(0.0, e), lvl);
    vec3 n = normalize(mix(vNormal, normalize(vec3(-hx, 2.0 * e, -hz)), 1.0 - smoothstep(4000.0, 12000.0, vDist)));
    float pxm = length(fwidth(p));

    // Art weich an den Rändern: die vier nächsten Zellen, bilinear gewichtet
    vec3 alb = vec3(0.0);
    {
        vec2 u = (p - uGrid.xy) / uGrid.z - 0.5;
        ivec2 i = ivec2(floor(u));
        vec2 f = u - vec2(i);
        bool drin = insideDist(p) > 0.0;
        for (int b = 0; b <= 1; b++)
            for (int c = 0; c <= 1; c++) {
                ivec2 q = clamp(i + ivec2(c, b), ivec2(0), ivec2(int(uGridN) - 1));
                uint a = drin ? texelFetch(uSource, q, 0).r : (vWorld.y < 0.0 ? 1u : 9u);
                float wt = (c == 0 ? 1.0 - f.x : f.x) * (b == 0 ? 1.0 - f.y : f.y);
                alb += artColor(a, p, pxm) * wt;
            }
    }
    // steile Wand: das Kliff in Schichten [G]: unten heller Kaolinsand, darüber rot oxidierter Geschiebelehm, oben
    // eine dünne Lage Flugsand und die Grasnarbe; Phase 4 ersetzt das durch das Schichtprofil aus cliff (F14 A)
    float wand = smoothstep(0.75, 0.45, n.y);
    if (wand > 0.0) {
        float rel = clamp((vWorld.y - 3.0) / 25.0, 0.0, 1.0);
        float bandRausch = fleck(vec2(p.x + p.y, vWorld.y * 6.0), 2.0, pxm);
        vec3 kaolin = srgb(mix(vec3(0.74, 0.71, 0.64), vec3(0.67, 0.63, 0.55), bandRausch));
        vec3 lehm = srgb(mix(vec3(0.58, 0.30, 0.17), vec3(0.49, 0.25, 0.13), bandRausch));
        vec3 wandFarbe = mix(kaolin, lehm, smoothstep(0.30, 0.42, rel + 0.06 * (bandRausch - 0.5)));
        alb = mix(alb, wandFarbe, wand);
    }
    // Fernring: außerhalb des Hauptrasters eine Mischfarbe nach der Höhe
    float fern = 1.0 - smoothstep(0.0, uNaht, insideDist(p));
    alb = mix(alb, mix(srgb(vec3(0.62, 0.58, 0.46)), srgb(vec3(0.40, 0.46, 0.30)), smoothstep(1.0, 6.0, vWorld.y)), fern);

    vec3 col;
    float B;
    if (uSkyMode == 1) {
        vec3 direct = sunLight() * max(dot(n, uSun), 0.0) + moonLight() * max(dot(n, uMoonDir), 0.0);
        vec3 amb = uSkyAmb * (0.6 + 0.4 * n.y);
        col = alb * (direct / 3.14159265 + amb);
        B = 0.32 * dot(sunLight() * max(uSun.y, 0.0) / 3.14159265 + uSkyAmb + moonLight() * max(uMoonDir.y, 0.0) / 3.14159265, vec3(0.2126, 0.7152, 0.0722));
    } else {
        vec3 L = sunLight();
        float sun = max(dot(n, uSun), 0.0);
        B = skyBrightness(sunElevDeg());
        vec3 amb = vec3(0.30, 0.38, 0.52) * B / 0.32 * (0.55 + 0.45 * n.y);
        col = alb * (L * sun + amb);
    }
    col = applyMarks(col, p, vWorld.y, vDist, B);
    vec3 v = vWorld - uCam;
    float dist = length(v);
    col = aerial(col, v / max(dist, 1e-3), dist);
    frag = vec4(col, 1.0);
}
