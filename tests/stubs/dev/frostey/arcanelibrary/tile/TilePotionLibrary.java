package dev.frostey.arcanelibrary.tile;
import net.minecraft.nbt.NBTTagCompound;
public class TilePotionLibrary {
 public int changes;
 protected void writeStorage(NBTTagCompound tag){dev.frostey.arcanelibrary.v2.V2Hooks.writePotions(this,tag);}
 protected void readStorage(NBTTagCompound tag){dev.frostey.arcanelibrary.v2.V2Hooks.readPotions(this,tag);}
 protected void storageChanged(){changes++;}
}
