import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;

public class PatchJar implements Opcodes {
 static final String HOOK="dev/frostey/arcanelibrary/v2/V2Hooks";
 static final String STACK="Lnet/minecraft/item/ItemStack;";
 static final String PLAYER="Lnet/minecraft/entity/player/EntityPlayer;";
 static InsnList s(Object... ops){InsnList l=new InsnList();for(Object o:ops)l.add((AbstractInsnNode)o);return l;}
 static MethodNode method(ClassNode c,String name,String desc){for(MethodNode m:c.methods)if(m.name.equals(name)&&(desc==null||m.desc.equals(desc)))return m; throw new IllegalStateException(c.name+" missing "+name+desc);}
 static void impl(MethodNode m,InsnList i){m.instructions.clear();m.instructions.add(i);m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.maxLocals=0;m.maxStack=0;}
 static MethodInsnNode hook(String name,String desc){return new MethodInsnNode(INVOKESTATIC,HOOK,name,desc,false);}
 static void proxy(ClassNode c,String name,String desc,String hookName,String hookDesc,int[] loads,int ret,String checkcast){
   MethodNode m=method(c,name,desc);InsnList i=new InsnList();
   for(int op:loads){i.add(new VarInsnNode(op>>>16,op&0xffff));}
   i.add(hook(hookName,hookDesc));if(checkcast!=null)i.add(new TypeInsnNode(CHECKCAST,checkcast));i.add(new InsnNode(ret));impl(m,i);
 }
 static void beforeReturns(MethodNode m,String hookName,boolean tile){
    for(AbstractInsnNode ins:m.instructions.toArray())if(ins.getOpcode()==RETURN){
      InsnList i=s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),hook(hookName,"(Ljava/lang/Object;Ljava/lang/Object;)V"));m.instructions.insertBefore(ins,i);
    }
 }
 static void tilePotion(ClassNode c){
    proxy(c,"accepts","("+STACK+")Z","validPotion","(Ljava/lang/Object;)Z",new int[]{(ALOAD<<16)|1},IRETURN,null);
    proxy(c,"func_145845_h","()V","tickPotion","(Ljava/lang/Object;)V",new int[]{(ALOAD<<16)|0},RETURN,null);
    proxy(c,"entryCount","()I","potionCount","(Ljava/lang/Object;)I",new int[]{(ALOAD<<16)|0},IRETURN,null);
    proxy(c,"preview","(I)"+STACK,"potionPreview","(Ljava/lang/Object;I)Ljava/lang/Object;",new int[]{(ALOAD<<16)|0,(ILOAD<<16)|1},ARETURN,"net/minecraft/item/ItemStack");
    MethodNode ex=method(c,"extract","("+PLAYER+"I)Z");impl(ex,s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ILOAD,2),new InsnNode(ICONST_0),hook("potionExtract","(Ljava/lang/Object;Ljava/lang/Object;II)Z"),new InsnNode(IRETURN)));
    beforeReturns(method(c,"readStorage",null),"readPotions",true);
    beforeReturns(method(c,"writeStorage",null),"writePotions",true);
 }
 static void tileBase(ClassNode c){
   MethodNode changed=method(c,"storageChanged","()V");
   // Mark the world for an NBT tile update.  Do not send from the client.
   InsnList appended=new InsnList();
   appended.add(new VarInsnNode(ALOAD,0));
   appended.add(hook("notifyTile","(Ljava/lang/Object;)V"));
   for(AbstractInsnNode ins:changed.instructions.toArray())if(ins.getOpcode()==RETURN)changed.instructions.insertBefore(ins,appended);
   // Minecraft 1.7.10 TileEntity#getDescriptionPacket (SRG func_145844_m).
   MethodNode get=new MethodNode(ACC_PUBLIC,"func_145844_m","()Lnet/minecraft/network/Packet;",null,null);
   get.instructions.add(s(new VarInsnNode(ALOAD,0),hook("describeTile","(Ljava/lang/Object;)Ljava/lang/Object;"),new TypeInsnNode(CHECKCAST,"net/minecraft/network/Packet"),new InsnNode(ARETURN)));
   c.methods.add(get);
   MethodNode on=new MethodNode(ACC_PUBLIC,"onDataPacket","(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/network/play/server/S35PacketUpdateTileEntity;)V",null,null);
   on.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,2),hook("receiveTile","(Ljava/lang/Object;Ljava/lang/Object;)V"),new InsnNode(RETURN)));
   c.methods.add(on);
 }
 static void containers(ClassNode c,boolean potion){
   MethodNode ctor=method(c,"<init>",null);
   // Match specific literals in each Slot constructor only.
   int index=0;
   for(AbstractInsnNode n:ctor.instructions.toArray())if(n instanceof MethodInsnNode){
       MethodInsnNode mi=(MethodInsnNode)n;
       if(mi.getOpcode()==INVOKESPECIAL && mi.name.equals("<init>") && (mi.owner.equals("net/minecraft/inventory/Slot")||mi.owner.endsWith("/SlotReadOnly"))){
           int[] coords=index==0?new int[]{143,64}:new int[]{143,92};
           AbstractInsnNode y=n.getPrevious(),x=y.getPrevious();
           if(x instanceof IntInsnNode)((IntInsnNode)x).operand=coords[0]; else throw new IllegalStateException("slot x "+x);
           if(y instanceof IntInsnNode)((IntInsnNode)y).operand=coords[1]; else throw new IllegalStateException("slot y "+y);
           index++;
       }
   }
   if(index!=2)throw new IllegalStateException("only "+index+" slot changes");
   MethodNode inv=method(c,"addPlayerInventory",null);
   int repl=0;for(AbstractInsnNode n:inv.instructions.toArray())if(n instanceof IntInsnNode){IntInsnNode v=(IntInsnNode)n;
     if(v.operand==84){v.operand=148;repl++;}else if(v.operand==142){v.operand=206;repl++;}}
   if(repl!=2)throw new IllegalStateException("player inventory positions "+repl);
   String name="func_82846_b",desc="("+PLAYER+"I)"+STACK;
   proxy(c,name,desc,"quickInsert","(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)Ljava/lang/Object;",new int[]{(ALOAD<<16)|0},ARETURN,null); // reset below, include tile
   MethodNode transfer=method(c,name,desc);
   transfer.instructions.clear();transfer.instructions.add(s(
     new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,0),
     new FieldInsnNode(GETFIELD,c.name,"tile","Ldev/frostey/arcanelibrary/tile/Tile"+(potion?"Potion":"Enchantment")+"Library;"),
     new VarInsnNode(ALOAD,1),new VarInsnNode(ILOAD,2),hook("quickInsert","(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)Ljava/lang/Object;"),
     new TypeInsnNode(CHECKCAST,"net/minecraft/item/ItemStack"),new InsnNode(ARETURN)));
   MethodNode click=method(c,"func_75140_a",null);
   if(potion){
       impl(click,s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ILOAD,2),hook("potionButton","(Ljava/lang/Object;Ljava/lang/Object;I)Z"),new InsnNode(IRETURN)));
   }else{
       impl(click,s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ILOAD,2),hook("enchantButton","(Ljava/lang/Object;Ljava/lang/Object;I)Z"),new InsnNode(IRETURN)));
   }
 }
 static void gui(ClassNode c,boolean potion){
   MethodNode ctor=method(c,"<init>",null);
   int h=0;for(AbstractInsnNode n:ctor.instructions.toArray())if(n instanceof IntInsnNode&&((IntInsnNode)n).operand==166){((IntInsnNode)n).operand=230;h++;}
   if(h!=1)throw new IllegalStateException("gui height "+h);
   // draw background from mod asset, rather than vanilla crafting table.
   MethodNode clinit=method(c,"<clinit>",null);
   int bg=0;for(AbstractInsnNode n:clinit.instructions.toArray())if(n instanceof LdcInsnNode){LdcInsnNode x=(LdcInsnNode)n;if("textures/gui/container/crafting_table.png".equals(x.cst)){x.cst="atm0arcanelibrary:textures/gui/library_v2.png";bg++;}}
   if(bg!=1)throw new IllegalStateException("background path changes "+bg);
   String container="Ldev/frostey/arcanelibrary/container/Container"+(potion?"Potion":"Enchantment")+"Library;";
   proxy(c,"rebuildButtons","()V","buildGui","(Ljava/lang/Object;Z)V",new int[]{(ALOAD<<16)|0},RETURN,null);
   MethodNode rb=method(c,"rebuildButtons","()V");rb.instructions.clear();rb.instructions.add(s(new VarInsnNode(ALOAD,0),new InsnNode(potion?ICONST_1:ICONST_0),hook("buildGui","(Ljava/lang/Object;Z)V"),new InsnNode(RETURN)));
   MethodNode act=method(c,"func_146284_a",null);
   impl(act,s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new InsnNode(potion?ICONST_1:ICONST_0),hook("guiClick","(Ljava/lang/Object;Ljava/lang/Object;Z)V"),new InsnNode(RETURN)));
   MethodNode fore=method(c,"func_146979_b",null);
   impl(fore,s(new VarInsnNode(ALOAD,0),new InsnNode(potion?ICONST_1:ICONST_0),hook("drawGui","(Ljava/lang/Object;Z)V"),new InsnNode(RETURN)));
   // For dynamically changing enchant lists, rebuild controls when counts change.
   MethodNode tick=new MethodNode(ACC_PUBLIC,"func_73876_c","()V",null,null);
   tick.instructions.add(s(new VarInsnNode(ALOAD,0),new MethodInsnNode(INVOKESPECIAL,"net/minecraft/client/gui/inventory/GuiContainer","func_73876_c","()V",false),new VarInsnNode(ALOAD,0),hook("refreshGui","(Ljava/lang/Object;)V"),new InsnNode(RETURN)));
   c.methods.add(tick);
 }

 /** 1.7.10 Forge drop/placement overrides for *both* library blocks. */
 static void block(ClassNode c){
   final String hooks="dev/frostey/arcanelibrary/v2/V21BlockHooks";
   final String world="Lnet/minecraft/world/World;";
   final String player="Lnet/minecraft/entity/player/EntityPlayer;";
   final String living="Lnet/minecraft/entity/EntityLivingBase;";
   // Forge-specific method is intentionally unmapped in SRG runtime.
   MethodNode remove=new MethodNode(ACC_PUBLIC,"removedByPlayer","("+world+player+"IIIZ)Z",null,null);
   remove.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ALOAD,2),new VarInsnNode(ILOAD,3),new VarInsnNode(ILOAD,4),new VarInsnNode(ILOAD,5),new VarInsnNode(ILOAD,6),new MethodInsnNode(INVOKESTATIC,hooks,"removedByPlayer","(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;IIIZ)Z",false),new InsnNode(IRETURN)));
   c.methods.add(remove);
   MethodNode drops=new MethodNode(ACC_PUBLIC,"getDrops","("+world+"IIIII)Ljava/util/ArrayList;",null,null);
   drops.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ILOAD,2),new VarInsnNode(ILOAD,3),new VarInsnNode(ILOAD,4),new VarInsnNode(ILOAD,5),new VarInsnNode(ILOAD,6),new MethodInsnNode(INVOKESTATIC,hooks,"getDrops","(Ljava/lang/Object;Ljava/lang/Object;IIIII)Ljava/util/ArrayList;",false),new InsnNode(ARETURN)));
   c.methods.add(drops);
   MethodNode harvest=new MethodNode(ACC_PUBLIC,"func_149636_a","("+world+player+"IIII)V",null,null);
   harvest.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ALOAD,2),new VarInsnNode(ILOAD,3),new VarInsnNode(ILOAD,4),new VarInsnNode(ILOAD,5),new VarInsnNode(ILOAD,6),new MethodInsnNode(INVOKESPECIAL,"net/minecraft/block/BlockContainer","func_149636_a","("+world+player+"IIII)V",false),new VarInsnNode(ALOAD,1),new VarInsnNode(ILOAD,3),new VarInsnNode(ILOAD,4),new VarInsnNode(ILOAD,5),new MethodInsnNode(INVOKESTATIC,hooks,"finishHarvest","(Ljava/lang/Object;III)V",false),new InsnNode(RETURN)));
   c.methods.add(harvest);
   MethodNode placed=new MethodNode(ACC_PUBLIC,"func_149689_a","("+world+"III"+living+STACK+")V",null,null);
   placed.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ILOAD,2),new VarInsnNode(ILOAD,3),new VarInsnNode(ILOAD,4),new VarInsnNode(ALOAD,6),new MethodInsnNode(INVOKESTATIC,hooks,"onPlaced","(Ljava/lang/Object;Ljava/lang/Object;IIILjava/lang/Object;)V",false),new InsnNode(RETURN)));
   c.methods.add(placed);
   // Silk Touch must go through the same NBT-aware getDrops code path.
   MethodNode silk=new MethodNode(ACC_PUBLIC,"canSilkHarvest","("+world+player+"IIII)Z",null,null);
   silk.instructions.add(s(new InsnNode(ICONST_0),new InsnNode(IRETURN)));
   c.methods.add(silk);
 }
 static byte[] transform(byte[] input,String name){ClassReader cr=new ClassReader(input);ClassNode c=new ClassNode();cr.accept(c,0);
   if(name.endsWith("block/BlockPotionLibrary.class")||name.endsWith("block/BlockEnchantmentLibrary.class"))block(c);
   else if(name.endsWith("tile/TilePotionLibrary.class"))tilePotion(c);
   else if(name.endsWith("tile/TileLibraryBase.class"))tileBase(c);
   else if(name.endsWith("container/ContainerPotionLibrary.class"))containers(c,true);
   else if(name.endsWith("container/ContainerEnchantmentLibrary.class"))containers(c,false);
   else if(name.endsWith("client/GuiEnchantmentLibrary.class"))gui(c,false);
   else if(name.endsWith("client/GuiPotionLibrary.class"))gui(c,true);
   else if(name.endsWith("ATM0ArcaneLibrary.class")){
       for(FieldNode f:c.fields)if(f.name.equals("VERSION"))f.value="0.2.1";
       if(c.visibleAnnotations!=null)for(AnnotationNode a:c.visibleAnnotations)if(a.values!=null)for(int i=0;i+1<a.values.size();i+=2)if("version".equals(a.values.get(i)))a.values.set(i+1,"0.2.1");
   }
   ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(cw);return cw.toByteArray();
 }
 public static void main(String[] args)throws Exception{
  try(ZipFile zip=new ZipFile(args[0]);ZipOutputStream out=new ZipOutputStream(new FileOutputStream(args[1]))){
   Enumeration<? extends ZipEntry> it=zip.entries();int touched=0;
   while(it.hasMoreElements()){
    ZipEntry e=it.nextElement();String name=e.getName();
    byte[] bytes;try(InputStream is=zip.getInputStream(e)){bytes=is.readAllBytes();}
    if(name.endsWith(".class")&&(name.endsWith("TilePotionLibrary.class")||name.endsWith("TileLibraryBase.class")||name.endsWith("BlockPotionLibrary.class")||name.endsWith("BlockEnchantmentLibrary.class")||name.endsWith("ContainerPotionLibrary.class")||name.endsWith("ContainerEnchantmentLibrary.class")||name.endsWith("GuiPotionLibrary.class")||name.endsWith("GuiEnchantmentLibrary.class")||name.endsWith("ATM0ArcaneLibrary.class"))){
      bytes=transform(bytes,name);System.out.println("patched "+name);touched++;
    }
    if(name.equals("mcmod.info")){String x=new String(bytes,"UTF-8");x=x.replace("0.1.2-exp3-listui","0.2.1");bytes=x.getBytes("UTF-8");}
    out.putNextEntry(new ZipEntry(name));out.write(bytes);out.closeEntry();
   }
   if(touched!=9)throw new IOException("Patched "+touched+" of 9 classes");
   Path cls=Paths.get(args[2]);Files.walk(cls).filter(Files::isRegularFile).forEach(p->{try{
      String k=cls.relativize(p).toString().replace(File.separatorChar,'/');if(!k.startsWith("dev/"))return;out.putNextEntry(new ZipEntry(k));out.write(Files.readAllBytes(p));out.closeEntry();
   }catch(IOException e){throw new UncheckedIOException(e);}});
   Path assets=Paths.get(args[3]);Files.walk(assets).filter(Files::isRegularFile).forEach(p->{try{
      String k=assets.relativize(p).toString().replace(File.separatorChar,'/');out.putNextEntry(new ZipEntry(k));out.write(Files.readAllBytes(p));out.closeEntry();
   }catch(IOException e){throw new UncheckedIOException(e);}});
  }
 }
}
