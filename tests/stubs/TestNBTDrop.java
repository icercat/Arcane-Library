
import dev.frostey.arcanelibrary.v2.*;
import dev.frostey.arcanelibrary.tile.*;
import dev.frostey.arcanelibrary.block.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import java.util.*;
public class TestNBTDrop {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 @SuppressWarnings("rawtypes")
 public static void main(String[] args){
   Object bookBlock=new BlockEnchantmentLibrary();
   TileEnchantmentLibrary t1=new TileEnchantmentLibrary();
   TileEnchantmentLibrary.Record r=new TileEnchantmentLibrary.Record();r.points=348234L;r.maxLevel=12;t1.records.put(47,r);
   World world=new World(false,t1);
   check(V21BlockHooks.removedByPlayer(bookBlock,world,null,1,64,2,true),"survival delays break");
   check(world.func_147438_o(1,64,2)==t1,"tile must still exist at getDrops");
   ArrayList drops=V21BlockHooks.getDrops(bookBlock,world,1,64,2,0,3);
   check(drops.size()==1,"exactly one block drop");
   ItemStack item=(ItemStack)drops.get(0);
   NBTTagCompound wrapped=item.tag.func_74775_l("ATM0LibraryData");
   check(wrapped.func_74779_i("Kind").equals("enchantment"),"enchant type");
   check(wrapped.func_74775_l("Storage").func_150295_c("Enchantments",10).func_74745_c()==1,"serialized enchantment");
   check(!wrapped.func_74775_l("Storage").data.containsKey("x"),"no tile coordinates in dropped NBT");
   V21BlockHooks.finishHarvest(world,1,64,2);
   check(world.removes==1&&world.func_147438_o(1,64,2)==null,"after drop: removed once");
   TileEnchantmentLibrary t2=new TileEnchantmentLibrary();world.place(t2);
   V21BlockHooks.onPlaced(bookBlock,world,55,80,81,item);
   check(t2.records.get(47).points==348234L&&t2.records.get(47).maxLevel==12,"enchant stored state restored");
   check(t2.changes==1,"enchant state sync notified");
   // Empty/non-library NBT is ignored rather than destroying existing tile data.
   ItemStack plain=new ItemStack(item.item,1,0);
   V21BlockHooks.onPlaced(bookBlock,world,1,64,2,plain);
   check(t2.records.get(47).points==348234L,"ordinary untagged blocks unaffected");
   TileEnchantmentLibrary.Record unchanged=t2.records.get(47);
   Object potBlock=new BlockPotionLibrary();
   V21BlockHooks.onPlaced(potBlock,world,1,64,2,item);
   check(t2.records.get(47)==unchanged,"wrong kind must not apply");
   // Creative removal must not create a drop via our player-harvest hook.
   check(V21BlockHooks.removedByPlayer(bookBlock,world,null,55,80,81,false),"creative removes block");
   check(world.removes==2,"creative removed exactly once");

   TilePotionLibrary pot=new TilePotionLibrary();
   NBTTagCompound state=new NBTTagCompound(),ent=new NBTTagCompound(),chg=new NBTTagCompound();
   NBTTagList entries=new NBTTagList();ent.func_74768_a("key",9101);ent.func_74772_a("count",17L);entries.func_74742_a(ent);
   state.func_74768_a("ATM0V2Data",1);state.func_74782_a("ATM0V2Essences",entries);
   chg.func_74772_a("glow",9L);chg.func_74772_a("red",3L);chg.func_74772_a("gun",5L);
   state.func_74782_a("ATM0V2Charges",chg);V2Hooks.readPotions(pot,state);
   World pw=new World(false,pot);
   check(V21BlockHooks.removedByPlayer(potBlock,pw,null,1,2,3,true),"potion delayed destruction");
   ArrayList pdrop=V21BlockHooks.getDrops(potBlock,pw,1,2,3,0,0);
   check(pdrop.size()==1,"exactly one potion library drop");
   ItemStack potionItem=(ItemStack)pdrop.get(0);
   NBTTagCompound pst=potionItem.tag.func_74775_l("ATM0LibraryData").func_74775_l("Storage");
   V21BlockHooks.finishHarvest(pw,1,2,3);
   TilePotionLibrary pot2=new TilePotionLibrary();pw.place(pot2);
   V21BlockHooks.onPlaced(potBlock,pw,5,6,7,potionItem);
   check(V2Hooks.potionCount(pot2)==1,"potion essence count restored");
   check(V2Hooks.charges(pot2).equals("G:9 R:3 P:5"),"all charges restored");
   check(pot2.changes==1,"potion NBT restored and notified");
   check(pst.func_74775_l("ATM0V2Charges").func_74763_f("glow")==9,"persisted potion charge" );
   // Missing tile returns a plain block, never two items.
   pw.func_147468_f(5,6,7);
   ArrayList noTile=V21BlockHooks.getDrops(potBlock,pw,5,6,7,0,0);
   check(noTile.size()==1 && ((ItemStack)noTile.get(0)).tag==null,"missing tile plain block");
   System.out.println("PASS: 0.2.1 NBT drops, delayed removal, restore, storage parity, creative path, no dupes");
 }
}
