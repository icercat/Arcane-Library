package dev.frostey.arcanelibrary.tile;
public class TileEnchantmentLibrary{
 public int changes;
 public java.util.Map<Integer,Record> records=new java.util.TreeMap<Integer,Record>();
 protected void writeStorage(net.minecraft.nbt.NBTTagCompound tag){
   net.minecraft.nbt.NBTTagList ls=new net.minecraft.nbt.NBTTagList();
   for(java.util.Map.Entry<Integer,Record> r:records.entrySet()){
      net.minecraft.nbt.NBTTagCompound n=new net.minecraft.nbt.NBTTagCompound();
      n.func_74768_a("id",r.getKey());n.func_74772_a("points",r.getValue().points);n.func_74768_a("max",r.getValue().maxLevel);ls.func_74742_a(n);
   } tag.func_74782_a("Enchantments",ls);
 }
 protected void readStorage(net.minecraft.nbt.NBTTagCompound tag){
   records.clear();net.minecraft.nbt.NBTTagList ls=tag.func_150295_c("Enchantments",10);
   for(int i=0;i<ls.func_74745_c();i++){
     net.minecraft.nbt.NBTTagCompound n=ls.func_150305_b(i);
     Record rec=new Record();rec.points=n.func_74763_f("points");rec.maxLevel=n.func_74762_e("max");
     records.put(n.func_74762_e("id"),rec);
   }
 }
 protected void storageChanged(){changes++;}
 public static class Record{public long points;public int maxLevel;}
}