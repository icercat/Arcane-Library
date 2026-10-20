package net.minecraft.item;import net.minecraft.nbt.*;
public class ItemStack{
 public int field_77994_a;public int damage;public Item item; public NBTTagCompound tag;
 public ItemStack(Item i,int size,int d){item=i;damage=d;field_77994_a=size;}
 public Item func_77973_b(){return item;}
 public int func_77960_j(){return damage;}
 public NBTTagCompound func_77978_p(){return tag;}
 public void func_77982_d(NBTTagCompound n){tag=n;}
 public ItemStack func_77946_l(){ItemStack s=new ItemStack(item,field_77994_a,damage);s.tag=tag;return s;}
}