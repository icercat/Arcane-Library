package net.minecraft.entity.player;
import net.minecraft.item.ItemStack;import java.util.*;
public class InventoryPlayer{public ArrayList<ItemStack> items=new ArrayList<>();public boolean allow=true;
 public boolean func_70441_a(ItemStack s){if(!allow)return false;items.add(s);return true;}}
