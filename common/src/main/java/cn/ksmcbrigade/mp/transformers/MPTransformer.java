package cn.ksmcbrigade.mp.transformers;

import cn.ksmcbrigade.mp.MPAgent;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MPTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if(className==null) return null;
        if(classFileBuffer!=null && MPAgent.FOR_EXPORT.contains(className)){
            MPAgent.LOGGER.info("Exporting {} before transforming.",className);
            try {
                FileUtils.writeByteArrayToFile(MPAgent.exportDir.toPath().resolve(className+".class").toFile(),classFileBuffer);
            } catch (IOException e) {
                MPAgent.LOGGER.error("Failed to export {}",className,e);
            }
        }
        for (String s : MPAgent.TRANSFORMATIONS.keySet()) {
            if(s.equals(className)){
                MPAgent.LOGGER.info("Transforming {}", className);
                byte[] bytes = MPAgent.TRANSFORMATIONS.get(s);
                if(MPAgent.INST!=null && classBeingRedefined!=null){
                    try {
                        MPAgent.INST.redefineClasses(new ClassDefinition(classBeingRedefined,bytes));
                    } catch (Throwable e) {
                        MPAgent.LOGGER.error("Failed to redefine class: {}", className, e);
                    }
                }
                return bytes;
            }
        }
        return null;
    }
}
