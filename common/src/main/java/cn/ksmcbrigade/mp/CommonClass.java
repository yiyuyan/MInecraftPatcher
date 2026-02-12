package cn.ksmcbrigade.mp;

import cn.ksmcbrigade.mp.platform.Services;
import cn.ksmcbrigade.mp.utils.UnsafeUtils;

import java.io.File;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

    public static String MOD_FILE_FOR_DEVELOPMENT;

    static {
        File parent = new File(System.getProperty("user.dir")).getParentFile();
        if(!Services.PLATFORM.getPlatformName().equals("NeoForge")) parent = parent.getParentFile();
        MOD_FILE_FOR_DEVELOPMENT = new File(parent.getPath()
                + "/build/libs/" + Constants.MOD_NAME
                + "-" + Services.PLATFORM.getPlatformName()
                + "-" + Services.PLATFORM.getGameVersion()
                + "-" + Services.PLATFORM.getModVersion() +".jar")
            .getAbsolutePath();
    }

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {

        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.
        Constants.LOG.info("Hello {} world!",Services.PLATFORM.getPlatformName());
        loadAgent();
    }

    public static void loadAgent(){
        UnsafeUtils.loadAgent(getModFilePath());
    }

    public static String getModFilePath(){
        if(Services.PLATFORM.isDevelopmentEnvironment()){
            return MOD_FILE_FOR_DEVELOPMENT;
        }
        else{
            return UnsafeUtils.getJarPath(CommonClass.class);
        }
    }
}
