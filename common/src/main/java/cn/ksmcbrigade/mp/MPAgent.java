package cn.ksmcbrigade.mp;

import cn.ksmcbrigade.mp.transformers.MPTransformer;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.util.asm.ASM;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MPAgent {

    public static final Logger LOGGER = LoggerFactory.getLogger(MPAgent.class.getSimpleName());

    public static Map<String,byte[]> TRANSFORMATIONS = new HashMap<>();

    public static Instrumentation INST;

    public static void premain(String arg, Instrumentation instrumentation){
        INST = instrumentation;
        LOGGER.info("MP Agent Loading...");
        instrumentation.addTransformer(new MPTransformer(),true);
        File configsDir = new File("mp-classes");
        while(!configsDir.exists()) configsDir.mkdirs();
        initConfig(configsDir);

        for (String s : TRANSFORMATIONS.keySet()) {
            String name = s.replace("/",".");
            Arrays.stream(instrumentation.getAllLoadedClasses()).filter((c)->c.getName().equals(name)).forEach((c)->{
                try {
                    instrumentation.retransformClasses(c);
                } catch (Throwable e) {
                    LOGGER.error("Cannot retransform class {}",c,e);
                }
            });
        }

        LOGGER.info("MP Agent Loaded.");
    }

    public static void initConfig(File dir){
        File[] files = dir.listFiles();
        if(files!=null){
            for (File file : files) {
                try {
                    if(file.isFile() && file.getName().endsWith(".class")){
                        byte[] bytes = FileUtils.readFileToByteArray(file);
                        ClassNode node = new ClassNode();
                        ClassReader reader = new ClassReader(bytes);
                        reader.accept(node, ASM.API_VERSION);
                        TRANSFORMATIONS.put(node.name,bytes);
                    }
                    else if(file.isDirectory()){
                        initConfig(file);
                    }
                } catch (Throwable e) {
                   if(e instanceof IOException ioException){
                       LOGGER.error("Can't read the file: {}",file,ioException);
                   }
                   else{
                       LOGGER.error("Failed to init config file: {}",file,e);
                   }
                }
            }
        }
    }

    public static void agentmain(String arg,Instrumentation instrumentation){
        premain(arg,instrumentation);
    }
}
