from PIL import Image
from pathlib import Path
root=Path(__file__).resolve().parents[1]
dir=root/'src/main/resources/assets/atm0arcanelibrary/textures/gui'
src=Image.open(dir/'sotnah_library.png').convert('RGBA')
# Preserve Sotnah's original pixels; add a 76px seamless inner segment to the upper panel.
# Lower player-inventory sheet is centered, keeping pixel-perfect source details.
DELTA=76
TOP=136
w=176+DELTA
atlas=Image.new('RGBA',(512,256),(0,0,0,0))
# Header & list: seam placed inside a flat area of the central content field.
atlas.paste(src.crop((0,0,106,TOP)),(0,0))
col=src.crop((106,0,107,TOP))
for i in range(DELTA):atlas.paste(col,(106+i,0))
atlas.paste(src.crop((106,0,176,TOP)),(106+DELTA,0))
# Lower inventory naturally appears as a narrower centered base beneath the wider top panel.
atlas.paste(src.crop((0,TOP,176,230)),(DELTA//2,TOP))
# Expanded rows use original hover/normal row sprite with nearest-pixel stretching of a 1 px interior band.
for mode in (0,1):
    y=20*mode
    original=src.crop((194,y,307,y+20))
    row=Image.new('RGBA',(113+DELTA,20),(0,0,0,0))
    row.paste(original.crop((0,0,60,20)),(0,0))
    col=original.crop((60,0,61,20))
    for i in range(DELTA):row.paste(col,(60+i,0))
    row.paste(original.crop((60,0,113,20)),(60+DELTA,0))
    atlas.paste(row,(280,y))
# Scrollbar sprite kept from unmodified Sotnah source.
atlas.paste(src.crop((303,40,307,64)),(480,40))
dst=dir/'library_wide.png'
atlas.save(dst)
# A practical 1:1 GUI preview (no fake buttons or tooltip, for sprite QA).
atlas.crop((0,0,w,230)).resize((w*3,690),Image.Resampling.NEAREST).save(root/'docs/0.2.3-wide-atlas-preview.png')
print(dst,atlas.size)
