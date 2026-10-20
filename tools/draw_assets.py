from PIL import Image, ImageDraw, ImageFont
from pathlib import Path
root=Path('/mnt/data/atm0_v2work/assets/assets/atm0arcanelibrary/textures/gui');root.mkdir(parents=True,exist_ok=True)
palette={'border':(70,66,65),'highlight':(248,245,238),'shell':(196,193,190),'face':(219,217,212),'shadow':(132,128,125),'inside':(80,72,65),'dark':(48,43,37),'purple':(160,72,156),'purple_light':(219,121,211),'slot':(106,105,106)}
im=Image.new('RGBA',(256,256),(0,0,0,0));d=ImageDraw.Draw(im)
def frame(x,y,w,h,fill,stroke=None):
  x2,y2=x+w-1,y+h-1
  d.rectangle((x,y,x2,y2),fill=palette.get(fill,fill),outline=palette.get(stroke,stroke) if stroke else palette['border'])
  d.line((x+1,y+1,x2-2,y+1),fill=palette['highlight']);d.line((x+1,y+1,x+1,y2-2),fill=palette['highlight'])
  d.line((x+2,y2-1,x2-1,y2-1),fill=palette['shadow']);d.line((x2-1,y+2,x2-1,y2-1),fill=palette['shadow'])
def slot(x,y,w=18,h=18):
  frame(x,y,w,h,'shadow');d.rectangle((x+3,y+3,x+w-4,y+h-4),fill=palette['slot']);d.line((x+3,y+3,x+w-4,y+3),fill=palette['border']);d.line((x+3,y+3,x+3,y+h-4),fill=palette['border'])
def text(x,y,t,col=(120,120,120)):
  d.text((x,y),t,fill=col)
frame(0,0,176,230,'shell')
d.rectangle((4,4,171,225),fill=palette['face'])
d.line((5,15,168,15),fill=palette['purple']); d.line((5,16,168,16),fill=(233,192,228))
for xx,yy in [(4,4),(168,4),(4,222),(168,222)]: d.rectangle((xx,yy,xx+2,yy+2),fill=(135,126,124))
frame(16,27,119,104,'border')
d.rectangle((19,29,132,129),fill=palette['dark'])
for i in range(5):
 yy=30+20*i
 d.rectangle((20,yy,130,yy+18),fill=(57,51,43),outline=(120,110,90))
 d.line((21,yy+1,129,yy+1),fill=(97,87,70))
# scrollbar
d.rectangle((134,30,142,129),fill=(91,89,90),outline=(62,58,58));d.line((135,32,135,126),fill=(180,171,175))
# controls and book preview
for yy in (64,92):slot(142,yy)
# potion mode row backgrounds
for x,w in [(4,39),(44,39),(84,39),(124,48)]:frame(x,132,w,15,'face')
# inventory
for yy in (148,166,184):
 for xx in range(8,170,18):slot(xx,yy)
for xx in range(8,170,18):slot(xx,206)
# selected highlight tile right atlas
frame(179,4,74,18,'dark');d.rectangle((181,6,251,19),fill=(87,54,91),outline=palette['purple_light'])
frame(179,26,74,18,'dark')
frame(179,48,18,18,'face');frame(202,48,18,18,'face')
frame(179,70,74,30,'face')
for i,color in enumerate(((219,165,45),(183,61,61),(114,112,108))):
 frame(179+i*25,105,22,22,'face'); d.ellipse((186+i*25,111,193+i*25,119),fill=color)
im.save(root/'library_v2.png')
# Transparent reusable 1x asset atlas. Individual sprites crop from atlas in original logical pixels.
parts={'window_frame':(0,0,176,230),'list_row_normal':(20,30,111,19),'list_row_selected':(179,4,74,18),
'ui_slot':(142,64,18,18),'scroll_track':(134,30,9,99),'button_back':(179,26,74,18),'status_panel':(179,70,74,30)}
for name,(x,y,w,h) in parts.items():
 im.crop((x,y,x+w,y+h)).save(root/(name+'.png'))
preview=im.crop((0,0,176,230)).resize((704,920),Image.Resampling.NEAREST)
preview.save('/mnt/data/ATM0-Arcane-Library-0.2.0-UI-preview.png')
print('generated',len(parts)+1,'PNG assets')
