// terrain.frag — das Gelände (seit Phase 3 das Grundbild): Farbe nach der Art des Grunds, im Kern zu 1 m aus dem
// Luftbild (daten/kern/klasse.u8, uKlasse), sonst die Art des Hauptrasters (uSource, 10 m); die Albedo je Art ist am
// DOP20 geeicht (uAlb, KernKarte). Strand, nasser Sand und Watt nehmen das Material aus com.dan.mudflat (mudflat.glsl:
// Rippel, Nässe nach der Höhe über dem Pegel, Wasserfilm, Wattwurmhaufen) und spiegeln den Himmel nach Fresnel. Das
// Kliff vorerst mit Grundmaterial nach Neigung und Höhe in zwei Schichten (unten heller Kaolinsand, oben rot
// oxidierter Geschiebelehm [G]); Phase 4 ersetzt das durch cliff. Licht: mit com.dan.sky Sonne und Mond vorbelichtet,
// Himmelslicht aus uSkyAmb, Wolkenschatten aus com.dan.cloud (uCloudShadow); sonst der Platzhalter aus Phase 1. Dazu
// zuschaltbar die Marken (marken.glsl). Unter dem Wasser zeichnet com.dan.ocean (sonst meer.frag).
#include "common.glsl"
#include "terrain_height.glsl"
#include "marken.glsl"
#include "/com/dan/mudflat/gl/mudflat.glsl"
in vec3 vWorld;
in vec3 vNormal;
in float vDist;
out vec4 frag;

uniform vec4 uAlb[14];          // Albedo je Art (Terrain.ART), linear, am DOP20 geeicht (Phase 3)
uniform usampler2D uKlasse;     // Arten im Kern zu 1 m (R8UI, Zeilen von Nord nach Süd wie das Kernraster)
uniform int uKlasseOn;
uniform sampler2D uCloudShadow; // Wolkenschatten (com.dan.cloud), Draufsicht
uniform vec4 uCloudRect;        // x, z der Ecke, Kantenlänge, an

vec3 srgb(vec3 c) { return pow(c, vec3(2.2)); }

float hash2(vec2 i) { return fract(sin(dot(i, vec2(127.1, 311.7))) * 43758.5453); }
float vnoise(vec2 x) {
    vec2 i = floor(x), f = x - i;
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash2(i), hash2(i + vec2(1, 0)), f.x), mix(hash2(i + vec2(0, 1)), hash2(i + vec2(1, 1)), f.x), f.y);
}
// Flecken der Größe s (m); in der Ferne (Bildpunkt pxm m) zum Mittel 0,5
float fleck(vec2 p, float s, float pxm) { return mix(vnoise(p / s), 0.5, smoothstep(0.15 * s, 0.6 * s, pxm)); }

// Farbe einer Art am Ort: die geeichte Albedo mit Flecken (Mittel 1) [G nach RotesKliff1–5 und Kampen1–4]
vec3 artColor(uint a, vec2 p, float pxm) {
    float f = fleck(p, 6.0, pxm), g = fleck(p + 17.0, 25.0, pxm), k = fleck(p + 41.0, 1.6, pxm);
    vec3 A = uAlb[min(a, 13u)].rgb;
    if (a == 1u) return A;                                                                   // See: Sand unter Wasser
    if (a == 2u || a == 3u || a == 4u) return A * (0.92 + 0.16 * f);                         // Watt, Strand, nasser Sand
    if (a == 6u) return A * (0.90 + 0.20 * f);                                               // Düne: offener Sand
    if (a == 12u) return mix(uAlb[6].rgb, A, 0.55 + 0.35 * g) * (0.9 + 0.2 * k);             // Strandhafer auf Sand
    if (a == 7u) return A * mix(vec3(1.0, 0.98, 0.92), vec3(0.95, 0.82, 1.05), 0.6 * f) * (0.85 + 0.3 * k);   // Heide, Krähenbeere
    if (a == 8u) return A * (0.80 + 0.40 * f);                                               // Gebüsch, Kartoffelrose
    if (a == 9u) return A * mix(vec3(1.0), vec3(1.10, 1.05, 0.85), g) * (0.9 + 0.2 * k);    // Wiese, Golfplatz
    if (a == 10u || a == 13u) return A * (0.80 + 0.40 * g);                                  // Siedlung, Dach
    return A * (0.9 + 0.2 * f);
}

// Anteil von Sand und Watt (Material aus mudflat) an einer Art
// (die Düne nicht: Wind- statt Wellenrippel bringt dune in Phase 8)
float sandShare(uint a) { return (a == 1u || a == 2u || a == 3u || a == 4u) ? 1.0 : 0.0; }

void main() {
    vec2 p = vWorld.xz;
    vec3 V = normalize(uCam - vWorld);
    // Normale im Bildpunkt aus der Höhe (fein in der Nähe, gröber in der Ferne), nie enger als das Raster an diesem Ort
    float kern = uKernOn == 1 ? smoothstep(0.0, uKNaht, insideOf(p, uKernGrid, uKernN)) : 0.0;
    float e = clamp(vDist * 0.003, mix(uGrid.z * 0.8, 1.0, kern), 80.0);
    float lvl = heightLevel(vDist) - 1.0;
    float hx = heightAt(p + vec2(e, 0.0), lvl) - heightAt(p - vec2(e, 0.0), lvl);
    float hz = heightAt(p + vec2(0.0, e), lvl) - heightAt(p - vec2(0.0, e), lvl);
    vec3 n = normalize(mix(vNormal, normalize(vec3(-hx, 2.0 * e, -hz)), 1.0 - smoothstep(4000.0, 12000.0, vDist)));
    float pxm = length(fwidth(p));
    float pxD = max(pxm, vDist * 0.0012 / max(0.08, V.y));

    // Art weich an den Rändern: die vier nächsten Zellen, bilinear gewichtet; im Kern nah zu 1 m aus dem Luftbild
    vec3 alb = vec3(0.0);
    float sand = 0.0, band = 0.0;   // band: Anteil des Kliffs (seit Phase 4, nur dort die Wand)
    bool imKern = uKlasseOn == 1 && insideOf(p, uKernGrid, uKernN) > 0.5 && vDist < 3000.0;
    vec2 u = imKern ? (p - uKernGrid.xy) / uKernGrid.z - 0.5 : (p - uGrid.xy) / uGrid.z - 0.5;
    ivec2 i0 = ivec2(floor(u));
    vec2 fr = u - vec2(i0);
    bool drin = insideDist(p) > 0.0;
    int nmax = imKern ? int(uKernN) - 1 : int(uGridN) - 1;
    for (int b = 0; b <= 1; b++)
        for (int c = 0; c <= 1; c++) {
            ivec2 q = clamp(i0 + ivec2(c, b), ivec2(0), ivec2(nmax));
            uint a = imKern ? texelFetch(uKlasse, q, 0).r : (drin ? texelFetch(uSource, q, 0).r : (vWorld.y < 0.0 ? 1u : 9u));
            float wt = (c == 0 ? 1.0 - fr.x : fr.x) * (b == 0 ? 1.0 - fr.y : fr.y);
            if (imKern) { band += (a & 32u) != 0u ? wt : 0.0; a &= 31u; }
            alb += artColor(a, p, pxm) * wt;
            sand += sandShare(a) * wt;
        }
    // das Band des Kliffs auch in der Ferne (sonst verlöre das Kliff aus mehr als 3 km seine Wand)
    if (!imKern && uKlasseOn == 1 && insideOf(p, uKernGrid, uKernN) > 0.5) {
        vec2 uk = (p - uKernGrid.xy) / uKernGrid.z - 0.5;
        ivec2 k0 = ivec2(floor(uk));
        vec2 fk = uk - vec2(k0);
        for (int b = 0; b <= 1; b++)
            for (int c = 0; c <= 1; c++)
                band += (texelFetch(uKlasse, clamp(k0 + ivec2(c, b), ivec2(0), ivec2(int(uKernN) - 1)), 0).r & 32u) != 0u
                        ? (c == 0 ? 1.0 - fk.x : fk.x) * (b == 0 ? 1.0 - fk.y : fk.y) : 0.0;
    }
    // Übergang von 1 m zu 10 m nach der Entfernung: weit draußen im Kern die Art des Hauptrasters
    if (imKern && vDist > 2000.0) {
        vec2 u2 = (p - uGrid.xy) / uGrid.z - 0.5;
        ivec2 j0 = ivec2(floor(u2));
        vec2 f2 = u2 - vec2(j0);
        vec3 alb2 = vec3(0.0);
        for (int b = 0; b <= 1; b++)
            for (int c = 0; c <= 1; c++) {
                uint a = texelFetch(uSource, clamp(j0 + ivec2(c, b), ivec2(0), ivec2(int(uGridN) - 1)), 0).r;
                alb2 += artColor(a, p, pxm) * (c == 0 ? 1.0 - f2.x : f2.x) * (b == 0 ? 1.0 - f2.y : f2.y);
            }
        alb = mix(alb, alb2, smoothstep(2000.0, 3000.0, vDist));
    }

    // Sand und Watt: Material aus com.dan.mudflat (Nässe nach der Höhe über dem Pegel, Rippel, Film), nah am Auge
    MudSurface m = MudSurface(vec3(0.0), n, 0.0, 1.0, 0.0, 0.0);
    float mudW = sand * (1.0 - smoothstep(900.0, 1500.0, vDist));
    if (mudW > 0.001) {
        m = mudflat(p, n, vWorld.y - uLevel, pxD);
        // Helligkeit und Nässe aus dem Material, die Farbe aus der Eichung
        vec3 ref = mudColor(m.sediment);
        alb = mix(alb, alb * clamp(m.albedo / max(ref, vec3(1e-3)), 0.3, 1.6), mudW);
        // Rippel vor allem im nassen Sand (der Spülsaum formt sie); trockener Strand ist zertreten und vom Wind glatt [G]
        n = normalize(mix(n, m.normal, mudW * mix(0.15, 1.0, smoothstep(0.35, 0.8, m.wetness))));
    } else {
        // in der Ferne nur das Mittel: nasser Sand nahe dem Wasser dunkler
        float ueber = vWorld.y - uLevel;
        alb *= mix(1.0, 1.0 - 0.45 * clamp(1.0 - ueber * 0.5, 0.0, 1.0), sand);
    }

    // steile Wand: das Kliff in Schichten [G]: unten heller Kaolinsand, darüber rot oxidierter Geschiebelehm, oben
    // eine dünne Lage Flugsand und die Grasnarbe; Phase 4 ersetzt das durch das Schichtprofil aus cliff (F14 A)
    float wand = smoothstep(0.75, 0.45, n.y) * (1.0 - step(vWorld.y, 1.0)) * (uKlasseOn == 1 ? band : 0.0);
    if (wand > 0.0) {
        float rel = clamp((vWorld.y - 3.0) / 25.0, 0.0, 1.0);
        float bandRausch = fleck(vec2(p.x + p.y, vWorld.y * 6.0), 2.0, pxm);
        vec3 kaolin = srgb(mix(vec3(0.74, 0.71, 0.64), vec3(0.67, 0.63, 0.55), bandRausch));
        vec3 lehm = srgb(mix(vec3(0.62, 0.31, 0.16), vec3(0.52, 0.26, 0.13), bandRausch));
        vec3 wandFarbe = mix(kaolin, lehm, smoothstep(0.30, 0.42, rel + 0.06 * (bandRausch - 0.5)));
        alb = mix(alb, wandFarbe, wand);
        m.film *= 1.0 - wand;
    }
    // Fernring: außerhalb des Hauptrasters eine Mischfarbe nach der Höhe (Sand, Marsch)
    float fern = 1.0 - smoothstep(0.0, uNaht, insideDist(p));
    alb = mix(alb, mix(uAlb[6].rgb, uAlb[9].rgb, smoothstep(1.0, 6.0, vWorld.y)), fern);

    float cloud = uCloudRect.w > 0.0 ? textureLod(uCloudShadow, (p - uCloudRect.xy) / uCloudRect.z, 0.0).r : 1.0;
    vec3 col;
    float B;
    if (uSkyMode == 1) {
        vec3 direct = (sunLight() * max(dot(n, uSun), 0.0) + moonLight() * max(dot(n, uMoonDir), 0.0)) * cloud;
        vec3 amb = uSkyAmb * (0.6 + 0.4 * n.y);
        col = alb * (direct / 3.14159265 + amb);
        B = 0.32 * dot(sunLight() * cloud * max(uSun.y, 0.0) / 3.14159265 + uSkyAmb + moonLight() * max(uMoonDir.y, 0.0) / 3.14159265, vec3(0.2126, 0.7152, 0.0722));
    } else {
        vec3 L = sunLight();
        float sun = max(dot(n, uSun), 0.0);
        B = skyBrightness(sunElevDeg());
        vec3 amb = vec3(0.30, 0.38, 0.52) * B / 0.32 * (0.55 + 0.45 * n.y);
        col = alb * (L * sun * cloud + amb);
    }
    // nasser Sand und Watt: der Wasserfilm spiegelt den Himmel nach Fresnel und die Sonne, umso schärfer, je glatter
    if (mudW > 0.001 && (m.film > 0.01 || m.rough < 0.95)) {
        vec3 r = reflect(-V, n);
        float F = 0.02 + 0.98 * pow(1.0 - max(dot(n, V), 0.0), 5.0);
        vec3 refl = skyColor(vec3(r.x, max(r.y, 0.0), r.z)) * 0.85;
        vec3 wet = mix(col, refl, clamp(F, 0.0, 1.0) * m.film);
        wet += refl * F * 0.08 * (1.0 - m.film) * (1.0 - m.rough);
        float sharp = mix(30.0, 1200.0, 1.0 - m.rough);
        wet += sunLight() * cloud * 4.0 * pow(max(dot(r, uSun), 0.0), sharp) * F * (1.0 - m.rough) * sqrt(sharp / 300.0)
             * (uSkyMode == 1 ? 1.0 / 3.14159265 : 1.0);
        col = mix(col, wet, mudW);
    }
    col = applyMarks(col, p, vWorld.y, vDist, B);
    vec3 v = vWorld - uCam;
    float dist = length(v);
    col = aerial(col, v / max(dist, 1e-3), dist);
    frag = vec4(col, 1.0);
}
