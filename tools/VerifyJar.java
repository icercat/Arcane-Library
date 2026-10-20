import java.util.*;
import java.util.zip.*;
import java.io.*;
import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;
import jdk.internal.org.objectweb.asm.tree.analysis.*;

public class VerifyJar {
 public static void main(String[] args)throws Exception{
  int mcount=0,fail=0;try(ZipFile z=new ZipFile(args[0])){
   Enumeration<? extends ZipEntry> e=z.entries();
   while(e.hasMoreElements()){
    ZipEntry x=e.nextElement();if(!x.getName().endsWith(".class"))continue;
    byte[] data=z.getInputStream(x).readAllBytes();ClassNode cn=new ClassNode();new ClassReader(data).accept(cn,ClassReader.EXPAND_FRAMES);
    if(!x.getName().startsWith("dev/frostey/arcanelibrary/"))continue;
    for(MethodNode mn:cn.methods){if(mn.instructions==null||mn.instructions.size()==0)continue;mcount++;
     try {new Analyzer<BasicValue>(new BasicVerifier()).analyze(cn.name,mn);}catch(Throwable t){
      System.out.println("FAIL "+cn.name+"."+mn.name+mn.desc+" "+t);fail++;}}
   }
  }System.out.println("analyzed "+mcount+" methods; failures="+fail);
  if(fail>0)System.exit(1);
 }
}
