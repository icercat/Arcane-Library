from pathlib import Path
b=Path('/mnt/data/atm0_v2work/testsrc'); b.mkdir(parents=True,exist_ok=True)
def f(path,txt):
 p=b/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(txt)
f('net/minecraft/nbt/NBTBase.java','package net.minecraft.nbt; public class NBTBase {}')
f('net/minecraft/nbt/NBTTagList.java','''package net.minecraft.nbt;import java.util.*;
public class NBTTagList extends NBTBase{
 public java.util.List<NBTBase> contents=new ArrayList<>();
 public void func_74742_a(NBTBase b){contents.add(b);}
 public int func_74745_c(){return contents.size();}
 public NBTTagCompound func_150305_b(int n){return (NBTTagCompound)contents.get(n);}
}''')
f('net/minecraft/nbt/NBTTagCompound.java','''package net.minecraft.nbt;import java.util.*;
public class NBTTagCompound extends NBTBase{
 public HashMap<String,Object> data=new HashMap<>();
 public void func_74772_a(String s,long n){data.put(s,n);}
 public void func_74768_a(String s,int n){data.put(s,n);}
 public void func_74777_a(String s,short n){data.put(s,n);}
 public void func_74782_a(String s,NBTBase n){data.put(s,n);}
 public int func_74762_e(String s){return ((Number)data.getOrDefault(s,0)).intValue();}
 public long func_74763_f(String s){return ((Number)data.getOrDefault(s,0)).longValue();}
 public short func_74765_d(String s){return ((Number)data.getOrDefault(s,0)).shortValue();}
 public boolean func_74764_b(String s){return data.containsKey(s);}
 public NBTTagList func_150295_c(String s,int type){return data.containsKey(s)?(NBTTagList)data.get(s):new NBTTagList();}
 public NBTTagCompound func_74775_l(String s){return data.containsKey(s)?(NBTTagCompound)data.get(s):new NBTTagCompound();}
 public NBTBase func_74737_b(){NBTTagCompound t=new NBTTagCompound();t.data.putAll(data);return t;}
}''')
f('net/minecraft/item/Item.java','''package net.minecraft.item;import java.util.*;
public class Item {
  static Map<Integer,Item> ids=new HashMap<>();
  public Item(){ids.put(373,this);}
  public static Item func_150899_d(int id){return ids.get(id);}
}''')
f('net/minecraft/potion/PotionEffect.java','''package net.minecraft.potion;
public class PotionEffect{
 private int id, amp;public PotionEffect(int id,int amp){this.id=id;this.amp=amp;}
 public int func_76456_a(){return id;} public int func_76458_c(){return amp;}
}''')
f('net/minecraft/item/ItemPotion.java','''package net.minecraft.item;import java.util.*;import net.minecraft.potion.PotionEffect;
public class ItemPotion extends Item{
 public List<PotionEffect> func_77832_l(ItemStack s){
  if(s.tag!=null&&s.tag.data.containsKey("CustomPotionEffects"))return Arrays.asList(new PotionEffect(1,0),new PotionEffect(5,0));
  return Arrays.asList(new PotionEffect(s.damage&15,(s.damage&0x20)!=0?1:0));
 }
}''')
f('net/minecraft/item/ItemStack.java','''package net.minecraft.item;import net.minecraft.nbt.*;
public class ItemStack{
 public int field_77994_a;public int damage;public Item item; public NBTTagCompound tag;
 public ItemStack(Item i,int size,int d){item=i;damage=d;field_77994_a=size;}
 public Item func_77973_b(){return item;}
 public int func_77960_j(){return damage;}
 public NBTTagCompound func_77978_p(){return tag;}
 public void func_77982_d(NBTTagCompound n){tag=n;}
 public ItemStack func_77946_l(){ItemStack s=new ItemStack(item,field_77994_a,damage);s.tag=tag;return s;}
}''')
f('cpw/mods/fml/common/registry/GameRegistry.java','''package cpw.mods.fml.common.registry;
import net.minecraft.item.*;
public class GameRegistry{
 public static ItemPotion POTION=new ItemPotion();
 public static Item findItem(String m,String n){return n.equals("potion")?POTION:n.equals("enchanted_book")?new Item():null;}
}''')
f('net/minecraft/entity/player/InventoryPlayer.java','''package net.minecraft.entity.player;
import net.minecraft.item.ItemStack;import java.util.*;
public class InventoryPlayer{public ArrayList<ItemStack> items=new ArrayList<>();public boolean allow=true;
 public boolean func_70441_a(ItemStack s){if(!allow)return false;items.add(s);return true;}}
''')
f('net/minecraft/entity/player/EntityPlayer.java','''package net.minecraft.entity.player;
public class EntityPlayer{public InventoryPlayer field_71071_by=new InventoryPlayer();}
''')
f('dev/frostey/arcanelibrary/tile/TileEnchantmentLibrary.java','''package dev.frostey.arcanelibrary.tile;
public class TileEnchantmentLibrary{
 public static class Record{public long points;public int maxLevel;}
}''')
f('TestV2.java','''import dev.frostey.arcanelibrary.v2.V2Hooks;
import net.minecraft.item.*;import net.minecraft.nbt.*;import net.minecraft.entity.player.*;import cpw.mods.fml.common.registry.GameRegistry;
import java.util.*;
public class TestV2{
 public static class World{public boolean field_72995_K=false;}
 public static class FakePotionTile{
   public World field_145850_b=new World();public ItemStack input=null;
   public List<Object> records=new ArrayList<>();public int changes=0;
   public void storageChanged(){changes++;}public void setPreview(ItemStack s){}
 }
 public static class OldRecord{public int itemId=373,damage;public long count;public NBTTagCompound tag;}
 public static class MockEnchantTile{
   public Map<Integer,Object> records=new HashMap<>();public int changes=0;
   public void storageChanged(){changes++;}
   public int entryCount(){return records.size();}public int enchantmentIdAt(int idx){return new ArrayList<>(records.keySet()).get(idx);}
   public int maxLevel(int id){return ((dev.frostey.arcanelibrary.tile.TileEnchantmentLibrary.Record)records.get(id)).maxLevel;}
   public long points(int id){return ((dev.frostey.arcanelibrary.tile.TileEnchantmentLibrary.Record)records.get(id)).points;}
   public ItemStack makeBook(int id,int lv){ItemStack book=new ItemStack(new Item(),1,0);NBTTagCompound tag=new NBTTagCompound();NBTTagList list=new NBTTagList();
      NBTTagCompound row=new NBTTagCompound();row.func_74777_a("id",(short)id);row.func_74777_a("lvl",(short)lv);list.func_74742_a(row);tag.func_74782_a("StoredEnchantments",list);book.tag=tag;return book;}
 }
 public static class FakeContainer{public MockEnchantTile tile;public int selectedIndex=0,selectedLevel=1;
  public FakeContainer(MockEnchantTile tile){this.tile=tile;}public void refreshPreview(){}
 }
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 static ItemStack p(int dmg){return new ItemStack(GameRegistry.POTION,1,dmg);}
 public static void main(String[]a){
   FakePotionTile t=new FakePotionTile();
   ok(V2Hooks.validPotion(p(8194)),"normal valid");
   ok(V2Hooks.ingestPotion(t,p(8194)),"normal deposit");
   ok(V2Hooks.ingestPotion(t,p(8194|0x20)),"strong deposit");
   ok(V2Hooks.ingestPotion(t,p(8194|0x40|0x4000)),"splash-long deposit");
   ok(V2Hooks.potionCount(t)==1,"homogenized grouping");
   ok(V2Hooks.charges(t).equals("G:1 R:1 P:1"),"charge extraction from inputs "+V2Hooks.charges(t));
   ItemStack invalid=p(8194);invalid.tag=new NBTTagCompound();invalid.tag.data.put("CustomPotionEffects",new NBTTagList());
   ok(!V2Hooks.validPotion(invalid),"reject multi effects");
   EntityPlayer player=new EntityPlayer();
   ok(V2Hooks.potionExtract(t,player,0,4),"extract splash-strong with charges");
   ok(player.field_71071_by.items.size()==1,"output exactly 1 potion");
   ok(player.field_71071_by.items.get(0).damage==(8194|0x20|0x4000),"output variant");
   ok(V2Hooks.charges(t).equals("G:0 R:1 P:0"),"charges decremented");
   ok(!V2Hooks.potionExtract(t,player,0,4),"cannot overdraft charges");
   NBTTagCompound nbt=new NBTTagCompound();V2Hooks.writePotions(t,nbt);
   FakePotionTile t2=new FakePotionTile();V2Hooks.readPotions(t2,nbt);
   ok(V2Hooks.potionCount(t2)==1&&V2Hooks.charges(t2).equals("G:0 R:1 P:0"),"NBT persistence");
   FakePotionTile legacy=new FakePotionTile();OldRecord rec=new OldRecord();rec.damage=8194|0x20;rec.count=4;legacy.records.add(rec);
   NBTTagCompound old=new NBTTagCompound();V2Hooks.readPotions(legacy,old);
   ok(V2Hooks.potionCount(legacy)==1&&V2Hooks.charges(legacy).startsWith("G:4"),"legacy migration");
   ok(legacy.records.size()==0,"migration clears recognized legacy entries");
   // Enchanted books with multiple effects accumulate independently.
   MockEnchantTile en=new MockEnchantTile();ItemStack book=new ItemStack(new Item(),1,0);
   NBTTagCompound n=new NBTTagCompound();NBTTagList l=new NBTTagList();
   for(int id:new int[]{0,16}){NBTTagCompound row=new NBTTagCompound();row.func_74777_a("id",(short)id);row.func_74777_a("lvl",(short)(id==0?4:5));l.func_74742_a(row);}
   n.func_74782_a("StoredEnchantments",l);book.tag=n;
   ok(V2Hooks.ingestEnchant(en,book),"enchant ingest");
   ok(en.points(0)==8&&en.points(16)==16,"enchant points");
   FakeContainer container=new FakeContainer(en);container.selectedIndex=0;container.selectedLevel=4;
   ok(V2Hooks.selected(container),"queued enchant 1");container.selectedIndex=1;container.selectedLevel=5;
   ok(V2Hooks.selected(container),"queued enchant 2");
   EntityPlayer ep=new EntityPlayer();ok(V2Hooks.emitMixed(container,ep),"mixed output");
   ItemStack mixed=ep.field_71071_by.items.get(0);
   ok(mixed.tag.func_150295_c("StoredEnchantments",10).func_74745_c()==2,"one book with 2 enchants");
   ok(en.points(0)==0&&en.points(16)==0,"consumed points");
   System.out.println("PASS: potion homogenization, charge generation/consumption, multi-effect rejection, NBT, legacy migration, mixed enchant output");
 }
}''')
print('mock code emitted to',b)
