package dev.frostey.arcanelibrary.v2;

import java.lang.reflect.*;
import java.util.*;

/** Pixel-exact Sotnah library.png background and scroll-list bridge (1.7.10). */
public final class V22GuiHooks {
    private V22GuiHooks(){}
    private static final int BG_W=307,BG_H=256;
    private static final Map<Object,String> FILTER=Collections.synchronizedMap(new WeakHashMap<Object,String>());
    private static final Map<Object,Boolean> FOCUS=Collections.synchronizedMap(new WeakHashMap<Object,Boolean>());
    private static Class<?> cl(String n){try{return Class.forName(n);}catch(Exception e){throw new IllegalStateException(n,e);}}
    private static Object call(Object obj,String m,Class<?>[] t,Object...a){
        try{for(Class<?> k=obj.getClass();k!=null;k=k.getSuperclass())
          try{Method f=k.getDeclaredMethod(m,t);f.setAccessible(true);return f.invoke(obj,a);}catch(NoSuchMethodException ignored){}
          throw new NoSuchMethodException(m);
        }catch(InvocationTargetException e){throw new IllegalStateException(m,e.getCause());}catch(Exception e){throw new IllegalStateException(m,e);}
    }
    private static int number(Object obj,String field){return ((Number)V2Hooks.field(obj,field)).intValue();}
    private static boolean potion(Object gui){return gui.getClass().getSimpleName().contains("Potion");}
    private static int left(Object gui){return number(gui,"field_147003_i");}
    private static int top(Object gui){return number(gui,"field_147009_r");}
    private static Object container(Object gui){return V2Hooks.field(gui,"field_147002_h");}
    private static Object tile(Object gui){return V2Hooks.field(container(gui),"tile");}
    private static void setInt(Object obj,String field,int value){
        try{for(Class<?> c=obj.getClass();c!=null;c=c.getSuperclass())
         try{Field f=c.getDeclaredField(field);f.setAccessible(true);f.setInt(obj,value);return;}catch(NoSuchFieldException ignored){}
        }catch(Exception e){throw new IllegalStateException(e);}
    }
    private static Object button(int id,int x,int y,int w,int h,String label){
        try{return cl("net.minecraft.client.gui.GuiButton").getConstructor(int.class,int.class,int.class,int.class,int.class,String.class).newInstance(id,x,y,w,h,label);}
        catch(Exception e){throw new IllegalStateException(e);}
    }
    @SuppressWarnings("unchecked")
    public static void build(Object gui,boolean potion) {
        List<Object> buttons=(List<Object>)V2Hooks.field(gui,"field_146292_n");
        buttons.clear();
        if(potion){
            int x=left(gui),y=top(gui);
            buttons.add(button(10,x+2,y+132,42,14,"Normal"));
            buttons.add(button(11,x+45,y+132,40,14,"Strong"));
            buttons.add(button(12,x+86,y+132,40,14,"Long"));
            buttons.add(button(13,x+127,y+132,47,14,"Splash"));
        }
    }
    private static void bindTexture(){
        try{
            Object mc=cl("net.minecraft.client.Minecraft").getMethod("func_71410_x").invoke(null);
            Object mgr=call(mc,"func_110434_K",new Class<?>[0]);
            Class<?> res=cl("net.minecraft.util.ResourceLocation");
            Object tex=res.getConstructor(String.class,String.class).newInstance("atm0arcanelibrary","textures/gui/sotnah_library.png");
            call(mgr,"func_110577_a",new Class<?>[]{res},tex);
        }catch(Exception e){throw new IllegalStateException("Sotnah UI texture",e);}
    }
    private static void quad(int x,int y,int w,int h,int u,int v){
        try{
            Class<?> tess=cl("net.minecraft.client.renderer.Tessellator");
            Field f=tess.getDeclaredField("field_78398_a");f.setAccessible(true);Object t=f.get(null);
            call(t,"func_78382_b",new Class<?>[0]);
            Class<?>[] args={double.class,double.class,double.class,double.class,double.class};
            double u0=u/(double)BG_W,v0=v/(double)BG_H,u1=(u+w)/(double)BG_W,v1=(v+h)/(double)BG_H;
            call(t,"func_78374_a",args,(double)x,(double)(y+h),0.0,u0,v1);
            call(t,"func_78374_a",args,(double)(x+w),(double)(y+h),0.0,u1,v1);
            call(t,"func_78374_a",args,(double)(x+w),(double)y,0.0,u1,v0);
            call(t,"func_78374_a",args,(double)x,(double)y,0.0,u0,v0);
            call(t,"func_78381_a",new Class<?>[0]);
        }catch(Exception e){throw new IllegalStateException("Sotnah atlas draw",e);}
    }
    private static int size(Object gui){
        try{return ((Number)call(tile(gui),"entryCount",new Class<?>[0])).intValue();}
        catch(Exception e){return 0;}
    }
    private static int[] matches(Object gui){
        ArrayList<Integer> v=new ArrayList<Integer>();
        int n=size(gui);String f=FILTER.containsKey(gui)?FILTER.get(gui).toLowerCase(Locale.ROOT):"";
        for(int i=0;i<n;i++){
            String label=entry(gui,i);
            if(f.isEmpty()||label.toLowerCase(Locale.ROOT).contains(f))v.add(i);
        }
        int[] arr=new int[v.size()];for(int j=0;j<arr.length;j++)arr[j]=v.get(j);return arr;
    }
    private static String entry(Object gui,int index){
        try{return (String)call(gui,"entryLabel",new Class<?>[]{int.class},index);}
        catch(Exception e){return "";}
    }
    private static void drawString(Object gui,String label,int x,int y,int color){
        try{Object font=V2Hooks.field(gui,"field_146289_q");
            call(font,"func_78276_b",new Class<?>[]{String.class,int.class,int.class,int.class},label,x,y,color);
        }catch(Exception ignored){}
    }
    public static void render(Object gui,float partial,int mouseX,int mouseY){
        bindTexture();
        int x=left(gui),y=top(gui);
        quad(x,y,176,230,0,0);
        int[] filtered=matches(gui),first=number(gui,"pageStart");
        first=Math.max(0,Math.min(first,Math.max(0,filtered.length-5)));
        setInt(gui,"pageStart",first);
        int sel=number(gui,"localSelected");
        for(int row=0;row<5&&first+row<filtered.length;row++){
            int id=filtered[first+row],sy=y+30+20*row;
            boolean hover=mouseX>=x+20&&mouseX<x+133&&mouseY>=sy&&mouseY<sy+20;
            quad(x+20,sy,113,20,194,hover?20:0);
            // Text must not be pre-rendered into a button. It is drawn as part of the list.
            String label=entry(gui,id);
            if(label.length()>21)label=label.substring(0,19)+"..";
            drawString(gui,label,x+22,sy+4,id==sel?0xE7C7E8:0xFFFFFF);
        }
        // Sotnah scrollbar artwork. Source atlas at (303,40/52), 4x12.
        if(filtered.length>5){
            int h=89,den=Math.max(1,filtered.length-5);
            int at=(int)Math.round(h*(first/(double)den));
            quad(x+13,y+29+at,4,12,303,40);
        }else quad(x+13,y+29,4,12,303,52);
        // Search string stays aligned to the original built-in filter field.
        drawString(gui,FILTER.containsKey(gui)?FILTER.get(gui):"",x+17,y+17,0xFFFFFF);
    }
    public static void foreground(Object gui,int mx,int my){
        drawString(gui,potion(gui)?"Potion Library":"Enchantment Library",7,5,0x3D302B);
        drawString(gui,"Inventory",8,136,0x5B514A);
        if(potion(gui)){
            String[] count=V2Hooks.charges(tile(gui)).split(" ");
            for(int i=0;i<count.length;i++)drawString(gui,count[i],139,10+i*10,i==0?0xBBA132:i==1?0xB6544B:0x555555);
        }
    }
    private static void send(Object gui,int id){
        try{
            Object mc=cl("net.minecraft.client.Minecraft").getMethod("func_71410_x").invoke(null);
            Object ctrl=V2Hooks.field(mc,"field_71442_b");
            int window=number(container(gui),"field_75152_c");
            call(ctrl,"func_78756_a",new Class<?>[]{int.class,int.class},window,id);
        }catch(Exception e){throw new IllegalStateException("Selection packet",e);}
    }
    private static boolean pressed(String m){
        try{return Boolean.TRUE.equals(cl("org.lwjgl.input.Keyboard").getMethod("isKeyDown",int.class).invoke(null,"ctrl".equals(m)?29:42));}
        catch(Exception e){return false;}
    }
    public static void click(Object gui,int mouseX,int mouseY,int button){
        int x=left(gui),y=top(gui);
        if(mouseX>=x+16&&mouseX<x+126&&mouseY>=y+16&&mouseY<y+27){
            FOCUS.put(gui,true);
            if(button==1){FILTER.put(gui,"");setInt(gui,"pageStart",0);}
            return;
        }
        FOCUS.put(gui,false);
        if(mouseX>=x+20&&mouseX<x+133&&mouseY>=y+30&&mouseY<y+130){
            int row=(mouseY-y-30)/20;
            int[] filtered=matches(gui);
            int i=number(gui,"pageStart")+row;
            if(i<0||i>=filtered.length)return;
            int selected=filtered[i];setInt(gui,"localSelected",selected);
            int id=100+selected;
            if(!potion(gui)){
                if(pressed("shift"))id|=0x80000000;
                if(pressed("ctrl"))id|=0x40000000;
            }
            send(gui,id);return;
        }
        if(mouseX>=x+13&&mouseX<x+19&&mouseY>=y+29&&mouseY<y+132){
            int[] filtered=matches(gui);
            int first=Math.max(0,Math.min(filtered.length-5,(mouseY-y-29)*(Math.max(0,filtered.length-5))/103));
            setInt(gui,"pageStart",first);return;
        }
        superMouse(gui,mouseX,mouseY,button);
    }
    public static void superMouse(Object gui,int x,int y,int b){
        call(gui,"ATM0superMouse",new Class<?>[]{int.class,int.class,int.class},x,y,b);
    }
    public static void key(Object gui,char ch,int key){
        if(Boolean.TRUE.equals(FOCUS.get(gui))){
            String f=FILTER.containsKey(gui)?FILTER.get(gui):"";
            if(key==14 && f.length()>0)f=f.substring(0,f.length()-1);
            else if(key==1 || key==28)FOCUS.put(gui,false);
            else if(ch>=32&&ch<127&&f.length()<50)f+=ch;
            FILTER.put(gui,f);setInt(gui,"pageStart",0);return;
        }
        call(gui,"ATM0superKey",new Class<?>[]{char.class,int.class},ch,key);
    }
    public static void wheel(Object gui){
        try {
            int w=((Number)cl("org.lwjgl.input.Mouse").getMethod("getDWheel").invoke(null)).intValue();
            if(w==0)return;
            int size=matches(gui).length,first=number(gui,"pageStart");
            first=Math.max(0,Math.min(Math.max(0,size-5),first+(w<0?1:-1)));
            setInt(gui,"pageStart",first);
        }catch(Exception ignored){}
    }
}