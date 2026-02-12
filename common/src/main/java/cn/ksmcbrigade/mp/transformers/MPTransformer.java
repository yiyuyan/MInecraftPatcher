package cn.ksmcbrigade.mp.transformers;

import cn.ksmcbrigade.mp.MPAgent;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MPTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(className==null) return null;
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
