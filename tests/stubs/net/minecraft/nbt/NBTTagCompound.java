package net.minecraft.nbt;import java.util.*;
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
}