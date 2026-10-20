package net.minecraft.world;
public class World {
  public boolean field_72995_K;
  public int removes;
  private Object tile;
  public World(boolean client,Object tile){field_72995_K=client;this.tile=tile;}
  public Object func_147438_o(int x,int y,int z){return tile;}
  public boolean func_147468_f(int x,int y,int z){removes++;tile=null;return true;}
  public void place(Object tile){this.tile=tile;}
}
