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
   int existing=0;
   for(AbstractInsnNode n:ctor.instructions.toArray())if(n instanceof MethodInsnNode){
       MethodInsnNode mi=(MethodInsnNode)n;
       if(mi.getOpcode()==INVOKESPECIAL&&mi.name.equals("<init>")
            &&(mi.owner.equals("net/minecraft/inventory/Slot")||mi.owner.endsWith("/SlotReadOnly"))){
           int[] coords=existing==0?new int[]{143,64}:new int[]{143,92};
           AbstractInsnNode y=n.getPrevious(),x=y.getPrevious();
           if(!(x instanceof IntInsnNode)||!(y instanceof IntInsnNode))throw new IllegalStateException("Slot constructor coordinates");
           ((IntInsnNode)x).operand=coords[0];((IntInsnNode)y).operand=coords[1];existing++;
       }
   }
   if(existing!=2)throw new IllegalStateException("Expected exactly 2 old library slots, got "+existing);
   // Add real filter inventory BEFORE the 36-player slot initializer.
   boolean added=false;
   for(AbstractInsnNode n:ctor.instructions.toArray())if(n instanceof MethodInsnNode){
       MethodInsnNode m=(MethodInsnNode)n;
       if(m.name.equals("addPlayerInventory")){
           InsnList f=s(
               new VarInsnNode(ALOAD,0),new TypeInsnNode(NEW,"net/minecraft/inventory/Slot"),
               new InsnNode(DUP),new TypeInsnNode(NEW,"net/minecraft/inventory/InventoryBasic"),
               new InsnNode(DUP),new LdcInsnNode("Filter"),new InsnNode(ICONST_0),new InsnNode(ICONST_1),
               new MethodInsnNode(INVOKESPECIAL,"net/minecraft/inventory/InventoryBasic","<init>","(Ljava/lang/String;ZI)V",false),
               new InsnNode(ICONST_0),new IntInsnNode(SIPUSH,143),new IntInsnNode(BIPUSH,37),
               new MethodInsnNode(INVOKESPECIAL,"net/minecraft/inventory/Slot","<init>","(Lnet/minecraft/inventory/IInventory;III)V",false),
               new MethodInsnNode(INVOKEVIRTUAL,c.name,"func_75146_a","(Lnet/minecraft/inventory/Slot;)Lnet/minecraft/inventory/Slot;",false),
               new InsnNode(POP));
           ctor.instructions.insertBefore(n,f);added=true;break;
       }
   }
   if(!added)throw new IllegalStateException("Missing player inventory initializer");
   for(AbstractInsnNode n:ctor.instructions.toArray())if(n.getOpcode()==RETURN){
       ctor.instructions.insertBefore(n,s(new VarInsnNode(ALOAD,0),
           new MethodInsnNode(INVOKESTATIC,"dev/frostey/arcanelibrary/v2/V22OutputHooks","bind","(Ljava/lang/Object;)V",false)));
   }
   MethodNode inv=method(c,"addPlayerInventory",null);
   int repl=0;for(AbstractInsnNode n:inv.instructions.toArray())if(n instanceof IntInsnNode){
       IntInsnNode i=(IntInsnNode)n;
       if(i.operand==84){i.operand=148;repl++;}
       else if(i.operand==142){i.operand=206;repl++;}
   }
   if(repl!=2)throw new IllegalStateException("Expected 27 inventory + 9 hotbar position changes");
   MethodNode transfer=method(c,"func_82846_b","("+PLAYER+"I)"+STACK);
   impl(transfer,s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ILOAD,2),
       new MethodInsnNode(INVOKESTATIC,"dev/frostey/arcanelibrary/v2/V22OutputHooks","quickMove","(Ljava/lang/Object;Ljava/lang/Object;I)Ljava/lang/Object;",false),
       new TypeInsnNode(CHECKCAST,"net/minecraft/item/ItemStack"),new InsnNode(ARETURN)));
   MethodNode click=method(c,"func_75140_a",null);
   impl(click,s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ILOAD,2),
       hook(potion?"potionButton":"enchantButton","(Ljava/lang/Object;Ljava/lang/Object;I)Z"),
       new InsnNode(IRETURN)));
   String clickDesc="(IIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;";
   MethodNode parentClick=new MethodNode(ACC_PUBLIC,"ATM0superClick",clickDesc,null,null);
   parentClick.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ILOAD,1),new VarInsnNode(ILOAD,2),
       new VarInsnNode(ILOAD,3),new VarInsnNode(ALOAD,4),
       new MethodInsnNode(INVOKESPECIAL,"net/minecraft/inventory/Container","func_75144_a",clickDesc,false),new InsnNode(ARETURN)));
   c.methods.add(parentClick);
   MethodNode intercept=new MethodNode(ACC_PUBLIC,"func_75144_a",clickDesc,null,null);
   intercept.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ILOAD,1),new VarInsnNode(ILOAD,2),
       new VarInsnNode(ILOAD,3),new VarInsnNode(ALOAD,4),
       new MethodInsnNode(INVOKESTATIC,"dev/frostey/arcanelibrary/v2/V22OutputHooks","slotClick",
           "(Ljava/lang/Object;IIILjava/lang/Object;)Ljava/lang/Object;",false),
       new TypeInsnNode(CHECKCAST,"net/minecraft/item/ItemStack"),new InsnNode(ARETURN)));
   c.methods.add(intercept);
   String closeDesc="(Lnet/minecraft/entity/player/EntityPlayer;)V";
   MethodNode close=new MethodNode(ACC_PUBLIC,"func_75134_a",closeDesc,null,null);
   close.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),
       new MethodInsnNode(INVOKESPECIAL,"net/minecraft/inventory/Container","func_75134_a",closeDesc,false),
       new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),
       new MethodInsnNode(INVOKESTATIC,"dev/frostey/arcanelibrary/v2/V22OutputHooks","onClose","(Ljava/lang/Object;Ljava/lang/Object;)V",false),
       new InsnNode(RETURN)));
   c.methods.add(close);
 }
 static void gui(ClassNode c,boolean potion){
   MethodNode ctor=method(c,"<init>",null);
   int updated=0;for(AbstractInsnNode n:ctor.instructions.toArray())if(n instanceof IntInsnNode&&((IntInsnNode)n).operand==166){
       ((IntInsnNode)n).operand=230;updated++;
   }
   if(updated!=1)throw new IllegalStateException("Expected old GUI height once");
   MethodNode clinit=method(c,"<clinit>",null);
   int found=0;for(AbstractInsnNode n:clinit.instructions.toArray())if(n instanceof LdcInsnNode){
       LdcInsnNode x=(LdcInsnNode)n;
       if("textures/gui/container/crafting_table.png".equals(x.cst)){
           x.cst="atm0arcanelibrary:textures/gui/sotnah_library.png";found++;
       }
   }
   if(found!=1)throw new IllegalStateException("Missing GUI texture ref");
   final String h="dev/frostey/arcanelibrary/v2/V22GuiHooks";
   MethodNode rb=method(c,"rebuildButtons","()V");
   impl(rb,s(new VarInsnNode(ALOAD,0),new InsnNode(potion?ICONST_1:ICONST_0),
       new MethodInsnNode(INVOKESTATIC,h,"build","(Ljava/lang/Object;Z)V",false),new InsnNode(RETURN)));
   MethodNode foreground=method(c,"func_146979_b","(II)V");
   impl(foreground,s(new VarInsnNode(ALOAD,0),new VarInsnNode(ILOAD,1),new VarInsnNode(ILOAD,2),
       new MethodInsnNode(INVOKESTATIC,h,"foreground","(Ljava/lang/Object;II)V",false),new InsnNode(RETURN)));
   MethodNode background=method(c,"func_146976_a","(FII)V");
   impl(background,s(new VarInsnNode(ALOAD,0),new VarInsnNode(FLOAD,1),new VarInsnNode(ILOAD,2),
       new VarInsnNode(ILOAD,3),new MethodInsnNode(INVOKESTATIC,h,"render","(Ljava/lang/Object;FII)V",false),new InsnNode(RETURN)));
   MethodNode act=method(c,"func_146284_a",null);
   impl(act,s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new InsnNode(potion?ICONST_1:ICONST_0),
       hook("guiClick","(Ljava/lang/Object;Ljava/lang/Object;Z)V"),new InsnNode(RETURN)));
   String mouse="(III)V",key="(CI)V";
   MethodNode superMouse=new MethodNode(ACC_PUBLIC,"ATM0superMouse",mouse,null,null);
   superMouse.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ILOAD,1),new VarInsnNode(ILOAD,2),new VarInsnNode(ILOAD,3),
       new MethodInsnNode(INVOKESPECIAL,"net/minecraft/client/gui/inventory/GuiContainer","func_73864_a",mouse,false),new InsnNode(RETURN)));
   c.methods.add(superMouse);
   MethodNode clicked=new MethodNode(ACC_PROTECTED,"func_73864_a",mouse,null,null);
   clicked.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ILOAD,1),new VarInsnNode(ILOAD,2),new VarInsnNode(ILOAD,3),
       new MethodInsnNode(INVOKESTATIC,h,"click","(Ljava/lang/Object;III)V",false),new InsnNode(RETURN)));
   c.methods.add(clicked);
   MethodNode superKey=new MethodNode(ACC_PUBLIC,"ATM0superKey",key,null,null);
   superKey.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ILOAD,1),new VarInsnNode(ILOAD,2),
       new MethodInsnNode(INVOKESPECIAL,"net/minecraft/client/gui/inventory/GuiContainer","func_73869_a",key,false),new InsnNode(RETURN)));
   c.methods.add(superKey);
   MethodNode typed=new MethodNode(ACC_PROTECTED,"func_73869_a",key,null,null);
   typed.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ILOAD,1),new VarInsnNode(ILOAD,2),
       new MethodInsnNode(INVOKESTATIC,h,"key","(Ljava/lang/Object;CI)V",false),new InsnNode(RETURN)));
   c.methods.add(typed);
   MethodNode wheel=new MethodNode(ACC_PUBLIC,"func_146274_d","()V",null,null);
   wheel.instructions.add(s(new VarInsnNode(ALOAD,0),
       new MethodInsnNode(INVOKESTATIC,h,"wheel","(Ljava/lang/Object;)V",false),
       new VarInsnNode(ALOAD,0),new MethodInsnNode(INVOKESPECIAL,"net/minecraft/client/gui/inventory/GuiContainer","func_146274_d","()V",false),
       new InsnNode(RETURN)));
   c.methods.add(wheel);
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

 static void patchSlotOutput(ClassNode c){
    final String h="dev/frostey/arcanelibrary/v2/V22OutputHooks";
    MethodNode take=method(c,"func_82869_a","(Lnet/minecraft/entity/player/EntityPlayer;)Z");
    impl(take,s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),
        new MethodInsnNode(INVOKESTATIC,h,"canTake","(Ljava/lang/Object;Ljava/lang/Object;)Z",false),new InsnNode(IRETURN)));
    MethodNode get=new MethodNode(ACC_PUBLIC,"func_75211_c","()Lnet/minecraft/item/ItemStack;",null,null);
    get.instructions.add(s(new VarInsnNode(ALOAD,0),
        new MethodInsnNode(INVOKESTATIC,h,"preview","(Ljava/lang/Object;)Ljava/lang/Object;",false),
        new TypeInsnNode(CHECKCAST,"net/minecraft/item/ItemStack"),new InsnNode(ARETURN)));c.methods.add(get);
    MethodNode decr=new MethodNode(ACC_PUBLIC,"func_75209_a","(I)Lnet/minecraft/item/ItemStack;",null,null);
    decr.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ILOAD,1),
        new MethodInsnNode(INVOKESTATIC,h,"takePart","(Ljava/lang/Object;I)Ljava/lang/Object;",false),
        new TypeInsnNode(CHECKCAST,"net/minecraft/item/ItemStack"),new InsnNode(ARETURN)));c.methods.add(decr);
    MethodNode put=new MethodNode(ACC_PUBLIC,"func_75215_d","(Lnet/minecraft/item/ItemStack;)V",null,null);
    put.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),
        new MethodInsnNode(INVOKESTATIC,h,"ignoreSet","(Ljava/lang/Object;Ljava/lang/Object;)V",false),new InsnNode(RETURN)));c.methods.add(put);
    MethodNode pickup=new MethodNode(ACC_PUBLIC,"func_82870_a","(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)V",null,null);
    pickup.instructions.add(s(new VarInsnNode(ALOAD,0),new VarInsnNode(ALOAD,1),new VarInsnNode(ALOAD,2),
        new MethodInsnNode(INVOKESTATIC,h,"onPickup","(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V",false),
        new InsnNode(RETURN)));c.methods.add(pickup);
 }
 static byte[] transform(byte[] input,String name){ClassReader cr=new ClassReader(input);ClassNode c=new ClassNode();cr.accept(c,0);
   if(name.endsWith("container/SlotReadOnly.class"))patchSlotOutput(c);
   else if(name.endsWith("block/BlockPotionLibrary.class")||name.endsWith("block/BlockEnchantmentLibrary.class"))block(c);
   else if(name.endsWith("tile/TilePotionLibrary.class"))tilePotion(c);
   else if(name.endsWith("tile/TileLibraryBase.class"))tileBase(c);
   else if(name.endsWith("container/ContainerPotionLibrary.class"))containers(c,true);
   else if(name.endsWith("container/ContainerEnchantmentLibrary.class"))containers(c,false);
   else if(name.endsWith("client/GuiEnchantmentLibrary.class"))gui(c,false);
   else if(name.endsWith("client/GuiPotionLibrary.class"))gui(c,true);
   else if(name.endsWith("ATM0ArcaneLibrary.class")){
       for(FieldNode f:c.fields)if(f.name.equals("VERSION"))f.value="0.2.2";
       if(c.visibleAnnotations!=null)for(AnnotationNode a:c.visibleAnnotations)if(a.values!=null)for(int i=0;i+1<a.values.size();i+=2)if("version".equals(a.values.get(i)))a.values.set(i+1,"0.2.2");
   }
   // IMPORTANT: changing BIPUSH operand 84 to 148 or 26 to 143 must also
   // widen the instruction. Otherwise 148 becomes -108, 143 becomes -113:
   // the exact reason only the hotbar was clickable in 0.2.1.
   for(MethodNode method:c.methods)for(AbstractInsnNode node:method.instructions.toArray()){
       if(node instanceof IntInsnNode){
           IntInsnNode integer=(IntInsnNode)node;
           if(integer.getOpcode()==BIPUSH && (integer.operand>127 || integer.operand<-128)){
               integer.setOpcode(SIPUSH);
           }
       }
   }
   ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(cw);return cw.toByteArray();
 }
 public static void main(String[] args)throws Exception{
  try(ZipFile zip=new ZipFile(args[0]);ZipOutputStream out=new ZipOutputStream(new FileOutputStream(args[1]))){
   Enumeration<? extends ZipEntry> it=zip.entries();int touched=0;
   while(it.hasMoreElements()){
    ZipEntry e=it.nextElement();String name=e.getName();
    byte[] bytes;try(InputStream is=zip.getInputStream(e)){bytes=is.readAllBytes();}
    if(name.endsWith(".class")&&(name.endsWith("TilePotionLibrary.class")||name.endsWith("TileLibraryBase.class")||name.endsWith("BlockPotionLibrary.class")||name.endsWith("BlockEnchantmentLibrary.class")||name.endsWith("ContainerPotionLibrary.class")||name.endsWith("ContainerEnchantmentLibrary.class")||name.endsWith("GuiPotionLibrary.class")||name.endsWith("GuiEnchantmentLibrary.class")||name.endsWith("ATM0ArcaneLibrary.class")||name.endsWith("SlotReadOnly.class"))){
      bytes=transform(bytes,name);System.out.println("patched "+name);touched++;
    }
    if(name.equals("mcmod.info")){String x=new String(bytes,"UTF-8");x=x.replace("0.1.2-exp3-listui","0.2.2");bytes=x.getBytes("UTF-8");}
    out.putNextEntry(new ZipEntry(name));out.write(bytes);out.closeEntry();
   }
   if(touched!=10)throw new IOException("Patched "+touched+" of 10 classes");
   Path cls=Paths.get(args[2]);Files.walk(cls).filter(Files::isRegularFile).forEach(p->{try{
      String k=cls.relativize(p).toString().replace(File.separatorChar,'/');if(!k.startsWith("dev/"))return;out.putNextEntry(new ZipEntry(k));out.write(Files.readAllBytes(p));out.closeEntry();
   }catch(IOException e){throw new UncheckedIOException(e);}});
   Path assets=Paths.get(args[3]);Files.walk(assets).filter(Files::isRegularFile).forEach(p->{try{
      String k=assets.relativize(p).toString().replace(File.separatorChar,'/');out.putNextEntry(new ZipEntry(k));out.write(Files.readAllBytes(p));out.closeEntry();
   }catch(IOException e){throw new UncheckedIOException(e);}});
  }
 }
}
