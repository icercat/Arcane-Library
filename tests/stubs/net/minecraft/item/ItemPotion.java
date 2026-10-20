package net.minecraft.item;import java.util.*;import net.minecraft.potion.PotionEffect;
public class ItemPotion extends Item{
 public List<PotionEffect> func_77832_l(ItemStack s){
  if(s.tag!=null&&s.tag.data.containsKey("CustomPotionEffects"))return Arrays.asList(new PotionEffect(1,0),new PotionEffect(5,0));
  return Arrays.asList(new PotionEffect(s.damage&15,(s.damage&0x20)!=0?1:0));
 }
}