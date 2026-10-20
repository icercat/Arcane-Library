import dev.frostey.arcanelibrary.v2.V2Hooks;
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
}