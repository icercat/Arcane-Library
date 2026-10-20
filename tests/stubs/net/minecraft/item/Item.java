package net.minecraft.item;import java.util.*;
public class Item {
  static Map<Integer,Item> ids=new HashMap<>();
  public Item(){ids.put(373,this);}
  public static Item func_150899_d(int id){return ids.get(id);}
  private static final java.util.IdentityHashMap<net.minecraft.block.Block,Item> BLOCKS=new java.util.IdentityHashMap<>();
  public static Item func_150898_a(net.minecraft.block.Block block){Item it=BLOCKS.get(block);if(it==null){it=new Item();BLOCKS.put(block,it);}return it;}
}