package net.minecraft.item;import java.util.*;
public class Item {
  static Map<Integer,Item> ids=new HashMap<>();
  public Item(){ids.put(373,this);}
  public static Item func_150899_d(int id){return ids.get(id);}
}