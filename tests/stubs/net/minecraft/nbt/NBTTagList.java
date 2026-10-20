package net.minecraft.nbt;import java.util.*;
public class NBTTagList extends NBTBase{
 public java.util.List<NBTBase> contents=new ArrayList<>();
 public void func_74742_a(NBTBase b){contents.add(b);}
 public int func_74745_c(){return contents.size();}
 public NBTTagCompound func_150305_b(int n){return (NBTTagCompound)contents.get(n);}
}