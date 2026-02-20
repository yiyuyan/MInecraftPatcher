package cn.ksmcbrigade.mp;

import cn.ksmcbrigade.mp.transformers.MPTransformer;
import cn.ksmcbrigade.mp.utils.CompileUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
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
import java.util.*;

public class MPAgent {

    public static final Logger LOGGER = LoggerFactory.getLogger(MPAgent.class.getSimpleName());

    public static Map<String,byte[]> TRANSFORMATIONS = new HashMap<>();
    public static Set<String> FOR_EXPORT = new HashSet<>();

    public static Instrumentation INST;

    public static File exportDir = new File("mp-exports");
    public static File javaDir = new File("mp-javas");

    public static void premain(String arg, Instrumentation instrumentation) {
        INST = instrumentation;
        LOGGER.info("MP Agent Loading...");
        instrumentation.addTransformer(new MPTransformer(),true);
        File configsDir = new File("mp-classes");
        while(!configsDir.exists() || !exportDir.exists() || !javaDir.exists()){
            configsDir.mkdirs();
            exportDir.mkdirs();
            javaDir.mkdirs();
        }

        initJavaConfig(javaDir);
        initConfig(configsDir);
        initExportConfig(exportDir.toPath().resolve("mp-export-config.json").toFile());

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
                        LOGGER.info("Loaded {}.",file.getName());
                    }
                    else if(file.isFile() && file.getName().toLowerCase().endsWith(".json")){
                        for (JsonElement jsonElement : JsonParser.parseString(FileUtils.readFileToString(file)).getAsJsonArray()) {
                            try {
                                byte[] bytes = getFromURL(jsonElement.getAsString());
                                if(bytes==null){
                                    LOGGER.warn("Ignoring {} because the bytes is null.",file);
                                    return;
                                }
                                ClassNode node = new ClassNode();
                                ClassReader reader = new ClassReader(bytes);
                                reader.accept(node, ASM.API_VERSION);
                                TRANSFORMATIONS.put(node.name,bytes);
                                LOGGER.warn("Loaded {} from {}",node.name,jsonElement);
                            } catch (Throwable e) {
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

    public static void initJavaConfig(File dir){
        File[] files = dir.listFiles();
        if(files!=null){
            for (File file : files) {
                try {
                    if(file.isFile() && file.getName().endsWith(".java")){
                        File compiledFile = CompileUtils.compile(file);
                        if(!compiledFile.exists()) LOGGER.error("Failed to compile {}",file);
                        LOGGER.info("Compiled {}",file);
                    }
                    else if(file.isFile() && file.getName().toLowerCase().endsWith(".json")){
                        for (JsonElement jsonElement : JsonParser.parseString(FileUtils.readFileToString(file)).getAsJsonArray()) {
                            try {
                                byte[] bytes = getFromURL(jsonElement.getAsString());
                                if(bytes==null){
                                    LOGGER.warn("Ignoring java file(s) information {} because the bytes is null.",file);
                                    return;
                                }
                                File onlineFile = javaDir.toPath().resolve("/"+CompileUtils.getFullClassName(bytes).replace(".","/")+"_online").toFile();
                                FileUtils.writeByteArrayToFile(onlineFile,bytes);
                                File compiledFile = CompileUtils.compile(onlineFile);
                                if(!compiledFile.exists()) LOGGER.error("Failed to compile {} from {}",onlineFile,jsonElement);
                                LOGGER.info("Compiled {} from {}",file,jsonElement);
                            } catch (Throwable e) {
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

    public static void initExportConfig(File configFile) {
        try {
            if(!configFile.exists()){
                FileUtils.writeStringToFile(configFile,new JsonArray().toString());
            }
            JsonArray array = JsonParser.parseString(FileUtils.readFileToString(configFile)).getAsJsonArray();
            for (JsonElement jsonElement : array) {
                FOR_EXPORT.add(jsonElement.getAsString().replace(".","/"));
            }
        } catch (Throwable e) {
            LOGGER.error("Failed to init export config: {}", configFile, e);
        }
    }

    public static byte[] getFromURL(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int read;

            byte[] buffer = new byte[1024];
            while((read = inputStream.read(buffer,0,1024))!=-1){
                outputStream.write(buffer,0,read);
            }

            return outputStream.toByteArray();
        } catch (Throwable e) {
            LOGGER.info("Failed to read {}",urlString,e);
            return null;
        }
    }

    public static void agentmain(String arg,Instrumentation instrumentation){
        premain(arg,instrumentation);
    }
}
