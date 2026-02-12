package cn.ksmcbrigade.mp;

import cn.ksmcbrigade.mp.platform.Services;
import cn.ksmcbrigade.mp.transformers.MPTransformer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.util.asm.ASM;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLConnection;
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
                    if(file.isFile() && file.getName().toLowerCase().endsWith(".class")){
                        byte[] bytes = FileUtils.readFileToByteArray(file);
                        ClassNode node = new ClassNode();
                        ClassReader reader = new ClassReader(bytes);
                        reader.accept(node, ASM.API_VERSION);
                        TRANSFORMATIONS.put(node.name,bytes);
                    }
                    else if(file.isFile() && file.getName().toLowerCase().endsWith(".json")){
                        for (JsonElement jsonElement : JsonParser.parseString(FileUtils.readFileToString(file)).getAsJsonArray()) {
                            try {
                                byte[] bytes = getFromURL(jsonElement.getAsString());
                                ClassNode node = new ClassNode();
                                ClassReader reader = new ClassReader(bytes);
                                reader.accept(node, ASM.API_VERSION);
                                TRANSFORMATIONS.put(node.name,bytes);
                                LOGGER.info("Loaded {} from {}",node.name,jsonElement);
                            } catch (IOException e) {
                                LOGGER.error("Failed to load {} from {}",jsonElement,file,e);
                            }
                        }
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

    public static byte[] getFromURL(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        InputStream inputStream = urlConnection.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int read = 0;

        byte[] buffer = new byte[1024];
        while((read = inputStream.read(buffer,0,1024))!=-1){
            outputStream.write(buffer,0,read);
        }

        return outputStream.toByteArray();
    }
}
