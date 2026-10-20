package net.minecraft.nbt;import java.util.*;
public class NBTTagCompound extends NBTBase{
 public HashMap<String,Object> data=new HashMap<>();
 public void func_74772_a(String s,long n){data.put(s,n);}
 public void func_74768_a(String s,int n){data.put(s,n);}
 public void func_74777_a(String s,short n){data.put(s,n);}
 public void func_74782_a(String s,NBTBase n){data.put(s,n);}
 public void func_74778_a(String s,String n){data.put(s,n);}
 public String func_74779_i(String s){Object v=data.get(s);return v instanceof String?(String)v:"";}
 public boolean func_150297_b(String s,int n){Object v=data.get(s);return n==10?v instanceof NBTTagCompound:n==9?v instanceof NBTTagList:false;}
 public int func_74762_e(String s){return ((Number)data.getOrDefault(s,0)).intValue();}
 public long func_74763_f(String s){return ((Number)data.getOrDefault(s,0)).longValue();}
 public short func_74765_d(String s){return ((Number)data.getOrDefault(s,0)).shortValue();}
 public boolean func_74764_b(String s){return data.containsKey(s);}
 public NBTTagList func_150295_c(String s,int type){return data.containsKey(s)?(NBTTagList)data.get(s):new NBTTagList();}
 public NBTTagCompound func_74775_l(String s){return data.containsKey(s)?(NBTTagCompound)data.get(s):new NBTTagCompound();}
 public NBTBase func_74737_b(){NBTTagCompound t=new NBTTagCompound();for(Map.Entry<String,Object> e:data.entrySet()){Object v=e.getValue();t.data.put(e.getKey(),v instanceof NBTTagCompound?((NBTTagCompound)v).func_74737_b():v);}return t;}
}