package dev.frostey.arcanelibrary.v2;

import java.lang.reflect.*;
import java.util.*;

/** 1.7.10-SRG compatible logic layer.  No compile-time Minecraft dependencies. */
public final class V2Hooks {
    private V2Hooks() {}
    private static final Map<Object, PotionState> POTIONS = Collections.synchronizedMap(new WeakHashMap<Object,PotionState>());
    private static final Map<Object, LinkedHashMap<Integer,Integer>> BOOKS = Collections.synchronizedMap(new WeakHashMap<Object,LinkedHashMap<Integer,Integer>>());
    private static final Map<Object,Integer> MODES = Collections.synchronizedMap(new WeakHashMap<Object,Integer>());
    private static final class PotionState {
        final TreeMap<Integer,Long> essence = new TreeMap<Integer,Long>();
        final long[] charges = new long[3];
    }
    private static PotionState state(Object tile) {
        PotionState p = POTIONS.get(tile);
        if (p == null) {p = new PotionState();POTIONS.put(tile,p);} return p;
    }
    public static Object field(Object o, String name) {
        if (o == null) return null;
        try { Field f = findField(o.getClass(),name); return f.get(o); }
        catch (Throwable e) { return null; }
    }
    private static Field findField(Class<?> cls, String name) throws Exception {
        for(Class<?> c=cls;c!=null;c=c.getSuperclass())try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException ignored){}
        throw new NoSuchFieldException(name);
    }
    private static void setField(Object o,String name,Object value) {
        try {findField(o.getClass(),name).set(o,value);}catch(Throwable e){throw new IllegalStateException(name,e);}
    }
    private static Class<?> c(String name) {try{return Class.forName(name);}catch(Exception e){throw new IllegalStateException(name,e);}}
    private static Object call(Object o,String name,Class<?>[] args,Object... vals) {
        try { Method m = null; for(Class<?> cur=o.getClass();cur!=null;cur=cur.getSuperclass()) {try {m=cur.getDeclaredMethod(name,args);m.setAccessible(true);break;}catch(NoSuchMethodException ignored){}}
            if(m==null)throw new NoSuchMethodException(name);return m.invoke(o,vals);
        }catch(InvocationTargetException e){throw new IllegalStateException(name,e.getCause());}catch(Exception e){throw new IllegalStateException(name,e);}
    }
    private static Object staticCall(String cl,String method,Class<?>[] args,Object... vals) {
        try{return c(cl).getMethod(method,args).invoke(null,vals);}catch(Exception e){throw new IllegalStateException(cl+"."+method,e);}
    }
    private static int count(Object stack){return stack == null?0:((Integer)field(stack,"field_77994_a")).intValue();}
    private static void count(Object stack,int v){setField(stack,"field_77994_a",v);}
    private static Class<?> stackClass(){return c("net.minecraft.item.ItemStack");}
    private static Object getItem(Object stack){return call(stack,"func_77973_b",new Class<?>[0]);}
    private static int damage(Object stack){return ((Integer)call(stack,"func_77960_j",new Class<?>[0])).intValue();}
    private static Object getTag(Object stack){return call(stack,"func_77978_p",new Class<?>[0]);}
    private static final Class<?>[] STR = {String.class};
    private static int num(Object obj,String m,String k){return ((Number)call(obj,m,STR,k)).intValue();}
    private static long lng(Object obj,String m,String k){return ((Number)call(obj,m,STR,k)).longValue();}
    private static void putLong(Object obj,String k,long n){call(obj,"func_74772_a",new Class<?>[]{String.class,long.class},k,n);}
    private static void putInt(Object obj,String k,int n){call(obj,"func_74768_a",new Class<?>[]{String.class,int.class},k,n);}
    private static Object tagCompound(){try{return c("net.minecraft.nbt.NBTTagCompound").newInstance();}catch(Exception e){throw new IllegalStateException(e);}}
    private static Object tagList(){try{return c("net.minecraft.nbt.NBTTagList").newInstance();}catch(Exception e){throw new IllegalStateException(e);}}
    private static void tagSet(Object obj,String k,Object val){call(obj,"func_74782_a",new Class<?>[]{String.class,c("net.minecraft.nbt.NBTBase")},k,val);}
    private static void listAppend(Object list,Object val){call(list,"func_74742_a",new Class<?>[]{c("net.minecraft.nbt.NBTBase")},val);}
    private static Object list(Object tag,String k){return call(tag,"func_150295_c",new Class<?>[]{String.class,int.class},k,10);}
    private static int listLength(Object list){return ((Integer)call(list,"func_74745_c",new Class<?>[0])).intValue();}
    private static Object listAt(Object list,int i){return call(list,"func_150305_b",new Class<?>[]{int.class},i);}
    private static Object stack(Object item,int dmg){
        try {return stackClass().getConstructor(c("net.minecraft.item.Item"),int.class,int.class).newInstance(item,1,dmg);}
        catch(Exception e){throw new IllegalStateException(e);}
    }
    private static Object vanillaItem(String name){return staticCall("cpw.mods.fml.common.registry.GameRegistry","findItem",new Class<?>[]{String.class,String.class},"minecraft",name);}
    private static Object playerInventory(Object player){return field(player,"field_71071_by");}
    private static boolean give(Object player,Object stack){return Boolean.TRUE.equals(call(playerInventory(player),"func_70441_a",new Class<?>[]{stackClass()},stack));}
    private static void change(Object tile){call(tile,"storageChanged",new Class<?>[0]);}
    private static long plus(long a,long b){return b>0&&Long.MAX_VALUE-a<b?Long.MAX_VALUE:a+b;}

    private static List<?> potionEffects(Object stack){
        if(stack==null||count(stack)<1)return null;
        Object item=getItem(stack);
        if(item==null||!c("net.minecraft.item.ItemPotion").isInstance(item))return null;
        try {
            for(String name : new String[]{"func_77832_l","getEffects"}){
                for(Method m:item.getClass().getMethods()){
                    if(m.getName().equals(name)&&m.getParameterTypes().length==1&&m.getParameterTypes()[0].isAssignableFrom(stackClass())&&List.class.isAssignableFrom(m.getReturnType())) {
                        Object got=m.invoke(item,stack);return got instanceof List?(List<?>)got:null;
                    }
                }
            }
            return null;
        } catch(Throwable t){return null;}
    }
    private static final class PotionInfo {int key;int power;int length;int splash;}
    private static PotionInfo potion(Object stack){
        try{
            List<?> effects=potionEffects(stack);
            if(effects==null||effects.size()!=1)return null;
            Object tag=getTag(stack);
            if(tag!=null) {
                // Preserve NBT safety: custom potion effects cannot be reconstructed losslessly.
                boolean custom=Boolean.TRUE.equals(call(tag,"func_74764_b",new Class<?>[]{String.class},"CustomPotionEffects"));
                if(custom)return null;
            }
            Object eff=effects.get(0);
            int amp=((Integer)call(eff,"func_76458_c",new Class<?>[0])).intValue();
            if(amp>1||amp<0)return null;
            int dmg=damage(stack);
            boolean stronger=(dmg&0x20)!=0;
            // Reject abnormal damage bits that would otherwise mint glowstone charge.
            if(stronger!=(amp>0))return null;
            if((dmg&0x40)!=0){
                int duration=((Number)call(eff,"func_76459_b",new Class<?>[0])).intValue();
                if(duration<=1)return null; // Instant potions cannot be lengthened.
            }
            int potionId=((Integer)call(eff,"func_76456_a",new Class<?>[0])).intValue();
            PotionInfo info=new PotionInfo();
            // Key combines unmodified recipe base and effect id; distinct effects cannot collide.
            int base=dmg & ~0x4060;
            info.key=(potionId<<16)|(base&0xffff);
            info.power=((dmg&0x20)!=0||amp>0)?1:0;
            info.length=(dmg&0x40)!=0?1:0;
            info.splash=(dmg&0x4000)!=0?1:0;
            if(info.power>0&&info.length>0)return null;
            return info;
        }catch(Throwable t){return null;}
    }
    public static boolean validPotion(Object stack){return potion(stack)!=null;}
    public static boolean ingestPotion(Object tile,Object item) {
        PotionInfo info=potion(item);if(info==null)return false;
        PotionState p=state(tile);
        Long prior=p.essence.get(info.key);
        if(prior!=null&&prior==Long.MAX_VALUE)return false;
        if(info.power>0&&p.charges[0]==Long.MAX_VALUE)return false;
        if(info.length>0&&p.charges[1]==Long.MAX_VALUE)return false;
        if(info.splash>0&&p.charges[2]==Long.MAX_VALUE)return false;
        p.essence.put(info.key,plus(prior==null?0:prior,1));
        p.charges[0]+=info.power;p.charges[1]+=info.length;p.charges[2]+=info.splash;
        change(tile);return true;
    }
    public static void tickPotion(Object tile) {
        Object world=field(tile,"field_145850_b");
        if(world==null||Boolean.TRUE.equals(field(world,"field_72995_K")))return;
        Object input=field(tile,"input");
        if(input==null||count(input)<=0||!ingestPotion(tile,input))return;
        count(input,count(input)-1);if(count(input)<=0)setField(tile,"input",null);
    }
    public static int potionCount(Object tile){return state(tile).essence.size();}
    public static long potionStock(Object tile,int index){int key=keyAt(tile,index);Long n=state(tile).essence.get(key);return n==null?0:n;}
    private static int keyAt(Object tile,int index){
        PotionState p=state(tile); if(index<0||index>=p.essence.size())return -1;
        Iterator<Integer> it=p.essence.keySet().iterator();for(int i=0;i<index;i++)it.next();return it.next();
    }
    public static void setPotionMode(Object container,int mode){if(mode>=0&&mode<=5)MODES.put(container,mode);}
    public static int mode(Object container){Integer x=MODES.get(container);return x==null?0:x;}
    private static int normalizedMode(int v){return v>=0&&v<=5?v:0;}
    private static int modeFlags(int mode){
        switch(mode){case 1:return 0x20;case 2:return 0x40;case 3:return 0x4000;case 4:return 0x4020;case 5:return 0x4040;default:return 0;}
    }
    private static int[] need(int mode){return new int[]{mode==1||mode==4?1:0,mode==2||mode==5?1:0,mode>=3?1:0};}
    private static Object potionStack(Object tile,int index,int mode){
        int key=keyAt(tile,index);if(key<0)return null;
        Object item=vanillaItem("potion");if(item==null)return null;
        return stack(item,(key&0xffff)|modeFlags(normalizedMode(mode)));
    }
    public static Object potionPreview(Object tile,int index){return potionStack(tile,index,0);}
    public static boolean potionExtract(Object tile,Object player,int index,int mode){
        if(player==null)return false;
        PotionState p=state(tile);int key=keyAt(tile,index);if(key<0)return false;
        Long avail=p.essence.get(key);if(avail==null||avail<=0)return false;
        int[] needed=need(normalizedMode(mode));
        for(int i=0;i<3;i++)if(p.charges[i]<needed[i])return false;
        Object item=potionStack(tile,index,mode);if(item==null)return false;
        if(!give(player,item))return false;
        if(avail==1)p.essence.remove(key);else p.essence.put(key,avail-1);
        for(int i=0;i<3;i++)p.charges[i]-=needed[i];
        change(tile);return true;
    }
    public static String charges(Object tile){PotionState p=state(tile);return "G:"+p.charges[0]+" R:"+p.charges[1]+" P:"+p.charges[2];}
    public static void writePotions(Object tile,Object nbt){
        PotionState p=state(tile);
        Object n=tagList();
        for(Map.Entry<Integer,Long> entry:p.essence.entrySet()){
            Object row=tagCompound();putInt(row,"key",entry.getKey());putLong(row,"count",entry.getValue());listAppend(n,row);
        }
        tagSet(nbt,"ATM0V2Essences",n);
        Object charge=tagCompound();putLong(charge,"glow",p.charges[0]);putLong(charge,"red",p.charges[1]);putLong(charge,"gun",p.charges[2]);tagSet(nbt,"ATM0V2Charges",charge);
        putInt(nbt,"ATM0V2Data",1);
    }
    @SuppressWarnings("unchecked")
    public static void readPotions(Object tile,Object nbt){
        PotionState p=new PotionState();POTIONS.put(tile,p);
        if(num(nbt,"func_74762_e","ATM0V2Data")==1){
            Object entries=list(nbt,"ATM0V2Essences");
            for(int i=0;i<listLength(entries);i++){
                Object row=listAt(entries,i);int key=num(row,"func_74762_e","key");long qty=lng(row,"func_74763_f","count");
                if(qty>0)p.essence.put(key,qty);
            }
            Object charge=call(nbt,"func_74775_l",STR,"ATM0V2Charges");
            p.charges[0]=Math.max(0,lng(charge,"func_74763_f","glow"));
            p.charges[1]=Math.max(0,lng(charge,"func_74763_f","red"));
            p.charges[2]=Math.max(0,lng(charge,"func_74763_f","gun"));
        }else{
            // Older saves: migrate only losslessly recognized single-effect potion entries.
            Object old=field(tile,"records");
            if(old instanceof List){
                Iterator<?> it=((List<?>)old).iterator();
                while(it.hasNext()){
                    Object rec=it.next();try{
                        int id=((Number)field(rec,"itemId")).intValue();int dmg=((Number)field(rec,"damage")).intValue();long qty=((Number)field(rec,"count")).longValue();
                        if(qty<=0)continue;
                        Object item=staticCall("net.minecraft.item.Item","func_150899_d",new Class<?>[]{int.class},id);
                        if(item==null)continue;
                        Object s=stack(item,dmg);Object tag=field(rec,"tag");
                        if(tag!=null)call(s,"func_77982_d",new Class<?>[]{c("net.minecraft.nbt.NBTTagCompound")},tag);
                        PotionInfo info=potion(s);if(info==null)continue;
                        p.essence.put(info.key,plus(p.essence.containsKey(info.key)?p.essence.get(info.key):0,qty));
                        p.charges[0]=plus(p.charges[0],info.power>0?qty:0);
                        p.charges[1]=plus(p.charges[1],info.length>0?qty:0);
                        p.charges[2]=plus(p.charges[2],info.splash>0?qty:0);
                        it.remove();
                    }catch(Throwable ignored){/* unsupported legacy entries remain intact */}
                }
            }
        }
    }

    private static Object bookList(Object item){
        Object nbt=getTag(item);if(nbt==null)return tagList();return list(nbt,"StoredEnchantments");
    }
    @SuppressWarnings("unchecked")
    public static boolean ingestEnchant(Object tile,Object item){
        if(item==null||count(item)<1)return false;
        Object l=bookList(item);int n=listLength(l);if(n<1)return false;
        Map<Integer,Object> records=(Map<Integer,Object>)field(tile,"records");
        // Prevalidate before mutating or consuming the input.
        ArrayList<int[]> parsed=new ArrayList<int[]>();
        for(int i=0;i<n;i++){
            Object row=listAt(l,i);
            int id=num(row,"func_74765_d","id")&0xffff;
            int level=num(row,"func_74765_d","lvl")&0xffff;
            long c=cost(level);if(level<1||c<=0||id>32767)return false;
            parsed.add(new int[]{id,level});
        }
        for(int[] p:parsed){
            Object rec=records.get(p[0]);
            if(rec==null){
                try{
                    Class<?> rc=c("dev.frostey.arcanelibrary.tile.TileEnchantmentLibrary$Record");
                    Constructor<?> cons=rc.getDeclaredConstructors()[0];cons.setAccessible(true);
                    Object[] args=new Object[cons.getParameterTypes().length];rec=cons.newInstance(args);
                    records.put(p[0],rec);
                }catch(Exception e){throw new IllegalStateException("enchantment record",e);}
            }
            long c=cost(p[1]);long pts=((Number)field(rec,"points")).longValue();
            setField(rec,"points",plus(pts,c));
            int max=((Number)field(rec,"maxLevel")).intValue();if(p[1]>max)setField(rec,"maxLevel",p[1]);
        }
        change(tile);return true;
    }
    private static long cost(int level){return level<1||level>62?0L:1L<<(level-1);}
    public static boolean selected(Object container){
        Object tile=field(container,"tile");int idx=((Number)field(container,"selectedIndex")).intValue();
        int id=((Number)call(tile,"enchantmentIdAt",new Class<?>[]{int.class},idx)).intValue();
        int lvl=((Number)field(container,"selectedLevel")).intValue();
        if(id<0||lvl<1||lvl>62||lvl>((Number)call(tile,"maxLevel",new Class<?>[]{int.class},id)).intValue())return false;
        long pts=((Number)call(tile,"points",new Class<?>[]{int.class},id)).longValue();if(pts<cost(lvl))return false;
        LinkedHashMap<Integer,Integer> q=BOOKS.get(container);if(q==null){q=new LinkedHashMap<Integer,Integer>();BOOKS.put(container,q);}
        if(q.containsKey(id)&&q.get(id)==lvl)q.remove(id);else q.put(id,lvl);
        return true;
    }
    public static int selectedCount(Object container){Map<?,?> q=BOOKS.get(container);return q==null?0:q.size();}
    @SuppressWarnings("unchecked")
    public static boolean emitMixed(Object container,Object player){
        Object tile=field(container,"tile");
        LinkedHashMap<Integer,Integer> q=BOOKS.get(container);
        if(q==null||q.isEmpty()){
            int idx=((Number)field(container,"selectedIndex")).intValue(),lvl=((Number)field(container,"selectedLevel")).intValue();
            q=new LinkedHashMap<Integer,Integer>();
            int id=((Number)call(tile,"enchantmentIdAt",new Class<?>[]{int.class},idx)).intValue();
            if(id>=0)q.put(id,lvl);
        }
        if(q.isEmpty()||q.size()>256)return false;
        Map<Integer,Object> records=(Map<Integer,Object>)field(tile,"records");
        for(Map.Entry<Integer,Integer> en:q.entrySet()){
            Object rec=records.get(en.getKey());long c=cost(en.getValue());
            if(rec==null||c<=0||en.getValue()>((Number)field(rec,"maxLevel")).intValue()||((Number)field(rec,"points")).longValue()<c)return false;
        }
        Object output=null;
        int i=0;
        for(Map.Entry<Integer,Integer> en:q.entrySet()){
            if(i++==0){output=call(tile,"makeBook",new Class<?>[]{int.class,int.class},en.getKey(),en.getValue());continue;}
            Object tag=getTag(output);Object list=list(tag,"StoredEnchantments");Object row=tagCompound();
            call(row,"func_74777_a",new Class<?>[]{String.class,short.class},"id",(short)(int)en.getKey());
            call(row,"func_74777_a",new Class<?>[]{String.class,short.class},"lvl",(short)(int)en.getValue());
            listAppend(list,row);tagSet(tag,"StoredEnchantments",list);
        }
        if(output==null||!give(player,output))return false;
        for(Map.Entry<Integer,Integer> en:q.entrySet()){
            Object rec=records.get(en.getKey());setField(rec,"points",((Number)field(rec,"points")).longValue()-cost(en.getValue()));
            // Preserve discovered maxLevel while remaining points are exhausted, so on next deposit highest level remains known.
        }
        change(tile);BOOKS.remove(container);return true;
    }
    public static void notifyTile(Object tile){
        try {
            Object world=field(tile,"field_145850_b");
            if(world==null||Boolean.TRUE.equals(field(world,"field_72995_K")))return;
            int x=((Number)field(tile,"field_145851_c")).intValue();
            int y=((Number)field(tile,"field_145848_d")).intValue();
            int z=((Number)field(tile,"field_145849_e")).intValue();
            call(world,"func_147471_g",new Class<?>[]{int.class,int.class,int.class},x,y,z);
        }catch(Throwable ignored){}
    }
    public static Object describeTile(Object tile){
        try{
            Object nbt=tagCompound();
            call(tile,"func_145841_b",new Class<?>[]{c("net.minecraft.nbt.NBTTagCompound")},nbt);
            int x=((Number)field(tile,"field_145851_c")).intValue();
            int y=((Number)field(tile,"field_145848_d")).intValue();
            int z=((Number)field(tile,"field_145849_e")).intValue();
            return c("net.minecraft.network.play.server.S35PacketUpdateTileEntity")
                  .getConstructor(int.class,int.class,int.class,int.class,c("net.minecraft.nbt.NBTTagCompound"))
                  .newInstance(x,y,z,1,nbt);
        }catch(Throwable e){return null;}
    }
    public static void receiveTile(Object tile,Object packet){
        try{
            Object nbt=call(packet,"func_148857_g",new Class<?>[0]);
            call(tile,"func_145839_a",new Class<?>[]{c("net.minecraft.nbt.NBTTagCompound")},nbt);
        }catch(Throwable ignored){}
    }
    public static boolean potionButton(Object container,Object player,int id){
        Object tile=field(container,"tile");if(tile==null)return false;
        if(id>=100&&id<10000){
            int count=((Number)call(tile,"entryCount",new Class<?>[0])).intValue();
            if(count<=0)return false;
            setField(container,"selectedIndex",Math.min(id-100,count-1));
        }else if(id>=10&&id<=13){togglePotionMode(container,id);}
        else if(id==4){return false;}else{return false;}
        int idx=((Number)field(container,"selectedIndex")).intValue();
        Object preview=potionStack(tile,idx,mode(container));
        call(tile,"setPreview",new Class<?>[]{stackClass()},preview);
        return true;
    }
    @SuppressWarnings("unchecked")
    public static void buildGui(Object gui,boolean potion){
        try {
            List<Object> b=(List<Object>)field(gui,"field_146292_n"); b.clear();
            Object con=field(gui,"field_147002_h");Object tile=field(con,"tile");
            int x=((Number)field(gui,"field_147003_i")).intValue();
            int y=((Number)field(gui,"field_147009_r")).intValue();
            int len=((Number)call(tile,"entryCount",new Class<?>[0])).intValue();
            int page=((Number)field(gui,"pageStart")).intValue();
            page=Math.min(Math.max(0,page),Math.max(0,len-5));
            setField(gui,"pageStart",page);
            int sel=((Number)field(gui,"localSelected")).intValue();
            b.add(button(200,x+132,y+17,12,12,"^") );
            b.add(button(201,x+132,y+118,12,12,"v") );
            for(int i=0;i<5&&i+page<len;i++){
                int idx=i+page;
                String label=(String)call(gui,"entryLabel",new Class<?>[]{int.class},idx);
                b.add(button(100+idx,x+20,y+30+i*20,111,19,(idx==sel?"> ":"  ")+label));
            }
            if(potion){
                b.add(button(10,x+4,y+132,39,15,"Normal"));
                b.add(button(11,x+44,y+132,39,15,"Strong"));
                b.add(button(12,x+84,y+132,39,15,"Long"));
                b.add(button(13,x+124,y+132,48,15,"Splash"));
                b.add(button(4,x+143,y+114,29,15,"Take"));
            }else{
                b.add(button(2,x+139,y+17,16,15,"-"));
                b.add(button(3,x+156,y+17,16,15,"+"));
                b.add(button(5,x+140,y+114,32,15,"Add"));
                b.add(button(4,x+140,y+132,32,15,"Take"));
                b.add(button(6,x+140,y+46,32,13,"Clear"));
            }
        }catch(Throwable t){throw new IllegalStateException("buildGui",t);}
    }
    private static Object button(int id,int x,int y,int w,int h,String txt)throws Exception{
        return c("net.minecraft.client.gui.GuiButton").getConstructor(int.class,int.class,int.class,int.class,int.class,String.class).newInstance(id,x,y,w,h,txt);
    }
    public static void guiClick(Object gui,Object btn,boolean potion){
        int id=((Number)field(btn,"field_146127_k")).intValue();
        Object container=field(gui,"field_147002_h");Object tile=field(container,"tile");
        if(id==200||id==201){
            int count=((Number)call(tile,"entryCount",new Class<?>[0])).intValue();
            int page=((Number)field(gui,"pageStart")).intValue();
            page=Math.max(0,Math.min(Math.max(0,count-5),page+(id==200?-5:5)));
            setField(gui,"pageStart",page);buildGui(gui,potion);return;
        }
        if(id>=100){setField(gui,"localSelected",id-100);buildGui(gui,potion);}
        if(potion&&id>=10&&id<=13){togglePotionMode(container,id);}
        try{
            Object mc=staticCall("net.minecraft.client.Minecraft","func_71410_x",new Class<?>[0]);
            Object controller=field(mc,"field_71442_b");
            int window=((Number)field(container,"field_75152_c")).intValue();
            call(controller,"func_78756_a",new Class<?>[]{int.class,int.class},window,id);
        }catch(Throwable t){throw new IllegalStateException("button click",t);}
    }
    public static void drawGui(Object gui,boolean potion){
        try{
            Object font=field(gui,"field_146289_q");if(font==null)return;
            Object container=field(gui,"field_147002_h");
            txt(font,potion?"Potion Library":"Enchantment Library",8,6,0x44372C);
            txt(font,"Inventory",8,151,0x554B45);
            if(potion){
                String s=charges(field(container,"tile"));
                String[] parts=s.split(" ");
                for(int i=0;i<parts.length;i++)txt(font,parts[i],144,37+i*10,i==0?0xE3A600:i==1?0xAD3434:0x555555);
            }else{
                txt(font,"Mix: "+selectedCount(container),140,66,0x703A70);
            }
        }catch(Throwable ignored){}
    }
    private static void txt(Object font,String msg,int x,int y,int color){
        call(font,"func_78261_a",new Class<?>[]{String.class,int.class,int.class,int.class},msg,x,y,color);
    }
    public static boolean enchantButton(Object container,Object player,int button) {
        if(button==4)return false; // Legacy Take is disabled.
        if(button==6){BOOKS.remove(container);return true;}
        if(button==5)return false; // No Add button; list clicks modify output directly.
        if(button<100 && button>=0)return false;
        return menuSelectEnchantment(container,button);
    }
    public static void togglePotionMode(Object container,int button){
        int prev=mode(container),v=prev;
        if(button==10)v=0;
        else if(button==11)v=(prev==3||prev==4||prev==5)?4:1;
        else if(button==12)v=(prev==3||prev==4||prev==5)?5:2;
        else if(button==13)v=prev==3?0:prev==4?1:prev==5?2:prev==1?4:prev==2?5:3;
        setPotionMode(container,v);
        Object tile=field(container,"tile");int idx=((Number)field(container,"selectedIndex")).intValue();
        Object preview=potionStack(tile,idx,mode(container));call(tile,"setPreview",new Class<?>[]{stackClass()},preview);
    }
    private static final Map<Object,Integer> LAST_GUI = Collections.synchronizedMap(new WeakHashMap<Object,Integer>());
    public static void refreshGui(Object gui){
        try {
            Object con=field(gui,"field_147002_h"); Object tile=field(con,"tile");
            int n=((Number)call(tile,"entryCount",new Class<?>[0])).intValue();
            Integer prev=LAST_GUI.get(gui);
            if(prev==null||prev.intValue()!=n){LAST_GUI.put(gui,n);call(gui,"rebuildButtons",new Class<?>[0]);}
        }catch(Throwable ignored){}
    }
    public static void drawExtra(Object gui,Object container,boolean potion){
        try {
            Object font=field(gui,"field_146289_q");if(font==null)return;
            String status=potion?charges(field(container,"tile")):"Mixed: "+selectedCount(container)+" | + to add";
            // small label rendered above the player inventory.
            call(font,"func_78276_b",new Class<?>[]{String.class,int.class,int.class,int.class},status,8,139,0x504540);
        }catch(Throwable ignored){}
    }
    public static Object quickInsert(Object container,Object tile,Object player,int slotIndex) {
        // Forge containers map 0 = input, 1 = preview; 2... are the player inventory slots.
        if(slotIndex<3)return null;
        Object world=field(tile,"field_145850_b");if(world==null||Boolean.TRUE.equals(field(world,"field_72995_K")))return null;
        Object slots=field(container,"field_75151_b");if(!(slots instanceof List)||slotIndex>=((List<?>)slots).size())return null;
        Object slot=((List<?>)slots).get(slotIndex);
        Object item=call(slot,"func_75211_c",new Class<?>[0]);
        if(item==null||count(item)<=0)return null;
        boolean potion=tile.getClass().getName().endsWith("TilePotionLibrary");
        Object copy=call(item,"func_77946_l",new Class<?>[0]);
        if(!(potion?ingestPotion(tile,item):ingestEnchant(tile,item)))return null;
        count(item,count(item)-1);
        if(count(item)<=0)call(slot,"func_75215_d",new Class<?>[]{stackClass()},(Object)null);
        else call(slot,"func_75218_e",new Class<?>[0]);
        return copy;
    }

    /** Sotnah-like row interactions. Never spend points until actual output removal. */
    private static boolean menuSelectEnchantment(Object container,int encoded){
        boolean shift=(encoded&0x80000000)!=0,ctrl=(encoded&0x40000000)!=0;
        int index=(encoded&0x3fffffff)-100;
        Object tile=field(container,"tile");
        int size=((Number)call(tile,"entryCount",new Class<?>[0])).intValue();
        if(index<0||index>=size)return false;
        int id=((Number)call(tile,"enchantmentIdAt",new Class<?>[]{int.class},index)).intValue();
        int max=((Number)call(tile,"maxLevel",new Class<?>[]{int.class},id)).intValue();
        if(max<1)return false;
        LinkedHashMap<Integer,Integer> q=BOOKS.get(container);
        if(q==null){q=new LinkedHashMap<Integer,Integer>();BOOKS.put(container,q);}
        int previous=q.containsKey(id)?q.get(id):0;
        int next;
        if(ctrl)next=shift?0:Math.max(0,previous-1);
        else if(shift) {
            next=previous;
            for(int n=previous+1;n<=Math.min(max,62);n++){
                q.put(id,n);if(menuCanSpend(container))next=n;else break;
            }
        }else next=Math.min(Math.min(max,62),previous+1);
        if(next==0)q.remove(id);else q.put(id,next);
        if(!menuCanSpend(container)&&next>0){if(previous==0)q.remove(id);else q.put(id,previous);return false;}
        setField(container,"selectedIndex",index);
        setField(container,"selectedLevel",Math.max(1,next));
        call(container,"refreshPreview",new Class<?>[0]);
        return true;
    }
    public static Object mixedMenuPreview(Object container){
        LinkedHashMap<Integer,Integer> selection=BOOKS.get(container);
        if(selection==null||selection.isEmpty()||!menuCanSpend(container))return null;
        Object tile=field(container,"tile"), book=null;
        int i=0;
        for(Map.Entry<Integer,Integer> en:selection.entrySet()){
            if(i++==0){
                book=call(tile,"makeBook",new Class<?>[]{int.class,int.class},en.getKey(),en.getValue());
                continue;
            }
            Object tag=getTag(book), ls=list(tag,"StoredEnchantments"), row=tagCompound();
            call(row,"func_74777_a",new Class<?>[]{String.class,short.class},"id",(short)(int)en.getKey());
            call(row,"func_74777_a",new Class<?>[]{String.class,short.class},"lvl",(short)(int)en.getValue());
            listAppend(ls,row);tagSet(tag,"StoredEnchantments",ls);
        }
        return book;
    }
    public static Object potionMenuPreview(Object container){
        Object tile=field(container,"tile");
        int idx=((Number)field(container,"selectedIndex")).intValue();
        return menuCanSpend(container)?potionStack(tile,idx,mode(container)):null;
    }
    @SuppressWarnings("unchecked")
    public static boolean menuCanSpend(Object container){
        Object tile=field(container,"tile");
        if(tile==null)return false;
        boolean potion=tile.getClass().getName().endsWith("TilePotionLibrary");
        if(potion){
            PotionState p=state(tile);
            int index=((Number)field(container,"selectedIndex")).intValue(), key=keyAt(tile,index);
            if(key<0||!p.essence.containsKey(key)||p.essence.get(key)<1)return false;
            int[] required=need(mode(container));
            for(int i=0;i<3;i++)if(p.charges[i]<required[i])return false;
            return potionStack(tile,index,mode(container))!=null;
        }
        LinkedHashMap<Integer,Integer> q=BOOKS.get(container);
        if(q==null||q.isEmpty()||q.size()>256)return false;
        Map<Integer,Object> records=(Map<Integer,Object>)field(tile,"records");
        if(records==null)return false;
        for(Map.Entry<Integer,Integer> e:q.entrySet()){
            Object rec=records.get(e.getKey());
            int level=e.getValue();long cost=cost(level);
            if(rec==null||cost<1||level>((Number)field(rec,"maxLevel")).intValue()
                ||((Number)field(rec,"points")).longValue()<cost)return false;
        }
        return true;
    }
    @SuppressWarnings("unchecked")
    public static boolean menuSpend(Object container){
        if(!menuCanSpend(container))return false;
        Object tile=field(container,"tile");
        if(tile.getClass().getName().endsWith("TilePotionLibrary")){
            PotionState p=state(tile);
            int key=keyAt(tile,((Number)field(container,"selectedIndex")).intValue());
            long qty=p.essence.get(key);
            if(qty==1)p.essence.remove(key);else p.essence.put(key,qty-1);
            int[] req=need(mode(container));
            for(int i=0;i<3;i++)p.charges[i]-=req[i];
        }else{
            LinkedHashMap<Integer,Integer> q=BOOKS.get(container);
            Map<Integer,Object> records=(Map<Integer,Object>)field(tile,"records");
            for(Map.Entry<Integer,Integer> e:q.entrySet()){
                Object rec=records.get(e.getKey());
                long remain=((Number)field(rec,"points")).longValue()-cost(e.getValue());
                setField(rec,"points",remain);
            }
            BOOKS.remove(container);
        }
        change(tile);
        return true;
    }

}
