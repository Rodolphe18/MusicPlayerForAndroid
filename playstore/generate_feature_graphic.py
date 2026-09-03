# -*- coding: utf-8 -*-
"""Génère l'image de présentation (feature graphic) 1024x500 du Play Store.

Palette « Tropical Fresh » de l'app (ui/theme/Color.kt) et polices embarquées
dans res/font, pour que la fiche et l'application se ressemblent.
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.dirname(os.path.abspath(__file__))
FONT_DIR = os.path.join(ROOT, "..", "app", "src", "main", "res", "font")

NIGHT_DEEP = (6, 30, 42)
TEAL = (46, 158, 139)
CORAL = (232, 93, 84)
CREAM = (255, 248, 247)
SUBTLE = (176, 209, 203)

# Attention : ce script utilise Pillow, qui ne sait pas assembler les ecritures
# complexes (devanagari, arabe...) faute de Raqm — les matras seraient mal placees.
# Pour ces langues, passer par feature-graphic-<langue>.html rendu avec Chrome :
#   chrome --headless=new --window-size=1024,500 --screenshot=sortie.png fichier.html
# Voir feature-graphic-hi.html, qui reprend exactement le meme design.

# (suffixe de fichier, titre, accroche, police du titre)
# Poppins ne couvre pas le cyrillique : le titre russe y sortirait en tofu, d'ou
# Manrope Bold, seule police du projet a le prendre en charge (verifie au rendu).
VARIANTES = [
    ("en", "MP3 Player", "Your music, offline.", "poppins_extrabold.ttf"),
    ("pt", "Player de MP3", "Sua música, offline.", "poppins_extrabold.ttf"),
    ("id", "Pemutar MP3", "Musik Anda, tanpa internet.", "poppins_extrabold.ttf"),
    ("ru", "MP3-плеер", "Ваша музыка офлайн.", "manrope_bold.ttf"),
]


def font(name, size):
    return ImageFont.truetype(os.path.join(FONT_DIR, name), size)


def radial(size, color, center, radius, alpha):
    """Halo radial doux, calculé en petit puis agrandi."""
    w, h = size
    s = 96
    lay = Image.new("L", (s, s), 0)
    px = lay.load()
    cx, cy, r = center[0] * s, center[1] * s, radius * s
    for y in range(s):
        for x in range(s):
            v = max(0.0, 1.0 - ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5 / r)
            px[x, y] = int(alpha * v * v)
    return Image.new("RGB", (w, h), color), lay.resize((w, h), Image.BICUBIC)


def background(w, h):
    base = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(base)
    top, bot = NIGHT_DEEP, (15, 62, 76)
    for y in range(h):
        t = y / max(1, h - 1)
        d.line([(0, y), (w, y)],
               fill=tuple(int(top[i] + (bot[i] - top[i]) * t) for i in range(3)))
    for color, center, rad, alpha in (
        (TEAL, (0.86, 0.92), 0.85, 150),
        (CORAL, (0.08, 0.02), 0.55, 70),
    ):
        tint, mask = radial((w, h), color, center, rad, alpha)
        base.paste(tint, (0, 0), mask)
    return base


def note_layer(size, color):
    """Note « music_note » de Material, identique à ic_launcher_foreground.xml :
    tête ronde + hampe + drapeau, dans un viewBox 24x24."""
    s = size * 4
    lay = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    k = s / 24.0
    ox = (s - 12 * k) / 2 - 6 * k
    oy = (s - 18 * k) / 2 - 3 * k
    P = lambda x, y: (ox + x * k, oy + y * k)
    d.ellipse([P(6, 13), P(14, 21)], fill=color)
    d.rectangle([P(12, 3), P(14, 17)], fill=color)
    d.rectangle([P(14, 3), P(18, 7)], fill=color)
    return lay.resize((size, size), Image.LANCZOS)


def make_feature(path, titre, accroche, police_titre="poppins_extrabold.ttf"):
    W, H = 1024, 500
    img = background(W, H)
    d = ImageDraw.Draw(img)

    tile = 190
    tx, ty = 92, (H - tile) // 2

    sh = Image.new("L", (W, H), 0)
    ImageDraw.Draw(sh).rounded_rectangle([tx, ty + 14, tx + tile, ty + tile + 14],
                                         radius=48, fill=170)
    img.paste(Image.new("RGB", (W, H), (0, 10, 16)), (0, 0),
              sh.filter(ImageFilter.GaussianBlur(22)))

    icon = Image.new("RGBA", (tile * 2, tile * 2), (0, 0, 0, 0))
    ImageDraw.Draw(icon).rounded_rectangle([0, 0, tile * 2, tile * 2], radius=96,
                                           fill=CORAL + (255,))
    g = note_layer(int(tile * 2 * 0.56), (255, 255, 255, 255))
    icon.paste(g, ((tile * 2 - g.width) // 2, (tile * 2 - g.height) // 2), g)
    img.paste(icon.resize((tile, tile), Image.LANCZOS), (tx, ty),
              icon.resize((tile, tile), Image.LANCZOS))

    x = tx + tile + 62
    # Reduit la taille si le titre deborde de la zone disponible a droite de l'icone.
    dispo = W - x - 48
    taille = 76
    while taille > 40 and d.textlength(titre, font=font(police_titre, taille)) > dispo:
        taille -= 2
    ft, fs = font(police_titre, taille), font("manrope_medium.ttf", 36)
    th = ft.getbbox("Ag")[3] - ft.getbbox("Ag")[1]
    y = (H - (th + 74)) // 2 - 12
    d.text((x, y), titre, font=ft, fill=CREAM)
    d.text((x, y + th + 46), accroche, font=fs, fill=SUBTLE)
    d.rounded_rectangle([x, y + th + 118, x + 84, y + th + 128], radius=5, fill=CORAL)

    img.save(path)
    print("%s  %sx%s  %.0f Ko" % (os.path.basename(path), img.width, img.height,
                                  os.path.getsize(path) / 1024))


if __name__ == "__main__":
    for suffixe, titre, accroche, police in VARIANTES:
        make_feature(os.path.join(ROOT, "feature-graphic-%s.png" % suffixe),
                     titre, accroche, police)
