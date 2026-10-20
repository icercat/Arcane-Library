import dev.frostey.arcanelibrary.v2.*;
import java.util.*;
import net.minecraft.nbt.*;
import dev.frostey.arcanelibrary.tile.TileEnchantmentLibrary;

public class TestHover {
  public static class Menu { public Object tile; Menu(Object t){tile=t;} }
  public static class FakeEnchantmentGui {
    public int field_147003_i=60,field_147009_r=40,pageStart=0,localSelected=0;
    public Menu field_147002_h;
    public List<String> last;
    FakeEnchantmentGui(Object t){field_147002_h=new Menu(t);}
    private String entryLabel(int n){return "Exceptionally Long Named Enchantment – Protection";}
    protected void func_146283_a(List<String> a,int x,int y){last=a;}
  }
  public static class FakePotionGui {
    public int field_147003_i=60,field_147009_r=40,pageStart=0,localSelected=0;
    public Menu field_147002_h;
    public List<String> last;
    FakePotionGui(Object t){field_147002_h=new Menu(t);}
    private String entryLabel(int n){return "Potion of Spectacularly Long Lasting Swift Movement";}
    protected void func_146283_a(List<String> a,int x,int y){last=a;}
  }
  private static void ok(boolean v,String msg){if(!v)throw new AssertionError(msg);}
  public static void main(String[]args){
    TileEnchantmentLibrary tile=new TileEnchantmentLibrary();
    TileEnchantmentLibrary.Record r=new TileEnchantmentLibrary.Record();r.maxLevel=14;r.points=123456789L;
    tile.records.put(0,r);
    FakeEnchantmentGui gui=new FakeEnchantmentGui(tile);
    V22GuiHooks.tooltip(gui,90,75);
    ok(gui.last!=null&&gui.last.size()==3,"enchant hover tooltip row");
    ok(gui.last.get(0).contains("Exceptionally Long Named"),"hover displays untruncated name");
    ok(gui.last.get(1).contains("14"),"highest learned level");
    ok(gui.last.get(2).contains("123456789"),"exact long points");
    gui.last=null;V22GuiHooks.tooltip(gui,90,300);
    ok(gui.last==null,"tooltip must not appear outside list");
    net.minecraft.nbt.NBTTagCompound n=new net.minecraft.nbt.NBTTagCompound();
    NBTTagList ls=new NBTTagList();NBTTagCompound entry=new NBTTagCompound();entry.func_74768_a("key",1);entry.func_74772_a("count",57L);ls.func_74742_a(entry);
    n.func_74768_a("ATM0V2Data",1);n.func_74782_a("ATM0V2Essences",ls);
    NBTTagCompound charges=new NBTTagCompound();charges.func_74772_a("glow",6);charges.func_74772_a("red",8);charges.func_74772_a("gun",10);
    n.func_74782_a("ATM0V2Charges",charges);
    dev.frostey.arcanelibrary.tile.TilePotionLibrary pot=new dev.frostey.arcanelibrary.tile.TilePotionLibrary();
    V2Hooks.readPotions(pot,n);
    FakePotionGui pg=new FakePotionGui(pot);V22GuiHooks.tooltip(pg,90,75);
    ok(pg.last!=null&&pg.last.size()==5,"potion hover tooltip lines");
    ok(pg.last.get(0).contains("Spectacularly"),"full potion name");
    ok(pg.last.get(1).contains("57"),"potion stored amount");
    ok(pg.last.get(4).contains("G:6")&&pg.last.get(4).contains("P:10"),"potion charges");
    System.out.println("PASS: enchant/potion full-name hover, learned levels, long points, essence, charges and hover boundaries");
  }
}
