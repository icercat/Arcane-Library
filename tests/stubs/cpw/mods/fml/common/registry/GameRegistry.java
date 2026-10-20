package cpw.mods.fml.common.registry;
import net.minecraft.item.*;
public class GameRegistry{
 public static ItemPotion POTION=new ItemPotion();
 public static Item findItem(String m,String n){return n.equals("potion")?POTION:n.equals("enchanted_book")?new Item():null;}
}