package dev.frostey.arcanelibrary.v2;

import java.lang.reflect.*;
import java.util.*;

/**
 * Per-menu output slot bridge. 1.7.10 SlotReadOnly is patched to use these
 * methods, so the preview is NEVER an independently owned inventory stack.
 */
public final class V22OutputHooks {
    private V22OutputHooks(){}
    private static final Map<Object,Object> SLOTS=Collections.synchronizedMap(new WeakHashMap<Object,Object>());
    private static final Map<Object,Object> FILTERS=Collections.synchronizedMap(new WeakHashMap<Object,Object>());
    private static final Map<Object,Object> CLIENT_PREVIEWS=Collections.synchronizedMap(new WeakHashMap<Object,Object>());
    private static Class<?> cl(String s){try{return Class.forName(s);}catch(Exception e){throw new IllegalStateException(s,e);}}
    private static Object call(Object obj,String m,Class<?>[] t,Object... args){
        try {
            for(Class<?> k=obj.getClass();k!=null;k=k.getSuperclass()){
                try{Method method=k.getDeclaredMethod(m,t);method.setAccessible(true);return method.invoke(obj,args);}catch(NoSuchMethodException ignored){}
            }
            throw new NoSuchMethodException(m);
        } catch(InvocationTargetException e){throw new IllegalStateException(m,e.getCause());}
          catch(Exception e){throw new IllegalStateException(m,e);}
    }
    private static Class<?> stackType(){return cl("net.minecraft.item.ItemStack");}
    private static Object inventory(Object player){return V2Hooks.field(player,"field_71071_by");}
    private static Object held(Object player){return call(inventory(player),"func_70445_o",new Class<?>[0]);}
    private static Object item(Object stack){return stack==null?null:call(stack,"func_77946_l",new Class<?>[0]);}
    private static int count(Object stack){return stack==null?0:((Number)V2Hooks.field(stack,"field_77994_a")).intValue();}
    private static List<?> slots(Object container){return (List<?>)V2Hooks.field(container,"field_75151_b");}
    private static boolean client(Object container){
        Object tile=V2Hooks.field(container,"tile");
        Object world=V2Hooks.field(tile,"field_145850_b");
        return world==null||Boolean.TRUE.equals(V2Hooks.field(world,"field_72995_K"));
    }
    public static void bind(Object container){
        List<?> list=slots(container);
        if(list!=null&&list.size()>=2)SLOTS.put(list.get(1),container);
        if(list!=null&&list.size()>=3)FILTERS.put(container,list.get(2));
    }
    private static Object owner(Object slot){return SLOTS.get(slot);}
    public static Object preview(Object slot){
        Object container=owner(slot);
        if(container==null)return null;
        // Client output is synced by vanilla Container set-slot packets; never fabricate from unsynced menu selections.
        if(client(container))return CLIENT_PREVIEWS.get(slot);
        boolean potion=container.getClass().getName().endsWith("ContainerPotionLibrary");
        return potion ? V2Hooks.potionMenuPreview(container) : V2Hooks.mixedMenuPreview(container);
    }
    public static boolean canTake(Object slot,Object player){
        Object container=owner(slot);
        return container!=null&&player!=null&&held(player)==null&&preview(slot)!=null
            &&(client(container)||V2Hooks.menuCanSpend(container));
    }
    // Virtual output: splitting does not mutate TileEntity inventory.
    public static Object takePart(Object slot,int amount){
        Object p=preview(slot);
        if(p==null||amount<1)return null;
        return item(p);
    }
    public static void ignoreSet(Object slot,Object stack){
        Object container=owner(slot);
        if(container!=null && client(container)){
            if(stack==null)CLIENT_PREVIEWS.remove(slot);
            else CLIENT_PREVIEWS.put(slot,item(stack));
        }
    }
    public static boolean rejectInput(Object slot,Object stack){return false;}
    public static void onPickup(Object slot,Object player,Object stack){
        Object container=owner(slot);
        if(container==null||stack==null||client(container))return;
        if(!V2Hooks.menuSpend(container))throw new IllegalStateException("Output transaction failed after canTake");
        call(container,"refreshPreview",new Class<?>[0]);
    }
    /**
     * Only support vanilla normal-click and shift-click on Output.
     * Blocks number-key swaps, double-click, drag splitting and creative
     * middle click. Those interaction families must not mint preview items.
     */
    public static boolean blockedOutputClick(Object container,int slotId,int mode){
        return slotId==1&&mode!=0&&mode!=1;
    }
    public static Object shiftOutput(Object container,Object player){
        if(client(container))return null;
        Object slot=slots(container).get(1);
        Object p=preview(slot);
        if(p==null||!V2Hooks.menuCanSpend(container))return null;
        Object copy=item(p);
        // One output item, no partial insert. Must not charge if inventory is full.
        boolean inserted=Boolean.TRUE.equals(call(inventory(player),"func_70441_a",new Class<?>[]{stackType()},copy));
        if(!inserted)return null;
        if(!V2Hooks.menuSpend(container))throw new IllegalStateException("Shift output failed after inventory insertion");
        call(container,"refreshPreview",new Class<?>[0]);
        return copy;
    }
    /** Close GUI: return the temporary item-filter stack without copying it. */
    public static void onClose(Object container,Object player){
        List<?> list=slots(container);
        if(list!=null&&list.size()>1)CLIENT_PREVIEWS.remove(list.get(1));
        Object f=FILTERS.remove(container);
        if(f==null||player==null)return;
        Object stack=call(f,"func_75211_c",new Class<?>[0]);
        if(stack==null)return;
        call(f,"func_75215_d",new Class<?>[]{stackType()},(Object)null);
        if(!Boolean.TRUE.equals(call(inventory(player),"func_70441_a",new Class<?>[]{stackType()},stack))){
            call(player,"func_71019_a",new Class<?>[]{stackType(),boolean.class},stack,false);
        }
    }
    public static Object quickMove(Object container,Object player,int slotId){
        List<?> all=slots(container);
        if(all==null||slotId<0||slotId>=all.size())return null;
        if(slotId==1)return shiftOutput(container,player);
        if(slotId==0||slotId==2)return null;
        // 3..38 are the main inventory and hotbar (36 real slots).
        Object tile=V2Hooks.field(container,"tile");
        if(tile==null)return null;
        Object result=V2Hooks.quickInsert(container,tile,player,slotId);
        if(result!=null)return result;
        // Unsupported input is NOT consumed. Manual placement in filter remains possible.
        return null;
    }
    /**
     * Delegates normal container clicks to the parent implementation.
     * Output slot interaction is limited to normal pickup or Shift pickup.
     */
    public static Object slotClick(Object container,int slotId,int button,int mode,Object player){
        if(blockedOutputClick(container,slotId,mode))return null;
        if(slotId==1&&mode==1)return shiftOutput(container,player);
        return call(container,"ATM0superClick",
            new Class<?>[]{int.class,int.class,int.class,cl("net.minecraft.entity.player.EntityPlayer")},
            slotId,button,mode,player);
    }

}