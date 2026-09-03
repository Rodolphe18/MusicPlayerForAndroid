# -*- coding: utf-8 -*-
"""Met des captures brutes aux formats exigés par le Play Store.

Règles Google : ratio 16:9 ou 9:16 strict ; côtés 320–3840 px (téléphone et
tablette 7"), 1080–7680 px (tablette 10") ; 8 Mo maximum par image.

Principe : on ne rogne jamais le contenu utile et on ne déforme rien. La zone
publicitaire du bas est retirée (une capture montrant une annonce est refusée),
puis l'image est centrée sur le plus petit canevas au bon ratio, le complément
étant obtenu en prolongeant les pixels de bord — invisible sur un fond uni comme
sur un dégradé.
"""
import math
import os
import glob
from PIL import Image

ROOT = os.path.dirname(os.path.abspath(__file__))

# (dossier source, dossier de sortie, portrait, taille imposée ou None, suffixe de nom)
# Le suffixe s'ajoute au nom du fichier produit : "" donne phone-01.png,
# "-ru" donne phone-01-ru.png.
JOBS = [
    ("english/phone", "english-playstore/phone", True, None, ""),
    ("english/tablet7", "english-playstore/tablet7", True, None, ""),
    # La tablette 10" impose 1080 px minimum par côté : un agrandissement est
    # inévitable, la source ne faisant que 800 px de haut.
    ("english/tablet10", "english-playstore/tablet10", False, (1920, 1080), ""),
    ("hindi/phone", "hindi-playstore/phone", True, None, ""),
    ("indonesie", "indonesie-playstore/phone", True, None, ""),
    ("portugais", "portugais-playstore/phone", True, None, ""),
    ("russian", "russian-playstore/phone", True, None, "-ru"),
]


def ad_top(im, calm_run=12):
    """Remonte depuis le bas et renvoie la limite sous laquelle il ne reste que
    la bannière publicitaire (ou du fond uni)."""
    w, h = im.size
    px = im.convert("RGB").load()
    step = max(1, w // 160)

    def calm(y):
        seen = set()
        for x in range(0, w, step):
            r, g, b = px[x, y]
            seen.add((r >> 5, g >> 5, b >> 5))
            if len(seen) > 4:
                return False
        return True

    run, y = 0, h - 1
    while y > int(h * 0.6):
        if calm(y):
            run += 1
            if run >= calm_run:
                return y + run
        else:
            run = 0
        y -= 1
    return h


def bottom_band(im, tol=10):
    """Bande uniforme collee au bas dont la couleur differe du fond de l'app.

    Complete ad_top, qui ne voit pas un emplacement publicitaire *non rempli* :
    celui-ci est une bande grise parfaitement uniforme, que la detection par
    variation de couleur prend pour du fond.
    """
    w, h = im.size
    px = im.convert("RGB").load()
    step = max(1, w // 120)

    def moy(y):
        cols = [px[x, y] for x in range(0, w, step)]
        return tuple(sum(c[i] for c in cols) // len(cols) for i in range(3))

    fond, bas = moy(2), moy(h - 1)
    if all(abs(fond[i] - bas[i]) <= tol for i in range(3)):
        return h  # le bas est deja le fond de l'app : rien a retirer

    def majoritairement_bas(y):
        """Critere de majorite plutot qu'uniformite stricte : l'emplacement
        publicitaire vide porte une petite icone en son centre, qui ferait
        echouer un test d'uniformite et arreterait la detection trop tot."""
        cols = [px[x, y] for x in range(0, w, step)]
        n = sum(1 for c in cols if all(abs(c[i] - bas[i]) <= tol for i in range(3)))
        return n >= 0.8 * len(cols)

    y = h - 1
    while y > int(h * 0.7) and majoritairement_bas(y):
        y -= 1
    return y + 1


def plain_band(im, tol=4, marge=0.85):
    """Bandeau du bas dont le fond est presque identique a celui de l'app.

    Cas des bannieres AdMob sur fond blanc alors que l'app est creme (255,248,247) :
    l'ecart est trop faible pour bottom_band, et les marges uniformes de la banniere
    trompent ad_top. On remonte donc jusqu'a la premiere ligne entierement a la
    couleur de fond relevee en haut de l'ecran.
    """
    w, h = im.size
    px = im.convert("RGB").load()
    step = max(1, w // 200)
    fond = px[5, 5]

    def ligne_de_fond(y):
        return all(all(abs(px[x, y][i] - fond[i]) <= tol for i in range(3))
                   for x in range(0, w, step))

    y = h - 1
    while y > int(h * marge) and not ligne_de_fond(y):
        y -= 1
    return y + 1


def edge_row(im, top, inset=3):
    """Ligne de fond à étirer. On évite y=0 (bordure système plus claire que le
    fond) et on rogne les bords latéraux, où court la même bordure."""
    w, h = im.size
    y = min(2, h - 1) if top else h - 1
    return im.crop((inset, y, w - inset, y + 1)).resize((w, 1), Image.BILINEAR)


def canvas_size(w, h, portrait):
    """Plus petit canevas au ratio exact 9:16 (ou 16:9) contenant w x h."""
    if portrait:
        k = math.ceil(max(w / 9, h / 16))
        return 9 * k, 16 * k
    k = math.ceil(max(w / 16, h / 9))
    return 16 * k, 9 * k


def frame(im, portrait):
    w, h = im.size
    W, H = canvas_size(w, h, portrait)
    ox, oy = (W - w) // 2, (H - h) // 2
    out = Image.new("RGB", (W, H))
    if oy > 0:
        out.paste(edge_row(im, True).resize((w, oy), Image.NEAREST), (ox, 0))
        out.paste(edge_row(im, False).resize((w, H - oy - h), Image.NEAREST), (ox, oy + h))
    out.paste(im, (ox, oy))
    if ox > 0:
        out.paste(out.crop((ox, 0, ox + 1, H)).resize((ox, H), Image.NEAREST), (0, 0))
        out.paste(out.crop((ox + w - 1, 0, ox + w, H)).resize((W - ox - w, H), Image.NEAREST),
                  (ox + w, 0))
    return out


def run():
    for src, dest, portrait, taille, suffixe in JOBS:
        nom = dest.replace("/", " / ")
        files = sorted(glob.glob(os.path.join(ROOT, src, "*.png")) +
                       glob.glob(os.path.join(ROOT, src, "*.jpg")))
        if not files:
            print("== %s == aucun fichier" % nom)
            continue
        loaded = [(f, Image.open(f).convert("RGB")) for f in files]

        # Rognage commun, en écartant les détections aberrantes : une bannière
        # n'occupe jamais plus de ~15 % de la hauteur.
        h0 = loaded[0][1].height
        cuts = [min(ad_top(im), bottom_band(im), plain_band(im)) for _, im in loaded]
        plausible = [c for c in cuts if c >= 0.85 * h0] or cuts
        cut = min(plausible)
        print("\n== %s == détections %s -> commun %s (sur %s px)" % (nom, cuts, cut, h0))

        d = os.path.join(ROOT, dest)
        os.makedirs(d, exist_ok=True)
        prefixe = os.path.basename(dest)
        for i, (f, im) in enumerate(loaded, 1):
            img = frame(im.crop((0, 0, im.width, cut)), portrait)
            if taille:
                img = img.resize(taille, Image.LANCZOS)
            p = os.path.join(d, "%s-%02d%s.png" % (prefixe, i, suffixe))
            img.save(p)
            print("   %-16s %sx%-5s ratio %.5f  %.0f Ko"
                  % (os.path.basename(p), img.width, img.height,
                     img.width / img.height, os.path.getsize(p) / 1024))


if __name__ == "__main__":
    run()
