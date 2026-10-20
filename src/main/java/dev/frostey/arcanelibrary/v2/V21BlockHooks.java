package dev.frostey.arcanelibrary.v2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Safe pick-up / replacement bridge for the Minecraft 1.7.10 (SRG) block API.
 *
 * Neither a separate item registry nor client-side world caches are required.
 * The dropped ItemStack contains ONLY library storage (never the original
 * TileEntity coordinates or identity). Both blocks use the same code path.
 */
public final class V21BlockHooks {
    private static final String DATA_KEY = "ATM0LibraryData";
    private static final String STORAGE_KEY = "Storage";
    private static final int FORMAT = 1;
    private V21BlockHooks() {}

    private static Class<?> clazz(String name) {
        try { return Class.forName(name); }
        catch (ClassNotFoundException e) { throw new IllegalStateException(name, e); }
    }

    private static Object invoke(Object instance, String method, Class<?>[] types, Object... args) {
        if (instance == null) throw new IllegalStateException("Missing instance: " + method);
        try {
            Class<?> type = instance.getClass();
            while (type != null) {
                try {
                    Method m = type.getDeclaredMethod(method, types);
                    m.setAccessible(true);
                    return m.invoke(instance, args);
                } catch (NoSuchMethodException ignored) { type = type.getSuperclass(); }
            }
            throw new NoSuchMethodException(method + " on " + instance.getClass());
        } catch (InvocationTargetException e) { throw new IllegalStateException(method, e.getCause()); }
        catch (Exception e) { throw new IllegalStateException(method, e); }
    }

    private static Object newCompound() {
        try { return clazz("net.minecraft.nbt.NBTTagCompound").newInstance(); }
        catch (Exception e) { throw new IllegalStateException("NBT", e); }
    }
    private static Object getTile(Object world, int x, int y, int z) {
        return invoke(world, "func_147438_o", new Class<?>[] {int.class,int.class,int.class}, x,y,z);
    }
    private static String type(Object block) {
        String name = block.getClass().getName();
        if (name.equals("dev.frostey.arcanelibrary.block.BlockEnchantmentLibrary")) return "enchantment";
        if (name.equals("dev.frostey.arcanelibrary.block.BlockPotionLibrary")) return "potion";
        throw new IllegalArgumentException("Unexpected Arcane Library block: " + name);
    }
    private static boolean expectedTile(Object tile, String kind) {
        return tile != null && tile.getClass().getName().equals(
                "dev.frostey.arcanelibrary.tile.Tile" + ("potion".equals(kind) ? "Potion" : "Enchantment") + "Library");
    }
    private static boolean remote(Object world) {
        try {
            Field f = world.getClass().getField("field_72995_K");
            return f.getBoolean(world);
        } catch (Exception e) { throw new IllegalStateException("World.isRemote",e); }
    }
    private static boolean tagType(Object compound,String name,int type) {
        return Boolean.TRUE.equals(invoke(compound,"func_150297_b",new Class<?>[] {String.class,int.class},name,type));
    }
    private static Object cloneTag(Object nbt) {
        return invoke(nbt,"func_74737_b",new Class<?>[0]);
    }
    private static void putTag(Object parent,String key,Object nbt) {
        invoke(parent,"func_74782_a",new Class<?>[] {String.class,clazz("net.minecraft.nbt.NBTBase")},key,nbt);
    }
    private static void putInt(Object parent,String key,int n) {
        invoke(parent,"func_74768_a",new Class<?>[] {String.class,int.class},key,n);
    }
    private static void putString(Object parent,String key,String s) {
        invoke(parent,"func_74778_a",new Class<?>[] {String.class,String.class},key,s);
    }
    private static Object getCompound(Object parent,String key) {
        return invoke(parent,"func_74775_l",new Class<?>[] {String.class},key);
    }
    private static String getString(Object parent,String key) {
        return (String) invoke(parent,"func_74779_i",new Class<?>[] {String.class},key);
    }
    private static int getInt(Object parent,String key) {
        return ((Number)invoke(parent,"func_74762_e",new Class<?>[] {String.class},key)).intValue();
    }
    private static Object blockItem(Object block) {
        try {
            Method m=clazz("net.minecraft.item.Item").getMethod("func_150898_a",clazz("net.minecraft.block.Block"));
            return m.invoke(null,block);
        } catch (InvocationTargetException e) { throw new IllegalStateException("Block Item",e.getCause()); }
        catch (Exception e) { throw new IllegalStateException("Block Item",e); }
    }
    private static Object itemStack(Object item,int metadata) {
        try {
            return clazz("net.minecraft.item.ItemStack")
                .getConstructor(clazz("net.minecraft.item.Item"),int.class,int.class)
                .newInstance(item,1,metadata);
        } catch (Exception e) { throw new IllegalStateException("ItemStack",e); }
    }

    /** Called by each block's Forge getDrops override, before its TileEntity disappears. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ArrayList getDrops(Object block,Object world,int x,int y,int z,int metadata,int fortune) {
        ArrayList result = new ArrayList(1);
        if (remote(world)) return result; // Only the server actually creates drops.
        Object item=blockItem(block);
        if (item == null) return result;
        Object drop=itemStack(item,metadata);
        Object tile=getTile(world,x,y,z);
        if (expectedTile(tile,type(block))) {
            Object storage=newCompound();
            // Includes legacy Enchantments/Potions and the v2 Potion Essences/Charges.
            invoke(tile,"writeStorage",new Class<?>[] {clazz("net.minecraft.nbt.NBTTagCompound")},storage);
            Object wrapped=newCompound();
            putInt(wrapped,"Format",FORMAT);
            putString(wrapped,"Kind",type(block));
            putTag(wrapped,STORAGE_KEY,storage);
            Object stackTag=newCompound();
            putTag(stackTag,DATA_KEY,wrapped);
            invoke(drop,"func_77982_d",new Class<?>[] {clazz("net.minecraft.nbt.NBTTagCompound")},stackTag);
        }
        result.add(drop); // Exactly one block item; never append a second vanilla drop.
        return result;
    }

    /**
     * Keep the block and its tile alive through getDrops() during survival
     * harvesting. Creative mode (willHarvest=false) removes it immediately.
     */
    public static boolean removedByPlayer(Object block,Object world,Object player,int x,int y,int z,boolean willHarvest) {
        if (willHarvest) return true;
        return Boolean.TRUE.equals(invoke(world,"func_147468_f",new Class<?>[] {int.class,int.class,int.class},x,y,z));
    }

    /** Called at the END of Block.harvestBlock after the vanilla drop logic. */
    public static void finishHarvest(Object world,int x,int y,int z) {
        invoke(world,"func_147468_f",new Class<?>[] {int.class,int.class,int.class},x,y,z);
    }

    /** Restore serialized storage on ordinary placement. Mismatched/malformed tags are rejected. */
    public static void onPlaced(Object block,Object world,int x,int y,int z,Object stack) {
        if (remote(world) || stack == null) return;
        Object tag = invoke(stack,"func_77978_p",new Class<?>[0]);
        if(tag==null || !tagType(tag,DATA_KEY,10)) return;
        Object wrapped=getCompound(tag,DATA_KEY);
        if(getInt(wrapped,"Format")!=FORMAT || !type(block).equals(getString(wrapped,"Kind")) ||
           !tagType(wrapped,STORAGE_KEY,10)) return;
        Object tile=getTile(world,x,y,z);
        if(!expectedTile(tile,type(block))) return;
        // Storage-only restore avoids accidentally resetting the new tile's coordinates.
        Object storage=cloneTag(getCompound(wrapped,STORAGE_KEY));
        invoke(tile,"readStorage",new Class<?>[] {clazz("net.minecraft.nbt.NBTTagCompound")},storage);
        invoke(tile,"storageChanged",new Class<?>[0]);
    }
}
